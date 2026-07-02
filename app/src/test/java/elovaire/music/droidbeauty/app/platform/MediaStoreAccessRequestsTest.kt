package elovaire.music.droidbeauty.app.platform

import android.net.TestUri
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreAccessRequestsTest {
    @Test
    fun isContentUri_acceptsContentUris() {
        assertTrue(TestUri("content://media/external/audio/media/1").isContentUri())
    }
}
