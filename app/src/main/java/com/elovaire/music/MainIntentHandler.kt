package elovaire.music.droidbeauty.app

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.AppShortcutCommand
import elovaire.music.droidbeauty.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION
import elovaire.music.droidbeauty.app.data.playback.ExternalAudioIntentHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class MainIntentHandler(
    private val activity: ComponentActivity,
    private val container: AppContainer,
) {
    private var externalAudioJob: Job? = null

    fun handle(intent: Intent?) {
        handleAppShortcut(intent)
        handleNotificationIntent(intent)
        handleExternalAudioIntent(intent)
    }

    fun release() {
        externalAudioJob?.cancel()
        externalAudioJob = null
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, false) == true) {
            container.requestOpenNowPlaying()
            intent.removeExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION)
        }
    }

    private fun handleAppShortcut(intent: Intent?) {
        val command = when (intent?.action) {
            ACTION_LAST_PLAYED -> AppShortcutCommand.LastPlayed
            ACTION_ALBUMS -> AppShortcutCommand.Albums
            ACTION_PLAYLISTS -> AppShortcutCommand.Playlists
            ACTION_SEARCH -> AppShortcutCommand.Search
            else -> return
        }
        container.requestAppShortcut(command)
        intent.action = Intent.ACTION_MAIN
    }

    private fun handleExternalAudioIntent(intent: Intent?) {
        if (!ExternalAudioIntentHandler.canHandle(intent)) return
        val request = intent?.let(::Intent) ?: return
        externalAudioJob?.cancel()
        val job = activity.lifecycleScope.launch {
            val song = ExternalAudioIntentHandler.buildSong(activity.applicationContext, request) ?: return@launch
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
        externalAudioJob = job
        job.invokeOnCompletion {
            if (externalAudioJob == job) externalAudioJob = null
        }
    }

    private companion object {
        const val ACTION_LAST_PLAYED = "elovaire.music.droidbeauty.app.action.LAST_PLAYED"
        const val ACTION_ALBUMS = "elovaire.music.droidbeauty.app.action.ALBUMS"
        const val ACTION_PLAYLISTS = "elovaire.music.droidbeauty.app.action.PLAYLISTS"
        const val ACTION_SEARCH = "elovaire.music.droidbeauty.app.action.SEARCH"
    }
}
