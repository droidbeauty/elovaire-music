package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode


internal data class SearchUiCopy(
    val placeholder: String,
    val clearSearch: String,
    val nothingSearchedTitle: String,
    val nothingSearchedMessage: String,
    val suggestedAlbumsTitle: String,
    val suggestedAlbumsSubtitle: String,
    val recentlySearched: String,
    val clearHistory: String,
    val noResultsTitle: String,
    val noResultsPrefix: String,
    val noResultsSuffix: String,
    val matchingArtistsSuffix: String,
    val matchingAlbumsSuffix: String,
    val matchingSongsSuffix: String,
) {
    fun noResultsMessage(query: String): String = "$noResultsPrefix \"$query\" $noResultsSuffix".trimEnd()
    fun matchingArtists(count: Int, language: AppLanguage): String = if (language == AppLanguage.Polish) {
        val adjective = if (count == 1) "pasujący" else "pasujących"
        "$count $adjective ${localizedCountLabel(count, "artist", language).substringAfter(' ')}"
    } else if (language in labeledSearchCountLanguages) {
        localizedSearchCount(count, "artist", language)
    } else if (language == AppLanguage.Latvian) {
        latvianMatchingCount(count, "artist")
    } else if (language == AppLanguage.Slovenian) {
        slovenianMatchingCount(count, "artist")
    } else if (language == AppLanguage.French) {
        frenchMatchingCount(count, "artist")
    } else if (language == AppLanguage.German) {
        germanMatchingCount(count, "artist")
    } else if (language == AppLanguage.Dutch) {
        dutchMatchingCount(count, "artist")
    } else if (language == AppLanguage.Catalan) {
        catalanMatchingCount(count, "artist")
    } else if (language == AppLanguage.Finnish) {
        finnishMatchingCount(count, "artist")
    } else if (language == AppLanguage.Romanian) {
        romanianMatchingCount(count, "artist")
    } else if (language == AppLanguage.Danish || language == AppLanguage.Swedish) {
        scandinavianMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Czech) {
        czechMatchingCount(count, "artist")
    } else if (language == AppLanguage.Slovak) {
        slovakMatchingCount(count, "artist")
    } else if (language == AppLanguage.Russian || language == AppLanguage.Ukrainian) {
        eastSlavicMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Bosnian || language == AppLanguage.Croatian || language == AppLanguage.Serbian) {
        southSlavicMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Bulgarian || language == AppLanguage.Macedonian) {
        balkanMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Greek || language == AppLanguage.Hungarian || language == AppLanguage.Albanian) {
        labeledMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Estonian) {
        estonianMatchingCount(count, "artist")
    } else if (language == AppLanguage.Lithuanian) {
        lithuanianMatchingCount(count, "artist")
    } else if (language == AppLanguage.Hindi || language == AppLanguage.Punjabi || language == AppLanguage.Urdu) {
        southAsianMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.Bengali) {
        bengaliMatchingCount(count, "artist")
    } else if (language in setOf(AppLanguage.Spanish, AppLanguage.Portuguese, AppLanguage.Italian)) {
        romanceMatchingCount(count, "artist", language)
    } else if (language == AppLanguage.English && count == 1) {
        "1 matching artist"
    } else "$count $matchingArtistsSuffix"

    fun matchingAlbums(count: Int, language: AppLanguage): String = matchingItems(count, "album", matchingAlbumsSuffix, language)
    fun matchingSongs(count: Int, language: AppLanguage): String = matchingItems(count, "song", matchingSongsSuffix, language)

    private fun matchingItems(count: Int, noun: String, suffix: String, language: AppLanguage): String {
        if (language == AppLanguage.English && count == 1) return "1 matching $noun result"
        if (language in labeledSearchCountLanguages) return localizedSearchCount(count, noun, language)
        if (language == AppLanguage.Latvian) return latvianMatchingCount(count, noun)
        if (language == AppLanguage.Slovenian) return slovenianMatchingCount(count, noun)
        if (language == AppLanguage.French) return frenchMatchingCount(count, noun)
        if (language == AppLanguage.German) return germanMatchingCount(count, noun)
        if (language == AppLanguage.Dutch) return dutchMatchingCount(count, noun)
        if (language == AppLanguage.Catalan) return catalanMatchingCount(count, noun)
        if (language == AppLanguage.Finnish) return finnishMatchingCount(count, noun)
        if (language == AppLanguage.Romanian) return romanianMatchingCount(count, noun)
        if (language == AppLanguage.Danish || language == AppLanguage.Swedish) return scandinavianMatchingCount(count, noun, language)
        if (language == AppLanguage.Czech) return czechMatchingCount(count, noun)
        if (language == AppLanguage.Slovak) return slovakMatchingCount(count, noun)
        if (language == AppLanguage.Russian || language == AppLanguage.Ukrainian) return eastSlavicMatchingCount(count, noun, language)
        if (language == AppLanguage.Bosnian || language == AppLanguage.Croatian || language == AppLanguage.Serbian) return southSlavicMatchingCount(count, noun, language)
        if (language == AppLanguage.Bulgarian || language == AppLanguage.Macedonian) return balkanMatchingCount(count, noun, language)
        if (language == AppLanguage.Greek || language == AppLanguage.Hungarian || language == AppLanguage.Albanian) return labeledMatchingCount(count, noun, language)
        if (language == AppLanguage.Estonian) return estonianMatchingCount(count, noun)
        if (language == AppLanguage.Lithuanian) return lithuanianMatchingCount(count, noun)
        if (language == AppLanguage.Hindi || language == AppLanguage.Punjabi || language == AppLanguage.Urdu) return southAsianMatchingCount(count, noun, language)
        if (language == AppLanguage.Bengali) return bengaliMatchingCount(count, noun)
        if (language in setOf(AppLanguage.Spanish, AppLanguage.Portuguese, AppLanguage.Italian)) return romanceMatchingCount(count, noun, language)
        if (language != AppLanguage.Polish) return "$count $suffix"
        val adjective = when {
            count == 1 -> "pasujący"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "pasujące"
            else -> "pasujących"
        }
        return "$count $adjective ${localizedCountLabel(count, noun, language).substringAfter(' ')}"
    }
}

