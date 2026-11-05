package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PreferenceStorePolicyTest {
    @Test
    fun removedLanguageFallsBackToDeviceLanguageWithoutChangingValidSelections() {
        assertFalse(AppLanguage.entries.any { it.name == "Latin" })
        assertEquals(AppLanguage.Polish, resolveSavedAppLanguage("Latin") { AppLanguage.Polish })
        assertEquals(AppLanguage.English, resolveSavedAppLanguage("Latin") { AppLanguage.English })
        assertEquals(AppLanguage.French, resolveSavedAppLanguage("French") { AppLanguage.English })
    }

    @Test
    fun incrementPlayCount_neverOverflowsOrPersistsNegativeCounts() {
        assertEquals(1, incrementPlayCount(null))
        assertEquals(1, incrementPlayCount(-4))
        assertEquals(4, incrementPlayCount(3))
        assertEquals(Int.MAX_VALUE, incrementPlayCount(Int.MAX_VALUE))
    }

    @Test
    fun normalizeFavoriteSongIds_keepsSafIdsAndDropsZero() {
        assertEquals(
            listOf(-1L, 8L, 3L),
            normalizeFavoriteSongIds(listOf(0L, -1L, 8L, 3L, 8L, 0L)),
        )
    }
}
