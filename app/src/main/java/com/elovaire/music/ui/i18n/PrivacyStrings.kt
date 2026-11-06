package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal data class PrivacySafetyCopy(
    val title: String,
    val sections: List<PrivacySafetySectionCopy>,
)

internal data class PrivacySafetySectionCopy(
    val title: String,
    val body: String,
)

internal fun privacyPolicyCopy(language: AppLanguage): PrivacySafetyCopy {
    val title = when (language) {
        AppLanguage.Bulgarian -> BulgarianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Catalan -> CatalanTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Finnish -> FinnishTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Romanian -> RomanianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Slovenian -> SlovenianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Bosnian -> BosnianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Indonesian -> IndonesianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Punjabi -> PunjabiTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Polish -> PolishTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Slovak -> SlovakTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Czech -> CzechTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.German -> GermanTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.French -> FrenchTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Spanish -> SpanishTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Portuguese -> PortugueseTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Italian -> ItalianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Dutch -> DutchTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Danish -> DanishTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Swedish -> SwedishTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Norwegian -> NorwegianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Estonian -> EstonianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Latvian -> LatvianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Lithuanian -> LithuanianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Hungarian -> HungarianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Greek -> GreekTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Russian -> RussianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Ukrainian -> UkrainianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Serbian -> SerbianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Croatian -> CroatianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Macedonian -> MacedonianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Albanian -> AlbanianTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Hindi -> HindiTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Bengali -> BengaliTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Urdu -> UrduTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Japanese -> JapaneseTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Korean -> KoreanTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Thai -> ThaiTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.Malay -> MalayTranslations.privacy_privacyPolicyCopy_1
        AppLanguage.English -> EnglishTranslations.privacy_privacyPolicyCopy_1
    }
    return PrivacySafetyCopy(
        title = title,
        sections = privacyPolicySections(language),
    )
}

internal fun privacyPolicySection(title: String, body: String) = PrivacySafetySectionCopy(title, body.trimEnd('.', '。', '।'))

private fun privacyPolicySections(language: AppLanguage): List<PrivacySafetySectionCopy> {
    return when (language) {
        AppLanguage.Bosnian -> BosnianTranslations.privacy_listOf_multiline_1
        AppLanguage.Indonesian -> IndonesianTranslations.privacy_listOf_multiline_1
        AppLanguage.Punjabi -> PunjabiTranslations.privacy_listOf_multiline_1
        AppLanguage.Albanian -> AlbanianTranslations.privacy_listOf_multiline_1
        AppLanguage.Bengali -> BengaliTranslations.privacy_listOf_multiline_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.privacy_listOf_multiline_1
        AppLanguage.Croatian -> CroatianTranslations.privacy_listOf_multiline_1
        AppLanguage.Czech -> CzechTranslations.privacy_listOf_multiline_1
        AppLanguage.Danish -> DanishTranslations.privacy_listOf_multiline_1
        AppLanguage.Dutch -> DutchTranslations.privacy_listOf_multiline_1
        AppLanguage.Estonian -> EstonianTranslations.privacy_listOf_multiline_1
        AppLanguage.French -> FrenchTranslations.privacy_listOf_multiline_1
        AppLanguage.German -> GermanTranslations.privacy_listOf_multiline_1
        AppLanguage.Greek -> GreekTranslations.privacy_listOf_multiline_1
        AppLanguage.Hindi -> HindiTranslations.privacy_listOf_multiline_1
        AppLanguage.Hungarian -> HungarianTranslations.privacy_listOf_multiline_1
        AppLanguage.Italian -> ItalianTranslations.privacy_listOf_multiline_1
        AppLanguage.Japanese -> JapaneseTranslations.privacy_listOf_multiline_1
        AppLanguage.Korean -> KoreanTranslations.privacy_listOf_multiline_1
        AppLanguage.Latvian -> LatvianTranslations.privacy_listOf_multiline_1
        AppLanguage.Lithuanian -> LithuanianTranslations.privacy_listOf_multiline_1
        AppLanguage.Malay -> MalayTranslations.privacy_listOf_multiline_1
        AppLanguage.Macedonian -> MacedonianTranslations.privacy_listOf_multiline_1
        AppLanguage.Norwegian -> NorwegianTranslations.privacy_listOf_multiline_1
        AppLanguage.Polish -> PolishTranslations.privacy_listOf_multiline_1
        AppLanguage.Portuguese -> PortugueseTranslations.privacy_listOf_multiline_1
        AppLanguage.Russian -> RussianTranslations.privacy_listOf_multiline_1
        AppLanguage.Serbian -> SerbianTranslations.privacy_listOf_multiline_1
        AppLanguage.Slovak -> SlovakTranslations.privacy_listOf_multiline_1
        AppLanguage.Spanish -> SpanishTranslations.privacy_listOf_multiline_1
        AppLanguage.Swedish -> SwedishTranslations.privacy_listOf_multiline_1
        AppLanguage.Thai -> ThaiTranslations.privacy_listOf_multiline_1
        AppLanguage.Ukrainian -> UkrainianTranslations.privacy_listOf_multiline_1
        AppLanguage.Urdu -> UrduTranslations.privacy_listOf_multiline_1
        AppLanguage.Bulgarian -> BulgarianTranslations.privacy_listOf_multiline_1
        AppLanguage.Catalan -> CatalanTranslations.privacy_listOf_multiline_1
        AppLanguage.Romanian -> RomanianTranslations.privacy_listOf_multiline_1
        AppLanguage.Finnish -> FinnishTranslations.privacy_listOf_multiline_1
        AppLanguage.Slovenian -> SlovenianTranslations.privacy_listOf_multiline_1
        AppLanguage.English -> EnglishTranslations.privacy_listOf_multiline_1
    }
}

