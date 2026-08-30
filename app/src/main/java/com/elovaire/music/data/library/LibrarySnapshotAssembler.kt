package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

internal object LibrarySnapshotAssembler {
    fun assemble(songs: List<Song>): LibrarySnapshot {
        val canonicalSongs = canonicalizeAlbumIds(
            LibrarySongDuplicateResolver.dedupeLoadedSnapshotSongs(songs),
        )
        return LibrarySnapshot(
            songs = canonicalSongs,
            albums = buildAlbumsFromSongs(canonicalSongs),
            contentRevision = librarySongsContentRevision(canonicalSongs),
        )
    }

    /**
     * Album IDs from external providers are only source-local. Keep the existing ID when it is
     * unambiguous, but split colliding source groups before they reach routes or persistence.
     */
    internal fun canonicalizeAlbumIds(songs: List<Song>): List<Song> {
        if (songs.size < 2) return songs
        val groups = linkedMapOf<AlbumGroupKey, MutableList<Song>>()
        songs.forEach { song ->
            groups.getOrPut(albumGroupKey(song), ::mutableListOf).add(song)
        }
        val groupsByOriginalId = groups.keys.groupBy(AlbumGroupKey::originalId)
        val collidingGroups = groupsByOriginalId.values.filter { it.size > 1 }
        if (collidingGroups.isEmpty()) return songs

        val usedIds = songs.mapTo(hashSetOf(), Song::albumId)
        val replacementIds = hashMapOf<AlbumGroupKey, Long>()
        collidingGroups.forEach { sameIdGroups ->
            sameIdGroups
                .sortedWith(
                    compareBy<AlbumGroupKey>(
                        { albumSourcePriority(it.sourceNamespace) },
                        { it.sourceNamespace },
                        { it.discriminator },
                    ),
                )
                .forEachIndexed { index, group ->
                    if (index == 0) {
                        replacementIds[group] = group.originalId
                    } else {
                        var attempt = 0
                        var replacement = stableNegativeAlbumId(group.stableKey, attempt)
                        while (replacement == 0L || replacement in usedIds) {
                            attempt += 1
                            replacement = stableNegativeAlbumId(group.stableKey, attempt)
                        }
                        usedIds += replacement
                        replacementIds[group] = replacement
                    }
                }
        }
        return songs.map { song ->
            val group = albumGroupKey(song)
            val replacement = replacementIds[group] ?: return@map song
            if (replacement == song.albumId) song else song.copy(albumId = replacement)
        }
    }

    private fun albumGroupKey(song: Song): AlbumGroupKey {
        val source = MediaIdentityResolver.resolve(song)
        val sourceNamespace = when (source) {
            is MediaSourceIdentity.MediaStoreItem -> "mediastore:${source.volumeName}"
            is MediaSourceIdentity.SafDocument ->
                "saf:${source.authority}:${source.treeId.orEmpty()}"
            is MediaSourceIdentity.NetworkFile -> "network:${source.sourceId}"
            is MediaSourceIdentity.DirectFile -> "file"
            null -> fallbackUriSourceNamespace(song.uri)
        }
        val discriminator = if (source is MediaSourceIdentity.MediaStoreItem && song.albumId < 0L) {
            listOf(song.albumArtist.orEmpty(), song.artist, song.album)
                .joinToString("\u0000") { it.trim().lowercase(Locale.ROOT) }
        } else {
            ""
        }
        return AlbumGroupKey(
            originalId = song.albumId,
            sourceNamespace = sourceNamespace,
            discriminator = discriminator,
        )
    }

    private fun albumSourcePriority(sourceNamespace: String): Int {
        return when {
            sourceNamespace == "mediastore:${android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY}" -> 0
            sourceNamespace.startsWith("mediastore:") -> 1
            else -> 2
        }
    }

    private fun fallbackUriSourceNamespace(uri: android.net.Uri): String {
        val raw = uri.toString().trim()
        val mediaPrefix = "content://media/"
        if (raw.startsWith(mediaPrefix, ignoreCase = true)) {
            val volume = raw.substring(mediaPrefix.length)
                .substringBefore('/')
                .trim()
                .lowercase(Locale.ROOT)
                .ifBlank { "external" }
            return "mediastore:$volume"
        }
        val authorityPart = raw.substringAfter("://", "")
        val authorityEnd = authorityPart.indexOfAny(charArrayOf('/', '?', '#'))
        val authority = authorityPart
            .substring(0, if (authorityEnd >= 0) authorityEnd else authorityPart.length)
            .lowercase(Locale.ROOT)
            .ifBlank { uri.authority.orEmpty().lowercase(Locale.ROOT) }
        return "uri:$authority"
    }

    private fun stableNegativeAlbumId(stableKey: String, attempt: Int): Long {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$stableKey#$attempt".toByteArray(StandardCharsets.UTF_8))
        val positive = digest.take(8).fold(0L) { value, byte ->
            (value shl 8) or (byte.toLong() and 0xffL)
        } and Long.MAX_VALUE
        return -positive.coerceAtLeast(1L)
    }

    private data class AlbumGroupKey(
        val originalId: Long,
        val sourceNamespace: String,
        val discriminator: String,
    ) {
        val stableKey: String
            get() = "$sourceNamespace\u0000$originalId\u0000$discriminator"
    }
}
