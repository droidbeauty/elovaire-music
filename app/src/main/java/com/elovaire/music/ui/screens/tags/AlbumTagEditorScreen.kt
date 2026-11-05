package elovaire.music.droidbeauty.app.ui.screens.tags

import elovaire.music.droidbeauty.app.ui.i18n.localizedTagValidationError
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagSaveFailure
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagSaveStatus
import elovaire.music.droidbeauty.app.ui.i18n.localizedArtistName
import elovaire.music.droidbeauty.app.ui.i18n.localizedAlbumTitle

import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import android.net.Uri
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.artwork.decodeArtworkBytes
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmapAwaitingAdmission
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.LocalBackendResourceTracker
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkPaletteAccent
import androidx.palette.graphics.Palette
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.screens.DetailListTopBar
import elovaire.music.droidbeauty.app.ui.screens.FastScrollbar
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.EditorTextField
import elovaire.music.droidbeauty.app.ui.screens.ensureSingleItemRubberBand
import elovaire.music.droidbeauty.app.ui.screens.formatDuration
import elovaire.music.droidbeauty.app.ui.screens.sharedTopBarOccupiedHeight
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun AlbumTagEditorScreen(
    state: AlbumTagEditorUiState,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onPickCoverArt: () -> Unit,
    onAlbumTitleChange: (String) -> Unit,
    onAlbumArtistChange: (String) -> Unit,
    onReleaseYearChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onTrackTitleChange: (Long, String) -> Unit,
    onTrackArtistChange: (Long, String) -> Unit,
    onTrackNumberChange: (Long, String) -> Unit,
    onDiscNumberChange: (Long, String) -> Unit,
) {
    val album = state.originalAlbum
    val copy = remember(appLanguage) { tagEditorCopy(appLanguage) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val contentTop = sharedTopBarOccupiedHeight() + ElovaireSpacing.sectionVerticalGap
    var expandedTrackId by rememberSaveable(state.albumId) { mutableStateOf<Long?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.isLoading && album == null -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    album == null -> Text(
                        text = copy.albumNotFound,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    else -> {
                        LazyColumn(
                            state = listState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("album_tag_editor_list")
                                .ensureSingleItemRubberBand(listState),
                            contentPadding = PaddingValues(
                                start = ElovaireSpacing.screenHorizontalPadding,
                                top = contentTop,
                                end = ElovaireSpacing.screenHorizontalPadding,
                                bottom = ElovaireSpacing.sectionVerticalGap,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            item(key = "artwork", contentType = "artwork") {
                                val previewBitmap = rememberPreviewBitmap(state.selectedArtworkUri, state.selectedArtworkBytes)
                                val fallbackAccent by rememberArtworkPaletteAccent(album.artUri, TAG_EDITOR_PREVIEW_ARTWORK_SIZE_PX)
                                val previewAccent by produceState<Color?>(initialValue = null, key1 = previewBitmap) {
                                    value = previewBitmap?.let { bitmap ->
                                        withContext(Dispatchers.Default) {
                                            val original = bitmap.asAndroidBitmap()
                                            val software = if (original.config == Bitmap.Config.HARDWARE) {
                                                original.copy(Bitmap.Config.ARGB_8888, false)
                                            } else original
                                            software?.let {
                                                val palette = Palette.from(it).generate()
                                                (palette.darkMutedSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch)?.rgb?.let(::Color)
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(ElovaireSpacing.sectionVerticalGap),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    EditableArtworkCard(
                                        previewBitmap = previewBitmap,
                                        fallbackArtworkUri = album.artUri,
                                        title = copy.changeCoverHint,
                                        enabled = !state.isSaving,
                                        onClick = onPickCoverArt,
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
                                    ) {
                                        Text(
                                            text = localizedAlbumTitle(state.albumTitle.ifBlank { album.title }, appLanguage),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = localizedArtistName(state.albumArtist.ifBlank { album.artist }, appLanguage),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${localizedCountLabel(state.tracks.size, "song", appLanguage)} · ${formatDuration(album.durationMs)}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        )
                                        CoverArtIndicator(color = previewAccent ?: fallbackAccent ?: MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (state.saveFailures.isNotEmpty()) {
                                item(key = "feedback", contentType = "feedback") {
                                    ModuleCard {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.saveFailures.forEach { failure ->
                                                Text(
                                                    text = "${failure.fileName}: ${localizedTagSaveFailure(failure, appLanguage)}",
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item(key = "album_fields", contentType = "album_fields") {
                                ModuleCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        TagEditorSectionTitle(copy.albumSection, R.drawable.ic_lucide_disc_album)
                                        TagEditorTextField(
                                            value = state.albumTitle,
                                            onValueChange = onAlbumTitleChange,
                                            label = copy.albumTitle,
                                            enabled = !state.isSaving,
                                            isError = state.albumTitle.isBlank(),
                                        )
                                        TagEditorTextField(
                                            value = state.albumArtist,
                                            onValueChange = onAlbumArtistChange,
                                            label = copy.albumArtist,
                                            enabled = !state.isSaving,
                                            isError = state.albumArtist.isBlank(),
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            TagEditorTextField(
                                                value = state.releaseYear,
                                                onValueChange = onReleaseYearChange,
                                                label = copy.releaseYear,
                                                modifier = Modifier.weight(1f),
                                                enabled = !state.isSaving,
                                                keyboardType = KeyboardType.Number,
                                                isError = state.releaseYear.isNotBlank() && state.releaseYear.toIntOrNull() !in 1..9999,
                                            )
                                            TagEditorTextField(
                                                value = state.genre,
                                                onValueChange = onGenreChange,
                                                label = copy.genre,
                                                modifier = Modifier.weight(1f),
                                                enabled = !state.isSaving,
                                                imeAction = ImeAction.Done,
                                            )
                                        }
                                    }
                                }
                            }
                            item(key = "track_header", contentType = "section_title") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TagEditorSectionTitle(copy.songSection, R.drawable.ic_lucide_list_music)
                                    Text(
                                        text = state.tracks.size.toString(),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                            }
                            itemsIndexed(
                                items = state.tracks,
                                key = { _, track -> "track_${track.songId}" },
                                contentType = { _, _ -> "track_editor" },
                            ) { index, track ->
                                EditableTrackCard(
                                    index = index,
                                    track = track,
                                    copy = copy,
                                    appLanguage = appLanguage,
                                    expanded = expandedTrackId == track.songId,
                                    enabled = !state.isSaving,
                                    onToggle = {
                                        focusManager.clearFocus()
                                        keyboard?.hide()
                                        expandedTrackId = track.songId.takeUnless { it == expandedTrackId }
                                    },
                                    onTitleChange = { onTrackTitleChange(track.songId, it) },
                                    onArtistChange = { onTrackArtistChange(track.songId, it) },
                                    onTrackNumberChange = { onTrackNumberChange(track.songId, it) },
                                    onDiscNumberChange = { onDiscNumberChange(track.songId, it) },
                                )
                            }
                        }
                        FastScrollbar(
                            state = listState,
                            topInset = contentTop,
                            bottomInset = ElovaireSpacing.sectionVerticalGap,
                        )
                    }
                }
            }
            if (album != null) {
                AlbumTagEditorSaveAction(
                    label = rootUiCopy(appLanguage).save,
                    language = appLanguage,
                    state = state,
                    onSave = {
                        if (state.canSave && !state.isSaving) {
                            focusManager.clearFocus()
                            keyboard?.hide()
                            onSave()
                        }
                    },
                )
            }
        }
        DetailListTopBar(
            title = copy.editorTitle,
            subtitle = album?.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun TagEditorSectionTitle(title: String, iconResId: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EditableTrackCard(
    index: Int,
    track: EditableTrackTagState,
    copy: AlbumTagEditorCopy,
    appLanguage: AppLanguage,
    expanded: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onTitleChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onTrackNumberChange: (String) -> Unit,
    onDiscNumberChange: (String) -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = rememberElovaireInteractionSource()
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionSpecs.tween(MotionDuration.Fast),
        label = "track_editor_chevron",
    )
    val hasError = track.title.isBlank() || track.artist.isBlank() ||
        track.trackNumber.toIntOrNull() !in 1..9999 || track.discNumber.toIntOrNull() !in 1..9999

    ModuleCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("album_tag_editor_track_${track.songId}")
                    .elovaireClickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        onClickLabel = "${copy.editorTitle}: ${copy.track} ${index + 1}",
                        onClick = onToggle,
                    )
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasError) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = track.trackNumber.ifBlank { (index + 1).toString() },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
                ) {
                    Text(
                        text = track.title.ifBlank { track.originalTitle },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = localizedArtistName(track.artist.ifBlank { track.originalArtist }, appLanguage),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_chevron_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp).rotate(chevronRotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = motionSpecs.tween(MotionDuration.Standard),
                ) +
                    fadeIn(animationSpec = motionSpecs.tween(MotionDuration.Fast)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = motionSpecs.tween(MotionDuration.Standard),
                ) +
                    fadeOut(animationSpec = motionSpecs.tween(MotionDuration.Fast)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Text(
                        text = track.fileName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TagEditorTextField(
                        value = track.title,
                        onValueChange = onTitleChange,
                        label = copy.songTitle,
                        enabled = enabled,
                        isError = track.title.isBlank(),
                    )
                    TagEditorTextField(
                        value = track.artist,
                        onValueChange = onArtistChange,
                        label = copy.songArtist,
                        enabled = enabled,
                        isError = track.artist.isBlank(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TagEditorTextField(
                            value = track.trackNumber,
                            onValueChange = onTrackNumberChange,
                            label = copy.trackNumber,
                            modifier = Modifier.weight(1f),
                            enabled = enabled,
                            keyboardType = KeyboardType.Number,
                            isError = track.trackNumber.toIntOrNull() !in 1..9999,
                        )
                        TagEditorTextField(
                            value = track.discNumber,
                            onValueChange = onDiscNumberChange,
                            label = copy.discNumber,
                            modifier = Modifier.weight(1f),
                            enabled = enabled,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            isError = track.discNumber.toIntOrNull() !in 1..9999,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    val focusManager = LocalFocusManager.current
    EditorTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = label,
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() },
        ),
    )
}

@Composable
private fun AlbumTagEditorSaveAction(
    label: String,
    language: AppLanguage,
    state: AlbumTagEditorUiState,
    onSave: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    val enabled = state.canSave && !state.isSaving
    val emphasized = enabled || state.isSaving
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ElovaireSpacing.screenHorizontalPadding)
                .padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            (state.validationErrors.firstOrNull()?.let { localizedTagValidationError(it, language) }
                ?: state.statusMessage?.takeIf(String::isNotBlank)?.let { status ->
                    state.saveFailures.firstOrNull { it.reason == status }?.let { localizedTagSaveFailure(it, language) }
                        ?: localizedTagSaveStatus(status, state.saveFailures.size, language)
                })?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("album_tag_editor_save")
                    .heightIn(min = 52.dp)
                    .elovaireActionBump(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        label = "album_tag_editor_save",
                    ),
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (emphasized) 1f else 0.12f),
                contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                onClick = onSave,
                enabled = enabled,
                interactionSource = interactionSource,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_check),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = elovaireScaledSp(15f),
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableArtworkCard(
    previewBitmap: ImageBitmap?,
    fallbackArtworkUri: Uri?,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = Modifier
            .size(104.dp)
            .elovaireActionBump(
                enabled = enabled,
                interactionSource = interactionSource,
                label = "album_tag_editor_artwork",
            )
            .clip(RoundedCornerShape(ElovaireRadii.artwork))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = title,
                onClick = onClick,
            ),
    ) {
        Crossfade(
            targetState = previewBitmap,
            animationSpec = motionSpecs.tween(MotionDuration.Standard),
            label = "tag_editor_artwork_preview",
        ) { bitmap ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                ArtworkImage(
                    uri = fallbackArtworkUri,
                    title = title,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = ElovaireRadii.artwork,
                )
            }
        }
    }
}

@Composable
private fun CoverArtIndicator(color: Color) {
    Box(
        modifier = Modifier
            .padding(top = 5.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lucide_pencil_line),
            contentDescription = null,
            tint = if (color.luminance() > 0.179f) Color.Black else Color.White,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun rememberPreviewBitmap(
    selectedUri: Uri?,
    artworkBytes: ByteArray?,
): ImageBitmap? {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    val resourceTracker = LocalBackendResourceTracker.current
    LaunchedEffect(selectedUri, artworkBytes) {
        bitmap = withContext(Dispatchers.IO) {
            when {
                artworkBytes != null -> decodeArtworkBytes(artworkBytes, TAG_EDITOR_PREVIEW_ARTWORK_SIZE_PX)
                selectedUri != null -> loadArtworkBitmapAwaitingAdmission(
                    context,
                    selectedUri,
                    TAG_EDITOR_PREVIEW_ARTWORK_SIZE_PX,
                    resourceTracker = resourceTracker,
                )
                else -> null
            }?.also { decoded -> decoded.prepareToDraw() }?.asImageBitmap()
        }
    }
    return bitmap
}

private const val TAG_EDITOR_PREVIEW_ARTWORK_SIZE_PX = 256

internal data class AlbumTagEditorCopy(
    val editorTitle: String,
    val albumNotFound: String,
    val albumSection: String,
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: String,
    val genre: String,
    val changeCover: String,
    val changeCoverHint: String,
    val songSection: String,
    val track: String,
    val songTitle: String,
    val songArtist: String,
    val trackNumber: String,
    val discNumber: String,
)

internal fun tagEditorCopy(language: AppLanguage): AlbumTagEditorCopy {
    return when (language) {
        AppLanguage.Albanian -> AlbumTagEditorCopy("Redakto etiketat", "Albumi nuk u gjet", "Etiketat e albumit", "Titulli i albumit", "Artisti i albumit", "Viti i publikimit", "Zhanri", "Ndrysho kopertinën", "Prek për të zgjedhur një kopertinë të re", "Këngët", "Pista", "Titulli i këngës", "Artisti i këngës", "Numri i pistës", "Numri i diskut")
        AppLanguage.Estonian -> AlbumTagEditorCopy("Muuda silte", "Albumit ei leitud", "Albumi sildid", "Albumi pealkiri", "Albumi esitaja", "Ilmumisaasta", "Žanr", "Muuda kaanepilti", "Uue kaanepildi valimiseks puuduta", "Lood", "Lugu", "Loo pealkiri", "Loo esitaja", "Loo number", "Plaadi number")
        AppLanguage.Greek -> AlbumTagEditorCopy("Επεξεργασία ετικετών", "Το άλμπουμ δεν βρέθηκε", "Ετικέτες άλμπουμ", "Τίτλος άλμπουμ", "Καλλιτέχνης άλμπουμ", "Έτος κυκλοφορίας", "Είδος", "Αλλαγή εξωφύλλου", "Πατήστε για επιλογή νέου εξωφύλλου", "Τραγούδια", "Κομμάτι", "Τίτλος τραγουδιού", "Καλλιτέχνης τραγουδιού", "Αριθμός κομματιού", "Αριθμός δίσκου")
        AppLanguage.Hungarian -> AlbumTagEditorCopy("Címkék szerkesztése", "Az album nem található", "Albumcímkék", "Album címe", "Album előadója", "Megjelenés éve", "Műfaj", "Borító módosítása", "Koppints új borító kiválasztásához", "Dalok", "Szám", "Dal címe", "Dal előadója", "Szám sorszáma", "Lemez száma")
        AppLanguage.Latvian -> AlbumTagEditorCopy("Rediģēt tagus", "Albums nav atrasts", "Albuma tagi", "Albuma nosaukums", "Albuma izpildītājs", "Izdošanas gads", "Žanrs", "Mainīt vāciņu", "Pieskaries, lai izvēlētos jaunu vāciņu", "Dziesmas", "Celiņš", "Dziesmas nosaukums", "Dziesmas izpildītājs", "Celiņa numurs", "Diska numurs")
        AppLanguage.Lithuanian -> AlbumTagEditorCopy("Redaguoti žymas", "Albumas nerastas", "Albumo žymos", "Albumo pavadinimas", "Albumo atlikėjas", "Išleidimo metai", "Žanras", "Keisti viršelį", "Palieskite, kad pasirinktumėte naują viršelį", "Dainos", "Takelis", "Dainos pavadinimas", "Dainos atlikėjas", "Takelio numeris", "Disko numeris")
        AppLanguage.Macedonian -> AlbumTagEditorCopy("Уреди ознаки", "Албумот не е пронајден", "Ознаки на албумот", "Наслов на албумот", "Изведувач на албумот", "Година на издавање", "Жанр", "Смени насловна", "Допрете за да изберете нова насловна", "Песни", "Песна", "Наслов на песната", "Изведувач на песната", "Број на песната", "Број на дискот")
        AppLanguage.Serbian -> AlbumTagEditorCopy("Уреди ознаке", "Албум није пронађен", "Ознаке албума", "Наслов албума", "Извођач албума", "Година издања", "Жанр", "Промени омот", "Додирните да изаберете нови омот", "Песме", "Нумера", "Наслов песме", "Извођач песме", "Број нумере", "Број диска")
        AppLanguage.Thai -> AlbumTagEditorCopy("แก้ไขแท็ก", "ไม่พบอัลบั้ม", "แท็กอัลบั้ม", "ชื่ออัลบั้ม", "ศิลปินอัลบั้ม", "ปีที่เผยแพร่", "แนวเพลง", "เปลี่ยนปก", "แตะเพื่อเลือกภาพปกใหม่", "เพลง", "แทร็ก", "ชื่อเพลง", "ศิลปินเพลง", "หมายเลขแทร็ก", "หมายเลขแผ่น")
        AppLanguage.ChineseSimplified -> AlbumTagEditorCopy("编辑标签", "找不到专辑", "专辑标签", "专辑名称", "专辑艺人", "发行年份", "流派", "更换封面", "点击选择新的专辑封面", "歌曲", "曲目", "歌曲名称", "歌曲艺人", "曲目编号", "唱片编号")
        AppLanguage.Czech -> AlbumTagEditorCopy("Upravit značky", "Album nebylo nalezeno", "Značky alba", "Název alba", "Interpret alba", "Rok vydání", "Žánr", "Změnit obal", "Klepnutím vyberte nový obal alba", "Skladby", "Skladba", "Název skladby", "Interpret skladby", "Číslo skladby", "Číslo disku")
        AppLanguage.Danish -> AlbumTagEditorCopy("Rediger tags", "Album blev ikke fundet", "Albumtags", "Albumtitel", "Albumkunstner", "Udgivelsesår", "Genre", "Skift cover", "Tryk for at vælge et nyt albumcover", "Sange", "Spor", "Sangtitel", "Sangens kunstner", "Spornummer", "Disknummer")
        AppLanguage.Dutch -> AlbumTagEditorCopy("Tags bewerken", "Album niet gevonden", "Albumtags", "Albumtitel", "Albumartiest", "Jaar van uitgave", "Genre", "Omslag wijzigen", "Tik om een nieuwe albumhoes te kiezen", "Nummers", "Track", "Titel van nummer", "Artiest van nummer", "Tracknummer", "Schijfnummer")
        AppLanguage.Hindi -> AlbumTagEditorCopy("टैग संपादित करें", "एल्बम नहीं मिला", "एल्बम टैग", "एल्बम का शीर्षक", "एल्बम कलाकार", "रिलीज़ का वर्ष", "शैली", "कवर बदलें", "नया एल्बम कवर चुनने के लिए टैप करें", "गाने", "ट्रैक", "गाने का शीर्षक", "गाने का कलाकार", "ट्रैक नंबर", "डिस्क नंबर")
        AppLanguage.Japanese -> AlbumTagEditorCopy("タグを編集", "アルバムが見つかりません", "アルバムタグ", "アルバム名", "アルバムアーティスト", "リリース年", "ジャンル", "カバーを変更", "タップして新しいカバーを選択", "曲", "トラック", "曲名", "曲のアーティスト", "トラック番号", "ディスク番号")
        AppLanguage.Norwegian -> AlbumTagEditorCopy("Rediger tagger", "Albumet ble ikke funnet", "Albumtagger", "Albumtittel", "Albumartist", "Utgivelsesår", "Sjanger", "Endre omslag", "Trykk for å velge et nytt albumomslag", "Sanger", "Spor", "Sangtittel", "Sangens artist", "Spornummer", "Platenummer")
        AppLanguage.Swedish -> AlbumTagEditorCopy("Redigera taggar", "Albumet hittades inte", "Albumtaggar", "Albumtitel", "Albumartist", "Utgivningsår", "Genre", "Byt omslag", "Tryck för att välja ett nytt albumomslag", "Låtar", "Spår", "Låttitel", "Låtens artist", "Spårnummer", "Skivnummer")
        AppLanguage.French -> AlbumTagEditorCopy("Modifier les tags", "Album introuvable", "Tags de l’album", "Titre de l’album", "Artiste de l’album", "Année de sortie", "Genre", "Changer la pochette", "Appuyez pour choisir une nouvelle pochette", "Morceaux", "Piste", "Titre du morceau", "Artiste du morceau", "Numéro de piste", "Numéro de disque")
        AppLanguage.German -> AlbumTagEditorCopy("Tags bearbeiten", "Album nicht gefunden", "Album-Tags", "Albumtitel", "Albuminterpret", "Erscheinungsjahr", "Genre", "Cover ändern", "Tippen, um ein neues Cover auszuwählen", "Titel", "Titel", "Titelname", "Interpret", "Titelnummer", "CD-Nummer")
        AppLanguage.Italian -> AlbumTagEditorCopy("Modifica tag", "Album non trovato", "Tag dell’album", "Titolo dell’album", "Artista dell’album", "Anno di pubblicazione", "Genere", "Cambia copertina", "Tocca per scegliere una nuova copertina", "Brani", "Traccia", "Titolo del brano", "Artista del brano", "Numero traccia", "Numero disco")
        AppLanguage.Portuguese -> AlbumTagEditorCopy("Editar etiquetas", "Álbum não encontrado", "Etiquetas do álbum", "Título do álbum", "Artista do álbum", "Ano de lançamento", "Género", "Alterar capa", "Toque para escolher uma nova capa", "Músicas", "Faixa", "Título da música", "Artista da música", "Número da faixa", "Número do disco")
        AppLanguage.Russian -> AlbumTagEditorCopy("Редактировать теги", "Альбом не найден", "Теги альбома", "Название альбома", "Исполнитель альбома", "Год выпуска", "Жанр", "Изменить обложку", "Нажмите, чтобы выбрать новую обложку", "Песни", "Трек", "Название песни", "Исполнитель песни", "Номер трека", "Номер диска")
        AppLanguage.Spanish -> AlbumTagEditorCopy("Editar etiquetas", "Álbum no encontrado", "Etiquetas del álbum", "Título del álbum", "Artista del álbum", "Año de lanzamiento", "Género", "Cambiar portada", "Toca para elegir una nueva portada", "Canciones", "Pista", "Título de la canción", "Artista de la canción", "Número de pista", "Número de disco")
        AppLanguage.Ukrainian -> AlbumTagEditorCopy("Редагувати теги", "Альбом не знайдено", "Теги альбому", "Назва альбому", "Виконавець альбому", "Рік випуску", "Жанр", "Змінити обкладинку", "Натисніть, щоб вибрати нову обкладинку", "Пісні", "Трек", "Назва пісні", "Виконавець пісні", "Номер треку", "Номер диска")
        AppLanguage.Bulgarian -> AlbumTagEditorCopy("Редактирай етикетите", "Албумът не е намерен", "Етикети на албума", "Име на албума", "Изпълнител на албума", "Година на издаване", "Жанр", "Смени обложката", "Докоснете, за да изберете нова обложка", "Песни", "Песен", "Име на песента", "Изпълнител на песента", "Номер на песента", "Номер на диска")
        AppLanguage.Catalan -> AlbumTagEditorCopy("Edita les etiquetes", "No s’ha trobat l’àlbum", "Etiquetes de l’àlbum", "Títol de l’àlbum", "Artista de l’àlbum", "Any de publicació", "Gènere", "Canvia la portada", "Toca per triar una portada nova", "Cançons", "Pista", "Títol de la cançó", "Artista de la cançó", "Número de pista", "Número de disc")
        AppLanguage.Finnish -> AlbumTagEditorCopy("Muokkaa tunnisteita", "Albumia ei löytynyt", "Albumin tunnisteet", "Albumin nimi", "Albumin esittäjä", "Julkaisuvuosi", "Tyylilaji", "Vaihda kansikuva", "Valitse uusi kansikuva napauttamalla", "Kappaleet", "Kappale", "Kappaleen nimi", "Kappaleen esittäjä", "Kappaleen numero", "Levyn numero")
        AppLanguage.Romanian -> AlbumTagEditorCopy("Editează etichetele", "Albumul nu a fost găsit", "Etichetele albumului", "Titlul albumului", "Artistul albumului", "Anul lansării", "Gen", "Schimbă coperta", "Atinge pentru a alege o copertă nouă", "Melodii", "Piesă", "Titlul melodiei", "Artistul melodiei", "Numărul piesei", "Numărul discului")
        AppLanguage.Slovenian -> AlbumTagEditorCopy("Uredi oznake", "Albuma ni mogoče najti", "Oznake albuma", "Naslov albuma", "Izvajalec albuma", "Leto izdaje", "Žanr", "Zamenjaj naslovnico", "Tapnite za izbiro nove naslovnice", "Skladbe", "Skladba", "Naslov skladbe", "Izvajalec skladbe", "Številka skladbe", "Številka diska")
        AppLanguage.Bosnian -> AlbumTagEditorCopy("Uredi oznake", "Album nije pronađen", "Oznake albuma", "Naziv albuma", "Izvođač albuma", "Godina izdanja", "Žanr", "Promijeni omot", "Dodirnite da odaberete novi omot albuma", "Pjesme", "Pjesma", "Naziv pjesme", "Izvođač pjesme", "Broj pjesme", "Broj diska")
        AppLanguage.Indonesian -> AlbumTagEditorCopy("Edit tag", "Album tidak ditemukan", "Tag album", "Judul album", "Artis album", "Tahun rilis", "Genre", "Ganti sampul", "Ketuk untuk memilih sampul album baru", "Lagu", "Trek", "Judul lagu", "Artis lagu", "Nomor trek", "Nomor cakram")
        AppLanguage.Punjabi -> AlbumTagEditorCopy("ਟੈਗ ਸੋਧੋ", "ਐਲਬਮ ਨਹੀਂ ਮਿਲੀ", "ਐਲਬਮ ਦੇ ਟੈਗ", "ਐਲਬਮ ਦਾ ਸਿਰਲੇਖ", "ਐਲਬਮ ਕਲਾਕਾਰ", "ਰਿਲੀਜ਼ ਦਾ ਸਾਲ", "ਸ਼ੈਲੀ", "ਕਵਰ ਬਦਲੋ", "ਨਵਾਂ ਐਲਬਮ ਕਵਰ ਚੁਣਨ ਲਈ ਦਬਾਓ", "ਗੀਤ", "ਟ੍ਰੈਕ", "ਗੀਤ ਦਾ ਸਿਰਲੇਖ", "ਗੀਤ ਦਾ ਕਲਾਕਾਰ", "ਟ੍ਰੈਕ ਨੰਬਰ", "ਡਿਸਕ ਨੰਬਰ")
        AppLanguage.Polish -> AlbumTagEditorCopy("Edytuj tagi", "Nie znaleziono albumu", "Tagi albumu", "Tytuł albumu", "Artysta albumu", "Rok wydania", "Gatunek", "Zmień okładkę", "Dotknij, aby wybrać nową okładkę albumu", "Utwory", "Utwór", "Tytuł utworu", "Artysta utworu", "Numer ścieżki", "Numer dysku")
        AppLanguage.Slovak -> AlbumTagEditorCopy("Upraviť tagy", "Album sa nenašiel", "Tagy albumu", "Názov albumu", "Interpret albumu", "Rok vydania", "Žáner", "Zmeniť obal", "Ťuknutím vyberte nový obal albumu", "Skladby", "Skladba", "Názov skladby", "Interpret skladby", "Číslo stopy", "Číslo disku")
        AppLanguage.Croatian -> AlbumTagEditorCopy("Uredi tagove", "Album nije pronađen", "Tagovi albuma", "Naslov albuma", "Izvođač albuma", "Godina izdanja", "Žanr", "Promijeni omot", "Dodirnite za odabir novog omota albuma", "Pjesme", "Pjesma", "Naslov pjesme", "Izvođač pjesme", "Broj pjesme", "Broj diska")
        AppLanguage.Korean -> AlbumTagEditorCopy("태그 편집", "앨범을 찾을 수 없습니다", "앨범 태그", "앨범 제목", "앨범 아티스트", "발매 연도", "장르", "커버 변경", "탭하여 새 앨범 아트를 선택하세요", "곡", "트랙", "곡 제목", "곡 아티스트", "트랙 번호", "디스크 번호")
        AppLanguage.Malay -> AlbumTagEditorCopy("Edit tag", "Album tidak ditemui", "Tag album", "Tajuk album", "Artis album", "Tahun keluaran", "Genre", "Tukar kulit", "Ketik untuk memilih karya seni album baharu", "Lagu", "Runut", "Tajuk lagu", "Artis lagu", "Nombor runut", "Nombor cakera")
        AppLanguage.Bengali -> AlbumTagEditorCopy("ট্যাগ সম্পাদনা", "অ্যালবাম পাওয়া যায়নি", "অ্যালবাম ট্যাগ", "অ্যালবামের শিরোনাম", "অ্যালবাম শিল্পী", "প্রকাশের বছর", "ধরন", "কভার বদলান", "নতুন অ্যালবাম আর্ট বেছে নিতে ট্যাপ করুন", "গান", "ট্র্যাক", "গানের শিরোনাম", "গানের শিল্পী", "ট্র্যাক নম্বর", "ডিস্ক নম্বর")
        AppLanguage.Urdu -> AlbumTagEditorCopy("ٹیگز میں ترمیم کریں", "البم نہیں ملا", "البم ٹیگز", "البم کا عنوان", "البم آرٹسٹ", "اجرا کا سال", "صنف", "کور تبدیل کریں", "نیا البم آرٹ منتخب کرنے کے لیے ٹیپ کریں", "گانے", "ٹریک", "گانے کا عنوان", "گانے کا آرٹسٹ", "ٹریک نمبر", "ڈسک نمبر")
        AppLanguage.English -> AlbumTagEditorCopy("Edit tags", "Album not found", "Album tags", "Album title", "Album artist", "Release year", "Genre", "Change cover", "Tap to choose new album artwork", "Songs", "Track", "Song title", "Song artist", "Track number", "Disc number")
    }
}
