package elovaire.music.droidbeauty.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UserDataIdPolicyTest {
    @Test
    fun incrementsWithoutWrapping() {
        assertEquals(2L, nextPersistentUserDataId(1L))
        assertEquals(Long.MAX_VALUE, nextPersistentUserDataId(Long.MAX_VALUE - 1L))
    }

    @Test
    fun rejectsExhaustedOrInvalidIds() {
        assertThrows(IllegalStateException::class.java) {
            nextPersistentUserDataId(Long.MAX_VALUE)
        }
        assertThrows(IllegalStateException::class.java) {
            nextPersistentUserDataId(0L)
        }
    }
}
