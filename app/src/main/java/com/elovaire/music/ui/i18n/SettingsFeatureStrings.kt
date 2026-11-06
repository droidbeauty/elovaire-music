package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType

internal data class AudiobookExtraCopy(val speed: String, val speedSubtitle: String, val rewind: String, val rewindSubtitle: String)

internal fun audiobookPlaybackCategory(language: AppLanguage): String = when (language) {
    AppLanguage.Estonian -> EstonianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Greek -> GreekTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Latvian -> LatvianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Malay -> MalayTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Hindi -> HindiTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Korean -> KoreanTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Thai -> ThaiTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Bengali -> BengaliTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Croatian -> CroatianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Serbian -> SerbianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Urdu -> UrduTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Catalan -> CatalanTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Finnish -> FinnishTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Romanian -> RomanianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Polish -> PolishTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.German -> GermanTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.French -> FrenchTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Spanish -> SpanishTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Italian -> ItalianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Russian -> RussianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Czech -> CzechTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Slovak -> SlovakTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Dutch -> DutchTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Swedish -> SwedishTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Danish -> DanishTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_audiobookPlaybackCategory_1
    AppLanguage.English -> EnglishTranslations.settingsFeature_audiobookPlaybackCategory_1
}

internal fun audiobookExtraCopy(language: AppLanguage): AudiobookExtraCopy = when (language) {
    AppLanguage.Estonian -> EstonianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Greek -> GreekTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Latvian -> LatvianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Malay -> MalayTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Hindi -> HindiTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Korean -> KoreanTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Thai -> ThaiTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Bengali -> BengaliTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Croatian -> CroatianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Serbian -> SerbianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Urdu -> UrduTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Catalan -> CatalanTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Finnish -> FinnishTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Romanian -> RomanianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Polish -> PolishTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.German -> GermanTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.French -> FrenchTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Spanish -> SpanishTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Italian -> ItalianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Russian -> RussianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Czech -> CzechTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Slovak -> SlovakTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Dutch -> DutchTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Swedish -> SwedishTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Danish -> DanishTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_audiobookExtraCopy_1
    AppLanguage.English -> EnglishTranslations.settingsFeature_audiobookExtraCopy_1
}

internal data class AudiobookSettingsCopy(
    val title: String,
    val subtitle: String,
    val rewindAmount: String,
    val forwardAmount: String,
    val seconds: String,
    val resumePlayback: String,
    val resumePlaybackSubtitle: String,
)

internal fun audiobookSettingsCopy(language: AppLanguage): AudiobookSettingsCopy = when (language) {
    AppLanguage.Estonian -> EstonianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Greek -> GreekTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Latvian -> LatvianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Malay -> MalayTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Hindi -> HindiTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Korean -> KoreanTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Thai -> ThaiTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Bengali -> BengaliTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Croatian -> CroatianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Serbian -> SerbianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Urdu -> UrduTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Catalan -> CatalanTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Finnish -> FinnishTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Romanian -> RomanianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Polish -> PolishTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.German -> GermanTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.French -> FrenchTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Spanish -> SpanishTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Italian -> ItalianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Russian -> RussianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Czech -> CzechTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Slovak -> SlovakTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Dutch -> DutchTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Swedish -> SwedishTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Danish -> DanishTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_audiobookSettingsCopy_1
    AppLanguage.English -> EnglishTranslations.settingsFeature_audiobookSettingsCopy_1
}

internal data class SmartPlaylistSettingsCopy(
    val title: String,
    val subtitle: String,
    val availableMixes: String,
    val maximumSongs: String,
    val maximumSongsSubtitle: String,
)

