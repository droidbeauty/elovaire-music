package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState

internal fun rootPerformanceRouteLabel(route: String?): String? {
    return when (route) {
        null -> null
        HOME_ROUTE -> "home"
        ALBUMS_ROUTE -> "library_hub"
        PLAYLISTS_ROUTE -> "playlists"
        SEARCH_ROUTE -> "search"
        SETTINGS_ROUTE -> "settings"
        LIBRARY_FOLDERS_ROUTE -> "library_folders"
        PRIVACY_SAFETY_ROUTE -> "privacy_safety"
        CHANGELOG_ROUTE -> "changelog"
        ABOUT_ROUTE -> "about"
        EQUALIZER_ROUTE -> "equalizer"
        PLAYER_ROUTE -> "now_playing"
        else -> when {
            route.startsWith("$ALBUM_ROUTE/") || route == "$ALBUM_ROUTE/{albumId}" -> "album_detail"
            route.startsWith("$PLAYLIST_ROUTE/") || route == "$PLAYLIST_ROUTE/{playlistId}" -> "playlist_detail"
            route.startsWith("$ARTIST_ROUTE/") || route == "$ARTIST_ROUTE/{artistName}" -> "artist_detail"
            route.startsWith("$GENRE_ROUTE/") || route == "$GENRE_ROUTE/{genre}" -> "genre_detail"
            route.startsWith("$LIBRARY_COLLECTION_ROUTE/") || route == "$LIBRARY_COLLECTION_ROUTE/{kind}" -> "library_collection"
            route.startsWith("$ALBUM_TAG_EDITOR_ROUTE/") || route == "$ALBUM_TAG_EDITOR_ROUTE/{albumId}" -> "tag_editor"
            else -> "other"
        }
    }
}

internal fun rootPerformanceLibraryLabel(libraryState: LibraryUiState): String {
    return when {
        libraryState.isLoading -> "library_loading"
        libraryState.scanProgress in 0f..0.999f -> "scan_active"
        else -> "scan_idle"
    }
}

internal fun rootPerformanceInteractionLabel(isPlaybackActuallyPlaying: Boolean): String {
    return if (isPlaybackActuallyPlaying) "playback_progress_active" else "idle"
}

@Composable
internal fun RootPerformanceStates(
    route: String?,
    libraryState: LibraryUiState,
    isPlaybackActuallyPlaying: Boolean,
) {
    val routeLabel = rootPerformanceRouteLabel(route)
    PerformanceState("route", routeLabel)
    PerformanceState("screen", routeLabel)
    PerformanceState("library", rootPerformanceLibraryLabel(libraryState))
    PerformanceState("interaction", rootPerformanceInteractionLabel(isPlaybackActuallyPlaying))
}
