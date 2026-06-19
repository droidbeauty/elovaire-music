package elovaire.music.droidbeauty.app.data.tags

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import elovaire.music.droidbeauty.app.core.queryMediaStoreFilePath
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import org.json.JSONObject

internal data class EditableAlbumTrack(
    val songId: Long,
    val title: String,
    val artist: String,
    val trackNumber: Int,
    val discNumber: Int,
)

internal data class AlbumTagEditRequest(
    val album: Album,
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: Int?,
    val coverArtUri: Uri?,
    val coverArtBytes: ByteArray? = null,
    val tracks: List<EditableAlbumTrack>,
)

internal data class AlbumTagMatchSuggestion(
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: Int?,
    val coverArtBytes: ByteArray?,
    val tracks: List<EditableAlbumTrack>,
)

internal data class TagEditApplyResult(
    val editedSongIds: List<Long>,
    val editedUris: List<Uri>,
    val editedFilePaths: List<String>,
    val artworkChanged: Boolean,
    val failures: List<TagEditFailure> = emptyList(),
)

internal data class TagEditFailure(
    val songId: Long,
    val fileName: String,
    val reason: String,
)

internal class AlbumTagEditorService(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver

    suspend fun findBestOnlineMatch(album: Album): AlbumTagMatchSuggestion? = withContext(Dispatchers.IO) {
        val bestCandidate = searchBestCandidate(album) ?: return@withContext null
        delay(MUSIC_BRAINZ_RATE_LIMIT_MS)
        val release = fetchRelease(bestCandidate.id) ?: return@withContext null
        val albumArtist = release.albumArtist.ifBlank { bestCandidate.albumArtist.ifBlank { album.artist } }
        val coverBytes = fetchTidalCoverArt(
            albumTitle = release.title.ifBlank { album.title },
            albumArtist = albumArtist,
        ) ?: fetchCoverArt(bestCandidate.id)
        val mappedTracks = mapTracks(
            albumSongs = album.songs,
            matchedTracks = release.tracks,
            fallbackArtist = albumArtist,
        )
        AlbumTagMatchSuggestion(
            albumTitle = release.title.ifBlank { album.title },
            albumArtist = albumArtist,
            releaseYear = release.releaseYear ?: bestCandidate.releaseYear,
            coverArtBytes = coverBytes,
            tracks = mappedTracks,
        )
    }

    suspend fun applyEdits(request: AlbumTagEditRequest): TagEditApplyResult = withContext(Dispatchers.IO) {
        val trackEditsById = request.tracks.associateBy { it.songId }
        val coverArtBytes = request.coverArtBytes ?: request.coverArtUri?.let(::readBytes)
        val coverArtMimeType = coverArtBytes?.let(::detectMimeType)
        val editedSongIds = mutableListOf<Long>()
        val editedUris = mutableListOf<Uri>()
        val editedFilePaths = mutableListOf<String>()
        val failures = mutableListOf<TagEditFailure>()
        request.album.songs.forEach { song ->
            val trackEdit = trackEditsById[song.id] ?: return@forEach
            val tempFile = copySongToTempFile(song)
            try {
                updateTagFile(
                    tempFile = tempFile,
                    originalSong = song,
                    request = request,
                    track = trackEdit,
                    coverArtBytes = coverArtBytes,
                    coverArtMimeType = coverArtMimeType,
                )
                val verificationFailures = verifyWrittenTags(
                    tempFile = tempFile,
                    expected = ExpectedTagValues(
                        title = trackEdit.title.trim().ifBlank { song.title },
                        artist = trackEdit.artist.trim().ifBlank { song.artist },
                        album = request.albumTitle.trim().ifBlank { song.album },
                        albumArtist = request.albumArtist.trim().ifBlank { song.artist },
                        year = request.releaseYear?.takeIf { it > 0 }?.toString(),
                        trackNumber = trackEdit.trackNumber.coerceAtLeast(1).toString(),
                        discNumber = trackEdit.discNumber.coerceAtLeast(1).toString(),
                    ),
                    expectArtwork = coverArtBytes != null,
                )
                if (verificationFailures.isNotEmpty()) {
                    failures += TagEditFailure(
                        songId = song.id,
                        fileName = song.fileName,
                        reason = verificationFailures.joinToString(),
                    )
                    return@forEach
                }
                overwriteSongFromTemp(song.uri, tempFile)
                editedSongIds += song.id
                editedUris += song.uri
                resolveFilePath(song)?.let(editedFilePaths::add)
            } catch (throwable: Throwable) {
                if (throwable is SecurityException) throw throwable
                failures += TagEditFailure(
                    songId = song.id,
                    fileName = song.fileName,
                    reason = throwable.message ?: "Unable to save tags.",
                )
            } finally {
                runCatching { tempFile.delete() }
            }
        }
        TagEditApplyResult(
            editedSongIds = editedSongIds,
            editedUris = editedUris,
            editedFilePaths = editedFilePaths.distinct(),
            artworkChanged = coverArtBytes != null,
            failures = failures,
        )
    }

    private fun updateTagFile(
        tempFile: File,
        originalSong: Song,
        request: AlbumTagEditRequest,
        track: EditableAlbumTrack,
        coverArtBytes: ByteArray?,
        coverArtMimeType: String?,
    ) {
        val audioFile = AudioFileIO.read(tempFile)
        val tag = audioFile.tagOrCreateAndSetDefault
        val normalizedAlbumTitle = request.albumTitle.trim().ifBlank { originalSong.album }
        val normalizedAlbumArtist = request.albumArtist.trim().ifBlank { originalSong.artist }
        val normalizedTrackArtist = track.artist.trim().ifBlank {
            if (originalSong.artist.isBlank() || originalSong.artist == request.album.artist) normalizedAlbumArtist else originalSong.artist
        }
        setOrDeleteTextField(tag, FieldKey.ALBUM, normalizedAlbumTitle)
        setOrDeleteTextField(tag, FieldKey.ALBUM_ARTIST, normalizedAlbumArtist)
        setOrDeleteTextField(tag, FieldKey.ARTIST, normalizedTrackArtist)
        setOrDeleteTextField(tag, FieldKey.TITLE, track.title.trim().ifBlank { originalSong.title })
        setOrDeleteTextField(tag, FieldKey.TRACK, track.trackNumber.coerceAtLeast(1).toString())
        setOrDeleteTextField(tag, FieldKey.DISC_NO, track.discNumber.coerceAtLeast(1).toString())
        request.releaseYear?.takeIf { it > 0 }?.let { year ->
            setOrDeleteTextField(tag, FieldKey.YEAR, year.toString())
        } ?: runCatching {
            tag.deleteField(FieldKey.YEAR)
        }
        if (coverArtBytes != null && coverArtMimeType != null) {
            runCatching { tag.deleteArtworkField() }
            val artworkTempFile = createArtworkTempFile(
                bytes = coverArtBytes,
                mimeType = coverArtMimeType,
                songId = originalSong.id,
            )
            try {
                tag.setField(ArtworkFactory.createArtworkFromFile(artworkTempFile))
            } finally {
                runCatching { artworkTempFile.delete() }
            }
        }
        audioFile.commit()
    }

    private fun setOrDeleteTextField(
        tag: org.jaudiotagger.tag.Tag,
        fieldKey: FieldKey,
        value: String?,
    ) {
        val normalizedValue = value?.trim().orEmpty()
        if (normalizedValue.isBlank()) {
            runCatching { tag.deleteField(fieldKey) }
        } else {
            tag.setField(fieldKey, normalizedValue)
        }
    }

    private fun verifyWrittenTags(
        tempFile: File,
        expected: ExpectedTagValues,
        expectArtwork: Boolean,
    ): List<String> {
        val audioFile = AudioFileIO.read(tempFile)
        val tag = audioFile.tagOrCreateAndSetDefault
        val failures = mutableListOf<String>()

        fun check(field: FieldKey, expectedValue: String?, label: String) {
            val normalizedExpected = expectedValue.orEmpty().trim()
            if (normalizedExpected.isBlank()) return
            val actual = tag.getFirst(field).orEmpty().trim()
            if (actual != normalizedExpected) {
                failures += "$label expected '$normalizedExpected' but was '$actual'"
            }
        }

        check(FieldKey.TITLE, expected.title, "Title")
        check(FieldKey.ARTIST, expected.artist, "Artist")
        check(FieldKey.ALBUM, expected.album, "Album")
        check(FieldKey.ALBUM_ARTIST, expected.albumArtist, "Album artist")
        check(FieldKey.YEAR, expected.year, "Year")
        check(FieldKey.TRACK, expected.trackNumber, "Track")
        check(FieldKey.DISC_NO, expected.discNumber, "Disc")
        if (expectArtwork && !hasEmbeddedArtwork(tempFile)) {
            failures += "Artwork was not embedded correctly"
        }
        return failures
    }

    private fun hasEmbeddedArtwork(tempFile: File): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(tempFile.absolutePath)
            retriever.embeddedPicture?.isNotEmpty() == true
        } catch (_: Throwable) {
            false
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun copySongToTempFile(song: Song): File {
        val tempDir = File(appContext.cacheDir, TEMP_TAG_EDIT_DIR_NAME).apply { mkdirs() }
        val suffix = song.fileName.substringAfterLast('.', "").ifBlank { "tmp" }
        val tempFile = File(tempDir, "${song.id}-${System.nanoTime()}.$suffix")
        contentResolver.openInputStream(song.uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open ${song.fileName}")
        return tempFile
    }

    private fun overwriteSongFromTemp(
        songUri: Uri,
        tempFile: File,
    ) {
        contentResolver.openFileDescriptor(songUri, "rw")?.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).channel.use { outputChannel ->
                outputChannel.position(0L)
                outputChannel.truncate(0L)
                FileInputStream(tempFile).channel.use { inputChannel ->
                    var transferred = 0L
                    val totalBytes = inputChannel.size()
                    while (transferred < totalBytes) {
                        val copiedBytes = inputChannel.transferTo(
                            transferred,
                            totalBytes - transferred,
                            outputChannel,
                        )
                        if (copiedBytes <= 0L) {
                            error("Unable to copy updated tags")
                        }
                        transferred += copiedBytes
                    }
                }
                outputChannel.force(true)
            }
        } ?: error("Unable to write updated tags")
    }

    private fun resolveFilePath(song: Song): String? {
        return contentResolver.queryMediaStoreFilePath(appContext, song.uri)
    }

    private fun readBytes(uri: Uri): ByteArray? {
        return contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
    }

    private fun detectMimeType(bytes: ByteArray): String {
        return when {
            bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "image/png"
            bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "image/jpeg"
            bytes.size >= 12 && bytes.copyOfRange(0, 4).decodeToString() == "RIFF" && bytes.copyOfRange(8, 12).decodeToString() == "WEBP" -> "image/webp"
            else -> "image/jpeg"
        }
    }

    private fun createArtworkTempFile(
        bytes: ByteArray,
        mimeType: String,
        songId: Long,
    ): File {
        val tempDir = File(appContext.cacheDir, TEMP_TAG_EDIT_DIR_NAME).apply { mkdirs() }
        val extension = when (mimeType.lowercase(Locale.US)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val artworkFile = File(tempDir, "cover-$songId-${System.nanoTime()}.$extension")
        artworkFile.writeBytes(bytes)
        return artworkFile
    }

    private fun searchBestCandidate(album: Album): ReleaseCandidate? {
        val query = buildSearchQuery(album)
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = "$MUSIC_BRAINZ_BASE/release?query=$encodedQuery&fmt=json&limit=5"
        val json = getJsonObject(url) ?: return null
        val releases = json.optJSONArray("releases") ?: return null
        var best: ReleaseCandidate? = null
        var bestScore = Float.NEGATIVE_INFINITY
        for (index in 0 until releases.length()) {
            val candidate = releases.optJSONObject(index)?.toReleaseCandidate() ?: continue
            val score = scoreCandidate(album, candidate)
            if (score > bestScore) {
                best = candidate
                bestScore = score
            }
        }
        return best
    }

    private fun buildSearchQuery(album: Album): String {
        val title = album.title.trim()
        val artist = album.artist.trim()
        return buildString {
            if (title.isNotBlank()) append("release:\"").append(title).append('"')
            if (artist.isNotBlank()) {
                if (isNotBlank()) append(" AND ")
                append("artist:\"").append(artist).append('"')
            }
        }.ifBlank {
            listOf(album.title, album.artist).filter { it.isNotBlank() }.joinToString(" ")
        }
    }

    private fun scoreCandidate(
        album: Album,
        candidate: ReleaseCandidate,
    ): Float {
        var score = 0f
        score += normalizedStringSimilarity(album.title, candidate.title) * 6f
        score += normalizedStringSimilarity(album.artist, candidate.albumArtist) * 5f
        score -= kotlin.math.abs(album.songCount - candidate.trackCount).toFloat() * 0.8f
        val releaseYear = album.songs.firstNotNullOfOrNull { it.releaseYear }
        if (releaseYear != null && candidate.releaseYear != null) {
            score -= kotlin.math.abs(releaseYear - candidate.releaseYear).toFloat() * 0.35f
        }
        return score
    }

    private fun fetchRelease(releaseId: String): MatchedRelease? {
        val url = "$MUSIC_BRAINZ_BASE/release/$releaseId?inc=recordings+artist-credits+media&fmt=json"
        val json = getJsonObject(url) ?: return null
        val title = json.optString("title").trim()
        val albumArtist = json.optJSONArray("artist-credit").toArtistCreditString()
        val releaseYear = json.optString("date").takeIf { it.isNotBlank() }?.take(4)?.toIntOrNull()
        val media = json.optJSONArray("media") ?: return null
        val tracks = buildList {
            for (mediaIndex in 0 until media.length()) {
                val mediaObject = media.optJSONObject(mediaIndex) ?: continue
                val discNumber = mediaObject.optInt("position").takeIf { it > 0 } ?: mediaIndex + 1
                val mediaTracks = mediaObject.optJSONArray("tracks") ?: continue
                for (trackIndex in 0 until mediaTracks.length()) {
                    val trackObject = mediaTracks.optJSONObject(trackIndex) ?: continue
                    val matchedTitle = trackObject.optString("title").trim()
                        .ifBlank { trackObject.optJSONObject("recording")?.optString("title").orEmpty().trim() }
                    if (matchedTitle.isBlank()) continue
                    val matchedArtist = trackObject.optJSONArray("artist-credit").toArtistCreditString()
                        .ifBlank { albumArtist }
                    add(
                        EditableAlbumTrack(
                            songId = -1L,
                            title = matchedTitle,
                            artist = matchedArtist,
                            trackNumber = trackObject.optInt("position").takeIf { it > 0 } ?: trackIndex + 1,
                            discNumber = discNumber,
                        ),
                    )
                }
            }
        }
        if (tracks.isEmpty()) return null
        return MatchedRelease(
            title = title,
            albumArtist = albumArtist,
            releaseYear = releaseYear,
            tracks = tracks,
        )
    }

    private fun fetchCoverArt(releaseId: String): ByteArray? {
        val url = "$COVER_ART_BASE/release/$releaseId/front-500"
        return getBytes(url)
    }

    private fun fetchTidalCoverArt(
        albumTitle: String,
        albumArtist: String,
    ): ByteArray? {
        val queries = buildList {
            listOf(albumArtist, albumTitle, "tidal")
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .takeIf { it.isNotBlank() }
                ?.let(::add)
            listOf(albumArtist, albumTitle)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .takeIf { it.isNotBlank() }
                ?.let(::add)
        }.distinct()
        queries.forEach { query ->
            val artworkUrl = extractTidalArtworkUrl(query) ?: return@forEach
            getBytes(artworkUrl)?.let { return it }
        }
        return null
    }

    private fun extractTidalArtworkUrl(query: String): String? {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val urls = listOf(
            "https://listen.tidal.com/search/albums?q=$encodedQuery",
            "https://tidal.com/browse/search?query=$encodedQuery",
        )
        urls.forEach { url ->
            val html = getText(url, accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                ?: return@forEach
            TIDAL_ARTWORK_REGEX.find(html)?.groupValues?.getOrNull(1)?.let { match ->
                return match
                    .replace("\\/", "/")
                    .replace("&amp;", "&")
                    .replace(Regex("""/\d{2,4}x\d{2,4}(?=\.jpg)"""), "/1280x1280")
            }
        }
        return null
    }

    private fun mapTracks(
        albumSongs: List<Song>,
        matchedTracks: List<EditableAlbumTrack>,
        fallbackArtist: String,
    ): List<EditableAlbumTrack> {
        val sortedSongs = albumSongs.sortedWith(
            compareBy<Song>({ it.discNumber }, { it.trackNumber }, { it.fileName.lowercase(Locale.ROOT) }),
        )
        return sortedSongs.mapIndexed { index, song ->
            val matched = matchedTracks.getOrNull(index)
            EditableAlbumTrack(
                songId = song.id,
                title = matched?.title?.ifBlank { song.title } ?: song.title,
                artist = matched?.artist?.ifBlank { fallbackArtist } ?: song.artist.ifBlank { fallbackArtist },
                trackNumber = matched?.trackNumber ?: song.trackNumber.coerceAtLeast(index + 1),
                discNumber = matched?.discNumber ?: song.discNumber.coerceAtLeast(1),
            )
        }
    }

    private fun normalizedStringSimilarity(
        left: String,
        right: String,
    ): Float {
        val normalizedLeft = normalizeForMatching(left)
        val normalizedRight = normalizeForMatching(right)
        if (normalizedLeft.isBlank() || normalizedRight.isBlank()) return 0f
        if (normalizedLeft == normalizedRight) return 1f
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) return 0.85f
        val sharedTokens = normalizedLeft.split(' ').intersect(normalizedRight.split(' ').toSet())
        val maxTokens = maxOf(normalizedLeft.split(' ').size, normalizedRight.split(' ').size).coerceAtLeast(1)
        return (sharedTokens.size.toFloat() / maxTokens.toFloat()).coerceIn(0f, 1f)
    }

    private fun normalizeForMatching(value: String): String {
        return value
            .lowercase(Locale.ROOT)
            .replace(Regex("""\([^)]*\)|\[[^]]*]"""), " ")
            .replace(Regex("""[^a-z0-9]+"""), " ")
            .trim()
            .replace(Regex("""\s+"""), " ")
    }

    private fun getJsonObject(url: String): JSONObject? {
        return getText(url)?.let(::JSONObject)
    }

    private fun getText(
        url: String,
        accept: String = "application/json",
    ): String? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return connection.useRequest(accept = accept) { input ->
            input.bufferedReader().use { reader -> reader.readText() }
        }
    }

    private fun getBytes(url: String): ByteArray? {
        val connection = (URL(url).openConnection() as? HttpURLConnection) ?: return null
        return connection.useRequest(accept = "*/*") { input ->
            input.readBytes()
        }
    }

    private inline fun <T> HttpURLConnection.useRequest(
        accept: String = "application/json",
        block: (java.io.InputStream) -> T,
    ): T? {
        connectTimeout = NETWORK_TIMEOUT_MS
        readTimeout = NETWORK_TIMEOUT_MS
        requestMethod = "GET"
        setRequestProperty("Accept", accept)
        setRequestProperty("User-Agent", USER_AGENT)
        instanceFollowRedirects = true
        return runCatching {
            inputStream.use(block)
        }.getOrNull().also {
            disconnect()
        }
    }

    private fun JSONObject.toReleaseCandidate(): ReleaseCandidate? {
        val id = optString("id").trim().takeIf { it.isNotBlank() } ?: return null
        val title = optString("title").trim().takeIf { it.isNotBlank() } ?: return null
        val trackCount = optInt("track-count").takeIf { it > 0 }
            ?: optJSONArray("media")?.let { media ->
                var total = 0
                for (index in 0 until media.length()) {
                    total += media.optJSONObject(index)?.optInt("track-count") ?: 0
                }
                total.takeIf { it > 0 }
            }
            ?: 0
        return ReleaseCandidate(
            id = id,
            title = title,
            albumArtist = optJSONArray("artist-credit").toArtistCreditString(),
            releaseYear = optString("date").takeIf { it.isNotBlank() }?.take(4)?.toIntOrNull(),
            trackCount = trackCount,
        )
    }

    private fun org.json.JSONArray?.toArtistCreditString(): String {
        if (this == null) return ""
        return buildString {
            for (index in 0 until length()) {
                val value = opt(index)
                when (value) {
                    is JSONObject -> {
                        val name = value.optString("name").trim()
                            .ifBlank { value.optJSONObject("artist")?.optString("name").orEmpty().trim() }
                        append(name)
                    }
                    is String -> append(value)
                }
            }
        }.replace(Regex("""\s+"""), " ").trim()
    }

    private data class ReleaseCandidate(
        val id: String,
        val title: String,
        val albumArtist: String,
        val releaseYear: Int?,
        val trackCount: Int,
    )

    private data class MatchedRelease(
        val title: String,
        val albumArtist: String,
        val releaseYear: Int?,
        val tracks: List<EditableAlbumTrack>,
    )

    private data class ExpectedTagValues(
        val title: String,
        val artist: String,
        val album: String,
        val albumArtist: String,
        val year: String?,
        val trackNumber: String,
        val discNumber: String,
    )

    private companion object {
        const val NETWORK_TIMEOUT_MS = 5_000
        const val MUSIC_BRAINZ_RATE_LIMIT_MS = 1_050L
        const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"
        const val MUSIC_BRAINZ_BASE = "https://musicbrainz.org/ws/2"
        const val COVER_ART_BASE = "https://coverartarchive.org"
        const val TEMP_TAG_EDIT_DIR_NAME = "album-tag-edits"
        val TIDAL_ARTWORK_REGEX = Regex(
            """(https:\\?/\\?/resources\.tidal\.com/images/[a-z0-9/]+/1280x1280\.jpg)""",
            RegexOption.IGNORE_CASE,
        )
    }
}
