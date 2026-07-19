package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerAudioProcessorTest {
    @Test
    fun queueInput_copiesPcmThroughWhenEffectsAreOff() {
        val processor = EqualizerAudioProcessor()
        val input = ByteBuffer.allocateDirect(8)
            .order(ByteOrder.nativeOrder())
            .putShort((-12_000).toShort())
            .putShort(4_000.toShort())
            .putShort(8_000.toShort())
            .putShort((-2_000).toShort())
            .flip() as ByteBuffer
        val expected = ByteArray(input.remaining()).also { input.duplicate().get(it) }

        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(input)
        val output = processor.getOutput()
        val actual = ByteArray(output.remaining()).also(output::get)

        assertArrayEquals(expected, actual)
        assertTrue(processor.debugSnapshot().dspBypassed)
    }
}
