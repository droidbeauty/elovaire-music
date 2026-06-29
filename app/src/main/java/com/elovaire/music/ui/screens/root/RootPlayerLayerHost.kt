package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.motion.PlayerOverlayMotionHost

@Composable
internal fun RootPlayerLayerHost(
    visible: Boolean,
    playerLayerState: PlayerLayerState,
    onExitFinished: () -> Unit,
    onReturnToCompactFinished: () -> Unit,
    nowPlayingViewModel: NowPlayingViewModel,
    playbackManager: PlaybackManager,
    songsById: Map<Long, Song>,
    isCurrentSongFavorite: Boolean,
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
    PlayerOverlayMotionHost(
        visible = visible,
        onExitFinished = {
            onExitFinished()
            if (playerLayerState == PlayerLayerState.ReturningToCompact) {
                onReturnToCompactFinished()
            }
        },
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .zIndex(20f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .consumePointersWithoutSemantics(),
            )
            NowPlayingRouteHost(
                viewModel = nowPlayingViewModel,
                playbackManager = playbackManager,
                enrichedSongsById = songsById,
                isFavorite = isCurrentSongFavorite,
                playlists = playlists,
                onBack = onBack,
                onOpenCurrentAlbum = onOpenCurrentAlbum,
                onToggleFavorite = onToggleFavorite,
                onAddCurrentSongToPlaylist = onAddCurrentSongToPlaylist,
                onCreatePlaylist = onCreatePlaylist,
                onOpenEqualizer = onOpenEqualizer,
                transitionSnapshot = transitionSnapshot,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
