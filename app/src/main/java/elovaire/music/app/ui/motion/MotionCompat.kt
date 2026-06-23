package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.TransformOrigin

object ElovaireAlbumMotion {
    fun forwardEnter(transformOrigin: TransformOrigin): EnterTransition =
        ElovaireMotion.albumDetailForwardEnter(transformOrigin)

    fun forwardExit(): ExitTransition = ElovaireMotion.albumDetailForwardExit()

    fun backEnter(): EnterTransition = ElovaireMotion.albumDetailBackEnter()

    fun backExit(transformOrigin: TransformOrigin): ExitTransition =
        ElovaireMotion.albumDetailBackExit(transformOrigin)
}

@Composable
fun rememberSystemAnimationScale(): Float = LocalMotionRuntime.current.durationScale

@Composable
fun SyncElovaireMotionScale() {
    val scale = rememberSystemAnimationScale()
    SideEffect {
        ElovaireMotion.updateSystemDurationScale(scale)
    }
}
