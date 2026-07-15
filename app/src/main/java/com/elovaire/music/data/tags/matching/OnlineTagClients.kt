package elovaire.music.droidbeauty.app.data.tags.matching

import elovaire.music.droidbeauty.app.data.network.HttpRequest
import elovaire.music.droidbeauty.app.data.network.HttpTransport
import elovaire.music.droidbeauty.app.core.runSuspendCatching
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

internal class HttpAcoustIdClient(
    private val apiKey: String,
    private val cache: TagMatchCache,
) : AcoustIdClient {
    override val isConfigured: Boolean = apiKey.isNotBlank()
    private val rateLimiter = RequestRateLimiter(ACOUSTID_REQUEST_INTERVAL_MS)

    override suspend fun lookup(
        fingerprint: String,
        durationSeconds: Int,
    ): Result<AcoustIdLookupResponse> = runSuspendCatching {
        check(isConfigured) { "AcoustID API key is not configured." }
        val cacheKey = "acoustid:$durationSeconds:$fingerprint"
        val jsonText = cache.getResponse(cacheKey) ?: run {
            rateLimiter.awaitTurn()
            postForm(
                url = ACOUSTID_LOOKUP_URL,
                values = mapOf(
                    "client" to apiKey,
                    "format" to "json",
                    "meta" to "recordings+releases+releasegroups+tracks+compress",
                    "duration" to durationSeconds.toString(),
                    "fingerprint" to fingerprint,
                ),
            ).also { cache.putResponse(cacheKey, it) }
        }
        parseAcoustIdResponse(JSONObject(jsonText))
    }

    private fun parseAcoustIdResponse(root: JSONObject): AcoustIdLookupResponse {
        check(root.optString("status") == "ok") {
            root.optJSONObject("error")?.optString("message").orEmpty().ifBlank { "AcoustID lookup failed." }
        }
        val recordings = buildList {
            val results = root.optJSONArray("results") ?: JSONArray()
            for (resultIndex in 0 until results.length()) {
                val result = results.optJSONObject(resultIndex) ?: continue
                val resultScore = result.optDouble("score", 0.0)
                val resultRecordings = result.optJSONArray("recordings") ?: continue
                for (recordingIndex in 0 until resultRecordings.length()) {
                    val recording = resultRecordings.optJSONObject(recordingIndex) ?: continue
                    val recordingId = recording.optString("id").trim()
                    if (recordingId.isBlank()) continue
                    val releaseIds = buildList {
                        val releases = recording.optJSONArray("releases") ?: JSONArray()
                        for (releaseIndex in 0 until releases.length()) {
                            releases.optJSONObject(releaseIndex)
                                ?.optString("id")
                                ?.trim()
                                ?.takeIf(String::isNotBlank)
                                ?.let(::add)
                        }
                    }.distinct()
                    add(
                        AcoustIdRecordingMatch(
                            recordingId = recordingId,
                            score = resultScore,
                            title = recording.optString("title").trim(),
                            artist = recording.optJSONArray("artists").artistNames(),
                            releaseIds = releaseIds,
                        ),
                    )
                }
            }
        }
        return AcoustIdLookupResponse(
            recordings = recordings
                .groupBy(AcoustIdRecordingMatch::recordingId)
                .map { (_, matches) -> matches.maxBy(AcoustIdRecordingMatch::score) },
        )
    }

    private companion object {
        const val ACOUSTID_LOOKUP_URL = "https://api.acoustid.org/v2/lookup"
        const val ACOUSTID_REQUEST_INTERVAL_MS = 350L
    }
}

