package elovaire.music.droidbeauty.app.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.AppWorkKind
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStore
import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class GitHubUpdateController(
    context: Context,
    private val scope: CoroutineScope,
    private val preferences: UpdatePreferencesStore,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val clock: AppClock = AndroidAppClock,
) : UpdateController {
    private val appContext = context.applicationContext
    private val boundedHttpTransport = BoundedHttpTransport(
        connectTimeoutMs = 12_000,
        readTimeoutMs = 12_000,
    )
    private val _uiState = MutableStateFlow(AppUpdateUiState())
    override val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()
    override val isSupported: Boolean = true
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private var foregroundJob: Job? = null
    private var checkJob: Job? = null
    private var downloadJob: Job? = null
    private var startupJob: Job? = null
    private var cleanupJob: Job? = null
    private var pendingInstallApk: File? = null
    private var resumeInstallAfterPermission = false
    private var pendingAutomaticCheck = false
    private var lastAutomaticFailureElapsedMs: Long? = null

    override fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        foregroundJob = scope.launch {
            backgroundWorkPolicy.isForeground.collect { foreground ->
                if (released.get()) return@collect
                if (foreground) {
                    if (launchPendingInstallIfAllowed()) return@collect
                    if (pendingAutomaticCheck) {
                        pendingAutomaticCheck = false
                        checkForUpdates()
                    }
                } else {
                    checkJob?.cancel()
                    checkJob = null
                    if (_uiState.value.isDownloading) cancelDownload()
                }
            }
        }
    }

    override fun checkForUpdates(force: Boolean) {
        if (released.get()) return
        if (!backgroundWorkPolicy.canStart(AppWorkKind.ForegroundOnlyMaintenance, userInitiated = force)) {
            if (!force) pendingAutomaticCheck = true
            return
        }
        if (checkJob?.isActive == true || downloadJob?.isActive == true) return
        val automaticStart = if (force) null else {
            val now = clock.wallTimeMs()
            if (!shouldRunAutomaticUpdateCheck(
                    lastSuccessfulWallTimeMs = preferences.lastAutomaticUpdateCheckAtMs(),
                    nowWallTimeMs = now,
                    lastFailureElapsedTimeMs = lastAutomaticFailureElapsedMs,
                    nowElapsedTimeMs = clock.elapsedTimeMs(),
                    successIntervalMs = AUTOMATIC_CHECK_INTERVAL_MS,
                    failureBackoffMs = AUTOMATIC_FAILURE_BACKOFF_MS,
                )
            ) return
            now
        }
        checkJob = scope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null, transientStatus = null) }
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    ElovaireTrace.section("update_release_fetch") {
                        GitHubReleaseClient.fetchNewerRelease(BuildConfig.VERSION_NAME)
                    }
                }
            }
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            if (automaticStart != null) {
                if (result.isSuccess) {
                    lastAutomaticFailureElapsedMs = null
                    preferences.setLastAutomaticUpdateCheckAtMs(automaticStart)
                } else {
                    lastAutomaticFailureElapsedMs = clock.elapsedTimeMs()
                }
            }
            val release = result.getOrNull()
            val dismissed = preferences.dismissedUpdateVersion.value
            if (dismissed != null && !AppVersionPolicy.isNewer(dismissed, BuildConfig.VERSION_NAME)) {
                preferences.setDismissedUpdateVersion(null)
            }
            val shouldShow = release != null && (force || dismissed != release.versionName)
            _uiState.update {
                it.copy(
                    availableRelease = release.takeIf { shouldShow },
                    isChecking = false,
                    errorMessage = result.exceptionOrNull()?.let(::userMessage),
                    transientStatus = if (force && release == null && result.isSuccess) {
                        AppUpdateTransientStatus.UpToDate
                    } else null,
                )
            }
        }.also { job ->
            job.invokeOnCompletion { cause ->
                if (checkJob === job) checkJob = null
                if (!released.get() && cause is CancellationException) {
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
        if (released.get() || downloadJob?.isActive == true) return
        if (!backgroundWorkPolicy.canStart(AppWorkKind.UserInitiatedLongTransfer, userInitiated = true)) return
        val release = _uiState.value.availableRelease ?: return
        downloadJob = scope.launch {
            val existing = pendingInstallApk?.takeIf { it.isFile && it.name == release.assetFileName }
            val apk = if (existing != null) {
                if (!verifyDownloadedApkOrReport(existing, release)) return@launch
                existing
            } else {
                _uiState.update { it.copy(isDownloading = true, isInstalling = false, installPermissionRequired = false, downloadProgress = 0f, errorMessage = null) }
                runCatching { withContext(Dispatchers.IO) { downloadReleaseApk(release) } }.getOrElse { failure ->
                    if (failure is CancellationException) throw failure
                    reportFailure(failure)
                    return@launch
                }
            }
            pendingInstallApk = apk
            _uiState.update { it.copy(isDownloading = false, isInstalling = true, installPermissionRequired = false, downloadProgress = 1f, errorMessage = null) }
            if (!ensureInstallerPermission(apk)) return@launch
            launchInstallerOrReport(apk)
        }.also { job ->
            job.invokeOnCompletion { cause ->
                if (downloadJob === job) downloadJob = null
                if (!released.get() && cause is CancellationException) {
                    _uiState.update { it.copy(isDownloading = false, isInstalling = false, installPermissionRequired = false, downloadProgress = null) }
                }
            }
        }
    }

    override fun clearInstallState() {
        pendingInstallApk = null
        resumeInstallAfterPermission = false
        _uiState.update { it.copy(isDownloading = false, isInstalling = false, installPermissionRequired = false, downloadProgress = null) }
    }

    override fun clearTransientStatus() {
        _uiState.update { it.copy(transientStatus = null) }
    }

    override fun scheduleStartupMaintenance() {
        if (released.get() || startupJob != null) return
        startupJob = scope.launch {
            delay(STARTUP_UPDATE_CHECK_DELAY_MS)
            if (!backgroundWorkPolicy.isForeground.value) {
                pendingAutomaticCheck = true
                return@launch
            }
            checkForUpdates()
        }
        cleanupJob = scope.launch(Dispatchers.IO) {
            delay(STARTUP_CLEANUP_DELAY_MS)
            cleanupStagedFiles()
        }
    }

    override fun release() {
        if (!released.compareAndSet(false, true)) return
        foregroundJob?.cancel()
        checkJob?.cancel()
        downloadJob?.cancel()
        startupJob?.cancel()
        cleanupJob?.cancel()
    }

    private fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.update { it.copy(isDownloading = false, downloadProgress = null) }
    }

    private suspend fun downloadReleaseApk(release: AppReleaseInfo): File {
        val directory = updatesDirectory().apply { mkdirs() }
        require(AppUpdateIntegrity.isSafeApkFileName(release.assetFileName)) { "Update asset name is invalid" }
        val target = File(directory, release.assetFileName)
        val part = File(directory, "${release.assetFileName}.part")
        part.delete()
        target.delete()
        var completed = false
        try {
            withTrustedConnection(release.downloadUrl, "application/vnd.android.package-archive") { connection ->
                if (connection.responseCode !in 200..299) error("Update download failed")
                val expectedLength = connection.contentLengthLong.takeIf { it > 0L }
                val downloadLimit = AppUpdateIntegrity.downloadLimit(expectedLength, release.assetSizeBytes)
                var copied = 0L
                val throttler = UpdateDownloadProgressThrottler()
                connection.inputStream.use { input ->
                    part.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer)
                            if (count <= 0) break
                            copied = AppUpdateIntegrity.checkedDownloadedByteCount(copied, count, downloadLimit)
                            output.write(buffer, 0, count)
                            val progress = expectedLength?.let { (copied.toFloat() / it).coerceIn(0f, 1f) }
                            if (progress != null && throttler.shouldEmit(progress, clock.elapsedTimeMs())) {
                                _uiState.update { it.copy(downloadProgress = progress) }
                            }
                        }
                    }
                }
                if (copied <= 0L || (expectedLength != null && copied != expectedLength)) error("Downloaded update is incomplete")
            }
            if (!part.renameTo(target)) {
                part.copyTo(target, overwrite = true)
                part.delete()
            }
            release.assetSizeBytes?.let { require(target.length() == it) { "Downloaded update is incomplete" } }
            verifyDownloadedApk(target, release)
            completed = true
            return target
        } finally {
            if (!completed) {
                part.delete()
                target.delete()
            }
        }
    }

    private suspend fun verifyDownloadedApkOrReport(file: File, release: AppReleaseInfo): Boolean {
        val result = runCatching { withContext(Dispatchers.IO) { verifyDownloadedApk(file, release) } }
        result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
        if (result.isSuccess) return true
        file.delete()
        pendingInstallApk = null
        reportFailure(result.exceptionOrNull() ?: IllegalStateException("Downloaded update is invalid"))
        return false
    }

    private suspend fun verifyDownloadedApk(file: File, release: AppReleaseInfo) {
        require(file.isFile && file.length() > 0L) { "Downloaded update is invalid" }
        val checksum = release.checksumSha256 ?: release.checksumUrl?.let { url ->
            val response = boundedHttpTransport.get(
                rawUrl = url,
                headers = mapOf(
                    "Accept" to "text/plain",
                    "User-Agent" to "Elovaire/${BuildConfig.VERSION_NAME}",
                ),
                maxBytes = MAX_CHECKSUM_TEXT_CHARS,
                urlPolicy = ::isTrustedUpdateUrl,
            )
            if (response.statusCode !in 200..299) error("Unable to verify update")
            AppUpdateIntegrity.expectedSha256(
                response.body.toString(Charsets.UTF_8),
                release.assetFileName,
            )
        } ?: error("Unable to verify update")
        require(AppUpdateIntegrity.verifySha256(file, checksum)) { "Update verification failed" }
        val packageInfo = appContext.packageManager.getPackageArchiveInfo(
            file.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: error("Downloaded update is not a valid app")
        require(packageInfo.packageName == BuildConfig.APPLICATION_ID) { "Downloaded update targets another app" }
        val expectedVersion = AppVersionPolicy.normalize(release.versionName)
        require(AppVersionPolicy.normalize(packageInfo.versionName.orEmpty()) == expectedVersion) { "Downloaded update version is invalid" }
        require(packageInfo.longVersionCode > BuildConfig.VERSION_CODE) { "Downloaded update is not newer" }
        val installed = appContext.packageManager.getPackageInfo(
            BuildConfig.APPLICATION_ID,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )
        val installedDigests = signerDigests(installed)
        require(installedDigests.isNotEmpty() && signerDigests(packageInfo).containsAll(installedDigests)) {
            "Downloaded update is signed by an unknown certificate"
        }
    }

    private fun signerDigests(info: android.content.pm.PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners?.toList().orEmpty()
        } else {
            @Suppress("DEPRECATION") info.signatures?.toList().orEmpty()
        }
        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString("") { byte -> "%02x".format(byte) }
        }.toSet()
    }

    private fun ensureInstallerPermission(file: File): Boolean {
        if (appContext.packageManager.canRequestPackageInstalls()) return true
        pendingInstallApk = file
        resumeInstallAfterPermission = true
        _uiState.update { it.copy(isDownloading = false, isInstalling = false, installPermissionRequired = true, downloadProgress = null, errorMessage = "Allow installing updates from this source first.") }
        val opened = runCatching {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(appContext.packageManager) == null) false else {
                appContext.startActivity(intent)
                true
            }
        }.getOrDefault(false)
        if (!opened) reportFailure(IllegalStateException("Unable to open install permission settings"))
        return false
    }

    private suspend fun launchPendingInstallIfAllowed(): Boolean {
        val file = pendingInstallApk ?: return false
        if (!resumeInstallAfterPermission || !appContext.packageManager.canRequestPackageInstalls()) return false
        val release = _uiState.value.availableRelease ?: return false
        if (!verifyDownloadedApkOrReport(file, release)) return false
        resumeInstallAfterPermission = false
        _uiState.update { it.copy(isInstalling = true, installPermissionRequired = false, errorMessage = null) }
        launchInstallerOrReport(file)
        return true
    }

    private fun launchInstallerOrReport(file: File): Boolean {
        val result = runCatching {
            val uri = FileProvider.getUriForFile(appContext, "${BuildConfig.APPLICATION_ID}.update.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, APK_MIME_TYPE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            check(intent.resolveActivity(appContext.packageManager) != null) { "No package installer is available" }
            appContext.startActivity(intent)
        }
        result.exceptionOrNull()?.let {
            reportFailure(it)
            return false
        }
        _uiState.update { it.copy(isInstalling = false, installPermissionRequired = false, downloadProgress = null, errorMessage = null) }
        pendingInstallApk = null
        return true
    }

    private fun reportFailure(failure: Throwable) {
        _uiState.update { it.copy(isChecking = false, isDownloading = false, isInstalling = false, installPermissionRequired = false, downloadProgress = null, errorMessage = userMessage(failure)) }
    }

    private fun cleanupStagedFiles() {
        val keep = pendingInstallApk?.canonicalFile
        updatesDirectory().listFiles().orEmpty().forEach { file ->
            if (file.canonicalFile == keep) return@forEach
            if (file.extension.equals("part", true) || file.extension.equals("apk", true)) file.delete()
        }
    }

    private fun updatesDirectory(): File = File(appContext.filesDir, "updates")

    private fun userMessage(failure: Throwable): String = failure.message ?: "Unable to check for updates"

    private companion object {
        const val AUTOMATIC_CHECK_INTERVAL_MS = 12L * 60L * 60L * 1_000L
        const val AUTOMATIC_FAILURE_BACKOFF_MS = 30L * 60L * 1_000L
        const val STARTUP_UPDATE_CHECK_DELAY_MS = 4_500L
        const val STARTUP_CLEANUP_DELAY_MS = 8_000L
        const val MAX_CHECKSUM_TEXT_CHARS = 64 * 1024
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}

private object GitHubReleaseClient {
    private val transport = BoundedHttpTransport(
        connectTimeoutMs = 12_000,
        readTimeoutMs = 12_000,
    )

    suspend fun fetchNewerRelease(installedVersion: String): AppReleaseInfo? {
        val latest = runNetworkCatching { parseRelease(JSONObject(getText(LATEST_RELEASE_URL))) }.getOrNull()
        if (latest != null && AppVersionPolicy.isNewer(latest.versionName, installedVersion)) {
            return latest
        }
        val releases = runNetworkCatching { JSONArray(getText(RELEASES_URL)) }.getOrNull() ?: return null
        return buildList {
            for (index in 0 until releases.length()) {
                parseRelease(releases.optJSONObject(index))?.let(::add)
            }
        }
            .filter { AppVersionPolicy.isNewer(it.versionName, installedVersion) }
            .maxWithOrNull { left, right -> AppVersionPolicy.compare(left.versionName, right.versionName) }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> runNetworkCatching(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }

    private fun parseRelease(json: JSONObject?): AppReleaseInfo? {
        if (json == null || json.optBoolean("draft") || json.optBoolean("prerelease")) return null
        val tag = json.optString("tag_name")
        val name = json.optString("name")
        val assets = json.optJSONArray("assets") ?: return null
        val apkAssets = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }
            .filter { it.optString("name").lowercase(Locale.ROOT).endsWith(".apk") }
            .filterNot { it.optString("name").lowercase(Locale.ROOT).contains("play") || it.optString("name").lowercase(Locale.ROOT).contains("debug") }
        val asset = if (apkAssets.size == 1) {
            apkAssets.single()
        } else {
            apkAssets
                .filter { it.optString("name").lowercase(Locale.ROOT).contains("github") }
                .singleOrNull()
        } ?: return null
        val assetName = asset.optString("name").takeIf(AppUpdateIntegrity::isSafeApkFileName) ?: return null
        val assetSize = asset.optLong("size", -1L).takeIf { it > 0L }
        if (assetSize != null && assetSize > AppUpdateIntegrity.MAX_APK_BYTES) return null
        val version = AppVersionPolicy.resolve(tag, name, assetName).takeIf { it.isNotBlank() } ?: return null
        val downloadUrl = asset.optString("browser_download_url").takeIf(::isTrustedDownloadUrl) ?: return null
        val checksumAsset = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }
            .firstOrNull { item ->
                val itemName = item.optString("name").lowercase(Locale.ROOT)
                (itemName.endsWith(".sha256") || itemName.endsWith(".sha256sum")) && itemName.contains(assetName.lowercase(Locale.ROOT))
            }
        val digest = asset.optString("digest").removePrefix("sha256:").takeIf { AppUpdateIntegrity.verifyDigestShape(it) }
        val pageUrl = json.optString("html_url").takeIf(::isTrustedReleasePageUrl) ?: return null
        return AppReleaseInfo(
            versionName = version,
            tagName = tag,
            downloadUrl = downloadUrl,
            checksumUrl = checksumAsset?.optString("browser_download_url")?.takeIf(::isTrustedDownloadUrl),
            checksumSha256 = digest,
            assetSizeBytes = assetSize,
            notes = json.optString("body").take(MAX_RELEASE_METADATA_BYTES),
            publishedAt = json.optString("published_at"),
            assetFileName = assetName,
            releasePageUrl = pageUrl,
        )
    }

    private suspend fun getText(
        url: String,
        accept: String = "application/vnd.github+json",
        maxBytes: Int = MAX_RELEASE_METADATA_BYTES,
    ): String {
        val response = transport.get(
            rawUrl = url,
            headers = mapOf(
                "Accept" to accept,
                "User-Agent" to "Elovaire/${BuildConfig.VERSION_NAME}",
                "X-GitHub-Api-Version" to "2022-11-28",
            ),
            maxBytes = maxBytes,
            urlPolicy = ::isTrustedUpdateUrl,
        )
        if (response.statusCode !in 200..299) error("GitHub update check failed")
        return response.body.toString(Charsets.UTF_8)
    }

    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases/latest"
    private const val RELEASES_URL = "https://api.github.com/repos/droidbeauty/elovaire-music/releases?per_page=20"
    private const val MAX_RELEASE_METADATA_BYTES = 1 * 1024 * 1024
}

