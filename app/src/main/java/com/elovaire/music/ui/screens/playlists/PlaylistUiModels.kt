package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class PlaylistRowModel(
    val playlist: Playlist,
    val previewSongs: List<Song>,
)

internal data class PlaylistPickerRowModel(
    val playlist: Playlist,
    val previewSongs: List<Song>,
    val durationMs: Long,
    val index: Int,
)

internal data class PlaylistPreview(
    val songs: List<Song>,
    val durationMs: Long,
)

internal data class PlaylistDetailState(
    val playlist: Playlist?,
    val songs: List<Song>,
    val missingSongIds: List<Long>,
    val durationMs: Long,
)

internal fun buildPlaylistRowModels(
    playlists: List<Playlist>,
    songsById: Map<Long, Song>,
): List<PlaylistRowModel> {
    return playlists.map { playlist ->
        PlaylistRowModel(
            playlist = playlist,
            previewSongs = buildPlaylistPreview(playlist.songIds, songsById).songs,
        )
    }
}

internal fun buildPlaylistPickerRowModels(
    playlists: List<Playlist>,
    songsById: Map<Long, Song>,
): List<PlaylistPickerRowModel> {
    return playlists.mapIndexed { index, playlist ->
        val preview = buildPlaylistPreview(playlist.songIds, songsById)
        PlaylistPickerRowModel(
            playlist = playlist,
            previewSongs = preview.songs,
            durationMs = preview.durationMs,
            index = index,
        )
    }
}

internal fun buildPlaylistPreview(
    songIds: List<Long>,
    songsById: Map<Long, Song>,
): PlaylistPreview {
    val previewSongs = ArrayList<Song>(PREVIEW_SONG_LIMIT)
    val usedAlbumIds = HashSet<Long>(PREVIEW_SONG_LIMIT)
    var durationMs = 0L
    songIds.forEach { songId ->
        val song = songsById[songId] ?: return@forEach
        durationMs += song.durationMs
        if (previewSongs.size < PREVIEW_SONG_LIMIT && usedAlbumIds.add(song.albumId)) {
            previewSongs += song
        }
    }
    return PlaylistPreview(previewSongs, durationMs)
}

internal fun buildPlaylistDetailState(
    playlist: Playlist?,
    songsById: Map<Long, Song>,
): PlaylistDetailState {
    if (playlist == null) {
        return PlaylistDetailState(
            playlist = null,
            songs = emptyList(),
            missingSongIds = emptyList(),
            durationMs = 0L,
        )
    }

    val songs = ArrayList<Song>(playlist.songIds.size)
    val missingSongIds = ArrayList<Long>()
    playlist.songIds.forEach { songId ->
        val song = songsById[songId]
        if (song != null) {
            songs += song
        } else {
            missingSongIds += songId
        }
    }
    return PlaylistDetailState(
        playlist = playlist,
        songs = songs,
        missingSongIds = missingSongIds,
        durationMs = songs.sumOf(Song::durationMs),
    )
}

internal fun playlistCollageSongs(songs: List<Song>): List<Song> {
    val usedAlbumIds = HashSet<Long>(PREVIEW_SONG_LIMIT)
    return buildList(PREVIEW_SONG_LIMIT) {
        for (song in songs) {
            if (usedAlbumIds.add(song.albumId)) add(song)
            if (size == PREVIEW_SONG_LIMIT) break
        }
    }
}

private const val PREVIEW_SONG_LIMIT = 4

internal fun formatPlaylistDuration(durationMs: Long): String {
    val hours = durationMs / 3_600_000
    val minutes = (durationMs % 3_600_000) / 60_000
    return when {
        hours > 0L -> buildString {
            append(hours)
            append('h')
            if (minutes > 0L) {
                append(' ')
                append(minutes)
                append('m')
            }
        }
        else -> "${minutes}m"
    }
}
