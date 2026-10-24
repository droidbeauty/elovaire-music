package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.screens.common.secondaryBodyTextStyle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.chrisbanes.haze.HazeState
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
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.MotionEasing
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.motion.MotionVisibilityHost
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import androidx.compose.foundation.gestures.detectTapGestures
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import kotlinx.coroutines.launch

@Composable
internal fun PlaylistArtworkPreview(
    songs: List<Song>,
    title: String,
    modifier: Modifier = Modifier,
    placeholderIconSize: Dp = 20.dp,
    cornerRadius: Dp = ElovaireRadii.artworkSmall,
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
                    .clip(RoundedCornerShape(cornerRadius))
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
            shape = RoundedCornerShape(cornerRadius),
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
                        cornerRadius = cornerRadius,
                        requestedSizePx = 384,
                        placeholderIconSize = placeholderIconSize,
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
                            modifier = Modifier.size(placeholderIconSize),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistNameDialog(
    visible: Boolean,
    surfaceId: String,
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
    var mounted by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) mounted = true
    }
    if (mounted) Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onDismiss) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center,
        ) {
            PopupCardMotionHost(
                surfaceId = surfaceId,
                visible = visible,
                onExitFinished = { mounted = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 20.dp),
            ) {
                DynamicBackdropSurface(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        val confirmInteractionSource = rememberElovaireInteractionSource()
                        Surface(
                            modifier = Modifier.elovaireActionBump(
                                enabled = canConfirm,
                                interactionSource = confirmInteractionSource,
                                label = "playlist_create_confirm_bump",
                            ),
                            onClick = { onConfirm(name.trim()) },
                            enabled = canConfirm,
                            interactionSource = confirmInteractionSource,
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
}

@Composable
internal fun PlaylistCreateFullScreenLayer(
    visible: Boolean,
    surfaceId: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onCreatePlaylist: PlaylistCreateAction,
    onCreated: (Long?) -> Unit,
    onExitFinished: (() -> Unit)? = null,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val transitions = rememberMotionTransitions()
    val motionSpecs = rememberMotionSpecs()
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(surfaceId) { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mounted by remember { mutableStateOf(visible) }
    val hasName = name.trim().isNotEmpty()
    val canCreate = hasName && !isCreating
    val backdropAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motionSpecs.tween(
            durationMillis = if (visible) MotionDuration.FullScreenEnter else MotionDuration.FullScreenExit,
            easing = if (visible) MotionEasing.FadeIn else MotionEasing.FadeOut,
        ),
        label = "playlist_create_backdrop_alpha",
    )

    fun submitPlaylist() {
        if (!canCreate) return
        val playlistName = name.trim()
        isCreating = true
        errorMessage = null
        scope.launch {
            try {
                when (val result = onCreatePlaylist(playlistName).await()) {
                    is PlaylistMutationResult.Success -> onCreated(result.playlistId)
                    is PlaylistMutationResult.Failure -> errorMessage = result.reason
                    else -> Unit
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                errorMessage = failure.localizedMessage
            } finally {
                isCreating = false
            }
        }
    }

    LaunchedEffect(visible) {
        if (visible) mounted = true
    }
    BackHandler(enabled = visible) {
        if (!isCreating) onDismiss()
    }

    if (mounted) {
        Box(
            modifier = modifier
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background.copy(alpha = backdropAlpha))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Final).changes.forEach { it.consume() }
                        }
                    }
                },
        ) {
            MotionVisibilityHost(
                visible = visible,
                surfaceId = surfaceId,
                modifier = Modifier.fillMaxSize(),
                enter = transitions.fullScreenForwardEnter(),
            exit = transitions.fullScreenBackExit(),
                onExitFinished = {
                    mounted = false
                    name = ""
                    errorMessage = null
                    onExitFinished?.invoke()
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(top = topBarOccupiedHeight() + 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        val previewName = name.trim().ifEmpty { copy.newPlaylist }
                        val previewShape = RoundedCornerShape(ElovaireRadii.module)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(previewShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.56f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = previewShape,
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PlaylistArtworkPreview(
                                songs = emptyList(),
                                title = previewName,
                                modifier = Modifier.size(88.dp),
                                placeholderIconSize = 24.dp,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AnimatedContent(
                                    targetState = previewName,
                                    transitionSpec = { transitions.titleSwapTransform() },
                                    label = "playlist_create_name_preview",
                                ) { displayedName ->
                                    Text(
                                        text = displayedName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = copy.noSongsInPlaylistYet,
                                    style = secondaryBodyTextStyle().copy(fontWeight = FontWeight.Medium),
                                    color = readableSecondaryTextColor(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        PlaylistNameInputField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            requestFocusOnStart = visible,
                            enabled = !isCreating,
                            onImeDone = ::submitPlaylist,
                        )
                        AnimatedVisibility(visible = errorMessage != null) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    val createInteractionSource = rememberElovaireInteractionSource()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(bottom = 20.dp)
                            .height(56.dp)
                            .elovaireActionBump(
                                enabled = canCreate,
                                interactionSource = createInteractionSource,
                                label = "playlist_create_fullscreen_bump",
                            ),
                        onClick = ::submitPlaylist,
                        enabled = canCreate,
                        shape = RoundedCornerShape(ElovaireRadii.pill),
                        color = if (hasName) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        },
                        contentColor = if (hasName) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        },
                        interactionSource = createInteractionSource,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = copy.createPlaylistButton,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
                PinnedBackTopBar(
                    title = copy.newPlaylist,
                    onBack = { if (!isCreating) onDismiss() },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
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
    enabled: Boolean = true,
    onImeDone: (() -> Unit)? = null,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    val localizedPlaceholder = if (placeholder == rootUiCopy(AppLanguage.English).playlistNamePlaceholder) copy.playlistNamePlaceholder else placeholder
    val contentColor = MaterialTheme.colorScheme.onSurface
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val motionSpecs = rememberMotionSpecs()
    val leadingIconAlpha by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = motionSpecs.tween(durationMillis = MotionDuration.Micro),
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
        enabled = enabled,
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
                enter = fadeIn(animationSpec = motionSpecs.fadeIn()) +
                    scaleIn(
                        animationSpec = motionSpecs.tween(easing = MotionEasing.SoftOut),
                        initialScale = 0.92f,
                    ),
                exit = fadeOut(animationSpec = motionSpecs.fadeOut()) +
                    scaleOut(
                        animationSpec = motionSpecs.fadeOut(),
                        targetScale = 0.92f,
                    ),
            ) {
                val clearInteractionSource = rememberElovaireInteractionSource()
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.1f))
                        .elovaireActionBump(
                            enabled = enabled,
                            interactionSource = clearInteractionSource,
                            label = "playlist_name_clear_bump",
                        )
                        .clickable(
                            enabled = enabled,
                            interactionSource = clearInteractionSource,
                            indication = null,
                            onClick = { onValueChange("") },
                        ),
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
    onPlaylistSelected: (Long, Album) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        visible = true,
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = album.title,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = { playlistId -> onPlaylistSelected(playlistId, album).await() },
        onCreatePlaylist = onCreatePlaylist,
    )
}

@Composable
internal fun PlaylistSelectionDialog(
    visible: Boolean,
    title: String,
    subtitle: String?,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: suspend (Long) -> PlaylistMutationResult,
    onCreatePlaylist: PlaylistCreateAction?,
    hazeState: HazeState? = null,
) {
    val revealRegistry = rememberMotionRevealRegistry()
    val language = LocalAppLanguage.current
    val motionSpecs = rememberMotionSpecs()
    val listState = rememberElovaireLazyListState(title, subtitle, "playlist_picker")
    val createPlaylist = onCreatePlaylist
    var showPlaylistCreator by rememberSaveable(title, subtitle) { mutableStateOf(false) }
    var playlistCreatorMounted by remember { mutableStateOf(false) }
    var selectedPlaylistId by rememberSaveable(title, subtitle) { mutableStateOf<Long?>(null) }
    var isAddingToPlaylist by rememberSaveable(title, subtitle) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
    var mounted by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) mounted = true
    }
    if (mounted) Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onDismiss) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center,
        ) {
            PopupCardMotionHost(
                surfaceId = "playlists.add_songs",
                visible = visible,
                onExitFinished = { mounted = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                DynamicBackdropSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .consumePointersWithoutSemantics(),
                    shape = RoundedCornerShape(ElovaireRadii.card),
                    overlayAlpha = 0.6f,
                    borderColor = blurSurfaceBorderColor(),
                    hazeState = hazeState,
                ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateContentSize(animationSpec = motionSpecs.contentSize()),
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
                        if (displayedRows.isEmpty()) {
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
                                            .elovaireListReveal(
                                                itemKey = row.playlist.id,
                                                index = row.index,
                                                registry = revealRegistry,
                                            ),
                                        onClick = {
                                            selectedPlaylistId = if (selectedPlaylistId == row.playlist.id) null else row.playlist.id
                                        },
                                    )
                                }
                            }
                        }
                    }
                    if (createPlaylist != null) {
                        val newPlaylistInteractionSource = rememberElovaireInteractionSource()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(40.dp)
                                .align(Alignment.CenterHorizontally)
                                .elovaireActionBump(
                                    interactionSource = newPlaylistInteractionSource,
                                    label = "playlist_new_playlist_bump",
                                ),
                            onClick = {
                                showPlaylistCreator = true
                                playlistCreatorMounted = true
                                selectedPlaylistId = null
                            },
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                            interactionSource = newPlaylistInteractionSource,
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
                        val addToPlaylistInteractionSource = rememberElovaireInteractionSource()
                        Surface(
                            modifier = Modifier.elovaireActionBump(
                                enabled = selectedPlaylistId != null && !isAddingToPlaylist,
                                interactionSource = addToPlaylistInteractionSource,
                                label = "playlist_add_to_playlist_bump",
                            ),
                            onClick = {
                                val playlistId = selectedPlaylistId ?: return@Surface
                                if (!isAddingToPlaylist) {
                                    isAddingToPlaylist = true
                                    scope.launch {
                                        when (onPlaylistSelected(playlistId)) {
                                            is PlaylistMutationResult.Success -> onDismiss()
                                            else -> Unit
                                        }
                                        isAddingToPlaylist = false
                                    }
                                }
                            },
                            enabled = selectedPlaylistId != null && !isAddingToPlaylist,
                            interactionSource = addToPlaylistInteractionSource,
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
    if (playlistCreatorMounted && createPlaylist != null) {
        Dialog(
            onDismissRequest = { showPlaylistCreator = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            PlaylistCreateFullScreenLayer(
                visible = showPlaylistCreator,
                surfaceId = "playlists.picker_create_screen",
                modifier = Modifier.fillMaxSize(),
                onDismiss = { showPlaylistCreator = false },
                onCreatePlaylist = createPlaylist,
                onCreated = { playlistId ->
                    playlistId?.let { selectedPlaylistId = it }
                    showPlaylistCreator = false
                },
                onExitFinished = { playlistCreatorMounted = false },
            )
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
    val motionSpecs = rememberMotionSpecs()
    val highlightColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = motionSpecs.tween(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.SoftOut,
        ),
        label = "playlist_picker_row_highlight",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(highlightColor)
            .elovaireClickable(onClick = onClick)
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
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = readableSecondaryTextColor().copy(alpha = 0.7f),
                    )
                } else {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append(formatCountLabel(playlist.songIds.size, "track", language))
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
internal fun AddToPlaylistPickerDialog(
    visible: Boolean,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    onDismiss: () -> Unit,
    onPlaylistSelected: suspend (Long) -> PlaylistMutationResult,
    onCreatePlaylist: PlaylistCreateAction? = null,
    hazeState: HazeState? = null,
) {
    val language = LocalAppLanguage.current
    PlaylistSelectionDialog(
        visible = visible,
        title = uiPhrase(language, UiPhrase.AddToPlaylist),
        subtitle = null,
        playlists = playlists,
        playlistSongsById = playlistSongsById,
        onDismiss = onDismiss,
        onPlaylistSelected = onPlaylistSelected,
        onCreatePlaylist = onCreatePlaylist,
        hazeState = hazeState,
    )
}
