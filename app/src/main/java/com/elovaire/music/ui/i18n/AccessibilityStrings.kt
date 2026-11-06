package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun crossfadeToggleAccessibilityLabel(enabled: Boolean, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to AlbanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Bengali -> BengaliTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to BengaliTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Bosnian -> BosnianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to BosnianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to BulgarianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Catalan -> CatalanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to CatalanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to ChineseSimplifiedTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Croatian -> CroatianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to CroatianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Czech -> CzechTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to CzechTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Danish -> DanishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to DanishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Dutch -> DutchTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to DutchTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.English -> EnglishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to EnglishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Estonian -> EstonianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to EstonianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Finnish -> FinnishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to FinnishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.French -> FrenchTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to FrenchTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.German -> GermanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to GermanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Greek -> GreekTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to GreekTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Hindi -> HindiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to HindiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Hungarian -> HungarianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to HungarianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Indonesian -> IndonesianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to IndonesianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Italian -> ItalianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to ItalianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Japanese -> JapaneseTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to JapaneseTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Korean -> KoreanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to KoreanTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Latvian -> LatvianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to LatvianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to LithuanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Macedonian -> MacedonianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to MacedonianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Malay -> MalayTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to MalayTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Norwegian -> NorwegianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to NorwegianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Polish -> PolishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to PolishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Portuguese -> PortugueseTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to PortugueseTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Punjabi -> PunjabiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to PunjabiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Romanian -> RomanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to RomanianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Russian -> RussianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to RussianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Serbian -> SerbianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to SerbianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Slovak -> SlovakTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to SlovakTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Slovenian -> SlovenianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to SlovenianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Spanish -> SpanishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to SpanishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Swedish -> SwedishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to SwedishTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Thai -> ThaiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to ThaiTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to UkrainianTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
        AppLanguage.Urdu -> UrduTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_1 to UrduTranslations.accessibility_crossfadeToggleAccessibilityLabel_variant_2
    }
    return if (enabled) labels.second else labels.first
}

internal fun songFavoriteAccessibilityLabel(favorite: Boolean, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to AlbanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Bengali -> BengaliTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to BengaliTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Bosnian -> BosnianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to BosnianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to BulgarianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Catalan -> CatalanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to CatalanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to ChineseSimplifiedTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Croatian -> CroatianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to CroatianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Czech -> CzechTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to CzechTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Danish -> DanishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to DanishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Dutch -> DutchTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to DutchTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.English -> EnglishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to EnglishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Estonian -> EstonianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to EstonianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Finnish -> FinnishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to FinnishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.French -> FrenchTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to FrenchTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.German -> GermanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to GermanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Greek -> GreekTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to GreekTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Hindi -> HindiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to HindiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Hungarian -> HungarianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to HungarianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Indonesian -> IndonesianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to IndonesianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Italian -> ItalianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to ItalianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Japanese -> JapaneseTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to JapaneseTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Korean -> KoreanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to KoreanTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Latvian -> LatvianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to LatvianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to LithuanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Macedonian -> MacedonianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to MacedonianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Malay -> MalayTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to MalayTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Norwegian -> NorwegianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to NorwegianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Polish -> PolishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to PolishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Portuguese -> PortugueseTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to PortugueseTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Punjabi -> PunjabiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to PunjabiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Romanian -> RomanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to RomanianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Russian -> RussianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to RussianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Serbian -> SerbianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to SerbianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Slovak -> SlovakTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to SlovakTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Slovenian -> SlovenianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to SlovenianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Spanish -> SpanishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to SpanishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Swedish -> SwedishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to SwedishTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Thai -> ThaiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to ThaiTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to UkrainianTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
        AppLanguage.Urdu -> UrduTranslations.accessibility_songFavoriteAccessibilityLabel_variant_1 to UrduTranslations.accessibility_songFavoriteAccessibilityLabel_variant_2
    }
    return if (favorite) labels.second else labels.first
}

internal fun editLyricsAccessibilityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Czech -> CzechTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Danish -> DanishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.English -> EnglishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.French -> FrenchTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.German -> GermanTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Greek -> GreekTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Malay -> MalayTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Polish -> PolishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Russian -> RussianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_editLyricsAccessibilityLabel_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_editLyricsAccessibilityLabel_1
}

