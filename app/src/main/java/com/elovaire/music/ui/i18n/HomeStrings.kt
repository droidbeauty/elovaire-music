package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal data class RootUiCopy(
    val firstLaunchPermissionTitle: String,
    val firstLaunchPermissionMessage: String,
    val firstLaunchPermissionButton: String,
    val appName: String,
    val allAlbumsTitle: String,
    val allAlbumsSubtitle: String,
    val renamePlaylistTitle: String,
    val save: String,
    val newPlaylist: String,
    val playlistArtworkPlaceholder: String,
    val createPlaylistButton: String,
    val tapToCreateNewPlaylist: String,
    val playlistNamePlaceholder: String,
    val clearPlaylistName: String,
    val noSongsInPlaylistYet: String,
    val searchLibrary: String,
    val addSongsTitle: String,
    val loadingLyrics: String,
    val noLyrics: String,
    val hideLyrics: String,
    val ok: String,
    val albumNotFound: String,
    val playlistNotFound: String,
    val mostPlayedSongs: String,
    val availableReleasesSuffix: String,
    val playAlbum: String,
    val shuffleAlbum: String,
    val editTags: String,
    val savePlaylistChanges: String,
    val editPlaylist: String,
)

internal fun rootUiCopy(language: AppLanguage): RootUiCopy = when (language) {
    AppLanguage.Polish -> RootUiCopy(
        firstLaunchPermissionTitle = "Dostęp do pamięci jest wymagany, aby rozpocząć",
        firstLaunchPermissionMessage = "Aplikacja domyślnie skanuje folder Muzyka w poszukiwaniu muzyki na urządzeniu",
        firstLaunchPermissionButton = "Zezwól na dostęp do pamięci",
        appName = "Elovaire",
        allAlbumsTitle = "Wszystkie albumy",
        allAlbumsSubtitle = "Alfabetycznie według wykonawcy albumu, a następnie tytułu albumu.",
        renamePlaylistTitle = "Zmień nazwę playlisty",
        save = "Zapisz",
        newPlaylist = "Nowa playlista",
        playlistArtworkPlaceholder = "Symbol zastępczy okładki playlisty",
        createPlaylistButton = "Utwórz playlistę",
        tapToCreateNewPlaylist = "Stuknij, aby utworzyć nową playlistę",
        playlistNamePlaceholder = "Nazwa playlisty",
        clearPlaylistName = "Wyczyść nazwę listy",
        noSongsInPlaylistYet = "W tej liście nie ma jeszcze utworów",
        searchLibrary = "Szukaj w bibliotece",
        addSongsTitle = "Dodaj utwory",
        loadingLyrics = "Ładowanie tekstu…",
        noLyrics = "Wygląda na to, że ten utwór nie ma tekstu",
        hideLyrics = "Ukryj tekst",
        ok = "OK",
        albumNotFound = "Nie znaleziono albumu.",
        playlistNotFound = "Nie znaleziono playlisty.",
        mostPlayedSongs = "Najczęściej odtwarzane utwory",
        availableReleasesSuffix = "dostępne wydania",
        playAlbum = "Odtwórz album",
        shuffleAlbum = "Tasuj album",
        editTags = "Edytuj tagi",
        savePlaylistChanges = "Zapisz zmiany playlisty",
        editPlaylist = "Edytuj playlistę",
    )
    AppLanguage.Slovak -> RootUiCopy(
        firstLaunchPermissionTitle = "Na spustenie je potrebný prístup k úložisku",
        firstLaunchPermissionMessage = "Aplikácia predvolene prehľadáva priečinok Music a hľadá hudbu v zariadení",
        firstLaunchPermissionButton = "Povoliť prístup k úložisku",
        appName = "Elovaire",
        allAlbumsTitle = "Všetky albumy",
        allAlbumsSubtitle = "Abecedne podľa interpreta albumu, potom podľa názvu albumu.",
        renamePlaylistTitle = "Premenovať playlist",
        save = "Uložiť",
        newPlaylist = "Nový playlist",
        playlistArtworkPlaceholder = "Zástupný obrázok playlistu",
        createPlaylistButton = "Vytvoriť playlist",
        tapToCreateNewPlaylist = "Ťuknutím vytvoríte nový playlist",
        playlistNamePlaceholder = "Názov playlistu",
        clearPlaylistName = "Vymazať názov playlistu",
        noSongsInPlaylistYet = "V tomto playliste ešte nie sú žiadne skladby",
        searchLibrary = "Hľadať v knižnici",
        addSongsTitle = "Pridať skladby",
        loadingLyrics = "Načítavajú sa texty…",
        noLyrics = "Zdá sa, že táto skladba nemá text",
        hideLyrics = "Skryť text",
        ok = "OK",
        albumNotFound = "Album sa nenašiel.",
        playlistNotFound = "Playlist sa nenašiel.",
        mostPlayedSongs = "Najčastejšie prehrávané skladby",
        availableReleasesSuffix = "dostupné vydania",
        playAlbum = "Prehrať album",
        shuffleAlbum = "Zamiešať album",
        editTags = "Upraviť tagy",
        savePlaylistChanges = "Uložiť zmeny playlistu",
        editPlaylist = "Upraviť playlist",
    )
    AppLanguage.Korean -> RootUiCopy(
        firstLaunchPermissionTitle = "시작하려면 저장소 접근 권한이 필요합니다",
        firstLaunchPermissionMessage = "앱은 기본적으로 기기의 음악을 찾기 위해 Music 폴더를 스캔합니다",
        firstLaunchPermissionButton = "저장소 접근 허용",
        appName = "Elovaire",
        allAlbumsTitle = "모든 앨범",
        allAlbumsSubtitle = "앨범 아티스트 기준 가나다순, 그다음 앨범 제목순입니다.",
        renamePlaylistTitle = "플레이리스트 이름 변경",
        save = "저장",
        newPlaylist = "새 플레이리스트",
        playlistArtworkPlaceholder = "플레이리스트 아트워크 자리표시자",
        createPlaylistButton = "플레이리스트 만들기",
        tapToCreateNewPlaylist = "탭하여 새 플레이리스트 만들기",
        playlistNamePlaceholder = "플레이리스트 이름",
        clearPlaylistName = "플레이리스트 이름 지우기",
        noSongsInPlaylistYet = "이 플레이리스트에는 아직 곡이 없습니다",
        searchLibrary = "라이브러리 검색",
        addSongsTitle = "곡 추가",
        loadingLyrics = "가사를 불러오는 중…",
        noLyrics = "이 곡에는 가사가 없는 것 같습니다",
        hideLyrics = "가사 숨기기",
        ok = "확인",
        albumNotFound = "앨범을 찾을 수 없습니다.",
        playlistNotFound = "플레이리스트를 찾을 수 없습니다.",
        mostPlayedSongs = "가장 많이 재생한 곡",
        availableReleasesSuffix = "개의 발매반",
        playAlbum = "앨범 재생",
        shuffleAlbum = "앨범 셔플",
        editTags = "태그 편집",
        savePlaylistChanges = "플레이리스트 변경사항 저장",
        editPlaylist = "플레이리스트 편집",
    )
    AppLanguage.Malay -> RootUiCopy(
        firstLaunchPermissionTitle = "Akses storan diperlukan untuk bermula",
        firstLaunchPermissionMessage = "Aplikasi mengimbas folder Music secara lalai untuk mencari muzik pada peranti",
        firstLaunchPermissionButton = "Benarkan akses storan",
        appName = "Elovaire",
        allAlbumsTitle = "Semua album",
        allAlbumsSubtitle = "Disusun mengikut artis album, kemudian tajuk album.",
        renamePlaylistTitle = "Namakan semula senarai main",
        save = "Simpan",
        newPlaylist = "Senarai main baharu",
        playlistArtworkPlaceholder = "Pemegang tempat karya seni senarai main",
        createPlaylistButton = "Cipta senarai main",
        tapToCreateNewPlaylist = "Ketik untuk mencipta senarai main baharu",
        playlistNamePlaceholder = "Nama senarai main",
        clearPlaylistName = "Kosongkan nama senarai main",
        noSongsInPlaylistYet = "Belum ada lagu dalam senarai main ini",
        searchLibrary = "Cari pustaka",
        addSongsTitle = "Tambah lagu",
        loadingLyrics = "Memuatkan lirik…",
        noLyrics = "Lagu ini nampaknya tiada lirik",
        hideLyrics = "Sembunyikan lirik",
        ok = "OK",
        albumNotFound = "Album tidak ditemui.",
        playlistNotFound = "Senarai main tidak ditemui.",
        mostPlayedSongs = "Lagu paling kerap dimainkan",
        availableReleasesSuffix = "keluaran tersedia",
        playAlbum = "Mainkan album",
        shuffleAlbum = "Kocok album",
        editTags = "Edit tag",
        savePlaylistChanges = "Simpan perubahan senarai main",
        editPlaylist = "Edit senarai main",
    )
    AppLanguage.Bengali -> RootUiCopy(
        firstLaunchPermissionTitle = "শুরু করতে স্টোরেজ অ্যাক্সেস প্রয়োজন",
        firstLaunchPermissionMessage = "অ্যাপটি ডিভাইসে গান খুঁজতে ডিফল্টভাবে Music ফোল্ডার স্ক্যান করে",
        firstLaunchPermissionButton = "স্টোরেজ অ্যাক্সেস দিন",
        appName = "Elovaire",
        allAlbumsTitle = "সব অ্যালবাম",
        allAlbumsSubtitle = "অ্যালবাম শিল্পী অনুযায়ী, তারপর অ্যালবামের শিরোনাম অনুযায়ী বর্ণানুক্রমে।",
        renamePlaylistTitle = "প্লেলিস্টের নাম বদলান",
        save = "সংরক্ষণ করুন",
        newPlaylist = "নতুন প্লেলিস্ট",
        playlistArtworkPlaceholder = "প্লেলিস্ট কভার প্লেসহোল্ডার",
        createPlaylistButton = "প্লেলিস্ট তৈরি করুন",
        tapToCreateNewPlaylist = "ট্যাপ করে নতুন প্লেলিস্ট তৈরি করুন",
        playlistNamePlaceholder = "প্লেলিস্টের নাম",
        clearPlaylistName = "প্লেলিস্টের নাম মুছুন",
        noSongsInPlaylistYet = "এই প্লেলিস্টে এখনো কোনো গান নেই",
        searchLibrary = "লাইব্রেরি খুঁজুন",
        addSongsTitle = "গান যোগ করুন",
        loadingLyrics = "গানের কথা লোড হচ্ছে…",
        noLyrics = "মনে হচ্ছে এই গানের কথা নেই",
        hideLyrics = "গানের কথা লুকান",
        ok = "ঠিক আছে",
        albumNotFound = "অ্যালবাম পাওয়া যায়নি।",
        playlistNotFound = "প্লেলিস্ট পাওয়া যায়নি।",
        mostPlayedSongs = "সবচেয়ে বেশি শোনা গান",
        availableReleasesSuffix = "টি উপলভ্য সংস্করণ",
        playAlbum = "অ্যালবাম চালান",
        shuffleAlbum = "অ্যালবাম শাফল করুন",
        editTags = "ট্যাগ সম্পাদনা",
        savePlaylistChanges = "প্লেলিস্টের পরিবর্তন সংরক্ষণ করুন",
        editPlaylist = "প্লেলিস্ট সম্পাদনা",
    )
    AppLanguage.Urdu -> RootUiCopy(
        firstLaunchPermissionTitle = "شروع کرنے کے لیے اسٹوریج تک رسائی درکار ہے",
        firstLaunchPermissionMessage = "ایپ ڈیفالٹ طور پر آلے میں موسیقی تلاش کرنے کے لیے Music فولڈر اسکین کرتی ہے",
        firstLaunchPermissionButton = "اسٹوریج تک رسائی دیں",
        appName = "Elovaire",
        allAlbumsTitle = "تمام البمز",
        allAlbumsSubtitle = "پہلے البم آرٹسٹ، پھر البم عنوان کے مطابق حروف تہجی ترتیب۔",
        renamePlaylistTitle = "پلے لسٹ کا نام تبدیل کریں",
        save = "محفوظ کریں",
        newPlaylist = "نئی پلے لسٹ",
        playlistArtworkPlaceholder = "پلے لسٹ آرٹ ورک پلیس ہولڈر",
        createPlaylistButton = "پلے لسٹ بنائیں",
        tapToCreateNewPlaylist = "نئی پلے لسٹ بنانے کے لیے ٹیپ کریں",
        playlistNamePlaceholder = "پلے لسٹ کا نام",
        clearPlaylistName = "پلے لسٹ کا نام صاف کریں",
        noSongsInPlaylistYet = "اس پلے لسٹ میں ابھی کوئی گانا نہیں ہے",
        searchLibrary = "لائبریری تلاش کریں",
        addSongsTitle = "گانے شامل کریں",
        loadingLyrics = "بول لوڈ ہو رہے ہیں…",
        noLyrics = "لگتا ہے اس گانے کے بول موجود نہیں ہیں",
        hideLyrics = "بول چھپائیں",
        ok = "ٹھیک ہے",
        albumNotFound = "البم نہیں ملا۔",
        playlistNotFound = "پلے لسٹ نہیں ملی۔",
        mostPlayedSongs = "سب سے زیادہ چلنے والے گانے",
        availableReleasesSuffix = "دستیاب ریلیزز",
        playAlbum = "البم چلائیں",
        shuffleAlbum = "البم شفل کریں",
        editTags = "ٹیگز میں ترمیم کریں",
        savePlaylistChanges = "پلے لسٹ کی تبدیلیاں محفوظ کریں",
        editPlaylist = "پلے لسٹ میں ترمیم کریں",
    )
    else -> RootUiCopy(
        firstLaunchPermissionTitle = "Storage access required to start",
        firstLaunchPermissionMessage = "The app scans Music folder by default in search for music on the device",
        firstLaunchPermissionButton = "Allow storage access",
        appName = "Elovaire",
        allAlbumsTitle = "All albums",
        allAlbumsSubtitle = "Alphabetical by album artist, then album title.",
        renamePlaylistTitle = "Rename playlist",
        save = "Save",
        newPlaylist = "New playlist",
        playlistArtworkPlaceholder = "Playlist artwork placeholder",
        createPlaylistButton = "Create playlist",
        tapToCreateNewPlaylist = "Tap to create new playlist",
        playlistNamePlaceholder = "Playlist name",
        clearPlaylistName = "Clear playlist name",
        noSongsInPlaylistYet = "No songs in this playlist yet",
        searchLibrary = "Search library",
        addSongsTitle = "Add songs",
        loadingLyrics = "Loading lyrics...",
        noLyrics = "This song seems to have no lyrics",
        hideLyrics = "Hide lyrics",
        ok = "OK",
        albumNotFound = "Album not found.",
        playlistNotFound = "Playlist not found.",
        mostPlayedSongs = "Most played songs",
        availableReleasesSuffix = "available releases",
        playAlbum = "Play album",
        shuffleAlbum = "Shuffle album",
        editTags = "Edit tags",
        savePlaylistChanges = "Save playlist changes",
        editPlaylist = "Edit playlist",
    )
}.withLocalizedFirstLaunchPermission(language)

