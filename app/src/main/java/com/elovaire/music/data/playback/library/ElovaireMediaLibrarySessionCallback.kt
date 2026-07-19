package elovaire.music.droidbeauty.app.data.playback.library

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
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager

@OptIn(UnstableApi::class)
internal class ElovaireMediaLibrarySessionCallback(
    private val browser: MediaLibraryBrowser,
    private val commandResolver: MediaLibraryCommandResolver,
    private val playbackManager: PlaybackManager,
) : MediaLibrarySession.Callback {
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
        return Futures.immediateFuture(
            LibraryResult.ofItemList(pageItems(browser.childrenOf(parsed), page, pageSize), params),
        )
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        controller: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = browser.item(mediaId)
            ?: return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        return Futures.immediateFuture(LibraryResult.ofItem(item, null))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        return if (MediaLibraryRequestPolicy.acceptsSearchQuery(query)) {
            Futures.immediateFuture(LibraryResult.ofVoid(params))
        } else {
            Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
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
        return Futures.immediateFuture(
            LibraryResult.ofItemList(pageItems(browser.search(query), page, pageSize), params),
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val requested = mediaItems.getOrNull(startIndex.coerceAtLeast(0)) ?: mediaItems.firstOrNull()
        val resolved = requested?.let {
            commandResolver.resolvePlayableQueue(it.mediaId)
                ?: it.requestMetadata.searchQuery?.let(commandResolver::resolveSearchQueue)
                ?: commandResolver.defaultPlayableQueue().takeIf { _ ->
                    it.mediaId.isBlank() && it.requestMetadata.searchQuery.isNullOrBlank()
                }
        } ?: commandResolver.defaultPlayableQueue().takeIf { requested == null }
        if (resolved != null) {
            val result = resolved.toMediaItemsWithStartPosition(startPositionMs)
            // MediaSession applies these items and dispatches prepare/play after this callback returns.
            playbackManager.stageExternalQueue(
                songs = resolved.queue,
                startIndex = result.startIndex,
                sourceLabel = resolved.sourceLabel,
                sourcePlaylistId = resolved.sourcePlaylistId,
            )
            return Futures.immediateFuture(result)
        }
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L),
        )
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val resolved = commandResolver.resumptionQueue()
            ?: return Futures.immediateFuture(emptyMediaItemsWithStartPosition())
        val result = resolved.toMediaItemsWithStartPosition(C.TIME_UNSET)
        if (isForPlayback) {
            playbackManager.stageExternalQueue(
                songs = resolved.queue,
                startIndex = result.startIndex,
                sourceLabel = resolved.sourceLabel,
                sourcePlaylistId = resolved.sourcePlaylistId,
            )
        }
        return Futures.immediateFuture(result)
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
            queue.map(ElovaireMediaItems::song),
            resolvedStartIndex,
            resolvedStartPositionMs,
        )
    }

    private fun pageItems(items: List<MediaItem>, page: Int, pageSize: Int): List<MediaItem> {
        val from = page.toLong() * pageSize.toLong()
        if (from >= items.size) return emptyList()
        val to = (from + pageSize.toLong()).coerceAtMost(items.size.toLong())
        return items.subList(from.toInt(), to.toInt())
    }

    private fun invalidMediaIdError(): SessionError {
        return SessionError(SessionError.ERROR_BAD_VALUE, "This item is no longer available.")
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
            ?: Futures.immediateFuture(LibraryResult.ofItemList(emptyList(), params))
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return delegate?.onGetItem(session, browser, mediaId)
            ?: Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        return delegate?.onSearch(session, browser, query, params)
            ?: Futures.immediateFuture(LibraryResult.ofVoid(params))
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
            ?: Futures.immediateFuture(LibraryResult.ofItemList(emptyList(), params))
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return delegate?.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
            ?: Futures.immediateFuture(MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs))
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return delegate?.onPlaybackResumption(mediaSession, controller, isForPlayback)
            ?: Futures.immediateFuture(emptyMediaItemsWithStartPosition())
    }
}

@OptIn(UnstableApi::class)
internal fun emptyMediaItemsWithStartPosition(): MediaSession.MediaItemsWithStartPosition {
    return MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
}
