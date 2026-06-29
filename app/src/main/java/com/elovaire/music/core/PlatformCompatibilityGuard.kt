package elovaire.music.droidbeauty.app.core

import android.os.Build
import android.os.StrictMode
import elovaire.music.droidbeauty.app.BuildConfig

internal object PlatformCompatibilityGuard {
    fun install() {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectNonSdkApiUsage()
                .penaltyLog()
                .build(),
        )
    }
}
