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

private const val DEFAULT_HTTP_TIMEOUT_MS = 8_000
