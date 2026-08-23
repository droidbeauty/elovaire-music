package elovaire.music.droidbeauty.app.data.library.network

import android.util.Xml
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

internal class WebDavNetworkFileSystem : NetworkFileSystem {
    override fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult {
        return runCatching {
            propFind(source, credentials, source.shareOrPath, depth = 0)
            NetworkProbeResult(NetworkAvailability.Available)
        }.getOrElse { failure ->
            NetworkProbeResult(classifyFailure(failure), failure::class.simpleName)
        }
    }

    override fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int,
        maxDepth: Int,
    ): List<NetworkFileEntry> {
        require(maxEntries > 0)
        val results = ArrayList<NetworkFileEntry>()
        val pending = ArrayDeque<Pair<String, Int>>()
        pending.addLast(source.shareOrPath to 0)
        val visited = hashSetOf<String>()
        while (pending.isNotEmpty() && results.size < maxEntries) {
            val (directory, depth) = pending.removeFirst()
            val normalized = NetworkPathPolicy.normalizeRelativePath(directory)
            if (!visited.add(normalized)) continue
            propFind(source, credentials, normalized, depth = 1).forEach { entry ->
                if (entry.path == normalized || results.size >= maxEntries) return@forEach
                results += entry
                if (entry.isDirectory && depth < maxDepth) {
                    pending.addLast(entry.path to depth + 1)
                }
            }
        }
        return results
    }

    override fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        require(position >= 0L)
        require(length == -1L || length > 0L)
        val connection = openConnection(source, credentials, path)
        connection.requestMethod = "GET"
        if (position > 0L || length > 0L) {
            val end = if (length > 0L) position + length - 1L else null
            connection.setRequestProperty("Range", "bytes=$position-${end ?: ""}")
        }
        connection.connect()
        val status = connection.responseCode
        if (status !in 200..299) {
            connection.disconnect()
            throw IOException("WebDAV media request failed with HTTP $status")
        }
        if (position > 0L && status != HttpURLConnection.HTTP_PARTIAL) {
            connection.disconnect()
            throw IOException("WebDAV server did not honor the requested byte range")
        }
        val input = connection.inputStream
        val responseLength = connection.contentLengthLong.takeIf { it >= 0L }
        return NetworkReadHandle(
            input = input,
            length = if (length > 0L) length.coerceAtMost(responseLength ?: length) else responseLength,
            closeHandle = connection::disconnect,
        )
    }

    private fun propFind(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        depth: Int,
    ): List<NetworkFileEntry> {
        val connection = openConnection(source, credentials, path)
        connection.requestMethod = "PROPFIND"
        connection.setRequestProperty("Depth", depth.toString())
        connection.setRequestProperty("Content-Type", "application/xml; charset=utf-8")
        connection.doOutput = true
        val requestBody = PROPFIND_BODY.toByteArray(Charsets.UTF_8)
        connection.setRequestProperty("Content-Length", requestBody.size.toString())
        connection.connect()
        connection.outputStream.use { output -> output.write(requestBody) }
        val status = connection.responseCode
        if (status !in 200..299) {
            connection.disconnect()
            throw IOException("WebDAV directory request failed with HTTP $status")
        }
        return try {
            connection.inputStream.use { input -> parseMultiStatus(input.readBounded(MAX_PROPFIND_BYTES), source, path) }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
    ): HttpURLConnection {
        val baseUrl = NetworkPathPolicy.webDavBaseUrl(source.server)
            ?: throw IOException("WebDAV requires an HTTPS server URL")
        val url = URL("${baseUrl.trimEnd('/')}/${NetworkPathPolicy.normalizeRelativePath(path)}")
        if (!url.protocol.equals("https", ignoreCase = true)) throw IOException("WebDAV requires HTTPS")
        return (url.openConnection() as? HttpURLConnection)?.apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = false
            setRequestProperty("Authorization", basicAuth(credentials))
            setRequestProperty("Accept", "application/xml, text/xml, */*")
        } ?: throw IOException("Unsupported WebDAV connection")
    }

    private fun parseMultiStatus(
        body: ByteArray,
        source: NetworkLibrarySource,
        requestedPath: String,
    ): List<NetworkFileEntry> {
        val parser = Xml.newPullParser().apply {
            setInput(ByteArrayInputStream(body), Charsets.UTF_8.name())
        }
        val entries = mutableListOf<NetworkFileEntry>()
        var event = parser.eventType
        var inResponse = false
        var currentHref: String? = null
        var currentResourceType = false
        var currentLength: Long? = null
        var currentModified: Long? = null
        var currentType: String? = null
        var currentEtag: String? = null
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name.substringAfterLast(':').lowercase(Locale.ROOT)) {
                    "response" -> {
                        inResponse = true
                        currentHref = null
                        currentResourceType = false
                        currentLength = null
                        currentModified = null
                        currentType = null
                        currentEtag = null
                    }
                    "href" -> if (inResponse) currentHref = parser.nextText()
                    "collection" -> if (inResponse) currentResourceType = true
                    "getcontentlength" -> if (inResponse) currentLength = parser.nextText().toLongOrNull()
                    "getlastmodified" -> if (inResponse) currentModified = parseHttpDate(parser.nextText())
                    "getcontenttype" -> if (inResponse) currentType = parser.nextText().trim().takeIf(String::isNotBlank)
                    "getetag" -> if (inResponse) currentEtag = parser.nextText().trim().takeIf(String::isNotBlank)
                }
                XmlPullParser.END_TAG -> if (parser.name.substringAfterLast(':').equals("response", true)) {
                    val path = currentHref?.let { hrefToPath(it, source, requestedPath) }
                    if (path != null) {
                        entries += NetworkFileEntry(path, currentResourceType, currentLength, currentModified, currentType, currentEtag)
                    }
                    inResponse = false
                }
            }
            event = parser.next()
        }
        return entries.distinctBy { it.path }
    }

    private fun hrefToPath(href: String, source: NetworkLibrarySource, requestedPath: String): String? {
        val baseUrl = NetworkPathPolicy.webDavBaseUrl(source.server) ?: return null
        if (href.contains("%2e", ignoreCase = true) ||
            href.contains("%2f", ignoreCase = true) ||
            href.contains("%5c", ignoreCase = true)
        ) return null
        val base = URL("${baseUrl.trimEnd('/')}/")
        val resolved = URL(base, href)
        if (!resolved.protocol.equals("https", true) || resolved.host != base.host || resolved.port != base.port) return null
        val basePath = base.path.trimEnd('/')
        val resolvedPath = resolved.path
        val relative = when {
            resolvedPath == basePath -> ""
            resolvedPath.startsWith("$basePath/") -> resolvedPath.removePrefix(basePath).trim('/')
            else -> return null
        }
        val path = NetworkPathPolicy.normalizeRelativePath(relative)
        if (path.isBlank() && requestedPath.isBlank()) return null
        return path
    }

    private fun basicAuth(credentials: NetworkCredentials): String {
        return "Basic " + Base64.getEncoder().encodeToString(
            "${credentials.username}:${credentials.password}".toByteArray(Charsets.UTF_8),
        )
    }

    private fun classifyFailure(failure: Throwable): NetworkAvailability = when {
        failure.message?.contains("401") == true || failure.message?.contains("403") == true ->
            NetworkAvailability.AuthenticationRequired
        failure is java.net.UnknownHostException || failure is java.net.ConnectException -> NetworkAvailability.Offline
        else -> NetworkAvailability.Unavailable
    }

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
        const val TIMEOUT_MS = 12_000
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
