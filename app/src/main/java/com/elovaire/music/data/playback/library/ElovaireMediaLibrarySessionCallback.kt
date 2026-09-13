package elovaire.music.droidbeauty.app.data.playback.library

import android.content.Intent
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import java.util.LinkedHashMap
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommand
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommandOrigin
import elovaire.music.droidbeauty.app.data.playback.pendingMediaButtonResumption
import elovaire.music.droidbeauty.app.data.playback.toPlayerRepeatMode
import elovaire.music.droidbeauty.app.core.getParcelableExtraCompat
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery

@OptIn(UnstableApi::class)
internal class ElovaireMediaLibrarySessionCallback(
    private val catalog: MediaCatalogPort,
    private val playback: MediaPlaybackPort,
    private val readExecutor: MediaLibraryReadExecutor = MediaLibraryReadExecutor(),
    private val readiness: MediaLibraryReadinessPort = StartupReadinessPort(),
) : MediaLibrarySession.Callback {
    private val queueResolutionGeneration = AtomicLong(0L)
    private val searchResults = MediaLibrarySearchCache()
    private val playerHandler = android.os.Handler(playback.applicationLooper)
    private val playerExecutor = Executor { command ->
        if (!playerHandler.post(command)) throw RejectedExecutionException("Player looper is unavailable")
    }
    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(LibraryResult.ofItem(ElovaireMediaItems.root(), params))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        controller: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val parsed = ElovaireMediaIds.parse(parentId)
            ?: return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        if (!MediaLibraryRequestPolicy.acceptsPage(page, pageSize)) {
            return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        }
        return submitRead {
            LibraryResult.ofItemList(catalog.childrenOfPage(parsed, page, pageSize), params)
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        controller: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return submitRead {
            val item = catalog.item(mediaId)
                ?: return@submitRead LibraryResult.ofError(invalidMediaIdError())
            LibraryResult.ofItem(item, null)
        }
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        if (!MediaLibraryRequestPolicy.acceptsSearchQuery(query)) {
            return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        }
        val count = submitRead {
            searchResults.getCount(browser, query, catalog.searchRevision()) {
                catalog.searchCount(query)
            }
        }
        return transformOnPlayerLooper(count) { itemCount ->
            session.notifySearchResultChanged(browser, query, itemCount, params)
            LibraryResult.ofVoid(params)
        }
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        controller: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        if (
            !MediaLibraryRequestPolicy.acceptsSearchQuery(query) ||
            !MediaLibraryRequestPolicy.acceptsPage(page, pageSize)
        ) {
            return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        }
        return submitRead {
            val offset = page.toLong() * pageSize.toLong()
            if (offset >= MediaLibraryRequestPolicy.MAX_SEARCH_RESULT_ITEMS) {
                return@submitRead LibraryResult.ofItemList(ImmutableList.of(), params)
            }
            val items = searchResults.getPage(
                controller = controller,
                query = query,
                revision = catalog.searchRevision(),
                offset = offset.toInt(),
                limit = pageSize,
            ) {
                catalog.searchPage(query, offset.toInt(), pageSize)
            }
            LibraryResult.ofItemList(
                ImmutableList.copyOf(items),
                params,
            )
        }
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        if (!MediaLibraryRequestPolicy.acceptsStartPositionMs(startPositionMs)) {
            return Futures.immediateFuture(emptyMediaItemsWithStartPosition())
        }
        val requested = mediaItems.getOrNull(startIndex.coerceAtLeast(0)) ?: mediaItems.firstOrNull()
        val requestGeneration = queueResolutionGeneration.incrementAndGet()
        val resolved = submitRead {
            requested?.let {
                catalog.resolvePlayableQueue(it.mediaId)
                    ?: it.requestMetadata.searchQuery?.let(catalog::resolveSearchQueue)
                    ?: catalog.defaultPlayableQueue().takeIf { _ ->
                        it.mediaId.isBlank() && it.requestMetadata.searchQuery.isNullOrBlank()
                    }
            } ?: catalog.defaultPlayableQueue().takeIf { requested == null }
        }
        return transformOnPlayerLooper(resolved) { queue ->
            if (requestGeneration != queueResolutionGeneration.get()) {
                return@transformOnPlayerLooper emptyMediaItemsWithStartPosition()
            }
            if (queue == null) return@transformOnPlayerLooper emptyMediaItemsWithStartPosition()
            val result = queue.toMediaItemsWithStartPosition(startPositionMs)
            // MediaSession applies these items and dispatches prepare/play after this callback returns.
            playback.stageExternalQueue(queue, result.startIndex)
            result
        }
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val pending = pendingMediaButtonResumption(consume = isForPlayback)
        val requestGeneration = queueResolutionGeneration.incrementAndGet()
        val resolved = submitRead { pending?.queue ?: catalog.resumptionQueue() }
        return transformOnPlayerLooper(resolved) { queue ->
            if (requestGeneration != queueResolutionGeneration.get()) {
                return@transformOnPlayerLooper emptyMediaItemsWithStartPosition()
            }
            if (queue == null) return@transformOnPlayerLooper emptyMediaItemsWithStartPosition()
            val result = queue.toMediaItemsWithStartPosition(pending?.persisted?.positionMs ?: C.TIME_UNSET)
            if (isForPlayback) {
                if (pending != null) {
                    mediaSession.player.repeatMode = pending.persisted.repeatMode.toPlayerRepeatMode()
                    mediaSession.player.shuffleModeEnabled = pending.persisted.shuffleEnabled
                }
                playback.stageExternalQueue(queue, result.startIndex)
            }
            result
        }
    }

    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent,
    ): Boolean {
        val keyEvent = intent.getParcelableExtraCompat<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            ?: return false
        if (
            keyEvent.action != KeyEvent.ACTION_DOWN ||
            keyEvent.repeatCount != 0 ||
            keyEvent.keyCode !in PLAYBACK_START_KEY_CODES
        ) {
            return false
        }
        if (!readiness.ready.completedSuccessfully() || !readiness.isOperational()) return false
        val pending = pendingMediaButtonResumption(consume = true) ?: return false
        val startIndex = pending.queue.queue.indexOfFirst { it.id == pending.queue.startSong.id }
            .coerceAtLeast(0)
        playback.restoreSession(pending.queue.queue, startIndex, pending.persisted)
        playback.dispatchPlaybackCommand(PlaybackCommand.Play, PlaybackCommandOrigin.ExternalController)
        return true
    }

    private fun ResolvedPlayableQueue.toMediaItemsWithStartPosition(startPositionMs: Long): MediaSession.MediaItemsWithStartPosition {
        val resolvedStartIndex = queue.indexOfFirst { it.id == startSong.id }.coerceAtLeast(0)
        val resolvedStartPositionMs = when {
            startPositionMs == C.TIME_UNSET -> C.TIME_UNSET
            startPositionMs <= 0L -> 0L
            startSong.durationMs > 0L -> startPositionMs.coerceAtMost(startSong.durationMs)
            else -> startPositionMs
        }
        return MediaSession.MediaItemsWithStartPosition(
            queue.map(ElovaireMediaItems::playable),
            resolvedStartIndex,
            resolvedStartPositionMs,
        )
    }

    private fun <T> submitRead(task: () -> T): ListenableFuture<T> {
        return Futures.transformAsync(
            readiness.ready,
            { submitReadDirect(task) },
            MoreExecutors.directExecutor(),
        )
    }

    private fun <T> submitReadDirect(task: () -> T): ListenableFuture<T> {
        return try {
            readExecutor.submit(Callable(task))
        } catch (_: RejectedExecutionException) {
            Futures.immediateFailedFuture(IllegalStateException("Media library query executor is closed"))
        }
    }

    private fun <T, R> transformOnPlayerLooper(
        source: ListenableFuture<T>,
        transform: (T) -> R,
    ): ListenableFuture<R> {
        return Futures.transform(source, transform, playerExecutor)
    }

    private fun invalidMediaIdError(): SessionError {
        return SessionError(SessionError.ERROR_BAD_VALUE, "This item is no longer available.")
    }

    private companion object {
        val PLAYBACK_START_KEY_CODES = setOf(
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK,
        )
    }
}

