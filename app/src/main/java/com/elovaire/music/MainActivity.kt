package elovaire.music.droidbeauty.app

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.performance.ElovaireJankMonitor

class MainActivity : ComponentActivity() {
    private lateinit var intentHandler: MainIntentHandler
    private var jankMonitor: ElovaireJankMonitor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        jankMonitor = runCatching { ElovaireJankMonitor.start(window) }.getOrNull()

        val app = application as ElovaireApp
        val container = app.container.also(AppContainer::start)
        intentHandler = MainIntentHandler(this, container)
        val shouldShowColdStartSplash = savedInstanceState == null
        val isFirstActivityInProcess = container.consumeColdStartHomeReset()
        val resetHomeScrollOnColdStart = shouldShowColdStartSplash && isFirstActivityInProcess
        if (savedInstanceState == null) {
            intentHandler.handle(intent)
        }
        setContent {
            ElovaireAppShell(
                container = container,
                shouldShowColdStartSplash = shouldShowColdStartSplash,
                resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentHandler.handle(intent)
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
        jankMonitor?.setTrackingEnabled(true)
    }

    override fun onPause() {
        jankMonitor?.setTrackingEnabled(false)
        super.onPause()
    }

    override fun onDestroy() {
        intentHandler.release()
        jankMonitor?.release()
        jankMonitor = null
        super.onDestroy()
    }
}
