package elovaire.music.droidbeauty.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.rootUiCopy
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedContent
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.InkText
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

internal val LocalSharedTopBarController = compositionLocalOf<SharedTopBarController?> { null }
internal val LocalRenderSharedTopBarContent = compositionLocalOf { false }
internal val LocalSharedBackIconPainter = compositionLocalOf<Painter?> { null }
internal val LocalSharedTopMenuIconPainter = compositionLocalOf<Painter?> { null }

internal data class TopBarActionSpec(
    @DrawableRes val iconResId: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
)

internal enum class TopBarMotionDirection {
    Forward,
    Back,
    Lateral,
}

internal sealed interface SharedTopBarSpec {
    data class Unified(
        val title: String,
        val showSettings: Boolean,
        @DrawableRes val supplementalActionIconResId: Int? = null,
        val supplementalActionContentDescription: String? = null,
        val onSupplementalAction: (() -> Unit)? = null,
        val onOpenMenu: () -> Unit,
    ) : SharedTopBarSpec

    data class Back(
        val title: String,
        val onBack: () -> Unit,
        val centeredTitle: Boolean = false,
    ) : SharedTopBarSpec

    data class Detail(
        val title: String,
        val subtitle: String?,
        val onBack: () -> Unit,
        val actions: List<TopBarActionSpec> = emptyList(),
    ) : SharedTopBarSpec
}

internal fun SharedTopBarSpec.visualSignature(): String {
    return when (this) {
        is SharedTopBarSpec.Unified -> "unified"
        is SharedTopBarSpec.Back -> "back"
        is SharedTopBarSpec.Detail -> "detail"
    }
}

internal fun topBarMotionDirection(
    initial: SharedTopBarSpec,
    target: SharedTopBarSpec,
): TopBarMotionDirection {
    val initialDepth = initial.topBarDepth()
    val targetDepth = target.topBarDepth()
    return when {
        targetDepth > initialDepth -> TopBarMotionDirection.Forward
        targetDepth < initialDepth -> TopBarMotionDirection.Back
        else -> TopBarMotionDirection.Lateral
    }
}

private fun SharedTopBarSpec.topBarDepth(): Int = when (this) {
    is SharedTopBarSpec.Unified -> 0
    is SharedTopBarSpec.Back -> 1
    is SharedTopBarSpec.Detail -> 2
}

private fun ElovaireMotion.topBarTextTransform(direction: TopBarMotionDirection) = when (direction) {
    TopBarMotionDirection.Forward -> topBarTextForwardTransform()
    TopBarMotionDirection.Back -> topBarTextBackTransform()
    TopBarMotionDirection.Lateral -> titleSwapTransform()
}

private fun ElovaireMotion.topBarActionsTransform(direction: TopBarMotionDirection) = when (direction) {
    TopBarMotionDirection.Forward -> topBarActionsForwardTransform()
    TopBarMotionDirection.Back,
    TopBarMotionDirection.Lateral,
    -> topBarActionsBackTransform()
}

internal data class SharedTopBarRegistration(
    val id: Any,
    val spec: SharedTopBarSpec,
)

internal class SharedTopBarController {
    var registration by mutableStateOf<SharedTopBarRegistration?>(null)
}

@Composable
internal fun RegisterSharedTopBar(spec: SharedTopBarSpec) {
    val controller = LocalSharedTopBarController.current ?: return
    val registrationId = remember { Any() }
    SideEffect {
        controller.registration = SharedTopBarRegistration(
            id = registrationId,
            spec = spec,
        )
    }
    DisposableEffect(controller, registrationId) {
        onDispose {
            if (controller.registration?.id == registrationId) {
                controller.registration = null
            }
        }
    }
}

