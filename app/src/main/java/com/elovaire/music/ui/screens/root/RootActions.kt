package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.localizedAllSongsSource

internal class RootPlaybackActions internal constructor(
    private val container: AppContainer,
    private val languageProvider: () -> AppLanguage,
    private val songsByAlbumIdProvider: () -> Map<Long, List<Song>>,
    private val albumsByIdProvider: () -> Map<Long, Album>,
    private val openNowPlaying: (NowPlayingTransitionSnapshot?) -> Unit,
) {
    fun playAlbum(
        album: Album,
        shuffle: Boolean = false,
        openPlayer: Boolean = false,
    ) {
        container.playbackManager.playAlbum(album, shuffleEnabled = shuffle)
        if (openPlayer) {
            openNowPlaying(null)
        }
    }

    fun playPlaylist(
        playlist: Playlist,
        songs: List<Song>,
        shuffle: Boolean = false,
    ) {
        val queue = if (shuffle) songs.shuffled() else songs
        val firstSong = queue.firstOrNull() ?: return
        container.playbackManager.playSong(
            song = firstSong,
            collection = queue,
            sourceLabel = playlist.name,
            sourcePlaylistId = playlist.id,
        )
    }

    fun playSongFromAlbumOrSingle(song: Song) {
        val album = albumsByIdProvider()[song.albumId]
        if (album != null) {
            container.playbackManager.playAlbum(
                album = album,
                startSongId = song.id,
                sourceLabel = album.title,
            )
        } else {
            val albumSongs = songsByAlbumIdProvider()[song.albumId].orEmpty()
            container.playbackManager.playSong(
                song = song,
                collection = albumSongs.ifEmpty { listOf(song) },
                sourceLabel = song.album,
            )
        }
    }

    fun playSongQueue(
        song: Song,
        queue: List<Song>,
        sourceLabel: String? = null,
        sourcePlaylistId: Long? = null,
    ) {
        container.playbackManager.playSong(
            song = song,
            collection = queue,
            sourceLabel = sourceLabel ?: queue.playbackSourceLabel(
                fallbackAlbum = song.album,
                language = languageProvider(),
            ),
            sourcePlaylistId = sourcePlaylistId,
        )
    }

    fun playAllSongs(
        song: Song,
        queue: List<Song>,
    ) {
        container.playbackManager.playSong(
            song = song,
            collection = queue,
            sourceLabel = localizedAllSongsSource(languageProvider()),
        )
    }
}

internal class RootPlaylistActions internal constructor(
    private val container: AppContainer,
) {
    fun createPlaylist(name: String): Long = container.preferenceStore.createPlaylist(name)

    fun createPlaylistAndAddSongs(
        name: String,
        songIds: List<Long>,
    ): Long {
        val createdId = createPlaylist(name)
        if (createdId > 0L && songIds.isNotEmpty()) {
            container.preferenceStore.addSongsToPlaylist(createdId, songIds)
        }
        return createdId
    }

    fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        container.preferenceStore.addSongsToPlaylist(playlistId, songIds)
    }

    fun addAlbumToPlaylist(
        playlistId: Long,
        album: Album,
    ) {
        addSongsToPlaylist(playlistId, album.songs.map(Song::id))
    }

    fun setSongsFavorite(
        songIds: List<Long>,
        favorite: Boolean,
    ) {
        container.preferenceStore.setFavoriteSongs(songIds, favorite)
    }

    fun toggleFavorite(songId: Long) {
        container.preferenceStore.toggleFavoriteSong(songId)
    }
}

@Composable
internal fun rememberRootPlaybackActions(
    container: AppContainer,
    appLanguage: AppLanguage,
    songsByAlbumId: Map<Long, List<Song>>,
    albumsById: Map<Long, Album>,
    openNowPlaying: (NowPlayingTransitionSnapshot?) -> Unit,
): RootPlaybackActions {
    return remember(container, appLanguage, songsByAlbumId, albumsById, openNowPlaying) {
        RootPlaybackActions(
            container = container,
            languageProvider = { appLanguage },
            songsByAlbumIdProvider = { songsByAlbumId },
            albumsByIdProvider = { albumsById },
            openNowPlaying = openNowPlaying,
        )
    }
}

@Composable
internal fun rememberRootPlaylistActions(container: AppContainer): RootPlaylistActions {
    return remember(container) { RootPlaylistActions(container) }
}

internal fun List<Song>.playbackSourceLabel(
    fallbackAlbum: String,
    language: AppLanguage,
): String {
    val distinctAlbums = asSequence().map { it.album }.filter { it.isNotBlank() }.distinct().toList()
    return when {
        distinctAlbums.size == 1 -> distinctAlbums.first()
        else -> localizedAllSongsSource(language)
    }.ifBlank { fallbackAlbum }
}
