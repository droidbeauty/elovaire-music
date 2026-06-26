package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

class LibraryRepository(
    appContext: Context,
    private val scanner: MediaStoreScanner,
    private val scope: CoroutineScope,
    private val appForegroundState: StateFlow<Boolean>,
) {
    private val snapshotStore = LibrarySnapshotStore(appContext)
    private val _contentState = MutableStateFlow(LibraryContentState())
    private val snapshotPublisher = LibrarySnapshotPublisher(
        publish = { _contentState.value = it },
        currentState = { _contentState.value },
    )
    private val _scanState = MutableStateFlow(LibraryScanState())
    private var scanJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private val refreshRequests = LibraryRefreshRequests()
    private var backgroundLibraryDirty = false
    private val deletionMarkers = LibraryDeletionMarkers()
    private var didBootstrapLibrary = false
    val contentState: StateFlow<LibraryContentState> = _contentState.asStateFlow()
    val scanState: StateFlow<LibraryScanState> = _scanState.asStateFlow()
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
        scope.launch {
            appForegroundState.collect { isForeground ->
                if (isForeground && backgroundLibraryDirty && _scanState.value.permissionGranted) {
                    backgroundLibraryDirty = false
                    refresh(
                        forceMediaIndex = false,
                        enrichMetadata = false,
                        showLoadingIndicator = false,
                    )
                }
            }
        }
    }

    fun onPermissionChanged(granted: Boolean) {
        _scanState.update { current ->
            current.copy(permissionGranted = granted, errorMessage = if (granted) current.errorMessage else null)
        }
        if (granted) {
            observerController.ensureRegistered()
            bootstrapLibrary()
        } else {
            didBootstrapLibrary = false
            releaseObserversAndJobs(clearPermissionState = false)
        }
    }

    fun release() {
        didBootstrapLibrary = false
        releaseObserversAndJobs(clearPermissionState = true)
    }

    private fun bootstrapLibrary() {
        if (didBootstrapLibrary) return
        didBootstrapLibrary = true
        scope.launch {
                val cachedSnapshot = withContext(Dispatchers.IO) { snapshotStore.load() }
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
                val currentSignature = withContext(Dispatchers.IO) { scanner.currentSignature() }
                if (currentSignature != cachedSnapshot.signature) {
                    refresh(
                        forceMediaIndex = false,
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
                    forceMediaIndex = false,
                    enrichMetadata = false,
                    showLoadingIndicator = true,
                )
            }
        }
    }

    fun refresh(
        forceMediaIndex: Boolean = false,
        enrichMetadata: Boolean = false,
        showLoadingIndicator: Boolean = _contentState.value.songs.isEmpty(),
    ) {
        if (!_scanState.value.permissionGranted) return
        val request = LibraryRefreshRequest(
            forceMediaIndex = forceMediaIndex,
            enrichMetadata = enrichMetadata,
        )
        if (scanJob?.isActive == true) {
            refreshRequests.enqueue(request)
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
            val refreshRequest = refreshRequests.takeForImmediateScan(request)
            val progressThrottler = LibraryScanProgressThrottler()
            runCatching {
                withContext(Dispatchers.IO) {
                    scanner.scan(
                        refreshMediaIndex = refreshRequest.forceMediaIndex,
                        refreshMediaPaths = refreshRequest.targetedPaths,
                        enrichMetadata = refreshRequest.enrichMetadata,
                        onProgress = if (showLoadingIndicator) { current, total ->
                            val progress = if (total <= 0) 1f else (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            if (progressThrottler.shouldEmit(progress)) {
                                _scanState.update { state ->
                                    state.copy(
                                        permissionGranted = true,
                                        isLoading = true,
                                        scanProgress = progress,
                                        errorMessage = null,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                }
            }
                .onSuccess { snapshot ->
                    val suppressedSongIds = deletionMarkers.suppressingSongIds()
                    val visibleSongs = snapshot.songs.filterNot { it.id in suppressedSongIds }
                    val scannedSongIds = snapshot.songs.mapTo(hashSetOf(), Song::id)
                    deletionMarkers.retainConfirmedSongsStillIn(scannedSongIds)
                    val nextContentState = publishLibraryContent(visibleSongs)
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
                        )
                    }
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
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _scanState.update {
                        it.copy(
                            isLoading = false,
                            scanProgress = 0f,
                            errorMessage = throwable.message ?: "Unable to scan local music.",
                        )
                    }
            }

            scanJob = null
            val pendingRequest = refreshRequests.takePendingAfterScan()
            if (pendingRequest != null && _scanState.value.permissionGranted) {
                startRefresh(pendingRequest, showLoadingIndicator = false)
            }
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
            snapshotStore.save(
                snapshot = snapshotPublisher.snapshotOf(updatedState),
                filterFingerprint = scanner.currentFilterFingerprint(),
            )
        }

        delay(DELETE_CONFIRMATION_DELAY_MS)
        val stillPresent = withContext(Dispatchers.IO) {
            scanner.findExistingSongIds(request.songIds)
        }
        val deletedSongIds = request.songIds - stillPresent
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
            deletedAlbumIds = fullyDeletedAlbumIds.filterTo(linkedSetOf()) { albumId ->
                updatedState.albums.none { it.id == albumId }
            },
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
            snapshotStore.save(
                snapshot = snapshotPublisher.snapshotOf(updatedState),
                filterFingerprint = scanner.currentFilterFingerprint(),
            )
        }
    }

    fun albumById(albumId: Long): Album? = _contentState.value.albums.firstOrNull { it.id == albumId }

    fun defaultMediaFolderPath(): String = scanner.musicDirectory().absolutePath

    fun setPreferredLibraryFolderPath(path: String?) {
        val changed = scanner.setPreferredLibraryFolderPath(path)
        if (!changed) return
        if (_scanState.value.permissionGranted) {
            observerController.ensureMusicDirectoryObserver(forceRebuild = true)
            refresh(
                forceMediaIndex = true,
                enrichMetadata = false,
                showLoadingIndicator = _contentState.value.songs.isEmpty(),
            )
        }
    }

    private fun scheduleMediaRefresh(
        forceMediaIndex: Boolean = false,
        changedFilePath: String? = null,
    ) {
        if (!_scanState.value.permissionGranted) return
        refreshRequests.enqueue(
            forceMediaIndex = forceMediaIndex,
            targetedPaths = listOfNotNull(changedFilePath),
        )
        if (!appForegroundState.value) {
            backgroundLibraryDirty = true
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
        scanJob?.cancel()
        scanJob = null
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        refreshRequests.clear()
        backgroundLibraryDirty = false
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
