package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.SleepTimerOption
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.audiobookCopy
import elovaire.music.droidbeauty.app.ui.i18n.sleepTimerCopy
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.MutedSectionHeader
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
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
    val progressFraction = remember(book, progress) {
        progress?.let { saved ->
            val partIndex = book.parts.indexOfFirst { it.song.id == saved.songId }
            if (partIndex < 0 || book.durationMs <= 0L) {
                0f
            } else {
                val elapsed = book.parts.take(partIndex).sumOf { it.durationMs.coerceAtLeast(0L) } +
                    saved.positionMs.coerceAtLeast(0L).coerceAtMost(book.parts[partIndex].durationMs.coerceAtLeast(0L))
                (elapsed.toFloat() / book.durationMs.toFloat()).coerceIn(0f, 1f)
            }
        } ?: 0f
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
    Column(modifier = Modifier.fillMaxSize()) {
        DetailListTopBar(
            title = copy.allAudiobooks,
            subtitle = null,
            onBack = onBack,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomPadding + 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(books, key = Audiobook::stableKey, contentType = { "audiobook_row" }) { book ->
                AudiobookCollectionRow(book = book, onClick = { onBookSelected(book) })
            }
        }
    }
}

@Composable
private fun AudiobookCollectionRow(
    book: Audiobook,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ElovaireRadii.card),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(
                uri = book.artUri,
                title = book.title,
                modifier = Modifier.size(width = 86.dp, height = 114.dp),
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
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Suppress("LongMethod")
@Composable
internal fun AudiobookDetailScreen(
    book: Audiobook,
    rewindSeconds: Int,
    forwardSeconds: Int,
    currentSongId: Long?,
    progressMs: Long,
    savedProgress: AudiobookProgress?,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onPlay: (AudiobookPart, Boolean) -> Unit,
    onStartOver: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    sleepTimerOption: SleepTimerOption,
    onSleepTimerSelected: (SleepTimerOption) -> Unit,
) {
    val copy = audiobookCopy(LocalAppLanguage.current)
    val motionTransitions = rememberMotionTransitions()
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val activeSongId = currentSongId?.takeIf { id -> book.parts.any { it.song.id == id } }
        ?: savedProgress?.songId
    val activePositionMs = if (activeSongId == currentSongId) progressMs else savedProgress?.positionMs ?: 0L
    val currentPart = book.parts.withIndex()
        .firstOrNull { (_, part) ->
            part.song.id == activeSongId &&
                (part.startMs == null || activePositionMs < (part.endMs ?: Long.MAX_VALUE))
        }
        ?.index
        ?: book.parts.indexOfLast { it.song.id == activeSongId }.takeIf { it >= 0 }
    val currentPartDuration = currentPart?.let { book.parts[it].durationMs } ?: 0L
    val currentPartOffsetMs = currentPart?.let { index ->
        val part = book.parts[index]
        (activePositionMs - (part.startMs ?: 0L)).coerceAtLeast(0L)
            .coerceAtMost(part.durationMs.coerceAtLeast(0L))
    } ?: 0L
    val elapsedBookMs = currentPart?.let { index ->
        book.parts.take(index).sumOf { it.durationMs.coerceAtLeast(0L) } + currentPartOffsetMs
    } ?: 0L
    val bookProgress = if (book.durationMs > 0L) {
        (elapsedBookMs.toFloat() / book.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isCompleted = savedProgress?.completed == true && activeSongId != currentSongId
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding + 20.dp),
    ) {
        item(key = "audiobook_detail_topbar") {
            DetailListTopBar(title = book.title, subtitle = book.author, onBack = onBack)
        }
        item(key = "audiobook_detail_hero") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ArtworkImage(
                    uri = book.artUri,
                    title = book.title,
                    modifier = Modifier.size(width = 190.dp, height = 254.dp),
                    cornerRadius = ElovaireRadii.artwork,
                    requestedSizePx = 768,
                    showArtworkGlow = true,
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = elovaireScaledSp(24f),
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(text = book.author, style = MaterialTheme.typography.bodyLarge, color = readableSecondaryTextColor())
                if (currentPart != null && (currentPartDuration > 0L || book.durationMs > 0L)) {
                    androidx.compose.material3.LinearProgressIndicator(
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
            ModuleCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                onPlay(
                                    if (isCompleted) book.parts.first() else {
                                        book.parts.getOrNull(currentPart ?: 0) ?: book.parts.first()
                                    },
                                    !isCompleted,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (isCompleted) copy.playAgain else if (currentPart != null) copy.resume else copy.play)
                        }
                        Button(
                            onClick = onStartOver,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) { Text(copy.startOver) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(onClick = onSeekBack, enabled = currentPart != null && activeSongId == currentSongId) { Text("−${rewindSeconds}s") }
                        Button(onClick = onSeekForward, enabled = currentPart != null && activeSongId == currentSongId) { Text("+${forwardSeconds}s") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(copy.speed, style = MaterialTheme.typography.labelLarge, color = readableSecondaryTextColor())
                        listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            Button(
                                onClick = { onSetSpeed(speed) },
                                contentPadding = PaddingValues(horizontal = 10.dp),
                            ) { Text("${speed}x") }
                        }
                    }
                    Button(
                        onClick = { showSleepTimerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(sleepTimerCopy(LocalAppLanguage.current).title)
                    }
                }
            }
        }
        item(key = "audiobook_detail_chapters") {
            Text(
                text = copy.chapters,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
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
    ElovaireAnimatedVisibility(
        visible = showSleepTimerDialog,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f),
        enter = motionTransitions.overlayFadeEnter(initialAlpha = 0.86f),
        exit = motionTransitions.overlayFadeExit(targetAlpha = 0.94f),
        label = "AudiobookSleepTimerSheetOverlay",
    ) {
        SleepTimerDialog(
            selectedOption = sleepTimerOption,
            visible = showSleepTimerDialog,
            onOptionSelected = { option ->
                onSleepTimerSelected(option)
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false },
        )
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
