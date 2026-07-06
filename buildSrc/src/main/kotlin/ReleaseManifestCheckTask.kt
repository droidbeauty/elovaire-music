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

    @TaskAction
    fun checkManifest() {
        val manifest = manifestFile.asFile.get()
        if (!manifest.isFile) {
            throw GradleException("Release manifest was not generated.")
        }
        val text = manifest.readText()
        listOf(
            "android:usesCleartextTraffic=\"false\"",
            "android:allowBackup=\"false\"",
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
        listOf(
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android:usesCleartextTraffic=\"true\"",
            "android:debuggable=\"true\"",
        ).forEach { value ->
            if (value in text) {
                throw GradleException("Release manifest contains forbidden entry: $value")
            }
        }
    }
}
