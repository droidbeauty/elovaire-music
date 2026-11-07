package elovaire.music.droidbeauty.app.ui.screens

internal fun shouldShowFirstLaunchPermissionOverlay(
    firstLaunchPermissionExperienceActive: Boolean,
    hasAudioPermission: Boolean,
    isLibraryLoading: Boolean,
    songsCount: Int,
    albumsCount: Int,
    errorMessage: String?,
    playFirstLaunchHomeReveal: Boolean,
): Boolean {
    return firstLaunchPermissionExperienceActive &&
        (
            !hasAudioPermission ||
                (
                    !playFirstLaunchHomeReveal &&
                    (isLibraryLoading || songsCount == 0 &&
                        albumsCount == 0 &&
                        errorMessage == null)
                    )
            )
}

internal fun shouldRevealFirstLaunchHome(
    firstLaunchPermissionExperienceActive: Boolean,
    hasAudioPermission: Boolean,
    isLibraryLoading: Boolean,
    songsCount: Int,
    albumsCount: Int,
    errorMessage: String?,
    scanProgress: Float,
    initialWaitTimedOut: Boolean,
): Boolean {
    return firstLaunchPermissionExperienceActive &&
        hasAudioPermission &&
        (initialWaitTimedOut || !isLibraryLoading &&
            (scanProgress >= 1f || songsCount > 0 || albumsCount > 0 || errorMessage != null))
}
