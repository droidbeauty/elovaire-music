package elovaire.music.droidbeauty.app

import android.content.Intent
import androidx.activity.ComponentActivity
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION
import elovaire.music.droidbeauty.app.data.playback.ExternalAudioIntentHandler

internal class MainIntentHandler(
    private val activity: ComponentActivity,
    private val container: AppContainer,
) {
    fun handle(intent: Intent?) {
        handleNotificationIntent(intent)
        handleExternalAudioIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, false) == true) {
            container.requestOpenNowPlaying()
            intent.removeExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION)
        }
    }

    private fun handleExternalAudioIntent(intent: Intent?) {
        val song = ExternalAudioIntentHandler.buildSong(activity, intent) ?: return
        container.playbackManager.playSong(
            song = song,
            collection = listOf(song),
            sourceLabel = "External audio",
        )
        container.requestOpenNowPlaying()
        activity.setIntent(
            Intent(activity, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            },
        )
    }
}
