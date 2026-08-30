package elovaire.music.droidbeauty.app.data.library.network

import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkCredentialEnvelopeTest {
    @Test
    fun currentEnvelope_roundTripsExactCiphertext() {
        val iv = ByteArray(12) { it.toByte() }
        val ciphertext = ByteArray(32) { (it + 1).toByte() }

        val result = NetworkCredentialEnvelope.decode(NetworkCredentialEnvelope.encode(iv, ciphertext))

        assertTrue(result is NetworkCredentialEnvelopeResult.Current)
        result as NetworkCredentialEnvelopeResult.Current
        assertTrue(iv.contentEquals(result.iv))
        assertTrue(ciphertext.contentEquals(result.ciphertext))
    }

    @Test
    fun legacyEnvelope_isRecognizedForMigration() {
        val iv = ByteArray(16)
        val ciphertext = ByteArray(16)
        val bytes = ByteBuffer.allocate(4 + iv.size + ciphertext.size)
            .putInt(iv.size)
            .put(iv)
            .put(ciphertext)
            .array()

        val result = NetworkCredentialEnvelope.decode(bytes)

        assertTrue(result is NetworkCredentialEnvelopeResult.Legacy)
        result as NetworkCredentialEnvelopeResult.Legacy
        assertTrue(iv.contentEquals(result.iv))
        assertTrue(ciphertext.contentEquals(result.ciphertext))
    }

    @Test
    fun malformedEnvelope_isRejectedBeforeDecodeAllocation() {
        val bytes = ByteBuffer.allocate(6)
            .putInt(0x454C5652)
            .put(2)
            .put(64)
            .array()

        val result = NetworkCredentialEnvelope.decode(bytes)

        assertEquals(
            NetworkCredentialEnvelopeError.InvalidIvLength,
            (result as NetworkCredentialEnvelopeResult.Invalid).reason,
        )
    }

    @Test
    fun oversizedEnvelope_isRejected() {
        val result = NetworkCredentialEnvelope.decode(ByteArray(NetworkCredentialEnvelope.MAX_SIZE + 1))

        assertEquals(
            NetworkCredentialEnvelopeError.TooLarge,
            (result as NetworkCredentialEnvelopeResult.Invalid).reason,
        )
    }
}
