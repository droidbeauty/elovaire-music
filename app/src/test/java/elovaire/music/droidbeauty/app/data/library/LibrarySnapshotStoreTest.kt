package elovaire.music.droidbeauty.app.data.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibrarySnapshotStoreTest {
    @Test
    fun songSignatureChecksum_includesDateModifiedSeconds() {
        val baseline = songSignatureChecksum(
            id = 1L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 111L,
        )

        val modified = songSignatureChecksum(
            id = 1L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 222L,
        )

        assertNotEquals(baseline, modified)
    }

    @Test
    fun songSignatureChecksum_treatsMissingDateModifiedAsZero() {
        val withoutModified = songSignatureChecksum(
            id = 7L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = null,
        )

        val zeroModified = songSignatureChecksum(
            id = 7L,
            dateAddedSeconds = 1_000L,
            dateModifiedSeconds = 0L,
        )

        assertEquals(withoutModified, zeroModified)
    }

    @Test
    fun finiteFloatOrNull_rejectsInvalidSnapshotNumbers() {
        assertNull(finiteFloatOrNull(Double.NaN))
        assertNull(finiteFloatOrNull(Double.POSITIVE_INFINITY))
        assertEquals(0.92f, finiteFloatOrNull(0.92) ?: 0f, 0.001f)
    }

    @Test
    fun finiteFloatOrNull_acceptsFiniteSnapshotNumbers() {
        assertEquals(-4f, finiteFloatOrNull(-4.0) ?: 0f, 0.001f)
    }
}
