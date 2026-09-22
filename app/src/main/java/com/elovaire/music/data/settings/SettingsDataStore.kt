package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "elovaire_settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = PreferenceStorage.PREFERENCE_FILE_NAME,
                keysToMigrate = settingsPreferenceKeys,
            ),
        )
    },
)

internal fun Context.elovaireSettingsDataStore(): DataStore<Preferences> = settingsDataStore

internal class SettingsSnapshot internal constructor(
    private val values: Map<String, Any?>,
) {
    constructor(values: Preferences) : this(
        values.asMap().entries.associate { (key, value) -> key.name to value },
    )

    fun getBoolean(key: String, default: Boolean): Boolean =
        values[key] as? Boolean ?: default

    fun getFloat(key: String, default: Float): Float =
        (values[key] as? Number)?.toFloat() ?: default

    fun getInt(key: String, default: Int): Int =
        (values[key] as? Number)?.toInt() ?: default

    fun getLong(key: String, default: Long): Long =
        (values[key] as? Number)?.toLong() ?: default

    fun getString(key: String, default: String?): String? =
        values[key] as? String ?: default

    fun contains(key: String): Boolean = key in values

    internal fun asMap(): Map<String, Any?> = values
}

internal suspend fun DataStore<Preferences>.editSettings(
    transform: suspend MutablePreferences.() -> Unit,
) {
    edit { preferences -> preferences.transform() }
}

internal fun MutablePreferences.putBoolean(key: String, value: Boolean) {
    this[booleanPreferencesKey(key)] = value
}

internal fun MutablePreferences.putFloat(key: String, value: Float) {
    this[floatPreferencesKey(key)] = value
}

internal fun MutablePreferences.putInt(key: String, value: Int) {
    this[intPreferencesKey(key)] = value
}

internal fun MutablePreferences.putLong(key: String, value: Long) {
    this[longPreferencesKey(key)] = value
}

internal fun MutablePreferences.putString(key: String, value: String) {
    this[stringPreferencesKey(key)] = value
}

internal fun MutablePreferences.remove(key: String) {
    val existing = asMap().keys.firstOrNull { it.name == key } ?: return
    @Suppress("UNCHECKED_CAST")
    remove(existing as Preferences.Key<Any>)
}

// This migration is intentionally limited to settings. The legacy preferences file also
// contains Room's one-time user-data migration and update-controller state, which must remain
// available to their owners until those migrations have completed.
internal val settingsPreferenceKeys = setOf(
    "theme_mode",
    "text_size_preset",
    "app_language",
    "playback_volume",
    "audiobook_playback_speed",
    "crossfade_enabled",
    "crossfade_duration_ms",
    "crossfade_silence_threshold_db",
    "audiobook_rewind_seconds",
    "audiobook_forward_seconds",
    "audiobook_resume_playback",
    "smart_playlist_enabled_types",
    "smart_playlist_max_songs",
    "gapless_playback_enabled",
    "volume_normalization_enabled",
    "online_lyrics_enabled",
    "now_playing_bar_style",
    "album_collection_grid_enabled",
    "album_collection_layout_mode",
    "album_collection_layout_mode_user_selected",
    "song_collection_grid_enabled",
    "album_collection_sort_mode",
    "song_collection_sort_mode",
    "library_folder_uri",
    "library_folder_path",
    "library_folders",
    "eq_bands",
    "eq_preamp_db",
    "eq_bass",
    "eq_midrange",
    "eq_treble",
    "eq_spaciousness",
    "eq_spaciousness_mode",
    "eq_reverb_duration_ms",
    "eq_reverb_profile",
    "eq_custom_presets",
    "dismissed_update_version",
    "last_automatic_update_check_at_ms",
)
