package elovaire.music.droidbeauty.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryPressureTest {
    @Test
    fun trimLevelsMapToStablePolicies() {
        assertEquals(MemoryPressure.Normal, memoryPressureForTrimLevel(0))
        assertEquals(MemoryPressure.Moderate, memoryPressureForTrimLevel(10))
        assertEquals(MemoryPressure.Critical, memoryPressureForTrimLevel(15))
        assertEquals(MemoryPressure.Moderate, memoryPressureForTrimLevel(40))
        assertEquals(MemoryPressure.Critical, memoryPressureForTrimLevel(80))
    }
}
