package elovaire.music.droidbeauty.app.data.library.network

import java.security.MessageDigest

internal enum class NetworkLibraryProtocol {
    Smb,
    WebDav,
}

internal data class NetworkLibrarySource(
    val id: String,
    val name: String,
    val protocol: NetworkLibraryProtocol,
    val server: String,
    val shareOrPath: String,
    val username: String,
    val credentialKey: String,
    val enabled: Boolean = true,
)

internal data class NetworkCredentials(
    val username: String,
    val password: String,
    val domain: String? = null,
)

internal data class NetworkFileEntry(
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long? = null,
    val modifiedAtMs: Long? = null,
    val contentType: String? = null,
    val etag: String? = null,
    val sourceEntryId: String? = null,
)

internal data class NetworkReadHandle(
    val input: java.io.InputStream,
    val length: Long?,
    val closeHandle: () -> Unit,
) : java.io.Closeable {
    override fun close() {
        runCatching { input.close() }
        closeHandle()
    }
}

internal enum class NetworkAvailability {
    Checking,
    Available,
    Offline,
    AuthenticationRequired,
    Misconfigured,
    Unavailable,
}

internal data class NetworkProbeResult(
    val availability: NetworkAvailability,
    val message: String? = null,
)

internal object NetworkSourceIdentity {
    fun stableKey(source: NetworkLibrarySource): String {
        return "network:${source.id.trim()}"
    }

    fun songId(sourceId: String, path: String, sourceEntryId: String? = null): Long {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(
                (sourceEntryId?.takeIf(String::isNotBlank)?.let { "$sourceId|entry:$it" }
                    ?: "$sourceId|${NetworkPathPolicy.normalizeRelativePath(path)}")
                    .toByteArray(),
            )
        var value = 0L
        repeat(8) { index ->
            value = (value shl 8) or (digest[index].toLong() and 0xffL)
        }
        return -(value and Long.MAX_VALUE).coerceAtLeast(1L)
    }
}

internal object NetworkResourceUri {
    private const val SCHEME = "elovaire-network"

    fun create(sourceId: String, path: String): android.net.Uri = android.net.Uri.Builder()
        .scheme(SCHEME)
        .authority(sourceId)
        .appendQueryParameter("path", NetworkPathPolicy.normalizeRelativePath(path))
        .build()

    fun isNetworkUri(uri: android.net.Uri): Boolean = uri.scheme.equals(SCHEME, ignoreCase = true)

    fun sourceId(uri: android.net.Uri): String? = uri.host?.takeIf(String::isNotBlank)

    fun path(uri: android.net.Uri): String? = uri.getQueryParameter("path")
        ?.let(NetworkPathPolicy::normalizeRelativePath)
        ?.takeIf(String::isNotBlank)
}
