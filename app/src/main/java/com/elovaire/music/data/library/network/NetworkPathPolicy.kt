package elovaire.music.droidbeauty.app.data.library.network

import java.net.URI
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

    fun webDavBaseUrl(server: String): String? {
        val uri = runCatching { URI(server.trim()) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.host.isNullOrBlank()) return null
        if (uri.userInfo != null || uri.query != null || uri.fragment != null) return null
        return uri.toASCIIString().trimEnd('/')
    }

    fun smbServer(server: String): String? {
        val value = server.trim().removePrefix("smb://").substringBefore('/')
        return value.substringBefore(':').takeIf { it.isNotBlank() }
    }

    fun smbPort(server: String): Int = server.trim()
        .removePrefix("smb://")
        .substringBefore('/')
        .substringAfter(':', "445")
        .toIntOrNull()
        ?.takeIf { it in 1..65535 }
        ?: 445

    fun smbShareAndPath(value: String): Pair<String, String>? {
        val parts = normalizeRelativePath(value).split('/', limit = 2)
        val share = parts.firstOrNull()?.takeIf(String::isNotBlank) ?: return null
        return share to parts.getOrNull(1).orEmpty()
    }
}