internal fun ListenableFuture<*>.completedSuccessfully(): Boolean {
    if (!isDone || isCancelled) return false
    return runCatching { get() }.isSuccess
}

private class MediaLibrarySearchCache {
    private var activeRevision: String? = null
    private val counts = object : LinkedHashMap<SearchKey, Int>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<SearchKey, Int>): Boolean =
            size > MAX_COUNT_ENTRIES
    }
    private val pages = object : LinkedHashMap<PageKey, List<MediaItem>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<PageKey, List<MediaItem>>): Boolean =
            size > MAX_PAGE_ENTRIES
    }

    fun getCount(
        controller: MediaSession.ControllerInfo,
        query: String,
        revision: String,
        loader: () -> Int,
    ): Int {
        val key = SearchKey(controller, NormalizedSearchQuery.from(query).value, revision)
        synchronized(this) {
            prepareRevisionLocked(revision)
            counts[key]
        }?.let { return it }
        val loaded = loader()
        synchronized(this) {
            if (activeRevision != revision) return loaded
            return counts[key] ?: loaded.also { counts[key] = it }
        }
    }

    fun getPage(
        controller: MediaSession.ControllerInfo,
        query: String,
        revision: String,
        offset: Int,
        limit: Int,
        loader: () -> List<MediaItem>,
    ): List<MediaItem> {
        val key = PageKey(controller, NormalizedSearchQuery.from(query).value, revision, offset, limit)
        synchronized(this) {
            prepareRevisionLocked(revision)
            pages[key]
        }?.let { return it }
        val loaded = loader().toList()
        synchronized(this) {
            if (activeRevision != revision) return loaded
            return pages[key] ?: loaded.also { pages[key] = it }
        }
    }

    private fun prepareRevisionLocked(revision: String) {
        if (activeRevision == revision) return
        activeRevision = revision
        counts.clear()
        pages.clear()
    }

    private data class SearchKey(
        val controller: MediaSession.ControllerInfo,
        val query: String,
        val revision: String,
    )

    private data class PageKey(
        val controller: MediaSession.ControllerInfo,
        val query: String,
        val revision: String,
        val offset: Int,
        val limit: Int,
    )

    private companion object {
        const val MAX_COUNT_ENTRIES = 8
        const val MAX_PAGE_ENTRIES = 16
    }
}

