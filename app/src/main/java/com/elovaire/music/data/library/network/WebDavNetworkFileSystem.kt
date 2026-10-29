package elovaire.music.droidbeauty.app.data.library.network

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import org.w3c.dom.Element

internal class WebDavNetworkFileSystem(
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
    private val propFindClient: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .writeTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .build(),
) : NetworkFileSystem {
    private val rangeCapabilities = ConcurrentHashMap<String, NetworkRangeCapability>()
    private val activeConnections = ConcurrentHashMap.newKeySet<HttpURLConnection>()
    private val activePropFindCalls = ConcurrentHashMap.newKeySet<Call>()
    private val released = AtomicBoolean(false)

    override fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult {
        checkNotReleased()
        return networkProbeResult(
            attempt = {
                val root = NetworkPathPolicy.validateRelativePath(source.shareOrPath)
                    ?: throw IOException("Invalid WebDAV root path")
                requireWebDavDirectory(propFind(source, credentials, root, depth = 0), root)
                NetworkProbeResult(NetworkAvailability.Available)
            },
            onFailure = { failure ->
                NetworkProbeResult(classifyFailure(failure), failure::class.simpleName)
            },
        )
    }

    override fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int,
        maxDepth: Int,
    ): NetworkListingResult {
        checkNotReleased()
        require(maxEntries > 0)
        val results = ArrayList<NetworkFileEntry>()
        val pending = ArrayDeque<Pair<String, Int>>()
        val root = NetworkPathPolicy.validateRelativePath(source.shareOrPath)
            ?: throw IOException("Invalid WebDAV root path")
        pending.addLast(root to 0)
        val visited = hashSetOf<String>()
        var directoryCount = 0
        var incompleteReason: String? = null
        while (pending.isNotEmpty() && results.size < maxEntries) {
            val (directory, depth) = pending.removeFirst()
            val normalized = NetworkPathPolicy.normalizeRelativePath(directory)
            if (!visited.add(normalized)) continue
            if (++directoryCount > MAX_DIRECTORY_COUNT) {
                incompleteReason = "directory-budget"
                break
            }
            val directoryEntries = propFind(source, credentials, normalized, depth = 1)
            requireWebDavDirectory(directoryEntries, normalized)
            directoryEntries.forEach { entry ->
                if (entry.path == normalized) return@forEach
                if (results.size >= maxEntries) {
                    incompleteReason = "entry-budget"
                    return@forEach
                }
                results += entry
                if (entry.isDirectory) {
                    if (depth < maxDepth) {
                        pending.addLast(entry.path to depth + 1)
                    } else {
                        incompleteReason = incompleteReason ?: "depth-budget"
                    }
                }
            }
        }
        if (results.size >= maxEntries) incompleteReason = incompleteReason ?: "entry-budget"
        return incompleteReason?.let { NetworkListingResult.Incomplete(results, it) }
            ?: NetworkListingResult.Complete(results)
    }

    override fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        checkNotReleased()
        require(position >= 0L)
        require(length == -1L || length >= 0L)
        if (length == 0L) return NetworkReadHandle.empty()
        val requestedEnd = if (length > 0L) {
            position.checkedRangeEnd(length)
        } else {
            null
        }
        val requestsRange = position > 0L || length > 0L
        if (position > 0L && rangeCapabilities[source.id] == NetworkRangeCapability.Unsupported) {
            throw NetworkRangeUnsupportedException("WebDAV server does not support byte ranges")
        }
        val connection = executeGet(
            source = source,
            credentials = credentials,
            path = path,
            range = if (requestsRange) "bytes=$position-${requestedEnd ?: ""}" else null,
        )
        if (released.get()) {
            disconnect(connection)
            throw IllegalStateException("WebDAV file system is released")
        }
        val status = connection.responseCode
        if (status == HTTP_RANGE_NOT_SATISFIABLE) {
            val totalLength = parseUnsatisfiedContentRange(connection.getHeaderField("Content-Range"))
            disconnect(connection)
            if (totalLength != null && position == totalLength) {
                rangeCapabilities[source.id] = NetworkRangeCapability.Supported
                return NetworkReadHandle(
                    input = ByteArrayInputStream(ByteArray(0)),
                    length = 0L,
                    closeHandle = {},
                )
            }
            if (totalLength != null && position > totalLength) {
                throw NetworkRangeException("WebDAV read position is outside the file")
            }
            throw IOException("WebDAV media range is not satisfiable")
        }
        if (status !in 200..299) {
            disconnect(connection)
            throw WebDavHttpException(status)
        }
        val responseLength = connection.contentLengthLong.takeIf { it >= 0L }
        val input: InputStream
        val handleLength: Long?
        when (status) {
            HttpURLConnection.HTTP_PARTIAL -> {
                val contentRange = parseWebDavContentRange(connection.getHeaderField("Content-Range"))
                val rangeLength = contentRange?.length
                val valid = contentRange != null &&
                    contentRange.start == position &&
                    rangeLength != null &&
                    (requestedEnd == null || contentRange.end <= requestedEnd) &&
                    (responseLength == null || responseLength == rangeLength) &&
                    (contentRange.totalLength == null || contentRange.end < contentRange.totalLength)
                if (!valid) {
                    disconnect(connection)
                    throw IOException("WebDAV server returned an invalid Content-Range")
                }
                rangeCapabilities[source.id] = NetworkRangeCapability.Supported
                input = connection.openInputStreamOrDisconnect()
                handleLength = if (length >= 0L) minOf(length, rangeLength) else rangeLength
            }
            HttpURLConnection.HTTP_OK -> {
                if (position > 0L) {
                    disconnect(connection)
                    rangeCapabilities[source.id] = NetworkRangeCapability.Unsupported
                    throw NetworkRangeUnsupportedException("WebDAV server did not honor the requested byte range")
                }
                if (requestsRange) rangeCapabilities[source.id] = NetworkRangeCapability.Unsupported
                val boundedLength = if (length >= 0L) {
                    minOf(length, responseLength ?: length)
                } else {
                    responseLength
                }
                input = connection.openInputStreamOrDisconnect().let { stream ->
                    if (boundedLength != null) {
                        limitWebDavRead(stream, boundedLength)
                    } else {
                        stream
                    }
                }
                handleLength = boundedLength
            }
            else -> {
                disconnect(connection)
                throw IOException("WebDAV media request returned unsupported HTTP $status")
            }
        }
        val requestResource = resourceTracker.acquire(BackendResourceKind.ActiveWebDavRequest)
        return NetworkReadHandle(
            input = input,
            length = handleLength,
            closeHandle = {
                try {
                    disconnect(connection)
                } finally {
                    requestResource.close()
                }
            },
        )
    }

    override fun invalidate(sourceId: String) {
        rangeCapabilities.remove(sourceId)
    }

    override fun invalidateAll() {
        rangeCapabilities.clear()
    }

    override fun release() {
        if (!released.compareAndSet(false, true)) return
        rangeCapabilities.clear()
        activeConnections.toList().forEach(::disconnect)
        activePropFindCalls.toList().forEach(Call::cancel)
    }

    private fun propFind(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        depth: Int,
    ): List<NetworkFileEntry> {
        val requestResource = resourceTracker.acquire(BackendResourceKind.ActiveWebDavRequest)
        return try {
            parseMultiStatus(executePropFind(source, credentials, path, depth), source, path)
        } finally {
            requestResource.close()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun executeGet(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        range: String?,
    ): HttpURLConnection {
        checkNotReleased()
        var url = NetworkPathPolicy.webDavResourceUrl(source.server, path)
            ?: throw IOException("WebDAV requires an HTTPS server URL")
        val visited = hashSetOf(url.toString())
        var redirects = 0
        while (true) {
            val connection = openConnection(url, credentials)
            try {
                connection.requestMethod = "GET"
                if (range != null) connection.setRequestProperty("Range", range)
                connection.connect()
                val status = connection.responseCode
                if (status !in REDIRECT_STATUSES) return connection
                val next = connection.getHeaderField("Location")
                    ?.let { resolveRedirect(url, it, source) }
                disconnect(connection)
                if (++redirects > MAX_REDIRECTS || next == null || !visited.add(next.toString())) {
                    throw NetworkRedirectException("WebDAV redirect policy rejected the response")
                }
                url = next
            } catch (failure: IOException) {
                disconnect(connection)
                throw failure
            } catch (failure: RuntimeException) {
                disconnect(connection)
                throw failure
            }
        }
    }

    private fun executePropFind(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        depth: Int,
    ): ByteArray {
        checkNotReleased()
        var url = NetworkPathPolicy.webDavResourceUrl(source.server, path)
            ?: throw IOException("WebDAV requires an HTTPS server URL")
        val visited = hashSetOf(url.toString())
        val requestBody = PROPFIND_BODY.trimIndent().toRequestBody("application/xml; charset=utf-8".toMediaType())
        var redirects = 0
        while (true) {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", basicAuth(credentials))
                .header("Accept", "application/xml, text/xml, */*")
                .header("Depth", depth.toString())
                .method("PROPFIND", requestBody)
                .build()
            val call = propFindClient.newCall(request)
            activePropFindCalls.add(call)
            try {
                if (released.get()) {
                    call.cancel()
                    throw IllegalStateException("WebDAV file system is released")
                }
                call.execute().use { response ->
                    val status = response.code
                    if (status !in REDIRECT_STATUSES) {
                        if (status !in 200..299) throw WebDavHttpException(status)
                        return response.body?.byteStream()?.readBounded(MAX_PROPFIND_BYTES)
                            ?: throw IOException("WebDAV response has no body")
                    }
                    val next = response.header("Location")?.let { resolveRedirect(url, it, source) }
                    if (++redirects > MAX_REDIRECTS || next == null || !visited.add(next.toString())) {
                        throw NetworkRedirectException("WebDAV redirect policy rejected the response")
                    }
                    url = next
                }
            } finally {
                activePropFindCalls.remove(call)
            }
        }
    }

    private fun openConnection(url: URL, credentials: NetworkCredentials): HttpURLConnection {
        checkNotReleased()
        if (!url.protocol.equals("https", ignoreCase = true)) throw IOException("WebDAV requires HTTPS")
        return (url.openConnection() as? HttpURLConnection)?.apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = false
            setRequestProperty("Authorization", basicAuth(credentials))
            setRequestProperty("Accept", "application/xml, text/xml, */*")
        }?.also { connection ->
            activeConnections.add(connection)
            if (released.get()) {
                disconnect(connection)
                throw IllegalStateException("WebDAV file system is released")
            }
        } ?: throw IOException("Unsupported WebDAV connection")
    }

    private fun HttpURLConnection.openInputStreamOrDisconnect(): InputStream = try {
        inputStream
    } catch (failure: IOException) {
        disconnect(this)
        throw failure
    } catch (failure: RuntimeException) {
        disconnect(this)
        throw failure
    }

    private fun checkNotReleased() {
        check(!released.get()) { "WebDAV file system is released" }
    }

    private fun disconnect(connection: HttpURLConnection?) {
        if (connection == null) return
        activeConnections.remove(connection)
        runCatching { connection.disconnect() }
    }

    private fun resolveRedirect(current: URL, location: String, source: NetworkLibrarySource): URL? {
        val resolved = runCatching { java.net.URI(current.toString()).resolve(location) }.getOrNull() ?: return null
        if (
            !resolved.scheme.equals("https", ignoreCase = true) ||
            resolved.host.isNullOrBlank() ||
            resolved.userInfo != null ||
            resolved.query != null ||
            resolved.fragment != null
        ) return null
        val origin = runCatching { java.net.URI(source.server.trim()) }.getOrNull() ?: return null
        val resolvedPort = resolved.port.takeIf { it >= 0 } ?: 443
        val originPort = origin.port.takeIf { it >= 0 } ?: 443
        if (!resolved.host.equals(origin.host, ignoreCase = true) || resolvedPort != originPort) return null
        val decodedPath = NetworkPathPolicy.decodeUriPath(resolved.rawPath ?: return null) ?: return null
        val configuredRoot = NetworkPathPolicy.webDavConfiguredRoot(source).orEmpty().trimEnd('/')
        if (
            configuredRoot.isNotBlank() &&
            decodedPath != configuredRoot &&
            !decodedPath.startsWith("$configuredRoot/")
        ) return null
        return runCatching {
            URL("https://${resolved.rawAuthority}/${NetworkPathPolicy.encodePath(decodedPath)}")
        }.getOrNull()
    }

    internal fun parseMultiStatus(
        body: ByteArray,
        source: NetworkLibrarySource,
        requestedPath: String,
    ): List<NetworkFileEntry> {
        return try {
            parseMultiStatusDocument(body, source, requestedPath)
        } catch (failure: org.xml.sax.SAXException) {
            throw IOException("Invalid WebDAV multistatus response", failure)
        }
    }

    private fun parseMultiStatusDocument(
        body: ByteArray,
        source: NetworkLibrarySource,
        requestedPath: String,
    ): List<NetworkFileEntry> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            // Android's platform DOM builder uses KXml with document-declaration processing
            // disabled and does not implement these JAXP features. Reject its DOCTYPE below.
            if (javaClass.name != "org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl") {
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                isXIncludeAware = false
                isExpandEntityReferences = false
            }
        }
        val builder = factory.newDocumentBuilder().apply {
            setEntityResolver { _, _ -> throw org.xml.sax.SAXException("External entities are not allowed") }
        }
        val document = builder.parse(ByteArrayInputStream(body))
        if (document.doctype != null) throw IOException("WebDAV document type declarations are not allowed")
        if (!document.documentElement.matchesName("multistatus")) {
            throw IOException("Invalid WebDAV multistatus root")
        }
        return document.documentElement.childElements("response")
            .mapNotNull { response -> parseResponse(response, source, requestedPath) }
            .distinctBy(NetworkFileEntry::path)
    }

    private fun parseResponse(
        response: Element,
        source: NetworkLibrarySource,
        requestedPath: String,
    ): NetworkFileEntry? {
        val responseStatus = response.childElement("status")?.textContent?.let(::parseHttpStatus)
        if (responseStatus != null && !isSuccessfulHttpStatus(responseStatus)) return null
        val href = response.childElement("href")?.textContent ?: return null
        val properties = MutableWebDavProperties()
        val successfulPropstats = response.childElements("propstat").filter { propstat ->
            val status = propstat.childElement("status")?.textContent?.let(::parseHttpStatus)
            isSuccessfulHttpStatus(status)
        }
        if (responseStatus == null && successfulPropstats.isEmpty()) return null
        successfulPropstats.forEach { propstat ->
            val prop = propstat.childElement("prop") ?: return@forEach
            properties.isDirectory = properties.isDirectory || prop.childElement("resourcetype")
                ?.childElement("collection") != null
            properties.sizeBytes = prop.childElement("getcontentlength")
                ?.textContent
                ?.trim()
                ?.toLongOrNull()
                ?.takeIf { it >= 0L }
                ?: properties.sizeBytes
            properties.modifiedAtMs = prop.childElement("getlastmodified")
                ?.textContent
                ?.let(::parseHttpDate)
                ?: properties.modifiedAtMs
            properties.contentType = prop.childElement("getcontenttype")
                ?.textContent
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: properties.contentType
            properties.etag = prop.childElement("getetag")
                ?.textContent
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: properties.etag
        }
        val path = hrefToPath(href, source, requestedPath) ?: return null
        return NetworkFileEntry(
            path = path,
            isDirectory = properties.isDirectory,
            sizeBytes = properties.sizeBytes,
            modifiedAtMs = properties.modifiedAtMs,
            contentType = properties.contentType,
            etag = properties.etag,
        )
    }

    private fun Element.childElement(name: String): Element? {
        val children = childNodes
        for (index in 0 until children.length) {
            val element = children.item(index) as? Element ?: continue
            if (element.matchesName(name)) return element
        }
        return null
    }

    private fun Element.childElements(name: String): List<Element> {
        val children = childNodes
        val matches = ArrayList<Element>()
        for (index in 0 until children.length) {
            val element = children.item(index) as? Element ?: continue
            if (element.matchesName(name)) matches += element
        }
        return matches
    }

    private fun Element.matchesName(name: String): Boolean {
        return (localName ?: tagName.substringAfterLast(':')).equals(name, ignoreCase = true)
    }

    internal fun hrefToPath(href: String, source: NetworkLibrarySource, requestedPath: String): String? {
        val requestUrl = NetworkPathPolicy.webDavResourceUrl(source.server, requestedPath) ?: return null
        val base = URL("${requestUrl.toString().trimEnd('/')}/")
        val resolved = runCatching { URL(base, href) }.getOrNull() ?: return null
        val baseOrigin = URL(source.server)
        if (
            !resolved.protocol.equals("https", true) ||
            !resolved.host.equals(baseOrigin.host, true) ||
            effectivePort(resolved) != effectivePort(baseOrigin)
        ) return null
        val basePath = java.net.URI(source.server).rawPath.orEmpty()
            .let(NetworkPathPolicy::decodeUriPath)
            ?.trimEnd('/')
            .orEmpty()
        val resolvedPath = runCatching { java.net.URI(resolved.toString()).rawPath }
            .getOrNull()
            ?.let(NetworkPathPolicy::decodeUriPath)
            ?.trimEnd('/')
            ?: return null
        val configuredRoot = NetworkPathPolicy.webDavConfiguredRoot(source).orEmpty().trimEnd('/')
        if (
            configuredRoot.isNotBlank() &&
            resolvedPath != configuredRoot &&
            !resolvedPath.startsWith("$configuredRoot/")
        ) return null
        val relative = when {
            basePath.isBlank() -> resolvedPath
            resolvedPath == basePath -> ""
            resolvedPath.startsWith("$basePath/") -> resolvedPath.removePrefix(basePath).trim('/')
            else -> return null
        }
        val path = NetworkPathPolicy.normalizeRelativePath(relative)
        return path
    }

    private fun effectivePort(url: URL): Int = url.port.takeIf { it >= 0 } ?: 443

    private fun basicAuth(credentials: NetworkCredentials): String {
        return "Basic " + Base64.getEncoder().encodeToString(
            "${credentials.username}:${credentials.password}".toByteArray(Charsets.UTF_8),
        )
    }

    private fun classifyFailure(failure: Throwable): NetworkAvailability =
        failure.remoteIoFailureKind().toNetworkAvailability()

    private fun parseHttpStatus(value: String): Int? =
        HTTP_STATUS_REGEX.find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun isSuccessfulHttpStatus(statusCode: Int?): Boolean =
        statusCode != null && statusCode in 200..299

    private fun java.io.InputStream.readBounded(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream(minOf(maxBytes, 32 * 1024))
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val count = read(buffer)
            if (count < 0) return output.toByteArray()
            if (output.size() > maxBytes - count) throw IOException("WebDAV response is too large")
            output.write(buffer, 0, count)
        }
    }

    private fun parseHttpDate(value: String): Long? = runCatching {
        ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
    }.getOrNull()

    private companion object {
        const val MAX_PROPFIND_BYTES = 512 * 1024
        const val MAX_DIRECTORY_COUNT = 25_000
        const val TIMEOUT_MS = 12_000
        const val HTTP_RANGE_NOT_SATISFIABLE = 416
        const val MAX_REDIRECTS = 3
        val REDIRECT_STATUSES = setOf(301, 302, 303, 307, 308)
        val HTTP_STATUS_REGEX = Regex("\\b(\\d{3})\\b")
        const val PROPFIND_BODY = """
            <?xml version="1.0" encoding="utf-8" ?>
            <D:propfind xmlns:D="DAV:">
                <D:prop>
                    <D:getcontentlength/>
                    <D:getlastmodified/>
                    <D:getcontenttype/>
                    <D:getetag/>
                    <D:resourcetype/>
                </D:prop>
            </D:propfind>
        """
    }
}

