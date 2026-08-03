import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ReleaseArtifactIntegrityTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencyInventoryFile: RegularFileProperty

    @get:OutputFile
    abstract val checksumFile: RegularFileProperty

    @TaskAction
    fun inspect() {
        val apk = apkFile.asFile.get().takeIf { it.isFile }
            ?: throw GradleException("Release APK was not generated.")
        requireNonEmpty(mappingFile.asFile.get(), "R8 mapping")
        requireNonEmpty(dependencyInventoryFile.asFile.get(), "release dependency inventory")

        ZipFile(apk).use { zip ->
            val entries = zip.entries().iterator().asSequence().filterNot { it.isDirectory }.toList()
            val names = entries.map { it.name }
            if (names.size != names.toSet().size) throw GradleException("Release APK contains duplicate entries.")
            FORBIDDEN_NAMES.forEach { forbidden ->
                if (names.any { it.endsWith(forbidden, ignoreCase = true) }) {
                    throw GradleException("Release APK contains forbidden entry: $forbidden")
                }
            }
            entries
                .filter { entry -> entry.name.endsWith(".dex") || entry.name.endsWith(".pb") }
                .forEach { entry ->
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    FORBIDDEN_BINARY_MARKERS.firstOrNull(bytes::containsAscii)?.let { marker ->
                        throw GradleException("Release APK contains forbidden code or data: $marker")
                    }
                }
            REQUIRED_ENTRIES.forEach { required ->
                val entry = zip.getEntry(required)
                    ?: throw GradleException("Release APK is missing required entry: $required")
                if (entry.size <= 0L) throw GradleException("Release APK contains empty entry: $required")
            }
            val timestamps = entries.mapNotNull { it.time.takeIf { time -> time >= 0L } }.distinct()
            if (timestamps.size > 1) {
                throw GradleException("Release APK entries have nondeterministic timestamps.")
            }
            if (names.none { it.contains("/LICENSE") }) {
                throw GradleException("Release APK contains no packaged dependency license inventory.")
            }
        }

        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(apk.readBytes())
            .joinToString("") { byte -> "%02x".format(byte) }
        checksumFile.asFile.get().apply {
            parentFile.mkdirs()
            writeText("$checksum  ${apk.name}\n")
        }
    }

    private fun requireNonEmpty(file: java.io.File, label: String) {
        if (!file.isFile || file.length() <= 0L) throw GradleException("Release $label was not generated.")
    }

    private companion object {
        val FORBIDDEN_NAMES = listOf(".DS_Store", "local.properties", "DebugProbesKt.bin", ".keystore", ".jks")
        val FORBIDDEN_BINARY_MARKERS = listOf(
            "AppUpdateInstallReceiver",
            "LeakCanary",
            "ComposeViewAdapter",
        )
        val REQUIRED_ENTRIES = listOf(
            "AndroidManifest.xml",
            "resources.arsc",
            "classes.dex",
            "assets/dexopt/baseline.prof",
            "assets/dexopt/baseline.profm",
            "assets/licenses/THIRD_PARTY_NOTICES.txt",
        )
    }
}

private fun ByteArray.containsAscii(value: String): Boolean {
    val needle = value.toByteArray(Charsets.US_ASCII)
    if (needle.isEmpty() || needle.size > size) return false
    for (start in 0..size - needle.size) {
        var matches = true
        for (offset in needle.indices) {
            if (this[start + offset] != needle[offset]) {
                matches = false
                break
            }
        }
        if (matches) return true
    }
    return false
}
