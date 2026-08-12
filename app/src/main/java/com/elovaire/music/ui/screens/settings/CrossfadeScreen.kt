package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.crossfadeCopy
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy

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
                top = topBarOccupiedHeight() + 8.dp,
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
                        Text(
                            text = copy.fadeLength,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = copy.fadeLengthExplanation,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${formatSeconds(durationSeconds)} seconds",
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ThinContinuousSlider(
                            value = durationSeconds,
                            onValueChange = { value ->
                                val snapped = ((value * 2f).roundToInt() / 2f)
                                    .coerceIn(2f, 5f)
                                onDurationChanged((snapped * 1_000f).roundToInt().toLong())
                            },
                            valueRange = 2f..5f,
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
                        Text(
                            text = copy.silenceDetection,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = copy.silenceDetectionExplanation,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${silenceDb.toInt()} dB",
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ThinContinuousSlider(
                            value = silenceDb,
                            onValueChange = { value ->
                                val snapped = ((value + 100f) / 5f).roundToInt() * 5f - 100f
                                onSilenceThresholdChanged(snapped.coerceIn(-100f, -80f))
                            },
                            valueRange = -100f..-80f,
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
