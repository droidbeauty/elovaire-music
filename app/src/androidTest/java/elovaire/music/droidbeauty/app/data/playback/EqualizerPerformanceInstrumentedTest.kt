package elovaire.music.droidbeauty.app.data.playback

import android.os.SystemClock
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@UnstableApi
class EqualizerPerformanceInstrumentedTest {
    @Test
    fun reportsBufferDeadlinesForNeutralAndCombinedProcessing() {
        val scenarios = listOf(
            "neutral" to EqSettings(),
            "combined" to EqSettings(
                bands = List(EqualizerDspModel.BAND_COUNT) { if (it % 2 == 0) 0.5f else -0.5f },
                bass = 0.7f,
                midrange = 0.4f,
                treble = 0.4f,
                spaciousness = 0.5f,
                spaciousnessMode = SpaciousnessMode.StereoWidth,
                reverbDurationMs = 500,
            ),
        )
        val frames = 256
        val count = 1_000
        for ((name, settings) in scenarios) {
            val processor = EqualizerAudioProcessor()
            processor.updateSettings(settings)
            processor.configure(AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT))
            val input = ByteBuffer.allocateDirect(frames * 4).order(ByteOrder.nativeOrder())
            repeat(frames * 2) { input.putShort(((it * 97) % 12_000 - 6_000).toShort()) }

            fun processBuffers(iterations: Int, timings: LongArray?): Long {
                processor.flush(AudioProcessor.StreamMetadata.DEFAULT)
                val cpuStart = SystemClock.currentThreadTimeMillis()
                var bytes = 0L
                repeat(iterations) { index ->
                    input.position(0)
                    val started = SystemClock.elapsedRealtimeNanos()
                    processor.queueInput(input)
                    bytes += processor.output.remaining()
                    timings?.set(index, SystemClock.elapsedRealtimeNanos() - started)
                }
                val cpuMs = SystemClock.currentThreadTimeMillis() - cpuStart
                processor.queueEndOfStream()
                assertTrue(processor.isEnded)
                assertEquals(iterations.toLong() * frames * 4, bytes)
                return cpuMs
            }

            processBuffers(200, null)
            val timings = LongArray(count)
            val cpuMs = processBuffers(count, timings)
            timings.sort()
            fun percentile(value: Int): Long = timings[(timings.lastIndex * value) / 100]
            Log.i(
                "EqualizerPerformance",
                "scenario=$name rate=48000 channels=2 frames=$frames iterations=$count " +
                    "deadlineNs=${frames * 1_000_000_000L / 48_000} cpuMs=$cpuMs " +
                    "p50Ns=${percentile(50)} p95Ns=${percentile(95)} " +
                    "p99Ns=${percentile(99)} maxNs=${timings.last()}",
            )
            processor.reset()
        }
    }
}
