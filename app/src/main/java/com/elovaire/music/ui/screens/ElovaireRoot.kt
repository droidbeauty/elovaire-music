package elovaire.music.app.ui.screens

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Xml
import androidx.annotation.DrawableRes
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import elovaire.music.app.BuildConfig
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.app.R
import elovaire.music.app.core.AppContainer
import elovaire.music.app.data.changelog.ChangelogRelease
import elovaire.music.app.data.changelog.ChangelogRepository
import elovaire.music.app.data.library.LibraryUiState
import elovaire.music.app.data.lyrics.LyricsLine
import elovaire.music.app.data.lyrics.LyricsLookupMode
import elovaire.music.app.data.lyrics.LyricsPayload
import elovaire.music.app.data.lyrics.LyricsResult
import elovaire.music.app.data.lyrics.LyricsService
import elovaire.music.app.data.playback.EqualizerDspConfig
import elovaire.music.app.data.playback.EqualizerDspModel
import elovaire.music.app.data.playback.PlaybackManager
import elovaire.music.app.data.playback.PlaybackProgressState
import elovaire.music.app.data.playback.PlaybackRepeatMode
import elovaire.music.app.data.playback.PlaybackUiState
import elovaire.music.app.data.update.AppReleaseInfo
import elovaire.music.app.data.update.AppUpdateUiState
import elovaire.music.app.domain.model.Album
import elovaire.music.app.domain.model.EqSettings
import elovaire.music.app.domain.model.Playlist
import elovaire.music.app.domain.model.SearchHistoryEntry
import elovaire.music.app.domain.model.SearchHistoryKind
import elovaire.music.app.domain.model.Song
import elovaire.music.app.domain.model.SpaciousnessMode
import elovaire.music.app.domain.model.TextSizePreset
import elovaire.music.app.domain.model.ThemeMode
import elovaire.music.app.ui.components.ArtworkImage
import elovaire.music.app.ui.components.rememberArtworkBitmap
import elovaire.music.app.ui.components.rememberArtworkGradient
import elovaire.music.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.app.ui.motion.ElovaireMotion
import elovaire.music.app.ui.motion.rememberSystemAnimationScale
import elovaire.music.app.ui.theme.ElovaireRadii
import elovaire.music.app.ui.theme.ElovaireSpacing
import elovaire.music.app.ui.theme.AboutCardButtonAccent
import elovaire.music.app.ui.theme.DestructiveRed
import elovaire.music.app.ui.theme.elovaireScaledSp
import elovaire.music.app.ui.theme.rememberElovaireOverscrollFactory
import elovaire.music.app.ui.theme.InkText
import elovaire.music.app.ui.theme.RoseAccent
import elovaire.music.app.ui.theme.ToggleEnabledGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.math.pow
import org.xmlpull.v1.XmlPullParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val HOME_ROUTE = "home"
private const val ALBUMS_ROUTE = "albums"
private const val PLAYLISTS_ROUTE = "playlists"
private const val PLAYLIST_ROUTE = "playlist"
private const val SEARCH_ROUTE = "search"
private const val PLAYER_ROUTE = "player"
private const val EQUALIZER_ROUTE = "equalizer"
private const val SETTINGS_ROUTE = "settings"
private const val CHANGELOG_ROUTE = "changelog"
private const val ABOUT_ROUTE = "about"
private const val ALBUM_ROUTE = "album"
private const val LIBRARY_COLLECTION_ROUTE = "library_collection"
private const val GENRE_ROUTE = "genre"
private const val ARTIST_ROUTE = "artist"
private const val NOW_PLAYING_TITLE_TEXT_SIZE_SP = 23f
private const val NOW_PLAYING_ARTIST_TEXT_SIZE_SP = 18f
private const val ALBUM_HEADER_TITLE_TEXT_SIZE_SP = 23f
private const val ALBUM_HEADER_ARTIST_TEXT_SIZE_SP = 18f
private val EQ_DB_SCALE_WIDTH = 30.dp
private val EQ_DB_SCALE_GAP = 10.dp
private const val EQ_GRAPH_MIN_WIDTH_MULTIPLIER = 2.8f
private val EQ_GRAPH_MIN_WIDTH = 1080.dp

private data class TopLevelDestination(
    val route: String,
    val iconResId: Int,
    val contentDescription: String,
)

private data class SongMenuActions(
    val playlists: List<Playlist> = emptyList(),
    val onAddToPlaylist: (playlistId: Long, song: Song) -> Unit = { _, _ -> },
    val onAddToQueue: (Song) -> Unit = {},
    val onDeleteFromLibrary: (Song) -> Unit = {},
)

private enum class DetailRouteTransitionMode {
    TileExpand,
    Standard,
}

private object ElovaireNavigationTransitions {
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

    fun isSameLevelTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): Boolean {
        return depthOf(targetRoute) == depthOf(initialRoute)
    }

    fun isLibraryCategoryEnterTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): Boolean {
        return initialRoute.normalizedNavigationRoute() == ALBUMS_ROUTE &&
            targetRoute.normalizedNavigationRoute() == "$LIBRARY_COLLECTION_ROUTE/{kind}"
    }

    fun isLibraryCategoryReturnTransition(
        initialRoute: String?,
        targetRoute: String?,
    ): Boolean {
        return initialRoute.normalizedNavigationRoute() == "$LIBRARY_COLLECTION_ROUTE/{kind}" &&
            targetRoute.normalizedNavigationRoute() == ALBUMS_ROUTE
    }

    fun isUtilityScreenRoute(route: String?): Boolean {
        return route.normalizedNavigationRoute() in setOf(
            SETTINGS_ROUTE,
            EQUALIZER_ROUTE,
            ABOUT_ROUTE,
        )
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

private fun resolveTreePath(uri: Uri): String {
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

private fun Context.loadAboutScreenModel(): AboutScreenModel {
    val parser = resources.getXml(R.xml.info_screen)
    val sections = mutableListOf<AboutSection>()
    var currentSectionTitle = ""
    var currentSectionDescription: String? = null
    var currentEntries = mutableListOf<AboutEntry>()
    var currentEntryTitle: String? = null
    var currentEntryDescription: String? = null
    var currentLinks = mutableListOf<AboutLink>()

    fun closeEntry() {
        val entryTitle = currentEntryTitle
        if (!entryTitle.isNullOrBlank()) {
            currentEntries += AboutEntry(
                title = entryTitle,
                description = currentEntryDescription?.takeIf { it.isNotBlank() },
                links = currentLinks.toList(),
            )
        }
        currentEntryTitle = null
        currentEntryDescription = null
        currentLinks = mutableListOf()
    }

    fun closeSection() {
        closeEntry()
        if (currentSectionTitle.isNotBlank()) {
            sections += AboutSection(
                title = currentSectionTitle,
                description = currentSectionDescription?.takeIf { it.isNotBlank() },
                entries = currentEntries.toList(),
            )
        }
        currentSectionTitle = ""
        currentSectionDescription = null
        currentEntries = mutableListOf()
    }

    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
            XmlPullParser.START_TAG -> when (parser.name) {
                "section" -> {
                    closeSection()
                    currentSectionTitle = parser.getAttributeValue(null, "title").orEmpty()
                    currentSectionDescription = parser.getAttributeValue(null, "description")
                }

                "entry" -> {
                    closeEntry()
                    currentEntryTitle = parser.getAttributeValue(null, "title")
                    currentEntryDescription = parser.getAttributeValue(null, "description")
                }

                "link" -> {
                    val label = parser.getAttributeValue(null, "label").orEmpty()
                    val url = parser.getAttributeValue(null, "url").orEmpty()
                    if (label.isNotBlank() && url.isNotBlank()) {
                        currentLinks += AboutLink(label = label, url = url)
                    }
                }
            }

            XmlPullParser.END_TAG -> when (parser.name) {
                "entry" -> closeEntry()
                "section" -> closeSection()
            }
        }
        parser.next()
    }
    closeSection()
    return AboutScreenModel(sections = sections)
}

private fun defaultLibraryPickerUri(preferredUri: Uri? = null): Uri? {
    if (preferredUri != null) return preferredUri
    return runCatching {
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:",
        )
    }.getOrNull()
}

private fun createLibraryFolderPickerIntent(initialUri: Uri?): Intent {
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

private val LocalSongMenuActions = compositionLocalOf { SongMenuActions() }
private data class BackdropSnapshot(
    val bitmap: Bitmap,
    val sourceWidth: Int,
    val sourceHeight: Int,
)

private val LocalChromeBackdropBitmap = compositionLocalOf<BackdropSnapshot?> { null }
private val LocalChromeHazeState = compositionLocalOf<HazeState?> { null }
private val LocalPlayerHazeState = compositionLocalOf<HazeState?> { null }
private val LocalUseSharedTopBarBackdrop = compositionLocalOf { false }
private val LocalSharedTopBarController = compositionLocalOf<SharedTopBarController?> { null }
private val LocalRenderSharedTopBarContent = compositionLocalOf { false }
private val LocalSharedBackIconPainter = compositionLocalOf<Painter?> { null }
private val LocalSharedTopMenuIconPainter = compositionLocalOf<Painter?> { null }

private sealed interface SharedTopBarSpec {
    data class Unified(
        val title: String,
        val showSettings: Boolean,
        @DrawableRes val supplementalActionIconResId: Int? = null,
        val supplementalActionContentDescription: String? = null,
        val onSupplementalAction: (() -> Unit)? = null,
        val onOpenMenu: () -> Unit,
    ) : SharedTopBarSpec

    data class Back(
        val title: String,
        val onBack: () -> Unit,
        val centeredTitle: Boolean = false,
    ) : SharedTopBarSpec

    data class Detail(
        val title: String,
        val subtitle: String?,
        val onBack: () -> Unit,
    ) : SharedTopBarSpec
}

private fun SharedTopBarSpec.visualSignature(): String {
    return when (this) {
        is SharedTopBarSpec.Unified -> "unified|$title|$showSettings|${supplementalActionIconResId ?: 0}|${supplementalActionContentDescription.orEmpty()}"
        is SharedTopBarSpec.Back -> "back|$title|$centeredTitle"
        is SharedTopBarSpec.Detail -> "detail|$title|${subtitle.orEmpty()}"
    }
}

private data class SharedTopBarRegistration(
    val id: Any,
    val spec: SharedTopBarSpec,
)

private data class AboutScreenModel(
    val sections: List<AboutSection>,
)

private data class AboutSection(
    val title: String,
    val description: String?,
    val entries: List<AboutEntry>,
)

private data class AboutEntry(
    val title: String,
    val description: String?,
    val links: List<AboutLink>,
)

private data class AboutLink(
    val label: String,
    val url: String,
)

private class SharedTopBarController {
    var registration by mutableStateOf<SharedTopBarRegistration?>(null)
}

private enum class AlbumLayoutMode {
    Compact,
    Grid,
}

private enum class SongSortMode(
    val label: String,
) {
    Title("Song name"),
    Artist("Artist name"),
    Album("Album"),
}

private enum class SearchSongSortMode(
    val label: String,
) {
    Title("Song name"),
    Artist("Artist name"),
}

private enum class SearchContentMode {
    Discover,
    Results,
    AllSongs,
}

private enum class AlbumSortMode(
    val label: String,
) {
    Artist("Artist name"),
    Album("Album name"),
}

private fun String.toAlbumSortMode(): AlbumSortMode {
    return AlbumSortMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: AlbumSortMode.Artist
}

private fun String.toSongSortMode(): SongSortMode {
    return SongSortMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: SongSortMode.Title
}

private enum class LibraryCollectionKind {
    Songs,
    Albums,
    Artists,
    Genres,
}

private enum class HomeScreenState {
    Loading,
    Empty,
    Content,
}

private data class ExpandOrigin(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f,
)

private data class NowPlayingTransitionSnapshot(
    val songId: Long,
    val barBounds: androidx.compose.ui.geometry.Rect,
    val artworkBounds: androidx.compose.ui.geometry.Rect,
)

private enum class PlayerOverlayTransitionState {
    Compact,
    Expanding,
    Expanded,
    Dragging,
    Collapsing,
}

private val androidx.compose.ui.geometry.Rect.isValidTransitionBounds: Boolean
    get() = left.isFinite() &&
        top.isFinite() &&
        right.isFinite() &&
        bottom.isFinite() &&
        width > 1f &&
        height > 1f

private data class ArtistEntry(
    val name: String,
    val artUri: android.net.Uri?,
    val albumCount: Int,
    val songCount: Int,
)

private data class GenreEntry(
    val name: String,
    val albumCount: Int,
)

private sealed interface LyricsUiState {
    data object Hidden : LyricsUiState
    data object Loading : LyricsUiState
    data class Ready(val payload: LyricsPayload) : LyricsUiState
    data object Empty : LyricsUiState
}

private fun LyricsResult.toUiState(): LyricsUiState = when (this) {
    is LyricsResult.Found -> LyricsUiState.Ready(payload)
    LyricsResult.NotFound -> LyricsUiState.Empty
    LyricsResult.Timeout -> LyricsUiState.Empty
}

private enum class ProgressiveChromeEdge {
    Top,
    Bottom,
}

private data class PlayerAdaptivePalette(
    val backdropBase: Color,
    val tintColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color,
)

private fun Color.contrastRatioAgainst(other: Color): Float {
    val lighter = max(luminance(), other.luminance()) + 0.05f
    val darker = min(luminance(), other.luminance()) + 0.05f
    return lighter / darker
}

private fun pickReadablePlayerForeground(
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

private fun artworkLedPlayerBase(primary: Color, secondary: Color): Color {
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

private fun buildPlayerAdaptivePalette(
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

@Composable
private fun rememberChromeBackdropSnapshot(
    enabled: Boolean = true,
    refreshKey: Any? = Unit,
) : BackdropSnapshot? {
    val hostView = LocalView.current
    var bitmap by remember(refreshKey) { mutableStateOf<BackdropSnapshot?>(null) }

    DisposableEffect(refreshKey) {
        onDispose {
            bitmap = null
        }
    }

    LaunchedEffect(enabled, refreshKey) {
        if (!enabled) {
            bitmap = null
            return@LaunchedEffect
        }
        withFrameNanos { }
        bitmap = runCatching { hostView.rootView.drawToDownsampledBitmap() }.getOrNull()
    }

    return bitmap
}

private fun android.view.View.drawToDownsampledBitmap(
    downsampleFactor: Int = 4,
): BackdropSnapshot? {
    val sourceWidth = width.takeIf { it > 0 } ?: return null
    val sourceHeight = height.takeIf { it > 0 } ?: return null
    val targetWidth = (sourceWidth / downsampleFactor).coerceAtLeast(1)
    val targetHeight = (sourceHeight / downsampleFactor).coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val scaleX = targetWidth.toFloat() / sourceWidth.toFloat()
    val scaleY = targetHeight.toFloat() / sourceHeight.toFloat()
    canvas.scale(scaleX, scaleY)
    draw(canvas)
    return BackdropSnapshot(
        bitmap = bitmap,
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
    )
}

@Composable
private fun statusBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.statusBars.getTop(this).toDp() }
}

@Composable
private fun navigationBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
}

@Composable
private fun screenContainerSizePx(): androidx.compose.ui.unit.IntSize {
    return LocalWindowInfo.current.containerSize
}

@Composable
private fun topBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.topBarContentHeight

@Composable
private fun detailTopBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.detailTopBarContentHeight

@Composable
private fun sharedTopBarOccupiedHeight(): Dp =
    statusBarInsetDp() + maxOf(ElovaireSpacing.topBarContentHeight, ElovaireSpacing.detailTopBarContentHeight)

@Composable
private fun bottomNavigationOccupiedHeight(): Dp {
    return navigationBarInsetDp() + ElovaireSpacing.bottomNavigationBodyHeight
}

@Composable
private fun SnapshotBackdropBlurLayer(
    backdropBitmap: BackdropSnapshot?,
    bounds: androidx.compose.ui.geometry.Rect?,
    blurRadius: Dp,
    modifier: Modifier = Modifier,
) {
    if (backdropBitmap == null || bounds == null) return
    val density = LocalDensity.current
    val blurRadiusPx = with(density) { blurRadius.toPx() }
    Image(
        bitmap = backdropBitmap.bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .width(with(density) { backdropBitmap.sourceWidth.toDp() })
            .height(with(density) { backdropBitmap.sourceHeight.toDp() })
            .graphicsLayer {
                translationX = -bounds.left
                translationY = -bounds.top
                alpha = 0.995f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    renderEffect = android.graphics.RenderEffect
                        .createBlurEffect(
                            blurRadiusPx,
                            blurRadiusPx,
                            Shader.TileMode.CLAMP,
                        )
                        .asComposeRenderEffect()
                }
            }
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier
                } else {
                    Modifier.blur(blurRadius)
                },
            ),
    )
}

@Composable
private fun blurSurfaceOverlayColor(): Color = MaterialTheme.colorScheme.surface

@Composable
private fun blurSurfaceBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        InkText.copy(alpha = 0.08f)
    }
}

@Composable
private fun Modifier.horizontalGestureSafe(): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.systemGestureExclusion()
    } else {
        this
    }
}

@Composable
private fun rememberElovaireLazyListState(vararg inputs: Any?): LazyListState {
    return rememberSaveable(*inputs, saver = LazyListState.Saver) {
        LazyListState()
    }
}

@Composable
private fun rememberElovaireLazyGridState(vararg inputs: Any?): LazyGridState {
    return rememberSaveable(*inputs, saver = LazyGridState.Saver) {
        LazyGridState()
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun DynamicBackdropSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    overlayAlpha: Float = 0.7f,
    borderColor: Color? = null,
    showTopEdgeLine: Boolean = false,
    showBottomEdgeLine: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val hazeState = LocalChromeHazeState.current
    val overlayColor = blurSurfaceOverlayColor()

    Box(
        modifier = modifier.clip(shape),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(hazeState) {
                        blurRadius = 30.dp
                        backgroundColor = overlayColor.copy(alpha = overlayAlpha)
                        tints = listOf(HazeTint(overlayColor.copy(alpha = overlayAlpha)))
                        noiseFactor = 0.015f
                    },
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(overlayColor.copy(alpha = overlayAlpha)),
        )
        if (borderColor != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, borderColor, shape),
            )
        }
        if (showTopEdgeLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(blurSurfaceBorderColor()),
            )
        }
        if (showBottomEdgeLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(blurSurfaceBorderColor()),
            )
        }
        content()
    }
}

