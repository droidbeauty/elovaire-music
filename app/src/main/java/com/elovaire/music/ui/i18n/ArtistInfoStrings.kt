package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun closeArtistInfoLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Mbyll informacionin për artistin"
    AppLanguage.Bengali -> "শিল্পীর তথ্য বন্ধ করুন"
    AppLanguage.Bosnian -> "Zatvori informacije o izvođaču"
    AppLanguage.Bulgarian -> "Затвори информацията за изпълнителя"
    AppLanguage.Catalan -> "Tanca la informació de l’artista"
    AppLanguage.ChineseSimplified -> "关闭艺人信息"
    AppLanguage.Croatian -> "Zatvori informacije o izvođaču"
    AppLanguage.Czech -> "Zavřít informace o umělci"
    AppLanguage.Danish -> "Luk kunstnerinformation"
    AppLanguage.Dutch -> "Artiestinformatie sluiten"
    AppLanguage.English -> "Close artist information"
    AppLanguage.Estonian -> "Sulge esitaja teave"
    AppLanguage.Finnish -> "Sulje esittäjän tiedot"
    AppLanguage.French -> "Fermer les informations sur l’artiste"
    AppLanguage.German -> "Interpreteninformationen schließen"
    AppLanguage.Greek -> "Κλείσιμο πληροφοριών καλλιτέχνη"
    AppLanguage.Hindi -> "कलाकार की जानकारी बंद करें"
    AppLanguage.Hungarian -> "Előadó adatainak bezárása"
    AppLanguage.Indonesian -> "Tutup informasi artis"
    AppLanguage.Italian -> "Chiudi le informazioni sull’artista"
    AppLanguage.Japanese -> "アーティスト情報を閉じる"
    AppLanguage.Korean -> "아티스트 정보 닫기"
    AppLanguage.Latin -> "Informationes de artifice claude"
    AppLanguage.Latvian -> "Aizvērt informāciju par mākslinieku"
    AppLanguage.Lithuanian -> "Uždaryti atlikėjo informaciją"
    AppLanguage.Macedonian -> "Затвори информации за изведувачот"
    AppLanguage.Malay -> "Tutup maklumat artis"
    AppLanguage.Norwegian -> "Lukk artistinformasjon"
    AppLanguage.Polish -> "Zamknij informacje o wykonawcy"
    AppLanguage.Portuguese -> "Fechar informações do artista"
    AppLanguage.Punjabi -> "ਕਲਾਕਾਰ ਦੀ ਜਾਣਕਾਰੀ ਬੰਦ ਕਰੋ"
    AppLanguage.Romanian -> "Închide informațiile despre artist"
    AppLanguage.Russian -> "Закрыть информацию об исполнителе"
    AppLanguage.Serbian -> "Затвори информације о извођачу"
    AppLanguage.Slovak -> "Zavrieť informácie o interpretovi"
    AppLanguage.Slovenian -> "Zapri podatke o izvajalcu"
    AppLanguage.Spanish -> "Cerrar información del artista"
    AppLanguage.Swedish -> "Stäng artistinformation"
    AppLanguage.Thai -> "ปิดข้อมูลศิลปิน"
    AppLanguage.Ukrainian -> "Закрити інформацію про виконавця"
    AppLanguage.Urdu -> "فنکار کی معلومات بند کریں"
}

internal data class ArtistInfoCopy(
    val buttonDescription: String,
    val discography: String,
    val loading: String,
    val unavailable: String,
    val releaseDate: String,
)

