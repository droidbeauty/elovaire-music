package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class SmartPlaylistOrderCopy(val sort: String, val direction: String, val ascending: String, val descending: String)

internal fun smartPlaylistOrderCopy(language: AppLanguage): SmartPlaylistOrderCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistRule_smartPlaylistOrderCopy_1
}

internal data class SmartPlaylistInputCopy(val count: String, val extension: String, val folderText: String, val limit: String)

internal fun smartPlaylistInputCopy(language: AppLanguage): SmartPlaylistInputCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistRule_smartPlaylistInputCopy_1
}

internal data class SmartPlaylistValueCopy(val value: String, val yes: String, val no: String, val seconds: String)

internal fun smartPlaylistValueCopy(language: AppLanguage): SmartPlaylistValueCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistRule_smartPlaylistValueCopy_1
}

internal data class SmartPlaylistOtherRuleCopy(val favorite: String, val duration: String, val playCount: String, val format: String)

internal fun smartPlaylistOtherRuleCopy(language: AppLanguage): SmartPlaylistOtherRuleCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistRule_smartPlaylistOtherRuleCopy_1
}

internal data class SmartPlaylistTextRuleCopy(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val folder: String,
)

internal fun smartPlaylistTextRuleCopy(language: AppLanguage): SmartPlaylistTextRuleCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistRule_smartPlaylistTextRuleCopy_1
}