@Composable
private fun ProgressiveChromeBackdrop(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    modifier: Modifier = Modifier,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    DynamicBackdropSurface(
        modifier = modifier,
        overlayAlpha = overlayAlpha ?: 0.7f,
        showTopEdgeLine = edge == ProgressiveChromeEdge.Bottom && showEdgeLine,
        showBottomEdgeLine = edge == ProgressiveChromeEdge.Top && showEdgeLine,
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun ChromeHazeLayer(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    modifier: Modifier = Modifier,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    ProgressiveChromeBackdrop(
        darkTheme = darkTheme,
        edge = edge,
        overlayAlpha = overlayAlpha,
        flatOverlay = flatOverlay,
        showEdgeLine = showEdgeLine,
        modifier = modifier,
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun FrostedTopBarBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    edge: ProgressiveChromeEdge = ProgressiveChromeEdge.Top,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    ChromeHazeLayer(
        darkTheme = darkTheme,
        edge = edge,
        overlayAlpha = overlayAlpha,
        flatOverlay = flatOverlay,
        showEdgeLine = showEdgeLine,
        modifier = modifier,
    )
}

@Composable
private fun RegisterSharedTopBar(spec: SharedTopBarSpec) {
    val controller = LocalSharedTopBarController.current ?: return
    val registrationId = remember { Any() }
    val specSignature = remember(spec) {
        when (spec) {
            is SharedTopBarSpec.Unified -> "unified|${spec.title}|${spec.showSettings}|${spec.supplementalActionIconResId ?: 0}|${spec.supplementalActionContentDescription.orEmpty()}"
            is SharedTopBarSpec.Back -> "back|${spec.title}|${spec.centeredTitle}"
            is SharedTopBarSpec.Detail -> "detail|${spec.title}|${spec.subtitle.orEmpty()}"
        }
    }
    LaunchedEffect(controller, registrationId, specSignature) {
        controller.registration = SharedTopBarRegistration(
            id = registrationId,
            spec = spec,
        )
    }
    DisposableEffect(controller, registrationId) {
        onDispose {
            if (controller.registration?.id == registrationId) {
                controller.registration = null
            }
        }
    }
}

@OptIn(ExperimentalHazeApi::class)
private fun Modifier.playerFrostedSurface(
    tint: Color,
): Modifier = composed {
    val hazeState = LocalPlayerHazeState.current
    if (hazeState == null) {
        this
    } else {
        val tintIsDark = tint.luminance() < 0.44f
        hazeEffect(hazeState) {
            progressive = HazeProgressive.LinearGradient(
                startIntensity = 0.9f,
                endIntensity = 0.42f,
                preferPerformance = true,
            )
            blurRadius = 28.dp
            backgroundColor = tint.copy(alpha = if (tintIsDark) 0.18f else 0.14f)
            tints = listOf(
                HazeTint(tint.copy(alpha = if (tintIsDark) 0.28f else 0.2f)),
                HazeTint(
                    if (tintIsDark) {
                        Color.Black.copy(alpha = 0.14f)
                    } else {
                        Color.White.copy(alpha = 0.16f)
                    },
                ),
            )
            noiseFactor = 0.04f
        }
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun ElovaireRoot(
    container: AppContainer,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val libraryState by container.libraryRepository.state.collectAsStateWithLifecycle()
    val playbackState by container.playbackManager.state.collectAsStateWithLifecycle()
    val eqSettings by container.preferenceStore.eqSettings.collectAsStateWithLifecycle()
    val themeMode by container.preferenceStore.themeMode.collectAsStateWithLifecycle()
    val textSizePreset by container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
    val searchHistory by container.preferenceStore.searchHistory.collectAsStateWithLifecycle()
    val playlists by container.preferenceStore.playlists.collectAsStateWithLifecycle()
    val favoriteSongIds by container.preferenceStore.favoriteSongIds.collectAsStateWithLifecycle()
    val favoriteSongIdSet = remember(favoriteSongIds) { favoriteSongIds.toHashSet() }
    val albumPlayCounts by container.preferenceStore.albumPlayCounts.collectAsStateWithLifecycle()
    val songPlayCounts by container.preferenceStore.songPlayCounts.collectAsStateWithLifecycle()
    val albumCollectionGridEnabled by container.preferenceStore.albumCollectionGridEnabled.collectAsStateWithLifecycle()
    val songCollectionGridEnabled by container.preferenceStore.songCollectionGridEnabled.collectAsStateWithLifecycle()
    val albumCollectionSortModeName by container.preferenceStore.albumCollectionSortMode.collectAsStateWithLifecycle()
    val songCollectionSortModeName by container.preferenceStore.songCollectionSortMode.collectAsStateWithLifecycle()
    val openPlayerRequestVersion by container.openPlayerRequestVersion.collectAsStateWithLifecycle()
    val appUpdateState by container.appUpdateManager.uiState.collectAsStateWithLifecycle()
    val changelogReleases = remember(context) { ChangelogRepository(context).loadReleases() }
    val rootScope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    var hasRequestedAudioPermission by rememberSaveable { mutableStateOf(false) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var firstLaunchPermissionExperienceActive by rememberSaveable {
        mutableStateOf(!hasPermission)
    }
    var playFirstLaunchHomeReveal by rememberSaveable {
        mutableStateOf(false)
    }
    var pendingDeleteSong by remember { mutableStateOf<Song?>(null) }
    val songsById = remember(libraryState.songs) { libraryState.songs.associateBy { it.id } }
    val songsByAlbumId = remember(libraryState.songs) { libraryState.songs.groupBy { it.albumId } }
    val albumsById = remember(libraryState.albums) { libraryState.albums.associateBy { it.id } }

    val recentlyAddedAlbums = remember(libraryState.albums) {
        recentlyAddedAlbumsFor(libraryState)
    }
    val recentAlbums = remember(libraryState.albums, playbackState.recentAlbumIds) {
        recentAlbumsFor(libraryState, playbackState)
    }
    val favoriteAlbums = remember(libraryState.albums, songPlayCounts, recentAlbums, recentlyAddedAlbums) {
        favoriteAlbumsFor(
            libraryState = libraryState,
            songPlayCounts = songPlayCounts,
            recentAlbums = recentAlbums,
            recentlyAddedAlbums = recentlyAddedAlbums,
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasNotificationPermission = granted
        container.setNotificationsEnabled(granted)
    }
    val lyricsService = remember(container, context.applicationContext) {
        LyricsService(context.applicationContext)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        container.libraryRepository.onPermissionChanged(granted)
        if (
            granted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission &&
            !hasRequestedNotificationPermission
        ) {
            hasRequestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val deleteSongLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val song = pendingDeleteSong ?: return@rememberLauncherForActivityResult
        pendingDeleteSong = null
        if (result.resultCode == Activity.RESULT_OK) {
            rootScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.delete(song.uri, null, null)
                    }
                }.onSuccess {
                    container.preferenceStore.removeSongReferences(song.id)
                    container.libraryRepository.refresh(
                        forceMediaIndex = true,
                        showLoadingIndicator = false,
                    )
                }
            }
        }
    }

    LaunchedEffect(container) {
        delay(900)
        container.appUpdateManager.checkForUpdates()
    }

    LaunchedEffect(hasPermission, hasNotificationPermission) {
        container.libraryRepository.onPermissionChanged(hasPermission)
        if (!hasPermission && !hasRequestedAudioPermission) {
            hasRequestedAudioPermission = true
            permissionLauncher.launch(audioPermission())
        } else if (
            hasPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission &&
            !hasRequestedNotificationPermission
        ) {
            hasRequestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasNotificationPermission) {
        container.setNotificationsEnabled(hasNotificationPermission)
    }

    val showFirstLaunchPermissionOverlay =
        firstLaunchPermissionExperienceActive &&
            (
                !hasPermission ||
                    libraryState.isLoading ||
                    (
                        libraryState.songs.isEmpty() &&
                            libraryState.albums.isEmpty() &&
                            libraryState.errorMessage == null &&
                            !playFirstLaunchHomeReveal
                        )
                )

    LaunchedEffect(
        firstLaunchPermissionExperienceActive,
        hasPermission,
        libraryState.isLoading,
        libraryState.songs.size,
        libraryState.albums.size,
        libraryState.errorMessage,
    ) {
        if (
            firstLaunchPermissionExperienceActive &&
            hasPermission &&
            !libraryState.isLoading &&
            (libraryState.songs.isNotEmpty() || libraryState.albums.isNotEmpty() || libraryState.errorMessage != null)
        ) {
            playFirstLaunchHomeReveal = true
        }
    }

    if (!hasPermission) {
        FirstLaunchPermissionLoadingScreen(
            showLoading = true,
            onRequestPermission = { permissionLauncher.launch(audioPermission()) },
        )
        return
    }

    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null

    val topLevelDestinations = listOf(
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

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentAlbumRouteId = currentBackStackEntry?.arguments?.let { arguments ->
        when {
            arguments.containsKey("albumId") -> arguments.getString("albumId")?.toLongOrNull()
                ?: arguments.getLong("albumId").takeIf { it > 0L }
            else -> null
        }
    }
    var detailExpandOrigin by remember { mutableStateOf(ExpandOrigin()) }
    var detailRouteTransitionMode by remember { mutableStateOf(DetailRouteTransitionMode.TileExpand) }
    var nowPlayingTransitionSnapshot by remember { mutableStateOf<NowPlayingTransitionSnapshot?>(null) }
    var isPlayerOverlayVisible by rememberSaveable { mutableStateOf(false) }
    var lastPlayerOpenRequestAt by remember { mutableLongStateOf(0L) }
    var isSearchQueryActive by rememberSaveable { mutableStateOf(false) }
    var browsingOriginRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var selectedBottomRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var homeScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    val showTopLevelChrome = currentRoute in setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE, SEARCH_ROUTE)
    val showBottomNavigation = currentRoute in setOf(
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
    LaunchedEffect(currentRoute) {
        if (currentRoute in setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE, SEARCH_ROUTE)) {
            browsingOriginRoute = currentRoute.orEmpty()
            selectedBottomRoute = currentRoute.orEmpty()
        }
    }
    val activeBottomRoute = selectedBottomRoute
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val hideCompactNowPlaying = (keyboardVisible && currentRoute == PLAYLISTS_ROUTE) ||
        (currentRoute == SEARCH_ROUTE && isSearchQueryActive)
    val reserveCompactNowPlayingSpace = playbackState.currentSong != null && !hideCompactNowPlaying
    val canHostCompactNowPlaying = playbackState.currentSong != null
    val showGlobalNowPlaying = canHostCompactNowPlaying && !hideCompactNowPlaying && !isPlayerOverlayVisible
    val reenteringFromPlayer = false
    val overscrollFactory = rememberElovaireOverscrollFactory()
    val navHostBlur = 0.dp
    val navHostScrimAlpha = 0f
    val rootView = LocalView.current
    val appBackground = MaterialTheme.colorScheme.background
    val darkTheme = appBackground.luminance() < 0.5f
    val chromeHazeState = rememberHazeState()
    val sharedTopBarController = remember { SharedTopBarController() }
    val sharedBackIconPainter = painterResource(id = R.drawable.ic_lucide_chevron_left)
    val sharedTopMenuIconPainter = painterResource(id = R.drawable.ic_lucide_menu)
    var showTopBarMenu by rememberSaveable { mutableStateOf(false) }
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    var showPlaylistCreateDialog by rememberSaveable { mutableStateOf(false) }
    val playerArtworkGradient = rememberArtworkGradient(playbackState.currentSong?.artUri).value
    val playerAdaptivePalette = remember(
        playbackState.currentSong?.id,
        playerArtworkGradient,
        darkTheme,
        appBackground,
    ) {
        buildPlayerAdaptivePalette(
            gradient = playerArtworkGradient,
            appBackground = appBackground,
            darkTheme = darkTheme,
        )
    }
    val openPlayerIfAllowed: (NowPlayingTransitionSnapshot?) -> Unit = { snapshot ->
        val now = System.currentTimeMillis()
        if (now - lastPlayerOpenRequestAt > 450L) {
            lastPlayerOpenRequestAt = now
            nowPlayingTransitionSnapshot = snapshot
            isPlayerOverlayVisible = true
        }
    }
    val openCurrentPlayingAlbum: (Long) -> Unit = { albumId ->
        val sameAlbumAlreadyVisible =
            currentRoute == "$ALBUM_ROUTE/{albumId}" && currentAlbumRouteId == albumId
        isPlayerOverlayVisible = false
        if (!sameAlbumAlreadyVisible) {
            navController.navigate("$ALBUM_ROUTE/$albumId")
        }
    }
    val openSettingsFromMenu = remember(navController) {
        {
            showTopBarMenu = false
            navController.navigate(SETTINGS_ROUTE)
        }
    }
    val openEqualizerFromMenu = remember(navController) {
        {
            showTopBarMenu = false
            navController.navigate(EQUALIZER_ROUTE)
        }
    }
    val openChangelogSheetFromMenu = remember {
        {
            showTopBarMenu = false
            showChangelogSheet = true
        }
    }
    val openAboutFromMenu = remember(navController) {
        {
            showTopBarMenu = false
            navController.navigate(ABOUT_ROUTE)
        }
    }
    val showPlaylistCreateAction = currentRoute == PLAYLISTS_ROUTE && playlists.isNotEmpty()
    val sharedTopBarSpec = sharedTopBarController.registration?.spec
        ?: if (showTopLevelChrome) {
            SharedTopBarSpec.Unified(
                title = topBarTitle(currentRoute),
                showSettings = currentRoute in setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE),
                supplementalActionIconResId = if (showPlaylistCreateAction) R.drawable.ic_lucide_list_plus else null,
                supplementalActionContentDescription = if (showPlaylistCreateAction) "Create playlist" else null,
                onSupplementalAction = if (showPlaylistCreateAction) {
                    { showPlaylistCreateDialog = true }
                } else {
                    null
                },
                onOpenMenu = { showTopBarMenu = true },
            )
        } else {
            null
        }
    var lastHandledOpenPlayerRequest by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(openPlayerRequestVersion) {
        if (openPlayerRequestVersion > 0L && openPlayerRequestVersion != lastHandledOpenPlayerRequest) {
            lastHandledOpenPlayerRequest = openPlayerRequestVersion
            openPlayerIfAllowed(null)
        }
    }
    LaunchedEffect(isPlayerOverlayVisible) {
        if (!isPlayerOverlayVisible) {
            nowPlayingTransitionSnapshot = null
        }
    }
    LaunchedEffect(currentRoute) {
        showTopBarMenu = false
        if (currentRoute != PLAYLISTS_ROUTE) {
            showPlaylistCreateDialog = false
        }
    }
    SideEffect {
        val window = (rootView.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, rootView)
        val usesLightSystemBarIcons = if (isPlayerOverlayVisible) {
            playerAdaptivePalette.contentColor.luminance() < 0.56f
        } else {
            !darkTheme
        }
        controller.isAppearanceLightStatusBars = usesLightSystemBarIcons
        controller.isAppearanceLightNavigationBars = usesLightSystemBarIcons
    }

    val songMenuActions = remember(playlists) {
        SongMenuActions(
            playlists = playlists.filterNot { it.isSystem },
            onAddToPlaylist = { playlistId, song ->
                container.preferenceStore.addSongsToPlaylist(playlistId, listOf(song.id))
            },
            onAddToQueue = { song ->
                container.playbackManager.enqueueSong(song)
            },
            onDeleteFromLibrary = { song ->
                rootScope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.delete(song.uri, null, null)
                        }
                    }.onSuccess {
                        container.preferenceStore.removeSongReferences(song.id)
                        container.libraryRepository.refresh(
                            forceMediaIndex = true,
                            showLoadingIndicator = false,
                        )
                    }.onFailure { throwable ->
                        val intentSender = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                MediaStore.createDeleteRequest(
                                    context.contentResolver,
                                    listOf(song.uri),
                                ).intentSender
                            }
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && throwable is RecoverableSecurityException -> {
                                throwable.userAction.actionIntent.intentSender
                            }
                            else -> null
                        }
                        if (intentSender != null) {
                            pendingDeleteSong = song
                            deleteSongLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build(),
                            )
                        }
                    }
                }
            },
        )
    }

    CompositionLocalProvider(
        LocalOverscrollFactory provides overscrollFactory,
        LocalSongMenuActions provides songMenuActions,
        LocalChromeHazeState provides chromeHazeState,
        LocalSharedBackIconPainter provides sharedBackIconPainter,
        LocalSharedTopMenuIconPainter provides sharedTopMenuIconPainter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
            val topBarHeight = topBarOccupiedHeight()
            val detailTopBarHeight = detailTopBarOccupiedHeight()
            val sharedTopBarHeight = sharedTopBarOccupiedHeight()
            val bottomNavHeight = if (showBottomNavigation) bottomNavigationOccupiedHeight() else 0.dp
            val showSharedTopBarBackdrop = currentRoute != null && currentRoute != PLAYER_ROUTE
            val topContentPadding = if (showTopLevelChrome) {
                topBarHeight + ElovaireSpacing.topBarToFirstContentGap
            } else {
                innerPadding.calculateTopPadding()
            }
            val bottomContentPadding =
                bottomNavHeight +
                    (if (reserveCompactNowPlayingSpace) ElovaireSpacing.miniPlayerReservedHeight else 0.dp) +
                    ElovaireSpacing.scrollTailPadding
            val detailBottomPadding =
                bottomNavHeight +
                    (if (reserveCompactNowPlayingSpace) ElovaireSpacing.miniPlayerReservedHeight else 0.dp) +
                    ElovaireSpacing.scrollTailPadding

            Box(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalUseSharedTopBarBackdrop provides showSharedTopBarBackdrop,
                    LocalSharedTopBarController provides sharedTopBarController,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(chromeHazeState),
                    ) {
                    NavHost(
                        navController = navController,
                        startDestination = HOME_ROUTE,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(navHostBlur),
                        enterTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        if (targetRoute == PLAYER_ROUTE) {
                            EnterTransition.None
                        } else if (targetRoute == CHANGELOG_ROUTE) {
                            fadeIn(
                                animationSpec = ElovaireMotion.fadeMedium(),
                            ) +
                                slideInHorizontally(
                                    animationSpec = ElovaireMotion.offsetSoft(),
                                    initialOffsetX = { -(it / 5) },
                                ) +
                                scaleIn(
                                    animationSpec = ElovaireMotion.scaleSoft(),
                                    initialScale = 0.994f,
                                )
                        } else if (
                            ElovaireNavigationTransitions.usesTileExpand(
                                route = targetRoute,
                                mode = detailRouteTransitionMode,
                            )
                        ) {
                            fadeIn(
                                animationSpec = ElovaireMotion.fadeMedium(),
                            ) +
                                scaleIn(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    initialScale = 0.8f,
                                    transformOrigin = detailExpandOrigin.toTransformOrigin(),
                                ) +
                                slideInHorizontally(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    initialOffsetX = { fullWidth ->
                                        ((detailExpandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
                                    },
                                ) +
                                slideInVertically(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    initialOffsetY = { fullHeight ->
                                        ((detailExpandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
                                    },
                                )
                        } else if (
                            ElovaireNavigationTransitions.isLibraryCategoryEnterTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            ElovaireMotion.fullScreenForwardEnter()
                        } else if (ElovaireNavigationTransitions.isUtilityScreenRoute(targetRoute)) {
                            ElovaireMotion.fullScreenForwardEnter()
                        } else if (
                            ElovaireNavigationTransitions.isTopLevelRouteTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            if (
                                ElovaireNavigationTransitions.isForwardTopLevelRouteTransition(
                                    initialRoute = initialRoute,
                                    targetRoute = targetRoute,
                                )
                            ) {
                                ElovaireMotion.fullScreenForwardEnter()
                            } else {
                                ElovaireMotion.fullScreenBackEnter()
                            }
                        } else if (
                            ElovaireNavigationTransitions.isSameLevelTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            fadeIn(
                                animationSpec = ElovaireMotion.fadeMedium(),
                            ) +
                                scaleIn(
                                    animationSpec = ElovaireMotion.scaleSoft(),
                                    initialScale = 0.996f,
                                )
                        } else {
                            ElovaireMotion.fullScreenForwardEnter()
                        }
                    },
                    exitTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        if (targetRoute == PLAYER_ROUTE) {
                            ExitTransition.None
                        } else if (
                            ElovaireNavigationTransitions.usesTileExpand(
                                route = targetRoute,
                                mode = detailRouteTransitionMode,
                            )
                        ) {
                            fadeOut(
                                animationSpec = ElovaireMotion.fadeFast(),
                            )
                        } else if (
                            ElovaireNavigationTransitions.isLibraryCategoryEnterTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            ElovaireMotion.fullScreenForwardExit()
                        } else if (
                            ElovaireNavigationTransitions.isTopLevelRouteTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            if (
                                ElovaireNavigationTransitions.isForwardTopLevelRouteTransition(
                                    initialRoute = initialRoute,
                                    targetRoute = targetRoute,
                                )
                            ) {
                                ElovaireMotion.fullScreenForwardExit()
                            } else {
                                ElovaireMotion.fullScreenBackExit()
                            }
                        } else if (
                            ElovaireNavigationTransitions.isSameLevelTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            fadeOut(animationSpec = ElovaireMotion.fadeMedium()) +
                                scaleOut(
                                    animationSpec = ElovaireMotion.fadeMedium(),
                                    targetScale = 0.996f,
                                )
                        } else {
                            ElovaireMotion.fullScreenForwardExit()
                        }
                    },
                    popEnterTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        if (initialRoute == PLAYER_ROUTE) {
                            EnterTransition.None
                        } else if (
                            ElovaireNavigationTransitions.usesTileExpand(
                                route = initialRoute,
                                mode = detailRouteTransitionMode,
                            )
                        ) {
                            fadeIn(
                                animationSpec = ElovaireMotion.fadeMedium(),
                            )
                        } else if (
                            ElovaireNavigationTransitions.isLibraryCategoryReturnTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            ElovaireMotion.fullScreenBackEnter()
                        } else if (ElovaireNavigationTransitions.isUtilityScreenRoute(targetRoute)) {
                            ElovaireMotion.fullScreenBackEnter()
                        } else if (
                            ElovaireNavigationTransitions.isTopLevelRouteTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            if (
                                ElovaireNavigationTransitions.isForwardTopLevelRouteTransition(
                                    initialRoute = initialRoute,
                                    targetRoute = targetRoute,
                                )
                            ) {
                                ElovaireMotion.fullScreenForwardEnter()
                            } else {
                                ElovaireMotion.fullScreenBackEnter()
                            }
                        } else if (
                            ElovaireNavigationTransitions.isSameLevelTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            fadeIn(
                                animationSpec = ElovaireMotion.fadeMedium(),
                            ) +
                                scaleIn(
                                    animationSpec = ElovaireMotion.scaleSoft(),
                                    initialScale = 0.996f,
                                )
                        } else {
                            ElovaireMotion.fullScreenBackEnter()
                        }
                    },
                    popExitTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        if (initialRoute == PLAYER_ROUTE) {
                            ExitTransition.None
                        } else if (initialRoute == CHANGELOG_ROUTE) {
                            fadeOut(animationSpec = ElovaireMotion.fadeMedium()) +
                                slideOutHorizontally(
                                    animationSpec = ElovaireMotion.offsetSoft(),
                                    targetOffsetX = { -(it / 5) },
                                ) +
                                scaleOut(
                                    animationSpec = ElovaireMotion.fadeMedium(),
                                    targetScale = 0.994f,
                                )
                        } else if (
                            ElovaireNavigationTransitions.usesTileExpand(
                                route = initialRoute,
                                mode = detailRouteTransitionMode,
                            )
                        ) {
                            fadeOut(
                                animationSpec = ElovaireMotion.fadeFast(),
                            ) +
                                scaleOut(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    targetScale = 0.84f,
                                    transformOrigin = detailExpandOrigin.toTransformOrigin(),
                                ) +
                                slideOutHorizontally(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    targetOffsetX = { fullWidth ->
                                        ((detailExpandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
                                    },
                                ) +
                                slideOutVertically(
                                    animationSpec = ElovaireMotion.emphasizedEnterSpec(),
                                    targetOffsetY = { fullHeight ->
                                        ((detailExpandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
                                    },
                                )
                        } else if (
                            ElovaireNavigationTransitions.isLibraryCategoryReturnTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            ElovaireMotion.fullScreenBackExit()
                        } else if (ElovaireNavigationTransitions.isUtilityScreenRoute(initialRoute)) {
                            ElovaireMotion.fullScreenBackExit()
                        } else if (
                            ElovaireNavigationTransitions.isTopLevelRouteTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            if (
                                ElovaireNavigationTransitions.isForwardTopLevelRouteTransition(
                                    initialRoute = initialRoute,
                                    targetRoute = targetRoute,
                                )
                            ) {
                                ElovaireMotion.fullScreenForwardExit()
                            } else {
                                ElovaireMotion.fullScreenBackExit()
                            }
                        } else if (
                            ElovaireNavigationTransitions.isSameLevelTransition(
                                initialRoute = initialRoute,
                                targetRoute = targetRoute,
                            )
                        ) {
                            fadeOut(animationSpec = ElovaireMotion.fadeMedium()) +
                                scaleOut(
                                    animationSpec = ElovaireMotion.fadeMedium(),
                                    targetScale = 0.996f,
                                )
                        } else {
                            ElovaireMotion.fullScreenBackExit()
                        }
                    },
                ) {
                    composable(HOME_ROUTE) {
                        val recentSongs = remember(songsById, playbackState.recentSongIds) {
                            playbackState.recentSongIds.mapNotNull(songsById::get).take(5)
                        }
                        HomeScreen(
                            lastPlayedAlbum = recentAlbums.firstOrNull(),
                            recentlyAddedAlbums = recentlyAddedAlbums,
                            recentSongs = recentSongs,
                            favoriteAlbums = favoriteAlbums,
                            playbackState = playbackState,
                            isLibraryLoading = libraryState.isLoading,
                            libraryScanProgress = libraryState.scanProgress,
                            favoriteSongIds = favoriteSongIdSet,
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            scrollToTopRequestVersion = homeScrollRequestVersion,
                            playInitialReveal = playFirstLaunchHomeReveal,
                            onInitialRevealFinished = {
                                playFirstLaunchHomeReveal = false
                                firstLaunchPermissionExperienceActive = false
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onPlayAlbum = { album ->
                                container.playbackManager.playAlbum(album)
                            },
                            onSongSelected = { song ->
                                val sourceAlbum = albumsById[song.albumId]
                                if (sourceAlbum != null) {
                                    container.playbackManager.playAlbum(
                                        album = sourceAlbum,
                                        startSongId = song.id,
                                        sourceLabel = sourceAlbum.title,
                                    )
                                } else {
                                    val albumSongs = songsByAlbumId[song.albumId].orEmpty()
                                    container.playbackManager.playSong(
                                        song = song,
                                        collection = albumSongs.ifEmpty { listOf(song) },
                                        sourceLabel = song.album,
                                    )
                                }
                            },
                            onToggleFavorite = { songId ->
                                container.preferenceStore.toggleFavoriteSong(songId)
                            },
                        )
                    }

                    composable(ALBUMS_ROUTE) {
                        LibraryHubScreen(
                            libraryState = libraryState,
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            onOpenCollection = { kind ->
                                navController.navigate("$LIBRARY_COLLECTION_ROUTE/${kind.name}")
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                        )
                    }

                    composable(PLAYLISTS_ROUTE) {
                        PlaylistsScreen(
                            playlists = playlists,
                            libraryState = libraryState,
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            onRequestCreatePlaylist = { showPlaylistCreateDialog = true },
                            onRenamePlaylist = container.preferenceStore::renamePlaylist,
                            onDeletePlaylists = container.preferenceStore::deletePlaylists,
                            onOpenPlaylist = { playlist, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
                                navController.navigate("$PLAYLIST_ROUTE/${playlist.id}")
                            },
                        )
                    }

                    composable(SEARCH_ROUTE) {
                        SearchScreen(
                            libraryState = libraryState,
                            playbackState = playbackState,
                            albumPlayCounts = albumPlayCounts,
                            recentSearches = searchHistory,
                            favoriteSongIds = favoriteSongIdSet,
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            onSearchQueryActiveChanged = { isSearchQueryActive = it },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = queue.playbackSourceLabel(fallbackAlbum = song.album),
                                )
                                openPlayerIfAllowed(null)
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onArtistSelected = { artistName ->
                                navController.navigate("$ARTIST_ROUTE/${Uri.encode(artistName)}")
                            },
                            onRememberAlbumSearch = { album ->
                                container.preferenceStore.addSearchHistoryEntry(albumSearchHistoryEntry(album))
                            },
                            onRememberArtistSearch = { song ->
                                container.preferenceStore.addSearchHistoryEntry(artistSearchHistoryEntry(song))
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                            onClearSearchHistory = {
                                container.preferenceStore.clearSearchHistory()
                            },
                        )
                    }

                    composable(
                        route = "$PLAYLIST_ROUTE/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                        val playlist = playlists.firstOrNull { it.id == playlistId }
                        PlaylistDetailScreen(
                            playlist = playlist,
                            librarySongs = libraryState.songs,
                            favoriteSongIds = favoriteSongIdSet,
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = isPlaybackActuallyPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onPlayPlaylist = { songs, sourceLabel ->
                                songs.firstOrNull()?.let { firstSong ->
                                    container.playbackManager.playSong(
                                        song = firstSong,
                                        collection = songs,
                                        sourceLabel = sourceLabel,
                                    )
                                    openPlayerIfAllowed(null)
                                }
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = playlist?.name ?: queue.playbackSourceLabel(fallbackAlbum = song.album),
                                )
                                openPlayerIfAllowed(null)
                            },
                            onAddSongs = { songIds ->
                                container.preferenceStore.addSongsToPlaylist(playlistId, songIds)
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                        )
                    }

                    composable(
                        route = "$ALBUM_ROUTE/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
                        val album = libraryState.albums.firstOrNull { it.id == albumId }
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        AlbumScreen(
                            album = album,
                            favoriteSongIds = favoriteSongIdSet,
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = isPlaybackActuallyPlaying,
                            bottomPadding = detailBottomPadding,
                            collapsedTopBarTitle = detailFallbackTitle(previousRoute),
                            onBack = navController::navigateUp,
                            onPlayAlbum = { selectedAlbum ->
                                container.playbackManager.playAlbum(
                                    album = selectedAlbum,
                                    shuffleEnabled = false,
                                )
                            },
                            onShuffleAlbum = { selectedAlbum ->
                                container.playbackManager.playAlbum(
                                    album = selectedAlbum,
                                    shuffleEnabled = true,
                                )
                            },
                            onSongSelected = { selectedSong, songs ->
                                container.playbackManager.playSong(
                                    song = selectedSong,
                                    collection = songs,
                                    sourceLabel = album?.title ?: selectedSong.album,
                                )
                                openPlayerIfAllowed(null)
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                            onSetAlbumFavorite = { songIds, favorite ->
                                container.preferenceStore.setFavoriteSongs(songIds, favorite)
                            },
                        )
                    }

                    composable(
                        route = "$LIBRARY_COLLECTION_ROUTE/{kind}",
                        arguments = listOf(navArgument("kind") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val kindArg = backStackEntry.arguments?.getString("kind")
                        val kind = kindArg?.let { runCatching { LibraryCollectionKind.valueOf(it) }.getOrNull() }
                            ?: LibraryCollectionKind.Albums
                        LibraryCollectionScreen(
                            kind = kind,
                            libraryState = libraryState,
                            songPlayCounts = songPlayCounts,
                            favoriteSongIds = favoriteSongIdSet,
                            albumCollectionLayoutMode = if (albumCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
                            songCollectionLayoutMode = if (songCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
                            albumSortMode = albumCollectionSortModeName.toAlbumSortMode(),
                            songSortMode = songCollectionSortModeName.toSongSortMode(),
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = isPlaybackActuallyPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = if (kind == LibraryCollectionKind.Songs) {
                                        "all songs"
                                    } else {
                                        queue.playbackSourceLabel(fallbackAlbum = song.album)
                                    },
                                )
                                openPlayerIfAllowed(null)
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                            onAlbumCollectionLayoutModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionGridEnabled(mode == AlbumLayoutMode.Grid)
                            },
                            onSongCollectionLayoutModeChanged = { mode ->
                                container.preferenceStore.setSongCollectionGridEnabled(mode == AlbumLayoutMode.Grid)
                            },
                            onAlbumSortModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionSortMode(mode.name)
                            },
                            onSongSortModeChanged = { mode ->
                                container.preferenceStore.setSongCollectionSortMode(mode.name)
                            },
                            onGenreSelected = { genre ->
                                navController.navigate("$GENRE_ROUTE/${Uri.encode(genre)}")
                            },
                            onArtistSelected = { artistName ->
                                navController.navigate("$ARTIST_ROUTE/${Uri.encode(artistName)}")
                            },
                        )
                    }

                    composable(
                        route = "$GENRE_ROUTE/{genre}",
                        arguments = listOf(navArgument("genre") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val genre = backStackEntry.arguments?.getString("genre")?.let(Uri::decode).orEmpty()
                        GenreAlbumsScreen(
                            genre = genre,
                            libraryState = libraryState,
                            layoutMode = if (albumCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
                            sortMode = albumCollectionSortModeName.toAlbumSortMode(),
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onLayoutModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionGridEnabled(mode == AlbumLayoutMode.Grid)
                            },
                            onSortModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionSortMode(mode.name)
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                        )
                    }

                    composable(
                        route = "$ARTIST_ROUTE/{artistName}",
                        arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val artistName = backStackEntry.arguments?.getString("artistName")?.let(Uri::decode).orEmpty()
                        ArtistDetailScreen(
                            artistName = artistName,
                            libraryState = libraryState,
                            songPlayCounts = songPlayCounts,
                            favoriteSongIds = favoriteSongIdSet,
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = isPlaybackActuallyPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(song, queue, sourceLabel = artistName)
                                openPlayerIfAllowed(null)
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                        )
                    }

                    composable(EQUALIZER_ROUTE) {
                        EqualizerScreen(
                            settings = eqSettings,
                            onBack = navController::navigateUp,
                            onBandChanged = container.preferenceStore::updateBand,
                            onBassChanged = container.preferenceStore::updateBass,
                            onTrebleChanged = container.preferenceStore::updateTreble,
                            onSpaciousnessChanged = container.preferenceStore::updateSpaciousness,
                            onSpaciousnessModeChanged = container.preferenceStore::updateSpaciousnessMode,
                            onApplyPreset = container.preferenceStore::setEqSettings,
                            onReset = container.preferenceStore::resetEqSettings,
                        )
                    }

                    composable(SETTINGS_ROUTE) {
                        SettingsScreen(
                            themeMode = themeMode,
                            textSizePreset = textSizePreset,
                            eqSettings = eqSettings,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onThemeModeSelected = container.preferenceStore::setThemeMode,
                            onTextSizePresetSelected = container.preferenceStore::setTextSizePreset,
                            onBassChanged = container.preferenceStore::updateBass,
                            onSpaciousnessChanged = container.preferenceStore::updateSpaciousness,
                            onMonoPlaybackChanged = container.preferenceStore::updateMonoPlaybackEnabled,
                            onOpenEqualizer = { navController.navigate(EQUALIZER_ROUTE) },
                            onOpenChangelog = { navController.navigate(CHANGELOG_ROUTE) },
                            onScanLibrary = {
                                container.libraryRepository.refresh(
                                    forceMediaIndex = true,
                                    enrichMetadata = true,
                                    showLoadingIndicator = true,
                                )
                            },
                            onCheckForUpdates = {
                                container.appUpdateManager.checkForUpdates(force = true)
                            },
                        )
                    }

                    composable(CHANGELOG_ROUTE) {
                        ChangelogScreen(
                            releases = changelogReleases,
                            onBack = navController::navigateUp,
                        )
                    }

                    composable(ABOUT_ROUTE) {
                        AboutScreen(
                            onBack = navController::navigateUp,
                            bottomPadding = detailBottomPadding,
                        )
                    }
                }
                    if (navHostScrimAlpha > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = navHostScrimAlpha)),
                        )
                    }
                    }
                    if (showSharedTopBarBackdrop && sharedTopBarSpec != null) {
                        FrostedTopBarBackground(
                            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(sharedTopBarHeight)
                                .zIndex(7f),
                        )
                    }
                    if (sharedTopBarSpec != null) {
                        CompositionLocalProvider(LocalRenderSharedTopBarContent provides true) {
                            SharedTopBarOverlay(
                                spec = sharedTopBarSpec,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .zIndex(9f),
                            )
                        }
                    }
                    ElovaireAnimatedVisibility(
                        visible = showTopBarMenu,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f),
                        enter = fadeIn(animationSpec = ElovaireMotion.fadeFast()),
                        exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                        label = "TopBarContextMenuOverlay",
                    ) {
                        TopBarContextMenuOverlay(
                            onDismiss = { showTopBarMenu = false },
                            onOpenSettings = openSettingsFromMenu,
                            onOpenEqualizer = openEqualizerFromMenu,
                            onOpenChangelog = openChangelogSheetFromMenu,
                            onOpenAbout = openAboutFromMenu,
                        )
                    }
                    ElovaireAnimatedVisibility(
                        visible = showChangelogSheet,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(11f),
                        enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                        exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                        label = "ChangelogSheetOverlay",
                    ) {
                        ChangelogBottomSheetOverlay(
                            releases = changelogReleases,
                            onDismiss = { showChangelogSheet = false },
                        )
                    }
                    if (showPlaylistCreateDialog) {
                        PlaylistNameDialog(
                            onDismiss = { showPlaylistCreateDialog = false },
                            onConfirm = { name ->
                                val createdId = container.preferenceStore.createPlaylist(name)
                                if (createdId > 0L) {
                                    showPlaylistCreateDialog = false
                                }
                            },
                        )
                    }
                    ElovaireAnimatedVisibility(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(7f)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = topBarHeight + 8.dp,
                            ),
                        visible = showTopLevelChrome && appUpdateState.availableRelease != null,
                        enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                            slideInVertically(
                                animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Spacious),
                                initialOffsetY = { -(it / 2) },
                            ),
                        exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                            slideOutVertically(
                                animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                targetOffsetY = { -(it / 3) },
                            ),
                        label = "UpdateBannerVisibility",
                    ) {
                        appUpdateState.availableRelease?.let { release ->
                            UpdateAvailableBanner(
                                release = release,
                                uiState = appUpdateState,
                                onDismiss = container.appUpdateManager::dismissAvailableUpdate,
                                onUpdate = container.appUpdateManager::startUpdate,
                            )
                        }
                    }
                    ElovaireAnimatedVisibility(
                        visible = showFirstLaunchPermissionOverlay,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(9f),
                        enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                        exit = fadeOut(animationSpec = ElovaireMotion.fadeSlow()),
                        label = "FirstLaunchPermissionOverlayVisibility",
                    ) {
                        FirstLaunchPermissionLoadingScreen(
                            showLoading = true,
                            onRequestPermission = { permissionLauncher.launch(audioPermission()) },
                        )
                    }
                }
                if (canHostCompactNowPlaying) {
                    playbackState.currentSong?.let { currentSong ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(7f)
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = if (showBottomNavigation) bottomNavHeight + 8.dp else navigationBarInsetDp() + 10.dp,
                                ),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            CompactNowPlayingDockHost(
                                playbackManager = container.playbackManager,
                                playbackState = playbackState,
                                song = currentSong,
                                visible = showGlobalNowPlaying,
                                suppressEnterAnimation = reenteringFromPlayer,
                                onOpenPlayer = openPlayerIfAllowed,
                                onTogglePlayback = container.playbackManager::togglePlayback,
                                onSkipPrevious = container.playbackManager::skipPrevious,
                                onSkipNext = container.playbackManager::skipNext,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                ElovaireAnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(8f)
                        .fillMaxWidth(),
                    visible = showBottomNavigation,
                    enter = if (reenteringFromPlayer) {
                        EnterTransition.None
                    } else {
                        fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                            slideInVertically(
                                animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                initialOffsetY = { it / 2 },
                            )
                    },
                    exit = fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec()) +
                        slideOutVertically(
                            animationSpec = ElovaireMotion.fadeFast(),
                            targetOffsetY = { it / 2 },
                        ),
                    label = "BottomNavigationVisibility",
                ) {
                    BottomNavigationBar(
                        currentRoute = activeBottomRoute,
                        suppressEnterAnimation = reenteringFromPlayer,
                        destinations = topLevelDestinations,
                        onNavigate = { route ->
                            if (route == HOME_ROUTE && currentRoute == HOME_ROUTE) {
                                homeScrollRequestVersion += 1L
                            }
                            val shouldReturnToTabRoot = route == activeBottomRoute && currentRoute != route
                            browsingOriginRoute = route
                            selectedBottomRoute = route
                            if (shouldReturnToTabRoot) {
                                val poppedToTabRoot = navController.popBackStack(route, inclusive = false)
                                if (!poppedToTabRoot) {
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }
                                }
                            } else if (currentRoute != route) {
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            }
            if (isPlayerOverlayVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .zIndex(20f),
                ) {
                    NowPlayingScreen(
                        playbackManager = container.playbackManager,
                        playbackState = playbackState,
                        enrichedSongsById = songsById,
                        isFavorite = playbackState.currentSong?.id in favoriteSongIdSet,
                        lyricsService = lyricsService,
                        onBack = { isPlayerOverlayVisible = false },
                        onOpenCurrentAlbum = openCurrentPlayingAlbum,
                        onTogglePlayback = container.playbackManager::togglePlayback,
                        onSkipPrevious = container.playbackManager::skipPrevious,
                        onSkipNext = container.playbackManager::skipNext,
                        onCycleRepeatMode = container.playbackManager::cycleRepeatMode,
                        onToggleShuffle = container.playbackManager::toggleShuffle,
                        onToggleFavorite = { songId -> container.preferenceStore.toggleFavoriteSong(songId) },
                        onQueueItemSelected = container.playbackManager::playQueueIndex,
                        eqSettings = eqSettings,
                        onSpaciousnessChanged = container.preferenceStore::updateSpaciousness,
                        onVolumeChanged = { volume ->
                            container.playbackManager.setVolume(volume)
                        },
                        transitionSnapshot = nowPlayingTransitionSnapshot,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactNowPlayingDockHost(
    playbackManager: PlaybackManager,
    playbackState: PlaybackUiState,
    song: Song,
    visible: Boolean,
    suppressEnterAnimation: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playbackProgress by playbackManager.progressState.collectAsStateWithLifecycle()
    val transportShowsPause = remember(playbackState.currentSong?.id, playbackState.transportShowsPause, song.id) {
        playbackState.currentSong?.id == song.id && playbackState.transportShowsPause
    }
    val progress = remember(playbackProgress.displayPositionMs, playbackProgress.durationMs) {
        if (playbackProgress.durationMs > 0L) {
            (playbackProgress.displayPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    StandaloneNowPlayingDock(
        song = song,
        isPlaying = transportShowsPause,
        progress = progress,
        visible = visible,
        suppressEnterAnimation = suppressEnterAnimation,
        onOpenPlayer = onOpenPlayer,
        onTogglePlayback = onTogglePlayback,
        onSkipPrevious = onSkipPrevious,
        onSkipNext = onSkipNext,
        modifier = modifier,
    )
}

@Composable
private fun StandaloneNowPlayingDock(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    visible: Boolean,
    suppressEnterAnimation: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artwork = rememberArtworkBitmap(song.artUri, size = 768)
    val gradient = rememberArtworkGradient(song.artUri).value
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseTint = if (darkTheme) Color(0xFF141414).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.82f)
    val albumTint = gradient.first().copy(alpha = 0.5f)
    val resolvedSurface = albumTint.compositeOver(baseTint)
    val contentColor = if (resolvedSurface.luminance() > 0.42f) InkText else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.72f)
    Box(
        modifier = modifier.graphicsLayer {
            alpha = if (visible) 1f else 0f
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ElovaireRadii.card))
                .background(baseTint)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.05f else 0.04f),
                    shape = RoundedCornerShape(ElovaireRadii.card),
                ),
        ) {
            val artworkBitmap = artwork.value
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .blur(48.dp),
                    alpha = 0.9f,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(albumTint),
            )
            NowPlayingBar(
                song = song,
                isPlaying = isPlaying,
                progress = progress,
                visible = visible,
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onOpenPlayer = onOpenPlayer,
                onTogglePlayback = onTogglePlayback,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
            )
        }
    }
}

@Composable
private fun UnifiedTopBar(
    title: String,
    showSettings: Boolean,
    @DrawableRes supplementalActionIconResId: Int? = null,
    supplementalActionContentDescription: String? = null,
    onSupplementalAction: (() -> Unit)? = null,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useSharedBackdrop = LocalUseSharedTopBarBackdrop.current
    if (useSharedBackdrop && !LocalRenderSharedTopBarContent.current) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Unified(
                title = title,
                showSettings = showSettings,
                supplementalActionIconResId = supplementalActionIconResId,
                supplementalActionContentDescription = supplementalActionContentDescription,
                onSupplementalAction = onSupplementalAction,
                onOpenMenu = onOpenMenu,
            ),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (useSharedBackdrop) 8f else 0f)
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
        if (!useSharedBackdrop) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 16.dp, top = 3.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .weight(1f)
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            if (showSettings) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (supplementalActionIconResId != null && onSupplementalAction != null) {
                        HeaderIconButton(
                            iconResId = supplementalActionIconResId,
                            contentDescription = supplementalActionContentDescription ?: "Action",
                            showBackground = false,
                            onClick = onSupplementalAction,
                            modifier = Modifier.zIndex(1f),
                        )
                    }
                    HeaderIconButton(
                        iconResId = R.drawable.ic_lucide_menu,
                        contentDescription = "Menu",
                        showBackground = false,
                        onClick = onOpenMenu,
                        modifier = Modifier.zIndex(1f),
                    )
                }
            } else {
                SpacerTile(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun PinnedBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    centeredTitle: Boolean = false,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useSharedBackdrop = LocalUseSharedTopBarBackdrop.current
    if (useSharedBackdrop && !LocalRenderSharedTopBarContent.current) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Back(
                title = title,
                onBack = onBack,
                centeredTitle = centeredTitle,
            ),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (useSharedBackdrop) 8f else 0f)
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
        if (!useSharedBackdrop) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        if (centeredTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp)
                    .height(40.dp),
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(1f),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(1f)
                        .padding(horizontal = 64.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                    modifier = Modifier.zIndex(1f),
                )
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .weight(1f)
                        .height(40.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SharedTopBarOverlay(
    spec: SharedTopBarSpec,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
        when (spec) {
            is SharedTopBarSpec.Unified -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 16.dp, top = 3.dp, bottom = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        ElovaireAnimatedContent(
                            targetState = spec.title,
                            transitionSpec = {
                                ElovaireMotion.sharedTopBarTransform()
                            },
                            label = "SharedTopBarUnifiedTitle",
                        ) { currentTitle ->
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                    if (spec.showSettings) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (spec.supplementalActionIconResId != null && spec.onSupplementalAction != null) {
                                HeaderIconButton(
                                    iconResId = spec.supplementalActionIconResId,
                                    contentDescription = spec.supplementalActionContentDescription ?: "Action",
                                    showBackground = false,
                                    onClick = spec.onSupplementalAction,
                                )
                            }
                            HeaderIconButton(
                                iconResId = R.drawable.ic_lucide_menu,
                                contentDescription = "Menu",
                                showBackground = false,
                                onClick = spec.onOpenMenu,
                            )
                        }
                    } else {
                        SpacerTile(modifier = Modifier.size(40.dp))
                    }
                }
            }

            is SharedTopBarSpec.Back -> {
                if (spec.centeredTitle) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp)
                            .height(40.dp),
                    ) {
                        HeaderIconButton(
                            iconResId = R.drawable.ic_lucide_chevron_left,
                            contentDescription = "Back",
                            showBackground = false,
                            onClick = spec.onBack,
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ElovaireAnimatedContent(
                                targetState = spec.title,
                                transitionSpec = {
                                    ElovaireMotion.sharedTopBarTransform()
                                },
                                label = "SharedTopBarBackCenteredTitle",
                            ) { currentTitle ->
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HeaderIconButton(
                            iconResId = R.drawable.ic_lucide_chevron_left,
                            contentDescription = "Back",
                            showBackground = false,
                            onClick = spec.onBack,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            ElovaireAnimatedContent(
                                targetState = spec.title,
                                transitionSpec = {
                                    ElovaireMotion.sharedTopBarTransform()
                                },
                                label = "SharedTopBarBackTitle",
                            ) { currentTitle ->
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            is SharedTopBarSpec.Detail -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeaderIconButton(
                        iconResId = R.drawable.ic_lucide_chevron_left,
                        contentDescription = "Back",
                        showBackground = false,
                        onClick = spec.onBack,
                    )
                    if (spec.subtitle.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            ElovaireAnimatedContent(
                                targetState = spec.title,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(180, delayMillis = 40))
                                        .togetherWith(fadeOut(animationSpec = tween(140)))
                                },
                                label = "SharedTopBarDetailTitleOnly",
                            ) { currentTitle ->
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            ElovaireAnimatedContent(
                                targetState = spec.title,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(180, delayMillis = 40))
                                        .togetherWith(fadeOut(animationSpec = tween(140)))
                                },
                                label = "SharedTopBarDetailTitleWithSubtitle",
                            ) { currentTitle ->
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = spec.subtitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    iconResId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showBackground: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val sharedBackPainter = LocalSharedBackIconPainter.current
    val sharedTopMenuPainter = LocalSharedTopMenuIconPainter.current
    val iconPainter = when {
        iconResId == R.drawable.ic_lucide_chevron_left && sharedBackPainter != null -> sharedBackPainter
        iconResId == R.drawable.ic_lucide_menu && sharedTopMenuPainter != null -> sharedTopMenuPainter
        else -> painterResource(id = iconResId)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.88f else 1f,
        animationSpec = if (pressed && enabled) {
            ElovaireMotion.pressDownSpec()
        } else {
            ElovaireMotion.releaseSpringSpec(
                dampingRatio = 0.72f,
                stiffness = 420f,
            )
        },
        label = "${contentDescription}_header_scale",
    )
    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (showBackground) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = if (enabled) 0.58f else 0.32f,
                    )
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = tint.copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BottomNavigationBar(
    currentRoute: String,
    suppressEnterAnimation: Boolean,
    destinations: List<TopLevelDestination>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iconColor = if (darkTheme) Color.White else InkText
    val navigationInset = navigationBarInsetDp()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ElovaireSpacing.bottomNavigationBodyHeight + navigationInset),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
        ) {
            BottomNavigationHazeBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ElovaireSpacing.bottomNavigationBodyHeight)
                    .padding(horizontal = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    BottomNavigationItemButton(
                        iconResId = destination.iconResId,
                        contentDescription = destination.contentDescription,
                        baseTint = iconColor,
                        selected = currentRoute == destination.route,
                        onClick = { onNavigate(destination.route) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BottomNavigationHazeBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    FrostedTopBarBackground(
        darkTheme = darkTheme,
        edge = ProgressiveChromeEdge.Bottom,
        overlayAlpha = 0.7f,
        flatOverlay = true,
        showEdgeLine = true,
        modifier = modifier,
    )
}

@Composable
private fun BottomNavigationItemButton(
    iconResId: Int,
    contentDescription: String,
    baseTint: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val selectionTransition = updateTransition(
        targetState = selected,
        label = "BottomNavItemSelection",
    )
    val iconTint by selectionTransition.animateColor(
        transitionSpec = { ElovaireMotion.colorFadeSpec() },
        label = "BottomNavItemIconTint",
    ) { isSelected ->
        if (isSelected) {
            baseTint
        } else {
            baseTint.copy(alpha = 0.5f)
        }
    }
    val pressScale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        if (pressed) {
            pressScale.animateTo(
                targetValue = 0.88f,
                animationSpec = ElovaireMotion.pressDownSpec(),
            )
        } else {
            pressScale.animateTo(
                targetValue = 1f,
                animationSpec = ElovaireMotion.releaseSpringSpec(
                    dampingRatio = 0.78f,
                    stiffness = 520f,
                ),
            )
        }
    }
    val baseIconScale by selectionTransition.animateFloat(
        transitionSpec = {
            ElovaireMotion.releaseSpringSpec<Float>(
            dampingRatio = 0.8f,
            stiffness = 540f,
            )
        },
        label = "BottomNavItemBaseIconScale",
    ) { isSelected -> if (isSelected) 1.14f else 1f }
    val buttonTranslateY by animateDpAsState(
        targetValue = if (pressed) 1.dp else 0.dp,
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.82f,
            stiffness = 560f,
        ),
        label = "bottom_nav_button_translate",
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .offset { IntOffset(x = 0, y = buttonTranslateY.roundToPx()) }
            .scale(pressScale.value)
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .scale(baseIconScale)
                .alpha(if (selected) 1f else 0.95f),
        )
    }
}

@Composable
private fun NowPlayingBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    visible: Boolean,
    contentColor: Color,
    secondaryContentColor: Color,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
) {
    val barGradient = rememberArtworkGradient(song.artUri).value
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val controlBaseTint = if (darkTheme) {
        barGradient.last().copy(alpha = 0.28f).compositeOver(Color.Black.copy(alpha = 0.16f))
    } else {
        barGradient.last().copy(alpha = 0.22f).compositeOver(Color.White.copy(alpha = 0.16f))
    }
    val controlTint by animateColorAsState(
        targetValue = controlBaseTint,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerButtonTint",
    )
    val controlIconTint by animateColorAsState(
        targetValue = if (controlTint.luminance() > 0.42f) InkText else Color.White,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerButtonIconTint",
    )
    val resolvedPrimaryTextColor by animateColorAsState(
        targetValue = controlIconTint,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerTextPrimary",
    )
    val resolvedSecondaryTextColor by animateColorAsState(
        targetValue = controlIconTint.copy(alpha = 0.72f),
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerTextSecondary",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.58f,
            stiffness = 420f,
        ),
        label = "MiniPlayerPlayButtonScale",
    )
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 52.dp.toPx() }
    var dragOffsetX by remember(song.id) { mutableFloatStateOf(0f) }
    var barBounds by remember(song.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var artworkBounds by remember(song.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val animatedDragOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.82f,
            stiffness = 380f,
        ),
        label = "MiniPlayerDragOffsetX",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.card))
            .onGloballyPositioned { barBounds = it.boundsInRoot() }
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.08f else 0.05f),
                shape = RoundedCornerShape(ElovaireRadii.card),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationX = animatedDragOffsetX * 0.18f }
                    .then(
                        if (visible) {
                            Modifier.pointerInput(song.id) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-160f, 160f)
                                    },
                                    onDragEnd = {
                                        when {
                                            dragOffsetX <= -swipeThresholdPx -> onSkipNext()
                                            dragOffsetX >= swipeThresholdPx -> onSkipPrevious()
                                        }
                                        dragOffsetX = 0f
                                    },
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                    },
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        enabled = visible,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val validSnapshot = if (
                                barBounds != null &&
                                artworkBounds != null &&
                                barBounds!!.isValidTransitionBounds &&
                                artworkBounds!!.isValidTransitionBounds
                            ) {
                                NowPlayingTransitionSnapshot(
                                    songId = song.id,
                                    barBounds = barBounds!!,
                                    artworkBounds = artworkBounds!!,
                                )
                            } else {
                                null
                            }
                            onOpenPlayer(
                                validSnapshot,
                            )
                        },
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.title,
                    modifier = Modifier
                        .size(48.dp)
                        .onGloballyPositioned { artworkBounds = it.boundsInRoot() },
                    cornerRadius = ElovaireRadii.artworkSmall,
                    requestedSizePx = 192,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        color = resolvedPrimaryTextColor,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                            repeatDelayMillis = 2500,
                            initialDelayMillis = 2500,
                            velocity = 24.dp,
                        ),
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.labelLarge,
                        color = resolvedSecondaryTextColor,
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(controlTint)
                    .clickable(
                        enabled = visible,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTogglePlayback,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier.matchParentSize(),
                ) {
                    val strokeWidth = size.minDimension * 0.08f
                    val arcInset = strokeWidth / 2f + 1.5f
                    drawArc(
                        color = controlIconTint.copy(alpha = 0.18f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    drawArc(
                        color = controlIconTint,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                }
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        (
                            fadeIn(animationSpec = ElovaireMotion.iconSwapInSpec()) +
                                scaleIn(
                                    initialScale = 0.9f,
                                    animationSpec = ElovaireMotion.releaseSpringSpec(),
                                )
                            ) togetherWith
                            (
                                fadeOut(animationSpec = ElovaireMotion.iconSwapOutSpec()) +
                                    scaleOut(
                                        targetScale = 1.04f,
                                        animationSpec = ElovaireMotion.contentFadeOutSpec(),
                                    )
                                )
                    },
                    label = "mini_player_play_pause_icon",
                ) { playing ->
                    Icon(
                        painter = painterResource(
                            id = if (playing) R.drawable.ic_lucide_pause else R.drawable.ic_lucide_play,
                        ),
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = controlIconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FrostedChrome(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseTint = if (darkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.82f)
    }
    val softTint = if (darkTheme) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.04f)
    }
    Box(
        modifier = modifier
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.99f }
                .blur(70.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            softTint,
                            baseTint.copy(alpha = 0.18f),
                            softTint.copy(alpha = 0.76f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(baseTint),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                    shape = shape,
                ),
        )
        content()
    }
}

