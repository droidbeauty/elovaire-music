package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsReader
import elovaire.music.droidbeauty.app.data.lyrics.LyricsWriter
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackController
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsStore
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.EqualizerSettingsStore
import elovaire.music.droidbeauty.app.data.settings.FavoritesStore
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.settings.NowPlayingSettingsStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.data.settings.SearchSettingsStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditor
import kotlinx.coroutines.flow.StateFlow

internal interface RootReadDependencies {
    val libraryReader: LibraryReader
    val rootSettingsReader: RootSettingsReader
    val playbackReader: PlaybackReader
}

internal interface RootViewModelDependencies : RootReadDependencies

internal interface SearchViewModelDependencies {
    val libraryReader: LibraryReader
    val searchSettings: SearchSettingsStore
    val playbackReader: PlaybackReader
}

internal interface NowPlayingViewModelDependencies {
    val playback: NowPlayingPlayback
    val settings: NowPlayingSettingsStore
    val lyricsReader: LyricsReader
    val lyricsWriter: LyricsWriter
}

internal interface EqualizerViewModelDependencies {
    val settings: EqualizerSettingsStore
}

internal interface AlbumTagEditorViewModelDependencies {
    val libraryReader: LibraryReader
    val libraryTagUpdates: LibraryTagUpdateWriter
    val editor: AlbumTagEditor
}

internal interface ElovaireViewModelDependencies {
    val root: RootViewModelDependencies
    val search: SearchViewModelDependencies
    val nowPlaying: NowPlayingViewModelDependencies
    val equalizer: EqualizerViewModelDependencies
    val albumTagEditor: AlbumTagEditorViewModelDependencies
}

internal interface PlaybackActionDependencies {
    val playbackController: PlaybackController
}

internal interface LibraryActionDependencies {
    val libraryRepository: LibraryRepository
    val networkSources: StateFlow<List<NetworkLibrarySource>>
    val networkProbeResults: StateFlow<Map<String, NetworkProbeResult>>
    fun saveNetworkSource(source: NetworkLibrarySource, credentials: NetworkCredentials)
    fun removeNetworkSource(source: NetworkLibrarySource)
}

internal interface SettingsActionDependencies {
    val appearanceSettings: AppearanceSettingsWriter
    val appearanceSettingsReader: AppearanceSettingsStore
    val librarySettings: LibrarySettingsWriter
    val playbackSettings: PlaybackSettingsWriter
}

internal interface PlaylistActionDependencies {
    val playlistStore: PlaylistStore
    val favoritesStore: FavoritesStore
}
