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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.resolveAudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.audiobookPartPrefixDurations
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.audiobookCopy
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.MutedSectionHeader
import elovaire.music.droidbeauty.app.ui.screens.common.readableMutedIconColor
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

@Composable
internal fun AudiobookMiniGallery(
    books: List<Audiobook>,
    progressByBookKey: Map<String, AudiobookProgress> = emptyMap(),
    currentSongId: Long? = null,
    onOpenCollection: () -> Unit,
    onBookSelected: (Audiobook) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (books.isEmpty()) return
    val copy = audiobookCopy(LocalAppLanguage.current)
    ModuleCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MutedSectionHeader(
                title = copy.title,
                iconResId = R.drawable.ic_lucide_book_audio,
                onClick = onOpenCollection,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(
                    items = books,
                    key = Audiobook::stableKey,
                    contentType = { "audiobook_mini_card" },
                ) { book ->
                    AudiobookMiniCard(
                        book = book,
                        progress = progressByBookKey[book.stableKey],
                        isCurrentlyPlaying = currentSongId != null && book.parts.any { it.song.id == currentSongId },
                        onClick = { onBookSelected(book) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AudiobookMiniCard(
    book: Audiobook,
    progress: AudiobookProgress?,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
) {
    val copy = audiobookCopy(LocalAppLanguage.current)
    val partPrefixDurations = remember(book) { audiobookPartPrefixDurations(book.parts) }
    val progressFraction = remember(book, progress) {
        resolveAudiobookProgress(
            book = book,
            savedProgress = progress,
            currentSongId = null,
            currentPositionMs = 0L,
            partPrefixDurations = partPrefixDurations,
        ).progressFraction
    }
    Column(
        modifier = Modifier
            .width(124.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        ArtworkImage(
            uri = book.artUri,
            title = book.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            cornerRadius = ElovaireRadii.artwork,
            requestedSizePx = 320,
            showArtworkGlow = true,
        )
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.labelLarge,
            color = readableSecondaryTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (book.parts.size == 1) {
                formatDuration(book.durationMs)
            } else {
                "${book.parts.size} ${copy.parts}  •  ${formatDuration(book.durationMs)}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = readableSecondaryTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        when {
            progress?.completed == true -> Text(copy.completed, style = MaterialTheme.typography.labelSmall, color = readableSecondaryTextColor())
            isCurrentlyPlaying -> Text(copy.listening, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            progressFraction > 0f -> {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(copy.continueListening, style = MaterialTheme.typography.labelSmall, color = readableSecondaryTextColor())
            }
        }
    }
}

@Composable
internal fun AudiobooksScreen(
    books: List<Audiobook>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onBookSelected: (Audiobook) -> Unit,
) {
    val copy = audiobookCopy(LocalAppLanguage.current)
    var layoutModeName by rememberSaveable { mutableStateOf(AlbumLayoutMode.Compact.name) }
    val layoutMode = remember(layoutModeName) {
        AlbumLayoutMode.entries.firstOrNull { it.name == layoutModeName } ?: AlbumLayoutMode.Compact
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val gridState = rememberElovaireLazyGridState("audiobooks_screen_grid")
    Box(modifier = Modifier.fillMaxSize()) {
        when (layoutMode) {
            AlbumLayoutMode.Compact -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(listState),
                    overscrollEffect = null,
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                        end = 20.dp,
                        bottom = bottomPadding,
                    ),
                ) {
                    item(key = "audiobooks_view_switcher") {
                        AudiobooksViewSwitcher(
                            layoutMode = layoutMode,
                            onLayoutModeChanged = { layoutModeName = it.name },
                        )
                    }
                    item(key = "audiobooks_view_switcher_gap") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    itemsIndexed(
                        items = books,
                        key = { _, book -> book.stableKey },
                        contentType = { _, _ -> "audiobook_row" },
                    ) { index, book ->
                        AudiobookCollectionRow(book = book, onClick = { onBookSelected(book) })
                        if (index != books.lastIndex) {
                            DividerLine()
                        }
                    }
                }
                FastScrollbar(
                    state = listState,
                    topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
                    bottomInset = bottomPadding + 16.dp,
                )
            }

            AlbumLayoutMode.Grid -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .ensureSingleItemRubberBand(gridState),
                    overscrollEffect = null,
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = detailTopBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                        end = 20.dp,
                        bottom = bottomPadding,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(
                        key = "audiobooks_view_switcher",
                        span = { GridItemSpan(2) },
                    ) {
                        AudiobooksViewSwitcher(
                            layoutMode = layoutMode,
                            onLayoutModeChanged = { layoutModeName = it.name },
                        )
                    }
                    items(
                        items = books,
                        key = Audiobook::stableKey,
                        contentType = { "audiobook_grid_card" },
                    ) { book ->
                        AudiobookGridCard(book = book, onClick = { onBookSelected(book) })
                    }
                }
                FastScrollbar(
                    state = gridState,
                    topInset = detailTopBarOccupiedHeight() + ElovaireSpacing.detailCompactTopGap,
                    bottomInset = bottomPadding + 16.dp,
                )
            }

            AlbumLayoutMode.DenseGrid -> Unit
        }
        DetailListTopBar(
            title = copy.allAudiobooks,
            subtitle = null,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun AudiobooksViewSwitcher(
    layoutMode: AlbumLayoutMode,
    onLayoutModeChanged: (AlbumLayoutMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        ToggleIconChip(
            iconResId = R.drawable.ic_lucide_list,
            selected = layoutMode == AlbumLayoutMode.Compact,
            contentDescription = "List",
            onClick = { onLayoutModeChanged(AlbumLayoutMode.Compact) },
        )
        Spacer(modifier = Modifier.width(8.dp))
        ToggleIconChip(
            iconResId = R.drawable.ic_lucide_grid_2x2,
            selected = layoutMode == AlbumLayoutMode.Grid,
            contentDescription = "Grid",
            onClick = { onLayoutModeChanged(AlbumLayoutMode.Grid) },
        )
    }
}

@Composable
internal fun AudiobookUnavailableScreen(
    bottomPadding: Dp,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailListTopBar(title = "Audiobook unavailable", subtitle = null, onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "This audiobook is no longer available",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "Refresh your library and try again",
                    style = MaterialTheme.typography.bodyMedium,
                    color = readableSecondaryTextColor(),
                )
            }
        }
    }
}

@Composable
private fun AudiobookCollectionRow(
    book: Audiobook,
    onClick: () -> Unit,
) {
    val artworkAspectRatio = audiobookArtworkAspectRatio(book)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = book.artUri,
                title = book.title,
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(artworkAspectRatio),
                cornerRadius = ElovaireRadii.artworkSmall,
                requestedSizePx = 384,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = book.author, style = MaterialTheme.typography.bodyMedium, color = readableSecondaryTextColor())
                Text(
                    text = "${book.parts.size} ${audiobookCopy(LocalAppLanguage.current).parts}  •  ${formatDuration(book.durationMs)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = readableSecondaryTextColor(),
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_lucide_chevron_left),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180f),
            )
        }
    }
}

