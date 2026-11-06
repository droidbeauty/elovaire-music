package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal data class RootUiCopy(
    val firstLaunchPermissionTitle: String,
    val firstLaunchPermissionMessage: String,
    val firstLaunchPermissionButton: String,
    val appName: String,
    val allAlbumsTitle: String,
    val allAlbumsSubtitle: String,
    val renamePlaylistTitle: String,
    val save: String,
    val newPlaylist: String,
    val playlistArtworkPlaceholder: String,
    val createPlaylistButton: String,
    val tapToCreateNewPlaylist: String,
    val playlistNamePlaceholder: String,
    val clearPlaylistName: String,
    val noSongsInPlaylistYet: String,
    val searchLibrary: String,
    val addSongsTitle: String,
    val loadingLyrics: String,
    val noLyrics: String,
    val hideLyrics: String,
    val ok: String,
    val albumNotFound: String,
    val playlistNotFound: String,
    val mostPlayedSongs: String,
    val availableReleasesSuffix: String,
    val playAlbum: String,
    val shuffleAlbum: String,
    val editTags: String,
    val savePlaylistChanges: String,
    val editPlaylist: String,
)

internal fun rootUiCopy(language: AppLanguage): RootUiCopy = when (language) {
    AppLanguage.Latvian -> LatvianTranslations.home_rootUiCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_rootUiCopy_1
    AppLanguage.Estonian -> EstonianTranslations.home_rootUiCopy_1
    AppLanguage.Greek -> GreekTranslations.home_rootUiCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.home_rootUiCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_rootUiCopy_1
    AppLanguage.Italian -> ItalianTranslations.home_rootUiCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_rootUiCopy_1
    AppLanguage.Russian -> RussianTranslations.home_rootUiCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_rootUiCopy_1
    AppLanguage.Czech -> CzechTranslations.home_rootUiCopy_1
    AppLanguage.Danish -> DanishTranslations.home_rootUiCopy_1
    AppLanguage.Dutch -> DutchTranslations.home_rootUiCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_rootUiCopy_1
    AppLanguage.Swedish -> SwedishTranslations.home_rootUiCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_rootUiCopy_1
    AppLanguage.Hindi -> HindiTranslations.home_rootUiCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.home_rootUiCopy_1
    AppLanguage.Thai -> ThaiTranslations.home_rootUiCopy_1
    AppLanguage.Croatian -> CroatianTranslations.home_rootUiCopy_1
    AppLanguage.French -> FrenchTranslations.home_rootUiCopy_1
    AppLanguage.German -> GermanTranslations.home_rootUiCopy_1
    AppLanguage.Spanish -> SpanishTranslations.home_rootUiCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.home_rootUiCopy_1
    AppLanguage.Catalan -> CatalanTranslations.home_rootUiCopy_1
    AppLanguage.Finnish -> FinnishTranslations.home_rootUiCopy_1
    AppLanguage.Romanian -> RomanianTranslations.home_rootUiCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_rootUiCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.home_rootUiCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_rootUiCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_rootUiCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Serbian -> SerbianTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Polish -> PolishTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Slovak -> SlovakTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Korean -> KoreanTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Malay -> MalayTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Bengali -> BengaliTranslations.home_rootUiCopy_multiline_1
    AppLanguage.Urdu -> UrduTranslations.home_rootUiCopy_multiline_1
    AppLanguage.English -> EnglishTranslations.home_rootUiCopy_multiline_1
}.withLocalizedFirstLaunchPermission(language)

private fun RootUiCopy.withLocalizedFirstLaunchPermission(language: AppLanguage): RootUiCopy {
    return copy(
        firstLaunchPermissionTitle = firstLaunchPermissionTitle(language),
        firstLaunchPermissionMessage = firstLaunchPermissionMessage(language),
        firstLaunchPermissionButton = firstLaunchPermissionButton(language),
    )
}

private fun firstLaunchPermissionTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Catalan -> CatalanTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Finnish -> FinnishTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Romanian -> RomanianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Bosnian -> BosnianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Polish -> PolishTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Albanian -> AlbanianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Croatian -> CroatianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Czech -> CzechTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Danish -> DanishTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Dutch -> DutchTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Estonian -> EstonianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.French -> FrenchTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.German -> GermanTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Greek -> GreekTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Hindi -> HindiTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Hungarian -> HungarianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Italian -> ItalianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Japanese -> JapaneseTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Latvian -> LatvianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Russian -> RussianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Serbian -> SerbianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Spanish -> SpanishTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Swedish -> SwedishTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Thai -> ThaiTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Slovak -> SlovakTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Korean -> KoreanTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Malay -> MalayTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Bengali -> BengaliTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.Urdu -> UrduTranslations.home_firstLaunchPermissionTitle_1
    AppLanguage.English -> EnglishTranslations.home_firstLaunchPermissionTitle_1
}

private fun firstLaunchPermissionMessage(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Catalan -> CatalanTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Finnish -> FinnishTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Romanian -> RomanianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Bosnian -> BosnianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Polish -> PolishTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Albanian -> AlbanianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Croatian -> CroatianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Czech -> CzechTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Danish -> DanishTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Dutch -> DutchTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Estonian -> EstonianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.French -> FrenchTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.German -> GermanTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Greek -> GreekTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Hindi -> HindiTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Hungarian -> HungarianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Italian -> ItalianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Japanese -> JapaneseTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Latvian -> LatvianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Russian -> RussianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Serbian -> SerbianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Spanish -> SpanishTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Swedish -> SwedishTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Thai -> ThaiTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Slovak -> SlovakTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Korean -> KoreanTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Malay -> MalayTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Bengali -> BengaliTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.Urdu -> UrduTranslations.home_firstLaunchPermissionMessage_1
    AppLanguage.English -> EnglishTranslations.home_firstLaunchPermissionMessage_1
}

private fun firstLaunchPermissionButton(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Catalan -> CatalanTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Finnish -> FinnishTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Romanian -> RomanianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Bosnian -> BosnianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Polish -> PolishTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Albanian -> AlbanianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Croatian -> CroatianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Czech -> CzechTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Danish -> DanishTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Dutch -> DutchTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Estonian -> EstonianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.French -> FrenchTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.German -> GermanTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Greek -> GreekTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Hindi -> HindiTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Hungarian -> HungarianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Italian -> ItalianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Japanese -> JapaneseTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Latvian -> LatvianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Russian -> RussianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Serbian -> SerbianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Spanish -> SpanishTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Swedish -> SwedishTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Thai -> ThaiTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Slovak -> SlovakTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Korean -> KoreanTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Malay -> MalayTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Bengali -> BengaliTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.Urdu -> UrduTranslations.home_firstLaunchPermissionButton_1
    AppLanguage.English -> EnglishTranslations.home_firstLaunchPermissionButton_1
}

