package elovaire.music.droidbeauty.app.data.update

import java.io.File
import java.security.MessageDigest
import java.util.Locale

internal object AppUpdateIntegrity {
    fun expectedSha256(
        checksumText: String,
        apkFileName: String,
    ): String? {
        val normalizedName = apkFileName.trim()
        return checksumText
            .lineSequence()
            .mapNotNull { line -> parseChecksumLine(line, normalizedName) }
            .firstOrNull()
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
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun verifySha256(
        file: File,
        expectedSha256: String,
    ): Boolean {
        val normalizedExpected = expectedSha256.trim().lowercase(Locale.ROOT)
        if (!SHA_256_REGEX.matches(normalizedExpected)) return false
        return sha256(file).equals(normalizedExpected, ignoreCase = true)
    }

    private fun parseChecksumLine(
        rawLine: String,
        apkFileName: String,
    ): String? {
        val line = rawLine.substringBefore('#').trim()
        if (line.isBlank()) return null
        val match = SHA_256_REGEX.find(line) ?: return null
        val digest = match.value.lowercase(Locale.ROOT)
        val remainder = line.removeRange(match.range).trim().trimStart('*')
        return when {
            remainder.isBlank() -> digest
            remainder.substringAfterLast('/').trim() == apkFileName -> digest
            else -> null
        }
    }

    private val SHA_256_REGEX = Regex("""(?i)\b[a-f0-9]{64}\b""")
}
