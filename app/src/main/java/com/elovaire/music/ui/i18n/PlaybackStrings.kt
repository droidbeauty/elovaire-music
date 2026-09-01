package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal fun playingFromPrefix(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Odtwarzanie z"
    AppLanguage.Slovak -> "Prehráva sa z"
    AppLanguage.ChineseSimplified -> "播放来源"
    AppLanguage.Korean -> "재생 위치"
    AppLanguage.Czech -> "Přehrávání z"
    AppLanguage.Lithuanian -> "Groja iš"
    AppLanguage.Danish -> "Afspiller fra"
    AppLanguage.French -> "Lecture depuis"
    AppLanguage.German -> "Wiedergabe aus"
    AppLanguage.Dutch -> "Afspelen vanuit"
    AppLanguage.Malay -> "Dimainkan dari"
    AppLanguage.Norwegian -> "Spiller fra"
    AppLanguage.Swedish -> "Spelar från"
    AppLanguage.Spanish -> "Reproduciendo desde"
    AppLanguage.Portuguese -> "A reproduzir de"
    AppLanguage.Estonian -> "Esitamine allikast"
    AppLanguage.Bengali -> "যেখান থেকে চলছে"
    AppLanguage.Greek -> "Αναπαραγωγή από"
    AppLanguage.Croatian -> "Reprodukcija iz"
    AppLanguage.Russian -> "Воспроизведение из"
    AppLanguage.Ukrainian -> "Відтворення з"
    AppLanguage.Urdu -> "یہاں سے چل رہا ہے"
    AppLanguage.Latvian -> "Atskaņo no"
    AppLanguage.Italian -> "Riproduzione da"
    AppLanguage.Albanian -> "Duke luajtur nga"
    AppLanguage.Hindi -> "चल रहा है"
    AppLanguage.Hungarian -> "Lejátszás innen:"
    AppLanguage.Japanese -> "再生元"
    AppLanguage.Latin -> "Canitur ex"
    AppLanguage.Macedonian -> "Се репродуцира од"
    AppLanguage.Serbian -> "Репродукује се из"
    AppLanguage.Thai -> "กำลังเล่นจาก"
    AppLanguage.English -> "Playing from"
}

internal fun localizedAllSongsSource(language: AppLanguage): String = when (language) {
    AppLanguage.English -> "all songs"
    else -> commonUiCopy(language).songs.lowercase()
}

