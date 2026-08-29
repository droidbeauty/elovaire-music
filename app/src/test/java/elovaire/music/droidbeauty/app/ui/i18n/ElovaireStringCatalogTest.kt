package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ElovaireStringCatalogTest {
    @Test
    fun polishSongCountsUseTheCorrectNumeralForms() {
        assertEquals("1 utwór", localizedCountLabel(1, "song", AppLanguage.Polish))
        assertEquals("2 utwory", localizedCountLabel(2, "song", AppLanguage.Polish))
        assertEquals("5 utworów", localizedCountLabel(5, "song", AppLanguage.Polish))
        assertEquals("12 utworów", localizedCountLabel(12, "song", AppLanguage.Polish))
        assertEquals("22 utwory", localizedCountLabel(22, "song", AppLanguage.Polish))
    }

    @Test
    fun otherSlavicSongCountsUseTheirPluralForms() {
        assertEquals("5 skladeb", localizedCountLabel(5, "song", AppLanguage.Czech))
        assertEquals("5 skladieb", localizedCountLabel(5, "song", AppLanguage.Slovak))
        assertEquals("5 песен", localizedCountLabel(5, "song", AppLanguage.Russian))
        assertEquals("5 пісень", localizedCountLabel(5, "song", AppLanguage.Ukrainian))
        assertEquals("5 песама", localizedCountLabel(5, "song", AppLanguage.Serbian))
        assertEquals("5 pjesama", localizedCountLabel(5, "song", AppLanguage.Croatian))
    }

    @Test
    fun settingsEntriesArePopulatedForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = settingsCopy(language)
            listOf(
                copy.crossfadeTitle,
                copy.crossfadeSubtitle,
                copy.managePlaylistsTitle,
                copy.managePlaylistsSubtitle,
                copy.onlineLyricsTitle,
                copy.onlineLyricsSubtitle,
                copy.checkForUpdatesTitle,
                copy.checkForUpdatesSubtitle,
            ).forEach { value -> assertFalse(value.isBlank()) }
        }
    }

    @Test
    fun networkSourceEntriesArePopulatedForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = networkSourcesCopy(language)
            listOf(
                copy.sectionTitle,
                copy.available,
                copy.checking,
                copy.signIn,
                copy.allowLocalNetwork,
                copy.addSource,
                copy.closeSourcePicker,
                copy.chooseFolderSubtitle,
                copy.nasTitle,
                copy.nasSubtitle,
                copy.removeTitle,
                copy.removeMessage,
                copy.remove,
                copy.editorTitle,
                copy.saveEditor,
                copy.name,
                copy.server,
                copy.httpsServer,
                copy.sharePath,
                copy.path,
                copy.username,
                copy.domainOptional,
                copy.password,
                copy.connectionAvailable,
                copy.allowLocalNetworkSettings,
                copy.authenticationRequired,
                copy.hostUnreachable,
                copy.checkServerPath,
                copy.sourceUnavailable,
                copy.testingConnection,
            ).forEach { value -> assertFalse(value.isBlank()) }
        }
    }
}
