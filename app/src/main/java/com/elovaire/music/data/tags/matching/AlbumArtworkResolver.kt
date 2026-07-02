package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.net.URLEncoder
import java.util.Locale

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
    override suspend fun findArtwork(match: ResolvedAlbumMatch): Result<AlbumArtworkResult?> = runCatching {
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
            val html = runCatching { getTextContent(pageUrl, "text/html,*/*;q=0.8") }.getOrNull()
                ?: return@forEach
            if (relationUrls.isEmpty() && !htmlMatchesRelease(html, match.release)) return@forEach
            val imageUrl = TIDAL_ARTWORK_REGEX.find(html)?.groupValues?.getOrNull(1)
                ?.replace("\\/", "/")
                ?.replace("&amp;", "&")
                ?.replace(Regex("""/\d{2,4}x\d{2,4}(?=\.jpg)"""), "/1280x1280")
                ?: return@forEach
            val bytes = runCatching { getBytes(imageUrl) }.getOrNull() ?: return@forEach
            return@runCatching bytes.toArtworkResult(ArtworkSource.Tidal)
        }
        null
    }

    private fun htmlMatchesRelease(html: String, release: MusicBrainzRelease): Boolean {
        val normalizedHtml = normalize(html)
        val normalizedTitle = normalize(release.title)
        val normalizedArtist = normalize(release.albumArtist)
        return normalizedTitle.length >= 3 && normalizedTitle in normalizedHtml &&
            (normalizedArtist.isBlank() || normalizedArtist in normalizedHtml)
    }

    private companion object {
        val TIDAL_ARTWORK_REGEX = Regex(
            """(https:\\?/\\?/resources\.tidal\.com/images/[a-z0-9/]+/(?:1280x1280|750x750|640x640)\.jpg)""",
            RegexOption.IGNORE_CASE,
        )
    }
}

internal class CoverArtArchiveClient {
    suspend fun getFrontCoverArt(releaseMbid: String): Result<AlbumArtworkResult?> = runCatching {
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
            } catch (_: Throwable) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }.maxByOrNull { artwork -> (artwork.width ?: 0) * (artwork.height ?: 0) }
    }
}

private fun getTextContent(url: String, accept: String): String {
    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
    connection.connectTimeout = 8_000
    connection.readTimeout = 8_000
    connection.requestMethod = "GET"
    connection.instanceFollowRedirects = true
    connection.setRequestProperty("Accept", accept)
    connection.setRequestProperty("User-Agent", "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)")
    return try {
        connection.inputStream.use { input ->
            readBytesBounded(input, MAX_HTML_BYTES, connection.contentLengthLong)
                .toString(Charsets.UTF_8)
        }
    } finally {
        connection.disconnect()
    }
}

private fun ByteArray.toArtworkResult(source: ArtworkSource): AlbumArtworkResult? {
    if (isEmpty() || size > MAX_ARTWORK_BYTES) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    return AlbumArtworkResult(
        bytes = this,
        width = bounds.outWidth,
        height = bounds.outHeight,
        source = source,
    )
}

private fun normalize(value: String): String {
    return value.lowercase(Locale.ROOT)
        .replace(Regex("""[^\p{L}\p{N}]+"""), " ")
        .trim()
        .replace(Regex("""\s+"""), " ")
}

private const val MAX_ARTWORK_BYTES = 16 * 1024 * 1024
private const val MAX_HTML_BYTES = 1 * 1024 * 1024