private fun RootUiCopy.withLocalizedFirstLaunchPermission(language: AppLanguage): RootUiCopy {
    return copy(
        firstLaunchPermissionTitle = firstLaunchPermissionTitle(language),
        firstLaunchPermissionMessage = firstLaunchPermissionMessage(language),
        firstLaunchPermissionButton = firstLaunchPermissionButton(language),
    )
}

private fun firstLaunchPermissionTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Dostęp do pamięci jest wymagany, aby rozpocząć"
    AppLanguage.Albanian -> "Kërkohet qasje në hapësirën ruajtëse"
    AppLanguage.ChineseSimplified -> "需要存储访问权限才能开始"
    AppLanguage.Croatian -> "Za početak je potreban pristup pohrani"
    AppLanguage.Czech -> "Pro spuštění je potřeba přístup k úložišti"
    AppLanguage.Danish -> "Lageradgang kræves for at starte"
    AppLanguage.Dutch -> "Opslagtoegang vereist om te beginnen"
    AppLanguage.Estonian -> "Alustamiseks on vaja juurdepääsu salvestusruumile"
    AppLanguage.French -> "Accès au stockage requis pour commencer"
    AppLanguage.German -> "Speicherzugriff zum Start erforderlich"
    AppLanguage.Greek -> "Απαιτείται πρόσβαση στον χώρο αποθήκευσης"
    AppLanguage.Hindi -> "शुरू करने के लिए स्टोरेज एक्सेस चाहिए"
    AppLanguage.Hungarian -> "A kezdéshez tárhely-hozzáférés szükséges"
    AppLanguage.Italian -> "Accesso allo spazio di archiviazione richiesto"
    AppLanguage.Japanese -> "開始するにはストレージアクセスが必要です"
    AppLanguage.Latin -> "Accessus ad repositorium requiritur"
    AppLanguage.Latvian -> "Lai sāktu, vajadzīga piekļuve krātuvei"
    AppLanguage.Lithuanian -> "Norint pradėti reikia saugyklos prieigos"
    AppLanguage.Macedonian -> "Потребен е пристап до складиште"
    AppLanguage.Norwegian -> "Lagringstilgang kreves for å starte"
    AppLanguage.Portuguese -> "É necessário acesso ao armazenamento"
    AppLanguage.Russian -> "Для начала нужен доступ к хранилищу"
    AppLanguage.Serbian -> "Потребан је приступ складишту"
    AppLanguage.Spanish -> "Se necesita acceso al almacenamiento"
    AppLanguage.Swedish -> "Lagringsåtkomst krävs för att börja"
    AppLanguage.Thai -> "ต้องให้สิทธิ์เข้าถึงพื้นที่เก็บข้อมูลเพื่อเริ่ม"
    AppLanguage.Ukrainian -> "Для початку потрібен доступ до сховища"
    AppLanguage.Slovak -> "Na spustenie je potrebný prístup k úložisku"
    AppLanguage.Korean -> "시작하려면 저장소 접근 권한이 필요합니다"
    AppLanguage.Malay -> "Akses storan diperlukan untuk bermula"
    AppLanguage.Bengali -> "শুরু করতে স্টোরেজ অ্যাক্সেস প্রয়োজন"
    AppLanguage.Urdu -> "شروع کرنے کے لیے اسٹوریج تک رسائی درکار ہے"
    AppLanguage.English -> "Storage access required to start"
}

private fun firstLaunchPermissionMessage(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Aplikacja domyślnie skanuje folder Muzyka w poszukiwaniu muzyki na urządzeniu"
    AppLanguage.Albanian -> "Aplikacioni skanon si parazgjedhje dosjen Music për të gjetur muzikë në pajisje"
    AppLanguage.ChineseSimplified -> "应用默认扫描 Music 文件夹，以查找设备上的音乐"
    AppLanguage.Croatian -> "Aplikacija prema zadanim postavkama skenira mapu Music kako bi pronašla glazbu na uređaju"
    AppLanguage.Czech -> "Aplikace ve výchozím nastavení skenuje složku Music a hledá hudbu v zařízení"
    AppLanguage.Danish -> "Appen scanner som standard Music-mappen for at finde musik på enheden"
    AppLanguage.Dutch -> "De app scant standaard de map Music om muziek op het apparaat te vinden"
    AppLanguage.Estonian -> "Rakendus skannib vaikimisi kausta Music, et leida seadmest muusikat"
    AppLanguage.French -> "L’application analyse par défaut le dossier Music pour trouver la musique sur l’appareil"
    AppLanguage.German -> "Die App scannt standardmäßig den Ordner Music, um Musik auf dem Gerät zu finden"
    AppLanguage.Greek -> "Η εφαρμογή σαρώνει από προεπιλογή τον φάκελο Music για να βρει μουσική στη συσκευή"
    AppLanguage.Hindi -> "ऐप डिवाइस पर संगीत खोजने के लिए डिफ़ॉल्ट रूप से Music फ़ोल्डर स्कैन करता है"
    AppLanguage.Hungarian -> "Az app alapértelmezés szerint a Music mappát olvassa be, hogy zenét találjon az eszközön"
    AppLanguage.Italian -> "L’app scansiona per impostazione predefinita la cartella Music per trovare musica sul dispositivo"
    AppLanguage.Japanese -> "アプリは既定で Music フォルダをスキャンし、端末上の音楽を探します"
    AppLanguage.Latin -> "App folder Music per defaltum scrutatur ut musicam in machina inveniat"
    AppLanguage.Latvian -> "Lietotne pēc noklusējuma skenē mapi Music, lai ierīcē atrastu mūziku"
    AppLanguage.Lithuanian -> "Programa pagal numatymą skenuoja Music aplanką, kad rastų muziką įrenginyje"
    AppLanguage.Macedonian -> "Апликацијата стандардно ја скенира папката Music за да најде музика на уредот"
    AppLanguage.Norwegian -> "Appen skanner Music-mappen som standard for å finne musikk på enheten"
    AppLanguage.Portuguese -> "A app analisa por predefinição a pasta Music para encontrar música no dispositivo"
    AppLanguage.Russian -> "Приложение по умолчанию сканирует папку Music, чтобы найти музыку на устройстве"
    AppLanguage.Serbian -> "Апликација подразумевано скенира фасциклу Music да пронађе музику на уређају"
    AppLanguage.Spanish -> "La app escanea de forma predeterminada la carpeta Music para buscar música en el dispositivo"
    AppLanguage.Swedish -> "Appen skannar som standard Music-mappen för att hitta musik på enheten"
    AppLanguage.Thai -> "แอปจะสแกนโฟลเดอร์ Music เป็นค่าเริ่มต้นเพื่อค้นหาเพลงในอุปกรณ์"
    AppLanguage.Ukrainian -> "Застосунок типово сканує папку Music, щоб знайти музику на пристрої"
    AppLanguage.Slovak -> "Aplikácia predvolene prehľadáva priečinok Music a hľadá hudbu v zariadení"
    AppLanguage.Korean -> "앱은 기본적으로 기기의 음악을 찾기 위해 Music 폴더를 스캔합니다"
    AppLanguage.Malay -> "Aplikasi mengimbas folder Music secara lalai untuk mencari muzik pada peranti"
    AppLanguage.Bengali -> "অ্যাপটি ডিভাইসে গান খুঁজতে ডিফল্টভাবে Music ফোল্ডার স্ক্যান করে"
    AppLanguage.Urdu -> "ایپ ڈیفالٹ طور پر آلے میں موسیقی تلاش کرنے کے لیے Music فولڈر اسکین کرتی ہے"
    AppLanguage.English -> "The app scans Music folder by default in search for music on the device"
}

