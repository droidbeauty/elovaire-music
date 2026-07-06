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
                file.uncommentedLines().mapNotNull { (index, line) ->
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

    private fun java.io.File.uncommentedLines(): List<Pair<Int, String>> {
        var inBlockComment = false
        return readLines().mapIndexed { index, line ->
            val builder = StringBuilder(line.length)
            var position = 0
            while (position < line.length) {
                when {
                    inBlockComment -> {
                        val end = line.indexOf("*/", position)
                        if (end == -1) {
                            position = line.length
                        } else {
                            inBlockComment = false
                            position = end + 2
                        }
                    }

                    line.startsWith("//", position) -> position = line.length
                    line.startsWith("/*", position) -> {
                        inBlockComment = true
                        position += 2
                    }

                    else -> {
                        builder.append(line[position])
                        position += 1
                    }
                }
            }
            index to builder.toString()
        }
    }
}
