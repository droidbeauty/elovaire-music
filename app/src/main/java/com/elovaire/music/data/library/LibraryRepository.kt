package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
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
    val errorMessage: String? = null,
)

class LibraryRepository(
    appContext: Context,
    private val scanner: MediaStoreScanner,
    private val scope: CoroutineScope,
) {
    private val snapshotStore = LibrarySnapshotStore(appContext)
    private val contentResolver = appContext.contentResolver
    private val _contentState = MutableStateFlow(LibraryContentState())
    private val _scanState = MutableStateFlow(LibraryScanState())
    private var scanJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private var pendingRefresh = false
    private var pendingIndexRefresh = false
    private val pendingTargetedIndexRefreshPaths = linkedSetOf<String>()
    private var pendingMetadataEnrichment = false
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
            errorMessage = scan.errorMessage,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = LibraryUiState(),
    )
    private var musicDirectoryObserver: RecursiveMusicDirectoryObserver? = null
    private var mediaObserverRegistered = false

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
            scanJob?.cancel()
            scanJob = null
            pendingRefresh = false
            pendingIndexRefresh = false
            pendingTargetedIndexRefreshPaths.clear()
            pendingMetadataEnrichment = false
            refreshDebounceJob?.cancel()
            didBootstrapLibrary = false
            musicDirectoryObserver?.stopWatching()
            musicDirectoryObserver = null
            unregisterMediaObserver()
        }
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
            if (cachedSnapshot != null) {
                scanner.primeMetadataCache(cachedSnapshot.snapshot.songs)
                val cachedSnapshotNeedsMetadata = cachedSnapshot.snapshot.songs.any { song ->
                    !song.metadataResolved ||
                        song.releaseYear == null ||
                        song.qualityNeedsEnrichment() ||
                        song.genre.isBlank() ||
                        song.genre == "Unknown Genre"
                }
                _contentState.value = LibraryContentState(
                    songs = cachedSnapshot.snapshot.songs,
                    albums = cachedSnapshot.snapshot.albums,
                )
                _scanState.value = LibraryScanState(
                    permissionGranted = true,
                    isLoading = false,
                    scanProgress = 1f,
                )
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
                            _scanState.update { state ->
                                state.copy(
                                    permissionGranted = true,
                                    isLoading = true,
                                    scanProgress = progress,
                                    errorMessage = null,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
                .onSuccess { snapshot ->
                    withContext(Dispatchers.IO) {
                        snapshotStore.save(snapshot)
                    }
                    _contentState.value = LibraryContentState(
                        songs = snapshot.songs,
                        albums = snapshot.albums,
                    )
                    _scanState.value = LibraryScanState(
                        permissionGranted = true,
                        isLoading = false,
                        scanProgress = 1f,
                    )
                    if (!shouldEnrichMetadata && snapshot.songs.isNotEmpty()) {
                        pendingRefresh = true
                        pendingMetadataEnrichment = true
                    }
                }
                .onFailure { throwable ->
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

    fun albumById(albumId: Long): Album? = _contentState.value.albums.firstOrNull { it.id == albumId }

    fun defaultMediaFolderPath(): String = scanner.musicDirectory().absolutePath

    fun setPreferredLibraryFolderPath(path: String?) {
        scanner.setPreferredLibraryFolderPath(path)
        if (_scanState.value.permissionGranted) {
            ensureMusicDirectoryObserver()
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
        pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
        if (!forceMediaIndex) {
            changedFilePath
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let(pendingTargetedIndexRefreshPaths::add)
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

    private fun ensureMusicDirectoryObserver() {
        val musicDirectory = scanner.musicDirectory()
        if (musicDirectoryObserver?.rootPath == musicDirectory.absolutePath) return
        musicDirectoryObserver?.stopWatching()
        musicDirectoryObserver = createMusicDirectoryObserver()?.also { it.startWatching() }
    }

    private fun createMusicDirectoryObserver(): RecursiveMusicDirectoryObserver? {
        val musicDirectory = scanner.musicDirectory()
        if (!musicDirectory.exists() || !musicDirectory.isDirectory) return null

        return RecursiveMusicDirectoryObserver(musicDirectory) { event, changedFile ->
            if (event and DIRECTORY_STRUCTURE_CHANGE_MASK != 0) {
                ensureMusicDirectoryObserver()
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

        fun startWatching() {
            rebuildObservers()
        }

        fun stopWatching() {
            observers.values.forEach(FileObserver::stopWatching)
            observers.clear()
        }

        private fun rebuildObservers() {
            stopWatching()
            if (!rootDirectory.exists() || !rootDirectory.isDirectory) return

            observeDirectory(rootDirectory)
            rootDirectory.walkTopDown()
                .maxDepth(8)
                .filter { it.isDirectory && it.absolutePath != rootDirectory.absolutePath }
                .forEach(::observeDirectory)
        }

        private fun observeDirectory(directory: File) {
            val observer = createObserver(directory)
            observer.startWatching()
            observers[directory.absolutePath] = observer
        }

        private fun createObserver(directory: File): FileObserver {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                object : FileObserver(directory, OBSERVER_MASK) {
                    override fun onEvent(
                        event: Int,
                        path: String?,
                    ) {
                        dispatchDirectoryEvent(directory, event, path)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                object : FileObserver(directory.absolutePath, OBSERVER_MASK) {
                    override fun onEvent(
                        event: Int,
                        path: String?,
                    ) {
                        dispatchDirectoryEvent(directory, event, path)
                    }
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

    private companion object {
        const val AUTO_REFRESH_DEBOUNCE_MS = 900L
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
}