private fun firstLaunchPermissionButton(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Zezwól na dostęp do pamięci"
    AppLanguage.Albanian -> "Lejo qasjen në hapësirën ruajtëse"
    AppLanguage.ChineseSimplified -> "允许存储访问"
    AppLanguage.Croatian -> "Dopusti pristup pohrani"
    AppLanguage.Czech -> "Povolit přístup k úložišti"
    AppLanguage.Danish -> "Tillad lageradgang"
    AppLanguage.Dutch -> "Opslagtoegang toestaan"
    AppLanguage.Estonian -> "Luba juurdepääs salvestusruumile"
    AppLanguage.French -> "Autoriser l’accès au stockage"
    AppLanguage.German -> "Speicherzugriff erlauben"
    AppLanguage.Greek -> "Να επιτραπεί η πρόσβαση στον χώρο αποθήκευσης"
    AppLanguage.Hindi -> "स्टोरेज एक्सेस दें"
    AppLanguage.Hungarian -> "Tárhely-hozzáférés engedélyezése"
    AppLanguage.Italian -> "Consenti accesso allo spazio"
    AppLanguage.Japanese -> "ストレージアクセスを許可"
    AppLanguage.Latin -> "Accessum ad repositorium sine"
    AppLanguage.Latvian -> "Atļaut piekļuvi krātuvei"
    AppLanguage.Lithuanian -> "Leisti saugyklos prieigą"
    AppLanguage.Macedonian -> "Дозволи пристап до складиште"
    AppLanguage.Norwegian -> "Tillat lagringstilgang"
    AppLanguage.Portuguese -> "Permitir acesso ao armazenamento"
    AppLanguage.Russian -> "Разрешить доступ к хранилищу"
    AppLanguage.Serbian -> "Дозволи приступ складишту"
    AppLanguage.Spanish -> "Permitir acceso al almacenamiento"
    AppLanguage.Swedish -> "Tillåt lagringsåtkomst"
    AppLanguage.Thai -> "อนุญาตการเข้าถึงพื้นที่เก็บข้อมูล"
    AppLanguage.Ukrainian -> "Дозволити доступ до сховища"
    AppLanguage.Slovak -> "Povoliť prístup k úložisku"
    AppLanguage.Korean -> "저장소 접근 허용"
    AppLanguage.Malay -> "Benarkan akses storan"
    AppLanguage.Bengali -> "স্টোরেজ অ্যাক্সেস দিন"
    AppLanguage.Urdu -> "اسٹوریج تک رسائی دیں"
    AppLanguage.English -> "Allow storage access"
}

internal fun availableReleasesLabel(count: Int, language: AppLanguage): String {
    val copy = rootUiCopy(language)
    return "$count ${copy.availableReleasesSuffix}"
}

internal fun repeatModeLabel(repeatMode: PlaybackRepeatMode, language: AppLanguage): String = when (repeatMode) {
    PlaybackRepeatMode.Off -> when (language) {
        AppLanguage.Polish -> "Kolejność"
        AppLanguage.Slovak -> "Poradie"
        AppLanguage.Korean -> "순서"
        AppLanguage.Malay -> "Turutan"
        AppLanguage.Bengali -> "ক্রম"
        AppLanguage.Urdu -> "ترتیب"
        else -> "Order"
    }
    PlaybackRepeatMode.One -> when (language) {
        AppLanguage.Polish -> "Powtórz jeden"
        AppLanguage.Slovak -> "Opakovať jednu"
        AppLanguage.Korean -> "한 곡 반복"
        AppLanguage.Malay -> "Ulang satu"
        AppLanguage.Bengali -> "একটি পুনরাবৃত্তি"
        AppLanguage.Urdu -> "ایک کو دہرائیں"
        else -> "Repeat one"
    }
    PlaybackRepeatMode.All -> when (language) {
        AppLanguage.Polish -> "Powtórz wszystko"
        AppLanguage.Slovak -> "Opakovať všetko"
        AppLanguage.Korean -> "전체 반복"
        AppLanguage.Malay -> "Ulang semua"
        AppLanguage.Bengali -> "সব পুনরাবৃত্তি"
        AppLanguage.Urdu -> "سب دہرائیں"
        else -> "Repeat all"
    }
}

internal fun ReverbProfile.displayLabel(language: AppLanguage): String = when (this) {
    ReverbProfile.Dry -> uiPhrase(language, UiPhrase.Dry)
    ReverbProfile.Wet -> uiPhrase(language, UiPhrase.Wet)
}

internal data class HomeUiCopy(
    val indexingTitle: String,
    val indexingMessage: String,
    val emptyLibraryTitle: String,
    val emptyLibraryMessage: String,
    val noRecentAdditionsTitle: String,
    val noRecentAdditionsMessage: String,
    val recentlyPlayedSongsTitle: String,
    val recentlyPlayedSongsEmpty: String,
    val favoriteAlbumsTitle: String,
    val favoriteAlbumsSubtitle: String,
    val noFavoriteAlbumsTitle: String,
    val noFavoriteAlbumsMessage: String,
)

