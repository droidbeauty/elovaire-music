package elovaire.music.droidbeauty.app.data.library

import android.content.ContextWrapper
import android.net.Uri
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.UUID
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibrarySnapshotSongsInstrumentedTest {
    @Test
    fun songOnlyLoadMatchesFullSnapshotIncludingAlbumIdCollisions() {
        withStore { store, _ ->
            val songs = (1L..1_000L).map(::song)
            store.save(LibrarySnapshot(songs, emptyList()), "test")
            val songOnly = checkNotNull(store.loadSongs())
            val full = checkNotNull(store.load()).snapshot.songs
            assertEquals(full, songOnly)
            assertTrue(songOnly.map(Song::albumId).distinct().size > 20)

            val times = Array(2) { mutableListOf<Long>() }
            val allocations = Array(2) { mutableListOf<Long>() }
            repeat(5) { sample ->
                for (variant in if (sample % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                    val before = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
                    val start = SystemClock.elapsedRealtimeNanos()
                    val result = if (variant == 0) store.load()?.snapshot?.songs else store.loadSongs()
                    val elapsed = SystemClock.elapsedRealtimeNanos() - start
                    val allocated = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong() - before
                    assertEquals(full, result)
                    if (sample > 0) {
                        times[variant] += elapsed
                        allocations[variant] += allocated
                    }
                }
            }
            Log.i("SnapshotSongsTest", "fullNs=${times[0]} songsNs=${times[1]} fullBytes=${allocations[0]} songsBytes=${allocations[1]}")
        }
    }

    @Test
    fun songOnlyLoadRejectsCorruptedSignatures() {
        withStore { store, directory ->
            store.save(LibrarySnapshot(listOf(song(1L)), emptyList()), "test")
            val file = directory.resolve("library_snapshot_v7.json")
            val corrupted = JSONObject(file.readText()).put("songCount", 99)
            file.writeText(corrupted.toString())

            assertNull(store.loadSongs())
            assertFalse(file.exists())
        }
    }

    @Test
    fun songOnlyLoadStillRepairsDuplicateSources() {
        withStore { store, _ ->
            val first = song(1L)
            val duplicate = first.copy(id = 2L)
            store.save(LibrarySnapshot(listOf(first, duplicate), emptyList()), "test")

            val songOnly = checkNotNull(store.loadSongs())
            assertEquals(1, songOnly.size)
            assertEquals(songOnly, store.load()?.snapshot?.songs)
        }
    }

    @Test
    fun songOnlyLoadDoesNotReuseAnOutdatedSaveRevision() {
        withStore { firstStore, directory ->
            val target = InstrumentationRegistry.getInstrumentation().targetContext
            val context = object : ContextWrapper(target) {
                override fun getFilesDir(): File = directory
            }
            val first = LibrarySnapshot(listOf(song(1L)), emptyList())
            val second = LibrarySnapshot(listOf(song(2L)), emptyList())
            firstStore.save(first, "test")
            LibrarySnapshotStore(context).save(second, "test")
            assertEquals(listOf(2L), firstStore.loadSongs()?.map(Song::id))

            firstStore.save(first, "test")

            assertEquals(listOf(1L), firstStore.loadSongs()?.map(Song::id))
        }
    }

    @Test
    fun legacyAndUnknownMediaKindsRetainClassification() {
        withStore { store, directory ->
            val songs = listOf(
                song(1L), song(2L), song(3L).copy(fileName = "3.m4b"),
                song(4L).copy(mediaKind = AudioMediaKind.Audiobook),
            )
            store.save(LibrarySnapshot(songs, emptyList()), "test")
            val file = directory.resolve("library_snapshot_v7.json")
            val root = JSONObject(file.readText()).put("version", 7)
            val rows = root.getJSONArray("songs")
            rows.getJSONObject(0).remove("mediaKind")
            rows.getJSONObject(1).put("mediaKind", "unknown")
            rows.getJSONObject(2).put("mediaKind", "Music")
            file.writeText(root.toString())

            val loaded = checkNotNull(store.loadSongs())
            assertEquals(
                listOf(AudioMediaKind.Music, AudioMediaKind.Music, AudioMediaKind.Audiobook, AudioMediaKind.Audiobook),
                loaded.map(Song::mediaKind),
            )
            assertEquals(loaded, store.load()?.snapshot?.songs)
        }
    }

    private inline fun withStore(block: (LibrarySnapshotStore, File) -> Unit) {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(target.cacheDir, "snapshot-test-${UUID.randomUUID()}")
        check(directory.mkdirs())
        val context = object : ContextWrapper(target) {
            override fun getFilesDir(): File = directory
        }
        try {
            block(LibrarySnapshotStore(context), directory)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun song(id: Long) = Song(
        id = id, title = "Song $id", isExplicit = false, artist = "Artist $id", album = "Album $id",
        releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null,
        fileName = "$id.mp3", albumId = id % 20, durationMs = 1_000L, trackNumber = 1,
        discNumber = 1, dateAddedSeconds = id,
        uri = Uri.parse("content://media/${if (id / 20 % 2 == 0L) "external_primary" else "1234-5678"}/audio/media/$id"),
        artUri = null,
    )
}
