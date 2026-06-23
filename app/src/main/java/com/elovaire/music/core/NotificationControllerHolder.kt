package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.playback.PlaybackNotificationController

internal class NotificationControllerHolder(
    private val factory: () -> PlaybackNotificationController,
) {
    private var controller: PlaybackNotificationController? = null

    fun get(): PlaybackNotificationController {
        return controller ?: factory().also { created ->
            controller = created
        }
    }

    fun release() {
        controller?.setNotificationsEnabled(false)
        controller = null
    }
}
