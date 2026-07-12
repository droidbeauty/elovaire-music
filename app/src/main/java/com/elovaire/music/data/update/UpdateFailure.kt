package elovaire.music.droidbeauty.app.data.update

internal sealed interface UpdateFailure {
    data object Offline : UpdateFailure
    data object UnvalidatedNetwork : UpdateFailure
    data class HttpFailure(val code: Int?) : UpdateFailure
    data object InvalidReleasePayload : UpdateFailure
    data object DownloadIncomplete : UpdateFailure
    data object ChecksumMismatch : UpdateFailure
    data class InstallerFailure(val cause: Throwable?) : UpdateFailure
}
