package elovaire.music.droidbeauty.app.quality

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.LocalHazeBlurStyle
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.droidbeauty.app.ui.components.ArtworkPaletteShadow
import elovaire.music.droidbeauty.app.ui.screens.DynamicBackdropSurface
import elovaire.music.droidbeauty.app.ui.screens.LyricsControlButton
import elovaire.music.droidbeauty.app.ui.screens.LyricsSkipControl
import elovaire.music.droidbeauty.app.ui.screens.QueueEdgeHaze
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import kotlin.math.roundToInt
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackdropBlurRenderingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun artworkShadowUsesPaletteTintBeyondArtworkBounds() {
        val cover = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.rgb(20, 90, 220)) }
            .asImageBitmap()
        composeRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .testTag("artwork_shadow_fixture"),
                ) {
                    Box(modifier = Modifier.align(Alignment.Center).size(120.dp)) {
                        ArtworkPaletteShadow(
                            cornerRadius = 12.dp,
                            tint = Color(0xFF145ADC),
                        )
                        Image(
                            bitmap = cover,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("artwork_shadow"),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()

        val bounds = composeRule.onNodeWithTag("artwork_shadow").fetchSemanticsNode().boundsInRoot
        val pixels = composeRule.onNodeWithTag("artwork_shadow_fixture").captureToImage().toPixelMap()
        val shadowOffset = (4f * composeRule.activity.resources.displayMetrics.density).roundToInt()
        val sampleY = bounds.center.y.toInt()
        val nearShadow = pixels[(bounds.right + shadowOffset).toInt(), sampleY]
        val clearBackgroundOffset = (40f * composeRule.activity.resources.displayMetrics.density).roundToInt()
        val clearBackground = pixels[(bounds.right + clearBackgroundOffset).toInt(), sampleY]

        assertTrue("the palette shadow should retain the cover's blue hue", nearShadow.blue > nearShadow.red)
        assertTrue("the shadow should soften the background beyond the art bounds", nearShadow.luminance() < clearBackground.luminance())
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun backdropBlurSoftensLazyAndRegularTextButKeepsGlassLabelCrisp() {
        val blurEnabled = mutableStateOf(false)
        val useLazyList = mutableStateOf(true)
        val lightTheme = mutableStateOf(false)
        val image = checkerboardImage()
        composeRule.setContent {
            BackdropBlurFixture(
                blurEnabled = blurEnabled.value,
                image = image,
                useLazyList = useLazyList.value,
                lightTheme = lightTheme.value,
            )
        }
        listOf(false, true).forEach { isLightTheme ->
            listOf(true, false).forEach { lazyList ->
                composeRule.runOnIdle {
                    useLazyList.value = lazyList
                    lightTheme.value = isLightTheme
                    blurEnabled.value = false
                }
                composeRule.waitForIdle()
                val sharpCapture = composeRule.onNodeWithTag("blur_fixture").captureToImage()
                val textBounds = composeRule.onNodeWithTag("backdrop_text_0").fetchSemanticsNode().boundsInRoot
                val iconBounds = composeRule.onNodeWithTag("backdrop_icon").fetchSemanticsNode().boundsInRoot
                val imageBounds = composeRule.onNodeWithTag("backdrop_image").fetchSemanticsNode().boundsInRoot
                val shapeBounds = composeRule.onNodeWithTag("backdrop_shape").fetchSemanticsNode().boundsInRoot
                val foregroundBounds = composeRule.onNodeWithTag("glass_label").fetchSemanticsNode().boundsInRoot
                val sharpPixels = sharpCapture.toPixelMap()
                val sharpTextEdges = edgeEnergy(sharpPixels, textBounds)
                val sharpIconEdges = edgeEnergy(sharpPixels, iconBounds)
                val sharpImageEdges = edgeEnergy(sharpPixels, imageBounds)
                val sharpShapeEdges = edgeEnergy(sharpPixels, shapeBounds)
                val sharpForegroundEdges = edgeEnergy(sharpPixels, foregroundBounds)

                composeRule.runOnIdle { blurEnabled.value = true }
                composeRule.waitForIdle()
                val blurredPixels = composeRule.onNodeWithTag("blur_fixture").captureToImage().toPixelMap()
                val blurredTextEdges = edgeEnergy(blurredPixels, textBounds)
                val blurredIconEdges = edgeEnergy(blurredPixels, iconBounds)
                val blurredImageEdges = edgeEnergy(blurredPixels, imageBounds)
                val blurredShapeEdges = edgeEnergy(blurredPixels, shapeBounds)
                val blurredForegroundEdges = edgeEnergy(blurredPixels, foregroundBounds)

                val variant = "${if (lazyList) "lazy" else "regular"}/${if (isLightTheme) "light" else "dark"}"
                assertTrue("fixture must contain measurable text edges", sharpTextEdges > 0.0)
                assertTrue(
                    "$variant background text did not blur: sharp=$sharpTextEdges blurred=$blurredTextEdges",
                    blurredTextEdges < sharpTextEdges * 0.72,
                )
                assertTrue(
                    "$variant vector did not blur: sharp=$sharpIconEdges blurred=$blurredIconEdges",
                    blurredIconEdges < sharpIconEdges * 0.72,
                )
                assertTrue(
                    "$variant image did not blur: sharp=$sharpImageEdges blurred=$blurredImageEdges",
                    blurredImageEdges < sharpImageEdges * 0.72,
                )
                assertTrue(
                    "$variant shape did not blur: sharp=$sharpShapeEdges blurred=$blurredShapeEdges",
                    blurredShapeEdges < sharpShapeEdges * 0.85,
                )
                assertTrue(
                    "foreground label lost crispness: sharp=$sharpForegroundEdges blurred=$blurredForegroundEdges",
                    blurredForegroundEdges > sharpForegroundEdges * 0.85,
                )

                if (lazyList && !isLightTheme) {
                    composeRule.onNodeWithTag("blur_list").performScrollToIndex(2)
                    composeRule.waitForIdle()
                    val scrolledTextBounds = composeRule.onNodeWithTag("backdrop_text_2").fetchSemanticsNode().boundsInRoot
                    composeRule.runOnIdle { blurEnabled.value = false }
                    composeRule.waitForIdle()
                    val scrolledSharpPixels = composeRule.onNodeWithTag("blur_fixture").captureToImage().toPixelMap()
                    val scrolledSharpEdges = edgeEnergy(scrolledSharpPixels, scrolledTextBounds)
                    composeRule.runOnIdle { blurEnabled.value = true }
                    composeRule.waitForIdle()
                    val scrolledBlurredPixels = composeRule.onNodeWithTag("blur_fixture").captureToImage().toPixelMap()
                    val scrolledBlurredEdges = edgeEnergy(scrolledBlurredPixels, scrolledTextBounds)
                    assertTrue(
                        "scrolled lazy-list text entering the surface must blur: sharp=$scrolledSharpEdges blurred=$scrolledBlurredEdges",
                        scrolledBlurredEdges < scrolledSharpEdges * 0.72,
                    )
                }
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun playerBackdropSourceIncludesLazyQueueLayers() {
        val blurEnabled = mutableStateOf(false)
        val image = checkerboardImage()
        composeRule.setContent {
            BackdropBoundaryFixture(
                blurEnabled = blurEnabled.value,
                image = image,
            )
        }

        composeRule.waitForIdle()
        val sharpCapture = composeRule.onNodeWithTag("boundary_fixture").captureToImage().toPixelMap()
        val textBounds = composeRule.onNodeWithTag("queue_text").fetchSemanticsNode().boundsInRoot
        val shapeBounds = composeRule.onNodeWithTag("queue_shape").fetchSemanticsNode().boundsInRoot
        val sharpTextEdges = edgeEnergy(sharpCapture, textBounds)
        val sharpShapeEdges = edgeEnergy(sharpCapture, shapeBounds)

        composeRule.runOnIdle { blurEnabled.value = true }
        composeRule.waitForIdle()
        val fixedPixels = composeRule.onNodeWithTag("boundary_fixture").captureToImage().toPixelMap()
        val fixedTextEdges = edgeEnergy(fixedPixels, textBounds)
        val fixedShapeEdges = edgeEnergy(fixedPixels, shapeBounds)
        val foregroundBounds = composeRule.onNodeWithTag("boundary_glass_label").fetchSemanticsNode().boundsInRoot
        val sharpForegroundEdges = edgeEnergy(sharpCapture, foregroundBounds)
        val fixedForegroundEdges = edgeEnergy(fixedPixels, foregroundBounds)

        assertTrue(
            "the unblurred queue text should be measurable: edges=$sharpTextEdges bounds=$textBounds image=${sharpCapture.width}x${sharpCapture.height}",
            sharpTextEdges > 0.0,
        )
        assertTrue(
            "queue text must blur when included by the player source: sharp=$sharpTextEdges blurred=$fixedTextEdges",
            fixedTextEdges < sharpTextEdges * 0.72,
        )
        assertTrue(
            "queue shape must blur when included by the player source: sharp=$sharpShapeEdges blurred=$fixedShapeEdges",
            fixedShapeEdges < sharpShapeEdges * 0.85,
        )
        assertTrue(
            "foreground glass label should stay crisp: sharp=$sharpForegroundEdges fixed=$fixedForegroundEdges",
            fixedForegroundEdges > sharpForegroundEdges * 0.85,
        )
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun lyricsControlsBlurTheCompleteBackdropAndKeepBothSkipTargetsActive() {
        val blurEnabled = mutableStateOf(false)
        var playClicks = 0
        var previousClicks = 0
        var nextClicks = 0
        composeRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val state = rememberHazeState()
                Box(Modifier.fillMaxSize().testTag("lyrics_controls_fixture")) {
                    Box(Modifier.fillMaxSize().hazeSource(state, zIndex = -1f)
                        .background(Color(0xFF172332)))
                    Column(Modifier.fillMaxSize().hazeSource(state, zIndex = 2f)) {
                        repeat(20) {
                            Text("BACKGROUND WWW MMM 0123456789".repeat(3), color = Color.White,
                                fontSize = 18.sp, lineHeight = 18.sp, maxLines = 1,
                                modifier = Modifier.height(18.dp))
                        }
                    }
                    CompositionLocalProvider(LocalHazeBlurStyle provides HazeBlurStyle { blurEnabled(blurEnabled.value) }) {
                        Row(Modifier.align(Alignment.TopCenter).offset(y = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LyricsControlButton(
                                modifier = Modifier.size(44.dp).testTag("lyrics_glass_play"),
                                shape = CircleShape, lyricsHazeState = state, contentColor = Color.White,
                                onClick = { playClicks++ }, label = "lyrics_test_play",
                            ) {
                                Text("P", color = Color.White, fontSize = 12.sp)
                            }
                            Box(Modifier.testTag("lyrics_glass_skip")) {
                                LyricsSkipControl(state, Color.White, { previousClicks++ }, { nextClicks++ })
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val fixture = composeRule.onNodeWithTag("lyrics_controls_fixture")
        val sharp = fixture.captureToImage().toPixelMap()
        val scale = composeRule.activity.resources.displayMetrics.density
        val sampleBounds = listOf("lyrics_glass_play", "lyrics_glass_skip").map { tag ->
            val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            androidx.compose.ui.geometry.Rect(
                bounds.left + 14f * scale, bounds.top + 32f * scale,
                bounds.right - 14f * scale, bounds.bottom - 6f * scale,
            )
        }
        composeRule.runOnIdle { blurEnabled.value = true }
        composeRule.waitForIdle()
        val blurred = fixture.captureToImage().toPixelMap()
        sampleBounds.forEachIndexed { index, bounds ->
            val before = edgeEnergy(sharp, bounds)
            val after = edgeEnergy(blurred, bounds)
            assertTrue("lyrics control $index has no source text to test", before > 0)
            assertTrue("lyrics control $index leaves text sharp: $before -> $after", after < before * 0.65)
        }
        composeRule.onNodeWithTag("lyrics_glass_play").performClick()
        composeRule.onNode(androidx.compose.ui.test.hasContentDescription("Previous")).performClick()
        composeRule.onNode(androidx.compose.ui.test.hasContentDescription("Next")).performClick()
        composeRule.runOnIdle {
            assertEquals(1, playClicks)
            assertEquals(1, previousClicks)
            assertEquals(1, nextClicks)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun queueSoftEdgeBlursAcrossTheWidthInsteadOfSmearingOnlyVertically() {
        val showEdge = mutableStateOf(false)
        composeRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val state = rememberHazeState()
                Box(Modifier.fillMaxSize().testTag("queue_edge_fixture")) {
                    Canvas(Modifier.fillMaxSize().hazeSource(state, zIndex = -1f)) {
                        val stripeWidth = 3.dp.toPx()
                        repeat((size.width / stripeWidth).toInt() + 1) { index ->
                            drawRect(if (index % 2 == 0) Color.White else Color.Black,
                                topLeft = androidx.compose.ui.geometry.Offset(index * stripeWidth, 0f),
                                size = androidx.compose.ui.geometry.Size(stripeWidth, size.height))
                        }
                    }
                    QueueEdgeHaze(visible = showEdge.value, alignment = Alignment.BottomCenter, hazeState = state,
                        startIntensity = 0f, endIntensity = 1f, edgeHeight = 30.dp)
                }
            }
        }
        composeRule.waitForIdle()
        val fixture = composeRule.onNodeWithTag("queue_edge_fixture")
        val sharp = fixture.captureToImage().toPixelMap()
        val scale = composeRule.activity.resources.displayMetrics.density
        val sample = androidx.compose.ui.geometry.Rect(30f * scale, sharp.height - 6f * scale,
            sharp.width - 30f * scale, sharp.height - scale)
        composeRule.runOnIdle { showEdge.value = true }
        composeRule.waitForIdle()
        val blurred = fixture.captureToImage().toPixelMap()
        val before = edgeEnergy(sharp, sample)
        val after = edgeEnergy(blurred, sample)
        assertTrue("queue edge leaves vertical text strokes sharp: $before -> $after", after < before * 0.35)
    }

    private fun checkerboardImage() = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until height) {
            for (x in 0 until width) {
                setPixel(x, y, if ((x / 3 + y / 3) % 2 == 0) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
            }
        }
    }.asImageBitmap()

    private fun edgeEnergy(
        pixels: androidx.compose.ui.graphics.PixelMap,
        bounds: androidx.compose.ui.geometry.Rect,
    ): Double {
        val left = bounds.left.toInt().coerceIn(0, pixels.width - 2)
        val top = bounds.top.toInt().coerceIn(0, pixels.height - 2)
        val right = bounds.right.toInt().coerceIn(left + 1, pixels.width - 1)
        val bottom = bounds.bottom.toInt().coerceIn(top + 1, pixels.height - 1)
        var differences = 0L
        var samples = 0L
        for (y in top until bottom) {
            for (x in left until right) {
                differences += kotlin.math.abs(pixels[x, y].luminance() - pixels[x + 1, y].luminance()).toLong()
                differences += kotlin.math.abs(pixels[x, y].luminance() - pixels[x, y + 1].luminance()).toLong()
                samples += 2
            }
        }
        return differences.toDouble() / samples
    }

    private fun Color.luminance(): Int = (red * 255f).toInt() + (green * 255f).toInt() + (blue * 255f).toInt()
}

@OptIn(ExperimentalHazeApi::class)
@androidx.compose.runtime.Composable
private fun BackdropBlurFixture(
    blurEnabled: Boolean,
    image: androidx.compose.ui.graphics.ImageBitmap,
    useLazyList: Boolean,
    lightTheme: Boolean,
) {
    MaterialTheme(colorScheme = if (lightTheme) lightColorScheme() else darkColorScheme()) {
        val hazeState = rememberHazeState()
        val backdropColor = if (lightTheme) Color(0xFFF4F4F4) else Color(0xFF080808)
        val sourceTextColor = if (lightTheme) Color(0xFF111111) else Color.White
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor)
                .testTag("blur_fixture"),
        ) {
            if (useLazyList) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState, zIndex = -1f)
                        .background(backdropColor)
                        .testTag("blur_list"),
                ) {
                    items((0..20).toList(), key = { it }) { index ->
                        Box(modifier = Modifier.animateItem()) {
                            BackdropFixtureRow(index, image, sourceTextColor)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .hazeSource(hazeState, zIndex = -1f)
                        .background(backdropColor),
                ) {
                    (0..20).forEach { index -> BackdropFixtureRow(index, image, sourceTextColor) }
                }
            }
            CompositionLocalProvider(
                LocalHazeBlurStyle provides HazeBlurStyle { blurEnabled(blurEnabled) },
            ) {
                DynamicBackdropSurface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(190.dp),
                    overlayAlpha = 0f,
                    hazeState = hazeState,
                ) {
                    Text(
                        text = "FOREGROUND LABEL",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .testTag("glass_label"),
                        color = sourceTextColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun BackdropFixtureRow(
    index: Int,
    image: androidx.compose.ui.graphics.ImageBitmap,
    sourceTextColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.size(56.dp).then(if (index == 0) Modifier.testTag("backdrop_image") else Modifier),
        )
        Canvas(modifier = Modifier.size(48.dp).then(if (index == 0) Modifier.testTag("backdrop_icon") else Modifier)) {
            drawCircle(sourceTextColor, style = Stroke(width = 3.dp.toPx()))
            drawLine(sourceTextColor, start = center.copy(x = center.x - size.width * 0.25f), end = center.copy(x = center.x + size.width * 0.25f), strokeWidth = 3.dp.toPx())
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(sourceTextColor)
                .then(if (index == 0) Modifier.testTag("backdrop_shape") else Modifier),
        )
        Text(
            text = if (index == 0) "BACKGROUND TEXT" else "Background row $index",
            modifier = Modifier
                .weight(1f)
            .testTag("backdrop_text_$index"),
            color = sourceTextColor,
            fontSize = if (index == 0) 24.sp else 18.sp,
            fontWeight = if (index == 0) FontWeight.Black else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalHazeApi::class)
@androidx.compose.runtime.Composable
private fun BackdropBoundaryFixture(
    blurEnabled: Boolean,
    image: androidx.compose.ui.graphics.ImageBitmap,
) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val hazeState = rememberHazeState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080808))
                .testTag("boundary_fixture"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState, zIndex = -1f),
            ) {
                Box(Modifier.fillMaxSize().background(Color(0xFF121212)))
                BoundaryQueue(image)
            }
            CompositionLocalProvider(
                LocalHazeBlurStyle provides HazeBlurStyle { blurEnabled(blurEnabled) },
            ) {
                DynamicBackdropSurface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(160.dp),
                    overlayAlpha = 0.34f,
                    hazeState = hazeState,
                ) {
                    Text(
                        text = "FOREGROUND LABEL",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .testTag("boundary_glass_label"),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun BoundaryQueue(image: androidx.compose.ui.graphics.ImageBitmap) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        item {
            Box(modifier = Modifier.animateItem()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF282828))
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Image(bitmap = image, contentDescription = null, modifier = Modifier.size(56.dp))
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(Color.White, style = Stroke(width = 3.dp.toPx()))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "QUEUE BACKGROUND TEXT",
                            modifier = Modifier.testTag("queue_text"),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White)
                                .testTag("queue_shape"),
                        )
                    }
                }
            }
        }
        items((1..10).toList(), key = { it }) { index ->
            Box(modifier = Modifier.animateItem()) {
                Text("Queue row $index", color = Color.White)
            }
        }
    }
}