internal class HttpMusicBrainzClient(
    private val cache: TagMatchCache,
) : MusicBrainzClient {
    private val rateLimiter = RequestRateLimiter(MUSIC_BRAINZ_REQUEST_INTERVAL_MS)

    override suspend fun getRecording(recordingMbid: String): Result<MusicBrainzRecording> = runSuspendCatching {
        val url = "$MUSIC_BRAINZ_BASE/recording/$recordingMbid?inc=artist-credits+releases&fmt=json"
        val json = getJson(url)
        MusicBrainzRecording(
            id = json.optString("id"),
            title = json.optString("title").trim(),
            artist = json.optJSONArray("artist-credit").artistCredit(),
            releaseIds = buildList {
                val releases = json.optJSONArray("releases") ?: JSONArray()
                for (index in 0 until releases.length()) {
                    releases.optJSONObject(index)
                        ?.optString("id")
                        ?.trim()
                        ?.takeIf(String::isNotBlank)
                        ?.let(::add)
                }
            }.distinct(),
        )
    }

    override suspend fun getRelease(releaseMbid: String): Result<MusicBrainzRelease> = runSuspendCatching {
        val url = "$MUSIC_BRAINZ_BASE/release/$releaseMbid" +
            "?inc=recordings+artist-credits+url-rels+release-groups+release-group-level-rels&fmt=json"
        val json = getJson(url)
        parseRelease(json)
    }

    override suspend fun searchRelease(
        albumTitle: String,
        albumArtist: String,
    ): Result<List<String>> = runSuspendCatching {
        val query = buildString {
            if (albumTitle.isNotBlank()) append("release:\"").append(albumTitle).append('"')
            if (albumArtist.isNotBlank()) {
                if (isNotEmpty()) append(" AND ")
                append("artist:\"").append(albumArtist).append('"')
            }
        }
        if (query.isBlank()) return@runSuspendCatching emptyList()
        val url = "$MUSIC_BRAINZ_BASE/release?query=${query.urlEncode()}&fmt=json&limit=5"
        val releases = getJson(url).optJSONArray("releases") ?: return@runSuspendCatching emptyList()
        buildList {
            for (index in 0 until releases.length()) {
                releases.optJSONObject(index)
                    ?.optString("id")
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?.let(::add)
            }
        }
    }

    private suspend fun getJson(url: String): JSONObject {
        val cached = cache.getResponse(url)
        if (cached != null) return JSONObject(cached)
        rateLimiter.awaitTurn()
        val text = getText(url)
        cache.putResponse(url, text)
        return JSONObject(text)
    }

    private fun parseRelease(json: JSONObject): MusicBrainzRelease {
        val albumArtist = json.optJSONArray("artist-credit").artistCredit()
        val tracks = buildList {
            val media = json.optJSONArray("media") ?: JSONArray()
            for (mediaIndex in 0 until media.length()) {
                val medium = media.optJSONObject(mediaIndex) ?: continue
                val discNumber = medium.optInt("position").takeIf { it > 0 } ?: mediaIndex + 1
                val mediumTracks = medium.optJSONArray("tracks") ?: continue
                for (trackIndex in 0 until mediumTracks.length()) {
                    val track = mediumTracks.optJSONObject(trackIndex) ?: continue
                    val recording = track.optJSONObject("recording")
                    val title = track.optString("title").trim()
                        .ifBlank { recording?.optString("title").orEmpty().trim() }
                    if (title.isBlank()) continue
                    add(
                        MusicBrainzTrack(
                            id = track.optString("id").trim(),
                            recordingId = recording?.optString("id").orEmpty().trim(),
                            title = title,
                            artist = track.optJSONArray("artist-credit").artistCredit()
                                .ifBlank { recording?.optJSONArray("artist-credit").artistCredit() }
                                .ifBlank { albumArtist },
                            trackNumber = track.optInt("position").takeIf { it > 0 } ?: trackIndex + 1,
                            discNumber = discNumber,
                            durationMs = track.optLong("length").takeIf { it > 0L }
                                ?: recording?.optLong("length")?.takeIf { it > 0L },
                        ),
                    )
                }
            }
        }
        val relatedUrls = buildList {
            addAll(json.optJSONArray("relations").relatedUrls())
            addAll(json.optJSONObject("release-group")?.optJSONArray("relations").relatedUrls())
        }.distinct()
        return MusicBrainzRelease(
            id = json.optString("id").trim(),
            title = json.optString("title").trim(),
            albumArtist = albumArtist,
            releaseYear = json.optString("date").take(4).toIntOrNull(),
            tracks = tracks,
            relatedUrls = relatedUrls,
        )
    }

    private companion object {
        const val MUSIC_BRAINZ_BASE = "https://musicbrainz.org/ws/2"
        const val MUSIC_BRAINZ_REQUEST_INTERVAL_MS = 1_050L
    }
}

internal class RequestRateLimiter(
    private val minimumIntervalMs: Long,
) {
    private val mutex = Mutex()
    private var lastRequestAtMs = 0L

    suspend fun awaitTurn() {
        mutex.withLock {
            val now = android.os.SystemClock.elapsedRealtime()
            val waitMs = (lastRequestAtMs + minimumIntervalMs - now).coerceAtLeast(0L)
            if (waitMs > 0L) delay(waitMs)
            lastRequestAtMs = android.os.SystemClock.elapsedRealtime()
        }
    }
}

internal fun getText(url: String): String {
    return getText(url, "application/json")
}

internal fun getText(url: String, accept: String): String {
    return HTTP_TRANSPORT.getText(httpRequest(url, accept), MAX_TEXT_RESPONSE_BYTES)
}

internal fun getBytes(url: String): ByteArray {
    return HTTP_TRANSPORT.getBytes(httpRequest(url, "*/*"), MAX_BINARY_RESPONSE_BYTES)
}

private fun postForm(url: String, values: Map<String, String>): String {
    val body = values.entries.joinToString("&") { (key, value) ->
        "${key.urlEncode()}=${value.urlEncode()}"
    }.toByteArray()
    return HTTP_TRANSPORT.postForm(
        request = httpRequest(url, "application/json"),
        body = body,
        maxBytes = MAX_TEXT_RESPONSE_BYTES,
    )
}

private fun httpRequest(url: String, accept: String) = HttpRequest(
    url = url,
    accept = accept,
    headers = mapOf("User-Agent" to USER_AGENT),
    connectTimeoutMs = NETWORK_TIMEOUT_MS,
    readTimeoutMs = NETWORK_TIMEOUT_MS,
)

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun JSONArray?.artistNames(): String {
    if (this == null) return ""
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.optString("name")?.trim()?.takeIf(String::isNotBlank)?.let(::add)
        }
    }.joinToString(" & ")
}

private fun JSONArray?.artistCredit(): String {
    if (this == null) return ""
    return buildString {
        for (index in 0 until length()) {
            when (val value = opt(index)) {
                is JSONObject -> {
                    append(
                        value.optString("name").trim()
                            .ifBlank { value.optJSONObject("artist")?.optString("name").orEmpty().trim() },
                    )
                    append(value.optString("joinphrase"))
                }

                is String -> append(value)
            }
        }
    }.replace(Regex("""\s+"""), " ").trim()
}

private fun JSONArray?.relatedUrls(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)
                ?.optJSONObject("url")
                ?.optString("resource")
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let(::add)
        }
    }
}

private const val NETWORK_TIMEOUT_MS = 8_000
private const val MAX_TEXT_RESPONSE_BYTES = 1 * 1024 * 1024
private const val MAX_BINARY_RESPONSE_BYTES = 16 * 1024 * 1024
private const val USER_AGENT = "Elovaire/1.0 (https://github.com/droidbeauty/elovaire-music)"
private val HTTP_TRANSPORT = HttpTransport()
