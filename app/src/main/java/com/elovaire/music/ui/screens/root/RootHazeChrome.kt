package elovaire.music.droidbeauty.app.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText
import kotlinx.coroutines.flow.distinctUntilChanged

internal val LocalChromeHazeState = compositionLocalOf<HazeState?> { null }
internal val LocalPlayerHazeState = compositionLocalOf<HazeState?> { null }
internal val LocalUseSharedTopBarBackdrop = compositionLocalOf { false }

@Composable
internal fun statusBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.statusBars.getTop(this).toDp() }
}

@Composable
internal fun navigationBarInsetDp(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
}

@Composable
internal fun buttonNavigationScrollBoost(): Dp {
    val navigationInset = navigationBarInsetDp()
    return if (navigationInset >= 28.dp) {
        (navigationInset - 16.dp).coerceAtLeast(0.dp)
    } else {
        0.dp
    }
}

@Composable
internal fun screenContainerSizePx(): IntSize = LocalWindowInfo.current.containerSize

@Composable
internal fun topBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.topBarContentHeight

@Composable
internal fun detailTopBarOccupiedHeight(): Dp = statusBarInsetDp() + ElovaireSpacing.detailTopBarContentHeight

@Composable
internal fun sharedTopBarOccupiedHeight(): Dp =
    statusBarInsetDp() + maxOf(ElovaireSpacing.topBarContentHeight, ElovaireSpacing.detailTopBarContentHeight)

@Composable
internal fun bottomNavigationOccupiedHeight(): Dp =
    navigationBarInsetDp() + ElovaireSpacing.bottomNavigationBodyHeight

@Composable
private fun blurSurfaceOverlayColor(): Color = androidx.compose.material3.MaterialTheme.colorScheme.surface

@Composable
internal fun blurSurfaceBorderColor(): Color {
    return if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        InkText.copy(alpha = 0.08f)
    }
}

@Composable
internal fun Modifier.horizontalGestureSafe(): Modifier = this.systemGestureExclusion()

@Composable
internal fun rememberElovaireLazyListState(vararg inputs: Any?): LazyListState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) {
        lazyListPositionCache[cacheKey] ?: (0 to 0)
    }
    val state = rememberSaveable(cacheKey, saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = cachedPosition.first,
            firstVisibleItemScrollOffset = cachedPosition.second,
        )
    }
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { position ->
                lazyListPositionCache[cacheKey] = position
            }
    }
    return state
}

@Composable
internal fun rememberElovaireLazyGridState(vararg inputs: Any?): LazyGridState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) {
        lazyGridPositionCache[cacheKey] ?: (0 to 0)
    }
    val state = rememberSaveable(cacheKey, saver = LazyGridState.Saver) {
        LazyGridState(
            firstVisibleItemIndex = cachedPosition.first,
            firstVisibleItemScrollOffset = cachedPosition.second,
        )
    }
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { position ->
                lazyGridPositionCache[cacheKey] = position
            }
    }
    return state
}

@Composable
internal fun rememberElovaireScrollState(vararg inputs: Any?): androidx.compose.foundation.ScrollState {
    val cacheKey = remember(*inputs) {
        inputs.joinToString(separator = "|") { it?.toString().orEmpty() }
    }
    val cachedPosition = remember(cacheKey) { scrollPositionCache[cacheKey] ?: 0 }
    val state = rememberScrollState(cachedPosition)
    LaunchedEffect(state, cacheKey) {
        snapshotFlow { state.value }
            .distinctUntilChanged()
            .collect { value ->
                scrollPositionCache[cacheKey] = value
            }
    }
    return state
}

@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun DynamicBackdropSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    overlayAlpha: Float = 0.7f,
    borderColor: Color? = null,
    showTopEdgeLine: Boolean = false,
    showBottomEdgeLine: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val hazeState = LocalChromeHazeState.current
    val overlayColor = blurSurfaceOverlayColor()

    Box(
        modifier = modifier.clip(shape),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(hazeState) {
                        blurRadius = 30.dp
                        backgroundColor = overlayColor.copy(alpha = overlayAlpha)
                        tints = listOf(HazeTint(overlayColor.copy(alpha = overlayAlpha)))
                        noiseFactor = 0.015f
                    },
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(overlayColor.copy(alpha = overlayAlpha)),
        )
        if (borderColor != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, borderColor, shape),
            )
        }
        if (showTopEdgeLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(blurSurfaceBorderColor()),
            )
        }
        if (showBottomEdgeLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(blurSurfaceBorderColor()),
            )
        }
        content()
    }
}

@Composable
internal fun ProgressiveChromeBackdrop(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    modifier: Modifier = Modifier,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    DynamicBackdropSurface(
        modifier = modifier,
        overlayAlpha = overlayAlpha ?: 0.7f,
        showTopEdgeLine = edge == ProgressiveChromeEdge.Bottom && showEdgeLine,
        showBottomEdgeLine = edge == ProgressiveChromeEdge.Top && showEdgeLine,
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun ChromeHazeLayer(
    darkTheme: Boolean,
    edge: ProgressiveChromeEdge,
    modifier: Modifier = Modifier,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    ProgressiveChromeBackdrop(
        darkTheme = darkTheme,
        edge = edge,
        overlayAlpha = overlayAlpha,
        flatOverlay = flatOverlay,
        showEdgeLine = showEdgeLine,
        modifier = modifier,
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun FrostedTopBarBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    edge: ProgressiveChromeEdge = ProgressiveChromeEdge.Top,
    overlayAlpha: Float? = null,
    flatOverlay: Boolean = false,
    showEdgeLine: Boolean = true,
) {
    ChromeHazeLayer(
        darkTheme = darkTheme,
        edge = edge,
        overlayAlpha = overlayAlpha,
        flatOverlay = flatOverlay,
        showEdgeLine = showEdgeLine,
        modifier = modifier,
    )
}

@OptIn(ExperimentalHazeApi::class)
internal fun Modifier.playerFrostedSurface(
    tint: Color,
): Modifier = composed {
    val hazeState = LocalPlayerHazeState.current
    if (hazeState == null) {
        this
    } else {
        val tintIsDark = tint.luminance() < 0.44f
        hazeEffect(hazeState) {
            progressive = HazeProgressive.LinearGradient(
                startIntensity = 0.9f,
                endIntensity = 0.42f,
                preferPerformance = true,
            )
            blurRadius = 28.dp
            backgroundColor = tint.copy(alpha = if (tintIsDark) 0.18f else 0.14f)
            tints = listOf(
                HazeTint(tint.copy(alpha = if (tintIsDark) 0.28f else 0.2f)),
                HazeTint(
                    if (tintIsDark) {
                        Color.Black.copy(alpha = 0.14f)
                    } else {
                        Color.White.copy(alpha = 0.16f)
                    },
                ),
            )
            noiseFactor = 0.04f
        }
    }
}
