package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
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
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import elovaire.music.droidbeauty.app.BuildConfig
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.toEmbeddedLyricsText
import elovaire.music.droidbeauty.app.data.artist.ArtistBackdropState
import elovaire.music.droidbeauty.app.data.artist.ArtistImageReader
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspConfig
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspModel
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressState
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressConsumer
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import elovaire.music.droidbeauty.app.data.playback.SleepTimerOption
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.searchAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchPlaylists
import elovaire.music.droidbeauty.app.domain.search.searchSongsForPicker
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkPaletteAccent
import elovaire.music.droidbeauty.app.ui.interaction.CompactBarGestureActions
import elovaire.music.droidbeauty.app.ui.interaction.compactBarGestures
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.elovairePillActionMotion
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.LocalMotionRuntime
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.MotionEasing
import elovaire.music.droidbeauty.app.ui.motion.MotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.MotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.SettingsLanguageCopy
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.discLabel
import elovaire.music.droidbeauty.app.ui.i18n.availableReleasesLabel
import elovaire.music.droidbeauty.app.ui.i18n.artistTopTracksSubtitle
import elovaire.music.droidbeauty.app.ui.i18n.formatCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.homeCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedAllSongsSource
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.libraryFoldersCopy
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.playLabel
import elovaire.music.droidbeauty.app.ui.i18n.playingFromPrefix
import elovaire.music.droidbeauty.app.ui.i18n.queueTitle
import elovaire.music.droidbeauty.app.ui.i18n.repeatModeLabel
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.searchCopy
import elovaire.music.droidbeauty.app.ui.i18n.searchSortModeLabel
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.sleepTimerCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.displayLabel
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorScreen
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.AboutCardButtonAccent
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.ForceDarkColorScheme
import elovaire.music.droidbeauty.app.ui.theme.elovaireResolvedColorScheme
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import elovaire.music.droidbeauty.app.ui.theme.rememberElovaireOverscrollFactory
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.ToggleEnabledGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.math.pow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull



