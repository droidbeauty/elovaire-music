package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal enum class MiscPhrase {
    RecentlyAdded,
    WhatsNew,
    NoSongsYet,
    AddSongsViaEdit,
    NothingInHere,
    TapPlusToCreatePlaylist,
    Selected,
    ChooseSongs,
    AddSongs,
}

internal fun localizedSelectedCount(count: Int, noun: String, language: AppLanguage): String {
    val counted = localizedCountLabel(count, noun, language)
    when (language) {
        AppLanguage.Albanian, AppLanguage.Bengali, AppLanguage.Bosnian, AppLanguage.ChineseSimplified,
        AppLanguage.Croatian, AppLanguage.Czech, AppLanguage.Danish, AppLanguage.Dutch, AppLanguage.Estonian,
        AppLanguage.Finnish, AppLanguage.Hungarian, AppLanguage.Indonesian, AppLanguage.Latvian,
        AppLanguage.Lithuanian, AppLanguage.Macedonian, AppLanguage.Malay, AppLanguage.Norwegian,
        AppLanguage.Russian, AppLanguage.Serbian, AppLanguage.Slovenian, AppLanguage.Ukrainian, AppLanguage.Urdu -> {
            val label = miscPhrase(language, MiscPhrase.Selected).replaceFirstChar { it.titlecase() }
            return "$label: $counted"
        }
        else -> Unit
    }
    if (language == AppLanguage.Bulgarian) return "Избрано: $counted"
    if (language == AppLanguage.Hindi) return "चयनित: $counted"
    if (language == AppLanguage.Punjabi) return "ਚੋਣ: $counted"
    if (language == AppLanguage.Romanian) return "Selecție: $counted"
    val feminine = noun == "song" || noun == "track"
    val selected = when (language) {
        AppLanguage.Polish -> when {
            count == 1 -> "wybrany"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "wybrane"
            else -> "wybranych"
        }
        AppLanguage.French -> if (noun == FrenchTranslations.library_localizedSelectedCount_variant_1) {
            if (count == 1) "sélectionnée" else "sélectionnées"
        } else if (count == 1) "sélectionné" else "sélectionnés"
        AppLanguage.Italian -> if (noun == ItalianTranslations.library_localizedSelectedCount_variant_1) {
            if (count == 1) "selezionata" else "selezionate"
        } else if (count == 1) "selezionato" else "selezionati"
        AppLanguage.Spanish -> if (feminine) {
            if (count == 1) "seleccionada" else "seleccionadas"
        } else if (count == 1) "seleccionado" else "seleccionados"
        AppLanguage.Portuguese -> if (feminine) {
            if (count == 1) "selecionada" else "selecionadas"
        } else if (count == 1) "selecionado" else "selecionados"
        AppLanguage.Catalan -> if (feminine) {
            if (count == 1) "seleccionada" else "seleccionades"
        } else if (count == 1) "seleccionat" else "seleccionats"
        AppLanguage.Swedish -> if (count == 1) {
            if (noun == "album" || noun == "track") "valt" else "vald"
        } else "valda"
        AppLanguage.Slovak -> when {
            count == 1 -> if (feminine) "vybraná" else "vybraný"
            count in 2..4 -> "vybrané"
            else -> "vybraných"
        }
        AppLanguage.Greek -> if (count == 1) GreekTranslations.library_localizedSelectedCount_variant_1 else GreekTranslations.library_localizedSelectedCount_variant_2
        else -> miscPhrase(language, MiscPhrase.Selected)
    }
    return "$counted $selected"
}

internal fun miscPhrase(language: AppLanguage, phrase: MiscPhrase): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_miscPhrase(phrase)
    AppLanguage.Catalan -> CatalanTranslations.library_miscPhrase(phrase)
    AppLanguage.Finnish -> FinnishTranslations.library_miscPhrase(phrase)
    AppLanguage.Romanian -> RomanianTranslations.library_miscPhrase(phrase)
    AppLanguage.Slovenian -> SlovenianTranslations.library_miscPhrase(phrase)
    AppLanguage.Bosnian -> BosnianTranslations.library_miscPhrase(phrase)
    AppLanguage.Indonesian -> IndonesianTranslations.library_miscPhrase(phrase)
    AppLanguage.Punjabi -> PunjabiTranslations.library_miscPhrase(phrase)
    AppLanguage.Polish -> PolishTranslations.library_miscPhrase(phrase)
    AppLanguage.Albanian -> AlbanianTranslations.library_miscPhrase(phrase)
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_miscPhrase(phrase)
    AppLanguage.Croatian -> CroatianTranslations.library_miscPhrase(phrase)
    AppLanguage.Czech -> CzechTranslations.library_miscPhrase(phrase)
    AppLanguage.Danish -> DanishTranslations.library_miscPhrase(phrase)
    AppLanguage.Dutch -> DutchTranslations.library_miscPhrase(phrase)
    AppLanguage.Estonian -> EstonianTranslations.library_miscPhrase(phrase)
    AppLanguage.French -> FrenchTranslations.library_miscPhrase(phrase)
    AppLanguage.German -> GermanTranslations.library_miscPhrase(phrase)
    AppLanguage.Greek -> GreekTranslations.library_miscPhrase(phrase)
    AppLanguage.Hindi -> HindiTranslations.library_miscPhrase(phrase)
    AppLanguage.Hungarian -> HungarianTranslations.library_miscPhrase(phrase)
    AppLanguage.Italian -> ItalianTranslations.library_miscPhrase(phrase)
    AppLanguage.Japanese -> JapaneseTranslations.library_miscPhrase(phrase)
    AppLanguage.Latvian -> LatvianTranslations.library_miscPhrase(phrase)
    AppLanguage.Lithuanian -> LithuanianTranslations.library_miscPhrase(phrase)
    AppLanguage.Macedonian -> MacedonianTranslations.library_miscPhrase(phrase)
    AppLanguage.Norwegian -> NorwegianTranslations.library_miscPhrase(phrase)
    AppLanguage.Portuguese -> PortugueseTranslations.library_miscPhrase(phrase)
    AppLanguage.Russian -> RussianTranslations.library_miscPhrase(phrase)
    AppLanguage.Serbian -> SerbianTranslations.library_miscPhrase(phrase)
    AppLanguage.Spanish -> SpanishTranslations.library_miscPhrase(phrase)
    AppLanguage.Swedish -> SwedishTranslations.library_miscPhrase(phrase)
    AppLanguage.Thai -> ThaiTranslations.library_miscPhrase(phrase)
    AppLanguage.Ukrainian -> UkrainianTranslations.library_miscPhrase(phrase)
    AppLanguage.Slovak -> SlovakTranslations.library_miscPhrase(phrase)
    AppLanguage.Korean -> KoreanTranslations.library_miscPhrase(phrase)
    AppLanguage.Malay -> MalayTranslations.library_miscPhrase(phrase)
    AppLanguage.Bengali -> BengaliTranslations.library_miscPhrase(phrase)
    AppLanguage.Urdu -> UrduTranslations.library_miscPhrase(phrase)
    AppLanguage.English -> EnglishTranslations.library_miscPhrase(phrase)
}

