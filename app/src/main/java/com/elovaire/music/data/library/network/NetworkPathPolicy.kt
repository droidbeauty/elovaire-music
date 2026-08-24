package elovaire.music.droidbeauty.app.data.library.network

import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

internal object NetworkPathPolicy {
    fun normalizeServer(server: String): String {
        return server.trim().replace('\\', '/').trimEnd('/').lowercase(Locale.ROOT)
    }

    fun normalizeRelativePath(path: String): String {
        val parts = path.trim().replace('\\', '/').split('/')
        val normalized = ArrayDeque<String>()
        parts.forEach { part ->
            when {
                part.isBlank() || part == "." -> Unit
                part == ".." -> if (normalized.isNotEmpty()) normalized.removeLast()
                else -> normalized.addLast(part)
            }
        }
        return normalized.joinToString("/")
    }

    fun join(base: String, child: String): String {
        return normalizeRelativePath(listOf(base, child).filter(String::isNotBlank).joinToString("/"))
    }

    /** Returns a root-relative path only when it cannot escape or change path segmentation. */
    fun validateRelativePath(path: String): String? {
        if (path.indexOf('\\') >= 0) return null
        val normalized = path.trim('/').split('/').filter(String::isNotEmpty)
        if (normalized.any { it == "." || it == ".." || it.indexOf('\u0000') >= 0 }) return null
        return normalized.joinToString("/")
    }

    fun encodePath(path: String): String {
        return validateRelativePath(path).orEmpty()
            .split('/')
            .filter(String::isNotEmpty)
            .joinToString("/") { encodeSegment(it) }
    }

    private fun encodeSegment(segment: String): String = buildString {
        segment.toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            val value = byte.toInt() and 0xff
            if (isUnreserved(value)) {
                append(value.toChar())
            } else {
                append('%')
                append(HEX[value ushr 4])
                append(HEX[value and 0x0f])
            }
        }
    }

    private fun isUnreserved(value: Int): Boolean = when {
        value in 'A'.code..'Z'.code -> true
        value in 'a'.code..'z'.code -> true
        value in '0'.code..'9'.code -> true
        else -> value == '-'.code || value == '.'.code || value == '_'.code || value == '~'.code
    }

    fun decodeUriPath(rawPath: String): String? {
        if (Regex("%(?i:2f|5c)").containsMatchIn(rawPath) || rawPath.indexOf('\\') >= 0) return null
        val decoded = runCatching {
            URLDecoder.decode(rawPath.replace("+", "%2B"), Charsets.UTF_8.name())
        }.getOrNull() ?: return null
        return validateRelativePath(decoded)
    }

    fun webDavBaseUrl(server: String): String? {
        val uri = runCatching { URI(server.trim()) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.host.isNullOrBlank()) return null
        if (uri.userInfo != null || uri.query != null || uri.fragment != null) return null
        return uri.toASCIIString().trimEnd('/')
    }

    fun webDavResourceUrl(server: String, path: String): URL? {
        val base = runCatching { URI(server.trim()) }.getOrNull() ?: return null
        if (
            !base.scheme.equals("https", ignoreCase = true) ||
            base.host.isNullOrBlank() ||
            base.userInfo != null ||
            base.query != null ||
            base.fragment != null
        ) return null
        val basePath = decodeUriPath(base.rawPath.orEmpty()).orEmpty()
        val resourcePath = validateRelativePath(join(basePath, path)) ?: return null
        val encodedPath = "/" + encodePath(resourcePath)
        return runCatching { URL("https://${base.rawAuthority}$encodedPath") }.getOrNull()
    }

    fun webDavConfiguredRoot(source: NetworkLibrarySource): String? {
        val base = runCatching { URI(source.server.trim()) }.getOrNull() ?: return null
        val basePath = decodeUriPath(base.rawPath.orEmpty()).orEmpty()
        return validateRelativePath(join(basePath, source.shareOrPath))
    }

    fun smbServer(server: String): String? {
        return smbEndpoint(server)?.first
    }

    fun smbPort(server: String): Int = smbEndpoint(server)?.second ?: 445

    private fun smbEndpoint(server: String): Pair<String, Int>? {
        val raw = server.trim().removePrefix("smb://").substringBefore('/')
        if (raw.startsWith('[')) {
            val end = raw.indexOf(']')
            if (end <= 1) return null
            val host = raw.substring(1, end)
            val port = raw.substring(end + 1).removePrefix(":").toIntOrNull() ?: 445
            return host.takeIf(String::isNotBlank)?.let { it to (port.takeIf { value -> value in 1..65535 } ?: 445) }
        }
        val separator = raw.lastIndexOf(':')
        val host = if (separator > 0 && raw.indexOf(':') == separator) raw.substring(0, separator) else raw
        val port = if (host == raw) 445 else raw.substring(separator + 1).toIntOrNull() ?: 445
        return host.takeIf(String::isNotBlank)?.let { it to (port.takeIf { value -> value in 1..65535 } ?: 445) }
    }

    private const val HEX = "0123456789ABCDEF"

    fun smbShareAndPath(value: String): Pair<String, String>? {
        val parts = normalizeRelativePath(value).split('/', limit = 2)
        val share = parts.firstOrNull()?.takeIf(String::isNotBlank) ?: return null
        return share to parts.getOrNull(1).orEmpty()
    }
}
