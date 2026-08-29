package elovaire.music.droidbeauty.app.data.library.network

import java.security.MessageDigest
import java.net.URI
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

internal enum class NetworkReadPurpose {
    Playback,
    Metadata,
    Artwork,
    Listing,
}

internal enum class NetworkRangeCapability {
    Unknown,
    Supported,
    Unsupported,
}

internal enum class RemoteIoFailureKind {
    Authentication,
    Permission,
    HostUnreachable,
    Timeout,
    ConnectionReset,
    SourceRemoved,
    ShareMissing,
    PathMissing,
    RangeOutOfBounds,
    RangeUnsupported,
    SessionInvalid,
    Protocol,
    Interrupted,
    TransientServer,
    Unknown,
}

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

internal class NetworkReadHandle(
    val input: java.io.InputStream,
    val length: Long?,
    val closeHandle: () -> Unit,
) : java.io.Closeable {
    private val closed = AtomicBoolean(false)

    @Suppress("TooGenericExceptionCaught")
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        var failure: Throwable? = null
        try {
            input.close()
        } catch (closeFailure: Throwable) {
            failure = closeFailure
        }
        try {
            closeHandle()
        } catch (closeFailure: Throwable) {
            if (failure == null) failure = closeFailure
        }
        failure?.let { throw it }
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

internal open class NetworkRemoteIoException(
    val kind: RemoteIoFailureKind,
    message: String,
    cause: Throwable? = null,
) : java.io.IOException(message, cause)

internal class NetworkLocalNetworkPermissionException : NetworkRemoteIoException(
    kind = RemoteIoFailureKind.Permission,
    message = "Local network permission is required for network library access",
)

internal class NetworkRangeException(message: String) : NetworkRemoteIoException(
    kind = RemoteIoFailureKind.RangeOutOfBounds,
    message = message,
)

internal class NetworkRangeUnsupportedException(message: String) : NetworkRemoteIoException(
    kind = RemoteIoFailureKind.RangeUnsupported,
    message = message,
)

internal class WebDavHttpException(val statusCode: Int) : NetworkRemoteIoException(
    kind = when (statusCode) {
        401 -> RemoteIoFailureKind.Authentication
        403 -> RemoteIoFailureKind.Permission
        404 -> RemoteIoFailureKind.PathMissing
        408 -> RemoteIoFailureKind.Timeout
        429, in 500..599 -> RemoteIoFailureKind.TransientServer
        else -> RemoteIoFailureKind.Protocol
    },
    message = "WebDAV request failed with HTTP $statusCode",
)

internal class NetworkRedirectException(message: String) : NetworkRemoteIoException(
    kind = RemoteIoFailureKind.Protocol,
    message = message,
)

internal fun Throwable.remoteIoFailureKind(): RemoteIoFailureKind = when (this) {
    is NetworkRemoteIoException -> kind
    is java.net.SocketTimeoutException -> RemoteIoFailureKind.Timeout
    is java.net.UnknownHostException,
    is java.net.ConnectException,
    -> RemoteIoFailureKind.HostUnreachable
    is java.io.InterruptedIOException -> RemoteIoFailureKind.Interrupted
    is java.net.SocketException -> RemoteIoFailureKind.ConnectionReset
    else -> RemoteIoFailureKind.Unknown
}

internal fun RemoteIoFailureKind.toNetworkAvailability(): NetworkAvailability = when (this) {
    RemoteIoFailureKind.Authentication -> NetworkAvailability.AuthenticationRequired
    RemoteIoFailureKind.HostUnreachable,
    RemoteIoFailureKind.Timeout,
    RemoteIoFailureKind.ConnectionReset,
    RemoteIoFailureKind.TransientServer,
    -> NetworkAvailability.Offline
    RemoteIoFailureKind.Permission -> NetworkAvailability.Unavailable
    RemoteIoFailureKind.SourceRemoved -> NetworkAvailability.Unavailable
    RemoteIoFailureKind.ShareMissing,
    RemoteIoFailureKind.PathMissing,
    RemoteIoFailureKind.RangeOutOfBounds,
    RemoteIoFailureKind.RangeUnsupported,
    RemoteIoFailureKind.SessionInvalid,
    RemoteIoFailureKind.Protocol,
    RemoteIoFailureKind.Interrupted,
    RemoteIoFailureKind.Unknown,
    -> NetworkAvailability.Unavailable
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
