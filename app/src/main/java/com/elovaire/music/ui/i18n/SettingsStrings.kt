package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal fun settingsCopy(language: AppLanguage): SettingsLanguageCopy = when (language) {
    AppLanguage.Polish -> SettingsLanguageCopy("Ustawienia", "Wygląd", "Motyw", "Rozmiar tekstu", "Język", "Obecnie używany: ${language.nativeName}", "Dźwięk", "Podbicie basu", "Przestrzenność", "Korektor", "Włącz mono", "Przełącza odtwarzanie stereo na mono", "Inne ustawienia", "Skanuj bibliotekę", "Odśwież indeksowanie w poszukiwaniu nowych multimediów", "Skanuj", "Lista zmian", "Zaprojektowane z pasją do muzyki i świetnego designu")
    AppLanguage.ChineseSimplified -> SettingsLanguageCopy("设置", "外观", "主题", "文字大小", "语言", "当前使用：${language.nativeName}", "声音", "低音增强", "空间感", "均衡器", "启用单声道", "将立体声播放切换为单声道", "其他设置", "扫描媒体库", "刷新索引以查找新媒体", "扫描", "更新日志", "为音乐和优秀设计倾注热情")
    AppLanguage.Czech -> SettingsLanguageCopy("Nastavení", "Vzhled", "Motiv", "Velikost textu", "Jazyk", "Aktuálně používaný: ${language.nativeName}", "Zvuk", "Zesílení basů", "Prostorovost", "Ekvalizér", "Zapnout mono", "Přepne stereo přehrávání na mono", "Další nastavení", "Skenovat knihovnu", "Obnoví index pro nová média", "Skenovat", "Změny", "Navrženo s vášní pro hudbu a skvělý design")
    AppLanguage.Lithuanian -> SettingsLanguageCopy("Nustatymai", "Išvaizda", "Tema", "Teksto dydis", "Kalba", "Šiuo metu naudojama: ${language.nativeName}", "Garsas", "Bosų stiprinimas", "Erdviškumas", "Ekvalaizeris", "Įjungti mono", "Perjungia stereo atkūrimą į mono", "Kiti nustatymai", "Skenuoti biblioteką", "Atnaujina indeksą ieškant naujos medijos", "Skenuoti", "Pakeitimai", "Sukurta su aistra muzikai ir puikiam dizainui")
    AppLanguage.Danish -> SettingsLanguageCopy("Indstillinger", "Udseende", "Tema", "Tekststørrelse", "Sprog", "Aktuelt brugt: ${language.nativeName}", "Lyd", "Basboost", "Rumlighed", "Equalizer", "Aktivér mono", "Skifter stereoafspilning til mono", "Andre indstillinger", "Scan bibliotek", "Opdater indeksering efter nye medier", "Scan", "Ændringslog", "Designet med passion for musik og godt design")
    AppLanguage.French -> SettingsLanguageCopy("Réglages", "Apparence", "Thème", "Taille du texte", "Langue", "Actuellement utilisé : ${language.nativeName}", "Son", "Renfort des basses", "Spatialisation", "Égaliseur", "Activer mono", "Passe la lecture stéréo en mono", "Autres réglages", "Analyser la bibliothèque", "Actualise l’index pour trouver de nouveaux médias", "Analyser", "Nouveautés", "Conçu avec passion pour la musique et le beau design")
    AppLanguage.German -> SettingsLanguageCopy("Einstellungen", "Darstellung", "Design", "Textgröße", "Sprache", "Aktuell verwendet: ${language.nativeName}", "Klang", "Bassverstärkung", "Räumlichkeit", "Equalizer", "Mono aktivieren", "Schaltet Stereo-Wiedergabe auf Mono", "Weitere Einstellungen", "Bibliothek scannen", "Aktualisiert den Index für neue Medien", "Scannen", "Änderungen", "Mit Leidenschaft für Musik und gutes Design gestaltet")
    AppLanguage.Dutch -> SettingsLanguageCopy("Instellingen", "Weergave", "Thema", "Tekstgrootte", "Taal", "Momenteel gebruikt: ${language.nativeName}", "Geluid", "Basversterking", "Ruimtelijkheid", "Equalizer", "Mono inschakelen", "Schakelt stereo afspelen om naar mono", "Andere instellingen", "Bibliotheek scannen", "Vernieuwt indexering voor nieuwe media", "Scannen", "Wijzigingen", "Ontworpen met passie voor muziek en sterk design")
    AppLanguage.Norwegian -> SettingsLanguageCopy("Innstillinger", "Utseende", "Tema", "Tekststørrelse", "Språk", "Brukes nå: ${language.nativeName}", "Lyd", "Bassforsterkning", "Romfølelse", "Equalizer", "Aktiver mono", "Bytter stereoavspilling til mono", "Andre innstillinger", "Skann bibliotek", "Oppdaterer indeksen for nye medier", "Skann", "Endringslogg", "Designet med lidenskap for musikk og flott design")
    AppLanguage.Swedish -> SettingsLanguageCopy("Inställningar", "Utseende", "Tema", "Textstorlek", "Språk", "Används nu: ${language.nativeName}", "Ljud", "Basförstärkning", "Rymd", "Equalizer", "Aktivera mono", "Växlar stereouppspelning till mono", "Andra inställningar", "Skanna bibliotek", "Uppdaterar indexering för ny media", "Skanna", "Ändringslogg", "Designad med passion för musik och bra design")
    AppLanguage.Spanish -> SettingsLanguageCopy("Ajustes", "Apariencia", "Tema", "Tamaño de texto", "Idioma", "Usado actualmente: ${language.nativeName}", "Sonido", "Refuerzo de graves", "Espacialidad", "Ecualizador", "Activar mono", "Cambia la reproducción estéreo a mono", "Otros ajustes", "Escanear biblioteca", "Actualiza la indexación para buscar nuevos medios", "Escanear", "Cambios", "Diseñado con pasión por la música y el buen diseño")
    AppLanguage.Portuguese -> SettingsLanguageCopy("Definições", "Aparência", "Tema", "Tamanho do texto", "Idioma", "Atualmente usado: ${language.nativeName}", "Som", "Reforço de graves", "Espacialidade", "Equalizador", "Ativar mono", "Muda a reprodução estéreo para mono", "Outras definições", "Analisar biblioteca", "Atualiza a indexação para novos ficheiros", "Analisar", "Novidades", "Criado com paixão por música e bom design")
    AppLanguage.Estonian -> SettingsLanguageCopy("Seaded", "Välimus", "Teema", "Teksti suurus", "Keel", "Praegu kasutusel: ${language.nativeName}", "Heli", "Bassi võimendus", "Ruumilisus", "Ekvalaiser", "Luba mono", "Lülitab stereo taasesituse monoks", "Muud seaded", "Skanni teeki", "Värskendab indeksit uue meedia leidmiseks", "Skanni", "Muudatused", "Loodud kirega muusika ja hea disaini vastu")
    AppLanguage.Greek -> SettingsLanguageCopy("Ρυθμίσεις", "Εμφάνιση", "Θέμα", "Μέγεθος κειμένου", "Γλώσσα", "Χρησιμοποιείται τώρα: ${language.nativeName}", "Ήχος", "Ενίσχυση μπάσων", "Χωρικότητα", "Ισοσταθμιστής", "Ενεργοποίηση μονοφωνικού", "Αλλάζει την αναπαραγωγή stereo σε mono", "Άλλες ρυθμίσεις", "Σάρωση βιβλιοθήκης", "Ανανεώνει το ευρετήριο για νέα πολυμέσα", "Σάρωση", "Αλλαγές", "Σχεδιασμένο με πάθος για μουσική και όμορφο design")
    AppLanguage.Croatian -> SettingsLanguageCopy("Postavke", "Izgled", "Tema", "Veličina teksta", "Jezik", "Trenutno se koristi: ${language.nativeName}", "Zvuk", "Pojačanje basa", "Prostornost", "Ekvilizator", "Uključi mono", "Prebacuje stereo reprodukciju u mono", "Ostale postavke", "Skeniraj biblioteku", "Osvježava indeks za nove medije", "Skeniraj", "Promjene", "Dizajnirano sa strašću za glazbu i dobar dizajn")
    AppLanguage.Russian -> SettingsLanguageCopy("Настройки", "Внешний вид", "Тема", "Размер текста", "Язык", "Сейчас используется: ${language.nativeName}", "Звук", "Усиление баса", "Пространственность", "Эквалайзер", "Включить моно", "Переключает стерео воспроизведение в моно", "Другие настройки", "Сканировать библиотеку", "Обновляет индекс для поиска новых медиа", "Сканировать", "Список изменений", "Создано с любовью к музыке и хорошему дизайну")
    AppLanguage.Ukrainian -> SettingsLanguageCopy("Налаштування", "Вигляд", "Тема", "Розмір тексту", "Мова", "Зараз використовується: ${language.nativeName}", "Звук", "Підсилення басів", "Просторовість", "Еквалайзер", "Увімкнути моно", "Перемикає стереовідтворення на моно", "Інші налаштування", "Сканувати бібліотеку", "Оновлює індекс для нових медіа", "Сканувати", "Зміни", "Створено з любов’ю до музики та гарного дизайну")
    AppLanguage.Latvian -> SettingsLanguageCopy("Iestatījumi", "Izskats", "Tēma", "Teksta izmērs", "Valoda", "Pašlaik lietota: ${language.nativeName}", "Skaņa", "Basa pastiprinājums", "Telpiskums", "Ekvalaizers", "Ieslēgt mono", "Pārslēdz stereo atskaņošanu uz mono", "Citi iestatījumi", "Skenēt bibliotēku", "Atjauno indeksu jauniem multivides failiem", "Skenēt", "Izmaiņas", "Radīts ar aizrautību pret mūziku un lielisku dizainu")
    AppLanguage.Italian -> SettingsLanguageCopy("Impostazioni", "Aspetto", "Tema", "Dimensione testo", "Lingua", "Attualmente in uso: ${language.nativeName}", "Suono", "Potenziamento bassi", "Spazialità", "Equalizzatore", "Attiva mono", "Passa la riproduzione stereo a mono", "Altre impostazioni", "Scansiona libreria", "Aggiorna l’indice per nuovi media", "Scansiona", "Novità", "Progettato con passione per la musica e il buon design")
    AppLanguage.Japanese -> SettingsLanguageCopy("設定", "外観", "テーマ", "文字サイズ", "言語", "現在使用中: ${language.nativeName}", "サウンド", "低音ブースト", "空間感", "イコライザー", "モノラルを有効化", "ステレオ再生をモノラルに切り替えます", "その他の設定", "ライブラリをスキャン", "新しいメディアを探すためにインデックスを更新します", "スキャン", "更新履歴", "音楽への情熱と優れたデザインで作られています")
    AppLanguage.Albanian -> SettingsLanguageCopy("Cilësimet", "Pamja", "Tema", "Madhësia e tekstit", "Gjuha", "Aktualisht në përdorim: ${language.nativeName}", "Tingulli", "Përforcim basi", "Hapësirë", "Ekualizuesi", "Aktivizo mono", "E kalon riprodhimin stereo në mono", "Cilësime të tjera", "Skano bibliotekën", "Rifreskon indeksimin për media të reja", "Skano", "Ndryshimet", "Dizajnuar me pasion për muzikën dhe dizajnin e mirë")
    AppLanguage.Hindi -> SettingsLanguageCopy("सेटिंग्स", "दिखावट", "थीम", "टेक्स्ट आकार", "भाषा", "वर्तमान में उपयोग: ${language.nativeName}", "ध्वनि", "बास बूस्ट", "स्पेशियसनेस", "इक्वलाइज़र", "मोनो चालू करें", "स्टीरियो प्लेबैक को मोनो में बदलता है", "अन्य सेटिंग्स", "लाइब्रेरी स्कैन करें", "नई मीडिया के लिए इंडेक्स ताज़ा करें", "स्कैन", "बदलाव", "संगीत और अच्छे डिज़ाइन के प्रति जुनून से बनाया गया")
    AppLanguage.Hungarian -> SettingsLanguageCopy("Beállítások", "Megjelenés", "Téma", "Szövegméret", "Nyelv", "Jelenleg használt: ${language.nativeName}", "Hang", "Basszuskiemelés", "Térhatás", "Hangszínszabályzó", "Monó engedélyezése", "A sztereó lejátszást monóra váltja", "Egyéb beállítások", "Könyvtár beolvasása", "Frissíti az indexelést új médiához", "Beolvasás", "Változások", "Szenvedéllyel tervezve zenéhez és jó designhoz")
    AppLanguage.Latin -> SettingsLanguageCopy("Optiones", "Aspectus", "Thema", "Magnitudo textus", "Lingua", "Nunc adhibetur: ${language.nativeName}", "Sonus", "Bassus auctus", "Spatium", "Aequator", "Mono activa", "Playback stereo in mono vertit", "Aliae optiones", "Bibliothecam scrutare", "Indicem pro novis mediis renovat", "Scrutare", "Mutationes", "Studio musicae et bono consilio creatum")
    AppLanguage.Macedonian -> SettingsLanguageCopy("Поставки", "Изглед", "Тема", "Големина на текст", "Јазик", "Моментално се користи: ${language.nativeName}", "Звук", "Засилување на бас", "Просторност", "Еквилајзер", "Вклучи моно", "Ја префрла стерео репродукцијата во моно", "Други поставки", "Скенирај библиотека", "Го освежува индексирањето за нови медиуми", "Скенирај", "Промени", "Создадено со страст за музика и добар дизајн")
    AppLanguage.Serbian -> SettingsLanguageCopy("Подешавања", "Изглед", "Тема", "Величина текста", "Језик", "Тренутно се користи: ${language.nativeName}", "Звук", "Појачање баса", "Просторност", "Еквилајзер", "Укључи моно", "Пребацује стерео репродукцију у моно", "Остала подешавања", "Скенирај библиотеку", "Освежава индексирање за нове медије", "Скенирај", "Промене", "Дизајнирано са страшћу за музику и добар дизајн")
    AppLanguage.Thai -> SettingsLanguageCopy("การตั้งค่า", "รูปลักษณ์", "ธีม", "ขนาดข้อความ", "ภาษา", "ใช้อยู่: ${language.nativeName}", "เสียง", "เพิ่มเสียงเบส", "มิติเสียง", "อีควอไลเซอร์", "เปิดโมโน", "เปลี่ยนการเล่นสเตอริโอเป็นโมโน", "การตั้งค่าอื่น", "สแกนคลังเพลง", "รีเฟรชดัชนีเพื่อค้นหาสื่อใหม่", "สแกน", "บันทึกการเปลี่ยนแปลง", "ออกแบบด้วยความหลงใหลในดนตรีและดีไซน์ที่ดี")
    AppLanguage.Slovak -> SettingsLanguageCopy("Nastavenia", "Vzhľad", "Téma", "Veľkosť textu", "Jazyk", "Momentálne používaný: ${language.nativeName}", "Zvuk", "Zosilnenie basov", "Priestorovosť", "Ekvalizér", "Zapnúť mono", "Prepne stereo prehrávanie na mono", "Ďalšie nastavenia", "Skenovať knižnicu", "Obnoví index pre nové médiá", "Skenovať", "Zmeny", "Vytvorené s vášňou pre hudbu a skvelý dizajn")
    AppLanguage.Korean -> SettingsLanguageCopy("설정", "모양", "테마", "텍스트 크기", "언어", "현재 사용 중: ${language.nativeName}", "사운드", "저음 강화", "공간감", "이퀄라이저", "모노 사용", "스테레오 재생을 모노로 전환합니다", "기타 설정", "라이브러리 스캔", "새 미디어를 찾기 위해 인덱스를 새로 고칩니다", "스캔", "변경 사항", "음악과 좋은 디자인에 대한 열정으로 만들어졌습니다")
    AppLanguage.Malay -> SettingsLanguageCopy("Tetapan", "Penampilan", "Tema", "Saiz teks", "Bahasa", "Sedang digunakan: ${language.nativeName}", "Bunyi", "Penguat bass", "Keluasan", "Penyama", "Dayakan mono", "Menukar main balik stereo kepada mono", "Tetapan lain", "Imbas pustaka", "Segarkan pengindeksan untuk media baharu", "Imbas", "Log perubahan", "Direka dengan semangat terhadap muzik dan reka bentuk yang hebat")
    AppLanguage.Bengali -> SettingsLanguageCopy("সেটিংস", "চেহারা", "থিম", "টেক্সটের আকার", "ভাষা", "বর্তমানে ব্যবহৃত: ${language.nativeName}", "শব্দ", "বেস বুস্ট", "স্পেসিয়াসনেস", "ইকুয়ালাইজার", "মোনো চালু করুন", "স্টেরিও প্লেব্যাককে মোনোতে বদলে দেয়", "অন্যান্য সেটিংস", "লাইব্রেরি স্ক্যান করুন", "নতুন মিডিয়ার জন্য ইনডেক্স রিফ্রেশ করে", "স্ক্যান", "পরিবর্তনপঞ্জি", "সঙ্গীত ও দারুণ ডিজাইনের প্রতি ভালবাসা দিয়ে নির্মিত")
    AppLanguage.Urdu -> SettingsLanguageCopy("سیٹنگز", "ظاہری شکل", "تھیم", "متن کا سائز", "زبان", "فی الحال استعمال میں: ${language.nativeName}", "آواز", "باس بوسٹ", "کشادگی", "ایکوالائزر", "مونو فعال کریں", "اسٹیریو پلے بیک کو مونو میں بدلتا ہے", "دیگر سیٹنگز", "لائبریری اسکین کریں", "نئے میڈیا کے لیے انڈیکس تازہ کرتا ہے", "اسکین", "تبدیلیاں", "موسیقی اور عمدہ ڈیزائن کے شوق سے تیار کیا گیا")
    AppLanguage.English -> SettingsLanguageCopy("Settings", "Appearance", "Theme", "Text size", "Language", "Currently used: ${language.nativeName}", "Sound", "Bass boost", "Spaciousness", "Equalizer", "Enable mono", "Switches stereo playback to mono", "Other settings", "Scan library", "Refresh indexing in search for new media", "Scan", "Changelog", "Designed with passion for music and great design")
}.withLocalizedVolumeNormalization(language).withLocalizedSettingsEntries(language)

