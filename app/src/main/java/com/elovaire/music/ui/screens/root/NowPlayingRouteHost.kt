package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressConsumer
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.performance.PerformanceScreenState
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState

@Composable
internal fun NowPlayingRouteHost(
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
    PerformanceScreenState("now_playing")
    PerformanceState(
        key = "interaction",
        value = if (lyricsUiState is LyricsUiState.Ready) "lyrics" else "playback_progress_active",
    )
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
        viewModel.setProgressConsumerActive(PlaybackProgressConsumer.NowPlaying, true)
        onDispose {
            viewModel.setProgressConsumerActive(PlaybackProgressConsumer.NowPlaying, false)
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
            onSleepTimerSelected = viewModel::setSleepTimer,
            onVolumeChanged = viewModel::setVolume,
            transitionSnapshot = transitionSnapshot,
            modifier = modifier,
        )
    }
}
