import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class NativePageSizeValidationTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundleFile: RegularFileProperty

    @get:Input
    abstract val minPageSize: Property<Long>

    @TaskAction
    fun validate() {
        val bundle = bundleFile.asFile.get()
            .takeIf { file -> file.isFile && file.extension == "aab" }
            ?: throw GradleException("Release AAB was not generated.")
        var checkedLibraries = 0
        ZipFile(bundle).use { zip ->
            zip.entries().iterator().asSequence()
                .filter { entry -> !entry.isDirectory && entry.name.endsWith(".so") }
                .forEach { entry ->
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    checkElfLoadAlignment(entry.name, bytes, minPageSize.get())
                    checkedLibraries++
                }
        }
        if (checkedLibraries == 0) {
            throw GradleException("Release AAB does not contain native libraries to validate.")
        }
    }

    private fun checkElfLoadAlignment(
        name: String,
        bytes: ByteArray,
        minPageSize: Long,
    ) {
        if (
            bytes.size < 64 ||
            bytes[0] != 0x7f.toByte() ||
            bytes[1] != 'E'.code.toByte() ||
            bytes[2] != 'L'.code.toByte() ||
            bytes[3] != 'F'.code.toByte()
        ) {
            throw GradleException("$name is not a valid ELF shared object.")
        }
        val elfClass = bytes[4].toInt()
        val byteOrder = when (bytes[5].toInt()) {
            1 -> ByteOrder.LITTLE_ENDIAN
            2 -> ByteOrder.BIG_ENDIAN
            else -> throw GradleException("$name has an unsupported ELF byte order.")
        }
        val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
        val programHeaderOffset = when (elfClass) {
            1 -> buffer.getInt(28).toLong()
            2 -> buffer.getLong(32)
            else -> throw GradleException("$name has an unsupported ELF class.")
        }
        val programHeaderEntrySize = when (elfClass) {
            1 -> buffer.getShort(42).toInt()
            else -> buffer.getShort(54).toInt()
        }
        val programHeaderCount = when (elfClass) {
            1 -> buffer.getShort(44).toInt()
            else -> buffer.getShort(56).toInt()
        }
        repeat(programHeaderCount) { index ->
            val headerOffset = programHeaderOffset + index.toLong() * programHeaderEntrySize
            if (headerOffset < 0 || headerOffset + programHeaderEntrySize > bytes.size) {
                throw GradleException("$name has an invalid ELF program header table.")
            }
            val header = headerOffset.toInt()
            val type = buffer.getInt(header)
            if (type == PT_LOAD) {
                val alignment = when (elfClass) {
                    1 -> buffer.getInt(header + 28).toLong()
                    else -> buffer.getLong(header + 48)
                }
                if (alignment < minPageSize) {
                    throw GradleException("$name has PT_LOAD alignment $alignment, expected at least $minPageSize.")
                }
            }
        }
    }

    private companion object {
        const val PT_LOAD = 1
    }
}
