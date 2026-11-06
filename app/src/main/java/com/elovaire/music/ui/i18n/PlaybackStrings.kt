package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal fun pauseLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playback_pauseLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_pauseLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_pauseLabel_1
    AppLanguage.Catalan -> CatalanTranslations.playback_pauseLabel_1
    AppLanguage.Finnish -> FinnishTranslations.playback_pauseLabel_1
    AppLanguage.Romanian -> RomanianTranslations.playback_pauseLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_pauseLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_pauseLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_pauseLabel_1
    AppLanguage.English -> EnglishTranslations.playback_pauseLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_pauseLabel_1
    AppLanguage.Croatian -> CroatianTranslations.playback_pauseLabel_1
    AppLanguage.Czech -> CzechTranslations.playback_pauseLabel_1
    AppLanguage.Danish -> DanishTranslations.playback_pauseLabel_1
    AppLanguage.Dutch -> DutchTranslations.playback_pauseLabel_1
    AppLanguage.Bengali -> BengaliTranslations.playback_pauseLabel_1
    AppLanguage.Estonian -> EstonianTranslations.playback_pauseLabel_1
    AppLanguage.French -> FrenchTranslations.playback_pauseLabel_1
    AppLanguage.German -> GermanTranslations.playback_pauseLabel_1
    AppLanguage.Greek -> GreekTranslations.playback_pauseLabel_1
    AppLanguage.Hindi -> HindiTranslations.playback_pauseLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_pauseLabel_1
    AppLanguage.Italian -> ItalianTranslations.playback_pauseLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_pauseLabel_1
    AppLanguage.Korean -> KoreanTranslations.playback_pauseLabel_1
    AppLanguage.Latvian -> LatvianTranslations.playback_pauseLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_pauseLabel_1
    AppLanguage.Malay -> MalayTranslations.playback_pauseLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_pauseLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_pauseLabel_1
    AppLanguage.Polish -> PolishTranslations.playback_pauseLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_pauseLabel_1
    AppLanguage.Russian -> RussianTranslations.playback_pauseLabel_1
    AppLanguage.Slovak -> SlovakTranslations.playback_pauseLabel_1
    AppLanguage.Serbian -> SerbianTranslations.playback_pauseLabel_1
    AppLanguage.Spanish -> SpanishTranslations.playback_pauseLabel_1
    AppLanguage.Swedish -> SwedishTranslations.playback_pauseLabel_1
    AppLanguage.Thai -> ThaiTranslations.playback_pauseLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_pauseLabel_1
    AppLanguage.Urdu -> UrduTranslations.playback_pauseLabel_1
}

internal fun playingFromPrefix(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_playingFromPrefix_1
    AppLanguage.Catalan -> CatalanTranslations.playback_playingFromPrefix_1
    AppLanguage.Finnish -> FinnishTranslations.playback_playingFromPrefix_1
    AppLanguage.Romanian -> RomanianTranslations.playback_playingFromPrefix_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_playingFromPrefix_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_playingFromPrefix_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_playingFromPrefix_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_playingFromPrefix_1
    AppLanguage.Polish -> PolishTranslations.playback_playingFromPrefix_1
    AppLanguage.Slovak -> SlovakTranslations.playback_playingFromPrefix_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_playingFromPrefix_1
    AppLanguage.Korean -> KoreanTranslations.playback_playingFromPrefix_1
    AppLanguage.Czech -> CzechTranslations.playback_playingFromPrefix_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_playingFromPrefix_1
    AppLanguage.Danish -> DanishTranslations.playback_playingFromPrefix_1
    AppLanguage.French -> FrenchTranslations.playback_playingFromPrefix_1
    AppLanguage.German -> GermanTranslations.playback_playingFromPrefix_1
    AppLanguage.Dutch -> DutchTranslations.playback_playingFromPrefix_1
    AppLanguage.Malay -> MalayTranslations.playback_playingFromPrefix_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_playingFromPrefix_1
    AppLanguage.Swedish -> SwedishTranslations.playback_playingFromPrefix_1
    AppLanguage.Spanish -> SpanishTranslations.playback_playingFromPrefix_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_playingFromPrefix_1
    AppLanguage.Estonian -> EstonianTranslations.playback_playingFromPrefix_1
    AppLanguage.Bengali -> BengaliTranslations.playback_playingFromPrefix_1
    AppLanguage.Greek -> GreekTranslations.playback_playingFromPrefix_1
    AppLanguage.Croatian -> CroatianTranslations.playback_playingFromPrefix_1
    AppLanguage.Russian -> RussianTranslations.playback_playingFromPrefix_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_playingFromPrefix_1
    AppLanguage.Urdu -> UrduTranslations.playback_playingFromPrefix_1
    AppLanguage.Latvian -> LatvianTranslations.playback_playingFromPrefix_1
    AppLanguage.Italian -> ItalianTranslations.playback_playingFromPrefix_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_playingFromPrefix_1
    AppLanguage.Hindi -> HindiTranslations.playback_playingFromPrefix_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_playingFromPrefix_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_playingFromPrefix_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_playingFromPrefix_1
    AppLanguage.Serbian -> SerbianTranslations.playback_playingFromPrefix_1
    AppLanguage.Thai -> ThaiTranslations.playback_playingFromPrefix_1
    AppLanguage.English -> EnglishTranslations.playback_playingFromPrefix_1
}