@Suppress("CyclomaticComplexMethod")
internal fun builtInSmartPlaylistTitle(type: BuiltInSmartPlaylistType, language: AppLanguage): String = when (type) {
    BuiltInSmartPlaylistType.RecentlyAdded -> miscPhrase(language, MiscPhrase.RecentlyAdded)
    BuiltInSmartPlaylistType.MostPlayed -> when (language) {
        AppLanguage.Albanian -> "Më të luajturat"
        AppLanguage.Bengali -> "সবচেয়ে বেশি শোনা"
        AppLanguage.Croatian -> "Najslušanije"
        AppLanguage.Czech -> "Nejhranější"
        AppLanguage.Danish -> "Mest afspillet"
        AppLanguage.Dutch -> "Meest afgespeeld"
        AppLanguage.Estonian -> "Enim esitatud"
        AppLanguage.Polish -> "Najczęściej odtwarzane"
        AppLanguage.German -> "Am häufigsten gespielt"
        AppLanguage.French -> "Les plus écoutés"
        AppLanguage.Spanish -> "Más reproducidos"
        AppLanguage.Portuguese -> "Mais reproduzidas"
        AppLanguage.Italian -> "Più ascoltati"
        AppLanguage.Russian -> "Самые прослушиваемые"
        AppLanguage.Ukrainian -> "Найчастіше відтворювані"
        AppLanguage.Japanese -> "よく聴く曲"
        AppLanguage.Korean -> "よく聴く曲"
        AppLanguage.ChineseSimplified -> "最常播放"
        AppLanguage.Thai -> "เล่นบ่อยที่สุด"
        AppLanguage.Greek -> "Πιο συχνά παιγμένα"
        AppLanguage.Hindi -> "सबसे ज़्यादा चलाए गए"
        AppLanguage.Hungarian -> "Legtöbbször lejátszott"
        AppLanguage.Latin -> "Saepissime canita"
        AppLanguage.Latvian -> "Visbiežāk atskaņotās"
        AppLanguage.Lithuanian -> "Dažniausiai klausomos"
        AppLanguage.Malay -> "Paling kerap dimainkan"
        AppLanguage.Macedonian -> "Најчесто пуштани"
        AppLanguage.Norwegian -> "Mest spilt"
        AppLanguage.Serbian -> "Највише пуштано"
        AppLanguage.Slovak -> "Najčastejšie prehrávané"
        AppLanguage.Swedish -> "Mest spelade"
        AppLanguage.Urdu -> "سب سے زیادہ چلائے گئے"
        AppLanguage.English -> "Most played"
    }
    BuiltInSmartPlaylistType.Favorites -> when (language) {
        AppLanguage.Albanian -> "Të preferuarat"
        AppLanguage.Bengali -> "পছন্দের"
        AppLanguage.Croatian -> "Favoriti"
        AppLanguage.Czech -> "Oblíbené"
        AppLanguage.Danish -> "Favoritter"
        AppLanguage.Dutch -> "Favorieten"
        AppLanguage.Estonian -> "Lemmikud"
        AppLanguage.Polish -> "Ulubione"
        AppLanguage.German -> "Favoriten"
        AppLanguage.French -> "Favoris"
        AppLanguage.Spanish -> "Favoritos"
        AppLanguage.Portuguese -> "Favoritas"
        AppLanguage.Italian -> "Preferiti"
        AppLanguage.Russian -> "Избранное"
        AppLanguage.Ukrainian -> "Улюблені"
        AppLanguage.Japanese -> "お気に入り"
        AppLanguage.Korean -> "즐겨찾기"
        AppLanguage.ChineseSimplified -> "收藏"
        AppLanguage.Thai -> "รายการโปรด"
        AppLanguage.Greek -> "Αγαπημένα"
        AppLanguage.Hindi -> "पसंदीदा"
        AppLanguage.Hungarian -> "Kedvencek"
        AppLanguage.Latin -> "Dilecta"
        AppLanguage.Latvian -> "Izlase"
        AppLanguage.Lithuanian -> "Mėgstamiausi"
        AppLanguage.Malay -> "Kegemaran"
        AppLanguage.Macedonian -> "Омилени"
        AppLanguage.Norwegian -> "Favoritter"
        AppLanguage.Serbian -> "Омиљено"
        AppLanguage.Slovak -> "Obľúbené"
        AppLanguage.Swedish -> "Favoriter"
        AppLanguage.Urdu -> "پسندیدہ"
        AppLanguage.English -> "Favorites"
    }
}

internal fun artistTopTracksSubtitle(count: Int, language: AppLanguage): String {
    val tracks = localizedCountLabel(count, "track", language)
    val suffix = when (language) {
        AppLanguage.Albanian -> "që i ktheheni më shpesh"
        AppLanguage.Bengali -> "যেগুলোতে আপনি সবচেয়ে বেশি ফিরে আসেন"
        AppLanguage.Croatian -> "kojima se najčešće vraćate"
        AppLanguage.Czech -> "ke kterým se nejčastěji vracíte"
        AppLanguage.Danish -> "du vender mest tilbage til"
        AppLanguage.Dutch -> "waar je het vaakst naar terugkeert"
        AppLanguage.Estonian -> "mida kuulad kõige sagedamini"
        AppLanguage.Polish -> "do których najczęściej wracasz"
        AppLanguage.German -> "zu denen du am häufigsten zurückkehrst"
        AppLanguage.French -> "que vous écoutez le plus souvent"
        AppLanguage.Spanish -> "a las que más vuelves"
        AppLanguage.Portuguese -> "às quais você mais volta"
        AppLanguage.Italian -> "a cui torni più spesso"
        AppLanguage.Russian -> "к которым вы возвращаетесь чаще всего"
        AppLanguage.Ukrainian -> "до яких ви повертаєтеся найчастіше"
        AppLanguage.Japanese -> "最もよく聴き返す曲"
        AppLanguage.Korean -> "가장 자주 다시 듣는 곡"
        AppLanguage.ChineseSimplified -> "最常重听的曲目"
        AppLanguage.Thai -> "ที่คุณกลับมาฟังบ่อยที่สุด"
        AppLanguage.Greek -> "στα οποία επιστρέφετε πιο συχνά"
        AppLanguage.Hindi -> "जिन पर आप सबसे ज़्यादा लौटते हैं"
        AppLanguage.Hungarian -> "amelyekhez a leggyakrabban tér vissza"
        AppLanguage.Latin -> "ad quos saepissime redis"
        AppLanguage.Latvian -> "pie kuriem atgriežaties visbiežāk"
        AppLanguage.Lithuanian -> "prie kurių grįžtate dažniausiai"
        AppLanguage.Malay -> "yang paling kerap anda kembali dengar"
        AppLanguage.Macedonian -> "на кои најчесто им се враќате"
        AppLanguage.Norwegian -> "du oftest kommer tilbake til"
        AppLanguage.Serbian -> "којима се најчешће враћате"
        AppLanguage.Slovak -> "ku ktorým sa najčastejšie vraciate"
        AppLanguage.Swedish -> "du oftast återkommer till"
        AppLanguage.Urdu -> "جن کی طرف آپ سب سے زیادہ لوٹتے ہیں"
        AppLanguage.English -> "you return to the most"
    }
    return "$tracks $suffix"
}

