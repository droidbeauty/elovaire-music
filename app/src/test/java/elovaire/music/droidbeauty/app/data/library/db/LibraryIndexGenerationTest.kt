package elovaire.music.droidbeauty.app.data.library.db

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryIndexGenerationTest {
    @Test
    fun repeatedScanInSameMillisecondGetsNewGeneration() {
        assertEquals(1_001L, nextLibraryGenerationId(1_000L, 1_000L))
    }

    @Test
    fun wallClockRollbackCannotReuseGeneration() {
        assertEquals(5_001L, nextLibraryGenerationId(100L, 5_000L))
    }

    @Test
    fun firstGenerationUsesPositiveWallTime() {
        assertEquals(1L, nextLibraryGenerationId(0L, null))
    }
}
