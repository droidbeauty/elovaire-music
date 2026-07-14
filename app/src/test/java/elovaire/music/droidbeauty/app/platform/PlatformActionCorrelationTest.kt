package elovaire.music.droidbeauty.app.platform

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformActionCorrelationTest {
    @Test
    fun acceptsOnlyTheCurrentOperationResult() {
        assertTrue(matchesPlatformActionResult("current", "current"))
        assertFalse(matchesPlatformActionResult("current", "stale"))
        assertFalse(matchesPlatformActionResult(null, "current"))
    }
}
