package elovaire.music.droidbeauty.app.data.artwork

import android.app.Application
import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.quality.appDiagnostics
import elovaire.music.droidbeauty.app.quality.captureJourneyBaseline
import elovaire.music.droidbeauty.app.quality.captureJourneyDelta
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.asCoroutineDispatcher
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
    fun failedSharedDecodePropagatesWithoutRetryingEachWaiter() {
        val executor = Executors.newFixedThreadPool(2)
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val key = "failed-shared-${System.nanoTime()}"
        val attempts = AtomicInteger()
        val failure = IllegalStateException("decode failed")
        try {
            val owner = executor.submit<Throwable?> {
                runCatching {
                    ArtworkBitmapCache.getOrLoad(key) {
                        attempts.incrementAndGet()
                        started.countDown()
                        check(release.await(5, TimeUnit.SECONDS))
                        throw failure
                    }
                }.exceptionOrNull()
            }
            assertTrue(started.await(5, TimeUnit.SECONDS))
            val waiterStarted = CountDownLatch(1)
            var waiterThread: Thread? = null
            val waiter = executor.submit<Throwable?> {
                waiterThread = Thread.currentThread()
                waiterStarted.countDown()
                runCatching {
                    ArtworkBitmapCache.getOrLoad(key) { attempts.incrementAndGet(); null }
                }.exceptionOrNull()
            }
            assertTrue(waiterStarted.await(5, TimeUnit.SECONDS))
            val deadline = android.os.SystemClock.uptimeMillis() + 5_000L
            while (waiterThread?.state != Thread.State.WAITING && android.os.SystemClock.uptimeMillis() < deadline) {
                android.os.SystemClock.sleep(1L)
            }
            assertEquals(Thread.State.WAITING, waiterThread?.state)
            release.countDown()
            assertSame(failure, owner.get(5, TimeUnit.SECONDS))
            assertSame(failure, waiter.get(5, TimeUnit.SECONDS))
            assertEquals(1, attempts.get())
        } finally {
            release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }
    @Test
    fun suspendedWaiterSharesFailureAndRetriesOwnerCancellation() = runBlocking {
        val executor = Executors.newSingleThreadExecutor()
        try {
            for (cancelOwner in listOf(false, true)) {
                val key = "suspended-failure-${System.nanoTime()}"
                val started = CountDownLatch(1)
                val release = CountDownLatch(1)
                val attempts = AtomicInteger()
                val failure = if (cancelOwner) CancellationException("owner cancelled")
                    else IllegalStateException("decode failed")
                val owner = executor.submit {
                    runCatching {
                        ArtworkBitmapCache.getOrLoad(key) {
                            attempts.incrementAndGet()
                            started.countDown()
                            check(release.await(5, TimeUnit.SECONDS))
                            throw failure
                        }
                    }
                }
                try {
                    assertTrue(started.await(5, TimeUnit.SECONDS))
                    val waiter = async(start = CoroutineStart.UNDISPATCHED) {
                        runCatching { ArtworkBitmapCache.getOrLoadAwaiting(key) { attempts.incrementAndGet(); null } }
                    }
                    assertFalse(waiter.isCompleted)
                    release.countDown()
                    val result = withTimeout(5_000L) { waiter.await() }
                    if (cancelOwner) {
                        assertTrue(result.isSuccess)
                        assertEquals(2, attempts.get())
                    } else {
                        assertSame(failure, result.exceptionOrNull())
                        assertEquals(1, attempts.get())
                    }
                    owner.get(5, TimeUnit.SECONDS)
                } finally {
                    release.countDown()
                }
            }
        } finally {
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }

    @Test
    fun synchronousWaitersShareMissingArtworkWithoutRepeatingTheDecode() {
        val executor = Executors.newFixedThreadPool(8)
        try {
            repeat(10) { iteration ->
                val key = "missing-shared-${System.nanoTime()}-$iteration"
                val started = CountDownLatch(1)
                val release = CountDownLatch(1)
                val waitersStarted = CountDownLatch(7)
                val decodes = AtomicInteger()
                val owner = executor.submit<Bitmap?> {
                    ArtworkBitmapCache.getOrLoad(key) {
                        decodes.incrementAndGet()
                        started.countDown()
                        assertTrue(release.await(5, TimeUnit.SECONDS))
                        null
                    }
                }
                try {
                    assertTrue(started.await(5, TimeUnit.SECONDS))
                    val waiterThreads = arrayOfNulls<Thread>(7)
                    val waiters = List(7) { index ->
                        executor.submit<Bitmap?> {
                            waiterThreads[index] = Thread.currentThread()
                            waitersStarted.countDown()
                            ArtworkBitmapCache.getOrLoad(key) {
                                decodes.incrementAndGet()
                                null
                            }
                        }
                    }
                    assertTrue(waitersStarted.await(5, TimeUnit.SECONDS))
                    // Wait for each worker to join the shared load before releasing its owner.
                    val deadline = android.os.SystemClock.uptimeMillis() + 5_000L
                    while (waiterThreads.any { it?.state != Thread.State.WAITING } &&
                        android.os.SystemClock.uptimeMillis() < deadline
                    ) {
                        android.os.SystemClock.sleep(1L)
                    }
                    assertTrue(waiterThreads.all { it?.state == Thread.State.WAITING })
                    release.countDown()
                    assertNull(owner.get(5, TimeUnit.SECONDS))
                    waiters.forEach { assertNull(it.get(5, TimeUnit.SECONDS)) }
                    assertEquals("Repeated decode in burst $iteration", 1, decodes.get())
                } finally {
                    release.countDown()
                }
            }
        } finally {
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }

    @Test
    fun invalidatingInFlightSynchronousDecodeLoadsTheUpdatedCover() = assertInFlightInvalidation(false)

    @Test
    fun invalidatingInFlightSuspendingDecodeLoadsTheUpdatedCover() = assertInFlightInvalidation(true)

    private fun assertInFlightInvalidation(suspending: Boolean) {
        val uri = Uri.parse("content://artwork/invalidation-${System.nanoTime()}")
        val key = requireNotNull(artworkRequestKey(uri, 512, ArtworkPurpose.UiLarge))
        val oldCover = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val newCover = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val decodes = AtomicInteger()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val decode = {
                if (decodes.incrementAndGet() == 1) {
                    started.countDown()
                    assertTrue(release.await(5, TimeUnit.SECONDS))
                    oldCover
                } else {
                    newCover
                }
            }
            val loaded = executor.submit<Bitmap?> {
                if (suspending) runBlocking { ArtworkBitmapCache.getOrLoadAwaiting(key.cacheKey, decode) }
                else ArtworkBitmapCache.getOrLoad(key.cacheKey, decode = decode)
            }
            assertTrue(started.await(5, TimeUnit.SECONDS))
            invalidateArtworkBitmapCache(listOf(uri.toString()))
            release.countDown()

            assertSame(newCover, loaded.get(5, TimeUnit.SECONDS))
            assertSame(newCover, ArtworkBitmapCache[key.cacheKey])
            assertEquals(2, decodes.get())
        } finally {
            release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            invalidateArtworkBitmapCache(listOf(uri.toString()))
            oldCover.recycle()
            newCover.recycle()
        }
    }

    @Test
    fun libraryArtworkInvalidationAlsoClearsDerivedColors() {
        val uri = "content://artwork/colors-${System.nanoTime()}"
        val gradientKey = "$uri|512|argb8888-thumbnail|gradient"
        val accentKey = "$uri|512|argb8888-thumbnail|accent"
        try {
            ArtworkGradientCache.putGradient(gradientKey, listOf(0xff112233.toInt()))
            ArtworkGradientCache.putAccent(accentKey, 0xff112233.toInt())

            invalidateArtworkBitmapCache(listOf(uri))

            assertNull(ArtworkGradientCache.gradient(gradientKey))
            assertNull(ArtworkGradientCache.accent(accentKey))
        } finally {
            ArtworkGradientCache.removeMatching(setOf(uri))
        }
    }

    @Test
    fun invalidatedColorWorkCannotRestoreOutdatedGradientsOrAccents() {
        val uri = "content://artwork/color-race-${System.nanoTime()}"
        val key = "$uri|512|argb8888-thumbnail"
        val previousRevision = ArtworkGradientCache.invalidationRevision.value
        try {
            invalidateArtworkBitmapCache(listOf(uri))
            ArtworkGradientCache.putGradient("$key|gradient", listOf(1), previousRevision)
            ArtworkGradientCache.putAccent("$key|accent", 1, previousRevision)

            assertNull(ArtworkGradientCache.gradient("$key|gradient"))
            assertNull(ArtworkGradientCache.accent("$key|accent"))

            val currentRevision = ArtworkGradientCache.invalidationRevision.value
            ArtworkGradientCache.putGradient("$key|gradient", listOf(2), currentRevision)
            ArtworkGradientCache.putAccent("$key|accent", 2, currentRevision)
            assertEquals(listOf(2), ArtworkGradientCache.gradient("$key|gradient"))
            assertEquals(2, ArtworkGradientCache.accent("$key|accent"))
        } finally {
            invalidateArtworkBitmapCache(listOf(uri))
        }
    }

    @Test
    fun coalescedWaiterDoesNotBlockDispatcherOrCancelDecodeOwner() = runBlocking {
        val key = "coalesced-${System.nanoTime()}"
        val decodeStarted = CountDownLatch(1)
        val releaseDecode = CountDownLatch(1)
        val ownerExecutor = Executors.newSingleThreadExecutor()
        val waiterDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val owner = ownerExecutor.submit<Bitmap?> {
            ArtworkBitmapCache.getOrLoad(key) {
                decodeStarted.countDown()
                assertTrue(releaseDecode.await(10, TimeUnit.SECONDS))
                null
            }
        }
        try {
            assertTrue(decodeStarted.await(10, TimeUnit.SECONDS))
            val waiter = async(waiterDispatcher) {
                ArtworkBitmapCache.getOrLoadAwaiting(key) {
                    throw AssertionError("A coalesced request must share the active decode")
                }
            }
            val dispatcherProbe = async(waiterDispatcher) { true }

            assertTrue(withTimeout(5_000L) { dispatcherProbe.await() })
            assertFalse(waiter.isCompleted)
            waiter.cancelAndJoin()
            assertFalse(owner.isDone)
        } finally {
            releaseDecode.countDown()
            owner.get(10, TimeUnit.SECONDS)
            ownerExecutor.shutdownNow()
            assertTrue(ownerExecutor.awaitTermination(10, TimeUnit.SECONDS))
            waiterDispatcher.close()
        }
    }

    @Test
    fun missingArtwork_decodesOnlyOncePerRequest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = appDiagnostics(context)
        val baseline = diagnostics.captureJourneyBaseline()
        val uri = Uri.fromFile(File(context.cacheDir, "missing-artwork-${System.nanoTime()}.mp3"))

        repeat(5) { assertNull(loadArtworkBitmap(context, uri, 512, resourceTracker = diagnostics.resources)) }

        val delta = diagnostics.captureJourneyDelta(baseline)
        assertEquals(5, delta.traceSections.count { it == "artwork_decode" })
        listOf(BackendResourceKind.ActiveArtworkDecode, BackendResourceKind.ActiveRetriever).forEach { kind ->
            assertFalse(kind.key in delta.resourceDeltas)
        }
    }

    @Test
    fun successfulArtwork_preservesPixelsAndReusesCachedDecode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = appDiagnostics(context)
        val baseline = diagnostics.captureJourneyBaseline()
        val file = File.createTempFile("artwork-cache-", ".png", context.cacheDir)
        val source = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888)
        val uri = Uri.fromFile(file)
        try {
            source.eraseColor(0xff336699.toInt())
            file.outputStream().use { assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val loaded = requireNotNull(
                loadArtworkBitmap(context, uri, 96, ArtworkPurpose.Notification, diagnostics.resources),
            )

            repeat(5) {
                assertSame(loaded, loadArtworkBitmap(context, uri, 96, ArtworkPurpose.Notification, diagnostics.resources))
            }

            assertEquals(32, loaded.width)
            assertEquals(16, loaded.height)
            assertEquals(source.getPixel(16, 8), loaded.getPixel(16, 8))
            assertEquals(1, diagnostics.captureJourneyDelta(baseline).traceSections.count { it == "artwork_decode" })
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
        val diagnostics = appDiagnostics(application)
        val baseline = diagnostics.captureJourneyBaseline()
        ArtworkBitmapCache.ensureRegistered(application, diagnostics.resources)
        val prefix = "content://artwork/trim-${System.nanoTime()}"
        val uris = List(8) { Uri.parse("$prefix/$it") }
        val bitmaps = List(8) { Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888) }
        val keys = uris.map { requireNotNull(artworkRequestKey(it, 512, ArtworkPurpose.UiLarge)) }
        fun cachedCount() = keys.count { ArtworkBitmapCache[it.cacheKey] != null }
        try {
            keys.forEachIndexed { index, key -> ArtworkBitmapCache.put(key.cacheKey, bitmaps[index]) }
            val before = cachedCount()
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) }
            assertEquals(before, cachedCount())
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) }
            assertTrue(cachedCount() in 0..minOf(before, 4))
            instrumentation.runOnMainSync { application.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) }
            assertEquals(0, cachedCount())
            uris.forEach { assertNull(ArtworkBitmapCache.bestForUri(it, 512, ArtworkPurpose.UiLarge)) }
            bitmaps.forEach { assertFalse(it.isRecycled) }
            assertEquals(0, diagnostics.captureJourneyDelta(baseline).traceSections.count { it == "artwork_decode" })
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(uris.map(Uri::toString))
            bitmaps.forEach(Bitmap::recycle)
        }
    }

    @Test
    fun fullAdmission_doesNotDecodeOutsideCache() = runBlocking(Dispatchers.IO) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = appDiagnostics(context)
        val baseline = diagnostics.captureJourneyBaseline()
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
            ArtworkBitmapCache.removeAllMatchingUris(List(8) { "admission-$it" })
            val uri = Uri.fromFile(File(context.cacheDir, "missing-admission-${System.nanoTime()}.mp3"))
            assertNull(loadArtworkBitmap(context, uri, 512, resourceTracker = diagnostics.resources))
            assertEquals(0, diagnostics.captureJourneyDelta(baseline).traceSections.count { it == "artwork_decode" })

            val cancelled = async(start = CoroutineStart.UNDISPATCHED) {
                loadArtworkBitmapAwaitingAdmission(context, uri, 512, resourceTracker = diagnostics.resources)
            }
            assertFalse(cancelled.isCompleted)
            withTimeout(5_000L) { cancelled.cancelAndJoin() }
            val visibleRequests = List(16) {
                async(start = CoroutineStart.UNDISPATCHED) {
                    loadArtworkBitmapAwaitingAdmission(
                        context,
                        fixtureUri,
                        96,
                        ArtworkPurpose.Notification,
                        diagnostics.resources,
                    )
                }
            }
            visibleRequests.forEach { assertFalse(it.isCompleted) }
            assertEquals(0, diagnostics.captureJourneyDelta(baseline).traceSections.count { it == "artwork_decode" })

            release.countDown()
            pending.forEach { it.get(10, TimeUnit.SECONDS) }
            withTimeout(5_000L) {
                val loaded = requireNotNull(visibleRequests.first().await())
                assertEquals(source.getPixel(16, 8), loaded.getPixel(16, 8))
                visibleRequests.forEach { assertSame(loaded, it.await()) }
            }
            assertEquals(1, diagnostics.captureJourneyDelta(baseline).traceSections.count { it == "artwork_decode" })
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
