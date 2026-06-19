package elovaire.music.droidbeauty.app.ui.screens.tags

import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.playback.invalidateNotificationArtworkCache
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.AlbumTagMatchSuggestion
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import kotlinx.coroutines.Dispatchers
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
        val request: AlbumTagEditRequest,
        val uris: List<Uri>,
    ) : AlbumTagEditorEvent

    data class RequestRecoverableWritePermission(
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
) : ViewModel() {
    private val albumId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(AlbumTagEditorUiState())
    val uiState: StateFlow<AlbumTagEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AlbumTagEditorEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AlbumTagEditorEvent> = _events.asSharedFlow()

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
        _uiState.value = _uiState.value.copy(
            releaseYear = value.filter(Char::isDigit).take(4),
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

    fun matchOnline() {
        val currentState = _uiState.value
        val album = currentState.originalAlbum ?: return
        if (currentState.isMatchingOnline || currentState.isSaving) return
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isMatchingOnline = true,
                statusMessage = null,
                saveFailures = emptyList(),
            ).recalculateFlags()
            val result = withContext(Dispatchers.IO) {
                runCatching { tagEditorService.findBestOnlineMatch(album) }.getOrNull()
            }
            _uiState.value = if (result == null) {
                _uiState.value.copy(
                    isMatchingOnline = false,
                    statusMessage = "No close online match found.",
                ).recalculateFlags()
            } else {
                _uiState.value.applyMatchSuggestionSafely(result)
                    .copy(
                        isMatchingOnline = false,
                        matchedRelease = result,
                        statusMessage = null,
                    )
                    .recalculateFlags()
            }
        }
    }

    fun requestSave() {
        val request = _uiState.value.toAlbumTagEditRequest() ?: return
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                _events.emit(
                    AlbumTagEditorEvent.RequestWritePermission(
                        request = request,
                        uris = request.album.songs.map { it.uri },
                    ),
                )
            } else {
                performSave(request)
            }
        }
    }

    fun onWritePermissionResult(
        granted: Boolean,
        request: AlbumTagEditRequest?,
    ) {
        if (!granted || request == null) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                statusMessage = "Write access was not granted.",
            ).recalculateFlags()
            return
        }
        viewModelScope.launch {
            performSave(request)
        }
    }

    private suspend fun performSave(request: AlbumTagEditRequest) {
        _uiState.value = _uiState.value.copy(
            isSaving = true,
            statusMessage = null,
            saveFailures = emptyList(),
        ).recalculateFlags()
        runCatching {
            withContext(Dispatchers.IO) {
                tagEditorService.applyEdits(request)
            }
        }.onSuccess { result ->
            if (result.artworkChanged) {
                val artworkUrisToInvalidate = buildList {
                    add(request.album.artUri)
                    addAll(request.album.songs.map { it.artUri })
                }
                invalidateEditedArtwork(artworkUrisToInvalidate)
            }
            if (result.editedFilePaths.size == result.editedSongIds.size && result.editedFilePaths.isNotEmpty()) {
                libraryRepository.refreshChangedFiles(
                    filePaths = result.editedFilePaths,
                    enrichMetadata = true,
                )
            } else if (result.editedSongIds.isNotEmpty()) {
                libraryRepository.refresh(
                    forceMediaIndex = true,
                    enrichMetadata = true,
                    showLoadingIndicator = false,
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
                isSaving = false,
                saveFailures = failures,
                statusMessage = when {
                    failures.isEmpty() -> null
                    result.editedSongIds.isNotEmpty() -> "Saved with ${failures.size} issue(s)."
                    else -> failures.firstOrNull()?.reason ?: "No tags were saved."
                },
            ).recalculateFlags()
            if (failures.isEmpty()) {
                _events.emit(AlbumTagEditorEvent.SaveSucceeded)
            } else if (result.editedSongIds.isNotEmpty()) {
                _events.emit(AlbumTagEditorEvent.SavePartiallySucceeded(failures))
            }
        }.onFailure { throwable ->
            val recoverableIntentSender = when {
                throwable is RecoverableSecurityException -> {
                    throwable.userAction.actionIntent.intentSender
                }

                else -> null
            }
            if (recoverableIntentSender != null) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = null,
                ).recalculateFlags()
                _events.emit(
                    AlbumTagEditorEvent.RequestRecoverableWritePermission(
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

    private fun AlbumTagEditorUiState.applyMatchSuggestionSafely(
        suggestion: AlbumTagMatchSuggestion,
    ): AlbumTagEditorUiState {
        val updatedTracks = tracks.mapIndexed { index, track ->
            val matched = suggestion.tracks.getOrNull(index)
            if (matched == null) {
                track
            } else {
                track.copy(
                    title = matched.title.ifBlank { track.title },
                    artist = matched.artist.ifBlank { track.artist },
                    trackNumber = matched.trackNumber.toString(),
                    discNumber = matched.discNumber.toString(),
                )
            }
        }
        return copy(
            albumTitle = suggestion.albumTitle.ifBlank { albumTitle },
            albumArtist = suggestion.albumArtist.ifBlank { albumArtist },
            releaseYear = suggestion.releaseYear?.toString().orEmpty().ifBlank { releaseYear },
            tracks = updatedTracks,
            selectedArtworkBytes = suggestion.coverArtBytes ?: selectedArtworkBytes,
            selectedArtworkUri = if (suggestion.coverArtBytes != null) null else selectedArtworkUri,
        )
    }

    private fun invalidateEditedArtwork(artworkUrisToInvalidate: List<Uri?>) {
        invalidateArtworkCaches(artworkUrisToInvalidate)
        invalidateNotificationArtworkCache(artworkUrisToInvalidate)
    }
}