internal fun privacyPolicySettingsSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Si i trajton Elovaire skedarët, lejet, kërkesat e rrjetit dhe të dhënat e aplikacionit"
    AppLanguage.Bengali -> "Elovaire কীভাবে ফাইল, অনুমতি, নেটওয়ার্ক অনুরোধ ও অ্যাপ ডেটা পরিচালনা করে"
    AppLanguage.Croatian -> "Kako Elovaire upravlja datotekama, dozvolama, mrežnim zahtjevima i podacima aplikacije"
    AppLanguage.Czech -> "Jak Elovaire pracuje se soubory, oprávněními, síťovými požadavky a daty aplikace"
    AppLanguage.Danish -> "Sådan håndterer Elovaire filer, tilladelser, netværksanmodninger og appdata"
    AppLanguage.Dutch -> "Hoe Elovaire bestanden, machtigingen, netwerkverzoeken en appgegevens verwerkt"
    AppLanguage.Estonian -> "Kuidas Elovaire käsitleb faile, õigusi, võrgupäringuid ja rakenduse andmeid"
    AppLanguage.Polish -> "Jak Elovaire obsługuje pliki, uprawnienia, żądania sieciowe i dane aplikacji"
    AppLanguage.German -> "Wie Elovaire Dateien, Berechtigungen, Netzwerkanfragen und App-Daten verarbeitet"
    AppLanguage.French -> "Comment Elovaire gère les fichiers, autorisations, requêtes réseau et données de l’app"
    AppLanguage.Spanish -> "Cómo Elovaire gestiona archivos, permisos, solicitudes de red y datos de la app"
    AppLanguage.Portuguese -> "Como o Elovaire lida com ficheiros, permissões, pedidos de rede e dados da aplicação"
    AppLanguage.Italian -> "Come Elovaire gestisce file, autorizzazioni, richieste di rete e dati dell’app"
    AppLanguage.Russian -> "Как Elovaire обрабатывает файлы, разрешения, сетевые запросы и данные приложения"
    AppLanguage.Ukrainian -> "Як Elovaire обробляє файли, дозволи, мережеві запити й дані застосунку"
    AppLanguage.Japanese -> "Elovaire によるファイル、権限、ネットワーク要求、アプリデータの扱い"
    AppLanguage.Korean -> "Elovaire가 파일, 권한, 네트워크 요청 및 앱 데이터를 처리하는 방법"
    AppLanguage.ChineseSimplified -> "Elovaire 如何处理文件、权限、网络请求和应用数据"
    AppLanguage.Thai -> "Elovaire จัดการไฟล์ สิทธิ์ คำขอเครือข่าย และข้อมูลแอปอย่างไร"
    AppLanguage.Greek -> "Πώς το Elovaire διαχειρίζεται αρχεία, άδειες, αιτήματα δικτύου και δεδομένα εφαρμογής"
    AppLanguage.Hindi -> "Elovaire फ़ाइलों, अनुमतियों, नेटवर्क अनुरोधों और ऐप डेटा को कैसे संभालता है"
    AppLanguage.Hungarian -> "Az Elovaire fájlok, engedélyek, hálózati kérések és alkalmazásadatok kezelése"
    AppLanguage.Latin -> "Quomodo Elovaire fasciculos, licentias, petitiones retis et data applicationis tractat"
    AppLanguage.Latvian -> "Kā Elovaire apstrādā failus, atļaujas, tīkla pieprasījumus un lietotnes datus"
    AppLanguage.Lithuanian -> "Kaip Elovaire tvarko failus, leidimus, tinklo užklausas ir programos duomenis"
    AppLanguage.Malay -> "Cara Elovaire mengendalikan fail, kebenaran, permintaan rangkaian dan data aplikasi"
    AppLanguage.Macedonian -> "Како Elovaire управува со датотеки, дозволи, мрежни барања и податоци од апликацијата"
    AppLanguage.Norwegian -> "Hvordan Elovaire håndterer filer, tillatelser, nettverksforespørsler og appdata"
    AppLanguage.Serbian -> "Како Elovaire обрађује датотеке, дозволе, мрежне захтеве и податке апликације"
    AppLanguage.Slovak -> "Ako Elovaire spracúva súbory, povolenia, sieťové požiadavky a údaje aplikácie"
    AppLanguage.Swedish -> "Så här hanterar Elovaire filer, behörigheter, nätverksbegäranden och appdata"
    AppLanguage.Urdu -> "Elovaire فائلوں، اجازتوں، نیٹ ورک درخواستوں اور ایپ ڈیٹا کو کیسے سنبھالتا ہے"
    AppLanguage.English -> "How Elovaire handles files, permissions, network requests, and app data"
}

