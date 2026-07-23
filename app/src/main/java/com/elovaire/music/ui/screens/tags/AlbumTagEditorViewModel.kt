package elovaire.music.droidbeauty.app.ui.screens.tags

import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.invalidateNotificationArtworkCache
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.mutatedUris
import elovaire.music.droidbeauty.app.data.tags.retryForFailures
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.platform.matchesPlatformActionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface AlbumTagEditorEvent {
    data class RequestWritePermission(
        val operationId: String,
        val request: AlbumTagEditRequest,
        val uris: List<Uri>,
    ) : AlbumTagEditorEvent

    data class RequestRecoverableWritePermission(
        val operationId: String,
        val request: AlbumTagEditRequest,
        val intentSender: IntentSender,
    ) : AlbumTagEditorEvent

    data object SaveSucceeded : AlbumTagEditorEvent

    data class SavePartiallySucceeded(
        val failures: List<TagEditFailureUi>,
    ) : AlbumTagEditorEvent
}

internal class AlbumTagEditorViewModel(
    private val libraryRepository: LibraryRepository,
    private val tagEditorService: AlbumTagEditorService,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
) : ViewModel() {
    private val albumId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(AlbumTagEditorUiState())
    val uiState: StateFlow<AlbumTagEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AlbumTagEditorEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AlbumTagEditorEvent> = _events.asSharedFlow()
    private var pendingWriteRequest: PendingTagWrite? = null

    init {
        viewModelScope.launch {
            combine(
                albumId,
                libraryRepository.contentState,
            ) { targetAlbumId, content ->
                targetAlbumId?.let { id -> content.albums.firstOrNull { it.id == id } }
            }.collectLatest { album ->
                if (album == null) {
                    _uiState.value = _uiState.value.copy(
                        originalAlbum = null,
                        isLoading = false,
                    ).recalculateFlags()
                    return@collectLatest
                }
                val current = _uiState.value
                if (current.albumId == album.id && current.hasUnsavedChanges && current.originalAlbum != null) {
                    return@collectLatest
                }
                _uiState.value = album.toTagEditorUiState()
            }
        }
    }

    fun loadAlbum(targetAlbumId: Long) {
        if (_uiState.value.albumId == targetAlbumId && _uiState.value.originalAlbum != null) return
        _uiState.value = AlbumTagEditorUiState(albumId = targetAlbumId, isLoading = true)
        albumId.value = targetAlbumId
    }

    fun clearAlbum() {
        albumId.value = null
        _uiState.value = AlbumTagEditorUiState(isLoading = false)
    }

    fun onAlbumTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(
            albumTitle = value,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onAlbumArtistChange(value: String) {
        _uiState.value = _uiState.value.copy(
            albumArtist = value,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onReleaseYearChange(value: String) {
        val normalizedYear = value.filter(Char::isDigit).take(4)
        _uiState.value = _uiState.value.copy(
            releaseYear = normalizedYear,
            yearClearedExplicitly = value.isBlank(),
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onGenreChange(value: String) {
        _uiState.value = _uiState.value.copy(
            genre = value,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onTrackTitleChange(songId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            tracks = _uiState.value.tracks.map { track ->
                if (track.songId == songId) track.copy(title = value) else track
            },
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onTrackArtistChange(songId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            tracks = _uiState.value.tracks.map { track ->
                if (track.songId == songId) track.copy(artist = value) else track
            },
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onTrackNumberChange(songId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            tracks = _uiState.value.tracks.map { track ->
                if (track.songId == songId) {
                    track.copy(trackNumber = value.filter(Char::isDigit))
                } else {
                    track
                }
            },
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onDiscNumberChange(songId: Long, value: String) {
        _uiState.value = _uiState.value.copy(
            tracks = _uiState.value.tracks.map { track ->
                if (track.songId == songId) {
                    track.copy(discNumber = value.filter(Char::isDigit))
                } else {
                    track
                }
            },
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun onPickedCoverArt(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedArtworkUri = uri ?: _uiState.value.selectedArtworkUri,
            selectedArtworkBytes = null,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
    }

    fun requestSave() {
        val currentState = _uiState.value
        if (currentState.isSaving) return
        val request = currentState.toAlbumTagEditRequest() ?: return
        _uiState.value = currentState.copy(
            isSaving = true,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
        val pending = PendingTagWrite(operationIdGenerator.nextId(), request)
        pendingWriteRequest = pending
        viewModelScope.launch {
            _events.emit(
                AlbumTagEditorEvent.RequestWritePermission(
                    operationId = pending.operationId,
                    request = request,
                    uris = request.mutatedUris(),
                ),
            )
        }
    }

    fun onWritePermissionResult(
        operationId: String,
        granted: Boolean,
    ) {
        if (!matchesPlatformActionResult(pendingWriteRequest?.operationId, operationId)) return
        val pending = pendingWriteRequest ?: return
        if (!granted) {
            pendingWriteRequest = null
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                statusMessage = "Write access was not granted.",
            ).recalculateFlags()
            return
        }
        pendingWriteRequest = null
        viewModelScope.launch {
            performSave(pending.request, writeConsentGranted = true)
        }
    }

    fun onWritePermissionLaunchFailed(operationId: String) {
        if (!matchesPlatformActionResult(pendingWriteRequest?.operationId, operationId)) return
        pendingWriteRequest = null
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            statusMessage = "Android could not open the write-access request.",
        ).recalculateFlags()
    }

    private suspend fun performSave(
        request: AlbumTagEditRequest,
        writeConsentGranted: Boolean,
    ) {
        _uiState.value = _uiState.value.copy(
            isSaving = true,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
        runCatching {
            withContext(Dispatchers.IO) {
                tagEditorService.applyEdits(
                    request = request,
                    writeConsentGranted = writeConsentGranted,
                )
            }
        }.onSuccess { result ->
            if (result.artworkChanged) {
                val artworkUrisToInvalidate = buildList {
                    add(request.album.artUri)
                    addAll(request.album.songs.map { it.artUri })
                }
                invalidateEditedArtwork(artworkUrisToInvalidate)
            }
            if (result.editedSongIds.isNotEmpty()) {
                libraryRepository.applyVerifiedTagEdits(result.editedSongs)
                libraryRepository.refreshChangedFiles(
                    filePaths = result.editedFilePaths,
                    songIds = result.editedSongIds,
                    enrichMetadata = true,
                )
            }
            val failures = result.failures.map { failure ->
                TagEditFailureUi(
                    songId = failure.songId,
                    fileName = failure.fileName,
                    reason = failure.reason,
                )
            }
            _uiState.value = _uiState.value.copy(
                originalAlbum = result.editedSongs.takeIf { it.isNotEmpty() }?.let { editedSongs ->
                    request.album.copy(
                        title = editedSongs.firstOrNull()?.album ?: request.album.title,
                        artist = editedSongs.firstOrNull()?.albumArtist ?: editedSongs.firstOrNull()?.artist ?: request.album.artist,
                        songs = request.album.songs.map { song ->
                            editedSongs.firstOrNull { it.id == song.id } ?: song
                        },
                    )
                } ?: _uiState.value.originalAlbum,
                isSaving = false,
                saveFailures = failures,
                statusMessage = when {
                    result.permissionRequest != null && result.editedSongIds.isNotEmpty() -> "Saved with ${failures.size} issue(s)."
                    result.permissionRequest != null -> "Additional write access is needed to finish saving."
                    failures.isEmpty() -> null
                    result.editedSongIds.isNotEmpty() -> "Saved with ${failures.size} issue(s)."
                    else -> failures.firstOrNull()?.reason ?: "No tags were saved."
                },
            ).recalculateFlags()
            if (result.permissionRequest != null) {
                val retryRequest = request.retryForFailures(
                    failedSongIds = result.failures.map { it.songId }.toSet(),
                )
                val pending = PendingTagWrite(operationIdGenerator.nextId(), retryRequest)
                pendingWriteRequest = pending
                _events.emit(
                    AlbumTagEditorEvent.RequestRecoverableWritePermission(
                        operationId = pending.operationId,
                        request = retryRequest,
                        intentSender = result.permissionRequest.intentSender,
                    ),
                )
            } else if (failures.isEmpty() && result.editedSongIds.isNotEmpty()) {
                _events.emit(AlbumTagEditorEvent.SaveSucceeded)
            } else if (result.editedSongIds.isNotEmpty()) {
                _events.emit(AlbumTagEditorEvent.SavePartiallySucceeded(failures))
            }
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
            val recoverableIntentSender = when {
                throwable is RecoverableSecurityException -> {
                    throwable.userAction.actionIntent.intentSender
                }

                else -> null
            }
            if (recoverableIntentSender != null) {
                val pending = PendingTagWrite(operationIdGenerator.nextId(), request)
                pendingWriteRequest = pending
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = null,
                ).recalculateFlags()
                _events.emit(
                    AlbumTagEditorEvent.RequestRecoverableWritePermission(
                        operationId = pending.operationId,
                        request = request,
                        intentSender = recoverableIntentSender,
                    ),
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = throwable.message ?: "Unable to save tags.",
                ).recalculateFlags()
            }
        }
    }

    private data class PendingTagWrite(
        val operationId: String,
        val request: AlbumTagEditRequest,
    )

    private fun invalidateEditedArtwork(artworkUrisToInvalidate: List<Uri?>) {
        invalidateArtworkCaches(artworkUrisToInvalidate)
        invalidateNotificationArtworkCache(artworkUrisToInvalidate)
    }
}
