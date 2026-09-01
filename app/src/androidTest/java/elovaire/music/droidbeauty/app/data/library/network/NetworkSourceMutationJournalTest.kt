package elovaire.music.droidbeauty.app.data.library.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkSourceMutationJournalTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("network_source_mutations_v1", 0)

    @Before
    fun setUp() {
        check(preferences.edit().clear().commit())
    }

    @After
    fun tearDown() {
        check(preferences.edit().clear().commit())
    }

    @Test
    fun fullJournalRejectsNewMutationWithoutDroppingExistingMarkers() {
        val journal = NetworkSourceMutationJournal(context)
        repeat(32) { index ->
            journal.prepareSave(
                sourceId = "source-$index",
                previousCredentialKey = null,
                newCredentialKey = "credential-$index",
                previousLocationFingerprint = null,
                newLocationFingerprint = "location-$index",
            )
        }

        assertThrows(IllegalStateException::class.java) {
            journal.prepareSave(
                sourceId = "source-over-capacity",
                previousCredentialKey = null,
                newCredentialKey = "credential-over-capacity",
                previousLocationFingerprint = null,
                newLocationFingerprint = "location-over-capacity",
            )
        }
        assertEquals(32, journal.pending().size)
    }
}