internal fun homeCopy(language: AppLanguage): HomeUiCopy = when (language) {
    AppLanguage.Polish -> HomeUiCopy(
        indexingTitle = "Trwa indeksowanie biblioteki",
        indexingMessage = "Utwory i albumy pojawią się po zakończeniu indeksowania",
        emptyLibraryTitle = "Nie znaleziono muzyki",
        emptyLibraryMessage = "Utwory i albumy pojawią się tutaj, gdy dodasz muzykę do domyślnego folderu Muzyka na urządzeniu",
        noRecentAdditionsTitle = "Brak ostatnio dodanych",
        noRecentAdditionsMessage = "Dodaj albumy do folderu Muzyka na urządzeniu, a najnowsze pojawią się tutaj automatycznie",
        recentlyPlayedSongsTitle = "Ostatnio odtwarzane",
        recentlyPlayedSongsEmpty = "Utwory pojawią się tutaj wkrótce",
        favoriteAlbumsTitle = "Twoje ulubione albumy",
        favoriteAlbumsSubtitle = "Muzyka, do której często wracasz",
        noFavoriteAlbumsTitle = "Nie otwarto jeszcze żadnych albumów",
        noFavoriteAlbumsMessage = "Otwórz lub odtwórz dowolny album, a pojawi się tutaj z okładką na pierwszym planie",
    )
    AppLanguage.ChineseSimplified -> HomeUiCopy(
        indexingTitle = "正在索引媒体库",
        indexingMessage = "索引完成后，这里会显示歌曲和专辑",
        emptyLibraryTitle = "未找到音乐",
        emptyLibraryMessage = "当你将音乐添加到设备默认的 Music 文件夹后，这里会显示歌曲和专辑",
        noRecentAdditionsTitle = "还没有最近添加内容",
        noRecentAdditionsMessage = "将专辑添加到设备的 Music 文件夹后，最新内容会自动显示在这里",
        recentlyPlayedSongsTitle = "最近播放",
        recentlyPlayedSongsEmpty = "歌曲很快就会显示在这里",
        favoriteAlbumsTitle = "你喜爱的专辑",
        favoriteAlbumsSubtitle = "你会经常回听的音乐",
        noFavoriteAlbumsTitle = "还没有打开过任何专辑",
        noFavoriteAlbumsMessage = "打开或播放任意专辑后，它就会带着封面显示在这里",
    )
    AppLanguage.Croatian -> HomeUiCopy(
        indexingTitle = "Indeksiranje biblioteke",
        indexingMessage = "Pjesme i albumi pojavit će se kada indeksiranje završi",
        emptyLibraryTitle = "Nije pronađena glazba",
        emptyLibraryMessage = "Pjesme i albumi pojavit će se ovdje kada dodate glazbu u zadanu mapu Music na uređaju",
        noRecentAdditionsTitle = "Nema nedavnih dodataka",
        noRecentAdditionsMessage = "Dodajte albume u mapu Music na uređaju i najnoviji će se ovdje automatski pojaviti",
        recentlyPlayedSongsTitle = "Nedavno reproducirano",
        recentlyPlayedSongsEmpty = "Pjesme će se ovdje uskoro pojaviti",
        favoriteAlbumsTitle = "Vaši omiljeni albumi",
        favoriteAlbumsSubtitle = "Glazba kojoj se često vraćate",
        noFavoriteAlbumsTitle = "Još nijedan album nije otvoren",
        noFavoriteAlbumsMessage = "Otvorite ili reproducirajte bilo koji album i ovdje će se pojaviti s naslovnicom u prvom planu",
    )
    AppLanguage.Czech -> HomeUiCopy(
        indexingTitle = "Indexuje se knihovna",
        indexingMessage = "Skladby a alba se zobrazí po dokončení indexace",
        emptyLibraryTitle = "Nebyla nalezena žádná hudba",
        emptyLibraryMessage = "Skladby a alba se zde zobrazí, jakmile přidáte hudbu do výchozí složky Music v zařízení",
        noRecentAdditionsTitle = "Zatím nic nového",
        noRecentAdditionsMessage = "Přidejte alba do složky Music v zařízení a nejnovější se zde objeví automaticky",
        recentlyPlayedSongsTitle = "Nedávno přehráno",
        recentlyPlayedSongsEmpty = "Skladby se zde brzy objeví",
        favoriteAlbumsTitle = "Vaše oblíbená alba",
        favoriteAlbumsSubtitle = "Hudba, ke které se často vracíte",
        noFavoriteAlbumsTitle = "Zatím nebyla otevřena žádná alba",
        noFavoriteAlbumsMessage = "Otevřete nebo přehrajte libovolné album a zobrazí se zde s obalem v popředí",
    )
    AppLanguage.Danish -> HomeUiCopy(
        indexingTitle = "Indekserer bibliotek",
        indexingMessage = "Sange og album vises, når indekseringen er færdig",
        emptyLibraryTitle = "Ingen musik fundet",
        emptyLibraryMessage = "Sange og album vises her, når du føjer musik til enhedens standardmappe Music",
        noRecentAdditionsTitle = "Ingen nylige tilføjelser endnu",
        noRecentAdditionsMessage = "Tilføj album til enhedens Music-mappe, så vises de nyeste automatisk her",
        recentlyPlayedSongsTitle = "Nyligt afspillet",
        recentlyPlayedSongsEmpty = "Sange vises snart her",
        favoriteAlbumsTitle = "Dine favoritalbum",
        favoriteAlbumsSubtitle = "Musik du ofte vender tilbage til",
        noFavoriteAlbumsTitle = "Ingen album er åbnet endnu",
        noFavoriteAlbumsMessage = "Åbn eller afspil et album, så vises det her med omslaget i centrum",
    )
    AppLanguage.Dutch -> HomeUiCopy(
        indexingTitle = "Bibliotheek wordt geïndexeerd",
        indexingMessage = "Nummers en albums verschijnen zodra het indexeren klaar is",
        emptyLibraryTitle = "Geen muziek gevonden",
        emptyLibraryMessage = "Nummers en albums verschijnen hier zodra je muziek toevoegt aan de standaardmap Music op je apparaat",
        noRecentAdditionsTitle = "Nog geen recente toevoegingen",
        noRecentAdditionsMessage = "Voeg albums toe aan de Music-map van je apparaat en de nieuwste verschijnen hier automatisch",
        recentlyPlayedSongsTitle = "Recent afgespeeld",
        recentlyPlayedSongsEmpty = "Nummers verschijnen hier binnenkort",
        favoriteAlbumsTitle = "Je favoriete albums",
        favoriteAlbumsSubtitle = "Muziek waar je vaak naar terugkeert",
        noFavoriteAlbumsTitle = "Er zijn nog geen albums geopend",
        noFavoriteAlbumsMessage = "Open of speel een album af en het verschijnt hier met de hoes prominent in beeld",
    )
    AppLanguage.Estonian -> HomeUiCopy(
        indexingTitle = "Teeki indekseeritakse",
        indexingMessage = "Lood ja albumid kuvatakse pärast indekseerimise lõppu",
        emptyLibraryTitle = "Muusikat ei leitud",
        emptyLibraryMessage = "Lood ja albumid ilmuvad siia, kui lisate muusikat seadme vaikimisi Music kausta",
        noRecentAdditionsTitle = "Hiljutisi lisamisi veel pole",
        noRecentAdditionsMessage = "Lisage albumid seadme Music kausta ja uusimad ilmuvad siia automaatselt",
        recentlyPlayedSongsTitle = "Hiljuti esitatud",
        recentlyPlayedSongsEmpty = "Lood ilmuvad siia varsti",
        favoriteAlbumsTitle = "Sinu lemmikalbumid",
        favoriteAlbumsSubtitle = "Muusika, mille juurde tihti tagasi pöördud",
        noFavoriteAlbumsTitle = "Ühtegi albumit pole veel avatud",
        noFavoriteAlbumsMessage = "Ava või esita mõni album ning see ilmub siia koos esikaanega",
    )
    AppLanguage.French -> HomeUiCopy(
        indexingTitle = "Indexation de la bibliothèque",
        indexingMessage = "Les morceaux et les albums apparaîtront une fois l’indexation terminée",
        emptyLibraryTitle = "Aucune musique trouvée",
        emptyLibraryMessage = "Les morceaux et les albums apparaîtront ici dès que vous ajouterez de la musique au dossier Music par défaut de l’appareil",
        noRecentAdditionsTitle = "Aucun ajout récent",
        noRecentAdditionsMessage = "Ajoutez des albums au dossier Music de l’appareil et les plus récents apparaîtront ici automatiquement",
        recentlyPlayedSongsTitle = "Récemment lus",
        recentlyPlayedSongsEmpty = "Les morceaux apparaîtront bientôt ici",
        favoriteAlbumsTitle = "Vos albums favoris",
        favoriteAlbumsSubtitle = "La musique vers laquelle vous revenez souvent",
        noFavoriteAlbumsTitle = "Aucun album n’a encore été ouvert",
        noFavoriteAlbumsMessage = "Ouvrez ou lisez un album et il apparaîtra ici avec sa pochette bien en évidence",
    )
    AppLanguage.German -> HomeUiCopy(
        indexingTitle = "Bibliothek wird indiziert",
        indexingMessage = "Songs und Alben erscheinen nach Abschluss der Indizierung",
        emptyLibraryTitle = "Keine Musik gefunden",
        emptyLibraryMessage = "Songs und Alben erscheinen hier, sobald du Musik zum Standardordner Music auf deinem Gerät hinzufügst",
        noRecentAdditionsTitle = "Noch keine Neuheiten",
        noRecentAdditionsMessage = "Füge Alben zum Music-Ordner deines Geräts hinzu, dann erscheinen die neuesten hier automatisch",
        recentlyPlayedSongsTitle = "Zuletzt gespielt",
        recentlyPlayedSongsEmpty = "Songs werden hier bald angezeigt",
        favoriteAlbumsTitle = "Deine Lieblingsalben",
        favoriteAlbumsSubtitle = "Musik, zu der du oft zurückkehrst",
        noFavoriteAlbumsTitle = "Noch keine Alben geöffnet",
        noFavoriteAlbumsMessage = "Öffne oder spiele ein Album ab und es erscheint hier mit dem Cover im Mittelpunkt",
    )
    AppLanguage.Greek -> HomeUiCopy(
        indexingTitle = "Γίνεται ευρετηρίαση της βιβλιοθήκης",
        indexingMessage = "Τα τραγούδια και τα άλμπουμ θα εμφανιστούν όταν ολοκληρωθεί η ευρετηρίαση",
        emptyLibraryTitle = "Δεν βρέθηκε μουσική",
        emptyLibraryMessage = "Τα τραγούδια και τα άλμπουμ θα εμφανιστούν εδώ όταν προσθέσετε μουσική στον προεπιλεγμένο φάκελο Music της συσκευής",
        noRecentAdditionsTitle = "Δεν υπάρχουν πρόσφατες προσθήκες",
        noRecentAdditionsMessage = "Προσθέστε άλμπουμ στον φάκελο Music της συσκευής και τα νεότερα θα εμφανίζονται εδώ αυτόματα",
        recentlyPlayedSongsTitle = "Πρόσφατα παιγμένα",
        recentlyPlayedSongsEmpty = "Τα τραγούδια θα εμφανιστούν εδώ σύντομα",
        favoriteAlbumsTitle = "Τα αγαπημένα σας άλμπουμ",
        favoriteAlbumsSubtitle = "Μουσική στην οποία επιστρέφετε συχνά",
        noFavoriteAlbumsTitle = "Δεν έχει ανοίξει ακόμη κανένα άλμπουμ",
        noFavoriteAlbumsMessage = "Ανοίξτε ή αναπαράγετε οποιοδήποτε άλμπουμ και θα εμφανιστεί εδώ με το εξώφυλλό του μπροστά",
    )
    AppLanguage.Hindi -> HomeUiCopy(
        indexingTitle = "लाइब्रेरी इंडेक्स की जा रही है",
        indexingMessage = "इंडेक्स पूरा होने पर गाने और एल्बम यहाँ दिखेंगे",
        emptyLibraryTitle = "कोई संगीत नहीं मिला",
        emptyLibraryMessage = "जब आप अपने डिवाइस के डिफ़ॉल्ट Music फ़ोल्डर में संगीत जोड़ेंगे, तब गाने और एल्बम यहाँ दिखेंगे",
        noRecentAdditionsTitle = "अभी तक कोई हालिया जोड़ नहीं",
        noRecentAdditionsMessage = "डिवाइस के Music फ़ोल्डर में एल्बम जोड़ें और नए एल्बम यहाँ अपने आप दिखेंगे",
        recentlyPlayedSongsTitle = "हाल ही में चलाए गए",
        recentlyPlayedSongsEmpty = "गाने यहाँ जल्द दिखाई देंगे",
        favoriteAlbumsTitle = "आपके पसंदीदा एल्बम",
        favoriteAlbumsSubtitle = "वह संगीत जिसे आप बार-बार सुनते हैं",
        noFavoriteAlbumsTitle = "अभी तक कोई एल्बम नहीं खोला गया",
        noFavoriteAlbumsMessage = "कोई भी एल्बम खोलें या चलाएँ, वह यहाँ अपने कवर के साथ दिखाई देगा",
    )
    AppLanguage.Hungarian -> HomeUiCopy(
        indexingTitle = "A könyvtár indexelése folyamatban",
        indexingMessage = "A dalok és albumok az indexelés befejezése után jelennek meg",
        emptyLibraryTitle = "Nem található zene",
        emptyLibraryMessage = "A dalok és albumok itt jelennek meg, amikor zenét ad hozzá az eszköz alapértelmezett Music mappájához",
        noRecentAdditionsTitle = "Még nincsenek friss hozzáadások",
        noRecentAdditionsMessage = "Adjon albumokat az eszköz Music mappájához, és a legújabbak automatikusan itt jelennek meg",
        recentlyPlayedSongsTitle = "Nemrég lejátszott",
        recentlyPlayedSongsEmpty = "A dalok hamarosan itt jelennek meg",
        favoriteAlbumsTitle = "Kedvenc albumai",
        favoriteAlbumsSubtitle = "Zene, amelyhez gyakran visszatér",
        noFavoriteAlbumsTitle = "Még nem nyitott meg albumot",
        noFavoriteAlbumsMessage = "Nyisson meg vagy játsszon le egy albumot, és az itt jelenik meg a borítójával középpontban",
    )
    AppLanguage.Italian -> HomeUiCopy(
        indexingTitle = "Indicizzazione libreria in corso",
        indexingMessage = "Brani e album appariranno quando l’indicizzazione sarà completata",
        emptyLibraryTitle = "Nessuna musica trovata",
        emptyLibraryMessage = "Brani e album appariranno qui quando aggiungerai musica alla cartella Music predefinita del dispositivo",
        noRecentAdditionsTitle = "Nessuna aggiunta recente",
        noRecentAdditionsMessage = "Aggiungi album alla cartella Music del dispositivo e i più recenti appariranno qui automaticamente",
        recentlyPlayedSongsTitle = "Ascoltati di recente",
        recentlyPlayedSongsEmpty = "I brani appariranno qui presto",
        favoriteAlbumsTitle = "I tuoi album preferiti",
        favoriteAlbumsSubtitle = "La musica a cui torni spesso",
        noFavoriteAlbumsTitle = "Nessun album è stato ancora aperto",
        noFavoriteAlbumsMessage = "Apri o riproduci un album e apparirà qui con la copertina in primo piano",
    )
    AppLanguage.Japanese -> HomeUiCopy(
        indexingTitle = "ライブラリを索引中です",
        indexingMessage = "索引が完了すると、曲とアルバムがここに表示されます",
        emptyLibraryTitle = "音楽が見つかりませんでした",
        emptyLibraryMessage = "デバイスの既定の Music フォルダに音楽を追加すると、曲とアルバムがここに表示されます",
        noRecentAdditionsTitle = "最近追加された項目はまだありません",
        noRecentAdditionsMessage = "デバイスの Music フォルダにアルバムを追加すると、最新のものがここに自動で表示されます",
        recentlyPlayedSongsTitle = "最近再生",
        recentlyPlayedSongsEmpty = "曲はまもなくここに表示されます",
        favoriteAlbumsTitle = "お気に入りのアルバム",
        favoriteAlbumsSubtitle = "何度も聴きたくなる音楽",
        noFavoriteAlbumsTitle = "まだアルバムは開かれていません",
        noFavoriteAlbumsMessage = "アルバムを開くか再生すると、そのアートワークとともにここに表示されます",
    )
    AppLanguage.Latin -> HomeUiCopy(
        indexingTitle = "Bibliotheca indicatur",
        indexingMessage = "Cantus et albumina hic apparebunt post indicem confectum",
        emptyLibraryTitle = "Nulla musica inventa est",
        emptyLibraryMessage = "Cantus et albumina hic apparebunt cum musicam in folder Music praeordinatum addideris",
        noRecentAdditionsTitle = "Nullae recentes additiones",
        noRecentAdditionsMessage = "Albumina ad folder Music adde et novissima hic sponte apparebunt",
        recentlyPlayedSongsTitle = "Nuper acta",
        recentlyPlayedSongsEmpty = "Cantus hic mox apparebunt",
        favoriteAlbumsTitle = "Albumina tua dilecta",
        favoriteAlbumsSubtitle = "Musica ad quam saepe redis",
        noFavoriteAlbumsTitle = "Nullum album adhuc apertum est",
        noFavoriteAlbumsMessage = "Aperi vel cane quodlibet album et hic apparebit cum imagine principali",
    )
    AppLanguage.Latvian -> HomeUiCopy(
        indexingTitle = "Bibliotēka tiek indeksēta",
        indexingMessage = "Dziesmas un albumi parādīsies, kad indeksēšana būs pabeigta",
        emptyLibraryTitle = "Mūzika netika atrasta",
        emptyLibraryMessage = "Dziesmas un albumi parādīsies šeit, kad pievienosiet mūziku ierīces noklusējuma Music mapei",
        noRecentAdditionsTitle = "Vēl nav nesenu papildinājumu",
        noRecentAdditionsMessage = "Pievienojiet albumus ierīces Music mapei, un jaunākie šeit parādīsies automātiski",
        recentlyPlayedSongsTitle = "Nesen atskaņots",
        recentlyPlayedSongsEmpty = "Dziesmas drīz parādīsies šeit",
        favoriteAlbumsTitle = "Jūsu iecienītie albumi",
        favoriteAlbumsSubtitle = "Mūzika, pie kuras bieži atgriežaties",
        noFavoriteAlbumsTitle = "Vēl nav atvērts neviens albums",
        noFavoriteAlbumsMessage = "Atveriet vai atskaņojiet jebkuru albumu, un tas šeit parādīsies ar vāciņu priekšplānā",
    )
    AppLanguage.Lithuanian -> HomeUiCopy(
        indexingTitle = "Indeksuojama biblioteka",
        indexingMessage = "Dainos ir albumai čia pasirodys, kai indeksavimas bus baigtas",
        emptyLibraryTitle = "Muzikos nerasta",
        emptyLibraryMessage = "Dainos ir albumai čia pasirodys, kai pridėsite muziką į numatytąjį įrenginio Music aplanką",
        noRecentAdditionsTitle = "Neseniai pridėtų dar nėra",
        noRecentAdditionsMessage = "Pridėkite albumų į įrenginio Music aplanką, ir naujausi čia pasirodys automatiškai",
        recentlyPlayedSongsTitle = "Neseniai grota",
        recentlyPlayedSongsEmpty = "Dainos netrukus pasirodys čia",
        favoriteAlbumsTitle = "Jūsų mėgstami albumai",
        favoriteAlbumsSubtitle = "Muzika, prie kurios dažnai grįžtate",
        noFavoriteAlbumsTitle = "Dar neatidarytas nė vienas albumas",
        noFavoriteAlbumsMessage = "Atidarykite arba paleiskite bet kurį albumą, ir jis čia pasirodys su viršeliu priekyje",
    )
    AppLanguage.Macedonian -> HomeUiCopy(
        indexingTitle = "Библиотеката се индексира",
        indexingMessage = "Песните и албумите ќе се појават кога индексирањето ќе заврши",
        emptyLibraryTitle = "Не е пронајдена музика",
        emptyLibraryMessage = "Песните и албумите ќе се појават тука кога ќе додадете музика во стандардната папка Music на уредот",
        noRecentAdditionsTitle = "Сѐ уште нема неодамнешни додатоци",
        noRecentAdditionsMessage = "Додајте албуми во папката Music на уредот и најновите автоматски ќе се појават тука",
        recentlyPlayedSongsTitle = "Неодамна пуштено",
        recentlyPlayedSongsEmpty = "Песните наскоро ќе се појават тука",
        favoriteAlbumsTitle = "Вашите омилени албуми",
        favoriteAlbumsSubtitle = "Музика на која често ѝ се враќате",
        noFavoriteAlbumsTitle = "Сѐ уште не е отворен ниту еден албум",
        noFavoriteAlbumsMessage = "Отворете или пуштете кој било албум и ќе се појави тука со корицата во преден план",
    )
    AppLanguage.Norwegian -> HomeUiCopy(
        indexingTitle = "Biblioteket indekseres",
        indexingMessage = "Sanger og album vises når indekseringen er ferdig",
        emptyLibraryTitle = "Ingen musikk funnet",
        emptyLibraryMessage = "Sanger og album vises her når du legger til musikk i enhetens standardmappe Music",
        noRecentAdditionsTitle = "Ingen nylige tillegg ennå",
        noRecentAdditionsMessage = "Legg til album i enhetens Music-mappe, så vises de nyeste automatisk her",
        recentlyPlayedSongsTitle = "Nylig spilt",
        recentlyPlayedSongsEmpty = "Sanger vises her snart",
        favoriteAlbumsTitle = "Dine favorittalbum",
        favoriteAlbumsSubtitle = "Musikk du ofte vender tilbake til",
        noFavoriteAlbumsTitle = "Ingen album er åpnet ennå",
        noFavoriteAlbumsMessage = "Åpne eller spill av et album, så vises det her med omslaget i sentrum",
    )
    AppLanguage.Portuguese -> HomeUiCopy(
        indexingTitle = "A indexar biblioteca",
        indexingMessage = "As músicas e os álbuns aparecerão quando a indexação terminar",
        emptyLibraryTitle = "Nenhuma música encontrada",
        emptyLibraryMessage = "As músicas e os álbuns aparecerão aqui quando adicionar música à pasta Music predefinida do dispositivo",
        noRecentAdditionsTitle = "Ainda não há adições recentes",
        noRecentAdditionsMessage = "Adicione álbuns à pasta Music do dispositivo e os mais recentes aparecerão aqui automaticamente",
        recentlyPlayedSongsTitle = "Reproduzidas recentemente",
        recentlyPlayedSongsEmpty = "As músicas aparecerão aqui em breve",
        favoriteAlbumsTitle = "Os seus álbuns favoritos",
        favoriteAlbumsSubtitle = "Música à qual volta com frequência",
        noFavoriteAlbumsTitle = "Ainda não foi aberto nenhum álbum",
        noFavoriteAlbumsMessage = "Abra ou reproduza qualquer álbum e ele aparecerá aqui com a capa em destaque",
    )
    AppLanguage.Russian -> HomeUiCopy(
        indexingTitle = "Идёт индексирование библиотеки",
        indexingMessage = "Песни и альбомы появятся после завершения индексирования",
        emptyLibraryTitle = "Музыка не найдена",
        emptyLibraryMessage = "Песни и альбомы появятся здесь, когда вы добавите музыку в стандартную папку Music на устройстве",
        noRecentAdditionsTitle = "Пока нет недавних добавлений",
        noRecentAdditionsMessage = "Добавьте альбомы в папку Music на устройстве, и новейшие автоматически появятся здесь",
        recentlyPlayedSongsTitle = "Недавно воспроизведённое",
        recentlyPlayedSongsEmpty = "Песни скоро появятся здесь",
        favoriteAlbumsTitle = "Ваши любимые альбомы",
        favoriteAlbumsSubtitle = "Музыка, к которой вы часто возвращаетесь",
        noFavoriteAlbumsTitle = "Пока не был открыт ни один альбом",
        noFavoriteAlbumsMessage = "Откройте или включите любой альбом, и он появится здесь с обложкой на первом плане",
    )
    AppLanguage.Serbian -> HomeUiCopy(
        indexingTitle = "Библиотека се индексира",
        indexingMessage = "Песме и албуми ће се појавити када се индексирање заврши",
        emptyLibraryTitle = "Музика није пронађена",
        emptyLibraryMessage = "Песме и албуми ће се појавити овде када додате музику у подразумевани Music фолдер на уређају",
        noRecentAdditionsTitle = "Још нема недавних додавања",
        noRecentAdditionsMessage = "Додајте албуме у Music фолдер уређаја и најновији ће се овде појавити аутоматски",
        recentlyPlayedSongsTitle = "Недавно пуштано",
        recentlyPlayedSongsEmpty = "Песме ће се овде ускоро појавити",
        favoriteAlbumsTitle = "Ваши омиљени албуми",
        favoriteAlbumsSubtitle = "Музика којој се често враћате",
        noFavoriteAlbumsTitle = "Још није отворен ниједан албум",
        noFavoriteAlbumsMessage = "Отворите или пустите било који албум и појавиће се овде са омотом у првом плану",
    )
    AppLanguage.Spanish -> HomeUiCopy(
        indexingTitle = "Indexando la biblioteca",
        indexingMessage = "Las canciones y los álbumes aparecerán cuando termine la indexación",
        emptyLibraryTitle = "No se encontró música",
        emptyLibraryMessage = "Las canciones y los álbumes aparecerán aquí cuando añadas música a la carpeta Music predeterminada del dispositivo",
        noRecentAdditionsTitle = "Aún no hay añadidos recientes",
        noRecentAdditionsMessage = "Añade álbumes a la carpeta Music del dispositivo y los más recientes aparecerán aquí automáticamente",
        recentlyPlayedSongsTitle = "Reproducido recientemente",
        recentlyPlayedSongsEmpty = "Las canciones aparecerán aquí pronto",
        favoriteAlbumsTitle = "Tus álbumes favoritos",
        favoriteAlbumsSubtitle = "La música a la que vuelves con frecuencia",
        noFavoriteAlbumsTitle = "Aún no se ha abierto ningún álbum",
        noFavoriteAlbumsMessage = "Abre o reproduce cualquier álbum y aparecerá aquí con su portada en primer plano",
    )
    AppLanguage.Swedish -> HomeUiCopy(
        indexingTitle = "Biblioteket indexeras",
        indexingMessage = "Låtar och album visas när indexeringen är klar",
        emptyLibraryTitle = "Ingen musik hittades",
        emptyLibraryMessage = "Låtar och album visas här när du lägger till musik i enhetens standardmapp Music",
        noRecentAdditionsTitle = "Inga nyliga tillägg ännu",
        noRecentAdditionsMessage = "Lägg till album i enhetens Music-mapp så visas de senaste här automatiskt",
        recentlyPlayedSongsTitle = "Nyligen spelat",
        recentlyPlayedSongsEmpty = "Låtar visas här snart",
        favoriteAlbumsTitle = "Dina favoritalbum",
        favoriteAlbumsSubtitle = "Musik du ofta återvänder till",
        noFavoriteAlbumsTitle = "Inga album har öppnats ännu",
        noFavoriteAlbumsMessage = "Öppna eller spela ett album så visas det här med omslaget i fokus",
    )
    AppLanguage.Thai -> HomeUiCopy(
        indexingTitle = "กำลังจัดทำดัชนีคลังเพลง",
        indexingMessage = "เพลงและอัลบั้มจะปรากฏเมื่อการจัดทำดัชนีเสร็จสิ้น",
        emptyLibraryTitle = "ไม่พบเพลง",
        emptyLibraryMessage = "เพลงและอัลบั้มจะปรากฏที่นี่เมื่อคุณเพิ่มเพลงลงในโฟลเดอร์ Music เริ่มต้นของอุปกรณ์",
        noRecentAdditionsTitle = "ยังไม่มีสิ่งที่เพิ่มล่าสุด",
        noRecentAdditionsMessage = "เพิ่มอัลบั้มลงในโฟลเดอร์ Music ของอุปกรณ์ แล้วรายการล่าสุดจะปรากฏที่นี่โดยอัตโนมัติ",
        recentlyPlayedSongsTitle = "เล่นล่าสุด",
        recentlyPlayedSongsEmpty = "เพลงจะปรากฏที่นี่ในไม่ช้า",
        favoriteAlbumsTitle = "อัลบั้มโปรดของคุณ",
        favoriteAlbumsSubtitle = "เพลงที่คุณกลับมาฟังบ่อย ๆ",
        noFavoriteAlbumsTitle = "ยังไม่มีการเปิดอัลบั้ม",
        noFavoriteAlbumsMessage = "เปิดหรือเล่นอัลบั้มใดก็ได้ แล้วมันจะปรากฏที่นี่พร้อมปกอยู่ด้านหน้า",
    )
    AppLanguage.Ukrainian -> HomeUiCopy(
        indexingTitle = "Бібліотека індексується",
        indexingMessage = "Пісні та альбоми з’являться після завершення індексації",
        emptyLibraryTitle = "Музику не знайдено",
        emptyLibraryMessage = "Пісні та альбоми з’являться тут, коли ви додасте музику до стандартної папки Music на пристрої",
        noRecentAdditionsTitle = "Поки немає нещодавніх додавань",
        noRecentAdditionsMessage = "Додайте альбоми до папки Music на пристрої, і найновіші автоматично з’являться тут",
        recentlyPlayedSongsTitle = "Нещодавно відтворене",
        recentlyPlayedSongsEmpty = "Пісні скоро з’являться тут",
        favoriteAlbumsTitle = "Ваші улюблені альбоми",
        favoriteAlbumsSubtitle = "Музика, до якої ви часто повертаєтесь",
        noFavoriteAlbumsTitle = "Ще не було відкрито жодного альбому",
        noFavoriteAlbumsMessage = "Відкрийте або відтворіть будь-який альбом, і він з’явиться тут зі своєю обкладинкою в центрі уваги",
    )
    AppLanguage.Slovak -> HomeUiCopy(
        indexingTitle = "Prebieha indexovanie knižnice",
        indexingMessage = "Skladby a albumy sa zobrazia po dokončení indexovania",
        emptyLibraryTitle = "Nenašla sa žiadna hudba",
        emptyLibraryMessage = "Skladby a albumy sa tu zobrazia, keď pridáte hudbu do predvoleného priečinka Music v zariadení",
        noRecentAdditionsTitle = "Zatiaľ nič nedávno pridané",
        noRecentAdditionsMessage = "Pridajte albumy do priečinka Music v zariadení a najnovšie sa tu zobrazia automaticky",
        recentlyPlayedSongsTitle = "Nedávno prehrávané",
        recentlyPlayedSongsEmpty = "Skladby sa tu čoskoro zobrazia",
        favoriteAlbumsTitle = "Vaše obľúbené albumy",
        favoriteAlbumsSubtitle = "Hudba, ku ktorej sa často vraciate",
        noFavoriteAlbumsTitle = "Zatiaľ nebol otvorený žiadny album",
        noFavoriteAlbumsMessage = "Otvorte alebo prehrajte ľubovoľný album a zobrazí sa tu s obalom v popredí",
    )
    AppLanguage.Korean -> HomeUiCopy(
        indexingTitle = "라이브러리를 인덱싱하는 중입니다",
        indexingMessage = "인덱싱이 끝나면 곡과 앨범이 여기에 표시됩니다",
        emptyLibraryTitle = "음악을 찾을 수 없습니다",
        emptyLibraryMessage = "기기의 기본 Music 폴더에 음악을 추가하면 곡과 앨범이 여기에 표시됩니다",
        noRecentAdditionsTitle = "최근 추가된 항목이 아직 없습니다",
        noRecentAdditionsMessage = "기기의 Music 폴더에 앨범을 추가하면 최신 항목이 여기에 자동으로 표시됩니다",
        recentlyPlayedSongsTitle = "최근 재생",
        recentlyPlayedSongsEmpty = "곡이 곧 여기에 표시됩니다",
        favoriteAlbumsTitle = "자주 듣는 앨범",
        favoriteAlbumsSubtitle = "자주 다시 찾게 되는 음악",
        noFavoriteAlbumsTitle = "아직 연 앨범이 없습니다",
        noFavoriteAlbumsMessage = "아무 앨범이나 열거나 재생하면 커버와 함께 여기에 표시됩니다",
    )
    AppLanguage.Malay -> HomeUiCopy(
        indexingTitle = "Pustaka sedang diindeks",
        indexingMessage = "Lagu dan album akan muncul apabila pengindeksan selesai",
        emptyLibraryTitle = "Tiada muzik ditemui",
        emptyLibraryMessage = "Lagu dan album akan muncul di sini apabila anda menambah muzik ke folder Music lalai pada peranti",
        noRecentAdditionsTitle = "Belum ada penambahan terkini",
        noRecentAdditionsMessage = "Tambah album ke folder Music pada peranti dan yang terbaharu akan muncul di sini secara automatik",
        recentlyPlayedSongsTitle = "Baru dimainkan",
        recentlyPlayedSongsEmpty = "Lagu akan muncul di sini tidak lama lagi",
        favoriteAlbumsTitle = "Album kegemaran anda",
        favoriteAlbumsSubtitle = "Muzik yang anda kerap kembali dengar",
        noFavoriteAlbumsTitle = "Belum ada album yang dibuka",
        noFavoriteAlbumsMessage = "Buka atau mainkan mana-mana album dan ia akan muncul di sini dengan kulit hadapan di depan",
    )
    AppLanguage.Bengali -> HomeUiCopy(
        indexingTitle = "লাইব্রেরি ইনডেক্স করা হচ্ছে",
        indexingMessage = "ইনডেক্সিং শেষ হলে গান ও অ্যালবাম এখানে দেখা যাবে",
        emptyLibraryTitle = "কোনো সঙ্গীত পাওয়া যায়নি",
        emptyLibraryMessage = "আপনি ডিভাইসের ডিফল্ট Music ফোল্ডারে সঙ্গীত যোগ করলে গান ও অ্যালবাম এখানে দেখা যাবে",
        noRecentAdditionsTitle = "এখনও সাম্প্রতিক কিছু যোগ হয়নি",
        noRecentAdditionsMessage = "ডিভাইসের Music ফোল্ডারে অ্যালবাম যোগ করুন, নতুনগুলো এখানে স্বয়ংক্রিয়ভাবে দেখাবে",
        recentlyPlayedSongsTitle = "সম্প্রতি শোনা",
        recentlyPlayedSongsEmpty = "গানগুলো শিগগিরই এখানে দেখা যাবে",
        favoriteAlbumsTitle = "আপনার প্রিয় অ্যালবাম",
        favoriteAlbumsSubtitle = "যে সঙ্গীতে আপনি বারবার ফিরে আসেন",
        noFavoriteAlbumsTitle = "এখনও কোনো অ্যালবাম খোলা হয়নি",
        noFavoriteAlbumsMessage = "যেকোনো অ্যালবাম খুলুন বা চালান, সেটি এখানে কভারের সাথে দেখাবে",
    )
    AppLanguage.Urdu -> HomeUiCopy(
        indexingTitle = "لائبریری کی فہرست بنائی جا رہی ہے",
        indexingMessage = "فہرست سازی مکمل ہونے پر گانے اور البمز یہاں دکھائی دیں گے",
        emptyLibraryTitle = "کوئی موسیقی نہیں ملی",
        emptyLibraryMessage = "جب آپ آلے کے طے شدہ Music فولڈر میں موسیقی شامل کریں گے تو گانے اور البمز یہاں دکھائی دیں گے",
        noRecentAdditionsTitle = "ابھی تک حالیہ اضافہ نہیں ہوا",
        noRecentAdditionsMessage = "آلے کے Music فولڈر میں البمز شامل کریں، تازہ ترین خود بخود یہاں ظاہر ہوں گے",
        recentlyPlayedSongsTitle = "حال ہی میں چلائے گئے",
        recentlyPlayedSongsEmpty = "گانے جلد یہاں دکھائی دیں گے",
        favoriteAlbumsTitle = "آپ کے پسندیدہ البمز",
        favoriteAlbumsSubtitle = "وہ موسیقی جس کی طرف آپ بار بار لوٹتے ہیں",
        noFavoriteAlbumsTitle = "ابھی تک کوئی البم نہیں کھولا گیا",
        noFavoriteAlbumsMessage = "کوئی بھی البم کھولیں یا چلائیں، وہ یہاں اپنے کور کے ساتھ نمایاں ہو جائے گا",
    )
    AppLanguage.Albanian -> HomeUiCopy(
        indexingTitle = "Biblioteka po indeksohet",
        indexingMessage = "Këngët dhe albumet do të shfaqen pasi të përfundojë indeksimi",
        emptyLibraryTitle = "Nuk u gjet muzikë",
        emptyLibraryMessage = "Këngët dhe albumet do të shfaqen këtu pasi të shtoni muzikë në dosjen e parazgjedhur Music të pajisjes",
        noRecentAdditionsTitle = "Ende s’ka shtesa të fundit",
        noRecentAdditionsMessage = "Shtoni albume në dosjen Music të pajisjes dhe më të rejat do të shfaqen këtu automatikisht",
        recentlyPlayedSongsTitle = "Luajtur së fundi",
        recentlyPlayedSongsEmpty = "Këngët do të shfaqen këtu së shpejti",
        favoriteAlbumsTitle = "Albumet tuaja të preferuara",
        favoriteAlbumsSubtitle = "Muzikë tek e cila ktheheni shpesh",
        noFavoriteAlbumsTitle = "Ende nuk është hapur asnjë album",
        noFavoriteAlbumsMessage = "Hapni ose luani cilindo album dhe ai do të shfaqet këtu me kopertinën në qendër",
    )
    AppLanguage.English -> HomeUiCopy(
        indexingTitle = "Indexing library",
        indexingMessage = "Songs and albums will show when indexing is done",
        emptyLibraryTitle = "No music was found",
        emptyLibraryMessage = "Songs and albums will show here as you add music to your device's default Music folder",
        noRecentAdditionsTitle = "No recent additions yet",
        noRecentAdditionsMessage = "Add albums to the device Music folder and the newest ones will appear here automatically",
        recentlyPlayedSongsTitle = "Recently played",
        recentlyPlayedSongsEmpty = "Songs will show up here soon",
        favoriteAlbumsTitle = "Your favorite albums",
        favoriteAlbumsSubtitle = "Music you come back to frequently",
        noFavoriteAlbumsTitle = "No albums have been opened yet",
        noFavoriteAlbumsMessage = "Open or play any album and it will appear here with its artwork front and center",
    )
}

