package elovaire.music.droidbeauty.app.core

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import java.util.concurrent.Executor

internal object DebugStrictModeInstaller {
    private const val CRASH_ON_STRICT_MODE_VIOLATION = false
    private val violationHandler = Handler(Looper.getMainLooper())
    private val violationExecutor = Executor { runnable ->
        violationHandler.post(runnable)
    }

    fun install() {
        StrictMode.setThreadPolicy(buildThreadPolicy())
        StrictMode.setVmPolicy(buildVmPolicy())
    }

    private fun buildThreadPolicy(): StrictMode.ThreadPolicy {
        val builder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectCustomSlowCalls()
            .detectResourceMismatches()
            .detectUnbufferedIo()
            .penaltyListener(violationExecutor) { violation ->
                StrictModeViolationRecorder.record(violation)
            }
            .penaltyLog()

        if (CRASH_ON_STRICT_MODE_VIOLATION) {
            builder.penaltyDeath()
        }

        return builder.build()
    }

    private fun buildVmPolicy(): StrictMode.VmPolicy {
        val builder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .detectFileUriExposure()
            .detectCleartextNetwork()
            .detectContentUriWithoutPermission()
            .detectUntaggedSockets()
            .detectCredentialProtectedWhileLocked()
            .detectImplicitDirectBoot()
            .penaltyListener(violationExecutor) { violation ->
                StrictModeViolationRecorder.record(violation)
            }
            .penaltyLog()

        builder.detectNonSdkApiUsage()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.detectIncorrectContextUse()
            builder.detectUnsafeIntentLaunch()
        }
        if (CRASH_ON_STRICT_MODE_VIOLATION) {
            builder.penaltyDeath()
        }

        return builder.build()
    }
}

internal inline fun <T> allowStrictModeDiskReads(block: () -> T): T {
    val previousPolicy = StrictMode.allowThreadDiskReads()
    return try {
        block()
    } finally {
        StrictMode.setThreadPolicy(previousPolicy)
    }
}

internal inline fun <T> allowStrictModeDiskWrites(block: () -> T): T {
    val previousPolicy = StrictMode.allowThreadDiskWrites()
    return try {
        block()
    } finally {
        StrictMode.setThreadPolicy(previousPolicy)
    }
}
