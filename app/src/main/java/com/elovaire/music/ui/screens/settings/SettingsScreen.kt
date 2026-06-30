package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.ui.i18n.SettingsLanguageCopy
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.libraryFoldersCopy
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.privacySafetyCopy
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.ToggleEnabledGreen
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlin.math.roundToInt

@Composable
internal fun SettingsScreen(
    themeMode: ThemeMode,
    textSizePreset: TextSizePreset,
    appLanguage: AppLanguage,
    eqSettings: EqSettings,
    onlineLyricsLookupEnabled: Boolean,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onTextSizePresetSelected: (TextSizePreset) -> Unit,
    onAppLanguageSelected: (AppLanguage) -> Unit,
    onBassChanged: (Float) -> Unit,
    onMidrangeChanged: (Float) -> Unit,
    onTrebleChanged: (Float) -> Unit,
    onMonoPlaybackChanged: (Boolean) -> Unit,
    onOnlineLyricsLookupChanged: (Boolean) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenLibraryFolders: () -> Unit,
    onOpenPrivacySafety: () -> Unit,
    onOpenChangelog: () -> Unit,
    onScanLibrary: () -> Unit,
    showUpdateChecks: Boolean,
    onCheckForUpdates: () -> Unit,
) {
    val listState = rememberElovaireLazyListState("settings_screen")
    val copy = remember(appLanguage) { settingsCopy(appLanguage) }
    val foldersCopy = remember(appLanguage) { libraryFoldersCopy(appLanguage) }
    val privacyCopy = remember(appLanguage) {
        privacySafetyCopy(
            language = appLanguage,
            includeUpdates = BuildConfig.ENABLE_GITHUB_UPDATE_FLOW,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 8.dp,
                end = 18.dp,
                bottom = bottomPadding + buttonNavigationScrollBoost(),
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsSectionHeader(
                    title = copy.appearance,
                    iconResId = R.drawable.ic_lucide_palette,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SectionTitleRow(
                            title = copy.theme,
                            compact = true,
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ThemeModeSegmentedPicker(
                                selectedMode = themeMode,
                                onModeSelected = onThemeModeSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionTitleRow(
                                title = copy.textSize,
                                compact = true,
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                TextSizeStepper(
                                    selectedPreset = textSizePreset,
                                    onPresetSelected = onTextSizePresetSelected,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                )
                            }
                        }
                        LanguagePickerRow(
                            selectedLanguage = appLanguage,
                            copy = copy,
                            onLanguageSelected = onAppLanguageSelected,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = copy.sound,
                    iconResId = R.drawable.ic_lucide_volume_2,
                )
            }

            item {
                ModuleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(30.dp),
                        ) {
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Bass),
                                value = eqSettings.bass.coerceIn(0f, 1f),
                                valueRange = 0f..1f,
                                accentColor = Color(0xFF2FE08D),
                                modifier = Modifier.weight(1f),
                                onValueChange = onBassChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Midrange),
                                value = eqSettings.midrange.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFF39C2FF),
                                modifier = Modifier.weight(1f),
                                onValueChange = onMidrangeChanged,
                            )
                            EqToneKnob(
                                title = uiPhrase(appLanguage, UiPhrase.Treble),
                                value = eqSettings.treble.coerceIn(-1f, 1f),
                                valueRange = -1f..1f,
                                accentColor = Color(0xFFFFB056),
                                modifier = Modifier.weight(1f),
                                onValueChange = onTrebleChanged,
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Surface(
                                modifier = Modifier.elovairePressScale(
                                    interactionSource = interactionSource,
                                    pressedScale = 0.9f,
                                    animationSpec = ElovaireMotion.chromeReleaseSpec(),
                                    label = "settings_equalizer_button_scale",
                                ),
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            onClick = onOpenEqualizer,
                                        )
                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_audio_waveform),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = copy.equalizer,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        SettingToggleRow(
                            title = copy.enableMono,
                            subtitle = copy.monoSubtitle,
                            enabled = eqSettings.monoEnabled,
                            onEnabledChanged = onMonoPlaybackChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = copy.otherSettings,
                    iconResId = R.drawable.ic_lucide_settings,
                )
            }

            item {
                ModuleCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        SettingNavigationRow(
                            title = foldersCopy.title,
                            subtitle = foldersCopy.subtitle,
                            onClick = onOpenLibraryFolders,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        SettingNavigationRow(
                            title = privacyCopy.title,
                            subtitle = privacyCopy.subtitle,
                            onClick = onOpenPrivacySafety,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        SettingToggleRow(
                            title = privacyCopy.onlineLyricsTitle,
                            subtitle = privacyCopy.onlineLyricsSubtitle,
                            enabled = onlineLyricsLookupEnabled,
                            onEnabledChanged = onOnlineLyricsLookupChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        SettingActionRow(
                            title = copy.scanLibrary,
                            subtitle = copy.scanLibrarySubtitle,
                            actionLabel = copy.scan,
                            onAction = onScanLibrary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                        )
                        if (showUpdateChecks) {
                            SettingActionRow(
                                title = copy.checkUpdates,
                                subtitle = copy.checkUpdatesSubtitle,
                                actionLabel = copy.check,
                                onAction = onCheckForUpdates,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                    )
                    Column(
                        modifier = Modifier.padding(top = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("Elovaire", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                                    Color.White.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                },
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = onOpenChangelog,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = copy.changelog,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(180f),
                                    )
                                }
                            }
                        }
                        Text(
                            text = commonUiCopy(appLanguage).refinedFooter,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = copy.settings,
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
private fun LanguagePickerRow(
    selectedLanguage: AppLanguage,
    copy: SettingsLanguageCopy,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = copy.language,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = copy.currentlyUsed,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Box {
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier.elovairePressScale(
                    interactionSource = interactionSource,
                    pressedScale = 0.9f,
                    animationSpec = ElovaireMotion.chromeReleaseSpec(),
                    label = "settings_language_button_scale",
                ),
                shape = RoundedCornerShape(ElovaireRadii.pill),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { expanded = true },
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_languages),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = selectedLanguage.nativeName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
    if (expanded) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            title = copy.language,
            onDismiss = { expanded = false },
            onConfirm = { language ->
                expanded = false
                onLanguageSelected(language)
            },
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedLanguage: AppLanguage,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (AppLanguage) -> Unit,
) {
    val revealRegistry = rememberMotionRevealRegistry()
    val listState = rememberElovaireLazyListState("language_picker")
    val copy = remember(selectedLanguage) { rootUiCopy(selectedLanguage) }
    val languages = remember {
        AppLanguage.entries.sortedBy { it.englishName }
    }
    var pendingLanguage by rememberSaveable(selectedLanguage) { mutableStateOf(selectedLanguage) }
    val visibleRows = 5
    val rowHeight = 56.dp
    val rowSpacing = 2.dp
    val listHeight = (rowHeight * visibleRows) + (rowSpacing * (visibleRows - 1))

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(ElovaireRadii.card),
                overlayAlpha = 0.6f,
                borderColor = blurSurfaceBorderColor(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .animateContentSize(animationSpec = ElovaireMotion.sizeSoft()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_languages),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(listHeight),
                    ) {
                        LazyColumn(
                            state = listState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .ensureSingleItemRubberBand(listState),
                            verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        ) {
                            items(languages, key = { it.name }) { language ->
                                LanguagePickerOptionRow(
                                    language = language,
                                    selected = language == pendingLanguage,
                                    modifier = Modifier
                                        .animateItem(
                                            placementSpec = ElovaireMotion.listPlacementSpec(),
                                        )
                                        .elovaireListReveal(
                                            itemKey = language.name,
                                            index = languages.indexOf(language),
                                            registry = revealRegistry,
                                        ),
                                    onClick = { pendingLanguage = language },
                                )
                            }
                        }
                        FastScrollbar(
                            state = listState,
                            topInset = 0.dp,
                            bottomInset = 0.dp,
                            modifier = Modifier.padding(end = 2.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = uiPhrase(selectedLanguage, UiPhrase.Cancel),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = { onConfirm(pendingLanguage) },
                            shape = RoundedCornerShape(ElovaireRadii.pill),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text(
                                text = copy.ok,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePickerOptionRow(
    language: AppLanguage,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val highlightColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = ElovaireMotion.colorFadeSpec(),
        label = "language_picker_row_highlight",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(highlightColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_circle),
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
                    },
                    modifier = Modifier.size(20.dp),
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(animationSpec = motionSpecs.tween(40)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = ElovaireMotion.releaseSpringSpec(),
                    ),
                    exit = fadeOut(animationSpec = motionSpecs.tween(20)) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = motionSpecs.tween(20),
                    ),
                    label = "language_picker_check",
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    iconResId: Int,
) {
    Row(
        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun TextSizeStepper(
    selectedPreset: TextSizePreset,
    onPresetSelected: (TextSizePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val presets = TextSizePreset.entries
    val currentSelectedPreset by rememberUpdatedState(selectedPreset)
    val currentOnPresetSelected by rememberUpdatedState(onPresetSelected)
    val selectedIndex = presets.indexOf(selectedPreset).coerceAtLeast(0)
    val maxIndex = (presets.size - 1).coerceAtLeast(1)
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .horizontalGestureSafe(),
        ) {
            val density = LocalDensity.current
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val stepFraction = selectedIndex.toFloat() / maxIndex.toFloat()
            val knobSizePx = with(density) { knobSize.toPx() }
            val selectedCenterPx = maxWidthPx * stepFraction
            val stepCenters = remember(maxWidthPx, maxIndex) {
                presets.indices.map { index ->
                    if (maxIndex == 0) {
                        maxWidthPx / 2f
                    } else {
                        maxWidthPx * (index.toFloat() / maxIndex.toFloat())
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
                label = "text_size_knob_offset",
            )
            val updateFromPosition: (Float) -> Unit = { xPosition ->
                val clampedX = xPosition.coerceIn(0f, maxWidthPx)
                dragCenterPx = clampedX
                val targetIndex = stepCenters
                    .withIndex()
                    .minByOrNull { (_, center) -> kotlin.math.abs(center - clampedX) }
                    ?.index
                    ?: presets.indexOf(currentSelectedPreset).coerceAtLeast(0)
                val preset = presets[targetIndex]
                if (preset != currentSelectedPreset) {
                    currentOnPresetSelected(preset)
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
                    .pointerInput(maxWidthPx, presets.size) {
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
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(2.dp)
                        .clip(RoundedCornerShape(ElovaireRadii.pill))
                        .background(lineColor),
                )

                Canvas(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val selectedDotRadius = 3.5.dp.toPx()
                    val defaultDotRadius = 2.5.dp.toPx()
                    val centerY = size.height / 2f
                    presets.forEachIndexed { index, _ ->
                        val fraction = if (maxIndex == 0) 0f else index.toFloat() / maxIndex.toFloat()
                        drawCircle(
                            color = dotColor,
                            radius = if (index == selectedIndex) selectedDotRadius else defaultDotRadius,
                            center = Offset(size.width * fraction, centerY),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_case_sensitive),
                contentDescription = "Smaller text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = selectedPreset.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_a_large_small),
                contentDescription = "Larger text",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}


@Composable
internal fun SettingToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        MonoPlaybackToggle(
            checked = enabled,
            onCheckedChange = onEnabledChanged,
        )
    }
}

@Composable
internal fun SettingActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Surface(
            modifier = Modifier.elovairePressScale(
                interactionSource = interactionSource,
                pressedScale = 0.9f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                label = "${actionLabel}_setting_action_scale",
            ),
            shape = RoundedCornerShape(ElovaireRadii.pill),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(ElovaireRadii.pill))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onAction,
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun MonoPlaybackToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val knobColor = if (checked) {
        Color.White
    } else if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            ToggleEnabledGreen
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.16f else 0.2f)
        },
        animationSpec = motionSpecs.tween(60),
        label = "mono_toggle_track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        animationSpec = motionSpecs.spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "mono_toggle_thumb_offset",
    )
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(percent = 50),
        color = trackColor,
        contentColor = knobColor,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset, y = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(knobColor),
            )
        }
    }
}

@Composable
internal fun ThemeModeSegmentedPicker(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionSpecs = rememberMotionSpecs()
    val language = LocalAppLanguage.current
    val common = remember(language) { commonUiCopy(language) }
    val options = listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System)
    BoxWithConstraints(
        modifier = modifier
            .height(46.dp)
            .horizontalGestureSafe()
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                },
            )
            .padding(5.dp),
    ) {
        val selectedIndex = options.indexOf(selectedMode).coerceAtLeast(0)
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = motionSpecs.spring(
                dampingRatio = 0.82f,
                stiffness = 420f,
            ),
            label = "theme_picker_offset",
        )
        val indicatorColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(indicatorColor),
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { option ->
                val selected = option == selectedMode
                val iconResId = when (option) {
                    ThemeMode.Light -> R.drawable.ic_lucide_sun
                    ThemeMode.Dark -> R.drawable.ic_lucide_moon
                    ThemeMode.System -> R.drawable.ic_lucide_settings_2
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onModeSelected(option) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = when (option) {
                                ThemeMode.Light -> common.light
                                ThemeMode.Dark -> common.dark
                                ThemeMode.System -> common.system
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            },
                        )
                    }
                }
            }
        }
    }
}
