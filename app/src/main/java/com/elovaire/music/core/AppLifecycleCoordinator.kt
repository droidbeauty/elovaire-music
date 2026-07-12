package elovaire.music.droidbeauty.app.core

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(UnstableApi::class)
internal class AppLifecycleCoordinator(
    private val bridges: AppBridgeCoordinator,
    private val notifications: NotificationControllerHolder,
    private val services: AppServices,
    private val runtimeScope: AppRuntimeScope,
    private val foregroundTracker: AppForegroundTracker,
) {
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)

    fun start() {
        if (released.get() || !started.compareAndSet(false, true)) return
        bridges.start()
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        bridges.release()
        notifications.release()
        services.release()
        runtimeScope.close()
        foregroundTracker.close()
    }
}
