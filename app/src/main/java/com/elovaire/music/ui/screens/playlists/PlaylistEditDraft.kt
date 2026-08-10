package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.domain.model.Playlist

internal data class PlaylistEditDraft(
    val playlistId: Long,
    val originalSongIds: List<Long>,
    val songIds: List<Long>,
    val markedForRemoval: Set<Long> = emptySet(),
) {
    val dirty: Boolean
        get() = songIds != originalSongIds || markedForRemoval.isNotEmpty()

    val finalSongIds: List<Long>
        get() = songIds.filterNot(markedForRemoval::contains)

    fun addSongs(ids: List<Long>): PlaylistEditDraft = copy(
        songIds = (songIds + ids).distinct(),
    )

    fun toggleRemoval(songId: Long): PlaylistEditDraft = copy(
        markedForRemoval = if (songId in markedForRemoval) {
            markedForRemoval - songId
        } else {
            markedForRemoval + songId
        },
    )

    fun removeMarked(): PlaylistEditDraft = copy(
        songIds = finalSongIds,
        markedForRemoval = emptySet(),
    )

    fun move(songId: Long, delta: Int): PlaylistEditDraft {
        val fromIndex = songIds.indexOf(songId)
        if (fromIndex < 0 || delta == 0) return this
        val targetIndex = (fromIndex + delta).coerceIn(0, songIds.lastIndex)
        if (targetIndex == fromIndex) return this
        return copy(
            songIds = songIds.toMutableList().apply {
                add(targetIndex, removeAt(fromIndex))
            },
        )
    }

    companion object {
        fun fromPersisted(playlist: Playlist): PlaylistEditDraft = PlaylistEditDraft(
            playlistId = playlist.id,
            originalSongIds = playlist.songIds,
            songIds = playlist.songIds,
        )
    }
}
