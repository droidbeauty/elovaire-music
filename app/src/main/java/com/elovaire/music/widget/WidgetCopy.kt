package elovaire.music.droidbeauty.app.widget

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
        AppLanguage.Albanian -> listOf("Kënga e mëparshme", "Kënga tjetër")
        AppLanguage.Bosnian -> listOf("Prethodna pjesma", "Sljedeća pjesma")
        AppLanguage.Bulgarian -> listOf("Предишна песен", "Следваща песен")
        AppLanguage.Catalan -> listOf("Cançó anterior", "Cançó següent")
        AppLanguage.Finnish -> listOf("Edellinen kappale", "Seuraava kappale")
        AppLanguage.Romanian -> listOf("Piesa anterioară", "Piesa următoare")
        AppLanguage.Slovenian -> listOf("Prejšnja skladba", "Naslednja skladba")
        AppLanguage.Indonesian -> listOf("Lagu sebelumnya", "Lagu berikutnya")
        AppLanguage.Punjabi -> listOf("ਪਿਛਲਾ ਗੀਤ", "ਅਗਲਾ ਗੀਤ")
        AppLanguage.English -> listOf("Previous track", "Next track")
        AppLanguage.ChineseSimplified -> listOf("上一首", "下一首")
        AppLanguage.Croatian -> listOf("Prethodna pjesma", "Sljedeća pjesma")
        AppLanguage.Czech -> listOf("Předchozí skladba", "Další skladba")
        AppLanguage.Danish -> listOf("Forrige nummer", "Næste nummer")
        AppLanguage.Dutch -> listOf("Vorig nummer", "Volgend nummer")
        AppLanguage.Bengali -> listOf("আগের গান", "পরের গান")
        AppLanguage.Estonian -> listOf("Eelmine lugu", "Järgmine lugu")
        AppLanguage.French -> listOf("Titre précédent", "Titre suivant")
        AppLanguage.German -> listOf("Vorheriger Titel", "Nächster Titel")
        AppLanguage.Greek -> listOf("Προηγούμενο κομμάτι", "Επόμενο κομμάτι")
        AppLanguage.Hindi -> listOf("पिछला गीत", "अगला गीत")
        AppLanguage.Hungarian -> listOf("Előző szám", "Következő szám")
        AppLanguage.Italian -> listOf("Brano precedente", "Brano successivo")
        AppLanguage.Japanese -> listOf("前の曲", "次の曲")
        AppLanguage.Korean -> listOf("이전 곡", "다음 곡")
        AppLanguage.Latvian -> listOf("Iepriekšējā dziesma", "Nākamā dziesma")
        AppLanguage.Lithuanian -> listOf("Ankstesnis kūrinys", "Kitas kūrinys")
        AppLanguage.Malay -> listOf("Lagu sebelumnya", "Lagu seterusnya")
        AppLanguage.Macedonian -> listOf("Претходна песна", "Следна песна")
        AppLanguage.Norwegian -> listOf("Forrige spor", "Neste spor")
        AppLanguage.Polish -> listOf("Poprzedni utwór", "Następny utwór")
        AppLanguage.Portuguese -> listOf("Faixa anterior", "Faixa seguinte")
        AppLanguage.Russian -> listOf("Предыдущий трек", "Следующий трек")
        AppLanguage.Slovak -> listOf("Predchádzajúca skladba", "Nasledujúca skladba")
        AppLanguage.Serbian -> listOf("Претходна песма", "Следећа песма")
        AppLanguage.Spanish -> listOf("Canción anterior", "Canción siguiente")
        AppLanguage.Swedish -> listOf("Föregående låt", "Nästa låt")
        AppLanguage.Thai -> listOf("เพลงก่อนหน้า", "เพลงถัดไป")
        AppLanguage.Ukrainian -> listOf("Попередній трек", "Наступний трек")
        AppLanguage.Urdu -> listOf("پچھلا گانا", "اگلا گانا")
    }
    return WidgetCopy(playLabel(language), pauseLabel(language), controls[0], controls[1], localizedArtistName("Unknown Artist", language), externalAudioLabel(language))
}
