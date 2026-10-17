package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class ArtistInfoCopy(
    val buttonDescription: String,
    val discography: String,
    val loading: String,
    val unavailable: String,
    val releaseDate: String,
)

internal fun artistInfoCopy(language: AppLanguage): ArtistInfoCopy = when (language) {
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
