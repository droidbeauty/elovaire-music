package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_PERMISSION_DENIED_MESSAGE
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_POST_GRANT_FAILURE_MESSAGE
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationIssue
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationKind
import elovaire.music.droidbeauty.app.ui.screens.tags.TagEditFailureUi
import elovaire.music.droidbeauty.app.data.tags.TagEditFailureCause
import elovaire.music.droidbeauty.app.data.tags.TagEditValidationFailure
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteFailure
import elovaire.music.droidbeauty.app.data.lyrics.userMessage
import elovaire.music.droidbeauty.app.widget.widgetCopy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElovaireStringsTest {
    @Test
    fun bengaliCountsUsePeopleAndObjectClassifiers() {
        assertEquals("1জন শিল্পী", localizedCountLabel(1, "artist", AppLanguage.Bengali))
        assertEquals("2জন শিল্পী", localizedCountLabel(2, "artist", AppLanguage.Bengali))
        assertEquals("2টি গান", localizedCountLabel(2, "song", AppLanguage.Bengali))
        assertEquals("2টি অ্যালবাম", localizedCountLabel(2, "album", AppLanguage.Bengali))
        assertEquals("2টি প্লেলিস্ট", localizedCountLabel(2, "playlist", AppLanguage.Bengali))
        assertEquals("2টি মিল থাকা গান", searchCopy(AppLanguage.Bengali).matchingSongs(2, AppLanguage.Bengali))
        assertEquals("2জন মিল থাকা শিল্পী", searchCopy(AppLanguage.Bengali).matchingArtists(2, AppLanguage.Bengali))
    }
    @Test
    fun southAsianSearchResultsUseLocalizedCountedNouns() {
        val hindi = searchCopy(AppLanguage.Hindi)
        assertEquals("नतीजे: 1 गाना", hindi.matchingSongs(1, AppLanguage.Hindi))
        assertEquals("नतीजे: 2 गाने", hindi.matchingSongs(2, AppLanguage.Hindi))
        assertEquals("ਨਤੀਜੇ: 2 ਐਲਬਮਾਂ", searchCopy(AppLanguage.Punjabi).matchingAlbums(2, AppLanguage.Punjabi))
        assertEquals("نتائج: 2 گانے", searchCopy(AppLanguage.Urdu).matchingSongs(2, AppLanguage.Urdu))
    }
    @Test
    fun playbackSourceLocalizesOnlyTheAlbumFallback() {
        assertEquals("Nieznany album", localizedPlaybackSource("Unknown Album", "Unknown Album", null, AppLanguage.Polish))
        assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Unknown Album", 4L, AppLanguage.Polish))
        assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Another album", null, AppLanguage.Polish))
        assertEquals("A real album", localizedPlaybackSource("A real album", "A real album", null, AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            val localized = localizedPlaybackSource("Unknown Album", "Unknown Album", null, language)
            assertEquals(localizedAlbumTitle("Unknown Album", language), localized)
            assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Unknown Album", 4L, language))
            if (language != AppLanguage.English) assertFalse(localized == "Unknown Album")
        }
    }
    @Test
    fun externalAudioFallbackIsLocalizedWithoutChangingUserMetadata() {
        val externalId = -8_000_000_000_000L
        AppLanguage.entries.forEach { language ->
            val label = localizedExternalAudioText("External audio", externalId, language)
            assertFalse(label.isBlank())
            assertEquals(label, widgetCopy(language).externalAudio)
            if (language != AppLanguage.English) assertFalse(label == "External audio")
            assertEquals("External audio", localizedExternalAudioText("External audio", 42L, language))
            assertEquals("A real title", localizedExternalAudioText("A real title", externalId, language))
        }
        assertEquals("Zewnętrzny plik audio", localizedExternalAudioText("External audio", externalId, AppLanguage.Polish))
        assertEquals("External audio", localizedExternalAudioText("External audio", -7_000_000_000_000L, AppLanguage.Polish))
    }
    @Test
    fun latinAlbumAndSearchResultsUseCorrectDeclension() {
        val copy = searchCopy(AppLanguage.Latin)
        assertEquals("1 album inventum", copy.matchingAlbums(1, AppLanguage.Latin))
        assertEquals("2 alba inventa", copy.matchingAlbums(2, AppLanguage.Latin))
        assertEquals("1 artifex inventus", copy.matchingArtists(1, AppLanguage.Latin))
        assertEquals("2 artifices inventi", copy.matchingArtists(2, AppLanguage.Latin))
        assertTrue(homeCopy(AppLanguage.Latin).emptyLibraryMessage.contains("et alba"))
        assertEquals("2 albuma", localizedCountLabel(2, "album", AppLanguage.Bosnian))
        assertEquals("5 albuma", localizedCountLabel(5, "album", AppLanguage.Bosnian))
    }
    @Test
    fun searchResultCountsArePresentForEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = searchCopy(language)
            listOf(0, 1, 2, 5, 11, 21).forEach { count ->
                val labels = listOf(
                    copy.matchingArtists(count, language),
                    copy.matchingAlbums(count, language),
                    copy.matchingSongs(count, language),
                )
                labels.forEach { label ->
                    assertFalse("Empty search count for $language", label.isBlank())
                    assertTrue("Missing count for $language: $label", label.contains(count.toString()))
                    assertFalse("Unexpected period for $language: $label", label.endsWith('.'))
                }
            }
        }
    }
    @Test
    fun lithuanianSearchResultsUseCountedNounForms() {
        val copy = searchCopy(AppLanguage.Lithuanian)
        assertEquals("Rasta: 1 daina", copy.matchingSongs(1, AppLanguage.Lithuanian))
        assertEquals("Rasta: 2 dainos", copy.matchingSongs(2, AppLanguage.Lithuanian))
        assertEquals("Rasta: 11 dainų", copy.matchingSongs(11, AppLanguage.Lithuanian))
        assertEquals("Rasta: 21 daina", copy.matchingSongs(21, AppLanguage.Lithuanian))
    }
    @Test
    fun unsupportedFileAddressErrorIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val result = localizedTagSaveStatus("Unsupported URI.", 0, language)
            assertFalse(result.isBlank())
            assertFalse(result == "Unsupported URI.")
        }
        assertEquals("Adres pliku nie jest obsługiwany", localizedTagSaveStatus("Unsupported URI.", 0, AppLanguage.Polish))
        assertEquals("The file address is unsupported", localizedTagSaveStatus("Unsupported URI.", 0, AppLanguage.English))
        assertEquals("Provider-specific diagnostic", localizedTagSaveStatus("Provider-specific diagnostic", 0, AppLanguage.Polish))
        val failure = TagEditFailureUi(1, "track", "Unsupported URI.", TagEditFailureCause.CannotOpenOutput)
        assertEquals("Adres pliku nie jest obsługiwany", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun englishPrivacySectionsFollowSentencePunctuationRules() {
        val separator = Regex("(?<!\\.)\\.\\s+(?=[A-Z])")
        privacyPolicyCopy(AppLanguage.English).sections.forEach { section ->
            assertFalse(section.title.endsWith('.'))
            assertFalse(section.body.endsWith('.'))
            assertTrue("Too many sentence separators in ${section.title}", separator.findAll(section.body).count() <= 1)
        }
    }
    @Test
    fun builtInEqualizerPresetLabelsPreserveUnknownNames() {
        assertEquals("Klasyczna", localizedEqualizerPreset("Classical", AppLanguage.Polish))
        assertEquals("My preset", localizedEqualizerPreset("My preset", AppLanguage.Polish))
        assertEquals("Classical", localizedEqualizerPreset("Classical", AppLanguage.English))
        assertEquals("ジャズ", localizedEqualizerPreset("Jazz", AppLanguage.Japanese))
        assertEquals("Хіп-хоп", localizedEqualizerPreset("Hip-Hop", AppLanguage.Ukrainian))
        assertEquals("R&B", localizedEqualizerPreset("R&B", AppLanguage.Japanese))
        val nonLatinLanguages = listOf(
            AppLanguage.Bengali, AppLanguage.Bulgarian, AppLanguage.ChineseSimplified,
            AppLanguage.Greek, AppLanguage.Hindi, AppLanguage.Japanese, AppLanguage.Korean,
            AppLanguage.Macedonian, AppLanguage.Punjabi, AppLanguage.Russian, AppLanguage.Serbian,
            AppLanguage.Thai, AppLanguage.Ukrainian, AppLanguage.Urdu,
        )
        nonLatinLanguages.forEach { language ->
            listOf("Jazz", "Pop", "Rock", "Metal", "Soul", "Hip-Hop").forEach { preset ->
                assertFalse("$preset is untranslated for $language", localizedEqualizerPreset(preset, language) == preset)
            }
        }
        AppLanguage.entries.forEach { language ->
            listOf("Electronic", "Classical", "Acoustic", "Vocal").forEach { preset ->
                val label = localizedEqualizerPreset(preset, language)
                assertFalse(label.isBlank())
                if (language != AppLanguage.English) assertFalse("$preset is untranslated for $language", label == preset)
            }
            assertEquals("Personal Mix", localizedEqualizerPreset("Personal Mix", language))
            assertEquals("R&B", localizedEqualizerPreset("R&B", language))
        }
    }
    @Test
    fun customEqualizerStatusIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val label = equalizerCustomLabel(language)
            assertFalse(label.isBlank())
            if (language != AppLanguage.English) assertFalse(label == "Custom")
            assertTrue(equalizerStatusLabel(language, label).contains(label))
        }
        assertEquals("Wybrano: własne", equalizerStatusLabel(AppLanguage.Polish, equalizerCustomLabel(AppLanguage.Polish)))
    }
    @Test
    fun estonianSearchResultsAgreeWithCount() {
        val copy = searchCopy(AppLanguage.Estonian)
        assertEquals("1 sobiv artist", copy.matchingArtists(1, AppLanguage.Estonian))
        assertEquals("2 sobivat artisti", copy.matchingArtists(2, AppLanguage.Estonian))
        assertEquals("1 sobiv album", copy.matchingAlbums(1, AppLanguage.Estonian))
        assertEquals("2 sobivat albumit", copy.matchingAlbums(2, AppLanguage.Estonian))
    }
    @Test
    fun searchResultLabelsUseLocalizedCountedNouns() {
        assertEquals("Αποτελέσματα: 1 τραγούδι", searchCopy(AppLanguage.Greek).matchingSongs(1, AppLanguage.Greek))
        assertEquals("Αποτελέσματα: 2 τραγούδια", searchCopy(AppLanguage.Greek).matchingSongs(2, AppLanguage.Greek))
        assertEquals("Találatok: 2 dal", searchCopy(AppLanguage.Hungarian).matchingSongs(2, AppLanguage.Hungarian))
        assertEquals("Rezultate: 2 artistë", searchCopy(AppLanguage.Albanian).matchingArtists(2, AppLanguage.Albanian))
    }
    @Test
    fun bulgarianAndMacedonianSearchResultsUseCountedNouns() {
        val bulgarian = searchCopy(AppLanguage.Bulgarian)
        assertEquals("Резултати: 1 албум", bulgarian.matchingAlbums(1, AppLanguage.Bulgarian))
        assertEquals("Резултати: 2 албума", bulgarian.matchingAlbums(2, AppLanguage.Bulgarian))
        val macedonian = searchCopy(AppLanguage.Macedonian)
        assertEquals("Резултати: 1 песна", macedonian.matchingSongs(1, AppLanguage.Macedonian))
        assertEquals("Резултати: 2 песни", macedonian.matchingSongs(2, AppLanguage.Macedonian))
    }
    @Test
    fun southSlavicSearchResultsUseCountedNounForms() {
        val bosnian = searchCopy(AppLanguage.Bosnian)
        assertEquals("Pronađeno: 1 pjesma", bosnian.matchingSongs(1, AppLanguage.Bosnian))
        assertEquals("Pronađeno: 2 pjesme", bosnian.matchingSongs(2, AppLanguage.Bosnian))
        assertEquals("Pronađeno: 5 pjesama", bosnian.matchingSongs(5, AppLanguage.Bosnian))
        assertEquals("Pronađeno: 5 pjesama", searchCopy(AppLanguage.Croatian).matchingSongs(5, AppLanguage.Croatian))
        assertEquals("Пронађено: 5 песама", searchCopy(AppLanguage.Serbian).matchingSongs(5, AppLanguage.Serbian))
    }
    @Test
    fun eastSlavicSearchResultsUseCountedNounForms() {
        val russian = searchCopy(AppLanguage.Russian)
        assertEquals("Найдено: 1 исполнитель", russian.matchingArtists(1, AppLanguage.Russian))
        assertEquals("Найдено: 2 исполнителя", russian.matchingArtists(2, AppLanguage.Russian))
        assertEquals("Найдено: 5 песен", russian.matchingSongs(5, AppLanguage.Russian))
        val ukrainian = searchCopy(AppLanguage.Ukrainian)
        assertEquals("Знайдено: 1 альбом", ukrainian.matchingAlbums(1, AppLanguage.Ukrainian))
        assertEquals("Знайдено: 2 альбоми", ukrainian.matchingAlbums(2, AppLanguage.Ukrainian))
        assertEquals("Знайдено: 5 альбомів", ukrainian.matchingAlbums(5, AppLanguage.Ukrainian))
    }
    @Test
    fun slovakSearchResultsUseCorrectNumberAndGender() {
        val copy = searchCopy(AppLanguage.Slovak)
        assertEquals("1 nájdený interpret", copy.matchingArtists(1, AppLanguage.Slovak))
        assertEquals("2 nájdení interpreti", copy.matchingArtists(2, AppLanguage.Slovak))
        assertEquals("1 nájdený album", copy.matchingAlbums(1, AppLanguage.Slovak))
        assertEquals("2 nájdené albumy", copy.matchingAlbums(2, AppLanguage.Slovak))
        assertEquals("1 nájdená skladba", copy.matchingSongs(1, AppLanguage.Slovak))
        assertEquals("5 nájdených skladieb", copy.matchingSongs(5, AppLanguage.Slovak))
    }
    @Test
    fun czechSearchResultsUseCorrectNumberAndGender() {
        val copy = searchCopy(AppLanguage.Czech)
        assertEquals("1 nalezený umělec", copy.matchingArtists(1, AppLanguage.Czech))
        assertEquals("2 nalezení umělci", copy.matchingArtists(2, AppLanguage.Czech))
        assertEquals("1 nalezené album", copy.matchingAlbums(1, AppLanguage.Czech))
        assertEquals("2 nalezená alba", copy.matchingAlbums(2, AppLanguage.Czech))
        assertEquals("1 nalezená skladba", copy.matchingSongs(1, AppLanguage.Czech))
        assertEquals("5 nalezených skladeb", copy.matchingSongs(5, AppLanguage.Czech))
    }
    @Test
    fun scandinavianSearchResultsAgreeWithTheirCount() {
        val danish = searchCopy(AppLanguage.Danish)
        assertEquals("1 matchende kunstner", danish.matchingArtists(1, AppLanguage.Danish))
        assertEquals("2 matchende sange", danish.matchingSongs(2, AppLanguage.Danish))
        val swedish = searchCopy(AppLanguage.Swedish)
        assertEquals("1 matchande låt", swedish.matchingSongs(1, AppLanguage.Swedish))
        assertEquals("2 matchande artister", swedish.matchingArtists(2, AppLanguage.Swedish))
    }
    @Test
    fun romanianSearchResultsAgreeWithCountAndGender() {
        val copy = searchCopy(AppLanguage.Romanian)
        assertEquals("1 artist găsit", copy.matchingArtists(1, AppLanguage.Romanian))
        assertEquals("20 de artiști găsiți", copy.matchingArtists(20, AppLanguage.Romanian))
        assertEquals("1 melodie găsită", copy.matchingSongs(1, AppLanguage.Romanian))
        assertEquals("2 albume găsite", copy.matchingAlbums(2, AppLanguage.Romanian))
    }
    @Test
    fun finnishSearchResultsUseSingularAndPartitive() {
        val copy = searchCopy(AppLanguage.Finnish)
        assertEquals("1 vastaava esittäjä", copy.matchingArtists(1, AppLanguage.Finnish))
        assertEquals("2 vastaavaa esittäjää", copy.matchingArtists(2, AppLanguage.Finnish))
        assertEquals("1 vastaava albumi", copy.matchingAlbums(1, AppLanguage.Finnish))
        assertEquals("2 vastaavaa kappaletta", copy.matchingSongs(2, AppLanguage.Finnish))
    }
    @Test
    fun catalanSearchResultsAgreeWithCountAndGender() {
        val copy = searchCopy(AppLanguage.Catalan)
        assertEquals("1 artista trobat", copy.matchingArtists(1, AppLanguage.Catalan))
        assertEquals("2 àlbums trobats", copy.matchingAlbums(2, AppLanguage.Catalan))
        assertEquals("1 cançó trobada", copy.matchingSongs(1, AppLanguage.Catalan))
        assertEquals("2 cançons trobades", copy.matchingSongs(2, AppLanguage.Catalan))
    }
    @Test
    fun dutchSearchResultsAgreeWithCountAndGender() {
        val copy = searchCopy(AppLanguage.Dutch)
        assertEquals("1 overeenkomende artiest", copy.matchingArtists(1, AppLanguage.Dutch))
        assertEquals("1 overeenkomend album", copy.matchingAlbums(1, AppLanguage.Dutch))
        assertEquals("1 overeenkomend nummer", copy.matchingSongs(1, AppLanguage.Dutch))
        assertEquals("2 overeenkomende nummers", copy.matchingSongs(2, AppLanguage.Dutch))
    }
    @Test
    fun romanceSearchResultsAgreeWithTheirCount() {
        assertEquals("1 canción coincidente", searchCopy(AppLanguage.Spanish).matchingSongs(1, AppLanguage.Spanish))
        assertEquals("2 canciones coincidentes", searchCopy(AppLanguage.Spanish).matchingSongs(2, AppLanguage.Spanish))
        assertEquals("1 música correspondente", searchCopy(AppLanguage.Portuguese).matchingSongs(1, AppLanguage.Portuguese))
        assertEquals("2 álbuns correspondentes", searchCopy(AppLanguage.Portuguese).matchingAlbums(2, AppLanguage.Portuguese))
        assertEquals("1 artista corrispondente", searchCopy(AppLanguage.Italian).matchingArtists(1, AppLanguage.Italian))
        assertEquals("2 artisti corrispondenti", searchCopy(AppLanguage.Italian).matchingArtists(2, AppLanguage.Italian))
    }
    @Test
    fun germanSearchResultsAgreeWithCountAndGender() {
        val copy = searchCopy(AppLanguage.German)
        assertEquals("1 passender Künstler", copy.matchingArtists(1, AppLanguage.German))
        assertEquals("2 passende Künstler", copy.matchingArtists(2, AppLanguage.German))
        assertEquals("1 passendes Album", copy.matchingAlbums(1, AppLanguage.German))
        assertEquals("2 passende Alben", copy.matchingAlbums(2, AppLanguage.German))
        assertEquals("1 passender Titel", copy.matchingSongs(1, AppLanguage.German))
    }
    @Test
    fun frenchSearchResultsAgreeWithTheirCount() {
        val copy = searchCopy(AppLanguage.French)
        assertEquals("1 artiste correspondant", copy.matchingArtists(1, AppLanguage.French))
        assertEquals("2 artistes correspondants", copy.matchingArtists(2, AppLanguage.French))
        assertEquals("1 album correspondant", copy.matchingAlbums(1, AppLanguage.French))
        assertEquals("2 morceaux correspondants", copy.matchingSongs(2, AppLanguage.French))
    }
    @Test
    fun slovenianSearchResultsRespectSingularDualAndPlural() {
        val copy = searchCopy(AppLanguage.Slovenian)
        assertEquals("1 najdena skladba", copy.matchingSongs(1, AppLanguage.Slovenian))
        assertEquals("2 najdeni skladbi", copy.matchingSongs(2, AppLanguage.Slovenian))
        assertEquals("3 najdene skladbe", copy.matchingSongs(3, AppLanguage.Slovenian))
        assertEquals("5 najdenih skladb", copy.matchingSongs(5, AppLanguage.Slovenian))
        assertEquals("2 najdena albuma", copy.matchingAlbums(2, AppLanguage.Slovenian))
        assertEquals("3 najdeni izvajalci", copy.matchingArtists(3, AppLanguage.Slovenian))
    }
    @Test
    fun latvianCountsUsePluralExceptForZeroAndTeens() {
        assertEquals("1 dziesma", localizedCountLabel(1, "song", AppLanguage.Latvian))
        assertEquals("5 dziesmas", localizedCountLabel(5, "song", AppLanguage.Latvian))
        assertEquals("11 dziesmu", localizedCountLabel(11, "song", AppLanguage.Latvian))
        assertEquals("19 dziesmu", localizedCountLabel(19, "song", AppLanguage.Latvian))
        assertEquals("25 dziesmas", localizedCountLabel(25, "song", AppLanguage.Latvian))
        assertEquals("29 dziesmas", localizedCountLabel(29, "song", AppLanguage.Latvian))
        assertEquals("30 dziesmu", localizedCountLabel(30, "song", AppLanguage.Latvian))
        assertEquals("111 dziesmu", localizedCountLabel(111, "song", AppLanguage.Latvian))
        assertEquals("21 dziesma", localizedCountLabel(21, "song", AppLanguage.Latvian))
        val search = searchCopy(AppLanguage.Latvian)
        assertEquals("1 atbilstoša dziesma", search.matchingSongs(1, AppLanguage.Latvian))
        assertEquals("5 atbilstošas dziesmas", search.matchingSongs(5, AppLanguage.Latvian))
        assertEquals("11 atbilstošu dziesmu", search.matchingSongs(11, AppLanguage.Latvian))
        assertEquals("2 atbilstoši mākslinieki", search.matchingArtists(2, AppLanguage.Latvian))
    }
    @Test
    fun unknownAudiobookAuthorsAreLocalizedWithoutChangingRealNames() {
        AppLanguage.entries.forEach { language ->
            val unknown = localizedAudiobookAuthor("Unknown Author", language)
            assertFalse(unknown.isBlank())
            assertEquals(unknown, localizedAudiobookAuthor("", language))
            assertEquals("Jane Austen", localizedAudiobookAuthor("Jane Austen", language))
            if (language != AppLanguage.English) assertFalse(unknown == "Unknown Author")
        }
        assertEquals("Nieznany autor", localizedAudiobookAuthor("Unknown Author", AppLanguage.Polish))
    }
    @Test
    fun androidAutoGuidanceIsTranslatedForEveryLanguage() {
        val english = carMediaCopy(AppLanguage.English)
        AppLanguage.entries.forEach { language ->
            val copy = carMediaCopy(language)
            listOf(copy.openPhone, copy.grantAccess, copy.addMusic).forEach { assertFalse(it.isBlank()) }
            if (language != AppLanguage.English) {
                assertFalse(copy.openPhone == english.openPhone)
                assertFalse(copy.grantAccess == english.grantAccess)
                assertFalse(copy.addMusic == english.addMusic)
            }
        }
        assertEquals("Otwórz Elovaire na telefonie", carMediaCopy(AppLanguage.Polish).openPhone)
    }
    @Test
    fun genericAudioFormatIsLocalizedWithoutChangingCodecNames() {
        AppLanguage.entries.forEach { language ->
            assertFalse(localizedAudioFormat("AUDIO", language).isBlank())
            assertEquals(localizedAudioFormat("AUDIO", language), localizedAudioFormat("", language))
            assertEquals("FLAC", localizedAudioFormat("FLAC", language))
        }
        assertEquals("DŹWIĘK", localizedAudioFormat("AUDIO", AppLanguage.Polish))
    }
    @Test
    fun favoriteAccessibilityStatesAreLocalized() {
        AppLanguage.entries.forEach { language ->
            val add = songFavoriteAccessibilityLabel(false, language)
            val remove = songFavoriteAccessibilityLabel(true, language)
            assertFalse(add.isBlank())
            assertFalse(remove.isBlank())
            assertFalse(add == remove)
            if (language != AppLanguage.English) {
                assertFalse(add == "Like song")
                assertFalse(remove == "Unlike song")
            }
        }
        assertEquals("Usuń utwór z ulubionych", songFavoriteAccessibilityLabel(true, AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            val enable = crossfadeToggleAccessibilityLabel(false, language)
            val disable = crossfadeToggleAccessibilityLabel(true, language)
            assertFalse(enable.isBlank())
            assertFalse(disable.isBlank())
            assertFalse(enable == disable)
            if (language != AppLanguage.English) {
                assertFalse(enable == "Enable crossfade")
                assertFalse(disable == "Disable crossfade")
            }
        }
    }
    @Test
    fun artworkPlaceholderAccessibilityIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val label = artworkPlaceholderLabel(language)
            assertFalse(label.isBlank())
            if (language != AppLanguage.English) assertFalse(label == "Artwork placeholder")
        }
        assertEquals("Obraz zastępczy okładki", artworkPlaceholderLabel(AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            val label = editLyricsAccessibilityLabel(language)
            assertFalse(label.isBlank())
            if (language != AppLanguage.English) assertFalse(label == "Edit lyrics")
        }
    }
    @Test
    fun unknownAlbumFallbackIsLocalizedWithoutChangingRealTitles() {
        AppLanguage.entries.forEach { language ->
            val unknown = localizedAlbumTitle("Unknown Album", language)
            assertFalse(unknown.isBlank())
            assertEquals("Known Album", localizedAlbumTitle("Known Album", language))
            if (language != AppLanguage.English) assertFalse(unknown == "Unknown Album")
        }
        assertEquals("Nieznany album", localizedAlbumTitle("Unknown Album", AppLanguage.Polish))
    }
    @Test
    fun unknownArtistFallbackIsLocalizedWithoutChangingRealNames() {
        AppLanguage.entries.forEach { language ->
            val unknown = localizedArtistName("Unknown Artist", language)
            assertFalse(unknown.isBlank())
            assertEquals(unknown, localizedArtistName("", language))
            assertEquals("Known Artist", localizedArtistName("Known Artist", language))
            if (language != AppLanguage.English) assertFalse(unknown == "Unknown Artist")
        }
        assertEquals("Nieznany wykonawca", localizedArtistName("Unknown Artist", AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            assertEquals(localizedArtistName("Unknown Artist", language), widgetCopy(language).unknownArtist)
            assertEquals(pauseLabel(language), widgetCopy(language).pause)
        }
    }
    @Test
    fun longerSharedCopyDoesNotFallBackToEnglish() {
        fun copies(language: AppLanguage): List<Any> = listOf(
            rootUiCopy(language), libraryFoldersCopy(language), networkSourcesCopy(language),
            playlistAccessibilityCopy(language), settingsAccessibilityCopy(language),
            playerControlAccessibilityCopy(language), mediaOptionsAccessibilityCopy(language),
            lyricsAccessibilityCopy(language), navigationAccessibilityCopy(language),
            playbackNavigationAccessibilityCopy(language), shuffleAccessibilityCopy(language),
            viewModeAccessibilityCopy(language), sleepTimerCopy(language), commonUiCopy(language),
            playlistMainCopy(language), equalizerCopy(language), settingsCopy(language),
            libraryScanStatusCopy(language), playlistManagementCopy(language), crossfadeCopy(language),
            aboutUpdateCopy(language), updateActionCopy(language), artistInfoCopy(language),
            smartPlaylistOrderCopy(language), smartPlaylistInputCopy(language),
            smartPlaylistValueCopy(language), smartPlaylistOtherRuleCopy(language),
            smartPlaylistTextRuleCopy(language), searchCopy(language), nowPlayingBarStyleCopy(language),
            audiobookExtraCopy(language), audiobookSettingsCopy(language), upscalerCopy(language),
            audiobookUnavailableCopy(language), audiobookDescriptionCopy(language), audiobookCopy(language),
            smartPlaylistSettingsCopy(language), smartPlaylistActionCopy(language), smartPlaylistMatchCopy(language),
            smartPlaylistEditorHeadingCopy(language), smartPlaylistMissingCopy(language),
            smartPlaylistDeleteCopy(language), smartPlaylistEmptyCopy(language), homeCopy(language),
            tagAdditionalIssuesCopy(language), carMediaCopy(language),
        )
        val english = copies(AppLanguage.English)
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            english.zip(copies(language)).forEach { (source, translated) ->
                source.javaClass.declaredFields.filter { it.type == String::class.java }.forEach { field ->
                    field.isAccessible = true
                    val original = field.get(source) as String
                    if (original.length >= 15) {
                        assertFalse("${source.javaClass.simpleName}.${field.name} is untranslated for $language", field.get(translated) == original)
                    }
                }
            }
        }
    }
    @Test
    fun eastAsianLibraryCountsUseLocalizedNouns() {
        val expected = mapOf(
            AppLanguage.ChineseSimplified to listOf("2 首歌曲", "2 首歌曲", "2 张专辑", "2 位艺术家", "2 种曲风", "2 本有声书"),
            AppLanguage.Japanese to listOf("2 曲", "2 曲", "2 枚のアルバム", "2 組のアーティスト", "2 種類のジャンル", "2 冊のオーディオブック"),
            AppLanguage.Korean to listOf("2 곡", "2 곡", "2 개의 앨범", "2 명의 아티스트", "2 개의 장르", "2 권의 오디오북"),
        )
        expected.forEach { (language, labels) ->
            listOf("song", "track", "album", "artist", "genre", "audiobook").zip(labels).forEach { (noun, label) ->
                assertEquals(label, localizedCountLabel(2, noun, language))
            }
        }
        assertEquals("2 个播放列表", localizedCountLabel(2, "playlist", AppLanguage.ChineseSimplified))
        assertEquals("2 件のプレイリスト", localizedCountLabel(2, "playlist", AppLanguage.Japanese))
        assertEquals("2 개의 재생목록", localizedCountLabel(2, "playlist", AppLanguage.Korean))
    }

    @Test
    fun audiobookCountsDoNotUseEnglishFallbacks() {
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            listOf(0, 1, 2, 5, 21).forEach { count ->
                val label = localizedCountLabel(count, "audiobook", language)
                if (language != AppLanguage.Polish || count != 1) {
                    assertFalse("Audiobook count is untranslated for $language: $label", label == localizedCountLabel(count, "audiobook", AppLanguage.English))
                }
            }
        }
    }

    @Test
    fun songCountsDoNotUseEnglishFallbacks() {
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            listOf(0, 1, 2, 5, 21).forEach { count ->
                val label = localizedCountLabel(count, "song", language)
                assertFalse("Song count is untranslated for $language: $label", label == localizedCountLabel(count, "song", AppLanguage.English))
            }
        }
    }

    @Test
    fun trackCountsDoNotUseEnglishFallbacks() {
        AppLanguage.entries.filterNot { it == AppLanguage.English || it == AppLanguage.Dutch }.forEach { language ->
            listOf(0, 1, 2, 5, 21).forEach { count ->
                val label = localizedCountLabel(count, "track", language)
                assertFalse("Track count is untranslated for $language: $label", label == localizedCountLabel(count, "track", AppLanguage.English))
            }
        }
    }
    @Test
    fun playlistCreationStoreErrorsAreLocalized() {
        val errors = listOf(
            "User-data queue is full.",
            "User-data store is released.",
            "User-data store failed.",
            "Mutation did not produce a result.",
            "Database operation failed.",
            "User-data operation failed.",
            "User-data actor failed.",
        )
        errors.forEach { message ->
            val english = localizedPlaylistCreateError(message, AppLanguage.English)
            assertFalse(english.endsWith('.'))
            AppLanguage.entries.filter { it != AppLanguage.English }.forEach { language ->
                val translated = localizedPlaylistCreateError(message, language)
                assertFalse("$message is empty for $language", translated.isBlank())
                assertFalse("$message is untranslated for $language", translated == english || translated == message)
            }
        }
    }
    @Test
    fun appOwnedUpdateErrorsHaveTranslationsForEveryLanguage() {
        val messages = listOf(
            "Update asset name is invalid",
            "Update download failed",
            "Downloaded update is incomplete",
            "Downloaded update is invalid",
            "Unable to verify update",
            "Update verification failed",
            "Downloaded update is not a valid app",
            "Downloaded update targets another app",
            "Downloaded update version is invalid",
            "Downloaded update is not newer",
            "Downloaded update is signed by an unknown certificate",
            "Allow installing updates from this source first",
            "Unable to open install permission settings",
            "Unable to persist update installer handoff",
            "The staged update is no longer available",
            "The update was not installed",
            "Unable to check for updates",
            "GitHub update check failed",
            "Update source is invalid",
            "Update source redirect is invalid",
            "Too many update redirects",
        )
        messages.forEach { message ->
            val english = localizedUpdateError(message, AppLanguage.English)
            assertFalse(english.endsWith('.'))
            AppLanguage.entries.filter { it != AppLanguage.English }.forEach { language ->
                val translated = localizedUpdateError(message, language)
                assertFalse("$message is empty for $language", translated.isBlank())
                assertFalse("$message is untranslated for $language", translated == english || translated == message)
            }
        }
    }
    @Test
    fun playlistQueueFailureIsLocalized() {
        val message = "User-data queue is full."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(message, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(message, AppLanguage.English))
            assertEquals("Provider detail.", localizedPlaylistCreateError("Provider detail.", language))
        }
        assertEquals("Kolejka operacji na danych użytkownika jest pełna", localizedPlaylistCreateError(message, AppLanguage.Polish))
        val database = "Database operation failed."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(database, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(database, AppLanguage.English))
        }
        assertEquals("Operacja na bazie danych nie powiodła się", localizedPlaylistCreateError(database, AppLanguage.Polish))
        val operation = "User-data operation failed."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(operation, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(operation, AppLanguage.English))
        }
        assertEquals("Operacja na danych użytkownika nie powiodła się", localizedPlaylistCreateError(operation, AppLanguage.Polish))
        val store = "User-data store failed."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(store, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(store, AppLanguage.English))
        }
        assertEquals("Magazyn danych użytkownika uległ awarii", localizedPlaylistCreateError(store, AppLanguage.Polish))
        val closed = "User-data store is released."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(closed, language)
            assertFalse(translated.isBlank())
            assertFalse(translated == localizedPlaylistCreateError(store, language))
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(closed, AppLanguage.English))
        }
        assertEquals("Magazyn danych użytkownika jest zamknięty", localizedPlaylistCreateError(closed, AppLanguage.Polish))
        val noResult = "Mutation did not produce a result."
        AppLanguage.entries.forEach { language ->
            val translated = localizedPlaylistCreateError(noResult, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedPlaylistCreateError(noResult, AppLanguage.English))
            assertEquals(localizedPlaylistCreateError(store, language), localizedPlaylistCreateError("User-data actor failed.", language))
        }
        assertEquals("Zmiana nie zwróciła wyniku", localizedPlaylistCreateError(noResult, AppLanguage.Polish))
    }
    @Test
    fun updateInstallationFailureIsLocalized() {
        val message = "The update was not installed"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(message, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == message)
            assertEquals("Other error.", localizedUpdateError("Other error.", language))
        }
        assertEquals("Aktualizacja nie została zainstalowana", localizedUpdateError(message, AppLanguage.Polish))
        val staged = "The staged update is no longer available"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(staged, language)
            assertFalse(translated.isBlank())
            assertFalse(translated == localizedUpdateError(message, language))
            if (language != AppLanguage.English) assertFalse(translated == staged)
        }
        assertEquals("Przygotowana aktualizacja nie jest już dostępna", localizedUpdateError(staged, AppLanguage.Polish))
        val permission = "Allow installing updates from this source first"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(permission, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == permission)
        }
        assertEquals("Najpierw zezwól na instalowanie aktualizacji z tego źródła", localizedUpdateError(permission, AppLanguage.Polish))
        val download = "Update download failed"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(download, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == download)
        }
        assertEquals("Nie udało się pobrać aktualizacji", localizedUpdateError(download, AppLanguage.Polish))
        val incomplete = "Downloaded update is incomplete"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(incomplete, language)
            assertFalse(translated.isBlank())
            assertFalse(translated == localizedUpdateError(download, language))
            if (language != AppLanguage.English) assertFalse(translated == incomplete)
        }
        assertEquals("Pobrana aktualizacja jest niekompletna", localizedUpdateError(incomplete, AppLanguage.Polish))
        val verification = "Update verification failed"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(verification, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == verification)
        }
        assertEquals("Weryfikacja aktualizacji nie powiodła się", localizedUpdateError(verification, AppLanguage.Polish))
        val cannotVerify = "Unable to verify update"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(cannotVerify, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == cannotVerify)
        }
        assertEquals("Nie można zweryfikować aktualizacji", localizedUpdateError(cannotVerify, AppLanguage.Polish))
        assertFalse(localizedUpdateError(cannotVerify, AppLanguage.Lithuanian) == localizedUpdateError(verification, AppLanguage.Lithuanian))
        val certificate = "Downloaded update is signed by an unknown certificate"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(certificate, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == certificate)
        }
        assertEquals("Pobrana aktualizacja jest podpisana nieznanym certyfikatem", localizedUpdateError(certificate, AppLanguage.Polish))
        val wrongApp = "Downloaded update targets another app"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(wrongApp, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == wrongApp)
        }
        assertEquals("Pobrana aktualizacja jest przeznaczona dla innej aplikacji", localizedUpdateError(wrongApp, AppLanguage.Polish))
        val invalidVersion = "Downloaded update version is invalid"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(invalidVersion, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == invalidVersion)
        }
        assertEquals("Wersja pobranej aktualizacji jest nieprawidłowa", localizedUpdateError(invalidVersion, AppLanguage.Polish))
        val notNewer = "Downloaded update is not newer"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(notNewer, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == notNewer)
        }
        assertEquals("Pobrana aktualizacja nie jest nowsza", localizedUpdateError(notNewer, AppLanguage.Polish))
        val invalidApp = "Downloaded update is not a valid app"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(invalidApp, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == invalidApp)
        }
        assertEquals("Pobrana aktualizacja nie jest prawidłową aplikacją", localizedUpdateError(invalidApp, AppLanguage.Polish))
        val invalidDownload = "Downloaded update is invalid"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(invalidDownload, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == invalidDownload)
        }
        assertEquals("Pobrana aktualizacja jest nieprawidłowa", localizedUpdateError(invalidDownload, AppLanguage.Polish))
        val check = "Unable to check for updates"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(check, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == check)
        }
        assertEquals("Nie można sprawdzić dostępności aktualizacji", localizedUpdateError(check, AppLanguage.Polish))
        val github = "GitHub update check failed"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(github, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == github)
        }
        assertEquals("Sprawdzanie aktualizacji na GitHubie nie powiodło się", localizedUpdateError(github, AppLanguage.Polish))
        val source = "Update source is invalid"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(source, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == source)
        }
        assertEquals("Źródło aktualizacji jest nieprawidłowe", localizedUpdateError(source, AppLanguage.Polish))
        val fileName = "Update asset name is invalid"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(fileName, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == fileName)
        }
        assertEquals("Nazwa pliku aktualizacji jest nieprawidłowa", localizedUpdateError(fileName, AppLanguage.Polish))
        val redirects = "Too many update redirects"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(redirects, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == redirects)
        }
        assertEquals("Zbyt wiele przekierowań podczas aktualizacji", localizedUpdateError(redirects, AppLanguage.Polish))
        val invalidRedirect = "Update source redirect is invalid"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(invalidRedirect, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == invalidRedirect)
        }
        assertEquals("Przekierowanie źródła aktualizacji jest nieprawidłowe", localizedUpdateError(invalidRedirect, AppLanguage.Polish))
        val permissionSettings = "Unable to open install permission settings"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(permissionSettings, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == permissionSettings)
        }
        assertEquals("Nie można otworzyć ustawień zezwolenia na instalację", localizedUpdateError(permissionSettings, AppLanguage.Polish))
        val handoff = "Unable to persist update installer handoff"
        AppLanguage.entries.forEach { language ->
            val translated = localizedUpdateError(handoff, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == localizedUpdateError(handoff, AppLanguage.English))
        }
        assertEquals("Nie można zapisać stanu przekazania aktualizacji do instalatora", localizedUpdateError(handoff, AppLanguage.Polish))
    }
    @Test
    fun sourceFileOpenFailureIsLocalized() {
        val status = "Unable to open the source file."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveStatus(status, 0, AppLanguage.English))
        }
        assertEquals("Nie można otworzyć pliku źródłowego", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
        val writeStatus = "Unable to open the file for writing."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(writeStatus, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveStatus(writeStatus, 0, AppLanguage.English))
        }
        assertEquals("Nie można otworzyć pliku do zapisu", localizedTagSaveStatus(writeStatus, 0, AppLanguage.Polish))
        listOf(status, writeStatus).forEach { reason ->
            val failure = TagEditFailureUi(1, "track", reason, TagEditFailureCause.CannotOpenOutput)
            AppLanguage.entries.forEach { language ->
                assertEquals(localizedTagSaveStatus(reason, 0, language), localizedTagSaveFailure(failure, language))
            }
        }
        val externalFailure = TagEditFailureUi(1, "track", "Provider detail.", TagEditFailureCause.CannotOpenOutput)
        assertEquals("Provider detail.", localizedTagSaveFailure(externalFailure, AppLanguage.Polish))
    }
    @Test
    fun lyricsPermissionErrorsAreLocalizedWithoutHidingProviderDetails() {
        AppLanguage.entries.forEach { language ->
            val denied = localizedLyricsSaveError(LYRICS_PERMISSION_DENIED_MESSAGE, language)
            val afterGrant = localizedLyricsSaveError(LYRICS_POST_GRANT_FAILURE_MESSAGE, language)
            assertFalse(denied.isBlank())
            assertFalse(afterGrant.isBlank())
            assertFalse(denied == afterGrant)
            if (language != AppLanguage.English) {
                assertFalse(denied == LYRICS_PERMISSION_DENIED_MESSAGE)
                assertFalse(afterGrant == LYRICS_POST_GRANT_FAILURE_MESSAGE)
            }
            assertEquals("Provider detail", localizedLyricsSaveError("Provider detail", language))
            assertEquals(afterGrant, localizedLyricsSaveError(EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant.userMessage, language))
            val rollback = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.RollbackFailed.userMessage, language)
            assertFalse(rollback.isBlank())
            if (language != AppLanguage.English) assertFalse(rollback == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.RollbackFailed.userMessage, AppLanguage.English))
            val tooLarge = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.LyricsTooLarge.userMessage, language)
            assertFalse(tooLarge.isBlank())
            if (language != AppLanguage.English) assertFalse(tooLarge == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.LyricsTooLarge.userMessage, AppLanguage.English))
            val unavailable = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.WriteAccessUnavailable.userMessage, language)
            assertFalse(unavailable.isBlank())
            if (language != AppLanguage.English) assertFalse(unavailable == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.WriteAccessUnavailable.userMessage, AppLanguage.English))
            val source = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.SourceReadFailed.userMessage, language)
            assertFalse(source.isBlank())
            if (language != AppLanguage.English) assertFalse(source == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.SourceReadFailed.userMessage, AppLanguage.English))
            val folder = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.StoragePermissionMissing.userMessage, language)
            assertFalse(folder.isBlank())
            if (language != AppLanguage.English) assertFalse(folder == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.StoragePermissionMissing.userMessage, AppLanguage.English))
            val unsupported = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.UnsupportedFormat.userMessage, language)
            assertFalse(unsupported.isBlank())
            if (language != AppLanguage.English) assertFalse(unsupported == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.UnsupportedFormat.userMessage, AppLanguage.English))
            val synced = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.UnsupportedSyncedLyrics.userMessage, language)
            assertFalse(synced.isBlank())
            assertFalse(synced == unsupported)
            if (language != AppLanguage.English) assertFalse(synced == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.UnsupportedSyncedLyrics.userMessage, AppLanguage.English))
            val provider = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.ProviderRejectedWriteMode.userMessage, language)
            assertFalse(provider.isBlank())
            if (language != AppLanguage.English) assertFalse(provider == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.ProviderRejectedWriteMode.userMessage, AppLanguage.English))
            val write = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.OriginalOverwriteFailed.userMessage, language)
            assertFalse(write.isBlank())
            if (language != AppLanguage.English) assertFalse(write == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.OriginalOverwriteFailed.userMessage, AppLanguage.English))
            val beforeSave = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TempVerificationFailed.userMessage, language)
            assertFalse(beforeSave.isBlank())
            if (language != AppLanguage.English) assertFalse(beforeSave == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TempVerificationFailed.userMessage, AppLanguage.English))
            val afterSave = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.PersistedVerificationFailed.userMessage, language)
            assertFalse(afterSave.isBlank())
            assertFalse(afterSave == beforeSave)
            if (language != AppLanguage.English) assertFalse(afterSave == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.PersistedVerificationFailed.userMessage, AppLanguage.English))
            val preparation = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TempWriteFailed.userMessage, language)
            assertFalse(preparation.isBlank())
            if (language != AppLanguage.English) assertFalse(preparation == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TempWriteFailed.userMessage, AppLanguage.English))
            val metadata = localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TagCommitFailed.userMessage, language)
            assertFalse(metadata.isBlank())
            if (language != AppLanguage.English) assertFalse(metadata == localizedLyricsSaveError(EmbeddedLyricsWriteFailure.TagCommitFailed.userMessage, AppLanguage.English))
        }
        assertEquals("Nie udało się przywrócić utworu po nieudanym zapisie tekstu", localizedLyricsSaveError(EmbeddedLyricsWriteFailure.RollbackFailed.userMessage, AppLanguage.Polish))
        EmbeddedLyricsWriteFailure.entries.forEach { failure ->
            assertFalse(localizedLyricsSaveError(failure.userMessage, AppLanguage.English).endsWith('.'))
            AppLanguage.entries.filter { it != AppLanguage.English }.forEach { language ->
                assertFalse("$failure is untranslated for $language", localizedLyricsSaveError(failure.userMessage, language) == localizedLyricsSaveError(failure.userMessage, AppLanguage.English))
            }
        }
        assertEquals("Provider detail.", localizedLyricsSaveError("Provider detail.", AppLanguage.English))
    }
    @Test
    fun documentWriteCapabilityQueryFailureIsLocalized() {
        val status = "Unable to query document write capability."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Nie można sprawdzić, czy dokument obsługuje zapis", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
        assertEquals("Unable to query document write capability", localizedTagSaveStatus(status, 0, AppLanguage.English))
    }
    @Test
    fun unsupportedDocumentWriteIsLocalized() {
        val status = "The selected document provider does not support writing this file."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Wybrany dostawca dokumentów nie obsługuje zapisu tego pliku", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun missingDocumentWritePermissionIsLocalized() {
        val status = "The selected document has no persisted write permission."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Wybrany dokument nie ma trwałego uprawnienia do zapisu", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
        assertEquals("The selected document has no persisted write permission", localizedTagSaveStatus(status, 0, AppLanguage.English))
    }
    @Test
    fun tagDocumentProviderFallbackIsLocalized() {
        val status = "The selected document provider does not allow tag editing"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
            assertEquals("Provider detail", localizedTagSaveStatus("Provider detail", 0, language))
        }
        assertEquals("Wybrany dostawca dokumentów nie pozwala na edycję tagów", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun missingPersistentFolderAccessStatusIsLocalized() {
        val status = "The selected folder did not grant persistent write access"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Wybrany folder nie przyznał trwałego uprawnienia do zapisu", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun audiobookTagSaveFallbackIsLocalized() {
        val status = "Unable to save audiobook tags"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Nie można zapisać tagów audiobooka", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun genericTagSaveFailureStatusIsLocalized() {
        val status = "Unable to save tags"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Nie można zapisać tagów", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun noTagsSavedStatusIsLocalized() {
        val status = "No tags were saved"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Nie zapisano żadnych tagów", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun existingTagWriteRequestStatusIsLocalized() {
        val status = "A write-access request is already pending"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Prośba o uprawnienie do zapisu już oczekuje na odpowiedź", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun tagWriteRequestLaunchFailureIsLocalized() {
        val status = "Android could not open the write-access request"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Android nie mógł otworzyć prośby o uprawnienie do zapisu", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun declinedTagWriteAccessIsLocalized() {
        val status = "Write access was not granted"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
            assertFalse(message == localizedTagSaveStatus("Additional write access is needed to finish saving", 0, language))
        }
        assertEquals("Nie przyznano uprawnienia do zapisu", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun pendingTagWriteAccessStatusIsLocalized() {
        val status = "Additional write access is needed to finish saving"
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(status, 0, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == status)
        }
        assertEquals("Aby zakończyć zapisywanie, potrzebne jest dodatkowe uprawnienie do zapisu", localizedTagSaveStatus(status, 0, AppLanguage.Polish))
    }
    @Test
    fun partialTagSaveStatusIsLocalizedAndCountsIssues() {
        assertEquals("Saved with 1 issue", localizedTagSaveStatus("Saved with 1 issue(s)", 1, AppLanguage.English))
        assertEquals("Saved with 2 issues", localizedTagSaveStatus("Saved with 2 issue(s)", 2, AppLanguage.English))
        assertEquals("Zapisano z problemami: 2", localizedTagSaveStatus("Saved with 2 issue(s)", 2, AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            assertFalse(localizedTagSaveStatus("Saved with 2 issue(s)", 2, language).isBlank())
            assertEquals("Unexpected", localizedTagSaveStatus("Unexpected", 2, language))
        }
    }
    @Test
    fun invalidTrackSelectionIsLocalizedForAnyCollection() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.InvalidInput(TagEditValidationFailure.InvalidTrackSelection))
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Wybrane utwory nie są już prawidłowe dla tej kolekcji", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun oversizedTagTextIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.InvalidInput(TagEditValidationFailure.TextTooLong))
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Co najmniej jedna wartość tagu jest zbyt długa, aby bezpiecznie ją zapisać", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun oversizedArtworkErrorIsLocalizedAndDistinct() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.InvalidInput(TagEditValidationFailure.ArtworkTooLarge))
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            assertFalse(message == localizedTagSaveFailure(failure.copy(cause = TagEditFailureCause.UnsupportedArtworkFormat), language))
        }
        assertEquals("Wybrana okładka jest zbyt duża, aby bezpiecznie ją osadzić", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun invalidTagNumbersUseLocalizedValidationMessage() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.InvalidInput(TagEditValidationFailure.InvalidTrackNumber))
        AppLanguage.entries.forEach { language ->
            assertFalse(localizedTagSaveFailure(failure, language).isBlank())
            if (language != AppLanguage.English) assertFalse(localizedTagSaveFailure(failure, language) == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Numery utworu i płyty muszą mieścić się w zakresie od 1 do 9999", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun canonicalTagPreflightFailureIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "This song is not ready for safe tag editing.", TagEditFailureCause.CannotOpenOutput)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Ten utwór nie jest gotowy do bezpiecznej edycji tagów", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun canonicalTagOutputFailureIsLocalizedWithoutMaskingDetails() {
        val failure = TagEditFailureUi(1, "song", "Unable to save tags to the song file.", TagEditFailureCause.CannotOpenOutput)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
            assertEquals("Specific provider error", localizedTagSaveFailure(failure.copy(reason = "Specific provider error"), language))
        }
        assertEquals("Nie można zapisać tagów w pliku utworu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun canonicalTagSourceReadFailureIsLocalizedWithoutMaskingDetails() {
        val failure = TagEditFailureUi(1, "song", "Unable to read this song for tag editing.", TagEditFailureCause.CannotOpenInput)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
            assertEquals("Specific provider error", localizedTagSaveFailure(failure.copy(reason = "Specific provider error"), language))
        }
        assertEquals("Nie można odczytać tego utworu do edycji tagów", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun tagVerificationAfterSaveRemainsDistinct() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.PersistedVerificationFailed)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            assertFalse(message == localizedTagSaveFailure(failure.copy(cause = TagEditFailureCause.TempVerificationFailed), language))
        }
        assertEquals("Nie udało się zweryfikować zmienionych tagów po zapisaniu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun tagVerificationBeforeSaveIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.TempVerificationFailed)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Nie udało się zweryfikować zmienionych tagów przed zapisaniem", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun temporaryTagWriteFailureIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.TempWriteFailed)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
            assertFalse(message == localizedTagSaveFailure(failure.copy(cause = TagEditFailureCause.RollbackFailed), language))
        }
        assertEquals("Nie można bezpiecznie zapisać tagów", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun deniedAfterGrantRemainsDistinctFromPermissionRequest() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.PermissionDeniedAfterGrant)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            assertFalse(message == localizedTagSaveFailure(failure.copy(cause = TagEditFailureCause.PermissionRequired), language))
        }
        assertEquals("System nie zezwolił na aktualizację pliku po przyznaniu uprawnienia do zapisu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun tagWritePermissionFailureIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.PermissionRequired)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Ten plik wymaga dodatkowego uprawnienia do zapisu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun tagRollbackFailurePreservesTheWarningInEveryLanguage() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.RollbackFailed)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Nie udało się przywrócić utworu po nieudanym zapisie tagów", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun artworkReadFailureIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.CannotReadArtwork)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Nie można odczytać wybranej okładki", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun unsupportedArtworkFailureIsLocalized() {
        val failure = TagEditFailureUi(1, "song", "Original", TagEditFailureCause.UnsupportedArtworkFormat)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Nie można bezpiecznie osadzić okładki w tym formacie audio", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }
    @Test
    fun unsupportedTagFormatIsLocalizedWithoutDiscardingUnknownDetails() {
        val failure = TagEditFailureUi(1, "song", "Original diagnostic.", TagEditFailureCause.UnsupportedFormat)
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveFailure(failure, language)
            assertFalse(message.isBlank())
            if (language != AppLanguage.English) assertFalse(message == localizedTagSaveFailure(failure, AppLanguage.English))
        }
        assertEquals("Original diagnostic.", localizedTagSaveFailure(failure.copy(cause = null), AppLanguage.Polish))
        assertEquals("This audio format cannot be tagged safely", localizedTagSaveFailure(failure, AppLanguage.English))
    }
    @Test
    fun allTagValidationIssuesAreLocalized() {
        AppLanguage.entries.forEach { language ->
            TagValidationKind.entries.forEach { kind ->
                val text = localizedTagValidationError(TagValidationIssue(kind), language)
                assertFalse(text.isBlank())
                if (language != AppLanguage.English) assertFalse(text == kind.english)
            }
        }
    }
    @Test
    fun trackTitleValidationIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagValidationError(TagValidationIssue(TagValidationKind.TrackTitle, "song.mp3"), language)
            assertEquals(true, message.startsWith("song.mp3: "))
            if (language != AppLanguage.English) assertFalse(message.endsWith(TagValidationKind.TrackTitle.english))
        }
        assertEquals("song.mp3: Tytuł utworu nie może być pusty", localizedTagValidationError(TagValidationIssue(TagValidationKind.TrackTitle, "song.mp3"), AppLanguage.Polish))
    }
    @Test
    fun trackNumberValidationIsLocalizedAndRetainsFilename() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagValidationError(TagValidationIssue(TagValidationKind.TrackNumbers, "a.mp3"), language)
            assertFalse(message.isBlank())
            assertEquals(true, message.startsWith("a.mp3: "))
            if (language != AppLanguage.English) assertFalse(message.endsWith(TagValidationKind.TrackNumbers.english))
        }
    }
    @Test
    fun albumRequiredFieldsAreLocalized() {
        AppLanguage.entries.forEach { language ->
            listOf(TagValidationKind.AlbumTitle, TagValidationKind.AlbumArtist).forEach { kind ->
                val value = localizedTagValidationError(TagValidationIssue(kind), language)
                assertFalse(value.isBlank())
                if (language != AppLanguage.English) assertFalse(value == kind.english)
            }
        }
        assertEquals("Pole wykonawcy albumu nie może być puste", localizedTagValidationError(TagValidationIssue(TagValidationKind.AlbumArtist), AppLanguage.Polish))
    }
    @Test
    fun audiobookRequiredFieldsAreLocalized() {
        AppLanguage.entries.forEach { language ->
            listOf(TagValidationKind.BookTitle, TagValidationKind.Author, TagValidationKind.ChapterAuthor).forEach { kind ->
                val value = localizedTagValidationError(TagValidationIssue(kind), language)
                assertFalse(value.isBlank())
                if (language != AppLanguage.English) assertFalse(value == kind.english)
            }
        }
        assertEquals("Tytuł książki nie może być pusty", localizedTagValidationError(TagValidationIssue(TagValidationKind.BookTitle), AppLanguage.Polish))
    }
    @Test
    fun releaseYearValidationIsLocalizedWithoutChangingUnknownErrors() {
        val error = TagValidationIssue(TagValidationKind.ReleaseYear)
        AppLanguage.entries.forEach { language ->
            val translated = localizedTagValidationError(error, language)
            assertFalse(translated.isBlank())
            if (language != AppLanguage.English) assertFalse(translated == error.kind.english)
            assertEquals(true, localizedTagValidationError(TagValidationIssue(TagValidationKind.TrackTitle, "track.mp3"), language).startsWith("track.mp3: "))
        }
        assertEquals("Rok wydania musi mieścić się w zakresie od 1 do 9999", localizedTagValidationError(error, AppLanguage.Polish))
    }
    @Test
    fun additionalTagFeedbackIsTranslated() {
        AppLanguage.entries.forEach { language ->
            val copy = tagAdditionalIssuesCopy(language)
            assertFalse(copy.validation.isBlank())
            assertFalse(copy.save.isBlank())
            if (language != AppLanguage.English) assertFalse(copy.validation == "Additional validation issues")
        }
        assertEquals("Pozostałe błędy zapisu", tagAdditionalIssuesCopy(AppLanguage.Polish).save)
    }
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
        val language = AppLanguage.English
        val copies = listOf(
            rootUiCopy(language), libraryFoldersCopy(language), networkSourcesCopy(language),
            playlistAccessibilityCopy(language), settingsAccessibilityCopy(language),
            playerControlAccessibilityCopy(language), mediaOptionsAccessibilityCopy(language),
            lyricsAccessibilityCopy(language), navigationAccessibilityCopy(language),
            playbackNavigationAccessibilityCopy(language), shuffleAccessibilityCopy(language),
            viewModeAccessibilityCopy(language), sleepTimerCopy(language), commonUiCopy(language),
            playlistMainCopy(language), equalizerCopy(language), settingsCopy(language),
            libraryScanStatusCopy(language), playlistManagementCopy(language), crossfadeCopy(language),
            aboutUpdateCopy(language), updateActionCopy(language), artistInfoCopy(language),
            smartPlaylistOrderCopy(language), smartPlaylistInputCopy(language),
            smartPlaylistValueCopy(language), smartPlaylistOtherRuleCopy(language),
            smartPlaylistTextRuleCopy(language), searchCopy(language), nowPlayingBarStyleCopy(language),
            audiobookExtraCopy(language), audiobookSettingsCopy(language), upscalerCopy(language),
            audiobookUnavailableCopy(language), audiobookDescriptionCopy(language), audiobookCopy(language),
            smartPlaylistSettingsCopy(language), smartPlaylistActionCopy(language), smartPlaylistMatchCopy(language),
            smartPlaylistEditorHeadingCopy(language), smartPlaylistMissingCopy(language),
            smartPlaylistDeleteCopy(language), smartPlaylistEmptyCopy(language), homeCopy(language),
            tagAdditionalIssuesCopy(language), carMediaCopy(language),
        )
        copies.forEach { copy ->
            copy.javaClass.declaredFields.filter { it.type == String::class.java }.forEach { field ->
                field.isAccessible = true
                val value = field.get(copy) as String
                assertFalse("${copy.javaClass.simpleName}.${field.name} ends in a period", value.endsWith('.') && !value.endsWith("..."))
                val sentenceSeparators = Regex("(?<!\\.)\\.\\s+(?=[A-Z])").findAll(value).count()
                assertTrue("${copy.javaClass.simpleName}.${field.name} has too many sentence separators", sentenceSeparators <= 1)
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