internal data class LibraryFoldersCopy(
    val title: String,
    val subtitle: String,
    val addFolder: String,
    val edit: String,
    val done: String,
    val refresh: String,
    val noFoldersTitle: String,
    val noFoldersMessage: String,
    val unavailable: String,
    val unavailableSubtitle: String,
    val removalSafety: String,
)

internal fun libraryFoldersCopy(language: AppLanguage): LibraryFoldersCopy {
    fun copy(
        title: String,
        subtitle: String,
        addFolder: String,
        edit: String,
        done: String,
        refresh: String,
        noFoldersTitle: String,
        noFoldersMessage: String,
        unavailable: String,
        unavailableSubtitle: String,
        removalSafety: String,
    ) = LibraryFoldersCopy(title, subtitle, addFolder, edit, done, refresh, noFoldersTitle, noFoldersMessage, unavailable, unavailableSubtitle, removalSafety)
    return when (language) {
        AppLanguage.Bosnian -> copy(BosnianTranslations.privacy_libraryFoldersCopy_variant_1, BosnianTranslations.privacy_libraryFoldersCopy_variant_2, BosnianTranslations.privacy_libraryFoldersCopy_variant_3, BosnianTranslations.privacy_libraryFoldersCopy_variant_4, BosnianTranslations.privacy_libraryFoldersCopy_variant_5, BosnianTranslations.privacy_libraryFoldersCopy_variant_6, BosnianTranslations.privacy_libraryFoldersCopy_variant_7, BosnianTranslations.privacy_libraryFoldersCopy_variant_8, BosnianTranslations.privacy_libraryFoldersCopy_variant_9, BosnianTranslations.privacy_libraryFoldersCopy_variant_10, BosnianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Indonesian -> copy(IndonesianTranslations.privacy_libraryFoldersCopy_variant_1, IndonesianTranslations.privacy_libraryFoldersCopy_variant_2, IndonesianTranslations.privacy_libraryFoldersCopy_variant_3, IndonesianTranslations.privacy_libraryFoldersCopy_variant_4, IndonesianTranslations.privacy_libraryFoldersCopy_variant_5, IndonesianTranslations.privacy_libraryFoldersCopy_variant_6, IndonesianTranslations.privacy_libraryFoldersCopy_variant_7, IndonesianTranslations.privacy_libraryFoldersCopy_variant_8, IndonesianTranslations.privacy_libraryFoldersCopy_variant_9, IndonesianTranslations.privacy_libraryFoldersCopy_variant_10, IndonesianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Punjabi -> copy(PunjabiTranslations.privacy_libraryFoldersCopy_variant_1, PunjabiTranslations.privacy_libraryFoldersCopy_variant_2, PunjabiTranslations.privacy_libraryFoldersCopy_variant_3, PunjabiTranslations.privacy_libraryFoldersCopy_variant_4, PunjabiTranslations.privacy_libraryFoldersCopy_variant_5, PunjabiTranslations.privacy_libraryFoldersCopy_variant_6, PunjabiTranslations.privacy_libraryFoldersCopy_variant_7, PunjabiTranslations.privacy_libraryFoldersCopy_variant_8, PunjabiTranslations.privacy_libraryFoldersCopy_variant_9, PunjabiTranslations.privacy_libraryFoldersCopy_variant_10, PunjabiTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Polish -> copy(PolishTranslations.privacy_libraryFoldersCopy_variant_1, PolishTranslations.privacy_libraryFoldersCopy_variant_2, PolishTranslations.privacy_libraryFoldersCopy_variant_3, PolishTranslations.privacy_libraryFoldersCopy_variant_4, PolishTranslations.privacy_libraryFoldersCopy_variant_5, PolishTranslations.privacy_libraryFoldersCopy_variant_6, PolishTranslations.privacy_libraryFoldersCopy_variant_7, PolishTranslations.privacy_libraryFoldersCopy_variant_8, PolishTranslations.privacy_libraryFoldersCopy_variant_9, PolishTranslations.privacy_libraryFoldersCopy_variant_10, PolishTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Slovak -> copy(SlovakTranslations.privacy_libraryFoldersCopy_variant_1, SlovakTranslations.privacy_libraryFoldersCopy_variant_2, SlovakTranslations.privacy_libraryFoldersCopy_variant_3, SlovakTranslations.privacy_libraryFoldersCopy_variant_4, SlovakTranslations.privacy_libraryFoldersCopy_variant_5, SlovakTranslations.privacy_libraryFoldersCopy_variant_6, SlovakTranslations.privacy_libraryFoldersCopy_variant_7, SlovakTranslations.privacy_libraryFoldersCopy_variant_8, SlovakTranslations.privacy_libraryFoldersCopy_variant_9, SlovakTranslations.privacy_libraryFoldersCopy_variant_10, SlovakTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.ChineseSimplified -> copy(ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_1, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_2, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_3, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_4, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_5, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_6, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_7, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_8, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_9, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_10, ChineseSimplifiedTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Korean -> copy(KoreanTranslations.privacy_libraryFoldersCopy_variant_1, KoreanTranslations.privacy_libraryFoldersCopy_variant_2, KoreanTranslations.privacy_libraryFoldersCopy_variant_3, KoreanTranslations.privacy_libraryFoldersCopy_variant_4, KoreanTranslations.privacy_libraryFoldersCopy_variant_5, KoreanTranslations.privacy_libraryFoldersCopy_variant_6, KoreanTranslations.privacy_libraryFoldersCopy_variant_7, KoreanTranslations.privacy_libraryFoldersCopy_variant_8, KoreanTranslations.privacy_libraryFoldersCopy_variant_9, KoreanTranslations.privacy_libraryFoldersCopy_variant_10, KoreanTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Czech -> copy(CzechTranslations.privacy_libraryFoldersCopy_variant_1, CzechTranslations.privacy_libraryFoldersCopy_variant_2, CzechTranslations.privacy_libraryFoldersCopy_variant_3, CzechTranslations.privacy_libraryFoldersCopy_variant_4, CzechTranslations.privacy_libraryFoldersCopy_variant_5, CzechTranslations.privacy_libraryFoldersCopy_variant_6, CzechTranslations.privacy_libraryFoldersCopy_variant_7, CzechTranslations.privacy_libraryFoldersCopy_variant_8, CzechTranslations.privacy_libraryFoldersCopy_variant_9, CzechTranslations.privacy_libraryFoldersCopy_variant_10, CzechTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Lithuanian -> copy(LithuanianTranslations.privacy_libraryFoldersCopy_variant_1, LithuanianTranslations.privacy_libraryFoldersCopy_variant_2, LithuanianTranslations.privacy_libraryFoldersCopy_variant_3, LithuanianTranslations.privacy_libraryFoldersCopy_variant_4, LithuanianTranslations.privacy_libraryFoldersCopy_variant_5, LithuanianTranslations.privacy_libraryFoldersCopy_variant_6, LithuanianTranslations.privacy_libraryFoldersCopy_variant_7, LithuanianTranslations.privacy_libraryFoldersCopy_variant_8, LithuanianTranslations.privacy_libraryFoldersCopy_variant_9, LithuanianTranslations.privacy_libraryFoldersCopy_variant_10, LithuanianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Danish -> copy(DanishTranslations.privacy_libraryFoldersCopy_variant_1, DanishTranslations.privacy_libraryFoldersCopy_variant_2, DanishTranslations.privacy_libraryFoldersCopy_variant_3, DanishTranslations.privacy_libraryFoldersCopy_variant_4, DanishTranslations.privacy_libraryFoldersCopy_variant_5, DanishTranslations.privacy_libraryFoldersCopy_variant_6, DanishTranslations.privacy_libraryFoldersCopy_variant_7, DanishTranslations.privacy_libraryFoldersCopy_variant_8, DanishTranslations.privacy_libraryFoldersCopy_variant_9, DanishTranslations.privacy_libraryFoldersCopy_variant_10, DanishTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.French -> copy(FrenchTranslations.privacy_libraryFoldersCopy_variant_1, FrenchTranslations.privacy_libraryFoldersCopy_variant_2, FrenchTranslations.privacy_libraryFoldersCopy_variant_3, FrenchTranslations.privacy_libraryFoldersCopy_variant_4, FrenchTranslations.privacy_libraryFoldersCopy_variant_5, FrenchTranslations.privacy_libraryFoldersCopy_variant_6, FrenchTranslations.privacy_libraryFoldersCopy_variant_7, FrenchTranslations.privacy_libraryFoldersCopy_variant_8, FrenchTranslations.privacy_libraryFoldersCopy_variant_9, FrenchTranslations.privacy_libraryFoldersCopy_variant_10, FrenchTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.German -> copy(GermanTranslations.privacy_libraryFoldersCopy_variant_1, GermanTranslations.privacy_libraryFoldersCopy_variant_2, GermanTranslations.privacy_libraryFoldersCopy_variant_3, GermanTranslations.privacy_libraryFoldersCopy_variant_4, GermanTranslations.privacy_libraryFoldersCopy_variant_5, GermanTranslations.privacy_libraryFoldersCopy_variant_6, GermanTranslations.privacy_libraryFoldersCopy_variant_7, GermanTranslations.privacy_libraryFoldersCopy_variant_8, GermanTranslations.privacy_libraryFoldersCopy_variant_9, GermanTranslations.privacy_libraryFoldersCopy_variant_10, GermanTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Dutch -> copy(DutchTranslations.privacy_libraryFoldersCopy_variant_1, DutchTranslations.privacy_libraryFoldersCopy_variant_2, DutchTranslations.privacy_libraryFoldersCopy_variant_3, DutchTranslations.privacy_libraryFoldersCopy_variant_4, DutchTranslations.privacy_libraryFoldersCopy_variant_5, DutchTranslations.privacy_libraryFoldersCopy_variant_6, DutchTranslations.privacy_libraryFoldersCopy_variant_7, DutchTranslations.privacy_libraryFoldersCopy_variant_8, DutchTranslations.privacy_libraryFoldersCopy_variant_9, DutchTranslations.privacy_libraryFoldersCopy_variant_10, DutchTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Malay -> copy(MalayTranslations.privacy_libraryFoldersCopy_variant_1, MalayTranslations.privacy_libraryFoldersCopy_variant_2, MalayTranslations.privacy_libraryFoldersCopy_variant_3, MalayTranslations.privacy_libraryFoldersCopy_variant_4, MalayTranslations.privacy_libraryFoldersCopy_variant_5, MalayTranslations.privacy_libraryFoldersCopy_variant_6, MalayTranslations.privacy_libraryFoldersCopy_variant_7, MalayTranslations.privacy_libraryFoldersCopy_variant_8, MalayTranslations.privacy_libraryFoldersCopy_variant_9, MalayTranslations.privacy_libraryFoldersCopy_variant_10, MalayTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Norwegian -> copy(NorwegianTranslations.privacy_libraryFoldersCopy_variant_1, NorwegianTranslations.privacy_libraryFoldersCopy_variant_2, NorwegianTranslations.privacy_libraryFoldersCopy_variant_3, NorwegianTranslations.privacy_libraryFoldersCopy_variant_4, NorwegianTranslations.privacy_libraryFoldersCopy_variant_5, NorwegianTranslations.privacy_libraryFoldersCopy_variant_6, NorwegianTranslations.privacy_libraryFoldersCopy_variant_7, NorwegianTranslations.privacy_libraryFoldersCopy_variant_8, NorwegianTranslations.privacy_libraryFoldersCopy_variant_9, NorwegianTranslations.privacy_libraryFoldersCopy_variant_10, NorwegianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Swedish -> copy(SwedishTranslations.privacy_libraryFoldersCopy_variant_1, SwedishTranslations.privacy_libraryFoldersCopy_variant_2, SwedishTranslations.privacy_libraryFoldersCopy_variant_3, SwedishTranslations.privacy_libraryFoldersCopy_variant_4, SwedishTranslations.privacy_libraryFoldersCopy_variant_5, SwedishTranslations.privacy_libraryFoldersCopy_variant_6, SwedishTranslations.privacy_libraryFoldersCopy_variant_7, SwedishTranslations.privacy_libraryFoldersCopy_variant_8, SwedishTranslations.privacy_libraryFoldersCopy_variant_9, SwedishTranslations.privacy_libraryFoldersCopy_variant_10, SwedishTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Spanish -> copy(SpanishTranslations.privacy_libraryFoldersCopy_variant_1, SpanishTranslations.privacy_libraryFoldersCopy_variant_2, SpanishTranslations.privacy_libraryFoldersCopy_variant_3, SpanishTranslations.privacy_libraryFoldersCopy_variant_4, SpanishTranslations.privacy_libraryFoldersCopy_variant_5, SpanishTranslations.privacy_libraryFoldersCopy_variant_6, SpanishTranslations.privacy_libraryFoldersCopy_variant_7, SpanishTranslations.privacy_libraryFoldersCopy_variant_8, SpanishTranslations.privacy_libraryFoldersCopy_variant_9, SpanishTranslations.privacy_libraryFoldersCopy_variant_10, SpanishTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Portuguese -> copy(PortugueseTranslations.privacy_libraryFoldersCopy_variant_1, PortugueseTranslations.privacy_libraryFoldersCopy_variant_2, PortugueseTranslations.privacy_libraryFoldersCopy_variant_3, PortugueseTranslations.privacy_libraryFoldersCopy_variant_4, PortugueseTranslations.privacy_libraryFoldersCopy_variant_5, PortugueseTranslations.privacy_libraryFoldersCopy_variant_6, PortugueseTranslations.privacy_libraryFoldersCopy_variant_7, PortugueseTranslations.privacy_libraryFoldersCopy_variant_8, PortugueseTranslations.privacy_libraryFoldersCopy_variant_9, PortugueseTranslations.privacy_libraryFoldersCopy_variant_10, PortugueseTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Estonian -> copy(EstonianTranslations.privacy_libraryFoldersCopy_variant_1, EstonianTranslations.privacy_libraryFoldersCopy_variant_2, EstonianTranslations.privacy_libraryFoldersCopy_variant_3, EstonianTranslations.privacy_libraryFoldersCopy_variant_4, EstonianTranslations.privacy_libraryFoldersCopy_variant_5, EstonianTranslations.privacy_libraryFoldersCopy_variant_6, EstonianTranslations.privacy_libraryFoldersCopy_variant_7, EstonianTranslations.privacy_libraryFoldersCopy_variant_8, EstonianTranslations.privacy_libraryFoldersCopy_variant_9, EstonianTranslations.privacy_libraryFoldersCopy_variant_10, EstonianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Bengali -> copy(BengaliTranslations.privacy_libraryFoldersCopy_variant_1, BengaliTranslations.privacy_libraryFoldersCopy_variant_2, BengaliTranslations.privacy_libraryFoldersCopy_variant_3, BengaliTranslations.privacy_libraryFoldersCopy_variant_4, BengaliTranslations.privacy_libraryFoldersCopy_variant_5, BengaliTranslations.privacy_libraryFoldersCopy_variant_6, BengaliTranslations.privacy_libraryFoldersCopy_variant_7, BengaliTranslations.privacy_libraryFoldersCopy_variant_8, BengaliTranslations.privacy_libraryFoldersCopy_variant_9, BengaliTranslations.privacy_libraryFoldersCopy_variant_10, BengaliTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Greek -> copy(GreekTranslations.privacy_libraryFoldersCopy_variant_1, GreekTranslations.privacy_libraryFoldersCopy_variant_2, GreekTranslations.privacy_libraryFoldersCopy_variant_3, GreekTranslations.privacy_libraryFoldersCopy_variant_4, GreekTranslations.privacy_libraryFoldersCopy_variant_5, GreekTranslations.privacy_libraryFoldersCopy_variant_6, GreekTranslations.privacy_libraryFoldersCopy_variant_7, GreekTranslations.privacy_libraryFoldersCopy_variant_8, GreekTranslations.privacy_libraryFoldersCopy_variant_9, GreekTranslations.privacy_libraryFoldersCopy_variant_10, GreekTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Croatian -> copy(CroatianTranslations.privacy_libraryFoldersCopy_variant_1, CroatianTranslations.privacy_libraryFoldersCopy_variant_2, CroatianTranslations.privacy_libraryFoldersCopy_variant_3, CroatianTranslations.privacy_libraryFoldersCopy_variant_4, CroatianTranslations.privacy_libraryFoldersCopy_variant_5, CroatianTranslations.privacy_libraryFoldersCopy_variant_6, CroatianTranslations.privacy_libraryFoldersCopy_variant_7, CroatianTranslations.privacy_libraryFoldersCopy_variant_8, CroatianTranslations.privacy_libraryFoldersCopy_variant_9, CroatianTranslations.privacy_libraryFoldersCopy_variant_10, CroatianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Russian -> copy(RussianTranslations.privacy_libraryFoldersCopy_variant_1, RussianTranslations.privacy_libraryFoldersCopy_variant_2, RussianTranslations.privacy_libraryFoldersCopy_variant_3, RussianTranslations.privacy_libraryFoldersCopy_variant_4, RussianTranslations.privacy_libraryFoldersCopy_variant_5, RussianTranslations.privacy_libraryFoldersCopy_variant_6, RussianTranslations.privacy_libraryFoldersCopy_variant_7, RussianTranslations.privacy_libraryFoldersCopy_variant_8, RussianTranslations.privacy_libraryFoldersCopy_variant_9, RussianTranslations.privacy_libraryFoldersCopy_variant_10, RussianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Ukrainian -> copy(UkrainianTranslations.privacy_libraryFoldersCopy_variant_1, UkrainianTranslations.privacy_libraryFoldersCopy_variant_2, UkrainianTranslations.privacy_libraryFoldersCopy_variant_3, UkrainianTranslations.privacy_libraryFoldersCopy_variant_4, UkrainianTranslations.privacy_libraryFoldersCopy_variant_5, UkrainianTranslations.privacy_libraryFoldersCopy_variant_6, UkrainianTranslations.privacy_libraryFoldersCopy_variant_7, UkrainianTranslations.privacy_libraryFoldersCopy_variant_8, UkrainianTranslations.privacy_libraryFoldersCopy_variant_9, UkrainianTranslations.privacy_libraryFoldersCopy_variant_10, UkrainianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Urdu -> copy(UrduTranslations.privacy_libraryFoldersCopy_variant_1, UrduTranslations.privacy_libraryFoldersCopy_variant_2, UrduTranslations.privacy_libraryFoldersCopy_variant_3, UrduTranslations.privacy_libraryFoldersCopy_variant_4, UrduTranslations.privacy_libraryFoldersCopy_variant_5, UrduTranslations.privacy_libraryFoldersCopy_variant_6, UrduTranslations.privacy_libraryFoldersCopy_variant_7, UrduTranslations.privacy_libraryFoldersCopy_variant_8, UrduTranslations.privacy_libraryFoldersCopy_variant_9, UrduTranslations.privacy_libraryFoldersCopy_variant_10, UrduTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Latvian -> copy(LatvianTranslations.privacy_libraryFoldersCopy_variant_1, LatvianTranslations.privacy_libraryFoldersCopy_variant_2, LatvianTranslations.privacy_libraryFoldersCopy_variant_3, LatvianTranslations.privacy_libraryFoldersCopy_variant_4, LatvianTranslations.privacy_libraryFoldersCopy_variant_5, LatvianTranslations.privacy_libraryFoldersCopy_variant_6, LatvianTranslations.privacy_libraryFoldersCopy_variant_7, LatvianTranslations.privacy_libraryFoldersCopy_variant_8, LatvianTranslations.privacy_libraryFoldersCopy_variant_9, LatvianTranslations.privacy_libraryFoldersCopy_variant_10, LatvianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Italian -> copy(ItalianTranslations.privacy_libraryFoldersCopy_variant_1, ItalianTranslations.privacy_libraryFoldersCopy_variant_2, ItalianTranslations.privacy_libraryFoldersCopy_variant_3, ItalianTranslations.privacy_libraryFoldersCopy_variant_4, ItalianTranslations.privacy_libraryFoldersCopy_variant_5, ItalianTranslations.privacy_libraryFoldersCopy_variant_6, ItalianTranslations.privacy_libraryFoldersCopy_variant_7, ItalianTranslations.privacy_libraryFoldersCopy_variant_8, ItalianTranslations.privacy_libraryFoldersCopy_variant_9, ItalianTranslations.privacy_libraryFoldersCopy_variant_10, ItalianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Albanian -> copy(AlbanianTranslations.privacy_libraryFoldersCopy_variant_1, AlbanianTranslations.privacy_libraryFoldersCopy_variant_2, AlbanianTranslations.privacy_libraryFoldersCopy_variant_3, AlbanianTranslations.privacy_libraryFoldersCopy_variant_4, AlbanianTranslations.privacy_libraryFoldersCopy_variant_5, AlbanianTranslations.privacy_libraryFoldersCopy_variant_6, AlbanianTranslations.privacy_libraryFoldersCopy_variant_7, AlbanianTranslations.privacy_libraryFoldersCopy_variant_8, AlbanianTranslations.privacy_libraryFoldersCopy_variant_9, AlbanianTranslations.privacy_libraryFoldersCopy_variant_10, AlbanianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Hindi -> copy(HindiTranslations.privacy_libraryFoldersCopy_variant_1, HindiTranslations.privacy_libraryFoldersCopy_variant_2, HindiTranslations.privacy_libraryFoldersCopy_variant_3, HindiTranslations.privacy_libraryFoldersCopy_variant_4, HindiTranslations.privacy_libraryFoldersCopy_variant_5, HindiTranslations.privacy_libraryFoldersCopy_variant_6, HindiTranslations.privacy_libraryFoldersCopy_variant_7, HindiTranslations.privacy_libraryFoldersCopy_variant_8, HindiTranslations.privacy_libraryFoldersCopy_variant_9, HindiTranslations.privacy_libraryFoldersCopy_variant_10, HindiTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Hungarian -> copy(HungarianTranslations.privacy_libraryFoldersCopy_variant_1, HungarianTranslations.privacy_libraryFoldersCopy_variant_2, HungarianTranslations.privacy_libraryFoldersCopy_variant_3, HungarianTranslations.privacy_libraryFoldersCopy_variant_4, HungarianTranslations.privacy_libraryFoldersCopy_variant_5, HungarianTranslations.privacy_libraryFoldersCopy_variant_6, HungarianTranslations.privacy_libraryFoldersCopy_variant_7, HungarianTranslations.privacy_libraryFoldersCopy_variant_8, HungarianTranslations.privacy_libraryFoldersCopy_variant_9, HungarianTranslations.privacy_libraryFoldersCopy_variant_10, HungarianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Japanese -> copy(JapaneseTranslations.privacy_libraryFoldersCopy_variant_1, JapaneseTranslations.privacy_libraryFoldersCopy_variant_2, JapaneseTranslations.privacy_libraryFoldersCopy_variant_3, JapaneseTranslations.privacy_libraryFoldersCopy_variant_4, JapaneseTranslations.privacy_libraryFoldersCopy_variant_5, JapaneseTranslations.privacy_libraryFoldersCopy_variant_6, JapaneseTranslations.privacy_libraryFoldersCopy_variant_7, JapaneseTranslations.privacy_libraryFoldersCopy_variant_8, JapaneseTranslations.privacy_libraryFoldersCopy_variant_9, JapaneseTranslations.privacy_libraryFoldersCopy_variant_10, JapaneseTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Macedonian -> copy(MacedonianTranslations.privacy_libraryFoldersCopy_variant_1, MacedonianTranslations.privacy_libraryFoldersCopy_variant_2, MacedonianTranslations.privacy_libraryFoldersCopy_variant_3, MacedonianTranslations.privacy_libraryFoldersCopy_variant_4, MacedonianTranslations.privacy_libraryFoldersCopy_variant_5, MacedonianTranslations.privacy_libraryFoldersCopy_variant_6, MacedonianTranslations.privacy_libraryFoldersCopy_variant_7, MacedonianTranslations.privacy_libraryFoldersCopy_variant_8, MacedonianTranslations.privacy_libraryFoldersCopy_variant_9, MacedonianTranslations.privacy_libraryFoldersCopy_variant_10, MacedonianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Serbian -> copy(SerbianTranslations.privacy_libraryFoldersCopy_variant_1, SerbianTranslations.privacy_libraryFoldersCopy_variant_2, SerbianTranslations.privacy_libraryFoldersCopy_variant_3, SerbianTranslations.privacy_libraryFoldersCopy_variant_4, SerbianTranslations.privacy_libraryFoldersCopy_variant_5, SerbianTranslations.privacy_libraryFoldersCopy_variant_6, SerbianTranslations.privacy_libraryFoldersCopy_variant_7, SerbianTranslations.privacy_libraryFoldersCopy_variant_8, SerbianTranslations.privacy_libraryFoldersCopy_variant_9, SerbianTranslations.privacy_libraryFoldersCopy_variant_10, SerbianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Thai -> copy(ThaiTranslations.privacy_libraryFoldersCopy_variant_1, ThaiTranslations.privacy_libraryFoldersCopy_variant_2, ThaiTranslations.privacy_libraryFoldersCopy_variant_3, ThaiTranslations.privacy_libraryFoldersCopy_variant_4, ThaiTranslations.privacy_libraryFoldersCopy_variant_5, ThaiTranslations.privacy_libraryFoldersCopy_variant_6, ThaiTranslations.privacy_libraryFoldersCopy_variant_7, ThaiTranslations.privacy_libraryFoldersCopy_variant_8, ThaiTranslations.privacy_libraryFoldersCopy_variant_9, ThaiTranslations.privacy_libraryFoldersCopy_variant_10, ThaiTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Bulgarian -> copy(BulgarianTranslations.privacy_libraryFoldersCopy_variant_1, BulgarianTranslations.privacy_libraryFoldersCopy_variant_2, BulgarianTranslations.privacy_libraryFoldersCopy_variant_3, BulgarianTranslations.privacy_libraryFoldersCopy_variant_4, BulgarianTranslations.privacy_libraryFoldersCopy_variant_5, BulgarianTranslations.privacy_libraryFoldersCopy_variant_6, BulgarianTranslations.privacy_libraryFoldersCopy_variant_7, BulgarianTranslations.privacy_libraryFoldersCopy_variant_8, BulgarianTranslations.privacy_libraryFoldersCopy_variant_9, BulgarianTranslations.privacy_libraryFoldersCopy_variant_10, BulgarianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Catalan -> copy(CatalanTranslations.privacy_libraryFoldersCopy_variant_1, CatalanTranslations.privacy_libraryFoldersCopy_variant_2, CatalanTranslations.privacy_libraryFoldersCopy_variant_3, CatalanTranslations.privacy_libraryFoldersCopy_variant_4, CatalanTranslations.privacy_libraryFoldersCopy_variant_5, CatalanTranslations.privacy_libraryFoldersCopy_variant_6, CatalanTranslations.privacy_libraryFoldersCopy_variant_7, CatalanTranslations.privacy_libraryFoldersCopy_variant_8, CatalanTranslations.privacy_libraryFoldersCopy_variant_9, CatalanTranslations.privacy_libraryFoldersCopy_variant_10, CatalanTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Finnish -> copy(FinnishTranslations.privacy_libraryFoldersCopy_variant_1, FinnishTranslations.privacy_libraryFoldersCopy_variant_2, FinnishTranslations.privacy_libraryFoldersCopy_variant_3, FinnishTranslations.privacy_libraryFoldersCopy_variant_4, FinnishTranslations.privacy_libraryFoldersCopy_variant_5, FinnishTranslations.privacy_libraryFoldersCopy_variant_6, FinnishTranslations.privacy_libraryFoldersCopy_variant_7, FinnishTranslations.privacy_libraryFoldersCopy_variant_8, FinnishTranslations.privacy_libraryFoldersCopy_variant_9, FinnishTranslations.privacy_libraryFoldersCopy_variant_10, FinnishTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Romanian -> copy(RomanianTranslations.privacy_libraryFoldersCopy_variant_1, RomanianTranslations.privacy_libraryFoldersCopy_variant_2, RomanianTranslations.privacy_libraryFoldersCopy_variant_3, RomanianTranslations.privacy_libraryFoldersCopy_variant_4, RomanianTranslations.privacy_libraryFoldersCopy_variant_5, RomanianTranslations.privacy_libraryFoldersCopy_variant_6, RomanianTranslations.privacy_libraryFoldersCopy_variant_7, RomanianTranslations.privacy_libraryFoldersCopy_variant_8, RomanianTranslations.privacy_libraryFoldersCopy_variant_9, RomanianTranslations.privacy_libraryFoldersCopy_variant_10, RomanianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.Slovenian -> copy(SlovenianTranslations.privacy_libraryFoldersCopy_variant_1, SlovenianTranslations.privacy_libraryFoldersCopy_variant_2, SlovenianTranslations.privacy_libraryFoldersCopy_variant_3, SlovenianTranslations.privacy_libraryFoldersCopy_variant_4, SlovenianTranslations.privacy_libraryFoldersCopy_variant_5, SlovenianTranslations.privacy_libraryFoldersCopy_variant_6, SlovenianTranslations.privacy_libraryFoldersCopy_variant_7, SlovenianTranslations.privacy_libraryFoldersCopy_variant_8, SlovenianTranslations.privacy_libraryFoldersCopy_variant_9, SlovenianTranslations.privacy_libraryFoldersCopy_variant_10, SlovenianTranslations.privacy_libraryFoldersCopy_variant_11)
        AppLanguage.English -> copy(EnglishTranslations.privacy_libraryFoldersCopy_variant_1, EnglishTranslations.privacy_libraryFoldersCopy_variant_2, EnglishTranslations.privacy_libraryFoldersCopy_variant_3, EnglishTranslations.privacy_libraryFoldersCopy_variant_4, EnglishTranslations.privacy_libraryFoldersCopy_variant_5, EnglishTranslations.privacy_libraryFoldersCopy_variant_6, EnglishTranslations.privacy_libraryFoldersCopy_variant_7, EnglishTranslations.privacy_libraryFoldersCopy_variant_8, EnglishTranslations.privacy_libraryFoldersCopy_variant_9, EnglishTranslations.privacy_libraryFoldersCopy_variant_10, EnglishTranslations.privacy_libraryFoldersCopy_variant_11)
    }
}

internal data class NetworkSourcesCopy(
    val sectionTitle: String,
    val available: String,
    val checking: String,
    val signIn: String,
    val allowLocalNetwork: String,
    val addSource: String,
    val closeSourcePicker: String,
    val chooseFolderSubtitle: String,
    val nasTitle: String,
    val nasSubtitle: String,
    val removeTitle: String,
    val removeMessage: String,
    val remove: String,
    val editorTitle: String,
    val saveEditor: String,
    val name: String,
    val server: String,
    val httpsServer: String,
    val sharePath: String,
    val path: String,
    val username: String,
    val domainOptional: String,
    val password: String,
    val connectionAvailable: String,
    val allowLocalNetworkSettings: String,
    val authenticationRequired: String,
    val hostUnreachable: String,
    val checkServerPath: String,
    val sourceUnavailable: String,
    val testingConnection: String,
)

internal fun networkSourcesCopy(language: AppLanguage): NetworkSourcesCopy = when (language) {
    AppLanguage.Bulgarian -> BulgarianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Catalan -> CatalanTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Finnish -> FinnishTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Romanian -> RomanianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Albanian -> AlbanianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Bengali -> BengaliTranslations.privacy_networkSourcesCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Croatian -> CroatianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Czech -> CzechTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Danish -> DanishTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Dutch -> DutchTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Estonian -> EstonianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.French -> FrenchTranslations.privacy_networkSourcesCopy_1
    AppLanguage.German -> GermanTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Greek -> GreekTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Hindi -> HindiTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Italian -> ItalianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Korean -> KoreanTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Latvian -> LatvianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Malay -> MalayTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Polish -> PolishTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Russian -> RussianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Serbian -> SerbianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Slovak -> SlovakTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Spanish -> SpanishTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Swedish -> SwedishTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Thai -> ThaiTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.privacy_networkSourcesCopy_1
    AppLanguage.Urdu -> UrduTranslations.privacy_networkSourcesCopy_1
    AppLanguage.English -> EnglishTranslations.privacy_networkSourcesCopy_1
}
