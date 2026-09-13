package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class AppServiceScopes(parentScope: CoroutineScope) {
    val playback: CoroutineScope = ownedChildScope(parentScope, "playback")
    val library: CoroutineScope = ownedChildScope(parentScope, "library")
    val optional: CoroutineScope = ownedChildScope(parentScope, "optional")

    fun cancelPlayback() = playback.cancel()

    fun cancelLibrary() = library.cancel()

    fun cancelOptional() = optional.cancel()

}
