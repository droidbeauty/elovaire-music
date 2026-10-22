package elovaire.music.droidbeauty.app.data.mutation

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendEventSink
import elovaire.music.droidbeauty.app.data.library.db.ElovaireDatabase
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.quality.deleteTestOwnedRows
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MediaFileMutationRecoveryTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resolver = context.contentResolver
    private val databaseName = "media-file-mutation-recovery-test.db"
    private lateinit var database: ElovaireDatabase
    private val journals = mutableListOf<MediaMutationJournal>()
    private val insertedUris = mutableListOf<Uri>()
    private val backups = mutableListOf<File>()

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
        openDatabase()
    }

    @After
    fun tearDown() {
        journals.forEach(MediaMutationJournal::close)
        database.close()
        context.deleteDatabase(databaseName)
        backups.forEach(File::delete)
        resolver.deleteTestOwnedRows(insertedUris, "media mutation recovery")
    }

    @Test
    fun interruptedInPlaceWriteRetainsBackupAndRecoversAfterReopen() = runBlocking {
        val song = fixtureSong()
        val original = read(song.uri)
        val journal = journal()
        val faults = MediaMutationFaultInjector { phase ->
            if (phase == MediaMutationTransactionPhase.OriginalCommitted) {
                resolver.openOutputStream(song.uri, "rwt")!!.use { it.write(0) }
                throw InterruptedWrite()
            }
        }
        val transaction = VerifiedMediaFileMutationTransaction(
            MediaFileMutationRunner(context, "media-recovery-test", faults), journal, faults,
        )
        assertThrows(InterruptedWrite::class.java) {
            runBlocking {
                transaction.execute(
                    song, operation(song),
                    mutateWorkingCopy = { it.appendBytes(byteArrayOf(1, 2, 3)) },
                    verifyWorkingCopy = {}, verifyPersistedCopy = {},
                )
            }
        }
        val pending = database.mediaMutationDao().recoverableMutations(10).single()
        val backup = File(checkNotNull(pending.recoveryBackupPath)).also(backups::add)
        assertEquals(MediaMutationStatus.TempVerified.name, pending.status)
        assertArrayEquals(original, backup.readBytes())
        assertEquals(1, read(song.uri).size)
        val maintenanceResult = journal().recoverIncomplete() as MediaMutationRecoveryResult.Success
        assertEquals(0, maintenanceResult.recoveredCount)
        assertEquals(1, maintenanceResult.remainingCount)

        journals.forEach(MediaMutationJournal::close)
        database.close()
        openDatabase()
        val failingRecovery = journal(MediaMutationFaultInjector { phase ->
            if (phase == MediaMutationTransactionPhase.OriginalOverwrite) throw IOException("Provider unavailable")
        })
        assertTrue(failingRecovery.recoverIncomplete() is MediaMutationRecoveryResult.Failure)
        assertArrayEquals(original, backup.readBytes())
        assertEquals(backup.path, database.mediaMutationDao().mutation(pending.mutationId)?.recoveryBackupPath)

        // A failed rollback is terminal for the edit, but its recovery material must stay discoverable.
        database.mediaMutationDao().upsertMutation(pending.copy(status = MediaMutationStatus.Failed.name, updatedAtMs = 0))
        assertEquals(0, database.persistenceMaintenanceDao().deleteTerminalMutationsBefore(Long.MAX_VALUE))
        assertEquals(1, database.mediaMutationDao().recoverableMutationCount())
        val recovery = journal()
        assertEquals(1, (recovery.recoverIncomplete() as MediaMutationRecoveryResult.Success).recoveredCount)
        assertArrayEquals(original, read(song.uri))
        assertNull(database.mediaMutationDao().mutation(pending.mutationId)?.recoveryBackupPath)
        assertEquals(MediaMutationStatus.Cancelled.name, database.mediaMutationDao().mutation(pending.mutationId)?.status)
        assertFalse(backup.exists())
        assertEquals(0, (recovery.recoverIncomplete() as MediaMutationRecoveryResult.Success).recoveredCount)
    }

    @Test
    fun verifiedWriteSurvivesRecoveryWithoutRestoringOldBytes() = runBlocking {
        val song = fixtureSong()
        val runner = MediaFileMutationRunner(context)
        val backup = runner.copySongToTemp(song, "backup").also(backups::add)
        val replacement = read(song.uri) + byteArrayOf(1, 2, 3)
        val journal = journal()
        val id = journal.create(operation(song))
        listOf(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified)
            .forEach { journal.mark(id, it) }
        journal.retainBackup(id, backup)
        resolver.openOutputStream(song.uri, "rwt")!!.use { it.write(replacement) }
        journal.mark(id, MediaMutationStatus.Committed)
        journal.mark(id, MediaMutationStatus.PersistedVerified)
        journal.close()
        database.close()
        openDatabase()

        assertEquals(1, (journal().recoverIncomplete() as MediaMutationRecoveryResult.Success).recoveredCount)
        assertArrayEquals(replacement, read(song.uri))
        assertEquals(MediaMutationStatus.Completed.name, database.mediaMutationDao().mutation(id)?.status)
        assertNull(database.mediaMutationDao().mutation(id)?.recoveryBackupPath)
        assertFalse(backup.exists())
    }

    @Test
    fun successfulTransactionClearsRecoveryMaterial() = runBlocking {
        val song = fixtureSong()
        val journal = journal()
        val runner = MediaFileMutationRunner(context, "media-recovery-test")
        val result = VerifiedMediaFileMutationTransaction(runner, journal, NoOpMediaMutationFaultInjector).execute(
            song, operation(song),
            mutateWorkingCopy = { it.appendBytes(byteArrayOf(1, 2, 3)) },
            verifyWorkingCopy = {}, verifyPersistedCopy = {},
        )
        assertTrue(result is VerifiedMediaFileMutationResult.Success)
        val id = (result as VerifiedMediaFileMutationResult.Success).mutationId!!
        assertEquals(MediaMutationStatus.Completed.name, database.mediaMutationDao().mutation(id)?.status)
        assertNull(database.mediaMutationDao().mutation(id)?.recoveryBackupPath)
        assertEquals(0, database.mediaMutationDao().recoverableMutationCount())
    }

    @Test
    fun failureBeforeWriteCallbackStillRestoresTruncatedProviderFile() = runBlocking {
        val song = fixtureSong()
        val original = read(song.uri)
        var injected = false
        val faults = MediaMutationFaultInjector { phase ->
            if (phase == MediaMutationTransactionPhase.OriginalOverwrite && !injected) {
                injected = true
                resolver.openOutputStream(song.uri, "rwt")!!.use { it.write(0) }
                throw IOException("Provider failed after truncation")
            }
        }
        val result = VerifiedMediaFileMutationTransaction(
            MediaFileMutationRunner(context, "media-recovery-test", faults), journal(), faults,
        ).execute(
            song, operation(song), mutateWorkingCopy = {}, verifyWorkingCopy = {}, verifyPersistedCopy = {},
        )
        assertTrue(result is VerifiedMediaFileMutationResult.Failure)
        assertFalse((result as VerifiedMediaFileMutationResult.Failure).rollbackFailed)
        assertArrayEquals(original, read(song.uri))
        assertEquals(0, database.mediaMutationDao().recoverableMutationCount())
    }

    @Test
    fun interruptedRollbackOfVerifiedWriteRemainsRecoverable() = runBlocking {
        val song = fixtureSong()
        val original = read(song.uri)
        val backup = MediaFileMutationRunner(context).copySongToTemp(song, "backup").also(backups::add)
        val journal = journal()
        val id = journal.create(operation(song))
        listOf(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified)
            .forEach { journal.mark(id, it) }
        journal.retainBackup(id, backup)
        journal.mark(id, MediaMutationStatus.Committed)
        journal.mark(id, MediaMutationStatus.PersistedVerified)
        journal.prepareRollback(id)
        resolver.openOutputStream(song.uri, "rwt")!!.use { it.write(0) }
        journal.close()
        database.close()
        openDatabase()

        assertEquals(1, (journal().recoverIncomplete() as MediaMutationRecoveryResult.Success).recoveredCount)
        assertArrayEquals(original, read(song.uri))
        assertFalse(backup.exists())
    }

    @Test
    fun permissionRetryRestoresPendingBackupBeforeReadingNewWorkingCopy() = runBlocking {
        val song = fixtureSong()
        val original = read(song.uri)
        val runner = MediaFileMutationRunner(context)
        val backup = runner.copySongToTemp(song, "backup").also(backups::add)
        val journal = journal()
        val id = journal.create(operation(song))
        listOf(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified)
            .forEach { journal.mark(id, it) }
        journal.retainBackup(id, backup)
        resolver.openOutputStream(song.uri, "rwt")!!.use { it.write(0) }
        journal.mark(id, MediaMutationStatus.NeedsPermission)
        journal.create(operation(song).copy(mutationId = id))
        journal.mark(id, MediaMutationStatus.PermissionGranted)

        val result = VerifiedMediaFileMutationTransaction(runner, journal, NoOpMediaMutationFaultInjector).execute(
            song, operation(song), mutationId = id,
            mutateWorkingCopy = { assertArrayEquals(original, it.readBytes()) },
            verifyWorkingCopy = {}, verifyPersistedCopy = {},
        )
        assertTrue(result is VerifiedMediaFileMutationResult.Success)
        assertArrayEquals(original, read(song.uri))
        assertFalse(backup.exists())
        assertEquals(0, database.mediaMutationDao().recoverableMutationCount())
    }

    @Test
    fun unchangedOriginalDoesNotNeedWriteAccessForRecovery() = runBlocking {
        val song = fixtureSong()
        val backup = MediaFileMutationRunner(context).copySongToTemp(song, "backup").also(backups::add)
        val journal = journal()
        val id = journal.create(operation(song))
        listOf(MediaMutationStatus.PreflightPassed, MediaMutationStatus.TempWritten, MediaMutationStatus.TempVerified)
            .forEach { journal.mark(id, it) }
        journal.retainBackup(id, backup)
        journal.close()

        val recovery = journal(MediaMutationFaultInjector { phase ->
            if (phase == MediaMutationTransactionPhase.OriginalOverwrite) throw SecurityException("Write access expired")
        })
        assertEquals(1, (recovery.recoverIncomplete() as MediaMutationRecoveryResult.Success).recoveredCount)
        assertEquals(0, database.mediaMutationDao().recoverableMutationCount())
        assertFalse(backup.exists())
    }

    private fun openDatabase() {
        database = Room.databaseBuilder(context, ElovaireDatabase::class.java, databaseName).build()
    }

    private fun journal(faults: MediaMutationFaultInjector = NoOpMediaMutationFaultInjector) = MediaMutationJournal(
        database.mediaMutationDao(),
        backendEventSink = NoOpBackendEventSink,
        recoveryRunner = MediaFileMutationRunner(context, faultInjector = faults),
    ).also(journals::add)

    private fun read(uri: Uri): ByteArray = resolver.openInputStream(uri)!!.use { it.readBytes() }

    private fun operation(song: Song) = MediaMutationOperation(
        type = MediaMutationType.TagEdit, songId = song.id, uri = song.uri,
    )

    private fun fixtureSong(): Song {
        val name = "elovaire-recovery-${System.nanoTime()}.mp3"
        val uri = checkNotNull(resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireWriteTest")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }))
        insertedUris += uri
        resolver.openOutputStream(uri)!!.use { output ->
            instrumentation.context.assets.open("media-metadata/write-fixture.mp3").use { it.copyTo(output) }
        }
        return Song(
            id = 1, title = "Recovery test", isExplicit = false, artist = "Test", album = "Test",
            releaseYear = null, genre = "", audioFormat = "MP3", audioQuality = null, fileName = name,
            albumId = 1, durationMs = 500, trackNumber = 1, discNumber = 1, dateAddedSeconds = 0,
            uri = uri, artUri = null,
        )
    }

    private class InterruptedWrite : Error("Simulated process interruption")
}
