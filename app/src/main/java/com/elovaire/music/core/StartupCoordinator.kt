package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.update.AppUpdateManager

internal class StartupCoordinator(
    private val appUpdateManager: AppUpdateManager,
) {
    fun start() = Unit

    fun scheduleDeferredStartupWork() {
        appUpdateManager.scheduleStartupMaintenance()
    }
}
