import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ArchitectureBoundaryCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun checkBoundaries() {
        val violations = mutableListOf<String>()
        sourceFiles.files.filter { it.isFile }.forEach { file ->
            val path = normalizeGuardrailPath(file.invariantSeparatorsPath)
            val text = file.readText()
            val codeWithLiterals = stripCommentsPreservingLiterals(text)
            val code = stripStringLiterals(codeWithLiterals)
            if (coreImportsUi(path, code)) {
                violations += "$path makes the application core depend on a UI implementation"
            }
            NARROW_BOUNDARY_IMPORTS.entries.firstOrNull { path.endsWith(it.key) }?.value
                ?.filter { import -> Regex("(?m)^import\\s+$import$").containsMatchIn(code) }
                ?.forEach { import ->
                    violations += "$path depends directly on $import; use the narrow boundary contract"
                }
            if (
                "/domain/kernel/" in path &&
                (Regex("(?m)^import android(?:x)?[.]").containsMatchIn(code) ||
                    "elovaire.music.droidbeauty.app.data." in code)
            ) {
                violations += "$path makes the domain kernel depend on Android or a data implementation"
            }
            if ("GlobalScope" in code) violations += "$path uses GlobalScope"
            if (
                "/ui/" !in path &&
                    ("System.currentTimeMillis" in code || "SystemClock.elapsedRealtime" in code) &&
                    !isGuardrailPathAllowed(path, CLOCK_ALLOWED)
            ) {
                violations += "$path reads wall or elapsed time outside the application clock boundary"
            }
            FORBIDDEN_OEM_BRANCH_MARKERS.firstOrNull(code::contains)?.let { marker ->
                violations += "$path branches on broad OEM identity without an evidence-backed platform quirk: $marker"
            }
            FORBIDDEN_UPDATE_MARKERS.firstOrNull(code::contains)?.takeIf { _ ->
                !isGuardrailPathAllowed(path, UPDATE_ALLOWED)
            }?.let { marker ->
                violations += "$path reintroduces removed OTA update functionality: $marker"
            }
            FORBIDDEN_ARTWORK_SOURCE_MARKERS.firstOrNull(code::contains)?.let { marker ->
                violations += "$path reintroduces a disallowed album-art source: $marker"
            }
            if ("Channel.UNLIMITED" in code) {
                violations += "$path introduces an unreviewed unbounded operation queue"
            }
            if ("/ui/" in path && "elovaire.music.droidbeauty.app.data.library.db" in code) {
                violations += "$path imports the library database implementation"
            }
            if (
                "/ui/" in path &&
                    Regex("container\\.(playbackManager|libraryRepository|preferenceStore)").containsMatchIn(code) &&
                    !isGuardrailPathAllowed(path, setOf("/ui/screens/root/RootComposition.kt"))
            ) {
                violations += "$path reaches a concrete application service instead of an action/read dependency"
            }
            if ("MediaStore.createWriteRequest" in code && !isGuardrailPathAllowed(path, setOf("/platform/MediaStoreAccessRequests.kt"))) {
                violations += "$path creates MediaStore write requests outside the platform boundary"
            }
            if ("BitmapFactory" in code && !isGuardrailPathAllowed(path, BITMAP_ALLOWED)) {
                violations += "$path decodes bitmaps outside the approved image boundaries"
            }
            if (("HttpURLConnection" in code || ".openConnection(" in code) && !isGuardrailPathAllowed(path, HTTP_ALLOWED)) {
                violations += "$path opens an ad hoc HTTP connection"
            }
            if (Regex("(?<!Bounded)\\bHttpTransport\\(").containsMatchIn(code)) {
                violations += "$path constructs a duplicate HTTP transport"
            }
            if ("AppContainer(" in code && !isGuardrailPathAllowed(path, setOf("/ElovaireApp.kt", "/core/AppContainer.kt"))) {
                violations += "$path constructs the application graph outside ElovaireApp"
            }
            if ("ExoPlayer.Builder" in code && !isGuardrailPathAllowed(path, setOf("/data/playback/PlaybackPlayerFactory.kt"))) {
                violations += "$path creates an ExoPlayer outside the player factory"
            }
            if (
                "AudioFormatPolicy.capabilities" in code &&
                !isGuardrailPathAllowed(path, setOf("/data/audio/AudioFormatPolicy.kt"))
            ) {
                violations += "$path bypasses the audio-format registry API"
            }
            if (" external fun " in code && !isGuardrailPathAllowed(path, NATIVE_ALLOWED)) {
                violations += "$path declares a native entry point outside an approved bridge"
            }
            if (
                ("registerAudioDeviceCallback" in code || "unregisterAudioDeviceCallback" in code) &&
                !isGuardrailPathAllowed(path, setOf("/data/playback/PlaybackRuntimeResources.kt"))
            ) {
                violations += "$path registers audio-device callbacks outside playback runtime resources"
            }
            if (
                ("getSharedPreferences" in code || "SharedPreferences" in code) &&
                !isGuardrailPathAllowed(path, SHARED_PREFERENCES_ALLOWED)
            ) {
                violations += "$path accesses SharedPreferences outside an approved persistence boundary"
            }
            if (path.endsWith("ViewModel.kt") && "AppContainer" in code) {
                violations += "$path depends on the broad application container"
            }
            if (
                "CoroutineScope(SupervisorJob" in code &&
                !isGuardrailPathAllowed(path, SUPERVISOR_SCOPE_ALLOWED)
            ) {
                violations += "$path creates an unapproved independent supervisor scope"
            }
            if (
                ("SharedPreferences" in code || "PreferenceStorage" in code) &&
                LEGACY_USER_DATA_KEYS.any(codeWithLiterals::contains) &&
                !path.endsWith("/data/settings/RoomUserDataStore.kt")
            ) {
                violations += "$path accesses legacy structured preference storage outside its migration boundary"
            }
        }
        if (violations.isNotEmpty()) throw GradleException(violations.joinToString(separator = "\n"))
    }

    private companion object {
        val BITMAP_ALLOWED = setOf(
            "/data/artwork/ArtworkLoader.kt",
            "/data/tags/AlbumTagEditorService.kt",
            "/ui/screens/about/AboutScreens.kt",
        )
        val NARROW_BOUNDARY_IMPORTS = mapOf(
            "/core/AppBridgeCoordinator.kt" to setOf(
                "elovaire.music.droidbeauty.app.core.AppServices",
            ),
            "/core/PlaybackIntegrationCoordinator.kt" to setOf(
                "elovaire.music.droidbeauty.app.data.playback.PlaybackManager",
            ),
            "/data/library/DeviceDeleteCoordinator.kt" to setOf(
                "elovaire.music.droidbeauty.app.data.library.LibraryRepository",
                "elovaire.music.droidbeauty.app.data.playback.PlaybackManager",
            ),
        )
        val HTTP_ALLOWED = setOf(
            "/data/artwork/ArtworkLoader.kt",
            "/data/lyrics/LrclibClient.kt",
            "/data/network/BoundedHttpTransport.kt",
            "/data/update/GitHubUpdateController.kt",
            "/data/library/network/WebDavNetworkFileSystem.kt",
        )
        val NATIVE_ALLOWED = emptySet<String>()
        val SHARED_PREFERENCES_ALLOWED = setOf(
            "/core/AppExitDiagnostics.kt",
            "/data/playback/PlaybackSessionStore.kt",
            "/data/playback/AudiobookProgressStore.kt",
            "/data/playback/UsbDacHardwareVolumeManager.kt",
            "/data/settings/PortableSettingsBackup.kt",
            "/data/settings/SettingsDataStore.kt",
            "/data/settings/PreferenceStorage.kt",
            "/data/settings/PreferenceStore.kt",
            "/data/settings/RoomUserDataStore.kt",
            "/data/library/network/NetworkCredentialStore.kt",
            "/data/library/network/NetworkLibrarySourceStore.kt",
            "/data/library/network/NetworkSourceMutationJournal.kt",
            "/data/library/db/PersistenceMaintenanceWorker.kt",
            "/data/update/GitHubUpdateController.kt",
        )
        val CLOCK_ALLOWED = setOf("/core/AppRuntimeBoundaries.kt")
        val SUPERVISOR_SCOPE_ALLOWED = setOf(
            "/core/PlaybackIntegrationCoordinator.kt",
            "/data/settings/PortableSettingsBackup.kt",
            "/data/settings/PreferenceStore.kt",
            "/data/settings/RoomUserDataStore.kt",
        )
        val LEGACY_USER_DATA_KEYS = setOf(
            "\"favorite_song_ids\"",
            "\"song_play_counts\"",
            "\"album_play_counts\"",
            "\"recent_song_ids\"",
            "\"recent_album_ids\"",
            "\"smart_playlists\"",
        )
        val FORBIDDEN_OEM_BRANCH_MARKERS = setOf(
            "Build.MANUFACTURER",
            "Build.BRAND",
        )
        val FORBIDDEN_UPDATE_MARKERS = setOf(
            "AppUpdateManager",
            "AppUpdateInstallReceiver",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_INSTALL_PACKAGE",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "application/vnd.android.package-archive",
            "browser_download_url",
            "api.github.com/repos/droidbeauty/elovaire-music/releases",
            "github.com/droidbeauty/elovaire-music/releases",
            "releases/latest",
            "download latest APK",
            "dismissed_update_version",
            "last_automatic_update_check_at_ms",
        )
        val UPDATE_ALLOWED = setOf(
            "/data/settings/PreferenceStore.kt",
            "/data/settings/SettingsDataStore.kt",
            "/data/update/GitHubUpdateController.kt",
            "/src/main/AndroidManifest.xml",
        )
        val FORBIDDEN_ARTWORK_SOURCE_MARKERS = setOf(
            "TidalArtworkProvider",
            "tidal.com",
        )
    }
}