internal fun SettingsLanguageCopy.withLocalizedVolumeNormalization(language: AppLanguage): SettingsLanguageCopy {
    return copy(
        volumeNormalization = volumeNormalizationTitle(language),
        volumeNormalizationSubtitle = volumeNormalizationSubtitle(language),
    )
}

private fun volumeNormalizationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Normalizacja głośności"
    AppLanguage.Albanian -> "Normalizimi i volumit"
    AppLanguage.ChineseSimplified -> "音量标准化"
    AppLanguage.Croatian -> "Normalizacija glasnoće"
    AppLanguage.Czech -> "Normalizace hlasitosti"
    AppLanguage.Danish -> "Volumennormalisering"
    AppLanguage.Dutch -> "Volumenormalisatie"
    AppLanguage.Estonian -> "Helitugevuse normaliseerimine"
    AppLanguage.French -> "Normalisation du volume"
    AppLanguage.German -> "Lautstärkenormalisierung"
    AppLanguage.Greek -> "Κανονικοποίηση έντασης"
    AppLanguage.Hindi -> "वॉल्यूम नॉर्मलाइज़ेशन"
    AppLanguage.Hungarian -> "Hangerő-normalizálás"
    AppLanguage.Italian -> "Normalizzazione volume"
    AppLanguage.Japanese -> "音量正規化"
    AppLanguage.Latin -> "Voluminis normalizatio"
    AppLanguage.Latvian -> "Skaļuma normalizēšana"
    AppLanguage.Lithuanian -> "Garsumo normalizavimas"
    AppLanguage.Macedonian -> "Нормализација на јачина"
    AppLanguage.Norwegian -> "Volumnormalisering"
    AppLanguage.Portuguese -> "Normalização de volume"
    AppLanguage.Russian -> "Нормализация громкости"
    AppLanguage.Serbian -> "Нормализација јачине"
    AppLanguage.Spanish -> "Normalización de volumen"
    AppLanguage.Swedish -> "Volymnormalisering"
    AppLanguage.Thai -> "ปรับระดับเสียงให้สม่ำเสมอ"
    AppLanguage.Ukrainian -> "Нормалізація гучності"
    AppLanguage.Slovak -> "Normalizácia hlasitosti"
    AppLanguage.Korean -> "음량 정규화"
    AppLanguage.Malay -> "Penormalan kelantangan"
    AppLanguage.Bengali -> "ভলিউম স্বাভাবিকীকরণ"
    AppLanguage.Urdu -> "آواز کی نارملائزیشن"
    AppLanguage.English -> "Volume normalization"
}

