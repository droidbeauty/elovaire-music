package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

object MotionDuration {
    const val Micro = 80
    const val Quick = 90
    const val Fast = 120
    const val Standard = 180
    const val Medium = 210
    const val Spacious = 260
    const val Screen = 220
    const val ScreenFade = 160
    const val ScreenSlide = 260
    const val ScreenExpand = 300
    const val Player = 360
    const val PlayerFade = 180
    const val Component = 190
    const val ChromeResize = 180
    const val Emphasized = 320
    const val TopLevelEnter = 190
    const val TopLevelExit = 150
    const val DetailEnter = 310
    const val DetailExit = 240
    const val AlbumDetail = 480
    const val FullScreenEnter = 280
    const val FullScreenExit = 220
    const val QueueMenuEnter = 320
    const val QueueMenuExit = 240
    const val ListReveal = 260
    const val ListPlacement = 260
}

object MotionEasing {
    val SoftOut = FastOutSlowInEasing
    val FadeIn = LinearOutSlowInEasing
    val FadeOut = FastOutLinearInEasing
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val RefinedDecelerate = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val RefinedAccelerate = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

object MotionScale {
    const val ChromePressed = 0.965f
    const val MediaPressed = 0.94f
    const val PlayerOverlayEnter = 0.995f
    const val PlayerOverlayExit = 0.992f
}
