package elovaire.music.droidbeauty.app.data.library.network

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkCredentialStoreInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
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

        store.put(key, expected)

        assertEquals(NetworkCredentialReadResult.Available(expected), store.read(key))
    }

    @Test
    fun malformedStoredValueIsTypedAsCorrupt() {
        context.getSharedPreferences("network_credentials_v1", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(key, "not-base64")
            .commit()

        val result = store.read(key)

        assertTrue(result is NetworkCredentialReadResult.Corrupt)
        assertEquals(NetworkCredentialCorruption.InvalidBase64, (result as NetworkCredentialReadResult.Corrupt).reason)
    }
}