/** Bounded because Media3 can issue concurrent browser requests from multiple controllers. */
internal class MediaLibraryReadExecutor private constructor(
    private val delegate: ListeningExecutorService,
) : AutoCloseable {
    constructor() : this(MoreExecutors.newDirectExecutorService())

    fun <T> submit(task: Callable<T>): ListenableFuture<T> = delegate.submit(task)

    override fun close() {
        delegate.shutdownNow().forEach { runnable ->
            (runnable as? Future<*>)?.cancel(false)
        }
    }

    companion object {
        fun bounded(): MediaLibraryReadExecutor {
            val executor = ThreadPoolExecutor(
                1,
                2,
                30L,
                TimeUnit.SECONDS,
                ArrayBlockingQueue(32),
                Executors.defaultThreadFactory(),
                ThreadPoolExecutor.AbortPolicy(),
            )
            return MediaLibraryReadExecutor(MoreExecutors.listeningDecorator(executor))
        }
    }
}

@OptIn(UnstableApi::class)
internal class MediaLibraryCallbackRouter : MediaLibrarySession.Callback {
    @Volatile
    private var delegate: MediaLibrarySession.Callback? = null

    fun setDelegate(delegate: MediaLibrarySession.Callback) {
        this.delegate = delegate
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return delegate?.onGetLibraryRoot(session, browser, params)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_SESSION_SETUP_REQUIRED))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return delegate?.onGetChildren(session, browser, parentId, page, pageSize, params)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_SESSION_SETUP_REQUIRED))
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return delegate?.onGetItem(session, browser, mediaId)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_SESSION_SETUP_REQUIRED))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        return delegate?.onSearch(session, browser, query, params)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_SESSION_SETUP_REQUIRED))
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return delegate?.onGetSearchResult(session, browser, query, page, pageSize, params)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_SESSION_SETUP_REQUIRED))
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return delegate?.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
            ?: Futures.immediateFuture(emptyMediaItemsWithStartPosition())
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return delegate?.onPlaybackResumption(mediaSession, controller, isForPlayback)
            ?: Futures.immediateFuture(emptyMediaItemsWithStartPosition())
    }

    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent,
    ): Boolean {
        return delegate?.onMediaButtonEvent(session, controllerInfo, intent) ?: false
    }
}

@OptIn(UnstableApi::class)
internal fun emptyMediaItemsWithStartPosition(): MediaSession.MediaItemsWithStartPosition {
    return MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
}
