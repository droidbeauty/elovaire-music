package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.core.FakeAppClock
import org.junit.Assert.assertEquals
import org.junit.Test

class ScannerProgressEmitterTest {
    @Test
    fun progressThrottleUsesElapsedClockWithoutRealDelay() {
        val clock = FakeAppClock()
        val emissions = mutableListOf<Pair<Int, Int>>()
        val emitter = ScannerProgressEmitter(
            onProgress = { current, total -> emissions += current to total },
            clock = clock,
        )

        emitter.emit(1, 1_000)
        emitter.emit(2, 1_000)
        clock.elapsedTime = 81L
        emitter.emit(3, 1_000)

        assertEquals(listOf(1 to 1_000, 3 to 1_000), emissions)
    }
}
