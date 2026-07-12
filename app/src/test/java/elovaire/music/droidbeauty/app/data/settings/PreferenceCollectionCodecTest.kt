package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceCollectionCodecTest {
    @Test
    fun corruptPlayCountsAreDiscardedWithoutLosingValidEntries() {
        val decoded = PreferenceCollectionCodec.deserializePlayCounts("1:3,broken,2:-4,-1:9,3:x")

        assertEquals(mapOf(1L to 3, 2L to 0), decoded)
    }

    @Test
    fun corruptSearchHistoryIsRejected() {
        assertEquals(null, PreferenceCollectionCodec.deserializeSearchHistory("broken"))
    }

    @Test
    fun corruptLibraryFolderIsRejected() {
        assertEquals(null, PreferenceCollectionCodec.deserializeLibraryFolder("broken"))
    }

    @Test
    fun emptyPlayCountsRemainEmpty() {
        assertTrue(PreferenceCollectionCodec.deserializePlayCounts("").isEmpty())
    }
}
