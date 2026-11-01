package elovaire.music.droidbeauty.app.ui.screens
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
                top = topBarOccupiedHeight() + 24.dp,
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
                        text = change,
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
                top = detailTopBarOccupiedHeight() + ElovaireSpacing.topBarToFirstContentGap,
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
                            text = errorMessage,
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
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            logoBitmap != null -> {
                Image(
                    bitmap = logoBitmap!!,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            else -> if (uri?.scheme == "http" || uri?.scheme == "https") {
                Icon(
                    painter = painterResource(id = aboutIconForUrl(uri.toString())),
                    contentDescription = title,
                    tint = readableMutedIconColor(),
                    modifier = Modifier.size(size * 0.38f),
                )
            } else {
                ArtworkImage(
                    uri = uri,
                    title = title,
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

private fun localizedAboutTitle(
    title: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> title
    "Elovaire" -> title
    "Resources" -> when (language) {
        AppLanguage.Bulgarian -> "Ресурси"
        AppLanguage.Albanian -> "Burimet"
        AppLanguage.Danish -> "Ressourcer"
        AppLanguage.Dutch -> "Bronnen"
        AppLanguage.Estonian -> "Ressursid"
        AppLanguage.Greek -> "Πόροι"
        AppLanguage.Hindi -> "संसाधन"
        AppLanguage.Hungarian -> "Erőforrások"
        AppLanguage.Latin -> "Facultates"
        AppLanguage.Latvian -> "Resursi"
        AppLanguage.Lithuanian -> "Ištekliai"
        AppLanguage.Macedonian -> "Ресурси"
        AppLanguage.Norwegian -> "Ressurser"
        AppLanguage.Serbian -> "Ресурси"
        AppLanguage.Swedish -> "Resurser"
        AppLanguage.Thai -> "แหล่งข้อมูล"
        AppLanguage.Catalan -> "Recursos"
        AppLanguage.Finnish -> "Resurssit"
        AppLanguage.Romanian -> "Resurse"
        AppLanguage.Slovenian -> "Viri"
        AppLanguage.Bosnian -> "Resursi"
        AppLanguage.Indonesian -> "Sumber daya"
        AppLanguage.Punjabi -> "ਸਰੋਤ"
        AppLanguage.Polish -> "Zasoby"
        AppLanguage.ChineseSimplified -> "资源"
        AppLanguage.Croatian -> "Resursi"
        AppLanguage.Czech -> "Zdroje"
        AppLanguage.French -> "Ressources"
        AppLanguage.German -> "Ressourcen"
        AppLanguage.Italian -> "Risorse"
        AppLanguage.Japanese -> "リソース"
        AppLanguage.Korean -> "리소스"
        AppLanguage.Malay -> "Sumber"
        AppLanguage.Portuguese -> "Recursos"
        AppLanguage.Russian -> "Ресурсы"
        AppLanguage.Slovak -> "Zdroje"
        AppLanguage.Spanish -> "Recursos"
        AppLanguage.Ukrainian -> "Ресурси"
        AppLanguage.Bengali -> "রিসোর্স"
        AppLanguage.Urdu -> "وسائل"
        AppLanguage.English -> title
    }
    else -> title
}

private fun localizedAboutDescription(
    title: String,
    description: String,
    language: AppLanguage,
): String = when (title) {
    "Droid Beauty" -> when (language) {
        AppLanguage.Bulgarian -> "Минималистични приложения и изживявания за по-красив Android"
        AppLanguage.Albanian -> "Aplikacione dhe përvoja minimaliste për një Android më të bukur"
        AppLanguage.Danish -> "Minimalistiske apps og oplevelser til et smukkere Android"
        AppLanguage.Dutch -> "Minimalistisch ontworpen apps en ervaringen voor een mooier Android"
        AppLanguage.Estonian -> "Minimalistlikud rakendused ja elamused kaunima Androidi jaoks"
        AppLanguage.Greek -> "Μινιμαλιστικές εφαρμογές και εμπειρίες για ένα ομορφότερο Android"
        AppLanguage.Hindi -> "अधिक सुंदर Android के लिए न्यूनतम डिज़ाइन वाले ऐप और अनुभव"
        AppLanguage.Hungarian -> "Letisztult alkalmazások és élmények egy szebb Androidért"
        AppLanguage.Latin -> "Applicationes simpliciter formatae ad Android pulchrius"
        AppLanguage.Latvian -> "Minimālistiskas lietotnes un pieredze skaistākam Android"
        AppLanguage.Lithuanian -> "Minimalistinės programėlės ir patirtys gražesniam Android"
        AppLanguage.Macedonian -> "Минималистички апликации и искуства за поубав Android"
        AppLanguage.Norwegian -> "Minimalistiske apper og opplevelser for et vakrere Android"
        AppLanguage.Serbian -> "Минималистичке апликације и искуства за лепши Android"
        AppLanguage.Swedish -> "Minimalistiska appar och upplevelser för ett vackrare Android"
        AppLanguage.Thai -> "แอปและประสบการณ์ดีไซน์เรียบง่ายเพื่อ Android ที่สวยงามยิ่งขึ้น"
        AppLanguage.Catalan -> "Aplicacions i experiències minimalistes per a un Android més bonic"
        AppLanguage.Finnish -> "Minimalistisia sovelluksia ja elämyksiä kauniimpaan Androidiin"
        AppLanguage.Romanian -> "Aplicații și experiențe minimaliste pentru un Android mai frumos"
        AppLanguage.Slovenian -> "Minimalistično oblikovane aplikacije in izkušnje za lepši Android"
        AppLanguage.Bosnian -> "Minimalistički dizajnirane aplikacije i iskustva za ljepši Android"
        AppLanguage.Indonesian -> "Aplikasi dan pengalaman berdesain minimalis untuk Android yang lebih indah"
        AppLanguage.Punjabi -> "ਹੋਰ ਸੁੰਦਰ Android ਲਈ ਸਾਦਗੀ ਨਾਲ ਬਣਾਈਆਂ ਐਪਾਂ ਅਤੇ ਤਜਰਬੇ"
        AppLanguage.Polish -> "Minimalnie zaprojektowane aplikacje i doświadczenia dla piękniejszego Androida"
        AppLanguage.ChineseSimplified -> "以极简设计打造更美好的 Android 应用与体验"
        AppLanguage.Croatian -> "Minimalno dizajnirane aplikacije i iskustva za ljepši Android"
        AppLanguage.Czech -> "Minimalisticky navržené aplikace a zážitky pro krásnější Android"
        AppLanguage.French -> "Des applications et expériences au design minimal pour embellir Android"
        AppLanguage.German -> "Minimal gestaltete Apps und Erlebnisse für ein schöneres Android"
        AppLanguage.Italian -> "App ed esperienze dal design minimale per rendere Android più bello"
        AppLanguage.Japanese -> "Android をより美しくする、ミニマルに設計されたアプリと体験"
        AppLanguage.Korean -> "더 아름다운 Android를 위한 미니멀한 앱과 경험"
        AppLanguage.Malay -> "Aplikasi dan pengalaman berreka bentuk minimal untuk Android yang lebih indah"
        AppLanguage.Portuguese -> "Apps e experiências de design minimal para tornar o Android mais bonito"
        AppLanguage.Russian -> "Минималистичные приложения и впечатления для более красивого Android"
        AppLanguage.Slovak -> "Minimalisticky navrhnuté aplikácie a zážitky pre krajší Android"
        AppLanguage.Spanish -> "Apps y experiencias de diseño minimalista para hacer Android más bello"
        AppLanguage.Ukrainian -> "Мінімалістично створені застосунки й враження для красивішого Android"
        AppLanguage.Bengali -> "আরও সুন্দর Android-এর জন্য ন্যূনতম নকশার অ্যাপ ও অভিজ্ঞতা"
        AppLanguage.Urdu -> "زیادہ خوبصورت Android کے لیے کم سے کم انداز میں ڈیزائن کردہ ایپس اور تجربات"
        AppLanguage.English -> description
    }
    "Elovaire" -> when (language) {
        AppLanguage.Bulgarian -> "Елегантен офлайн плейър за вашата локална музика"
        AppLanguage.Albanian -> "Një luajtës elegant jashtë linje për muzikën tënde lokale"
        AppLanguage.Danish -> "En elegant offlineafspiller til din lokale musik"
        AppLanguage.Dutch -> "Een elegante offlinespeler voor je lokale muziek"
        AppLanguage.Estonian -> "Elegantne võrguühenduseta pleier sinu kohalikule muusikale"
        AppLanguage.Greek -> "Ένα κομψό πρόγραμμα αναπαραγωγής εκτός σύνδεσης για την τοπική σας μουσική"
        AppLanguage.Hindi -> "आपके स्थानीय संगीत के लिए एक शानदार ऑफ़लाइन प्लेयर"
        AppLanguage.Hungarian -> "Elegáns offline lejátszó a helyi zenéidhez"
        AppLanguage.Latin -> "Lusor elegans sine rete musicae tuae locali destinatus"
        AppLanguage.Latvian -> "Elegants bezsaistes atskaņotājs tavai ierīcē esošajai mūzikai"
        AppLanguage.Lithuanian -> "Elegantiškas neprisijungęs grotuvas jūsų įrenginio muzikai"
        AppLanguage.Macedonian -> "Елегантен офлајн плеер за вашата локална музика"
        AppLanguage.Norwegian -> "En elegant frakoblet spiller for din lokale musikk"
        AppLanguage.Serbian -> "Елегантан офлајн плејер за вашу локалну музику"
        AppLanguage.Swedish -> "En elegant offlinespelare för din lokala musik"
        AppLanguage.Thai -> "เครื่องเล่นออฟไลน์ที่สวยงามสำหรับเพลงในเครื่องของคุณ"
        AppLanguage.Catalan -> "Un reproductor fora de línia elegant per a la teva música local"
        AppLanguage.Finnish -> "Tyylikäs offline-soitin paikalliselle musiikillesi"
        AppLanguage.Romanian -> "Un player offline elegant pentru muzica ta locală"
        AppLanguage.Slovenian -> "Eleganten predvajalnik brez povezave za vašo lokalno glasbo"
        AppLanguage.Bosnian -> "Elegantan offline player napravljen za vašu lokalnu muziku"
        AppLanguage.Indonesian -> "Pemutar offline elegan yang dibuat untuk musik lokal Anda"
        AppLanguage.Punjabi -> "ਤੁਹਾਡੇ ਸਥਾਨਕ ਸੰਗੀਤ ਲਈ ਬਣਾਇਆ ਸ਼ਾਨਦਾਰ ਆਫਲਾਈਨ ਪਲੇਅਰ"
        AppLanguage.Polish -> "Elegancki odtwarzacz offline stworzony z myślą o Twojej lokalnej muzyce"
        AppLanguage.ChineseSimplified -> "为你的本地音乐打造的优雅离线播放器"
        AppLanguage.Croatian -> "Elegantni offline reproduktor stvoren za vašu lokalnu glazbu"
        AppLanguage.Czech -> "Elegantní offline přehrávač vytvořený pro vaši místní hudbu"
        AppLanguage.French -> "Un lecteur hors ligne élégant conçu pour votre musique locale"
        AppLanguage.German -> "Ein eleganter Offline-Player für deine lokale Musik"
        AppLanguage.Italian -> "Un player offline elegante creato per la tua musica locale"
        AppLanguage.Japanese -> "ローカル音楽のために作られた、エレガントなオフラインプレーヤー"
        AppLanguage.Korean -> "로컬 음악을 위해 만들어진 우아한 오프라인 플레이어"
        AppLanguage.Malay -> "Pemain luar talian yang elegan untuk muzik tempatan anda"
        AppLanguage.Portuguese -> "Um reprodutor offline elegante feito para a sua música local"
        AppLanguage.Russian -> "Элегантный офлайн-плеер для вашей локальной музыки"
        AppLanguage.Slovak -> "Elegantný offline prehrávač vytvorený pre vašu lokálnu hudbu"
        AppLanguage.Spanish -> "Un reproductor sin conexión elegante hecho para tu música local"
        AppLanguage.Ukrainian -> "Елегантний офлайн-програвач для вашої локальної музики"
        AppLanguage.Bengali -> "আপনার লোকাল সঙ্গীতের জন্য তৈরি একটি মার্জিত অফলাইন প্লেয়ার"
        AppLanguage.Urdu -> "آپ کی مقامی موسیقی کے لیے بنایا گیا ایک نفیس آف لائن پلیئر"
        AppLanguage.English -> description
    }
    "Resources" -> when (language) {
        AppLanguage.Bulgarian -> "Проектите, инструментите и библиотеките, които помагат за създаването на Elovaire"
        AppLanguage.Albanian -> "Projektet, mjetet dhe bibliotekat që ndihmojnë në krijimin e Elovaire"
        AppLanguage.Danish -> "Projekter, værktøjer og biblioteker, der hjælper med at skabe Elovaire"
        AppLanguage.Dutch -> "Projecten, hulpmiddelen en bibliotheken die Elovaire mogelijk maken"
        AppLanguage.Estonian -> "Projektid, tööriistad ja teegid, mis aitavad Elovaire’i luua"
        AppLanguage.Greek -> "Έργα, εργαλεία και βιβλιοθήκες που συμβάλλουν στη δημιουργία του Elovaire"
        AppLanguage.Hindi -> "वे प्रोजेक्ट, टूल और लाइब्रेरी जो Elovaire बनाने में मदद करते हैं"
        AppLanguage.Hungarian -> "Az Elovaire létrehozását segítő projektek, eszközök és könyvtárak"
        AppLanguage.Latin -> "Opera, instrumenta et bibliothecae quae Elovaire creare adiuvant"
        AppLanguage.Latvian -> "Projekti, rīki un bibliotēkas, kas palīdz radīt Elovaire"
        AppLanguage.Lithuanian -> "Projektai, įrankiai ir bibliotekos, padedantys kurti Elovaire"
        AppLanguage.Macedonian -> "Проекти, алатки и библиотеки што помагаат во создавањето на Elovaire"
        AppLanguage.Norwegian -> "Prosjekter, verktøy og biblioteker som bidrar til å skape Elovaire"
        AppLanguage.Serbian -> "Пројекти, алати и библиотеке који помажу у стварању Elovaire-а"
        AppLanguage.Swedish -> "Projekt, verktyg och bibliotek som hjälper till att skapa Elovaire"
        AppLanguage.Thai -> "โครงการ เครื่องมือ และไลบรารีที่ช่วยสร้าง Elovaire"
        AppLanguage.Catalan -> "Projectes, eines i biblioteques que ajuden a crear Elovaire"
        AppLanguage.Finnish -> "Projektit, työkalut ja kirjastot, jotka auttavat Elovairen luomisessa"
        AppLanguage.Romanian -> "Proiectele, instrumentele și bibliotecile care ajută la crearea Elovaire"
        AppLanguage.Slovenian -> "Projekti, orodja in knjižnice, ki pomagajo ustvarjati Elovaire"
        AppLanguage.Bosnian -> "Projekti, alati i biblioteke koji pomažu u stvaranju Elovairea"
        AppLanguage.Indonesian -> "Proyek, alat, dan pustaka yang membantu membuat Elovaire"
        AppLanguage.Punjabi -> "Elovaire ਬਣਾਉਣ ਵਿੱਚ ਮਦਦ ਕਰਨ ਵਾਲੇ ਪ੍ਰੋਜੈਕਟ, ਔਜ਼ਾਰ ਅਤੇ ਲਾਇਬ੍ਰੇਰੀਆਂ"
        AppLanguage.Polish -> "Projekty, narzędzia i biblioteki, które pomagają tworzyć Elovaire"
        AppLanguage.ChineseSimplified -> "帮助打造 Elovaire 的项目、工具和库"
        AppLanguage.Croatian -> "Projekti, alati i biblioteke koji pomažu stvarati Elovaire"
        AppLanguage.Czech -> "Projekty, nástroje a knihovny, které pomáhají tvořit Elovaire"
        AppLanguage.French -> "Projets, outils et bibliothèques qui aident à créer Elovaire"
        AppLanguage.German -> "Projekte, Werkzeuge und Bibliotheken, die Elovaire ermöglichen"
        AppLanguage.Italian -> "Progetti, strumenti e librerie che aiutano a creare Elovaire"
        AppLanguage.Japanese -> "Elovaire の制作を支えるプロジェクト、ツール、ライブラリ"
        AppLanguage.Korean -> "Elovaire를 만드는 데 도움을 주는 프로젝트, 도구, 라이브러리"
        AppLanguage.Malay -> "Projek, alat dan pustaka yang membantu membina Elovaire"
        AppLanguage.Portuguese -> "Projetos, ferramentas e bibliotecas que ajudam a criar o Elovaire"
        AppLanguage.Russian -> "Проекты, инструменты и библиотеки, которые помогают создавать Elovaire"
        AppLanguage.Slovak -> "Projekty, nástroje a knižnice, ktoré pomáhajú vytvárať Elovaire"
        AppLanguage.Spanish -> "Proyectos, herramientas y bibliotecas que ayudan a crear Elovaire"
        AppLanguage.Ukrainian -> "Проєкти, інструменти та бібліотеки, що допомагають створювати Elovaire"
        AppLanguage.Bengali -> "Elovaire তৈরিতে সহায়ক প্রকল্প, টুল এবং লাইব্রেরি"
        AppLanguage.Urdu -> "وہ منصوبے، اوزار اور لائبریریاں جو Elovaire بنانے میں مدد کرتی ہیں"
        AppLanguage.English -> description
    }
    else -> description
}

private fun localizedAboutLinkLabel(
    label: String,
    language: AppLanguage,
): String = when (label.lowercase()) {
    "website", "play store" -> when (language) {
        AppLanguage.Bulgarian -> if (label.equals("Play Store", true)) "Play Store" else "Уебсайт"
        AppLanguage.Albanian -> if (label.equals("Play Store", true)) "Play Store" else "Faqja web"
        AppLanguage.Danish -> if (label.equals("Play Store", true)) "Play Butik" else "Websted"
        AppLanguage.Dutch -> if (label.equals("Play Store", true)) "Play Store" else "Website"
        AppLanguage.Estonian -> if (label.equals("Play Store", true)) "Play pood" else "Veebileht"
        AppLanguage.Greek -> if (label.equals("Play Store", true)) "Play Store" else "Ιστότοπος"
        AppLanguage.Hindi -> if (label.equals("Play Store", true)) "Play Store" else "वेबसाइट"
        AppLanguage.Hungarian -> if (label.equals("Play Store", true)) "Play Áruház" else "Weboldal"
        AppLanguage.Latin -> if (label.equals("Play Store", true)) "Play Store" else "Pagina interretialis"
        AppLanguage.Latvian -> if (label.equals("Play Store", true)) "Play veikals" else "Tīmekļvietne"
        AppLanguage.Lithuanian -> if (label.equals("Play Store", true)) "Play parduotuvė" else "Svetainė"
        AppLanguage.Macedonian -> if (label.equals("Play Store", true)) "Play Store" else "Веб-страница"
        AppLanguage.Norwegian -> if (label.equals("Play Store", true)) "Play Butikk" else "Nettside"
        AppLanguage.Serbian -> if (label.equals("Play Store", true)) "Play продавница" else "Веб-сајт"
        AppLanguage.Swedish -> if (label.equals("Play Store", true)) "Play Butik" else "Webbplats"
        AppLanguage.Thai -> if (label.equals("Play Store", true)) "Play Store" else "เว็บไซต์"
        AppLanguage.Catalan -> if (label.equals("Play Store", true)) "Play Store" else "Lloc web"
        AppLanguage.Finnish -> if (label.equals("Play Store", true)) "Play Kauppa" else "Verkkosivusto"
        AppLanguage.Romanian -> if (label.equals("Play Store", true)) "Play Store" else "Site web"
        AppLanguage.Slovenian -> if (label.equals("Play Store", true)) "Trgovina Play" else "Spletna stran"
        AppLanguage.Bosnian -> if (label.equals("Play Store", true)) "Play prodavnica" else "Web stranica"
        AppLanguage.Indonesian -> if (label.equals("Play Store", true)) "Play Store" else "Situs web"
        AppLanguage.Punjabi -> if (label.equals("Play Store", true)) "ਪਲੇ ਸਟੋਰ" else "ਵੈੱਬਸਾਈਟ"
        AppLanguage.Polish -> if (label.equals("Play Store", true)) "Sklep Play" else "Strona"
        AppLanguage.ChineseSimplified -> if (label.equals("Play Store", true)) "Play 商店" else "网站"
        AppLanguage.Croatian -> if (label.equals("Play Store", true)) "Play trgovina" else "Web"
        AppLanguage.Czech -> if (label.equals("Play Store", true)) "Obchod Play" else "Web"
        AppLanguage.French -> if (label.equals("Play Store", true)) "Play Store" else "Site web"
        AppLanguage.German -> if (label.equals("Play Store", true)) "Play Store" else "Website"
        AppLanguage.Italian -> if (label.equals("Play Store", true)) "Play Store" else "Sito web"
        AppLanguage.Japanese -> if (label.equals("Play Store", true)) "Play ストア" else "ウェブサイト"
        AppLanguage.Korean -> if (label.equals("Play Store", true)) "Play 스토어" else "웹사이트"
        AppLanguage.Malay -> if (label.equals("Play Store", true)) "Play Store" else "Laman web"
        AppLanguage.Portuguese -> if (label.equals("Play Store", true)) "Play Store" else "Site"
        AppLanguage.Russian -> if (label.equals("Play Store", true)) "Play Маркет" else "Сайт"
        AppLanguage.Slovak -> if (label.equals("Play Store", true)) "Obchod Play" else "Web"
        AppLanguage.Spanish -> if (label.equals("Play Store", true)) "Play Store" else "Sitio web"
        AppLanguage.Ukrainian -> if (label.equals("Play Store", true)) "Play Маркет" else "Сайт"
        AppLanguage.Bengali -> if (label.equals("Play Store", true)) "Play Store" else "ওয়েবসাইট"
        AppLanguage.Urdu -> if (label.equals("Play Store", true)) "پلے اسٹور" else "ویب سائٹ"
        AppLanguage.English -> label
    }
    "twitter" -> if (language == AppLanguage.Japanese) "X" else label
    "instagram", "ko-fi" -> label
    else -> label
}
