package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.content.ContextWrapper
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendEventSink
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.quality.deleteTestOwnedRows
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryMainSafetyInstrumentedTest {
    @Test
    fun scanPreparationDoesNotPublishAfterPermissionIsRevoked() = runBlocking {
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val dispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.Default.dispatch(context) {
                    try {
                        entered.countDown()
                        check(proceed.await(10, TimeUnit.SECONDS))
                        block.run()
                    } finally {
                        finished.countDown()
                    }
                }
            }
        }
        try {
            withRepository(defaultDispatcher = dispatcher) { repository, _ ->
                withContext(Dispatchers.Main) {
                    repository.start()
                    repository.onPermissionChanged(true)
                }
                assertTrue(withContext(Dispatchers.IO) { entered.await(10, TimeUnit.SECONDS) })
                withContext(Dispatchers.Main) { repository.onPermissionChanged(false) }
                proceed.countDown()
                assertTrue(withContext(Dispatchers.IO) { finished.await(10, TimeUnit.SECONDS) })
                withContext(Dispatchers.Main) {
                    assertTrue(!repository.scanState.value.permissionGranted)
                    assertTrue(!repository.scanState.value.isAuthoritative)
                }
            }
        } finally {
            proceed.countDown()
        }
    }

    @Test
    fun scanPreparationRunsOffMainAndRetriesAfterDeletionMarkersChange() = runBlocking {
        verifyScanPreparation(directPath = false)
    }

    @Test
    fun scanPreparationRetriesAfterDirectPathDeletion() = runBlocking {
        verifyScanPreparation(directPath = true)
    }

    @Test
    fun resolvingAnAbsentDirectPathDoesNotRepeatScanPreparation() = runBlocking {
        verifyScanPreparation(directPath = false, resolvedOnly = true)
    }

    @Test
    fun resolvingAnAbsentConfirmedSongDoesNotRepeatScanPreparation() = runBlocking {
        verifyScanPreparation(directPath = false, resolvedOnly = true, confirmedOnly = true)
    }

    private suspend fun verifyScanPreparation(
        directPath: Boolean,
        resolvedOnly: Boolean = false,
        confirmedOnly: Boolean = false,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val base = instrumentation.targetContext
        val fixture = base.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "elovaire-scan-race-${UUID.randomUUID()}.wav")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireScanRace")
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            },
        ) ?: error("Unable to create scan fixture")
        val fixtureId = ContentUris.parseId(fixture)
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val dispatches = java.util.concurrent.atomic.AtomicInteger()
        val repeatedBeforePublication = AtomicBoolean(false)
        lateinit var activeRepository: LibraryRepository
        val dispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.Default.dispatch(context) {
                    val count = dispatches.incrementAndGet()
                    if (count > 1 && !activeRepository.scanState.value.isAuthoritative) {
                        repeatedBeforePublication.set(true)
                    }
                    if (count == 1) {
                        entered.countDown()
                        check(proceed.await(10, TimeUnit.SECONDS))
                    }
                    block.run()
                }
            }
        }
        try {
            instrumentation.context.assets.open("media-metadata/write-fixture.wav").use { input ->
                base.contentResolver.openOutputStream(fixture)!!.use { input.copyTo(it) }
            }
            base.contentResolver.update(fixture, ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }, null, null)
            val folder = LibraryFolderSelection(
                uri = null,
                path = File(MediaFilePathResolver.defaultMusicDirectory(), "ElovaireScanRace").absolutePath,
                displayName = "Scan race",
            )
            val scanner = MediaStoreScanner(base).apply { setLibraryFolders(listOf(folder)) }
            val fixtureSong = withContext(Dispatchers.IO) {
                scanner.scan(refreshMediaIndex = false, enrichMetadata = false).single { it.id == fixtureId }
            }
            withRepository(defaultDispatcher = dispatcher, folder = folder) { repository, _ ->
                activeRepository = repository
                @Suppress("UNCHECKED_CAST")
                val tombstones = LibraryRepository::class.java.getDeclaredField("directPathTombstones")
                    .apply { isAccessible = true }.get(repository) as MutableSet<String>
                val lock = requireNotNull(LibraryRepository::class.java.getDeclaredField("directPathTombstonesLock")
                    .apply { isAccessible = true }.get(repository))
                val path = requireNotNull(LibrarySongDuplicateResolver.normalizedRealPath(fixtureSong.libraryPath))
                if (confirmedOnly) {
                    val markers = LibraryRepository::class.java.getDeclaredField("deletionMarkers")
                        .apply { isAccessible = true }.get(repository) as LibraryDeletionMarkers
                    markers.confirmDeletedSongs(listOf(9_000_000_001L))
                } else if (directPath || resolvedOnly) {
                    synchronized(lock) { tombstones.add("/missing/old-track.wav") }
                }
                withContext(Dispatchers.Main) {
                    repository.start()
                    repository.onPermissionChanged(true)
                }
                assertTrue(withContext(Dispatchers.IO) { entered.await(10, TimeUnit.SECONDS) })
                withContext(Dispatchers.Main) {
                    if (directPath) synchronized(lock) { tombstones.add(path) }
                    else if (!resolvedOnly) repository.markDeletingSongs(listOf(fixtureId))
                }
                proceed.countDown()
                withTimeout(10_000L) { repository.scanState.first { it.isAuthoritative && !it.isLoading } }
                if (resolvedOnly) {
                    withTimeout(10_000L) { repository.contentState.first { state -> state.songs.any { it.id == fixtureId } } }
                    assertTrue(!repeatedBeforePublication.get())
                    return@withRepository
                }
                assertTrue(dispatches.get() >= 2)
                assertEquals(if (directPath) emptySet<Long>() else setOf(fixtureId), repository.contentState.value.removingSongIds)
                assertTrue(repository.contentState.value.songs.none { it.id == fixtureId })
                withContext(Dispatchers.Main) {
                    if (directPath) synchronized(lock) { tombstones.remove(path) }
                    else repository.clearPendingDeletedSongs(listOf(fixtureId))
                    repository.refresh(showLoadingIndicator = false)
                }
                withTimeout(10_000L) { repository.contentState.first { state -> state.songs.any { it.id == fixtureId } } }
            }
        } finally {
            proceed.countDown()
            base.contentResolver.deleteTestOwnedRows(listOf(fixture), "scan race")
        }
    }

    @Test
    fun cachedStartupPreparationKeepsDeletionMarkersChangedDuringBackgroundWork() = runBlocking {
        verifyCachedStartup(revokePermission = false)
    }

    @Test
    fun cachedStartupDoesNotPublishAfterPermissionIsRevoked() = runBlocking {
        verifyCachedStartup(revokePermission = true)
    }

    @Test
    fun cachedStartupDoesNotRestoreADeletedSong() = runBlocking {
        verifyCachedStartup(revokePermission = false, deleteDuringPreparation = true)
    }

    @Test
    fun cachedStartupKeepsOtherSongsAfterDeletion() = runBlocking {
        verifyCachedStartup(revokePermission = false, deleteDuringPreparation = true, keepAnotherSong = true)
    }

    private suspend fun verifyCachedStartup(
        revokePermission: Boolean,
        deleteDuringPreparation: Boolean = false,
        keepAnotherSong: Boolean = false,
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "library-startup-safety-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = directory
        }
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val gateIo = AtomicBoolean(false)
        val releaseIo = CountDownLatch(1)
        val ioDispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.IO.dispatch(context) {
                    if (gateIo.get()) releaseIo.await(30, TimeUnit.SECONDS)
                    block.run()
                }
            }
        }
        val firstDispatch = AtomicBoolean(true)
        val dispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.Default.dispatch(context) {
                    val gated = firstDispatch.compareAndSet(true, false)
                    try {
                        if (gated) {
                            entered.countDown()
                            check(proceed.await(10, TimeUnit.SECONDS))
                        }
                        block.run()
                    } finally {
                        if (gated) finished.countDown()
                    }
                }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val scanner = LibraryScanCoordinator(
            localScanner = MediaStoreScanner(context),
            safScanner = SafTreeLibraryScanner(context),
            networkScannerProvider = { error("No network source is used") },
        )
        val snapshot = LibrarySnapshotAssembler.assembleDeduplicatedSongs(
            listOf(song(9_000_000_001L)) + if (keepAnotherSong) listOf(song(9_000_000_002L)) else emptyList(),
        )
        val store = LibrarySnapshotStore(context)
        withContext(Dispatchers.IO) { store.save(snapshot, scanner.currentFilterFingerprint()) }
        val repository = LibraryRepository(
            appContext = context,
            scanner = scanner,
            scope = scope,
            backgroundWorkPolicy = AppBackgroundWorkPolicy(MutableStateFlow(false)),
            backendEventSink = NoOpBackendEventSink,
            defaultDispatcher = dispatcher,
            ioDispatcher = ioDispatcher,
            snapshotStore = store,
        )
        try {
            if (deleteDuringPreparation) {
                @Suppress("UNCHECKED_CAST")
                val state = LibraryRepository::class.java.getDeclaredField("_contentState")
                    .apply { isAccessible = true }.get(repository) as MutableStateFlow<LibraryContentState>
                state.value = LibraryContentState(
                    songs = snapshot.songs,
                    albums = snapshot.albums,
                    audiobooks = snapshot.audiobooks,
                    contentRevision = snapshot.contentRevision,
                )
            }
            withContext(Dispatchers.Main) {
                repository.start()
                repository.onPermissionChanged(true)
            }
            assertTrue(withContext(Dispatchers.IO) { entered.await(10, TimeUnit.SECONDS) })
            withContext(Dispatchers.Main) {
                if (revokePermission) repository.onPermissionChanged(false)
                else if (deleteDuringPreparation) {
                    val song = snapshot.songs.first()
                    val result = repository.refreshAfterDelete(
                        LibraryDeleteRequest(
                            songIds = setOf(song.id),
                            albumIds = setOf(song.albumId),
                            uris = setOf(song.uri),
                            filePaths = emptySet(),
                            uriBySongId = mapOf(song.id to song.uri),
                        ),
                    )
                    assertEquals(setOf(song.id), result.deletedSongIds)
                    gateIo.set(true)
                }
                else repository.markDeletingSongs(listOf(snapshot.songs.single().id))
            }
            proceed.countDown()
            if (revokePermission) {
                assertTrue(withContext(Dispatchers.IO) { finished.await(10, TimeUnit.SECONDS) })
                withContext(Dispatchers.Main) { assertTrue(repository.contentState.value.songs.isEmpty()) }
            } else if (deleteDuringPreparation) {
                withTimeout(10_000L) { repository.scanState.first { it.scanProgress == 1f } }
                assertEquals(snapshot.songs.drop(1), repository.contentState.value.songs)
            } else {
                val state = withTimeout(10_000L) { repository.contentState.first { it.songs.isNotEmpty() } }
                assertEquals(setOf(snapshot.songs.single().id), state.removingSongIds)
            }
        } finally {
            proceed.countDown()
            releaseIo.countDown()
            withContext(Dispatchers.Main) { repository.release() }
            scope.coroutineContext[Job]?.cancelAndJoin()
            directory.deleteRecursively()
        }
    }

    @Test
    fun verifiedMetadataPatchReadsLibraryOffMainThread() = runBlocking {
        withRepository { repository, songs ->
            withContext(Dispatchers.Main) {
                repository.applyVerifiedTagEdits(listOf(songs.first().copy(title = "Edited")))
            }
            assertEquals("Edited", repository.contentState.value.songs.first().title)
        }
    }

    @Test
    fun verifiedMetadataPatchPreservesDeletionMarkersChangedDuringPreparation() = runBlocking {
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val dispatches = java.util.concurrent.atomic.AtomicInteger()
        val dispatcher = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Dispatchers.IO.dispatch(context) {
                    if (dispatches.incrementAndGet() == 2) {
                        entered.countDown()
                        check(proceed.await(10, TimeUnit.SECONDS))
                    }
                    block.run()
                }
            }
        }
        try {
            withRepository(ioDispatcher = dispatcher) { repository, songs ->
                val edit = async(Dispatchers.Main) {
                    repository.applyVerifiedTagEdits(listOf(songs.first().copy(title = "Edited")))
                }
                assertTrue(withContext(Dispatchers.IO) { entered.await(10, TimeUnit.SECONDS) })
                withContext(Dispatchers.Main) {
                    repository.markDeletingSongs(listOf(songs.last().id))
                    repository.markDeletingAlbums(listOf(songs.last().albumId))
                }
                proceed.countDown()
                edit.await()
                assertEquals(setOf(songs.last().id), repository.contentState.value.removingSongIds)
                assertEquals(setOf(songs.last().albumId), repository.contentState.value.removingAlbumIds)
            }
        } finally {
            proceed.countDown()
        }
    }

    @Test
    fun deleteRefreshReadsLibraryOffMainThread() = runBlocking {
        withRepository { repository, songs ->
            val removed = songs.first()
            val result = withContext(Dispatchers.Main) {
                repository.refreshAfterDelete(
                    LibraryDeleteRequest(
                        songIds = setOf(removed.id),
                        albumIds = setOf(removed.albumId),
                        uris = setOf(removed.uri),
                        filePaths = emptySet(),
                        uriBySongId = mapOf(removed.id to removed.uri),
                    ),
                )
            }
            assertEquals(setOf(removed.id), result.deletedSongIds)
            assertEquals(listOf(songs.last().id), repository.contentState.value.songs.map(Song::id))
        }
    }

    private suspend fun withRepository(
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
        folder: LibraryFolderSelection? = null,
        block: suspend (LibraryRepository, List<Song>) -> Unit,
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "library-main-safety-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = directory
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val scanner = LibraryScanCoordinator(
            localScanner = MediaStoreScanner(context).apply { if (folder != null) setLibraryFolders(listOf(folder)) },
            safScanner = SafTreeLibraryScanner(context),
            networkScannerProvider = { error("No network source is used") },
        )
        val repository = LibraryRepository(
            appContext = context,
            scanner = scanner,
            scope = scope,
            backgroundWorkPolicy = AppBackgroundWorkPolicy(MutableStateFlow(false)),
            backendEventSink = NoOpBackendEventSink,
            ioDispatcher = ioDispatcher,
            defaultDispatcher = defaultDispatcher,
        )
        val songs = listOf(song(9_000_000_001L), song(9_000_000_002L))
        val snapshot = LibrarySnapshotAssembler.assembleDeduplicatedSongs(songs)
        val checkedSongs = object : AbstractList<Song>() {
            override val size: Int get() = songs.size
            override fun get(index: Int): Song {
                check(Looper.myLooper() != Looper.getMainLooper()) { "Library traversal ran on the main thread" }
                return songs[index]
            }
        }
        @Suppress("UNCHECKED_CAST")
        val state = LibraryRepository::class.java.getDeclaredField("_contentState")
            .apply { isAccessible = true }.get(repository) as MutableStateFlow<LibraryContentState>
        state.value = LibraryContentState(
            songs = checkedSongs,
            albums = snapshot.albums,
            contentRevision = snapshot.contentRevision,
            portableMediaIdentityRevision = MediaIdentityResolver.portableIdentityRevision(songs),
        )
        try {
            block(repository, songs)
        } finally {
            withContext(Dispatchers.Main) { repository.release() }
            scope.cancel()
            directory.deleteRecursively()
        }
    }

    private fun song(id: Long): Song = Song(
        id = id,
        title = "Track $id",
        isExplicit = false,
        artist = "Artist",
        album = "Album $id",
        releaseYear = null,
        genre = "Genre",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "$id.mp3",
        albumId = id,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = Uri.parse("content://media/external/audio/media/$id"),
        artUri = null,
    )
}
