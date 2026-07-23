package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
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
        if (source.all.isEmpty() && backup.all.isNotEmpty()) {
            copyValues(backup, source, PORTABLE_KEYS)
        }
        syncAll()
    }

    fun start() {
        if (released.get()) return
        restore()
        if (released.get()) return
        if (!started.compareAndSet(false, true)) return
        source.registerOnSharedPreferenceChangeListener(this)
        if (released.get() && started.compareAndSet(true, false)) {
            source.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    fun release() {
        released.set(true)
        if (!started.compareAndSet(true, false)) return
        source.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == null || !isPortableSettingKey(key)) return
        copyValues(sharedPreferences, backup, setOf(key))
    }

    private fun syncAll() {
        val editor = backup.edit().clear()
        source.all.forEach { (key, value) ->
            if (isPortableSettingKey(key)) editor.putPreferenceValue(key, value)
        }
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
        val PORTABLE_KEYS = portableSettingKeys
    }
}

internal fun isPortableSettingKey(key: String): Boolean = key in portableSettingKeys

private val portableSettingKeys = setOf(
            "theme_mode",
            "text_size_preset",
            "app_language",
            "playback_volume",
            "gapless_playback_enabled",
            "volume_normalization_enabled",
            "online_lyrics_lookup_enabled",
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
