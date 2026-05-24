package elovaire.music.app.ui.motion

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.math.roundToLong

object ElovaireMotion {
    const val Quick = 120
    const val Fast = 160
    const val Standard = 200
    const val Medium = 240
    const val Spacious = 280
    const val Screen = 260
    const val ScreenFade = 180
    const val ScreenSlide = 240
    const val ScreenExpand = 320
    const val PlayerScreen = 360
    const val PlayerFade = 180
    const val Controls = 140
    const val ChromeResize = 180

    val SoftOut: Easing = FastOutSlowInEasing
    val FadeIn: Easing = LinearOutSlowInEasing
    val FadeOut: Easing = FastOutLinearInEasing
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> fadeFast(): FiniteAnimationSpec<T> = tween(
        durationMillis = Quick,
        easing = FadeOut,
    )

    fun <T> fadeMedium(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenFade,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> fadeSlow(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = Spacious,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> scaleSoft(): FiniteAnimationSpec<T> = tween(
        durationMillis = Standard,
        easing = SoftOut,
    )

    fun <T> offsetSoft(
        durationMillis: Int = ScreenSlide,
        delayMillis: Int = 0,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = SoftOut,
    )

    fun <T> sizeSoft(): FiniteAnimationSpec<T> = tween(
        durationMillis = ChromeResize,
        easing = SoftOut,
    )

    fun <T> standardTween(
        durationMillis: Int = Standard,
        delayMillis: Int = 0,
        easing: Easing = SoftOut,
    ): FiniteAnimationSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing,
    )

    fun <T> standardSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = 520f,
    ): FiniteAnimationSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> colorFadeSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = Controls,
        easing = SoftOut,
    )

    fun <T> contentFadeInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = Standard,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> contentFadeOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = Quick,
        easing = FadeOut,
    )

    fun <T> pressDownSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = 90,
        easing = SoftOut,
    )

    fun <T> releaseSpringSpec(
        dampingRatio: Float = 0.82f,
        stiffness: Float = 560f,
    ): FiniteAnimationSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
    )

    fun <T> bounceSpringSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.68f,
        stiffness = 420f,
    )

    fun <T> overscrollSpringSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 680f,
    )

    fun <T> iconSwapInSpec(delayMillis: Int = 0): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenFade,
        delayMillis = delayMillis,
        easing = FadeIn,
    )

    fun <T> iconSwapOutSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = 110,
        easing = FadeOut,
    )

    fun <T> emphasizedEnterSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenExpand,
        easing = EmphasizedDecelerate,
    )

    fun <T> emphasizedExitSpec(): FiniteAnimationSpec<T> = tween(
        durationMillis = ScreenFade,
        easing = EmphasizedAccelerate,
    )

    fun standardEnter(
        delayMillis: Int = 0,
        initialOffsetY: (fullHeight: Int) -> Int = { it / 8 },
    ): EnterTransition = fadeIn(animationSpec = fadeMedium(delayMillis)) +
        slideInVertically(
            animationSpec = offsetSoft(durationMillis = ScreenSlide, delayMillis = delayMillis),
            initialOffsetY = initialOffsetY,
        )

    fun standardExit(
        targetOffsetY: (fullHeight: Int) -> Int = { it / 10 },
    ): ExitTransition = fadeOut(animationSpec = fadeFast()) +
        slideOutVertically(
            animationSpec = offsetSoft(durationMillis = Fast),
            targetOffsetY = targetOffsetY,
        )

    fun emphasizedEnter(
        delayMillis: Int = 0,
    ): EnterTransition = fadeIn(animationSpec = contentFadeInSpec(delayMillis)) +
        scaleIn(
            animationSpec = emphasizedEnterSpec(),
            initialScale = 0.985f,
        )

    fun emphasizedExit(): ExitTransition = fadeOut(animationSpec = emphasizedExitSpec()) +
        scaleOut(
            animationSpec = emphasizedExitSpec(),
            targetScale = 0.99f,
        )

    fun softContentTransform(): ContentTransform =
        (fadeIn(animationSpec = contentFadeInSpec()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = Standard),
                initialOffsetY = { -it / 10 },
            )) togetherWith fadeOut(animationSpec = contentFadeOutSpec())

    fun sharedTopBarTransform(): ContentTransform =
        (fadeIn(animationSpec = fadeMedium()) +
            slideInVertically(
                animationSpec = offsetSoft(durationMillis = ScreenFade),
                initialOffsetY = { -it / 5 },
            )) togetherWith fadeOut(animationSpec = fadeFast())

    fun fullScreenForwardEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { it / 7 },
    ): EnterTransition = fadeIn(animationSpec = fadeMedium()) +
        slideInHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenSlide),
            initialOffsetX = initialOffsetX,
        ) +
        scaleIn(
            animationSpec = scaleSoft(),
            initialScale = 0.992f,
        )

    fun fullScreenForwardExit(
        targetOffsetX: (fullWidth: Int) -> Int = { -(it / 14) },
    ): ExitTransition = fadeOut(animationSpec = fadeFast()) +
        slideOutHorizontally(
            animationSpec = offsetSoft(durationMillis = Fast),
            targetOffsetX = targetOffsetX,
        ) +
        scaleOut(
            animationSpec = fadeFast(),
            targetScale = 0.996f,
        )

    fun fullScreenBackEnter(
        initialOffsetX: (fullWidth: Int) -> Int = { -(it / 14) },
    ): EnterTransition = fadeIn(animationSpec = fadeMedium()) +
        slideInHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenSlide),
            initialOffsetX = initialOffsetX,
        ) +
        scaleIn(
            animationSpec = scaleSoft(),
            initialScale = 0.996f,
        )

    fun fullScreenBackExit(
        targetOffsetX: (fullWidth: Int) -> Int = { it / 4 },
    ): ExitTransition = fadeOut(animationSpec = fadeMedium()) +
        slideOutHorizontally(
            animationSpec = offsetSoft(durationMillis = ScreenSlide),
            targetOffsetX = targetOffsetX,
        ) +
        scaleOut(
            animationSpec = fadeMedium(),
            targetScale = 0.992f,
        )

    fun scaleDurationMillis(
        durationMillis: Long,
        durationScale: Float,
    ): Long = when {
        durationMillis <= 0L -> 0L
        durationScale <= 0f -> 0L
        else -> (durationMillis * durationScale).roundToLong().coerceAtLeast(1L)
    }

    fun scaleDurationMillis(
        durationMillis: Int,
        durationScale: Float,
    ): Long = scaleDurationMillis(durationMillis.toLong(), durationScale)
}

