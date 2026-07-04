package elovaire.music.droidbeauty.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.smartplaylists.NumericOperator
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistEngine
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistMatchMode
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistRule
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSort
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.SortDirection
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

@Composable
internal fun SmartPlaylistDetailScreen(
    playlist: SmartPlaylist?,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    songPlayCounts: Map<Long, Int>,
    recentSongIds: List<Long>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onEdit: (SmartPlaylist) -> Unit,
    onDuplicate: (SmartPlaylist) -> Unit,
    onDelete: (Long) -> Unit,
    onConvertToNormalPlaylist: (SmartPlaylist, List<Song>) -> Unit,
    onPlay: (SmartPlaylist, List<Song>, Boolean) -> Unit,
    onSongSelected: (Song, List<Song>, SmartPlaylist) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val language = LocalAppLanguage.current
    val result = remember(playlist, songs, favoriteSongIds, songPlayCounts, recentSongIds) {
        playlist?.let {
            SmartPlaylistEngine().resolve(
                definition = it,
                songs = songs,
                favoriteSongIds = favoriteSongIds,
                playCounts = songPlayCounts,
                recentSongIds = recentSongIds,
            )
        }
    }
    if (playlist == null || result == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            DetailListTopBar(
                title = "Smart playlist",
                subtitle = null,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            Text(
                text = "Smart playlist not found",
                style = MaterialTheme.typography.bodyLarge,
                color = readableSecondaryTextColor(),
                modifier = Modifier.align(Alignment.Center),
            )
        }
        return
    }
    val listState = rememberElovaireLazyListState(playlist.id, "smart_playlist_detail")
    var showDeleteConfirm by rememberSaveable(playlist.id) { mutableStateOf(false) }
    BackHandler(enabled = showDeleteConfirm) { showDeleteConfirm = false }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.albumHeaderTopGap,
                end = 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                SmartPlaylistDetailHeader(
                    playlist = playlist,
                    songs = result.songs,
                    songCountLabel = "${localizedCountLabel(result.songs.size, "song", language)} • Auto-updating",
                    onPlay = { onPlay(playlist, result.songs, false) },
                    onShuffle = { onPlay(playlist, result.songs, true) },
                )
            }
            item { Spacer(modifier = Modifier.size(18.dp)) }
            if (result.songs.isEmpty()) {
                item {
                    SmartPlaylistEmptyResult()
                }
            } else {
                itemsIndexed(
                    items = result.songs,
                    key = { _, song -> song.id },
                    contentType = { _, _ -> "smart_playlist_song_row" },
                ) { index, song ->
                    GroupedListRowContainer(index = index, lastIndex = result.songs.lastIndex) {
                        PlaylistSongRow(
                            song = song,
                            isFavorite = song.id in favoriteSongIds,
                            isCurrentSong = song.id == currentSongId,
                            isPlaybackActive = isCurrentSongPlaying,
                            onClick = { onSongSelected(song, result.songs, playlist) },
                            onLongPress = {},
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            showOverflowMenu = true,
                            showDivider = index != result.songs.lastIndex,
                        )
                    }
                }
            }
        }
        SmartPlaylistDetailTopBar(
            playlist = playlist,
            songCountLabel = localizedCountLabel(result.songs.size, "song", language),
            onBack = onBack,
            onEdit = { if (playlist.isBuiltIn) onDuplicate(playlist) else onEdit(playlist) },
            onDuplicate = { onDuplicate(playlist) },
            onConvert = { onConvertToNormalPlaylist(playlist, result.songs) },
            onDelete = { showDeleteConfirm = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )
        if (showDeleteConfirm) {
            SimpleConfirmDialog(
                title = "Delete smart playlist?",
                body = "This removes only the saved rules.",
                confirmLabel = "Delete",
                onDismiss = { showDeleteConfirm = false },
                onConfirm = {
                    showDeleteConfirm = false
                    onDelete(playlist.id)
                    onBack()
                },
            )
        }
    }
}

@Composable
private fun SmartPlaylistDetailHeader(
    playlist: SmartPlaylist,
    songs: List<Song>,
    songCountLabel: String,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PlaylistArtworkPreview(
            songs = songs,
            title = playlist.name,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = elovaireScaledSp(ALBUM_HEADER_TITLE_TEXT_SIZE_SP),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8f,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = songCountLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = elovaireScaledSp(12f)),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AlbumHeaderPlayButton(
                    tint = Color.White,
                    backgroundColor = RoseAccent,
                    onClick = onPlay,
                )
                AlbumHeaderActionButton(
                    iconResId = R.drawable.ic_lucide_shuffle,
                    contentDescription = "Shuffle smart playlist",
                    tint = Color.White,
                    backgroundColor = RoseAccent,
                    iconSize = 18.dp,
                    onClick = onShuffle,
                )
            }
        }
    }
}

