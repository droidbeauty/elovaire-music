package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun audiobookPartsCount(count: Int, language: AppLanguage): String {
    val lastTwo = count % 100
    val lastDigit = count % 10
    val slavicOne = lastDigit == 1 && lastTwo != 11
    val slavicFew = lastDigit in 2..4 && lastTwo !in 12..14
    val part = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Bengali -> return BengaliTranslations.audiobook_audiobookPartsCount_dynamic_1(count)
        AppLanguage.Bosnian, AppLanguage.Croatian -> when { slavicOne -> "dio"; slavicFew -> "dijela"; else -> "dijelova" }
        AppLanguage.Bulgarian -> if (count == 1) BulgarianTranslations.audiobook_audiobookPartsCount_variant_1 else BulgarianTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Catalan -> if (count == 1) CatalanTranslations.audiobook_audiobookPartsCount_variant_1 else CatalanTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.ChineseSimplified -> return ChineseSimplifiedTranslations.audiobook_audiobookPartsCount_dynamic_1(count)
        AppLanguage.Czech -> when (count) { 1 -> CzechTranslations.audiobook_audiobookPartsCount_variant_1; in 2..4 -> CzechTranslations.audiobook_audiobookPartsCount_variant_2; else -> CzechTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Danish -> if (count == 1) DanishTranslations.audiobook_audiobookPartsCount_variant_1 else DanishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Dutch -> if (count == 1) DutchTranslations.audiobook_audiobookPartsCount_variant_1 else DutchTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.English -> if (count == 1) EnglishTranslations.audiobook_audiobookPartsCount_variant_1 else EnglishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Estonian -> EstonianTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Finnish -> if (count == 1) FinnishTranslations.audiobook_audiobookPartsCount_variant_1 else FinnishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.French -> if (count == 1) FrenchTranslations.audiobook_audiobookPartsCount_variant_1 else FrenchTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.German -> if (count == 1) GermanTranslations.audiobook_audiobookPartsCount_variant_1 else GermanTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Greek -> if (count == 1) GreekTranslations.audiobook_audiobookPartsCount_variant_1 else GreekTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Hindi -> HindiTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Hungarian -> HungarianTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Indonesian -> IndonesianTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Italian -> if (count == 1) ItalianTranslations.audiobook_audiobookPartsCount_variant_1 else ItalianTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Japanese -> return JapaneseTranslations.audiobook_audiobookPartsCount_dynamic_1(count)
        AppLanguage.Korean -> return KoreanTranslations.audiobook_audiobookPartsCount_dynamic_1(count)
        AppLanguage.Latvian -> when { lastTwo in 11..19 || lastDigit == 0 -> LatvianTranslations.audiobook_audiobookPartsCount_variant_1; lastDigit == 1 -> LatvianTranslations.audiobook_audiobookPartsCount_variant_2; else -> LatvianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Lithuanian -> when { lastTwo in 11..19 || lastDigit == 0 -> LithuanianTranslations.audiobook_audiobookPartsCount_variant_1; lastDigit == 1 -> LithuanianTranslations.audiobook_audiobookPartsCount_variant_2; else -> LithuanianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Macedonian -> if (count == 1) MacedonianTranslations.audiobook_audiobookPartsCount_variant_1 else MacedonianTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Malay -> MalayTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Norwegian -> if (count == 1) NorwegianTranslations.audiobook_audiobookPartsCount_variant_1 else NorwegianTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Polish -> if (count == 1) PolishTranslations.audiobook_audiobookPartsCount_variant_1 else PolishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Portuguese -> if (count == 1) PortugueseTranslations.audiobook_audiobookPartsCount_variant_1 else PortugueseTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Punjabi -> PunjabiTranslations.audiobook_audiobookPartsCount_1
        AppLanguage.Romanian -> when { count == 1 -> RomanianTranslations.audiobook_audiobookPartsCount_variant_1; lastTwo == 0 || lastTwo >= 20 -> RomanianTranslations.audiobook_audiobookPartsCount_variant_2; else -> RomanianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Russian -> when { slavicOne -> RussianTranslations.audiobook_audiobookPartsCount_variant_1; slavicFew -> RussianTranslations.audiobook_audiobookPartsCount_variant_2; else -> RussianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Serbian -> when { slavicOne -> SerbianTranslations.audiobook_audiobookPartsCount_variant_1; slavicFew -> SerbianTranslations.audiobook_audiobookPartsCount_variant_2; else -> SerbianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Slovak -> when (count) { 1 -> SlovakTranslations.audiobook_audiobookPartsCount_variant_1; in 2..4 -> SlovakTranslations.audiobook_audiobookPartsCount_variant_2; else -> SlovakTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Slovenian -> when (lastTwo) { 1 -> SlovenianTranslations.audiobook_audiobookPartsCount_variant_1; 2 -> SlovenianTranslations.audiobook_audiobookPartsCount_variant_2; 3, 4 -> SlovenianTranslations.audiobook_audiobookPartsCount_variant_3; else -> SlovenianTranslations.audiobook_audiobookPartsCount_variant_4 }
        AppLanguage.Spanish -> if (count == 1) SpanishTranslations.audiobook_audiobookPartsCount_variant_1 else SpanishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Swedish -> if (count == 1) SwedishTranslations.audiobook_audiobookPartsCount_variant_1 else SwedishTranslations.audiobook_audiobookPartsCount_variant_2
        AppLanguage.Thai -> return ThaiTranslations.audiobook_audiobookPartsCount_dynamic_1(count)
        AppLanguage.Ukrainian -> when { slavicOne -> UkrainianTranslations.audiobook_audiobookPartsCount_variant_1; slavicFew -> UkrainianTranslations.audiobook_audiobookPartsCount_variant_2; else -> UkrainianTranslations.audiobook_audiobookPartsCount_variant_3 }
        AppLanguage.Urdu -> if (count == 1) UrduTranslations.audiobook_audiobookPartsCount_variant_1 else UrduTranslations.audiobook_audiobookPartsCount_variant_2
    }
    return "$count $part"
}

internal fun localizedAudiobookAuthor(author: String, language: AppLanguage): String {
    if (author.isNotBlank() && author != "Unknown Author" && author != "Unknown Artist") return author
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Bengali -> BengaliTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Bosnian -> BosnianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Bulgarian -> BulgarianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Catalan -> CatalanTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Croatian -> CroatianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Czech -> CzechTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Danish -> DanishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Dutch -> DutchTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.English -> EnglishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Estonian -> EstonianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Finnish -> FinnishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.French -> FrenchTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.German -> GermanTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Greek -> GreekTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Hindi -> HindiTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Hungarian -> HungarianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Indonesian -> IndonesianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Italian -> ItalianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Japanese -> JapaneseTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Korean -> KoreanTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Latvian -> LatvianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Lithuanian -> LithuanianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Macedonian -> MacedonianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Malay -> MalayTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Norwegian -> NorwegianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Polish -> PolishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Portuguese -> PortugueseTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Punjabi -> PunjabiTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Romanian -> RomanianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Russian -> RussianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Serbian -> SerbianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Slovak -> SlovakTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Slovenian -> SlovenianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Spanish -> SpanishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Swedish -> SwedishTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Thai -> ThaiTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Ukrainian -> UkrainianTranslations.audiobook_localizedAudiobookAuthor_1
        AppLanguage.Urdu -> UrduTranslations.audiobook_localizedAudiobookAuthor_1
    }
}

internal data class AudiobookUiCopy(
    val title: String,
    val continueListening: String,
    val allAudiobooks: String,
    val author: String,
    val resume: String,
    val play: String,
    val playAgain: String,
    val startOver: String,
    val chapters: String,
    val completed: String,
    val notStarted: String,
    val listening: String,
    val speed: String,
    val parts: String,
    val rewind15: String,
    val forward15: String,
    val editTags: String,
)

internal data class AudiobookDescriptionCopy(
    val about: String,
    val loading: String,
    val unavailable: String,
    val more: String,
    val close: String,
)

internal data class AudiobookUnavailableCopy(val title: String, val message: String, val retry: String)

internal fun audiobookUnavailableCopy(language: AppLanguage): AudiobookUnavailableCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Bengali -> BengaliTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Catalan -> CatalanTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Croatian -> CroatianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Czech -> CzechTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Danish -> DanishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Dutch -> DutchTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.English -> EnglishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Estonian -> EstonianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Finnish -> FinnishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.French -> FrenchTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.German -> GermanTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Greek -> GreekTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Hindi -> HindiTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Italian -> ItalianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Korean -> KoreanTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Latvian -> LatvianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Malay -> MalayTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Polish -> PolishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Romanian -> RomanianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Russian -> RussianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Serbian -> SerbianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Slovak -> SlovakTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Spanish -> SpanishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Swedish -> SwedishTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Thai -> ThaiTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.audiobook_audiobookUnavailableCopy_1
    AppLanguage.Urdu -> UrduTranslations.audiobook_audiobookUnavailableCopy_1
}

internal fun audiobookDescriptionCopy(language: AppLanguage): AudiobookDescriptionCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Catalan -> CatalanTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Finnish -> FinnishTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Romanian -> RomanianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Croatian -> CroatianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Czech -> CzechTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Danish -> DanishTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Dutch -> DutchTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Bengali -> BengaliTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Estonian -> EstonianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.French -> FrenchTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.German -> GermanTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Greek -> GreekTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Hindi -> HindiTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Italian -> ItalianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Korean -> KoreanTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Latvian -> LatvianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Malay -> MalayTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Polish -> PolishTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Russian -> RussianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Serbian -> SerbianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Slovak -> SlovakTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Spanish -> SpanishTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Swedish -> SwedishTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Thai -> ThaiTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.Urdu -> UrduTranslations.audiobook_audiobookDescriptionCopy_1
    AppLanguage.English -> EnglishTranslations.audiobook_audiobookDescriptionCopy_1
}

internal fun audiobookCopy(language: AppLanguage): AudiobookUiCopy {
    return when (language) {
        AppLanguage.Estonian -> EstonianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Greek -> GreekTranslations.audiobook_audiobookCopy_1
        AppLanguage.Hungarian -> HungarianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Latvian -> LatvianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Lithuanian -> LithuanianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Malay -> MalayTranslations.audiobook_audiobookCopy_1
        AppLanguage.Macedonian -> MacedonianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Thai -> ThaiTranslations.audiobook_audiobookCopy_1
        AppLanguage.Croatian -> CroatianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Bengali -> BengaliTranslations.audiobook_audiobookCopy_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobook_audiobookCopy_1
        AppLanguage.Hindi -> HindiTranslations.audiobook_audiobookCopy_1
        AppLanguage.Japanese -> JapaneseTranslations.audiobook_audiobookCopy_1
        AppLanguage.Korean -> KoreanTranslations.audiobook_audiobookCopy_1
        AppLanguage.Bulgarian -> BulgarianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Catalan -> CatalanTranslations.audiobook_audiobookCopy_1
        AppLanguage.Finnish -> FinnishTranslations.audiobook_audiobookCopy_1
        AppLanguage.Romanian -> RomanianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Slovenian -> SlovenianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Bosnian -> BosnianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Indonesian -> IndonesianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Punjabi -> PunjabiTranslations.audiobook_audiobookCopy_1
        AppLanguage.Albanian -> AlbanianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Serbian -> SerbianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Urdu -> UrduTranslations.audiobook_audiobookCopy_1
        AppLanguage.Polish -> PolishTranslations.audiobook_audiobookUiCopy_multiline_1
        AppLanguage.German -> GermanTranslations.audiobook_audiobookCopy_1
        AppLanguage.French -> FrenchTranslations.audiobook_audiobookCopy_1
        AppLanguage.Spanish -> SpanishTranslations.audiobook_audiobookCopy_1
        AppLanguage.Italian -> ItalianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Portuguese -> PortugueseTranslations.audiobook_audiobookCopy_1
        AppLanguage.Russian -> RussianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Ukrainian -> UkrainianTranslations.audiobook_audiobookCopy_1
        AppLanguage.Czech -> CzechTranslations.audiobook_audiobookCopy_1
        AppLanguage.Slovak -> SlovakTranslations.audiobook_audiobookCopy_1
        AppLanguage.Dutch -> DutchTranslations.audiobook_audiobookCopy_1
        AppLanguage.Swedish -> SwedishTranslations.audiobook_audiobookCopy_1
        AppLanguage.Danish -> DanishTranslations.audiobook_audiobookCopy_1
        AppLanguage.Norwegian -> NorwegianTranslations.audiobook_audiobookCopy_1
        AppLanguage.English -> EnglishTranslations.audiobook_audiobookCopy_1
    }
}

internal fun audiobookFinishedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Catalan -> CatalanTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Finnish -> FinnishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Romanian -> RomanianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Bengali -> BengaliTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Croatian -> CroatianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Czech -> CzechTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Danish -> DanishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Dutch -> DutchTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.English -> EnglishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Estonian -> EstonianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.French -> FrenchTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.German -> GermanTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Greek -> GreekTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Hindi -> HindiTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Italian -> ItalianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Korean -> KoreanTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Latvian -> LatvianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Malay -> MalayTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Polish -> PolishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Russian -> RussianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Serbian -> SerbianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Slovak -> SlovakTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Spanish -> SpanishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Swedish -> SwedishTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Thai -> ThaiTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.audiobook_audiobookFinishedLabel_1
    AppLanguage.Urdu -> UrduTranslations.audiobook_audiobookFinishedLabel_1
}
