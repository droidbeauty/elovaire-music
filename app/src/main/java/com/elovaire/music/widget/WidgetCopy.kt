package elovaire.music.droidbeauty.app.widget

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.playLabel

internal data class WidgetCopy(val play: String, val pause: String, val previous: String, val next: String)

internal fun widgetCopy(language: AppLanguage): WidgetCopy {
    val controls = when (language) {
        AppLanguage.Albanian -> listOf("Pauzë", "Kënga e mëparshme", "Kënga tjetër")
        AppLanguage.Bosnian -> listOf("Pauziraj", "Prethodna pjesma", "Sljedeća pjesma")
        AppLanguage.Bulgarian -> listOf("Пауза", "Предишна песен", "Следваща песен")
        AppLanguage.Catalan -> listOf("Pausa", "Cançó anterior", "Cançó següent")
        AppLanguage.Finnish -> listOf("Tauko", "Edellinen kappale", "Seuraava kappale")
        AppLanguage.Romanian -> listOf("Pauză", "Piesa anterioară", "Piesa următoare")
        AppLanguage.Slovenian -> listOf("Premor", "Prejšnja skladba", "Naslednja skladba")
        AppLanguage.Indonesian -> listOf("Jeda", "Lagu sebelumnya", "Lagu berikutnya")
        AppLanguage.Punjabi -> listOf("ਰੋਕੋ", "ਪਿਛਲਾ ਗੀਤ", "ਅਗਲਾ ਗੀਤ")
        AppLanguage.English -> listOf("Pause", "Previous track", "Next track")
        AppLanguage.ChineseSimplified -> listOf("暂停", "上一首", "下一首")
        AppLanguage.Croatian -> listOf("Pauziraj", "Prethodna pjesma", "Sljedeća pjesma")
        AppLanguage.Czech -> listOf("Pozastavit", "Předchozí skladba", "Další skladba")
        AppLanguage.Danish -> listOf("Pause", "Forrige nummer", "Næste nummer")
        AppLanguage.Dutch -> listOf("Pauzeren", "Vorig nummer", "Volgend nummer")
        AppLanguage.Bengali -> listOf("বিরতি", "আগের গান", "পরের গান")
        AppLanguage.Estonian -> listOf("Paus", "Eelmine lugu", "Järgmine lugu")
        AppLanguage.French -> listOf("Pause", "Titre précédent", "Titre suivant")
        AppLanguage.German -> listOf("Pause", "Vorheriger Titel", "Nächster Titel")
        AppLanguage.Greek -> listOf("Παύση", "Προηγούμενο κομμάτι", "Επόμενο κομμάτι")
        AppLanguage.Hindi -> listOf("रोकें", "पिछला गीत", "अगला गीत")
        AppLanguage.Hungarian -> listOf("Szünet", "Előző szám", "Következő szám")
        AppLanguage.Italian -> listOf("Pausa", "Brano precedente", "Brano successivo")
        AppLanguage.Japanese -> listOf("一時停止", "前の曲", "次の曲")
        AppLanguage.Korean -> listOf("일시정지", "이전 곡", "다음 곡")
        AppLanguage.Latin -> listOf("Intermitte", "Carmen prius", "Carmen proximum")
        AppLanguage.Latvian -> listOf("Pauze", "Iepriekšējā dziesma", "Nākamā dziesma")
        AppLanguage.Lithuanian -> listOf("Pristabdyti", "Ankstesnis kūrinys", "Kitas kūrinys")
        AppLanguage.Malay -> listOf("Jeda", "Lagu sebelumnya", "Lagu seterusnya")
        AppLanguage.Macedonian -> listOf("Пауза", "Претходна песна", "Следна песна")
        AppLanguage.Norwegian -> listOf("Pause", "Forrige spor", "Neste spor")
        AppLanguage.Polish -> listOf("Wstrzymaj", "Poprzedni utwór", "Następny utwór")
        AppLanguage.Portuguese -> listOf("Pausa", "Faixa anterior", "Faixa seguinte")
        AppLanguage.Russian -> listOf("Пауза", "Предыдущий трек", "Следующий трек")
        AppLanguage.Slovak -> listOf("Pozastaviť", "Predchádzajúca skladba", "Nasledujúca skladba")
        AppLanguage.Serbian -> listOf("Пауза", "Претходна песма", "Следећа песма")
        AppLanguage.Spanish -> listOf("Pausar", "Canción anterior", "Canción siguiente")
        AppLanguage.Swedish -> listOf("Pausa", "Föregående låt", "Nästa låt")
        AppLanguage.Thai -> listOf("หยุดชั่วคราว", "เพลงก่อนหน้า", "เพลงถัดไป")
        AppLanguage.Ukrainian -> listOf("Пауза", "Попередній трек", "Наступний трек")
        AppLanguage.Urdu -> listOf("توقف", "پچھلا گانا", "اگلا گانا")
    }
    return WidgetCopy(playLabel(language), controls[0], controls[1], controls[2])
}
