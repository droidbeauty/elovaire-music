package elovaire.music.droidbeauty.app.data.network

import java.net.HttpURLConnection
import java.net.URL

internal data class HttpRequest(
    val url: String,
    val accept: String,
    val headers: Map<String, String> = emptyMap(),
    val connectTimeoutMs: Int = DEFAULT_HTTP_TIMEOUT_MS,
    val readTimeoutMs: Int = DEFAULT_HTTP_TIMEOUT_MS,
)

internal enum class HttpFailureKind {
    InvalidUrl,
    HttpStatus,
    RequestTooLarge,
    ResponseTooLarge,
    Transport,
}

internal class HttpTransportException(
    val kind: HttpFailureKind,
    message: String,
    cause: Throwable? = null,
) : java.io.IOException(message, cause)

internal class HttpTransport {
    fun getText(request: HttpRequest, maxBytes: Int): String {
        validateHttpRequest(request, maxBytes)
        return execute(request) { connection ->
            connection.inputStream.use { input ->
                try {
                    input.readUtf8Bounded(maxBytes, connection.contentLengthLong)
                } catch (failure: java.io.IOException) {
                    throw HttpTransportException(HttpFailureKind.ResponseTooLarge, failure.message.orEmpty(), failure)
                }
            }
        }
    }

    fun getBytes(request: HttpRequest, maxBytes: Int): ByteArray {
        validateHttpRequest(request, maxBytes)
        return execute(request) { connection ->
            connection.inputStream.use { input ->
                try {
                    input.readBytesBounded(maxBytes, connection.contentLengthLong)
                } catch (failure: java.io.IOException) {
                    throw HttpTransportException(HttpFailureKind.ResponseTooLarge, failure.message.orEmpty(), failure)
                }
            }
        }
    }

    fun postForm(request: HttpRequest, body: ByteArray, maxBytes: Int): String {
        validateHttpRequest(request, maxBytes)
        if (body.size > MAX_HTTP_REQUEST_BODY_BYTES) {
            throw HttpTransportException(HttpFailureKind.RequestTooLarge, "The network request is too large.")
        }
        return execute(request, method = "POST") { connection ->
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setFixedLengthStreamingMode(body.size)
            connection.outputStream.use { output -> output.write(body) }
            val status = connection.responseCode
            ensureHttps(connection)
            if (status !in 200..299) throw httpStatusFailure(status)
            connection.inputStream.use { input -> input.readUtf8Bounded(maxBytes, connection.contentLengthLong) }
        }
    }

    private inline fun <T> execute(
        request: HttpRequest,
        method: String = "GET",
        block: (HttpURLConnection) -> T,
    ): T {
        val parsed = runCatching { URL(request.url) }.getOrElse { failure ->
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network address is invalid.", failure)
        }
        if (parsed.protocol != "https") {
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "Only HTTPS requests are supported.")
        }
        val connection = (parsed.openConnection() as? HttpURLConnection)
            ?: throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network address is unsupported.")
        return try {
            connection.requestMethod = method
            connection.connectTimeout = request.connectTimeoutMs
            connection.readTimeout = request.readTimeoutMs
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", request.accept)
            request.headers.forEach(connection::setRequestProperty)
            if (method == "GET") {
                val status = connection.responseCode
                ensureHttps(connection)
                if (status !in 200..299) throw httpStatusFailure(status)
            }
            block(connection)
        } catch (failure: HttpTransportException) {
            throw failure
        } catch (failure: java.io.IOException) {
            throw HttpTransportException(HttpFailureKind.Transport, "The network request failed.", failure)
        } finally {
            connection.disconnect()
        }
    }

    private fun httpStatusFailure(status: Int): HttpTransportException {
        return HttpTransportException(HttpFailureKind.HttpStatus, "The server returned HTTP $status.")
    }

    private fun ensureHttps(connection: HttpURLConnection) {
        if (connection.url.protocol != "https") {
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network request left HTTPS.")
        }
    }
}

internal fun validateHttpRequest(request: HttpRequest, maxBytes: Int) {
    val parsed = runCatching { URL(request.url) }.getOrElse { failure ->
        throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network address is invalid.", failure)
    }
    if (request.url.length > MAX_HTTP_URL_CHARACTERS || parsed.protocol != "https" || parsed.host.isBlank()) {
        throw HttpTransportException(HttpFailureKind.InvalidUrl, "Only valid HTTPS requests are supported.")
    }
    if (request.connectTimeoutMs !in 1..MAX_HTTP_TIMEOUT_MS || request.readTimeoutMs !in 1..MAX_HTTP_TIMEOUT_MS) {
        throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network timeout is invalid.")
    }
    if (maxBytes !in 0..MAX_HTTP_RESPONSE_BYTES) {
        throw HttpTransportException(HttpFailureKind.ResponseTooLarge, "The response limit is invalid.")
    }
}

private const val DEFAULT_HTTP_TIMEOUT_MS = 8_000
private const val MAX_HTTP_TIMEOUT_MS = 120_000
private const val MAX_HTTP_URL_CHARACTERS = 4_096
private const val MAX_HTTP_RESPONSE_BYTES = 64 * 1024 * 1024
private const val MAX_HTTP_REQUEST_BODY_BYTES = 1 * 1024 * 1024
