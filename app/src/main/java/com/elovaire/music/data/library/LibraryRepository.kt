package elovaire.music.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import elovaire.music.app.domain.model.Album
import elovaire.music.app.domain.model.Song
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val _state = MutableStateFlow(LibraryUiState())
    private var scanJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private var pendingRefresh = false
    private var pendingIndexRefresh = false
    private var pendingMetadataEnrichment = false
    private var didBootstrapLibrary = false
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()
    private var musicDirectoryObserver: RecursiveMusicDirectoryObserver? = null

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

    init {
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver,
        )
        ensureMusicDirectoryObserver()
    }

    fun onPermissionChanged(granted: Boolean) {
        _state.update { current ->
            current.copy(permissionGranted = granted, errorMessage = if (granted) current.errorMessage else null)
        }
        if (granted) {
            ensureMusicDirectoryObserver()
            bootstrapLibrary()
        } else {
            pendingRefresh = false
            pendingIndexRefresh = false
            refreshDebounceJob?.cancel()
            didBootstrapLibrary = false
            musicDirectoryObserver?.stopWatching()
            musicDirectoryObserver = null
        }
    }

    private fun bootstrapLibrary() {
        if (didBootstrapLibrary) return
        didBootstrapLibrary = true
        scope.launch {
            val cachedSnapshot = withContext(Dispatchers.IO) { snapshotStore.load() }
            if (cachedSnapshot != null) {
                scanner.primeMetadataCache(cachedSnapshot.snapshot.songs)
                val cachedSnapshotNeedsMetadata = cachedSnapshot.snapshot.songs.any { song ->
                    song.releaseYear == null ||
                        song.qualityNeedsEnrichment() ||
                        song.genre.isBlank() ||
                        song.genre == "Unknown Genre"
                }
                _state.value = LibraryUiState(
                    permissionGranted = true,
                    isLoading = false,
                    scanProgress = 1f,
                    songs = cachedSnapshot.snapshot.songs,
                    albums = cachedSnapshot.snapshot.albums,
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
        showLoadingIndicator: Boolean = _state.value.songs.isEmpty(),
    ) {
        if (!_state.value.permissionGranted) return
        if (scanJob?.isActive == true) {
            pendingRefresh = true
            pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
            pendingMetadataEnrichment = pendingMetadataEnrichment || enrichMetadata
            return
        }

        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        if (showLoadingIndicator) {
            _state.update { it.copy(isLoading = true, scanProgress = 0f, errorMessage = null) }
        } else {
            _state.update { it.copy(errorMessage = null) }
        }
        scanJob = scope.launch {
            val shouldRefreshIndex = forceMediaIndex || pendingIndexRefresh
            val shouldEnrichMetadata = enrichMetadata || pendingMetadataEnrichment
            pendingIndexRefresh = false
            pendingMetadataEnrichment = false
            runCatching {
                withContext(Dispatchers.IO) {
                    scanner.scan(
                        refreshMediaIndex = shouldRefreshIndex,
                        enrichMetadata = shouldEnrichMetadata,
                        onProgress = if (showLoadingIndicator) { current, total ->
                            val progress = if (total <= 0) 1f else (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            _state.update { state ->
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
                    _state.value = LibraryUiState(
                        permissionGranted = true,
                        isLoading = false,
                        scanProgress = 1f,
                        songs = snapshot.songs,
                        albums = snapshot.albums,
                    )
                    if (!shouldEnrichMetadata && snapshot.songs.isNotEmpty()) {
                        pendingRefresh = true
                        pendingMetadataEnrichment = true
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scanProgress = 0f,
                            errorMessage = throwable.message ?: "Unable to scan local music.",
                        )
                    }
                }

            scanJob = null
            if (pendingRefresh && _state.value.permissionGranted) {
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

    fun albumById(albumId: Long): Album? = _state.value.albums.firstOrNull { it.id == albumId }

    fun defaultMediaFolderPath(): String = scanner.musicDirectory().absolutePath

    fun setPreferredLibraryFolderPath(path: String?) {
        scanner.setPreferredLibraryFolderPath(path)
        if (_state.value.permissionGranted) {
            refresh(
                forceMediaIndex = true,
                enrichMetadata = false,
                showLoadingIndicator = _state.value.songs.isEmpty(),
            )
        }
    }

    private fun scheduleMediaRefresh(
        forceMediaIndex: Boolean = false,
    ) {
        if (!_state.value.permissionGranted) return
        pendingIndexRefresh = pendingIndexRefresh || forceMediaIndex
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
            if (changedFile == null || changedFile.isDirectory || changedFile.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS) {
                scheduleMediaRefresh(forceMediaIndex = true)
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
        val SUPPORTED_AUDIO_EXTENSIONS = setOf(
            "mp3",
            "m4a",
            "aac",
            "flac",
            "wav",
            "alac",
            "ogg",
            "opus",
            "wma",
            "ape",
            "dsf",
            "dff",
            "amr",
            "3gp",
            "mp4",
            "mka",
        )
    }
}