internal fun localizedAllSongsSource(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Bengali -> BengaliTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Catalan -> CatalanTranslations.playback_localizedAllSongsSource_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Croatian -> CroatianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Czech -> CzechTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Danish -> DanishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Dutch -> DutchTranslations.playback_localizedAllSongsSource_1
    AppLanguage.English -> EnglishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Estonian -> EstonianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Finnish -> FinnishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.French -> FrenchTranslations.playback_localizedAllSongsSource_1
    AppLanguage.German -> GermanTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Greek -> GreekTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Hindi -> HindiTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Italian -> ItalianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Korean -> KoreanTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Latvian -> LatvianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Malay -> MalayTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Polish -> PolishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Romanian -> RomanianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Russian -> RussianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Serbian -> SerbianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Slovak -> SlovakTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Spanish -> SpanishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Swedish -> SwedishTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Thai -> ThaiTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_localizedAllSongsSource_1
    AppLanguage.Urdu -> UrduTranslations.playback_localizedAllSongsSource_1
}

internal fun localizedSearchResultsSource(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Bengali -> BengaliTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Catalan -> CatalanTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Croatian -> CroatianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Czech -> CzechTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Danish -> DanishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Dutch -> DutchTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.English -> EnglishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Estonian -> EstonianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Finnish -> FinnishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.French -> FrenchTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.German -> GermanTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Greek -> GreekTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Hindi -> HindiTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Italian -> ItalianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Korean -> KoreanTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Latvian -> LatvianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Malay -> MalayTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Polish -> PolishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Romanian -> RomanianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Russian -> RussianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Serbian -> SerbianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Slovak -> SlovakTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Spanish -> SpanishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Swedish -> SwedishTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Thai -> ThaiTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_localizedSearchResultsSource_1
    AppLanguage.Urdu -> UrduTranslations.playback_localizedSearchResultsSource_1
}

internal fun playingFromText(source: String, language: AppLanguage): String = when (language) {
    AppLanguage.Bengali -> BengaliTranslations.playback_playingFromText_dynamic_1(source)
    AppLanguage.Hindi -> HindiTranslations.playback_playingFromText_dynamic_1(source)
    AppLanguage.Punjabi -> PunjabiTranslations.playback_playingFromText_dynamic_1(source)
    AppLanguage.Urdu -> UrduTranslations.playback_playingFromText_dynamic_1(source)
    else -> "${playingFromPrefix(language)} $source"
}

