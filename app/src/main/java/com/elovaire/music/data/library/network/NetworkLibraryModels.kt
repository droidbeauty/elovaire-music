package elovaire.music.droidbeauty.app.data.library.network

import java.security.MessageDigest
import java.net.URI
import java.util.Locale

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
    LocalNetworkPermissionRequired,
    Offline,
    AuthenticationRequired,
    Misconfigured,
    Unavailable,
}

internal class NetworkLocalNetworkPermissionException : java.io.IOException(
    "Local network permission is required for network library access",
)

internal class NetworkRangeException(message: String) : java.io.IOException(message)

internal class WebDavHttpException(val statusCode: Int) : java.io.IOException(
    "WebDAV request failed with HTTP $statusCode",
)

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

    fun locationFingerprint(source: NetworkLibrarySource): String {
        val server = when (source.protocol) {
            NetworkLibraryProtocol.Smb ->
                "${NetworkPathPolicy.smbServer(source.server)?.lowercase(Locale.ROOT)}:${NetworkPathPolicy.smbPort(source.server)}"
            NetworkLibraryProtocol.WebDav -> runCatching { URI(source.server.trim()) }
                .getOrNull()
                ?.let { uri ->
                    val port = uri.port.takeIf { it >= 0 } ?: 443
                    "${uri.host.orEmpty().lowercase(Locale.ROOT)}:$port${uri.rawPath.orEmpty()}"
                }
                .orEmpty()
        }
        val canonical = "${source.protocol.name}|$server|${NetworkPathPolicy.normalizeRelativePath(source.shareOrPath)}"
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }
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
        ?.let(NetworkPathPolicy::validateRelativePath)
        ?.takeIf(String::isNotBlank)
}
