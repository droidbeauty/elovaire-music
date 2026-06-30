package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.update.UpdateController

internal class StartupCoordinator(
    private val appUpdateManager: UpdateController,
) {
    fun start() = Unit

    fun scheduleDeferredStartupWork() {
        appUpdateManager.scheduleStartupMaintenance()
    }
}
