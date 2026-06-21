package elovaire.music.droidbeauty.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.theme.InkText
import kotlin.math.max
import kotlin.math.min

internal const val HOME_ROUTE = "home"
internal const val ALBUMS_ROUTE = "albums"
internal const val PLAYLISTS_ROUTE = "playlists"
internal const val PLAYLIST_ROUTE = "playlist"
internal const val SEARCH_ROUTE = "search"
internal const val PLAYER_ROUTE = "player"
internal const val EQUALIZER_ROUTE = "equalizer"
internal const val SETTINGS_ROUTE = "settings"
internal const val CHANGELOG_ROUTE = "changelog"
internal const val ABOUT_ROUTE = "about"
internal const val ALBUM_ROUTE = "album"
internal const val ALBUM_TAG_EDITOR_ROUTE = "album_tag_editor"
internal const val LIBRARY_COLLECTION_ROUTE = "library_collection"
internal const val GENRE_ROUTE = "genre"
internal const val ARTIST_ROUTE = "artist"
internal val TopLevelRoutes = setOf(
    HOME_ROUTE,
    ALBUMS_ROUTE,
    PLAYLISTS_ROUTE,
    SEARCH_ROUTE,
)
internal val BottomNavigationRoutes = setOf(
    HOME_ROUTE,
    ALBUMS_ROUTE,
    PLAYLISTS_ROUTE,
    SEARCH_ROUTE,
    "$ALBUM_ROUTE/{albumId}",
    "$PLAYLIST_ROUTE/{playlistId}",
    "$LIBRARY_COLLECTION_ROUTE/{kind}",
    "$GENRE_ROUTE/{genre}",
    "$ARTIST_ROUTE/{artistName}",
)
internal const val NOW_PLAYING_TITLE_TEXT_SIZE_SP = 23f
internal const val NOW_PLAYING_ARTIST_TEXT_SIZE_SP = 18f
internal const val ALBUM_HEADER_TITLE_TEXT_SIZE_SP = 23f
internal const val ALBUM_HEADER_ARTIST_TEXT_SIZE_SP = 18f
internal val EQ_DB_SCALE_WIDTH = 30.dp
internal val EQ_DB_SCALE_GAP = 10.dp
internal val EQ_BAND_SPACING = 40.dp
internal val EQ_GRAPH_EDGE_PADDING = 18.dp
internal val EQ_BAND_PANEL_HEIGHT = 226.dp
internal val lazyListPositionCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, Int>>()
internal val lazyGridPositionCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, Int>>()
internal val scrollPositionCache = java.util.concurrent.ConcurrentHashMap<String, Int>()
internal val topLevelScrollCachePrefixes = mapOf(
    HOME_ROUTE to listOf("home_screen"),
    ALBUMS_ROUTE to listOf(
        "library_hub",
        "song_collection_list",
        "artist_collection",
        "genre_collection",
        "album_collection_list",
        "album_collection_grid",
        "artist_detail",
        "album_detail",
    ),
    PLAYLISTS_ROUTE to listOf(
        "playlists_screen",
        "playlist_detail",
        "playlist_add_songs_overlay",
    ),
    SEARCH_ROUTE to listOf("search_screen"),
)

internal data class TopLevelDestination(
    val route: String,
    val iconResId: Int,
    val contentDescription: String,
)

internal val DefaultTopLevelDestinations = listOf(
    TopLevelDestination(
        route = HOME_ROUTE,
        iconResId = R.drawable.ic_lucide_house,
        contentDescription = "Home",
    ),
    TopLevelDestination(
        route = ALBUMS_ROUTE,
        iconResId = R.drawable.ic_lucide_library,
        contentDescription = "Albums",
    ),
    TopLevelDestination(
        route = PLAYLISTS_ROUTE,
        iconResId = R.drawable.ic_lucide_list_music,
        contentDescription = "Playlists",
    ),
    TopLevelDestination(
        route = SEARCH_ROUTE,
        iconResId = R.drawable.ic_lucide_search,
        contentDescription = "Search",
    ),
)

internal data class TopLevelRouteTransitionResolution(
    val isTopLevelTransition: Boolean,
    val isForward: Boolean,
)

