package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryActionController
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.data.library.DeviceDeleteHandler
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.data.lyrics.LyricsReader
import elovaire.music.droidbeauty.app.data.lyrics.LyricsWriter
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackUiState
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsStore
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.CollectionSettingsStore
import elovaire.music.droidbeauty.app.data.settings.EqualizerSettingsStore
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.settings.NowPlayingSettingsStore
import elovaire.music.droidbeauty.app.data.settings.PlaybackSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaybackHistoryStore
import elovaire.music.droidbeauty.app.data.settings.SearchHistoryStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditor
import elovaire.music.droidbeauty.app.data.tags.AudiobookTagEditor
import kotlinx.coroutines.flow.StateFlow

internal interface RootDeleteDependencies {
    val deleteHandler: DeviceDeleteHandler
}

internal interface RootViewModelDependencies {
    val libraryReader: LibraryReader
    val appearanceSettings: AppearanceSettingsStore
    val userData: CollectionSettingsStore
    val playbackState: StateFlow<PlaybackUiState>
}

internal interface SearchViewModelDependencies {
    val libraryReader: LibraryReader
    val playbackHistory: PlaybackHistoryStore
    val searchHistory: SearchHistoryStore
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

internal interface AudiobookTagEditorViewModelDependencies {
    val libraryReader: LibraryReader
    val libraryTagUpdates: LibraryTagUpdateWriter
    val editor: AudiobookTagEditor
    val remapProgressKey: suspend (String, String) -> Unit
}

internal interface ElovaireViewModelDependencies {
    val dispatchers: AppDispatchers
    val root: RootViewModelDependencies
    val search: SearchViewModelDependencies
    val nowPlaying: NowPlayingViewModelDependencies
    val equalizer: EqualizerViewModelDependencies
    val albumTagEditor: AlbumTagEditorViewModelDependencies
    val audiobookTagEditor: AudiobookTagEditorViewModelDependencies
}

internal interface LibraryActionDependencies {
    val libraryController: LibraryActionController
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
