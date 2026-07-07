package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.playback.PlaybackProgressConsumer
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkBitmap
import elovaire.music.droidbeauty.app.ui.components.rememberArtworkGradient
import elovaire.music.droidbeauty.app.ui.interaction.CompactBarGestureActions
import elovaire.music.droidbeauty.app.ui.interaction.compactBarGestures
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionTransitions
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ForceDarkColorScheme
import elovaire.music.droidbeauty.app.ui.theme.InkText
import kotlinx.coroutines.delay

@Composable
internal fun CompactNowPlayingDockHost(
    viewModel: NowPlayingViewModel,
    song: Song,
    transportShowsPause: Boolean,
    visible: Boolean,
    suppressEnterAnimation: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var keepProgressActive by remember(song.id) { mutableStateOf(visible) }
    val progressState = remember(song.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(visible, song.id) {
        if (visible) {
            keepProgressActive = true
        } else {
            delay(ElovaireMotion.Standard.toLong().coerceAtLeast(120L))
            keepProgressActive = false
        }
    }
    DisposableEffect(viewModel, keepProgressActive) {
        viewModel.setProgressConsumerActive(PlaybackProgressConsumer.CompactDock, keepProgressActive)
        onDispose {
            viewModel.setProgressConsumerActive(PlaybackProgressConsumer.CompactDock, false)
        }
    }

    if (keepProgressActive) {
        LaunchedEffect(viewModel, song.id) {
            viewModel.progressState().collect { playbackProgress ->
                progressState.floatValue = if (playbackProgress.durationMs > 0L) {
                    (playbackProgress.displayPositionMs.toFloat() / playbackProgress.durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
        }
    }

    StandaloneNowPlayingDock(
        song = song,
        isPlaying = transportShowsPause,
        progressState = progressState,
        visible = visible,
        suppressEnterAnimation = suppressEnterAnimation,
        onOpenPlayer = onOpenPlayer,
        onTogglePlayback = viewModel::togglePlayback,
        onSkipPrevious = viewModel::skipPrevious,
        onSkipNext = viewModel::skipNext,
        modifier = modifier,
    )
}

@Composable
private fun StandaloneNowPlayingDock(
    song: Song,
    isPlaying: Boolean,
    progressState: MutableFloatState,
    visible: Boolean,
    suppressEnterAnimation: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionTransitions = rememberMotionTransitions()
    val artwork = rememberArtworkBitmap(song.artUri, size = 768)
    val gradient = rememberArtworkGradient(song.artUri).value
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseTint = if (darkTheme) Color(0xFF141414).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.82f)
    val albumTint = gradient.first().copy(alpha = 0.5f)
    val resolvedSurface = albumTint.compositeOver(baseTint)
    val contentColor = if (resolvedSurface.luminance() > 0.42f) InkText else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.72f)
    ForceDarkColorScheme {
        ElovaireAnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = motionTransitions.compactBarEnter(suppressEnterAnimation),
            exit = motionTransitions.compactBarExit(),
            label = "CompactNowPlayingDockVisibility",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ElovaireRadii.card))
                    .background(baseTint)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.05f else 0.04f),
                        shape = RoundedCornerShape(ElovaireRadii.card),
                    ),
            ) {
                val artworkBitmap = artwork.value
                if (artworkBitmap != null) {
                    Image(
                        bitmap = artworkBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .blur(48.dp),
                        alpha = 0.9f,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(albumTint),
                )
                NowPlayingBar(
                    song = song,
                    isPlaying = isPlaying,
                    progressState = progressState,
                    visible = visible,
                    onOpenPlayer = onOpenPlayer,
                    onTogglePlayback = onTogglePlayback,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                )
            }
        }
    }
}

