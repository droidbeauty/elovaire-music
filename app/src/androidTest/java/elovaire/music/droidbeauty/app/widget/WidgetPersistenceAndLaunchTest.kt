package elovaire.music.droidbeauty.app.widget

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.MainActivity
import elovaire.music.droidbeauty.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetPersistenceAndLaunchTest {
    @Test
    fun snapshotSurvivesRepositoryRecreationAndSkipsNoOpWrite() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val snapshotFile = File(context.cacheDir, "widget-test-${UUID.randomUUID()}.bin")
        val firstStore = AtomicWidgetSnapshotStore(snapshotFile)
        try {
            val snapshot = testSnapshot()
            assertTrue(firstStore.writeIfChanged(snapshot))
            assertEquals(false, firstStore.writeIfChanged(snapshot))

            val recreatedStore = AtomicWidgetSnapshotStore(snapshotFile)
            assertEquals(snapshot, recreatedStore.readLatest())
            assertTrue(snapshotFile.length() > 0L)
        } finally {
            firstStore.clear()
        }
    }

    @Test
    fun corruptSnapshotCanBeReplacedAndCleared() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val snapshotFile = File(context.cacheDir, "widget-corrupt-${UUID.randomUUID()}.bin")
        snapshotFile.writeBytes(byteArrayOf(1, 2, 3))
        val store = AtomicWidgetSnapshotStore(snapshotFile)
        try {
            assertEquals(null, store.readLatest())
            assertTrue(store.writeIfChanged(testSnapshot()))
            assertEquals(testSnapshot(), store.readLatest())
            store.clear()
            assertEquals(null, store.readLatest())
        } finally {
            store.clear()
        }
    }

    @Test
    fun launchIntentsTargetMainActivityAndShareNowPlayingContract() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appIntent = WidgetLaunchIntentFactory.openApp(context)
        val playerIntent = WidgetLaunchIntentFactory.openNowPlaying(context)

        assertEquals(MainActivity::class.java.name, appIntent.component?.className)
        assertEquals(MainActivity::class.java.name, playerIntent.component?.className)
        assertTrue(playerIntent.getBooleanExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, false))
        assertTrue(playerIntent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(playerIntent.flags and android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }

    private fun testSnapshot() = WidgetPlaybackSnapshot(
        contentRevision = 3L,
        currentSongId = 91L,
        title = "Track",
        artist = "Artist",
        album = "Album",
        artworkIdentity = WidgetArtworkIdentity(91L, 4L),
        isPlaying = true,
        transportShowsPause = true,
        repeatMode = WidgetRepeatMode.All,
        shuffleEnabled = false,
        durationMs = 30_000L,
        capturedAtElapsedRealtimeMs = 1_000L,
    )
}
