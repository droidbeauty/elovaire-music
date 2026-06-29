import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class HiddenApiUsageCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Input
    abstract val riskyPatterns: ListProperty<String>

    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    @TaskAction
    fun checkSources() {
        val patterns = riskyPatterns.get()
        val root = projectDirectory.asFile.get()
        val violations = sourceFiles.files
            .filter { it.isFile }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    patterns
                        .firstOrNull { pattern -> line.contains(pattern) }
                        ?.let { pattern -> "${file.relativeTo(root)}:${index + 1}: $pattern" }
                }
            }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Potential hidden/non-SDK API usage detected:\n${violations.joinToString("\n")}",
            )
        }
    }
}
