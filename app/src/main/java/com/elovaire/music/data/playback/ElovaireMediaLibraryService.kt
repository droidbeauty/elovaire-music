package elovaire.music.droidbeauty.app.data.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import elovaire.music.droidbeauty.app.ElovaireApp

@OptIn(UnstableApi::class)
class ElovaireMediaLibraryService : MediaLibraryService() {
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaLibrarySession? {
        return (application as ElovaireApp)
            .container
            .playbackManager
            .mediaLibrarySession
    }
}
