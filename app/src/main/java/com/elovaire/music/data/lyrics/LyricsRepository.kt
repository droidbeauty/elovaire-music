package elovaire.music.app.data.lyrics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import elovaire.music.app.BuildConfig
import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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
    private val providers: List<LyricsProvider> = listOf(LrcLibLyricsProvider())
    private val serviceScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<LyricsLookupOutcome>>()

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean,
    ): LyricsResult? {
        return cache.get(song.toLyricsIdentity(), includeNotFound)
    }

    fun isLookupInFlight(song: Song): Boolean {
        return inFlightRequests.containsKey(song.toLyricsIdentity().normalizedLookupKey)
    }

    fun prefetchLyrics(song: Song) {
        val identity = song.toLyricsIdentity()
        if (cache.get(identity, includeNotFound = false) != null || inFlightRequests.containsKey(identity.normalizedLookupKey)) {
            return
        }
        serviceScope.launch {
            fetchLyrics(song, allowCachedNotFound = false)
        }
    }

    fun cancelObsoleteRequests(keepSongs: List<Song?>) {
        val keepKeys = keepSongs
            .filterNotNull()
            .mapTo(mutableSetOf()) { it.toLyricsIdentity().normalizedLookupKey }
        inFlightRequests.entries.removeIf { (key, request) ->
            val obsolete = key !in keepKeys
            if (obsolete) {
                request.cancel()
            }
            obsolete
        }
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean,
    ): LyricsResult = coroutineScope {
        val identity = song.toLyricsIdentity()
        cache.get(identity, includeNotFound = allowCachedNotFound)?.let { return@coroutineScope it }

        val existing = inFlightRequests[identity.normalizedLookupKey]
        if (existing != null) {
            return@coroutineScope existing.await().result
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song, identity)
            }.getOrElse { throwable ->
                logDebug("lyrics lookup failed for ${identity.artist} - ${identity.title}", throwable)
                LyricsLookupOutcome(
                    result = LyricsResult.NotFound,
                    cacheTtlMs = null,
                    state = LyricsLookupState.Error,
                )
            }
        }
        val activeRequest = inFlightRequests.putIfAbsent(identity.normalizedLookupKey, request) ?: request
        if (activeRequest !== request) {
            request.cancel()
        }

        try {
            val outcome = activeRequest.await()
            outcome.cacheTtlMs?.let { ttl ->
                cache.put(
                    identity = identity,
                    entry = LyricsCacheEntry(
                        result = outcome.result,
                        expiresAtMillis = System.currentTimeMillis() + ttl,
                        providerName = outcome.providerName,
                        confidence = outcome.confidence,
                    ),
                )
            }
            outcome.result
        } finally {
            inFlightRequests.remove(identity.normalizedLookupKey, activeRequest)
        }
    }

    private suspend fun resolveLyrics(
        song: Song,
        identity: LyricsIdentity,
    ): LyricsLookupOutcome = coroutineScope {
        cache.clearExpired()

        val localDeferred = async(ioDispatcher) {
            localLyricsResolver.resolve(song)
        }

        if (!isNetworkAvailable()) {
            val local = localDeferred.await()
            if (local != null) {
                return@coroutineScope LyricsLookupOutcome(
                    result = LyricsResult.Found(local.payload),
                    cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                    state = if (local.payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                    confidence = 100,
                )
            }
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = null,
                state = LyricsLookupState.NotFound,
            )
        }

        val remoteDeferred = async(ioDispatcher) {
            withTimeoutOrNull(REMOTE_LOOKUP_TIMEOUT_MS) {
                resolveRemoteLyrics(identity)
            }
        }

        val local = localDeferred.await()
        if (local?.payload?.isSynced == true) {
            remoteDeferred.cancel()
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(local.payload),
                cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                state = LyricsLookupState.FoundSynced,
                confidence = 100,
            )
        }

        val remote = remoteDeferred.await()
        if (remote != null) {
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(remote.payload),
                cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                state = if (remote.payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                providerName = remote.providerName,
                confidence = remote.confidence,
            )
        }

        if (local != null) {
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(local.payload),
                cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                state = if (local.payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                confidence = 100,
            )
        }

        return@coroutineScope LyricsLookupOutcome(
            result = LyricsResult.NotFound,
            cacheTtlMs = NOT_FOUND_CACHE_TTL_MS,
            state = LyricsLookupState.NotFound,
        )
    }

    private suspend fun resolveRemoteLyrics(identity: LyricsIdentity): ProviderLyricsMatch? {
        val query = LyricsSearchQuery(
            identity = identity,
            variants = buildLyricsQueryVariants(identity),
        )

        providers.forEach { provider ->
            val candidates = runCatching { provider.search(query) }.getOrNull().orEmpty()
            if (candidates.isEmpty()) return@forEach

            val rankedCandidates = candidates
                .map { candidate -> candidate to candidate.scoreAgainst(identity) }
                .sortedWith(
                    compareByDescending<Pair<LyricsCandidate, Int>> { it.second }
                        .thenByDescending { if (it.first.syncedLyrics.isNotBlank()) 1 else 0 },
                )

            val acceptableCandidates = rankedCandidates.filter { (candidate, score) ->
                candidate.isAcceptableMatchFor(identity, score)
            }.ifEmpty {
                rankedCandidates.filter { (_, score) -> score >= MIN_FALLBACK_SCORE }
            }

            acceptableCandidates.forEach { (candidate, _) ->
                val match = runCatching { provider.getLyrics(candidate, identity) }.getOrNull() ?: return@forEach
                if (match.payload.isSynced || match.payload.lines.isNotEmpty()) {
                    return match
                }
            }
        }

        return null
    }

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
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1000L
        const val NOT_FOUND_CACHE_TTL_MS = 30L * 60L * 1000L
        const val REMOTE_LOOKUP_TIMEOUT_MS = 3_200L
        const val MIN_FALLBACK_SCORE = 78
    }
}
