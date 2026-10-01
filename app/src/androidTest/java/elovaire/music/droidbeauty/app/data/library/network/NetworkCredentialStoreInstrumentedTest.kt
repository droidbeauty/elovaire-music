package elovaire.music.droidbeauty.app.data.library.network

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkCredentialStoreInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val sourceId = "instrumented-source"
    private val key = "instrumented-credential-test"
    private val store = NetworkCredentialStore(context)

    @After
    fun tearDown() {
        store.remove(key)
    }

    @Test
    fun keystoreRoundTripReturnsDurableCredentials() {
        val expected = NetworkCredentials(
            username = "user",
            password = "password",
            domain = "domain",
        )

        store.put(sourceId, key, expected)

        assertEquals(NetworkCredentialReadResult.Available(expected), store.read(sourceId, key))
        assertTrue(store.read("different-source", key) is NetworkCredentialReadResult.Corrupt)
    }

    @Test
    fun malformedStoredValueIsTypedAsCorrupt() {
        context.getSharedPreferences("network_credentials_v1", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(key, "not-base64")
            .commit()

        val result = store.read(sourceId, key)

        assertTrue(result is NetworkCredentialReadResult.Corrupt)
        assertEquals(NetworkCredentialCorruption.InvalidBase64, (result as NetworkCredentialReadResult.Corrupt).reason)
    }

    @Test
    fun removeAndReinsertAdvanceCredentialGeneration() {
        val initial = store.generation(key)
        store.put(sourceId, key, NetworkCredentials("user", "first"))
        val stored = store.generation(key)

        store.remove(key)
        val removed = store.generation(key)
        store.put(sourceId, key, NetworkCredentials("user", "second"))
        val restored = store.generation(key)

        assertNotEquals(initial, stored)
        assertNotEquals(stored, removed)
        assertNotEquals(removed, restored)
    }
}
