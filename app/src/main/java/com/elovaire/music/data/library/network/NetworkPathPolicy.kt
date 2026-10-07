package elovaire.music.droidbeauty.app.data.library.network

import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

internal object NetworkPathPolicy {
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
        return when {
            base.isBlank() -> normalizeRelativePath(child)
            child.isBlank() -> normalizeRelativePath(base)
            else -> normalizeRelativePath("$base/$child")
        }
    }

    /** Returns a root-relative path only when it cannot escape or change path segmentation. */
    fun validateRelativePath(path: String): String? {
        if (path.indexOf('\\') >= 0 || path.indexOf('\u0000') >= 0) return null
        val normalized = StringBuilder(path.length)
        var segmentStart = 0
        var index = 0
        while (index <= path.length) {
            if (index == path.length || path[index] == '/') {
                if (index > segmentStart) {
                    val segmentLength = index - segmentStart
                    if (segmentLength == 1 && path[segmentStart] == '.') return null
                    if (segmentLength == 2 &&
                        path[segmentStart] == '.' &&
                        path[segmentStart + 1] == '.'
                    ) return null
                    if (normalized.isNotEmpty()) normalized.append('/')
                    normalized.append(path, segmentStart, index)
                }
                segmentStart = index + 1
            }
            index += 1
        }
        return normalized.toString()
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
        if (ENCODED_PATH_SEPARATOR.containsMatchIn(rawPath) || rawPath.indexOf('\\') >= 0) return null
        val decoded = runCatching {
            URLDecoder.decode(rawPath.replace("+", "%2B"), Charsets.UTF_8.name())
        }.getOrNull() ?: return null
        return validateRelativePath(decoded)
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
        val basePath = decodeUriPath(base.rawPath.orEmpty()) ?: return null
        val relativePath = validateRelativePath(path) ?: return null
        val resourcePath = validateRelativePath(join(basePath, relativePath)) ?: return null
        val encodedPath = "/" + encodePath(resourcePath)
        return runCatching { URL("https://${base.rawAuthority}$encodedPath") }.getOrNull()
    }

    fun webDavConfiguredRoot(source: NetworkLibrarySource): String? {
        val base = runCatching { URI(source.server.trim()) }.getOrNull() ?: return null
        if (
            !base.scheme.equals("https", ignoreCase = true) ||
            base.host.isNullOrBlank() ||
            base.userInfo != null ||
            base.query != null ||
            base.fragment != null
        ) return null
        val basePath = decodeUriPath(base.rawPath.orEmpty()) ?: return null
        val sharePath = validateRelativePath(source.shareOrPath) ?: return null
        return validateRelativePath(join(basePath, sharePath))
    }

    fun smbServer(server: String): String? {
        return smbEndpoint(server)?.first
    }

    fun smbPort(server: String): Int = smbEndpoint(server)?.second ?: 445

    private fun smbEndpoint(server: String): Pair<String, Int>? {
        val normalizedServer = server.trim()
        val endpoint = if (normalizedServer.startsWith("smb://", ignoreCase = true)) {
            normalizedServer.substring("smb://".length)
        } else {
            normalizedServer
        }
        val raw = endpoint.substringBefore('/')
        if (raw.startsWith('[')) {
            val end = raw.indexOf(']')
            if (end <= 1) return null
            val host = raw.substring(1, end)
            val suffix = raw.substring(end + 1)
            val port = when {
                suffix.isEmpty() -> 445
                !suffix.startsWith(':') -> return null
                else -> suffix.substring(1).toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
            }
            return host.takeIf(String::isNotBlank)?.let { it to port }
        }
        val separator = raw.lastIndexOf(':')
        if (separator > 0 && raw.indexOf(':') != separator) return null
        val host = if (separator > 0 && raw.indexOf(':') == separator) raw.substring(0, separator) else raw
        val port = if (host == raw) {
            445
        } else {
            raw.substring(separator + 1).toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
        }
        return host.takeIf(String::isNotBlank)?.let { it to port }
    }

    private const val HEX = "0123456789ABCDEF"
    private val ENCODED_PATH_SEPARATOR = Regex("%(?i:2f|5c)")

    fun smbShareAndPath(value: String): Pair<String, String>? {
        val parts = normalizeRelativePath(value).split('/', limit = 2)
        val share = parts.firstOrNull()?.takeIf(String::isNotBlank) ?: return null
        return share to parts.getOrNull(1).orEmpty()
    }
}
