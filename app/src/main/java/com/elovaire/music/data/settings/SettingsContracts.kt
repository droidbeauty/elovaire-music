package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

internal interface AppearanceSettingsStore {
    val eqSettings: StateFlow<EqSettings>
    val themeMode: StateFlow<ThemeMode>
    val textSizePreset: StateFlow<TextSizePreset>
    val appLanguage: StateFlow<AppLanguage>
    val albumCollectionLayoutMode: StateFlow<String>
    val songCollectionGridEnabled: StateFlow<Boolean>
    val albumCollectionSortMode: StateFlow<String>
    val songCollectionSortMode: StateFlow<String>
    val onlineLyricsLookupEnabled: StateFlow<Boolean>
    val volumeNormalizationEnabled: StateFlow<Boolean>
}

internal interface CollectionSettingsStore {
    val playlists: StateFlow<List<Playlist>>
    val smartPlaylists: StateFlow<List<SmartPlaylist>>
    val favoriteSongIds: StateFlow<List<Long>>
    val albumPlayCounts: StateFlow<Map<Long, Int>>
    val songPlayCounts: StateFlow<Map<Long, Int>>
    val recentSongIds: StateFlow<List<Long>>
    val recentAlbumIds: StateFlow<List<Long>>
    val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?>
    val lastPlayedCollectionId: StateFlow<Long?>
}

internal interface RootSettingsReader : AppearanceSettingsStore, CollectionSettingsStore

internal interface PlaybackIntegrationSettings {
    val eqSettings: StateFlow<EqSettings>
    val gaplessPlaybackEnabled: StateFlow<Boolean>
    val volumeNormalizationEnabled: StateFlow<Boolean>
    fun recordPlaybackTransition(songId: Long?, albumId: Long?)
}

internal interface AppearanceSettingsWriter {
    fun setThemeMode(themeMode: ThemeMode)
    fun setTextSizePreset(textSizePreset: TextSizePreset)
    fun setAppLanguage(language: AppLanguage)
}

internal interface LibrarySettingsWriter {
    val libraryFolders: StateFlow<List<LibraryFolderSelection>>
    fun addLibraryFolder(selection: LibraryFolderSelection)
    fun removeLibraryFolder(selection: LibraryFolderSelection)
    fun setLibraryFolders(selections: List<LibraryFolderSelection>)
    fun restoreDefaultLibraryFolderIfEmpty()
    fun setAlbumCollectionLayoutMode(mode: String)
    fun setSongCollectionGridEnabled(enabled: Boolean)
    fun setAlbumCollectionSortMode(sortMode: String)
    fun setSongCollectionSortMode(sortMode: String)
}

internal interface PlaybackSettingsWriter {
    fun setPlaybackVolume(value: Float)
    fun setGaplessPlaybackEnabled(enabled: Boolean)
    fun setVolumeNormalizationEnabled(enabled: Boolean)
    fun updateBass(value: Float)
    fun updateMidrange(value: Float)
    fun updateTreble(value: Float)
    fun updateMonoPlaybackEnabled(enabled: Boolean)
}

internal interface PlaylistStore {
    fun createPlaylist(name: String): Long
    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    fun renamePlaylist(playlistId: Long, name: String)
    fun updatePlaylistSongIds(playlistId: Long, songIds: List<Long>)
    fun deletePlaylists(playlistIds: Set<Long>)
    fun removeSongReferences(songIds: Set<Long>)
    fun createSmartPlaylist(name: String): Long
    fun updateSmartPlaylist(playlist: SmartPlaylist)
    fun deleteSmartPlaylists(playlistIds: Set<Long>)
}

internal interface FavoritesStore {
    fun toggleFavoriteSong(songId: Long)
    fun setFavoriteSongs(songIds: List<Long>, favorite: Boolean)
}

internal interface UpdatePreferencesStore {
    val dismissedUpdateVersion: StateFlow<String?>
    fun setDismissedUpdateVersion(versionName: String?)
    fun lastAutomaticUpdateCheckAtMs(): Long
    fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long)
}
