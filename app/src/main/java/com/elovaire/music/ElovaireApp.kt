package elovaire.music.app

import android.app.Application
import elovaire.music.app.core.AppContainer

class ElovaireApp : Application() {
    lateinit var container: AppContainer
        private set
    private var shouldShowColdStartSplash = true

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    fun consumeColdStartSplash(): Boolean {
        val shouldShow = shouldShowColdStartSplash
        shouldShowColdStartSplash = false
        return shouldShow
    }
}
