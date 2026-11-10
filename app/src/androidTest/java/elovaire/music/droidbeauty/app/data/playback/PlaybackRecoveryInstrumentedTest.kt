package elovaire.music.droidbeauty.app.data.playback

import android.graphics.Bitmap
import android.content.Context
import android.content.ContextWrapper
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
    fun incompleteDualFileGenerationDoesNotRestoreAnUnrelatedPosition() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(target) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("incomplete_generation_test_$name", mode)
        }
        val structure = context.getSharedPreferences("playback_session_structure", Context.MODE_PRIVATE)
        val recovery = context.getSharedPreferences("playback_session_recovery", Context.MODE_PRIVATE)
        try {
            assertTrue(structure.edit().putInt("format_version", DUAL_FILE_FORMAT_VERSION)
                .putLong("generation", 2L).putString("queue_song_ids", "41,42").commit())
            assertTrue(recovery.edit().putLong("position_ms", 1234L).putInt("current_index", 1)
                .putLong("current_song_id", 42L).putBoolean("was_playing", true)
                .putLong("saved_at_wall_time_ms", System.currentTimeMillis()).commit())
            val result = checkNotNull(PlaybackSessionStore(context).load())
            assertEquals(0L, result.positionMs)
            assertEquals(0, result.currentIndex)
            assertFalse(result.wasPlaying)
            assertTrue(structure.edit().remove("generation").commit())
            val legacy = checkNotNull(PlaybackSessionStore(context).load())
            assertEquals(1234L, legacy.positionMs)
            assertEquals(1, legacy.currentIndex)
            assertTrue(legacy.wasPlaying)
        } finally {
            listOf("playback_session_committed", "playback_session_structure", "playback_session_recovery", "playback_session")
                .forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
        }
    }

    @Test
    fun newerDualFilePlaybackSessionIsPreserved() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(target) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("future_dual_playback_test_$name", mode)
        }
        val structure = context.getSharedPreferences("playback_session_structure", Context.MODE_PRIVATE)
        val recovery = context.getSharedPreferences("playback_session_recovery", Context.MODE_PRIVATE)
        try {
            assertTrue(structure.edit().putInt("format_version", CURRENT_FORMAT_VERSION + 1)
                .putString("queue_song_ids", "42").commit())
            assertTrue(recovery.edit().putLong("position_ms", 1234L).commit())
            val store = PlaybackSessionStore(context)
            assertEquals(null, store.load())
            store.clear()
            store.save(PersistedPlaybackSession(emptyList(), null, -1, 0L, PlaybackRepeatMode.Off, false, null, false, 0L))
            assertEquals("42", structure.getString("queue_song_ids", null))
            assertEquals(1234L, recovery.getLong("position_ms", -1L))
            assertTrue(structure.edit().clear().commit())
            assertTrue(recovery.edit().clear().commit())
            val legacy = context.getSharedPreferences("playback_session", Context.MODE_PRIVATE)
            assertTrue(legacy.edit().putInt("format_version", CURRENT_FORMAT_VERSION + 1)
                .putString("queue_song_ids", "99").commit())
            val olderApp = PlaybackSessionStore(context)
            assertEquals(null, olderApp.load())
            olderApp.clear()
            olderApp.save(PersistedPlaybackSession(emptyList(), null, -1, 0L, PlaybackRepeatMode.Off, false, null, false, 0L))
            assertEquals("99", legacy.getString("queue_song_ids", null))
        } finally {
            listOf("playback_session_committed", "playback_session_structure", "playback_session_recovery", "playback_session")
                .forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
        }
    }

    @Test
    fun newerPlaybackSessionIsNotErasedByAnOlderAppVersion() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(target) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("future_playback_test_$name", mode)
        }
        val preferences = context.getSharedPreferences("playback_session_committed", Context.MODE_PRIVATE)
        try {
            assertTrue(preferences.edit().putInt("format_version", CURRENT_FORMAT_VERSION + 1)
                .putString("queue_song_ids", "42").commit())
            val store = PlaybackSessionStore(context)
            assertEquals(null, store.load())
            store.clear()
            store.save(PersistedPlaybackSession(emptyList(), null, -1, 0L, PlaybackRepeatMode.Off, false, null, false, 0L))
            assertEquals(CURRENT_FORMAT_VERSION + 1, preferences.getInt("format_version", 0))
            assertEquals("42", preferences.getString("queue_song_ids", null))
        } finally {
            context.getSharedPreferences("playback_session_committed", Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test
    fun playbackSessionRestoresSmartPlaylistAndReadsLegacyNullSentinel() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(target) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("playback_session_test_$name", mode)
        }
        val preferences = context.getSharedPreferences("playback_session_committed", Context.MODE_PRIVATE)
        val session = PersistedPlaybackSession(
            queueSongIds = listOf(1L), currentSongId = 1L, currentIndex = 0,
            positionMs = 0L, repeatMode = PlaybackRepeatMode.Off, shuffleEnabled = false,
            sourcePlaylistId = -1L, wasPlaying = false, savedAtWallTimeMs = 1L,
        )
        try {
            PlaybackSessionStore(context).save(session)
            assertEquals(-1L, PlaybackSessionStore(context).load()?.sourcePlaylistId)
            assertTrue(preferences.edit().remove("source_playlist_present").commit())
            assertEquals(null, PlaybackSessionStore(context).load()?.sourcePlaylistId)
        } finally {
            listOf("playback_session_committed", "playback_session_structure", "playback_session_recovery", "playback_session")
                .forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
        }
    }

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
