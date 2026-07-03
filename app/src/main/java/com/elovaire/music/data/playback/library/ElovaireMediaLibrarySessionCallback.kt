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
    private val mediaTree: ElovaireMediaTree,
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
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val parsed = ElovaireMediaIds.parse(parentId)
            ?: return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        if (page < 0 || pageSize < 0) {
            return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        }
        return Futures.immediateFuture(
            LibraryResult.ofItemList(pageItems(mediaTree.childrenOf(parsed), page, pageSize), params),
        )
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = mediaTree.item(mediaId)
            ?: return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        return Futures.immediateFuture(LibraryResult.ofItem(item, null))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        return Futures.immediateFuture(LibraryResult.ofVoid(params))
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        if (page < 0 || pageSize < 0) {
            return Futures.immediateFuture(LibraryResult.ofError(invalidMediaIdError()))
        }
        return Futures.immediateFuture(
            LibraryResult.ofItemList(pageItems(mediaTree.search(query), page, pageSize), params),
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
            mediaTree.resolvePlayableQueue(it.mediaId)
                ?: it.requestMetadata.searchQuery?.let(mediaTree::resolveSearchQueue)
                ?: mediaTree.defaultPlayableQueue().takeIf { _ ->
                    it.mediaId.isBlank() && it.requestMetadata.searchQuery.isNullOrBlank()
                }
        } ?: mediaTree.defaultPlayableQueue().takeIf { requested == null }
        if (resolved != null) {
            playResolvedQueue(resolved, startPositionMs)
            return Futures.immediateFuture(resolved.toMediaItemsWithStartPosition(startPositionMs))
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
        val resolved = mediaTree.resumptionQueue()
            ?: return Futures.immediateFuture(emptyMediaItemsWithStartPosition())
        if (isForPlayback) {
            playResolvedQueue(resolved, C.TIME_UNSET)
        }
        return Futures.immediateFuture(resolved.toMediaItemsWithStartPosition(C.TIME_UNSET))
    }

    private fun playResolvedQueue(
        resolved: ResolvedPlayableQueue,
        startPositionMs: Long,
    ) {
        playbackManager.playSong(
            song = resolved.startSong,
            collection = resolved.queue,
            sourceLabel = resolved.sourceLabel,
            shuffleEnabled = playbackManager.state.value.shuffleEnabled,
            sourcePlaylistId = resolved.sourcePlaylistId,
        )
        if (startPositionMs > 0L) {
            playbackManager.seekTo(startPositionMs.coerceAtMost(resolved.startSong.durationMs.takeIf { it > 0 } ?: startPositionMs))
        }
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
        if (page < 0 || pageSize <= 0) return items
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
