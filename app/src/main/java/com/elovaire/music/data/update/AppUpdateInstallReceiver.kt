package elovaire.music.app.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import elovaire.music.app.MainActivity

class AppUpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED != intent.action) return
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(launchIntent)
    }
}
