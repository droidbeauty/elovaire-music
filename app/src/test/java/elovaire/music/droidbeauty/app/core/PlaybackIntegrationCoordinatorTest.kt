package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.playback.PersistedPlaybackSession
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackEffects
import elovaire.music.droidbeauty.app.data.playback.PlaybackIntegrationPort
import elovaire.music.droidbeauty.app.data.playback.PlaybackPersistenceSnapshot
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackSessionPersistence
import elovaire.music.droidbeauty.app.data.settings.PlaybackIntegrationSettings
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import elovaire.music.droidbeauty.app.testing.FakeLibraryReader
import elovaire.music.droidbeauty.app.testing.FakePlaybackReader
import elovaire.music.droidbeauty.app.testing.FakeSearchSettingsStore
import elovaire.music.droidbeauty.app.testing.MainDispatcherRule
import elovaire.music.droidbeauty.app.testing.testSong
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.CoroutineContext

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlaybackIntegrationCoordinatorTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun newerLibrarySnapshotCancelsAnObsoleteQueueRefresh() = runTest(mainDispatcherRule.dispatcher) {
        val first = testSong(1L)
        val second = testSong(2L)
        val library = FakeLibraryReader(
            LibraryContentState(songs = listOf(first)),
            LibraryScanState(isAuthoritative = true),
        )
        val playback = SuspendingPlayback()
        val coordinator = coordinator(backgroundScope, library, playback, SessionStore())
        coordinator.start()
        runCurrent()
        assertEquals(listOf(listOf(first)), playback.started)

        library.mutableContentState.value = LibraryContentState(songs = listOf(second))
        runCurrent()

        assertTrue(playback.firstCancelled)
        assertEquals(listOf(listOf(first), listOf(second)), playback.started)
        assertEquals(listOf(listOf(second)), playback.completed)
        coordinator.release()
        runCurrent()
    }
    @Test
    fun restoresAvailableSongsInPersistedOrderIncludingDuplicates() = runTest(mainDispatcherRule.dispatcher) {
        val songs = listOf(testSong(1L), testSong(2L))
        val library = FakeLibraryReader(LibraryContentState(songs = songs), LibraryScanState(isAuthoritative = true))
        val playback = SuspendingPlayback(activeQueue = false, suspendFirst = false)
        val coordinator = coordinator(backgroundScope, library, playback, SessionStore(session(listOf(2L, 1L, 2L, 99L), 2)))

        coordinator.start()
        runCurrent()

        assertEquals(listOf(songs[1], songs[0], songs[1]), playback.restoredSongs)
        assertEquals(2, playback.restoredIndex)
        coordinator.release()
        runCurrent()
    }

    @Test
    fun userQueueStartedDuringRestorationIsNotReplacedOrCleared() = runTest(mainDispatcherRule.dispatcher) {
        val songs = listOf(testSong(1L))
        val library = FakeLibraryReader(LibraryContentState(songs = songs), LibraryScanState(isAuthoritative = true))
        val playback = SuspendingPlayback(activeQueue = false, suspendFirst = false)
        val store = SessionStore(session(listOf(1L), 0))
        val worker = PausedDispatcher()
        val coordinator = coordinator(backgroundScope, library, playback, store, worker)

        coordinator.start()
        runCurrent()
        assertEquals(1, worker.pending.size)
        playback.activeQueue = true
        worker.pending.removeFirst().run()
        runCurrent()

        assertEquals(null, playback.restoredSongs)
        assertEquals(0, store.clearCount)
        assertEquals(listOf(42L), store.saved?.queueSongIds)
        coordinator.release()
        runCurrent()
    }

    @Test
    fun newerLibrarySnapshotReplacesAnInFlightSessionLookup() = runTest(mainDispatcherRule.dispatcher) {
        val songs = listOf(testSong(1L), testSong(2L))
        val library = FakeLibraryReader(
            LibraryContentState(songs = songs.take(1)), LibraryScanState(isAuthoritative = true),
        )
        val playback = SuspendingPlayback(activeQueue = false, suspendFirst = false)
        val store = SessionStore(session(listOf(1L, 2L), 0))
        val worker = PausedDispatcher()
        val coordinator = coordinator(backgroundScope, library, playback, store, worker)

        coordinator.start()
        runCurrent()
        assertEquals(1, worker.pending.size)
        library.mutableContentState.value = LibraryContentState(songs = songs)
        runCurrent()
        worker.pending.removeFirst().run()
        runCurrent()
        assertEquals(1, worker.pending.size)
        worker.pending.removeFirst().run()
        runCurrent()

        assertEquals(songs, playback.restoredSongs)
        assertEquals(0, store.clearCount)
        coordinator.release()
        runCurrent()
    }

    @Test
    fun userQueueStartedWhileOldSessionIsClearedGetsPersisted() = runTest(mainDispatcherRule.dispatcher) {
        val library = FakeLibraryReader(LibraryContentState(), LibraryScanState(isAuthoritative = true))
        val playback = SuspendingPlayback(activeQueue = false, suspendFirst = false)
        val store = SessionStore(onClear = { playback.activeQueue = true })
        val coordinator = coordinator(backgroundScope, library, playback, store)

        coordinator.start()
        runCurrent()

        assertEquals(1, store.clearCount)
        assertEquals(listOf(42L), store.saved?.queueSongIds)
        assertEquals(null, playback.restoredSongs)
        coordinator.release()
        runCurrent()
    }

    @Test
    fun userQueueStartedDuringPlayerPreparationGetsPersisted() = runTest(mainDispatcherRule.dispatcher) {
        val songs = listOf(testSong(1L))
        val library = FakeLibraryReader(LibraryContentState(songs = songs), LibraryScanState(isAuthoritative = true))
        val playback = SuspendingPlayback(activeQueue = false, suspendFirst = false).apply { rejectRestore = true }
        val store = SessionStore(session(listOf(1L), 0))
        val coordinator = coordinator(backgroundScope, library, playback, store)

        coordinator.start()
        runCurrent()

        assertEquals(null, playback.restoredSongs)
        assertEquals(listOf(42L), store.saved?.queueSongIds)
        coordinator.release()
        runCurrent()
    }

    private fun coordinator(
        scope: CoroutineScope,
        library: FakeLibraryReader,
        playback: SuspendingPlayback,
        store: SessionStore,
        defaultDispatcher: CoroutineDispatcher = mainDispatcherRule.dispatcher,
    ) = PlaybackIntegrationCoordinator(
        scope = scope,
        preferences = IntegrationSettings(),
        history = FakeSearchSettingsStore(),
        library = library,
        playback = playback,
        effects = object : PlaybackEffects {
            override fun applyUpscalerSettings(settings: UpscalerSettings) = Unit
            override fun applyEffectSettings(settings: EqSettings) = Unit
            override fun hasSignalAlteringEffects() = false
        },
        sessionStore = store,
        clock = object : AppClock {
            override fun wallTimeMs() = 1L
            override fun elapsedTimeMs() = 1L
        },
        ioDispatcher = mainDispatcherRule.dispatcher,
        defaultDispatcher = defaultDispatcher,
    )

    private fun session(ids: List<Long>, currentIndex: Int) = PersistedPlaybackSession(
        ids, ids[currentIndex], currentIndex, 500L, PlaybackRepeatMode.Off, false, null, false, 1L,
    )
}

