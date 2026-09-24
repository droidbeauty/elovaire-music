package elovaire.music.droidbeauty.app.widget

import android.content.Context
import android.content.Intent
import elovaire.music.droidbeauty.app.MainActivity
import elovaire.music.droidbeauty.app.data.playback.EXTRA_OPEN_PLAYER_FROM_NOTIFICATION

internal object WidgetLaunchIntentFactory {
    fun openApp(context: Context): Intent = baseIntent(context)

    fun openNowPlaying(context: Context): Intent = baseIntent(context).apply {
        putExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, true)
    }

    private fun baseIntent(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
}
