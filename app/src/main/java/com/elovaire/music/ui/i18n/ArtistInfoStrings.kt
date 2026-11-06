package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun closeArtistInfoLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Bengali -> BengaliTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Catalan -> CatalanTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Croatian -> CroatianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Czech -> CzechTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Danish -> DanishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Dutch -> DutchTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.English -> EnglishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Estonian -> EstonianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Finnish -> FinnishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.French -> FrenchTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.German -> GermanTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Greek -> GreekTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Hindi -> HindiTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Italian -> ItalianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Korean -> KoreanTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Latvian -> LatvianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Malay -> MalayTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Polish -> PolishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Romanian -> RomanianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Russian -> RussianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Serbian -> SerbianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Slovak -> SlovakTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Spanish -> SpanishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Swedish -> SwedishTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Thai -> ThaiTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.artistInfo_closeArtistInfoLabel_1
    AppLanguage.Urdu -> UrduTranslations.artistInfo_closeArtistInfoLabel_1
}

internal data class ArtistInfoCopy(
    val buttonDescription: String,
    val discography: String,
    val loading: String,
    val unavailable: String,
    val releaseDate: String,
)

internal fun artistInfoCopy(language: AppLanguage): ArtistInfoCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Catalan -> CatalanTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Finnish -> FinnishTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Romanian -> RomanianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Bengali -> BengaliTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Croatian -> CroatianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Czech -> CzechTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Danish -> DanishTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Dutch -> DutchTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Estonian -> EstonianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.French -> FrenchTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.German -> GermanTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Greek -> GreekTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Hindi -> HindiTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Italian -> ItalianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Korean -> KoreanTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Latvian -> LatvianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Malay -> MalayTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Polish -> PolishTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Russian -> RussianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Serbian -> SerbianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Slovak -> SlovakTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Spanish -> SpanishTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Swedish -> SwedishTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Thai -> ThaiTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.Urdu -> UrduTranslations.artistInfo_artistInfoCopy_1
    AppLanguage.English -> EnglishTranslations.artistInfo_artistInfoCopy_1
}
