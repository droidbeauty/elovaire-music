package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class CarMediaCopy(val openPhone: String, val grantAccess: String, val addMusic: String)

internal fun carMediaCopy(language: AppLanguage): CarMediaCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Bengali -> BengaliTranslations.carMedia_carMediaCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Catalan -> CatalanTranslations.carMedia_carMediaCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.carMedia_carMediaCopy_1
    AppLanguage.Croatian -> CroatianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Czech -> CzechTranslations.carMedia_carMediaCopy_1
    AppLanguage.Danish -> DanishTranslations.carMedia_carMediaCopy_1
    AppLanguage.Dutch -> DutchTranslations.carMedia_carMediaCopy_1
    AppLanguage.English -> EnglishTranslations.carMedia_carMediaCopy_1
    AppLanguage.Estonian -> EstonianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Finnish -> FinnishTranslations.carMedia_carMediaCopy_1
    AppLanguage.French -> FrenchTranslations.carMedia_carMediaCopy_1
    AppLanguage.German -> GermanTranslations.carMedia_carMediaCopy_1
    AppLanguage.Greek -> GreekTranslations.carMedia_carMediaCopy_1
    AppLanguage.Hindi -> HindiTranslations.carMedia_carMediaCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Italian -> ItalianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.carMedia_carMediaCopy_1
    AppLanguage.Korean -> KoreanTranslations.carMedia_carMediaCopy_1
    AppLanguage.Latvian -> LatvianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Malay -> MalayTranslations.carMedia_carMediaCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Polish -> PolishTranslations.carMedia_carMediaCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.carMedia_carMediaCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.carMedia_carMediaCopy_1
    AppLanguage.Romanian -> RomanianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Russian -> RussianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Serbian -> SerbianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Slovak -> SlovakTranslations.carMedia_carMediaCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Spanish -> SpanishTranslations.carMedia_carMediaCopy_1
    AppLanguage.Swedish -> SwedishTranslations.carMedia_carMediaCopy_1
    AppLanguage.Thai -> ThaiTranslations.carMedia_carMediaCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.carMedia_carMediaCopy_1
    AppLanguage.Urdu -> UrduTranslations.carMedia_carMediaCopy_1
}
