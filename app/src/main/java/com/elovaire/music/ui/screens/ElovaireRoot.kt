package elovaire.music.droidbeauty.app.ui.screens

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
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLine
import elovaire.music.droidbeauty.app.data.lyrics.LyricsLookupMode
import elovaire.music.droidbeauty.app.data.lyrics.LyricsPayload
import elovaire.music.droidbeauty.app.data.lyrics.LyricsResult
import elovaire.music.droidbeauty.app.data.lyrics.toEmbeddedLyricsText
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspConfig
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspModel
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
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
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.searchAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchSongsForPicker
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.interaction.CompactBarGestureActions
import elovaire.music.droidbeauty.app.ui.interaction.compactBarGestures
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.LocalMotionRuntime
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState
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
import elovaire.music.droidbeauty.app.ui.i18n.sleepTimerEndOfSongLabel
import elovaire.music.droidbeauty.app.ui.i18n.sleepTimerOffLabel
import elovaire.music.droidbeauty.app.ui.i18n.sleepTimerTitle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal val aboutLogoImageCache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

private fun Set<Long>.toggleSelection(id: Long): Set<Long> {
    return if (id in this) this - id else this + id
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun ElovaireRoot(
    container: AppContainer,
    resetHomeScrollOnColdStart: Boolean = false,
    adaptiveInfo: ElovaireAdaptiveInfo = elovaireAdaptiveInfo(width = 0.dp),
) {
    val navController = rememberNavController()
    val rootMotionTransitions = rememberMotionTransitions()
    val context = LocalContext.current
    val viewModelFactory = remember(container) { ElovaireViewModelFactory(container.viewModelDependencies) }
    val rootViewModel: RootViewModel = viewModel(factory = viewModelFactory)
    val appState by rootViewModel.appState.collectAsStateWithLifecycle()
    val derivedState = rememberRootLibraryDerivedState(
        library = appState.library,
        playback = appState.playback,
        playlists = appState.playlists,
        songPlayCounts = appState.songPlayCounts,
    )
    val permissionController = rememberRootPermissionController(
        container = container,
        libraryState = appState.library,
    )
    val deleteController = rememberRootDeleteController(container)
    RootUpdateTransientStatusEffect(
        enabled = true,
        transientStatus = appState.appUpdateState.transientStatus,
        clearTransientStatus = container.appUpdateManager::clearTransientStatus,
    )
    val albumCollectionLayoutMode = appState.albumCollectionLayoutModeName.toAlbumLayoutMode()
    val changelogReleases = rememberChangelogReleases(context)
    val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val nowPlayingViewModel: NowPlayingViewModel = viewModel(factory = viewModelFactory)
    val libraryState = appState.library
    val playbackState = appState.playback
    val songsById = derivedState.songsById
    val songsByAlbumId = derivedState.songsByAlbumId
    val albumsById = derivedState.albumsById
    val playlistsById = derivedState.playlistsById
    val recentlyAddedAlbums = derivedState.recentlyAddedAlbums
    val recentAlbums = derivedState.recentAlbums
    val favoriteAlbums = derivedState.favoriteAlbums
    val lastPlayedAlbum = derivedState.lastPlayedAlbum
    val lastPlayedPlaylist = derivedState.lastPlayedPlaylist

    if (!permissionController.state.hasAudioPermission) {
        PerformanceState("screen", "permission")
        FirstLaunchPermissionLoadingScreen(
            showLoading = false,
            onRequestPermission = permissionController::requestAudioPermission,
        )
        return
    }

    val isPlaybackActuallyPlaying = playbackState.isPlaying && playbackState.currentSong != null
    LaunchedEffect(
        isPlaybackActuallyPlaying,
        permissionController.state.hasNotificationPermission,
    ) {
        if (
            isPlaybackActuallyPlaying &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !permissionController.state.hasNotificationPermission
        ) {
            permissionController.requestNotificationPermission()
        }
    }

    val topLevelDestinations = DefaultTopLevelDestinations

    val navigationState = rememberRootNavigationState(navController)
    val routeObservation = rememberRootRouteObservation(
        navController = navController,
        navigationState = navigationState,
        libraryState = libraryState,
        isPlaybackActuallyPlaying = isPlaybackActuallyPlaying,
    )
    val currentRoute = routeObservation.route
    val currentAlbumRouteId = routeObservation.currentAlbumRouteId
    val currentSongPresent = playbackState.currentSong != null
    val playerLayerController = rememberRootPlayerLayerController(
        currentSongId = playbackState.currentSong?.id,
        currentSongPresent = currentSongPresent,
    )
    val playerLayerState = playerLayerController.state
    val searchChromeState = rememberRootSearchChromeState()
    val openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit = { album, origin, source ->
        navigationState.openAlbum(album, origin, source)
    }
    val activeBottomRoute = routeObservation.activeBottomRoute
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val chromeVisibility = rootChromeVisibility(
        currentRoute = currentRoute,
        keyboardVisible = keyboardVisible,
        searchQueryActive = searchChromeState.isQueryActive,
        currentSongPresent = currentSongPresent,
        playerLayerState = playerLayerState,
    )
    val overscrollFactory = rememberElovaireOverscrollFactory()
    val navHostBlur = 0.dp
    val navHostScrimAlpha = 0f
    val appBackground = MaterialTheme.colorScheme.background
    val darkTheme = appBackground.luminance() < 0.5f
    val chromeHazeState = rememberHazeState()
    val sharedTopBarController = remember { SharedTopBarController() }
    val sharedBackIconPainter = painterResource(id = R.drawable.ic_lucide_chevron_left)
    val sharedTopMenuIconPainter = painterResource(id = R.drawable.ic_lucide_menu)
    val overlayState = rememberRootOverlayStateHolder(currentRoute)
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
    val openCurrentPlayingAlbum: (Long) -> Unit = { albumId ->
        val sameAlbumAlreadyVisible =
            currentRoute == "$ALBUM_ROUTE/{albumId}" && currentAlbumRouteId == albumId
        playerLayerController.hide(false)
        if (!sameAlbumAlreadyVisible) {
            albumsById[albumId]?.let { album ->
                openAlbum(album, ExpandOrigin(), AlbumOpenSource.Player)
            } ?: run {
                navigationState.detailExpandOrigin = ExpandOrigin()
                navigationState.detailRouteTransitionMode = DetailRouteTransitionMode.TileExpand
                navController.navigate(Routes.album(albumId))
            }
        }
    }
    val topBarMenuActions = rememberRootTopBarMenuActions(navController, overlayState)
    val sharedTopBarSpec = sharedTopBarController.registration?.spec
        ?: rootSharedTopBarSpec(
            currentRoute = currentRoute,
            showTopLevelChrome = chromeVisibility.showTopLevelChrome,
            language = appState.appLanguage,
            onRequestCreatePlaylist = overlayState::requestCreatePlaylist,
            onOpenMenu = overlayState::openTopBarMenu,
        )
    val currentPlayerLayerController by rememberUpdatedState(playerLayerController)
    LaunchedEffect(container) {
        container.openNowPlayingCommands.collect {
            currentPlayerLayerController.requestOpen(null)
        }
    }
    RootSystemBarEffect(
        darkTheme = darkTheme,
        showPlayerOverlay = chromeVisibility.showPlayerOverlay,
        playerContentColor = playerAdaptivePalette.contentColor,
    )

    val songMenuActions = rememberRootSongMenuActions(
        playlists = appState.playlists,
        songsById = songsById,
        albumsById = albumsById,
        playbackManager = container.playbackManager,
        preferenceStore = container.preferenceStore,
        onDeleteSongsFromDevice = deleteController::deleteSongsFromDevice,
        openAlbum = openAlbum,
        navigateToAlbumId = { albumId -> navController.navigate(Routes.album(albumId)) },
    )
    val playbackActions = rememberRootPlaybackActions(
        container = container,
        appLanguage = appState.appLanguage,
        songsByAlbumId = songsByAlbumId,
        albumsById = albumsById,
        openNowPlaying = playerLayerController::requestOpen,
    )
    val playlistActions = rememberRootPlaylistActions(container)
    val routeState = rootRouteStateOf(
        appState = appState,
        derivedState = derivedState,
        albumCollectionLayoutMode = albumCollectionLayoutMode,
        resetHomeScrollOnColdStart = resetHomeScrollOnColdStart,
        playFirstLaunchHomeReveal = permissionController.state.playFirstLaunchHomeReveal,
        searchFieldFocused = searchChromeState.isFieldFocused,
    )
    val routeActions = rememberRootRouteActions(
        context = context,
        container = container,
        navController = navController,
        navigationState = navigationState,
        playbackActions = playbackActions,
        playlistActions = playlistActions,
        deleteController = deleteController,
        onRequestCreatePlaylist = overlayState::requestCreatePlaylist,
        onInitialRevealFinished = permissionController::onInitialRevealFinished,
        onSearchFieldFocusedChange = searchChromeState::onFieldFocusedChanged,
        onSearchQueryActiveChanged = searchChromeState::onQueryActiveChanged,
        openAlbum = openAlbum,
    )

    ElovaireRootShell(
        overscrollFactory = overscrollFactory,
        songMenuActions = songMenuActions,
        chromeHazeState = chromeHazeState,
        sharedBackIconPainter = sharedBackIconPainter,
        sharedTopMenuIconPainter = sharedTopMenuIconPainter,
        appLanguage = appState.appLanguage,
        adaptiveInfo = adaptiveInfo,
        chromeVisibility = chromeVisibility,
        sharedTopBarController = sharedTopBarController,
        navHostBlur = navHostBlur,
        navHostScrimAlpha = navHostScrimAlpha,
        routeHost = { routePadding, modifier ->
            RootRouteGraph(
                navState = navigationState,
                routeState = routeState,
                routeActions = routeActions,
                padding = routePadding,
                searchViewModel = searchViewModel,
                viewModelFactory = viewModelFactory,
                changelogReleases = changelogReleases,
                modifier = modifier,
            )
        },
        chromeHost = { layout ->
            RootChromeHost(
                sharedTopBarSpec = sharedTopBarSpec,
                showSharedTopBarBackdrop = chromeVisibility.showSharedTopBarBackdrop,
                sharedTopBarHeight = layout.sharedTopBarHeight,
                canHostCompactNowPlaying = chromeVisibility.canHostCompactNowPlaying,
                playbackState = playbackState,
                nowPlayingViewModel = nowPlayingViewModel,
                showGlobalNowPlaying = chromeVisibility.showGlobalNowPlaying,
                reenteringFromPlayer = chromeVisibility.reenteringFromPlayer,
                showBottomNavigation = chromeVisibility.showBottomNavigation,
                adaptiveInfo = adaptiveInfo,
                bottomNavHeight = layout.bottomNavHeight,
                activeBottomRoute = activeBottomRoute,
                currentRoute = currentRoute,
                navigationState = navigationState,
                topLevelDestinations = topLevelDestinations,
                motionTransitions = rootMotionTransitions,
                onOpenPlayer = playerLayerController::requestOpen,
            )
        },
        overlayHost = { layout ->
            RootOverlayHost(
                showTopBarMenu = overlayState.showTopBarMenu,
                onDismissTopBarMenu = overlayState::dismissTopBarMenu,
                onOpenSettings = topBarMenuActions.openSettings,
                onOpenEqualizer = topBarMenuActions.openEqualizer,
                onOpenChangelog = topBarMenuActions.openChangelogSheet,
                onOpenAbout = topBarMenuActions.openAbout,
                showChangelogSheet = overlayState.showChangelogSheet,
                changelogReleases = changelogReleases,
                onDismissChangelogSheet = overlayState::dismissChangelogSheet,
                showPlaylistCreateDialog = overlayState.showPlaylistCreateDialog,
                onDismissPlaylistCreateDialog = overlayState::dismissPlaylistCreateDialog,
                onCreatePlaylist = playlistActions::createPlaylist,
                showTopLevelChrome = chromeVisibility.showTopLevelChrome,
                currentRoute = currentRoute,
                topBarHeight = layout.topBarHeight,
                appUpdateState = appState.appUpdateState,
                onDismissUpdate = container.appUpdateManager::dismissAvailableUpdate,
                onStartUpdate = container.appUpdateManager::startUpdate,
                permissionState = permissionController.state,
                onRequestAudioPermission = permissionController::requestAudioPermission,
                motionTransitions = rootMotionTransitions,
            )
        },
        playerLayerHost = {
            RootPlayerLayerHost(
                visible = chromeVisibility.showPlayerOverlay,
                playerLayerState = playerLayerState,
                onExitFinished = playerLayerController::clearTransitionSnapshot,
                onReturnToCompactFinished = playerLayerController::finishReturnToCompact,
                nowPlayingViewModel = nowPlayingViewModel,
                playbackManager = container.playbackManager,
                songsById = songsById,
                isCurrentSongFavorite = playbackState.currentSong?.id in appState.favoriteSongIds,
                playlists = appState.playlists.filterNot { it.isSystem },
                onBack = { playerLayerController.hide(true) },
                onOpenCurrentAlbum = openCurrentPlayingAlbum,
                onToggleFavorite = playlistActions::toggleFavorite,
                onAddCurrentSongToPlaylist = { playlistId, song ->
                    playlistActions.addSongsToPlaylist(playlistId, listOf(song.id))
                },
                onCreatePlaylist = playlistActions::createPlaylist,
                onOpenEqualizer = {
                    playerLayerController.hide(false)
                    navController.navigate(EQUALIZER_ROUTE)
                },
                transitionSnapshot = playerLayerController.transitionSnapshot,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

@Composable
internal fun ForceDarkColorScheme(
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
    onAddAlbumToPlaylist: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
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
    val motionSpecs = rememberMotionSpecs()
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
            enter = fadeIn(animationSpec = motionSpecs.tween(MotionDuration.Quick)) +
                slideInVertically(
                    animationSpec = motionSpecs.tween(MotionDuration.Quick),
                    initialOffsetY = { -it / 4 },
                ),
            exit = fadeOut(animationSpec = motionSpecs.tween(MotionDuration.Quick)) +
                slideOutVertically(
                    animationSpec = motionSpecs.tween(MotionDuration.Quick),
                    targetOffsetY = { -it / 4 },
                ),
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
internal fun LibraryCollectionScreen(
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
                Box(
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
    val motionSpecs = rememberMotionSpecs()
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
            enter = fadeIn(animationSpec = motionSpecs.tween(MotionDuration.Quick)) +
                slideInVertically(
                    animationSpec = motionSpecs.tween(MotionDuration.Quick),
                    initialOffsetY = { -it / 4 },
                ),
            exit = fadeOut(animationSpec = motionSpecs.tween(MotionDuration.Quick)) +
                slideOutVertically(
                    animationSpec = motionSpecs.tween(MotionDuration.Quick),
                    targetOffsetY = { -it / 4 },
                ),
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
    bottomPadding: Dp,
    onBack: () -> Unit,
    onArtistSelected: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val scrollState = rememberElovaireScrollState("artist_collection")
    val artists = remember(songs) {
        songs
            .groupBy { it.libraryArtistName() }
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
internal fun ArtistDetailScreen(
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

private fun Song.libraryArtistName(): String {
    return albumArtist?.takeIf { it.isNotBlank() } ?: artist.ifBlank { "Unknown Artist" }
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
internal fun SearchRoute(
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
    val revealRegistry = rememberMotionRevealRegistry()
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
                                contentType = { _, _ -> "search_song_row" },
                            ) { index, song ->
                                Box(
                                    modifier = Modifier
                                        .animateItem(
                                            placementSpec = ElovaireMotion.listPlacementSpec(),
                                        )
                                        .elovaireListReveal(
                                            itemKey = song.id,
                                            index = index,
                                            registry = revealRegistry,
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
                                                    modifier = Modifier.elovaireListReveal(
                                                        itemKey = song.id,
                                                        index = index,
                                                        registry = revealRegistry,
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
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        },
        animationSpec = motionSpecs.tween(MotionDuration.Quick),
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
internal fun readableMutedIconColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    }
}

@Composable
internal fun readableCardSurfaceColor(): Color {
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
internal fun SectionTitleRow(
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
private fun HomeRecentSongRow(
    song: Song,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isPlaybackActive: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    showDivider: Boolean,
) {
    val motionSpecs = rememberMotionSpecs()
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
internal fun AlbumScreen(
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
    val revealRegistry = rememberMotionRevealRegistry()
    val motionTransitions = rememberMotionTransitions()
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
                            .elovaireListReveal(
                                itemKey = song.id,
                                index = index,
                                registry = revealRegistry,
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
            enter = motionTransitions.verticalRevealEnter(),
            exit = motionTransitions.verticalRevealExit(),
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
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val phase = if (animate && !motionRuntime.reduceMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "audio_lines_phase")
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = motionSpecs.tween(
                    durationMillis = 1_416,
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
internal fun PlaybackActiveArtworkOverlay(
    uri: Uri?,
    title: String,
    modifier: Modifier = Modifier,
) {
    val artworkShape = RoundedCornerShape(ElovaireRadii.artworkSmall)
    val artworkBitmap = rememberArtworkBitmap(uri = uri, size = 256).value
    var entered by remember { mutableStateOf(false) }
    val blurRadius by animateDpAsState(
        targetValue = if (entered) 18.dp else 0.dp,
        animationSpec = ElovaireMotion.sizeSoft(),
        label = "active_artwork_blur_radius",
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = ElovaireMotion.contentFadeInSpec(),
        label = "active_artwork_blur_alpha",
    )
    LaunchedEffect(Unit) {
        entered = true
    }
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
                        alpha = surfaceAlpha
                    }
                    .blur(blurRadius),
            )
        } else {
            ArtworkImage(
                uri = uri,
                title = title,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = surfaceAlpha }
                    .blur(blurRadius),
                cornerRadius = ElovaireRadii.artworkSmall,
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.08f * surfaceAlpha)),
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
internal fun AddSongsToPlaylistOverlay(
    availableSongs: List<Song>,
    existingSongIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddSongs: (List<Long>) -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    var selectedTab by rememberSaveable { mutableStateOf(PlaylistPickerTab.Songs) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSongIds by rememberSaveable { mutableStateOf(listOf<Long>()) }
    var selectedAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedArtistName by rememberSaveable { mutableStateOf<String?>(null) }
    var listResetVersion by remember { mutableLongStateOf(0L) }
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
    val normalizedQuery = remember(query) { NormalizedSearchQuery.from(query) }
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
        candidateSongs.groupBy { it.libraryArtistName() }
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
    val filteredAlbums = remember(albums, normalizedQuery) {
        searchAlbumsForPicker(
            albums = albums,
            query = normalizedQuery,
        )
    }
    val filteredArtists = remember(artists, normalizedQuery) {
        searchArtistsForPicker(
            artists = artists,
            query = normalizedQuery,
            name = { it.first.name },
            songs = { it.second },
            songCount = { it.first.songCount },
        )
    }
    val filteredSongs = remember(candidateSongs, normalizedQuery) {
        searchSongsForPicker(
            songs = candidateSongs,
            query = normalizedQuery,
        )
    }
    val selectedAlbum = remember(selectedAlbumId, albums) {
        albums.firstOrNull { it.id == selectedAlbumId }
    }
    val selectedArtistSongs = remember(selectedArtistName, artists) {
        artists.firstOrNull { it.first.name == selectedArtistName }?.second.orEmpty()
    }
    val filteredAlbumSongs = remember(selectedAlbum, normalizedQuery) {
        searchSongsForPicker(
            songs = selectedAlbum?.songs.orEmpty(),
            query = normalizedQuery,
        )
    }
    val filteredArtistSongs = remember(selectedArtistSongs, normalizedQuery) {
        searchSongsForPicker(
            songs = selectedArtistSongs,
            query = normalizedQuery,
        )
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
                            animationSpec = motionSpecs.tween(
                                durationMillis = 120,
                                easing = LinearOutSlowInEasing,
                            ),
                            initialAlpha = 0.86f,
                        ) + slideInHorizontally(
                            animationSpec = motionSpecs.tween(
                                durationMillis = 160,
                                easing = FastOutSlowInEasing,
                            ),
                            initialOffsetX = { fullWidth ->
                                val offset = (fullWidth / 18f).roundToInt()
                                if (forward) offset else -offset
                            },
                        )) togetherWith (fadeOut(
                            animationSpec = motionSpecs.tween(
                                durationMillis = 50,
                                easing = FastOutLinearInEasing,
                            ),
                            targetAlpha = 0.9f,
                        ) + slideOutHorizontally(
                            animationSpec = motionSpecs.tween(
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
                                items(
                                    items = filteredAlbumSongs,
                                    key = { it.id },
                                    contentType = { "song-row" },
                                ) { song ->
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
                                items(
                                    items = filteredArtistSongs,
                                    key = { it.id },
                                    contentType = { "song-row" },
                                ) { song ->
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
                                items(
                                    items = filteredAlbums,
                                    key = { it.id },
                                    contentType = { "album-row" },
                                ) { album ->
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
                                itemsIndexed(
                                    items = filteredArtists,
                                    key = { _, item -> item.first.name },
                                    contentType = { _, _ -> "artist-row" },
                                ) { index, (artist, _) ->
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
                                items(
                                    items = filteredSongs,
                                    key = { it.id },
                                    contentType = { "song-row" },
                                ) { song ->
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
internal fun NowPlayingScreen(
    playbackManager: PlaybackManager,
    playerUiState: PlayerUiState,
    enrichedSongsById: Map<Long, Song>,
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
    onAddCurrentSongToPlaylist: (Long, Song) -> Unit,
    onCreatePlaylist: (String) -> Long,
    onQueueItemSelected: (Int) -> Unit,
    onQueueItemRemoved: (Int) -> Unit,
    onOpenEqualizer: () -> Unit,
    onToggleGaplessPlayback: () -> Unit,
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
    val expandSettleAnimationSpec = motionSpecs.tween<Float>(
        durationMillis = 360,
        easing = LinearOutSlowInEasing,
    )
    val collapseSettleAnimationSpec = motionSpecs.tween<Float>(
        durationMillis = 360,
        easing = androidx.compose.animation.core.Easing { fraction ->
            1f - LinearOutSlowInEasing.transform(1f - fraction)
        },
    )
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
                animationSpec = motionSpecs.tween(80),
                label = "queue_favorite_alpha",
            )
            val transportAlpha by animateFloatAsState(
                targetValue = if (showQueueSheet) 0f else 1f,
                animationSpec = motionSpecs.tween(80),
                label = "queue_transport_alpha",
            )
            val animatedArtworkCornerRadius by animateDpAsState(
                targetValue = if (showQueueSheet) 10.dp else ElovaireRadii.module,
                animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
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
                            .then(if (showQueueSheet) Modifier else Modifier.nowPlayingDismissGesture()),
                    ) {
                        val expandedArtworkWidth = maxWidth
                        val compactArtworkWidth = maxWidth * 0.38f
                        val animatedArtworkWidth by animateDpAsState(
                            targetValue = if (showQueueSheet) compactArtworkWidth else expandedArtworkWidth,
                            animationSpec = motionSpecs.tween(MotionDuration.Standard, easing = FastOutSlowInEasing),
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
                val metadataClickInteractionSource = remember { MutableInteractionSource() }
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
                    .weight(1f),
            ) {
                val queueSheetTopExtension = 562.dp
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
                    enter = motionTransitions.queueMenuEnter(),
                    exit = motionTransitions.queueMenuExit(),
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
                playbackManager = playbackManager,
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
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                selectedOption = playerUiState.sleepTimer.option,
                onOptionSelected = { option ->
                    onSleepTimerSelected(option)
                    showSleepTimerDialog = false
                },
                onDismiss = { showSleepTimerDialog = false },
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
    sleepTimerActive: Boolean,
    onOpenSleepTimer: () -> Unit,
    onAddSongToPlaylist: (Long, Song) -> Unit,
    onCreatePlaylist: (String) -> Long,
    statusText: String?,
    onDismiss: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val revealRegistry = rememberMotionRevealRegistry()
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
                        iconResId = R.drawable.ic_lucide_moon,
                        label = "",
                        contentDescription = sleepTimerTitle(language),
                        iconSize = 20.dp,
                        tint = tint,
                        showBackground = sleepTimerActive,
                        onClick = onOpenSleepTimer,
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
private fun SleepTimerDialog(
    selectedOption: SleepTimerOption,
    onOptionSelected: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val options = remember {
        listOf(
            SleepTimerOption.Off,
            SleepTimerOption.FifteenMinutes,
            SleepTimerOption.ThirtyMinutes,
            SleepTimerOption.FortyFiveMinutes,
            SleepTimerOption.SixtyMinutes,
            SleepTimerOption.EndOfSong,
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = sleepTimerTitle(language),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val selected = option == selectedOption
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ElovaireRadii.pill))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onOptionSelected(option) },
                            ),
                        shape = RoundedCornerShape(ElovaireRadii.pill),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (selected) R.drawable.ic_lucide_check else R.drawable.ic_lucide_circle,
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 0.95f else 0.5f),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = sleepTimerOptionLabel(option, language),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = rootUiCopy(language).ok)
            }
        },
    )
}

private fun sleepTimerOptionLabel(
    option: SleepTimerOption,
    language: AppLanguage,
): String {
    return when (option) {
        SleepTimerOption.Off -> sleepTimerOffLabel(language)
        SleepTimerOption.FifteenMinutes -> "15 min"
        SleepTimerOption.ThirtyMinutes -> "30 min"
        SleepTimerOption.FortyFiveMinutes -> "45 min"
        SleepTimerOption.SixtyMinutes -> "60 min"
        SleepTimerOption.EndOfSong -> sleepTimerEndOfSongLabel(language)
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
    val motionRuntime = LocalMotionRuntime.current
    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        } else if (shouldRenderMenu) {
            delay(motionRuntime.duration(180L))
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
    val motionRuntime = LocalMotionRuntime.current
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(motionRuntime.duration(180L))
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
internal fun AlbumHeaderActionButton(
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
internal fun AlbumHeaderPlayButton(
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
internal fun InlineFavoriteSongButton(
    isFavorite: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionRuntime = LocalMotionRuntime.current
    var previousFavoriteState by remember { mutableStateOf(isFavorite) }
    var shouldBounce by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        if (previousFavoriteState != isFavorite) {
            shouldBounce = true
            delay(motionRuntime.duration(180L))
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
    val motionTransitions = rememberMotionTransitions()
    Popup(
        popupPositionProvider = OverflowContextMenuPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = motionTransitions.contextMenuEnter(),
            exit = motionTransitions.contextMenuExit(),
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
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = motionSpecs.spring(
            dampingRatio = 0.6f,
            stiffness = 380f,
        ),
        label = "album_overflow_scale",
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        } else if (shouldRenderMenu) {
            delay(motionRuntime.duration(180L))
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
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = motionSpecs.spring(
            dampingRatio = 0.6f,
            stiffness = 380f,
        ),
        label = "song_overflow_scale",
    )
    LaunchedEffect(expanded) {
        if (expanded) {
            shouldRenderMenu = true
        } else if (shouldRenderMenu) {
            delay(motionRuntime.duration(180L))
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
        ElovaireAnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 10.dp),
            enter = motionTransitions.contextMenuEnter(),
            exit = motionTransitions.contextMenuExit(),
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
private fun LyricsOverlay(
    song: Song?,
    lyricsUiState: LyricsUiState,
    lyricsEditorUiState: LyricsEditorUiState,
    activeLyricsLineIndex: Int,
    playbackManager: PlaybackManager,
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
    val playbackProgress = rememberRenderedPlaybackProgress(
        playbackManager = playbackManager,
        currentSongId = song?.id,
        freezeUpdates = false,
    )
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
                enter = motionTransitions.standardEnter(),
                exit = motionTransitions.standardExit(),
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
    val motionSpecs = rememberMotionSpecs()
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
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showBackground || transientHighlight) 0.2f else 0f,
        animationSpec = motionSpecs.tween(MotionDuration.Standard),
        label = "${label}_button_alpha",
    )
    val buttonScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.9f
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
