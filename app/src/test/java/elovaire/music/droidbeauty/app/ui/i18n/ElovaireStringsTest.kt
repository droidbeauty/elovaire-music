package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ElovaireStringsTest {
    @Test
    fun sortOptionsArePresentAndConsistentAcrossLibraryAndSearch() {
        AppLanguage.entries.forEach { language ->
            SongSortMode.entries.forEach { assertFalse(songSortLabel(language, it).isBlank()) }
            AlbumSortMode.entries.forEach { assertFalse(albumSortLabel(language, it).isBlank()) }
            assertEquals(songSortLabel(language, SongSortMode.Title), searchSortModeLabel(SearchSongSortMode.Title, language))
            assertEquals(songSortLabel(language, SongSortMode.Artist), searchSortModeLabel(SearchSongSortMode.Artist, language))
            if (language != AppLanguage.English) {
                assertFalse(songSortLabel(language, SongSortMode.Title) == "Song name")
                assertFalse(songSortLabel(language, SongSortMode.Artist) == "Artist name")
            }
        }
    }
    @Test
    fun privacySectionsHaveNoFinalFullStopInAnyLanguage() {
        AppLanguage.entries.forEach { language ->
            privacyPolicyCopy(language).sections.forEach { section ->
                assertFalse(section.body.isBlank())
                assertFalse(section.body.last() in listOf('.', '。', '।'))
            }
        }
    }

    @Test
    fun recentEuropeanLanguagesHaveLocalizedPrivacySections() {
        val english = privacyPolicyCopy(AppLanguage.English).sections
        listOf(AppLanguage.Bulgarian, AppLanguage.Catalan, AppLanguage.Finnish, AppLanguage.Romanian, AppLanguage.Slovenian).forEach { language ->
            val sections = privacyPolicyCopy(language).sections
            assertEquals(english.size, sections.size)
            sections.zip(english).forEach { (localized, source) ->
                assertFalse(localized.title == source.title)
                assertFalse(localized.body == source.body)
            }
            assertFalse(libraryFoldersCopy(language).noFoldersMessage == libraryFoldersCopy(AppLanguage.English).noFoldersMessage)
            assertFalse(networkSourcesCopy(language).removeMessage == networkSourcesCopy(AppLanguage.English).removeMessage)
        }
    }

    @Test
    fun polishSongCountsUseTheCorrectNumeralForms() {
        assertEquals("1 utwór", localizedCountLabel(1, "song", AppLanguage.Polish))
        assertEquals("2 utwory", localizedCountLabel(2, "song", AppLanguage.Polish))
        assertEquals("5 utworów", localizedCountLabel(5, "song", AppLanguage.Polish))
        assertEquals("12 utworów", localizedCountLabel(12, "song", AppLanguage.Polish))
        assertEquals("22 utwory", localizedCountLabel(22, "song", AppLanguage.Polish))
    }

    @Test
    fun polishSmartMixSortingUsesGenitiveForms() {
        assertEquals("Sortowane według: tytułu", smartPlaylistSortLabel(AppLanguage.Polish, SmartPlaylistSortField.Title))
        assertEquals("Sortowane według: wykonawcy", smartPlaylistSortLabel(AppLanguage.Polish, SmartPlaylistSortField.Artist))
        assertEquals("Sortowane według: albumu", smartPlaylistSortLabel(AppLanguage.Polish, SmartPlaylistSortField.Album))
        assertEquals("Sortowane według: gatunku", smartPlaylistSortLabel(AppLanguage.Polish, SmartPlaylistSortField.Genre))
        assertEquals("Sortowane według: czasu trwania", smartPlaylistSortLabel(AppLanguage.Polish, SmartPlaylistSortField.Duration))
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
    fun newEuropeanLanguagesUseAppropriateCountForms() {
        assertEquals("1 skladba", localizedCountLabel(1, "song", AppLanguage.Slovenian))
        assertEquals("2 skladbi", localizedCountLabel(2, "song", AppLanguage.Slovenian))
        assertEquals("3 skladbe", localizedCountLabel(3, "song", AppLanguage.Slovenian))
        assertEquals("5 skladb", localizedCountLabel(5, "song", AppLanguage.Slovenian))
        assertEquals("20 de melodii", localizedCountLabel(20, "song", AppLanguage.Romanian))
        assertEquals("21 de melodii", localizedCountLabel(21, "song", AppLanguage.Romanian))
        assertEquals("2 kappaletta", localizedCountLabel(2, "song", AppLanguage.Finnish))
    }

    @Test
    fun audiobookAndMixSettingsDoNotFallBackToEnglish() {
        val languages = AppLanguage.entries.filterNot { it == AppLanguage.English }
        languages.forEach { language ->
            assertFalse(audiobookCopy(language).title == audiobookCopy(AppLanguage.English).title)
            assertFalse(audiobookSettingsCopy(language).title == audiobookSettingsCopy(AppLanguage.English).title)
            assertFalse(audiobookExtraCopy(language).speedSubtitle == audiobookExtraCopy(AppLanguage.English).speedSubtitle)
            assertFalse(smartPlaylistSettingsCopy(language).maximumSongsSubtitle == smartPlaylistSettingsCopy(AppLanguage.English).maximumSongsSubtitle)
        }
    }

    @Test
    fun englishUpscalerCopyHasNoFinalSentencePeriod() {
        val copy = upscalerCopy(AppLanguage.English)
        listOf(copy.precisionSubtitle, copy.headroomSubtitle, copy.explanation).forEach {
            assertFalse(it.endsWith('.'))
        }
        assertEquals(1, copy.explanation.count { it == '.' })
    }

    @Test
    fun upscalerDescriptionsAreLocalizedForEveryLanguage() {
        val source = upscalerCopy(AppLanguage.English)
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            val copy = upscalerCopy(language)
            assertFalse(copy.precisionSubtitle == source.precisionSubtitle)
            assertFalse(copy.headroomSubtitle == source.headroomSubtitle)
            assertFalse(copy.explanation == source.explanation)
        }
    }

    @Test
    fun englishSharedCopyHasNoTerminalSentencePeriod() {
        val copies = listOf(rootUiCopy(AppLanguage.English), libraryFoldersCopy(AppLanguage.English), networkSourcesCopy(AppLanguage.English))
        copies.forEach { copy ->
            copy.javaClass.declaredFields.filter { it.type == String::class.java }.forEach { field ->
                field.isAccessible = true
                val value = field.get(copy) as String
                assertFalse("${copy.javaClass.simpleName}.${field.name} ends in a period", value.endsWith('.') && !value.endsWith("..."))
            }
        }
    }

    @Test
    fun newLanguagesHaveLocalizedCoreScreensAndBosnianCounts() {
        assertEquals("1 pjesma", localizedCountLabel(1, "song", AppLanguage.Bosnian))
        assertEquals("2 pjesme", localizedCountLabel(2, "song", AppLanguage.Bosnian))
        assertEquals("5 pjesama", localizedCountLabel(5, "song", AppLanguage.Bosnian))
        assertEquals("12 pjesama", localizedCountLabel(12, "song", AppLanguage.Bosnian))
        assertEquals("22 pjesme", localizedCountLabel(22, "song", AppLanguage.Bosnian))
        assertEquals("5 lagu", localizedCountLabel(5, "song", AppLanguage.Indonesian))
        assertEquals("21 ਪਲੇਲਿਸਟਾਂ", localizedCountLabel(21, "playlist", AppLanguage.Punjabi))
        listOf(AppLanguage.Bosnian, AppLanguage.Indonesian, AppLanguage.Punjabi).forEach { language ->
            assertFalse(settingsCopy(language).settings == settingsCopy(AppLanguage.English).settings)
            assertFalse(privacyPolicyCopy(language).title == privacyPolicyCopy(AppLanguage.English).title)
            assertEquals(8, privacyPolicyCopy(language).sections.size)
            assertFalse(audiobookCopy(language).title == audiobookCopy(AppLanguage.English).title)
            UiPhrase.entries.forEach { assertFalse(uiPhrase(language, it).isBlank()) }
        }
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
    fun searchCopyIsPresentForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = searchCopy(language)
            assertFalse(copy.placeholder.isBlank())
            assertFalse(copy.nothingSearchedMessage.isBlank())
            assertFalse(copy.noResultsMessage("query").endsWith(' '))
            if (language != AppLanguage.English) {
                assertFalse(copy.nothingSearchedMessage == searchCopy(AppLanguage.English).nothingSearchedMessage)
                assertFalse(recentAdditionsEmptyMessage(language) == recentAdditionsEmptyMessage(AppLanguage.English))
            }
        }
    }

    @Test
    fun audiobookEditingLabelIsLocalizedForEveryLanguage() {
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            val copy = audiobookCopy(language)
            assertFalse(copy.editTags.isBlank())
            assertFalse(copy.editTags == audiobookCopy(AppLanguage.English).editTags)
        }
    }

    @Test
    fun rootCopyDoesNotFallBackToEnglish() {
        val source = rootUiCopy(AppLanguage.English)
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            val copy = rootUiCopy(language)
            assertFalse(copy.renamePlaylistTitle == source.renamePlaylistTitle)
            assertFalse(copy.noLyrics == source.noLyrics)
            assertFalse(copy.savePlaylistChanges == source.savePlaylistChanges)
        }
    }

    @Test
    fun sharedPhrasesAreExplicitlyTranslatedForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            UiPhrase.entries.forEach { phrase ->
                org.junit.Assert.assertTrue("Missing $phrase in $language", hasUiPhraseTranslation(language, phrase))
            }
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
