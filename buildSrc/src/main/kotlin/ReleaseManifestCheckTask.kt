import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

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
    abstract val baselineProfileFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val startupProfileFile: RegularFileProperty

    @get:Input
    abstract val publicPrivacyPolicyUrl: Property<String>

    @TaskAction
    fun checkManifest() {
        val privacyPolicyUrl = publicPrivacyPolicyUrl.get().trim()
        if (!isPublicPrivacyPolicyUrl(privacyPolicyUrl)) {
            throw GradleException("PRIVACY_POLICY_URL must be a public HTTPS URL for the Play release.")
        }
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
            "android.permission.READ_MEDIA_AUDIO",
            "android:foregroundServiceType=\"mediaPlayback\"",
            "androidx.media3.session.MediaButtonReceiver",
            "ElovaireMediaLibraryService",
            "androidx.media3.session.MediaLibraryService",
            "android.media.browse.MediaBrowserService",
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
            "android.permission.MANAGE_MEDIA",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.ACCESS_MEDIA_LOCATION",
            "android.permission.SCHEDULE_EXACT_ALARM",
            "android.permission.USE_EXACT_ALARM",
            "android.permission.USE_FULL_SCREEN_INTENT",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_PHONE_STATE",
            "android.permission.AD_ID",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "androidx.core.content.FileProvider",
            "AppUpdateInstallReceiver",
            "androidx.work.impl.diagnostics.DiagnosticsReceiver",
            "androidx.profileinstaller.ProfileInstallReceiver",
            "application/vnd.android.package-archive",
            "android.intent.action.INSTALL_PACKAGE",
            "android.settings.MANAGE_UNKNOWN_APP_SOURCES",
        ).forEach { value ->
            if (value in text) {
                throw GradleException("Release manifest contains forbidden entry: $value")
            }
        }
        checkStructuredManifest(manifest)
    }

    private fun checkStructuredManifest(manifest: java.io.File) {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val document = factory.newDocumentBuilder().parse(manifest)
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val manifestElement = document.documentElement
        if (manifestElement.getAttribute("package") != AppBuildConfig.packageName) {
            throw GradleException("Release application ID does not match AppBuildConfig.packageName.")
        }
        if (manifestElement.androidAttribute(androidNamespace, "versionCode").toIntOrNull() != AppBuildConfig.versionCode) {
            throw GradleException("Release version code does not match AppBuildConfig.versionCode.")
        }
        if (manifestElement.androidAttribute(androidNamespace, "versionName") != AppBuildConfig.versionName) {
            throw GradleException("Release version name does not match AppBuildConfig.versionName.")
        }
        val usesSdk = document.getElementsByTagName("uses-sdk").item(0) as? Element
            ?: throw GradleException("Release manifest has no uses-sdk element.")
        val minSdk = usesSdk.androidAttribute(androidNamespace, "minSdkVersion").toIntOrNull()
        if (minSdk != AppBuildConfig.minSdk) {
            throw GradleException("Release min SDK must be ${AppBuildConfig.minSdk}; found $minSdk.")
        }
        val targetSdk = usesSdk.androidAttribute(androidNamespace, "targetSdkVersion").toIntOrNull()
        if (targetSdk != AppBuildConfig.targetSdk) {
            throw GradleException(
                "Release target SDK must be the reviewed stable API ${AppBuildConfig.targetSdk}; found $targetSdk.",
            )
        }
        val application = document.getElementsByTagName("application").item(0) as? Element
            ?: throw GradleException("Release manifest has no application element.")
        if (application.androidAttribute(androidNamespace, "debuggable") == "true") {
            throw GradleException("Release application must not be debuggable.")
        }
        if (application.androidAttribute(androidNamespace, "testOnly") == "true") {
            throw GradleException("Release application must not be test-only.")
        }

        val permissionNames = document.getElementsByTagName("uses-permission")
            .asElementSequence()
            .map { it.androidAttribute(androidNamespace, "name") }
            .toSet()
        val unexpectedPermissions = permissionNames - ALLOWED_RELEASE_PERMISSIONS
        if (unexpectedPermissions.isNotEmpty()) {
            throw GradleException("Release manifest contains unexpected permissions: ${unexpectedPermissions.sorted()}.")
        }
        val legacyStorage = document.getElementsByTagName("uses-permission")
            .asElementSequence()
            .firstOrNull { it.androidAttribute(androidNamespace, "name") == "android.permission.READ_EXTERNAL_STORAGE" }
        if (legacyStorage?.androidAttribute(androidNamespace, "maxSdkVersion") != "32") {
            throw GradleException("READ_EXTERNAL_STORAGE must be capped at API 32.")
        }

        val exported = sequenceOf("activity", "service", "receiver", "provider")
            .flatMap { tag -> document.getElementsByTagName(tag).asElementSequence() }
            .filter { it.androidAttribute(androidNamespace, "exported") == "true" }
            .associateBy { it.androidAttribute(androidNamespace, "name") }
        val unexpectedExported = exported.keys - ALLOWED_EXPORTED_COMPONENTS.keys
        if (unexpectedExported.isNotEmpty()) {
            throw GradleException("Release manifest exports unexpected components: ${unexpectedExported.sorted()}.")
        }
        ALLOWED_EXPORTED_COMPONENTS.forEach { (name, requiredPermission) ->
            val component = exported[name]
                ?: throw GradleException("Required exported component is missing: $name")
            if (
                requiredPermission != null &&
                component.androidAttribute(androidNamespace, "permission") != requiredPermission
            ) {
                throw GradleException("Exported component $name must require $requiredPermission.")
            }
        }
    }

    private fun Element.androidAttribute(namespace: String, name: String): String {
        return getAttributeNS(namespace, name)
    }

    private fun org.w3c.dom.NodeList.asElementSequence(): Sequence<Element> = sequence {
        for (index in 0 until length) {
            (item(index) as? Element)?.let { yield(it) }
        }
    }

    private companion object {
        val ALLOWED_RELEASE_PERMISSIONS = setOf(
            "android.permission.INTERNET",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.WAKE_LOCK",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "${AppBuildConfig.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
        )
        val ALLOWED_EXPORTED_COMPONENTS = mapOf(
            "${AppBuildConfig.packageName}.MainActivity" to null,
            "${AppBuildConfig.packageName}.data.playback.ElovaireMediaLibraryService" to null,
            "androidx.media3.session.MediaButtonReceiver" to null,
            "androidx.work.impl.background.systemjob.SystemJobService" to "android.permission.BIND_JOB_SERVICE",
        )
    }
}

private fun isPublicPrivacyPolicyUrl(value: String): Boolean {
    if (value.any(Char::isWhitespace)) return false
    val uri = runCatching { URI(value) }.getOrNull() ?: return false
    val host = uri.host?.lowercase()?.trimEnd('.') ?: return false
    if (uri.scheme != "https" || uri.userInfo != null || uri.rawFragment != null || '.' !in host) return false
    return host !in setOf("example.com", "example.net", "example.org") &&
        !host.endsWith(".example.com") &&
        !host.endsWith(".example.net") &&
        !host.endsWith(".example.org") &&
        !host.endsWith(".example") &&
        !host.endsWith(".local") &&
        !host.endsWith(".localhost") &&
        !host.endsWith(".test") &&
        !host.endsWith(".invalid")
}
