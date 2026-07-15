package elovaire.music.droidbeauty.app.data.update

import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AppWorkKind
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.network.HttpRequest
import elovaire.music.droidbeauty.app.data.network.HttpTransport
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
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
    context: Context,
    private val scope: CoroutineScope,
    private val preferences: UpdatePreferencesStore,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val clock: AppClock = AndroidAppClock,
) : UpdateController {
    private val appContext = context.applicationContext
    private val httpTransport = HttpTransport()
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
    private var lastAutomaticCheckFailureElapsedMs: Long? = null
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private var foregroundJob: Job? = null

    override fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        foregroundJob = scope.launch {
            backgroundWorkPolicy.isForeground.collect { isForeground ->
                if (released.get()) return@collect
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

    override fun checkForUpdates(force: Boolean) {
        if (released.get()) return
        if (!backgroundWorkPolicy.canStart(AppWorkKind.ForegroundOnlyMaintenance, userInitiated = force)) {
            if (!force) pendingAutomaticStartupCheck = true
            return
        }
        if (checkJob?.isActive == true || _uiState.value.isDownloading || _uiState.value.isInstalling) return
    
        val automaticCheckStartedAtMs = if (!force) {
            val nowMs = clock.wallTimeMs()
            val nowElapsedMs = clock.elapsedTimeMs()
            val shouldRun = shouldRunAutomaticUpdateCheck(
                lastSuccessfulWallTimeMs = preferences.lastAutomaticUpdateCheckAtMs(),
                nowWallTimeMs = nowMs,
                lastFailureElapsedTimeMs = lastAutomaticCheckFailureElapsedMs,
                nowElapsedTimeMs = nowElapsedMs,
                successIntervalMs = AUTOMATIC_CHECK_INTERVAL_MS,
                failureBackoffMs = AUTOMATIC_CHECK_FAILURE_BACKOFF_MS,
            )
            if (!shouldRun) return
            nowMs
        } else {
            null
        }

        checkJob = scope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null, transientStatus = null) }
            val installedVersion = AppVersionPolicy.normalize(BuildConfig.VERSION_NAME)
            val dismissedVersion = preferences.dismissedUpdateVersion.value
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            if (dismissedVersion != null && !AppVersionPolicy.isNewer(dismissedVersion, installedVersion)) {
                preferences.setDismissedUpdateVersion(null)
            }
            val latestReleaseResult = runCatching {
                withContext(Dispatchers.IO) {
                    ElovaireTrace.section("update_release_fetch") {
                        fetchLatestRelease(installedVersion)
                    }
                }
            }
            latestReleaseResult.exceptionOrNull()?.let { throwable ->
                if (throwable is CancellationException) throw throwable
            }
            if (automaticCheckStartedAtMs != null && latestReleaseResult.isSuccess) {
                lastAutomaticCheckFailureElapsedMs = null
                preferences.setLastAutomaticUpdateCheckAtMs(automaticCheckStartedAtMs)
            } else if (automaticCheckStartedAtMs != null) {
                lastAutomaticCheckFailureElapsedMs = clock.elapsedTimeMs()
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
                if (!released.get() && throwable is CancellationException) {
                    _uiState.update { it.copy(isChecking = false) }
                }
            }
        }
    }

    override fun dismissAvailableUpdate() {
        val version = _uiState.value.availableRelease?.versionName ?: return
        preferences.setDismissedUpdateVersion(version)
        _uiState.update { it.copy(availableRelease = null, errorMessage = null) }
    }

    override fun startUpdate() {
        if (released.get()) return
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
                if (!released.get() && throwable is CancellationException && _uiState.value.isDownloading) {
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
        _uiState.update { state ->
            if (state.transientStatus == null) state else state.copy(transientStatus = null)
        }
    }

    fun clearDownloadedInstallers() {
        val keepPendingInstall = pendingInstallApk
            ?.takeIf { resumeInstallAfterPermissionGrant || _uiState.value.installPermissionRequired || _uiState.value.isInstalling }
            ?.canonicalFile
        runCatching {
            updatesDirectory().listFiles()?.forEach { file ->
                if (keepPendingInstall != null && file.canonicalFile == keepPendingInstall) return@forEach
                if (file.isFile && (
                        file.extension.equals("apk", ignoreCase = true) ||
                            file.extension.equals("part", ignoreCase = true)
                    )
                ) {
                    file.delete()
                }
            }
        }
        if (keepPendingInstall == null) {
            pendingInstallApk = null
            resumeInstallAfterPermissionGrant = false
        }
    }

    override fun scheduleStartupMaintenance() {
        if (released.get() || startupMaintenanceScheduled) return
        startupMaintenanceScheduled = true
        startupCleanupJob = scope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(STARTUP_CLEANUP_DELAY_MS)
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
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        foregroundJob?.cancel()
        foregroundJob = null
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
        return withUpdateTrafficStatsTag {
            val releases = JSONArray(fetchText(RELEASES_URL, GITHUB_ACCEPT)).let { json ->
                (0 until json.length())
                    .mapNotNull(json::optJSONObject)
                    .mapNotNull(::parseReleaseInfo)
            }
            releases
                .filter { release -> AppVersionPolicy.isNewer(release.versionName, installedVersion) }
                .maxWithOrNull { left, right -> AppVersionPolicy.compare(left.versionName, right.versionName) }
                ?: parseReleaseInfo(JSONObject(fetchText(LATEST_RELEASE_URL, GITHUB_ACCEPT)))
                    ?.takeIf { release -> AppVersionPolicy.isNewer(release.versionName, installedVersion) }
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
                val name = assetJson.optString("name").orEmpty().lowercase(Locale.ROOT)
                name.endsWith(".apk") &&
                    ("release" in name || BuildConfig.APPLICATION_ID.lowercase(Locale.ROOT) in name)
            }
            ?: (0 until assets.length())
            .mapNotNull { index -> assets.optJSONObject(index) }
            .firstOrNull { assetJson ->
                assetJson.optString("name").orEmpty().lowercase(Locale.ROOT).endsWith(".apk")
            }
            ?: return null
        val assetName = asset.optString("name").orEmpty()
        val checksumAsset = findChecksumAsset(assets, assetName)
        val versionName = AppVersionPolicy.resolve(
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
        val lowerApkName = apkAssetName.lowercase(Locale.ROOT)
        val candidates = (0 until assets.length())
            .mapNotNull { index -> assets.optJSONObject(index) }
            .filter { asset ->
                val name = asset.optString("name").orEmpty().lowercase(Locale.ROOT)
                name.endsWith(".sha256") ||
                    name.endsWith(".sha256sum") ||
                    "checksum" in name
            }
        return candidates.firstOrNull { asset ->
            lowerApkName in asset.optString("name").orEmpty().lowercase(Locale.ROOT)
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
        require(isTrustedUpdateDownloadUrl(parsedUrl)) { "Update source is invalid" }
        val connection = (parsedUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            instanceFollowRedirects = true
        }
        val previousTrafficStatsTag = TrafficStats.getThreadStatsTag()
        try {
            TrafficStats.setThreadStatsTag(UPDATE_TRAFFIC_STATS_TAG)
            connection.connect()
            if (!isTrustedUpdateDownloadUrl(connection.url)) {
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
                    val progressThrottler = UpdateDownloadProgressThrottler()
                    val downloadContext = currentCoroutineContext()
                    ElovaireTrace.section("update_apk_download_copy") {
                        while (true) {
                            downloadContext.ensureActive()
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            bytesCopied += read
                            val progress = totalBytes?.let { bytesCopied.toFloat() / it.toFloat() }
                            val normalizedProgress = progress?.coerceIn(0f, 1f)
                            if (normalizedProgress != null && progressThrottler.shouldEmit(
                                    progress = normalizedProgress,
                                    nowMs = clock.elapsedTimeMs(),
                                )
                            ) {
                                _uiState.update { state ->
                                    state.copy(downloadProgress = normalizedProgress)
                                }
                            }
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
            TrafficStats.setThreadStatsTag(previousTrafficStatsTag)
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
        result.exceptionOrNull()?.let { failure ->
            if (failure is CancellationException) throw failure
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
        return withUpdateTrafficStatsTag {
            fetchText(url, "text/plain", MAX_CHECKSUM_TEXT_CHARS)
        }
    }

    private fun fetchText(
        url: String,
        accept: String,
        maxBytes: Int = MAX_RELEASE_METADATA_BYTES,
    ): String {
        return httpTransport.getText(
            request = HttpRequest(
                url = url,
                accept = accept,
                headers = mapOf(
                    "User-Agent" to "Elovaire/${BuildConfig.VERSION_NAME}",
                    "X-GitHub-Api-Version" to "2022-11-28",
                ),
                connectTimeoutMs = NETWORK_TIMEOUT_MS,
                readTimeoutMs = NETWORK_TIMEOUT_MS,
            ),
            maxBytes = maxBytes,
        )
    }

    private inline fun <T> withUpdateTrafficStatsTag(block: () -> T): T {
        val previousTag = TrafficStats.getThreadStatsTag()
        TrafficStats.setThreadStatsTag(UPDATE_TRAFFIC_STATS_TAG)
        return try {
            block()
        } finally {
            TrafficStats.setThreadStatsTag(previousTag)
        }
    }

    private fun ensureInstallerPermission(apkFile: File): Boolean {
        if (!appContext.packageManager.canRequestPackageInstalls()) {
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
            if (!openUnknownAppSourcesSettings()) {
                resumeInstallAfterPermissionGrant = false
                _uiState.update {
                    it.copy(
                        installPermissionRequired = false,
                        errorMessage = "Unable to open install permission settings.",
                    )
                }
            }
            return false
        }
        return true
    }

    private suspend fun launchPendingInstallIfPermissionGranted(): Boolean {
        val apkFile = pendingInstallApk ?: return false
        if (!resumeInstallAfterPermissionGrant) return false
        val release = _uiState.value.availableRelease
        if (release != null && !verifyDownloadedApkOrReport(apkFile, release)) return false
        if (!appContext.packageManager.canRequestPackageInstalls()) {
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

    private fun openUnknownAppSourcesSettings(): Boolean {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            if (intent.resolveActivity(appContext.packageManager) == null) return@runCatching false
            appContext.startActivity(intent)
            true
        }.getOrDefault(false)
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
        check(intent.resolveActivity(appContext.packageManager) != null) {
            "No package installer is available"
        }
        appContext.startActivity(intent)
    }

    private fun updatesDirectory(): File = File(appContext.cacheDir, "updates")

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases/latest"
        const val RELEASES_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases"
        const val GITHUB_ACCEPT = "application/vnd.github+json"
        const val AUTOMATIC_CHECK_INTERVAL_MS = 12 * 60 * 60 * 1_000L
        const val STARTUP_UPDATE_CHECK_DELAY_MS = 4_500L
        const val STARTUP_CLEANUP_DELAY_MS = 8_000L
        const val AUTOMATIC_CHECK_FAILURE_BACKOFF_MS = 30 * 60 * 1_000L
        const val NETWORK_TIMEOUT_MS = 12_000
        const val MAX_RELEASE_METADATA_BYTES = 1 * 1024 * 1024
        const val MAX_CHECKSUM_TEXT_CHARS = 64 * 1024
        const val UPDATE_TRAFFIC_STATS_TAG = 0x454C5550
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}

internal fun isTrustedUpdateDownloadUrl(url: URL): Boolean {
    if (url.protocol != "https") return false
    val host = url.host.lowercase(Locale.ROOT)
    return host == "github.com" || host.endsWith(".github.com") || host.endsWith(".githubusercontent.com")
}

private const val DEFAULT_DOWNLOAD_PROGRESS_DELTA = 0.01f
private const val DEFAULT_DOWNLOAD_PROGRESS_INTERVAL_MS = 150L

internal class UpdateDownloadProgressThrottler(
    private val minimumProgressDelta: Float = DEFAULT_DOWNLOAD_PROGRESS_DELTA,
    private val minimumIntervalMs: Long = DEFAULT_DOWNLOAD_PROGRESS_INTERVAL_MS,
) {
    private var lastProgress = 0f
    private var lastUpdateMs = -minimumIntervalMs

    fun shouldEmit(
        progress: Float,
        nowMs: Long,
    ): Boolean {
        val normalizedProgress = progress.coerceIn(0f, 1f)
        if (normalizedProgress < lastProgress) return false
        if (normalizedProgress >= 1f || normalizedProgress - lastProgress >= minimumProgressDelta) {
            lastProgress = normalizedProgress
            lastUpdateMs = nowMs
            return true
        }
        if (nowMs - lastUpdateMs >= minimumIntervalMs) {
            lastProgress = normalizedProgress
            lastUpdateMs = nowMs
            return true
        }
        return false
    }
}
