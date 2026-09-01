package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


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
    val changelog: String,
    val footerSubtitle: String,
    val volumeNormalization: String = "Volume normalization",
    val volumeNormalizationSubtitle: String = "Reduce loudness differences between songs when supported by file metadata",
    val crossfadeTitle: String = "Crossfade",
    val crossfadeSubtitle: String = "Adjust the transition between songs",
    val managePlaylistsTitle: String = "Manage playlists",
    val managePlaylistsSubtitle: String = "Import and export your playlists",
    val onlineLyricsTitle: String = "Online lyrics",
    val onlineLyricsSubtitle: String = "Fetch lyrics from LRCLIB",
    val checkForUpdatesTitle: String = "Check for updates",
    val checkForUpdatesSubtitle: String = "Check whether a newer version is available",
)

internal data class NowPlayingBarStyleCopy(
    val title: String,
    val subtitle: String,
    val floating: String,
    val compact: String,
)

internal fun nowPlayingBarStyleCopy(language: AppLanguage): NowPlayingBarStyleCopy = when (language) {
    AppLanguage.Albanian -> NowPlayingBarStyleCopy("Stili i shiritit të riprodhimit", "Personalizo modulin e riprodhimit", "Lundrues", "Kompakt")
    AppLanguage.Bengali -> NowPlayingBarStyleCopy("এখন বাজছে বারের স্টাইল", "এখন বাজছে মডিউলটি কাস্টমাইজ করুন", "ভাসমান", "কমপ্যাক্ট")
    AppLanguage.ChineseSimplified -> NowPlayingBarStyleCopy("正在播放栏样式", "自定义正在播放模块", "浮动", "紧凑")
    AppLanguage.Croatian -> NowPlayingBarStyleCopy("Stil trake reprodukcije", "Prilagodite modul reprodukcije", "Plutajući", "Kompaktni")
    AppLanguage.Czech -> NowPlayingBarStyleCopy("Styl lišty přehrávání", "Přizpůsobte modul přehrávání", "Plovoucí", "Kompaktní")
    AppLanguage.Danish -> NowPlayingBarStyleCopy("Stil for afspilningslinjen", "Tilpas afspilningsmodulet", "Flydende", "Kompakt")
    AppLanguage.Dutch -> NowPlayingBarStyleCopy("Stijl van de afspeelbalk", "Pas de afspeelmodule aan", "Zwevend", "Compact")
    AppLanguage.English -> NowPlayingBarStyleCopy("Now playing bar style", "Customize the now playing module", "Floating", "Compact")
    AppLanguage.Estonian -> NowPlayingBarStyleCopy("Esitusriba stiil", "Kohanda esituse moodulit", "Hõljuv", "Kompaktne")
    AppLanguage.French -> NowPlayingBarStyleCopy("Style de la barre de lecture", "Personnalisez le module de lecture", "Flottant", "Compact")
    AppLanguage.German -> NowPlayingBarStyleCopy("Stil der Wiedergabeleiste", "Passe das Wiedergabemodul an", "Schwebend", "Kompakt")
    AppLanguage.Greek -> NowPlayingBarStyleCopy("Στυλ γραμμής αναπαραγωγής", "Προσαρμόστε τη μονάδα αναπαραγωγής", "Αιωρούμενο", "Συμπαγές")
    AppLanguage.Hindi -> NowPlayingBarStyleCopy("नाउ प्लेइंग बार शैली", "नाउ प्लेइंग मॉड्यूल को कस्टमाइज़ करें", "फ्लोटिंग", "कॉम्पैक्ट")
    AppLanguage.Hungarian -> NowPlayingBarStyleCopy("Lejátszósáv stílusa", "A lejátszási modul testreszabása", "Lebegő", "Kompakt")
    AppLanguage.Italian -> NowPlayingBarStyleCopy("Stile della barra di riproduzione", "Personalizza il modulo di riproduzione", "Fluttuante", "Compatto")
    AppLanguage.Japanese -> NowPlayingBarStyleCopy("再生バーのスタイル", "再生モジュールをカスタマイズ", "フローティング", "コンパクト")
    AppLanguage.Korean -> NowPlayingBarStyleCopy("재생 바 스타일", "재생 모듈 맞춤설정", "플로팅", "컴팩트")
    AppLanguage.Latin -> NowPlayingBarStyleCopy("Stylus vectis lusionis", "Modulum lusionis customiza", "Fluitans", "Compactus")
    AppLanguage.Latvian -> NowPlayingBarStyleCopy("Atskaņošanas joslas stils", "Pielāgo atskaņošanas moduli", "Peldošs", "Kompakts")
    AppLanguage.Lithuanian -> NowPlayingBarStyleCopy("Atkūrimo juostos stilius", "Pritaikykite atkūrimo modulį", "Slankusis", "Kompaktiškas")
    AppLanguage.Macedonian -> NowPlayingBarStyleCopy("Стил на лентата за репродукција", "Приспособете го модулот за репродукција", "Пловечки", "Компактен")
    AppLanguage.Malay -> NowPlayingBarStyleCopy("Gaya bar sedang dimainkan", "Sesuaikan modul sedang dimainkan", "Terapung", "Kompak")
    AppLanguage.Norwegian -> NowPlayingBarStyleCopy("Stil for avspillingslinje", "Tilpass avspillingsmodulen", "Flytende", "Kompakt")
    AppLanguage.Polish -> NowPlayingBarStyleCopy("Styl paska odtwarzania", "Dostosuj moduł odtwarzania", "Pływający", "Kompaktowy")
    AppLanguage.Portuguese -> NowPlayingBarStyleCopy("Estilo da barra de reprodução", "Personalize o módulo de reprodução", "Flutuante", "Compacto")
    AppLanguage.Russian -> NowPlayingBarStyleCopy("Стиль панели воспроизведения", "Настройте модуль воспроизведения", "Плавающий", "Компактный")
    AppLanguage.Serbian -> NowPlayingBarStyleCopy("Стил траке за репродукцију", "Прилагодите модул за репродукцију", "Плутајући", "Компактан")
    AppLanguage.Slovak -> NowPlayingBarStyleCopy("Štýl lišty prehrávania", "Prispôsobte modul prehrávania", "Plávajúci", "Kompaktný")
    AppLanguage.Spanish -> NowPlayingBarStyleCopy("Estilo de la barra de reproducción", "Personaliza el módulo de reproducción", "Flotante", "Compacto")
    AppLanguage.Swedish -> NowPlayingBarStyleCopy("Stil på uppspelningsfältet", "Anpassa uppspelningsmodulen", "Flytande", "Kompakt")
    AppLanguage.Thai -> NowPlayingBarStyleCopy("รูปแบบแถบกำลังเล่น", "ปรับแต่งโมดูลกำลังเล่น", "ลอย", "กะทัดรัด")
    AppLanguage.Ukrainian -> NowPlayingBarStyleCopy("Стиль панелі відтворення", "Налаштуйте модуль відтворення", "Плаваючий", "Компактний")
    AppLanguage.Urdu -> NowPlayingBarStyleCopy("اب چلنے والی بار کا انداز", "اب چلنے والے ماڈیول کو حسبِ ضرورت بنائیں", "فلوٹنگ", "کمپیکٹ")
}