private val labeledSearchCountLanguages = setOf(
    AppLanguage.ChineseSimplified, AppLanguage.Japanese, AppLanguage.Korean,
    AppLanguage.Indonesian, AppLanguage.Malay, AppLanguage.Thai,
)

private fun localizedSearchCount(count: Int, noun: String, language: AppLanguage): String {
    if (language == AppLanguage.Thai) {
        val counted = when (noun) {
            "artist" -> "ศิลปิน $count คน"
            "album" -> "$count อัลบั้ม"
            "song" -> "$count เพลง"
            else -> error("Unsupported noun")
        }
        return "ผลการค้นหา: $counted"
    }
    val label = when (language) {
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.search_localizedSearchCount_1
        AppLanguage.Japanese -> JapaneseTranslations.search_localizedSearchCount_1
        AppLanguage.Korean -> KoreanTranslations.search_localizedSearchCount_1
        AppLanguage.Indonesian, AppLanguage.Malay -> "Hasil"
        else -> error("Unsupported language")
    }
    val separator = if (language == AppLanguage.ChineseSimplified || language == AppLanguage.Japanese) "：" else ": "
    return "$label$separator${localizedCountLabel(count, noun, language)}"
}

private fun romanceMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val adjective = when (language) {
        AppLanguage.Spanish -> if (count == 1) SpanishTranslations.search_romanceMatchingCount_variant_1 else SpanishTranslations.search_romanceMatchingCount_variant_2
        AppLanguage.Portuguese -> if (count == 1) PortugueseTranslations.search_romanceMatchingCount_variant_1 else PortugueseTranslations.search_romanceMatchingCount_variant_2
        AppLanguage.Italian -> if (count == 1) ItalianTranslations.search_romanceMatchingCount_variant_1 else ItalianTranslations.search_romanceMatchingCount_variant_2
        else -> error("Unsupported language")
    }
    return "${localizedCountLabel(count, noun, language)} $adjective"
}

private fun scandinavianMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val adjective = if (language == AppLanguage.Danish) "matchende" else "matchande"
    return "$count $adjective ${localizedCountLabel(count, noun, language).substringAfter(' ')}"
}