internal fun discLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Disku"
    AppLanguage.English -> "Disc"
    AppLanguage.ChineseSimplified -> "碟片"
    AppLanguage.Croatian -> "Disk"
    AppLanguage.Czech -> "Disk"
    AppLanguage.Danish -> "Disk"
    AppLanguage.Dutch -> "Schijf"
    AppLanguage.Bengali -> "ডিস্ক"
    AppLanguage.Estonian -> "Plaat"
    AppLanguage.French -> "Disque"
    AppLanguage.German -> "Disc"
    AppLanguage.Greek -> "Δίσκος"
    AppLanguage.Hindi -> "डिस्क"
    AppLanguage.Hungarian -> "Lemez"
    AppLanguage.Italian -> "Disco"
    AppLanguage.Japanese -> "ディスク"
    AppLanguage.Korean -> "디스크"
    AppLanguage.Latin -> "Discus"
    AppLanguage.Latvian -> "Disks"
    AppLanguage.Lithuanian -> "Diskas"
    AppLanguage.Malay -> "Cakera"
    AppLanguage.Macedonian -> "Диск"
    AppLanguage.Norwegian -> "Disk"
    AppLanguage.Polish -> "Płyta"
    AppLanguage.Portuguese -> "Disco"
    AppLanguage.Russian -> "Диск"
    AppLanguage.Slovak -> "Disk"
    AppLanguage.Serbian -> "Диск"
    AppLanguage.Spanish -> "Disco"
    AppLanguage.Swedish -> "Skiva"
    AppLanguage.Thai -> "ดิสก์"
    AppLanguage.Ukrainian -> "Диск"
    AppLanguage.Urdu -> "ڈسک"
}