@Composable
private fun PermissionGate(
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(ElovaireRadii.dialog),
            tonalElevation = 8.dp,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Offline audio deserves access to your library",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(30f)),
                )
                Text(
                    text = "Elovaire scans the device Music folder for local albums, artwork, and track queues",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onRequestPermission) {
                    Text("Allow audio library access")
                }
            }
        }
    }
}

@Composable
private fun FirstLaunchPermissionLoadingScreen(
    showLoading: Boolean,
    onRequestPermission: () -> Unit,
) {
    val spinnerColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val infiniteTransition = rememberInfiniteTransition(label = "first_launch_permission_spinner")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "first_launch_permission_spinner_rotation",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        UnifiedTopBar(
            title = "Elovaire",
            showSettings = false,
            onOpenMenu = onRequestPermission,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        )
        ElovaireAnimatedVisibility(
            visible = showLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
            exit = fadeOut(animationSpec = ElovaireMotion.fadeSlow()),
            label = "FirstLaunchPermissionSpinnerVisibility",
        ) {
            Canvas(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer { rotationZ = rotationDegrees },
            ) {
                val stroke = 2.5.dp.toPx()
                val inset = stroke / 2f + 1.dp.toPx()
                val arcSize = size.minDimension - inset * 2f
                drawArc(
                    color = spinnerColor.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = spinnerColor,
                    startAngle = -80f,
                    sweepAngle = 88f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    lastPlayedAlbum: Album?,
    recentlyAddedAlbums: List<Album>,
    recentSongs: List<Song>,
    favoriteAlbums: List<Album>,
    playbackState: PlaybackUiState,
    isLibraryLoading: Boolean,
    libraryScanProgress: Float,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    playInitialReveal: Boolean,
    onInitialRevealFinished: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val listState = rememberElovaireLazyListState("home_screen")
    val motionDurationScale = rememberSystemAnimationScale()
    var revealModules by rememberSaveable(playInitialReveal) { mutableStateOf(!playInitialReveal) }
    LaunchedEffect(scrollToTopRequestVersion) {
        if (scrollToTopRequestVersion > 0L && listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset > 0) {
            listState.animateScrollToItem(0)
        }
    }
    LaunchedEffect(playInitialReveal) {
        if (playInitialReveal) {
            revealModules = false
            delay(ElovaireMotion.scaleDurationMillis(70L, motionDurationScale))
            revealModules = true
            delay(ElovaireMotion.scaleDurationMillis(520L, motionDurationScale))
            onInitialRevealFinished()
        } else {
            revealModules = true
        }
    }
    val showInitialLoadingState = isLibraryLoading &&
        recentlyAddedAlbums.isEmpty() &&
        favoriteAlbums.isEmpty() &&
        playbackState.recentSongIds.isEmpty()
    val showEmptyLibraryState = !isLibraryLoading &&
        recentlyAddedAlbums.isEmpty() &&
        favoriteAlbums.isEmpty() &&
        recentSongs.isEmpty()
    Box(modifier = Modifier.fillMaxSize()) {
        ElovaireAnimatedContent(
            targetState = when {
                showInitialLoadingState -> HomeScreenState.Loading
                showEmptyLibraryState -> HomeScreenState.Empty
                else -> HomeScreenState.Content
            },
            transitionSpec = {
                if (targetState == HomeScreenState.Loading) {
                    fadeIn(animationSpec = ElovaireMotion.fadeMedium()) togetherWith
                        fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec())
                } else {
                    (fadeIn(animationSpec = ElovaireMotion.fadeSlow(delayMillis = 40)) +
                        slideInVertically(
                            animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Screen),
                            initialOffsetY = { -it / 14 },
                        )) togetherWith fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec())
                }
            },
            label = "HomeLoadingTransition",
        ) { state ->
            when (state) {
                HomeScreenState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_disc_3),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Indexing library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Songs and albums will show when indexing is done",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    LinearProgressIndicator(
                        progress = { libraryScanProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(ElovaireRadii.pill)),
                        color = MaterialTheme.colorScheme.onSurface,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                    )
                }
                }

                HomeScreenState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "No music was found",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Songs and albums will show here as you add music to your device's default music folder",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                HomeScreenState.Content -> {
                ElovaireAnimatedVisibility(
                    visible = revealModules,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeSlow()) +
                        slideInVertically(
                            animationSpec = ElovaireMotion.offsetSoft(durationMillis = 420),
                            initialOffsetY = { -it / 18 },
                        ),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                    label = "HomeFirstLaunchModulesReveal",
                ) {
                    LazyColumn(
                        state = listState,
                        overscrollEffect = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .ensureSingleItemRubberBand(listState),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = topPadding + 8.dp,
                            end = 20.dp,
                            bottom = bottomPadding + 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        lastPlayedAlbum?.let { album ->
                            item {
                                LastPlayedAlbumModule(
                                    album = album,
                                    onOpen = { origin -> onAlbumSelected(album, origin) },
                                    onPlay = { onPlayAlbum(album) },
                                )
                            }
                        }

                        if (recentlyAddedAlbums.isNotEmpty()) {
                            item {
                                ModuleCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        MutedSectionHeader(
                                            title = "Recently added",
                                            iconResId = R.drawable.ic_lucide_gallery_vertical_end,
                                        )
                                        recentlyAddedAlbums.take(4).chunked(2).forEach { rowAlbums ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                rowAlbums.forEach { album ->
                                                    AlbumGridCard(
                                                        album = album,
                                                        modifier = Modifier.weight(1f),
                                                        onOpen = { origin -> onAlbumSelected(album, origin) },
                                                    )
                                                }
                                                repeat((2 - rowAlbums.size).coerceAtLeast(0)) {
                                                    SpacerTile(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (!isLibraryLoading) {
                            item {
                                EmptyStateCard(
                                    title = "No recent additions yet",
                                    message = "Add albums to the device Music folder and the newest ones will appear here automatically",
                                )
                            }
                        }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_circle_play),
                                    contentDescription = null,
                                    tint = readableMutedIconColor(),
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = "Recently played songs",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (recentSongs.isEmpty()) {
                                Text(
                                    text = "Songs will show up here soon",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = readableSecondaryTextColor(),
                                )
                            } else {
                                Column {
                                    recentSongs.forEachIndexed { index, song ->
                                        HomeRecentSongRow(
                                            song = song,
                                            isFavorite = song.id in favoriteSongIds,
                                            onClick = { onSongSelected(song) },
                                            onToggleFavorite = { onToggleFavorite(song.id) },
                                            showDivider = index != recentSongs.lastIndex,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (favoriteAlbums.isNotEmpty()) {
                        item {
                            FavoriteAlbumsModule(
                                albums = favoriteAlbums.take(6),
                                onAlbumSelected = onAlbumSelected,
                            )
                        }
                    } else if (!isLibraryLoading) {
                        item {
                            EmptyStateCard(
                                title = "No albums have been opened yet",
                                message = "Open or play any album and it will appear here with its artwork front and center",
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}
}


@Composable
private fun LastPlayedAlbumModule(
    album: Album,
    onOpen: (ExpandOrigin) -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val artwork = rememberArtworkBitmap(album.artUri, size = 512)
    val year = remember(album.songs) { album.songs.firstNotNullOfOrNull { it.releaseYear } }
    val genre = remember(album.songs) {
        album.songs.firstOrNull { it.genre.isNotBlank() && it.genre != "Unknown Genre" }?.genre
    }
    val gradient = rememberArtworkGradient(album.artUri).value
    val metaItems = remember(year, genre) {
        buildList {
            year?.toString()?.let(::add)
            genre?.let(::add)
        }
    }
    val playBackground = gradient.first()
        .copy(alpha = 0.24f)
        .compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
    val playTint = if (playBackground.luminance() > 0.56f) InkText else Color.White
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseTint = if (darkTheme) Color(0xFF141414).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.82f)
    val albumTint = gradient.first().copy(alpha = 0.46f)
    val controlBaseTint = if (darkTheme) {
        gradient.last().copy(alpha = 0.28f).compositeOver(Color.Black.copy(alpha = 0.16f))
    } else {
        gradient.last().copy(alpha = 0.22f).compositeOver(Color.White.copy(alpha = 0.16f))
    }
    val contentColor = if (controlBaseTint.luminance() > 0.42f) InkText else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.72f)

    Box(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
            )
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .background(baseTint)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (darkTheme) 0.05f else 0.04f),
                shape = RoundedCornerShape(ElovaireRadii.module),
            ),
    ) {
        artwork.value?.let { artworkBitmap ->
            Image(
                bitmap = artworkBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(40.dp),
                alpha = 0.88f,
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(albumTint),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.size(88.dp),
                cornerRadius = ElovaireRadii.artwork,
                showArtworkGlow = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = secondaryContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metaItems.isNotEmpty()) {
                    Text(
                        text = metaItems.joinToString("  •  "),
                        style = MaterialTheme.typography.labelLarge,
                        color = secondaryContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = playBackground,
                contentColor = playTint,
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_play),
                        contentDescription = "Play album",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumCollectionContent(
    albums: List<Album>,
    layoutMode: AlbumLayoutMode,
    sortMode: AlbumSortMode,
    topPadding: Dp,
    bottomPadding: Dp,
    title: String = "All albums",
    subtitle: String = "Alphabetical by album artist, then album title.",
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSortModeChanged: (AlbumSortMode) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    val listState = rememberElovaireLazyListState(title, "album_collection_list")
    val gridState = rememberElovaireLazyGridState(title, "album_collection_grid")
    val sortedAlbums = remember(albums, sortMode) {
        when (sortMode) {
            AlbumSortMode.Artist -> albums.sortedWith(
                compareBy<Album> { it.artist.lowercase() }
                    .thenBy { it.title.lowercase() },
            )
            AlbumSortMode.Album -> albums.sortedWith(
                compareBy<Album> { it.title.lowercase() }
                    .thenBy { it.artist.lowercase() },
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (layoutMode == AlbumLayoutMode.Grid) {
            LazyVerticalGrid(
                state = gridState,
                overscrollEffect = null,
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .ensureSingleItemRubberBand(gridState),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = topPadding + 8.dp,
                    end = 20.dp,
                    bottom = bottomPadding + 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AlbumSortControl(
                            selected = sortMode,
                            expanded = showSortOptions,
                            onToggleExpanded = { showSortOptions = !showSortOptions },
                            onSelect = { selectedMode ->
                                onSortModeChanged(selectedMode)
                                showSortOptions = false
                            },
                        )
                        Spacer(modifier = Modifier.width(11.dp))
                        LibraryModeToggle(
                            layoutMode = layoutMode,
                            onLayoutModeChanged = onLayoutModeChanged,
                        )
                    }
                }

                items(sortedAlbums, key = { it.id }) { album ->
                    AlbumGridCard(
                        album = album,
                        onOpen = { origin -> onAlbumSelected(album, origin) },
                    )
                }
            }
            FastScrollbar(
                state = gridState,
                topInset = topPadding + 16.dp,
                bottomInset = bottomPadding + 16.dp,
            )
        } else {
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier
                    .fillMaxSize()
                    .ensureSingleItemRubberBand(listState),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = topPadding + 8.dp,
                    end = 20.dp,
                    bottom = bottomPadding + 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AlbumSortControl(
                            selected = sortMode,
                            expanded = showSortOptions,
                            onToggleExpanded = { showSortOptions = !showSortOptions },
                            onSelect = { selectedMode ->
                                onSortModeChanged(selectedMode)
                                showSortOptions = false
                            },
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        LibraryModeToggle(
                            layoutMode = layoutMode,
                            onLayoutModeChanged = onLayoutModeChanged,
                        )
                    }
                }

                items(sortedAlbums, key = { it.id }) { album ->
                    CompactAlbumRow(
                        album = album,
                        onOpen = { origin -> onAlbumSelected(album, origin) },
                    )
                }
            }
            FastScrollbar(
                state = listState,
                topInset = topPadding + 16.dp,
                bottomInset = bottomPadding + 16.dp,
            )
        }
    }
}

@Composable
private fun AlbumSortControl(
    selected: AlbumSortMode,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelect: (AlbumSortMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            onClick = onToggleExpanded,
            shape = RoundedCornerShape(ElovaireRadii.pill),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_arrow_down_up),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_chevron_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (expanded) 180f else 0f),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(ElovaireMotion.Quick)) +
                slideInVertically(
                    animationSpec = tween(ElovaireMotion.Quick),
                    initialOffsetY = { -it / 4 },
                ),
            exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
                slideOutVertically(
                    animationSpec = tween(ElovaireMotion.Quick),
                    targetOffsetY = { -it / 4 },
                ),
        ) {
            Surface(
                shape = RoundedCornerShape(ElovaireRadii.card),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    AlbumSortMode.entries.forEachIndexed { index, mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSelect(mode) },
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (mode == selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                            if (mode == selected) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        if (index != AlbumSortMode.entries.lastIndex) {
                            DividerLine()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsScreen(
    playlists: List<Playlist>,
    libraryState: LibraryUiState,
    topPadding: Dp,
    bottomPadding: Dp,
    onRequestCreatePlaylist: () -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylists: (Set<Long>) -> Unit,
    onOpenPlaylist: (Playlist, ExpandOrigin) -> Unit,
) {
    var playlistBeingRenamed by remember { mutableStateOf<Playlist?>(null) }
    var selectedPlaylistIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var deleteConfirmationVisible by rememberSaveable { mutableStateOf(false) }
    val songsById = remember(libraryState.songs) { libraryState.songs.associateBy { it.id } }
    val gridState = rememberElovaireLazyGridState("playlists_screen")
    val editMode = selectedPlaylistIds.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding),
    ) {
        if (playlists.isEmpty()) {
            EmptyPlaylistState(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                onCreate = onRequestCreatePlaylist,
            )
        } else {
            LazyVerticalGrid(
                state = gridState,
                overscrollEffect = null,
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .ensureSingleItemRubberBand(gridState),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistGridTile(
                        playlist = playlist,
                        previewSongs = playlist.songIds.mapNotNull(songsById::get),
                        selected = playlist.id in selectedPlaylistIds,
                        selectionMode = editMode,
                        onClick = { origin ->
                            if (editMode && !playlist.isSystem) {
                                selectedPlaylistIds = selectedPlaylistIds.togglePlaylistSelection(playlist.id)
                                if (selectedPlaylistIds.isEmpty()) {
                                    deleteConfirmationVisible = false
                                }
                            } else {
                                onOpenPlaylist(playlist, origin)
                            }
                        },
                        onLongPress = {
                            if (!playlist.isSystem) {
                                selectedPlaylistIds = selectedPlaylistIds.togglePlaylistSelection(playlist.id)
                                deleteConfirmationVisible = false
                            }
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
            enter = fadeIn(animationSpec = tween(180)) + slideInVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 2 },
            ),
            exit = fadeOut(animationSpec = tween(160)) + slideOutVertically(
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                targetOffsetY = { it / 2 },
            ),
        ) {
            val renameEnabled = selectedPlaylistIds.size == 1
            val deleteLabel = if (deleteConfirmationVisible) "Delete" else "Delete"
            DynamicBackdropSurface(
                shape = RoundedCornerShape(ElovaireRadii.pill),
                overlayAlpha = 0.7f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (deleteConfirmationVisible) {
                        PlaylistActionPill(
                            label = "Delete",
                            enabled = true,
                            backgroundColor = DestructiveRed.copy(alpha = 0.2f),
                            contentColor = DestructiveRed,
                            onClick = {
                                onDeletePlaylists(selectedPlaylistIds)
                                selectedPlaylistIds = emptySet()
                                deleteConfirmationVisible = false
                            },
                        )
                        PlaylistActionPill(
                            label = "Cancel",
                            enabled = true,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { deleteConfirmationVisible = false },
                        )
                    } else {
                        PlaylistActionPill(
                            label = "Rename",
                            enabled = renameEnabled,
                            backgroundColor = if (renameEnabled) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            },
                            contentColor = if (renameEnabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            },
                            onClick = {
                                playlistBeingRenamed = playlists.firstOrNull { it.id == selectedPlaylistIds.firstOrNull() }
                            },
                        )
                        PlaylistActionPill(
                            label = deleteLabel,
                            enabled = true,
                            backgroundColor = DestructiveRed.copy(alpha = 0.2f),
                            contentColor = DestructiveRed,
                            onClick = { deleteConfirmationVisible = true },
                        )
                    }
                }
            }
        }

        playlistBeingRenamed?.let { playlist ->
            PlaylistNameDialog(
                title = "Rename playlist",
                confirmLabel = "Save",
                initialName = playlist.name,
                onDismiss = { playlistBeingRenamed = null },
                onConfirm = { name ->
                    onRenamePlaylist(playlist.id, name)
                    playlistBeingRenamed = null
                    selectedPlaylistIds = emptySet()
                    deleteConfirmationVisible = false
                },
            )
        }
    }
}

@Composable
private fun LibraryHubScreen(
    libraryState: LibraryUiState,
    topPadding: Dp,
    bottomPadding: Dp,
    onOpenCollection: (LibraryCollectionKind) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val totalSongs = libraryState.songs.size
    val totalAlbums = libraryState.albums.size
    val recentlyAddedAlbums = remember(libraryState.albums) {
        recentlyAddedAlbumsFor(libraryState).take(8)
    }
    val totalArtists = remember(libraryState.songs) {
        libraryState.songs.map { it.artist.ifBlank { "Unknown Artist" } }.distinct().size
    }
    val totalGenres = remember(libraryState.songs) {
        libraryState.songs.map { it.genre.ifBlank { "Unknown Genre" } }.distinct().size
    }

    val listState = rememberElovaireLazyListState("library_hub")
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = topPadding + 8.dp,
                end = 20.dp,
                bottom = bottomPadding + 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                ModuleCard {
                    Column {
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_music,
                            title = "Songs",
                            detail = "${formatCountLabel(totalSongs, "song")} in your library",
                            onClick = { onOpenCollection(LibraryCollectionKind.Songs) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_disc_album,
                            title = "Albums",
                            detail = "${formatCountLabel(totalAlbums, "album")} in total",
                            onClick = { onOpenCollection(LibraryCollectionKind.Albums) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_mic_vocal,
                            title = "Artists",
                            detail = "${formatCountLabel(totalArtists, "artist")} found",
                            onClick = { onOpenCollection(LibraryCollectionKind.Artists) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_guitar,
                            title = "Genres",
                            detail = "${formatCountLabel(totalGenres, "genre")} found",
                            onClick = { onOpenCollection(LibraryCollectionKind.Genres) },
                        )
                    }
                }
            }

            if (recentlyAddedAlbums.isNotEmpty()) {
                item {
                    ModuleCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MutedSectionHeader(
                                title = "Recently added",
                                iconResId = R.drawable.ic_lucide_gallery_vertical_end,
                            )
                            recentlyAddedAlbums.chunked(2).take(4).forEach { rowAlbums ->
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowAlbums.forEach { album ->
                                        AlbumGridCard(
                                            album = album,
                                            modifier = Modifier.weight(1f),
                                            onOpen = { origin -> onAlbumSelected(album, origin) },
                                        )
                                    }
                                    repeat((2 - rowAlbums.size).coerceAtLeast(0)) {
                                        SpacerTile(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryHubRow(
    iconResId: Int,
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
            contentDescription = null,
            tint = readableMutedIconColor().copy(alpha = 0.5f),
            modifier = Modifier
                .size(18.dp)
                .rotate(180f),
        )
    }
}

@Composable
private fun LibraryCollectionScreen(
    kind: LibraryCollectionKind,
    libraryState: LibraryUiState,
    songPlayCounts: Map<Long, Int>,
    favoriteSongIds: Set<Long>,
    albumCollectionLayoutMode: AlbumLayoutMode,
    songCollectionLayoutMode: AlbumLayoutMode,
    albumSortMode: AlbumSortMode,
    songSortMode: SongSortMode,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAlbumCollectionLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSongCollectionLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onAlbumSortModeChanged: (AlbumSortMode) -> Unit,
    onSongSortModeChanged: (SongSortMode) -> Unit,
    onGenreSelected: (String) -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    when (kind) {
        LibraryCollectionKind.Songs -> SongCollectionScreen(
            songs = libraryState.songs,
            favoriteSongIds = favoriteSongIds,
            sortMode = songSortMode,
            currentSongId = currentSongId,
            isCurrentSongPlaying = isCurrentSongPlaying,
            bottomPadding = bottomPadding,
            onBack = onBack,
            onSortModeChanged = onSongSortModeChanged,
            onSongSelected = onSongSelected,
            onToggleFavorite = onToggleFavorite,
        )

        LibraryCollectionKind.Albums -> Box(modifier = Modifier.fillMaxSize()) {
            AlbumCollectionContent(
                albums = libraryState.albums,
                layoutMode = albumCollectionLayoutMode,
                sortMode = albumSortMode,
                topPadding = detailTopBarOccupiedHeight(),
                bottomPadding = bottomPadding,
                title = "Albums",
                subtitle = "Alphabetical by album artist, then album title",
                onLayoutModeChanged = onAlbumCollectionLayoutModeChanged,
                onSortModeChanged = onAlbumSortModeChanged,
                onAlbumSelected = onAlbumSelected,
            )
            DetailListTopBar(
                title = "Albums",
                subtitle = "${libraryState.albums.size} albums",
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        LibraryCollectionKind.Artists -> ArtistCollectionScreen(
            songs = libraryState.songs,
            bottomPadding = bottomPadding,
            onBack = onBack,
            onArtistSelected = onArtistSelected,
        )

        LibraryCollectionKind.Genres -> GenreCollectionScreen(
            songs = libraryState.songs,
            bottomPadding = bottomPadding,
            onBack = onBack,
            onGenreSelected = onGenreSelected,
        )
    }
}

@Composable
private fun SongCollectionScreen(
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    sortMode: SongSortMode,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSortModeChanged: (SongSortMode) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    val listState = rememberElovaireLazyListState("song_collection_list")
    val sortedSongs = remember(songs, sortMode) {
        when (sortMode) {
            SongSortMode.Title -> songs.sortedWith(
                compareBy<Song> { it.title.lowercase() }
                    .thenBy { it.artist.lowercase() }
                    .thenBy { it.album.lowercase() },
            )
            SongSortMode.Artist -> songs.sortedWith(
                compareBy<Song> { it.artist.lowercase() }
                    .thenBy { it.title.lowercase() }
                    .thenBy { it.album.lowercase() },
            )
            SongSortMode.Album -> songs.sortedWith(
                compareBy<Song> { it.album.lowercase() }
                    .thenBy { it.title.lowercase() }
                    .thenBy { it.artist.lowercase() },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                SongSortControl(
                    selected = sortMode,
                    expanded = showSortOptions,
                    onToggleExpanded = { showSortOptions = !showSortOptions },
                    onSelect = { selectedMode ->
                        onSortModeChanged(selectedMode)
                        showSortOptions = false
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            itemsIndexed(
                items = sortedSongs,
                key = { _, song -> song.id },
                contentType = { _, _ -> "song_row" },
            ) { index, song ->
                GroupedListRowContainer(
                    index = index,
                    lastIndex = sortedSongs.lastIndex,
                ) {
                    PlaylistSongRow(
                        song = song,
                        isFavorite = song.id in favoriteSongIds,
                        isCurrentSong = song.id == currentSongId,
                        isPlaybackActive = isCurrentSongPlaying,
                        onClick = { onSongSelected(song, sortedSongs) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                        showOverflowMenu = true,
                        showDivider = index != sortedSongs.lastIndex,
                    )
                }
            }
        }

        DetailListTopBar(
            title = "Songs",
            subtitle = "${sortedSongs.size} songs",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SongSortControl(
    selected: SongSortMode,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelect: (SongSortMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            onClick = onToggleExpanded,
            shape = RoundedCornerShape(ElovaireRadii.pill),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_arrow_down_up),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_chevron_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (expanded) 180f else 0f),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(ElovaireMotion.Quick)) +
                slideInVertically(
                    animationSpec = tween(ElovaireMotion.Quick),
                    initialOffsetY = { -it / 4 },
                ),
            exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
                slideOutVertically(
                    animationSpec = tween(ElovaireMotion.Quick),
                    targetOffsetY = { -it / 4 },
                ),
        ) {
            Surface(
                shape = RoundedCornerShape(ElovaireRadii.card),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    SongSortMode.entries.forEachIndexed { index, mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSelect(mode) },
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (mode == selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                            if (mode == selected) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        if (index != SongSortMode.entries.lastIndex) {
                            DividerLine()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistCollectionScreen(
    songs: List<Song>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    val listState = rememberElovaireLazyListState("artist_collection")
    val artists = remember(songs) {
        songs
            .groupBy { it.artist.ifBlank { "Unknown Artist" } }
            .map { (name, artistSongs) ->
                ArtistEntry(
                    name = name,
                    artUri = artistSongs.firstOrNull()?.artUri,
                    albumCount = artistSongs.map { it.albumId }.distinct().size,
                    songCount = artistSongs.size,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column {
                        artists.forEachIndexed { index, artist ->
                            ArtistRow(
                                artist = artist,
                                onClick = { onArtistSelected(artist.name) },
                            )
                            if (index != artists.lastIndex) {
                                DividerLine()
                            }
                        }
                    }
                }
            }
        }
        FastScrollbar(
            state = listState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
        )

        DetailListTopBar(
            title = "Artists",
            subtitle = "${artists.size} artists",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun GenreCollectionScreen(
    songs: List<Song>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onGenreSelected: (String) -> Unit,
) {
    val listState = rememberElovaireLazyListState("genre_collection")
    val genres = remember(songs) {
        songs
            .groupBy { it.genre.ifBlank { "Unknown Genre" } }
            .map { (name, genreSongs) ->
                GenreEntry(
                    name = name,
                    albumCount = genreSongs.map { it.albumId }.distinct().size,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailSectionTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column {
                        genres.forEachIndexed { index, genre ->
                            GenreRow(
                                genre = genre,
                                onClick = { onGenreSelected(genre.name) },
                            )
                            if (index != genres.lastIndex) {
                                DividerLine()
                            }
                        }
                    }
                }
            }
        }
        FastScrollbar(
            state = listState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
        )

        DetailListTopBar(
            title = "Genres",
            subtitle = "${genres.size} genres",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun GenreAlbumsScreen(
    genre: String,
    libraryState: LibraryUiState,
    layoutMode: AlbumLayoutMode,
    sortMode: AlbumSortMode,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSortModeChanged: (AlbumSortMode) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val filteredAlbums = remember(genre, libraryState.albums) {
        libraryState.albums.filter { album ->
            album.songs.any { song ->
                song.genre.equals(genre, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AlbumCollectionContent(
            albums = filteredAlbums,
            layoutMode = layoutMode,
            sortMode = sortMode,
            topPadding = detailTopBarOccupiedHeight(),
            bottomPadding = bottomPadding,
            title = genre.ifBlank { "Unknown Genre" },
            subtitle = "${filteredAlbums.size} albums tagged in this genre",
            onLayoutModeChanged = onLayoutModeChanged,
            onSortModeChanged = onSortModeChanged,
            onAlbumSelected = onAlbumSelected,
        )
        DetailListTopBar(
            title = genre.ifBlank { "Unknown Genre" },
            subtitle = "${filteredAlbums.size} albums",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ArtistDetailScreen(
    artistName: String,
    libraryState: LibraryUiState,
    songPlayCounts: Map<Long, Int>,
    favoriteSongIds: Set<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val normalizedArtist = artistName.ifBlank { "Unknown Artist" }
    val artistSongs = remember(normalizedArtist, libraryState.songs) {
        libraryState.songs.filter { song ->
            song.artist.ifBlank { "Unknown Artist" }.equals(normalizedArtist, ignoreCase = true)
        }
    }
    val topSongs = remember(artistSongs, songPlayCounts) {
        artistSongs
            .sortedWith(
                compareByDescending<Song> { songPlayCounts[it.id] ?: 0 }
                    .thenBy { it.title.lowercase() },
            )
            .take(5)
    }
    val artistAlbums = remember(normalizedArtist, libraryState.albums) {
        libraryState.albums
            .filter { album -> album.artist.equals(normalizedArtist, ignoreCase = true) }
            .sortedBy { it.title.lowercase() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val listState = rememberElovaireLazyListState(normalizedArtist, "artist_detail")
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailSectionTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (topSongs.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionTitleRow(
                            title = "Most played songs",
                            subtitle = "${topSongs.size} tracks you return to the most",
                            compact = true,
                        )
                        Column {
                            topSongs.forEachIndexed { index, song ->
                                HomeRecentSongRow(
                                    song = song,
                                    isFavorite = song.id in favoriteSongIds,
                                    isCurrentSong = song.id == currentSongId,
                                    isPlaybackActive = isCurrentSongPlaying,
                                    onClick = { onSongSelected(song, artistSongs) },
                                    onToggleFavorite = { onToggleFavorite(song.id) },
                                    showDivider = index != topSongs.lastIndex,
                                )
                            }
                        }
                    }
                }
            }

            if (artistAlbums.isNotEmpty()) {
                item {
                    ModuleCard {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SectionTitleRow(
                                title = "Albums",
                                subtitle = "${artistAlbums.size} available releases",
                                compact = true,
                            )
                            ArtistAlbumGallery(
                                albums = artistAlbums,
                                onAlbumSelected = onAlbumSelected,
                            )
                        }
                    }
                }
            }
        }

        DetailListTopBar(
            title = normalizedArtist,
            subtitle = buildArtistScreenSubtitle(
                songCount = artistSongs.size,
                albumCount = artistAlbums.size,
            ),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

private fun buildArtistScreenSubtitle(
    songCount: Int,
    albumCount: Int,
): String {
    return "${formatCountLabel(albumCount, "album")} • ${formatCountLabel(songCount, "song")}"
}

@Composable
private fun ArtistAlbumGallery(
    albums: List<Album>,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val scrollState = rememberScrollState()
    val itemWidth = 158.dp
    val itemGap = 14.dp
    val contentWidth = remember(albums.size) {
        if (albums.isEmpty()) {
            0.dp
        } else {
            (itemWidth * albums.size) + (itemGap * (albums.size - 1).coerceAtLeast(0))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalGestureSafe()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(itemGap),
        ) {
            albums.forEach { album ->
                AlbumGridCard(
                    album = album,
                    modifier = Modifier.width(itemWidth),
                    onOpen = { origin -> onAlbumSelected(album, origin) },
                )
            }
        }
        EqHorizontalScrollbar(
            scrollState = scrollState,
            contentWidth = contentWidth,
            modifier = Modifier.height(26.dp),
        )
    }
}

@Composable
private fun EmptyPlaylistState(
    modifier: Modifier = Modifier,
    onCreate: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            onClick = onCreate,
            shape = RoundedCornerShape(ElovaireRadii.card),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 6.dp,
        ) {
            Box(
                modifier = Modifier.size(74.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_plus),
                    contentDescription = "Create playlist",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Text(
            text = "Tap to create new playlist",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CreatePlaylistTile(
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(ElovaireRadii.card),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_plus),
                    contentDescription = "Create playlist",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Text(
            text = "New playlist",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun PlaylistGridTile(
    playlist: Playlist,
    previewSongs: List<Song>,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: (ExpandOrigin) -> Unit,
    onLongPress: () -> Unit,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = Modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
                onLongClick = onLongPress,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            PlaylistArtworkPreview(
                songs = previewSongs,
                title = playlist.name,
                modifier = Modifier.fillMaxWidth(),
            )
            if (selectionMode && !playlist.isSystem) {
                PlaylistSelectionIndicator(
                    selected = selected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }
        }
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
        )
        Text(
            text = "${playlist.songIds.size} songs",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Set<Long>.togglePlaylistSelection(playlistId: Long): Set<Long> {
    return if (playlistId in this) this - playlistId else this + playlistId
}

@Composable
private fun PlaylistSelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    tint.copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                },
            )
            .border(
                width = 1.dp,
                color = tint.copy(alpha = if (selected) 0f else 0.64f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_check),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun PlaylistActionPill(
    label: String,
    enabled: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
    }
}

@Composable
private fun PlaylistArtworkPreview(
    songs: List<Song>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val collageSongs = remember(songs) {
        val usedAlbumIds = mutableSetOf<Long>()
        songs.filter { song ->
            usedAlbumIds.add(song.albumId)
        }.take(4)
    }
    val usesCollage = collageSongs.size >= 4
    val coverSong = songs.firstOrNull()
    val gradient = rememberArtworkGradient(coverSong?.artUri).value
    Box(modifier = modifier.aspectRatio(1f)) {
        if (songs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 18.dp, end = 12.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.card))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                gradient.first().copy(alpha = 0f),
                                gradient.first().copy(alpha = 0.12f),
                                gradient.last().copy(alpha = 0.2f),
                            ),
                        ),
                    )
                    .blur(30.dp),
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(ElovaireRadii.card),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        ) {
            when {
                usesCollage -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        repeat(2) { rowIndex ->
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                repeat(2) { columnIndex ->
                                    val song = collageSongs.getOrNull((rowIndex * 2) + columnIndex)
                                    if (song != null) {
                                        ArtworkImage(
                                            uri = song.artUri,
                                            title = song.title,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            cornerRadius = 0.dp,
                                            requestedSizePx = 160,
                                        )
                                    } else {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                            .fillMaxHeight(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                songs.isNotEmpty() -> {
                    ArtworkImage(
                        uri = coverSong?.artUri,
                        title = coverSong?.title ?: title,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = ElovaireRadii.artwork,
                        requestedSizePx = 384,
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_music),
                            contentDescription = title.ifBlank { "Playlist artwork placeholder" },
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistNameDialog(
    title: String = "New playlist",
    confirmLabel: String = "Create",
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(ElovaireRadii.card),
                overlayAlpha = 0.72f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(24f)),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        shape = RoundedCornerShape(ElovaireRadii.input),
                        placeholder = { Text("Playlist name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.42f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.32f),
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            onClick = { onConfirm(name) },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = if (name.isNotBlank()) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                            contentColor = if (name.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
                            },
                        ) {
                            Text(
                                text = confirmLabel,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    libraryState: LibraryUiState,
    playbackState: PlaybackUiState,
    albumPlayCounts: Map<Long, Int>,
    recentSearches: List<SearchHistoryEntry>,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onArtistSelected: (String) -> Unit,
    onRememberAlbumSearch: (Album) -> Unit,
    onRememberArtistSearch: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onClearSearchHistory: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var isSearchFieldFocused by rememberSaveable { mutableStateOf(false) }
    var showAllSongResults by rememberSaveable { mutableStateOf(false) }
    var searchSongSortMode by rememberSaveable { mutableStateOf(SearchSongSortMode.Title) }
    var showSearchSongSortOptions by rememberSaveable { mutableStateOf(false) }
    val listState = rememberElovaireLazyListState("search_screen")
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedQuery = query.trim()
    val isSearchUiActive = trimmedQuery.isNotBlank() || isSearchFieldFocused || showAllSongResults
    val collapseAllSongResults: () -> Unit = {
        showAllSongResults = false
        showSearchSongSortOptions = false
        isSearchFieldFocused = false
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }
    val resetSearchToMain: () -> Unit = {
        query = ""
        isSearchFieldFocused = false
        showAllSongResults = false
        showSearchSongSortOptions = false
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }
    if (showAllSongResults && trimmedQuery.isNotBlank()) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Back(
                title = "Search",
                onBack = collapseAllSongResults,
                centeredTitle = false,
            ),
        )
    }
    BackHandler(enabled = isSearchUiActive) {
        when {
            showSearchSongSortOptions -> showSearchSongSortOptions = false
            showAllSongResults && trimmedQuery.isNotBlank() -> collapseAllSongResults()
            else -> resetSearchToMain()
        }
    }
    LaunchedEffect(isSearchUiActive) {
        onSearchQueryActiveChanged(isSearchUiActive)
    }
    LaunchedEffect(trimmedQuery) {
        if (trimmedQuery.isBlank()) {
            showAllSongResults = false
            showSearchSongSortOptions = false
        }
    }
    val contentMode = when {
        showAllSongResults && trimmedQuery.isNotBlank() -> SearchContentMode.AllSongs
        trimmedQuery.isBlank() -> SearchContentMode.Discover
        else -> SearchContentMode.Results
    }
    val allMatchingSongs = remember(trimmedQuery, libraryState.songs, searchSongSortMode) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            val filteredSongs = libraryState.songs.filter { song ->
                searchMatchesComposite(
                    query = trimmedQuery,
                    fields = listOf(song.title, song.artist, song.album),
                )
            }
            when (searchSongSortMode) {
                SearchSongSortMode.Title -> filteredSongs.sortedWith(
                    compareBy<Song> { it.title.lowercase() }
                        .thenBy { it.artist.lowercase() }
                        .thenBy { it.album.lowercase() },
                )
                SearchSongSortMode.Artist -> filteredSongs.sortedWith(
                    compareBy<Song> { it.artist.lowercase() }
                        .thenBy { it.title.lowercase() }
                        .thenBy { it.album.lowercase() },
                )
            }
        }
    }
    val matchingSongs = remember(allMatchingSongs) { allMatchingSongs.take(20) }
    val matchingAlbums = remember(trimmedQuery, libraryState.albums) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            libraryState.albums.filter { album ->
                searchMatchesComposite(
                    query = trimmedQuery,
                    fields = listOf(album.title, album.artist),
                )
            }.take(12)
        }
    }
    val matchingArtists = remember(trimmedQuery, libraryState.songs) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            libraryState.songs
                .groupBy { it.artist }
                .values
                .map { artistSongs ->
                    val firstSong = artistSongs.first()
                    SearchHistoryEntry(
                        key = "artist:${firstSong.artist.lowercase()}",
                        kind = SearchHistoryKind.Artist,
                        title = firstSong.artist,
                        subtitle = "${artistSongs.size} songs",
                        artUri = firstSong.artUri,
                        query = firstSong.artist,
                    )
                }
                .filter { artist ->
                    searchMatchesComposite(
                        query = trimmedQuery,
                        fields = listOf(artist.title),
                    )
                }
                .take(6)
        }
    }
    val suggestedAlbums = remember(libraryState.albums, albumPlayCounts, playbackState.recentAlbumIds) {
        suggestedAlbumsFor(
            libraryState = libraryState,
            albumPlayCounts = albumPlayCounts,
            recentAlbumIds = playbackState.recentAlbumIds,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = topPadding + 8.dp,
                end = 20.dp,
                bottom = bottomPadding + if (isSearchUiActive) 84.dp else 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                val searchBarContentColor = MaterialTheme.colorScheme.onSurface
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.trim().isBlank()) {
                            showAllSongResults = false
                            showSearchSongSortOptions = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isSearchFieldFocused = focusState.isFocused
                        },
                    shape = RoundedCornerShape(ElovaireRadii.input),
                    singleLine = true,
                    placeholder = { Text("Artists, albums & more") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = isSearchUiActive,
                            enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                                scaleIn(
                                    animationSpec = ElovaireMotion.scaleSoft(),
                                    initialScale = 0.92f,
                                ),
                            exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                                scaleOut(
                                    animationSpec = ElovaireMotion.fadeFast(),
                                    targetScale = 0.92f,
                                ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(searchBarContentColor.copy(alpha = 0.1f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = resetSearchToMain,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_x),
                                    contentDescription = "Clear search",
                                    tint = searchBarContentColor.copy(alpha = 0.86f),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = searchBarContentColor.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = searchBarContentColor.copy(alpha = 0.5f),
                    ),
                )
            }
            item {
                ElovaireAnimatedContent(
                    targetState = contentMode,
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = {
                        when {
                            targetState == SearchContentMode.Discover -> {
                                (fadeIn(
                                    animationSpec = ElovaireMotion.contentFadeInSpec(delayMillis = 60),
                                ) + slideInVertically(
                                    animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Medium),
                                    initialOffsetY = { it / 14 },
                                )) togetherWith (fadeOut(
                                    animationSpec = ElovaireMotion.contentFadeOutSpec(),
                                ) + slideOutVertically(
                                    animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Fast),
                                    targetOffsetY = { -it / 18 },
                                ))
                            }

                            initialState == SearchContentMode.Results && targetState == SearchContentMode.AllSongs -> {
                                ElovaireMotion.fullScreenForwardEnter(
                                    initialOffsetX = { it / 10 },
                                ) togetherWith ElovaireMotion.fullScreenForwardExit(
                                    targetOffsetX = { -(it / 18) },
                                )
                            }

                            initialState == SearchContentMode.AllSongs && targetState == SearchContentMode.Results -> {
                                ElovaireMotion.fullScreenBackEnter(
                                    initialOffsetX = { -(it / 18) },
                                ) togetherWith ElovaireMotion.fullScreenBackExit(
                                    targetOffsetX = { it / 10 },
                                )
                            }

                            initialState == SearchContentMode.Discover -> {
                                (fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                                    slideInVertically(
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                        initialOffsetY = { it / 16 },
                                    )) togetherWith fadeOut(
                                    animationSpec = ElovaireMotion.contentFadeOutSpec(),
                                )
                            }

                            else -> ElovaireMotion.softContentTransform()
                        }
                    },
                    label = "SearchScreenContent",
                ) { mode ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        when (mode) {
                            SearchContentMode.AllSongs -> {
                                SearchSongsResultsHeader(
                                    resultCount = allMatchingSongs.size,
                                    selected = searchSongSortMode,
                                    expanded = showSearchSongSortOptions,
                                    onToggleExpanded = {
                                        showSearchSongSortOptions = !showSearchSongSortOptions
                                    },
                                    onSelect = { selectedMode ->
                                        searchSongSortMode = selectedMode
                                        showSearchSongSortOptions = false
                                    },
                                )
                                Surface(
                                    shape = RoundedCornerShape(ElovaireRadii.card),
                                    color = MaterialTheme.colorScheme.surface,
                                ) {
                                    Column {
                                        allMatchingSongs.forEachIndexed { index, song ->
                                            PlaylistSongRow(
                                                song = song,
                                                isFavorite = song.id in favoriteSongIds,
                                                isCurrentSong = song.id == playbackState.currentSong?.id,
                                                isPlaybackActive = playbackState.isPlaying,
                                                onClick = {
                                                    onRememberArtistSearch(song)
                                                    onSongSelected(song, allMatchingSongs)
                                                },
                                                onToggleFavorite = { onToggleFavorite(song.id) },
                                                showDivider = index != allMatchingSongs.lastIndex,
                                            )
                                        }
                                    }
                                }
                            }

                            SearchContentMode.Discover -> {
                                if (recentSearches.isNotEmpty()) {
                                    SearchHistorySectionHeader(
                                        showClearAction = true,
                                        onClearHistory = onClearSearchHistory,
                                    )
                                    SearchHistoryListCard(
                                        entries = recentSearches.take(6),
                                        onAlbumSelected = { albumId ->
                                            libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                                onAlbumSelected(album, ExpandOrigin())
                                            }
                                        },
                                        onArtistSelected = onArtistSelected,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 14.dp, bottom = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = "Nothing searched yet",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = "More results will show here as you search for songs and albums",
                                                style = secondaryBodyTextStyle(),
                                                color = readableSecondaryTextColor(),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(0.74f),
                                            )
                                        }
                                    }
                                }
                                if (suggestedAlbums.isNotEmpty()) {
                                    FavoriteAlbumsModule(
                                        albums = suggestedAlbums,
                                        title = "Suggested albums",
                                        subtitle = "You should probably revisit these",
                                        iconResId = R.drawable.ic_lucide_eye,
                                        onAlbumSelected = { album, origin ->
                                            onAlbumSelected(album, origin)
                                        },
                                    )
                                }
                            }

                            SearchContentMode.Results -> {
                                if (matchingArtists.isNotEmpty()) {
                                    SectionTitleRow(
                                        title = "Artists",
                                        subtitle = "${matchingArtists.size} matching artists",
                                    )
                                    SearchHistoryListCard(
                                        entries = matchingArtists,
                                        onAlbumSelected = { albumId ->
                                            libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                                onAlbumSelected(album, ExpandOrigin())
                                            }
                                        },
                                        onArtistSelected = onArtistSelected,
                                    )
                                }

                                if (matchingAlbums.isNotEmpty()) {
                                    SectionTitleRow(
                                        title = "Albums",
                                        subtitle = "${matchingAlbums.size} matching album results",
                                    )
                                    LazyRow(
                                        overscrollEffect = null,
                                        modifier = Modifier.horizontalGestureSafe(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        items(matchingAlbums, key = { it.id }) { album ->
                                            AlbumGridCard(
                                                album = album,
                                                modifier = Modifier.width(168.dp),
                                                onOpen = { origin ->
                                                    onRememberAlbumSearch(album)
                                                    onAlbumSelected(album, origin)
                                                },
                                            )
                                        }
                                    }
                                }

                                if (matchingSongs.isNotEmpty()) {
                                    SearchSongsPreviewHeader(
                                        resultCount = allMatchingSongs.size,
                                        showSeeAll = allMatchingSongs.size > matchingSongs.size,
                                        onShowAll = {
                                            focusManager.clearFocus(force = true)
                                            keyboardController?.hide()
                                            isSearchFieldFocused = false
                                            showAllSongResults = true
                                        },
                                    )
                                    Column {
                                        matchingSongs.forEachIndexed { index, song ->
                                            HomeRecentSongRow(
                                                song = song,
                                                isFavorite = song.id in favoriteSongIds,
                                                onClick = {
                                                    onRememberArtistSearch(song)
                                                    onSongSelected(song, matchingSongs)
                                                },
                                                onToggleFavorite = { onToggleFavorite(song.id) },
                                                showDivider = index != matchingSongs.lastIndex,
                                            )
                                        }
                                    }
                                }

                                if (matchingAlbums.isEmpty() && matchingSongs.isEmpty() && matchingArtists.isEmpty()) {
                                    EmptyStateCard(
                                        title = "No results",
                                        message = "Nothing in the current offline library matches \"$trimmedQuery\" yet",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchQuickPick(
    album: Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ArtworkImage(
            uri = album.artUri,
            title = album.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = ElovaireRadii.pill,
            showArtworkGlow = true,
        )
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun searchMatchesComposite(
    query: String,
    fields: List<String>,
): Boolean {
    val normalizedQuery = query
        .trim()
        .lowercase()
    if (normalizedQuery.isBlank()) return true
    val tokens = normalizedQuery
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    if (tokens.isEmpty()) return true
    val haystack = fields
        .asSequence()
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .joinToString(separator = " ")
    return tokens.all { token -> haystack.contains(token) }
}

@Composable
private fun SearchHistorySectionHeader(
    showClearAction: Boolean,
    onClearHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recently searched",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
        AnimatedVisibility(visible = showClearAction) {
            Surface(
                onClick = onClearHistory,
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                contentColor = if (MaterialTheme.colorScheme.primary.luminance() > 0.5f) InkText else Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_trash_2),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "Clear history",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSongsPreviewHeader(
    resultCount: Int,
    showSeeAll: Boolean,
    onShowAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTitleRow(
            title = "Songs",
            subtitle = "$resultCount matching song results",
        )
        AnimatedVisibility(visible = showSeeAll) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onShowAll,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
                    contentDescription = "Show all song results",
                    tint = readableMutedIconColor().copy(alpha = 0.82f),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(180f),
                )
            }
        }
    }
}

@Composable
private fun SearchSongsResultsHeader(
    resultCount: Int,
    selected: SearchSongSortMode,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelect: (SearchSongSortMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitleRow(
            title = "Songs",
            subtitle = "$resultCount matching song results",
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = onToggleExpanded,
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_arrow_down_up),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = selected.label,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Surface(
                    shape = RoundedCornerShape(ElovaireRadii.card),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column {
                        SearchSongSortMode.entries.forEachIndexed { index, mode ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onSelect(mode) },
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (mode == selected) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = if (mode == selected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        readableSecondaryTextColor()
                                    },
                                )
                            }
                            if (index != SearchSongSortMode.entries.lastIndex) {
                                DividerLine()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryListCard(
    entries: List<SearchHistoryEntry>,
    onAlbumSelected: (Long) -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(ElovaireRadii.card),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            entries.forEachIndexed { index, entry ->
                SearchHistoryListRow(
                    entry = entry,
                    onClick = {
                        when (entry.kind) {
                            SearchHistoryKind.Album -> entry.albumId?.let(onAlbumSelected)
                            SearchHistoryKind.Artist -> onArtistSelected(entry.query ?: entry.title)
                        }
                    },
                )
                if (index != entries.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryListRow(
    entry: SearchHistoryEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtworkImage(
            uri = entry.artUri,
            title = entry.title,
            modifier = Modifier.size(46.dp),
            cornerRadius = if (entry.kind == SearchHistoryKind.Artist) ElovaireRadii.pill else ElovaireRadii.artworkSmall,
            showArtworkGlow = entry.kind == SearchHistoryKind.Album,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchCategoryGrid(
    categories: List<Pair<String, Color>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                rowItems.forEach { (label, color) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(148.dp),
                        color = color,
                        shape = RoundedCornerShape(ElovaireRadii.card),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryModeToggle(
    layoutMode: AlbumLayoutMode,
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToggleIconChip(
            iconResId = R.drawable.ic_lucide_list,
            selected = layoutMode == AlbumLayoutMode.Compact,
            contentDescription = "Compact list",
            onClick = { onLayoutModeChanged(AlbumLayoutMode.Compact) },
        )
        ToggleIconChip(
            iconResId = R.drawable.ic_lucide_grid_2x2,
            selected = layoutMode == AlbumLayoutMode.Grid,
            contentDescription = "Grid",
            onClick = { onLayoutModeChanged(AlbumLayoutMode.Grid) },
        )
    }
}

@Composable
private fun ToggleIconChip(
    iconResId: Int,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            readableMutedIconColor()
        },
        animationSpec = tween(ElovaireMotion.Quick),
        label = "toggle_chip_content",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.54f,
            stiffness = 360f,
        ),
        label = "toggle_chip_scale",
    )
    Surface(
        modifier = Modifier.scale(scale),
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.button),
        color = Color.Transparent,
        contentColor = contentColor,
        shadowElevation = 0.dp,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun readableSecondaryTextColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f)
    }
}

@Composable
private fun secondaryBodyTextStyle(): TextStyle {
    return MaterialTheme.typography.bodyLarge.copy(
        lineHeight = elovaireScaledSp(19.2f),
    )
}

@Composable
private fun readableMutedIconColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    }
}

@Composable
private fun readableCardSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface
}

@Composable
private fun readableCardBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.07f)
    }
}

@Composable
private fun ModuleCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = readableCardSurfaceColor(),
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(readableCardSurfaceColor())
                .border(
                    width = 1.dp,
                    color = readableCardBorderColor(),
                    shape = RoundedCornerShape(ElovaireRadii.module),
                )
                .padding(18.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SectionTitleRow(
    title: String,
    subtitle: String? = null,
    compact: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)) {
        Text(
            text = title,
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = if (compact) {
                    MaterialTheme.typography.labelLarge
                } else {
                    secondaryBodyTextStyle()
                },
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun MutedSectionHeader(
    title: String,
    iconResId: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = readableMutedIconColor(),
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FavoriteAlbumsModule(
    albums: List<Album>,
    title: String = "Your favorite albums",
    subtitle: String = "Check out your most frequently played stuff",
    iconResId: Int = R.drawable.ic_lucide_star,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (darkTheme) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    }
    val borderColor = if (darkTheme) {
        Color.White.copy(alpha = 0.07f)
    } else {
        InkText.copy(alpha = 0.08f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(ElovaireRadii.module),
            )
            .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = readableMutedIconColor(),
                        modifier = Modifier
                            .size(18.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = readableSecondaryTextColor(),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                albums.chunked(2).take(3).forEach { rowAlbums ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowAlbums.forEach { album ->
                            FavoriteAlbumCompactCell(
                                album = album,
                                modifier = Modifier.weight(1f),
                                onOpen = { origin -> onAlbumSelected(album, origin) },
                            )
                        }
                        repeat((2 - rowAlbums.size).coerceAtLeast(0)) {
                            SpacerTile(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAlbumCompactCell(
    album: Album,
    modifier: Modifier = Modifier,
    onOpen: (ExpandOrigin) -> Unit,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val cellColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Surface(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() },
        shape = RoundedCornerShape(6.dp),
        color = cellColor,
        onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.size(48.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
                showArtworkGlow = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 0.72f,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CompactSongTile(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
        },
        shape = RoundedCornerShape(ElovaireRadii.tile),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = song.artUri,
                title = song.album,
                modifier = Modifier.size(48.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SongGridCard(
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            uri = song.artUri,
            title = song.album,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = ElovaireRadii.artwork,
            showArtworkGlow = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            InlineFavoriteSongButton(
                isFavorite = isFavorite,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onToggleFavorite,
            )
        }
    }
}

@Composable
private fun ArtistGridCard(
    artist: ArtistEntry,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            uri = artist.artUri,
            title = artist.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = ElovaireRadii.pill,
        )
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${formatCountLabel(artist.albumCount, "album")}  •  ${formatCountLabel(artist.songCount, "song")}",
            style = MaterialTheme.typography.labelLarge,
            color = readableSecondaryTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ArtistRow(
    artist: ArtistEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtworkImage(
            uri = artist.artUri,
            title = artist.name,
            modifier = Modifier.size(50.dp),
            cornerRadius = ElovaireRadii.pill,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatCountLabel(artist.albumCount, "album")}  •  ${formatCountLabel(artist.songCount, "song")}",
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GenreRow(
    genre: GenreEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
            },
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_gallery_vertical_end),
                    contentDescription = null,
                    tint = readableMutedIconColor().copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatCountLabel(genre.albumCount, "album"),
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
            contentDescription = null,
            tint = readableMutedIconColor().copy(alpha = 0.5f),
            modifier = Modifier
                .size(18.dp)
                .rotate(180f),
        )
    }
}

@Composable
private fun SpacerTile(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier)
}

@Composable
private fun AlbumPosterGrid(
    albums: List<Album>,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        albums.chunked(2).forEach { rowAlbums ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowAlbums.forEach { album ->
                    AlbumGridCard(
                        album = album,
                        modifier = Modifier.weight(1f),
                        onOpen = { origin -> onAlbumSelected(album, origin) },
                    )
                }
                repeat((2 - rowAlbums.size).coerceAtLeast(0)) {
                    SpacerTile(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecentSongRow(
    song: Song,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = readableCardSurfaceColor(),
        shape = RoundedCornerShape(ElovaireRadii.card),
        shadowElevation = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 6.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = song.artUri,
                title = song.album,
                modifier = Modifier.size(52.dp),
                cornerRadius = ElovaireRadii.artwork,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = readableSecondaryTextColor(),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ExplicitTitleText(
    title: String,
    isExplicit: Boolean,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val badgeFontSize = remember(style.fontSize) {
        if (style.fontSize == androidx.compose.ui.unit.TextUnit.Unspecified) 10.sp else (style.fontSize.value * 0.56f).sp
    }
    val titleText = remember(title, isExplicit) {
        buildAnnotatedString {
            append(title)
            if (isExplicit) {
                append(" ")
                pushStyle(
                    SpanStyle(
                        fontSize = badgeFontSize,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                append("🅴")
                pop()
            }
        }
    }
    Text(
        text = titleText,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
    )
}

@Composable
private fun SearchSongRow(
    song: Song,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplicitTitleText(
                        title = song.title,
                        isExplicit = song.isExplicit,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.width(78.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                SongOverflowMenuButton(
                    song = song,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun HomeRecentSongRow(
    song: Song,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isPlaybackActive: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 2.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.title,
                    modifier = Modifier.matchParentSize(),
                    cornerRadius = ElovaireRadii.artworkSmall,
                    showArtworkGlow = true,
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = isCurrentSong && isPlaybackActive,
                    enter = fadeIn(animationSpec = tween(160)),
                    exit = fadeOut(animationSpec = tween(160)),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedAudioLinesIcon(
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplicitTitleText(
                        title = song.title,
                        isExplicit = song.isExplicit,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.width(96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                InlineFavoriteSongButton(
                    isFavorite = isFavorite,
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onToggleFavorite,
                )
                SongOverflowMenuButton(
                    song = song,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun RecentAlbumGrid(
    albums: List<Album>,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        overscrollEffect = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(378.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumGridCard(
                album = album,
                modifier = Modifier.width(164.dp),
                onOpen = { origin -> onAlbumSelected(album, origin) },
            )
        }
    }
}

@Composable
private fun AlbumGridCard(
    album: Album,
    modifier: Modifier = Modifier,
    onOpen: (ExpandOrigin) -> Unit,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .clickable { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            uri = album.artUri,
            title = album.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = ElovaireRadii.artwork,
            showArtworkGlow = true,
        )
        Column(
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = album.artist,
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactAlbumRow(
    album: Album,
    onOpen: (ExpandOrigin) -> Unit,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Surface(
        onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
        shape = RoundedCornerShape(ElovaireRadii.tile),
        color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            MaterialTheme.colorScheme.background
        } else {
            readableCardSurfaceColor()
        },
        shadowElevation = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 6.dp else 1.dp,
        modifier = Modifier.onGloballyPositioned { bounds = it.boundsInWindow() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.size(62.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1f),
                            ),
                        ) {
                            append(formatCountLabel(album.songCount, "track"))
                        }
                        append("  •  ")
                        withStyle(
                            SpanStyle(
                                color = readableSecondaryTextColor().copy(alpha = 0.7f),
                            ),
                        ) {
                            append(formatDuration(album.durationMs))
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ElovaireRadii.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun PlaylistLaneCard(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = readableCardSurfaceColor(),
        shadowElevation = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 8.dp else 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.artworkSmall))
                    .background(
                        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.84f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_play),
                    contentDescription = null,
                    tint = readableMutedIconColor(),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyLarge,
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun AlbumScreen(
    album: Album?,
    favoriteSongIds: Set<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    collapsedTopBarTitle: String,
    onBack: () -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onShuffleAlbum: (Album) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
) {
    LaunchedEffect(album?.id) {
        if (album == null) {
            onBack()
        }
    }
    if (album == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Album not found.")
        }
        return
    }

    val gradient by rememberArtworkGradient(album.artUri)
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val albumSongIds = remember(album.songs) { album.songs.map(Song::id) }
    val discGroups = remember(album.songs) {
        album.songs
            .groupBy { it.discNumber.coerceAtLeast(1) }
            .toSortedMap()
            .entries
            .map { it.key to it.value }
    }
    val showDiscSections = discGroups.size > 1
    val isAlbumFavorite = albumSongIds.isNotEmpty() && albumSongIds.all { it in favoriteSongIds }
    val albumFavoriteBackground = gradient.first()
        .copy(alpha = if (isLightTheme) 0.2f else 0.26f)
        .compositeOver(
            if (isLightTheme) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            } else {
                Color.Black.copy(alpha = 0.18f)
            },
        )
    val albumFavoriteTint = if (albumFavoriteBackground.luminance() > 0.56f) InkText else Color.White
    val albumOnSurface = MaterialTheme.colorScheme.onSurface
    val albumActionBackground = gradient.first()
        .copy(alpha = if (isLightTheme) 0.18f else 0.28f)
        .compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = if (isLightTheme) 0.92f else 0.86f))
    val albumActionTint = if (albumActionBackground.luminance() > 0.56f) InkText else Color.White
    val albumSecondaryActionBackground = gradient.last()
        .copy(alpha = if (isLightTheme) 0.14f else 0.2f)
        .compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = if (isLightTheme) 0.9f else 0.8f))
    val albumSecondaryActionTint = if (albumSecondaryActionBackground.luminance() > 0.56f) InkText else Color.White
    val albumInfoPillBackground = gradient.first()
        .copy(alpha = if (isLightTheme) 0.1f else 0.16f)
        .compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = if (isLightTheme) 0.94f else 0.88f))
    val albumInfoPillTint = if (albumInfoPillBackground.luminance() > 0.56f) InkText else Color.White
    val density = LocalDensity.current
    var albumTitleBounds by remember(album.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val albumPrimarySong = remember(album.songs) {
        album.songs.firstOrNull()
    }
    val albumYear = remember(album.songs) {
        album.songs.firstNotNullOfOrNull { it.releaseYear }
    }
    val albumGenre = remember(album.songs) {
        album.songs.firstOrNull { it.genre.isNotBlank() && it.genre != "Unknown Genre" }?.genre
    }
    val albumTechnicalReferenceSong = remember(album.songs) {
        album.songs.firstOrNull { !it.audioQuality.isNullOrBlank() }
            ?: album.songs.firstOrNull()
    }
    val albumMetaItems = remember(album) {
        buildList {
            albumYear?.toString()?.let(::add)
            albumGenre
                ?.let(::add)
        }
    }
    val albumMetaText = remember(albumMetaItems, albumOnSurface) {
        buildAnnotatedString {
            albumMetaItems.forEachIndexed { index, item ->
                if (index > 0) {
                    pushStyle(SpanStyle(color = albumOnSurface.copy(alpha = 0.72f)))
                    append("  •  ")
                    pop()
                }
                val isYear = index == 0 && albumYear != null
                pushStyle(
                    SpanStyle(
                        color = if (isYear) albumOnSurface else albumOnSurface.copy(alpha = 0.72f),
                        fontWeight = if (isYear) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
                append(item)
                pop()
            }
        }
    }
    val albumFooterText = remember(album.songCount, album.durationMs, albumOnSurface) {
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    color = albumOnSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal,
                ),
            )
            append("${album.songCount} tracks")
            pop()
            pushStyle(SpanStyle(color = albumOnSurface.copy(alpha = 0.5f)))
            append("  •  ")
            pop()
            pushStyle(
                SpanStyle(
                    color = albumOnSurface,
                    fontWeight = FontWeight.Normal,
                ),
            )
            append(formatDuration(album.durationMs))
            pop()
        }
    }
    val detailTopPadding = detailTopBarOccupiedHeight()
    val topBarBottomPx = with(density) { detailTopPadding.roundToPx() }
    val detailTopBarTitle = if ((albumTitleBounds?.top ?: Float.MAX_VALUE) < topBarBottomPx) {
        album.title
    } else {
        collapsedTopBarTitle
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val listState = rememberElovaireLazyListState(album.id, "album_detail")
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopPadding + ElovaireSpacing.albumHeaderTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 28.dp, vertical = 36.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            gradient.first().copy(alpha = if (isLightTheme) 0.28f else 0.41f),
                                            gradient.last().copy(alpha = if (isLightTheme) 0.1f else 0.14f),
                                            Color.Transparent,
                                        ),
                                        radius = 780f,
                                    ),
                                    shape = RoundedCornerShape(ElovaireRadii.module),
                                )
                                .blur(if (isLightTheme) 32.dp else 40.dp),
                        )
                        Surface(
                            modifier = Modifier.matchParentSize(),
                            shape = RoundedCornerShape(ElovaireRadii.module),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isLightTheme) 0.08f else 0.16f),
                            tonalElevation = 0.dp,
                            shadowElevation = 22.dp,
                        ) {
                            ArtworkImage(
                                uri = album.artUri,
                                title = album.title,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = ElovaireRadii.artwork,
                            )
                        }
                        FavoriteSongButton(
                            isFavorite = isAlbumFavorite,
                            tint = albumFavoriteTint,
                            backgroundColor = albumFavoriteBackground,
                            borderColor = Color.White.copy(alpha = 0.16f),
                            frosted = true,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp),
                            onClick = {
                                onSetAlbumFavorite(albumSongIds, !isAlbumFavorite)
                            },
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = elovaireScaledSp(ALBUM_HEADER_TITLE_TEXT_SIZE_SP),
                                lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8f,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                albumTitleBounds = coordinates.boundsInWindow()
                            },
                        )
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = elovaireScaledSp(ALBUM_HEADER_ARTIST_TEXT_SIZE_SP),
                                fontWeight = FontWeight.Medium,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = albumMetaText,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                                color = albumOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = albumInfoPillBackground,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_audio_waveform),
                                        contentDescription = null,
                                        tint = albumInfoPillTint.copy(alpha = 0.82f),
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = albumTechnicalReferenceSong?.audioFormat ?: "AUDIO",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(11f)),
                                        color = albumInfoPillTint.copy(alpha = 0.94f),
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = albumTechnicalReferenceSong?.audioQuality ?: "--",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(11f)),
                                        color = albumInfoPillTint.copy(alpha = 0.74f),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumHeaderPlayButton(
                                tint = albumActionTint,
                                backgroundColor = albumActionBackground,
                                onClick = { onPlayAlbum(album) },
                            )
                            AlbumHeaderActionButton(
                                iconResId = R.drawable.ic_lucide_shuffle,
                                contentDescription = "Shuffle album",
                                tint = albumSecondaryActionTint,
                                backgroundColor = albumSecondaryActionBackground,
                                onClick = { onShuffleAlbum(album) },
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }

            discGroups.forEachIndexed { discGroupIndex, (discNumber, discSongs) ->
                if (showDiscSections) {
                    item("disc_header_$discNumber") {
                        DiscSectionHeader(discNumber = discNumber)
                    }
                }

                itemsIndexed(
                    items = discSongs,
                    key = { _, song -> song.id },
                    contentType = { _, _ -> "album_song_row" },
                ) { index, song ->
                    GroupedListRowContainer(
                        index = index,
                        lastIndex = discSongs.lastIndex,
                    ) {
                        AlbumSongRow(
                            song = song,
                            trackIndex = if (song.trackNumber > 0) song.trackNumber else index + 1,
                            isFavorite = song.id in favoriteSongIds,
                            isCurrentSong = song.id == currentSongId,
                            isPlaybackActive = isCurrentSongPlaying,
                            onClick = { onSongSelected(song, album.songs) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            showDivider = index != discSongs.lastIndex,
                        )
                    }
                }

                if (showDiscSections && discGroupIndex != discGroups.lastIndex) {
                    item("disc_spacer_$discNumber") {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 6.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = albumFooterText,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                        color = albumOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        DetailListTopBar(
            title = detailTopBarTitle,
            subtitle = null,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun DiscSectionHeader(
    discNumber: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_disc_3),
            contentDescription = null,
            tint = readableMutedIconColor(),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Disc $discNumber",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AnimatedAudioLinesIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val phase by rememberInfiniteTransition(label = "audio_lines_phase").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "audio_lines_value",
    )
    val baseHeights = floatArrayOf(0.26f, 0.54f, 0.84f, 0.42f, 0.68f, 0.3f)
    val phaseOffsets = floatArrayOf(0f, 0.17f, 0.31f, 0.48f, 0.67f, 0.83f)
    Canvas(modifier = modifier) {
        val lineWidth = size.width / 10f
        val gap = (size.width - (lineWidth * baseHeights.size)) / (baseHeights.size - 1).coerceAtLeast(1)
        val centerY = size.height / 2f
        baseHeights.forEachIndexed { index, baseHeight ->
            val animationFactor = (
                (
                    sin(((phase + phaseOffsets[index]) * 6.2831855f).toDouble()) + 1.0
                    ) / 2.0
                ).toFloat()
            val heightFactor = (baseHeight * 0.68f) + (animationFactor * 0.22f)
            val lineHeight = size.height * heightFactor
            val startX = index * (lineWidth + gap) + (lineWidth / 2f)
            drawLine(
                color = tint,
                start = Offset(startX, centerY - (lineHeight / 2f)),
                end = Offset(startX, centerY + (lineHeight / 2f)),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun AlbumSongRow(
    song: Song,
    trackIndex: Int,
    isFavorite: Boolean,
    isCurrentSong: Boolean,
    isPlaybackActive: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = isCurrentSong && isPlaybackActive,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                    },
                    label = "album_row_track_indicator",
                ) { showSignal ->
                    if (showSignal) {
                        AnimatedAudioLinesIcon(
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text(
                            text = trackIndex.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplicitTitleText(
                        title = song.title,
                        isExplicit = song.isExplicit,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.width(94.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                InlineFavoriteSongButton(
                    isFavorite = isFavorite,
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onToggleFavorite,
                )
                SongOverflowMenuButton(
                    song = song,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun PlaylistDetailScreen(
    playlist: Playlist?,
    librarySongs: List<Song>,
    favoriteSongIds: Set<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onPlayPlaylist: (List<Song>, String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAddSongs: (List<Long>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    if (playlist == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Playlist not found.")
        }
        return
    }

    var showAddSongsDialog by rememberSaveable { mutableStateOf(false) }
    val songsById = remember(librarySongs) { librarySongs.associateBy { it.id } }
    val playlistSongs = remember(playlist.songIds, songsById) {
        playlist.songIds.mapNotNull(songsById::get)
    }
    val detailTopPadding = detailTopBarOccupiedHeight()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val listState = rememberElovaireLazyListState(playlist.id, "playlist_detail")
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopPadding + ElovaireSpacing.albumHeaderTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PlaylistArtworkPreview(
                        songs = playlistSongs,
                        title = playlist.name,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatCountLabel(playlistSongs.size, "track"),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        AlbumHeaderPlayButton(
                            tint = MaterialTheme.colorScheme.onSurface,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                            onClick = { onPlayPlaylist(playlistSongs, playlist.name) },
                        )
                    }
                    if (!playlist.isSystem) {
                        SelectablePill(
                            label = "Add songs",
                            selected = true,
                            onClick = { showAddSongsDialog = true },
                        )
                    }
                }
            }
            if (playlistSongs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            if (playlistSongs.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No songs yet",
                        message = "Start building this playlist by adding songs from your offline library",
                    )
                }
            } else {
                itemsIndexed(
                    items = playlistSongs,
                    key = { index, song -> "${song.id}_$index" },
                    contentType = { _, _ -> "playlist_song_row" },
                ) { index, song ->
                    GroupedListRowContainer(
                        index = index,
                        lastIndex = playlistSongs.lastIndex,
                    ) {
                        PlaylistSongRow(
                            song = song,
                            isFavorite = song.id in favoriteSongIds,
                            isCurrentSong = song.id == currentSongId,
                            isPlaybackActive = isCurrentSongPlaying,
                            onClick = { onSongSelected(song, playlistSongs) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            showDivider = index != playlistSongs.lastIndex,
                        )
                    }
                }
            }
        }

        DetailListTopBar(
            title = playlist.name,
            subtitle = "${playlistSongs.size} songs",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    if (showAddSongsDialog && !playlist.isSystem) {
        AddSongsToPlaylistDialog(
            availableSongs = librarySongs,
            existingSongIds = playlist.songIds.toSet(),
            onDismiss = { showAddSongsDialog = false },
            onAddSongs = { selectedSongIds ->
                onAddSongs(selectedSongIds)
                showAddSongsDialog = false
            },
        )
    }
}

@Composable
private fun PlaylistSongRow(
    song: Song,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isPlaybackActive: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showOverflowMenu: Boolean = false,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.album,
                    modifier = Modifier.matchParentSize(),
                    cornerRadius = ElovaireRadii.artworkSmall,
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = isCurrentSong && isPlaybackActive,
                    enter = fadeIn(animationSpec = tween(160)),
                    exit = fadeOut(animationSpec = tween(160)),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedAudioLinesIcon(
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExplicitTitleText(
                        title = song.title,
                        isExplicit = song.isExplicit,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.width(if (showOverflowMenu) 96.dp else 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                InlineFavoriteSongButton(
                    isFavorite = isFavorite,
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onToggleFavorite,
                )
                if (showOverflowMenu) {
                    SongOverflowMenuButton(
                        song = song,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

@Composable
private fun GroupedListRowContainer(
    index: Int,
    lastIndex: Int,
    content: @Composable () -> Unit,
) {
    val shape = when {
        lastIndex <= 0 -> RoundedCornerShape(ElovaireRadii.card)
        index == 0 -> RoundedCornerShape(
            topStart = ElovaireRadii.card,
            topEnd = ElovaireRadii.card,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )
        index == lastIndex -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = ElovaireRadii.card,
            bottomEnd = ElovaireRadii.card,
        )
        else -> RoundedCornerShape(0.dp)
    }
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.padding(
                top = if (index == 0) 6.dp else 0.dp,
                bottom = if (index == lastIndex) 6.dp else 0.dp,
            ),
        ) {
            content()
        }
    }
}

@Composable
private fun DetailListTopBar(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useSharedBackdrop = LocalUseSharedTopBarBackdrop.current
    if (useSharedBackdrop && !LocalRenderSharedTopBarContent.current) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Detail(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
            ),
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (useSharedBackdrop) 8f else 0f)
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
        if (!useSharedBackdrop) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconButton(
                iconResId = R.drawable.ic_lucide_chevron_left,
                contentDescription = "Back",
                showBackground = false,
                onClick = onBack,
                modifier = Modifier.zIndex(1f),
            )
            if (subtitle.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180, delayMillis = 40))
                                .togetherWith(fadeOut(animationSpec = tween(140)))
                        },
                        label = "detailTopBarTitleOnly",
                    ) { currentTitle ->
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .zIndex(1f)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180, delayMillis = 40))
                                .togetherWith(fadeOut(animationSpec = tween(140)))
                        },
                        label = "detailTopBarTitleWithSubtitle",
                    ) { currentTitle ->
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSongsToPlaylistDialog(
    availableSongs: List<Song>,
    existingSongIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddSongs: (List<Long>) -> Unit,
) {
    val candidates = remember(availableSongs, existingSongIds) {
        availableSongs.filterNot { it.id in existingSongIds }.take(24)
    }
    val selectedSongIds = remember { mutableStateOf(setOf<Long>()) }
    val listState = rememberLazyListState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add songs") },
        text = {
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier
                    .height(320.dp)
                    .ensureSingleItemRubberBand(listState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(candidates, key = { it.id }) { song ->
                    Surface(
                        onClick = {
                            selectedSongIds.value = if (song.id in selectedSongIds.value) {
                                selectedSongIds.value - song.id
                            } else {
                                selectedSongIds.value + song.id
                            }
                        },
                        shape = RoundedCornerShape(ElovaireRadii.tile),
                        color = if (song.id in selectedSongIds.value) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ArtworkImage(
                                uri = song.artUri,
                                title = song.title,
                                modifier = Modifier.size(42.dp),
                                cornerRadius = ElovaireRadii.artworkSmall,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAddSongs(selectedSongIds.value.toList()) },
                enabled = selectedSongIds.value.isNotEmpty(),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun NowPlayingScreen(
    playbackManager: PlaybackManager,
    playbackState: PlaybackUiState,
    enrichedSongsById: Map<Long, Song>,
    isFavorite: Boolean,
    lyricsService: LyricsService,
    onBack: () -> Unit,
    onOpenCurrentAlbum: (Long) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onQueueItemSelected: (Int) -> Unit,
    eqSettings: EqSettings,
    onSpaciousnessChanged: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val liveCurrentSong = playbackState.currentSong
    val liveDisplaySong = liveCurrentSong?.let { enrichedSongsById[it.id] ?: it }
    val playbackProgress by playbackManager.progressState.collectAsStateWithLifecycle()
    val playerHazeState = rememberHazeState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var playerDismissTriggered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(liveCurrentSong?.id) {
        if (liveCurrentSong == null) {
            if (!playerDismissTriggered) {
                playerDismissTriggered = true
                onBack()
            }
        } else {
            playerDismissTriggered = false
        }
    }
    val appBackground = MaterialTheme.colorScheme.background
    val gradient = rememberArtworkGradient(liveCurrentSong?.artUri).value
    val artwork = rememberArtworkBitmap(liveCurrentSong?.artUri, size = 768)
    val activeTransitionSnapshot = remember(transitionSnapshot, liveCurrentSong?.id) {
        transitionSnapshot?.takeIf {
            it.songId == liveCurrentSong?.id &&
                it.barBounds.isValidTransitionBounds &&
                it.artworkBounds.isValidTransitionBounds
        }
    }
    val transitionProgress = remember(liveCurrentSong?.id, activeTransitionSnapshot?.songId) {
        Animatable(if (activeTransitionSnapshot != null) 0f else 1f)
    }
    var transitionState by remember(liveCurrentSong?.id, activeTransitionSnapshot?.songId) {
        mutableStateOf(
            if (activeTransitionSnapshot != null) {
                PlayerOverlayTransitionState.Expanding
            } else {
                PlayerOverlayTransitionState.Expanded
            },
        )
    }
    val expandSettleAnimationSpec = remember {
        tween<Float>(
            durationMillis = 420,
            easing = FastOutSlowInEasing,
        )
    }
    val collapseSettleAnimationSpec = remember {
        tween<Float>(
            durationMillis = 340,
            easing = FastOutSlowInEasing,
        )
    }
    var interactiveTransitionProgress by remember(liveCurrentSong?.id) { mutableStateOf<Float?>(null) }
    var dismissAnimationRunning by remember(liveCurrentSong?.id) { mutableStateOf(false) }
    val effectiveTransitionProgress = interactiveTransitionProgress ?: transitionProgress.value
    val transitionInFlight = transitionState != PlayerOverlayTransitionState.Expanded || interactiveTransitionProgress != null || dismissAnimationRunning
    var frozenPlaybackProgress by remember(liveCurrentSong?.id) { mutableStateOf(playbackProgress) }
    LaunchedEffect(playbackProgress, transitionInFlight, liveCurrentSong?.id) {
        if (!transitionInFlight) {
            frozenPlaybackProgress = playbackProgress
        }
    }
    val renderedPlaybackProgress = if (transitionInFlight) frozenPlaybackProgress else playbackProgress
    val adaptivePalette = remember(gradient, appBackground) {
        buildPlayerAdaptivePalette(
            gradient = gradient,
            appBackground = appBackground,
            darkTheme = false,
        )
    }
    val tintColor by animateColorAsState(
        targetValue = adaptivePalette.tintColor,
        animationSpec = tween(420, easing = LinearOutSlowInEasing),
        label = "player_tint_color",
    )
    val baseSurface by animateColorAsState(
        targetValue = adaptivePalette.backdropBase,
        animationSpec = tween(420, easing = LinearOutSlowInEasing),
        label = "player_backdrop_base",
    )
    val contentColor by animateColorAsState(
        targetValue = adaptivePalette.contentColor,
        animationSpec = tween(360, easing = LinearOutSlowInEasing),
        label = "player_content_color",
    )
    val secondaryContentColor by animateColorAsState(
        targetValue = adaptivePalette.secondaryContentColor,
        animationSpec = tween(360, easing = LinearOutSlowInEasing),
        label = "player_secondary_content_color",
    )
    val currentSong = liveCurrentSong
    val displaySong = liveDisplaySong
    val playingFromText = remember(playbackState.sourceLabel, currentSong?.album) {
        playbackState.sourceLabel
            ?.takeIf { it.isNotBlank() }
            ?.let { "Playing from $it" }
            ?: currentSong?.album?.takeIf { it.isNotBlank() }?.let { "Playing from $it" }
            ?: "Playing from all songs"
    }
    var showLyricsSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var showQueueSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var queueStatusText by remember(currentSong?.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(currentSong?.id, playbackState.currentIndex, playbackState.queue.size) {
        val queue = playbackState.queue
        val currentIndex = playbackState.currentIndex
        lyricsService.cancelObsoleteRequests(
            listOf(
                currentSong,
                queue.getOrNull(currentIndex + 1),
                queue.getOrNull(currentIndex - 1),
            ),
        )
        currentSong?.let { lyricsService.prefetchLyrics(it) }
        queue.getOrNull(currentIndex + 1)?.let { lyricsService.prefetchLyrics(it) }
        queue.getOrNull(currentIndex - 1)?.let { lyricsService.prefetchLyrics(it) }
    }
    LaunchedEffect(queueStatusText) {
        if (queueStatusText != null) {
            delay(1500L)
            queueStatusText = null
        }
    }
    val lyricsUiState by produceState<LyricsUiState>(
        initialValue = when {
            !showLyricsSheet || currentSong == null -> LyricsUiState.Hidden
            else -> lyricsService.cachedLyrics(currentSong, includeNotFound = false)?.toUiState() ?: LyricsUiState.Loading
        },
        key1 = showLyricsSheet,
        key2 = currentSong?.id,
    ) {
        if (!showLyricsSheet || currentSong == null) {
            value = LyricsUiState.Hidden
            return@produceState
        }

        lyricsService.cachedLyrics(currentSong, includeNotFound = false)?.let { cached ->
            value = cached.toUiState()
            return@produceState
        }

        value = LyricsUiState.Loading
        val fetchedResult = withTimeoutOrNull(4_200L) {
            lyricsService.fetchLyrics(
                song = currentSong,
                allowCachedNotFound = false,
                lookupMode = LyricsLookupMode.Full,
            )
        } ?: LyricsResult.Timeout

        value = when (fetchedResult) {
            is LyricsResult.Found -> LyricsUiState.Ready(fetchedResult.payload)
            LyricsResult.Timeout -> {
                lyricsService.cachedLyrics(currentSong, includeNotFound = false)?.toUiState()
                    ?: if (lyricsService.isLookupInFlight(currentSong)) LyricsUiState.Loading else LyricsUiState.Empty
            }
            LyricsResult.NotFound -> {
                if (lyricsService.isLookupInFlight(currentSong)) {
                    LyricsUiState.Loading
                } else {
                    LyricsUiState.Empty
                }
            }
        }
    }
    DisposableEffect(currentSong?.id) {
        onDispose {
            playbackManager.cancelScrub()
        }
    }

    suspend fun settlePlayerTransition(
        targetValue: Float,
        animationSpec: AnimationSpec<Float>,
        targetState: PlayerOverlayTransitionState,
    ) {
        val startValue = interactiveTransitionProgress ?: transitionProgress.value
        interactiveTransitionProgress = null
        transitionState = if (targetValue >= startValue) {
            PlayerOverlayTransitionState.Expanding
        } else {
            PlayerOverlayTransitionState.Collapsing
        }
        transitionProgress.stop()
        transitionProgress.snapTo(startValue)
        transitionProgress.animateTo(
            targetValue = targetValue,
            animationSpec = animationSpec,
        )
        transitionState = targetState
    }

    LaunchedEffect(currentSong?.id, activeTransitionSnapshot?.songId) {
        if (currentSong == null || dismissAnimationRunning || transitionState == PlayerOverlayTransitionState.Collapsing) {
            return@LaunchedEffect
        }
        if (activeTransitionSnapshot != null && transitionProgress.value < 1f) {
            settlePlayerTransition(
                targetValue = 1f,
                animationSpec = expandSettleAnimationSpec,
                targetState = PlayerOverlayTransitionState.Expanded,
            )
        } else if (activeTransitionSnapshot == null && transitionProgress.value != 1f) {
            transitionProgress.stop()
            transitionProgress.snapTo(1f)
            transitionState = PlayerOverlayTransitionState.Expanded
        }
    }

    val dismissNowPlaying: ((() -> Unit)?) -> Unit = { afterDismiss ->
        if (!dismissAnimationRunning && transitionState != PlayerOverlayTransitionState.Compact) {
            dismissAnimationRunning = true
            scope.launch {
                settlePlayerTransition(
                    targetValue = 0f,
                    animationSpec = collapseSettleAnimationSpec,
                    targetState = PlayerOverlayTransitionState.Compact,
                )
                if (afterDismiss != null) {
                    afterDismiss()
                } else {
                    onBack()
                }
            }
        }
    }

    BackHandler(enabled = !showLyricsSheet) {
        dismissNowPlaying(null)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .then(
                if (transitionInFlight) {
                    Modifier
                } else {
                    Modifier.hazeSource(playerHazeState)
                },
            ),
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val fullSurfaceBounds = remember(screenWidthPx, screenHeightPx) {
            androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = 0f,
                right = screenWidthPx,
                bottom = screenHeightPx,
            )
        }
        val fallbackSourceBounds = remember(screenWidthPx, screenHeightPx, density) {
            val horizontalInset = with(density) { 16.dp.toPx() }
            val bottomInset = with(density) { 88.dp.toPx() }
            val barHeight = with(density) { 72.dp.toPx() }
            androidx.compose.ui.geometry.Rect(
                left = horizontalInset,
                top = screenHeightPx - bottomInset - barHeight,
                right = screenWidthPx - horizontalInset,
                bottom = screenHeightPx - bottomInset,
            )
        }
        val sourceSurfaceBounds = (activeTransitionSnapshot?.barBounds ?: fallbackSourceBounds).coerceWithin(fullSurfaceBounds)
        val sourceArtworkBounds = (activeTransitionSnapshot?.artworkBounds ?: fallbackSourceBounds).coerceWithin(fullSurfaceBounds)
        val statusBarTopInsetPx = WindowInsets.statusBars.getTop(density).toFloat()
        val fallbackTargetArtworkBounds = remember(screenWidthPx, statusBarTopInsetPx, density) {
            val horizontalInset = with(density) { 20.dp.toPx() }
            val artworkSize = screenWidthPx - (horizontalInset * 2f)
            val topInset = statusBarTopInsetPx + with(density) { 70.dp.toPx() }
            androidx.compose.ui.geometry.Rect(
                left = horizontalInset,
                top = topInset,
                right = horizontalInset + artworkSize,
                bottom = topInset + artworkSize,
            )
        }
        val targetArtworkBounds = fallbackTargetArtworkBounds.coerceWithin(fullSurfaceBounds)
        val animatedSurfaceBounds = lerpRect(sourceSurfaceBounds, fullSurfaceBounds, effectiveTransitionProgress)
        val artworkRevealProgress = ((effectiveTransitionProgress - 0.08f) / 0.92f).coerceIn(0f, 1f)
        val contentRevealProgress = ((effectiveTransitionProgress - 0.22f) / 0.78f).coerceIn(0f, 1f)
        val playerContentAlpha = if (showLyricsSheet) 0f else contentRevealProgress
        val playerSurfaceCorner = lerpFloat(with(density) { ElovaireRadii.card.toPx() }, 0f, effectiveTransitionProgress)
        val sharedArtworkBounds = lerpRect(sourceArtworkBounds, targetArtworkBounds, artworkRevealProgress).coerceWithin(fullSurfaceBounds)
        val volumeSectionProgress = ((effectiveTransitionProgress - 0.22f) / 0.16f).coerceIn(0f, 1f)
        val actionsSectionProgress = ((effectiveTransitionProgress - 0.34f) / 0.16f).coerceIn(0f, 1f)
        val transportSectionProgress = ((effectiveTransitionProgress - 0.48f) / 0.16f).coerceIn(0f, 1f)
        val progressSectionProgress = ((effectiveTransitionProgress - 0.6f) / 0.15f).coerceIn(0f, 1f)
        val metadataSectionProgress = ((effectiveTransitionProgress - 0.72f) / 0.14f).coerceIn(0f, 1f)
        val useSharedArtworkOverlay =
            activeTransitionSnapshot != null &&
                transitionState != PlayerOverlayTransitionState.Expanded &&
                sourceArtworkBounds.isValidTransitionBounds &&
                targetArtworkBounds.isValidTransitionBounds &&
                sharedArtworkBounds.isValidTransitionBounds &&
                artwork.value != null

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    baseSurface.copy(alpha = 0.68f * effectiveTransitionProgress.coerceIn(0f, 1f)),
                ),
        )
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedSurfaceBounds.left.roundToInt(),
                        y = animatedSurfaceBounds.top.roundToInt(),
                    )
                }
                .width(with(density) { animatedSurfaceBounds.width.toDp() })
                .height(with(density) { animatedSurfaceBounds.height.toDp() })
                .clip(RoundedCornerShape(with(density) { playerSurfaceCorner.toDp() }))
                .background(baseSurface)
                .graphicsLayer {
                    clip = true
                },
        ) {
        val backgroundArtworkBitmap = artwork.value
        if (backgroundArtworkBitmap != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (transitionInFlight) {
                    Image(
                        bitmap = backgroundArtworkBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.04f
                                scaleY = 1.04f
                            }
                            .blur(56.dp),
                        alpha = 0.92f,
                    )
                } else {
                    Image(
                        bitmap = backgroundArtworkBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.08f
                                scaleY = 1.08f
                            }
                            .blur(116.dp),
                        alpha = 0.98f,
                    )
                    Image(
                        bitmap = backgroundArtworkBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.03f
                                scaleY = 1.03f
                                alpha = 0.34f
                            }
                            .blur(48.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tintColor.copy(alpha = 0.38f),
                            baseSurface.copy(alpha = 0.44f),
                            baseSurface.copy(alpha = 0.7f),
                            baseSurface.copy(alpha = 0.9f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gradient.first().copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        radius = 1200f,
                    ),
                ),
        )

        CompositionLocalProvider(LocalPlayerHazeState provides playerHazeState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 20.dp)
                    .alpha(playerContentAlpha),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            if (currentSong == null) {
                Spacer(modifier = Modifier.fillMaxSize())
                return@Column
            }

            val centeredInfoWidth = 0.95f
            val nowPlayingTitleTopGap = ElovaireSpacing.nowPlayingTitleTopGap
            val nowPlayingTitleBottomGap = ElovaireSpacing.nowPlayingTitleBottomGap
            val displayedPositionMs = renderedPlaybackProgress.displayPositionMs
            val displayedProgressFraction = remember(renderedPlaybackProgress.displayPositionMs, renderedPlaybackProgress.durationMs) {
                if (renderedPlaybackProgress.durationMs > 0L) {
                    (renderedPlaybackProgress.displayPositionMs.toFloat() / renderedPlaybackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            val transportShowsPause = remember(
                playbackState.currentSong?.id,
                playbackState.transportShowsPause,
                currentSong.id,
            ) {
                playbackState.currentSong?.id == currentSong.id && playbackState.transportShowsPause
            }
            val spaciousnessEnabled = eqSettings.spaciousness > 0.02f
            val favoriteAlpha by animateFloatAsState(
                targetValue = if (showQueueSheet) 0f else 1f,
                animationSpec = tween(180),
                label = "queue_favorite_alpha",
            )
            val transportAlpha by animateFloatAsState(
                targetValue = if (showQueueSheet) 0f else 1f,
                animationSpec = tween(180),
                label = "queue_transport_alpha",
            )
            val animatedArtworkCornerRadius by animateDpAsState(
                targetValue = if (showQueueSheet) 10.dp else ElovaireRadii.module,
                animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                label = "queue_artwork_corner_radius",
            )
            fun Modifier.nowPlayingDismissGesture(): Modifier = pointerInput(currentSong.id) {
                var dragDistance = 0f
                val dismissDistance = with(density) { 320.dp.toPx() }
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dismissAnimationRunning) return@detectVerticalDragGestures
                        dragDistance = (dragDistance + dragAmount).coerceAtLeast(0f)
                        transitionState = PlayerOverlayTransitionState.Dragging
                        interactiveTransitionProgress =
                            (1f - (dragDistance / dismissDistance)).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        val progress = interactiveTransitionProgress ?: 1f
                        dragDistance = 0f
                        if (progress < 0.6f) {
                            dismissNowPlaying(null)
                        } else {
                            scope.launch {
                                settlePlayerTransition(
                                    targetValue = 1f,
                                    animationSpec = expandSettleAnimationSpec,
                                    targetState = PlayerOverlayTransitionState.Expanded,
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        if (dismissAnimationRunning) return@detectVerticalDragGestures
                        dragDistance = 0f
                        scope.launch {
                            settlePlayerTransition(
                                targetValue = 1f,
                                animationSpec = expandSettleAnimationSpec,
                                targetState = PlayerOverlayTransitionState.Expanded,
                            )
                        }
                    },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                    ) {
                        HeaderIconButton(
                            iconResId = R.drawable.ic_lucide_chevron_down,
                            contentDescription = "Minimize",
                            showBackground = false,
                            tint = contentColor,
                            onClick = { dismissNowPlaying(null) },
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 64.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_circle_play),
                                contentDescription = null,
                                tint = secondaryContentColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = playingFromText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                                color = secondaryContentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .nowPlayingDismissGesture(),
                    ) {
                        val expandedArtworkWidth = maxWidth
                        val compactArtworkWidth = maxWidth * 0.38f
                        val animatedArtworkWidth by animateDpAsState(
                            targetValue = if (showQueueSheet) compactArtworkWidth else expandedArtworkWidth,
                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                            label = "queue_artwork_width",
                        )
                        val compactContentStart = compactArtworkWidth + 18.dp
                        if (!useSharedArtworkOverlay) {
                            AnimatedContent(
                                targetState = currentSong.id,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith
                                        fadeOut(animationSpec = tween(180))
                                },
                                label = "player_artwork_content",
                            ) { songId ->
                                val animatedSong = playbackState.queue.firstOrNull { it.id == songId } ?: currentSong
                                ArtworkImage(
                                    uri = animatedSong.artUri,
                                    title = animatedSong.title,
                                    modifier = Modifier
                                        .width(animatedArtworkWidth)
                                        .aspectRatio(1f),
                                    cornerRadius = animatedArtworkCornerRadius,
                                    requestedSizePx = 1024,
                                )
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showQueueSheet,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = compactContentStart, end = 2.dp),
                            enter = fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing)) +
                                slideInVertically(
                                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 5 },
                                ),
                            exit = fadeOut(animationSpec = tween(120)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ExplicitTitleText(
                                            title = currentSong.title,
                                            isExplicit = currentSong.isExplicit,
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(NOW_PLAYING_TITLE_TEXT_SIZE_SP)),
                                            color = contentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .basicMarquee(
                                                    iterations = Int.MAX_VALUE,
                                                    animationMode = MarqueeAnimationMode.Immediately,
                                                    repeatDelayMillis = 2500,
                                                    initialDelayMillis = 2500,
                                                    velocity = 24.dp,
                                                ),
                                        )
                                    }
                                    Text(
                                        text = currentSong.artist,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(NOW_PLAYING_ARTIST_TEXT_SIZE_SP)),
                                        color = secondaryContentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier.basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                            repeatDelayMillis = 2500,
                                            initialDelayMillis = 2500,
                                            velocity = 24.dp,
                                        ),
                                    )
                                }
                                CompactPlaybackProgressBar(
                                    progress = displayedProgressFraction,
                                    contentColor = contentColor,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                CompactPlaybackTimingRow(
                                    displayedPositionMs = displayedPositionMs,
                                    durationMs = renderedPlaybackProgress.durationMs,
                                    contentColor = contentColor,
                                    secondaryContentColor = secondaryContentColor,
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = nowPlayingTitleTopGap, bottom = nowPlayingTitleBottomGap),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(centeredInfoWidth)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            alpha = if (showQueueSheet) 0f else metadataSectionProgress
                        },
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    currentSong.takeIf { it.albumId > 0L }?.albumId?.let { albumId ->
                                        dismissNowPlaying {
                                            onOpenCurrentAlbum(albumId)
                                        }
                                    }
                                },
                            ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedContent(
                            targetState = currentSong.id,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith
                                    fadeOut(animationSpec = tween(180))
                            },
                            label = "player_metadata_content",
                            modifier = Modifier.weight(1f),
                        ) { songId ->
                            val animatedSong = playbackState.queue.firstOrNull { it.id == songId } ?: currentSong
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ExplicitTitleText(
                                        title = animatedSong.title,
                                        isExplicit = animatedSong.isExplicit,
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(NOW_PLAYING_TITLE_TEXT_SIZE_SP)),
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                                repeatDelayMillis = 2500,
                                                initialDelayMillis = 2500,
                                                velocity = 28.dp,
                                            ),
                                    )
                                }
                                Text(
                                    text = animatedSong.artist,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(NOW_PLAYING_ARTIST_TEXT_SIZE_SP)),
                                    color = secondaryContentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .alpha(favoriteAlpha * if (showQueueSheet) 0f else metadataSectionProgress),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        FavoriteSongButton(
                            isFavorite = isFavorite,
                            tint = contentColor,
                            onClick = { onToggleFavorite(currentSong.id) },
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(centeredInfoWidth)
                    .align(Alignment.CenterHorizontally)
                    .weight(1f),
            ) {
                val queueSheetTopExtension = 28.dp
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = if (showQueueSheet) 0f else progressSectionProgress
                                },
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                PlaybackProgressBar(
                                    progress = displayedProgressFraction,
                                    isInteracting = renderedPlaybackProgress.isUserScrubbing,
                                    contentColor = contentColor,
                                    onScrubStarted = {
                                        playbackManager.beginScrub()
                                    },
                                    onScrubFractionChanged = { fraction ->
                                        val target = fractionToDurationPosition(
                                            fraction = fraction,
                                            durationMs = renderedPlaybackProgress.durationMs,
                                        )
                                        playbackManager.updateScrubPosition(target)
                                    },
                                    onScrubFinished = { fraction ->
                                        val target = fractionToDurationPosition(
                                            fraction = fraction,
                                            durationMs = renderedPlaybackProgress.durationMs,
                                        )
                                        playbackManager.finishScrub(target)
                                    },
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Text(
                                            text = formatPlaybackPosition(displayedPositionMs),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = secondaryContentColor,
                                        )
                                    }
                                    SongFileInfoPill(
                                        format = displaySong?.audioFormat ?: currentSong.audioFormat,
                                        quality = displaySong?.audioQuality ?: currentSong.audioQuality,
                                        tint = contentColor,
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Text(
                                            text = formatDuration(renderedPlaybackProgress.durationMs),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = secondaryContentColor,
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = if (showQueueSheet) 0f else transportSectionProgress * transportAlpha
                                    },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(22.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        PlayerTransportButton(
                                            iconResId = R.drawable.ic_elovaire_backward_filled,
                                            contentDescription = "Previous",
                                            tint = contentColor,
                                            iconSize = 42.dp,
                                            onClick = onSkipPrevious,
                                        )
                                        PlayerTransportButton(
                                            iconResId = if (transportShowsPause) R.drawable.ic_elovaire_pause_filled else R.drawable.ic_lucide_play,
                                            contentDescription = if (transportShowsPause) "Pause" else "Play",
                                            tint = contentColor,
                                            iconSize = 46.dp,
                                            onClick = onTogglePlayback,
                                        )
                                        PlayerTransportButton(
                                            iconResId = R.drawable.ic_elovaire_forward_filled,
                                            contentDescription = "Next",
                                            tint = contentColor,
                                            iconSize = 42.dp,
                                            onClick = onSkipNext,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = if (showQueueSheet) 0f else actionsSectionProgress
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerSecondaryActionButton(
                            iconResId = R.drawable.ic_lucide_align_left,
                            label = "Lyrics",
                            tint = contentColor,
                            showBackground = false,
                            onClick = {
                                showQueueSheet = false
                                showLyricsSheet = true
                            },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        PlayerSecondaryActionButton(
                            iconResId = repeatModeIconRes(playbackState.repeatMode),
                            label = repeatModeLabel(playbackState.repeatMode),
                            tint = contentColor,
                            showBackground = playbackState.repeatMode != PlaybackRepeatMode.Off,
                            onClick = onCycleRepeatMode,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        PlayerSecondaryActionButton(
                            iconResId = R.drawable.ic_lucide_list_music,
                            label = "Queue",
                            tint = contentColor,
                            showBackground = showQueueSheet,
                            onClick = {
                                showLyricsSheet = false
                                showQueueSheet = !showQueueSheet
                            },
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showQueueSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard)) +
                        scaleIn(
                            initialScale = 0.94f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                        ) +
                        slideInHorizontally(
                            initialOffsetX = { it / 14 },
                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                        ) +
                        slideInVertically(
                            initialOffsetY = { it / 14 },
                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                        ),
                    exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
                        scaleOut(
                            targetScale = 0.98f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = tween(ElovaireMotion.Quick),
                        ),
                ) {
                    QueueSheet(
                        queue = playbackState.queue,
                        currentIndex = playbackState.currentIndex,
                        tint = contentColor,
                        secondaryTint = secondaryContentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxHeight + queueSheetTopExtension)
                            .align(Alignment.BottomCenter),
                        onSongSelected = onQueueItemSelected,
                        shuffleEnabled = playbackState.shuffleEnabled,
                        onToggleShuffle = {
                            queueStatusText = if (playbackState.shuffleEnabled) {
                                "Shuffle | Disabled"
                            } else {
                                "Shuffle | Enabled"
                            }
                            onToggleShuffle()
                        },
                        spaciousnessEnabled = spaciousnessEnabled,
                        onToggleSpaciousness = {
                            val enabling = !spaciousnessEnabled
                            queueStatusText = if (enabling) "Spaciousness | Enabled" else null
                            onSpaciousnessChanged(if (enabling) 0.5f else 0f)
                        },
                        spaciousnessAmount = eqSettings.spaciousness.coerceIn(0f, 1f),
                        onSpaciousnessAmountChanged = onSpaciousnessChanged,
                        statusText = queueStatusText,
                        onDismiss = { showQueueSheet = false },
                        isPlaying = playbackState.isPlaying,
                    )
                }
            }

            VolumeControlBar(
                volume = playbackState.volume,
                contentColor = contentColor,
                onVolumeChanged = onVolumeChanged,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = volumeSectionProgress
                    }
                    .fillMaxWidth(centeredInfoWidth)
                    .align(Alignment.CenterHorizontally),
            )
            }
        }
        }
        if (useSharedArtworkOverlay && currentSong != null) {
            val sharedArtworkCornerRadius = with(density) {
                lerpFloat(
                    ElovaireRadii.artworkSmall.toPx(),
                    ElovaireRadii.module.toPx(),
                    artworkRevealProgress,
                ).toDp()
            }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = sharedArtworkBounds.left.roundToInt(),
                            y = sharedArtworkBounds.top.roundToInt(),
                        )
                    }
                    .width(with(density) { sharedArtworkBounds.width.toDp() })
                    .height(with(density) { sharedArtworkBounds.height.toDp() })
                    .clipToBounds()
                    .graphicsLayer {
                        clip = true
                        shape = RoundedCornerShape(sharedArtworkCornerRadius)
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val sharedArtworkBitmap = artwork.value
                if (sharedArtworkBitmap != null) {
                    Image(
                        bitmap = sharedArtworkBitmap,
                        contentDescription = currentSong.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = showLyricsSheet,
            enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 12 },
                ) +
                scaleIn(
                    animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                    initialScale = 0.985f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
            exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick, easing = FastOutLinearInEasing)) +
                slideOutVertically(
                    animationSpec = tween(ElovaireMotion.Quick, easing = FastOutSlowInEasing),
                    targetOffsetY = { it / 18 },
                ) +
                scaleOut(
                    animationSpec = tween(ElovaireMotion.Quick, easing = FastOutLinearInEasing),
                    targetScale = 0.992f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
        ) {
            LyricsOverlay(
                song = currentSong,
                playbackProgress = renderedPlaybackProgress,
                lyricsUiState = lyricsUiState,
                tintColor = baseSurface.copy(alpha = 0.66f),
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onSeekTo = playbackManager::seekTo,
                onHideLyrics = { showLyricsSheet = false },
            )
        }
    }
}

@Composable
private fun SongFileInfoPill(
    format: String,
    quality: String?,
    tint: Color,
) {
    Surface(
        modifier = Modifier.playerFrostedSurface(tint = tint),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = tint.copy(alpha = 0.2f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_audio_waveform),
                contentDescription = null,
                tint = tint.copy(alpha = 0.82f),
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = format.ifBlank { "AUDIO" },
                style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(11f)),
                color = tint.copy(alpha = 0.92f),
                maxLines = 1,
            )
            Text(
                text = quality ?: "--",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(11f)),
                color = tint.copy(alpha = 0.72f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CompactPlaybackProgressBar(
    progress: Float,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(ElovaireRadii.pill))
                .background(contentColor.copy(alpha = 0.18f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(clampedProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(ElovaireRadii.pill))
                .background(contentColor),
        )
    }
}

@Composable
private fun CompactPlaybackTimingRow(
    displayedPositionMs: Long,
    durationMs: Long,
    contentColor: Color,
    secondaryContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatPlaybackPosition(displayedPositionMs),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "・",
            style = MaterialTheme.typography.labelLarge,
            color = contentColor.copy(alpha = 0.5f),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatDuration(durationMs),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
            color = secondaryContentColor.copy(alpha = 0.7f),
            maxLines = 1,
        )
    }
}

@Composable
private fun QueueSheet(
    queue: List<Song>,
    currentIndex: Int,
    tint: Color,
    secondaryTint: Color,
    onSongSelected: (Int) -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    spaciousnessEnabled: Boolean,
    onToggleSpaciousness: () -> Unit,
    spaciousnessAmount: Float,
    onSpaciousnessAmountChanged: (Float) -> Unit,
    statusText: String?,
    onDismiss: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberElovaireLazyListState("equalizer_screen")
    var showSpaciousnessSlider by remember(spaciousnessEnabled) { mutableStateOf(spaciousnessEnabled) }
    val footerExpanded = showSpaciousnessSlider || statusText != null
    val footerHeight by animateDpAsState(
        targetValue = if (footerExpanded) 90.dp else 60.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "queue_footer_height",
    )
    LaunchedEffect(spaciousnessEnabled) {
        if (!spaciousnessEnabled) {
            showSpaciousnessSlider = false
        }
    }
    LaunchedEffect(currentIndex, queue.size) {
        if (currentIndex in queue.indices) {
            listState.scrollToItem((currentIndex - 2).coerceAtLeast(0))
        }
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .playerFrostedSurface(tint = tint),
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = tint.copy(alpha = 0.05f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_list_music),
                            contentDescription = null,
                            tint = tint.copy(alpha = 0.92f),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Queue",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = elovaireScaledSp(18f),
                                fontWeight = FontWeight.Medium,
                            ),
                            color = tint,
                        )
                    }
                    Row(
                        modifier = Modifier.offset(x = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatCountLabel(queue.size, "track"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                            color = secondaryTint.copy(alpha = 0.7f),
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(tint.copy(alpha = 0.2f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDismiss,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_x),
                                contentDescription = "Close queue",
                                tint = tint.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            QueueSeparator(tint = tint, modifier = Modifier.fillMaxWidth())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                LazyColumn(
                    state = listState,
                    overscrollEffect = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(listState),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    itemsIndexed(queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                        QueueSongRow(
                            song = song,
                            index = index,
                            active = index == currentIndex,
                            tint = tint,
                            secondaryTint = secondaryTint,
                            showDivider = index != queue.lastIndex,
                            onClick = { onSongSelected(index) },
                            isPlaying = isPlaying,
                        )
                    }
                }
            }
            QueueSeparator(tint = tint, modifier = Modifier.fillMaxWidth())
            val bottomSectionOpacity = 0.1f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(footerHeight)
                    .background(tint.copy(alpha = bottomSectionOpacity)),
            ) {
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing)) +
                            slideInVertically(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 5 },
                            ) togetherWith
                            fadeOut(animationSpec = tween(140))
                    },
                    label = "queue_status_text",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                ) { queueStatus ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (queueStatus != null && !showSpaciousnessSlider) {
                            Text(
                                text = queueStatus,
                                style = MaterialTheme.typography.labelLarge,
                                color = tint.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showSpaciousnessSlider,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                    enter = fadeIn(animationSpec = tween(180)) + slideInVertically(
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 3 },
                    ),
                    exit = fadeOut(animationSpec = tween(140)) + slideOutVertically(
                        animationSpec = tween(180, easing = FastOutLinearInEasing),
                        targetOffsetY = { it / 4 },
                    ),
                ) {
                    ThinContinuousSlider(
                        value = spaciousnessAmount.coerceIn(0f, 1f),
                        onValueChange = {
                            val clamped = it.coerceIn(0f, 1f)
                            onSpaciousnessAmountChanged(clamped)
                            if (clamped <= 0.001f) {
                                showSpaciousnessSlider = false
                                if (spaciousnessEnabled) {
                                    onToggleSpaciousness()
                                }
                            }
                        },
                        valueRange = 0f..1f,
                        lineThickness = 4.dp,
                        knobSize = 18.dp,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(60.dp)
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_wind,
                        label = if (spaciousnessEnabled || showSpaciousnessSlider) "${(spaciousnessAmount.coerceIn(0f, 1f) * 100f).roundToInt()}%" else "",
                        tint = tint,
                        showBackground = spaciousnessEnabled,
                        onClick = {
                            if (!spaciousnessEnabled) {
                                showSpaciousnessSlider = true
                                if (spaciousnessAmount < 0.02f) {
                                    onSpaciousnessAmountChanged(0.5f)
                                }
                                onToggleSpaciousness()
                            } else if (!showSpaciousnessSlider) {
                                showSpaciousnessSlider = true
                            } else {
                                showSpaciousnessSlider = false
                                onToggleSpaciousness()
                            }
                        },
                    )
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_shuffle,
                        label = if (shuffleEnabled) "Shuffle" else "",
                        tint = tint,
                        showBackground = shuffleEnabled,
                        onClick = onToggleShuffle,
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueSeparator(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(tint.copy(alpha = 0.3f)),
    )
}

@Composable
private fun QueueSongRow(
    song: Song,
    index: Int,
    active: Boolean,
    isPlaying: Boolean,
    tint: Color,
    secondaryTint: Color,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(30.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = active && isPlaying,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                    },
                    label = "queue_row_track_indicator",
                ) { showSignal ->
                    if (showSignal) {
                        AnimatedAudioLinesIcon(
                            tint = tint.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = secondaryTint,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium),
                    color = if (active) tint else tint.copy(alpha = 0.84f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryTint.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.labelLarge,
                color = secondaryTint.copy(alpha = 0.78f),
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
        if (showDivider) {
            QueueSeparator(
                tint = tint,
                modifier = Modifier
                    .fillMaxWidth(0.9f),
            )
        }
    }
}

@Composable
private fun PlayerTransportButton(
    iconResId: Int,
    contentDescription: String,
    tint: Color,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "${contentDescription}_transport_scale",
    )
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = iconResId,
            transitionSpec = {
                (
                    fadeIn(animationSpec = ElovaireMotion.iconSwapInSpec()) +
                        scaleIn(
                            initialScale = 0.9f,
                            animationSpec = ElovaireMotion.releaseSpringSpec(
                                dampingRatio = 0.8f,
                                stiffness = 520f,
                            ),
                        )
                    ) togetherWith
                    (
                        fadeOut(animationSpec = ElovaireMotion.iconSwapOutSpec()) +
                            scaleOut(
                                targetScale = 1.04f,
                                animationSpec = ElovaireMotion.contentFadeOutSpec(),
                            )
                        )
            },
            label = "${contentDescription}_transport_icon",
        ) { currentIcon ->
            Icon(
                painter = painterResource(id = currentIcon),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
private fun QueueMenuButton(
    iconResId: Int,
    tint: Color,
    active: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (active) 0.2f else 0f,
        animationSpec = ElovaireMotion.contentFadeInSpec(),
        label = "queue_button_alpha",
    )
    val buttonScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.9f
            active -> 1f
            else -> 0.96f
        },
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.82f,
            stiffness = 520f,
        ),
        label = "queue_button_scale",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .playerFrostedSurface(tint = tint)
            .background(tint.copy(alpha = backgroundAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "Queue",
            tint = tint.copy(alpha = 0.92f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun FavoriteSongButton(
    isFavorite: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = tint.copy(alpha = 0.2f),
    borderColor: Color = Color.Transparent,
    frosted: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionDurationScale = rememberSystemAnimationScale()
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(ElovaireMotion.scaleDurationMillis(180L, motionDurationScale))
            shouldBounce = false
            previousFavoriteState = isFavorite
        }
    }
    val buttonScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            shouldBounce -> 1.08f
            else -> 1f
        },
        animationSpec = if (shouldBounce) {
            ElovaireMotion.bounceSpringSpec()
        } else {
            ElovaireMotion.releaseSpringSpec()
        },
        label = "favorite_button_scale",
    )
    val iconScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.84f
            shouldBounce -> 1.12f
            isFavorite -> 1f
            else -> 0.96f
        },
        animationSpec = if (shouldBounce) {
            ElovaireMotion.bounceSpringSpec()
        } else {
            ElovaireMotion.releaseSpringSpec(
                dampingRatio = 0.8f,
                stiffness = 520f,
            )
        },
        label = "favorite_icon_scale",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (frosted) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(18.dp)
                    .background(backgroundColor.copy(alpha = 0.86f)),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor),
            )
            if (borderColor.alpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(1.dp, borderColor, CircleShape),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor),
            )
        }
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (
                    fadeIn(animationSpec = ElovaireMotion.iconSwapInSpec()) +
                        scaleIn(
                            initialScale = 0.88f,
                            animationSpec = ElovaireMotion.releaseSpringSpec(),
                        )
                    ) togetherWith
                    (
                        fadeOut(animationSpec = ElovaireMotion.iconSwapOutSpec()) +
                            scaleOut(
                                targetScale = 1.04f,
                                animationSpec = ElovaireMotion.contentFadeOutSpec(),
                            )
                        )
            },
            label = "favorite_button_icon",
        ) { favorite ->
            Icon(
                painter = painterResource(
                    id = if (favorite) R.drawable.ic_lucide_star_filled else R.drawable.ic_lucide_star,
                ),
                contentDescription = if (favorite) "Unlike song" else "Like song",
                tint = tint,
                modifier = Modifier
                    .size(20.dp)
                    .scale(iconScale),
            )
        }
    }
}

@Composable
private fun AlbumHeaderActionButton(
    iconResId: Int,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "${contentDescription}_album_header_scale",
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AlbumHeaderPlayButton(
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "album_play_button_scale",
    )

    Surface(
        modifier = Modifier.scale(scale),
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_circle_play),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Play",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(16f)),
                color = tint,
            )
        }
    }
}

@Composable
private fun InlineFavoriteSongButton(
    isFavorite: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionDurationScale = rememberSystemAnimationScale()
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(ElovaireMotion.scaleDurationMillis(180L, motionDurationScale))
            shouldBounce = false
            previousFavoriteState = isFavorite
        }
    }
    val buttonScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.8f
            shouldBounce -> 1.12f
            else -> 1f
        },
        animationSpec = if (shouldBounce) {
            ElovaireMotion.bounceSpringSpec()
        } else {
            ElovaireMotion.releaseSpringSpec(
                dampingRatio = 0.8f,
                stiffness = 520f,
            )
        },
        label = "inline_favorite_scale",
    )
    val iconScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.8f
            shouldBounce -> 1.18f
            isFavorite -> 1f
            else -> 0.96f
        },
        animationSpec = if (shouldBounce) {
            ElovaireMotion.bounceSpringSpec()
        } else {
            ElovaireMotion.releaseSpringSpec(
                dampingRatio = 0.8f,
                stiffness = 520f,
            )
        },
        label = "inline_favorite_icon_scale",
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(buttonScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (
                    fadeIn(animationSpec = ElovaireMotion.iconSwapInSpec()) +
                        scaleIn(
                            initialScale = 0.88f,
                            animationSpec = ElovaireMotion.releaseSpringSpec(),
                        )
                    ) togetherWith
                    (
                        fadeOut(animationSpec = ElovaireMotion.iconSwapOutSpec()) +
                            scaleOut(
                                targetScale = 1.04f,
                                animationSpec = ElovaireMotion.contentFadeOutSpec(),
                            )
                        )
            },
            label = "inline_favorite_icon",
        ) { favorite ->
            Icon(
                painter = painterResource(
                    id = if (favorite) R.drawable.ic_lucide_star_filled else R.drawable.ic_lucide_star,
                ),
                contentDescription = if (favorite) "Unlike song" else "Like song",
                tint = tint.copy(alpha = if (favorite) 1f else 0.82f),
                modifier = Modifier
                    .size(18.dp)
                    .scale(iconScale),
            )
        }
    }
}

@Composable
private fun SongOverflowMenuButton(
    song: Song,
    tint: Color,
) {
    val actions = LocalSongMenuActions.current
    var expanded by remember(song.id) { mutableStateOf(false) }
    var shouldRenderMenu by remember(song.id) { mutableStateOf(false) }
    var showPlaylistDialog by remember(song.id) { mutableStateOf(false) }
    val motionDurationScale = rememberSystemAnimationScale()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 380f,
        ),
        label = "song_overflow_scale",
    )
    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        } else if (shouldRenderMenu) {
            delay(ElovaireMotion.scaleDurationMillis(180L, motionDurationScale))
            shouldRenderMenu = false
        }
    }

    Box {
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(buttonScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        expanded = true
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_ellipsis_vertical),
                contentDescription = "Song options",
                tint = tint.copy(alpha = 0.82f),
                modifier = Modifier.size(21.6.dp),
            )
        }

        if (shouldRenderMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 0.dp, y = (-10).dp),
                containerColor = Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(170, easing = LinearOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 0.94f,
                            transformOrigin = TransformOrigin(1f, 0f),
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                        ) +
                        slideInVertically(
                            initialOffsetY = { -it / 6 },
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                        ),
                    exit = fadeOut(animationSpec = tween(130, easing = FastOutLinearInEasing)) +
                        scaleOut(
                            targetScale = 0.98f,
                            transformOrigin = TransformOrigin(1f, 0f),
                            animationSpec = tween(130, easing = FastOutLinearInEasing),
                        ),
                    label = "SongOverflowMenuVisibility",
                ) {
                    FrostedContextMenuSurface(
                        modifier = Modifier.width(208.dp),
                    ) {
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_list_music,
                            text = "Add to playlist",
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                expanded = false
                                showPlaylistDialog = true
                            },
                        )
                        DividerLine()
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_plus,
                            text = "Add to queue",
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                expanded = false
                                actions.onAddToQueue(song)
                            },
                        )
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_trash_2,
                            text = "Delete from library",
                            tint = DestructiveRed,
                            containerColor = DestructiveRed.copy(alpha = 0.2f),
                            bottomPadding = 10.dp,
                            onClick = {
                                expanded = false
                                actions.onDeleteFromLibrary(song)
                            },
                        )
                    }
                }
            }
        }
    }

    if (showPlaylistDialog) {
        AddToPlaylistPickerDialog(
            playlists = actions.playlists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                actions.onAddToPlaylist(playlistId, song)
                showPlaylistDialog = false
            },
        )
    }
}

@Composable
private fun FrostedContextMenuSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(ElovaireRadii.card)
    DynamicBackdropSurface(
        modifier = modifier,
        shape = shape,
        overlayAlpha = 0.7f,
        borderColor = blurSurfaceBorderColor(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun TopBarContextMenuOverlay(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 10.dp),
            enter = fadeIn(animationSpec = tween(170, easing = LinearOutSlowInEasing)) +
                scaleIn(
                    initialScale = 0.94f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                ) +
                slideInVertically(
                    initialOffsetY = { -it / 6 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                ),
            exit = fadeOut(animationSpec = tween(130, easing = FastOutLinearInEasing)) +
                scaleOut(
                    targetScale = 0.98f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = tween(130, easing = FastOutLinearInEasing),
                ),
            label = "TopBarContextMenuVisibility",
        ) {
            FrostedContextMenuSurface(
                modifier = Modifier.width(190.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_settings,
                        text = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        topPadding = 8.dp,
                        onClick = onOpenSettings,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_audio_waveform,
                        text = "Equalizer",
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenEqualizer,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_list,
                        text = "Changelog",
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenChangelog,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_info,
                        text = "About",
                        tint = MaterialTheme.colorScheme.onSurface,
                        topPadding = 6.dp,
                        bottomPadding = 8.dp,
                        onClick = onOpenAbout,
                    )
                }
            }
        }
    }
}

