package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.backend.BackendEvent
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.core.backend.LogcatBackendEventSink
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

data class LibraryContentState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val removingSongIds: Set<Long> = emptySet(),
    val removingAlbumIds: Set<Long> = emptySet(),
)

data class LibraryScanState(
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val scanProgress: Float = 0f,
    val errorMessage: String? = null,
)

data class LibraryUiState(
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val scanProgress: Float = 0f,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val removingSongIds: Set<Long> = emptySet(),
    val removingAlbumIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
)

data class LibraryDeleteRequest(
    val songIds: Set<Long>,
    val albumIds: Set<Long>,
    val uris: Set<Uri>,
    val filePaths: Set<String>,
)

data class LibraryDeleteResult(
    val deletedSongIds: Set<Long>,
    val deletedAlbumIds: Set<Long>,
    val failed: List<LibraryDeleteFailure>,
)

data class LibraryDeleteFailure(
    val songId: Long?,
    val albumId: Long?,
    val reason: String,
)

class LibraryRepository internal constructor(
    appContext: Context,
    private val scanner: MediaStoreScanner,
    private val scope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val indexStore: LibraryIndexStore? = null,
    private val backendEventSink: BackendEventSink = LogcatBackendEventSink,
) : LibraryReader {
    private val snapshotStore = LibrarySnapshotStore(appContext)
    private val _contentState = MutableStateFlow(LibraryContentState())
    private val snapshotPublisher = LibrarySnapshotPublisher(
        publish = { _contentState.value = it },
        currentState = { _contentState.value },
    )
    private val _scanState = MutableStateFlow(LibraryScanState())
    private var scanJob: Job? = null
    private var bootstrapJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private var foregroundObserverJob: Job? = null
    private val refreshRequests = LibraryRefreshRequests()
    private val _runtimeState = MutableStateFlow<LibraryRuntimeState>(LibraryRuntimeState.NoPermission)
    private val deletionMarkers = LibraryDeletionMarkers()
    private val released = AtomicBoolean(false)
    @Volatile
    private var permissionChangeVersion = 0L
    private var didBootstrapLibrary = false
    override val contentState: StateFlow<LibraryContentState> = _contentState.asStateFlow()
    override val scanState: StateFlow<LibraryScanState> = _scanState.asStateFlow()
    val state: StateFlow<LibraryUiState> = combine(contentState, scanState) { content, scan ->
        LibraryUiState(
            permissionGranted = scan.permissionGranted,
            isLoading = scan.isLoading,
            scanProgress = scan.scanProgress,
            songs = content.songs,
            albums = content.albums,
            removingSongIds = content.removingSongIds,
            removingAlbumIds = content.removingAlbumIds,
            errorMessage = scan.errorMessage,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LibraryUiState(),
    )
    private val observerController = LibraryObserverController(
        appContext = appContext,
        scanner = scanner,
        scope = scope,
        onObservedRefresh = ::scheduleMediaRefresh,
    )

    init {
        foregroundObserverJob = scope.launch {
            backgroundWorkPolicy.isForeground.collect { isForeground ->
                if (released.get()) return@collect
                updateObserverRegistration()
                val runtime = _runtimeState.value
                if (isForeground && runtime is LibraryRuntimeState.BackgroundDirty && _scanState.value.permissionGranted) {
                    startRefresh(runtime.pending, showLoadingIndicator = false)
                }
            }
        }
    }

    fun onPermissionChanged(granted: Boolean) {
        if (released.get()) return
        if (_scanState.value.permissionGranted == granted) return
        permissionChangeVersion += 1L
        _scanState.update { current ->
            current.copy(permissionGranted = granted, errorMessage = if (granted) current.errorMessage else null)
        }
        if (granted) {
            _runtimeState.value = LibraryRuntimeState.Idle
            updateObserverRegistration()
            bootstrapLibrary()
        } else {
            _runtimeState.value = LibraryRuntimeState.NoPermission
            didBootstrapLibrary = false
            releaseObserversAndJobs(clearPermissionState = false)
            _scanState.value = LibraryScanState(permissionGranted = false)
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        didBootstrapLibrary = false
        releaseObserversAndJobs(clearPermissionState = true)
        foregroundObserverJob?.cancel()
        foregroundObserverJob = null
        _runtimeState.value = LibraryRuntimeState.Released
    }

    private fun bootstrapLibrary() {
        if (didBootstrapLibrary) return
        didBootstrapLibrary = true
        val bootstrapPermissionVersion = permissionChangeVersion
        _runtimeState.value = LibraryRuntimeState.Bootstrapping(bootstrapPermissionVersion)
        bootstrapJob = scope.launch {
            try {
                val cachedSnapshot = withContext(Dispatchers.IO) {
                    ElovaireTrace.section("library_snapshot_load") { snapshotStore.load() }
                }
                if (!hasCurrentPermission(bootstrapPermissionVersion)) return@launch
                val cacheMatchesCurrentFilter = cachedSnapshot?.signature?.filterFingerprint == scanner.currentFilterFingerprint()
                if (cachedSnapshot != null && cacheMatchesCurrentFilter) {
                    scanner.primeMetadataCache(cachedSnapshot.snapshot.songs)
                    val cachedSnapshotNeedsMetadata = cachedSnapshot.snapshot.songs.any { song ->
                        !song.metadataResolved ||
                            song.releaseYear == null ||
                            song.qualityNeedsEnrichment() ||
                            song.genre.isBlank() ||
                            song.genre == "Unknown Genre"
                    }
                    val cachedContent = LibraryContentState(
                        songs = cachedSnapshot.snapshot.songs,
                        albums = cachedSnapshot.snapshot.albums,
                        removingSongIds = deletionMarkers.pendingSongIds.value,
                        removingAlbumIds = deletionMarkers.pendingAlbumIds.value,
                    )
                    if (!hasCurrentPermission(bootstrapPermissionVersion)) return@launch
                    if (_contentState.value != cachedContent) {
                        _contentState.value = cachedContent
                    }
                    val cachedScanState = LibraryScanState(
                        permissionGranted = true,
                        isLoading = false,
                        scanProgress = 1f,
                    )
                    if (_scanState.value != cachedScanState) {
                        _scanState.value = cachedScanState
                    }
                    val currentSyncState = withContext(Dispatchers.IO) { scanner.currentSyncState() }
                    if (!hasCurrentPermission(bootstrapPermissionVersion)) return@launch
                    val syncDecision = decideLibrarySync(
                        cached = cachedSnapshot.syncState,
                        current = currentSyncState,
                        cachedSongCount = cachedSnapshot.snapshot.songs.size,
                    )
                    if (syncDecision != LibrarySyncDecision.ReuseCached) {
                        refresh(
                            forceMediaIndex = cachedSnapshot.snapshot.songs.isEmpty(),
                            enrichMetadata = false,
                            showLoadingIndicator = false,
                        )
                    } else if (cachedSnapshotNeedsMetadata) {
                        refresh(
                            forceMediaIndex = false,
                            enrichMetadata = true,
                            showLoadingIndicator = false,
                        )
                    }
                } else {
                    refresh(
                        forceMediaIndex = true,
                        enrichMetadata = false,
                        showLoadingIndicator = true,
                    )
                }
            } finally {
                if (bootstrapJob === currentCoroutineContext()[Job]) {
                    bootstrapJob = null
                }
                if (hasCurrentPermission(bootstrapPermissionVersion) && _runtimeState.value is LibraryRuntimeState.Bootstrapping) {
                    _runtimeState.value = LibraryRuntimeState.Idle
                }
            }
        }
    }

    private fun hasCurrentPermission(permissionVersion: Long): Boolean {
        return permissionChangeVersion == permissionVersion && _scanState.value.permissionGranted
    }

    fun refresh(
        forceMediaIndex: Boolean = false,
        enrichMetadata: Boolean = false,
        showLoadingIndicator: Boolean = _contentState.value.songs.isEmpty(),
    ) {
        if (released.get() || !_scanState.value.permissionGranted) return
        val request = LibraryRefreshRequest(
            forceMediaIndex = forceMediaIndex,
            enrichMetadata = enrichMetadata,
        )
        if (scanJob?.isActive == true) {
            refreshRequests.enqueue(request)
            backendEventSink.emit(
                BackendEvent.LibraryRefreshCoalesced(
                    mapOf(
                        "force_index" to forceMediaIndex.toString(),
                        "enrich_metadata" to enrichMetadata.toString(),
                    ),
                ),
            )
            return
        }
        startRefresh(request, showLoadingIndicator)
    }

    private fun startRefresh(
        request: LibraryRefreshRequest,
        showLoadingIndicator: Boolean,
    ) {
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        if (showLoadingIndicator) {
            _scanState.update { it.copy(isLoading = true, scanProgress = 0f, errorMessage = null) }
        } else {
            _scanState.update { it.copy(errorMessage = null) }
        }
        scanJob = scope.launch {
            val currentScanJob = currentCoroutineContext()[Job]
            val scanPermissionVersion = permissionChangeVersion
            val refreshRequest = refreshRequests.takeForImmediateScan(request)
            _runtimeState.value = LibraryRuntimeState.Scanning(refreshRequest, scanPermissionVersion)
            backendEventSink.emit(
                BackendEvent.LibraryScanStarted(
                    mapOf(
                        "force_index" to refreshRequest.forceMediaIndex.toString(),
                        "enrich_metadata" to refreshRequest.enrichMetadata.toString(),
                        "targeted_paths" to refreshRequest.targetedPaths.size.toString(),
                    ),
                ),
            )
            val progressThrottler = LibraryScanProgressThrottler()
            try {
                runCatching {
                    scanLibrary(refreshRequest, showLoadingIndicator, scanPermissionVersion, progressThrottler)
                }.onSuccess { snapshot ->
                    if (!hasCurrentPermission(scanPermissionVersion)) return@onSuccess
                    val suppressedSongIds = deletionMarkers.suppressingSongIds()
                    val visibleSongs = snapshot.songs.filterNot { it.id in suppressedSongIds }
                    val scannedSongIds = snapshot.songs.mapTo(hashSetOf(), Song::id)
                    deletionMarkers.retainConfirmedSongsStillIn(scannedSongIds)
                    val nextContentState = ElovaireTrace.section("library_publish_content") {
                        publishLibraryContent(visibleSongs)
                    }
                    val visibleSnapshot = snapshotPublisher.snapshotOf(nextContentState)
                    val nextScanState = LibraryScanState(
                        permissionGranted = true,
                        isLoading = false,
                        scanProgress = 1f,
                    )
                    if (_scanState.value != nextScanState) {
                        _scanState.value = nextScanState
                    }
                    withContext(Dispatchers.IO) {
                        snapshotStore.save(
                            snapshot = visibleSnapshot,
                            filterFingerprint = scanner.currentFilterFingerprint(),
                            syncState = scanner.currentSyncState(),
                        )
                        indexStore?.indexSnapshot(
                            snapshot = visibleSnapshot,
                            filterFingerprint = scanner.currentFilterFingerprint(),
                            source = "MediaStore",
                        )
                    }
                    if (!hasCurrentPermission(scanPermissionVersion)) return@onSuccess
                    val snapshotNeedsMetadata = visibleSnapshot.songs.any { song ->
                        !song.metadataResolved ||
                            song.releaseYear == null ||
                            song.qualityNeedsEnrichment() ||
                            song.genre.isBlank() ||
                            song.genre == "Unknown Genre"
                    }
                    if (!refreshRequest.enrichMetadata && snapshotNeedsMetadata) {
                        refreshRequests.enqueue(enrichMetadata = true)
                    }
                    backendEventSink.emit(
                        BackendEvent.LibraryScanCompleted(
                            mapOf(
                                "songs" to visibleSnapshot.songs.size.toString(),
                                "albums" to visibleSnapshot.albums.size.toString(),
                            ),
                        ),
                    )
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    backendEventSink.emit(
                        BackendEvent.LibraryScanFailed(
                            mapOf("error_type" to (throwable::class.simpleName ?: "Unknown")),
                        ),
                    )
                    if (hasCurrentPermission(scanPermissionVersion)) {
                        val failure = throwable.toLibraryScanFailure("refresh")
                        _runtimeState.value = LibraryRuntimeState.Failed(failure, recoverable = true)
                        _scanState.update {
                            it.copy(
                                isLoading = false,
                                scanProgress = 0f,
                                errorMessage = failure.toUserMessage(),
                            )
                        }
                    }
                }
            } finally {
                if (scanJob === currentScanJob) {
                    scanJob = null
                }
            }

            if (scanJob != null || !hasCurrentPermission(scanPermissionVersion)) return@launch
            val pendingRequest = refreshRequests.takePendingAfterScan()
            if (pendingRequest != null && _scanState.value.permissionGranted) {
                startRefresh(pendingRequest, showLoadingIndicator = false)
            } else if (_runtimeState.value is LibraryRuntimeState.Scanning) {
                _runtimeState.value = LibraryRuntimeState.Idle
            }
        }
    }

    private suspend fun scanLibrary(
        request: LibraryRefreshRequest,
        showLoadingIndicator: Boolean,
        permissionVersion: Long,
        progressThrottler: LibraryScanProgressThrottler,
    ) = withContext(Dispatchers.IO) {
        ElovaireTrace.suspendSection("library_refresh_scan") {
            scanner.scan(
                refreshMediaIndex = request.forceMediaIndex,
                refreshMediaPaths = request.targetedPaths,
                enrichMetadata = request.enrichMetadata,
                onProgress = if (showLoadingIndicator) progress@{ current, total ->
                    if (!hasCurrentPermission(permissionVersion)) return@progress
                    val progress = if (total <= 0) {
                        1f
                    } else {
                        (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    }
                    if (progressThrottler.shouldEmit(progress)) {
                        ElovaireTrace.section("library_scan_progress") {
                            _scanState.update { state ->
                                state.copy(
                                    permissionGranted = true,
                                    isLoading = true,
                                    scanProgress = progress,
                                    errorMessage = null,
                                )
                            }
                        }
                    }
                } else {
                    null
                },
            )
        }
    }

    fun refreshChangedFiles(
        filePaths: List<String>,
        songIds: List<Long> = emptyList(),
        enrichMetadata: Boolean = true,
    ) {
        if (!_scanState.value.permissionGranted) return
        if (enrichMetadata && songIds.isNotEmpty()) {
            scanner.invalidateMetadataCacheForSongIds(songIds)
        }
        val normalizedPaths = filePaths
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
        if (normalizedPaths.isEmpty()) {
            if (enrichMetadata && songIds.isEmpty()) {
                scanner.clearMetadataCache()
            }
            refresh(
                forceMediaIndex = true,
                enrichMetadata = enrichMetadata,
                showLoadingIndicator = false,
            )
            return
        }
        if (enrichMetadata) {
            scanner.invalidateMetadataCacheForPaths(normalizedPaths)
        }
        val request = LibraryRefreshRequest(
            enrichMetadata = enrichMetadata,
            targetedPaths = normalizedPaths,
        )
        if (scanJob?.isActive == true) {
            refreshRequests.enqueue(request)
            return
        }
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        startRefresh(request, showLoadingIndicator = false)
    }

    fun markDeletingSongs(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        deletionMarkers.markSongs(songIds)
        publishPendingDeletionState()
    }

    fun markDeletingAlbums(albumIds: Collection<Long>) {
        if (albumIds.isEmpty()) return
        deletionMarkers.markAlbums(albumIds)
        publishPendingDeletionState()
    }

    fun clearPendingDeletedSongs(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        deletionMarkers.clearSongs(songIds)
        publishPendingDeletionState()
    }

    fun clearPendingDeletedAlbums(albumIds: Collection<Long>) {
        if (albumIds.isEmpty()) return
        deletionMarkers.clearAlbums(albumIds)
        publishPendingDeletionState()
    }

    suspend fun refreshAfterDelete(request: LibraryDeleteRequest): LibraryDeleteResult {
        if (request.songIds.isEmpty()) {
            return LibraryDeleteResult(emptySet(), emptySet(), emptyList())
        }
        val current = _contentState.value
        val fullyDeletedAlbumIds = request.albumIds.filterTo(linkedSetOf()) { albumId ->
            current.albums
                .firstOrNull { it.id == albumId }
                ?.songs
                ?.all { it.id in request.songIds } == true
        }
        markDeletingSongs(request.songIds)
        markDeletingAlbums(fullyDeletedAlbumIds)
        observerController.setSuppressRefreshUntil(System.currentTimeMillis() + DELETE_OBSERVER_SUPPRESSION_MS)
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        refreshRequests.clearIndexRefresh()
        scanner.invalidateMetadataCacheForSongIds(request.songIds)
        scanner.invalidateMetadataCacheForPaths(request.filePaths)

        delay(DELETE_EXIT_ANIMATION_MS)
        val remainingSongs = _contentState.value.songs.filterNot { it.id in request.songIds }
        val updatedState = publishLibraryContent(remainingSongs)
        withContext(Dispatchers.IO) {
            val updatedSnapshot = snapshotPublisher.snapshotOf(updatedState)
            snapshotStore.save(
                snapshot = updatedSnapshot,
                filterFingerprint = scanner.currentFilterFingerprint(),
                syncState = scanner.currentSyncState(),
            )
        }

        delay(DELETE_CONFIRMATION_DELAY_MS)
        val stillPresent = withContext(Dispatchers.IO) {
            scanner.findExistingSongIds(request.songIds)
        }
        val deletedSongIds = request.songIds - stillPresent
        val affectedAlbumIds = current.songs
            .asSequence()
            .filter { it.id in deletedSongIds }
            .map(Song::albumId)
            .toSet()
        val deletedAlbumIds = fullyDeletedAlbumIds.filterTo(linkedSetOf()) { albumId ->
            updatedState.albums.none { it.id == albumId }
        }
        withContext(Dispatchers.IO) {
            indexStore?.markRemoved(deletedSongIds, deletedAlbumIds)
            indexStore?.applyChangedSongs(
                songs = emptyList(),
                albums = updatedState.albums.filter { it.id in affectedAlbumIds },
            )
        }
        deletionMarkers.confirmDeletedSongs(deletedSongIds)
        clearPendingDeletedSongs(request.songIds)
        clearPendingDeletedAlbums(fullyDeletedAlbumIds)
        if (stillPresent.isNotEmpty()) {
            refresh(
                forceMediaIndex = false,
                enrichMetadata = false,
                showLoadingIndicator = false,
            )
            _scanState.update { state ->
                state.copy(errorMessage = "Some files could not be deleted.")
            }
        }
        return LibraryDeleteResult(
            deletedSongIds = deletedSongIds,
            deletedAlbumIds = deletedAlbumIds,
            failed = stillPresent.map { songId ->
                LibraryDeleteFailure(
                    songId = songId,
                    albumId = current.songs.firstOrNull { it.id == songId }?.albumId,
                    reason = "The file is still present after deletion.",
                )
            },
        )
    }

    private fun publishPendingDeletionState() {
        _contentState.update { current ->
            current.copy(
                removingSongIds = deletionMarkers.pendingSongIds.value,
                removingAlbumIds = deletionMarkers.pendingAlbumIds.value,
            )
        }
    }

    suspend fun applyVerifiedTagEdits(editedSongs: List<Song>) {
        if (editedSongs.isEmpty()) return
        val updatesById = editedSongs.associateBy(Song::id)
        val current = _contentState.value
        val updatedSongs = current.songs.map { song -> updatesById[song.id] ?: song }
        if (updatedSongs == current.songs) return
        val updatedState = publishLibraryContent(
            songs = updatedSongs,
            removingSongIds = current.removingSongIds,
            removingAlbumIds = current.removingAlbumIds,
        )
        withContext(Dispatchers.IO) {
            val updatedSnapshot = snapshotPublisher.snapshotOf(updatedState)
            snapshotStore.save(
                snapshot = updatedSnapshot,
                filterFingerprint = scanner.currentFilterFingerprint(),
                syncState = scanner.currentSyncState(),
            )
            val affectedAlbumIds = editedSongs.mapTo(hashSetOf(), Song::albumId)
            indexStore?.applyChangedSongs(
                songs = editedSongs,
                albums = updatedSnapshot.albums.filter { it.id in affectedAlbumIds },
            )
        }
    }

    fun albumById(albumId: Long): Album? = _contentState.value.albums.firstOrNull { it.id == albumId }

    fun defaultMediaFolderPath(): String = scanner.musicDirectory().absolutePath

    fun setLibraryFolders(
        selections: List<LibraryFolderSelection>,
        enrichMetadata: Boolean = false,
        showLoadingIndicator: Boolean = _contentState.value.songs.isEmpty(),
    ) {
        val changed = scanner.setLibraryFolders(selections)
        if (!changed) return
        if (_scanState.value.permissionGranted) {
            if (backgroundWorkPolicy.shouldKeepRecursiveLibraryObservers(permissionGranted = true)) {
                observerController.ensureLibraryFolderObservers(forceRebuild = true)
            }
            refresh(
                forceMediaIndex = true,
                enrichMetadata = enrichMetadata,
                showLoadingIndicator = showLoadingIndicator,
            )
        }
    }

    private fun scheduleMediaRefresh(
        forceMediaIndex: Boolean = false,
        changedFilePath: String? = null,
    ) {
        if (released.get() || !_scanState.value.permissionGranted) return
        refreshRequests.enqueue(
            forceMediaIndex = forceMediaIndex,
            targetedPaths = listOfNotNull(changedFilePath),
        )
        if (backgroundWorkPolicy.shouldDeferLibraryRefresh()) {
            val pending = refreshRequests.takePendingAfterScan() ?: return
            _runtimeState.value = LibraryRuntimeState.BackgroundDirty(pending)
            return
        }
        refreshDebounceJob?.cancel()
        refreshDebounceJob = scope.launch {
            delay(AUTO_REFRESH_DEBOUNCE_MS)
            refreshDebounceJob = null
            refresh(
                forceMediaIndex = false,
                enrichMetadata = false,
                showLoadingIndicator = false,
            )
        }
    }

    private fun updateObserverRegistration() {
        val permissionGranted = _scanState.value.permissionGranted
        if (!backgroundWorkPolicy.shouldKeepMediaStoreObserver(permissionGranted)) {
            observerController.release()
            return
        }
        observerController.ensureRegistered(
            enableDirectoryObservers = backgroundWorkPolicy.shouldKeepRecursiveLibraryObservers(permissionGranted),
        )
    }

    private fun publishLibraryContent(
        songs: List<Song>,
        removingSongIds: Set<Long> = deletionMarkers.pendingSongIds.value,
        removingAlbumIds: Set<Long> = deletionMarkers.pendingAlbumIds.value,
    ): LibraryContentState {
        return snapshotPublisher.publishSongs(
            songs = songs,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
    }

    private companion object {
        const val AUTO_REFRESH_DEBOUNCE_MS = 350L
        const val DELETE_EXIT_ANIMATION_MS = 190L
        const val DELETE_CONFIRMATION_DELAY_MS = 500L
        const val DELETE_OBSERVER_SUPPRESSION_MS = 1_200L
    }

    private fun releaseObserversAndJobs(clearPermissionState: Boolean) {
        bootstrapJob?.cancel()
        bootstrapJob = null
        scanJob?.cancel()
        scanJob = null
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        refreshRequests.clear()
        deletionMarkers.clear()
        _contentState.update { current ->
            current.copy(removingSongIds = emptySet(), removingAlbumIds = emptySet())
        }
        observerController.release()
        if (clearPermissionState) {
            _scanState.value = _scanState.value.copy(
                permissionGranted = false,
                isLoading = false,
                scanProgress = 0f,
            )
        }
    }

}

private class LibraryScanProgressThrottler(
    private val minStep: Float = 0.01f,
    private val minIntervalMs: Long = 80L,
) {
    private var lastProgress = -1f
    private var lastEmitMs = 0L

    fun shouldEmit(progress: Float): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (progress >= 1f) return true
        if (lastProgress < 0f) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        val enoughProgress = progress - lastProgress >= minStep
        val enoughTime = now - lastEmitMs >= minIntervalMs
        if (enoughProgress || enoughTime) {
            lastProgress = progress
            lastEmitMs = now
            return true
        }
        return false
    }
}
