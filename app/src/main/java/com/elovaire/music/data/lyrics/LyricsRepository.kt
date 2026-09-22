package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.LinkedHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LyricsRepository(
    appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: AppClock = AndroidAppClock,
    resourceTracker: BackendResourceTracker = BackendResourceRegistry,
) {
    private val cache = LyricsCache(appContext.applicationContext, clock, ioDispatcher)
    private val localLyricsResolver = LocalLyricsResolver(appContext.applicationContext)
    private val lrclibClient = LrclibClient(resourceTracker = resourceTracker)
    private data class MemoryEntry(
        val keys: Set<String>,
        val value: LyricsCacheEntry,
    )

    private val memoryLock = Any()
    private val memoryPositiveCache = LinkedHashMap<String, MemoryEntry>(MAX_MEMORY_CACHE_KEYS, 0.75f, true)

    suspend fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean,
        includeOnline: Boolean = true,
    ): LyricsResult? = withContext(ioDispatcher) {
        val identity = song.toLyricsIdentity()
        memoryCachedLyrics(identity)?.takeUnless { it.online && !includeOnline }?.result
            ?: cache.get(identity, includeNotFound, includeOnline)
    }

    suspend fun localLyrics(song: Song): LyricsResult? = withContext(ioDispatcher) {
        val identity = song.toLyricsIdentity()
        val localMatch = localLyricsResolver.resolve(song) ?: return@withContext null
        val entry = localMatch.toCacheEntry()
        rememberPositive(identity, entry)
        cache.put(identity, entry)
        entry.result
    }

    fun clearCacheFor(song: Song) {
        val identity = song.toLyricsIdentity()
        synchronized(memoryLock) {
            memoryPositiveCache.entries.removeIf { (_, value) -> value.keys.any(identity.cacheKeys::contains) }
        }
        cache.remove(identity)
    }

    fun onMemoryPressure(pressure: MemoryPressure) {
        if (pressure == MemoryPressure.Normal) return
        trimMemoryCache(if (pressure == MemoryPressure.Critical) 0 else MODERATE_MEMORY_CACHE_KEYS)
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean,
        onlineEnabled: Boolean = true,
    ): LyricsResult = withContext(ioDispatcher) {
        val identity = song.toLyricsIdentity()
        localLyricsResolver.resolve(song)?.let { local ->
            local.toCacheEntry().also { entry ->
                rememberPositive(identity, entry)
                cache.put(identity, entry)
            }.result
        }
            ?: memoryCachedLyrics(identity)?.takeUnless { it.online && !onlineEnabled }?.result
            ?: cache.get(identity, includeNotFound = allowCachedNotFound, includeOnline = onlineEnabled)
            ?: if (!onlineEnabled) LyricsResult.NotFound else lrclibClient.fetch(song).also { result ->
                if (result is LyricsResult.Found || result == LyricsResult.NotFound) {
                    cache.put(
                        identity,
                        LyricsCacheEntry(
                            result = result,
                            expiresAtMillis = clock.wallTimeMs() + if (result is LyricsResult.Found) POSITIVE_CACHE_TTL_MS else NEGATIVE_CACHE_TTL_MS,
                            online = true,
                        ),
                    )
                }
            }
    }

    private fun LocalLyricsMatch.toCacheEntry(): LyricsCacheEntry = LyricsCacheEntry(
        result = LyricsResult.Found(payload),
        expiresAtMillis = clock.wallTimeMs() + POSITIVE_CACHE_TTL_MS,
    )

    private fun memoryCachedLyrics(identity: LyricsIdentity): LyricsCacheEntry? {
        val now = clock.wallTimeMs()
        return synchronized(memoryLock) {
            val match = memoryPositiveCache.entries.firstOrNull { (_, value) ->
                value.keys.any(identity.cacheKeys::contains)
            } ?: return@synchronized null
            if (match.value.value.isExpired(now)) {
                memoryPositiveCache.remove(match.key)
                return@synchronized null
            }
            val touched = match.value
            memoryPositiveCache.remove(match.key)
            memoryPositiveCache[match.key] = touched
            touched.value
        }
    }

    private fun rememberPositive(identity: LyricsIdentity, entry: LyricsCacheEntry) {
        val canonicalKey = identity.cacheKeys.firstOrNull() ?: return
        synchronized(memoryLock) {
            memoryPositiveCache.entries.removeIf { (_, value) -> value.keys.any(identity.cacheKeys::contains) }
            memoryPositiveCache[canonicalKey] = MemoryEntry(identity.cacheKeys.toSet(), entry)
            trimMemoryCacheLocked(MAX_MEMORY_CACHE_KEYS)
        }
    }

    private fun trimMemoryCache(maxKeys: Int) {
        synchronized(memoryLock) { trimMemoryCacheLocked(maxKeys) }
    }

    private fun trimMemoryCacheLocked(maxEntries: Int) {
        if (memoryPositiveCache.size <= maxEntries) return
        val now = clock.wallTimeMs()
        memoryPositiveCache.entries.removeIf { (_, entry) -> entry.value.isExpired(now) }
        while (memoryPositiveCache.size > maxEntries) {
            memoryPositiveCache.entries.iterator().apply {
                if (hasNext()) {
                    next()
                    remove()
                }
            }
        }
    }

    private companion object {
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 24L * 60L * 60L * 1_000L
        const val MAX_MEMORY_CACHE_KEYS = 96
        const val MODERATE_MEMORY_CACHE_KEYS = 24
    }
}