internal fun artistInfoCopy(language: AppLanguage): ArtistInfoCopy = when (language) {
    AppLanguage.Bulgarian -> ArtistInfoCopy("Информация за изпълнителя", "Дискография", "Зареждане на дискографията…", "Дискографията не е налична", "Дата на издаване")
    AppLanguage.Catalan -> ArtistInfoCopy("Informació de l’artista", "Discografia", "S’està carregant la discografia…", "La discografia no està disponible", "Data de publicació")
    AppLanguage.Finnish -> ArtistInfoCopy("Esittäjän tiedot", "Diskografia", "Ladataan diskografiaa…", "Diskografia ei ole saatavilla", "Julkaisupäivä")
    AppLanguage.Romanian -> ArtistInfoCopy("Informații despre artist", "Discografie", "Se încarcă discografia…", "Discografia nu este disponibilă", "Data lansării")
    AppLanguage.Slovenian -> ArtistInfoCopy("Podatki o izvajalcu", "Diskografija", "Nalaganje diskografije…", "Diskografija ni na voljo", "Datum izdaje")
    AppLanguage.Bosnian -> ArtistInfoCopy("Informacije o izvođaču", "Diskografija", "Učitavanje diskografije…", "Diskografija nije dostupna", "Datum izdanja")
    AppLanguage.Indonesian -> ArtistInfoCopy("Informasi artis", "Diskografi", "Memuat diskografi…", "Diskografi tidak tersedia", "Tanggal rilis")
    AppLanguage.Punjabi -> ArtistInfoCopy("ਕਲਾਕਾਰ ਦੀ ਜਾਣਕਾਰੀ", "ਡਿਸਕੋਗ੍ਰਾਫੀ", "ਡਿਸਕੋਗ੍ਰਾਫੀ ਲੋਡ ਹੋ ਰਹੀ ਹੈ…", "ਡਿਸਕੋਗ੍ਰਾਫੀ ਉਪਲਬਧ ਨਹੀਂ", "ਰਿਲੀਜ਼ ਦੀ ਮਿਤੀ")
    AppLanguage.Albanian -> ArtistInfoCopy("Informacion për artistin", "Diskografia", "Po ngarkohet diskografia…", "Diskografia nuk është e disponueshme", "Data e publikimit")
    AppLanguage.Bengali -> ArtistInfoCopy("শিল্পীর তথ্য", "ডিসকোগ্রাফি", "ডিসকোগ্রাফি লোড হচ্ছে…", "ডিসকোগ্রাফি পাওয়া যায়নি", "প্রকাশের তারিখ")
    AppLanguage.ChineseSimplified -> ArtistInfoCopy("艺人信息", "作品目录", "正在加载作品目录…", "暂无作品目录信息", "发行日期")
    AppLanguage.Croatian -> ArtistInfoCopy("Informacije o izvođaču", "Diskografija", "Učitavanje diskografije…", "Diskografija nije dostupna", "Datum izdanja")
    AppLanguage.Czech -> ArtistInfoCopy("Informace o umělci", "Diskografie", "Načítání diskografie…", "Diskografie není k dispozici", "Datum vydání")
    AppLanguage.Danish -> ArtistInfoCopy("Kunstnerinformation", "Diskografi", "Indlæser diskografi…", "Diskografi er ikke tilgængelig", "Udgivelsesdato")
    AppLanguage.Dutch -> ArtistInfoCopy("Artiestinformatie", "Discografie", "Discografie laden…", "Discografie is niet beschikbaar", "Releasedatum")
    AppLanguage.Estonian -> ArtistInfoCopy("Esitaja teave", "Diskograafia", "Diskograafia laadimine…", "Diskograafia pole saadaval", "Avaldamiskuupäev")
    AppLanguage.French -> ArtistInfoCopy("Informations sur l’artiste", "Discographie", "Chargement de la discographie…", "Discographie indisponible", "Date de sortie")
    AppLanguage.German -> ArtistInfoCopy("Künstlerinformationen", "Diskografie", "Diskografie wird geladen…", "Diskografie nicht verfügbar", "Veröffentlichungsdatum")
    AppLanguage.Greek -> ArtistInfoCopy("Πληροφορίες καλλιτέχνη", "Δισκογραφία", "Φόρτωση δισκογραφίας…", "Η δισκογραφία δεν είναι διαθέσιμη", "Ημερομηνία κυκλοφορίας")
    AppLanguage.Hindi -> ArtistInfoCopy("कलाकार की जानकारी", "डिस्कोग्राफी", "डिस्कोग्राफी लोड हो रही है…", "डिस्कोग्राफी उपलब्ध नहीं है", "रिलीज़ की तारीख")
    AppLanguage.Hungarian -> ArtistInfoCopy("Előadói információk", "Diszkográfia", "Diszkográfia betöltése…", "A diszkográfia nem érhető el", "Megjelenés dátuma")
    AppLanguage.Italian -> ArtistInfoCopy("Informazioni sull’artista", "Discografia", "Caricamento della discografia…", "Discografia non disponibile", "Data di uscita")
    AppLanguage.Japanese -> ArtistInfoCopy("アーティスト情報", "ディスコグラフィー", "ディスコグラフィーを読み込み中…", "ディスコグラフィーを取得できません", "リリース日")
    AppLanguage.Korean -> ArtistInfoCopy("아티스트 정보", "디스코그래피", "디스코그래피를 불러오는 중…", "디스코그래피를 사용할 수 없습니다", "발매일")
    AppLanguage.Latin -> ArtistInfoCopy("De artifice", "Discographia", "Discographia oneratur…", "Discographia praesto non est", "Dies editionis")
    AppLanguage.Latvian -> ArtistInfoCopy("Informācija par mākslinieku", "Diskogrāfija", "Ielādē diskogrāfiju…", "Diskogrāfija nav pieejama", "Izdošanas datums")
    AppLanguage.Lithuanian -> ArtistInfoCopy("Atlikėjo informacija", "Diskografija", "Įkeliama diskografija…", "Diskografija nepasiekiama", "Išleidimo data")
    AppLanguage.Macedonian -> ArtistInfoCopy("Информации за изведувачот", "Дискографија", "Се вчитува дискографијата…", "Дискографијата не е достапна", "Датум на издавање")
    AppLanguage.Malay -> ArtistInfoCopy("Maklumat artis", "Diskografi", "Memuatkan diskografi…", "Diskografi tidak tersedia", "Tarikh keluaran")
    AppLanguage.Norwegian -> ArtistInfoCopy("Artistinformasjon", "Diskografi", "Laster inn diskografi…", "Diskografi er ikke tilgjengelig", "Utgivelsesdato")
    AppLanguage.Polish -> ArtistInfoCopy("Informacje o wykonawcy", "Dyskografia", "Wczytywanie dyskografii…", "Dyskografia jest niedostępna", "Data wydania")
    AppLanguage.Portuguese -> ArtistInfoCopy("Informações do artista", "Discografia", "A carregar a discografia…", "Discografia indisponível", "Data de lançamento")
    AppLanguage.Russian -> ArtistInfoCopy("Информация об исполнителе", "Дискография", "Загрузка дискографии…", "Дискография недоступна", "Дата выпуска")
    AppLanguage.Serbian -> ArtistInfoCopy("Информације о извођачу", "Дискографија", "Учитавање дискографије…", "Дискографија није доступна", "Датум издања")
    AppLanguage.Slovak -> ArtistInfoCopy("Informácie o interpretovi", "Diskografia", "Načítava sa diskografia…", "Diskografia nie je k dispozícii", "Dátum vydania")
    AppLanguage.Spanish -> ArtistInfoCopy("Información del artista", "Discografía", "Cargando discografía…", "Discografía no disponible", "Fecha de lanzamiento")
    AppLanguage.Swedish -> ArtistInfoCopy("Artistinformation", "Diskografi", "Läser in diskografi…", "Diskografin är inte tillgänglig", "Utgivningsdatum")
    AppLanguage.Thai -> ArtistInfoCopy("ข้อมูลศิลปิน", "ผลงานเพลง", "กำลังโหลดผลงานเพลง…", "ไม่พบข้อมูลผลงานเพลง", "วันที่วางจำหน่าย")
    AppLanguage.Ukrainian -> ArtistInfoCopy("Інформація про виконавця", "Дискографія", "Завантаження дискографії…", "Дискографія недоступна", "Дата випуску")
    AppLanguage.Urdu -> ArtistInfoCopy("فنکار کی معلومات", "ڈسکوگرافی", "ڈسکوگرافی لوڈ ہو رہی ہے…", "ڈسکوگرافی دستیاب نہیں", "اجرا کی تاریخ")
    AppLanguage.English -> ArtistInfoCopy("Artist information", "Discography", "Loading discography…", "Discography is unavailable", "Release date")
}