@Composable
internal fun NowPlayingScreen(
    playbackManager: NowPlayingPlayback,
    playerUiState: PlayerUiState,
    enrichedSongsById: Map<Long, Song>,
    audiobookSettings: AudiobookSettings,
    isFavorite: Boolean,
    playlists: List<Playlist>,
    lyricsUiState: LyricsUiState,
    lyricsEditorUiState: LyricsEditorUiState,
    activeLyricsLineIndex: Int,
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
    onAddCurrentSongToPlaylist: (Long, Song) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
    onQueueItemSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    onOpenEqualizer: () -> Unit,
    onToggleCrossfade: () -> Unit,
    onSleepTimerSelected: (SleepTimerOption) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val liveCurrentSong = playerUiState.currentSong
    val motionTransitions = rememberMotionTransitions()
    val motionSpecs = rememberMotionSpecs()
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
    val transitionEntrySongId = remember { liveCurrentSong?.id }
    var entryTransitionInvalidated by remember { mutableStateOf(false) }
    LaunchedEffect(liveCurrentSong?.id, transitionEntrySongId) {
        if (liveCurrentSong?.id != transitionEntrySongId) entryTransitionInvalidated = true
    }
    val activeTransitionSnapshot = remember(
        transitionSnapshot,
        transitionEntrySongId,
        liveCurrentSong?.id,
        entryTransitionInvalidated,
    ) {
        transitionSnapshot?.takeIf {
            !entryTransitionInvalidated &&
            liveCurrentSong?.id == transitionEntrySongId &&
                it.songId == transitionEntrySongId &&
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
    val expandSettleAnimationSpec = motionSpecs.tween<Float>(
        durationMillis = 420,
        easing = MotionEasing.RefinedDecelerate,
    )
    val collapseSettleAnimationSpec = motionSpecs.tween<Float>(
        durationMillis = 340,
        easing = MotionEasing.RefinedAccelerate,
    )
    var interactiveTransitionProgress by remember(liveCurrentSong?.id) { mutableStateOf<Float?>(null) }
    var dismissAnimationRunning by remember(liveCurrentSong?.id) { mutableStateOf(false) }
    var artworkSwipePushTarget by remember(liveCurrentSong?.id) { mutableFloatStateOf(0f) }
    val artworkSwipePushProgress by animateFloatAsState(
        targetValue = artworkSwipePushTarget,
        animationSpec = motionSpecs.tween(
            durationMillis = if (artworkSwipePushTarget > 0f) 80 else 220,
            easing = if (artworkSwipePushTarget > 0f) LinearOutSlowInEasing else MotionEasing.RefinedDecelerate,
        ),
        label = "artwork_swipe_push_progress",
    )
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
        animationSpec = motionSpecs.tween(320, easing = LinearOutSlowInEasing),
        label = "player_tint_color",
    )
    val baseSurface by animateColorAsState(
        targetValue = adaptivePalette.backdropBase,
        animationSpec = motionSpecs.tween(320, easing = LinearOutSlowInEasing),
        label = "player_backdrop_base",
    )
    val contentColor by animateColorAsState(
        targetValue = adaptivePalette.contentColor,
        animationSpec = motionSpecs.tween(260, easing = LinearOutSlowInEasing),
        label = "player_content_color",
    )
    val secondaryContentColor by animateColorAsState(
        targetValue = adaptivePalette.secondaryContentColor,
        animationSpec = motionSpecs.tween(260, easing = LinearOutSlowInEasing),
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
    var showSleepTimerDialog by remember { mutableStateOf(false) }
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
            .clipToBounds(),
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
                .then(
                    if (transitionInFlight) {
                        Modifier
                    } else {
                        Modifier.hazeSource(playerHazeState, zIndex = -1f)
                    },
                ),
        ) {
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
                animationSpec = motionSpecs.tween(80),
                label = "queue_favorite_alpha",
            )
            val transportAlpha by animateFloatAsState(
                targetValue = if (showQueueSheet) 0f else 1f,
                animationSpec = motionSpecs.tween(80),
                label = "queue_transport_alpha",
            )
            val queueProgressTransition by animateFloatAsState(
                targetValue = if (showQueueSheet) 1f else 0f,
                animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
                label = "queue_progress_transition",
            )
            val animatedArtworkCornerRadius by animateDpAsState(
                targetValue = if (showQueueSheet) 8.dp else ElovaireRadii.module,
                animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
                label = "queue_artwork_corner_radius",
            )
            fun Modifier.nowPlayingDismissGesture(): Modifier = pointerInput(currentSong.id) {
                var dragDistance = 0f
                var upwardPushDistance = 0f
                val dismissDistance = with(density) { 320.dp.toPx() }
                val pushDistance = with(density) { 132.dp.toPx() }
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (dismissAnimationRunning) return@detectVerticalDragGestures
                        val continuingDismissDrag = dragDistance > 0f
                        if (dragAmount < 0f && !continuingDismissDrag) {
                            change.consume()
                            upwardPushDistance = (upwardPushDistance - dragAmount).coerceIn(0f, pushDistance)
                            artworkSwipePushTarget = (upwardPushDistance / pushDistance).coerceIn(0f, 1f)
                            return@detectVerticalDragGestures
                        }
                        if (dragAmount <= 0f && !continuingDismissDrag) return@detectVerticalDragGestures
                        change.consume()
                        upwardPushDistance = 0f
                        artworkSwipePushTarget = 0f
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
                        upwardPushDistance = 0f
                        artworkSwipePushTarget = 0f
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
                        upwardPushDistance = 0f
                        artworkSwipePushTarget = 0f
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
            val playerSwipePushModifier = Modifier.graphicsLayer {
                translationY = with(density) { 22.dp.toPx() } * artworkSwipePushProgress
                scaleX = 1f - (0.012f * artworkSwipePushProgress)
                scaleY = 1f - (0.012f * artworkSwipePushProgress)
                alpha = 1f - (0.1f * artworkSwipePushProgress)
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
                            .then(
                                if (showQueueSheet || showSleepTimerDialog) {
                                    Modifier
                                } else {
                                    Modifier.nowPlayingDismissGesture()
                                },
                            ),
                    ) {
                        val expandedArtworkWidth = maxWidth
                        val compactArtworkWidth = (maxWidth * 0.38f) - 10.dp
                        val animatedArtworkWidth by animateDpAsState(
                            targetValue = if (showQueueSheet) compactArtworkWidth else expandedArtworkWidth,
                            animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
                            label = "queue_artwork_width",
                        )
                        val compactContentStart = compactArtworkWidth + 18.dp
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(animatedArtworkWidth),
                            ) {
                                if (!useSharedArtworkOverlay) {
                                    AnimatedContent(
                                        targetState = currentSong.id,
                                        transitionSpec = { motionTransitions.quickContentSwapTransform() },
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
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
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
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showQueueSheet,
                                enter = fadeIn(animationSpec = ElovaireMotion.contentFadeInSpec()) +
                                    expandVertically(
                                        expandFrom = Alignment.Top,
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                    ) +
                                    slideInVertically(
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                        initialOffsetY = { it },
                                    ),
                                exit = fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec()) +
                                    shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                    ) +
                                    slideOutVertically(
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                        targetOffsetY = { it },
                                    ),
                            ) {
                                NowPlayingProgressSummary(
                                    playbackManager = playbackManager,
                                    currentSongId = currentSong.id,
                                    freezeUpdates = transitionInFlight,
                                    format = displaySong?.audioFormat ?: currentSong.audioFormat,
                                    quality = displaySong?.audioQuality ?: currentSong.audioQuality,
                                    contentColor = contentColor,
                                    secondaryContentColor = secondaryContentColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
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
                val metadataClickInteractionSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(centeredInfoWidth)
                        .align(Alignment.Center)
                        .then(playerSwipePushModifier)
                        .graphicsLayer {
                            alpha = if (showQueueSheet) 0f else metadataSectionProgress
                        },
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (showQueueSheet) {
                                    Modifier
                                } else {
                                    Modifier.clickable(
                                        interactionSource = metadataClickInteractionSource,
                                        indication = null,
                                        onClick = {
                                            currentSong.takeIf { it.albumId != 0L }?.albumId?.let { albumId ->
                                                dismissNowPlaying {
                                                    onOpenCurrentAlbum(albumId)
                                                }
                                            }
                                        },
                                    )
                                },
                            ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedContent(
                            targetState = currentSong.id,
                            transitionSpec = { motionTransitions.quickContentSwapTransform() },
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
                        if (!showQueueSheet) {
                            FavoriteSongButton(
                                isFavorite = isFavorite,
                                tint = contentColor,
                                onClick = { onToggleFavorite(currentSong.id) },
                            )
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(centeredInfoWidth)
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-2).dp)
                    .then(playerSwipePushModifier)
                    .weight(1f),
            ) {
                val queueSheetTopExtension = (1000.dp - buttonNavigationScrollBoost()).coerceAtLeast(0.dp)
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
                                    alpha = progressSectionProgress * (1f - queueProgressTransition)
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
                                        if (currentSong.mediaKind == AudioMediaKind.Audiobook) {
                                            AudiobookTransportButton(
                                                iconResId = R.drawable.ic_elovaire_backward_filled,
                                                seconds = audiobookSettings.rewindSeconds,
                                                contentDescription = "Rewind",
                                                tint = contentColor,
                                                onClick = {
                                                    seekCurrentPlaybackBy(
                                                        playbackManager,
                                                        -audiobookSettings.rewindSeconds * 1_000L,
                                                    )
                                                },
                                            )
                                        } else {
                                            PlayerTransportButton(
                                                iconResId = R.drawable.ic_elovaire_backward_filled,
                                                contentDescription = "Previous",
                                                tint = contentColor,
                                                iconSize = 42.dp,
                                                onClick = onSkipPrevious,
                                            )
                                        }
                                        PlayerTransportButton(
                                            iconResId = if (transportShowsPause) R.drawable.ic_elovaire_pause_filled else R.drawable.ic_lucide_play,
                                            contentDescription = if (transportShowsPause) "Pause" else "Play",
                                            tint = contentColor,
                                            iconSize = 46.dp,
                                            onClick = onTogglePlayback,
                                        )
                                        if (currentSong.mediaKind == AudioMediaKind.Audiobook) {
                                            AudiobookTransportButton(
                                                iconResId = R.drawable.ic_elovaire_forward_filled,
                                                seconds = audiobookSettings.forwardSeconds,
                                                contentDescription = "Forward",
                                                tint = contentColor,
                                                onClick = {
                                                    seekCurrentPlaybackBy(
                                                        playbackManager,
                                                        audiobookSettings.forwardSeconds * 1_000L,
                                                    )
                                                },
                                            )
                                        } else {
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

                PopupCardMotionHost(
                    visible = showQueueSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    QueueSheet(
                        queue = playerUiState.queue,
                        currentIndex = playerUiState.currentIndex,
                        playlists = playlists,
                        playlistSongsById = enrichedSongsById,
                        currentSong = currentSong,
                        audiobookMode = currentSong.mediaKind == AudioMediaKind.Audiobook,
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
                        crossfadeEnabled = playerUiState.crossfadeEnabled,
                        onToggleCrossfade = {
                            queueStatusText = if (playerUiState.crossfadeEnabled) {
                                "Crossfade | Disabled"
                            } else {
                                "Crossfade | Enabled"
                            }
                            queueStatusVersion += 1L
                            onToggleCrossfade()
                        },
                        onOpenEqualizer = onOpenEqualizer,
                        sleepTimerActive = playerUiState.sleepTimer.option != SleepTimerOption.Off,
                        onOpenSleepTimer = {
                            showAddToPlaylistDialog = false
                            showSleepTimerDialog = true
                        },
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
                    .then(playerSwipePushModifier)
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
                hazeState = playerHazeState,
                onDismiss = { showAddToPlaylistDialog = false },
                onPlaylistSelected = { playlistId ->
                    currentSong?.let { onAddCurrentSongToPlaylist(playlistId, it).await() }
                        ?: PlaylistMutationResult.InvalidInput
                },
                onCreatePlaylist = onCreatePlaylist,
            )
        }
        ElovaireAnimatedVisibility(
            visible = showSleepTimerDialog,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(20f),
            enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.86f),
            exit = motionTransitions.overlayFadeExit(targetAlpha = 0.94f),
            label = "SleepTimerSheetOverlay",
        ) {
            CompositionLocalProvider(LocalPlayerHazeState provides playerHazeState) {
                SleepTimerDialog(
                    selectedOption = playerUiState.sleepTimer.option,
                    visible = showSleepTimerDialog,
                    onOptionSelected = { option ->
                        onSleepTimerSelected(option)
                        showSleepTimerDialog = false
                    },
                    onDismiss = { showSleepTimerDialog = false },
                )
            }
        }
    }
}

@Composable
private fun rememberRenderedPlaybackProgress(
    playbackManager: NowPlayingPlayback,
    currentSongId: Long?,
    freezeUpdates: Boolean,
): PlaybackProgressState {
    val liveProgress by playbackManager.progressState.collectAsStateWithLifecycle()
    val frozenProgress = remember(currentSongId, freezeUpdates) {
        liveProgress
    }
    return if (freezeUpdates) frozenProgress else liveProgress
}

@Composable
private fun NowPlayingProgressSummary(
    playbackManager: NowPlayingPlayback,
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
@OptIn(ExperimentalHazeApi::class)
private fun QueueSheet(
    queue: List<Song>,
    currentIndex: Int,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    currentSong: Song?,
    audiobookMode: Boolean,
    tint: Color,
    secondaryTint: Color,
    onSongSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    crossfadeEnabled: Boolean,
    onToggleCrossfade: () -> Unit,
    onOpenEqualizer: () -> Unit,
    sleepTimerActive: Boolean,
    onOpenSleepTimer: () -> Unit,
    onAddSongToPlaylist: (Long, Song) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
    statusText: String?,
    onDismiss: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val revealRegistry = rememberMotionRevealRegistry()
    val language = LocalAppLanguage.current
    val listState = rememberElovaireLazyListState("now_playing_queue")
    val queueEdgeHazeState = rememberHazeState()
    var playlistTargetSong by remember(currentSong?.id, queue) { mutableStateOf<Song?>(null) }
    val footerExpanded = statusText != null
    val footerHeight by animateDpAsState(
        targetValue = if (footerExpanded) 76.dp else 46.dp,
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
            QueueSheetHeader(
                queueSize = queue.size,
                language = language,
                tint = tint,
                secondaryTint = secondaryTint,
                onDismiss = onDismiss,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                QueueSongList(
                    queue = queue,
                    currentIndex = currentIndex,
                    tint = tint,
                    secondaryTint = secondaryTint,
                    listState = listState,
                    revealRegistry = revealRegistry,
                    queueEdgeHazeState = queueEdgeHazeState,
                    isPlaying = isPlaying,
                    onSongSelected = onSongSelected,
                    onQueueItemRemoved = onQueueItemRemoved,
                    onAddToPlaylist = { playlistTargetSong = it },
                )
            }
            QueueSheetFooter(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(footerHeight),
                statusText = statusText,
                tint = tint,
                language = language,
                audiobookMode = audiobookMode,
                crossfadeEnabled = crossfadeEnabled,
                onToggleCrossfade = onToggleCrossfade,
                onDismiss = onDismiss,
                onOpenEqualizer = onOpenEqualizer,
                sleepTimerActive = sleepTimerActive,
                onOpenSleepTimer = onOpenSleepTimer,
                shuffleEnabled = shuffleEnabled,
                onToggleShuffle = onToggleShuffle,
            )
        }
    }
    playlistTargetSong?.let { song ->
        AddToPlaylistPickerDialog(
            playlists = playlists,
            playlistSongsById = playlistSongsById,
            onDismiss = { playlistTargetSong = null },
            onPlaylistSelected = { playlistId ->
                onAddSongToPlaylist(playlistId, song).await()
            },
            onCreatePlaylist = onCreatePlaylist,
        )
    }
}

@Composable
private fun QueueSheetHeader(
    queueSize: Int,
    language: AppLanguage,
    tint: Color,
    secondaryTint: Color,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
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
                    text = localizedCountLabel(queueSize, "track", language),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    color = secondaryTint.copy(alpha = 0.7f),
                )
                val closeQueueInteractionSource = rememberElovaireInteractionSource()
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.1f))
                        .elovaireActionBump(
                            interactionSource = closeQueueInteractionSource,
                            label = "close_queue_bump",
                        )
                        .clickable(
                            interactionSource = closeQueueInteractionSource,
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
}

@Composable
private fun QueueSheetFooter(
    modifier: Modifier,
    statusText: String?,
    tint: Color,
    language: AppLanguage,
    audiobookMode: Boolean,
    crossfadeEnabled: Boolean,
    onToggleCrossfade: () -> Unit,
    onDismiss: () -> Unit,
    onOpenEqualizer: () -> Unit,
    sleepTimerActive: Boolean,
    onOpenSleepTimer: () -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
) {
    Box(modifier = modifier) {
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
            if (!audiobookMode) {
                PlayerSecondaryActionButton(
                    iconResId = R.drawable.ic_lucide_send_to_back,
                    label = "",
                    contentDescription = if (crossfadeEnabled) "Disable crossfade" else "Enable crossfade",
                    iconSize = 20.dp,
                    tint = tint,
                    showBackground = crossfadeEnabled,
                    onClick = onToggleCrossfade,
                )
                Spacer(modifier = Modifier.width(20.dp))
            }
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
                iconResId = R.drawable.ic_lucide_timer,
                label = "",
                contentDescription = sleepTimerCopy(language).title,
                iconSize = 20.dp,
                tint = tint,
                showBackground = sleepTimerActive,
                onClick = onOpenSleepTimer,
            )
            if (!audiobookMode) {
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

@Composable
@OptIn(ExperimentalHazeApi::class)
private fun QueueSongList(
    queue: List<Song>,
    currentIndex: Int,
    tint: Color,
    secondaryTint: Color,
    listState: LazyListState,
    revealRegistry: MotionRevealRegistry,
    queueEdgeHazeState: HazeState,
    isPlaying: Boolean,
    onSongSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(queueEdgeHazeState)
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            itemsIndexed(
                items = queue,
                key = { index, song -> "${song.id}_$index" },
                contentType = { _, _ -> "queue-song" },
            ) { index, song ->
                Box(
                    modifier = Modifier
                        .animateItem(
                            placementSpec = ElovaireMotion.listPlacementSpec(),
                        )
                        .elovaireListReveal(
                            itemKey = "${song.id}_$index",
                            index = index,
                            registry = revealRegistry,
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
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onRemoveFromQueue = { onQueueItemRemoved(index) },
                    )
                }
            }
        }
        QueueEdgeHaze(
            visible = listState.canScrollBackward,
            alignment = Alignment.TopCenter,
            tint = tint,
            hazeState = queueEdgeHazeState,
            startIntensity = 1f,
            endIntensity = 0f,
        )
        QueueEdgeHaze(
            visible = listState.canScrollForward,
            alignment = Alignment.BottomCenter,
            tint = tint,
            hazeState = queueEdgeHazeState,
            startIntensity = 0f,
            endIntensity = 1f,
        )
    }
}

@Composable
@OptIn(ExperimentalHazeApi::class)
internal fun BoxScope.QueueEdgeHaze(
    visible: Boolean,
    alignment: Alignment,
    tint: Color,
    hazeState: HazeState,
    startIntensity: Float,
    endIntensity: Float,
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .align(alignment)
            .fillMaxWidth()
            .height(20.dp)
            .hazeEffect(hazeState) {
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = startIntensity,
                    endIntensity = endIntensity,
                    preferPerformance = true,
                )
                blurRadius = 20.dp
                backgroundColor = Color.Transparent
                tints = listOf(
                    HazeTint(Color.Black.copy(alpha = 0.06f)),
                    HazeTint(tint.copy(alpha = 0.04f)),
                )
                noiseFactor = 0.015f
            },
    )
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
@Suppress("LongMethod")
internal fun SleepTimerDialog(
    selectedOption: SleepTimerOption,
    visible: Boolean,
    onOptionSelected: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { sleepTimerCopy(language) }
    var selectedMinutes by remember(selectedOption) {
        mutableFloatStateOf(
            selectedOption.durationMs?.div(60_000L)?.toFloat() ?: 30f,
        )
    }
    var pendingEndOfSong by remember(selectedOption) {
        mutableStateOf(selectedOption == SleepTimerOption.EndOfSong)
    }
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        PopupCardMotionHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            visible = visible,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = ElovaireRadii.dialog,
                    topEnd = ElovaireRadii.dialog,
                ),
                overlayAlpha = 0.6f,
                borderColor = null,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_timer),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = copy.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
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
                                contentDescription = copy.close,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = selectedMinutes.roundToInt(),
                        transitionSpec = {
                            val direction = if (targetState >= initialState) 1 else -1
                            (
                                fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                                    slideInVertically(
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                        initialOffsetY = { direction * it },
                                    ) +
                                    scaleIn(
                                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                        initialScale = 0.88f,
                                    )
                                ) togetherWith (
                                    fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                                        slideOutVertically(
                                            animationSpec = ElovaireMotion.offsetSoft(durationMillis = ElovaireMotion.Standard),
                                            targetOffsetY = { -direction * it },
                                        ) +
                                        scaleOut(
                                            animationSpec = ElovaireMotion.fadeFast(),
                                            targetScale = 1.08f,
                                        )
                                    )
                        },
                        label = "sleep_timer_minutes_flip",
                    ) { minutes ->
                        Text(
                            text = minutes.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(34f)),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = copy.minuteSuffix,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(34f)),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SleepTimerSlider(
                        value = selectedMinutes,
                        onValueChange = {
                            selectedMinutes = it
                            pendingEndOfSong = false
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "10",
                            style = MaterialTheme.typography.labelLarge,
                            color = readableSecondaryTextColor(),
                        )
                        Text(
                            text = "30",
                            style = MaterialTheme.typography.labelLarge,
                            color = readableSecondaryTextColor(),
                        )
                        Text(
                            text = "60",
                            style = MaterialTheme.typography.labelLarge,
                            color = readableSecondaryTextColor(),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .clickable { onOptionSelected(SleepTimerOption.Off) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_timer_reset),
                            contentDescription = "Reset timer",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    SleepTimerAction(
                        text = copy.endOfSong,
                        selected = pendingEndOfSong,
                        modifier = Modifier.weight(1f),
                        onClick = { pendingEndOfSong = true },
                    )
                    SleepTimerAction(
                        text = copy.confirm,
                        selected = !pendingEndOfSong &&
                            selectedOption.durationMs == selectedMinutes.roundToInt() * 60_000L,
                        modifier = Modifier.weight(1f),
                        emphasized = true,
                        onClick = {
                            if (pendingEndOfSong) {
                                onOptionSelected(SleepTimerOption.EndOfSong)
                            } else {
                                SleepTimerOption.forMinutes(selectedMinutes.roundToInt())?.let(onOptionSelected)
                            }
                        },
                    )
                }
            }
        }
    }
}

}

