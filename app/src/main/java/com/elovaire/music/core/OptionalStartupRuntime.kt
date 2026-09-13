package elovaire.music.droidbeauty.app.core

import android.util.Log
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.update.UpdateController
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Owns non-critical startup maintenance so its failures cannot poison durable readiness. */
internal class OptionalStartupRuntime(
    private val scope: CoroutineScope,
    private val backgroundWorkPolicy: AppBackgroundWorkPolicy,
    private val portableSettingsBackup: PortableSettingsBackup,
    private val updateControllerProvider: () -> UpdateController,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val released = AtomicBoolean(false)
    private var job: Job? = null

    @Suppress("TooGenericExceptionCaught")
    fun start(
        mediaMutationRecoverySucceeded: Boolean,
        exitSnapshot: AppExitSnapshot,
    ) {
        if (released.get() || job?.isActive == true) return
        job = scope.launch(ioDispatcher) {
            try {
                backgroundWorkPolicy.setOptionalStartupSuppressed(
                    exitSnapshot.suppressOptionalStartup || !mediaMutationRecoverySucceeded,
                )
                portableSettingsBackup.start()
                updateControllerProvider().scheduleStartupMaintenance()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: RuntimeException) {
                Log.w(TAG, "Optional startup work deferred after playback startup", failure)
                backgroundWorkPolicy.setOptionalStartupSuppressed(true)
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        job?.cancel()
        job = null
    }

    private companion object {
        const val TAG = "OptionalStartup"
    }
}
