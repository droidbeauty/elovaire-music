package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.screens.common.secondaryBodyTextStyle
import elovaire.music.droidbeauty.app.ui.screens.common.readableMutedIconColor

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
import elovaire.music.droidbeauty.app.domain.model.Audiobook
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
import elovaire.music.droidbeauty.app.ui.i18n.audiobookCopy
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
internal fun SearchRoute(
    viewModel: SearchViewModel,
    libraryState: LibraryUiState,
    playlists: List<Playlist>,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    onSearchActiveChanged: (Boolean) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onArtistSelected: (String) -> Unit,
    onAudiobookSelected: (Audiobook) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        libraryState = libraryState,
        playlists = playlists,
        state = state,
        favoriteSongIds = favoriteSongIds,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        scrollToTopRequestVersion = scrollToTopRequestVersion,
        onQueryChange = viewModel::onQueryChange,
        onShowAllSongResultsChange = viewModel::onShowAllSongResultsChange,
        onSearchSongSortModeChange = viewModel::onSearchSongSortModeChange,
        onShowSearchSongSortOptionsChange = viewModel::onShowSearchSongSortOptionsChange,
        onSearchActiveChanged = onSearchActiveChanged,
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
        onAudiobookSelected = onAudiobookSelected,
        onPlaylistSelected = onPlaylistSelected,
        onToggleFavorite = onToggleFavorite,
        onClearSearchHistory = viewModel::clearSearchHistory,
        onClearQuery = viewModel::clearQuery,
        onResetSearchUi = viewModel::resetSearchUi,
    )
}

