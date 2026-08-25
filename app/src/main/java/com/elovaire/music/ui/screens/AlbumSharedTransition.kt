package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import elovaire.music.droidbeauty.app.ui.motion.MotionDuration
import elovaire.music.droidbeauty.app.ui.motion.MotionEasing
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs
import java.util.concurrent.atomic.AtomicLong

@Immutable
internal data class AlbumSharedTransitionSelection(
    val albumId: Long,
    val sourceToken: Long,
)

internal class AlbumSharedTransitionController {
    var selection by mutableStateOf<AlbumSharedTransitionSelection?>(null)
        private set

    fun select(albumId: Long, sourceToken: Long) {
        selection = AlbumSharedTransitionSelection(albumId, sourceToken)
    }
}

internal object AlbumSharedTransitionToken {
    private val next = AtomicLong(0L)

    fun next(): Long = next.incrementAndGet()
}

internal val LocalAlbumSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope?> { null }

internal val LocalAlbumSharedTransitionController =
    staticCompositionLocalOf<AlbumSharedTransitionController?> { null }

internal val LocalAlbumAnimatedVisibilityScope =
    staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@Composable
internal fun AlbumTransitionContent(
    animatedVisibilityScope: AnimatedVisibilityScope,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAlbumAnimatedVisibilityScope provides animatedVisibilityScope,
        content = content,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.albumSharedArtwork(
    albumId: Long,
    sourceToken: Long? = null,
    destination: Boolean = false,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val scope = LocalAlbumSharedTransitionScope.current ?: return this
    val controller = LocalAlbumSharedTransitionController.current ?: return this
    val animatedVisibilityScope = LocalAlbumAnimatedVisibilityScope.current ?: return this
    val selection = controller.selection
    val token = if (destination) {
        selection?.takeIf { it.albumId == albumId }?.sourceToken
    } else {
        sourceToken
    } ?: return this
    val sharedContentState: SharedTransitionScope.SharedContentState = scope.rememberSharedContentState(
        key = AlbumSharedTransitionKey(albumId, token),
    )
    val specs = rememberMotionSpecs()
    val boundsTransform = androidx.compose.animation.BoundsTransform { _, _ ->
        specs.tween(
            durationMillis = MotionDuration.AlbumDetail,
            easing = MotionEasing.RefinedDecelerate,
        )
    }
    return with(scope) {
        this@albumSharedArtwork.sharedElement(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = boundsTransform,
            zIndexInOverlay = 1f,
        )
    }
}

@Immutable
private data class AlbumSharedTransitionKey(
    val albumId: Long,
    val sourceToken: Long,
)