internal data class NavHostTransitionResolution(
    val initialRoute: String?,
    val targetRoute: String?,
    val initialUsesTileExpand: Boolean,
    val targetUsesTileExpand: Boolean,
    val initialUsesDetailTransition: Boolean,
    val targetUsesDetailTransition: Boolean,
    val topLevelTransition: TopLevelRouteTransitionResolution,
)

internal enum class DetailRouteTransitionMode {
    TileExpand,
    Standard,
}

internal object ElovaireNavigationTransitions {
    fun depthOf(route: String?): Int {
        return when (route.normalizedNavigationRoute()) {
            HOME_ROUTE,
            ALBUMS_ROUTE,
            PLAYLISTS_ROUTE,
            SEARCH_ROUTE,
            PLAYER_ROUTE,
            null,
            -> 0

            SETTINGS_ROUTE,
            EQUALIZER_ROUTE,
            CHANGELOG_ROUTE,
            ABOUT_ROUTE,
            "$LIBRARY_COLLECTION_ROUTE/{kind}",
            "$GENRE_ROUTE/{genre}",
            "$ARTIST_ROUTE/{artistName}",
            -> 1

            "$PLAYLIST_ROUTE/{playlistId}",
            "$ALBUM_ROUTE/{albumId}",
            "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
            -> 2

            else -> 1
        }
    }

    fun usesTileExpand(
        route: String?,
        mode: DetailRouteTransitionMode,
    ): Boolean {
        return mode == DetailRouteTransitionMode.TileExpand && route.normalizedNavigationRoute().isExpandFromTileRoute()
    }

    fun usesDetailTransition(route: String?): Boolean {
        return when (route.normalizedNavigationRoute()) {
            "$ALBUM_ROUTE/{albumId}",
            "$PLAYLIST_ROUTE/{playlistId}",
            "$LIBRARY_COLLECTION_ROUTE/{kind}",
            "$GENRE_ROUTE/{genre}",
            "$ARTIST_ROUTE/{artistName}",
            -> true

            else -> false
        }
    }

    fun isTopLevelRouteTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): Boolean {
        return topLevelRouteIndex(initialRoute) >= 0 && topLevelRouteIndex(targetRoute) >= 0
    }

    fun isForwardTopLevelRouteTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): Boolean {
        val initialIndex = topLevelRouteIndex(initialRoute)
        val targetIndex = topLevelRouteIndex(targetRoute)
        return initialIndex >= 0 && targetIndex >= 0 && targetIndex > initialIndex
    }

    fun resolveTopLevelRouteTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): TopLevelRouteTransitionResolution {
        val initialIndex = topLevelRouteIndex(initialRoute)
        val targetIndex = topLevelRouteIndex(targetRoute)
        return TopLevelRouteTransitionResolution(
            isTopLevelTransition = initialIndex >= 0 && targetIndex >= 0 && initialIndex != targetIndex,
            isForward = initialIndex >= 0 && targetIndex >= 0 && targetIndex > initialIndex,
        )
    }

    fun resolveNavHostTransition(
        initialRoute: String?,
        targetRoute: String?,
        initialFallbackTopLevelRoute: String?,
        targetFallbackTopLevelRoute: String?,
        detailRouteTransitionMode: DetailRouteTransitionMode,
    ): NavHostTransitionResolution {
        val resolvedInitialRoute = initialRoute.normalizedNavigationRoute()
        val resolvedTargetRoute = targetRoute.normalizedNavigationRoute()
        return NavHostTransitionResolution(
            initialRoute = resolvedInitialRoute,
            targetRoute = resolvedTargetRoute,
            initialUsesTileExpand = usesTileExpand(
                route = resolvedInitialRoute,
                mode = detailRouteTransitionMode,
            ),
            targetUsesTileExpand = usesTileExpand(
                route = resolvedTargetRoute,
                mode = detailRouteTransitionMode,
            ),
            initialUsesDetailTransition = usesDetailTransition(resolvedInitialRoute),
            targetUsesDetailTransition = usesDetailTransition(resolvedTargetRoute),
            topLevelTransition = resolveTopLevelRouteTransition(
                initialRoute = transitionTopLevelOwnerRoute(
                    route = resolvedInitialRoute,
                    fallbackTopLevelRoute = initialFallbackTopLevelRoute,
                ),
                targetRoute = transitionTopLevelOwnerRoute(
                    route = resolvedTargetRoute,
                    fallbackTopLevelRoute = targetFallbackTopLevelRoute,
                ),
            ),
        )
    }

    private fun topLevelRouteIndex(route: String?): Int {
        return when (route.normalizedNavigationRoute()) {
            HOME_ROUTE -> 0
            ALBUMS_ROUTE -> 1
            PLAYLISTS_ROUTE -> 2
            SEARCH_ROUTE -> 3
            else -> -1
        }
    }
}

