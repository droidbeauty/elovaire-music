package elovaire.music.droidbeauty.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.formatCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlinx.coroutines.launch

@Composable
internal fun PlaylistDetailScreen(
    playlist: Playlist?,
    librarySongs: List<Song>,
    favoriteSongIds: Set<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onPlayPlaylist: (List<Song>, String) -> Unit,
    onShufflePlaylist: (List<Song>, String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onUpdateSongOrder: (List<Long>) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val revealRegistry = rememberMotionRevealRegistry()
    val motionTransitions = rememberMotionTransitions()
    val songsById = remember(librarySongs) { librarySongs.associateBy { it.id } }
    val detailState = remember(playlist, songsById) {
        buildPlaylistDetailState(playlist, songsById)
    }
    if (detailState.playlist == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            DetailListTopBar(
                title = commonUiCopy(LocalAppLanguage.current).playlists,
                subtitle = null,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                        end = 20.dp,
                        bottom = bottomPadding,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rootUiCopy(LocalAppLanguage.current).playlistNotFound,
                    style = MaterialTheme.typography.bodyLarge,
                    color = readableSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val playlistState = detailState.playlist
    val defaultSongMenuActions = LocalSongMenuActions.current
    var editMode by rememberSaveable(playlistState.id) { mutableStateOf(false) }
    var showAddSongsPicker by rememberSaveable(playlistState.id) { mutableStateOf(false) }
    var editableSongIds by rememberSaveable(playlistState.id) { mutableStateOf(playlistState.songIds) }
    var songIdsMarkedForRemoval by rememberSaveable(playlistState.id) { mutableStateOf(setOf<Long>()) }
    var activelyDraggedSongId by rememberSaveable(playlistState.id) { mutableStateOf<Long?>(null) }
    var showEditModeMenu by rememberSaveable(playlistState.id) { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable(playlistState.id) { mutableStateOf(false) }
    val language = LocalAppLanguage.current
    val rootCopy = rootUiCopy(language)
    LaunchedEffect(playlistState.id, playlistState.songIds, editMode) {
        if (!editMode) {
            editableSongIds = playlistState.songIds
            songIdsMarkedForRemoval = emptySet()
            activelyDraggedSongId = null
        }
    }
    val displayedSongIds = if (editMode) editableSongIds else playlistState.songIds
    val playlistSongMenuActions = remember(defaultSongMenuActions, playlistState.id, playlistState.songIds) {
        defaultSongMenuActions.copy(
            onDeleteFromLibrary = { song ->
                onUpdateSongOrder(playlistState.songIds.filterNot { it == song.id })
            },
            deletePhrase = UiPhrase.RemoveFromList,
        )
    }
    val playlistSongs = remember(displayedSongIds, songsById) {
        displayedSongIds.mapNotNull(songsById::get)
    }
    val playlistDurationMs = remember(playlistSongs) { playlistSongs.sumOf(Song::durationMs) }
    val detailTopPadding = detailTopBarOccupiedHeight()
    val editMenuTopInset by animateDpAsState(
        targetValue = if (editMode && showEditModeMenu) 50.dp else 0.dp,
        animationSpec = ElovaireMotion.sizeSoft(),
        label = "playlist_edit_menu_top_inset",
    )
    val topBarActions = remember(
        editMode,
        editableSongIds,
        songIdsMarkedForRemoval,
        playlistState.isSystem,
        rootCopy,
    ) {
        buildList {
            if (editMode && !playlistState.isSystem) {
                add(
                    TopBarActionSpec(
                        iconResId = R.drawable.ic_lucide_plus,
                        contentDescription = rootCopy.addSongsTitle,
                        onClick = { showAddSongsPicker = true },
                    ),
                )
            }
            if (!playlistState.isSystem) {
                add(
                    TopBarActionSpec(
                        iconResId = if (editMode) R.drawable.ic_lucide_check else R.drawable.ic_lucide_square_pen,
                        contentDescription = if (editMode) rootCopy.savePlaylistChanges else rootCopy.editPlaylist,
                        onClick = {
                            if (editMode) {
                                val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                                onUpdateSongOrder(updatedSongIds)
                                editableSongIds = updatedSongIds
                                songIdsMarkedForRemoval = emptySet()
                                activelyDraggedSongId = null
                                editMode = false
                                showEditModeMenu = false
                            } else {
                                editableSongIds = playlistState.songIds
                                songIdsMarkedForRemoval = emptySet()
                                editMode = true
                                showEditModeMenu = true
                            }
                        },
                    ),
                )
            }
        }
    }
    BackHandler(enabled = showAddSongsPicker || editMode) {
        if (showAddSongsPicker) {
            showAddSongsPicker = false
        } else if (editMode) {
            val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
            onUpdateSongOrder(updatedSongIds)
            editableSongIds = updatedSongIds
            songIdsMarkedForRemoval = emptySet()
            activelyDraggedSongId = null
            editMode = false
            showEditModeMenu = false
        }
    }
    CompositionLocalProvider(LocalSongMenuActions provides playlistSongMenuActions) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val listState = rememberElovaireLazyListState(playlistState.id, "playlist_detail")
            val scope = rememberCoroutineScope()
            val density = androidx.compose.ui.platform.LocalDensity.current
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .ensureSingleItemRubberBand(listState),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = detailTopPadding + ElovaireSpacing.albumHeaderTopGap + editMenuTopInset,
                    end = 20.dp,
                    bottom = bottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PlaylistArtworkPreview(
                            songs = playlistSongs,
                            title = playlistState.name,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = playlistState.name,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = elovaireScaledSp(ALBUM_HEADER_TITLE_TEXT_SIZE_SP),
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8f,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Normal,
                                            ),
                                        ) {
                                            append(formatCountLabel(playlistSongs.size, "track"))
                                        }
                                        append("  •  ")
                                        withStyle(
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                                fontWeight = FontWeight.Normal,
                                            ),
                                        ) {
                                            append(formatPlaylistDuration(playlistDurationMs))
                                        }
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AlbumHeaderPlayButton(
                                    tint = Color.White,
                                    backgroundColor = RoseAccent,
                                    onClick = { onPlayPlaylist(playlistSongs, playlistState.name) },
                                )
                                AlbumHeaderActionButton(
                                    iconResId = R.drawable.ic_lucide_shuffle,
                                    contentDescription = "Shuffle playlist",
                                    tint = Color.White,
                                    backgroundColor = RoseAccent,
                                    iconSize = 18.dp,
                                    onClick = { onShufflePlaylist(playlistSongs, playlistState.name) },
                                )
                            }
                        }
                    }
                }
                if (playlistSongs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.size(18.dp))
                    }
                }

                if (playlistSongs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 34.dp, bottom = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = miscPhrase(LocalAppLanguage.current, MiscPhrase.NoSongsYet),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = miscPhrase(LocalAppLanguage.current, MiscPhrase.AddSongsViaEdit),
                                style = MaterialTheme.typography.bodyLarge,
                                color = readableSecondaryTextColor(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = playlistSongs,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> "playlist_song_row" },
                    ) { index, song ->
                        GroupedListRowContainer(
                            index = index,
                            lastIndex = playlistSongs.lastIndex,
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = ElovaireMotion.listPlacementSpec(),
                                )
                                .elovaireListReveal(
                                    itemKey = "${song.id}_$index",
                                    index = index,
                                    registry = revealRegistry,
                                    enabled = !editMode,
                                ),
                        ) {
                            PlaylistSongRow(
                                song = song,
                                isFavorite = song.id in favoriteSongIds,
                                isCurrentSong = song.id == currentSongId,
                                isPlaybackActive = isCurrentSongPlaying,
                                editMode = editMode,
                                onClick = {
                                    if (!editMode) {
                                        onSongSelected(song, playlistSongs)
                                    }
                                },
                                markedForRemoval = song.id in songIdsMarkedForRemoval,
                                onLongPress = {
                                    if (!playlistState.isSystem && playlistSongs.isNotEmpty() && !editMode) {
                                        editableSongIds = playlistState.songIds
                                        songIdsMarkedForRemoval = emptySet()
                                        editMode = true
                                        showEditModeMenu = true
                                    }
                                },
                                onToggleMarkedForRemoval = {
                                    songIdsMarkedForRemoval = if (song.id in songIdsMarkedForRemoval) {
                                        songIdsMarkedForRemoval - song.id
                                    } else {
                                        songIdsMarkedForRemoval + song.id
                                    }
                                },
                                isDragged = activelyDraggedSongId == song.id,
                                onDragActiveChanged = { isActive ->
                                    activelyDraggedSongId = when {
                                        isActive -> song.id
                                        activelyDraggedSongId == song.id -> null
                                        else -> activelyDraggedSongId
                                    }
                                },
                                onToggleFavorite = { onToggleFavorite(song.id) },
                                onMoveBy = { delta ->
                                    if (editMode && delta != 0) {
                                        val fromIndex = editableSongIds.indexOf(song.id)
                                        if (fromIndex >= 0) {
                                            val targetIndex = (fromIndex + delta).coerceIn(0, editableSongIds.lastIndex)
                                            if (targetIndex != fromIndex) {
                                                editableSongIds = editableSongIds.toMutableList().apply {
                                                    add(targetIndex, removeAt(fromIndex))
                                                }.toList()
                                            }
                                        }
                                    }
                                },
                                onReorderDrag = { dragAmount ->
                                    if (editMode && editableSongIds.size > 1) {
                                        val layoutInfo = listState.layoutInfo
                                        val draggedItem = layoutInfo.visibleItemsInfo.firstOrNull { it.key == song.id }
                                        if (draggedItem != null) {
                                            val edgeThresholdPx = with(density) { 84.dp.toPx() }
                                            val canScrollUp =
                                                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                                            val canScrollDown =
                                                layoutInfo.visibleItemsInfo.lastOrNull()?.index != layoutInfo.totalItemsCount - 1
                                            val nearTop = draggedItem.offset <= layoutInfo.viewportStartOffset + edgeThresholdPx
                                            val nearBottom = draggedItem.offset + draggedItem.size >=
                                                layoutInfo.viewportEndOffset - edgeThresholdPx
                                            when {
                                                dragAmount < 0f && nearTop && canScrollUp -> {
                                                    scope.launch {
                                                        listState.scrollBy((dragAmount * 0.72f).coerceAtLeast(-22f))
                                                    }
                                                }
                                                dragAmount > 0f && nearBottom && canScrollDown -> {
                                                    scope.launch {
                                                        listState.scrollBy((dragAmount * 0.72f).coerceAtMost(22f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                showOverflowMenu = !editMode,
                                showDivider = index != playlistSongs.lastIndex,
                            )
                        }
                    }
                }
            }

            DetailListTopBar(
                title = playlistState.name,
                subtitle = localizedCountLabel(playlistSongs.size, "song", LocalAppLanguage.current),
                onBack = {
                    if (showAddSongsPicker) {
                        showAddSongsPicker = false
                    } else if (editMode) {
                        val updatedSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                        onUpdateSongOrder(updatedSongIds)
                        editableSongIds = updatedSongIds
                        songIdsMarkedForRemoval = emptySet()
                        activelyDraggedSongId = null
                        editMode = false
                        showEditModeMenu = false
                    } else {
                        onBack()
                    }
                },
                actions = topBarActions,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            AnimatedVisibility(
                visible = editMode && showEditModeMenu && !playlistState.isSystem,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(3f),
                enter = motionTransitions.verticalRevealEnter(),
                exit = motionTransitions.verticalRevealExit(),
            ) {
                TopBarDualActionMenu(
                    topBarHeight = detailTopPadding,
                    leadingAction = TopBarMenuAction(
                        iconResId = R.drawable.ic_lucide_square_pen,
                        label = uiPhrase(LocalAppLanguage.current, UiPhrase.Rename),
                        tint = MaterialTheme.colorScheme.onSurface,
                        onClick = { showRenameDialog = true },
                    ),
                    trailingAction = TopBarMenuAction(
                        iconResId = R.drawable.ic_lucide_trash_2,
                        label = uiPhrase(LocalAppLanguage.current, UiPhrase.RemoveFromList),
                        tint = DestructiveRed,
                        enabled = songIdsMarkedForRemoval.isNotEmpty(),
                        onClick = {
                            if (songIdsMarkedForRemoval.isNotEmpty()) {
                                editableSongIds = editableSongIds.filterNot { it in songIdsMarkedForRemoval }
                                songIdsMarkedForRemoval = emptySet()
                                activelyDraggedSongId = null
                            }
                        },
                    ),
                )
            }
        }
    }

    if (showAddSongsPicker && !playlistState.isSystem) {
        AddSongsToPlaylistOverlay(
            availableSongs = librarySongs,
            existingSongIds = editableSongIds.toSet(),
            onDismiss = { showAddSongsPicker = false },
            onAddSongs = { selectedSongIds ->
                editableSongIds = (editableSongIds + selectedSongIds).distinct()
                showAddSongsPicker = false
            },
        )
    }
    if (showRenameDialog && !playlistState.isSystem) {
        PlaylistNameDialog(
            title = rootUiCopy(LocalAppLanguage.current).renamePlaylistTitle,
            confirmLabel = rootUiCopy(LocalAppLanguage.current).save,
            initialName = playlistState.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                onRenamePlaylist(playlistState.id, name)
                showRenameDialog = false
            },
        )
    }
}

@Composable
internal fun PlaylistSongRow(
    song: Song,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isPlaybackActive: Boolean = false,
    editMode: Boolean = false,
    onClick: () -> Unit,
    markedForRemoval: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleMarkedForRemoval: () -> Unit = {},
    isDragged: Boolean = false,
    onDragActiveChanged: (Boolean) -> Unit = {},
    onToggleFavorite: () -> Unit,
    onMoveBy: (Int) -> Unit = {},
    onReorderDrag: (Float) -> Unit = {},
    showOverflowMenu: Boolean = false,
    showDivider: Boolean,
) {
    val motionSpecs = rememberMotionSpecs()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val reorderStepPx = remember(density) { with(density) { 18.dp.toPx() } }
    var reorderDragAccumulator by remember(song.id, editMode) { mutableFloatStateOf(0f) }
    var handleDragActive by remember(song.id, editMode) { mutableStateOf(false) }
    val visualDragOffsetY = reorderDragAccumulator.coerceIn(-20f, 20f)
    val handleTint by animateColorAsState(
        targetValue = if (handleDragActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_reorder_handle_tint",
    )
    val handleScale by animateFloatAsState(
        targetValue = if (handleDragActive) 1.1f else 1f,
        animationSpec = ElovaireMotion.releaseSpringSpec(),
        label = "playlist_reorder_handle_scale",
    )
    val rowScale by animateFloatAsState(
        targetValue = if (isDragged) 1.018f else 1f,
        animationSpec = if (isDragged) {
            motionSpecs.spring(
                dampingRatio = 0.5f,
                stiffness = 210f,
            )
        } else {
            ElovaireMotion.releaseSpringSpec()
        },
        label = "playlist_drag_row_scale",
    )
    val rowTranslationY by animateFloatAsState(
        targetValue = if (isDragged) visualDragOffsetY else 0f,
        animationSpec = motionSpecs.spring(
            dampingRatio = 0.42f,
            stiffness = 250f,
        ),
        label = "playlist_drag_row_translation",
    )
    val dragHighlight by animateColorAsState(
        targetValue = if (handleDragActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_reorder_drag_highlight",
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ElovaireRadii.tile))
                .background(dragHighlight)
                .graphicsLayer {
                    scaleX = rowScale
                    scaleY = rowScale
                    translationY = rowTranslationY
                }
                .combinedClickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongPress,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ElovaireAnimatedVisibility(
                visible = editMode,
                enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = ElovaireMotion.offsetSoft(),
                    ),
                exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = ElovaireMotion.offsetSoft(durationMillis = 80),
                    ),
                label = "playlist_song_reorder_handle",
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = handleScale
                            scaleY = handleScale
                        }
                        .pointerInput(song.id, editMode) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = true
                                    onDragActiveChanged(true)
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    reorderDragAccumulator += dragAmount
                                    onReorderDrag(dragAmount)
                                    while (reorderDragAccumulator <= -reorderStepPx) {
                                        onMoveBy(-1)
                                        reorderDragAccumulator += reorderStepPx
                                    }
                                    while (reorderDragAccumulator >= reorderStepPx) {
                                        onMoveBy(1)
                                        reorderDragAccumulator -= reorderStepPx
                                    }
                                },
                                onDragEnd = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = false
                                    onDragActiveChanged(false)
                                },
                                onDragCancel = {
                                    reorderDragAccumulator = 0f
                                    handleDragActive = false
                                    onDragActiveChanged(false)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_chevrons_up_down),
                        contentDescription = "Reorder song",
                        tint = handleTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
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
                    visible = isCurrentSong && isPlaybackActive,
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
                modifier = Modifier.width(if (editMode) 36.dp else if (showOverflowMenu) 96.dp else 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElovaireAnimatedVisibility(
                    visible = !editMode,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
                    label = "playlist_song_metadata_visibility",
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatDuration(song.durationMs),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(40.dp),
                        )
                        InlineFavoriteSongButton(
                            isFavorite = isFavorite,
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = onToggleFavorite,
                        )
                        if (showOverflowMenu) {
                            SongOverflowMenuButton(
                                song = song,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                ElovaireAnimatedVisibility(
                    visible = editMode,
                    enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()) +
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = ElovaireMotion.scaleSoft(),
                        ),
                    exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()) +
                        scaleOut(
                            targetScale = 0.92f,
                            animationSpec = ElovaireMotion.fadeFast(),
                        ),
                    label = "playlist_song_remove_toggle",
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleMarkedForRemoval,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        SelectionIndicatorIcon(selected = markedForRemoval)
                    }
                }
            }
        }
        if (showDivider) {
            DividerLine()
        }
    }
}