@Suppress("CyclomaticComplexMethod")
internal fun builtInSmartPlaylistTitle(type: BuiltInSmartPlaylistType, language: AppLanguage): String = when (type) {
    BuiltInSmartPlaylistType.RecentlyAdded -> miscPhrase(language, MiscPhrase.RecentlyAdded)
    BuiltInSmartPlaylistType.MostPlayed -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Catalan -> CatalanTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Finnish -> FinnishTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Romanian -> RomanianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Slovenian -> SlovenianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Bosnian -> BosnianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Indonesian -> IndonesianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Punjabi -> PunjabiTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Albanian -> AlbanianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Bengali -> BengaliTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Croatian -> CroatianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Czech -> CzechTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Danish -> DanishTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Dutch -> DutchTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Estonian -> EstonianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Polish -> PolishTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.German -> GermanTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.French -> FrenchTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Spanish -> SpanishTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Portuguese -> PortugueseTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Italian -> ItalianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Russian -> RussianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Japanese -> JapaneseTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Korean -> KoreanTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Thai -> ThaiTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Greek -> GreekTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Hindi -> HindiTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Hungarian -> HungarianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Latvian -> LatvianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Lithuanian -> LithuanianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Malay -> MalayTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Macedonian -> MacedonianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Norwegian -> NorwegianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Serbian -> SerbianTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Slovak -> SlovakTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Swedish -> SwedishTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.Urdu -> UrduTranslations.playback_builtInSmartPlaylistTitle_1
        AppLanguage.English -> EnglishTranslations.playback_builtInSmartPlaylistTitle_1
    }
    BuiltInSmartPlaylistType.Favorites -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Catalan -> CatalanTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Finnish -> FinnishTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Romanian -> RomanianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Slovenian -> SlovenianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Bosnian -> BosnianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Indonesian -> IndonesianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Punjabi -> PunjabiTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Albanian -> AlbanianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Bengali -> BengaliTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Croatian -> CroatianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Czech -> CzechTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Danish -> DanishTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Dutch -> DutchTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Estonian -> EstonianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Polish -> PolishTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.German -> GermanTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.French -> FrenchTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Spanish -> SpanishTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Portuguese -> PortugueseTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Italian -> ItalianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Russian -> RussianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Japanese -> JapaneseTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Korean -> KoreanTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Thai -> ThaiTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Greek -> GreekTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Hindi -> HindiTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Hungarian -> HungarianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Latvian -> LatvianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Lithuanian -> LithuanianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Malay -> MalayTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Macedonian -> MacedonianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Norwegian -> NorwegianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Serbian -> SerbianTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Slovak -> SlovakTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Swedish -> SwedishTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.Urdu -> UrduTranslations.playback_builtInSmartPlaylistTitle_2
        AppLanguage.English -> EnglishTranslations.playback_builtInSmartPlaylistTitle_2
    }
}

internal fun artistTopTracksSubtitle(count: Int, language: AppLanguage): String {
    val tracks = localizedCountLabel(count, "track", language)
    val slavicSingular = count % 10 == 1 && count % 100 != 11
    val suffix = when (language) {
        AppLanguage.Bulgarian -> return BulgarianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Catalan -> CatalanTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Finnish -> return FinnishTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Romanian -> if (count == 1) RomanianTranslations.playback_artistTopTracksSubtitle_variant_1 else RomanianTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.Slovenian -> return SlovenianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Bosnian -> return BosnianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Indonesian -> IndonesianTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Punjabi -> PunjabiTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Albanian -> AlbanianTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Bengali -> if (count == 1) BengaliTranslations.playback_artistTopTracksSubtitle_variant_1 else BengaliTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.Croatian -> return CroatianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Czech -> return CzechTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Danish -> DanishTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Dutch -> DutchTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Estonian -> EstonianTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Polish -> return PolishTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.German -> return GermanTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.French -> FrenchTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Spanish -> if (count == 1) SpanishTranslations.playback_artistTopTracksSubtitle_variant_1 else SpanishTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.Portuguese -> if (count == 1) PortugueseTranslations.playback_artistTopTracksSubtitle_variant_1 else PortugueseTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.Italian -> ItalianTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Russian -> return RussianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Ukrainian -> return UkrainianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Japanese -> return JapaneseTranslations.playback_artistTopTracksSubtitle_dynamic_1(tracks)
        AppLanguage.Korean -> return KoreanTranslations.playback_artistTopTracksSubtitle_dynamic_1(tracks)
        AppLanguage.ChineseSimplified -> return ChineseSimplifiedTranslations.playback_artistTopTracksSubtitle_dynamic_1(tracks)
        AppLanguage.Thai -> ThaiTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Greek -> return GreekTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Hindi -> if (count == 1) HindiTranslations.playback_artistTopTracksSubtitle_variant_1 else HindiTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.Hungarian -> return HungarianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Latvian -> return LatvianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Lithuanian -> return LithuanianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Malay -> MalayTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Macedonian -> return MacedonianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Norwegian -> NorwegianTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Serbian -> return SerbianTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, slavicSingular)
        AppLanguage.Slovak -> return SlovakTranslations.playback_artistTopTracksSubtitle_dynamic(tracks, count)
        AppLanguage.Swedish -> SwedishTranslations.playback_artistTopTracksSubtitle_1
        AppLanguage.Urdu -> if (count == 1) UrduTranslations.playback_artistTopTracksSubtitle_variant_1 else UrduTranslations.playback_artistTopTracksSubtitle_variant_2
        AppLanguage.English -> EnglishTranslations.playback_artistTopTracksSubtitle_1
    }
    return "$tracks $suffix"
}

