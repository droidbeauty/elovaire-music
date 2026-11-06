package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.ui.i18n.*

import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.sumDurationMs

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
        durationMs = songs.sumDurationMs(),
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

internal fun playlistArtworkPreviewSongs(
    songIds: List<Long>,
    songsById: Map<Long, Song>,
): List<Song> {
    val usedAlbumIds = HashSet<Long>(PREVIEW_SONG_LIMIT)
    return buildList(PREVIEW_SONG_LIMIT) {
        for (songId in songIds) {
            val song = songsById[songId] ?: continue
            if (usedAlbumIds.add(song.albumId)) add(song)
            if (size == PREVIEW_SONG_LIMIT) break
        }
    }
}

private const val PREVIEW_SONG_LIMIT = 4

internal fun formatPlaylistDuration(durationMs: Long, language: AppLanguage): String {
    val hours = durationMs / 3_600_000
    val minutes = (durationMs % 3_600_000) / 60_000
    val (hourUnit, minuteUnit) = when (language) {
        AppLanguage.Bengali -> BengaliTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to BengaliTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Bulgarian, AppLanguage.Macedonian, AppLanguage.Russian, AppLanguage.Serbian -> "ч" to "мин"
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to ChineseSimplifiedTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Finnish -> FinnishTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to FinnishTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Greek -> GreekTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to GreekTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Hindi -> HindiTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to HindiTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Japanese -> JapaneseTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to JapaneseTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Korean -> KoreanTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to KoreanTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Lithuanian -> LithuanianTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to LithuanianTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Polish -> PolishTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to PolishTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Punjabi -> PunjabiTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to PunjabiTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Thai -> ThaiTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to ThaiTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Ukrainian -> UkrainianTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to UkrainianTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        AppLanguage.Urdu -> (if (hours == 1L) UrduTranslations.playlistUiModels_formatPlaylistDuration_variant_1 else UrduTranslations.playlistUiModels_formatPlaylistDuration_variant_2) to UrduTranslations.playlistUiModels_formatPlaylistDuration_variant_3
        AppLanguage.English -> EnglishTranslations.playlistUiModels_formatPlaylistDuration_variant_1 to EnglishTranslations.playlistUiModels_formatPlaylistDuration_variant_2
        else -> "h" to "min"
    }
    val unitSeparator = when (language) {
        AppLanguage.English, AppLanguage.ChineseSimplified, AppLanguage.Japanese, AppLanguage.Korean -> ""
        else -> " "
    }
    return when {
        hours > 0L -> buildString {
            append(hours)
            append(unitSeparator)
            append(hourUnit)
            if (minutes > 0L) {
                append(' ')
                append(minutes)
                append(unitSeparator)
                append(minuteUnit)
            }
        }
        else -> "$minutes$unitSeparator$minuteUnit"
    }
}