@Composable
internal fun UnifiedTopBar(
    title: String,
    showSettings: Boolean,
    @DrawableRes supplementalActionIconResId: Int? = null,
    supplementalActionContentDescription: String? = null,
    onSupplementalAction: (() -> Unit)? = null,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useSharedBackdrop = LocalUseSharedTopBarBackdrop.current
    if (useSharedBackdrop && !LocalRenderSharedTopBarContent.current) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Unified(
                title = title,
                showSettings = showSettings,
                supplementalActionIconResId = supplementalActionIconResId,
                supplementalActionContentDescription = supplementalActionContentDescription,
                onSupplementalAction = onSupplementalAction,
                onOpenMenu = onOpenMenu,
            ),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (useSharedBackdrop) 8f else 0f)
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .consumePointersWithoutSemantics(),
        )
        if (!useSharedBackdrop) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 16.dp, top = 3.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .weight(1f)
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            if (showSettings) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (supplementalActionIconResId != null && onSupplementalAction != null) {
                        HeaderIconButton(
                            iconResId = supplementalActionIconResId,
                            contentDescription = supplementalActionContentDescription ?: "Action",
                            showBackground = false,
                            onClick = onSupplementalAction,
                            modifier = Modifier.zIndex(1f),
                        )
                    }
                    HeaderIconButton(
                        iconResId = R.drawable.ic_lucide_menu,
                        contentDescription = "Menu",
                        showBackground = false,
                        onClick = onOpenMenu,
                        modifier = Modifier.zIndex(1f),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
internal fun PinnedBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    centeredTitle: Boolean = false,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useSharedBackdrop = LocalUseSharedTopBarBackdrop.current
    if (useSharedBackdrop && !LocalRenderSharedTopBarContent.current) {
        RegisterSharedTopBar(
            SharedTopBarSpec.Back(
                title = title,
                onBack = onBack,
                centeredTitle = centeredTitle,
            ),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (useSharedBackdrop) 8f else 0f)
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .consumePointersWithoutSemantics(),
        )
        if (!useSharedBackdrop) {
            FrostedTopBarBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )
        }
        if (centeredTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 10.dp, end = 14.dp, top = 3.dp, bottom = 13.dp)
                    .height(40.dp),
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(1f),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(1f)
                        .padding(horizontal = 64.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 10.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_chevron_left,
                    contentDescription = "Back",
                    showBackground = false,
                    onClick = onBack,
                    modifier = Modifier.zIndex(1f),
                )
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .weight(1f)
                        .height(40.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SharedTopBarOverlay(
    spec: SharedTopBarSpec,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { rootUiCopy(language) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .consumePointersWithoutSemantics(),
        )
        ElovaireAnimatedContent(
            targetState = spec,
            transitionSpec = {
                when (topBarMotionDirection(initialState, targetState)) {
                    TopBarMotionDirection.Forward -> ElovaireMotion.sharedTopBarForwardTransform()
                    TopBarMotionDirection.Back -> ElovaireMotion.sharedTopBarBackTransform()
                    TopBarMotionDirection.Lateral -> ElovaireMotion.sharedTopBarTransform()
                }
            },
            contentKey = { it.visualSignature() },
            label = "SharedTopBarOverlayContent",
        ) { currentSpec ->
            when (currentSpec) {
                is SharedTopBarSpec.Unified -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 16.dp, top = 3.dp, bottom = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            ElovaireAnimatedContent(
                                targetState = currentSpec.title,
                                transitionSpec = {
                                    ElovaireMotion.topBarTextTransform(TopBarMotionDirection.Lateral)
                                },
                                label = "SharedTopBarUnifiedTitle",
                            ) { currentTitle ->
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                )
                            }
                        }
                        if (currentSpec.showSettings) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (currentSpec.supplementalActionIconResId != null && currentSpec.onSupplementalAction != null) {
                                    ElovaireAnimatedContent(
                                        targetState = currentSpec.supplementalActionIconResId,
                                        transitionSpec = { ElovaireMotion.topBarActionsTransform(TopBarMotionDirection.Forward) },
                                        label = "SharedTopBarUnifiedSupplementalAction",
                                    ) { iconResId ->
                                        HeaderIconButton(
                                            iconResId = iconResId,
                                            contentDescription = currentSpec.supplementalActionContentDescription ?: "Action",
                                            showBackground = false,
                                            onClick = currentSpec.onSupplementalAction,
                                        )
                                    }
                                }
                                HeaderIconButton(
                                    iconResId = R.drawable.ic_lucide_menu,
                                    contentDescription = "Menu",
                                    showBackground = false,
                                    onClick = currentSpec.onOpenMenu,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }

                is SharedTopBarSpec.Back -> {
                    if (currentSpec.centeredTitle) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(start = 10.dp, end = 14.dp, top = 3.dp, bottom = 13.dp)
                                .height(40.dp),
                        ) {
                            HeaderIconButton(
                                iconResId = R.drawable.ic_lucide_chevron_left,
                                contentDescription = "Back",
                                showBackground = false,
                                onClick = currentSpec.onBack,
                                modifier = Modifier.align(Alignment.CenterStart),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 64.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
                                    transitionSpec = {
                                        ElovaireMotion.topBarTextTransform(TopBarMotionDirection.Back)
                                    },
                                    label = "SharedTopBarBackCenteredTitle",
                                ) { currentTitle ->
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(start = 10.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ElovaireAnimatedContent(
                                targetState = "back",
                                transitionSpec = { ElovaireMotion.topBarNavigationTransform() },
                                label = "SharedTopBarBackNavigation",
                            ) {
                                HeaderIconButton(
                                    iconResId = R.drawable.ic_lucide_chevron_left,
                                    contentDescription = "Back",
                                    showBackground = false,
                                    onClick = currentSpec.onBack,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
                                    transitionSpec = {
                                        ElovaireMotion.topBarTextTransform(TopBarMotionDirection.Back)
                                    },
                                    label = "SharedTopBarBackTitle",
                                ) { currentTitle ->
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = elovaireScaledSp(25f)),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                is SharedTopBarSpec.Detail -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 10.dp, end = 14.dp, top = 3.dp, bottom = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ElovaireAnimatedContent(
                            targetState = "detail",
                            transitionSpec = { ElovaireMotion.topBarNavigationTransform() },
                            label = "SharedTopBarDetailNavigation",
                        ) {
                            HeaderIconButton(
                                iconResId = R.drawable.ic_lucide_chevron_left,
                                contentDescription = "Back",
                                showBackground = false,
                                onClick = currentSpec.onBack,
                            )
                        }
                        if (currentSpec.subtitle.isNullOrBlank()) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
                                    transitionSpec = { ElovaireMotion.topBarTextTransform(TopBarMotionDirection.Forward) },
                                    label = "SharedTopBarDetailTitleOnly",
                                ) { currentTitle ->
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = elovaireScaledSp(19f),
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        } else {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                ElovaireAnimatedContent(
                                    targetState = currentSpec.title,
                                    transitionSpec = { ElovaireMotion.topBarTextTransform(TopBarMotionDirection.Forward) },
                                    label = "SharedTopBarDetailTitleWithSubtitle",
                                ) { currentTitle ->
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = elovaireScaledSp(19f),
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = currentSpec.subtitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (currentSpec.actions.isNotEmpty()) {
                            ElovaireAnimatedContent(
                                targetState = currentSpec.actions.map { it.iconResId to it.contentDescription },
                                transitionSpec = { ElovaireMotion.topBarActionsTransform(TopBarMotionDirection.Forward) },
                                label = "SharedTopBarDetailActions",
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    currentSpec.actions.forEach { action ->
                                        HeaderIconButton(
                                            iconResId = action.iconResId,
                                            contentDescription = action.contentDescription,
                                            showBackground = false,
                                            onClick = action.onClick,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HeaderIconButton(
    iconResId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showBackground: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    val sharedBackPainter = LocalSharedBackIconPainter.current
    val sharedTopMenuPainter = LocalSharedTopMenuIconPainter.current
    val iconPainter = when {
        iconResId == R.drawable.ic_lucide_chevron_left && sharedBackPainter != null -> sharedBackPainter
        iconResId == R.drawable.ic_lucide_menu && sharedTopMenuPainter != null -> sharedTopMenuPainter
        else -> painterResource(id = iconResId)
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .elovairePressScale(
                enabled = enabled,
                pressedScale = 0.88f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "${contentDescription}_header_scale",
            )
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (showBackground) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = if (enabled) 0.58f else 0.32f,
                        )
                    } else {
                        Color.Transparent
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = contentDescription,
                tint = tint.copy(alpha = if (enabled) 1f else 0.35f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
