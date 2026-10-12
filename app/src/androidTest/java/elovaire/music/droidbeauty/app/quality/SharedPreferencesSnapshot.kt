package elovaire.music.droidbeauty.app.quality

import android.content.SharedPreferences

internal class SharedPreferencesSnapshot private constructor(
    private val values: Map<String, Any?>,
) {
    fun restore(preferences: SharedPreferences) {
        val editor = preferences.edit().clear()
        values.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.map { item ->
                    item as? String ?: error("Unsupported SharedPreferences set value for $key.")
                }.toSet())
                else -> error("Unsupported SharedPreferences value for $key.")
            }
        }
        check(editor.commit()) { "Unable to restore SharedPreferences after instrumentation." }
    }

    companion object {
        fun capture(preferences: SharedPreferences): SharedPreferencesSnapshot =
            SharedPreferencesSnapshot(preferences.all.mapValues { (_, value) ->
                if (value is Set<*>) value.toSet() else value
            })
    }
}
