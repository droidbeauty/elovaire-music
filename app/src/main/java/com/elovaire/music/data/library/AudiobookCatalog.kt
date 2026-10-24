package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.sumDurationMs
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

internal object AudiobookCatalog {
    fun build(songs: List<Song>): List<Audiobook> {
        return songs
            .asSequence()
            .filter { it.mediaKind == AudioMediaKind.Audiobook }
            .groupBy(::groupKey)
            .mapNotNull { (_, parts) -> buildBook(parts) }
            .sortedWith(AUDIOBOOK_COMPARATOR)
            .toList()
    }

    /** Resolves only the book that contains [song], without materializing the catalog. */
    fun findContaining(songs: List<Song>, song: Song): Audiobook? {
        val songIdentity = MediaIdentityResolver.stableKey(song)
        val matchingKeys = songs
            .asSequence()
            .filter {
                it.mediaKind == AudioMediaKind.Audiobook &&
                    MediaIdentityResolver.stableKey(it) == songIdentity
            }
            .mapTo(linkedSetOf(), ::groupKey)
        if (matchingKeys.isEmpty()) return null

        val matchingParts = linkedMapOf<String, MutableList<Song>>()
        songs.forEach { song ->
            if (song.mediaKind != AudioMediaKind.Audiobook) return@forEach
            val stableKey = groupKey(song)
            if (stableKey in matchingKeys) {
                matchingParts.getOrPut(stableKey, ::mutableListOf).add(song)
            }
        }
        return matchingParts
            .asSequence()
            .mapNotNull { (_, parts) -> buildBook(parts) }
            .minWithOrNull(AUDIOBOOK_COMPARATOR)
    }

    private fun groupKey(song: Song): String {
        val uri = song.uri.toString()
        val source = when {
            uri.startsWith("content://media/", ignoreCase = true) -> uri.substringAfter("content://media/").substringBefore('/')
            uri.startsWith("elovaire-network://", ignoreCase = true) -> uri.substringAfter("elovaire-network://").substringBefore('/')
            else -> uri.substringAfter("://", uri).substringBefore('/')
        }.lowercase(Locale.ROOT)
        val title = bookTitle(song).lowercase(Locale.ROOT)
        val author = (song.albumArtist ?: song.artist).trim().lowercase(Locale.ROOT)
        val parent = song.libraryPath
            ?.replace('\\', '/')
            ?.substringBeforeLast('/', "")
            ?.lowercase(Locale.ROOT)
            .orEmpty()
        return if (song.albumId > 0L) {
            "$source|album:${song.albumId}|$author"
        } else {
            "$source|folder:$parent|$title|$author"
        }
    }

    fun routeKey(book: Audiobook): String = book.stableKey

    fun legacyStableKey(book: Audiobook): String? = book.parts.firstOrNull()?.song?.let(::groupKey)

    fun stableId(book: Audiobook): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(book.stableKey.toByteArray(StandardCharsets.UTF_8))
            .toHexString()
    }

    private fun buildBook(parts: List<Song>): Audiobook? {
        val orderedParts = parts.map { song ->
            PartSortKey(song, song.fileName.lowercase(Locale.ROOT), MediaIdentityResolver.stableKey(song))
        }.sortedWith(PART_COMPARATOR).distinctBy(PartSortKey::stableKey)
        val ordered = orderedParts.map(PartSortKey::song)
        val first = ordered.firstOrNull() ?: return null
        val stableKey = stableBookKey(orderedParts.minOf(PartSortKey::stableKey))
        val author = ordered
            .asSequence()
            .mapNotNull { it.albumArtist?.trim()?.takeIf(String::isNotBlank) ?: it.artist.trim().takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: "Unknown Author"
        return Audiobook(
            stableKey = stableKey,
            title = bookTitle(first),
            author = author,
            artUri = ordered.firstNotNullOfOrNull(Song::artUri),
            durationMs = ordered.sumDurationMs(),
            parts = ordered.mapIndexed { index, song -> AudiobookPart(song, index + 1) },
            description = ordered.firstNotNullOfOrNull { it.description?.trim()?.takeIf(String::isNotBlank) },
        )
    }

    private fun stableBookKey(anchor: String): String {
        return "book:" + MessageDigest.getInstance("SHA-256")
            .digest(anchor.toByteArray(StandardCharsets.UTF_8))
            .toHexString()
    }

    private fun bookTitle(song: Song): String {
        return song.album.trim()
            .takeUnless { it.isBlank() || it.equals("Unknown Album", ignoreCase = true) }
            ?: song.libraryPath
                ?.replace('\\', '/')
                ?.substringBeforeLast('/', "")
                ?.substringAfterLast('/')
                ?.trim()
                ?.takeUnless(String::isNullOrBlank)
            ?: song.title.trim().takeUnless(String::isNullOrBlank)
            ?: song.fileName.substringBeforeLast('.').ifBlank { "Audiobook" }
    }

    private data class PartSortKey(val song: Song, val fileName: String, val stableKey: String)

    private val PART_COMPARATOR = compareBy<PartSortKey>(
        { it.song.discNumber.coerceAtLeast(1) },
        { it.song.trackNumber.coerceAtLeast(0) },
        { it.fileName },
        { it.song.id },
        { it.stableKey },
    )

    private val AUDIOBOOK_COMPARATOR = compareBy<Audiobook>(
        { it.title.lowercase(Locale.ROOT) },
        { it.author.lowercase(Locale.ROOT) },
        { it.stableKey },
    )
}
