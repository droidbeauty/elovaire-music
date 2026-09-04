package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSettingsPolicy
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.audiobookSettingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.builtInSmartPlaylistTitle
import elovaire.music.droidbeauty.app.ui.i18n.smartPlaylistSettingsCopy
import elovaire.music.droidbeauty.app.ui.screens.common.ModuleCard
import elovaire.music.droidbeauty.app.ui.screens.common.SectionTitleRow
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

@Composable
internal fun AudiobookSettingsScreen(
    settings: AudiobookSettings,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onRewindChanged: (Int) -> Unit,
    onForwardChanged: (Int) -> Unit,
    onResumePlaybackChanged: (Boolean) -> Unit,
) {
    val copy = audiobookSettingsCopy(LocalAppLanguage.current)
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
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = bottomPadding + buttonNavigationScrollBoost() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SectionTitleRow(title = copy.title, compact = true)
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = copy.chapterMetadata,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                text = copy.chapterMetadataSubtitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
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
            Text(
                text = value.toString(),
                modifier = Modifier.size(width = 38.dp, height = 36.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = elovaireScaledSp(17f)),
            )
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
    Text(
        text = label,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.9f else 0.35f),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(top = 1.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.35f),
    )
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
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = bottomPadding + buttonNavigationScrollBoost() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionTitleRow(title = copy.availableMixes, compact = true)
                        BuiltInSmartPlaylistType.entries.forEach { type ->
                            SettingToggleRow(
                                title = builtInSmartPlaylistTitle(type, language),
                                subtitle = copy.availableMixes,
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
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionTitleRow(title = copy.maximumSongs, compact = true)
                        Text(
                            text = copy.maximumSongsSubtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
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
                            Text(
                                text = maxSongs.toString(),
                                modifier = Modifier.size(width = 64.dp, height = 36.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = elovaireScaledSp(18f)),
                            )
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
