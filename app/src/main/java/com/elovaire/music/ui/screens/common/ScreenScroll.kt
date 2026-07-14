package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.InkText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private data class FastScrollbarMetrics(
    val scrollFraction: Float,
    val viewportFraction: Float,
    val totalItems: Int,
    val estimatedUnits: Int,
    val scrollableContentPx: Float,
    val beforeContentPaddingPx: Int,
    val estimator: LazyItemSizeEstimator,
)

internal val FastScrollbarTouchWidth = 40.dp
internal val FastScrollbarEdgePadding = 0.dp
internal val FastScrollbarTrackWidth = 1.dp
internal val FastScrollbarThumbWidth = 3.dp
internal val FastScrollbarMinThumbHeight = 40.dp

@Composable
internal fun BoxScope.FastScrollbar(
    state: LazyListState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    val estimator = remember(state) { LazyItemSizeEstimator() }
    val metrics by remember(state, estimator) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || (!state.canScrollBackward && !state.canScrollForward)) {
                null
            } else {
                estimator.prepare(totalItems, layoutInfo.mainAxisItemSpacing)
                visibleItems.forEach { estimator.record(it.index, it.size, it.key) }
                val viewportPx = layoutInfo.viewportSize.height.toFloat().coerceAtLeast(1f)
                val contentPx = (
                    estimator.estimatedTotalPx() +
                        layoutInfo.beforeContentPadding +
                        layoutInfo.afterContentPadding
                    ).coerceAtLeast(viewportPx)
                val scrollablePx = (contentPx - viewportPx).coerceAtLeast(1f)
                val estimatedScrollPx = estimator.estimatedPrefixPx(state.firstVisibleItemIndex) +
                    state.firstVisibleItemScrollOffset
                val fraction = when {
                    !state.canScrollBackward -> 0f
                    !state.canScrollForward -> 1f
                    else -> estimatedScrollPx / scrollablePx
                }
                FastScrollbarMetrics(
                    scrollFraction = fraction.coerceIn(0f, 1f),
                    viewportFraction = (viewportPx / contentPx).coerceIn(0f, 1f),
                    totalItems = totalItems,
                    estimatedUnits = totalItems,
                    scrollableContentPx = scrollablePx,
                    beforeContentPaddingPx = layoutInfo.beforeContentPadding,
                    estimator = estimator,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        viewportFraction = resolvedMetrics.viewportFraction,
        totalItems = resolvedMetrics.totalItems,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        isScrollInProgress = state.isScrollInProgress,
        onJumpToFraction = { fraction ->
            val position = resolvedMetrics.estimator.positionForOffset(
                (resolvedMetrics.scrollableContentPx * fraction - resolvedMetrics.beforeContentPaddingPx)
                    .coerceAtLeast(0f),
            )
            state.scrollToItem(position.index, position.offsetPx)
        },
    )
}

@Composable
internal fun BoxScope.FastScrollbar(
    state: androidx.compose.foundation.ScrollState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .matchParentSize(),
    ) {
        val viewportHeightPx = with(LocalDensity.current) { maxHeight.toPx() }.coerceAtLeast(1f)
        val scrollableContentHeightPx = state.maxValue.toFloat()
        if (scrollableContentHeightPx <= 0f) return@BoxWithConstraints

        val estimatedContentHeightPx = viewportHeightPx + scrollableContentHeightPx
        FastScrollbarTrack(
            scrollFraction = (state.value / scrollableContentHeightPx).coerceIn(0f, 1f),
            viewportFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0f, 1f),
            totalItems = 2,
            topInset = topInset,
            bottomInset = bottomInset,
            isScrollInProgress = state.isScrollInProgress,
            onJumpToFraction = { fraction ->
                state.scrollTo((scrollableContentHeightPx * fraction).roundToInt())
            },
        )
    }
}

