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
    private val lrclibProvider = LrcLibLyricsProvider()
    private val lyricsOvhProvider = LyricsOvhProvider()
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
            fetchLyrics(
                song = song,
                allowCachedNotFound = false,
                lookupMode = LyricsLookupMode.Full,
            )
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
        lookupMode: LyricsLookupMode,
    ): LyricsResult = coroutineScope {
        val identity = song.toLyricsIdentity()
        cache.get(identity, includeNotFound = allowCachedNotFound)?.let { cachedResult ->
            logDebug("cache hit for ${identity.normalizedLookupKey}: ${cachedResult.debugName()}")
            return@coroutineScope cachedResult
        }
        val ignoredNegativeCache = cache.get(identity, includeNotFound = true)
        if (!allowCachedNotFound && (ignoredNegativeCache == LyricsResult.NotFound || ignoredNegativeCache == LyricsResult.Timeout)) {
            logDebug("cached negative ignored for ${identity.normalizedLookupKey}: ${ignoredNegativeCache.debugName()}")
        }

        val existing = inFlightRequests[identity.normalizedLookupKey]
        if (existing != null) {
            return@coroutineScope existing.await().result
        }

        val request = serviceScope.async {
            runCatching {
                resolveLyrics(song, identity, lookupMode)
            }.getOrElse { throwable ->
                logDebug("lyrics lookup failed for ${identity.artist} - ${identity.title}", throwable)
                LyricsLookupOutcome(
                    result = LyricsResult.Timeout,
                    cacheTtlMs = CACHE_TTL_TIMEOUT_MS,
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
        lookupMode: LyricsLookupMode,
    ): LyricsLookupOutcome = coroutineScope {
        cache.clearExpired()
        val startedAtMs = System.currentTimeMillis()
        val totalBudgetMs = lookupMode.totalBudgetMs()
        logDebug(
            "lookup start ${identity.normalizedLookupKey} mode=$lookupMode budget=${totalBudgetMs}ms",
        )

        val local = withContext(ioDispatcher) {
            withTimeoutOrNull(LOCAL_LOOKUP_TIMEOUT_MS) {
                localLyricsResolver.resolve(song)
            }
        }
        if (local != null) {
            val payload = local.payload
            logDebug(
                "local lyrics resolved for ${identity.normalizedLookupKey} " +
                    "source=${if (payload.isSynced) "synced" else "plain"} in ${System.currentTimeMillis() - startedAtMs}ms",
            )
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.Found(payload),
                cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                state = if (payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                providerName = payload.providerName,
                confidence = payload.confidence.coerceAtLeast(100),
            )
        }

        if (!isNetworkAvailable()) {
            logDebug("offline fallback for ${identity.normalizedLookupKey}")
            return@coroutineScope LyricsLookupOutcome(
                result = LyricsResult.NotFound,
                cacheTtlMs = CACHE_TTL_OFFLINE_MS,
                state = LyricsLookupState.Error,
            )
        }

        val elapsedAfterLocalMs = System.currentTimeMillis() - startedAtMs
        val remainingBudgetMs = (totalBudgetMs - elapsedAfterLocalMs).coerceAtLeast(250L)
        val remoteOutcome = withContext(ioDispatcher) {
            withTimeoutOrNull(remainingBudgetMs) {
                resolveRemoteLyrics(identity, lookupMode)?.let { RemoteLookupResult.Match(it) }
                    ?: RemoteLookupResult.NotFound
            } ?: RemoteLookupResult.Timeout
        }
        val totalElapsedMs = System.currentTimeMillis() - startedAtMs

        return@coroutineScope when (remoteOutcome) {
            is RemoteLookupResult.Match -> {
                logDebug(
                    "remote source selected ${remoteOutcome.match.providerName} for ${identity.normalizedLookupKey} " +
                        "confidence=${remoteOutcome.match.confidence} offset=${remoteOutcome.match.payload.displayTimingOffsetMs}ms " +
                        "elapsed=${totalElapsedMs}ms",
                )
                LyricsLookupOutcome(
                    result = LyricsResult.Found(remoteOutcome.match.payload),
                    cacheTtlMs = POSITIVE_CACHE_TTL_MS,
                    state = if (remoteOutcome.match.payload.isSynced) LyricsLookupState.FoundSynced else LyricsLookupState.FoundUnsynced,
                    providerName = remoteOutcome.match.providerName,
                    confidence = remoteOutcome.match.confidence,
                )
            }

            RemoteLookupResult.Timeout -> {
                logDebug("lyrics lookup timed out for ${identity.normalizedLookupKey} after ${totalElapsedMs}ms")
                LyricsLookupOutcome(
                    result = LyricsResult.Timeout,
                    cacheTtlMs = CACHE_TTL_TIMEOUT_MS,
                    state = LyricsLookupState.Error,
                )
            }

            RemoteLookupResult.NotFound -> {
                logDebug("final NotFound source for ${identity.normalizedLookupKey} after ${totalElapsedMs}ms")
                LyricsLookupOutcome(
                    result = LyricsResult.NotFound,
                    cacheTtlMs = CACHE_TTL_NOT_FOUND_MS,
                    state = LyricsLookupState.NotFound,
                )
            }
        }
    }

    private suspend fun resolveRemoteLyrics(
        identity: LyricsIdentity,
        lookupMode: LyricsLookupMode,
    ): ProviderLyricsMatch? = coroutineScope {
        val fastVariants = buildLyricsQueryVariants(identity)
            .take(lookupMode.maxRemoteQueryVariants())
        if (fastVariants.isEmpty()) return@coroutineScope null
        logDebug(
            "remote query variants for ${identity.normalizedLookupKey}: ${fastVariants.size}",
        )

        val lrclibQuery = LyricsSearchQuery(
            identity = identity,
            variants = fastVariants,
        )
        val lrclibCandidates = runCatching {
            lrclibProvider.search(lrclibQuery)
        }.getOrElse { throwable ->
            logDebug("lrclib search failed for ${identity.normalizedLookupKey}", throwable)
            emptyList()
        }
        val lrclibMatch = selectBestCandidateMatch(
            provider = lrclibProvider,
            identity = identity,
            candidates = lrclibCandidates,
            minimumScore = if (lookupMode == LyricsLookupMode.FastPresenceCheck) FAST_MODE_MIN_SCORE else FULL_MODE_MIN_SCORE,
        )
        if (lrclibMatch != null && lrclibMatch.payload.isSynced && lrclibMatch.confidence >= HIGH_CONFIDENCE_SYNC_SCORE) {
            return@coroutineScope lrclibMatch
        }

        val ovhQuery = LyricsSearchQuery(
            identity = identity,
            variants = fastVariants.take(MAX_LYRICS_OVH_QUERY_VARIANTS),
        )
        val ovhCandidates = runCatching {
            lyricsOvhProvider.search(ovhQuery)
        }.getOrElse { throwable ->
            logDebug("lyrics.ovh search failed for ${identity.normalizedLookupKey}", throwable)
            emptyList()
        }
        val ovhMatch = selectBestCandidateMatch(
            provider = lyricsOvhProvider,
            identity = identity,
            candidates = ovhCandidates,
            minimumScore = Ovh_MIN_SCORE,
        )

        return@coroutineScope listOfNotNull(lrclibMatch, ovhMatch)
            .maxWithOrNull(compareBy<ProviderLyricsMatch>({ if (it.payload.isSynced) 1 else 0 }, { it.confidence }))
    }

    private suspend fun selectBestCandidateMatch(
        provider: LyricsProvider,
        identity: LyricsIdentity,
        candidates: List<LyricsCandidate>,
        minimumScore: Int,
    ): ProviderLyricsMatch? {
        if (candidates.isEmpty()) return null

        val rankedCandidates = candidates
            .map { candidate -> candidate to candidate.scoreAgainst(identity) }
            .sortedWith(
                compareByDescending<Pair<LyricsCandidate, Int>> { it.second }
                    .thenByDescending { if (it.first.syncedLyrics.isNotBlank()) 1 else 0 },
            )

        val acceptableCandidates = rankedCandidates.filter { (candidate, score) ->
            candidate.isAcceptableMatchFor(identity, score)
        }.ifEmpty {
            rankedCandidates.filter { (_, score) -> score >= minimumScore }
        }

        var bestMatch: ProviderLyricsMatch? = null
        acceptableCandidates.forEach { (candidate, _) ->
            val match = provider.getLyrics(candidate, identity) ?: return@forEach
            if (bestMatch == null || match.isBetterThan(bestMatch)) {
                bestMatch = match
            }
            if (match.payload.isSynced && match.confidence >= HIGH_CONFIDENCE_SYNC_SCORE) {
                return match
            }
        }
        return bestMatch
    }

    private fun ProviderLyricsMatch.isBetterThan(other: ProviderLyricsMatch?): Boolean {
        if (other == null) return true
        return when {
            payload.isSynced != other.payload.isSynced -> payload.isSynced
            confidence != other.confidence -> confidence > other.confidence
            else -> payload.lines.size > other.payload.lines.size
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun LyricsResult.debugName(): String = when (this) {
        is LyricsResult.Found -> if (payload.isSynced) "found_synced" else "found_unsynced"
        LyricsResult.NotFound -> "not_found"
        LyricsResult.Timeout -> "timeout"
    }

    private fun LyricsLookupMode.totalBudgetMs(): Long = when (this) {
        LyricsLookupMode.FastPresenceCheck -> FAST_NOT_FOUND_BUDGET_MS
        LyricsLookupMode.Full -> FULL_REMOTE_LOOKUP_BUDGET_MS
    }

    private fun LyricsLookupMode.maxRemoteQueryVariants(): Int = when (this) {
        LyricsLookupMode.FastPresenceCheck -> 3
        LyricsLookupMode.Full -> MAX_FAST_REMOTE_QUERY_VARIANTS
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

    private sealed interface RemoteLookupResult {
        data class Match(val match: ProviderLyricsMatch) : RemoteLookupResult
        data object NotFound : RemoteLookupResult
        data object Timeout : RemoteLookupResult
    }

    private companion object {
        const val TAG = "LyricsRepository"
        const val LOCAL_LOOKUP_TIMEOUT_MS = 250L
        const val FAST_NOT_FOUND_BUDGET_MS = 2_000L
        const val FULL_REMOTE_LOOKUP_BUDGET_MS = 3_800L
        const val MAX_FAST_REMOTE_QUERY_VARIANTS = 5
        const val POSITIVE_CACHE_TTL_MS = 30L * 24L * 60L * 60L * 1000L
        const val CACHE_TTL_NOT_FOUND_MS = 30_000L
        const val CACHE_TTL_TIMEOUT_MS = 15_000L
        const val CACHE_TTL_OFFLINE_MS = 90_000L
        const val FULL_MODE_MIN_SCORE = 78
        const val FAST_MODE_MIN_SCORE = 82
        const val Ovh_MIN_SCORE = 74
        const val HIGH_CONFIDENCE_SYNC_SCORE = 88
        const val MAX_LYRICS_OVH_QUERY_VARIANTS = 2
    }
}
