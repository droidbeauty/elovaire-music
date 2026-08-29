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
                isLibraryLoading ||
                (
                    songsCount == 0 &&
                        albumsCount == 0 &&
                        errorMessage == null &&
                        !playFirstLaunchHomeReveal
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
): Boolean {
    return firstLaunchPermissionExperienceActive &&
        hasAudioPermission &&
        !isLibraryLoading &&
        (songsCount > 0 || albumsCount > 0 || errorMessage != null)
}