internal fun resolveTreePath(uri: Uri): String {
    val treeDocumentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull().orEmpty()
    if (treeDocumentId.isBlank()) return ""
    val separatorIndex = treeDocumentId.indexOf(':')
    if (separatorIndex <= 0) return ""
    val volume = treeDocumentId.substring(0, separatorIndex)
    val relativePath = treeDocumentId.substring(separatorIndex + 1).trim('/').replace(':', '/')
    val basePath = if (volume.equals("primary", ignoreCase = true)) {
        "/storage/emulated/0"
    } else {
        "/storage/$volume"
    }
    return listOf(basePath, relativePath)
        .filter { it.isNotBlank() }
        .joinToString("/")
        .replace("//", "/")
}

internal fun defaultLibraryPickerUri(preferredUri: Uri? = null): Uri? {
    if (preferredUri != null) return preferredUri
    return runCatching {
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:",
        )
    }.getOrNull()
}

internal fun createLibraryFolderPickerIntent(initialUri: Uri?): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
        )
        if (initialUri != null) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }
    }
}

internal enum class AlbumLayoutMode {
    Compact,
    Grid,
    DenseGrid,
}

internal enum class SongSortMode(
    val label: String,
) {
    Title("Song name"),
    Artist("Artist name"),
    Album("Album"),
}

internal enum class SearchSongSortMode {
    Title,
    Artist,
}

internal enum class SearchContentMode {
    Discover,
    Results,
    AllSongs,
}

internal enum class PlaylistPickerTab(
    val label: String,
    @DrawableRes val iconResId: Int,
) {
    Songs("Songs", R.drawable.ic_lucide_music),
    Albums("Albums", R.drawable.ic_lucide_disc_album),
    Artists("Artists", R.drawable.ic_lucide_mic_vocal),
}

internal enum class AlbumSortMode(
    val label: String,
) {
    Artist("Artist name"),
    Album("Album name"),
}

internal fun String.toAlbumSortMode(): AlbumSortMode {
    return AlbumSortMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: AlbumSortMode.Artist
}

internal fun String.toAlbumLayoutMode(): AlbumLayoutMode {
    return AlbumLayoutMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: AlbumLayoutMode.Grid
}

internal fun String.toSongSortMode(): SongSortMode {
    return SongSortMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: SongSortMode.Title
}

internal enum class LibraryCollectionKind {
    Songs,
    Albums,
    Artists,
    Genres,
}

internal enum class HomeScreenState {
    Loading,
    Empty,
    Content,
}

internal data class ExpandOrigin(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f,
)

internal enum class AlbumOpenSource {
    LibraryAlbums,
    HomeSection,
    SearchResults,
    ArtistDetail,
    GenreDetail,
    Player,
}

internal data class NowPlayingTransitionSnapshot(
    val songId: Long,
    val barBounds: Rect,
    val artworkBounds: Rect,
)

internal enum class PlayerOverlayTransitionState {
    Compact,
    Expanding,
    Expanded,
    Dragging,
    Collapsing,
}

internal val Rect.isValidTransitionBounds: Boolean
    get() = left.isFinite() &&
        top.isFinite() &&
        right.isFinite() &&
        bottom.isFinite() &&
        width > 1f &&
        height > 1f

internal data class ArtistEntry(
    val name: String,
    val artUri: Uri?,
    val albumCount: Int,
    val songCount: Int,
)

internal data class GenreEntry(
    val name: String,
    val albumCount: Int,
)

