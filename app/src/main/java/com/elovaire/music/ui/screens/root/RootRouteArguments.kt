package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavBackStackEntry

internal fun NavBackStackEntry.albumRouteId(): Long? = routeLongArg("albumId")

internal fun NavBackStackEntry.playlistRouteId(): Long? = routeLongArg("playlistId")

internal fun NavBackStackEntry.smartPlaylistRouteId(): Long? = routeLongArg("smartPlaylistId")

internal fun NavBackStackEntry.libraryCollectionKindArg(): LibraryCollectionKind {
    return arguments?.getString("kind")
        ?.let { runCatching { LibraryCollectionKind.valueOf(it) }.getOrNull() }
        ?: LibraryCollectionKind.Albums
}

internal fun NavBackStackEntry.genreRouteArg(): String {
    return arguments?.getString("genre")?.let(Uri::decode).orEmpty()
}

internal fun NavBackStackEntry.artistRouteArg(): String {
    return arguments?.getString("artistName")?.let(Uri::decode).orEmpty()
}

private fun NavBackStackEntry.routeLongArg(name: String): Long? {
    return arguments.routeLongArg(name)
}

internal fun Bundle?.routeLongArg(name: String): Long? {
    return this?.takeIf { it.containsKey(name) }
        ?.getLong(name)
        ?.takeIf { it != 0L }
}
