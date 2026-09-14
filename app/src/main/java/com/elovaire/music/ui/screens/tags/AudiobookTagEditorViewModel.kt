package elovaire.music.droidbeauty.app.ui.screens.tags

import android.app.RecoverableSecurityException
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.core.UuidOperationIdGenerator
import elovaire.music.droidbeauty.app.data.library.AudiobookCatalog
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditor
import elovaire.music.droidbeauty.app.data.tags.TagEditFailure
import elovaire.music.droidbeauty.app.data.tags.retryForFailures
import elovaire.music.droidbeauty.app.data.tags.mutatedUris
import elovaire.music.droidbeauty.app.platform.matchesPlatformActionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AudiobookTagEditorViewModel(
    private val libraryRepository: LibraryReader,
    private val libraryTagUpdates: LibraryTagUpdateWriter,
    private val tagEditorService: AudiobookTagEditor,
    private val remapProgressKey: suspend (String, String) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val operationIdGenerator: OperationIdGenerator = UuidOperationIdGenerator,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val bookKey = MutableStateFlow<String?>(savedStateHandle[KEY_BOOK_KEY])
    private val _uiState = MutableStateFlow(AudiobookTagEditorUiState())
    val uiState: StateFlow<AudiobookTagEditorUiState> = _uiState.asStateFlow()
    private val writePermissionState = AudiobookTagWritePermissionState(operationIdGenerator)

    init {
        viewModelScope.launch {
            combine(bookKey, libraryRepository.contentState) { targetKey, content ->
                targetKey?.let { key -> content.audiobooks.firstOrNull { it.stableKey == key } }
            }.collectLatest { book ->
                if (book == null) {
                    _uiState.value = _uiState.value.copy(originalBook = null, isLoading = false).recalculateFlags()
                    return@collectLatest
                }
                val current = _uiState.value
                if (current.bookKey == book.stableKey && current.hasUnsavedChanges && current.originalBook != null) return@collectLatest
                _uiState.value = restoreDraft(book.toTagEditorUiState())
            }
        }
    }

    fun loadBook(targetBookKey: String) {
        if (_uiState.value.bookKey == targetBookKey && _uiState.value.originalBook != null) return
        if (savedStateHandle.get<String>(KEY_DRAFT_BOOK_KEY) != targetBookKey) clearSavedDraft()
        savedStateHandle[KEY_BOOK_KEY] = targetBookKey
        bookKey.value = targetBookKey
        _uiState.value = AudiobookTagEditorUiState(bookKey = targetBookKey, isLoading = true)
    }

    fun clearBook() {
        savedStateHandle.remove<String>(KEY_BOOK_KEY)
        clearSavedDraft()
        bookKey.value = null
        _uiState.value = AudiobookTagEditorUiState(isLoading = false)
    }

    fun onBookTitleChange(value: String) = updateDraft { it.copy(bookTitle = value, statusMessage = null, saveFailures = emptyList()) }
    fun onAuthorChange(value: String) = updateDraft { it.copy(author = value, statusMessage = null, saveFailures = emptyList()) }
    fun onDescriptionChange(value: String) = updateDraft { it.copy(description = value, statusMessage = null, saveFailures = emptyList()) }
    fun onReleaseYearChange(value: String) = updateDraft {
        it.copy(releaseYear = value.filter(Char::isDigit).take(4), statusMessage = null, saveFailures = emptyList())
    }
    fun onGenreChange(value: String) = updateDraft { it.copy(genre = value, statusMessage = null, saveFailures = emptyList()) }

    fun onPartTitleChange(songId: Long, value: String) = updatePart(songId) { it.copy(title = value) }
    fun onPartArtistChange(songId: Long, value: String) = updatePart(songId) { it.copy(artist = value) }
    fun onPartTrackNumberChange(songId: Long, value: String) = updatePart(songId) { it.copy(trackNumber = value.filter(Char::isDigit)) }
    fun onPartDiscNumberChange(songId: Long, value: String) = updatePart(songId) { it.copy(discNumber = value.filter(Char::isDigit)) }

    fun onPickedCoverArt(uri: Uri?) {
        if (uri == null) return
        updateDraft { it.copy(selectedArtworkUri = uri, selectedArtworkBytes = null, statusMessage = null) }
    }

    fun requestSave() {
        val current = _uiState.value
        if (current.isSaving || !current.canSave) return
        val request = current.toAudiobookTagEditRequest() ?: return
        val pending = writePermissionState.begin(request) ?: return
        _uiState.value = current.copy(
            isSaving = true,
            statusMessage = null,
            saveFailures = emptyList(),
            saveOutcome = null,
            platformAction = AudiobookTagEditorPlatformAction.RequestWritePermission(
                operationId = pending.operationId,
                request = request,
                uris = request.mutatedUris(),
            ),
        ).recalculateFlags()
    }

    fun consumePlatformAction(operationId: String) {
        if (_uiState.value.platformAction?.operationId == operationId) {
            _uiState.value = _uiState.value.copy(platformAction = null)
        }
    }

    fun onWritePermissionResult(operationId: String, granted: Boolean) {
        if (!matchesPlatformActionResult(writePermissionState.pending(operationId)?.operationId, operationId)) return
        val pending = writePermissionState.consume(operationId) ?: return
        if (!granted) {
            finishFailure("Write access was not granted.")
            return
        }
        viewModelScope.launch { performSave(pending.request, true) }
    }

    fun onWritePermissionNotRequired(operationId: String) {
        if (!matchesPlatformActionResult(writePermissionState.pending(operationId)?.operationId, operationId)) return
        val pending = writePermissionState.consume(operationId) ?: return
        viewModelScope.launch { performSave(pending.request, false) }
    }

    fun onSafWritePermissionResult(operationId: String, granted: Boolean, failureMessage: String) {
        val pending = writePermissionState.pending(operationId) ?: return
        if (!granted) {
            writePermissionState.consume(operationId)
            finishFailure(failureMessage)
            return
        }
        _uiState.value = _uiState.value.copy(
            platformAction = AudiobookTagEditorPlatformAction.RequestWritePermission(
                operationId = pending.operationId,
                request = pending.request,
                uris = pending.request.mutatedUris(),
            ),
        )
    }

    fun onWritePreflightFailed(operationId: String, message: String) {
        if (writePermissionState.consume(operationId) != null) finishFailure(message)
    }

    fun onWritePermissionLaunchFailed(operationId: String) {
        if (writePermissionState.consume(operationId) != null) finishFailure("Android could not open the write-access request.")
    }

    private suspend fun performSave(request: AudiobookTagEditRequest, writeConsentGranted: Boolean) {
        _uiState.value = _uiState.value.copy(isSaving = true, statusMessage = null, saveFailures = emptyList())
            .recalculateFlags()
        runCatching { tagEditorService.applyEdits(request, writeConsentGranted) }
            .onSuccess { result ->
                if (result.editedSongs.isNotEmpty()) libraryTagUpdates.applyVerifiedTagEdits(result.editedSongs)
                val failures = result.failures.map { it.toUiFailure() }
                val savedKey = request.bookKey
                    .let { oldKey ->
                        libraryRepository.contentState.value.songs.firstOrNull { it.id in result.editedSongIds }
                            ?.let { song -> AudiobookCatalog.findContaining(libraryRepository.contentState.value.songs, song.id)?.stableKey }
                            ?: oldKey
                }
                if (result.permissionRequest == null && failures.isEmpty() && result.editedSongs.isNotEmpty() && savedKey != request.bookKey) {
                    withContext(ioDispatcher) {
                        remapProgressKey(request.bookKey, savedKey)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = result.permissionRequest != null,
                    statusMessage = when {
                        result.permissionRequest != null -> "Additional write access is needed to finish saving."
                        failures.isEmpty() -> null
                        result.editedSongs.isNotEmpty() -> "Saved with ${failures.size} issue(s)."
                        else -> failures.firstOrNull()?.reason ?: "No tags were saved."
                    },
                    saveFailures = failures,
                    platformAction = null,
                    saveOutcome = when {
                        failures.isEmpty() && result.permissionRequest == null && result.editedSongs.isNotEmpty() ->
                            AudiobookTagEditorSaveOutcome.Succeeded(savedKey)
                        result.editedSongs.isNotEmpty() -> AudiobookTagEditorSaveOutcome.PartiallySucceeded(failures)
                        else -> null
                    },
                ).recalculateFlags()
                if (result.permissionRequest != null) {
                    val retryRequest = request.retryForFailures(result.failures.map { it.songId }.toSet())
                    val pending = writePermissionState.begin(retryRequest)
                    if (pending != null) {
                        _uiState.value = _uiState.value.copy(
                            platformAction = AudiobookTagEditorPlatformAction.RequestRecoverableWritePermission(
                                operationId = pending.operationId,
                                request = retryRequest,
                                intentSender = result.permissionRequest.intentSender,
                            ),
                        )
                    }
                } else if (failures.isEmpty() && result.editedSongs.isNotEmpty()) {
                    clearSavedDraft()
                }
            }
            .onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                val intentSender = (throwable as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
                if (intentSender != null) {
                    val pending = writePermissionState.begin(request)
                    if (pending != null) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            platformAction = AudiobookTagEditorPlatformAction.RequestRecoverableWritePermission(
                                operationId = pending.operationId,
                                request = request,
                                intentSender = intentSender,
                            ),
                        ).recalculateFlags()
                    }
                } else {
                    finishFailure(throwable.message ?: "Unable to save audiobook tags.")
                }
            }
    }

    private fun updatePart(songId: Long, transform: (EditableAudiobookPartState) -> EditableAudiobookPartState) {
        updateDraft { state -> state.copy(parts = state.parts.map { if (it.songId == songId) transform(it) else it }, statusMessage = null, saveFailures = emptyList()) }
    }

    private fun updateDraft(transform: (AudiobookTagEditorUiState) -> AudiobookTagEditorUiState) {
        val next = transform(_uiState.value).recalculateFlags()
        _uiState.value = next
        persistDraft(next)
    }

    private fun persistDraft(state: AudiobookTagEditorUiState) {
        val key = state.bookKey ?: return
        if (state.originalBook == null) return
        savedStateHandle[KEY_DRAFT_BOOK_KEY] = key
        savedStateHandle[KEY_TITLE] = state.bookTitle
        savedStateHandle[KEY_AUTHOR] = state.author
        savedStateHandle[KEY_DESCRIPTION] = state.description
        savedStateHandle[KEY_YEAR] = state.releaseYear
        savedStateHandle[KEY_GENRE] = state.genre
        savedStateHandle[KEY_ARTWORK] = state.selectedArtworkUri?.toString()
        val originalById = state.originalBook.parts.map { it.song }.associateBy { it.id }
        val parts = state.parts.mapIndexedNotNull { index, part ->
            val original = originalById[part.songId] ?: return@mapIndexedNotNull null
            if (part.title == original.title && part.artist == original.artist &&
                part.trackNumber == (original.trackNumber.takeIf { it > 0 } ?: index + 1).toString() &&
                part.discNumber == (original.discNumber.takeIf { it > 0 } ?: 1).toString()
            ) return@mapIndexedNotNull null
            listOf(part.songId.toString(), part.title, part.artist, part.trackNumber, part.discNumber)
                .joinToString("|", transform = ::escapeDraftValue)
        }.joinToString("\n")
        savedStateHandle[KEY_PARTS] = parts
    }

    private fun restoreDraft(state: AudiobookTagEditorUiState): AudiobookTagEditorUiState {
        if (savedStateHandle.get<String>(KEY_DRAFT_BOOK_KEY) != state.bookKey) return state
        val partDrafts = savedStateHandle.get<String>(KEY_PARTS).orEmpty()
            .lineSequence()
            .mapNotNull { line ->
                val values = line.split('|').map(::unescapeDraftValue)
                if (values.size != 5) return@mapNotNull null
                values[0].toLongOrNull()?.let { id -> id to values.drop(1) }
            }.toMap()
        val restoredParts = state.parts.map { part ->
            val draft = partDrafts[part.songId] ?: return@map part
            part.copy(
                title = draft[0],
                artist = draft[1],
                trackNumber = draft[2],
                discNumber = draft[3],
            )
        }
        return state.copy(
            bookTitle = savedStateHandle.get<String>(KEY_TITLE) ?: state.bookTitle,
            author = savedStateHandle.get<String>(KEY_AUTHOR) ?: state.author,
            description = savedStateHandle.get<String>(KEY_DESCRIPTION) ?: state.description,
            releaseYear = savedStateHandle.get<String>(KEY_YEAR) ?: state.releaseYear,
            genre = savedStateHandle.get<String>(KEY_GENRE) ?: state.genre,
            selectedArtworkUri = savedStateHandle.get<String>(KEY_ARTWORK)?.takeIf(String::isNotBlank)?.let(Uri::parse)
                ?: state.selectedArtworkUri,
            parts = restoredParts,
        ).recalculateFlags()
    }

    private fun clearSavedDraft() {
        savedStateHandle.remove<String>(KEY_DRAFT_BOOK_KEY)
        savedStateHandle.remove<String>(KEY_TITLE)
        savedStateHandle.remove<String>(KEY_AUTHOR)
        savedStateHandle.remove<String>(KEY_DESCRIPTION)
        savedStateHandle.remove<String>(KEY_YEAR)
        savedStateHandle.remove<String>(KEY_GENRE)
        savedStateHandle.remove<String>(KEY_ARTWORK)
        savedStateHandle.remove<String>(KEY_PARTS)
    }

    private fun finishFailure(message: String) {
        _uiState.value = _uiState.value.copy(isSaving = false, statusMessage = message, platformAction = null, saveOutcome = null)
            .recalculateFlags()
    }

    private fun escapeDraftValue(value: String): String = value
        .replace("%", "%25")
        .replace("|", "%7C")
        .replace("\n", "%0A")

    private fun unescapeDraftValue(value: String): String = value
        .replace("%0A", "\n")
        .replace("%7C", "|")
        .replace("%25", "%")

    private fun TagEditFailure.toUiFailure() = TagEditFailureUi(songId, fileName, reason)

    private companion object {
        const val KEY_BOOK_KEY = "audiobook_tag_editor.book_key"
        const val KEY_DRAFT_BOOK_KEY = "audiobook_tag_editor.draft_book_key"
        const val KEY_TITLE = "audiobook_tag_editor.title"
        const val KEY_AUTHOR = "audiobook_tag_editor.author"
        const val KEY_DESCRIPTION = "audiobook_tag_editor.description"
        const val KEY_YEAR = "audiobook_tag_editor.year"
        const val KEY_GENRE = "audiobook_tag_editor.genre"
        const val KEY_ARTWORK = "audiobook_tag_editor.artwork"
        const val KEY_PARTS = "audiobook_tag_editor.parts"
    }
}

private data class PendingAudiobookTagWrite(
    val operationId: String,
    val request: AudiobookTagEditRequest,
)

private class AudiobookTagWritePermissionState(
    private val operationIdGenerator: OperationIdGenerator,
) {
    private var pending: PendingAudiobookTagWrite? = null

    fun begin(request: AudiobookTagEditRequest): PendingAudiobookTagWrite? {
        if (pending != null) return null
        val next = PendingAudiobookTagWrite(operationIdGenerator.nextId(), request)
        pending = next
        return next
    }

    fun pending(operationId: String): PendingAudiobookTagWrite? = pending?.takeIf { it.operationId == operationId }
    fun consume(operationId: String): PendingAudiobookTagWrite? = pending(operationId)?.also { pending = null }
}
