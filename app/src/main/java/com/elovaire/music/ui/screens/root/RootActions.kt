package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.core.PlaybackActionDependencies
import elovaire.music.droidbeauty.app.core.PlaylistActionDependencies
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.localizedAllSongsSource

internal class RootPlaybackActions internal constructor(
    private val playbackManager: PlaybackManager,
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
        playbackManager.playAlbum(album, shuffleEnabled = shuffle)
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
        playbackManager.playSong(
            song = firstSong,
            collection = queue,
            sourceLabel = playlist.name,
            sourcePlaylistId = playlist.id,
        )
    }

    fun playSongFromAlbumOrSingle(song: Song) {
        val album = albumsByIdProvider()[song.albumId]
        if (album != null) {
            playbackManager.playAlbum(
                album = album,
                startSongId = song.id,
                sourceLabel = album.title,
            )
        } else {
            val albumSongs = songsByAlbumIdProvider()[song.albumId].orEmpty()
            playbackManager.playSong(
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
        playbackManager.playSong(
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
        playbackManager.playSong(
            song = song,
            collection = queue,
            sourceLabel = localizedAllSongsSource(languageProvider()),
        )
    }

    fun enqueueAlbum(album: Album) {
        album.songs.forEach(playbackManager::enqueueSong)
    }
}

internal class RootPlaylistActions internal constructor(
    private val dependencies: PlaylistActionDependencies,
) {
    fun createPlaylist(name: String): Long = dependencies.playlistStore.createPlaylist(name)

    fun createPlaylistAndAddSongs(
        name: String,
        songIds: List<Long>,
    ): Long {
        val createdId = createPlaylist(name)
        if (createdId > 0L && songIds.isNotEmpty()) {
            dependencies.playlistStore.addSongsToPlaylist(createdId, songIds)
        }
        return createdId
    }

    fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        dependencies.playlistStore.addSongsToPlaylist(playlistId, songIds)
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
        dependencies.favoritesStore.setFavoriteSongs(songIds, favorite)
    }

    fun toggleFavorite(songId: Long) {
        dependencies.favoritesStore.toggleFavoriteSong(songId)
    }
}

@Composable
internal fun rememberRootPlaybackActions(
    dependencies: PlaybackActionDependencies,
    playbackManager: PlaybackManager,
    appLanguage: AppLanguage,
    songsByAlbumId: Map<Long, List<Song>>,
    albumsById: Map<Long, Album>,
    openNowPlaying: (NowPlayingTransitionSnapshot?) -> Unit,
): RootPlaybackActions {
    return remember(dependencies, playbackManager, appLanguage, songsByAlbumId, albumsById, openNowPlaying) {
        RootPlaybackActions(
            playbackManager = playbackManager,
            languageProvider = { appLanguage },
            songsByAlbumIdProvider = { songsByAlbumId },
            albumsByIdProvider = { albumsById },
            openNowPlaying = openNowPlaying,
        )
    }
}

@Composable
internal fun rememberRootPlaylistActions(dependencies: PlaylistActionDependencies): RootPlaylistActions {
    return remember(dependencies) { RootPlaylistActions(dependencies) }
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