@Composable
private fun SongContextMenuItem(
    @DrawableRes iconResId: Int,
    text: String,
    tint: Color,
    containerColor: Color = Color.Transparent,
    topPadding: Dp = 6.dp,
    bottomPadding: Dp = 6.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = topPadding, end = 10.dp, bottom = bottomPadding)
            .clip(RoundedCornerShape(ElovaireRadii.card * 0.72f))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = tint,
            )
        }
    }
}

@Composable
private fun AddToPlaylistPickerDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
) {
    val listState = rememberElovaireLazyListState("settings_screen")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = "Create a playlist first to start saving songs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = readableSecondaryTextColor(),
                )
            } else {
                LazyColumn(
                    state = listState,
                    overscrollEffect = null,
                    modifier = Modifier
                        .height(280.dp)
                        .ensureSingleItemRubberBand(listState),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Surface(
                            onClick = { onPlaylistSelected(playlist.id) },
                            shape = RoundedCornerShape(ElovaireRadii.tile),
                            color = readableCardSurfaceColor(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_list_music),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                                    modifier = Modifier.size(17.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = formatCountLabel(playlist.songIds.size, "song"),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = readableSecondaryTextColor(),
                                    )
                                }
                            }
                        }
                        if (playlist != playlists.last()) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun LyricsOverlay(
    song: Song?,
    playbackProgress: PlaybackProgressState,
    lyricsUiState: LyricsUiState,
    tintColor: Color,
    contentColor: Color,
    secondaryContentColor: Color,
    onSeekTo: (Long) -> Unit,
    onHideLyrics: () -> Unit,
) {
    BackHandler(onBack = onHideLyrics)
    val scope = rememberCoroutineScope()
    val hideButtonArea = 112.dp
    val lyricsBottomBlurArea = 72.dp
    val bottomBlurSurfaceHeight = lyricsBottomBlurArea + navigationBarInsetDp()
    val lyricsHazeState = rememberHazeState()
    val lyricLines = remember(lyricsUiState) {
        when (lyricsUiState) {
            is LyricsUiState.Ready -> lyricsUiState.payload.lines

            else -> emptyList()
        }
    }
    val listState = rememberLazyListState()
    var autoScrollHeld by remember(song?.id) { mutableStateOf(false) }
    var autoScrollResumeJob by remember(song?.id) { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var userLyricsScrollActive by remember(song?.id) { mutableStateOf(false) }
    val readyLyricsPayload = (lyricsUiState as? LyricsUiState.Ready)?.payload
    val autoScrollCenterOffsetPx = with(LocalDensity.current) { 180.dp.roundToPx() }
    val activeLyricLineIndex by remember(readyLyricsPayload, playbackProgress.displayPositionMs) {
        derivedStateOf {
            readyLyricsPayload
                ?.takeIf { it.isSynced }
                ?.currentLineIndexAt(
                    positionMs = playbackProgress.displayPositionMs,
                    timingOffsetMs = 0L,
                    switchGraceMs = 0L,
                )
                ?: -1
        }
    }

    LaunchedEffect(activeLyricLineIndex, readyLyricsPayload?.isSynced, autoScrollHeld) {
        if (!autoScrollHeld && readyLyricsPayload?.isSynced == true && activeLyricLineIndex >= 0) {
            listState.animateLyricJumpToItem(
                index = activeLyricLineIndex,
                scrollOffset = -autoScrollCenterOffsetPx,
            )
        }
    }

    LaunchedEffect(listState.isScrollInProgress, userLyricsScrollActive) {
        if (userLyricsScrollActive && !listState.isScrollInProgress) {
            autoScrollResumeJob?.cancel()
            autoScrollResumeJob = scope.launch {
                delay(1_600L)
                autoScrollHeld = false
                userLyricsScrollActive = false
            }
        }
    }

    val lyricsScrollObserver = remember(song?.id) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    autoScrollHeld = true
                    userLyricsScrollActive = true
                    autoScrollResumeJob?.cancel()
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.9f),
                        tintColor.copy(alpha = 0.84f),
                        tintColor.copy(alpha = 0.92f),
                    ),
                ),
            ),
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(lyricsHazeState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                song?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_circle_play),
                            contentDescription = null,
                            tint = secondaryContentColor,
                            modifier = Modifier.size(18.dp),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = elovaireScaledSp(17f),
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = it.artist,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = elovaireScaledSp(15f),
                                ),
                                color = secondaryContentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(screenWidth * 0.9f)
                            .height(1.dp)
                            .background(contentColor.copy(alpha = 0.2f)),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    AnimatedContent(
                        targetState = lyricsUiState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(ElovaireMotion.Standard, easing = LinearOutSlowInEasing)) +
                                slideInVertically(
                                    animationSpec = tween(ElovaireMotion.ScreenExpand, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 12 },
                                ) +
                                expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween(ElovaireMotion.ScreenExpand, easing = FastOutSlowInEasing),
                                ) togetherWith
                                fadeOut(animationSpec = tween(ElovaireMotion.Quick, easing = FastOutLinearInEasing))
                        },
                        contentKey = { state -> state::class },
                        label = "lyrics_content_state",
                    ) { state ->
                        when (state) {
                            LyricsUiState.Hidden -> Unit
                            LyricsUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Loading lyrics...",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = contentColor,
                                    )
                                }
                            }

                            LyricsUiState.Empty -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_lucide_info),
                                            contentDescription = null,
                                            tint = contentColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = "This song seems to have no lyrics",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = contentColor,
                                        )
                                    }
                                }
                            }

                            is LyricsUiState.Ready -> {
                                val bottomMaskHeightPx = with(LocalDensity.current) {
                                    (hideButtonArea + lyricsBottomBlurArea).toPx()
                                }
                                LazyColumn(
                                    state = listState,
                                    overscrollEffect = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                        .drawWithContent {
                                            drawContent()
                                            val maskStartY = (size.height - bottomMaskHeightPx).coerceAtLeast(0f)
                                            val maskStartFraction = if (size.height == 0f) 0f else {
                                                (maskStartY / size.height).coerceIn(0f, 1f)
                                            }
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    colorStops = arrayOf(
                                                        0f to Color.Black,
                                                        maskStartFraction to Color.Black,
                                                        1f to Color.Transparent,
                                                    ),
                                                ),
                                                blendMode = BlendMode.DstIn,
                                            )
                                        }
                                        .nestedScroll(lyricsScrollObserver)
                                        .ensureSingleItemRubberBand(listState),
                                    contentPadding = PaddingValues(
                                        top = 12.dp,
                                        bottom = hideButtonArea + lyricsBottomBlurArea,
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    itemsIndexed(
                                        items = lyricLines,
                                        key = { _, line -> "${line.index}:${line.startTimeMs}:${line.text}" },
                                    ) { index, line ->
                                        val isActive = state.payload.isSynced && index == activeLyricLineIndex
                                        val lineFontSize by animateFloatAsState(
                                            targetValue = if (isActive) 24f else 22f,
                                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                                            label = "lyrics_line_font_$index",
                                        )
                                        val lineColor by animateColorAsState(
                                            targetValue = when {
                                                isActive -> contentColor.copy(alpha = 1f)
                                                else -> contentColor.copy(alpha = 0.7f)
                                            },
                                            animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                                            label = "lyrics_line_color_$index",
                                        )
                                        Text(
                                            text = line.text,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontSize = lineFontSize.sp,
                                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                                lineHeight = if (isActive) 31.sp else 29.sp,
                                            ),
                                            color = lineColor,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(
                                                    song?.id,
                                                    lyricLines.size,
                                                    state.payload.isSynced,
                                                    activeLyricLineIndex,
                                                ) {
                                                    detectTapGestures {
                                                        lyricsSeekPositionMs(
                                                            lines = lyricLines,
                                                            index = index,
                                                            isSynced = state.payload.isSynced,
                                                        )?.let { seekPositionMs ->
                                                            autoScrollHeld = false
                                                            userLyricsScrollActive = false
                                                            autoScrollResumeJob?.cancel()
                                                            scope.launch {
                                                                listState.animateLyricJumpToItem(
                                                                    index = index,
                                                                    scrollOffset = -autoScrollCenterOffsetPx,
                                                                )
                                                            }
                                                            onSeekTo(seekPositionMs)
                                                        }
                                                    }
                                                },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBlurSurfaceHeight)
                .clipToBounds()
                .zIndex(3f),
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .hazeEffect(lyricsHazeState) {
                            progressive = HazeProgressive.LinearGradient(
                                startIntensity = 0f,
                                endIntensity = 1f,
                                preferPerformance = true,
                            )
                            blurRadius = 34.dp
                            backgroundColor = Color.Transparent
                            tints = listOf(
                                HazeTint(tintColor.copy(alpha = 0.06f)),
                                HazeTint(tintColor.copy(alpha = 0.02f)),
                            )
                            noiseFactor = 0.02f
                        },
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                tintColor.copy(alpha = 0.08f),
                                tintColor.copy(alpha = 0.28f),
                                tintColor.copy(alpha = 0.62f),
                                tintColor.copy(alpha = 0.96f),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(lyricsBottomBlurArea)
                    .offset(y = (-6).dp)
                    .padding(horizontal = 20.dp)
                    .zIndex(4f),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    onClick = onHideLyrics,
                    shape = RoundedCornerShape(ElovaireRadii.pill),
                    color = contentColor.copy(alpha = 0.18f),
                    contentColor = contentColor,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_eye_off),
                            contentDescription = "Hide lyrics",
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Hide lyrics",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSecondaryActionButton(
    iconResId: Int,
    label: String,
    tint: Color,
    showBackground: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var transientHighlight by remember { mutableStateOf(false) }
    val motionDurationScale = rememberSystemAnimationScale()
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showBackground || transientHighlight) 0.2f else 0f,
        animationSpec = tween(ElovaireMotion.Standard),
        label = "${label}_button_alpha",
    )
    val buttonScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.9f
            showBackground -> 1f
            else -> 0.96f
        },
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 340f,
        ),
        label = "${label}_button_scale",
    )
    Box(
        modifier = Modifier
            .scale(buttonScale)
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .playerFrostedSurface(tint = tint)
            .background(tint.copy(alpha = backgroundAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!showBackground) {
                        transientHighlight = true
                    }
                    onClick()
                },
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (label.isBlank()) 0.dp else 10.dp),
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = tint.copy(alpha = 0.92f),
                modifier = Modifier.size(18.dp),
            )
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = tint.copy(alpha = 0.88f),
                )
            }
        }
    }
    LaunchedEffect(transientHighlight) {
        if (transientHighlight) {
            delay(ElovaireMotion.scaleDurationMillis(220L, motionDurationScale))
            transientHighlight = false
        }
    }
}

