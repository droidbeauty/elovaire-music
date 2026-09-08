package elovaire.music.droidbeauty.app.data.playback

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AudiobookProgressStoreTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val preferences = context.getSharedPreferences("audiobook_progress", 0)

    @Before
    fun clearProgress() {
        preferences.edit().clear().commit()
    }

    @Test
    fun remapBookKeyKeepsNewestCheckpointAndRemovesOldKey() {
        val store = AudiobookProgressStore(context)
        store.save("old", 1L, 100L, 1_000L, nowMs = 10L)
        store.save("new", 2L, 200L, 1_000L, nowMs = 20L)

        store.remapBookKey("old", "new")

        assertEquals(2L, store.load("new")?.songId)
        assertEquals(20L, store.load("new")?.updatedAtMs)
        assertNull(store.load("old"))
    }

    @Test
    fun remapBookKeyDoesNotDeleteOnlyValidCheckpointWhenReplacementIsMalformed() {
        val store = AudiobookProgressStore(context)
        store.save("old", 1L, 100L, 1_000L, nowMs = 10L)
        preferences.edit().putString(hashedKey("new"), "not-json").commit()

        store.remapBookKey("old", "new")

        assertEquals(1L, store.load("new")?.songId)
        assertNull(store.load("old"))
    }

    private fun hashedKey(bookKey: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(bookKey.toByteArray(Charsets.UTF_8))
        return "book_" + digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
