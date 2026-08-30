package elovaire.music.droidbeauty.app.data.mutation

internal enum class MediaMutationTransactionPhase {
    BeforeJournal,
    AfterJournal,
    SourcePreflight,
    BackupCopied,
    WorkingCopyCopied,
    WorkingMutationStarted,
    WorkingVerified,
    OriginalOverwrite,
    OriginalCommitted,
    PersistedVerification,
    RollbackStarted,
    CleanupStarted,
}

internal fun interface MediaMutationFaultInjector {
    fun checkpoint(phase: MediaMutationTransactionPhase)
}

internal val NoOpMediaMutationFaultInjector = MediaMutationFaultInjector { }
