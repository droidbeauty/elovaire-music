package elovaire.music.droidbeauty.app.data.library

import android.database.MatrixCursor
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CancellableContentResolverQueryTest {
    @Test
    fun cancellationSignalsAProviderWhileItsQueryIsBlocked() = runBlocking {
        val started = CountDownLatch(1)
        val cancelled = CountDownLatch(1)
        val query = launch(Dispatchers.IO) {
            cancellableCursorQuery { signal ->
                signal.setOnCancelListener { cancelled.countDown() }
                started.countDown()
                check(cancelled.await(5, TimeUnit.SECONDS))
                null
            }
        }
        try {
            assertTrue(withContext(Dispatchers.IO) { started.await(5, TimeUnit.SECONDS) })
            query.cancel()
            assertTrue(withContext(Dispatchers.IO) { cancelled.await(5, TimeUnit.SECONDS) })
            withTimeout(5_000L) { query.join() }
        } finally {
            query.cancel()
        }
    }

    @Test
    fun cursorReturnedAfterCancellationIsClosed() = runBlocking {
        val started = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val cursor = MatrixCursor(arrayOf("id"))
        val query = launch(Dispatchers.IO) {
            cancellableCursorQuery {
                started.countDown()
                check(proceed.await(5, TimeUnit.SECONDS))
                cursor
            }
        }
        try {
            assertTrue(withContext(Dispatchers.IO) { started.await(5, TimeUnit.SECONDS) })
            query.cancel()
            proceed.countDown()
            withTimeout(5_000L) { query.join() }
            assertTrue(cursor.isClosed)
        } finally {
            proceed.countDown()
            query.cancel()
            cursor.close()
        }
    }
}