@Composable
internal fun BoxScope.FastScrollbar(
    state: LazyGridState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    val rowEstimator = remember(state) { LazyItemSizeEstimator() }
    val metrics by remember(state, rowEstimator) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || (!state.canScrollBackward && !state.canScrollForward)) {
                null
            } else {
                val spanCount = layoutInfo.maxSpan.coerceAtLeast(1)
                val totalRows = ceil(totalItems.toFloat() / spanCount).toInt().coerceAtLeast(1)
                rowEstimator.prepare(totalRows, layoutInfo.mainAxisItemSpacing)
                var measuredRow = -1
                var measuredRowHeight = 0
                visibleItems.forEach { item ->
                    if (item.row >= 0 && item.row != measuredRow) {
                        if (measuredRow >= 0) rowEstimator.record(measuredRow, measuredRowHeight)
                        measuredRow = item.row
                        measuredRowHeight = item.size.height
                    } else if (item.row == measuredRow) {
                        measuredRowHeight = max(measuredRowHeight, item.size.height)
                    }
                }
                if (measuredRow >= 0) rowEstimator.record(measuredRow, measuredRowHeight)
                val viewportPx = layoutInfo.viewportSize.height.toFloat().coerceAtLeast(1f)
                val contentPx = (
                    rowEstimator.estimatedTotalPx() +
                        layoutInfo.beforeContentPadding +
                        layoutInfo.afterContentPadding
                    ).coerceAtLeast(viewportPx)
                val scrollablePx = (contentPx - viewportPx).coerceAtLeast(1f)
                val firstRow = visibleItems
                    .firstOrNull { it.index == state.firstVisibleItemIndex }
                    ?.row
                    ?.takeIf { it >= 0 }
                    ?: (state.firstVisibleItemIndex / spanCount)
                val estimatedScrollPx = rowEstimator.estimatedPrefixPx(firstRow) +
                    state.firstVisibleItemScrollOffset
                val fraction = when {
                    !state.canScrollBackward -> 0f
                    !state.canScrollForward -> 1f
                    else -> estimatedScrollPx / scrollablePx
                }
                FastScrollbarMetrics(
                    scrollFraction = fraction.coerceIn(0f, 1f),
                    viewportFraction = (viewportPx / contentPx).coerceIn(0f, 1f),
                    totalItems = totalItems,
                    estimatedUnits = totalRows,
                    scrollableContentPx = scrollablePx,
                    beforeContentPaddingPx = layoutInfo.beforeContentPadding,
                    estimator = rowEstimator,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        viewportFraction = resolvedMetrics.viewportFraction,
        totalItems = resolvedMetrics.totalItems,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        isScrollInProgress = state.isScrollInProgress,
        onJumpToFraction = { fraction ->
            val position = resolvedMetrics.estimator.positionForOffset(
                (resolvedMetrics.scrollableContentPx * fraction - resolvedMetrics.beforeContentPaddingPx)
                    .coerceAtLeast(0f),
            )
            val targetIndex = (
                position.index.toFloat() / resolvedMetrics.estimatedUnits * resolvedMetrics.totalItems
                ).roundToInt().coerceIn(0, resolvedMetrics.totalItems - 1)
            state.scrollToItem(targetIndex, position.offsetPx)
        },
    )
}

@Composable
private fun BoxScope.FastScrollbarTrack(
    scrollFraction: Float,
    viewportFraction: Float,
    totalItems: Int,
    topInset: Dp,
    bottomInset: Dp,
    isScrollInProgress: Boolean,
    onJumpToFraction: suspend (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val targetFractions = remember { Channel<Float>(Channel.CONFLATED) }
    val currentJump by rememberUpdatedState(onJumpToFraction)
    var isDragging by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var dragFraction by remember { mutableFloatStateOf(scrollFraction.coerceIn(0f, 1f)) }
    var dragGrabOffsetPx by remember { mutableFloatStateOf(0f) }
    var dragThumbLengthPx by remember { mutableFloatStateOf(0f) }
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val trackColor = fastScrollbarTrackColor(darkTheme)
    val thumbColor = fastScrollbarThumbColor(darkTheme)
    val displayedScrollFraction = if (isDragging) dragFraction else scrollFraction.coerceIn(0f, 1f)
    val chromeAlpha by animateFloatAsState(
        targetValue = if (visible || isDragging) 1f else 0f,
        animationSpec = motionSpecs.tween(if (visible || isDragging) 120 else 220),
        label = "fast_scrollbar_visibility",
    )
    LaunchedEffect(targetFractions) {
        for (fraction in targetFractions) currentJump(fraction)
    }
    DisposableEffect(targetFractions) {
        onDispose { targetFractions.close() }
    }
    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            visible = true
        } else {
            delay(2000L)
            visible = false
        }
    }
    BoxWithConstraints(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .zIndex(3f)
            .fillMaxHeight()
            .padding(top = topInset, end = FastScrollbarEdgePadding, bottom = bottomInset)
            .width(FastScrollbarTouchWidth),
    ) {
        val density = LocalDensity.current
        val trackHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val passiveLayout = scrollbarLayout(
            trackLengthPx = trackHeightPx,
            minThumbPx = with(density) { FastScrollbarMinThumbHeight.toPx() },
            scrollFraction = displayedScrollFraction,
            viewportFraction = viewportFraction,
        )
        val layout = if (isDragging && dragThumbLengthPx > 0f) {
            passiveLayout.copy(
                thumbLengthPx = dragThumbLengthPx,
                thumbOffsetPx = displayedScrollFraction * (trackHeightPx - dragThumbLengthPx).coerceAtLeast(0f),
            )
        } else {
            passiveLayout
        }
        val jumpToPointer: (Float, Float) -> Unit = { pointerY, grabOffset ->
            val fraction = scrollbarFractionForPointer(pointerY, layout, grabOffset)
            dragFraction = fraction
            targetFractions.trySend(fraction)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(displayedScrollFraction, 0f..1f)
                }
                .pointerInput(totalItems, layout.thumbLengthPx, layout.thumbTravelPx) {
                    detectTapGestures { offset ->
                        jumpToPointer(offset.y, layout.thumbLengthPx / 2f)
                    }
                }
                .pointerInput(totalItems, layout.thumbLengthPx, layout.thumbTravelPx) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            visible = true
                            dragThumbLengthPx = layout.thumbLengthPx
                            dragGrabOffsetPx = if (offset.y in layout.thumbOffsetPx..(
                                    layout.thumbOffsetPx + layout.thumbLengthPx
                                    )
                            ) {
                                offset.y - layout.thumbOffsetPx
                            } else {
                                layout.thumbLengthPx / 2f
                            }
                            jumpToPointer(offset.y, dragGrabOffsetPx)
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            jumpToPointer(change.position.y, dragGrabOffsetPx)
                        },
                        onDragEnd = {
                            isDragging = false
                            dragThumbLengthPx = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragThumbLengthPx = 0f
                        },
                    )
                },
        ) {
            FastScrollbarChrome(
                trackColor = trackColor,
                thumbColor = thumbColor,
                thumbHeight = with(density) { layout.thumbLengthPx.toDp() },
                thumbOffsetPx = layout.thumbOffsetPx,
                thumbWidth = if (isDragging) 5.dp else FastScrollbarThumbWidth,
                alpha = chromeAlpha,
            )
        }
    }
}

