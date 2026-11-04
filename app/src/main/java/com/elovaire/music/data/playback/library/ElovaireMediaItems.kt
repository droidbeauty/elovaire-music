package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.carMediaCopy
import elovaire.music.droidbeauty.app.ui.i18n.homeCopy
import elovaire.music.droidbeauty.app.ui.i18n.audiobookCopy
import elovaire.music.droidbeauty.app.ui.i18n.builtInSmartPlaylistTitle
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.localizedArtistName
import elovaire.music.droidbeauty.app.ui.i18n.localizedAlbumTitle
import elovaire.music.droidbeauty.app.ui.i18n.localizedGenreName
import elovaire.music.droidbeauty.app.ui.i18n.localizedExternalAudioText
import elovaire.music.droidbeauty.app.ui.i18n.localizedAudiobookAuthor
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType

internal object ElovaireMediaItems {
    fun root(): MediaItem = browsable(
        mediaId = ElovaireMediaId.Root.value,
        title = "Elovaire",
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        extras = CarMediaStyleExtras.rootExtras(),
    )

    fun permissionRequiredInfo(language: AppLanguage = AppLanguage.English): MediaItem = informational(
        mediaId = ElovaireMediaId.PermissionRequired.value,
        title = carMediaCopy(language).openPhone,
        subtitle = carMediaCopy(language).grantAccess,
    )

    fun emptyLibraryInfo(language: AppLanguage = AppLanguage.English): MediaItem = informational(
        mediaId = ElovaireMediaId.EmptyLibrary.value,
        title = if (language == AppLanguage.English) "No music found" else homeCopy(language).emptyLibraryTitle,
        subtitle = carMediaCopy(language).addMusic,
    )

    fun songsRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Songs.value,
        commonUiCopy(language).songs,
        MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_list_music"),
        extras = CarMediaStyleExtras.songsExtras(),
    )
    fun albumsRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Albums.value,
        commonUiCopy(language).albums,
        MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS,
        artworkUri = drawableUri("ic_lucide_disc_album"),
        extras = CarMediaStyleExtras.albumsExtras(),
    )
    fun artistsRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Artists.value,
        commonUiCopy(language).artists,
        MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS,
        artworkUri = drawableUri("ic_lucide_mic_vocal"),
    )
    fun genresRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Genres.value,
        commonUiCopy(language).genres,
        MediaMetadata.MEDIA_TYPE_FOLDER_GENRES,
        artworkUri = drawableUri("ic_lucide_guitar"),
    )
    fun playlistsRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Playlists.value,
        commonUiCopy(language).playlists,
        MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS,
        artworkUri = drawableUri("ic_lucide_list_music"),
    )
    fun favoritesRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Favorites.value,
        builtInSmartPlaylistTitle(BuiltInSmartPlaylistType.Favorites, language),
        MediaMetadata.MEDIA_TYPE_PLAYLIST,
        artworkUri = drawableUri("ic_lucide_star_filled"),
        extras = CarMediaStyleExtras.songsExtras(),
    )
    fun recentlyAddedRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.RecentlyAdded.value,
        miscPhrase(language, MiscPhrase.RecentlyAdded),
        MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_refresh_cw"),
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun audiobooksRoot(language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaId.Audiobooks.value,
        audiobookCopy(language).title,
        MediaMetadata.MEDIA_TYPE_FOLDER_AUDIO_BOOKS,
        artworkUri = drawableUri("ic_lucide_library"),
    )

    fun audiobook(book: Audiobook, language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        ElovaireMediaIds.audiobook(book.stableKey),
        book.title,
        MediaMetadata.MEDIA_TYPE_AUDIO_BOOK,
        subtitle = localizedAudiobookAuthor(book.author, language),
        artworkUri = localArtworkUri(book.artUri) ?: drawableUri("ic_lucide_library"),
    )

    fun bucket(parent: String, key: String): MediaItem = browsable(
        mediaId = ElovaireMediaIds.bucket(parent, key),
        title = key,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
        artworkUri = drawableUri("ic_lucide_list"),
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun album(album: Album, language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        mediaId = ElovaireMediaIds.album(album.id),
        title = localizedAlbumTitle(album.title, language),
        subtitle = localizedArtistName(album.artist, language),
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

    fun artist(name: String, language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        mediaId = ElovaireMediaIds.artist(name),
        title = localizedArtistName(name, language),
        artworkUri = drawableUri("ic_lucide_mic_vocal"),
        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun genre(name: String, language: AppLanguage = AppLanguage.English): MediaItem = browsable(
        mediaId = ElovaireMediaIds.genre(name),
        title = localizedGenreName(name, language),
        artworkUri = drawableUri("ic_lucide_guitar"),
        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
        extras = CarMediaStyleExtras.songsExtras(),
    )

    fun audiobookPart(song: Song): MediaItem = song(song, MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)

    fun song(song: Song): MediaItem = song(song, MediaMetadata.MEDIA_TYPE_MUSIC)

    fun song(song: Song, language: AppLanguage): MediaItem = song(song, MediaMetadata.MEDIA_TYPE_MUSIC, language)

    fun audiobookPart(song: Song, language: AppLanguage): MediaItem = song(song, MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER, language)

    fun playable(song: Song): MediaItem = if (song.mediaKind == elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Audiobook) {
        audiobookPart(song)
    } else {
        song(song)
    }

    fun playable(song: Song, language: AppLanguage): MediaItem = if (song.mediaKind == elovaire.music.droidbeauty.app.domain.model.AudioMediaKind.Audiobook) {
        audiobookPart(song, language)
    } else {
        song(song, language)
    }

    private fun song(song: Song, mediaType: Int, language: AppLanguage = AppLanguage.English): MediaItem = MediaItem.Builder()
        .setMediaId(ElovaireMediaIds.song(song.id))
        .setUri(song.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(localizedExternalAudioText(song.title, song.id, language))
                .setArtist(localizedArtistName(song.artist, language))
                .setAlbumTitle(localizedExternalAudioText(localizedAlbumTitle(song.album, language), song.id, language))
                .setAlbumArtist(localizedArtistName(song.albumArtist ?: song.artist, language))
                .setTrackNumber(song.trackNumber.takeIf { it > 0 })
                .setDiscNumber(song.discNumber.takeIf { it > 0 })
                .setReleaseYear(song.releaseYear)
                .setGenre(song.genre.takeIf(String::isNotBlank)?.let { localizedGenreName(it, language) })
                .setDurationMs(song.durationMs.takeIf { it > 0 })
                .setArtworkUri(localArtworkUri(song.artUri) ?: drawableUri("ic_lucide_music"))
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(mediaType)
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

}
