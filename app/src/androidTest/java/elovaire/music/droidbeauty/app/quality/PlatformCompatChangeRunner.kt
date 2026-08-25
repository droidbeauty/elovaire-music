package elovaire.music.droidbeauty.app.quality

import android.app.Instrumentation
import android.os.ParcelFileDescriptor

internal data class CompatibilityChangeComparison(
    val changeName: String,
    val initialState: String,
    val enabledState: String,
    val resetState: String,
)

/**
 * Small instrumentation-only bridge for the documented `am compat` workflow.
 * Process restart is supplied by the caller because an instrumentation process
 * cannot safely force-stop its own target package.
 */
internal class PlatformCompatChangeRunner(
    private val instrumentation: Instrumentation,
) {
    private val targetContext = instrumentation.targetContext
    private val packageName = targetContext.packageName

    fun dumpPlatformCompat(): String = shell("dumpsys platform_compat")

    fun enable(changeName: String): String {
        validateChangeName(changeName)
        return shell("am compat enable $changeName $packageName")
    }

    fun reset(changeName: String): String {
        validateChangeName(changeName)
        return shell("am compat reset $changeName $packageName")
    }

    fun runWithRestart(
        changeName: String,
        restartTarget: () -> Unit,
        scenario: () -> Unit,
    ): CompatibilityChangeComparison {
        validateChangeName(changeName)
        val initialState = dumpPlatformCompat()
        enable(changeName)
        val enabledState: String
        try {
            restartTarget()
            enabledState = dumpPlatformCompat()
            scenario()
        } finally {
            reset(changeName)
            restartTarget()
        }
        val resetState = dumpPlatformCompat()
        return CompatibilityChangeComparison(
            changeName = changeName,
            initialState = initialState,
            enabledState = enabledState,
            resetState = resetState,
        )
    }

    private fun validateChangeName(changeName: String) {
        require(CHANGE_NAME_PATTERN.matches(changeName)) { "Invalid compatibility change name" }
    }

    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
            val reader = input.bufferedReader()
            buildString {
                while (length < MAX_SHELL_OUTPUT_CHARS) {
                    val line = reader.readLine() ?: break
                    appendLine(line)
                }
            }
        }
    }

    private companion object {
        val CHANGE_NAME_PATTERN = Regex("[A-Z0-9_]+")
        const val MAX_SHELL_OUTPUT_CHARS = 256 * 1024
    }
}
