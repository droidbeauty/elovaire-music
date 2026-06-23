package elovaire.music.droidbeauty.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.formatCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed

@Composable
internal fun PlaylistsScreen(
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
    val playlistRows = remember(playlists, libraryState.songs) {
        buildPlaylistRowModels(
            playlists = playlists,
            songsById = libraryState.songs.associateBy(Song::id),
        )
    }
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
    LaunchedEffect(playlists) {
        val existingUserIds = playlists.asSequence()
            .filterNot(Playlist::isSystem)
            .mapTo(mutableSetOf(), Playlist::id)
        val retainedSelection = selectedPlaylistIds.filterTo(linkedSetOf()) { it in existingUserIds }
        if (retainedSelection != selectedPlaylistIds) {
            selectedPlaylistIds = retainedSelection
        }
        if (playlistBeingRenamed?.id?.let { it !in existingUserIds } == true) {
            playlistBeingRenamed = null
        }
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
        if (playlistRows.isEmpty()) {
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
                items(
                    items = playlistRows,
                    key = { it.playlist.id },
                    contentType = { "playlist_tile" },
                ) { row ->
                    PlaylistGridTile(
                        playlist = row.playlist,
                        previewSongs = row.previewSongs,
                        selected = row.playlist.id in selectedPlaylistIds,
                        selectionMode = editMode,
                        onClick = { origin ->
                            if (editMode && !row.playlist.isSystem) {
                                selectedPlaylistIds = selectedPlaylistIds.togglePlaylistSelection(row.playlist.id)
                            } else {
                                onOpenPlaylist(row.playlist, origin)
                            }
                        },
                        onLongPress = {
                            if (!row.playlist.isSystem) {
                                selectedPlaylistIds = selectedPlaylistIds.togglePlaylistSelection(row.playlist.id)
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
                    enabled = selectedPlaylistIds.size == 1,
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
internal fun PlaylistGridTile(
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
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = Modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .combinedClickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
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
            overflow = TextOverflow.Ellipsis,
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
internal fun PlaylistSelectionIndicator(
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
internal fun EmptyPlaylistState(
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
        )
        Text(
            text = miscPhrase(language, MiscPhrase.TapPlusToCreatePlaylist),
            style = MaterialTheme.typography.bodyLarge,
            color = readableSecondaryTextColor(),
        )
    }
}
