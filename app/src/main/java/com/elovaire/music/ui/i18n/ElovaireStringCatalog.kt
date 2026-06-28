package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
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
    val updateAvailable: String,
    val appUpToDate: String,
    val installing: String,
    val download: String,
    val ok: String,
    val albumNotFound: String,
    val playlistNotFound: String,
    val mostPlayedSongs: String,
    val availableReleasesSuffix: String,
    val playAlbum: String,
    val shuffleAlbum: String,
    val editTags: String,
)

internal fun rootUiCopy(language: AppLanguage): RootUiCopy = when (language) {
    AppLanguage.Polish -> RootUiCopy(
        firstLaunchPermissionTitle = "Muzyka offline wymaga dostępu do Twojej biblioteki",
        firstLaunchPermissionMessage = "Elovaire skanuje folder Muzyka na urządzeniu, aby znaleźć lokalne albumy, okładki i kolejki utworów",
        firstLaunchPermissionButton = "Zezwól na dostęp do biblioteki audio",
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
        updateAvailable = "Dostępna aktualizacja",
        appUpToDate = "Elovaire jest aktualny",
        installing = "Instalowanie",
        download = "Pobierz",
        ok = "OK",
        albumNotFound = "Nie znaleziono albumu.",
        playlistNotFound = "Nie znaleziono playlisty.",
        mostPlayedSongs = "Najczęściej odtwarzane utwory",
        availableReleasesSuffix = "dostępne wydania",
        playAlbum = "Odtwórz album",
        shuffleAlbum = "Tasuj album",
        editTags = "Edytuj tagi",
    )
    AppLanguage.Slovak -> RootUiCopy(
        firstLaunchPermissionTitle = "Offline hudba si zaslúži prístup k vašej knižnici",
        firstLaunchPermissionMessage = "Elovaire prehľadáva priečinok Music v zariadení a hľadá lokálne albumy, obaly a fronty skladieb",
        firstLaunchPermissionButton = "Povoliť prístup k zvukovej knižnici",
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
        updateAvailable = "Dostupná aktualizácia",
        appUpToDate = "Elovaire je aktuálny",
        installing = "Inštaluje sa",
        download = "Stiahnuť",
        ok = "OK",
        albumNotFound = "Album sa nenašiel.",
        playlistNotFound = "Playlist sa nenašiel.",
        mostPlayedSongs = "Najčastejšie prehrávané skladby",
        availableReleasesSuffix = "dostupné vydania",
        playAlbum = "Prehrať album",
        shuffleAlbum = "Zamiešať album",
        editTags = "Upraviť tagy",
    )
    AppLanguage.Korean -> RootUiCopy(
        firstLaunchPermissionTitle = "오프라인 음악에는 라이브러리 접근 권한이 필요합니다",
        firstLaunchPermissionMessage = "Elovaire가 기기의 Music 폴더를 스캔하여 로컬 앨범, 아트워크, 트랙 대기열을 찾습니다",
        firstLaunchPermissionButton = "오디오 라이브러리 접근 허용",
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
        updateAvailable = "업데이트 사용 가능",
        appUpToDate = "Elovaire가 최신 상태입니다",
        installing = "설치 중",
        download = "다운로드",
        ok = "확인",
        albumNotFound = "앨범을 찾을 수 없습니다.",
        playlistNotFound = "플레이리스트를 찾을 수 없습니다.",
        mostPlayedSongs = "가장 많이 재생한 곡",
        availableReleasesSuffix = "개의 발매반",
        playAlbum = "앨범 재생",
        shuffleAlbum = "앨범 셔플",
        editTags = "태그 편집",
    )
    AppLanguage.Malay -> RootUiCopy(
        firstLaunchPermissionTitle = "Audio luar talian memerlukan akses ke pustaka anda",
        firstLaunchPermissionMessage = "Elovaire mengimbas folder Music pada peranti untuk album tempatan, karya seni dan barisan trek",
        firstLaunchPermissionButton = "Benarkan akses pustaka audio",
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
        updateAvailable = "Kemas kini tersedia",
        appUpToDate = "Elovaire sudah terkini",
        installing = "Memasang",
        download = "Muat turun",
        ok = "OK",
        albumNotFound = "Album tidak ditemui.",
        playlistNotFound = "Senarai main tidak ditemui.",
        mostPlayedSongs = "Lagu paling kerap dimainkan",
        availableReleasesSuffix = "keluaran tersedia",
        playAlbum = "Mainkan album",
        shuffleAlbum = "Kocok album",
        editTags = "Edit tag",
    )
    AppLanguage.Bengali -> RootUiCopy(
        firstLaunchPermissionTitle = "অফলাইন অডিওর জন্য আপনার লাইব্রেরিতে প্রবেশাধিকার দরকার",
        firstLaunchPermissionMessage = "Elovaire আপনার ডিভাইসের Music ফোল্ডার স্ক্যান করে স্থানীয় অ্যালবাম, কভার আর্ট ও ট্র্যাক কিউ খুঁজে নেয়",
        firstLaunchPermissionButton = "অডিও লাইব্রেরি অ্যাক্সেস দিন",
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
        updateAvailable = "আপডেট উপলভ্য",
        appUpToDate = "Elovaire সর্বশেষ সংস্করণে আছে",
        installing = "ইনস্টল হচ্ছে",
        download = "ডাউনলোড",
        ok = "ঠিক আছে",
        albumNotFound = "অ্যালবাম পাওয়া যায়নি।",
        playlistNotFound = "প্লেলিস্ট পাওয়া যায়নি।",
        mostPlayedSongs = "সবচেয়ে বেশি শোনা গান",
        availableReleasesSuffix = "টি উপলভ্য সংস্করণ",
        playAlbum = "অ্যালবাম চালান",
        shuffleAlbum = "অ্যালবাম শাফল করুন",
        editTags = "ট্যাগ সম্পাদনা",
    )
    AppLanguage.Urdu -> RootUiCopy(
        firstLaunchPermissionTitle = "آف لائن آڈیو کو آپ کی لائبریری تک رسائی درکار ہے",
        firstLaunchPermissionMessage = "Elovaire آپ کے آلے کے Music فولڈر کو اسکین کرتا ہے تاکہ مقامی البمز، آرٹ ورک اور ٹریک قطاریں تلاش کی جا سکیں",
        firstLaunchPermissionButton = "آڈیو لائبریری تک رسائی دیں",
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
        updateAvailable = "اپ ڈیٹ دستیاب ہے",
        appUpToDate = "Elovaire تازہ ترین ہے",
        installing = "انسٹال ہو رہا ہے",
        download = "ڈاؤن لوڈ",
        ok = "ٹھیک ہے",
        albumNotFound = "البم نہیں ملا۔",
        playlistNotFound = "پلے لسٹ نہیں ملی۔",
        mostPlayedSongs = "سب سے زیادہ چلنے والے گانے",
        availableReleasesSuffix = "دستیاب ریلیزز",
        playAlbum = "البم چلائیں",
        shuffleAlbum = "البم شفل کریں",
        editTags = "ٹیگز میں ترمیم کریں",
    )
    else -> RootUiCopy(
        firstLaunchPermissionTitle = "Offline audio deserves access to your library",
        firstLaunchPermissionMessage = "Elovaire scans the device Music folder for local albums, artwork, and track queues",
        firstLaunchPermissionButton = "Allow audio library access",
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
        updateAvailable = "Update available",
        appUpToDate = "Elovaire is up to date",
        installing = "Installing",
        download = "Download",
        ok = "OK",
        albumNotFound = "Album not found.",
        playlistNotFound = "Playlist not found.",
        mostPlayedSongs = "Most played songs",
        availableReleasesSuffix = "available releases",
        playAlbum = "Play album",
        shuffleAlbum = "Shuffle album",
        editTags = "Edit tags",
    )
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
        recentlyPlayedSongsTitle = "Ostatnio odtwarzane utwory",
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
        recentlyPlayedSongsTitle = "最近播放的歌曲",
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
        recentlyPlayedSongsTitle = "Nedavno reproducirane pjesme",
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
        recentlyPlayedSongsTitle = "Nedávno přehrávané skladby",
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
        recentlyPlayedSongsTitle = "Nyligt afspillede sange",
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
        recentlyPlayedSongsTitle = "Recent afgespeelde nummers",
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
        recentlyPlayedSongsTitle = "Hiljuti esitatud lood",
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
        recentlyPlayedSongsTitle = "Morceaux récemment lus",
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
        recentlyPlayedSongsTitle = "Zuletzt gespielte Songs",
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
        recentlyPlayedSongsTitle = "Τραγούδια που παίχτηκαν πρόσφατα",
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
        recentlyPlayedSongsTitle = "हाल ही में चलाए गए गाने",
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
        recentlyPlayedSongsTitle = "Nemrég lejátszott dalok",
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
        recentlyPlayedSongsTitle = "Brani ascoltati di recente",
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
        recentlyPlayedSongsTitle = "最近再生した曲",
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
        recentlyPlayedSongsTitle = "Cantus nuper acti",
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
        recentlyPlayedSongsTitle = "Nesen atskaņotās dziesmas",
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
        recentlyPlayedSongsTitle = "Neseniai grotos dainos",
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
        recentlyPlayedSongsTitle = "Неодамна пуштени песни",
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
        recentlyPlayedSongsTitle = "Nylig spilte sanger",
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
        recentlyPlayedSongsTitle = "Músicas reproduzidas recentemente",
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
        recentlyPlayedSongsTitle = "Недавно воспроизведённые песни",
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
        recentlyPlayedSongsTitle = "Недавно пуштане песме",
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
        recentlyPlayedSongsTitle = "Canciones reproducidas recientemente",
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
        recentlyPlayedSongsTitle = "Nyligen spelade låtar",
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
        recentlyPlayedSongsTitle = "เพลงที่เล่นล่าสุด",
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
        recentlyPlayedSongsTitle = "Нещодавно відтворені пісні",
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
        recentlyPlayedSongsTitle = "Nedávno prehrávané skladby",
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
        recentlyPlayedSongsTitle = "최근 재생한 곡",
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
        recentlyPlayedSongsTitle = "Lagu yang baru dimainkan",
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
        recentlyPlayedSongsTitle = "সম্প্রতি শোনা গান",
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
        recentlyPlayedSongsTitle = "حال ہی میں چلائے گئے گانے",
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
        recentlyPlayedSongsTitle = "Këngë të luajtura së fundi",
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
        recentlyPlayedSongsTitle = "Recently played songs",
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
): String {
    return if (count == 1) {
        "1 $singular"
    } else {
        "$count ${singular}s"
    }
}

