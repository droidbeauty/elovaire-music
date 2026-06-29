package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager
import elovaire.music.droidbeauty.app.data.update.UpdateReader

internal interface ElovaireViewModelDependencies {
    val libraryReader: LibraryReader
    val libraryRepository: LibraryRepository
    val rootSettingsReader: RootSettingsReader
    val preferenceStore: PreferenceStore
    val playbackReader: PlaybackReader
    val playbackManager: PlaybackManager
    val updateReader: UpdateReader
    val lyricsService: LyricsService
    val albumTagEditorService: AlbumTagEditorService
    val appUpdateManager: AppUpdateManager
    val backgroundWorkPolicy: AppBackgroundWorkPolicy
}
