package elovaire.music.droidbeauty.app.data.library.network

import java.nio.ByteBuffer

internal sealed interface NetworkCredentialEnvelopeResult {
    data class Current(val iv: ByteArray, val ciphertext: ByteArray) : NetworkCredentialEnvelopeResult
    data class Legacy(val iv: ByteArray, val ciphertext: ByteArray) : NetworkCredentialEnvelopeResult
    data class Invalid(val reason: NetworkCredentialEnvelopeError) : NetworkCredentialEnvelopeResult
}

internal enum class NetworkCredentialEnvelopeError {
    TooShort,
    UnsupportedVersion,
    InvalidIvLength,
    InvalidCiphertext,
    TooLarge,
}

internal object NetworkCredentialEnvelope {
    private const val MAGIC = 0x454C5652
    private const val VERSION = 2
    private const val HEADER_SIZE = Int.SIZE_BYTES + 2
    private const val LEGACY_HEADER_SIZE = Int.SIZE_BYTES
    private const val MIN_IV_SIZE = 12
    private const val MAX_IV_SIZE = 32
    private const val GCM_TAG_SIZE = 16
    const val MAX_SIZE = 64 * 1024

    fun encode(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        require(iv.size in MIN_IV_SIZE..MAX_IV_SIZE)
        require(ciphertext.size >= GCM_TAG_SIZE)
        require(HEADER_SIZE + iv.size + ciphertext.size <= MAX_SIZE)
        return ByteBuffer.allocate(HEADER_SIZE + iv.size + ciphertext.size)
            .putInt(MAGIC)
            .put(VERSION.toByte())
            .put(iv.size.toByte())
            .put(iv)
            .put(ciphertext)
            .array()
    }

    fun decode(bytes: ByteArray): NetworkCredentialEnvelopeResult {
        if (bytes.size > MAX_SIZE) return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.TooLarge)
        if (bytes.size >= HEADER_SIZE && ByteBuffer.wrap(bytes).int == MAGIC) {
            return decodeCurrent(bytes)
        }
        return decodeLegacy(bytes)
    }

    private fun decodeCurrent(bytes: ByteArray): NetworkCredentialEnvelopeResult {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.int
        val version = buffer.get().toInt() and 0xff
        if (version != VERSION) return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.UnsupportedVersion)
        val ivSize = buffer.get().toInt() and 0xff
        if (ivSize !in MIN_IV_SIZE..MAX_IV_SIZE) {
            return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.InvalidIvLength)
        }
        if (buffer.remaining() < ivSize + GCM_TAG_SIZE) {
            return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.InvalidCiphertext)
        }
        val iv = ByteArray(ivSize).also(buffer::get)
        val ciphertext = ByteArray(buffer.remaining()).also(buffer::get)
        return NetworkCredentialEnvelopeResult.Current(iv, ciphertext)
    }

    private fun decodeLegacy(bytes: ByteArray): NetworkCredentialEnvelopeResult {
        if (bytes.size < LEGACY_HEADER_SIZE) return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.TooShort)
        val buffer = ByteBuffer.wrap(bytes)
        val ivSize = buffer.int
        if (ivSize !in MIN_IV_SIZE..MAX_IV_SIZE) {
            return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.InvalidIvLength)
        }
        if (buffer.remaining() < ivSize + GCM_TAG_SIZE) {
            return NetworkCredentialEnvelopeResult.Invalid(NetworkCredentialEnvelopeError.InvalidCiphertext)
        }
        val iv = ByteArray(ivSize).also(buffer::get)
        val ciphertext = ByteArray(buffer.remaining()).also(buffer::get)
        return NetworkCredentialEnvelopeResult.Legacy(iv, ciphertext)
    }
}
