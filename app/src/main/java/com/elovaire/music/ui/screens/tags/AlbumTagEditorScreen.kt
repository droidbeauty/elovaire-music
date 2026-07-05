package elovaire.music.droidbeauty.app.ui.screens.tags

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun AlbumTagEditorScreen(
    state: AlbumTagEditorUiState,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAutoMatch: () -> Unit,
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
    if (state.isLoading && album == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        return
    }
    if (album == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tagEditorCopy(appLanguage).albumNotFound,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        return
    }

    val copy = remember(appLanguage) { tagEditorCopy(appLanguage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = editorTopBarHeight() + 16.dp,
                end = 18.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(ElovaireRadii.module),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                EditableArtworkCard(
                                    artworkUri = state.selectedArtworkUri,
                                    artworkBytes = state.selectedArtworkBytes,
                                    fallbackArtworkUri = album.artUri,
                                    title = state.albumTitle,
                                    onClick = onPickCoverArt,
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = copy.albumSection,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = copy.changeCoverHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                                    )
                                    AccentPillButton(
                                        label = copy.changeCover,
                                        iconResId = R.drawable.ic_lucide_disc_album,
                                        onClick = onPickCoverArt,
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = state.albumTitle,
                                onValueChange = onAlbumTitleChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(copy.albumTitle) },
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = state.albumArtist,
                                onValueChange = onAlbumArtistChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(copy.albumArtist) },
                                singleLine = true,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedTextField(
                                    value = state.releaseYear,
                                    onValueChange = onReleaseYearChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text(copy.releaseYear) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                OutlinedTextField(
                                    value = state.genre,
                                    onValueChange = onGenreChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text(copy.genre) },
                                    singleLine = true,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(ElovaireRadii.module),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.36f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = copy.autoMatchTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = copy.autoMatchSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                                    )
                                }
                                AccentPillButton(
                                    label = copy.findOnline,
                                    iconResId = R.drawable.ic_lucide_search,
                                    enabled = !state.isMatchingOnline && !state.isSaving,
                                    loading = state.isMatchingOnline,
                                    onClick = onAutoMatch,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = copy.songSection,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }

            itemsIndexed(state.tracks, key = { _, track -> track.songId }) { index, track ->
                Surface(
                    shape = RoundedCornerShape(ElovaireRadii.card),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = elovaireScaledSp(12f),
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            ) {
                                Text(
                                    text = track.fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                )
                            }
                        }

                        OutlinedTextField(
                            value = track.title,
                            onValueChange = { value -> onTrackTitleChange(track.songId, value) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(copy.songTitle) },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = track.artist,
                            onValueChange = { value -> onTrackArtistChange(track.songId, value) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(copy.songArtist) },
                            singleLine = true,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = track.trackNumber,
                                onValueChange = { value -> onTrackNumberChange(track.songId, value) },
                                modifier = Modifier.weight(1f),
                                label = { Text(copy.trackNumber) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            OutlinedTextField(
                                value = track.discNumber,
                                onValueChange = { value -> onDiscNumberChange(track.songId, value) },
                                modifier = Modifier.weight(1f),
                                label = { Text(copy.discNumber) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = !state.statusMessage.isNullOrBlank(),
                    enter = fadeIn(animationSpec = ElovaireMotion.standardTween(durationMillis = 120)),
                    exit = fadeOut(animationSpec = ElovaireMotion.standardTween(durationMillis = 120)),
                ) {
                    Text(
                        text = state.statusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }
            }
        }
        TagEditorFastScrollbar(
            state = listState,
            topInset = editorTopBarHeight() + 16.dp,
            bottomInset = 28.dp,
        )

        AlbumTagEditorTopBar(
            title = copy.editorTitle,
            subtitle = album.title,
            onBack = onBack,
            onAutoMatch = onAutoMatch,
            onSave = onSave,
            matching = state.isMatchingOnline,
            saving = state.isSaving,
            saveEnabled = state.canSave,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun EditableArtworkCard(
    artworkUri: Uri?,
    artworkBytes: ByteArray?,
    fallbackArtworkUri: Uri?,
    title: String,
    onClick: () -> Unit,
) {
    val previewBitmap = rememberPreviewBitmap(artworkUri, artworkBytes)
    Surface(
        modifier = Modifier
            .size(126.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.24f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                previewBitmap != null -> {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                else -> {
                    ArtworkImage(
                        uri = fallbackArtworkUri,
                        title = title,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = ElovaireRadii.module,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)),
            )
        }
    }
}

@Composable
private fun rememberPreviewBitmap(
    selectedUri: Uri?,
    artworkBytes: ByteArray?,
): ImageBitmap? {
    var bitmap by remember(selectedUri, artworkBytes) { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(selectedUri, artworkBytes) {
        bitmap = when {
            artworkBytes != null -> BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size)?.asImageBitmap()
            selectedUri != null -> runCatching {
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
            else -> null
        }
    }
    return bitmap
}

@Composable
private fun AlbumTagEditorTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onAutoMatch: () -> Unit,
    onSave: () -> Unit,
    matching: Boolean,
    saving: Boolean,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (darkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.86f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditorTopBarIconButton(
                iconResId = R.drawable.ic_lucide_chevron_left,
                contentDescription = "Back",
                onClick = onBack,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            EditorTopBarIconButton(
                iconResId = R.drawable.ic_lucide_search,
                contentDescription = "Find online",
                onClick = onAutoMatch,
                enabled = !matching && !saving,
                loading = matching,
            )
            EditorTopBarIconButton(
                iconResId = R.drawable.ic_lucide_check,
                contentDescription = "Save",
                onClick = onSave,
                enabled = !matching && !saving && saveEnabled,
                loading = saving,
            )
        }
    }
}

@Composable
private fun EditorTopBarIconButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = Modifier
            .size(40.dp)
            .elovairePressScale(
                enabled = enabled && !loading,
                pressedScale = 0.88f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "editorTopBarActionScale",
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun AccentPillButton(
    label: String,
    iconResId: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Surface(
        modifier = Modifier.elovairePressScale(
            enabled = enabled && !loading,
            pressedScale = 0.94f,
            animationSpec = ElovaireMotion.releaseSpringSpec(),
            interactionSource = interactionSource,
            label = "tagEditorPillScale",
        ),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = RoseAccent.copy(alpha = if (enabled) 1f else 0.55f),
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = elovaireScaledSp(15f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun editorTopBarHeight() = ElovaireSpacing.topBarContentHeight + 28.dp

private data class TagEditorScrollbarMetrics(
    val scrollFraction: Float,
    val visibleFraction: Float,
    val totalItems: Int,
    val visibleItemsCount: Int,
)

@Composable
private fun BoxScope.TagEditorFastScrollbar(
    state: LazyListState,
    topInset: Dp,
    bottomInset: Dp,
) {
    val metrics by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || totalItems <= visibleItems.size) {
                null
            } else {
                val viewportHeightPx =
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
                val averageItemHeightPx = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
                val estimatedContentHeightPx = max(viewportHeightPx, averageItemHeightPx * totalItems)
                val scrollableContentHeightPx = max(estimatedContentHeightPx - viewportHeightPx, 1f)
                val currentScrollPx =
                    (state.firstVisibleItemIndex * averageItemHeightPx + state.firstVisibleItemScrollOffset)
                        .coerceAtLeast(0f)
                TagEditorScrollbarMetrics(
                    scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f),
                    visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
                    totalItems = totalItems,
                    visibleItemsCount = visibleItems.size,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return
    TagEditorFastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        visibleFraction = resolvedMetrics.visibleFraction,
        totalItems = resolvedMetrics.totalItems,
        visibleItemsCount = resolvedMetrics.visibleItemsCount,
        topInset = topInset,
        bottomInset = bottomInset,
        isScrollInProgress = state.isScrollInProgress,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleIndex = (resolvedMetrics.totalItems - resolvedMetrics.visibleItemsCount).coerceAtLeast(0)
            val targetIndex = (maxFirstVisibleIndex * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleIndex)
            state.scrollToItem(targetIndex)
        },
    )
}

@Composable
private fun BoxScope.TagEditorFastScrollbarTrack(
    scrollFraction: Float,
    visibleFraction: Float,
    totalItems: Int,
    visibleItemsCount: Int,
    topInset: Dp,
    bottomInset: Dp,
    isScrollInProgress: Boolean,
    onJumpToFraction: suspend (Float) -> Unit,
) {
    if (totalItems <= visibleItemsCount) return

    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var dragFraction by remember { mutableFloatStateOf(scrollFraction.coerceIn(0f, 1f)) }
    var lastRequestedFraction by remember { mutableFloatStateOf(-1f) }
    val colors = MaterialTheme.colorScheme
    val trackColor = colors.fastScrollbarTrackColor()
    val thumbColor = colors.fastScrollbarThumbColor()
    val motionSpecs = rememberMotionSpecs()
    val animatedScrollFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) dragFraction.coerceIn(0f, 1f) else scrollFraction.coerceIn(0f, 1f),
        animationSpec = motionSpecs.tween(
            durationMillis = if (isDragging) 50 else 90,
        ),
        label = "tag_editor_fast_scrollbar_fraction",
    )
    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            visible = true
        } else {
            delay(2000L)
            visible = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .zIndex(3f)
            .fillMaxHeight()
            .padding(top = topInset, end = 3.dp, bottom = bottomInset)
            .width(28.dp),
    ) {
        val density = LocalDensity.current
        val trackHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val thumbHeightPx = max(with(density) { 40.dp.toPx() }, trackHeightPx * visibleFraction)
        val trackTravelPx = max(trackHeightPx - thumbHeightPx, 1f)
        val thumbOffsetPx = trackTravelPx * animatedScrollFraction
        val fractionForPosition: (Float) -> Float = { y -> (y / trackHeightPx).coerceIn(0f, 1f) }
        val jumpToFraction: (Float) -> Unit = { fraction ->
            val normalized = fraction.coerceIn(0f, 1f)
            dragFraction = normalized
            if (abs(normalized - lastRequestedFraction) >= 0.0025f) {
                lastRequestedFraction = normalized
                scrollJob?.cancel()
                scrollJob = scope.launch {
                    onJumpToFraction(normalized)
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = motionSpecs.tween(120)),
            exit = fadeOut(animationSpec = motionSpecs.tween(220)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalItems, visibleItemsCount, trackTravelPx, thumbHeightPx) {
                        detectTapGestures { offset ->
                            isDragging = true
                            jumpToFraction(fractionForPosition(offset.y))
                            isDragging = false
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(totalItems, visibleItemsCount, trackTravelPx, thumbHeightPx) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    jumpToFraction(fractionForPosition(offset.y))
                                },
                                onVerticalDrag = { change, _ ->
                                    change.consume()
                                    jumpToFraction(fractionForPosition(change.position.y))
                                },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false },
                            )
                        },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .width(2.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(trackColor),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = with(density) { thumbOffsetPx.toDp() })
                        .width(5.dp)
                        .height(with(density) { thumbHeightPx.toDp() })
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(thumbColor),
                )
            }
        }
    }
}

