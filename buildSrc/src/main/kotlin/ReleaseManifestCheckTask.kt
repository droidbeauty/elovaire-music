import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ReleaseManifestCheckTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val backupRulesFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dataExtractionRulesFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fileProviderPathsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineProfileFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val startupProfileFile: RegularFileProperty

    @TaskAction
    fun checkManifest() {
        val manifest = manifestFile.asFile.get()
        if (!manifest.isFile) {
            throw GradleException("Release manifest was not generated.")
        }
        val text = manifest.readText()
        listOf(
            "android:usesCleartextTraffic=\"false\"",
            "android:allowBackup=\"true\"",
            "android:dataExtractionRules=\"@xml/data_extraction_rules\"",
            "android:fullBackupContent=\"@xml/backup_rules\"",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "android:foregroundServiceType=\"mediaPlayback\"",
            "androidx.core.content.FileProvider",
            "androidx.media3.session.MediaButtonReceiver",
            "AppUpdateInstallReceiver",
            "ElovaireMediaLibraryService",
            "androidx.media3.session.MediaLibraryService",
            "android.media.browse.MediaBrowserService",
            ".fileprovider",
        ).forEach { value ->
            if (value !in text) {
                throw GradleException("Release manifest is missing expected entry: $value")
            }
        }
        listOf(backupRulesFile, dataExtractionRulesFile).forEach { rulesFile ->
            val rules = rulesFile.asFile.get().readText()
            if ("portable_settings.xml" !in rules || "domain=\"root\"" in rules) {
                throw GradleException("Backup rules must include only portable settings.")
            }
        }
        val providerPaths = fileProviderPathsFile.asFile.get().readText()
        if ("<cache-path" !in providerPaths || "path=\"updates/\"" !in providerPaths) {
            throw GradleException("FileProvider must expose only the updates cache directory.")
        }
        listOf("<root-path", "<external-path", "<external-files-path", "path=\".\"").forEach { value ->
            if (value in providerPaths) throw GradleException("FileProvider contains forbidden path: $value")
        }
        listOf(baselineProfileFile, startupProfileFile).forEach { profileFile ->
            if (profileFile.asFile.get().readText().isBlank()) {
                throw GradleException("Release profile must not be empty: ${profileFile.asFile.get().name}")
            }
        }
        listOf(
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android:usesCleartextTraffic=\"true\"",
            "android:debuggable=\"true\"",
            "androidx.work.WorkManagerInitializer",
        ).forEach { value ->
            if (value in text) {
                throw GradleException("Release manifest contains forbidden entry: $value")
            }
        }
    }
}
