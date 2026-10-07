package elovaire.music.droidbeauty.app.data.library.network

import java.io.ByteArrayInputStream
import java.util.concurrent.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkBackendSafetyTest {
    @Test
    fun boundedDirectoryConsumerStopsBeforeReadingAnotherEntry() {
        val iterator = (1..10).iterator()
        val consumed = mutableListOf<Int>()

        consumeWhile(iterator, { consumed.size < 3 }, consumed::add)

        assertEquals(listOf(1, 2, 3), consumed)
        assertEquals(4, iterator.next())
    }

    @Test
    fun networkProbeConvertsOrdinaryFailureButPreservesCancellation() {
        assertEquals(
            "failed",
            networkProbeResult<String>(
                attempt = { throw IllegalStateException("offline") },
                onFailure = { "failed" },
            ),
        )
        assertThrows(CancellationException::class.java) {
            networkProbeResult<String>(
                attempt = { throw CancellationException("cancelled") },
                onFailure = { "failed" },
            )
        }
    }

    @Test
    fun staleNetworkOpenClosesItsHandleBeforeReturningFailure() {
        var closeCount = 0
        var inputClosed = false
        val input = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun close() {
                inputClosed = true
                super.close()
            }
        }
        val handle = NetworkReadHandle(input, 1L) { closeCount += 1 }

        val failure = assertThrows(NetworkRemoteIoException::class.java) {
            requireCurrentNetworkHandle(handle) { false }
        }

        assertEquals(RemoteIoFailureKind.SourceRemoved, failure.kind)
        assertEquals(1, closeCount)
        assertTrue(inputClosed)
    }

    @Test
    fun staleNetworkOpenDoesNotMaskFatalHandleCloseFailure() {
        val fatalFailure = AssertionError("close failed")
        val handle = NetworkReadHandle(ByteArrayInputStream(byteArrayOf(1)), 1L) {
            throw fatalFailure
        }

        val thrown = assertThrows(AssertionError::class.java) {
            requireCurrentNetworkHandle(handle) { false }
        }

        assertEquals(fatalFailure, thrown)
    }

    @Test
    fun smbSessionDoesNotRetainCredentialObjects() {
        val sessionClass = Class.forName("${SmbNetworkFileSystem::class.java.name}\$SourceSession")

        assertTrue(sessionClass.declaredFields.none { it.type == NetworkCredentials::class.java })
    }
}