@Composable
private fun SearchScreen(
    libraryState: LibraryUiState,
    playlists: List<Playlist>,
    state: SearchUiState,
    favoriteSongIds: Set<Long>,
    topPadding: Dp,
    bottomPadding: Dp,
    scrollToTopRequestVersion: Long,
    onQueryChange: (String) -> Unit,
    onShowAllSongResultsChange: (Boolean) -> Unit,
    onSearchSongSortModeChange: (SearchSongSortMode) -> Unit,
    onShowSearchSongSortOptionsChange: (Boolean) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onAlbumSelected: (Album, ExpandOrigin, Boolean) -> Unit,
    onArtistSelected: (String) -> Unit,
    onAudiobookSelected: (Audiobook) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onClearSearchHistory: () -> Unit,
    onClearQuery: () -> Unit,
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
    val browsingContentMode = if (state.contentMode == SearchContentMode.AllSongs) {
        SearchContentMode.Results
    } else {
        state.contentMode
    }
    val allSongsListState = rememberElovaireLazyListState("search_all_songs", trimmedQuery, state.searchSongSortMode)
    var isFieldFocused by remember { mutableStateOf(false) }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val isSearchUiActive = trimmedQuery.isNotBlank() || isFieldFocused || state.showAllSongResults
    val collapseAllSongResults: () -> Unit = {
        onShowAllSongResultsChange(false)
    }
    val dismissSearchInput: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    val resetSearchToDiscover: () -> Unit = {
        onResetSearchUi()
        dismissSearchInput()
    }
    val selectSong: (Song, List<Song>) -> Unit = { song, queue ->
        dismissSearchInput()
        onSongSelected(song, queue)
    }
    val selectAlbum: (Album, ExpandOrigin, Boolean) -> Unit = { album, origin, rememberSearch ->
        dismissSearchInput()
        onAlbumSelected(album, origin, rememberSearch)
    }
    val selectArtist: (String) -> Unit = { artist ->
        dismissSearchInput()
        onArtistSelected(artist)
    }
    val selectPlaylist: (Playlist) -> Unit = { playlist ->
        dismissSearchInput()
        onPlaylistSelected(playlist)
    }
    if (state.showAllSongResults && trimmedQuery.isNotBlank()) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Back(
                title = commonUiCopy(language).search,
                onBack = collapseAllSongResults,
                centeredTitle = false,
            ),
            priority = 10,
        )
    }
    BackHandler(
        enabled = state.showSearchSongSortOptions ||
            state.showAllSongResults ||
            (trimmedQuery.isNotBlank() && !imeVisible),
    ) {
        when {
            state.showSearchSongSortOptions -> onShowSearchSongSortOptionsChange(false)
            state.showAllSongResults && trimmedQuery.isNotBlank() -> collapseAllSongResults()
            else -> resetSearchToDiscover()
        }
    }
    LaunchedEffect(isSearchUiActive) {
        onSearchActiveChanged(isSearchUiActive)
    }
    LaunchedEffect(scrollToTopRequestVersion, state.contentMode) {
        if (scrollToTopRequestVersion > 0L && state.contentMode == SearchContentMode.AllSongs) {
            allSongsListState.animateScrollToItem(0)
        }
    }
    val matchingPlaylists = remember(playlists, state.resultQuery) {
        if (state.resultQuery.trim().isBlank()) {
            emptyList()
        } else {
            searchPlaylists(
                playlists = playlists.filterNot(Playlist::isSystem),
                rawQuery = state.resultQuery,
            ).take(6)
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

    val hasSearchResults = state.matchingAlbums.isNotEmpty() ||
        state.matchingSongs.isNotEmpty() ||
        state.matchingAudiobooks.isNotEmpty() ||
        matchingArtists.isNotEmpty() ||
        matchingPlaylists.isNotEmpty()

    val searchBar: @Composable () -> Unit = {
        val searchBarContentColor = MaterialTheme.colorScheme.onSurface
        OutlinedTextField(
            value = state.query,
            onValueChange = {
                onQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFieldFocused = focusState.isFocused
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            enabled = trimmedQuery.isNotBlank(),
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClearQuery,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (trimmedQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(searchBarContentColor.copy(alpha = 0.1f)),
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
        ElovaireAnimatedContent(
            targetState = state.contentMode == SearchContentMode.AllSongs,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                when {
                    !initialState && targetState -> {
                        ElovaireMotion.fullScreenForwardEnter(
                            initialOffsetX = { it / 10 },
                        ) togetherWith ElovaireMotion.fullScreenForwardExit()
                    }

                    initialState && !targetState -> {
                        ElovaireMotion.fullScreenBackEnter() togetherWith ElovaireMotion.fullScreenBackExit(
                            targetOffsetX = { it / 10 },
                        )
                    }

                    else -> ElovaireMotion.softContentTransform()
                }
            },
            label = "SearchScreenContent",
        ) { showAllSongs ->
            val isActiveContent = transition.targetState == EnterExitState.Visible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (isActiveContent) 1f else 0f)
                    .then(
                        if (isActiveContent) {
                            Modifier
                        } else {
                            Modifier.pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    down.consume()
                                    do {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { change -> change.consume() }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                        },
                    ),
            ) {
                if (showAllSongs) {
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
                SearchSongsResultsHeader(
                    resultCount = state.totalSongMatchCount,
                    selected = state.searchSongSortMode,
                    expanded = state.showSearchSongSortOptions,
                    onToggleExpanded = {
                        onShowSearchSongSortOptionsChange(!state.showSearchSongSortOptions)
                    },
                    onSelect = { selectedMode ->
                        onSearchSongSortModeChange(selectedMode)
                    },
                )
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = allSongsListState,
                        overscrollEffect = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .ensureSingleItemRubberBand(allSongsListState),
                        contentPadding = PaddingValues(bottom = bottomPadding + 20.dp),
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
                                HomeRecentSongRow(
                                    song = song,
                                    isFavorite = song.id in favoriteSongIds,
                                    isCurrentSong = song.id == state.currentSongId,
                                    isPlaybackActive = state.isPlaybackActive,
                                    onClick = {
                                        selectSong(song, state.allMatchingSongs)
                                    },
                                    onToggleFavorite = { onToggleFavorite(song.id) },
                                    showDivider = index != state.allMatchingSongs.lastIndex,
                                )
                            }
                        }
                    }
                    FastScrollbar(
                        state = allSongsListState,
                        topInset = 8.dp,
                        bottomInset = bottomPadding + 20.dp,
                        trackGesturesEnabled = false,
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
                    bottom = bottomPadding + if (isSearchUiActive) 20.dp else 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item(
                    key = "search_input",
                    contentType = "search_input",
                ) {
                    searchBar()
                }
                when (browsingContentMode) {
                    SearchContentMode.AllSongs -> Unit

                    SearchContentMode.Discover -> {
                        if (state.recentSearches.isNotEmpty()) {
                            item(
                                key = "search_recent",
                                contentType = "search_history",
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                    SearchHistorySectionHeader(
                                        showClearAction = true,
                                        onClearHistory = onClearSearchHistory,
                                    )
                                    SearchHistoryListCard(
                                        entries = state.recentSearches.take(6),
                                        onAlbumSelected = { albumId ->
                                            libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                                selectAlbum(album, ExpandOrigin(), false)
                                            }
                                        },
                                        onArtistSelected = selectArtist,
                                    )
                                }
                            }
                        } else {
                            item(
                                key = "search_nothing_searched",
                                contentType = "search_empty_state",
                            ) {
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
                        }
                        if (state.suggestedAlbums.isNotEmpty()) {
                            item(
                                key = "search_suggested_albums",
                                contentType = "search_album_module",
                            ) {
                                FavoriteAlbumsModule(
                                    albums = state.suggestedAlbums,
                                    title = searchCopy(language).suggestedAlbumsTitle,
                                    subtitle = searchCopy(language).suggestedAlbumsSubtitle,
                                    iconResId = R.drawable.ic_lucide_eye,
                                    onAlbumSelected = { album, origin ->
                                        selectAlbum(album, origin, false)
                                    },
                                )
                            }
                        }
                    }

                    SearchContentMode.Results -> {
                        if (matchingArtists.isNotEmpty()) {
                            item(
                                key = "search_artists",
                                contentType = "search_history",
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                    SearchResultsCategoryHeader(
                                        title = commonUiCopy(language).artists,
                                        subtitle = searchCopy(language).matchingArtists(matchingArtists.size),
                                        iconResId = R.drawable.ic_lucide_mic_vocal,
                                    )
                                    SearchHistoryListCard(
                                        entries = matchingArtists,
                                        onAlbumSelected = { albumId ->
                                            libraryState.albums.firstOrNull { it.id == albumId }?.let { album ->
                                                selectAlbum(album, ExpandOrigin(), false)
                                            }
                                        },
                                        onArtistSelected = selectArtist,
                                    )
                                }
                            }
                        }

                        if (state.matchingAlbums.isNotEmpty()) {
                            item(
                                key = "search_albums",
                                contentType = "search_album_module",
                            ) {
                                ModuleCard(
                                    contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 2.dp),
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        SearchResultsCategoryHeader(
                                            title = commonUiCopy(language).albums,
                                            subtitle = copy.matchingAlbums(state.matchingAlbums.size),
                                            iconResId = R.drawable.ic_lucide_disc_album,
                                        )
                                        ArtistAlbumGallery(
                                            albums = state.matchingAlbums,
                                            onAlbumSelected = { album, origin ->
                                                selectAlbum(album, origin, true)
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        if (state.matchingSongs.isNotEmpty()) {
                            item(
                                key = "search_songs",
                                contentType = "search_song_results",
                            ) {
                                val previewSongs = state.matchingSongs.take(10)
                                Column {
                                    SearchSongsPreviewHeader(
                                        resultCount = state.totalSongMatchCount,
                                        showSeeAll = state.totalSongMatchCount > 10,
                                        onShowAll = {
                                            dismissSearchInput()
                                            onShowAllSongResultsChange(true)
                                        },
                                    )
                                    previewSongs.forEachIndexed { index, song ->
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
                                                    selectSong(song, state.matchingSongs)
                                                },
                                                onToggleFavorite = { onToggleFavorite(song.id) },
                                                showDivider = index != previewSongs.lastIndex,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (state.matchingAudiobooks.isNotEmpty()) {
                            item(
                                key = "search_audiobooks",
                                contentType = "search_audiobook_results",
                            ) {
                                SearchAudiobooksResults(
                                    books = state.matchingAudiobooks,
                                    onBookSelected = onAudiobookSelected,
                                )
                            }
                        }

                        if (matchingPlaylists.isNotEmpty()) {
                            item(
                                key = "search_playlists",
                                contentType = "search_playlist_results",
                            ) {
                                Column {
                                    SearchResultsCategoryHeader(
                                        title = commonUiCopy(language).playlists,
                                        subtitle = localizedCountLabel(
                                            matchingPlaylists.size,
                                            "playlist",
                                            language,
                                        ),
                                        iconResId = R.drawable.ic_lucide_list_music,
                                    )
                                    SearchPlaylistListCard(
                                        playlists = matchingPlaylists,
                                        songs = libraryState.songs,
                                        onPlaylistSelected = selectPlaylist,
                                    )
                                }
                            }
                        }

                        if (!state.isSearchPending && !hasSearchResults) {
                            item(
                                key = "search_no_results",
                                contentType = "search_empty_state",
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp, bottom = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = searchCopy(language).noResultsTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = searchCopy(language).noResultsMessage(trimmedQuery),
                                        style = secondaryBodyTextStyle(),
                                        color = readableSecondaryTextColor(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(0.74f),
                                    )
                                }
                            }
                        }
                    }
                }
                    }
                    FastScrollbar(
                        state = listState,
                        topInset = topPadding + 88.dp,
                        bottomInset = bottomPadding + if (isSearchUiActive) 20.dp else 12.dp,
                        trackGesturesEnabled = false,
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
    val language = LocalAppLanguage.current
    val copy = searchCopy(language)
    val interactionSource = rememberElovaireInteractionSource()
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
                modifier = Modifier.elovaireActionBump(
                    interactionSource = interactionSource,
                    label = "search_clear_history_bump",
                ),
                onClick = onClearHistory,
                interactionSource = interactionSource,
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
private fun SearchAudiobooksResults(
    books: List<Audiobook>,
    onBookSelected: (Audiobook) -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = audiobookCopy(language)
    ModuleCard(
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 2.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SearchResultsCategoryHeader(
                title = copy.title,
                iconResId = R.drawable.ic_lucide_library,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(books, key = Audiobook::stableKey, contentType = { "search_audiobook_card" }) { book ->
                    Column(
                        modifier = Modifier
                            .width(124.dp)
                            .clickable { onBookSelected(book) },
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        ArtworkImage(
                            uri = book.artUri,
                            title = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f),
                            cornerRadius = ElovaireRadii.artwork,
                            requestedSizePx = 320,
                            showArtworkGlow = true,
                        )
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.labelLarge,
                            color = readableSecondaryTextColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsCategoryHeader(
    title: String,
    subtitle: String? = null,
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = readableMutedIconColor(),
            modifier = Modifier.size(15.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            subtitle?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                )
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
        SearchResultsCategoryHeader(
            title = commonUiCopy(language).songs,
            subtitle = copy.matchingSongs(resultCount),
            iconResId = R.drawable.ic_lucide_music,
        )
        AnimatedVisibility(visible = showSeeAll) {
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape),
                onClick = onShowAll,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
    val interactionSource = rememberElovaireInteractionSource()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchResultsCategoryHeader(
                title = commonUiCopy(language).songs,
                subtitle = copy.matchingSongs(resultCount),
                iconResId = R.drawable.ic_lucide_music,
                modifier = Modifier.weight(1f),
            )
            Surface(
                modifier = Modifier.elovaireActionBump(
                    interactionSource = interactionSource,
                    label = "search_song_sort_bump",
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
                        text = searchSortModeLabel(selected, language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }
        PopupCardMotionHost(visible = expanded) {
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
private fun SearchPlaylistListCard(
    playlists: List<Playlist>,
    songs: List<Song>,
    onPlaylistSelected: (Playlist) -> Unit,
) {
    val songsById = remember(songs) { songs.associateBy(Song::id) }
    Surface(
        shape = RoundedCornerShape(ElovaireRadii.card),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            playlists.forEachIndexed { index, playlist ->
                SearchPlaylistListRow(
                    playlist = playlist,
                    songs = playlist.songIds.mapNotNull(songsById::get),
                    onClick = { onPlaylistSelected(playlist) },
                )
                if (index != playlists.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
}

@Composable
private fun SearchPlaylistListRow(
    playlist: Playlist,
    songs: List<Song>,
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
        PlaylistArtworkPreview(
            songs = songs,
            title = playlist.name,
            modifier = Modifier.size(50.dp),
            placeholderIconSize = 18.dp,
            cornerRadius = ElovaireRadii.artworkSmall,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = localizedCountLabel(playlist.songIds.size, "track", language),
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
        horizontalArrangement = Arrangement.spacedBy(
            if (entry.kind == SearchHistoryKind.Artist) 14.dp else 12.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtworkImage(
            uri = entry.artUri,
            title = entry.title,
            modifier = Modifier.size(
                if (entry.kind == SearchHistoryKind.Artist) 50.dp else 46.dp,
            ),
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
        if (entry.kind == SearchHistoryKind.Artist) {
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
internal fun LibraryModeToggle(
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
    val interactionSource = rememberElovaireInteractionSource()
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        },
        animationSpec = motionSpecs.tween(MotionDuration.Quick),
        label = "toggle_chip_content",
    )
    Surface(
        modifier = Modifier.elovaireActionBump(
            interactionSource = interactionSource,
            label = "toggle_chip_bump",
        ),
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
                    .size(15.dp),
            )
        }
    }
}