@Suppress("CyclomaticComplexMethod")
internal fun equalizerStatusLabel(language: AppLanguage, presetName: String?): String = when {
    presetName != null -> when (language) {
        AppLanguage.Albanian -> "Caktuar në $presetName"
        AppLanguage.English -> "Set to $presetName"
        AppLanguage.ChineseSimplified -> "已设为 $presetName"
        AppLanguage.Croatian -> "Postavljeno na $presetName"
        AppLanguage.Czech -> "Nastaveno na $presetName"
        AppLanguage.Danish -> "Indstillet til $presetName"
        AppLanguage.Dutch -> "Ingesteld op $presetName"
        AppLanguage.Bengali -> "$presetName-এ সেট করা"
        AppLanguage.Estonian -> "Seadistatud: $presetName"
        AppLanguage.French -> "Réglé sur $presetName"
        AppLanguage.German -> "Auf $presetName eingestellt"
        AppLanguage.Greek -> "Ρυθμίστηκε σε $presetName"
        AppLanguage.Hindi -> "$presetName पर सेट"
        AppLanguage.Hungarian -> "Beállítva: $presetName"
        AppLanguage.Italian -> "Impostato su $presetName"
        AppLanguage.Japanese -> "$presetName に設定"
        AppLanguage.Korean -> "$presetName(으)로 설정됨"
        AppLanguage.Latin -> "Ad $presetName positum"
        AppLanguage.Latvian -> "Iestatīts uz $presetName"
        AppLanguage.Lithuanian -> "Nustatyta į $presetName"
        AppLanguage.Malay -> "Ditetapkan kepada $presetName"
        AppLanguage.Macedonian -> "Поставено на $presetName"
        AppLanguage.Norwegian -> "Satt til $presetName"
        AppLanguage.Polish -> "Ustawiono na $presetName"
        AppLanguage.Portuguese -> "Definido para $presetName"
        AppLanguage.Russian -> "Установлено: $presetName"
        AppLanguage.Slovak -> "Nastavené na $presetName"
        AppLanguage.Serbian -> "Подешено на $presetName"
        AppLanguage.Spanish -> "Establecido en $presetName"
        AppLanguage.Swedish -> "Inställd på $presetName"
        AppLanguage.Thai -> "ตั้งเป็น $presetName"
        AppLanguage.Ukrainian -> "Установлено: $presetName"
        AppLanguage.Urdu -> "$presetName پر سیٹ ہے"
    }
    else -> when (language) {
        AppLanguage.Albanian -> "Aktualisht i fikur"
        AppLanguage.English -> "Currently off"
        AppLanguage.ChineseSimplified -> "当前已关闭"
        AppLanguage.Croatian -> "Trenutno isključeno"
        AppLanguage.Czech -> "Aktuálně vypnuto"
        AppLanguage.Danish -> "Slået fra i øjeblikket"
        AppLanguage.Dutch -> "Momenteel uit"
        AppLanguage.Bengali -> "বর্তমানে বন্ধ"
        AppLanguage.Estonian -> "Praegu väljas"
        AppLanguage.French -> "Actuellement désactivé"
        AppLanguage.German -> "Derzeit aus"
        AppLanguage.Greek -> "Αυτή τη στιγμή ανενεργό"
        AppLanguage.Hindi -> "अभी बंद है"
        AppLanguage.Hungarian -> "Jelenleg kikapcsolva"
        AppLanguage.Italian -> "Attualmente disattivato"
        AppLanguage.Japanese -> "現在オフ"
        AppLanguage.Korean -> "현재 꺼짐"
        AppLanguage.Latin -> "Nunc exstinctum"
        AppLanguage.Latvian -> "Pašlaik izslēgts"
        AppLanguage.Lithuanian -> "Šiuo metu išjungta"
        AppLanguage.Malay -> "Dimatikan sekarang"
        AppLanguage.Macedonian -> "Моментално исклучено"
        AppLanguage.Norwegian -> "For øyeblikket av"
        AppLanguage.Polish -> "Obecnie wyłączony"
        AppLanguage.Portuguese -> "Atualmente desligado"
        AppLanguage.Russian -> "Сейчас выключен"
        AppLanguage.Slovak -> "Momentálne vypnuté"
        AppLanguage.Serbian -> "Тренутно искључено"
        AppLanguage.Spanish -> "Actualmente desactivado"
        AppLanguage.Swedish -> "För närvarande av"
        AppLanguage.Thai -> "ปิดอยู่ในขณะนี้"
        AppLanguage.Ukrainian -> "Зараз вимкнено"
        AppLanguage.Urdu -> "فی الحال بند ہے"
    }
}

internal fun queueTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Kolejka"
    AppLanguage.Slovak -> "Front"
    AppLanguage.ChineseSimplified -> "队列"
    AppLanguage.Korean -> "대기열"
    AppLanguage.Czech -> "Fronta"
    AppLanguage.Lithuanian -> "Eilė"
    AppLanguage.Danish -> "Kø"
    AppLanguage.French -> "File"
    AppLanguage.German -> "Warteschlange"
    AppLanguage.Dutch -> "Wachtrij"
    AppLanguage.Malay -> "Barisan"
    AppLanguage.Norwegian -> "Kø"
    AppLanguage.Swedish -> "Kö"
    AppLanguage.Spanish -> "Cola"
    AppLanguage.Portuguese -> "Fila"
    AppLanguage.Estonian -> "Järjekord"
    AppLanguage.Bengali -> "কিউ"
    AppLanguage.Greek -> "Ουρά"
    AppLanguage.Croatian -> "Red"
    AppLanguage.Russian -> "Очередь"
    AppLanguage.Ukrainian -> "Черга"
    AppLanguage.Urdu -> "قطار"
    AppLanguage.Latvian -> "Rinda"
    AppLanguage.Italian -> "Coda"
    AppLanguage.Albanian -> "Radha"
    AppLanguage.Hindi -> "कतार"
    AppLanguage.Hungarian -> "Sor"
    AppLanguage.Japanese -> "キュー"
    AppLanguage.Latin -> "Ordo"
    AppLanguage.Macedonian -> "Редица"
    AppLanguage.Serbian -> "Ред"
    AppLanguage.Thai -> "คิว"
    AppLanguage.English -> "Queue"
}

