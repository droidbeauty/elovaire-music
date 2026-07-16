import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ResourceStructureCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val assetFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val profileFiles: ConfigurableFileCollection

    @TaskAction
    fun checkResources() {
        val violations = mutableListOf<String>()
        val packagedFiles = (resourceFiles.files + assetFiles.files).filter(File::isFile)
        packagedFiles.filter { it.name.startsWith(".") }.forEach { file ->
            violations += "Packaged resources must not contain hidden metadata files: ${file.invariantSeparatorsPath}"
        }
        resourceFiles.files.filter(File::isFile).forEach { file ->
            if (!RESOURCE_NAME.matches(file.nameWithoutExtension)) {
                violations += "Invalid Android resource name: ${file.invariantSeparatorsPath}"
            }
            if (file.extension == "xml") validateXml(file, violations)
        }
        validateProfiles(violations)
        if (violations.isNotEmpty()) throw GradleException(violations.joinToString(separator = "\n"))
    }

    private fun validateXml(file: File, violations: MutableList<String>) {
        try {
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }.newDocumentBuilder().parse(file)
        } catch (failure: Exception) {
            violations += "Invalid XML resource ${file.invariantSeparatorsPath}: ${failure.message}"
        }
    }

    private fun validateProfiles(violations: MutableList<String>) {
        val sourceTypes = buildSet {
            sourceFiles.files.filter { it.isFile && it.extension == "kt" }.forEach { file ->
                val text = file.readText()
                val packageName = PACKAGE.find(text)?.groupValues?.get(1).orEmpty()
                if (packageName.isBlank()) return@forEach
                add("$packageName.${file.nameWithoutExtension}Kt")
                TYPE.findAll(text).forEach { match ->
                    add("$packageName.${match.groupValues[1]}")
                }
            }
        }
        profileFiles.files.filter(File::isFile).forEach { profile ->
            profile.readLines().forEachIndexed { index, line ->
                val className = PROFILE_CLASS.find(line)?.groupValues?.get(1)?.replace('/', '.') ?: return@forEachIndexed
                if (className !in sourceTypes) {
                    violations += "${profile.invariantSeparatorsPath}:${index + 1} references missing source type $className"
                }
            }
        }
    }

    private companion object {
        val RESOURCE_NAME = Regex("""[a-z][a-z0-9_]*""")
        val PACKAGE = Regex("""(?m)^package\s+([\w.]+)""")
        val TYPE = Regex(
            """(?m)^(?:internal\s+|public\s+|private\s+|protected\s+)?""" +
                """(?:data\s+|sealed\s+|value\s+|enum\s+|annotation\s+|fun\s+)?""" +
                """(?:class|interface|object)\s+(\w+)""",
        )
        val PROFILE_CLASS = Regex("""^L([^;]+);""")
    }
}
