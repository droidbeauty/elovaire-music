package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.CrossfadeDurationPolicy
import elovaire.music.droidbeauty.app.data.playback.CrossfadeSilencePolicy
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.data.playback.normalizeReverbDurationMs
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSettingsPolicy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("TooManyFunctions")
class PreferenceStore internal constructor(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ownerScope: CoroutineScope? = null,
    private val initialSettings: SettingsSnapshot = SettingsSnapshot(emptyMap<String, Any?>()),
    private val portableSettingsBackup: PortableSettingsBackup? = null,
    private val diagnostics: BackendDiagnosticsRuntime? = null,
) :
    AppearanceSettingsStore,
    AppearanceSettingsWriter,
    LibrarySettingsWriter,
    PlaybackSettingsWriter,
    EqualizerSettingsStore,
    NowPlayingSettingsStore,
    UpdatePreferencesStore,
    PlaybackIntegrationSettings {
    private val appContext = context.applicationContext
    private val settingsDataStore = appContext.elovaireSettingsDataStore()
    private var currentSettings = initialSettings
    private var bootSettingsInitialized = false
    private val persistenceScope = CoroutineScope(
        (ownerScope?.coroutineContext ?: EmptyCoroutineContext) +
            SupervisorJob(ownerScope?.coroutineContext?.get(Job)) +
            ioDispatcher + CoroutineName("settings-persistence-owner"),
    )
    private val settingsWriteSequencer = SettingsWriteSequencer(
        ownerScope = persistenceScope,
        dispatcher = ioDispatcher,
        persist = ::persistSettings,
    )
    private var preferences: SettingsSnapshot
        get() = currentSettings
        set(value) {
            currentSettings = value
        }
    private val legacyPreferences: SharedPreferences = allowStrictModeDiskReads {
        PreferenceStorage(appContext).preferences
    }
    private var pendingEqSettings: EqSettings? = null
    private var pendingCrossfadeDurationMs: Long? = null
    private var pendingCrossfadeSilenceThresholdDb: Float? = null

    private val _dismissedUpdateVersion = MutableStateFlow(
        preferences.getString(KEY_DISMISSED_UPDATE_VERSION, null)
            ?: runCatching { legacyPreferences.getString(KEY_DISMISSED_UPDATE_VERSION, null) }.getOrNull(),
    )
    override val dismissedUpdateVersion: StateFlow<String?> = _dismissedUpdateVersion.asStateFlow()
    private val lastUpdateCheckAtMs = AtomicLong(
        if (preferences.contains(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS)
        ) {
            preferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L)
        } else {
            runCatching {
                legacyPreferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L)
            }.getOrDefault(0L)
        }.coerceAtLeast(0L),
    )

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

    private val _crossfadeEnabled = MutableStateFlow(loadCrossfadeEnabled())
    override val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()

    private val _crossfadeDurationMs = MutableStateFlow(loadCrossfadeDurationMs())
    override val crossfadeDurationMs: StateFlow<Long> = _crossfadeDurationMs.asStateFlow()

    private val _crossfadeSilenceThresholdDb = MutableStateFlow(loadCrossfadeSilenceThresholdDb())
    override val crossfadeSilenceThresholdDb: StateFlow<Float> = _crossfadeSilenceThresholdDb.asStateFlow()

    private val _audiobookSettings = MutableStateFlow(loadAudiobookSettings())
    override val audiobookSettings: StateFlow<AudiobookSettings> = _audiobookSettings.asStateFlow()

    private val _smartPlaylistEnabledTypes = MutableStateFlow(loadSmartPlaylistEnabledTypes())
    override val smartPlaylistEnabledTypes: StateFlow<Set<BuiltInSmartPlaylistType>> =
        _smartPlaylistEnabledTypes.asStateFlow()

    private val _smartPlaylistMaxSongs = MutableStateFlow(loadSmartPlaylistMaxSongs())
    override val smartPlaylistMaxSongs: StateFlow<Int> = _smartPlaylistMaxSongs.asStateFlow()

    private val _volumeNormalizationEnabled = MutableStateFlow(loadVolumeNormalizationEnabled())
    override val volumeNormalizationEnabled: StateFlow<Boolean> = _volumeNormalizationEnabled.asStateFlow()

    private val _onlineLyricsEnabled = MutableStateFlow(preferences.getBoolean(KEY_ONLINE_LYRICS_ENABLED, true))
    override val onlineLyricsEnabled: StateFlow<Boolean> = _onlineLyricsEnabled.asStateFlow()

    private val _nowPlayingBarStyle = MutableStateFlow(loadNowPlayingBarStyle())
    override val nowPlayingBarStyle: StateFlow<NowPlayingBarStyle> = _nowPlayingBarStyle.asStateFlow()

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

    init {
        migrateLegacyUpdatePreferencesIfNeeded()
        persistenceScope.launch {
            settingsDataStore.data.collect(::applyDataStoreSettings)
        }
    }

    override fun setDismissedUpdateVersion(versionName: String?) {
        val normalized = versionName?.trim()?.takeIf { it.isNotBlank() }
        if (_dismissedUpdateVersion.value == normalized) return
        _dismissedUpdateVersion.value = normalized
        checkpointBootSettings(KEY_DISMISSED_UPDATE_VERSION to normalized)
        enqueueSettingsWrite {
            settingsDataStore.editSettings {
                remove(KEY_DISMISSED_UPDATE_VERSION)
                normalized?.let { putString(KEY_DISMISSED_UPDATE_VERSION, it) }
            }
        }
    }

    override fun lastAutomaticUpdateCheckAtMs(): Long = lastUpdateCheckAtMs.get()

    override fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long) {
        val normalized = timestampMs.coerceAtLeast(0L)
        lastUpdateCheckAtMs.set(normalized)
        checkpointBootSettings(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS to normalized)
        enqueueSettingsWrite {
            settingsDataStore.editSettings {
                putLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, normalized)
            }
        }
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        updateStateAndPreference(_themeMode, themeMode, KEY_THEME_MODE to themeMode.name) {
            putString(KEY_THEME_MODE, themeMode.name)
        }
    }

    override fun setTextSizePreset(textSizePreset: TextSizePreset) {
        updateStateAndPreference(_textSizePreset, textSizePreset, KEY_TEXT_SIZE_PRESET to textSizePreset.name) {
            putString(KEY_TEXT_SIZE_PRESET, textSizePreset.name)
        }
    }

    override fun setAppLanguage(language: AppLanguage) {
        updateStateAndPreference(_appLanguage, language, KEY_APP_LANGUAGE to language.name) {
            putString(KEY_APP_LANGUAGE, language.name)
        }
    }

    override fun setNowPlayingBarStyle(style: NowPlayingBarStyle) {
        updateStateAndPreference(_nowPlayingBarStyle, style, KEY_NOW_PLAYING_BAR_STYLE to style.name) {
            putString(KEY_NOW_PLAYING_BAR_STYLE, style.name)
        }
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

    override fun setEqSettings(settings: EqSettings) {
        persistEqSettings(EqValuePolicy.sanitize(settings))
    }

    fun resetEqSettings() {
        persistEqSettings(EqSettings())
    }

    override fun setPlaybackVolume(value: Float) {
        val volume = value.coerceIn(0f, 1f)
        updateStateAndPreference(_playbackVolume, volume, KEY_PLAYBACK_VOLUME to volume) {
            putFloat(KEY_PLAYBACK_VOLUME, volume)
        }
    }

    override fun setCrossfadeEnabled(enabled: Boolean) {
        updateStateAndPreference(_crossfadeEnabled, enabled, KEY_CROSSFADE_ENABLED to enabled) {
            putBoolean(KEY_CROSSFADE_ENABLED, enabled)
            remove(KEY_GAPLESS_PLAYBACK_ENABLED)
        }
    }

    override fun setCrossfadeDurationMs(value: Long) {
        val durationMs = CrossfadeDurationPolicy.sanitizeSettingsDuration(value)
        if (_crossfadeDurationMs.value == durationMs && pendingCrossfadeDurationMs == null) return
        _crossfadeDurationMs.value = durationMs
        pendingCrossfadeDurationMs = durationMs
        checkpointBootSettings(KEY_CROSSFADE_DURATION_MS to durationMs)
        scheduleCrossfadePersistence()
    }

    override fun setCrossfadeSilenceThresholdDb(value: Float) {
        val thresholdDb = CrossfadeSilencePolicy.sanitizeLevelDb(value)
        if (_crossfadeSilenceThresholdDb.value == thresholdDb && pendingCrossfadeSilenceThresholdDb == null) return
        _crossfadeSilenceThresholdDb.value = thresholdDb
        pendingCrossfadeSilenceThresholdDb = thresholdDb
        checkpointBootSettings(KEY_CROSSFADE_SILENCE_THRESHOLD_DB to thresholdDb)
        scheduleCrossfadePersistence()
    }

    override fun setAudiobookRewindSeconds(value: Int) {
        updateAudiobookSettings(_audiobookSettings.value.copy(rewindSeconds = value.sanitizeAudiobookSeekSeconds()))
    }

    override fun setAudiobookForwardSeconds(value: Int) {
        updateAudiobookSettings(_audiobookSettings.value.copy(forwardSeconds = value.sanitizeAudiobookSeekSeconds()))
    }

    override fun setAudiobookResumePlayback(enabled: Boolean) {
        updateAudiobookSettings(_audiobookSettings.value.copy(resumePlayback = enabled))
    }

    override fun setSmartPlaylistEnabled(type: BuiltInSmartPlaylistType, enabled: Boolean) {
        val next = _smartPlaylistEnabledTypes.value.toMutableSet().apply {
            if (enabled) add(type) else remove(type)
        }.toSet()
        if (_smartPlaylistEnabledTypes.value == next) return
        _smartPlaylistEnabledTypes.value = next
        checkpointBootSettings(
            KEY_SMART_PLAYLIST_ENABLED_TYPES to next.joinToString(",") { it.name },
        )
        enqueueSettingsEdit {
            putString(KEY_SMART_PLAYLIST_ENABLED_TYPES, next.joinToString(",") { it.name })
        }
    }

    override fun setSmartPlaylistMaxSongs(value: Int) {
        val next = SmartPlaylistSettingsPolicy.sanitizeSongLimit(value)
        if (_smartPlaylistMaxSongs.value == next) return
        _smartPlaylistMaxSongs.value = next
        checkpointBootSettings(KEY_SMART_PLAYLIST_MAX_SONGS to next)
        enqueueSettingsEdit { putInt(KEY_SMART_PLAYLIST_MAX_SONGS, next) }
    }

    override fun setVolumeNormalizationEnabled(enabled: Boolean) {
        updateStateAndPreference(
            _volumeNormalizationEnabled,
            enabled,
            KEY_VOLUME_NORMALIZATION_ENABLED to enabled,
        ) {
            putBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, enabled)
        }
    }

    override fun setOnlineLyricsEnabled(enabled: Boolean) {
        updateStateAndPreference(_onlineLyricsEnabled, enabled, KEY_ONLINE_LYRICS_ENABLED to enabled) {
            putBoolean(KEY_ONLINE_LYRICS_ENABLED, enabled)
        }
    }

    override fun setAlbumCollectionLayoutMode(mode: String) {
        val normalizedMode = mode.trim().ifBlank { DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE }
        if (_albumCollectionLayoutMode.value == normalizedMode) return
        enqueueSettingsEdit {
            putString(KEY_ALBUM_COLLECTION_LAYOUT_MODE, normalizedMode)
            putBoolean(KEY_ALBUM_COLLECTION_LAYOUT_MODE_USER_SELECTED, true)
        }
        _albumCollectionLayoutMode.value = normalizedMode
        checkpointBootSettings(
            KEY_ALBUM_COLLECTION_LAYOUT_MODE to normalizedMode,
            KEY_ALBUM_COLLECTION_LAYOUT_MODE_USER_SELECTED to true,
        )
    }

    override fun setSongCollectionGridEnabled(enabled: Boolean) {
        updateStateAndPreference(_songCollectionGridEnabled, enabled, KEY_SONG_COLLECTION_GRID_ENABLED to enabled) {
            putBoolean(KEY_SONG_COLLECTION_GRID_ENABLED, enabled)
        }
    }

    override fun setAlbumCollectionSortMode(sortMode: String) {
        val normalizedSortMode = sortMode.trim().ifBlank { DEFAULT_ALBUM_COLLECTION_SORT_MODE }
        if (_albumCollectionSortMode.value == normalizedSortMode) return
        enqueueSettingsEdit {
            putString(KEY_ALBUM_COLLECTION_SORT_MODE, normalizedSortMode)
        }
        _albumCollectionSortMode.value = normalizedSortMode
        checkpointBootSettings(KEY_ALBUM_COLLECTION_SORT_MODE to normalizedSortMode)
    }

    override fun setSongCollectionSortMode(sortMode: String) {
        val normalizedSortMode = sortMode.trim().ifBlank { DEFAULT_SONG_COLLECTION_SORT_MODE }
        if (_songCollectionSortMode.value == normalizedSortMode) return
        enqueueSettingsEdit {
            putString(KEY_SONG_COLLECTION_SORT_MODE, normalizedSortMode)
        }
        _songCollectionSortMode.value = normalizedSortMode
        checkpointBootSettings(KEY_SONG_COLLECTION_SORT_MODE to normalizedSortMode)
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
        enqueueSettingsEdit {
            putString(
                KEY_LIBRARY_FOLDERS,
                normalized.joinToString(PreferenceCollectionCodec.RECORD_SEPARATOR) {
                    PreferenceCollectionCodec.serializeLibraryFolder(it)
                },
            )
        }
        _libraryFolders.value = normalized
        checkpointBootSettings(KEY_LIBRARY_FOLDERS to normalized.joinToString(
            PreferenceCollectionCodec.RECORD_SEPARATOR,
        ) { PreferenceCollectionCodec.serializeLibraryFolder(it) })
    }

    override fun restoreDefaultLibraryFolderIfEmpty() {
        if (_libraryFolders.value.isNotEmpty()) return
        setLibraryFolders(listOf(LibraryFolderSelectionResolver.defaultMusicFolder()))
    }

    fun release() {
        runBlocking(ioDispatcher) { settingsWriteSequencer.flush() }
        settingsWriteSequencer.close()
        persistenceScope.cancel()
    }

    private fun persistEqSettings(
        settings: EqSettings,
        immediate: Boolean = true,
    ) {
        val normalizedSettings = EqValuePolicy.sanitize(settings)
        if (_eqSettings.value == normalizedSettings && pendingEqSettings == normalizedSettings) return
        if (_eqSettings.value == normalizedSettings && pendingEqSettings == null) return
        _eqSettings.value = normalizedSettings
        checkpointBootSettings(
            KEY_BANDS to normalizedSettings.bands.joinToString(","),
            KEY_BASS to normalizedSettings.bass,
            KEY_MIDRANGE to normalizedSettings.midrange,
            KEY_TREBLE to normalizedSettings.treble,
            KEY_SPACIOUSNESS to normalizedSettings.spaciousness,
            KEY_SPACIOUSNESS_MODE to normalizedSettings.spaciousnessMode.name,
            KEY_REVERB_DURATION_MS to normalizedSettings.reverbDurationMs,
            KEY_REVERB_PROFILE to normalizedSettings.reverbProfile.name,
        )
        if (immediate) {
            pendingEqSettings = null
            settingsWriteSequencer.replaceLatest("equalizer", write = { writeEqSettings(normalizedSettings) })
        } else {
            pendingEqSettings = normalizedSettings
            scheduleEqSettingsPersistence()
        }
    }

    private fun scheduleEqSettingsPersistence() {
        val settings = pendingEqSettings ?: return
        settingsWriteSequencer.replaceLatest(
            key = "equalizer",
            debounceMs = EQ_SETTINGS_PERSIST_DEBOUNCE_MS,
        ) {
            pendingEqSettings = null
            writeEqSettings(settings)
        }
    }

    private suspend fun writeEqSettings(settings: EqSettings) {
        settingsDataStore.editSettings {
            putString(KEY_BANDS, settings.bands.joinToString(","))
            putFloat(KEY_BASS, settings.bass)
            putFloat(KEY_MIDRANGE, settings.midrange)
            putFloat(KEY_TREBLE, settings.treble)
            putFloat(KEY_SPACIOUSNESS, settings.spaciousness)
            putString(KEY_SPACIOUSNESS_MODE, settings.spaciousnessMode.name)
            putInt(KEY_REVERB_DURATION_MS, settings.reverbDurationMs)
            putString(KEY_REVERB_PROFILE, settings.reverbProfile.name)
        }
    }

    private fun scheduleCrossfadePersistence() {
        settingsWriteSequencer.replaceLatest(
            key = "crossfade",
            debounceMs = CROSSFADE_SETTINGS_PERSIST_DEBOUNCE_MS,
        ) {
            flushCrossfadePersistence()
        }
    }

    private suspend fun flushCrossfadePersistence() {
        val durationMs = pendingCrossfadeDurationMs
        val thresholdDb = pendingCrossfadeSilenceThresholdDb
        if (durationMs == null && thresholdDb == null) return
        pendingCrossfadeDurationMs = null
        pendingCrossfadeSilenceThresholdDb = null
        settingsDataStore.editSettings {
            durationMs?.let { putLong(KEY_CROSSFADE_DURATION_MS, it) }
            thresholdDb?.let { putFloat(KEY_CROSSFADE_SILENCE_THRESHOLD_DB, it) }
        }
    }

    private inline fun <T> updateStateAndPreference(
        state: MutableStateFlow<T>,
        value: T,
        vararg bootChanges: Pair<String, Any?>,
        crossinline write: MutablePreferences.() -> Unit,
    ) {
        if (state.value == value) return
        state.value = value
        checkpointBootSettings(*bootChanges)
        enqueueSettingsWrite {
            settingsDataStore.editSettings { write() }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun persistSettings(kind: String, write: suspend () -> Unit) {
        try {
            write()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            Log.w(TAG, "Unable to persist $kind settings.", failure)
        } catch (failure: IllegalStateException) {
            Log.w(TAG, "Unable to persist $kind settings.", failure)
        } catch (failure: RuntimeException) {
            (diagnostics ?: BackendDiagnostics).recordWorkerFailure("settings-persistence", failure)
            throw failure
        }
    }

    private fun applyDataStoreSettings(next: Preferences) {
        val nextSettings = SettingsSnapshot(next)
        if (nextSettings.asMap().isEmpty() && preferences.asMap().isNotEmpty()) return
        if (nextSettings.asMap() == preferences.asMap()) return
        if (
            settingsWriteSequencer.hasPendingWork() ||
                pendingEqSettings != null ||
                pendingCrossfadeDurationMs != null ||
                pendingCrossfadeSilenceThresholdDb != null
        ) return

        preferences = nextSettings
        _dismissedUpdateVersion.value = nextSettings.getString(
            KEY_DISMISSED_UPDATE_VERSION,
            runCatching { legacyPreferences.getString(KEY_DISMISSED_UPDATE_VERSION, null) }.getOrNull(),
        )
        lastUpdateCheckAtMs.set(
            nextSettings.getLong(
                KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS,
                runCatching { legacyPreferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L) }
                    .getOrDefault(0L),
            ).coerceAtLeast(0L),
        )
        _themeMode.value = loadThemeMode()
        _textSizePreset.value = loadTextSizePreset()
        _appLanguage.value = loadAppLanguage()
        _eqSettings.value = loadEqSettings()
        _playbackVolume.value = loadPlaybackVolume()
        _crossfadeEnabled.value = loadCrossfadeEnabled()
        _crossfadeDurationMs.value = loadCrossfadeDurationMs()
        _crossfadeSilenceThresholdDb.value = loadCrossfadeSilenceThresholdDb()
        _audiobookSettings.value = loadAudiobookSettings()
        _smartPlaylistEnabledTypes.value = loadSmartPlaylistEnabledTypes()
        _smartPlaylistMaxSongs.value = loadSmartPlaylistMaxSongs()
        _volumeNormalizationEnabled.value = loadVolumeNormalizationEnabled()
        _onlineLyricsEnabled.value = nextSettings.getBoolean(KEY_ONLINE_LYRICS_ENABLED, true)
        _nowPlayingBarStyle.value = loadNowPlayingBarStyle()
        _albumCollectionLayoutMode.value = loadAlbumCollectionLayoutMode()
        _songCollectionGridEnabled.value = loadSongCollectionGridEnabled()
        _albumCollectionSortMode.value = loadAlbumCollectionSortMode()
        _songCollectionSortMode.value = loadSongCollectionSortMode()
        _libraryFolders.value = loadLibraryFolders()
        portableSettingsBackup?.checkpointBootSettings(currentSettingsValues())
    }

    private fun checkpointBootSettings(vararg changes: Pair<String, Any?>) {
        val values = if (!bootSettingsInitialized || changes.isEmpty()) {
            currentSettingsValues()
        } else {
            preferences.asMap().toMutableMap().apply {
                changes.forEach { (key, value) ->
                    if (value == null) remove(key) else this[key] = value
                }
            }.filterKeys { it in settingsPreferenceKeys }
        }
        currentSettings = SettingsSnapshot(values)
        bootSettingsInitialized = true
        portableSettingsBackup?.checkpointBootSettings(values)
    }

    private fun currentSettingsValues(): Map<String, Any?> {
        return preferences.asMap().toMutableMap().apply {
            _dismissedUpdateVersion.value?.let { this[KEY_DISMISSED_UPDATE_VERSION] = it }
                ?: remove(KEY_DISMISSED_UPDATE_VERSION)
            this[KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS] = lastUpdateCheckAtMs.get()
            this[KEY_THEME_MODE] = _themeMode.value.name
            this[KEY_TEXT_SIZE_PRESET] = _textSizePreset.value.name
            this[KEY_APP_LANGUAGE] = _appLanguage.value.name
            this[KEY_BANDS] = _eqSettings.value.bands.joinToString(",")
            this[KEY_BASS] = _eqSettings.value.bass
            this[KEY_MIDRANGE] = _eqSettings.value.midrange
            this[KEY_TREBLE] = _eqSettings.value.treble
            this[KEY_SPACIOUSNESS] = _eqSettings.value.spaciousness
            this[KEY_SPACIOUSNESS_MODE] = _eqSettings.value.spaciousnessMode.name
            this[KEY_REVERB_DURATION_MS] = _eqSettings.value.reverbDurationMs
            this[KEY_REVERB_PROFILE] = _eqSettings.value.reverbProfile.name
            this[KEY_PLAYBACK_VOLUME] = _playbackVolume.value
            this[KEY_CROSSFADE_ENABLED] = _crossfadeEnabled.value
            this[KEY_CROSSFADE_DURATION_MS] = _crossfadeDurationMs.value
            this[KEY_CROSSFADE_SILENCE_THRESHOLD_DB] = _crossfadeSilenceThresholdDb.value
            this[KEY_AUDIOBOOK_REWIND_SECONDS] = _audiobookSettings.value.rewindSeconds
            this[KEY_AUDIOBOOK_FORWARD_SECONDS] = _audiobookSettings.value.forwardSeconds
            this[KEY_AUDIOBOOK_RESUME_PLAYBACK] = _audiobookSettings.value.resumePlayback
            this[KEY_SMART_PLAYLIST_ENABLED_TYPES] = _smartPlaylistEnabledTypes.value
                .joinToString(",") { it.name }
            this[KEY_SMART_PLAYLIST_MAX_SONGS] = _smartPlaylistMaxSongs.value
            this[KEY_VOLUME_NORMALIZATION_ENABLED] = _volumeNormalizationEnabled.value
            this[KEY_ONLINE_LYRICS_ENABLED] = _onlineLyricsEnabled.value
            this[KEY_NOW_PLAYING_BAR_STYLE] = _nowPlayingBarStyle.value.name
            this[KEY_ALBUM_COLLECTION_LAYOUT_MODE] = _albumCollectionLayoutMode.value
            this[KEY_SONG_COLLECTION_GRID_ENABLED] = _songCollectionGridEnabled.value
            this[KEY_ALBUM_COLLECTION_SORT_MODE] = _albumCollectionSortMode.value
            this[KEY_SONG_COLLECTION_SORT_MODE] = _songCollectionSortMode.value
            this[KEY_LIBRARY_FOLDERS] = _libraryFolders.value.joinToString(
                PreferenceCollectionCodec.RECORD_SEPARATOR,
            ) { PreferenceCollectionCodec.serializeLibraryFolder(it) }
            remove(KEY_GAPLESS_PLAYBACK_ENABLED)
        }.filterKeys { it in settingsPreferenceKeys }
    }

    private fun enqueueSettingsEdit(write: MutablePreferences.() -> Unit) {
        enqueueSettingsWrite {
            settingsDataStore.editSettings { write() }
        }
    }

    private fun enqueueSettingsWrite(write: suspend () -> Unit) {
        settingsWriteSequencer.enqueue("settings", write)
    }

    private fun migrateLegacyUpdatePreferencesIfNeeded() {
        val hasLegacyDismissed = legacyPreferences.contains(KEY_DISMISSED_UPDATE_VERSION)
        val hasLegacyCheck = legacyPreferences.contains(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS)
        if (!hasLegacyDismissed && !hasLegacyCheck) return
        enqueueSettingsWrite {
            settingsDataStore.editSettings {
                if (!preferences.contains(KEY_DISMISSED_UPDATE_VERSION)) {
                    runCatching { legacyPreferences.getString(KEY_DISMISSED_UPDATE_VERSION, null) }
                        .getOrNull()
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { putString(KEY_DISMISSED_UPDATE_VERSION, it) }
                }
                if (!preferences.contains(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS)) {
                    putLong(
                        KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS,
                        runCatching {
                            legacyPreferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L)
                        }.getOrDefault(0L).coerceAtLeast(0L),
                    )
                }
            }
            check(
                legacyPreferences.edit()
                    .remove(KEY_DISMISSED_UPDATE_VERSION)
                    .remove(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS)
                    .commit(),
            ) { "Unable to retire legacy update preferences." }
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

    private fun loadCrossfadeEnabled(): Boolean {
        val enabled = if (preferences.contains(KEY_CROSSFADE_ENABLED)) {
            preferences.getBoolean(KEY_CROSSFADE_ENABLED, false)
        } else {
            preferences.getBoolean(KEY_GAPLESS_PLAYBACK_ENABLED, false)
        }
        if (preferences.contains(KEY_GAPLESS_PLAYBACK_ENABLED)) {
            enqueueSettingsEdit {
                putBoolean(KEY_CROSSFADE_ENABLED, enabled)
                remove(KEY_GAPLESS_PLAYBACK_ENABLED)
            }
        }
        return enabled
    }

    private fun loadCrossfadeDurationMs(): Long {
        return CrossfadeDurationPolicy.sanitizeSettingsDuration(
            preferences.getLong(
                KEY_CROSSFADE_DURATION_MS,
                CrossfadeDurationPolicy.DEFAULT_DURATION_MS,
            ),
        )
    }

    private fun loadCrossfadeSilenceThresholdDb(): Float {
        return CrossfadeSilencePolicy.sanitizeLevelDb(
            preferences.getFloat(
                KEY_CROSSFADE_SILENCE_THRESHOLD_DB,
                CrossfadeSilencePolicy.BASE_LEVEL_DB,
            ),
        )
    }

    private fun loadAudiobookSettings(): AudiobookSettings {
        return AudiobookSettings(
            rewindSeconds = preferences.getInt(KEY_AUDIOBOOK_REWIND_SECONDS, 15).sanitizeAudiobookSeekSeconds(),
            forwardSeconds = preferences.getInt(KEY_AUDIOBOOK_FORWARD_SECONDS, 15).sanitizeAudiobookSeekSeconds(),
            resumePlayback = preferences.getBoolean(KEY_AUDIOBOOK_RESUME_PLAYBACK, true),
        )
    }

    private fun loadSmartPlaylistEnabledTypes(): Set<BuiltInSmartPlaylistType> {
        return preferences.getString(KEY_SMART_PLAYLIST_ENABLED_TYPES, null)
            ?.split(",")
            ?.mapNotNull { saved -> BuiltInSmartPlaylistType.entries.firstOrNull { it.name == saved } }
            ?.toSet()
            ?: BuiltInSmartPlaylistType.entries.toSet()
    }

    private fun loadSmartPlaylistMaxSongs(): Int {
        return SmartPlaylistSettingsPolicy.sanitizeSongLimit(
            preferences.getInt(KEY_SMART_PLAYLIST_MAX_SONGS, SmartPlaylistSettingsPolicy.MIN_SONG_LIMIT),
        )
    }

    private fun updateAudiobookSettings(settings: AudiobookSettings) {
        val normalized = settings.copy(
            rewindSeconds = settings.rewindSeconds.sanitizeAudiobookSeekSeconds(),
            forwardSeconds = settings.forwardSeconds.sanitizeAudiobookSeekSeconds(),
        )
        updateStateAndPreference(
            _audiobookSettings,
            normalized,
            KEY_AUDIOBOOK_REWIND_SECONDS to normalized.rewindSeconds,
            KEY_AUDIOBOOK_FORWARD_SECONDS to normalized.forwardSeconds,
            KEY_AUDIOBOOK_RESUME_PLAYBACK to normalized.resumePlayback,
        ) {
            putInt(KEY_AUDIOBOOK_REWIND_SECONDS, normalized.rewindSeconds)
            putInt(KEY_AUDIOBOOK_FORWARD_SECONDS, normalized.forwardSeconds)
            putBoolean(KEY_AUDIOBOOK_RESUME_PLAYBACK, normalized.resumePlayback)
        }
    }

    private fun loadVolumeNormalizationEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOLUME_NORMALIZATION_ENABLED, false)
    }

    private fun loadNowPlayingBarStyle(): NowPlayingBarStyle {
        return preferences.getString(KEY_NOW_PLAYING_BAR_STYLE, NowPlayingBarStyle.Floating.name)
            ?.let { saved -> NowPlayingBarStyle.entries.firstOrNull { it.name == saved } }
            ?: NowPlayingBarStyle.Floating
    }

    private fun loadAlbumCollectionLayoutMode(): String {
        val savedMode = preferences.getString(KEY_ALBUM_COLLECTION_LAYOUT_MODE, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        if (savedMode != null) {
            if (
                savedMode.equals("DenseGrid", ignoreCase = true) &&
                !preferences.getBoolean(KEY_ALBUM_COLLECTION_LAYOUT_MODE_USER_SELECTED, false)
            ) {
                enqueueSettingsEdit {
                    putString(KEY_ALBUM_COLLECTION_LAYOUT_MODE, DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE)
                }
                return DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE
            }
            return savedMode
        }
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
        enqueueSettingsEdit {
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
        const val KEY_CROSSFADE_ENABLED = "crossfade_enabled"
        const val KEY_CROSSFADE_DURATION_MS = "crossfade_duration_ms"
        const val KEY_CROSSFADE_SILENCE_THRESHOLD_DB = "crossfade_silence_threshold_db"
        const val KEY_AUDIOBOOK_REWIND_SECONDS = "audiobook_rewind_seconds"
        const val KEY_AUDIOBOOK_FORWARD_SECONDS = "audiobook_forward_seconds"
        const val KEY_AUDIOBOOK_RESUME_PLAYBACK = "audiobook_resume_playback"
        const val KEY_SMART_PLAYLIST_ENABLED_TYPES = "smart_playlist_enabled_types"
        const val KEY_SMART_PLAYLIST_MAX_SONGS = "smart_playlist_max_songs"
        const val CROSSFADE_SETTINGS_PERSIST_DEBOUNCE_MS = 250L
        const val KEY_GAPLESS_PLAYBACK_ENABLED = "gapless_playback_enabled"
        const val KEY_VOLUME_NORMALIZATION_ENABLED = "volume_normalization_enabled"
        const val KEY_ONLINE_LYRICS_ENABLED = "online_lyrics_enabled"
        const val KEY_NOW_PLAYING_BAR_STYLE = "now_playing_bar_style"
        const val KEY_ALBUM_COLLECTION_GRID_ENABLED = "album_collection_grid_enabled"
        const val KEY_ALBUM_COLLECTION_LAYOUT_MODE = "album_collection_layout_mode"
        const val KEY_ALBUM_COLLECTION_LAYOUT_MODE_USER_SELECTED = "album_collection_layout_mode_user_selected"
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
        const val KEY_REVERB_DURATION_MS = "eq_reverb_duration_ms"
        const val KEY_REVERB_PROFILE = "eq_reverb_profile"
        const val KEY_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
        const val KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS = "last_automatic_update_check_at_ms"
        const val DEFAULT_ALBUM_COLLECTION_LAYOUT_MODE = "Grid"
        const val DEFAULT_ALBUM_COLLECTION_SORT_MODE = "Artist"
        const val DEFAULT_SONG_COLLECTION_SORT_MODE = "Title"
        const val EQ_SETTINGS_PERSIST_DEBOUNCE_MS = 120L
        const val TAG = "PreferenceStore"
    }
}

private fun Int.sanitizeAudiobookSeekSeconds(): Int = coerceIn(5, 30)

internal fun incrementPlayCount(current: Int?, increment: Int = 1): Int {
    val safeIncrement = increment.coerceAtLeast(0)
    return current?.coerceAtLeast(0)?.let { value ->
        (value.toLong() + safeIncrement).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    } ?: safeIncrement
}

internal fun normalizeFavoriteSongIds(songIds: Iterable<Long>): List<Long> {
    return songIds.asSequence().filter { it != 0L }.distinct().toList()
}