@Composable
fun rememberSystemAnimationScale(): Float {
    val context = LocalContext.current
    var durationScale by remember(context) {
        mutableFloatStateOf(readSystemAnimationScale(context))
    }
    DisposableEffect(context) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                durationScale = readSystemAnimationScale(context)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose {
            resolver.unregisterContentObserver(observer)
        }
    }
    return durationScale
}

private fun readSystemAnimationScale(context: Context): Float = runCatching {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
}.getOrDefault(1f).takeIf { it.isFinite() && it >= 0f } ?: 1f

@Composable
fun ElovaireAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = ElovaireMotion.standardEnter(),
    exit: ExitTransition = ElovaireMotion.standardExit(),
    label: String,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
fun <S> ElovaireAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        ElovaireMotion.softContentTransform()
    },
    contentAlignment: Alignment = Alignment.TopStart,
    contentKey: (targetState: S) -> Any? = { it },
    label: String,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        contentKey = contentKey,
        label = label,
        content = content,
    )
}

@Composable
fun <S> ElovaireCrossfade(
    targetState: S,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = ElovaireMotion.fadeMedium(),
    label: String,
    content: @Composable (targetState: S) -> Unit,
) {
    androidx.compose.animation.Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = label,
        content = content,
    )
}

fun Modifier.elovaireAnimateContentSize(): Modifier = animateContentSize(
    animationSpec = ElovaireMotion.sizeSoft(),
)