@Composable
private fun AudiobookGridCard(
    book: Audiobook,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            uri = book.artUri,
            title = book.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(audiobookArtworkAspectRatio(book)),
            cornerRadius = ElovaireRadii.artwork,
            requestedSizePx = 384,
        )
        Column(
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Suppress("LongMethod")
@Composable
internal fun AudiobookDetailScreen(
    book: Audiobook,
    currentSongId: Long?,
    progressMs: Long,
    savedProgress: AudiobookProgress?,
    descriptionState: AudiobookDescriptionLoadState,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onPlay: (AudiobookPart, Boolean) -> Unit,
    onStartOver: () -> Unit,
) {
    if (book.parts.isEmpty()) {
        AudiobookUnavailableScreen(bottomPadding = bottomPadding, onBack = onBack)
        return
    }
    val copy = audiobookCopy(LocalAppLanguage.current)
    var showDescriptionDialog by remember { mutableStateOf(false) }
    val description = (descriptionState as? AudiobookDescriptionLoadState.Loaded)?.text
    val descriptionPreview = remember(description) { audiobookDescriptionPreview(description) }
    val partPrefixDurations = remember(book.parts) { audiobookPartPrefixDurations(book.parts) }
    val resolvedProgress = remember(book, savedProgress, currentSongId, progressMs, partPrefixDurations) {
        resolveAudiobookProgress(
            book = book,
            savedProgress = savedProgress,
            currentSongId = currentSongId,
            currentPositionMs = progressMs,
            partPrefixDurations = partPrefixDurations,
        )
    }
    val activeSongId = resolvedProgress.songId
    val currentPart = resolvedProgress.partIndex
    val currentPartDuration = currentPart?.let { book.parts.getOrNull(it)?.durationMs } ?: 0L
    val elapsedBookMs = resolvedProgress.bookElapsedMs
    val bookProgress = resolvedProgress.progressFraction
    val isCompleted = resolvedProgress.completed && activeSongId != currentSongId
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = detailTopBarOccupiedHeight() + 20.dp,
                bottom = bottomPadding + 20.dp,
            ),
        ) {
        item(key = "audiobook_detail_hero") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    uri = book.artUri,
                    title = book.title,
                    modifier = Modifier
                        .width(80.dp)
                        .aspectRatio(audiobookArtworkAspectRatio(book)),
                    cornerRadius = ElovaireRadii.artworkSmall,
                    requestedSizePx = 384,
                    showArtworkGlow = true,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = readableSecondaryTextColor(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        item(key = "audiobook_detail_progress") {
            if (currentPart != null && (currentPartDuration > 0L || book.durationMs > 0L)) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { bookProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatDuration(elapsedBookMs), style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
                        Text(formatDuration((book.durationMs - elapsedBookMs).coerceAtLeast(0L)), style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
                    }
                }
            }
        }
        item(key = "audiobook_detail_controls") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AudiobookActionButton(
                    text = if (isCompleted) copy.playAgain else if (currentPart != null) copy.resume else copy.play,
                    iconResId = R.drawable.ic_lucide_circle_play,
                    tint = Color.White,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val selectedPart = if (isCompleted) {
                            book.parts.firstOrNull()
                        } else {
                            book.parts.getOrNull(currentPart ?: 0) ?: book.parts.firstOrNull()
                        }
                        selectedPart?.let { onPlay(it, !isCompleted) }
                    },
                )
                AudiobookActionButton(
                    text = copy.startOver,
                    iconResId = R.drawable.ic_lucide_rotate_ccw,
                    tint = MaterialTheme.colorScheme.onSurface,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f),
                    onClick = onStartOver,
                )
            }
        }
        item(key = "audiobook_detail_description") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (description != null) showDescriptionDialog = true },
                    )
                    .padding(start = 20.dp, top = 12.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AudiobookSectionHeader(
                    title = "About this book",
                    iconResId = R.drawable.ic_lucide_info,
                    onTrailingClick = { if (description != null) showDescriptionDialog = true },
                )
                Text(
                    text = when (descriptionState) {
                        AudiobookDescriptionLoadState.Loading -> "Loading description…"
                        AudiobookDescriptionLoadState.Unavailable -> "No description available"
                        is AudiobookDescriptionLoadState.Loaded -> descriptionPreview.text
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = readableSecondaryTextColor(),
                )
                if (descriptionPreview.hasMore) {
                    Button(
                        onClick = { showDescriptionDialog = true },
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "MORE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
        item(key = "audiobook_detail_section_separator") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            )
        }
        item(key = "audiobook_detail_chapters") {
            AudiobookSectionHeader(
                title = copy.chapters,
                iconResId = R.drawable.ic_lucide_book_open_text,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 8.dp),
            )
        }
        itemsIndexed(
            items = book.parts,
            key = { _, part -> "${part.song.id}:${part.number}:${part.startMs ?: -1L}" },
            contentType = { _, _ -> "audiobook_part" },
        ) { index, part ->
            AudiobookPartRow(
                part = part,
                number = part.number,
                selected = index == currentPart,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                onClick = { onPlay(part, false) },
            )
        }
        }
        DetailListTopBar(
            title = book.title,
            subtitle = book.author,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
    AudiobookDescriptionDialog(
        text = description,
        visible = showDescriptionDialog && description != null,
        onDismiss = { showDescriptionDialog = false },
    )
}

