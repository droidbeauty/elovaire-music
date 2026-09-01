package elovaire.music.droidbeauty.app.ui.screens
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
    onArtistSelected: (String) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
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
    val selectionHazeState = rememberHazeState()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(selectionHazeState, zIndex = -1f),
        ) {
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .albumSharedArtwork(
                                        albumId = album.id,
                                        destination = true,
                                    ),
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
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onArtistSelected(album.artist.ifBlank { "Unknown Artist" }) },
                            ),
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
                                motionTransitions = motionTransitions,
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
            FastScrollbar(
                state = listState,
                topInset = detailTopPadding + 16.dp,
                bottomInset = bottomPadding + 16.dp,
            )
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
                hazeState = selectionHazeState,
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
                val result = onAddSongsToPlaylist(playlistId, selectedSongs.map(Song::id)).await()
                if (result is PlaylistMutationResult.Success) {
                    selectedSongIds = emptySet()
                    showPlaylistPicker = false
                }
                result
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
            text = "${discLabel(LocalAppLanguage.current)} $discNumber",
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
    val baseHeights = remember { floatArrayOf(0.26f, 0.54f, 0.84f, 0.42f, 0.68f, 0.3f) }
    val phaseOffsets = remember { floatArrayOf(0f, 0.17f, 0.31f, 0.48f, 0.67f, 0.83f) }
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
    val artworkKey = remember(uri, title) { PlaybackArtworkKey(uri, title) }
    var entered by remember(artworkKey) { mutableStateOf(false) }
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
    LaunchedEffect(artworkKey) {
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

private data class PlaybackArtworkKey(
    val uri: Uri?,
    val title: String,
)

@Composable
private fun AlbumSongRow(
    motionTransitions: MotionTransitions,
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
                        transitionSpec = { motionTransitions.quickContentSwapTransform() },
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
    val selectedSongIdsState = rememberSaveable { mutableStateOf(listOf<Long>()) }
    val selectedSongIds = selectedSongIdsState.value
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
    val toggleSongSelection: (Long) -> Unit = { songId ->
        val current = selectedSongIdsState.value
        selectedSongIdsState.value = if (songId in current) {
            current.filterNot { it == songId }
        } else {
            current + songId
        }
    }
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
                        val tabInteractionSource = rememberElovaireInteractionSource()
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .elovaireActionBump(
                                    interactionSource = tabInteractionSource,
                                    label = "playlist_picker_${tab.name}_bump",
                                )
                                .clickable(
                                    interactionSource = tabInteractionSource,
                                    indication = null,
                                ) {
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
                                        onClick = { toggleSongSelection(song.id) },
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
                                        onClick = { toggleSongSelection(song.id) },
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
                                            val current = selectedSongIdsState.value
                                            val allSelected = albumSongIds.all { it in current }
                                            selectedSongIdsState.value = if (allSelected) {
                                                current.filterNot { it in albumSongIds }
                                            } else {
                                                (current + albumSongIds).distinct()
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
                                        onClick = { toggleSongSelection(song.id) },
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
            sharedTopBarPriority = 1,
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
    val selectionInteractionSource = rememberElovaireInteractionSource()
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .elovaireActionBump(
                                interactionSource = selectionInteractionSource,
                                label = "song_selection_indicator_bump",
                            )
                            .clickable(
                                interactionSource = selectionInteractionSource,
                                indication = null,
                                onClick = onClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
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
    val language = LocalAppLanguage.current
    val selectionInteractionSource = rememberElovaireInteractionSource()
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
                            append(formatCountLabel(album.songCount, "track", language))
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
                    .elovaireActionBump(
                        interactionSource = selectionInteractionSource,
                        label = "album_selection_indicator_bump",
                    )
                    .clickable(
                        interactionSource = selectionInteractionSource,
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
