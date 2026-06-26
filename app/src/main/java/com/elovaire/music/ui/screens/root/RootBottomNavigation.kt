package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import elovaire.music.droidbeauty.app.ui.interaction.consumePointersWithoutSemantics
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing
import elovaire.music.droidbeauty.app.ui.theme.InkText

@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun BottomNavigationBar(
    currentRoute: String,
    suppressEnterAnimation: Boolean,
    destinations: List<TopLevelDestination>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iconColor = if (darkTheme) Color.White else InkText
    val navigationInset = navigationBarInsetDp()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ElovaireSpacing.bottomNavigationBodyHeight + navigationInset),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .consumePointersWithoutSemantics(),
        ) {
            BottomNavigationHazeBackground(
                darkTheme = darkTheme,
                modifier = Modifier.matchParentSize(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ElovaireSpacing.bottomNavigationBodyHeight)
                    .padding(horizontal = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    BottomNavigationItemButton(
                        iconResId = destination.iconResId,
                        contentDescription = destination.contentDescription,
                        baseTint = iconColor,
                        suppressEnterAnimation = suppressEnterAnimation,
                        selected = currentRoute == destination.route,
                        onClick = { onNavigate(destination.route) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BottomNavigationHazeBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    FrostedTopBarBackground(
        darkTheme = darkTheme,
        edge = ProgressiveChromeEdge.Bottom,
        overlayAlpha = 0.7f,
        flatOverlay = true,
        showEdgeLine = true,
        modifier = modifier,
    )
}

@Composable
private fun BottomNavigationItemButton(
    iconResId: Int,
    contentDescription: String,
    baseTint: Color,
    suppressEnterAnimation: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val motionSpecs = rememberMotionSpecs()
    val interactionSource = rememberElovaireInteractionSource()
    val selectionTransition = updateTransition(
        targetState = selected,
        label = "BottomNavItemSelection",
    )
    val iconTint by selectionTransition.animateColor(
        transitionSpec = { ElovaireMotion.colorFadeSpec() },
        label = "BottomNavItemIconTint",
    ) { isSelected ->
        if (isSelected) {
            baseTint
        } else {
            baseTint.copy(alpha = 0.5f)
        }
    }
    val baseIconScale by selectionTransition.animateFloat(
        transitionSpec = {
            if (suppressEnterAnimation) {
                motionSpecs.tween(durationMillis = 0)
            } else {
                ElovaireMotion.releaseSpringSpec<Float>(
                    dampingRatio = 0.8f,
                    stiffness = 540f,
                )
            }
        },
        label = "BottomNavItemBaseIconScale",
    ) { isSelected -> if (isSelected) 1.14f else 1f }

    Box(
        modifier = Modifier
            .size(56.dp)
            .elovairePressScale(
                pressedScale = 0.88f,
                animationSpec = ElovaireMotion.chromeReleaseSpec(),
                interactionSource = interactionSource,
                label = "${contentDescription}_bottom_nav_scale",
            )
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .scale(baseIconScale)
                .alpha(if (selected) 1f else 0.95f),
        )
    }
}