private fun ColorScheme.fastScrollbarTrackColor(): Color {
    return if (background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        InkText.copy(alpha = 0.12f)
    }
}

private fun ColorScheme.fastScrollbarThumbColor(): Color {
    return if (background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.78f)
    } else {
        InkText.copy(alpha = 0.72f)
    }
}

private data class AlbumTagEditorCopy(
    val editorTitle: String,
    val albumNotFound: String,
    val albumSection: String,
    val albumTitle: String,
    val albumArtist: String,
    val releaseYear: String,
    val genre: String,
    val changeCover: String,
    val changeCoverHint: String,
    val autoMatchTitle: String,
    val autoMatchSubtitle: String,
    val findOnline: String,
    val songSection: String,
    val track: String,
    val songTitle: String,
    val songArtist: String,
    val trackNumber: String,
    val discNumber: String,
)

private fun tagEditorCopy(language: AppLanguage): AlbumTagEditorCopy {
    return when (language) {
        AppLanguage.Polish -> AlbumTagEditorCopy("Edytuj tagi", "Nie znaleziono albumu", "Tagi albumu", "Tytuł albumu", "Artysta albumu", "Rok wydania", "Gatunek", "Zmień okładkę", "Dotknij, aby wybrać nową okładkę albumu", "Dopasowanie online", "Wyszukaj metadane albumu i utworów online", "Znajdź online", "Utwory", "Utwór", "Tytuł utworu", "Artysta utworu", "Numer ścieżki", "Numer dysku")
        AppLanguage.Slovak -> AlbumTagEditorCopy("Upraviť tagy", "Album sa nenašiel", "Tagy albumu", "Názov albumu", "Interpret albumu", "Rok vydania", "Žáner", "Zmeniť obal", "Ťuknutím vyberte nový obal albumu", "Online zhoda", "Nájdite online metadáta albumu a skladieb", "Nájsť online", "Skladby", "Skladba", "Názov skladby", "Interpret skladby", "Číslo stopy", "Číslo disku")
        AppLanguage.Croatian -> AlbumTagEditorCopy("Uredi tagove", "Album nije pronađen", "Tagovi albuma", "Naslov albuma", "Izvođač albuma", "Godina izdanja", "Žanr", "Promijeni omot", "Dodirnite za odabir novog omota albuma", "Online podudaranje", "Pronađi online metapodatke albuma i pjesama", "Pronađi online", "Pjesme", "Pjesma", "Naslov pjesme", "Izvođač pjesme", "Broj pjesme", "Broj diska")
        AppLanguage.Korean -> AlbumTagEditorCopy("태그 편집", "앨범을 찾을 수 없습니다", "앨범 태그", "앨범 제목", "앨범 아티스트", "발매 연도", "장르", "커버 변경", "탭하여 새 앨범 아트를 선택하세요", "온라인 매칭", "앨범과 곡 메타데이터를 온라인에서 찾습니다", "온라인에서 찾기", "곡", "트랙", "곡 제목", "곡 아티스트", "트랙 번호", "디스크 번호")
        AppLanguage.Malay -> AlbumTagEditorCopy("Edit tag", "Album tidak ditemui", "Tag album", "Tajuk album", "Artis album", "Tahun keluaran", "Genre", "Tukar kulit", "Ketik untuk memilih karya seni album baharu", "Padanan dalam talian", "Cari metadata album dan lagu dalam talian", "Cari dalam talian", "Lagu", "Runut", "Tajuk lagu", "Artis lagu", "Nombor runut", "Nombor cakera")
        AppLanguage.Bengali -> AlbumTagEditorCopy("ট্যাগ সম্পাদনা", "অ্যালবাম পাওয়া যায়নি", "অ্যালবাম ট্যাগ", "অ্যালবামের শিরোনাম", "অ্যালবাম শিল্পী", "প্রকাশের বছর", "ধরন", "কভার বদলান", "নতুন অ্যালবাম আর্ট বেছে নিতে ট্যাপ করুন", "অনলাইন মিল", "অনলাইনে অ্যালবাম ও গানের মেটাডেটা খুঁজুন", "অনলাইনে খুঁজুন", "গান", "ট্র্যাক", "গানের শিরোনাম", "গানের শিল্পী", "ট্র্যাক নম্বর", "ডিস্ক নম্বর")
        AppLanguage.Urdu -> AlbumTagEditorCopy("ٹیگز میں ترمیم کریں", "البم نہیں ملا", "البم ٹیگز", "البم کا عنوان", "البم آرٹسٹ", "اجرا کا سال", "صنف", "کور تبدیل کریں", "نیا البم آرٹ منتخب کرنے کے لیے ٹیپ کریں", "آن لائن مطابقت", "البم اور گانوں کی میٹا ڈیٹا آن لائن تلاش کریں", "آن لائن تلاش کریں", "گانے", "ٹریک", "گانے کا عنوان", "گانے کا آرٹسٹ", "ٹریک نمبر", "ڈسک نمبر")
        else -> AlbumTagEditorCopy("Edit tags", "Album not found", "Album tags", "Album title", "Album artist", "Release year", "Genre", "Change cover", "Tap to choose new album artwork", "Online match", "Find album and track metadata online", "Find online", "Songs", "Track", "Song title", "Song artist", "Track number", "Disc number")
    }
}