private suspend fun <T> withTrustedConnection(
    rawUrl: String,
    accept: String,
    block: suspend (HttpURLConnection) -> T,
): T {
    TrafficStats.setThreadStatsTag(UPDATE_NETWORK_TAG)
    try {
        var current = rawUrl
        repeat(4) { attempt ->
            val url = URL(current)
            require(isTrustedUpdateUrl(url)) { "Update source is invalid" }
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = false
                setRequestProperty("Accept", accept)
                setRequestProperty("User-Agent", "Elovaire/${elovaire.music.droidbeauty.app.BuildConfig.VERSION_NAME}")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }
            try {
                connection.connect()
                if (connection.responseCode in 300..399) {
                    val location = connection.getHeaderField("Location") ?: error("Update source redirect is invalid")
                    current = URL(url, location).toString()
                    if (attempt == 3) error("Too many update redirects")
                } else {
                    return block(connection)
                }
            } finally {
                connection.disconnect()
            }
        }
        error("Too many update redirects")
    } finally {
        TrafficStats.clearThreadStatsTag()
    }
}

private fun isTrustedUpdateUrl(url: URL): Boolean {
    if (url.protocol != "https" || url.userInfo != null || (url.port != -1 && url.port != 443)) return false
    val host = url.host.lowercase(Locale.ROOT)
    return host in TRUSTED_UPDATE_HOSTS
}

private fun isTrustedDownloadUrl(raw: String): Boolean = runCatching { isTrustedUpdateUrl(URL(raw)) }.getOrDefault(false)
private fun isTrustedReleasePageUrl(raw: String): Boolean = runCatching {
    val url = URL(raw)
    url.protocol == "https" && url.host.equals("github.com", ignoreCase = true) && url.path.contains("/releases/")
}.getOrDefault(false)

private fun AppUpdateIntegrity.verifyDigestShape(value: String): Boolean = value.matches(Regex("(?i)[a-f0-9]{64}"))

private val TRUSTED_UPDATE_HOSTS = setOf(
    "api.github.com",
    "github.com",
    "objects.githubusercontent.com",
    "release-assets.githubusercontent.com",
    "github-releases.githubusercontent.com",
)

private const val UPDATE_NETWORK_TAG = 0x454C
