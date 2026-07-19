package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import elovaire.music.droidbeauty.app.core.runSuspendCatching
import elovaire.music.droidbeauty.app.data.artwork.isArtworkBoundsSafe
import java.net.URLEncoder

internal class AlbumArtworkResolver(
    private val tidalArtworkProvider: TidalArtworkProvider,
    private val coverArtArchiveClient: CoverArtArchiveClient,
    private val embeddedArtworkProvider: EmbeddedArtworkProvider,
) {
    suspend fun resolve(match: ResolvedAlbumMatch): AlbumArtworkResult? {
        return tidalArtworkProvider.findArtwork(match).getOrNull()
            ?.takeIf(AlbumArtworkResult::isAcceptableForEmbedding)
            ?: coverArtArchiveClient.getFrontCoverArt(match.release.id).getOrNull()
                ?.takeIf(AlbumArtworkResult::isAcceptableForEmbedding)
            ?: embeddedArtworkProvider.findBestLocalArtwork(match.trackMatches.map { it.song })
    }
}

internal class TidalArtworkProvider : AlbumArtworkProvider {
    override suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?> = runSuspendCatching {
        val relationUrls = match.release.relatedUrls.filter { url ->
            url.contains("tidal.com", ignoreCase = true)
        }
        val pages = buildList {
            addAll(relationUrls)
            if (relationUrls.isEmpty()) {
                val query = listOf(match.release.albumArtist, match.release.title)
                    .filter(String::isNotBlank)
                    .joinToString(" ")
                if (query.isNotBlank()) {
                    val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
                    add("https://listen.tidal.com/search/albums?q=$encoded")
                    add("https://tidal.com/browse/search?query=$encoded")
                }
            }
        }.distinct()

        pages.forEach { pageUrl ->
            val html = runSuspendCatching { getText(pageUrl, "text/html,*/*;q=0.8") }.getOrNull()
                ?: return@forEach
            if (relationUrls.isEmpty() && !htmlMatchesRelease(html, match.release)) return@forEach
            val imageUrl = TIDAL_ARTWORK_REGEX.find(html)?.groupValues?.getOrNull(1)
                ?.replace("\\/", "/")
                ?.replace("&amp;", "&")
                ?.replace(TIDAL_IMAGE_SIZE_REGEX, "/1280x1280")
                ?: return@forEach
            val bytes = runSuspendCatching { getBytes(imageUrl) }.getOrNull() ?: return@forEach
            return@runSuspendCatching bytes.toArtworkResult(ArtworkSource.Tidal)
        }
        null
    }

    private fun htmlMatchesRelease(html: String, release: MusicBrainzRelease): Boolean {
        val normalizedHtml = normalizeRemoteIdentity(html)
        val normalizedTitle = normalizeRemoteIdentity(release.title)
        val normalizedArtist = normalizeRemoteIdentity(release.albumArtist)
        return normalizedTitle.length >= 3 && normalizedTitle in normalizedHtml &&
            (normalizedArtist.isBlank() || normalizedArtist in normalizedHtml)
    }

    private companion object {
        val TIDAL_ARTWORK_REGEX = Regex(
            """(https:\\?/\\?/resources\.tidal\.com/images/[a-z0-9/]+/(?:1280x1280|750x750|640x640)\.jpg)""",
            RegexOption.IGNORE_CASE,
        )
        val TIDAL_IMAGE_SIZE_REGEX = Regex("""/\d{2,4}x\d{2,4}(?=\.jpg)""")
    }
}

internal class CoverArtArchiveClient {
    suspend fun getFrontCoverArt(releaseMbid: String): Result<AlbumArtworkResult?> = runSuspendCatching {
        val bytes = getBytes("https://coverartarchive.org/release/$releaseMbid/front-1200")
        bytes.toArtworkResult(ArtworkSource.CoverArtArchive)
    }
}

internal class EmbeddedArtworkProvider(context: Context) {
    private val appContext = context.applicationContext

    fun findBestLocalArtwork(songs: List<elovaire.music.droidbeauty.app.domain.model.Song>): AlbumArtworkResult? {
        return songs.asSequence().mapNotNull { song ->
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(appContext, song.uri)
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