internal data class CommonUiCopy(
    val home: String,
    val library: String,
    val playlists: String,
    val search: String,
    val welcome: String,
    val songs: String,
    val albums: String,
    val artists: String,
    val genres: String,
    val light: String,
    val dark: String,
    val system: String,
    val inYourLibrary: String,
    val inTotal: String,
    val found: String,
    val refinedFooter: String,
)

internal fun localizedAudioFormat(format: String, language: AppLanguage): String {
    if (format.isNotBlank() && format != "AUDIO") return format
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.library_localizedAudioFormat_1
        AppLanguage.Bengali -> BengaliTranslations.library_localizedAudioFormat_1
        AppLanguage.Bosnian -> BosnianTranslations.library_localizedAudioFormat_1
        AppLanguage.Bulgarian -> BulgarianTranslations.library_localizedAudioFormat_1
        AppLanguage.Catalan -> CatalanTranslations.library_localizedAudioFormat_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_localizedAudioFormat_1
        AppLanguage.Croatian -> CroatianTranslations.library_localizedAudioFormat_1
        AppLanguage.Czech -> CzechTranslations.library_localizedAudioFormat_1
        AppLanguage.Danish -> DanishTranslations.library_localizedAudioFormat_1
        AppLanguage.Dutch -> DutchTranslations.library_localizedAudioFormat_1
        AppLanguage.English -> EnglishTranslations.library_localizedAudioFormat_1
        AppLanguage.Estonian -> EstonianTranslations.library_localizedAudioFormat_1
        AppLanguage.Finnish -> FinnishTranslations.library_localizedAudioFormat_1
        AppLanguage.French -> FrenchTranslations.library_localizedAudioFormat_1
        AppLanguage.German -> GermanTranslations.library_localizedAudioFormat_1
        AppLanguage.Greek -> GreekTranslations.library_localizedAudioFormat_1
        AppLanguage.Hindi -> HindiTranslations.library_localizedAudioFormat_1
        AppLanguage.Hungarian -> HungarianTranslations.library_localizedAudioFormat_1
        AppLanguage.Indonesian -> IndonesianTranslations.library_localizedAudioFormat_1
        AppLanguage.Italian -> ItalianTranslations.library_localizedAudioFormat_1
        AppLanguage.Japanese -> JapaneseTranslations.library_localizedAudioFormat_1
        AppLanguage.Korean -> KoreanTranslations.library_localizedAudioFormat_1
        AppLanguage.Latvian -> LatvianTranslations.library_localizedAudioFormat_1
        AppLanguage.Lithuanian -> LithuanianTranslations.library_localizedAudioFormat_1
        AppLanguage.Macedonian -> MacedonianTranslations.library_localizedAudioFormat_1
        AppLanguage.Malay -> MalayTranslations.library_localizedAudioFormat_1
        AppLanguage.Norwegian -> NorwegianTranslations.library_localizedAudioFormat_1
        AppLanguage.Polish -> PolishTranslations.library_localizedAudioFormat_1
        AppLanguage.Portuguese -> PortugueseTranslations.library_localizedAudioFormat_1
        AppLanguage.Punjabi -> PunjabiTranslations.library_localizedAudioFormat_1
        AppLanguage.Romanian -> RomanianTranslations.library_localizedAudioFormat_1
        AppLanguage.Russian -> RussianTranslations.library_localizedAudioFormat_1
        AppLanguage.Serbian -> SerbianTranslations.library_localizedAudioFormat_1
        AppLanguage.Slovak -> SlovakTranslations.library_localizedAudioFormat_1
        AppLanguage.Slovenian -> SlovenianTranslations.library_localizedAudioFormat_1
        AppLanguage.Spanish -> SpanishTranslations.library_localizedAudioFormat_1
        AppLanguage.Swedish -> SwedishTranslations.library_localizedAudioFormat_1
        AppLanguage.Thai -> ThaiTranslations.library_localizedAudioFormat_1
        AppLanguage.Ukrainian -> UkrainianTranslations.library_localizedAudioFormat_1
        AppLanguage.Urdu -> UrduTranslations.library_localizedAudioFormat_1
    }
}

internal fun localizedAlbumTitle(title: String, language: AppLanguage): String {
    if (title != "Unknown Album") return title
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Bengali -> BengaliTranslations.library_localizedAlbumTitle_1
        AppLanguage.Bosnian -> BosnianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Bulgarian -> BulgarianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Catalan -> CatalanTranslations.library_localizedAlbumTitle_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_localizedAlbumTitle_1
        AppLanguage.Croatian -> CroatianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Czech -> CzechTranslations.library_localizedAlbumTitle_1
        AppLanguage.Danish -> DanishTranslations.library_localizedAlbumTitle_1
        AppLanguage.Dutch -> DutchTranslations.library_localizedAlbumTitle_1
        AppLanguage.English -> EnglishTranslations.library_localizedAlbumTitle_1
        AppLanguage.Estonian -> EstonianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Finnish -> FinnishTranslations.library_localizedAlbumTitle_1
        AppLanguage.French -> FrenchTranslations.library_localizedAlbumTitle_1
        AppLanguage.German -> GermanTranslations.library_localizedAlbumTitle_1
        AppLanguage.Greek -> GreekTranslations.library_localizedAlbumTitle_1
        AppLanguage.Hindi -> HindiTranslations.library_localizedAlbumTitle_1
        AppLanguage.Hungarian -> HungarianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Indonesian -> IndonesianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Italian -> ItalianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Japanese -> JapaneseTranslations.library_localizedAlbumTitle_1
        AppLanguage.Korean -> KoreanTranslations.library_localizedAlbumTitle_1
        AppLanguage.Latvian -> LatvianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Lithuanian -> LithuanianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Macedonian -> MacedonianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Malay -> MalayTranslations.library_localizedAlbumTitle_1
        AppLanguage.Norwegian -> NorwegianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Polish -> PolishTranslations.library_localizedAlbumTitle_1
        AppLanguage.Portuguese -> PortugueseTranslations.library_localizedAlbumTitle_1
        AppLanguage.Punjabi -> PunjabiTranslations.library_localizedAlbumTitle_1
        AppLanguage.Romanian -> RomanianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Russian -> RussianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Serbian -> SerbianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Slovak -> SlovakTranslations.library_localizedAlbumTitle_1
        AppLanguage.Slovenian -> SlovenianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Spanish -> SpanishTranslations.library_localizedAlbumTitle_1
        AppLanguage.Swedish -> SwedishTranslations.library_localizedAlbumTitle_1
        AppLanguage.Thai -> ThaiTranslations.library_localizedAlbumTitle_1
        AppLanguage.Ukrainian -> UkrainianTranslations.library_localizedAlbumTitle_1
        AppLanguage.Urdu -> UrduTranslations.library_localizedAlbumTitle_1
    }
}