internal fun artworkPlaceholderLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Czech -> CzechTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Danish -> DanishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.English -> EnglishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.French -> FrenchTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.German -> GermanTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Greek -> GreekTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Malay -> MalayTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Polish -> PolishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Russian -> RussianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_artworkPlaceholderLabel_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_artworkPlaceholderLabel_1
}

internal data class PlaylistAccessibilityCopy(val confirmSongs: String, val reorderSong: String)

internal fun playlistAccessibilityCopy(language: AppLanguage): PlaylistAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_playlistAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_playlistAccessibilityCopy_1
}

internal data class SettingsAccessibilityCopy(val closeLanguage: String, val smallerText: String, val largerText: String)

internal fun settingsAccessibilityCopy(language: AppLanguage): SettingsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_settingsAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_settingsAccessibilityCopy_1
}

internal data class PlayerControlAccessibilityCopy(val minimize: String, val resetTimer: String, val mutedVolume: String, val maximumVolume: String)

internal fun playerControlAccessibilityCopy(language: AppLanguage): PlayerControlAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_playerControlAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_playerControlAccessibilityCopy_1
}

internal data class MediaOptionsAccessibilityCopy(val queueSong: String, val album: String, val song: String)

internal fun mediaOptionsAccessibilityCopy(language: AppLanguage): MediaOptionsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_mediaOptionsAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_mediaOptionsAccessibilityCopy_1
}

internal data class LyricsAccessibilityCopy(val lyrics: String, val addLyrics: String)

internal fun lyricsAccessibilityCopy(language: AppLanguage): LyricsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_lyricsAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_lyricsAccessibilityCopy_1
}

internal fun closeQueueAccessibilityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Czech -> CzechTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Danish -> DanishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.English -> EnglishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.French -> FrenchTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.German -> GermanTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Greek -> GreekTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Malay -> MalayTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Polish -> PolishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Russian -> RussianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_closeQueueAccessibilityLabel_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_closeQueueAccessibilityLabel_1
}

internal data class NavigationAccessibilityCopy(val back: String, val menu: String)

internal fun navigationAccessibilityCopy(language: AppLanguage): NavigationAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_navigationAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_navigationAccessibilityCopy_1
}

internal data class PlaybackNavigationAccessibilityCopy(
    val previous: String,
    val next: String,
    val rewind: String,
    val forward: String,
)

internal fun playbackNavigationAccessibilityCopy(language: AppLanguage): PlaybackNavigationAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_playbackNavigationAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_playbackNavigationAccessibilityCopy_1
}

internal data class ShuffleAccessibilityCopy(val playlist: String, val artist: String)

internal fun shuffleAccessibilityCopy(language: AppLanguage): ShuffleAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_shuffleAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_shuffleAccessibilityCopy_1
}

internal fun showAllSongResultsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Czech -> CzechTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Danish -> DanishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.English -> EnglishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.French -> FrenchTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.German -> GermanTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Greek -> GreekTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Malay -> MalayTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Polish -> PolishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Russian -> RussianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_showAllSongResultsLabel_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_showAllSongResultsLabel_1
}

internal data class ViewModeAccessibilityCopy(val list: String, val compactList: String, val grid: String, val denseGrid: String)

internal fun viewModeAccessibilityCopy(language: AppLanguage): ViewModeAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Bengali -> BengaliTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Catalan -> CatalanTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Croatian -> CroatianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Czech -> CzechTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Danish -> DanishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Dutch -> DutchTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.English -> EnglishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Estonian -> EstonianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Finnish -> FinnishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.French -> FrenchTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.German -> GermanTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Greek -> GreekTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Hindi -> HindiTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Italian -> ItalianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Korean -> KoreanTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Latvian -> LatvianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Malay -> MalayTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Polish -> PolishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Romanian -> RomanianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Russian -> RussianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Serbian -> SerbianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Slovak -> SlovakTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Spanish -> SpanishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Swedish -> SwedishTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Thai -> ThaiTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.accessibility_viewModeAccessibilityCopy_1
    AppLanguage.Urdu -> UrduTranslations.accessibility_viewModeAccessibilityCopy_1
}