@Composable
private fun audiobookArtworkAspectRatio(book: Audiobook): Float {
    val bitmap = rememberArtworkBitmap(uri = book.artUri, size = 384).value
    return remember(bitmap) {
        bitmap
            ?.let { image -> image.width.toFloat() / image.height.toFloat() }
            ?.takeIf { it.isFinite() && it > 0f }
            ?.coerceIn(0.6f, 1.6f)
            ?: 0.75f
    }
}

private data class AudiobookDescriptionPreview(
    val text: String,
    val hasMore: Boolean,
)

private fun audiobookDescriptionPreview(description: String?): AudiobookDescriptionPreview {
    val normalized = description.orEmpty().trim()
    if (normalized.isBlank()) return AudiobookDescriptionPreview("No description available", false)
    val sentences = normalized.split(Regex("(?<=[.!?])\\s+")).filter(String::isNotBlank)
    val preview = sentences.take(5).joinToString(" ").ifBlank { normalized }
    return AudiobookDescriptionPreview(
        text = preview,
        hasMore = sentences.size > 5,
    )
}

@Composable
private fun AudiobookSectionHeader(
    title: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = readableMutedIconColor(),
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            )
        }
        if (onTrailingClick != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTrailingClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_ellipsis),
                    contentDescription = "More about this book",
                    tint = readableMutedIconColor(),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AudiobookActionButton(
    text: String,
    iconResId: Int,
    tint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Surface(
        modifier = modifier.elovaireActionBump(
            interactionSource = interactionSource,
            label = "audiobook_${text}_button_bump",
        ),
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AudiobookDescriptionDialog(
    text: String?,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (text == null) return
    BackHandler(enabled = visible, onBack = onDismiss)
    val motionTransitions = rememberMotionTransitions()
    ElovaireAnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f),
        enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.86f),
        exit = motionTransitions.overlayFadeExit(targetAlpha = 0.94f),
        label = "AudiobookDescriptionDialogOverlay",
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.46f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            PopupCardMotionHost(
                visible = visible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.62f),
            ) {
                DynamicBackdropSurface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = ElovaireRadii.card, topEnd = ElovaireRadii.card),
                    overlayAlpha = 0.6f,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = navigationBarInsetDp() + 20.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "About this book",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onDismiss,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_x),
                                    contentDescription = "Close description",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        )
                        Text(
                            text = text,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                                .verticalScroll(rememberScrollState()),
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudiobookPartRow(
    part: AudiobookPart,
    number: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.card))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = number.toString(), style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = part.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = formatDuration(part.durationMs), style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
        }
        if (selected) {
            Icon(painter = painterResource(R.drawable.ic_lucide_play), contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
