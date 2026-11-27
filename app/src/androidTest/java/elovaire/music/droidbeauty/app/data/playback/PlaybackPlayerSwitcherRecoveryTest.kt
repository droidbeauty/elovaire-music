package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertSame
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@UnstableApi
class PlaybackPlayerSwitcherRecoveryTest {
    @Test
    fun externalSpeedChangeDisablesOffload() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val updated = CountDownLatch(1)
        var manager: PlaybackManager? = null
        try {
            instrumentation.runOnMainSync {
                manager = PlaybackManager(context, scope)
                val player = checkNotNull(manager).playerInstance
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setAudioOffloadPreferences(PlaybackOffloadPolicy(enabled = true).toAudioOffloadPreferences())
                    .build()
                player.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        if (player.playbackParameters.speed == 1.5f &&
                            player.trackSelectionParameters.audioOffloadPreferences.audioOffloadMode ==
                            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
                        ) updated.countDown()
                    }
                })
                player.playbackParameters = PlaybackParameters(1.5f)
            }
            assertTrue("Offload was not disabled after speed changed", updated.await(5, TimeUnit.SECONDS))
        } finally {
            instrumentation.runOnMainSync { manager?.release() }
            scope.cancel()
        }
    }

    @Test
    fun requestingEffectsOrCrossfadeDisablesOffloadBeforeAQueueIsLoaded() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            var effectsActive = false
            val manager = PlaybackManager(context, scope, hasSignalAlteringEffects = { effectsActive })
            try {
                val player = manager.playerInstance
                val enabled = PlaybackOffloadPolicy(enabled = true).toAudioOffloadPreferences()
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setAudioOffloadPreferences(enabled)
                    .build()
                manager.setCrossfadeEnabled(true)
                assertEquals(
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED,
                    player.trackSelectionParameters.audioOffloadPreferences.audioOffloadMode,
                )
                manager.setCrossfadeEnabled(false)
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setAudioOffloadPreferences(enabled)
                    .build()
                effectsActive = true
                manager.reevaluateAudioOutputPath()
                assertEquals(
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED,
                    player.trackSelectionParameters.audioOffloadPreferences.audioOffloadMode,
                )
            } finally {
                manager.release()
                scope.cancel()
            }
        }
    }

    @Test
    fun replacementHasTargetGainBeforeObserversCanStartPlayback() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            for (direct in listOf(false, true)) {
                val original = ExoPlayer.Builder(context).build()
                var replacement: ExoPlayer? = null
                var gainAtAttach = -1f
                try {
                    val switcher = PlaybackPlayerSwitcher(
                        createPlayer = { ExoPlayer.Builder(context).build().also { replacement = it } },
                        releasePlayer = { it.release() },
                        attachPlayerObservers = { gainAtAttach = it.volume },
                        detachPlayerObservers = {},
                        onPlayerReplaced = {},
                        applyPreferredAudioDevice = {},
                        targetPlayerOutputGain = { useDirect -> if (useDirect) 0.75f else 0.25f },
                    )
                    switcher.switchPlayerAudioPath(original, emptyList(), useDirectPlayback = direct)
                    assertEquals(if (direct) 0.75f else 0.25f, gainAtAttach, 0f)
                } finally {
                    original.release()
                    replacement?.release()
                }
            }
        }
    }

    @Test
    fun oldPlayerCleanupFailureDoesNotDiscardThePublishedReplacement() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val original = ExoPlayer.Builder(context).build()
            var published = original
            var replacement: ExoPlayer? = null
            try {
                val switcher = PlaybackPlayerSwitcher(
                    createPlayer = { ExoPlayer.Builder(context).build().also { replacement = it } },
                    releasePlayer = { if (it === original) throw IllegalStateException("cleanup failed") else it.release() },
                    attachPlayerObservers = {}, detachPlayerObservers = {},
                    onPlayerReplaced = { published = it }, applyPreferredAudioDevice = {},
                    targetPlayerOutputGain = { 1f },
                )
                val result = switcher.switchPlayerAudioPath(original, emptyList(), useDirectPlayback = false)
                assertNotSame(original, result)
                assertSame(result, published)
            } finally {
                original.release()
                replacement?.release()
            }
        }
    }

    @Test
    fun publicationFailureRestoresTheOriginalPlayer() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val original = ExoPlayer.Builder(context).build()
            var published = original
            var replacement: ExoPlayer? = null
            try {
                val switcher = PlaybackPlayerSwitcher(
                    createPlayer = { ExoPlayer.Builder(context).build().also { replacement = it } },
                    releasePlayer = {
                        it.release()
                        if (it !== original) throw IllegalStateException("replacement cleanup failed")
                    },
                    attachPlayerObservers = {},
                    detachPlayerObservers = {},
                    onPlayerReplaced = { player ->
                        published = player
                        if (player !== original) throw IllegalStateException("publication failed")
                    },
                    applyPreferredAudioDevice = {},
                    targetPlayerOutputGain = { 1f },
                )
                val result = switcher.switchPlayerAudioPath(original, emptyList(), useDirectPlayback = false)
                assertSame(original, result)
                assertSame(original, published)
            } finally {
                original.release()
                replacement?.release()
            }
        }
    }
}
