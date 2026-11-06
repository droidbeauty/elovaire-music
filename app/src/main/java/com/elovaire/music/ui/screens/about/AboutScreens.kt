package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.ui.i18n.*
import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor
import elovaire.music.droidbeauty.app.ui.screens.common.secondaryBodyTextStyle
import elovaire.music.droidbeauty.app.ui.screens.common.readableMutedIconColor

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.loadArtworkBitmapAwaitingAdmission
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.update.UpdateController
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.localizedUpdateError
import elovaire.music.droidbeauty.app.ui.i18n.aboutUpdateCopy
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.settingsCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.LocalMotionRuntime
import elovaire.music.droidbeauty.app.ui.LocalBackendResourceTracker
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.AboutCardButtonAccent
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.RoseAccent
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ChangelogScreen(
    releases: List<ChangelogRelease>,
    onBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
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
                top = topBarOccupiedHeight() + 20.dp,
                bottom = navigationBarInsetDp() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = miscPhrase(LocalAppLanguage.current, MiscPhrase.WhatsNew),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(ElovaireRadii.pill),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                )
            }

            item {
                ChangelogReleaseContent(
                    release = release,
                    contentHorizontalPadding = 20.dp,
                    pointedEntries = true,
                )
            }
        }
        PinnedBackTopBar(
            title = settingsCopy(LocalAppLanguage.current).changelog,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
internal fun ChangelogBottomSheetOverlay(
    releases: List<ChangelogRelease>,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val listState = rememberLazyListState()
    val release = remember(releases) {
        releases.firstOrNull { it.version == BuildConfig.VERSION_NAME } ?: releases.firstOrNull()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.46f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        PopupCardMotionHost(
            surfaceId = "root.changelog_sheet",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            visible = visible,
        ) {
            DynamicBackdropSurface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                overlayAlpha = 0.6f,
                borderColor = null,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = miscPhrase(LocalAppLanguage.current, MiscPhrase.WhatsNew),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(ElovaireRadii.pill),
                                color = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ) {
                                Text(
                                    text = BuildConfig.VERSION_NAME,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .elovaireClickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_x),
                                contentDescription = aboutUpdateCopy(LocalAppLanguage.current).closeChangelog,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 20.dp, bottom = navigationBarInsetDp() + 18.dp),
                    ) {
                        LazyColumn(
                            state = listState,
                            overscrollEffect = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .ensureSingleItemRubberBand(listState),
                            contentPadding = PaddingValues(bottom = 18.dp),
                        ) {
                            item {
                                ChangelogReleaseContent(
                                    release = release,
                                    contentHorizontalPadding = 20.dp,
                                    pointedEntries = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChangelogReleaseContent(
    release: ChangelogRelease?,
    contentHorizontalPadding: Dp = 20.dp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryTextColor: Color = readableSecondaryTextColor(),
    dividerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    pointedEntries: Boolean = false,
) {
    val changes = release?.changes?.filter { it.isNotBlank() }.orEmpty()
    val language = LocalAppLanguage.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = contentHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (changes.isEmpty()) {
            Text(
                text = aboutUpdateCopy(LocalAppLanguage.current).noChangelog,
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor,
            )
        } else {
            changes.forEachIndexed { index, change ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pointedEntries) {
                        Box(
                            modifier = Modifier
                            .size(4.dp)
                                .clip(CircleShape)
                                .background(textColor),
                        )
                    }
                    Text(
                        text = remember(change, language) { elovaire.music.droidbeauty.app.ui.i18n.localizedChangelogItem(change, language) },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                        ),
                        color = textColor,
                    )
                }
                if (index != changes.lastIndex) {
                    if (pointedEntries) {
                        Spacer(modifier = Modifier.height(18.dp))
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(dividerColor),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun AboutScreen(
    onBack: () -> Unit,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val aboutModel = remember(context) { context.loadAboutScreenModel() }
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
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
                end = 18.dp,
                top = topBarOccupiedHeight() + 20.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            itemsIndexed(aboutModel.sections, key = { index, section -> "${section.title}_$index" }) { index, section ->
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    AboutSectionBlock(
                        section = section,
                        showEntryLogo = index == 0,
                        isResourceSection = section.title == "Resources",
                    )
                    if (index != aboutModel.sections.lastIndex) {
                        DividerLine()
                    }
                }
            }
        }
        PinnedBackTopBar(
            title = uiPhrase(language, UiPhrase.About),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
@Suppress("LongMethod")
internal fun UpdateAvailableDialog(
    controller: UpdateController,
    state: elovaire.music.droidbeauty.app.data.update.AppUpdateUiState,
    release: elovaire.music.droidbeauty.app.data.update.AppReleaseInfo,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val changes = remember(release.notes) { releaseNotesAsChanges(release.notes) }
    val language = LocalAppLanguage.current
    val copy = remember(language) { aboutUpdateCopy(language) }
    BackHandler(onBack = controller::dismissAvailableUpdate)
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.46f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = controller::dismissAvailableUpdate,
                    ),
            )
        }
        PopupCardMotionHost(
            surfaceId = "root.update_dialog.card",
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = ElovaireRadii.card,
                    topEnd = ElovaireRadii.card,
                ),
                overlayAlpha = 0.6f,
                borderColor = null,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp,
                            top = 18.dp,
                            end = 20.dp,
                            bottom = navigationBarInsetDp() + 20.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_download),
                            contentDescription = null,
                            tint = readableMutedIconColor(),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap)) {
                            Text(
                                text = copy.updateAvailable,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${copy.version} ${release.versionName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = readableSecondaryTextColor(),
                            )
                        }
                    }
                    val visibleChangeCount = minOf(changes.size, 6).coerceAtLeast(1)
                    val changelogListState = rememberLazyListState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((visibleChangeCount * 24 + (visibleChangeCount - 1) * 18).dp),
                    ) {
                        LazyColumn(
                            state = changelogListState,
                            overscrollEffect = null,
                            modifier = Modifier.fillMaxSize().ensureSingleItemRubberBand(changelogListState),
                            contentPadding = PaddingValues.Zero,
                        ) {
                            item {
                                ChangelogReleaseContent(
                                    release = ChangelogRelease(
                                        version = release.versionName,
                                        changes = changes,
                                    ),
                                    contentHorizontalPadding = 0.dp,
                                    pointedEntries = true,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (state.isDownloading || state.isInstalling) {
                        LinearProgressIndicator(
                            progress = { state.downloadProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth(),
                            drawStopIndicator = {},
                        )
                    }
                    state.errorMessage?.let { errorMessage ->
                        Text(
                            text = localizedUpdateError(errorMessage, language),
                            style = secondaryBodyTextStyle(),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        UpdateDialogButton(
                            text = copy.later,
                            modifier = Modifier.weight(1f),
                            onClick = controller::dismissAvailableUpdate,
                        )
                        UpdateDialogButton(
                            text = copy.download,
                            modifier = Modifier.weight(1f),
                            emphasized = true,
                            loading = state.isDownloading || state.isInstalling,
                            enabled = !state.isDownloading && !state.isInstalling,
                            onClick = controller::startUpdate,
                        )
                    }
                }
            }
        }
    }
}

private fun releaseNotesAsChanges(notes: String): List<String> {
    val lines = notes.lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .toList()
    val bulletChanges = lines
        .filter { it.startsWith("-") || it.startsWith("*") || it.startsWith("•") }
        .map { line -> line.drop(1).trim() }
        .filter(String::isNotBlank)
    return bulletChanges.ifEmpty { lines.ifEmpty { notes.trim().takeIf(String::isNotBlank)?.let(::listOf).orEmpty() } }
}

@Composable
private fun UpdateDialogButton(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    loading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Surface(
        modifier = Modifier
            .then(modifier)
            .elovaireActionBump(
                enabled = enabled,
                interactionSource = interactionSource,
                label = "update_dialog_button_bump",
            )
            .height(46.dp),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.65f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.08f else 0.05f)
        },
        contentColor = if (emphasized) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = if (enabled) 1f else 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.55f)
        },
        enabled = enabled,
        interactionSource = interactionSource,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                UpdateDownloadSpinner()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun UpdateDownloadSpinner() {
    val motionRuntime = LocalMotionRuntime.current
    val motionSpecs = rememberMotionSpecs()
    val rotationDegrees = if (motionRuntime.reduceMotion) {
        0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "update_download_spinner")
        val animatedRotationDegrees by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = motionSpecs.tween(
                    durationMillis = 1_100,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "update_download_spinner_rotation",
        )
        animatedRotationDegrees
    }
    val spinnerColor = LocalContentColor.current
    Canvas(
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer { rotationZ = rotationDegrees },
    ) {
        val stroke = 1.75.dp.toPx()
        val inset = stroke / 2f + 1.dp.toPx()
        val arcSize = size.minDimension - inset * 2f
        drawArc(
            color = spinnerColor.copy(alpha = 0.24f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(arcSize, arcSize),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = spinnerColor,
            startAngle = -80f,
            sweepAngle = 88f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(arcSize, arcSize),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun AboutSectionBlock(
    section: AboutSection,
    showEntryLogo: Boolean = false,
    isResourceSection: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (isResourceSection) {
            AboutEntryTextStack(
                entry = AboutEntry(
                    title = section.title,
                    description = section.description,
                    logoUri = null,
                    links = emptyList(),
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            section.entries.forEachIndexed { index, entry ->
                AboutResourceEntry(entry = entry)
                if (index != section.entries.lastIndex) DividerLine()
            }
        } else {
            section.entries.forEachIndexed { index, entry ->
                AboutEntryBlock(
                    entry = entry,
                    horizontalScrollableLinks = true,
                    useRoseAccentButtons = index == 0 && showEntryLogo,
                    showLogo = showEntryLogo && index == 0,
                    showPlayStoreCard = showEntryLogo && index == 0,
                )
                if (index != section.entries.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
}

@Composable
private fun AboutEntryBlock(
    entry: AboutEntry,
    horizontalScrollableLinks: Boolean = false,
    useCardAccentButtons: Boolean = false,
    useRoseAccentButtons: Boolean = false,
    showLogo: Boolean = false,
    showPlayStoreCard: Boolean = false,
) {
    val context = LocalContext.current
    val playStoreLink = entry.links.firstOrNull { it.label.equals("Play Store", ignoreCase = true) }
    val visibleLinks = entry.links.filterNot { link ->
        showPlayStoreCard && link.label.equals("Play Store", ignoreCase = true)
    }
    val linksScrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (showLogo) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AboutEntryLogo(
                    logoUri = entry.logoUri,
                    title = entry.title,
                )
                AboutEntryTextStack(
                    entry = entry,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            AboutEntryTextStack(entry = entry)
        }
        if (visibleLinks.isNotEmpty()) {
            if (horizontalScrollableLinks) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .ensureHorizontalRubberBand(linksScrollState)
                        .horizontalScroll(linksScrollState, overscrollEffect = null),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    visibleLinks.forEach { link ->
                        AboutLinkPill(
                            link = link,
                            useCardAccent = useCardAccentButtons,
                            useRoseAccent = useRoseAccentButtons,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    visibleLinks.forEach { link ->
                        Box(modifier = Modifier.weight(1f)) {
                            AboutLinkPill(
                                link = link,
                                useCardAccent = useCardAccentButtons,
                                useRoseAccent = useRoseAccentButtons,
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
        if (showPlayStoreCard) {
            playStoreLink?.let { link ->
                AboutPlayStoreCard(
                    link = link,
                    onClick = { context.openAboutLink(link.url) },
                )
            }
        }
    }
}

@Composable
private fun AboutEntryTextStack(
    entry: AboutEntry,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = localizedAboutTitle(entry.title, language),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(22f)),
            color = MaterialTheme.colorScheme.onSurface,
        )
        entry.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = localizedAboutDescription(entry.title, description, language),
                style = secondaryBodyTextStyle(),
                color = readableSecondaryTextColor(),
            )
        }
    }
}

@Composable
private fun AboutEntryLogo(
    logoUri: String?,
    title: String,
    size: Dp = 60.dp,
) {
    val context = LocalContext.current
    val logoDescription = localizedAboutTitle(title, LocalAppLanguage.current)
    val resourceTracker = LocalBackendResourceTracker.current
    val drawableRes = remember(context, logoUri, title) {
        if (title == "Droid Beauty") {
            R.drawable.droid_beauty_logo
        } else {
            resolveAboutLogoDrawableRes(logoUri)
        }
    }
    val logoBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = logoUri?.trim()?.let(aboutLogoImageCache::get),
        key1 = logoUri,
        key2 = drawableRes,
        key3 = resourceTracker,
    ) {
        val source = logoUri?.trim()?.takeIf { it.isNotBlank() } ?: return@produceState
        if (drawableRes != null) return@produceState
        aboutLogoImageCache[source]?.let {
            value = it
            return@produceState
        }
        value = null
        value = withContext(Dispatchers.IO) {
            try {
                loadArtworkBitmapAwaitingAdmission(
                    context = context,
                    uri = Uri.parse(source),
                    targetPx = ABOUT_LOGO_TARGET_PX,
                    purpose = ArtworkPurpose.AboutLogo,
                    resourceTracker = resourceTracker,
                )?.asImageBitmap()?.also { bitmap ->
                    aboutLogoImageCache.put(source, bitmap)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        }
    }
    val uri = remember(logoUri, drawableRes, logoBitmap) {
        logoUri
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { drawableRes == null && logoBitmap == null }
            ?.let(Uri::parse)
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            drawableRes != null -> {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = logoDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            logoBitmap != null -> {
                Image(
                    bitmap = logoBitmap!!,
                    contentDescription = logoDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            else -> if (uri?.scheme == "http" || uri?.scheme == "https") {
                Icon(
                    painter = painterResource(id = aboutIconForUrl(uri.toString())),
                    contentDescription = logoDescription,
                    tint = readableMutedIconColor(),
                    modifier = Modifier.size(size * 0.38f),
                )
            } else {
                ArtworkImage(
                    uri = uri,
                    title = logoDescription,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = size / 2,
                    requestedSizePx = 160,
                )
            }
        }
    }
}

@Composable
private fun AboutResourceEntry(
    entry: AboutEntry,
) {
    val context = LocalContext.current
    val link = entry.links.firstOrNull() ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AboutEntryLogo(
            logoUri = entry.logoUri,
            title = entry.title,
            size = 48.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = localizedAboutTitle(entry.title, LocalAppLanguage.current),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.links.first().url.removePrefix("https://").removeSuffix("/"),
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AboutResourceLinkButton(
            link = link,
            onClick = { context.openAboutLink(link.url) },
        )
    }
}

@Composable
private fun AboutResourceLinkButton(
    link: AboutLink,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Surface(
        modifier = Modifier.elovaireActionBump(
            interactionSource = interactionSource,
            label = "about_resource_link_bump",
        ),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = aboutIconForUrl(link.url)),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = localizedAboutLinkLabel(link.label, LocalAppLanguage.current),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun AboutPlayStoreCard(
    link: AboutLink,
    onClick: () -> Unit,
) {
    val cardColor = AboutCardButtonAccent.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.28f else 0.18f)
    val contentColor = if (cardColor.luminance() > 0.42f) InkText else Color.White
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = cardColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_store),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ElovaireSpacing.mediaTextStackGap),
            ) {
                Text(
                    text = localizedAboutLinkLabel(link.label, LocalAppLanguage.current),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "Droid Beauty",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = contentColor.copy(alpha = 0.76f),
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = 180f },
            )
        }
    }
}

private fun Context.openAboutLink(url: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

private const val ABOUT_LOGO_TARGET_PX = 320
private val aboutLogoImageCache = android.util.LruCache<String, androidx.compose.ui.graphics.ImageBitmap>(8)

private fun resolveAboutLogoDrawableRes(logoUri: String?): Int? {
    val source = logoUri?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val drawableName = when {
        source.startsWith("@drawable/") -> source.substringAfter("@drawable/")
        source.startsWith("drawable/") -> source.substringAfter("drawable/")
        source.startsWith("android.resource://") && "/drawable/" in source -> source.substringAfterLast("/drawable/")
        else -> null
    }
        ?.substringBefore('?')
        ?.substringBefore('#')
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return null
    return if (drawableName == "droid_beauty_logo") R.drawable.droid_beauty_logo else null
}

@Composable
private fun AboutLinkPill(
    link: AboutLink,
    useCardAccent: Boolean = false,
    useRoseAccent: Boolean = false,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val containerColor = when {
        useRoseAccent -> RoseAccent.copy(alpha = 0.72f)
        useCardAccent -> AboutCardButtonAccent
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
    }
    val contentColor = if (containerColor.luminance() > 0.42f) InkText else Color.White
    val interactionSource = rememberElovaireInteractionSource()
    Surface(
        modifier = Modifier.elovaireActionBump(
            interactionSource = interactionSource,
            label = "about_link_pill_bump",
        ),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = aboutIconForUrl(link.url)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizedAboutLinkLabel(link.label, language),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@DrawableRes
private fun aboutIconForUrl(url: String): Int {
    val normalizedUrl = url.lowercase()
    return when {
        "instagram.com" in normalizedUrl -> R.drawable.ic_about_instagram
        "twitter.com" in normalizedUrl || "x.com" in normalizedUrl -> R.drawable.ic_about_twitter
        "gumroad.com" in normalizedUrl -> R.drawable.ic_lucide_candy
        "ko-fi.com" in normalizedUrl || "kofi.com" in normalizedUrl -> R.drawable.ic_about_coffee
        "play.google.com" in normalizedUrl -> R.drawable.ic_lucide_store
        else -> R.drawable.ic_about_globe
    }
}

internal fun localizedAboutTitle(
    title: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> title
    "Elovaire" -> title
    "Lucide icons", "Geist font" -> elovaire.music.droidbeauty.app.ui.i18n.localizedResourceTitle(title, language)
    "Resources" -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Albanian -> AlbanianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Danish -> DanishTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Dutch -> DutchTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Estonian -> EstonianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Greek -> GreekTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Hindi -> HindiTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Hungarian -> HungarianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Latvian -> LatvianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Lithuanian -> LithuanianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Macedonian -> MacedonianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Norwegian -> NorwegianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Serbian -> SerbianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Swedish -> SwedishTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Thai -> ThaiTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Catalan -> CatalanTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Finnish -> FinnishTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Romanian -> RomanianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Slovenian -> SlovenianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Bosnian -> BosnianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Indonesian -> IndonesianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Punjabi -> PunjabiTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Polish -> PolishTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Croatian -> CroatianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Czech -> CzechTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.French -> FrenchTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.German -> GermanTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Italian -> ItalianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Japanese -> JapaneseTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Korean -> KoreanTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Malay -> MalayTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Portuguese -> PortugueseTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Russian -> RussianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Slovak -> SlovakTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Spanish -> SpanishTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Ukrainian -> UkrainianTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Bengali -> BengaliTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.Urdu -> UrduTranslations.aboutScreens_localizedAboutTitle_1
        AppLanguage.English -> title
    }
    else -> title
}

internal fun localizedAboutDescription(
    title: String,
    description: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> if (language == AppLanguage.English) description else elovaire.music.droidbeauty.app.ui.i18n.droidBeautyDescription(language)
    "Elovaire" -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Albanian -> AlbanianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Danish -> DanishTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Dutch -> DutchTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Estonian -> EstonianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Greek -> GreekTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Hindi -> HindiTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Hungarian -> HungarianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Latvian -> LatvianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Lithuanian -> LithuanianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Macedonian -> MacedonianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Norwegian -> NorwegianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Serbian -> SerbianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Swedish -> SwedishTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Thai -> ThaiTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Catalan -> CatalanTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Finnish -> FinnishTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Romanian -> RomanianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Slovenian -> SlovenianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Bosnian -> BosnianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Indonesian -> IndonesianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Punjabi -> PunjabiTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Polish -> PolishTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Croatian -> CroatianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Czech -> CzechTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.French -> FrenchTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.German -> GermanTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Italian -> ItalianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Japanese -> JapaneseTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Korean -> KoreanTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Malay -> MalayTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Portuguese -> PortugueseTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Russian -> RussianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Slovak -> SlovakTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Spanish -> SpanishTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Ukrainian -> UkrainianTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Bengali -> BengaliTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.Urdu -> UrduTranslations.aboutScreens_localizedAboutDescription_1
        AppLanguage.English -> description
    }
    "Resources" -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Albanian -> AlbanianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Danish -> DanishTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Dutch -> DutchTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Estonian -> EstonianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Greek -> GreekTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Hindi -> HindiTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Hungarian -> HungarianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Latvian -> LatvianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Lithuanian -> LithuanianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Macedonian -> MacedonianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Norwegian -> NorwegianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Serbian -> SerbianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Swedish -> SwedishTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Thai -> ThaiTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Catalan -> CatalanTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Finnish -> FinnishTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Romanian -> RomanianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Slovenian -> SlovenianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Bosnian -> BosnianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Indonesian -> IndonesianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Punjabi -> PunjabiTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Polish -> PolishTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Croatian -> CroatianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Czech -> CzechTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.French -> FrenchTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.German -> GermanTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Italian -> ItalianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Japanese -> JapaneseTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Korean -> KoreanTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Malay -> MalayTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Portuguese -> PortugueseTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Russian -> RussianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Slovak -> SlovakTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Spanish -> SpanishTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Ukrainian -> UkrainianTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Bengali -> BengaliTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.Urdu -> UrduTranslations.aboutScreens_localizedAboutDescription_2
        AppLanguage.English -> description
    }.let { summary ->
        if (language == AppLanguage.English) summary else
            "$summary\n\nAndroid/Kotlin (Apache-2.0) · Lucide (ISC/MIT) · Geist (OFL-1.1) · jaudiotagger (LGPL)\n${elovaire.music.droidbeauty.app.ui.i18n.bundledLicensesLabel(language)}"
    }
    else -> description
}

internal fun localizedAboutLinkLabel(
    label: String,
    language: AppLanguage,
): String = when (label.lowercase()) {
    "website", "play store" -> when (language) {
        AppLanguage.Bulgarian -> if (label.equals("Play Store", true)) BulgarianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else BulgarianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Albanian -> if (label.equals("Play Store", true)) AlbanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else AlbanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Danish -> if (label.equals("Play Store", true)) DanishTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else DanishTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Dutch -> if (label.equals("Play Store", true)) DutchTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else DutchTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Estonian -> if (label.equals("Play Store", true)) EstonianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else EstonianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Greek -> if (label.equals("Play Store", true)) GreekTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else GreekTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Hindi -> if (label.equals("Play Store", true)) HindiTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else HindiTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Hungarian -> if (label.equals("Play Store", true)) HungarianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else HungarianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Latvian -> if (label.equals("Play Store", true)) LatvianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else LatvianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Lithuanian -> if (label.equals("Play Store", true)) LithuanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else LithuanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Macedonian -> if (label.equals("Play Store", true)) MacedonianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else MacedonianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Norwegian -> if (label.equals("Play Store", true)) NorwegianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else NorwegianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Serbian -> if (label.equals("Play Store", true)) SerbianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else SerbianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Swedish -> if (label.equals("Play Store", true)) SwedishTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else SwedishTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Thai -> if (label.equals("Play Store", true)) ThaiTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else ThaiTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Catalan -> if (label.equals("Play Store", true)) CatalanTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else CatalanTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Finnish -> if (label.equals("Play Store", true)) FinnishTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else FinnishTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Romanian -> if (label.equals("Play Store", true)) RomanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else RomanianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Slovenian -> if (label.equals("Play Store", true)) SlovenianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else SlovenianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Bosnian -> if (label.equals("Play Store", true)) BosnianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else BosnianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Indonesian -> if (label.equals("Play Store", true)) IndonesianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else IndonesianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Punjabi -> if (label.equals("Play Store", true)) PunjabiTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else PunjabiTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Polish -> if (label.equals("Play Store", true)) PolishTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else PolishTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.ChineseSimplified -> if (label.equals("Play Store", true)) ChineseSimplifiedTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else ChineseSimplifiedTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Croatian -> if (label.equals("Play Store", true)) CroatianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else CroatianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Czech -> if (label.equals("Play Store", true)) CzechTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else CzechTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.French -> if (label.equals("Play Store", true)) FrenchTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else FrenchTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.German -> if (label.equals("Play Store", true)) GermanTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else GermanTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Italian -> if (label.equals("Play Store", true)) ItalianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else ItalianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Japanese -> if (label.equals("Play Store", true)) JapaneseTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else JapaneseTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Korean -> if (label.equals("Play Store", true)) KoreanTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else KoreanTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Malay -> if (label.equals("Play Store", true)) MalayTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else MalayTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Portuguese -> if (label.equals("Play Store", true)) PortugueseTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else PortugueseTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Russian -> if (label.equals("Play Store", true)) RussianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else RussianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Slovak -> if (label.equals("Play Store", true)) SlovakTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else SlovakTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Spanish -> if (label.equals("Play Store", true)) SpanishTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else SpanishTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Ukrainian -> if (label.equals("Play Store", true)) UkrainianTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else UkrainianTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Bengali -> if (label.equals("Play Store", true)) BengaliTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else BengaliTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.Urdu -> if (label.equals("Play Store", true)) UrduTranslations.aboutScreens_localizedAboutLinkLabel_variant_1 else UrduTranslations.aboutScreens_localizedAboutLinkLabel_variant_2
        AppLanguage.English -> label
    }
    "twitter" -> if (language == AppLanguage.Japanese) "X" else label
    "instagram", "ko-fi" -> label
    else -> label
}
