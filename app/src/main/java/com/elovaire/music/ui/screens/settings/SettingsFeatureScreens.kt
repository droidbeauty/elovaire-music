package elovaire.music.droidbeauty.app.ui.screens

import kotlin.math.roundToInt

import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSettingsPolicy
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.audiobookSettingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.audiobookExtraCopy
import elovaire.music.droidbeauty.app.ui.i18n.audiobookPlaybackCategory
import elovaire.music.droidbeauty.app.ui.i18n.builtInSmartPlaylistTitle
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistDescription
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistSettingsCopy
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.SectionTitleRow
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

@Composable
internal fun AudiobookSettingsScreen(
    settings: AudiobookSettings,
    playbackSpeed: Float,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onRewindChanged: (Int) -> Unit,
    onForwardChanged: (Int) -> Unit,
    onResumePlaybackChanged: (Boolean) -> Unit,
    onPlaybackSpeedChanged: (Float) -> Unit,
    onRewindOnResumeChanged: (Boolean) -> Unit,
) {
    val copy = audiobookSettingsCopy(LocalAppLanguage.current)
    val extraCopy = audiobookExtraCopy(LocalAppLanguage.current)
    val listState = rememberElovaireLazyListState("audiobook_settings")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 20.dp,
                end = 18.dp,
                bottom = bottomPadding + buttonNavigationScrollBoost() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SectionTitleRow(title = audiobookPlaybackCategory(LocalAppLanguage.current), compact = true)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap)) {
                                    Text(extraCopy.speed, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(extraCopy.speedSubtitle, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                                Text("${(playbackSpeed * 10).roundToInt() / 10f}×", style = MaterialTheme.typography.titleMedium)
                            }
                            SteppedSlider(
                                selectedIndex = ((playbackSpeed * 10).roundToInt() - 5).coerceIn(0, 20),
                                stepCount = 21,
                                onSelectedIndexChanged = { onPlaybackSpeedChanged((it + 5) / 10f) },
                                modifier = Modifier.testTag("audiobook_speed_slider").semantics {
                                    contentDescription = extraCopy.speed
                                    progressBarRangeInfo = ProgressBarRangeInfo(playbackSpeed.coerceIn(0.5f, 2.5f), 0.5f..2.5f, 19)
                                    setProgress { value -> onPlaybackSpeedChanged((value.coerceIn(0.5f, 2.5f) * 10).roundToInt() / 10f); true }
                                },
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("0.5×", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("2.5×", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        AudiobookSeekSettingRow(
                            title = copy.rewindAmount,
                            subtitle = "${settings.rewindSeconds}${copy.seconds}",
                            value = settings.rewindSeconds,
                            onValueChanged = onRewindChanged,
                        )
                        AudiobookSeekSettingRow(
                            title = copy.forwardAmount,
                            subtitle = "${settings.forwardSeconds}${copy.seconds}",
                            value = settings.forwardSeconds,
                            onValueChanged = onForwardChanged,
                        )
                    }
                }
            }
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingToggleRow(
                            title = copy.resumePlayback,
                            subtitle = copy.resumePlaybackSubtitle,
                            enabled = settings.resumePlayback,
                            onEnabledChanged = onResumePlaybackChanged,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        )
                        SettingToggleRow(
                            title = extraCopy.rewind,
                            subtitle = extraCopy.rewindSubtitle,
                            enabled = settings.rewindOnResume,
                            onEnabledChanged = onRewindOnResumeChanged,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 20.dp,
            bottomInset = bottomPadding + buttonNavigationScrollBoost(),
        )
    }
}

@Composable
private fun AudiobookSeekSettingRow(
    title: String,
    subtitle: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AudiobookStepButton(
                label = "−",
                enabled = value > 5,
                onClick = { onValueChanged((value - 5).coerceAtLeast(5)) },
            )
            Box(
                modifier = Modifier.size(width = 38.dp, height = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.toString(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = elovaireScaledSp(17f)),
                )
            }
            AudiobookStepButton(
                label = "+",
                enabled = value < 30,
                onClick = { onValueChanged((value + 5).coerceAtMost(30)) },
            )
        }
    }
}

@Composable
private fun AudiobookStepButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.9f else 0.35f),
            )
            .elovaireClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.35f),
        )
    }
}

@Composable
internal fun SmartPlaylistSettingsScreen(
    enabledTypes: Set<BuiltInSmartPlaylistType>,
    maxSongs: Int,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onTypeEnabledChanged: (BuiltInSmartPlaylistType, Boolean) -> Unit,
    onMaxSongsChanged: (Int) -> Unit,
) {
    val language = LocalAppLanguage.current
    val copy = smartPlaylistSettingsCopy(language)
    val listState = rememberElovaireLazyListState("smart_playlist_settings")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 20.dp,
                end = 18.dp,
                bottom = bottomPadding + buttonNavigationScrollBoost() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitleRow(title = copy.availableMixes, compact = true)
                        BuiltInSmartPlaylistType.entries.forEach { type ->
                            SettingToggleRow(
                                title = builtInSmartPlaylistTitle(type, language),
                                subtitle = smartPlaylistDescription(type, language),
                                enabled = type in enabledTypes,
                                onEnabledChanged = { enabled -> onTypeEnabledChanged(type, enabled) },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                            )
                        }
                    }
                }
            }
            item {
                ModuleCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap)) {
                            Text(
                                text = copy.maximumSongs,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                text = copy.maximumSongsSubtitle,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AudiobookStepButton(
                                label = "−",
                                enabled = maxSongs > SmartPlaylistSettingsPolicy.MIN_SONG_LIMIT,
                                onClick = {
                                    onMaxSongsChanged(
                                        (maxSongs - SmartPlaylistSettingsPolicy.SONG_LIMIT_STEP)
                                            .coerceAtLeast(SmartPlaylistSettingsPolicy.MIN_SONG_LIMIT),
                                    )
                                },
                            )
                            Box(
                                modifier = Modifier.size(width = 64.dp, height = 36.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = maxSongs.toString(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = elovaireScaledSp(18f)),
                                )
                            }
                            AudiobookStepButton(
                                label = "+",
                                enabled = maxSongs < SmartPlaylistSettingsPolicy.MAX_SONG_LIMIT,
                                onClick = {
                                    onMaxSongsChanged(
                                        (maxSongs + SmartPlaylistSettingsPolicy.SONG_LIMIT_STEP)
                                            .coerceAtMost(SmartPlaylistSettingsPolicy.MAX_SONG_LIMIT),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 8.dp,
            bottomInset = bottomPadding + buttonNavigationScrollBoost(),
        )
    }
}
