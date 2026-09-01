package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.isValidMediaId
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind

internal object PreferenceCollectionCodec {
    const val RECORD_SEPARATOR = "\u001E"
    private const val FIELD_SEPARATOR = "\u001F"
    private const val VERSIONED_PREFIX = "v2:"
    private const val MAX_SERIALIZED_FIELD_CHARS = 32 * 1024
    private const val MAX_DECODED_FIELD_CHARS = 16 * 1024
    private const val MAX_PLAY_COUNT_SERIALIZED_CHARS = 4 * 1024 * 1024
    private const val MAX_PLAY_COUNT_ENTRIES = 100_000

    fun serializeSearchHistory(entry: SearchHistoryEntry): String = listOf(
        entry.key,
        entry.kind.name,
        entry.title,
        entry.subtitle,
        entry.artUri?.toString().orEmpty(),
        entry.albumId?.toString().orEmpty(),
        entry.query.orEmpty(),
    ).joinToString(FIELD_SEPARATOR, prefix = VERSIONED_PREFIX, transform = ::encodeField)

    fun deserializeSearchHistory(value: String): SearchHistoryEntry? {
        if (value.length > MAX_SERIALIZED_FIELD_CHARS) return null
        val versioned = value.startsWith(VERSIONED_PREFIX)
        if (!versioned && VERSIONED_PREFIX_MARKER.containsMatchIn(value)) return null
        val parts = if (versioned) {
            value.removePrefix(VERSIONED_PREFIX)
                .split(FIELD_SEPARATOR)
                .map(::decodeField)
        } else {
            value.split(FIELD_SEPARATOR).map { it }
        }
        if (parts.size != 7 || parts.any { it == null }) return null
        val fields = parts.mapNotNull { it }
        val kind = SearchHistoryKind.entries.firstOrNull { it.name == fields[1] } ?: return null
        return SearchHistoryEntry(
            key = fields[0],
            kind = kind,
            title = fields[2],
            subtitle = fields[3],
            artUri = fields[4].takeIf(String::isNotBlank)?.let(Uri::parse),
            albumId = fields[5].toLongOrNull()?.takeIf(::isValidMediaId),
            query = fields[6].takeIf(String::isNotBlank),
        )
    }

    fun serializeLibraryFolder(selection: LibraryFolderSelection): String = listOf(
        selection.uri?.toString().orEmpty(),
        selection.path,
        selection.displayName,
        selection.isDefaultMusicFolder.toString(),
    ).joinToString(FIELD_SEPARATOR, prefix = VERSIONED_PREFIX, transform = ::encodeField)

    fun deserializeLibraryFolder(value: String): LibraryFolderSelection? {
        if (value.length > MAX_SERIALIZED_FIELD_CHARS) return null
        val versioned = value.startsWith(VERSIONED_PREFIX)
        if (!versioned && VERSIONED_PREFIX_MARKER.containsMatchIn(value)) return null
        val parts = if (versioned) {
            value.removePrefix(VERSIONED_PREFIX)
                .split(FIELD_SEPARATOR)
                .map(::decodeField)
        } else {
            value.split(FIELD_SEPARATOR).map { it }
        }
        if (parts.size != 4 || parts.any { it == null }) return null
        val fields = parts.mapNotNull { it }
        val path = fields[1].trimCodecWhitespace()
        val uri = fields[0].takeIf(String::isNotBlank)?.let(::parseStoredFolderUri)
        if (fields[0].isNotBlank() && uri == null) return null
        if (path.isBlank() && uri == null) return null
        return LibraryFolderSelection(
            uri = uri,
            path = path.ifBlank { uri.toString() },
            displayName = fields[2].trimCodecWhitespace().ifBlank { "Library folder" },
            isDefaultMusicFolder = fields[3].toBooleanStrictOrNull() == true,
        )
    }

    fun serializePlayCounts(counts: Map<Long, Int>): String =
        counts.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun deserializePlayCounts(value: String): Map<Long, Int> {
        if (value.length > MAX_PLAY_COUNT_SERIALIZED_CHARS) return emptyMap()
        val entries = value.split(",", limit = MAX_PLAY_COUNT_ENTRIES + 1)
        if (entries.size > MAX_PLAY_COUNT_ENTRIES) return emptyMap()
        val decoded = LinkedHashMap<Long, Int>(entries.size)
        var duplicateId = false
        entries.forEach { entry ->
            val parts = entry.split(":", limit = 3)
            if (parts.size != 2) return@forEach
            val id = parts.getOrNull(0)?.toLongOrNull()?.takeIf(::isValidMediaId)
                ?: return@forEach
            val count = parts.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0)
                ?: return@forEach
            if (decoded.put(id, count) != null) duplicateId = true
        }
        return if (duplicateId) emptyMap() else decoded
    }

    private fun encodeField(value: String): String {
        return java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))
    }

    private fun decodeField(value: String): String? {
        if (value.length > MAX_SERIALIZED_FIELD_CHARS) return null
        return runCatching {
            String(java.util.Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
                .takeIf { it.length <= MAX_DECODED_FIELD_CHARS }
        }.getOrNull()
    }

    private fun parseStoredFolderUri(raw: String): Uri? {
        if (raw.length > MAX_SERIALIZED_FIELD_CHARS || raw.any(Char::isWhitespace)) return null
        return runCatching { Uri.parse(raw) }
            .getOrNull()
            ?.takeIf { it.scheme.equals("content", ignoreCase = true) && !it.authority.isNullOrBlank() }
    }

    private fun String.trimCodecWhitespace(): String = trim {
        it != '\u001E' && it != '\u001F' && (it.isWhitespace() || it.code <= 0x20)
    }

    private val VERSIONED_PREFIX_MARKER = Regex("(?i)^v\\d+:")
}
