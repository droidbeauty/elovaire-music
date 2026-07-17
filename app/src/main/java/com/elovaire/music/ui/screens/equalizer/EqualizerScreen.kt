package elovaire.music.droidbeauty.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspConfig
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspModel
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.displayLabel
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun EqualizerScreen(
    settings: EqSettings,
    selectedPresetName: String?,
    equalizerEnabled: Boolean,
    onBack: () -> Unit,
    onBandChanged: (Int, Float) -> Unit,
    onBassChanged: (Float) -> Unit,
    onMidrangeChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
    onSpaciousnessChanged: (Float) -> Unit,
    onSpaciousnessModeChanged: (SpaciousnessMode) -> Unit,
    onReverbDurationChanged: (Int) -> Unit,
    onReverbProfileChanged: (ReverbProfile) -> Unit,
    onResetReverb: () -> Unit,
    onApplyPreset: (String, EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val graphScrollState = rememberScrollState()
    val language = LocalAppLanguage.current
    val copy = remember(language) { settingsCopy(language) }
    val graphContentWidth = EQ_GRAPH_EDGE_PADDING * 2 +
        EQ_BAND_SPACING * (EqualizerDspModel.BAND_COUNT - 1).coerceAtLeast(0).toFloat()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    start = 18.dp,
                    top = topBarOccupiedHeight() + 8.dp,
                    end = 18.dp,
                    bottom = 20.dp,
                ),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EQ_DB_SCALE_GAP),
                    verticalAlignment = Alignment.Top,
                ) {
                    EqDbScale(
                        modifier = Modifier
                            .width(EQ_DB_SCALE_WIDTH)
                            .height(EQ_BAND_PANEL_HEIGHT),
                    )
                        Column(
                            modifier = Modifier
                                .horizontalGestureSafe()
                                .horizontalScroll(graphScrollState),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                        EqResponseGraph(
                            settings = settings,
                            onBandChanged = onBandChanged,
                            modifier = Modifier
                                .width(graphContentWidth)
                                .height(EQ_BAND_PANEL_HEIGHT),
                        )
                        EqBandFrequencyLabels(
                            contentWidth = graphContentWidth,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                EqHorizontalScrollbar(
                    scrollState = graphScrollState,
                    contentWidth = graphContentWidth,
                )
                Spacer(modifier = Modifier.height(8.dp))
                EqMiniResponseGraph(
                    settings = settings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                EqPresetMenu(
                    currentSettings = settings,
                    selectedPresetName = selectedPresetName,
                    equalizerEnabled = equalizerEnabled,
                    onApplyPreset = onApplyPreset,
                    onReset = onReset,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .ensureSingleItemRubberBand(listState),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    ModuleCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            SettingsCategoryText(
                                title = uiPhrase(language, UiPhrase.ToneShaping),
                                iconResId = R.drawable.ic_lucide_audio_waveform,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                EqToneKnob(
                                    title = uiPhrase(language, UiPhrase.Bass),
                                    value = settings.bass.coerceIn(0f, 1f),
                                    valueRange = 0f..1f,
                                    accentColor = Color(0xFF2FE08D),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = onBassChanged,
                                )
                                EqToneKnob(
                                    title = uiPhrase(language, UiPhrase.Midrange),
                                    value = settings.midrange.coerceIn(-1f, 1f),
                                    valueRange = -1f..1f,
                                    accentColor = Color(0xFF39C2FF),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = onMidrangeChanged,
                                )
                                EqToneKnob(
                                    title = uiPhrase(language, UiPhrase.Treble),
                                    value = settings.treble.coerceIn(-1f, 1f),
                                    valueRange = -1f..1f,
                                    accentColor = Color(0xFFFFB056),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = onTrebleChanged,
                                )
                            }
                        }
                    }
                }
                item {
                    ModuleCard {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            SettingsCategoryText(
                                title = copy.spaciousness,
                                iconResId = R.drawable.ic_lucide_wind,
                            )
                            SpaciousnessModeMenu(
                                currentMode = settings.spaciousnessMode,
                                spaciousnessAmount = settings.spaciousness,
                                onModeSelected = onSpaciousnessModeChanged,
                            )
                            EqMacroSliderRow(
                                title = uiPhrase(language, UiPhrase.EffectStrength),
                                value = settings.spaciousness.coerceIn(0f, 1f),
                                valueText = "${(settings.spaciousness.coerceIn(0f, 1f) * 100f).roundToInt()}%",
                                onValueChange = onSpaciousnessChanged,
                                valueRange = 0f..1f,
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = uiPhrase(language, UiPhrase.Reverb),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = elovaireScaledSp(16f),
                                        ),
                                    )
                                    Text(
                                        text = if (settings.reverbDurationMs <= 0) uiPhrase(language, UiPhrase.Off) else "${settings.reverbDurationMs} ms",
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalGestureSafe()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    EqPresetPill(
                                        label = uiPhrase(language, UiPhrase.Reset),
                                        selected = false,
                                        emphasized = true,
                                        useSubtleIdleBackground = true,
                                        onClick = onResetReverb,
                                    )
                                    ReverbProfile.entries.forEach { profile ->
                                        EqPresetPill(
                                            label = when (profile) {
                                                ReverbProfile.Dry -> uiPhrase(language, UiPhrase.Dry)
                                                ReverbProfile.Wet -> uiPhrase(language, UiPhrase.Wet)
                                            },
                                            selected = settings.reverbDurationMs > 0 && settings.reverbProfile == profile,
                                            useSubtleIdleBackground = true,
                                            onClick = { onReverbProfileChanged(profile) },
                                        )
                                    }
                                }
                                ReverbStepSlider(
                                    valueMs = settings.reverbDurationMs,
                                    onValueChange = onReverbDurationChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 390.dp,
            bottomInset = navigationBarInsetDp() + 20.dp,
        )
        PinnedBackTopBar(
            title = copy.equalizer,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ReverbStepSlider(
    valueMs: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val steps = remember { (0..500 step 50).toList() }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val selectedValue = steps.minByOrNull { kotlin.math.abs(it - valueMs.coerceIn(0, 500)) } ?: 0
    val selectedIndex = steps.indexOf(selectedValue).coerceAtLeast(0)
    val maxIndex = (steps.size - 1).coerceAtLeast(1)
    val knobSize = 20.dp
    val dotColor = MaterialTheme.colorScheme.onSurface
    val knobColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val lineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    var isDragging by remember { mutableStateOf(false) }
    var dragCenterPx by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .height(36.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val trackStartPx = knobSizePx / 2f
        val trackWidthPx = (maxWidthPx - knobSizePx).coerceAtLeast(1f)
        val selectedCenterPx = trackStartPx + (trackWidthPx * (selectedIndex.toFloat() / maxIndex.toFloat()))
        val stepCenters = remember(maxWidthPx, maxIndex) {
            steps.indices.map { index ->
                if (maxIndex == 0) {
                    maxWidthPx / 2f
                } else {
                    trackStartPx + (trackWidthPx * (index.toFloat() / maxIndex.toFloat()))
                }
            }
        }
        LaunchedEffect(selectedCenterPx, maxWidthPx) {
            if (!isDragging) {
                dragCenterPx = selectedCenterPx
            }
        }
        val knobOffset by animateDpAsState(
            targetValue = with(density) {
                ((if (isDragging) dragCenterPx else selectedCenterPx) - (knobSizePx / 2f)).toDp()
            },
            animationSpec = if (isDragging) {
                motionSpecs.tween(durationMillis = 60)
            } else {
                motionSpecs.spring(
                    dampingRatio = 0.82f,
                    stiffness = 480f,
                )
            },
            label = "reverb_step_knob_offset",
        )
        val updateFromPosition: (Float) -> Unit = { xPosition ->
            val clampedX = xPosition.coerceIn(trackStartPx, trackStartPx + trackWidthPx)
            dragCenterPx = clampedX
            val targetIndex = stepCenters
                .withIndex()
                .minByOrNull { (_, center) -> kotlin.math.abs(center - clampedX) }
                ?.index
                ?: selectedIndex
            val targetValue = steps[targetIndex]
            if (targetValue != selectedValue) {
                currentOnValueChange(targetValue)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxWidthPx) {
                    detectTapGestures { offset ->
                        if (maxWidthPx > 0f) {
                            updateFromPosition(offset.x)
                        }
                    }
                }
                .pointerInput(maxWidthPx, steps.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            if (maxWidthPx > 0f) {
                                updateFromPosition(offset.x)
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            if (maxWidthPx > 0f) {
                                change.consume()
                                updateFromPosition(change.position.x)
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = with(density) { trackStartPx.toDp() })
                    .width(with(density) { trackWidthPx.toDp() })
                    .height(2.dp)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(lineColor),
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val selectedDotRadius = 3.5.dp.toPx()
                val defaultDotRadius = 2.5.dp.toPx()
                val centerY = size.height / 2f
                    steps.forEachIndexed { index, _ ->
                        val fraction = if (maxIndex == 0) 0f else index.toFloat() / maxIndex.toFloat()
                        drawCircle(
                            color = dotColor,
                            radius = if (index == selectedIndex) selectedDotRadius else defaultDotRadius,
                            center = Offset(trackStartPx + (trackWidthPx * fraction), centerY),
                        )
                    }
                }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = knobOffset.roundToPx(), y = 0) }
                    .size(knobSize)
                    .clip(CircleShape)
                    .background(knobColor)
                    .align(Alignment.CenterStart),
            )
        }
    }
}

@Composable
private fun DigitalSoundKnob(
    title: String,
    iconResId: Int,
    value: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    var dragValue by remember(value) { mutableFloatStateOf(value.coerceIn(0f, 1f)) }
    LaunchedEffect(value) {
        dragValue = value.coerceIn(0f, 1f)
    }
    val animatedValue by animateFloatAsState(
        targetValue = dragValue,
        animationSpec = motionSpecs.tween(MotionDuration.Standard),
        label = "${title}_sound_knob",
    )
    val glowColor = Color(0xFF61F6A2)
    val inactiveDot = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f)
    val trackColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.3f)
    }
    val activeArcColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val tickColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(134.dp)
                .horizontalGestureSafe()
                .pointerInput(title) {
                    detectTapGestures { offset ->
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        val horizontalInsetPx = widthPx * 0.035f
                        val activeWidthPx = (widthPx - (horizontalInsetPx * 2f)).coerceAtLeast(1f)
                        dragValue = ((offset.x - horizontalInsetPx) / activeWidthPx).coerceIn(0f, 1f)
                        onValueChange(dragValue)
                    }
                }
                .pointerInput(title) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val widthPx = size.width.toFloat().coerceAtLeast(1f)
                            val horizontalInsetPx = widthPx * 0.035f
                            val activeWidthPx = (widthPx - (horizontalInsetPx * 2f)).coerceAtLeast(1f)
                            dragValue = ((offset.x - horizontalInsetPx) / activeWidthPx).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val widthPx = size.width.toFloat().coerceAtLeast(1f)
                            dragValue = (dragValue + ((dragAmount / widthPx) * 0.99f)).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        },
                    )
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                val strokeWidth = 5.5.dp.toPx()
                val horizontalInset = 8.dp.toPx()
                val topInset = 12.dp.toPx()
                val radius = min(
                    ((size.width - (horizontalInset * 2f)) / 2f).coerceAtLeast(1f),
                    ((size.height - topInset - 8.dp.toPx()) * 0.54f).coerceAtLeast(1f),
                )
                val center = Offset(size.width / 2f, topInset + radius)
                val startAngle = 180f
                val sweepAngle = 180f
                val activeSweep = sweepAngle * animatedValue
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                if (activeSweep > 0f) {
                    drawArc(
                        color = activeArcColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                val tickOuterRadius = (radius - 8.dp.toPx()).coerceAtLeast(1f)
                val tickInnerRadius = (tickOuterRadius - 6.dp.toPx()).coerceAtLeast(1f)
                val tickCount = 30
                repeat(tickCount) { tickIndex ->
                    val fraction = tickIndex / (tickCount - 1).toFloat()
                    val angleDegrees = 180f + (180f * fraction)
                    val angleRadians = Math.toRadians(angleDegrees.toDouble())
                    val start = Offset(
                        x = center.x + (cos(angleRadians) * tickInnerRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickInnerRadius).toFloat(),
                    )
                    val end = Offset(
                        x = center.x + (cos(angleRadians) * tickOuterRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickOuterRadius).toFloat(),
                    )
                    drawLine(
                        color = tickColor,
                        start = start,
                        end = end,
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Square,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(top = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${(animatedValue * 100f).roundToInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(20f)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (animatedValue > 0f) glowColor else inactiveDot),
                )
            }
        }

        Row(
            modifier = Modifier
                .offset(y = (-28).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            )
        }
    }
}