internal data class PlaylistManagementCopy(
    val title: String,
    val importAction: String,
    val exportAll: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val exportAction: String,
)

@Suppress("CyclomaticComplexMethod")
internal fun playlistManagementCopy(language: AppLanguage): PlaylistManagementCopy {
    val labels = when (language) {
        AppLanguage.Albanian -> listOf("Importo", "Eksporto të gjitha", "Nuk ke lista dëgjimi", "Krijo të paktën një listë dëgjimi që të mund ta eksportosh", "Eksporto")
        AppLanguage.Bengali -> listOf("ইমপোর্ট", "সব এক্সপোর্ট করুন", "আপনার কোনো প্লেলিস্ট নেই", "এক্সপোর্ট করতে অন্তত একটি প্লেলিস্ট তৈরি করুন", "এক্সপোর্ট")
        AppLanguage.ChineseSimplified -> listOf("导入", "全部导出", "你还没有播放列表", "请至少创建一个播放列表后再导出", "导出")
        AppLanguage.Croatian -> listOf("Uvezi", "Izvezi sve", "Nemate playlista", "Izradite barem jednu playlistu da biste je mogli izvesti", "Izvezi")
        AppLanguage.Czech -> listOf("Importovat", "Exportovat vše", "Nemáte žádné playlisty", "Abyste mohli playlist exportovat, vytvořte alespoň jeden", "Exportovat")
        AppLanguage.Danish -> listOf("Importér", "Eksportér alle", "Du har ingen afspilningslister", "Opret mindst én afspilningsliste for at kunne eksportere den", "Eksportér")
        AppLanguage.Dutch -> listOf("Importeren", "Alles exporteren", "Je hebt geen afspeellijsten", "Maak minstens één afspeellijst om deze te kunnen exporteren", "Exporteren")
        AppLanguage.Estonian -> listOf("Impordi", "Ekspordi kõik", "Sul pole esitusloendeid", "Ekspordi võimaldamiseks loo vähemalt üks esitusloend", "Ekspordi")
        AppLanguage.French -> listOf("Importer", "Tout exporter", "Vous n’avez aucune playlist", "Créez au moins une playlist pour pouvoir l’exporter", "Exporter")
        AppLanguage.German -> listOf("Importieren", "Alle exportieren", "Du hast keine Playlists", "Erstelle mindestens eine Playlist, um sie exportieren zu können", "Exportieren")
        AppLanguage.Greek -> listOf("Εισαγωγή", "Εξαγωγή όλων", "Δεν έχεις λίστες αναπαραγωγής", "Δημιούργησε τουλάχιστον μία λίστα αναπαραγωγής για να μπορείς να την εξαγάγεις", "Εξαγωγή")
        AppLanguage.Hindi -> listOf("आयात", "सभी निर्यात करें", "आपके पास कोई प्लेलिस्ट नहीं है", "निर्यात करने के लिए कम से कम एक प्लेलिस्ट बनाएं", "निर्यात")
        AppLanguage.Hungarian -> listOf("Importálás", "Összes exportálása", "Nincsenek lejátszólistáid", "Hozz létre legalább egy lejátszólistát az exportálásához", "Exportálás")
        AppLanguage.Italian -> listOf("Importa", "Esporta tutto", "Non hai playlist", "Crea almeno una playlist per poterla esportare", "Esporta")
        AppLanguage.Japanese -> listOf("インポート", "すべてエクスポート", "プレイリストがありません", "エクスポートするには、少なくとも1つのプレイリストを作成してください", "エクスポート")
        AppLanguage.Korean -> listOf("가져오기", "모두 내보내기", "플레이리스트가 없습니다", "내보내려면 플레이리스트를 하나 이상 만드세요", "내보내기")
        AppLanguage.Latin -> listOf("Importa", "Omnia exporta", "Nullum indicem habes", "Crea saltem unum indicem ut eum exportare possis", "Exporta")
        AppLanguage.Latvian -> listOf("Importēt", "Eksportēt visu", "Jums nav atskaņošanas sarakstu", "Izveidojiet vismaz vienu atskaņošanas sarakstu, lai to varētu eksportēt", "Eksportēt")
        AppLanguage.Lithuanian -> listOf("Importuoti", "Eksportuoti viską", "Neturite grojaraščių", "Sukurkite bent vieną grojaraštį, kad galėtumėte jį eksportuoti", "Eksportuoti")
        AppLanguage.Macedonian -> listOf("Увези", "Извези ги сите", "Немате плејлисти", "Создадете барем една плејлиста за да можете да ја извезете", "Извези")
        AppLanguage.Malay -> listOf("Import", "Eksport semua", "Anda tiada senarai main", "Cipta sekurang-kurangnya satu senarai main untuk mengeksportnya", "Eksport")
        AppLanguage.Norwegian -> listOf("Importer", "Eksporter alle", "Du har ingen spillelister", "Opprett minst én spilleliste for å kunne eksportere den", "Eksporter")
        AppLanguage.Polish -> listOf("Importuj", "Eksportuj wszystko", "Nie masz żadnych playlist", "Utwórz co najmniej jedną playlistę, aby móc ją eksportować", "Eksportuj")
        AppLanguage.Portuguese -> listOf("Importar", "Exportar tudo", "Não tem playlists", "Crie pelo menos uma playlist para a poder exportar", "Exportar")
        AppLanguage.Russian -> listOf("Импортировать", "Экспортировать все", "У вас нет плейлистов", "Создайте хотя бы один плейлист, чтобы экспортировать его", "Экспортировать")
        AppLanguage.Serbian -> listOf("Увези", "Извези све", "Немате плејлисте", "Направите бар једну плејлисту да бисте могли да је извезете", "Извези")
        AppLanguage.Slovak -> listOf("Importovať", "Exportovať všetko", "Nemáte žiadne playlisty", "Vytvorte aspoň jeden playlist, aby ste ho mohli exportovať", "Exportovať")
        AppLanguage.Spanish -> listOf("Importar", "Exportar todo", "No tienes playlists", "Crea al menos una playlist para poder exportarla", "Exportar")
        AppLanguage.Swedish -> listOf("Importera", "Exportera alla", "Du har inga spellistor", "Skapa minst en spellista för att kunna exportera den", "Exportera")
        AppLanguage.Thai -> listOf("นำเข้า", "ส่งออกทั้งหมด", "คุณยังไม่มีเพลย์ลิสต์", "สร้างเพลย์ลิสต์อย่างน้อยหนึ่งรายการเพื่อส่งออก", "ส่งออก")
        AppLanguage.Ukrainian -> listOf("Імпортувати", "Експортувати все", "У вас немає плейлистів", "Створіть хоча б один плейлист, щоб експортувати його", "Експортувати")
        AppLanguage.Urdu -> listOf("درآمد", "سب برآمد کریں", "آپ کے پاس کوئی پلے لسٹ نہیں", "برآمد کرنے کے لیے کم از کم ایک پلے لسٹ بنائیں", "برآمد کریں")
        AppLanguage.English -> listOf("Import", "Export all", "You have no playlists", "Create at least one playlist to be able to export it", "Export")
    }
    val settings = settingsCopy(language)
    return PlaylistManagementCopy(
        title = settings.managePlaylistsTitle,
        importAction = labels[0],
        exportAll = labels[1],
        emptyTitle = labels[2],
        emptySubtitle = labels[3],
        exportAction = labels[4],
    )
}

