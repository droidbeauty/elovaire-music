package elovaire.music.droidbeauty.app.quality

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.DebugStrictModeInstaller
import elovaire.music.droidbeauty.app.core.StrictModeViolationRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendOperationMonitor
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendSubsystem
import elovaire.music.droidbeauty.app.core.backend.RecordingBackendEventSink
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticFrameworkInstrumentedTest {
    @Test
    fun selfTestCorrelatesOutcomesTraceStrictModeAndResourceBaseline() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val sink = RecordingBackendEventSink()
        val operationIds = generateSequence(1) { it + 1 }.iterator()
        val monitor = BackendOperationMonitor(
            sink = sink,
            operationIdGenerator = { "diagnostic-${operationIds.next()}" },
        )
        BackendResourceRegistry.clear()
        StrictModeViolationRecorder.clear()
        ElovaireTrace.clearRecordedSections()

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
        ElovaireTrace.section("diagnostic_self_test") { Unit }
        val lease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveRetriever)
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
        Handler(Looper.getMainLooper()).post { }
        Thread.sleep(200L)

        val events = sink.snapshot()
        assertEquals(6, events.size)
        assertEquals(3, events.map { it.fields["operation_id"] }.distinct().size)
        assertTrue(events.any { it.name == "OperationFailed" && it.fields["error_type"] == "IllegalStateException" })
        assertTrue(events.any { it.name == "OperationCancelled" })
        assertTrue("diagnostic_self_test" in ElovaireTrace.recordedSectionNames())
        assertFalse(file.exists())
        assertEquals(emptyMap<String, Int>(), BackendResourceRegistry.snapshot())
        assertTrue(StrictModeViolationRecorder.snapshot().isNotEmpty())
    }
}
