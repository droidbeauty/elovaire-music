package elovaire.music.droidbeauty.app.data.library

internal sealed interface LibraryFailure {
    data object PermissionMissing : LibraryFailure
    data object MediaStoreUnavailable : LibraryFailure
    data class SafProviderFailure(
        val authority: String?,
        val operation: String,
        val cause: Throwable?,
    ) : LibraryFailure
    data class DatabaseFailure(val operation: String, val cause: Throwable?) : LibraryFailure
    data class ScanFailure(val phase: String, val cause: Throwable?) : LibraryFailure
}

internal fun LibraryFailure.toUserMessage(): String = when (this) {
    LibraryFailure.PermissionMissing -> "Music access is required to scan the library."
    LibraryFailure.MediaStoreUnavailable -> "The device media library is unavailable."
    is LibraryFailure.SafProviderFailure -> "The selected library folder could not be read."
    is LibraryFailure.DatabaseFailure -> "The local library index could not be updated."
    is LibraryFailure.ScanFailure -> "Unable to scan local music."
}

internal fun Throwable.toLibraryScanFailure(phase: String): LibraryFailure = when (this) {
    is SecurityException -> LibraryFailure.PermissionMissing
    else -> LibraryFailure.ScanFailure(phase, this)
}
