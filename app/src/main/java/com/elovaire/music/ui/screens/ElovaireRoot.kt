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
import androidx.annotation.DrawableRes
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
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
import elovaire.music.app.data.lyrics.LyricsPayload
import elovaire.music.app.data.lyrics.LyricsResult
import elovaire.music.app.data.lyrics.LyricsService
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
import elovaire.music.app.domain.model.TextSizePreset
import elovaire.music.app.domain.model.ThemeMode
import elovaire.music.app.ui.components.ArtworkImage
import elovaire.music.app.ui.components.rememberArtworkBitmap
import elovaire.music.app.ui.components.rememberArtworkGradient
import elovaire.music.app.ui.theme.ElovaireMotion
import elovaire.music.app.ui.theme.ElovaireRadii
import elovaire.music.app.ui.theme.ElovaireSpacing
import elovaire.music.app.ui.theme.elovaireScaledSp
import elovaire.music.app.ui.theme.rememberElovaireOverscrollFactory
import elovaire.music.app.ui.theme.InkText
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.math.pow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val HOME_ROUTE = "home"
private const val ALBUMS_ROUTE = "albums"
private const val PLAYLISTS_ROUTE = "playlists"
private const val PLAYLIST_ROUTE = "playlist"
private const val SEARCH_ROUTE = "search"
private const val PLAYER_ROUTE = "player"
private const val EQUALIZER_ROUTE = "equalizer"
private const val SETTINGS_ROUTE = "settings"
private const val CHANGELOG_ROUTE = "changelog"
private const val ALBUM_ROUTE = "album"
private const val LIBRARY_COLLECTION_ROUTE = "library_collection"
private const val GENRE_ROUTE = "genre"
private const val ARTIST_ROUTE = "artist"

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && initialUri != null) {
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
        while (isActive) {
            bitmap = runCatching { hostView.rootView.drawToDownsampledBitmap() }.getOrNull()
            delay(180L)
        }
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
private fun ProgressiveChromeBackdrop(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val backdropBitmap = LocalChromeBackdropBitmap.current
    val matteColor = if (darkTheme) {
        Color(0xFF141414)
    } else {
        Color.White
    }
    val depthTint = if (darkTheme) {
        Color.Black.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.18f)
    }
    val edgeTint = if (darkTheme) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.42f)
    }
    val highlightTint = if (darkTheme) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.18f)
    }
    val matteBrush = if (flatOverlay) {
        Brush.verticalGradient(
            colors = List(2) { matteColor.copy(alpha = overlayAlpha ?: 0.7f) },
        )
    } else {
        when (edge) {
            ProgressiveChromeEdge.Top -> Brush.verticalGradient(
                colors = listOf(
                    matteColor.copy(alpha = overlayAlpha ?: 0.82f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.72f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.62f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.48f),
                ),
            )
            ProgressiveChromeEdge.Bottom -> Brush.verticalGradient(
                colors = listOf(
                    matteColor.copy(alpha = overlayAlpha ?: 0.26f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.38f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.5f),
                    matteColor.copy(alpha = overlayAlpha ?: 0.64f),
                ),
            )
        }
    }
    val depthBrush = when (edge) {
        ProgressiveChromeEdge.Top -> Brush.verticalGradient(
            colors = listOf(
                depthTint.copy(alpha = 0.78f),
                depthTint.copy(alpha = 0.32f),
                Color.Transparent,
            ),
        )
        ProgressiveChromeEdge.Bottom -> Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                depthTint.copy(alpha = 0.18f),
                depthTint.copy(alpha = 0.42f),
            ),
        )
    }
    val highlightBrush = when (edge) {
        ProgressiveChromeEdge.Top -> Brush.verticalGradient(
            colors = listOf(
                highlightTint,
                Color.Transparent,
            ),
        )
        ProgressiveChromeEdge.Bottom -> Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                highlightTint,
            ),
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() },
    ) {
        SnapshotBackdropBlurLayer(
            backdropBitmap = backdropBitmap,
            bounds = bounds,
            blurRadius = if (edge == ProgressiveChromeEdge.Bottom) 72.dp else 60.dp,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.98f },
        )
        SnapshotBackdropBlurLayer(
            backdropBitmap = backdropBitmap,
            bounds = bounds,
            blurRadius = if (edge == ProgressiveChromeEdge.Bottom) 34.dp else 26.dp,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (edge == ProgressiveChromeEdge.Bottom) 0.48f else 0.34f },
        )
        if (!flatOverlay) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.99f }
                    .blur(22.dp)
                    .background(depthBrush),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(highlightBrush),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(matteBrush),
        )
        if (showEdgeLine) {
            Box(
                modifier = Modifier
                    .align(
                        if (edge == ProgressiveChromeEdge.Top) {
                            Alignment.BottomCenter
                        } else {
                            Alignment.TopCenter
                        },
                    )
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(edgeTint),
            )
        }
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun ChromeHazeLayer(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
    modifier: Modifier = Modifier,
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
    edge: ProgressiveChromeEdge = ProgressiveChromeEdge.Top,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
    modifier: Modifier = Modifier,
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
    val playbackProgress by container.playbackManager.progressState.collectAsStateWithLifecycle()
    val eqSettings by container.preferenceStore.eqSettings.collectAsStateWithLifecycle()
    val themeMode by container.preferenceStore.themeMode.collectAsStateWithLifecycle()
    val textSizePreset by container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
    val searchHistory by container.preferenceStore.searchHistory.collectAsStateWithLifecycle()
    val playlists by container.preferenceStore.playlists.collectAsStateWithLifecycle()
    val favoriteSongIds by container.preferenceStore.favoriteSongIds.collectAsStateWithLifecycle()
    val libraryFolderUri by container.preferenceStore.libraryFolderUri.collectAsStateWithLifecycle()
    val libraryFolderPath by container.preferenceStore.libraryFolderPath.collectAsStateWithLifecycle()
    val favoriteSongIdSet = remember(favoriteSongIds) { favoriteSongIds.toHashSet() }
    val albumPlayCounts by container.preferenceStore.albumPlayCounts.collectAsStateWithLifecycle()
    val songPlayCounts by container.preferenceStore.songPlayCounts.collectAsStateWithLifecycle()
    val openPlayerRequestVersion by container.openPlayerRequestVersion.collectAsStateWithLifecycle()
    val appUpdateState by container.appUpdateManager.uiState.collectAsStateWithLifecycle()
    val changelogReleases = remember(context) { ChangelogRepository(context).loadReleases() }
    val rootScope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    var hasRequestedAudioPermission by rememberSaveable { mutableStateOf(false) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
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
    val lyricsService = remember(container) { LyricsService() }
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
    val libraryFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            }
            val resolvedPath = resolveTreePath(uri).ifBlank { uri.toString() }
            container.preferenceStore.setLibraryFolder(uri, resolvedPath)
            container.libraryRepository.setPreferredLibraryFolderPath(resolvedPath)
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

    LaunchedEffect(libraryFolderPath) {
        container.libraryRepository.setPreferredLibraryFolderPath(libraryFolderPath.takeIf { it.isNotBlank() })
    }

    if (!hasPermission) {
        PermissionGate(
            onRequestPermission = { permissionLauncher.launch(audioPermission()) },
        )
        return
    }

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
    var detailExpandOrigin by remember { mutableStateOf(ExpandOrigin()) }
    var lastPlayerOpenRequestAt by remember { mutableLongStateOf(0L) }
    var isSearchQueryActive by rememberSaveable { mutableStateOf(false) }
    var browsingOriginRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var selectedBottomRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
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
    ) && !(currentRoute == SEARCH_ROUTE && isSearchQueryActive)
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
    val canHostCompactNowPlaying = playbackState.currentSong != null && currentRoute != PLAYER_ROUTE
    val showGlobalNowPlaying = canHostCompactNowPlaying && !hideCompactNowPlaying
    val overscrollFactory = rememberElovaireOverscrollFactory()
    val navHostBlur = 0.dp
    val navHostScrimAlpha = 0f
    val openPlayerIfAllowed: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastPlayerOpenRequestAt > 450L) {
            lastPlayerOpenRequestAt = now
            navController.navigate(PLAYER_ROUTE)
        }
    }
    var lastHandledOpenPlayerRequest by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(openPlayerRequestVersion) {
        if (openPlayerRequestVersion > 0L && openPlayerRequestVersion != lastHandledOpenPlayerRequest) {
            lastHandledOpenPlayerRequest = openPlayerRequestVersion
            openPlayerIfAllowed()
        }
    }
    val rootView = LocalView.current
    val appBackground = MaterialTheme.colorScheme.background
    val darkTheme = appBackground.luminance() < 0.5f
    val chromeHazeState = rememberHazeState()
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
    val chromeBackdropBitmap = rememberChromeBackdropSnapshot(
        enabled = currentRoute != PLAYER_ROUTE,
        refreshKey = "$darkTheme:$currentRoute",
    )
    SideEffect {
        val window = (rootView.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, rootView)
        val usesLightSystemBarIcons = if (currentRoute == PLAYER_ROUTE) {
            playerAdaptivePalette.backdropBase.luminance() > 0.56f
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
        LocalChromeBackdropBitmap provides chromeBackdropBitmap,
        LocalChromeHazeState provides chromeHazeState,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showTopLevelChrome) {
                UnifiedTopBar(
                    title = topBarTitle(currentRoute),
                    showSettings = currentRoute in setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE),
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                    modifier = Modifier.fillMaxWidth(),
                )
                }
            },
        ) { innerPadding ->
            val topBarHeight = topBarOccupiedHeight()
            val detailTopBarHeight = detailTopBarOccupiedHeight()
            val bottomNavHeight = if (showBottomNavigation) bottomNavigationOccupiedHeight() else 0.dp
            val topContentPadding = if (showTopLevelChrome) {
                topBarHeight + ElovaireSpacing.topBarToFirstContentGap
            } else {
                innerPadding.calculateTopPadding()
            }
            val bottomContentPadding =
                bottomNavHeight +
                (if (showGlobalNowPlaying) ElovaireSpacing.miniPlayerReservedHeight else 0.dp) +
                ElovaireSpacing.scrollTailPadding
            val detailBottomPadding =
                bottomNavHeight +
                (if (showGlobalNowPlaying) ElovaireSpacing.miniPlayerReservedHeight else 0.dp) +
                ElovaireSpacing.scrollTailPadding

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
                        if (targetState.destination.route == PLAYER_ROUTE) {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.PlayerFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ) +
                                scaleIn(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialScale = 0.9f,
                                    transformOrigin = TransformOrigin(0.5f, 1f),
                                ) +
                                slideInVertically(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialOffsetY = { it / 14 },
                                )
                        } else if (targetState.destination.route.isExpandFromTileRoute()) {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ) +
                                scaleIn(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialScale = 0.8f,
                                    transformOrigin = detailExpandOrigin.toTransformOrigin(),
                                ) +
                                slideInHorizontally(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialOffsetX = { fullWidth ->
                                        ((detailExpandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
                                    },
                                ) +
                                slideInVertically(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialOffsetY = { fullHeight ->
                                        ((detailExpandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
                                    },
                                )
                        } else {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ) +
                                slideInHorizontally(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenSlide,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialOffsetX = { it / 6 },
                                ) +
                                scaleIn(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenFade,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialScale = 0.992f,
                                )
                        }
                    },
                    exitTransition = {
                        if (targetState.destination.route == PLAYER_ROUTE) {
                            fadeOut(
                                animationSpec = tween(
                                    ElovaireMotion.PlayerFade,
                                    easing = FastOutSlowInEasing,
                                ),
                            ) +
                                scaleOut(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetScale = 0.992f,
                                    transformOrigin = TransformOrigin(0.5f, 1f),
                                ) +
                                slideOutVertically(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetOffsetY = { -(it / 18) },
                                )
                        } else if (targetState.destination.route.isExpandFromTileRoute()) {
                            fadeOut(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        } else {
                            fadeOut(animationSpec = tween(ElovaireMotion.ScreenFade)) +
                                scaleOut(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenFade,
                                        easing = FastOutLinearInEasing,
                                    ),
                                    targetScale = 0.992f,
                                )
                        }
                    },
                    popEnterTransition = {
                        if (initialState.destination.route == PLAYER_ROUTE) {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.PlayerFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            )
                        } else if (initialState.destination.route.isExpandFromTileRoute()) {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            )
                        } else {
                            fadeIn(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ) +
                                slideInHorizontally(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenSlide,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialOffsetX = { -(it / 14) },
                                ) +
                                scaleIn(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenFade,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialScale = 0.992f,
                                )
                        }
                    },
                    popExitTransition = {
                        if (initialState.destination.route == PLAYER_ROUTE) {
                            fadeOut(
                                animationSpec = tween(
                                    ElovaireMotion.PlayerFade,
                                    easing = FastOutSlowInEasing,
                                ),
                            ) +
                                scaleOut(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetScale = 0.82f,
                                    transformOrigin = TransformOrigin(0.5f, 1f),
                                ) +
                                slideOutVertically(
                                    animationSpec = tween(
                                        ElovaireMotion.PlayerScreen,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetOffsetY = { it / 10 },
                                )
                        } else if (initialState.destination.route.isExpandFromTileRoute()) {
                            fadeOut(
                                animationSpec = tween(
                                    ElovaireMotion.ScreenFade,
                                    easing = FastOutSlowInEasing,
                                ),
                            ) +
                                scaleOut(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetScale = 0.84f,
                                    transformOrigin = detailExpandOrigin.toTransformOrigin(),
                                ) +
                                slideOutHorizontally(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetOffsetX = { fullWidth ->
                                        ((detailExpandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
                                    },
                                ) +
                                slideOutVertically(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenExpand,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetOffsetY = { fullHeight ->
                                        ((detailExpandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
                                    },
                                )
                        } else {
                            fadeOut(animationSpec = tween(ElovaireMotion.ScreenFade)) +
                                slideOutHorizontally(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenSlide,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetOffsetX = { it / 3 },
                                ) +
                                scaleOut(
                                    animationSpec = tween(
                                        ElovaireMotion.ScreenFade,
                                        easing = FastOutLinearInEasing,
                                    ),
                                    targetScale = 0.992f,
                                )
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
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
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
                            onCreatePlaylist = { name ->
                                container.preferenceStore.createPlaylist(name)
                            },
                            onOpenPlaylist = { playlist, origin ->
                                detailExpandOrigin = origin
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
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            onSearchQueryActiveChanged = { isSearchQueryActive = it },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = queue.playbackSourceLabel(fallbackAlbum = song.album),
                                )
                                openPlayerIfAllowed()
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
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
                            isCurrentSongPlaying = playbackState.isPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onPlayPlaylist = { songs, sourceLabel ->
                                songs.firstOrNull()?.let { firstSong ->
                                    container.playbackManager.playSong(
                                        song = firstSong,
                                        collection = songs,
                                        sourceLabel = sourceLabel,
                                    )
                                    openPlayerIfAllowed()
                                }
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = playlist?.name ?: queue.playbackSourceLabel(fallbackAlbum = song.album),
                                )
                                openPlayerIfAllowed()
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
                            isCurrentSongPlaying = playbackState.isPlaying,
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
                                openPlayerIfAllowed()
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
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = playbackState.isPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
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
                                openPlayerIfAllowed()
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
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
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
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
                            isCurrentSongPlaying = playbackState.isPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(song, queue, sourceLabel = artistName)
                                openPlayerIfAllowed()
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                        )
                    }

                    composable(PLAYER_ROUTE) {
                        val playbackProgress by container.playbackManager.progressState.collectAsStateWithLifecycle()
                        NowPlayingScreen(
                            playbackState = playbackState,
                            playbackProgress = playbackProgress,
                            isFavorite = playbackState.currentSong?.id in favoriteSongIdSet,
                            lyricsService = lyricsService,
                            onBack = navController::navigateUp,
                            onTogglePlayback = container.playbackManager::togglePlayback,
                            onSkipPrevious = container.playbackManager::skipPrevious,
                            onSkipNext = container.playbackManager::skipNext,
                            onCycleRepeatMode = container.playbackManager::cycleRepeatMode,
                            onToggleShuffle = container.playbackManager::toggleShuffle,
                            onToggleFavorite = { songId -> container.preferenceStore.toggleFavoriteSong(songId) },
                            onSeekTo = container.playbackManager::seekTo,
                            onQueueItemSelected = container.playbackManager::playQueueIndex,
                            onVolumeChanged = { volume ->
                                container.playbackManager.setVolume(volume)
                            },
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
                        )
                    }

                    composable(SETTINGS_ROUTE) {
                        SettingsScreen(
                            themeMode = themeMode,
                            textSizePreset = textSizePreset,
                            eqSettings = eqSettings,
                            libraryFolderPath = libraryFolderPath.ifBlank { container.libraryRepository.defaultMediaFolderPath() },
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onThemeModeSelected = container.preferenceStore::setThemeMode,
                            onTextSizePresetSelected = container.preferenceStore::setTextSizePreset,
                            onBassChanged = container.preferenceStore::updateBass,
                            onSpaciousnessChanged = container.preferenceStore::updateSpaciousness,
                            onOpenEqualizer = { navController.navigate(EQUALIZER_ROUTE) },
                            onOpenChangelog = { navController.navigate(CHANGELOG_ROUTE) },
                            onChangeLibraryFolder = {
                                libraryFolderPickerLauncher.launch(
                                    createLibraryFolderPickerIntent(
                                        defaultLibraryPickerUri(libraryFolderUri),
                                    ),
                                )
                            },
                        )
                    }

                    composable(CHANGELOG_ROUTE) {
                        ChangelogScreen(
                            releases = changelogReleases,
                            onBack = navController::navigateUp,
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
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = topBarHeight + 8.dp,
                        ),
                    visible = showTopLevelChrome && appUpdateState.availableRelease != null,
                    enter = fadeIn(animationSpec = tween(220)) +
                        slideInVertically(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            initialOffsetY = { -(it / 2) },
                        ),
                    exit = fadeOut(animationSpec = tween(180)) +
                        slideOutVertically(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            targetOffsetY = { -(it / 3) },
                        ),
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
                if (canHostCompactNowPlaying) {
                    playbackState.currentSong?.let { currentSong ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = if (showBottomNavigation) bottomNavHeight + 8.dp else navigationBarInsetDp() + 10.dp,
                                ),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            StandaloneNowPlayingDock(
                                song = currentSong,
                                isPlaying = playbackState.isPlaying,
                                progress = if (playbackProgress.durationMs > 0L) {
                                    (playbackProgress.positionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                },
                                visible = showGlobalNowPlaying,
                                onOpenPlayer = { navController.navigate(PLAYER_ROUTE) },
                                onTogglePlayback = container.playbackManager::togglePlayback,
                                onSkipPrevious = container.playbackManager::skipPrevious,
                                onSkipNext = container.playbackManager::skipNext,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    visible = showBottomNavigation,
                    enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard)) +
                        slideInVertically(
                            animationSpec = tween(ElovaireMotion.Standard),
                            initialOffsetY = { it / 2 },
                        ),
                    exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
                        slideOutVertically(
                            animationSpec = tween(ElovaireMotion.Quick),
                            targetOffsetY = { it / 2 },
                        ),
                ) {
                    BottomNavigationBar(
                        currentRoute = activeBottomRoute,
                        destinations = topLevelDestinations,
                        onNavigate = { route ->
                            browsingOriginRoute = route
                            selectedBottomRoute = route
                            val poppedToExistingRoot = navController.popBackStack(route, inclusive = false)
                            if (!poppedToExistingRoot && currentRoute != route) {
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(HOME_ROUTE) {
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
    }
}

@Composable
private fun StandaloneNowPlayingDock(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    visible: Boolean,
    onOpenPlayer: () -> Unit,
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
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard)) +
            slideInVertically(
                animationSpec = tween(ElovaireMotion.Standard),
                initialOffsetY = { -it / 2 },
            ),
        exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
            slideOutVertically(
                animationSpec = tween(ElovaireMotion.Quick),
                targetOffsetY = { it / 2 },
            ),
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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
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
        FrostedTopBarBackground(
            darkTheme = darkTheme,
            modifier = Modifier.matchParentSize(),
        )
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            if (showSettings) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_settings,
                    contentDescription = "Settings",
                    showBackground = false,
                    onClick = onOpenSettings,
                )
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
        FrostedTopBarBackground(
            darkTheme = darkTheme,
            modifier = Modifier.matchParentSize(),
        )
        if (centeredTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 18.dp, end = 18.dp, top = 3.dp, bottom = 13.dp)
                    .height(40.dp),
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
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
                        .padding(horizontal = 64.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 18.dp, end = 16.dp, top = 3.dp, bottom = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                )
                Box(
                    modifier = Modifier
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
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
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
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint.copy(alpha = if (enabled) 1f else 0.35f),
        )
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BottomNavigationBar(
    currentRoute: String,
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
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                overlayAlpha = 0.7f,
                flatOverlay = true,
                edge = ProgressiveChromeEdge.Bottom,
                showEdgeLine = false,
                modifier = Modifier.matchParentSize(),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        if (darkTheme) {
                            Color.White.copy(alpha = 0.08f)
                        } else {
                            Color.Black.copy(alpha = 0.06f)
                        },
                    ),
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
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            baseTint
        } else {
            baseTint.copy(alpha = 0.5f)
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "bottom_nav_icon_tint",
    )
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
    val baseIconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.8f,
            stiffness = 540f,
        ),
        label = "bottom_nav_base_icon_scale",
    )
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
    contentColor: Color,
    secondaryContentColor: Color,
    onOpenPlayer: () -> Unit,
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
        animationSpec = tween(ElovaireMotion.Controls),
        label = "mini_player_button_tint",
    )
    val controlIconTint by animateColorAsState(
        targetValue = if (controlTint.luminance() > 0.42f) InkText else Color.White,
        animationSpec = tween(ElovaireMotion.Controls),
        label = "mini_player_button_icon_tint",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 420f,
        ),
        label = "mini_player_play_button_scale",
    )
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 52.dp.toPx() }
    var dragOffsetX by remember(song.id) { mutableFloatStateOf(0f) }
    val animatedDragOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 380f,
        ),
        label = "mini_player_drag_offset_x",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.card))
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
                    .pointerInput(song.id) {
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenPlayer,
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.title,
                    modifier = Modifier.size(48.dp),
                    cornerRadius = ElovaireRadii.artworkSmall,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        color = contentColor,
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
                        color = secondaryContentColor,
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
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val showInitialLoadingState = isLibraryLoading &&
        recentlyAddedAlbums.isEmpty() &&
        favoriteAlbums.isEmpty() &&
        playbackState.recentSongIds.isEmpty()
    val showEmptyLibraryState = !isLibraryLoading &&
        recentlyAddedAlbums.isEmpty() &&
        favoriteAlbums.isEmpty() &&
        recentSongs.isEmpty()
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = when {
                showInitialLoadingState -> HomeScreenState.Loading
                showEmptyLibraryState -> HomeScreenState.Empty
                else -> HomeScreenState.Content
            },
            transitionSpec = {
                if (targetState == HomeScreenState.Loading) {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                } else {
                    (fadeIn(animationSpec = tween(260, delayMillis = 40)) +
                        slideInVertically(
                            animationSpec = tween(260, easing = LinearOutSlowInEasing),
                            initialOffsetY = { -it / 14 },
                        )) togetherWith fadeOut(animationSpec = tween(180))
                }
            },
            label = "home_loading_transition",
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
    val resolvedSurface = albumTint.compositeOver(baseTint)
    val contentColor = if (resolvedSurface.luminance() > 0.42f) InkText else Color.White
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
                    style = MaterialTheme.typography.bodyLarge,
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
    topPadding: Dp,
    bottomPadding: Dp,
    title: String = "All albums",
    subtitle: String = "Alphabetical by album artist, then album title.",
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    var layoutMode by rememberSaveable { mutableStateOf(AlbumLayoutMode.Grid) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
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
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionTitleRow(
                                title = title,
                                subtitle = subtitle,
                                compact = true,
                            )
                            LibraryModeToggle(
                                layoutMode = layoutMode,
                                onLayoutModeChanged = { layoutMode = it },
                            )
                        }
                    }
                }

                items(albums, key = { it.id }) { album ->
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
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionTitleRow(
                                title = title,
                                subtitle = subtitle,
                                compact = true,
                            )
                            LibraryModeToggle(
                                layoutMode = layoutMode,
                                onLayoutModeChanged = { layoutMode = it },
                            )
                        }
                    }
                }

                items(albums, key = { it.id }) { album ->
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
private fun PlaylistsScreen(
    playlists: List<Playlist>,
    libraryState: LibraryUiState,
    topPadding: Dp,
    bottomPadding: Dp,
    onCreatePlaylist: (String) -> Long,
    onOpenPlaylist: (Playlist, ExpandOrigin) -> Unit,
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    val songsById = remember(libraryState.songs) { libraryState.songs.associateBy { it.id } }
    val gridState = rememberLazyGridState()

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
                onCreate = { showCreateDialog = true },
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
                item {
                    CreatePlaylistTile(onClick = { showCreateDialog = true })
                }
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistGridTile(
                        playlist = playlist,
                        previewSongs = playlist.songIds.mapNotNull(songsById::get),
                        onClick = { origin -> onOpenPlaylist(playlist, origin) },
                    )
                }
            }
        }

        if (showCreateDialog) {
            PlaylistNameDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name ->
                    val createdId = onCreatePlaylist(name)
                    if (createdId > 0) {
                        showCreateDialog = false
                    }
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

    val listState = rememberLazyListState()
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
                            detail = "${formatCountLabel(totalAlbums, "album")} available offline",
                            onClick = { onOpenCollection(LibraryCollectionKind.Albums) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_mic_vocal,
                            title = "Artists",
                            detail = "${formatCountLabel(totalArtists, "artist")} detected",
                            onClick = { onOpenCollection(LibraryCollectionKind.Artists) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_gallery_vertical_end,
                            title = "Genres",
                            detail = "${formatCountLabel(totalGenres, "genre")} tagged",
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
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onGenreSelected: (String) -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    when (kind) {
        LibraryCollectionKind.Songs -> SongCollectionScreen(
            songs = libraryState.songs,
            favoriteSongIds = favoriteSongIds,
            currentSongId = currentSongId,
            isCurrentSongPlaying = isCurrentSongPlaying,
            bottomPadding = bottomPadding,
            onBack = onBack,
            onSongSelected = onSongSelected,
            onToggleFavorite = onToggleFavorite,
        )

        LibraryCollectionKind.Albums -> Box(modifier = Modifier.fillMaxSize()) {
            AlbumCollectionContent(
                albums = libraryState.albums,
                topPadding = detailTopBarOccupiedHeight(),
                bottomPadding = bottomPadding,
                title = "Albums",
                subtitle = "Alphabetical by album artist, then album title",
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
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    var layoutMode by rememberSaveable { mutableStateOf(AlbumLayoutMode.Compact) }
    var sortMode by rememberSaveable { mutableStateOf(SongSortMode.Title) }
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
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
                    top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SectionTitleRow(
                                    title = "All songs",
                                    subtitle = "Every track in your offline library",
                                    compact = true,
                                )
                                SongSortControl(
                                    selected = sortMode,
                                    expanded = showSortOptions,
                                    onToggleExpanded = { showSortOptions = !showSortOptions },
                                    onSelect = { selectedMode ->
                                        sortMode = selectedMode
                                        showSortOptions = false
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.align(Alignment.Top),
                            )
                            {
                                LibraryModeToggle(
                                    layoutMode = layoutMode,
                                    onLayoutModeChanged = { layoutMode = it },
                                )
                            }
                        }
                    }
                }
                items(sortedSongs, key = { it.id }) { song ->
                    SongGridCard(
                        song = song,
                        isFavorite = song.id in favoriteSongIds,
                        onClick = { onSongSelected(song, sortedSongs) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                    )
                }
            }
        } else {
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
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SectionTitleRow(
                                    title = "All songs",
                                    subtitle = "Every track in your offline library",
                                    compact = true,
                                )
                                SongSortControl(
                                    selected = sortMode,
                                    expanded = showSortOptions,
                                    onToggleExpanded = { showSortOptions = !showSortOptions },
                                    onSelect = { selectedMode ->
                                        sortMode = selectedMode
                                        showSortOptions = false
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.align(Alignment.Top),
                            )
                            {
                                LibraryModeToggle(
                                    layoutMode = layoutMode,
                                    onLayoutModeChanged = { layoutMode = it },
                                )
                            }
                        }
                    }
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
                            showDivider = index != sortedSongs.lastIndex,
                        )
                    }
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
                    painter = painterResource(id = R.drawable.ic_lucide_align_left),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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
    var layoutMode by rememberSaveable { mutableStateOf(AlbumLayoutMode.Compact) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
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
                    top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionTitleRow(
                                title = "All artists",
                                subtitle = "Artist names pulled from your local tags",
                                compact = true,
                            )
                            LibraryModeToggle(
                                layoutMode = layoutMode,
                                onLayoutModeChanged = { layoutMode = it },
                            )
                        }
                    }
                }
                items(artists, key = { it.name }) { artist ->
                    ArtistGridCard(
                        artist = artist,
                        onClick = { onArtistSelected(artist.name) },
                    )
                }
            }
            FastScrollbar(
                state = gridState,
                topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
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
                    top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ModuleCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionTitleRow(
                                title = "All artists",
                                subtitle = "Artist names pulled from your local tags",
                                compact = true,
                            )
                            LibraryModeToggle(
                                layoutMode = layoutMode,
                                onLayoutModeChanged = { layoutMode = it },
                            )
                        }
                    }
                }
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
        }

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
    val listState = rememberLazyListState()
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
                    SectionTitleRow(
                        title = "Genres",
                        subtitle = "Albums grouped from your embedded genre tags",
                        compact = true,
                    )
                }
            }
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
    bottomPadding: Dp,
    onBack: () -> Unit,
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
            topPadding = detailTopBarOccupiedHeight(),
            bottomPadding = bottomPadding,
            title = genre.ifBlank { "Unknown Genre" },
            subtitle = "${filteredAlbums.size} albums tagged in this genre",
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
        val listState = rememberLazyListState()
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
                    ModuleCard {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SectionTitleRow(
                                title = "Most played songs",
                                subtitle = "${topSongs.size} tracks you return to the most",
                                compact = true,
                            )
                            Column {
                                topSongs.forEachIndexed { index, song ->
                                    GroupedListRowContainer(
                                        index = index,
                                        lastIndex = topSongs.lastIndex,
                                    ) {
                                        PlaylistSongRow(
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
                            AlbumPosterGrid(
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
private fun EmptyPlaylistState(
    modifier: Modifier = Modifier,
    onCreate: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    onClick: (ExpandOrigin) -> Unit,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = Modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .clickable { onClick(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaylistArtworkPreview(
            songs = previewSongs,
            title = playlist.name,
            modifier = Modifier.fillMaxWidth(),
        )
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
                    LazyVerticalGrid(
                        overscrollEffect = null,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        itemsIndexed(collageSongs, key = { index, song -> "${song.id}_$index" }) { _, song ->
                            ArtworkImage(
                                uri = song.artUri,
                                title = song.title,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 0.dp,
                            )
                        }
                    }
                }

                songs.isNotEmpty() -> {
                    ArtworkImage(
                        uri = coverSong?.artUri,
                        title = coverSong?.title ?: title,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = ElovaireRadii.artwork,
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
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                shape = RoundedCornerShape(ElovaireRadii.input),
                placeholder = { Text("Playlist name") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("Create")
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
private fun SearchScreen(
    libraryState: LibraryUiState,
    playbackState: PlaybackUiState,
    albumPlayCounts: Map<Long, Int>,
    recentSearches: List<SearchHistoryEntry>,
    topPadding: Dp,
    bottomPadding: Dp,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onArtistSelected: (String) -> Unit,
    onRememberAlbumSearch: (Album) -> Unit,
    onRememberArtistSearch: (Song) -> Unit,
    onClearSearchHistory: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var isSearchFieldFocused by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedQuery = query.trim()
    val isSearchUiActive = trimmedQuery.isNotBlank() || isSearchFieldFocused
    LaunchedEffect(trimmedQuery, isSearchFieldFocused) {
        onSearchQueryActiveChanged(isSearchUiActive)
    }
    val matchingSongs = remember(trimmedQuery, libraryState.songs) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            libraryState.songs.filter { song ->
                song.title.contains(trimmedQuery, ignoreCase = true) ||
                    song.artist.contains(trimmedQuery, ignoreCase = true) ||
                    song.album.contains(trimmedQuery, ignoreCase = true)
            }.take(20)
        }
    }
    val matchingAlbums = remember(trimmedQuery, libraryState.albums) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            libraryState.albums.filter { album ->
                album.title.contains(trimmedQuery, ignoreCase = true) ||
                    album.artist.contains(trimmedQuery, ignoreCase = true)
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
                    artist.title.contains(trimmedQuery, ignoreCase = true)
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isSearchFieldFocused = focusState.isFocused
                        },
                    shape = RoundedCornerShape(ElovaireRadii.input),
                    singleLine = true,
                    placeholder = { Text("Artists, albums, playlists, and more") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            if (trimmedQuery.isBlank()) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        SearchHistorySectionHeader(
                            showClearAction = true,
                            onClearHistory = onClearSearchHistory,
                        )
                    }
                    item {
                        SearchHistoryListCard(
                            entries = recentSearches.take(6),
                            onAlbumSelected = { albumId ->
                                libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                    onAlbumSelected(album, ExpandOrigin())
                                }
                            },
                            onArtistSelected = onArtistSelected,
                        )
                    }
                } else {
                    item {
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.74f),
                                )
                            }
                        }
                    }
                }
                if (suggestedAlbums.isNotEmpty()) {
                    item {
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
            } else {
            if (matchingArtists.isNotEmpty()) {
                item {
                    SectionTitleRow(
                        title = "Artists",
                        subtitle = "${matchingArtists.size} matching artists",
                    )
                }
                item {
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
            }

            if (matchingAlbums.isNotEmpty()) {
                item {
                    SectionTitleRow(
                        title = "Albums",
                        subtitle = "${matchingAlbums.size} matching album results",
                    )
                }
                item {
                    LazyRow(
                        overscrollEffect = null,
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
            }

            if (matchingSongs.isNotEmpty()) {
                item {
                    SectionTitleRow(
                        title = "Songs",
                        subtitle = "${matchingSongs.size} matching song results",
                    )
                }
                itemsIndexed(
                    items = matchingSongs,
                    key = { _, song -> song.id },
                    contentType = { _, _ -> "search_song_row" },
                ) { index, song ->
                    GroupedListRowContainer(
                        index = index,
                        lastIndex = matchingSongs.lastIndex,
                    ) {
                        SearchSongRow(
                            song = song,
                            onClick = {
                                onRememberArtistSearch(song)
                                onSongSelected(song, matchingSongs)
                            },
                            showDivider = index != matchingSongs.lastIndex,
                        )
                    }
                }
            }

            if (matchingAlbums.isEmpty() && matchingSongs.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No results",
                        message = "Nothing in the current offline library matches \"$trimmedQuery\" yet",
                    )
                }
            }
        }
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = bottomPadding + 10.dp),
            visible = isSearchUiActive,
            enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard)) +
                slideInVertically(
                    animationSpec = tween(ElovaireMotion.Standard),
                    initialOffsetY = { it / 2 },
                ),
            exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)) +
                slideOutVertically(
                    animationSpec = tween(ElovaireMotion.Quick),
                    targetOffsetY = { it / 2 },
                ),
        ) {
            Surface(
                onClick = {
                    query = ""
                    isSearchFieldFocused = false
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                },
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                contentColor = MaterialTheme.colorScheme.onSurface,
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
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.1f else 0.05f,
            )
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(ElovaireMotion.Quick),
        label = "toggle_chip_background",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = 0.dp,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
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
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge,
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
    title: String = "Your favourite albums",
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
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = readableSecondaryTextColor(),
                )
            }

            albums.chunked(2).take(3).forEach { rowAlbums ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cellColor = if (darkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.34f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
    }

    Surface(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() },
        shape = RoundedCornerShape(ElovaireRadii.tile),
        color = cellColor,
        onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.size(42.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
                showArtworkGlow = true,
            )
            Column(
                modifier = Modifier.weight(1f),
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
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (song.isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = elovaireScaledSp(8.8f),
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
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
            ArtworkImage(
                uri = song.artUri,
                title = song.title,
                modifier = Modifier.size(44.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
                showArtworkGlow = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (song.isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = elovaireScaledSp(8.8f),
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
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
        color = readableCardSurfaceColor(),
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                    text = "${album.songCount} tracks  •  ${formatDuration(album.durationMs)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
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
    val albumFavoriteBackground = gradient.first().copy(alpha = 0.26f).compositeOver(Color.Black.copy(alpha = 0.18f))
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
            add("${album.songCount} tracks")
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
                    ),
                )
                append(item)
                pop()
            }
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
            .background(Brush.verticalGradient(gradient)),
    ) {
        val listState = rememberLazyListState()
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
                                            gradient.first().copy(alpha = 0.41f),
                                            gradient.last().copy(alpha = 0.14f),
                                            Color.Transparent,
                                        ),
                                        radius = 780f,
                                    ),
                                    shape = RoundedCornerShape(ElovaireRadii.module),
                                )
                                .blur(40.dp),
                        )
                        Surface(
                            modifier = Modifier.matchParentSize(),
                            shape = RoundedCornerShape(ElovaireRadii.module),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f),
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
                                fontSize = elovaireScaledSp(24f),
                                lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8f,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                albumTitleBounds = coordinates.boundsInWindow()
                            },
                        )
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.bodyLarge,
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
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (song.isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = elovaireScaledSp(8.8f),
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
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
        val listState = rememberLazyListState()
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
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (song.isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = elovaireScaledSp(8.8f),
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
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
                modifier = Modifier.width(64.dp),
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
        content()
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
        FrostedTopBarBackground(
            darkTheme = darkTheme,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 3.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconButton(
                iconResId = R.drawable.ic_lucide_chevron_left,
                contentDescription = "Back",
                showBackground = false,
                onClick = onBack,
            )
            if (subtitle.isNullOrBlank()) {
                Box(
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(1f),
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add songs") },
        text = {
            LazyColumn(
                overscrollEffect = null,
                modifier = Modifier.height(320.dp),
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
    playbackState: PlaybackUiState,
    playbackProgress: PlaybackProgressState,
    isFavorite: Boolean,
    lyricsService: LyricsService,
    onBack: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onQueueItemSelected: (Int) -> Unit,
    onVolumeChanged: (Float) -> Unit,
) {
    val currentSong = playbackState.currentSong
    val playerHazeState = rememberHazeState()
    var playerDismissTriggered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(currentSong?.id) {
        if (currentSong == null) {
            if (!playerDismissTriggered) {
                playerDismissTriggered = true
                onBack()
            }
        } else {
            playerDismissTriggered = false
        }
    }
    val appBackground = MaterialTheme.colorScheme.background
    val gradient = rememberArtworkGradient(currentSong?.artUri).value
    val artwork = rememberArtworkBitmap(currentSong?.artUri, size = 1024)
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
    val playingFromText = remember(playbackState.sourceLabel, currentSong?.album) {
        playbackState.sourceLabel
            ?.takeIf { it.isNotBlank() }
            ?.let { "Playing from $it" }
            ?: currentSong?.album?.takeIf { it.isNotBlank() }?.let { "Playing from $it" }
            ?: "Playing from all songs"
    }
    var showLyricsSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var showQueueSheet by remember(currentSong?.id) { mutableStateOf(false) }
    LaunchedEffect(currentSong?.id, playbackState.currentIndex, playbackState.queue.size) {
        val queue = playbackState.queue
        val currentIndex = playbackState.currentIndex
        currentSong?.let { lyricsService.prefetchLyrics(it) }
        queue.getOrNull(currentIndex + 1)?.let { lyricsService.prefetchLyrics(it) }
        queue.getOrNull(currentIndex - 1)?.let { lyricsService.prefetchLyrics(it) }
    }
    val lyricsUiState by produceState<LyricsUiState>(
        initialValue = when {
            !showLyricsSheet || currentSong == null -> LyricsUiState.Hidden
            else -> lyricsService.cachedLyrics(currentSong)?.toUiState() ?: LyricsUiState.Loading
        },
        key1 = showLyricsSheet,
        key2 = currentSong?.id,
    ) {
        if (!showLyricsSheet || currentSong == null) {
            value = LyricsUiState.Hidden
            return@produceState
        }
        value = lyricsService.cachedLyrics(currentSong)?.toUiState() ?: LyricsUiState.Loading
        val immediateResult = kotlinx.coroutines.withTimeoutOrNull(2_400L) {
            lyricsService.fetchLyrics(currentSong)
        }
        if (immediateResult != null) {
            value = immediateResult.toUiState()
        } else {
            value = LyricsUiState.Loading
            value = lyricsService.fetchLyrics(currentSong).toUiState()
        }
    }

    var dragValue by remember(currentSong?.id) { mutableFloatStateOf(0f) }
    var isScrubbing by remember(currentSong?.id) { mutableStateOf(false) }
    var pendingSeekPositionMs by remember(currentSong?.id) { mutableStateOf<Long?>(null) }
    var pendingSeekIssuedAtMs by remember(currentSong?.id) { mutableLongStateOf(0L) }

    LaunchedEffect(currentSong?.id, playbackProgress.durationMs) {
        pendingSeekPositionMs = null
        pendingSeekIssuedAtMs = 0L
        dragValue = if (playbackProgress.durationMs > 0) {
            playbackProgress.positionMs.toFloat() / playbackProgress.durationMs.toFloat()
        } else {
            0f
        }
    }

    LaunchedEffect(playbackProgress.positionMs, playbackProgress.durationMs, isScrubbing, pendingSeekPositionMs, pendingSeekIssuedAtMs) {
        if (playbackProgress.durationMs <= 0) {
            dragValue = 0f
            pendingSeekPositionMs = null
            pendingSeekIssuedAtMs = 0L
            return@LaunchedEffect
        }

        val actualFraction = (playbackProgress.positionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
        if (isScrubbing) return@LaunchedEffect

        val targetPendingPositionMs = pendingSeekPositionMs
        if (targetPendingPositionMs != null) {
            dragValue = (targetPendingPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
            val isPlayerCaughtUp = kotlin.math.abs(playbackProgress.positionMs - targetPendingPositionMs) <= 600L ||
                System.currentTimeMillis() - pendingSeekIssuedAtMs >= 1_200L
            if (isPlayerCaughtUp) {
                pendingSeekPositionMs = null
                pendingSeekIssuedAtMs = 0L
                dragValue = actualFraction
            }
        } else {
            dragValue = actualFraction
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseSurface)
            .hazeSource(playerHazeState),
    ) {
        AnimatedContent(
            targetState = currentSong?.artUri,
            transitionSpec = {
                fadeIn(animationSpec = tween(420, easing = LinearOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialScale = 1.02f,
                    ) togetherWith
                    fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
            },
            label = "player_background_artwork",
        ) { artUri ->
            val artworkBitmap = rememberArtworkBitmap(artUri, size = 1024).value
            if (artworkBitmap != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = artworkBitmap,
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
                        bitmap = artworkBitmap,
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

        val playerContentAlpha by animateFloatAsState(
            targetValue = if (showLyricsSheet) 0f else 1f,
            animationSpec = tween(ElovaireMotion.Standard),
            label = "player_content_alpha",
        )
        var dragOffsetY by remember(currentSong?.id) { mutableFloatStateOf(0f) }
        val animatedDragOffsetY by animateFloatAsState(
            targetValue = dragOffsetY,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 320f,
            ),
            label = "player_drag_offset",
        )
        val dragDismissProgress = (animatedDragOffsetY / 240f).coerceIn(0f, 1f)
        val playerScreenAlpha by animateFloatAsState(
            targetValue = (1f - (dragDismissProgress * 0.18f)).coerceIn(0.82f, 1f),
            animationSpec = tween(120),
            label = "player_drag_alpha",
        )

        CompositionLocalProvider(LocalPlayerHazeState provides playerHazeState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 20.dp)
                    .alpha(playerContentAlpha * playerScreenAlpha)
                    .graphicsLayer {
                        translationY = animatedDragOffsetY
                    },
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            if (currentSong == null) {
                Spacer(modifier = Modifier.fillMaxSize())
                return@Column
            }

            val centeredInfoWidth = 0.95f
            val nowPlayingTitleTopGap = ElovaireSpacing.nowPlayingTitleTopGap
            val nowPlayingTitleBottomGap = ElovaireSpacing.nowPlayingTitleBottomGap
            val displayedPositionMs = remember(dragValue, isScrubbing, pendingSeekPositionMs, playbackProgress.positionMs, playbackProgress.durationMs) {
                if ((isScrubbing || pendingSeekPositionMs != null) && playbackProgress.durationMs > 0) {
                    (playbackProgress.durationMs * dragValue.coerceIn(0f, 1f)).toLong()
                } else {
                    playbackProgress.positionMs
                }
            }
            val artworkScale by animateFloatAsState(
                targetValue = if (playbackState.isPlaying) 1f else 0.95f,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 260f,
                ),
                label = "now_playing_artwork_scale",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(currentSong.id) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 0f) {
                                    dragOffsetY = (dragOffsetY + dragAmount).coerceIn(0f, 320f)
                                } else {
                                    dragOffsetY = (dragOffsetY + (dragAmount * 0.32f)).coerceAtLeast(0f)
                                }
                            },
                            onDragEnd = {
                                if (dragOffsetY > 120f) {
                                    onBack()
                                } else {
                                    dragOffsetY = 0f
                                }
                            },
                            onDragCancel = {
                                dragOffsetY = 0f
                            },
                        )
                    },
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
                            onClick = onBack,
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
                                .fillMaxWidth()
                                .scale(artworkScale)
                                .aspectRatio(1f),
                            cornerRadius = ElovaireRadii.module,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(centeredInfoWidth)
                    .padding(top = nowPlayingTitleTopGap, bottom = nowPlayingTitleBottomGap)
                    .align(Alignment.CenterHorizontally),
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
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = animatedSong.title,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(22f)),
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                                repeatDelayMillis = 2500,
                                initialDelayMillis = 2500,
                                velocity = 28.dp,
                            ),
                        )
                        Text(
                            text = animatedSong.artist,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                            color = secondaryContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FavoriteSongButton(
                    isFavorite = isFavorite,
                    tint = contentColor,
                    onClick = { onToggleFavorite(currentSong.id) },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(centeredInfoWidth)
                    .align(Alignment.CenterHorizontally)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !showQueueSheet,
                        modifier = Modifier.fillMaxSize(),
                        enter = fadeIn(animationSpec = tween(ElovaireMotion.Standard)),
                        exit = fadeOut(animationSpec = tween(ElovaireMotion.Quick)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                PlaybackProgressBar(
                                    progress = dragValue,
                                    isInteracting = isScrubbing || pendingSeekPositionMs != null,
                                    contentColor = contentColor,
                                    onScrubStarted = {
                                        isScrubbing = true
                                        pendingSeekPositionMs = null
                                        pendingSeekIssuedAtMs = 0L
                                    },
                                    onScrubFractionChanged = { fraction ->
                                        dragValue = fraction
                                    },
                                    onScrubFinished = { fraction ->
                                        isScrubbing = false
                                        dragValue = fraction
                                        val target = (playbackProgress.durationMs * fraction.coerceIn(0f, 1f)).toLong()
                                        pendingSeekPositionMs = target
                                        pendingSeekIssuedAtMs = System.currentTimeMillis()
                                        onSeekTo(target)
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
                                        format = currentSong.audioFormat,
                                        quality = currentSong.audioQuality,
                                        tint = contentColor,
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Text(
                                            text = formatDuration(playbackProgress.durationMs),
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
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(22.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        PlayerTransportButton(
                                            iconResId = R.drawable.ic_elovaire_fast_previous_filled,
                                            contentDescription = "Previous",
                                            tint = contentColor,
                                            iconSize = 34.dp,
                                            onClick = onSkipPrevious,
                                        )
                                        PlayerTransportButton(
                                            iconResId = if (playbackState.isPlaying) R.drawable.ic_elovaire_pause_filled else R.drawable.ic_lucide_play,
                                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                            tint = contentColor,
                                            iconSize = 42.dp,
                                            onClick = onTogglePlayback,
                                        )
                                        PlayerTransportButton(
                                            iconResId = R.drawable.ic_elovaire_fast_forward_filled,
                                            contentDescription = "Next",
                                            tint = contentColor,
                                            iconSize = 34.dp,
                                            onClick = onSkipNext,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showQueueSheet,
                        modifier = Modifier.fillMaxSize(),
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
                            modifier = Modifier.fillMaxSize(),
                            onSongSelected = onQueueItemSelected,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        iconResId = R.drawable.ic_lucide_shuffle,
                        label = "Shuffle",
                        tint = contentColor,
                        showBackground = playbackState.shuffleEnabled,
                        onClick = onToggleShuffle,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    QueueMenuButton(
                        iconResId = R.drawable.ic_lucide_ellipsis_vertical,
                        tint = contentColor,
                        active = showQueueSheet,
                        onClick = { showQueueSheet = !showQueueSheet },
                    )
                }
            }

            VolumeControlBar(
                volume = playbackState.volume,
                contentColor = contentColor,
                onVolumeChanged = onVolumeChanged,
                modifier = Modifier
                    .fillMaxWidth(centeredInfoWidth)
                    .align(Alignment.CenterHorizontally),
            )
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
                playbackProgress = playbackProgress,
                lyricsUiState = lyricsUiState,
                tintColor = baseSurface.copy(alpha = 0.66f),
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onSeekTo = onSeekTo,
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
private fun QueueSheet(
    queue: List<Song>,
    currentIndex: Int,
    tint: Color,
    secondaryTint: Color,
    onSongSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
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
        color = tint.copy(alpha = 0.2f),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(vertical = 6.dp),
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
                )
            }
        }
    }
}

@Composable
private fun QueueSongRow(
    song: Song,
    index: Int,
    active: Boolean,
    tint: Color,
    secondaryTint: Color,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = (index + 1).toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (active) tint else secondaryTint,
                modifier = Modifier.width(20.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
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
                    color = secondaryTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.labelLarge,
                color = secondaryTint,
                maxLines = 1,
            )
        }
        if (showDivider) {
            DividerLine()
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
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(180L)
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
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(180L)
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
    val hostView = LocalView.current
    var expanded by remember(song.id) { mutableStateOf(false) }
    var showPlaylistDialog by remember(song.id) { mutableStateOf(false) }
    var backdropBitmap by remember(song.id) { mutableStateOf<BackdropSnapshot?>(null) }
    var menuBounds by remember(song.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
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

    Box {
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(buttonScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        backdropBitmap = runCatching { hostView.rootView.drawToDownsampledBitmap() }.getOrNull()
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                menuBounds = null
                backdropBitmap = null
            },
            containerColor = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            FrostedContextMenuSurface(
                modifier = Modifier
                    .width(208.dp)
                    .onGloballyPositioned { menuBounds = it.boundsInWindow() },
                backdropBitmap = backdropBitmap,
                menuBounds = menuBounds,
            ) {
                SongContextMenuItem(
                    iconResId = R.drawable.ic_lucide_list_music,
                    text = "Add to playlist",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        expanded = false
                        menuBounds = null
                        backdropBitmap = null
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
                        menuBounds = null
                        backdropBitmap = null
                        actions.onAddToQueue(song)
                    },
                )
                DividerLine()
                SongContextMenuItem(
                    iconResId = R.drawable.ic_lucide_trash_2,
                    text = "Delete from library",
                    tint = Color(0xFFFF5C5C),
                    onClick = {
                        expanded = false
                        menuBounds = null
                        backdropBitmap = null
                        actions.onDeleteFromLibrary(song)
                    },
                )
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
    backdropBitmap: BackdropSnapshot?,
    menuBounds: androidx.compose.ui.geometry.Rect?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shape = RoundedCornerShape(ElovaireRadii.card)
    val croppedBackdrop = remember(backdropBitmap, menuBounds) {
        if (backdropBitmap == null || menuBounds == null) {
            null
        } else {
            runCatching {
                val sourceBitmap = backdropBitmap.bitmap
                val scaleX = sourceBitmap.width.toFloat() / backdropBitmap.sourceWidth.toFloat()
                val scaleY = sourceBitmap.height.toFloat() / backdropBitmap.sourceHeight.toFloat()
                val left = (menuBounds.left * scaleX).roundToInt().coerceIn(0, sourceBitmap.width - 1)
                val top = (menuBounds.top * scaleY).roundToInt().coerceIn(0, sourceBitmap.height - 1)
                val width = (menuBounds.width * scaleX).roundToInt().coerceAtLeast(1)
                    .coerceAtMost(sourceBitmap.width - left)
                val height = (menuBounds.height * scaleY).roundToInt().coerceAtLeast(1)
                    .coerceAtMost(sourceBitmap.height - top)
                Bitmap.createBitmap(sourceBitmap, left, top, width, height)
            }.getOrNull()
        }
    }
    val matteTint = if (darkTheme) {
        Color(0xFF141414).copy(alpha = 0.5f)
    } else {
        readableCardSurfaceColor().copy(alpha = 0.5f)
    }
    val softTint = if (darkTheme) {
        Color.Black.copy(alpha = 0.14f)
    } else {
        Color.White.copy(alpha = 0.16f)
    }
    val edgeTint = if (darkTheme) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.White.copy(alpha = 0.48f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, edgeTint, shape)
            .background(Color.Transparent),
    ) {
        if (croppedBackdrop != null) {
            Image(
                bitmap = croppedBackdrop.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.995f }
                    .blur(52.dp),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.99f }
                .blur(14.dp)
                .background(softTint),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(matteTint),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (darkTheme) 0.04f else 0.1f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SongContextMenuItem(
    @DrawableRes iconResId: Int,
    text: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
        )
    }
}

@Composable
private fun AddToPlaylistPickerDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
) {
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
                    overscrollEffect = null,
                    modifier = Modifier.height(280.dp),
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
    val hideButtonArea = 112.dp
    val lyricLines = remember(lyricsUiState) {
        when (lyricsUiState) {
            is LyricsUiState.Ready -> lyricsUiState.payload.lines

            else -> emptyList()
        }
    }
    val listState = rememberLazyListState()
    val activeLyricLineIndex = remember(playbackProgress.positionMs, playbackProgress.durationMs, lyricLines, lyricsUiState) {
        when (lyricsUiState) {
            is LyricsUiState.Ready -> activeLyricLineIndex(
                lines = lyricLines,
                isSynced = lyricsUiState.payload.isSynced,
                positionMs = playbackProgress.positionMs,
                durationMs = playbackProgress.durationMs,
            )

            else -> -1
        }
    }

    LaunchedEffect(activeLyricLineIndex, lyricLines.size) {
        if (lyricLines.isNotEmpty() && activeLyricLineIndex >= 0) {
            listState.animateScrollToItem((activeLyricLineIndex - 3).coerceAtLeast(0))
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
                            LazyColumn(
                                state = listState,
                                overscrollEffect = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .ensureSingleItemRubberBand(listState),
                                contentPadding = PaddingValues(top = 12.dp, bottom = hideButtonArea),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                itemsIndexed(lyricLines) { index, line ->
                                    val isActive = index == activeLyricLineIndex
                                    val lineFontSize by animateFloatAsState(
                                        targetValue = if (isActive) {
                                            MaterialTheme.typography.headlineMedium.fontSize.value - 1f
                                        } else {
                                            MaterialTheme.typography.headlineMedium.fontSize.value - 3f
                                        },
                                        animationSpec = tween(ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                                        label = "lyrics_line_font_$index",
                                    )
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = lineFontSize.sp,
                                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                            lineHeight = 37.sp,
                                        ),
                                        color = if (isActive) contentColor else contentColor.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    lyricsSeekPositionMs(
                                                        lines = lyricLines,
                                                        index = index,
                                                        isSynced = state.payload.isSynced,
                                                        durationMs = playbackProgress.durationMs,
                                                    )?.let(onSeekTo)
                                                },
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(hideButtonArea)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    tintColor.copy(alpha = 0.72f),
                                    tintColor.copy(alpha = 0.96f),
                                ),
                            ),
                        ),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = tint.copy(alpha = 0.92f),
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = tint.copy(alpha = 0.88f),
            )
        }
    }
    LaunchedEffect(transientHighlight) {
        if (transientHighlight) {
            delay(220L)
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
                            onScrubStarted()
                            var latestFraction = (down.position.x / maxWidthPx).coerceIn(0f, 1f)
                            onScrubFractionChanged(latestFraction)

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                latestFraction = (change.position.x / maxWidthPx).coerceIn(0f, 1f)
                                onScrubFractionChanged(latestFraction)
                                change.consume()
                            }

                            onScrubFinished(latestFraction)
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
            dampingRatio = 0.82f,
            stiffness = 280f,
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
                    .height(6.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(contentColor.copy(alpha = 0.2f))
                    .align(Alignment.CenterStart)
                    .pointerInput(maxWidthPx) {
                        detectTapGestures { offset ->
                            onVolumeChanged((offset.x / maxWidthPx).coerceIn(0f, 1f))
                        }
                    }
                    .pointerInput(maxWidthPx) {
                        detectHorizontalDragGestures { change, _ ->
                            onVolumeChanged((change.position.x / maxWidthPx).coerceIn(0f, 1f))
                        }
                    },
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
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_volume_2),
            contentDescription = "Maximum volume",
            tint = contentColor.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun BoxScope.FastScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (visibleItems.isEmpty() || totalItems <= visibleItems.size) return

    val viewportHeightPx = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
    val averageItemHeightPx = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
    val estimatedContentHeightPx = max(viewportHeightPx, averageItemHeightPx * totalItems)
    val scrollableContentHeightPx = max(estimatedContentHeightPx - viewportHeightPx, 1f)
    val currentScrollPx =
        (state.firstVisibleItemIndex * averageItemHeightPx + state.firstVisibleItemScrollOffset).coerceAtLeast(0f)
    val scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f)

    FastScrollbarTrack(
        scrollFraction = scrollFraction,
        visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
        totalItems = totalItems,
        visibleItemsCount = visibleItems.size,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleIndex = (totalItems - visibleItems.size).coerceAtLeast(0)
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
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (visibleItems.isEmpty() || totalItems <= visibleItems.size) return

    val viewportHeightPx = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
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
    val scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f)

    FastScrollbarTrack(
        scrollFraction = scrollFraction,
        visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
        totalItems = totalItems,
        visibleItemsCount = visibleItems.size,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleRow = (totalRows - visibleRows).coerceAtLeast(0)
            val targetRow = (maxFirstVisibleRow * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleRow)
            val targetIndex = (targetRow * spanCount).coerceIn(0, (totalItems - 1).coerceAtLeast(0))
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
            .padding(top = topInset, end = 1.dp, bottom = bottomInset)
            .width(18.dp),
    ) {
        val density = LocalDensity.current
        val trackHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val thumbHeightPx = max(with(density) { 40.dp.toPx() }, trackHeightPx * visibleFraction)
        val trackTravelPx = max(trackHeightPx - thumbHeightPx, 1f)
        val thumbOffsetPx = trackTravelPx * animatedScrollFraction
        val fractionForPosition: (Float) -> Float = { y ->
            ((y - (thumbHeightPx / 2f)) / trackTravelPx).coerceIn(0f, 1f)
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
                    .width(4.dp)
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
        PlaybackRepeatMode.Off -> R.drawable.ic_lucide_list_music
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
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader(
                            title = "Live response",
                            subtitle = "Drag directly on the curve to shape the spectrum",
                        )
                        EqResponseGraph(
                            settings = settings,
                            onBandChanged = onBandChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(232.dp),
                        )
                    }
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionHeader(
                            title = "Tone shaping",
                            subtitle = "Macro controls tuned to stay musical and clean",
                        )
                        EqMacroSliderCard(
                            title = "Bass",
                            subtitle = "Weight and punch",
                            accent = Color(0xFFFF8A5B),
                            value = settings.bass,
                            onValueChange = onBassChanged,
                        )
                        EqMacroSliderCard(
                            title = "Treble",
                            subtitle = "Air and clarity",
                            accent = Color(0xFF7D8BFF),
                            value = settings.treble,
                            onValueChange = onTrebleChanged,
                        )
                        EqMacroSliderCard(
                            title = "Spaciousness",
                            subtitle = "Width without hollow mids",
                            accent = Color(0xFF6DE0B5),
                            value = settings.spaciousness,
                            onValueChange = onSpaciousnessChanged,
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = "Equalizer",
            onBack = onBack,
            centeredTitle = true,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    textSizePreset: TextSizePreset,
    eqSettings: EqSettings,
    libraryFolderPath: String,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onTextSizePresetSelected: (TextSizePreset) -> Unit,
    onBassChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onChangeLibraryFolder: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            overscrollEffect = null,
            modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionTitleRow(
                                title = "Text size",
                                compact = true,
                            )
                            TextSizeStepper(
                                selectedPreset = textSizePreset,
                                onPresetSelected = onTextSizePresetSelected,
                            )
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
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        SectionTitleRow(
                            title = "Sound shaping",
                            compact = true,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                            modifier = Modifier.fillMaxWidth(),
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
                                Text("Open equalizer")
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = "Others",
                    iconResId = R.drawable.ic_lucide_library,
                )
            }

            item {
                ModuleCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SectionTitleRow(
                                title = "Default media folder",
                                compact = true,
                            )
                            Text(
                                text = libraryFolderPath,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Surface(
                            onClick = onChangeLibraryFolder,
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text(
                                text = "Change",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            overscrollEffect = null,
            modifier = Modifier.fillMaxSize(),
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
                        .height(236.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
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
                    ModuleCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            release?.changes
                                ?.filter { it.isNotBlank() }
                                ?.forEach { change ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)),
                                        )
                                        Text(
                                            text = change,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                                ?: Text(
                                    text = "No changelog entries yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = readableSecondaryTextColor(),
                                )
                        }
                    }
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
private fun UpdateAvailableBanner(
    release: AppReleaseInfo,
    uiState: AppUpdateUiState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
) {
    ModuleCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Update available",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = "Version ${release.versionName} is ready to install",
                        style = MaterialTheme.typography.bodyLarge,
                        color = readableSecondaryTextColor(),
                    )
                }
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_down,
                    contentDescription = "Dismiss update",
                    showBackground = false,
                    onClick = onDismiss,
                    modifier = Modifier.rotate(90f),
                )
            }
            if (uiState.isDownloading) {
                LinearProgressIndicator(
                    progress = { uiState.downloadProgress ?: 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(
                    onClick = onUpdate,
                    shape = RoundedCornerShape(ElovaireRadii.pill),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = when {
                            uiState.isInstalling -> "Installing"
                            uiState.isDownloading -> {
                                val percent = ((uiState.downloadProgress ?: 0f) * 100f).roundToInt()
                                "Downloading $percent%"
                            }
                            else -> "Update"
                        },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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
) {
    val presets = TextSizePreset.entries
    val currentSelectedPreset by rememberUpdatedState(selectedPreset)
    val currentOnPresetSelected by rememberUpdatedState(onPresetSelected)
    val selectedIndex = presets.indexOf(selectedPreset).coerceAtLeast(0)
    val maxIndex = (presets.size - 1).coerceAtLeast(1)
    val knobSize = 20.dp
    val dotColor = MaterialTheme.colorScheme.onSurface
    var isDragging by remember { mutableStateOf(false) }
    var dragCenterPx by remember { mutableFloatStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
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
                        .background(Color.White.copy(alpha = 0.2f)),
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
                        .background(Color.White)
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
private fun ThemeModeSegmentedPicker(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)
    BoxWithConstraints(
        modifier = modifier
            .height(46.dp)
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
        val indicatorColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            Color.White
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        }

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
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
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
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
    val arcAlpha = 0.3f + (animatedValue * 0.7f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        dragValue = (dragValue + (dragAmount / 120f)).coerceIn(0f, 1f)
                        onValueChange(dragValue)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        dragValue = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValueChange(dragValue)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val inset = strokeWidth / 2f + 8.dp.toPx()
                val arcSize = size.minDimension - inset * 2f
                val arcTopLeft = Offset(inset, inset)
                val arcCenter = Offset(
                    x = arcTopLeft.x + (arcSize / 2f),
                    y = arcTopLeft.y + (arcSize / 2f),
                )
                val radius = arcSize / 2f
                val tipRadius = strokeWidth / 2f
                val tipOrbitRadius = radius
                val startAngle = 140f
                val sweepAngle = 260f
                val activeSweep = sweepAngle * animatedValue

                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = arcAlpha),
                        ),
                        center = arcCenter,
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )

                if (animatedValue > 0.001f) {
                    val arcPath = android.graphics.Path().apply {
                        addArc(
                            android.graphics.RectF(
                                arcTopLeft.x,
                                arcTopLeft.y,
                                arcTopLeft.x + arcSize,
                                arcTopLeft.y + arcSize,
                            ),
                            startAngle,
                            activeSweep,
                        )
                    }
                    val pathMeasure = android.graphics.PathMeasure(arcPath, false)
                    val position = FloatArray(2)
                    pathMeasure.getPosTan(pathMeasure.length, position, null)
                    val tipCenter = Offset(position[0], position[1])
                    drawCircle(
                        color = Color.White,
                        radius = tipRadius,
                        center = tipCenter,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${(animatedValue * 100f).roundToInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(24f)),
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
private fun EqMacroSliderCard(
    title: String,
    subtitle: String,
    accent: Color,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -1f..1f,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accent,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun EqResponseGraph(
    settings: EqSettings,
    onBandChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bandValues = settings.bands.ifEmpty { List(16) { 0f } }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .background(readableCardSurfaceColor().copy(alpha = 0.72f))
            .pointerInput(bandValues) {
                detectTapGestures { offset ->
                    val lastIndex = bandValues.lastIndex.coerceAtLeast(1)
                    val index = ((offset.x / size.width.toFloat()) * lastIndex)
                        .roundToInt()
                        .coerceIn(0, bandValues.lastIndex)
                    val normalized = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                    onBandChanged(index, ((normalized * 2f) - 1f).coerceIn(-1f, 1f))
                }
            }
            .pointerInput(bandValues) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val lastIndex = bandValues.lastIndex.coerceAtLeast(1)
                        val index = ((offset.x / size.width.toFloat()) * lastIndex)
                            .roundToInt()
                            .coerceIn(0, bandValues.lastIndex)
                        val normalized = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onBandChanged(index, ((normalized * 2f) - 1f).coerceIn(-1f, 1f))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val lastIndex = bandValues.lastIndex.coerceAtLeast(1)
                        val index = ((change.position.x / size.width.toFloat()) * lastIndex)
                            .roundToInt()
                            .coerceIn(0, bandValues.lastIndex)
                        val normalized = (1f - (change.position.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onBandChanged(index, ((normalized * 2f) - 1f).coerceIn(-1f, 1f))
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val midY = size.height / 2f
            repeat(7) { step ->
                val y = size.height * (step / 6f)
                drawLine(
                    color = Color.White.copy(alpha = if (step == 3) 0.12f else 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            repeat(bandValues.lastIndex) { index ->
                val x = size.width * (index / bandValues.lastIndex.coerceAtLeast(1).toFloat())
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val points = bandValues.mapIndexed { index, band ->
                val x = size.width * (index / bandValues.lastIndex.coerceAtLeast(1).toFloat())
                val y = midY - (band.coerceIn(-1f, 1f) * (size.height * 0.34f))
                Offset(x, y)
            }

            val strokePath = androidx.compose.ui.graphics.Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        moveTo(point.x, point.y)
                    } else {
                        lineTo(point.x, point.y)
                    }
                }
            }
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, midY)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, midY)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7D8BFF).copy(alpha = 0.34f),
                        Color(0xFF7D8BFF).copy(alpha = 0.08f),
                    ),
                ),
            )
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF6F61),
                        Color(0xFFB05CFF),
                        Color(0xFF57D4FF),
                        Color(0xFF6DE0B5),
                    ),
                ),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            points.forEachIndexed { index, point ->
                if (index % 2 == 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.94f),
                        radius = 4.dp.toPx(),
                        center = point,
                    )
                }
            }
        }
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
    return if (played.isNotEmpty()) played.take(6) else libraryState.albums.take(6)
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

private fun activeLyricLineIndex(
    lines: List<LyricsLine>,
    isSynced: Boolean,
    positionMs: Long,
    durationMs: Long,
): Int {
    if (lines.isEmpty()) return -1

    if (isSynced) {
        val timedLines = lines.mapIndexedNotNull { index, line ->
            line.startTimeMs?.let { startTime -> index to startTime }
        }
        if (timedLines.isEmpty()) return -1
        val firstLineLeadMs = 100L
        if (positionMs + firstLineLeadMs < timedLines.first().second) return -1

        var activeIndex = timedLines.first().first
        timedLines.forEachIndexed { timedIndex, (index, startTime) ->
            val nextStartTime = timedLines.getOrNull(timedIndex + 1)?.second ?: durationMs
            val gapMs = (nextStartTime - startTime).coerceAtLeast(0L)
            val adaptiveLeadMs = when {
                gapMs <= 700L -> 20L
                gapMs <= 1200L -> 55L
                gapMs <= 2200L -> 85L
                else -> 100L
            }
            if (positionMs + adaptiveLeadMs >= startTime) {
                activeIndex = index
            } else {
                return@forEachIndexed
            }
        }
        return activeIndex.coerceIn(0, lines.lastIndex)
    }

    if (durationMs <= 0L) return -1
    val progress = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    return (progress * lines.lastIndex).roundToInt().coerceIn(0, lines.lastIndex)
}

private fun lyricsSeekPositionMs(
    lines: List<LyricsLine>,
    index: Int,
    isSynced: Boolean,
    durationMs: Long,
): Long? {
    if (lines.isEmpty() || index !in lines.indices) return null

    if (isSynced) {
        return lines[index].startTimeMs?.coerceAtLeast(0L)
    }

    if (durationMs <= 0L) return null
    val fraction = if (lines.size == 1) 0f else index.toFloat() / lines.lastIndex.toFloat()
    return (durationMs * fraction).roundToInt().toLong().coerceAtLeast(0L)
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

private fun String?.isExpandFromTileRoute(): Boolean {
    return this == "$ALBUM_ROUTE/{albumId}" || this == "$PLAYLIST_ROUTE/{playlistId}"
}

private fun ExpandOrigin.toTransformOrigin(): TransformOrigin {
    return TransformOrigin(
        pivotFractionX = xFraction.coerceIn(0f, 1f),
        pivotFractionY = yFraction.coerceIn(0f, 1f),
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