@Composable
private fun PlaybackProgressBar(
    progress: Float,
    isInteracting: Boolean,
    contentColor: Color,
    onScrubStarted: () -> Unit,
    onScrubFractionChanged: (Float) -> Unit,
    onScrubFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnScrubStarted by rememberUpdatedState(onScrubStarted)
    val latestOnScrubFractionChanged by rememberUpdatedState(onScrubFractionChanged)
    val latestOnScrubFinished by rememberUpdatedState(onScrubFinished)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
        ) {
            val density = LocalDensity.current
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val clampedProgress = progress.coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.CenterStart)
                    .pointerInput(maxWidthPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (maxWidthPx <= 0f) return@awaitEachGesture
                            latestOnScrubStarted()
                            var latestFraction = (down.position.x / maxWidthPx).coerceIn(0f, 1f)
                            latestOnScrubFractionChanged(latestFraction)

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                latestFraction = (change.position.x / maxWidthPx).coerceIn(0f, 1f)
                                latestOnScrubFractionChanged(latestFraction)
                                change.consume()
                            }

                            latestOnScrubFinished(latestFraction)
                        }
                    },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(contentColor.copy(alpha = 0.2f))
                    .align(Alignment.CenterStart),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(contentColor)
                    .align(Alignment.CenterStart),
            )
        }
    }
}