private fun requireWebDavDirectory(entries: List<NetworkFileEntry>, path: String) {
    if (entries.none { it.path == path && it.isDirectory }) {
        throw IOException("WebDAV response does not confirm the requested directory")
    }
}

private class MutableWebDavProperties {
    var isDirectory = false
    var sizeBytes: Long? = null
    var modifiedAtMs: Long? = null
    var contentType: String? = null
    var etag: String? = null
}

internal data class WebDavContentRange(
    val start: Long,
    val end: Long,
    val totalLength: Long?,
) {
    val length: Long?
        get() = if (end < start || end - start == Long.MAX_VALUE) null else end - start + 1L
}

internal fun parseWebDavContentRange(value: String?): WebDavContentRange? {
    val normalized = value?.trim() ?: return null
    if (!normalized.startsWith("bytes ", ignoreCase = true)) return null
    val parts = normalized.substring(6).split('/', limit = 2)
    if (parts.size != 2) return null
    val bounds = parts[0].split('-', limit = 2)
    if (bounds.size != 2) return null
    val start = bounds[0].toLongOrNull() ?: return null
    val end = bounds[1].toLongOrNull() ?: return null
    if (start < 0L || end < start || end - start == Long.MAX_VALUE) return null
    val total = parts[1].takeUnless { it == "*" }?.toLongOrNull() ?:
        if (parts[1] == "*") null else return null
    if (total != null && (total <= 0L || end >= total)) return null
    return WebDavContentRange(start, end, total)
}