internal fun localizedPlaybackSource(source: String, album: String?, playlistId: Long?, language: AppLanguage): String = when {
    playlistId != null || source == album -> if (playlistId == null) localizedAlbumTitle(source, language) else source
    source == elovaire.music.droidbeauty.app.domain.model.ALL_SONGS_PLAYBACK_SOURCE -> localizedAllSongsSource(language)
    source == elovaire.music.droidbeauty.app.domain.model.SEARCH_PLAYBACK_SOURCE -> localizedSearchResultsSource(language)
    source == elovaire.music.droidbeauty.app.domain.model.UNKNOWN_ARTIST_PLAYBACK_SOURCE -> localizedArtistName("Unknown Artist", language)
    source == elovaire.music.droidbeauty.app.domain.model.UNKNOWN_GENRE_PLAYBACK_SOURCE -> localizedGenreName("Unknown Genre", language)
    source.startsWith(elovaire.music.droidbeauty.app.domain.model.SONG_BUCKET_PLAYBACK_SOURCE_PREFIX) ->
        "${commonUiCopy(language).songs} ${source.removePrefix(elovaire.music.droidbeauty.app.domain.model.SONG_BUCKET_PLAYBACK_SOURCE_PREFIX)}"
    source.startsWith(elovaire.music.droidbeauty.app.domain.model.SMART_PLAYLIST_PLAYBACK_SOURCE_PREFIX) -> {
        val type = source.removePrefix(elovaire.music.droidbeauty.app.domain.model.SMART_PLAYLIST_PLAYBACK_SOURCE_PREFIX)
        elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType.entries
            .firstOrNull { it.name == type }
            ?.let { builtInSmartPlaylistTitle(it, language) } ?: source
    }
    else -> source
}

internal fun localizedArtistName(name: String, language: AppLanguage): String {
    if (!name.isBlank() && name != "Unknown Artist") return name
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.library_localizedArtistName_1
        AppLanguage.Bengali -> BengaliTranslations.library_localizedArtistName_1
        AppLanguage.Bosnian -> BosnianTranslations.library_localizedArtistName_1
        AppLanguage.Bulgarian -> BulgarianTranslations.library_localizedArtistName_1
        AppLanguage.Catalan -> CatalanTranslations.library_localizedArtistName_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_localizedArtistName_1
        AppLanguage.Croatian -> CroatianTranslations.library_localizedArtistName_1
        AppLanguage.Czech -> CzechTranslations.library_localizedArtistName_1
        AppLanguage.Danish -> DanishTranslations.library_localizedArtistName_1
        AppLanguage.Dutch -> DutchTranslations.library_localizedArtistName_1
        AppLanguage.English -> EnglishTranslations.library_localizedArtistName_1
        AppLanguage.Estonian -> EstonianTranslations.library_localizedArtistName_1
        AppLanguage.Finnish -> FinnishTranslations.library_localizedArtistName_1
        AppLanguage.French -> FrenchTranslations.library_localizedArtistName_1
        AppLanguage.German -> GermanTranslations.library_localizedArtistName_1
        AppLanguage.Greek -> GreekTranslations.library_localizedArtistName_1
        AppLanguage.Hindi -> HindiTranslations.library_localizedArtistName_1
        AppLanguage.Hungarian -> HungarianTranslations.library_localizedArtistName_1
        AppLanguage.Indonesian -> IndonesianTranslations.library_localizedArtistName_1
        AppLanguage.Italian -> ItalianTranslations.library_localizedArtistName_1
        AppLanguage.Japanese -> JapaneseTranslations.library_localizedArtistName_1
        AppLanguage.Korean -> KoreanTranslations.library_localizedArtistName_1
        AppLanguage.Latvian -> LatvianTranslations.library_localizedArtistName_1
        AppLanguage.Lithuanian -> LithuanianTranslations.library_localizedArtistName_1
        AppLanguage.Macedonian -> MacedonianTranslations.library_localizedArtistName_1
        AppLanguage.Malay -> MalayTranslations.library_localizedArtistName_1
        AppLanguage.Norwegian -> NorwegianTranslations.library_localizedArtistName_1
        AppLanguage.Polish -> PolishTranslations.library_localizedArtistName_1
        AppLanguage.Portuguese -> PortugueseTranslations.library_localizedArtistName_1
        AppLanguage.Punjabi -> PunjabiTranslations.library_localizedArtistName_1
        AppLanguage.Romanian -> RomanianTranslations.library_localizedArtistName_1
        AppLanguage.Russian -> RussianTranslations.library_localizedArtistName_1
        AppLanguage.Serbian -> SerbianTranslations.library_localizedArtistName_1
        AppLanguage.Slovak -> SlovakTranslations.library_localizedArtistName_1
        AppLanguage.Slovenian -> SlovenianTranslations.library_localizedArtistName_1
        AppLanguage.Spanish -> SpanishTranslations.library_localizedArtistName_1
        AppLanguage.Swedish -> SwedishTranslations.library_localizedArtistName_1
        AppLanguage.Thai -> ThaiTranslations.library_localizedArtistName_1
        AppLanguage.Ukrainian -> UkrainianTranslations.library_localizedArtistName_1
        AppLanguage.Urdu -> UrduTranslations.library_localizedArtistName_1
    }
}

