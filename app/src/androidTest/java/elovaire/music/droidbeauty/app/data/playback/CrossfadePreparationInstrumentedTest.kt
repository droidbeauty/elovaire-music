package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@UnstableApi
@RunWith(AndroidJUnit4::class)
class CrossfadePreparationInstrumentedTest {
    @Test
    fun cancellationWhileQueueIsBeingBuiltDoesNotCreateAPlayer() = verifyPreparation(cancel = true)

    @Test
    fun languageChangeWhileQueueIsBeingBuiltUsesTheLatestLanguage() = verifyPreparation(cancel = false)

    @Test
    fun durationChangeWhileQueueIsBeingBuiltKeepsTheNewPlan() = verifyPreparation(cancel = false, changeDuration = true)

    @Test
    fun languageFailureWhileQueueIsBeingBuiltFallsBackWithoutCreatingAPlayer() =
        verifyPreparation(cancel = false, failLanguage = true)

    @Test
    fun largeQueuePreservesOrderAndCurrentIndex() = verifyPreparation(cancel = false, largeQueue = true)

    private fun verifyPreparation(
        cancel: Boolean,
        changeDuration: Boolean = false,
        failLanguage: Boolean = false,
        largeQueue: Boolean = false,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val fixture = File(context.cacheDir, "crossfade-preparation-${UUID.randomUUID()}.wav")
        instrumentation.context.assets.open("media-metadata/write-fixture.wav").use { input ->
            fixture.outputStream().use { input.copyTo(it) }
        }
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val created = CountDownLatch(1)
        val failed = CountDownLatch(1)
        val dispatches = AtomicInteger()
        val language = AtomicReference(AppLanguage.English)
        val languageUnavailable = AtomicBoolean(false)
        val dispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.Default.dispatch(context) {
                    val first = dispatches.incrementAndGet() == 1
                    try {
                        if (first) {
                            entered.countDown()
                            check(proceed.await(10, TimeUnit.SECONDS))
                        }
                        block.run()
                    } finally {
                        if (first) finished.countDown()
                    }
                }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        lateinit var primary: ExoPlayer
        var primaryInitialized = false
        var secondary: ExoPlayer? = null
        lateinit var controller: PlaybackCrossfadeController
        var controllerInitialized = false
        val song = Song(
            id = 1L, title = "Track", isExplicit = false, artist = "Unknown Artist", album = "Album",
            releaseYear = null, genre = "Genre", audioFormat = "WAV", audioQuality = null,
            fileName = fixture.name, albumId = 1L, durationMs = 60_000L, trackNumber = 1,
            discNumber = 1, dateAddedSeconds = 1L, uri = Uri.fromFile(fixture), artUri = null,
        )
        val songs = if (largeQueue) List(10_000) { index -> song.copy(id = index + 1L) }
        else listOf(song, song.copy(id = 2L, title = "Next"))
        val nextSong = songs.last()
        try {
            instrumentation.runOnMainSync {
                primary = ExoPlayer.Builder(context).build()
                primaryInitialized = true
                controller = PlaybackCrossfadeController(
                    handler = Handler(Looper.getMainLooper()),
                    scope = scope,
                    cueAnalyzer = CrossfadeCueAnalyzer(context, scope),
                    createPlayer = { ExoPlayer.Builder(context).build().also { secondary = it; created.countDown() } },
                    releasePlayer = ExoPlayer::release,
                    onPromote = { _, _ -> error("Unexpected promotion") },
                    onFailed = { failed.countDown() },
                    language = {
                        check(!languageUnavailable.get())
                        language.get()
                    },
                    preparationDispatcher = dispatcher,
                )
                controllerInitialized = true
                fun setField(name: String, value: Any) {
                    PlaybackCrossfadeController::class.java.getDeclaredField(name)
                        .apply { isAccessible = true }.set(controller, value)
                }
                setField("state", CrossfadeState.WaitingForPrewarm)
                setField("outgoing", primary)
                setField("queue", songs)
                setField("nextQueueIndex", songs.lastIndex)
                setField("transitionToken", 1L)
                setField("outgoingDurationMs", 60_000L)
                setField("incomingSong", nextSong)
                setField("currentCue", CrossfadeCue.fallback(60_000L))
                setField("plan", CrossfadeTransitionPlan(60_000L, 57_000L, 3_000L, 0L))
                PlaybackCrossfadeController::class.java.getDeclaredMethod("prewarmIncoming", Long::class.javaPrimitiveType)
                    .apply { isAccessible = true }.invoke(controller, 1L)
            }
            assertTrue(entered.await(10, TimeUnit.SECONDS))
            instrumentation.runOnMainSync {
                when {
                    cancel -> controller.cancel()
                    changeDuration -> controller.updateSettings(primary, 5_000L, CrossfadeSilencePolicy.BASE_LEVEL_DB)
                    failLanguage -> languageUnavailable.set(true)
                    largeQueue -> Unit
                    else -> language.set(AppLanguage.Polish)
                }
            }
            val preparationStartedAt = SystemClock.elapsedRealtime()
            proceed.countDown()
            assertTrue(finished.await(10, TimeUnit.SECONDS))
            if (cancel) {
                instrumentation.waitForIdleSync()
                assertEquals(1L, created.count)
                assertEquals(CrossfadeState.Idle, controller.state)
            } else if (failLanguage) {
                assertTrue(failed.await(10, TimeUnit.SECONDS))
                assertEquals(1L, created.count)
                assertEquals(CrossfadeState.Idle, controller.state)
            } else {
                assertTrue(created.await(10, TimeUnit.SECONDS))
                val playerCreatedAt = SystemClock.elapsedRealtime()
                instrumentation.runOnMainSync {
                    val player = requireNotNull(secondary)
                    assertEquals(songs.size, player.mediaItemCount)
                    assertEquals(songs.lastIndex, player.currentMediaItemIndex)
                    assertEquals("1", player.getMediaItemAt(0).mediaId)
                    assertEquals(nextSong.id.toString(), player.getMediaItemAt(songs.lastIndex).mediaId)
                    if (changeDuration) {
                        val plan = PlaybackCrossfadeController::class.java.getDeclaredField("plan")
                            .apply { isAccessible = true }.get(controller) as CrossfadeTransitionPlan
                        assertEquals(5_000L, plan.fadeDurationMs)
                    } else if (!largeQueue) {
                        assertTrue(dispatches.get() >= 2)
                        assertEquals(nextSong.toPlaybackMediaItem(AppLanguage.Polish).mediaMetadata.artist, player.getMediaItemAt(songs.lastIndex).mediaMetadata.artist)
                    }
                }
                if (largeQueue) {
                    Log.i("CrossfadeScale", "songs=${songs.size} itemBuildMs=${playerCreatedAt - preparationStartedAt} " +
                        "playerSetupMs=${SystemClock.elapsedRealtime() - playerCreatedAt}")
                }
            }
        } finally {
            proceed.countDown()
            instrumentation.runOnMainSync {
                if (controllerInitialized) controller.release()
                if (primaryInitialized) primary.release()
            }
            scope.cancel()
            fixture.delete()
        }
    }
}
