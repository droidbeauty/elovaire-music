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
    fun englishCopyUsesProjectSentencePunctuation() {
        val copies = listOf(
            rootUiCopy(AppLanguage.English),
            homeCopy(AppLanguage.English),
            searchCopy(AppLanguage.English),
            settingsCopy(AppLanguage.English),
            upscalerCopy(AppLanguage.English),
            aboutUpdateCopy(AppLanguage.English),
            updateActionCopy(AppLanguage.English),
            smartPlaylistEmptyCopy(AppLanguage.English),
            smartPlaylistDeleteCopy(AppLanguage.English),
            smartPlaylistMissingCopy(AppLanguage.English),
        )
        val values = copies.flatMap { copy ->
            copy.javaClass.declaredFields.filter { it.type == String::class.java }.map { field ->
                field.isAccessible = true
                field.get(copy) as String
            }
        } + privacyPolicyCopy(AppLanguage.English).sections.flatMap { listOf(it.title, it.body) }
        val sentenceBoundary = Regex("(?<!\\.)\\.(?!\\.)\\s+(?=[A-Z])")
        values.forEach { value ->
            assertFalse("Unexpected final period: $value", value.endsWith('.') && !value.endsWith("..."))
            assertFalse("More than one sentence separator: $value", sentenceBoundary.findAll(value).count() > 1)
        }
    }

    @Test
    fun navigationAccessibilityLabelsAreAvailableInEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = navigationAccessibilityCopy(language)
            val playback = playbackNavigationAccessibilityCopy(language)
            val viewModes = viewModeAccessibilityCopy(language)
            assertFalse(copy.back.isBlank())
            assertFalse(copy.menu.isBlank())
            assertFalse(playback.previous.isBlank())
            assertFalse(playback.next.isBlank())
            assertFalse(playback.rewind.isBlank())
            assertFalse(playback.forward.isBlank())
            assertFalse(showAllSongResultsLabel(language).isBlank())
            assertFalse(viewModes.list.isBlank())
            assertFalse(viewModes.compactList.isBlank())
            assertFalse(viewModes.grid.isBlank())
            assertFalse(viewModes.denseGrid.isBlank())
            assertFalse(closeQueueAccessibilityLabel(language).isBlank())
            assertFalse(lyricsAccessibilityCopy(language).lyrics.isBlank())
            assertFalse(lyricsAccessibilityCopy(language).addLyrics.isBlank())
            val options = mediaOptionsAccessibilityCopy(language)
            listOf(options.queueSong, options.album, options.song).forEach { assertFalse(it.isBlank()) }
            val controls = playerControlAccessibilityCopy(language)
            listOf(controls.minimize, controls.resetTimer, controls.mutedVolume, controls.maximumVolume).forEach { assertFalse(it.isBlank()) }
            val settings = settingsAccessibilityCopy(language)
            listOf(settings.closeLanguage, settings.smallerText, settings.largerText).forEach { assertFalse(it.isBlank()) }
            val playlist = playlistAccessibilityCopy(language)
            listOf(playlist.confirmSongs, playlist.reorderSong).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) assertFalse(copy.back == "Back")
        }
        assertEquals("Zamknij kolejkę", closeQueueAccessibilityLabel(AppLanguage.Polish))
        assertEquals("Dodaj tekst utworu", lyricsAccessibilityCopy(AppLanguage.Polish).addLyrics)
        assertEquals("Opcje utworu w kolejce", mediaOptionsAccessibilityCopy(AppLanguage.Polish).queueSong)
        assertEquals("Maksymalna głośność", playerControlAccessibilityCopy(AppLanguage.Polish).maximumVolume)
        assertEquals("Zamknij wybór języka", settingsAccessibilityCopy(AppLanguage.Polish).closeLanguage)
        assertEquals("Potwierdź dodanie utworów", playlistAccessibilityCopy(AppLanguage.Polish).confirmSongs)
    }

    @Test
    fun preampLabelIsAvailableForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            assertFalse(preampLabel(language).isBlank())
        }
        assertEquals("Przedwzmacniacz", preampLabel(AppLanguage.Polish))
        assertEquals("Preamp", preampLabel(AppLanguage.English))
    }

    @Test
    fun unknownGenreIsLocalizedWithoutChangingRealMetadata() {
        assertEquals("Nieznany gatunek", localizedGenreName("Unknown Genre", AppLanguage.Polish))
        assertEquals("Nieznany gatunek", localizedGenreName("", AppLanguage.Polish))
        assertEquals("Jazz", localizedGenreName("Jazz", AppLanguage.Polish))
    }

    @Test
    fun updateAndChangelogCopyIsLocalizedForEveryLanguage() {
        val source = aboutUpdateCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = aboutUpdateCopy(language)
            listOf(copy.updateAvailable, copy.version, copy.later, copy.download, copy.closeChangelog, copy.noChangelog).forEach {
                assertFalse(it.isBlank())
                assertFalse(it.endsWith('.'))
            }
            if (language != AppLanguage.English) assertFalse(copy.noChangelog == source.noChangelog)
        }
    }

    @Test
    fun updateActionsAreAvailableForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = updateActionCopy(language)
            listOf(copy.checking, copy.downloading, copy.installing, copy.allowInstall, copy.update, copy.retry)
                .forEach { assertFalse(it.isBlank()) }
        }
        assertEquals("Instalowanie…", updateActionCopy(AppLanguage.Polish).installing)
        assertEquals("Retry", updateActionCopy(AppLanguage.English).retry)
    }

    @Test
    fun searchResultCountsUseSingularInEnglish() {
        val copy = searchCopy(AppLanguage.English)
        assertEquals("1 matching artist", copy.matchingArtists(1, AppLanguage.English))
        assertEquals("1 matching album result", copy.matchingAlbums(1, AppLanguage.English))
        assertEquals("1 matching song result", copy.matchingSongs(1, AppLanguage.English))
        assertEquals("2 matching artists", copy.matchingArtists(2, AppLanguage.English))
    }

    @Test
    fun artistInformationCloseActionIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val label = closeArtistInfoLabel(language)
            assertFalse(label.isBlank())
            if (language != AppLanguage.English) assertFalse(label == "Close artist information")
        }
        assertEquals("Zamknij informacje o wykonawcy", closeArtistInfoLabel(AppLanguage.Polish))
    }

    @Test
    fun smartPlaylistEmptyStateIsLocalized() {
        val english = smartPlaylistEmptyCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistEmptyCopy(language)
            assertFalse(copy.title.isBlank())
            assertFalse(copy.message.isBlank())
            if (language != AppLanguage.English) {
                assertFalse(copy.title == english.title)
                assertFalse(copy.message == english.message)
            }
        }
        assertEquals("Żaden utwór nie spełnia tych reguł", smartPlaylistEmptyCopy(AppLanguage.Polish).title)
    }

    @Test
    fun smartPlaylistDeleteConfirmationIsLocalized() {
        val english = smartPlaylistDeleteCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistDeleteCopy(language)
            assertFalse(copy.title.isBlank())
            assertFalse(copy.message.isBlank())
            if (language != AppLanguage.English) {
                assertFalse(copy.title == english.title)
                assertFalse(copy.message == english.message)
            }
        }
        assertEquals("Usunąć inteligentną playlistę?", smartPlaylistDeleteCopy(AppLanguage.Polish).title)
    }

    @Test
    fun missingSmartPlaylistIsLocalized() {
        val english = smartPlaylistMissingCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistMissingCopy(language)
            assertFalse(copy.title.isBlank())
            assertFalse(copy.message.isBlank())
            if (language != AppLanguage.English) assertFalse(copy.message == english.message)
        }
    }

    @Test
    fun smartPlaylistPreviewUsesLocalizedCountForms() {
        assertEquals("Preview: 1 song", smartPlaylistPreviewLabel(AppLanguage.English, 1))
        assertEquals("Podgląd: 5 utworów", smartPlaylistPreviewLabel(AppLanguage.Polish, 5))
        assertEquals("Podgląd: 22 utwory", smartPlaylistPreviewLabel(AppLanguage.Polish, 22))
        AppLanguage.entries.forEach { assertFalse(smartPlaylistPreviewLabel(it, 1).isBlank()) }
    }

    @Test
    fun smartPlaylistEditRulesActionIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val label = smartPlaylistEditRulesLabel(language)
            assertFalse(label.isBlank())
            if (language != AppLanguage.English) assertFalse(label == "Edit rules")
            assertFalse(smartPlaylistRemoveRuleLabel(language).isBlank())
        }
        assertEquals("Edytuj reguły", smartPlaylistEditRulesLabel(AppLanguage.Polish))
        assertEquals("Usuń regułę", smartPlaylistRemoveRuleLabel(AppLanguage.Polish))
    }

    @Test
    fun smartPlaylistEditorHeadingsAreLocalized() {
        val english = smartPlaylistEditorHeadingCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistEditorHeadingCopy(language)
            listOf(copy.create, copy.edit, copy.save).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) assertFalse(copy.save == english.save)
        }
        assertEquals("Zapisz inteligentną playlistę", smartPlaylistEditorHeadingCopy(AppLanguage.Polish).save)
    }

    @Test
    fun smartPlaylistMatchModesAreLocalized() {
        val english = smartPlaylistMatchCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistMatchCopy(language)
            listOf(copy.mode, copy.all, copy.any, copy.add).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) assertFalse(copy.all == english.all)
        }
        assertEquals("Spełnia wszystkie reguły", smartPlaylistMatchCopy(AppLanguage.Polish).all)
    }

    @Test
    fun smartPlaylistTextRulesAreLocalized() {
        val english = smartPlaylistTextRuleCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistTextRuleCopy(language)
            listOf(copy.title, copy.artist, copy.album, copy.genre, copy.folder).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) assertFalse(copy.title == english.title)
        }
        assertEquals("Tytuł zawiera", smartPlaylistTextRuleCopy(AppLanguage.Polish).title)
    }

    @Test
    fun smartPlaylistOtherRulesAreLocalized() {
        val english = smartPlaylistOtherRuleCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistOtherRuleCopy(language)
            listOf(copy.favorite, copy.duration, copy.playCount, copy.format).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) assertFalse(copy.duration == english.duration)
        }
        assertEquals("Liczba odtworzeń", smartPlaylistOtherRuleCopy(AppLanguage.Polish).playCount)
    }

    @Test
    fun smartPlaylistRuleValuesAreLocalized() {
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistValueCopy(language)
            listOf(copy.value, copy.yes, copy.no, copy.seconds).forEach { assertFalse(it.isBlank()) }
        }
        assertEquals("Wartość", smartPlaylistValueCopy(AppLanguage.Polish).value)
        assertEquals("Sekundy", smartPlaylistValueCopy(AppLanguage.Polish).seconds)
    }

    @Test
    fun smartPlaylistInputLabelsAreAvailable() {
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistInputCopy(language)
            listOf(copy.count, copy.extension, copy.folderText, copy.limit).forEach { assertFalse(it.isBlank()) }
        }
        assertEquals("Rozszerzenie", smartPlaylistInputCopy(AppLanguage.Polish).extension)
    }

    @Test
    fun smartPlaylistOrderLabelsAreAvailable() {
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistOrderCopy(language)
            listOf(copy.sort, copy.direction, copy.ascending, copy.descending).forEach { assertFalse(it.isBlank()) }
        }
        assertEquals("Rosnąco", smartPlaylistOrderCopy(AppLanguage.Polish).ascending)
        assertEquals("Malejąco", smartPlaylistOrderCopy(AppLanguage.Polish).descending)
    }

    @Test
    fun smartPlaylistActionsAreLocalized() {
        AppLanguage.entries.forEach { language ->
            val copy = smartPlaylistActionCopy(language)
            assertFalse(copy.shuffle.isBlank())
            assertFalse(copy.delete.isBlank())
            if (language != AppLanguage.English) assertFalse(copy.delete == "Delete smart playlist")
        }
        assertEquals("Usuń inteligentną playlistę", smartPlaylistActionCopy(AppLanguage.Polish).delete)
    }

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
        assertEquals("21 utworów", localizedCountLabel(21, "song", AppLanguage.Polish))
        assertEquals("22 utwory", localizedCountLabel(22, "song", AppLanguage.Polish))
        assertEquals("101 utworów", localizedCountLabel(101, "song", AppLanguage.Polish))
        assertEquals("2 artystów", localizedCountLabel(2, "artist", AppLanguage.Polish))
        assertEquals("21 playlist", localizedCountLabel(21, "playlist", AppLanguage.Polish))
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
    fun polishSearchResultCountsAgreeWithTheNumeral() {
        val copy = searchCopy(AppLanguage.Polish)
        assertEquals("1 pasujący utwór", copy.matchingSongs(1, AppLanguage.Polish))
        assertEquals("2 pasujące utwory", copy.matchingSongs(2, AppLanguage.Polish))
        assertEquals("5 pasujących utworów", copy.matchingSongs(5, AppLanguage.Polish))
        assertEquals("21 pasujących utworów", copy.matchingSongs(21, AppLanguage.Polish))
        assertEquals("22 pasujące albumy", copy.matchingAlbums(22, AppLanguage.Polish))
        assertEquals("2 pasujących artystów", copy.matchingArtists(2, AppLanguage.Polish))
    }

    @Test
    fun otherSlavicSongCountsUseTheirPluralForms() {
        assertEquals("5 skladeb", localizedCountLabel(5, "song", AppLanguage.Czech))
        assertEquals("21 skladeb", localizedCountLabel(21, "song", AppLanguage.Czech))
        assertEquals("22 skladeb", localizedCountLabel(22, "song", AppLanguage.Czech))
        assertEquals("5 skladieb", localizedCountLabel(5, "song", AppLanguage.Slovak))
        assertEquals("21 skladieb", localizedCountLabel(21, "song", AppLanguage.Slovak))
        assertEquals("5 песен", localizedCountLabel(5, "song", AppLanguage.Russian))
        assertEquals("5 пісень", localizedCountLabel(5, "song", AppLanguage.Ukrainian))
        assertEquals("5 песама", localizedCountLabel(5, "song", AppLanguage.Serbian))
        assertEquals("2 албума", localizedCountLabel(2, "album", AppLanguage.Serbian))
        assertEquals("3 извођача", localizedCountLabel(3, "artist", AppLanguage.Serbian))
        assertEquals("4 жанра", localizedCountLabel(4, "genre", AppLanguage.Serbian))
        assertEquals("5 pjesama", localizedCountLabel(5, "song", AppLanguage.Croatian))
        assertEquals("21 playlists", localizedCountLabel(21, "playlist", AppLanguage.English))
        assertEquals("21 lejátszási lista", localizedCountLabel(21, "playlist", AppLanguage.Hungarian))
    }

    @Test
    fun newEuropeanLanguagesUseAppropriateCountForms() {
        assertEquals("1 skladba", localizedCountLabel(1, "song", AppLanguage.Slovenian))
        assertEquals("2 skladbi", localizedCountLabel(2, "song", AppLanguage.Slovenian))
        assertEquals("3 skladbe", localizedCountLabel(3, "song", AppLanguage.Slovenian))
        assertEquals("5 skladb", localizedCountLabel(5, "song", AppLanguage.Slovenian))
        assertEquals("1 seznam predvajanja", localizedCountLabel(1, "playlist", AppLanguage.Slovenian))
        assertEquals("2 seznama predvajanja", localizedCountLabel(2, "playlist", AppLanguage.Slovenian))
        assertEquals("3 seznami predvajanja", localizedCountLabel(3, "playlist", AppLanguage.Slovenian))
        assertEquals("5 seznamov predvajanja", localizedCountLabel(5, "playlist", AppLanguage.Slovenian))
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
            val unavailable = audiobookUnavailableCopy(language)
            assertFalse(unavailable.title.isBlank())
            assertFalse(unavailable.message == audiobookUnavailableCopy(AppLanguage.English).message)
            assertFalse(unavailable.retry.isBlank())
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
