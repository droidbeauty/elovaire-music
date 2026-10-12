package elovaire.music.droidbeauty.app.quality

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.datastore.preferences.core.stringPreferencesKey
import elovaire.music.droidbeauty.app.data.settings.elovaireSettingsDataStore
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RapidUiFixtureTest {
    @Test
    fun closeRestoresThePreviousLibraryFolderSelection() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        val bootPreferences = context.getSharedPreferences("settings_boot_snapshot", Context.MODE_PRIVATE)
        val settingsDataStore = context.elovaireSettingsDataStore()
        val key = stringPreferencesKey(LIBRARY_FOLDERS_KEY)
        val originalSettings = runBlocking { settingsDataStore.data.first().asMap() }
        val originalPreferences = preferences.all
        val originalBootPreferences = bootPreferences.all
        val fixture = RapidUiFixture(context, instrumentation.context)

        try {
            fixture.install()
            val fixtureSelection = preferences.getString(LIBRARY_FOLDERS_KEY, null)
            assertEquals(fixtureSelection, runBlocking { settingsDataStore.data.first()[key] })
            assertEquals(fixtureSelection, bootPreferences.getString(LIBRARY_FOLDERS_KEY, null))
        } finally {
            fixture.close()
        }

        assertEquals(originalPreferences, preferences.all)
        assertEquals(originalBootPreferences, bootPreferences.all)
        assertEquals(originalSettings, runBlocking { settingsDataStore.data.first().asMap() })
    }

    private companion object {
        const val LIBRARY_FOLDERS_KEY = "library_folders"
    }
}
