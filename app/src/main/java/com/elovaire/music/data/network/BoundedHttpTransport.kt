package elovaire.music.droidbeauty.app.data.network

import android.net.TrafficStats
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small, bounded transport for app-owned HTTPS GETs. Callers retain response semantics. */
internal class BoundedHttpTransport(
    private val connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
    private val readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS,
    private val maxRedirects: Int = DEFAULT_MAX_REDIRECTS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    init {
        require(connectTimeoutMs > 0) { "connectTimeoutMs must be positive" }
        require(readTimeoutMs > 0) { "readTimeoutMs must be positive" }
        require(maxRedirects in 0..MAX_REDIRECTS) { "maxRedirects is out of bounds" }
    }

    suspend fun get(
        rawUrl: String,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpResponse {
        val httpResource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveHttpRequest)
        return try {
            withContext(ioDispatcher) {
                getBlocking(rawUrl, headers, maxBytes, urlPolicy, cancellationContext = currentCoroutineContext())
            }
        } finally {
            httpResource.close()
        }
    }

    fun getBlocking(
        rawUrl: String,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpResponse {
        val httpResource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveHttpRequest)
        return try {
            getBlocking(rawUrl, headers, maxBytes, urlPolicy, cancellationContext = null)
        } finally {
            httpResource.close()
        }
    }

    fun getBlockingToFile(
        rawUrl: String,
        target: File,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpFileResponse {
        val httpResource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveHttpRequest)
        return withTrafficStatsTag {
            try {
                getBlockingToFileTagged(rawUrl, target, headers, maxBytes, urlPolicy)
            } catch (failure: IOException) {
                target.delete()
                throw failure
            } catch (failure: SecurityException) {
                target.delete()
                throw failure
            } catch (failure: IllegalArgumentException) {
                target.delete()
                throw failure
            } catch (failure: IllegalStateException) {
                target.delete()
                throw failure
            } finally {
                httpResource.close()
            }
        }
    }

    private fun getBlocking(
        rawUrl: String,
        headers: Map<String, String>,
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean,
        cancellationContext: kotlin.coroutines.CoroutineContext?,
    ): BoundedHttpResponse {
        return withTrafficStatsTag {
            getBlockingTagged(rawUrl, headers, maxBytes, urlPolicy, cancellationContext)
        }
    }

    private fun getBlockingTagged(
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

    private fun getBlockingToFileTagged(
        rawUrl: String,
        target: File,
        headers: Map<String, String>,
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean,
    ): BoundedHttpFileResponse {
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
                if (status in 200..299) {
                    connection.inputStream.use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyBoundedTo(output, maxBytes)
                            output.flush()
                        }
                    }
                }
                return BoundedHttpFileResponse(
                    statusCode = status,
                    bytesWritten = if (status in 200..299) target.length() else 0L,
                    retryAfterMs = connection.getHeaderField("Retry-After")?.toRetryAfterMs(),
                    finalUrl = currentUrl,
                )
            } finally {
                connection.disconnect()
            }
        }
        error("Unable to resolve HTTPS request")
    }

    suspend fun post(
        rawUrl: String,
        body: ByteArray,
        headers: Map<String, String> = emptyMap(),
        maxBytes: Int,
        urlPolicy: (URL) -> Boolean = ::isHttpsUrl,
    ): BoundedHttpResponse = withContext(ioDispatcher) {
        val cancellationContext = currentCoroutineContext()
        withTrafficStatsTag {
            val httpResource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveHttpRequest)
            try {
                require(maxBytes > 0) { "maxBytes must be positive" }
                val url = URL(rawUrl)
                require(urlPolicy(url)) { "HTTP request URL is not allowed" }
                val connection = (url.openConnection() as? HttpURLConnection)
                    ?: error("Unsupported HTTP connection")
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = connectTimeoutMs
                    connection.readTimeout = readTimeoutMs
                    connection.instanceFollowRedirects = false
                    connection.doOutput = true
                    headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
                    connection.connect()
                    connection.outputStream.use { output -> output.write(body) }
                    val status = connection.responseCode
                    val contentLength = connection.contentLengthLong
                    if (contentLength > maxBytes) error("HTTP response is too large")
                    val responseBody = if (status in 200..299) {
                        connection.inputStream.use { it.readBounded(maxBytes, cancellationContext) }
                    } else {
                        ByteArray(0)
                    }
                    BoundedHttpResponse(
                        statusCode = status,
                        body = responseBody,
                        retryAfterMs = connection.getHeaderField("Retry-After")?.toRetryAfterMs(),
                        finalUrl = url,
                    )
                } finally {
                    connection.disconnect()
                }
            } finally {
                httpResource.close()
            }
        }
    }

    private inline fun <T> withTrafficStatsTag(block: () -> T): T {
        val previousTag = TrafficStats.getThreadStatsTag()
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG)
        return try {
            block()
        } finally {
            TrafficStats.setThreadStatsTag(previousTag)
        }
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
            if (count == 0) {
                val singleByte = read()
                if (singleByte < 0) return output.toByteArray()
                if (output.size() >= maxBytes) error("HTTP response is too large")
                output.write(singleByte)
                continue
            }
            if (output.size() > maxBytes - count) error("HTTP response is too large")
            output.write(buffer, 0, count)
        }
    }

    private fun java.io.InputStream.copyBoundedTo(
        output: FileOutputStream,
        maxBytes: Int,
    ) {
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val count = read(buffer)
            if (count < 0) return
            if (count == 0) {
                val singleByte = read()
                if (singleByte < 0) return
                if (total >= maxBytes) error("HTTP response is too large")
                output.write(singleByte)
                total += 1
                continue
            }
            if (total > maxBytes - count) error("HTTP response is too large")
            output.write(buffer, 0, count)
            total += count
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
        const val MAX_REDIRECTS = 8
        const val MAX_RETRY_AFTER_SECONDS = 24L * 60L * 60L
        const val TRAFFIC_STATS_TAG = 0x454C4F56
    }
}

private fun isHttpsUrl(url: URL): Boolean = url.protocol.equals("https", ignoreCase = true)

internal data class BoundedHttpResponse(
    val statusCode: Int,
    val body: ByteArray,
    val retryAfterMs: Long?,
    val finalUrl: URL,
)

internal data class BoundedHttpFileResponse(
    val statusCode: Int,
    val bytesWritten: Long,
    val retryAfterMs: Long?,
    val finalUrl: URL,
)
