package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.core.PlaybackActionDependencies
import elovaire.music.droidbeauty.app.core.PlaylistActionDependencies
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.SleepTimerOption
import elovaire.music.droidbeauty.app.data.playback.AudiobookPlaybackContext
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.localizedAllSongsSource

internal class RootPlaybackActions internal constructor(
    private val playbackManager: NowPlayingPlayback,
    private val languageProvider: () -> AppLanguage,
    private val songsByAlbumIdProvider: () -> Map<Long, List<Song>>,
    private val albumsByIdProvider: () -> Map<Long, Album>,
    private val openNowPlaying: (NowPlayingTransitionSnapshot?) -> Unit,
) {
    val progressState get() = playbackManager.progressState
    val sleepTimerState get() = playbackManager.sleepTimerState
    val audiobookProgressRevision get() = playbackManager.audiobookProgressRevision

    fun audiobookProgress(bookKey: String): AudiobookProgress? = playbackManager.audiobookProgress(bookKey)

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

    fun playAudiobook(
        book: Audiobook,
        part: AudiobookPart? = null,
        resume: Boolean = true,
    ) {
        val requestedPart = part ?: book.parts.firstOrNull() ?: return
        val resumeSongId = if (resume) {
            playbackManager.audiobookResumeSongId(book.stableKey)
        } else {
            null
        }
        val selectedPart = book.parts.firstOrNull { it.song.id == resumeSongId } ?: requestedPart
        val orderedSongs = book.parts.map(AudiobookPart::song).distinctBy(Song::id)
        if (orderedSongs.isEmpty() || orderedSongs.none { it.id == selectedPart.song.id }) return
        val savedProgress = if (resume) playbackManager.audiobookProgress(book.stableKey) else null
        playbackManager.playSongAtPosition(
            song = selectedPart.song,
            collection = orderedSongs,
            positionMs = if (resume && resumeSongId != null && savedProgress?.songId == selectedPart.song.id) {
                savedProgress.positionMs.coerceAtMost(selectedPart.song.durationMs.coerceAtLeast(0L))
            } else if (resume && savedProgress == null) {
                selectedPart.song.bookmarkMs?.coerceAtLeast(0L) ?: 0L
            } else {
                selectedPart.startMs?.coerceAtLeast(0L) ?: 0L
            },
            sourceLabel = book.title,
            shuffleEnabled = false,
            audiobookContext = AudiobookPlaybackContext(
                bookKey = book.stableKey,
                orderedSongIds = orderedSongs.map(Song::id),
                bookDurationMs = book.durationMs,
                orderedSongDurationsMs = orderedSongs.map(Song::durationMs),
            ),
        )
    }

    fun seekCurrentBy(deltaMs: Long) {
        val progress = playbackManager.progressState.value
        val duration = progress.durationMs.takeIf { it > 0L }
        val target = (progress.positionMs + deltaMs).coerceAtLeast(0L)
        playbackManager.seekTo(duration?.let { target.coerceAtMost(it) } ?: target)
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackManager.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(option: SleepTimerOption) {
        playbackManager.setSleepTimer(option)
    }
}

internal class RootPlaylistActions internal constructor(
    private val dependencies: PlaylistActionDependencies,
) {
    fun createPlaylist(name: String): PlaylistMutationRequest = dependencies.playlistStore.createPlaylist(name)

    fun createPlaylistAndAddSongs(
        name: String,
        songIds: List<Long>,
    ): PlaylistMutationRequest = dependencies.playlistStore.createPlaylistWithSongs(name, songIds)

    fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>,
    ): PlaylistMutationRequest = dependencies.playlistStore.addSongsToPlaylist(playlistId, songIds)

    fun addAlbumToPlaylist(
        playlistId: Long,
        album: Album,
    ): PlaylistMutationRequest = addSongsToPlaylist(playlistId, album.songs.map(Song::id))

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
    playbackManager: NowPlayingPlayback,
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