internal fun smartPlaylistSettingsCopy(language: AppLanguage): SmartPlaylistSettingsCopy = when (language) {
    AppLanguage.Estonian -> EstonianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Greek -> GreekTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Latvian -> LatvianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Malay -> MalayTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Hindi -> HindiTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Korean -> KoreanTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Thai -> ThaiTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Bengali -> BengaliTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Croatian -> CroatianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Serbian -> SerbianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Urdu -> UrduTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Catalan -> CatalanTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Finnish -> FinnishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Romanian -> RomanianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Polish -> PolishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.German -> GermanTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.French -> FrenchTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Spanish -> SpanishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Italian -> ItalianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Russian -> RussianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Czech -> CzechTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Slovak -> SlovakTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Dutch -> DutchTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Swedish -> SwedishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Danish -> DanishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_smartPlaylistSettingsCopy_1
    AppLanguage.English -> EnglishTranslations.settingsFeature_smartPlaylistSettingsCopy_1
}

@Suppress("CyclomaticComplexMethod")
internal fun smartPlaylistDescription(type: BuiltInSmartPlaylistType, language: AppLanguage): String = when (type) {
    BuiltInSmartPlaylistType.RecentlyAdded -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Catalan -> CatalanTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Finnish -> FinnishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Romanian -> RomanianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Bengali -> BengaliTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Croatian -> CroatianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Czech -> CzechTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Danish -> DanishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Dutch -> DutchTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.English -> EnglishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Estonian -> EstonianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.French -> FrenchTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.German -> GermanTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Greek -> GreekTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Hindi -> HindiTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Italian -> ItalianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Korean -> KoreanTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Latvian -> LatvianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Malay -> MalayTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Polish -> PolishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Russian -> RussianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Serbian -> SerbianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Slovak -> SlovakTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Spanish -> SpanishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Swedish -> SwedishTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Thai -> ThaiTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_smartPlaylistDescription_1
        AppLanguage.Urdu -> UrduTranslations.settingsFeature_smartPlaylistDescription_1
    }
    BuiltInSmartPlaylistType.MostPlayed -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Catalan -> CatalanTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Finnish -> FinnishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Romanian -> RomanianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Bengali -> BengaliTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Croatian -> CroatianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Czech -> CzechTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Danish -> DanishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Dutch -> DutchTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.English -> EnglishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Estonian -> EstonianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.French -> FrenchTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.German -> GermanTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Greek -> GreekTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Hindi -> HindiTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Italian -> ItalianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Korean -> KoreanTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Latvian -> LatvianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Malay -> MalayTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Polish -> PolishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Russian -> RussianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Serbian -> SerbianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Slovak -> SlovakTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Spanish -> SpanishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Swedish -> SwedishTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Thai -> ThaiTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_smartPlaylistDescription_2
        AppLanguage.Urdu -> UrduTranslations.settingsFeature_smartPlaylistDescription_2
    }
    BuiltInSmartPlaylistType.Favorites -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Catalan -> CatalanTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Finnish -> FinnishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Romanian -> RomanianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Slovenian -> SlovenianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Bosnian -> BosnianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Indonesian -> IndonesianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Punjabi -> PunjabiTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Albanian -> AlbanianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Bengali -> BengaliTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Croatian -> CroatianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Czech -> CzechTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Danish -> DanishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Dutch -> DutchTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.English -> EnglishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Estonian -> EstonianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.French -> FrenchTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.German -> GermanTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Greek -> GreekTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Hindi -> HindiTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Hungarian -> HungarianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Italian -> ItalianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Japanese -> JapaneseTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Korean -> KoreanTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Latvian -> LatvianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Lithuanian -> LithuanianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Macedonian -> MacedonianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Malay -> MalayTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Norwegian -> NorwegianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Polish -> PolishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Portuguese -> PortugueseTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Russian -> RussianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Serbian -> SerbianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Slovak -> SlovakTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Spanish -> SpanishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Swedish -> SwedishTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Thai -> ThaiTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Ukrainian -> UkrainianTranslations.settingsFeature_smartPlaylistDescription_3
        AppLanguage.Urdu -> UrduTranslations.settingsFeature_smartPlaylistDescription_3
    }
}
