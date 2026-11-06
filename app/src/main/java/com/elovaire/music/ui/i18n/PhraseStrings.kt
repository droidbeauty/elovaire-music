package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal enum class UiPhrase {
    About,
    AddToPlaylist,
    AddToQueue,
    GoToAlbum,
    DeleteFromLibrary,
    DeleteAlbum,
    Delete,
    Rename,
    RemoveFromList,
    NewPlaylist,
    Cancel,
    Create,
    Reset,
    Dry,
    Wet,
    Off,
    Reverb,
    ToneShaping,
    Bass,
    Midrange,
    Treble,
    EffectStrength,
}

internal fun uiPhrase(language: AppLanguage, phrase: UiPhrase): String {
    return uiPhraseTranslations[language]?.get(phrase) ?: uiPhraseTranslations.getValue(AppLanguage.English).getValue(phrase)
}

internal fun hasUiPhraseTranslation(language: AppLanguage, phrase: UiPhrase): Boolean =
    uiPhraseTranslations[language]?.containsKey(phrase) == true

private val uiPhraseTranslations = mapOf(
    AppLanguage.Bulgarian to BulgarianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Catalan to CatalanTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Finnish to FinnishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Romanian to RomanianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Slovenian to SlovenianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Bosnian to BosnianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Indonesian to IndonesianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Punjabi to PunjabiTranslations.phrase_uiPhraseTranslations,
    AppLanguage.English to EnglishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Polish to PolishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Albanian to AlbanianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.ChineseSimplified to ChineseSimplifiedTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Croatian to CroatianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Slovak to SlovakTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Korean to KoreanTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Malay to MalayTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Bengali to BengaliTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Urdu to UrduTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Czech to CzechTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Danish to DanishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Dutch to DutchTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Estonian to EstonianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.French to FrenchTranslations.phrase_uiPhraseTranslations,
    AppLanguage.German to GermanTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Greek to GreekTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Hindi to HindiTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Hungarian to HungarianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Italian to ItalianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Japanese to JapaneseTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Latvian to LatvianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Lithuanian to LithuanianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Macedonian to MacedonianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Norwegian to NorwegianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Portuguese to PortugueseTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Russian to RussianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Serbian to SerbianTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Spanish to SpanishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Swedish to SwedishTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Thai to ThaiTranslations.phrase_uiPhraseTranslations,
    AppLanguage.Ukrainian to UkrainianTranslations.phrase_uiPhraseTranslations,
)

