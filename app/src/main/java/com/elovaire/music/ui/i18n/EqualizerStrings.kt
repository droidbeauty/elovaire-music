package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun preampLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.equalizer_preampLabel_1
    AppLanguage.Bengali -> BengaliTranslations.equalizer_preampLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.equalizer_preampLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.equalizer_preampLabel_1
    AppLanguage.Catalan -> CatalanTranslations.equalizer_preampLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.equalizer_preampLabel_1
    AppLanguage.Croatian -> CroatianTranslations.equalizer_preampLabel_1
    AppLanguage.Czech -> CzechTranslations.equalizer_preampLabel_1
    AppLanguage.Danish -> DanishTranslations.equalizer_preampLabel_1
    AppLanguage.Dutch -> DutchTranslations.equalizer_preampLabel_1
    AppLanguage.English -> EnglishTranslations.equalizer_preampLabel_1
    AppLanguage.Estonian -> EstonianTranslations.equalizer_preampLabel_1
    AppLanguage.Finnish -> FinnishTranslations.equalizer_preampLabel_1
    AppLanguage.French -> FrenchTranslations.equalizer_preampLabel_1
    AppLanguage.German -> GermanTranslations.equalizer_preampLabel_1
    AppLanguage.Greek -> GreekTranslations.equalizer_preampLabel_1
    AppLanguage.Hindi -> HindiTranslations.equalizer_preampLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.equalizer_preampLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.equalizer_preampLabel_1
    AppLanguage.Italian -> ItalianTranslations.equalizer_preampLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.equalizer_preampLabel_1
    AppLanguage.Korean -> KoreanTranslations.equalizer_preampLabel_1
    AppLanguage.Latvian -> LatvianTranslations.equalizer_preampLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.equalizer_preampLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.equalizer_preampLabel_1
    AppLanguage.Malay -> MalayTranslations.equalizer_preampLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.equalizer_preampLabel_1
    AppLanguage.Polish -> PolishTranslations.equalizer_preampLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.equalizer_preampLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.equalizer_preampLabel_1
    AppLanguage.Romanian -> RomanianTranslations.equalizer_preampLabel_1
    AppLanguage.Russian -> RussianTranslations.equalizer_preampLabel_1
    AppLanguage.Serbian -> SerbianTranslations.equalizer_preampLabel_1
    AppLanguage.Slovak -> SlovakTranslations.equalizer_preampLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.equalizer_preampLabel_1
    AppLanguage.Spanish -> SpanishTranslations.equalizer_preampLabel_1
    AppLanguage.Swedish -> SwedishTranslations.equalizer_preampLabel_1
    AppLanguage.Thai -> ThaiTranslations.equalizer_preampLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.equalizer_preampLabel_1
    AppLanguage.Urdu -> UrduTranslations.equalizer_preampLabel_1
}

internal data class EqualizerCopy(
    val presets: String,
    val savePreset: String,
    val presetName: String,
    val customPresets: String,
    val replace: String,
    val replaceWarning: String,
)

internal fun equalizerCopy(language: AppLanguage): EqualizerCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Catalan -> CatalanTranslations.equalizer_equalizerCopy_1
    AppLanguage.Finnish -> FinnishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Romanian -> RomanianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.equalizer_equalizerCopy_1
    AppLanguage.English -> EnglishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Polish -> PolishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.equalizer_equalizerCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.equalizer_equalizerCopy_1
    AppLanguage.Croatian -> CroatianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Czech -> CzechTranslations.equalizer_equalizerCopy_1
    AppLanguage.Danish -> DanishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Dutch -> DutchTranslations.equalizer_equalizerCopy_1
    AppLanguage.Bengali -> BengaliTranslations.equalizer_equalizerCopy_1
    AppLanguage.Estonian -> EstonianTranslations.equalizer_equalizerCopy_1
    AppLanguage.French -> FrenchTranslations.equalizer_equalizerCopy_1
    AppLanguage.German -> GermanTranslations.equalizer_equalizerCopy_1
    AppLanguage.Greek -> GreekTranslations.equalizer_equalizerCopy_1
    AppLanguage.Hindi -> HindiTranslations.equalizer_equalizerCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Italian -> ItalianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.equalizer_equalizerCopy_1
    AppLanguage.Korean -> KoreanTranslations.equalizer_equalizerCopy_1
    AppLanguage.Latvian -> LatvianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Malay -> MalayTranslations.equalizer_equalizerCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.equalizer_equalizerCopy_1
    AppLanguage.Russian -> RussianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Slovak -> SlovakTranslations.equalizer_equalizerCopy_1
    AppLanguage.Serbian -> SerbianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Spanish -> SpanishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Swedish -> SwedishTranslations.equalizer_equalizerCopy_1
    AppLanguage.Thai -> ThaiTranslations.equalizer_equalizerCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.equalizer_equalizerCopy_1
    AppLanguage.Urdu -> UrduTranslations.equalizer_equalizerCopy_1
}
