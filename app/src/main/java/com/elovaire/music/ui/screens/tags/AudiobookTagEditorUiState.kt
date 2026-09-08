package elovaire.music.droidbeauty.app.ui.screens.tags

import android.content.IntentSender
import android.net.Uri
import androidx.compose.runtime.Immutable
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagEditFailure
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Song

internal sealed interface AudiobookTagEditorPlatformAction {
    val operationId: String

    data class RequestWritePermission(
        override val operationId: String,
        val request: AudiobookTagEditRequest,
        val uris: List<Uri>,
    ) : AudiobookTagEditorPlatformAction

    data class RequestRecoverableWritePermission(
        override val operationId: String,
        val request: AudiobookTagEditRequest,
        val intentSender: IntentSender,
    ) : AudiobookTagEditorPlatformAction
}

internal sealed interface AudiobookTagEditorSaveOutcome {
    data class Succeeded(val stableKey: String) : AudiobookTagEditorSaveOutcome
    data class PartiallySucceeded(
        val failures: List<TagEditFailureUi>,
    ) : AudiobookTagEditorSaveOutcome
}

@Immutable
internal data class AudiobookTagEditorUiState(
    val bookKey: String? = null,
    val originalBook: Audiobook? = null,
    val bookTitle: String = "",
    val author: String = "",
    val releaseYear: String = "",
    val genre: String = "",
    val parts: List<EditableAudiobookPartState> = emptyList(),
    val selectedArtworkUri: Uri? = null,
    val selectedArtworkBytes: ByteArray? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationErrors: List<String> = emptyList(),
    val saveFailures: List<TagEditFailureUi> = emptyList(),
    val statusMessage: String? = null,
    val platformAction: AudiobookTagEditorPlatformAction? = null,
    val saveOutcome: AudiobookTagEditorSaveOutcome? = null,
)

@Immutable
internal data class EditableAudiobookPartState(
    val songId: Long,
    val uri: Uri,
    val originalTitle: String,
    val originalArtist: String,
    val originalTrackNumber: Int,
    val originalDiscNumber: Int,
    val title: String,
    val artist: String,
    val trackNumber: String,
    val discNumber: String,
    val durationMs: Long,
    val fileName: String,
)

internal fun Audiobook.toTagEditorUiState(): AudiobookTagEditorUiState {
    val songs = canonicalSongs()
    return AudiobookTagEditorUiState(
        bookKey = stableKey,
        originalBook = this,
        bookTitle = title,
        author = author,
        releaseYear = songs.firstNotNullOfOrNull { it.releaseYear }?.toString().orEmpty(),
        genre = songs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty(),
        parts = songs.mapIndexed { index, song ->
            EditableAudiobookPartState(
                songId = song.id,
                uri = song.uri,
                originalTitle = song.title,
                originalArtist = song.artist,
                originalTrackNumber = song.trackNumber.takeIf { it > 0 } ?: index + 1,
                originalDiscNumber = song.discNumber.takeIf { it > 0 } ?: 1,
                title = song.title,
                artist = song.artist,
                trackNumber = (song.trackNumber.takeIf { it > 0 } ?: index + 1).toString(),
                discNumber = (song.discNumber.takeIf { it > 0 } ?: 1).toString(),
                durationMs = song.durationMs,
                fileName = song.fileName,
            )
        },
        selectedArtworkUri = artUri,
        isLoading = false,
    ).recalculateFlags()
}

internal fun AudiobookTagEditorUiState.toAudiobookTagEditRequest(): AudiobookTagEditRequest? {
    val book = originalBook ?: return null
    val originalSongs = book.canonicalSongs().associateBy { it.id }
    val tracks = parts.mapNotNull { part ->
        val original = originalSongs[part.songId] ?: return@mapNotNull null
        val trackNumber = part.trackNumber.toIntOrNull()?.coerceAtLeast(1) ?: part.originalTrackNumber
        val discNumber = part.discNumber.toIntOrNull()?.coerceAtLeast(1) ?: part.originalDiscNumber
        if (part.title.trim() == original.title.trim() &&
            part.artist.trim() == original.artist.trim() &&
            trackNumber == part.originalTrackNumber &&
            discNumber == part.originalDiscNumber
        ) {
            return@mapNotNull null
        }
        EditableAlbumTrack(
            songId = part.songId,
            title = part.title,
            artist = part.artist,
            trackNumber = trackNumber,
            discNumber = discNumber,
            durationMs = part.durationMs,
        )
    }
    val originalYear = book.canonicalSongs().mapNotNull { it.releaseYear }.firstOrNull()?.toString().orEmpty()
    return AudiobookTagEditRequest(
        bookKey = book.stableKey,
        songs = book.canonicalSongs(),
        bookTitle = bookTitle.toTextEdit(book.title),
        author = author.toTextEdit(book.author),
        releaseYear = when {
            releaseYear.trim() == originalYear -> TagFieldEdit.Unchanged
            releaseYear.isBlank() -> TagFieldEdit.Cleared
            else -> releaseYear.toIntOrNull()?.let(TagFieldEdit<Int>::Value) ?: TagFieldEdit.Unchanged
        },
        genre = genre.toTextEdit(book.canonicalSongs().firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty()),
        coverArtUri = selectedArtworkUri?.takeIf { it.toString() != book.artUri?.toString() },
        coverArtBytes = selectedArtworkBytes,
        tracks = tracks,
    )
}

internal fun AudiobookTagEditorUiState.recalculateFlags(): AudiobookTagEditorUiState {
    val book = originalBook
    val errors = buildList {
        if (bookTitle.isBlank()) add("Book title cannot be empty.")
        if (author.isBlank()) add("Author cannot be empty.")
        if (releaseYear.isNotBlank() && releaseYear.toIntOrNull() !in 1..9999) {
            add("Release year must be between 1 and 9999.")
        }
        parts.forEach { part ->
            if (part.title.isBlank()) add("${part.fileName}: chapter title cannot be empty.")
            if (part.artist.isBlank()) add("${part.fileName}: author cannot be empty.")
        }
    }
    val originalSongs = book?.canonicalSongs().orEmpty()
    val originalYear = originalSongs.firstNotNullOfOrNull { it.releaseYear }?.toString().orEmpty()
    val originalGenre = originalSongs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty()
    val hasChanges = book != null && (
        selectedArtworkBytes != null ||
            selectedArtworkUri?.toString() != book.artUri?.toString() ||
            bookTitle.trim() != book.title.trim() ||
            author.trim() != book.author.trim() ||
            releaseYear.trim() != originalYear.trim() ||
            genre.trim() != originalGenre.trim() ||
            parts.any { part ->
                part.title.trim() != part.originalTitle.trim() ||
                    part.artist.trim() != part.originalArtist.trim() ||
                    part.trackNumber.toIntOrNull()?.coerceAtLeast(1) != part.originalTrackNumber ||
                    part.discNumber.toIntOrNull()?.coerceAtLeast(1) != part.originalDiscNumber
            }
        )
    return copy(
        validationErrors = errors,
        hasUnsavedChanges = hasChanges,
        canSave = !isLoading && !isSaving && errors.isEmpty() && hasChanges,
    )
}

private fun String.toTextEdit(original: String): TagFieldEdit<String> {
    val normalized = trim()
    return when {
        normalized == original.trim() -> TagFieldEdit.Unchanged
        normalized.isBlank() -> TagFieldEdit.Cleared
        else -> TagFieldEdit.Value(normalized)
    }
}

private fun Audiobook.canonicalSongs(): List<Song> = parts
    .map { it.song }
    .distinctBy { song -> song.uri.toString().ifBlank { "id:${song.id}" } }
