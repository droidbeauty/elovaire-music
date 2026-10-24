package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.upscalerCopy
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import kotlin.math.roundToInt

@Composable
internal fun UpscalerScreen(
    settings: UpscalerSettings,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onSettingsChanged: (UpscalerSettings) -> Unit,
) {
    val copy = upscalerCopy(LocalAppLanguage.current)
    val value = settings.sanitized()
    val factors = listOf(2, 4, 8)
    val listState = rememberElovaireLazyListState("upscaler_settings")
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp, end = 18.dp,
                top = topBarOccupiedHeight() + ElovaireSpacing.detailListTopGap,
                bottom = bottomPadding + buttonNavigationScrollBoost() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "upscaler", contentType = "settings") {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        UpscalerToggleRow(
                            title = copy.enabled,
                            subtitle = copy.subtitle,
                            checked = value.enabled,
                            onChanged = { onSettingsChanged(value.copy(enabled = it)) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(copy.multiplier, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            UpscalerSlider(
                                selected = factors.indexOf(value.multiplier),
                                stepCount = factors.size,
                                title = copy.multiplier,
                                stateLabel = "${value.multiplier}×",
                                onChanged = { onSettingsChanged(value.copy(multiplier = factors[it])) },
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                factors.forEach { Text("${it}×", style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface) }
                            }
                        }
                    }
                }
            }
            item {
                ModuleCard {
                    UpscalerToggleRow(
                        title = copy.precision,
                        subtitle = copy.precisionSubtitle,
                        checked = value.highPrecision,
                        onChanged = { onSettingsChanged(value.copy(highPrecision = it)) },
                    )
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${copy.headroom} · ${value.headroomDb} dB", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(copy.headroomSubtitle, style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        UpscalerSlider(
                            selected = value.headroomDb,
                            stepCount = 7,
                            title = copy.headroom,
                            stateLabel = "${value.headroomDb} dB",
                            onChanged = { onSettingsChanged(value.copy(headroomDb = it)) },
                        )
                    }
                }
            }
            item {
                Text(copy.explanation, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = listOf(TopBarActionSpec(
                iconResId = R.drawable.ic_lucide_rotate_ccw,
                contentDescription = uiPhrase(LocalAppLanguage.current, UiPhrase.Reset),
                enabled = value != UpscalerSettings(),
                onClick = { onSettingsChanged(UpscalerSettings()) },
            )),
        )
        FastScrollbar(state = listState, topInset = topBarOccupiedHeight() + 8.dp,
            bottomInset = bottomPadding + buttonNavigationScrollBoost())
    }
}

@Composable
private fun UpscalerToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    SettingToggleRow(
        title, subtitle, checked, onChanged,
        Modifier.fillMaxWidth().elovaireClickable { onChanged(!checked) }.clearAndSetSemantics {
            contentDescription = "$title. $subtitle"
            role = Role.Switch
            toggleableState = ToggleableState(checked)
            onClick { onChanged(!checked); true }
        },
    )
}

@Composable
private fun UpscalerSlider(
    selected: Int,
    stepCount: Int,
    title: String,
    stateLabel: String,
    onChanged: (Int) -> Unit,
) {
    SteppedSlider(
        selectedIndex = selected,
        stepCount = stepCount,
        onSelectedIndexChanged = onChanged,
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = title
            stateDescription = stateLabel
            progressBarRangeInfo = ProgressBarRangeInfo(selected.toFloat(), 0f..(stepCount - 1).toFloat(), stepCount - 2)
            setProgress { value ->
                if (!value.isFinite()) return@setProgress false
                onChanged(value.roundToInt().coerceIn(0, stepCount - 1))
                true
            }
        },
    )
}
