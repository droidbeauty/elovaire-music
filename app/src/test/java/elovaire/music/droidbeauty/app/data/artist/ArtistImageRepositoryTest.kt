package elovaire.music.droidbeauty.app.data.artist

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.core.AppClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ArtistImageRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val requestScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repositories = mutableListOf<ArtistImageRepository>()
    private val repository = newRepository()

    @After
    fun tearDown() {
        repositories.forEach(ArtistImageRepository::release)
        requestScope.cancel()
    }

    @Test
    fun backdropUsesBestLocalAlbumArtwork() = runBlocking {
        val art = TestUri("content://art/album")
        val state = repository.backdropState(
            artistName = "Artist",
            songs = listOf(song(artUri = TestUri("content://art/song"))),
            albums = listOf(
                album(artUri = TestUri("content://art/less-preferred"), songCount = 1),
                album(artUri = art, songCount = 2),
            ),
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
        val repository = newRepository(
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
        val repository = newRepository(
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
    fun unexpectedRemoteFailureCompletesSharedLookup() = runBlocking {
        val calls = AtomicInteger()
        val repository = newRepository(
            ArtistImageClient {
                calls.incrementAndGet()
                throw UnsupportedOperationException("test failure")
            },
        )

        withTimeout(1_000L) {
            repository.imageState("Artist", null).last()
        }

        assertEquals(1, calls.get())
    }

    @Test
    fun concurrentRemoteLookupsShareOneRequest() = runBlocking {
        val calls = AtomicInteger()
        val requestStarted = CompletableDeferred<Unit>()
        val releaseRequest = CompletableDeferred<Unit>()
        val remoteUri = TestUri("https://example.test/artist.jpg")
        val repository = newRepository(
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
    fun cancelledOwnerDoesNotCancelSharedLookupForAnotherCaller() = runBlocking {
        val calls = AtomicInteger()
        val requestStarted = CompletableDeferred<Unit>()
        val releaseRequest = CompletableDeferred<Unit>()
        val remoteUri = TestUri("https://example.test/artist.jpg")
        val repository = newRepository(
            ArtistImageClient {
                calls.incrementAndGet()
                requestStarted.complete(Unit)
                releaseRequest.await()
                ArtistImageLookup.Found(remoteUri)
            },
        )

        val first = async { repository.imageState("Artist", null).last() }
        requestStarted.await()
        first.cancelAndJoin()
        val second = async { repository.imageState("artist", null).last() }
        releaseRequest.complete(Unit)

        assertSame(remoteUri, (second.await() as ArtistBackdropState.Fallback).remoteArtworkUri)
        assertEquals(1, calls.get())
    }

    @Test
    fun releasingRepositoryCancelsOwnedLookup() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val requestCancelled = CompletableDeferred<Unit>()
        val repository = newRepository(
            ArtistImageClient {
                requestStarted.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    requestCancelled.complete(Unit)
                }
            },
        )

        val request = async { repository.imageState("Artist", null).last() }
        requestStarted.await()
        repository.release()

        withTimeout(1_000L) { requestCancelled.await() }
        request.cancelAndJoin()
    }

    @Test
    fun wallClockRollbackExpiresNegativeCache() = runBlocking {
        val calls = AtomicInteger()
        val clock = MutableClock(wallTimeMs = 2L * DAY_MS)
        val repository = newRepository(
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

    @Test
    fun diskCacheTrimKeepsAnotherRequestTemporaryFile() {
        val directory = temporaryFolder.newFolder("artist-images")
        val keep = File(directory, "keep.img").apply { writeBytes(byteArrayOf(1)) }
        val activeDownload = File(directory, "active.tmp").apply { writeBytes(byteArrayOf(2)) }

        repository.trimDiskArtworkCache(directory, keep)

        assertTrue(activeDownload.exists())
    }

    private fun newRepository(
        client: ArtistImageClient = ArtistImageClient { ArtistImageLookup.NotFound },
        clock: AppClock = MutableClock(0L),
    ): ArtistImageRepository = ArtistImageRepository(
        client = client,
        scope = requestScope,
        clock = clock,
    ).also(repositories::add)

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
