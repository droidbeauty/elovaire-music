package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.Deferred
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
    val volumeNormalizationEnabled: StateFlow<Boolean>
    val onlineLyricsEnabled: StateFlow<Boolean>
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
    val crossfadeEnabled: StateFlow<Boolean>
    val volumeNormalizationEnabled: StateFlow<Boolean>
    val recentSongIds: StateFlow<List<Long>>
    val recentAlbumIds: StateFlow<List<Long>>
    val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?>
    val lastPlayedCollectionId: StateFlow<Long?>
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
    fun setCrossfadeEnabled(enabled: Boolean)
    fun setVolumeNormalizationEnabled(enabled: Boolean)
    fun setOnlineLyricsEnabled(enabled: Boolean)
    fun updateBass(value: Float)
    fun updateMidrange(value: Float)
    fun updateTreble(value: Float)
    fun updateMonoPlaybackEnabled(enabled: Boolean)
}

internal interface PlaylistStore {
    fun createPlaylist(name: String): Deferred<PlaylistMutationResult>
    fun createPlaylistWithSongs(name: String, songIds: List<Long>): Deferred<PlaylistMutationResult>
    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>): Deferred<PlaylistMutationResult>
    fun renamePlaylist(playlistId: Long, name: String): Deferred<PlaylistMutationResult>
    fun updatePlaylistSongIds(playlistId: Long, songIds: List<Long>): Deferred<PlaylistMutationResult>
    fun deletePlaylists(playlistIds: Set<Long>): Deferred<PlaylistMutationResult>
    fun removeSongReferences(songIds: Set<Long>): Deferred<PlaylistMutationResult>
    fun createSmartPlaylist(name: String): Deferred<PlaylistMutationResult>
    fun createSmartPlaylist(playlist: SmartPlaylist): Deferred<PlaylistMutationResult>
    fun updateSmartPlaylist(playlist: SmartPlaylist): Deferred<PlaylistMutationResult>
    fun deleteSmartPlaylists(playlistIds: Set<Long>): Deferred<PlaylistMutationResult>
}

internal sealed interface PlaylistMutationResult {
    data class Success(
        val playlistId: Long? = null,
        val changed: Boolean = true,
    ) : PlaylistMutationResult

    data object NotFound : PlaylistMutationResult
    data object InvalidInput : PlaylistMutationResult
    data object NotAllowed : PlaylistMutationResult
    data class Failure(val reason: String, val cause: Throwable? = null) : PlaylistMutationResult
}

internal interface FavoritesStore {
    fun toggleFavoriteSong(songId: Long)
    fun setFavoriteSongs(songIds: List<Long>, favorite: Boolean)
}

internal interface PlaybackHistoryStore {
    val albumPlayCounts: StateFlow<Map<Long, Int>>
    val songPlayCounts: StateFlow<Map<Long, Int>>
    val recentSongIds: StateFlow<List<Long>>
    val recentAlbumIds: StateFlow<List<Long>>
    val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?>
    val lastPlayedCollectionId: StateFlow<Long?>

    fun recordPlaybackTransition(songId: Long?, albumId: Long?)
    fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    )
}

internal interface SearchHistoryStore {
    val searchHistory: StateFlow<List<SearchHistoryEntry>>
    fun addSearchHistoryEntry(entry: SearchHistoryEntry)
    fun clearSearchHistoryEntries()
}

internal interface UpdatePreferencesStore {
    val dismissedUpdateVersion: StateFlow<String?>
    fun setDismissedUpdateVersion(versionName: String?)
    fun lastAutomaticUpdateCheckAtMs(): Long
    fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long)
}