internal data class CrossfadeCopy(
    val fadeLength: String,
    val fadeLengthExplanation: String,
    val silenceDetection: String,
    val silenceDetectionExplanation: String,
)

@Suppress("CyclomaticComplexMethod")
internal fun crossfadeCopy(language: AppLanguage): CrossfadeCopy = when (language) {
    AppLanguage.Albanian -> CrossfadeCopy("Kohëzgjatja e kalimit", "Përcakton sa gjatë mbivendosen këngët gjatë kalimit", "Zbulimi i heshtjes", "Përcakton sa heshtje shpërfillet gjatë gjetjes së fundit të këngës")
    AppLanguage.Bengali -> CrossfadeCopy("ক্রসফেডের দৈর্ঘ্য", "ট্রানজিশনের সময় গানগুলো কতক্ষণ একসঙ্গে বাজবে তা নিয়ন্ত্রণ করে", "নীরবতা শনাক্তকরণ", "গানের শেষ খোঁজার সময় কতটা নীরবতা উপেক্ষা করা হবে তা নির্ধারণ করে")
    AppLanguage.ChineseSimplified -> CrossfadeCopy("淡化时长", "控制歌曲过渡时重叠播放的时间", "静音检测", "设置查找歌曲结尾时忽略的静音长度")
    AppLanguage.Croatian -> CrossfadeCopy("Duljina pretapanja", "Određuje koliko se dugo pjesme preklapaju tijekom prijelaza", "Detekcija tišine", "Određuje koliko se tišine zanemaruje pri pronalaženju kraja pjesme")
    AppLanguage.Czech -> CrossfadeCopy("Délka prolínání", "Určuje, jak dlouho se skladby při přechodu překrývají", "Detekce ticha", "Určuje, kolik ticha se ignoruje při hledání konce skladby")
    AppLanguage.Danish -> CrossfadeCopy("Crossfade-længde", "Bestemmer hvor længe sange overlapper under overgangen", "Stilhedsregistrering", "Bestemmer hvor meget stilhed der ignoreres ved søgning efter sangens slutning")
    AppLanguage.Dutch -> CrossfadeCopy("Crossfade-duur", "Bepaalt hoelang nummers overlappen tijdens de overgang", "Stilte detecteren", "Bepaalt hoeveel stilte wordt genegeerd bij het vinden van het einde van een nummer")
    AppLanguage.Estonian -> CrossfadeCopy("Ülemineku pikkus", "Määrab, kui kaua lood ülemineku ajal kattuvad", "Vaikuse tuvastamine", "Määrab, kui palju vaikust loo lõpu leidmisel eiratakse")
    AppLanguage.French -> CrossfadeCopy("Durée du fondu", "Détermine combien de temps les morceaux se chevauchent pendant la transition", "Détection du silence", "Détermine quelle durée de silence ignorer pour trouver la fin d’un morceau")
    AppLanguage.German -> CrossfadeCopy("Überblendungsdauer", "Legt fest, wie lange sich Titel beim Übergang überschneiden", "Stilleerkennung", "Legt fest, wie viel Stille bei der Suche nach dem Titelende ignoriert wird")
    AppLanguage.Greek -> CrossfadeCopy("Διάρκεια μετάβασης", "Καθορίζει για πόσο χρόνο επικαλύπτονται τα τραγούδια στη μετάβαση", "Ανίχνευση σιωπής", "Καθορίζει πόση σιωπή αγνοείται κατά την εύρεση του τέλους ενός τραγουδιού")
    AppLanguage.Hindi -> CrossfadeCopy("क्रॉसफ़ेड अवधि", "ट्रांज़िशन के दौरान गाने कितनी देर तक एक साथ बजेंगे यह नियंत्रित करता है", "मौन पहचान", "गाने का अंत खोजते समय कितने मौन को अनदेखा करना है यह तय करता है")
    AppLanguage.Hungarian -> CrossfadeCopy("Átkeverés hossza", "Meghatározza, mennyi ideig fedik át egymást a dalok az átmenet során", "Csendérzékelés", "Meghatározza, mennyi csendet hagyjon figyelmen kívül a dal végének keresésekor")
    AppLanguage.Italian -> CrossfadeCopy("Durata della dissolvenza", "Determina per quanto tempo i brani si sovrappongono durante la transizione", "Rilevamento del silenzio", "Determina quanto silenzio ignorare per trovare la fine del brano")
    AppLanguage.Japanese -> CrossfadeCopy("クロスフェード時間", "切り替え中に曲が重なる時間を調整します", "無音検出", "曲の終わりを探すときに無視する無音の量を設定します")
    AppLanguage.Korean -> CrossfadeCopy("크로스페이드 길이", "전환 중 곡이 얼마나 오래 겹칠지 조정합니다", "무음 감지", "곡의 끝을 찾을 때 무시할 무음의 양을 설정합니다")
    AppLanguage.Latin -> CrossfadeCopy("Longitudo transitus", "Definit quam diu cantus durante transitione superponantur", "Silentii detectio", "Definit quantum silentium cum finem cantus quaeritur neglegatur")
    AppLanguage.Latvian -> CrossfadeCopy("Sapludināšanas ilgums", "Nosaka, cik ilgi dziesmas pārklājas pārejas laikā", "Klusuma noteikšana", "Nosaka, cik daudz klusuma ignorēt, meklējot dziesmas beigas")
    AppLanguage.Lithuanian -> CrossfadeCopy("Suliejimo trukmė", "Nustato, kiek laiko dainos persidengia pereinant", "Tylos aptikimas", "Nustato, kiek tylos ignoruoti ieškant dainos pabaigos")
    AppLanguage.Macedonian -> CrossfadeCopy("Времетраење на прелевањето", "Одредува колку долго песните се преклопуваат при преминот", "Откривање тишина", "Одредува колку тишина се игнорира при наоѓање на крајот на песната")
    AppLanguage.Malay -> CrossfadeCopy("Tempoh crossfade", "Menentukan berapa lama lagu bertindih semasa peralihan", "Pengesanan senyap", "Menentukan jumlah senyap yang diabaikan ketika mencari penghujung lagu")
    AppLanguage.Norwegian -> CrossfadeCopy("Kryssfade-lengde", "Bestemmer hvor lenge sanger overlapper under overgangen", "Stillhetsregistrering", "Bestemmer hvor mye stillhet som ignoreres når slutten av sangen finnes")
    AppLanguage.Polish -> CrossfadeCopy("Długość przenikania", "Określa, jak długo utwory nakładają się na siebie podczas przejścia", "Wykrywanie ciszy", "Określa, ile ciszy ignorować przy wyszukiwaniu końca utworu")
    AppLanguage.Portuguese -> CrossfadeCopy("Duração do crossfade", "Define durante quanto tempo as músicas se sobrepõem na transição", "Deteção de silêncio", "Define quanto silêncio ignorar ao encontrar o fim da música")
    AppLanguage.Russian -> CrossfadeCopy("Длительность кроссфейда", "Определяет, как долго треки перекрываются во время перехода", "Определение тишины", "Определяет, сколько тишины игнорировать при поиске конца трека")
    AppLanguage.Serbian -> CrossfadeCopy("Дужина унакрсног преливања", "Одређује колико дуго се песме преклапају током прелаза", "Откривање тишине", "Одређује колико тишине треба занемарити при проналажењу краја песме")
    AppLanguage.Slovak -> CrossfadeCopy("Dĺžka prelínania", "Určuje, ako dlho sa skladby pri prechode prekrývajú", "Detekcia ticha", "Určuje, koľko ticha sa ignoruje pri hľadaní konca skladby")
    AppLanguage.Spanish -> CrossfadeCopy("Duración del fundido", "Determina cuánto tiempo se superponen las canciones durante la transición", "Detección de silencio", "Determina cuánto silencio se ignora al buscar el final de una canción")
    AppLanguage.Swedish -> CrossfadeCopy("Övertoningslängd", "Bestämmer hur länge låtar överlappar under övergången", "Tystnadsdetektering", "Bestämmer hur mycket tystnad som ignoreras när låtens slut hittas")
    AppLanguage.Thai -> CrossfadeCopy("ความยาวครอสเฟด", "กำหนดระยะเวลาที่เพลงซ้อนกันระหว่างการเปลี่ยนเพลง", "ตรวจจับความเงียบ", "กำหนดปริมาณความเงียบที่ละเว้นเมื่อค้นหาจุดจบของเพลง")
    AppLanguage.Ukrainian -> CrossfadeCopy("Тривалість кросфейду", "Визначає, як довго треки накладаються під час переходу", "Виявлення тиші", "Визначає, скільки тиші ігнорувати під час пошуку кінця треку")
    AppLanguage.Urdu -> CrossfadeCopy("کراس فیڈ کی مدت", "منتقلی کے دوران گانے کتنی دیر تک ایک دوسرے پر چلیں گے یہ طے کرتا ہے", "خاموشی کی شناخت", "گانے کا اختتام تلاش کرتے وقت نظرانداز کی جانے والی خاموشی کی مقدار طے کرتا ہے")
    AppLanguage.English -> CrossfadeCopy("Fade length", "Controls how long songs overlap during the transition", "Silence detection", "Sets how much silence is ignored when finding the end of a song")
}

