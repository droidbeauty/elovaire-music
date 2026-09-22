package elovaire.music.droidbeauty.app.core.backend

import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendDiagnosticsTest {
    @Test
    fun operationOutcomesKeepOneCorrelationAndPreserveCancellation() = runBlocking {
        val sink = RecordingBackendEventSink()
        val monitor = BackendOperationMonitor(sink, TestClock(), OperationIdGenerator { "operation-1" })

        monitor.run(BackendSubsystem.Library) { Unit }
        try {
            monitor.run(BackendSubsystem.Persistence) { error("test failure") }
        } catch (_: IllegalStateException) {
            // The monitor records the failure and preserves the original exception.
        }
        val cancellation = CancellationException("test cancellation")
        try {
            monitor.run(BackendSubsystem.UserData) { throw cancellation }
        } catch (actual: CancellationException) {
            assertEquals(cancellation, actual)
        }

        val events = sink.snapshot()
        assertEquals(6, events.size)
        assertEquals(
            setOf("OperationStarted", "OperationCompleted", "OperationFailed", "OperationCancelled"),
            events.map { it.name }.toSet(),
        )
        assertTrue(events.all { it.fields["operation_id"] == "operation-1" })
        assertEquals(
            "IllegalStateException",
            events.first { it.name == "OperationFailed" }.fields["error_type"],
        )
        assertFalse(events.any { it.fields.values.any { value -> value.contains("test failure") } })
    }

    @Test
    fun fieldsAreBoundedAndDoNotAcceptSecretOrContentFields() {
        val fields = BackendOperationContext(
            id = "operation-1",
            subsystem = BackendSubsystem.NetworkLibrary,
            startedAtElapsedMs = 10L,
        ).fields(
            phase = "network_listing",
            elapsedTimeMs = 20L,
            extra = mapOf(
                "retry" to "1",
                "password" to "secret",
                "uri" to "content://private",
            ),
            metrics = BackendOperationMetrics(
                itemsInput = 4,
                itemsOutput = 3,
                rowsRead = 10,
                rowsChanged = 2,
                bytesRead = 100L,
                bytesWritten = 50L,
                cache = "hit",
                fallback = false,
            ),
        )

        assertEquals("1", fields["retry"])
        assertEquals("4", fields["items_input"])
        assertEquals("hit", fields["cache"])
        assertFalse("password" in fields)
        assertFalse("uri" in fields)
        assertTrue(fields.values.all { it.length <= 128 })
    }

    @Test
    fun resourceRegistryReturnsToBaselineAfterIdempotentRelease() {
        BackendResourceRegistry.clear()
        val lease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveRetriever)
        assertEquals(1, BackendResourceRegistry.snapshot()["active_retrievers"])
        lease.close()
        lease.close()
        assertEquals(emptyMap<String, Int>(), BackendResourceRegistry.snapshot())
    }

    @Test
    fun resourceRegistryAdjustReturnsToBaseline() {
        BackendResourceRegistry.clear()

        BackendResourceRegistry.adjust(BackendResourceKind.WaitingNetworkBackgroundRead, 2)
        assertEquals(2, BackendResourceRegistry.snapshot()["waiting_network_background_reads"])
        BackendResourceRegistry.adjust(BackendResourceKind.WaitingNetworkBackgroundRead, -1)
        BackendResourceRegistry.adjust(BackendResourceKind.WaitingNetworkBackgroundRead, -1)

        assertEquals(emptyMap<String, Int>(), BackendResourceRegistry.snapshot())
    }

    @Test
    fun appScopedDiagnosticsDoNotLeakEventsResourcesOrBreadcrumbs() {
        val first = BackendDiagnosticsRuntime()
        val second = BackendDiagnosticsRuntime()
        var firstBreadcrumbs = 0
        var secondBreadcrumbs = 0
        first.installBreadcrumbCheckpoint { firstBreadcrumbs += 1 }
        second.installBreadcrumbCheckpoint { secondBreadcrumbs += 1 }

        val firstLease = first.resources.acquire(BackendResourceKind.ActiveRetriever)
        second.emit(BackendEvent.WorkerFailed(mapOf("owner" to "second")))

        assertEquals(1, first.resources.snapshot()["active_retrievers"])
        assertEquals(emptyMap<String, Int>(), second.resources.snapshot())
        assertTrue(first.snapshot().isEmpty())
        assertEquals(listOf("WorkerFailed"), second.snapshot().map { it.name })
        assertEquals(0, firstBreadcrumbs)
        assertEquals(1, secondBreadcrumbs)

        firstLease.close()
        first.close()
        assertEquals(emptyMap<String, Int>(), first.resources.snapshot())
        assertEquals(listOf("WorkerFailed"), second.snapshot().map { it.name })
        assertEquals(1, secondBreadcrumbs)

        first.emit(BackendEvent.WorkerFailed(mapOf("owner" to "released")))
        assertTrue(first.snapshot().isEmpty())
    }

    @Test
    fun appRuntimeKeepsASequencedBoundedPrivacySafeEventWindow() {
        val runtime = BackendDiagnosticsRuntime(maxEvents = 2)
        runtime.emit(BackendEvent.WorkerFailed(mapOf("owner" to "worker-one", "uri" to "content://private/item")))
        val baseline = runtime.sequenceMarker()
        runtime.emit(BackendEvent.WorkerFailed(mapOf("owner" to "worker-two")))
        runtime.emit(BackendEvent.WorkerFailed(mapOf("owner" to "worker-three")))
        runtime.emit(BackendEvent.WorkerFailed(mapOf("owner" to "worker-four")))

        val (latest, delta) = runtime.eventsSince(baseline)
        assertEquals(2, runtime.snapshot().size)
        assertEquals(3L, latest - baseline)
        assertEquals(2, delta.size)
        assertEquals(setOf("worker-three", "worker-four"), delta.map { it.fields["owner"] }.toSet())
        assertTrue(delta.all { "uri" !in it.fields })
        runtime.close()
    }

    private class TestClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 100L
    }
}
