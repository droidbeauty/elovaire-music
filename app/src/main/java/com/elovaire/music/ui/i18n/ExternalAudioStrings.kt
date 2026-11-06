package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.playback.isExternalAudioSongId

internal fun localizedExternalAudioText(text: String, songId: Long?, language: AppLanguage): String =
    if (songId != null && isExternalAudioSongId(songId) && text == "External audio") externalAudioLabel(language) else text

internal fun externalAudioLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Bengali -> BengaliTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Catalan -> CatalanTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Croatian -> CroatianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Czech -> CzechTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Danish -> DanishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Dutch -> DutchTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.English -> EnglishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Estonian -> EstonianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Finnish -> FinnishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.French -> FrenchTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.German -> GermanTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Greek -> GreekTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Hindi -> HindiTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Italian -> ItalianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Korean -> KoreanTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Latvian -> LatvianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Malay -> MalayTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Polish -> PolishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Romanian -> RomanianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Russian -> RussianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Serbian -> SerbianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Slovak -> SlovakTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Spanish -> SpanishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Swedish -> SwedishTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Thai -> ThaiTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.externalAudio_externalAudioLabel_1
    AppLanguage.Urdu -> UrduTranslations.externalAudio_externalAudioLabel_1
}