internal fun privacyPolicySettingsSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Catalan -> CatalanTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Finnish -> FinnishTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Romanian -> RomanianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Bengali -> BengaliTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Croatian -> CroatianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Czech -> CzechTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Danish -> DanishTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Dutch -> DutchTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Estonian -> EstonianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Polish -> PolishTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.German -> GermanTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.French -> FrenchTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Spanish -> SpanishTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Italian -> ItalianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Russian -> RussianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Korean -> KoreanTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Thai -> ThaiTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Greek -> GreekTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Hindi -> HindiTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Latvian -> LatvianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Malay -> MalayTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Serbian -> SerbianTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Slovak -> SlovakTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Swedish -> SwedishTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.Urdu -> UrduTranslations.playback_privacyPolicySettingsSubtitle_1
    AppLanguage.English -> EnglishTranslations.playback_privacyPolicySettingsSubtitle_1
}

internal fun discLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_discLabel_1
    AppLanguage.Catalan -> CatalanTranslations.playback_discLabel_1
    AppLanguage.Finnish -> FinnishTranslations.playback_discLabel_1
    AppLanguage.Romanian -> RomanianTranslations.playback_discLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_discLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_discLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_discLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_discLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_discLabel_1
    AppLanguage.English -> EnglishTranslations.playback_discLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_discLabel_1
    AppLanguage.Croatian -> CroatianTranslations.playback_discLabel_1
    AppLanguage.Czech -> CzechTranslations.playback_discLabel_1
    AppLanguage.Danish -> DanishTranslations.playback_discLabel_1
    AppLanguage.Dutch -> DutchTranslations.playback_discLabel_1
    AppLanguage.Bengali -> BengaliTranslations.playback_discLabel_1
    AppLanguage.Estonian -> EstonianTranslations.playback_discLabel_1
    AppLanguage.French -> FrenchTranslations.playback_discLabel_1
    AppLanguage.German -> GermanTranslations.playback_discLabel_1
    AppLanguage.Greek -> GreekTranslations.playback_discLabel_1
    AppLanguage.Hindi -> HindiTranslations.playback_discLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_discLabel_1
    AppLanguage.Italian -> ItalianTranslations.playback_discLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_discLabel_1
    AppLanguage.Korean -> KoreanTranslations.playback_discLabel_1
    AppLanguage.Latvian -> LatvianTranslations.playback_discLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_discLabel_1
    AppLanguage.Malay -> MalayTranslations.playback_discLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_discLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_discLabel_1
    AppLanguage.Polish -> PolishTranslations.playback_discLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_discLabel_1
    AppLanguage.Russian -> RussianTranslations.playback_discLabel_1
    AppLanguage.Slovak -> SlovakTranslations.playback_discLabel_1
    AppLanguage.Serbian -> SerbianTranslations.playback_discLabel_1
    AppLanguage.Spanish -> SpanishTranslations.playback_discLabel_1
    AppLanguage.Swedish -> SwedishTranslations.playback_discLabel_1
    AppLanguage.Thai -> ThaiTranslations.playback_discLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_discLabel_1
    AppLanguage.Urdu -> UrduTranslations.playback_discLabel_1
}

