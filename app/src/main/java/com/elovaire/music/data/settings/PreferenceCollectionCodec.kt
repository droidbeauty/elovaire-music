package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind

internal object PreferenceCollectionCodec {
    const val RECORD_SEPARATOR = "\u001E"
    private const val FIELD_SEPARATOR = "\u001F"

    fun serializeSearchHistory(entry: SearchHistoryEntry): String = listOf(
        entry.key,
        entry.kind.name,
        entry.title,
        entry.subtitle,
        entry.artUri?.toString().orEmpty(),
        entry.albumId?.toString().orEmpty(),
        entry.query.orEmpty(),
    ).joinToString(FIELD_SEPARATOR)

    fun deserializeSearchHistory(value: String): SearchHistoryEntry? {
        val parts = value.split(FIELD_SEPARATOR)
        if (parts.size < 7) return null
        val kind = SearchHistoryKind.entries.firstOrNull { it.name == parts[1] } ?: return null
        return SearchHistoryEntry(
            key = parts[0],
            kind = kind,
            title = parts[2],
            subtitle = parts[3],
            artUri = parts[4].takeIf(String::isNotBlank)?.let(Uri::parse),
            albumId = parts[5].toLongOrNull(),
            query = parts[6].takeIf(String::isNotBlank),
        )
    }

    fun serializeLibraryFolder(selection: LibraryFolderSelection): String = listOf(
        selection.uri?.toString().orEmpty(),
        selection.path,
        selection.displayName,
        selection.isDefaultMusicFolder.toString(),
    ).joinToString(FIELD_SEPARATOR)

    fun deserializeLibraryFolder(value: String): LibraryFolderSelection? {
        val parts = value.split(FIELD_SEPARATOR)
        if (parts.size < 4) return null
        val path = parts[1].trim()
        val uri = parts[0].takeIf(String::isNotBlank)?.let(Uri::parse)
        if (path.isBlank() && uri == null) return null
        return LibraryFolderSelection(
            uri = uri,
            path = path.ifBlank { uri.toString() },
            displayName = parts[2].trim().ifBlank { "Library folder" },
            isDefaultMusicFolder = parts[3].toBooleanStrictOrNull() == true,
        )
    }

    fun serializePlayCounts(counts: Map<Long, Int>): String =
        counts.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun deserializePlayCounts(value: String): Map<Long, Int> = value.split(",")
        .mapNotNull { entry ->
            val parts = entry.split(":")
            val id = parts.getOrNull(0)?.toLongOrNull()?.takeIf { it > 0L } ?: return@mapNotNull null
            val count = parts.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0) ?: return@mapNotNull null
            id to count
        }
        .toMap()
}