internal fun unknownGenreLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_unknownGenreLabel_1
    AppLanguage.Bengali -> BengaliTranslations.library_unknownGenreLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.library_unknownGenreLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_unknownGenreLabel_1
    AppLanguage.Catalan -> CatalanTranslations.library_unknownGenreLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_unknownGenreLabel_1
    AppLanguage.Croatian -> CroatianTranslations.library_unknownGenreLabel_1
    AppLanguage.Czech -> CzechTranslations.library_unknownGenreLabel_1
    AppLanguage.Danish -> DanishTranslations.library_unknownGenreLabel_1
    AppLanguage.Dutch -> DutchTranslations.library_unknownGenreLabel_1
    AppLanguage.English -> EnglishTranslations.library_unknownGenreLabel_1
    AppLanguage.Estonian -> EstonianTranslations.library_unknownGenreLabel_1
    AppLanguage.Finnish -> FinnishTranslations.library_unknownGenreLabel_1
    AppLanguage.French -> FrenchTranslations.library_unknownGenreLabel_1
    AppLanguage.German -> GermanTranslations.library_unknownGenreLabel_1
    AppLanguage.Greek -> GreekTranslations.library_unknownGenreLabel_1
    AppLanguage.Hindi -> HindiTranslations.library_unknownGenreLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.library_unknownGenreLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_unknownGenreLabel_1
    AppLanguage.Italian -> ItalianTranslations.library_unknownGenreLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.library_unknownGenreLabel_1
    AppLanguage.Korean -> KoreanTranslations.library_unknownGenreLabel_1
    AppLanguage.Latvian -> LatvianTranslations.library_unknownGenreLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_unknownGenreLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_unknownGenreLabel_1
    AppLanguage.Malay -> MalayTranslations.library_unknownGenreLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_unknownGenreLabel_1
    AppLanguage.Polish -> PolishTranslations.library_unknownGenreLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_unknownGenreLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_unknownGenreLabel_1
    AppLanguage.Romanian -> RomanianTranslations.library_unknownGenreLabel_1
    AppLanguage.Russian -> RussianTranslations.library_unknownGenreLabel_1
    AppLanguage.Serbian -> SerbianTranslations.library_unknownGenreLabel_1
    AppLanguage.Slovak -> SlovakTranslations.library_unknownGenreLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_unknownGenreLabel_1
    AppLanguage.Spanish -> SpanishTranslations.library_unknownGenreLabel_1
    AppLanguage.Swedish -> SwedishTranslations.library_unknownGenreLabel_1
    AppLanguage.Thai -> ThaiTranslations.library_unknownGenreLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_unknownGenreLabel_1
    AppLanguage.Urdu -> UrduTranslations.library_unknownGenreLabel_1
}

internal fun localizedGenreName(genre: String, language: AppLanguage): String =
    if (genre.isBlank() || genre == "Unknown Genre") unknownGenreLabel(language) else genre

internal fun commonUiCopy(language: AppLanguage): CommonUiCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_commonUiCopy_1
    AppLanguage.Catalan -> CatalanTranslations.library_commonUiCopy_1
    AppLanguage.Finnish -> FinnishTranslations.library_commonUiCopy_1
    AppLanguage.Romanian -> RomanianTranslations.library_commonUiCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_commonUiCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.library_commonUiCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_commonUiCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_commonUiCopy_1
    AppLanguage.Polish -> PolishTranslations.library_commonUiCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.library_commonUiCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_commonUiCopy_1
    AppLanguage.Croatian -> CroatianTranslations.library_commonUiCopy_1
    AppLanguage.Czech -> CzechTranslations.library_commonUiCopy_1
    AppLanguage.Danish -> DanishTranslations.library_commonUiCopy_1
    AppLanguage.Dutch -> DutchTranslations.library_commonUiCopy_1
    AppLanguage.Estonian -> EstonianTranslations.library_commonUiCopy_1
    AppLanguage.French -> FrenchTranslations.library_commonUiCopy_1
    AppLanguage.German -> GermanTranslations.library_commonUiCopy_1
    AppLanguage.Greek -> GreekTranslations.library_commonUiCopy_1
    AppLanguage.Hindi -> HindiTranslations.library_commonUiCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.library_commonUiCopy_1
    AppLanguage.Italian -> ItalianTranslations.library_commonUiCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.library_commonUiCopy_1
    AppLanguage.Latvian -> LatvianTranslations.library_commonUiCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_commonUiCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_commonUiCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_commonUiCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_commonUiCopy_1
    AppLanguage.Russian -> RussianTranslations.library_commonUiCopy_1
    AppLanguage.Serbian -> SerbianTranslations.library_commonUiCopy_1
    AppLanguage.Spanish -> SpanishTranslations.library_commonUiCopy_1
    AppLanguage.Swedish -> SwedishTranslations.library_commonUiCopy_1
    AppLanguage.Thai -> ThaiTranslations.library_commonUiCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_commonUiCopy_1
    AppLanguage.Slovak -> SlovakTranslations.library_commonUiCopy_1
    AppLanguage.Korean -> KoreanTranslations.library_commonUiCopy_1
    AppLanguage.Malay -> MalayTranslations.library_commonUiCopy_1
    AppLanguage.Bengali -> BengaliTranslations.library_commonUiCopy_1
    AppLanguage.Urdu -> UrduTranslations.library_commonUiCopy_1
    AppLanguage.English -> EnglishTranslations.library_commonUiCopy_1
}

internal data class PlaylistMainCopy(
    val smartMixes: String,
    val autoUpdating: String,
)

internal fun playlistMainCopy(language: AppLanguage): PlaylistMainCopy = PlaylistMainCopy(
    smartMixes = smartMixesLabel(language),
    autoUpdating = autoUpdatingLabel(language),
)

