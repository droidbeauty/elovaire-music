package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.screens.common.MutedSectionHeader
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.SectionTitleRow
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.screens.common.readableMutedIconColor
import elovaire.music.droidbeauty.app.ui.screens.common.readableCardSurfaceColor
import elovaire.music.droidbeauty.app.ui.screens.common.toggleSelection

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



@OptIn(ExperimentalHazeApi::class)

@Composable
internal fun HomeScreen(
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
    onOpenRecentlyAdded: () -> Unit,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val listState = rememberElovaireLazyListState("home_screen")
    val language = LocalAppLanguage.current
    val homeCopy = remember(language) { homeCopy(language) }
    val motionRuntime = LocalMotionRuntime.current
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
            delay(motionRuntime.duration(70L))
            revealModules = true
            delay(motionRuntime.duration(520L))
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
        lastPlayedPlaylist?.songIds
            ?.mapNotNull(songsById::get)
            ?.filter { it.mediaKind == AudioMediaKind.Music }
            .orEmpty()
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
                        drawStopIndicator = {},
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
                                            onClick = onOpenRecentlyAdded,
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
        val sharedSourceToken = remember { AlbumSharedTransitionToken.next() }
        val sharedTransitionController = LocalAlbumSharedTransitionController.current
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
                    onClick = {
                        sharedTransitionController?.select(album.id, sharedSourceToken)
                        onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx))
                    },
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
                    modifier = Modifier
                        .size(88.dp)
                        .albumSharedArtwork(
                            albumId = album.id,
                            sourceToken = sharedSourceToken,
                        ),
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
                val playInteractionSource = rememberElovaireInteractionSource()
                Surface(
                    modifier = Modifier.elovaireActionBump(
                        interactionSource = playInteractionSource,
                        label = "album_header_play_bump",
                    ),
                    onClick = onPlay,
                    interactionSource = playInteractionSource,
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
                PlaylistArtworkPreview(
                    songs = songs,
                    title = playlist.name,
                    modifier = Modifier.size(88.dp),
                    cornerRadius = ElovaireRadii.artwork,
                    placeholderIconSize = 20.dp,
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
                    modifier = Modifier
                        .width(46.dp)
                        .height(92.dp),
                    shape = RoundedCornerShape(ElovaireRadii.pill),
                    color = playBackground,
                    contentColor = playTint,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val shuffleInteractionSource = rememberElovaireInteractionSource()
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .elovaireActionBump(
                                    interactionSource = shuffleInteractionSource,
                                    label = "playlist_header_shuffle_bump",
                                )
                                .clickable(
                                    interactionSource = shuffleInteractionSource,
                                    indication = null,
                                    onClick = onShuffle,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_shuffle),
                                contentDescription = "Shuffle playlist",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        val playInteractionSource = rememberElovaireInteractionSource()
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .elovaireActionBump(
                                    interactionSource = playInteractionSource,
                                    label = "playlist_header_play_bump",
                                )
                                .clickable(
                                    interactionSource = playInteractionSource,
                                    indication = null,
                                    onClick = onPlay,
                                ),
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
    }
}

