package elovaire.music.droidbeauty.app.data.artist

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.core.AppClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ArtistImageRepositoryTest {
    private val repository = ArtistImageRepository()

    @Test
    fun backdropUsesBestLocalAlbumArtwork() = runBlocking {
        val art = TestUri("content://art/album")
        val state = repository.backdropState(
            artistName = "Artist",
            songs = listOf(song(artUri = TestUri("content://art/song"))),
            albums = listOf(album(artUri = art, songCount = 2)),
        ).first()

        assertSame(art, (state as ArtistBackdropState.Fallback).localArtworkUri)
        assertEquals("artist", state.artistKey)
    }

    @Test
    fun backdropFallsBackToLocalSongArtwork() = runBlocking {
        val art = TestUri("content://art/song")
        val state = repository.backdropState("Artist", listOf(song(artUri = art)), emptyList()).first()

        assertSame(art, (state as ArtistBackdropState.Fallback).localArtworkUri)
        assertEquals("artist", state.artistKey)
    }

    @Test
    fun backdropWithoutArtworkRemainsLocalFallback() = runBlocking {
        val state = repository.backdropState(" Artist ", listOf(song(artUri = null)), emptyList()).first()

        assertEquals("artist", (state as ArtistBackdropState.Fallback).artistKey)
        assertNull(state.localArtworkUri)
    }

    @Test
    fun successfulRemoteLookupIsCachedForNormalizedArtistKey() = runBlocking {
        val calls = AtomicInteger()
        val remoteUri = TestUri("https://example.test/artist.jpg")
        val repository = ArtistImageRepository(
            ArtistImageClient {
                calls.incrementAndGet()
                ArtistImageLookup.Found(remoteUri)
            },
        )

        val first = repository.imageState("Artist", null).last()
        assertEquals(1, calls.get())
        val second = repository.imageState(" Artist ", null).last()

        assertEquals(1, calls.get())
        assertSame(remoteUri, (first as ArtistBackdropState.Fallback).remoteArtworkUri)
        assertSame(remoteUri, (second as ArtistBackdropState.Fallback).remoteArtworkUri)
    }

    @Test
    fun transientRemoteFailureIsNotNegativeCached() = runBlocking {
        val calls = AtomicInteger()
        val repository = ArtistImageRepository(
            ArtistImageClient {
                calls.incrementAndGet()
                ArtistImageLookup.Failed
            },
        )

        repository.imageState("Artist", null).last()
        repository.imageState("Artist", null).last()

        assertEquals(2, calls.get())
    }

    @Test
    fun concurrentRemoteLookupsShareOneRequest() = runBlocking {
        val calls = AtomicInteger()
        val requestStarted = CompletableDeferred<Unit>()
        val releaseRequest = CompletableDeferred<Unit>()
        val remoteUri = TestUri("https://example.test/artist.jpg")
        val repository = ArtistImageRepository(
            ArtistImageClient {
                calls.incrementAndGet()
                requestStarted.complete(Unit)
                releaseRequest.await()
                ArtistImageLookup.Found(remoteUri)
            },
        )

        val first = async { repository.imageState("Artist", null).last() }
        requestStarted.await()
        val second = async { repository.imageState(" artist ", null).last() }
        releaseRequest.complete(Unit)

        assertSame(remoteUri, (first.await() as ArtistBackdropState.Fallback).remoteArtworkUri)
        assertSame(remoteUri, (second.await() as ArtistBackdropState.Fallback).remoteArtworkUri)
        assertEquals(1, calls.get())
    }

    @Test
    fun wallClockRollbackExpiresNegativeCache() = runBlocking {
        val calls = AtomicInteger()
        val clock = MutableClock(wallTimeMs = 2L * DAY_MS)
        val repository = ArtistImageRepository(
            client = ArtistImageClient {
                calls.incrementAndGet()
                ArtistImageLookup.NotFound
            },
            clock = clock,
        )

        repository.imageState("Artist", null).last()
        repository.imageState("Artist", null).last()
        assertEquals(1, calls.get())

        clock.wallTimeMs -= DAY_MS
        repository.imageState("Artist", null).last()
        assertEquals(2, calls.get())
    }

    private fun album(artUri: android.net.Uri?, songCount: Int) = Album(
        id = 1L,
        title = "Album",
        artist = "Artist",
        artUri = artUri,
        songCount = songCount,
        durationMs = 1_000L,
        songs = emptyList(),
    )

    private fun song(artUri: android.net.Uri?) = Song(
        id = 1L,
        title = "Track",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "track.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("content://media/1"),
        artUri = artUri,
    )

    private class MutableClock(
        var wallTimeMs: Long,
    ) : AppClock {
        override fun wallTimeMs(): Long = wallTimeMs
        override fun elapsedTimeMs(): Long = 0L
    }

    private companion object {
        const val DAY_MS = 24L * 60L * 60L * 1_000L
    }
}