internal fun localizedCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String {
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

internal enum class MiscPhrase {
    RecentlyAdded,
    WhatsNew,
    NoSongsYet,
    AddSongsViaEdit,
    NothingInHere,
    TapPlusToCreatePlaylist,
    Selected,
    ChooseSongs,
    AddSongs,
}

internal fun miscPhrase(language: AppLanguage, phrase: MiscPhrase): String = when (language) {
    AppLanguage.Polish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Ostatnio dodane"
        MiscPhrase.WhatsNew -> "Co nowego?"
        MiscPhrase.NoSongsYet -> "Nie ma jeszcze utworów"
        MiscPhrase.AddSongsViaEdit -> "Dodaj tu utwory, stukając przycisk edycji"
        MiscPhrase.NothingInHere -> "Nic tu nie ma"
        MiscPhrase.TapPlusToCreatePlaylist -> "Stuknij przycisk \"+\", aby utworzyć nową playlistę"
        MiscPhrase.Selected -> "wybrane"
        MiscPhrase.ChooseSongs -> "Wybierz utwory"
        MiscPhrase.AddSongs -> "Dodaj utwory"
    }
    AppLanguage.Albanian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Shtuar së fundi"
        MiscPhrase.WhatsNew -> "Çfarë ka të re?"
        MiscPhrase.NoSongsYet -> "Nuk ka ende këngë"
        MiscPhrase.AddSongsViaEdit -> "Shto këngë këtu duke prekur butonin e modifikimit"
        MiscPhrase.NothingInHere -> "Asgjë këtu"
        MiscPhrase.TapPlusToCreatePlaylist -> "Prek butonin \"+\" për të krijuar një playlistë të re"
        MiscPhrase.Selected -> "zgjedhur"
        MiscPhrase.ChooseSongs -> "Zgjidh këngë"
        MiscPhrase.AddSongs -> "Shto këngë"
    }
    AppLanguage.ChineseSimplified -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "最近添加"
        MiscPhrase.WhatsNew -> "有什么新内容？"
        MiscPhrase.NoSongsYet -> "还没有歌曲"
        MiscPhrase.AddSongsViaEdit -> "点击编辑按钮在此添加歌曲"
        MiscPhrase.NothingInHere -> "这里什么都没有"
        MiscPhrase.TapPlusToCreatePlaylist -> "点按“+”按钮以创建新播放列表"
        MiscPhrase.Selected -> "已选择"
        MiscPhrase.ChooseSongs -> "选择歌曲"
        MiscPhrase.AddSongs -> "添加歌曲"
    }
    AppLanguage.Croatian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nedavno dodano"
        MiscPhrase.WhatsNew -> "Što je novo?"
        MiscPhrase.NoSongsYet -> "Još nema pjesama"
        MiscPhrase.AddSongsViaEdit -> "Dodajte pjesme ovdje dodirom na gumb za uređivanje"
        MiscPhrase.NothingInHere -> "Ovdje nema ničega"
        MiscPhrase.TapPlusToCreatePlaylist -> "Dodirnite gumb \"+\" za izradu nove playliste"
        MiscPhrase.Selected -> "odabrano"
        MiscPhrase.ChooseSongs -> "Odaberi pjesme"
        MiscPhrase.AddSongs -> "Dodaj pjesme"
    }
    AppLanguage.Czech -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nedávno přidané"
        MiscPhrase.WhatsNew -> "Co je nového?"
        MiscPhrase.NoSongsYet -> "Zatím žádné skladby"
        MiscPhrase.AddSongsViaEdit -> "Přidejte sem skladby klepnutím na tlačítko úprav"
        MiscPhrase.NothingInHere -> "Nic tu není"
        MiscPhrase.TapPlusToCreatePlaylist -> "Klepnutím na tlačítko \"+\" vytvoříte nový playlist"
        MiscPhrase.Selected -> "vybráno"
        MiscPhrase.ChooseSongs -> "Vyberte skladby"
        MiscPhrase.AddSongs -> "Přidat skladby"
    }
    AppLanguage.Danish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nyligt tilføjet"
        MiscPhrase.WhatsNew -> "Hvad er nyt?"
        MiscPhrase.NoSongsYet -> "Ingen sange endnu"
        MiscPhrase.AddSongsViaEdit -> "Tilføj sange her ved at trykke på redigeringsknappen"
        MiscPhrase.NothingInHere -> "Her er ingenting"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tryk på \"+\"-knappen for at oprette en ny playliste"
        MiscPhrase.Selected -> "valgt"
        MiscPhrase.ChooseSongs -> "Vælg sange"
        MiscPhrase.AddSongs -> "Tilføj sange"
    }
    AppLanguage.Dutch -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Recent toegevoegd"
        MiscPhrase.WhatsNew -> "Wat is er nieuw?"
        MiscPhrase.NoSongsYet -> "Nog geen nummers"
        MiscPhrase.AddSongsViaEdit -> "Voeg hier nummers toe door op de bewerkknop te tikken"
        MiscPhrase.NothingInHere -> "Hier staat niets"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tik op de knop \"+\" om een nieuwe afspeellijst te maken"
        MiscPhrase.Selected -> "geselecteerd"
        MiscPhrase.ChooseSongs -> "Kies nummers"
        MiscPhrase.AddSongs -> "Nummers toevoegen"
    }
    AppLanguage.Estonian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Hiljuti lisatud"
        MiscPhrase.WhatsNew -> "Mis on uut?"
        MiscPhrase.NoSongsYet -> "Laule pole veel"
        MiscPhrase.AddSongsViaEdit -> "Lisa siia lugusid, puudutades muutmisnuppu"
        MiscPhrase.NothingInHere -> "Siin pole midagi"
        MiscPhrase.TapPlusToCreatePlaylist -> "Puuduta nuppu \"+\", et luua uus esitusloend"
        MiscPhrase.Selected -> "valitud"
        MiscPhrase.ChooseSongs -> "Vali lood"
        MiscPhrase.AddSongs -> "Lisa lugusid"
    }
    AppLanguage.French -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Ajoutés récemment"
        MiscPhrase.WhatsNew -> "Quoi de neuf ?"
        MiscPhrase.NoSongsYet -> "Aucun morceau pour le moment"
        MiscPhrase.AddSongsViaEdit -> "Ajoutez des morceaux ici en touchant le bouton modifier"
        MiscPhrase.NothingInHere -> "Il n’y a rien ici"
        MiscPhrase.TapPlusToCreatePlaylist -> "Touchez le bouton \"+\" pour créer une nouvelle playlist"
        MiscPhrase.Selected -> "sélectionnés"
        MiscPhrase.ChooseSongs -> "Choisir des morceaux"
        MiscPhrase.AddSongs -> "Ajouter des morceaux"
    }
    AppLanguage.German -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Kürzlich hinzugefügt"
        MiscPhrase.WhatsNew -> "Was ist neu?"
        MiscPhrase.NoSongsYet -> "Noch keine Titel"
        MiscPhrase.AddSongsViaEdit -> "Füge hier Titel hinzu, indem du auf die Bearbeiten-Schaltfläche tippst"
        MiscPhrase.NothingInHere -> "Hier ist nichts"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tippe auf die Schaltfläche \"+\", um eine neue Playlist zu erstellen"
        MiscPhrase.Selected -> "ausgewählt"
        MiscPhrase.ChooseSongs -> "Titel auswählen"
        MiscPhrase.AddSongs -> "Titel hinzufügen"
    }
    AppLanguage.Greek -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Προστέθηκαν πρόσφατα"
        MiscPhrase.WhatsNew -> "Τι νέο υπάρχει;"
        MiscPhrase.NoSongsYet -> "Δεν υπάρχουν ακόμη τραγούδια"
        MiscPhrase.AddSongsViaEdit -> "Προσθέστε τραγούδια εδώ πατώντας το κουμπί επεξεργασίας"
        MiscPhrase.NothingInHere -> "Δεν υπάρχει τίποτα εδώ"
        MiscPhrase.TapPlusToCreatePlaylist -> "Πατήστε το κουμπί \"+\" για να δημιουργήσετε νέα λίστα αναπαραγωγής"
        MiscPhrase.Selected -> "επιλεγμένα"
        MiscPhrase.ChooseSongs -> "Επιλέξτε τραγούδια"
        MiscPhrase.AddSongs -> "Προσθήκη τραγουδιών"
    }
    AppLanguage.Hindi -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "हाल ही में जोड़े गए"
        MiscPhrase.WhatsNew -> "नया क्या है?"
        MiscPhrase.NoSongsYet -> "अभी तक कोई गाने नहीं"
        MiscPhrase.AddSongsViaEdit -> "एडिट बटन दबाकर यहाँ गाने जोड़ें"
        MiscPhrase.NothingInHere -> "यहाँ कुछ नहीं है"
        MiscPhrase.TapPlusToCreatePlaylist -> "नई प्लेलिस्ट बनाने के लिए \"+\" बटन दबाएँ"
        MiscPhrase.Selected -> "चुने गए"
        MiscPhrase.ChooseSongs -> "गाने चुनें"
        MiscPhrase.AddSongs -> "गाने जोड़ें"
    }
    AppLanguage.Hungarian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nemrég hozzáadva"
        MiscPhrase.WhatsNew -> "Mi az újdonság?"
        MiscPhrase.NoSongsYet -> "Még nincsenek dalok"
        MiscPhrase.AddSongsViaEdit -> "Adj hozzá dalokat itt a szerkesztés gomb megérintésével"
        MiscPhrase.NothingInHere -> "Itt nincs semmi"
        MiscPhrase.TapPlusToCreatePlaylist -> "Érintsd meg a \"+\" gombot új lejátszási lista létrehozásához"
        MiscPhrase.Selected -> "kiválasztva"
        MiscPhrase.ChooseSongs -> "Válassz dalokat"
        MiscPhrase.AddSongs -> "Dalok hozzáadása"
    }
    AppLanguage.Italian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Aggiunti di recente"
        MiscPhrase.WhatsNew -> "Cosa c'è di nuovo?"
        MiscPhrase.NoSongsYet -> "Nessun brano ancora"
        MiscPhrase.AddSongsViaEdit -> "Aggiungi qui i brani toccando il pulsante modifica"
        MiscPhrase.NothingInHere -> "Qui non c’è nulla"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tocca il pulsante \"+\" per creare una nuova playlist"
        MiscPhrase.Selected -> "selezionati"
        MiscPhrase.ChooseSongs -> "Scegli brani"
        MiscPhrase.AddSongs -> "Aggiungi brani"
    }
    AppLanguage.Japanese -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "最近追加"
        MiscPhrase.WhatsNew -> "新着情報"
        MiscPhrase.NoSongsYet -> "まだ曲がありません"
        MiscPhrase.AddSongsViaEdit -> "編集ボタンをタップしてここに曲を追加します"
        MiscPhrase.NothingInHere -> "ここには何もありません"
        MiscPhrase.TapPlusToCreatePlaylist -> "新しいプレイリストを作成するには「+」ボタンをタップします"
        MiscPhrase.Selected -> "選択済み"
        MiscPhrase.ChooseSongs -> "曲を選択"
        MiscPhrase.AddSongs -> "曲を追加"
    }
    AppLanguage.Latin -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nuper addita"
        MiscPhrase.WhatsNew -> "Quid novi?"
        MiscPhrase.NoSongsYet -> "Nulli cantus adhuc"
        MiscPhrase.AddSongsViaEdit -> "Cantus hic adde tangendo bullam emendandi"
        MiscPhrase.NothingInHere -> "Nihil hic est"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tange bullam \"+\" ut novam indicem crees"
        MiscPhrase.Selected -> "selecta"
        MiscPhrase.ChooseSongs -> "Elige cantus"
        MiscPhrase.AddSongs -> "Adde cantus"
    }
    AppLanguage.Latvian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nesen pievienots"
        MiscPhrase.WhatsNew -> "Kas jauns?"
        MiscPhrase.NoSongsYet -> "Vēl nav dziesmu"
        MiscPhrase.AddSongsViaEdit -> "Pievieno dziesmas šeit, pieskaroties rediģēšanas pogai"
        MiscPhrase.NothingInHere -> "Šeit nekā nav"
        MiscPhrase.TapPlusToCreatePlaylist -> "Pieskarieties pogai \"+\", lai izveidotu jaunu atskaņošanas sarakstu"
        MiscPhrase.Selected -> "atlasīts"
        MiscPhrase.ChooseSongs -> "Izvēlies dziesmas"
        MiscPhrase.AddSongs -> "Pievienot dziesmas"
    }
    AppLanguage.Lithuanian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Neseniai pridėta"
        MiscPhrase.WhatsNew -> "Kas naujo?"
        MiscPhrase.NoSongsYet -> "Dar nėra dainų"
        MiscPhrase.AddSongsViaEdit -> "Pridėkite dainas čia paliesdami redagavimo mygtuką"
        MiscPhrase.NothingInHere -> "Čia nieko nėra"
        MiscPhrase.TapPlusToCreatePlaylist -> "Palieskite mygtuką \"+\", kad sukurtumėte naują grojaraštį"
        MiscPhrase.Selected -> "pasirinkta"
        MiscPhrase.ChooseSongs -> "Pasirinkite dainas"
        MiscPhrase.AddSongs -> "Pridėti dainas"
    }
    AppLanguage.Macedonian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Неодамна додадено"
        MiscPhrase.WhatsNew -> "Што има ново?"
        MiscPhrase.NoSongsYet -> "Сè уште нема песни"
        MiscPhrase.AddSongsViaEdit -> "Додај песни тука со допирање на копчето за уредување"
        MiscPhrase.NothingInHere -> "Нема ништо тука"
        MiscPhrase.TapPlusToCreatePlaylist -> "Допрете го копчето \"+\" за да создадете нова плејлиста"
        MiscPhrase.Selected -> "избрано"
        MiscPhrase.ChooseSongs -> "Избери песни"
        MiscPhrase.AddSongs -> "Додај песни"
    }
    AppLanguage.Norwegian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nylig lagt til"
        MiscPhrase.WhatsNew -> "Hva er nytt?"
        MiscPhrase.NoSongsYet -> "Ingen sanger ennå"
        MiscPhrase.AddSongsViaEdit -> "Legg til sanger her ved å trykke på redigeringsknappen"
        MiscPhrase.NothingInHere -> "Det er ingenting her"
        MiscPhrase.TapPlusToCreatePlaylist -> "Trykk på \"+\"-knappen for å opprette en ny spilleliste"
        MiscPhrase.Selected -> "valgt"
        MiscPhrase.ChooseSongs -> "Velg sanger"
        MiscPhrase.AddSongs -> "Legg til sanger"
    }
    AppLanguage.Portuguese -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Adicionados recentemente"
        MiscPhrase.WhatsNew -> "O que há de novo?"
        MiscPhrase.NoSongsYet -> "Ainda não há músicas"
        MiscPhrase.AddSongsViaEdit -> "Adicione músicas aqui tocando no botão editar"
        MiscPhrase.NothingInHere -> "Não há nada aqui"
        MiscPhrase.TapPlusToCreatePlaylist -> "Toque no botão \"+\" para criar uma nova playlist"
        MiscPhrase.Selected -> "selecionados"
        MiscPhrase.ChooseSongs -> "Escolher músicas"
        MiscPhrase.AddSongs -> "Adicionar músicas"
    }
    AppLanguage.Russian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Недавно добавлено"
        MiscPhrase.WhatsNew -> "Что нового?"
        MiscPhrase.NoSongsYet -> "Песен пока нет"
        MiscPhrase.AddSongsViaEdit -> "Добавьте песни сюда, нажав кнопку редактирования"
        MiscPhrase.NothingInHere -> "Здесь ничего нет"
        MiscPhrase.TapPlusToCreatePlaylist -> "Нажмите кнопку \"+\", чтобы создать новый плейлист"
        MiscPhrase.Selected -> "выбрано"
        MiscPhrase.ChooseSongs -> "Выберите песни"
        MiscPhrase.AddSongs -> "Добавить песни"
    }
    AppLanguage.Serbian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Недавно додато"
        MiscPhrase.WhatsNew -> "Шта је ново?"
        MiscPhrase.NoSongsYet -> "Још нема песама"
        MiscPhrase.AddSongsViaEdit -> "Додај песме овде додиром на дугме за уређивање"
        MiscPhrase.NothingInHere -> "Овде нема ничега"
        MiscPhrase.TapPlusToCreatePlaylist -> "Додирни дугме \"+\" да направиш нову листу песама"
        MiscPhrase.Selected -> "изабрано"
        MiscPhrase.ChooseSongs -> "Изабери песме"
        MiscPhrase.AddSongs -> "Додај песме"
    }
    AppLanguage.Spanish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Añadidos recientemente"
        MiscPhrase.WhatsNew -> "¿Qué hay de nuevo?"
        MiscPhrase.NoSongsYet -> "Aún no hay canciones"
        MiscPhrase.AddSongsViaEdit -> "Añade canciones aquí tocando el botón de editar"
        MiscPhrase.NothingInHere -> "No hay nada aquí"
        MiscPhrase.TapPlusToCreatePlaylist -> "Toca el botón \"+\" para crear una nueva playlist"
        MiscPhrase.Selected -> "seleccionados"
        MiscPhrase.ChooseSongs -> "Elegir canciones"
        MiscPhrase.AddSongs -> "Añadir canciones"
    }
    AppLanguage.Swedish -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nyligen tillagt"
        MiscPhrase.WhatsNew -> "Vad är nytt?"
        MiscPhrase.NoSongsYet -> "Inga låtar ännu"
        MiscPhrase.AddSongsViaEdit -> "Lägg till låtar här genom att trycka på redigeringsknappen"
        MiscPhrase.NothingInHere -> "Det finns inget här"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tryck på \"+\"-knappen för att skapa en ny spellista"
        MiscPhrase.Selected -> "valda"
        MiscPhrase.ChooseSongs -> "Välj låtar"
        MiscPhrase.AddSongs -> "Lägg till låtar"
    }
    AppLanguage.Thai -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "เพิ่มล่าสุด"
        MiscPhrase.WhatsNew -> "มีอะไรใหม่?"
        MiscPhrase.NoSongsYet -> "ยังไม่มีเพลง"
        MiscPhrase.AddSongsViaEdit -> "เพิ่มเพลงที่นี่ด้วยการแตะปุ่มแก้ไข"
        MiscPhrase.NothingInHere -> "ที่นี่ไม่มีอะไร"
        MiscPhrase.TapPlusToCreatePlaylist -> "แตะปุ่ม \"+\" เพื่อสร้างเพลย์ลิสต์ใหม่"
        MiscPhrase.Selected -> "ที่เลือก"
        MiscPhrase.ChooseSongs -> "เลือกเพลง"
        MiscPhrase.AddSongs -> "เพิ่มเพลง"
    }
    AppLanguage.Ukrainian -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Нещодавно додано"
        MiscPhrase.WhatsNew -> "Що нового?"
        MiscPhrase.NoSongsYet -> "Пісень ще немає"
        MiscPhrase.AddSongsViaEdit -> "Додайте сюди пісні, натиснувши кнопку редагування"
        MiscPhrase.NothingInHere -> "Тут нічого немає"
        MiscPhrase.TapPlusToCreatePlaylist -> "Натисніть кнопку \"+\", щоб створити новий плейлист"
        MiscPhrase.Selected -> "вибрано"
        MiscPhrase.ChooseSongs -> "Оберіть пісні"
        MiscPhrase.AddSongs -> "Додати пісні"
    }
    AppLanguage.Slovak -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Nedávno pridané"
        MiscPhrase.WhatsNew -> "Čo je nové?"
        MiscPhrase.NoSongsYet -> "Zatiaľ žiadne skladby"
        MiscPhrase.AddSongsViaEdit -> "Pridajte sem skladby klepnutím na tlačidlo úprav"
        MiscPhrase.NothingInHere -> "Nie je tu nič"
        MiscPhrase.TapPlusToCreatePlaylist -> "Klepnutím na tlačidlo \"+\" vytvoríte nový playlist"
        MiscPhrase.Selected -> "vybrané"
        MiscPhrase.ChooseSongs -> "Vyberte skladby"
        MiscPhrase.AddSongs -> "Pridať skladby"
    }
    AppLanguage.Korean -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "최근 추가됨"
        MiscPhrase.WhatsNew -> "새로운 내용"
        MiscPhrase.NoSongsYet -> "아직 곡이 없습니다"
        MiscPhrase.AddSongsViaEdit -> "편집 버튼을 눌러 여기에 곡을 추가하세요"
        MiscPhrase.NothingInHere -> "여기에는 아무것도 없습니다"
        MiscPhrase.TapPlusToCreatePlaylist -> "새 플레이리스트를 만들려면 \"+\" 버튼을 누르세요"
        MiscPhrase.Selected -> "선택됨"
        MiscPhrase.ChooseSongs -> "곡 선택"
        MiscPhrase.AddSongs -> "곡 추가"
    }
    AppLanguage.Malay -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Baru ditambah"
        MiscPhrase.WhatsNew -> "Apa yang baharu?"
        MiscPhrase.NoSongsYet -> "Belum ada lagu"
        MiscPhrase.AddSongsViaEdit -> "Tambah lagu di sini dengan mengetik butang edit"
        MiscPhrase.NothingInHere -> "Tiada apa-apa di sini"
        MiscPhrase.TapPlusToCreatePlaylist -> "Ketik butang \"+\" untuk mencipta senarai main baharu"
        MiscPhrase.Selected -> "dipilih"
        MiscPhrase.ChooseSongs -> "Pilih lagu"
        MiscPhrase.AddSongs -> "Tambah lagu"
    }
    AppLanguage.Bengali -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "সম্প্রতি যোগ করা"
        MiscPhrase.WhatsNew -> "নতুন কী?"
        MiscPhrase.NoSongsYet -> "এখনও কোনো গান নেই"
        MiscPhrase.AddSongsViaEdit -> "এডিট বোতাম ট্যাপ করে এখানে গান যোগ করুন"
        MiscPhrase.NothingInHere -> "এখানে কিছুই নেই"
        MiscPhrase.TapPlusToCreatePlaylist -> "নতুন প্লেলিস্ট তৈরি করতে \"+\" বোতাম ট্যাপ করুন"
        MiscPhrase.Selected -> "নির্বাচিত"
        MiscPhrase.ChooseSongs -> "গান বেছে নিন"
        MiscPhrase.AddSongs -> "গান যোগ করুন"
    }
    AppLanguage.Urdu -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "حال ہی میں شامل کردہ"
        MiscPhrase.WhatsNew -> "نیا کیا ہے؟"
        MiscPhrase.NoSongsYet -> "ابھی تک کوئی گانا نہیں"
        MiscPhrase.AddSongsViaEdit -> "ایڈٹ بٹن دبا کر یہاں گانے شامل کریں"
        MiscPhrase.NothingInHere -> "یہاں کچھ نہیں ہے"
        MiscPhrase.TapPlusToCreatePlaylist -> "نئی پلے لسٹ بنانے کے لیے \"+\" بٹن دبائیں"
        MiscPhrase.Selected -> "منتخب"
        MiscPhrase.ChooseSongs -> "گانے منتخب کریں"
        MiscPhrase.AddSongs -> "گانے شامل کریں"
    }
    AppLanguage.English -> when (phrase) {
        MiscPhrase.RecentlyAdded -> "Recently added"
        MiscPhrase.WhatsNew -> "What’s new?"
        MiscPhrase.NoSongsYet -> "No songs yet"
        MiscPhrase.AddSongsViaEdit -> "Add songs here by tapping on edit button"
        MiscPhrase.NothingInHere -> "Nothing in here"
        MiscPhrase.TapPlusToCreatePlaylist -> "Tap the \"+\" button to create new playlist"
        MiscPhrase.Selected -> "selected"
        MiscPhrase.ChooseSongs -> "Choose songs"
        MiscPhrase.AddSongs -> "Add songs"
    }
}

