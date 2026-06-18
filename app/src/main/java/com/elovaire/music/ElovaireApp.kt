package elovaire.music.droidbeauty.app

import android.app.Application
import elovaire.music.droidbeauty.app.core.AppContainer

class ElovaireApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTerminate() {
        runCatching { container.release() }
        super.onTerminate()
    }
}
