package elovaire.music.droidbeauty.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.PlatformCompatibilityGuard
import elovaire.music.droidbeauty.app.core.memoryPressureForTrimLevel
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace

class ElovaireApp : Application(), Configuration.Provider {
    private val containerDelegate = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ElovaireTrace.section("app_container_create") {
            AppContainer(this).also(AppContainer::start)
        }
    }
    val container: AppContainer get() = containerDelegate.value

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.INFO else Log.ERROR)
            .build()

    override fun onCreate() {
        super.onCreate()
        PlatformCompatibilityGuard.install()
    }

    override fun onTerminate() {
        if (containerDelegate.isInitialized()) runCatching { container.release() }
        super.onTerminate()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (containerDelegate.isInitialized()) {
            container.onMemoryPressure(memoryPressureForTrimLevel(level))
        }
    }
}
