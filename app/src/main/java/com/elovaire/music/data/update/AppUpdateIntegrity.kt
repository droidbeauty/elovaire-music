package elovaire.music.droidbeauty.app.data.update

import java.io.File
import java.security.MessageDigest
import java.util.Locale

internal object AppUpdateIntegrity {
    const val MAX_APK_BYTES = 512L * 1024L * 1024L

    fun isSafeApkFileName(fileName: String): Boolean {
        return fileName.isNotBlank() &&
            fileName.length <= MAX_FILE_NAME_LENGTH &&
            fileName == fileName.trim() &&
            fileName.endsWith(".apk", ignoreCase = true) &&
            File(fileName).name == fileName &&
            '/' !in fileName &&
            '\\' !in fileName &&
            '\u0000' !in fileName
    }

    fun downloadLimit(
        contentLengthBytes: Long?,
        assetSizeBytes: Long?,
    ): Long {
        val declared = listOfNotNull(contentLengthBytes, assetSizeBytes)
        require(declared.all { it in 1L..MAX_APK_BYTES }) { "Update download is too large" }
        return declared.minOrNull() ?: MAX_APK_BYTES
    }

    fun checkedDownloadedByteCount(
        copiedBytes: Long,
        readBytes: Int,
        limitBytes: Long,
    ): Long {
        require(copiedBytes >= 0L && readBytes >= 0 && limitBytes > 0L)
        val next = copiedBytes + readBytes
        require(next >= copiedBytes && next <= limitBytes) { "Update download exceeded its byte limit" }
        return next
    }

    fun expectedSha256(checksumText: String, apkFileName: String): String? {
        val normalizedName = apkFileName.trim()
        val entries = checksumText.lineSequence().mapNotNull(::parseChecksumLine).toList()
        val namedDigests = entries
            .filter { entry -> entry.fileName?.substringAfterLast('/')?.trim() == normalizedName }
            .map(ChecksumEntry::digest)
            .distinct()
        if (namedDigests.size == 1) return namedDigests.single()
        return entries.singleOrNull { it.fileName == null }?.digest.takeIf { entries.size == 1 }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    fun verifySha256(file: File, expectedSha256: String): Boolean {
        val normalized = expectedSha256.trim().lowercase(Locale.ROOT)
        return SHA_256_REGEX.matches(normalized) && sha256(file).equals(normalized, ignoreCase = true)
    }

    private fun parseChecksumLine(rawLine: String): ChecksumEntry? {
        val line = rawLine.substringBefore('#').trim()
        val match = CHECKSUM_LINE_REGEX.matchEntire(line) ?: return null
        return ChecksumEntry(
            digest = match.groupValues[1].lowercase(Locale.ROOT),
            fileName = match.groupValues[2].trim().trimStart('*').takeIf(String::isNotBlank),
        )
    }

    private val SHA_256_REGEX = Regex("""(?i)\b[a-f0-9]{64}\b""")
    private val CHECKSUM_LINE_REGEX = Regex("""(?i)^([a-f0-9]{64})(?:\s+(.+))?$""")
    private const val MAX_FILE_NAME_LENGTH = 255

    private data class ChecksumEntry(
        val digest: String,
        val fileName: String?,
    )
}
