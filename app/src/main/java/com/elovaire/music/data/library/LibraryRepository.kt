package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.net.Uri
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.core.backend.BackendEvent
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.core.backend.BackendOperationContext
import elovaire.music.droidbeauty.app.core.backend.BackendOperationMetrics
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import elovaire.music.droidbeauty.app.core.backend.BackendSubsystem
import elovaire.music.droidbeauty.app.core.backend.LogcatBackendEventSink
import elovaire.music.droidbeauty.app.core.backend.emitLazy
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.library.db.LibraryIndexStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.LibrarySnapshot
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

data class LibraryContentState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val audiobooks: List<Audiobook> = emptyList(),
    val removingSongIds: Set<Long> = emptySet(),
    val removingAlbumIds: Set<Long> = emptySet(),
    val contentRevision: String = "",
)

data class LibraryScanState(
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val scanProgress: Float = 0f,
    val errorMessage: String? = null,
    val isAuthoritative: Boolean = false,
)

data class LibraryUiState(
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val scanProgress: Float = 0f,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val audiobooks: List<Audiobook> = emptyList(),
    val removingSongIds: Set<Long> = emptySet(),
    val removingAlbumIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
)

data class LibraryDeleteRequest(
    val songIds: Set<Long>,
    val albumIds: Set<Long>,
    val uris: Set<Uri>,
    val filePaths: Set<String>,
    val uriBySongId: Map<Long, Uri> = emptyMap(),
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

@Suppress("LargeClass", "TooManyFunctions")
internal class LibraryRepository internal constructor(
    appContext: Context,
    private val scanner: LibraryScanCoordinator,
    private val scope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val backendEventSink: BackendEventSink = LogcatBackendEventSink,
    private val clock: AppClock = AndroidAppClock,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
    private val libraryIndexStore: LibraryIndexStore? = null,
    private val onSongRelocations: suspend (Map<Long, Long>) -> SongRelocationOutcome = {
        SongRelocationOutcome.Applied
    },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val resourceTracker: BackendResourceTracker = BackendResourceRegistry,
) : LibraryStartupController, LibraryNetworkController, LibraryTagUpdateWriter {
    private val snapshotStore = LibrarySnapshotStore(appContext)
    private val _contentState = MutableStateFlow(LibraryContentState())
    private val snapshotPublisher = LibrarySnapshotPublisher(
        publish = { _contentState.value = it },
        currentState = { _contentState.value },
    )
    private val _scanState = MutableStateFlow(LibraryScanState())
    private var scanJob: Job? = null
    private var refreshDebounceJob: Job? = null
    private var foregroundObserverJob: Job? = null
    private val refreshRequests = LibraryRefreshRequests()
    private val _runtimeState = MutableStateFlow<LibraryRuntimeState>(LibraryRuntimeState.NoPermission)
    private val deletionMarkers = LibraryDeletionMarkers()
    private val directPathTombstones = linkedSetOf<String>()
    private val directPathTombstonesLock = Any()
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    @Volatile
    private var permissionChangeVersion = 0L
    private var needsForegroundReconcile = false
    @Volatile
    private var lastSuccessfulMediaStoreSyncState: LibraryMediaStoreSyncState? = null
    override val contentState: StateFlow<LibraryContentState> = _contentState.asStateFlow()
    override val scanState: StateFlow<LibraryScanState> = _scanState.asStateFlow()
    private val observerController = LibraryObserverController(
        appContext = appContext,
        scanner = scanner.localScanner,
        scope = scope,
        onObservedRefresh = ::scheduleMediaRefresh,
        clock = clock,
        ioDispatcher = ioDispatcher,
    )

    override fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        foregroundObserverJob = scope.launch {
            var wasForeground = backgroundWorkPolicy.isForeground.value
            var wasInteractionCritical = backgroundWorkPolicy.interactionCritical.value
            combine(
                backgroundWorkPolicy.isForeground,
                backgroundWorkPolicy.interactionCritical,
            ) { isForeground, interactionCritical ->
                isForeground to interactionCritical
            }.collect { (isForeground, interactionCritical) ->
                if (released.get()) return@collect
                val enteredForeground = isForeground && !wasForeground
                val interactionEnded = wasInteractionCritical && !interactionCritical
                if (!isForeground) {
                    needsForegroundReconcile = true
                }
                wasForeground = isForeground
                wasInteractionCritical = interactionCritical
                updateObserverRegistration()
                val runtime = _runtimeState.value
                val canResumeDeferredRefresh =
                    isForeground &&
                        _scanState.value.permissionGranted &&
                        (!interactionCritical || runtime.pendingRefreshIsFreshnessCritical())
                if (
                    canResumeDeferredRefresh &&
                        runtime is LibraryRuntimeState.BackgroundDirty
                ) {
                    needsForegroundReconcile = false
                    startRefresh(runtime.pending, showLoadingIndicator = false)
                } else if (
                    canResumeDeferredRefresh &&
                    (interactionEnded || enteredForeground) &&
                    runtime is LibraryRuntimeState.InteractionDirty
                ) {
                    startRefresh(runtime.pending, showLoadingIndicator = false)
                } else if (
                    enteredForeground &&
                    !interactionCritical &&
                    needsForegroundReconcile &&
                    _scanState.value.permissionGranted
                ) {
                    if (scanJob?.isActive != true) {
                        val reconcileRequest = withContext(ioDispatcher) {
                            decideForegroundReconcile(
                                cached = lastSuccessfulMediaStoreSyncState,
                                current = scanner.currentSyncState(),
                                cachedSongCount = _contentState.value.songs.size,
                                hasSafSelections = scanner.hasSafSelections(),
                                staleNetworkSourceIds = scanner.staleNetworkSourceIds(),
                            )
                        }
                        needsForegroundReconcile = false
                        reconcileRequest?.let { request ->
                            startRefresh(request, showLoadingIndicator = false)
                        }
                    }
                }
            }
        }
    }

    override fun onPermissionChanged(granted: Boolean) {
        if (!started.get() || released.get()) return
        if (_scanState.value.permissionGranted == granted) return
        permissionChangeVersion += 1L
        _scanState.update { current ->
            current.copy(permissionGranted = granted, errorMessage = if (granted) current.errorMessage else null)
        }
        if (granted) {
            updateObserverRegistration()
            bootstrapLibrary()
        } else {
            _runtimeState.value = LibraryRuntimeState.NoPermission
            releaseObserversAndJobs(clearPermissionState = false)
            _scanState.value = LibraryScanState(permissionGranted = false)
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        started.set(false)
        needsForegroundReconcile = false
        releaseObserversAndJobs(clearPermissionState = true)
        foregroundObserverJob?.cancel()
        foregroundObserverJob = null
        _runtimeState.value = LibraryRuntimeState.Released
    }

    private fun bootstrapLibrary() {
        if (_runtimeState.value !is LibraryRuntimeState.NoPermission) return
        val bootstrapPermissionVersion = permissionChangeVersion
        _runtimeState.value = LibraryRuntimeState.Bootstrapping(bootstrapPermissionVersion)
        scanJob = scope.launch {
            try {
                val cachedSnapshot = withContext(ioDispatcher) {
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
                    val cachedContent = snapshotPublisher.stateForSnapshot(
                        snapshot = cachedSnapshot.snapshot,
                        removingSongIds = deletionMarkers.pendingSongIds.value,
                        removingAlbumIds = deletionMarkers.pendingAlbumIds.value,
                    )
                    if (!hasCurrentPermission(bootstrapPermissionVersion)) return@launch
                    snapshotPublisher.publishState(cachedContent)
                    val cachedScanState = LibraryScanState(
                        permissionGranted = true,
                        isLoading = false,
                        scanProgress = 1f,
                        isAuthoritative = false,
                    )
                    if (_scanState.value != cachedScanState) {
                        _scanState.value = cachedScanState
                    }
                    val currentSyncState = withContext(ioDispatcher) { scanner.currentSyncState() }
                    val networkSourceNeedsRefresh = withContext(ioDispatcher) {
                        scanner.networkSourceNeedsRefresh()
                    }
                    if (!hasCurrentPermission(bootstrapPermissionVersion)) return@launch
                    lastSuccessfulMediaStoreSyncState = cachedSnapshot.syncState
                    val syncDecision = decideLibrarySyncAtStartup(
                        cached = cachedSnapshot.syncState,
                        current = currentSyncState,
                        cachedSongCount = cachedSnapshot.snapshot.songs.size,
                        hasSafSelections = scanner.hasSafSelections(),
                    )
                    if (syncDecision != LibrarySyncDecision.ReuseCached || networkSourceNeedsRefresh) {
                        val incrementalRequest = if (
                            syncDecision == LibrarySyncDecision.IncrementalScan &&
                            !scanner.hasSafSelections()
                        ) {
                            cachedSnapshot.syncState?.let(::mediaStoreIncrementalRefreshRequest)
                        } else {
                            null
                        }
                        startRefresh(
                            request = LibraryRefreshRequest(
                                forceMediaIndex = false,
                                enrichMetadata = false,
                                mediaStoreGenerationFloor = incrementalRequest?.mediaStoreGenerationFloor,
                                mediaStoreGenerationFloors = incrementalRequest?.mediaStoreGenerationFloors.orEmpty(),
                            ),
                            showLoadingIndicator = false,
                        )
                    } else if (cachedSnapshotNeedsMetadata) {
                        startRefresh(
                            request = LibraryRefreshRequest(enrichMetadata = true),
                            showLoadingIndicator = false,
                        )
                    } else {
                        _scanState.update { it.copy(isAuthoritative = true) }
                    }
                } else {
                    startRefresh(
                        request = LibraryRefreshRequest(
                        // MediaStore is the authoritative catalog. A first run must not walk
                        // every file under Music just to read rows the provider already indexes.
                            forceMediaIndex = false,
                            enrichMetadata = false,
                        ),
                        showLoadingIndicator = true,
                    )
                }
            } finally {
                if (scanJob === currentCoroutineContext()[Job]) {
                    scanJob = null
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
        targetedSafTreeIds: Set<String>? = null,
        targetedNetworkSourceIds: Set<String>? = null,
        mediaStoreGenerationFloor: Long? = null,
        mediaStoreGenerationFloors: Map<String, Long> = emptyMap(),
        targetedPaths: List<String> = emptyList(),
        reuseLocalState: Boolean = false,
        priority: LibraryRefreshPriority = LibraryRefreshPriority.Background,
        removedPaths: List<String> = emptyList(),
    ) {
        if (released.get() || !_scanState.value.permissionGranted) return
        val request = LibraryRefreshRequest(
            forceMediaIndex = forceMediaIndex,
            enrichMetadata = enrichMetadata,
            targetedPaths = targetedPaths,
            targetedSafTreeIds = targetedSafTreeIds,
            targetedNetworkSourceIds = targetedNetworkSourceIds,
            mediaStoreGenerationFloor = mediaStoreGenerationFloor,
            mediaStoreGenerationFloors = mediaStoreGenerationFloors,
            reuseLocalState = reuseLocalState,
            priority = priority,
            removedPaths = removedPaths,
        )
        if (scanJob?.isActive == true) {
            refreshRequests.enqueue(request)
            if (
                request.priority == LibraryRefreshPriority.FreshnessCritical &&
                (_runtimeState.value as? LibraryRuntimeState.Scanning)
                    ?.request
                    ?.priority != LibraryRefreshPriority.FreshnessCritical
            ) {
                val activeScan = scanJob
                activeScan?.invokeOnCompletion {
                    continueAfterPreemptedScan()
                }
                activeScan?.cancel()
            }
            backendEventSink.emitLazy {
                BackendEvent.LibraryRefreshCoalesced(
                    mapOf(
                        "force_index" to forceMediaIndex.toString(),
                        "enrich_metadata" to enrichMetadata.toString(),
                    ),
                )
            }
            return
        }
        startRefresh(request, showLoadingIndicator)
    }

    @Suppress("TooGenericExceptionCaught")
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
            val scanResource = resourceTracker.acquire(BackendResourceKind.ActiveScan)
            val operation = BackendOperationContext(operationIdGenerator.nextId(), BackendSubsystem.Library, clock.elapsedTimeMs())
            val currentScanJob = currentCoroutineContext()[Job]
            val scanPermissionVersion = permissionChangeVersion
            val refreshRequest = refreshRequests.takeForImmediateScan(request)
            _runtimeState.value = LibraryRuntimeState.Scanning(refreshRequest, scanPermissionVersion)
            backendEventSink.emitLazy {
                BackendEvent.LibraryScanStarted(
                    operation.fields(
                        phase = "scan_started",
                        elapsedTimeMs = clock.elapsedTimeMs(),
                        extra = mapOf(
                            "force_index" to refreshRequest.forceMediaIndex.toString(),
                            "enrich_metadata" to refreshRequest.enrichMetadata.toString(),
                            "targeted_paths" to refreshRequest.targetedPaths.size.toString(),
                            "targeted_saf_trees" to (refreshRequest.targetedSafTreeIds?.size?.toString() ?: "all"),
                            "targeted_network_sources" to
                                (refreshRequest.targetedNetworkSourceIds?.size?.toString() ?: "all"),
                        ),
                    ),
                )
            }
            val progressThrottler = LibraryScanProgressThrottler(clock)
            try {
                val scanResult = scanLibrary(
                    refreshRequest,
                    showLoadingIndicator,
                    scanPermissionVersion,
                    progressThrottler,
                )
                publishSuccessfulScan(
                    scanResult = scanResult,
                    refreshRequest = refreshRequest,
                    scanPermissionVersion = scanPermissionVersion,
                    operation = operation,
                )
            } catch (throwable: CancellationException) {
                backendEventSink.emitLazy {
                    BackendEvent.OperationCancelled(
                        operation.fields(
                            phase = "scan_cancelled",
                            elapsedTimeMs = clock.elapsedTimeMs(),
                        ),
                    )
                }
                throw throwable
            } catch (throwable: Exception) {
                backendEventSink.emitLazy {
                    BackendEvent.LibraryScanFailed(
                        operation.fields(
                            phase = "scan_failed",
                            elapsedTimeMs = clock.elapsedTimeMs(),
                            extra = mapOf("error_type" to (throwable::class.simpleName ?: "Unknown")),
                        ),
                    )
                }
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
            } finally {
                scanResource.close()
                if (scanJob === currentScanJob) scanJob = null
            }

            if (scanJob != null || !hasCurrentPermission(scanPermissionVersion)) return@launch
            val pendingRequest = refreshRequests.takePendingAfterScan()
            if (pendingRequest != null && _scanState.value.permissionGranted) {
                if (backgroundWorkPolicy.shouldDeferLibraryRefresh(
                        freshnessCritical = pendingRequest.priority == LibraryRefreshPriority.FreshnessCritical,
                    )
                ) {
                    holdDeferredRefresh(pendingRequest)
                } else if (pendingRequest.safProviderRetryAttempt > 0) {
                    refreshDebounceJob?.cancel()
                    refreshDebounceJob = scope.launch {
                        delay(SAF_PROVIDER_RETRY_DELAY_MS)
                        refreshDebounceJob = null
                        if (!released.get() && scanJob == null && _scanState.value.permissionGranted) {
                            startRefresh(pendingRequest, showLoadingIndicator = false)
                        }
                    }
                } else {
                    startRefresh(pendingRequest, showLoadingIndicator = false)
                }
            } else if (_runtimeState.value is LibraryRuntimeState.Scanning) {
                _runtimeState.value = LibraryRuntimeState.Idle
            }
        }
    }

    private suspend fun publishSuccessfulScan(
        scanResult: CoordinatedLibraryScan,
        refreshRequest: LibraryRefreshRequest,
        scanPermissionVersion: Long,
        operation: BackendOperationContext,
    ) {
        if (!hasCurrentPermission(scanPermissionVersion)) return
        val prepared = prepareVisibleSnapshot(
            snapshot = scanResult.snapshot,
            scanComplete = scanResult.isComplete,
        )
        val nextScanState = LibraryScanState(
            permissionGranted = true,
            isLoading = false,
            scanProgress = 1f,
            errorMessage = if (prepared.snapshot.songs.isEmpty()) scanResult.incompleteMessage else null,
            isAuthoritative = scanResult.isComplete,
        )
        if (_scanState.value != nextScanState) {
            _scanState.value = nextScanState
        }
        if (scanResult.isComplete) {
            withContext(ioDispatcher) {
                val syncState = scanner.currentSyncState()
                ElovaireTrace.section("library_snapshot_persist") {
                    snapshotStore.save(
                        snapshot = prepared.snapshot,
                        filterFingerprint = scanner.currentFilterFingerprint(),
                        syncState = syncState,
                    )
                }
                ElovaireTrace.section("library_room_index_commit") {
                    libraryIndexStore?.applyChangeSet(
                        changeSet = prepared.changeSet,
                        snapshot = prepared.snapshot,
                        fullRebuild = refreshRequest.forceMediaIndex &&
                            prepared.changeSet.added.size == prepared.snapshot.songs.size,
                    )
                }
                lastSuccessfulMediaStoreSyncState = syncState
            }
        }
        invalidateArtworkBitmapCache(prepared.changeSet.artworkInvalidatedUris)
        if (!hasCurrentPermission(scanPermissionVersion)) return
        val snapshotNeedsMetadata = prepared.snapshot.songs.any { song ->
            !song.metadataResolved ||
                song.releaseYear == null ||
                song.qualityNeedsEnrichment() ||
                song.genre.isBlank() ||
                song.genre == "Unknown Genre"
        }
        val retrySafProviderLoading =
            scanResult.retryableSafTreeIds.isNotEmpty() &&
            refreshRequest.safProviderRetryAttempt < MAX_SAF_PROVIDER_LOADING_RETRIES
        if (retrySafProviderLoading) {
            refreshRequests.enqueue(
                LibraryRefreshRequest(
                    targetedSafTreeIds = scanResult.retryableSafTreeIds,
                    targetedNetworkSourceIds = emptySet(),
                    reuseLocalState = true,
                    safProviderRetryAttempt = refreshRequest.safProviderRetryAttempt + 1,
                ),
            )
        } else if (!refreshRequest.enrichMetadata && snapshotNeedsMetadata) {
            refreshRequests.enqueue(enrichMetadata = true)
        }
        backendEventSink.emitLazy {
            BackendEvent.LibraryScanCompleted(
                operation.fields(
                    phase = "scan_completed",
                    elapsedTimeMs = clock.elapsedTimeMs(),
                    extra = mapOf(
                        "songs" to prepared.snapshot.songs.size.toString(),
                        "albums" to prepared.snapshot.albums.size.toString(),
                    ),
                    metrics = BackendOperationMetrics(
                        itemsOutput = prepared.snapshot.songs.size,
                        rowsChanged = prepared.changeSet.added.size +
                            prepared.changeSet.updated.size +
                            prepared.changeSet.relocated.size +
                            prepared.changeSet.removed.size,
                        fallback = !scanResult.isComplete,
                    ),
                ),
            )
        }
    }

    private data class PreparedLibrarySnapshot(
        val snapshot: LibrarySnapshot,
        val changeSet: LibraryChangeSet,
    )

    private suspend fun prepareVisibleSnapshot(
        snapshot: LibrarySnapshot,
        scanComplete: Boolean,
    ): PreparedLibrarySnapshot {
        val songs = songsVisibleAfterDirectPathTombstones(snapshot.songs)
        if (scanComplete) {
            clearResolvedDirectPathTombstones(snapshot.songs)
        }
        val scannedSongIds = songs.mapTo(hashSetOf(), Song::id)
        deletionMarkers.retainConfirmedSongsStillIn(scannedSongIds)
        var suppressedSongIds = deletionMarkers.suppressingSongIds()
        var visibleSongs = songsVisibleAfterDeletionMarkers(songs, suppressedSongIds)
        var preparedSnapshot = if (visibleSongs === snapshot.songs) {
            snapshot
        } else {
            ElovaireTrace.suspendSection("library_prepare_content") {
                withContext(defaultDispatcher) {
                    snapshotPublisher.prepareSongs(visibleSongs)
                }
            }
        }
        val latestSuppressedSongIds = deletionMarkers.suppressingSongIds()
        if (latestSuppressedSongIds != suppressedSongIds) {
            suppressedSongIds = latestSuppressedSongIds
            visibleSongs = songsVisibleAfterDeletionMarkers(songs, suppressedSongIds)
            preparedSnapshot = ElovaireTrace.suspendSection("library_prepare_content_refresh") {
                withContext(defaultDispatcher) {
                    snapshotPublisher.prepareSongs(visibleSongs)
                }
            }
        }
        val previousSongs = _contentState.value.songs
        val nextContentState = ElovaireTrace.section("library_prepare_content_state") {
            snapshotPublisher.stateForSnapshot(
                snapshot = preparedSnapshot,
                removingSongIds = deletionMarkers.pendingSongIds.value,
                removingAlbumIds = deletionMarkers.pendingAlbumIds.value,
            )
        }
        val nextSnapshot = snapshotPublisher.snapshotOf(nextContentState)
        val changeSet = ElovaireTrace.section("library_diff") {
            LibraryChangeSetCalculator.between(previousSongs, nextSnapshot.songs)
        }
        if (changeSet.relocated.isNotEmpty()) {
            val relocationOutcome = onSongRelocations(
                changeSet.relocated.associate { relocation ->
                    relocation.before.id to relocation.after.id
                },
            )
            when (relocationOutcome) {
                SongRelocationOutcome.Applied -> Unit
                SongRelocationOutcome.RetryableFailure -> error(
                    "Unable to preserve user-data references during media relocation; retry is required.",
                )
                SongRelocationOutcome.UnrecoverableConflict -> error(
                    "Media relocation conflicts with existing user-data references.",
                )
            }
        }
        snapshotPublisher.publishState(nextContentState)
        return PreparedLibrarySnapshot(
            snapshot = nextSnapshot,
            changeSet = changeSet,
        )
    }

    private suspend fun scanLibrary(
        request: LibraryRefreshRequest,
        showLoadingIndicator: Boolean,
        permissionVersion: Long,
        progressThrottler: LibraryScanProgressThrottler,
    ): CoordinatedLibraryScan = withContext(ioDispatcher) {
        val existingSnapshot = snapshotPublisher.snapshotOf(_contentState.value)
        ElovaireTrace.suspendSection("library_refresh_scan") {
            scanner.scanWithStatus(
                refreshMediaIndex = request.forceMediaIndex,
                refreshMediaPaths = request.targetedPaths,
                enrichMetadata = request.enrichMetadata,
                mediaStoreGenerationFloor = request.mediaStoreGenerationFloor,
                mediaStoreGenerationFloors = request.mediaStoreGenerationFloors,
                targetedSafTreeIds = request.targetedSafTreeIds,
                targetedNetworkSourceIds = request.targetedNetworkSourceIds,
                baseSnapshot = existingSnapshot,
                reuseLocalState = request.reuseLocalState,
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
        val normalizedPaths = resolveTargetedRefreshPaths(
            requestedPaths = filePaths,
            songIds = songIds,
            currentSongs = _contentState.value.songs,
        )
        if (normalizedPaths.isEmpty()) {
            if (enrichMetadata && songIds.isEmpty()) {
                scanner.clearMetadataCache()
            }
            refresh(
                // A known item with no path (for example a SAF document) is
                // still refreshable through its content URI. Re-indexing the
                // entire storage tree here is both unnecessary and unsafe.
                forceMediaIndex = false,
                enrichMetadata = enrichMetadata,
                showLoadingIndicator = false,
                priority = LibraryRefreshPriority.FreshnessCritical,
            )
            return
        }
        if (enrichMetadata) {
            scanner.invalidateMetadataCacheForPaths(normalizedPaths)
        }
        val request = LibraryRefreshRequest(
            enrichMetadata = enrichMetadata,
            targetedPaths = normalizedPaths,
            priority = LibraryRefreshPriority.FreshnessCritical,
        )
        if (scanJob?.isActive == true) {
            refreshRequests.enqueue(request)
            return
        }
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        startRefresh(request, showLoadingIndicator = false)
    }

    internal fun onMemoryPressure(pressure: MemoryPressure) {
        scanner.onMemoryPressure(pressure)
    }

    override fun blockNetworkSources(sourceIds: Set<String>) {
        scanner.blockNetworkSources(sourceIds)
    }

    override fun unblockNetworkSource(sourceId: String) {
        scanner.unblockNetworkSource(sourceId)
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
        observerController.expectSelfMutation(
            paths = request.filePaths,
            uris = request.uris,
            durationMs = DELETE_OBSERVER_SUPPRESSION_MS,
        )
        refreshDebounceJob?.cancel()
        refreshDebounceJob = null
        refreshRequests.clearIndexRefresh()
        scanner.invalidateMetadataCacheForSongIds(request.songIds)
        scanner.invalidateMetadataCacheForPaths(request.filePaths)

        val remainingSongs = _contentState.value.songs.filterNot { it.id in request.songIds }
        val publication = publishLibraryContent(remainingSongs)
        val updatedState = publication.state
        withContext(ioDispatcher) {
            val updatedSnapshot = snapshotPublisher.snapshotOf(updatedState)
            snapshotStore.save(
                snapshot = updatedSnapshot,
                filterFingerprint = scanner.currentFilterFingerprint(),
                syncState = scanner.currentSyncState(),
            )
            libraryIndexStore?.applyChangeSet(
                changeSet = publication.changeSet,
                snapshot = updatedSnapshot,
            )
        }
        invalidateArtworkBitmapCache(publication.changeSet.artworkInvalidatedUris)

        var remainingTargets = request.uriBySongId
        var stillPresent = withContext(ioDispatcher) {
            if (remainingTargets.isNotEmpty()) {
                scanner.targetExistenceProbe.findExistingSongIds(remainingTargets)
            } else {
                scanner.findExistingSongIds(request.songIds)
            }
        }
        var pollCount = 0
        while (stillPresent.isNotEmpty() && pollCount < DELETE_CONFIRMATION_MAX_POLLS) {
            pollCount += 1
            delay(DELETE_CONFIRMATION_POLL_MS)
            stillPresent = withContext(ioDispatcher) {
                if (remainingTargets.isNotEmpty()) {
                    remainingTargets = remainingTargets.filterKeys { it in stillPresent }
                    scanner.targetExistenceProbe.findExistingSongIds(remainingTargets)
                } else {
                    scanner.findExistingSongIds(stillPresent)
                }
            }
        }
        val deletedSongIds = request.songIds - stillPresent
        val deletedAlbumIds = fullyDeletedAlbumIds.filterTo(linkedSetOf()) { albumId ->
            updatedState.albums.none { it.id == albumId }
        }
        deletionMarkers.confirmDeletedSongs(deletedSongIds)
        clearPendingDeletedSongs(request.songIds)
        clearPendingDeletedAlbums(fullyDeletedAlbumIds)
        if (stillPresent.isNotEmpty()) {
            refresh(
                forceMediaIndex = false,
                enrichMetadata = false,
                showLoadingIndicator = false,
                priority = LibraryRefreshPriority.FreshnessCritical,
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

    override suspend fun applyVerifiedTagEdits(editedSongs: List<Song>) {
        if (editedSongs.isEmpty()) return
        val current = _contentState.value
        val updatedState = snapshotPublisher.patchSongs(
            editedSongs = editedSongs,
            removingSongIds = current.removingSongIds,
            removingAlbumIds = current.removingAlbumIds,
        )
        val changeSet = snapshotPublisher.takeLastPatchChangeSet()
        if (changeSet.isEmpty) return
        withContext(ioDispatcher) {
            val updatedSnapshot = snapshotPublisher.snapshotOf(updatedState)
            snapshotStore.save(
                snapshot = updatedSnapshot,
                filterFingerprint = scanner.currentFilterFingerprint(),
                syncState = scanner.currentSyncState(),
            )
            libraryIndexStore?.applyChangeSet(
                changeSet = changeSet,
                snapshot = updatedSnapshot,
            )
        }
        invalidateArtworkBitmapCache(changeSet.artworkInvalidatedUris)
    }

    fun albumById(albumId: Long): Album? = _contentState.value.albums.firstOrNull { it.id == albumId }

    fun defaultMediaFolderPath(): String = scanner.musicDirectory().absolutePath

    fun setLibraryFolders(
        selections: List<LibraryFolderSelection>,
        enrichMetadata: Boolean = false,
        showLoadingIndicator: Boolean = _contentState.value.songs.isEmpty(),
    ) {
        val previousSelections = scanner.libraryFolderSelections()
        val normalizedSelections = LibraryFolderSelectionResolver.normalize(selections)
        val addedSelections = normalizedSelections.filterNot(previousSelections::contains)
        val onlyAddingSafTrees = addedSelections.isNotEmpty() &&
            normalizedSelections.size > previousSelections.size &&
            previousSelections.all(normalizedSelections::contains) &&
            addedSelections.all { it.uri != null }
        val addedSafTreeIds = addedSelections.mapNotNull { selection -> safTreeIdentity(selection.uri) }.toSet()
        val canTargetAddedSafTrees = onlyAddingSafTrees && addedSafTreeIds.size == addedSelections.size
        val changed = scanner.setLibraryFolders(selections)
        if (!changed) return
        if (_scanState.value.permissionGranted) {
            if (backgroundWorkPolicy.shouldKeepRecursiveLibraryObservers(permissionGranted = true)) {
                observerController.ensureLibraryFolderObservers(forceRebuild = true)
            }
            if (backgroundWorkPolicy.shouldKeepMediaStoreObserver(permissionGranted = true)) {
                observerController.ensureSafTreeObservers(forceRebuild = true)
            }
            refresh(
                forceMediaIndex = addedSelections.any { it.uri == null },
                enrichMetadata = enrichMetadata,
                showLoadingIndicator = showLoadingIndicator,
                targetedSafTreeIds = addedSafTreeIds.takeIf { canTargetAddedSafTrees },
                targetedNetworkSourceIds = emptySet<String>()
                    .takeIf { onlyAddingSafTrees && !enrichMetadata },
                reuseLocalState = onlyAddingSafTrees && !enrichMetadata,
                priority = LibraryRefreshPriority.FreshnessCritical,
            )
        }
    }

    override fun setNetworkSources(
        sources: List<NetworkLibrarySource>,
        enrichMetadata: Boolean,
        showLoadingIndicator: Boolean,
        forceRefreshSourceIds: Set<String>,
    ) {
        val changedNetworkSourceIds = scanner.networkSourceIdsChanged(sources)
        val changed = scanner.setNetworkSources(sources)
        if (!changed && forceRefreshSourceIds.isEmpty()) return
        refresh(
            forceMediaIndex = false,
            enrichMetadata = enrichMetadata,
            showLoadingIndicator = showLoadingIndicator,
            targetedNetworkSourceIds = changedNetworkSourceIds + forceRefreshSourceIds,
            priority = LibraryRefreshPriority.FreshnessCritical,
        )
    }

    private fun scheduleMediaRefresh(change: LibraryObservedChange) {
        val request = when (change) {
            is LibraryObservedChange.MediaStore -> LibraryRefreshRequest(
                priority = LibraryRefreshPriority.FreshnessCritical,
            )
            is LibraryObservedChange.SafTree -> LibraryRefreshRequest(
                priority = LibraryRefreshPriority.FreshnessCritical,
            )
            LibraryObservedChange.CoverageIncomplete -> LibraryRefreshRequest(
                forceMediaIndex = true,
                priority = LibraryRefreshPriority.FreshnessCritical,
            )
            is LibraryObservedChange.DirectFile -> {
                noteDirectPathChange(change.path, change.operation)
                LibraryRefreshRequest(
                    targetedPaths = listOf(change.path),
                    priority = LibraryRefreshPriority.FreshnessCritical,
                    removedPaths = listOfNotNull(
                        change.path.takeIf {
                            change.operation == DirectFileOperation.Delete ||
                                change.operation == DirectFileOperation.MoveFrom
                        },
                    ),
                )
            }
        }
        scope.launch {
            if (released.get() || !_scanState.value.permissionGranted) return@launch
            refreshRequests.enqueue(request)
            if (backgroundWorkPolicy.shouldDeferLibraryRefresh(
                    freshnessCritical = request.priority == LibraryRefreshPriority.FreshnessCritical,
                )
            ) {
                val pending = refreshRequests.takePendingAfterScan() ?: return@launch
                holdDeferredRefresh(pending)
                return@launch
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
    }

    private fun holdDeferredRefresh(incoming: LibraryRefreshRequest) {
        val existing = when (val runtime = _runtimeState.value) {
            is LibraryRuntimeState.BackgroundDirty -> runtime.pending
            is LibraryRuntimeState.InteractionDirty -> runtime.pending
            else -> null
        }
        val merged = mergeBackgroundRefreshRequest(existing = existing, incoming = incoming)
        _runtimeState.value = if (backgroundWorkPolicy.isForeground.value) {
            LibraryRuntimeState.InteractionDirty(merged)
        } else {
            LibraryRuntimeState.BackgroundDirty(merged)
        }
    }

    private fun continueAfterPreemptedScan() {
        scope.launch {
            while (scanJob?.isActive == true) yield()
            if (released.get() || !_scanState.value.permissionGranted) return@launch
            val pending = refreshRequests.takePendingAfterScan() ?: return@launch
            if (backgroundWorkPolicy.shouldDeferLibraryRefresh(
                    freshnessCritical = pending.priority == LibraryRefreshPriority.FreshnessCritical,
                )
            ) {
                holdDeferredRefresh(pending)
            } else {
                startRefresh(pending, showLoadingIndicator = false)
            }
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

    private fun noteDirectPathChange(
        path: String,
        operation: DirectFileOperation,
    ) {
        val key = LibrarySongDuplicateResolver.normalizedRealPath(path) ?: return
        synchronized(directPathTombstonesLock) {
            when (operation) {
                DirectFileOperation.Delete,
                DirectFileOperation.MoveFrom,
                -> directPathTombstones += key
                DirectFileOperation.Create,
                DirectFileOperation.CloseWrite,
                DirectFileOperation.MoveTo,
                -> directPathTombstones.remove(key)
                DirectFileOperation.DirectoryTopology,
                DirectFileOperation.Other,
                -> Unit
            }
        }
    }

    private fun songsVisibleAfterDirectPathTombstones(songs: List<Song>): List<Song> {
        val tombstones = synchronized(directPathTombstonesLock) { directPathTombstones.toSet() }
        if (tombstones.isEmpty()) return songs
        val filtered = songs.filterNot { song ->
            LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath) in tombstones
        }
        return filtered.takeIf { it.size != songs.size } ?: songs
    }

    private fun clearResolvedDirectPathTombstones(scannedSongs: List<Song>) {
        val presentPaths = scannedSongs.asSequence()
            .mapNotNull { song -> LibrarySongDuplicateResolver.normalizedRealPath(song.libraryPath) }
            .toSet()
        synchronized(directPathTombstonesLock) {
            directPathTombstones.retainAll(presentPaths)
        }
    }

    private data class LibraryContentPublication(
        val state: LibraryContentState,
        val changeSet: LibraryChangeSet,
    )

    private fun publishLibraryContent(
        songs: List<Song>,
        removingSongIds: Set<Long> = deletionMarkers.pendingSongIds.value,
        removingAlbumIds: Set<Long> = deletionMarkers.pendingAlbumIds.value,
    ): LibraryContentPublication {
        val previousSongs = _contentState.value.songs
        val state = snapshotPublisher.publishSongs(
            songs = songs,
            removingSongIds = removingSongIds,
            removingAlbumIds = removingAlbumIds,
        )
        return LibraryContentPublication(
            state = state,
            changeSet = LibraryChangeSetCalculator.between(previousSongs, state.songs),
        )
    }

    private companion object {
        const val AUTO_REFRESH_DEBOUNCE_MS = 350L
        const val DELETE_OBSERVER_SUPPRESSION_MS = 1_200L
        const val DELETE_CONFIRMATION_POLL_MS = 100L
        const val DELETE_CONFIRMATION_MAX_POLLS = 5
        const val MAX_SAF_PROVIDER_LOADING_RETRIES = 6
        const val SAF_PROVIDER_RETRY_DELAY_MS = 250L
    }

    private fun releaseObserversAndJobs(clearPermissionState: Boolean) {
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
        synchronized(directPathTombstonesLock) { directPathTombstones.clear() }
        if (clearPermissionState) {
            _scanState.value = _scanState.value.copy(
                permissionGranted = false,
                isLoading = false,
                scanProgress = 0f,
            )
        }
    }

}

private fun LibraryRuntimeState.pendingRefreshIsFreshnessCritical(): Boolean {
    return when (this) {
        is LibraryRuntimeState.BackgroundDirty -> pending.priority == LibraryRefreshPriority.FreshnessCritical
        is LibraryRuntimeState.InteractionDirty -> pending.priority == LibraryRefreshPriority.FreshnessCritical
        else -> false
    }
}

private fun songsVisibleAfterDeletionMarkers(
    songs: List<Song>,
    suppressedSongIds: Set<Long>,
): List<Song> = if (suppressedSongIds.isEmpty()) {
    songs
} else {
    songs.filterNot { it.id in suppressedSongIds }
}

private class LibraryScanProgressThrottler(
    private val clock: AppClock = AndroidAppClock,
    private val minStep: Float = 0.01f,
    private val minIntervalMs: Long = 80L,
) {
    private var lastProgress = -1f
    private var lastEmitMs = 0L

    fun shouldEmit(progress: Float): Boolean {
        val now = clock.elapsedTimeMs()
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