@Composable
private fun VolumeControlBar(
    volume: Float,
    contentColor: Color,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedVolume by animateFloatAsState(
        targetValue = volume.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 360f,
        ),
        label = "player_volume_slider",
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_volume_x),
            contentDescription = "Muted volume",
            tint = contentColor.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp),
        )
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(32.dp),
        ) {
            val density = LocalDensity.current
            val maxWidthPx = with(density) { maxWidth.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.CenterStart)
                    .pointerInput(maxWidthPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (maxWidthPx <= 0f) return@awaitEachGesture
                            var latestFraction = (down.position.x / maxWidthPx).coerceIn(0f, 1f)
                            onVolumeChanged(latestFraction)

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                latestFraction = (change.position.x / maxWidthPx).coerceIn(0f, 1f)
                                onVolumeChanged(latestFraction)
                                change.consume()
                            }
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(contentColor.copy(alpha = 0.2f))
                        .align(Alignment.CenterStart),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedVolume.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(contentColor)
                        .align(Alignment.CenterStart),
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_volume_2),
            contentDescription = "Maximum volume",
            tint = contentColor.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp),
        )
    }
}

private data class FastListScrollbarMetrics(
    val scrollFraction: Float,
    val visibleFraction: Float,
    val totalItems: Int,
    val visibleItemsCount: Int,
)

