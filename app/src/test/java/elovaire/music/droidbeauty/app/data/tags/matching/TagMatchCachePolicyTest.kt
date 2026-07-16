package elovaire.music.droidbeauty.app.data.tags.matching

import org.junit.Assert.assertEquals
import org.junit.Test

class TagMatchCachePolicyTest {
    @Test
    fun trimsDeterministicallyWithoutEvictingIncomingEntry() {
        assertEquals(
            listOf("fp_a", "fp_b"),
            fingerprintKeysToTrim(
                keys = setOf("fp_c", "fp_a", "fp_b"),
                incomingKey = "fp_new",
                maxEntries = 2,
            ),
        )
    }

    @Test
    fun replacingExistingEntryDoesNotGrowCache() {
        assertEquals(
            emptyList<String>(),
            fingerprintKeysToTrim(
                keys = setOf("fp_a", "fp_b"),
                incomingKey = "fp_b",
                maxEntries = 2,
            ),
        )
    }
}