internal data class CommonUiCopy(
    val home: String,
    val library: String,
    val playlists: String,
    val search: String,
    val welcome: String,
    val songs: String,
    val albums: String,
    val artists: String,
    val genres: String,
    val light: String,
    val dark: String,
    val system: String,
    val inYourLibrary: String,
    val inTotal: String,
    val found: String,
    val refinedFooter: String,
)

internal fun commonUiCopy(language: AppLanguage): CommonUiCopy = when (language) {
    AppLanguage.Polish -> CommonUiCopy("Główna", "Biblioteka", "Playlisty", "Szukaj", "Witamy", "Utwory", "Albumy", "Artyści", "Gatunki", "Jasny", "Ciemny", "System", "w Twojej bibliotece", "łącznie", "znaleziono", "Twoja muzyka, dopracowana w eleganckie doświadczenie")
    AppLanguage.Albanian -> CommonUiCopy("Kreu", "Biblioteka", "Listat", "Kërko", "Mirë se vini", "Këngë", "Albume", "Artistë", "Zhanre", "E çelët", "E errët", "Sistemi", "në bibliotekën tënde", "gjithsej", "u gjetën", "Muzika jote, e rafinuar në një përvojë elegante")
    AppLanguage.ChineseSimplified -> CommonUiCopy("主页", "媒体库", "播放列表", "搜索", "欢迎", "歌曲", "专辑", "艺人", "流派", "浅色", "深色", "跟随系统", "在你的媒体库中", "总计", "已找到", "你的音乐，被雕琢成优雅的体验")
    AppLanguage.Croatian -> CommonUiCopy("Početna", "Biblioteka", "Playliste", "Pretraži", "Dobrodošli", "Pjesme", "Albumi", "Izvođači", "Žanrovi", "Svijetlo", "Tamno", "Sustav", "u tvojoj biblioteci", "ukupno", "pronađeno", "Tvoja glazba, profinjena u elegantno iskustvo")
    AppLanguage.Czech -> CommonUiCopy("Domů", "Knihovna", "Playlisty", "Hledat", "Vítejte", "Skladby", "Alba", "Umělci", "Žánry", "Světlý", "Tmavý", "Systém", "ve vaší knihovně", "celkem", "nalezeno", "Vaše hudba, vytříbená do elegantního zážitku")
    AppLanguage.Danish -> CommonUiCopy("Hjem", "Bibliotek", "Playlister", "Søg", "Velkommen", "Sange", "Albummer", "Kunstnere", "Genrer", "Lys", "Mørk", "System", "i dit bibliotek", "i alt", "fundet", "Din musik, raffineret til en elegant oplevelse")
    AppLanguage.Dutch -> CommonUiCopy("Home", "Bibliotheek", "Afspeellijsten", "Zoeken", "Welkom", "Nummers", "Albums", "Artiesten", "Genres", "Licht", "Donker", "Systeem", "in je bibliotheek", "in totaal", "gevonden", "Jouw muziek, verfijnd tot een elegante ervaring")
    AppLanguage.Estonian -> CommonUiCopy("Avaleht", "Teek", "Esitusloendid", "Otsi", "Tere tulemast", "Lood", "Albumid", "Artistid", "Žanrid", "Hele", "Tume", "Süsteem", "sinu teegis", "kokku", "leitud", "Sinu muusika, viimistletud elegantseks elamuseks")
    AppLanguage.French -> CommonUiCopy("Accueil", "Bibliothèque", "Playlists", "Recherche", "Bienvenue", "Morceaux", "Albums", "Artistes", "Genres", "Clair", "Sombre", "Système", "dans votre bibliothèque", "au total", "trouvés", "Votre musique, affinée en une expérience élégante")
    AppLanguage.German -> CommonUiCopy("Start", "Bibliothek", "Playlists", "Suche", "Willkommen", "Titel", "Alben", "Künstler", "Genres", "Hell", "Dunkel", "System", "in deiner Bibliothek", "insgesamt", "gefunden", "Deine Musik, veredelt zu einem eleganten Erlebnis")
    AppLanguage.Greek -> CommonUiCopy("Αρχική", "Βιβλιοθήκη", "Playlists", "Αναζήτηση", "Καλώς ήρθατε", "Τραγούδια", "Άλμπουμ", "Καλλιτέχνες", "Είδη", "Φωτεινό", "Σκούρο", "Σύστημα", "στη βιβλιοθήκη σας", "συνολικά", "βρέθηκαν", "Η μουσική σας, εκλεπτυσμένη σε μια κομψή εμπειρία")
    AppLanguage.Hindi -> CommonUiCopy("होम", "लाइब्रेरी", "प्लेलिस्ट", "खोजें", "स्वागत है", "गाने", "एल्बम", "कलाकार", "शैलियाँ", "लाइट", "डार्क", "सिस्टम", "आपकी लाइब्रेरी में", "कुल", "मिले", "आपका संगीत, एक सुरुचिपूर्ण अनुभव में निखरा हुआ")
    AppLanguage.Hungarian -> CommonUiCopy("Kezdőlap", "Könyvtár", "Lejátszási listák", "Keresés", "Üdvözöljük", "Dalok", "Albumok", "Előadók", "Műfajok", "Világos", "Sötét", "Rendszer", "a könyvtáradban", "összesen", "találat", "A zenéd, kifinomítva elegáns élménnyé")
    AppLanguage.Italian -> CommonUiCopy("Home", "Libreria", "Playlist", "Cerca", "Benvenuto", "Brani", "Album", "Artisti", "Generi", "Chiaro", "Scuro", "Sistema", "nella tua libreria", "in totale", "trovati", "La tua musica, rifinita in un'esperienza elegante")
    AppLanguage.Japanese -> CommonUiCopy("ホーム", "ライブラリ", "プレイリスト", "検索", "ようこそ", "曲", "アルバム", "アーティスト", "ジャンル", "ライト", "ダーク", "システム", "ライブラリ内", "合計", "見つかりました", "あなたの音楽を、洗練された体験へ")
    AppLanguage.Latin -> CommonUiCopy("Domus", "Bibliotheca", "Indices", "Quaere", "Salve", "Cantus", "Albumina", "Artifices", "Genera", "Clarus", "Obscurus", "Systema", "in bibliotheca tua", "omnino", "inventa", "Musica tua, in experientiam elegantem expolita")
    AppLanguage.Latvian -> CommonUiCopy("Sākums", "Bibliotēka", "Atskaņošanas saraksti", "Meklēt", "Laipni lūdzam", "Dziesmas", "Albumi", "Mākslinieki", "Žanri", "Gaišs", "Tumšs", "Sistēma", "tavā bibliotēkā", "kopā", "atrasts", "Tava mūzika, izsmalcināta elegantā pieredzē")
    AppLanguage.Lithuanian -> CommonUiCopy("Pradžia", "Biblioteka", "Grojaraščiai", "Paieška", "Sveiki", "Dainos", "Albumai", "Atlikėjai", "Žanrai", "Šviesi", "Tamsi", "Sistema", "jūsų bibliotekoje", "iš viso", "rasta", "Tavo muzika, ištobulinta į elegantišką patirtį")
    AppLanguage.Macedonian -> CommonUiCopy("Почетна", "Библиотека", "Плејлисти", "Пребарај", "Добредојдовте", "Песни", "Албуми", "Артисти", "Жанрови", "Светла", "Темна", "Систем", "во вашата библиотека", "вкупно", "пронајдени", "Вашата музика, префинета во елегантно доживување")
    AppLanguage.Norwegian -> CommonUiCopy("Hjem", "Bibliotek", "Spillelister", "Søk", "Velkommen", "Sanger", "Album", "Artister", "Sjangre", "Lys", "Mørk", "System", "i biblioteket ditt", "totalt", "funnet", "Musikken din, raffinert til en elegant opplevelse")
    AppLanguage.Portuguese -> CommonUiCopy("Início", "Biblioteca", "Playlists", "Pesquisar", "Bem-vindo", "Músicas", "Álbuns", "Artistas", "Géneros", "Claro", "Escuro", "Sistema", "na sua biblioteca", "no total", "encontrados", "A sua música, refinada numa experiência elegante")
    AppLanguage.Russian -> CommonUiCopy("Главная", "Библиотека", "Плейлисты", "Поиск", "Добро пожаловать", "Песни", "Альбомы", "Исполнители", "Жанры", "Светлая", "Тёмная", "Система", "в вашей библиотеке", "всего", "найдено", "Ваша музыка, отточенная до элегантного опыта")
    AppLanguage.Serbian -> CommonUiCopy("Почетна", "Библиотека", "Плејлисте", "Претрага", "Добро дошли", "Песме", "Албуми", "Извођачи", "Жанрови", "Светла", "Тамна", "Систем", "у вашој библиотеци", "укупно", "пронађено", "Ваша музика, префињена у елегантно искуство")
    AppLanguage.Spanish -> CommonUiCopy("Inicio", "Biblioteca", "Playlists", "Buscar", "Bienvenido", "Canciones", "Álbumes", "Artistas", "Géneros", "Claro", "Oscuro", "Sistema", "en tu biblioteca", "en total", "encontrados", "Tu música, refinada en una experiencia elegante")
    AppLanguage.Swedish -> CommonUiCopy("Hem", "Bibliotek", "Spellistor", "Sök", "Välkommen", "Låtar", "Album", "Artister", "Genrer", "Ljust", "Mörkt", "System", "i ditt bibliotek", "totalt", "hittade", "Din musik, förädlad till en elegant upplevelse")
    AppLanguage.Thai -> CommonUiCopy("หน้าแรก", "คลังเพลง", "เพลย์ลิสต์", "ค้นหา", "ยินดีต้อนรับ", "เพลง", "อัลบั้ม", "ศิลปิน", "แนวเพลง", "สว่าง", "มืด", "ระบบ", "ในคลังของคุณ", "ทั้งหมด", "พบ", "เพลงของคุณ ถูกขัดเกลาให้เป็นประสบการณ์อันสง่างาม")
    AppLanguage.Ukrainian -> CommonUiCopy("Головна", "Бібліотека", "Плейлисти", "Пошук", "Ласкаво просимо", "Пісні", "Альбоми", "Виконавці", "Жанри", "Світла", "Темна", "Система", "у вашій бібліотеці", "усього", "знайдено", "Ваша музика, відточена до елегантного досвіду")
    AppLanguage.Slovak -> CommonUiCopy("Domov", "Knižnica", "Playlisty", "Hľadať", "Vitajte", "Skladby", "Albumy", "Interpreti", "Žánre", "Svetlý", "Tmavý", "Systém", "vo vašej knižnici", "celkovo", "nájdené", "Vaša hudba, premenená na elegantný zážitok")
    AppLanguage.Korean -> CommonUiCopy("홈", "라이브러리", "플레이리스트", "검색", "환영합니다", "곡", "앨범", "아티스트", "장르", "라이트", "다크", "시스템", "라이브러리 안", "전체", "찾음", "당신의 음악을 우아한 경험으로 다듬었습니다")
    AppLanguage.Malay -> CommonUiCopy("Laman utama", "Pustaka", "Senarai main", "Cari", "Selamat datang", "Lagu", "Album", "Artis", "Genre", "Cerah", "Gelap", "Sistem", "dalam pustaka anda", "jumlah", "ditemui", "Muzik anda, diperhalus menjadi pengalaman yang elegan")
    AppLanguage.Bengali -> CommonUiCopy("হোম", "লাইব্রেরি", "প্লেলিস্ট", "সার্চ", "স্বাগতম", "গান", "অ্যালবাম", "শিল্পী", "ধরন", "হালকা", "গাঢ়", "সিস্টেম", "আপনার লাইব্রেরিতে", "মোট", "পাওয়া গেছে", "আপনার সঙ্গীত, একে পরিণত করা হয়েছে এক মার্জিত অভিজ্ঞতায়")
    AppLanguage.Urdu -> CommonUiCopy("ہوم", "لائبریری", "پلے لسٹس", "تلاش", "خوش آمدید", "گانے", "البمز", "آرٹسٹس", "اصناف", "ہلکا", "گہرا", "سسٹم", "آپ کی لائبریری میں", "کل", "ملے", "آپ کی موسیقی، ایک نفیس تجربے میں ڈھلی ہوئی")
    AppLanguage.English -> CommonUiCopy("Home", "Library", "Playlists", "Search", "Welcome", "Songs", "Albums", "Artists", "Genres", "Light", "Dark", "System", "in your library", "in total", "found", "Your music, refined into an elegant experience")
}

internal data class SearchUiCopy(
    val placeholder: String,
    val clearSearch: String,
    val nothingSearchedTitle: String,
    val nothingSearchedMessage: String,
    val suggestedAlbumsTitle: String,
    val suggestedAlbumsSubtitle: String,
    val recentlySearched: String,
    val clearHistory: String,
    val noResultsTitle: String,
    val noResultsPrefix: String,
    val noResultsSuffix: String,
    val matchingArtistsSuffix: String,
    val matchingAlbumsSuffix: String,
    val matchingSongsSuffix: String,
) {
    fun noResultsMessage(query: String): String = "$noResultsPrefix \"$query\" $noResultsSuffix"
    fun matchingArtists(count: Int): String = "$count $matchingArtistsSuffix"
    fun matchingAlbums(count: Int): String = "$count $matchingAlbumsSuffix"
    fun matchingSongs(count: Int): String = "$count $matchingSongsSuffix"
}

