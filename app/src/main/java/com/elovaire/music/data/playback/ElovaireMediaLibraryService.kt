package elovaire.music.droidbeauty.app.data.playback

import android.content.Intent
import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import elovaire.music.droidbeauty.app.ElovaireApp

@OptIn(UnstableApi::class)
class ElovaireMediaLibraryService : MediaLibraryService() {
    private val mainHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(mainLooper) }
    private val mediaButtonStateCheck = Runnable {
        val player = (application as ElovaireApp).container.playbackManager.playerInstance
        if (!player.playWhenReady && !isPlaybackOngoing) {
            pauseAllPlayersAndStopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            mainHandler.removeCallbacks(mediaButtonStateCheck)
            mainHandler.post(mediaButtonStateCheck)
        }
        return result
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaLibrarySession? {
        return (application as ElovaireApp).container
            .also { it.startPlayback() }
            .playbackManager
            .mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val playbackManager = (application as ElovaireApp).container
            .also { it.startPlayback() }
            .playbackManager
        if (!playbackManager.state.value.transportShowsPause) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }
}
