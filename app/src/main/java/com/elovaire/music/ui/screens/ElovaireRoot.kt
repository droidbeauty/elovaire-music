package elovaire.music.droidbeauty.app.ui.screens

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Xml
import java.io.File
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
import androidx.compose.animation.animateContentSize
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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import elovaire.music.droidbeauty.app.BuildConfig
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshotFlow
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
import androidx.navigation.NavBackStackEntry
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
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRepository
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.tags.AlbumTagMatchSuggestion
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspConfig
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspModel
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import elovaire.music.droidbeauty.app.data.update.AppReleaseInfo
import elovaire.music.droidbeauty.app.data.update.AppUpdateUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.SyncElovaireMotionScale
import elovaire.music.droidbeauty.app.ui.motion.rememberSystemAnimationScale
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorScreen
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.AboutCardButtonAccent
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import elovaire.music.droidbeauty.app.ui.theme.rememberElovaireOverscrollFactory
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.ToggleEnabledGreen
import java.net.URL
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal val aboutLogoImageCache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

private data class SongMenuActions(
    val playlists: List<Playlist> = emptyList(),
    val songsById: Map<Long, Song> = emptyMap(),
    val onAddToPlaylist: (playlistId: Long, song: Song) -> Unit = { _, _ -> },
    val onCreatePlaylist: (String) -> Long = { -1L },
    val onAddToQueue: (Song) -> Unit = {},
    val onDeleteFromLibrary: (Song) -> Unit = {},
    val deletePhrase: UiPhrase = UiPhrase.DeleteFromLibrary,
)

private data class PendingSongDeletion(
    val songs: List<Song>,
    val parentDirectories: Set<String> = emptySet(),
)

internal fun Context.loadAboutScreenModel(): AboutScreenModel {
    val parser = resources.getXml(R.xml.info_screen)
    val sections = mutableListOf<AboutSection>()
    var currentSectionTitle = ""
    var currentSectionDescription: String? = null
    var sectionOpen = false
    var currentEntries = mutableListOf<AboutEntry>()
    var currentEntryTitle: String? = null
    var currentEntryDescription: String? = null
    var currentEntryLogoUri: String? = null
    var currentLinks = mutableListOf<AboutLink>()

    fun closeEntry() {
        val entryTitle = currentEntryTitle
        if (!entryTitle.isNullOrBlank()) {
            val entry = AboutEntry(
                title = entryTitle,
                description = currentEntryDescription?.takeIf { it.isNotBlank() },
                logoUri = currentEntryLogoUri?.takeIf { it.isNotBlank() },
                links = currentLinks.toList(),
            )
            if (sectionOpen) {
                currentEntries += entry
            } else {
                sections += AboutSection(
                    title = entry.title,
                    description = null,
                    entries = listOf(entry),
                )
            }
        }
        currentEntryTitle = null
        currentEntryDescription = null
        currentEntryLogoUri = null
        currentLinks = mutableListOf()
    }

    fun closeSection() {
        closeEntry()
        if (sectionOpen && (currentSectionTitle.isNotBlank() || currentEntries.isNotEmpty())) {
            sections += AboutSection(
                title = currentSectionTitle,
                description = currentSectionDescription?.takeIf { it.isNotBlank() },
                entries = currentEntries.toList(),
            )
        }
        sectionOpen = false
        currentSectionTitle = ""
        currentSectionDescription = null
        currentEntries = mutableListOf()
    }

    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
            XmlPullParser.START_TAG -> when (parser.name) {
                "section" -> {
                    closeSection()
                    sectionOpen = true
                    currentSectionTitle = parser.getAttributeValue(null, "title").orEmpty()
                    currentSectionDescription = parser.getAttributeValue(null, "description")
                }

                "entry" -> {
                    closeEntry()
                    currentEntryTitle = parser.getAttributeValue(null, "title")
                    currentEntryDescription = parser.getAttributeValue(null, "description")
                    currentEntryLogoUri = parser.getAttributeValue(null, "logoUrl")
                        ?: parser.getAttributeValue(null, "logoUri")
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

private val LocalSongMenuActions = compositionLocalOf { SongMenuActions() }

private val LocalChromeHazeState = compositionLocalOf<HazeState?> { null }
private val LocalPlayerHazeState = compositionLocalOf<HazeState?> { null }
private val LocalUseSharedTopBarBackdrop = compositionLocalOf { false }
private val LocalSharedTopBarController = compositionLocalOf<SharedTopBarController?> { null }
private val LocalRenderSharedTopBarContent = compositionLocalOf { false }
private val LocalSharedBackIconPainter = compositionLocalOf<Painter?> { null }
private val LocalSharedTopMenuIconPainter = compositionLocalOf<Painter?> { null }
internal val LocalAppLanguage = compositionLocalOf { AppLanguage.English }

private data class TopBarActionSpec(
    @DrawableRes val iconResId: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
)

private fun tileExpandEnterTransition(expandOrigin: ExpandOrigin): EnterTransition {
    return fadeIn(
        animationSpec = ElovaireMotion.fadeMedium(),
    ) +
        scaleIn(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            initialScale = 0.8f,
            transformOrigin = expandOrigin.toTransformOrigin(),
        ) +
        slideInHorizontally(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            initialOffsetX = { fullWidth ->
                ((expandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
            },
        ) +
        slideInVertically(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            initialOffsetY = { fullHeight ->
                ((expandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
            },
        )
}

private fun tileExpandExitTransition(expandOrigin: ExpandOrigin): ExitTransition {
    return fadeOut(
        animationSpec = ElovaireMotion.fadeFast(),
    ) +
        scaleOut(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            targetScale = 0.84f,
            transformOrigin = expandOrigin.toTransformOrigin(),
        ) +
        slideOutHorizontally(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            targetOffsetX = { fullWidth ->
                ((expandOrigin.xFraction - 0.5f) * fullWidth * 0.2f).roundToInt()
            },
        ) +
        slideOutVertically(
            animationSpec = ElovaireMotion.emphasizedEnterSpec(),
            targetOffsetY = { fullHeight ->
                ((expandOrigin.yFraction - 0.5f) * fullHeight * 0.2f).roundToInt()
            },
        )
}

private fun resolveForwardEnterTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
): EnterTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.targetUsesTileExpand -> tileExpandEnterTransition(expandOrigin)
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelEnter(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetUsesDetailTransition -> ElovaireMotion.detailForwardEnter()
    else -> ElovaireMotion.fullScreenForwardEnter()
}

private fun resolveForwardExitTransition(
    transition: NavHostTransitionResolution,
): ExitTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.targetUsesTileExpand -> fadeOut(animationSpec = ElovaireMotion.fadeFast())
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelExit(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetUsesDetailTransition -> ElovaireMotion.detailForwardExit()
    else -> ElovaireMotion.fullScreenForwardExit()
}

private fun resolvePopEnterTransition(
    transition: NavHostTransitionResolution,
): EnterTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.initialUsesTileExpand -> fadeIn(animationSpec = ElovaireMotion.fadeMedium())
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelEnter(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetUsesDetailTransition -> ElovaireMotion.detailBackEnter()
    else -> ElovaireMotion.fullScreenBackEnter()
}

private fun resolvePopExitTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
): ExitTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.initialUsesTileExpand -> tileExpandExitTransition(expandOrigin)
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelExit(
        forward = transition.topLevelTransition.isForward,
    )
    transition.initialUsesDetailTransition -> ElovaireMotion.detailBackExit()
    else -> ElovaireMotion.fullScreenBackExit()
}

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
        val actions: List<TopBarActionSpec> = emptyList(),
    ) : SharedTopBarSpec
}

private fun SharedTopBarSpec.visualSignature(): String {
    return when (this) {
        is SharedTopBarSpec.Unified -> "unified|$showSettings|${supplementalActionIconResId ?: 0}|${supplementalActionContentDescription.orEmpty()}"
        is SharedTopBarSpec.Back -> "back|$title|$centeredTitle"
        is SharedTopBarSpec.Detail -> "detail|$title|${subtitle.orEmpty()}|${actions.joinToString { "${it.iconResId}:${it.contentDescription}" }}"
    }
}

private data class SharedTopBarRegistration(
    val id: Any,
    val spec: SharedTopBarSpec,
)

internal data class AboutScreenModel(
    val sections: List<AboutSection>,
)

internal data class AboutSection(
    val title: String,
    val description: String?,
    val entries: List<AboutEntry>,
)

internal data class AboutEntry(
    val title: String,
    val description: String?,
    val logoUri: String?,
    val links: List<AboutLink>,
)

internal data class AboutLink(
    val label: String,
    val url: String,
)

private class SharedTopBarController {
    var registration by mutableStateOf<SharedTopBarRegistration?>(null)
}

@Composable
private fun statusBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.statusBars.getTop(this).toDp() }
}

@Composable
internal fun navigationBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
}

@Composable
private fun buttonNavigationScrollBoost(): Dp {
    val navigationInset = navigationBarInsetDp()
    return if (navigationInset >= 28.dp) {
        (navigationInset - 16.dp).coerceAtLeast(0.dp)
    } else {
        0.dp
    }
}

@Composable
private fun screenContainerSizePx(): androidx.compose.ui.unit.IntSize {
    return LocalWindowInfo.current.containerSize
}

@Composable
private fun topBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.topBarContentHeight

@Composable
internal fun detailTopBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.detailTopBarContentHeight

@Composable
private fun sharedTopBarOccupiedHeight(): Dp =
    statusBarInsetDp() + maxOf(ElovaireSpacing.topBarContentHeight, ElovaireSpacing.detailTopBarContentHeight)

@Composable
private fun bottomNavigationOccupiedHeight(): Dp {
    return navigationBarInsetDp() + ElovaireSpacing.bottomNavigationBodyHeight
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
internal fun rememberElovaireLazyListState(vararg inputs: Any?): LazyListState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) {
        lazyListPositionCache[cacheKey] ?: (0 to 0)
    }
    val state = rememberSaveable(cacheKey, saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = cachedPosition.first,
            firstVisibleItemScrollOffset = cachedPosition.second,
        )
    }
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { position ->
                lazyListPositionCache[cacheKey] = position
            }
    }
    return state
}

private fun querySongParentDirectories(
    context: Context,
    songs: List<Song>,
): Set<String> {
    val contentResolver = context.contentResolver
    return songs.asSequence()
        .mapNotNull { song ->
            when (song.uri.scheme) {
                "file" -> song.uri.path
                else -> runCatching {
                    contentResolver.query(
                        song.uri,
                        arrayOf(MediaStore.MediaColumns.DATA),
                        null,
                        null,
                        null,
                    )?.use { cursor ->
                        if (!cursor.moveToFirst()) {
                            null
                        } else {
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                        }
                    }
                }.getOrNull()
            }
        }
        .mapNotNull { path -> File(path).parentFile?.absolutePath }
        .toSet()
}

private fun cleanupEmptyDirectories(paths: Set<String>) {
    paths.asSequence()
        .map(::File)
        .filter { file -> file.exists() && file.isDirectory }
        .sortedByDescending { file -> file.absolutePath.length }
        .forEach { directory ->
            runCatching {
                if (directory.listFiles().isNullOrEmpty()) {
                    directory.delete()
                }
            }
        }
}

private fun Set<Long>.toggleSelection(id: Long): Set<Long> {
    return if (id in this) this - id else this + id
}

private fun clearTopLevelScrollPositionMemory(route: String) {
    val prefixes = topLevelScrollCachePrefixes[route].orEmpty()
    if (prefixes.isEmpty()) return
    lazyListPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
    lazyGridPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
    scrollPositionCache.keys.removeIf { cacheKey ->
        prefixes.any { prefix -> cacheKey.contains(prefix) }
    }
}

@Composable
private fun rememberElovaireLazyGridState(vararg inputs: Any?): LazyGridState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) {
        lazyGridPositionCache[cacheKey] ?: (0 to 0)
    }
    val state = rememberSaveable(cacheKey, saver = LazyGridState.Saver) {
        LazyGridState(
            firstVisibleItemIndex = cachedPosition.first,
            firstVisibleItemScrollOffset = cachedPosition.second,
        )
    }
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { position ->
                lazyGridPositionCache[cacheKey] = position
            }
    }
    return state
}

@Composable
private fun rememberElovaireScrollState(vararg inputs: Any?): androidx.compose.foundation.ScrollState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) { scrollPositionCache[cacheKey] ?: 0 }
    val state = rememberScrollState(cachedPosition)
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.value }
            .distinctUntilChanged()
            .collect { value ->
                scrollPositionCache[cacheKey] = value
            }
    }
    return state
}

@Composable
private fun Modifier.elovairePressBounce(
    interactionSource: MutableInteractionSource,
    label: String,
    pressedScale: Float = 0.9f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) {
            ElovaireMotion.pressDownSpec()
        } else {
            ElovaireMotion.bounceSpringSpec()
        },
        label = label,
    )
    return this.scale(scale)
}

