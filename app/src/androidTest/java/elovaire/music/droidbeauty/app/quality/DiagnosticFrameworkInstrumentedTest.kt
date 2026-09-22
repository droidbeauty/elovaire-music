package elovaire.music.droidbeauty.app.quality

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.DebugStrictModeInstaller
import elovaire.music.droidbeauty.app.core.backend.BackendOperationMonitor
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendSubsystem
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticFrameworkInstrumentedTest {
    @Test
    fun selfTestCorrelatesOutcomesTraceStrictModeAndResourceBaseline() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val runtime = appDiagnostics(context)
        assertSame(runtime.resources, appDiagnostics(context).resources)
        val baseline = runtime.captureJourneyBaseline()
        val operationIds = generateSequence(1) { it + 1 }.iterator()
        val monitor = BackendOperationMonitor(
            sink = runtime,
            operationIdGenerator = { "diagnostic-${operationIds.next()}" },
        )

        monitor.run(BackendSubsystem.Persistence) { Unit }
        try {
            monitor.run(BackendSubsystem.NetworkLibrary) { error("diagnostic failure") }
        } catch (_: IllegalStateException) {
            // The original failure is intentionally rethrown after recording it.
        }
        try {
            monitor.run(BackendSubsystem.UserData) { throw CancellationException("diagnostic cancellation") }
        } catch (_: CancellationException) {
            // Cancellation remains cancellation after recording.
        }
        runtime.recordWorkerFailure("diagnostic-worker", IllegalStateException("private title and path"))
        ElovaireTrace.section("diagnostic_self_test") { Unit }
        val lease = runtime.resources.acquire(BackendResourceKind.ActiveRetriever)
        lease.close()

        val file = File(context.cacheDir, "diagnostic-strict-mode-${System.nanoTime()}.tmp")
        val completed = CountDownLatch(1)
        instrumentation.runOnMainSync {
            DebugStrictModeInstaller.install()
            file.writeText("diagnostic")
            file.readText()
            file.delete()
            completed.countDown()
        }

        assertTrue(completed.await(2, TimeUnit.SECONDS))
        val mainQueueDrained = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post(mainQueueDrained::countDown)
        assertTrue(mainQueueDrained.await(2, TimeUnit.SECONDS))

        assertFalse(file.exists())
        val delta = runtime.captureJourneyDelta(baseline)
        val operationEvents = delta.backendEvents.filter { it.name.startsWith("Operation") }
        assertEquals(3, operationEvents.mapNotNull { it.fields["operation_id"] }.distinct().size)
        assertTrue(delta.backendEvents.any { it.name == "OperationFailed" && it.fields["error_type"] == "IllegalStateException" })
        assertTrue(delta.backendEvents.any { it.name == "OperationCancelled" })
        assertTrue(delta.workerFailures.any {
            it.fields["owner"] == "diagnostic-worker" && it.fields["error_type"] == "IllegalStateException"
        })
        assertFalse(delta.backendEvents.any { event ->
            event.fields.values.any { value -> "private title" in value || "private path" in value }
        })
        assertTrue("diagnostic_self_test" in delta.traceSections)
        assertFalse("active_retrievers" in delta.resourceDeltas)
        val strictTypes = delta.strictModeViolations.map { it.violationType }.toSet()
        assertTrue("DiskWriteViolation" in strictTypes)
        assertTrue("DiskReadViolation" in strictTypes)
        assertEquals(0, delta.droppedBackendEvents)
    }
}
