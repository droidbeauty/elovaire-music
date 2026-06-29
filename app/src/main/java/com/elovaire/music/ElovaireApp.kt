package elovaire.music.droidbeauty.app

import android.app.Application
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.PlatformCompatibilityGuard

class ElovaireApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        PlatformCompatibilityGuard.install()
        container = AppContainer(this)
    }

    override fun onTerminate() {
        runCatching { container.release() }
        super.onTerminate()
    }
}
