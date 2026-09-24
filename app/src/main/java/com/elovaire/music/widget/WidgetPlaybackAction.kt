package elovaire.music.droidbeauty.app.widget

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.GlanceId
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.data.playback.ElovaireMediaLibraryService
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal enum class WidgetPlaybackAction(val parameterValue: String) {
    TogglePlayback("toggle_playback"),
    SkipPrevious("skip_previous"),
    SkipNext("skip_next");

    companion object {
        fun fromParameter(value: String?): WidgetPlaybackAction? =
            entries.firstOrNull { it.parameterValue == value }
    }
}

internal val WIDGET_PLAYBACK_ACTION_PARAMETER = ActionParameters.Key<String>("elovaire_widget_playback_action")

internal fun widgetPlaybackAction(action: WidgetPlaybackAction) = actionRunCallback<WidgetPlaybackActionCallback>(
    actionParametersOf(WIDGET_PLAYBACK_ACTION_PARAMETER to action.parameterValue),
)

internal fun interface WidgetPlaybackCommandGateway {
    suspend fun dispatch(action: WidgetPlaybackAction)
}

internal enum class WidgetActionDispatchResult {
    Dispatched,
    UnknownAction,
    TimedOut,
}

internal class WidgetPlaybackActionDispatcher(
    private val gateway: WidgetPlaybackCommandGateway,
    private val timeoutMs: Long = CONNECTION_TIMEOUT_MS,
) {
    suspend fun dispatch(actionValue: String?): WidgetActionDispatchResult {
        val action = WidgetPlaybackAction.fromParameter(actionValue)
            ?: return WidgetActionDispatchResult.UnknownAction
        require(timeoutMs > 0L)
        return if (withTimeoutOrNull(timeoutMs) { gateway.dispatch(action); true } == true) {
            WidgetActionDispatchResult.Dispatched
        } else {
            WidgetActionDispatchResult.TimedOut
        }
    }

    private companion object {
        const val CONNECTION_TIMEOUT_MS = 5_000L
    }
}

internal class Media3WidgetPlaybackCommandGateway(context: Context) : WidgetPlaybackCommandGateway {
    private val applicationContext = context.applicationContext

    override suspend fun dispatch(action: WidgetPlaybackAction) {
        val token = SessionToken(
            applicationContext,
            ComponentName(applicationContext, ElovaireMediaLibraryService::class.java),
        )
        val future = MediaController.Builder(applicationContext, token).buildAsync()
        val controller = awaitController(future)
        try {
            withContext(Dispatchers.Main.immediate) {
                when (action) {
                    WidgetPlaybackAction.TogglePlayback -> {
                        if (controller.playWhenReady) controller.pause() else controller.play()
                    }
                    WidgetPlaybackAction.SkipPrevious -> controller.seekToPreviousMediaItem()
                    WidgetPlaybackAction.SkipNext -> controller.seekToNextMediaItem()
                }
            }
        } finally {
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                controller.release()
            }
        }
    }

    private suspend fun awaitController(
        future: com.google.common.util.concurrent.ListenableFuture<MediaController>,
    ): MediaController = suspendCancellableCoroutine { continuation ->
        future.addListener(
            {
                if (!continuation.isActive) return@addListener
                try {
                    continuation.resume(future.get())
                } catch (failure: ExecutionException) {
                    continuation.resumeWithException(failure.cause ?: failure)
                } catch (failure: CancellationException) {
                    continuation.cancel(failure)
                } catch (failure: InterruptedException) {
                    Thread.currentThread().interrupt()
                    continuation.resumeWithException(failure)
                }
            },
            ContextCompat.getMainExecutor(applicationContext),
        )
        continuation.invokeOnCancellation {
            ContextCompat.getMainExecutor(applicationContext).execute {
                MediaController.releaseFuture(future)
            }
        }
    }
}

class WidgetPlaybackActionCallback : ActionCallback {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val result = try {
            WidgetPlaybackActionDispatcher(Media3WidgetPlaybackCommandGateway(context))
                .dispatch(parameters[WIDGET_PLAYBACK_ACTION_PARAMETER])
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            BackendDiagnostics.recordWorkerFailure("widget-playback-action", failure)
            return
        }
        if (result == WidgetActionDispatchResult.TimedOut) {
            BackendDiagnostics.recordWorkerFailure(
                "widget-playback-action",
                TimeoutException("Media session connection timed out."),
            )
        }
    }
}
