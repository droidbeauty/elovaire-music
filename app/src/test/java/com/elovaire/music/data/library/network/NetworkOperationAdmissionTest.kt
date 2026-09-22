package elovaire.music.droidbeauty.app.data.library.network

import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class NetworkOperationAdmissionTest {
    @Test
    fun backgroundReadsDoNotConsumePlaybackPermits() {
        val admission = NetworkOperationAdmission(backgroundCapacity = 3, playbackCapacity = 2, maxWaitMs = 1L)
        val background = List(3) { admission.acquire(NetworkReadPurpose.Metadata) }

        val playback = admission.acquire(NetworkReadPurpose.Playback)
        assertEquals(3, admission.snapshot().activeBackground)
        assertEquals(1, admission.snapshot().activePlayback)

        playback.close()
        background.forEach(NetworkOperationAdmission.Permit::close)
        assertEquals(
            NetworkOperationAdmission.Snapshot(0, 0, 0, 0),
            admission.snapshot(),
        )
    }

    @Test
    fun exhaustedPermitFailsWithinBoundedWait() {
        BackendResourceRegistry.clear()
        val admission = NetworkOperationAdmission(backgroundCapacity = 1, playbackCapacity = 1, maxWaitMs = 1L)
        val held = admission.acquire(NetworkReadPurpose.Playback)
        try {
            assertThrows(IOException::class.java) {
                admission.acquire(NetworkReadPurpose.Playback)
            }
            assertEquals(1, admission.snapshot().activePlayback)
        } finally {
            held.close()
        }
        assertEquals(0, admission.snapshot().activePlayback)
        assertFalse("waiting_network_playback_reads" in BackendResourceRegistry.snapshot())
    }

    @Test
    fun closeRejectsNewOperations() {
        val admission = NetworkOperationAdmission(backgroundCapacity = 1, playbackCapacity = 1)

        admission.close()

        assertThrows(IOException::class.java) {
            admission.acquire(NetworkReadPurpose.Metadata)
        }
    }

    @Test
    fun readHandleClosesInputAndPermitExactlyOnce() {
        val inputCloses = AtomicInteger()
        val handleCloses = AtomicInteger()
        val handle = NetworkReadHandle(
            input = object : InputStream() {
                override fun read(): Int = -1
                override fun close() { inputCloses.incrementAndGet() }
            },
            length = 0L,
            closeHandle = { handleCloses.incrementAndGet() },
        )

        handle.close()
        handle.close()

        assertEquals(1, inputCloses.get())
        assertEquals(1, handleCloses.get())
    }
}
