package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import elovaire.music.droidbeauty.app.platform.mediaStoreWriteRequest
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressConsumer
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.performance.PerformanceScreenState
import elovaire.music.droidbeauty.app.ui.performance.PerformanceState
import elovaire.music.droidbeauty.app.ui.theme.ForceDarkColorScheme

@Composable
internal fun NowPlayingRouteHost(
    viewModel: NowPlayingViewModel,
    playbackManager: NowPlayingPlayback,
    enrichedSongsById: Map<Long, Song>,
    audiobookSettings: AudiobookSettings,
    isFavorite: Boolean,
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onOpenCurrentAlbum: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddCurrentSongToPlaylist: (Long, Song) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
    onOpenEqualizer: () -> Unit,
    transitionSnapshot: NowPlayingTransitionSnapshot?,
    modifier: Modifier = Modifier,
) {
    val playerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()
    val lyricsEditorUiState by viewModel.lyricsEditorUiState.collectAsStateWithLifecycle()
    val activeLyricsLineIndex by viewModel.activeLyricsLineIndex.collectAsStateWithLifecycle()
    val context = LocalContext.current
    PerformanceScreenState("now_playing")
    PerformanceState(
        key = "interaction",
        value = if (lyricsUiState is LyricsUiState.Ready) "lyrics" else "idle",
    )
    var pendingLyricsOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingLyricsMediaUri by rememberSaveable { mutableStateOf<String?>(null) }
    val lyricsWriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val operationId = pendingLyricsOperationId
        val mediaUri = pendingLyricsMediaUri?.let(Uri::parse)
        if (operationId != null && mediaUri != null) {
            viewModel.onLyricsWritePermissionResult(
                operationId = operationId,
                mediaUri = mediaUri,
                resultCode = result.resultCode,
            )
        }
        pendingLyricsOperationId = null
        pendingLyricsMediaUri = null
    }
    LaunchedEffect(viewModel) {
        viewModel.lyricsEditorEvents.collect { event ->
            when (event) {
                is LyricsEditorEvent.RequestWritePermission -> {
                    pendingLyricsOperationId = event.operationId
                    pendingLyricsMediaUri = event.mediaUri.toString()
                    val request = if (
                        MediaWriteTargetClassifier.classify(context, event.mediaUri) is
                            MediaWriteTarget.MediaStoreItem
                    ) {
                        mediaStoreWriteRequest(context, listOf(event.mediaUri))
                    } else {
                        null
                    } ?: IntentSenderRequest.Builder(event.request.intentSender).build()
                    lyricsWriteLauncher.launch(request)
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
            audiobookSettings = audiobookSettings,
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
            onToggleCrossfade = viewModel::toggleCrossfade,
            onSleepTimerSelected = viewModel::setSleepTimer,
            onVolumeChanged = viewModel::setVolume,
            transitionSnapshot = transitionSnapshot,
            modifier = modifier,
        )
    }
}