private data class FastGridScrollbarMetrics(
    val scrollFraction: Float,
    val visibleFraction: Float,
    val totalItems: Int,
    val visibleItemsCount: Int,
    val visibleRows: Int,
    val totalRows: Int,
    val spanCount: Int,
)

@Composable
private fun BoxScope.FastScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    val metrics by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || totalItems <= visibleItems.size) {
                null
            } else {
                val viewportHeightPx =
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
                val averageItemHeightPx = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
                val estimatedContentHeightPx = max(viewportHeightPx, averageItemHeightPx * totalItems)
                val scrollableContentHeightPx = max(estimatedContentHeightPx - viewportHeightPx, 1f)
                val currentScrollPx =
                    (state.firstVisibleItemIndex * averageItemHeightPx + state.firstVisibleItemScrollOffset)
                        .coerceAtLeast(0f)
                FastListScrollbarMetrics(
                    scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f),
                    visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
                    totalItems = totalItems,
                    visibleItemsCount = visibleItems.size,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        visibleFraction = resolvedMetrics.visibleFraction,
        totalItems = resolvedMetrics.totalItems,
        visibleItemsCount = resolvedMetrics.visibleItemsCount,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleIndex = (resolvedMetrics.totalItems - resolvedMetrics.visibleItemsCount).coerceAtLeast(0)
            val targetIndex = (maxFirstVisibleIndex * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleIndex)
            state.requestScrollToItem(targetIndex)
        },
    )
}

@Composable
private fun BoxScope.FastScrollbar(
    state: LazyGridState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    val metrics by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || totalItems <= visibleItems.size) {
                null
            } else {
                val viewportHeightPx =
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
                val averageItemHeightPx = visibleItems.map { it.size.height }.average().toFloat().coerceAtLeast(1f)
                val firstRowOffsetY = visibleItems.firstOrNull()?.offset?.y
                val spanCount = visibleItems
                    .takeWhile { it.offset.y == firstRowOffsetY }
                    .size
                    .coerceAtLeast(1)
                val totalRows = ceil(totalItems.toFloat() / spanCount.toFloat()).toInt().coerceAtLeast(1)
                val visibleRows = ceil(visibleItems.size.toFloat() / spanCount.toFloat()).toInt().coerceAtLeast(1)
                val estimatedContentHeightPx = max(viewportHeightPx, averageItemHeightPx * totalRows)
                val scrollableContentHeightPx = max(estimatedContentHeightPx - viewportHeightPx, 1f)
                val currentScrollPx =
                    ((state.firstVisibleItemIndex / spanCount) * averageItemHeightPx + state.firstVisibleItemScrollOffset)
                        .coerceAtLeast(0f)
                FastGridScrollbarMetrics(
                    scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f),
                    visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
                    totalItems = totalItems,
                    visibleItemsCount = visibleItems.size,
                    visibleRows = visibleRows,
                    totalRows = totalRows,
                    spanCount = spanCount,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        visibleFraction = resolvedMetrics.visibleFraction,
        totalItems = resolvedMetrics.totalItems,
        visibleItemsCount = resolvedMetrics.visibleItemsCount,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleRow = (resolvedMetrics.totalRows - resolvedMetrics.visibleRows).coerceAtLeast(0)
            val targetRow = (maxFirstVisibleRow * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleRow)
            val targetIndex =
                (targetRow * resolvedMetrics.spanCount).coerceIn(0, (resolvedMetrics.totalItems - 1).coerceAtLeast(0))
            state.requestScrollToItem(targetIndex)
        },
    )
}

@Composable
private fun BoxScope.FastScrollbarTrack(
    scrollFraction: Float,
    visibleFraction: Float,
    totalItems: Int,
    visibleItemsCount: Int,
    topInset: Dp,
    bottomInset: Dp,
    onJumpToFraction: suspend (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalItems <= visibleItemsCount) return

    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(scrollFraction.coerceIn(0f, 1f)) }
    var lastRequestedFraction by remember { mutableFloatStateOf(-1f) }
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val trackColor = if (darkTheme) {
        Color.White.copy(alpha = 0.12f)
    } else {
        InkText.copy(alpha = 0.12f)
    }
    val thumbColor = if (darkTheme) {
        Color.White.copy(alpha = 0.78f)
    } else {
        InkText.copy(alpha = 0.72f)
    }
    val animatedScrollFraction by animateFloatAsState(
        targetValue = if (isDragging) dragFraction.coerceIn(0f, 1f) else scrollFraction.coerceIn(0f, 1f),
        animationSpec = if (isDragging) tween(50) else tween(90),
        label = "fast_scrollbar_fraction",
    )
    BoxWithConstraints(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .zIndex(3f)
            .fillMaxHeight()
            .padding(top = topInset, end = 3.dp, bottom = bottomInset)
            .width(28.dp),
    ) {
        val density = LocalDensity.current
        val trackHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val thumbHeightPx = max(with(density) { 40.dp.toPx() }, trackHeightPx * visibleFraction)
        val trackTravelPx = max(trackHeightPx - thumbHeightPx, 1f)
        val thumbOffsetPx = trackTravelPx * animatedScrollFraction
        val fractionForPosition: (Float) -> Float = { y ->
            (y / trackHeightPx).coerceIn(0f, 1f)
        }
        val jumpToFraction: (Float) -> Unit = { fraction ->
            val normalized = fraction.coerceIn(0f, 1f)
            dragFraction = normalized
            if (kotlin.math.abs(normalized - lastRequestedFraction) >= 0.0025f) {
                lastRequestedFraction = normalized
                scope.launch {
                    onJumpToFraction(normalized)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalItems, visibleItemsCount, trackTravelPx, thumbHeightPx) {
                    detectTapGestures { offset ->
                        isDragging = true
                        jumpToFraction(fractionForPosition(offset.y))
                        isDragging = false
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalItems, visibleItemsCount, trackTravelPx, thumbHeightPx) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                jumpToFraction(fractionForPosition(offset.y))
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                jumpToFraction(fractionForPosition(change.position.y))
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                        )
                    },
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(2.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(trackColor),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = with(density) { thumbOffsetPx.toDp() })
                    .width(5.dp)
                    .height(with(density) { thumbHeightPx.toDp() })
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(thumbColor),
            )
        }
    }
}

private fun Modifier.ensureSingleItemRubberBand(state: androidx.compose.foundation.lazy.LazyListState): Modifier = composed {
    val baseModifier = this.kuperRubberBand(
        canScrollBackward = { state.canScrollBackward },
        canScrollForward = { state.canScrollForward },
    )
    if (state.canScrollBackward || state.canScrollForward) return@composed baseModifier
    val fallbackScrollState = rememberScrollableState { 0f }
    baseModifier.scrollable(
        state = fallbackScrollState,
        orientation = Orientation.Vertical,
        overscrollEffect = null,
    )
}

private fun Modifier.ensureSingleItemRubberBand(state: LazyGridState): Modifier = composed {
    val baseModifier = this.kuperRubberBand(
        canScrollBackward = { state.canScrollBackward },
        canScrollForward = { state.canScrollForward },
    )
    if (state.canScrollBackward || state.canScrollForward) return@composed baseModifier
    val fallbackScrollState = rememberScrollableState { 0f }
    baseModifier.scrollable(
        state = fallbackScrollState,
        orientation = Orientation.Vertical,
        overscrollEffect = null,
    )
}

private fun Modifier.kuperRubberBand(
    canScrollBackward: () -> Boolean,
    canScrollForward: () -> Boolean,
): Modifier = composed {
    var translationTarget by remember { mutableFloatStateOf(0f) }
    val translation by animateFloatAsState(
        targetValue = translationTarget,
        animationSpec = ElovaireMotion.overscrollSpringSpec(),
        label = "list_rubber_band_translation",
    )
    val maxTranslationPx = with(LocalDensity.current) { 11.dp.toPx() }
    val connection = remember(maxTranslationPx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val isPullingDown = available.y > 0f && !canScrollBackward()
                val isPullingUp = available.y < 0f && !canScrollForward()
                if (!isPullingDown && !isPullingUp) return Offset.Zero
                translationTarget = (translationTarget + (available.y * 0.032f))
                    .coerceIn(-maxTranslationPx, maxTranslationPx)
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (translationTarget != 0f) {
                    translationTarget = 0f
                }
                return Velocity.Zero
            }
        }
    }
    this
        .graphicsLayer { translationY = translation }
        .nestedScroll(connection)
}

private fun repeatModeLabel(repeatMode: PlaybackRepeatMode): String {
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> "Order"
        PlaybackRepeatMode.One -> "Repeat one"
        PlaybackRepeatMode.All -> "Repeat all"
    }
}

@DrawableRes
private fun repeatModeIconRes(repeatMode: PlaybackRepeatMode): Int {
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> R.drawable.ic_lucide_arrow_line_down
        PlaybackRepeatMode.One -> R.drawable.ic_lucide_repeat_1
        PlaybackRepeatMode.All -> R.drawable.ic_lucide_repeat
    }
}

private fun List<Song>.playbackSourceLabel(fallbackAlbum: String): String {
    val distinctAlbums = asSequence().map { it.album }.filter { it.isNotBlank() }.distinct().toList()
    return when {
        distinctAlbums.size == 1 -> distinctAlbums.first()
        else -> "all songs"
    }.ifBlank { fallbackAlbum }
}

private fun formatCountLabel(
    count: Int,
    singular: String,
): String {
    return if (count == 1) {
        "1 $singular"
    } else {
        "$count ${singular}s"
    }
}

@Composable
private fun EqualizerScreen(
    settings: EqSettings,
    onBack: () -> Unit,
    onBandChanged: (Int, Float) -> Unit,
    onBassChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onSpaciousnessModeChanged: (SpaciousnessMode) -> Unit,
    onApplyPreset: (EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val listState = rememberElovaireLazyListState("changelog_screen")
    val graphScrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = navigationBarInsetDp() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                BoxWithConstraints {
                    val scaledGraphWidth = maxWidth * EQ_GRAPH_MIN_WIDTH_MULTIPLIER
                    val graphContentWidth = if (scaledGraphWidth > EQ_GRAPH_MIN_WIDTH) {
                        scaledGraphWidth
                    } else {
                        EQ_GRAPH_MIN_WIDTH
                    }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(EQ_DB_SCALE_GAP),
                            verticalAlignment = Alignment.Top,
                        ) {
                            EqDbScale(
                                modifier = Modifier
                                    .width(EQ_DB_SCALE_WIDTH)
                                    .height(260.dp),
                            )
                            Column(
                                modifier = Modifier
                                    .horizontalGestureSafe()
                                    .horizontalScroll(graphScrollState),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                EqResponseGraph(
                                    settings = settings,
                                    onBandChanged = onBandChanged,
                                    modifier = Modifier
                                        .width(graphContentWidth)
                                        .height(260.dp),
                                )
                                EqBandFrequencyLabels(
                                    contentWidth = graphContentWidth,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        EqHorizontalScrollbar(
                            scrollState = graphScrollState,
                            contentWidth = graphContentWidth,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EqMiniResponseGraph(
                            settings = settings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        EqPresetMenu(
                            currentSettings = settings,
                            onApplyPreset = onApplyPreset,
                            onReset = onReset,
                        )
                    }
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingsCategoryText(
                            title = "Tone shaping",
                            iconResId = R.drawable.ic_lucide_sliders_horizontal,
                        )
                        EqMacroSliderRow(
                            title = "Bass",
                            value = settings.bass.coerceIn(0f, 1f),
                            valueText = "${(settings.bass.coerceIn(0f, 1f) * 100f).roundToInt()}%",
                            onValueChange = onBassChanged,
                            valueRange = 0f..1f,
                        )
                        EqMacroSliderRow(
                            title = "Treble",
                            value = settings.treble.coerceIn(-1f, 1f),
                            valueText = "${(settings.treble.coerceIn(-1f, 1f) * 100f).roundToInt()}%",
                            onValueChange = onTrebleChanged,
                            valueRange = -1f..1f,
                        )
                    }
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingsCategoryText(
                            title = "Spaciousness",
                            iconResId = R.drawable.ic_lucide_wind,
                        )
                        SpaciousnessModeMenu(
                            currentMode = settings.spaciousnessMode,
                            spaciousnessAmount = settings.spaciousness,
                            onModeSelected = onSpaciousnessModeChanged,
                        )
                        EqMacroSliderRow(
                            title = "Effect strength",
                            value = settings.spaciousness.coerceIn(0f, 1f),
                            valueText = "${(settings.spaciousness.coerceIn(0f, 1f) * 100f).roundToInt()}%",
                            onValueChange = onSpaciousnessChanged,
                            valueRange = 0f..1f,
                        )
                    }
                }
            }
        }
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 16.dp,
            bottomInset = navigationBarInsetDp() + 48.dp,
        )
        PinnedBackTopBar(
            title = "Equalizer",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    textSizePreset: TextSizePreset,
    eqSettings: EqSettings,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onTextSizePresetSelected: (TextSizePreset) -> Unit,
    onBassChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onMonoPlaybackChanged: (Boolean) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onScanLibrary: () -> Unit,
    onCheckForUpdates: () -> Unit,
) {
    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsSectionHeader(
                    title = "Appearance",
                    iconResId = R.drawable.ic_lucide_settings,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SectionTitleRow(
                            title = "Theme",
                            compact = true,
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ThemeModeSegmentedPicker(
                                selectedMode = themeMode,
                                onModeSelected = onThemeModeSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionTitleRow(
                                title = "Text size",
                                compact = true,
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                TextSizeStepper(
                                    selectedPreset = textSizePreset,
                                    onPresetSelected = onTextSizePresetSelected,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = "Sound",
                    iconResId = R.drawable.ic_lucide_volume_2,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(30.dp),
                        ) {
                            DigitalSoundKnob(
                                title = "Bass boost",
                                iconResId = R.drawable.ic_lucide_speaker,
                                value = eqSettings.bass.coerceAtLeast(0f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f),
                                onValueChange = onBassChanged,
                            )
                            DigitalSoundKnob(
                                title = "Spaciousness",
                                iconResId = R.drawable.ic_lucide_wind,
                                value = eqSettings.spaciousness.coerceAtLeast(0f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f),
                                onValueChange = onSpaciousnessChanged,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(onClick = onOpenEqualizer) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_audio_waveform),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                )
                                Text(
                                    text = "Equalizer",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                )
                            }
                        }
                        SettingToggleRow(
                            title = "Enable mono",
                            subtitle = "Switches stereo playback to mono",
                            enabled = eqSettings.monoEnabled,
                            onEnabledChanged = onMonoPlaybackChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = "Other settings",
                    iconResId = R.drawable.ic_lucide_settings,
                )
            }

            item {
                ModuleCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SettingActionRow(
                            title = "Scan library",
                            subtitle = "Refresh indexing in search for new media",
                            actionLabel = "Scan",
                            onAction = onScanLibrary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        SettingActionRow(
                            title = "Check for updates",
                            subtitle = "Check if there's new version available",
                            actionLabel = "Check",
                            onAction = onCheckForUpdates,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                    )
                    Column(
                        modifier = Modifier.padding(top = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("Elovaire", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                                    Color.White.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                },
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = onOpenChangelog,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Changelog",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(180f),
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Designed with passion for music and great design",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = elovaireScaledSp(14f)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = "Settings",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ChangelogScreen(
    releases: List<ChangelogRelease>,
    onBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                bottom = navigationBarInsetDp() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.changelog_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(354.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "What’s new?",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Surface(
                        shape = RoundedCornerShape(ElovaireRadii.pill),
                        color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                            Color.White.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        },
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                    ModuleCard { ChangelogReleaseContent(release = release) }
                }
            }
        }
        PinnedBackTopBar(
            title = "Changelog",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ChangelogBottomSheetOverlay(
    releases: List<ChangelogRelease>,
    onDismiss: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                scaleIn(
                    animationSpec = tween(
                        durationMillis = ElovaireMotion.Spacious,
                        easing = FastOutSlowInEasing,
                    ),
                    initialScale = 0.94f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) +
                slideInVertically(
                    animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Spacious),
                    initialOffsetY = { it / 2 },
                ),
            exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                scaleOut(
                    animationSpec = tween(
                        durationMillis = ElovaireMotion.Quick,
                        easing = FastOutLinearInEasing,
                    ),
                    targetScale = 0.985f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) +
                slideOutVertically(
                    animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                    targetOffsetY = { it / 3 },
                ),
            label = "ChangelogBottomSheetCard",
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                overlayAlpha = 0.6f,
                borderColor = null,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "What’s new?",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ) {
                                Text(
                                    text = BuildConfig.VERSION_NAME,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDismiss,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_x),
                                contentDescription = "Close changelog",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = navigationBarInsetDp() + 18.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(ElovaireRadii.card))
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            LazyColumn(
                                state = listState,
                                overscrollEffect = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .ensureSingleItemRubberBand(listState),
                                contentPadding = PaddingValues(
                                    top = 18.dp,
                                    bottom = 18.dp,
                                ),
                            ) {
                                item {
                                    ChangelogReleaseContent(
                                        release = release,
                                        contentHorizontalPadding = 20.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogReleaseContent(
    release: ChangelogRelease?,
    contentHorizontalPadding: Dp = 20.dp,
) {
    val changes = release?.changes?.filter { it.isNotBlank() }.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = contentHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (changes.isEmpty()) {
            Text(
                text = "No changelog entries yet",
                style = MaterialTheme.typography.bodyLarge,
                color = readableSecondaryTextColor(),
            )
        } else {
            changes.forEachIndexed { index, change ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (index != changes.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        )
                    }
                }
                if (index != changes.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AboutScreen(
    onBack: () -> Unit,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val aboutModel = remember(context) { context.loadAboutScreenModel() }
    val listState = rememberElovaireLazyListState("about_screen")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.topBarToFirstContentGap,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            itemsIndexed(aboutModel.sections, key = { index, section -> "${section.title}_$index" }) { index, section ->
                AboutSectionCard(
                    section = section,
                    renderOnBackground = index == 0,
                )
            }
        }
        PinnedBackTopBar(
            title = "About",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun AboutSectionCard(
    section: AboutSection,
    renderOnBackground: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            section.entries.forEachIndexed { index, entry ->
                AboutEntryBlock(
                    entry = entry,
                    horizontalScrollableLinks = renderOnBackground,
                    useCardAccentButtons = !renderOnBackground,
                    useRoseAccentButtons = renderOnBackground,
                )
                if (index != section.entries.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
    if (renderOnBackground) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
        ) {
            content()
        }
    } else {
        ModuleCard {
            content()
        }
    }
}

@Composable
private fun AboutEntryBlock(
    entry: AboutEntry,
    horizontalScrollableLinks: Boolean = false,
    useCardAccentButtons: Boolean = false,
    useRoseAccentButtons: Boolean = false,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(22f)),
            color = MaterialTheme.colorScheme.onSurface,
        )
        entry.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = secondaryBodyTextStyle(),
                color = readableSecondaryTextColor(),
            )
        }
        if (entry.links.isNotEmpty()) {
            if (horizontalScrollableLinks) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    entry.links.forEach { link ->
                        AboutLinkPill(
                            link = link,
                            useCardAccent = useCardAccentButtons,
                            useRoseAccent = useRoseAccentButtons,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    entry.links.forEach { link ->
                        Box(modifier = Modifier.weight(1f)) {
                            AboutLinkPill(
                                link = link,
                                useCardAccent = useCardAccentButtons,
                                useRoseAccent = useRoseAccentButtons,
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutLinkPill(
    link: AboutLink,
    useCardAccent: Boolean = false,
    useRoseAccent: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor = when {
        useRoseAccent -> RoseAccent.copy(alpha = 0.72f)
        useCardAccent -> AboutCardButtonAccent
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
    }
    val contentColor = if (containerColor.luminance() > 0.42f) InkText else Color.White
    Surface(
        modifier = Modifier,
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = aboutIconForUrl(link.url)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = link.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@DrawableRes
private fun aboutIconForUrl(url: String): Int {
    val normalizedUrl = url.lowercase()
    return when {
        "instagram.com" in normalizedUrl -> R.drawable.ic_about_instagram
        "twitter.com" in normalizedUrl || "x.com" in normalizedUrl -> R.drawable.ic_about_twitter
        "github.com" in normalizedUrl -> R.drawable.ic_about_github
        "ko-fi.com" in normalizedUrl || "kofi.com" in normalizedUrl -> R.drawable.ic_about_coffee
        else -> R.drawable.ic_about_globe
    }
}

@Composable
private fun UpdateAvailableBanner(
    release: AppReleaseInfo,
    uiState: AppUpdateUiState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryTextColor = if (darkTheme) Color.White else InkText
    val secondaryTextColor = if (darkTheme) {
        Color.White.copy(alpha = 0.7f)
    } else {
        InkText.copy(alpha = 0.7f)
    }
    DynamicBackdropSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        overlayAlpha = 0.7f,
        borderColor = blurSurfaceBorderColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Update available",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp,
                        ),
                        color = primaryTextColor,
                    )
                    Text(
                        text = release.versionName,
                        style = MaterialTheme.typography.labelLarge,
                        color = secondaryTextColor,
                    )
                }
            }
            Surface(
                onClick = onUpdate,
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                enabled = !uiState.isInstalling,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = when {
                            uiState.isInstalling -> "Installing"
                            uiState.isDownloading -> {
                                val percent = ((uiState.downloadProgress ?: 0f) * 100f).roundToInt()
                                "Downloading $percent%"
                            }
                            else -> "Download"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_download),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    iconResId: Int,
) {
    Row(
        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun TextSizeStepper(
    selectedPreset: TextSizePreset,
    onPresetSelected: (TextSizePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presets = TextSizePreset.entries
    val currentSelectedPreset by rememberUpdatedState(selectedPreset)
    val currentOnPresetSelected by rememberUpdatedState(onPresetSelected)
    val selectedIndex = presets.indexOf(selectedPreset).coerceAtLeast(0)
    val maxIndex = (presets.size - 1).coerceAtLeast(1)
    val knobSize = 20.dp
    val dotColor = MaterialTheme.colorScheme.onSurface
    val knobColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val lineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    var isDragging by remember { mutableStateOf(false) }
    var dragCenterPx by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .horizontalGestureSafe(),
        ) {
            val density = LocalDensity.current
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val stepFraction = selectedIndex.toFloat() / maxIndex.toFloat()
            val knobSizePx = with(density) { knobSize.toPx() }
            val selectedCenterPx = maxWidthPx * stepFraction
            val stepCenters = remember(maxWidthPx, maxIndex) {
                presets.indices.map { index ->
                    if (maxIndex == 0) {
                        maxWidthPx / 2f
                    } else {
                        maxWidthPx * (index.toFloat() / maxIndex.toFloat())
                    }
                }
            }
            LaunchedEffect(selectedCenterPx, maxWidthPx) {
                if (!isDragging) {
                    dragCenterPx = selectedCenterPx
                }
            }
            val knobOffset by animateDpAsState(
                targetValue = with(density) {
                    ((if (isDragging) dragCenterPx else selectedCenterPx) - (knobSizePx / 2f)).toDp()
                },
                animationSpec = if (isDragging) {
                    tween(durationMillis = 60)
                } else {
                    spring(
                        dampingRatio = 0.82f,
                        stiffness = 480f,
                    )
                },
                label = "text_size_knob_offset",
            )
            val updateFromPosition: (Float) -> Unit = { xPosition ->
                val clampedX = xPosition.coerceIn(0f, maxWidthPx)
                dragCenterPx = clampedX
                val targetIndex = stepCenters
                    .withIndex()
                    .minByOrNull { (_, center) -> kotlin.math.abs(center - clampedX) }
                    ?.index
                    ?: presets.indexOf(currentSelectedPreset).coerceAtLeast(0)
                val preset = presets[targetIndex]
                if (preset != currentSelectedPreset) {
                    currentOnPresetSelected(preset)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(maxWidthPx) {
                        detectTapGestures { offset ->
                            if (maxWidthPx > 0f) {
                                updateFromPosition(offset.x)
                            }
                        }
                    }
                    .pointerInput(maxWidthPx, presets.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                if (maxWidthPx > 0f) {
                                    updateFromPosition(offset.x)
                                }
                            },
                            onHorizontalDrag = { change, _ ->
                                if (maxWidthPx > 0f) {
                                    change.consume()
                                    updateFromPosition(change.position.x)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(2.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(lineColor),
                )

                Canvas(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val selectedDotRadius = 3.5.dp.toPx()
                    val defaultDotRadius = 2.5.dp.toPx()
                    val centerY = size.height / 2f
                    presets.forEachIndexed { index, _ ->
                        val fraction = if (maxIndex == 0) 0f else index.toFloat() / maxIndex.toFloat()
                        drawCircle(
                            color = dotColor,
                            radius = if (index == selectedIndex) selectedDotRadius else defaultDotRadius,
                            center = Offset(size.width * fraction, centerY),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = knobOffset.roundToPx(), y = 0) }
                        .size(knobSize)
                        .clip(CircleShape)
                        .background(knobColor)
                        .align(Alignment.CenterStart),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_case_sensitive),
                contentDescription = "Smaller text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = selectedPreset.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_a_large_small),
                contentDescription = "Larger text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        MonoPlaybackToggle(
            checked = enabled,
            onCheckedChange = onEnabledChanged,
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Surface(
            onClick = onAction,
            shape = RoundedCornerShape(ElovaireRadii.pill),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                text = actionLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun MonoPlaybackToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val knobColor = if (checked) {
        Color.White
    } else if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            ToggleEnabledGreen
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.16f else 0.2f)
        },
        animationSpec = tween(160),
        label = "mono_toggle_track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "mono_toggle_thumb_offset",
    )
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(percent = 50),
        color = trackColor,
        contentColor = knobColor,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset, y = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(knobColor),
            )
        }
    }
}

@Composable
private fun ThemeModeSegmentedPicker(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)
    BoxWithConstraints(
        modifier = modifier
            .height(46.dp)
            .horizontalGestureSafe()
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                },
            )
            .padding(5.dp),
    ) {
        val selectedIndex = options.indexOf(selectedMode).coerceAtLeast(0)
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 420f,
            ),
            label = "theme_picker_offset",
        )
        val indicatorColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(indicatorColor),
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { option ->
                val selected = option == selectedMode
                val iconResId = when (option) {
                    ThemeMode.Light -> R.drawable.ic_lucide_sun
                    ThemeMode.Dark -> R.drawable.ic_lucide_moon
                    ThemeMode.System -> R.drawable.ic_lucide_settings_2
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onModeSelected(option) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalSoundKnob(
    title: String,
    iconResId: Int,
    value: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    var dragValue by remember(value) { mutableFloatStateOf(value.coerceIn(0f, 1f)) }
    LaunchedEffect(value) {
        dragValue = value.coerceIn(0f, 1f)
    }
    val animatedValue by animateFloatAsState(
        targetValue = dragValue,
        animationSpec = tween(ElovaireMotion.Standard),
        label = "${title}_sound_knob",
    )
    val glowColor = Color(0xFF61F6A2)
    val inactiveDot = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f)
    val trackColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.3f)
    }
    val activeArcColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val tickColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(134.dp)
                .horizontalGestureSafe()
                .pointerInput(title) {
                    detectTapGestures { offset ->
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        val horizontalInsetPx = widthPx * 0.035f
                        val activeWidthPx = (widthPx - (horizontalInsetPx * 2f)).coerceAtLeast(1f)
                        dragValue = ((offset.x - horizontalInsetPx) / activeWidthPx).coerceIn(0f, 1f)
                        onValueChange(dragValue)
                    }
                }
                .pointerInput(title) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val widthPx = size.width.toFloat().coerceAtLeast(1f)
                            val horizontalInsetPx = widthPx * 0.035f
                            val activeWidthPx = (widthPx - (horizontalInsetPx * 2f)).coerceAtLeast(1f)
                            dragValue = ((offset.x - horizontalInsetPx) / activeWidthPx).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val widthPx = size.width.toFloat().coerceAtLeast(1f)
                            dragValue = (dragValue + ((dragAmount / widthPx) * 0.99f)).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        },
                    )
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                val strokeWidth = 5.5.dp.toPx()
                val horizontalInset = 8.dp.toPx()
                val topInset = 12.dp.toPx()
                val radius = min(
                    ((size.width - (horizontalInset * 2f)) / 2f).coerceAtLeast(1f),
                    ((size.height - topInset - 8.dp.toPx()) * 0.54f).coerceAtLeast(1f),
                )
                val center = Offset(size.width / 2f, topInset + radius)
                val startAngle = 180f
                val sweepAngle = 180f
                val activeSweep = sweepAngle * animatedValue
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                if (activeSweep > 0f) {
                    drawArc(
                        color = activeArcColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                val tickOuterRadius = (radius - 8.dp.toPx()).coerceAtLeast(1f)
                val tickInnerRadius = (tickOuterRadius - 12.dp.toPx()).coerceAtLeast(1f)
                val tickCount = 30
                repeat(tickCount) { tickIndex ->
                    val fraction = tickIndex / (tickCount - 1).toFloat()
                    val angleDegrees = 180f + (180f * fraction)
                    val angleRadians = Math.toRadians(angleDegrees.toDouble())
                    val start = Offset(
                        x = center.x + (cos(angleRadians) * tickInnerRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickInnerRadius).toFloat(),
                    )
                    val end = Offset(
                        x = center.x + (cos(angleRadians) * tickOuterRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickOuterRadius).toFloat(),
                    )
                    drawLine(
                        color = tickColor,
                        start = start,
                        end = end,
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Square,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(top = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${(animatedValue * 100f).roundToInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (animatedValue > 0f) glowColor else inactiveDot),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            )
        }
    }
}

@Composable
private fun DetailScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(
            iconResId = R.drawable.ic_lucide_chevron_left,
            contentDescription = "Back",
            showBackground = false,
            onClick = onBack,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                )
            }
        }
    }
}

private fun circularKnobValueForOffset(
    offset: Offset,
    size: Size,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
): Float {
    if (size.width <= 0f || size.height <= 0f) return 0f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val angle = Math.toDegrees(
        atan2(
            (offset.y - centerY).toDouble(),
            (offset.x - centerX).toDouble(),
        ),
    ).toFloat().let { if (it < 0f) it + 360f else it }
    val relative = ((angle - startAngleDegrees) % 360f + 360f) % 360f
    return when {
        relative <= sweepAngleDegrees -> (relative / sweepAngleDegrees).coerceIn(0f, 1f)
        relative < (sweepAngleDegrees + ((360f - sweepAngleDegrees) / 2f)) -> 1f
        else -> 0f
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val clampedValue = value.coerceIn(-1f, 1f)
    val accent = if (clampedValue >= 0f) Color(0xFF7D8BFF) else Color(0xFFFF6F61)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(190.dp)
                .clip(RoundedCornerShape(ElovaireRadii.module))
                .background(readableCardSurfaceColor())
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            onValueChange((clampedValue - (dragAmount / 180f)).coerceIn(-1f, 1f))
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = 1f - (offset.y / size.height.toFloat())
                        onValueChange(((fraction * 2f) - 1f).coerceIn(-1f, 1f))
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp),
            ) {
                val trackWidth = 6.dp.toPx()
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val knobRadius = 8.dp.toPx()
                val travel = (size.height / 2f) - knobRadius - 8.dp.toPx()
                val knobY = centerY - (travel * clampedValue)

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    topLeft = Offset(centerX - (trackWidth / 2f), 8.dp.toPx()),
                    size = Size(trackWidth, size.height - 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth, trackWidth),
                )

                val fillTop = minOf(centerY, knobY)
                val fillBottom = maxOf(centerY, knobY)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.94f),
                            accent.copy(alpha = 0.55f),
                        ),
                    ),
                    topLeft = Offset(centerX - (trackWidth / 2f), fillTop),
                    size = Size(trackWidth, (fillBottom - fillTop).coerceAtLeast(trackWidth)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth, trackWidth),
                )

                drawCircle(
                    color = accent.copy(alpha = 0.28f),
                    radius = knobRadius * 1.75f,
                    center = Offset(centerX, knobY),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.96f),
                    radius = knobRadius,
                    center = Offset(centerX, knobY),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = readableSecondaryTextColor(),
        )
    }
}