private fun smartMixesLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_smartMixesLabel_1
    AppLanguage.Catalan -> CatalanTranslations.library_smartMixesLabel_1
    AppLanguage.Finnish -> FinnishTranslations.library_smartMixesLabel_1
    AppLanguage.Romanian -> RomanianTranslations.library_smartMixesLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_smartMixesLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.library_smartMixesLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_smartMixesLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_smartMixesLabel_1
    AppLanguage.Polish -> PolishTranslations.library_smartMixesLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.library_smartMixesLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_smartMixesLabel_1
    AppLanguage.Croatian -> CroatianTranslations.library_smartMixesLabel_1
    AppLanguage.Czech -> CzechTranslations.library_smartMixesLabel_1
    AppLanguage.Danish -> DanishTranslations.library_smartMixesLabel_1
    AppLanguage.Dutch -> DutchTranslations.library_smartMixesLabel_1
    AppLanguage.Estonian -> EstonianTranslations.library_smartMixesLabel_1
    AppLanguage.French -> FrenchTranslations.library_smartMixesLabel_1
    AppLanguage.German -> GermanTranslations.library_smartMixesLabel_1
    AppLanguage.Greek -> GreekTranslations.library_smartMixesLabel_1
    AppLanguage.Hindi -> HindiTranslations.library_smartMixesLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.library_smartMixesLabel_1
    AppLanguage.Italian -> ItalianTranslations.library_smartMixesLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.library_smartMixesLabel_1
    AppLanguage.Latvian -> LatvianTranslations.library_smartMixesLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_smartMixesLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_smartMixesLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_smartMixesLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_smartMixesLabel_1
    AppLanguage.Russian -> RussianTranslations.library_smartMixesLabel_1
    AppLanguage.Serbian -> SerbianTranslations.library_smartMixesLabel_1
    AppLanguage.Spanish -> SpanishTranslations.library_smartMixesLabel_1
    AppLanguage.Swedish -> SwedishTranslations.library_smartMixesLabel_1
    AppLanguage.Thai -> ThaiTranslations.library_smartMixesLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_smartMixesLabel_1
    AppLanguage.Slovak -> SlovakTranslations.library_smartMixesLabel_1
    AppLanguage.Korean -> KoreanTranslations.library_smartMixesLabel_1
    AppLanguage.Malay -> MalayTranslations.library_smartMixesLabel_1
    AppLanguage.Bengali -> BengaliTranslations.library_smartMixesLabel_1
    AppLanguage.Urdu -> UrduTranslations.library_smartMixesLabel_1
    AppLanguage.English -> EnglishTranslations.library_smartMixesLabel_1
}

private fun autoUpdatingLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Catalan -> CatalanTranslations.library_autoUpdatingLabel_1
    AppLanguage.Finnish -> FinnishTranslations.library_autoUpdatingLabel_1
    AppLanguage.Romanian -> RomanianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_autoUpdatingLabel_1
    AppLanguage.Polish -> PolishTranslations.library_autoUpdatingLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.library_autoUpdatingLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_autoUpdatingLabel_1
    AppLanguage.Croatian -> CroatianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Czech -> CzechTranslations.library_autoUpdatingLabel_1
    AppLanguage.Danish -> DanishTranslations.library_autoUpdatingLabel_1
    AppLanguage.Dutch -> DutchTranslations.library_autoUpdatingLabel_1
    AppLanguage.Estonian -> EstonianTranslations.library_autoUpdatingLabel_1
    AppLanguage.French -> FrenchTranslations.library_autoUpdatingLabel_1
    AppLanguage.German -> GermanTranslations.library_autoUpdatingLabel_1
    AppLanguage.Greek -> GreekTranslations.library_autoUpdatingLabel_1
    AppLanguage.Hindi -> HindiTranslations.library_autoUpdatingLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Italian -> ItalianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.library_autoUpdatingLabel_1
    AppLanguage.Latvian -> LatvianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_autoUpdatingLabel_1
    AppLanguage.Russian -> RussianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Serbian -> SerbianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Spanish -> SpanishTranslations.library_autoUpdatingLabel_1
    AppLanguage.Swedish -> SwedishTranslations.library_autoUpdatingLabel_1
    AppLanguage.Thai -> ThaiTranslations.library_autoUpdatingLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_autoUpdatingLabel_1
    AppLanguage.Slovak -> SlovakTranslations.library_autoUpdatingLabel_1
    AppLanguage.Korean -> KoreanTranslations.library_autoUpdatingLabel_1
    AppLanguage.Malay -> MalayTranslations.library_autoUpdatingLabel_1
    AppLanguage.Bengali -> BengaliTranslations.library_autoUpdatingLabel_1
    AppLanguage.Urdu -> UrduTranslations.library_autoUpdatingLabel_1
    AppLanguage.English -> EnglishTranslations.library_autoUpdatingLabel_1
}

internal fun smartPlaylistSortLabel(language: AppLanguage, field: SmartPlaylistSortField): String {
    return when (field) {
        SmartPlaylistSortField.Title -> sortedByLabel(language, titleName(language))
        SmartPlaylistSortField.Artist -> sortedByLabel(language, artistName(language))
        SmartPlaylistSortField.Album -> sortedByLabel(language, albumName(language))
        SmartPlaylistSortField.Genre -> sortedByLabel(language, genreName(language))
        SmartPlaylistSortField.Duration -> sortedByLabel(language, durationName(language))
        SmartPlaylistSortField.DateAdded -> sortedByLabel(language, dateAddedName(language))
        SmartPlaylistSortField.PlayCount -> sortedByLabel(language, playCountName(language))
        SmartPlaylistSortField.Random -> randomOrderLabel(language)
    }
}

private fun sortedByLabel(language: AppLanguage, field: String): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Catalan -> CatalanTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Finnish -> FinnishTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Romanian -> RomanianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Slovenian -> SlovenianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Bosnian -> BosnianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Indonesian -> IndonesianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Punjabi -> PunjabiTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Polish -> PolishTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Albanian -> AlbanianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Croatian -> CroatianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Czech -> CzechTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Danish -> DanishTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Dutch -> DutchTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Estonian -> EstonianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.French -> FrenchTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.German -> GermanTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Greek -> GreekTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Hindi -> HindiTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Hungarian -> HungarianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Italian -> ItalianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Japanese -> JapaneseTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Latvian -> LatvianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Lithuanian -> LithuanianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Macedonian -> MacedonianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Norwegian -> NorwegianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Portuguese -> PortugueseTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Russian -> RussianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Serbian -> SerbianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Spanish -> SpanishTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Swedish -> SwedishTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Thai -> ThaiTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Ukrainian -> UkrainianTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Slovak -> SlovakTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Korean -> KoreanTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Malay -> MalayTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Bengali -> BengaliTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.Urdu -> UrduTranslations.library_sortedByLabel_dynamic_1(field)
    AppLanguage.English -> EnglishTranslations.library_sortedByLabel_dynamic_1(field)
}

