package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.lyrics.LyricsService
import elovaire.music.droidbeauty.app.data.playback.PlaybackManager
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditorService
import elovaire.music.droidbeauty.app.data.update.AppUpdateManager

internal interface ElovaireViewModelDependencies {
    val libraryRepository: LibraryRepository
    val preferenceStore: PreferenceStore
    val playbackManager: PlaybackManager
    val lyricsService: LyricsService
    val albumTagEditorService: AlbumTagEditorService
    val appUpdateManager: AppUpdateManager
}
