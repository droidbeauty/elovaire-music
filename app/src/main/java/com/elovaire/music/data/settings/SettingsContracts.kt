package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface RootSettingsReader {
    val eqSettings: StateFlow<EqSettings>
    val themeMode: StateFlow<ThemeMode>
    val textSizePreset: StateFlow<TextSizePreset>
    val appLanguage: StateFlow<AppLanguage>
    val playlists: StateFlow<List<Playlist>>
    val favoriteSongIds: StateFlow<List<Long>>
    val albumPlayCounts: StateFlow<Map<Long, Int>>
    val songPlayCounts: StateFlow<Map<Long, Int>>
    val albumCollectionLayoutMode: StateFlow<String>
    val songCollectionGridEnabled: StateFlow<Boolean>
    val albumCollectionSortMode: StateFlow<String>
    val songCollectionSortMode: StateFlow<String>
    val onlineLyricsLookupEnabled: StateFlow<Boolean>
    val recentSongIds: StateFlow<List<Long>>
    val recentAlbumIds: StateFlow<List<Long>>
    val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?>
    val lastPlayedCollectionId: StateFlow<Long?>
}