internal sealed interface LyricsUiState {
    data object Hidden : LyricsUiState
    data object Loading : LyricsUiState
    data class Ready(val payload: LyricsPayload) : LyricsUiState
    data object Empty : LyricsUiState
}

internal fun LyricsResult.toUiState(): LyricsUiState = when (this) {
    is LyricsResult.Found -> if (payload.lines.isEmpty()) LyricsUiState.Empty else LyricsUiState.Ready(payload)
    LyricsResult.NotFound -> LyricsUiState.Empty
    LyricsResult.Timeout -> LyricsUiState.Empty
}

internal enum class ProgressiveChromeEdge {
    Top,
    Bottom,
}

internal data class PlayerAdaptivePalette(
    val backdropBase: Color,
    val tintColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color,
)

internal fun Color.contrastRatioAgainst(other: Color): Float {
    val lighter = max(luminance(), other.luminance()) + 0.05f
    val darker = min(luminance(), other.luminance()) + 0.05f
    return lighter / darker
}

internal fun pickReadablePlayerForeground(
    background: Color,
    preferred: Color,
): Color {
    val candidates = listOf(
        preferred,
        Color.White,
        InkText,
    ).distinct()
    return candidates.maxByOrNull { it.contrastRatioAgainst(background) } ?: Color.White
}

internal fun artworkLedPlayerBase(primary: Color, secondary: Color): Color {
    val averageLuminance = (primary.luminance() + secondary.luminance()) / 2f
    val deepAnchor = if (averageLuminance > 0.52f) {
        Color(0xFF0B1014)
    } else {
        Color(0xFF050608)
    }
    return primary.copy(alpha = 0.58f)
        .compositeOver(secondary.copy(alpha = 0.4f))
        .compositeOver(deepAnchor)
}

internal fun buildPlayerAdaptivePalette(
    gradient: List<Color>,
    appBackground: Color,
    darkTheme: Boolean,
): PlayerAdaptivePalette {
    val primary = gradient.firstOrNull() ?: appBackground
    val secondary = gradient.lastOrNull() ?: primary
    val backdropBase = artworkLedPlayerBase(primary, secondary)
    val preferredForeground = if (backdropBase.luminance() < 0.34f) {
        secondary.copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.88f))
    } else {
        primary.copy(alpha = 1f).compositeOver(InkText.copy(alpha = 0.74f))
    }
    val contentColor = pickReadablePlayerForeground(
        background = backdropBase,
        preferred = preferredForeground,
    )
    val accentForeground = pickReadablePlayerForeground(
        background = backdropBase,
        preferred = if (contentColor.luminance() > 0.5f) {
            secondary.copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.72f))
        } else {
            primary.copy(alpha = 1f).compositeOver(InkText.copy(alpha = 0.42f))
        },
    )
    return PlayerAdaptivePalette(
        backdropBase = backdropBase,
        tintColor = primary.copy(alpha = 0.76f).compositeOver(secondary.copy(alpha = 0.24f)),
        contentColor = contentColor,
        secondaryContentColor = accentForeground.copy(alpha = 0.82f),
    )
}

internal fun topBarTitle(route: String?, language: AppLanguage): String {
    val common = commonUiCopy(language)
    return when (route) {
        ALBUMS_ROUTE -> common.library
        PLAYLISTS_ROUTE -> common.playlists
        SEARCH_ROUTE -> common.search
        else -> common.welcome
    }
}

internal fun detailFallbackTitle(route: String?, language: AppLanguage): String {
    val common = commonUiCopy(language)
    return when (route) {
        HOME_ROUTE -> common.home
        SEARCH_ROUTE -> common.search
        PLAYLISTS_ROUTE, "$PLAYLIST_ROUTE/{playlistId}" -> common.playlists
        ALBUMS_ROUTE, "$LIBRARY_COLLECTION_ROUTE/{kind}", "$GENRE_ROUTE/{genre}" -> common.library
        else -> common.library
    }
}

