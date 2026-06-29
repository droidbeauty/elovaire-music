package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song

internal object ElovaireMediaItems {
    fun root(): MediaItem = browsable(
        mediaId = ElovaireMediaId.Root.value,
        title = "Elovaire",
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        extras = CarMediaStyleExtras.rootExtras(),
    )

    fun permissionRequiredInfo(): MediaItem = informational(
        mediaId = ElovaireMediaId.PermissionRequired.value,
        title = "Open Elovaire on phone",
        subtitle = "Grant music access to browse in Android Auto",
    )

    fun emptyLibraryInfo(): MediaItem = informational(
        mediaId = ElovaireMediaId.EmptyLibrary.value,
        title = "No music found",
        subtitle = "Add music on your phone to browse in Android Auto",
    )

    fun songsRoot(): MediaItem = browsable(
        ElovaireMediaId.Songs.value,
        "Songs",
        MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_list_music"),
        extras = CarMediaStyleExtras.songsExtras(),
    )
    fun albumsRoot(): MediaItem = browsable(
        ElovaireMediaId.Albums.value,
        "Albums",
        MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS,
        artworkUri = drawableUri("ic_lucide_disc_album"),
        extras = CarMediaStyleExtras.albumsExtras(),
    )
    fun artistsRoot(): MediaItem = browsable(
        ElovaireMediaId.Artists.value,
        "Artists",
        MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS,
        artworkUri = drawableUri("ic_lucide_mic_vocal"),
    )
    fun genresRoot(): MediaItem = browsable(
        ElovaireMediaId.Genres.value,
        "Genres",
        MediaMetadata.MEDIA_TYPE_FOLDER_GENRES,
        artworkUri = drawableUri("ic_lucide_guitar"),
    )
    fun playlistsRoot(): MediaItem = browsable(
        ElovaireMediaId.Playlists.value,
        "Playlists",
        MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS,
        artworkUri = drawableUri("ic_lucide_list_music"),
    )
    fun favoritesRoot(): MediaItem = browsable(
        ElovaireMediaId.Favorites.value,
        "Favorites",
        MediaMetadata.MEDIA_TYPE_PLAYLIST,
        artworkUri = drawableUri("ic_lucide_star_filled"),
        extras = CarMediaStyleExtras.songsExtras(),
    )
    fun recentlyAddedRoot(): MediaItem = browsable(
        ElovaireMediaId.RecentlyAdded.value,
        "Recently added",
        MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_refresh_cw"),
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun bucket(parent: String, key: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.bucket(parent, key),
        title = key,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_list"),
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun album(album: Album): MediaItem = browsable(
        mediaId = ElovaireMediaIds.album(album.id),
        title = album.title,
        subtitle = album.artist,
        artworkUri = localArtworkUri(album.artUri) ?: drawableUri("ic_lucide_disc_album"),
        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun playlist(playlist: Playlist): MediaItem = browsable(
        mediaId = ElovaireMediaIds.playlist(playlist.id),
        title = playlist.name,
        artworkUri = drawableUri("ic_lucide_list_music"),
        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun artist(name: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.artist(name),
        title = name.ifBlank { UNKNOWN_ARTIST },
        artworkUri = drawableUri("ic_lucide_mic_vocal"),
        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun genre(name: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.genre(name),
        title = name.ifBlank { UNKNOWN_GENRE },
        artworkUri = drawableUri("ic_lucide_guitar"),
        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
        extras = CarMediaStyleExtras.songsExtras(),
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
                .setArtworkUri(localArtworkUri(song.artUri) ?: drawableUri("ic_lucide_music"))
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
        extras: Bundle? = null,
    ): MediaItem = MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setArtworkUri(localArtworkUri(artworkUri))
                .setExtras(extras)
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .setMediaType(mediaType)
                .build(),
        )
        .build()

    private fun informational(
        mediaId: String,
        title: String,
        subtitle: String,
    ): MediaItem = MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setIsPlayable(false)
                .setIsBrowsable(false)
                .build(),
        )
        .build()

    private fun localArtworkUri(uri: Uri?): Uri? {
        return when (uri?.scheme) {
            "content",
            "android.resource",
            -> uri
            else -> null
        }
    }

    private fun drawableUri(name: String): Uri {
        return Uri.Builder()
            .scheme("android.resource")
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath("drawable")
            .appendPath(name)
            .build()
    }

    private const val UNKNOWN_ARTIST = "Unknown Artist"
    private const val UNKNOWN_GENRE = "Unknown Genre"
}