internal fun volumeNormalizationSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Zmniejsza różnice głośności między utworami, gdy metadane pliku to obsługują"
    AppLanguage.Albanian -> "Zvogëlon dallimet e volumit mes këngëve kur metadatat e skedarit e mbështesin"
    AppLanguage.ChineseSimplified -> "在文件元数据支持时减少歌曲之间的响度差异"
    AppLanguage.Croatian -> "Smanjuje razlike u glasnoći između pjesama kada to podržavaju metapodaci datoteke"
    AppLanguage.Czech -> "Snižuje rozdíly hlasitosti mezi skladbami, pokud to podporují metadata souboru"
    AppLanguage.Danish -> "Reducerer lydstyrkeforskelle mellem sange, når filmetadata understøtter det"
    AppLanguage.Dutch -> "Vermindert volumeverschillen tussen nummers wanneer bestandsmetadata dit ondersteunen"
    AppLanguage.Estonian -> "Vähendab lugude helitugevuse erinevusi, kui faili metaandmed seda toetavad"
    AppLanguage.French -> "Réduit les écarts de volume entre les morceaux quand les métadonnées du fichier le permettent"
    AppLanguage.German -> "Verringert Lautstärkeunterschiede zwischen Songs, wenn die Dateimetadaten es unterstützen"
    AppLanguage.Greek -> "Μειώνει τις διαφορές έντασης μεταξύ τραγουδιών όταν το υποστηρίζουν τα μεταδεδομένα αρχείου"
    AppLanguage.Hindi -> "फ़ाइल मेटाडेटा समर्थित होने पर गानों के बीच लाउडनेस अंतर कम करता है"
    AppLanguage.Hungarian -> "Csökkenti a dalok közti hangerőkülönbségeket, ha a fájl metaadatai támogatják"
    AppLanguage.Italian -> "Riduce le differenze di volume tra i brani quando i metadati del file lo supportano"
    AppLanguage.Japanese -> "ファイルのメタデータが対応している場合に曲間の音量差を抑えます"
    AppLanguage.Latin -> "Differentias voluminis inter cantus minuit cum metadata fasciculi id sustinent"
    AppLanguage.Latvian -> "Samazina skaļuma atšķirības starp dziesmām, ja to atbalsta faila metadati"
    AppLanguage.Lithuanian -> "Sumažina garsumo skirtumus tarp dainų, kai tai palaiko failo metaduomenys"
    AppLanguage.Macedonian -> "Ги намалува разликите во јачина меѓу песните кога метаподатоците го поддржуваат тоа"
    AppLanguage.Norwegian -> "Reduserer lydstyrkeforskjeller mellom sanger når filmetadata støtter det"
    AppLanguage.Portuguese -> "Reduz diferenças de volume entre músicas quando os metadados do ficheiro o suportam"
    AppLanguage.Russian -> "Уменьшает разницу громкости между треками, если это поддерживают метаданные файла"
    AppLanguage.Serbian -> "Смањује разлике у јачини између песама када то подржавају метаподаци фајла"
    AppLanguage.Spanish -> "Reduce diferencias de volumen entre canciones cuando los metadatos del archivo lo admiten"
    AppLanguage.Swedish -> "Minskar volymskillnader mellan låtar när filens metadata stöder det"
    AppLanguage.Thai -> "ลดความต่างของความดังระหว่างเพลงเมื่อเมตาดาตาของไฟล์รองรับ"
    AppLanguage.Ukrainian -> "Зменшує різницю гучності між піснями, якщо це підтримують метадані файла"
    AppLanguage.Slovak -> "Znižuje rozdiely hlasitosti medzi skladbami, keď to podporujú metadáta súboru"
    AppLanguage.Korean -> "파일 메타데이터가 지원할 때 곡 사이의 음량 차이를 줄입니다"
    AppLanguage.Malay -> "Mengurangkan perbezaan kelantangan antara lagu apabila metadata fail menyokongnya"
    AppLanguage.Bengali -> "ফাইল মেটাডেটা সমর্থন করলে গানগুলোর মধ্যে ভলিউমের পার্থক্য কমায়"
    AppLanguage.Urdu -> "فائل میٹا ڈیٹا کے تعاون پر گانوں کے درمیان آواز کا فرق کم کرتا ہے"
    AppLanguage.English -> "Reduce loudness differences between songs when supported by file metadata"
}

