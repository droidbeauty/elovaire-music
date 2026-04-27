package elovaire.music.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION
import elovaire.music.app.ui.screens.ElovaireRoot
import elovaire.music.app.ui.theme.ElovaireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as ElovaireApp).container
        handleNotificationIntent()
        setContent {
            val themeMode = container.preferenceStore.themeMode.collectAsStateWithLifecycle()
            val textSizePreset = container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
            ElovaireTheme(
                themeMode = themeMode.value,
                textSizePreset = textSizePreset.value,
            ) {
                ElovaireRoot(container = container)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, false) == true) {
            (application as ElovaireApp).container.requestOpenPlayer()
            intent?.removeExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION)
        }
    }
}
