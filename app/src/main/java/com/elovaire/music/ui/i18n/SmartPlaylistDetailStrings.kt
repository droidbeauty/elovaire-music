package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class SmartPlaylistActionCopy(val shuffle: String, val delete: String)

internal fun smartPlaylistActionCopy(language: AppLanguage): SmartPlaylistActionCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistActionCopy_1
}

internal data class SmartPlaylistMatchCopy(val mode: String, val all: String, val any: String, val add: String)

internal fun smartPlaylistMatchCopy(language: AppLanguage): SmartPlaylistMatchCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistMatchCopy_1
}

internal fun smartPlaylistRemoveRuleLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistRemoveRuleLabel_1
}

internal data class SmartPlaylistEditorHeadingCopy(val create: String, val edit: String, val save: String)

internal fun smartPlaylistEditorHeadingCopy(language: AppLanguage): SmartPlaylistEditorHeadingCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistEditorHeadingCopy_1
}

internal fun smartPlaylistEditRulesLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistEditRulesLabel_1
}

internal fun smartPlaylistPreviewLabel(language: AppLanguage, count: Int): String {
    val preview = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
        AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistPreviewLabel_1
    }
    return "$preview: ${localizedCountLabel(count, "song", language)}"
}

internal data class SmartPlaylistEmptyCopy(val title: String, val message: String)

internal data class SmartPlaylistMissingCopy(val title: String, val message: String)

internal fun smartPlaylistMissingCopy(language: AppLanguage): SmartPlaylistMissingCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistMissingCopy_1
}

internal data class SmartPlaylistDeleteCopy(val title: String, val message: String)

internal fun smartPlaylistDeleteCopy(language: AppLanguage): SmartPlaylistDeleteCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistDeleteCopy_1
}

internal fun smartPlaylistEmptyCopy(language: AppLanguage): SmartPlaylistEmptyCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Bengali -> BengaliTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Catalan -> CatalanTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Croatian -> CroatianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Czech -> CzechTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Danish -> DanishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Dutch -> DutchTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.English -> EnglishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Estonian -> EstonianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Finnish -> FinnishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.French -> FrenchTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.German -> GermanTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Greek -> GreekTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Hindi -> HindiTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Italian -> ItalianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Korean -> KoreanTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Latvian -> LatvianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Malay -> MalayTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Polish -> PolishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Romanian -> RomanianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Russian -> RussianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Serbian -> SerbianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Slovak -> SlovakTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Spanish -> SpanishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Swedish -> SwedishTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Thai -> ThaiTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
    AppLanguage.Urdu -> UrduTranslations.smartPlaylistDetail_smartPlaylistEmptyCopy_1
}
