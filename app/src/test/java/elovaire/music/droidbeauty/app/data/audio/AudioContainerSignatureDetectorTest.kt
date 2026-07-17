package elovaire.music.droidbeauty.app.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class AudioContainerSignatureDetectorTest {
    @Test
    fun detectsSupportedContainerSignatures() {
        assertSignature(AudioContainerSignature.Flac, "fLaC")
        assertSignature(AudioContainerSignature.Wav, "RIFF\u0000\u0000\u0000\u0000WAVE")
        assertSignature(AudioContainerSignature.Mp4, "\u0000\u0000\u0000\u0018ftypM4A ")
        assertSignature(AudioContainerSignature.ThreeGp, "\u0000\u0000\u0000\u0018ftyp3gp6")
        assertSignature(AudioContainerSignature.Amr, "#!AMR\n")
        assertSignature(AudioContainerSignature.Matroska, byteArrayOf(0x1a, 0x45, 0xdf.toByte(), 0xa3.toByte()))
    }

    @Test
    fun distinguishesOggMappingsFromHeaders() {
        assertSignature(AudioContainerSignature.OggOpus, "OggS----OpusHead")
        assertSignature(AudioContainerSignature.OggVorbis, "OggS----\u0001vorbis")
        assertSignature(AudioContainerSignature.OggFlac, "OggS----\u007fFLAC")
        assertNull(AudioContainerSignatureDetector.detect("OggS----unknown".toByteArray()))
    }

    @Test
    fun truncatedOrMalformedInputFailsClosed() {
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf()))
        assertNull(AudioContainerSignatureDetector.detect(byteArrayOf(0xff.toByte())))
        assertNull(AudioContainerSignatureDetector.detect("not audio".toByteArray()))
    }

    @Test
    fun seededMalformedInputsAlwaysTerminate() {
        val random = Random(0xE10A1E)
        repeat(500) {
            val bytes = random.nextBytes(random.nextInt(0, 2_048))
            AudioContainerSignatureDetector.detect(bytes)
        }
    }

    private fun assertSignature(expected: AudioContainerSignature, input: String) {
        assertSignature(expected, input.toByteArray(Charsets.ISO_8859_1))
    }

    private fun assertSignature(expected: AudioContainerSignature, input: ByteArray) {
        assertEquals(expected, AudioContainerSignatureDetector.detect(input))
    }
}
