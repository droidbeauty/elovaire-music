package elovaire.music.droidbeauty.app.data.network

import java.net.HttpURLConnection
import java.net.URL

internal data class HttpRequest(
    val url: String,
    val accept: String,
    val headers: Map<String, String> = emptyMap(),
    val connectTimeoutMs: Int = DEFAULT_HTTP_TIMEOUT_MS,
    val readTimeoutMs: Int = DEFAULT_HTTP_TIMEOUT_MS,
    val allowedRedirectHostSuffixes: Set<String> = emptySet(),
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
    val statusCode: Int? = null,
) : java.io.IOException(message, cause)

internal object HttpTransport {
    fun getText(request: HttpRequest, maxBytes: Int): String {
        validateHttpRequest(request, maxBytes)
        return execute(request) { connection ->
            connection.inputStream.use { input ->
                try {
                    input.readUtf8Bounded(maxBytes, connection.contentLengthLong)
                } catch (failure: BoundedResponseException) {
                    throw failure.toTransportFailure()
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
                } catch (failure: BoundedResponseException) {
                    throw failure.toTransportFailure()
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
            connection.inputStream.use { input ->
                try {
                    input.readUtf8Bounded(maxBytes, connection.contentLengthLong)
                } catch (failure: BoundedResponseException) {
                    throw failure.toTransportFailure()
                }
            }
        }
    }

    private inline fun <T> execute(
        request: HttpRequest,
        method: String = "GET",
        block: (HttpURLConnection) -> T,
    ): T {
        var currentUrl = runCatching { URL(request.url) }.getOrElse { failure ->
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network address is invalid.", failure)
        }
        if (currentUrl.protocol != "https") {
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "Only HTTPS requests are supported.")
        }
        var redirectCount = 0
        while (true) {
            val connection = (currentUrl.openConnection() as? HttpURLConnection)
                ?: throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network address is unsupported.")
            try {
                connection.requestMethod = method
                connection.connectTimeout = request.connectTimeoutMs
                connection.readTimeout = request.readTimeoutMs
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", request.accept)
                request.headers.forEach(connection::setRequestProperty)
                if (method == "GET") {
                    val status = connection.responseCode
                    ensureHttps(connection)
                    if (status in HTTP_REDIRECT_STATUS_CODES) {
                        if (redirectCount >= MAX_HTTP_REDIRECTS) {
                            throw HttpTransportException(HttpFailureKind.InvalidUrl, "Too many network redirects.")
                        }
                        currentUrl = resolveSafeHttpRedirect(
                            currentUrl,
                            connection.getHeaderField("Location"),
                            request.allowedRedirectHostSuffixes,
                        )
                        redirectCount += 1
                        continue
                    }
                    if (status !in 200..299) throw httpStatusFailure(status)
                }
                return block(connection)
            } catch (failure: HttpTransportException) {
                throw failure
            } catch (failure: java.io.IOException) {
                throw HttpTransportException(HttpFailureKind.Transport, "The network request failed.", failure)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun httpStatusFailure(status: Int): HttpTransportException {
        return HttpTransportException(
            kind = HttpFailureKind.HttpStatus,
            message = "The server returned HTTP $status.",
            statusCode = status,
        )
    }

    private fun ensureHttps(connection: HttpURLConnection) {
        if (connection.url.protocol != "https") {
            throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network request left HTTPS.")
        }
    }
}

private fun BoundedResponseException.toTransportFailure(): HttpTransportException {
    val failureKind = if (kind == BoundedReadFailure.TooLarge) {
        HttpFailureKind.ResponseTooLarge
    } else {
        HttpFailureKind.Transport
    }
    return HttpTransportException(failureKind, message.orEmpty(), this)
}

internal fun resolveSafeHttpRedirect(
    currentUrl: URL,
    location: String?,
    allowedHostSuffixes: Set<String> = emptySet(),
): URL {
    val target = location
        ?.takeIf { it.length <= MAX_HTTP_URL_CHARACTERS }
        ?.let { value -> runCatching { URL(currentUrl, value) }.getOrNull() }
        ?: throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network redirect is invalid.")
    val sameOrigin = target.protocol == "https" &&
        target.host.equals(currentUrl.host, ignoreCase = true) &&
        target.effectivePort() == currentUrl.effectivePort()
    val allowedHttpsHost = target.protocol == "https" && allowedHostSuffixes.any { suffix ->
        target.host.equals(suffix, ignoreCase = true) ||
            target.host.endsWith(".$suffix", ignoreCase = true)
    }
    if (!sameOrigin && !allowedHttpsHost) {
        throw HttpTransportException(HttpFailureKind.InvalidUrl, "The network redirect changed origin.")
    }
    return target
}

private fun URL.effectivePort(): Int = if (port >= 0) port else defaultPort

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
private const val MAX_HTTP_REDIRECTS = 3
private val HTTP_REDIRECT_STATUS_CODES = setOf(301, 302, 303, 307, 308)
