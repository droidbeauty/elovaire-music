package elovaire.music.droidbeauty.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import elovaire.music.droidbeauty.app.core.backend.NoOpBackendResourceTracker

internal val LocalBackendResourceTracker = staticCompositionLocalOf<BackendResourceTracker> {
    NoOpBackendResourceTracker
}
