package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import elovaire.music.droidbeauty.app.quality.SharedPreferencesSnapshot
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking

class NetworkLibrarySourceStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sourcePreferences = preferences()
    private var originalPreferences: SharedPreferencesSnapshot? = null

    @Before
    fun setUp() {
        originalPreferences = SharedPreferencesSnapshot.capture(sourcePreferences)
        sourcePreferences.edit().clear().commit()
    }

    @After
    fun tearDown() {
        originalPreferences?.restore(sourcePreferences)
        originalPreferences = null
    }

    @Test
    fun upsertRejectsCredentialKeySharedByDifferentSources() = runBlocking {
        val store = NetworkLibrarySourceStore(context)
        store.upsert(source(id = "source-a"))

        assertThrows(IllegalStateException::class.java) {
            runBlocking { store.upsert(source(id = "source-b")) }
        }
        Unit
    }

    @Test
    fun persistedSourceStartsCurrentAndReaddingItRejectsTheOldGeneration() = runBlocking {
        val originalStore = NetworkLibrarySourceStore(context)
        originalStore.upsert(source(id = "restored-source"))

        val restoredStore = NetworkLibrarySourceStore(context)
        val restored = restoredStore.sources.value.single()
        val restoredGeneration = restoredStore.generation(restored.id)
        assertEquals(0L, restoredGeneration)
        assertTrue(restoredStore.isCurrent(restored, restoredGeneration))

        restoredStore.remove(restored.id)
        val readded = restoredStore.upsert(restored)

        assertFalse(restoredStore.isCurrent(readded, restoredGeneration))
        assertTrue(restoredStore.generation(readded.id) > restoredGeneration)
        assertTrue(restoredStore.isCurrent(readded, restoredStore.generation(readded.id)))
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