internal fun formatCountLabel(
    count: Int,
    singular: String,
    language: AppLanguage,
): String {
    return localizedCountLabel(count, singular, language)
}

internal fun localizedCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String {
    if (noun == "playlist") {
        return localizedPlaylistCountLabel(count, language)
    }
    localizedComplexCountLabel(count, noun, language)?.let { return it }
    val (singular, plural) = when (language) {
        AppLanguage.Albanian -> when (noun) {
            "song" -> "këngë" to "këngë"
            "track" -> "këngë" to "këngë"
            "album" -> "album" to "albume"
            "artist" -> "artist" to "artistë"
            "genre" -> "zhanër" to "zhanre"
            else -> noun to "${noun}e"
        }
        AppLanguage.ChineseSimplified -> noun to noun
        AppLanguage.Croatian -> when (noun) {
            "song" -> "pjesma" to "pjesme"
            "track" -> "pjesma" to "pjesme"
            "album" -> "album" to "albuma"
            "artist" -> "izvođač" to "izvođača"
            "genre" -> "žanr" to "žanra"
            else -> noun to "${noun}a"
        }
        AppLanguage.Czech -> when (noun) {
            "song" -> "skladba" to "skladby"
            "track" -> "skladba" to "skladby"
            "album" -> "album" to "alba"
            "artist" -> "umělec" to "umělci"
            "genre" -> "žánr" to "žánry"
            else -> noun to "${noun}y"
        }
        AppLanguage.Danish -> when (noun) {
            "song" -> "sang" to "sange"
            "track" -> "nummer" to "numre"
            "album" -> "album" to "albummer"
            "artist" -> "kunstner" to "kunstnere"
            "genre" -> "genre" to "genrer"
            else -> noun to "${noun}er"
        }
        AppLanguage.Dutch -> when (noun) {
            "song" -> "nummer" to "nummers"
            "track" -> "track" to "tracks"
            "album" -> "album" to "albums"
            "artist" -> "artiest" to "artiesten"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.Estonian -> when (noun) {
            "song" -> "lugu" to "lugu"
            "track" -> "lugu" to "lugu"
            "album" -> "album" to "albumit"
            "artist" -> "artist" to "artisti"
            "genre" -> "žanr" to "žanri"
            else -> noun to noun
        }
        AppLanguage.French -> when (noun) {
            "song" -> "morceau" to "morceaux"
            "track" -> "piste" to "pistes"
            "album" -> "album" to "albums"
            "artist" -> "artiste" to "artistes"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.German -> when (noun) {
            "song" -> "Titel" to "Titel"
            "track" -> "Track" to "Tracks"
            "album" -> "Album" to "Alben"
            "artist" -> "Künstler" to "Künstler"
            "genre" -> "Genre" to "Genres"
            else -> noun to "${noun}e"
        }
        AppLanguage.Greek -> when (noun) {
            "song" -> "τραγούδι" to "τραγούδια"
            "track" -> "κομμάτι" to "κομμάτια"
            "album" -> "άλμπουμ" to "άλμπουμ"
            "artist" -> "καλλιτέχνης" to "καλλιτέχνες"
            "genre" -> "είδος" to "είδη"
            else -> noun to noun
        }
        AppLanguage.Hindi -> when (noun) {
            "song" -> "गाना" to "गाने"
            "track" -> "ट्रैक" to "ट्रैक"
            "album" -> "एल्बम" to "एल्बम"
            "artist" -> "कलाकार" to "कलाकार"
            "genre" -> "शैली" to "शैलियाँ"
            else -> noun to noun
        }
        AppLanguage.Hungarian -> when (noun) {
            "song" -> "dal" to "dal"
            "track" -> "szám" to "szám"
            "album" -> "album" to "album"
            "artist" -> "előadó" to "előadó"
            "genre" -> "műfaj" to "műfaj"
            else -> noun to noun
        }
        AppLanguage.Italian -> when (noun) {
            "song" -> "brano" to "brani"
            "track" -> "traccia" to "tracce"
            "album" -> "album" to "album"
            "artist" -> "artista" to "artisti"
            "genre" -> "genere" to "generi"
            else -> noun to "${noun}i"
        }
        AppLanguage.Japanese -> noun to noun
        AppLanguage.Latin -> when (noun) {
            "song" -> "cantus" to "cantus"
            "track" -> "cantus" to "cantus"
            "album" -> "album" to "albuma"
            "artist" -> "artifex" to "artifices"
            "genre" -> "genus" to "genera"
            else -> noun to noun
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> "dziesma" to "dziesmas"
            "track" -> "ieraksts" to "ieraksti"
            "album" -> "albums" to "albumi"
            "artist" -> "mākslinieks" to "mākslinieki"
            "genre" -> "žanrs" to "žanri"
            else -> noun to "${noun}i"
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> "daina" to "dainos"
            "track" -> "takelis" to "takeliai"
            "album" -> "albumas" to "albumai"
            "artist" -> "atlikėjas" to "atlikėjai"
            "genre" -> "žanras" to "žanrai"
            else -> noun to "${noun}ai"
        }
        AppLanguage.Macedonian -> when (noun) {
            "song" -> "песна" to "песни"
            "track" -> "нумера" to "нумери"
            "album" -> "албум" to "албуми"
            "artist" -> "артист" to "артисти"
            "genre" -> "жанр" to "жанрови"
            else -> noun to noun
        }
        AppLanguage.Norwegian -> when (noun) {
            "song" -> "sang" to "sanger"
            "track" -> "spor" to "spor"
            "album" -> "album" to "album"
            "artist" -> "artist" to "artister"
            "genre" -> "sjanger" to "sjangre"
            else -> noun to noun
        }
        AppLanguage.Slovak -> when (noun) {
            "song" -> "skladba" to "skladby"
            "track" -> "skladba" to "skladby"
            "album" -> "album" to "albumy"
            "artist" -> "interpret" to "interpreti"
            "genre" -> "žáner" to "žánre"
            else -> noun to "${noun}y"
        }
        AppLanguage.Polish -> when (noun) {
            "song" -> "utwór" to "utwory"
            "track" -> "utwór" to "utwory"
            "album" -> "album" to "albumy"
            "artist" -> "artysta" to "artyści"
            "genre" -> "gatunek" to "gatunki"
            else -> noun to "${noun}y"
        }
        AppLanguage.Portuguese -> when (noun) {
            "song" -> "música" to "músicas"
            "track" -> "faixa" to "faixas"
            "album" -> "álbum" to "álbuns"
            "artist" -> "artista" to "artistas"
            "genre" -> "género" to "géneros"
            else -> noun to "${noun}s"
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> "песня" to "песни"
            "track" -> "трек" to "треки"
            "album" -> "альбом" to "альбомы"
            "artist" -> "исполнитель" to "исполнители"
            "genre" -> "жанр" to "жанры"
            else -> noun to noun
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> "песма" to "песме"
            "track" -> "нумера" to "нумере"
            "album" -> "албум" to "албуми"
            "artist" -> "извођач" to "извођачи"
            "genre" -> "жанр" to "жанрови"
            else -> noun to noun
        }
        AppLanguage.Spanish -> when (noun) {
            "song" -> "canción" to "canciones"
            "track" -> "pista" to "pistas"
            "album" -> "álbum" to "álbumes"
            "artist" -> "artista" to "artistas"
            "genre" -> "género" to "géneros"
            else -> noun to "${noun}s"
        }
        AppLanguage.Swedish -> when (noun) {
            "song" -> "låt" to "låtar"
            "track" -> "spår" to "spår"
            "album" -> "album" to "album"
            "artist" -> "artist" to "artister"
            "genre" -> "genre" to "genrer"
            else -> noun to noun
        }
        AppLanguage.Korean -> noun to noun
        AppLanguage.Malay -> when (noun) {
            "song" -> "lagu" to "lagu"
            "track" -> "runut" to "runut"
            "album" -> "album" to "album"
            "artist" -> "artis" to "artis"
            "genre" -> "genre" to "genre"
            else -> noun to noun
        }
        AppLanguage.Thai -> when (noun) {
            "song" -> "เพลง" to "เพลง"
            "track" -> "แทร็ก" to "แทร็ก"
            "album" -> "อัลบั้ม" to "อัลบั้ม"
            "artist" -> "ศิลปิน" to "ศิลปิน"
            "genre" -> "แนวเพลง" to "แนวเพลง"
            else -> noun to noun
        }
        AppLanguage.Bengali -> when (noun) {
            "song" -> "গান" to "গান"
            "track" -> "ট্র্যাক" to "ট্র্যাক"
            "album" -> "অ্যালবাম" to "অ্যালবাম"
            "artist" -> "শিল্পী" to "শিল্পী"
            "genre" -> "ধরন" to "ধরন"
            else -> noun to noun
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> "пісня" to "пісні"
            "track" -> "трек" to "треки"
            "album" -> "альбом" to "альбоми"
            "artist" -> "виконавець" to "виконавці"
            "genre" -> "жанр" to "жанри"
            else -> noun to noun
        }
        AppLanguage.Urdu -> when (noun) {
            "song" -> "گانا" to "گانے"
            "track" -> "ٹریک" to "ٹریک"
            "album" -> "البم" to "البمز"
            "artist" -> "آرٹسٹ" to "آرٹسٹس"
            "genre" -> "صنف" to "اصناف"
            else -> noun to noun
        }
        AppLanguage.English -> when (noun) {
            "song" -> "song" to "songs"
            "track" -> "track" to "tracks"
            "album" -> "album" to "albums"
            "artist" -> "artist" to "artists"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
    }
    val label = if (count == 1) singular else plural
    return "$count $label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedPlaylistCountLabel(count: Int, language: AppLanguage): String {
    val forms = when (language) {
        AppLanguage.Albanian -> Triple("listë dëgjimi", "lista dëgjimi", "lista dëgjimi")
        AppLanguage.ChineseSimplified -> Triple("播放列表", "播放列表", "播放列表")
        AppLanguage.Croatian -> Triple("playlista", "playliste", "playlista")
        AppLanguage.Czech -> Triple("playlist", "playlisty", "playlistů")
        AppLanguage.Danish -> Triple("afspilningsliste", "afspilningslister", "afspilningslister")
        AppLanguage.Dutch -> Triple("afspeellijst", "afspeellijsten", "afspeellijsten")
        AppLanguage.Bengali -> Triple("প্লেলিস্ট", "প্লেলিস্ট", "প্লেলিস্ট")
        AppLanguage.Estonian -> Triple("esitusloend", "esitusloendit", "esitusloendit")
        AppLanguage.French -> Triple("playlist", "playlists", "playlists")
        AppLanguage.German -> Triple("Playlist", "Playlists", "Playlists")
        AppLanguage.Greek -> Triple("λίστα αναπαραγωγής", "λίστες αναπαραγωγής", "λίστες αναπαραγωγής")
        AppLanguage.Hindi -> Triple("प्लेलिस्ट", "प्लेलिस्ट", "प्लेलिस्ट")
        AppLanguage.Hungarian -> Triple("lejátszási lista", "lejátszási listák", "lejátszási listák")
        AppLanguage.Italian -> Triple("playlist", "playlist", "playlist")
        AppLanguage.Japanese -> Triple("プレイリスト", "プレイリスト", "プレイリスト")
        AppLanguage.Korean -> Triple("재생목록", "재생목록", "재생목록")
        AppLanguage.Latin -> Triple("index cantuum", "indices cantuum", "indices cantuum")
        AppLanguage.Latvian -> Triple("atskaņošanas saraksts", "atskaņošanas saraksti", "atskaņošanas sarakstu")
        AppLanguage.Lithuanian -> Triple("grojaraštis", "grojaraščiai", "grojaraščių")
        AppLanguage.Malay -> Triple("senarai main", "senarai main", "senarai main")
        AppLanguage.Macedonian -> Triple("плејлиста", "плејлисти", "плејлисти")
        AppLanguage.Norwegian -> Triple("spilleliste", "spillelister", "spillelister")
        AppLanguage.Polish -> Triple("playlista", "playlisty", "playlistów")
        AppLanguage.Portuguese -> Triple("playlist", "playlists", "playlists")
        AppLanguage.Russian -> Triple("плейлист", "плейлиста", "плейлистов")
        AppLanguage.Slovak -> Triple("zoznam skladieb", "zoznamy skladieb", "zoznamov skladieb")
        AppLanguage.Serbian -> Triple("плејлиста", "плејлисте", "плејлиста")
        AppLanguage.Spanish -> Triple("lista de reproducción", "listas de reproducción", "listas de reproducción")
        AppLanguage.Swedish -> Triple("spellista", "spellistor", "spellistor")
        AppLanguage.Thai -> Triple("เพลย์ลิสต์", "เพลย์ลิสต์", "เพลย์ลิสต์")
        AppLanguage.Ukrainian -> Triple("плейлист", "плейлисти", "плейлистів")
        AppLanguage.Urdu -> Triple("پلے لسٹ", "پلے لسٹیں", "پلے لسٹیں")
        AppLanguage.English -> Triple("playlist", "playlists", "playlists")
    }
    val number = count.coerceAtLeast(0)
    val lastTwo = number % 100
    val last = number % 10
    val label = when {
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Lithuanian && (last == 0 || lastTwo in 11..19) -> forms.third
        language in setOf(
            AppLanguage.Croatian,
            AppLanguage.Czech,
            AppLanguage.Polish,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Slovak,
            AppLanguage.Ukrainian,
        ) && last == 1 && lastTwo != 11 -> forms.first
        language in setOf(
            AppLanguage.Croatian,
            AppLanguage.Czech,
            AppLanguage.Polish,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Slovak,
            AppLanguage.Ukrainian,
        ) && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language in setOf(
            AppLanguage.Croatian,
            AppLanguage.Czech,
            AppLanguage.Polish,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Slovak,
            AppLanguage.Ukrainian,
        ) -> forms.third
        language == AppLanguage.Lithuanian && last == 1 && lastTwo != 11 -> forms.first
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        last == 1 && lastTwo != 11 -> forms.first
        else -> forms.second
    }
    return "$count $label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedComplexCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String? {
    val forms = when (language) {
        AppLanguage.Croatian -> when (noun) {
            "song", "track" -> Triple("pjesma", "pjesme", "pjesama")
            "album" -> Triple("album", "albuma", "albuma")
            "artist" -> Triple("izvođač", "izvođača", "izvođača")
            "genre" -> Triple("žanr", "žanra", "žanrova")
            else -> return null
        }
        AppLanguage.Czech -> when (noun) {
            "song", "track" -> Triple("skladba", "skladby", "skladeb")
            "album" -> Triple("album", "alba", "albumů")
            "artist" -> Triple("umělec", "umělci", "umělců")
            "genre" -> Triple("žánr", "žánry", "žánrů")
            else -> return null
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> Triple("dziesma", "dziesmas", "dziesmu")
            "track" -> Triple("ieraksts", "ieraksti", "ierakstu")
            "album" -> Triple("albums", "albumi", "albumu")
            "artist" -> Triple("mākslinieks", "mākslinieki", "mākslinieku")
            "genre" -> Triple("žanrs", "žanri", "žanru")
            else -> return null
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> Triple("daina", "dainos", "dainų")
            "track" -> Triple("takelis", "takeliai", "takelių")
            "album" -> Triple("albumas", "albumai", "albumų")
            "artist" -> Triple("atlikėjas", "atlikėjai", "atlikėjų")
            "genre" -> Triple("žanras", "žanrai", "žanrų")
            else -> return null
        }
        AppLanguage.Polish -> when (noun) {
            "song", "track" -> Triple("utwór", "utwory", "utworów")
            "album" -> Triple("album", "albumy", "albumów")
            "artist" -> Triple("artysta", "artyści", "artystów")
            "genre" -> Triple("gatunek", "gatunki", "gatunków")
            else -> return null
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> Triple("песня", "песни", "песен")
            "track" -> Triple("трек", "трека", "треков")
            "album" -> Triple("альбом", "альбома", "альбомов")
            "artist" -> Triple("исполнитель", "исполнителя", "исполнителей")
            "genre" -> Triple("жанр", "жанра", "жанров")
            else -> return null
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> Triple("песма", "песме", "песама")
            "track" -> Triple("нумера", "нумере", "нумера")
            "album" -> Triple("албум", "албуми", "албума")
            "artist" -> Triple("извођач", "извођачи", "извођача")
            "genre" -> Triple("жанр", "жанрови", "жанрова")
            else -> return null
        }
        AppLanguage.Slovak -> when (noun) {
            "song", "track" -> Triple("skladba", "skladby", "skladieb")
            "album" -> Triple("album", "albumy", "albumov")
            "artist" -> Triple("interpret", "interpreti", "interpretov")
            "genre" -> Triple("žáner", "žánre", "žánrov")
            else -> return null
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> Triple("пісня", "пісні", "пісень")
            "track" -> Triple("трек", "треки", "треків")
            "album" -> Triple("альбом", "альбоми", "альбомів")
            "artist" -> Triple("виконавець", "виконавці", "виконавців")
            "genre" -> Triple("жанр", "жанри", "жанрів")
            else -> return null
        }
        else -> return null
    }
    val number = count.coerceAtLeast(0)
    val lastTwo = number % 100
    val last = number % 10
    val label = when {
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Lithuanian && lastTwo in 11..19 -> forms.third
        last == 1 && lastTwo != 11 -> forms.first
        last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        else -> forms.third
    }
    return "$count $label"
}

