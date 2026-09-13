package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticRecorder
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class AppServiceScopes(
    parentScope: CoroutineScope,
    diagnostics: BackendDiagnosticRecorder = BackendDiagnostics,
) {
    val playback: CoroutineScope = ownedChildScope(parentScope, "playback", diagnostics)
    val library: CoroutineScope = ownedChildScope(parentScope, "library", diagnostics)
    val optional: CoroutineScope = ownedChildScope(parentScope, "optional", diagnostics)

    fun cancelPlayback() = playback.cancel()

    fun cancelLibrary() = library.cancel()

    fun cancelOptional() = optional.cancel()

}
