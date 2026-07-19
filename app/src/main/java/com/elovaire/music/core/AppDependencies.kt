package elovaire.music.droidbeauty.app.core

internal class AppDependencies(
    services: AppServices,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
) {
    val rootReadDependencies: RootReadDependencies = object : RootReadDependencies {
        override val libraryReader get() = services.libraryRepository
        override val rootSettingsReader get() = services.preferenceStore
        override val playbackReader get() = services.playbackManager
    }
    val playbackActionDependencies: PlaybackActionDependencies = object : PlaybackActionDependencies {
        override val playbackController get() = services.playbackManager
    }
    val libraryActionDependencies: LibraryActionDependencies = object : LibraryActionDependencies {
        override val libraryRepository get() = services.libraryRepository
    }
    val settingsActionDependencies: SettingsActionDependencies = object : SettingsActionDependencies {
        override val appearanceSettings get() = services.preferenceStore
        override val librarySettings get() = services.preferenceStore
        override val playbackSettings get() = services.preferenceStore
        override val setOnlineLyricsLookupEnabled = services.preferenceStore::setOnlineLyricsLookupEnabled
    }
    val playlistActionDependencies: PlaylistActionDependencies = object : PlaylistActionDependencies {
        override val playlistStore get() = services.preferenceStore
        override val favoritesStore get() = services.preferenceStore
    }
    val viewModelDependencies: ElovaireViewModelDependencies = object : ElovaireViewModelDependencies {
        override val libraryReader get() = services.libraryRepository
        override val libraryRepository get() = services.libraryRepository
        override val rootSettingsReader get() = services.preferenceStore
        override val preferenceStore get() = services.preferenceStore
        override val playbackReader get() = services.playbackManager
        override val playbackManager get() = services.playbackManager
        override val lyricsService get() = services.lyricsService
        override val albumTagEditorService get() = services.albumTagEditorService
        override val backgroundWorkPolicy get() = backgroundWorkPolicy
    }
}