internal fun searchCopy(language: AppLanguage): SearchUiCopy = when (language) {
    AppLanguage.Polish -> SearchUiCopy("Artyści, albumy i więcej", "Wyczyść wyszukiwanie", "Jeszcze nic nie wyszukano", "Więcej wyników pojawi się podczas wyszukiwania utworów i albumów", "Sugerowane albumy", "Warto do nich wrócić", "Ostatnio wyszukiwane", "Wyczyść historię", "Brak wyników", "Nic w obecnej bibliotece offline nie pasuje do", "jeszcze", "pasujących artystów", "pasujących albumów", "pasujących utworów")
    AppLanguage.ChineseSimplified -> SearchUiCopy("艺人、专辑等", "清除搜索", "还没有搜索", "搜索歌曲和专辑时会显示更多结果", "推荐专辑", "你可能会想再听听", "最近搜索", "清除历史", "没有结果", "当前离线媒体库中没有匹配", "", "个匹配艺人", "个匹配专辑", "个匹配歌曲")
    AppLanguage.Czech -> SearchUiCopy("Umělci, alba a další", "Vymazat hledání", "Zatím nic nehledáno", "Další výsledky se zobrazí při hledání skladeb a alb", "Navržená alba", "Možná se k nim chcete vrátit", "Nedávno hledané", "Vymazat historii", "Žádné výsledky", "V aktuální offline knihovně se nic neshoduje s", "zatím", "odpovídajících umělců", "odpovídajících alb", "odpovídajících skladeb")
    AppLanguage.French -> SearchUiCopy("Artistes, albums et plus", "Effacer la recherche", "Aucune recherche pour l’instant", "Plus de résultats apparaîtront pendant la recherche de morceaux et d’albums", "Albums suggérés", "Vous devriez peut-être les réécouter", "Recherches récentes", "Effacer l’historique", "Aucun résultat", "Rien dans la bibliothèque hors ligne actuelle ne correspond à", "pour l’instant", "artistes correspondants", "albums correspondants", "morceaux correspondants")
    AppLanguage.German -> SearchUiCopy("Künstler, Alben und mehr", "Suche löschen", "Noch nichts gesucht", "Weitere Ergebnisse erscheinen, wenn du nach Songs und Alben suchst", "Vorgeschlagene Alben", "Diese solltest du vielleicht wieder hören", "Zuletzt gesucht", "Verlauf löschen", "Keine Ergebnisse", "In der aktuellen Offline-Bibliothek passt nichts zu", "bisher", "passende Künstler", "passende Alben", "passende Songs")
    AppLanguage.Italian -> SearchUiCopy("Artisti, album e altro", "Cancella ricerca", "Nessuna ricerca ancora", "Altri risultati appariranno mentre cerchi brani e album", "Album suggeriti", "Potresti volerli riascoltare", "Ricerche recenti", "Cancella cronologia", "Nessun risultato", "Nella libreria offline attuale non corrisponde nulla a", "ancora", "artisti corrispondenti", "album corrispondenti", "brani corrispondenti")
    AppLanguage.Japanese -> SearchUiCopy("アーティスト、アルバムなど", "検索をクリア", "まだ検索していません", "曲やアルバムを検索すると、さらに結果が表示されます", "おすすめアルバム", "また聴きたくなるかもしれません", "最近の検索", "履歴を消去", "結果なし", "現在のオフラインライブラリに一致するものはありません:", "", "件の一致するアーティスト", "件の一致するアルバム", "件の一致する曲")
    AppLanguage.Spanish -> SearchUiCopy("Artistas, álbumes y más", "Borrar búsqueda", "Aún no has buscado nada", "Aparecerán más resultados al buscar canciones y álbumes", "Álbumes sugeridos", "Quizá quieras volver a escucharlos", "Búsquedas recientes", "Borrar historial", "Sin resultados", "Nada en la biblioteca sin conexión actual coincide con", "todavía", "artistas coincidentes", "álbumes coincidentes", "canciones coincidentes")
    AppLanguage.Portuguese -> SearchUiCopy("Artistas, álbuns e mais", "Limpar pesquisa", "Ainda nada pesquisado", "Mais resultados aparecerão ao pesquisar músicas e álbuns", "Álbuns sugeridos", "Talvez queira revisitá-los", "Pesquisas recentes", "Limpar histórico", "Sem resultados", "Nada na biblioteca offline atual corresponde a", "ainda", "artistas correspondentes", "álbuns correspondentes", "músicas correspondentes")
    AppLanguage.Russian -> SearchUiCopy("Исполнители, альбомы и другое", "Очистить поиск", "Пока ничего не искали", "Больше результатов появится при поиске песен и альбомов", "Предложенные альбомы", "Возможно, стоит вернуться к ним", "Недавние поиски", "Очистить историю", "Нет результатов", "В текущей офлайн-библиотеке ничего не найдено для", "пока", "подходящих исполнителей", "подходящих альбомов", "подходящих песен")
    AppLanguage.Ukrainian -> SearchUiCopy("Виконавці, альбоми тощо", "Очистити пошук", "Поки нічого не шукали", "Більше результатів з’явиться під час пошуку пісень і альбомів", "Запропоновані альбоми", "Можливо, варто повернутися до них", "Нещодавні пошуки", "Очистити історію", "Немає результатів", "У поточній офлайн-бібліотеці нічого не збігається з", "поки", "відповідних виконавців", "відповідних альбомів", "відповідних пісень")
    AppLanguage.Slovak -> SearchUiCopy("Interpreti, albumy a ďalšie", "Vymazať hľadanie", "Zatiaľ nič nehľadané", "Ďalšie výsledky sa zobrazia počas hľadania skladieb a albumov", "Navrhované albumy", "Možno sa k nim budete chcieť vrátiť", "Nedávno hľadané", "Vymazať históriu", "Žiadne výsledky", "V aktuálnej offline knižnici sa nič nezhoduje s", "zatiaľ", "zodpovedajúcich interpretov", "zodpovedajúcich albumov", "zodpovedajúcich skladieb")
    AppLanguage.Korean -> SearchUiCopy("아티스트, 앨범 등", "검색 지우기", "아직 검색한 내용이 없습니다", "곡과 앨범을 검색하면 더 많은 결과가 여기에 표시됩니다", "추천 앨범", "다시 들어보고 싶을지도 모릅니다", "최근 검색", "기록 지우기", "결과 없음", "현재 오프라인 라이브러리에서 다음과 일치하는 항목이 없습니다", "", "개의 일치하는 아티스트", "개의 일치하는 앨범", "개의 일치하는 곡")
    AppLanguage.Malay -> SearchUiCopy("Artis, album dan banyak lagi", "Kosongkan carian", "Belum ada carian", "Lebih banyak hasil akan muncul di sini apabila anda mencari lagu dan album", "Album disyorkan", "Anda mungkin mahu kembali mendengarnya", "Carian terkini", "Kosongkan sejarah", "Tiada hasil", "Tiada apa-apa dalam pustaka luar talian semasa yang sepadan dengan", "lagi", "artis sepadan", "album sepadan", "lagu sepadan")
    AppLanguage.Bengali -> SearchUiCopy("শিল্পী, অ্যালবাম এবং আরও", "সার্চ মুছুন", "এখনও কিছু খোঁজা হয়নি", "গান ও অ্যালবাম খুঁজলে আরও ফল এখানে দেখা যাবে", "প্রস্তাবিত অ্যালবাম", "সম্ভবত এগুলোতে আবার ফিরতে চাইবেন", "সাম্প্রতিক অনুসন্ধান", "ইতিহাস মুছুন", "কোনো ফল নেই", "বর্তমান অফলাইন লাইব্রেরিতে এর সাথে মেলে এমন কিছু নেই", "এখনও", "মিল থাকা শিল্পী", "মিল থাকা অ্যালবাম", "মিল থাকা গান")
    AppLanguage.Urdu -> SearchUiCopy("آرٹسٹس، البمز اور مزید", "تلاش صاف کریں", "ابھی تک کچھ تلاش نہیں کیا گیا", "گانے اور البمز تلاش کرتے وقت مزید نتائج یہاں دکھائی دیں گے", "تجویز کردہ البمز", "شاید آپ دوبارہ انہیں سننا چاہیں", "حالیہ تلاشیں", "تاریخ صاف کریں", "کوئی نتیجہ نہیں", "موجودہ آف لائن لائبریری میں اس سے ملتا کچھ نہیں", "ابھی", "مطابق آرٹسٹس", "مطابق البمز", "مطابق گانے")
    else -> SearchUiCopy("Artists, albums & more", "Clear search", "Nothing searched yet", "More results will show here as you search for songs and albums", "Suggested albums", "You should probably revisit these", "Recently searched", "Clear history", "No results", "Nothing in the current offline library matches", "yet", "matching artists", "matching album results", "matching song results")
}

internal fun searchSortModeLabel(
    mode: SearchSongSortMode,
    language: AppLanguage,
): String = when (mode) {
    SearchSongSortMode.Title -> when (language) {
        AppLanguage.Polish -> "Nazwa utworu"
        AppLanguage.ChineseSimplified -> "歌曲名"
        AppLanguage.Czech -> "Název skladby"
        AppLanguage.French -> "Nom du morceau"
        AppLanguage.German -> "Songname"
        AppLanguage.Italian -> "Nome brano"
        AppLanguage.Japanese -> "曲名"
        AppLanguage.Spanish -> "Nombre de canción"
        AppLanguage.Portuguese -> "Nome da música"
        AppLanguage.Russian -> "Название песни"
        AppLanguage.Ukrainian -> "Назва пісні"
        AppLanguage.Slovak -> "Názov skladby"
        AppLanguage.Korean -> "곡 이름"
        AppLanguage.Malay -> "Nama lagu"
        AppLanguage.Bengali -> "গানের নাম"
        AppLanguage.Urdu -> "گانے کا نام"
        else -> "Song name"
    }
    SearchSongSortMode.Artist -> when (language) {
        AppLanguage.Polish -> "Nazwa artysty"
        AppLanguage.ChineseSimplified -> "艺人名"
        AppLanguage.Czech -> "Jméno umělce"
        AppLanguage.French -> "Nom de l’artiste"
        AppLanguage.German -> "Künstlername"
        AppLanguage.Italian -> "Nome artista"
        AppLanguage.Japanese -> "アーティスト名"
        AppLanguage.Spanish -> "Nombre de artista"
        AppLanguage.Portuguese -> "Nome do artista"
        AppLanguage.Russian -> "Имя исполнителя"
        AppLanguage.Ukrainian -> "Ім’я виконавця"
        AppLanguage.Slovak -> "Meno interpreta"
        AppLanguage.Korean -> "아티스트 이름"
        AppLanguage.Malay -> "Nama artis"
        AppLanguage.Bengali -> "শিল্পীর নাম"
        AppLanguage.Urdu -> "آرٹسٹ کا نام"
        else -> "Artist name"
    }
}


internal data class SettingsLanguageCopy(
    val settings: String,
    val appearance: String,
    val theme: String,
    val textSize: String,
    val language: String,
    val currentlyUsed: String,
    val sound: String,
    val bassBoost: String,
    val spaciousness: String,
    val equalizer: String,
    val enableMono: String,
    val monoSubtitle: String,
    val otherSettings: String,
    val scanLibrary: String,
    val scanLibrarySubtitle: String,
    val scan: String,
    val checkUpdates: String,
    val checkUpdatesSubtitle: String,
    val check: String,
    val changelog: String,
    val footerSubtitle: String,
)

