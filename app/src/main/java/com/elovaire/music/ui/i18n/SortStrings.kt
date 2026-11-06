package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode

internal data class SortNames(val song: String, val artist: String, val album: String, val albumName: String)

private fun sortNames(language: AppLanguage): SortNames = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.sort_sortNames_1
    AppLanguage.Bosnian -> BosnianTranslations.sort_sortNames_1
    AppLanguage.Bulgarian -> BulgarianTranslations.sort_sortNames_1
    AppLanguage.Catalan -> CatalanTranslations.sort_sortNames_1
    AppLanguage.Finnish -> FinnishTranslations.sort_sortNames_1
    AppLanguage.Romanian -> RomanianTranslations.sort_sortNames_1
    AppLanguage.Slovenian -> SlovenianTranslations.sort_sortNames_1
    AppLanguage.Indonesian -> IndonesianTranslations.sort_sortNames_1
    AppLanguage.Punjabi -> PunjabiTranslations.sort_sortNames_1
    AppLanguage.Bengali -> BengaliTranslations.sort_sortNames_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.sort_sortNames_1
    AppLanguage.Croatian -> CroatianTranslations.sort_sortNames_1
    AppLanguage.Czech -> CzechTranslations.sort_sortNames_1
    AppLanguage.Danish -> DanishTranslations.sort_sortNames_1
    AppLanguage.Dutch -> DutchTranslations.sort_sortNames_1
    AppLanguage.English -> EnglishTranslations.sort_sortNames_1
    AppLanguage.Estonian -> EstonianTranslations.sort_sortNames_1
    AppLanguage.French -> FrenchTranslations.sort_sortNames_1
    AppLanguage.German -> GermanTranslations.sort_sortNames_1
    AppLanguage.Greek -> GreekTranslations.sort_sortNames_1
    AppLanguage.Hindi -> HindiTranslations.sort_sortNames_1
    AppLanguage.Hungarian -> HungarianTranslations.sort_sortNames_1
    AppLanguage.Italian -> ItalianTranslations.sort_sortNames_1
    AppLanguage.Japanese -> JapaneseTranslations.sort_sortNames_1
    AppLanguage.Korean -> KoreanTranslations.sort_sortNames_1
    AppLanguage.Latvian -> LatvianTranslations.sort_sortNames_1
    AppLanguage.Lithuanian -> LithuanianTranslations.sort_sortNames_1
    AppLanguage.Macedonian -> MacedonianTranslations.sort_sortNames_1
    AppLanguage.Malay -> MalayTranslations.sort_sortNames_1
    AppLanguage.Norwegian -> NorwegianTranslations.sort_sortNames_1
    AppLanguage.Polish -> PolishTranslations.sort_sortNames_1
    AppLanguage.Portuguese -> PortugueseTranslations.sort_sortNames_1
    AppLanguage.Russian -> RussianTranslations.sort_sortNames_1
    AppLanguage.Serbian -> SerbianTranslations.sort_sortNames_1
    AppLanguage.Slovak -> SlovakTranslations.sort_sortNames_1
    AppLanguage.Spanish -> SpanishTranslations.sort_sortNames_1
    AppLanguage.Swedish -> SwedishTranslations.sort_sortNames_1
    AppLanguage.Thai -> ThaiTranslations.sort_sortNames_1
    AppLanguage.Ukrainian -> UkrainianTranslations.sort_sortNames_1
    AppLanguage.Urdu -> UrduTranslations.sort_sortNames_1
}

internal fun songSortLabel(language: AppLanguage, mode: SongSortMode): String = sortNames(language).let {
    when (mode) {
        SongSortMode.Title -> it.song
        SongSortMode.Artist -> it.artist
        SongSortMode.Album -> it.album
    }
}

internal fun albumSortLabel(language: AppLanguage, mode: AlbumSortMode): String = sortNames(language).let {
    when (mode) {
        AlbumSortMode.Artist -> it.artist
        AlbumSortMode.Album -> it.albumName
    }
}
