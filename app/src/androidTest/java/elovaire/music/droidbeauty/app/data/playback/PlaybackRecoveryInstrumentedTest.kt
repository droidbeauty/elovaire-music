package elovaire.music.droidbeauty.app.data.playback

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.artwork.ArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.quality.appDiagnostics
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackRecoveryInstrumentedTest {
    @Test
    fun rejectedSeekStopsPinningProgressAndPollingForever() {
        var elapsed = 0L
        val controller = PlaybackProgressController { elapsed }
        fun snapshot() = controller.onPlayerSnapshot(1L, positionMs = 0L, durationMs = 60_000L,
            bufferedPositionMs = 0L, isPlaying = false)
        snapshot()
        controller.beginScrub()
        controller.finishScrub(45_000L)
        assertEquals(45_000L, snapshot().displayPositionMs)
        elapsed = 10_000L
        assertEquals(0L, snapshot().displayPositionMs)
        assertFalse(controller.needsActivePolling())
    }

    @Test
    fun longExternalCopiesRecheckStorageBeforeExhaustingTheSafetyReserve() = runBlocking {
        val bytes = ByteArray(3 * 1024 * 1024)
        val output = ByteArrayOutputStream()
        var checks = 0
        try {
            ExternalAudioPrivateCopy.copyAudioStream(ByteArrayInputStream(bytes), output, null) {
                if (++checks == 1) 64L * 1024L * 1024L else 0L
            }
            fail("The copy ignored storage exhaustion after its initial check")
        } catch (_: IOException) {
            assertTrue(checks >= 2)
            assertTrue(output.size() < bytes.size)
        }
    }

    @Test
    fun notificationArtworkWaitsForBusyDecodersInsteadOfPermanentlyDroppingItsCover() = runBlocking(Dispatchers.IO) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val executor = Executors.newFixedThreadPool(8)
        val started = CountDownLatch(8)
        val release = CountDownLatch(1)
        val file = File.createTempFile("notification-admission-", ".png", context.cacheDir)
        val uri = Uri.fromFile(file)
        val source = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        var request: Deferred<Bitmap?>? = null
        try {
            source.eraseColor(android.graphics.Color.BLUE)
            file.outputStream().use { check(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val owners = List(8) { index ->
                executor.submit {
                    ArtworkBitmapCache.getOrLoad("notification-blocked-${System.nanoTime()}-$index") {
                        started.countDown()
                        check(release.await(5, TimeUnit.SECONDS))
                        null
                    }
                }
            }
            assertTrue(started.await(5, TimeUnit.SECONDS))
            val pending = async(start = CoroutineStart.UNDISPATCHED) {
                loadNotificationArtworkBitmap(context, listOf(uri), appDiagnostics(context).resources)
            }
            request = pending
            assertFalse("The artwork request was dropped while admission was full", pending.isCompleted)
            release.countDown()
            owners.forEach { it.get(5, TimeUnit.SECONDS) }
            val loaded = withTimeout(5_000L) { requireNotNull(pending.await()) }
            assertEquals(source.getPixel(0, 0), loaded.getPixel(0, 0))
        } finally {
            release.countDown()
            request?.cancelAndJoin()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            invalidateArtworkCaches(listOf(uri))
            source.recycle()
            check(file.delete())
        }
    }
}
