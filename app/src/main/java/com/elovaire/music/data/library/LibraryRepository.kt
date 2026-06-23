package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
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
    private val contentResolver = appContext.contentResolver
    private val _contentState = MutableStateFlow(LibraryContentState())
    private val snapshotPublisher = LibrarySnapshotPublisher(
        publish = { _contentState.value = it },
        currentState = { _contentState.value },
    )
    private val _scanState = MutableStateFlow(LibraryScanState())
    private var scanJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private var observerRebuildJob: Job? = null
    private var pendingRefresh = false
    private var pendingIndexRefresh = false
    private val pendingTargetedIndexRefreshPaths = linkedSetOf<String>()
    private var pendingMetadataEnrichment = false
    private var suppressObserverRefreshUntilMs = 0L
    private var backgroundLibraryDirty = false
    private val recentObservedPaths = linkedMapOf<String, Long>()
    private val pendingDeletedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    private val pendingDeletedAlbumIds = MutableStateFlow<Set<Long>>(emptySet())
    private val confirmedDeletedSongIds = MutableStateFlow<Set<Long>>(emptySet())
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
    private var musicDirectoryObserver: RecursiveMusicDirectoryObserver? = null
    private var mediaObserverRegistered = false

    init {
        scope.launch {
            appForegroundState.collect { isForeground ->
                if (isForeground && backgroundLibraryDirty && _scanState.value.permissionGranted) {
                    backgroundLibraryDirty = false
                    refresh(
                        forceMediaIndex = pendingIndexRefresh,
                        enrichMetadata = false,
                        showLoadingIndicator = false,
                    )
                }
            }
        }
    }

    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            scheduleMediaRefresh()
        }

        override fun onChange(
            selfChange: Boolean,
            uri: android.net.Uri?,
        ) {
            scheduleMediaRefresh()
        }
    }

    fun onPermissionChanged(granted: Boolean) {
        _scanState.update { current ->
            current.copy(permissionGranted = granted, errorMessage = if (granted) current.errorMessage else null)
        }
        if (granted) {
            ensureMediaObserverRegistered()
            ensureMusicDirectoryObserver()
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

    private fun ensureMediaObserverRegistered() {
        if (mediaObserverRegistered) return
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver,
        )
        mediaObserverRegistered = true
    }

    private fun unregisterMediaObserver() {
        if (!mediaObserverRegistered) return
        runCatching {
            contentResolver.unregisterContentObserver(mediaObserver)
        }
        mediaObserverRegistered = false
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
                    removingSongIds = pendingDeletedSongIds.value,
                    removingAlbumIds = pendingDeletedAlbumIds.value,
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
        if (scanJob?.isActive == true) {
            pendingRefresh = true
            pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
            pendingMetadataEnrichment = pendingMetadataEnrichment || enrichMetadata
            return
        }

        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        if (showLoadingIndicator) {
            _scanState.update { it.copy(isLoading = true, scanProgress = 0f, errorMessage = null) }
        } else {
            _scanState.update { it.copy(errorMessage = null) }
        }
        scanJob = scope.launch {
            val shouldRefreshIndex = forceMediaIndex || pendingIndexRefresh
            val targetedRefreshPaths = if (shouldRefreshIndex) {
                emptyList()
            } else {
                pendingTargetedIndexRefreshPaths.toList()
            }
            val shouldEnrichMetadata = enrichMetadata || pendingMetadataEnrichment
            val progressThrottler = LibraryScanProgressThrottler()
            pendingIndexRefresh = false
            pendingTargetedIndexRefreshPaths.clear()
            pendingMetadataEnrichment = false
            runCatching {
                withContext(Dispatchers.IO) {
                    scanner.scan(
                        refreshMediaIndex = shouldRefreshIndex,
                        refreshMediaPaths = targetedRefreshPaths,
                        enrichMetadata = shouldEnrichMetadata,
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
                    val suppressedSongIds = pendingDeletedSongIds.value + confirmedDeletedSongIds.value
                    val visibleSongs = snapshot.songs.filterNot { it.id in suppressedSongIds }
                    val scannedSongIds = snapshot.songs.mapTo(hashSetOf(), Song::id)
                    confirmedDeletedSongIds.update { tombstones -> tombstones.filterTo(linkedSetOf()) { it in scannedSongIds } }
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
                    if (!shouldEnrichMetadata && snapshotNeedsMetadata) {
                        pendingRefresh = true
                        pendingMetadataEnrichment = true
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
            if (pendingRefresh && _scanState.value.permissionGranted) {
                val shouldRefreshIndexAgain = pendingIndexRefresh
                val shouldEnrichMetadataAgain = pendingMetadataEnrichment
                pendingRefresh = false
                pendingIndexRefresh = false
                pendingMetadataEnrichment = false
                refresh(
                    forceMediaIndex = shouldRefreshIndexAgain,
                    enrichMetadata = shouldEnrichMetadataAgain,
                    showLoadingIndicator = false,
                )
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
        pendingTargetedIndexRefreshPaths.addAll(normalizedPaths)
        pendingMetadataEnrichment = pendingMetadataEnrichment || enrichMetadata
        if (scanJob?.isActive == true) {
            pendingRefresh = true
            return
        }
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        refresh(
            forceMediaIndex = false,
            enrichMetadata = enrichMetadata,
            showLoadingIndicator = false,
        )
    }

    fun markDeletingSongs(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        pendingDeletedSongIds.update { it + songIds }
        publishPendingDeletionState()
    }

    fun markDeletingAlbums(albumIds: Collection<Long>) {
        if (albumIds.isEmpty()) return
        pendingDeletedAlbumIds.update { it + albumIds }
        publishPendingDeletionState()
    }

    fun clearPendingDeletedSongs(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        pendingDeletedSongIds.update { it - songIds.toSet() }
        publishPendingDeletionState()
    }

    fun clearPendingDeletedAlbums(albumIds: Collection<Long>) {
        if (albumIds.isEmpty()) return
        pendingDeletedAlbumIds.update { it - albumIds.toSet() }
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
        suppressObserverRefreshUntilMs = System.currentTimeMillis() + DELETE_OBSERVER_SUPPRESSION_MS
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        pendingIndexRefresh = false
        pendingTargetedIndexRefreshPaths.clear()
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
        confirmedDeletedSongIds.update { it + deletedSongIds }
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
                removingSongIds = pendingDeletedSongIds.value,
                removingAlbumIds = pendingDeletedAlbumIds.value,
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
            ensureMusicDirectoryObserver(forceRebuild = true)
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
        if (System.currentTimeMillis() < suppressObserverRefreshUntilMs) return
        val normalizedChangedPath = changedFilePath?.normalizedObservedPath()
        if (!forceMediaIndex && normalizedChangedPath != null && shouldCoalesceObservedPath(normalizedChangedPath)) {
            backgroundLibraryDirty = backgroundLibraryDirty || !appForegroundState.value
            return
        }
        pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
        if (!forceMediaIndex) {
            normalizedChangedPath?.let(pendingTargetedIndexRefreshPaths::add)
        }
        if (!appForegroundState.value) {
            backgroundLibraryDirty = true
            return
        }
        refreshDebounceJob?.cancel()
        refreshDebounceJob = scope.launch {
            delay(AUTO_REFRESH_DEBOUNCE_MS)
            refreshDebounceJob = null
            refresh(
                forceMediaIndex = pendingIndexRefresh,
                enrichMetadata = false,
                showLoadingIndicator = false,
            )
        }
    }

    private fun ensureMusicDirectoryObserver(forceRebuild: Boolean = false) {
        val musicDirectory = scanner.musicDirectory()
        if (!forceRebuild && musicDirectoryObserver?.rootPath == musicDirectory.absolutePath) return
        musicDirectoryObserver?.stopWatching()
        musicDirectoryObserver = createMusicDirectoryObserver()?.also { it.startWatching() }
    }

    private fun requestMusicDirectoryObserverRebuild() {
        observerRebuildJob?.cancel()
        observerRebuildJob = scope.launch {
            delay(AUTO_REFRESH_DEBOUNCE_MS)
            observerRebuildJob = null
            val observer = musicDirectoryObserver
            if (observer != null) {
                observer.rebuildWatchingTree()
            } else {
                ensureMusicDirectoryObserver(forceRebuild = true)
            }
        }
    }

    private fun shouldCoalesceObservedPath(path: String): Boolean {
        val nowMs = SystemClock.elapsedRealtime()
        recentObservedPaths.entries.removeIf { (_, observedAtMs) ->
            nowMs - observedAtMs > OBSERVED_PATH_COALESCE_WINDOW_MS
        }
        val lastObservedAtMs = recentObservedPaths[path]
        recentObservedPaths[path] = nowMs
        return lastObservedAtMs != null && nowMs - lastObservedAtMs < OBSERVED_PATH_COALESCE_WINDOW_MS
    }

    private fun createMusicDirectoryObserver(): RecursiveMusicDirectoryObserver? {
        val musicDirectory = scanner.musicDirectory()
        if (!musicDirectory.exists() || !musicDirectory.isDirectory) return null

        return RecursiveMusicDirectoryObserver(musicDirectory) { event, changedFile ->
            if (event and DIRECTORY_STRUCTURE_CHANGE_MASK != 0) {
                requestMusicDirectoryObserverRebuild()
            }
            val requiresFullMediaIndexRefresh = event and FULL_INDEX_REFRESH_EVENT_MASK != 0
            if (changedFile == null || changedFile.isDirectory || isSupportedAudioExtension(changedFile.extension)) {
                scheduleMediaRefresh(
                    forceMediaIndex = requiresFullMediaIndexRefresh,
                    changedFilePath = if (requiresFullMediaIndexRefresh) null else changedFile?.absolutePath,
                )
            }
        }
    }

    private inner class RecursiveMusicDirectoryObserver(
        private val rootDirectory: File,
        private val onEventReceived: (event: Int, changedFile: File?) -> Unit,
    ) {
        val rootPath: String = rootDirectory.absolutePath
        private val observers = linkedMapOf<String, FileObserver>()
        private var lastTreeSignature: Int? = null

        fun startWatching() {
            rebuildObservers(force = true)
        }

        fun rebuildWatchingTree() {
            rebuildObservers(force = false)
        }

        fun stopWatching() {
            observers.values.forEach(FileObserver::stopWatching)
            observers.clear()
        }

        private fun rebuildObservers(force: Boolean) {
            if (!rootDirectory.exists() || !rootDirectory.isDirectory) {
                lastTreeSignature = null
                stopWatching()
                return
            }
            val nextDirectories = rootDirectory.walkTopDown()
                .maxDepth(8)
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .toList()
            val nextSignature = nextDirectories
                .sorted()
                .fold(17) { acc, path -> 31 * acc + path.hashCode() }
            if (!force && lastTreeSignature == nextSignature) return
            lastTreeSignature = nextSignature
            stopWatching()
            nextDirectories.forEach { path ->
                observeDirectory(File(path))
            }
        }

        private fun observeDirectory(directory: File) {
            val observer = createObserver(directory)
            observer.startWatching()
            observers[directory.absolutePath] = observer
        }

        private fun createObserver(directory: File): FileObserver {
            return object : FileObserver(directory, OBSERVER_MASK) {
                override fun onEvent(
                    event: Int,
                    path: String?,
                ) {
                    dispatchDirectoryEvent(directory, event, path)
                }
            }
        }

        private fun dispatchDirectoryEvent(
            directory: File,
            event: Int,
            path: String?,
        ) {
            if (event == 0) return
            onEventReceived(event, path?.let { File(directory, it) })
        }
    }

    private fun publishLibraryContent(
        songs: List<Song>,
        removingSongIds: Set<Long> = pendingDeletedSongIds.value,
        removingAlbumIds: Set<Long> = pendingDeletedAlbumIds.value,
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
        const val OBSERVED_PATH_COALESCE_WINDOW_MS = 900L
        const val OBSERVER_MASK =
            FileObserver.CREATE or
                FileObserver.CLOSE_WRITE or
                FileObserver.MOVED_TO or
                FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MODIFY or
                FileObserver.MOVE_SELF
        const val DIRECTORY_STRUCTURE_CHANGE_MASK =
            FileObserver.CREATE or
                FileObserver.MOVED_TO or
                FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF
        const val FULL_INDEX_REFRESH_EVENT_MASK =
            FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF
    }

    private fun releaseObserversAndJobs(clearPermissionState: Boolean) {
        scanJob?.cancel()
        scanJob = null
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        observerRebuildJob?.cancel()
        observerRebuildJob = null
        pendingRefresh = false
        pendingIndexRefresh = false
        pendingTargetedIndexRefreshPaths.clear()
        pendingMetadataEnrichment = false
        suppressObserverRefreshUntilMs = 0L
        backgroundLibraryDirty = false
        recentObservedPaths.clear()
        pendingDeletedSongIds.value = emptySet()
        pendingDeletedAlbumIds.value = emptySet()
        confirmedDeletedSongIds.value = emptySet()
        _contentState.update { current ->
            current.copy(removingSongIds = emptySet(), removingAlbumIds = emptySet())
        }
        musicDirectoryObserver?.stopWatching()
        musicDirectoryObserver = null
        unregisterMediaObserver()
        if (clearPermissionState) {
            _scanState.value = _scanState.value.copy(
                permissionGranted = false,
                isLoading = false,
                scanProgress = 0f,
            )
        }
    }

    private fun String.normalizedObservedPath(): String? {
        return trim()
            .takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.absolutePath
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