internal fun String?.normalizedNavigationRoute(): String? {
    return when {
        this == null -> null
        startsWith("$ALBUM_ROUTE/") -> "$ALBUM_ROUTE/{albumId}"
        startsWith("$ALBUM_TAG_EDITOR_ROUTE/") -> "$ALBUM_TAG_EDITOR_ROUTE/{albumId}"
        startsWith("$PLAYLIST_ROUTE/") -> "$PLAYLIST_ROUTE/{playlistId}"
        startsWith("$GENRE_ROUTE/") -> "$GENRE_ROUTE/{genre}"
        startsWith("$ARTIST_ROUTE/") -> "$ARTIST_ROUTE/{artistName}"
        startsWith("$LIBRARY_COLLECTION_ROUTE/") -> "$LIBRARY_COLLECTION_ROUTE/{kind}"
        else -> this
    }
}

internal fun androidx.navigation.NavBackStackEntry.elovaireConcreteRoute(): String? {
    return when (destination.route) {
        "$ALBUM_ROUTE/{albumId}" -> "$ALBUM_ROUTE/${arguments?.getLong("albumId") ?: return null}"
        "$ALBUM_TAG_EDITOR_ROUTE/{albumId}" -> "$ALBUM_TAG_EDITOR_ROUTE/${arguments?.getLong("albumId") ?: return null}"
        "$PLAYLIST_ROUTE/{playlistId}" -> "$PLAYLIST_ROUTE/${arguments?.getLong("playlistId") ?: return null}"
        "$GENRE_ROUTE/{genre}" -> "$GENRE_ROUTE/${Uri.encode(arguments?.getString("genre") ?: return null)}"
        "$ARTIST_ROUTE/{artistName}" -> "$ARTIST_ROUTE/${Uri.encode(arguments?.getString("artistName") ?: return null)}"
        "$LIBRARY_COLLECTION_ROUTE/{kind}" -> "$LIBRARY_COLLECTION_ROUTE/${arguments?.getString("kind") ?: return null}"
        else -> destination.route
    }
}

internal fun String?.isExpandFromTileRoute(): Boolean {
    return this.normalizedNavigationRoute() == "$ALBUM_ROUTE/{albumId}" ||
        this.normalizedNavigationRoute() == "$PLAYLIST_ROUTE/{playlistId}"
}

internal fun androidx.navigation.NavBackStackEntry.concreteNavigationRoute(): String? {
    val route = destination.route ?: return null
    val args = arguments
    return when (route) {
        "$ALBUM_ROUTE/{albumId}" -> args?.getLong("albumId")?.takeIf { it > 0L }?.let { "$ALBUM_ROUTE/$it" }
        "$ALBUM_TAG_EDITOR_ROUTE/{albumId}" -> args?.getLong("albumId")?.takeIf { it > 0L }?.let { "$ALBUM_TAG_EDITOR_ROUTE/$it" }
        "$PLAYLIST_ROUTE/{playlistId}" -> args?.getLong("playlistId")?.takeIf { it > 0L }?.let { "$PLAYLIST_ROUTE/$it" }
        "$LIBRARY_COLLECTION_ROUTE/{kind}" -> args?.getString("kind")?.let { "$LIBRARY_COLLECTION_ROUTE/$it" }
        "$GENRE_ROUTE/{genre}" -> args?.getString("genre")?.let { "$GENRE_ROUTE/${Uri.encode(it)}" }
        "$ARTIST_ROUTE/{artistName}" -> args?.getString("artistName")?.let { "$ARTIST_ROUTE/${Uri.encode(it)}" }
        else -> route
    }
}

internal fun topLevelOwnerRoute(
    route: String?,
    browsingOriginRoute: String?,
): String? {
    return when (route.normalizedNavigationRoute()) {
        HOME_ROUTE -> HOME_ROUTE
        SEARCH_ROUTE -> SEARCH_ROUTE
        ALBUMS_ROUTE -> ALBUMS_ROUTE
        PLAYLISTS_ROUTE -> PLAYLISTS_ROUTE
        "$LIBRARY_COLLECTION_ROUTE/{kind}" -> ALBUMS_ROUTE
        "$PLAYLIST_ROUTE/{playlistId}" -> PLAYLISTS_ROUTE
        "$GENRE_ROUTE/{genre}",
        "$ARTIST_ROUTE/{artistName}",
        "$ALBUM_ROUTE/{albumId}",
        "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
        -> browsingOriginRoute.takeIf { it in TopLevelRoutes } ?: ALBUMS_ROUTE

        else -> browsingOriginRoute.takeIf { it in TopLevelRoutes }
    }
}

