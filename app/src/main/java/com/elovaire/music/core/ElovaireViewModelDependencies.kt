package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackController
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.FavoritesStore
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaybackSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService

internal interface RootReadDependencies {
    val libraryReader: LibraryReader
    val rootSettingsReader: RootSettingsReader
    val playbackReader: PlaybackReader
}

internal interface ElovaireViewModelDependencies : RootReadDependencies {
    val libraryRepository: LibraryRepository
    val preferenceStore: PreferenceStore
    val playbackManager: PlaybackManager
    val lyricsService: LyricsService
    val albumTagEditorService: AlbumTagEditorService
    val backgroundWorkPolicy: AppBackgroundWorkPolicy
}

internal interface PlaybackActionDependencies {
    val playbackController: PlaybackController
}

internal interface LibraryActionDependencies {
    val libraryRepository: LibraryRepository
}

internal interface SettingsActionDependencies {
    val appearanceSettings: AppearanceSettingsWriter
    val librarySettings: LibrarySettingsWriter
    val playbackSettings: PlaybackSettingsWriter
}

internal interface PlaylistActionDependencies {
    val playlistStore: PlaylistStore
    val favoritesStore: FavoritesStore
}
