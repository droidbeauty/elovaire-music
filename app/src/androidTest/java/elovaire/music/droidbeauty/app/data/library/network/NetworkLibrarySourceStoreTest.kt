package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class NetworkLibrarySourceStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        preferences().edit().clear().commit()
    }

    @After
    fun tearDown() {
        preferences().edit().clear().commit()
    }

    @Test
    fun upsertRejectsCredentialKeySharedByDifferentSources() {
        val store = NetworkLibrarySourceStore(context)
        store.upsert(source(id = "source-a"))

        assertThrows(IllegalStateException::class.java) {
            store.upsert(source(id = "source-b"))
        }
    }

    private fun preferences() = context.getSharedPreferences("network_library_sources_v1", Context.MODE_PRIVATE)

    private fun source(id: String) = NetworkLibrarySource(
        id = id,
        name = id,
        protocol = NetworkLibraryProtocol.Smb,
        server = "server",
        shareOrPath = "share",
        username = "user",
        credentialKey = "shared-key",
    )
}
