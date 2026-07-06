import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class DeprecatedAndroidApiUsageCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    @TaskAction
    fun checkSources() {
        val root = projectDirectory.asFile.get()
        val violations = sourceFiles.files
            .filter { file -> file.isFile }
            .flatMap { file ->
                val relativePath = file.relativeTo(root).path.replace(File.separatorChar, '/')
                file.uncommentedLines().mapNotNull { (index, line) ->
                    val pattern = riskyPatterns.firstOrNull { rule ->
                        line.contains(rule.pattern) && rule.allowedPathSuffixes.none(relativePath::endsWith)
                    }
                    pattern?.let { "${file.relativeTo(root)}:${index + 1}: ${it.pattern}" }
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Deprecated Android API usage must stay behind approved compatibility shims:\n" +
                    violations.joinToString("\n"),
            )
        }
    }

    private fun File.uncommentedLines(): List<Pair<Int, String>> {
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

    private companion object {
        val riskyPatterns = listOf(
            RiskyPattern(
                pattern = "Environment.getExternalStorageDirectory",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/data/library/MediaFilePathResolver.kt"),
            ),
            RiskyPattern(
                pattern = "Environment.getExternalStoragePublicDirectory",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/data/library/MediaFilePathResolver.kt"),
            ),
            RiskyPattern(
                pattern = "MediaStore.MediaColumns.DATA",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/data/library/MediaFilePathResolver.kt"),
            ),
            RiskyPattern(
                pattern = ".getParcelableExtra(",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/core/AndroidApiCompat.kt"),
            ),
            RiskyPattern(
                pattern = ".getSerializableExtra(",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/core/AndroidApiCompat.kt"),
            ),
            RiskyPattern(
                pattern = "@Suppress(\"DEPRECATION\")",
                allowedPathSuffixes = listOf("src/main/java/com/elovaire/music/data/library/MediaFilePathResolver.kt"),
            ),
            RiskyPattern(
                pattern = "@SuppressLint(\"DEPRECATION\")",
                allowedPathSuffixes = emptyList(),
            ),
        )
    }

    private data class RiskyPattern(
        val pattern: String,
        val allowedPathSuffixes: List<String>,
    )
}
