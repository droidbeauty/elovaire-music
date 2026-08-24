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
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommand
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommandOrigin
import elovaire.music.droidbeauty.app.data.playback.pendingMediaButtonResumption
import elovaire.music.droidbeauty.app.data.playback.toPlayerRepeatMode
import elovaire.music.droidbeauty.app.core.getParcelableExtraCompat

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
            LibraryResult.ofItemList(browser.childrenOfPage(parsed, page, pageSize), params),
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
        if (!MediaLibraryRequestPolicy.acceptsStartPositionMs(startPositionMs)) {
            return Futures.immediateFuture(emptyMediaItemsWithStartPosition())
        }
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
        val pending = pendingMediaButtonResumption(consume = isForPlayback)
        val resolved = pending?.queue ?: commandResolver.resumptionQueue()
            ?: return Futures.immediateFuture(emptyMediaItemsWithStartPosition())
        val result = resolved.toMediaItemsWithStartPosition(pending?.persisted?.positionMs ?: C.TIME_UNSET)
        if (isForPlayback) {
            if (pending != null) {
                mediaSession.player.repeatMode = pending.persisted.repeatMode.toPlayerRepeatMode()
                mediaSession.player.shuffleModeEnabled = pending.persisted.shuffleEnabled
            }
            playbackManager.stageExternalQueue(
                songs = resolved.queue,
                startIndex = result.startIndex,
                sourceLabel = resolved.sourceLabel,
                sourcePlaylistId = resolved.sourcePlaylistId,
            )
        }
        return Futures.immediateFuture(result)
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
        val pending = pendingMediaButtonResumption(consume = true) ?: return false
        val startIndex = pending.queue.queue.indexOfFirst { it.id == pending.queue.startSong.id }
            .coerceAtLeast(0)
        playbackManager.restoreSession(pending.queue.queue, startIndex, pending.persisted)
        playbackManager.dispatchPlaybackCommand(PlaybackCommand.Play, PlaybackCommandOrigin.ExternalController)
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

    private companion object {
        val PLAYBACK_START_KEY_CODES = setOf(
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK,
        )
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
