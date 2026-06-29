package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private data class FastListScrollbarMetrics(
    val scrollFraction: Float,
    val visibleFraction: Float,
    val totalItems: Int,
    val visibleItemsCount: Int,
)

private data class FastGridScrollbarMetrics(
    val scrollFraction: Float,
    val visibleFraction: Float,
    val totalItems: Int,
    val visibleItemsCount: Int,
    val visibleRows: Int,
    val totalRows: Int,
    val spanCount: Int,
)

@Composable
internal fun BoxScope.FastScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
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
                FastListScrollbarMetrics(
                    scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f),
                    visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
                    totalItems = totalItems,
                    visibleItemsCount = visibleItems.size,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        visibleFraction = resolvedMetrics.visibleFraction,
        totalItems = resolvedMetrics.totalItems,
        visibleItemsCount = resolvedMetrics.visibleItemsCount,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleIndex = (resolvedMetrics.totalItems - resolvedMetrics.visibleItemsCount).coerceAtLeast(0)
            val targetIndex = (maxFirstVisibleIndex * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleIndex)
            state.requestScrollToItem(targetIndex)
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
        val scrollableContentHeightPx = state.maxValue.toFloat().coerceAtLeast(0f)
        if (scrollableContentHeightPx <= 0f) return@BoxWithConstraints

        val estimatedContentHeightPx = viewportHeightPx + scrollableContentHeightPx
        FastScrollbarTrack(
            scrollFraction = (state.value / scrollableContentHeightPx).coerceIn(0f, 1f),
            visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
            totalItems = 2,
            visibleItemsCount = 1,
            topInset = topInset,
            bottomInset = bottomInset,
            modifier = modifier,
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
                val averageItemHeightPx = visibleItems.map { it.size.height }.average().toFloat().coerceAtLeast(1f)
                val firstRowOffsetY = visibleItems.firstOrNull()?.offset?.y
                val spanCount = visibleItems
                    .takeWhile { it.offset.y == firstRowOffsetY }
                    .size
                    .coerceAtLeast(1)
                val totalRows = ceil(totalItems.toFloat() / spanCount.toFloat()).toInt().coerceAtLeast(1)
                val visibleRows = ceil(visibleItems.size.toFloat() / spanCount.toFloat()).toInt().coerceAtLeast(1)
                val estimatedContentHeightPx = max(viewportHeightPx, averageItemHeightPx * totalRows)
                val scrollableContentHeightPx = max(estimatedContentHeightPx - viewportHeightPx, 1f)
                val currentScrollPx =
                    ((state.firstVisibleItemIndex / spanCount) * averageItemHeightPx + state.firstVisibleItemScrollOffset)
                        .coerceAtLeast(0f)
                FastGridScrollbarMetrics(
                    scrollFraction = (currentScrollPx / scrollableContentHeightPx).coerceIn(0f, 1f),
                    visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.12f, 0.5f),
                    totalItems = totalItems,
                    visibleItemsCount = visibleItems.size,
                    visibleRows = visibleRows,
                    totalRows = totalRows,
                    spanCount = spanCount,
                )
            }
        }
    }
    val resolvedMetrics = metrics ?: return

    FastScrollbarTrack(
        scrollFraction = resolvedMetrics.scrollFraction,
        visibleFraction = resolvedMetrics.visibleFraction,
        totalItems = resolvedMetrics.totalItems,
        visibleItemsCount = resolvedMetrics.visibleItemsCount,
        topInset = topInset,
        bottomInset = bottomInset,
        modifier = modifier,
        onJumpToFraction = { fraction ->
            val maxFirstVisibleRow = (resolvedMetrics.totalRows - resolvedMetrics.visibleRows).coerceAtLeast(0)
            val targetRow = (maxFirstVisibleRow * fraction)
                .roundToInt()
                .coerceIn(0, maxFirstVisibleRow)
            val targetIndex =
                (targetRow * resolvedMetrics.spanCount).coerceIn(0, (resolvedMetrics.totalItems - 1).coerceAtLeast(0))
            state.requestScrollToItem(targetIndex)
        },
    )
}

@Composable
private fun BoxScope.FastScrollbarTrack(
    scrollFraction: Float,
    visibleFraction: Float,
    totalItems: Int,
    visibleItemsCount: Int,
    topInset: Dp,
    bottomInset: Dp,
    onJumpToFraction: suspend (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalItems <= visibleItemsCount) return

    val motionSpecs = rememberMotionSpecs()
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(scrollFraction.coerceIn(0f, 1f)) }
    var lastRequestedFraction by remember { mutableFloatStateOf(-1f) }
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val trackColor = if (darkTheme) {
        Color.White.copy(alpha = 0.12f)
    } else {
        InkText.copy(alpha = 0.12f)
    }
    val thumbColor = if (darkTheme) {
        Color.White.copy(alpha = 0.78f)
    } else {
        InkText.copy(alpha = 0.72f)
    }
    val animatedScrollFraction by animateFloatAsState(
        targetValue = if (isDragging) dragFraction.coerceIn(0f, 1f) else scrollFraction.coerceIn(0f, 1f),
        animationSpec = motionSpecs.tween(
            durationMillis = if (isDragging) 50 else 90,
        ),
        label = "fast_scrollbar_fraction",
    )
    BoxWithConstraints(
        modifier = modifier
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
        val fractionForPosition: (Float) -> Float = { y ->
            (y / trackHeightPx).coerceIn(0f, 1f)
        }
        val jumpToFraction: (Float) -> Unit = { fraction ->
            val normalized = fraction.coerceIn(0f, 1f)
            dragFraction = normalized
            if (kotlin.math.abs(normalized - lastRequestedFraction) >= 0.0025f) {
                lastRequestedFraction = normalized
                scope.launch {
                    onJumpToFraction(normalized)
                }
            }
        }

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
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
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
                    .width(5.dp)
                    .height(with(density) { thumbHeightPx.toDp() })
                    .graphicsLayer {
                        translationY = thumbOffsetPx
                    }
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(thumbColor),
            )
        }
    }
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
