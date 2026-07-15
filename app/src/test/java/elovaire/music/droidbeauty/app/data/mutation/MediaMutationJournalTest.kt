package elovaire.music.droidbeauty.app.data.mutation

import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendEventSink
import elovaire.music.droidbeauty.app.data.library.db.LibraryDao
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaMutationJournalTest {
    @Test
    fun validPipelineTransitions_areAccepted() {
        assertTrue(isValidMutationTransition(MediaMutationStatus.Created, MediaMutationStatus.PreflightPassed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten))
        assertTrue(isValidMutationTransition(MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified))
        assertTrue(isValidMutationTransition(MediaMutationStatus.TempVerified, MediaMutationStatus.Committed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Committed, MediaMutationStatus.PersistedVerified))
        assertTrue(isValidMutationTransition(MediaMutationStatus.PersistedVerified, MediaMutationStatus.Completed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.NeedsPermission, MediaMutationStatus.PermissionGranted))
        assertTrue(isValidMutationTransition(MediaMutationStatus.PermissionGranted, MediaMutationStatus.TempWritten))
    }

    @Test
    fun terminalAndSkippedTransitions_areRejected() {
        assertFalse(isValidMutationTransition(MediaMutationStatus.Created, MediaMutationStatus.Completed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.Completed, MediaMutationStatus.Failed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.Cancelled, MediaMutationStatus.PreflightPassed))
        assertFalse(isValidMutationTransition(MediaMutationStatus.TempWritten, MediaMutationStatus.TempWritten))
    }

    @Test
    fun failureTransitions_remainAvailableBeforeCompletion() {
        assertTrue(isValidMutationTransition(MediaMutationStatus.PreflightPassed, MediaMutationStatus.Failed))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Committed, MediaMutationStatus.NeedsRepair))
        assertTrue(isValidMutationTransition(MediaMutationStatus.Failed, MediaMutationStatus.NeedsRepair))
    }

    @Test
    fun recoveryClassifiesInterruptedMutationByDurabilityPhase() {
        assertEquals(MediaMutationStatus.Cancelled, recoveryStatusFor(MediaMutationStatus.TempVerified))
        assertEquals(MediaMutationStatus.NeedsRepair, recoveryStatusFor(MediaMutationStatus.Committed))
        assertEquals(MediaMutationStatus.Completed, recoveryStatusFor(MediaMutationStatus.PersistedVerified))
        assertNull(recoveryStatusFor(MediaMutationStatus.NeedsRepair))
    }

    @Test
    fun correlatedPermissionRetryDoesNotResetExistingJournalState() = runBlocking {
        val stored = mutableMapOf<String, LibraryMutationEntity>()
        val dao = libraryDao(stored)
        val journal = MediaMutationJournal(
            dao = dao,
            clock = FixedClock,
            operationIdGenerator = { "generated" },
            backendEventSink = NoOpBackendEventSink,
        )
        val operation = MediaMutationOperation(
            mutationId = "permission-retry",
            type = MediaMutationType.EmbeddedLyricsWrite,
        )

        journal.create(operation)
        journal.mark("permission-retry", MediaMutationStatus.PreflightPassed)
        journal.mark("permission-retry", MediaMutationStatus.NeedsPermission)
        journal.create(operation)

        assertEquals(MediaMutationStatus.NeedsPermission.name, stored.getValue("permission-retry").status)
        journal.mark("permission-retry", MediaMutationStatus.PermissionGranted)
        assertEquals(MediaMutationStatus.PermissionGranted.name, stored.getValue("permission-retry").status)
        journal.mark("permission-retry", MediaMutationStatus.Failed)
    }

    @Test
    fun startupRecoveryDoesNotTouchActiveForegroundMutation() = runBlocking {
        val stored = mutableMapOf<String, LibraryMutationEntity>()
        val dao = libraryDao(stored)
        val foregroundJournal = MediaMutationJournal(dao, FixedClock, { "active" }, NoOpBackendEventSink)
        val recoveryJournal = MediaMutationJournal(dao, FixedClock, { "recovery" }, NoOpBackendEventSink)

        foregroundJournal.create(MediaMutationOperation(type = MediaMutationType.TagEdit))
        val result = recoveryJournal.recoverIncomplete()

        assertEquals(MediaMutationStatus.Created.name, stored.getValue("active").status)
        assertEquals(0, (result as MediaMutationRecoveryResult.Success).recoveredCount)
        foregroundJournal.mark("active", MediaMutationStatus.Cancelled)
    }

    private fun libraryDao(stored: MutableMap<String, LibraryMutationEntity>): LibraryDao {
        return Proxy.newProxyInstance(
            LibraryDao::class.java.classLoader,
            arrayOf(LibraryDao::class.java),
        ) { _, method, arguments ->
            when (method.name) {
                "mutation" -> stored[arguments?.get(0) as String]
                "upsertMutation" -> {
                    val entity = arguments?.get(0) as LibraryMutationEntity
                    stored[entity.mutationId] = entity
                    Unit
                }
                "recoverableMutations" -> stored.values.toList()
                "toString" -> "TestLibraryDao"
                else -> error("Unexpected DAO call: ${method.name}")
            }
        } as LibraryDao
    }

    private object FixedClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 500L
    }
}
