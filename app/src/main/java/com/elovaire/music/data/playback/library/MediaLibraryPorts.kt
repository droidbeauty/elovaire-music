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
