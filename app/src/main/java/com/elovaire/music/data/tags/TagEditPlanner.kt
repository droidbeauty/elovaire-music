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
