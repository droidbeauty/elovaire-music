package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import elovaire.music.droidbeauty.app.core.runSuspendCatching
import elovaire.music.droidbeauty.app.data.artwork.isArtworkBoundsSafe

internal class AlbumArtworkResolver(
    private val coverArtArchiveProvider: AlbumArtworkProvider,
    private val embeddedArtworkProvider: AlbumArtworkProvider,
) {
    suspend fun resolve(match: ResolvedAlbumMatch): AlbumArtworkResult? {
        return coverArtArchiveProvider.findArtwork(match).getOrNull()
            ?.takeIf(AlbumArtworkResult::isAcceptableForEmbedding)
            ?: embeddedArtworkProvider.findArtwork(match).getOrNull()
                ?.takeIf(AlbumArtworkResult::isAcceptableForEmbedding)
    }
}

internal class CoverArtArchiveProvider : AlbumArtworkProvider {
    override suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?> = runSuspendCatching {
        val releaseUrl = "https://coverartarchive.org/release/${match.release.id}"
        val bytes = runSuspendCatching {
            getBytes(
                url = "$releaseUrl/front-1200",
                allowedRedirectHostSuffixes = COVER_ART_REDIRECT_HOSTS,
            )
        }.getOrNull() ?: getBytes(
            url = "$releaseUrl/front",
            allowedRedirectHostSuffixes = COVER_ART_REDIRECT_HOSTS,
        )
        bytes.toArtworkResult(ArtworkSource.CoverArtArchive)
    }

    private companion object {
        val COVER_ART_REDIRECT_HOSTS = setOf("archive.org")
    }
}

internal class EmbeddedArtworkProvider(context: Context) : AlbumArtworkProvider {
    private val appContext = context.applicationContext

    override suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?> = runSuspendCatching {
        match.trackMatches.asSequence().mapNotNull { trackMatch ->
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(appContext, trackMatch.song.uri)
                retriever.embeddedPicture?.toArtworkResult(ArtworkSource.Embedded)
            } catch (_: Exception) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }.maxByOrNull { artwork -> (artwork.width ?: 0).toLong() * (artwork.height ?: 0) }
    }
}

private fun ByteArray.toArtworkResult(source: ArtworkSource): AlbumArtworkResult? {
    if (isEmpty() || size > MAX_ARTWORK_BYTES) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    if (!isArtworkBoundsSafe(bounds.outWidth, bounds.outHeight)) return null
    return AlbumArtworkResult(
        bytes = this,
        width = bounds.outWidth,
        height = bounds.outHeight,
        source = source,
    )
}

private const val MAX_ARTWORK_BYTES = 16 * 1024 * 1024