internal fun settingsCopy(language: AppLanguage): SettingsLanguageCopy = when (language) {
    AppLanguage.Polish -> SettingsLanguageCopy("Ustawienia", "Wygląd", "Motyw", "Rozmiar tekstu", "Język", "Obecnie używany: ${language.nativeName}", "Dźwięk", "Podbicie basu", "Przestrzenność", "Korektor", "Włącz mono", "Przełącza odtwarzanie stereo na mono", "Inne ustawienia", "Skanuj bibliotekę", "Odśwież indeksowanie w poszukiwaniu nowych multimediów", "Skanuj", "Sprawdź aktualizacje", "Sprawdź, czy jest dostępna nowa wersja", "Sprawdź", "Lista zmian", "Zaprojektowane z pasją do muzyki i świetnego designu")
    AppLanguage.ChineseSimplified -> SettingsLanguageCopy("设置", "外观", "主题", "文字大小", "语言", "当前使用：${language.nativeName}", "声音", "低音增强", "空间感", "均衡器", "启用单声道", "将立体声播放切换为单声道", "其他设置", "扫描媒体库", "刷新索引以查找新媒体", "扫描", "检查更新", "检查是否有新版本可用", "检查", "更新日志", "为音乐和优秀设计倾注热情")
    AppLanguage.Czech -> SettingsLanguageCopy("Nastavení", "Vzhled", "Motiv", "Velikost textu", "Jazyk", "Aktuálně používaný: ${language.nativeName}", "Zvuk", "Zesílení basů", "Prostorovost", "Ekvalizér", "Zapnout mono", "Přepne stereo přehrávání na mono", "Další nastavení", "Skenovat knihovnu", "Obnoví index pro nová média", "Skenovat", "Zkontrolovat aktualizace", "Zjistit, zda je k dispozici nová verze", "Zkontrolovat", "Změny", "Navrženo s vášní pro hudbu a skvělý design")
    AppLanguage.Lithuanian -> SettingsLanguageCopy("Nustatymai", "Išvaizda", "Tema", "Teksto dydis", "Kalba", "Šiuo metu naudojama: ${language.nativeName}", "Garsas", "Bosų stiprinimas", "Erdviškumas", "Ekvalaizeris", "Įjungti mono", "Perjungia stereo atkūrimą į mono", "Kiti nustatymai", "Skenuoti biblioteką", "Atnaujina indeksą ieškant naujos medijos", "Skenuoti", "Tikrinti naujinimus", "Patikrina, ar yra nauja versija", "Tikrinti", "Pakeitimai", "Sukurta su aistra muzikai ir puikiam dizainui")
    AppLanguage.Danish -> SettingsLanguageCopy("Indstillinger", "Udseende", "Tema", "Tekststørrelse", "Sprog", "Aktuelt brugt: ${language.nativeName}", "Lyd", "Basboost", "Rumlighed", "Equalizer", "Aktivér mono", "Skifter stereoafspilning til mono", "Andre indstillinger", "Scan bibliotek", "Opdater indeksering efter nye medier", "Scan", "Søg efter opdateringer", "Tjek om en ny version er tilgængelig", "Tjek", "Ændringslog", "Designet med passion for musik og godt design")
    AppLanguage.French -> SettingsLanguageCopy("Réglages", "Apparence", "Thème", "Taille du texte", "Langue", "Actuellement utilisé : ${language.nativeName}", "Son", "Renfort des basses", "Spatialisation", "Égaliseur", "Activer mono", "Passe la lecture stéréo en mono", "Autres réglages", "Analyser la bibliothèque", "Actualise l’index pour trouver de nouveaux médias", "Analyser", "Rechercher des mises à jour", "Vérifie si une nouvelle version est disponible", "Vérifier", "Nouveautés", "Conçu avec passion pour la musique et le beau design")
    AppLanguage.German -> SettingsLanguageCopy("Einstellungen", "Darstellung", "Design", "Textgröße", "Sprache", "Aktuell verwendet: ${language.nativeName}", "Klang", "Bassverstärkung", "Räumlichkeit", "Equalizer", "Mono aktivieren", "Schaltet Stereo-Wiedergabe auf Mono", "Weitere Einstellungen", "Bibliothek scannen", "Aktualisiert den Index für neue Medien", "Scannen", "Nach Updates suchen", "Prüft, ob eine neue Version verfügbar ist", "Prüfen", "Änderungen", "Mit Leidenschaft für Musik und gutes Design gestaltet")
    AppLanguage.Dutch -> SettingsLanguageCopy("Instellingen", "Weergave", "Thema", "Tekstgrootte", "Taal", "Momenteel gebruikt: ${language.nativeName}", "Geluid", "Basversterking", "Ruimtelijkheid", "Equalizer", "Mono inschakelen", "Schakelt stereo afspelen om naar mono", "Andere instellingen", "Bibliotheek scannen", "Vernieuwt indexering voor nieuwe media", "Scannen", "Controleren op updates", "Controleert of er een nieuwe versie beschikbaar is", "Controleren", "Wijzigingen", "Ontworpen met passie voor muziek en sterk design")
    AppLanguage.Norwegian -> SettingsLanguageCopy("Innstillinger", "Utseende", "Tema", "Tekststørrelse", "Språk", "Brukes nå: ${language.nativeName}", "Lyd", "Bassforsterkning", "Romfølelse", "Equalizer", "Aktiver mono", "Bytter stereoavspilling til mono", "Andre innstillinger", "Skann bibliotek", "Oppdaterer indeksen for nye medier", "Skann", "Se etter oppdateringer", "Sjekker om en ny versjon er tilgjengelig", "Sjekk", "Endringslogg", "Designet med lidenskap for musikk og flott design")
    AppLanguage.Swedish -> SettingsLanguageCopy("Inställningar", "Utseende", "Tema", "Textstorlek", "Språk", "Används nu: ${language.nativeName}", "Ljud", "Basförstärkning", "Rymd", "Equalizer", "Aktivera mono", "Växlar stereouppspelning till mono", "Andra inställningar", "Skanna bibliotek", "Uppdaterar indexering för ny media", "Skanna", "Sök efter uppdateringar", "Kontrollerar om en ny version finns", "Sök", "Ändringslogg", "Designad med passion för musik och bra design")
    AppLanguage.Spanish -> SettingsLanguageCopy("Ajustes", "Apariencia", "Tema", "Tamaño de texto", "Idioma", "Usado actualmente: ${language.nativeName}", "Sonido", "Refuerzo de graves", "Espacialidad", "Ecualizador", "Activar mono", "Cambia la reproducción estéreo a mono", "Otros ajustes", "Escanear biblioteca", "Actualiza la indexación para buscar nuevos medios", "Escanear", "Buscar actualizaciones", "Comprueba si hay una nueva versión disponible", "Buscar", "Cambios", "Diseñado con pasión por la música y el buen diseño")
    AppLanguage.Portuguese -> SettingsLanguageCopy("Definições", "Aparência", "Tema", "Tamanho do texto", "Idioma", "Atualmente usado: ${language.nativeName}", "Som", "Reforço de graves", "Espacialidade", "Equalizador", "Ativar mono", "Muda a reprodução estéreo para mono", "Outras definições", "Analisar biblioteca", "Atualiza a indexação para novos ficheiros", "Analisar", "Procurar atualizações", "Verifica se há nova versão disponível", "Verificar", "Novidades", "Criado com paixão por música e bom design")
    AppLanguage.Estonian -> SettingsLanguageCopy("Seaded", "Välimus", "Teema", "Teksti suurus", "Keel", "Praegu kasutusel: ${language.nativeName}", "Heli", "Bassi võimendus", "Ruumilisus", "Ekvalaiser", "Luba mono", "Lülitab stereo taasesituse monoks", "Muud seaded", "Skanni teeki", "Värskendab indeksit uue meedia leidmiseks", "Skanni", "Kontrolli uuendusi", "Kontrollib, kas uus versioon on saadaval", "Kontrolli", "Muudatused", "Loodud kirega muusika ja hea disaini vastu")
    AppLanguage.Greek -> SettingsLanguageCopy("Ρυθμίσεις", "Εμφάνιση", "Θέμα", "Μέγεθος κειμένου", "Γλώσσα", "Χρησιμοποιείται τώρα: ${language.nativeName}", "Ήχος", "Ενίσχυση μπάσων", "Χωρικότητα", "Ισοσταθμιστής", "Ενεργοποίηση μονοφωνικού", "Αλλάζει την αναπαραγωγή stereo σε mono", "Άλλες ρυθμίσεις", "Σάρωση βιβλιοθήκης", "Ανανεώνει το ευρετήριο για νέα πολυμέσα", "Σάρωση", "Έλεγχος ενημερώσεων", "Ελέγχει αν υπάρχει νέα έκδοση", "Έλεγχος", "Αλλαγές", "Σχεδιασμένο με πάθος για μουσική και όμορφο design")
    AppLanguage.Croatian -> SettingsLanguageCopy("Postavke", "Izgled", "Tema", "Veličina teksta", "Jezik", "Trenutno se koristi: ${language.nativeName}", "Zvuk", "Pojačanje basa", "Prostornost", "Ekvilizator", "Uključi mono", "Prebacuje stereo reprodukciju u mono", "Ostale postavke", "Skeniraj biblioteku", "Osvježava indeks za nove medije", "Skeniraj", "Provjeri ažuriranja", "Provjerava postoji li nova verzija", "Provjeri", "Promjene", "Dizajnirano sa strašću za glazbu i dobar dizajn")
    AppLanguage.Russian -> SettingsLanguageCopy("Настройки", "Внешний вид", "Тема", "Размер текста", "Язык", "Сейчас используется: ${language.nativeName}", "Звук", "Усиление баса", "Пространственность", "Эквалайзер", "Включить моно", "Переключает стерео воспроизведение в моно", "Другие настройки", "Сканировать библиотеку", "Обновляет индекс для поиска новых медиа", "Сканировать", "Проверить обновления", "Проверяет, доступна ли новая версия", "Проверить", "Список изменений", "Создано с любовью к музыке и хорошему дизайну")
    AppLanguage.Ukrainian -> SettingsLanguageCopy("Налаштування", "Вигляд", "Тема", "Розмір тексту", "Мова", "Зараз використовується: ${language.nativeName}", "Звук", "Підсилення басів", "Просторовість", "Еквалайзер", "Увімкнути моно", "Перемикає стереовідтворення на моно", "Інші налаштування", "Сканувати бібліотеку", "Оновлює індекс для нових медіа", "Сканувати", "Перевірити оновлення", "Перевіряє, чи доступна нова версія", "Перевірити", "Зміни", "Створено з любов’ю до музики та гарного дизайну")
    AppLanguage.Latvian -> SettingsLanguageCopy("Iestatījumi", "Izskats", "Tēma", "Teksta izmērs", "Valoda", "Pašlaik lietota: ${language.nativeName}", "Skaņa", "Basa pastiprinājums", "Telpiskums", "Ekvalaizers", "Ieslēgt mono", "Pārslēdz stereo atskaņošanu uz mono", "Citi iestatījumi", "Skenēt bibliotēku", "Atjauno indeksu jauniem multivides failiem", "Skenēt", "Meklēt atjauninājumus", "Pārbauda, vai pieejama jauna versija", "Pārbaudīt", "Izmaiņas", "Radīts ar aizrautību pret mūziku un lielisku dizainu")
    AppLanguage.Italian -> SettingsLanguageCopy("Impostazioni", "Aspetto", "Tema", "Dimensione testo", "Lingua", "Attualmente in uso: ${language.nativeName}", "Suono", "Potenziamento bassi", "Spazialità", "Equalizzatore", "Attiva mono", "Passa la riproduzione stereo a mono", "Altre impostazioni", "Scansiona libreria", "Aggiorna l’indice per nuovi media", "Scansiona", "Cerca aggiornamenti", "Controlla se è disponibile una nuova versione", "Controlla", "Novità", "Progettato con passione per la musica e il buon design")
    AppLanguage.Japanese -> SettingsLanguageCopy("設定", "外観", "テーマ", "文字サイズ", "言語", "現在使用中: ${language.nativeName}", "サウンド", "低音ブースト", "空間感", "イコライザー", "モノラルを有効化", "ステレオ再生をモノラルに切り替えます", "その他の設定", "ライブラリをスキャン", "新しいメディアを探すためにインデックスを更新します", "スキャン", "アップデートを確認", "新しいバージョンが利用可能か確認します", "確認", "更新履歴", "音楽への情熱と優れたデザインで作られています")
    AppLanguage.Albanian -> SettingsLanguageCopy("Cilësimet", "Pamja", "Tema", "Madhësia e tekstit", "Gjuha", "Aktualisht në përdorim: ${language.nativeName}", "Tingulli", "Përforcim basi", "Hapësirë", "Ekualizuesi", "Aktivizo mono", "E kalon riprodhimin stereo në mono", "Cilësime të tjera", "Skano bibliotekën", "Rifreskon indeksimin për media të reja", "Skano", "Kontrollo për përditësime", "Kontrollon nëse ka version të ri", "Kontrollo", "Ndryshimet", "Dizajnuar me pasion për muzikën dhe dizajnin e mirë")
    AppLanguage.Hindi -> SettingsLanguageCopy("सेटिंग्स", "दिखावट", "थीम", "टेक्स्ट आकार", "भाषा", "वर्तमान में उपयोग: ${language.nativeName}", "ध्वनि", "बास बूस्ट", "स्पेशियसनेस", "इक्वलाइज़र", "मोनो चालू करें", "स्टीरियो प्लेबैक को मोनो में बदलता है", "अन्य सेटिंग्स", "लाइब्रेरी स्कैन करें", "नई मीडिया के लिए इंडेक्स ताज़ा करें", "स्कैन", "अपडेट जांचें", "नया संस्करण उपलब्ध है या नहीं जांचें", "जांचें", "बदलाव", "संगीत और अच्छे डिज़ाइन के प्रति जुनून से बनाया गया")
    AppLanguage.Hungarian -> SettingsLanguageCopy("Beállítások", "Megjelenés", "Téma", "Szövegméret", "Nyelv", "Jelenleg használt: ${language.nativeName}", "Hang", "Basszuskiemelés", "Térhatás", "Hangszínszabályzó", "Monó engedélyezése", "A sztereó lejátszást monóra váltja", "Egyéb beállítások", "Könyvtár beolvasása", "Frissíti az indexelést új médiához", "Beolvasás", "Frissítések keresése", "Ellenőrzi, hogy elérhető-e új verzió", "Ellenőrzés", "Változások", "Szenvedéllyel tervezve zenéhez és jó designhoz")
    AppLanguage.Latin -> SettingsLanguageCopy("Optiones", "Aspectus", "Thema", "Magnitudo textus", "Lingua", "Nunc adhibetur: ${language.nativeName}", "Sonus", "Bassus auctus", "Spatium", "Aequator", "Mono activa", "Playback stereo in mono vertit", "Aliae optiones", "Bibliothecam scrutare", "Indicem pro novis mediis renovat", "Scrutare", "Renovationes inspice", "Inspicit an nova versio praesto sit", "Inspice", "Mutationes", "Studio musicae et bono consilio creatum")
    AppLanguage.Macedonian -> SettingsLanguageCopy("Поставки", "Изглед", "Тема", "Големина на текст", "Јазик", "Моментално се користи: ${language.nativeName}", "Звук", "Засилување на бас", "Просторност", "Еквилајзер", "Вклучи моно", "Ја префрла стерео репродукцијата во моно", "Други поставки", "Скенирај библиотека", "Го освежува индексирањето за нови медиуми", "Скенирај", "Провери ажурирања", "Проверува дали има нова верзија", "Провери", "Промени", "Создадено со страст за музика и добар дизајн")
    AppLanguage.Serbian -> SettingsLanguageCopy("Подешавања", "Изглед", "Тема", "Величина текста", "Језик", "Тренутно се користи: ${language.nativeName}", "Звук", "Појачање баса", "Просторност", "Еквилајзер", "Укључи моно", "Пребацује стерео репродукцију у моно", "Остала подешавања", "Скенирај библиотеку", "Освежава индексирање за нове медије", "Скенирај", "Провери ажурирања", "Проверава да ли је доступна нова верзија", "Провери", "Промене", "Дизајнирано са страшћу за музику и добар дизајн")
    AppLanguage.Thai -> SettingsLanguageCopy("การตั้งค่า", "รูปลักษณ์", "ธีม", "ขนาดข้อความ", "ภาษา", "ใช้อยู่: ${language.nativeName}", "เสียง", "เพิ่มเสียงเบส", "มิติเสียง", "อีควอไลเซอร์", "เปิดโมโน", "เปลี่ยนการเล่นสเตอริโอเป็นโมโน", "การตั้งค่าอื่น", "สแกนคลังเพลง", "รีเฟรชดัชนีเพื่อค้นหาสื่อใหม่", "สแกน", "ตรวจสอบอัปเดต", "ตรวจสอบว่ามีเวอร์ชันใหม่หรือไม่", "ตรวจสอบ", "บันทึกการเปลี่ยนแปลง", "ออกแบบด้วยความหลงใหลในดนตรีและดีไซน์ที่ดี")
    AppLanguage.Slovak -> SettingsLanguageCopy("Nastavenia", "Vzhľad", "Téma", "Veľkosť textu", "Jazyk", "Momentálne používaný: ${language.nativeName}", "Zvuk", "Zosilnenie basov", "Priestorovosť", "Ekvalizér", "Zapnúť mono", "Prepne stereo prehrávanie na mono", "Ďalšie nastavenia", "Skenovať knižnicu", "Obnoví index pre nové médiá", "Skenovať", "Skontrolovať aktualizácie", "Skontroluje, či je dostupná nová verzia", "Skontrolovať", "Zmeny", "Vytvorené s vášňou pre hudbu a skvelý dizajn")
    AppLanguage.Korean -> SettingsLanguageCopy("설정", "모양", "테마", "텍스트 크기", "언어", "현재 사용 중: ${language.nativeName}", "사운드", "저음 강화", "공간감", "이퀄라이저", "모노 사용", "스테레오 재생을 모노로 전환합니다", "기타 설정", "라이브러리 스캔", "새 미디어를 찾기 위해 인덱스를 새로 고칩니다", "스캔", "업데이트 확인", "새 버전이 있는지 확인합니다", "확인", "변경 사항", "음악과 좋은 디자인에 대한 열정으로 만들어졌습니다")
    AppLanguage.Malay -> SettingsLanguageCopy("Tetapan", "Penampilan", "Tema", "Saiz teks", "Bahasa", "Sedang digunakan: ${language.nativeName}", "Bunyi", "Penguat bass", "Keluasan", "Penyama", "Dayakan mono", "Menukar main balik stereo kepada mono", "Tetapan lain", "Imbas pustaka", "Segarkan pengindeksan untuk media baharu", "Imbas", "Semak kemas kini", "Semak sama ada versi baharu tersedia", "Semak", "Log perubahan", "Direka dengan semangat terhadap muzik dan reka bentuk yang hebat")
    AppLanguage.Bengali -> SettingsLanguageCopy("সেটিংস", "চেহারা", "থিম", "টেক্সটের আকার", "ভাষা", "বর্তমানে ব্যবহৃত: ${language.nativeName}", "শব্দ", "বেস বুস্ট", "স্পেসিয়াসনেস", "ইকুয়ালাইজার", "মোনো চালু করুন", "স্টেরিও প্লেব্যাককে মোনোতে বদলে দেয়", "অন্যান্য সেটিংস", "লাইব্রেরি স্ক্যান করুন", "নতুন মিডিয়ার জন্য ইনডেক্স রিফ্রেশ করে", "স্ক্যান", "আপডেট পরীক্ষা করুন", "নতুন সংস্করণ উপলভ্য কি না পরীক্ষা করে", "পরীক্ষা করুন", "পরিবর্তনপঞ্জি", "সঙ্গীত ও দারুণ ডিজাইনের প্রতি ভালবাসা দিয়ে নির্মিত")
    AppLanguage.Urdu -> SettingsLanguageCopy("سیٹنگز", "ظاہری شکل", "تھیم", "متن کا سائز", "زبان", "فی الحال استعمال میں: ${language.nativeName}", "آواز", "باس بوسٹ", "کشادگی", "ایکوالائزر", "مونو فعال کریں", "اسٹیریو پلے بیک کو مونو میں بدلتا ہے", "دیگر سیٹنگز", "لائبریری اسکین کریں", "نئے میڈیا کے لیے انڈیکس تازہ کرتا ہے", "اسکین", "اپ ڈیٹس چیک کریں", "چیک کریں کہ نئی ورژن دستیاب ہے یا نہیں", "چیک کریں", "تبدیلیاں", "موسیقی اور عمدہ ڈیزائن کے شوق سے تیار کیا گیا")
    AppLanguage.English -> SettingsLanguageCopy("Settings", "Appearance", "Theme", "Text size", "Language", "Currently used: ${language.nativeName}", "Sound", "Bass boost", "Spaciousness", "Equalizer", "Enable mono", "Switches stereo playback to mono", "Other settings", "Scan library", "Refresh indexing in search for new media", "Scan", "Check for updates", "Check if there's new version available", "Check", "Changelog", "Designed with passion for music and great design")
}