@Composable
private fun DetailScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(
            iconResId = R.drawable.ic_lucide_chevron_left,
            contentDescription = "Back",
            showBackground = false,
            onClick = onBack,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(26f)),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                )
            }
        }
    }
}

internal fun circularKnobValueForOffset(
    offset: Offset,
    size: Size,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
): Float {
    if (size.width <= 0f || size.height <= 0f) return 0f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val angle = Math.toDegrees(
        atan2(
            (offset.y - centerY).toDouble(),
            (offset.x - centerX).toDouble(),
        ),
    ).toFloat().let { if (it < 0f) it + 360f else it }
    val relative = ((angle - startAngleDegrees) % 360f + 360f) % 360f
    return when {
        relative <= sweepAngleDegrees -> (relative / sweepAngleDegrees).coerceIn(0f, 1f)
        relative < (sweepAngleDegrees + ((360f - sweepAngleDegrees) / 2f)) -> 1f
        else -> 0f
    }
}

@Composable
internal fun EqToneKnob(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val safeRange = remember(valueRange) {
        if (valueRange.endInclusive > valueRange.start) valueRange else 0f..1f
    }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val clampedValue = value.coerceIn(safeRange.start, safeRange.endInclusive)
    val targetFraction = ((clampedValue - safeRange.start) / (safeRange.endInclusive - safeRange.start))
        .coerceIn(0f, 1f)
    var dragFraction by remember { mutableFloatStateOf(targetFraction) }
    LaunchedEffect(targetFraction) {
        dragFraction = targetFraction
    }
    val animatedFraction by animateFloatAsState(
        targetValue = dragFraction,
        animationSpec = motionSpecs.tween(MotionDuration.Standard),
        label = "${title}_eq_tone_knob",
    )
    val tickIdleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val glowColor = accentColor.copy(alpha = 0.28f)
    val knobFaceColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.92f)
    } else {
        Color(0xFF1A1A1C)
    }
    val knobEdgeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val valueText = remember(clampedValue, safeRange) {
        val percent = if (safeRange.start < 0f) {
            (clampedValue * 100f).roundToInt()
        } else {
            ((clampedValue.coerceAtLeast(0f)) * 100f).roundToInt()
        }
        if (safeRange.start < 0f && percent > 0) "+$percent" else percent.toString()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .horizontalGestureSafe()
                .pointerInput(title, safeRange.start, safeRange.endInclusive) {
                    detectTapGestures { offset ->
                        val fraction = circularKnobValueForOffset(
                            offset = offset,
                            size = Size(size.width.toFloat(), size.height.toFloat()),
                            startAngleDegrees = 140f,
                            sweepAngleDegrees = 260f,
                        )
                        dragFraction = fraction
                        currentOnValueChange(
                            safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                        )
                    }
                }
                .pointerInput(title, safeRange.start, safeRange.endInclusive) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val fraction = circularKnobValueForOffset(
                                offset = offset,
                                size = Size(size.width.toFloat(), size.height.toFloat()),
                                startAngleDegrees = 140f,
                                sweepAngleDegrees = 260f,
                            )
                            dragFraction = fraction
                            currentOnValueChange(
                                safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = circularKnobValueForOffset(
                                offset = change.position,
                                size = Size(size.width.toFloat(), size.height.toFloat()),
                                startAngleDegrees = 140f,
                                sweepAngleDegrees = 260f,
                            )
                            dragFraction = fraction
                            currentOnValueChange(
                                safeRange.start + ((safeRange.endInclusive - safeRange.start) * fraction),
                            )
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val glowWidth = 12.dp.toPx()
                val outerPadding = 9.dp.toPx()
                val radius = ((size.minDimension - outerPadding * 2f) / 2f).coerceAtLeast(1f)
                val center = Offset(size.width / 2f, size.height / 2f)
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)
                val startAngle = 140f
                val sweepAngle = 260f
                val activeSweep = sweepAngle * animatedFraction
                val tickCount = 34
                val tickOuterRadius = radius + 5.dp.toPx()
                val tickInnerRadius = tickOuterRadius - 6.dp.toPx()
                val activeTickCount = (animatedFraction * (tickCount - 1)).roundToInt()

                repeat(tickCount) { tickIndex ->
                    val fraction = tickIndex / (tickCount - 1).toFloat()
                    val angleDegrees = startAngle + (sweepAngle * fraction)
                    val angleRadians = Math.toRadians(angleDegrees.toDouble())
                    val start = Offset(
                        x = center.x + (cos(angleRadians) * tickInnerRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickInnerRadius).toFloat(),
                    )
                    val end = Offset(
                        x = center.x + (cos(angleRadians) * tickOuterRadius).toFloat(),
                        y = center.y + (sin(angleRadians) * tickOuterRadius).toFloat(),
                    )
                    drawLine(
                        color = if (tickIndex <= activeTickCount) accentColor else tickIdleColor,
                        start = start,
                        end = end,
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Square,
                    )
                }
                if (activeSweep > 0f) {
                    drawArc(
                        color = glowColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = glowWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                drawCircle(
                    color = knobFaceColor,
                    radius = radius * 0.64f,
                    center = center,
                )
                drawCircle(
                    color = knobEdgeColor,
                    radius = radius * 0.66f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )

                val pointerAngle = Math.toRadians((startAngle + activeSweep).toDouble())
                val pointerRadius = radius * 0.56f
                val pointerCenter = Offset(
                    x = center.x + (cos(pointerAngle) * pointerRadius).toFloat(),
                    y = center.y + (sin(pointerAngle) * pointerRadius).toFloat(),
                )
                drawCircle(
                    color = accentColor,
                    radius = 3.dp.toPx(),
                    center = pointerCenter,
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.96f),
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = elovaireScaledSp(10f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val clampedValue = value.coerceIn(-1f, 1f)
    val accent = if (clampedValue >= 0f) Color(0xFF7D8BFF) else Color(0xFFFF6F61)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(190.dp)
                .clip(RoundedCornerShape(ElovaireRadii.module))
                .background(readableCardSurfaceColor())
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            onValueChange((clampedValue - (dragAmount / 180f)).coerceIn(-1f, 1f))
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = 1f - (offset.y / size.height.toFloat())
                        onValueChange(((fraction * 2f) - 1f).coerceIn(-1f, 1f))
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp),
            ) {
                val trackWidth = 6.dp.toPx()
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val knobRadius = 8.dp.toPx()
                val travel = (size.height / 2f) - knobRadius - 8.dp.toPx()
                val knobY = centerY - (travel * clampedValue)

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    topLeft = Offset(centerX - (trackWidth / 2f), 8.dp.toPx()),
                    size = Size(trackWidth, size.height - 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth, trackWidth),
                )

                val fillTop = minOf(centerY, knobY)
                val fillBottom = maxOf(centerY, knobY)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.94f),
                            accent.copy(alpha = 0.55f),
                        ),
                    ),
                    topLeft = Offset(centerX - (trackWidth / 2f), fillTop),
                    size = Size(trackWidth, (fillBottom - fillTop).coerceAtLeast(trackWidth)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth, trackWidth),
                )

                drawCircle(
                    color = accent.copy(alpha = 0.28f),
                    radius = knobRadius * 1.75f,
                    center = Offset(centerX, knobY),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.96f),
                    radius = knobRadius,
                    center = Offset(centerX, knobY),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = readableSecondaryTextColor(),
        )
    }
}

