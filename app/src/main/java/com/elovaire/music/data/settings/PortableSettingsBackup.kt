package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

internal class PortableSettingsBackup(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val appContext = context.applicationContext
    private val source = allowStrictModeDiskReads {
        appContext.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
    }
    private val backup = allowStrictModeDiskReads {
        appContext.getSharedPreferences(BACKUP_FILE_NAME, Context.MODE_PRIVATE)
    }
    private val restored = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)

    fun restore() {
        if (released.get()) return
        if (!restored.compareAndSet(false, true)) return
        allowStrictModeDiskReads {
            val backupValues = backup.all.filterKeys(::isPortableSettingKey)
            if (source.all.isEmpty() && backupValues.isNotEmpty() && isValidBackup(backupValues)) {
                copyValues(backup, source, PORTABLE_KEYS)
            }
            syncAll()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun start() {
        if (released.get()) return
        restore()
        if (released.get()) return
        if (!started.compareAndSet(false, true)) return
        try {
            source.registerOnSharedPreferenceChangeListener(this)
        } catch (failure: RuntimeException) {
            started.set(false)
            runCatching { source.unregisterOnSharedPreferenceChangeListener(this) }
            throw failure
        }
        if (released.get() && started.compareAndSet(true, false)) {
            runCatching { source.unregisterOnSharedPreferenceChangeListener(this) }
        }
    }

    fun release() {
        released.set(true)
        if (!started.compareAndSet(true, false)) return
        source.unregisterOnSharedPreferenceChangeListener(this)
    }

    @Suppress("UNUSED_PARAMETER")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == null || !isPortableSettingKey(key)) return
        syncAll()
    }

    private fun syncAll() {
        val desired = source.all.filterKeys(::isPortableSettingKey)
        val current = backup.all.filterKeys(::isPortableSettingKey)
        val checksum = portableSettingsBackupChecksum(desired)
        val currentVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
        val currentChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
        if (
            desired == current &&
            currentVersion == BACKUP_FORMAT_VERSION &&
            currentChecksum == checksum
        ) return
        val editor = backup.edit()
        (current.keys - desired.keys).forEach(editor::remove)
        desired.forEach { (key, value) -> editor.putPreferenceValue(key, value) }
        editor.putInt(BACKUP_FORMAT_VERSION_KEY, BACKUP_FORMAT_VERSION)
        editor.putString(BACKUP_CHECKSUM_KEY, checksum)
        editor.putLong(BACKUP_CREATED_AT_KEY, System.currentTimeMillis())
        editor.apply()
    }

    private fun copyValues(from: SharedPreferences, to: SharedPreferences, keys: Set<String>) {
        val values = from.all
        val editor = to.edit()
        keys.forEach { key ->
            if (key in values) editor.putPreferenceValue(key, values[key]) else editor.remove(key)
        }
        editor.apply()
    }

    private fun isValidBackup(values: Map<String, *>): Boolean {
        val storedVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
        val storedChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
        if (storedVersion == 0 && storedChecksum == null) return true
        return storedVersion == BACKUP_FORMAT_VERSION &&
            storedChecksum == portableSettingsBackupChecksum(values)
    }

    private fun SharedPreferences.Editor.putPreferenceValue(key: String, value: Any?): SharedPreferences.Editor {
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

    private companion object {
        const val BACKUP_FILE_NAME = "portable_settings"
        const val BACKUP_FORMAT_VERSION_KEY = "_format_version"
        const val BACKUP_CHECKSUM_KEY = "_checksum"
        const val BACKUP_CREATED_AT_KEY = "_created_at_ms"
        const val BACKUP_FORMAT_VERSION = 1
        val PORTABLE_KEYS = portableSettingKeys
    }
}

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
