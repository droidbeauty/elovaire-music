package elovaire.music.droidbeauty.app.data.tags.matching

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AlbumArtworkResolverTest {
    @Test
    fun usesCoverArtArchiveBeforeEmbeddedArtwork() = runBlocking {
        val coverArt = artwork(ArtworkSource.CoverArtArchive)
        val embeddedArt = artwork(ArtworkSource.Embedded)
        val coverArtArchive = FakeArtworkProvider(Result.success(coverArt))
        val embedded = FakeArtworkProvider(Result.success(embeddedArt))

        val result = AlbumArtworkResolver(coverArtArchive, embedded).resolve(albumMatch())

        assertSame(coverArt, result)
        assertEquals(1, coverArtArchive.calls)
        assertEquals(0, embedded.calls)
    }

    @Test
    fun fallsBackToEmbeddedArtworkWhenArchiveHasNoUsableCover() = runBlocking {
        val embeddedArt = artwork(ArtworkSource.Embedded)
        val coverArtArchive = FakeArtworkProvider(Result.success(null))
        val embedded = FakeArtworkProvider(Result.success(embeddedArt))

        val result = AlbumArtworkResolver(coverArtArchive, embedded).resolve(albumMatch())

        assertSame(embeddedArt, result)
        assertEquals(1, coverArtArchive.calls)
        assertEquals(1, embedded.calls)
    }

    private fun albumMatch() = ResolvedAlbumMatch(
        release = MusicBrainzRelease(
            id = "release-id",
            title = "Album",
            albumArtist = "Artist",
            releaseYear = 2026,
            tracks = emptyList(),
            relatedUrls = emptyList(),
        ),
        trackMatches = emptyList(),
        confidence = MatchConfidence.High,
        score = 100,
    )

    private fun artwork(source: ArtworkSource) = AlbumArtworkResult(
        bytes = byteArrayOf(1),
        width = 1_200,
        height = 1_200,
        source = source,
    )

    private class FakeArtworkProvider(
        private val result: Result<AlbumArtworkResult?>,
    ) : AlbumArtworkProvider {
        var calls = 0
            private set

        override suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?> {
            calls += 1
            return result
        }
    }
}