@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun DynamicBackdropSurface(
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
            is SharedTopBarSpec.Unified -> "unified|${spec.showSettings}|${spec.supplementalActionIconResId ?: 0}|${spec.supplementalActionContentDescription.orEmpty()}"
            is SharedTopBarSpec.Back -> "back|${spec.title}|${spec.centeredTitle}"
            is SharedTopBarSpec.Detail -> "detail|${spec.title}|${spec.subtitle.orEmpty()}|${spec.actions.joinToString { "${it.iconResId}:${it.contentDescription}" }}"
        }
    }
    LaunchedEffect(controller, registrationId, specSignature, spec) {
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
    SyncElovaireMotionScale()
    val libraryContentState by container.libraryRepository.contentState.collectAsStateWithLifecycle()
    val libraryScanState by container.libraryRepository.scanState.collectAsStateWithLifecycle()
    val playbackNowPlayingState by container.playbackManager.nowPlayingState.collectAsStateWithLifecycle()
    val playbackTransportState by container.playbackManager.transportState.collectAsStateWithLifecycle()
    val playbackQueueState by container.playbackManager.queueState.collectAsStateWithLifecycle()
    val playbackVolumeState by container.playbackManager.volumeState.collectAsStateWithLifecycle()
    val recentPlaybackState by container.playbackManager.recentPlaybackState.collectAsStateWithLifecycle()
    val eqSettings by container.preferenceStore.eqSettings.collectAsStateWithLifecycle()
    val themeMode by container.preferenceStore.themeMode.collectAsStateWithLifecycle()
    val textSizePreset by container.preferenceStore.textSizePreset.collectAsStateWithLifecycle()
    val appLanguage by container.preferenceStore.appLanguage.collectAsStateWithLifecycle()
    val searchHistory by container.preferenceStore.searchHistory.collectAsStateWithLifecycle()
    val playlists by container.preferenceStore.playlists.collectAsStateWithLifecycle()
    val favoriteSongIds by container.preferenceStore.favoriteSongIds.collectAsStateWithLifecycle()
    val favoriteSongIdSet = remember(favoriteSongIds) { favoriteSongIds.toHashSet() }
    val albumPlayCounts by container.preferenceStore.albumPlayCounts.collectAsStateWithLifecycle()
    val songPlayCounts by container.preferenceStore.songPlayCounts.collectAsStateWithLifecycle()
    val albumCollectionLayoutModeName by container.preferenceStore.albumCollectionLayoutMode.collectAsStateWithLifecycle()
    val songCollectionGridEnabled by container.preferenceStore.songCollectionGridEnabled.collectAsStateWithLifecycle()
    val albumCollectionSortModeName by container.preferenceStore.albumCollectionSortMode.collectAsStateWithLifecycle()
    val songCollectionSortModeName by container.preferenceStore.songCollectionSortMode.collectAsStateWithLifecycle()
    val appUpdateState by container.appUpdateManager.uiState.collectAsStateWithLifecycle()
    val albumCollectionLayoutMode = albumCollectionLayoutModeName.toAlbumLayoutMode()
    val changelogReleases = remember(context) { ChangelogRepository(context).loadReleases() }
    val rootScope = rememberCoroutineScope()
    val libraryState = remember(libraryContentState, libraryScanState) {
        libraryUiStateOf(libraryContentState, libraryScanState)
    }
    val playbackState = remember(
        playbackNowPlayingState,
        playbackTransportState,
        playbackQueueState,
        playbackVolumeState,
        recentPlaybackState,
    ) {
        playbackUiStateOf(
            nowPlaying = playbackNowPlayingState,
            transport = playbackTransportState,
            queue = playbackQueueState,
            volume = playbackVolumeState,
            recent = recentPlaybackState,
        )
    }
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
    var pendingSongDeletion by remember { mutableStateOf<PendingSongDeletion?>(null) }
    val songsById = remember(libraryState.songs) { libraryState.songs.associateBy { it.id } }
    val songsByAlbumId = remember(libraryState.songs) { libraryState.songs.groupBy { it.albumId } }
    val albumsById = remember(libraryState.albums) { libraryState.albums.associateBy { it.id } }

    val recentlyAddedAlbums = remember(libraryState.albums) {
        recentlyAddedAlbumsFor(libraryState)
    }
    val recentAlbums = remember(libraryState.albums, playbackState.recentAlbumIds) {
        recentAlbumsFor(libraryState, playbackState)
    }
    val playlistsById = remember(playlists) { playlists.associateBy { it.id } }
    val lastPlayedPlaylist = remember(
        playlistsById,
        playbackState.lastPlayedCollectionKind,
        playbackState.lastPlayedCollectionId,
    ) {
        if (playbackState.lastPlayedCollectionKind == PlaybackCollectionKind.Playlist) {
            playbackState.lastPlayedCollectionId?.let(playlistsById::get)
        } else {
            null
        }
    }
    val lastPlayedAlbum = remember(
        albumsById,
        recentAlbums,
        playbackState.lastPlayedCollectionKind,
        playbackState.lastPlayedCollectionId,
    ) {
        when (playbackState.lastPlayedCollectionKind) {
            PlaybackCollectionKind.Album -> playbackState.lastPlayedCollectionId?.let(albumsById::get)
            PlaybackCollectionKind.Playlist -> null
            null -> recentAlbums.firstOrNull()
        } ?: recentAlbums.firstOrNull()
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
    suspend fun completeSongDeletion(
        songs: List<Song>,
        parentDirectories: Set<String>,
    ) {
        songs.forEach { song ->
            container.preferenceStore.removeSongReferences(song.id)
        }
        withContext(Dispatchers.IO) {
            cleanupEmptyDirectories(parentDirectories)
        }
        container.libraryRepository.refresh(
            forceMediaIndex = true,
            showLoadingIndicator = false,
        )
    }
    val deleteSongLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val pendingDeletion = pendingSongDeletion ?: return@rememberLauncherForActivityResult
        pendingSongDeletion = null
        if (result.resultCode == Activity.RESULT_OK) {
            rootScope.launch {
                completeSongDeletion(
                    songs = pendingDeletion.songs,
                    parentDirectories = pendingDeletion.parentDirectories,
                )
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

    val topLevelDestinations = DefaultTopLevelDestinations

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentConcreteRoute = currentBackStackEntry?.concreteNavigationRoute() ?: currentRoute
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
    var lastHomeTabRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var lastLibraryTabRoute by rememberSaveable { mutableStateOf(ALBUMS_ROUTE) }
    var lastPlaylistsTabRoute by rememberSaveable { mutableStateOf(PLAYLISTS_ROUTE) }
    var lastSearchTabRoute by rememberSaveable { mutableStateOf(SEARCH_ROUTE) }
    val routeOwnerOverrides = remember { mutableStateMapOf<String, String>() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchFieldFocused by rememberSaveable { mutableStateOf(false) }
    var searchAllSongsVisible by rememberSaveable { mutableStateOf(false) }
    var searchSongSortMode by rememberSaveable { mutableStateOf(SearchSongSortMode.Title) }
    var searchSongSortOptionsVisible by rememberSaveable { mutableStateOf(false) }
    var homeScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var libraryScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var playlistsScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var searchScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    val showTopLevelChrome = currentRoute in TopLevelRoutes
    val showBottomNavigation = currentRoute in BottomNavigationRoutes
    LaunchedEffect(currentRoute) {
        if (currentRoute in TopLevelRoutes) {
            browsingOriginRoute = currentRoute.orEmpty()
            selectedBottomRoute = currentRoute.orEmpty()
        }
    }
    LaunchedEffect(currentBackStackEntry, browsingOriginRoute, currentConcreteRoute) {
        val concreteRoute = currentBackStackEntry?.elovaireConcreteRoute() ?: return@LaunchedEffect
        val normalizedConcreteRoute = concreteRoute.normalizedNavigationRoute()
        val ownerRoute = when (concreteRoute.normalizedNavigationRoute()) {
            HOME_ROUTE -> HOME_ROUTE
            SEARCH_ROUTE -> SEARCH_ROUTE
            ALBUMS_ROUTE -> ALBUMS_ROUTE
            PLAYLISTS_ROUTE -> PLAYLISTS_ROUTE
            "$LIBRARY_COLLECTION_ROUTE/{kind}",
            "$GENRE_ROUTE/{genre}",
            "$ARTIST_ROUTE/{artistName}",
            -> ALBUMS_ROUTE

            "$PLAYLIST_ROUTE/{playlistId}" -> PLAYLISTS_ROUTE
            "$ALBUM_ROUTE/{albumId}" -> {
                routeOwnerOverrides[concreteRoute]
                    ?: navController.previousBackStackEntry?.concreteNavigationRoute()?.let(routeOwnerOverrides::get)
                    ?: topLevelOwnerRoute(
                        navController.previousBackStackEntry?.destination?.route,
                        browsingOriginRoute,
                    )
                    ?: browsingOriginRoute.takeIf { it in TopLevelRoutes }
                    ?: selectedBottomRoute
            }

            else -> topLevelOwnerRoute(currentRoute, browsingOriginRoute) ?: selectedBottomRoute
        }
        if (ownerRoute in TopLevelRoutes) {
            routeOwnerOverrides[concreteRoute] = ownerRoute
        }
        if (concreteRoute in setOf(PLAYER_ROUTE, SETTINGS_ROUTE, EQUALIZER_ROUTE, CHANGELOG_ROUTE, ABOUT_ROUTE)) {
            return@LaunchedEffect
        }
        if (normalizedConcreteRoute == "$ALBUM_TAG_EDITOR_ROUTE/{albumId}") {
            return@LaunchedEffect
        }
        if (normalizedConcreteRoute in setOf("$ALBUM_ROUTE/{albumId}", "$PLAYLIST_ROUTE/{playlistId}")) {
            return@LaunchedEffect
        }
        when (ownerRoute) {
            HOME_ROUTE -> lastHomeTabRoute = concreteRoute
            ALBUMS_ROUTE -> lastLibraryTabRoute = concreteRoute
            PLAYLISTS_ROUTE -> lastPlaylistsTabRoute = concreteRoute
            SEARCH_ROUTE -> lastSearchTabRoute = concreteRoute
        }
    }
    val activeBottomRoute = routeOwnerOverrides[currentConcreteRoute]
        ?: topLevelOwnerRoute(currentRoute, browsingOriginRoute)
        ?: selectedBottomRoute
    val resetTopLevelTabState: (String) -> Unit = { route ->
        clearTopLevelScrollPositionMemory(route)
        when (route) {
            HOME_ROUTE -> {
                lastHomeTabRoute = HOME_ROUTE
                homeScrollRequestVersion += 1L
            }
            ALBUMS_ROUTE -> {
                lastLibraryTabRoute = ALBUMS_ROUTE
                libraryScrollRequestVersion += 1L
            }
            PLAYLISTS_ROUTE -> {
                lastPlaylistsTabRoute = PLAYLISTS_ROUTE
                playlistsScrollRequestVersion += 1L
            }
            SEARCH_ROUTE -> {
                lastSearchTabRoute = SEARCH_ROUTE
                searchScrollRequestVersion += 1L
            }
        }
    }
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
                title = topBarTitle(currentRoute, appLanguage),
                showSettings = currentRoute in setOf(HOME_ROUTE, ALBUMS_ROUTE, PLAYLISTS_ROUTE),
                supplementalActionIconResId = if (showPlaylistCreateAction) R.drawable.ic_lucide_plus else null,
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
    LaunchedEffect(container) {
        container.openPlayerRequests.collect {
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

    val deleteSongsFromDevice = remember(context, rootScope, deleteSongLauncher) {
        { songs: List<Song> ->
            val uniqueSongs = songs.distinctBy { it.id }
            if (uniqueSongs.isNotEmpty()) {
                rootScope.launch {
                    val parentDirectories = querySongParentDirectories(context, uniqueSongs)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pendingSongDeletion = PendingSongDeletion(
                            songs = uniqueSongs,
                            parentDirectories = parentDirectories,
                        )
                        deleteSongLauncher.launch(
                            IntentSenderRequest.Builder(
                                MediaStore.createDeleteRequest(
                                    context.contentResolver,
                                    uniqueSongs.map(Song::uri),
                                ).intentSender,
                            ).build(),
                        )
                    } else {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                uniqueSongs.forEach { song ->
                                    context.contentResolver.delete(song.uri, null, null)
                                }
                            }
                        }.onSuccess {
                            completeSongDeletion(
                                songs = uniqueSongs,
                                parentDirectories = parentDirectories,
                            )
                        }.onFailure { throwable ->
                            val intentSender = when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && throwable is RecoverableSecurityException -> {
                                    throwable.userAction.actionIntent.intentSender
                                }
                                else -> null
                            }
                            if (intentSender != null) {
                                pendingSongDeletion = PendingSongDeletion(
                                    songs = uniqueSongs,
                                    parentDirectories = parentDirectories,
                                )
                                deleteSongLauncher.launch(
                                    IntentSenderRequest.Builder(intentSender).build(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    val songMenuActions = remember(playlists, songsById, deleteSongsFromDevice) {
        SongMenuActions(
            playlists = playlists.filterNot { it.isSystem },
            songsById = songsById,
            onAddToPlaylist = { playlistId, song ->
                container.preferenceStore.addSongsToPlaylist(playlistId, listOf(song.id))
            },
            onCreatePlaylist = container.preferenceStore::createPlaylist,
            onAddToQueue = { song ->
                container.playbackManager.enqueueSong(song)
            },
            onDeleteFromLibrary = { song ->
                deleteSongsFromDevice(listOf(song))
            },
        )
    }
    val createPlaylistAndAddSongs = remember(container.preferenceStore) {
        { name: String, songIds: List<Long> ->
            val createdId = container.preferenceStore.createPlaylist(name)
            if (songIds.isNotEmpty()) {
                container.preferenceStore.addSongsToPlaylist(createdId, songIds)
            }
            createdId
        }
    }
    val deleteAlbumFromDevice = remember(deleteSongsFromDevice) {
        { album: Album ->
            deleteSongsFromDevice(album.songs)
        }
    }

    CompositionLocalProvider(
        LocalOverscrollFactory provides overscrollFactory,
        LocalSongMenuActions provides songMenuActions,
        LocalChromeHazeState provides chromeHazeState,
        LocalSharedBackIconPainter provides sharedBackIconPainter,
        LocalSharedTopMenuIconPainter provides sharedTopMenuIconPainter,
        LocalAppLanguage provides appLanguage,
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
                            resolveForwardEnterTransition(
                                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                                    initialRoute = initialState.destination.route,
                                    targetRoute = targetState.destination.route,
                                    initialFallbackTopLevelRoute = browsingOriginRoute,
                                    targetFallbackTopLevelRoute = selectedBottomRoute,
                                    detailRouteTransitionMode = detailRouteTransitionMode,
                                ),
                                expandOrigin = detailExpandOrigin,
                            )
                        },
                        exitTransition = {
                            resolveForwardExitTransition(
                                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                                    initialRoute = initialState.destination.route,
                                    targetRoute = targetState.destination.route,
                                    initialFallbackTopLevelRoute = browsingOriginRoute,
                                    targetFallbackTopLevelRoute = selectedBottomRoute,
                                    detailRouteTransitionMode = detailRouteTransitionMode,
                                ),
                            )
                        },
                        popEnterTransition = {
                            resolvePopEnterTransition(
                                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                                    initialRoute = initialState.destination.route,
                                    targetRoute = targetState.destination.route,
                                    initialFallbackTopLevelRoute = browsingOriginRoute,
                                    targetFallbackTopLevelRoute = selectedBottomRoute,
                                    detailRouteTransitionMode = detailRouteTransitionMode,
                                ),
                            )
                        },
                        popExitTransition = {
                            resolvePopExitTransition(
                                transition = ElovaireNavigationTransitions.resolveNavHostTransition(
                                    initialRoute = initialState.destination.route,
                                    targetRoute = targetState.destination.route,
                                    initialFallbackTopLevelRoute = browsingOriginRoute,
                                    targetFallbackTopLevelRoute = selectedBottomRoute,
                                    detailRouteTransitionMode = detailRouteTransitionMode,
                                ),
                                expandOrigin = detailExpandOrigin,
                            )
                        },
                ) {
                    composable(HOME_ROUTE) {
                        val recentSongs = remember(songsById, playbackState.recentSongIds) {
                            playbackState.recentSongIds.mapNotNull(songsById::get).take(5)
                        }
                        HomeScreen(
                            lastPlayedAlbum = lastPlayedAlbum,
                            lastPlayedPlaylist = lastPlayedPlaylist,
                            songsById = songsById,
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
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onPlaylistSelected = { playlist ->
                                detailExpandOrigin = ExpandOrigin()
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
                                navController.navigate("$PLAYLIST_ROUTE/${playlist.id}")
                            },
                            onPlayAlbum = { album ->
                                container.playbackManager.playAlbum(album)
                            },
                            onPlayPlaylist = { playlist, songs ->
                                songs.firstOrNull()?.let { firstSong ->
                                    container.playbackManager.playSong(
                                        song = firstSong,
                                        collection = songs,
                                        sourceLabel = playlist.name,
                                        sourcePlaylistId = playlist.id,
                                    )
                                    openPlayerIfAllowed(null)
                                }
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
                                openPlayerIfAllowed(null)
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
                            scrollToTopRequestVersion = libraryScrollRequestVersion,
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
                            scrollToTopRequestVersion = playlistsScrollRequestVersion,
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
                            scrollToTopRequestVersion = searchScrollRequestVersion,
                            query = searchQuery,
                            isSearchFieldFocused = searchFieldFocused,
                            showAllSongResults = searchAllSongsVisible,
                            searchSongSortMode = searchSongSortMode,
                            showSearchSongSortOptions = searchSongSortOptionsVisible,
                            onQueryChange = { searchQuery = it },
                            onSearchFieldFocusedChange = { searchFieldFocused = it },
                            onShowAllSongResultsChange = { searchAllSongsVisible = it },
                            onSearchSongSortModeChange = { searchSongSortMode = it },
                            onShowSearchSongSortOptionsChange = { searchSongSortOptionsVisible = it },
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
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
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
                                        sourcePlaylistId = playlist?.id,
                                    )
                                    openPlayerIfAllowed(null)
                                }
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = playlist?.name ?: queue.playbackSourceLabel(fallbackAlbum = song.album),
                                    sourcePlaylistId = playlist?.id,
                                )
                                openPlayerIfAllowed(null)
                            },
                            onAddSongs = { songIds ->
                                container.preferenceStore.addSongsToPlaylist(playlistId, songIds)
                            },
                            onUpdateSongOrder = { songIds ->
                                container.preferenceStore.updatePlaylistSongIds(playlistId, songIds)
                            },
                            onRenamePlaylist = container.preferenceStore::renamePlaylist,
                            onDeletePlaylist = { targetPlaylistId ->
                                container.preferenceStore.deletePlaylists(setOf(targetPlaylistId))
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
                            collapsedTopBarTitle = detailFallbackTitle(previousRoute, appLanguage),
                            onBack = navController::navigateUp,
                            onOpenTagEditor = { selectedAlbum ->
                                navController.navigate("$ALBUM_TAG_EDITOR_ROUTE/${selectedAlbum.id}")
                            },
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
                            playlists = playlists,
                            onAddSongsToPlaylist = { playlistId, songIds ->
                                container.preferenceStore.addSongsToPlaylist(playlistId, songIds)
                            },
                            onCreatePlaylist = container.preferenceStore::createPlaylist,
                            playlistSongsById = songsById,
                            onDeleteSongsFromDevice = deleteSongsFromDevice,
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                            onSetAlbumFavorite = { songIds, favorite ->
                                container.preferenceStore.setFavoriteSongs(songIds, favorite)
                            },
                        )
                    }

                    composable(
                        route = "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
                        val album = libraryState.albums.firstOrNull { it.id == albumId }
                        val tagEditorService = remember(context.applicationContext) {
                            AlbumTagEditorService(context.applicationContext)
                        }
                        val routeScope = rememberCoroutineScope()
                        var pendingWriteRequest by remember(albumId) { mutableStateOf<AlbumTagEditRequest?>(null) }
                        var pickedCoverArtUri by remember(albumId) { mutableStateOf<Uri?>(null) }
                        var autofillSuggestion by remember(albumId) { mutableStateOf<AlbumTagMatchSuggestion?>(null) }
                        var editorStatusMessage by rememberSaveable(albumId) { mutableStateOf<String?>(null) }
                        var isSavingTags by remember(albumId) { mutableStateOf(false) }
                        var isMatchingTags by remember(albumId) { mutableStateOf(false) }
                        var performAlbumTagSave by remember(albumId) {
                            mutableStateOf<(suspend (AlbumTagEditRequest) -> Unit)?>(null)
                        }

                        val albumTagWriteLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                        ) { result ->
                            val pendingRequest = pendingWriteRequest ?: return@rememberLauncherForActivityResult
                            pendingWriteRequest = null
                            if (result.resultCode == Activity.RESULT_OK) {
                                routeScope.launch {
                                    performAlbumTagSave?.invoke(pendingRequest)
                                }
                            }
                        }

                        val coverArtPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument(),
                        ) { uri ->
                            if (uri != null) {
                                pickedCoverArtUri = uri
                            }
                        }
                        performAlbumTagSave = { request ->
                            isSavingTags = true
                            editorStatusMessage = null
                            runCatching {
                                tagEditorService.applyEdits(request)
                            }.onSuccess {
                                container.libraryRepository.refresh(
                                    forceMediaIndex = true,
                                    enrichMetadata = true,
                                    showLoadingIndicator = false,
                                )
                                navController.navigateUp()
                            }.onFailure { throwable ->
                                val recoverableIntentSender = when {
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && throwable is RecoverableSecurityException -> {
                                        throwable.userAction.actionIntent.intentSender
                                    }
                                    else -> null
                                }
                                if (recoverableIntentSender != null) {
                                    pendingWriteRequest = request
                                    albumTagWriteLauncher.launch(
                                        IntentSenderRequest.Builder(recoverableIntentSender).build(),
                                    )
                                } else {
                                    editorStatusMessage = throwable.message ?: "Unable to save tags."
                                }
                            }
                            isSavingTags = false
                        }

                        AlbumTagEditorScreen(
                            album = album,
                            appLanguage = appLanguage,
                            isSaving = isSavingTags,
                            isMatching = isMatchingTags,
                            statusMessage = editorStatusMessage,
                            autofillSuggestion = autofillSuggestion,
                            pickedCoverArtUri = pickedCoverArtUri,
                            onBack = navController::navigateUp,
                            onSave = { request ->
                                routeScope.launch {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        pendingWriteRequest = request
                                        album?.songs
                                            ?.takeIf { it.isNotEmpty() }
                                            ?.let { songsToWrite ->
                                                albumTagWriteLauncher.launch(
                                                    IntentSenderRequest.Builder(
                                                        MediaStore.createWriteRequest(
                                                            context.contentResolver,
                                                            songsToWrite.map(Song::uri),
                                                        ).intentSender,
                                                    ).build(),
                                                )
                                            }
                                            ?: performAlbumTagSave?.invoke(request)
                                    } else {
                                        performAlbumTagSave?.invoke(request)
                                    }
                                }
                            },
                            onAutoMatch = {
                                val targetAlbum = album
                                if (targetAlbum != null) {
                                    routeScope.launch {
                                    isMatchingTags = true
                                    editorStatusMessage = null
                                    autofillSuggestion = runCatching {
                                        tagEditorService.findBestOnlineMatch(targetAlbum)
                                    }.onFailure { throwable ->
                                        editorStatusMessage = throwable.message ?: "Unable to match album online."
                                    }.getOrNull()
                                    if (autofillSuggestion == null && editorStatusMessage == null) {
                                        editorStatusMessage = "No close online match found."
                                    }
                                    isMatchingTags = false
                                }
                                }
                            },
                            onPickCoverArt = {
                                coverArtPickerLauncher.launch(arrayOf("image/*"))
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
                            playlists = playlists,
                            songPlayCounts = songPlayCounts,
                            favoriteSongIds = favoriteSongIdSet,
                            albumCollectionLayoutMode = albumCollectionLayoutMode,
                            songCollectionLayoutMode = if (songCollectionGridEnabled) AlbumLayoutMode.Grid else AlbumLayoutMode.Compact,
                            albumSortMode = albumCollectionSortModeName.toAlbumSortMode(),
                            songSortMode = songCollectionSortModeName.toSongSortMode(),
                            currentSongId = playbackState.currentSong?.id,
                            isCurrentSongPlaying = isPlaybackActuallyPlaying,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
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
                            onAddAlbumToPlaylist = { playlistId, album ->
                                container.preferenceStore.addSongsToPlaylist(
                                    playlistId,
                                    album.songs.map(Song::id),
                                )
                            },
                            onCreatePlaylist = container.preferenceStore::createPlaylist,
                            playlistSongsById = songsById,
                            onSetAlbumFavorite = { songIds, favorite ->
                                container.preferenceStore.setFavoriteSongs(songIds, favorite)
                            },
                            onDeleteAlbumFromDevice = deleteAlbumFromDevice,
                            onAlbumCollectionLayoutModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionLayoutMode(mode.name)
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
                            playlists = playlists,
                            layoutMode = albumCollectionLayoutMode,
                            sortMode = albumCollectionSortModeName.toAlbumSortMode(),
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onLayoutModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionLayoutMode(mode.name)
                            },
                            onSortModeChanged = { mode ->
                                container.preferenceStore.setAlbumCollectionSortMode(mode.name)
                            },
                            onAlbumSelected = { album, origin ->
                                detailExpandOrigin = origin
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
                                navController.navigate("$ALBUM_ROUTE/${album.id}")
                            },
                            onAddAlbumToPlaylist = { playlistId, album ->
                                container.preferenceStore.addSongsToPlaylist(playlistId, album.songs.map(Song::id))
                            },
                            onCreatePlaylist = container.preferenceStore::createPlaylist,
                            playlistSongsById = songsById,
                            favoriteSongIds = favoriteSongIdSet,
                            onSetAlbumFavorite = { songIds, favorite ->
                                container.preferenceStore.setFavoriteSongs(songIds, favorite)
                            },
                            onDeleteAlbumFromDevice = deleteAlbumFromDevice,
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
                                detailRouteTransitionMode = DetailRouteTransitionMode.Standard
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
                            onMidrangeChanged = container.preferenceStore::updateMidrange,
                            onTrebleChanged = container.preferenceStore::updateTreble,
                            onSpaciousnessChanged = container.preferenceStore::updateSpaciousness,
                            onSpaciousnessModeChanged = container.preferenceStore::updateSpaciousnessMode,
                            onReverbDurationChanged = container.preferenceStore::updateReverbDurationMs,
                            onReverbProfileChanged = container.preferenceStore::updateReverbProfile,
                            onResetReverb = {
                                container.preferenceStore.updateReverbDurationMs(0)
                                container.preferenceStore.updateReverbProfile(ReverbProfile.Dry)
                            },
                            onApplyPreset = container.preferenceStore::setEqSettings,
                            onReset = container.preferenceStore::resetEqSettings,
                        )
                    }

                    composable(SETTINGS_ROUTE) {
                        SettingsScreen(
                            themeMode = themeMode,
                            textSizePreset = textSizePreset,
                            appLanguage = appLanguage,
                            eqSettings = eqSettings,
                            bottomPadding = detailBottomPadding,
                            onBack = navController::navigateUp,
                            onThemeModeSelected = container.preferenceStore::setThemeMode,
                            onTextSizePresetSelected = container.preferenceStore::setTextSizePreset,
                            onAppLanguageSelected = container.preferenceStore::setAppLanguage,
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
                        enter = ElovaireMotion.overlayFadeEnter(initialAlpha = 0.86f),
                        exit = ElovaireMotion.overlayFadeExit(),
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
                        enter = ElovaireMotion.bottomSheetEnter(),
                        exit = ElovaireMotion.bottomSheetExit(),
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
                        visible = (showTopLevelChrome || currentRoute == SETTINGS_ROUTE) &&
                            appUpdateState.availableRelease != null,
                        enter = ElovaireMotion.bannerEnter(),
                        exit = ElovaireMotion.bannerExit(),
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
                        enter = ElovaireMotion.overlayFadeEnter(initialAlpha = 0.82f),
                        exit = ElovaireMotion.overlayFadeExit(targetAlpha = 0.96f),
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
                        ElovaireMotion.bottomBarEnter()
                    },
                    exit = ElovaireMotion.bottomBarExit(),
                    label = "BottomNavigationVisibility",
                ) {
                    BottomNavigationBar(
                        currentRoute = activeBottomRoute,
                        suppressEnterAnimation = reenteringFromPlayer,
                        destinations = topLevelDestinations,
                        onNavigate = { route ->
                            val currentTopLevelRoute = activeBottomRoute
                            browsingOriginRoute = route
                            selectedBottomRoute = route
                            routeOwnerOverrides[route] = route
                            if (route == currentTopLevelRoute) {
                                if (currentRoute != route) {
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
                                    resetTopLevelTabState(route)
                                } else {
                                    resetTopLevelTabState(route)
                                }
                            } else {
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
                        playlists = playlists.filterNot { it.isSystem },
                        lyricsService = lyricsService,
                        onBack = { isPlayerOverlayVisible = false },
                        onOpenCurrentAlbum = openCurrentPlayingAlbum,
                        onTogglePlayback = container.playbackManager::togglePlayback,
                        onSkipPrevious = container.playbackManager::skipPrevious,
                        onSkipNext = container.playbackManager::skipNext,
                        onCycleRepeatMode = container.playbackManager::cycleRepeatMode,
                        onToggleShuffle = container.playbackManager::toggleShuffle,
                        onToggleFavorite = { songId -> container.preferenceStore.toggleFavoriteSong(songId) },
                        onAddCurrentSongToPlaylist = { playlistId, song ->
                            container.preferenceStore.addSongsToPlaylist(playlistId, listOf(song.id))
                        },
                        onCreatePlaylist = container.preferenceStore::createPlaylist,
                        onQueueItemSelected = container.playbackManager::playQueueIndex,
                        onQueueItemRemoved = container.playbackManager::removeQueueIndex,
                        onOpenEqualizer = {
                            isPlayerOverlayVisible = false
                            navController.navigate(EQUALIZER_ROUTE)
                        },
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

private fun libraryUiStateOf(
    content: LibraryContentState,
    scan: LibraryScanState,
): LibraryUiState {
    return LibraryUiState(
        permissionGranted = scan.permissionGranted,
        isLoading = scan.isLoading,
        scanProgress = scan.scanProgress,
        songs = content.songs,
        albums = content.albums,
        errorMessage = scan.errorMessage,
    )
}

private fun playbackUiStateOf(
    nowPlaying: PlaybackNowPlayingState,
    transport: PlaybackTransportState,
    queue: PlaybackQueueState,
    volume: PlaybackVolumeState,
    recent: RecentPlaybackState,
): PlaybackUiState {
    return PlaybackUiState(
        queue = queue.queue,
        currentIndex = queue.currentIndex,
        isPlaying = transport.isPlaying,
        transportShowsPause = transport.transportShowsPause,
        repeatMode = transport.repeatMode,
        shuffleEnabled = transport.shuffleEnabled,
        sourceLabel = nowPlaying.sourceLabel,
        volume = volume.volume,
        audioSessionId = nowPlaying.audioSessionId,
        recentSongIds = recent.recentSongIds,
        recentAlbumIds = recent.recentAlbumIds,
        sourcePlaylistId = queue.sourcePlaylistId,
        lastPlayedCollectionKind = recent.lastPlayedCollectionKind,
        lastPlayedCollectionId = recent.lastPlayedCollectionId,
    )
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
    ElovaireAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = ElovaireMotion.compactBarEnter(),
        exit = ElovaireMotion.compactBarExit(),
        label = "CompactNowPlayingDockVisibility",
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
internal fun PinnedBackTopBar(
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
        ElovaireAnimatedContent(
            targetState = spec,
            transitionSpec = {
                when {
                    initialState is SharedTopBarSpec.Unified && targetState !is SharedTopBarSpec.Unified -> {
                        ElovaireMotion.sharedTopBarForwardTransform()
                    }

                    initialState !is SharedTopBarSpec.Unified && targetState is SharedTopBarSpec.Unified -> {
                        ElovaireMotion.sharedTopBarBackTransform()
                    }

                    else -> ElovaireMotion.sharedTopBarTransform()
                }
            },
            contentKey = { it.visualSignature() },
            label = "SharedTopBarOverlayContent",
        ) { currentSpec ->
            when (currentSpec) {
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
                                targetState = currentSpec.title,
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
                        if (currentSpec.showSettings) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (currentSpec.supplementalActionIconResId != null && currentSpec.onSupplementalAction != null) {
                                    HeaderIconButton(
                                        iconResId = currentSpec.supplementalActionIconResId,
                                        contentDescription = currentSpec.supplementalActionContentDescription ?: "Action",
                                        showBackground = false,
                                        onClick = currentSpec.onSupplementalAction,
                                    )
                                }
                                HeaderIconButton(
                                    iconResId = R.drawable.ic_lucide_menu,
                                    contentDescription = "Menu",
                                    showBackground = false,
                                    onClick = currentSpec.onOpenMenu,
                                )
                            }
                        } else {
                            SpacerTile(modifier = Modifier.size(40.dp))
                        }
                    }
                }

                is SharedTopBarSpec.Back -> {
                    if (currentSpec.centeredTitle) {
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
                                onClick = currentSpec.onBack,
                                modifier = Modifier.align(Alignment.CenterStart),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 64.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
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
                                onClick = currentSpec.onBack,
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
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
                            onClick = currentSpec.onBack,
                        )
                        if (currentSpec.subtitle.isNullOrBlank()) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
                                    transitionSpec = { ElovaireMotion.titleSwapTransform() },
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
                                    targetState = currentSpec.title,
                                    transitionSpec = { ElovaireMotion.titleSwapTransform() },
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
                                    text = currentSpec.subtitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (currentSpec.actions.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                currentSpec.actions.forEach { action ->
                                    HeaderIconButton(
                                        iconResId = action.iconResId,
                                        contentDescription = action.contentDescription,
                                        showBackground = false,
                                        onClick = action.onClick,
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
    val compactControlBackground by animateColorAsState(
        targetValue = resolvedPrimaryTextColor.copy(alpha = 0.2f),
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerControlBackground",
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
                    .background(compactControlBackground)
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
                    val strokeWidth = size.minDimension * 0.1f
                    val arcInset = strokeWidth / 2f + 2.2f
                    val arcStart = -90f
                    val arcSweep = 360f
                    drawArc(
                        color = controlIconTint.copy(alpha = 0.18f),
                        startAngle = arcStart,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = controlIconTint,
                        startAngle = arcStart,
                        sweepAngle = arcSweep * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
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
    val motionDurationScale = rememberSystemAnimationScale()
    val infiniteTransition = rememberInfiniteTransition(label = "first_launch_permission_spinner")
    val animatedRotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ElovaireMotion.scaleDurationMillis(1100, motionDurationScale).toInt(),
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "first_launch_permission_spinner_rotation",
    )
    val rotationDegrees = if (motionDurationScale <= 0f) 0f else animatedRotationDegrees
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
    lastPlayedPlaylist: Playlist?,
    songsById: Map<Long, Song>,
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
    onPlaylistSelected: (Playlist) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayPlaylist: (Playlist, List<Song>) -> Unit,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val listState = rememberElovaireLazyListState("home_screen")
    val language = LocalAppLanguage.current
    val homeCopy = remember(language) { homeCopy(language) }
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
    val lastPlayedPlaylistSongs = remember(lastPlayedPlaylist, songsById) {
        lastPlayedPlaylist?.songIds?.mapNotNull(songsById::get).orEmpty()
    }
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
                        text = homeCopy.indexingTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = homeCopy.indexingMessage,
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
                                text = homeCopy.emptyLibraryTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = homeCopy.emptyLibraryMessage,
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
                            animationSpec = ElovaireMotion.offsetSoft(durationMillis = 320),
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
                        when {
                            lastPlayedPlaylist != null && lastPlayedPlaylistSongs.isNotEmpty() -> item(
                                key = "home_last_played_playlist_${lastPlayedPlaylist.id}",
                            ) {
                                LastPlayedPlaylistModule(
                                    playlist = lastPlayedPlaylist,
                                    songs = lastPlayedPlaylistSongs,
                                    onOpen = { onPlaylistSelected(lastPlayedPlaylist) },
                                    onPlay = { onPlayPlaylist(lastPlayedPlaylist, lastPlayedPlaylistSongs) },
                                )
                            }
                            lastPlayedAlbum != null -> item(
                                key = "home_last_played_album_${lastPlayedAlbum.id}",
                            ) {
                                val album = lastPlayedAlbum
                                LastPlayedAlbumModule(
                                    album = album,
                                    onOpen = { origin -> onAlbumSelected(album, origin) },
                                    onPlay = { onPlayAlbum(album) },
                                )
                            }
                        }

                        if (recentlyAddedAlbums.isNotEmpty()) {
                            item(key = "home_recently_added") {
                                ModuleCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        MutedSectionHeader(
                                            title = miscPhrase(LocalAppLanguage.current, MiscPhrase.RecentlyAdded),
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
                            item(key = "home_recently_added_empty") {
                                EmptyStateCard(
                                    title = homeCopy.noRecentAdditionsTitle,
                                    message = homeCopy.noRecentAdditionsMessage,
                                )
                            }
                        }

                    item(key = "home_recently_played") {
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
                                    text = homeCopy.recentlyPlayedSongsTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (recentSongs.isEmpty()) {
                                Text(
                                    text = homeCopy.recentlyPlayedSongsEmpty,
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
                        item(key = "home_favorite_albums") {
                            FavoriteAlbumsModule(
                                albums = favoriteAlbums.take(6),
                                title = homeCopy.favoriteAlbumsTitle,
                                subtitle = homeCopy.favoriteAlbumsSubtitle,
                                onAlbumSelected = onAlbumSelected,
                            )
                        }
                    } else if (!isLibraryLoading) {
                        item(key = "home_favorite_albums_empty") {
                            EmptyStateCard(
                                title = homeCopy.noFavoriteAlbumsTitle,
                                message = homeCopy.noFavoriteAlbumsMessage,
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
    val language = LocalAppLanguage.current
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
private fun LastPlayedPlaylistModule(
    playlist: Playlist,
    songs: List<Song>,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artworkSong = songs.firstOrNull()
    val artwork = rememberArtworkBitmap(artworkSong?.artUri, size = 512)
    val gradient = rememberArtworkGradient(artworkSong?.artUri).value
    val totalDurationMs = remember(songs) { songs.sumOf { it.durationMs } }
    val language = LocalAppLanguage.current
    val metaItems = remember(songs, totalDurationMs, language) {
        listOf(
            localizedCountLabel(songs.size, "track", language),
            formatPlaylistDuration(totalDurationMs),
        )
    }
    val playBackground = gradient.first()
        .copy(alpha = 0.24f)
        .compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
    val playTint = if (playBackground.luminance() > 0.56f) InkText else Color.White
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseTint = if (darkTheme) Color(0xFF141414).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.82f)
    val playlistTint = gradient.first().copy(alpha = 0.46f)
    val controlBaseTint = if (darkTheme) {
        gradient.last().copy(alpha = 0.28f).compositeOver(Color.Black.copy(alpha = 0.16f))
    } else {
        gradient.last().copy(alpha = 0.22f).compositeOver(Color.White.copy(alpha = 0.16f))
    }
    val contentColor = if (controlBaseTint.luminance() > 0.42f) InkText else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.72f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpen,
            )
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
                .background(playlistTint),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = artworkSong?.artUri,
                title = playlist.name,
                modifier = Modifier.size(88.dp),
                cornerRadius = ElovaireRadii.artwork,
                showArtworkGlow = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = artworkSong?.artist.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = secondaryContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metaItems.joinToString("  •  "),
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                        contentDescription = playLabel(language),
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
    playlists: List<Playlist>,
    layoutMode: AlbumLayoutMode,
    sortMode: AlbumSortMode,
    topPadding: Dp,
    bottomPadding: Dp,
    title: String = "All albums",
    subtitle: String = "Alphabetical by album artist, then album title.",
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSortModeChanged: (AlbumSortMode) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onAddAlbumToPlaylist: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
    playlistSongsById: Map<Long, Song>,
    favoriteSongIds: Set<Long>,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
    onDeleteAlbumFromDevice: (Album) -> Unit,
) {
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    var selectedAlbumIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var showPlaylistPicker by rememberSaveable { mutableStateOf(false) }
    val listState = rememberElovaireLazyListState(title, "album_collection_list")
    val gridState = rememberElovaireLazyGridState(title, "album_collection_grid")
    val selectionModeActive = selectedAlbumIds.isNotEmpty()
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
    val selectedAlbums = remember(sortedAlbums, selectedAlbumIds) {
        sortedAlbums.filter { it.id in selectedAlbumIds }
    }
    val selectedAlbumSongs = remember(selectedAlbums) {
        selectedAlbums.flatMap { it.songs }.distinctBy { it.id }
    }
    val selectionTopInset by animateDpAsState(
        targetValue = if (selectionModeActive) 50.dp else 0.dp,
        animationSpec = ElovaireMotion.sizeSoft(),
        label = "album_selection_top_inset",
    )
    BackHandler(enabled = selectionModeActive) {
        selectedAlbumIds = emptySet()
        showPlaylistPicker = false
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when (layoutMode) {
            AlbumLayoutMode.Grid -> {
                LazyVerticalGrid(
                    state = gridState,
                    overscrollEffect = null,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(gridState),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = topPadding + selectionTopInset + 8.dp,
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
                            selectionMode = selectionModeActive,
                            selected = album.id in selectedAlbumIds,
                            onOpen = { origin ->
                                if (selectionModeActive) {
                                    selectedAlbumIds = selectedAlbumIds.toggleSelection(album.id)
                                } else {
                                    onAlbumSelected(album, origin)
                                }
                            },
                            onLongPress = {
                                showSortOptions = false
                                selectedAlbumIds = selectedAlbumIds + album.id
                            },
                        )
                    }
                }
                FastScrollbar(
                    state = gridState,
                    topInset = topPadding + selectionTopInset + 16.dp,
                    bottomInset = bottomPadding + 16.dp,
                )
            }

            AlbumLayoutMode.Compact -> {
                LazyColumn(
                    state = listState,
                    overscrollEffect = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(listState),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = topPadding + selectionTopInset + 8.dp,
                        end = 20.dp,
                        bottom = bottomPadding + 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
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

                    itemsIndexed(sortedAlbums, key = { _, album -> album.id }) { index, album ->
                        CompactAlbumRow(
                            album = album,
                            selectionMode = selectionModeActive,
                            selected = album.id in selectedAlbumIds,
                            isFavorite = album.songs.isNotEmpty() && album.songs.all { it.id in favoriteSongIds },
                            showFavoriteButton = true,
                            onOpen = { origin ->
                                if (selectionModeActive) {
                                    selectedAlbumIds = selectedAlbumIds.toggleSelection(album.id)
                                } else {
                                    onAlbumSelected(album, origin)
                                }
                            },
                            onToggleFavorite = {
                                onSetAlbumFavorite(
                                    album.songs.map(Song::id),
                                    album.songs.any { it.id !in favoriteSongIds },
                                )
                            },
                            onLongPress = {
                                showSortOptions = false
                                selectedAlbumIds = selectedAlbumIds + album.id
                            },
                        )
                        if (index != sortedAlbums.lastIndex) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                DividerLine(
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                )
                            }
                        }
                    }
                }
                FastScrollbar(
                    state = listState,
                    topInset = topPadding + selectionTopInset + 16.dp,
                    bottomInset = bottomPadding + 16.dp,
                )
            }

        }
        AnimatedVisibility(
            visible = selectionModeActive,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = ElovaireMotion.verticalRevealEnter(),
            exit = ElovaireMotion.verticalRevealExit(),
        ) {
            TopBarSelectionMenu(
                topBarHeight = topPadding,
                onAddToPlaylist = { showPlaylistPicker = true },
                onDelete = {
                    onDeleteAlbumFromDevice(
                        Album(
                            id = -1L,
                            title = "",
                            artist = "",
                            artUri = null,
                            songCount = selectedAlbumSongs.size,
                            durationMs = selectedAlbumSongs.sumOf { it.durationMs },
                            songs = selectedAlbumSongs,
                        ),
                    )
                    selectedAlbumIds = emptySet()
                },
            )
        }
    }
    if (showPlaylistPicker && selectionModeActive) {
        val language = LocalAppLanguage.current
        PlaylistSelectionDialog(
            title = uiPhrase(language, UiPhrase.AddToPlaylist),
            subtitle = when (selectedAlbums.size) {
                1 -> selectedAlbums.first().title
                else -> "${localizedCountLabel(selectedAlbums.size, "album", language)} ${miscPhrase(language, MiscPhrase.Selected)} • ${localizedCountLabel(selectedAlbumSongs.size, "song", language)}"
            },
            playlists = playlists.filterNot { it.isSystem },
            playlistSongsById = playlistSongsById,
            onDismiss = { showPlaylistPicker = false },
            onPlaylistSelected = { playlistId ->
                selectedAlbums.forEach { album ->
                    onAddAlbumToPlaylist(playlistId, album)
                }
                showPlaylistPicker = false
                selectedAlbumIds = emptySet()
            },
            onCreatePlaylist = onCreatePlaylist,
        )
    }
}

@Composable
private fun TopBarSelectionMenu(
    topBarHeight: Dp,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val language = LocalAppLanguage.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight + 50.dp),
    ) {
        FrostedTopBarBackground(
            darkTheme = darkTheme,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumCollectionActionButton(
                iconResId = R.drawable.ic_lucide_list_plus,
                label = uiPhrase(language, UiPhrase.AddToPlaylist),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = onAddToPlaylist,
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            )
            AlbumCollectionActionButton(
                iconResId = R.drawable.ic_lucide_trash_2,
                label = uiPhrase(language, UiPhrase.Delete),
                tint = DestructiveRed,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
            )
        }
    }
}

private data class TopBarMenuAction(
    @DrawableRes val iconResId: Int,
    val label: String,
    val tint: Color,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
private fun TopBarDualActionMenu(
    topBarHeight: Dp,
    leadingAction: TopBarMenuAction,
    trailingAction: TopBarMenuAction,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight + 50.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp),
        ) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumCollectionActionButton(
                iconResId = leadingAction.iconResId,
                label = leadingAction.label,
                tint = leadingAction.tint,
                enabled = leadingAction.enabled,
                modifier = Modifier.weight(1f),
                onClick = leadingAction.onClick,
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            )
            AlbumCollectionActionButton(
                iconResId = trailingAction.iconResId,
                label = trailingAction.label,
                tint = trailingAction.tint,
                enabled = trailingAction.enabled,
                modifier = Modifier.weight(1f),
                onClick = trailingAction.onClick,
            )
        }
    }
}

@Composable
private fun AlbumCollectionActionButton(
    @DrawableRes iconResId: Int,
    label: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = tint.copy(alpha = if (enabled) 1f else 0.5f),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = tint.copy(alpha = if (enabled) 1f else 0.5f),
                textAlign = TextAlign.Center,
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
    scrollToTopRequestVersion: Long,
    onRequestCreatePlaylist: () -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylists: (Set<Long>) -> Unit,
    onOpenPlaylist: (Playlist, ExpandOrigin) -> Unit,
) {
    var playlistBeingRenamed by remember { mutableStateOf<Playlist?>(null) }
    var selectedPlaylistIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    val songsById = remember(libraryState.songs) { libraryState.songs.associateBy { it.id } }
    val gridState = rememberElovaireLazyGridState("playlists_screen")
    val editMode = selectedPlaylistIds.isNotEmpty()
    val selectionTopInset by animateDpAsState(
        targetValue = if (editMode) 50.dp else 0.dp,
        animationSpec = ElovaireMotion.sizeSoft(),
        label = "playlist_selection_top_inset",
    )
    BackHandler(enabled = editMode) {
        selectedPlaylistIds = emptySet()
    }
    LaunchedEffect(scrollToTopRequestVersion) {
        if (scrollToTopRequestVersion > 0L) {
            gridState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
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
                    top = topPadding + selectionTopInset + 12.dp,
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
                            } else {
                                onOpenPlaylist(playlist, origin)
                            }
                        },
                        onLongPress = {
                            if (!playlist.isSystem) {
                                selectedPlaylistIds = selectedPlaylistIds.togglePlaylistSelection(playlist.id)
                            }
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = ElovaireMotion.verticalRevealEnter(),
            exit = ElovaireMotion.verticalRevealExit(),
        ) {
            TopBarDualActionMenu(
                topBarHeight = topPadding,
                leadingAction = TopBarMenuAction(
                    iconResId = R.drawable.ic_lucide_square_pen,
                    label = uiPhrase(LocalAppLanguage.current, UiPhrase.Rename),
                    tint = if (selectedPlaylistIds.size == 1) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    },
                    onClick = {
                        if (selectedPlaylistIds.size == 1) {
                            playlistBeingRenamed = playlists.firstOrNull { it.id == selectedPlaylistIds.firstOrNull() }
                        }
                    },
                ),
                trailingAction = TopBarMenuAction(
                    iconResId = R.drawable.ic_lucide_trash_2,
                    label = uiPhrase(LocalAppLanguage.current, UiPhrase.RemoveFromList),
                    tint = DestructiveRed,
                    onClick = {
                        onDeletePlaylists(selectedPlaylistIds)
                        selectedPlaylistIds = emptySet()
                    },
                ),
            )
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
    scrollToTopRequestVersion: Long,
    onOpenCollection: (LibraryCollectionKind) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
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
    LaunchedEffect(scrollToTopRequestVersion) {
        if (scrollToTopRequestVersion > 0L) {
            listState.animateScrollToItem(0)
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
                            title = common.songs,
                            detail = "${localizedCountLabel(totalSongs, "song", language)} ${common.inYourLibrary}",
                            onClick = { onOpenCollection(LibraryCollectionKind.Songs) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_disc_album,
                            title = common.albums,
                            detail = localizedCountLabel(totalAlbums, "album", language),
                            onClick = { onOpenCollection(LibraryCollectionKind.Albums) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_mic_vocal,
                            title = common.artists,
                            detail = localizedCountLabel(totalArtists, "artist", language),
                            onClick = { onOpenCollection(LibraryCollectionKind.Artists) },
                        )
                        DividerLine()
                        LibraryHubRow(
                            iconResId = R.drawable.ic_lucide_guitar,
                            title = common.genres,
                            detail = localizedCountLabel(totalGenres, "genre", language),
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
                                title = miscPhrase(LocalAppLanguage.current, MiscPhrase.RecentlyAdded),
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
    playlists: List<Playlist>,
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
    onAddAlbumToPlaylist: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
    playlistSongsById: Map<Long, Song>,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
    onDeleteAlbumFromDevice: (Album) -> Unit,
    onAlbumCollectionLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSongCollectionLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onAlbumSortModeChanged: (AlbumSortMode) -> Unit,
    onSongSortModeChanged: (SongSortMode) -> Unit,
    onGenreSelected: (String) -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
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
                playlists = playlists,
                layoutMode = albumCollectionLayoutMode,
                sortMode = albumSortMode,
                topPadding = detailTopBarOccupiedHeight(),
                bottomPadding = bottomPadding,
                title = common.albums,
                subtitle = "Alphabetical by album artist, then album title",
                onLayoutModeChanged = onAlbumCollectionLayoutModeChanged,
                onSortModeChanged = onAlbumSortModeChanged,
                onAlbumSelected = onAlbumSelected,
                onAddAlbumToPlaylist = onAddAlbumToPlaylist,
                onCreatePlaylist = onCreatePlaylist,
                playlistSongsById = playlistSongsById,
                favoriteSongIds = favoriteSongIds,
                onSetAlbumFavorite = onSetAlbumFavorite,
                onDeleteAlbumFromDevice = onDeleteAlbumFromDevice,
            )
            DetailListTopBar(
                title = common.albums,
                subtitle = localizedCountLabel(libraryState.albums.size, "album", language),
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
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
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
            title = common.songs,
            subtitle = localizedCountLabel(sortedSongs.size, "song", language),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
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
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val scrollState = rememberElovaireScrollState("artist_collection")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
        FastScrollbar(
            state = scrollState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
        )

        DetailListTopBar(
            title = common.artists,
            subtitle = localizedCountLabel(artists.size, "artist", language),
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
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val scrollState = rememberElovaireScrollState("genre_collection")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailSectionTopGap,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
        FastScrollbar(
            state = scrollState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
        )

        DetailListTopBar(
            title = common.genres,
            subtitle = localizedCountLabel(genres.size, "genre", language),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun GenreAlbumsScreen(
    genre: String,
    libraryState: LibraryUiState,
    playlists: List<Playlist>,
    layoutMode: AlbumLayoutMode,
    sortMode: AlbumSortMode,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSortModeChanged: (AlbumSortMode) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onAddAlbumToPlaylist: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
    playlistSongsById: Map<Long, Song>,
    favoriteSongIds: Set<Long>,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
    onDeleteAlbumFromDevice: (Album) -> Unit,
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
            playlists = playlists,
            layoutMode = layoutMode,
            sortMode = sortMode,
            topPadding = detailTopBarOccupiedHeight(),
            bottomPadding = bottomPadding,
            title = genre.ifBlank { "Unknown Genre" },
            subtitle = localizedCountLabel(filteredAlbums.size, "album", LocalAppLanguage.current),
            onLayoutModeChanged = onLayoutModeChanged,
            onSortModeChanged = onSortModeChanged,
            onAlbumSelected = onAlbumSelected,
            onAddAlbumToPlaylist = onAddAlbumToPlaylist,
            onCreatePlaylist = onCreatePlaylist,
            playlistSongsById = playlistSongsById,
            favoriteSongIds = favoriteSongIds,
            onSetAlbumFavorite = onSetAlbumFavorite,
            onDeleteAlbumFromDevice = onDeleteAlbumFromDevice,
        )
        DetailListTopBar(
            title = genre.ifBlank { "Unknown Genre" },
            subtitle = localizedCountLabel(filteredAlbums.size, "album", LocalAppLanguage.current),
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
                            subtitle = "${formatCountLabel(topSongs.size, "track")} you return to the most",
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
                language = LocalAppLanguage.current,
            ),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

private fun buildArtistScreenSubtitle(
    songCount: Int,
    albumCount: Int,
    language: AppLanguage,
): String {
    return "${localizedCountLabel(albumCount, "album", language)} • ${localizedCountLabel(songCount, "song", language)}"
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    painter = painterResource(id = R.drawable.ic_lucide_square_plus),
                    contentDescription = "Create playlist",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Text(
            text = uiPhrase(LocalAppLanguage.current, UiPhrase.NewPlaylist),
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
    val language = LocalAppLanguage.current
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
            text = formatCountLabel(playlist.songIds.size, "song"),
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
                    .clip(RoundedCornerShape(ElovaireRadii.artwork))
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
            shape = RoundedCornerShape(ElovaireRadii.artwork),
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
                                            requestedSizePx = 512,
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
    confirmLabel: String = "Save",
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val canConfirm = name.isNotBlank()
    val displayTitle = if (title == "New playlist") uiPhrase(language, UiPhrase.NewPlaylist) else title
    val displayConfirmLabel = if (confirmLabel == "Save" || confirmLabel == "Create") uiPhrase(language, UiPhrase.Create) else confirmLabel
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
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(24f)),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PlaylistNameInputField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(language, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            onClick = { onConfirm(name.trim()) },
                            enabled = canConfirm,
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = if (canConfirm) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            contentColor = if (canConfirm) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                            },
                        ) {
                            Text(
                                text = displayConfirmLabel,
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
private fun PlaylistNameInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Playlist name",
) {
    val language = LocalAppLanguage.current
    val localizedPlaceholder = if (placeholder == "Playlist name") uiPhrase(language, UiPhrase.NewPlaylist) else placeholder
    val contentColor = MaterialTheme.colorScheme.onSurface
    val leadingIconAlpha by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = ElovaireMotion.standardTween(durationMillis = 80),
        label = "playlist_name_icon_alpha",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(ElovaireRadii.input),
        placeholder = {
            Text(
                text = localizedPlaceholder,
                color = contentColor.copy(alpha = 0.44f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_pencil_line),
                contentDescription = null,
                tint = contentColor.copy(alpha = leadingIconAlpha),
                modifier = Modifier.size(16.dp),
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
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
                        .background(contentColor.copy(alpha = 0.1f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onValueChange("") },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_x),
                        contentDescription = "Clear playlist name",
                        tint = contentColor.copy(alpha = 0.86f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.38f),
            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            cursorColor = contentColor,
            focusedPlaceholderColor = contentColor.copy(alpha = 0.44f),
            unfocusedPlaceholderColor = contentColor.copy(alpha = 0.44f),
        ),
    )
}

@Composable
private fun AddAlbumToPlaylistDialog(
    album: Album,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = album.title,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = { playlistId -> onPlaylistSelected(playlistId, album) },
        onCreatePlaylist = onCreatePlaylist,
    )
}

@Composable
private fun PlaylistSelectionDialog(
    title: String,
    subtitle: String?,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
    onCreatePlaylist: ((String) -> Long)?,
) {
    val language = LocalAppLanguage.current
    val listState = rememberElovaireLazyListState(title, subtitle, "playlist_picker")
    val createdPlaylists = remember(title, subtitle) { mutableStateListOf<Playlist>() }
    var draftPlaylistName by rememberSaveable(title, subtitle) { mutableStateOf("") }
    var showInlineCreator by rememberSaveable(title, subtitle) { mutableStateOf(false) }
    var selectedPlaylistId by rememberSaveable(title, subtitle) { mutableStateOf<Long?>(null) }
    val visibleRows = 4
    val rowHeight = 82.dp
    val rowSpacing = 12.dp
    val listHeight = (rowHeight * visibleRows) + (rowSpacing * (visibleRows - 1))
    val displayedPlaylists = run {
        val existingIds = playlists.mapTo(mutableSetOf<Long>()) { it.id }
        playlists + createdPlaylists.filter { existingIds.add(it.id) }
    }

    LaunchedEffect(showInlineCreator, displayedPlaylists.size) {
        if (showInlineCreator) {
            listState.animateScrollToItem(displayedPlaylists.size)
        }
    }

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
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateContentSize(animationSpec = ElovaireMotion.sizeSoft()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_list_plus),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = secondaryBodyTextStyle().copy(fontWeight = FontWeight.Medium),
                            color = readableSecondaryTextColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight),
                    ) {
                        if (displayedPlaylists.isEmpty() && !showInlineCreator) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = uiPhrase(language, UiPhrase.NewPlaylist),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = readableSecondaryTextColor(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                overscrollEffect = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .ensureSingleItemRubberBand(listState),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing),
                            ) {
                                items(displayedPlaylists, key = { it.id }) { playlist ->
                                    val previewSongs = playlist.songIds.mapNotNull(playlistSongsById::get)
                                    PlaylistPickerRow(
                                        playlist = playlist,
                                        previewSongs = previewSongs,
                                        selected = playlist.id == selectedPlaylistId,
                                        modifier = Modifier.animateItem(
                                            placementSpec = spring(
                                                dampingRatio = 0.76f,
                                                stiffness = 360f,
                                            ),
                                        ),
                                        onClick = {
                                            selectedPlaylistId = if (selectedPlaylistId == playlist.id) {
                                                null
                                            } else {
                                                playlist.id
                                            }
                                        },
                                    )
                                }
                                if (showInlineCreator && onCreatePlaylist != null) {
                                    item(key = "inline_playlist_creator") {
                                        InlinePlaylistCreatorRow(
                                            name = draftPlaylistName,
                                            onNameChange = { draftPlaylistName = it },
                                            modifier = Modifier.animateItem(
                                                placementSpec = spring(
                                                    dampingRatio = 0.76f,
                                                    stiffness = 360f,
                                                ),
                                            ),
                                            onSave = {
                                                val trimmedName = draftPlaylistName.trim()
                                                if (trimmedName.isBlank()) return@InlinePlaylistCreatorRow
                                                val createdId = onCreatePlaylist(trimmedName)
                                                if (createdId > 0L) {
                                                    createdPlaylists += Playlist(id = createdId, name = trimmedName)
                                                    selectedPlaylistId = createdId
                                                    draftPlaylistName = ""
                                                    showInlineCreator = false
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (onCreatePlaylist != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(40.dp)
                                .align(Alignment.CenterHorizontally),
                            onClick = {
                                showInlineCreator = true
                                selectedPlaylistId = null
                            },
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiPhrase(language, UiPhrase.NewPlaylist),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(language, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = {
                                selectedPlaylistId?.let(onPlaylistSelected)
                            },
                            enabled = selectedPlaylistId != null,
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = if (selectedPlaylistId != null) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            contentColor = if (selectedPlaylistId != null) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                            },
                        ) {
                            Text(
                                text = uiPhrase(language, UiPhrase.AddToPlaylist),
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
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
private fun PlaylistPickerRow(
    playlist: Playlist,
    previewSongs: List<Song>,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val highlightColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_picker_row_highlight",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(highlightColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaylistArtworkPreview(
                songs = previewSongs,
                title = playlist.name,
                modifier = Modifier.size(62.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playlist.songIds.isEmpty()) {
                    Text(
                        text = "No songs in this playlist yet",
                        style = MaterialTheme.typography.labelLarge,
                        color = readableSecondaryTextColor().copy(alpha = 0.7f),
                    )
                } else {
                    val durationMs = remember(previewSongs) { previewSongs.sumOf { it.durationMs } }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                append(formatCountLabel(playlist.songIds.size, "track"))
                            }
                            append("  •  ")
                            withStyle(
                                SpanStyle(
                                    color = readableSecondaryTextColor().copy(alpha = 0.82f),
                                ),
                            ) {
                                append(formatPlaylistDuration(durationMs))
                            }
                        },
                        style = secondaryBodyTextStyle().copy(fontSize = MaterialTheme.typography.labelLarge.fontSize),
                    )
                }
            }
            SelectionIndicatorIcon(
                selected = selected,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
    }
}

@Composable
private fun InlinePlaylistCreatorRow(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSave = name.isNotBlank()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaylistArtworkPreview(
                    songs = emptyList(),
                    title = uiPhrase(LocalAppLanguage.current, UiPhrase.NewPlaylist),
                    modifier = Modifier.size(62.dp),
                )
                PlaylistNameInputField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onSave,
                    enabled = canSave,
                    shape = RoundedCornerShape(ElovaireRadii.pill),
                    color = if (canSave) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    },
                    contentColor = if (canSave) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                    },
                ) {
                    Text(
                        text = "Save",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
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
    scrollToTopRequestVersion: Long,
    query: String,
    isSearchFieldFocused: Boolean,
    showAllSongResults: Boolean,
    searchSongSortMode: SearchSongSortMode,
    showSearchSongSortOptions: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchFieldFocusedChange: (Boolean) -> Unit,
    onShowAllSongResultsChange: (Boolean) -> Unit,
    onSearchSongSortModeChange: (SearchSongSortMode) -> Unit,
    onShowSearchSongSortOptionsChange: (Boolean) -> Unit,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onArtistSelected: (String) -> Unit,
    onRememberAlbumSearch: (Album) -> Unit,
    onRememberArtistSearch: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onClearSearchHistory: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = searchCopy(language)
    val listState = rememberElovaireLazyListState("search_screen")
    LaunchedEffect(scrollToTopRequestVersion) {
        if (scrollToTopRequestVersion > 0L) {
            listState.animateScrollToItem(0)
        }
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedQuery = query.trim()
    val allSongsListState = rememberElovaireLazyListState("search_all_songs", trimmedQuery, searchSongSortMode)
    val isSearchUiActive = trimmedQuery.isNotBlank() || isSearchFieldFocused || showAllSongResults
    val collapseAllSongResults: () -> Unit = {
        onShowAllSongResultsChange(false)
        onShowSearchSongSortOptionsChange(false)
    }
    val resetSearchToMain: () -> Unit = {
        onQueryChange("")
        onSearchFieldFocusedChange(false)
        onShowAllSongResultsChange(false)
        onShowSearchSongSortOptionsChange(false)
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }
    if (showAllSongResults && trimmedQuery.isNotBlank()) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Back(
                title = commonUiCopy(language).search,
                onBack = collapseAllSongResults,
                centeredTitle = false,
            ),
        )
    }
    BackHandler(enabled = isSearchUiActive) {
        when {
            showSearchSongSortOptions -> onShowSearchSongSortOptionsChange(false)
            showAllSongResults && trimmedQuery.isNotBlank() -> collapseAllSongResults()
            else -> resetSearchToMain()
        }
    }
    LaunchedEffect(isSearchUiActive) {
        onSearchQueryActiveChanged(isSearchUiActive)
    }
    LaunchedEffect(trimmedQuery) {
        if (trimmedQuery.isBlank()) {
            onShowAllSongResultsChange(false)
            onShowSearchSongSortOptionsChange(false)
        }
    }
    val contentMode = when {
        showAllSongResults && trimmedQuery.isNotBlank() -> SearchContentMode.AllSongs
        trimmedQuery.isBlank() -> SearchContentMode.Discover
        else -> SearchContentMode.Results
    }
    LaunchedEffect(scrollToTopRequestVersion, contentMode) {
        if (scrollToTopRequestVersion > 0L && contentMode == SearchContentMode.AllSongs) {
            allSongsListState.animateScrollToItem(0)
        }
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
                        subtitle = localizedCountLabel(artistSongs.size, "song", language),
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

    val searchBar: @Composable () -> Unit = {
        val searchBarContentColor = MaterialTheme.colorScheme.onSurface
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                if (it.trim().isBlank()) {
                    onShowAllSongResultsChange(false)
                    onShowSearchSongSortOptionsChange(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    onSearchFieldFocusedChange(focusState.isFocused)
                },
            shape = RoundedCornerShape(ElovaireRadii.input),
            singleLine = true,
            placeholder = { Text(copy.placeholder) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                androidx.compose.animation.AnimatedVisibility(
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
                            contentDescription = copy.clearSearch,
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (contentMode == SearchContentMode.AllSongs) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        top = topPadding + 8.dp,
                        end = 20.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                searchBar()
                SearchSongsResultsHeader(
                    resultCount = allMatchingSongs.size,
                    selected = searchSongSortMode,
                    expanded = showSearchSongSortOptions,
                    onToggleExpanded = {
                        onShowSearchSongSortOptionsChange(!showSearchSongSortOptions)
                    },
                    onSelect = { selectedMode ->
                        onSearchSongSortModeChange(selectedMode)
                        onShowSearchSongSortOptionsChange(false)
                    },
                )
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(ElovaireRadii.card),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        LazyColumn(
                            state = allSongsListState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .ensureSingleItemRubberBand(allSongsListState),
                            contentPadding = PaddingValues(bottom = bottomPadding + 84.dp),
                        ) {
                            itemsIndexed(
                                items = allMatchingSongs,
                                key = { _, song -> song.id },
                            ) { index, song ->
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
                    FastScrollbar(
                        state = allSongsListState,
                        topInset = 8.dp,
                        bottomInset = bottomPadding + 48.dp,
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
                    top = topPadding + 8.dp,
                    end = 20.dp,
                    bottom = bottomPadding + if (isSearchUiActive) 84.dp else 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    searchBar()
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
                                ) togetherWith ElovaireMotion.fullScreenForwardExit()
                            }

                            initialState == SearchContentMode.AllSongs && targetState == SearchContentMode.Results -> {
                                ElovaireMotion.fullScreenBackEnter() togetherWith ElovaireMotion.fullScreenBackExit(
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
                                    Spacer(modifier = Modifier)
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
                                                    text = searchCopy(language).nothingSearchedTitle,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = searchCopy(language).nothingSearchedMessage,
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
                                            title = searchCopy(language).suggestedAlbumsTitle,
                                            subtitle = searchCopy(language).suggestedAlbumsSubtitle,
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
                                            title = commonUiCopy(language).artists,
                                            subtitle = searchCopy(language).matchingArtists(matchingArtists.size),
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
                                        ModuleCard {
                                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                                SectionTitleRow(
                                                    title = commonUiCopy(language).albums,
                                                    subtitle = copy.matchingAlbums(matchingAlbums.size),
                                                    compact = true,
                                                )
                                                ArtistAlbumGallery(
                                                    albums = matchingAlbums,
                                                    onAlbumSelected = { album, origin ->
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
                                                onSearchFieldFocusedChange(false)
                                                onShowAllSongResultsChange(true)
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
                                            title = searchCopy(language).noResultsTitle,
                                            message = searchCopy(language).noResultsMessage(trimmedQuery),
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
    val language = LocalAppLanguage.current
    val copy = searchCopy(language)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = copy.recentlySearched,
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
                        text = copy.clearHistory,
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
    val language = LocalAppLanguage.current
    val copy = searchCopy(language)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTitleRow(
            title = commonUiCopy(language).songs,
            subtitle = copy.matchingSongs(resultCount),
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
    val language = LocalAppLanguage.current
    val copy = searchCopy(language)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitleRow(
                title = commonUiCopy(language).songs,
                subtitle = copy.matchingSongs(resultCount),
                modifier = Modifier.weight(1f),
            )
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
                        text = searchSortModeLabel(selected, language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    )
                }
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
                                text = searchSortModeLabel(mode, language),
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
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
internal fun readableSecondaryTextColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f)
    }
}

@Composable
internal fun secondaryBodyTextStyle(): TextStyle {
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
internal fun ModuleCard(
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        Text(
            text = title,
            style = if (compact) {
                MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(16f))
            } else {
                MaterialTheme.typography.headlineMedium
            },
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
    subtitle: String = "Music you come back to frequently",
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
    val language = LocalAppLanguage.current
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
    val language = LocalAppLanguage.current
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
            text = "${localizedCountLabel(artist.albumCount, "album", language)}  •  ${localizedCountLabel(artist.songCount, "song", language)}",
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
    val language = LocalAppLanguage.current
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
                text = "${localizedCountLabel(artist.albumCount, "album", language)}  •  ${localizedCountLabel(artist.songCount, "song", language)}",
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
    val language = LocalAppLanguage.current
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
                text = localizedCountLabel(genre.albumCount, "album", language),
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
                    enter = fadeIn(animationSpec = tween(60)),
                    exit = fadeOut(animationSpec = tween(60)),
                ) {
                    PlaybackActiveArtworkOverlay(
                        uri = song.artUri,
                        title = song.title,
                        modifier = Modifier.matchParentSize(),
                    )
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
private fun SelectionIndicatorIcon(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(tint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(12.dp),
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_circle),
                contentDescription = null,
                tint = tint.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AlbumGridCard(
    album: Album,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onOpen: (ExpandOrigin) -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    val language = LocalAppLanguage.current
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
                onLongClick = { onLongPress?.invoke() },
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.matchParentSize(),
                cornerRadius = ElovaireRadii.artwork,
                showArtworkGlow = true,
            )
            if (selectionMode) {
                SelectionIndicatorIcon(
                    selected = selected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
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
    selectionMode: Boolean = false,
    selected: Boolean = false,
    isFavorite: Boolean = false,
    showFavoriteButton: Boolean = false,
    onOpen: (ExpandOrigin) -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    val language = LocalAppLanguage.current
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx)) },
                onLongClick = { onLongPress?.invoke() },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(62.dp),
            ) {
                ArtworkImage(
                    uri = album.artUri,
                    title = album.title,
                    modifier = Modifier.matchParentSize(),
                    cornerRadius = ElovaireRadii.artworkSmall,
                )
            }
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
                            append(localizedCountLabel(album.songCount, "track", language))
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
            if (selectionMode) {
                Box(
                    modifier = Modifier.padding(end = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SelectionIndicatorIcon(selected = selected)
                }
            } else if (showFavoriteButton && onToggleFavorite != null) {
                AnimatedVisibility(
                    visible = !selectionMode,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                ) {
                    Box(modifier = Modifier.padding(end = 10.dp)) {
                        InlineFavoriteSongButton(
                            isFavorite = isFavorite,
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = onToggleFavorite,
                        )
                    }
                }
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
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onBack: () -> Unit,
    onOpenTagEditor: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onShuffleAlbum: (Album) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onCreatePlaylist: (String) -> Long,
    onDeleteSongsFromDevice: (List<Song>) -> Unit,
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

    val language = LocalAppLanguage.current
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
    val albumFooterText = remember(album.songCount, album.durationMs, albumOnSurface, language) {
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    color = albumOnSurface,
                    fontWeight = FontWeight.Normal,
                ),
            )
            append(localizedCountLabel(album.songCount, "track", language))
            pop()
            pushStyle(SpanStyle(color = albumOnSurface.copy(alpha = 0.5f)))
            append("  •  ")
            pop()
            pushStyle(
                SpanStyle(
                    color = albumOnSurface.copy(alpha = 0.7f),
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
    var selectedSongIds by rememberSaveable(album.id) { mutableStateOf(setOf<Long>()) }
    var showPlaylistPicker by rememberSaveable(album.id) { mutableStateOf(false) }
    val selectionModeActive = selectedSongIds.isNotEmpty()
    val selectedSongs = remember(album.songs, selectedSongIds) {
        album.songs.filter { it.id in selectedSongIds }
    }
    BackHandler(enabled = selectionModeActive) {
        selectedSongIds = emptySet()
        showPlaylistPicker = false
    }
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
                                tint = Color.White,
                                backgroundColor = RoseAccent,
                                onClick = { onPlayAlbum(album) },
                            )
                            AlbumHeaderActionButton(
                                iconResId = R.drawable.ic_lucide_shuffle,
                                contentDescription = "Shuffle album",
                                tint = Color.White,
                                backgroundColor = RoseAccent.copy(alpha = 0.7f),
                                iconSize = 18.dp,
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
                            selectionMode = selectionModeActive,
                            selected = song.id in selectedSongIds,
                            isFavorite = song.id in favoriteSongIds,
                            isCurrentSong = song.id == currentSongId,
                            isPlaybackActive = isCurrentSongPlaying,
                            onClick = {
                                if (selectionModeActive) {
                                    selectedSongIds = selectedSongIds.toggleSelection(song.id)
                                } else {
                                    onSongSelected(song, album.songs)
                                }
                            },
                            onLongPress = {
                                selectedSongIds = selectedSongIds + song.id
                            },
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
            actions = listOf(
                TopBarActionSpec(
                    iconResId = R.drawable.ic_lucide_square_pen,
                    contentDescription = "Edit tags",
                    onClick = { onOpenTagEditor(album) },
                ),
            ),
            modifier = Modifier.align(Alignment.TopCenter),
        )
        AnimatedVisibility(
            visible = selectionModeActive,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = ElovaireMotion.verticalRevealEnter(),
            exit = ElovaireMotion.verticalRevealExit(),
        ) {
            TopBarSelectionMenu(
                topBarHeight = detailTopPadding,
                onAddToPlaylist = { showPlaylistPicker = true },
                onDelete = {
                    onDeleteSongsFromDevice(selectedSongs)
                    selectedSongIds = emptySet()
                },
            )
        }
    }

    if (showPlaylistPicker && selectionModeActive) {
        val language = LocalAppLanguage.current
        PlaylistSelectionDialog(
            title = uiPhrase(language, UiPhrase.AddToPlaylist),
            subtitle = "${localizedCountLabel(selectedSongs.size, "song", language)} ${miscPhrase(language, MiscPhrase.Selected)}",
            playlists = playlists.filterNot { it.isSystem },
            playlistSongsById = playlistSongsById,
            onDismiss = { showPlaylistPicker = false },
            onPlaylistSelected = { playlistId ->
                onAddSongsToPlaylist(playlistId, selectedSongs.map(Song::id))
                selectedSongIds = emptySet()
                showPlaylistPicker = false
            },
            onCreatePlaylist = onCreatePlaylist,
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
    animate: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val motionDurationScale = rememberSystemAnimationScale()
    val phase = if (animate && motionDurationScale > 0f) {
        val infiniteTransition = rememberInfiniteTransition(label = "audio_lines_phase")
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = ElovaireMotion.scaleDurationMillis(1180, motionDurationScale).toInt(),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "audio_lines_value",
        )
        animatedPhase
    } else {
        0.35f
    }
    val baseHeights = floatArrayOf(0.26f, 0.54f, 0.84f, 0.42f, 0.68f, 0.3f)
    val phaseOffsets = floatArrayOf(0f, 0.17f, 0.31f, 0.48f, 0.67f, 0.83f)
    Canvas(modifier = modifier) {
        val lineWidth = size.width / 10f
        val gap = (size.width - (lineWidth * baseHeights.size)) / (baseHeights.size - 1).coerceAtLeast(1)
        val centerY = size.height / 2f
        baseHeights.forEachIndexed { index, baseHeight ->
            val primaryPhase = ((phase + phaseOffsets[index]) % 1f) * 6.2831855f
            val secondaryPhase = ((phase * 1.82f) + (phaseOffsets[index] * 0.58f)) * 6.2831855f
            val animationFactor = (
                ((sin(primaryPhase.toDouble()) + 1.0) * 0.5 * 0.72) +
                    ((sin(secondaryPhase.toDouble()) + 1.0) * 0.5 * 0.28)
                ).toFloat()
            val heightFactor = (baseHeight * 0.66f) + (animationFactor * 0.24f)
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
private fun PlaybackActiveArtworkOverlay(
    uri: Uri?,
    title: String,
    modifier: Modifier = Modifier,
) {
    val artworkShape = RoundedCornerShape(ElovaireRadii.artworkSmall)
    val artworkBitmap = rememberArtworkBitmap(uri = uri, size = 256).value
    Box(
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                shape = artworkShape
                clip = true
            }
            .clip(artworkShape),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkBitmap != null) {
            Image(
                bitmap = artworkBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
                    .blur(18.dp),
            )
        } else {
            ArtworkImage(
                uri = uri,
                title = title,
                modifier = Modifier
                    .matchParentSize()
                    .blur(18.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.12f)),
        )
        AnimatedAudioLinesIcon(
            tint = Color.White.copy(alpha = 0.82f),
            animate = true,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AlbumSongRow(
    song: Song,
    trackIndex: Int,
    selectionMode: Boolean,
    selected: Boolean,
    isFavorite: Boolean,
    isCurrentSong: Boolean,
    isPlaybackActive: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleFavorite: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongPress,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (selectionMode) {
                    SelectionIndicatorIcon(
                        selected = selected,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    AnimatedContent(
                        targetState = isCurrentSong && isPlaybackActive,
                        transitionSpec = { ElovaireMotion.quickContentSwapTransform() },
                        label = "album_row_track_indicator",
                    ) { showSignal ->
                        if (showSignal) {
                            AnimatedAudioLinesIcon(
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                animate = isPlaybackActive,
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
    onUpdateSongOrder: (List<Long>) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    if (playlist == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Playlist not found.")
        }
        return
    }

    val songsById = remember(librarySongs) { librarySongs.associateBy { it.id } }
    val defaultSongMenuActions = LocalSongMenuActions.current
    var editMode by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var showAddSongsPicker by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var editableSongIds by rememberSaveable(playlist.id) { mutableStateOf(playlist.songIds) }
    var songIdsMarkedForRemoval by rememberSaveable(playlist.id) { mutableStateOf(setOf<Long>()) }
    var activelyDraggedSongId by rememberSaveable(playlist.id) { mutableStateOf<Long?>(null) }
    var showEditModeMenu by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable(playlist.id) { mutableStateOf(false) }
    LaunchedEffect(playlist.id, playlist.songIds, editMode) {
        if (!editMode) {
            editableSongIds = playlist.songIds
            songIdsMarkedForRemoval = emptySet()
            activelyDraggedSongId = null
        }
    }
    val displayedSongIds = if (editMode) editableSongIds else playlist.songIds
    val playlistSongMenuActions = remember(defaultSongMenuActions, playlist.id, playlist.songIds) {
        defaultSongMenuActions.copy(
            onDeleteFromLibrary = { song ->
                onUpdateSongOrder(playlist.songIds.filterNot { it == song.id })
            },
            deletePhrase = UiPhrase.RemoveFromList,
        )
    }
    val playlistSongs = remember(displayedSongIds, songsById) {
        displayedSongIds.mapNotNull(songsById::get)
    }
    val playlistDurationMs = remember(playlistSongs) { playlistSongs.sumOf { it.durationMs } }
    val detailTopPadding = detailTopBarOccupiedHeight()
    val editMenuTopInset by animateDpAsState(
        targetValue = if (editMode && showEditModeMenu) 50.dp else 0.dp,
        animationSpec = ElovaireMotion.sizeSoft(),
        label = "playlist_edit_menu_top_inset",
    )
    val topBarActions = remember(editMode, playlist.isSystem) {
        buildList {
            if (editMode && !playlist.isSystem) {
                add(
                    TopBarActionSpec(
                        iconResId = R.drawable.ic_lucide_plus,
                        contentDescription = "Add songs",
                        onClick = { showAddSongsPicker = true },
                    ),
                )
            }
            if (!playlist.isSystem) {
                add(
                    TopBarActionSpec(
                        iconResId = if (editMode) R.drawable.ic_lucide_check else R.drawable.ic_lucide_square_pen,
                        contentDescription = if (editMode) "Save playlist changes" else "Edit playlist",
                        onClick = {
                            if (editMode) {
                                val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                                onUpdateSongOrder(updatedSongIds)
                                editableSongIds = updatedSongIds
                                songIdsMarkedForRemoval = emptySet()
                                activelyDraggedSongId = null
                                editMode = false
                                showEditModeMenu = false
                            } else {
                                editableSongIds = playlist.songIds
                                songIdsMarkedForRemoval = emptySet()
                                editMode = true
                                showEditModeMenu = true
                            }
                        },
                    ),
                )
            }
        }
    }
    BackHandler(enabled = showAddSongsPicker || editMode) {
        if (showAddSongsPicker) {
            showAddSongsPicker = false
        } else if (editMode) {
            val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
            onUpdateSongOrder(updatedSongIds)
            editableSongIds = updatedSongIds
            songIdsMarkedForRemoval = emptySet()
            activelyDraggedSongId = null
            editMode = false
            showEditModeMenu = false
        }
    }
    CompositionLocalProvider(LocalSongMenuActions provides playlistSongMenuActions) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
        val listState = rememberElovaireLazyListState(playlist.id, "playlist_detail")
        val scope = rememberCoroutineScope()
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            userScrollEnabled = true,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopPadding + ElovaireSpacing.albumHeaderTopGap + editMenuTopInset,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    PlaylistArtworkPreview(
                        songs = playlistSongs,
                        title = playlist.name,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = elovaireScaledSp(ALBUM_HEADER_TITLE_TEXT_SIZE_SP),
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8f,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                    ) {
                                        append(formatCountLabel(playlistSongs.size, "track"))
                                    }
                                    append("  •  ")
                                    withStyle(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Normal,
                                        ),
                                    ) {
                                        append(formatPlaylistDuration(playlistDurationMs))
                                    }
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        AlbumHeaderPlayButton(
                            tint = Color.White,
                            backgroundColor = RoseAccent,
                            onClick = { onPlayPlaylist(playlistSongs, playlist.name) },
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
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 34.dp, bottom = 34.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = miscPhrase(LocalAppLanguage.current, MiscPhrase.NoSongsYet),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = miscPhrase(LocalAppLanguage.current, MiscPhrase.AddSongsViaEdit),
                            style = MaterialTheme.typography.bodyLarge,
                            color = readableSecondaryTextColor(),
                            textAlign = TextAlign.Center,
                        )
                    }
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
                        modifier = if (editMode) {
                            Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = 0.52f,
                                    stiffness = 190f,
                                ),
                            )
                        } else {
                            Modifier
                        },
                    ) {
                        PlaylistSongRow(
                            song = song,
                            isFavorite = song.id in favoriteSongIds,
                            isCurrentSong = song.id == currentSongId,
                            isPlaybackActive = isCurrentSongPlaying,
                            editMode = editMode,
                            onClick = {
                                if (!editMode) {
                                    onSongSelected(song, playlistSongs)
                                }
                            },
                            markedForRemoval = song.id in songIdsMarkedForRemoval,
                            onLongPress = {
                                if (!playlist.isSystem && playlistSongs.isNotEmpty()) {
                                    if (!editMode) {
                                        editableSongIds = playlist.songIds
                                        songIdsMarkedForRemoval = emptySet()
                                        editMode = true
                                        showEditModeMenu = true
                                    }
                                }
                            },
                            onToggleMarkedForRemoval = {
                                songIdsMarkedForRemoval = if (song.id in songIdsMarkedForRemoval) {
                                    songIdsMarkedForRemoval - song.id
                                } else {
                                    songIdsMarkedForRemoval + song.id
                                }
                            },
                            isDragged = activelyDraggedSongId == song.id,
                            onDragActiveChanged = { isActive ->
                                activelyDraggedSongId = when {
                                    isActive -> song.id
                                    activelyDraggedSongId == song.id -> null
                                    else -> activelyDraggedSongId
                                }
                            },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onMoveBy = { delta ->
                                if (editMode && delta != 0) {
                                    val fromIndex = editableSongIds.indexOf(song.id)
                                    if (fromIndex >= 0) {
                                        val targetIndex = (fromIndex + delta).coerceIn(0, editableSongIds.lastIndex)
                                        if (targetIndex != fromIndex) {
                                            editableSongIds = editableSongIds.toMutableList().apply {
                                                add(targetIndex, removeAt(fromIndex))
                                            }.toList()
                                        }
                                    }
                                }
                            },
                            onReorderDrag = { dragAmount ->
                                if (editMode && editableSongIds.size > 1) {
                                    val visibleSongItems = listState.layoutInfo.visibleItemsInfo
                                        .filter { it.contentType == "playlist_song_row" }
                                    val currentAbsoluteIndex = index + 2
                                    val firstVisibleSongIndex = visibleSongItems.firstOrNull()?.index ?: currentAbsoluteIndex
                                    val lastVisibleSongIndex = visibleSongItems.lastOrNull()?.index ?: currentAbsoluteIndex
                                    val canScrollUp =
                                        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                                    val canScrollDown =
                                        visibleSongItems.lastOrNull()?.index != listState.layoutInfo.totalItemsCount - 1
                                    when {
                                        dragAmount < 0f &&
                                            currentAbsoluteIndex <= firstVisibleSongIndex &&
                                            canScrollUp -> {
                                            scope.launch {
                                                listState.scrollBy((dragAmount * 0.72f).coerceAtLeast(-22f))
                                            }
                                        }
                                        dragAmount > 0f &&
                                            currentAbsoluteIndex >= lastVisibleSongIndex &&
                                            canScrollDown -> {
                                            scope.launch {
                                                listState.scrollBy((dragAmount * 0.72f).coerceAtMost(22f))
                                            }
                                        }
                                    }
                                }
                            },
                            showOverflowMenu = !editMode,
                            showDivider = index != playlistSongs.lastIndex,
                        )
                    }
                }
            }
        }

        DetailListTopBar(
            title = playlist.name,
            subtitle = localizedCountLabel(playlistSongs.size, "song", LocalAppLanguage.current),
            onBack = {
                if (showAddSongsPicker) {
                    showAddSongsPicker = false
                } else if (editMode) {
                    val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                    onUpdateSongOrder(updatedSongIds)
                    editableSongIds = updatedSongIds
                    songIdsMarkedForRemoval = emptySet()
                    activelyDraggedSongId = null
                    editMode = false
                    showEditModeMenu = false
                } else {
                    onBack()
                }
            },
            actions = topBarActions,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        AnimatedVisibility(
            visible = editMode && showEditModeMenu && !playlist.isSystem,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = ElovaireMotion.verticalRevealEnter(),
            exit = ElovaireMotion.verticalRevealExit(),
        ) {
            TopBarDualActionMenu(
                topBarHeight = detailTopPadding,
                leadingAction = TopBarMenuAction(
                    iconResId = R.drawable.ic_lucide_square_pen,
                    label = uiPhrase(LocalAppLanguage.current, UiPhrase.Rename),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = { showRenameDialog = true },
                ),
                trailingAction = TopBarMenuAction(
                    iconResId = R.drawable.ic_lucide_trash_2,
                    label = uiPhrase(LocalAppLanguage.current, UiPhrase.RemoveFromList),
                    tint = DestructiveRed,
                    enabled = songIdsMarkedForRemoval.isNotEmpty(),
                    onClick = {
                        if (songIdsMarkedForRemoval.isNotEmpty()) {
                            editableSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                            songIdsMarkedForRemoval = emptySet()
                            activelyDraggedSongId = null
                        }
                    },
                ),
            )
        }
    }
    }

    if (showAddSongsPicker && !playlist.isSystem) {
        AddSongsToPlaylistOverlay(
            availableSongs = librarySongs,
            existingSongIds = editableSongIds.toSet(),
            onDismiss = { showAddSongsPicker = false },
            onAddSongs = { selectedSongIds ->
                editableSongIds = (editableSongIds + selectedSongIds).distinct()
                showAddSongsPicker = false
            },
        )
    }
    if (showRenameDialog && !playlist.isSystem) {
        PlaylistNameDialog(
            title = "Rename playlist",
            confirmLabel = "Save",
            initialName = playlist.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                onRenamePlaylist(playlist.id, name)
                showRenameDialog = false
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
    editMode: Boolean = false,
    onClick: () -> Unit,
    markedForRemoval: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleMarkedForRemoval: () -> Unit = {},
    isDragged: Boolean = false,
    onDragActiveChanged: (Boolean) -> Unit = {},
    onToggleFavorite: () -> Unit,
    onMoveBy: (Int) -> Unit = {},
    onReorderDrag: (Float) -> Unit = {},
    showOverflowMenu: Boolean = false,
    showDivider: Boolean,
) {
    val density = LocalDensity.current
    val reorderStepPx = remember(density) { with(density) { 18.dp.toPx() } }
    var reorderDragAccumulator by remember(song.id, editMode) { mutableFloatStateOf(0f) }
    var handleDragActive by remember(song.id, editMode) { mutableStateOf(false) }
    val visualDragOffsetY = reorderDragAccumulator.coerceIn(-20f, 20f)
    val handleTint by animateColorAsState(
        targetValue = if (handleDragActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_reorder_handle_tint",
    )
    val handleScale by animateFloatAsState(
        targetValue = if (handleDragActive) 1.1f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "playlist_reorder_handle_scale",
    )
    val rowScale by animateFloatAsState(
        targetValue = if (isDragged) 1.018f else 1f,
        animationSpec = if (isDragged) {
            spring(
                dampingRatio = 0.5f,
                stiffness = 210f,
            )
        } else {
            ElovaireMotion.releaseSpringSpec()
        },
        label = "playlist_drag_row_scale",
    )
    val rowTranslationY by animateFloatAsState(
        targetValue = if (isDragged) visualDragOffsetY else 0f,
        animationSpec = spring(
            dampingRatio = 0.42f,
            stiffness = 250f,
        ),
        label = "playlist_drag_row_translation",
    )
    val dragHighlight by animateColorAsState(
        targetValue = if (handleDragActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_reorder_drag_highlight",
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ElovaireRadii.tile))
                .background(dragHighlight)
                .graphicsLayer {
                    scaleX = rowScale
                    scaleY = rowScale
                    translationY = rowTranslationY
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongPress,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ElovaireAnimatedVisibility(
                visible = editMode,
                enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = ElovaireMotion.offsetSoft(),
                    ),
                exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = 80),
                    ),
                label = "playlist_song_reorder_handle",
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = handleScale
                            scaleY = handleScale
                        }
                        .pointerInput(song.id, editMode) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = true
                                    onDragActiveChanged(true)
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    reorderDragAccumulator += dragAmount
                                    onReorderDrag(dragAmount)
                                    while (reorderDragAccumulator <= -reorderStepPx) {
                                        onMoveBy(-1)
                                        reorderDragAccumulator += reorderStepPx
                                    }
                                    while (reorderDragAccumulator >= reorderStepPx) {
                                        onMoveBy(1)
                                        reorderDragAccumulator -= reorderStepPx
                                    }
                                },
                                onDragEnd = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = false
                                    onDragActiveChanged(false)
                                },
                                onDragCancel = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = false
                                    onDragActiveChanged(false)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_chevrons_up_down),
                        contentDescription = "Reorder song",
                        tint = handleTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
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
                    enter = fadeIn(animationSpec = tween(60)),
                    exit = fadeOut(animationSpec = tween(60)),
                ) {
                    PlaybackActiveArtworkOverlay(
                        uri = song.artUri,
                        title = song.album,
                        modifier = Modifier.matchParentSize(),
                    )
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
                modifier = Modifier.width(if (editMode) 36.dp else if (showOverflowMenu) 96.dp else 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElovaireAnimatedVisibility(
                    visible = !editMode,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                    label = "playlist_song_metadata_visibility",
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatDuration(song.durationMs),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(40.dp),
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
                ElovaireAnimatedVisibility(
                    visible = editMode,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = ElovaireMotion.scaleSoft(),
                        ),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                        scaleOut(
                            targetScale = 0.92f,
                            animationSpec = ElovaireMotion.fadeFast(),
                        ),
                    label = "playlist_song_remove_toggle",
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleMarkedForRemoval,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        SelectionIndicatorIcon(selected = markedForRemoval)
                    }
                }
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun AddSongsToPlaylistOverlay(
    availableSongs: List<Song>,
    existingSongIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddSongs: (List<Long>) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(PlaylistPickerTab.Songs) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSongIds by rememberSaveable { mutableStateOf(listOf<Long>()) }
    var selectedAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedArtistName by rememberSaveable { mutableStateOf<String?>(null) }
    var listResetVersion by rememberSaveable { mutableLongStateOf(0L) }
    val selectedSongIdSet = remember(selectedSongIds) { selectedSongIds.toSet() }
    val listState = rememberElovaireLazyListState(
        "playlist_add_songs_overlay",
        selectedTab.name,
        selectedAlbumId ?: -1L,
        selectedArtistName.orEmpty(),
        listResetVersion,
    )
    val candidateSongs = remember(availableSongs, existingSongIds) {
        availableSongs.filterNot { it.id in existingSongIds }
    }
    val trimmedQuery = query.trim()
    val albums = remember(candidateSongs) {
        candidateSongs.groupBy { it.albumId }
            .values
            .mapNotNull { songs ->
                songs.firstOrNull()?.let { first ->
                    val orderedSongs = songs.sortedWith(
                        compareBy<Song> { it.discNumber.takeIf { disc -> disc > 0 } ?: Int.MAX_VALUE }
                            .thenBy { it.trackNumber.takeIf { track -> track > 0 } ?: Int.MAX_VALUE }
                            .thenBy { it.dateAddedSeconds }
                            .thenBy { it.id },
                    )
                    Album(
                        id = first.albumId,
                        title = first.album,
                        artist = first.artist,
                        artUri = first.artUri,
                        songCount = orderedSongs.size,
                        durationMs = orderedSongs.sumOf { it.durationMs },
                        songs = orderedSongs,
                    )
                }
            }
            .sortedWith(compareBy<Album> { it.artist.lowercase() }.thenBy { it.title.lowercase() })
    }
    val artists = remember(candidateSongs) {
        candidateSongs.groupBy { it.artist }
            .map { (artistName, songs) ->
                ArtistEntry(
                    name = artistName,
                    artUri = songs.firstOrNull()?.artUri,
                    albumCount = songs.map { it.albumId }.distinct().size,
                    songCount = songs.size,
                ) to songs.sortedBy { it.title.lowercase() }
            }
            .sortedBy { it.first.name.lowercase() }
    }
    val filteredAlbums = remember(albums, trimmedQuery) {
        albums.filter { album ->
            searchMatchesComposite(trimmedQuery, listOf(album.title, album.artist))
        }
    }
    val filteredArtists = remember(artists, trimmedQuery) {
        artists.filter { (artist, songs) ->
            searchMatchesComposite(trimmedQuery, buildList {
                add(artist.name)
                songs.firstOrNull()?.album?.let(::add)
            })
        }
    }
    val filteredSongs = remember(candidateSongs, trimmedQuery) {
        candidateSongs.filter { song ->
            searchMatchesComposite(trimmedQuery, listOf(song.title, song.artist, song.album))
        }
    }
    val selectedAlbum = remember(selectedAlbumId, albums) {
        albums.firstOrNull { it.id == selectedAlbumId }
    }
    val selectedArtistSongs = remember(selectedArtistName, artists) {
        artists.firstOrNull { it.first.name == selectedArtistName }?.second.orEmpty()
    }
    val filteredAlbumSongs = remember(selectedAlbum) {
        selectedAlbum?.songs
            .orEmpty()
    }
    val filteredArtistSongs = remember(selectedArtistSongs, trimmedQuery) {
        selectedArtistSongs.filter { song ->
            searchMatchesComposite(trimmedQuery, listOf(song.title, song.artist, song.album))
        }
    }
    val handleBack: () -> Unit = {
        when {
            selectedAlbumId != null -> {
                selectedAlbumId = null
                listResetVersion += 1L
            }
            selectedArtistName != null -> {
                selectedArtistName = null
                listResetVersion += 1L
            }
            else -> onDismiss()
        }
    }
    val currentHandleBack = rememberUpdatedState(handleBack)
    val stableHandleBack = remember {
        { currentHandleBack.value.invoke() }
    }
    BackHandler(enabled = selectedAlbumId != null || selectedArtistName != null) {
        stableHandleBack()
    }
    val overlayTopPadding = detailTopBarOccupiedHeight()
    val overlayBottomPadding = 124.dp + buttonNavigationScrollBoost()
    val selectedSongIdsState = rememberUpdatedState(selectedSongIds)
    val topBarActions = remember(onAddSongs, onDismiss) {
        listOf(
            TopBarActionSpec(
                iconResId = R.drawable.ic_lucide_check,
                contentDescription = "Confirm added songs",
                onClick = {
                    val currentSelectedSongIds = selectedSongIdsState.value
                    if (currentSelectedSongIds.isNotEmpty()) {
                        onAddSongs(currentSelectedSongIds)
                    } else {
                        onDismiss()
                    }
                },
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .zIndex(12f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = overlayTopPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlaylistPickerTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .clickable {
                                    if (selected) {
                                        selectedAlbumId = null
                                        selectedArtistName = null
                                        listResetVersion += 1L
                                    } else {
                                        selectedTab = tab
                                        selectedAlbumId = null
                                        selectedArtistName = null
                                        listResetVersion += 1L
                                    }
                                }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = tab.iconResId),
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                                },
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = tab.label,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                                },
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                )
            }

            val searchBarContentColor = MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ElovaireRadii.input),
                    singleLine = true,
                    placeholder = { Text("Search library") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = trimmedQuery.isNotBlank(),
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
                                        onClick = { query = "" },
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val pickerContentKey = when {
                    selectedTab == PlaylistPickerTab.Albums && selectedAlbum != null -> "album:${selectedAlbum.id}"
                    selectedTab == PlaylistPickerTab.Artists && selectedArtistName != null -> "artist:${selectedArtistName.orEmpty()}"
                    else -> "tab:${selectedTab.name}"
                }
                ElovaireAnimatedContent(
                    targetState = pickerContentKey,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val forward = targetState > initialState
                        (fadeIn(
                            animationSpec = tween(
                                durationMillis = 120,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialAlpha = 0.86f,
                        ) + slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 160,
                                easing = FastOutSlowInEasing,
                            ),
                            initialOffsetX = { fullWidth ->
                                val offset = (fullWidth / 18f).roundToInt()
                                if (forward) offset else -offset
                            },
                        )) togetherWith (fadeOut(
                            animationSpec = tween(
                                durationMillis = 50,
                                easing = FastOutLinearInEasing,
                            ),
                            targetAlpha = 0.9f,
                        ) + slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 110,
                                easing = FastOutSlowInEasing,
                            ),
                            targetOffsetX = { fullWidth ->
                                val offset = (fullWidth / 22f).roundToInt()
                                if (forward) -offset else offset
                            },
                        ))
                    },
                    label = "PlaylistAddSongsContent",
                ) {
                    LazyColumn(
                        state = listState,
                        overscrollEffect = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .ensureSingleItemRubberBand(listState),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = overlayBottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        when {
                            selectedTab == PlaylistPickerTab.Albums && selectedAlbum != null -> {
                                items(filteredAlbumSongs, key = { it.id }) { song ->
                                    val selected = song.id in selectedSongIdSet
                                    SelectableSongRow(
                                        song = song,
                                        selected = selected,
                                        selectionIndicatorOnRight = true,
                                        showDivider = song != filteredAlbumSongs.lastOrNull(),
                                        onClick = {
                                            selectedSongIds = if (selected) {
                                                selectedSongIds.filterNot { it == song.id }
                                            } else {
                                                selectedSongIds + song.id
                                            }
                                        },
                                    )
                                }
                            }
                            selectedTab == PlaylistPickerTab.Artists && selectedArtistName != null -> {
                                items(filteredArtistSongs, key = { it.id }) { song ->
                                    val selected = song.id in selectedSongIdSet
                                    SelectableSongRow(
                                        song = song,
                                        selected = selected,
                                        selectionIndicatorOnRight = true,
                                        showDivider = song != filteredArtistSongs.lastOrNull(),
                                        onClick = {
                                            selectedSongIds = if (selected) {
                                                selectedSongIds.filterNot { it == song.id }
                                            } else {
                                                selectedSongIds + song.id
                                            }
                                        },
                                    )
                                }
                            }
                            selectedTab == PlaylistPickerTab.Albums -> {
                                items(filteredAlbums, key = { it.id }) { album ->
                                    SelectableAlbumPickerRow(
                                        album = album,
                                        selected = album.songs.all { it.id in selectedSongIdSet } && album.songs.isNotEmpty(),
                                        showDivider = album != filteredAlbums.lastOrNull(),
                                        onOpen = { selectedAlbumId = album.id },
                                        onToggleSelection = {
                                            val albumSongIds = album.songs.map(Song::id)
                                            val allSelected = albumSongIds.all { it in selectedSongIdSet }
                                            selectedSongIds = if (allSelected) {
                                                selectedSongIds.filterNot { it in albumSongIds }
                                            } else {
                                                (selectedSongIds + albumSongIds).distinct()
                                            }
                                        },
                                    )
                                }
                            }

                            selectedTab == PlaylistPickerTab.Artists -> {
                                itemsIndexed(filteredArtists, key = { _, item -> item.first.name }) { index, (artist, _) ->
                                    ArtistRow(
                                        artist = artist,
                                        onClick = { selectedArtistName = artist.name },
                                    )
                                    if (index != filteredArtists.lastIndex) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }

                            selectedTab == PlaylistPickerTab.Songs -> {
                                items(filteredSongs, key = { it.id }) { song ->
                                    val selected = song.id in selectedSongIdSet
                                    SelectableSongRow(
                                        song = song,
                                        selected = selected,
                                        selectionIndicatorOnRight = true,
                                        showDivider = song != filteredSongs.lastOrNull(),
                                        onClick = {
                                            selectedSongIds = if (selected) {
                                                selectedSongIds.filterNot { it == song.id }
                                            } else {
                                                selectedSongIds + song.id
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                FastScrollbar(
                    state = listState,
                    topInset = 0.dp,
                    bottomInset = overlayBottomPadding - 6.dp,
                )
            }
        }

        DetailListTopBar(
            title = when {
                selectedAlbum != null -> selectedAlbum.title
                selectedArtistName != null -> selectedArtistName.orEmpty()
                else -> miscPhrase(LocalAppLanguage.current, MiscPhrase.AddSongs)
            },
            subtitle = when (selectedSongIds.size) {
                0 -> when {
                    selectedAlbum != null -> selectedAlbum.artist
                    selectedArtistName != null -> miscPhrase(LocalAppLanguage.current, MiscPhrase.ChooseSongs)
                    else -> null
                }
                else -> localizedCountLabel(selectedSongIds.size, "song", LocalAppLanguage.current)
            },
            onBack = stableHandleBack,
            actions = topBarActions,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SelectableCollectionRow(
    title: String,
    subtitle: String,
    artUri: Uri?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(ElovaireRadii.tile),
        color = readableCardSurfaceColor(),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(62.dp)) {
                ArtworkImage(
                    uri = artUri,
                    title = title,
                    modifier = Modifier.matchParentSize(),
                    cornerRadius = ElovaireRadii.artworkSmall,
                )
                SelectionIndicatorIcon(
                    selected = selected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
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
private fun SelectableSongRow(
    song: Song,
    selected: Boolean,
    selectionIndicatorOnRight: Boolean = false,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.album,
                    modifier = Modifier.matchParentSize(),
                    cornerRadius = ElovaireRadii.artworkSmall,
                )
                if (!selectionIndicatorOnRight) {
                    SelectionIndicatorIcon(
                        selected = selected,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp),
                    )
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
                    color = readableSecondaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.width(if (selectionIndicatorOnRight) 72.dp else 40.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End,
                )
                if (selectionIndicatorOnRight) {
                    Box(contentAlignment = Alignment.Center) {
                        SelectionIndicatorIcon(selected = selected)
                    }
                }
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun SelectableAlbumPickerRow(
    album: Album,
    selected: Boolean,
    showDivider: Boolean = true,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpen,
                )
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier.size(62.dp),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleSelection,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                SelectionIndicatorIcon(selected = selected)
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
internal fun DividerLine(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
    modifier: Modifier = Modifier,
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
        modifier = modifier,
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
    actions: List<TopBarActionSpec> = emptyList(),
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
                actions = actions,
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
                        transitionSpec = { ElovaireMotion.titleSwapTransform() },
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
                        transitionSpec = { ElovaireMotion.titleSwapTransform() },
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
            if (actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions.forEach { action ->
                        HeaderIconButton(
                            iconResId = action.iconResId,
                            contentDescription = action.contentDescription,
                            showBackground = false,
                            onClick = action.onClick,
                            modifier = Modifier.zIndex(1f),
                        )
                    }
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
                Text(uiPhrase(LocalAppLanguage.current, UiPhrase.AddToPlaylist))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(uiPhrase(LocalAppLanguage.current, UiPhrase.Cancel))
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
    playlists: List<Playlist>,
    lyricsService: LyricsService,
    onBack: () -> Unit,
    onOpenCurrentAlbum: (Long) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddCurrentSongToPlaylist: (Long, Song) -> Unit,
    onCreatePlaylist: (String) -> Long,
    onQueueItemSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    onOpenEqualizer: () -> Unit,
    eqSettings: EqSettings,
    onSpaciousnessChanged: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val liveCurrentSong = playbackState.currentSong
    val liveDisplaySong = liveCurrentSong?.let { enrichedSongsById[it.id] ?: it }
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
            durationMillis = 360,
            easing = LinearOutSlowInEasing,
        )
    }
    val collapseSettleAnimationSpec = remember {
        tween<Float>(
            durationMillis = 260,
            easing = FastOutSlowInEasing,
        )
    }
    var interactiveTransitionProgress by remember(liveCurrentSong?.id) { mutableStateOf<Float?>(null) }
    var dismissAnimationRunning by remember(liveCurrentSong?.id) { mutableStateOf(false) }
    val effectiveTransitionProgress = interactiveTransitionProgress ?: transitionProgress.value
    val transitionInFlight = transitionState != PlayerOverlayTransitionState.Expanded || interactiveTransitionProgress != null || dismissAnimationRunning
    val adaptivePalette = remember(gradient, appBackground) {
        buildPlayerAdaptivePalette(
            gradient = gradient,
            appBackground = appBackground,
            darkTheme = false,
        )
    }
    val tintColor by animateColorAsState(
        targetValue = adaptivePalette.tintColor,
        animationSpec = tween(320, easing = LinearOutSlowInEasing),
        label = "player_tint_color",
    )
    val baseSurface by animateColorAsState(
        targetValue = adaptivePalette.backdropBase,
        animationSpec = tween(320, easing = LinearOutSlowInEasing),
        label = "player_backdrop_base",
    )
    val contentColor by animateColorAsState(
        targetValue = adaptivePalette.contentColor,
        animationSpec = tween(260, easing = LinearOutSlowInEasing),
        label = "player_content_color",
    )
    val secondaryContentColor by animateColorAsState(
        targetValue = adaptivePalette.secondaryContentColor,
        animationSpec = tween(260, easing = LinearOutSlowInEasing),
        label = "player_secondary_content_color",
    )
    val currentSong = liveCurrentSong
    val displaySong = liveDisplaySong
    val language = LocalAppLanguage.current
    val playingFromText = remember(language, playbackState.sourceLabel, currentSong?.album) {
        val source = playbackState.sourceLabel
            ?.takeIf { it.isNotBlank() }
            ?: currentSong?.album?.takeIf { it.isNotBlank() }
            ?: localizedAllSongsSource(language)
        "${playingFromPrefix(language)} $source"
    }
    var showLyricsSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var showQueueSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember(currentSong?.id) { mutableStateOf(false) }
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
            if (cached is LyricsResult.Found && cached.payload.isSynced) {
                return@produceState
            }
        }

        if (value !is LyricsUiState.Ready) {
            value = LyricsUiState.Loading
        }
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
                animationSpec = tween(80),
                label = "queue_favorite_alpha",
            )
            val transportAlpha by animateFloatAsState(
                targetValue = if (showQueueSheet) 0f else 1f,
                animationSpec = tween(80),
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
                                transitionSpec = { ElovaireMotion.quickContentSwapTransform() },
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
                            enter = fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                                slideInVertically(
                                    animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                    initialOffsetY = { it / 5 },
                                ),
                            exit = fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec()),
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
                                CompactQueuePlaybackSummary(
                                    playbackManager = playbackManager,
                                    currentSongId = currentSong.id,
                                    freezeUpdates = transitionInFlight,
                                    contentColor = contentColor,
                                    secondaryContentColor = secondaryContentColor,
                                    modifier = Modifier.fillMaxWidth(),
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
                            transitionSpec = { ElovaireMotion.quickContentSwapTransform() },
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
                val queueSheetTopExtension = 462.dp
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
                            NowPlayingProgressSummary(
                                playbackManager = playbackManager,
                                currentSongId = currentSong.id,
                                freezeUpdates = transitionInFlight,
                                format = displaySong?.audioFormat ?: currentSong.audioFormat,
                                quality = displaySong?.audioQuality ?: currentSong.audioQuality,
                                contentColor = contentColor,
                                secondaryContentColor = secondaryContentColor,
                                modifier = Modifier.fillMaxWidth(),
                            )

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
                            label = "",
                            iconSize = 20.dp,
                            tint = contentColor,
                            showBackground = false,
                            onClick = {
                                showQueueSheet = false
                                showAddToPlaylistDialog = false
                                showLyricsSheet = true
                            },
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        PlayerSecondaryActionButton(
                            iconResId = repeatModeIconRes(playbackState.repeatMode),
                            label = "",
                            iconSize = 20.dp,
                            tint = contentColor,
                            showBackground = playbackState.repeatMode != PlaybackRepeatMode.Off,
                            onClick = onCycleRepeatMode,
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        PlayerSecondaryActionButton(
                            iconResId = R.drawable.ic_lucide_plus,
                            label = "",
                            iconSize = 20.dp,
                            tint = contentColor,
                            showBackground = showAddToPlaylistDialog,
                            onClick = {
                                showLyricsSheet = false
                                showQueueSheet = false
                                showAddToPlaylistDialog = true
                            },
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        PlayerSecondaryActionButton(
                            iconResId = R.drawable.ic_lucide_list_music,
                            label = "",
                            iconSize = 20.dp,
                            tint = contentColor,
                            showBackground = showQueueSheet,
                            onClick = {
                                showLyricsSheet = false
                                showAddToPlaylistDialog = false
                                showQueueSheet = !showQueueSheet
                            },
                        )
                    }
                }

                if (showQueueSheet) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showQueueSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    enter = fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                        scaleIn(
                            initialScale = 0.94f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = ElovaireMotion.standardTween(
                                durationMillis = ElovaireMotion.Standard,
                                easing = FastOutSlowInEasing,
                            ),
                        ) +
                        slideInHorizontally(
                            initialOffsetX = { it / 14 },
                            animationSpec = ElovaireMotion.standardTween(
                                durationMillis = ElovaireMotion.Standard,
                                easing = FastOutSlowInEasing,
                            ),
                        ) +
                        slideInVertically(
                            initialOffsetY = { it / 14 },
                            animationSpec = ElovaireMotion.standardTween(
                                durationMillis = ElovaireMotion.Standard,
                                easing = FastOutSlowInEasing,
                            ),
                        ),
                    exit = fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec()) +
                        scaleOut(
                            targetScale = 0.98f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = ElovaireMotion.standardTween(
                                durationMillis = ElovaireMotion.Quick,
                            ),
                        ),
                ) {
                    QueueSheet(
                        queue = playbackState.queue,
                        currentIndex = playbackState.currentIndex,
                        playlists = playlists,
                        playlistSongsById = enrichedSongsById,
                        currentSong = currentSong,
                        tint = contentColor,
                        secondaryTint = secondaryContentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxHeight + queueSheetTopExtension)
                            .align(Alignment.BottomCenter),
                        onSongSelected = onQueueItemSelected,
                        onQueueItemRemoved = onQueueItemRemoved,
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
                        onOpenEqualizer = onOpenEqualizer,
                        onAddSongToPlaylist = onAddCurrentSongToPlaylist,
                        onCreatePlaylist = onCreatePlaylist,
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
            enter = fadeIn(animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Standard, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 12 },
                ) +
                scaleIn(
                    animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Standard, easing = FastOutSlowInEasing),
                    initialScale = 0.985f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
            exit = fadeOut(animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Quick, easing = FastOutLinearInEasing)) +
                slideOutVertically(
                    animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Quick, easing = FastOutSlowInEasing),
                    targetOffsetY = { it / 18 },
                ) +
                scaleOut(
                    animationSpec = ElovaireMotion.standardTween(durationMillis = ElovaireMotion.Quick, easing = FastOutLinearInEasing),
                    targetScale = 0.992f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ),
        ) {
            LyricsOverlay(
                song = currentSong,
                playbackManager = playbackManager,
                lyricsUiState = lyricsUiState,
                tintColor = baseSurface.copy(alpha = 0.66f),
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onSeekTo = playbackManager::seekTo,
                onHideLyrics = { showLyricsSheet = false },
            )
        }
        if (showAddToPlaylistDialog) {
            AddToPlaylistPickerDialog(
                playlists = playlists,
                playlistSongsById = enrichedSongsById,
                onDismiss = { showAddToPlaylistDialog = false },
                onPlaylistSelected = { playlistId ->
                    currentSong?.let { onAddCurrentSongToPlaylist(playlistId, it) }
                    showAddToPlaylistDialog = false
                },
                onCreatePlaylist = onCreatePlaylist,
            )
        }
    }
}

@Composable
private fun rememberRenderedPlaybackProgress(
    playbackManager: PlaybackManager,
    currentSongId: Long?,
    freezeUpdates: Boolean,
): PlaybackProgressState {
    val liveProgress by playbackManager.progressState.collectAsStateWithLifecycle()
    var frozenProgress by remember(currentSongId) {
        mutableStateOf(playbackManager.progressState.value)
    }
    LaunchedEffect(liveProgress, freezeUpdates, currentSongId) {
        if (!freezeUpdates) {
            frozenProgress = liveProgress
        }
    }
    return if (freezeUpdates) frozenProgress else liveProgress
}

@Composable
private fun CompactQueuePlaybackSummary(
    playbackManager: PlaybackManager,
    currentSongId: Long,
    freezeUpdates: Boolean,
    contentColor: Color,
    secondaryContentColor: Color,
    modifier: Modifier = Modifier,
) {
    val playbackProgress = rememberRenderedPlaybackProgress(
        playbackManager = playbackManager,
        currentSongId = currentSongId,
        freezeUpdates = freezeUpdates,
    )
    val progress = remember(playbackProgress.displayPositionMs, playbackProgress.durationMs) {
        if (playbackProgress.durationMs > 0L) {
            (playbackProgress.displayPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        CompactPlaybackProgressBar(
            progress = progress,
            contentColor = contentColor,
            modifier = Modifier.fillMaxWidth(),
        )
        CompactPlaybackTimingRow(
            displayedPositionMs = playbackProgress.displayPositionMs,
            durationMs = playbackProgress.durationMs,
            contentColor = contentColor,
            secondaryContentColor = secondaryContentColor,
        )
    }
}

@Composable
private fun NowPlayingProgressSummary(
    playbackManager: PlaybackManager,
    currentSongId: Long,
    freezeUpdates: Boolean,
    format: String,
    quality: String?,
    contentColor: Color,
    secondaryContentColor: Color,
    modifier: Modifier = Modifier,
) {
    val playbackProgress = rememberRenderedPlaybackProgress(
        playbackManager = playbackManager,
        currentSongId = currentSongId,
        freezeUpdates = freezeUpdates,
    )
    val progress = remember(playbackProgress.displayPositionMs, playbackProgress.durationMs) {
        if (playbackProgress.durationMs > 0L) {
            (playbackProgress.displayPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PlaybackProgressBar(
            progress = progress,
            isInteracting = playbackProgress.isUserScrubbing,
            contentColor = contentColor,
            onScrubStarted = playbackManager::beginScrub,
            onScrubFractionChanged = { fraction ->
                val target = fractionToDurationPosition(
                    fraction = fraction,
                    durationMs = playbackProgress.durationMs,
                )
                playbackManager.updateScrubPosition(target)
            },
            onScrubFinished = { fraction ->
                val target = fractionToDurationPosition(
                    fraction = fraction,
                    durationMs = playbackProgress.durationMs,
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
                    text = formatPlaybackPosition(playbackProgress.displayPositionMs),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = secondaryContentColor,
                )
            }
            SongFileInfoPill(
                format = format,
                quality = quality,
                tint = contentColor,
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = formatDuration(playbackProgress.durationMs),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = secondaryContentColor.copy(alpha = 0.7f),
                )
            }
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
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    currentSong: Song?,
    tint: Color,
    secondaryTint: Color,
    onSongSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    spaciousnessEnabled: Boolean,
    onToggleSpaciousness: () -> Unit,
    spaciousnessAmount: Float,
    onSpaciousnessAmountChanged: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onAddSongToPlaylist: (Long, Song) -> Unit,
    onCreatePlaylist: (String) -> Long,
    statusText: String?,
    onDismiss: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val listState = rememberElovaireLazyListState("equalizer_screen")
    var showSpaciousnessSlider by remember(spaciousnessEnabled) { mutableStateOf(spaciousnessEnabled) }
    var playlistTargetSong by remember(currentSong?.id, queue) { mutableStateOf<Song?>(null) }
    val footerExpanded = showSpaciousnessSlider || statusText != null
    val footerHeight by animateDpAsState(
        targetValue = if (footerExpanded) 90.dp else 60.dp,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
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
    Box(
        modifier = modifier
            .fillMaxWidth(),
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
                        .fillMaxWidth()
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
                            text = queueTitle(language),
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
                            text = localizedCountLabel(queue.size, "track", language),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                            color = secondaryTint.copy(alpha = 0.7f),
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(tint.copy(alpha = 0.1f))
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
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    itemsIndexed(queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                        QueueSongRow(
                            song = song,
                            active = index == currentIndex,
                            tint = tint,
                            secondaryTint = secondaryTint,
                            showDivider = false,
                            onClick = { onSongSelected(index) },
                            isPlaying = isPlaying,
                            onAddToPlaylist = { playlistTargetSong = song },
                            onRemoveFromQueue = { onQueueItemRemoved(index) },
                        )
                    }
                }
            }
            QueueSeparator(tint = tint, modifier = Modifier.fillMaxWidth())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(footerHeight),
            ) {
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                            slideInVertically(
                                animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                initialOffsetY = { it / 5 },
                            ) togetherWith
                            fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec())
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
                    enter = fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) + slideInVertically(
                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                        initialOffsetY = { it / 3 },
                    ),
                    exit = fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec()) + slideOutVertically(
                        animationSpec = ElovaireMotion.standardTween(
                            durationMillis = ElovaireMotion.Fast,
                            easing = FastOutLinearInEasing,
                        ),
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
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_wind,
                        label = "",
                        iconSize = 20.dp,
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
                    Spacer(modifier = Modifier.width(20.dp))
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_sliders_vertical,
                        label = "",
                        iconSize = 20.dp,
                        tint = tint,
                        showBackground = false,
                        onClick = {
                            onDismiss()
                            onOpenEqualizer()
                        },
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_plus,
                        label = "",
                        iconSize = 20.dp,
                        tint = tint,
                        showBackground = playlistTargetSong != null,
                        onClick = {
                            playlistTargetSong = currentSong
                        },
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_shuffle,
                        label = "",
                        iconSize = 20.dp,
                        tint = tint,
                        showBackground = shuffleEnabled,
                        onClick = onToggleShuffle,
                    )
                }
            }
        }
    }
    playlistTargetSong?.let { song ->
        AddToPlaylistPickerDialog(
            playlists = playlists,
            playlistSongsById = playlistSongsById,
            onDismiss = { playlistTargetSong = null },
            onPlaylistSelected = { playlistId ->
                onAddSongToPlaylist(playlistId, song)
                playlistTargetSong = null
            },
            onCreatePlaylist = onCreatePlaylist,
        )
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
    active: Boolean,
    isPlaying: Boolean,
    tint: Color,
    secondaryTint: Color,
    showDivider: Boolean,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromQueue: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (active) tint.copy(alpha = 0.1f) else Color.Transparent,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                    visible = active && isPlaying,
                    enter = fadeIn(animationSpec = tween(60)),
                    exit = fadeOut(animationSpec = tween(60)),
                ) {
                    PlaybackActiveArtworkOverlay(
                        uri = song.artUri,
                        title = song.album,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExplicitTitleText(
                    title = song.title,
                    isExplicit = song.isExplicit,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (active) tint else tint.copy(alpha = 0.84f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryTint.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryTint.copy(alpha = 0.78f),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(40.dp),
                )
                QueueSongOverflowMenuButton(
                    tint = tint,
                    onAddToPlaylist = onAddToPlaylist,
                    onRemoveFromQueue = onRemoveFromQueue,
                )
            }
        }
        if (showDivider) {
            QueueSeparator(
                tint = tint,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun QueueSongOverflowMenuButton(
    tint: Color,
    onAddToPlaylist: () -> Unit,
    onRemoveFromQueue: () -> Unit,
) {
    val language = LocalAppLanguage.current
    var expanded by remember { mutableStateOf(false) }
    var shouldRenderMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionDurationScale = rememberSystemAnimationScale()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "queue_song_overflow_scale",
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
                    onClick = { expanded = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_ellipsis_vertical),
                contentDescription = "Queue song options",
                tint = tint.copy(alpha = 0.82f),
                modifier = Modifier.size(OverflowMenuIconSize),
            )
        }

        if (shouldRenderMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                offset = OverflowMenuAnchorOffset,
                containerColor = Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = ElovaireMotion.contextMenuEnter(),
                    exit = ElovaireMotion.contextMenuExit(),
                    label = "QueueSongOverflowMenuVisibility",
                ) {
                    QueueContextMenuSurface(
                        modifier = Modifier.width(210.dp),
                    ) {
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_list_plus,
                            text = uiPhrase(language, UiPhrase.AddToPlaylist),
                            tint = tint,
                            onClick = {
                                expanded = false
                                onAddToPlaylist()
                            },
                        )
                        DividerLine()
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_list_x,
                            text = uiPhrase(language, UiPhrase.RemoveFromList),
                            tint = tint,
                            onClick = {
                                expanded = false
                                onRemoveFromQueue()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueContextMenuSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DynamicBackdropSurface(
        modifier = modifier,
        shape = RoundedCornerShape(ElovaireRadii.card),
        overlayAlpha = 0.1f,
        borderColor = blurSurfaceBorderColor(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
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
    iconSize: Dp = 20.dp,
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
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun AlbumHeaderPlayButton(
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
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
                text = playLabel(language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
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

private val OverflowMenuIconSize = 21.6.dp
private val OverflowMenuAnchorOffset = DpOffset(x = 4.dp, y = (-8).dp)

@Composable
private fun SongOverflowMenuButton(
    song: Song,
    tint: Color,
) {
    val actions = LocalSongMenuActions.current
    val language = LocalAppLanguage.current
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
                modifier = Modifier.size(OverflowMenuIconSize),
            )
        }

        if (shouldRenderMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                offset = OverflowMenuAnchorOffset,
                containerColor = Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = ElovaireMotion.contextMenuEnter(),
                    exit = ElovaireMotion.contextMenuExit(),
                    label = "SongOverflowMenuVisibility",
                ) {
                    FrostedContextMenuSurface(
                        modifier = Modifier.width(208.dp),
                    ) {
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_list_music,
                            text = uiPhrase(language, UiPhrase.AddToPlaylist),
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                expanded = false
                                showPlaylistDialog = true
                            },
                        )
                        DividerLine()
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_plus,
                            text = uiPhrase(language, UiPhrase.AddToQueue),
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                expanded = false
                                actions.onAddToQueue(song)
                            },
                        )
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_trash_2,
                            text = uiPhrase(language, actions.deletePhrase),
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
            playlistSongsById = actions.songsById,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                actions.onAddToPlaylist(playlistId, song)
                showPlaylistDialog = false
            },
            onCreatePlaylist = actions.onCreatePlaylist,
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
    val language = LocalAppLanguage.current
    val settingsCopy = remember(language) { settingsCopy(language) }
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
            enter = ElovaireMotion.contextMenuEnter(),
            exit = ElovaireMotion.contextMenuExit(),
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
                        text = settingsCopy.settings,
                        tint = MaterialTheme.colorScheme.onSurface,
                        topPadding = 8.dp,
                        onClick = onOpenSettings,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_audio_waveform,
                        text = settingsCopy.equalizer,
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenEqualizer,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_list,
                        text = settingsCopy.changelog,
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenChangelog,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_info,
                        text = uiPhrase(language, UiPhrase.About),
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
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
    onCreatePlaylist: ((String) -> Long)? = null,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = null,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = onPlaylistSelected,
        onCreatePlaylist = onCreatePlaylist,
    )
}

@Composable
private fun LyricsOverlay(
    song: Song?,
    playbackManager: PlaybackManager,
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
    val listState = rememberLazyListState()
    var autoScrollHeld by remember(song?.id) { mutableStateOf(false) }
    var autoScrollResumeJob by remember(song?.id) { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var userLyricsScrollActive by remember(song?.id) { mutableStateOf(false) }

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
                                LyricsReadyContent(
                                    playbackManager = playbackManager,
                                    song = song,
                                    payload = state.payload,
                                    listState = listState,
                                    autoScrollHeld = autoScrollHeld,
                                    setAutoScrollHeld = { autoScrollHeld = it },
                                    autoScrollResumeJob = autoScrollResumeJob,
                                    setAutoScrollResumeJob = { autoScrollResumeJob = it },
                                    setUserLyricsScrollActive = { userLyricsScrollActive = it },
                                    lyricsScrollObserver = lyricsScrollObserver,
                                    hideButtonArea = hideButtonArea,
                                    lyricsBottomBlurArea = lyricsBottomBlurArea,
                                    contentColor = contentColor,
                                    onSeekTo = onSeekTo,
                                    scope = scope,
                                )
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
private fun LyricsReadyContent(
    playbackManager: PlaybackManager,
    song: Song?,
    payload: LyricsPayload,
    listState: LazyListState,
    autoScrollHeld: Boolean,
    setAutoScrollHeld: (Boolean) -> Unit,
    autoScrollResumeJob: kotlinx.coroutines.Job?,
    setAutoScrollResumeJob: (kotlinx.coroutines.Job?) -> Unit,
    setUserLyricsScrollActive: (Boolean) -> Unit,
    lyricsScrollObserver: NestedScrollConnection,
    hideButtonArea: Dp,
    lyricsBottomBlurArea: Dp,
    contentColor: Color,
    onSeekTo: (Long) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val playbackProgress by playbackManager.progressState.collectAsStateWithLifecycle()
    val autoScrollCenterOffsetPx = with(LocalDensity.current) { 180.dp.roundToPx() }
    val activeLyricLineIndex by remember(payload, playbackProgress.displayPositionMs) {
        derivedStateOf {
            payload
                .takeIf { it.isSynced }
                ?.currentLineIndexAt(
                    positionMs = playbackProgress.displayPositionMs,
                    timingOffsetMs = 0L,
                    switchGraceMs = 0L,
                )
                ?: -1
        }
    }

    LaunchedEffect(activeLyricLineIndex, payload.isSynced, autoScrollHeld) {
        if (!autoScrollHeld && payload.isSynced && activeLyricLineIndex >= 0) {
            listState.animateLyricJumpToItem(
                index = activeLyricLineIndex,
                scrollOffset = -autoScrollCenterOffsetPx,
            )
        }
    }

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
            items = payload.lines,
            key = { _, line -> "${line.index}:${line.startTimeMs}:${line.text}" },
        ) { index, line ->
            val isActive = payload.isSynced && index == activeLyricLineIndex
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
                    .pointerInput(song?.id, payload.lines.size, payload.isSynced, activeLyricLineIndex) {
                        detectTapGestures {
                            lyricsSeekPositionMs(
                                lines = payload.lines,
                                index = index,
                                isSynced = payload.isSynced,
                            )?.let { seekPositionMs ->
                                setAutoScrollHeld(false)
                                setUserLyricsScrollActive(false)
                                autoScrollResumeJob?.cancel()
                                setAutoScrollResumeJob(null)
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

@Composable
private fun PlayerSecondaryActionButton(
    iconResId: Int,
    label: String,
    iconSize: Dp = 18.dp,
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
                modifier = Modifier.size(iconSize),
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
                    .background(contentColor.copy(alpha = 0.1f))
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
                        .background(contentColor.copy(alpha = 0.1f))
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
    state: androidx.compose.foundation.ScrollState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .matchParentSize(),
    ) {
        val viewportHeightPx = with(LocalDensity.current) { maxHeight.toPx() }.coerceAtLeast(1f)
        val scrollableContentHeightPx = state.maxValue.toFloat().coerceAtLeast(0f)
        if (scrollableContentHeightPx <= 0f) return@BoxWithConstraints

        val estimatedContentHeightPx = viewportHeightPx + scrollableContentHeightPx
        FastScrollbarTrack(
            scrollFraction = (state.value / scrollableContentHeightPx).coerceIn(0f, 1f),
            visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
            totalItems = 2,
            visibleItemsCount = 1,
            topInset = topInset,
            bottomInset = bottomInset,
            modifier = modifier,
            onJumpToFraction = { fraction ->
                state.scrollTo((scrollableContentHeightPx * fraction).roundToInt())
            },
        )
    }
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
    val motionDurationScale = rememberSystemAnimationScale()
    val animatedScrollFraction by animateFloatAsState(
        targetValue = if (isDragging) dragFraction.coerceIn(0f, 1f) else scrollFraction.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = ElovaireMotion.scaleDurationMillis(
                durationMillis = if (isDragging) 50 else 90,
                durationScale = motionDurationScale,
            ).toInt(),
        ),
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

internal fun Modifier.ensureSingleItemRubberBand(state: androidx.compose.foundation.lazy.LazyListState): Modifier = composed {
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

internal fun Modifier.ensureSingleItemRubberBand(state: LazyGridState): Modifier = composed {
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

private fun playingFromPrefix(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Odtwarzanie z"
    AppLanguage.ChineseSimplified -> "播放来源"
    AppLanguage.Czech -> "Přehrávání z"
    AppLanguage.Lithuanian -> "Groja iš"
    AppLanguage.Danish -> "Afspiller fra"
    AppLanguage.French -> "Lecture depuis"
    AppLanguage.German -> "Wiedergabe aus"
    AppLanguage.Dutch -> "Afspelen vanuit"
    AppLanguage.Norwegian -> "Spiller fra"
    AppLanguage.Swedish -> "Spelar från"
    AppLanguage.Spanish -> "Reproduciendo desde"
    AppLanguage.Portuguese -> "A reproduzir de"
    AppLanguage.Estonian -> "Esitamine allikast"
    AppLanguage.Greek -> "Αναπαραγωγή από"
    AppLanguage.Croatian -> "Reprodukcija iz"
    AppLanguage.Russian -> "Воспроизведение из"
    AppLanguage.Ukrainian -> "Відтворення з"
    AppLanguage.Latvian -> "Atskaņo no"
    AppLanguage.Italian -> "Riproduzione da"
    AppLanguage.Albanian -> "Duke luajtur nga"
    AppLanguage.Hindi -> "चल रहा है"
    AppLanguage.Hungarian -> "Lejátszás innen:"
    AppLanguage.Japanese -> "再生元"
    AppLanguage.Latin -> "Canitur ex"
    AppLanguage.Macedonian -> "Се репродуцира од"
    AppLanguage.Serbian -> "Репродукује се из"
    AppLanguage.Thai -> "กำลังเล่นจาก"
    AppLanguage.English -> "Playing from"
}

private fun localizedAllSongsSource(language: AppLanguage): String = when (language) {
    AppLanguage.English -> "all songs"
    else -> commonUiCopy(language).songs.lowercase()
}

private fun queueTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Kolejka"
    AppLanguage.ChineseSimplified -> "队列"
    AppLanguage.Czech -> "Fronta"
    AppLanguage.Lithuanian -> "Eilė"
    AppLanguage.Danish -> "Kø"
    AppLanguage.French -> "File"
    AppLanguage.German -> "Warteschlange"
    AppLanguage.Dutch -> "Wachtrij"
    AppLanguage.Norwegian -> "Kø"
    AppLanguage.Swedish -> "Kö"
    AppLanguage.Spanish -> "Cola"
    AppLanguage.Portuguese -> "Fila"
    AppLanguage.Estonian -> "Järjekord"
    AppLanguage.Greek -> "Ουρά"
    AppLanguage.Croatian -> "Red"
    AppLanguage.Russian -> "Очередь"
    AppLanguage.Ukrainian -> "Черга"
    AppLanguage.Latvian -> "Rinda"
    AppLanguage.Italian -> "Coda"
    AppLanguage.Albanian -> "Radha"
    AppLanguage.Hindi -> "कतार"
    AppLanguage.Hungarian -> "Sor"
    AppLanguage.Japanese -> "キュー"
    AppLanguage.Latin -> "Ordo"
    AppLanguage.Macedonian -> "Редица"
    AppLanguage.Serbian -> "Ред"
    AppLanguage.Thai -> "คิว"
    AppLanguage.English -> "Queue"
}

private fun playLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Odtwórz"
    AppLanguage.ChineseSimplified -> "播放"
    AppLanguage.Croatian -> "Reproduciraj"
    AppLanguage.Czech -> "Přehrát"
    AppLanguage.Danish -> "Afspil"
    AppLanguage.Dutch -> "Afspelen"
    AppLanguage.Estonian -> "Esita"
    AppLanguage.French -> "Lire"
    AppLanguage.German -> "Abspielen"
    AppLanguage.Greek -> "Αναπαραγωγή"
    AppLanguage.Hindi -> "चलाएँ"
    AppLanguage.Hungarian -> "Lejátszás"
    AppLanguage.Italian -> "Riproduci"
    AppLanguage.Japanese -> "再生"
    AppLanguage.Latin -> "Cane"
    AppLanguage.Latvian -> "Atskaņot"
    AppLanguage.Lithuanian -> "Leisti"
    AppLanguage.Macedonian -> "Пушти"
    AppLanguage.Norwegian -> "Spill av"
    AppLanguage.Portuguese -> "Reproduzir"
    AppLanguage.Russian -> "Играть"
    AppLanguage.Serbian -> "Пусти"
    AppLanguage.Spanish -> "Reproducir"
    AppLanguage.Swedish -> "Spela"
    AppLanguage.Thai -> "เล่น"
    AppLanguage.Ukrainian -> "Відтворити"
    AppLanguage.Albanian -> "Luaj"
    AppLanguage.English -> "Play"
}

private data class HomeUiCopy(
    val indexingTitle: String,
    val indexingMessage: String,
    val emptyLibraryTitle: String,
    val emptyLibraryMessage: String,
    val noRecentAdditionsTitle: String,
    val noRecentAdditionsMessage: String,
    val recentlyPlayedSongsTitle: String,
    val recentlyPlayedSongsEmpty: String,
    val favoriteAlbumsTitle: String,
    val favoriteAlbumsSubtitle: String,
    val noFavoriteAlbumsTitle: String,
    val noFavoriteAlbumsMessage: String,
)

private fun homeCopy(language: AppLanguage): HomeUiCopy = when (language) {
    AppLanguage.Polish -> HomeUiCopy(
        indexingTitle = "Trwa indeksowanie biblioteki",
        indexingMessage = "Utwory i albumy pojawią się po zakończeniu indeksowania",
        emptyLibraryTitle = "Nie znaleziono muzyki",
        emptyLibraryMessage = "Utwory i albumy pojawią się tutaj, gdy dodasz muzykę do domyślnego folderu Muzyka na urządzeniu",
        noRecentAdditionsTitle = "Brak ostatnio dodanych",
        noRecentAdditionsMessage = "Dodaj albumy do folderu Muzyka na urządzeniu, a najnowsze pojawią się tutaj automatycznie",
        recentlyPlayedSongsTitle = "Ostatnio odtwarzane utwory",
        recentlyPlayedSongsEmpty = "Utwory pojawią się tutaj wkrótce",
        favoriteAlbumsTitle = "Twoje ulubione albumy",
        favoriteAlbumsSubtitle = "Muzyka, do której często wracasz",
        noFavoriteAlbumsTitle = "Nie otwarto jeszcze żadnych albumów",
        noFavoriteAlbumsMessage = "Otwórz lub odtwórz dowolny album, a pojawi się tutaj z okładką na pierwszym planie",
    )
    AppLanguage.ChineseSimplified -> HomeUiCopy(
        indexingTitle = "正在索引媒体库",
        indexingMessage = "索引完成后，这里会显示歌曲和专辑",
        emptyLibraryTitle = "未找到音乐",
        emptyLibraryMessage = "当你将音乐添加到设备默认的 Music 文件夹后，这里会显示歌曲和专辑",
        noRecentAdditionsTitle = "还没有最近添加内容",
        noRecentAdditionsMessage = "将专辑添加到设备的 Music 文件夹后，最新内容会自动显示在这里",
        recentlyPlayedSongsTitle = "最近播放的歌曲",
        recentlyPlayedSongsEmpty = "歌曲很快就会显示在这里",
        favoriteAlbumsTitle = "你喜爱的专辑",
        favoriteAlbumsSubtitle = "你会经常回听的音乐",
        noFavoriteAlbumsTitle = "还没有打开过任何专辑",
        noFavoriteAlbumsMessage = "打开或播放任意专辑后，它就会带着封面显示在这里",
    )
    AppLanguage.Croatian -> HomeUiCopy(
        indexingTitle = "Indeksiranje biblioteke",
        indexingMessage = "Pjesme i albumi pojavit će se kada indeksiranje završi",
        emptyLibraryTitle = "Nije pronađena glazba",
        emptyLibraryMessage = "Pjesme i albumi pojavit će se ovdje kada dodate glazbu u zadanu mapu Music na uređaju",
        noRecentAdditionsTitle = "Nema nedavnih dodataka",
        noRecentAdditionsMessage = "Dodajte albume u mapu Music na uređaju i najnoviji će se ovdje automatski pojaviti",
        recentlyPlayedSongsTitle = "Nedavno reproducirane pjesme",
        recentlyPlayedSongsEmpty = "Pjesme će se ovdje uskoro pojaviti",
        favoriteAlbumsTitle = "Vaši omiljeni albumi",
        favoriteAlbumsSubtitle = "Glazba kojoj se često vraćate",
        noFavoriteAlbumsTitle = "Još nijedan album nije otvoren",
        noFavoriteAlbumsMessage = "Otvorite ili reproducirajte bilo koji album i ovdje će se pojaviti s naslovnicom u prvom planu",
    )
    AppLanguage.Czech -> HomeUiCopy(
        indexingTitle = "Indexuje se knihovna",
        indexingMessage = "Skladby a alba se zobrazí po dokončení indexace",
        emptyLibraryTitle = "Nebyla nalezena žádná hudba",
        emptyLibraryMessage = "Skladby a alba se zde zobrazí, jakmile přidáte hudbu do výchozí složky Music v zařízení",
        noRecentAdditionsTitle = "Zatím nic nového",
        noRecentAdditionsMessage = "Přidejte alba do složky Music v zařízení a nejnovější se zde objeví automaticky",
        recentlyPlayedSongsTitle = "Nedávno přehrávané skladby",
        recentlyPlayedSongsEmpty = "Skladby se zde brzy objeví",
        favoriteAlbumsTitle = "Vaše oblíbená alba",
        favoriteAlbumsSubtitle = "Hudba, ke které se často vracíte",
        noFavoriteAlbumsTitle = "Zatím nebyla otevřena žádná alba",
        noFavoriteAlbumsMessage = "Otevřete nebo přehrajte libovolné album a zobrazí se zde s obalem v popředí",
    )
    AppLanguage.Danish -> HomeUiCopy(
        indexingTitle = "Indekserer bibliotek",
        indexingMessage = "Sange og album vises, når indekseringen er færdig",
        emptyLibraryTitle = "Ingen musik fundet",
        emptyLibraryMessage = "Sange og album vises her, når du føjer musik til enhedens standardmappe Music",
        noRecentAdditionsTitle = "Ingen nylige tilføjelser endnu",
        noRecentAdditionsMessage = "Tilføj album til enhedens Music-mappe, så vises de nyeste automatisk her",
        recentlyPlayedSongsTitle = "Nyligt afspillede sange",
        recentlyPlayedSongsEmpty = "Sange vises snart her",
        favoriteAlbumsTitle = "Dine favoritalbum",
        favoriteAlbumsSubtitle = "Musik du ofte vender tilbage til",
        noFavoriteAlbumsTitle = "Ingen album er åbnet endnu",
        noFavoriteAlbumsMessage = "Åbn eller afspil et album, så vises det her med omslaget i centrum",
    )
    AppLanguage.Dutch -> HomeUiCopy(
        indexingTitle = "Bibliotheek wordt geïndexeerd",
        indexingMessage = "Nummers en albums verschijnen zodra het indexeren klaar is",
        emptyLibraryTitle = "Geen muziek gevonden",
        emptyLibraryMessage = "Nummers en albums verschijnen hier zodra je muziek toevoegt aan de standaardmap Music op je apparaat",
        noRecentAdditionsTitle = "Nog geen recente toevoegingen",
        noRecentAdditionsMessage = "Voeg albums toe aan de Music-map van je apparaat en de nieuwste verschijnen hier automatisch",
        recentlyPlayedSongsTitle = "Recent afgespeelde nummers",
        recentlyPlayedSongsEmpty = "Nummers verschijnen hier binnenkort",
        favoriteAlbumsTitle = "Je favoriete albums",
        favoriteAlbumsSubtitle = "Muziek waar je vaak naar terugkeert",
        noFavoriteAlbumsTitle = "Er zijn nog geen albums geopend",
        noFavoriteAlbumsMessage = "Open of speel een album af en het verschijnt hier met de hoes prominent in beeld",
    )
    AppLanguage.Estonian -> HomeUiCopy(
        indexingTitle = "Teeki indekseeritakse",
        indexingMessage = "Lood ja albumid kuvatakse pärast indekseerimise lõppu",
        emptyLibraryTitle = "Muusikat ei leitud",
        emptyLibraryMessage = "Lood ja albumid ilmuvad siia, kui lisate muusikat seadme vaikimisi Music kausta",
        noRecentAdditionsTitle = "Hiljutisi lisamisi veel pole",
        noRecentAdditionsMessage = "Lisage albumid seadme Music kausta ja uusimad ilmuvad siia automaatselt",
        recentlyPlayedSongsTitle = "Hiljuti esitatud lood",
        recentlyPlayedSongsEmpty = "Lood ilmuvad siia varsti",
        favoriteAlbumsTitle = "Sinu lemmikalbumid",
        favoriteAlbumsSubtitle = "Muusika, mille juurde tihti tagasi pöördud",
        noFavoriteAlbumsTitle = "Ühtegi albumit pole veel avatud",
        noFavoriteAlbumsMessage = "Ava või esita mõni album ning see ilmub siia koos esikaanega",
    )
    AppLanguage.French -> HomeUiCopy(
        indexingTitle = "Indexation de la bibliothèque",
        indexingMessage = "Les morceaux et les albums apparaîtront une fois l’indexation terminée",
        emptyLibraryTitle = "Aucune musique trouvée",
        emptyLibraryMessage = "Les morceaux et les albums apparaîtront ici dès que vous ajouterez de la musique au dossier Music par défaut de l’appareil",
        noRecentAdditionsTitle = "Aucun ajout récent",
        noRecentAdditionsMessage = "Ajoutez des albums au dossier Music de l’appareil et les plus récents apparaîtront ici automatiquement",
        recentlyPlayedSongsTitle = "Morceaux récemment lus",
        recentlyPlayedSongsEmpty = "Les morceaux apparaîtront bientôt ici",
        favoriteAlbumsTitle = "Vos albums favoris",
        favoriteAlbumsSubtitle = "La musique vers laquelle vous revenez souvent",
        noFavoriteAlbumsTitle = "Aucun album n’a encore été ouvert",
        noFavoriteAlbumsMessage = "Ouvrez ou lisez un album et il apparaîtra ici avec sa pochette bien en évidence",
    )
    AppLanguage.German -> HomeUiCopy(
        indexingTitle = "Bibliothek wird indiziert",
        indexingMessage = "Songs und Alben erscheinen nach Abschluss der Indizierung",
        emptyLibraryTitle = "Keine Musik gefunden",
        emptyLibraryMessage = "Songs und Alben erscheinen hier, sobald du Musik zum Standardordner Music auf deinem Gerät hinzufügst",
        noRecentAdditionsTitle = "Noch keine Neuheiten",
        noRecentAdditionsMessage = "Füge Alben zum Music-Ordner deines Geräts hinzu, dann erscheinen die neuesten hier automatisch",
        recentlyPlayedSongsTitle = "Zuletzt gespielte Songs",
        recentlyPlayedSongsEmpty = "Songs werden hier bald angezeigt",
        favoriteAlbumsTitle = "Deine Lieblingsalben",
        favoriteAlbumsSubtitle = "Musik, zu der du oft zurückkehrst",
        noFavoriteAlbumsTitle = "Noch keine Alben geöffnet",
        noFavoriteAlbumsMessage = "Öffne oder spiele ein Album ab und es erscheint hier mit dem Cover im Mittelpunkt",
    )
    AppLanguage.Greek -> HomeUiCopy(
        indexingTitle = "Γίνεται ευρετηρίαση της βιβλιοθήκης",
        indexingMessage = "Τα τραγούδια και τα άλμπουμ θα εμφανιστούν όταν ολοκληρωθεί η ευρετηρίαση",
        emptyLibraryTitle = "Δεν βρέθηκε μουσική",
        emptyLibraryMessage = "Τα τραγούδια και τα άλμπουμ θα εμφανιστούν εδώ όταν προσθέσετε μουσική στον προεπιλεγμένο φάκελο Music της συσκευής",
        noRecentAdditionsTitle = "Δεν υπάρχουν πρόσφατες προσθήκες",
        noRecentAdditionsMessage = "Προσθέστε άλμπουμ στον φάκελο Music της συσκευής και τα νεότερα θα εμφανίζονται εδώ αυτόματα",
        recentlyPlayedSongsTitle = "Τραγούδια που παίχτηκαν πρόσφατα",
        recentlyPlayedSongsEmpty = "Τα τραγούδια θα εμφανιστούν εδώ σύντομα",
        favoriteAlbumsTitle = "Τα αγαπημένα σας άλμπουμ",
        favoriteAlbumsSubtitle = "Μουσική στην οποία επιστρέφετε συχνά",
        noFavoriteAlbumsTitle = "Δεν έχει ανοίξει ακόμη κανένα άλμπουμ",
        noFavoriteAlbumsMessage = "Ανοίξτε ή αναπαράγετε οποιοδήποτε άλμπουμ και θα εμφανιστεί εδώ με το εξώφυλλό του μπροστά",
    )
    AppLanguage.Hindi -> HomeUiCopy(
        indexingTitle = "लाइब्रेरी इंडेक्स की जा रही है",
        indexingMessage = "इंडेक्स पूरा होने पर गाने और एल्बम यहाँ दिखेंगे",
        emptyLibraryTitle = "कोई संगीत नहीं मिला",
        emptyLibraryMessage = "जब आप अपने डिवाइस के डिफ़ॉल्ट Music फ़ोल्डर में संगीत जोड़ेंगे, तब गाने और एल्बम यहाँ दिखेंगे",
        noRecentAdditionsTitle = "अभी तक कोई हालिया जोड़ नहीं",
        noRecentAdditionsMessage = "डिवाइस के Music फ़ोल्डर में एल्बम जोड़ें और नए एल्बम यहाँ अपने आप दिखेंगे",
        recentlyPlayedSongsTitle = "हाल ही में चलाए गए गाने",
        recentlyPlayedSongsEmpty = "गाने यहाँ जल्द दिखाई देंगे",
        favoriteAlbumsTitle = "आपके पसंदीदा एल्बम",
        favoriteAlbumsSubtitle = "वह संगीत जिसे आप बार-बार सुनते हैं",
        noFavoriteAlbumsTitle = "अभी तक कोई एल्बम नहीं खोला गया",
        noFavoriteAlbumsMessage = "कोई भी एल्बम खोलें या चलाएँ, वह यहाँ अपने कवर के साथ दिखाई देगा",
    )
    AppLanguage.Hungarian -> HomeUiCopy(
        indexingTitle = "A könyvtár indexelése folyamatban",
        indexingMessage = "A dalok és albumok az indexelés befejezése után jelennek meg",
        emptyLibraryTitle = "Nem található zene",
        emptyLibraryMessage = "A dalok és albumok itt jelennek meg, amikor zenét ad hozzá az eszköz alapértelmezett Music mappájához",
        noRecentAdditionsTitle = "Még nincsenek friss hozzáadások",
        noRecentAdditionsMessage = "Adjon albumokat az eszköz Music mappájához, és a legújabbak automatikusan itt jelennek meg",
        recentlyPlayedSongsTitle = "Nemrég lejátszott dalok",
        recentlyPlayedSongsEmpty = "A dalok hamarosan itt jelennek meg",
        favoriteAlbumsTitle = "Kedvenc albumai",
        favoriteAlbumsSubtitle = "Zene, amelyhez gyakran visszatér",
        noFavoriteAlbumsTitle = "Még nem nyitott meg albumot",
        noFavoriteAlbumsMessage = "Nyisson meg vagy játsszon le egy albumot, és az itt jelenik meg a borítójával középpontban",
    )
    AppLanguage.Italian -> HomeUiCopy(
        indexingTitle = "Indicizzazione libreria in corso",
        indexingMessage = "Brani e album appariranno quando l’indicizzazione sarà completata",
        emptyLibraryTitle = "Nessuna musica trovata",
        emptyLibraryMessage = "Brani e album appariranno qui quando aggiungerai musica alla cartella Music predefinita del dispositivo",
        noRecentAdditionsTitle = "Nessuna aggiunta recente",
        noRecentAdditionsMessage = "Aggiungi album alla cartella Music del dispositivo e i più recenti appariranno qui automaticamente",
        recentlyPlayedSongsTitle = "Brani ascoltati di recente",
        recentlyPlayedSongsEmpty = "I brani appariranno qui presto",
        favoriteAlbumsTitle = "I tuoi album preferiti",
        favoriteAlbumsSubtitle = "La musica a cui torni spesso",
        noFavoriteAlbumsTitle = "Nessun album è stato ancora aperto",
        noFavoriteAlbumsMessage = "Apri o riproduci un album e apparirà qui con la copertina in primo piano",
    )
    AppLanguage.Japanese -> HomeUiCopy(
        indexingTitle = "ライブラリを索引中です",
        indexingMessage = "索引が完了すると、曲とアルバムがここに表示されます",
        emptyLibraryTitle = "音楽が見つかりませんでした",
        emptyLibraryMessage = "デバイスの既定の Music フォルダに音楽を追加すると、曲とアルバムがここに表示されます",
        noRecentAdditionsTitle = "最近追加された項目はまだありません",
        noRecentAdditionsMessage = "デバイスの Music フォルダにアルバムを追加すると、最新のものがここに自動で表示されます",
        recentlyPlayedSongsTitle = "最近再生した曲",
        recentlyPlayedSongsEmpty = "曲はまもなくここに表示されます",
        favoriteAlbumsTitle = "お気に入りのアルバム",
        favoriteAlbumsSubtitle = "何度も聴きたくなる音楽",
        noFavoriteAlbumsTitle = "まだアルバムは開かれていません",
        noFavoriteAlbumsMessage = "アルバムを開くか再生すると、そのアートワークとともにここに表示されます",
    )
    AppLanguage.Latin -> HomeUiCopy(
        indexingTitle = "Bibliotheca indicatur",
        indexingMessage = "Cantus et albumina hic apparebunt post indicem confectum",
        emptyLibraryTitle = "Nulla musica inventa est",
        emptyLibraryMessage = "Cantus et albumina hic apparebunt cum musicam in folder Music praeordinatum addideris",
        noRecentAdditionsTitle = "Nullae recentes additiones",
        noRecentAdditionsMessage = "Albumina ad folder Music adde et novissima hic sponte apparebunt",
        recentlyPlayedSongsTitle = "Cantus nuper acti",
        recentlyPlayedSongsEmpty = "Cantus hic mox apparebunt",
        favoriteAlbumsTitle = "Albumina tua dilecta",
        favoriteAlbumsSubtitle = "Musica ad quam saepe redis",
        noFavoriteAlbumsTitle = "Nullum album adhuc apertum est",
        noFavoriteAlbumsMessage = "Aperi vel cane quodlibet album et hic apparebit cum imagine principali",
    )
    AppLanguage.Latvian -> HomeUiCopy(
        indexingTitle = "Bibliotēka tiek indeksēta",
        indexingMessage = "Dziesmas un albumi parādīsies, kad indeksēšana būs pabeigta",
        emptyLibraryTitle = "Mūzika netika atrasta",
        emptyLibraryMessage = "Dziesmas un albumi parādīsies šeit, kad pievienosiet mūziku ierīces noklusējuma Music mapei",
        noRecentAdditionsTitle = "Vēl nav nesenu papildinājumu",
        noRecentAdditionsMessage = "Pievienojiet albumus ierīces Music mapei, un jaunākie šeit parādīsies automātiski",
        recentlyPlayedSongsTitle = "Nesen atskaņotās dziesmas",
        recentlyPlayedSongsEmpty = "Dziesmas drīz parādīsies šeit",
        favoriteAlbumsTitle = "Jūsu iecienītie albumi",
        favoriteAlbumsSubtitle = "Mūzika, pie kuras bieži atgriežaties",
        noFavoriteAlbumsTitle = "Vēl nav atvērts neviens albums",
        noFavoriteAlbumsMessage = "Atveriet vai atskaņojiet jebkuru albumu, un tas šeit parādīsies ar vāciņu priekšplānā",
    )
    AppLanguage.Lithuanian -> HomeUiCopy(
        indexingTitle = "Indeksuojama biblioteka",
        indexingMessage = "Dainos ir albumai čia pasirodys, kai indeksavimas bus baigtas",
        emptyLibraryTitle = "Muzikos nerasta",
        emptyLibraryMessage = "Dainos ir albumai čia pasirodys, kai pridėsite muziką į numatytąjį įrenginio Music aplanką",
        noRecentAdditionsTitle = "Neseniai pridėtų dar nėra",
        noRecentAdditionsMessage = "Pridėkite albumų į įrenginio Music aplanką, ir naujausi čia pasirodys automatiškai",
        recentlyPlayedSongsTitle = "Neseniai grotos dainos",
        recentlyPlayedSongsEmpty = "Dainos netrukus pasirodys čia",
        favoriteAlbumsTitle = "Jūsų mėgstami albumai",
        favoriteAlbumsSubtitle = "Muzika, prie kurios dažnai grįžtate",
        noFavoriteAlbumsTitle = "Dar neatidarytas nė vienas albumas",
        noFavoriteAlbumsMessage = "Atidarykite arba paleiskite bet kurį albumą, ir jis čia pasirodys su viršeliu priekyje",
    )
    AppLanguage.Macedonian -> HomeUiCopy(
        indexingTitle = "Библиотеката се индексира",
        indexingMessage = "Песните и албумите ќе се појават кога индексирањето ќе заврши",
        emptyLibraryTitle = "Не е пронајдена музика",
        emptyLibraryMessage = "Песните и албумите ќе се појават тука кога ќе додадете музика во стандардната папка Music на уредот",
        noRecentAdditionsTitle = "Сѐ уште нема неодамнешни додатоци",
        noRecentAdditionsMessage = "Додајте албуми во папката Music на уредот и најновите автоматски ќе се појават тука",
        recentlyPlayedSongsTitle = "Неодамна пуштени песни",
        recentlyPlayedSongsEmpty = "Песните наскоро ќе се појават тука",
        favoriteAlbumsTitle = "Вашите омилени албуми",
        favoriteAlbumsSubtitle = "Музика на која често ѝ се враќате",
        noFavoriteAlbumsTitle = "Сѐ уште не е отворен ниту еден албум",
        noFavoriteAlbumsMessage = "Отворете или пуштете кој било албум и ќе се појави тука со корицата во преден план",
    )
    AppLanguage.Norwegian -> HomeUiCopy(
        indexingTitle = "Biblioteket indekseres",
        indexingMessage = "Sanger og album vises når indekseringen er ferdig",
        emptyLibraryTitle = "Ingen musikk funnet",
        emptyLibraryMessage = "Sanger og album vises her når du legger til musikk i enhetens standardmappe Music",
        noRecentAdditionsTitle = "Ingen nylige tillegg ennå",
        noRecentAdditionsMessage = "Legg til album i enhetens Music-mappe, så vises de nyeste automatisk her",
        recentlyPlayedSongsTitle = "Nylig spilte sanger",
        recentlyPlayedSongsEmpty = "Sanger vises her snart",
        favoriteAlbumsTitle = "Dine favorittalbum",
        favoriteAlbumsSubtitle = "Musikk du ofte vender tilbake til",
        noFavoriteAlbumsTitle = "Ingen album er åpnet ennå",
        noFavoriteAlbumsMessage = "Åpne eller spill av et album, så vises det her med omslaget i sentrum",
    )
    AppLanguage.Portuguese -> HomeUiCopy(
        indexingTitle = "A indexar biblioteca",
        indexingMessage = "As músicas e os álbuns aparecerão quando a indexação terminar",
        emptyLibraryTitle = "Nenhuma música encontrada",
        emptyLibraryMessage = "As músicas e os álbuns aparecerão aqui quando adicionar música à pasta Music predefinida do dispositivo",
        noRecentAdditionsTitle = "Ainda não há adições recentes",
        noRecentAdditionsMessage = "Adicione álbuns à pasta Music do dispositivo e os mais recentes aparecerão aqui automaticamente",
        recentlyPlayedSongsTitle = "Músicas reproduzidas recentemente",
        recentlyPlayedSongsEmpty = "As músicas aparecerão aqui em breve",
        favoriteAlbumsTitle = "Os seus álbuns favoritos",
        favoriteAlbumsSubtitle = "Música à qual volta com frequência",
        noFavoriteAlbumsTitle = "Ainda não foi aberto nenhum álbum",
        noFavoriteAlbumsMessage = "Abra ou reproduza qualquer álbum e ele aparecerá aqui com a capa em destaque",
    )
    AppLanguage.Russian -> HomeUiCopy(
        indexingTitle = "Идёт индексирование библиотеки",
        indexingMessage = "Песни и альбомы появятся после завершения индексирования",
        emptyLibraryTitle = "Музыка не найдена",
        emptyLibraryMessage = "Песни и альбомы появятся здесь, когда вы добавите музыку в стандартную папку Music на устройстве",
        noRecentAdditionsTitle = "Пока нет недавних добавлений",
        noRecentAdditionsMessage = "Добавьте альбомы в папку Music на устройстве, и новейшие автоматически появятся здесь",
        recentlyPlayedSongsTitle = "Недавно воспроизведённые песни",
        recentlyPlayedSongsEmpty = "Песни скоро появятся здесь",
        favoriteAlbumsTitle = "Ваши любимые альбомы",
        favoriteAlbumsSubtitle = "Музыка, к которой вы часто возвращаетесь",
        noFavoriteAlbumsTitle = "Пока не был открыт ни один альбом",
        noFavoriteAlbumsMessage = "Откройте или включите любой альбом, и он появится здесь с обложкой на первом плане",
    )
    AppLanguage.Serbian -> HomeUiCopy(
        indexingTitle = "Библиотека се индексира",
        indexingMessage = "Песме и албуми ће се појавити када се индексирање заврши",
        emptyLibraryTitle = "Музика није пронађена",
        emptyLibraryMessage = "Песме и албуми ће се појавити овде када додате музику у подразумевани Music фолдер на уређају",
        noRecentAdditionsTitle = "Још нема недавних додавања",
        noRecentAdditionsMessage = "Додајте албуме у Music фолдер уређаја и најновији ће се овде појавити аутоматски",
        recentlyPlayedSongsTitle = "Недавно пуштане песме",
        recentlyPlayedSongsEmpty = "Песме ће се овде ускоро појавити",
        favoriteAlbumsTitle = "Ваши омиљени албуми",
        favoriteAlbumsSubtitle = "Музика којој се често враћате",
        noFavoriteAlbumsTitle = "Још није отворен ниједан албум",
        noFavoriteAlbumsMessage = "Отворите или пустите било који албум и појавиће се овде са омотом у првом плану",
    )
    AppLanguage.Spanish -> HomeUiCopy(
        indexingTitle = "Indexando la biblioteca",
        indexingMessage = "Las canciones y los álbumes aparecerán cuando termine la indexación",
        emptyLibraryTitle = "No se encontró música",
        emptyLibraryMessage = "Las canciones y los álbumes aparecerán aquí cuando añadas música a la carpeta Music predeterminada del dispositivo",
        noRecentAdditionsTitle = "Aún no hay añadidos recientes",
        noRecentAdditionsMessage = "Añade álbumes a la carpeta Music del dispositivo y los más recientes aparecerán aquí automáticamente",
        recentlyPlayedSongsTitle = "Canciones reproducidas recientemente",
        recentlyPlayedSongsEmpty = "Las canciones aparecerán aquí pronto",
        favoriteAlbumsTitle = "Tus álbumes favoritos",
        favoriteAlbumsSubtitle = "La música a la que vuelves con frecuencia",
        noFavoriteAlbumsTitle = "Aún no se ha abierto ningún álbum",
        noFavoriteAlbumsMessage = "Abre o reproduce cualquier álbum y aparecerá aquí con su portada en primer plano",
    )
    AppLanguage.Swedish -> HomeUiCopy(
        indexingTitle = "Biblioteket indexeras",
        indexingMessage = "Låtar och album visas när indexeringen är klar",
        emptyLibraryTitle = "Ingen musik hittades",
        emptyLibraryMessage = "Låtar och album visas här när du lägger till musik i enhetens standardmapp Music",
        noRecentAdditionsTitle = "Inga nyliga tillägg ännu",
        noRecentAdditionsMessage = "Lägg till album i enhetens Music-mapp så visas de senaste här automatiskt",
        recentlyPlayedSongsTitle = "Nyligen spelade låtar",
        recentlyPlayedSongsEmpty = "Låtar visas här snart",
        favoriteAlbumsTitle = "Dina favoritalbum",
        favoriteAlbumsSubtitle = "Musik du ofta återvänder till",
        noFavoriteAlbumsTitle = "Inga album har öppnats ännu",
        noFavoriteAlbumsMessage = "Öppna eller spela ett album så visas det här med omslaget i fokus",
    )
    AppLanguage.Thai -> HomeUiCopy(
        indexingTitle = "กำลังจัดทำดัชนีคลังเพลง",
        indexingMessage = "เพลงและอัลบั้มจะปรากฏเมื่อการจัดทำดัชนีเสร็จสิ้น",
        emptyLibraryTitle = "ไม่พบเพลง",
        emptyLibraryMessage = "เพลงและอัลบั้มจะปรากฏที่นี่เมื่อคุณเพิ่มเพลงลงในโฟลเดอร์ Music เริ่มต้นของอุปกรณ์",
        noRecentAdditionsTitle = "ยังไม่มีสิ่งที่เพิ่มล่าสุด",
        noRecentAdditionsMessage = "เพิ่มอัลบั้มลงในโฟลเดอร์ Music ของอุปกรณ์ แล้วรายการล่าสุดจะปรากฏที่นี่โดยอัตโนมัติ",
        recentlyPlayedSongsTitle = "เพลงที่เล่นล่าสุด",
        recentlyPlayedSongsEmpty = "เพลงจะปรากฏที่นี่ในไม่ช้า",
        favoriteAlbumsTitle = "อัลบั้มโปรดของคุณ",
        favoriteAlbumsSubtitle = "เพลงที่คุณกลับมาฟังบ่อย ๆ",
        noFavoriteAlbumsTitle = "ยังไม่มีการเปิดอัลบั้ม",
        noFavoriteAlbumsMessage = "เปิดหรือเล่นอัลบั้มใดก็ได้ แล้วมันจะปรากฏที่นี่พร้อมปกอยู่ด้านหน้า",
    )
    AppLanguage.Ukrainian -> HomeUiCopy(
        indexingTitle = "Бібліотека індексується",
        indexingMessage = "Пісні та альбоми з’являться після завершення індексації",
        emptyLibraryTitle = "Музику не знайдено",
        emptyLibraryMessage = "Пісні та альбоми з’являться тут, коли ви додасте музику до стандартної папки Music на пристрої",
        noRecentAdditionsTitle = "Поки немає нещодавніх додавань",
        noRecentAdditionsMessage = "Додайте альбоми до папки Music на пристрої, і найновіші автоматично з’являться тут",
        recentlyPlayedSongsTitle = "Нещодавно відтворені пісні",
        recentlyPlayedSongsEmpty = "Пісні скоро з’являться тут",
        favoriteAlbumsTitle = "Ваші улюблені альбоми",
        favoriteAlbumsSubtitle = "Музика, до якої ви часто повертаєтесь",
        noFavoriteAlbumsTitle = "Ще не було відкрито жодного альбому",
        noFavoriteAlbumsMessage = "Відкрийте або відтворіть будь-який альбом, і він з’явиться тут зі своєю обкладинкою в центрі уваги",
    )
    AppLanguage.Albanian -> HomeUiCopy(
        indexingTitle = "Biblioteka po indeksohet",
        indexingMessage = "Këngët dhe albumet do të shfaqen pasi të përfundojë indeksimi",
        emptyLibraryTitle = "Nuk u gjet muzikë",
        emptyLibraryMessage = "Këngët dhe albumet do të shfaqen këtu pasi të shtoni muzikë në dosjen e parazgjedhur Music të pajisjes",
        noRecentAdditionsTitle = "Ende s’ka shtesa të fundit",
        noRecentAdditionsMessage = "Shtoni albume në dosjen Music të pajisjes dhe më të rejat do të shfaqen këtu automatikisht",
        recentlyPlayedSongsTitle = "Këngë të luajtura së fundi",
        recentlyPlayedSongsEmpty = "Këngët do të shfaqen këtu së shpejti",
        favoriteAlbumsTitle = "Albumet tuaja të preferuara",
        favoriteAlbumsSubtitle = "Muzikë tek e cila ktheheni shpesh",
        noFavoriteAlbumsTitle = "Ende nuk është hapur asnjë album",
        noFavoriteAlbumsMessage = "Hapni ose luani cilindo album dhe ai do të shfaqet këtu me kopertinën në qendër",
    )
    AppLanguage.English -> HomeUiCopy(
        indexingTitle = "Indexing library",
        indexingMessage = "Songs and albums will show when indexing is done",
        emptyLibraryTitle = "No music was found",
        emptyLibraryMessage = "Songs and albums will show here as you add music to your device's default Music folder",
        noRecentAdditionsTitle = "No recent additions yet",
        noRecentAdditionsMessage = "Add albums to the device Music folder and the newest ones will appear here automatically",
        recentlyPlayedSongsTitle = "Recently played songs",
        recentlyPlayedSongsEmpty = "Songs will show up here soon",
        favoriteAlbumsTitle = "Your favorite albums",
        favoriteAlbumsSubtitle = "Music you come back to frequently",
        noFavoriteAlbumsTitle = "No albums have been opened yet",
        noFavoriteAlbumsMessage = "Open or play any album and it will appear here with its artwork front and center",
    )
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

private fun localizedCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String {
    val (singular, plural) = when (language) {
        AppLanguage.Albanian -> when (noun) {
            "song" -> "këngë" to "këngë"
            "track" -> "këngë" to "këngë"
            "album" -> "album" to "albume"
            "artist" -> "artist" to "artistë"
            "genre" -> "zhanër" to "zhanre"
            else -> noun to "${noun}e"
        }
        AppLanguage.ChineseSimplified -> noun to noun
        AppLanguage.Croatian -> when (noun) {
            "song" -> "pjesma" to "pjesme"
            "track" -> "pjesma" to "pjesme"
            "album" -> "album" to "albuma"
            "artist" -> "izvođač" to "izvođača"
            "genre" -> "žanr" to "žanra"
            else -> noun to "${noun}a"
        }
        AppLanguage.Czech -> when (noun) {
            "song" -> "skladba" to "skladby"
            "track" -> "skladba" to "skladby"
            "album" -> "album" to "alba"
            "artist" -> "umělec" to "umělci"
            "genre" -> "žánr" to "žánry"
            else -> noun to "${noun}y"
        }
        AppLanguage.Danish -> when (noun) {
            "song" -> "sang" to "sange"
            "track" -> "nummer" to "numre"
            "album" -> "album" to "albummer"
            "artist" -> "kunstner" to "kunstnere"
            "genre" -> "genre" to "genrer"
            else -> noun to "${noun}er"
        }
        AppLanguage.Dutch -> when (noun) {
            "song" -> "nummer" to "nummers"
            "track" -> "track" to "tracks"
            "album" -> "album" to "albums"
            "artist" -> "artiest" to "artiesten"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.Estonian -> when (noun) {
            "song" -> "lugu" to "lugu"
            "track" -> "lugu" to "lugu"
            "album" -> "album" to "albumit"
            "artist" -> "artist" to "artisti"
            "genre" -> "žanr" to "žanri"
            else -> noun to noun
        }
        AppLanguage.French -> when (noun) {
            "song" -> "morceau" to "morceaux"
            "track" -> "piste" to "pistes"
            "album" -> "album" to "albums"
            "artist" -> "artiste" to "artistes"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.German -> when (noun) {
            "song" -> "Titel" to "Titel"
            "track" -> "Track" to "Tracks"
            "album" -> "Album" to "Alben"
            "artist" -> "Künstler" to "Künstler"
            "genre" -> "Genre" to "Genres"
            else -> noun to "${noun}e"
        }
        AppLanguage.Greek -> when (noun) {
            "song" -> "τραγούδι" to "τραγούδια"
            "track" -> "κομμάτι" to "κομμάτια"
            "album" -> "άλμπουμ" to "άλμπουμ"
            "artist" -> "καλλιτέχνης" to "καλλιτέχνες"
            "genre" -> "είδος" to "είδη"
            else -> noun to noun
        }
        AppLanguage.Hindi -> when (noun) {
            "song" -> "गाना" to "गाने"
            "track" -> "ट्रैक" to "ट्रैक"
            "album" -> "एल्बम" to "एल्बम"
            "artist" -> "कलाकार" to "कलाकार"
            "genre" -> "शैली" to "शैलियाँ"
            else -> noun to noun
        }
        AppLanguage.Hungarian -> when (noun) {
            "song" -> "dal" to "dal"
            "track" -> "szám" to "szám"
            "album" -> "album" to "album"
            "artist" -> "előadó" to "előadó"
            "genre" -> "műfaj" to "műfaj"
            else -> noun to noun
        }
        AppLanguage.Italian -> when (noun) {
            "song" -> "brano" to "brani"
            "track" -> "traccia" to "tracce"
            "album" -> "album" to "album"
            "artist" -> "artista" to "artisti"
            "genre" -> "genere" to "generi"
            else -> noun to "${noun}i"
        }
        AppLanguage.Japanese -> noun to noun
        AppLanguage.Latin -> when (noun) {
            "song" -> "cantus" to "cantus"
            "track" -> "cantus" to "cantus"
            "album" -> "album" to "albuma"
            "artist" -> "artifex" to "artifices"
            "genre" -> "genus" to "genera"
            else -> noun to noun
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> "dziesma" to "dziesmas"
            "track" -> "ieraksts" to "ieraksti"
            "album" -> "albums" to "albumi"
            "artist" -> "mākslinieks" to "mākslinieki"
            "genre" -> "žanrs" to "žanri"
            else -> noun to "${noun}i"
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> "daina" to "dainos"
            "track" -> "takelis" to "takeliai"
            "album" -> "albumas" to "albumai"
            "artist" -> "atlikėjas" to "atlikėjai"
            "genre" -> "žanras" to "žanrai"
            else -> noun to "${noun}ai"
        }
        AppLanguage.Macedonian -> when (noun) {
            "song" -> "песна" to "песни"
            "track" -> "нумера" to "нумери"
            "album" -> "албум" to "албуми"
            "artist" -> "артист" to "артисти"
            "genre" -> "жанр" to "жанрови"
            else -> noun to noun
        }
        AppLanguage.Norwegian -> when (noun) {
            "song" -> "sang" to "sanger"
            "track" -> "spor" to "spor"
            "album" -> "album" to "album"
            "artist" -> "artist" to "artister"
            "genre" -> "sjanger" to "sjangre"
            else -> noun to noun
        }
        AppLanguage.Polish -> when (noun) {
            "song" -> "utwór" to "utwory"
            "track" -> "utwór" to "utwory"
            "album" -> "album" to "albumy"
            "artist" -> "artysta" to "artyści"
            "genre" -> "gatunek" to "gatunki"
            else -> noun to "${noun}y"
        }
        AppLanguage.Portuguese -> when (noun) {
            "song" -> "música" to "músicas"
            "track" -> "faixa" to "faixas"
            "album" -> "álbum" to "álbuns"
            "artist" -> "artista" to "artistas"
            "genre" -> "género" to "géneros"
            else -> noun to "${noun}s"
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> "песня" to "песни"
            "track" -> "трек" to "треки"
            "album" -> "альбом" to "альбомы"
            "artist" -> "исполнитель" to "исполнители"
            "genre" -> "жанр" to "жанры"
            else -> noun to noun
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> "песма" to "песме"
            "track" -> "нумера" to "нумере"
            "album" -> "албум" to "албуми"
            "artist" -> "извођач" to "извођачи"
            "genre" -> "жанр" to "жанрови"
            else -> noun to noun
        }
        AppLanguage.Spanish -> when (noun) {
            "song" -> "canción" to "canciones"
            "track" -> "pista" to "pistas"
            "album" -> "álbum" to "álbumes"
            "artist" -> "artista" to "artistas"
            "genre" -> "género" to "géneros"
            else -> noun to "${noun}s"
        }
        AppLanguage.Swedish -> when (noun) {
            "song" -> "låt" to "låtar"
            "track" -> "spår" to "spår"
            "album" -> "album" to "album"
            "artist" -> "artist" to "artister"
            "genre" -> "genre" to "genrer"
            else -> noun to noun
        }
        AppLanguage.Thai -> when (noun) {
            "song" -> "เพลง" to "เพลง"
            "track" -> "แทร็ก" to "แทร็ก"
            "album" -> "อัลบั้ม" to "อัลบั้ม"
            "artist" -> "ศิลปิน" to "ศิลปิน"
            "genre" -> "แนวเพลง" to "แนวเพลง"
            else -> noun to noun
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> "пісня" to "пісні"
            "track" -> "трек" to "треки"
            "album" -> "альбом" to "альбоми"
            "artist" -> "виконавець" to "виконавці"
            "genre" -> "жанр" to "жанри"
            else -> noun to noun
        }
        AppLanguage.English -> when (noun) {
            "song" -> "song" to "songs"
            "track" -> "track" to "tracks"
            "album" -> "album" to "albums"
            "artist" -> "artist" to "artists"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
    }
    val label = if (count == 1) singular else plural
    return "$count $label"
}

internal enum class MiscPhrase {
    RecentlyAdded,
    WhatsNew,
    NoSongsYet,
    AddSongsViaEdit,
    Selected,
    ChooseSongs,
    AddSongs,
}

internal fun miscPhrase(language: AppLanguage, phrase: MiscPhrase): String = when (language) {
    AppLanguage.Polish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Ostatnio dodane"
        MiscPhrase.WhatsNew -> "Co nowego?"
        MiscPhrase.NoSongsYet -> "Nie ma jeszcze utworów"
        MiscPhrase.AddSongsViaEdit -> "Dodaj tu utwory, stukając przycisk edycji"
        MiscPhrase.Selected -> "wybrane"
        MiscPhrase.ChooseSongs -> "Wybierz utwory"
        MiscPhrase.AddSongs -> "Dodaj utwory"
    }
    AppLanguage.Albanian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Shtuar së fundi"
        MiscPhrase.WhatsNew -> "Çfarë ka të re?"
        MiscPhrase.NoSongsYet -> "Nuk ka ende këngë"
        MiscPhrase.AddSongsViaEdit -> "Shto këngë këtu duke prekur butonin e modifikimit"
        MiscPhrase.Selected -> "zgjedhur"
        MiscPhrase.ChooseSongs -> "Zgjidh këngë"
        MiscPhrase.AddSongs -> "Shto këngë"
    }
    AppLanguage.ChineseSimplified -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "最近添加"
        MiscPhrase.WhatsNew -> "有什么新内容？"
        MiscPhrase.NoSongsYet -> "还没有歌曲"
        MiscPhrase.AddSongsViaEdit -> "点击编辑按钮在此添加歌曲"
        MiscPhrase.Selected -> "已选择"
        MiscPhrase.ChooseSongs -> "选择歌曲"
        MiscPhrase.AddSongs -> "添加歌曲"
    }
    AppLanguage.Croatian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nedavno dodano"
        MiscPhrase.WhatsNew -> "Što je novo?"
        MiscPhrase.NoSongsYet -> "Još nema pjesama"
        MiscPhrase.AddSongsViaEdit -> "Dodajte pjesme ovdje dodirom na gumb za uređivanje"
        MiscPhrase.Selected -> "odabrano"
        MiscPhrase.ChooseSongs -> "Odaberi pjesme"
        MiscPhrase.AddSongs -> "Dodaj pjesme"
    }
    AppLanguage.Czech -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nedávno přidané"
        MiscPhrase.WhatsNew -> "Co je nového?"
        MiscPhrase.NoSongsYet -> "Zatím žádné skladby"
        MiscPhrase.AddSongsViaEdit -> "Přidejte sem skladby klepnutím na tlačítko úprav"
        MiscPhrase.Selected -> "vybráno"
        MiscPhrase.ChooseSongs -> "Vyberte skladby"
        MiscPhrase.AddSongs -> "Přidat skladby"
    }
    AppLanguage.Danish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nyligt tilføjet"
        MiscPhrase.WhatsNew -> "Hvad er nyt?"
        MiscPhrase.NoSongsYet -> "Ingen sange endnu"
        MiscPhrase.AddSongsViaEdit -> "Tilføj sange her ved at trykke på redigeringsknappen"
        MiscPhrase.Selected -> "valgt"
        MiscPhrase.ChooseSongs -> "Vælg sange"
        MiscPhrase.AddSongs -> "Tilføj sange"
    }
    AppLanguage.Dutch -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Recent toegevoegd"
        MiscPhrase.WhatsNew -> "Wat is er nieuw?"
        MiscPhrase.NoSongsYet -> "Nog geen nummers"
        MiscPhrase.AddSongsViaEdit -> "Voeg hier nummers toe door op de bewerkknop te tikken"
        MiscPhrase.Selected -> "geselecteerd"
        MiscPhrase.ChooseSongs -> "Kies nummers"
        MiscPhrase.AddSongs -> "Nummers toevoegen"
    }
    AppLanguage.Estonian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Hiljuti lisatud"
        MiscPhrase.WhatsNew -> "Mis on uut?"
        MiscPhrase.NoSongsYet -> "Laule pole veel"
        MiscPhrase.AddSongsViaEdit -> "Lisa siia lugusid, puudutades muutmisnuppu"
        MiscPhrase.Selected -> "valitud"
        MiscPhrase.ChooseSongs -> "Vali lood"
        MiscPhrase.AddSongs -> "Lisa lugusid"
    }
    AppLanguage.French -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Ajoutés récemment"
        MiscPhrase.WhatsNew -> "Quoi de neuf ?"
        MiscPhrase.NoSongsYet -> "Aucun morceau pour le moment"
        MiscPhrase.AddSongsViaEdit -> "Ajoutez des morceaux ici en touchant le bouton modifier"
        MiscPhrase.Selected -> "sélectionnés"
        MiscPhrase.ChooseSongs -> "Choisir des morceaux"
        MiscPhrase.AddSongs -> "Ajouter des morceaux"
    }
    AppLanguage.German -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Kürzlich hinzugefügt"
        MiscPhrase.WhatsNew -> "Was ist neu?"
        MiscPhrase.NoSongsYet -> "Noch keine Titel"
        MiscPhrase.AddSongsViaEdit -> "Füge hier Titel hinzu, indem du auf die Bearbeiten-Schaltfläche tippst"
        MiscPhrase.Selected -> "ausgewählt"
        MiscPhrase.ChooseSongs -> "Titel auswählen"
        MiscPhrase.AddSongs -> "Titel hinzufügen"
    }
    AppLanguage.Greek -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Προστέθηκαν πρόσφατα"
        MiscPhrase.WhatsNew -> "Τι νέο υπάρχει;"
        MiscPhrase.NoSongsYet -> "Δεν υπάρχουν ακόμη τραγούδια"
        MiscPhrase.AddSongsViaEdit -> "Προσθέστε τραγούδια εδώ πατώντας το κουμπί επεξεργασίας"
        MiscPhrase.Selected -> "επιλεγμένα"
        MiscPhrase.ChooseSongs -> "Επιλέξτε τραγούδια"
        MiscPhrase.AddSongs -> "Προσθήκη τραγουδιών"
    }
    AppLanguage.Hindi -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "हाल ही में जोड़े गए"
        MiscPhrase.WhatsNew -> "नया क्या है?"
        MiscPhrase.NoSongsYet -> "अभी तक कोई गाने नहीं"
        MiscPhrase.AddSongsViaEdit -> "एडिट बटन दबाकर यहाँ गाने जोड़ें"
        MiscPhrase.Selected -> "चुने गए"
        MiscPhrase.ChooseSongs -> "गाने चुनें"
        MiscPhrase.AddSongs -> "गाने जोड़ें"
    }
    AppLanguage.Hungarian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nemrég hozzáadva"
        MiscPhrase.WhatsNew -> "Mi az újdonság?"
        MiscPhrase.NoSongsYet -> "Még nincsenek dalok"
        MiscPhrase.AddSongsViaEdit -> "Adj hozzá dalokat itt a szerkesztés gomb megérintésével"
        MiscPhrase.Selected -> "kiválasztva"
        MiscPhrase.ChooseSongs -> "Válassz dalokat"
        MiscPhrase.AddSongs -> "Dalok hozzáadása"
    }
    AppLanguage.Italian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Aggiunti di recente"
        MiscPhrase.WhatsNew -> "Cosa c'è di nuovo?"
        MiscPhrase.NoSongsYet -> "Nessun brano ancora"
        MiscPhrase.AddSongsViaEdit -> "Aggiungi qui i brani toccando il pulsante modifica"
        MiscPhrase.Selected -> "selezionati"
        MiscPhrase.ChooseSongs -> "Scegli brani"
        MiscPhrase.AddSongs -> "Aggiungi brani"
    }
    AppLanguage.Japanese -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "最近追加"
        MiscPhrase.WhatsNew -> "新着情報"
        MiscPhrase.NoSongsYet -> "まだ曲がありません"
        MiscPhrase.AddSongsViaEdit -> "編集ボタンをタップしてここに曲を追加します"
        MiscPhrase.Selected -> "選択済み"
        MiscPhrase.ChooseSongs -> "曲を選択"
        MiscPhrase.AddSongs -> "曲を追加"
    }
    AppLanguage.Latin -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nuper addita"
        MiscPhrase.WhatsNew -> "Quid novi?"
        MiscPhrase.NoSongsYet -> "Nulli cantus adhuc"
        MiscPhrase.AddSongsViaEdit -> "Cantus hic adde tangendo bullam emendandi"
        MiscPhrase.Selected -> "selecta"
        MiscPhrase.ChooseSongs -> "Elige cantus"
        MiscPhrase.AddSongs -> "Adde cantus"
    }
    AppLanguage.Latvian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nesen pievienots"
        MiscPhrase.WhatsNew -> "Kas jauns?"
        MiscPhrase.NoSongsYet -> "Vēl nav dziesmu"
        MiscPhrase.AddSongsViaEdit -> "Pievieno dziesmas šeit, pieskaroties rediģēšanas pogai"
        MiscPhrase.Selected -> "atlasīts"
        MiscPhrase.ChooseSongs -> "Izvēlies dziesmas"
        MiscPhrase.AddSongs -> "Pievienot dziesmas"
    }
    AppLanguage.Lithuanian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Neseniai pridėta"
        MiscPhrase.WhatsNew -> "Kas naujo?"
        MiscPhrase.NoSongsYet -> "Dar nėra dainų"
        MiscPhrase.AddSongsViaEdit -> "Pridėkite dainas čia paliesdami redagavimo mygtuką"
        MiscPhrase.Selected -> "pasirinkta"
        MiscPhrase.ChooseSongs -> "Pasirinkite dainas"
        MiscPhrase.AddSongs -> "Pridėti dainas"
    }
    AppLanguage.Macedonian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Неодамна додадено"
        MiscPhrase.WhatsNew -> "Што има ново?"
        MiscPhrase.NoSongsYet -> "Сè уште нема песни"
        MiscPhrase.AddSongsViaEdit -> "Додај песни тука со допирање на копчето за уредување"
        MiscPhrase.Selected -> "избрано"
        MiscPhrase.ChooseSongs -> "Избери песни"
        MiscPhrase.AddSongs -> "Додај песни"
    }
    AppLanguage.Norwegian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nylig lagt til"
        MiscPhrase.WhatsNew -> "Hva er nytt?"
        MiscPhrase.NoSongsYet -> "Ingen sanger ennå"
        MiscPhrase.AddSongsViaEdit -> "Legg til sanger her ved å trykke på redigeringsknappen"
        MiscPhrase.Selected -> "valgt"
        MiscPhrase.ChooseSongs -> "Velg sanger"
        MiscPhrase.AddSongs -> "Legg til sanger"
    }
    AppLanguage.Portuguese -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Adicionados recentemente"
        MiscPhrase.WhatsNew -> "O que há de novo?"
        MiscPhrase.NoSongsYet -> "Ainda não há músicas"
        MiscPhrase.AddSongsViaEdit -> "Adicione músicas aqui tocando no botão editar"
        MiscPhrase.Selected -> "selecionados"
        MiscPhrase.ChooseSongs -> "Escolher músicas"
        MiscPhrase.AddSongs -> "Adicionar músicas"
    }
    AppLanguage.Russian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Недавно добавлено"
        MiscPhrase.WhatsNew -> "Что нового?"
        MiscPhrase.NoSongsYet -> "Песен пока нет"
        MiscPhrase.AddSongsViaEdit -> "Добавьте песни сюда, нажав кнопку редактирования"
        MiscPhrase.Selected -> "выбрано"
        MiscPhrase.ChooseSongs -> "Выберите песни"
        MiscPhrase.AddSongs -> "Добавить песни"
    }
    AppLanguage.Serbian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Недавно додато"
        MiscPhrase.WhatsNew -> "Шта је ново?"
        MiscPhrase.NoSongsYet -> "Још нема песама"
        MiscPhrase.AddSongsViaEdit -> "Додај песме овде додиром на дугме за уређивање"
        MiscPhrase.Selected -> "изабрано"
        MiscPhrase.ChooseSongs -> "Изабери песме"
        MiscPhrase.AddSongs -> "Додај песме"
    }
    AppLanguage.Spanish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Añadidos recientemente"
        MiscPhrase.WhatsNew -> "¿Qué hay de nuevo?"
        MiscPhrase.NoSongsYet -> "Aún no hay canciones"
        MiscPhrase.AddSongsViaEdit -> "Añade canciones aquí tocando el botón de editar"
        MiscPhrase.Selected -> "seleccionados"
        MiscPhrase.ChooseSongs -> "Elegir canciones"
        MiscPhrase.AddSongs -> "Añadir canciones"
    }
    AppLanguage.Swedish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nyligen tillagt"
        MiscPhrase.WhatsNew -> "Vad är nytt?"
        MiscPhrase.NoSongsYet -> "Inga låtar ännu"
        MiscPhrase.AddSongsViaEdit -> "Lägg till låtar här genom att trycka på redigeringsknappen"
        MiscPhrase.Selected -> "valda"
        MiscPhrase.ChooseSongs -> "Välj låtar"
        MiscPhrase.AddSongs -> "Lägg till låtar"
    }
    AppLanguage.Thai -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "เพิ่มล่าสุด"
        MiscPhrase.WhatsNew -> "มีอะไรใหม่?"
        MiscPhrase.NoSongsYet -> "ยังไม่มีเพลง"
        MiscPhrase.AddSongsViaEdit -> "เพิ่มเพลงที่นี่ด้วยการแตะปุ่มแก้ไข"
        MiscPhrase.Selected -> "ที่เลือก"
        MiscPhrase.ChooseSongs -> "เลือกเพลง"
        MiscPhrase.AddSongs -> "เพิ่มเพลง"
    }
    AppLanguage.Ukrainian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Нещодавно додано"
        MiscPhrase.WhatsNew -> "Що нового?"
        MiscPhrase.NoSongsYet -> "Пісень ще немає"
        MiscPhrase.AddSongsViaEdit -> "Додайте сюди пісні, натиснувши кнопку редагування"
        MiscPhrase.Selected -> "вибрано"
        MiscPhrase.ChooseSongs -> "Оберіть пісні"
        MiscPhrase.AddSongs -> "Додати пісні"
    }
    AppLanguage.English -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Recently added"
        MiscPhrase.WhatsNew -> "What’s new?"
        MiscPhrase.NoSongsYet -> "No songs yet"
        MiscPhrase.AddSongsViaEdit -> "Add songs here by tapping on edit button"
        MiscPhrase.Selected -> "selected"
        MiscPhrase.ChooseSongs -> "Choose songs"
        MiscPhrase.AddSongs -> "Add songs"
    }
}

internal data class CommonUiCopy(
    val home: String,
    val library: String,
    val playlists: String,
    val search: String,
    val welcome: String,
    val songs: String,
    val albums: String,
    val artists: String,
    val genres: String,
    val light: String,
    val dark: String,
    val system: String,
    val inYourLibrary: String,
    val inTotal: String,
    val found: String,
    val refinedFooter: String,
)

internal fun commonUiCopy(language: AppLanguage): CommonUiCopy = when (language) {
    AppLanguage.Polish -> CommonUiCopy("Główna", "Biblioteka", "Playlisty", "Szukaj", "Witamy", "Utwory", "Albumy", "Artyści", "Gatunki", "Jasny", "Ciemny", "System", "w Twojej bibliotece", "łącznie", "znaleziono", "Twoja muzyka, dopracowana w eleganckie doświadczenie")
    AppLanguage.Albanian -> CommonUiCopy("Kreu", "Biblioteka", "Listat", "Kërko", "Mirë se vini", "Këngë", "Albume", "Artistë", "Zhanre", "E çelët", "E errët", "Sistemi", "në bibliotekën tënde", "gjithsej", "u gjetën", "Muzika jote, e rafinuar në një përvojë elegante")
    AppLanguage.ChineseSimplified -> CommonUiCopy("主页", "媒体库", "播放列表", "搜索", "欢迎", "歌曲", "专辑", "艺人", "流派", "浅色", "深色", "跟随系统", "在你的媒体库中", "总计", "已找到", "你的音乐，被雕琢成优雅的体验")
    AppLanguage.Croatian -> CommonUiCopy("Početna", "Biblioteka", "Playliste", "Pretraži", "Dobrodošli", "Pjesme", "Albumi", "Izvođači", "Žanrovi", "Svijetlo", "Tamno", "Sustav", "u tvojoj biblioteci", "ukupno", "pronađeno", "Tvoja glazba, profinjena u elegantno iskustvo")
    AppLanguage.Czech -> CommonUiCopy("Domů", "Knihovna", "Playlisty", "Hledat", "Vítejte", "Skladby", "Alba", "Umělci", "Žánry", "Světlý", "Tmavý", "Systém", "ve vaší knihovně", "celkem", "nalezeno", "Vaše hudba, vytříbená do elegantního zážitku")
    AppLanguage.Danish -> CommonUiCopy("Hjem", "Bibliotek", "Playlister", "Søg", "Velkommen", "Sange", "Albummer", "Kunstnere", "Genrer", "Lys", "Mørk", "System", "i dit bibliotek", "i alt", "fundet", "Din musik, raffineret til en elegant oplevelse")
    AppLanguage.Dutch -> CommonUiCopy("Home", "Bibliotheek", "Afspeellijsten", "Zoeken", "Welkom", "Nummers", "Albums", "Artiesten", "Genres", "Licht", "Donker", "Systeem", "in je bibliotheek", "in totaal", "gevonden", "Jouw muziek, verfijnd tot een elegante ervaring")
    AppLanguage.Estonian -> CommonUiCopy("Avaleht", "Teek", "Esitusloendid", "Otsi", "Tere tulemast", "Lood", "Albumid", "Artistid", "Žanrid", "Hele", "Tume", "Süsteem", "sinu teegis", "kokku", "leitud", "Sinu muusika, viimistletud elegantseks elamuseks")
    AppLanguage.French -> CommonUiCopy("Accueil", "Bibliothèque", "Playlists", "Recherche", "Bienvenue", "Morceaux", "Albums", "Artistes", "Genres", "Clair", "Sombre", "Système", "dans votre bibliothèque", "au total", "trouvés", "Votre musique, affinée en une expérience élégante")
    AppLanguage.German -> CommonUiCopy("Start", "Bibliothek", "Playlists", "Suche", "Willkommen", "Titel", "Alben", "Künstler", "Genres", "Hell", "Dunkel", "System", "in deiner Bibliothek", "insgesamt", "gefunden", "Deine Musik, veredelt zu einem eleganten Erlebnis")
    AppLanguage.Greek -> CommonUiCopy("Αρχική", "Βιβλιοθήκη", "Playlists", "Αναζήτηση", "Καλώς ήρθατε", "Τραγούδια", "Άλμπουμ", "Καλλιτέχνες", "Είδη", "Φωτεινό", "Σκούρο", "Σύστημα", "στη βιβλιοθήκη σας", "συνολικά", "βρέθηκαν", "Η μουσική σας, εκλεπτυσμένη σε μια κομψή εμπειρία")
    AppLanguage.Hindi -> CommonUiCopy("होम", "लाइब्रेरी", "प्लेलिस्ट", "खोजें", "स्वागत है", "गाने", "एल्बम", "कलाकार", "शैलियाँ", "लाइट", "डार्क", "सिस्टम", "आपकी लाइब्रेरी में", "कुल", "मिले", "आपका संगीत, एक सुरुचिपूर्ण अनुभव में निखरा हुआ")
    AppLanguage.Hungarian -> CommonUiCopy("Kezdőlap", "Könyvtár", "Lejátszási listák", "Keresés", "Üdvözöljük", "Dalok", "Albumok", "Előadók", "Műfajok", "Világos", "Sötét", "Rendszer", "a könyvtáradban", "összesen", "találat", "A zenéd, kifinomítva elegáns élménnyé")
    AppLanguage.Italian -> CommonUiCopy("Home", "Libreria", "Playlist", "Cerca", "Benvenuto", "Brani", "Album", "Artisti", "Generi", "Chiaro", "Scuro", "Sistema", "nella tua libreria", "in totale", "trovati", "La tua musica, rifinita in un'esperienza elegante")
    AppLanguage.Japanese -> CommonUiCopy("ホーム", "ライブラリ", "プレイリスト", "検索", "ようこそ", "曲", "アルバム", "アーティスト", "ジャンル", "ライト", "ダーク", "システム", "ライブラリ内", "合計", "見つかりました", "あなたの音楽を、洗練された体験へ")
    AppLanguage.Latin -> CommonUiCopy("Domus", "Bibliotheca", "Indices", "Quaere", "Salve", "Cantus", "Albumina", "Artifices", "Genera", "Clarus", "Obscurus", "Systema", "in bibliotheca tua", "omnino", "inventa", "Musica tua, in experientiam elegantem expolita")
    AppLanguage.Latvian -> CommonUiCopy("Sākums", "Bibliotēka", "Atskaņošanas saraksti", "Meklēt", "Laipni lūdzam", "Dziesmas", "Albumi", "Mākslinieki", "Žanri", "Gaišs", "Tumšs", "Sistēma", "tavā bibliotēkā", "kopā", "atrasts", "Tava mūzika, izsmalcināta elegantā pieredzē")
    AppLanguage.Lithuanian -> CommonUiCopy("Pradžia", "Biblioteka", "Grojaraščiai", "Paieška", "Sveiki", "Dainos", "Albumai", "Atlikėjai", "Žanrai", "Šviesi", "Tamsi", "Sistema", "jūsų bibliotekoje", "iš viso", "rasta", "Tavo muzika, ištobulinta į elegantišką patirtį")
    AppLanguage.Macedonian -> CommonUiCopy("Почетна", "Библиотека", "Плејлисти", "Пребарај", "Добредојдовте", "Песни", "Албуми", "Артисти", "Жанрови", "Светла", "Темна", "Систем", "во вашата библиотека", "вкупно", "пронајдени", "Вашата музика, префинета во елегантно доживување")
    AppLanguage.Norwegian -> CommonUiCopy("Hjem", "Bibliotek", "Spillelister", "Søk", "Velkommen", "Sanger", "Album", "Artister", "Sjangre", "Lys", "Mørk", "System", "i biblioteket ditt", "totalt", "funnet", "Musikken din, raffinert til en elegant opplevelse")
    AppLanguage.Portuguese -> CommonUiCopy("Início", "Biblioteca", "Playlists", "Pesquisar", "Bem-vindo", "Músicas", "Álbuns", "Artistas", "Géneros", "Claro", "Escuro", "Sistema", "na sua biblioteca", "no total", "encontrados", "A sua música, refinada numa experiência elegante")
    AppLanguage.Russian -> CommonUiCopy("Главная", "Библиотека", "Плейлисты", "Поиск", "Добро пожаловать", "Песни", "Альбомы", "Исполнители", "Жанры", "Светлая", "Тёмная", "Система", "в вашей библиотеке", "всего", "найдено", "Ваша музыка, отточенная до элегантного опыта")
    AppLanguage.Serbian -> CommonUiCopy("Почетна", "Библиотека", "Плејлисте", "Претрага", "Добро дошли", "Песме", "Албуми", "Извођачи", "Жанрови", "Светла", "Тамна", "Систем", "у вашој библиотеци", "укупно", "пронађено", "Ваша музика, префињена у елегантно искуство")
    AppLanguage.Spanish -> CommonUiCopy("Inicio", "Biblioteca", "Playlists", "Buscar", "Bienvenido", "Canciones", "Álbumes", "Artistas", "Géneros", "Claro", "Oscuro", "Sistema", "en tu biblioteca", "en total", "encontrados", "Tu música, refinada en una experiencia elegante")
    AppLanguage.Swedish -> CommonUiCopy("Hem", "Bibliotek", "Spellistor", "Sök", "Välkommen", "Låtar", "Album", "Artister", "Genrer", "Ljust", "Mörkt", "System", "i ditt bibliotek", "totalt", "hittade", "Din musik, förädlad till en elegant upplevelse")
    AppLanguage.Thai -> CommonUiCopy("หน้าแรก", "คลังเพลง", "เพลย์ลิสต์", "ค้นหา", "ยินดีต้อนรับ", "เพลง", "อัลบั้ม", "ศิลปิน", "แนวเพลง", "สว่าง", "มืด", "ระบบ", "ในคลังของคุณ", "ทั้งหมด", "พบ", "เพลงของคุณ ถูกขัดเกลาให้เป็นประสบการณ์อันสง่างาม")
    AppLanguage.Ukrainian -> CommonUiCopy("Головна", "Бібліотека", "Плейлисти", "Пошук", "Ласкаво просимо", "Пісні", "Альбоми", "Виконавці", "Жанри", "Світла", "Темна", "Система", "у вашій бібліотеці", "усього", "знайдено", "Ваша музика, відточена до елегантного досвіду")
    AppLanguage.English -> CommonUiCopy("Home", "Library", "Playlists", "Search", "Welcome", "Songs", "Albums", "Artists", "Genres", "Light", "Dark", "System", "in your library", "in total", "found", "Your music, refined into an elegant experience")
}

private data class SearchUiCopy(
    val placeholder: String,
    val clearSearch: String,
    val nothingSearchedTitle: String,
    val nothingSearchedMessage: String,
    val suggestedAlbumsTitle: String,
    val suggestedAlbumsSubtitle: String,
    val recentlySearched: String,
    val clearHistory: String,
    val noResultsTitle: String,
    val noResultsPrefix: String,
    val noResultsSuffix: String,
    val matchingArtistsSuffix: String,
    val matchingAlbumsSuffix: String,
    val matchingSongsSuffix: String,
) {
    fun noResultsMessage(query: String): String = "$noResultsPrefix \"$query\" $noResultsSuffix"
    fun matchingArtists(count: Int): String = "$count $matchingArtistsSuffix"
    fun matchingAlbums(count: Int): String = "$count $matchingAlbumsSuffix"
    fun matchingSongs(count: Int): String = "$count $matchingSongsSuffix"
}

private fun searchCopy(language: AppLanguage): SearchUiCopy = when (language) {
    AppLanguage.Polish -> SearchUiCopy("Artyści, albumy i więcej", "Wyczyść wyszukiwanie", "Jeszcze nic nie wyszukano", "Więcej wyników pojawi się podczas wyszukiwania utworów i albumów", "Sugerowane albumy", "Warto do nich wrócić", "Ostatnio wyszukiwane", "Wyczyść historię", "Brak wyników", "Nic w obecnej bibliotece offline nie pasuje do", "jeszcze", "pasujących artystów", "pasujących albumów", "pasujących utworów")
    AppLanguage.ChineseSimplified -> SearchUiCopy("艺人、专辑等", "清除搜索", "还没有搜索", "搜索歌曲和专辑时会显示更多结果", "推荐专辑", "你可能会想再听听", "最近搜索", "清除历史", "没有结果", "当前离线媒体库中没有匹配", "", "个匹配艺人", "个匹配专辑", "个匹配歌曲")
    AppLanguage.Czech -> SearchUiCopy("Umělci, alba a další", "Vymazat hledání", "Zatím nic nehledáno", "Další výsledky se zobrazí při hledání skladeb a alb", "Navržená alba", "Možná se k nim chcete vrátit", "Nedávno hledané", "Vymazat historii", "Žádné výsledky", "V aktuální offline knihovně se nic neshoduje s", "zatím", "odpovídajících umělců", "odpovídajících alb", "odpovídajících skladeb")
    AppLanguage.French -> SearchUiCopy("Artistes, albums et plus", "Effacer la recherche", "Aucune recherche pour l’instant", "Plus de résultats apparaîtront pendant la recherche de morceaux et d’albums", "Albums suggérés", "Vous devriez peut-être les réécouter", "Recherches récentes", "Effacer l’historique", "Aucun résultat", "Rien dans la bibliothèque hors ligne actuelle ne correspond à", "pour l’instant", "artistes correspondants", "albums correspondants", "morceaux correspondants")
    AppLanguage.German -> SearchUiCopy("Künstler, Alben und mehr", "Suche löschen", "Noch nichts gesucht", "Weitere Ergebnisse erscheinen, wenn du nach Songs und Alben suchst", "Vorgeschlagene Alben", "Diese solltest du vielleicht wieder hören", "Zuletzt gesucht", "Verlauf löschen", "Keine Ergebnisse", "In der aktuellen Offline-Bibliothek passt nichts zu", "bisher", "passende Künstler", "passende Alben", "passende Songs")
    AppLanguage.Italian -> SearchUiCopy("Artisti, album e altro", "Cancella ricerca", "Nessuna ricerca ancora", "Altri risultati appariranno mentre cerchi brani e album", "Album suggeriti", "Potresti volerli riascoltare", "Ricerche recenti", "Cancella cronologia", "Nessun risultato", "Nella libreria offline attuale non corrisponde nulla a", "ancora", "artisti corrispondenti", "album corrispondenti", "brani corrispondenti")
    AppLanguage.Japanese -> SearchUiCopy("アーティスト、アルバムなど", "検索をクリア", "まだ検索していません", "曲やアルバムを検索すると、さらに結果が表示されます", "おすすめアルバム", "また聴きたくなるかもしれません", "最近の検索", "履歴を消去", "結果なし", "現在のオフラインライブラリに一致するものはありません:", "", "件の一致するアーティスト", "件の一致するアルバム", "件の一致する曲")
    AppLanguage.Spanish -> SearchUiCopy("Artistas, álbumes y más", "Borrar búsqueda", "Aún no has buscado nada", "Aparecerán más resultados al buscar canciones y álbumes", "Álbumes sugeridos", "Quizá quieras volver a escucharlos", "Búsquedas recientes", "Borrar historial", "Sin resultados", "Nada en la biblioteca sin conexión actual coincide con", "todavía", "artistas coincidentes", "álbumes coincidentes", "canciones coincidentes")
    AppLanguage.Portuguese -> SearchUiCopy("Artistas, álbuns e mais", "Limpar pesquisa", "Ainda nada pesquisado", "Mais resultados aparecerão ao pesquisar músicas e álbuns", "Álbuns sugeridos", "Talvez queira revisitá-los", "Pesquisas recentes", "Limpar histórico", "Sem resultados", "Nada na biblioteca offline atual corresponde a", "ainda", "artistas correspondentes", "álbuns correspondentes", "músicas correspondentes")
    AppLanguage.Russian -> SearchUiCopy("Исполнители, альбомы и другое", "Очистить поиск", "Пока ничего не искали", "Больше результатов появится при поиске песен и альбомов", "Предложенные альбомы", "Возможно, стоит вернуться к ним", "Недавние поиски", "Очистить историю", "Нет результатов", "В текущей офлайн-библиотеке ничего не найдено для", "пока", "подходящих исполнителей", "подходящих альбомов", "подходящих песен")
    AppLanguage.Ukrainian -> SearchUiCopy("Виконавці, альбоми тощо", "Очистити пошук", "Поки нічого не шукали", "Більше результатів з’явиться під час пошуку пісень і альбомів", "Запропоновані альбоми", "Можливо, варто повернутися до них", "Нещодавні пошуки", "Очистити історію", "Немає результатів", "У поточній офлайн-бібліотеці нічого не збігається з", "поки", "відповідних виконавців", "відповідних альбомів", "відповідних пісень")
    else -> SearchUiCopy("Artists, albums & more", "Clear search", "Nothing searched yet", "More results will show here as you search for songs and albums", "Suggested albums", "You should probably revisit these", "Recently searched", "Clear history", "No results", "Nothing in the current offline library matches", "yet", "matching artists", "matching album results", "matching song results")
}

private fun searchSortModeLabel(
    mode: SearchSongSortMode,
    language: AppLanguage,
): String = when (mode) {
    SearchSongSortMode.Title -> when (language) {
        AppLanguage.Polish -> "Nazwa utworu"
        AppLanguage.ChineseSimplified -> "歌曲名"
        AppLanguage.Czech -> "Název skladby"
        AppLanguage.French -> "Nom du morceau"
        AppLanguage.German -> "Songname"
        AppLanguage.Italian -> "Nome brano"
        AppLanguage.Japanese -> "曲名"
        AppLanguage.Spanish -> "Nombre de canción"
        AppLanguage.Portuguese -> "Nome da música"
        AppLanguage.Russian -> "Название песни"
        AppLanguage.Ukrainian -> "Назва пісні"
        else -> "Song name"
    }
    SearchSongSortMode.Artist -> when (language) {
        AppLanguage.Polish -> "Nazwa artysty"
        AppLanguage.ChineseSimplified -> "艺人名"
        AppLanguage.Czech -> "Jméno umělce"
        AppLanguage.French -> "Nom de l’artiste"
        AppLanguage.German -> "Künstlername"
        AppLanguage.Italian -> "Nome artista"
        AppLanguage.Japanese -> "アーティスト名"
        AppLanguage.Spanish -> "Nombre de artista"
        AppLanguage.Portuguese -> "Nome do artista"
        AppLanguage.Russian -> "Имя исполнителя"
        AppLanguage.Ukrainian -> "Ім’я виконавця"
        else -> "Artist name"
    }
}

@Composable
private fun EqualizerScreen(
    settings: EqSettings,
    onBack: () -> Unit,
    onBandChanged: (Int, Float) -> Unit,
    onBassChanged: (Float) -> Unit,
    onMidrangeChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onSpaciousnessModeChanged: (SpaciousnessMode) -> Unit,
    onReverbDurationChanged: (Int) -> Unit,
    onReverbProfileChanged: (ReverbProfile) -> Unit,
    onResetReverb: () -> Unit,
    onApplyPreset: (EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val listState = rememberElovaireLazyListState("equalizer_screen")
    val graphScrollState = rememberScrollState()
    val language = LocalAppLanguage.current
    val copy = remember(language) { settingsCopy(language) }
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
                bottom = 96.dp + buttonNavigationScrollBoost(),
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Box {
                    val graphContentWidth = EQ_GRAPH_EDGE_PADDING * 2 +
                        EQ_BAND_SPACING * (EqualizerDspModel.BAND_COUNT - 1).coerceAtLeast(0).toFloat()
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        SettingsCategoryText(
                            title = uiPhrase(language, UiPhrase.ToneShaping),
                            iconResId = R.drawable.ic_lucide_audio_waveform,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            EqToneKnob(
                                title = uiPhrase(language, UiPhrase.Bass),
                                value = settings.bass.coerceIn(0f, 1f),
                                valueRange = 0f..1f,
                                accentColor = Color(0xFF2FE08D),
                                modifier = Modifier.weight(1f),
                                onValueChange = onBassChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(language, UiPhrase.Midrange),
                                value = settings.midrange.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFF39C2FF),
                                modifier = Modifier.weight(1f),
                                onValueChange = onMidrangeChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(language, UiPhrase.Treble),
                                value = settings.treble.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFFFFB056),
                                modifier = Modifier.weight(1f),
                                onValueChange = onTrebleChanged,
                            )
                        }
                    }
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingsCategoryText(
                            title = copy.spaciousness,
                            iconResId = R.drawable.ic_lucide_wind,
                        )
                        SpaciousnessModeMenu(
                            currentMode = settings.spaciousnessMode,
                            spaciousnessAmount = settings.spaciousness,
                            onModeSelected = onSpaciousnessModeChanged,
                        )
                        EqMacroSliderRow(
                            title = uiPhrase(language, UiPhrase.EffectStrength),
                            value = settings.spaciousness.coerceIn(0f, 1f),
                            valueText = "${(settings.spaciousness.coerceIn(0f, 1f) * 100f).roundToInt()}%",
                            onValueChange = onSpaciousnessChanged,
                            valueRange = 0f..1f,
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = uiPhrase(language, UiPhrase.Reverb),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = elovaireScaledSp(16f),
                                    ),
                                )
                                Text(
                                    text = if (settings.reverbDurationMs <= 0) uiPhrase(language, UiPhrase.Off) else "${settings.reverbDurationMs} ms",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
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
                                EqPresetPill(
                                    label = uiPhrase(language, UiPhrase.Reset),
                                    selected = false,
                                    emphasized = true,
                                    useSubtleIdleBackground = true,
                                    onClick = onResetReverb,
                                )
                                ReverbProfile.entries.forEach { profile ->
                                    EqPresetPill(
                                        label = when (profile) {
                                            ReverbProfile.Dry -> uiPhrase(language, UiPhrase.Dry)
                                            ReverbProfile.Wet -> uiPhrase(language, UiPhrase.Wet)
                                        },
                                        selected = settings.reverbDurationMs > 0 && settings.reverbProfile == profile,
                                        useSubtleIdleBackground = true,
                                        onClick = { onReverbProfileChanged(profile) },
                                    )
                                }
                            }
                            ReverbStepSlider(
                                valueMs = settings.reverbDurationMs,
                                onValueChange = onReverbDurationChanged,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
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
            title = copy.equalizer,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

internal data class SettingsLanguageCopy(
    val settings: String,
    val appearance: String,
    val theme: String,
    val textSize: String,
    val language: String,
    val currentlyUsed: String,
    val sound: String,
    val bassBoost: String,
    val spaciousness: String,
    val equalizer: String,
    val enableMono: String,
    val monoSubtitle: String,
    val otherSettings: String,
    val scanLibrary: String,
    val scanLibrarySubtitle: String,
    val scan: String,
    val checkUpdates: String,
    val checkUpdatesSubtitle: String,
    val check: String,
    val changelog: String,
    val footerSubtitle: String,
)

internal fun settingsCopy(language: AppLanguage): SettingsLanguageCopy = when (language) {
    AppLanguage.Polish -> SettingsLanguageCopy("Ustawienia", "Wygląd", "Motyw", "Rozmiar tekstu", "Język", "Obecnie używany: ${language.nativeName}", "Dźwięk", "Podbicie basu", "Przestrzenność", "Korektor", "Włącz mono", "Przełącza odtwarzanie stereo na mono", "Inne ustawienia", "Skanuj bibliotekę", "Odśwież indeksowanie w poszukiwaniu nowych multimediów", "Skanuj", "Sprawdź aktualizacje", "Sprawdź, czy jest dostępna nowa wersja", "Sprawdź", "Lista zmian", "Zaprojektowane z pasją do muzyki i świetnego designu")
    AppLanguage.ChineseSimplified -> SettingsLanguageCopy("设置", "外观", "主题", "文字大小", "语言", "当前使用：${language.nativeName}", "声音", "低音增强", "空间感", "均衡器", "启用单声道", "将立体声播放切换为单声道", "其他设置", "扫描媒体库", "刷新索引以查找新媒体", "扫描", "检查更新", "检查是否有新版本可用", "检查", "更新日志", "为音乐和优秀设计倾注热情")
    AppLanguage.Czech -> SettingsLanguageCopy("Nastavení", "Vzhled", "Motiv", "Velikost textu", "Jazyk", "Aktuálně používaný: ${language.nativeName}", "Zvuk", "Zesílení basů", "Prostorovost", "Ekvalizér", "Zapnout mono", "Přepne stereo přehrávání na mono", "Další nastavení", "Skenovat knihovnu", "Obnoví index pro nová média", "Skenovat", "Zkontrolovat aktualizace", "Zjistit, zda je k dispozici nová verze", "Zkontrolovat", "Změny", "Navrženo s vášní pro hudbu a skvělý design")
    AppLanguage.Lithuanian -> SettingsLanguageCopy("Nustatymai", "Išvaizda", "Tema", "Teksto dydis", "Kalba", "Šiuo metu naudojama: ${language.nativeName}", "Garsas", "Bosų stiprinimas", "Erdviškumas", "Ekvalaizeris", "Įjungti mono", "Perjungia stereo atkūrimą į mono", "Kiti nustatymai", "Skenuoti biblioteką", "Atnaujina indeksą ieškant naujos medijos", "Skenuoti", "Tikrinti naujinimus", "Patikrina, ar yra nauja versija", "Tikrinti", "Pakeitimai", "Sukurta su aistra muzikai ir puikiam dizainui")
    AppLanguage.Danish -> SettingsLanguageCopy("Indstillinger", "Udseende", "Tema", "Tekststørrelse", "Sprog", "Aktuelt brugt: ${language.nativeName}", "Lyd", "Basboost", "Rumlighed", "Equalizer", "Aktivér mono", "Skifter stereoafspilning til mono", "Andre indstillinger", "Scan bibliotek", "Opdater indeksering efter nye medier", "Scan", "Søg efter opdateringer", "Tjek om en ny version er tilgængelig", "Tjek", "Ændringslog", "Designet med passion for musik og godt design")
    AppLanguage.French -> SettingsLanguageCopy("Réglages", "Apparence", "Thème", "Taille du texte", "Langue", "Actuellement utilisé : ${language.nativeName}", "Son", "Renfort des basses", "Spatialisation", "Égaliseur", "Activer mono", "Passe la lecture stéréo en mono", "Autres réglages", "Analyser la bibliothèque", "Actualise l’index pour trouver de nouveaux médias", "Analyser", "Rechercher des mises à jour", "Vérifie si une nouvelle version est disponible", "Vérifier", "Nouveautés", "Conçu avec passion pour la musique et le beau design")
    AppLanguage.German -> SettingsLanguageCopy("Einstellungen", "Darstellung", "Design", "Textgröße", "Sprache", "Aktuell verwendet: ${language.nativeName}", "Klang", "Bassverstärkung", "Räumlichkeit", "Equalizer", "Mono aktivieren", "Schaltet Stereo-Wiedergabe auf Mono", "Weitere Einstellungen", "Bibliothek scannen", "Aktualisiert den Index für neue Medien", "Scannen", "Nach Updates suchen", "Prüft, ob eine neue Version verfügbar ist", "Prüfen", "Änderungen", "Mit Leidenschaft für Musik und gutes Design gestaltet")
    AppLanguage.Dutch -> SettingsLanguageCopy("Instellingen", "Weergave", "Thema", "Tekstgrootte", "Taal", "Momenteel gebruikt: ${language.nativeName}", "Geluid", "Basversterking", "Ruimtelijkheid", "Equalizer", "Mono inschakelen", "Schakelt stereo afspelen om naar mono", "Andere instellingen", "Bibliotheek scannen", "Vernieuwt indexering voor nieuwe media", "Scannen", "Controleren op updates", "Controleert of er een nieuwe versie beschikbaar is", "Controleren", "Wijzigingen", "Ontworpen met passie voor muziek en sterk design")
    AppLanguage.Norwegian -> SettingsLanguageCopy("Innstillinger", "Utseende", "Tema", "Tekststørrelse", "Språk", "Brukes nå: ${language.nativeName}", "Lyd", "Bassforsterkning", "Romfølelse", "Equalizer", "Aktiver mono", "Bytter stereoavspilling til mono", "Andre innstillinger", "Skann bibliotek", "Oppdaterer indeksen for nye medier", "Skann", "Se etter oppdateringer", "Sjekker om en ny versjon er tilgjengelig", "Sjekk", "Endringslogg", "Designet med lidenskap for musikk og flott design")
    AppLanguage.Swedish -> SettingsLanguageCopy("Inställningar", "Utseende", "Tema", "Textstorlek", "Språk", "Används nu: ${language.nativeName}", "Ljud", "Basförstärkning", "Rymd", "Equalizer", "Aktivera mono", "Växlar stereouppspelning till mono", "Andra inställningar", "Skanna bibliotek", "Uppdaterar indexering för ny media", "Skanna", "Sök efter uppdateringar", "Kontrollerar om en ny version finns", "Sök", "Ändringslogg", "Designad med passion för musik och bra design")
    AppLanguage.Spanish -> SettingsLanguageCopy("Ajustes", "Apariencia", "Tema", "Tamaño de texto", "Idioma", "Usado actualmente: ${language.nativeName}", "Sonido", "Refuerzo de graves", "Espacialidad", "Ecualizador", "Activar mono", "Cambia la reproducción estéreo a mono", "Otros ajustes", "Escanear biblioteca", "Actualiza la indexación para buscar nuevos medios", "Escanear", "Buscar actualizaciones", "Comprueba si hay una nueva versión disponible", "Buscar", "Cambios", "Diseñado con pasión por la música y el buen diseño")
    AppLanguage.Portuguese -> SettingsLanguageCopy("Definições", "Aparência", "Tema", "Tamanho do texto", "Idioma", "Atualmente usado: ${language.nativeName}", "Som", "Reforço de graves", "Espacialidade", "Equalizador", "Ativar mono", "Muda a reprodução estéreo para mono", "Outras definições", "Analisar biblioteca", "Atualiza a indexação para novos ficheiros", "Analisar", "Procurar atualizações", "Verifica se há nova versão disponível", "Verificar", "Novidades", "Criado com paixão por música e bom design")
    AppLanguage.Estonian -> SettingsLanguageCopy("Seaded", "Välimus", "Teema", "Teksti suurus", "Keel", "Praegu kasutusel: ${language.nativeName}", "Heli", "Bassi võimendus", "Ruumilisus", "Ekvalaiser", "Luba mono", "Lülitab stereo taasesituse monoks", "Muud seaded", "Skanni teeki", "Värskendab indeksit uue meedia leidmiseks", "Skanni", "Kontrolli uuendusi", "Kontrollib, kas uus versioon on saadaval", "Kontrolli", "Muudatused", "Loodud kirega muusika ja hea disaini vastu")
    AppLanguage.Greek -> SettingsLanguageCopy("Ρυθμίσεις", "Εμφάνιση", "Θέμα", "Μέγεθος κειμένου", "Γλώσσα", "Χρησιμοποιείται τώρα: ${language.nativeName}", "Ήχος", "Ενίσχυση μπάσων", "Χωρικότητα", "Ισοσταθμιστής", "Ενεργοποίηση μονοφωνικού", "Αλλάζει την αναπαραγωγή stereo σε mono", "Άλλες ρυθμίσεις", "Σάρωση βιβλιοθήκης", "Ανανεώνει το ευρετήριο για νέα πολυμέσα", "Σάρωση", "Έλεγχος ενημερώσεων", "Ελέγχει αν υπάρχει νέα έκδοση", "Έλεγχος", "Αλλαγές", "Σχεδιασμένο με πάθος για μουσική και όμορφο design")
    AppLanguage.Croatian -> SettingsLanguageCopy("Postavke", "Izgled", "Tema", "Veličina teksta", "Jezik", "Trenutno se koristi: ${language.nativeName}", "Zvuk", "Pojačanje basa", "Prostornost", "Ekvilizator", "Uključi mono", "Prebacuje stereo reprodukciju u mono", "Ostale postavke", "Skeniraj biblioteku", "Osvježava indeks za nove medije", "Skeniraj", "Provjeri ažuriranja", "Provjerava postoji li nova verzija", "Provjeri", "Promjene", "Dizajnirano sa strašću za glazbu i dobar dizajn")
    AppLanguage.Russian -> SettingsLanguageCopy("Настройки", "Внешний вид", "Тема", "Размер текста", "Язык", "Сейчас используется: ${language.nativeName}", "Звук", "Усиление баса", "Пространственность", "Эквалайзер", "Включить моно", "Переключает стерео воспроизведение в моно", "Другие настройки", "Сканировать библиотеку", "Обновляет индекс для поиска новых медиа", "Сканировать", "Проверить обновления", "Проверяет, доступна ли новая версия", "Проверить", "Список изменений", "Создано с любовью к музыке и хорошему дизайну")
    AppLanguage.Ukrainian -> SettingsLanguageCopy("Налаштування", "Вигляд", "Тема", "Розмір тексту", "Мова", "Зараз використовується: ${language.nativeName}", "Звук", "Підсилення басів", "Просторовість", "Еквалайзер", "Увімкнути моно", "Перемикає стереовідтворення на моно", "Інші налаштування", "Сканувати бібліотеку", "Оновлює індекс для нових медіа", "Сканувати", "Перевірити оновлення", "Перевіряє, чи доступна нова версія", "Перевірити", "Зміни", "Створено з любов’ю до музики та гарного дизайну")
    AppLanguage.Latvian -> SettingsLanguageCopy("Iestatījumi", "Izskats", "Tēma", "Teksta izmērs", "Valoda", "Pašlaik lietota: ${language.nativeName}", "Skaņa", "Basa pastiprinājums", "Telpiskums", "Ekvalaizers", "Ieslēgt mono", "Pārslēdz stereo atskaņošanu uz mono", "Citi iestatījumi", "Skenēt bibliotēku", "Atjauno indeksu jauniem multivides failiem", "Skenēt", "Meklēt atjauninājumus", "Pārbauda, vai pieejama jauna versija", "Pārbaudīt", "Izmaiņas", "Radīts ar aizrautību pret mūziku un lielisku dizainu")
    AppLanguage.Italian -> SettingsLanguageCopy("Impostazioni", "Aspetto", "Tema", "Dimensione testo", "Lingua", "Attualmente in uso: ${language.nativeName}", "Suono", "Potenziamento bassi", "Spazialità", "Equalizzatore", "Attiva mono", "Passa la riproduzione stereo a mono", "Altre impostazioni", "Scansiona libreria", "Aggiorna l’indice per nuovi media", "Scansiona", "Cerca aggiornamenti", "Controlla se è disponibile una nuova versione", "Controlla", "Novità", "Progettato con passione per la musica e il buon design")
    AppLanguage.Japanese -> SettingsLanguageCopy("設定", "外観", "テーマ", "文字サイズ", "言語", "現在使用中: ${language.nativeName}", "サウンド", "低音ブースト", "空間感", "イコライザー", "モノラルを有効化", "ステレオ再生をモノラルに切り替えます", "その他の設定", "ライブラリをスキャン", "新しいメディアを探すためにインデックスを更新します", "スキャン", "アップデートを確認", "新しいバージョンが利用可能か確認します", "確認", "更新履歴", "音楽への情熱と優れたデザインで作られています")
    AppLanguage.Albanian -> SettingsLanguageCopy("Cilësimet", "Pamja", "Tema", "Madhësia e tekstit", "Gjuha", "Aktualisht në përdorim: ${language.nativeName}", "Tingulli", "Përforcim basi", "Hapësirë", "Ekualizuesi", "Aktivizo mono", "E kalon riprodhimin stereo në mono", "Cilësime të tjera", "Skano bibliotekën", "Rifreskon indeksimin për media të reja", "Skano", "Kontrollo për përditësime", "Kontrollon nëse ka version të ri", "Kontrollo", "Ndryshimet", "Dizajnuar me pasion për muzikën dhe dizajnin e mirë")
    AppLanguage.Hindi -> SettingsLanguageCopy("सेटिंग्स", "दिखावट", "थीम", "टेक्स्ट आकार", "भाषा", "वर्तमान में उपयोग: ${language.nativeName}", "ध्वनि", "बास बूस्ट", "स्पेशियसनेस", "इक्वलाइज़र", "मोनो चालू करें", "स्टीरियो प्लेबैक को मोनो में बदलता है", "अन्य सेटिंग्स", "लाइब्रेरी स्कैन करें", "नई मीडिया के लिए इंडेक्स ताज़ा करें", "स्कैन", "अपडेट जांचें", "नया संस्करण उपलब्ध है या नहीं जांचें", "जांचें", "बदलाव", "संगीत और अच्छे डिज़ाइन के प्रति जुनून से बनाया गया")
    AppLanguage.Hungarian -> SettingsLanguageCopy("Beállítások", "Megjelenés", "Téma", "Szövegméret", "Nyelv", "Jelenleg használt: ${language.nativeName}", "Hang", "Basszuskiemelés", "Térhatás", "Hangszínszabályzó", "Monó engedélyezése", "A sztereó lejátszást monóra váltja", "Egyéb beállítások", "Könyvtár beolvasása", "Frissíti az indexelést új médiához", "Beolvasás", "Frissítések keresése", "Ellenőrzi, hogy elérhető-e új verzió", "Ellenőrzés", "Változások", "Szenvedéllyel tervezve zenéhez és jó designhoz")
    AppLanguage.Latin -> SettingsLanguageCopy("Optiones", "Aspectus", "Thema", "Magnitudo textus", "Lingua", "Nunc adhibetur: ${language.nativeName}", "Sonus", "Bassus auctus", "Spatium", "Aequator", "Mono activa", "Playback stereo in mono vertit", "Aliae optiones", "Bibliothecam scrutare", "Indicem pro novis mediis renovat", "Scrutare", "Renovationes inspice", "Inspicit an nova versio praesto sit", "Inspice", "Mutationes", "Studio musicae et bono consilio creatum")
    AppLanguage.Macedonian -> SettingsLanguageCopy("Поставки", "Изглед", "Тема", "Големина на текст", "Јазик", "Моментално се користи: ${language.nativeName}", "Звук", "Засилување на бас", "Просторност", "Еквилајзер", "Вклучи моно", "Ја префрла стерео репродукцијата во моно", "Други поставки", "Скенирај библиотека", "Го освежува индексирањето за нови медиуми", "Скенирај", "Провери ажурирања", "Проверува дали има нова верзија", "Провери", "Промени", "Создадено со страст за музика и добар дизајн")
    AppLanguage.Serbian -> SettingsLanguageCopy("Подешавања", "Изглед", "Тема", "Величина текста", "Језик", "Тренутно се користи: ${language.nativeName}", "Звук", "Појачање баса", "Просторност", "Еквилајзер", "Укључи моно", "Пребацује стерео репродукцију у моно", "Остала подешавања", "Скенирај библиотеку", "Освежава индексирање за нове медије", "Скенирај", "Провери ажурирања", "Проверава да ли је доступна нова верзија", "Провери", "Промене", "Дизајнирано са страшћу за музику и добар дизајн")
    AppLanguage.Thai -> SettingsLanguageCopy("การตั้งค่า", "รูปลักษณ์", "ธีม", "ขนาดข้อความ", "ภาษา", "ใช้อยู่: ${language.nativeName}", "เสียง", "เพิ่มเสียงเบส", "มิติเสียง", "อีควอไลเซอร์", "เปิดโมโน", "เปลี่ยนการเล่นสเตอริโอเป็นโมโน", "การตั้งค่าอื่น", "สแกนคลังเพลง", "รีเฟรชดัชนีเพื่อค้นหาสื่อใหม่", "สแกน", "ตรวจสอบอัปเดต", "ตรวจสอบว่ามีเวอร์ชันใหม่หรือไม่", "ตรวจสอบ", "บันทึกการเปลี่ยนแปลง", "ออกแบบด้วยความหลงใหลในดนตรีและดีไซน์ที่ดี")
    AppLanguage.English -> SettingsLanguageCopy("Settings", "Appearance", "Theme", "Text size", "Language", "Currently used: ${language.nativeName}", "Sound", "Bass boost", "Spaciousness", "Equalizer", "Enable mono", "Switches stereo playback to mono", "Other settings", "Scan library", "Refresh indexing in search for new media", "Scan", "Check for updates", "Check if there's new version available", "Check", "Changelog", "Designed with passion for music and great design")
}

internal enum class UiPhrase {
    About,
    AddToPlaylist,
    AddToQueue,
    DeleteFromLibrary,
    Delete,
    Rename,
    RemoveFromList,
    NewPlaylist,
    Cancel,
    Create,
    Reset,
    Dry,
    Wet,
    Off,
    Reverb,
    ToneShaping,
    Bass,
    Midrange,
    Treble,
    EffectStrength,
}

internal fun uiPhrase(language: AppLanguage, phrase: UiPhrase): String {
    return uiPhraseTranslations[language]?.get(phrase) ?: uiPhraseTranslations.getValue(AppLanguage.English).getValue(phrase)
}

private val uiPhraseTranslations = mapOf(
    AppLanguage.English to mapOf(
        UiPhrase.About to "About",
        UiPhrase.AddToPlaylist to "Add to playlist",
        UiPhrase.AddToQueue to "Add to queue",
        UiPhrase.DeleteFromLibrary to "Delete from library",
        UiPhrase.Delete to "Delete",
        UiPhrase.Rename to "Rename",
        UiPhrase.RemoveFromList to "Remove from list",
        UiPhrase.NewPlaylist to "New playlist",
        UiPhrase.Cancel to "Cancel",
        UiPhrase.Create to "Create",
        UiPhrase.Reset to "Reset",
        UiPhrase.Dry to "Dry",
        UiPhrase.Wet to "Wet",
        UiPhrase.Off to "Off",
        UiPhrase.Reverb to "Reverb",
        UiPhrase.ToneShaping to "Tonal balance",
        UiPhrase.Bass to "Bass",
        UiPhrase.Midrange to "Midrange",
        UiPhrase.Treble to "Treble",
        UiPhrase.EffectStrength to "Effect strength",
    ),
    AppLanguage.Polish to mapOf(UiPhrase.About to "O aplikacji", UiPhrase.AddToPlaylist to "Dodaj do playlisty", UiPhrase.AddToQueue to "Dodaj do kolejki", UiPhrase.DeleteFromLibrary to "Usuń z biblioteki", UiPhrase.Delete to "Usuń", UiPhrase.Rename to "Zmień nazwę", UiPhrase.RemoveFromList to "Usuń z listy", UiPhrase.NewPlaylist to "Nowa playlista", UiPhrase.Cancel to "Anuluj", UiPhrase.Create to "Utwórz", UiPhrase.Reset to "Resetuj", UiPhrase.Dry to "Suchy", UiPhrase.Wet to "Mokry", UiPhrase.Off to "Wyłączone", UiPhrase.Reverb to "Pogłos", UiPhrase.ToneShaping to "Balans tonalny", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Środek", UiPhrase.Treble to "Góra", UiPhrase.EffectStrength to "Siła efektu"),
    AppLanguage.Albanian to mapOf(UiPhrase.About to "Rreth", UiPhrase.AddToPlaylist to "Shto në listë", UiPhrase.AddToQueue to "Shto në radhë", UiPhrase.DeleteFromLibrary to "Fshi nga biblioteka", UiPhrase.Delete to "Fshi", UiPhrase.Rename to "Riemërto", UiPhrase.RemoveFromList to "Hiq nga lista", UiPhrase.NewPlaylist to "Listë e re", UiPhrase.Cancel to "Anulo", UiPhrase.Create to "Krijo", UiPhrase.Reset to "Rivendos", UiPhrase.Dry to "I thatë", UiPhrase.Wet to "I lagësht", UiPhrase.Off to "Fikur", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Formësim toni", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mesatare", UiPhrase.Treble to "Të larta", UiPhrase.EffectStrength to "Fuqia e efektit"),
    AppLanguage.ChineseSimplified to mapOf(UiPhrase.About to "关于", UiPhrase.AddToPlaylist to "添加到播放列表", UiPhrase.AddToQueue to "添加到队列", UiPhrase.DeleteFromLibrary to "从媒体库删除", UiPhrase.Delete to "删除", UiPhrase.Rename to "重命名", UiPhrase.RemoveFromList to "从列表移除", UiPhrase.NewPlaylist to "新建播放列表", UiPhrase.Cancel to "取消", UiPhrase.Create to "创建", UiPhrase.Reset to "重置", UiPhrase.Dry to "干声", UiPhrase.Wet to "湿声", UiPhrase.Off to "关闭", UiPhrase.Reverb to "混响", UiPhrase.ToneShaping to "音色塑形", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中频", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "效果强度"),
    AppLanguage.Croatian to mapOf(UiPhrase.About to "O aplikaciji", UiPhrase.AddToPlaylist to "Dodaj na popis", UiPhrase.AddToQueue to "Dodaj u red", UiPhrase.DeleteFromLibrary to "Izbriši iz biblioteke", UiPhrase.Delete to "Izbriši", UiPhrase.Rename to "Preimenuj", UiPhrase.RemoveFromList to "Ukloni s popisa", UiPhrase.NewPlaylist to "Novi popis", UiPhrase.Cancel to "Odustani", UiPhrase.Create to "Stvori", UiPhrase.Reset to "Resetiraj", UiPhrase.Dry to "Suho", UiPhrase.Wet to "Mokro", UiPhrase.Off to "Isključeno", UiPhrase.Reverb to "Odjek", UiPhrase.ToneShaping to "Oblikovanje tona", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Srednji", UiPhrase.Treble to "Visoki", UiPhrase.EffectStrength to "Jačina efekta"),
    AppLanguage.Czech to mapOf(UiPhrase.About to "O aplikaci", UiPhrase.AddToPlaylist to "Přidat do playlistu", UiPhrase.AddToQueue to "Přidat do fronty", UiPhrase.DeleteFromLibrary to "Smazat z knihovny", UiPhrase.Delete to "Smazat", UiPhrase.Rename to "Přejmenovat", UiPhrase.RemoveFromList to "Odebrat ze seznamu", UiPhrase.NewPlaylist to "Nový playlist", UiPhrase.Cancel to "Zrušit", UiPhrase.Create to "Vytvořit", UiPhrase.Reset to "Resetovat", UiPhrase.Dry to "Suchý", UiPhrase.Wet to "Mokrý", UiPhrase.Off to "Vypnuto", UiPhrase.Reverb to "Dozvuk", UiPhrase.ToneShaping to "Tvarování tónu", UiPhrase.Bass to "Basy", UiPhrase.Midrange to "Středy", UiPhrase.Treble to "Výšky", UiPhrase.EffectStrength to "Síla efektu"),
    AppLanguage.Danish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Føj til playliste", UiPhrase.AddToQueue to "Føj til kø", UiPhrase.DeleteFromLibrary to "Slet fra bibliotek", UiPhrase.Delete to "Slet", UiPhrase.Rename to "Omdøb", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny playliste", UiPhrase.Cancel to "Annuller", UiPhrase.Create to "Opret", UiPhrase.Reset to "Nulstil", UiPhrase.Dry to "Tør", UiPhrase.Wet to "Våd", UiPhrase.Off to "Fra", UiPhrase.Reverb to "Rumklang", UiPhrase.ToneShaping to "Toneformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellemtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Dutch to mapOf(UiPhrase.About to "Over", UiPhrase.AddToPlaylist to "Toevoegen aan afspeellijst", UiPhrase.AddToQueue to "Toevoegen aan wachtrij", UiPhrase.DeleteFromLibrary to "Verwijderen uit bibliotheek", UiPhrase.Delete to "Verwijderen", UiPhrase.Rename to "Naam wijzigen", UiPhrase.RemoveFromList to "Uit lijst verwijderen", UiPhrase.NewPlaylist to "Nieuwe afspeellijst", UiPhrase.Cancel to "Annuleren", UiPhrase.Create to "Maken", UiPhrase.Reset to "Resetten", UiPhrase.Dry to "Droog", UiPhrase.Wet to "Nat", UiPhrase.Off to "Uit", UiPhrase.Reverb to "Galm", UiPhrase.ToneShaping to "Toonvorming", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Midden", UiPhrase.Treble to "Hoge tonen", UiPhrase.EffectStrength to "Effectsterkte"),
    AppLanguage.Estonian to mapOf(UiPhrase.About to "Teave", UiPhrase.AddToPlaylist to "Lisa esitusloendisse", UiPhrase.AddToQueue to "Lisa järjekorda", UiPhrase.DeleteFromLibrary to "Kustuta teegist", UiPhrase.Delete to "Kustuta", UiPhrase.Rename to "Nimeta ümber", UiPhrase.RemoveFromList to "Eemalda loendist", UiPhrase.NewPlaylist to "Uus esitusloend", UiPhrase.Cancel to "Tühista", UiPhrase.Create to "Loo", UiPhrase.Reset to "Lähtesta", UiPhrase.Dry to "Kuiv", UiPhrase.Wet to "Märg", UiPhrase.Off to "Väljas", UiPhrase.Reverb to "Kaja", UiPhrase.ToneShaping to "Tooni kujundus", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Keskvahemik", UiPhrase.Treble to "Kõrged", UiPhrase.EffectStrength to "Efekti tugevus"),
    AppLanguage.French to mapOf(UiPhrase.About to "À propos", UiPhrase.AddToPlaylist to "Ajouter à une playlist", UiPhrase.AddToQueue to "Ajouter à la file", UiPhrase.DeleteFromLibrary to "Supprimer de la bibliothèque", UiPhrase.Delete to "Supprimer", UiPhrase.Rename to "Renommer", UiPhrase.RemoveFromList to "Retirer de la liste", UiPhrase.NewPlaylist to "Nouvelle playlist", UiPhrase.Cancel to "Annuler", UiPhrase.Create to "Créer", UiPhrase.Reset to "Réinitialiser", UiPhrase.Dry to "Sec", UiPhrase.Wet to "Humide", UiPhrase.Off to "Désactivé", UiPhrase.Reverb to "Réverbération", UiPhrase.ToneShaping to "Modelage du son", UiPhrase.Bass to "Basses", UiPhrase.Midrange to "Médiums", UiPhrase.Treble to "Aigus", UiPhrase.EffectStrength to "Intensité de l’effet"),
    AppLanguage.German to mapOf(UiPhrase.About to "Über", UiPhrase.AddToPlaylist to "Zur Playlist hinzufügen", UiPhrase.AddToQueue to "Zur Warteschlange hinzufügen", UiPhrase.DeleteFromLibrary to "Aus Bibliothek löschen", UiPhrase.Delete to "Löschen", UiPhrase.Rename to "Umbenennen", UiPhrase.RemoveFromList to "Aus Liste entfernen", UiPhrase.NewPlaylist to "Neue Playlist", UiPhrase.Cancel to "Abbrechen", UiPhrase.Create to "Erstellen", UiPhrase.Reset to "Zurücksetzen", UiPhrase.Dry to "Trocken", UiPhrase.Wet to "Nass", UiPhrase.Off to "Aus", UiPhrase.Reverb to "Hall", UiPhrase.ToneShaping to "Klangformung", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mitten", UiPhrase.Treble to "Höhen", UiPhrase.EffectStrength to "Effektstärke"),
    AppLanguage.Greek to mapOf(UiPhrase.About to "Σχετικά", UiPhrase.AddToPlaylist to "Προσθήκη σε playlist", UiPhrase.AddToQueue to "Προσθήκη στην ουρά", UiPhrase.DeleteFromLibrary to "Διαγραφή από βιβλιοθήκη", UiPhrase.Delete to "Διαγραφή", UiPhrase.Rename to "Μετονομασία", UiPhrase.RemoveFromList to "Αφαίρεση από λίστα", UiPhrase.NewPlaylist to "Νέο playlist", UiPhrase.Cancel to "Άκυρο", UiPhrase.Create to "Δημιουργία", UiPhrase.Reset to "Επαναφορά", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Ανενεργό", UiPhrase.Reverb to "Αντήχηση", UiPhrase.ToneShaping to "Διαμόρφωση τόνου", UiPhrase.Bass to "Μπάσα", UiPhrase.Midrange to "Μεσαία", UiPhrase.Treble to "Πρίμα", UiPhrase.EffectStrength to "Ένταση εφέ"),
    AppLanguage.Hindi to mapOf(UiPhrase.About to "परिचय", UiPhrase.AddToPlaylist to "प्लेलिस्ट में जोड़ें", UiPhrase.AddToQueue to "कतार में जोड़ें", UiPhrase.DeleteFromLibrary to "लाइब्रेरी से हटाएं", UiPhrase.Delete to "हटाएं", UiPhrase.Rename to "नाम बदलें", UiPhrase.RemoveFromList to "सूची से हटाएं", UiPhrase.NewPlaylist to "नई प्लेलिस्ट", UiPhrase.Cancel to "रद्द करें", UiPhrase.Create to "बनाएं", UiPhrase.Reset to "रीसेट", UiPhrase.Dry to "ड्राई", UiPhrase.Wet to "वेट", UiPhrase.Off to "बंद", UiPhrase.Reverb to "रीवर्ब", UiPhrase.ToneShaping to "टोन शेपिंग", UiPhrase.Bass to "बास", UiPhrase.Midrange to "मिडरेंज", UiPhrase.Treble to "ट्रेबल", UiPhrase.EffectStrength to "प्रभाव शक्ति"),
    AppLanguage.Hungarian to mapOf(UiPhrase.About to "Névjegy", UiPhrase.AddToPlaylist to "Hozzáadás lejátszási listához", UiPhrase.AddToQueue to "Hozzáadás a sorhoz", UiPhrase.DeleteFromLibrary to "Törlés a könyvtárból", UiPhrase.Delete to "Törlés", UiPhrase.Rename to "Átnevezés", UiPhrase.RemoveFromList to "Eltávolítás a listából", UiPhrase.NewPlaylist to "Új lejátszási lista", UiPhrase.Cancel to "Mégse", UiPhrase.Create to "Létrehozás", UiPhrase.Reset to "Visszaállítás", UiPhrase.Dry to "Száraz", UiPhrase.Wet to "Nedves", UiPhrase.Off to "Ki", UiPhrase.Reverb to "Visszhang", UiPhrase.ToneShaping to "Hangformálás", UiPhrase.Bass to "Basszus", UiPhrase.Midrange to "Közép", UiPhrase.Treble to "Magas", UiPhrase.EffectStrength to "Effekt erőssége"),
    AppLanguage.Italian to mapOf(UiPhrase.About to "Informazioni", UiPhrase.AddToPlaylist to "Aggiungi alla playlist", UiPhrase.AddToQueue to "Aggiungi alla coda", UiPhrase.DeleteFromLibrary to "Elimina dalla libreria", UiPhrase.Delete to "Elimina", UiPhrase.Rename to "Rinomina", UiPhrase.RemoveFromList to "Rimuovi dalla lista", UiPhrase.NewPlaylist to "Nuova playlist", UiPhrase.Cancel to "Annulla", UiPhrase.Create to "Crea", UiPhrase.Reset to "Ripristina", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Disattivato", UiPhrase.Reverb to "Riverbero", UiPhrase.ToneShaping to "Modellazione tono", UiPhrase.Bass to "Bassi", UiPhrase.Midrange to "Medi", UiPhrase.Treble to "Alti", UiPhrase.EffectStrength to "Intensità effetto"),
    AppLanguage.Japanese to mapOf(UiPhrase.About to "情報", UiPhrase.AddToPlaylist to "プレイリストに追加", UiPhrase.AddToQueue to "キューに追加", UiPhrase.DeleteFromLibrary to "ライブラリから削除", UiPhrase.Delete to "削除", UiPhrase.Rename to "名前を変更", UiPhrase.RemoveFromList to "リストから削除", UiPhrase.NewPlaylist to "新しいプレイリスト", UiPhrase.Cancel to "キャンセル", UiPhrase.Create to "作成", UiPhrase.Reset to "リセット", UiPhrase.Dry to "ドライ", UiPhrase.Wet to "ウェット", UiPhrase.Off to "オフ", UiPhrase.Reverb to "リバーブ", UiPhrase.ToneShaping to "音色調整", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中域", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "エフェクト強度"),
    AppLanguage.Latin to mapOf(UiPhrase.About to "De app", UiPhrase.AddToPlaylist to "Ad indicem adde", UiPhrase.AddToQueue to "Ad ordinem adde", UiPhrase.DeleteFromLibrary to "E bibliotheca dele", UiPhrase.Delete to "Dele", UiPhrase.Rename to "Renomina", UiPhrase.RemoveFromList to "E indice remove", UiPhrase.NewPlaylist to "Novus index", UiPhrase.Cancel to "Rescinde", UiPhrase.Create to "Crea", UiPhrase.Reset to "Restitue", UiPhrase.Dry to "Siccus", UiPhrase.Wet to "Humidus", UiPhrase.Off to "Exstinctum", UiPhrase.Reverb to "Reverberatio", UiPhrase.ToneShaping to "Formatio toni", UiPhrase.Bass to "Bassus", UiPhrase.Midrange to "Media", UiPhrase.Treble to "Acuti", UiPhrase.EffectStrength to "Vis effectus"),
    AppLanguage.Latvian to mapOf(UiPhrase.About to "Par", UiPhrase.AddToPlaylist to "Pievienot atskaņošanas sarakstam", UiPhrase.AddToQueue to "Pievienot rindai", UiPhrase.DeleteFromLibrary to "Dzēst no bibliotēkas", UiPhrase.Delete to "Dzēst", UiPhrase.Rename to "Pārdēvēt", UiPhrase.RemoveFromList to "Noņemt no saraksta", UiPhrase.NewPlaylist to "Jauns saraksts", UiPhrase.Cancel to "Atcelt", UiPhrase.Create to "Izveidot", UiPhrase.Reset to "Atiestatīt", UiPhrase.Dry to "Sauss", UiPhrase.Wet to "Mitrs", UiPhrase.Off to "Izslēgts", UiPhrase.Reverb to "Atbalss", UiPhrase.ToneShaping to "Toņa veidošana", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Vidējās", UiPhrase.Treble to "Augšas", UiPhrase.EffectStrength to "Efekta stiprums"),
    AppLanguage.Lithuanian to mapOf(UiPhrase.About to "Apie", UiPhrase.AddToPlaylist to "Pridėti į grojaraštį", UiPhrase.AddToQueue to "Pridėti į eilę", UiPhrase.DeleteFromLibrary to "Ištrinti iš bibliotekos", UiPhrase.Delete to "Ištrinti", UiPhrase.Rename to "Pervadinti", UiPhrase.RemoveFromList to "Pašalinti iš sąrašo", UiPhrase.NewPlaylist to "Naujas grojaraštis", UiPhrase.Cancel to "Atšaukti", UiPhrase.Create to "Sukurti", UiPhrase.Reset to "Atstatyti", UiPhrase.Dry to "Sausas", UiPhrase.Wet to "Šlapias", UiPhrase.Off to "Išjungta", UiPhrase.Reverb to "Aidas", UiPhrase.ToneShaping to "Tono formavimas", UiPhrase.Bass to "Bosai", UiPhrase.Midrange to "Viduriai", UiPhrase.Treble to "Aukšti", UiPhrase.EffectStrength to "Efekto stiprumas"),
    AppLanguage.Macedonian to mapOf(UiPhrase.About to "За апликацијата", UiPhrase.AddToPlaylist to "Додај во плејлиста", UiPhrase.AddToQueue to "Додај во редица", UiPhrase.DeleteFromLibrary to "Избриши од библиотека", UiPhrase.Delete to "Избриши", UiPhrase.Rename to "Преименувај", UiPhrase.RemoveFromList to "Отстрани од листа", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетирај", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Влажно", UiPhrase.Off to "Исклучено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликување тон", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средни", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Сила на ефект"),
    AppLanguage.Norwegian to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Legg til i spilleliste", UiPhrase.AddToQueue to "Legg til i kø", UiPhrase.DeleteFromLibrary to "Slett fra bibliotek", UiPhrase.Delete to "Slett", UiPhrase.Rename to "Gi nytt navn", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny spilleliste", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Opprett", UiPhrase.Reset to "Tilbakestill", UiPhrase.Dry to "Tørr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Romklang", UiPhrase.ToneShaping to "Toneforming", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mellomtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Portuguese to mapOf(UiPhrase.About to "Sobre", UiPhrase.AddToPlaylist to "Adicionar à playlist", UiPhrase.AddToQueue to "Adicionar à fila", UiPhrase.DeleteFromLibrary to "Eliminar da biblioteca", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renomear", UiPhrase.RemoveFromList to "Remover da lista", UiPhrase.NewPlaylist to "Nova playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Criar", UiPhrase.Reset to "Repor", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Molhado", UiPhrase.Off to "Desligado", UiPhrase.Reverb to "Reverberação", UiPhrase.ToneShaping to "Modelação de tom", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Médios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Força do efeito"),
    AppLanguage.Russian to mapOf(UiPhrase.About to "О приложении", UiPhrase.AddToPlaylist to "Добавить в плейлист", UiPhrase.AddToQueue to "Добавить в очередь", UiPhrase.DeleteFromLibrary to "Удалить из библиотеки", UiPhrase.Delete to "Удалить", UiPhrase.Rename to "Переименовать", UiPhrase.RemoveFromList to "Убрать из списка", UiPhrase.NewPlaylist to "Новый плейлист", UiPhrase.Cancel to "Отмена", UiPhrase.Create to "Создать", UiPhrase.Reset to "Сбросить", UiPhrase.Dry to "Сухой", UiPhrase.Wet to "Мокрый", UiPhrase.Off to "Выкл.", UiPhrase.Reverb to "Реверберация", UiPhrase.ToneShaping to "Формирование тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верх", UiPhrase.EffectStrength to "Сила эффекта"),
    AppLanguage.Serbian to mapOf(UiPhrase.About to "О апликацији", UiPhrase.AddToPlaylist to "Додај у плејлисту", UiPhrase.AddToQueue to "Додај у ред", UiPhrase.DeleteFromLibrary to "Обриши из библиотеке", UiPhrase.Delete to "Обриши", UiPhrase.Rename to "Преименуј", UiPhrase.RemoveFromList to "Уклони са листе", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетуј", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Мокро", UiPhrase.Off to "Искључено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликовање тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средњи", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Јачина ефекта"),
    AppLanguage.Spanish to mapOf(UiPhrase.About to "Acerca de", UiPhrase.AddToPlaylist to "Añadir a playlist", UiPhrase.AddToQueue to "Añadir a la cola", UiPhrase.DeleteFromLibrary to "Eliminar de la biblioteca", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renombrar", UiPhrase.RemoveFromList to "Quitar de la lista", UiPhrase.NewPlaylist to "Nueva playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Crear", UiPhrase.Reset to "Restablecer", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Húmedo", UiPhrase.Off to "Desactivado", UiPhrase.Reverb to "Reverberación", UiPhrase.ToneShaping to "Modelado de tono", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Medios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Intensidad del efecto"),
    AppLanguage.Swedish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Lägg till i spellista", UiPhrase.AddToQueue to "Lägg till i kö", UiPhrase.DeleteFromLibrary to "Ta bort från bibliotek", UiPhrase.Delete to "Ta bort", UiPhrase.Rename to "Byt namn", UiPhrase.RemoveFromList to "Ta bort från lista", UiPhrase.NewPlaylist to "Ny spellista", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Skapa", UiPhrase.Reset to "Återställ", UiPhrase.Dry to "Torr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Efterklang", UiPhrase.ToneShaping to "Tonformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellanregister", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrka"),
    AppLanguage.Thai to mapOf(UiPhrase.About to "เกี่ยวกับ", UiPhrase.AddToPlaylist to "เพิ่มไปยังเพลย์ลิสต์", UiPhrase.AddToQueue to "เพิ่มไปยังคิว", UiPhrase.DeleteFromLibrary to "ลบจากคลัง", UiPhrase.Delete to "ลบ", UiPhrase.Rename to "เปลี่ยนชื่อ", UiPhrase.RemoveFromList to "ลบออกจากรายการ", UiPhrase.NewPlaylist to "เพลย์ลิสต์ใหม่", UiPhrase.Cancel to "ยกเลิก", UiPhrase.Create to "สร้าง", UiPhrase.Reset to "รีเซ็ต", UiPhrase.Dry to "แห้ง", UiPhrase.Wet to "เปียก", UiPhrase.Off to "ปิด", UiPhrase.Reverb to "รีเวิร์บ", UiPhrase.ToneShaping to "ปรับโทนเสียง", UiPhrase.Bass to "เบส", UiPhrase.Midrange to "เสียงกลาง", UiPhrase.Treble to "เสียงแหลม", UiPhrase.EffectStrength to "ความแรงของเอฟเฟกต์"),
    AppLanguage.Ukrainian to mapOf(UiPhrase.About to "Про застосунок", UiPhrase.AddToPlaylist to "Додати до плейлиста", UiPhrase.AddToQueue to "Додати до черги", UiPhrase.DeleteFromLibrary to "Видалити з бібліотеки", UiPhrase.Delete to "Видалити", UiPhrase.Rename to "Перейменувати", UiPhrase.RemoveFromList to "Прибрати зі списку", UiPhrase.NewPlaylist to "Новий плейлист", UiPhrase.Cancel to "Скасувати", UiPhrase.Create to "Створити", UiPhrase.Reset to "Скинути", UiPhrase.Dry to "Сухий", UiPhrase.Wet to "Мокрий", UiPhrase.Off to "Вимкнено", UiPhrase.Reverb to "Реверберація", UiPhrase.ToneShaping to "Формування тону", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верхи", UiPhrase.EffectStrength to "Сила ефекту"),
)

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    textSizePreset: TextSizePreset,
    appLanguage: AppLanguage,
    eqSettings: EqSettings,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onTextSizePresetSelected: (TextSizePreset) -> Unit,
    onAppLanguageSelected: (AppLanguage) -> Unit,
    onBassChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onMonoPlaybackChanged: (Boolean) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onScanLibrary: () -> Unit,
    onCheckForUpdates: () -> Unit,
) {
    val listState = rememberElovaireLazyListState("settings_screen")
    val copy = remember(appLanguage) { settingsCopy(appLanguage) }
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
                bottom = bottomPadding + buttonNavigationScrollBoost(),
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsSectionHeader(
                    title = copy.appearance,
                    iconResId = R.drawable.ic_lucide_palette,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SectionTitleRow(
                            title = copy.theme,
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
                                title = copy.textSize,
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
                        LanguagePickerRow(
                            selectedLanguage = appLanguage,
                            copy = copy,
                            onLanguageSelected = onAppLanguageSelected,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = copy.sound,
                    iconResId = R.drawable.ic_lucide_volume_2,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(30.dp),
                        ) {
                            DigitalSoundKnob(
                                title = copy.bassBoost,
                                iconResId = R.drawable.ic_lucide_speaker,
                                value = eqSettings.bass.coerceAtLeast(0f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f),
                                onValueChange = onBassChanged,
                            )
                            DigitalSoundKnob(
                                title = copy.spaciousness,
                                iconResId = R.drawable.ic_lucide_wind,
                                value = eqSettings.spaciousness.coerceAtLeast(0f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f),
                                onValueChange = onSpaciousnessChanged,
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Surface(
                                modifier = Modifier.elovairePressBounce(
                                    interactionSource = interactionSource,
                                    label = "settings_equalizer_button_scale",
                                ),
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            onClick = onOpenEqualizer,
                                        )
                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_audio_waveform),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = copy.equalizer,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        SettingToggleRow(
                            title = copy.enableMono,
                            subtitle = copy.monoSubtitle,
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
                    title = copy.otherSettings,
                    iconResId = R.drawable.ic_lucide_settings,
                )
            }

            item {
                ModuleCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        SettingActionRow(
                            title = copy.scanLibrary,
                            subtitle = copy.scanLibrarySubtitle,
                            actionLabel = copy.scan,
                            onAction = onScanLibrary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        SettingActionRow(
                            title = copy.checkUpdates,
                            subtitle = copy.checkUpdatesSubtitle,
                            actionLabel = copy.check,
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
                                    Color.White.copy(alpha = 0.05f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
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
                                        text = copy.changelog,
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
                            text = commonUiCopy(appLanguage).refinedFooter,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = copy.settings,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun LanguagePickerRow(
    selectedLanguage: AppLanguage,
    copy: SettingsLanguageCopy,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = copy.language,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = copy.currentlyUsed,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Box {
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier.elovairePressBounce(
                    interactionSource = interactionSource,
                    label = "settings_language_button_scale",
                ),
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { expanded = true },
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_languages),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = selectedLanguage.nativeName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
    if (expanded) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            title = copy.language,
            onDismiss = { expanded = false },
            onConfirm = { language ->
                expanded = false
                onLanguageSelected(language)
            },
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedLanguage: AppLanguage,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (AppLanguage) -> Unit,
) {
    val listState = rememberElovaireLazyListState("language_picker")
    val languages = remember {
        AppLanguage.entries.sortedBy { it.englishName }
    }
    var pendingLanguage by rememberSaveable(selectedLanguage) { mutableStateOf(selectedLanguage) }
    val visibleRows = 5
    val rowHeight = 56.dp
    val rowSpacing = 2.dp
    val listHeight = (rowHeight * visibleRows) + (rowSpacing * (visibleRows - 1))

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
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateContentSize(animationSpec = ElovaireMotion.sizeSoft()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_languages),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight),
                    ) {
                        LazyColumn(
                            state = listState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .ensureSingleItemRubberBand(listState),
                            verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        ) {
                            items(languages, key = { it.name }) { language ->
                                LanguagePickerOptionRow(
                                    language = language,
                                    selected = language == pendingLanguage,
                                    modifier = Modifier.animateItem(
                                        placementSpec = spring(
                                            dampingRatio = 0.76f,
                                            stiffness = 360f,
                                        ),
                                    ),
                                    onClick = { pendingLanguage = language },
                                )
                            }
                        }
                        FastScrollbar(
                            state = listState,
                            topInset = 0.dp,
                            bottomInset = 0.dp,
                            modifier = Modifier.padding(end = 2.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(selectedLanguage, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = { onConfirm(pendingLanguage) },
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text(
                                text = "OK",
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
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
private fun LanguagePickerOptionRow(
    language: AppLanguage,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val highlightColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "language_picker_row_highlight",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(highlightColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_circle),
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
                    },
                    modifier = Modifier.size(20.dp),
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(animationSpec = tween(40)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = ElovaireMotion.releaseSpringSpec(),
                    ),
                    exit = fadeOut(animationSpec = tween(20)) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(20),
                    ),
                    label = "language_picker_check",
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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

private fun ReverbProfile.displayLabel(): String {
    return when (this) {
        ReverbProfile.Dry -> "Dry"
        ReverbProfile.Wet -> "Wet"
    }
}

@Composable
private fun ReverbStepSlider(
    valueMs: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = remember { (0..500 step 50).toList() }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val selectedValue = steps.minByOrNull { kotlin.math.abs(it - valueMs.coerceIn(0, 500)) } ?: 0
    val selectedIndex = steps.indexOf(selectedValue).coerceAtLeast(0)
    val maxIndex = (steps.size - 1).coerceAtLeast(1)
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

    BoxWithConstraints(
        modifier = modifier
            .height(36.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val trackStartPx = knobSizePx / 2f
        val trackWidthPx = (maxWidthPx - knobSizePx).coerceAtLeast(1f)
        val selectedCenterPx = trackStartPx + (trackWidthPx * (selectedIndex.toFloat() / maxIndex.toFloat()))
        val stepCenters = remember(maxWidthPx, maxIndex) {
            steps.indices.map { index ->
                if (maxIndex == 0) {
                    maxWidthPx / 2f
                } else {
                    trackStartPx + (trackWidthPx * (index.toFloat() / maxIndex.toFloat()))
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
            label = "reverb_step_knob_offset",
        )
        val updateFromPosition: (Float) -> Unit = { xPosition ->
            val clampedX = xPosition.coerceIn(trackStartPx, trackStartPx + trackWidthPx)
            dragCenterPx = clampedX
            val targetIndex = stepCenters
                .withIndex()
                .minByOrNull { (_, center) -> kotlin.math.abs(center - clampedX) }
                ?.index
                ?: selectedIndex
            val targetValue = steps[targetIndex]
            if (targetValue != selectedValue) {
                currentOnValueChange(targetValue)
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
                .pointerInput(maxWidthPx, steps.size) {
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
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = with(density) { trackStartPx.toDp() })
                    .width(with(density) { trackWidthPx.toDp() })
                    .height(2.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(lineColor),
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val selectedDotRadius = 3.5.dp.toPx()
                val defaultDotRadius = 2.5.dp.toPx()
                val centerY = size.height / 2f
                    steps.forEachIndexed { index, _ ->
                        val fraction = if (maxIndex == 0) 0f else index.toFloat() / maxIndex.toFloat()
                        drawCircle(
                            color = dotColor,
                            radius = if (index == selectedIndex) selectedDotRadius else defaultDotRadius,
                            center = Offset(trackStartPx + (trackWidthPx * fraction), centerY),
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
    val interactionSource = remember { MutableInteractionSource() }
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
            modifier = Modifier.elovairePressBounce(
                interactionSource = interactionSource,
                label = "${actionLabel}_setting_action_scale",
            ),
            shape = RoundedCornerShape(ElovaireRadii.pill),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onAction,
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
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
        animationSpec = tween(60),
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
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
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
                            text = when (option) {
                                ThemeMode.Light -> common.light
                                ThemeMode.Dark -> common.dark
                                ThemeMode.System -> common.system
                            },
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
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
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
                val tickInnerRadius = (tickOuterRadius - 6.dp.toPx()).coerceAtLeast(1f)
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
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(20f)),
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
            modifier = Modifier
                .offset(y = (-28).dp),
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
private fun EqToneKnob(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    val safeRange = remember(valueRange) {
        if (valueRange.endInclusive > valueRange.start) valueRange else 0f..1f
    }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val clampedValue = value.coerceIn(safeRange.start, safeRange.endInclusive)
    val targetFraction = ((clampedValue - safeRange.start) / (safeRange.endInclusive - safeRange.start))
        .coerceIn(0f, 1f)
    var dragFraction by remember { mutableFloatStateOf(targetFraction) }
    LaunchedEffect(targetFraction) {
        dragFraction = targetFraction
    }
    val animatedFraction by animateFloatAsState(
        targetValue = dragFraction,
        animationSpec = tween(ElovaireMotion.Standard),
        label = "${title}_eq_tone_knob",
    )
    val tickIdleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val glowColor = accentColor.copy(alpha = 0.28f)
    val knobFaceColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.92f)
    } else {
        Color(0xFF1A1A1C)
    }
    val knobEdgeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val valueText = remember(clampedValue, safeRange) {
        val percent = if (safeRange.start < 0f) {
            (clampedValue * 100f).roundToInt()
        } else {
            ((clampedValue.coerceAtLeast(0f)) * 100f).roundToInt()
        }
        if (safeRange.start < 0f && percent > 0) "+$percent" else percent.toString()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .horizontalGestureSafe()
                .pointerInput(title, safeRange.start, safeRange.endInclusive) {
                    detectTapGestures { offset ->
                        val fraction = circularKnobValueForOffset(
                            offset = offset,
                            size = Size(size.width.toFloat(), size.height.toFloat()),
                            startAngleDegrees = 140f,
                            sweepAngleDegrees = 260f,
                        )
                        dragFraction = fraction
                        currentOnValueChange(
                            safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                        )
                    }
                }
                .pointerInput(title, safeRange.start, safeRange.endInclusive) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val fraction = circularKnobValueForOffset(
                                offset = offset,
                                size = Size(size.width.toFloat(), size.height.toFloat()),
                                startAngleDegrees = 140f,
                                sweepAngleDegrees = 260f,
                            )
                            dragFraction = fraction
                            currentOnValueChange(
                                safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = circularKnobValueForOffset(
                                offset = change.position,
                                size = Size(size.width.toFloat(), size.height.toFloat()),
                                startAngleDegrees = 140f,
                                sweepAngleDegrees = 260f,
                            )
                            dragFraction = fraction
                            currentOnValueChange(
                                safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                            )
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val glowWidth = 12.dp.toPx()
                val outerPadding = 9.dp.toPx()
                val radius = ((size.minDimension - outerPadding * 2f) / 2f).coerceAtLeast(1f)
                val center = Offset(size.width / 2f, size.height / 2f)
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)
                val startAngle = 140f
                val sweepAngle = 260f
                val activeSweep = sweepAngle * animatedFraction
                val tickCount = 34
                val tickOuterRadius = radius + 5.dp.toPx()
                val tickInnerRadius = tickOuterRadius - 6.dp.toPx()
                val activeTickCount = (animatedFraction * (tickCount - 1)).roundToInt()

                repeat(tickCount) { tickIndex ->
                    val fraction = tickIndex / (tickCount - 1).toFloat()
                    val angleDegrees = startAngle + (sweepAngle * fraction)
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
                        color = if (tickIndex <= activeTickCount) accentColor else tickIdleColor,
                        start = start,
                        end = end,
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Square,
                    )
                }
                if (activeSweep > 0f) {
                    drawArc(
                        color = glowColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = glowWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                drawCircle(
                    color = knobFaceColor,
                    radius = radius * 0.64f,
                    center = center,
                )
                drawCircle(
                    color = knobEdgeColor,
                    radius = radius * 0.66f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )

                val pointerAngle = Math.toRadians((startAngle + activeSweep).toDouble())
                val pointerRadius = radius * 0.56f
                val pointerCenter = Offset(
                    x = center.x + (cos(pointerAngle) * pointerRadius).toFloat(),
                    y = center.y + (sin(pointerAngle) * pointerRadius).toFloat(),
                )
                drawCircle(
                    color = accentColor,
                    radius = 3.dp.toPx(),
                    center = pointerCenter,
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.96f),
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = elovaireScaledSp(10f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                ),
            )
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
    val language = LocalAppLanguage.current
    val presets = remember {
        listOf(
            eqPreset("Electronic", 0.40f, 0.58f, 0.42f, 0.26f, 0.10f, 0.08f, 0.16f, 0.24f),
            eqPreset("Jazz", 0.18f, 0.26f, 0.14f, -0.08f, 0.10f, 0.22f, 0.18f, 0.12f),
            eqPreset("Classical", 0.12f, 0.16f, 0.08f, -0.04f, 0.06f, 0.18f, 0.24f, 0.18f),
            eqPreset("Acoustic", 0.10f, 0.14f, 0.06f, -0.12f, 0.14f, 0.20f, 0.18f, 0.10f),
            eqPreset("Pop", 0.22f, 0.28f, 0.16f, -0.06f, 0.18f, 0.24f, 0.18f, 0.10f),
            eqPreset("Rock", 0.28f, 0.22f, 0.10f, -0.12f, 0.08f, 0.18f, 0.28f, 0.22f),
            eqPreset("Metal", 0.22f, 0.18f, 0.08f, -0.14f, 0.12f, 0.24f, 0.30f, 0.26f),
            eqPreset("Vocal", -0.08f, -0.12f, -0.04f, -0.10f, 0.20f, 0.28f, 0.16f, 0.06f),
            eqPreset("R&B", 0.28f, 0.32f, 0.20f, -0.06f, 0.20f, 0.22f, 0.10f, 0.06f),
            eqPreset("Soul", 0.20f, 0.24f, 0.18f, -0.04f, 0.18f, 0.24f, 0.10f, 0.04f),
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
            label = uiPhrase(language, UiPhrase.Reset),
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
    val language = LocalAppLanguage.current
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
                label = mode.displayLabel(language),
                selected = spaciousnessAmount > 0.001f && mode == currentMode,
                useSubtleIdleBackground = true,
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

private fun SpaciousnessMode.displayLabel(language: AppLanguage = AppLanguage.English): String {
    return when (this) {
        SpaciousnessMode.Off -> uiPhrase(language, UiPhrase.Off)
        SpaciousnessMode.StereoWidth -> when (language) {
            AppLanguage.Albanian -> "Gjerësi stereo"
            AppLanguage.Polish -> "Szerokość stereo"
            AppLanguage.Hindi -> "स्टीरियो चौड़ाई"
            AppLanguage.Hungarian -> "Sztereó szélesség"
            AppLanguage.German -> "Stereo-Breite"
            AppLanguage.French -> "Largeur stéréo"
            AppLanguage.Spanish -> "Amplitud estéreo"
            AppLanguage.Italian -> "Ampiezza stereo"
            AppLanguage.Latin -> "Latitudo stereo"
            AppLanguage.Portuguese -> "Largura estéreo"
            AppLanguage.Dutch -> "Stereo-breedte"
            AppLanguage.Swedish -> "Stereobredd"
            AppLanguage.Norwegian -> "Stereobredde"
            AppLanguage.Danish -> "Stereobredde"
            AppLanguage.Czech -> "Šířka sterea"
            AppLanguage.Croatian -> "Stereo širina"
            AppLanguage.Lithuanian -> "Stereo plotis"
            AppLanguage.Latvian -> "Stereo platums"
            AppLanguage.Estonian -> "Stereo laius"
            AppLanguage.Greek -> "Πλάτος stereo"
            AppLanguage.Macedonian -> "Стерео ширина"
            AppLanguage.Russian -> "Ширина стерео"
            AppLanguage.Serbian -> "Ширина стереа"
            AppLanguage.Thai -> "ความกว้างสเตอริโอ"
            AppLanguage.Ukrainian -> "Ширина стерео"
            AppLanguage.ChineseSimplified -> "立体声宽度"
            AppLanguage.Japanese -> "ステレオ幅"
            else -> "Stereo Width"
        }
        SpaciousnessMode.CrossfeedDepth -> when (language) {
            AppLanguage.Albanian -> "Përzierje kanalesh"
            AppLanguage.Polish -> "Przenikanie kanałów"
            AppLanguage.Hindi -> "क्रॉसफीड"
            AppLanguage.Hungarian -> "Crossfeed"
            AppLanguage.German -> "Crossfeed"
            AppLanguage.French -> "Crossfeed"
            AppLanguage.Spanish -> "Crossfeed"
            AppLanguage.Italian -> "Crossfeed"
            AppLanguage.Latin -> "Canales mixti"
            AppLanguage.Portuguese -> "Crossfeed"
            AppLanguage.Dutch -> "Crossfeed"
            AppLanguage.Swedish -> "Crossfeed"
            AppLanguage.Norwegian -> "Crossfeed"
            AppLanguage.Danish -> "Crossfeed"
            AppLanguage.Czech -> "Crossfeed"
            AppLanguage.Croatian -> "Crossfeed"
            AppLanguage.Lithuanian -> "Kanalų susiliejimas"
            AppLanguage.Latvian -> "Kanālu sajaukums"
            AppLanguage.Estonian -> "Kanalite segamine"
            AppLanguage.Greek -> "Crossfeed"
            AppLanguage.Macedonian -> "Вкрстено мешање"
            AppLanguage.Russian -> "Кроссфид"
            AppLanguage.Serbian -> "Кросфид"
            AppLanguage.Thai -> "ครอสฟีด"
            AppLanguage.Ukrainian -> "Кросфід"
            AppLanguage.ChineseSimplified -> "交叉馈送"
            AppLanguage.Japanese -> "クロスフィード"
            else -> "Crossfeed"
        }
        SpaciousnessMode.EarlyReflectionRoom -> when (language) {
            AppLanguage.Albanian -> "Dhomë"
            AppLanguage.Polish -> "Pokój"
            AppLanguage.Hindi -> "कमरा"
            AppLanguage.Hungarian -> "Szoba"
            AppLanguage.German -> "Raum"
            AppLanguage.French -> "Pièce"
            AppLanguage.Spanish -> "Sala"
            AppLanguage.Italian -> "Stanza"
            AppLanguage.Latin -> "Camera"
            AppLanguage.Portuguese -> "Sala"
            AppLanguage.Dutch -> "Ruimte"
            AppLanguage.Swedish -> "Rum"
            AppLanguage.Norwegian -> "Rom"
            AppLanguage.Danish -> "Rum"
            AppLanguage.Czech -> "Místnost"
            AppLanguage.Croatian -> "Soba"
            AppLanguage.Lithuanian -> "Kambarys"
            AppLanguage.Latvian -> "Istaba"
            AppLanguage.Estonian -> "Tuba"
            AppLanguage.Greek -> "Δωμάτιο"
            AppLanguage.Macedonian -> "Соба"
            AppLanguage.Russian -> "Комната"
            AppLanguage.Serbian -> "Соба"
            AppLanguage.Thai -> "ห้อง"
            AppLanguage.Ukrainian -> "Кімната"
            AppLanguage.ChineseSimplified -> "房间"
            AppLanguage.Japanese -> "ルーム"
            else -> "Room"
        }
        SpaciousnessMode.Philharmony -> when (language) {
            AppLanguage.Albanian -> "Filarmonia"
            AppLanguage.Polish -> "Filharmonia"
            AppLanguage.Hindi -> "फिलहार्मनी"
            AppLanguage.Hungarian -> "Filharmónia"
            AppLanguage.German -> "Philharmonie"
            AppLanguage.French -> "Philharmonie"
            AppLanguage.Spanish -> "Filarmónica"
            AppLanguage.Italian -> "Filarmonica"
            AppLanguage.Latin -> "Philharmonia"
            AppLanguage.Portuguese -> "Filarmônica"
            AppLanguage.Dutch -> "Filharmonie"
            AppLanguage.Swedish -> "Filharmoni"
            AppLanguage.Norwegian -> "Filharmoni"
            AppLanguage.Danish -> "Filharmoni"
            AppLanguage.Czech -> "Filharmonie"
            AppLanguage.Croatian -> "Filharmonija"
            AppLanguage.Lithuanian -> "Filharmonija"
            AppLanguage.Latvian -> "Filharmonija"
            AppLanguage.Estonian -> "Filharmoonia"
            AppLanguage.Greek -> "Φιλαρμονική"
            AppLanguage.Macedonian -> "Филхармонија"
            AppLanguage.Russian -> "Филармония"
            AppLanguage.Serbian -> "Филхармонија"
            AppLanguage.Thai -> "ฟิลฮาร์โมนี"
            AppLanguage.Ukrainian -> "Філармонія"
            AppLanguage.ChineseSimplified -> "爱乐厅"
            AppLanguage.Japanese -> "フィルハーモニー"
            else -> "Philharmony"
        }
        SpaciousnessMode.HaasSpace -> when (language) {
            AppLanguage.Albanian -> "Hapësira Haas"
            AppLanguage.Polish -> "Przestrzeń Haasa"
            AppLanguage.Hindi -> "हास स्पेस"
            AppLanguage.Hungarian -> "Haas tér"
            AppLanguage.German -> "Haas-Raum"
            AppLanguage.French -> "Espace Haas"
            AppLanguage.Spanish -> "Espacio Haas"
            AppLanguage.Italian -> "Spazio Haas"
            AppLanguage.Latin -> "Spatium Haas"
            AppLanguage.Portuguese -> "Espaço Haas"
            AppLanguage.Dutch -> "Haas-ruimte"
            AppLanguage.Swedish -> "Haas-rymd"
            AppLanguage.Norwegian -> "Haas-rom"
            AppLanguage.Danish -> "Haas-rum"
            AppLanguage.Czech -> "Haasův prostor"
            AppLanguage.Croatian -> "Haas prostor"
            AppLanguage.Lithuanian -> "Haas erdvė"
            AppLanguage.Latvian -> "Haas telpa"
            AppLanguage.Estonian -> "Haas ruum"
            AppLanguage.Greek -> "Χώρος Haas"
            AppLanguage.Macedonian -> "Haas простор"
            AppLanguage.Russian -> "Пространство Хааса"
            AppLanguage.Serbian -> "Haas простор"
            AppLanguage.Thai -> "พื้นที่ Haas"
            AppLanguage.Ukrainian -> "Простір Хааса"
            AppLanguage.ChineseSimplified -> "Haas 空间"
            AppLanguage.Japanese -> "ハース空間"
            else -> "Haas Space"
        }
        SpaciousnessMode.HarmonicAir -> when (language) {
            AppLanguage.Albanian -> "Ajër harmonik"
            AppLanguage.Polish -> "Harmoniczne powietrze"
            AppLanguage.Hindi -> "हार्मोनिक एयर"
            AppLanguage.Hungarian -> "Harmonikus levegő"
            AppLanguage.German -> "Harmonische Luft"
            AppLanguage.French -> "Air harmonique"
            AppLanguage.Spanish -> "Aire armónico"
            AppLanguage.Italian -> "Aria armonica"
            AppLanguage.Latin -> "Aer harmonicus"
            AppLanguage.Portuguese -> "Ar harmônico"
            AppLanguage.Dutch -> "Harmonische lucht"
            AppLanguage.Swedish -> "Harmonisk luft"
            AppLanguage.Norwegian -> "Harmonisk luft"
            AppLanguage.Danish -> "Harmonisk luft"
            AppLanguage.Czech -> "Harmonický vzduch"
            AppLanguage.Croatian -> "Harmonični zrak"
            AppLanguage.Lithuanian -> "Harmoningas oras"
            AppLanguage.Latvian -> "Harmonisks gaiss"
            AppLanguage.Estonian -> "Harmooniline õhk"
            AppLanguage.Greek -> "Αρμονικός αέρας"
            AppLanguage.Macedonian -> "Хармоничен воздух"
            AppLanguage.Russian -> "Гармонический воздух"
            AppLanguage.Serbian -> "Хармонични ваздух"
            AppLanguage.Thai -> "อากาศฮาร์มอนิก"
            AppLanguage.Ukrainian -> "Гармонійне повітря"
            AppLanguage.ChineseSimplified -> "和声音场"
            AppLanguage.Japanese -> "ハーモニックエア"
            else -> "Harmonic Air"
        }
    }
}

@Composable
private fun EqPresetPill(
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    useSubtleIdleBackground: Boolean = false,
    onClick: () -> Unit,
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else if (useSubtleIdleBackground) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier.elovairePressBounce(
            interactionSource = interactionSource,
            label = "${label}_eq_preset_scale",
        ),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .clip(RoundedCornerShape(ElovaireRadii.pill))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
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
            animationSpec = tween(120, easing = FastOutSlowInEasing),
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
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .pointerInput(bandFractions) {
                detectTapGestures { offset ->
                    if (size.width == 0 || size.height == 0) return@detectTapGestures
                    val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                    val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                    val bandIndex = nearestEqBandIndex(
                        fraction = ((offset.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
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
                        val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                        val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                        val bandIndex = nearestEqBandIndex(
                            fraction = ((offset.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
                            bandFractions = bandFractions,
                        )
                        val normalized = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onBandChanged(bandIndex, ((normalized * 2f) - 1f).coerceIn(-1f, 1f))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                        val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                        val index = nearestEqBandIndex(
                            fraction = ((change.position.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
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
            val horizontalPadding = EQ_GRAPH_EDGE_PADDING.toPx()
            val graphWidth = (size.width - horizontalPadding * 2f).coerceAtLeast(1f)
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
                val x = horizontalPadding + graphWidth * bandFractions.getOrElse(index) { 0f }
                val y = topPadding + (graphHeight * (1f - EqualizerDspModel.bandGraphFraction(band, eqGraphConfig)))
                val trackWidth = 5.dp.toPx()
                val activeWidth = 3.dp.toPx()
                val thumbWidth = 9.dp.toPx()
                val thumbHeight = 24.dp.toPx()
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
                    strokeWidth = activeWidth * 2.1f,
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
            animationSpec = tween(160, easing = FastOutSlowInEasing),
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
    val bandFractions = remember { eqBandFractions() }
    BoxWithConstraints(
        modifier = modifier
            .width(contentWidth)
            .height(18.dp),
    ) {
        val labelWidth = 36.dp
        val graphWidth = maxWidth - (EQ_GRAPH_EDGE_PADDING * 2)
        labels.forEachIndexed { index, label ->
            val fraction = bandFractions.getOrElse(index) { 0f }
            Box(
                modifier = Modifier
                    .width(labelWidth)
                    .align(Alignment.CenterStart)
                    .offset(x = EQ_GRAPH_EDGE_PADDING + graphWidth * fraction - (labelWidth / 2)),
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
            if (maxScrollPx > 0f && viewportWidthPx > 0f) {
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
        val trackStartPx = knobSizePx / 2f
        val trackWidthPx = (maxWidthPx - knobSizePx).coerceAtLeast(1f)
        val trackStart = with(density) { trackStartPx.toDp() }
        val trackWidth = with(density) { trackWidthPx.toDp() }
        val activeWidth by animateDpAsState(
            targetValue = trackWidth * fraction,
            animationSpec = tween(durationMillis = 70),
            label = "eq_macro_slider_fill",
        )
        val knobOffset by animateDpAsState(
            targetValue = with(density) { (trackStartPx + trackWidthPx * fraction - knobSizePx / 2f).toDp() },
            animationSpec = tween(durationMillis = 70),
            label = "eq_macro_slider_knob",
        )

        val updateFromX: (Float) -> Unit = { xPosition ->
            if (maxWidthPx > 0f) {
                val normalized = ((xPosition - trackStartPx) / trackWidthPx).coerceIn(0f, 1f)
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
                    .offset(x = trackStart)
                    .width(trackWidth)
                    .height(lineThickness)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(inactiveLineColor),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = trackStart)
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
    val rawLabel = when {
        kiloValue >= 10f || kiloValue % 1f == 0f -> kiloValue.roundToInt().toString()
        (kiloValue * 10f) % 1f == 0f -> String.format(java.util.Locale.ROOT, "%.1f", kiloValue)
        else -> String.format(java.util.Locale.ROOT, "%.2f", kiloValue)
    }
    val formatted = if ('.' in rawLabel) rawLabel.trimEnd('0').trimEnd('.') else rawLabel
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

private fun formatPlaylistDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "0 min."
    val totalMinutes = durationMs / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) {
        if (minutes > 0L) {
            "${hours}h ${minutes}min."
        } else {
            "${hours}h"
        }
    } else {
        "${minutes.coerceAtLeast(1L)} min."
    }
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
