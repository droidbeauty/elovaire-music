package elovaire.music.droidbeauty.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AppWorkKind
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal data class AppReleaseInfo(
    val versionName: String,
    val tagName: String,
    val downloadUrl: String,
    val checksumUrl: String?,
    val assetSizeBytes: Long?,
    val notes: String,
    val publishedAt: String,
    val assetFileName: String,
)

internal data class AppUpdateUiState(
    val availableRelease: AppReleaseInfo? = null,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val installPermissionRequired: Boolean = false,
    val downloadProgress: Float? = null,
    val errorMessage: String? = null,
    val transientStatus: AppUpdateTransientStatus? = null,
)

internal enum class AppUpdateTransientStatus {
    UpToDate,
}

internal class AppUpdateManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
) : UpdateController {
    private val appContext = context.applicationContext
    private val _uiState = MutableStateFlow(AppUpdateUiState())
    override val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()
    private var checkJob: Job? = null
    private var downloadJob: Job? = null
    private var startupCleanupJob: Job? = null
    private var startupUpdateCheckJob: Job? = null
    private var startupMaintenanceScheduled = false
    private var pendingAutomaticStartupCheck = false
    private var pendingInstallApk: File? = null
    private var resumeInstallAfterPermissionGrant = false

    init {
        if (BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) {
            scope.launch {
                backgroundWorkPolicy.isForeground.collect { isForeground ->
                    if (isForeground) {
                        if (launchPendingInstallIfPermissionGranted()) return@collect
                        if (pendingAutomaticStartupCheck) {
                            pendingAutomaticStartupCheck = false
                            checkForUpdates()
                        }
                    } else {
                        cancelForegroundBoundUpdateWork()
                    }
                }
            }
        }
    }

    override fun checkForUpdates(force: Boolean) {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        if (!backgroundWorkPolicy.canStart(AppWorkKind.ForegroundOnlyMaintenance, userInitiated = force)) {
            if (!force) pendingAutomaticStartupCheck = true
            return
        }
        if (checkJob?.isActive == true || _uiState.value.isDownloading || _uiState.value.isInstalling) return
    
        val automaticCheckStartedAtMs = if (!force) {
            val nowMs = System.currentTimeMillis()
            val elapsedMs = nowMs - preferenceStore.lastAutomaticUpdateCheckAtMs()
            if (elapsedMs in 0 until AUTOMATIC_CHECK_INTERVAL_MS) return
            nowMs
        } else {
            null
        }

        checkJob = scope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null, transientStatus = null) }
            val installedVersion = normalizeVersionLabel(BuildConfig.VERSION_NAME)
            val dismissedVersion = preferenceStore.dismissedUpdateVersion.value
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            if (dismissedVersion != null && !isVersionNewer(dismissedVersion, installedVersion)) {
                preferenceStore.setDismissedUpdateVersion(null)
            }
            val latestReleaseResult = runCatching {
                withContext(Dispatchers.IO) { fetchLatestRelease(installedVersion) }
            }
            latestReleaseResult.exceptionOrNull()?.let { throwable ->
                if (throwable is CancellationException) throw throwable
            }
            if (automaticCheckStartedAtMs != null && latestReleaseResult.isSuccess) {
                preferenceStore.setLastAutomaticUpdateCheckAtMs(automaticCheckStartedAtMs)
            }
            val latestRelease = latestReleaseResult.getOrNull()
            val shouldShow = latestRelease != null && (force || dismissedVersion != latestRelease.versionName)

            _uiState.update { current ->
                current.copy(
                    availableRelease = latestRelease.takeIf { shouldShow },
                    isChecking = false,
                    errorMessage = latestReleaseResult.exceptionOrNull()?.message,
                    transientStatus = if (force && latestRelease == null && latestReleaseResult.isSuccess) {
                        AppUpdateTransientStatus.UpToDate
                    } else {
                        null
                    },
                )
            }
        }.also { job ->
            job.invokeOnCompletion { throwable ->
                checkJob = null
                if (throwable is CancellationException) {
                    _uiState.update { it.copy(isChecking = false) }
                }
            }
        }
    }

    override fun dismissAvailableUpdate() {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        val version = _uiState.value.availableRelease?.versionName ?: return
        preferenceStore.setDismissedUpdateVersion(version)
        _uiState.update { it.copy(availableRelease = null, errorMessage = null) }
    }

    override fun startUpdate() {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        if (!backgroundWorkPolicy.canStart(AppWorkKind.UserInitiatedLongTransfer, userInitiated = true)) return
        val release = _uiState.value.availableRelease ?: return
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            val reusableApk = pendingInstallApk?.takeIf { it.exists() && it.name == release.assetFileName }
            if (reusableApk != null) {
                if (!verifyDownloadedApkOrReport(reusableApk, release)) return@launch
                if (!ensureInstallerPermission(reusableApk)) return@launch
                if (!launchInstallerOrReport(reusableApk)) return@launch
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false,
                        installPermissionRequired = false,
                        downloadProgress = null,
                        errorMessage = null,
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isDownloading = true,
                    isInstalling = false,
                    installPermissionRequired = false,
                    downloadProgress = 0f,
                    errorMessage = null,
                )
            }
            val apkFile = runCatching {
                withContext(Dispatchers.IO) { downloadReleaseApk(release) }
            }.getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false,
                        installPermissionRequired = false,
                        downloadProgress = null,
                        errorMessage = throwable.message ?: "Unable to download update",
                    )
                }
                return@launch
            }

            pendingInstallApk = apkFile
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = true,
                    installPermissionRequired = false,
                    downloadProgress = 1f,
                    errorMessage = null,
                )
            }
            if (!ensureInstallerPermission(apkFile)) return@launch
            if (!launchInstallerOrReport(apkFile)) return@launch
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = false,
                    installPermissionRequired = false,
                    downloadProgress = null,
                    errorMessage = null,
                )
            }
        }.also { job ->
            job.invokeOnCompletion { throwable ->
                downloadJob = null
                if (throwable is CancellationException && _uiState.value.isDownloading) {
                    clearDownloadedInstallers()
                    _uiState.update {
                        it.copy(
                            isDownloading = false,
                            isInstalling = false,
                            installPermissionRequired = false,
                            downloadProgress = null,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    override fun clearInstallState() {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        _uiState.update {
            it.copy(
                isDownloading = false,
                isInstalling = false,
                installPermissionRequired = false,
                downloadProgress = null,
                errorMessage = null,
            )
        }
    }

    override fun clearTransientStatus() {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        _uiState.update { state ->
            if (state.transientStatus == null) state else state.copy(transientStatus = null)
        }
    }

    fun clearDownloadedInstallers() {
        runCatching {
            updatesDirectory().listFiles()?.forEach { file ->
                if (file.isFile && (
                        file.extension.equals("apk", ignoreCase = true) ||
                            file.extension.equals("part", ignoreCase = true)
                    )
                ) {
                    file.delete()
                }
            }
        }
        pendingInstallApk = null
        resumeInstallAfterPermissionGrant = false
    }

    override fun scheduleStartupMaintenance() {
        if (!BuildConfig.ENABLE_GITHUB_UPDATE_FLOW) return
        if (startupMaintenanceScheduled) return
        startupMaintenanceScheduled = true
        startupCleanupJob = scope.launch(Dispatchers.IO) {
            clearDownloadedInstallers()
        }
        startupUpdateCheckJob = scope.launch {
            kotlinx.coroutines.delay(STARTUP_UPDATE_CHECK_DELAY_MS)
            if (backgroundWorkPolicy.shouldStartAutomaticUpdateCheck()) {
                checkForUpdates()
            } else {
                pendingAutomaticStartupCheck = true
            }
        }
    }

    override fun release() {
        checkJob?.cancel()
        checkJob = null
        downloadJob?.cancel()
        downloadJob = null
        startupCleanupJob?.cancel()
        startupCleanupJob = null
        startupUpdateCheckJob?.cancel()
        startupUpdateCheckJob = null
    }

    private fun cancelForegroundBoundUpdateWork() {
        checkJob?.cancel()
        checkJob = null
        if (_uiState.value.isDownloading) {
            downloadJob?.cancel()
            downloadJob = null
            clearDownloadedInstallers()
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = false,
                    installPermissionRequired = false,
                    downloadProgress = null,
                    errorMessage = null,
                )
            }
        }
    }

    private fun fetchLatestRelease(installedVersion: String): AppReleaseInfo? {
        val releases = openGithubConnection(RELEASES_URL).useJsonArray { json ->
            (0 until json.length())
                .mapNotNull(json::optJSONObject)
                .mapNotNull(::parseReleaseInfo)
        }
        return releases
            .filter { release -> isVersionNewer(release.versionName, installedVersion) }
            .maxWithOrNull { left, right -> compareVersions(left.versionName, right.versionName) }
            ?: openGithubConnection(LATEST_RELEASE_URL).useJsonObject(::parseReleaseInfo)
                ?.takeIf { release -> isVersionNewer(release.versionName, installedVersion) }
    }

    private fun openGithubConnection(url: String): HttpURLConnection {
        val parsedUrl = URL(url)
        require(parsedUrl.protocol == "https") { "Update source is invalid" }
        return (parsedUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            instanceFollowRedirects = true
        }
    }

    private fun parseReleaseInfo(json: JSONObject): AppReleaseInfo? {
        if (json.optBoolean("draft") || json.optBoolean("prerelease")) return null
        val tagName = json.optString("tag_name").orEmpty()
        val releaseName = json.optString("name").orEmpty()
        val assets = json.optJSONArray("assets") ?: return null
        val asset = (0 until assets.length())
            .mapNotNull { index -> assets.optJSONObject(index) }
            .firstOrNull { assetJson ->
                val name = assetJson.optString("name").orEmpty().lowercase()
                name.endsWith(".apk") &&
                    ("release" in name || BuildConfig.APPLICATION_ID.lowercase() in name)
            }
            ?: (0 until assets.length())
            .mapNotNull { index -> assets.optJSONObject(index) }
            .firstOrNull { assetJson ->
                assetJson.optString("name").orEmpty().lowercase().endsWith(".apk")
            }
            ?: return null
        val assetName = asset.optString("name").orEmpty()
        val checksumAsset = findChecksumAsset(assets, assetName)
        val versionName = resolveReleaseVersionLabel(
            tagName = tagName,
            releaseName = releaseName,
            assetFileName = assetName,
        )
        if (versionName.isBlank()) return null

        return AppReleaseInfo(
            versionName = versionName,
            tagName = tagName,
            downloadUrl = asset.optString("browser_download_url").orEmpty(),
            checksumUrl = checksumAsset?.optString("browser_download_url").orEmpty().ifBlank { null },
            assetSizeBytes = asset.optLong("size", -1L).takeIf { it > 0L },
            notes = json.optString("body").orEmpty(),
            publishedAt = json.optString("published_at").orEmpty(),
            assetFileName = asset.optString("name").orEmpty().ifBlank { "elovaire-update.apk" },
        ).takeIf { it.downloadUrl.startsWith("https://") }
    }

    private fun findChecksumAsset(
        assets: JSONArray,
        apkAssetName: String,
    ): JSONObject? {
        val lowerApkName = apkAssetName.lowercase()
        val candidates = (0 until assets.length())
            .mapNotNull { index -> assets.optJSONObject(index) }
            .filter { asset ->
                val name = asset.optString("name").orEmpty().lowercase()
                name.endsWith(".sha256") ||
                    name.endsWith(".sha256sum") ||
                    "checksum" in name
            }
        return candidates.firstOrNull { asset ->
            lowerApkName in asset.optString("name").orEmpty().lowercase()
        } ?: candidates.firstOrNull()
    }

    private suspend fun downloadReleaseApk(release: AppReleaseInfo): File {
        val updatesDir = updatesDirectory().apply { mkdirs() }
        val targetFile = File(updatesDir, release.assetFileName)
        val partFile = File(updatesDir, "${release.assetFileName}.part")
        runCatching {
            partFile.delete()
            targetFile.delete()
        }
        val parsedUrl = URL(release.downloadUrl)
        require(parsedUrl.protocol == "https") { "Update source is invalid" }
        val connection = (parsedUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            instanceFollowRedirects = true
        }
        try {
            connection.connect()
            if (connection.url.protocol != "https") {
                throw IllegalStateException("Update source is invalid")
            }
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Update download failed")
            }
            val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
            connection.inputStream.use { input ->
                partFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val progress = totalBytes?.let { bytesCopied.toFloat() / it.toFloat() }
                        _uiState.update { state ->
                            state.copy(downloadProgress = progress?.coerceIn(0f, 1f))
                        }
                    }
                    if (bytesCopied <= 0L || !partFile.exists()) {
                        error("Downloaded update is empty")
                    }
                    if (totalBytes != null && bytesCopied != totalBytes) {
                        error("Downloaded update is incomplete")
                    }
                }
            }

            if (!partFile.renameTo(targetFile)) {
                partFile.copyTo(targetFile, overwrite = true)
                partFile.delete()
            }
            if (!targetFile.exists() || targetFile.length() <= 0L) {
                runCatching { targetFile.delete() }
                error("Downloaded update is invalid")
            }
            release.assetSizeBytes?.let { expectedSize ->
                if (targetFile.length() != expectedSize) {
                    runCatching { targetFile.delete() }
                    error("Downloaded update is incomplete")
                }
            }
            verifyDownloadedApk(targetFile, release)
            return targetFile
        } catch (throwable: Throwable) {
            runCatching { partFile.delete() }
            runCatching { targetFile.delete() }
            throw throwable
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun verifyDownloadedApkOrReport(
        apkFile: File,
        release: AppReleaseInfo,
    ): Boolean {
        val result = runCatching {
            withContext(Dispatchers.IO) { verifyDownloadedApk(apkFile, release) }
        }
        if (result.isSuccess) return true
        runCatching { apkFile.delete() }
        pendingInstallApk = null
        _uiState.update {
            it.copy(
                isDownloading = false,
                isInstalling = false,
                installPermissionRequired = false,
                downloadProgress = null,
                errorMessage = result.exceptionOrNull()?.message ?: "Downloaded update is invalid",
            )
        }
        return false
    }

    private fun verifyDownloadedApk(
        apkFile: File,
        release: AppReleaseInfo,
    ) {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            error("Downloaded update is invalid")
        }
        release.assetSizeBytes?.let { expectedSize ->
            if (apkFile.length() != expectedSize) error("Downloaded update is incomplete")
        }
        val checksumUrl = release.checksumUrl ?: return
        val checksumText = fetchChecksumText(checksumUrl)
        val expectedChecksum = AppUpdateIntegrity.expectedSha256(
            checksumText = checksumText,
            apkFileName = release.assetFileName,
        ) ?: error("Unable to verify update")
        if (!AppUpdateIntegrity.verifySha256(apkFile, expectedChecksum)) {
            error("Update verification failed")
        }
    }

    private fun fetchChecksumText(url: String): String {
        val parsedUrl = URL(url)
        require(parsedUrl.protocol == "https") { "Update source is invalid" }
        val connection = (parsedUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            instanceFollowRedirects = true
        }
        return try {
            connection.connect()
            if (connection.url.protocol != "https") {
                throw IllegalStateException("Update source is invalid")
            }
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Update verification failed")
            }
            connection.inputStream.bufferedReader().use { reader ->
                buildString {
                    val buffer = CharArray(DEFAULT_BUFFER_SIZE)
                    while (length < MAX_CHECKSUM_TEXT_CHARS) {
                        val remaining = MAX_CHECKSUM_TEXT_CHARS - length
                        val read = reader.read(buffer, 0, minOf(buffer.size, remaining))
                        if (read <= 0) break
                        append(buffer, 0, read)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun ensureInstallerPermission(apkFile: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            pendingInstallApk = apkFile
            resumeInstallAfterPermissionGrant = true
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = false,
                    installPermissionRequired = true,
                    downloadProgress = null,
                    errorMessage = "Allow installing updates from this source first.",
                )
            }
            openUnknownAppSourcesSettings()
            return false
        }
        return true
    }

    private suspend fun launchPendingInstallIfPermissionGranted(): Boolean {
        val apkFile = pendingInstallApk ?: return false
        if (!resumeInstallAfterPermissionGrant) return false
        val release = _uiState.value.availableRelease
        if (release != null && !verifyDownloadedApkOrReport(apkFile, release)) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            return false
        }
        resumeInstallAfterPermissionGrant = false
        _uiState.update {
            it.copy(
                isDownloading = false,
                isInstalling = true,
                installPermissionRequired = false,
                downloadProgress = 1f,
                errorMessage = null,
            )
        }
        if (!launchInstallerOrReport(apkFile)) return false
        _uiState.update {
            it.copy(
                isDownloading = false,
                isInstalling = false,
                installPermissionRequired = false,
                downloadProgress = null,
                errorMessage = null,
            )
        }
        return true
    }

    private fun launchInstallerOrReport(apkFile: File): Boolean {
        val launchError = runCatching { launchPackageInstaller(apkFile) }.exceptionOrNull()
        if (launchError != null) {
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = false,
                    installPermissionRequired = false,
                    downloadProgress = null,
                    errorMessage = launchError.message ?: "Unable to install update",
                )
            }
            return false
        }
        return true
    }

    private fun openUnknownAppSourcesSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { appContext.startActivity(intent) }
    }

    private fun launchPackageInstaller(apkFile: File) {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            pendingInstallApk = null
            error("Downloaded update is invalid")
        }
        val apkUri = FileProvider.getUriForFile(
            appContext,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            apkFile,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        appContext.startActivity(intent)
    }

    private fun normalizeVersionLabel(raw: String): String {
        return raw.trim().removePrefix("v").removePrefix("V")
    }

    private fun resolveReleaseVersionLabel(
        tagName: String,
        releaseName: String,
        assetFileName: String,
    ): String {
        val normalizedTag = normalizeVersionLabel(tagName)
        if (normalizedTag.looksLikeSemanticVersion()) return normalizedTag

        val normalizedName = normalizeVersionLabel(releaseName)
        if (normalizedName.looksLikeSemanticVersion()) return normalizedName

        return VERSION_REGEX.find(assetFileName)
            ?.value
            ?.let(::normalizeVersionLabel)
            .orEmpty()
    }

    private fun isVersionNewer(candidate: String, installed: String): Boolean {
        return compareVersions(candidate, installed) > 0
    }

    private fun compareVersions(left: String, right: String): Int {
        val leftParts = left.normalizeVersionParts()
        val rightParts = right.normalizeVersionParts()
        val maxSize = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until maxSize) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) {
                return leftPart.compareTo(rightPart)
            }
        }
        return 0
    }

    private fun String.normalizeVersionParts(): List<Int> {
        return trim()
            .removePrefix("v")
            .removePrefix("V")
            .split('.', '-', '_')
            .mapNotNull { it.toIntOrNull() }
    }

    private fun String.looksLikeSemanticVersion(): Boolean {
        return VERSION_REGEX.containsMatchIn(this)
    }

    private inline fun <T> HttpURLConnection.useJsonArray(block: (JSONArray) -> T): T {
        return try {
            connect()
            if (url.protocol != "https") {
                throw IllegalStateException("Release check failed")
            }
            if (responseCode !in 200..299) {
                throw IllegalStateException("Release check failed")
            }
            val payload = inputStream.bufferedReader().use { it.readText() }
            block(JSONArray(payload))
        } finally {
            disconnect()
        }
    }

    private inline fun <T> HttpURLConnection.useJsonObject(block: (JSONObject) -> T): T {
        return try {
            connect()
            if (url.protocol != "https") {
                throw IllegalStateException("Release check failed")
            }
            if (responseCode !in 200..299) {
                throw IllegalStateException("Release check failed")
            }
            val payload = inputStream.bufferedReader().use { it.readText() }
            block(JSONObject(payload))
        } finally {
            disconnect()
        }
    }

    private fun updatesDirectory(): File = File(appContext.cacheDir, "updates")

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases/latest"
        const val RELEASES_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases"
        const val AUTOMATIC_CHECK_INTERVAL_MS = 12 * 60 * 60 * 1_000L
        const val STARTUP_UPDATE_CHECK_DELAY_MS = 4_500L
        const val NETWORK_TIMEOUT_MS = 12_000
        const val MAX_CHECKSUM_TEXT_CHARS = 64 * 1024
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        val VERSION_REGEX = Regex("""\d+(?:\.\d+)+""")
    }
}
