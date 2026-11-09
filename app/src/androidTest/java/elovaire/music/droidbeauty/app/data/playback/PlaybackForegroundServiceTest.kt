package elovaire.music.droidbeauty.app.data.playback

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackForegroundServiceTest {
    @Suppress("DEPRECATION")
    @Test
    fun rapidDemotionAndRestartKeepsTheNewServiceRequestAlive() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val channel = "foreground-service-test"
        notificationManager.createNotificationChannel(NotificationChannel(channel, "Test", NotificationManager.IMPORTANCE_LOW))
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Playback test")
            .build()
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                ElovaireMediaLibraryService.start(context, 4999, notification)
                ElovaireMediaLibraryService.demote(context)
                ElovaireMediaLibraryService.start(context, 4999, notification)
            }
            Thread.sleep(500)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val deadline = android.os.SystemClock.elapsedRealtime() + 5_000L
            var foreground = false
            while (!foreground && android.os.SystemClock.elapsedRealtime() < deadline) {
                foreground = activityManager.getRunningServices(Int.MAX_VALUE).any {
                    it.service.className == ElovaireMediaLibraryService::class.java.name && it.foreground
                }
                if (!foreground) Thread.sleep(50)
            }
            assertTrue("The restarted playback service did not become foreground", foreground)
        } finally {
            ElovaireMediaLibraryService.stop(context)
            notificationManager.deleteNotificationChannel(channel)
        }
    }
}
