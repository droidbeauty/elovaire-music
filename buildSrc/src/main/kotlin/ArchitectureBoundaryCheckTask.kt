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
            val path = file.invariantSeparatorsPath
            val text = file.readText()
            if (
                "/domain/kernel/" in path &&
                (Regex("(?m)^import android(?:x)?[.]").containsMatchIn(text) || "elovaire.music.droidbeauty.app.data." in text)
            ) {
                violations += "$path makes the domain kernel depend on Android or a data implementation"
            }
            if ("GlobalScope" in text) violations += "$path uses GlobalScope"
            FORBIDDEN_OEM_BRANCH_MARKERS.firstOrNull(text::contains)?.let { marker ->
                violations += "$path branches on broad OEM identity without an evidence-backed platform quirk: $marker"
            }
            FORBIDDEN_UPDATE_MARKERS.firstOrNull(text::contains)?.let { marker ->
                violations += "$path reintroduces removed OTA update functionality: $marker"
            }
            FORBIDDEN_ARTWORK_SOURCE_MARKERS.firstOrNull(text::contains)?.let { marker ->
                violations += "$path reintroduces a disallowed album-art source: $marker"
            }
            if ("Channel.UNLIMITED" in text && !path.endsWith("/data/settings/RoomUserDataStore.kt")) {
                violations += "$path introduces an unreviewed unbounded operation queue"
            }
            if ("/ui/" in path && "elovaire.music.droidbeauty.app.data.library.db" in text) {
                violations += "$path imports the library database implementation"
            }
            if ("MediaStore.createWriteRequest" in text && !path.endsWith("/platform/MediaStoreAccessRequests.kt")) {
                violations += "$path creates MediaStore write requests outside the platform boundary"
            }
            if ("BitmapFactory" in text && BITMAP_ALLOWED.none(path::endsWith)) {
                violations += "$path decodes bitmaps outside the approved image boundaries"
            }
            if (("HttpURLConnection" in text || ".openConnection(" in text) && HTTP_ALLOWED.none(path::endsWith)) {
                violations += "$path opens an ad hoc HTTP connection"
            }
            if ("HttpTransport(" in text) {
                violations += "$path constructs a duplicate HTTP transport"
            }
            if ("AppContainer(" in text && !path.endsWith("/ElovaireApp.kt") && !path.endsWith("/core/AppContainer.kt")) {
                violations += "$path constructs the application graph outside ElovaireApp"
            }
            if ("ExoPlayer.Builder" in text && !path.endsWith("/data/playback/PlaybackPlayerFactory.kt")) {
                violations += "$path creates an ExoPlayer outside the player factory"
            }
            if (
                "AudioFormatPolicy.capabilities" in text &&
                !path.endsWith("/data/audio/AudioFormatPolicy.kt")
            ) {
                violations += "$path bypasses the audio-format registry API"
            }
            if (" external fun " in text && NATIVE_ALLOWED.none(path::endsWith)) {
                violations += "$path declares a native entry point outside an approved bridge"
            }
            if (
                ("registerAudioDeviceCallback" in text || "unregisterAudioDeviceCallback" in text) &&
                !path.endsWith("/data/playback/PlaybackRuntimeResources.kt")
            ) {
                violations += "$path registers audio-device callbacks outside playback runtime resources"
            }
            if (
                ("getSharedPreferences" in text || "SharedPreferences" in text) &&
                SHARED_PREFERENCES_ALLOWED.none(path::endsWith)
            ) {
                violations += "$path accesses SharedPreferences outside an approved persistence boundary"
            }
            if (path.endsWith("ViewModel.kt") && "AppContainer" in text) {
                violations += "$path depends on the broad application container"
            }
            if (
                "CoroutineScope(SupervisorJob" in text &&
                SUPERVISOR_SCOPE_ALLOWED.none(path::endsWith)
            ) {
                violations += "$path creates an unapproved independent supervisor scope"
            }
            if (
                ("SharedPreferences" in text || "PreferenceStorage" in text) &&
                LEGACY_USER_DATA_KEYS.any(text::contains) &&
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
        val HTTP_ALLOWED = emptySet<String>()
        val NATIVE_ALLOWED = emptySet<String>()
        val SHARED_PREFERENCES_ALLOWED = setOf(
            "/core/AppExitDiagnostics.kt",
            "/data/playback/PlaybackSessionStore.kt",
            "/data/playback/UsbDacHardwareVolumeManager.kt",
            "/data/settings/PortableSettingsBackup.kt",
            "/data/settings/PreferenceStorage.kt",
            "/data/settings/PreferenceStore.kt",
            "/data/settings/RoomUserDataStore.kt",
        )
        val SUPERVISOR_SCOPE_ALLOWED = setOf(
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
        val FORBIDDEN_ARTWORK_SOURCE_MARKERS = setOf(
            "TidalArtworkProvider",
            "tidal.com",
        )
    }
}
