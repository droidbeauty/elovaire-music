package elovaire.music.droidbeauty.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.core.hasNotificationPostingPermission
import elovaire.music.droidbeauty.app.core.queryMediaStoreFilePath
import elovaire.music.droidbeauty.app.core.requiredAudioPermission
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRepository
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryDeleteRequest
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
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
import elovaire.music.droidbeauty.app.data.update.AppUpdateTransientStatus
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
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.interaction.CompactBarGestureActions
import elovaire.music.droidbeauty.app.ui.interaction.PlayerOverlayHost
import elovaire.music.droidbeauty.app.ui.interaction.compactBarGestures
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAlbumMotion
import elovaire.music.droidbeauty.app.ui.motion.elovaireListDropdownReveal
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.SyncElovaireMotionScale
import elovaire.music.droidbeauty.app.ui.motion.rememberSystemAnimationScale
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.SettingsLanguageCopy
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.availableReleasesLabel
import elovaire.music.droidbeauty.app.ui.i18n.formatCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.homeCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedAllSongsSource
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.playLabel
import elovaire.music.droidbeauty.app.ui.i18n.playingFromPrefix
import elovaire.music.droidbeauty.app.ui.i18n.queueTitle
import elovaire.music.droidbeauty.app.ui.i18n.repeatModeLabel
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.searchCopy
import elovaire.music.droidbeauty.app.ui.i18n.searchSortModeLabel
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.displayLabel
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorScreen
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorEvent
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.AboutCardButtonAccent
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.elovaireResolvedColorScheme
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val onGoToAlbum: (Song) -> Unit = {},
    val onDeleteFromLibrary: (Song) -> Unit = {},
    val deletePhrase: UiPhrase = UiPhrase.DeleteFromLibrary,
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

private data class TopBarActionSpec(
    @DrawableRes val iconResId: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
)

private fun String?.isAlbumDetailRoute(): Boolean = this == "$ALBUM_ROUTE/{albumId}"

private fun resolveForwardEnterTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
): EnterTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.targetUsesTileExpand -> ElovaireAlbumMotion.forwardEnter(expandOrigin.toTransformOrigin())
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelEnter(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetRoute.isAlbumDetailRoute() -> ElovaireAlbumMotion.forwardEnter(expandOrigin.toTransformOrigin())
    transition.targetUsesDetailTransition -> ElovaireMotion.detailForwardEnter()
    else -> ElovaireMotion.fullScreenForwardEnter()
}

private fun resolveForwardExitTransition(
    transition: NavHostTransitionResolution,
): ExitTransition = when {
    transition.targetRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.targetUsesTileExpand -> ElovaireAlbumMotion.forwardExit()
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelExit(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetRoute.isAlbumDetailRoute() -> ElovaireAlbumMotion.forwardExit()
    transition.targetUsesDetailTransition -> ElovaireMotion.detailForwardExit()
    else -> ElovaireMotion.fullScreenForwardExit()
}

private fun resolvePopEnterTransition(
    transition: NavHostTransitionResolution,
): EnterTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> EnterTransition.None
    transition.initialUsesTileExpand -> ElovaireAlbumMotion.backEnter()
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelEnter(
        forward = transition.topLevelTransition.isForward,
    )
    transition.targetRoute.isAlbumDetailRoute() -> ElovaireAlbumMotion.backEnter()
    transition.targetUsesDetailTransition -> ElovaireMotion.detailBackEnter()
    else -> ElovaireMotion.fullScreenBackEnter()
}