private fun eastSlavicMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val prefix = if (language == AppLanguage.Russian) "Найдено" else "Знайдено"
    return "$prefix: ${localizedCountLabel(count, noun, language)}"
}

private fun southSlavicMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val prefix = if (language == AppLanguage.Serbian) "Пронађено" else "Pronađeno"
    return "$prefix: ${localizedCountLabel(count, noun, language)}"
}

private fun balkanMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    return "Резултати: ${localizedCountLabel(count, noun, language)}"
}

private fun labeledMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val label = when (language) {
        AppLanguage.Greek -> GreekTranslations.search_labeledMatchingCount_1
        AppLanguage.Hungarian -> HungarianTranslations.search_labeledMatchingCount_1
        AppLanguage.Albanian -> AlbanianTranslations.search_labeledMatchingCount_1
        else -> error("Unsupported language")
    }
    return "$label: ${localizedCountLabel(count, noun, language)}"
}

private fun southAsianMatchingCount(count: Int, noun: String, language: AppLanguage): String {
    val label = when (language) {
        AppLanguage.Hindi -> HindiTranslations.search_southAsianMatchingCount_1
        AppLanguage.Punjabi -> PunjabiTranslations.search_southAsianMatchingCount_1
        AppLanguage.Urdu -> UrduTranslations.search_southAsianMatchingCount_1
        else -> error("Unsupported language")
    }
    return "$label: ${localizedCountLabel(count, noun, language)}"
}

internal fun searchCopy(language: AppLanguage): SearchUiCopy = when (language) {
    AppLanguage.Hindi -> HindiTranslations.search_searchCopy_1
    AppLanguage.Latvian -> LatvianTranslations.search_searchCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.search_searchCopy_1
    AppLanguage.Thai -> ThaiTranslations.search_searchCopy_1
    AppLanguage.Dutch -> DutchTranslations.search_searchCopy_1
    AppLanguage.Danish -> DanishTranslations.search_searchCopy_1
    AppLanguage.Swedish -> SwedishTranslations.search_searchCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.search_searchCopy_1
    AppLanguage.Croatian -> CroatianTranslations.search_searchCopy_1
    AppLanguage.Estonian -> EstonianTranslations.search_searchCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.search_searchCopy_1
    AppLanguage.Greek -> GreekTranslations.search_searchCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.search_searchCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.search_searchCopy_1
    AppLanguage.Catalan -> CatalanTranslations.search_searchCopy_1
    AppLanguage.Finnish -> FinnishTranslations.search_searchCopy_1
    AppLanguage.Romanian -> RomanianTranslations.search_searchCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.search_searchCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.search_searchCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.search_searchCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.search_searchCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.search_searchCopy_1
    AppLanguage.Serbian -> SerbianTranslations.search_searchCopy_1
    AppLanguage.Polish -> PolishTranslations.search_searchCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.search_searchCopy_1
    AppLanguage.Czech -> CzechTranslations.search_searchCopy_1
    AppLanguage.French -> FrenchTranslations.search_searchCopy_1
    AppLanguage.German -> GermanTranslations.search_searchCopy_1
    AppLanguage.Italian -> ItalianTranslations.search_searchCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.search_searchCopy_1
    AppLanguage.Spanish -> SpanishTranslations.search_searchCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.search_searchCopy_1
    AppLanguage.Russian -> RussianTranslations.search_searchCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.search_searchCopy_1
    AppLanguage.Slovak -> SlovakTranslations.search_searchCopy_1
    AppLanguage.Korean -> KoreanTranslations.search_searchCopy_1
    AppLanguage.Malay -> MalayTranslations.search_searchCopy_1
    AppLanguage.Bengali -> BengaliTranslations.search_searchCopy_1
    AppLanguage.Urdu -> UrduTranslations.search_searchCopy_1
    AppLanguage.English -> EnglishTranslations.search_searchCopy_1
}

internal fun searchSortModeLabel(
    mode: SearchSongSortMode,
    language: AppLanguage,
): String = when (mode) {
    SearchSongSortMode.Title -> songSortLabel(language, SongSortMode.Title)
    SearchSongSortMode.Artist -> songSortLabel(language, SongSortMode.Artist)
}

