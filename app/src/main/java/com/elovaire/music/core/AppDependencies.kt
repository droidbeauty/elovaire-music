package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource

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
        override val networkSources get() = services.networkSources
        override val networkProbeResults get() = services.networkProbeResults
        override fun saveNetworkSource(source: NetworkLibrarySource, credentials: NetworkCredentials) {
            services.saveNetworkSource(source, credentials)
        }
        override fun removeNetworkSource(source: NetworkLibrarySource) {
            services.removeNetworkSource(source)
        }
    }
    val settingsActionDependencies: SettingsActionDependencies = object : SettingsActionDependencies {
        override val appearanceSettings get() = services.preferenceStore
        override val appearanceSettingsReader get() = services.preferenceStore
        override val librarySettings get() = services.preferenceStore
        override val playbackSettings get() = services.preferenceStore
    }
    val playlistActionDependencies: PlaylistActionDependencies = object : PlaylistActionDependencies {
        override val playlistStore get() = services.preferenceStore
        override val favoritesStore get() = services.preferenceStore
    }
    val viewModelDependencies: ElovaireViewModelDependencies = object : ElovaireViewModelDependencies {
        override val root: RootViewModelDependencies = object : RootViewModelDependencies {
            override val libraryReader get() = services.libraryRepository
            override val rootSettingsReader get() = services.preferenceStore
            override val playbackReader get() = services.playbackManager
        }
        override val search: SearchViewModelDependencies = object : SearchViewModelDependencies {
            override val libraryReader get() = services.libraryRepository
            override val searchSettings get() = services.preferenceStore
            override val playbackReader get() = services.playbackManager
        }
        override val nowPlaying: NowPlayingViewModelDependencies = object : NowPlayingViewModelDependencies {
            override val playback get() = services.playbackManager
            override val settings get() = services.preferenceStore
            override val lyricsReader get() = services.lyricsService
            override val lyricsWriter get() = services.lyricsService
        }
        override val equalizer: EqualizerViewModelDependencies = object : EqualizerViewModelDependencies {
            override val settings get() = services.preferenceStore
        }
        override val albumTagEditor: AlbumTagEditorViewModelDependencies =
            object : AlbumTagEditorViewModelDependencies {
                override val libraryReader get() = services.libraryRepository
                override val libraryTagUpdates get() = services.libraryRepository
                override val editor get() = services.albumTagEditorService
            }
    }
}