@Composable
private fun SmartPlaylistEmptyResult() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "No songs match these rules",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Try changing the filters or refreshing your library",
            style = MaterialTheme.typography.bodyLarge,
            color = readableSecondaryTextColor(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SmartPlaylistDetailTopBar(
    playlist: SmartPlaylist,
    songCountLabel: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onConvert: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailListTopBar(
        title = playlist.name,
        subtitle = songCountLabel,
        onBack = onBack,
        actions = buildList {
            add(TopBarActionSpec(R.drawable.ic_lucide_square_pen, "Edit rules", onEdit))
            add(TopBarActionSpec(R.drawable.ic_lucide_list_plus, "Duplicate smart playlist", onDuplicate))
            add(TopBarActionSpec(R.drawable.ic_lucide_list_music, "Convert to playlist", onConvert))
            if (!playlist.isBuiltIn) {
                add(TopBarActionSpec(R.drawable.ic_lucide_trash_2, "Delete smart playlist", onDelete))
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun SmartPlaylistEditorScreen(
    playlist: SmartPlaylist?,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    songPlayCounts: Map<Long, Int>,
    recentSongIds: List<Long>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSave: (SmartPlaylist) -> Unit,
) {
    val now = remember { System.currentTimeMillis() }
    val editing = remember(playlist) {
        playlist?.takeUnless(SmartPlaylist::isBuiltIn)
            ?: SmartPlaylist(
                id = 1L,
                name = "",
                createdAtMs = now,
                updatedAtMs = now,
            )
    }
    var name by rememberSaveable(editing.id) { mutableStateOf(editing.name) }
    var matchMode by rememberSaveable(editing.id) { mutableStateOf(editing.matchMode) }
    var rules by remember(editing.id) { mutableStateOf(editing.rules) }
    var sortField by rememberSaveable(editing.id) { mutableStateOf(editing.sort.field) }
    var sortDirection by rememberSaveable(editing.id) { mutableStateOf(editing.sort.direction) }
    var limitText by rememberSaveable(editing.id) { mutableStateOf(editing.limit?.toString().orEmpty()) }
    val previewPlaylist = editing.copy(
        name = name.trim().ifBlank { "Smart playlist" },
        matchMode = matchMode,
        rules = rules,
        sort = SmartPlaylistSort(sortField, sortDirection),
        limit = limitText.toIntOrNull()?.takeIf { it > 0 },
    )
    val preview = remember(previewPlaylist, songs, favoriteSongIds, songPlayCounts, recentSongIds) {
        SmartPlaylistEngine().resolve(previewPlaylist, songs, favoriteSongIds, songPlayCounts, recentSongIds)
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SmartPlaylistEditorContent(
            name = name,
            matchMode = matchMode,
            rules = rules,
            sortField = sortField,
            sortDirection = sortDirection,
            limitText = limitText,
            previewCount = preview.songs.size,
            bottomPadding = bottomPadding,
            onNameChanged = { name = it },
            onMatchModeChanged = { matchMode = it },
            onRulesChanged = { rules = it },
            onSortFieldChanged = { sortField = it },
            onSortDirectionChanged = { sortDirection = it },
            onLimitChanged = { limitText = it },
        )
        SmartPlaylistEditorTopBar(
            title = if (playlist == null) "New smart playlist" else "Edit smart playlist",
            onBack = onBack,
            onSave = {
                val trimmedName = name.trim()
                if (trimmedName.isNotBlank()) {
                    onSave(
                        editing.copy(
                            name = trimmedName,
                            matchMode = matchMode,
                            rules = rules,
                            sort = SmartPlaylistSort(sortField, sortDirection),
                            limit = limitText.toIntOrNull()?.takeIf { it > 0 },
                            updatedAtMs = System.currentTimeMillis(),
                        ),
                    )
                }
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SmartPlaylistEditorContent(
    name: String,
    matchMode: SmartPlaylistMatchMode,
    rules: List<SmartPlaylistRule>,
    sortField: SmartPlaylistSortField,
    sortDirection: SortDirection,
    limitText: String,
    previewCount: Int,
    bottomPadding: Dp,
    onNameChanged: (String) -> Unit,
    onMatchModeChanged: (SmartPlaylistMatchMode) -> Unit,
    onRulesChanged: (List<SmartPlaylistRule>) -> Unit,
    onSortFieldChanged: (SmartPlaylistSortField) -> Unit,
    onSortDirectionChanged: (SortDirection) -> Unit,
    onLimitChanged: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        overscrollEffect = null,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = detailTopBarOccupiedHeight() + 20.dp,
            end = 20.dp,
            bottom = bottomPadding + 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SmartEditorTextField(name, onNameChanged, "Name") }
        item {
            SmartEditorChoiceRow(
                title = "Match mode",
                value = if (matchMode == SmartPlaylistMatchMode.All) "Match all rules" else "Match any rule",
                onClick = {
                    onMatchModeChanged(
                        if (matchMode == SmartPlaylistMatchMode.All) SmartPlaylistMatchMode.Any else SmartPlaylistMatchMode.All,
                    )
                },
            )
        }
        itemsIndexed(rules, key = { index, _ -> index }) { index, rule ->
            SmartRuleRow(
                rule = rule,
                onRuleChanged = { updated -> onRulesChanged(rules.toMutableList().apply { set(index, updated) }) },
                onRemove = { onRulesChanged(rules.toMutableList().apply { removeAt(index) }) },
            )
        }
        item {
            SmartEditorChoiceRow(
                title = "Add rule",
                value = "Title contains",
                iconResId = R.drawable.ic_lucide_plus,
                onClick = { onRulesChanged(rules + SmartPlaylistRule.TitleContains("")) },
            )
        }
        item { SmartEditorChoiceRow("Sort", sortField.name) { onSortFieldChanged(sortField.next()) } }
        item {
            SmartEditorChoiceRow(
                title = "Direction",
                value = sortDirection.name,
                onClick = {
                    onSortDirectionChanged(
                        if (sortDirection == SortDirection.Ascending) SortDirection.Descending else SortDirection.Ascending,
                    )
                },
            )
        }
        item {
            SmartEditorTextField(limitText, { onLimitChanged(it.filter(Char::isDigit).take(5)) }, "Limit")
        }
        item {
            Text(
                text = "Preview: $previewCount songs",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun SmartPlaylistEditorTopBar(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailListTopBar(
        title = title,
        subtitle = null,
        onBack = onBack,
        actions = listOf(TopBarActionSpec(R.drawable.ic_lucide_check, "Save smart playlist", onSave)),
        modifier = modifier,
    )
}

@Composable
private fun SmartRuleRow(
    rule: SmartPlaylistRule,
    onRuleChanged: (SmartPlaylistRule) -> Unit,
    onRemove: () -> Unit,
) {
    val label = when (rule) {
        is SmartPlaylistRule.TitleContains -> "Title contains"
        is SmartPlaylistRule.ArtistContains -> "Artist contains"
        is SmartPlaylistRule.AlbumContains -> "Album contains"
        is SmartPlaylistRule.GenreMatches -> "Genre contains"
        is SmartPlaylistRule.FavoriteIs -> "Favorite is"
        is SmartPlaylistRule.DurationBetween -> "Duration greater than"
        is SmartPlaylistRule.PlayCount -> "Play count"
        is SmartPlaylistRule.FileFormatIs -> "Format is"
        is SmartPlaylistRule.FolderContains -> "Folder contains"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_x),
                contentDescription = "Remove rule",
                modifier = Modifier.size(18.dp).clickable(onClick = onRemove),
            )
        }
        when (rule) {
            is SmartPlaylistRule.TitleContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, "Value")
            is SmartPlaylistRule.ArtistContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, "Value")
            is SmartPlaylistRule.AlbumContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, "Value")
            is SmartPlaylistRule.GenreMatches -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, "Value")
            is SmartPlaylistRule.FavoriteIs -> SmartEditorChoiceRow("Value", if (rule.favorite) "Yes" else "No") {
                onRuleChanged(rule.copy(favorite = !rule.favorite))
            }
            is SmartPlaylistRule.DurationBetween -> SmartEditorTextField(
                value = (rule.minMs?.div(1000L)).orZeroText(),
                onValueChange = { onRuleChanged(rule.copy(minMs = it.toLongOrNull()?.times(1000L))) },
                label = "Seconds",
            )
            is SmartPlaylistRule.PlayCount -> SmartEditorTextField(
                value = rule.value.toString(),
                onValueChange = {
                    onRuleChanged(rule.copy(operator = NumericOperator.GreaterThan, value = it.toIntOrNull()?.coerceAtLeast(0) ?: 0))
                },
                label = "Count",
            )
            is SmartPlaylistRule.FileFormatIs -> SmartEditorTextField(rule.extension, { onRuleChanged(rule.copy(extension = it)) }, "Extension")
            is SmartPlaylistRule.FolderContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, "Folder text")
        }
    }
}

@Composable
private fun SmartEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
        ),
    )
}

@Composable
private fun SmartEditorChoiceRow(
    title: String,
    value: String,
    iconResId: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconResId?.let {
            Icon(painter = painterResource(id = it), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(text = value, style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
        }
    }
}

@Composable
internal fun SmartPlaylistGridTile(
    summary: SmartPlaylistSummary,
    onClick: (ExpandOrigin) -> Unit,
) {
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val screenSizePx = screenContainerSizePx()
    Column(
        modifier = Modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .clickable { onClick(bounds.toExpandOrigin(screenSizePx.width.toFloat(), screenSizePx.height.toFloat())) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaylistArtworkPreview(
            songs = summary.result.songs,
            title = summary.playlist.name,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = summary.playlist.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${localizedCountLabel(summary.result.songs.size, "song", LocalAppLanguage.current)} • Auto-updating",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SimpleConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun SmartPlaylistSortField.next(): SmartPlaylistSortField {
    val entries = SmartPlaylistSortField.entries
    return entries[(ordinal + 1) % entries.size]
}

private fun Long?.orZeroText(): String = this?.toString().orEmpty()
