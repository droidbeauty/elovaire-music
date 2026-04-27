package elovaire.music.app

import android.app.Application
import elovaire.music.app.core.AppContainer

class ElovaireApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