private class PausedDispatcher : CoroutineDispatcher() {
    val pending = ArrayDeque<Runnable>()
    override fun dispatch(context: CoroutineContext, block: Runnable) { pending.addLast(block) }
}

private class SessionStore(
    private val session: PersistedPlaybackSession? = null,
    private val onClear: () -> Unit = {},
) : PlaybackSessionPersistence {
    var clearCount = 0
    var saved: PersistedPlaybackSession? = null
    override fun load() = session
    override fun save(session: PersistedPlaybackSession) { saved = session }
    override fun clear() { clearCount++; onClear() }
}

private class IntegrationSettings : PlaybackIntegrationSettings {
    override val upscalerSettings = MutableStateFlow(UpscalerSettings())
    override val eqSettings = MutableStateFlow(EqSettings())
    override val audiobookPlaybackSpeed = MutableStateFlow(1f)
    override val crossfadeEnabled = MutableStateFlow(false)
    override val crossfadeDurationMs = MutableStateFlow(0L)
    override val crossfadeSilenceThresholdDb = MutableStateFlow(-60f)
    override val volumeNormalizationEnabled = MutableStateFlow(false)
}

private class SuspendingPlayback(
    var activeQueue: Boolean = true,
    private val suspendFirst: Boolean = true,
) : PlaybackIntegrationPort, PlaybackReader by FakePlaybackReader() {
    override val progressState = MutableStateFlow(PlaybackProgressState())
    val started = mutableListOf<List<Song>>()
    val completed = mutableListOf<List<Song>>()
    var firstCancelled = false
    var restoredSongs: List<Song>? = null
    var restoredIndex = -1
    var rejectRestore = false

    override fun hasActiveQueue() = activeQueue
    override fun reevaluateAudioOutputPath() = Unit
    override fun setCrossfadeEnabled(enabled: Boolean) = Unit
    override fun setCrossfadeDurationMs(value: Long) = Unit
    override fun setCrossfadeSilenceThresholdDb(value: Float) = Unit
    override fun setVolumeNormalizationEnabled(enabled: Boolean) = Unit
    override suspend fun refreshQueuedLibraryMetadataIfNeeded(updatedSongs: List<Song>, authoritative: Boolean) {
        started += updatedSongs
        if (suspendFirst && started.size == 1) {
            try { awaitCancellation() } finally { firstCancelled = true }
        }
        completed += updatedSongs
    }
    override fun hydrateRecentPlayback(songIds: List<Long>, albumIds: List<Long>, lastPlayedCollectionKind: PlaybackCollectionKind?, lastPlayedCollectionId: Long?) = Unit
    override fun currentPositionForPersistence() = 0L
    override fun capturePersistenceSnapshot() = PlaybackPersistenceSnapshot(
        if (activeQueue) listOf(42L) else emptyList(), null, 0, 0L, PlaybackRepeatMode.Off, false, null, false,
    )
    override fun checkpointAudiobookProgress(force: Boolean) = Unit
    override suspend fun restoreSessionFromLibrary(songs: List<Song>, currentIndex: Int, persisted: PersistedPlaybackSession): Boolean {
        if (rejectRestore) {
            activeQueue = true
            return false
        }
        restoredSongs = songs
        restoredIndex = currentIndex
        activeQueue = true
        return true
    }
}
