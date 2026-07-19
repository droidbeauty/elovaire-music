import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class BaselineProfileResultCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val testResultFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatedProfileFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val results = testResultFiles.files.filter { it.isFile }
        check(results.isNotEmpty()) { "Baseline Profile generation produced no instrumentation result." }
        check(results.none { "<skipped" in it.readText() }) {
            "Baseline Profile generation was skipped; run it on a supported device or emulator."
        }
        val profiles = generatedProfileFiles.files.filter { it.isFile }
        check(profiles.isNotEmpty()) { "Baseline Profile generation produced no profile." }
        profiles.forEach { profile ->
            val profileText = profile.readText()
            check(profileText.lineSequence().count() > 100 && "Lelovaire/music/droidbeauty/app/" in profileText) {
                "Generated Baseline Profile is empty or does not contain Elovaire startup code."
            }
        }
    }
}
