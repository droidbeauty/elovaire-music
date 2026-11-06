package elovaire.music.droidbeauty.app.widget

import elovaire.music.droidbeauty.app.ui.i18n.*

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.localizedArtistName
import elovaire.music.droidbeauty.app.ui.i18n.playLabel
import elovaire.music.droidbeauty.app.ui.i18n.pauseLabel
import elovaire.music.droidbeauty.app.ui.i18n.externalAudioLabel
import elovaire.music.droidbeauty.app.data.playback.isExternalAudioSongId

internal data class WidgetCopy(val play: String, val pause: String, val previous: String, val next: String, val unknownArtist: String, val externalAudio: String)

internal fun widgetTitle(state: WidgetPlaybackSnapshot?, copy: WidgetCopy): String {
    val title = state?.title?.ifBlank { "Elovaire" } ?: return "Elovaire"
    return if (title == "External audio" && state.currentSongId?.let(::isExternalAudioSongId) == true) copy.externalAudio else title
}

internal fun widgetCopy(language: AppLanguage): WidgetCopy {
    val controls = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Bosnian -> BosnianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Bulgarian -> BulgarianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Catalan -> CatalanTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Finnish -> FinnishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Romanian -> RomanianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Slovenian -> SlovenianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Indonesian -> IndonesianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Punjabi -> PunjabiTranslations.widgetCopy_widgetCopy_1
        AppLanguage.English -> EnglishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Croatian -> CroatianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Czech -> CzechTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Danish -> DanishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Dutch -> DutchTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Bengali -> BengaliTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Estonian -> EstonianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.French -> FrenchTranslations.widgetCopy_widgetCopy_1
        AppLanguage.German -> GermanTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Greek -> GreekTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Hindi -> HindiTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Hungarian -> HungarianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Italian -> ItalianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Japanese -> JapaneseTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Korean -> KoreanTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Latvian -> LatvianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Lithuanian -> LithuanianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Malay -> MalayTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Macedonian -> MacedonianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Norwegian -> NorwegianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Polish -> PolishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Portuguese -> PortugueseTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Russian -> RussianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Slovak -> SlovakTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Serbian -> SerbianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Spanish -> SpanishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Swedish -> SwedishTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Thai -> ThaiTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Ukrainian -> UkrainianTranslations.widgetCopy_widgetCopy_1
        AppLanguage.Urdu -> UrduTranslations.widgetCopy_widgetCopy_1
    }
    return WidgetCopy(playLabel(language), pauseLabel(language), controls[0], controls[1], localizedArtistName("Unknown Artist", language), externalAudioLabel(language))
}