private fun parseUnsatisfiedContentRange(value: String?): Long? {
    val raw = value?.trim() ?: return null
    if (!raw.startsWith("bytes ", ignoreCase = true)) return null
    val normalized = raw.substring(6)
    if (!normalized.startsWith("*/")) return null
    return normalized.substringAfter('/').takeUnless { it == "*" }?.toLongOrNull()
}

private fun Long.checkedRangeEnd(length: Long): Long {
    if (length <= 0L || this > Long.MAX_VALUE - (length - 1L)) {
        throw IOException("WebDAV byte range is too large")
    }
    return this + length - 1L
}

private class LimitedInputStream(
    input: InputStream,
    private var remaining: Long,
) : FilterInputStream(input) {
    private var markedRemaining = remaining

    override fun available(): Int = minOf(super.available().toLong(), remaining).toInt()

    @Synchronized
    override fun mark(readlimit: Int) {
        super.mark(readlimit)
        markedRemaining = remaining
    }

    @Synchronized
    override fun reset() {
        super.reset()
        remaining = markedRemaining
    }

    override fun read(): Int {
        if (remaining == 0L) return -1
        val value = super.read()
        if (value >= 0) remaining -= 1L
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (offset < 0 || length < 0 || length > buffer.size - offset) {
            throw IndexOutOfBoundsException()
        }
        if (length == 0) return 0
        if (remaining == 0L) return -1
        val boundedLength = minOf(length.toLong(), remaining).toInt()
        val count = super.read(buffer, offset, boundedLength)
        if (count > 0) remaining -= count
        return count
    }

    override fun skip(length: Long): Long {
        val count = super.skip(minOf(length, remaining))
        remaining -= count
        return count
    }
}

internal fun limitWebDavRead(input: InputStream, length: Long): InputStream =
    if (length >= 0L) LimitedInputStream(input, length) else input
