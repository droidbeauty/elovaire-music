package elovaire.music.droidbeauty.app.data.playback.library

import android.os.Looper
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommand
import elovaire.music.droidbeauty.app.data.playback.PlaybackCommandOrigin
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager

/** Domain-facing catalog capability used by the Media3 adapter. */
internal interface MediaCatalogPort : MediaLibraryBrowser, MediaLibraryCommandResolver

internal class CompositeMediaCatalogPort(
    private val browser: MediaLibraryBrowser,
    private val commandResolver: MediaLibraryCommandResolver,
) : MediaCatalogPort {
    override fun childrenOf(id: ElovaireMediaId) = browser.childrenOf(id)
    override fun childrenOfPage(id: ElovaireMediaId, page: Int, pageSize: Int) =
        browser.childrenOfPage(id, page, pageSize)
    override fun item(mediaId: String) = browser.item(mediaId)
    override fun search(query: String, limit: Int) = browser.search(query, limit)
    override fun searchRevision() = browser.searchRevision()
    override fun searchPage(query: String, offset: Int, limit: Int) = browser.searchPage(query, offset, limit)
    override fun searchCount(query: String) = browser.searchCount(query)
    override fun resolvePlayableQueue(mediaId: String) = commandResolver.resolvePlayableQueue(mediaId)
    override fun resolveSearchQueue(query: String) = commandResolver.resolveSearchQueue(query)
    override fun defaultPlayableQueue() = commandResolver.defaultPlayableQueue()
    override fun resumptionQueue() = commandResolver.resumptionQueue()
}

/** Playback capability needed by a Media3 controller without exposing PlaybackManager. */
internal interface MediaPlaybackPort {
    val applicationLooper: Looper

    fun stageExternalQueue(queue: ResolvedPlayableQueue, startIndex: Int)

    fun restoreSession(
        songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
        currentIndex: Int,
        persisted: elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession,
    )

    fun dispatchPlaybackCommand(command: PlaybackCommand, origin: PlaybackCommandOrigin)
}

internal class PlaybackManagerMediaPlaybackPort(
    private val playbackManager: PlaybackManager,
) : MediaPlaybackPort {
    override val applicationLooper: Looper
        get() = playbackManager.playerInstance.applicationLooper

    override fun stageExternalQueue(queue: ResolvedPlayableQueue, startIndex: Int) {
        playbackManager.stageExternalQueue(
            songs = queue.queue,
            startIndex = startIndex,
            sourceLabel = queue.sourceLabel,
            sourcePlaylistId = queue.sourcePlaylistId,
            audiobookContext = queue.audiobookContext,
        )
    }

    override fun restoreSession(
        songs: List<elovaire.music.droidbeauty.app.domain.model.Song>,
        currentIndex: Int,
        persisted: elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession,
    ) = playbackManager.restoreSession(songs, currentIndex, persisted)

    override fun dispatchPlaybackCommand(command: PlaybackCommand, origin: PlaybackCommandOrigin) {
        playbackManager.dispatchPlaybackCommand(command, origin)
    }
}

internal interface MediaLibraryReadinessPort {
    val ready: ListenableFuture<Unit>

    fun isOperational(): Boolean
}

internal class StartupReadinessPort(
    override val ready: ListenableFuture<Unit> = Futures.immediateFuture(Unit),
    private val operational: () -> Boolean = { true },
) : MediaLibraryReadinessPort {
    override fun isOperational(): Boolean = operational()
}
