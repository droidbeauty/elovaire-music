package elovaire.music.app

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION
import elovaire.music.app.ui.screens.ElovaireRoot
import elovaire.music.app.ui.theme.ElovaireTheme
import elovaire.music.app.ui.theme.themeBackgroundForMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ElovaireApp
        val container = app.container
        val shouldShowColdStartSplash = app.consumeColdStartSplash()
        handleNotificationIntent()
        setContent {
            val themeMode = container.preferenceStore.themeMode.collectAsStateWithLifecycle()
            val textSizePreset = container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            var previousThemeMode by remember { mutableStateOf(themeMode.value) }
            var overlayColor by remember {
                mutableStateOf(themeBackgroundForMode(themeMode.value, systemDark))
            }
            val themeOverlayAlpha = remember { Animatable(0f) }
            var showSplash by remember { mutableStateOf(shouldShowColdStartSplash) }

            LaunchedEffect(themeMode.value, systemDark) {
                if (previousThemeMode != themeMode.value) {
                    overlayColor = themeBackgroundForMode(previousThemeMode, systemDark)
                    previousThemeMode = themeMode.value
                    themeOverlayAlpha.snapTo(1f)
                    themeOverlayAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    )
                }
            }

            LaunchedEffect(showSplash) {
                if (showSplash) {
                    delay(1_500L)
                    showSplash = false
                }
            }

            ElovaireTheme(
                themeMode = themeMode.value,
                textSizePreset = textSizePreset.value,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ElovaireRoot(container = container)
                    if (themeOverlayAlpha.value > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(overlayColor.copy(alpha = themeOverlayAlpha.value)),
                        )
                    }
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = tween(durationMillis = 180),
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                        ) + scaleOut(
                            targetScale = 1.02f,
                            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(92.dp),
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Elovaire",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun handleNotificationIntent() {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, false) == true) {
            (application as ElovaireApp).container.requestOpenPlayer()
            intent?.removeExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION)
        }
    }
}
