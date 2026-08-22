package elovaire.music.droidbeauty.app.data.network

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small, bounded transport for app-owned HTTPS GETs. Callers retain response semantics. */
internal class BoundedHttpTransport(
    private val connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
    private val readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS,
    private val maxRedirects: Int = DEFAULT_MAX_REDIRECTS,
) {
    suspend fun get(
        rawUrl: String,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpResponse = withContext(Dispatchers.IO) {
        getBlocking(rawUrl, headers, maxBytes, urlPolicy, cancellationContext = currentCoroutineContext())
    }

    fun getBlocking(
        rawUrl: String,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpResponse {
        return getBlocking(rawUrl, headers, maxBytes, urlPolicy, cancellationContext = null)
    }

    private fun getBlocking(
        rawUrl: String,
        headers: Map<String, String>,
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean,
        cancellationContext: kotlin.coroutines.CoroutineContext?,
    ): BoundedHttpResponse {
        require(maxBytes > 0) { "maxBytes must be positive" }
        var currentUrl = URL(rawUrl)
        repeat(maxRedirects + 1) { redirectAttempt ->
            require(urlPolicy(currentUrl)) { "HTTP request URL is not allowed" }
            val connection = (currentUrl.openConnection() as? HttpURLConnection)
                ?: error("Unsupported HTTP connection")
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = connectTimeoutMs
                connection.readTimeout = readTimeoutMs
                connection.instanceFollowRedirects = false
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
                connection.connect()
                val status = connection.responseCode
                if (status in 300..399) {
                    if (redirectAttempt == maxRedirects) error("Too many HTTPS redirects")
                    val location = connection.getHeaderField("Location") ?: error("Redirect has no location")
                    currentUrl = URL(currentUrl, location)
                    return@repeat
                }
                val contentLength = connection.contentLengthLong
                if (contentLength > maxBytes) error("HTTP response is too large")
                val body = if (status in 200..299) {
                    connection.inputStream.use { it.readBounded(maxBytes, cancellationContext) }
                } else {
                    ByteArray(0)
                }
                return BoundedHttpResponse(
                    statusCode = status,
                    body = body,
                    retryAfterMs = connection.getHeaderField("Retry-After")?.toRetryAfterMs(),
                    finalUrl = currentUrl,
                )
            } finally {
                connection.disconnect()
            }
        }
        error("Unable to resolve HTTPS request")
    }

    private fun java.io.InputStream.readBounded(
        maxBytes: Int,
        cancellationContext: kotlin.coroutines.CoroutineContext?,
    ): ByteArray {
        val output = ByteArrayOutputStream(minOf(maxBytes, 32 * 1024))
        val buffer = ByteArray(8 * 1024)
        while (true) {
            cancellationContext?.ensureActive()
            val count = read(buffer)
            if (count < 0) return output.toByteArray()
            if (output.size() > maxBytes - count) error("HTTP response is too large")
            output.write(buffer, 0, count)
        }
    }

    private fun String.toRetryAfterMs(): Long? {
        val seconds = trim().toLongOrNull() ?: return null
        return seconds.coerceIn(0L, MAX_RETRY_AFTER_SECONDS) * 1_000L
    }

    private companion object {
        const val DEFAULT_CONNECT_TIMEOUT_MS = 8_000
        const val DEFAULT_READ_TIMEOUT_MS = 10_000
        const val DEFAULT_MAX_REDIRECTS = 3
        const val MAX_RETRY_AFTER_SECONDS = 24L * 60L * 60L
    }
}

private fun isHttpsUrl(url: URL): Boolean = url.protocol.equals("https", ignoreCase = true)

internal data class BoundedHttpResponse(
    val statusCode: Int,
    val body: ByteArray,
    val retryAfterMs: Long?,
    val finalUrl: URL,
)