@Composable
private fun BoxScope.FastScrollbarChrome(
    trackColor: Color,
    thumbColor: Color,
    thumbHeight: Dp,
    thumbOffsetPx: Float,
    thumbWidth: Dp,
    alpha: Float,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(FastScrollbarTrackWidth)
            .alpha(alpha)
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .background(trackColor),
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .width(thumbWidth)
            .height(thumbHeight)
            .graphicsLayer {
                this.alpha = alpha
                translationY = thumbOffsetPx
            }
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .background(thumbColor),
    )
}

private fun fastScrollbarTrackColor(darkTheme: Boolean): Color {
    return (if (darkTheme) Color.White else InkText).copy(alpha = 0.12f)
}

private fun fastScrollbarThumbColor(darkTheme: Boolean): Color {
    return (if (darkTheme) Color.White else InkText).copy(alpha = if (darkTheme) 0.78f else 0.72f)
}

internal fun Modifier.ensureSingleItemRubberBand(state: androidx.compose.foundation.lazy.LazyListState): Modifier = composed {
    val baseModifier = this.kuperRubberBand(
        canScrollBackward = { state.canScrollBackward },
        canScrollForward = { state.canScrollForward },
    )
    if (state.canScrollBackward || state.canScrollForward) return@composed baseModifier
    val fallbackScrollState = rememberScrollableState { 0f }
    baseModifier.scrollable(
        state = fallbackScrollState,
        orientation = Orientation.Vertical,
        overscrollEffect = null,
    )
}

internal fun Modifier.ensureSingleItemRubberBand(state: LazyGridState): Modifier = composed {
    val baseModifier = this.kuperRubberBand(
        canScrollBackward = { state.canScrollBackward },
        canScrollForward = { state.canScrollForward },
    )
    if (state.canScrollBackward || state.canScrollForward) return@composed baseModifier
    val fallbackScrollState = rememberScrollableState { 0f }
    baseModifier.scrollable(
        state = fallbackScrollState,
        orientation = Orientation.Vertical,
        overscrollEffect = null,
    )
}

private fun Modifier.kuperRubberBand(
    canScrollBackward: () -> Boolean,
    canScrollForward: () -> Boolean,
): Modifier = composed {
    var translationTarget by remember { mutableFloatStateOf(0f) }
    val translation by animateFloatAsState(
        targetValue = translationTarget,
        animationSpec = ElovaireMotion.overscrollSpringSpec(),
        label = "list_rubber_band_translation",
    )
    val maxTranslationPx = with(LocalDensity.current) { 11.dp.toPx() }
    val connection = remember(maxTranslationPx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val isPullingDown = available.y > 0f && !canScrollBackward()
                val isPullingUp = available.y < 0f && !canScrollForward()
                if (!isPullingDown && !isPullingUp) return Offset.Zero
                translationTarget = (translationTarget + (available.y * 0.032f))
                    .coerceIn(-maxTranslationPx, maxTranslationPx)
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (translationTarget != 0f) {
                    translationTarget = 0f
                }
                return Velocity.Zero
            }
        }
    }
    this
        .graphicsLayer { translationY = translation }
        .nestedScroll(connection)
}
