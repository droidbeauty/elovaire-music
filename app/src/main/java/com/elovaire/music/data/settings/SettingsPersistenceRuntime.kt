package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.util.Log
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/** Owns the single DataStore collector, immutable boot snapshot, and serialized settings writes. */
internal class SettingsPersistenceRuntime(
    context: Context,
    ownerScope: CoroutineScope?,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    initialSettings: SettingsSnapshot,
    private val portableSettingsBackup: PortableSettingsBackup? = null,
    private val diagnostics: BackendDiagnosticsRuntime? = null,
) {
    private val appContext = context.applicationContext
    internal val dataStore = appContext.elovaireSettingsDataStore()
    internal val scope: CoroutineScope = CoroutineScope(
        (ownerScope?.coroutineContext ?: EmptyCoroutineContext) +
            SupervisorJob(ownerScope?.coroutineContext?.get(Job)) +
            ioDispatcher + CoroutineName("settings-persistence-owner"),
    )
    private var currentSettings = initialSettings
    private var bootSettingsInitialized = false
    internal val snapshot: SettingsSnapshot
        get() = currentSettings
    internal val writeSequencer = SettingsWriteSequencer(
        ownerScope = scope,
        dispatcher = ioDispatcher,
        persist = ::persist,
    )

    fun observe(consumer: (androidx.datastore.preferences.core.Preferences) -> Unit): Job = scope.launch {
        dataStore.data.collect { consumer(it) }
    }

    internal fun replaceSnapshot(snapshot: SettingsSnapshot) {
        currentSettings = snapshot
    }

    internal fun checkpoint(values: Map<String, Any?>) {
        currentSettings = SettingsSnapshot(values)
        bootSettingsInitialized = true
        portableSettingsBackup?.checkpointBootSettings(values)
    }

    internal fun isBootSettingsInitialized(): Boolean = bootSettingsInitialized

    internal fun release(ioDispatcher: CoroutineDispatcher) {
        runBlocking(ioDispatcher) { writeSequencer.flush() }
        writeSequencer.close()
        scope.cancel()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun persist(kind: String, write: suspend () -> Unit) {
        try {
            write()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            Log.w(TAG, "Unable to persist $kind settings.", failure)
        } catch (failure: IllegalStateException) {
            Log.w(TAG, "Unable to persist $kind settings.", failure)
        } catch (failure: RuntimeException) {
            (diagnostics ?: BackendDiagnostics).recordWorkerFailure("settings-persistence", failure)
            throw failure
        }
    }

    private companion object {
        const val TAG = "SettingsPersistence"
    }
}
