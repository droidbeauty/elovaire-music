package elovaire.music.droidbeauty.app.data.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import elovaire.music.droidbeauty.app.MainActivity
import elovaire.music.droidbeauty.app.data.playback.library.MediaLibraryCallbackRouter

@OptIn(UnstableApi::class)
internal class PlaybackSessionOwner(
    context: Context,
    initialPlayer: Player,
) {
    private val callbackRouter = MediaLibraryCallbackRouter()
    private val session = MediaLibrarySession.Builder(context, initialPlayer, callbackRouter)
        .setSessionActivity(
            PendingIntent.getActivity(
                context,
                3101,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, true)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    val mediaLibrarySession: MediaLibrarySession
        get() = session

    val mediaSessionToken
        get() = session.token

    val platformMediaSessionToken
        get() = session.platformToken

    fun setMediaLibrarySessionCallback(callback: MediaLibrarySession.Callback) {
        callbackRouter.setDelegate(callback)
    }

    fun setPlayer(player: Player) {
        session.setPlayer(player)
    }

    fun release() {
        session.release()
    }
}
