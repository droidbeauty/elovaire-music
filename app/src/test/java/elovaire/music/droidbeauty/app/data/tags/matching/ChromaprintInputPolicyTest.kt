package elovaire.music.droidbeauty.app.data.tags.matching

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromaprintInputPolicyTest {
    @Test
    fun startRejectsInvalidHandlesAndAudioShapes() {
        assertTrue(ChromaprintInputPolicy.acceptsStart(1L, 44_100, 2))
        assertFalse(ChromaprintInputPolicy.acceptsStart(0L, 44_100, 2))
        assertFalse(ChromaprintInputPolicy.acceptsStart(1L, 0, 2))
        assertFalse(ChromaprintInputPolicy.acceptsStart(1L, 44_100, 0))
        assertFalse(ChromaprintInputPolicy.acceptsStart(1L, 768_001, 2))
        assertFalse(ChromaprintInputPolicy.acceptsStart(1L, 44_100, 33))
    }

    @Test
    fun feedRejectsInvalidAndTruncatedLengths() {
        assertTrue(ChromaprintInputPolicy.acceptsFeed(1L, 4_096, 4_096))
        assertFalse(ChromaprintInputPolicy.acceptsFeed(0L, 4_096, 4_096))
        assertFalse(ChromaprintInputPolicy.acceptsFeed(1L, 4_096, 0))
        assertFalse(ChromaprintInputPolicy.acceptsFeed(1L, 4_096, 4_097))
        assertFalse(ChromaprintInputPolicy.acceptsFeed(1L, Int.MAX_VALUE, 16 * 1024 * 1024 + 1))
    }
}
