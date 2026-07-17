package elovaire.music.droidbeauty.app.domain.kernel

internal enum class MediaMutationStatus {
    Created,
    PreflightPassed,
    NeedsPermission,
    PermissionGranted,
    TempWritten,
    TempVerified,
    Committed,
    PersistedVerified,
    Published,
    Completed,
    Failed,
    NeedsRepair,
    Cancelled,
}

internal fun MediaMutationStatus.isTerminal(): Boolean {
    return this == MediaMutationStatus.Completed ||
        this == MediaMutationStatus.Cancelled ||
        this == MediaMutationStatus.Failed ||
        this == MediaMutationStatus.NeedsRepair
}

internal fun recoveryStatusFor(status: MediaMutationStatus): MediaMutationStatus? {
    return when (status) {
        MediaMutationStatus.Created,
        MediaMutationStatus.PreflightPassed,
        MediaMutationStatus.NeedsPermission,
        MediaMutationStatus.PermissionGranted,
        MediaMutationStatus.TempWritten,
        MediaMutationStatus.TempVerified,
        -> MediaMutationStatus.Cancelled
        MediaMutationStatus.Committed -> MediaMutationStatus.NeedsRepair
        MediaMutationStatus.PersistedVerified,
        MediaMutationStatus.Published,
        -> MediaMutationStatus.Completed
        MediaMutationStatus.Failed,
        MediaMutationStatus.NeedsRepair,
        MediaMutationStatus.Completed,
        MediaMutationStatus.Cancelled,
        -> null
    }
}

internal fun isValidMutationTransition(
    current: MediaMutationStatus,
    next: MediaMutationStatus,
): Boolean {
    if (current == next) return false
    return next in when (current) {
        MediaMutationStatus.Created -> setOf(
            MediaMutationStatus.PreflightPassed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PreflightPassed -> setOf(
            MediaMutationStatus.NeedsPermission,
            MediaMutationStatus.TempWritten,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.NeedsPermission -> setOf(
            MediaMutationStatus.PermissionGranted,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PermissionGranted -> setOf(
            MediaMutationStatus.TempWritten,
            MediaMutationStatus.Failed,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.TempWritten -> setOf(
            MediaMutationStatus.TempVerified,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.TempVerified -> setOf(
            MediaMutationStatus.Committed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Committed -> setOf(
            MediaMutationStatus.PersistedVerified,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.PersistedVerified -> setOf(
            MediaMutationStatus.Published,
            MediaMutationStatus.Completed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Published -> setOf(
            MediaMutationStatus.Completed,
            MediaMutationStatus.Failed,
            MediaMutationStatus.NeedsRepair,
            MediaMutationStatus.Cancelled,
        )
        MediaMutationStatus.Failed -> setOf(MediaMutationStatus.NeedsRepair)
        MediaMutationStatus.NeedsRepair,
        MediaMutationStatus.Completed,
        MediaMutationStatus.Cancelled,
        -> emptySet()
    }
}
