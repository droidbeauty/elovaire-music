package elovaire.music.app.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import elovaire.music.app.MainActivity
import java.io.File

class AppUpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED != intent.action) return
        clearDownloadedInstallers(context)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(launchIntent)
    }

    private fun clearDownloadedInstallers(context: Context) {
        runCatching {
            File(context.cacheDir, "updates").listFiles()?.forEach { file ->
                if (file.isFile && file.extension.equals("apk", ignoreCase = true)) {
                    file.delete()
                }
            }
        }
    }
}
