package elovaire.music.app.data.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import elovaire.music.app.domain.model.EqSettings
import elovaire.music.app.domain.model.Playlist
import elovaire.music.app.domain.model.SearchHistoryEntry
import elovaire.music.app.domain.model.SearchHistoryKind
import elovaire.music.app.domain.model.SpaciousnessMode
import elovaire.music.app.domain.model.TextSizePreset
import elovaire.music.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceStore(context: Context) {
    private val preferences = context.getSharedPreferences("elovaire_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _textSizePreset = MutableStateFlow(loadTextSizePreset())
    val textSizePreset: StateFlow<TextSizePreset> = _textSizePreset.asStateFlow()

    private val _eqSettings = MutableStateFlow(loadEqSettings())
    val eqSettings: StateFlow<EqSettings> = _eqSettings.asStateFlow()

    private val _playbackVolume = MutableStateFlow(loadPlaybackVolume())
    val playbackVolume: StateFlow<Float> = _playbackVolume.asStateFlow()

    private val _libraryFolderUri = MutableStateFlow(loadLibraryFolderUri())
    val libraryFolderUri: StateFlow<Uri?> = _libraryFolderUri.asStateFlow()

    private val _libraryFolderPath = MutableStateFlow(loadLibraryFolderPath())
    val libraryFolderPath: StateFlow<String> = _libraryFolderPath.asStateFlow()
    private val _dismissedUpdateVersion = MutableStateFlow(loadDismissedUpdateVersion())
    val dismissedUpdateVersion: StateFlow<String?> = _dismissedUpdateVersion.asStateFlow()

    private val _searchHistory = MutableStateFlow(loadSearchHistory())
    val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()
    private val _albumPlayCounts = MutableStateFlow(loadAlbumPlayCounts())
    val albumPlayCounts: StateFlow<Map<Long, Int>> = _albumPlayCounts.asStateFlow()
    private val _songPlayCounts = MutableStateFlow(loadSongPlayCounts())
    val songPlayCounts: StateFlow<Map<Long, Int>> = _songPlayCounts.asStateFlow()
    private val _recentSongIds = MutableStateFlow(loadRecentSongIds())
    val recentSongIds: StateFlow<List<Long>> = _recentSongIds.asStateFlow()
    private val _recentAlbumIds = MutableStateFlow(loadRecentAlbumIds())
    val recentAlbumIds: StateFlow<List<Long>> = _recentAlbumIds.asStateFlow()

    private val _userPlaylists = MutableStateFlow(loadPlaylists())
    private val _favoriteSongIds = MutableStateFlow(loadFavoriteSongIds())
    val favoriteSongIds: StateFlow<List<Long>> = _favoriteSongIds.asStateFlow()

    private val _playlists = MutableStateFlow(assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value))
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        preferences.edit {
            putString(KEY_THEME_MODE, themeMode.name)
        }
        _themeMode.value = themeMode
    }

    fun setTextSizePreset(textSizePreset: TextSizePreset) {
        preferences.edit {
            putString(KEY_TEXT_SIZE_PRESET, textSizePreset.name)
        }
        _textSizePreset.value = textSizePreset
    }

    fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        val updated = buildList {
            add(entry)
            _searchHistory.value.asSequence()
                .filter { it.key != entry.key }
                .take(MAX_SEARCH_HISTORY - 1)
                .forEach(::add)
        }
        preferences.edit {
            putString(KEY_SEARCH_HISTORY, updated.joinToString(RECORD_SEPARATOR) { it.serialize() })
        }
        _searchHistory.value = updated
    }

    fun clearSearchHistory() {
        preferences.edit {
            remove(KEY_SEARCH_HISTORY)
        }
        _searchHistory.value = emptyList()
    }

    fun incrementAlbumPlayCount(albumId: Long) {
        if (albumId <= 0L) return
        val updated = _albumPlayCounts.value.toMutableMap().apply {
            this[albumId] = (this[albumId] ?: 0) + 1
        }.toMap()
        persistAlbumPlayCounts(updated)
    }

    fun incrementSongPlayCount(songId: Long) {
        if (songId <= 0L) return
        val updated = _songPlayCounts.value.toMutableMap().apply {
            this[songId] = (this[songId] ?: 0) + 1
        }.toMap()
        persistSongPlayCounts(updated)
    }

    fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
    ) {
        val normalizedSongIds = songIds
            .filter { it > 0L }
            .distinct()
            .take(MAX_RECENT_PLAYBACK_IDS)
        val normalizedAlbumIds = albumIds
            .filter { it > 0L }
            .distinct()
            .take(MAX_RECENT_PLAYBACK_IDS)
        preferences.edit {
            putString(KEY_RECENT_SONG_IDS, normalizedSongIds.joinToString(","))
            putString(KEY_RECENT_ALBUM_IDS, normalizedAlbumIds.joinToString(","))
        }
        _recentSongIds.value = normalizedSongIds
        _recentAlbumIds.value = normalizedAlbumIds
    }

    fun createPlaylist(name: String): Long {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return -1L
        val playlist = Playlist(
            id = System.currentTimeMillis(),
            name = trimmedName,
        )
        persistPlaylists(listOf(playlist) + _userPlaylists.value)
        return playlist.id
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        val updated = _userPlaylists.value.map { playlist ->
            if (playlist.id != playlistId) {
                playlist
            } else {
                playlist.copy(songIds = (playlist.songIds + songIds).distinct())
            }
        }
        persistPlaylists(updated)
    }

    fun renamePlaylist(
        playlistId: Long,
        name: String,
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val updated = _userPlaylists.value.map { playlist ->
            if (playlist.id != playlistId) playlist else playlist.copy(name = trimmedName)
        }
        persistPlaylists(updated)
    }

    fun deletePlaylists(playlistIds: Set<Long>) {
        if (playlistIds.isEmpty()) return
        val updated = _userPlaylists.value.filterNot { it.id in playlistIds }
        persistPlaylists(updated)
    }

    fun toggleFavoriteSong(songId: Long) {
        val updated = if (songId in _favoriteSongIds.value) {
            _favoriteSongIds.value.filterNot { it == songId }
        } else {
            _favoriteSongIds.value + songId
        }
        persistFavoriteSongIds(updated)
    }

    fun setFavoriteSongs(
        songIds: List<Long>,
        favorite: Boolean,
    ) {
        if (songIds.isEmpty()) return
        val updated = if (favorite) {
            (_favoriteSongIds.value + songIds).distinct()
        } else {
            _favoriteSongIds.value.filterNot { it in songIds.toSet() }
        }
        persistFavoriteSongIds(updated)
    }

    fun removeSongReferences(songId: Long) {
        val updatedPlaylists = _userPlaylists.value.map { playlist ->
            playlist.copy(songIds = playlist.songIds.filterNot { it == songId })
        }
        val updatedFavorites = _favoriteSongIds.value.filterNot { it == songId }
        persistPlaylists(updatedPlaylists)
        persistFavoriteSongIds(updatedFavorites)
    }

    fun updateBand(index: Int, value: Float) {
        if (index !in 0 until BAND_COUNT) return

        val updatedBands = _eqSettings.value.bands.toMutableList().apply {
            set(index, value.coerceIn(-1f, 1f))
        }
        persistEqSettings(_eqSettings.value.copy(bands = updatedBands))
    }

    fun updateBass(value: Float) {
        persistEqSettings(_eqSettings.value.copy(bass = value.coerceIn(-1f, 1f)))
    }

    fun updateTreble(value: Float) {
        persistEqSettings(_eqSettings.value.copy(treble = value.coerceIn(-1f, 1f)))
    }

    fun updateSpaciousness(value: Float) {
        persistEqSettings(_eqSettings.value.copy(spaciousness = value.coerceIn(-1f, 1f)))
    }

    fun updateSpaciousnessMode(mode: SpaciousnessMode) {
        persistEqSettings(_eqSettings.value.copy(spaciousnessMode = mode))
    }

    fun setEqSettings(settings: EqSettings) {
        val normalizedBands = List(BAND_COUNT) { index ->
            settings.bands.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
        }
        persistEqSettings(
            settings.copy(
                bands = normalizedBands,
                bass = settings.bass.coerceIn(-1f, 1f),
                treble = settings.treble.coerceIn(-1f, 1f),
                spaciousness = settings.spaciousness.coerceIn(-1f, 1f),
                spaciousnessMode = settings.spaciousnessMode,
            ),
        )
    }

    fun resetEqSettings() {
        persistEqSettings(EqSettings())
    }

    fun setPlaybackVolume(value: Float) {
        val volume = value.coerceIn(0f, 1f)
        preferences.edit {
            putFloat(KEY_PLAYBACK_VOLUME, volume)
        }
        _playbackVolume.value = volume
    }

    fun setLibraryFolder(
        uri: Uri?,
        path: String,
    ) {
        val normalizedPath = path.trim()
        preferences.edit {
            if (uri != null) {
                putString(KEY_LIBRARY_FOLDER_URI, uri.toString())
            } else {
                remove(KEY_LIBRARY_FOLDER_URI)
            }
            putString(KEY_LIBRARY_FOLDER_PATH, normalizedPath)
        }
        _libraryFolderUri.value = uri
        _libraryFolderPath.value = normalizedPath
    }

    fun setDismissedUpdateVersion(versionName: String?) {
        preferences.edit {
            if (versionName.isNullOrBlank()) {
                remove(KEY_DISMISSED_UPDATE_VERSION)
            } else {
                putString(KEY_DISMISSED_UPDATE_VERSION, versionName.trim())
            }
        }
        _dismissedUpdateVersion.value = versionName?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun persistEqSettings(settings: EqSettings) {
        preferences.edit {
            putString(KEY_BANDS, settings.bands.joinToString(","))
            putFloat(KEY_BASS, settings.bass)
            putFloat(KEY_TREBLE, settings.treble)
            putFloat(KEY_SPACIOUSNESS, settings.spaciousness)
            putString(KEY_SPACIOUSNESS_MODE, settings.spaciousnessMode.name)
        }
        _eqSettings.value = settings
    }

    private fun loadThemeMode(): ThemeMode {
        return preferences.getString(KEY_THEME_MODE, ThemeMode.System.name)
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.System
    }

    private fun loadEqSettings(): EqSettings {
        val parsedBands = preferences.getString(KEY_BANDS, null)
            ?.split(",")
            ?.mapNotNull { it.toFloatOrNull() }
            .orEmpty()
        val bands = List(BAND_COUNT) { index -> parsedBands.getOrNull(index) ?: 0f }
        return EqSettings(
            bands = bands,
            bass = preferences.getFloat(KEY_BASS, 0f),
            treble = preferences.getFloat(KEY_TREBLE, 0f),
            spaciousness = preferences.getFloat(KEY_SPACIOUSNESS, 0f),
            spaciousnessMode = preferences.getString(KEY_SPACIOUSNESS_MODE, SpaciousnessMode.StereoWidth.name)
                ?.let { saved -> SpaciousnessMode.entries.firstOrNull { it.name == saved } }
                ?: SpaciousnessMode.StereoWidth,
        )
    }

    private fun loadTextSizePreset(): TextSizePreset {
        return preferences.getString(KEY_TEXT_SIZE_PRESET, TextSizePreset.Default.name)
            ?.let { saved -> TextSizePreset.entries.firstOrNull { it.name == saved } }
            ?: TextSizePreset.Default
    }

    private fun loadPlaybackVolume(): Float {
        return preferences.getFloat(KEY_PLAYBACK_VOLUME, 1f).coerceIn(0f, 1f)
    }

    private fun loadLibraryFolderUri(): Uri? {
        return preferences.getString(KEY_LIBRARY_FOLDER_URI, null)
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
    }

    private fun loadLibraryFolderPath(): String {
        return preferences.getString(KEY_LIBRARY_FOLDER_PATH, null).orEmpty()
    }

    private fun loadDismissedUpdateVersion(): String? {
        return preferences.getString(KEY_DISMISSED_UPDATE_VERSION, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun loadSearchHistory(): List<SearchHistoryEntry> {
        return preferences.getString(KEY_SEARCH_HISTORY, null)
            ?.split(RECORD_SEPARATOR)
            ?.mapNotNull { it.deserializeSearchHistoryEntry() }
            .orEmpty()
    }

    private fun loadPlaylists(): List<Playlist> {
        return preferences.getString(KEY_PLAYLISTS, null)
            ?.split(RECORD_SEPARATOR)
            ?.mapNotNull { it.deserializePlaylist() }
            .orEmpty()
    }

    private fun loadFavoriteSongIds(): List<Long> {
        return preferences.getString(KEY_FAVORITE_SONG_IDS, null)
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            .orEmpty()
    }

    private fun loadAlbumPlayCounts(): Map<Long, Int> {
        return preferences.getString(KEY_ALBUM_PLAY_COUNTS, null)
            ?.takeIf { it.isNotBlank() }
            ?.deserializePlayCounts()
            .orEmpty()
    }

    private fun loadSongPlayCounts(): Map<Long, Int> {
        return preferences.getString(KEY_SONG_PLAY_COUNTS, null)
            ?.takeIf { it.isNotBlank() }
            ?.deserializePlayCounts()
            .orEmpty()
    }

    private fun loadRecentSongIds(): List<Long> {
        return preferences.getString(KEY_RECENT_SONG_IDS, null)
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            .orEmpty()
    }

    private fun loadRecentAlbumIds(): List<Long> {
        return preferences.getString(KEY_RECENT_ALBUM_IDS, null)
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            .orEmpty()
    }

    private fun SearchHistoryEntry.serialize(): String {
        return listOf(
            key,
            kind.name,
            title,
            subtitle,
            artUri?.toString().orEmpty(),
            albumId?.toString().orEmpty(),
            query.orEmpty(),
        ).joinToString(FIELD_SEPARATOR)
    }

    private fun String.deserializeSearchHistoryEntry(): SearchHistoryEntry? {
        val parts = split(FIELD_SEPARATOR)
        if (parts.size < 7) return null
        val kind = SearchHistoryKind.entries.firstOrNull { it.name == parts[1] } ?: return null
        return SearchHistoryEntry(
            key = parts[0],
            kind = kind,
            title = parts[2],
            subtitle = parts[3],
            artUri = parts[4].takeIf { it.isNotBlank() }?.let(Uri::parse),
            albumId = parts[5].toLongOrNull(),
            query = parts[6].takeIf { it.isNotBlank() },
        )
    }

    private fun persistPlaylists(playlists: List<Playlist>) {
        preferences.edit {
            putString(KEY_PLAYLISTS, playlists.joinToString(RECORD_SEPARATOR) { it.serialize() })
        }
        _userPlaylists.value = playlists
        _playlists.value = assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value)
    }

    private fun persistFavoriteSongIds(songIds: List<Long>) {
        preferences.edit {
            putString(KEY_FAVORITE_SONG_IDS, songIds.joinToString(","))
        }
        _favoriteSongIds.value = songIds
        _playlists.value = assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value)
    }

    private fun persistAlbumPlayCounts(counts: Map<Long, Int>) {
        preferences.edit {
            putString(
                KEY_ALBUM_PLAY_COUNTS,
                counts.serializePlayCounts(),
            )
        }
        _albumPlayCounts.value = counts
    }

    private fun persistSongPlayCounts(counts: Map<Long, Int>) {
        preferences.edit {
            putString(
                KEY_SONG_PLAY_COUNTS,
                counts.serializePlayCounts(),
            )
        }
        _songPlayCounts.value = counts
    }

    private fun Map<Long, Int>.serializePlayCounts(): String {
        return entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    private fun String.deserializePlayCounts(): Map<Long, Int> {
        return split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                val id = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
                val count = parts.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0) ?: return@mapNotNull null
                id to count
            }
            .toMap()
    }

    private fun Playlist.serialize(): String {
        return listOf(
            id.toString(),
            name,
            songIds.joinToString(","),
            isSystem.toString(),
        ).joinToString(FIELD_SEPARATOR)
    }

    private fun String.deserializePlaylist(): Playlist? {
        val parts = split(FIELD_SEPARATOR)
        if (parts.size < 3) return null
        return Playlist(
            id = parts[0].toLongOrNull() ?: return null,
            name = parts[1],
            songIds = parts[2]
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                .orEmpty(),
            isSystem = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
        )
    }

    private fun assemblePlaylists(
        userPlaylists: List<Playlist>,
        favoriteSongIds: List<Long>,
    ): List<Playlist> {
        return userPlaylists
    }

    private companion object {
        const val BAND_COUNT = 24
        const val MAX_SEARCH_HISTORY = 6
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_TEXT_SIZE_PRESET = "text_size_preset"
        const val KEY_SEARCH_HISTORY = "search_history"
        const val KEY_PLAYLISTS = "playlists"
        const val KEY_FAVORITE_SONG_IDS = "favorite_song_ids"
        const val KEY_ALBUM_PLAY_COUNTS = "album_play_counts"
        const val KEY_SONG_PLAY_COUNTS = "song_play_counts"
        const val KEY_RECENT_SONG_IDS = "recent_song_ids"
        const val KEY_RECENT_ALBUM_IDS = "recent_album_ids"
        const val KEY_PLAYBACK_VOLUME = "playback_volume"
        const val KEY_LIBRARY_FOLDER_URI = "library_folder_uri"
        const val KEY_LIBRARY_FOLDER_PATH = "library_folder_path"
        const val KEY_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
        const val KEY_BANDS = "eq_bands"
        const val KEY_BASS = "eq_bass"
        const val KEY_TREBLE = "eq_treble"
        const val KEY_SPACIOUSNESS = "eq_spaciousness"
        const val KEY_SPACIOUSNESS_MODE = "eq_spaciousness_mode"
        const val MAX_RECENT_PLAYBACK_IDS = 24
        const val RECORD_SEPARATOR = "\u001E"
        const val FIELD_SEPARATOR = "\u001F"
    }
}
