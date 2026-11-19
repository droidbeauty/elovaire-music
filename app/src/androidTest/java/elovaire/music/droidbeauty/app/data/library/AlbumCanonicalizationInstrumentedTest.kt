package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumCanonicalizationInstrumentedTest {
    @Test
    fun metadataPatchPublishesEverySongMovedByAlbumCollision() {
        val songs = List(1_000) { index ->
            val item = song(index, collisions = false)
            if (index in 20..39) {
                item.copy(uri = Uri.parse("content://media/1234-5678/audio/media/${index + 1}"))
            } else {
                item
            }
        }
        val initial = LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs)
        val current = LibraryContentState(initial.songs, initial.albums, contentRevision = initial.contentRevision)
        val edited = songs.first().copy(albumId = songs[20].albumId, album = songs[20].album)
        val patch = LibrarySnapshotPublisher({}, { current }).patchSongs(listOf(edited), emptySet(), emptySet())
        val expected = LibrarySnapshotAssembler.assembleDeduplicatedSongs(listOf(edited) + songs.drop(1))

        assertEquals(expected.songs, patch.state.songs)
        assertEquals(expected.albums.associateBy { it.id }, patch.state.albums.associateBy { it.id })
        assertEquals((listOf(1L) + (21L..40L)).toSet(), patch.changeSet.updated.map { it.after.id }.toSet())
    }

    @Test
    fun canonicalizationPreservesOrderAndIdentityAtLibraryScale() {
        for (size in listOf(1_000, 10_000, 50_000)) {
            for (collisions in listOf(false, true)) {
                qualify(size, collisions)
            }
        }
    }

    private fun qualify(size: Int, collisions: Boolean) {
        val songs = List(size) { index -> song(index, collisions) }
        val expected = LibrarySnapshotAssembler.canonicalizeAlbumIds(songs)
        if (!collisions) assertSame(songs, expected)
        assertEquals(songs.map(Song::id), expected.map(Song::id))
        assertEquals(size / 20 * if (collisions) 2 else 1, expected.map(Song::albumId).distinct().size)
        val expectedIds = expected.map(Song::albumId)
        repeat(2) { LibrarySnapshotAssembler.canonicalizeAlbumIds(songs) }
        val timings = mutableListOf<Double>()
        val allocations = mutableListOf<Long>()
        repeat(5) {
            val allocatedBefore = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
            lateinit var result: List<Song>
            timings += measureNanoTime { result = LibrarySnapshotAssembler.canonicalizeAlbumIds(songs) } / 1_000_000.0
            allocations += Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - allocatedBefore
            assertEquals(expectedIds, result.map(Song::albumId))
            songs.indices.filter { !collisions || it % 2 == 0 }.forEach { assertSame(songs[it], result[it]) }
        }
        Log.i("AlbumCanonicalization", "songs=$size collisions=$collisions samplesMs=$timings allocatedBytes=$allocations")
    }

    private fun song(index: Int, collisions: Boolean): Song {
        val volume = if (collisions && index % 2 != 0) "1234-5678" else "external_primary"
        return Song(
            id = index.toLong() + 1,
            title = "Track $index",
            isExplicit = false,
            artist = "Artist",
            album = "Album ${index / 20}",
            releaseYear = null,
            genre = "Genre",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "$index.mp3",
            albumId = index.toLong() / 20 + 1,
            durationMs = 180_000L,
            trackNumber = index % 20 + 1,
            discNumber = 1,
            dateAddedSeconds = 1L,
            uri = Uri.parse("content://media/$volume/audio/media/${index + 1}"),
            artUri = null,
        )
    }
}
