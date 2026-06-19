package elovaire.music.droidbeauty.app.ui.screens.tags

import android.net.Uri
import androidx.compose.runtime.Immutable
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagMatchSuggestion
import elovaire.music.droidbeauty.app.data.tags.EditableAlbumTrack
import elovaire.music.droidbeauty.app.domain.model.Album

@Immutable
internal data class AlbumTagEditorUiState(
    val albumId: Long? = null,
    val originalAlbum: Album? = null,
    val albumTitle: String = "",
    val albumArtist: String = "",
    val releaseYear: String = "",
    val tracks: List<EditableTrackTagState> = emptyList(),
    val selectedArtworkUri: Uri? = null,
    val selectedArtworkBytes: ByteArray? = null,
    val matchedRelease: AlbumTagMatchSuggestion? = null,
    val isLoading: Boolean = true,
    val isMatchingOnline: Boolean = false,
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
    val filePath: String?,
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
    return AlbumTagEditorUiState(
        albumId = id,
        originalAlbum = this,
        albumTitle = title,
        albumArtist = artist,
        releaseYear = releaseYear,
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
                filePath = null,
                fileName = song.fileName,
            )
        },
        selectedArtworkUri = artUri,
        selectedArtworkBytes = null,
        matchedRelease = null,
        isLoading = false,
    ).recalculateFlags()
}

internal fun AlbumTagEditorUiState.toAlbumTagEditRequest(): AlbumTagEditRequest? {
    val album = originalAlbum ?: return null
    return AlbumTagEditRequest(
        album = album,
        albumTitle = albumTitle,
        albumArtist = albumArtist,
        releaseYear = releaseYear.toIntOrNull(),
        coverArtUri = selectedArtworkUri,
        coverArtBytes = selectedArtworkBytes,
        tracks = tracks.mapIndexed { index, track ->
            EditableAlbumTrack(
                songId = track.songId,
                title = track.title,
                artist = track.artist,
                trackNumber = track.trackNumber.toIntOrNull()?.coerceAtLeast(1) ?: (index + 1),
                discNumber = track.discNumber.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            )
        },
    )
}

private fun AlbumTagEditorUiState.computeValidationErrors(): List<String> {
    val errors = mutableListOf<String>()
    if (albumTitle.isBlank()) errors += "Album title cannot be empty."
    if (albumArtist.isBlank()) errors += "Album artist cannot be empty."
    if (releaseYear.isNotBlank() && releaseYear.toIntOrNull() == null) {
        errors += "Release year must be numeric."
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
    val originalTracks = album.songs.associateBy { it.id }
    return tracks.any { track ->
        val original = originalTracks[track.songId] ?: return@any true
        track.title.trim() != original.title.trim() ||
            track.artist.trim() != original.artist.trim() ||
            track.trackNumber.trim() != original.trackNumber.coerceAtLeast(1).toString() ||
            track.discNumber.trim() != original.discNumber.coerceAtLeast(1).toString()
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
