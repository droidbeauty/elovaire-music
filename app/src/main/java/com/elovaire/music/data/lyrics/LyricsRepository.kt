package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LyricsRepository(
    appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: AppClock = AndroidAppClock,
) {
    private val cache = LyricsCache(appContext.applicationContext, clock)
    private val localLyricsResolver = LocalLyricsResolver(appContext.applicationContext)
    private val lrclibClient = LrclibClient()
    private val memoryPositiveCache = ConcurrentHashMap<String, LyricsCacheEntry>()

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean,
        includeOnline: Boolean = true,
    ): LyricsResult? {
        val identity = song.toLyricsIdentity()
        return memoryCachedLyrics(identity)?.takeUnless { it.online && !includeOnline }?.result
            ?: cache.get(identity, includeNotFound, includeOnline)
    }

    fun localLyrics(song: Song): LyricsResult? {
        val identity = song.toLyricsIdentity()
        val localMatch = localLyricsResolver.resolve(song) ?: return null
        val entry = localMatch.toCacheEntry()
        rememberPositive(identity, entry)
        cache.put(identity, entry)
        return entry.result
    }

    fun clearCacheFor(song: Song) {
        val identity = song.toLyricsIdentity()
        identity.cacheKeys.forEach(memoryPositiveCache::remove)
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
            ?: localLyricsResolver.resolve(song)?.let { local ->
                local.toCacheEntry().also { entry ->
                    rememberPositive(identity, entry)
                    cache.put(identity, entry)
                }.result
            }
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
        var entry: LyricsCacheEntry? = null
        identity.cacheKeys.forEach { key ->
            val cached = memoryPositiveCache[key] ?: return@forEach
            if (cached.isExpired(now)) {
                memoryPositiveCache.remove(key)
            } else if (entry == null) {
                entry = cached
            }
        }
        return entry
    }

    private fun rememberPositive(identity: LyricsIdentity, entry: LyricsCacheEntry) {
        identity.cacheKeys.forEach { key -> memoryPositiveCache[key] = entry }
        trimMemoryCache(MAX_MEMORY_CACHE_KEYS)
    }

    private fun trimMemoryCache(maxKeys: Int) {
        if (memoryPositiveCache.size <= maxKeys) return
        val now = clock.wallTimeMs()
        memoryPositiveCache.entries.removeIf { (_, entry) -> entry.isExpired(now) }
        memoryPositiveCache.keys.take((memoryPositiveCache.size - maxKeys).coerceAtLeast(0))
            .forEach(memoryPositiveCache::remove)
    }

    private companion object {
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1_000L
        const val NEGATIVE_CACHE_TTL_MS = 24L * 60L * 60L * 1_000L
        const val MAX_MEMORY_CACHE_KEYS = 96
        const val MODERATE_MEMORY_CACHE_KEYS = 24
    }
}
