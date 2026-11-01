package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor

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
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.playlistMainCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.builtInSmartPlaylistTitle
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistEmptyCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistDeleteCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistMissingCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistPreviewLabel
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistEditRulesLabel
import elovaire.music.droidbeauty.app.ui.i18n.networkSourcesCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistEditorHeadingCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistRemoveRuleLabel
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistMatchCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistTextRuleCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistOtherRuleCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistValueCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistInputCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistOrderCopy
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistSortLabel
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistActionCopy

@Composable
internal fun SmartPlaylistDetailScreen(
    playlist: SmartPlaylist?,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    songPlayCounts: Map<Long, Int>,
    currentSongId: Long?,
    isCurrentSongPlaying: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onEdit: (SmartPlaylist) -> Unit,
    onDelete: (Long) -> Unit,
    onPlay: (SmartPlaylist, List<Song>, Boolean) -> Unit,
    onSongSelected: (Song, List<Song>, SmartPlaylist) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    val language = LocalAppLanguage.current
    val result = remember(playlist, songs, favoriteSongIds, songPlayCounts) {
        playlist?.let {
            SmartPlaylistEngine.resolve(
                definition = it,
                songs = songs,
                favoriteSongIds = favoriteSongIds,
                playCounts = songPlayCounts,
            )
        }
    }
    if (playlist == null || result == null) {
        val copy = smartPlaylistMissingCopy(language)
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            DetailListTopBar(
                title = copy.title,
                subtitle = null,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            Text(
                text = copy.message,
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
                    songCountLabel = "${localizedCountLabel(result.songs.size, "song", language)} • ${playlistMainCopy(language).autoUpdating}",
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
            onEdit = { onEdit(playlist) },
            onDelete = { showDeleteConfirm = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
            bottomInset = bottomPadding + 16.dp,
        )
        SimpleConfirmDialog(
            visible = showDeleteConfirm,
            title = smartPlaylistDeleteCopy(language).title,
            body = smartPlaylistDeleteCopy(language).message,
            confirmLabel = uiPhrase(language, UiPhrase.Delete),
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete(playlist.id)
                onBack()
            },
        )
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
    val language = LocalAppLanguage.current
    val title = playlist.builtInType?.let { builtInSmartPlaylistTitle(it, language) } ?: playlist.name
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PlaylistArtworkPreview(
            songs = songs,
            title = title,
            modifier = Modifier.fillMaxWidth(),
            placeholderIconSize = 30.dp,
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
                    text = title,
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
                    contentDescription = smartPlaylistActionCopy(LocalAppLanguage.current).shuffle,
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
    val copy = smartPlaylistEmptyCopy(LocalAppLanguage.current)
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = copy.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = copy.message,
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
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val title = playlist.builtInType?.let { builtInSmartPlaylistTitle(it, language) } ?: playlist.name
    DetailListTopBar(
        title = title,
        subtitle = songCountLabel,
        onBack = onBack,
        actions = buildList {
            if (!playlist.isBuiltIn) {
                add(TopBarActionSpec(R.drawable.ic_lucide_square_pen, smartPlaylistEditRulesLabel(language), onEdit))
            }
            if (!playlist.isBuiltIn) {
                add(TopBarActionSpec(R.drawable.ic_lucide_trash_2, smartPlaylistActionCopy(language).delete, onDelete))
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
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSave: (SmartPlaylist) -> Unit,
) {
    val now = remember { System.currentTimeMillis() }
    val headingCopy = smartPlaylistEditorHeadingCopy(LocalAppLanguage.current)
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
    val preview = remember(previewPlaylist, songs, favoriteSongIds, songPlayCounts) {
        SmartPlaylistEngine.resolve(previewPlaylist, songs, favoriteSongIds, songPlayCounts)
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
            title = if (playlist == null) headingCopy.create else headingCopy.edit,
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
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val language = LocalAppLanguage.current
    val matchCopy = smartPlaylistMatchCopy(language)
    val textRules = smartPlaylistTextRuleCopy(language)
    val orderCopy = smartPlaylistOrderCopy(language)
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
        overscrollEffect = null,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = detailTopBarOccupiedHeight() + 20.dp,
            end = 20.dp,
            bottom = bottomPadding + 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SmartEditorTextField(name, onNameChanged, networkSourcesCopy(language).name) }
        item {
            SmartEditorChoiceRow(
                title = matchCopy.mode,
                value = if (matchMode == SmartPlaylistMatchMode.All) matchCopy.all else matchCopy.any,
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
                title = matchCopy.add,
                value = textRules.title,
                iconResId = R.drawable.ic_lucide_plus,
                onClick = { onRulesChanged(rules + SmartPlaylistRule.TitleContains("")) },
            )
        }
        item { SmartEditorChoiceRow(orderCopy.sort, smartPlaylistSortLabel(language, sortField)) { onSortFieldChanged(sortField.next()) } }
        item {
            SmartEditorChoiceRow(
                title = orderCopy.direction,
                value = if (sortDirection == SortDirection.Ascending) orderCopy.ascending else orderCopy.descending,
                onClick = {
                    onSortDirectionChanged(
                        if (sortDirection == SortDirection.Ascending) SortDirection.Descending else SortDirection.Ascending,
                    )
                },
            )
        }
        item {
            SmartEditorTextField(limitText, { onLimitChanged(it.filter(Char::isDigit).take(5)) }, smartPlaylistInputCopy(language).limit)
        }
        item {
            Text(
                text = smartPlaylistPreviewLabel(language, previewCount),
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
        actions = listOf(TopBarActionSpec(R.drawable.ic_lucide_check, smartPlaylistEditorHeadingCopy(LocalAppLanguage.current).save, onSave)),
        modifier = modifier,
    )
}

@Composable
private fun SmartRuleRow(
    rule: SmartPlaylistRule,
    onRuleChanged: (SmartPlaylistRule) -> Unit,
    onRemove: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val textRules = smartPlaylistTextRuleCopy(language)
    val otherRules = smartPlaylistOtherRuleCopy(language)
    val values = smartPlaylistValueCopy(language)
    val inputs = smartPlaylistInputCopy(language)
    val label = when (rule) {
        is SmartPlaylistRule.TitleContains -> textRules.title
        is SmartPlaylistRule.ArtistContains -> textRules.artist
        is SmartPlaylistRule.AlbumContains -> textRules.album
        is SmartPlaylistRule.GenreMatches -> textRules.genre
        is SmartPlaylistRule.FavoriteIs -> otherRules.favorite
        is SmartPlaylistRule.DurationBetween -> otherRules.duration
        is SmartPlaylistRule.PlayCount -> otherRules.playCount
        is SmartPlaylistRule.FileFormatIs -> otherRules.format
        is SmartPlaylistRule.FolderContains -> textRules.folder
    }
    val interactionSource = rememberElovaireInteractionSource()
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
                contentDescription = smartPlaylistRemoveRuleLabel(LocalAppLanguage.current),
                modifier = Modifier
                    .size(28.dp)
                    .elovaireActionBump(
                        interactionSource = interactionSource,
                        label = "smart_rule_remove_bump",
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onRemove,
                    )
                    .padding(5.dp),
            )
        }
        when (rule) {
            is SmartPlaylistRule.TitleContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, values.value)
            is SmartPlaylistRule.ArtistContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, values.value)
            is SmartPlaylistRule.AlbumContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, values.value)
            is SmartPlaylistRule.GenreMatches -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, values.value)
            is SmartPlaylistRule.FavoriteIs -> SmartEditorChoiceRow(values.value, if (rule.favorite) values.yes else values.no) {
                onRuleChanged(rule.copy(favorite = !rule.favorite))
            }
            is SmartPlaylistRule.DurationBetween -> SmartEditorTextField(
                value = (rule.minMs?.div(1000L)).orZeroText(),
                onValueChange = {
                    val seconds = it.toLongOrNull()?.takeIf { value -> value in 0L..(Long.MAX_VALUE / 1_000L) }
                    onRuleChanged(rule.copy(minMs = seconds?.times(1_000L)))
                },
                label = values.seconds,
            )
            is SmartPlaylistRule.PlayCount -> SmartEditorTextField(
                value = rule.value.toString(),
                onValueChange = {
                    onRuleChanged(rule.copy(operator = NumericOperator.GreaterThan, value = it.toIntOrNull()?.coerceAtLeast(0) ?: 0))
                },
                label = inputs.count,
            )
            is SmartPlaylistRule.FileFormatIs -> SmartEditorTextField(rule.extension, { onRuleChanged(rule.copy(extension = it)) }, inputs.extension)
            is SmartPlaylistRule.FolderContains -> SmartEditorTextField(rule.query, { onRuleChanged(rule.copy(query = it)) }, inputs.folderText)
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
            .elovaireClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconResId?.let {
            Icon(painter = painterResource(id = it), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun SimpleConfirmDialog(
    visible: Boolean,
    title: String,
    body: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var mounted by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) mounted = true
    }
    if (!mounted) return
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PopupCardMotionHost(
                surfaceId = "playlists.delete_confirmation",
                visible = visible,
                onExitFinished = { if (!visible) mounted = false },
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ElovaireRadii.card),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = onDismiss) { Text(uiPhrase(LocalAppLanguage.current, UiPhrase.Cancel)) }
                            TextButton(onClick = onConfirm) { Text(confirmLabel) }
                        }
                    }
                }
            }
        }
    }
}

private fun SmartPlaylistSortField.next(): SmartPlaylistSortField {
    val entries = SmartPlaylistSortField.entries
    return entries[(ordinal + 1) % entries.size]
}

private fun Long?.orZeroText(): String = this?.toString().orEmpty()
