package elovaire.music.droidbeauty.app.data.tags

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class TagEditPlan(
    val song: Song,
    val trackEdit: EditableAlbumTrack?,
    val hasAlbumLevelChanges: Boolean,
    val artworkChanged: Boolean,
) {
    val requiresMutation: Boolean = trackEdit != null || hasAlbumLevelChanges || artworkChanged
}

internal object TagEditPlanner {
    fun validationFailure(request: AlbumTagEditRequest): TagEditValidationFailure? {
        val albumSongIds = request.album.songs.asSequence().map(Song::id).toHashSet()
        val trackIds = request.tracks.map(EditableAlbumTrack::songId)
        if (trackIds.size != trackIds.toSet().size || trackIds.any { it !in albumSongIds }) {
            return TagEditValidationFailure.InvalidTrackSelection
        }
        if (request.tracks.any { it.trackNumber !in VALID_TAG_NUMBER_RANGE || it.discNumber !in VALID_TAG_NUMBER_RANGE }) {
            return TagEditValidationFailure.InvalidTrackNumber
        }
        if (request.textValues().any { it.length > MAX_TAG_TEXT_LENGTH }) {
            return TagEditValidationFailure.TextTooLong
        }
        if (request.coverArtBytes?.size?.let { it > MAX_TAG_ARTWORK_BYTES } == true) {
            return TagEditValidationFailure.ArtworkTooLarge
        }
        return null
    }

    fun plansFor(request: AlbumTagEditRequest): List<TagEditPlan> {
        val trackEditsById = request.tracks.associateBy(EditableAlbumTrack::songId)
        val hasAlbumLevelChanges = request.hasAlbumLevelChanges()
        val artworkChanged = request.coverArtUri != null || request.coverArtBytes != null
        return request.album.songs.mapNotNull { song ->
            TagEditPlan(
                song = song,
                trackEdit = trackEditsById[song.id],
                hasAlbumLevelChanges = hasAlbumLevelChanges,
                artworkChanged = artworkChanged,
            ).takeIf(TagEditPlan::requiresMutation)
        }
    }
}

internal enum class TagEditValidationFailure {
    InvalidTrackSelection,
    InvalidTrackNumber,
    TextTooLong,
    ArtworkTooLarge,
}

internal fun AlbumTagEditRequest.mutatedUris(): List<Uri> {
    return TagEditPlanner.plansFor(this).map { it.song.uri }
}

internal fun AlbumTagEditRequest.retryForFailures(
    failedSongIds: Set<Long>,
): AlbumTagEditRequest {
    return copy(
        album = album.copy(songs = album.songs.filter { it.id in failedSongIds }),
        tracks = tracks.filter { it.songId in failedSongIds },
    )
}

private fun AlbumTagEditRequest.hasAlbumLevelChanges(): Boolean {
    return albumTitle !is TagFieldEdit.Unchanged ||
        albumArtist !is TagFieldEdit.Unchanged ||
        releaseYear !is TagFieldEdit.Unchanged ||
        genre !is TagFieldEdit.Unchanged
}

private fun AlbumTagEditRequest.textValues(): Sequence<String> = sequence {
    tracks.forEach {
        yield(it.title)
        yield(it.artist)
    }
    listOf(albumTitle, albumArtist, genre).forEach { edit ->
        (edit as? TagFieldEdit.Value)?.value?.let { yield(it) }
    }
}

internal const val MAX_TAG_ARTWORK_BYTES = 16 * 1024 * 1024
private const val MAX_TAG_TEXT_LENGTH = 4_096
private val VALID_TAG_NUMBER_RANGE = 1..9_999