@Suppress("CyclomaticComplexMethod")
internal fun equalizerCustomLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Bengali -> BengaliTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Catalan -> CatalanTranslations.playback_equalizerCustomLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Croatian -> CroatianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Czech -> CzechTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Danish -> DanishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Dutch -> DutchTranslations.playback_equalizerCustomLabel_1
    AppLanguage.English -> EnglishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Estonian -> EstonianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Finnish -> FinnishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.French -> FrenchTranslations.playback_equalizerCustomLabel_1
    AppLanguage.German -> GermanTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Greek -> GreekTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Hindi -> HindiTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Italian -> ItalianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Korean -> KoreanTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Latvian -> LatvianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Malay -> MalayTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Polish -> PolishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Romanian -> RomanianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Russian -> RussianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Serbian -> SerbianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Slovak -> SlovakTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Spanish -> SpanishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Swedish -> SwedishTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Thai -> ThaiTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_equalizerCustomLabel_1
    AppLanguage.Urdu -> UrduTranslations.playback_equalizerCustomLabel_1
}

internal fun localizedEqualizerPreset(name: String, language: AppLanguage): String {
    val index = when (name) {
        "Electronic" -> 0
        "Classical" -> 1
        "Acoustic" -> 2
        "Vocal" -> 3
        else -> return localizedEqualizerGenre(name, language)
    }
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Bengali -> BengaliTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Bosnian -> BosnianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Catalan -> CatalanTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Croatian -> CroatianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Czech -> CzechTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Danish -> DanishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Dutch -> DutchTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.English -> EnglishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Estonian -> EstonianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Finnish -> FinnishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.French -> FrenchTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.German -> GermanTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Greek -> GreekTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Hindi -> HindiTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Hungarian -> HungarianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Indonesian -> IndonesianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Italian -> ItalianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Japanese -> JapaneseTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Korean -> KoreanTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Latvian -> LatvianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Lithuanian -> LithuanianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Macedonian -> MacedonianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Malay -> MalayTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Norwegian -> NorwegianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Polish -> PolishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Portuguese -> PortugueseTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Punjabi -> PunjabiTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Romanian -> RomanianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Russian -> RussianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Serbian -> SerbianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Slovak -> SlovakTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Slovenian -> SlovenianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Spanish -> SpanishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Swedish -> SwedishTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Thai -> ThaiTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_localizedEqualizerPreset_1
        AppLanguage.Urdu -> UrduTranslations.playback_localizedEqualizerPreset_1
    }[index]
}

private fun localizedEqualizerGenre(name: String, language: AppLanguage): String {
    val index = when (name) {
        "Jazz" -> 0
        "Pop" -> 1
        "Rock" -> 2
        "Metal" -> 3
        "Soul" -> 4
        "Hip-Hop" -> 5
        else -> return name
    }
    val labels = when (language) {
        AppLanguage.Bengali -> BengaliTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Greek -> GreekTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Hindi -> HindiTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Japanese -> JapaneseTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Korean -> KoreanTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Macedonian -> MacedonianTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Punjabi -> PunjabiTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Russian -> RussianTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Serbian -> SerbianTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Thai -> ThaiTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_localizedEqualizerGenre_1
        AppLanguage.Urdu -> UrduTranslations.playback_localizedEqualizerGenre_1
        else -> return name
    }
    return labels[index]
}