internal fun recentAdditionsEmptyMessage(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Bengali -> BengaliTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Bosnian -> BosnianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Bulgarian -> BulgarianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Catalan -> CatalanTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Croatian -> CroatianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Czech -> CzechTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Danish -> DanishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Dutch -> DutchTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.English -> EnglishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Estonian -> EstonianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Finnish -> FinnishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.French -> FrenchTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.German -> GermanTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Greek -> GreekTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Hindi -> HindiTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Hungarian -> HungarianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Indonesian -> IndonesianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Italian -> ItalianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Japanese -> JapaneseTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Korean -> KoreanTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Latvian -> LatvianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Lithuanian -> LithuanianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Macedonian -> MacedonianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Malay -> MalayTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Norwegian -> NorwegianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Polish -> PolishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Portuguese -> PortugueseTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Punjabi -> PunjabiTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Romanian -> RomanianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Russian -> RussianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Serbian -> SerbianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Slovak -> SlovakTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Slovenian -> SlovenianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Spanish -> SpanishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Swedish -> SwedishTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Thai -> ThaiTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Ukrainian -> UkrainianTranslations.search_recentAdditionsEmptyMessage_1
    AppLanguage.Urdu -> UrduTranslations.search_recentAdditionsEmptyMessage_1
}


internal data class SettingsLanguageCopy(
    val settings: String,
    val appearance: String,
    val theme: String,
    val textSize: String,
    val language: String,
    val currentlyUsed: String,
    val sound: String,
    val bassBoost: String,
    val spaciousness: String,
    val equalizer: String,
    val otherSettings: String,
    val scanLibrary: String,
    val scanLibrarySubtitle: String,
    val scan: String,
    val changelog: String,
    val footerSubtitle: String,
    val volumeNormalization: String = "Volume normalization",
    val volumeNormalizationSubtitle: String = "Reduce loudness differences between songs when supported by file metadata",
    val crossfadeTitle: String = "Crossfade",
    val crossfadeSubtitle: String = "Adjust the transition between songs",
    val managePlaylistsTitle: String = "Manage playlists",
    val managePlaylistsSubtitle: String = "Import and export your playlists",
    val onlineLyricsTitle: String = "Online lyrics",
    val onlineLyricsSubtitle: String = "Fetch lyrics from LRCLIB",
    val checkForUpdatesTitle: String = "Check for updates",
    val checkForUpdatesSubtitle: String = "Check whether a newer version is available",
)

internal data class NowPlayingBarStyleCopy(
    val title: String,
    val subtitle: String,
    val floating: String,
    val compact: String,
)

internal fun nowPlayingBarStyleCopy(language: AppLanguage): NowPlayingBarStyleCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Catalan -> CatalanTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Finnish -> FinnishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Romanian -> RomanianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Bengali -> BengaliTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Croatian -> CroatianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Czech -> CzechTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Danish -> DanishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Dutch -> DutchTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.English -> EnglishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Estonian -> EstonianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.French -> FrenchTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.German -> GermanTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Greek -> GreekTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Hindi -> HindiTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Italian -> ItalianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Korean -> KoreanTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Latvian -> LatvianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Malay -> MalayTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Polish -> PolishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Russian -> RussianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Serbian -> SerbianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Slovak -> SlovakTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Spanish -> SpanishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Swedish -> SwedishTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Thai -> ThaiTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.search_nowPlayingBarStyleCopy_1
    AppLanguage.Urdu -> UrduTranslations.search_nowPlayingBarStyleCopy_1
}

