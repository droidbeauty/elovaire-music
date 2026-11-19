package elovaire.music.droidbeauty.app.data.playback

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackProgressTickerInstrumentedTest {
    @Test
    fun slowCallbackDoesNotTriggerAnImmediateExtraPoll() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val finished = CountDownLatch(1)
        val ticks = mutableListOf<Long>()
        val ticker = PlaybackProgressTicker(scope, intervalMs = { 80L }, onTick = {
            ticks += SystemClock.elapsedRealtime()
            if (ticks.size == 1) {
                Thread.sleep(120L)
                true
            } else {
                finished.countDown()
                false
            }
        })
        try {
            ticker.start()
            assertTrue(finished.await(5, TimeUnit.SECONDS))
            assertTrue(ticks[1] - ticks[0] >= 190L)
        } finally {
            ticker.stop()
            scope.cancel()
        }
    }
}
