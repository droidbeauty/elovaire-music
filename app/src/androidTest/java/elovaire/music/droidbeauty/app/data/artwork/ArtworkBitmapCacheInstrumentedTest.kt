package elovaire.music.droidbeauty.app.data.artwork

import android.app.Application
import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkBitmapCacheInstrumentedTest {
    @Test
    fun missingArtwork_decodesOnlyOncePerRequest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = Uri.fromFile(File(context.cacheDir, "missing-artwork-${System.nanoTime()}.mp3"))
        ElovaireTrace.clearRecordedSections()
        val before = BackendResourceRegistry.snapshot()

        repeat(5) { assertNull(loadArtworkBitmap(context, uri, 512)) }

        assertEquals(5, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })
        val after = BackendResourceRegistry.snapshot()
        listOf(BackendResourceKind.ActiveArtworkDecode, BackendResourceKind.ActiveRetriever).forEach {
            assertEquals(before[it.key], after[it.key])
        }
    }

    @Test
    fun successfulArtwork_preservesPixelsAndReusesCachedDecode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File.createTempFile("artwork-cache-", ".png", context.cacheDir)
        val source = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888)
        val uri = Uri.fromFile(file)
        try {
            source.eraseColor(0xff336699.toInt())
            file.outputStream().use { assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            ElovaireTrace.clearRecordedSections()
            val loaded = requireNotNull(loadArtworkBitmap(context, uri, 96, ArtworkPurpose.Notification))

            repeat(5) { assertSame(loaded, loadArtworkBitmap(context, uri, 96, ArtworkPurpose.Notification)) }

            assertEquals(32, loaded.width)
            assertEquals(16, loaded.height)
            assertEquals(source.getPixel(16, 8), loaded.getPixel(16, 8))
            assertEquals(1, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(listOf(uri.toString()))
            source.recycle()
            assertTrue(file.delete())
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun memoryPressure_shedsIndexedBitmapsWithoutRecyclingOrDecoding() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val application = instrumentation.targetContext.applicationContext as Application
        ArtworkBitmapCache.ensureRegistered(application)
        val prefix = "content://artwork/trim-${System.nanoTime()}"
        val uris = List(8) { Uri.parse("$prefix/$it") }
        val bitmaps = List(8) { Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888) }
        val keys = uris.map { requireNotNull(artworkRequestKey(it, 512, ArtworkPurpose.UiLarge)) }
        fun cachedCount() = keys.count { ArtworkBitmapCache[it.cacheKey] != null }
        try {
            keys.forEachIndexed { index, key -> ArtworkBitmapCache.put(key.cacheKey, bitmaps[index]) }
            val before = cachedCount()
            ElovaireTrace.clearRecordedSections()
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) }
            assertEquals(before, cachedCount())
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) }
            assertTrue(cachedCount() in 0..minOf(before, 4))
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) }
            assertEquals(0, cachedCount())
            uris.forEach { assertNull(ArtworkBitmapCache.bestForUri(it, 512, ArtworkPurpose.UiLarge)) }
            bitmaps.forEach { assertFalse(it.isRecycled) }
            assertEquals(0, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(uris.map(Uri::toString))
            bitmaps.forEach(Bitmap::recycle)
        }
    }

    @Test
    fun fullAdmission_doesNotDecodeOutsideCache() = runBlocking(Dispatchers.IO) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val started = CountDownLatch(8)
        val release = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)
        val file = File.createTempFile("artwork-admission-", ".png", context.cacheDir)
        val source = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888)
        val fixtureUri = Uri.fromFile(file)
        try {
            source.eraseColor(0xff336699.toInt())
            file.outputStream().use { assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val pending = List(8) { index ->
                executor.submit {
                    ArtworkBitmapCache.getOrLoad("admission-$index") {
                        started.countDown()
                        assertTrue(release.await(10, TimeUnit.SECONDS))
                        null
                    }
                }
            }
            assertTrue(started.await(10, TimeUnit.SECONDS))
            val uri = Uri.fromFile(File(context.cacheDir, "missing-admission-${System.nanoTime()}.mp3"))
            ElovaireTrace.clearRecordedSections()

            assertNull(loadArtworkBitmap(context, uri, 512))
            assertEquals(0, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })

            val cancelled = async(start = CoroutineStart.UNDISPATCHED) {
                loadArtworkBitmapAwaitingAdmission(context, uri, 512)
            }
            assertFalse(cancelled.isCompleted)
            withTimeout(5_000L) { cancelled.cancelAndJoin() }
            val visibleRequests = List(16) {
                async(start = CoroutineStart.UNDISPATCHED) {
                    loadArtworkBitmapAwaitingAdmission(context, fixtureUri, 96, ArtworkPurpose.Notification)
                }
            }
            visibleRequests.forEach { assertFalse(it.isCompleted) }
            assertEquals(0, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })

            release.countDown()
            pending.forEach { it.get(10, TimeUnit.SECONDS) }
            withTimeout(5_000L) {
                val loaded = requireNotNull(visibleRequests.first().await())
                assertEquals(source.getPixel(16, 8), loaded.getPixel(16, 8))
                visibleRequests.forEach { assertSame(loaded, it.await()) }
            }
            assertEquals(1, ElovaireTrace.recordedSectionNames().count { it == "artwork_decode" })
        } finally {
            release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))
            ArtworkBitmapCache.removeAllMatchingUris(listOf(fixtureUri.toString()))
            source.recycle()
            assertTrue(file.delete())
        }
    }

    @Test
    fun sameSizeEquivalentPurpose_reusesArgbBitmapOnly() {
        val uri = Uri.parse("content://media/external/audio/media/instrumented-equivalent-purpose")
        val key = requireNotNull(artworkRequestKey(uri, 512, ArtworkPurpose.UiLarge))
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            ArtworkBitmapCache.put(key.cacheKey, bitmap)

            assertSame(
                bitmap,
                ArtworkBitmapCache.sameSizeForEquivalentPurpose(
                    uri,
                    key.targetPx,
                    ArtworkPurpose.Notification,
                ),
            )
            assertNull(
                ArtworkBitmapCache.sameSizeForEquivalentPurpose(
                    uri,
                    key.targetPx,
                    ArtworkPurpose.UiGrid,
                ),
            )
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(listOf(uri.toString()))
            bitmap.recycle()
        }
    }

    @Test
    fun sameSizeEquivalentPurpose_keepsThumbnailAndNotificationDecodePathsSeparate() {
        val uri = Uri.parse("content://artwork/instrumented-thumbnail-purpose")
        val key = requireNotNull(artworkRequestKey(uri, 512, ArtworkPurpose.UiLarge))
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            ArtworkBitmapCache.put(key.cacheKey, bitmap)
            assertNull(ArtworkBitmapCache.sameSizeForEquivalentPurpose(uri, 512, ArtworkPurpose.Notification))
            assertSame(bitmap, ArtworkBitmapCache.sameSizeForEquivalentPurpose(uri, 512, ArtworkPurpose.TagEditorPreview))
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(listOf(uri.toString()))
            bitmap.recycle()
        }
    }
}
