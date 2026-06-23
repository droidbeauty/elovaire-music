package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.ElovaireAnimatedVisibility
import elovaire.music.droidbeauty.app.ui.motion.rememberSystemAnimationScale
import elovaire.music.droidbeauty.app.ui.theme.InkText

@Composable
internal fun FirstLaunchPermissionLoadingScreen(
    showLoading: Boolean,
    onRequestPermission: () -> Unit,
) {
    val spinnerColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
        InkText
    } else {
        Color.White
    }
    val motionDurationScale = rememberSystemAnimationScale()
    val infiniteTransition = rememberInfiniteTransition(label = "first_launch_permission_spinner")
    val animatedRotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ElovaireMotion.scaleDurationMillis(1100, motionDurationScale).toInt(),
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "first_launch_permission_spinner_rotation",
    )
    val rotationDegrees = if (motionDurationScale <= 0f) 0f else animatedRotationDegrees
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        UnifiedTopBar(
            title = "Elovaire",
            showSettings = false,
            onOpenMenu = onRequestPermission,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        )
        ElovaireAnimatedVisibility(
            visible = showLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = androidx.compose.animation.fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
            exit = androidx.compose.animation.fadeOut(animationSpec = ElovaireMotion.fadeSlow()),
            label = "FirstLaunchPermissionSpinnerVisibility",
        ) {
            Canvas(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer { rotationZ = rotationDegrees },
            ) {
                val stroke = 2.5.dp.toPx()
                val inset = stroke / 2f + 1.dp.toPx()
                val arcSize = size.minDimension - inset * 2f
                drawArc(
                    color = spinnerColor.copy(alpha = 0.2f),
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
    }
}
