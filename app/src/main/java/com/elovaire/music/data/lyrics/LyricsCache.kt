package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.network.readUtf8Bounded
import org.json.JSONArray
import org.json.JSONObject

internal class LyricsCache(
    appContext: Context,
) {
    private val cacheFile = allowStrictModeDiskReads {
        // The lazy cache only needs its app-private file handle during service construction.
        appContext.filesDir.resolve(CACHE_FILE_NAME)
    }
    private val cacheLock = Any()
    private val cacheEntries = LinkedHashMap<String, LyricsCacheEntry>()
    private var cacheLoaded = false

    fun get(
        identity: LyricsIdentity,
        includeNotFound: Boolean,
    ): LyricsResult? = synchronized(cacheLock) {
        ensureLoadedLocked()
        val now = System.currentTimeMillis()
        val staleKeys = mutableListOf<String>()
        val entry = identity.cacheKeys.firstNotNullOfOrNull { key ->
            cacheEntries[key]?.also { cached ->
                if (cached.isExpired(now)) {
                    staleKeys += key
                }
            }?.takeUnless { it.isExpired(now) }
        }
        if (staleKeys.isNotEmpty()) {
            staleKeys.forEach(cacheEntries::remove)
            persistLocked()
        }
        when {
            entry == null -> null
            !includeNotFound && (entry.result == LyricsResult.NotFound || entry.result == LyricsResult.Timeout) -> null
            else -> entry.result
        }
    }

    fun put(
        identity: LyricsIdentity,
        entry: LyricsCacheEntry,
    ) = synchronized(cacheLock) {
        ensureLoadedLocked()
        identity.cacheKeys.forEach { key ->
            cacheEntries[key] = entry
        }
        trimLocked()
        persistLocked()
    }

    fun clearExpired() = synchronized(cacheLock) {
        ensureLoadedLocked()
        val now = System.currentTimeMillis()
        val removed = cacheEntries.entries.removeIf { (_, entry) -> entry.isExpired(now) }
        if (removed) {
            persistLocked()
        }
    }

    fun remove(identity: LyricsIdentity) = synchronized(cacheLock) {
        ensureLoadedLocked()
        var removed = false
        identity.cacheKeys.forEach { key ->
            removed = cacheEntries.remove(key) != null || removed
        }
        if (removed) {
            persistLocked()
        }
    }

    private fun ensureLoadedLocked() {
        if (cacheLoaded) return
        cacheLoaded = true
        if (!cacheFile.exists()) return
        runCatching {
            val root = JSONObject(cacheFile.inputStream().use { input ->
                input.readUtf8Bounded(MAX_CACHE_FILE_BYTES, cacheFile.length())
            })
            if (root.optInt("version") != CACHE_VERSION) return@runCatching
            val entries = root.optJSONArray("entries") ?: JSONArray()
            repeat(entries.length()) { index ->
                val entryJson = entries.optJSONObject(index) ?: return@repeat
                val key = entryJson.optString("key")
                if (key.isBlank()) return@repeat
                val result = when (entryJson.optString("result")) {
                    RESULT_FOUND -> {
                        val payloadJson = entryJson.optJSONObject("payload") ?: return@repeat
                        LyricsResult.Found(payloadJson.toLyricsPayload())
                    }
                    RESULT_NOT_FOUND -> LyricsResult.NotFound
                    RESULT_TIMEOUT -> LyricsResult.Timeout
                    else -> return@repeat
                }
                cacheEntries[key] = LyricsCacheEntry(
                    result = result,
                    expiresAtMillis = entryJson.optLong("expiresAtMillis", 0L),
                    providerName = entryJson.optString("providerName").takeIf { it.isNotBlank() },
                    confidence = entryJson.optInt("confidence", 0),
                )
            }
        }
    }

    private fun persistLocked() {
        runCatching {
            val root = JSONObject().apply {
                put("version", CACHE_VERSION)
                put(
                    "entries",
                    JSONArray().apply {
                        cacheEntries.forEach { (key, entry) ->
                            put(
                                JSONObject().apply {
                                    put("key", key)
                                    put("expiresAtMillis", entry.expiresAtMillis)
                                    put("providerName", entry.providerName.orEmpty())
                                    put("confidence", entry.confidence)
                                    when (val result = entry.result) {
                                        is LyricsResult.Found -> {
                                            put("result", RESULT_FOUND)
                                            put("payload", result.payload.toJson())
                                        }
                                        LyricsResult.NotFound -> {
                                            put("result", RESULT_NOT_FOUND)
                                        }
                                        LyricsResult.Timeout -> {
                                            put("result", RESULT_TIMEOUT)
                                        }
                                    }
                                },
                            )
                        }
                    },
                )
            }
            cacheFile.writeText(root.toString())
        }
    }

    private fun trimLocked() {
        while (cacheEntries.size > MAX_ENTRIES) {
            val firstKey = cacheEntries.keys.firstOrNull() ?: return
            cacheEntries.remove(firstKey)
        }
    }

    private fun LyricsPayload.toJson(): JSONObject {
        return JSONObject().apply {
            put("isSynced", isSynced)
            put("displayTimingOffsetMs", displayTimingOffsetMs)
            put("timingScale", timingScale.toDouble())
            put("timingProfile", timingProfile.name)
            put("providerName", providerName.orEmpty())
            put("confidence", confidence)
            put(
                "lines",
                JSONArray().apply {
                    lines.forEach { line ->
                        put(
                            JSONObject().apply {
                                put("text", line.text)
                                put("startTimeMs", line.startTimeMs ?: JSONObject.NULL)
                                put("endTimeMs", line.endTimeMs ?: JSONObject.NULL)
                                put("index", line.index)
                            },
                        )
                    }
                },
            )
        }
    }

    private fun JSONObject.toLyricsPayload(): LyricsPayload {
        val linesArray = optJSONArray("lines") ?: JSONArray()
        val lines = buildList {
            repeat(linesArray.length()) { index ->
                val lineJson = linesArray.optJSONObject(index) ?: return@repeat
                add(
                    LyricsLine(
                        text = lineJson.optString("text"),
                        startTimeMs = lineJson.opt("startTimeMs")?.takeUnless { it == JSONObject.NULL }?.toString()?.toLongOrNull(),
                        endTimeMs = lineJson.opt("endTimeMs")?.takeUnless { it == JSONObject.NULL }?.toString()?.toLongOrNull(),
                        index = lineJson.optInt("index", index),
                    ),
                )
            }
        }
        return LyricsPayload(
            lines = lines,
            isSynced = optBoolean("isSynced"),
            displayTimingOffsetMs = optLong("displayTimingOffsetMs", 0L),
            timingScale = optDouble("timingScale", 1.0).toFloat().takeIf { it.isFinite() && it > 0f } ?: 1f,
            timingProfile = runCatching {
                SyncedLyricsTimingProfile.valueOf(optString("timingProfile", SyncedLyricsTimingProfile.ExactIntervals.name))
            }.getOrDefault(SyncedLyricsTimingProfile.ExactIntervals),
            providerName = optString("providerName").takeIf { it.isNotBlank() },
            confidence = optInt("confidence", 0),
        )
    }

    private companion object {
        const val CACHE_FILE_NAME = "lyrics_cache_v5.json"
        const val CACHE_VERSION = 5
        const val MAX_ENTRIES = 320
        const val MAX_CACHE_FILE_BYTES = 2 * 1024 * 1024
        const val RESULT_FOUND = "found"
        const val RESULT_NOT_FOUND = "not_found"
        const val RESULT_TIMEOUT = "timeout"
    }
}
