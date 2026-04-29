package elovaire.music.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import elovaire.music.app.BuildConfig
import elovaire.music.app.data.settings.PreferenceStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AppReleaseInfo(
    val versionName: String,
    val tagName: String,
    val downloadUrl: String,
    val notes: String,
    val publishedAt: String,
    val assetFileName: String,
)

data class AppUpdateUiState(
    val availableRelease: AppReleaseInfo? = null,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val downloadProgress: Float? = null,
    val errorMessage: String? = null,
)

class AppUpdateManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
) {
    private val appContext = context.applicationContext
    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()
    private var downloadJob: Job? = null

    init {
        clearDownloadedInstallers()
        checkForUpdates()
    }

    fun checkForUpdates(force: Boolean = false) {
        if (_uiState.value.isChecking || _uiState.value.isDownloading || _uiState.value.isInstalling) return
        scope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null) }
            val latestRelease = runCatching { withContext(Dispatchers.IO) { fetchLatestRelease() } }.getOrNull()
            val dismissedVersion = preferenceStore.dismissedUpdateVersion.value
            val shouldShow = latestRelease != null &&
                isVersionNewer(latestRelease.versionName, BuildConfig.VERSION_NAME) &&
                (force || dismissedVersion != latestRelease.versionName)

            _uiState.update { current ->
                current.copy(
                    availableRelease = latestRelease.takeIf { shouldShow },
                    isChecking = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun dismissAvailableUpdate() {
        val version = _uiState.value.availableRelease?.versionName ?: return
        preferenceStore.setDismissedUpdateVersion(version)
        _uiState.update { it.copy(availableRelease = null, errorMessage = null) }
    }

    fun startUpdate() {
        val release = _uiState.value.availableRelease ?: return
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            _uiState.update {
                it.copy(
                    isDownloading = true,
                    isInstalling = false,
                    downloadProgress = 0f,
                    errorMessage = null,
                )
            }
            val apkFile = runCatching {
                withContext(Dispatchers.IO) {
                    downloadReleaseApk(release)
                }
            }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        isInstalling = false,
                        downloadProgress = null,
                        errorMessage = throwable.message ?: "Unable to download update",
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = true,
                    downloadProgress = 1f,
                    errorMessage = null,
                )
            }
            launchPackageInstaller(apkFile)
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isInstalling = false,
                    downloadProgress = null,
                    errorMessage = null,
                )
            }
        }
    }

    fun clearInstallState() {
        _uiState.update {
            it.copy(
                isDownloading = false,
                isInstalling = false,
                downloadProgress = null,
                errorMessage = null,
            )
        }
    }

    fun clearDownloadedInstallers() {
        runCatching {
            updatesDirectory().listFiles()?.forEach { file ->
                if (file.isFile && file.extension.equals("apk", ignoreCase = true)) {
                    file.delete()
                }
            }
        }
    }

    private fun fetchLatestRelease(): AppReleaseInfo? {
        val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            instanceFollowRedirects = true
        }

        return connection.useJsonObject { json ->
            if (json.optBoolean("draft") || json.optBoolean("prerelease")) return@useJsonObject null
            val tagName = json.optString("tag_name").orEmpty()
            val versionName = normalizeVersionLabel(tagName.ifBlank { json.optString("name").orEmpty() })
            if (versionName.isBlank()) return@useJsonObject null
            val assets = json.optJSONArray("assets") ?: return@useJsonObject null
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
                ?: return@useJsonObject null

            AppReleaseInfo(
                versionName = versionName,
                tagName = tagName,
                downloadUrl = asset.optString("browser_download_url").orEmpty(),
                notes = json.optString("body").orEmpty(),
                publishedAt = json.optString("published_at").orEmpty(),
                assetFileName = asset.optString("name").orEmpty().ifBlank { "elovaire-update.apk" },
            ).takeIf { it.downloadUrl.isNotBlank() }
        }
    }

    private fun downloadReleaseApk(release: AppReleaseInfo): File {
        val updatesDir = updatesDirectory().apply { mkdirs() }
        val targetFile = File(updatesDir, release.assetFileName)
        val connection = (URL(release.downloadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("User-Agent", "Elovaire/${BuildConfig.VERSION_NAME}")
            instanceFollowRedirects = true
        }
        connection.connect()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Update download failed")
        }
        val totalBytes = connection.contentLengthLong.takeIf { it > 0L }
        connection.inputStream.use { input ->
            targetFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesCopied = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    bytesCopied += read
                    val progress = totalBytes?.let { bytesCopied.toFloat() / it.toFloat() }
                    _uiState.update { state ->
                        state.copy(downloadProgress = progress?.coerceIn(0f, 1f))
                    }
                }
            }
        }
        return targetFile
    }

    private fun launchPackageInstaller(apkFile: File) {
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

    private fun isVersionNewer(candidate: String, installed: String): Boolean {
        val candidateParts = candidate.normalizeVersionParts()
        val installedParts = installed.normalizeVersionParts()
        val maxSize = maxOf(candidateParts.size, installedParts.size)
        for (index in 0 until maxSize) {
            val candidatePart = candidateParts.getOrElse(index) { 0 }
            val installedPart = installedParts.getOrElse(index) { 0 }
            if (candidatePart != installedPart) {
                return candidatePart > installedPart
            }
        }
        return false
    }

    private fun String.normalizeVersionParts(): List<Int> {
        return trim()
            .removePrefix("v")
            .removePrefix("V")
            .split('.', '-', '_')
            .mapNotNull { it.toIntOrNull() }
    }

    private inline fun <T> HttpURLConnection.useJsonObject(block: (JSONObject) -> T): T {
        return try {
            connect()
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
        const val NETWORK_TIMEOUT_MS = 12_000
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