@Suppress("CyclomaticComplexMethod")
internal fun equalizerStatusLabel(language: AppLanguage, presetName: String?): String = when {
    presetName != null -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Catalan -> CatalanTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Finnish -> FinnishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Romanian -> RomanianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Slovenian -> SlovenianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Bosnian -> BosnianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Indonesian -> IndonesianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Punjabi -> PunjabiTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Albanian -> AlbanianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.English -> EnglishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Croatian -> CroatianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Czech -> CzechTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Danish -> DanishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Dutch -> DutchTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Bengali -> BengaliTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Estonian -> EstonianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.French -> FrenchTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.German -> GermanTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Greek -> GreekTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Hindi -> HindiTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Hungarian -> HungarianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Italian -> ItalianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Japanese -> JapaneseTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Korean -> KoreanTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Latvian -> LatvianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Lithuanian -> LithuanianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Malay -> MalayTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Macedonian -> MacedonianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Norwegian -> NorwegianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Polish -> PolishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Portuguese -> PortugueseTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Russian -> RussianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Slovak -> SlovakTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Serbian -> SerbianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Spanish -> SpanishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Swedish -> SwedishTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Thai -> ThaiTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
        AppLanguage.Urdu -> UrduTranslations.playback_equalizerStatusLabel_dynamic_1(presetName)
    }
    else -> when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Catalan -> CatalanTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Finnish -> FinnishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Romanian -> RomanianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Slovenian -> SlovenianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Bosnian -> BosnianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Indonesian -> IndonesianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Punjabi -> PunjabiTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Albanian -> AlbanianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.English -> EnglishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Croatian -> CroatianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Czech -> CzechTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Danish -> DanishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Dutch -> DutchTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Bengali -> BengaliTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Estonian -> EstonianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.French -> FrenchTranslations.playback_equalizerStatusLabel_1
        AppLanguage.German -> GermanTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Greek -> GreekTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Hindi -> HindiTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Hungarian -> HungarianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Italian -> ItalianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Japanese -> JapaneseTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Korean -> KoreanTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Latvian -> LatvianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Lithuanian -> LithuanianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Malay -> MalayTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Macedonian -> MacedonianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Norwegian -> NorwegianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Polish -> PolishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Portuguese -> PortugueseTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Russian -> RussianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Slovak -> SlovakTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Serbian -> SerbianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Spanish -> SpanishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Swedish -> SwedishTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Thai -> ThaiTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Ukrainian -> UkrainianTranslations.playback_equalizerStatusLabel_1
        AppLanguage.Urdu -> UrduTranslations.playback_equalizerStatusLabel_1
    }
}

internal fun queueTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_queueTitle_1
    AppLanguage.Catalan -> CatalanTranslations.playback_queueTitle_1
    AppLanguage.Finnish -> FinnishTranslations.playback_queueTitle_1
    AppLanguage.Romanian -> RomanianTranslations.playback_queueTitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_queueTitle_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_queueTitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_queueTitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_queueTitle_1
    AppLanguage.Polish -> PolishTranslations.playback_queueTitle_1
    AppLanguage.Slovak -> SlovakTranslations.playback_queueTitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_queueTitle_1
    AppLanguage.Korean -> KoreanTranslations.playback_queueTitle_1
    AppLanguage.Czech -> CzechTranslations.playback_queueTitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_queueTitle_1
    AppLanguage.Danish -> DanishTranslations.playback_queueTitle_1
    AppLanguage.French -> FrenchTranslations.playback_queueTitle_1
    AppLanguage.German -> GermanTranslations.playback_queueTitle_1
    AppLanguage.Dutch -> DutchTranslations.playback_queueTitle_1
    AppLanguage.Malay -> MalayTranslations.playback_queueTitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_queueTitle_1
    AppLanguage.Swedish -> SwedishTranslations.playback_queueTitle_1
    AppLanguage.Spanish -> SpanishTranslations.playback_queueTitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_queueTitle_1
    AppLanguage.Estonian -> EstonianTranslations.playback_queueTitle_1
    AppLanguage.Bengali -> BengaliTranslations.playback_queueTitle_1
    AppLanguage.Greek -> GreekTranslations.playback_queueTitle_1
    AppLanguage.Croatian -> CroatianTranslations.playback_queueTitle_1
    AppLanguage.Russian -> RussianTranslations.playback_queueTitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_queueTitle_1
    AppLanguage.Urdu -> UrduTranslations.playback_queueTitle_1
    AppLanguage.Latvian -> LatvianTranslations.playback_queueTitle_1
    AppLanguage.Italian -> ItalianTranslations.playback_queueTitle_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_queueTitle_1
    AppLanguage.Hindi -> HindiTranslations.playback_queueTitle_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_queueTitle_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_queueTitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_queueTitle_1
    AppLanguage.Serbian -> SerbianTranslations.playback_queueTitle_1
    AppLanguage.Thai -> ThaiTranslations.playback_queueTitle_1
    AppLanguage.English -> EnglishTranslations.playback_queueTitle_1
}

internal data class SleepTimerCopy(
    val title: String,
    val off: String,
    val endOfSong: String,
    val confirm: String,
    val close: String,
    val minuteSuffix: String = " min",
)

