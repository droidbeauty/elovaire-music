package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal fun settingsCopy(language: AppLanguage): SettingsLanguageCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Catalan -> CatalanTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Finnish -> FinnishTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Romanian -> RomanianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Slovenian -> SlovenianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Bosnian -> BosnianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Indonesian -> IndonesianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Punjabi -> PunjabiTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Polish -> PolishTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Czech -> CzechTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Lithuanian -> LithuanianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Danish -> DanishTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.French -> FrenchTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.German -> GermanTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Dutch -> DutchTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Norwegian -> NorwegianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Swedish -> SwedishTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Spanish -> SpanishTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Portuguese -> PortugueseTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Estonian -> EstonianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Greek -> GreekTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Croatian -> CroatianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Russian -> RussianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Ukrainian -> UkrainianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Latvian -> LatvianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Italian -> ItalianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Japanese -> JapaneseTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Albanian -> AlbanianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Hindi -> HindiTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Hungarian -> HungarianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Macedonian -> MacedonianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Serbian -> SerbianTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Thai -> ThaiTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Slovak -> SlovakTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Korean -> KoreanTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Malay -> MalayTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Bengali -> BengaliTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.Urdu -> UrduTranslations.settings_settingsCopy_dynamic_1(language)
    AppLanguage.English -> EnglishTranslations.settings_settingsCopy_dynamic_1(language)
}.withLocalizedVolumeNormalization(language).withLocalizedSettingsEntries(language)

internal data class LibraryScanStatusCopy(
    val scanning: String,
    val done: String,
)

internal fun libraryScanStatusCopy(language: AppLanguage): LibraryScanStatusCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Catalan -> CatalanTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Finnish -> FinnishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Romanian -> RomanianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Bengali -> BengaliTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Croatian -> CroatianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Czech -> CzechTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Danish -> DanishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Dutch -> DutchTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.English -> EnglishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Estonian -> EstonianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.French -> FrenchTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.German -> GermanTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Greek -> GreekTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Hindi -> HindiTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Italian -> ItalianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Korean -> KoreanTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Latvian -> LatvianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Malay -> MalayTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Polish -> PolishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Russian -> RussianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Serbian -> SerbianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Slovak -> SlovakTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Spanish -> SpanishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Swedish -> SwedishTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Thai -> ThaiTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settings_libraryScanStatusCopy_1
    AppLanguage.Urdu -> UrduTranslations.settings_libraryScanStatusCopy_1
}

internal fun SettingsLanguageCopy.withLocalizedVolumeNormalization(language: AppLanguage): SettingsLanguageCopy {
    return copy(
        volumeNormalization = volumeNormalizationTitle(language),
        volumeNormalizationSubtitle = volumeNormalizationSubtitle(language),
    )
}

private fun volumeNormalizationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Catalan -> CatalanTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Finnish -> FinnishTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Romanian -> RomanianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Bosnian -> BosnianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Polish -> PolishTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Albanian -> AlbanianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Croatian -> CroatianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Czech -> CzechTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Danish -> DanishTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Dutch -> DutchTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Estonian -> EstonianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.French -> FrenchTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.German -> GermanTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Greek -> GreekTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Hindi -> HindiTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Hungarian -> HungarianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Italian -> ItalianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Japanese -> JapaneseTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Latvian -> LatvianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Russian -> RussianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Serbian -> SerbianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Spanish -> SpanishTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Swedish -> SwedishTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Thai -> ThaiTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Slovak -> SlovakTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Korean -> KoreanTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Malay -> MalayTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Bengali -> BengaliTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.Urdu -> UrduTranslations.settings_volumeNormalizationTitle_1
    AppLanguage.English -> EnglishTranslations.settings_volumeNormalizationTitle_1
}

internal data class PlaylistManagementCopy(
    val title: String,
    val importAction: String,
    val exportAll: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val exportAction: String,
)

