package elovaire.music.droidbeauty.app.data.library.network

import java.io.ByteArrayInputStream
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkBackendSafetyTest {
    @Test
    fun smbConcurrentRequestsReuseAnUnconnectedSession() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val clock = object : elovaire.music.droidbeauty.app.core.AppClock {
            override fun wallTimeMs() = 0L
            override fun elapsedTimeMs() = 0L
        }
        val fileSystem = SmbNetworkFileSystem(scope, clock)
        try {
            val sessionFor = SmbNetworkFileSystem::class.java.getDeclaredMethod(
                "sessionFor", NetworkLibrarySource::class.java, NetworkCredentials::class.java,
            ).apply { isAccessible = true }
            val source = networkSource(NetworkLibraryProtocol.Smb)
            assertSame(sessionFor.invoke(fileSystem, source, credentials()), sessionFor.invoke(fileSystem, source, credentials()))
        } finally {
            fileSystem.release()
            scope.cancel()
        }
    }

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

    @Test
    fun zeroLengthWebDavReadDoesNotOpenAConnection() {
        val source = networkSource(NetworkLibraryProtocol.WebDav)
        val fileSystem = WebDavNetworkFileSystem()

        fileSystem.openBlocking(source, credentials(), "track.mp3", 0L, 0L).use { handle ->
            assertEquals(0L, handle.length)
            assertEquals(-1, handle.input.read())
        }
    }

    @Test
    fun zeroLengthSmbReadDoesNotAcquireASession() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val fileSystem = SmbNetworkFileSystem(scope)
        try {
            fileSystem.openBlocking(networkSource(NetworkLibraryProtocol.Smb), credentials(), "track.mp3", 0L, 0L)
                .use { handle ->
                    assertEquals(0L, handle.length)
                    assertEquals(-1, handle.input.read())
                }
        } finally {
            fileSystem.release()
            scope.cancel()
        }
    }

    private fun networkSource(protocol: NetworkLibraryProtocol) = NetworkLibrarySource(
        id = "source",
        name = "Source",
        protocol = protocol,
        server = "not-a-valid-server",
        shareOrPath = "share",
        username = "",
        credentialKey = "credential",
    )

    private fun credentials() = NetworkCredentials(username = "", password = "")
}
