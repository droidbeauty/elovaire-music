package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import kotlin.math.roundToInt
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.crossfadeCopy
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing

@Composable
internal fun CrossfadeScreen(
    durationMs: Long,
    silenceThresholdDb: Float,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onDurationChanged: (Long) -> Unit,
    onSilenceThresholdChanged: (Float) -> Unit,
) {
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val copy = crossfadeCopy(LocalAppLanguage.current)
    val durationSeconds = durationMs.coerceIn(2_000L, 5_000L) / 1_000f
    val silenceDb = silenceThresholdDb.coerceIn(-100f, -80f)
    val durationStep = (((durationSeconds - 2f) * 2f).roundToInt()).coerceIn(0, 6)
    val silenceStep = (((silenceDb + 100f) / 5f).roundToInt()).coerceIn(0, 4)
    BoxWithCrossfadeTopBar(
        listState = listState,
        bottomPadding = bottomPadding,
        onBack = onBack,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                end = 18.dp,
                bottom = bottomPadding + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ModuleCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_squares_intersect),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = copy.fadeLength,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                        Text(
                            text = copy.fadeLengthExplanation,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${formatSeconds(durationSeconds)} seconds",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SteppedSlider(
                            selectedIndex = durationStep,
                            stepCount = 7,
                            onSelectedIndexChanged = { index ->
                                onDurationChanged((2_000L + index * 500L).coerceIn(2_000L, 5_000L))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                ModuleCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_audio_lines_x),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = copy.silenceDetection,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                        Text(
                            text = copy.silenceDetectionExplanation,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${silenceDb.toInt()} dB",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SteppedSlider(
                            selectedIndex = silenceStep,
                            stepCount = 5,
                            onSelectedIndexChanged = { index ->
                                onSilenceThresholdChanged((-100f + index * 5f).coerceIn(-100f, -80f))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxWithCrossfadeTopBar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    bottomPadding: Dp,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
    ) {
        content()
        PinnedBackTopBar(
            title = settingsCopy(LocalAppLanguage.current).crossfadeTitle,
            onBack = onBack,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 8.dp,
            bottomInset = bottomPadding + buttonNavigationScrollBoost(),
        )
    }
}

private fun formatSeconds(value: Float): String {
    return if (value % 1f == 0f) value.toInt().toString() else value.toString()
}
