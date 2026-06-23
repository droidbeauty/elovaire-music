package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.formatCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.elovaireListDropdownReveal
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
internal fun PlaylistArtworkPreview(
    songs: List<Song>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val collageSongs = remember(songs) { playlistCollageSongs(songs) }
    val usesCollage = collageSongs.size >= 4
    val coverSong = songs.firstOrNull()
    val gradient = rememberArtworkGradient(coverSong?.artUri).value
    Box(modifier = modifier.aspectRatio(1f)) {
        if (songs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 18.dp, end = 12.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.artwork))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                gradient.first().copy(alpha = 0f),
                                gradient.first().copy(alpha = 0.12f),
                                gradient.last().copy(alpha = 0.2f),
                            ),
                        ),
                    )
                    .blur(30.dp),
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(ElovaireRadii.artwork),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        ) {
            when {
                usesCollage -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        repeat(2) { rowIndex ->
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                repeat(2) { columnIndex ->
                                    val song = collageSongs.getOrNull((rowIndex * 2) + columnIndex)
                                    if (song != null) {
                                        ArtworkImage(
                                            uri = song.artUri,
                                            title = song.title,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize(),
                                            cornerRadius = 0.dp,
                                            requestedSizePx = 512,
                                        )
                                    } else {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                songs.isNotEmpty() -> {
                    ArtworkImage(
                        uri = coverSong?.artUri,
                        title = coverSong?.title ?: title,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = ElovaireRadii.artwork,
                        requestedSizePx = 384,
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_music),
                            contentDescription = title.ifBlank { copy.playlistArtworkPlaceholder },
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistNameDialog(
    title: String = rootUiCopy(AppLanguage.English).newPlaylist,
    confirmLabel: String = uiPhrase(AppLanguage.English, UiPhrase.Create),
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val canConfirm = name.trim().isNotBlank()
    val displayTitle = if (title == rootUiCopy(AppLanguage.English).newPlaylist) uiPhrase(language, UiPhrase.NewPlaylist) else title
    val displayConfirmLabel = when (confirmLabel) {
        rootUiCopy(AppLanguage.English).save,
        copy.save,
        -> copy.save
        uiPhrase(AppLanguage.English, UiPhrase.Create),
        uiPhrase(language, UiPhrase.Create),
        -> uiPhrase(language, UiPhrase.Create)
        else -> confirmLabel
    }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onDismiss) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .consumePointersWithoutSemantics(),
                shape = RoundedCornerShape(ElovaireRadii.card),
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(24f)),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PlaylistNameInputField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        requestFocusOnStart = true,
                        onImeDone = {
                            if (name.trim().isNotBlank()) {
                                onConfirm(name.trim())
                            }
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(language, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            onClick = { onConfirm(name.trim()) },
                            enabled = canConfirm,
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = if (canConfirm) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            contentColor = if (canConfirm) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                            },
                        ) {
                            Text(
                                text = displayConfirmLabel,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistNameInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = rootUiCopy(AppLanguage.English).playlistNamePlaceholder,
    requestFocusOnStart: Boolean = false,
    onImeDone: (() -> Unit)? = null,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val localizedPlaceholder = if (placeholder == rootUiCopy(AppLanguage.English).playlistNamePlaceholder) copy.playlistNamePlaceholder else placeholder
    val contentColor = MaterialTheme.colorScheme.onSurface
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val leadingIconAlpha by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = ElovaireMotion.standardTween(durationMillis = 80),
        label = "playlist_name_icon_alpha",
    )
    LaunchedEffect(requestFocusOnStart) {
        if (requestFocusOnStart) {
            withFrameNanos { }
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        shape = RoundedCornerShape(ElovaireRadii.input),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onImeDone?.invoke() }),
        placeholder = {
            Text(
                text = localizedPlaceholder,
                color = contentColor.copy(alpha = 0.44f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_pencil_line),
                contentDescription = null,
                tint = contentColor.copy(alpha = leadingIconAlpha),
                modifier = Modifier.size(16.dp),
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
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
                        .background(contentColor.copy(alpha = 0.1f))
                        .clickable(onClick = { onValueChange("") }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_x),
                        contentDescription = copy.clearPlaylistName,
                        tint = contentColor.copy(alpha = 0.86f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.38f),
            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            cursorColor = contentColor,
            focusedPlaceholderColor = contentColor.copy(alpha = 0.44f),
            unfocusedPlaceholderColor = contentColor.copy(alpha = 0.44f),
        ),
    )
}

@Composable
internal fun AddAlbumToPlaylistDialog(
    album: Album,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long, Album) -> Unit,
    onCreatePlaylist: (String) -> Long,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = album.title,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = { playlistId -> onPlaylistSelected(playlistId, album) },
        onCreatePlaylist = onCreatePlaylist,
    )
}

@Composable
internal fun PlaylistSelectionDialog(
    title: String,
    subtitle: String?,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
    onCreatePlaylist: ((String) -> Long)?,
) {
    val language = LocalAppLanguage.current
    val listState = rememberElovaireLazyListState(title, subtitle, "playlist_picker")
    var draftPlaylistName by rememberSaveable(title, subtitle) { mutableStateOf("") }
    var showInlineCreator by rememberSaveable(title, subtitle) { mutableStateOf(false) }
    var selectedPlaylistId by rememberSaveable(title, subtitle) { mutableStateOf<Long?>(null) }
    val visibleRows = 4
    val rowHeight = 82.dp
    val rowSpacing = 12.dp
    val listHeight = (rowHeight * visibleRows) + (rowSpacing * (visibleRows - 1))
    val displayedRows = remember(playlists, playlistSongsById) {
        buildPlaylistPickerRowModels(playlists, playlistSongsById)
    }

    LaunchedEffect(playlists) {
        if (selectedPlaylistId != null && playlists.none { it.id == selectedPlaylistId }) {
            selectedPlaylistId = null
        }
    }
    LaunchedEffect(showInlineCreator, displayedRows.size) {
        if (showInlineCreator) {
            listState.animateScrollToItem(displayedRows.size)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onDismiss) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .consumePointersWithoutSemantics(),
                shape = RoundedCornerShape(ElovaireRadii.card),
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateContentSize(animationSpec = ElovaireMotion.sizeSoft()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_list_plus),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = secondaryBodyTextStyle().copy(fontWeight = FontWeight.Medium),
                            color = readableSecondaryTextColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight),
                    ) {
                        if (displayedRows.isEmpty() && !showInlineCreator) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = uiPhrase(language, UiPhrase.NewPlaylist),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = readableSecondaryTextColor(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                overscrollEffect = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .ensureSingleItemRubberBand(listState),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing),
                            ) {
                                items(
                                    items = displayedRows,
                                    key = { it.playlist.id },
                                    contentType = { "playlist_picker_row" },
                                ) { row ->
                                    PlaylistPickerRow(
                                        playlist = row.playlist,
                                        previewSongs = row.previewSongs,
                                        durationMs = row.durationMs,
                                        selected = row.playlist.id == selectedPlaylistId,
                                        modifier = Modifier
                                            .elovaireListDropdownReveal(
                                                key = row.playlist.id,
                                                index = row.index,
                                            ),
                                        onClick = {
                                            selectedPlaylistId = if (selectedPlaylistId == row.playlist.id) null else row.playlist.id
                                        },
                                    )
                                }
                                if (showInlineCreator && onCreatePlaylist != null) {
                                    item(key = "inline_playlist_creator", contentType = { "playlist_inline_creator" }) {
                                        InlinePlaylistCreatorRow(
                                            name = draftPlaylistName,
                                            onNameChange = { draftPlaylistName = it },
                                            modifier = Modifier
                                                .elovaireListDropdownReveal(
                                                    key = "inline_playlist_creator",
                                                    index = displayedRows.size,
                                                ),
                                            onSave = {
                                                val trimmedName = draftPlaylistName.trim()
                                                if (trimmedName.isBlank()) return@InlinePlaylistCreatorRow
                                                val createdId = onCreatePlaylist(trimmedName)
                                                if (createdId > 0L) {
                                                    selectedPlaylistId = createdId
                                                    draftPlaylistName = ""
                                                    showInlineCreator = false
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (onCreatePlaylist != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(40.dp)
                                .align(Alignment.CenterHorizontally),
                            onClick = {
                                showInlineCreator = true
                                selectedPlaylistId = null
                            },
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiPhrase(language, UiPhrase.NewPlaylist),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(language, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = {
                                selectedPlaylistId?.let(onPlaylistSelected)
                            },
                            enabled = selectedPlaylistId != null,
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = if (selectedPlaylistId != null) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            contentColor = if (selectedPlaylistId != null) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                            },
                        ) {
                            Text(
                                text = uiPhrase(language, UiPhrase.AddToPlaylist),
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistPickerRow(
    playlist: Playlist,
    previewSongs: List<Song>,
    durationMs: Long,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val highlightColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "playlist_picker_row_highlight",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(highlightColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaylistArtworkPreview(
                songs = previewSongs,
                title = playlist.name,
                modifier = Modifier.size(62.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playlist.songIds.isEmpty()) {
                    Text(
                        text = copy.noSongsInPlaylistYet,
                        style = MaterialTheme.typography.labelLarge,
                        color = readableSecondaryTextColor().copy(alpha = 0.7f),
                    )
                } else {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append(formatCountLabel(playlist.songIds.size, "track"))
                            }
                            append("  •  ")
                            withStyle(SpanStyle(color = readableSecondaryTextColor().copy(alpha = 0.82f))) {
                                append(formatPlaylistDuration(durationMs))
                            }
                        },
                        style = secondaryBodyTextStyle().copy(fontSize = MaterialTheme.typography.labelLarge.fontSize),
                    )
                }
            }
            SelectionIndicatorIcon(
                selected = selected,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
    }
}

@Composable
internal fun InlinePlaylistCreatorRow(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val canSave = name.trim().isNotBlank()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaylistArtworkPreview(
                    songs = emptyList(),
                    title = uiPhrase(LocalAppLanguage.current, UiPhrase.NewPlaylist),
                    modifier = Modifier.size(62.dp),
                )
                PlaylistNameInputField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                    requestFocusOnStart = true,
                    onImeDone = {
                        if (name.trim().isNotBlank()) {
                            onSave()
                        }
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onSave,
                    enabled = canSave,
                    shape = RoundedCornerShape(ElovaireRadii.pill),
                    color = if (canSave) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    },
                    contentColor = if (canSave) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                    },
                ) {
                    Text(
                        text = copy.save,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddToPlaylistPickerDialog(
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
    onCreatePlaylist: ((String) -> Long)? = null,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = null,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = onPlaylistSelected,
        onCreatePlaylist = onCreatePlaylist,
    )
}
