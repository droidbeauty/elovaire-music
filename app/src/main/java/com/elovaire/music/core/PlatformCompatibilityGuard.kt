package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.BuildConfig

internal object PlatformCompatibilityGuard {
    fun install() {
        if (!BuildConfig.DEBUG) return
        DebugStrictModeInstaller.install()
    }
}
