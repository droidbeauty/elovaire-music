package elovaire.music.droidbeauty.app.data.library

internal sealed interface LibraryRuntimeState {
    data object NoPermission : LibraryRuntimeState
    data object Idle : LibraryRuntimeState
    data class Bootstrapping(val permissionVersion: Long) : LibraryRuntimeState
    data class Scanning(
        val request: LibraryRefreshRequest,
        val permissionVersion: Long,
    ) : LibraryRuntimeState
    data class BackgroundDirty(val pending: LibraryRefreshRequest) : LibraryRuntimeState
    data class Failed(
        val failure: LibraryFailure,
        val recoverable: Boolean,
    ) : LibraryRuntimeState
    data object Released : LibraryRuntimeState
}
