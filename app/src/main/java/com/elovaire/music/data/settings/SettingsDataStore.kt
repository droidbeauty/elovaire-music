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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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

internal class SettingsSnapshot(
    private val values: Preferences,
) {
    fun getBoolean(key: String, default: Boolean): Boolean =
        values[booleanPreferencesKey(key)] ?: default

    fun getFloat(key: String, default: Float): Float =
        values[floatPreferencesKey(key)] ?: default

    fun getInt(key: String, default: Int): Int =
        values[intPreferencesKey(key)] ?: default

    fun getLong(key: String, default: Long): Long =
        values[longPreferencesKey(key)] ?: default

    fun getString(key: String, default: String?): String? =
        values[stringPreferencesKey(key)] ?: default

    fun contains(key: String): Boolean = values.asMap().keys.any { it.name == key }
}

internal fun readInitialSettings(
    dataStore: DataStore<Preferences>,
    dispatcher: kotlinx.coroutines.CoroutineDispatcher,
): SettingsSnapshot = runBlocking(dispatcher) {
    SettingsSnapshot(dataStore.data.first())
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
    "crossfade_enabled",
    "crossfade_duration_ms",
    "crossfade_silence_threshold_db",
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
    "eq_bass",
    "eq_midrange",
    "eq_treble",
    "eq_spaciousness",
    "eq_spaciousness_mode",
    "mono_playback_enabled",
    "eq_reverb_duration_ms",
    "eq_reverb_profile",
    "dismissed_update_version",
    "last_automatic_update_check_at_ms",
)
