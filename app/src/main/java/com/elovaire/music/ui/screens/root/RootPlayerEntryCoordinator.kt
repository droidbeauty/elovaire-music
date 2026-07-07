package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import elovaire.music.droidbeauty.app.domain.model.Album

@Composable
internal fun rememberOpenCurrentPlayingAlbum(
    navController: NavHostController,
    navigationState: RootNavigationState,
    currentRoute: String?,
    currentAlbumRouteId: Long?,
    albumsById: Map<Long, Album>,
    playerLayerController: RootPlayerLayerController,
    openAlbum: (Album, ExpandOrigin, AlbumOpenSource) -> Unit,
): (Long) -> Unit {
    return remember(
        navController,
        navigationState,
        currentRoute,
        currentAlbumRouteId,
        albumsById,
        playerLayerController,
        openAlbum,
    ) {
        { albumId ->
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
    }
}