@Composable
private fun NowPlayingBar(
    song: Song,
    isPlaying: Boolean,
    progressState: MutableFloatState,
    visible: Boolean,
    onOpenPlayer: (NowPlayingTransitionSnapshot?) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
) {
    val barGradient = rememberArtworkGradient(song.artUri).value
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val controlBaseTint = if (darkTheme) {
        barGradient.last().copy(alpha = 0.28f).compositeOver(Color.Black.copy(alpha = 0.16f))
    } else {
        barGradient.last().copy(alpha = 0.22f).compositeOver(Color.White.copy(alpha = 0.16f))
    }
    val controlTint by animateColorAsState(
        targetValue = controlBaseTint,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerButtonTint",
    )
    val controlIconTint by animateColorAsState(
        targetValue = if (controlTint.luminance() > 0.42f) InkText else Color.White,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerButtonIconTint",
    )
    val resolvedPrimaryTextColor by animateColorAsState(
        targetValue = controlIconTint,
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerTextPrimary",
    )
    val resolvedSecondaryTextColor by animateColorAsState(
        targetValue = controlIconTint.copy(alpha = 0.72f),
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerTextSecondary",
    )
    val compactControlBackground by animateColorAsState(
        targetValue = resolvedPrimaryTextColor.copy(alpha = 0.2f),
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "MiniPlayerControlBackground",
    )
    val interactionSource = rememberElovaireInteractionSource()
    var dragOffsetX by remember(song.id) { mutableFloatStateOf(0f) }
    var barBounds by remember(song.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var artworkBounds by remember(song.id) { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val animatedDragOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = ElovaireMotion.releaseSpringSpec(
            dampingRatio = 0.82f,
            stiffness = 380f,
        ),
        label = "MiniPlayerDragOffsetX",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.card))
            .onGloballyPositioned { barBounds = it.boundsInRoot() }
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.08f else 0.05f),
                shape = RoundedCornerShape(ElovaireRadii.card),
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 66.dp)
                .compactBarGestures(
                    enabled = visible,
                    actions = CompactBarGestureActions(
                        onTap = {
                            val validSnapshot = if (
                                barBounds != null &&
                                artworkBounds != null &&
                                barBounds!!.isValidTransitionBounds &&
                                artworkBounds!!.isValidTransitionBounds
                            ) {
                                NowPlayingTransitionSnapshot(
                                    songId = song.id,
                                    barBounds = barBounds!!,
                                    artworkBounds = artworkBounds!!,
                                )
                            } else {
                                null
                            }
                            onOpenPlayer(validSnapshot)
                        },
                        onSwipePrevious = onSkipPrevious,
                        onSwipeNext = onSkipNext,
                        onDragDelta = { delta ->
                            dragOffsetX = (dragOffsetX + delta).coerceIn(-160f, 160f)
                        },
                        onGestureFinished = {
                            dragOffsetX = 0f
                        },
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationX = animatedDragOffsetX * 0.18f },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    uri = song.artUri,
                    title = song.title,
                    modifier = Modifier
                        .size(48.dp)
                        .onGloballyPositioned { artworkBounds = it.boundsInRoot() },
                    cornerRadius = ElovaireRadii.artworkSmall,
                    requestedSizePx = 192,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        color = resolvedPrimaryTextColor,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                            repeatDelayMillis = 2500,
                            initialDelayMillis = 2500,
                            velocity = 24.dp,
                        ),
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.labelLarge,
                        color = resolvedSecondaryTextColor,
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .elovairePressScale(
                        enabled = visible,
                        pressedScale = 0.9f,
                        animationSpec = ElovaireMotion.chromeReleaseSpec(),
                        interactionSource = interactionSource,
                        label = "MiniPlayerPlayButtonScale",
                    )
                    .clip(CircleShape)
                    .background(compactControlBackground)
                    .clickable(
                        enabled = visible,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTogglePlayback,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier.matchParentSize(),
                ) {
                    val strokeWidth = size.minDimension * 0.1f
                    val arcInset = strokeWidth / 2f + 2.2f
                    val arcStart = -90f
                    val arcSweep = 360f
                    drawArc(
                        color = controlIconTint.copy(alpha = 0.18f),
                        startAngle = arcStart,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = controlIconTint,
                        startAngle = arcStart,
                        sweepAngle = arcSweep * progressState.floatValue.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = Size(size.width - (arcInset * 2f), size.height - (arcInset * 2f)),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        (
                            fadeIn(animationSpec = ElovaireMotion.iconSwapInSpec()) +
                                scaleIn(
                                    initialScale = 0.9f,
                                    animationSpec = ElovaireMotion.releaseSpringSpec(),
                                )
                            ) togetherWith
                            (
                                fadeOut(animationSpec = ElovaireMotion.iconSwapOutSpec()) +
                                    scaleOut(
                                        targetScale = 1.04f,
                                        animationSpec = ElovaireMotion.contentFadeOutSpec(),
                                    )
                                )
                    },
                    label = "mini_player_play_pause_icon",
                ) { playing ->
                    Icon(
                        painter = painterResource(
                            id = if (playing) R.drawable.ic_lucide_pause else R.drawable.ic_lucide_play,
                        ),
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = controlIconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
