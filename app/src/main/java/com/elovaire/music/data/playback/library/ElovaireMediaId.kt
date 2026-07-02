package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri

internal sealed interface ElovaireMediaId {
    val value: String

    data object Root : ElovaireMediaId { override val value = "elovaire:root" }
    data object PermissionRequired : ElovaireMediaId { override val value = "elovaire:info:permission_required" }
    data object EmptyLibrary : ElovaireMediaId { override val value = "elovaire:info:empty_library" }
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
    data class Bucket(val parent: String, val key: String) : ElovaireMediaId {
        override val value = "elovaire:bucket:$parent:${Uri.encode(key)}"
    }
}

internal object ElovaireMediaIds {
    fun song(id: Long): String = ElovaireMediaId.Song(id).value
    fun album(id: Long): String = ElovaireMediaId.Album(id).value
    fun artist(name: String): String = ElovaireMediaId.Artist(Uri.encode(name)).value
    fun genre(name: String): String = ElovaireMediaId.Genre(Uri.encode(name)).value
    fun playlist(id: Long): String = ElovaireMediaId.Playlist(id).value
    fun bucket(parent: String, key: String): String = ElovaireMediaId.Bucket(parent, key).value
    fun decodeName(encoded: String): String = Uri.decode(encoded).orEmpty()

    fun parse(value: String?): ElovaireMediaId? = when {
        value == ElovaireMediaId.Root.value -> ElovaireMediaId.Root
        value == ElovaireMediaId.PermissionRequired.value -> ElovaireMediaId.PermissionRequired
        value == ElovaireMediaId.EmptyLibrary.value -> ElovaireMediaId.EmptyLibrary
        value == ElovaireMediaId.Songs.value -> ElovaireMediaId.Songs
        value == ElovaireMediaId.Albums.value -> ElovaireMediaId.Albums
        value == ElovaireMediaId.Artists.value -> ElovaireMediaId.Artists
        value == ElovaireMediaId.Genres.value -> ElovaireMediaId.Genres
        value == ElovaireMediaId.Playlists.value -> ElovaireMediaId.Playlists
        value == ElovaireMediaId.Favorites.value -> ElovaireMediaId.Favorites
        value == ElovaireMediaId.RecentlyAdded.value -> ElovaireMediaId.RecentlyAdded
        value?.toLongOrNull() != null -> ElovaireMediaId.Song(value.toLong())
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
        value?.startsWith(BUCKET_PREFIX) == true -> {
            val parts = value.removePrefix(BUCKET_PREFIX).split(':', limit = 2)
            val parent = parts.getOrNull(0).orEmpty()
            val key = parts.getOrNull(1)?.let(::decodeName).orEmpty()
            if (parent.isNotBlank() && key.isNotBlank()) ElovaireMediaId.Bucket(parent, key) else null
        }
        else -> null
    }

    private const val SONG_PREFIX = "elovaire:song:"
    private const val ALBUM_PREFIX = "elovaire:album:"
    private const val ARTIST_PREFIX = "elovaire:artist:"
    private const val GENRE_PREFIX = "elovaire:genre:"
    private const val PLAYLIST_PREFIX = "elovaire:playlist:"
    private const val BUCKET_PREFIX = "elovaire:bucket:"
}