@Suppress("CyclomaticComplexMethod")
internal fun playlistManagementCopy(language: AppLanguage): PlaylistManagementCopy {
    val labels = when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Catalan -> CatalanTranslations.settings_playlistManagementCopy_1
        AppLanguage.Finnish -> FinnishTranslations.settings_playlistManagementCopy_1
        AppLanguage.Romanian -> RomanianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Slovenian -> SlovenianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Bosnian -> BosnianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Indonesian -> IndonesianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Punjabi -> PunjabiTranslations.settings_playlistManagementCopy_1
        AppLanguage.Albanian -> AlbanianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Bengali -> BengaliTranslations.settings_playlistManagementCopy_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_playlistManagementCopy_1
        AppLanguage.Croatian -> CroatianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Czech -> CzechTranslations.settings_playlistManagementCopy_1
        AppLanguage.Danish -> DanishTranslations.settings_playlistManagementCopy_1
        AppLanguage.Dutch -> DutchTranslations.settings_playlistManagementCopy_1
        AppLanguage.Estonian -> EstonianTranslations.settings_playlistManagementCopy_1
        AppLanguage.French -> FrenchTranslations.settings_playlistManagementCopy_1
        AppLanguage.German -> GermanTranslations.settings_playlistManagementCopy_1
        AppLanguage.Greek -> GreekTranslations.settings_playlistManagementCopy_1
        AppLanguage.Hindi -> HindiTranslations.settings_playlistManagementCopy_1
        AppLanguage.Hungarian -> HungarianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Italian -> ItalianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Japanese -> JapaneseTranslations.settings_playlistManagementCopy_1
        AppLanguage.Korean -> KoreanTranslations.settings_playlistManagementCopy_1
        AppLanguage.Latvian -> LatvianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Lithuanian -> LithuanianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Macedonian -> MacedonianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Malay -> MalayTranslations.settings_playlistManagementCopy_1
        AppLanguage.Norwegian -> NorwegianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Polish -> PolishTranslations.settings_playlistManagementCopy_1
        AppLanguage.Portuguese -> PortugueseTranslations.settings_playlistManagementCopy_1
        AppLanguage.Russian -> RussianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Serbian -> SerbianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Slovak -> SlovakTranslations.settings_playlistManagementCopy_1
        AppLanguage.Spanish -> SpanishTranslations.settings_playlistManagementCopy_1
        AppLanguage.Swedish -> SwedishTranslations.settings_playlistManagementCopy_1
        AppLanguage.Thai -> ThaiTranslations.settings_playlistManagementCopy_1
        AppLanguage.Ukrainian -> UkrainianTranslations.settings_playlistManagementCopy_1
        AppLanguage.Urdu -> UrduTranslations.settings_playlistManagementCopy_1
        AppLanguage.English -> EnglishTranslations.settings_playlistManagementCopy_1
    }
    val settings = settingsCopy(language)
    return PlaylistManagementCopy(
        title = settings.managePlaylistsTitle,
        importAction = labels[0],
        exportAll = labels[1],
        emptyTitle = labels[2],
        emptySubtitle = labels[3],
        exportAction = labels[4],
    )
}

internal data class CrossfadeCopy(
    val fadeLength: String,
    val fadeLengthExplanation: String,
    val silenceDetection: String,
    val silenceDetectionExplanation: String,
)

@Suppress("CyclomaticComplexMethod")
internal fun crossfadeCopy(language: AppLanguage): CrossfadeCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.settings_crossfadeCopy_1
    AppLanguage.Catalan -> CatalanTranslations.settings_crossfadeCopy_1
    AppLanguage.Finnish -> FinnishTranslations.settings_crossfadeCopy_1
    AppLanguage.Romanian -> RomanianTranslations.settings_crossfadeCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.settings_crossfadeCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.settings_crossfadeCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.settings_crossfadeCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.settings_crossfadeCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.settings_crossfadeCopy_1
    AppLanguage.Bengali -> BengaliTranslations.settings_crossfadeCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_crossfadeCopy_1
    AppLanguage.Croatian -> CroatianTranslations.settings_crossfadeCopy_1
    AppLanguage.Czech -> CzechTranslations.settings_crossfadeCopy_1
    AppLanguage.Danish -> DanishTranslations.settings_crossfadeCopy_1
    AppLanguage.Dutch -> DutchTranslations.settings_crossfadeCopy_1
    AppLanguage.Estonian -> EstonianTranslations.settings_crossfadeCopy_1
    AppLanguage.French -> FrenchTranslations.settings_crossfadeCopy_1
    AppLanguage.German -> GermanTranslations.settings_crossfadeCopy_1
    AppLanguage.Greek -> GreekTranslations.settings_crossfadeCopy_1
    AppLanguage.Hindi -> HindiTranslations.settings_crossfadeCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.settings_crossfadeCopy_1
    AppLanguage.Italian -> ItalianTranslations.settings_crossfadeCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.settings_crossfadeCopy_1
    AppLanguage.Korean -> KoreanTranslations.settings_crossfadeCopy_1
    AppLanguage.Latvian -> LatvianTranslations.settings_crossfadeCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settings_crossfadeCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.settings_crossfadeCopy_1
    AppLanguage.Malay -> MalayTranslations.settings_crossfadeCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.settings_crossfadeCopy_1
    AppLanguage.Polish -> PolishTranslations.settings_crossfadeCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.settings_crossfadeCopy_1
    AppLanguage.Russian -> RussianTranslations.settings_crossfadeCopy_1
    AppLanguage.Serbian -> SerbianTranslations.settings_crossfadeCopy_1
    AppLanguage.Slovak -> SlovakTranslations.settings_crossfadeCopy_1
    AppLanguage.Spanish -> SpanishTranslations.settings_crossfadeCopy_1
    AppLanguage.Swedish -> SwedishTranslations.settings_crossfadeCopy_1
    AppLanguage.Thai -> ThaiTranslations.settings_crossfadeCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settings_crossfadeCopy_1
    AppLanguage.Urdu -> UrduTranslations.settings_crossfadeCopy_1
    AppLanguage.English -> EnglishTranslations.settings_crossfadeCopy_1
}

