package elovaire.music.droidbeauty.app.data.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import elovaire.music.droidbeauty.app.ElovaireApp

@OptIn(UnstableApi::class)
class ElovaireMediaSessionService : MediaSessionService() {
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaSession? {
        return (application as ElovaireApp)
            .container
            .playbackManager
            .mediaSessionForExternalControllers
    }
}
