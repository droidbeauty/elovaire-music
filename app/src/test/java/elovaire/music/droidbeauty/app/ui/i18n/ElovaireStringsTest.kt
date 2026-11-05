package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import elovaire.music.droidbeauty.app.ui.screens.formatCrossfadeSeconds
import elovaire.music.droidbeauty.app.ui.screens.formatAudiobookSpeed
import elovaire.music.droidbeauty.app.ui.screens.homeLayoutEditorCopy
import elovaire.music.droidbeauty.app.ui.screens.homeLayoutTitle
import elovaire.music.droidbeauty.app.ui.screens.homeLayoutSubtitle
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutDescription
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutTitle
import elovaire.music.droidbeauty.app.ui.screens.localizedAboutLinkLabel
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_PERMISSION_DENIED_MESSAGE
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_POST_GRANT_FAILURE_MESSAGE
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationIssue
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationKind
import elovaire.music.droidbeauty.app.ui.screens.tags.TagEditFailureUi
import elovaire.music.droidbeauty.app.ui.screens.tags.tagEditorCopy
import elovaire.music.droidbeauty.app.ui.screens.tags.audiobookTagEditorCopy
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
    fun crossfadeDurationFollowsSelectedLanguageNumberFormat() {
        assertEquals("2.5", formatCrossfadeSeconds(2.5f, AppLanguage.English))
        assertEquals("2,5", formatCrossfadeSeconds(2.5f, AppLanguage.Polish))
        assertEquals("3", formatCrossfadeSeconds(3f, AppLanguage.Polish))
        assertEquals("2,125", formatCrossfadeSeconds(2.125f, AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            val formatted = formatCrossfadeSeconds(2.5f, language)
            val parsed = requireNotNull(java.text.NumberFormat.getNumberInstance(language.appLocale()).parse(formatted))
            assertEquals("Incorrect duration for $language", 2.5, parsed.toDouble(), 0.001)
        }
    }

    @Test
    fun audiobookSpeedFollowsSelectedLanguageNumberFormat() {
        assertEquals("1.0", formatAudiobookSpeed(1f, AppLanguage.English))
        assertEquals("0.75", formatAudiobookSpeed(0.75f, AppLanguage.English))
        assertEquals("1,0", formatAudiobookSpeed(1f, AppLanguage.Polish))
        assertEquals("1,25", formatAudiobookSpeed(1.25f, AppLanguage.Polish))
        assertEquals("2", formatAudiobookSpeed(2f, AppLanguage.English, showDecimalForWhole = false))
        AppLanguage.entries.forEach { language ->
            val parsed = requireNotNull(java.text.NumberFormat.getNumberInstance(language.appLocale()).parse(formatAudiobookSpeed(1.75f, language)))
            assertEquals("Incorrect speed for $language", 1.75, parsed.toDouble(), 0.001)
            val end = requireNotNull(java.text.NumberFormat.getNumberInstance(language.appLocale()).parse(formatAudiobookSpeed(2f, language, showDecimalForWhole = false)))
            assertEquals("Incorrect speed endpoint for $language", 2.0, end.toDouble(), 0.001)
        }
    }

    @Test
    fun selectedCountsAgreeWithTheirNounAndNumber() {
        assertEquals("1 album wybrany", localizedSelectedCount(1, "album", AppLanguage.Polish))
        assertEquals("2 albumy wybrane", localizedSelectedCount(2, "album", AppLanguage.Polish))
        assertEquals("5 albumów wybranych", localizedSelectedCount(5, "album", AppLanguage.Polish))
        assertEquals("21 utworów wybranych", localizedSelectedCount(21, "song", AppLanguage.Polish))
        assertEquals("22 utwory wybrane", localizedSelectedCount(22, "song", AppLanguage.Polish))
        assertEquals("1 canción seleccionada", localizedSelectedCount(1, "song", AppLanguage.Spanish))
        assertEquals("2 álbumes seleccionados", localizedSelectedCount(2, "album", AppLanguage.Spanish))
        assertEquals("1 cançó seleccionada", localizedSelectedCount(1, "song", AppLanguage.Catalan))
        assertEquals("1 album sélectionné", localizedSelectedCount(1, "album", AppLanguage.French))
        assertEquals("1 brano selezionato", localizedSelectedCount(1, "song", AppLanguage.Italian))
        assertEquals("1 album valt", localizedSelectedCount(1, "album", AppLanguage.Swedish))
        assertEquals("1 skladba vybraná", localizedSelectedCount(1, "song", AppLanguage.Slovak))
        assertEquals("5 albumov vybraných", localizedSelectedCount(5, "album", AppLanguage.Slovak))
        assertEquals("Выбрано: 1 песня", localizedSelectedCount(1, "song", AppLanguage.Russian))
        assertEquals("Vybráno: 5 alb", localizedSelectedCount(5, "album", AppLanguage.Czech))
        assertEquals("21 alb", localizedCountLabel(21, "album", AppLanguage.Czech))
        assertEquals("चयनित: 1 गाना", localizedSelectedCount(1, "song", AppLanguage.Hindi))
        assertEquals("Selecție: 1 album", localizedSelectedCount(1, "album", AppLanguage.Romanian))
        assertEquals("Selecție: 5 melodii", localizedSelectedCount(5, "song", AppLanguage.Romanian))
        assertEquals("Избрано: 1 песен", localizedSelectedCount(1, "song", AppLanguage.Bulgarian))
        assertEquals("1 τραγούδι επιλεγμένο", localizedSelectedCount(1, "song", AppLanguage.Greek))
        assertEquals("Valittu: 2 albumia", localizedSelectedCount(2, "album", AppLanguage.Finnish))
        assertEquals("2 albums selected", localizedSelectedCount(2, "album", AppLanguage.English))
        AppLanguage.entries.forEach { language ->
            listOf(0, 1, 2, 5, 11, 21, 22).forEach { count ->
                listOf("song", "album").forEach { noun ->
                    val label = localizedSelectedCount(count, noun, language)
                    assertTrue("Missing count for $language", label.contains(count.toString()))
                    assertEquals(label.trim(), label)
                }
            }
        }
    }

    @Test
    fun dynamicSettingsAndRepeatDescriptionsAreTranslatedForEveryLanguage() {
        assertEquals("Audiobook settings", audiobookSettingsCopy(AppLanguage.English).title)
        assertEquals("Songs added to your library recently", smartPlaylistDescription(BuiltInSmartPlaylistType.RecentlyAdded, AppLanguage.English))
        assertEquals("Utwory niedawno dodane do Twojej biblioteki", smartPlaylistDescription(BuiltInSmartPlaylistType.RecentlyAdded, AppLanguage.Polish))
        assertEquals("Powtórz utwór", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Polish))
        assertEquals("Opakovat skladbu", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Czech))
        assertEquals("Повторити трек", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Ukrainian))
        assertEquals("Ponovi pjesmu", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Croatian))
        assertEquals("Repetir canción", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Spanish))
        assertEquals("Repetir faixa", repeatModeLabel(PlaybackRepeatMode.One, AppLanguage.Portuguese))
        AppLanguage.entries.forEach { language ->
            BuiltInSmartPlaylistType.entries.forEach { type ->
                val translated = smartPlaylistDescription(type, language)
                assertTrue("Missing $type description for $language", translated.isNotBlank())
                if (language == AppLanguage.English) {
                    assertFalse("Final period in $type description", translated.endsWith('.') && !translated.endsWith("..."))
                } else {
                    assertFalse("English $type description for $language", translated == smartPlaylistDescription(type, AppLanguage.English))
                }
            }
            listOf(false, true).forEach { compact ->
                val translated = nowPlayingStyleDescription(language, compact)
                assertTrue("Missing player style description for $language", translated.isNotBlank())
                if (language == AppLanguage.English) {
                    assertFalse("Final period in player style description", translated.endsWith('.') && !translated.endsWith("..."))
                } else {
                    assertFalse("English player style description for $language", translated == nowPlayingStyleDescription(AppLanguage.English, compact))
                }
            }
            PlaybackRepeatMode.entries.forEach { mode ->
                val translated = repeatModeLabel(mode, language)
                assertTrue("Missing repeat mode for $language", translated.isNotBlank())
                if (language != AppLanguage.English && mode != PlaybackRepeatMode.Off) {
                    assertFalse("English repeat mode for $language", translated == repeatModeLabel(mode, AppLanguage.English))
                }
            }
        }
    }

    @Test
    fun albanianPrivacyCopyIncludesThePurposesOfOnlineRequests() {
        val onlineServices = privacyPolicyCopy(AppLanguage.Albanian).sections[2].body
        listOf("LRCLIB", "YouTube Music", "Google Books", "tekste këngësh", "imazhe artistësh", "përshkrime librash audio")
            .forEach { assertTrue("Missing $it", onlineServices.contains(it)) }
        assertFalse(onlineServices.endsWith('.'))
    }

    @Test
    fun onlinePrivacyTranslationsIdentifyTheTypesOfContentFetched() {
        val terms = mapOf(
            AppLanguage.Bulgarian to listOf("текстове на песни", "изображения на изпълнители", "описания на аудиокниги"),
            AppLanguage.Catalan to listOf("lletres de cançons", "imatges d’artistes", "descripcions d’audiollibres"),
            AppLanguage.Finnish to listOf("sanoitusten", "esittäjäkuvien", "äänikirjakuvausten"),
            AppLanguage.Hindi to listOf("गीत के बोल", "कलाकार की तस्वीर", "ऑडियोबुक का विवरण"),
            AppLanguage.Romanian to listOf("versuri", "imagini ale artiștilor", "descrieri ale audiocărților"),
            AppLanguage.Slovenian to listOf("besedil skladb", "slik izvajalcev", "opisov zvočnih knjig"),
            AppLanguage.Slovak to listOf("texty", "obrázky interpretov", "opisy audiokníh", "zabránite týmto požiadavkám"),
        )
        terms.forEach { (language, expected) ->
            val body = privacyPolicyCopy(language).sections[2].body
            expected.forEach { assertTrue("Missing $it for $language", body.contains(it)) }
        }
    }

    @Test
    fun privacySettingsSubtitleIdentifiesTheAppInEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            assertTrue("Missing app name for $language", privacyPolicySettingsSubtitle(language).contains("Elovaire"))
        }
        assertTrue(privacyPolicySettingsSubtitle(AppLanguage.Hungarian).startsWith("Hogyan kezeli az Elovaire"))
    }

    @Test
    fun normalizationSubtitlesDescribeBothSongsAndFileMetadata() {
        val expected = mapOf(
            AppLanguage.Bulgarian to listOf("песните", "метаданните на файла"),
            AppLanguage.Catalan to listOf("cançons", "metadades del fitxer"),
            AppLanguage.Romanian to listOf("melodii", "metadatele fișierului"),
            AppLanguage.Slovenian to listOf("skladbami", "metapodatki datoteke"),
            AppLanguage.Bosnian to listOf("pjesama", "metapodaci datoteke"),
        )
        expected.forEach { (language, terms) ->
            terms.forEach { assertTrue("Missing $it for $language", volumeNormalizationSubtitle(language).contains(it)) }
        }
    }

    @Test
    fun slovakPrivacyCopyDescribesNotificationAndUpdatePermissions() {
        val permissions = privacyPolicyCopy(AppLanguage.Slovak).sections[4].body
        assertTrue(permissions.contains("povolenie zobrazovať upozornenia na prehrávanie"))
        assertTrue(permissions.contains("inštalovať aktualizácie z GitHubu"))
    }

    @Test
    fun artistTopTracksSubtitleUsesNaturalWordOrderAndAgreement() {
        assertEquals("1 utwór, do którego najczęściej wracasz", artistTopTracksSubtitle(1, AppLanguage.Polish))
        assertEquals("2 utwory, do których najczęściej wracasz", artistTopTracksSubtitle(2, AppLanguage.Polish))
        assertEquals("1 Track, zu dem du am häufigsten zurückkehrst", artistTopTracksSubtitle(1, AppLanguage.German))
        assertEquals("2 Tracks, zu denen du am häufigsten zurückkehrst", artistTopTracksSubtitle(2, AppLanguage.German))
        assertEquals("よく聴き返す 2 曲", artistTopTracksSubtitle(2, AppLanguage.Japanese))
        assertEquals("가장 자주 다시 듣는 2 곡", artistTopTracksSubtitle(2, AppLanguage.Korean))
        assertEquals("最常重听的 2 首歌曲", artistTopTracksSubtitle(2, AppLanguage.ChineseSimplified))
        assertEquals("1 pista a la que más vuelves", artistTopTracksSubtitle(1, AppLanguage.Spanish))
        assertEquals("2 pistas a las que más vuelves", artistTopTracksSubtitle(2, AppLanguage.Spanish))
        assertEquals("21 трек, к которому вы возвращаетесь чаще всего", artistTopTracksSubtitle(21, AppLanguage.Russian))
        assertEquals("11 треков, к которым вы возвращаетесь чаще всего", artistTopTracksSubtitle(11, AppLanguage.Russian))
        assertEquals("101 skladba, h kateri se najpogosteje vračate", artistTopTracksSubtitle(101, AppLanguage.Slovenian))
        assertEquals("1 kappale, johon palaat useimmin", artistTopTracksSubtitle(1, AppLanguage.Finnish))
        assertEquals("2 kappaletta, joihin palaat useimmin", artistTopTracksSubtitle(2, AppLanguage.Finnish))
        assertEquals("1 melodie pe care o asculți cel mai des", artistTopTracksSubtitle(1, AppLanguage.Romanian))
        assertEquals("2 melodii pe care le asculți cel mai des", artistTopTracksSubtitle(2, AppLanguage.Romanian))
        assertEquals("1 szám, amelyhez a leggyakrabban tér vissza", artistTopTracksSubtitle(1, AppLanguage.Hungarian))
        assertEquals("2 szám, amelyekhez a leggyakrabban tér vissza", artistTopTracksSubtitle(2, AppLanguage.Hungarian))
        assertEquals("1 ट्रैक जिसे आप सबसे ज़्यादा सुनते हैं", artistTopTracksSubtitle(1, AppLanguage.Hindi))
        assertEquals("2 ट्रैक जिन्हें आप सबसे ज़्यादा सुनते हैं", artistTopTracksSubtitle(2, AppLanguage.Hindi))
        AppLanguage.entries.forEach { language ->
            listOf(0, 1, 2, 5, 11, 21, 101).forEach { count ->
                val subtitle = artistTopTracksSubtitle(count, language)
                assertTrue("Missing count for $language", subtitle.contains(count.toString()))
                assertTrue("Missing track label for $language", subtitle.contains(localizedCountLabel(count, "track", language)))
                assertFalse("Unexpected outer whitespace for $language", subtitle != subtitle.trim())
                if (language != AppLanguage.English) {
                    assertFalse("English artist subtitle for $language", subtitle == artistTopTracksSubtitle(count, AppLanguage.English))
                } else {
                    assertFalse("Final period in artist subtitle", subtitle.endsWith('.'))
                }
            }
        }
    }

    @Test
    fun standaloneLabelsHaveLocalizedCopyAndValidEnglishPunctuation() {
        val labels: Map<String, (AppLanguage) -> String> = mapOf(
            "edit lyrics" to ::editLyricsAccessibilityLabel,
            "artwork placeholder" to ::artworkPlaceholderLabel,
            "close queue" to ::closeQueueAccessibilityLabel,
            "show song results" to ::showAllSongResultsLabel,
            "playing from" to ::playingFromPrefix,
            "all songs" to ::localizedAllSongsSource,
            "search results" to ::localizedSearchResultsSource,
            "privacy subtitle" to ::privacyPolicySettingsSubtitle,
            "audiobook category" to ::audiobookPlaybackCategory,
            "remove smart rule" to ::smartPlaylistRemoveRuleLabel,
            "edit smart rules" to ::smartPlaylistEditRulesLabel,
            "upscaler reset" to ::upscalerSetToDefault,
            "bundled licenses" to ::bundledLicensesLabel,
            "close artist info" to ::closeArtistInfoLabel,
            "external audio" to ::externalAudioLabel,
            "recent additions empty" to ::recentAdditionsEmptyMessage,
            "volume normalization" to ::volumeNormalizationSubtitle,
            "album of day" to ::albumOfDayTitle,
        )
        labels.forEach { (name, label) ->
            val english = label(AppLanguage.English)
            assertTrue("Empty English $name", english.isNotBlank())
            assertFalse("Unexpected final period in $name", english.endsWith('.') && !english.endsWith("..."))
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                val translated = label(language)
                assertTrue("Empty $name for $language", translated.isNotBlank())
                assertFalse("English $name for $language", translated == english)
            }
        }
    }

    @Test
    fun bundledChangelogEntriesAreLocalizedWithoutChangingUnknownReleaseNotes() {
        val entries = listOf(
            "UI tweaks in NAS configuration, tag editing system and settings layout",
            "Added support for 11 new languages - Albanian, Bosnian, Bulgarian, Catalan, Finnish, Indonesian, Romanian, Serbian, Slovenian, Punjabi and Urdu",
            "General bug fixes",
            "UI tweaks on the initial setup splash screen",
        )
        AppLanguage.entries.forEach { language ->
            entries.forEach { entry ->
                val translated = localizedChangelogItem(entry, language)
                assertTrue(translated.isNotBlank())
                if (language == AppLanguage.English) assertEquals(entry, translated) else assertFalse(translated == entry)
            }
            assertEquals("Unrecognized release note", localizedChangelogItem("Unrecognized release note", language))
            assertTrue(localizedChangelogItem(entries[1], language).contains("11") || language == AppLanguage.Bengali)
            if (language != AppLanguage.English) {
                val separator = if (language == AppLanguage.ChineseSimplified || language == AppLanguage.Japanese) "、" else ", "
                val names = localizedChangelogItem(entries[1], language).substringAfter(": ").split(separator)
                assertEquals("Missing language names for $language", 11, names.size)
                assertTrue(names.all(String::isNotBlank))
                assertEquals("Duplicate language names for $language", 11, names.distinct().size)
                if (language in setOf(
                        AppLanguage.Bengali, AppLanguage.Bulgarian, AppLanguage.ChineseSimplified,
                        AppLanguage.Greek, AppLanguage.Hindi, AppLanguage.Japanese, AppLanguage.Korean,
                        AppLanguage.Macedonian, AppLanguage.Punjabi, AppLanguage.Russian, AppLanguage.Serbian,
                        AppLanguage.Thai, AppLanguage.Ukrainian, AppLanguage.Urdu,
                    )
                ) {
                    names.forEach { name ->
                        assertTrue("Untranslated language name for $language: $name", name.any { it.code > 127 })
                    }
                }
            }
        }
        assertEquals("Ogólne poprawki błędów", localizedChangelogItem(entries[2], AppLanguage.Polish))
        assertEquals("Poprawki wyglądu ekranu konfiguracji początkowej", localizedChangelogItem(entries[3], AppLanguage.Polish))
        assertEquals("初期設定画面のデザインを調整", localizedChangelogItem(entries[3], AppLanguage.Japanese))
        assertTrue(localizedChangelogItem(entries[1], AppLanguage.Polish).contains("albański", ignoreCase = true))
    }

    @Test
    fun aboutDescriptionsAreLocalizedWithoutChangingUnknownEntries() {
        val fallback = "English fallback description"
        AppLanguage.entries.forEach { language ->
            listOf("Droid Beauty", "Elovaire", "Resources").forEach { title ->
                val localized = localizedAboutDescription(title, fallback, language)
                assertTrue(localized.isNotBlank())
                if (language != AppLanguage.English) assertFalse(localized == fallback)
            }
            assertEquals(fallback, localizedAboutDescription("Unrecognized resource", fallback, language))
            if (language != AppLanguage.English) {
                val resources = localizedAboutDescription("Resources", fallback, language)
                assertTrue(bundledLicensesLabel(language).isNotBlank())
                assertFalse(bundledLicensesLabel(language) == bundledLicensesLabel(AppLanguage.English))
                listOf("Android/Kotlin", "Apache-2.0", "Lucide", "ISC/MIT", "Geist", "OFL-1.1", "jaudiotagger", "LGPL").forEach {
                    assertTrue("Missing $it for $language", it in resources)
                }
                assertTrue(resources.contains(bundledLicensesLabel(language)))
            }
            assertTrue(localizedAboutTitle("Resources", language).isNotBlank())
            assertTrue(localizedAboutLinkLabel("Website", language).isNotBlank())
            assertTrue(localizedAboutLinkLabel("Play Store", language).isNotBlank())
            assertTrue(localizedAboutTitle("Lucide icons", language).contains("Lucide"))
            assertTrue(localizedAboutTitle("Geist font", language).contains("Geist"))
            if (language != AppLanguage.English) assertFalse(localizedAboutTitle("Lucide icons", language) == "Lucide icons")
            assertEquals("jaudiotagger", localizedAboutTitle("jaudiotagger", language))
            if (language != AppLanguage.English) assertFalse(localizedAboutTitle("Resources", language) == "Resources")
        }
        assertEquals("Dopracowane widżety i motywy, które upiększą ekran główny Androida. Jestem również twórcą odtwarzacza muzyki, z którego teraz korzystasz", localizedAboutDescription("Droid Beauty", fallback, AppLanguage.Polish))
    }

    @Test
    fun commonActionsAreNotLeftInEnglishForNonLatinScripts() {
        val languages = listOf(
            AppLanguage.Bengali, AppLanguage.Bulgarian, AppLanguage.ChineseSimplified,
            AppLanguage.Greek, AppLanguage.Hindi, AppLanguage.Japanese, AppLanguage.Korean,
            AppLanguage.Macedonian, AppLanguage.Punjabi, AppLanguage.Russian, AppLanguage.Serbian,
            AppLanguage.Thai, AppLanguage.Ukrainian, AppLanguage.Urdu,
        )
        languages.forEach { language ->
            UiPhrase.entries.forEach { phrase ->
                val english = uiPhrase(AppLanguage.English, phrase)
                assertFalse("$phrase remains English for $language", uiPhrase(language, phrase) == english)
            }
        }
    }

    @Test
    fun homeLayoutEntryIsTranslatedInEverySupportedLanguage() {
        AppLanguage.entries.forEach { language ->
            val title = homeLayoutTitle(language)
            val subtitle = homeLayoutSubtitle(language)
            assertTrue(title.isNotBlank())
            assertTrue(subtitle.isNotBlank())
            if (language != AppLanguage.English) {
                assertFalse(title == homeLayoutTitle(AppLanguage.English))
                assertFalse(subtitle == homeLayoutSubtitle(AppLanguage.English))
            }
        }
    }

    @Test
    fun concurrentTagEditErrorIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus("The song is already being edited.", 1, language)
            assertTrue(message.isNotBlank())
            if (language != AppLanguage.English) assertFalse(message == "The song is already being edited")
        }
        assertEquals("Utwór jest już edytowany", localizedTagSaveStatus("The song is already being edited.", 1, AppLanguage.Polish))
    }

    @Test
    fun unavailableTagSourceLocationIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus("The file path is unavailable.", 1, language)
            assertEquals(message, localizedTagSaveStatus("The song directory is unavailable.", 1, language))
            assertTrue(message.isNotBlank())
            if (language != AppLanguage.English) assertFalse(message == "The song file location is unavailable")
        }
        assertEquals("Lokalizacja pliku utworu jest niedostępna", localizedTagSaveStatus("The file path is unavailable.", 1, AppLanguage.Polish))
    }

    @Test
    fun unwritableTagSourceErrorIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus("The song file is not writable.", 1, language)
            assertTrue(message.isNotBlank())
            if (language != AppLanguage.English) assertFalse(message == "The song file is not writable")
        }
        assertEquals("Nie można zapisać zmian w pliku utworu", localizedTagSaveStatus("The song file is not writable.", 1, AppLanguage.Polish))
        val failure = TagEditFailureUi(1L, "song.mp3", "The song file is not writable.", TagEditFailureCause.CannotOpenOutput)
        assertEquals("Nie można zapisać zmian w pliku utworu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }

    @Test
    fun emptyTagSourceErrorIsLocalized() {
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus("The song file is empty.", 1, language)
            assertTrue(message.isNotBlank())
            if (language != AppLanguage.English) assertFalse(message == "The song file is empty")
        }
        assertEquals("Plik utworu jest pusty", localizedTagSaveStatus("The song file is empty.", 1, AppLanguage.Polish))
        val failure = TagEditFailureUi(1L, "song.mp3", "The song file is empty.", TagEditFailureCause.CannotOpenInput)
        assertEquals("Plik utworu jest pusty", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }

    @Test
    fun insufficientTagStorageIsLocalizedForBothStorageChecks() {
        val temporary = "There is not enough temporary storage to edit this song safely."
        val replacement = "There is not enough storage to replace this song safely."
        AppLanguage.entries.forEach { language ->
            val message = localizedTagSaveStatus(temporary, 1, language)
            assertEquals(message, localizedTagSaveStatus(replacement, 1, language))
            assertTrue(message.isNotBlank())
            if (language != AppLanguage.English) assertFalse(message == "There is not enough free storage to edit this song safely")
        }
        assertEquals("Brakuje wolnego miejsca do bezpiecznej edycji utworu", localizedTagSaveStatus(temporary, 1, AppLanguage.Polish))
        val failure = TagEditFailureUi(1L, "song.mp3", temporary, TagEditFailureCause.CannotOpenInput)
        assertEquals("Brakuje wolnego miejsca do bezpiecznej edycji utworu", localizedTagSaveFailure(failure, AppLanguage.Polish))
    }

    @Test
    fun greekSpatialEffectLabelsAreLocalized() {
        assertEquals("Στερεοφωνικό εύρος", SpaciousnessMode.StereoWidth.displayLabel(AppLanguage.Greek))
        assertEquals("Διασταύρωση καναλιών", SpaciousnessMode.CrossfeedDepth.displayLabel(AppLanguage.Greek))
        assertEquals("Λιγότερη αντήχηση", uiPhrase(AppLanguage.Greek, UiPhrase.Dry))
        assertEquals("Περισσότερη αντήχηση", uiPhrase(AppLanguage.Greek, UiPhrase.Wet))
        assertEquals("Meno riverbero", uiPhrase(AppLanguage.Italian, UiPhrase.Dry))
        assertEquals("Più riverbero", uiPhrase(AppLanguage.Italian, UiPhrase.Wet))
        assertEquals("Διαδικτυακοί στίχοι", settingsCopy(AppLanguage.Greek).onlineLyricsTitle)
        assertEquals("Διαχείριση λιστών αναπαραγωγής", settingsCopy(AppLanguage.Greek).managePlaylistsTitle)
        assertEquals("Dozvuk", uiPhrase(AppLanguage.Slovak, UiPhrase.Reverb))
        assertEquals("Reverberim", uiPhrase(AppLanguage.Albanian, UiPhrase.Reverb))
        assertEquals("Reverberasi", uiPhrase(AppLanguage.Indonesian, UiPhrase.Reverb))
    }

    @Test
    fun audiobookPartCountsUseLocalizedNumberForms() {
        assertEquals("1 part", audiobookPartsCount(1, AppLanguage.English))
        assertEquals("1 część", audiobookPartsCount(1, AppLanguage.Polish))
        assertEquals("5 części", audiobookPartsCount(5, AppLanguage.Polish))
        assertEquals("21 часть", audiobookPartsCount(21, AppLanguage.Russian))
        assertEquals("12 частей", audiobookPartsCount(12, AppLanguage.Russian))
        assertEquals("2টি অংশ", audiobookPartsCount(2, AppLanguage.Bengali))
        assertEquals("101 del", audiobookPartsCount(101, AppLanguage.Slovenian))
        assertEquals("102 dela", audiobookPartsCount(102, AppLanguage.Slovenian))
        assertEquals("103 deli", audiobookPartsCount(103, AppLanguage.Slovenian))
        assertEquals("105 delov", audiobookPartsCount(105, AppLanguage.Slovenian))
        assertEquals("20 de părți", audiobookPartsCount(20, AppLanguage.Romanian))
        assertEquals("101 părți", audiobookPartsCount(101, AppLanguage.Romanian))
        AppLanguage.entries.forEach { assertTrue(audiobookPartsCount(2, it).isNotBlank()) }
    }

    @Test
    fun availableReleaseCountsUseCorrectNumberForms() {
        assertEquals("1 available release", availableReleasesLabel(1, AppLanguage.English))
        assertEquals("5 dostępnych wydań", availableReleasesLabel(5, AppLanguage.Polish))
        assertEquals("22 dostępne wydania", availableReleasesLabel(22, AppLanguage.Polish))
        assertEquals("21 dostępnych wydań", availableReleasesLabel(21, AppLanguage.Polish))
        assertEquals("102 izdaji na voljo", availableReleasesLabel(102, AppLanguage.Slovenian))
        assertEquals("103 izdaje na voljo", availableReleasesLabel(103, AppLanguage.Slovenian))
        assertEquals("21 доступный релиз", availableReleasesLabel(21, AppLanguage.Russian))
        assertEquals("12 доступных релизов", availableReleasesLabel(12, AppLanguage.Russian))
        assertEquals("1 Veröffentlichung verfügbar", availableReleasesLabel(1, AppLanguage.German))
        assertEquals("21 galimas leidinys", availableReleasesLabel(21, AppLanguage.Lithuanian))
        assertEquals("11 galimų leidinių", availableReleasesLabel(11, AppLanguage.Lithuanian))
        assertEquals("20 de lansări disponibile", availableReleasesLabel(20, AppLanguage.Romanian))
        assertEquals("1টি উপলভ্য সংস্করণ", availableReleasesLabel(1, AppLanguage.Bengali))
        assertEquals("1 julkaisu saatavilla", availableReleasesLabel(1, AppLanguage.Finnish))
        assertEquals("2件のリリース", availableReleasesLabel(2, AppLanguage.Japanese))
        assertEquals("มี 2 รายการเผยแพร่", availableReleasesLabel(2, AppLanguage.Thai))
        val polishForms = mapOf(
            0 to "dostępnych wydań", 1 to "dostępne wydanie", 2 to "dostępne wydania",
            4 to "dostępne wydania", 5 to "dostępnych wydań", 12 to "dostępnych wydań",
            21 to "dostępnych wydań", 22 to "dostępne wydania", 112 to "dostępnych wydań",
        )
        polishForms.forEach { (count, suffix) ->
            assertEquals("$count $suffix", availableReleasesLabel(count, AppLanguage.Polish))
        }
        AppLanguage.entries.forEach { language ->
            polishForms.keys.forEach { count ->
                assertTrue("Missing release count for $language and $count", availableReleasesLabel(count, language).contains(count.toString()))
            }
        }
    }

    @Test
    fun searchEmptyResultsPutTheQueryInANaturalPosition() {
        assertEquals("You might enjoy listening to these again", searchCopy(AppLanguage.English).suggestedAlbumsSubtitle)
        assertEquals("Nie znaleziono w bibliotece wyników dla \"jazz\"", searchCopy(AppLanguage.Polish).noResultsMessage("jazz"))
        assertEquals("当前离线媒体库中没有以下搜索的匹配结果： \"jazz\"", searchCopy(AppLanguage.ChineseSimplified).noResultsMessage("jazz"))
        assertEquals("현재 오프라인 라이브러리에 다음 검색 결과가 없습니다: \"jazz\"", searchCopy(AppLanguage.Korean).noResultsMessage("jazz"))
        assertEquals("In der Offline-Bibliothek wurden keine Ergebnisse gefunden für \"jazz\"", searchCopy(AppLanguage.German).noResultsMessage("jazz"))
        assertEquals("Tiada hasil dalam pustaka luar talian untuk \"jazz\"", searchCopy(AppLanguage.Malay).noResultsMessage("jazz"))
        assertEquals("অফলাইন লাইব্রেরিতে এই অনুসন্ধানের কোনো ফল নেই: \"jazz\"", searchCopy(AppLanguage.Bengali).noResultsMessage("jazz"))
        assertEquals("آف لائن لائبریری میں اس تلاش کا کوئی نتیجہ نہیں: \"jazz\"", searchCopy(AppLanguage.Urdu).noResultsMessage("jazz"))
        assertEquals("V offline knihovně zatím nejsou výsledky pro \"jazz\"", searchCopy(AppLanguage.Czech).noResultsMessage("jazz"))
        assertEquals("V offline knižnici zatiaľ nie sú výsledky pre \"jazz\"", searchCopy(AppLanguage.Slovak).noResultsMessage("jazz"))
        assertEquals("В офлайн-библиотеке пока нет результатов для \"jazz\"", searchCopy(AppLanguage.Russian).noResultsMessage("jazz"))
        assertEquals("В офлайн-бібліотеці поки немає результатів для \"jazz\"", searchCopy(AppLanguage.Ukrainian).noResultsMessage("jazz"))
        assertEquals("オフラインライブラリに次の検索結果はありません： \"jazz\"", searchCopy(AppLanguage.Japanese).noResultsMessage("jazz"))
        assertEquals("No hay resultados en la biblioteca sin conexión para \"jazz\"", searchCopy(AppLanguage.Spanish).noResultsMessage("jazz"))
        assertEquals("Não foram encontrados resultados na biblioteca offline para \"jazz\"", searchCopy(AppLanguage.Portuguese).noResultsMessage("jazz"))
        assertEquals("लाइब्रेरी में इस खोज का कोई नतीजा नहीं मिला: \"jazz\"", searchCopy(AppLanguage.Hindi).noResultsMessage("jazz"))
        assertEquals("ਲਾਇਬ੍ਰੇਰੀ ਵਿੱਚ ਇਸ ਖੋਜ ਲਈ ਕੋਈ ਨਤੀਜਾ ਨਹੀਂ: \"jazz\"", searchCopy(AppLanguage.Punjabi).noResultsMessage("jazz"))
        AppLanguage.entries.forEach { language ->
            val result = searchCopy(language).noResultsMessage("query-marker")
            assertEquals("Query repeated or missing for $language", 1, Regex("query-marker").findAll(result).count())
            assertEquals("Unexpected outer whitespace for $language", result.trim(), result)
        }
    }

    @Test
    fun majorUiCopyDoesNotLeaveEnglishSentencesInOtherLanguages() {
        fun copies(language: AppLanguage): List<Any> = listOf(
            rootUiCopy(language), homeCopy(language), searchCopy(language), settingsCopy(language),
            upscalerCopy(language), aboutUpdateCopy(language), updateActionCopy(language),
            libraryFoldersCopy(language), networkSourcesCopy(language), commonUiCopy(language),
            audiobookCopy(language), audiobookUnavailableCopy(language), audiobookDescriptionCopy(language),
            audiobookSettingsCopy(language), audiobookExtraCopy(language), smartPlaylistSettingsCopy(language),
            playlistManagementCopy(language), crossfadeCopy(language), equalizerCopy(language),
            artistInfoCopy(language), nowPlayingBarStyleCopy(language), carMediaCopy(language),
            playlistAccessibilityCopy(language), settingsAccessibilityCopy(language),
            playerControlAccessibilityCopy(language), mediaOptionsAccessibilityCopy(language),
            lyricsAccessibilityCopy(language), navigationAccessibilityCopy(language),
            playbackNavigationAccessibilityCopy(language), shuffleAccessibilityCopy(language),
            viewModeAccessibilityCopy(language), sleepTimerCopy(language), tagAdditionalIssuesCopy(language),
            playlistMainCopy(language), smartPlaylistOrderCopy(language), smartPlaylistInputCopy(language),
            smartPlaylistValueCopy(language), smartPlaylistOtherRuleCopy(language), smartPlaylistTextRuleCopy(language),
            smartPlaylistActionCopy(language), smartPlaylistMatchCopy(language), smartPlaylistEditorHeadingCopy(language),
            smartPlaylistEmptyCopy(language), smartPlaylistDeleteCopy(language), smartPlaylistMissingCopy(language),
            libraryScanStatusCopy(language),
            homeLayoutEditorCopy(language),
            tagEditorCopy(language), audiobookTagEditorCopy(language),
        )
        val english = copies(AppLanguage.English)
        val untranslated = mutableListOf<String>()
        AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
            copies(language).zip(english).forEach { (translated, source) ->
                source.javaClass.declaredFields.filter { it.type == String::class.java }.forEach { field ->
                    field.isAccessible = true
                    val sourceText = field.get(source) as String
                    val translatedText = field.get(translated) as String
                    if (sourceText.isNotBlank() && translatedText.isBlank() &&
                        !(source is SearchUiCopy && field.name == "noResultsSuffix")
                    ) {
                        untranslated += "${source.javaClass.simpleName}.${field.name} ($language): empty"
                    }
                    val nonLatinLanguage = language in setOf(
                        AppLanguage.Bengali, AppLanguage.Bulgarian, AppLanguage.ChineseSimplified,
                        AppLanguage.Greek, AppLanguage.Hindi, AppLanguage.Japanese, AppLanguage.Korean,
                        AppLanguage.Macedonian, AppLanguage.Punjabi, AppLanguage.Russian, AppLanguage.Serbian,
                        AppLanguage.Thai, AppLanguage.Ukrainian, AppLanguage.Urdu,
                    )
                    val commonEnglishLabel = sourceText in setOf(
                        "Settings", "Search", "Songs", "Artists", "Library", "Shuffle", "Queue",
                        "Favorites", "Playlists", "Save", "Delete", "Cancel", "Create", "Loading",
                    )
                    if (sourceText.trim().split(Regex("\\s+")).size >= 2 ||
                        (nonLatinLanguage && sourceText.matches(Regex("[A-Za-z]{4,}"))) || commonEnglishLabel
                    ) {
                        val sharedTechnicalTerm = (sourceText == "HTTPS server" &&
                            (language == AppLanguage.Bosnian || language == AppLanguage.Slovak)) ||
                            (source.javaClass.simpleName == "RootUiCopy" && field.name == "appName")
                        if (translatedText == sourceText && !sharedTechnicalTerm) {
                            untranslated += "${source.javaClass.simpleName}.${field.name} ($language): $sourceText"
                        }
                    }
                }
            }
        }
        assertTrue("English copy: ${untranslated.joinToString("; ")}", untranslated.isEmpty())
    }

    @Test
    fun oversizedUpdateErrorsAreLocalized() {
        AppLanguage.entries.forEach { language ->
            val declared = localizedUpdateError("Update download is too large", language)
            val streamed = localizedUpdateError("Update download exceeded its byte limit", language)
            assertEquals(declared, streamed)
            assertTrue(declared.isNotBlank())
            if (language != AppLanguage.English) assertFalse(declared == "The update exceeds the allowed download size")
        }
        assertEquals("Rozmiar pobieranej aktualizacji przekracza dozwolony limit", localizedUpdateError("Update download is too large", AppLanguage.Polish))
    }

    @Test
    fun playbackSourceUsesNaturalWordOrder() {
        val generatedSource = elovaire.music.droidbeauty.app.domain.model.SEARCH_PLAYBACK_SOURCE
        assertEquals("wyników wyszukiwania", localizedPlaybackSource(generatedSource, "Album", null, AppLanguage.Polish))
        assertEquals("search results", localizedPlaybackSource(generatedSource, "Album", null, AppLanguage.English))
        assertEquals(generatedSource, localizedPlaybackSource(generatedSource, "Album", 1L, AppLanguage.Polish))
        assertEquals(generatedSource, localizedPlaybackSource(generatedSource, generatedSource, null, AppLanguage.Polish))
        assertEquals("Album থেকে চলছে", playingFromText("Album", AppLanguage.Bengali))
        assertEquals("Album से चल रहा है", playingFromText("Album", AppLanguage.Hindi))
        assertEquals("Album ਤੋਂ ਚੱਲ ਰਿਹਾ ਹੈ", playingFromText("Album", AppLanguage.Punjabi))
        assertEquals("Album سے چل رہا ہے", playingFromText("Album", AppLanguage.Urdu))
        assertEquals("Playing from Album", playingFromText("Album", AppLanguage.English))
        assertEquals("Odtwarzanie z wszystkich utworów", playingFromText(localizedAllSongsSource(AppLanguage.Polish), AppLanguage.Polish))
        assertEquals("सभी गानों से चल रहा है", playingFromText(localizedAllSongsSource(AppLanguage.Hindi), AppLanguage.Hindi))
        assertEquals("Wiedergabe aus allen Titeln", playingFromText(localizedAllSongsSource(AppLanguage.German), AppLanguage.German))
        assertEquals("Odtwarzanie z wyników wyszukiwania", playingFromText(localizedSearchResultsSource(AppLanguage.Polish), AppLanguage.Polish))
        AppLanguage.entries.forEach { assertTrue(localizedAllSongsSource(it).isNotBlank()) }
        AppLanguage.entries.forEach { assertTrue(localizedSearchResultsSource(it).isNotBlank()) }
    }

    @Test
    fun mostPlayedSmartPlaylistUsesKoreanCopy() {
        assertEquals("가장 많이 재생한 곡", builtInSmartPlaylistTitle(BuiltInSmartPlaylistType.MostPlayed, AppLanguage.Korean))
    }

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
        val smartSource = elovaire.music.droidbeauty.app.domain.model.SMART_PLAYLIST_PLAYBACK_SOURCE_PREFIX + BuiltInSmartPlaylistType.MostPlayed.name
        assertEquals("Najczęściej odtwarzane", localizedPlaybackSource(smartSource, "Album", null, AppLanguage.Polish))
        assertEquals("Most played", localizedPlaybackSource(smartSource, "Album", null, AppLanguage.English))
        assertEquals(smartSource, localizedPlaybackSource(smartSource, "Album", 4L, AppLanguage.Polish))
        val bucket = elovaire.music.droidbeauty.app.domain.model.SONG_BUCKET_PLAYBACK_SOURCE_PREFIX + "T"
        assertEquals("Utwory T", localizedPlaybackSource(bucket, "Album", null, AppLanguage.Polish))
        assertEquals("Songs T", localizedPlaybackSource(bucket, "Album", null, AppLanguage.English))
        assertEquals("Nieznany album", localizedPlaybackSource("Unknown Album", "Unknown Album", null, AppLanguage.Polish))
        assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Unknown Album", 4L, AppLanguage.Polish))
        assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Another album", null, AppLanguage.Polish))
        assertEquals("A real album", localizedPlaybackSource("A real album", "A real album", null, AppLanguage.Polish))
        AppLanguage.entries.forEach { language ->
            val localized = localizedPlaybackSource("Unknown Album", "Unknown Album", null, language)
            assertEquals(localizedAlbumTitle("Unknown Album", language), localized)
            assertEquals("Unknown Album", localizedPlaybackSource("Unknown Album", "Unknown Album", 4L, language))
            val allSongs = elovaire.music.droidbeauty.app.domain.model.ALL_SONGS_PLAYBACK_SOURCE
            val search = elovaire.music.droidbeauty.app.domain.model.SEARCH_PLAYBACK_SOURCE
            assertEquals(localizedAllSongsSource(language), localizedPlaybackSource(allSongs, "Album", null, language))
            assertEquals(localizedSearchResultsSource(language), localizedPlaybackSource(search, "Album", null, language))
            assertEquals(localizedArtistName("Unknown Artist", language), localizedPlaybackSource(elovaire.music.droidbeauty.app.domain.model.UNKNOWN_ARTIST_PLAYBACK_SOURCE, "Album", null, language))
            assertEquals(localizedGenreName("Unknown Genre", language), localizedPlaybackSource(elovaire.music.droidbeauty.app.domain.model.UNKNOWN_GENRE_PLAYBACK_SOURCE, "Album", null, language))
            BuiltInSmartPlaylistType.entries.forEach { type ->
                val generated = elovaire.music.droidbeauty.app.domain.model.SMART_PLAYLIST_PLAYBACK_SOURCE_PREFIX + type.name
                assertEquals(builtInSmartPlaylistTitle(type, language), localizedPlaybackSource(generated, "Album", null, language))
            }
            assertEquals(search, localizedPlaybackSource(search, "Album", 4L, language))
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
    fun bosnianAlbumCountsUseCorrectDeclension() {
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
        val polish = searchCopy(AppLanguage.Polish)
        assertEquals("1 pasujący artysta", polish.matchingArtists(1, AppLanguage.Polish))
        assertEquals("2 pasujących artystów", polish.matchingArtists(2, AppLanguage.Polish))
        assertEquals("22 pasujących artystów", polish.matchingArtists(22, AppLanguage.Polish))
        assertEquals("2 pasujące albumy", polish.matchingAlbums(2, AppLanguage.Polish))
        assertEquals("搜索结果：2 位艺术家", searchCopy(AppLanguage.ChineseSimplified).matchingArtists(2, AppLanguage.ChineseSimplified))
        assertEquals("検索結果：2 曲", searchCopy(AppLanguage.Japanese).matchingSongs(2, AppLanguage.Japanese))
        assertEquals("검색 결과: 2 개의 앨범", searchCopy(AppLanguage.Korean).matchingAlbums(2, AppLanguage.Korean))
        assertEquals("Hasil: 2 artis", searchCopy(AppLanguage.Indonesian).matchingArtists(2, AppLanguage.Indonesian))
        assertEquals("Hasil: 2 lagu", searchCopy(AppLanguage.Malay).matchingSongs(2, AppLanguage.Malay))
        assertEquals("ผลการค้นหา: ศิลปิน 2 คน", searchCopy(AppLanguage.Thai).matchingArtists(2, AppLanguage.Thai))
        assertEquals("ผลการค้นหา: 2 อัลบั้ม", searchCopy(AppLanguage.Thai).matchingAlbums(2, AppLanguage.Thai))
        assertEquals("ผลการค้นหา: 2 เพลง", searchCopy(AppLanguage.Thai).matchingSongs(2, AppLanguage.Thai))
        assertEquals("Αποτελέσματα: 1 τραγούδι", searchCopy(AppLanguage.Greek).matchingSongs(1, AppLanguage.Greek))
        assertEquals("Αποτελέσματα: 2 τραγούδια", searchCopy(AppLanguage.Greek).matchingSongs(2, AppLanguage.Greek))
        assertEquals("Találatok: 2 dal", searchCopy(AppLanguage.Hungarian).matchingSongs(2, AppLanguage.Hungarian))
        assertEquals("Rezultate: 2 artistë", searchCopy(AppLanguage.Albanian).matchingArtists(2, AppLanguage.Albanian))
    }

    @Test
    fun playlistCountsFollowHigherNumberBoundaries() {
        assertEquals("100 de liste", localizedCountLabel(100, "playlist", AppLanguage.Romanian))
        assertEquals("101 liste", localizedCountLabel(101, "playlist", AppLanguage.Romanian))
        assertEquals("102 seznama predvajanja", localizedCountLabel(102, "playlist", AppLanguage.Slovenian))
        assertEquals("104 seznami predvajanja", localizedCountLabel(104, "playlist", AppLanguage.Slovenian))
        assertEquals("111 atskaņošanas sarakstu", localizedCountLabel(111, "playlist", AppLanguage.Latvian))
        assertEquals("121 grojaraštis", localizedCountLabel(121, "playlist", AppLanguage.Lithuanian))
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
        assertEquals("5 nalezených alb", copy.matchingAlbums(5, AppLanguage.Czech))
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
        assertEquals("102 najdeni skladbi", copy.matchingSongs(102, AppLanguage.Slovenian))
        assertEquals("111 najdenih albumov", copy.matchingAlbums(111, AppLanguage.Slovenian))
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
            assertEquals(unknown, localizedAudiobookAuthor("Unknown Artist", language))
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
    fun widgetControlLabelsAreAvailableInEveryLanguage() {
        AppLanguage.entries.forEach { language ->
            val copy = widgetCopy(language)
            listOf(copy.play, copy.pause, copy.previous, copy.next, copy.unknownArtist, copy.externalAudio).forEach {
                assertTrue("Empty widget label for $language", it.isNotBlank())
            }
            assertEquals(playLabel(language), copy.play)
            assertEquals(pauseLabel(language), copy.pause)
            if (language != AppLanguage.English) {
                assertFalse("English previous label for $language", copy.previous == "Previous track")
                assertFalse("English next label for $language", copy.next == "Next track")
            }
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
            playlistAccessibilityCopy(AppLanguage.English),
            settingsAccessibilityCopy(AppLanguage.English),
            playerControlAccessibilityCopy(AppLanguage.English),
            mediaOptionsAccessibilityCopy(AppLanguage.English),
            lyricsAccessibilityCopy(AppLanguage.English),
            navigationAccessibilityCopy(AppLanguage.English),
            playbackNavigationAccessibilityCopy(AppLanguage.English),
            shuffleAccessibilityCopy(AppLanguage.English),
            viewModeAccessibilityCopy(AppLanguage.English),
            carMediaCopy(AppLanguage.English),
            sleepTimerCopy(AppLanguage.English),
            tagAdditionalIssuesCopy(AppLanguage.English),
            libraryFoldersCopy(AppLanguage.English),
            networkSourcesCopy(AppLanguage.English),
            commonUiCopy(AppLanguage.English),
            playlistMainCopy(AppLanguage.English),
            audiobookUnavailableCopy(AppLanguage.English),
            audiobookDescriptionCopy(AppLanguage.English),
            audiobookCopy(AppLanguage.English),
            nowPlayingBarStyleCopy(AppLanguage.English),
            smartPlaylistOrderCopy(AppLanguage.English),
            smartPlaylistInputCopy(AppLanguage.English),
            smartPlaylistValueCopy(AppLanguage.English),
            smartPlaylistOtherRuleCopy(AppLanguage.English),
            smartPlaylistTextRuleCopy(AppLanguage.English),
            audiobookExtraCopy(AppLanguage.English),
            audiobookSettingsCopy(AppLanguage.English),
            smartPlaylistSettingsCopy(AppLanguage.English),
            equalizerCopy(AppLanguage.English),
            artistInfoCopy(AppLanguage.English),
            smartPlaylistActionCopy(AppLanguage.English),
            smartPlaylistMatchCopy(AppLanguage.English),
            smartPlaylistEditorHeadingCopy(AppLanguage.English),
            libraryScanStatusCopy(AppLanguage.English),
            playlistManagementCopy(AppLanguage.English),
            crossfadeCopy(AppLanguage.English),
            homeLayoutEditorCopy(AppLanguage.English),
            tagEditorCopy(AppLanguage.English), audiobookTagEditorCopy(AppLanguage.English),
        )
        val values = copies.flatMap { copy ->
            copy.javaClass.declaredFields.filter { it.type == String::class.java }.map { field ->
                field.isAccessible = true
                field.get(copy) as String
            }
        } + privacyPolicyCopy(AppLanguage.English).sections.flatMap { listOf(it.title, it.body) } +
            UiPhrase.entries.map { uiPhrase(AppLanguage.English, it) } +
            MiscPhrase.entries.map { miscPhrase(AppLanguage.English, it) } + listOf(
            localizedTagSaveStatus("The song file is empty.", 1, AppLanguage.English),
            localizedTagSaveStatus("The song file is not writable.", 1, AppLanguage.English),
            localizedTagSaveStatus("The file path is unavailable.", 1, AppLanguage.English),
            localizedTagSaveStatus("The song is already being edited.", 1, AppLanguage.English),
            localizedTagSaveStatus("There is not enough temporary storage to edit this song safely.", 1, AppLanguage.English),
            localizedUpdateError("Update download is too large", AppLanguage.English),
            homeLayoutTitle(AppLanguage.English),
            homeLayoutSubtitle(AppLanguage.English),
            droidBeautyDescription(AppLanguage.English),
        )
        val sentenceBoundary = Regex("(?<!\\.)\\.(?!\\.)\\s+(?=[A-Z])")
        values.forEach { value ->
            val text = value.trimEnd()
            assertFalse("Unexpected final period: $value", text.endsWith('.') && !text.endsWith("..."))
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
        assertEquals("Esittäjän nimi sisältää", smartPlaylistTextRuleCopy(AppLanguage.Finnish).artist)
        assertEquals("Albumo pavadinime yra", smartPlaylistTextRuleCopy(AppLanguage.Lithuanian).album)
        assertEquals("Le nom de l’artiste contient", smartPlaylistTextRuleCopy(AppLanguage.French).artist)
        assertEquals("Názov albumu obsahuje", smartPlaylistTextRuleCopy(AppLanguage.Slovak).album)
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
    fun privacyTranslationsRetainTheSharedServiceAndSecurityDetails() {
        val english = privacyPolicyCopy(AppLanguage.English).sections
        AppLanguage.entries.forEach { language ->
            val sections = privacyPolicyCopy(language).sections
            assertEquals("Section count for $language", english.size, sections.size)
            sections.zip(english).forEach { (section, source) ->
                assertTrue("Empty privacy section for $language", section.title.isNotBlank() && section.body.isNotBlank())
                if (language != AppLanguage.English) {
                    assertFalse("English privacy heading for $language", section.title == source.title)
                    assertFalse("English privacy body for $language", section.body == source.body)
                }
            }
            listOf("LRCLIB", "YouTube Music", "Google Books").forEach {
                assertTrue("Missing $it for $language", sections[2].body.contains(it))
            }
            listOf("SMB", "WebDAV").forEach {
                assertTrue("Missing $it for $language", sections[4].body.contains(it))
            }
            listOf("GitHub", "APK", "IP").forEach {
                assertTrue("Missing $it for $language", sections[6].body.contains(it))
            }
            assertTrue("Missing source link for $language", sections[7].body.contains("github.com/droidbeauty/elovaire-music"))
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
    fun firstLaunchMessageExplainsThatMusicFolderIsTheDefault() {
        val defaultTerms = mapOf(
            AppLanguage.Bulgarian to "по подразбиране",
            AppLanguage.Catalan to "per defecte",
            AppLanguage.Finnish to "oletusarvoisesti",
            AppLanguage.Romanian to "în mod implicit",
            AppLanguage.Slovenian to "privzeto",
            AppLanguage.Bosnian to "prema zadanim postavkama",
            AppLanguage.Indonesian to "default",
            AppLanguage.Punjabi to "ਮੂਲ ਰੂਪ ਵਿੱਚ",
        )
        defaultTerms.forEach { (language, term) ->
            val message = rootUiCopy(language).firstLaunchPermissionMessage
            assertTrue("Missing default qualifier in $language", message.contains(term, ignoreCase = true))
            assertTrue("Missing Music folder in $language", message.contains("Music"))
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
    fun miscellaneousPhrasesAreTranslatedForEveryLanguage() {
        MiscPhrase.entries.forEach { phrase ->
            val english = miscPhrase(AppLanguage.English, phrase)
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                val translated = miscPhrase(language, phrase)
                assertTrue("Missing $phrase in $language", translated.isNotBlank())
                assertFalse("English $phrase in $language", translated == english)
            }
        }
    }

    @Test
    fun sharedSentencePhrasesAreNotLeftInEnglish() {
        val untranslated = mutableListOf<String>()
        UiPhrase.entries.forEach { phrase ->
            val english = uiPhrase(AppLanguage.English, phrase)
            if (english.trim().split(Regex("\\s+")).size < 2) return@forEach
            AppLanguage.entries.filterNot { it == AppLanguage.English }.forEach { language ->
                if (uiPhrase(language, phrase) == english) untranslated += "$phrase ($language): $english"
            }
        }
        assertTrue("English phrases: ${untranslated.joinToString("; ")}", untranslated.isEmpty())
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
