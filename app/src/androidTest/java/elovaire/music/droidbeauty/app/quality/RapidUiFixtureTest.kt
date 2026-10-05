package elovaire.music.droidbeauty.app.quality

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import org.junit.Assert.assertEquals
import org.junit.Test

class RapidUiFixtureTest {
    @Test
    fun closeRestoresThePreviousLibraryFolderSelection() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        val hadOriginalSelection = preferences.contains(LIBRARY_FOLDERS_KEY)
        val originalSelection = preferences.getString(LIBRARY_FOLDERS_KEY, null)
        val fixture = RapidUiFixture(context, instrumentation.context)

        try {
            fixture.install()
        } finally {
            fixture.close()
        }

        assertEquals(hadOriginalSelection, preferences.contains(LIBRARY_FOLDERS_KEY))
        assertEquals(originalSelection, preferences.getString(LIBRARY_FOLDERS_KEY, null))
    }

    private companion object {
        const val LIBRARY_FOLDERS_KEY = "library_folders"
    }
}
