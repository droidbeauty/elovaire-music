package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
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

class PreferenceStore internal constructor(
    context: Context,
    private val userDataStore: RoomUserDataStore,
) :
    RootSettingsReader,
    AppearanceSettingsWriter,
    LibrarySettingsWriter,
    PlaybackSettingsWriter,
    PlaylistStore by userDataStore,
    FavoritesStore by userDataStore {
    private val appContext = context.applicationContext
    private val preferences = allowStrictModeDiskWrites {
        // SharedPreferences may create its private directory during first startup.
        // Initial settings must be available synchronously before the first UI state is published.
        PreferenceStorage(appContext).preferences.also { it.all }
    }
    private val preferenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eqPersistJob: Job? = null
    private var pendingEqSettings: EqSettings? = null

    fun clearRemovedRemoteProviderData() {
        appContext.getSharedPreferences("artist_image_cache", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        appContext.getSharedPreferences("tag_match_cache", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        appContext.cacheDir.resolve("artist_backdrops").let { directory ->
            directory.listFiles()?.forEach { file ->
                if (file.isFile) file.delete()
            }
            directory.delete()
        }
    }

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

    val searchHistory get() = userDataStore.searchHistory
    override val albumPlayCounts get() = userDataStore.albumPlayCounts
    override val songPlayCounts get() = userDataStore.songPlayCounts
    override val recentSongIds get() = userDataStore.recentSongIds
    override val recentAlbumIds get() = userDataStore.recentAlbumIds
    override val lastPlayedCollectionKind get() = userDataStore.lastPlayedCollectionKind
    override val lastPlayedCollectionId get() = userDataStore.lastPlayedCollectionId
    override val smartPlaylists get() = userDataStore.smartPlaylists
    override val favoriteSongIds get() = userDataStore.favoriteSongIds
    override val playlists get() = userDataStore.playlists

    override fun setThemeMode(themeMode: ThemeMode) {
        updateStateAndPreference(_themeMode, themeMode) {
            putString(KEY_THEME_MODE, themeMode.name)
        }
    }

    override fun setTextSizePreset(textSizePreset: TextSizePreset) {
        updateStateAndPreference(_textSizePreset, textSizePreset) {
            putString(KEY_TEXT_SIZE_PRESET, textSizePreset.name)
        }
    }

    override fun setAppLanguage(language: AppLanguage) {
        updateStateAndPreference(_appLanguage, language) {
            putString(KEY_APP_LANGUAGE, language.name)
        }
    }

    fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        userDataStore.addSearchHistoryEntry(entry)
    }

    fun clearSearchHistory() {
        userDataStore.clearSearchHistoryEntries()
    }

    fun recordPlaybackTransition(
        songId: Long?,
        albumId: Long?,
    ) {
        userDataStore.recordPlaybackTransition(songId, albumId)
    }

    fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) {
        userDataStore.setRecentPlaybackIds(
            songIds,
            albumIds,
            lastPlayedCollectionKind,
            lastPlayedCollectionId,
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
        updateStateAndPreference(_playbackVolume, volume) {
            putFloat(KEY_PLAYBACK_VOLUME, volume)
        }
    }

    override fun setGaplessPlaybackEnabled(enabled: Boolean) {
        updateStateAndPreference(_gaplessPlaybackEnabled, enabled) {
            putBoolean(KEY_GAPLESS_PLAYBACK_ENABLED, enabled)
        }
    }

    override fun setVolumeNormalizationEnabled(enabled: Boolean) {
        updateStateAndPreference(_volumeNormalizationEnabled, enabled) {
            putBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, enabled)
        }
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
        updateStateAndPreference(_songCollectionGridEnabled, enabled) {
            putBoolean(KEY_SONG_COLLECTION_GRID_ENABLED, enabled)
        }
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
            putString(
                KEY_LIBRARY_FOLDERS,
                normalized.joinToString(PreferenceCollectionCodec.RECORD_SEPARATOR) {
                    PreferenceCollectionCodec.serializeLibraryFolder(it)
                },
            )
        }
        _libraryFolders.value = normalized
    }

    override fun restoreDefaultLibraryFolderIfEmpty() {
        if (_libraryFolders.value.isNotEmpty()) return
        setLibraryFolders(listOf(LibraryFolderSelectionResolver.defaultMusicFolder()))
    }

    fun release(onUserDataDrained: () -> Unit = {}) {
        flushEqSettingsPersistence(commit = true)
        userDataStore.release(onUserDataDrained)
        preferenceScope.cancel()
    }

    private fun persistEqSettings(
        settings: EqSettings,
        immediate: Boolean = true,
    ) {
        val normalizedSettings = EqValuePolicy.sanitize(settings)
        if (_eqSettings.value == normalizedSettings && pendingEqSettings == normalizedSettings) return
        if (_eqSettings.value == normalizedSettings && pendingEqSettings == null) return
        _eqSettings.value = normalizedSettings
        if (immediate) {
            flushEqSettingsPersistence(commit = false)
            writeEqSettings(normalizedSettings, commit = false)
        } else {
            pendingEqSettings = normalizedSettings
            scheduleEqSettingsPersistence()
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

    private inline fun <T> updateStateAndPreference(
        state: MutableStateFlow<T>,
        value: T,
        crossinline write: SharedPreferences.Editor.() -> Unit,
    ) {
        if (state.value == value) return
        preferences.edit {
            write()
        }
        state.value = value
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
                ?.split(PreferenceCollectionCodec.RECORD_SEPARATOR)
                ?.mapNotNull(PreferenceCollectionCodec::deserializeLibraryFolder)
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
            putString(
                KEY_LIBRARY_FOLDERS,
                normalized.joinToString(PreferenceCollectionCodec.RECORD_SEPARATOR) {
                    PreferenceCollectionCodec.serializeLibraryFolder(it)
                },
            )
        }
        return normalized
    }

    private companion object {
        const val BAND_COUNT = 18
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_TEXT_SIZE_PRESET = "text_size_preset"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_PLAYBACK_VOLUME = "playback_volume"
        const val KEY_GAPLESS_PLAYBACK_ENABLED = "gapless_playback_enabled"
        const val KEY_VOLUME_NORMALIZATION_ENABLED = "volume_normalization_enabled"
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
        const val DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE = "Grid"
        const val DEFAULT_ALBUM_COLLECTION_SORT_MODE = "Artist"
        const val DEFAULT_SONG_COLLECTION_SORT_MODE = "Title"
        const val EQ_SETTINGS_PERSIST_DEBOUNCE_MS = 120L
    }
}

internal fun incrementPlayCount(current: Int?, increment: Int = 1): Int {
    val safeIncrement = increment.coerceAtLeast(0)
    return current?.coerceAtLeast(0)?.let { value ->
        (value.toLong() + safeIncrement).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    } ?: safeIncrement
}

internal fun normalizeFavoriteSongIds(songIds: Iterable<Long>): List<Long> {
    return songIds.asSequence().filter { it != 0L }.distinct().toList()
}
