package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceStorage(context: Context) {
    val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFERENCE_FILE_NAME = "elovaire_preferences"
    }
}