internal fun volumeNormalizationSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Catalan -> CatalanTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Finnish -> FinnishTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Romanian -> RomanianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Bosnian -> BosnianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Polish -> PolishTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Albanian -> AlbanianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Croatian -> CroatianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Czech -> CzechTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Danish -> DanishTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Dutch -> DutchTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Estonian -> EstonianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.French -> FrenchTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.German -> GermanTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Greek -> GreekTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Hindi -> HindiTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Hungarian -> HungarianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Italian -> ItalianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Japanese -> JapaneseTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Latvian -> LatvianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Russian -> RussianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Serbian -> SerbianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Spanish -> SpanishTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Swedish -> SwedishTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Thai -> ThaiTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Slovak -> SlovakTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Korean -> KoreanTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Malay -> MalayTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Bengali -> BengaliTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.Urdu -> UrduTranslations.settings_volumeNormalizationSubtitle_1
    AppLanguage.English -> EnglishTranslations.settings_volumeNormalizationSubtitle_1
}

internal data class SettingsEntryCopy(
    val crossfadeTitle: String,
    val crossfadeSubtitle: String,
    val managePlaylistsTitle: String,
    val managePlaylistsSubtitle: String,
    val onlineLyricsTitle: String,
    val onlineLyricsSubtitle: String,
    val checkForUpdatesTitle: String,
    val checkForUpdatesSubtitle: String,
)

internal fun SettingsLanguageCopy.withLocalizedSettingsEntries(language: AppLanguage): SettingsLanguageCopy {
    val entries = when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Catalan -> CatalanTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Finnish -> FinnishTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Romanian -> RomanianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Slovenian -> SlovenianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Bosnian -> BosnianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Indonesian -> IndonesianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Punjabi -> PunjabiTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Albanian -> AlbanianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Bengali -> BengaliTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Croatian -> CroatianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Czech -> CzechTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Danish -> DanishTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Dutch -> DutchTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Estonian -> EstonianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.French -> FrenchTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.German -> GermanTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Greek -> GreekTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Hindi -> HindiTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Hungarian -> HungarianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Italian -> ItalianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Japanese -> JapaneseTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Korean -> KoreanTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Latvian -> LatvianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Lithuanian -> LithuanianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Macedonian -> MacedonianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Malay -> MalayTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Norwegian -> NorwegianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Polish -> PolishTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Portuguese -> PortugueseTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Russian -> RussianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Serbian -> SerbianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Slovak -> SlovakTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Spanish -> SpanishTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Swedish -> SwedishTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Thai -> ThaiTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Ukrainian -> UkrainianTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.Urdu -> UrduTranslations.settings_withLocalizedSettingsEntries_1
        AppLanguage.English -> EnglishTranslations.settings_withLocalizedSettingsEntries_1
    }
    return copy(
        crossfadeTitle = entries.crossfadeTitle,
        crossfadeSubtitle = entries.crossfadeSubtitle,
        managePlaylistsTitle = entries.managePlaylistsTitle,
        managePlaylistsSubtitle = entries.managePlaylistsSubtitle,
        onlineLyricsTitle = entries.onlineLyricsTitle,
        onlineLyricsSubtitle = entries.onlineLyricsSubtitle,
        checkForUpdatesTitle = entries.checkForUpdatesTitle,
        checkForUpdatesSubtitle = entries.checkForUpdatesSubtitle,
    )
}
