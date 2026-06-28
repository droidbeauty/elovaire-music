package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri

internal sealed interface ElovaireMediaId {
    val value: String

    data object Root : ElovaireMediaId { override val value = "elovaire:root" }
    data object Songs : ElovaireMediaId { override val value = "elovaire:songs" }
    data object Albums : ElovaireMediaId { override val value = "elovaire:albums" }
    data object Artists : ElovaireMediaId { override val value = "elovaire:artists" }
    data object Genres : ElovaireMediaId { override val value = "elovaire:genres" }
    data object Playlists : ElovaireMediaId { override val value = "elovaire:playlists" }
    data object Favorites : ElovaireMediaId { override val value = "elovaire:favorites" }
    data object RecentlyAdded : ElovaireMediaId { override val value = "elovaire:recently_added" }
    data class Song(val songId: Long) : ElovaireMediaId { override val value = "elovaire:song:$songId" }
    data class Album(val albumId: Long) : ElovaireMediaId { override val value = "elovaire:album:$albumId" }
    data class Artist(val encodedName: String) : ElovaireMediaId { override val value = "elovaire:artist:$encodedName" }
    data class Genre(val encodedName: String) : ElovaireMediaId { override val value = "elovaire:genre:$encodedName" }
    data class Playlist(val playlistId: Long) : ElovaireMediaId { override val value = "elovaire:playlist:$playlistId" }
}

internal object ElovaireMediaIds {
    fun song(id: Long): String = ElovaireMediaId.Song(id).value
    fun album(id: Long): String = ElovaireMediaId.Album(id).value
    fun artist(name: String): String = ElovaireMediaId.Artist(Uri.encode(name)).value
    fun genre(name: String): String = ElovaireMediaId.Genre(Uri.encode(name)).value
    fun playlist(id: Long): String = ElovaireMediaId.Playlist(id).value
    fun decodeName(encoded: String): String = Uri.decode(encoded).orEmpty()

    fun parse(value: String?): ElovaireMediaId? = when {
        value == ElovaireMediaId.Root.value -> ElovaireMediaId.Root
        value == ElovaireMediaId.Songs.value -> ElovaireMediaId.Songs
        value == ElovaireMediaId.Albums.value -> ElovaireMediaId.Albums
        value == ElovaireMediaId.Artists.value -> ElovaireMediaId.Artists
        value == ElovaireMediaId.Genres.value -> ElovaireMediaId.Genres
        value == ElovaireMediaId.Playlists.value -> ElovaireMediaId.Playlists
        value == ElovaireMediaId.Favorites.value -> ElovaireMediaId.Favorites
        value == ElovaireMediaId.RecentlyAdded.value -> ElovaireMediaId.RecentlyAdded
        value?.startsWith(SONG_PREFIX) == true ->
            value.removePrefix(SONG_PREFIX).toLongOrNull()?.let(ElovaireMediaId::Song)
        value?.startsWith(ALBUM_PREFIX) == true ->
            value.removePrefix(ALBUM_PREFIX).toLongOrNull()?.let(ElovaireMediaId::Album)
        value?.startsWith(ARTIST_PREFIX) == true ->
            ElovaireMediaId.Artist(value.removePrefix(ARTIST_PREFIX))
        value?.startsWith(GENRE_PREFIX) == true ->
            ElovaireMediaId.Genre(value.removePrefix(GENRE_PREFIX))
        value?.startsWith(PLAYLIST_PREFIX) == true ->
            value.removePrefix(PLAYLIST_PREFIX).toLongOrNull()?.let(ElovaireMediaId::Playlist)
        else -> null
    }

    private const val SONG_PREFIX = "elovaire:song:"
    private const val ALBUM_PREFIX = "elovaire:album:"
    private const val ARTIST_PREFIX = "elovaire:artist:"
    private const val GENRE_PREFIX = "elovaire:genre:"
    private const val PLAYLIST_PREFIX = "elovaire:playlist:"
}