private data class SettingsEntryCopy(
    val crossfadeTitle: String,
    val crossfadeSubtitle: String,
    val managePlaylistsTitle: String,
    val managePlaylistsSubtitle: String,
    val onlineLyricsTitle: String,
    val onlineLyricsSubtitle: String,
    val checkForUpdatesTitle: String,
    val checkForUpdatesSubtitle: String,
)

internal fun SettingsLanguageCopy.withLocalizedSettingsEntries(language: AppLanguage): SettingsLanguageCopy {
    val entries = when (language) {
        AppLanguage.Albanian -> SettingsEntryCopy("Kalimi gradual", "Rregullo kalimin midis këngëve", "Menaxho listat e dëgjimit", "Importo dhe eksporto listat e tua të dëgjimit", "Tekste këngësh online", "Merr tekstet e këngëve nga LRCLIB", "Kontrollo për përditësime", "Kontrollo nëse disponohet një version më i ri")
        AppLanguage.Bengali -> SettingsEntryCopy("ক্রসফেড", "গানের মধ্যবর্তী পরিবর্তন সামঞ্জস্য করুন", "প্লেলিস্ট পরিচালনা", "আপনার প্লেলিস্ট ইমপোর্ট ও এক্সপোর্ট করুন", "অনলাইন গানের কথা", "LRCLIB থেকে গানের কথা আনুন", "আপডেট খুঁজুন", "নতুন সংস্করণ আছে কি না দেখুন")
        AppLanguage.ChineseSimplified -> SettingsEntryCopy("交叉淡化", "调整歌曲之间的过渡", "管理播放列表", "导入和导出播放列表", "在线歌词", "从 LRCLIB 获取歌词", "检查更新", "检查是否有新版本")
        AppLanguage.Croatian -> SettingsEntryCopy("Pretapanje", "Podesite prijelaz između pjesama", "Upravljanje playlistama", "Uvezite i izvezite svoje playliste", "Tekstovi pjesama na internetu", "Dohvati tekstove pjesama s usluge LRCLIB", "Provjeri ima li ažuriranja", "Provjeri je li dostupna novija verzija")
        AppLanguage.Czech -> SettingsEntryCopy("Prolínání", "Nastavte přechod mezi skladbami", "Správa playlistů", "Importujte a exportujte své playlisty", "Online texty písní", "Načítejte texty písní z LRCLIB", "Kontrolovat aktualizace", "Zkontrolujte, zda je k dispozici novější verze")
        AppLanguage.Danish -> SettingsEntryCopy("Crossfade", "Juster overgangen mellem sange", "Administrer afspilningslister", "Importér og eksportér dine afspilningslister", "Sangtekster online", "Hent sangtekster fra LRCLIB", "Søg efter opdateringer", "Se, om der findes en nyere version")
        AppLanguage.Dutch -> SettingsEntryCopy("Crossfaden", "Pas de overgang tussen nummers aan", "Afspeellijsten beheren", "Importeer en exporteer je afspeellijsten", "Online songteksten", "Haal songteksten op van LRCLIB", "Controleren op updates", "Controleer of er een nieuwere versie beschikbaar is")
        AppLanguage.Estonian -> SettingsEntryCopy("Lugude sujuv üleminek", "Reguleeri lugude vahelist üleminekut", "Esitusloendite haldamine", "Impordi ja ekspordi esitusloendeid", "Veebipõhised laulusõnad", "Hangi laulusõnad LRCLIB-st", "Kontrolli värskendusi", "Kontrolli, kas saadaval on uuem versioon")
        AppLanguage.French -> SettingsEntryCopy("Fondu enchaîné", "Régler la transition entre les morceaux", "Gérer les playlists", "Importer et exporter vos playlists", "Paroles en ligne", "Récupérer les paroles depuis LRCLIB", "Rechercher des mises à jour", "Vérifier si une version plus récente est disponible")
        AppLanguage.German -> SettingsEntryCopy("Überblendung", "Übergang zwischen Titeln anpassen", "Playlists verwalten", "Playlists importieren und exportieren", "Online-Liedtexte", "Liedtexte von LRCLIB abrufen", "Nach Updates suchen", "Prüfen, ob eine neuere Version verfügbar ist")
        AppLanguage.Greek -> SettingsEntryCopy("Ομαλή μετάβαση", "Ρυθμίστε τη μετάβαση μεταξύ τραγουδιών", "Διαχείριση playlist", "Εισαγάγετε και εξαγάγετε τις playlist σας", "Στίχοι online", "Λήψη στίχων από το LRCLIB", "Έλεγχος για ενημερώσεις", "Ελέγξτε αν υπάρχει νεότερη έκδοση")
        AppLanguage.Hindi -> SettingsEntryCopy("क्रॉसफ़ेड", "गानों के बीच ट्रांज़िशन समायोजित करें", "प्लेलिस्ट प्रबंधित करें", "अपनी प्लेलिस्ट आयात और निर्यात करें", "ऑनलाइन गीत", "LRCLIB से गीत प्राप्त करें", "अपडेट की जाँच करें", "देखें कि नया संस्करण उपलब्ध है या नहीं")
        AppLanguage.Hungarian -> SettingsEntryCopy("Átkeverés", "A dalok közötti átmenet beállítása", "Lejátszási listák kezelése", "Lejátszási listák importálása és exportálása", "Online dalszövegek", "Dalszövegek lekérése az LRCLIB-ből", "Frissítések keresése", "Ellenőrizze, elérhető-e újabb verzió")
        AppLanguage.Italian -> SettingsEntryCopy("Dissolvenza incrociata", "Regola la transizione tra i brani", "Gestisci playlist", "Importa ed esporta le tue playlist", "Testi online", "Recupera i testi da LRCLIB", "Controlla aggiornamenti", "Verifica se è disponibile una versione più recente")
        AppLanguage.Japanese -> SettingsEntryCopy("クロスフェード", "曲間のつなぎ方を調整", "プレイリストを管理", "プレイリストをインポート・エクスポート", "オンライン歌詞", "LRCLIBから歌詞を取得", "アップデートを確認", "新しいバージョンがあるか確認")
        AppLanguage.Korean -> SettingsEntryCopy("크로스페이드", "곡 사이의 전환을 조정합니다", "플레이리스트 관리", "플레이리스트를 가져오고 내보냅니다", "온라인 가사", "LRCLIB에서 가사를 가져옵니다", "업데이트 확인", "새 버전이 있는지 확인합니다")
        AppLanguage.Latin -> SettingsEntryCopy("Transitus gradualis", "Transitionem inter cantus compone", "Indices curare", "Indices tuos importa et exporta", "Verba carminum online", "Verba carminum ex LRCLIB pete", "Quaere renovationes", "Reprime num versio recentior praesto sit")
        AppLanguage.Latvian -> SettingsEntryCopy("Dziesmu sapludināšana", "Pielāgo pāreju starp dziesmām", "Pārvaldīt atskaņošanas sarakstus", "Importē un eksportē savus atskaņošanas sarakstus", "Dziesmu teksti tiešsaistē", "Iegūsti dziesmu tekstus no LRCLIB", "Pārbaudīt atjauninājumus", "Pārbaudi, vai pieejama jaunāka versija")
        AppLanguage.Lithuanian -> SettingsEntryCopy("Garso takelių suliejimas", "Koreguok perėjimą tarp dainų", "Tvarkyti grojaraščius", "Importuok ir eksportuok savo grojaraščius", "Dainų žodžiai internete", "Gauk dainų žodžius iš LRCLIB", "Tikrinti, ar yra naujinių", "Patikrink, ar yra naujesnė versija")
        AppLanguage.Macedonian -> SettingsEntryCopy("Вкрстено прелевање", "Прилагодете го преминот меѓу песните", "Управување со плејлисти", "Увезете и извезете ги вашите плејлисти", "Текстови на песни онлајн", "Преземете текстови од LRCLIB", "Провери за ажурирања", "Проверете дали има понова верзија")
        AppLanguage.Malay -> SettingsEntryCopy("Crossfade", "Laraskan peralihan antara lagu", "Urus senarai main", "Import dan eksport senarai main anda", "Lirik dalam talian", "Dapatkan lirik daripada LRCLIB", "Semak kemas kini", "Semak sama ada versi yang lebih baharu tersedia")
        AppLanguage.Norwegian -> SettingsEntryCopy("Kryssfade", "Juster overgangen mellom sanger", "Administrer spillelister", "Importer og eksporter spillelistene dine", "Sangtekster på nett", "Hent sangtekster fra LRCLIB", "Se etter oppdateringer", "Sjekk om en nyere versjon er tilgjengelig")
        AppLanguage.Polish -> SettingsEntryCopy("Przenikanie", "Dostosuj przejście między utworami", "Zarządzaj playlistami", "Importuj i eksportuj playlisty", "Teksty utworów online", "Pobieraj teksty utworów z LRCLIB", "Sprawdź aktualizacje", "Sprawdź, czy jest dostępna nowsza wersja")
        AppLanguage.Portuguese -> SettingsEntryCopy("Crossfade", "Ajustar a transição entre músicas", "Gerir playlists", "Importar e exportar as suas playlists", "Letras online", "Obter letras através do LRCLIB", "Procurar atualizações", "Verificar se está disponível uma versão mais recente")
        AppLanguage.Russian -> SettingsEntryCopy("Кроссфейд", "Настройте переход между треками", "Управление плейлистами", "Импорт и экспорт ваших плейлистов", "Тексты песен онлайн", "Загружать тексты песен из LRCLIB", "Проверить обновления", "Проверить наличие новой версии")
        AppLanguage.Serbian -> SettingsEntryCopy("Унакрсно преливање", "Подесите прелаз између песама", "Управљање плејлистама", "Увезите и извезите своје плејлисте", "Текстови песама на мрежи", "Преузмите текстове песама са LRCLIB-а", "Провери ажурирања", "Проверите да ли је доступна новија верзија")
        AppLanguage.Slovak -> SettingsEntryCopy("Prelínanie", "Nastavte prechod medzi skladbami", "Spravovať playlisty", "Importujte a exportujte svoje playlisty", "Texty piesní online", "Načítajte texty piesní z LRCLIB", "Skontrolovať aktualizácie", "Skontrolujte, či je k dispozícii novšia verzia")
        AppLanguage.Spanish -> SettingsEntryCopy("Fundido cruzado", "Ajustar la transición entre canciones", "Gestionar playlists", "Importar y exportar tus playlists", "Letras en línea", "Obtener letras de LRCLIB", "Buscar actualizaciones", "Comprobar si hay una versión más reciente")
        AppLanguage.Swedish -> SettingsEntryCopy("Övertoning", "Justera övergången mellan låtar", "Hantera spellistor", "Importera och exportera dina spellistor", "Låttexter online", "Hämta låttexter från LRCLIB", "Sök efter uppdateringar", "Kontrollera om en nyare version är tillgänglig")
        AppLanguage.Thai -> SettingsEntryCopy("ครอสเฟด", "ปรับช่วงเปลี่ยนระหว่างเพลง", "จัดการเพลย์ลิสต์", "นำเข้าและส่งออกเพลย์ลิสต์ของคุณ", "เนื้อเพลงออนไลน์", "ดึงเนื้อเพลงจาก LRCLIB", "ตรวจหาการอัปเดต", "ตรวจสอบว่ามีเวอร์ชันใหม่กว่าหรือไม่")
        AppLanguage.Ukrainian -> SettingsEntryCopy("Кросфейд", "Налаштуйте перехід між треками", "Керування плейлистами", "Імпортуйте та експортуйте свої плейлисти", "Тексти пісень онлайн", "Отримуйте тексти пісень із LRCLIB", "Перевірити оновлення", "Перевірте, чи доступна новіша версія")
        AppLanguage.Urdu -> SettingsEntryCopy("کراس فیڈ", "گانوں کے درمیان منتقلی کو ایڈجسٹ کریں", "پلے لسٹس کا انتظام", "اپنی پلے لسٹس درآمد اور برآمد کریں", "آن لائن گانے کے بول", "LRCLIB سے گانے کے بول حاصل کریں", "اپ ڈیٹس چیک کریں", "چیک کریں کہ نیا ورژن دستیاب ہے یا نہیں")
        AppLanguage.English -> SettingsEntryCopy("Crossfade", "Adjust the transition between songs", "Manage playlists", "Import and export your playlists", "Online lyrics", "Fetch lyrics from LRCLIB", "Check for updates", "Check whether a newer version is available")
    }
    return copy(
        crossfadeTitle = entries.crossfadeTitle,
        crossfadeSubtitle = entries.crossfadeSubtitle,
        managePlaylistsTitle = entries.managePlaylistsTitle,
        managePlaylistsSubtitle = entries.managePlaylistsSubtitle,
        onlineLyricsTitle = entries.onlineLyricsTitle,
        onlineLyricsSubtitle = entries.onlineLyricsSubtitle,
        checkForUpdatesTitle = entries.checkForUpdatesTitle,
        checkForUpdatesSubtitle = entries.checkForUpdatesSubtitle,
    )
}