@Composable
private fun EqMacroSliderRow(
    title: String,
    value: Float,
    valueText: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                ),
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = elovaireScaledSp(18f)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            )
        }
        ThinContinuousSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

private data class EqPresetDefinition(
    val name: String,
    val settings: EqSettings,
)

@Composable
private fun EqPresetMenu(
    currentSettings: EqSettings,
    selectedPresetName: String?,
    equalizerEnabled: Boolean,
    onApplyPreset: (String, EqSettings) -> Unit,
    onReset: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val presets = remember { eqPresetDefinitions() }
    val horizontalScrollState = rememberScrollState()
    val activePresetName = remember(currentSettings, selectedPresetName, equalizerEnabled, presets) {
        if (!equalizerEnabled) return@remember null
        selectedPresetName?.takeIf { selectedName ->
            presets.any { preset -> preset.name == selectedName }
        } ?: currentSettings.matchingEqPresetName(presets)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGestureSafe()
            .horizontalScroll(horizontalScrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EqPresetPill(
            label = uiPhrase(language, UiPhrase.Reset),
            selected = activePresetName == null && EqValuePolicy.hasSignalAlteringEffects(currentSettings),
            emphasized = true,
            onClick = onReset,
        )
        presets.forEach { preset ->
            EqPresetPill(
                label = preset.name,
                selected = preset.name == activePresetName,
                onClick = {
                    onApplyPreset(
                        preset.name,
                        currentSettings.copy(
                            bands = preset.settings.bands,
                        ),
                    )
                },
            )
        }
    }
}

private fun eqPresetDefinitions(): List<EqPresetDefinition> =
    listOf(
        eqPreset("Electronic", 0.40f, 0.58f, 0.42f, 0.26f, 0.10f, 0.08f, 0.16f, 0.24f),
        eqPreset("Jazz", 0.18f, 0.26f, 0.14f, -0.08f, 0.10f, 0.22f, 0.18f, 0.12f),
        eqPreset("Classical", 0.12f, 0.16f, 0.08f, -0.04f, 0.06f, 0.18f, 0.24f, 0.18f),
        eqPreset("Acoustic", 0.10f, 0.14f, 0.06f, -0.12f, 0.14f, 0.20f, 0.18f, 0.10f),
        eqPreset("Pop", 0.22f, 0.28f, 0.16f, -0.06f, 0.18f, 0.24f, 0.18f, 0.10f),
        eqPreset("Rock", 0.28f, 0.22f, 0.10f, -0.12f, 0.08f, 0.18f, 0.28f, 0.22f),
        eqPreset("Metal", 0.22f, 0.18f, 0.08f, -0.14f, 0.12f, 0.24f, 0.30f, 0.26f),
        eqPreset("Vocal", -0.08f, -0.12f, -0.04f, -0.10f, 0.20f, 0.28f, 0.16f, 0.06f),
        eqPreset("R&B", 0.28f, 0.32f, 0.20f, -0.06f, 0.20f, 0.22f, 0.10f, 0.06f),
        eqPreset("Soul", 0.20f, 0.24f, 0.18f, -0.04f, 0.18f, 0.24f, 0.10f, 0.04f),
        eqPreset("Hip-Hop", 0.42f, 0.46f, 0.22f, -0.12f, 0.10f, 0.12f, 0.08f, 0.04f),
    )

internal fun EqSettings.matchingEqPresetName(): String? = matchingEqPresetName(eqPresetDefinitions())

private fun EqSettings.matchingEqPresetName(presets: List<EqPresetDefinition>): String? {
    val currentBands = normalizedBandValues()
    return presets.firstOrNull { it.settings.normalizedBandValues() == currentBands }?.name
}

@Composable
private fun SpaciousnessModeMenu(
    currentMode: SpaciousnessMode,
    spaciousnessAmount: Float,
    onModeSelected: (SpaciousnessMode) -> Unit,
) {
    val language = LocalAppLanguage.current
    val modes = remember {
        listOf(
            SpaciousnessMode.StereoWidth,
            SpaciousnessMode.CrossfeedDepth,
            SpaciousnessMode.EarlyReflectionRoom,
            SpaciousnessMode.Philharmony,
            SpaciousnessMode.HaasSpace,
            SpaciousnessMode.HarmonicAir,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGestureSafe()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { mode ->
            EqPresetPill(
                label = mode.displayLabel(language),
                selected = spaciousnessAmount > 0.001f && mode == currentMode,
                useSubtleIdleBackground = true,
                onClick = {
                    onModeSelected(
                        if (spaciousnessAmount > 0.001f && mode == currentMode) {
                            SpaciousnessMode.Off
                        } else {
                            mode
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun EqPresetPill(
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    useSubtleIdleBackground: Boolean = false,
    onClick: () -> Unit,
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    } else if (useSubtleIdleBackground) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier.elovairePressScale(
            interactionSource = interactionSource,
            pressedScale = 0.96f,
            animationSpec = ElovaireMotion.bounceSpringSpec(),
            label = "${label}_eq_preset_scale",
        ),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .clip(RoundedCornerShape(ElovaireRadii.pill))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
    }
}

@Composable
private fun EqResponseGraph(
    settings: EqSettings,
    onBandChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val graphPointCount = EqualizerDspModel.BAND_COUNT
    val animatedBandValues = List(graphPointCount) { index ->
        val target = settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = motionSpecs.tween(120, easing = FastOutSlowInEasing),
            label = "eq_band_$index",
        )
        animated
    }
    val bandValues = remember(animatedBandValues) {
        normalizeEqBandValues(animatedBandValues, graphPointCount)
    }
    val bandFractions = remember { eqBandFractions() }
    val accentColor = Color(0xFF39E38E)
    val guideColor = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .pointerInput(bandFractions) {
                detectTapGestures { offset ->
                    if (size.width == 0 || size.height == 0) return@detectTapGestures
                    val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                    val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                    val bandIndex = nearestEqBandIndex(
                        fraction = ((offset.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
                        bandFractions = bandFractions,
                    )
                    onBandChanged(
                        bandIndex,
                        eqGraphYToNormalized(
                            y = offset.y,
                            height = size.height.toFloat(),
                            config = eqGraphConfig,
                        ),
                    )
                }
            }
            .pointerInput(bandFractions) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                        val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                        val bandIndex = nearestEqBandIndex(
                            fraction = ((offset.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
                            bandFractions = bandFractions,
                        )
                        onBandChanged(
                            bandIndex,
                            eqGraphYToNormalized(
                                y = offset.y,
                                height = size.height.toFloat(),
                                config = eqGraphConfig,
                            ),
                        )
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        val horizontalPadding = with(density) { EQ_GRAPH_EDGE_PADDING.toPx() }
                        val graphWidth = (size.width.toFloat() - horizontalPadding * 2f).coerceAtLeast(1f)
                        val index = nearestEqBandIndex(
                            fraction = ((change.position.x - horizontalPadding) / graphWidth).coerceIn(0f, 1f),
                            bandFractions = bandFractions,
                        )
                        onBandChanged(
                            index,
                            eqGraphYToNormalized(
                                y = change.position.y,
                                height = size.height.toFloat(),
                                config = eqGraphConfig,
                            ),
                        )
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val topPadding = size.height * 0.08f
            val bottomPadding = size.height * 0.12f
            val graphHeight = size.height - topPadding - bottomPadding
            val horizontalPadding = EQ_GRAPH_EDGE_PADDING.toPx()
            val graphWidth = (size.width - horizontalPadding * 2f).coerceAtLeast(1f)
            val zeroDbFraction = ((0f - eqGraphConfig.minBandGainDb) / (eqGraphConfig.maxBandGainDb - eqGraphConfig.minBandGainDb))
                .coerceIn(0f, 1f)
            val midY = topPadding + (graphHeight * (1f - zeroDbFraction))

            eqDbLevels().forEach { levelDb ->
                val y = topPadding + (graphHeight * (1f - eqLevelFraction(levelDb, eqGraphConfig)))
                drawLine(
                    color = guideColor.copy(alpha = if (levelDb == 0f) 0.12f else 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            bandValues.forEachIndexed { index, band ->
                val x = horizontalPadding + graphWidth * bandFractions.getOrElse(index) { 0f }
                val y = topPadding + (graphHeight * (1f - EqualizerDspModel.bandGraphFraction(band, eqGraphConfig)))
                val trackWidth = 5.dp.toPx()
                val activeWidth = 3.dp.toPx()
                val thumbWidth = 9.dp.toPx()
                val thumbHeight = 24.dp.toPx()
                val activeTop = min(y, midY)
                val activeHeight = max(2.dp.toPx(), kotlin.math.abs(y - midY))
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.08f),
                    topLeft = Offset(x - trackWidth * 1.45f, topPadding - 8.dp.toPx()),
                    size = Size(trackWidth * 2.9f, graphHeight + 16.dp.toPx()),
                    cornerRadius = CornerRadius(trackWidth * 2.9f, trackWidth * 2.9f),
                )
                drawRoundRect(
                    color = guideColor.copy(alpha = 0.05f),
                    topLeft = Offset(x - trackWidth / 2f, topPadding),
                    size = Size(trackWidth, graphHeight),
                    cornerRadius = CornerRadius(trackWidth, trackWidth),
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.18f),
                    start = Offset(x, midY),
                    end = Offset(x, y),
                    strokeWidth = activeWidth * 2.1f,
                    cap = StrokeCap.Round,
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(x - activeWidth / 2f, activeTop),
                    size = Size(activeWidth, activeHeight),
                    cornerRadius = CornerRadius(activeWidth, activeWidth),
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.16f),
                    topLeft = Offset(x - thumbWidth * 0.8f, y - thumbHeight / 2f),
                    size = Size(thumbWidth * 1.6f, thumbHeight),
                    cornerRadius = CornerRadius(thumbWidth, thumbWidth),
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(x - thumbWidth / 2f, y - thumbHeight / 2f),
                    size = Size(thumbWidth, thumbHeight),
                    cornerRadius = CornerRadius(thumbWidth, thumbWidth),
                )
            }
        }
    }
}

private fun eqGraphYToNormalized(
    y: Float,
    height: Float,
    config: EqualizerDspConfig,
): Float {
    if (height <= 0f) return 0f
    val topPadding = height * 0.08f
    val bottomPadding = height * 0.12f
    val graphHeight = (height - topPadding - bottomPadding).coerceAtLeast(1f)
    val graphFraction = (1f - ((y - topPadding) / graphHeight)).coerceIn(0f, 1f)
    return EqualizerDspModel.graphFractionToNormalized(graphFraction, config)
}

@Composable
private fun EqMiniResponseGraph(
    settings: EqSettings,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val graphPointCount = EqualizerDspModel.BAND_COUNT
    val animatedBandValues = List(graphPointCount) { index ->
        val target = settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = motionSpecs.tween(160, easing = FastOutSlowInEasing),
            label = "eq_mini_band_$index",
        )
        animated
    }
    val bandValues = remember(animatedBandValues) {
        normalizeEqBandValues(animatedBandValues, graphPointCount)
    }
    val bandFractions = remember { eqBandFractions() }
    val accentColor = Color(0xFF39E38E)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.module))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val horizontalPadding = 14.dp.toPx()
            val topPadding = size.height * 0.18f
            val bottomPadding = size.height * 0.2f
            val graphHeight = size.height - topPadding - bottomPadding
            val graphWidth = size.width - horizontalPadding * 2f
            val zeroDbFraction = ((0f - eqGraphConfig.minBandGainDb) / (eqGraphConfig.maxBandGainDb - eqGraphConfig.minBandGainDb))
                .coerceIn(0f, 1f)
            val midY = topPadding + (graphHeight * (1f - zeroDbFraction))
            val points = bandValues.mapIndexed { index, band ->
                val x = horizontalPadding + graphWidth * bandFractions.getOrElse(index) { 0f }
                val y = topPadding + (graphHeight * (1f - EqualizerDspModel.bandGraphFraction(band, eqGraphConfig)))
                Offset(x, y)
            }
            if (points.isEmpty()) return@Canvas
            val strokePath = smoothPathFromPoints(points)
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, midY)
                lineTo(points.first().x, midY)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            drawPath(
                path = strokePath,
                color = accentColor.copy(alpha = 0.2f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                path = strokePath,
                color = accentColor,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun EqDbScale(
    modifier: Modifier = Modifier,
) {
    val eqGraphConfig = remember { EqualizerDspConfig() }
    val markerColor = readableSecondaryTextColor().copy(alpha = 0.78f)
    val levels = remember { eqDbLevels() }
    BoxWithConstraints(modifier = modifier) {
        val topPadding = maxHeight * 0.08f
        val bottomPadding = maxHeight * 0.12f
        val graphHeight = maxHeight - topPadding - bottomPadding
        levels.forEach { levelDb ->
            val positionY = topPadding + (graphHeight * (1f - eqLevelFraction(levelDb, eqGraphConfig)))
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = positionY - 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatEqDbLabel(levelDb),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = elovaireScaledSp(9f)),
                    color = markerColor,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { markerIndex ->
                        Box(
                            modifier = Modifier
                                .width((5 - markerIndex).dp)
                                .height(1.5.dp)
                                .clip(RoundedCornerShape(ElovaireRadii.pill))
                                .background(markerColor.copy(alpha = 0.65f - (markerIndex * 0.14f))),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EqBandFrequencyLabels(
    contentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val labels = remember {
        EqualizerDspModel.BAND_CENTER_FREQUENCIES_HZ.map(::formatEqFrequencyLabel)
    }
    val bandFractions = remember { eqBandFractions() }
    BoxWithConstraints(
        modifier = modifier
            .width(contentWidth)
            .height(EQ_BAND_LABEL_HEIGHT),
    ) {
        val labelWidth = 36.dp
        val graphWidth = maxWidth - (EQ_GRAPH_EDGE_PADDING * 2)
        labels.forEachIndexed { index, label ->
            val fraction = bandFractions.getOrElse(index) { 0f }
            Box(
                modifier = Modifier
                    .width(labelWidth)
                    .align(Alignment.CenterStart)
                    .offset(x = EQ_GRAPH_EDGE_PADDING + graphWidth * fraction - (labelWidth / 2)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = elovaireScaledSp(9.2f)),
                    color = readableSecondaryTextColor().copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun EqHorizontalScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    contentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val motionSpecs = rememberMotionSpecs()
    var isDragging by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(scrollState.isScrollInProgress, isDragging) {
        if (scrollState.isScrollInProgress || isDragging) {
            visible = true
        } else {
            delay(2000L)
            visible = false
        }
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val contentWidthPx = with(density) { contentWidth.toPx() }.coerceAtLeast(viewportWidthPx)
        val maxScrollPx = scrollState.maxValue.toFloat().coerceAtLeast(0f)
        val viewportFraction = (viewportWidthPx / contentWidthPx).coerceIn(0.08f, 1f)
        val thumbWidthPx = (viewportWidthPx * viewportFraction).coerceAtLeast(with(density) { 46.dp.toPx() })
        val thumbTravelPx = (viewportWidthPx - thumbWidthPx).coerceAtLeast(0f)
        val thumbOffsetFraction = if (maxScrollPx <= 0f) 0f else (scrollState.value / maxScrollPx).coerceIn(0f, 1f)
        val thumbOffsetPx = thumbTravelPx * thumbOffsetFraction
        val trackColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            InkText.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.14f)
        }
        val thumbColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            InkText.copy(alpha = 0.58f)
        } else {
            Color.White.copy(alpha = 0.62f)
        }
        val updateScrollFromX: (Float) -> Unit = { xPosition ->
            if (maxScrollPx > 0f && viewportWidthPx > 0f) {
                val fraction = ((xPosition - (thumbWidthPx / 2f)) / thumbTravelPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val targetScroll = (maxScrollPx * fraction).roundToInt()
                scrollJob?.cancel()
                scrollJob = scope.launch {
                    scrollState.scrollTo(targetScroll)
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
                    .matchParentSize()
                    .pointerInput(viewportWidthPx, maxScrollPx) {
                        detectTapGestures { offset ->
                            isDragging = true
                            updateScrollFromX(offset.x)
                            isDragging = false
                        }
                    }
                    .pointerInput(viewportWidthPx, maxScrollPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                updateScrollFromX(offset.x)
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                updateScrollFromX(change.position.x)
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(trackColor),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(x = thumbOffsetPx.roundToInt(), y = 0) }
                        .width(with(density) { thumbWidthPx.toDp() })
                        .height(4.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(thumbColor),
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryText(
    title: String,
    @DrawableRes iconResId: Int? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResId != null) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = readableMutedIconColor(),
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun ThinContinuousSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    lineThickness: Dp = 2.dp,
    knobSize: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val fraction = ((coercedValue - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    val knobColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val inactiveLineColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .horizontalGestureSafe(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val trackStartPx = knobSizePx / 2f
        val trackWidthPx = (maxWidthPx - knobSizePx).coerceAtLeast(1f)
        val trackStart = with(density) { trackStartPx.toDp() }
        val trackWidth = with(density) { trackWidthPx.toDp() }
        val activeWidth by animateDpAsState(
            targetValue = trackWidth * fraction,
            animationSpec = motionSpecs.tween(durationMillis = 70),
            label = "eq_macro_slider_fill",
        )
        val knobOffset by animateDpAsState(
            targetValue = with(density) { (trackStartPx + trackWidthPx * fraction - knobSizePx / 2f).toDp() },
            animationSpec = motionSpecs.tween(durationMillis = 70),
            label = "eq_macro_slider_knob",
        )

        val updateFromX: (Float) -> Unit = { xPosition ->
            if (maxWidthPx > 0f) {
                val normalized = ((xPosition - trackStartPx) / trackWidthPx).coerceIn(0f, 1f)
                val rangedValue = valueRange.start + ((valueRange.endInclusive - valueRange.start) * normalized)
                currentOnValueChange(rangedValue.coerceIn(valueRange.start, valueRange.endInclusive))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxWidthPx, valueRange.start, valueRange.endInclusive) {
                    detectTapGestures { offset ->
                        updateFromX(offset.x)
                    }
                }
                .pointerInput(maxWidthPx, valueRange.start, valueRange.endInclusive) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateFromX(offset.x) },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateFromX(change.position.x)
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = trackStart)
                    .width(trackWidth)
                    .height(lineThickness)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(inactiveLineColor),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = trackStart)
                    .width(activeWidth)
                    .height(lineThickness)
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .background(knobColor),
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = knobOffset.roundToPx(), y = 0) }
                    .size(knobSize)
                    .clip(CircleShape)
                    .background(knobColor)
                    .align(Alignment.CenterStart),
            )
        }
    }
}

private fun eqBandFractions(): List<Float> {
    val count = EqualizerDspModel.BAND_COUNT
    if (count <= 1) return emptyList()
    return List(count) { index -> index.toFloat() / (count - 1).toFloat() }
}

private fun eqDbLevels(): List<Float> = listOf(8f, 4f, 0f, -4f, -8f)

private fun eqLevelFraction(
    levelDb: Float,
    config: EqualizerDspConfig = EqualizerDspConfig(),
): Float {
    return ((levelDb - config.minBandGainDb) / (config.maxBandGainDb - config.minBandGainDb))
        .coerceIn(0f, 1f)
}

private fun formatEqDbLabel(levelDb: Float): String {
    return when {
        levelDb > 0f -> "+${levelDb.roundToInt()}"
        levelDb < 0f -> levelDb.roundToInt().toString()
        else -> "0"
    }
}

private fun nearestEqBandIndex(
    fraction: Float,
    bandFractions: List<Float>,
): Int {
    return bandFractions
        .withIndex()
        .minByOrNull { (_, value) -> kotlin.math.abs(value - fraction) }
        ?.index
        ?: 0
}

private fun formatEqFrequencyLabel(frequencyHz: Float): String {
    return when {
        frequencyHz >= 1_000f -> {
            val kilo = frequencyHz / 1_000f
            formatEqKiloLabel(kilo)
        }
        frequencyHz % 1f == 0f -> frequencyHz.roundToInt().toString()
        else -> frequencyHz.toString()
    }
}

private fun formatEqKiloLabel(kiloValue: Float): String {
    val rawLabel = when {
        kiloValue >= 10f || kiloValue % 1f == 0f -> kiloValue.roundToInt().toString()
        (kiloValue * 10f) % 1f == 0f -> String.format(java.util.Locale.ROOT, "%.1f", kiloValue)
        else -> String.format(java.util.Locale.ROOT, "%.2f", kiloValue)
    }
    val formatted = if ('.' in rawLabel) rawLabel.trimEnd('0').trimEnd('.') else rawLabel
    return "${formatted}k"
}

private fun normalizeEqBandValues(
    values: List<Float>,
    targetCount: Int,
): List<Float> {
    if (values.isEmpty()) return List(targetCount) { 0f }
    return List(targetCount) { index ->
        values.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
    }
}

private fun eqPreset(
    name: String,
    bass: Float,
    subBass: Float,
    lowBass: Float,
    lowMid: Float,
    presence: Float,
    upperMid: Float,
    brilliance: Float,
    air: Float,
): EqPresetDefinition {
    fun adjustedPresetBandValue(value: Float): Float {
        return if (value < 0f) {
            (value * 1.25f).coerceIn(-1f, 0f)
        } else {
            value
        }
    }
    val bandShape = List(EqualizerDspModel.BAND_COUNT) { index ->
        when (EqualizerDspModel.bandDefinition(index).frequencyHz) {
            in 0f..30f -> adjustedPresetBandValue(bass)
            in 30.0001f..60f -> adjustedPresetBandValue(subBass)
            in 60.0001f..350f -> adjustedPresetBandValue(lowBass)
            in 350.0001f..750f -> adjustedPresetBandValue(lowMid)
            in 750.0001f..1_500f -> adjustedPresetBandValue(presence)
            in 1_500.0001f..3_000f -> adjustedPresetBandValue(upperMid)
            in 3_000.0001f..8_000f -> adjustedPresetBandValue(brilliance)
            else -> adjustedPresetBandValue(air)
        }.coerceIn(-1f, 1f)
    }
    val settings = EqSettings(
        bands = bandShape,
        bass = 0f,
        treble = 0f,
        spaciousness = 0f,
        spaciousnessMode = SpaciousnessMode.Off,
    ).normalizedEqSettings()
    return EqPresetDefinition(name = name, settings = settings)
}

private fun EqSettings.normalizedBandValues(): List<Float> {
    return List(EqualizerDspModel.BAND_COUNT) { index ->
        bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
    }
}

private fun EqSettings.normalizedEqSettings(): EqSettings {
    return copy(
        bands = normalizedBandValues(),
        bass = bass.coerceIn(-1f, 1f),
        treble = treble.coerceIn(-1f, 1f),
        spaciousness = spaciousness.coerceIn(0f, 1f),
    )
}

private fun smoothPathFromPoints(points: List<Offset>): androidx.compose.ui.graphics.Path {
    return androidx.compose.ui.graphics.Path().apply {
        if (points.isEmpty()) return@apply
        moveTo(points.first().x, points.first().y)
        if (points.size == 1) return@apply
        for (index in 1 until points.size) {
            val previous = points[index - 1]
            val current = points[index]
            val midPoint = Offset(
                x = (previous.x + current.x) / 2f,
                y = (previous.y + current.y) / 2f,
            )
            quadraticTo(previous.x, previous.y, midPoint.x, midPoint.y)
        }
        val last = points.last()
        lineTo(last.x, last.y)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.12f else 0.06f,
            )
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        modifier = Modifier
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ControlButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        contentColor = if (emphasized) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (emphasized) 22.dp else 0.dp,
        modifier = Modifier.size(if (emphasized) 88.dp else 64.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
            )
        }
    }
}