internal fun transitionTopLevelOwnerRoute(
    route: String?,
    fallbackTopLevelRoute: String?,
): String? {
    return when (route.normalizedNavigationRoute()) {
        HOME_ROUTE -> HOME_ROUTE
        SEARCH_ROUTE -> SEARCH_ROUTE
        ALBUMS_ROUTE -> ALBUMS_ROUTE
        PLAYLISTS_ROUTE -> PLAYLISTS_ROUTE
        "$LIBRARY_COLLECTION_ROUTE/{kind}",
        "$GENRE_ROUTE/{genre}",
        "$ARTIST_ROUTE/{artistName}",
        -> ALBUMS_ROUTE

        "$PLAYLIST_ROUTE/{playlistId}" -> PLAYLISTS_ROUTE
        "$ALBUM_ROUTE/{albumId}" -> fallbackTopLevelRoute.takeIf { it in TopLevelRoutes } ?: ALBUMS_ROUTE
        "$ALBUM_TAG_EDITOR_ROUTE/{albumId}" -> fallbackTopLevelRoute.takeIf { it in TopLevelRoutes } ?: ALBUMS_ROUTE
        else -> fallbackTopLevelRoute.takeIf { it in TopLevelRoutes }
    }
}

internal fun ExpandOrigin.toTransformOrigin(): TransformOrigin {
    return TransformOrigin(
        pivotFractionX = xFraction.coerceIn(0f, 1f),
        pivotFractionY = yFraction.coerceIn(0f, 1f),
    )
}

internal fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + ((stop - start) * fraction.coerceIn(0f, 1f))
}

internal fun lerpRect(
    start: Rect,
    stop: Rect,
    fraction: Float,
): Rect {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return Rect(
        left = lerpFloat(start.left, stop.left, clampedFraction),
        top = lerpFloat(start.top, stop.top, clampedFraction),
        right = lerpFloat(start.right, stop.right, clampedFraction),
        bottom = lerpFloat(start.bottom, stop.bottom, clampedFraction),
    )
}

internal fun Rect.coerceWithin(bounds: Rect): Rect {
    val width = width.coerceAtLeast(1f).coerceAtMost(bounds.width)
    val height = height.coerceAtLeast(1f).coerceAtMost(bounds.height)
    val clampedLeft = left.coerceIn(bounds.left, bounds.right - width)
    val clampedTop = top.coerceIn(bounds.top, bounds.bottom - height)
    return Rect(
        left = clampedLeft,
        top = clampedTop,
        right = clampedLeft + width,
        bottom = clampedTop + height,
    )
}

internal fun Rect?.toExpandOrigin(
    screenWidthPx: Float,
    screenHeightPx: Float,
): ExpandOrigin {
    if (this == null || screenWidthPx <= 0f || screenHeightPx <= 0f) {
        return ExpandOrigin()
    }

    val centerX = (left + right) / 2f
    val centerY = (top + bottom) / 2f
    return ExpandOrigin(
        xFraction = (centerX / screenWidthPx).coerceIn(0.1f, 0.9f),
        yFraction = (centerY / screenHeightPx).coerceIn(0.1f, 0.9f),
    )
}

internal fun albumSearchHistoryEntry(album: Album): SearchHistoryEntry {
    return SearchHistoryEntry(
        key = "album:${album.id}",
        kind = SearchHistoryKind.Album,
        title = album.title,
        subtitle = album.artist,
        artUri = album.artUri,
        albumId = album.id,
    )
}

internal fun artistSearchHistoryEntry(song: Song): SearchHistoryEntry {
    val normalizedArtist = normalizeSearchText(song.artist)
    return SearchHistoryEntry(
        key = "artist:$normalizedArtist",
        kind = SearchHistoryKind.Artist,
        title = song.artist.trim(),
        subtitle = song.album,
        artUri = song.artUri,
        query = song.artist.trim(),
    )
}