internal data class SleepTimerCopy(
    val title: String,
    val off: String,
    val endOfSong: String,
    val confirm: String,
    val close: String,
    val minuteSuffix: String = " min",
)

internal fun sleepTimerCopy(language: AppLanguage): SleepTimerCopy = when (language) {
    AppLanguage.Albanian -> SleepTimerCopy("Kohëmatësi i gjumit", "Fikur", "Fundi i këngës", "Konfirmo", "Mbyll kohëmatësin")
    AppLanguage.English -> SleepTimerCopy("Sleep timer", "Off", "End of song", "Confirm", "Close sleep timer")
    AppLanguage.ChineseSimplified -> SleepTimerCopy("睡眠定时器", "关闭", "歌曲结束时", "确认", "关闭睡眠定时器", " 分钟")
    AppLanguage.Croatian -> SleepTimerCopy("Mjerač vremena za spavanje", "Isključeno", "Kraj pjesme", "Potvrdi", "Zatvori mjerač vremena")
    AppLanguage.Czech -> SleepTimerCopy("Časovač vypnutí", "Vypnuto", "Konec skladby", "Potvrdit", "Zavřít časovač")
    AppLanguage.Danish -> SleepTimerCopy("Sleep-timer", "Fra", "Slutningen af sangen", "Bekræft", "Luk sleep-timer")
    AppLanguage.Dutch -> SleepTimerCopy("Slaaptimer", "Uit", "Einde van nummer", "Bevestigen", "Slaaptimer sluiten")
    AppLanguage.Bengali -> SleepTimerCopy("স্লিপ টাইমার", "বন্ধ", "গানের শেষে", "নিশ্চিত করুন", "স্লিপ টাইমার বন্ধ করুন", " মিনিট")
    AppLanguage.Estonian -> SleepTimerCopy("Unetaimer", "Väljas", "Loo lõpus", "Kinnita", "Sulge unetaimer")
    AppLanguage.French -> SleepTimerCopy("Minuteur de veille", "Désactivé", "Fin du morceau", "Confirmer", "Fermer le minuteur")
    AppLanguage.German -> SleepTimerCopy("Sleep-Timer", "Aus", "Titelende", "Bestätigen", "Sleep-Timer schließen")
    AppLanguage.Greek -> SleepTimerCopy("Χρονοδιακόπτης ύπνου", "Ανενεργό", "Τέλος τραγουδιού", "Επιβεβαίωση", "Κλείσιμο χρονοδιακόπτη")
    AppLanguage.Hindi -> SleepTimerCopy("स्लीप टाइमर", "बंद", "गाने के अंत में", "पुष्टि करें", "स्लीप टाइमर बंद करें", " मिनट")
    AppLanguage.Hungarian -> SleepTimerCopy("Elalvás-időzítő", "Ki", "Dal végén", "Megerősítés", "Időzítő bezárása")
    AppLanguage.Italian -> SleepTimerCopy("Timer di spegnimento", "Disattivato", "Fine del brano", "Conferma", "Chiudi timer")
    AppLanguage.Japanese -> SleepTimerCopy("スリープタイマー", "オフ", "曲の終了時", "確認", "スリープタイマーを閉じる", "分")
    AppLanguage.Korean -> SleepTimerCopy("취침 타이머", "꺼짐", "곡이 끝날 때", "확인", "취침 타이머 닫기", "분")
    AppLanguage.Latin -> SleepTimerCopy("Temporarium somni", "Exstinctum", "Fine cantus", "Confirma", "Temporarium claude")
    AppLanguage.Latvian -> SleepTimerCopy("Miega taimeris", "Izslēgts", "Dziesmas beigās", "Apstiprināt", "Aizvērt miega taimeri")
    AppLanguage.Lithuanian -> SleepTimerCopy("Miego laikmatis", "Išjungta", "Dainos pabaigoje", "Patvirtinti", "Uždaryti miego laikmatį")
    AppLanguage.Malay -> SleepTimerCopy("Pemasa tidur", "Mati", "Akhir lagu", "Sahkan", "Tutup pemasa tidur")
    AppLanguage.Macedonian -> SleepTimerCopy("Тајмер за спиење", "Исклучено", "Крај на песната", "Потврди", "Затвори го тајмерот")
    AppLanguage.Norwegian -> SleepTimerCopy("Innsovningstimer", "Av", "Slutten av sangen", "Bekreft", "Lukk innsovningstimer")
    AppLanguage.Polish -> SleepTimerCopy("Wyłącznik czasowy", "Wyłączony", "Koniec utworu", "Potwierdź", "Zamknij wyłącznik czasowy")
    AppLanguage.Portuguese -> SleepTimerCopy("Temporizador", "Desligado", "Fim da música", "Confirmar", "Fechar temporizador")
    AppLanguage.Russian -> SleepTimerCopy("Таймер сна", "Выкл.", "Конец песни", "Подтвердить", "Закрыть таймер сна", " мин")
    AppLanguage.Slovak -> SleepTimerCopy("Časovač spánku", "Vypnuté", "Koniec skladby", "Potvrdiť", "Zavrieť časovač")
    AppLanguage.Serbian -> SleepTimerCopy("Тајмер за спавање", "Искључено", "Крај песме", "Потврди", "Затвори тајмер")
    AppLanguage.Spanish -> SleepTimerCopy("Temporizador", "Desactivado", "Final de la canción", "Confirmar", "Cerrar temporizador")
    AppLanguage.Swedish -> SleepTimerCopy("Insomningstimer", "Av", "Slutet av låten", "Bekräfta", "Stäng insomningstimer")
    AppLanguage.Thai -> SleepTimerCopy("ตัวตั้งเวลาปิด", "ปิด", "เมื่อเพลงจบ", "ยืนยัน", "ปิดตัวตั้งเวลา", " นาที")
    AppLanguage.Ukrainian -> SleepTimerCopy("Таймер сну", "Вимкнено", "Кінець пісні", "Підтвердити", "Закрити таймер сну", " хв")
    AppLanguage.Urdu -> SleepTimerCopy("سلیپ ٹائمر", "بند", "گانے کے اختتام پر", "تصدیق کریں", "سلیپ ٹائمر بند کریں", " منٹ")
}