@Composable
internal fun Modifier.libraryRemovalAnimation(isRemoving: Boolean): Modifier {
    val motionSpecs = rememberMotionSpecs()
    val alpha by animateFloatAsState(
        targetValue = if (isRemoving) 0f else 1f,
        animationSpec = motionSpecs.tween(if (isRemoving) MotionDuration.Standard else MotionDuration.Fast),
        label = "library_item_removal_alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (isRemoving) 0.96f else 1f,
        animationSpec = motionSpecs.tween(if (isRemoving) MotionDuration.Standard else MotionDuration.Fast),
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
    onAddAlbumToPlaylist: (Long, Album) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
    playlistSongsById: Map<Long, Song>,
    favoriteSongIds: Set<Long>,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
    onDeleteAlbumFromDevice: (Album) -> Unit,
) {
    val revealRegistry = rememberMotionRevealRegistry()
    val motionTransitions = rememberMotionTransitions()
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    var selectedAlbumIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var showPlaylistPicker by rememberSaveable { mutableStateOf(false) }
    val listState = rememberElovaireLazyListState(title, "album_collection_list")
    val gridState = rememberElovaireLazyGridState(title, "album_collection_grid")
    val selectionHazeState = rememberHazeState()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(selectionHazeState, zIndex = -1f),
        ) {
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

                    itemsIndexed(
                        items = sortedAlbums,
                        key = { _, album -> album.id },
                        contentType = { _, _ -> "album_grid_card" },
                    ) { index, album ->
                        AlbumGridCard(
                            album = album,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListReveal(
                                    itemKey = album.id,
                                    index = index,
                                    registry = revealRegistry,
                                )
                                .libraryRemovalAnimation(album.id in removingAlbumIds),
                            selectionMode = selectionModeActive,
                            selected = album.id in selectedAlbumIds,
                            enableSharedTransition = !selectionModeActive,
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
                        if (showSortOptions) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    itemsIndexed(
                        items = sortedAlbums,
                        key = { _, album -> album.id },
                        contentType = { _, _ -> "album_grid_card" },
                    ) { index, album ->
                        AlbumGridCard(
                            album = album,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListReveal(
                                    itemKey = album.id,
                                    index = index,
                                    registry = revealRegistry,
                                )
                                .libraryRemovalAnimation(album.id in removingAlbumIds),
                            selectionMode = selectionModeActive,
                            selected = album.id in selectedAlbumIds,
                            showText = false,
                            artworkCornerRadius = 0.dp,
                            showArtworkGlow = false,
                            enableSharedTransition = !selectionModeActive,
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

                    itemsIndexed(
                        items = sortedAlbums,
                        key = { _, album -> album.id },
                        contentType = { _, _ -> "album_compact_row" },
                    ) { index, album ->
                        Box(
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListReveal(
                                    itemKey = album.id,
                                    index = index,
                                    registry = revealRegistry,
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
        }
        AnimatedVisibility(
            visible = selectionModeActive,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = motionTransitions.verticalRevealEnter(),
            exit = motionTransitions.verticalRevealExit(),
        ) {
            TopBarSelectionMenu(
                topBarHeight = topPadding,
                hazeState = selectionHazeState,
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
                val songs = selectedAlbums.flatMap(Album::songs).distinctBy(Song::id)
                val combinedAlbum = Album(
                    id = -1L,
                    title = "",
                    artist = "",
                    artUri = null,
                    songCount = songs.size,
                    durationMs = songs.sumOf(Song::durationMs),
                    songs = songs,
                )
                val result = onAddAlbumToPlaylist(playlistId, combinedAlbum).await()
                if (result is PlaylistMutationResult.Success) {
                    showPlaylistPicker = false
                    selectedAlbumIds = emptySet()
                }
                result
            },
            onCreatePlaylist = onCreatePlaylist,
        )
    }
}

@Composable
internal fun TopBarSelectionMenu(
    topBarHeight: Dp,
    hazeState: HazeState,
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
            hazeState = hazeState,
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
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
            )
        }
    }
}

internal data class TopBarMenuAction(
    @DrawableRes val iconResId: Int,
    val label: String,
    val tint: Color,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
internal fun TopBarDualActionMenu(
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
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .elovaireActionBump(
                enabled = enabled,
                interactionSource = interactionSource,
                label = "${label}_album_collection_action_bump",
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
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
    val interactionSource = rememberElovaireInteractionSource()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.elovaireActionBump(
                interactionSource = interactionSource,
                label = "album_sort_bump",
            ),
            onClick = onToggleExpanded,
            interactionSource = interactionSource,
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
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

        PopupCardMotionHost(
            visible = expanded,
        ) {
            Surface(
                shape = RoundedCornerShape(ElovaireRadii.card),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
internal fun LibraryHubScreen(
    libraryState: LibraryUiState,
    audiobookProgressByKey: Map<String, elovaire.music.droidbeauty.app.data.playback.AudiobookProgress> = emptyMap(),
    currentSongId: Long? = null,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    onOpenCollection: (LibraryCollectionKind) -> Unit,
    onOpenRecentlyAdded: () -> Unit,
    onOpenAudiobooks: () -> Unit,
    onAudiobookSelected: (elovaire.music.droidbeauty.app.domain.model.Audiobook) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val musicSongs = remember(libraryState.songs) {
        libraryState.songs.filter { it.mediaKind == AudioMediaKind.Music }
    }
    val totalSongs = musicSongs.size
    val totalAlbums = libraryState.albums.size
    val recentlyAddedAlbums = remember(libraryState.albums) {
        recentlyAddedAlbumsFor(libraryState).take(4)
    }
    val totalArtists = remember(musicSongs) {
        musicSongs.map { it.artist.ifBlank { "Unknown Artist" } }.distinct().size
    }
    val totalGenres = remember(musicSongs) {
        musicSongs.map { it.genre.ifBlank { "Unknown Genre" } }.distinct().size
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
                                onClick = onOpenRecentlyAdded,
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

            if (libraryState.audiobooks.isNotEmpty()) {
                item(key = "library_audiobooks") {
                    AudiobookMiniGallery(
                        books = libraryState.audiobooks,
                        progressByBookKey = audiobookProgressByKey,
                        currentSongId = currentSongId,
                        onOpenCollection = onOpenAudiobooks,
                        onBookSelected = onAudiobookSelected,
                    )
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
            .padding(horizontal = 6.dp, vertical = 12.5.dp),
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
internal fun LibraryCollectionScreen(
    kind: LibraryCollectionKind,
    libraryState: LibraryUiState,
    artistImageRepository: ArtistImageReader,
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
    onAddAlbumToPlaylist: (Long, Album) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
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
            songs = libraryState.songs.filter { it.mediaKind == AudioMediaKind.Music },
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
            songs = libraryState.songs.filter { it.mediaKind == AudioMediaKind.Music },
            artistImageRepository = artistImageRepository,
            bottomPadding = bottomPadding,
            onBack = onBack,
            onArtistSelected = onArtistSelected,
        )

        LibraryCollectionKind.Genres -> GenreCollectionScreen(
            songs = libraryState.songs.filter { it.mediaKind == AudioMediaKind.Music },
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
    val revealRegistry = rememberMotionRevealRegistry()
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
                HomeRecentSongRow(
                    song = song,
                    isFavorite = song.id in favoriteSongIds,
                    isCurrentSong = song.id == currentSongId,
                    isPlaybackActive = isCurrentSongPlaying,
                    onClick = { onSongSelected(song, sortedSongs) },
                    onToggleFavorite = { onToggleFavorite(song.id) },
                    showDivider = index != sortedSongs.lastIndex,
                    modifier = Modifier
                        .animateItem(
                            placementSpec = ElovaireMotion.listPlacementSpec(),
                        )
                        .elovaireListReveal(
                            itemKey = song.id,
                            index = index,
                            registry = revealRegistry,
                        )
                        .libraryRemovalAnimation(song.id in removingSongIds),
                )
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
    val interactionSource = rememberElovaireInteractionSource()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.elovaireActionBump(
                interactionSource = interactionSource,
                label = "song_sort_bump",
            ),
            onClick = onToggleExpanded,
            interactionSource = interactionSource,
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
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

        PopupCardMotionHost(
            visible = expanded,
        ) {
            Surface(
                shape = RoundedCornerShape(ElovaireRadii.card),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
    artistImageRepository: ArtistImageReader,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val listState = rememberElovaireLazyListState("artist_collection")
    val artists = remember(songs) {
        songs
            .groupBy { it.libraryArtistName() }
            .map { (name, artistSongs) ->
                ArtistEntry(
                    name = name,
                    artUri = artistSongs.firstOrNull { it.artUri != null }?.artUri,
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
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(
                items = artists,
                key = { _, artist -> artist.name },
                contentType = { _, _ -> "artist-row" },
            ) { index, artist ->
                ArtistRow(
                    artist = artist,
                    artistImageRepository = artistImageRepository,
                    onClick = { onArtistSelected(artist.name) },
                )
                if (index != artists.lastIndex) DividerLine()
            }
        }
        FastScrollbar(
            state = listState,
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
internal fun GenreAlbumsScreen(
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
    onAddAlbumToPlaylist: (Long, Album) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
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
internal fun ArtistDetailScreen(
    artistName: String,
    libraryState: LibraryUiState,
    artistBackdropState: ArtistBackdropState,
    songPlayCounts: Map<Long, Int>,
    favoriteSongIds: Set<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onPlayArtist: (List<Song>) -> Unit,
    onShuffleArtist: (List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val normalizedArtist = artistName.ifBlank { "Unknown Artist" }
    val artistSongs = remember(normalizedArtist, libraryState.songs) {
        libraryState.songs.filter { song ->
            song.mediaKind == AudioMediaKind.Music &&
            song.libraryArtistName().equals(normalizedArtist, ignoreCase = true)
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
                top = 0.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item("artist_hero") {
                ArtistHeroHeader(
                    artistName = normalizedArtist,
                    subtitle = buildArtistScreenSubtitle(
                        songCount = artistSongs.size,
                        albumCount = artistAlbums.size,
                        language = LocalAppLanguage.current,
                    ),
                    backdropState = artistBackdropState,
                    localArtworkUri = artistAlbums.firstOrNull { it.artUri != null }?.artUri
                        ?: artistSongs.firstOrNull { it.artUri != null }?.artUri,
                    enabled = artistSongs.isNotEmpty(),
                    onPlay = { onPlayArtist(artistSongs) },
                    onShuffle = { onShuffleArtist(artistSongs) },
                )
            }
            if (topSongs.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ArtistSectionTitleRow(
                            title = rootUiCopy(LocalAppLanguage.current).mostPlayedSongs,
                            subtitle = artistTopTracksSubtitle(topSongs.size, LocalAppLanguage.current),
                            iconResId = R.drawable.ic_lucide_music,
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
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ModuleCard(
                            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 2.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                ArtistSectionTitleRow(
                                    title = commonUiCopy(LocalAppLanguage.current).albums,
                                    subtitle = availableReleasesLabel(artistAlbums.size, LocalAppLanguage.current),
                                    iconResId = R.drawable.ic_lucide_disc_album,
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
        }

        DetailListTopBar(
            title = commonUiCopy(LocalAppLanguage.current).artists,
            subtitle = null,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ArtistHeroHeader(
    artistName: String,
    subtitle: String,
    backdropState: ArtistBackdropState,
    localArtworkUri: Uri?,
    enabled: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val sourceUri = when (backdropState) {
        is ArtistBackdropState.Fallback -> backdropState.artworkUri
        ArtistBackdropState.Loading -> localArtworkUri
    } ?: localArtworkUri
    val backdropImage = rememberArtworkBitmap(sourceUri, size = 1024).value
    val gradient = rememberArtworkGradient(sourceUri).value
    val paletteAccent = rememberAnimatedArtistPaletteAccent(sourceUri)
    val heroHazeState = rememberHazeState()
    var displayedBackdropImage by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(artistName, backdropImage, sourceUri) {
        when {
            backdropImage != null -> displayedBackdropImage = backdropImage
            sourceUri == null -> displayedBackdropImage = null
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(356.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = ElovaireRadii.dialog,
                    bottomEnd = ElovaireRadii.dialog,
                ),
            )
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeSource(heroHazeState, zIndex = -1f),
        ) {
            ArtistHeroBackdrop(
                image = displayedBackdropImage,
                gradient = gradient,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.48f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.84f),
                            ),
                        ),
                    ),
            )
            ArtistHeroAccentGradient(accent = paletteAccent)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = artistName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = elovaireScaledSp(34f),
                    lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.88f,
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumHeaderPlayButton(
                    tint = Color.White,
                    backgroundColor = RoseAccent,
                    onClick = onPlay,
                )
                ArtistShuffleButton(
                    enabled = enabled,
                    hazeState = heroHazeState,
                    onClick = onShuffle,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ArtistHeroBackdrop(
    image: ImageBitmap?,
    gradient: List<Color>,
) {
    AnimatedContent(
        targetState = image,
        transitionSpec = {
            fadeIn(animationSpec = ElovaireMotion.fadeMedium()) togetherWith
                fadeOut(animationSpec = ElovaireMotion.contentFadeOutSpec())
        },
        label = "artist_splash_artwork",
    ) { backdrop ->
        if (backdrop != null) {
            Image(
                bitmap = backdrop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.92f,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                gradient.first().copy(alpha = 0.96f),
                                MaterialTheme.colorScheme.background,
                                gradient.last().copy(alpha = 0.84f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun rememberAnimatedArtistPaletteAccent(uri: Uri?): Color {
    val paletteAccent = rememberArtworkPaletteAccent(uri).value ?: MaterialTheme.colorScheme.primary
    return animateColorAsState(
        targetValue = paletteAccent,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "artist_splash_palette_accent",
    ).value
}

@Composable
internal fun BoxScope.ArtistHeroAccentGradient(accent: Color) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.56f to accent.copy(alpha = 0.18f),
                        1f to accent.copy(alpha = 0.8f),
                    ),
                ),
            ),
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun ArtistShuffleButton(
    enabled: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(hazeState) {
                        blurRadius = 20.dp
                    },
            )
        }
        AlbumHeaderActionButton(
            iconResId = R.drawable.ic_lucide_shuffle,
            contentDescription = "Shuffle artist",
            tint = Color.White,
            backgroundColor = Color.White.copy(alpha = if (enabled) 0.18f else 0.08f),
            iconSize = 18.dp,
            onClick = onClick,
        )
    }
}

@Composable
private fun ArtistSectionTitleRow(
    title: String,
    subtitle: String,
    @DrawableRes iconResId: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = readableMutedIconColor(),
            modifier = Modifier.size(18.dp),
        )
        SectionTitleRow(
            title = title,
            subtitle = subtitle,
            compact = true,
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

internal fun Song.libraryArtistName(): String {
    return albumArtist?.takeIf { it.isNotBlank() } ?: artist.ifBlank { "Unknown Artist" }
}

@Composable
internal fun ArtistAlbumGallery(
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
            modifier = Modifier
                .padding(top = 2.dp)
                .height(26.dp),
        )
    }
}

@Composable
internal fun FavoriteAlbumsModule(
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
    val sharedSourceToken = remember { AlbumSharedTransitionToken.next() }
    val sharedTransitionController = LocalAlbumSharedTransitionController.current

    Surface(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() },
        shape = RoundedCornerShape(6.dp),
        color = cellColor,
        onClick = {
            sharedTransitionController?.select(album.id, sharedSourceToken)
            onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx))
        },
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
                modifier = Modifier
                    .size(48.dp)
                    .albumSharedArtwork(
                        albumId = album.id,
                        sourceToken = sharedSourceToken,
                    ),
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
internal fun ArtistRow(
    artist: ArtistEntry,
    artistImageRepository: ArtistImageReader? = null,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val resolvedArtworkUri = rememberArtistArtworkUri(artist, artistImageRepository)
    val resolvedArtworkLoaded = rememberArtworkBitmap(resolvedArtworkUri, size = 256).value != null
    val artworkUri = if (resolvedArtworkLoaded) resolvedArtworkUri else artist.artUri
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = artworkUri,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "artist_artwork",
        ) { uri ->
            ArtworkImage(
                uri = uri,
                title = artist.name,
                modifier = Modifier.size(50.dp),
                cornerRadius = ElovaireRadii.pill,
                requestedSizePx = 256,
            )
        }
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
private fun rememberArtistArtworkUri(
    artist: ArtistEntry,
    artistImageRepository: ArtistImageReader?,
): Uri? {
    if (artistImageRepository == null) return artist.artUri
    val state by remember(artist.name, artist.artUri, artistImageRepository) {
        artistImageRepository.imageState(artist.name, artist.artUri)
    }.collectAsStateWithLifecycle(
        initialValue = ArtistBackdropState.Fallback(
            localArtworkUri = artist.artUri,
            artistKey = artist.name,
        ),
    )
    return (state as? ArtistBackdropState.Fallback)?.artworkUri ?: artist.artUri
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
internal fun ExplicitTitleText(
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
internal fun HomeRecentSongRow(
    song: Song,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isPlaybackActive: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    Column(modifier = modifier) {
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
                    enter = fadeIn(animationSpec = motionSpecs.tween(60)),
                    exit = fadeOut(animationSpec = motionSpecs.tween(60)),
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
        items(
            items = albums,
            key = { it.id },
            contentType = { "album-grid-card" },
        ) { album ->
            AlbumGridCard(
                album = album,
                modifier = Modifier.width(164.dp),
                onOpen = { origin -> onAlbumSelected(album, origin) },
            )
        }
    }
}

@Composable
internal fun SelectionIndicatorIcon(
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
    enableSharedTransition: Boolean = true,
    onOpen: (ExpandOrigin) -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    val screenSizePx = screenContainerSizePx()
    val screenWidthPx = screenSizePx.width.toFloat()
    val screenHeightPx = screenSizePx.height.toFloat()
    val language = LocalAppLanguage.current
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val sharedSourceToken = remember { AlbumSharedTransitionToken.next() }
    val sharedTransitionController = LocalAlbumSharedTransitionController.current

    Column(
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (enableSharedTransition) {
                        sharedTransitionController?.select(album.id, sharedSourceToken)
                    }
                    onOpen(bounds.toExpandOrigin(screenWidthPx, screenHeightPx))
                },
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
                modifier = Modifier
                    .matchParentSize()
                    .albumSharedArtwork(
                        albumId = album.id,
                        sourceToken = sharedSourceToken,
                        enabled = enableSharedTransition,
                    ),
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
internal fun CompactAlbumRow(
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
    onAddToPlaylist: ((Long) -> PlaylistMutationRequest)? = null,
    onCreatePlaylist: PlaylistCreateAction? = null,
    onDeleteAlbum: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {
    val screenSizePx = screenContainerSizePx()
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val sharedSourceToken = remember { AlbumSharedTransitionToken.next() }
    val sharedTransitionController = LocalAlbumSharedTransitionController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    sharedTransitionController?.takeIf { !selectionMode }?.select(album.id, sharedSourceToken)
                    onOpen(bounds.toExpandOrigin(screenSizePx.width.toFloat(), screenSizePx.height.toFloat()))
                },
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
            ArtworkImage(
                uri = album.artUri,
                title = album.title,
                modifier = Modifier
                    .size(62.dp)
                    .albumSharedArtwork(
                        albumId = album.id,
                        sourceToken = sharedSourceToken,
                    ),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
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
                            append(localizedCountLabel(album.songCount, "track", LocalAppLanguage.current))
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
