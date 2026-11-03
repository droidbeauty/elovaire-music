package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.playback.isExternalAudioSongId

internal fun localizedExternalAudioText(text: String, songId: Long?, language: AppLanguage): String =
    if (songId != null && isExternalAudioSongId(songId) && text == "External audio") externalAudioLabel(language) else text

internal fun externalAudioLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Audio e jashtme"
    AppLanguage.Bengali -> "বাহ্যিক অডিও"
    AppLanguage.Bosnian -> "Vanjski audio"
    AppLanguage.Bulgarian -> "Външно аудио"
    AppLanguage.Catalan -> "Àudio extern"
    AppLanguage.ChineseSimplified -> "外部音频"
    AppLanguage.Croatian -> "Vanjski zvuk"
    AppLanguage.Czech -> "Externí zvuk"
    AppLanguage.Danish -> "Ekstern lyd"
    AppLanguage.Dutch -> "Externe audio"
    AppLanguage.English -> "External audio"
    AppLanguage.Estonian -> "Väline heli"
    AppLanguage.Finnish -> "Ulkoinen äänitiedosto"
    AppLanguage.French -> "Audio externe"
    AppLanguage.German -> "Externe Audiodatei"
    AppLanguage.Greek -> "Εξωτερικός ήχος"
    AppLanguage.Hindi -> "बाहरी ऑडियो"
    AppLanguage.Hungarian -> "Külső hangfájl"
    AppLanguage.Indonesian -> "Audio eksternal"
    AppLanguage.Italian -> "Audio esterno"
    AppLanguage.Japanese -> "外部オーディオ"
    AppLanguage.Korean -> "외부 오디오"
    AppLanguage.Latin -> "Audio externum"
    AppLanguage.Latvian -> "Ārējs audio"
    AppLanguage.Lithuanian -> "Išorinis garso įrašas"
    AppLanguage.Macedonian -> "Надворешно аудио"
    AppLanguage.Malay -> "Audio luaran"
    AppLanguage.Norwegian -> "Ekstern lyd"
    AppLanguage.Polish -> "Zewnętrzny plik audio"
    AppLanguage.Portuguese -> "Áudio externo"
    AppLanguage.Punjabi -> "ਬਾਹਰੀ ਆਡੀਓ"
    AppLanguage.Romanian -> "Audio extern"
    AppLanguage.Russian -> "Внешний аудиофайл"
    AppLanguage.Serbian -> "Спољни аудио"
    AppLanguage.Slovak -> "Externý zvuk"
    AppLanguage.Slovenian -> "Zunanji zvok"
    AppLanguage.Spanish -> "Audio externo"
    AppLanguage.Swedish -> "Externt ljud"
    AppLanguage.Thai -> "เสียงภายนอก"
    AppLanguage.Ukrainian -> "Зовнішній аудіофайл"
    AppLanguage.Urdu -> "بیرونی آڈیو"
}
