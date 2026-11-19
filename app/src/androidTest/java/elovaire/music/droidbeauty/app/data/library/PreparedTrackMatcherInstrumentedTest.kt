package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreparedTrackMatcherInstrumentedTest {
    @Test
    fun preparedLookupsHandleLargeDistinctCatalogs() {
        val songs = (1L..3_000L).map { id ->
            Song(
                id = id, title = "Song $id", isExplicit = false, artist = "Artist",
                album = "Album", releaseYear = null, genre = "", audioFormat = "MP3",
                audioQuality = null, fileName = "$id.mp3", albumId = 1L, durationMs = 1_000L,
                trackNumber = 0, discNumber = 0, dateAddedSeconds = 0L,
                uri = Uri.parse("content://media/external/audio/media/$id"), artUri = null,
            )
        }
        val identities = songs.take(1_200).map {
            MediaIdentityResolver.trackMatchIdentity(it, includeSourceStableKey = false)
        }
        val matcher = MediaIdentityResolver.prepareTrackMatcher(songs)
        val times = mutableListOf<Long>()
        repeat(4) { sample ->
            val start = SystemClock.elapsedRealtimeNanos()
            val matches = identities.map(matcher::resolve)
            if (sample > 0) times += SystemClock.elapsedRealtimeNanos() - start
            assertEquals(songs.take(1_200), matches.map { it.song })
        }
        Log.i("TrackMatcherTest", "preparedLookupNs=$times")
    }

    @Test
    fun indexedCatalogLookupsPreserveUnpreparedResults() {
        val songs = (1L..600L).map { id ->
            Song(
                id = id, title = "Song ${id % 400}", isExplicit = false, artist = "Artist ${id % 13}",
                album = "Album ${id % 17}", releaseYear = null, genre = "", audioFormat = "MP3",
                audioQuality = null, fileName = "$id.mp3", albumId = 1L, durationMs = 1_000L,
                trackNumber = 0, discNumber = 0, dateAddedSeconds = 0L,
                uri = Uri.parse("content://media/external/audio/media/$id"), artUri = null,
            )
        }
        val identities = songs.take(300).map {
            MediaIdentityResolver.trackMatchIdentity(it, includeSourceStableKey = false)
        }
        val matcher = MediaIdentityResolver.prepareTrackMatcher(songs)
        var start = SystemClock.elapsedRealtimeNanos()
        val baseline = identities.map { MediaIdentityResolver.resolveTrackMatch(it, songs) }
        val baselineTime = SystemClock.elapsedRealtimeNanos() - start
        start = SystemClock.elapsedRealtimeNanos()
        val indexed = identities.map(matcher::resolve)
        val indexedTime = SystemClock.elapsedRealtimeNanos() - start
        assertEquals(baseline, indexed)
        Log.i("TrackMatcherTest", "unpreparedNs=$baselineTime indexedNs=$indexedTime")
    }
}
