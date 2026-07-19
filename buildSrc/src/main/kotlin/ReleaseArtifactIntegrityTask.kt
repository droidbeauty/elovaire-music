import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ReleaseArtifactIntegrityTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundleFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val nativeSymbolsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencyInventoryFile: RegularFileProperty

    @get:org.gradle.api.tasks.Input
    abstract val expectedAbis: ListProperty<String>

    @get:OutputFile
    abstract val checksumFile: RegularFileProperty

    @TaskAction
    fun inspect() {
        val bundle = bundleFile.asFile.get().takeIf { it.isFile }
            ?: throw GradleException("Release AAB was not generated.")
        requireNonEmpty(mappingFile.asFile.get(), "R8 mapping")
        requireNonEmpty(nativeSymbolsFile.asFile.get(), "native symbols")
        requireNonEmpty(dependencyInventoryFile.asFile.get(), "release dependency inventory")

        ZipFile(bundle).use { zip ->
            val entries = zip.entries().iterator().asSequence().filterNot { it.isDirectory }.toList()
            val names = entries.map { it.name }
            if (names.size != names.toSet().size) throw GradleException("Release AAB contains duplicate entries.")
            FORBIDDEN_NAMES.forEach { forbidden ->
                if (names.any { it.endsWith(forbidden, ignoreCase = true) }) {
                    throw GradleException("Release AAB contains forbidden entry: $forbidden")
                }
            }
            entries
                .filter { entry -> entry.name.endsWith(".dex") || entry.name.endsWith(".pb") }
                .forEach { entry ->
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    FORBIDDEN_BINARY_MARKERS.firstOrNull(bytes::containsAscii)?.let { marker ->
                        throw GradleException("Release AAB contains forbidden code or data: $marker")
                    }
                }
            REQUIRED_ENTRIES.forEach { required ->
                val entry = zip.getEntry(required)
                    ?: throw GradleException("Release AAB is missing required entry: $required")
                if (entry.size <= 0L) throw GradleException("Release AAB contains empty entry: $required")
            }
            expectedAbis.get().forEach { abi ->
                val library = "base/lib/$abi/libelovaire_chromaprint.so"
                if (zip.getEntry(library)?.size?.let { it > 0L } != true) {
                    throw GradleException("Release AAB is missing native library for $abi.")
                }
            }
            val timestamps = entries.mapNotNull { it.time.takeIf { time -> time >= 0L } }.distinct()
            if (timestamps.size > 1) {
                throw GradleException("Release AAB entries have nondeterministic timestamps.")
            }
            if (names.none { it.contains("/LICENSE") }) {
                throw GradleException("Release AAB contains no packaged dependency license inventory.")
            }
            val signatureFiles = names.filter { it.startsWith("META-INF/") }
            if (
                signatureFiles.none { it.endsWith(".SF", ignoreCase = true) } ||
                signatureFiles.none { name ->
                    name.endsWith(".RSA", ignoreCase = true) ||
                        name.endsWith(".DSA", ignoreCase = true) ||
                        name.endsWith(".EC", ignoreCase = true)
                }
            ) {
                throw GradleException("Release AAB is unsigned; configure the Play upload key before release.")
            }
        }

        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(bundle.readBytes())
            .joinToString("") { byte -> "%02x".format(byte) }
        checksumFile.asFile.get().apply {
            parentFile.mkdirs()
            writeText("$checksum  ${bundle.name}\n")
        }
    }

    private fun requireNonEmpty(file: java.io.File, label: String) {
        if (!file.isFile || file.length() <= 0L) throw GradleException("Release $label was not generated.")
    }

    private companion object {
        val FORBIDDEN_NAMES = listOf(".DS_Store", "local.properties", "DebugProbesKt.bin", ".keystore", ".jks")
        val FORBIDDEN_BINARY_MARKERS = listOf(
            "AppUpdateManager",
            "AppUpdateInstallReceiver",
            "application/vnd.android.package-archive",
            "browser_download_url",
            "releases/latest",
            "LeakCanary",
            "ComposeViewAdapter",
        )
        val REQUIRED_ENTRIES = listOf(
            "BundleConfig.pb",
            "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof",
            "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.profm",
            "BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb",
            "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map",
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