internal fun SpaciousnessMode.displayLabel(language: AppLanguage = AppLanguage.English): String {
    return when (this) {
        SpaciousnessMode.Off -> uiPhrase(language, UiPhrase.Off)
        SpaciousnessMode.StereoWidth -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_1
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_1
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_1
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_1
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_1
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_1
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_1
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_1
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_1
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_1
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_1
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_1
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_1
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_1
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_1
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_1
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_1
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_1
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_1
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_1
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_1
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_1
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_1
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_1
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_1
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_1
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_1
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_1
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_1
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_1
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_1
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_1
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_1
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_1
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_1
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_1
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_1
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_1
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_1
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_1
        }
        SpaciousnessMode.CrossfeedDepth -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_2
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_2
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_2
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_2
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_2
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_2
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_2
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_2
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_2
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_2
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_2
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_2
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_2
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_2
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_2
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_2
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_2
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_2
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_2
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_2
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_2
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_2
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_2
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_2
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_2
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_2
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_2
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_2
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_2
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_2
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_2
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_2
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_2
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_2
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_2
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_2
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_2
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_2
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_2
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_2
        }
        SpaciousnessMode.EarlyReflectionRoom -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_3
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_3
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_3
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_3
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_3
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_3
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_3
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_3
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_3
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_3
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_3
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_3
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_3
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_3
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_3
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_3
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_3
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_3
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_3
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_3
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_3
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_3
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_3
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_3
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_3
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_3
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_3
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_3
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_3
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_3
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_3
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_3
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_3
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_3
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_3
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_3
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_3
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_3
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_3
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_3
        }
        SpaciousnessMode.Philharmony -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_4
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_4
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_4
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_4
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_4
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_4
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_4
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_4
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_4
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_4
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_4
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_4
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_4
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_4
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_4
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_4
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_4
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_4
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_4
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_4
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_4
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_4
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_4
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_4
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_4
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_4
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_4
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_4
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_4
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_4
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_4
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_4
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_4
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_4
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_4
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_4
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_4
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_4
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_4
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_4
        }
        SpaciousnessMode.HaasSpace -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_5
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_5
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_5
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_5
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_5
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_5
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_5
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_5
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_5
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_5
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_5
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_5
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_5
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_5
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_5
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_5
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_5
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_5
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_5
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_5
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_5
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_5
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_5
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_5
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_5
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_5
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_5
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_5
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_5
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_5
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_5
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_5
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_5
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_5
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_5
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_5
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_5
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_5
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_5
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_5
        }
        SpaciousnessMode.HarmonicAir -> when (language) {
            AppLanguage.Bulgarian -> BulgarianTranslations.phrase_displayLabel_6
            AppLanguage.Catalan -> CatalanTranslations.phrase_displayLabel_6
            AppLanguage.Finnish -> FinnishTranslations.phrase_displayLabel_6
            AppLanguage.Romanian -> RomanianTranslations.phrase_displayLabel_6
            AppLanguage.Slovenian -> SlovenianTranslations.phrase_displayLabel_6
            AppLanguage.Bosnian -> BosnianTranslations.phrase_displayLabel_6
            AppLanguage.Indonesian -> IndonesianTranslations.phrase_displayLabel_6
            AppLanguage.Punjabi -> PunjabiTranslations.phrase_displayLabel_6
            AppLanguage.Albanian -> AlbanianTranslations.phrase_displayLabel_6
            AppLanguage.Polish -> PolishTranslations.phrase_displayLabel_6
            AppLanguage.Hindi -> HindiTranslations.phrase_displayLabel_6
            AppLanguage.Hungarian -> HungarianTranslations.phrase_displayLabel_6
            AppLanguage.German -> GermanTranslations.phrase_displayLabel_6
            AppLanguage.French -> FrenchTranslations.phrase_displayLabel_6
            AppLanguage.Spanish -> SpanishTranslations.phrase_displayLabel_6
            AppLanguage.Italian -> ItalianTranslations.phrase_displayLabel_6
            AppLanguage.Portuguese -> PortugueseTranslations.phrase_displayLabel_6
            AppLanguage.Dutch -> DutchTranslations.phrase_displayLabel_6
            AppLanguage.Swedish -> SwedishTranslations.phrase_displayLabel_6
            AppLanguage.Norwegian -> NorwegianTranslations.phrase_displayLabel_6
            AppLanguage.Danish -> DanishTranslations.phrase_displayLabel_6
            AppLanguage.Czech -> CzechTranslations.phrase_displayLabel_6
            AppLanguage.Croatian -> CroatianTranslations.phrase_displayLabel_6
            AppLanguage.Lithuanian -> LithuanianTranslations.phrase_displayLabel_6
            AppLanguage.Latvian -> LatvianTranslations.phrase_displayLabel_6
            AppLanguage.Estonian -> EstonianTranslations.phrase_displayLabel_6
            AppLanguage.Greek -> GreekTranslations.phrase_displayLabel_6
            AppLanguage.Macedonian -> MacedonianTranslations.phrase_displayLabel_6
            AppLanguage.Russian -> RussianTranslations.phrase_displayLabel_6
            AppLanguage.Serbian -> SerbianTranslations.phrase_displayLabel_6
            AppLanguage.Thai -> ThaiTranslations.phrase_displayLabel_6
            AppLanguage.Ukrainian -> UkrainianTranslations.phrase_displayLabel_6
            AppLanguage.Slovak -> SlovakTranslations.phrase_displayLabel_6
            AppLanguage.Korean -> KoreanTranslations.phrase_displayLabel_6
            AppLanguage.Malay -> MalayTranslations.phrase_displayLabel_6
            AppLanguage.Bengali -> BengaliTranslations.phrase_displayLabel_6
            AppLanguage.Urdu -> UrduTranslations.phrase_displayLabel_6
            AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.phrase_displayLabel_6
            AppLanguage.Japanese -> JapaneseTranslations.phrase_displayLabel_6
            AppLanguage.English -> EnglishTranslations.phrase_displayLabel_6
        }
    }
}