@Composable
private fun EqMacroSliderRow(
    title: String,
    value: Float,
    valueText: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            )
        }
        ThinContinuousSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

private data class EqPresetDefinition(
    val name: String,
    val settings: EqSettings,
)

@Composable
private fun EqPresetMenu(
    currentSettings: EqSettings,
    onApplyPreset: (EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val presets = remember {
        listOf(
            eqPreset("Electronic", 0.40f, 0.58f, 0.42f, 0.26f, 0.10f, 0.08f, 0.16f, 0.24f),
            eqPreset("Jazz", 0.18f, 0.26f, 0.14f, -0.08f, 0.10f, 0.22f, 0.18f, 0.12f),
            eqPreset("Classical", 0.12f, 0.16f, 0.08f, -0.04f, 0.06f, 0.18f, 0.24f, 0.18f),
            eqPreset("Acoustic", 0.10f, 0.14f, 0.06f, -0.12f, 0.14f, 0.20f, 0.18f, 0.10f),
            eqPreset("Pop", 0.24f, 0.32f, 0.18f, -0.08f, 0.10f, 0.16f, 0.20f, 0.14f),
            eqPreset("Rock", 0.28f, 0.22f, 0.10f, -0.12f, 0.08f, 0.18f, 0.28f, 0.22f),
            eqPreset("Metal", 0.22f, 0.18f, 0.08f, -0.14f, 0.12f, 0.24f, 0.30f, 0.26f),
            eqPreset("Vocal", -0.08f, -0.12f, -0.04f, -0.10f, 0.20f, 0.28f, 0.16f, 0.06f),
            eqPreset("R&B", 0.30f, 0.34f, 0.16f, -0.10f, 0.12f, 0.16f, 0.12f, 0.08f),
            eqPreset("Hip-Hop", 0.42f, 0.46f, 0.22f, -0.12f, 0.10f, 0.12f, 0.08f, 0.04f),
        )
    }
    val horizontalScrollState = rememberScrollState()
    val activePresetName = remember(currentSettings, presets) {
        val currentBands = currentSettings.normalizedBandValues()
        presets.firstOrNull { preset -> preset.settings.normalizedBandValues() == currentBands }?.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGestureSafe()
            .horizontalScroll(horizontalScrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EqPresetPill(
            label = "Reset",
            selected = activePresetName == null && currentSettings != EqSettings(),
            emphasized = true,
            onClick = onReset,
        )
        presets.forEach { preset ->
            EqPresetPill(
                label = preset.name,
                selected = preset.name == activePresetName,
                onClick = {
                    onApplyPreset(
                        currentSettings.copy(
                            bands = preset.settings.bands,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun SpaciousnessModeMenu(
    currentMode: SpaciousnessMode,
    spaciousnessAmount: Float,
    onModeSelected: (SpaciousnessMode) -> Unit,
) {
    val modes = remember {
        listOf(
            SpaciousnessMode.StereoWidth,
            SpaciousnessMode.CrossfeedDepth,
            SpaciousnessMode.EarlyReflectionRoom,
            SpaciousnessMode.Philharmony,
            SpaciousnessMode.HaasSpace,
            SpaciousnessMode.HarmonicAir,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGestureSafe()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { mode ->
            EqPresetPill(
                label = mode.displayLabel(),
                selected = spaciousnessAmount > 0.001f && mode == currentMode,
                onClick = {
                    onModeSelected(
                        if (spaciousnessAmount > 0.001f && mode == currentMode) {
                            SpaciousnessMode.Off
                        } else {
                            mode
                        },
                    )
                },
            )
        }
    }
}

private fun SpaciousnessMode.displayLabel(): String {
    return when (this) {
        SpaciousnessMode.Off -> "Off"
        SpaciousnessMode.StereoWidth -> "Stereo Width"
        SpaciousnessMode.CrossfeedDepth -> "Crossfeed"
        SpaciousnessMode.EarlyReflectionRoom -> "Room"
        SpaciousnessMode.Philharmony -> "Philharmony"
        SpaciousnessMode.HaasSpace -> "Haas Space"
        SpaciousnessMode.HarmonicAir -> "Harmonic Air"
    }
}

@Composable
private fun EqPresetPill(
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.7f else 0.48f,
        )
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
    }
}

@Composable
private fun EqResponseGraph(
    settings: EqSettings,
    onBandChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val graphPointCount = EqualizerDspModel.BAND_COUNT
    val animatedBandValues = List(graphPointCount) { index ->
        val target = settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "eq_band_$index",
        )
        animated
    }
    val bandValues = remember(animatedBandValues) {
        normalizeEqBandValues(animatedBandValues, graphPointCount)
    }
    val bandFractions = remember { eqBandFractions() }
    val accentColor = Color(0xFF39E38E)
    val guideColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .pointerInput(bandFractions) {
                detectTapGestures { offset ->
                    if (size.width == 0 || size.height == 0) return@detectTapGestures
                    val bandIndex = nearestEqBandIndex(
                        fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                        bandFractions = bandFractions,
                    )
                    val verticalFraction = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                    onBandChanged(
                        bandIndex,
                        EqualizerDspModel.graphFractionToNormalized(verticalFraction, eqGraphConfig),
                    )
                }
            }
            .pointerInput(bandFractions) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        val bandIndex = nearestEqBandIndex(
                            fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                            bandFractions = bandFractions,
                        )
                        val normalized = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onBandChanged(bandIndex, ((normalized * 2f) - 1f).coerceIn(-1f, 1f))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        val index = nearestEqBandIndex(
                            fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f),
                            bandFractions = bandFractions,
                        )
                        val verticalFraction = (1f - (change.position.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onBandChanged(
                            index,
                            EqualizerDspModel.graphFractionToNormalized(verticalFraction, eqGraphConfig),
                        )
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val topPadding = size.height * 0.08f
            val bottomPadding = size.height * 0.12f
            val graphHeight = size.height - topPadding - bottomPadding
            val zeroDbFraction = ((0f - eqGraphConfig.minBandGainDb) / (eqGraphConfig.maxBandGainDb - eqGraphConfig.minBandGainDb))
                .coerceIn(0f, 1f)
            val midY = topPadding + (graphHeight * (1f - zeroDbFraction))

            eqDbLevels().forEach { levelDb ->
                val y = topPadding + (graphHeight * (1f - eqLevelFraction(levelDb, eqGraphConfig)))
                drawLine(
                    color = guideColor.copy(alpha = if (levelDb == 0f) 0.12f else 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            bandValues.forEachIndexed { index, band ->
                val x = size.width * bandFractions.getOrElse(index) { 0f }
                val y = topPadding + (graphHeight * (1f - EqualizerDspModel.bandGraphFraction(band, eqGraphConfig)))
                val trackWidth = 9.dp.toPx()
                val activeWidth = 6.dp.toPx()
                val thumbWidth = 14.dp.toPx()
                val thumbHeight = 30.dp.toPx()
                val activeTop = min(y, midY)
                val activeHeight = max(2.dp.toPx(), kotlin.math.abs(y - midY))
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.08f),
                    topLeft = Offset(x - trackWidth * 1.45f, topPadding - 8.dp.toPx()),
                    size = Size(trackWidth * 2.9f, graphHeight + 16.dp.toPx()),
                    cornerRadius = CornerRadius(trackWidth * 2.9f, trackWidth * 2.9f),
                )
                drawRoundRect(
                    color = guideColor.copy(alpha = 0.05f),
                    topLeft = Offset(x - trackWidth / 2f, topPadding),
                    size = Size(trackWidth, graphHeight),
                    cornerRadius = CornerRadius(trackWidth, trackWidth),
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.18f),
                    start = Offset(x, midY),
                    end = Offset(x, y),
                    strokeWidth = activeWidth * 2.6f,
                    cap = StrokeCap.Round,
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(x - activeWidth / 2f, activeTop),
                    size = Size(activeWidth, activeHeight),
                    cornerRadius = CornerRadius(activeWidth, activeWidth),
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.16f),
                    topLeft = Offset(x - thumbWidth * 0.8f, y - thumbHeight / 2f),
                    size = Size(thumbWidth * 1.6f, thumbHeight),
                    cornerRadius = CornerRadius(thumbWidth, thumbWidth),
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(x - thumbWidth / 2f, y - thumbHeight / 2f),
                    size = Size(thumbWidth, thumbHeight),
                    cornerRadius = CornerRadius(thumbWidth, thumbWidth),
                )
            }
        }
    }
}

@Composable
private fun EqMiniResponseGraph(
    settings: EqSettings,
    modifier: Modifier = Modifier,
) {
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val graphPointCount = EqualizerDspModel.BAND_COUNT
    val animatedBandValues = List(graphPointCount) { index ->
        val target = settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            label = "eq_mini_band_$index",
        )
        animated
    }
    val bandValues = remember(animatedBandValues) {
        normalizeEqBandValues(animatedBandValues, graphPointCount)
    }
    val bandFractions = remember { eqBandFractions() }
    val accentColor = Color(0xFF39E38E)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val horizontalPadding = 14.dp.toPx()
            val topPadding = size.height * 0.18f
            val bottomPadding = size.height * 0.2f
            val graphHeight = size.height - topPadding - bottomPadding
            val graphWidth = size.width - horizontalPadding * 2f
            val zeroDbFraction = ((0f - eqGraphConfig.minBandGainDb) / (eqGraphConfig.maxBandGainDb - eqGraphConfig.minBandGainDb))
                .coerceIn(0f, 1f)
            val midY = topPadding + (graphHeight * (1f - zeroDbFraction))
            val points = bandValues.mapIndexed { index, band ->
                val x = horizontalPadding + graphWidth * bandFractions.getOrElse(index) { 0f }
                val y = topPadding + (graphHeight * (1f - EqualizerDspModel.bandGraphFraction(band, eqGraphConfig)))
                Offset(x, y)
            }
            if (points.isEmpty()) return@Canvas
            val strokePath = smoothPathFromPoints(points)
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, midY)
                lineTo(points.first().x, midY)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            drawPath(
                path = strokePath,
                color = accentColor.copy(alpha = 0.2f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                path = strokePath,
                color = accentColor,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun EqDbScale(
    modifier: Modifier = Modifier,
) {
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val markerColor = readableSecondaryTextColor().copy(alpha = 0.78f)
    val levels = remember { eqDbLevels() }
    BoxWithConstraints(modifier = modifier) {
        val topPadding = maxHeight * 0.08f
        val bottomPadding = maxHeight * 0.12f
        val graphHeight = maxHeight - topPadding - bottomPadding
        levels.forEach { levelDb ->
            val positionY = topPadding + (graphHeight * (1f - eqLevelFraction(levelDb, eqGraphConfig)))
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = positionY - 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatEqDbLabel(levelDb),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = elovaireScaledSp(9f)),
                    color = markerColor,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { markerIndex ->
                        Box(
                            modifier = Modifier
                                .width((5 - markerIndex).dp)
                                .height(1.5.dp)
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .background(markerColor.copy(alpha = 0.65f - (markerIndex * 0.14f))),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EqBandFrequencyLabels(
    contentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val labels = remember {
        EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ.map(::formatEqFrequencyLabel)
    }
    Row(
        modifier = modifier.width(contentWidth),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = elovaireScaledSp(9.2f)),
                    color = readableSecondaryTextColor().copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EqHorizontalScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    contentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val contentWidthPx = with(density) { contentWidth.toPx() }.coerceAtLeast(viewportWidthPx)
        val maxScrollPx = scrollState.maxValue.toFloat().coerceAtLeast(0f)
        val viewportFraction = (viewportWidthPx / contentWidthPx).coerceIn(0.08f, 1f)
        val thumbWidthPx = (viewportWidthPx * viewportFraction).coerceAtLeast(with(density) { 46.dp.toPx() })
        val thumbTravelPx = (viewportWidthPx - thumbWidthPx).coerceAtLeast(0f)
        val thumbOffsetFraction = if (maxScrollPx <= 0f) 0f else (scrollState.value / maxScrollPx).coerceIn(0f, 1f)
        val thumbOffsetPx = thumbTravelPx * thumbOffsetFraction
        val trackColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            InkText.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.14f)
        }
        val thumbColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            InkText.copy(alpha = 0.58f)
        } else {
            Color.White.copy(alpha = 0.62f)
        }
        val updateScrollFromX: (Float) -> Unit = { xPosition ->
            if (maxScrollPx <= 0f || viewportWidthPx <= 0f) {
                Unit
            } else {
                val fraction = ((xPosition - (thumbWidthPx / 2f)) / thumbTravelPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val targetScroll = (maxScrollPx * fraction).roundToInt()
                scope.launch {
                    scrollState.scrollTo(targetScroll)
                }
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(viewportWidthPx, maxScrollPx) {
                    detectTapGestures { offset ->
                        updateScrollFromX(offset.x)
                    }
                }
                .pointerInput(viewportWidthPx, maxScrollPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateScrollFromX(offset.x) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateScrollFromX(change.position.x)
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(trackColor),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(x = thumbOffsetPx.roundToInt(), y = 0) }
                    .width(with(density) { thumbWidthPx.toDp() })
                    .height(4.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(thumbColor),
            )
        }
    }
}

@Composable
private fun SettingsCategoryText(
    title: String,
    @DrawableRes iconResId: Int? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResId != null) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = readableMutedIconColor(),
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun ThinContinuousSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    lineThickness: Dp = 2.dp,
    knobSize: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val fraction = ((coercedValue - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    val knobColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val inactiveLineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val activeWidth by animateDpAsState(
            targetValue = maxWidth * fraction,
            animationSpec = tween(durationMillis = 70),
            label = "eq_macro_slider_fill",
        )
        val knobOffset by animateDpAsState(
            targetValue = with(density) { ((maxWidthPx * fraction) - (knobSizePx / 2f)).toDp() },
            animationSpec = tween(durationMillis = 70),
            label = "eq_macro_slider_knob",
        )

        val updateFromX: (Float) -> Unit = { xPosition ->
            if (maxWidthPx > 0f) {
                val normalized = (xPosition / maxWidthPx).coerceIn(0f, 1f)
                val rangedValue = valueRange.start + ((valueRange.endInclusive - valueRange.start) * normalized)
                currentOnValueChange(rangedValue.coerceIn(valueRange.start, valueRange.endInclusive))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxWidthPx, valueRange.start, valueRange.endInclusive) {
                    detectTapGestures { offset ->
                        updateFromX(offset.x)
                    }
                }
                .pointerInput(maxWidthPx, valueRange.start, valueRange.endInclusive) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateFromX(offset.x) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateFromX(change.position.x)
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .height(lineThickness)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(inactiveLineColor),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(activeWidth)
                    .height(lineThickness)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(knobColor),
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = knobOffset.roundToPx(), y = 0) }
                    .size(knobSize)
                    .clip(CircleShape)
                    .background(knobColor)
                    .align(Alignment.CenterStart),
            )
        }
    }
}

private fun eqBandFractions(): List<Float> {
    val count = EqualizerDspModel.BAND_COUNT
    if (count <= 1) return emptyList()
    return List(count) { index -> index.toFloat() / (count - 1).toFloat() }
}

private fun eqDbLevels(): List<Float> = listOf(8f, 4f, 0f, -5f, -10f)

private fun eqLevelFraction(
    levelDb: Float,
    config: EqualizerDspConfig = EqualizerDspConfig(),
): Float {
    return ((levelDb - config.minBandGainDb) / (config.maxBandGainDb - config.minBandGainDb))
        .coerceIn(0f, 1f)
}

private fun formatEqDbLabel(levelDb: Float): String {
    return when {
        levelDb > 0f -> "+${levelDb.roundToInt()}"
        levelDb < 0f -> levelDb.roundToInt().toString()
        else -> "0"
    }
}

private fun nearestEqBandIndex(
    fraction: Float,
    bandFractions: List<Float>,
): Int {
    return bandFractions
        .withIndex()
        .minByOrNull { (_, value) -> kotlin.math.abs(value - fraction) }
        ?.index
        ?: 0
}

private fun formatEqFrequencyLabel(frequencyHz: Float): String {
    return when {
        frequencyHz >= 1_000f -> {
            val kilo = frequencyHz / 1_000f
            formatEqKiloLabel(kilo)
        }
        frequencyHz % 1f == 0f -> frequencyHz.roundToInt().toString()
        else -> frequencyHz.toString()
    }
}

private fun formatEqKiloLabel(kiloValue: Float): String {
    val formatted = when {
        kiloValue >= 10f || kiloValue % 1f == 0f -> kiloValue.roundToInt().toString()
        (kiloValue * 10f) % 1f == 0f -> String.format(java.util.Locale.ROOT, "%.1f", kiloValue)
        else -> String.format(java.util.Locale.ROOT, "%.2f", kiloValue)
    }.trimEnd('0').trimEnd('.')
    return "${formatted}k"
}

private fun normalizeEqBandValues(
    values: List<Float>,
    targetCount: Int,
): List<Float> {
    if (values.isEmpty()) return List(targetCount) { 0f }
    return List(targetCount) { index ->
        values.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
    }
}

private fun eqPreset(
    name: String,
    bass: Float,
    subBass: Float,
    lowBass: Float,
    lowMid: Float,
    presence: Float,
    upperMid: Float,
    brilliance: Float,
    air: Float,
): EqPresetDefinition {
    val bandShape = List(EqualizerDspModel.BAND_COUNT) { index ->
        when (EqualizerDspModel.bandDefinition(index).frequencyHz) {
            in 0f..30f -> bass
            in 30.0001f..60f -> subBass
            in 60.0001f..350f -> lowBass
            in 350.0001f..750f -> lowMid
            in 750.0001f..1_500f -> presence
            in 1_500.0001f..3_000f -> upperMid
            in 3_000.0001f..8_000f -> brilliance
            else -> air
        }.coerceIn(-1f, 1f)
    }
    val settings = EqSettings(
        bands = bandShape,
        bass = 0f,
        treble = 0f,
        spaciousness = 0f,
        spaciousnessMode = SpaciousnessMode.Off,
    ).normalizedEqSettings()
    return EqPresetDefinition(name = name, settings = settings)
}

private fun EqSettings.normalizedBandValues(): List<Float> {
    return List(EqualizerDspModel.BAND_COUNT) { index ->
        bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
    }
}

private fun EqSettings.normalizedEqSettings(): EqSettings {
    return copy(
        bands = normalizedBandValues(),
        bass = bass.coerceIn(-1f, 1f),
        treble = treble.coerceIn(-1f, 1f),
        spaciousness = spaciousness.coerceIn(0f, 1f),
    )
}

private fun smoothPathFromPoints(points: List<Offset>): androidx.compose.ui.graphics.Path {
    return androidx.compose.ui.graphics.Path().apply {
        if (points.isEmpty()) return@apply
        moveTo(points.first().x, points.first().y)
        if (points.size == 1) return@apply
        for (index in 1 until points.size) {
            val previous = points[index - 1]
            val current = points[index]
            val midPoint = Offset(
                x = (previous.x + current.x) / 2f,
                y = (previous.y + current.y) / 2f,
            )
            quadraticTo(previous.x, previous.y, midPoint.x, midPoint.y)
        }
        val last = points.last()
        lineTo(last.x, last.y)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.12f else 0.06f,
            )
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        modifier = Modifier
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ControlButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        contentColor = if (emphasized) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (emphasized) 22.dp else 0.dp,
        modifier = Modifier.size(if (emphasized) 88.dp else 64.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
            )
        }
    }
}

private fun recentlyAddedAlbumsFor(
    libraryState: LibraryUiState,
): List<Album> {
    return libraryState.albums
        .sortedByDescending { album ->
            album.songs.maxOfOrNull(Song::dateAddedSeconds) ?: 0L
        }
        .take(4)
}

private fun recentAlbumsFor(
    libraryState: LibraryUiState,
    playbackState: PlaybackUiState,
): List<Album> {
    val albumsById = libraryState.albums.associateBy { it.id }
    val played = playbackState.recentAlbumIds.mapNotNull(albumsById::get)
    return played.take(6)
}

private fun favoriteAlbumsFor(
    libraryState: LibraryUiState,
    songPlayCounts: Map<Long, Int>,
    recentAlbums: List<Album>,
    recentlyAddedAlbums: List<Album>,
): List<Album> {
    val rankedByFrequency = libraryState.albums
        .mapNotNull { album ->
            val playCount = album.songs.sumOf { songPlayCounts[it.id] ?: 0 }
            if (playCount > 0) album to playCount else null
        }
        .sortedWith(
            compareByDescending<Pair<Album, Int>> { it.second }
                .thenBy { it.first.artist.lowercase() }
                .thenBy { it.first.title.lowercase() },
        )
        .map { it.first }

    return buildList {
        (rankedByFrequency + recentAlbums + recentlyAddedAlbums).forEach { album ->
            if (none { it.id == album.id }) add(album)
            if (size == 6) return@buildList
        }
    }
}

private fun lyricsSeekPositionMs(
    lines: List<LyricsLine>,
    index: Int,
    isSynced: Boolean,
): Long? {
    if (lines.isEmpty() || index !in lines.indices) return null

    if (isSynced) {
        return lines[index].startTimeMs
            ?.coerceAtLeast(0L)
    }

    return null
}

private suspend fun LazyListState.animateLyricJumpToItem(
    index: Int,
    scrollOffset: Int = 0,
) {
    val distance = kotlin.math.abs(firstVisibleItemIndex - index)
    if (distance > 6) {
        val landingIndex = if (index > firstVisibleItemIndex) {
            (index - 2).coerceAtLeast(0)
        } else {
            (index + 2).coerceAtMost(layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
        scrollToItem(landingIndex, scrollOffset)
    }
    animateScrollToItem(index = index, scrollOffset = scrollOffset)
}

private fun fractionToDurationPosition(
    fraction: Float,
    durationMs: Long,
): Long {
    if (durationMs <= 0L) return 0L
    return (durationMs * fraction.coerceIn(0f, 1f)).roundToInt().toLong().coerceIn(0L, durationMs)
}

private fun suggestedAlbumsFor(
    libraryState: LibraryUiState,
    albumPlayCounts: Map<Long, Int>,
    recentAlbumIds: List<Long>,
): List<Album> {
    val recentAlbumIdSet = recentAlbumIds.toSet()
    val rarePlayedAlbums = libraryState.albums
        .mapNotNull { album ->
            val playCount = albumPlayCounts[album.id] ?: 0
            if (playCount > 0) album to playCount else null
        }
        .sortedWith(
            compareBy<Pair<Album, Int>> { it.second }
                .thenBy { album -> if (album.first.id in recentAlbumIdSet) 1 else 0 }
                .thenBy { it.first.artist.lowercase() }
                .thenBy { it.first.title.lowercase() },
        )
        .map { it.first }

    val neverPlayedAlbums = libraryState.albums
        .filter { (albumPlayCounts[it.id] ?: 0) == 0 }
        .sortedWith(
            compareBy<Album> { if (it.id in recentAlbumIdSet) 1 else 0 }
                .thenBy { it.artist.lowercase() }
                .thenBy { it.title.lowercase() },
        )

    return buildList {
        (rarePlayedAlbums + neverPlayedAlbums).forEach { album ->
            if (none { it.id == album.id }) add(album)
            if (size == 6) return@buildList
        }
    }
}

private fun topBarTitle(route: String?): String {
    return when (route) {
        ALBUMS_ROUTE -> "Library"
        PLAYLISTS_ROUTE -> "Playlists"
        SEARCH_ROUTE -> "Search"
        else -> "Welcome"
    }
}

private fun detailFallbackTitle(route: String?): String {
    return when (route) {
        HOME_ROUTE -> "Home"
        SEARCH_ROUTE -> "Search"
        PLAYLISTS_ROUTE, "$PLAYLIST_ROUTE/{playlistId}" -> "Playlists"
        ALBUMS_ROUTE, "$LIBRARY_COLLECTION_ROUTE/{kind}", "$GENRE_ROUTE/{genre}" -> "Library"
        else -> "Library"
    }
}

private fun String?.normalizedNavigationRoute(): String? {
    return when {
        this == null -> null
        startsWith("$ALBUM_ROUTE/") -> "$ALBUM_ROUTE/{albumId}"
        startsWith("$PLAYLIST_ROUTE/") -> "$PLAYLIST_ROUTE/{playlistId}"
        startsWith("$GENRE_ROUTE/") -> "$GENRE_ROUTE/{genre}"
        startsWith("$ARTIST_ROUTE/") -> "$ARTIST_ROUTE/{artistName}"
        startsWith("$LIBRARY_COLLECTION_ROUTE/") -> "$LIBRARY_COLLECTION_ROUTE/{kind}"
        else -> this
    }
}

private fun String?.isExpandFromTileRoute(): Boolean {
    return this.normalizedNavigationRoute() == "$ALBUM_ROUTE/{albumId}" ||
        this.normalizedNavigationRoute() == "$PLAYLIST_ROUTE/{playlistId}"
}

private fun ExpandOrigin.toTransformOrigin(): TransformOrigin {
    return TransformOrigin(
        pivotFractionX = xFraction.coerceIn(0f, 1f),
        pivotFractionY = yFraction.coerceIn(0f, 1f),
    )
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + ((stop - start) * fraction.coerceIn(0f, 1f))
}

private fun lerpRect(
    start: androidx.compose.ui.geometry.Rect,
    stop: androidx.compose.ui.geometry.Rect,
    fraction: Float,
): androidx.compose.ui.geometry.Rect {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return androidx.compose.ui.geometry.Rect(
        left = lerpFloat(start.left, stop.left, clampedFraction),
        top = lerpFloat(start.top, stop.top, clampedFraction),
        right = lerpFloat(start.right, stop.right, clampedFraction),
        bottom = lerpFloat(start.bottom, stop.bottom, clampedFraction),
    )
}

private fun androidx.compose.ui.geometry.Rect.coerceWithin(
    bounds: androidx.compose.ui.geometry.Rect,
): androidx.compose.ui.geometry.Rect {
    val width = width.coerceAtLeast(1f).coerceAtMost(bounds.width)
    val height = height.coerceAtLeast(1f).coerceAtMost(bounds.height)
    val clampedLeft = left.coerceIn(bounds.left, bounds.right - width)
    val clampedTop = top.coerceIn(bounds.top, bounds.bottom - height)
    return androidx.compose.ui.geometry.Rect(
        left = clampedLeft,
        top = clampedTop,
        right = clampedLeft + width,
        bottom = clampedTop + height,
    )
}

private fun androidx.compose.ui.geometry.Rect?.toExpandOrigin(
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

private fun albumSearchHistoryEntry(album: Album): SearchHistoryEntry {
    return SearchHistoryEntry(
        key = "album:${album.id}",
        kind = SearchHistoryKind.Album,
        title = album.title,
        subtitle = album.artist,
        artUri = album.artUri,
        albumId = album.id,
    )
}

private fun artistSearchHistoryEntry(song: Song): SearchHistoryEntry {
    return SearchHistoryEntry(
        key = "artist:${song.artist.lowercase()}",
        kind = SearchHistoryKind.Artist,
        title = song.artist,
        subtitle = song.album,
        artUri = song.artUri,
        query = song.artist,
    )
}

private fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, audioPermission()) == PackageManager.PERMISSION_GRANTED
}

private fun hasNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun audioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    return formatTimestamp(durationMs)
}

private fun formatPlaybackPosition(positionMs: Long): String {
    if (positionMs <= 0L) return "00:00"
    return formatTimestamp(positionMs)
}

private fun formatTimestamp(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        val remainingMinutes = (totalSeconds % 3600) / 60
        "%d:%02d:%02d".format(hours, remainingMinutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