internal data class LibraryFoldersCopy(
    val title: String,
    val subtitle: String,
    val addFolder: String,
    val edit: String,
    val done: String,
    val refresh: String,
    val noFoldersTitle: String,
    val noFoldersMessage: String,
    val unavailable: String,
    val unavailableSubtitle: String,
    val removalSafety: String,
)

internal fun libraryFoldersCopy(language: AppLanguage): LibraryFoldersCopy = LibraryFoldersCopy(
    title = "Library folders",
    subtitle = "Choose folders scanned for music",
    addFolder = "Add folder",
    edit = "Edit",
    done = "Done",
    refresh = "Refresh",
    noFoldersTitle = "No library folders selected",
    noFoldersMessage = "Add a folder to make music appear in your library.",
    unavailable = "Unavailable",
    unavailableSubtitle = "Folder access is unavailable",
    removalSafety = "Removing a folder only removes it from Elovaire's library scan. Your audio files stay on your device.",
)

internal enum class UiPhrase {
    About,
    AddToPlaylist,
    AddToQueue,
    GoToAlbum,
    DeleteFromLibrary,
    DeleteAlbum,
    Delete,
    Rename,
    RemoveFromList,
    NewPlaylist,
    Cancel,
    Create,
    Reset,
    Dry,
    Wet,
    Off,
    Reverb,
    ToneShaping,
    Bass,
    Midrange,
    Treble,
    EffectStrength,
}

internal fun uiPhrase(language: AppLanguage, phrase: UiPhrase): String {
    return uiPhraseTranslations[language]?.get(phrase) ?: uiPhraseTranslations.getValue(AppLanguage.English).getValue(phrase)
}

