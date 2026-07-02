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
    const val Medium = 260
    const val Spacious = 340
    const val Screen = 360
    const val ScreenFade = 240
    const val ScreenSlide = 420
    const val ScreenExpand = 440
    const val Player = 540
    const val PlayerFade = 260
    const val Component = 240
    const val ChromeResize = 220
    const val Emphasized = 460
    const val TopLevelEnter = 320
    const val TopLevelExit = 240
    const val DetailEnter = 480
    const val DetailExit = 340
    const val AlbumDetail = 540
    const val FullScreenEnter = 440
    const val FullScreenExit = 320
    const val QueueMenuEnter = 480
    const val QueueMenuExit = 340
    const val ListReveal = 320
    const val ListPlacement = 320
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
