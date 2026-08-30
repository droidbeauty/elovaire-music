package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.FlowPreview
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(FlowPreview::class)
internal class PortableSettingsBackup(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ownerScope: CoroutineScope? = null,
) {
    private val appContext = context.applicationContext
    private val settingsDataStore: DataStore<Preferences> = appContext.elovaireSettingsDataStore()
    private val backup by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        allowStrictModeDiskReads {
            appContext.getSharedPreferences(BACKUP_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    private val restored = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val mirrorScope = CoroutineScope(
        (ownerScope?.coroutineContext ?: EmptyCoroutineContext) +
            SupervisorJob(ownerScope?.coroutineContext?.get(Job)) +
            ioDispatcher,
    )
    private var settingsObservationJob: Job? = null

    fun restore() {
        if (released.get()) return
        if (!restored.compareAndSet(false, true)) return
        runBlocking(ioDispatcher) {
            val backupValues = backup.all.filterKeys(::isPortableSettingKey)
            val current = settingsDataStore.data.first().portableValues()
            if (current.isEmpty() && backupValues.isNotEmpty() && isValidBackup(backupValues)) {
                settingsDataStore.edit { values ->
                    backupValues.forEach { (key, value) -> values.putPortableValue(key, value) }
                }
            }
            syncAll(settingsDataStore.data.first())
        }
    }

    fun start() {
        if (released.get()) return
        restore()
        if (released.get()) return
        if (!started.compareAndSet(false, true)) return
        settingsObservationJob = mirrorScope.launch {
            settingsDataStore.data
                .debounce(MIRROR_COALESCE_DELAY_MS)
                .collect(::syncAll)
        }
    }

    fun release() {
        released.set(true)
        if (started.compareAndSet(true, false)) {
            settingsObservationJob?.cancel()
        }
        settingsObservationJob = null
        mirrorScope.cancel()
    }

    private fun syncAll(settings: Preferences) {
        ElovaireTrace.section("settings_backup_checkpoint") {
            val desired = settings.portableValues()
            val current = backup.all.filterKeys(::isPortableSettingKey)
            val checksum = portableSettingsBackupChecksum(desired)
            val currentVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
            val currentChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
            if (
                desired == current &&
                currentVersion == BACKUP_FORMAT_VERSION &&
                currentChecksum == checksum
            ) return@section
            val editor = backup.edit()
            (current.keys - desired.keys).forEach(editor::remove)
            desired.forEach { (key, value) -> editor.putPreferenceValue(key, value) }
            editor.putInt(BACKUP_FORMAT_VERSION_KEY, BACKUP_FORMAT_VERSION)
            editor.putString(BACKUP_CHECKSUM_KEY, checksum)
            editor.putLong(BACKUP_CREATED_AT_KEY, clock.wallTimeMs())
            editor.apply()
        }
    }

    private fun isValidBackup(values: Map<String, *>): Boolean {
        val storedVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
        val storedChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
        if (storedVersion == 0 && storedChecksum == null) return true
        return storedVersion == BACKUP_FORMAT_VERSION &&
            storedChecksum == portableSettingsBackupChecksum(values)
    }

    private fun android.content.SharedPreferences.Editor.putPreferenceValue(key: String, value: Any?): android.content.SharedPreferences.Editor {
        return when (value) {
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value.filterIsInstance<String>().toSet())
            else -> remove(key)
        }
    }

    private fun MutablePreferences.putPortableValue(key: String, value: Any?) {
        return when (value) {
            is Boolean -> set(booleanPreferencesKey(key), value)
            is Float -> set(floatPreferencesKey(key), value)
            is Int -> set(intPreferencesKey(key), value)
            is Long -> set(longPreferencesKey(key), value)
            is String -> set(stringPreferencesKey(key), value)
            is Set<*> -> set(stringSetPreferencesKey(key), value.filterIsInstance<String>().toSet())
            else -> Unit
        }
    }

    private companion object {
        const val BACKUP_FILE_NAME = "portable_settings"
        const val BACKUP_FORMAT_VERSION_KEY = "_format_version"
        const val BACKUP_CHECKSUM_KEY = "_checksum"
        const val BACKUP_CREATED_AT_KEY = "_created_at_ms"
        const val BACKUP_FORMAT_VERSION = 1
        const val MIRROR_COALESCE_DELAY_MS = 400L
    }
}

private fun Preferences.portableValues(): Map<String, Any?> = asMap()
    .asSequence()
    .filter { (key, _) -> isPortableSettingKey(key.name) }
    .associate { (key, value) -> key.name to value }

internal fun portableSettingsBackupChecksum(values: Map<String, *>): String {
    val canonical = values
        .filterKeys(::isPortableSettingKey)
        .toSortedMap()
        .entries
        .joinToString("\n") { (key, value) ->
            val (type, encoded) = when (value) {
                is Boolean -> "boolean" to value.toString()
                is Float -> "float" to value.toString()
                is Int -> "int" to value.toString()
                is Long -> "long" to value.toString()
                is Set<*> -> "string_set" to value.filterIsInstance<String>().sorted().joinToString(",")
                else -> "string" to value?.toString().orEmpty()
            }
            "$key:$type:${encoded.length}:$encoded"
        }
    return MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}

internal fun isPortableSettingKey(key: String): Boolean = key in portableSettingKeys

private val portableSettingKeys = setOf(
            "theme_mode",
            "text_size_preset",
            "app_language",
            "playback_volume",
            "crossfade_enabled",
            "crossfade_duration_ms",
            "crossfade_silence_threshold_db",
            "volume_normalization_enabled",
            "online_lyrics_enabled",
            "album_collection_grid_enabled",
            "album_collection_layout_mode",
            "song_collection_grid_enabled",
            "album_collection_sort_mode",
            "song_collection_sort_mode",
            "eq_bands",
            "eq_bass",
            "eq_midrange",
            "eq_treble",
            "eq_spaciousness",
            "eq_spaciousness_mode",
            "mono_playback_enabled",
            "eq_reverb_duration_ms",
            "eq_reverb_profile",
)