private fun titleName(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_titleName_1
    AppLanguage.Croatian -> CroatianTranslations.library_titleName_1
    AppLanguage.Czech -> CzechTranslations.library_titleName_1
    AppLanguage.Danish -> DanishTranslations.library_titleName_1
    AppLanguage.Dutch -> DutchTranslations.library_titleName_1
    AppLanguage.Estonian -> EstonianTranslations.library_titleName_1
    AppLanguage.French -> FrenchTranslations.library_titleName_1
    AppLanguage.German -> GermanTranslations.library_titleName_1
    AppLanguage.Greek -> GreekTranslations.library_titleName_1
    AppLanguage.Hindi -> HindiTranslations.library_titleName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_titleName_1
    AppLanguage.Italian -> ItalianTranslations.library_titleName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_titleName_1
    AppLanguage.Latvian -> LatvianTranslations.library_titleName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_titleName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_titleName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_titleName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_titleName_1
    AppLanguage.Russian -> RussianTranslations.library_titleName_1
    AppLanguage.Serbian -> SerbianTranslations.library_titleName_1
    AppLanguage.Slovak -> SlovakTranslations.library_titleName_1
    AppLanguage.Spanish -> SpanishTranslations.library_titleName_1
    AppLanguage.Swedish -> SwedishTranslations.library_titleName_1
    AppLanguage.Thai -> ThaiTranslations.library_titleName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_titleName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_titleName_1
    AppLanguage.Catalan -> CatalanTranslations.library_titleName_1
    AppLanguage.Finnish -> FinnishTranslations.library_titleName_1
    AppLanguage.Romanian -> RomanianTranslations.library_titleName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_titleName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_titleName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_titleName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_titleName_1
    AppLanguage.Polish -> PolishTranslations.library_titleName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_titleName_1
    AppLanguage.Korean -> KoreanTranslations.library_titleName_1
    AppLanguage.Malay -> MalayTranslations.library_titleName_1
    AppLanguage.Bengali -> BengaliTranslations.library_titleName_1
    AppLanguage.Urdu -> UrduTranslations.library_titleName_1
    AppLanguage.English -> EnglishTranslations.library_titleName_1
}

private fun artistName(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_artistName_1
    AppLanguage.Croatian -> CroatianTranslations.library_artistName_1
    AppLanguage.Czech -> CzechTranslations.library_artistName_1
    AppLanguage.Danish -> DanishTranslations.library_artistName_1
    AppLanguage.Dutch -> DutchTranslations.library_artistName_1
    AppLanguage.Estonian -> EstonianTranslations.library_artistName_1
    AppLanguage.French -> FrenchTranslations.library_artistName_1
    AppLanguage.German -> GermanTranslations.library_artistName_1
    AppLanguage.Greek -> GreekTranslations.library_artistName_1
    AppLanguage.Hindi -> HindiTranslations.library_artistName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_artistName_1
    AppLanguage.Italian -> ItalianTranslations.library_artistName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_artistName_1
    AppLanguage.Latvian -> LatvianTranslations.library_artistName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_artistName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_artistName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_artistName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_artistName_1
    AppLanguage.Russian -> RussianTranslations.library_artistName_1
    AppLanguage.Serbian -> SerbianTranslations.library_artistName_1
    AppLanguage.Slovak -> SlovakTranslations.library_artistName_1
    AppLanguage.Spanish -> SpanishTranslations.library_artistName_1
    AppLanguage.Swedish -> SwedishTranslations.library_artistName_1
    AppLanguage.Thai -> ThaiTranslations.library_artistName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_artistName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_artistName_1
    AppLanguage.Catalan -> CatalanTranslations.library_artistName_1
    AppLanguage.Finnish -> FinnishTranslations.library_artistName_1
    AppLanguage.Romanian -> RomanianTranslations.library_artistName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_artistName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_artistName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_artistName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_artistName_1
    AppLanguage.Polish -> PolishTranslations.library_artistName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_artistName_1
    AppLanguage.Korean -> KoreanTranslations.library_artistName_1
    AppLanguage.Malay -> MalayTranslations.library_artistName_1
    AppLanguage.Bengali -> BengaliTranslations.library_artistName_1
    AppLanguage.Urdu -> UrduTranslations.library_artistName_1
    AppLanguage.English -> EnglishTranslations.library_artistName_1
}

private fun albumName(language: AppLanguage): String = when (language) {
    AppLanguage.Bengali -> BengaliTranslations.library_albumName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_albumName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_albumName_1
    AppLanguage.Korean -> KoreanTranslations.library_albumName_1
    AppLanguage.Malay -> MalayTranslations.library_albumName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_albumName_1
    AppLanguage.Urdu -> UrduTranslations.library_albumName_1
    AppLanguage.Albanian -> AlbanianTranslations.library_albumName_1
    AppLanguage.Croatian -> CroatianTranslations.library_albumName_1
    AppLanguage.Czech -> CzechTranslations.library_albumName_1
    AppLanguage.Danish -> DanishTranslations.library_albumName_1
    AppLanguage.Dutch -> DutchTranslations.library_albumName_1
    AppLanguage.Estonian -> EstonianTranslations.library_albumName_1
    AppLanguage.French -> FrenchTranslations.library_albumName_1
    AppLanguage.German -> GermanTranslations.library_albumName_1
    AppLanguage.Greek -> GreekTranslations.library_albumName_1
    AppLanguage.Hindi -> HindiTranslations.library_albumName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_albumName_1
    AppLanguage.Italian -> ItalianTranslations.library_albumName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_albumName_1
    AppLanguage.Latvian -> LatvianTranslations.library_albumName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_albumName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_albumName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_albumName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_albumName_1
    AppLanguage.Russian -> RussianTranslations.library_albumName_1
    AppLanguage.Serbian -> SerbianTranslations.library_albumName_1
    AppLanguage.Slovak -> SlovakTranslations.library_albumName_1
    AppLanguage.Spanish -> SpanishTranslations.library_albumName_1
    AppLanguage.Swedish -> SwedishTranslations.library_albumName_1
    AppLanguage.Thai -> ThaiTranslations.library_albumName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_albumName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_albumName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_albumName_1
    AppLanguage.Catalan -> CatalanTranslations.library_albumName_1
    AppLanguage.Finnish -> FinnishTranslations.library_albumName_1
    AppLanguage.Polish -> PolishTranslations.library_albumName_1
    AppLanguage.Romanian -> RomanianTranslations.library_albumName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_albumName_1
    AppLanguage.English -> EnglishTranslations.library_albumName_1
}

