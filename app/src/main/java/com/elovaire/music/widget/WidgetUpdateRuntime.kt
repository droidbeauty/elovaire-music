package elovaire.music.droidbeauty.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.GlanceId
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface WidgetProviderUpdater {
    suspend fun activeInstanceCount(): Int
    suspend fun updateAll()
}

internal class GlanceWidgetProviderUpdater(
    context: Context,
    private val widget: GlanceAppWidget,
) : WidgetProviderUpdater {
    private val applicationContext = context.applicationContext
    private val manager = GlanceAppWidgetManager(applicationContext)

    override suspend fun activeInstanceCount(): Int = withContext(Dispatchers.IO) {
        glanceIds().size
    }

    override suspend fun updateAll() {
        glanceIds().forEach { glanceId -> widget.update(applicationContext, glanceId) }
    }

    private suspend fun glanceIds(): List<GlanceId> = withContext(Dispatchers.IO) {
        manager.getGlanceIds(widget.javaClass)
    }
}

internal object WidgetProviderRegistry {
    val productionProviders: List<WidgetProviderUpdater> = emptyList()
}

internal class WidgetUpdateRuntime(
    private val playbackReader: PlaybackReader,
    private val snapshotStore: WidgetSnapshotStore,
    private val scope: CoroutineScope,
    private val providers: List<WidgetProviderUpdater>,
    private val clock: AppClock = AndroidAppClock,
    private val diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) {
    private val lock = Any()
    private var closed = false
    private var reconciliationJob: Job? = null
    private var observationJob: Job? = null
    private var observedProviders: List<WidgetProviderUpdater> = emptyList()

    @Suppress("TooGenericExceptionCaught")
    fun onProviderLifecycleChanged() {
        if (providers.isEmpty()) return
        synchronized(lock) {
            if (closed || reconciliationJob?.isActive == true) return
            val currentJob = scope.launch(start = CoroutineStart.LAZY) {
                try {
                    reconcileInstances()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    diagnostics.recordWorkerFailure("widget-provider-lifecycle", failure)
                } finally {
                    val completedJob = currentCoroutineContext()[Job]
                    synchronized(lock) {
                        if (reconciliationJob === completedJob) reconciliationJob = null
                    }
                }
            }
            reconciliationJob = currentJob
            currentJob.start()
        }
    }

    fun release() {
        val jobs = synchronized(lock) {
            if (closed) return
            closed = true
            listOfNotNull(reconciliationJob, observationJob).also {
                reconciliationJob = null
                observationJob = null
                observedProviders = emptyList()
            }
        }
        jobs.forEach { job -> job.cancel() }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun reconcileInstances() {
        val activeProviders = providers.filter { provider ->
            try {
                provider.activeInstanceCount() > 0
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                diagnostics.recordWorkerFailure("widget-instance-query", failure)
                false
            }
        }
        synchronized(lock) {
            if (closed) return
            if (activeProviders != observedProviders) {
                observationJob?.cancel()
                observationJob = null
                observedProviders = activeProviders
            }
            if (activeProviders.isEmpty()) {
                observationJob?.cancel()
                observationJob = null
            } else if (observationJob?.isActive != true) {
                val job = scope.launch(start = CoroutineStart.LAZY) { observePlayback(activeProviders) }
                observationJob = job
                job.start()
            }
        }
    }

    @OptIn(FlowPreview::class)
    @Suppress("TooGenericExceptionCaught")
    private suspend fun observePlayback(activeProviders: List<WidgetProviderUpdater>) {
        var previous = snapshotStore.readLatest()
        combine(
            playbackReader.nowPlayingState,
            playbackReader.transportState,
            playbackReader.queueState,
            ::PlaybackSample,
        ).debounce(UPDATE_COALESCE_MS).collect { sample ->
            val snapshot = projectWidgetPlaybackSnapshot(
                nowPlaying = sample.nowPlaying,
                transport = sample.transport,
                queue = sample.queue,
                previous = previous,
                elapsedRealtimeMs = clock.elapsedTimeMs(),
            )
            val changed = try {
                snapshotStore.writeIfChanged(snapshot)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                diagnostics.recordWorkerFailure("widget-snapshot-write", failure)
                false
            }
            if (changed) {
                previous = snapshot
                activeProviders.forEach { provider ->
                    try {
                        provider.updateAll()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        diagnostics.recordWorkerFailure("widget-provider-update", failure)
                    }
                }
            }
        }
    }

    private data class PlaybackSample(
        val nowPlaying: elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState,
        val transport: elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState,
        val queue: elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState,
    )

    private companion object {
        const val UPDATE_COALESCE_MS = 50L
    }
}
