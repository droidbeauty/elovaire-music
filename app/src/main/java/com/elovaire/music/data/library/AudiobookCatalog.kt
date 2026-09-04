package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

internal object AudiobookCatalog {
    fun build(songs: List<Song>): List<Audiobook> {
        return songs
            .asSequence()
            .filter { it.mediaKind == AudioMediaKind.Audiobook }
            .groupBy(::groupKey)
            .values
            .mapNotNull { parts ->
                val ordered = parts.sortedWith(
                    compareBy<Song>({ it.discNumber.coerceAtLeast(1) }, { it.trackNumber.coerceAtLeast(0) }, { it.fileName.lowercase(Locale.ROOT) }, { it.id }),
                ).distinctBy(Song::id)
                val first = ordered.firstOrNull() ?: return@mapNotNull null
                val author = ordered
                    .asSequence()
                    .mapNotNull { it.albumArtist?.trim()?.takeIf(String::isNotBlank) ?: it.artist.trim().takeIf(String::isNotBlank) }
                    .firstOrNull()
                    ?: "Unknown Author"
                Audiobook(
                    stableKey = groupKey(first),
                    title = bookTitle(first),
                    author = author,
                    artUri = ordered.firstNotNullOfOrNull(Song::artUri),
                    durationMs = ordered.sumOf { it.durationMs.coerceAtLeast(0L) },
                    parts = ordered.mapIndexed { index, song -> AudiobookPart(song, index + 1) },
                )
            }
            .sortedWith(compareBy({ it.title.lowercase(Locale.ROOT) }, { it.author.lowercase(Locale.ROOT) }, { it.stableKey }))
            .toList()
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

    fun stableId(book: Audiobook): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(book.stableKey.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }
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
}
