package elovaire.music.droidbeauty.app.data.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRuntime
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HttpCancellationInstrumentedTest {
    @Test
    fun completedAndOversizedResponsesCloseTheirConnectionsAndResourceLeases() = runBlocking {
        val resources = BackendResourceRuntime()
        val disconnected = CountDownLatch(2)
        val transport = BoundedHttpTransport(resourceTracker = resources, openConnection = { url ->
            object : HttpURLConnection(url) {
                override fun connect() = Unit
                override fun usingProxy() = false
                override fun getResponseCode() = 200
                override fun getInputStream() = ByteArrayInputStream("lyrics".toByteArray())
                override fun getHeaderField(name: String?): String? = null
                override fun disconnect() { disconnected.countDown() }
            }
        })
        assertEquals("lyrics", transport.get("https://lyrics.test/ok", maxBytes = 1024).body.toString(Charsets.UTF_8))
        assertThrows(IllegalStateException::class.java) {
            runBlocking { transport.get("https://lyrics.test/large", maxBytes = 2) }
        }
        assertTrue(disconnected.await(2, TimeUnit.SECONDS))
        assertTrue(resources.snapshot().values.all { it == 0 })
    }

    @Test
    fun cancelledLyricsLookupDoesNotBlockTheNextTrackUntilTheSocketTimesOut() {
        val responseStarted = CountDownLatch(1)
        val releaseResponse = CountDownLatch(1)
        val disconnected = CountDownLatch(1)
        val connection = object : HttpURLConnection(URL("https://lyrics.test/slow")) {
            override fun connect() = Unit
            override fun usingProxy() = false
            override fun getResponseCode(): Int {
                responseStarted.countDown()
                check(releaseResponse.await(10, TimeUnit.SECONDS))
                return 200
            }
            override fun getInputStream() = ByteArrayInputStream("old lyrics".toByteArray())
            override fun getHeaderField(name: String?): String? = null
            override fun disconnect() {
                releaseResponse.countDown()
                disconnected.countDown()
            }
        }
        val transport = BoundedHttpTransport(openConnection = { connection })
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val lookup = scope.launch { transport.get("https://lyrics.test/slow", maxBytes = 1024) }
        try {
            assertTrue(responseStarted.await(3, TimeUnit.SECONDS))
            lookup.cancel()
            runBlocking { withTimeout(2_000L) { lookup.join() } }
            assertTrue("Cancelled socket must close", disconnected.await(2, TimeUnit.SECONDS))
        } finally {
            releaseResponse.countDown()
            runBlocking { lookup.join() }
            scope.cancel()
        }
    }
}
