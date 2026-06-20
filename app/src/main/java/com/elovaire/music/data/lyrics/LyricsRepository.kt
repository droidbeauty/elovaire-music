package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

internal class LyricsRepository(
    appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val applicationContext = appContext.applicationContext
    private val cache = LyricsCache(applicationContext)
    private val localLyricsResolver = LocalLyricsResolver(applicationContext)
    private val lrcLibLyricsProvider = LrcLibLyricsProvider(ioDispatcher = ioDispatcher)
    private val lyricsOvhProvider = LyricsOvhProvider()
    private val serviceScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<LyricsLookupOutcome>>()
    private val memoryPositiveCache = ConcurrentHashMap<String, LyricsCacheEntry>()

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean,
    ): LyricsResult? {
        val identity = song.toLyricsIdentity()
        val result = memoryCachedLyrics(identity)
            ?: cache.get(identity, includeNotFound)
        logDebug("cache ${if (result == null) "miss" else "hit"} song=${song.id} result=${result?.javaClass?.simpleName}")
        return result
    }

    fun clearCacheFor(song: Song) {
        val identity = song.toLyricsIdentity()
        identity.cacheKeys.forEach(memoryPositiveCache::remove)
        cache.remove(identity)
    }

    fun prefetchLyrics(song: Song) {
        val identity = song.toLyricsIdentity()
        val cachedResult = memoryCachedLyrics(identity) ?: cache.get(identity, includeNotFound = false)
        val alreadyHasSyncedLyrics = (cachedResult as? LyricsResult.Found)?.payload?.isSynced == true
        if (alreadyHasSyncedLyrics || inFlightRequests.keys.any { it.identityPart() == identity.normalizedLookupKey }) {
            return
        }
        serviceScope.launch {
            fetchLyrics(
                song = song,
                allowCachedNotFound = false,
                lookupMode = LyricsLookupMode.FastPresenceCheck,
            )
        }
    }

    fun cancelObsoleteRequests(keepSongs: List<Song?>) {
        val keepKeys = keepSongs
            .filterNotNull()
            .mapTo(mutableSetOf()) { it.toLyricsIdentity().normalizedLookupKey }
        inFlightRequests.entries.removeIf { (key, request) ->
            val obsolete = key.identityPart() !in keepKeys
            if (obsolete) {
                request.cancel()
            }
            obsolete
        }
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean,
        lookupMode: LyricsLookupMode,
    ): LyricsResult = coroutineScope {
        val identity = song.toLyricsIdentity()
        val requestKey = identity.requestKey(lookupMode)
        val preferSyncedUpgrade = lookupMode == LyricsLookupMode.Full
        val cachedResult = memoryCachedLyrics(identity) ?: cache.get(identity, includeNotFound = allowCachedNotFound)
        val cachedPlainFallback = (cachedResult as? LyricsResult.Found)
            ?.takeIf { preferSyncedUpgrade && !it.payload.isSynced }

        if (cachedResult != null && cachedPlainFallback == null) {
            return@coroutineScope cachedResult
        }

        val existing = inFlightRequests[requestKey]
        if (existing != null) {
            val existingResult = existing.await().result
            return@coroutineScope if (cachedPlainFallback != null && existingResult !is LyricsResult.Found) {
                cachedPlainFallback
            } else {
                existingResult
            }
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song, identity, lookupMode, cachedPlainFallback)
            }.getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                logDebug("lyrics lookup failed for ${identity.artist} - ${identity.title}", throwable)
                LyricsLookupOutcome(
                    result = cachedPlainFallback ?: LyricsResult.Timeout,
                    cacheTtlMs = if (cachedPlainFallback == null) CACHE_TTL_TIMEOUT_MS else POSITIVE_CACHE_TTL_MS,
                    state = if (cachedPlainFallback == null) LyricsLookupState.Error else LyricsLookupState.FoundUnsynced,
                    providerName = cachedPlainFallback?.payload?.providerName,
                    confidence = cachedPlainFallback?.payload?.confidence ?: 0,
                )
            }
        }
        val activeRequest = inFlightRequests.putIfAbsent(requestKey, request) ?: request
        if (activeRequest !== request) {
            request.cancel()
        } else {
            activeRequest.invokeOnCompletion {
                inFlightRequests.remove(requestKey, activeRequest)
            }
        }

        val outcome = activeRequest.await()
        val result = outcome.result
        outcome.cacheTtlMs?.let { ttl ->
            val entry = LyricsCacheEntry(
                result = result,
                expiresAtMillis = System.currentTimeMillis() + ttl,
                providerName = outcome.providerName,
                confidence = outcome.confidence,
            )
            if (result is LyricsResult.Found) {
                rememberPositive(identity, entry)
            }
            cache.put(identity, entry)
        }
        return@coroutineScope result
    }

    private suspend fun resolveLyrics(
        song: Song,
        identity: LyricsIdentity,
        lookupMode: LyricsLookupMode,
        cachedPlainFallback: LyricsResult.Found?,
    ): LyricsLookupOutcome = coroutineScope {
        cache.clearExpired()

        val local = withContext(ioDispatcher) {
            withTimeoutOrNull(LOCAL_LOOKUP_TIMEOUT_MS) {
                localLyricsResolver.resolve(song)
            }
        }
        logDebug("local song=${song.id} result=${local?.payload?.let { if (it.isSynced) "synced" else "plain" } ?: "none"}")
        if (local?.payload?.isSynced == true) {
            return@coroutineScope local.toLookupOutcome()
        }

        if (!isNetworkAvailable()) {
            val offlineResult = local?.payload
                ?: cachedPlainFallback?.payload
            return@coroutineScope if (offlineResult != null) {
                LyricsLookupOutcome(
                    result = LyricsResult.Found(offlineResult),
                    cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                    state = if (offlineResult.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                    providerName = offlineResult.providerName,
                    confidence = offlineResult.confidence,
                )
            } else {
                LyricsLookupOutcome(
                    result = LyricsResult.NotFound,
                    cacheTtlMs = CACHE_TTL_OFFLINE_MS,
                    state = LyricsLookupState.Error,
                )
            }
        }

        val remoteOutcome = withContext(ioDispatcher) {
            withTimeoutOrNull(
                if (lookupMode == LyricsLookupMode.Full) FULL_REMOTE_LOOKUP_BUDGET_MS else FAST_REMOTE_LOOKUP_BUDGET_MS,
            ) {
                lrcLibLyricsProvider.findBestLyrics(song, identity, lookupMode)
                    ?.let(RemoteLookupOutcome::Match)
                    ?: lyricsOvhProvider.findBestLyrics(song, identity, lookupMode)
                        ?.let(RemoteLookupOutcome::Match)
                    ?: RemoteLookupOutcome.NotFound
            } ?: RemoteLookupOutcome.Timeout
        }
        val remoteMatch = (remoteOutcome as? RemoteLookupOutcome.Match)?.match
        logDebug(
            "remote song=${song.id} outcome=${remoteOutcome::class.simpleName} " +
                "provider=${remoteMatch?.providerName} synced=${remoteMatch?.payload?.isSynced}",
        )

        val bestPayload = when {
            remoteMatch?.payload?.isSynced == true -> remoteMatch.payload
            local?.payload != null -> local.payload
            remoteMatch?.payload != null -> remoteMatch.payload
            cachedPlainFallback != null -> cachedPlainFallback.payload
            else -> null
        }

        return@coroutineScope when {
            bestPayload != null -> LyricsLookupOutcome(
                result = LyricsResult.Found(bestPayload),
                cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                state = if (bestPayload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                providerName = bestPayload.providerName,
                confidence = bestPayload.confidence,
            )

            remoteOutcome == RemoteLookupOutcome.Timeout -> LyricsLookupOutcome(
                result = LyricsResult.Timeout,
                cacheTtlMs = CACHE_TTL_TIMEOUT_MS,
                state = LyricsLookupState.Error,
            )

            else -> LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = CACHE_TTL_NOT_FOUND_MS,
                state = LyricsLookupState.NotFound,
            )
        }
    }

    private fun LocalLyricsMatch.toLookupOutcome(): LyricsLookupOutcome {
        return LyricsLookupOutcome(
            result = LyricsResult.Found(payload),
            cacheTtlMs = POSITIVE_CACHE_TTL_MS,
            state = if (payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
            providerName = payload.providerName,
            confidence = payload.confidence,
        )
    }

    private fun memoryCachedLyrics(identity: LyricsIdentity): LyricsResult? {
        val now = System.currentTimeMillis()
        val entry = identity.cacheKeys.firstNotNullOfOrNull { key ->
            memoryPositiveCache[key]?.takeUnless { it.isExpired(now) }
        }
        identity.cacheKeys.forEach { key ->
            if (memoryPositiveCache[key]?.isExpired(now) == true) {
                memoryPositiveCache.remove(key)
            }
        }
        return entry?.result
    }

    private fun rememberPositive(
        identity: LyricsIdentity,
        entry: LyricsCacheEntry,
    ) {
        if (entry.result !is LyricsResult.Found) return
        identity.cacheKeys.forEach { key ->
            memoryPositiveCache[key] = entry
        }
    }

    private fun LyricsIdentity.requestKey(lookupMode: LyricsLookupMode): String {
        return "$normalizedLookupKey$REQUEST_KEY_SEPARATOR${lookupMode.name}"
    }

    private fun String.identityPart(): String = substringBeforeLast(REQUEST_KEY_SEPARATOR)

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun logDebug(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        if (throwable == null) {
            Log.d(TAG, message)
        } else {
            Log.d(TAG, message, throwable)
        }
    }

    private companion object {
        const val TAG = "LyricsRepository"
        const val LOCAL_LOOKUP_TIMEOUT_MS = 250L
        const val FAST_REMOTE_LOOKUP_BUDGET_MS = 1_100L
        const val FULL_REMOTE_LOOKUP_BUDGET_MS = 6_000L
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1000L
        const val CACHE_TTL_NOT_FOUND_MS = 30_000L
        const val CACHE_TTL_TIMEOUT_MS = 15_000L
        const val CACHE_TTL_OFFLINE_MS = 90_000L
        const val REQUEST_KEY_SEPARATOR = "::lookup-mode::"
    }

    private sealed interface RemoteLookupOutcome {
        data class Match(val match: ProviderLyricsMatch) : RemoteLookupOutcome
        data object NotFound : RemoteLookupOutcome
        data object Timeout : RemoteLookupOutcome
    }
}