internal fun sleepTimerCopy(language: AppLanguage): SleepTimerCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Catalan -> CatalanTranslations.playback_sleepTimerCopy_1
    AppLanguage.Finnish -> FinnishTranslations.playback_sleepTimerCopy_1
    AppLanguage.Romanian -> RomanianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_sleepTimerCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_sleepTimerCopy_1
    AppLanguage.English -> EnglishTranslations.playback_sleepTimerCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_sleepTimerCopy_1
    AppLanguage.Croatian -> CroatianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Czech -> CzechTranslations.playback_sleepTimerCopy_1
    AppLanguage.Danish -> DanishTranslations.playback_sleepTimerCopy_1
    AppLanguage.Dutch -> DutchTranslations.playback_sleepTimerCopy_1
    AppLanguage.Bengali -> BengaliTranslations.playback_sleepTimerCopy_1
    AppLanguage.Estonian -> EstonianTranslations.playback_sleepTimerCopy_1
    AppLanguage.French -> FrenchTranslations.playback_sleepTimerCopy_1
    AppLanguage.German -> GermanTranslations.playback_sleepTimerCopy_1
    AppLanguage.Greek -> GreekTranslations.playback_sleepTimerCopy_1
    AppLanguage.Hindi -> HindiTranslations.playback_sleepTimerCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Italian -> ItalianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_sleepTimerCopy_1
    AppLanguage.Korean -> KoreanTranslations.playback_sleepTimerCopy_1
    AppLanguage.Latvian -> LatvianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Malay -> MalayTranslations.playback_sleepTimerCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Polish -> PolishTranslations.playback_sleepTimerCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_sleepTimerCopy_1
    AppLanguage.Russian -> RussianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Slovak -> SlovakTranslations.playback_sleepTimerCopy_1
    AppLanguage.Serbian -> SerbianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Spanish -> SpanishTranslations.playback_sleepTimerCopy_1
    AppLanguage.Swedish -> SwedishTranslations.playback_sleepTimerCopy_1
    AppLanguage.Thai -> ThaiTranslations.playback_sleepTimerCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_sleepTimerCopy_1
    AppLanguage.Urdu -> UrduTranslations.playback_sleepTimerCopy_1
}

internal fun playLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.playback_playLabel_1
    AppLanguage.Catalan -> CatalanTranslations.playback_playLabel_1
    AppLanguage.Finnish -> FinnishTranslations.playback_playLabel_1
    AppLanguage.Romanian -> RomanianTranslations.playback_playLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.playback_playLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.playback_playLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.playback_playLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.playback_playLabel_1
    AppLanguage.Polish -> PolishTranslations.playback_playLabel_1
    AppLanguage.Slovak -> SlovakTranslations.playback_playLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playback_playLabel_1
    AppLanguage.Croatian -> CroatianTranslations.playback_playLabel_1
    AppLanguage.Korean -> KoreanTranslations.playback_playLabel_1
    AppLanguage.Czech -> CzechTranslations.playback_playLabel_1
    AppLanguage.Danish -> DanishTranslations.playback_playLabel_1
    AppLanguage.Dutch -> DutchTranslations.playback_playLabel_1
    AppLanguage.Malay -> MalayTranslations.playback_playLabel_1
    AppLanguage.Estonian -> EstonianTranslations.playback_playLabel_1
    AppLanguage.French -> FrenchTranslations.playback_playLabel_1
    AppLanguage.German -> GermanTranslations.playback_playLabel_1
    AppLanguage.Greek -> GreekTranslations.playback_playLabel_1
    AppLanguage.Bengali -> BengaliTranslations.playback_playLabel_1
    AppLanguage.Hindi -> HindiTranslations.playback_playLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.playback_playLabel_1
    AppLanguage.Italian -> ItalianTranslations.playback_playLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.playback_playLabel_1
    AppLanguage.Latvian -> LatvianTranslations.playback_playLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playback_playLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.playback_playLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.playback_playLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.playback_playLabel_1
    AppLanguage.Russian -> RussianTranslations.playback_playLabel_1
    AppLanguage.Serbian -> SerbianTranslations.playback_playLabel_1
    AppLanguage.Spanish -> SpanishTranslations.playback_playLabel_1
    AppLanguage.Swedish -> SwedishTranslations.playback_playLabel_1
    AppLanguage.Thai -> ThaiTranslations.playback_playLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playback_playLabel_1
    AppLanguage.Urdu -> UrduTranslations.playback_playLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.playback_playLabel_1
    AppLanguage.English -> EnglishTranslations.playback_playLabel_1
}

