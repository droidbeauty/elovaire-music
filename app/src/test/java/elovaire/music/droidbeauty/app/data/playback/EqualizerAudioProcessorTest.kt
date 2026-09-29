package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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

    @Test
    fun queueInput_processesOnlyActiveEqBands() {
        val processor = EqualizerAudioProcessor()
        processor.updateSettings(
            EqSettings(bands = List(EqualizerDspModel.BAND_COUNT) { index -> if (index == 3) 0.6f else 0f }),
        )
        val input = ByteBuffer.allocateDirect(16)
            .order(ByteOrder.nativeOrder())
            .putShort(8_000.toShort())
            .putShort((-4_000).toShort())
            .putShort(6_000.toShort())
            .putShort((-3_000).toShort())
            .putShort(4_000.toShort())
            .putShort((-2_000).toShort())
            .putShort(2_000.toShort())
            .putShort((-1_000).toShort())
            .flip() as ByteBuffer

        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        processor.queueInput(input)

        val diagnostics = processor.debugSnapshot()
        assertEquals(1, diagnostics.activeFilterCount)
        assertEquals(1L, diagnostics.coefficientPlanBuilds)
        assertEquals(52L, diagnostics.coefficientApplications)
    }

    @Test
    fun controlThreadSettingsAreAppliedOnTheAudioThread() {
        val processor = EqualizerAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
        val activeSettings = EqSettings(
            bands = List(EqualizerDspModel.BAND_COUNT) { index -> if (index == 3) 0.6f else 0f },
        )
        val controlThread = Thread {
            repeat(200) { index ->
                processor.updateSettings(if (index % 2 == 0) EqSettings() else activeSettings)
            }
            processor.updateSettings(activeSettings)
        }
        val audioThread = Thread {
            repeat(200) {
                processor.queueInput(pcmBuffer())
                processor.getOutput()
            }
        }

        controlThread.start()
        audioThread.start()
        controlThread.join()
        audioThread.join()
        processor.queueInput(pcmBuffer())

        assertEquals(1, processor.debugSnapshot().activeFilterCount)
    }

    private fun pcmBuffer(): ByteBuffer = ByteBuffer.allocateDirect(16)
        .order(ByteOrder.nativeOrder())
        .putShort(8_000.toShort())
        .putShort((-4_000).toShort())
        .putShort(6_000.toShort())
        .putShort((-3_000).toShort())
        .putShort(4_000.toShort())
        .putShort((-2_000).toShort())
        .putShort(2_000.toShort())
        .putShort((-1_000).toShort())
        .flip() as ByteBuffer
}
