package elovaire.music.droidbeauty.app

import android.app.Application
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.PlatformCompatibilityGuard
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace

class ElovaireApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        PlatformCompatibilityGuard.install()
        container = ElovaireTrace.section("app_container_create") {
            AppContainer(this)
        }
    }

    override fun onTerminate() {
        runCatching { container.release() }
        super.onTerminate()
    }
}