internal fun playLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Odtwórz"
    AppLanguage.Slovak -> "Prehrať"
    AppLanguage.ChineseSimplified -> "播放"
    AppLanguage.Croatian -> "Reproduciraj"
    AppLanguage.Korean -> "재생"
    AppLanguage.Czech -> "Přehrát"
    AppLanguage.Danish -> "Afspil"
    AppLanguage.Dutch -> "Afspelen"
    AppLanguage.Malay -> "Mainkan"
    AppLanguage.Estonian -> "Esita"
    AppLanguage.French -> "Lire"
    AppLanguage.German -> "Abspielen"
    AppLanguage.Greek -> "Αναπαραγωγή"
    AppLanguage.Bengali -> "চালান"
    AppLanguage.Hindi -> "चलाएँ"
    AppLanguage.Hungarian -> "Lejátszás"
    AppLanguage.Italian -> "Riproduci"
    AppLanguage.Japanese -> "再生"
    AppLanguage.Latin -> "Cane"
    AppLanguage.Latvian -> "Atskaņot"
    AppLanguage.Lithuanian -> "Leisti"
    AppLanguage.Macedonian -> "Пушти"
    AppLanguage.Norwegian -> "Spill av"
    AppLanguage.Portuguese -> "Reproduzir"
    AppLanguage.Russian -> "Играть"
    AppLanguage.Serbian -> "Пусти"
    AppLanguage.Spanish -> "Reproducir"
    AppLanguage.Swedish -> "Spela"
    AppLanguage.Thai -> "เล่น"
    AppLanguage.Ukrainian -> "Відтворити"
    AppLanguage.Urdu -> "چلائیں"
    AppLanguage.Albanian -> "Luaj"
    AppLanguage.English -> "Play"
}