private fun resolvePopExitTransition(
    transition: NavHostTransitionResolution,
    expandOrigin: ExpandOrigin,
): ExitTransition = when {
    transition.initialRoute == PLAYER_ROUTE -> ExitTransition.None
    transition.initialUsesTileExpand -> ElovaireAlbumMotion.backExit(expandOrigin.toTransformOrigin())
    transition.topLevelTransition.isTopLevelTransition -> ElovaireMotion.topLevelExit(
        forward = transition.topLevelTransition.isForward,
    )
    transition.initialRoute.isAlbumDetailRoute() -> ElovaireAlbumMotion.backExit(expandOrigin.toTransformOrigin())
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
    return this.systemGestureExclusion()
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

private fun querySongFilePaths(
    context: Context,
    songs: List<Song>,
): Set<String> {
    val contentResolver = context.contentResolver
    return songs.asSequence()
        .mapNotNull { song -> contentResolver.queryMediaStoreFilePath(context, song.uri) }
        .toSet()
}

@SuppressLint("NewApi")
private fun createWriteRequestIntentSender(
    context: Context,
    uris: List<Uri>,
): IntentSenderRequest {
    return IntentSenderRequest.Builder(
        MediaStore.createWriteRequest(
            context.contentResolver,
            uris,
        ).intentSender,
    ).build()
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
    SideEffect {
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
    resetHomeScrollOnColdStart: Boolean = false,
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
    LaunchedEffect(appUpdateState.transientStatus) {
        if (appUpdateState.transientStatus != null) {
            delay(2_500L)
            container.appUpdateManager.clearTransientStatus()
        }
    }
    val albumCollectionLayoutMode = albumCollectionLayoutModeName.toAlbumLayoutMode()
    val changelogReleases = remember(context) { ChangelogRepository(context).loadReleases() }
    val rootScope = rememberCoroutineScope()
    val viewModelFactory = remember(container) { ElovaireViewModelFactory(container) }
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)
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
    var hasPermission by remember { mutableStateOf(context.hasAudioReadPermission()) }
    var hasNotificationPermission by remember { mutableStateOf(context.hasNotificationPostingPermission()) }
    var hasRequestedAudioPermission by rememberSaveable { mutableStateOf(false) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var firstLaunchPermissionExperienceActive by rememberSaveable {
        mutableStateOf(!hasPermission)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
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
        filePaths: Set<String>,
    ) {
        invalidateArtworkCaches(songs.flatMap { listOf(it.artUri, it.uri) })
        val deleteResult = container.libraryRepository.refreshAfterDelete(
            LibraryDeleteRequest(
                songIds = songs.mapTo(linkedSetOf(), Song::id),
                albumIds = songs.mapTo(linkedSetOf(), Song::albumId),
                uris = songs.mapTo(linkedSetOf(), Song::uri),
                filePaths = filePaths,
            ),
        )
        withContext(Dispatchers.IO) {
            cleanupEmptyDirectories(parentDirectories)
        }
        container.playbackManager.removeSongsFromQueue(deleteResult.deletedSongIds)
        deleteResult.deletedSongIds.forEach(container.preferenceStore::removeSongReferences)
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
                    filePaths = pendingDeletion.filePaths,
                )
            }
        }
    }

    LaunchedEffect(hasPermission, hasNotificationPermission) {
        container.libraryRepository.onPermissionChanged(hasPermission)
        if (!hasPermission && !hasRequestedAudioPermission) {
            hasRequestedAudioPermission = true
            permissionLauncher.launch(requiredAudioPermission())
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

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val refreshedAudioPermission = context.hasAudioReadPermission()
                val refreshedNotificationPermission = context.hasNotificationPostingPermission()
                if (hasPermission != refreshedAudioPermission) {
                    hasPermission = refreshedAudioPermission
                    container.libraryRepository.onPermissionChanged(refreshedAudioPermission)
                }
                if (hasNotificationPermission != refreshedNotificationPermission) {
                    hasNotificationPermission = refreshedNotificationPermission
                    container.setNotificationsEnabled(refreshedNotificationPermission)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            onRequestPermission = { permissionLauncher.launch(requiredAudioPermission()) },
        )
        return
    }

    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null

    val topLevelDestinations = DefaultTopLevelDestinations

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentConcreteRoute = currentBackStackEntry?.concreteNavigationRoute() ?: currentRoute
    var previousMotionRoute by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentConcreteRoute) {
        if (BuildConfig.DEBUG && previousMotionRoute != currentConcreteRoute) {
            Log.d("ElovaireMotion", "Route ${previousMotionRoute.orEmpty()} -> ${currentConcreteRoute.orEmpty()}")
        }
        previousMotionRoute = currentConcreteRoute
    }
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
    var playerLayerStateName by rememberSaveable { mutableStateOf(PlayerLayerState.Compact.name) }
    val playerLayerState = remember(playerLayerStateName) { PlayerLayerState.valueOf(playerLayerStateName) }
    val openNowPlayingMutex = remember { Mutex() }
    var isSearchQueryActive by rememberSaveable { mutableStateOf(false) }
    var browsingOriginRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var selectedBottomRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var lastHomeTabRoute by rememberSaveable { mutableStateOf(HOME_ROUTE) }
    var lastLibraryTabRoute by rememberSaveable { mutableStateOf(ALBUMS_ROUTE) }
    var lastPlaylistsTabRoute by rememberSaveable { mutableStateOf(PLAYLISTS_ROUTE) }
    var lastSearchTabRoute by rememberSaveable { mutableStateOf(SEARCH_ROUTE) }
    val routeOwnerOverrides = remember { mutableStateMapOf<String, String>() }
    var searchFieldFocused by rememberSaveable { mutableStateOf(false) }
    var homeScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var libraryScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var playlistsScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    var searchScrollRequestVersion by rememberSaveable { mutableLongStateOf(0L) }
    val openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit = { album, origin, source ->
        if (BuildConfig.DEBUG) {
            Log.d("ElovaireMotion", "Album transition ${source.name} -> AlbumDetail(${album.id})")
        }
        detailExpandOrigin = origin
        detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
        navController.navigate("$ALBUM_ROUTE/${album.id}")
    }
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
    val hideCompactNowPlayingRoutes = setOf(
        CHANGELOG_ROUTE,
        EQUALIZER_ROUTE,
        "$ALBUM_TAG_EDITOR_ROUTE/{albumId}",
    )
    val hideCompactNowPlaying = (keyboardVisible && currentRoute == PLAYLISTS_ROUTE) ||
        (currentRoute == SEARCH_ROUTE && isSearchQueryActive) ||
        currentRoute in hideCompactNowPlayingRoutes
    val reserveCompactNowPlayingSpace = playbackState.currentSong != null && !hideCompactNowPlaying
    val canHostCompactNowPlaying = playbackState.currentSong != null
    val showPlayerOverlay = playerLayerState == PlayerLayerState.Expanded && playbackNowPlayingState.currentSong != null
    val showGlobalNowPlaying = canHostCompactNowPlaying && !hideCompactNowPlaying && playerLayerState != PlayerLayerState.Expanded
    val reenteringFromPlayer = playerLayerState == PlayerLayerState.ReturningToCompact
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
    val requestOpenNowPlaying: (NowPlayingTransitionSnapshot?) -> Unit = { snapshot ->
        rootScope.launch {
            openNowPlayingMutex.withLock {
                if (container.playbackManager.state.value.currentSong == null || playerLayerState == PlayerLayerState.Expanded) {
                    return@withLock
                }
                playerLayerStateName = PlayerLayerState.Expanded.name
                nowPlayingTransitionSnapshot = snapshot
            }
        }
    }
    val hidePlayerOverlay: (Boolean) -> Unit = { returnToCompact ->
        playerLayerStateName = if (returnToCompact && container.playbackManager.state.value.currentSong != null) {
            PlayerLayerState.ReturningToCompact.name
        } else {
            PlayerLayerState.Compact.name
        }
    }
    val currentRequestOpenNowPlaying by rememberUpdatedState(requestOpenNowPlaying)
    val openCurrentPlayingAlbum: (Long) -> Unit = { albumId ->
        val sameAlbumAlreadyVisible =
            currentRoute == "$ALBUM_ROUTE/{albumId}" && currentAlbumRouteId == albumId
        hidePlayerOverlay(false)
        if (!sameAlbumAlreadyVisible) {
            albumsById[albumId]?.let { album ->
                openAlbum(album, ExpandOrigin(), AlbumOpenSource.Player)
            } ?: run {
                detailExpandOrigin = ExpandOrigin()
                detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                navController.navigate("$ALBUM_ROUTE/$albumId")
            }
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
    val showPlaylistCreateAction = currentRoute == PLAYLISTS_ROUTE
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
        container.openNowPlayingCommands.collect {
            currentRequestOpenNowPlaying(null)
        }
    }
    LaunchedEffect(playbackNowPlayingState.currentSong?.id) {
        if (playbackNowPlayingState.currentSong == null) {
            playerLayerStateName = PlayerLayerState.Compact.name
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
        val usesLightSystemBarIcons = if (showPlayerOverlay) {
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
                    val filePaths = querySongFilePaths(context, uniqueSongs)
                    val parentDirectories = filePaths.mapNotNullTo(linkedSetOf()) { path ->
                        File(path).parentFile?.absolutePath
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pendingSongDeletion = PendingSongDeletion(
                            songs = uniqueSongs,
                            parentDirectories = parentDirectories,
                            filePaths = filePaths,
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
                                filePaths = filePaths,
                            )
                        }.onFailure { throwable ->
                            val intentSender = when {
                                throwable is RecoverableSecurityException -> {
                                    throwable.userAction.actionIntent.intentSender
                                }
                                else -> null
                            }
                            if (intentSender != null) {
                                pendingSongDeletion = PendingSongDeletion(
                                    songs = uniqueSongs,
                                    parentDirectories = parentDirectories,
                                    filePaths = filePaths,
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
            onGoToAlbum = { song ->
                val album = albumsById[song.albumId]
                if (album != null) {
                    openAlbum(album, ExpandOrigin(), AlbumOpenSource.LibraryAlbums)
                } else if (song.albumId > 0L) {
                    navController.navigate("$ALBUM_ROUTE/${song.albumId}")
                }
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
                            resetScrollOnColdStart = resetHomeScrollOnColdStart,
                            playInitialReveal = playFirstLaunchHomeReveal,
                            onInitialRevealFinished = {
                                playFirstLaunchHomeReveal = false
                                firstLaunchPermissionExperienceActive = false
                            },
                            onAlbumSelected = { album, origin ->
                                openAlbum(album, origin, AlbumOpenSource.HomeSection)
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
                                    requestOpenNowPlaying(null)
                                }
                            },
                            onShufflePlaylist = { playlist, songs ->
                                val shuffledSongs = songs.shuffled()
                                shuffledSongs.firstOrNull()?.let { firstSong ->
                                    container.playbackManager.playSong(
                                        song = firstSong,
                                        collection = shuffledSongs,
                                        sourceLabel = playlist.name,
                                        sourcePlaylistId = playlist.id,
                                    )
                                    requestOpenNowPlaying(null)
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
                                requestOpenNowPlaying(null)
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
                                openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
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
                        SearchRoute(
                            viewModel = searchViewModel,
                            libraryState = libraryState,
                            favoriteSongIds = favoriteSongIdSet,
                            topPadding = topContentPadding,
                            bottomPadding = bottomContentPadding,
                            scrollToTopRequestVersion = searchScrollRequestVersion,
                            isSearchFieldFocused = searchFieldFocused,
                            onSearchFieldFocusedChange = { searchFieldFocused = it },
                            onSearchQueryActiveChanged = { isSearchQueryActive = it },
                            onPlaySong = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = searchViewModel.playbackSourceLabelFor(queue, song.album),
                                )
                                requestOpenNowPlaying(null)
                            },
                            onAlbumSelected = { album, origin ->
                                openAlbum(album, origin, AlbumOpenSource.SearchResults)
                            },
                            onArtistSelected = { artistName ->
                                navController.navigate("$ARTIST_ROUTE/${Uri.encode(artistName)}")
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
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
                                    requestOpenNowPlaying(null)
                                }
                            },
                            onShufflePlaylist = { songs, sourceLabel ->
                                val shuffledSongs = songs.shuffled()
                                shuffledSongs.firstOrNull()?.let { firstSong ->
                                    container.playbackManager.playSong(
                                        song = firstSong,
                                        collection = shuffledSongs,
                                        sourceLabel = sourceLabel,
                                        sourcePlaylistId = playlist?.id,
                                    )
                                    requestOpenNowPlaying(null)
                                }
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = playlist?.name ?: queue.playbackSourceLabel(
                                        fallbackAlbum = song.album,
                                        language = appLanguage,
                                    ),
                                    sourcePlaylistId = playlist?.id,
                                )
                                requestOpenNowPlaying(null)
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
                        var routedAlbumSongIds by remember(albumId) { mutableStateOf<Set<Long>>(emptySet()) }
                        val album = libraryState.albums.firstOrNull { it.id == albumId }
                            ?: libraryState.albums.firstOrNull { candidate ->
                                routedAlbumSongIds.isNotEmpty() && candidate.songs.any { it.id in routedAlbumSongIds }
                            }
                        LaunchedEffect(album?.id) {
                            album?.songs?.mapTo(linkedSetOf(), Song::id)?.let { routedAlbumSongIds = it }
                        }
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        AlbumScreen(
                            album = album,
                            removingSongIds = libraryState.removingSongIds,
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
                                requestOpenNowPlaying(null)
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
                        val tagEditorViewModel: AlbumTagEditorViewModel = viewModel(
                            viewModelStoreOwner = backStackEntry,
                            key = "album_tag_editor_$albumId",
                            factory = viewModelFactory,
                        )
                        val tagEditorState by tagEditorViewModel.uiState.collectAsStateWithLifecycle()
                        var pendingWriteRequest by remember(albumId) {
                            mutableStateOf<elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest?>(null)
                        }

                        val albumTagWriteLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                        ) { result ->
                            val pendingRequest = pendingWriteRequest ?: return@rememberLauncherForActivityResult
                            pendingWriteRequest = null
                            tagEditorViewModel.onWritePermissionResult(
                                granted = result.resultCode == Activity.RESULT_OK,
                                request = pendingRequest,
                            )
                        }

                        val coverArtPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument(),
                        ) { uri ->
                            tagEditorViewModel.onPickedCoverArt(uri)
                        }

                        LaunchedEffect(albumId) {
                            tagEditorViewModel.loadAlbum(albumId)
                        }

                        LaunchedEffect(tagEditorViewModel) {
                            tagEditorViewModel.events.collect { event ->
                                when (event) {
                                    is AlbumTagEditorEvent.RequestWritePermission -> {
                                        pendingWriteRequest = event.request
                                        if (event.uris.isNotEmpty()) {
                                            albumTagWriteLauncher.launch(
                                                createWriteRequestIntentSender(
                                                    context = context,
                                                    uris = event.uris,
                                                ),
                                            )
                                        } else {
                                            tagEditorViewModel.onWritePermissionResult(
                                                granted = true,
                                                request = event.request,
                                            )
                                        }
                                    }

                                    is AlbumTagEditorEvent.RequestRecoverableWritePermission -> {
                                        pendingWriteRequest = event.request
                                        albumTagWriteLauncher.launch(
                                            IntentSenderRequest.Builder(event.intentSender).build(),
                                        )
                                    }

                                    AlbumTagEditorEvent.SaveSucceeded -> {
                                        navController.navigateUp()
                                    }

                                    is AlbumTagEditorEvent.SavePartiallySucceeded -> Unit
                                }
                            }
                        }

                        AlbumTagEditorScreen(
                            state = tagEditorState,
                            appLanguage = appLanguage,
                            onBack = navController::navigateUp,
                            onSave = tagEditorViewModel::requestSave,
                            onAutoMatch = tagEditorViewModel::matchOnline,
                            onPickCoverArt = {
                                coverArtPickerLauncher.launch(arrayOf("image/*"))
                            },
                            onAlbumTitleChange = tagEditorViewModel::onAlbumTitleChange,
                            onAlbumArtistChange = tagEditorViewModel::onAlbumArtistChange,
                            onReleaseYearChange = tagEditorViewModel::onReleaseYearChange,
                            onTrackTitleChange = tagEditorViewModel::onTrackTitleChange,
                            onTrackArtistChange = tagEditorViewModel::onTrackArtistChange,
                            onTrackNumberChange = tagEditorViewModel::onTrackNumberChange,
                            onDiscNumberChange = tagEditorViewModel::onDiscNumberChange,
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
                                openAlbum(album, origin, AlbumOpenSource.LibraryAlbums)
                            },
                            onAddAlbumToQueue = { album ->
                                album.songs.forEach(container.playbackManager::enqueueSong)
                            },
                            onSongSelected = { song, queue ->
                                container.playbackManager.playSong(
                                    song = song,
                                    collection = queue,
                                    sourceLabel = if (kind == LibraryCollectionKind.Songs) {
                                        localizedAllSongsSource(appLanguage)
                                    } else {
                                        queue.playbackSourceLabel(
                                            fallbackAlbum = song.album,
                                            language = appLanguage,
                                        )
                                    },
                                )
                                requestOpenNowPlaying(null)
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
                                openAlbum(album, origin, AlbumOpenSource.GenreDetail)
                            },
                            onAddAlbumToQueue = { album ->
                                album.songs.forEach(container.playbackManager::enqueueSong)
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
                                requestOpenNowPlaying(null)
                            },
                            onAlbumSelected = { album, origin ->
                                openAlbum(album, origin, AlbumOpenSource.ArtistDetail)
                            },
                            onToggleFavorite = container.preferenceStore::toggleFavoriteSong,
                        )
                    }

                    composable(EQUALIZER_ROUTE) {
                        val equalizerViewModel: EqualizerViewModel = viewModel(factory = viewModelFactory)
                        val equalizerUiState by equalizerViewModel.uiState.collectAsStateWithLifecycle()
                        EqualizerScreen(
                            settings = equalizerUiState.toEqSettings(),
                            onBack = navController::navigateUp,
                            onBandChanged = equalizerViewModel::updateBand,
                            onBassChanged = equalizerViewModel::updateBass,
                            onMidrangeChanged = equalizerViewModel::updateMidrange,
                            onTrebleChanged = equalizerViewModel::updateTreble,
                            onSpaciousnessChanged = equalizerViewModel::updateSpaciousness,
                            onSpaciousnessModeChanged = equalizerViewModel::updateSpaciousnessMode,
                            onReverbDurationChanged = equalizerViewModel::updateReverbDuration,
                            onReverbProfileChanged = equalizerViewModel::updateReverbProfile,
                            onResetReverb = equalizerViewModel::resetReverb,
                            onApplyPreset = equalizerViewModel::applyPreset,
                            onReset = equalizerViewModel::resetEffects,
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
                            onMidrangeChanged = container.preferenceStore::updateMidrange,
                            onTrebleChanged = container.preferenceStore::updateTreble,
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
                    TopBarContextMenuOverlay(
                        expanded = showTopBarMenu,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f),
                        onDismiss = { showTopBarMenu = false },
                        onOpenSettings = openSettingsFromMenu,
                        onOpenEqualizer = openEqualizerFromMenu,
                        onOpenChangelog = openChangelogSheetFromMenu,
                        onOpenAbout = openAboutFromMenu,
                    )
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
                            (appUpdateState.availableRelease != null || appUpdateState.transientStatus != null),
                        enter = ElovaireMotion.bannerEnter(),
                        exit = ElovaireMotion.bannerExit(),
                        label = "UpdateBannerVisibility",
                    ) {
                        when {
                            appUpdateState.availableRelease != null -> {
                                UpdateAvailableBanner(
                                    release = requireNotNull(appUpdateState.availableRelease),
                                    uiState = appUpdateState,
                                    onDismiss = container.appUpdateManager::dismissAvailableUpdate,
                                    onUpdate = container.appUpdateManager::startUpdate,
                                )
                            }
                            appUpdateState.transientStatus == AppUpdateTransientStatus.UpToDate -> {
                                UpdateStatusBanner(
                                    text = rootUiCopy(LocalAppLanguage.current).appUpToDate,
                                    iconResId = R.drawable.ic_lucide_check,
                                )
                            }
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
                            onRequestPermission = { permissionLauncher.launch(requiredAudioPermission()) },
                        )
                    }
                }
                if (canHostCompactNowPlaying) {
                    playbackNowPlayingState.currentSong?.let {
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
                                viewModel = nowPlayingViewModel,
                                song = it,
                                transportShowsPause = playbackTransportState.transportShowsPause,
                                visible = showGlobalNowPlaying,
                                suppressEnterAnimation = reenteringFromPlayer,
                                onOpenPlayer = requestOpenNowPlaying,
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
            PlayerOverlayHost(
                visible = showPlayerOverlay,
                onExitFinished = {
                    nowPlayingTransitionSnapshot = null
                    if (playerLayerState == PlayerLayerState.ReturningToCompact) {
                        playerLayerStateName = PlayerLayerState.Compact.name
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .zIndex(20f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .consumePointersWithoutSemantics(),
                    )
                    NowPlayingRoute(
                        viewModel = nowPlayingViewModel,
                        playbackManager = container.playbackManager,
                        enrichedSongsById = songsById,
                        isFavorite = playbackNowPlayingState.currentSong?.id in favoriteSongIdSet,
                        playlists = playlists.filterNot { it.isSystem },
                        onBack = { hidePlayerOverlay(true) },
                        onOpenCurrentAlbum = openCurrentPlayingAlbum,
                        onToggleFavorite = { songId -> container.preferenceStore.toggleFavoriteSong(songId) },
                        onAddCurrentSongToPlaylist = { playlistId, song ->
                            container.preferenceStore.addSongsToPlaylist(playlistId, listOf(song.id))
                        },
                        onCreatePlaylist = container.preferenceStore::createPlaylist,
                        onOpenEqualizer = {
                            hidePlayerOverlay(false)
                            navController.navigate(EQUALIZER_ROUTE)
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
private fun ForceDarkColorScheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = elovaireResolvedColorScheme(darkTheme = true),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

@Composable
private fun NowPlayingRoute(
    viewModel: NowPlayingViewModel,
    playbackManager: PlaybackManager,
    enrichedSongsById: Map<Long, Song>,
    isFavorite: Boolean,
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onOpenCurrentAlbum: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddCurrentSongToPlaylist: (Long, Song) -> Unit,
    onCreatePlaylist: (String) -> Long,
    onOpenEqualizer: () -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val playerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()
    val lyricsEditorUiState by viewModel.lyricsEditorUiState.collectAsStateWithLifecycle()
    val activeLyricsLineIndex by viewModel.activeLyricsLineIndex.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.progressState().collectAsStateWithLifecycle()
    val lyricsWriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.onLyricsWritePermissionResult(result.resultCode == Activity.RESULT_OK)
    }
    LaunchedEffect(viewModel) {
        viewModel.lyricsEditorEvents.collect { event ->
            when (event) {
                is LyricsEditorEvent.RequestWritePermission -> {
                    lyricsWriteLauncher.launch(
                        IntentSenderRequest.Builder(event.request.intentSender).build(),
                    )
                }
            }
        }
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.setLyricsVisible(false)
        }
    }
    ForceDarkColorScheme {
        NowPlayingScreen(
            playbackManager = playbackManager,
            playerUiState = playerUiState,
            enrichedSongsById = enrichedSongsById,
            isFavorite = isFavorite,
            playlists = playlists,
            lyricsUiState = lyricsUiState,
            lyricsEditorUiState = lyricsEditorUiState,
            activeLyricsLineIndex = activeLyricsLineIndex,
            playbackProgress = playbackProgress,
            onLyricsVisibilityChanged = viewModel::setLyricsVisible,
            onSaveLyrics = viewModel::requestSaveLyrics,
            onClearLyricsEditorError = viewModel::clearLyricsEditorError,
            onBack = onBack,
            onOpenCurrentAlbum = onOpenCurrentAlbum,
            onTogglePlayback = viewModel::togglePlayback,
            onSkipPrevious = viewModel::skipPrevious,
            onSkipNext = viewModel::skipNext,
            onCycleRepeatMode = viewModel::cycleRepeatMode,
            onToggleShuffle = viewModel::toggleShuffle,
            onToggleFavorite = onToggleFavorite,
            onAddCurrentSongToPlaylist = onAddCurrentSongToPlaylist,
            onCreatePlaylist = onCreatePlaylist,
            onQueueItemSelected = viewModel::playQueueIndex,
            onQueueItemRemoved = viewModel::removeQueueIndex,
            onOpenEqualizer = onOpenEqualizer,
            onToggleGaplessPlayback = viewModel::toggleGaplessPlayback,
            onVolumeChanged = viewModel::setVolume,
            transitionSnapshot = transitionSnapshot,
            modifier = modifier,
        )
    }
}

@Composable
private fun CompactNowPlayingDockHost(
    viewModel: NowPlayingViewModel,
    song: Song,
    transportShowsPause: Boolean,
    visible: Boolean,
    suppressEnterAnimation: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var keepProgressActive by remember(song.id) { mutableStateOf(visible) }
    var lastProgress by remember(song.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(visible, song.id) {
        if (visible) {
            keepProgressActive = true
        } else {
            delay(ElovaireMotion.Standard.toLong().coerceAtLeast(120L))
            keepProgressActive = false
        }
    }

    if (keepProgressActive) {
        val playbackProgress by viewModel.progressState().collectAsStateWithLifecycle()
        LaunchedEffect(playbackProgress.displayPositionMs, playbackProgress.durationMs, song.id) {
            lastProgress = if (playbackProgress.durationMs > 0L) {
                (playbackProgress.displayPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    StandaloneNowPlayingDock(
        song = song,
        isPlaying = transportShowsPause,
        progress = lastProgress,
        visible = visible,
        suppressEnterAnimation = suppressEnterAnimation,
        onOpenPlayer = onOpenPlayer,
        onTogglePlayback = viewModel::togglePlayback,
        onSkipPrevious = viewModel::skipPrevious,
        onSkipNext = viewModel::skipNext,
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
    ForceDarkColorScheme {
        ElovaireAnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = if (suppressEnterAnimation) {
                ElovaireMotion.compactBarReturnEnter()
            } else {
                ElovaireMotion.compactBarEnter()
            },
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
}

@Composable
internal fun UnifiedTopBar(
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
                .consumePointersWithoutSemantics(),
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
                .consumePointersWithoutSemantics(),
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .consumePointersWithoutSemantics(),
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
    val interactionSource = rememberElovaireInteractionSource()
    val sharedBackPainter = LocalSharedBackIconPainter.current
    val sharedTopMenuPainter = LocalSharedTopMenuIconPainter.current
    val iconPainter = when {
        iconResId == R.drawable.ic_lucide_chevron_left && sharedBackPainter != null -> sharedBackPainter
        iconResId == R.drawable.ic_lucide_menu && sharedTopMenuPainter != null -> sharedTopMenuPainter
        else -> painterResource(id = iconResId)
    }
    Box(
        modifier = modifier
            .size(40.dp)
            .elovairePressScale(
                enabled = enabled,
                pressedScale = 0.88f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "${contentDescription}_header_scale",
            )
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
                .consumePointersWithoutSemantics()
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
                        suppressEnterAnimation = suppressEnterAnimation,
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
    suppressEnterAnimation: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
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
    val baseIconScale by selectionTransition.animateFloat(
        transitionSpec = {
            if (suppressEnterAnimation) {
                tween(durationMillis = 0)
            } else {
                ElovaireMotion.releaseSpringSpec<Float>(
                    dampingRatio = 0.8f,
                    stiffness = 540f,
                )
            }
        },
        label = "BottomNavItemBaseIconScale",
    ) { isSelected -> if (isSelected) 1.14f else 1f }

    Box(
        modifier = Modifier
            .size(56.dp)
            .elovairePressScale(
                pressedScale = 0.88f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "${contentDescription}_bottom_nav_scale",
            )
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
    val interactionSource = rememberElovaireInteractionSource()
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
                    .compactBarGestures(
                        enabled = visible,
                        actions = CompactBarGestureActions(
                            onTap = {
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
                                onOpenPlayer(validSnapshot)
                            },
                            onSwipePrevious = {
                                onSkipPrevious()
                            },
                            onSwipeNext = {
                                onSkipNext()
                            },
                            onDragDelta = { delta ->
                                dragOffsetX = (dragOffsetX + delta).coerceIn(-160f, 160f)
                            },
                            onGestureFinished = {
                                dragOffsetX = 0f
                            },
                        ),
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
                    .elovairePressScale(
                        enabled = visible,
                        pressedScale = 0.9f,
                        animationSpec = ElovaireMotion.chromeReleaseSpec(),
                        interactionSource = interactionSource,
                        label = "MiniPlayerPlayButtonScale",
                    )
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
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
                    text = copy.firstLaunchPermissionTitle,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(30f)),
                )
                Text(
                    text = copy.firstLaunchPermissionMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onRequestPermission) {
                    Text(copy.firstLaunchPermissionButton)
                }
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
    resetScrollOnColdStart: Boolean,
    playInitialReveal: Boolean,
    onInitialRevealFinished: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayPlaylist: (Playlist, List<Song>) -> Unit,
    onShufflePlaylist: (Playlist, List<Song>) -> Unit,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val listState = rememberElovaireLazyListState("home_screen")
    val language = LocalAppLanguage.current
    val homeCopy = remember(language) { homeCopy(language) }
    val motionDurationScale = rememberSystemAnimationScale()
    var revealModules by rememberSaveable(playInitialReveal) { mutableStateOf(!playInitialReveal) }
    LaunchedEffect(resetScrollOnColdStart) {
        if (resetScrollOnColdStart) {
            lazyListPositionCache["home_screen"] = 0 to 0
            listState.scrollToItem(0, 0)
            withFrameNanos { }
            listState.scrollToItem(0, 0)
        }
    }
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
                                    onShuffle = { onShufflePlaylist(lastPlayedPlaylist, lastPlayedPlaylistSongs) },
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
    ForceDarkColorScheme {
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
}

@Composable
private fun LastPlayedPlaylistModule(
    playlist: Playlist,
    songs: List<Song>,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForceDarkColorScheme {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                    AlbumHeaderActionButton(
                        iconResId = R.drawable.ic_lucide_shuffle,
                        contentDescription = "Shuffle playlist",
                        tint = playTint,
                        backgroundColor = playBackground,
                        onClick = onShuffle,
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.libraryRemovalAnimation(isRemoving: Boolean): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (isRemoving) 0f else 1f,
        animationSpec = tween(if (isRemoving) 180 else 120),
        label = "library_item_removal_alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (isRemoving) 0.96f else 1f,
        animationSpec = tween(if (isRemoving) 180 else 120),
        label = "library_item_removal_scale",
    )
    return graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun AlbumCollectionContent(
    albums: List<Album>,
    removingAlbumIds: Set<Long> = emptySet(),
    playlists: List<Playlist>,
    layoutMode: AlbumLayoutMode,
    sortMode: AlbumSortMode,
    topPadding: Dp,
    bottomPadding: Dp,
    title: String = rootUiCopy(AppLanguage.English).allAlbumsTitle,
    subtitle: String = rootUiCopy(AppLanguage.English).allAlbumsSubtitle,
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
    onSortModeChanged: (AlbumSortMode) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
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

                    itemsIndexed(sortedAlbums, key = { _, album -> album.id }) { index, album ->
                        AlbumGridCard(
                            album = album,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListDropdownReveal(
                                    key = album.id,
                                    index = index,
                                )
                                .libraryRemovalAnimation(album.id in removingAlbumIds),
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

            AlbumLayoutMode.DenseGrid -> {
                LazyVerticalGrid(
                    state = gridState,
                    overscrollEffect = null,
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(gridState),
                    contentPadding = PaddingValues(
                        start = 6.dp,
                        top = topPadding + selectionTopInset + 8.dp,
                        end = 6.dp,
                        bottom = bottomPadding + 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    item(span = { GridItemSpan(3) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
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

                    itemsIndexed(sortedAlbums, key = { _, album -> album.id }) { index, album ->
                        AlbumGridCard(
                            album = album,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListDropdownReveal(
                                    key = album.id,
                                    index = index,
                                )
                                .libraryRemovalAnimation(album.id in removingAlbumIds),
                            selectionMode = selectionModeActive,
                            selected = album.id in selectedAlbumIds,
                            showText = false,
                            artworkCornerRadius = 0.dp,
                            showArtworkGlow = false,
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
                        Box(
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListDropdownReveal(
                                    key = album.id,
                                    index = index,
                                )
                                .libraryRemovalAnimation(album.id in removingAlbumIds),
                        ) {
                            CompactAlbumRow(
                                album = album,
                                selectionMode = selectionModeActive,
                                selected = album.id in selectedAlbumIds,
                                isFavorite = album.songs.isNotEmpty() && album.songs.all { it.id in favoriteSongIds },
                                showFavoriteButton = true,
                                playlists = playlists,
                                playlistSongsById = playlistSongsById,
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
                                onAddToQueue = { onAddAlbumToQueue(album) },
                                onAddToPlaylist = { playlistId -> onAddAlbumToPlaylist(playlistId, album) },
                                onCreatePlaylist = onCreatePlaylist,
                                onDeleteAlbum = { onDeleteAlbumFromDevice(album) },
                                onLongPress = {
                                    showSortOptions = false
                                    selectedAlbumIds = selectedAlbumIds + album.id
                                },
                            )
                        }
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
            val copy = rootUiCopy(LocalAppLanguage.current)
            PlaylistNameDialog(
                title = copy.renamePlaylistTitle,
                confirmLabel = copy.save,
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
    onAddAlbumToQueue: (Album) -> Unit,
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
            removingSongIds = libraryState.removingSongIds,
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
                removingAlbumIds = libraryState.removingAlbumIds,
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
                            onAddAlbumToQueue = onAddAlbumToQueue,
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
    removingSongIds: Set<Long>,
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
                Box(
                    modifier = Modifier
                        .animateItem(
                            placementSpec = ElovaireMotion.listPlacementSpec(),
                        )
                        .elovaireListDropdownReveal(
                            key = song.id,
                            index = index,
                        )
                        .libraryRemovalAnimation(song.id in removingSongIds),
                ) {
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
    onAddAlbumToQueue: (Album) -> Unit,
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
            removingAlbumIds = libraryState.removingAlbumIds,
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
            onAddAlbumToQueue = onAddAlbumToQueue,
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
                            title = rootUiCopy(LocalAppLanguage.current).mostPlayedSongs,
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
                                title = commonUiCopy(LocalAppLanguage.current).albums,
                                subtitle = availableReleasesLabel(artistAlbums.size, LocalAppLanguage.current),
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
) {
    val language = LocalAppLanguage.current
    Column(
        modifier = modifier.fillMaxWidth(0.74f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = miscPhrase(language, MiscPhrase.NothingInHere),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = miscPhrase(language, MiscPhrase.TapPlusToCreatePlaylist),
            style = MaterialTheme.typography.bodyLarge,
            color = readableSecondaryTextColor(),
            textAlign = TextAlign.Center,
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
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
                            contentDescription = title.ifBlank { copy.playlistArtworkPlaceholder },
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
    title: String = rootUiCopy(AppLanguage.English).newPlaylist,
    confirmLabel: String = rootUiCopy(AppLanguage.English).save,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val canConfirm = name.isNotBlank()
    val displayTitle = if (title == rootUiCopy(AppLanguage.English).newPlaylist) uiPhrase(language, UiPhrase.NewPlaylist) else title
    val displayConfirmLabel = if (confirmLabel == copy.save || confirmLabel == uiPhrase(AppLanguage.English, UiPhrase.Create)) uiPhrase(language, UiPhrase.Create) else confirmLabel
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
    placeholder: String = rootUiCopy(AppLanguage.English).playlistNamePlaceholder,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val localizedPlaceholder = if (placeholder == rootUiCopy(AppLanguage.English).playlistNamePlaceholder) copy.playlistNamePlaceholder else placeholder
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
                        contentDescription = copy.clearPlaylistName,
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
                                        modifier = Modifier
                                            .animateItem(
                                                placementSpec = ElovaireMotion.listPlacementSpec(),
                                            )
                                            .elovaireListDropdownReveal(
                                                key = playlist.id,
                                                index = displayedPlaylists.indexOfFirst { it.id == playlist.id },
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
                                            modifier = Modifier
                                                .animateItem(
                                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                                )
                                                .elovaireListDropdownReveal(
                                                    key = "inline_playlist_creator",
                                                    index = displayedPlaylists.size,
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
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
                        text = copy.noSongsInPlaylistYet,
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
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
                        text = copy.save,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchRoute(
    viewModel: SearchViewModel,
    libraryState: LibraryUiState,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    isSearchFieldFocused: Boolean,
    onSearchFieldFocusedChange: (Boolean) -> Unit,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onArtistSelected: (String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        libraryState = libraryState,
        state = state,
        favoriteSongIds = favoriteSongIds,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        scrollToTopRequestVersion = scrollToTopRequestVersion,
        isSearchFieldFocused = isSearchFieldFocused,
        onQueryChange = viewModel::onQueryChange,
        onSearchFieldFocusedChange = onSearchFieldFocusedChange,
        onShowAllSongResultsChange = viewModel::onShowAllSongResultsChange,
        onSearchSongSortModeChange = viewModel::onSearchSongSortModeChange,
        onShowSearchSongSortOptionsChange = viewModel::onShowSearchSongSortOptionsChange,
        onSearchQueryActiveChanged = onSearchQueryActiveChanged,
        onSongSelected = { song, queue ->
            viewModel.rememberArtistSearch(song)
            onPlaySong(song, queue)
        },
        onAlbumSelected = { album, origin, rememberSearch ->
            if (rememberSearch) {
                viewModel.rememberAlbumSearch(album)
            }
            onAlbumSelected(album, origin)
        },
        onArtistSelected = onArtistSelected,
        onToggleFavorite = onToggleFavorite,
        onClearSearchHistory = viewModel::clearSearchHistory,
        onResetSearchUi = viewModel::resetSearchUi,
    )
}

@Composable
private fun SearchScreen(
    libraryState: LibraryUiState,
    state: SearchUiState,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    isSearchFieldFocused: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchFieldFocusedChange: (Boolean) -> Unit,
    onShowAllSongResultsChange: (Boolean) -> Unit,
    onSearchSongSortModeChange: (SearchSongSortMode) -> Unit,
    onShowSearchSongSortOptionsChange: (Boolean) -> Unit,
    onSearchQueryActiveChanged: (Boolean) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin, Boolean) -> Unit,
    onArtistSelected: (String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onClearSearchHistory: () -> Unit,
    onResetSearchUi: () -> Unit,
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
    val trimmedQuery = state.query.trim()
    val allSongsListState = rememberElovaireLazyListState("search_all_songs", trimmedQuery, state.searchSongSortMode)
    val isSearchUiActive = trimmedQuery.isNotBlank() || isSearchFieldFocused || state.showAllSongResults
    val collapseAllSongResults: () -> Unit = {
        onShowAllSongResultsChange(false)
        onShowSearchSongSortOptionsChange(false)
    }
    val resetSearchToMain: () -> Unit = {
        onResetSearchUi()
        onSearchFieldFocusedChange(false)
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }
    if (state.showAllSongResults && trimmedQuery.isNotBlank()) {
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
            state.showSearchSongSortOptions -> onShowSearchSongSortOptionsChange(false)
            state.showAllSongResults && trimmedQuery.isNotBlank() -> collapseAllSongResults()
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
    LaunchedEffect(scrollToTopRequestVersion, state.contentMode) {
        if (scrollToTopRequestVersion > 0L && state.contentMode == SearchContentMode.AllSongs) {
            allSongsListState.animateScrollToItem(0)
        }
    }
    val matchingArtists = remember(state.matchingArtists, language) {
        state.matchingArtists.map { artist ->
            SearchHistoryEntry(
                key = "artist:${artist.name.lowercase()}",
                kind = SearchHistoryKind.Artist,
                title = artist.name,
                subtitle = localizedCountLabel(artist.songCount, "song", language),
                artUri = artist.artUri,
                query = artist.name,
            )
        }
    }

    val searchBar: @Composable () -> Unit = {
        val searchBarContentColor = MaterialTheme.colorScheme.onSurface
        OutlinedTextField(
            value = state.query,
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
        if (state.contentMode == SearchContentMode.AllSongs) {
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
                    resultCount = state.allMatchingSongs.size,
                    selected = state.searchSongSortMode,
                    expanded = state.showSearchSongSortOptions,
                    onToggleExpanded = {
                        onShowSearchSongSortOptionsChange(!state.showSearchSongSortOptions)
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
                                items = state.allMatchingSongs,
                                key = { _, song -> song.id },
                            ) { index, song ->
                                Box(
                                    modifier = Modifier
                                        .animateItem(
                                            placementSpec = ElovaireMotion.listPlacementSpec(),
                                        )
                                        .elovaireListDropdownReveal(
                                            key = song.id,
                                            index = index,
                                        ),
                                ) {
                                    PlaylistSongRow(
                                        song = song,
                                        isFavorite = song.id in favoriteSongIds,
                                        isCurrentSong = song.id == state.currentSongId,
                                        isPlaybackActive = state.isPlaybackActive,
                                        onClick = {
                                            onSongSelected(song, state.allMatchingSongs)
                                        },
                                        onToggleFavorite = { onToggleFavorite(song.id) },
                                        showDivider = index != state.allMatchingSongs.lastIndex,
                                    )
                                }
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
                        targetState = state.contentMode,
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
                                    if (state.recentSearches.isNotEmpty()) {
                                        SearchHistorySectionHeader(
                                            showClearAction = true,
                                            onClearHistory = onClearSearchHistory,
                                        )
                                        SearchHistoryListCard(
                                            entries = state.recentSearches.take(6),
                                            onAlbumSelected = { albumId ->
                                                libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                                    onAlbumSelected(album, ExpandOrigin(), false)
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
                                    if (state.suggestedAlbums.isNotEmpty()) {
                                        FavoriteAlbumsModule(
                                            albums = state.suggestedAlbums,
                                            title = searchCopy(language).suggestedAlbumsTitle,
                                            subtitle = searchCopy(language).suggestedAlbumsSubtitle,
                                            iconResId = R.drawable.ic_lucide_eye,
                                            onAlbumSelected = { album, origin ->
                                                onAlbumSelected(album, origin, false)
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
                                                    onAlbumSelected(album, ExpandOrigin(), false)
                                                }
                                            },
                                            onArtistSelected = onArtistSelected,
                                        )
                                    }

                                    if (state.matchingAlbums.isNotEmpty()) {
                                        ModuleCard {
                                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                                SectionTitleRow(
                                                    title = commonUiCopy(language).albums,
                                                    subtitle = copy.matchingAlbums(state.matchingAlbums.size),
                                                    compact = true,
                                                )
                                                ArtistAlbumGallery(
                                                    albums = state.matchingAlbums,
                                                    onAlbumSelected = { album, origin ->
                                                        onAlbumSelected(album, origin, true)
                                                    },
                                                )
                                            }
                                        }
                                    }

                                    if (state.matchingSongs.isNotEmpty()) {
                                        SearchSongsPreviewHeader(
                                            resultCount = state.allMatchingSongs.size,
                                            showSeeAll = state.allMatchingSongs.size > state.matchingSongs.size,
                                            onShowAll = {
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                                onSearchFieldFocusedChange(false)
                                                onShowAllSongResultsChange(true)
                                            },
                                        )
                                        Column {
                                            state.matchingSongs.forEachIndexed { index, song ->
                                                Box(
                                                    modifier = Modifier.elovaireListDropdownReveal(
                                                        key = song.id,
                                                        index = index,
                                                    ),
                                                ) {
                                                    HomeRecentSongRow(
                                                        song = song,
                                                        isFavorite = song.id in favoriteSongIds,
                                                        onClick = {
                                                            onSongSelected(song, state.matchingSongs)
                                                        },
                                                        onToggleFavorite = { onToggleFavorite(song.id) },
                                                        showDivider = index != state.matchingSongs.lastIndex,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (state.matchingAlbums.isEmpty() && state.matchingSongs.isEmpty() && matchingArtists.isEmpty()) {
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
        ToggleIconChip(
            iconResId = R.drawable.ic_lucide_grid_3x3,
            selected = layoutMode == AlbumLayoutMode.DenseGrid,
            contentDescription = "Dense grid",
            onClick = { onLayoutModeChanged(AlbumLayoutMode.DenseGrid) },
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
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = if (pressed) {
            ElovaireMotion.pressDownSpec()
        } else {
            ElovaireMotion.bounceSpringSpec()
        },
        label = "toggle_chip_scale",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = if (pressed) {
            ElovaireMotion.pressDownSpec()
        } else {
            ElovaireMotion.releaseSpringSpec(
                dampingRatio = 0.78f,
                stiffness = 520f,
            )
        },
        label = "toggle_chip_icon_scale",
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
                modifier = Modifier
                    .size(15.dp)
                    .scale(iconScale),
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
    showText: Boolean = true,
    artworkCornerRadius: Dp = ElovaireRadii.artwork,
    showArtworkGlow: Boolean = true,
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
                cornerRadius = artworkCornerRadius,
                showArtworkGlow = showArtworkGlow,
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
        if (showText) {
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
}

@Composable
private fun CompactAlbumRow(
    album: Album,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    isFavorite: Boolean = false,
    showFavoriteButton: Boolean = false,
    playlists: List<Playlist> = emptyList(),
    playlistSongsById: Map<Long, Song> = emptyMap(),
    onOpen: (ExpandOrigin) -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onAddToPlaylist: ((Long) -> Unit)? = null,
    onCreatePlaylist: ((String) -> Long)? = null,
    onDeleteAlbum: (() -> Unit)? = null,
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
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
                    Row(
                        modifier = Modifier.padding(end = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InlineFavoriteSongButton(
                            isFavorite = isFavorite,
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = onToggleFavorite,
                        )
                        if (onAddToQueue != null && onAddToPlaylist != null && onDeleteAlbum != null) {
                            AlbumOverflowMenuButton(
                                album = album,
                                playlists = playlists,
                                playlistSongsById = playlistSongsById,
                                tint = MaterialTheme.colorScheme.onSurface,
                                onAddToQueue = onAddToQueue,
                                onAddToPlaylist = onAddToPlaylist,
                                onCreatePlaylist = onCreatePlaylist,
                                onDeleteAlbum = onDeleteAlbum,
                            )
                        }
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
    removingSongIds: Set<Long>,
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
            Text(rootUiCopy(LocalAppLanguage.current).albumNotFound)
        }
        return
    }

    val language = LocalAppLanguage.current
    var allowHeavyAlbumEffects by remember(album.id) { mutableStateOf(false) }
    LaunchedEffect(album.id) {
        withFrameNanos { }
        allowHeavyAlbumEffects = true
        if (BuildConfig.DEBUG) {
            Log.d("ElovaireMotion", "AlbumDetail(${album.id}) heavy effects enabled after first frame")
        }
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
    val copy = remember(language) { rootUiCopy(language) }
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
                                .blur(
                                    if (allowHeavyAlbumEffects) {
                                        if (isLightTheme) 32.dp else 40.dp
                                    } else {
                                        0.dp
                                    },
                                ),
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
                                backgroundColor = RoseAccent,
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
                    Box(
                        modifier = Modifier
                            .animateItem(
                                placementSpec = ElovaireMotion.listPlacementSpec(),
                            )
                            .elovaireListDropdownReveal(
                                key = song.id,
                                index = index,
                            )
                            .libraryRemovalAnimation(song.id in removingSongIds),
                    ) {
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
                    showGoToAlbum = false,
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
    onShufflePlaylist: (List<Song>, String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAddSongs: (List<Long>) -> Unit,
    onUpdateSongOrder: (List<Long>) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    if (playlist == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(rootUiCopy(LocalAppLanguage.current).playlistNotFound)
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumHeaderPlayButton(
                                tint = Color.White,
                                backgroundColor = RoseAccent,
                                onClick = { onPlayPlaylist(playlistSongs, playlist.name) },
                            )
                            AlbumHeaderActionButton(
                                iconResId = R.drawable.ic_lucide_shuffle,
                                contentDescription = "Shuffle playlist",
                                tint = Color.White,
                                backgroundColor = RoseAccent,
                                iconSize = 18.dp,
                                onClick = { onShufflePlaylist(playlistSongs, playlist.name) },
                            )
                        }
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
                        modifier = Modifier
                            .animateItem(
                                placementSpec = ElovaireMotion.listPlacementSpec(),
                            )
                            .elovaireListDropdownReveal(
                                key = "${song.id}_$index",
                                index = index,
                                enabled = !editMode,
                            ),
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
            title = rootUiCopy(LocalAppLanguage.current).renamePlaylistTitle,
            confirmLabel = rootUiCopy(LocalAppLanguage.current).save,
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
                    placeholder = { Text(rootUiCopy(LocalAppLanguage.current).searchLibrary) },
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
        title = { Text(rootUiCopy(LocalAppLanguage.current).addSongsTitle) },
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
    playerUiState: PlayerUiState,
    enrichedSongsById: Map<Long, Song>,
    isFavorite: Boolean,
    playlists: List<Playlist>,
    lyricsUiState: LyricsUiState,
    lyricsEditorUiState: LyricsEditorUiState,
    activeLyricsLineIndex: Int,
    playbackProgress: PlaybackProgressState,
    onLyricsVisibilityChanged: (Boolean) -> Unit,
    onSaveLyrics: (String) -> Unit,
    onClearLyricsEditorError: () -> Unit,
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
    onToggleGaplessPlayback: () -> Unit,
    onVolumeChanged: (Float) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val liveCurrentSong = playerUiState.currentSong
    val liveDisplaySong = liveCurrentSong?.let { enrichedSongsById[it.id] ?: it }
    val playerHazeState = rememberHazeState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var playerDismissTriggered by rememberSaveable { mutableStateOf(false) }
    var playerHasRenderedSong by rememberSaveable { mutableStateOf(liveCurrentSong != null) }
    LaunchedEffect(liveCurrentSong?.id) {
        if (liveCurrentSong == null) {
            if (playerHasRenderedSong && !playerDismissTriggered) {
                playerDismissTriggered = true
                onBack()
            }
        } else {
            playerHasRenderedSong = true
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
            durationMillis = 360,
            easing = androidx.compose.animation.core.Easing { fraction ->
                1f - LinearOutSlowInEasing.transform(1f - fraction)
            },
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
    val playingFromText = remember(language, playerUiState.sourceLabel, currentSong?.album) {
        val source = playerUiState.sourceLabel
            ?.takeIf { it.isNotBlank() }
            ?: currentSong?.album?.takeIf { it.isNotBlank() }
            ?: localizedAllSongsSource(language)
        "${playingFromPrefix(language)} $source"
    }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember(currentSong?.id) { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember(currentSong?.id) { mutableStateOf(false) }
    var queueStatusText by remember(currentSong?.id) { mutableStateOf<String?>(null) }
    var queueStatusVersion by remember(currentSong?.id) { mutableStateOf(0L) }
    LaunchedEffect(showLyricsSheet) {
        onLyricsVisibilityChanged(showLyricsSheet)
    }
    DisposableEffect(Unit) {
        onDispose { onLyricsVisibilityChanged(false) }
    }
    LaunchedEffect(queueStatusVersion) {
        if (queueStatusText != null) {
            delay(2000L)
            queueStatusText = null
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
            val transportShowsPause = remember(currentSong.id, playerUiState.transportShowsPause) {
                playerUiState.transportShowsPause
            }
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
                        if (dismissAnimationRunning) return@detectVerticalDragGestures
                        val continuingDismissDrag = dragDistance > 0f
                        if (dragAmount <= 0f && !continuingDismissDrag) return@detectVerticalDragGestures
                        change.consume()
                        dragDistance = (dragDistance + dragAmount).coerceAtLeast(0f)
                        if (dragDistance <= 0f) {
                            interactiveTransitionProgress = 1f
                            transitionState = PlayerOverlayTransitionState.Expanded
                            return@detectVerticalDragGestures
                        }
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
                                val animatedSong = playerUiState.queue.firstOrNull { it.id == songId } ?: currentSong
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
                            val animatedSong = playerUiState.queue.firstOrNull { it.id == songId } ?: currentSong
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
                val queueSheetTopExtension = 532.dp
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
                            iconResId = repeatModeIconRes(playerUiState.repeatMode),
                            label = "",
                            iconSize = 20.dp,
                            tint = contentColor,
                            showBackground = playerUiState.repeatMode != PlaybackRepeatMode.Off,
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
                            .consumePointersWithoutSemantics(),
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showQueueSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    enter = fadeIn(
                        animationSpec = ElovaireMotion.queueMenuEnterSpec(),
                        initialAlpha = 0.04f,
                    ) +
                        scaleIn(
                            initialScale = 0.94f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = ElovaireMotion.queueMenuEnterSpec(),
                        ) +
                        slideInHorizontally(
                            initialOffsetX = { it / 14 },
                            animationSpec = ElovaireMotion.queueMenuEnterSpec(),
                        ) +
                        slideInVertically(
                            initialOffsetY = { it / 9 },
                            animationSpec = ElovaireMotion.queueMenuEnterSpec(),
                        ),
                    exit = fadeOut(
                        animationSpec = ElovaireMotion.queueMenuExitSpec(),
                        targetAlpha = 0f,
                    ) +
                        scaleOut(
                            targetScale = 0.98f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = ElovaireMotion.queueMenuExitSpec(),
                        ) +
                        slideOutVertically(
                            targetOffsetY = { it / 12 },
                            animationSpec = ElovaireMotion.queueMenuExitSpec(),
                        ),
                ) {
                    QueueSheet(
                        queue = playerUiState.queue,
                        currentIndex = playerUiState.currentIndex,
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
                        shuffleEnabled = playerUiState.shuffleEnabled,
                        onToggleShuffle = {
                            queueStatusText = if (playerUiState.shuffleEnabled) {
                                "Shuffle | Disabled"
                            } else {
                                "Shuffle | Enabled"
                            }
                            queueStatusVersion += 1L
                            onToggleShuffle()
                        },
                        gaplessPlaybackEnabled = playerUiState.gaplessPlaybackEnabled,
                        onToggleGaplessPlayback = {
                            queueStatusText = if (playerUiState.gaplessPlaybackEnabled) {
                                "Gapless playback | Off"
                            } else {
                                "Gapless playback | On"
                            }
                            queueStatusVersion += 1L
                            onToggleGaplessPlayback()
                        },
                        onOpenEqualizer = onOpenEqualizer,
                        onAddSongToPlaylist = onAddCurrentSongToPlaylist,
                        onCreatePlaylist = onCreatePlaylist,
                        statusText = queueStatusText,
                        onDismiss = { showQueueSheet = false },
                        isPlaying = playerUiState.isPlaying,
                    )
                }
            }

            VolumeControlBar(
                volume = playerUiState.volume,
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
                lyricsUiState = lyricsUiState,
                lyricsEditorUiState = lyricsEditorUiState,
                activeLyricsLineIndex = activeLyricsLineIndex,
                playbackProgress = playbackProgress,
                tintColor = baseSurface.copy(alpha = 0.66f),
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onSeekTo = playbackManager::seekTo,
                onHideLyrics = { showLyricsSheet = false },
                onSaveLyrics = onSaveLyrics,
                onClearLyricsEditorError = onClearLyricsEditorError,
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
                    color = contentColor,
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
    gaplessPlaybackEnabled: Boolean,
    onToggleGaplessPlayback: () -> Unit,
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
    var playlistTargetSong by remember(currentSong?.id, queue) { mutableStateOf<Song?>(null) }
    val footerExpanded = statusText != null
    val footerHeight by animateDpAsState(
        targetValue = if (footerExpanded) 90.dp else 60.dp,
        animationSpec = ElovaireMotion.queueMenuEnterSpec(),
        label = "queue_footer_height",
    )
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
                        Box(
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListDropdownReveal(
                                    key = "${song.id}_$index",
                                    index = index,
                                ),
                        ) {
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
                        if (queueStatus != null) {
                            Text(
                                text = queueStatus,
                                style = MaterialTheme.typography.labelLarge,
                                color = tint.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerSecondaryActionButton(
                        iconResId = R.drawable.ic_lucide_separator_vertical,
                        label = "",
                        contentDescription = "Gapless playback",
                        iconSize = 20.dp,
                        tint = tint,
                        showBackground = gaplessPlaybackEnabled,
                        onClick = onToggleGaplessPlayback,
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
                .clip(RoundedCornerShape(8.dp))
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
    val interactionSource = rememberElovaireInteractionSource()
    val motionDurationScale = rememberSystemAnimationScale()
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
                .elovairePressScale(
                    pressedScale = 0.88f,
                    animationSpec = ElovaireMotion.softPressReturnSpec(),
                    interactionSource = interactionSource,
                    label = "queue_song_overflow_scale",
                )
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
            OverflowContextMenuPopup(
                expanded = expanded,
                onDismissRequest = { expanded = false },
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
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = Modifier
            .size(72.dp)
            .elovairePressScale(
                pressedScale = 0.9f,
                animationSpec = ElovaireMotion.softPressReturnSpec(),
                interactionSource = interactionSource,
                label = "${contentDescription}_transport_scale",
            )
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
    val interactionSource = rememberElovaireInteractionSource()
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (active) 0.2f else 0f,
        animationSpec = ElovaireMotion.contentFadeInSpec(),
        label = "queue_button_alpha",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .elovairePressScale(
                pressedScale = 0.9f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "queue_button_scale",
            )
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

private object OverflowContextMenuPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        val y = anchorBounds.top.coerceIn(0, maxY)
        return IntOffset(x, y)
    }
}

@Composable
private fun OverflowContextMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = OverflowContextMenuPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = ElovaireMotion.contextMenuEnter(),
            exit = ElovaireMotion.contextMenuExit(),
            label = "OverflowContextMenuVisibility",
        ) {
            content()
        }
    }
}

@Composable
private fun AlbumOverflowMenuButton(
    album: Album,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    tint: Color,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onCreatePlaylist: ((String) -> Long)?,
    onDeleteAlbum: () -> Unit,
) {
    val language = LocalAppLanguage.current
    var expanded by remember(album.id) { mutableStateOf(false) }
    var shouldRenderMenu by remember(album.id) { mutableStateOf(false) }
    var showPlaylistDialog by remember(album.id) { mutableStateOf(false) }
    val motionDurationScale = rememberSystemAnimationScale()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 380f,
        ),
        label = "album_overflow_scale",
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
                contentDescription = "Album options",
                tint = tint.copy(alpha = 0.82f),
                modifier = Modifier.size(OverflowMenuIconSize),
            )
        }

        if (shouldRenderMenu) {
            OverflowContextMenuPopup(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                FrostedContextMenuSurface(
                    modifier = Modifier.width(208.dp),
                ) {
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_plus,
                        text = uiPhrase(language, UiPhrase.AddToQueue),
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            expanded = false
                            onAddToQueue()
                        },
                    )
                    DividerLine()
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
                        iconResId = R.drawable.ic_lucide_trash_2,
                        text = uiPhrase(language, UiPhrase.DeleteAlbum),
                        tint = DestructiveRed,
                        containerColor = DestructiveRed.copy(alpha = 0.2f),
                        cornerRadius = (ElovaireRadii.card * 0.72f) - 2.dp,
                        bottomPadding = 10.dp,
                        onClick = {
                            expanded = false
                            onDeleteAlbum()
                        },
                    )
                }
            }
        }
    }

    if (showPlaylistDialog) {
        AddToPlaylistPickerDialog(
            playlists = playlists,
            playlistSongsById = playlistSongsById,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                onAddToPlaylist(playlistId)
                showPlaylistDialog = false
            },
            onCreatePlaylist = onCreatePlaylist,
        )
    }
}

@Composable
private fun SongOverflowMenuButton(
    song: Song,
    tint: Color,
    showGoToAlbum: Boolean = true,
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
            OverflowContextMenuPopup(
                expanded = expanded,
                onDismissRequest = { expanded = false },
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
                    if (showGoToAlbum) {
                        SongContextMenuItem(
                            iconResId = R.drawable.ic_lucide_disc_album,
                            text = uiPhrase(language, UiPhrase.GoToAlbum),
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                expanded = false
                                actions.onGoToAlbum(song)
                            },
                        )
                    }
                    SongContextMenuItem(
                        iconResId = R.drawable.ic_lucide_trash_2,
                        text = uiPhrase(language, actions.deletePhrase),
                        tint = DestructiveRed,
                        containerColor = DestructiveRed.copy(alpha = 0.2f),
                        cornerRadius = (ElovaireRadii.card * 0.72f) - 2.dp,
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
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val settingsCopy = remember(language) { settingsCopy(language) }
    BackHandler(enabled = expanded, onBack = onDismiss)
    Box(
        modifier = modifier,
    ) {
        ElovaireAnimatedVisibility(
            visible = expanded,
            modifier = Modifier.fillMaxSize(),
            enter = ElovaireMotion.overlayFadeEnter(initialAlpha = 0.86f),
            exit = ElovaireMotion.overlayFadeExit(),
            label = "TopBarContextMenuScrim",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        ElovaireAnimatedVisibility(
            visible = expanded,
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
    cornerRadius: Dp = ElovaireRadii.card * 0.72f,
    topPadding: Dp = 6.dp,
    bottomPadding: Dp = 6.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = topPadding, end = 10.dp, bottom = bottomPadding)
            .clip(RoundedCornerShape(cornerRadius))
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
    lyricsUiState: LyricsUiState,
    lyricsEditorUiState: LyricsEditorUiState,
    activeLyricsLineIndex: Int,
    playbackProgress: PlaybackProgressState,
    tintColor: Color,
    contentColor: Color,
    secondaryContentColor: Color,
    onSeekTo: (Long) -> Unit,
    onHideLyrics: () -> Unit,
    onSaveLyrics: (String) -> Unit,
    onClearLyricsEditorError: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var overlayEntered by remember(song?.id) { mutableStateOf(false) }
    val hideButtonArea = 112.dp
    val lyricsBottomBlurArea = 72.dp
    val bottomBlurSurfaceHeight = lyricsBottomBlurArea + navigationBarInsetDp()
    val lyricsHazeState = rememberHazeState()
    val listState = rememberLazyListState()
    var autoScrollHeld by remember(song?.id) { mutableStateOf(false) }
    var autoScrollResumeJob by remember(song?.id) { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var userLyricsScrollActive by remember(song?.id) { mutableStateOf(false) }
    var isEditingLyrics by remember(song?.id) { mutableStateOf(false) }
    var lyricsDraft by remember(song?.id) { mutableStateOf("") }
    var observedSaveRevision by remember(song?.id) {
        mutableLongStateOf(lyricsEditorUiState.savedRevision)
    }
    val backgroundReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = tween(
            durationMillis = ElovaireMotion.ScreenExpand,
            easing = FastOutSlowInEasing,
        ),
        label = "lyrics_background_reveal",
    )
    val headerReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = tween(
            durationMillis = ElovaireMotion.Standard,
            delayMillis = 30,
            easing = LinearOutSlowInEasing,
        ),
        label = "lyrics_header_reveal",
    )
    val dividerReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = tween(
            durationMillis = ElovaireMotion.Standard,
            delayMillis = 65,
            easing = LinearOutSlowInEasing,
        ),
        label = "lyrics_divider_reveal",
    )
    val contentReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = tween(
            durationMillis = ElovaireMotion.Screen,
            delayMillis = 95,
            easing = FastOutSlowInEasing,
        ),
        label = "lyrics_content_reveal",
    )
    val canSubmitLyricsEdit = !lyricsEditorUiState.isSaving &&
        (lyricsDraft.isNotBlank() || lyricsUiState is LyricsUiState.Ready)

    LaunchedEffect(song?.id) {
        overlayEntered = false
        withFrameNanos { }
        overlayEntered = true
    }
    BackHandler {
        if (isEditingLyrics) {
            isEditingLyrics = false
            onClearLyricsEditorError()
        } else {
            onHideLyrics()
        }
    }

    LaunchedEffect(lyricsUiState, song?.id, isEditingLyrics) {
        if (!isEditingLyrics) {
            lyricsDraft = (lyricsUiState as? LyricsUiState.Ready)
                ?.payload
                ?.lines
                ?.joinToString("\n") { it.text }
                .orEmpty()
        }
    }
    LaunchedEffect(lyricsEditorUiState.savedRevision) {
        if (lyricsEditorUiState.savedRevision > observedSaveRevision) {
            observedSaveRevision = lyricsEditorUiState.savedRevision
            isEditingLyrics = false
            focusManager.clearFocus(force = true)
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
                        tintColor.copy(alpha = 0.42f + (0.48f * backgroundReveal)),
                        tintColor.copy(alpha = 0.36f + (0.48f * backgroundReveal)),
                        tintColor.copy(alpha = 0.48f + (0.44f * backgroundReveal)),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(headerReveal)
                        .offset(y = ((1f - headerReveal) * (-18f)).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    song?.let {
                        Row(
                            modifier = Modifier.weight(1f),
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
                                modifier = Modifier.fillMaxWidth(0.75f),
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
                    ElovaireAnimatedVisibility(
                        visible = isEditingLyrics || lyricsUiState is LyricsUiState.Ready,
                        enter = ElovaireMotion.contextMenuEnter(),
                        exit = ElovaireMotion.contextMenuExit(),
                        label = "lyrics_edit_action_visibility",
                    ) {
                        LyricsEditorActionButton(
                            iconResId = if (isEditingLyrics) {
                                R.drawable.ic_lucide_check
                            } else {
                                R.drawable.ic_lucide_square_pen
                            },
                            contentDescription = if (isEditingLyrics) copy.save else "Edit lyrics",
                            tint = contentColor,
                            enabled = !isEditingLyrics || canSubmitLyricsEdit,
                            backgroundAlpha = 0f,
                            onClick = {
                                if (isEditingLyrics) {
                                    onSaveLyrics(lyricsDraft)
                                } else {
                                    lyricsDraft = (lyricsUiState as? LyricsUiState.Ready)
                                        ?.payload
                                        ?.lines
                                        ?.joinToString("\n") { line -> line.text }
                                        .orEmpty()
                                    onClearLyricsEditorError()
                                    isEditingLyrics = true
                                }
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .alpha(dividerReveal)
                        .offset(y = ((1f - dividerReveal) * (-12f)).dp),
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
                        .weight(1f)
                        .alpha(contentReveal)
                        .offset(y = ((1f - contentReveal) * (-10f)).dp),
                ) {
                    AnimatedContent(
                        targetState = isEditingLyrics,
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
                        contentKey = { it },
                        label = "lyrics_editor_state",
                    ) { editing ->
                        if (editing) {
                            LyricsTextEditor(
                                value = lyricsDraft,
                                onValueChange = {
                                    lyricsDraft = it
                                    onClearLyricsEditorError()
                                },
                                contentColor = contentColor,
                                errorMessage = lyricsEditorUiState.errorMessage,
                            )
                        } else when (val state = lyricsUiState) {
                            LyricsUiState.Hidden -> Unit
                            LyricsUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = copy.loadingLyrics,
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
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(24.dp),
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
                                                text = copy.noLyrics,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = contentColor,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                        LyricsEditorActionButton(
                                            iconResId = R.drawable.ic_lucide_plus,
                                            contentDescription = "Add lyrics",
                                            tint = contentColor,
                                            backgroundAlpha = 0.2f,
                                            onClick = {
                                                lyricsDraft = ""
                                                onClearLyricsEditorError()
                                                isEditingLyrics = true
                                            },
                                        )
                                    }
                                }
                            }

                            is LyricsUiState.Ready -> {
                                LyricsReadyContent(
                                    song = song,
                                    payload = state.payload,
                                    activeLyricLineIndex = activeLyricsLineIndex,
                                    playbackProgress = playbackProgress,
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
            ElovaireAnimatedVisibility(
                visible = !isEditingLyrics,
                enter = ElovaireMotion.standardEnter(),
                exit = ElovaireMotion.standardExit(),
                label = "hide_lyrics_action_visibility",
            ) {
                Box(modifier = Modifier.matchParentSize()) {
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
                                    contentDescription = copy.hideLyrics,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = copy.hideLyrics,
                                    style = MaterialTheme.typography.labelLarge,
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
private fun LyricsEditorActionButton(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    tint: Color,
    enabled: Boolean = true,
    backgroundAlpha: Float = 0f,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "lyrics_editor_action_scale",
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (backgroundAlpha > 0f) {
                    tint.copy(alpha = if (enabled) backgroundAlpha else backgroundAlpha * 0.4f)
                } else {
                    Color.Transparent
                },
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
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = tint.copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun LyricsTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    contentColor: Color,
    errorMessage: String?,
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        focusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = contentColor,
                fontWeight = FontWeight.Medium,
                lineHeight = elovaireScaledSp(30f),
            ),
            cursorBrush = SolidColor(contentColor),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusRequester)
                .verticalScroll(scrollState),
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun LyricsReadyContent(
    song: Song?,
    payload: LyricsPayload,
    activeLyricLineIndex: Int,
    playbackProgress: PlaybackProgressState,
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
    val resolvedActiveLyricLineIndex = remember(
        payload,
        activeLyricLineIndex,
        playbackProgress.displayPositionMs,
    ) {
        payload.currentLineIndexAt(
            positionMs = playbackProgress.displayPositionMs,
            timingOffsetMs = 0L,
            switchGraceMs = 0L,
        )?.takeIf { payload.isSynced && it >= 0 } ?: activeLyricLineIndex
    }
    val autoScrollCenterOffsetPx = with(LocalDensity.current) { 180.dp.roundToPx() }
    LaunchedEffect(resolvedActiveLyricLineIndex, payload.isSynced, autoScrollHeld) {
        if (!autoScrollHeld && payload.isSynced && resolvedActiveLyricLineIndex >= 0) {
            listState.animateLyricJumpToItem(
                index = resolvedActiveLyricLineIndex,
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
            val isActive = payload.isSynced && index == resolvedActiveLyricLineIndex
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
                    .pointerInput(song?.id, payload.lines.size, payload.isSynced, resolvedActiveLyricLineIndex) {
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
    contentDescription: String = label,
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
                contentDescription = contentDescription.ifBlank { null },
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

@DrawableRes
private fun repeatModeIconRes(repeatMode: PlaybackRepeatMode): Int {
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> R.drawable.ic_lucide_arrow_line_down
        PlaybackRepeatMode.One -> R.drawable.ic_lucide_repeat_1
        PlaybackRepeatMode.All -> R.drawable.ic_lucide_repeat
    }
}

private fun List<Song>.playbackSourceLabel(fallbackAlbum: String, language: AppLanguage): String {
    val distinctAlbums = asSequence().map { it.album }.filter { it.isNotBlank() }.distinct().toList()
    return when {
        distinctAlbums.size == 1 -> distinctAlbums.first()
        else -> localizedAllSongsSource(language)
    }.ifBlank { fallbackAlbum }
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
    onApplyPreset: (String, EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val listState = rememberElovaireLazyListState("equalizer_screen")
    val graphScrollState = rememberScrollState()
    val language = LocalAppLanguage.current
    val copy = remember(language) { settingsCopy(language) }
    val graphContentWidth = EQ_GRAPH_EDGE_PADDING * 2 +
        EQ_BAND_SPACING * (EqualizerDspModel.BAND_COUNT - 1).coerceAtLeast(0).toFloat()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    start = 18.dp,
                    top = topBarOccupiedHeight() + 8.dp,
                    end = 18.dp,
                    bottom = 16.dp,
                ),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EQ_DB_SCALE_GAP),
                    verticalAlignment = Alignment.Top,
                ) {
                    EqDbScale(
                        modifier = Modifier
                            .width(EQ_DB_SCALE_WIDTH)
                            .height(EQ_BAND_PANEL_HEIGHT),
                    )
                    Column(
                        modifier = Modifier
                            .horizontalGestureSafe()
                            .horizontalScroll(graphScrollState),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        EqResponseGraph(
                            settings = settings,
                            onBandChanged = onBandChanged,
                            modifier = Modifier
                                .width(graphContentWidth)
                                .height(EQ_BAND_PANEL_HEIGHT),
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
                Spacer(modifier = Modifier.height(4.dp))
                EqMiniResponseGraph(
                    settings = settings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                EqPresetMenu(
                    currentSettings = settings,
                    onApplyPreset = onApplyPreset,
                    onReset = onReset,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .ensureSingleItemRubberBand(listState),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
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
        }
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 390.dp,
            bottomInset = navigationBarInsetDp() + 16.dp,
        )
        PinnedBackTopBar(
            title = copy.equalizer,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

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
    onMidrangeChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
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
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Bass),
                                value = eqSettings.bass.coerceIn(0f, 1f),
                                valueRange = 0f..1f,
                                accentColor = Color(0xFF2FE08D),
                                modifier = Modifier.weight(1f),
                                onValueChange = onBassChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Midrange),
                                value = eqSettings.midrange.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFF39C2FF),
                                modifier = Modifier.weight(1f),
                                onValueChange = onMidrangeChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Treble),
                                value = eqSettings.treble.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFFFFB056),
                                modifier = Modifier.weight(1f),
                                onValueChange = onTrebleChanged,
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
                                modifier = Modifier.elovairePressScale(
                                    interactionSource = interactionSource,
                                    pressedScale = 0.9f,
                                    animationSpec = ElovaireMotion.chromeReleaseSpec(),
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
                                    Color.White.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
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
                modifier = Modifier.elovairePressScale(
                    interactionSource = interactionSource,
                    pressedScale = 0.9f,
                    animationSpec = ElovaireMotion.chromeReleaseSpec(),
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
    val copy = remember(selectedLanguage) { rootUiCopy(selectedLanguage) }
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
                                    modifier = Modifier
                                        .animateItem(
                                            placementSpec = ElovaireMotion.listPlacementSpec(),
                                        )
                                        .elovaireListDropdownReveal(
                                            key = language.name,
                                            index = languages.indexOf(language),
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
                                text = copy.ok,
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
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
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
                        text = copy.updateAvailable,
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
                            uiState.isInstalling -> copy.installing
                            uiState.isDownloading -> {
                                val percent = ((uiState.downloadProgress ?: 0f) * 100f).roundToInt()
                                "${copy.download} $percent%"
                            }
                            else -> copy.download
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
private fun UpdateStatusBanner(
    text: String,
    @DrawableRes iconResId: Int,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryTextColor = if (darkTheme) Color.White else InkText
    DynamicBackdropSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        overlayAlpha = 0.7f,
        borderColor = blurSurfaceBorderColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                ),
                color = primaryTextColor,
            )
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
            modifier = Modifier.elovairePressScale(
                interactionSource = interactionSource,
                pressedScale = 0.9f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
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
    onApplyPreset: (String, EqSettings) -> Unit,
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
            selected = activePresetName == null && EqValuePolicy.hasSignalAlteringEffects(currentSettings),
            emphasized = true,
            onClick = onReset,
        )
        presets.forEach { preset ->
            EqPresetPill(
                label = preset.name,
                selected = preset.name == activePresetName,
                onClick = {
                    onApplyPreset(
                        preset.name,
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
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    } else if (useSubtleIdleBackground) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier.elovairePressScale(
            interactionSource = interactionSource,
            pressedScale = 0.96f,
            animationSpec = ElovaireMotion.bounceSpringSpec(),
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
                    onBandChanged(
                        bandIndex,
                        eqGraphYToNormalized(
                            y = offset.y,
                            height = size.height.toFloat(),
                            config = eqGraphConfig,
                        ),
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
                        onBandChanged(
                            bandIndex,
                            eqGraphYToNormalized(
                                y = offset.y,
                                height = size.height.toFloat(),
                                config = eqGraphConfig,
                            ),
                        )
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
                        onBandChanged(
                            index,
                            eqGraphYToNormalized(
                                y = change.position.y,
                                height = size.height.toFloat(),
                                config = eqGraphConfig,
                            ),
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

private fun eqGraphYToNormalized(
    y: Float,
    height: Float,
    config: EqualizerDspConfig,
): Float {
    if (height <= 0f) return 0f
    val topPadding = height * 0.08f
    val bottomPadding = height * 0.12f
    val graphHeight = (height - topPadding - bottomPadding).coerceAtLeast(1f)
    val graphFraction = (1f - ((y - topPadding) / graphHeight)).coerceIn(0f, 1f)
    return EqualizerDspModel.graphFractionToNormalized(graphFraction, config)
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
            .height(EQ_BAND_LABEL_HEIGHT),
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

private fun eqDbLevels(): List<Float> = listOf(8f, 4f, 0f, -4f, -8f)

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
    fun adjustedPresetBandValue(value: Float): Float {
        return if (value < 0f) {
            (value * 1.25f).coerceIn(-1f, 0f)
        } else {
            value
        }
    }
    val bandShape = List(EqualizerDspModel.BAND_COUNT) { index ->
        when (EqualizerDspModel.bandDefinition(index).frequencyHz) {
            in 0f..30f -> adjustedPresetBandValue(bass)
            in 30.0001f..60f -> adjustedPresetBandValue(subBass)
            in 60.0001f..350f -> adjustedPresetBandValue(lowBass)
            in 350.0001f..750f -> adjustedPresetBandValue(lowMid)
            in 750.0001f..1_500f -> adjustedPresetBandValue(presence)
            in 1_500.0001f..3_000f -> adjustedPresetBandValue(upperMid)
            in 3_000.0001f..8_000f -> adjustedPresetBandValue(brilliance)
            else -> adjustedPresetBandValue(air)
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
