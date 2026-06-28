package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal object ElovaireMediaItems {
    fun root(): MediaItem = browsable(
        mediaId = ElovaireMediaId.Root.value,
        title = "Elovaire",
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
    )

    fun songsRoot(): MediaItem = browsable(ElovaireMediaId.Songs.value, "Songs", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
    fun albumsRoot(): MediaItem = browsable(ElovaireMediaId.Albums.value, "Albums", MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS)
    fun artistsRoot(): MediaItem = browsable(ElovaireMediaId.Artists.value, "Artists", MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
    fun genresRoot(): MediaItem = browsable(ElovaireMediaId.Genres.value, "Genres", MediaMetadata.MEDIA_TYPE_FOLDER_GENRES)
    fun playlistsRoot(): MediaItem = browsable(ElovaireMediaId.Playlists.value, "Playlists", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
    fun favoritesRoot(): MediaItem = browsable(ElovaireMediaId.Favorites.value, "Favorites", MediaMetadata.MEDIA_TYPE_PLAYLIST)
    fun recentlyAddedRoot(): MediaItem = browsable(ElovaireMediaId.RecentlyAdded.value, "Recently added", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)

    fun album(album: Album): MediaItem = browsable(
        mediaId = ElovaireMediaIds.album(album.id),
        title = album.title,
        subtitle = album.artist,
        artworkUri = album.artUri,
        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
    )

    fun playlist(playlist: Playlist): MediaItem = browsable(
        mediaId = ElovaireMediaIds.playlist(playlist.id),
        title = playlist.name,
        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
    )

    fun artist(name: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.artist(name),
        title = name.ifBlank { UNKNOWN_ARTIST },
        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
    )

    fun genre(name: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.genre(name),
        title = name.ifBlank { UNKNOWN_GENRE },
        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
    )

    fun song(song: Song): MediaItem = MediaItem.Builder()
        .setMediaId(ElovaireMediaIds.song(song.id))
        .setUri(song.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setAlbumArtist(song.albumArtist ?: song.artist)
                .setTrackNumber(song.trackNumber.takeIf { it > 0 })
                .setDiscNumber(song.discNumber.takeIf { it > 0 })
                .setReleaseYear(song.releaseYear)
                .setGenre(song.genre.takeIf(String::isNotBlank))
                .setDurationMs(song.durationMs.takeIf { it > 0 })
                .setArtworkUri(song.artUri)
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build(),
        )
        .build()

    private fun browsable(
        mediaId: String,
        title: String,
        mediaType: Int,
        subtitle: String? = null,
        artworkUri: Uri? = null,
    ): MediaItem = MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setArtworkUri(artworkUri)
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .setMediaType(mediaType)
                .build(),
        )
        .build()

    private const val UNKNOWN_ARTIST = "Unknown Artist"
    private const val UNKNOWN_GENRE = "Unknown Genre"
}