@Suppress("CyclomaticComplexMethod")
internal fun nowPlayingStyleDescription(language: AppLanguage, compact: Boolean): String = when (language) {
    AppLanguage.Bulgarian -> if (compact) BulgarianTranslations.search_nowPlayingStyleDescription_variant_1 else BulgarianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Catalan -> if (compact) CatalanTranslations.search_nowPlayingStyleDescription_variant_1 else CatalanTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Finnish -> if (compact) FinnishTranslations.search_nowPlayingStyleDescription_variant_1 else FinnishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Romanian -> if (compact) RomanianTranslations.search_nowPlayingStyleDescription_variant_1 else RomanianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Slovenian -> if (compact) SlovenianTranslations.search_nowPlayingStyleDescription_variant_1 else SlovenianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Bosnian -> if (compact) BosnianTranslations.search_nowPlayingStyleDescription_variant_1 else BosnianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Indonesian -> if (compact) IndonesianTranslations.search_nowPlayingStyleDescription_variant_1 else IndonesianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Punjabi -> if (compact) PunjabiTranslations.search_nowPlayingStyleDescription_variant_1 else PunjabiTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Albanian -> if (compact) AlbanianTranslations.search_nowPlayingStyleDescription_variant_1 else AlbanianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Bengali -> if (compact) BengaliTranslations.search_nowPlayingStyleDescription_variant_1 else BengaliTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.ChineseSimplified -> if (compact) ChineseSimplifiedTranslations.search_nowPlayingStyleDescription_variant_1 else ChineseSimplifiedTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Croatian -> if (compact) CroatianTranslations.search_nowPlayingStyleDescription_variant_1 else CroatianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Czech -> if (compact) CzechTranslations.search_nowPlayingStyleDescription_variant_1 else CzechTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Danish -> if (compact) DanishTranslations.search_nowPlayingStyleDescription_variant_1 else DanishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Dutch -> if (compact) DutchTranslations.search_nowPlayingStyleDescription_variant_1 else DutchTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.English -> if (compact) EnglishTranslations.search_nowPlayingStyleDescription_variant_1 else EnglishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Estonian -> if (compact) EstonianTranslations.search_nowPlayingStyleDescription_variant_1 else EstonianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.French -> if (compact) FrenchTranslations.search_nowPlayingStyleDescription_variant_1 else FrenchTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.German -> if (compact) GermanTranslations.search_nowPlayingStyleDescription_variant_1 else GermanTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Greek -> if (compact) GreekTranslations.search_nowPlayingStyleDescription_variant_1 else GreekTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Hindi -> if (compact) HindiTranslations.search_nowPlayingStyleDescription_variant_1 else HindiTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Hungarian -> if (compact) HungarianTranslations.search_nowPlayingStyleDescription_variant_1 else HungarianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Italian -> if (compact) ItalianTranslations.search_nowPlayingStyleDescription_variant_1 else ItalianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Japanese -> if (compact) JapaneseTranslations.search_nowPlayingStyleDescription_variant_1 else JapaneseTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Korean -> if (compact) KoreanTranslations.search_nowPlayingStyleDescription_variant_1 else KoreanTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Latvian -> if (compact) LatvianTranslations.search_nowPlayingStyleDescription_variant_1 else LatvianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Lithuanian -> if (compact) LithuanianTranslations.search_nowPlayingStyleDescription_variant_1 else LithuanianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Macedonian -> if (compact) MacedonianTranslations.search_nowPlayingStyleDescription_variant_1 else MacedonianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Malay -> if (compact) MalayTranslations.search_nowPlayingStyleDescription_variant_1 else MalayTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Norwegian -> if (compact) NorwegianTranslations.search_nowPlayingStyleDescription_variant_1 else NorwegianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Polish -> if (compact) PolishTranslations.search_nowPlayingStyleDescription_variant_1 else PolishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Portuguese -> if (compact) PortugueseTranslations.search_nowPlayingStyleDescription_variant_1 else PortugueseTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Russian -> if (compact) RussianTranslations.search_nowPlayingStyleDescription_variant_1 else RussianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Serbian -> if (compact) SerbianTranslations.search_nowPlayingStyleDescription_variant_1 else SerbianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Slovak -> if (compact) SlovakTranslations.search_nowPlayingStyleDescription_variant_1 else SlovakTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Spanish -> if (compact) SpanishTranslations.search_nowPlayingStyleDescription_variant_1 else SpanishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Swedish -> if (compact) SwedishTranslations.search_nowPlayingStyleDescription_variant_1 else SwedishTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Thai -> if (compact) ThaiTranslations.search_nowPlayingStyleDescription_variant_1 else ThaiTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Ukrainian -> if (compact) UkrainianTranslations.search_nowPlayingStyleDescription_variant_1 else UkrainianTranslations.search_nowPlayingStyleDescription_variant_2
    AppLanguage.Urdu -> if (compact) UrduTranslations.search_nowPlayingStyleDescription_variant_1 else UrduTranslations.search_nowPlayingStyleDescription_variant_2
}