private val uiPhraseTranslations = mapOf(
    AppLanguage.English to mapOf(
        UiPhrase.About to "About",
        UiPhrase.AddToPlaylist to "Add to playlist",
        UiPhrase.AddToQueue to "Add to queue",
        UiPhrase.GoToAlbum to "Go to album",
        UiPhrase.DeleteFromLibrary to "Delete from library",
        UiPhrase.DeleteAlbum to "Delete album",
        UiPhrase.Delete to "Delete",
        UiPhrase.Rename to "Rename",
        UiPhrase.RemoveFromList to "Remove from list",
        UiPhrase.NewPlaylist to "New playlist",
        UiPhrase.Cancel to "Cancel",
        UiPhrase.Create to "Create",
        UiPhrase.Reset to "Reset",
        UiPhrase.Dry to "Dry",
        UiPhrase.Wet to "Wet",
        UiPhrase.Off to "Off",
        UiPhrase.Reverb to "Reverb",
        UiPhrase.ToneShaping to "Tonal balance",
        UiPhrase.Bass to "Bass",
        UiPhrase.Midrange to "Midrange",
        UiPhrase.Treble to "Treble",
        UiPhrase.EffectStrength to "Effect strength",
    ),
    AppLanguage.Polish to mapOf(UiPhrase.About to "O aplikacji", UiPhrase.AddToPlaylist to "Dodaj do playlisty", UiPhrase.AddToQueue to "Dodaj do kolejki", UiPhrase.GoToAlbum to "Przejdź do albumu", UiPhrase.DeleteFromLibrary to "Usuń z biblioteki", UiPhrase.DeleteAlbum to "Usuń album", UiPhrase.Delete to "Usuń", UiPhrase.Rename to "Zmień nazwę", UiPhrase.RemoveFromList to "Usuń z listy", UiPhrase.NewPlaylist to "Nowa playlista", UiPhrase.Cancel to "Anuluj", UiPhrase.Create to "Utwórz", UiPhrase.Reset to "Resetuj", UiPhrase.Dry to "Suchy", UiPhrase.Wet to "Mokry", UiPhrase.Off to "Wyłączone", UiPhrase.Reverb to "Pogłos", UiPhrase.ToneShaping to "Balans tonalny", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Środek", UiPhrase.Treble to "Góra", UiPhrase.EffectStrength to "Siła efektu"),
    AppLanguage.Albanian to mapOf(UiPhrase.About to "Rreth", UiPhrase.AddToPlaylist to "Shto në listë", UiPhrase.AddToQueue to "Shto në radhë", UiPhrase.GoToAlbum to "Shko te albumi", UiPhrase.DeleteFromLibrary to "Fshi nga biblioteka", UiPhrase.DeleteAlbum to "Fshi albumin", UiPhrase.Delete to "Fshi", UiPhrase.Rename to "Riemërto", UiPhrase.RemoveFromList to "Hiq nga lista", UiPhrase.NewPlaylist to "Listë e re", UiPhrase.Cancel to "Anulo", UiPhrase.Create to "Krijo", UiPhrase.Reset to "Rivendos", UiPhrase.Dry to "I thatë", UiPhrase.Wet to "I lagësht", UiPhrase.Off to "Fikur", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Formësim toni", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mesatare", UiPhrase.Treble to "Të larta", UiPhrase.EffectStrength to "Fuqia e efektit"),
    AppLanguage.ChineseSimplified to mapOf(UiPhrase.About to "关于", UiPhrase.AddToPlaylist to "添加到播放列表", UiPhrase.AddToQueue to "添加到队列", UiPhrase.GoToAlbum to "前往专辑", UiPhrase.DeleteFromLibrary to "从媒体库删除", UiPhrase.DeleteAlbum to "删除专辑", UiPhrase.Delete to "删除", UiPhrase.Rename to "重命名", UiPhrase.RemoveFromList to "从列表移除", UiPhrase.NewPlaylist to "新建播放列表", UiPhrase.Cancel to "取消", UiPhrase.Create to "创建", UiPhrase.Reset to "重置", UiPhrase.Dry to "干声", UiPhrase.Wet to "湿声", UiPhrase.Off to "关闭", UiPhrase.Reverb to "混响", UiPhrase.ToneShaping to "音色塑形", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中频", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "效果强度"),
    AppLanguage.Croatian to mapOf(UiPhrase.About to "O aplikaciji", UiPhrase.AddToPlaylist to "Dodaj na popis", UiPhrase.AddToQueue to "Dodaj u red", UiPhrase.GoToAlbum to "Idi na album", UiPhrase.DeleteFromLibrary to "Izbriši iz biblioteke", UiPhrase.DeleteAlbum to "Izbriši album", UiPhrase.Delete to "Izbriši", UiPhrase.Rename to "Preimenuj", UiPhrase.RemoveFromList to "Ukloni s popisa", UiPhrase.NewPlaylist to "Novi popis", UiPhrase.Cancel to "Odustani", UiPhrase.Create to "Stvori", UiPhrase.Reset to "Resetiraj", UiPhrase.Dry to "Suho", UiPhrase.Wet to "Mokro", UiPhrase.Off to "Isključeno", UiPhrase.Reverb to "Odjek", UiPhrase.ToneShaping to "Oblikovanje tona", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Srednji", UiPhrase.Treble to "Visoki", UiPhrase.EffectStrength to "Jačina efekta"),
    AppLanguage.Slovak to mapOf(UiPhrase.About to "O aplikácii", UiPhrase.AddToPlaylist to "Pridať do playlistu", UiPhrase.AddToQueue to "Pridať do frontu", UiPhrase.GoToAlbum to "Prejsť na album", UiPhrase.DeleteFromLibrary to "Odstrániť z knižnice", UiPhrase.DeleteAlbum to "Odstrániť album", UiPhrase.Delete to "Odstrániť", UiPhrase.Rename to "Premenovať", UiPhrase.RemoveFromList to "Odstrániť zo zoznamu", UiPhrase.NewPlaylist to "Nový playlist", UiPhrase.Cancel to "Zrušiť", UiPhrase.Create to "Vytvoriť", UiPhrase.Reset to "Resetovať", UiPhrase.Dry to "Suchý", UiPhrase.Wet to "Mokrý", UiPhrase.Off to "Vypnuté", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Tónové vyváženie", UiPhrase.Bass to "Basy", UiPhrase.Midrange to "Stredy", UiPhrase.Treble to "Výšky", UiPhrase.EffectStrength to "Sila efektu"),
    AppLanguage.Korean to mapOf(UiPhrase.About to "정보", UiPhrase.AddToPlaylist to "플레이리스트에 추가", UiPhrase.AddToQueue to "대기열에 추가", UiPhrase.GoToAlbum to "앨범으로 이동", UiPhrase.DeleteFromLibrary to "라이브러리에서 삭제", UiPhrase.DeleteAlbum to "앨범 삭제", UiPhrase.Delete to "삭제", UiPhrase.Rename to "이름 변경", UiPhrase.RemoveFromList to "목록에서 제거", UiPhrase.NewPlaylist to "새 플레이리스트", UiPhrase.Cancel to "취소", UiPhrase.Create to "만들기", UiPhrase.Reset to "재설정", UiPhrase.Dry to "드라이", UiPhrase.Wet to "웻", UiPhrase.Off to "끔", UiPhrase.Reverb to "리버브", UiPhrase.ToneShaping to "톤 밸런스", UiPhrase.Bass to "저음", UiPhrase.Midrange to "중역", UiPhrase.Treble to "고역", UiPhrase.EffectStrength to "효과 강도"),
    AppLanguage.Malay to mapOf(UiPhrase.About to "Perihal", UiPhrase.AddToPlaylist to "Tambah ke senarai main", UiPhrase.AddToQueue to "Tambah ke barisan", UiPhrase.GoToAlbum to "Pergi ke album", UiPhrase.DeleteFromLibrary to "Padam daripada pustaka", UiPhrase.DeleteAlbum to "Padam album", UiPhrase.Delete to "Padam", UiPhrase.Rename to "Namakan semula", UiPhrase.RemoveFromList to "Buang daripada senarai", UiPhrase.NewPlaylist to "Senarai main baharu", UiPhrase.Cancel to "Batal", UiPhrase.Create to "Cipta", UiPhrase.Reset to "Tetapkan semula", UiPhrase.Dry to "Kering", UiPhrase.Wet to "Basah", UiPhrase.Off to "Mati", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Imbangan tonal", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Pertengahan", UiPhrase.Treble to "Trebel", UiPhrase.EffectStrength to "Kekuatan kesan"),
    AppLanguage.Bengali to mapOf(UiPhrase.About to "অ্যাপ সম্পর্কে", UiPhrase.AddToPlaylist to "প্লেলিস্টে যোগ করুন", UiPhrase.AddToQueue to "কিউতে যোগ করুন", UiPhrase.GoToAlbum to "অ্যালবামে যান", UiPhrase.DeleteFromLibrary to "লাইব্রেরি থেকে মুছুন", UiPhrase.DeleteAlbum to "অ্যালবাম মুছুন", UiPhrase.Delete to "মুছুন", UiPhrase.Rename to "নাম বদলান", UiPhrase.RemoveFromList to "তালিকা থেকে সরান", UiPhrase.NewPlaylist to "নতুন প্লেলিস্ট", UiPhrase.Cancel to "বাতিল", UiPhrase.Create to "তৈরি করুন", UiPhrase.Reset to "রিসেট", UiPhrase.Dry to "ড্রাই", UiPhrase.Wet to "ওয়েট", UiPhrase.Off to "বন্ধ", UiPhrase.Reverb to "রিভার্ব", UiPhrase.ToneShaping to "টোনাল ব্যালান্স", UiPhrase.Bass to "বেস", UiPhrase.Midrange to "মিডরেঞ্জ", UiPhrase.Treble to "ট্রেবল", UiPhrase.EffectStrength to "ইফেক্টের শক্তি"),
    AppLanguage.Urdu to mapOf(UiPhrase.About to "ایپ کے بارے میں", UiPhrase.AddToPlaylist to "پلے لسٹ میں شامل کریں", UiPhrase.AddToQueue to "قطار میں شامل کریں", UiPhrase.GoToAlbum to "البم پر جائیں", UiPhrase.DeleteFromLibrary to "لائبریری سے حذف کریں", UiPhrase.DeleteAlbum to "البم حذف کریں", UiPhrase.Delete to "حذف کریں", UiPhrase.Rename to "نام تبدیل کریں", UiPhrase.RemoveFromList to "فہرست سے ہٹائیں", UiPhrase.NewPlaylist to "نئی پلے لسٹ", UiPhrase.Cancel to "منسوخ", UiPhrase.Create to "بنائیں", UiPhrase.Reset to "ری سیٹ", UiPhrase.Dry to "خشک", UiPhrase.Wet to "گیلا", UiPhrase.Off to "بند", UiPhrase.Reverb to "ریورب", UiPhrase.ToneShaping to "ٹونل بیلنس", UiPhrase.Bass to "باس", UiPhrase.Midrange to "درمیانی", UiPhrase.Treble to "ٹرےبل", UiPhrase.EffectStrength to "اثر کی طاقت"),
    AppLanguage.Czech to mapOf(UiPhrase.About to "O aplikaci", UiPhrase.AddToPlaylist to "Přidat do playlistu", UiPhrase.AddToQueue to "Přidat do fronty", UiPhrase.GoToAlbum to "Přejít na album", UiPhrase.DeleteFromLibrary to "Smazat z knihovny", UiPhrase.DeleteAlbum to "Smazat album", UiPhrase.Delete to "Smazat", UiPhrase.Rename to "Přejmenovat", UiPhrase.RemoveFromList to "Odebrat ze seznamu", UiPhrase.NewPlaylist to "Nový playlist", UiPhrase.Cancel to "Zrušit", UiPhrase.Create to "Vytvořit", UiPhrase.Reset to "Resetovat", UiPhrase.Dry to "Suchý", UiPhrase.Wet to "Mokrý", UiPhrase.Off to "Vypnuto", UiPhrase.Reverb to "Dozvuk", UiPhrase.ToneShaping to "Tvarování tónu", UiPhrase.Bass to "Basy", UiPhrase.Midrange to "Středy", UiPhrase.Treble to "Výšky", UiPhrase.EffectStrength to "Síla efektu"),
    AppLanguage.Danish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Føj til playliste", UiPhrase.AddToQueue to "Føj til kø", UiPhrase.GoToAlbum to "Gå til album", UiPhrase.DeleteFromLibrary to "Slet fra bibliotek", UiPhrase.DeleteAlbum to "Slet album", UiPhrase.Delete to "Slet", UiPhrase.Rename to "Omdøb", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny playliste", UiPhrase.Cancel to "Annuller", UiPhrase.Create to "Opret", UiPhrase.Reset to "Nulstil", UiPhrase.Dry to "Tør", UiPhrase.Wet to "Våd", UiPhrase.Off to "Fra", UiPhrase.Reverb to "Rumklang", UiPhrase.ToneShaping to "Toneformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellemtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Dutch to mapOf(UiPhrase.About to "Over", UiPhrase.AddToPlaylist to "Toevoegen aan afspeellijst", UiPhrase.AddToQueue to "Toevoegen aan wachtrij", UiPhrase.GoToAlbum to "Ga naar album", UiPhrase.DeleteFromLibrary to "Verwijderen uit bibliotheek", UiPhrase.DeleteAlbum to "Album verwijderen", UiPhrase.Delete to "Verwijderen", UiPhrase.Rename to "Naam wijzigen", UiPhrase.RemoveFromList to "Uit lijst verwijderen", UiPhrase.NewPlaylist to "Nieuwe afspeellijst", UiPhrase.Cancel to "Annuleren", UiPhrase.Create to "Maken", UiPhrase.Reset to "Resetten", UiPhrase.Dry to "Droog", UiPhrase.Wet to "Nat", UiPhrase.Off to "Uit", UiPhrase.Reverb to "Galm", UiPhrase.ToneShaping to "Toonvorming", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Midden", UiPhrase.Treble to "Hoge tonen", UiPhrase.EffectStrength to "Effectsterkte"),
    AppLanguage.Estonian to mapOf(UiPhrase.About to "Teave", UiPhrase.AddToPlaylist to "Lisa esitusloendisse", UiPhrase.AddToQueue to "Lisa järjekorda", UiPhrase.GoToAlbum to "Ava album", UiPhrase.DeleteFromLibrary to "Kustuta teegist", UiPhrase.DeleteAlbum to "Kustuta album", UiPhrase.Delete to "Kustuta", UiPhrase.Rename to "Nimeta ümber", UiPhrase.RemoveFromList to "Eemalda loendist", UiPhrase.NewPlaylist to "Uus esitusloend", UiPhrase.Cancel to "Tühista", UiPhrase.Create to "Loo", UiPhrase.Reset to "Lähtesta", UiPhrase.Dry to "Kuiv", UiPhrase.Wet to "Märg", UiPhrase.Off to "Väljas", UiPhrase.Reverb to "Kaja", UiPhrase.ToneShaping to "Tooni kujundus", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Keskvahemik", UiPhrase.Treble to "Kõrged", UiPhrase.EffectStrength to "Efekti tugevus"),
    AppLanguage.French to mapOf(UiPhrase.About to "À propos", UiPhrase.AddToPlaylist to "Ajouter à une playlist", UiPhrase.AddToQueue to "Ajouter à la file", UiPhrase.GoToAlbum to "Aller à l’album", UiPhrase.DeleteFromLibrary to "Supprimer de la bibliothèque", UiPhrase.DeleteAlbum to "Supprimer l’album", UiPhrase.Delete to "Supprimer", UiPhrase.Rename to "Renommer", UiPhrase.RemoveFromList to "Retirer de la liste", UiPhrase.NewPlaylist to "Nouvelle playlist", UiPhrase.Cancel to "Annuler", UiPhrase.Create to "Créer", UiPhrase.Reset to "Réinitialiser", UiPhrase.Dry to "Sec", UiPhrase.Wet to "Humide", UiPhrase.Off to "Désactivé", UiPhrase.Reverb to "Réverbération", UiPhrase.ToneShaping to "Modelage du son", UiPhrase.Bass to "Basses", UiPhrase.Midrange to "Médiums", UiPhrase.Treble to "Aigus", UiPhrase.EffectStrength to "Intensité de l’effet"),
    AppLanguage.German to mapOf(UiPhrase.About to "Über", UiPhrase.AddToPlaylist to "Zur Playlist hinzufügen", UiPhrase.AddToQueue to "Zur Warteschlange hinzufügen", UiPhrase.GoToAlbum to "Zum Album", UiPhrase.DeleteFromLibrary to "Aus Bibliothek löschen", UiPhrase.DeleteAlbum to "Album löschen", UiPhrase.Delete to "Löschen", UiPhrase.Rename to "Umbenennen", UiPhrase.RemoveFromList to "Aus Liste entfernen", UiPhrase.NewPlaylist to "Neue Playlist", UiPhrase.Cancel to "Abbrechen", UiPhrase.Create to "Erstellen", UiPhrase.Reset to "Zurücksetzen", UiPhrase.Dry to "Trocken", UiPhrase.Wet to "Nass", UiPhrase.Off to "Aus", UiPhrase.Reverb to "Hall", UiPhrase.ToneShaping to "Klangformung", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mitten", UiPhrase.Treble to "Höhen", UiPhrase.EffectStrength to "Effektstärke"),
    AppLanguage.Greek to mapOf(UiPhrase.About to "Σχετικά", UiPhrase.AddToPlaylist to "Προσθήκη σε playlist", UiPhrase.AddToQueue to "Προσθήκη στην ουρά", UiPhrase.GoToAlbum to "Μετάβαση στο άλμπουμ", UiPhrase.DeleteFromLibrary to "Διαγραφή από βιβλιοθήκη", UiPhrase.DeleteAlbum to "Διαγραφή άλμπουμ", UiPhrase.Delete to "Διαγραφή", UiPhrase.Rename to "Μετονομασία", UiPhrase.RemoveFromList to "Αφαίρεση από λίστα", UiPhrase.NewPlaylist to "Νέο playlist", UiPhrase.Cancel to "Άκυρο", UiPhrase.Create to "Δημιουργία", UiPhrase.Reset to "Επαναφορά", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Ανενεργό", UiPhrase.Reverb to "Αντήχηση", UiPhrase.ToneShaping to "Διαμόρφωση τόνου", UiPhrase.Bass to "Μπάσα", UiPhrase.Midrange to "Μεσαία", UiPhrase.Treble to "Πρίμα", UiPhrase.EffectStrength to "Ένταση εφέ"),
    AppLanguage.Hindi to mapOf(UiPhrase.About to "परिचय", UiPhrase.AddToPlaylist to "प्लेलिस्ट में जोड़ें", UiPhrase.AddToQueue to "कतार में जोड़ें", UiPhrase.GoToAlbum to "एल्बम पर जाएं", UiPhrase.DeleteFromLibrary to "लाइब्रेरी से हटाएं", UiPhrase.DeleteAlbum to "एल्बम हटाएं", UiPhrase.Delete to "हटाएं", UiPhrase.Rename to "नाम बदलें", UiPhrase.RemoveFromList to "सूची से हटाएं", UiPhrase.NewPlaylist to "नई प्लेलिस्ट", UiPhrase.Cancel to "रद्द करें", UiPhrase.Create to "बनाएं", UiPhrase.Reset to "रीसेट", UiPhrase.Dry to "ड्राई", UiPhrase.Wet to "वेट", UiPhrase.Off to "बंद", UiPhrase.Reverb to "रीवर्ब", UiPhrase.ToneShaping to "टोन शेपिंग", UiPhrase.Bass to "बास", UiPhrase.Midrange to "मिडरेंज", UiPhrase.Treble to "ट्रेबल", UiPhrase.EffectStrength to "प्रभाव शक्ति"),
    AppLanguage.Hungarian to mapOf(UiPhrase.About to "Névjegy", UiPhrase.AddToPlaylist to "Hozzáadás lejátszási listához", UiPhrase.AddToQueue to "Hozzáadás a sorhoz", UiPhrase.GoToAlbum to "Ugrás az albumhoz", UiPhrase.DeleteFromLibrary to "Törlés a könyvtárból", UiPhrase.DeleteAlbum to "Album törlése", UiPhrase.Delete to "Törlés", UiPhrase.Rename to "Átnevezés", UiPhrase.RemoveFromList to "Eltávolítás a listából", UiPhrase.NewPlaylist to "Új lejátszási lista", UiPhrase.Cancel to "Mégse", UiPhrase.Create to "Létrehozás", UiPhrase.Reset to "Visszaállítás", UiPhrase.Dry to "Száraz", UiPhrase.Wet to "Nedves", UiPhrase.Off to "Ki", UiPhrase.Reverb to "Visszhang", UiPhrase.ToneShaping to "Hangformálás", UiPhrase.Bass to "Basszus", UiPhrase.Midrange to "Közép", UiPhrase.Treble to "Magas", UiPhrase.EffectStrength to "Effekt erőssége"),
    AppLanguage.Italian to mapOf(UiPhrase.About to "Informazioni", UiPhrase.AddToPlaylist to "Aggiungi alla playlist", UiPhrase.AddToQueue to "Aggiungi alla coda", UiPhrase.GoToAlbum to "Vai all'album", UiPhrase.DeleteFromLibrary to "Elimina dalla libreria", UiPhrase.DeleteAlbum to "Elimina album", UiPhrase.Delete to "Elimina", UiPhrase.Rename to "Rinomina", UiPhrase.RemoveFromList to "Rimuovi dalla lista", UiPhrase.NewPlaylist to "Nuova playlist", UiPhrase.Cancel to "Annulla", UiPhrase.Create to "Crea", UiPhrase.Reset to "Ripristina", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Disattivato", UiPhrase.Reverb to "Riverbero", UiPhrase.ToneShaping to "Modellazione tono", UiPhrase.Bass to "Bassi", UiPhrase.Midrange to "Medi", UiPhrase.Treble to "Alti", UiPhrase.EffectStrength to "Intensità effetto"),
    AppLanguage.Japanese to mapOf(UiPhrase.About to "情報", UiPhrase.AddToPlaylist to "プレイリストに追加", UiPhrase.AddToQueue to "キューに追加", UiPhrase.GoToAlbum to "アルバムへ移動", UiPhrase.DeleteFromLibrary to "ライブラリから削除", UiPhrase.DeleteAlbum to "アルバムを削除", UiPhrase.Delete to "削除", UiPhrase.Rename to "名前を変更", UiPhrase.RemoveFromList to "リストから削除", UiPhrase.NewPlaylist to "新しいプレイリスト", UiPhrase.Cancel to "キャンセル", UiPhrase.Create to "作成", UiPhrase.Reset to "リセット", UiPhrase.Dry to "ドライ", UiPhrase.Wet to "ウェット", UiPhrase.Off to "オフ", UiPhrase.Reverb to "リバーブ", UiPhrase.ToneShaping to "音色調整", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中域", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "エフェクト強度"),
    AppLanguage.Latin to mapOf(UiPhrase.About to "De app", UiPhrase.AddToPlaylist to "Ad indicem adde", UiPhrase.AddToQueue to "Ad ordinem adde", UiPhrase.GoToAlbum to "I ad album", UiPhrase.DeleteFromLibrary to "E bibliotheca dele", UiPhrase.DeleteAlbum to "Album dele", UiPhrase.Delete to "Dele", UiPhrase.Rename to "Renomina", UiPhrase.RemoveFromList to "E indice remove", UiPhrase.NewPlaylist to "Novus index", UiPhrase.Cancel to "Rescinde", UiPhrase.Create to "Crea", UiPhrase.Reset to "Restitue", UiPhrase.Dry to "Siccus", UiPhrase.Wet to "Humidus", UiPhrase.Off to "Exstinctum", UiPhrase.Reverb to "Reverberatio", UiPhrase.ToneShaping to "Formatio toni", UiPhrase.Bass to "Bassus", UiPhrase.Midrange to "Media", UiPhrase.Treble to "Acuti", UiPhrase.EffectStrength to "Vis effectus"),
    AppLanguage.Latvian to mapOf(UiPhrase.About to "Par", UiPhrase.AddToPlaylist to "Pievienot atskaņošanas sarakstam", UiPhrase.AddToQueue to "Pievienot rindai", UiPhrase.GoToAlbum to "Atvērt albumu", UiPhrase.DeleteFromLibrary to "Dzēst no bibliotēkas", UiPhrase.DeleteAlbum to "Dzēst albumu", UiPhrase.Delete to "Dzēst", UiPhrase.Rename to "Pārdēvēt", UiPhrase.RemoveFromList to "Noņemt no saraksta", UiPhrase.NewPlaylist to "Jauns saraksts", UiPhrase.Cancel to "Atcelt", UiPhrase.Create to "Izveidot", UiPhrase.Reset to "Atiestatīt", UiPhrase.Dry to "Sauss", UiPhrase.Wet to "Mitrs", UiPhrase.Off to "Izslēgts", UiPhrase.Reverb to "Atbalss", UiPhrase.ToneShaping to "Toņa veidošana", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Vidējās", UiPhrase.Treble to "Augšas", UiPhrase.EffectStrength to "Efekta stiprums"),
    AppLanguage.Lithuanian to mapOf(UiPhrase.About to "Apie", UiPhrase.AddToPlaylist to "Pridėti į grojaraštį", UiPhrase.AddToQueue to "Pridėti į eilę", UiPhrase.GoToAlbum to "Eiti į albumą", UiPhrase.DeleteFromLibrary to "Ištrinti iš bibliotekos", UiPhrase.DeleteAlbum to "Ištrinti albumą", UiPhrase.Delete to "Ištrinti", UiPhrase.Rename to "Pervadinti", UiPhrase.RemoveFromList to "Pašalinti iš sąrašo", UiPhrase.NewPlaylist to "Naujas grojaraštis", UiPhrase.Cancel to "Atšaukti", UiPhrase.Create to "Sukurti", UiPhrase.Reset to "Atstatyti", UiPhrase.Dry to "Sausas", UiPhrase.Wet to "Šlapias", UiPhrase.Off to "Išjungta", UiPhrase.Reverb to "Aidas", UiPhrase.ToneShaping to "Tono formavimas", UiPhrase.Bass to "Bosai", UiPhrase.Midrange to "Viduriai", UiPhrase.Treble to "Aukšti", UiPhrase.EffectStrength to "Efekto stiprumas"),
    AppLanguage.Macedonian to mapOf(UiPhrase.About to "За апликацијата", UiPhrase.AddToPlaylist to "Додај во плејлиста", UiPhrase.AddToQueue to "Додај во редица", UiPhrase.GoToAlbum to "Оди на албум", UiPhrase.DeleteFromLibrary to "Избриши од библиотека", UiPhrase.DeleteAlbum to "Избриши албум", UiPhrase.Delete to "Избриши", UiPhrase.Rename to "Преименувај", UiPhrase.RemoveFromList to "Отстрани од листа", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетирај", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Влажно", UiPhrase.Off to "Исклучено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликување тон", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средни", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Сила на ефект"),
    AppLanguage.Norwegian to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Legg til i spilleliste", UiPhrase.AddToQueue to "Legg til i kø", UiPhrase.GoToAlbum to "Gå til album", UiPhrase.DeleteFromLibrary to "Slett fra bibliotek", UiPhrase.DeleteAlbum to "Slett album", UiPhrase.Delete to "Slett", UiPhrase.Rename to "Gi nytt navn", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny spilleliste", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Opprett", UiPhrase.Reset to "Tilbakestill", UiPhrase.Dry to "Tørr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Romklang", UiPhrase.ToneShaping to "Toneforming", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mellomtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Portuguese to mapOf(UiPhrase.About to "Sobre", UiPhrase.AddToPlaylist to "Adicionar à playlist", UiPhrase.AddToQueue to "Adicionar à fila", UiPhrase.GoToAlbum to "Ir para o álbum", UiPhrase.DeleteFromLibrary to "Eliminar da biblioteca", UiPhrase.DeleteAlbum to "Eliminar álbum", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renomear", UiPhrase.RemoveFromList to "Remover da lista", UiPhrase.NewPlaylist to "Nova playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Criar", UiPhrase.Reset to "Repor", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Molhado", UiPhrase.Off to "Desligado", UiPhrase.Reverb to "Reverberação", UiPhrase.ToneShaping to "Modelação de tom", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Médios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Força do efeito"),
    AppLanguage.Russian to mapOf(UiPhrase.About to "О приложении", UiPhrase.AddToPlaylist to "Добавить в плейлист", UiPhrase.AddToQueue to "Добавить в очередь", UiPhrase.GoToAlbum to "Перейти к альбому", UiPhrase.DeleteFromLibrary to "Удалить из библиотеки", UiPhrase.DeleteAlbum to "Удалить альбом", UiPhrase.Delete to "Удалить", UiPhrase.Rename to "Переименовать", UiPhrase.RemoveFromList to "Убрать из списка", UiPhrase.NewPlaylist to "Новый плейлист", UiPhrase.Cancel to "Отмена", UiPhrase.Create to "Создать", UiPhrase.Reset to "Сбросить", UiPhrase.Dry to "Сухой", UiPhrase.Wet to "Мокрый", UiPhrase.Off to "Выкл.", UiPhrase.Reverb to "Реверберация", UiPhrase.ToneShaping to "Формирование тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верх", UiPhrase.EffectStrength to "Сила эффекта"),
    AppLanguage.Serbian to mapOf(UiPhrase.About to "О апликацији", UiPhrase.AddToPlaylist to "Додај у плејлисту", UiPhrase.AddToQueue to "Додај у ред", UiPhrase.GoToAlbum to "Иди на албум", UiPhrase.DeleteFromLibrary to "Обриши из библиотеке", UiPhrase.DeleteAlbum to "Обриши албум", UiPhrase.Delete to "Обриши", UiPhrase.Rename to "Преименуј", UiPhrase.RemoveFromList to "Уклони са листе", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетуј", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Мокро", UiPhrase.Off to "Искључено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликовање тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средњи", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Јачина ефекта"),
    AppLanguage.Spanish to mapOf(UiPhrase.About to "Acerca de", UiPhrase.AddToPlaylist to "Añadir a playlist", UiPhrase.AddToQueue to "Añadir a la cola", UiPhrase.GoToAlbum to "Ir al álbum", UiPhrase.DeleteFromLibrary to "Eliminar de la biblioteca", UiPhrase.DeleteAlbum to "Eliminar álbum", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renombrar", UiPhrase.RemoveFromList to "Quitar de la lista", UiPhrase.NewPlaylist to "Nueva playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Crear", UiPhrase.Reset to "Restablecer", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Húmedo", UiPhrase.Off to "Desactivado", UiPhrase.Reverb to "Reverberación", UiPhrase.ToneShaping to "Modelado de tono", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Medios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Intensidad del efecto"),
    AppLanguage.Swedish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Lägg till i spellista", UiPhrase.AddToQueue to "Lägg till i kö", UiPhrase.GoToAlbum to "Gå till album", UiPhrase.DeleteFromLibrary to "Ta bort från bibliotek", UiPhrase.DeleteAlbum to "Ta bort album", UiPhrase.Delete to "Ta bort", UiPhrase.Rename to "Byt namn", UiPhrase.RemoveFromList to "Ta bort från lista", UiPhrase.NewPlaylist to "Ny spellista", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Skapa", UiPhrase.Reset to "Återställ", UiPhrase.Dry to "Torr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Efterklang", UiPhrase.ToneShaping to "Tonformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellanregister", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrka"),
    AppLanguage.Thai to mapOf(UiPhrase.About to "เกี่ยวกับ", UiPhrase.AddToPlaylist to "เพิ่มไปยังเพลย์ลิสต์", UiPhrase.AddToQueue to "เพิ่มไปยังคิว", UiPhrase.GoToAlbum to "ไปที่อัลบั้ม", UiPhrase.DeleteFromLibrary to "ลบจากคลัง", UiPhrase.DeleteAlbum to "ลบอัลบั้ม", UiPhrase.Delete to "ลบ", UiPhrase.Rename to "เปลี่ยนชื่อ", UiPhrase.RemoveFromList to "ลบออกจากรายการ", UiPhrase.NewPlaylist to "เพลย์ลิสต์ใหม่", UiPhrase.Cancel to "ยกเลิก", UiPhrase.Create to "สร้าง", UiPhrase.Reset to "รีเซ็ต", UiPhrase.Dry to "แห้ง", UiPhrase.Wet to "เปียก", UiPhrase.Off to "ปิด", UiPhrase.Reverb to "รีเวิร์บ", UiPhrase.ToneShaping to "ปรับโทนเสียง", UiPhrase.Bass to "เบส", UiPhrase.Midrange to "เสียงกลาง", UiPhrase.Treble to "เสียงแหลม", UiPhrase.EffectStrength to "ความแรงของเอฟเฟกต์"),
    AppLanguage.Ukrainian to mapOf(UiPhrase.About to "Про застосунок", UiPhrase.AddToPlaylist to "Додати до плейлиста", UiPhrase.AddToQueue to "Додати до черги", UiPhrase.GoToAlbum to "Перейти до альбому", UiPhrase.DeleteFromLibrary to "Видалити з бібліотеки", UiPhrase.DeleteAlbum to "Видалити альбом", UiPhrase.Delete to "Видалити", UiPhrase.Rename to "Перейменувати", UiPhrase.RemoveFromList to "Прибрати зі списку", UiPhrase.NewPlaylist to "Новий плейлист", UiPhrase.Cancel to "Скасувати", UiPhrase.Create to "Створити", UiPhrase.Reset to "Скинути", UiPhrase.Dry to "Сухий", UiPhrase.Wet to "Мокрий", UiPhrase.Off to "Вимкнено", UiPhrase.Reverb to "Реверберація", UiPhrase.ToneShaping to "Формування тону", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верхи", UiPhrase.EffectStrength to "Сила ефекту"),
)

internal fun SpaciousnessMode.displayLabel(language: AppLanguage = AppLanguage.English): String {
    return when (this) {
        SpaciousnessMode.Off -> uiPhrase(language, UiPhrase.Off)
        SpaciousnessMode.StereoWidth -> when (language) {
            AppLanguage.Albanian -> "Gjerësi stereo"
            AppLanguage.Polish -> "Szerokość stereo"
            AppLanguage.Hindi -> "स्टीरियो चौड़ाई"
            AppLanguage.Hungarian -> "Sztereó szélesség"
            AppLanguage.German -> "Stereo-Breite"
            AppLanguage.French -> "Largeur stéréo"
            AppLanguage.Spanish -> "Amplitud estéreo"
            AppLanguage.Italian -> "Ampiezza stereo"
            AppLanguage.Latin -> "Latitudo stereo"
            AppLanguage.Portuguese -> "Largura estéreo"
            AppLanguage.Dutch -> "Stereo-breedte"
            AppLanguage.Swedish -> "Stereobredd"
            AppLanguage.Norwegian -> "Stereobredde"
            AppLanguage.Danish -> "Stereobredde"
            AppLanguage.Czech -> "Šířka sterea"
            AppLanguage.Croatian -> "Stereo širina"
            AppLanguage.Lithuanian -> "Stereo plotis"
            AppLanguage.Latvian -> "Stereo platums"
            AppLanguage.Estonian -> "Stereo laius"
            AppLanguage.Greek -> "Πλάτος stereo"
            AppLanguage.Macedonian -> "Стерео ширина"
            AppLanguage.Russian -> "Ширина стерео"
            AppLanguage.Serbian -> "Ширина стереа"
            AppLanguage.Thai -> "ความกว้างสเตอริโอ"
            AppLanguage.Ukrainian -> "Ширина стерео"
            AppLanguage.Slovak -> "Stereo šírka"
            AppLanguage.Korean -> "스테레오 폭"
            AppLanguage.Malay -> "Lebar stereo"
            AppLanguage.Bengali -> "স্টেরিও প্রস্থ"
            AppLanguage.Urdu -> "اسٹیریو چوڑائی"
            AppLanguage.ChineseSimplified -> "立体声宽度"
            AppLanguage.Japanese -> "ステレオ幅"
            else -> "Stereo Width"
        }
        SpaciousnessMode.CrossfeedDepth -> when (language) {
            AppLanguage.Albanian -> "Përzierje kanalesh"
            AppLanguage.Polish -> "Przenikanie kanałów"
            AppLanguage.Hindi -> "क्रॉसफीड"
            AppLanguage.Hungarian -> "Crossfeed"
            AppLanguage.German -> "Crossfeed"
            AppLanguage.French -> "Crossfeed"
            AppLanguage.Spanish -> "Crossfeed"
            AppLanguage.Italian -> "Crossfeed"
            AppLanguage.Latin -> "Canales mixti"
            AppLanguage.Portuguese -> "Crossfeed"
            AppLanguage.Dutch -> "Crossfeed"
            AppLanguage.Swedish -> "Crossfeed"
            AppLanguage.Norwegian -> "Crossfeed"
            AppLanguage.Danish -> "Crossfeed"
            AppLanguage.Czech -> "Crossfeed"
            AppLanguage.Croatian -> "Crossfeed"
            AppLanguage.Lithuanian -> "Kanalų susiliejimas"
            AppLanguage.Latvian -> "Kanālu sajaukums"
            AppLanguage.Estonian -> "Kanalite segamine"
            AppLanguage.Greek -> "Crossfeed"
            AppLanguage.Macedonian -> "Вкрстено мешање"
            AppLanguage.Russian -> "Кроссфид"
            AppLanguage.Serbian -> "Кросфид"
            AppLanguage.Thai -> "ครอสฟีด"
            AppLanguage.Ukrainian -> "Кросфід"
            AppLanguage.Slovak -> "Crossfeed"
            AppLanguage.Korean -> "크로스피드"
            AppLanguage.Malay -> "Crossfeed"
            AppLanguage.Bengali -> "ক্রসফিড"
            AppLanguage.Urdu -> "کراس فیڈ"
            AppLanguage.ChineseSimplified -> "交叉馈送"
            AppLanguage.Japanese -> "クロスフィード"
            else -> "Crossfeed"
        }
        SpaciousnessMode.EarlyReflectionRoom -> when (language) {
            AppLanguage.Albanian -> "Dhomë"
            AppLanguage.Polish -> "Pokój"
            AppLanguage.Hindi -> "कमरा"
            AppLanguage.Hungarian -> "Szoba"
            AppLanguage.German -> "Raum"
            AppLanguage.French -> "Pièce"
            AppLanguage.Spanish -> "Sala"
            AppLanguage.Italian -> "Stanza"
            AppLanguage.Latin -> "Camera"
            AppLanguage.Portuguese -> "Sala"
            AppLanguage.Dutch -> "Ruimte"
            AppLanguage.Swedish -> "Rum"
            AppLanguage.Norwegian -> "Rom"
            AppLanguage.Danish -> "Rum"
            AppLanguage.Czech -> "Místnost"
            AppLanguage.Croatian -> "Soba"
            AppLanguage.Lithuanian -> "Kambarys"
            AppLanguage.Latvian -> "Istaba"
            AppLanguage.Estonian -> "Tuba"
            AppLanguage.Greek -> "Δωμάτιο"
            AppLanguage.Macedonian -> "Соба"
            AppLanguage.Russian -> "Комната"
            AppLanguage.Serbian -> "Соба"
            AppLanguage.Thai -> "ห้อง"
            AppLanguage.Ukrainian -> "Кімната"
            AppLanguage.Slovak -> "Miestnosť"
            AppLanguage.Korean -> "룸"
            AppLanguage.Malay -> "Bilik"
            AppLanguage.Bengali -> "রুম"
            AppLanguage.Urdu -> "کمرہ"
            AppLanguage.ChineseSimplified -> "房间"
            AppLanguage.Japanese -> "ルーム"
            else -> "Room"
        }
        SpaciousnessMode.Philharmony -> when (language) {
            AppLanguage.Albanian -> "Filarmonia"
            AppLanguage.Polish -> "Filharmonia"
            AppLanguage.Hindi -> "फिलहार्मनी"
            AppLanguage.Hungarian -> "Filharmónia"
            AppLanguage.German -> "Philharmonie"
            AppLanguage.French -> "Philharmonie"
            AppLanguage.Spanish -> "Filarmónica"
            AppLanguage.Italian -> "Filarmonica"
            AppLanguage.Latin -> "Philharmonia"
            AppLanguage.Portuguese -> "Filarmônica"
            AppLanguage.Dutch -> "Filharmonie"
            AppLanguage.Swedish -> "Filharmoni"
            AppLanguage.Norwegian -> "Filharmoni"
            AppLanguage.Danish -> "Filharmoni"
            AppLanguage.Czech -> "Filharmonie"
            AppLanguage.Croatian -> "Filharmonija"
            AppLanguage.Lithuanian -> "Filharmonija"
            AppLanguage.Latvian -> "Filharmonija"
            AppLanguage.Estonian -> "Filharmoonia"
            AppLanguage.Greek -> "Φιλαρμονική"
            AppLanguage.Macedonian -> "Филхармонија"
            AppLanguage.Russian -> "Филармония"
            AppLanguage.Serbian -> "Филхармонија"
            AppLanguage.Thai -> "ฟิลฮาร์โมนี"
            AppLanguage.Ukrainian -> "Філармонія"
            AppLanguage.Slovak -> "Filharmónia"
            AppLanguage.Korean -> "필하모니"
            AppLanguage.Malay -> "Filharmoni"
            AppLanguage.Bengali -> "ফিলহারমনি"
            AppLanguage.Urdu -> "فلہارمونی"
            AppLanguage.ChineseSimplified -> "爱乐厅"
            AppLanguage.Japanese -> "フィルハーモニー"
            else -> "Philharmony"
        }
        SpaciousnessMode.HaasSpace -> when (language) {
            AppLanguage.Albanian -> "Hapësira Haas"
            AppLanguage.Polish -> "Przestrzeń Haasa"
            AppLanguage.Hindi -> "हास स्पेस"
            AppLanguage.Hungarian -> "Haas tér"
            AppLanguage.German -> "Haas-Raum"
            AppLanguage.French -> "Espace Haas"
            AppLanguage.Spanish -> "Espacio Haas"
            AppLanguage.Italian -> "Spazio Haas"
            AppLanguage.Latin -> "Spatium Haas"
            AppLanguage.Portuguese -> "Espaço Haas"
            AppLanguage.Dutch -> "Haas-ruimte"
            AppLanguage.Swedish -> "Haas-rymd"
            AppLanguage.Norwegian -> "Haas-rom"
            AppLanguage.Danish -> "Haas-rum"
            AppLanguage.Czech -> "Haasův prostor"
            AppLanguage.Croatian -> "Haas prostor"
            AppLanguage.Lithuanian -> "Haas erdvė"
            AppLanguage.Latvian -> "Haas telpa"
            AppLanguage.Estonian -> "Haas ruum"
            AppLanguage.Greek -> "Χώρος Haas"
            AppLanguage.Macedonian -> "Haas простор"
            AppLanguage.Russian -> "Пространство Хааса"
            AppLanguage.Serbian -> "Haas простор"
            AppLanguage.Thai -> "พื้นที่ Haas"
            AppLanguage.Ukrainian -> "Простір Хааса"
            AppLanguage.Slovak -> "Haas priestor"
            AppLanguage.Korean -> "하스 공간"
            AppLanguage.Malay -> "Ruang Haas"
            AppLanguage.Bengali -> "হাস স্পেস"
            AppLanguage.Urdu -> "ہاس اسپیس"
            AppLanguage.ChineseSimplified -> "Haas 空间"
            AppLanguage.Japanese -> "ハース空間"
            else -> "Haas Space"
        }
        SpaciousnessMode.HarmonicAir -> when (language) {
            AppLanguage.Albanian -> "Ajër harmonik"
            AppLanguage.Polish -> "Harmoniczne powietrze"
            AppLanguage.Hindi -> "हार्मोनिक एयर"
            AppLanguage.Hungarian -> "Harmonikus levegő"
            AppLanguage.German -> "Harmonische Luft"
            AppLanguage.French -> "Air harmonique"
            AppLanguage.Spanish -> "Aire armónico"
            AppLanguage.Italian -> "Aria armonica"
            AppLanguage.Latin -> "Aer harmonicus"
            AppLanguage.Portuguese -> "Ar harmônico"
            AppLanguage.Dutch -> "Harmonische lucht"
            AppLanguage.Swedish -> "Harmonisk luft"
            AppLanguage.Norwegian -> "Harmonisk luft"
            AppLanguage.Danish -> "Harmonisk luft"
            AppLanguage.Czech -> "Harmonický vzduch"
            AppLanguage.Croatian -> "Harmonični zrak"
            AppLanguage.Lithuanian -> "Harmoningas oras"
            AppLanguage.Latvian -> "Harmonisks gaiss"
            AppLanguage.Estonian -> "Harmooniline õhk"
            AppLanguage.Greek -> "Αρμονικός αέρας"
            AppLanguage.Macedonian -> "Хармоничен воздух"
            AppLanguage.Russian -> "Гармонический воздух"
            AppLanguage.Serbian -> "Хармонични ваздух"
            AppLanguage.Thai -> "อากาศฮาร์มอนิก"
            AppLanguage.Ukrainian -> "Гармонійне повітря"
            AppLanguage.Slovak -> "Harmonický vzduch"
            AppLanguage.Korean -> "하모닉 에어"
            AppLanguage.Malay -> "Udara harmonik"
            AppLanguage.Bengali -> "হারমনিক এয়ার"
            AppLanguage.Urdu -> "ہارمونک ایئر"
            AppLanguage.ChineseSimplified -> "和声音场"
            AppLanguage.Japanese -> "ハーモニックエア"
            else -> "Harmonic Air"
        }
    }
}
