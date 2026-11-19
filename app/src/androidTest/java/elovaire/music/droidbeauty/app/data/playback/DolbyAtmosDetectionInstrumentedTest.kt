package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@UnstableApi
@RunWith(AndroidJUnit4::class)
class DolbyAtmosDetectionInstrumentedTest {
    @Test
    fun selectedDeviceFileReportsDolbyAtmos() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val path = InstrumentationRegistry.getArguments().getString("atmosFile")
        assumeTrue("Pass atmosFile to test a device file", !path.isNullOrBlank())
        val ready = CountDownLatch(1)
        val result = AtomicReference<String>()
        lateinit var player: ExoPlayer
        var playerInitialized = false
        try {
            instrumentation.runOnMainSync {
                player = ExoPlayer.Builder(instrumentation.targetContext).build()
                playerInitialized = true
                player.addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        if (tracks.groups.any { it.isSelected }) {
                            result.set(if (hasSelectedDolbyAtmosTrack(tracks)) "atmos" else tracks.toString())
                            ready.countDown()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        result.set(error.toString())
                        ready.countDown()
                    }
                })
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(java.io.File(requireNotNull(path)))))
                player.prepare()
            }
            assertTrue("Playback preparation timed out", ready.await(30, TimeUnit.SECONDS))
            assertTrue("Selected track was not recognized as Atmos: ${result.get()}", result.get() == "atmos")
        } finally {
            if (playerInitialized) instrumentation.runOnMainSync { player.release() }
        }
    }
}