private fun genreName(language: AppLanguage): String = when (language) {
    AppLanguage.Bengali -> BengaliTranslations.library_genreName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_genreName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_genreName_1
    AppLanguage.Korean -> KoreanTranslations.library_genreName_1
    AppLanguage.Malay -> MalayTranslations.library_genreName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_genreName_1
    AppLanguage.Urdu -> UrduTranslations.library_genreName_1
    AppLanguage.Albanian -> AlbanianTranslations.library_genreName_1
    AppLanguage.Croatian -> CroatianTranslations.library_genreName_1
    AppLanguage.Czech -> CzechTranslations.library_genreName_1
    AppLanguage.Danish -> DanishTranslations.library_genreName_1
    AppLanguage.Dutch -> DutchTranslations.library_genreName_1
    AppLanguage.Estonian -> EstonianTranslations.library_genreName_1
    AppLanguage.French -> FrenchTranslations.library_genreName_1
    AppLanguage.German -> GermanTranslations.library_genreName_1
    AppLanguage.Greek -> GreekTranslations.library_genreName_1
    AppLanguage.Hindi -> HindiTranslations.library_genreName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_genreName_1
    AppLanguage.Italian -> ItalianTranslations.library_genreName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_genreName_1
    AppLanguage.Latvian -> LatvianTranslations.library_genreName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_genreName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_genreName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_genreName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_genreName_1
    AppLanguage.Russian -> RussianTranslations.library_genreName_1
    AppLanguage.Serbian -> SerbianTranslations.library_genreName_1
    AppLanguage.Slovak -> SlovakTranslations.library_genreName_1
    AppLanguage.Spanish -> SpanishTranslations.library_genreName_1
    AppLanguage.Swedish -> SwedishTranslations.library_genreName_1
    AppLanguage.Thai -> ThaiTranslations.library_genreName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_genreName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_genreName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_genreName_1
    AppLanguage.Catalan -> CatalanTranslations.library_genreName_1
    AppLanguage.Finnish -> FinnishTranslations.library_genreName_1
    AppLanguage.Polish -> PolishTranslations.library_genreName_1
    AppLanguage.Romanian -> RomanianTranslations.library_genreName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_genreName_1
    AppLanguage.English -> EnglishTranslations.library_genreName_1
}

private fun durationName(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_durationName_1
    AppLanguage.Croatian -> CroatianTranslations.library_durationName_1
    AppLanguage.Czech -> CzechTranslations.library_durationName_1
    AppLanguage.Danish -> DanishTranslations.library_durationName_1
    AppLanguage.Dutch -> DutchTranslations.library_durationName_1
    AppLanguage.Estonian -> EstonianTranslations.library_durationName_1
    AppLanguage.French -> FrenchTranslations.library_durationName_1
    AppLanguage.German -> GermanTranslations.library_durationName_1
    AppLanguage.Greek -> GreekTranslations.library_durationName_1
    AppLanguage.Hindi -> HindiTranslations.library_durationName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_durationName_1
    AppLanguage.Italian -> ItalianTranslations.library_durationName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_durationName_1
    AppLanguage.Latvian -> LatvianTranslations.library_durationName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_durationName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_durationName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_durationName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_durationName_1
    AppLanguage.Russian -> RussianTranslations.library_durationName_1
    AppLanguage.Serbian -> SerbianTranslations.library_durationName_1
    AppLanguage.Slovak -> SlovakTranslations.library_durationName_1
    AppLanguage.Spanish -> SpanishTranslations.library_durationName_1
    AppLanguage.Swedish -> SwedishTranslations.library_durationName_1
    AppLanguage.Thai -> ThaiTranslations.library_durationName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_durationName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_durationName_1
    AppLanguage.Catalan -> CatalanTranslations.library_durationName_1
    AppLanguage.Finnish -> FinnishTranslations.library_durationName_1
    AppLanguage.Romanian -> RomanianTranslations.library_durationName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_durationName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_durationName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_durationName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_durationName_1
    AppLanguage.Polish -> PolishTranslations.library_durationName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_durationName_1
    AppLanguage.Korean -> KoreanTranslations.library_durationName_1
    AppLanguage.Malay -> MalayTranslations.library_durationName_1
    AppLanguage.Bengali -> BengaliTranslations.library_durationName_1
    AppLanguage.Urdu -> UrduTranslations.library_durationName_1
    AppLanguage.English -> EnglishTranslations.library_durationName_1
}

private fun dateAddedName(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_dateAddedName_1
    AppLanguage.Croatian -> CroatianTranslations.library_dateAddedName_1
    AppLanguage.Czech -> CzechTranslations.library_dateAddedName_1
    AppLanguage.Danish -> DanishTranslations.library_dateAddedName_1
    AppLanguage.Dutch -> DutchTranslations.library_dateAddedName_1
    AppLanguage.Estonian -> EstonianTranslations.library_dateAddedName_1
    AppLanguage.French -> FrenchTranslations.library_dateAddedName_1
    AppLanguage.German -> GermanTranslations.library_dateAddedName_1
    AppLanguage.Greek -> GreekTranslations.library_dateAddedName_1
    AppLanguage.Hindi -> HindiTranslations.library_dateAddedName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_dateAddedName_1
    AppLanguage.Italian -> ItalianTranslations.library_dateAddedName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_dateAddedName_1
    AppLanguage.Latvian -> LatvianTranslations.library_dateAddedName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_dateAddedName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_dateAddedName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_dateAddedName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_dateAddedName_1
    AppLanguage.Russian -> RussianTranslations.library_dateAddedName_1
    AppLanguage.Serbian -> SerbianTranslations.library_dateAddedName_1
    AppLanguage.Slovak -> SlovakTranslations.library_dateAddedName_1
    AppLanguage.Spanish -> SpanishTranslations.library_dateAddedName_1
    AppLanguage.Swedish -> SwedishTranslations.library_dateAddedName_1
    AppLanguage.Thai -> ThaiTranslations.library_dateAddedName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_dateAddedName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_dateAddedName_1
    AppLanguage.Catalan -> CatalanTranslations.library_dateAddedName_1
    AppLanguage.Finnish -> FinnishTranslations.library_dateAddedName_1
    AppLanguage.Romanian -> RomanianTranslations.library_dateAddedName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_dateAddedName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_dateAddedName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_dateAddedName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_dateAddedName_1
    AppLanguage.Polish -> PolishTranslations.library_dateAddedName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_dateAddedName_1
    AppLanguage.Korean -> KoreanTranslations.library_dateAddedName_1
    AppLanguage.Malay -> MalayTranslations.library_dateAddedName_1
    AppLanguage.Bengali -> BengaliTranslations.library_dateAddedName_1
    AppLanguage.Urdu -> UrduTranslations.library_dateAddedName_1
    AppLanguage.English -> EnglishTranslations.library_dateAddedName_1
}