@Composable
private fun SleepTimerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val fraction = when (val minutes = value.coerceIn(10f, 60f)) {
        in 10f..30f -> (minutes - 10f) / 40f
        else -> 0.5f + ((minutes - 30f) / 60f)
    }.coerceIn(0f, 1f)
    val lineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val barCount = 41
    val activeBarIndex = (fraction * (barCount - 1)).roundToInt()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .horizontalGestureSafe(),
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)
        val updateFromX: (Float) -> Unit = { xPosition ->
            val normalized = (xPosition / maxWidthPx).coerceIn(0f, 1f)
            val minutes = if (normalized <= 0.5f) {
                10f + (normalized * 40f)
            } else {
                30f + ((normalized - 0.5f) * 60f)
            }
            currentOnValueChange((minutes / 5f).roundToInt() * 5f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxWidthPx) {
                    detectTapGestures { offset -> updateFromX(offset.x) }
                }
                .pointerInput(maxWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateFromX(offset.x) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateFromX(change.position.x)
                        },
                    )
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                repeat(barCount) { index ->
                    val active = index <= activeBarIndex
                    val waveDelay = (kotlin.math.abs(index - activeBarIndex) * 7).coerceAtMost(140)
                    val animatedHeight by animateDpAsState(
                        targetValue = if (active) 22.dp else 13.dp,
                        animationSpec = ElovaireMotion.standardTween(
                            durationMillis = 220,
                            delayMillis = waveDelay,
                            easing = FastOutSlowInEasing,
                        ),
                        label = "sleep_timer_bar_height_$index",
                    )
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (active) 1f else 0.3f,
                        animationSpec = ElovaireMotion.standardTween(
                            durationMillis = 180,
                            delayMillis = waveDelay,
                            easing = LinearOutSlowInEasing,
                        ),
                        label = "sleep_timer_bar_alpha_$index",
                    )
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(animatedHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(lineColor.copy(alpha = animatedAlpha)),
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepTimerAction(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    val backgroundColor = when {
        emphasized -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .background(backgroundColor)
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "sleep_timer_action_bump",
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
    val motionSpecs = rememberMotionSpecs()
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
                .padding(horizontal = 6.dp, vertical = 4.dp),
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
                    enter = fadeIn(animationSpec = motionSpecs.tween(60)),
                    exit = fadeOut(animationSpec = motionSpecs.tween(60)),
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
    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
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
                onExitFinished = { shouldRenderMenu = false },
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

private fun seekCurrentPlaybackBy(
    playbackManager: NowPlayingPlayback,
    deltaMs: Long,
) {
    val progress = playbackManager.progressState.value
    val target = (progress.positionMs + deltaMs).coerceAtLeast(0L)
    playbackManager.seekTo(progress.durationMs.takeIf { it > 0L }?.let(target::coerceAtMost) ?: target)
}

@Composable
private fun AudiobookTransportButton(
    iconResId: Int,
    seconds: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        PlayerTransportButton(
            iconResId = iconResId,
            contentDescription = contentDescription,
            tint = tint,
            iconSize = 42.dp,
            onClick = onClick,
        )
        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.labelMedium,
            color = tint.copy(alpha = 0.84f),
        )
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
internal fun FavoriteSongButton(
    isFavorite: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = tint.copy(alpha = 0.2f),
    borderColor: Color = Color.Transparent,
    frosted: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    val motionRuntime = LocalMotionRuntime.current
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        val changed = previousFavoriteState != isFavorite
        previousFavoriteState = isFavorite
        if (!changed) return@LaunchedEffect
        shouldBounce = true
        delay(motionRuntime.duration(180L))
        shouldBounce = false
    }
    val iconScale = animateFloatAsState(
        targetValue = when {
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
            .elovairePillActionMotion(
                confirmation = shouldBounce,
                interactionSource = interactionSource,
                label = "favorite_button_bump",
            )
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
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
            )
        }
    }
}

@Composable
internal fun AlbumHeaderActionButton(
    iconResId: Int,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    iconSize: Dp = 20.dp,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()

    Box(
        modifier = Modifier
            .size(44.dp)
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "${contentDescription}_album_header_bump",
            )
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
internal fun AlbumHeaderPlayButton(
    tint: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val interactionSource = rememberElovaireInteractionSource()

    Surface(
        modifier = Modifier.elovaireActionBump(
            interactionSource = interactionSource,
            label = "album_play_button_bump",
        ),
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
internal fun InlineFavoriteSongButton(
    isFavorite: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    val motionRuntime = LocalMotionRuntime.current
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        val changed = previousFavoriteState != isFavorite
        previousFavoriteState = isFavorite
        if (!changed) return@LaunchedEffect
        shouldBounce = true
        delay(motionRuntime.duration(180L))
        shouldBounce = false
    }
    val iconScale = animateFloatAsState(
        targetValue = when {
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
            .elovairePillActionMotion(
                confirmation = shouldBounce,
                interactionSource = interactionSource,
                label = "inline_favorite_bump",
            )
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
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
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
        val x = when (layoutDirection) {
            androidx.compose.ui.unit.LayoutDirection.Ltr -> anchorBounds.right - popupContentSize.width
            androidx.compose.ui.unit.LayoutDirection.Rtl -> anchorBounds.left
        }.coerceIn(0, maxX)
        val y = anchorBounds.top.coerceIn(0, maxY)
        return IntOffset(x, y)
    }
}

@Composable
private fun OverflowContextMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExitFinished: () -> Unit,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = OverflowContextMenuPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        PopupCardMotionHost(
            visible = expanded,
            onExitFinished = onExitFinished,
        ) {
            content()
        }
    }
}

@Composable
internal fun AlbumOverflowMenuButton(
    album: Album,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    tint: Color,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (Long) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction?,
    onDeleteAlbum: () -> Unit,
) {
    val language = LocalAppLanguage.current
    var expanded by remember(album.id) { mutableStateOf(false) }
    var shouldRenderMenu by remember(album.id) { mutableStateOf(false) }
    var showPlaylistDialog by remember(album.id) { mutableStateOf(false) }
    val interactionSource = rememberElovaireInteractionSource()

    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        }
    }

    Box {
        Box(
            modifier = Modifier
                .size(24.dp)
                .elovaireActionBump(
                    interactionSource = interactionSource,
                    label = "album_overflow_bump",
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
                contentDescription = "Album options",
                tint = tint.copy(alpha = 0.82f),
                modifier = Modifier.size(OverflowMenuIconSize),
            )
        }

        if (shouldRenderMenu) {
            OverflowContextMenuPopup(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                onExitFinished = { shouldRenderMenu = false },
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
                onAddToPlaylist(playlistId).await()
            },
            onCreatePlaylist = onCreatePlaylist,
        )
    }
}

