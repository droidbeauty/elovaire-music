package elovaire.music.droidbeauty.app.data.playback

import android.os.SystemClock
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.UpscalerSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class UpscalerPerformanceInstrumentedTest {
    @Test
    fun processingCostAndDurationForStereoAndMultichannel() {
        for ((channels, factor) in listOf(2 to 4, 8 to 8)) {
            fun process(): Long {
                val processor = UpscalerAudioProcessor(UpscalerSettings(true, factor, highPrecision = true)) { _, _ -> true }
                processor.configure(AudioProcessor.AudioFormat(48_000, channels, C.ENCODING_PCM_16BIT))
                processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
                val buffer = ByteBuffer.allocateDirect(256 * channels * 2).order(ByteOrder.nativeOrder())
                repeat(256 * channels) { buffer.putShort((it * 23).toShort()) }
                var frames = 0
                var bytes = 0
                val start = SystemClock.elapsedRealtimeNanos()
                while (frames < FRAMES) {
                    val count = minOf(256, FRAMES - frames)
                    buffer.position(0)
                    buffer.limit(count * channels * 2)
                    processor.queueInput(buffer)
                    bytes += processor.getOutput().remaining()
                    frames += count
                }
                processor.queueEndOfStream()
                bytes += processor.getOutput().remaining()
                val elapsed = SystemClock.elapsedRealtimeNanos() - start
                assertEquals(FRAMES * factor * channels * 2, bytes)
                processor.reset()
                return elapsed
            }
            repeat(2) { process() }
            val samplesMs = List(5) { process() / 1_000_000.0 }
            Log.i("UpscalerPerformance", "channels=$channels factor=$factor frames=$FRAMES samplesMs=$samplesMs")
        }
    }

    private companion object {
        const val FRAMES = 12_000
    }
}
