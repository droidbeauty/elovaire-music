package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import elovaire.music.droidbeauty.app.data.playlists.addSongsToPlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.createPlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.deletePlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.deserializePlaylists
import elovaire.music.droidbeauty.app.data.playlists.removeSongReferencesFromPlaylists
import elovaire.music.droidbeauty.app.data.playlists.renamePlaylistEntry
import elovaire.music.droidbeauty.app.data.playlists.serializePlaylists
import elovaire.music.droidbeauty.app.data.playlists.updatePlaylistSongIdsEntry
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistDefaults
import elovaire.music.droidbeauty.app.data.smartplaylists.createSmartPlaylistEntry
import elovaire.music.droidbeauty.app.data.smartplaylists.deleteSmartPlaylistEntries
import elovaire.music.droidbeauty.app.data.smartplaylists.deserializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.serializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.updateSmartPlaylistEntry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.data.playback.normalizeReverbDurationMs
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskWrites
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferenceStore(context: Context) :
    RootSettingsReader,
    AppearanceSettingsWriter,
    LibrarySettingsWriter,
    PlaybackSettingsWriter,
    PlaylistStore,
    FavoritesStore,
    UpdatePreferencesStore {
    private val appContext = context.applicationContext
    private val preferences = allowStrictModeDiskWrites {
        // SharedPreferences may create its private directory during first startup.
        // Initial settings must be available synchronously before the first UI state is published.
        PreferenceStorage(appContext).preferences.also { it.all }
    }
    private val updatePreferencesStore = UpdatePreferencesStoreImpl(preferences)
    private val preferenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playbackHistoryPersistJob: Job? = null
    private var eqPersistJob: Job? = null
    private var pendingEqSettings: EqSettings? = null

    private val _themeMode = MutableStateFlow(loadThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _textSizePreset = MutableStateFlow(loadTextSizePreset())
    override val textSizePreset: StateFlow<TextSizePreset> = _textSizePreset.asStateFlow()

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    override val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _eqSettings = MutableStateFlow(loadEqSettings())
    override val eqSettings: StateFlow<EqSettings> = _eqSettings.asStateFlow()

    private val _playbackVolume = MutableStateFlow(loadPlaybackVolume())
    val playbackVolume: StateFlow<Float> = _playbackVolume.asStateFlow()

    private val _gaplessPlaybackEnabled = MutableStateFlow(loadGaplessPlaybackEnabled())
    val gaplessPlaybackEnabled: StateFlow<Boolean> = _gaplessPlaybackEnabled.asStateFlow()

    private val _volumeNormalizationEnabled = MutableStateFlow(loadVolumeNormalizationEnabled())
    override val volumeNormalizationEnabled: StateFlow<Boolean> = _volumeNormalizationEnabled.asStateFlow()

    private val _onlineLyricsLookupEnabled = MutableStateFlow(loadOnlineLyricsLookupEnabled())
    override val onlineLyricsLookupEnabled: StateFlow<Boolean> = _onlineLyricsLookupEnabled.asStateFlow()

    private val _albumCollectionLayoutMode = MutableStateFlow(loadAlbumCollectionLayoutMode())
    override val albumCollectionLayoutMode: StateFlow<String> = _albumCollectionLayoutMode.asStateFlow()

    private val _songCollectionGridEnabled = MutableStateFlow(loadSongCollectionGridEnabled())
    override val songCollectionGridEnabled: StateFlow<Boolean> = _songCollectionGridEnabled.asStateFlow()

    private val _albumCollectionSortMode = MutableStateFlow(loadAlbumCollectionSortMode())
    override val albumCollectionSortMode: StateFlow<String> = _albumCollectionSortMode.asStateFlow()

    private val _songCollectionSortMode = MutableStateFlow(loadSongCollectionSortMode())
    override val songCollectionSortMode: StateFlow<String> = _songCollectionSortMode.asStateFlow()

    private val _libraryFolders = MutableStateFlow(loadLibraryFolders())
    override val libraryFolders: StateFlow<List<LibraryFolderSelection>> = _libraryFolders.asStateFlow()
    override val dismissedUpdateVersion: StateFlow<String?> = updatePreferencesStore.dismissedUpdateVersion

    private val _searchHistory = MutableStateFlow(loadSearchHistory())
    val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()
    private val _albumPlayCounts = MutableStateFlow(loadAlbumPlayCounts())
    override val albumPlayCounts: StateFlow<Map<Long, Int>> = _albumPlayCounts.asStateFlow()
    private val _songPlayCounts = MutableStateFlow(loadSongPlayCounts())
    override val songPlayCounts: StateFlow<Map<Long, Int>> = _songPlayCounts.asStateFlow()
    private val _recentSongIds = MutableStateFlow(loadRecentSongIds())
    override val recentSongIds: StateFlow<List<Long>> = _recentSongIds.asStateFlow()
    private val _recentAlbumIds = MutableStateFlow(loadRecentAlbumIds())
    override val recentAlbumIds: StateFlow<List<Long>> = _recentAlbumIds.asStateFlow()
    private val _lastPlayedCollectionKind = MutableStateFlow(loadLastPlayedCollectionKind())
    override val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?> = _lastPlayedCollectionKind.asStateFlow()
    private val _lastPlayedCollectionId = MutableStateFlow(loadLastPlayedCollectionId())
    override val lastPlayedCollectionId: StateFlow<Long?> = _lastPlayedCollectionId.asStateFlow()

    private val _userPlaylists = MutableStateFlow(loadPlaylists())
    private var nextPlaylistId = loadNextPlaylistId(_userPlaylists.value)
    private val _userSmartPlaylists = MutableStateFlow(loadSmartPlaylists())
    private var nextSmartPlaylistId = loadNextSmartPlaylistId(_userSmartPlaylists.value)
    private val _smartPlaylists = MutableStateFlow(assembleSmartPlaylists(_userSmartPlaylists.value))
    override val smartPlaylists: StateFlow<List<SmartPlaylist>> = _smartPlaylists.asStateFlow()
    private val _favoriteSongIds = MutableStateFlow(loadFavoriteSongIds())
    override val favoriteSongIds: StateFlow<List<Long>> = _favoriteSongIds.asStateFlow()

    private val _playlists = MutableStateFlow(assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value))
    override val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    override fun setThemeMode(themeMode: ThemeMode) {
        if (_themeMode.value == themeMode) return
        preferences.edit {
            putString(KEY_THEME_MODE, themeMode.name)
        }
        _themeMode.value = themeMode
    }

    override fun setTextSizePreset(textSizePreset: TextSizePreset) {
        if (_textSizePreset.value == textSizePreset) return
        preferences.edit {
            putString(KEY_TEXT_SIZE_PRESET, textSizePreset.name)
        }
        _textSizePreset.value = textSizePreset
    }

    override fun setAppLanguage(language: AppLanguage) {
        if (_appLanguage.value == language) return
        preferences.edit {
            putString(KEY_APP_LANGUAGE, language.name)
        }
        _appLanguage.value = language
    }

    fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        val normalizedEntry = entry.copy(
            key = entry.key.trim(),
            title = entry.title.trim(),
            subtitle = entry.subtitle.trim(),
            query = entry.query?.trim()?.takeIf { it.isNotBlank() },
        )
        if (normalizedEntry.key.isBlank() || normalizedEntry.title.isBlank()) return
        val updated = buildList {
            add(normalizedEntry)
            _searchHistory.value.asSequence()
                .filter { it.key != normalizedEntry.key }
                .take(MAX_SEARCH_HISTORY - 1)
                .forEach(::add)
        }
        if (_searchHistory.value == updated) return
        preferences.edit {
            putString(KEY_SEARCH_HISTORY, updated.joinToString(RECORD_SEPARATOR) { it.serialize() })
        }
        _searchHistory.value = updated
    }

    fun clearSearchHistory() {
        if (_searchHistory.value.isEmpty()) return
        preferences.edit {
            remove(KEY_SEARCH_HISTORY)
        }
        _searchHistory.value = emptyList()
    }

    fun recordPlaybackTransition(
        songId: Long?,
        albumId: Long?,
    ) {
        var changed = false
        if (songId != null && songId != 0L) {
            _songPlayCounts.value = _songPlayCounts.value.toMutableMap().apply {
                this[songId] = (this[songId] ?: 0) + 1
            }.toMap()
            changed = true
        }
        if (albumId != null && albumId != 0L) {
            _albumPlayCounts.value = _albumPlayCounts.value.toMutableMap().apply {
                this[albumId] = (this[albumId] ?: 0) + 1
            }.toMap()
            changed = true
        }
        if (changed) {
            schedulePlaybackHistoryPersistence()
        }
    }

    fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) {
        val normalizedSongIds = songIds
            .filter { it != 0L }
            .distinct()
            .take(MAX_RECENT_PLAYBACK_IDS)
        val normalizedAlbumIds = albumIds
            .filter { it != 0L }
            .distinct()
            .take(MAX_RECENT_PLAYBACK_IDS)
        val normalizedCollectionId = lastPlayedCollectionId?.takeIf { it != 0L }
        if (
            _recentSongIds.value == normalizedSongIds &&
            _recentAlbumIds.value == normalizedAlbumIds &&
            _lastPlayedCollectionKind.value == lastPlayedCollectionKind &&
            _lastPlayedCollectionId.value == normalizedCollectionId
        ) {
            return
        }
        _recentSongIds.value = normalizedSongIds
        _recentAlbumIds.value = normalizedAlbumIds
        _lastPlayedCollectionKind.value = lastPlayedCollectionKind
        _lastPlayedCollectionId.value = normalizedCollectionId
        schedulePlaybackHistoryPersistence()
    }

    override fun createPlaylist(name: String): Long {
        val result = createPlaylistEntries(
            playlists = _userPlaylists.value,
            name = name,
            nextPlaylistId = nextPlaylistId,
        ) ?: return -1L
        nextPlaylistId = result.nextPlaylistId
        persistPlaylists(result.playlists, nextPlaylistId = result.nextPlaylistId)
        return result.createdPlaylist.id
    }

    override fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val updated = addSongsToPlaylistEntries(_userPlaylists.value, playlistId, songIds) ?: return
        persistPlaylists(updated)
    }

    override fun renamePlaylist(
        playlistId: Long,
        name: String,
    ) {
        val updated = renamePlaylistEntry(_userPlaylists.value, playlistId, name) ?: return
        persistPlaylists(updated)
    }

    override fun updatePlaylistSongIds(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        val updated = updatePlaylistSongIdsEntry(_userPlaylists.value, playlistId, songIds) ?: return
        persistPlaylists(updated)
    }

    override fun deletePlaylists(playlistIds: Set<Long>) {
        val updated = deletePlaylistEntries(_userPlaylists.value, playlistIds) ?: return
        persistPlaylists(updated)
    }

    override fun createSmartPlaylist(name: String): Long {
        val result = createSmartPlaylistEntry(
            playlists = _userSmartPlaylists.value,
            name = name,
            nextSmartPlaylistId = nextSmartPlaylistId,
            nowMs = System.currentTimeMillis(),
        ) ?: return -1L
        nextSmartPlaylistId = result.nextSmartPlaylistId
        persistSmartPlaylists(result.playlists, nextSmartPlaylistId = result.nextSmartPlaylistId)
        return result.createdPlaylist.id
    }

    override fun updateSmartPlaylist(playlist: SmartPlaylist) {
        val updated = updateSmartPlaylistEntry(
            playlists = _userSmartPlaylists.value,
            playlist = playlist,
            nowMs = System.currentTimeMillis(),
        ) ?: return
        persistSmartPlaylists(updated)
    }

    override fun deleteSmartPlaylists(playlistIds: Set<Long>) {
        val updated = deleteSmartPlaylistEntries(_userSmartPlaylists.value, playlistIds) ?: return
        persistSmartPlaylists(updated)
    }

    override fun toggleFavoriteSong(songId: Long) {
        val updated = if (songId in _favoriteSongIds.value) {
            _favoriteSongIds.value.filterNot { it == songId }
        } else {
            _favoriteSongIds.value + songId
        }
        persistFavoriteSongIds(updated)
    }

    override fun setFavoriteSongs(
        songIds: List<Long>,
        favorite: Boolean,
    ) {
        if (songIds.isEmpty()) return
        val songIdSet = songIds.toSet()
        val updated = if (favorite) {
            (_favoriteSongIds.value + songIds).distinct()
        } else {
            _favoriteSongIds.value.filterNot { it in songIdSet }
        }
        if (_favoriteSongIds.value == updated) return
        persistFavoriteSongIds(updated)
    }

    override fun removeSongReferences(songId: Long) {
        val updatedPlaylists = removeSongReferencesFromPlaylists(_userPlaylists.value, songId) ?: _userPlaylists.value
        val updatedFavorites = _favoriteSongIds.value.filterNot { it == songId }
        if (_userPlaylists.value == updatedPlaylists && _favoriteSongIds.value == updatedFavorites) return
        persistPlaylistAndFavorites(
            playlists = updatedPlaylists,
            favoriteSongIds = updatedFavorites,
        )
    }

    fun updateBand(index: Int, value: Float) {
        if (index !in 0 until BAND_COUNT) return

        val updatedBands = _eqSettings.value.bands.toMutableList().apply {
            set(index, EqValuePolicy.clampBandNormalized(value))
        }
        persistEqSettings(_eqSettings.value.copy(bands = updatedBands), immediate = false)
    }

    override fun updateBass(value: Float) {
        persistEqSettings(_eqSettings.value.copy(bass = EqValuePolicy.clampPositiveMacro(value)), immediate = false)
    }

    override fun updateMidrange(value: Float) {
        persistEqSettings(_eqSettings.value.copy(midrange = EqValuePolicy.clampMacro(value)), immediate = false)
    }

    override fun updateTreble(value: Float) {
        persistEqSettings(_eqSettings.value.copy(treble = EqValuePolicy.clampMacro(value)), immediate = false)
    }

    fun updateSpaciousness(value: Float) {
        persistEqSettings(_eqSettings.value.copy(spaciousness = EqValuePolicy.clampPositiveMacro(value)), immediate = false)
    }

    fun updateSpaciousnessMode(mode: SpaciousnessMode) {
        val current = _eqSettings.value
        val normalizedMode = if (mode == SpaciousnessMode.Off) {
            SpaciousnessMode.Off
        } else {
            mode
        }
        val nextSettings = when {
            normalizedMode == SpaciousnessMode.Off -> {
                current.copy(
                    spaciousnessMode = SpaciousnessMode.Off,
                    spaciousness = 0f,
                )
            }
            current.spaciousnessMode == normalizedMode && current.spaciousness > 0.001f -> {
                current.copy(
                    spaciousnessMode = SpaciousnessMode.Off,
                    spaciousness = 0f,
                )
            }
            else -> {
                current.copy(
                    spaciousnessMode = normalizedMode,
                    spaciousness = 0.5f,
                )
            }
        }
        persistEqSettings(nextSettings)
    }

    fun updateReverbDurationMs(valueMs: Int) {
        persistEqSettings(
            _eqSettings.value.copy(
                reverbDurationMs = normalizeReverbDurationMs(valueMs),
            ),
        )
    }

    fun updateReverbProfile(profile: ReverbProfile) {
        persistEqSettings(_eqSettings.value.copy(reverbProfile = profile))
    }

    override fun updateMonoPlaybackEnabled(enabled: Boolean) {
        persistEqSettings(_eqSettings.value.copy(monoEnabled = enabled))
    }

    fun setEqSettings(settings: EqSettings) {
        persistEqSettings(EqValuePolicy.sanitize(settings))
    }

    fun resetEqSettings() {
        persistEqSettings(EqSettings())
    }

    override fun setPlaybackVolume(value: Float) {
        val volume = value.coerceIn(0f, 1f)
        if (_playbackVolume.value == volume) return
        preferences.edit {
            putFloat(KEY_PLAYBACK_VOLUME, volume)
        }
        _playbackVolume.value = volume
    }

    override fun setGaplessPlaybackEnabled(enabled: Boolean) {
        if (_gaplessPlaybackEnabled.value == enabled) return
        preferences.edit {
            putBoolean(KEY_GAPLESS_PLAYBACK_ENABLED, enabled)
        }
        _gaplessPlaybackEnabled.value = enabled
    }

    override fun setVolumeNormalizationEnabled(enabled: Boolean) {
        if (_volumeNormalizationEnabled.value == enabled) return
        preferences.edit {
            putBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, enabled)
        }
        _volumeNormalizationEnabled.value = enabled
    }

    fun setOnlineLyricsLookupEnabled(enabled: Boolean) {
        if (_onlineLyricsLookupEnabled.value == enabled) return
        preferences.edit {
            putBoolean(KEY_ONLINE_LYRICS_LOOKUP_ENABLED, enabled)
        }
        _onlineLyricsLookupEnabled.value = enabled
    }

    override fun setAlbumCollectionLayoutMode(mode: String) {
        val normalizedMode = mode.trim().ifBlank { DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE }
        if (_albumCollectionLayoutMode.value == normalizedMode) return
        preferences.edit {
            putString(KEY_ALBUM_COLLECTION_LAYOUT_MODE, normalizedMode)
        }
        _albumCollectionLayoutMode.value = normalizedMode
    }

    override fun setSongCollectionGridEnabled(enabled: Boolean) {
        if (_songCollectionGridEnabled.value == enabled) return
        preferences.edit {
            putBoolean(KEY_SONG_COLLECTION_GRID_ENABLED, enabled)
        }
        _songCollectionGridEnabled.value = enabled
    }

    override fun setAlbumCollectionSortMode(sortMode: String) {
        val normalizedSortMode = sortMode.trim().ifBlank { DEFAULT_ALBUM_COLLECTION_SORT_MODE }
        if (_albumCollectionSortMode.value == normalizedSortMode) return
        preferences.edit {
            putString(KEY_ALBUM_COLLECTION_SORT_MODE, normalizedSortMode)
        }
        _albumCollectionSortMode.value = normalizedSortMode
    }

    override fun setSongCollectionSortMode(sortMode: String) {
        val normalizedSortMode = sortMode.trim().ifBlank { DEFAULT_SONG_COLLECTION_SORT_MODE }
        if (_songCollectionSortMode.value == normalizedSortMode) return
        preferences.edit {
            putString(KEY_SONG_COLLECTION_SORT_MODE, normalizedSortMode)
        }
        _songCollectionSortMode.value = normalizedSortMode
    }

    override fun addLibraryFolder(selection: LibraryFolderSelection) {
        setLibraryFolders(_libraryFolders.value + selection)
    }

    override fun removeLibraryFolder(selection: LibraryFolderSelection) {
        val targetUri = selection.uri?.toString()
        val targetPath = LibraryFolderSelectionResolver.normalizedPathKey(selection.path)
        setLibraryFolders(
            _libraryFolders.value.filterNot { current ->
                current.uri?.toString() == targetUri &&
                    LibraryFolderSelectionResolver.normalizedPathKey(current.path) == targetPath
            },
        )
    }

    override fun setLibraryFolders(selections: List<LibraryFolderSelection>) {
        val normalized = LibraryFolderSelectionResolver.normalize(selections)
        if (_libraryFolders.value == normalized) return
        preferences.edit {
            putString(KEY_LIBRARY_FOLDERS, normalized.joinToString(RECORD_SEPARATOR) { it.serialize() })
        }
        _libraryFolders.value = normalized
    }

    override fun restoreDefaultLibraryFolderIfEmpty() {
        if (_libraryFolders.value.isNotEmpty()) return
        setLibraryFolders(listOf(LibraryFolderSelectionResolver.defaultMusicFolder()))
    }

    override fun setDismissedUpdateVersion(versionName: String?) {
        updatePreferencesStore.setDismissedUpdateVersion(versionName)
    }

    override fun lastAutomaticUpdateCheckAtMs(): Long {
        return updatePreferencesStore.lastAutomaticUpdateCheckAtMs()
    }

    override fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long) {
        updatePreferencesStore.setLastAutomaticUpdateCheckAtMs(timestampMs)
    }

    fun release() {
        flushPlaybackHistoryPersistence(commit = true)
        flushEqSettingsPersistence(commit = true)
        preferenceScope.cancel()
    }

    private fun persistEqSettings(
        settings: EqSettings,
        immediate: Boolean = true,
    ) {
        val normalizedSettings = EqValuePolicy.sanitize(settings)
        _eqSettings.value = normalizedSettings
        if (immediate) {
            flushEqSettingsPersistence(commit = false)
            writeEqSettings(normalizedSettings, commit = false)
        } else {
            pendingEqSettings = normalizedSettings
            scheduleEqSettingsPersistence()
        }
    }

    private fun schedulePlaybackHistoryPersistence() {
        playbackHistoryPersistJob?.cancel()
        playbackHistoryPersistJob = preferenceScope.launch {
            delay(PLAYBACK_HISTORY_PERSIST_DEBOUNCE_MS)
            flushPlaybackHistoryPersistence(commit = false)
        }
    }

    private fun flushPlaybackHistoryPersistence(commit: Boolean) {
        playbackHistoryPersistJob?.cancel()
        playbackHistoryPersistJob = null
        preferences.edit(commit = commit) {
            putString(KEY_ALBUM_PLAY_COUNTS, _albumPlayCounts.value.serializePlayCounts())
            putString(KEY_SONG_PLAY_COUNTS, _songPlayCounts.value.serializePlayCounts())
            putString(KEY_RECENT_SONG_IDS, _recentSongIds.value.joinToString(","))
            putString(KEY_RECENT_ALBUM_IDS, _recentAlbumIds.value.joinToString(","))
            putString(KEY_LAST_PLAYED_COLLECTION_KIND, _lastPlayedCollectionKind.value?.name)
            val lastPlayedCollectionId = _lastPlayedCollectionId.value
            if (lastPlayedCollectionId != null && lastPlayedCollectionId > 0L) {
                putLong(KEY_LAST_PLAYED_COLLECTION_ID, lastPlayedCollectionId)
            } else {
                remove(KEY_LAST_PLAYED_COLLECTION_ID)
            }
        }
    }

    private fun scheduleEqSettingsPersistence() {
        eqPersistJob?.cancel()
        eqPersistJob = preferenceScope.launch {
            delay(EQ_SETTINGS_PERSIST_DEBOUNCE_MS)
            flushEqSettingsPersistence(commit = false)
        }
    }

    private fun flushEqSettingsPersistence(commit: Boolean) {
        eqPersistJob?.cancel()
        eqPersistJob = null
        val settings = pendingEqSettings ?: return
        pendingEqSettings = null
        writeEqSettings(settings, commit = commit)
    }

    private fun writeEqSettings(
        settings: EqSettings,
        commit: Boolean,
    ) {
        preferences.edit(commit = commit) {
            putString(KEY_BANDS, settings.bands.joinToString(","))
            putFloat(KEY_BASS, settings.bass)
            putFloat(KEY_MIDRANGE, settings.midrange)
            putFloat(KEY_TREBLE, settings.treble)
            putFloat(KEY_SPACIOUSNESS, settings.spaciousness)
            putString(KEY_SPACIOUSNESS_MODE, settings.spaciousnessMode.name)
            putBoolean(KEY_MONO_ENABLED, settings.monoEnabled)
            putInt(KEY_REVERB_DURATION_MS, settings.reverbDurationMs)
            putString(KEY_REVERB_PROFILE, settings.reverbProfile.name)
        }
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
        return EqValuePolicy.sanitize(EqSettings(
            bands = bands,
            bass = preferences.getFloat(KEY_BASS, 0f),
            midrange = preferences.getFloat(KEY_MIDRANGE, 0f),
            treble = preferences.getFloat(KEY_TREBLE, 0f),
            spaciousness = preferences.getFloat(KEY_SPACIOUSNESS, 0f),
            spaciousnessMode = preferences.getString(KEY_SPACIOUSNESS_MODE, SpaciousnessMode.StereoWidth.name)
                ?.let { saved -> SpaciousnessMode.entries.firstOrNull { it.name == saved } }
                ?: SpaciousnessMode.StereoWidth,
            monoEnabled = preferences.getBoolean(KEY_MONO_ENABLED, false),
            reverbDurationMs = normalizeReverbDurationMs(preferences.getInt(KEY_REVERB_DURATION_MS, 0)),
            reverbProfile = preferences.getString(KEY_REVERB_PROFILE, ReverbProfile.Dry.name)
                ?.let { saved -> ReverbProfile.entries.firstOrNull { it.name == saved } }
                ?: ReverbProfile.Dry,
        ))
    }

    private fun loadTextSizePreset(): TextSizePreset {
        return preferences.getString(KEY_TEXT_SIZE_PRESET, TextSizePreset.Default.name)
            ?.let { saved -> TextSizePreset.entries.firstOrNull { it.name == saved } }
            ?: TextSizePreset.Default
    }

    private fun loadAppLanguage(): AppLanguage {
        val savedLanguage = preferences.getString(KEY_APP_LANGUAGE, null)
            ?.let { saved -> AppLanguage.entries.firstOrNull { it.name == saved } }
        return savedLanguage ?: resolveDeviceLanguage()
    }

    private fun resolveDeviceLanguage(): AppLanguage {
        val locale = appContext.resources.configuration.locales[0] ?: return AppLanguage.English
        return when (locale.language.lowercase()) {
            "sq" -> AppLanguage.Albanian
            "bn" -> AppLanguage.Bengali
            "hr" -> AppLanguage.Croatian
            "cs" -> AppLanguage.Czech
            "da" -> AppLanguage.Danish
            "nl" -> AppLanguage.Dutch
            "et" -> AppLanguage.Estonian
            "fr" -> AppLanguage.French
            "de" -> AppLanguage.German
            "el" -> AppLanguage.Greek
            "hi" -> AppLanguage.Hindi
            "hu" -> AppLanguage.Hungarian
            "it" -> AppLanguage.Italian
            "ja" -> AppLanguage.Japanese
            "ko" -> AppLanguage.Korean
            "la" -> AppLanguage.Latin
            "lv" -> AppLanguage.Latvian
            "lt" -> AppLanguage.Lithuanian
            "ms" -> AppLanguage.Malay
            "mk" -> AppLanguage.Macedonian
            "no", "nb", "nn" -> AppLanguage.Norwegian
            "pl" -> AppLanguage.Polish
            "pt" -> AppLanguage.Portuguese
            "ru" -> AppLanguage.Russian
            "sk" -> AppLanguage.Slovak
            "sr" -> AppLanguage.Serbian
            "zh" -> AppLanguage.ChineseSimplified
            "es" -> AppLanguage.Spanish
            "sv" -> AppLanguage.Swedish
            "th" -> AppLanguage.Thai
            "uk" -> AppLanguage.Ukrainian
            "ur" -> AppLanguage.Urdu
            "en" -> AppLanguage.English
            else -> AppLanguage.English
        }
    }

    private fun loadPlaybackVolume(): Float {
        return preferences.getFloat(KEY_PLAYBACK_VOLUME, 1f).coerceIn(0f, 1f)
    }

    private fun loadGaplessPlaybackEnabled(): Boolean {
        return preferences.getBoolean(KEY_GAPLESS_PLAYBACK_ENABLED, false)
    }

    private fun loadVolumeNormalizationEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, false)
    }

    private fun loadOnlineLyricsLookupEnabled(): Boolean {
        return preferences.getBoolean(KEY_ONLINE_LYRICS_LOOKUP_ENABLED, false)
    }

    private fun loadAlbumCollectionLayoutMode(): String {
        preferences.getString(KEY_ALBUM_COLLECTION_LAYOUT_MODE, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        return if (preferences.getBoolean(KEY_ALBUM_COLLECTION_GRID_ENABLED, true)) {
            "Grid"
        } else {
            "Compact"
        }
    }

    private fun loadSongCollectionGridEnabled(): Boolean {
        return preferences.getBoolean(KEY_SONG_COLLECTION_GRID_ENABLED, false)
    }

    private fun loadAlbumCollectionSortMode(): String {
        return preferences.getString(
            KEY_ALBUM_COLLECTION_SORT_MODE,
            DEFAULT_ALBUM_COLLECTION_SORT_MODE,
        )?.trim().takeUnless { it.isNullOrBlank() } ?: DEFAULT_ALBUM_COLLECTION_SORT_MODE
    }

    private fun loadSongCollectionSortMode(): String {
        return preferences.getString(
            KEY_SONG_COLLECTION_SORT_MODE,
            DEFAULT_SONG_COLLECTION_SORT_MODE,
        )?.trim().takeUnless { it.isNullOrBlank() } ?: DEFAULT_SONG_COLLECTION_SORT_MODE
    }

    private fun loadLibraryFolders(): List<LibraryFolderSelection> {
        val stored = preferences.getString(KEY_LIBRARY_FOLDERS, null)
        if (stored != null) {
            return stored
                .takeIf { it.isNotBlank() }
                ?.split(RECORD_SEPARATOR)
                ?.mapNotNull { it.deserializeLibraryFolderSelection() }
                ?.let(LibraryFolderSelectionResolver::normalize)
                .orEmpty()
        }
        val migratedPath = preferences.getString(KEY_LIBRARY_FOLDER_PATH, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val migratedUri = preferences.getString(KEY_LIBRARY_FOLDER_URI, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
        val migrated = if (migratedPath != null || migratedUri != null) {
            LibraryFolderSelection(
                uri = migratedUri,
                path = migratedPath ?: migratedUri.toString(),
                displayName = migratedPath?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "Library folder",
                isDefaultMusicFolder = false,
            )
        } else {
            LibraryFolderSelectionResolver.defaultMusicFolder()
        }
        val normalized = LibraryFolderSelectionResolver.normalize(listOf(migrated))
        preferences.edit {
            putString(KEY_LIBRARY_FOLDERS, normalized.joinToString(RECORD_SEPARATOR) { it.serialize() })
        }
        return normalized
    }

    private fun loadSearchHistory(): List<SearchHistoryEntry> {
        return preferences.getString(KEY_SEARCH_HISTORY, null)
            ?.split(RECORD_SEPARATOR)
            ?.mapNotNull { it.deserializeSearchHistoryEntry() }
            .orEmpty()
    }

    private fun loadPlaylists(): List<Playlist> {
        return deserializePlaylists(preferences.getString(KEY_PLAYLISTS, null))
    }

    private fun loadSmartPlaylists(): List<SmartPlaylist> {
        return deserializeSmartPlaylists(preferences.getString(KEY_SMART_PLAYLISTS, null))
    }

    private fun loadNextPlaylistId(playlists: List<Playlist>): Long {
        val existingIds = playlists.mapTo(mutableSetOf()) { it.id }
        val persisted = preferences.getLong(KEY_NEXT_PLAYLIST_ID, 0L)
        val baseline = maxOf(
            persisted,
            (existingIds.maxOrNull() ?: 0L) + 1L,
            System.currentTimeMillis().coerceAtLeast(1L),
        )
        var candidate = baseline
        while (candidate in existingIds || candidate <= 0L) {
            candidate = if (candidate == Long.MAX_VALUE) 1L else candidate + 1L
        }
        return candidate
    }

    private fun loadNextSmartPlaylistId(playlists: List<SmartPlaylist>): Long {
        val existingIds = playlists.mapTo(mutableSetOf()) { it.id }
        val persisted = preferences.getLong(KEY_NEXT_SMART_PLAYLIST_ID, 1L)
        var candidate = maxOf(persisted, (existingIds.maxOrNull() ?: 0L) + 1L, 1L)
        while (candidate in existingIds || candidate <= 0L) {
            candidate = if (candidate == Long.MAX_VALUE) 1L else candidate + 1L
        }
        return candidate
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

    private fun loadLastPlayedCollectionKind(): PlaybackCollectionKind? {
        val stored = preferences.getString(KEY_LAST_PLAYED_COLLECTION_KIND, null) ?: return null
        return PlaybackCollectionKind.entries.firstOrNull { it.name == stored }
    }

    private fun loadLastPlayedCollectionId(): Long? {
        return preferences.takeIf { it.contains(KEY_LAST_PLAYED_COLLECTION_ID) }
            ?.getLong(KEY_LAST_PLAYED_COLLECTION_ID, -1L)
            ?.takeIf { it != 0L }
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

    private fun LibraryFolderSelection.serialize(): String {
        return listOf(
            uri?.toString().orEmpty(),
            path,
            displayName,
            isDefaultMusicFolder.toString(),
        ).joinToString(FIELD_SEPARATOR)
    }

    private fun String.deserializeLibraryFolderSelection(): LibraryFolderSelection? {
        val parts = split(FIELD_SEPARATOR)
        if (parts.size < 4) return null
        val path = parts[1].trim()
        val uri = parts[0].takeIf { it.isNotBlank() }?.let(Uri::parse)
        if (path.isBlank() && uri == null) return null
        return LibraryFolderSelection(
            uri = uri,
            path = path.ifBlank { uri.toString() },
            displayName = parts[2].trim().ifBlank { "Library folder" },
            isDefaultMusicFolder = parts[3].toBooleanStrictOrNull() == true,
        )
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

    private fun persistPlaylists(
        playlists: List<Playlist>,
        nextPlaylistId: Long? = null,
    ) {
        preferences.edit {
            putString(KEY_PLAYLISTS, serializePlaylists(playlists))
            nextPlaylistId?.let { putLong(KEY_NEXT_PLAYLIST_ID, it) }
        }
        _userPlaylists.value = playlists
        _playlists.value = assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value)
    }

    private fun persistSmartPlaylists(
        playlists: List<SmartPlaylist>,
        nextSmartPlaylistId: Long? = null,
    ) {
        preferences.edit {
            putString(KEY_SMART_PLAYLISTS, serializeSmartPlaylists(playlists))
            nextSmartPlaylistId?.let { putLong(KEY_NEXT_SMART_PLAYLIST_ID, it) }
        }
        _userSmartPlaylists.value = playlists
        _smartPlaylists.value = assembleSmartPlaylists(playlists)
    }

    private fun persistFavoriteSongIds(songIds: List<Long>) {
        preferences.edit {
            putString(KEY_FAVORITE_SONG_IDS, songIds.joinToString(","))
        }
        _favoriteSongIds.value = songIds
        _playlists.value = assemblePlaylists(_userPlaylists.value, _favoriteSongIds.value)
    }

    private fun persistPlaylistAndFavorites(
        playlists: List<Playlist>,
        favoriteSongIds: List<Long>,
    ) {
        preferences.edit {
            putString(KEY_PLAYLISTS, serializePlaylists(playlists))
            putString(KEY_FAVORITE_SONG_IDS, favoriteSongIds.joinToString(","))
        }
        _userPlaylists.value = playlists
        _favoriteSongIds.value = favoriteSongIds
        _playlists.value = assemblePlaylists(playlists, favoriteSongIds)
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

    private fun assemblePlaylists(
        userPlaylists: List<Playlist>,
        favoriteSongIds: List<Long>,
    ): List<Playlist> {
        return userPlaylists
    }

    private fun assembleSmartPlaylists(userPlaylists: List<SmartPlaylist>): List<SmartPlaylist> {
        return SmartPlaylistDefaults.builtIns() + userPlaylists
    }

    private companion object {
        const val BAND_COUNT = 18
        const val MAX_SEARCH_HISTORY = 6
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_TEXT_SIZE_PRESET = "text_size_preset"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_SEARCH_HISTORY = "search_history"
        const val KEY_PLAYLISTS = "playlists"
        const val KEY_NEXT_PLAYLIST_ID = "next_playlist_id"
        const val KEY_SMART_PLAYLISTS = "smart_playlists"
        const val KEY_NEXT_SMART_PLAYLIST_ID = "next_smart_playlist_id"
        const val KEY_FAVORITE_SONG_IDS = "favorite_song_ids"
        const val KEY_ALBUM_PLAY_COUNTS = "album_play_counts"
        const val KEY_SONG_PLAY_COUNTS = "song_play_counts"
        const val KEY_RECENT_SONG_IDS = "recent_song_ids"
        const val KEY_RECENT_ALBUM_IDS = "recent_album_ids"
        const val KEY_LAST_PLAYED_COLLECTION_KIND = "last_played_collection_kind"
        const val KEY_LAST_PLAYED_COLLECTION_ID = "last_played_collection_id"
        const val KEY_PLAYBACK_VOLUME = "playback_volume"
        const val KEY_GAPLESS_PLAYBACK_ENABLED = "gapless_playback_enabled"
        const val KEY_VOLUME_NORMALIZATION_ENABLED = "volume_normalization_enabled"
        const val KEY_ONLINE_LYRICS_LOOKUP_ENABLED = "online_lyrics_lookup_enabled"
        const val KEY_ALBUM_COLLECTION_GRID_ENABLED = "album_collection_grid_enabled"
        const val KEY_ALBUM_COLLECTION_LAYOUT_MODE = "album_collection_layout_mode"
        const val KEY_SONG_COLLECTION_GRID_ENABLED = "song_collection_grid_enabled"
        const val KEY_ALBUM_COLLECTION_SORT_MODE = "album_collection_sort_mode"
        const val KEY_SONG_COLLECTION_SORT_MODE = "song_collection_sort_mode"
        const val KEY_LIBRARY_FOLDER_URI = "library_folder_uri"
        const val KEY_LIBRARY_FOLDER_PATH = "library_folder_path"
        const val KEY_LIBRARY_FOLDERS = "library_folders"
        const val KEY_BANDS = "eq_bands"
        const val KEY_BASS = "eq_bass"
        const val KEY_MIDRANGE = "eq_midrange"
        const val KEY_TREBLE = "eq_treble"
        const val KEY_SPACIOUSNESS = "eq_spaciousness"
        const val KEY_SPACIOUSNESS_MODE = "eq_spaciousness_mode"
        const val KEY_MONO_ENABLED = "mono_playback_enabled"
        const val KEY_REVERB_DURATION_MS = "eq_reverb_duration_ms"
        const val KEY_REVERB_PROFILE = "eq_reverb_profile"
        const val MAX_RECENT_PLAYBACK_IDS = 24
        const val DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE = "Grid"
        const val DEFAULT_ALBUM_COLLECTION_SORT_MODE = "Artist"
        const val DEFAULT_SONG_COLLECTION_SORT_MODE = "Title"
        const val PLAYBACK_HISTORY_PERSIST_DEBOUNCE_MS = 350L
        const val EQ_SETTINGS_PERSIST_DEBOUNCE_MS = 120L
        const val RECORD_SEPARATOR = "\u001E"
        const val FIELD_SEPARATOR = "\u001F"
    }
}