@Composable
internal fun SongOverflowMenuButton(
    song: Song,
    tint: Color,
    showGoToAlbum: Boolean = true,
) {
    val actions = LocalSongMenuActions.current
    val language = LocalAppLanguage.current
    var expanded by remember(song.id) { mutableStateOf(false) }
    var shouldRenderMenu by remember(song.id) { mutableStateOf(false) }
    var showPlaylistDialog by remember(song.id) { mutableStateOf(false) }
    val interactionSource = rememberElovaireInteractionSource()
    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        }
    }

    Box {
        Box(
            modifier = Modifier
                .size(24.dp)
                .elovaireActionBump(
                    interactionSource = interactionSource,
                    label = "song_overflow_bump",
                )
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
                onExitFinished = { shouldRenderMenu = false },
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
                actions.onAddToPlaylist(playlistId, song).await()
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
internal fun TopBarContextMenuOverlay(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val motionTransitions = rememberMotionTransitions()
    val language = LocalAppLanguage.current
    val settingsCopy = remember(language) { settingsCopy(language) }
    BackHandler(enabled = expanded, onBack = onDismiss)
    Box(
        modifier = modifier,
    ) {
        ElovaireAnimatedVisibility(
            visible = expanded,
            modifier = Modifier.fillMaxSize(),
            enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.86f),
            exit = motionTransitions.overlayFadeExit(),
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
        PopupCardMotionHost(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 10.dp),
        ) {
            FrostedContextMenuSurface(
                modifier = Modifier.width(190.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTagsAsResourceId = true }
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    SongContextMenuItem(
                        modifier = Modifier.testTag("top_menu_settings"),
                        iconResId = R.drawable.ic_lucide_settings,
                        text = settingsCopy.settings,
                        tint = MaterialTheme.colorScheme.onSurface,
                        topPadding = 8.dp,
                        onClick = onOpenSettings,
                    )
                    DividerLine()
                    SongContextMenuItem(
                        modifier = Modifier.testTag("top_menu_equalizer"),
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
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int,
    text: String,
    tint: Color,
    containerColor: Color = Color.Transparent,
    cornerRadius: Dp = ElovaireRadii.card * 0.72f,
    topPadding: Dp = 6.dp,
    bottomPadding: Dp = 6.dp,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = topPadding, end = 10.dp, bottom = bottomPadding)
            .elovairePressScale(
                pressedScale = 0.985f,
                animationSpec = ElovaireMotion.softPressReturnSpec(),
                interactionSource = interactionSource,
                label = "${text}_context_menu_scale",
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
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
private fun LyricsOverlay(
    song: Song?,
    lyricsUiState: LyricsUiState,
    lyricsEditorUiState: LyricsEditorUiState,
    activeLyricsLineIndex: Int,
    tintColor: Color,
    contentColor: Color,
    secondaryContentColor: Color,
    onSeekTo: (Long) -> Unit,
    onHideLyrics: () -> Unit,
    onSaveLyrics: (String) -> Unit,
    onClearLyricsEditorError: () -> Unit,
) {
    val motionTransitions = rememberMotionTransitions()
    val motionSpecs = rememberMotionSpecs()
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var overlayEntered by remember(song?.id) { mutableStateOf(false) }
    val hideButtonArea = 112.dp
    val lyricsBottomBlurArea = 92.dp
    val lyricsButtonArea = 72.dp
    val navigationBarInset = navigationBarInsetDp()
    val bottomBlurSurfaceHeight = lyricsBottomBlurArea + navigationBarInset
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
        animationSpec = motionSpecs.tween(
            durationMillis = MotionDuration.ScreenExpand,
            easing = FastOutSlowInEasing,
        ),
        label = "lyrics_background_reveal",
    )
    val headerReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = motionSpecs.tween(
            durationMillis = MotionDuration.Standard,
            delayMillis = 30,
            easing = LinearOutSlowInEasing,
        ),
        label = "lyrics_header_reveal",
    )
    val dividerReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = motionSpecs.tween(
            durationMillis = MotionDuration.Standard,
            delayMillis = 65,
            easing = LinearOutSlowInEasing,
        ),
        label = "lyrics_divider_reveal",
    )
    val contentReveal by animateFloatAsState(
        targetValue = if (overlayEntered) 1f else 0f,
        animationSpec = motionSpecs.tween(
            durationMillis = MotionDuration.Screen,
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
                ?.toEmbeddedLyricsText()
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
                    .graphicsLayer {
                        alpha = headerReveal
                        translationY = (1f - headerReveal) * (-18.dp.toPx())
                    },
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
                        enter = motionTransitions.contextMenuEnter(),
                        exit = motionTransitions.contextMenuExit(),
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
                                        ?.toEmbeddedLyricsText()
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
                        .graphicsLayer {
                            alpha = dividerReveal
                            translationY = (1f - dividerReveal) * (-12.dp.toPx())
                        },
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
                        .graphicsLayer {
                            alpha = contentReveal
                            translationY = (1f - contentReveal) * (-10.dp.toPx())
                        },
                ) {
                    AnimatedContent(
                        targetState = isEditingLyrics,
                        transitionSpec = {
                            fadeIn(animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = LinearOutSlowInEasing)) +
                                slideInVertically(
                                    animationSpec = motionSpecs.tween(MotionDuration.ScreenExpand, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 12 },
                                ) +
                                expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = motionSpecs.tween(MotionDuration.ScreenExpand, easing = FastOutSlowInEasing),
                                ) togetherWith
                                fadeOut(animationSpec = motionSpecs.tween(MotionDuration.Quick, easing = FastOutLinearInEasing))
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
                            LyricsUiState.Hidden,
                            LyricsUiState.Empty -> {
                                LyricsUnavailableContent(
                                    noLyricsText = copy.noLyrics,
                                    contentColor = contentColor,
                                    onAddLyrics = {
                                        lyricsDraft = ""
                                        onClearLyricsEditorError()
                                        isEditingLyrics = true
                                    },
                                )
                            }
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

                            is LyricsUiState.Ready -> {
                                if (state.payload.lines.isEmpty()) {
                                    LyricsUnavailableContent(
                                        noLyricsText = copy.noLyrics,
                                        contentColor = contentColor,
                                        onAddLyrics = {
                                            lyricsDraft = ""
                                            onClearLyricsEditorError()
                                            isEditingLyrics = true
                                        },
                                    )
                                } else {
                                    LyricsReadyContent(
                                        song = song,
                                        payload = state.payload,
                                        activeLyricLineIndex = activeLyricsLineIndex,
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
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBlurSurfaceHeight)
                .clipToBounds()
                .zIndex(3f),
        ) {
            if (!isEditingLyrics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                enter = motionTransitions.standardEnter(),
                exit = motionTransitions.standardExit(),
                label = "lyrics_bottom_shadow_visibility",
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
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(hideButtonArea + navigationBarInset)
                .padding(bottom = navigationBarInset)
                .zIndex(4f),
        ) {
            ElovaireAnimatedVisibility(
                visible = !isEditingLyrics,
                modifier = Modifier.fillMaxSize(),
                enter = motionTransitions.standardEnter(),
                exit = motionTransitions.standardExit(),
                label = "hide_lyrics_action_visibility",
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lyricsButtonArea)
                            .offset(y = (-22).dp)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val hideLyricsInteractionSource = rememberElovaireInteractionSource()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier.hazeEffect(lyricsHazeState) {
                                            blurRadius = 34.dp
                                            backgroundColor = Color.Transparent
                                        }
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            Surface(
                                onClick = onHideLyrics,
                                interactionSource = hideLyricsInteractionSource,
                                modifier = Modifier.elovaireActionBump(
                                    interactionSource = hideLyricsInteractionSource,
                                    label = "hide_lyrics_bump",
                                ),
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
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    )
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
                .height(hideButtonArea + navigationBarInset)
                .padding(bottom = navigationBarInset)
                .zIndex(4f),
        ) {
            ElovaireAnimatedVisibility(
                visible = isEditingLyrics,
                modifier = Modifier.fillMaxSize(),
                enter = motionTransitions.standardEnter(),
                exit = motionTransitions.standardExit(),
                label = "cancel_lyrics_edit_action_visibility",
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lyricsButtonArea)
                            .offset(y = (-22).dp)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val cancelLyricsInteractionSource = rememberElovaireInteractionSource()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier.hazeEffect(lyricsHazeState) {
                                            blurRadius = 34.dp
                                            backgroundColor = Color.Transparent
                                        }
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            Surface(
                                onClick = {
                                    isEditingLyrics = false
                                    focusManager.clearFocus(force = true)
                                    onClearLyricsEditorError()
                                },
                                interactionSource = cancelLyricsInteractionSource,
                                modifier = Modifier.elovaireActionBump(
                                    interactionSource = cancelLyricsInteractionSource,
                                    label = "cancel_lyrics_edit_bump",
                                ),
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = contentColor.copy(alpha = 0.18f),
                                contentColor = contentColor,
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
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
private fun LyricsUnavailableContent(
    noLyricsText: String,
    contentColor: Color,
    onAddLyrics: () -> Unit,
) {
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
                    text = noLyricsText,
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
                onClick = onAddLyrics,
            )
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
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = Modifier
            .size(44.dp)
            .elovaireActionBump(
                enabled = enabled,
                interactionSource = interactionSource,
                label = "lyrics_editor_action_bump",
            )
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
    val motionSpecs = rememberMotionSpecs()
    val autoScrollCenterOffsetPx = with(LocalDensity.current) { 180.dp.roundToPx() }
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
                animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
                label = "lyrics_line_font_$index",
            )
            val lineColor by animateColorAsState(
                targetValue = when {
                    isActive -> contentColor.copy(alpha = 1f)
                    else -> contentColor.copy(alpha = 0.7f)
                },
                animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
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
    contentDescription: String = label,
    iconSize: Dp = 18.dp,
    tint: Color,
    showBackground: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    var transientHighlight by remember { mutableStateOf(false) }
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showBackground || transientHighlight) 0.2f else 0f,
        animationSpec = motionSpecs.tween(MotionDuration.Standard),
        label = "${label}_button_alpha",
    )
    val buttonScale by animateFloatAsState(
        targetValue = when {
            showBackground -> 1f
            else -> 0.96f
        },
        animationSpec = motionSpecs.spring(
            dampingRatio = 0.72f,
            stiffness = 340f,
        ),
        label = "${label}_button_scale",
    )
    Box(
        modifier = Modifier
            .scale(buttonScale)
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "${label}_player_secondary_bump",
            )
            .clip(RoundedCornerShape(ElovaireRadii.pill))
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
            delay(motionRuntime.duration(220L))
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
    val motionSpecs = rememberMotionSpecs()
    val animatedVolume by animateFloatAsState(
        targetValue = volume.coerceIn(0f, 1f),
        animationSpec = motionSpecs.spring(
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

private fun repeatModeIconRes(repeatMode: PlaybackRepeatMode): Int {
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> R.drawable.ic_lucide_arrow_line_down
        PlaybackRepeatMode.One -> R.drawable.ic_lucide_repeat_1
        PlaybackRepeatMode.All -> R.drawable.ic_lucide_repeat
    }
}
