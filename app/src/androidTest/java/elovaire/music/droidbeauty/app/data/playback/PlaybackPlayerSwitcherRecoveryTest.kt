package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertSame
import org.junit.Assert.assertNotSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackPlayerSwitcherRecoveryTest {
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
