package elovaire.music.droidbeauty.app.ui.screens.tags

import android.net.Uri
import androidx.compose.runtime.Immutable
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.data.tags.TagFieldEdit
import elovaire.music.droidbeauty.app.domain.model.Album

@Immutable
internal data class AlbumTagEditorUiState(
    val albumId: Long? = null,
    val originalAlbum: Album? = null,
    val albumTitle: String = "",
    val albumArtist: String = "",
    val releaseYear: String = "",
    val genre: String = "",
    val yearClearedExplicitly: Boolean = false,
    val tracks: List<EditableTrackTagState> = emptyList(),
    val selectedArtworkUri: Uri? = null,
    val selectedArtworkBytes: ByteArray? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationErrors: List<String> = emptyList(),
    val saveFailures: List<TagEditFailureUi> = emptyList(),
    val statusMessage: String? = null,
)

@Immutable
internal data class EditableTrackTagState(
    val songId: Long,
    val uri: Uri,
    val originalTitle: String,
    val originalArtist: String,
    val originalAlbum: String,
    val title: String,
    val artist: String,
    val trackNumber: String,
    val discNumber: String,
    val durationMs: Long,
    val fileName: String,
)

@Immutable
internal data class TagEditFailureUi(
    val songId: Long,
    val fileName: String,
    val reason: String,
)

internal fun Album.toTagEditorUiState(): AlbumTagEditorUiState {
    val releaseYear = songs.firstNotNullOfOrNull { it.releaseYear }?.toString().orEmpty()
    val genre = songs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty()
    return AlbumTagEditorUiState(
        albumId = id,
        originalAlbum = this,
        albumTitle = title,
        albumArtist = artist,
        releaseYear = releaseYear,
        genre = genre,
        yearClearedExplicitly = false,
        tracks = songs.mapIndexed { index, song ->
            EditableTrackTagState(
                songId = song.id,
                uri = song.uri,
                originalTitle = song.title,
                originalArtist = song.artist,
                originalAlbum = song.album,
                title = song.title,
                artist = song.artist,
                trackNumber = (song.trackNumber.takeIf { it > 0 } ?: index + 1).toString(),
                discNumber = (song.discNumber.takeIf { it > 0 } ?: 1).toString(),
                durationMs = song.durationMs,
                fileName = song.fileName,
            )
        },
        selectedArtworkUri = artUri,
        selectedArtworkBytes = null,
        isLoading = false,
    ).recalculateFlags()
}

internal fun AlbumTagEditorUiState.toAlbumTagEditRequest(): AlbumTagEditRequest? {
    val album = originalAlbum ?: return null
    return AlbumTagEditRequest(
        album = album,
        albumTitle = albumTitle.toTextEdit(album.title),
        albumArtist = albumArtist.toTextEdit(album.artist),
        releaseYear = when {
            yearClearedExplicitly && releaseYear.isBlank() -> TagFieldEdit.Cleared
            releaseYear.trim() == album.songs.firstNotNullOfOrNull { it.releaseYear }?.toString().orEmpty() -> TagFieldEdit.Unchanged
            else -> releaseYear.toIntOrNull()?.let(TagFieldEdit<Int>::Value) ?: TagFieldEdit.Unchanged
        },
        genre = genre.toTextEdit(album.songs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty()),
        coverArtUri = selectedArtworkUri?.takeIf { selected ->
            selected.toString() != album.artUri?.toString()
        },
        coverArtBytes = selectedArtworkBytes,
        tracks = tracks.mapIndexedNotNull { index, track ->
            val original = album.songs.firstOrNull { it.id == track.songId } ?: return@mapIndexedNotNull null
            val resolvedTrackNumber = track.trackNumber.toIntOrNull()?.coerceAtLeast(1) ?: (index + 1)
            val resolvedDiscNumber = track.discNumber.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val originalTrackNumber = original.trackNumber.takeIf { it > 0 } ?: (index + 1)
            val originalDiscNumber = original.discNumber.takeIf { it > 0 } ?: 1
            val changed = track.title.trim() != original.title.trim() ||
                track.artist.trim() != original.artist.trim() ||
                resolvedTrackNumber != originalTrackNumber ||
                resolvedDiscNumber != originalDiscNumber
            if (!changed) return@mapIndexedNotNull null
            EditableAlbumTrack(
                songId = track.songId,
                title = track.title,
                artist = track.artist,
                trackNumber = resolvedTrackNumber,
                discNumber = resolvedDiscNumber,
                durationMs = track.durationMs,
            )
        },
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

private fun AlbumTagEditorUiState.computeValidationErrors(): List<String> {
    val errors = mutableListOf<String>()
    if (albumTitle.isBlank()) errors += "Album title cannot be empty."
    if (albumArtist.isBlank()) errors += "Album artist cannot be empty."
    if (releaseYear.isNotBlank() && releaseYear.toIntOrNull() !in 1..9999) {
        errors += "Release year must be between 1 and 9999."
    }
    tracks.forEach { track ->
        if (track.title.isBlank()) {
            errors += "${track.fileName}: track title cannot be empty."
        }
        if (track.artist.isBlank()) {
            errors += "${track.fileName}: track artist cannot be empty."
        }
    }
    return errors
}

private fun AlbumTagEditorUiState.computeHasUnsavedChanges(): Boolean {
    val album = originalAlbum ?: return false
    if (selectedArtworkBytes != null) return true
    if (selectedArtworkUri?.toString() != album.artUri?.toString()) return true
    if (albumTitle.trim() != album.title.trim()) return true
    if (albumArtist.trim() != album.artist.trim()) return true
    if (releaseYear.trim() != album.songs.firstNotNullOfOrNull { it.releaseYear }?.toString().orEmpty().trim()) return true
    if (genre.trim() != album.songs.firstOrNull { it.genre.isNotBlank() }?.genre.orEmpty().trim()) return true
    val originalTracks = album.songs.associateBy { it.id }
    return tracks.withIndex().any { (index, track) ->
        val original = originalTracks[track.songId] ?: return@any true
        val originalTrackNumber = original.trackNumber.takeIf { it > 0 } ?: (index + 1)
        val originalDiscNumber = original.discNumber.takeIf { it > 0 } ?: 1
        track.title.trim() != original.title.trim() ||
            track.artist.trim() != original.artist.trim() ||
            track.trackNumber.trim() != originalTrackNumber.toString() ||
            track.discNumber.trim() != originalDiscNumber.toString()
    }
}

internal fun AlbumTagEditorUiState.recalculateFlags(): AlbumTagEditorUiState {
    val errors = computeValidationErrors()
    val unsaved = computeHasUnsavedChanges()
    return copy(
        validationErrors = errors,
        hasUnsavedChanges = unsaved,
        canSave = !isLoading && !isSaving && errors.isEmpty() && unsaved && originalAlbum != null,
    )
}
