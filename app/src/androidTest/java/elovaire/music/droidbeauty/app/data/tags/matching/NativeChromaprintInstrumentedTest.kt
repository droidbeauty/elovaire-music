package elovaire.music.droidbeauty.app.data.tags.matching

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeChromaprintInstrumentedTest {
    @Test
    fun nativeSessionProcessesBoundedPcmAndClosesIdempotently() {
        val session = requireNotNull(NativeChromaprintSession.open())
        try {
            assertTrue(session.start(SAMPLE_RATE, CHANNELS))
            val samples = ShortArray(CHUNK_SAMPLES) { index ->
                ((index * 257) % Short.MAX_VALUE).toShort()
            }
            repeat(CHUNK_COUNT) {
                assertTrue(session.feed(samples, samples.size))
            }
            assertFalse(session.finish().isNullOrBlank())
        } finally {
            session.close()
            session.close()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val CHANNELS = 2
        const val CHUNK_SAMPLES = 4_096
        const val CHUNK_COUNT = 220
    }
}
