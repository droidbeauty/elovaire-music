package elovaire.music.droidbeauty.app.data.settings

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsWriteSequencerTest {
    @Test
    fun orderedWritesAndLatestValuesAreSerialized() = runBlocking {
        val ownerScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val writes = mutableListOf<String>()
        val sequencer = SettingsWriteSequencer(
            ownerScope = ownerScope,
            dispatcher = Dispatchers.Unconfined,
            persist = { _, write -> write() },
            nowMs = { 0L },
        )

        sequencer.enqueue("first") {
            firstStarted.complete(Unit)
            releaseFirst.await()
            writes += "first"
        }
        firstStarted.await()
        sequencer.replaceLatest("equalizer") { writes += "old" }
        sequencer.replaceLatest("equalizer") { writes += "new" }
        sequencer.enqueue("last") { writes += "last" }
        assertTrue(sequencer.hasPendingWork())

        releaseFirst.complete(Unit)
        sequencer.flush()

        assertEquals(listOf("first", "last", "new"), writes)
        sequencer.close()
        ownerScope.cancel()
    }

    @Test
    fun one_write_failure_does_not_stop_later_persistence() = runBlocking {
        val ownerScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var failNext = true
        val writes = mutableListOf<String>()
        val sequencer = SettingsWriteSequencer(
            ownerScope = ownerScope,
            dispatcher = Dispatchers.Unconfined,
            persist = { _, write ->
                if (failNext) {
                    failNext = false
                    throw IOException("temporary")
                }
                write()
            },
            nowMs = { 0L },
        )

        sequencer.enqueue("first") { writes += "first" }
        sequencer.enqueue("second") { writes += "second" }
        sequencer.flush()

        assertEquals(listOf("second"), writes)
        sequencer.close()
        ownerScope.cancel()
    }

    @Test
    fun close_drains_accepted_writes_without_blocking_the_caller() = runBlocking {
        val ownerScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val writes = mutableListOf<String>()
        val sequencer = SettingsWriteSequencer(
            ownerScope = ownerScope,
            dispatcher = Dispatchers.Unconfined,
            persist = { _, write -> write() },
            nowMs = { 0L },
        )

        sequencer.enqueue("ordered") { writes += "ordered" }
        sequencer.replaceLatest("latest", debounceMs = 10_000L) { writes += "latest" }
        sequencer.close()

        assertEquals(listOf("ordered", "latest"), writes)
        assertTrue(!sequencer.hasPendingWork())
        ownerScope.cancel()
    }
}