internal fun availableReleasesLabel(count: Int, language: AppLanguage): String {
    if (language == AppLanguage.Thai) return "มี $count รายการเผยแพร่"
    val lastTwo = count % 100
    val lastDigit = count % 10
    val slavicFew = lastDigit in 2..4 && lastTwo !in 12..14
    val suffix = when (language) {
        AppLanguage.English -> if (count == 1) EnglishTranslations.home_availableReleasesLabel_variant_1 else EnglishTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Albanian -> if (count == 1) AlbanianTranslations.home_availableReleasesLabel_variant_1 else AlbanianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Bulgarian -> if (count == 1) BulgarianTranslations.home_availableReleasesLabel_variant_1 else BulgarianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Catalan -> if (count == 1) CatalanTranslations.home_availableReleasesLabel_variant_1 else CatalanTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Danish -> if (count == 1) DanishTranslations.home_availableReleasesLabel_variant_1 else DanishTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Dutch -> if (count == 1) DutchTranslations.home_availableReleasesLabel_variant_1 else DutchTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Estonian -> if (count == 1) EstonianTranslations.home_availableReleasesLabel_variant_1 else EstonianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Finnish -> if (count == 1) FinnishTranslations.home_availableReleasesLabel_variant_1 else FinnishTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.French -> if (count == 1) FrenchTranslations.home_availableReleasesLabel_variant_1 else FrenchTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.German -> if (count == 1) GermanTranslations.home_availableReleasesLabel_variant_1 else GermanTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Greek -> if (count == 1) GreekTranslations.home_availableReleasesLabel_variant_1 else GreekTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Italian -> if (count == 1) ItalianTranslations.home_availableReleasesLabel_variant_1 else ItalianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Latvian -> when {
            lastTwo in 11..19 || lastDigit == 0 -> LatvianTranslations.home_availableReleasesLabel_plural_1
            lastDigit == 1 -> LatvianTranslations.home_availableReleasesLabel_plural_2
            else -> LatvianTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Lithuanian -> when {
            lastTwo in 11..19 || lastDigit == 0 -> LithuanianTranslations.home_availableReleasesLabel_plural_1
            lastDigit == 1 -> LithuanianTranslations.home_availableReleasesLabel_plural_2
            else -> LithuanianTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Macedonian -> if (count == 1) MacedonianTranslations.home_availableReleasesLabel_variant_1 else MacedonianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Norwegian -> if (count == 1) NorwegianTranslations.home_availableReleasesLabel_variant_1 else NorwegianTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Portuguese -> if (count == 1) PortugueseTranslations.home_availableReleasesLabel_variant_1 else PortugueseTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Punjabi -> if (count == 1) PunjabiTranslations.home_availableReleasesLabel_variant_1 else PunjabiTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Romanian -> when {
            count == 1 -> RomanianTranslations.home_availableReleasesLabel_plural_1
            lastTwo == 0 || lastTwo >= 20 -> RomanianTranslations.home_availableReleasesLabel_plural_2
            else -> RomanianTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Spanish -> if (count == 1) SpanishTranslations.home_availableReleasesLabel_variant_1 else SpanishTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Swedish -> if (count == 1) SwedishTranslations.home_availableReleasesLabel_variant_1 else SwedishTranslations.home_availableReleasesLabel_variant_2
        AppLanguage.Polish -> when {
            count == 1 -> PolishTranslations.home_availableReleasesLabel_plural_1
            slavicFew -> PolishTranslations.home_availableReleasesLabel_plural_2
            else -> PolishTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Czech -> when (count) {
            1 -> CzechTranslations.home_availableReleasesLabel_plural_1
            in 2..4 -> CzechTranslations.home_availableReleasesLabel_plural_2
            else -> CzechTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Slovak -> when (count) {
            1 -> SlovakTranslations.home_availableReleasesLabel_plural_1
            in 2..4 -> SlovakTranslations.home_availableReleasesLabel_plural_2
            else -> SlovakTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Slovenian -> when (lastTwo) {
            1 -> SlovenianTranslations.home_availableReleasesLabel_plural_1
            2 -> SlovenianTranslations.home_availableReleasesLabel_plural_2
            3, 4 -> SlovenianTranslations.home_availableReleasesLabel_plural_3
            else -> SlovenianTranslations.home_availableReleasesLabel_plural_4
        }
        AppLanguage.Russian -> when {
            lastDigit == 1 && lastTwo != 11 -> RussianTranslations.home_availableReleasesLabel_plural_1
            slavicFew -> RussianTranslations.home_availableReleasesLabel_plural_2
            else -> RussianTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Ukrainian -> when {
            lastDigit == 1 && lastTwo != 11 -> UkrainianTranslations.home_availableReleasesLabel_plural_1
            slavicFew -> UkrainianTranslations.home_availableReleasesLabel_plural_2
            else -> UkrainianTranslations.home_availableReleasesLabel_plural_3
        }
        AppLanguage.Bosnian, AppLanguage.Croatian -> when {
            lastDigit == 1 && lastTwo != 11 -> "dostupno izdanje"
            slavicFew -> "dostupna izdanja"
            else -> "dostupnih izdanja"
        }
        AppLanguage.Serbian -> when {
            lastDigit == 1 && lastTwo != 11 -> SerbianTranslations.home_availableReleasesLabel_plural_1
            slavicFew -> SerbianTranslations.home_availableReleasesLabel_plural_2
            else -> SerbianTranslations.home_availableReleasesLabel_plural_3
        }
        else -> rootUiCopy(language).availableReleasesSuffix
    }
    return when (language) {
        AppLanguage.Bengali, AppLanguage.ChineseSimplified, AppLanguage.Japanese, AppLanguage.Korean -> "$count$suffix"
        else -> "$count $suffix"
    }
}

internal fun repeatModeLabel(repeatMode: PlaybackRepeatMode, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.home_repeatModeLabel_1
        AppLanguage.Bengali -> BengaliTranslations.home_repeatModeLabel_1
        AppLanguage.Bosnian -> BosnianTranslations.home_repeatModeLabel_1
        AppLanguage.Bulgarian -> BulgarianTranslations.home_repeatModeLabel_1
        AppLanguage.Catalan -> CatalanTranslations.home_repeatModeLabel_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_repeatModeLabel_1
        AppLanguage.Croatian -> CroatianTranslations.home_repeatModeLabel_1
        AppLanguage.Czech -> CzechTranslations.home_repeatModeLabel_1
        AppLanguage.Danish -> DanishTranslations.home_repeatModeLabel_1
        AppLanguage.Dutch -> DutchTranslations.home_repeatModeLabel_1
        AppLanguage.English -> EnglishTranslations.home_repeatModeLabel_1
        AppLanguage.Estonian -> EstonianTranslations.home_repeatModeLabel_1
        AppLanguage.Finnish -> FinnishTranslations.home_repeatModeLabel_1
        AppLanguage.French -> FrenchTranslations.home_repeatModeLabel_1
        AppLanguage.German -> GermanTranslations.home_repeatModeLabel_1
        AppLanguage.Greek -> GreekTranslations.home_repeatModeLabel_1
        AppLanguage.Hindi -> HindiTranslations.home_repeatModeLabel_1
        AppLanguage.Hungarian -> HungarianTranslations.home_repeatModeLabel_1
        AppLanguage.Indonesian -> IndonesianTranslations.home_repeatModeLabel_1
        AppLanguage.Italian -> ItalianTranslations.home_repeatModeLabel_1
        AppLanguage.Japanese -> JapaneseTranslations.home_repeatModeLabel_1
        AppLanguage.Korean -> KoreanTranslations.home_repeatModeLabel_1
        AppLanguage.Latvian -> LatvianTranslations.home_repeatModeLabel_1
        AppLanguage.Lithuanian -> LithuanianTranslations.home_repeatModeLabel_1
        AppLanguage.Macedonian -> MacedonianTranslations.home_repeatModeLabel_1
        AppLanguage.Malay -> MalayTranslations.home_repeatModeLabel_1
        AppLanguage.Norwegian -> NorwegianTranslations.home_repeatModeLabel_1
        AppLanguage.Polish -> PolishTranslations.home_repeatModeLabel_1
        AppLanguage.Portuguese -> PortugueseTranslations.home_repeatModeLabel_1
        AppLanguage.Punjabi -> PunjabiTranslations.home_repeatModeLabel_1
        AppLanguage.Romanian -> RomanianTranslations.home_repeatModeLabel_1
        AppLanguage.Russian -> RussianTranslations.home_repeatModeLabel_1
        AppLanguage.Serbian -> SerbianTranslations.home_repeatModeLabel_1
        AppLanguage.Slovak -> SlovakTranslations.home_repeatModeLabel_1
        AppLanguage.Slovenian -> SlovenianTranslations.home_repeatModeLabel_1
        AppLanguage.Spanish -> SpanishTranslations.home_repeatModeLabel_1
        AppLanguage.Swedish -> SwedishTranslations.home_repeatModeLabel_1
        AppLanguage.Thai -> ThaiTranslations.home_repeatModeLabel_1
        AppLanguage.Ukrainian -> UkrainianTranslations.home_repeatModeLabel_1
        AppLanguage.Urdu -> UrduTranslations.home_repeatModeLabel_1
    }
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> labels.first
        PlaybackRepeatMode.One -> labels.second
        PlaybackRepeatMode.All -> labels.third
    }
}

internal fun ReverbProfile.displayLabel(language: AppLanguage): String = when (this) {
    ReverbProfile.Dry -> uiPhrase(language, UiPhrase.Dry)
    ReverbProfile.Wet -> uiPhrase(language, UiPhrase.Wet)
}

internal data class HomeUiCopy(
    val indexingTitle: String,
    val indexingMessage: String,
    val emptyLibraryTitle: String,
    val emptyLibraryMessage: String,
    val noRecentAdditionsTitle: String,
    val noRecentAdditionsMessage: String,
    val recentlyPlayedSongsTitle: String,
    val recentlyPlayedSongsEmpty: String,
    val favoriteAlbumsTitle: String,
    val favoriteAlbumsSubtitle: String,
    val noFavoriteAlbumsTitle: String,
    val noFavoriteAlbumsMessage: String,
)

internal fun albumOfDayTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.home_albumOfDayTitle_1
    AppLanguage.Catalan -> CatalanTranslations.home_albumOfDayTitle_1
    AppLanguage.Finnish -> FinnishTranslations.home_albumOfDayTitle_1
    AppLanguage.Romanian -> RomanianTranslations.home_albumOfDayTitle_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_albumOfDayTitle_1
    AppLanguage.Bosnian -> BosnianTranslations.home_albumOfDayTitle_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_albumOfDayTitle_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_albumOfDayTitle_1
    AppLanguage.Albanian -> AlbanianTranslations.home_albumOfDayTitle_1
    AppLanguage.English -> EnglishTranslations.home_albumOfDayTitle_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_albumOfDayTitle_1
    AppLanguage.Croatian -> CroatianTranslations.home_albumOfDayTitle_1
    AppLanguage.Czech -> CzechTranslations.home_albumOfDayTitle_1
    AppLanguage.Danish -> DanishTranslations.home_albumOfDayTitle_1
    AppLanguage.Dutch -> DutchTranslations.home_albumOfDayTitle_1
    AppLanguage.Bengali -> BengaliTranslations.home_albumOfDayTitle_1
    AppLanguage.Estonian -> EstonianTranslations.home_albumOfDayTitle_1
    AppLanguage.French -> FrenchTranslations.home_albumOfDayTitle_1
    AppLanguage.German -> GermanTranslations.home_albumOfDayTitle_1
    AppLanguage.Greek -> GreekTranslations.home_albumOfDayTitle_1
    AppLanguage.Hindi -> HindiTranslations.home_albumOfDayTitle_1
    AppLanguage.Hungarian -> HungarianTranslations.home_albumOfDayTitle_1
    AppLanguage.Italian -> ItalianTranslations.home_albumOfDayTitle_1
    AppLanguage.Japanese -> JapaneseTranslations.home_albumOfDayTitle_1
    AppLanguage.Korean -> KoreanTranslations.home_albumOfDayTitle_1
    AppLanguage.Latvian -> LatvianTranslations.home_albumOfDayTitle_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_albumOfDayTitle_1
    AppLanguage.Malay -> MalayTranslations.home_albumOfDayTitle_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_albumOfDayTitle_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_albumOfDayTitle_1
    AppLanguage.Polish -> PolishTranslations.home_albumOfDayTitle_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_albumOfDayTitle_1
    AppLanguage.Russian -> RussianTranslations.home_albumOfDayTitle_1
    AppLanguage.Slovak -> SlovakTranslations.home_albumOfDayTitle_1
    AppLanguage.Serbian -> SerbianTranslations.home_albumOfDayTitle_1
    AppLanguage.Spanish -> SpanishTranslations.home_albumOfDayTitle_1
    AppLanguage.Swedish -> SwedishTranslations.home_albumOfDayTitle_1
    AppLanguage.Thai -> ThaiTranslations.home_albumOfDayTitle_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_albumOfDayTitle_1
    AppLanguage.Urdu -> UrduTranslations.home_albumOfDayTitle_1
}

internal fun homeCopy(language: AppLanguage): HomeUiCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.home_homeCopy_1
    AppLanguage.Catalan -> CatalanTranslations.home_homeCopy_1
    AppLanguage.Finnish -> FinnishTranslations.home_homeCopy_1
    AppLanguage.Romanian -> RomanianTranslations.home_homeCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.home_homeCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.home_homeCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.home_homeCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.home_homeCopy_1
    AppLanguage.Polish -> PolishTranslations.home_homeUiCopy_multiline_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Croatian -> CroatianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Czech -> CzechTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Danish -> DanishTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Dutch -> DutchTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Estonian -> EstonianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.French -> FrenchTranslations.home_homeUiCopy_multiline_1
    AppLanguage.German -> GermanTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Greek -> GreekTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Hindi -> HindiTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Hungarian -> HungarianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Italian -> ItalianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Japanese -> JapaneseTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Latvian -> LatvianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Lithuanian -> LithuanianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Macedonian -> MacedonianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Norwegian -> NorwegianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Portuguese -> PortugueseTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Russian -> RussianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Serbian -> SerbianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Spanish -> SpanishTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Swedish -> SwedishTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Thai -> ThaiTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Ukrainian -> UkrainianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Slovak -> SlovakTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Korean -> KoreanTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Malay -> MalayTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Bengali -> BengaliTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Urdu -> UrduTranslations.home_homeUiCopy_multiline_1
    AppLanguage.Albanian -> AlbanianTranslations.home_homeUiCopy_multiline_1
    AppLanguage.English -> EnglishTranslations.home_homeUiCopy_multiline_1
}

internal fun formatCountLabel(
    count: Int,
    singular: String,
    language: AppLanguage,
): String {
    return localizedCountLabel(count, singular, language)
}

internal fun localizedCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String {
    if (language == AppLanguage.Bengali) {
        val label = when (noun) {
            "song" -> "গান"
            "track" -> "ট্র্যাক"
            "audiobook" -> "অডিওবুক"
            "album" -> "অ্যালবাম"
            "artist" -> "শিল্পী"
            "genre" -> "ধরন"
            "playlist" -> "প্লেলিস্ট"
            else -> null
        }
        if (label != null) return "$count${if (noun == "artist") "জন" else "টি"} $label"
    }
    if (noun == "playlist") {
        return localizedPlaylistCountLabel(count, language)
    }
    localizedComplexCountLabel(count, noun, language)?.let { return it }
    val (singular, plural) = when (language) {
        AppLanguage.Bulgarian -> when (noun) {
            "song", "track" -> BulgarianTranslations.home_localizedCountLabel_plural_1 to BulgarianTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> BulgarianTranslations.home_localizedCountLabel_plural_3 to BulgarianTranslations.home_localizedCountLabel_plural_4
            "album" -> BulgarianTranslations.home_localizedCountLabel_plural_5 to BulgarianTranslations.home_localizedCountLabel_plural_6
            "artist" -> BulgarianTranslations.home_localizedCountLabel_plural_7 to BulgarianTranslations.home_localizedCountLabel_plural_8
            "genre" -> BulgarianTranslations.home_localizedCountLabel_plural_9 to BulgarianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Catalan -> when (noun) {
            "song", "track" -> CatalanTranslations.home_localizedCountLabel_plural_1 to CatalanTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> CatalanTranslations.home_localizedCountLabel_plural_3 to CatalanTranslations.home_localizedCountLabel_plural_4
            "album" -> CatalanTranslations.home_localizedCountLabel_plural_5 to CatalanTranslations.home_localizedCountLabel_plural_6
            "artist" -> CatalanTranslations.home_localizedCountLabel_plural_7 to CatalanTranslations.home_localizedCountLabel_plural_8
            "genre" -> CatalanTranslations.home_localizedCountLabel_plural_9 to CatalanTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Finnish -> when (noun) {
            "song", "track" -> FinnishTranslations.home_localizedCountLabel_plural_1 to FinnishTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> FinnishTranslations.home_localizedCountLabel_plural_3 to FinnishTranslations.home_localizedCountLabel_plural_4
            "album" -> FinnishTranslations.home_localizedCountLabel_plural_5 to FinnishTranslations.home_localizedCountLabel_plural_6
            "artist" -> FinnishTranslations.home_localizedCountLabel_plural_7 to FinnishTranslations.home_localizedCountLabel_plural_8
            "genre" -> FinnishTranslations.home_localizedCountLabel_plural_9 to FinnishTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Romanian -> when (noun) {
            "song", "track" -> RomanianTranslations.home_localizedCountLabel_plural_1 to RomanianTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> RomanianTranslations.home_localizedCountLabel_plural_3 to RomanianTranslations.home_localizedCountLabel_plural_4
            "album" -> RomanianTranslations.home_localizedCountLabel_plural_5 to RomanianTranslations.home_localizedCountLabel_plural_6
            "artist" -> RomanianTranslations.home_localizedCountLabel_plural_7 to RomanianTranslations.home_localizedCountLabel_plural_8
            "genre" -> RomanianTranslations.home_localizedCountLabel_plural_9 to RomanianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Slovenian -> when (noun) {
            "song", "track" -> SlovenianTranslations.home_localizedCountLabel_plural_1 to SlovenianTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> SlovenianTranslations.home_localizedCountLabel_plural_3 to SlovenianTranslations.home_localizedCountLabel_plural_4
            "album" -> SlovenianTranslations.home_localizedCountLabel_plural_5 to SlovenianTranslations.home_localizedCountLabel_plural_6
            "artist" -> SlovenianTranslations.home_localizedCountLabel_plural_7 to SlovenianTranslations.home_localizedCountLabel_plural_8
            "genre" -> SlovenianTranslations.home_localizedCountLabel_plural_9 to SlovenianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Bosnian -> when (noun) {
            "song", "track" -> BosnianTranslations.home_localizedCountLabel_plural_1 to BosnianTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> BosnianTranslations.home_localizedCountLabel_plural_3 to BosnianTranslations.home_localizedCountLabel_plural_4
            "album" -> BosnianTranslations.home_localizedCountLabel_plural_5 to BosnianTranslations.home_localizedCountLabel_plural_6
            "artist" -> BosnianTranslations.home_localizedCountLabel_plural_7 to BosnianTranslations.home_localizedCountLabel_plural_8
            "genre" -> BosnianTranslations.home_localizedCountLabel_plural_9 to BosnianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Indonesian -> when (noun) {
            "song", "track" -> IndonesianTranslations.home_localizedCountLabel_plural_1 to IndonesianTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> IndonesianTranslations.home_localizedCountLabel_plural_3 to IndonesianTranslations.home_localizedCountLabel_plural_4
            "album" -> IndonesianTranslations.home_localizedCountLabel_plural_5 to IndonesianTranslations.home_localizedCountLabel_plural_6
            "artist" -> IndonesianTranslations.home_localizedCountLabel_plural_7 to IndonesianTranslations.home_localizedCountLabel_plural_8
            "genre" -> IndonesianTranslations.home_localizedCountLabel_plural_9 to IndonesianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Punjabi -> when (noun) {
            "song", "track" -> PunjabiTranslations.home_localizedCountLabel_plural_1 to PunjabiTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> PunjabiTranslations.home_localizedCountLabel_plural_3 to PunjabiTranslations.home_localizedCountLabel_plural_4
            "album" -> PunjabiTranslations.home_localizedCountLabel_plural_5 to PunjabiTranslations.home_localizedCountLabel_plural_6
            "artist" -> PunjabiTranslations.home_localizedCountLabel_plural_7 to PunjabiTranslations.home_localizedCountLabel_plural_8
            "genre" -> PunjabiTranslations.home_localizedCountLabel_plural_9 to PunjabiTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Albanian -> when (noun) {
            "song" -> AlbanianTranslations.home_localizedCountLabel_plural_1 to AlbanianTranslations.home_localizedCountLabel_plural_2
            "track" -> AlbanianTranslations.home_localizedCountLabel_plural_3 to AlbanianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> AlbanianTranslations.home_localizedCountLabel_plural_5 to AlbanianTranslations.home_localizedCountLabel_plural_6
            "album" -> AlbanianTranslations.home_localizedCountLabel_plural_7 to AlbanianTranslations.home_localizedCountLabel_plural_8
            "artist" -> AlbanianTranslations.home_localizedCountLabel_plural_9 to AlbanianTranslations.home_localizedCountLabel_plural_10
            "genre" -> AlbanianTranslations.home_localizedCountLabel_plural_11 to AlbanianTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}e"
        }
        AppLanguage.ChineseSimplified -> when (noun) {
            "song", "track" -> ChineseSimplifiedTranslations.home_localizedCountLabel_plural_1 to ChineseSimplifiedTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> ChineseSimplifiedTranslations.home_localizedCountLabel_plural_3 to ChineseSimplifiedTranslations.home_localizedCountLabel_plural_4
            "album" -> ChineseSimplifiedTranslations.home_localizedCountLabel_plural_5 to ChineseSimplifiedTranslations.home_localizedCountLabel_plural_6
            "artist" -> ChineseSimplifiedTranslations.home_localizedCountLabel_plural_7 to ChineseSimplifiedTranslations.home_localizedCountLabel_plural_8
            "genre" -> ChineseSimplifiedTranslations.home_localizedCountLabel_plural_9 to ChineseSimplifiedTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Croatian -> when (noun) {
            "song" -> CroatianTranslations.home_localizedCountLabel_plural_1 to CroatianTranslations.home_localizedCountLabel_plural_2
            "track" -> CroatianTranslations.home_localizedCountLabel_plural_3 to CroatianTranslations.home_localizedCountLabel_plural_4
            "album" -> CroatianTranslations.home_localizedCountLabel_plural_5 to CroatianTranslations.home_localizedCountLabel_plural_6
            "artist" -> CroatianTranslations.home_localizedCountLabel_plural_7 to CroatianTranslations.home_localizedCountLabel_plural_8
            "genre" -> CroatianTranslations.home_localizedCountLabel_plural_9 to CroatianTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}a"
        }
        AppLanguage.Czech -> when (noun) {
            "song" -> CzechTranslations.home_localizedCountLabel_plural_1 to CzechTranslations.home_localizedCountLabel_plural_2
            "track" -> CzechTranslations.home_localizedCountLabel_plural_3 to CzechTranslations.home_localizedCountLabel_plural_4
            "album" -> CzechTranslations.home_localizedCountLabel_plural_5 to CzechTranslations.home_localizedCountLabel_plural_6
            "artist" -> CzechTranslations.home_localizedCountLabel_plural_7 to CzechTranslations.home_localizedCountLabel_plural_8
            "genre" -> CzechTranslations.home_localizedCountLabel_plural_9 to CzechTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}y"
        }
        AppLanguage.Danish -> when (noun) {
            "song" -> DanishTranslations.home_localizedCountLabel_plural_1 to DanishTranslations.home_localizedCountLabel_plural_2
            "track" -> DanishTranslations.home_localizedCountLabel_plural_3 to DanishTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> DanishTranslations.home_localizedCountLabel_plural_5 to DanishTranslations.home_localizedCountLabel_plural_6
            "album" -> DanishTranslations.home_localizedCountLabel_plural_7 to DanishTranslations.home_localizedCountLabel_plural_8
            "artist" -> DanishTranslations.home_localizedCountLabel_plural_9 to DanishTranslations.home_localizedCountLabel_plural_10
            "genre" -> DanishTranslations.home_localizedCountLabel_plural_11 to DanishTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}er"
        }
        AppLanguage.Dutch -> when (noun) {
            "song" -> DutchTranslations.home_localizedCountLabel_plural_1 to DutchTranslations.home_localizedCountLabel_plural_2
            "track" -> DutchTranslations.home_localizedCountLabel_plural_3 to DutchTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> DutchTranslations.home_localizedCountLabel_plural_5 to DutchTranslations.home_localizedCountLabel_plural_6
            "album" -> DutchTranslations.home_localizedCountLabel_plural_7 to DutchTranslations.home_localizedCountLabel_plural_8
            "artist" -> DutchTranslations.home_localizedCountLabel_plural_9 to DutchTranslations.home_localizedCountLabel_plural_10
            "genre" -> DutchTranslations.home_localizedCountLabel_plural_11 to DutchTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}s"
        }
        AppLanguage.Estonian -> when (noun) {
            "song" -> EstonianTranslations.home_localizedCountLabel_plural_1 to EstonianTranslations.home_localizedCountLabel_plural_2
            "track" -> EstonianTranslations.home_localizedCountLabel_plural_3 to EstonianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> EstonianTranslations.home_localizedCountLabel_plural_5 to EstonianTranslations.home_localizedCountLabel_plural_6
            "album" -> EstonianTranslations.home_localizedCountLabel_plural_7 to EstonianTranslations.home_localizedCountLabel_plural_8
            "artist" -> EstonianTranslations.home_localizedCountLabel_plural_9 to EstonianTranslations.home_localizedCountLabel_plural_10
            "genre" -> EstonianTranslations.home_localizedCountLabel_plural_11 to EstonianTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.French -> when (noun) {
            "song" -> FrenchTranslations.home_localizedCountLabel_plural_1 to FrenchTranslations.home_localizedCountLabel_plural_2
            "track" -> FrenchTranslations.home_localizedCountLabel_plural_3 to FrenchTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> FrenchTranslations.home_localizedCountLabel_plural_5 to FrenchTranslations.home_localizedCountLabel_plural_6
            "album" -> FrenchTranslations.home_localizedCountLabel_plural_7 to FrenchTranslations.home_localizedCountLabel_plural_8
            "artist" -> FrenchTranslations.home_localizedCountLabel_plural_9 to FrenchTranslations.home_localizedCountLabel_plural_10
            "genre" -> FrenchTranslations.home_localizedCountLabel_plural_11 to FrenchTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}s"
        }
        AppLanguage.German -> when (noun) {
            "song" -> GermanTranslations.home_localizedCountLabel_plural_1 to GermanTranslations.home_localizedCountLabel_plural_2
            "track" -> GermanTranslations.home_localizedCountLabel_plural_3 to GermanTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> GermanTranslations.home_localizedCountLabel_plural_5 to GermanTranslations.home_localizedCountLabel_plural_6
            "album" -> GermanTranslations.home_localizedCountLabel_plural_7 to GermanTranslations.home_localizedCountLabel_plural_8
            "artist" -> GermanTranslations.home_localizedCountLabel_plural_9 to GermanTranslations.home_localizedCountLabel_plural_10
            "genre" -> GermanTranslations.home_localizedCountLabel_plural_11 to GermanTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}e"
        }
        AppLanguage.Greek -> when (noun) {
            "song" -> GreekTranslations.home_localizedCountLabel_plural_1 to GreekTranslations.home_localizedCountLabel_plural_2
            "track" -> GreekTranslations.home_localizedCountLabel_plural_3 to GreekTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> GreekTranslations.home_localizedCountLabel_plural_5 to GreekTranslations.home_localizedCountLabel_plural_6
            "album" -> GreekTranslations.home_localizedCountLabel_plural_7 to GreekTranslations.home_localizedCountLabel_plural_8
            "artist" -> GreekTranslations.home_localizedCountLabel_plural_9 to GreekTranslations.home_localizedCountLabel_plural_10
            "genre" -> GreekTranslations.home_localizedCountLabel_plural_11 to GreekTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Hindi -> when (noun) {
            "song" -> HindiTranslations.home_localizedCountLabel_plural_1 to HindiTranslations.home_localizedCountLabel_plural_2
            "track" -> HindiTranslations.home_localizedCountLabel_plural_3 to HindiTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> HindiTranslations.home_localizedCountLabel_plural_5 to HindiTranslations.home_localizedCountLabel_plural_6
            "album" -> HindiTranslations.home_localizedCountLabel_plural_7 to HindiTranslations.home_localizedCountLabel_plural_8
            "artist" -> HindiTranslations.home_localizedCountLabel_plural_9 to HindiTranslations.home_localizedCountLabel_plural_10
            "genre" -> HindiTranslations.home_localizedCountLabel_plural_11 to HindiTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Hungarian -> when (noun) {
            "song" -> HungarianTranslations.home_localizedCountLabel_plural_1 to HungarianTranslations.home_localizedCountLabel_plural_2
            "track" -> HungarianTranslations.home_localizedCountLabel_plural_3 to HungarianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> HungarianTranslations.home_localizedCountLabel_plural_5 to HungarianTranslations.home_localizedCountLabel_plural_6
            "album" -> HungarianTranslations.home_localizedCountLabel_plural_7 to HungarianTranslations.home_localizedCountLabel_plural_8
            "artist" -> HungarianTranslations.home_localizedCountLabel_plural_9 to HungarianTranslations.home_localizedCountLabel_plural_10
            "genre" -> HungarianTranslations.home_localizedCountLabel_plural_11 to HungarianTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Italian -> when (noun) {
            "song" -> ItalianTranslations.home_localizedCountLabel_plural_1 to ItalianTranslations.home_localizedCountLabel_plural_2
            "track" -> ItalianTranslations.home_localizedCountLabel_plural_3 to ItalianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> ItalianTranslations.home_localizedCountLabel_plural_5 to ItalianTranslations.home_localizedCountLabel_plural_6
            "album" -> ItalianTranslations.home_localizedCountLabel_plural_7 to ItalianTranslations.home_localizedCountLabel_plural_8
            "artist" -> ItalianTranslations.home_localizedCountLabel_plural_9 to ItalianTranslations.home_localizedCountLabel_plural_10
            "genre" -> ItalianTranslations.home_localizedCountLabel_plural_11 to ItalianTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}i"
        }
        AppLanguage.Japanese -> when (noun) {
            "song", "track" -> JapaneseTranslations.home_localizedCountLabel_plural_1 to JapaneseTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> JapaneseTranslations.home_localizedCountLabel_plural_3 to JapaneseTranslations.home_localizedCountLabel_plural_4
            "album" -> JapaneseTranslations.home_localizedCountLabel_plural_5 to JapaneseTranslations.home_localizedCountLabel_plural_6
            "artist" -> JapaneseTranslations.home_localizedCountLabel_plural_7 to JapaneseTranslations.home_localizedCountLabel_plural_8
            "genre" -> JapaneseTranslations.home_localizedCountLabel_plural_9 to JapaneseTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> LatvianTranslations.home_localizedCountLabel_plural_1 to LatvianTranslations.home_localizedCountLabel_plural_2
            "track" -> LatvianTranslations.home_localizedCountLabel_plural_3 to LatvianTranslations.home_localizedCountLabel_plural_4
            "album" -> LatvianTranslations.home_localizedCountLabel_plural_5 to LatvianTranslations.home_localizedCountLabel_plural_6
            "artist" -> LatvianTranslations.home_localizedCountLabel_plural_7 to LatvianTranslations.home_localizedCountLabel_plural_8
            "genre" -> LatvianTranslations.home_localizedCountLabel_plural_9 to LatvianTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}i"
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> LithuanianTranslations.home_localizedCountLabel_plural_1 to LithuanianTranslations.home_localizedCountLabel_plural_2
            "track" -> LithuanianTranslations.home_localizedCountLabel_plural_3 to LithuanianTranslations.home_localizedCountLabel_plural_4
            "album" -> LithuanianTranslations.home_localizedCountLabel_plural_5 to LithuanianTranslations.home_localizedCountLabel_plural_6
            "artist" -> LithuanianTranslations.home_localizedCountLabel_plural_7 to LithuanianTranslations.home_localizedCountLabel_plural_8
            "genre" -> LithuanianTranslations.home_localizedCountLabel_plural_9 to LithuanianTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}ai"
        }
        AppLanguage.Macedonian -> when (noun) {
            "song" -> MacedonianTranslations.home_localizedCountLabel_plural_1 to MacedonianTranslations.home_localizedCountLabel_plural_2
            "track" -> MacedonianTranslations.home_localizedCountLabel_plural_3 to MacedonianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> MacedonianTranslations.home_localizedCountLabel_plural_5 to MacedonianTranslations.home_localizedCountLabel_plural_6
            "album" -> MacedonianTranslations.home_localizedCountLabel_plural_7 to MacedonianTranslations.home_localizedCountLabel_plural_8
            "artist" -> MacedonianTranslations.home_localizedCountLabel_plural_9 to MacedonianTranslations.home_localizedCountLabel_plural_10
            "genre" -> MacedonianTranslations.home_localizedCountLabel_plural_11 to MacedonianTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Norwegian -> when (noun) {
            "song" -> NorwegianTranslations.home_localizedCountLabel_plural_1 to NorwegianTranslations.home_localizedCountLabel_plural_2
            "track" -> NorwegianTranslations.home_localizedCountLabel_plural_3 to NorwegianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> NorwegianTranslations.home_localizedCountLabel_plural_5 to NorwegianTranslations.home_localizedCountLabel_plural_6
            "album" -> NorwegianTranslations.home_localizedCountLabel_plural_7 to NorwegianTranslations.home_localizedCountLabel_plural_8
            "artist" -> NorwegianTranslations.home_localizedCountLabel_plural_9 to NorwegianTranslations.home_localizedCountLabel_plural_10
            "genre" -> NorwegianTranslations.home_localizedCountLabel_plural_11 to NorwegianTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Slovak -> when (noun) {
            "song" -> SlovakTranslations.home_localizedCountLabel_plural_1 to SlovakTranslations.home_localizedCountLabel_plural_2
            "track" -> SlovakTranslations.home_localizedCountLabel_plural_3 to SlovakTranslations.home_localizedCountLabel_plural_4
            "album" -> SlovakTranslations.home_localizedCountLabel_plural_5 to SlovakTranslations.home_localizedCountLabel_plural_6
            "artist" -> SlovakTranslations.home_localizedCountLabel_plural_7 to SlovakTranslations.home_localizedCountLabel_plural_8
            "genre" -> SlovakTranslations.home_localizedCountLabel_plural_9 to SlovakTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}y"
        }
        AppLanguage.Polish -> when (noun) {
            "song" -> PolishTranslations.home_localizedCountLabel_plural_1 to PolishTranslations.home_localizedCountLabel_plural_2
            "track" -> PolishTranslations.home_localizedCountLabel_plural_3 to PolishTranslations.home_localizedCountLabel_plural_4
            "album" -> PolishTranslations.home_localizedCountLabel_plural_5 to PolishTranslations.home_localizedCountLabel_plural_6
            "artist" -> PolishTranslations.home_localizedCountLabel_plural_7 to PolishTranslations.home_localizedCountLabel_plural_8
            "genre" -> PolishTranslations.home_localizedCountLabel_plural_9 to PolishTranslations.home_localizedCountLabel_plural_10
            else -> noun to "${noun}y"
        }
        AppLanguage.Portuguese -> when (noun) {
            "song" -> PortugueseTranslations.home_localizedCountLabel_plural_1 to PortugueseTranslations.home_localizedCountLabel_plural_2
            "track" -> PortugueseTranslations.home_localizedCountLabel_plural_3 to PortugueseTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> PortugueseTranslations.home_localizedCountLabel_plural_5 to PortugueseTranslations.home_localizedCountLabel_plural_6
            "album" -> PortugueseTranslations.home_localizedCountLabel_plural_7 to PortugueseTranslations.home_localizedCountLabel_plural_8
            "artist" -> PortugueseTranslations.home_localizedCountLabel_plural_9 to PortugueseTranslations.home_localizedCountLabel_plural_10
            "genre" -> PortugueseTranslations.home_localizedCountLabel_plural_11 to PortugueseTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}s"
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> RussianTranslations.home_localizedCountLabel_plural_1 to RussianTranslations.home_localizedCountLabel_plural_2
            "track" -> RussianTranslations.home_localizedCountLabel_plural_3 to RussianTranslations.home_localizedCountLabel_plural_4
            "album" -> RussianTranslations.home_localizedCountLabel_plural_5 to RussianTranslations.home_localizedCountLabel_plural_6
            "artist" -> RussianTranslations.home_localizedCountLabel_plural_7 to RussianTranslations.home_localizedCountLabel_plural_8
            "genre" -> RussianTranslations.home_localizedCountLabel_plural_9 to RussianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> SerbianTranslations.home_localizedCountLabel_plural_1 to SerbianTranslations.home_localizedCountLabel_plural_2
            "track" -> SerbianTranslations.home_localizedCountLabel_plural_3 to SerbianTranslations.home_localizedCountLabel_plural_4
            "album" -> SerbianTranslations.home_localizedCountLabel_plural_5 to SerbianTranslations.home_localizedCountLabel_plural_6
            "artist" -> SerbianTranslations.home_localizedCountLabel_plural_7 to SerbianTranslations.home_localizedCountLabel_plural_8
            "genre" -> SerbianTranslations.home_localizedCountLabel_plural_9 to SerbianTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Spanish -> when (noun) {
            "song" -> SpanishTranslations.home_localizedCountLabel_plural_1 to SpanishTranslations.home_localizedCountLabel_plural_2
            "track" -> SpanishTranslations.home_localizedCountLabel_plural_3 to SpanishTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> SpanishTranslations.home_localizedCountLabel_plural_5 to SpanishTranslations.home_localizedCountLabel_plural_6
            "album" -> SpanishTranslations.home_localizedCountLabel_plural_7 to SpanishTranslations.home_localizedCountLabel_plural_8
            "artist" -> SpanishTranslations.home_localizedCountLabel_plural_9 to SpanishTranslations.home_localizedCountLabel_plural_10
            "genre" -> SpanishTranslations.home_localizedCountLabel_plural_11 to SpanishTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}s"
        }
        AppLanguage.Swedish -> when (noun) {
            "song" -> SwedishTranslations.home_localizedCountLabel_plural_1 to SwedishTranslations.home_localizedCountLabel_plural_2
            "track" -> SwedishTranslations.home_localizedCountLabel_plural_3 to SwedishTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> SwedishTranslations.home_localizedCountLabel_plural_5 to SwedishTranslations.home_localizedCountLabel_plural_6
            "album" -> SwedishTranslations.home_localizedCountLabel_plural_7 to SwedishTranslations.home_localizedCountLabel_plural_8
            "artist" -> SwedishTranslations.home_localizedCountLabel_plural_9 to SwedishTranslations.home_localizedCountLabel_plural_10
            "genre" -> SwedishTranslations.home_localizedCountLabel_plural_11 to SwedishTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Korean -> when (noun) {
            "song", "track" -> KoreanTranslations.home_localizedCountLabel_plural_1 to KoreanTranslations.home_localizedCountLabel_plural_2
            "audiobook" -> KoreanTranslations.home_localizedCountLabel_plural_3 to KoreanTranslations.home_localizedCountLabel_plural_4
            "album" -> KoreanTranslations.home_localizedCountLabel_plural_5 to KoreanTranslations.home_localizedCountLabel_plural_6
            "artist" -> KoreanTranslations.home_localizedCountLabel_plural_7 to KoreanTranslations.home_localizedCountLabel_plural_8
            "genre" -> KoreanTranslations.home_localizedCountLabel_plural_9 to KoreanTranslations.home_localizedCountLabel_plural_10
            else -> noun to noun
        }
        AppLanguage.Malay -> when (noun) {
            "song" -> MalayTranslations.home_localizedCountLabel_plural_1 to MalayTranslations.home_localizedCountLabel_plural_2
            "track" -> MalayTranslations.home_localizedCountLabel_plural_3 to MalayTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> MalayTranslations.home_localizedCountLabel_plural_5 to MalayTranslations.home_localizedCountLabel_plural_6
            "album" -> MalayTranslations.home_localizedCountLabel_plural_7 to MalayTranslations.home_localizedCountLabel_plural_8
            "artist" -> MalayTranslations.home_localizedCountLabel_plural_9 to MalayTranslations.home_localizedCountLabel_plural_10
            "genre" -> MalayTranslations.home_localizedCountLabel_plural_11 to MalayTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Thai -> when (noun) {
            "song" -> ThaiTranslations.home_localizedCountLabel_plural_1 to ThaiTranslations.home_localizedCountLabel_plural_2
            "track" -> ThaiTranslations.home_localizedCountLabel_plural_3 to ThaiTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> ThaiTranslations.home_localizedCountLabel_plural_5 to ThaiTranslations.home_localizedCountLabel_plural_6
            "album" -> ThaiTranslations.home_localizedCountLabel_plural_7 to ThaiTranslations.home_localizedCountLabel_plural_8
            "artist" -> ThaiTranslations.home_localizedCountLabel_plural_9 to ThaiTranslations.home_localizedCountLabel_plural_10
            "genre" -> ThaiTranslations.home_localizedCountLabel_plural_11 to ThaiTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Bengali -> when (noun) {
            "song" -> BengaliTranslations.home_localizedCountLabel_plural_1 to BengaliTranslations.home_localizedCountLabel_plural_2
            "track" -> BengaliTranslations.home_localizedCountLabel_plural_3 to BengaliTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> BengaliTranslations.home_localizedCountLabel_plural_5 to BengaliTranslations.home_localizedCountLabel_plural_6
            "album" -> BengaliTranslations.home_localizedCountLabel_plural_7 to BengaliTranslations.home_localizedCountLabel_plural_8
            "artist" -> BengaliTranslations.home_localizedCountLabel_plural_9 to BengaliTranslations.home_localizedCountLabel_plural_10
            "genre" -> BengaliTranslations.home_localizedCountLabel_plural_11 to BengaliTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> UkrainianTranslations.home_localizedCountLabel_plural_1 to UkrainianTranslations.home_localizedCountLabel_plural_2
            "track" -> UkrainianTranslations.home_localizedCountLabel_plural_3 to UkrainianTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> UkrainianTranslations.home_localizedCountLabel_plural_5 to UkrainianTranslations.home_localizedCountLabel_plural_6
            "album" -> UkrainianTranslations.home_localizedCountLabel_plural_7 to UkrainianTranslations.home_localizedCountLabel_plural_8
            "artist" -> UkrainianTranslations.home_localizedCountLabel_plural_9 to UkrainianTranslations.home_localizedCountLabel_plural_10
            "genre" -> UkrainianTranslations.home_localizedCountLabel_plural_11 to UkrainianTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.Urdu -> when (noun) {
            "song" -> UrduTranslations.home_localizedCountLabel_plural_1 to UrduTranslations.home_localizedCountLabel_plural_2
            "track" -> UrduTranslations.home_localizedCountLabel_plural_3 to UrduTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> UrduTranslations.home_localizedCountLabel_plural_5 to UrduTranslations.home_localizedCountLabel_plural_6
            "album" -> UrduTranslations.home_localizedCountLabel_plural_7 to UrduTranslations.home_localizedCountLabel_plural_8
            "artist" -> UrduTranslations.home_localizedCountLabel_plural_9 to UrduTranslations.home_localizedCountLabel_plural_10
            "genre" -> UrduTranslations.home_localizedCountLabel_plural_11 to UrduTranslations.home_localizedCountLabel_plural_12
            else -> noun to noun
        }
        AppLanguage.English -> when (noun) {
            "song" -> EnglishTranslations.home_localizedCountLabel_plural_1 to EnglishTranslations.home_localizedCountLabel_plural_2
            "track" -> EnglishTranslations.home_localizedCountLabel_plural_3 to EnglishTranslations.home_localizedCountLabel_plural_4
            "audiobook" -> EnglishTranslations.home_localizedCountLabel_plural_5 to EnglishTranslations.home_localizedCountLabel_plural_6
            "album" -> EnglishTranslations.home_localizedCountLabel_plural_7 to EnglishTranslations.home_localizedCountLabel_plural_8
            "artist" -> EnglishTranslations.home_localizedCountLabel_plural_9 to EnglishTranslations.home_localizedCountLabel_plural_10
            "genre" -> EnglishTranslations.home_localizedCountLabel_plural_11 to EnglishTranslations.home_localizedCountLabel_plural_12
            else -> noun to "${noun}s"
        }
    }
    val label = if (count == 1) singular else plural
    val separator = if (language == AppLanguage.Romanian && count >= 20 && count % 100 !in 1..19) " de " else " "
    return "$count$separator$label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedPlaylistCountLabel(count: Int, language: AppLanguage): String {
    val forms = when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Catalan -> CatalanTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Finnish -> FinnishTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Romanian -> RomanianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Slovenian -> SlovenianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Bosnian -> BosnianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Indonesian -> IndonesianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Punjabi -> PunjabiTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Albanian -> AlbanianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Croatian -> CroatianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Czech -> CzechTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Danish -> DanishTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Dutch -> DutchTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Bengali -> BengaliTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Estonian -> EstonianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.French -> FrenchTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.German -> GermanTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Greek -> GreekTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Hindi -> HindiTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Hungarian -> HungarianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Italian -> ItalianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Japanese -> JapaneseTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Korean -> KoreanTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Latvian -> LatvianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Lithuanian -> LithuanianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Malay -> MalayTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Macedonian -> MacedonianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Norwegian -> NorwegianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Polish -> PolishTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Portuguese -> PortugueseTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Russian -> RussianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Slovak -> SlovakTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Serbian -> SerbianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Spanish -> SpanishTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Swedish -> SwedishTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Thai -> ThaiTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Ukrainian -> UkrainianTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.Urdu -> UrduTranslations.home_localizedPlaylistCountLabel_1
        AppLanguage.English -> EnglishTranslations.home_localizedPlaylistCountLabel_1
    }
    val number = count.coerceAtLeast(0)
    val lastTwo = number % 100
    val last = number % 10
    val label = when {
        language == AppLanguage.Slovenian && lastTwo == 1 -> forms.first
        language == AppLanguage.Slovenian && lastTwo == 2 -> forms.second
        language == AppLanguage.Slovenian && lastTwo in 3..4 -> "seznami predvajanja"
        language == AppLanguage.Slovenian -> forms.third
        language in setOf(AppLanguage.Bulgarian, AppLanguage.Catalan, AppLanguage.Finnish, AppLanguage.Romanian) -> if (number == 1) forms.first else forms.second
        language == AppLanguage.Punjabi -> if (number == 1) forms.first else forms.second
        language == AppLanguage.Polish && number == 1 -> forms.first
        language == AppLanguage.Polish && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Polish -> forms.third
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number == 1 -> forms.first
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number in 2..4 -> forms.second
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) -> forms.third
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Latvian && last in 2..9 -> forms.second
        language == AppLanguage.Latvian && last == 1 -> forms.first
        language == AppLanguage.Lithuanian && (last == 0 || lastTwo in 11..19) -> forms.third
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) && last == 1 && lastTwo != 11 -> forms.first
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) -> forms.third
        language == AppLanguage.Lithuanian && last == 1 && lastTwo != 11 -> forms.first
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        number == 1 -> forms.first
        else -> forms.second
    }
    val separator = if (language == AppLanguage.Romanian && count >= 20 && count % 100 !in 1..19) " de " else " "
    return "$count$separator$label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedComplexCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String? {
    val forms = when (language) {
        AppLanguage.Slovenian -> when (noun) {
            "song", "track" -> Triple(SlovenianTranslations.home_localizedComplexCountLabel_plural_1, SlovenianTranslations.home_localizedComplexCountLabel_plural_2, SlovenianTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(SlovenianTranslations.home_localizedComplexCountLabel_plural_4, SlovenianTranslations.home_localizedComplexCountLabel_plural_5, SlovenianTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(SlovenianTranslations.home_localizedComplexCountLabel_plural_7, SlovenianTranslations.home_localizedComplexCountLabel_plural_8, SlovenianTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(SlovenianTranslations.home_localizedComplexCountLabel_plural_10, SlovenianTranslations.home_localizedComplexCountLabel_plural_11, SlovenianTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(SlovenianTranslations.home_localizedComplexCountLabel_plural_13, SlovenianTranslations.home_localizedComplexCountLabel_plural_14, SlovenianTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Bosnian -> when (noun) {
            "song", "track" -> Triple(BosnianTranslations.home_localizedComplexCountLabel_plural_1, BosnianTranslations.home_localizedComplexCountLabel_plural_2, BosnianTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(BosnianTranslations.home_localizedComplexCountLabel_plural_4, BosnianTranslations.home_localizedComplexCountLabel_plural_5, BosnianTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(BosnianTranslations.home_localizedComplexCountLabel_plural_7, BosnianTranslations.home_localizedComplexCountLabel_plural_8, BosnianTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(BosnianTranslations.home_localizedComplexCountLabel_plural_10, BosnianTranslations.home_localizedComplexCountLabel_plural_11, BosnianTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(BosnianTranslations.home_localizedComplexCountLabel_plural_13, BosnianTranslations.home_localizedComplexCountLabel_plural_14, BosnianTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Croatian -> when (noun) {
            "song", "track" -> Triple(CroatianTranslations.home_localizedComplexCountLabel_plural_1, CroatianTranslations.home_localizedComplexCountLabel_plural_2, CroatianTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(CroatianTranslations.home_localizedComplexCountLabel_plural_4, CroatianTranslations.home_localizedComplexCountLabel_plural_5, CroatianTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(CroatianTranslations.home_localizedComplexCountLabel_plural_7, CroatianTranslations.home_localizedComplexCountLabel_plural_8, CroatianTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(CroatianTranslations.home_localizedComplexCountLabel_plural_10, CroatianTranslations.home_localizedComplexCountLabel_plural_11, CroatianTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(CroatianTranslations.home_localizedComplexCountLabel_plural_13, CroatianTranslations.home_localizedComplexCountLabel_plural_14, CroatianTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Czech -> when (noun) {
            "song", "track" -> Triple(CzechTranslations.home_localizedComplexCountLabel_plural_1, CzechTranslations.home_localizedComplexCountLabel_plural_2, CzechTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(CzechTranslations.home_localizedComplexCountLabel_plural_4, CzechTranslations.home_localizedComplexCountLabel_plural_5, CzechTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(CzechTranslations.home_localizedComplexCountLabel_plural_7, CzechTranslations.home_localizedComplexCountLabel_plural_8, CzechTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(CzechTranslations.home_localizedComplexCountLabel_plural_10, CzechTranslations.home_localizedComplexCountLabel_plural_11, CzechTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(CzechTranslations.home_localizedComplexCountLabel_plural_13, CzechTranslations.home_localizedComplexCountLabel_plural_14, CzechTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_1, LatvianTranslations.home_localizedComplexCountLabel_plural_2, LatvianTranslations.home_localizedComplexCountLabel_plural_3)
            "track" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_4, LatvianTranslations.home_localizedComplexCountLabel_plural_5, LatvianTranslations.home_localizedComplexCountLabel_plural_6)
            "audiobook" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_7, LatvianTranslations.home_localizedComplexCountLabel_plural_8, LatvianTranslations.home_localizedComplexCountLabel_plural_9)
            "album" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_10, LatvianTranslations.home_localizedComplexCountLabel_plural_11, LatvianTranslations.home_localizedComplexCountLabel_plural_12)
            "artist" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_13, LatvianTranslations.home_localizedComplexCountLabel_plural_14, LatvianTranslations.home_localizedComplexCountLabel_plural_15)
            "genre" -> Triple(LatvianTranslations.home_localizedComplexCountLabel_plural_16, LatvianTranslations.home_localizedComplexCountLabel_plural_17, LatvianTranslations.home_localizedComplexCountLabel_plural_18)
            else -> return null
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_1, LithuanianTranslations.home_localizedComplexCountLabel_plural_2, LithuanianTranslations.home_localizedComplexCountLabel_plural_3)
            "track" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_4, LithuanianTranslations.home_localizedComplexCountLabel_plural_5, LithuanianTranslations.home_localizedComplexCountLabel_plural_6)
            "audiobook" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_7, LithuanianTranslations.home_localizedComplexCountLabel_plural_8, LithuanianTranslations.home_localizedComplexCountLabel_plural_9)
            "album" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_10, LithuanianTranslations.home_localizedComplexCountLabel_plural_11, LithuanianTranslations.home_localizedComplexCountLabel_plural_12)
            "artist" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_13, LithuanianTranslations.home_localizedComplexCountLabel_plural_14, LithuanianTranslations.home_localizedComplexCountLabel_plural_15)
            "genre" -> Triple(LithuanianTranslations.home_localizedComplexCountLabel_plural_16, LithuanianTranslations.home_localizedComplexCountLabel_plural_17, LithuanianTranslations.home_localizedComplexCountLabel_plural_18)
            else -> return null
        }
        AppLanguage.Polish -> when (noun) {
            "song", "track" -> Triple(PolishTranslations.home_localizedComplexCountLabel_plural_1, PolishTranslations.home_localizedComplexCountLabel_plural_2, PolishTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(PolishTranslations.home_localizedComplexCountLabel_plural_4, PolishTranslations.home_localizedComplexCountLabel_plural_5, PolishTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(PolishTranslations.home_localizedComplexCountLabel_plural_7, PolishTranslations.home_localizedComplexCountLabel_plural_8, PolishTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(PolishTranslations.home_localizedComplexCountLabel_plural_10, PolishTranslations.home_localizedComplexCountLabel_plural_11, PolishTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(PolishTranslations.home_localizedComplexCountLabel_plural_13, PolishTranslations.home_localizedComplexCountLabel_plural_14, PolishTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_1, RussianTranslations.home_localizedComplexCountLabel_plural_2, RussianTranslations.home_localizedComplexCountLabel_plural_3)
            "track" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_4, RussianTranslations.home_localizedComplexCountLabel_plural_5, RussianTranslations.home_localizedComplexCountLabel_plural_6)
            "audiobook" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_7, RussianTranslations.home_localizedComplexCountLabel_plural_8, RussianTranslations.home_localizedComplexCountLabel_plural_9)
            "album" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_10, RussianTranslations.home_localizedComplexCountLabel_plural_11, RussianTranslations.home_localizedComplexCountLabel_plural_12)
            "artist" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_13, RussianTranslations.home_localizedComplexCountLabel_plural_14, RussianTranslations.home_localizedComplexCountLabel_plural_15)
            "genre" -> Triple(RussianTranslations.home_localizedComplexCountLabel_plural_16, RussianTranslations.home_localizedComplexCountLabel_plural_17, RussianTranslations.home_localizedComplexCountLabel_plural_18)
            else -> return null
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_1, SerbianTranslations.home_localizedComplexCountLabel_plural_2, SerbianTranslations.home_localizedComplexCountLabel_plural_3)
            "track" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_4, SerbianTranslations.home_localizedComplexCountLabel_plural_5, SerbianTranslations.home_localizedComplexCountLabel_plural_6)
            "audiobook" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_7, SerbianTranslations.home_localizedComplexCountLabel_plural_8, SerbianTranslations.home_localizedComplexCountLabel_plural_9)
            "album" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_10, SerbianTranslations.home_localizedComplexCountLabel_plural_11, SerbianTranslations.home_localizedComplexCountLabel_plural_12)
            "artist" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_13, SerbianTranslations.home_localizedComplexCountLabel_plural_14, SerbianTranslations.home_localizedComplexCountLabel_plural_15)
            "genre" -> Triple(SerbianTranslations.home_localizedComplexCountLabel_plural_16, SerbianTranslations.home_localizedComplexCountLabel_plural_17, SerbianTranslations.home_localizedComplexCountLabel_plural_18)
            else -> return null
        }
        AppLanguage.Slovak -> when (noun) {
            "song", "track" -> Triple(SlovakTranslations.home_localizedComplexCountLabel_plural_1, SlovakTranslations.home_localizedComplexCountLabel_plural_2, SlovakTranslations.home_localizedComplexCountLabel_plural_3)
            "audiobook" -> Triple(SlovakTranslations.home_localizedComplexCountLabel_plural_4, SlovakTranslations.home_localizedComplexCountLabel_plural_5, SlovakTranslations.home_localizedComplexCountLabel_plural_6)
            "album" -> Triple(SlovakTranslations.home_localizedComplexCountLabel_plural_7, SlovakTranslations.home_localizedComplexCountLabel_plural_8, SlovakTranslations.home_localizedComplexCountLabel_plural_9)
            "artist" -> Triple(SlovakTranslations.home_localizedComplexCountLabel_plural_10, SlovakTranslations.home_localizedComplexCountLabel_plural_11, SlovakTranslations.home_localizedComplexCountLabel_plural_12)
            "genre" -> Triple(SlovakTranslations.home_localizedComplexCountLabel_plural_13, SlovakTranslations.home_localizedComplexCountLabel_plural_14, SlovakTranslations.home_localizedComplexCountLabel_plural_15)
            else -> return null
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_1, UkrainianTranslations.home_localizedComplexCountLabel_plural_2, UkrainianTranslations.home_localizedComplexCountLabel_plural_3)
            "track" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_4, UkrainianTranslations.home_localizedComplexCountLabel_plural_5, UkrainianTranslations.home_localizedComplexCountLabel_plural_6)
            "audiobook" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_7, UkrainianTranslations.home_localizedComplexCountLabel_plural_8, UkrainianTranslations.home_localizedComplexCountLabel_plural_9)
            "album" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_10, UkrainianTranslations.home_localizedComplexCountLabel_plural_11, UkrainianTranslations.home_localizedComplexCountLabel_plural_12)
            "artist" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_13, UkrainianTranslations.home_localizedComplexCountLabel_plural_14, UkrainianTranslations.home_localizedComplexCountLabel_plural_15)
            "genre" -> Triple(UkrainianTranslations.home_localizedComplexCountLabel_plural_16, UkrainianTranslations.home_localizedComplexCountLabel_plural_17, UkrainianTranslations.home_localizedComplexCountLabel_plural_18)
            else -> return null
        }
        else -> return null
    }
    val number = count.coerceAtLeast(0)
    val lastTwo = number % 100
    val last = number % 10
    val label = when {
        language == AppLanguage.Slovenian && lastTwo == 1 -> forms.first
        language == AppLanguage.Slovenian && lastTwo == 2 -> forms.second
        language == AppLanguage.Slovenian && lastTwo in 3..4 -> when (noun) {
            "song", "track" -> "skladbe"
            "audiobook" -> "zvočne knjige"
            "album" -> "albumi"
            "artist" -> "izvajalci"
            "genre" -> "žanri"
            else -> forms.third
        }
        language == AppLanguage.Slovenian -> forms.third
        language == AppLanguage.Polish && number == 1 -> forms.first
        language == AppLanguage.Polish && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Polish -> forms.third
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number == 1 -> forms.first
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number in 2..4 -> forms.second
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) -> forms.third
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Lithuanian && lastTwo in 11..19 -> forms.third
        last == 1 && lastTwo != 11 -> forms.first
        language == AppLanguage.Latvian && last in 2..9 -> forms.second
        last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        else -> forms.third
    }
    return "$count $label"
}
