package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

internal class LyricsRepository(
    appContext: Context,
    private val onlineLookupEnabled: StateFlow<Boolean>,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
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

    init {
        serviceScope.launch {
            backgroundWorkPolicy.isForeground.collect { isForeground ->
                if (!isForeground) {
                    cancelObsoleteRequests(emptyList())
                }
            }
        }
    }

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

    fun localLyrics(song: Song): LyricsResult? {
        val identity = song.toLyricsIdentity()
        val localMatch = localLyricsResolver.resolve(song) ?: return null
        val entry = localMatch.toCacheEntry()
        rememberPositive(identity, entry)
        cache.put(identity, entry)
        logDebug(
            "local-priority song=${song.id} result=${if (localMatch.payload.isSynced) "synced" else "plain"}",
        )
        return entry.result
    }

    fun clearCacheFor(song: Song) {
        val identity = song.toLyricsIdentity()
        identity.cacheKeys.forEach(memoryPositiveCache::remove)
        cache.remove(identity)
    }

    fun prefetchLyrics(song: Song) {
        if (!backgroundWorkPolicy.shouldStartLyricsPrefetch()) return
        if (!onlineLookupEnabled.value) return
        if (!isNetworkSuitableForPrefetch()) return
        val identity = song.toLyricsIdentity()
        val cachedResult = memoryCachedLyrics(identity) ?: cache.get(identity, includeNotFound = true)
        if (
            cachedResult != null ||
            inFlightRequests.keys.any { it.identityPart() == identity.normalizedLookupKey } ||
            inFlightRequests.keys.any { it.endsWith("${REQUEST_KEY_SEPARATOR}${LyricsLookupMode.FastPresenceCheck.name}") }
        ) {
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

    fun release() {
        inFlightRequests.values.forEach { request -> request.cancel() }
        inFlightRequests.clear()
        serviceScope.cancel()
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean,
        lookupMode: LyricsLookupMode,
    ): LyricsResult = coroutineScope {
        val identity = song.toLyricsIdentity()
        val requestKey = identity.requestKey(lookupMode)
        val cachedResult = memoryCachedLyrics(identity) ?: cache.get(identity, includeNotFound = allowCachedNotFound)
        if (cachedResult != null) {
            return@coroutineScope cachedResult
        }

        val existing = inFlightRequests[requestKey]
        if (existing != null) {
            return@coroutineScope existing.await().result
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song, lookupMode)
            }.getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                logDebug("lyrics lookup failed song=${song.id}", throwable)
                LyricsLookupOutcome(
                    result = LyricsResult.Timeout,
                    cacheTtlMs = CACHE_TTL_TIMEOUT_MS,
                    state = LyricsLookupState.Error,
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
        lookupMode: LyricsLookupMode,
    ): LyricsLookupOutcome = coroutineScope {
        cache.clearExpired()
        val identity = song.toLyricsIdentity()

        val local = withContext(ioDispatcher) {
            withTimeoutOrNull(LOCAL_LOOKUP_TIMEOUT_MS) {
                localLyricsResolver.resolve(song)
            }
        }
        logDebug("local song=${song.id} result=${local?.payload?.let { if (it.isSynced) "synced" else "plain" } ?: "none"}")
        if (local != null) {
            return@coroutineScope local.toLookupOutcome()
        }

        if (!onlineLookupEnabled.value) {
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = null,
                state = LyricsLookupState.NotFound,
            )
        }

        val networkState = currentNetworkState()
        if (!networkState.hasInternet) {
            logDebug("remote skipped song=${song.id} network=${networkState.reason}")
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = CACHE_TTL_OFFLINE_MS,
                state = LyricsLookupState.Error,
            )
        }
        if (!networkState.validated) {
            logDebug("remote continuing on unvalidated network song=${song.id} network=${networkState.reason}")
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

        val bestPayload = remoteMatch?.payload

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

    private fun LocalLyricsMatch.toCacheEntry(): LyricsCacheEntry {
        return LyricsCacheEntry(
            result = LyricsResult.Found(payload),
            expiresAtMillis = System.currentTimeMillis() + POSITIVE_CACHE_TTL_MS,
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

    private fun currentNetworkState(): NetworkLookupState {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java)
            ?: return NetworkLookupState(hasInternet = false, validated = false, reason = "no_connectivity_manager")
        val network = connectivityManager.activeNetwork
            ?: return NetworkLookupState(hasInternet = false, validated = false, reason = "no_active_network")
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return NetworkLookupState(hasInternet = false, validated = false, reason = "no_capabilities")
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return NetworkLookupState(
            hasInternet = hasInternet,
            validated = validated,
            reason = when {
                !hasInternet -> "missing_internet"
                validated -> "validated"
                else -> "unvalidated"
            },
        )
    }

    private fun isNetworkSuitableForPrefetch(): Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
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
        const val FULL_REMOTE_LOOKUP_BUDGET_MS = 9_000L
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

    private data class NetworkLookupState(
        val hasInternet: Boolean,
        val validated: Boolean,
        val reason: String,
    )
}