private fun playCountName(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.library_playCountName_1
    AppLanguage.Croatian -> CroatianTranslations.library_playCountName_1
    AppLanguage.Czech -> CzechTranslations.library_playCountName_1
    AppLanguage.Danish -> DanishTranslations.library_playCountName_1
    AppLanguage.Dutch -> DutchTranslations.library_playCountName_1
    AppLanguage.Estonian -> EstonianTranslations.library_playCountName_1
    AppLanguage.French -> FrenchTranslations.library_playCountName_1
    AppLanguage.German -> GermanTranslations.library_playCountName_1
    AppLanguage.Greek -> GreekTranslations.library_playCountName_1
    AppLanguage.Hindi -> HindiTranslations.library_playCountName_1
    AppLanguage.Hungarian -> HungarianTranslations.library_playCountName_1
    AppLanguage.Italian -> ItalianTranslations.library_playCountName_1
    AppLanguage.Japanese -> JapaneseTranslations.library_playCountName_1
    AppLanguage.Latvian -> LatvianTranslations.library_playCountName_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_playCountName_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_playCountName_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_playCountName_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_playCountName_1
    AppLanguage.Russian -> RussianTranslations.library_playCountName_1
    AppLanguage.Serbian -> SerbianTranslations.library_playCountName_1
    AppLanguage.Slovak -> SlovakTranslations.library_playCountName_1
    AppLanguage.Spanish -> SpanishTranslations.library_playCountName_1
    AppLanguage.Swedish -> SwedishTranslations.library_playCountName_1
    AppLanguage.Thai -> ThaiTranslations.library_playCountName_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_playCountName_1
    AppLanguage.Bulgarian -> BulgarianTranslations.library_playCountName_1
    AppLanguage.Catalan -> CatalanTranslations.library_playCountName_1
    AppLanguage.Finnish -> FinnishTranslations.library_playCountName_1
    AppLanguage.Romanian -> RomanianTranslations.library_playCountName_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_playCountName_1
    AppLanguage.Bosnian -> BosnianTranslations.library_playCountName_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_playCountName_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_playCountName_1
    AppLanguage.Polish -> PolishTranslations.library_playCountName_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_playCountName_1
    AppLanguage.Korean -> KoreanTranslations.library_playCountName_1
    AppLanguage.Malay -> MalayTranslations.library_playCountName_1
    AppLanguage.Bengali -> BengaliTranslations.library_playCountName_1
    AppLanguage.Urdu -> UrduTranslations.library_playCountName_1
    AppLanguage.English -> EnglishTranslations.library_playCountName_1
}

private fun randomOrderLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.library_randomOrderLabel_1
    AppLanguage.Catalan -> CatalanTranslations.library_randomOrderLabel_1
    AppLanguage.Finnish -> FinnishTranslations.library_randomOrderLabel_1
    AppLanguage.Romanian -> RomanianTranslations.library_randomOrderLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.library_randomOrderLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.library_randomOrderLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.library_randomOrderLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.library_randomOrderLabel_1
    AppLanguage.Polish -> PolishTranslations.library_randomOrderLabel_1
    AppLanguage.Albanian -> AlbanianTranslations.library_randomOrderLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.library_randomOrderLabel_1
    AppLanguage.Croatian -> CroatianTranslations.library_randomOrderLabel_1
    AppLanguage.Czech -> CzechTranslations.library_randomOrderLabel_1
    AppLanguage.Danish -> DanishTranslations.library_randomOrderLabel_1
    AppLanguage.Dutch -> DutchTranslations.library_randomOrderLabel_1
    AppLanguage.Estonian -> EstonianTranslations.library_randomOrderLabel_1
    AppLanguage.French -> FrenchTranslations.library_randomOrderLabel_1
    AppLanguage.German -> GermanTranslations.library_randomOrderLabel_1
    AppLanguage.Greek -> GreekTranslations.library_randomOrderLabel_1
    AppLanguage.Hindi -> HindiTranslations.library_randomOrderLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.library_randomOrderLabel_1
    AppLanguage.Italian -> ItalianTranslations.library_randomOrderLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.library_randomOrderLabel_1
    AppLanguage.Latvian -> LatvianTranslations.library_randomOrderLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.library_randomOrderLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.library_randomOrderLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.library_randomOrderLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.library_randomOrderLabel_1
    AppLanguage.Russian -> RussianTranslations.library_randomOrderLabel_1
    AppLanguage.Serbian -> SerbianTranslations.library_randomOrderLabel_1
    AppLanguage.Spanish -> SpanishTranslations.library_randomOrderLabel_1
    AppLanguage.Swedish -> SwedishTranslations.library_randomOrderLabel_1
    AppLanguage.Thai -> ThaiTranslations.library_randomOrderLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.library_randomOrderLabel_1
    AppLanguage.Slovak -> SlovakTranslations.library_randomOrderLabel_1
    AppLanguage.Korean -> KoreanTranslations.library_randomOrderLabel_1
    AppLanguage.Malay -> MalayTranslations.library_randomOrderLabel_1
    AppLanguage.Bengali -> BengaliTranslations.library_randomOrderLabel_1
    AppLanguage.Urdu -> UrduTranslations.library_randomOrderLabel_1
    AppLanguage.English -> EnglishTranslations.library_randomOrderLabel_1
}

