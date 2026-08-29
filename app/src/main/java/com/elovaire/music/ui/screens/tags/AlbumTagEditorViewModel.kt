package elovaire.music.droidbeauty.app.ui.screens.tags

import android.app.RecoverableSecurityException
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditor
import elovaire.music.droidbeauty.app.data.tags.mutatedUris
import elovaire.music.droidbeauty.app.data.tags.retryForFailures
import elovaire.music.droidbeauty.app.platform.matchesPlatformActionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class AlbumTagEditorViewModel(
    private val libraryRepository: LibraryReader,
    private val libraryTagUpdates: LibraryTagUpdateWriter,
    private val tagEditorService: AlbumTagEditor,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val albumId = MutableStateFlow<Long?>(savedStateHandle[KEY_ALBUM_ID])
    private val _uiState = MutableStateFlow(AlbumTagEditorUiState())
    val uiState: StateFlow<AlbumTagEditorUiState> = _uiState.asStateFlow()
    private val writePermissionState = AlbumTagWritePermissionState(operationIdGenerator)

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
                _uiState.value = restoreDraft(album.toTagEditorUiState(), savedStateHandle)
            }
        }
    }

    fun loadAlbum(targetAlbumId: Long) {
        if (_uiState.value.albumId == targetAlbumId && _uiState.value.originalAlbum != null) return
        if (savedStateHandle.get<Long>(KEY_DRAFT_ALBUM_ID) != targetAlbumId) {
            clearSavedDraft()
        }
        savedStateHandle[KEY_ALBUM_ID] = targetAlbumId
        _uiState.value = AlbumTagEditorUiState(albumId = targetAlbumId, isLoading = true)
        albumId.value = targetAlbumId
    }

    fun clearAlbum() {
        savedStateHandle[KEY_ALBUM_ID] = null
        clearSavedDraft()
        albumId.value = null
        _uiState.value = AlbumTagEditorUiState(isLoading = false)
    }

    fun onAlbumTitleChange(value: String) {
        updateDraft {
            it.copy(
                albumTitle = value,
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onAlbumArtistChange(value: String) {
        updateDraft {
            it.copy(
                albumArtist = value,
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onReleaseYearChange(value: String) {
        val normalizedYear = value.filter(Char::isDigit).take(4)
        updateDraft {
            it.copy(
                releaseYear = normalizedYear,
                yearClearedExplicitly = value.isBlank(),
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onGenreChange(value: String) {
        updateDraft {
            it.copy(
                genre = value,
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onTrackTitleChange(songId: Long, value: String) {
        updateDraft {
            it.copy(
                tracks = it.tracks.map { track ->
                    if (track.songId == songId) track.copy(title = value) else track
                },
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onTrackArtistChange(songId: Long, value: String) {
        updateDraft {
            it.copy(
                tracks = it.tracks.map { track ->
                    if (track.songId == songId) track.copy(artist = value) else track
                },
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onTrackNumberChange(songId: Long, value: String) {
        updateDraft {
            it.copy(
                tracks = it.tracks.map { track ->
                    if (track.songId == songId) {
                        track.copy(trackNumber = value.filter(Char::isDigit))
                    } else {
                        track
                    }
                },
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onDiscNumberChange(songId: Long, value: String) {
        updateDraft {
            it.copy(
                tracks = it.tracks.map { track ->
                    if (track.songId == songId) {
                        track.copy(discNumber = value.filter(Char::isDigit))
                    } else {
                        track
                    }
                },
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun onPickedCoverArt(uri: Uri?) {
        updateDraft {
            it.copy(
                selectedArtworkUri = uri ?: it.selectedArtworkUri,
                selectedArtworkBytes = null,
                statusMessage = null,
                saveFailures = emptyList(),
            )
        }
    }

    fun requestSave() {
        val currentState = _uiState.value
        if (currentState.isSaving) return
        val request = currentState.toAlbumTagEditRequest() ?: return
        val pending = writePermissionState.begin(request) ?: return
        _uiState.value = currentState.copy(
            isSaving = true,
            statusMessage = null,
            saveFailures = emptyList(),
            platformAction = AlbumTagEditorPlatformAction.RequestWritePermission(
                operationId = pending.operationId,
                request = request,
                uris = request.mutatedUris(),
            ),
            saveOutcome = null,
        ).recalculateFlags()
    }

    fun consumePlatformAction(operationId: String) {
        if (_uiState.value.platformAction?.operationId != operationId) return
        _uiState.value = _uiState.value.copy(platformAction = null)
    }

    fun onWritePermissionResult(
        operationId: String,
        granted: Boolean,
    ) {
        if (!matchesPlatformActionResult(writePermissionState.pending(operationId)?.operationId, operationId)) return
        val pending = writePermissionState.consume(operationId) ?: return
        if (!granted) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                statusMessage = "Write access was not granted.",
                saveOutcome = null,
            ).recalculateFlags()
            return
        }
        viewModelScope.launch {
            performSave(pending.request, writeConsentGranted = true)
        }
    }

    /** A grouped MediaStore request is unavailable; the write itself must establish access. */
    fun onWritePermissionNotRequired(operationId: String) {
        if (!matchesPlatformActionResult(writePermissionState.pending(operationId)?.operationId, operationId)) return
        val pending = writePermissionState.consume(operationId) ?: return
        viewModelScope.launch {
            performSave(pending.request, writeConsentGranted = false)
        }
    }

    fun onSafWritePermissionResult(
        operationId: String,
        granted: Boolean,
        failureMessage: String = "Write access was not granted.",
    ) {
        val pending = writePermissionState.pending(operationId) ?: return
        if (!granted) {
            writePermissionState.consume(operationId)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                statusMessage = failureMessage,
                saveOutcome = null,
            ).recalculateFlags()
            return
        }
        _uiState.value = _uiState.value.copy(
            platformAction = AlbumTagEditorPlatformAction.RequestWritePermission(
                operationId = pending.operationId,
                request = pending.request,
                uris = pending.request.mutatedUris(),
            ),
            saveOutcome = null,
        )
    }

    fun onWritePreflightFailed(
        operationId: String,
        message: String,
    ) {
        if (writePermissionState.consume(operationId) == null) return
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            statusMessage = message,
            platformAction = null,
        ).recalculateFlags()
    }

    fun onWritePermissionLaunchFailed(operationId: String) {
        if (writePermissionState.consume(operationId) == null) return
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            statusMessage = "Android could not open the write-access request.",
            platformAction = null,
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
            tagEditorService.applyEdits(
                request = request,
                writeConsentGranted = writeConsentGranted,
            )
        }.onSuccess { result ->
            if (result.editedSongIds.isNotEmpty()) {
                libraryTagUpdates.applyVerifiedTagEdits(result.editedSongs)
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
                platformAction = null,
                saveOutcome = when {
                    failures.isEmpty() && result.permissionRequest == null && result.editedSongIds.isNotEmpty() ->
                        AlbumTagEditorSaveOutcome.Succeeded
                    result.editedSongIds.isNotEmpty() -> AlbumTagEditorSaveOutcome.PartiallySucceeded(failures)
                    else -> null
                },
            ).recalculateFlags()
            if (result.permissionRequest == null && failures.isEmpty() && result.editedSongIds.isNotEmpty()) {
                clearSavedDraft()
            }
            if (result.permissionRequest != null) {
                val retryRequest = request.retryForFailures(
                    failedSongIds = result.failures.map { it.songId }.toSet(),
                )
                val pending = writePermissionState.begin(retryRequest)
                if (pending == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        statusMessage = "A write-access request is already pending.",
                    ).recalculateFlags()
                    return@onSuccess
                }
                _uiState.value = _uiState.value.copy(
                    platformAction = AlbumTagEditorPlatformAction.RequestRecoverableWritePermission(
                        operationId = pending.operationId,
                        request = retryRequest,
                        intentSender = result.permissionRequest.intentSender,
                    ),
                )
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
                val pending = writePermissionState.begin(request)
                if (pending == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        statusMessage = "A write-access request is already pending.",
                    ).recalculateFlags()
                    return@onFailure
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = null,
                    platformAction = null,
                ).recalculateFlags()
                _uiState.value = _uiState.value.copy(
                    platformAction = AlbumTagEditorPlatformAction.RequestRecoverableWritePermission(
                        operationId = pending.operationId,
                        request = request,
                        intentSender = recoverableIntentSender,
                    ),
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = throwable.message ?: "Unable to save tags.",
                    platformAction = null,
                ).recalculateFlags()
            }
        }
    }

    private fun updateDraft(transform: (AlbumTagEditorUiState) -> AlbumTagEditorUiState) {
        val next = transform(_uiState.value).recalculateFlags()
        _uiState.value = next
        persistDraft(next)
    }

    private fun persistDraft(state: AlbumTagEditorUiState) {
        val targetAlbumId = state.albumId ?: return
        if (state.originalAlbum == null) return
        savedStateHandle[KEY_DRAFT_ALBUM_ID] = targetAlbumId
        savedStateHandle[KEY_ALBUM_TITLE] = state.albumTitle
        savedStateHandle[KEY_ALBUM_ARTIST] = state.albumArtist
        savedStateHandle[KEY_RELEASE_YEAR] = state.releaseYear
        savedStateHandle[KEY_GENRE] = state.genre
        savedStateHandle[KEY_YEAR_CLEARED] = state.yearClearedExplicitly
        savedStateHandle[KEY_ARTWORK_URI] = state.selectedArtworkUri?.toString()
    }

    private fun clearSavedDraft() {
        savedStateHandle.remove<Long>(KEY_DRAFT_ALBUM_ID)
        savedStateHandle.remove<String>(KEY_ALBUM_TITLE)
        savedStateHandle.remove<String>(KEY_ALBUM_ARTIST)
        savedStateHandle.remove<String>(KEY_RELEASE_YEAR)
        savedStateHandle.remove<String>(KEY_GENRE)
        savedStateHandle.remove<Boolean>(KEY_YEAR_CLEARED)
        savedStateHandle.remove<String>(KEY_ARTWORK_URI)
    }

    private fun restoreDraft(
        state: AlbumTagEditorUiState,
        handle: SavedStateHandle,
    ): AlbumTagEditorUiState {
        if (handle.get<Long>(KEY_DRAFT_ALBUM_ID) != state.albumId) return state
        return state.copy(
            albumTitle = handle.get<String>(KEY_ALBUM_TITLE) ?: state.albumTitle,
            albumArtist = handle.get<String>(KEY_ALBUM_ARTIST) ?: state.albumArtist,
            releaseYear = handle.get<String>(KEY_RELEASE_YEAR) ?: state.releaseYear,
            genre = handle.get<String>(KEY_GENRE) ?: state.genre,
            yearClearedExplicitly = handle.get<Boolean>(KEY_YEAR_CLEARED) ?: state.yearClearedExplicitly,
            selectedArtworkUri = handle.get<String>(KEY_ARTWORK_URI)
                ?.takeIf(String::isNotBlank)
                ?.let(Uri::parse)
                ?: state.selectedArtworkUri,
        ).recalculateFlags()
    }

    private companion object {
        const val KEY_ALBUM_ID = "album_tag_editor.album_id"
        const val KEY_DRAFT_ALBUM_ID = "album_tag_editor.draft_album_id"
        const val KEY_ALBUM_TITLE = "album_tag_editor.album_title"
        const val KEY_ALBUM_ARTIST = "album_tag_editor.album_artist"
        const val KEY_RELEASE_YEAR = "album_tag_editor.release_year"
        const val KEY_GENRE = "album_tag_editor.genre"
        const val KEY_YEAR_CLEARED = "album_tag_editor.year_cleared"
        const val KEY_ARTWORK_URI = "album_tag_editor.artwork_uri"
    }
}
