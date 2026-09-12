package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType

internal data class AudiobookSettingsCopy(
    val title: String,
    val subtitle: String,
    val rewindAmount: String,
    val forwardAmount: String,
    val seconds: String,
    val resumePlayback: String,
    val resumePlaybackSubtitle: String,
)

internal fun audiobookSettingsCopy(language: AppLanguage): AudiobookSettingsCopy = when (language) {
    AppLanguage.Polish -> AudiobookSettingsCopy("Ustawienia audiobooków", "Dostosuj odtwarzanie audiobooków", "Cofanie", "Przewijanie do przodu", "s", "Wznawiaj odtwarzanie audiobooków", "Kontynuuj od ostatnio zapisanego miejsca")
    AppLanguage.German -> AudiobookSettingsCopy("Hörbuch-Einstellungen", "Hörbuchwiedergabe anpassen", "Zurückspringen", "Vorspringen", "s", "Hörbuchwiedergabe fortsetzen", "Am zuletzt gespeicherten Wiedergabepunkt fortsetzen")
    AppLanguage.French -> AudiobookSettingsCopy("Réglages des livres audio", "Personnaliser la lecture des livres audio", "Retour", "Avance", "s", "Reprendre la lecture des livres audio", "Reprendre depuis la dernière position enregistrée")
    AppLanguage.Spanish -> AudiobookSettingsCopy("Ajustes de audiolibros", "Personaliza la reproducción de audiolibros", "Retroceder", "Avanzar", "s", "Reanudar la reproducción de audiolibros", "Continuar desde la última posición guardada")
    AppLanguage.Italian -> AudiobookSettingsCopy("Impostazioni audiolibri", "Personalizza la riproduzione degli audiolibri", "Indietro", "Avanti", "s", "Riprendi la riproduzione degli audiolibri", "Continua dall’ultima posizione salvata")
    AppLanguage.Portuguese -> AudiobookSettingsCopy("Definições de audiolivros", "Personalize a reprodução de audiolivros", "Retroceder", "Avançar", "s", "Retomar a reprodução de audiolivros", "Continuar a partir da última posição guardada")
    AppLanguage.Russian -> AudiobookSettingsCopy("Настройки аудиокниг", "Настройте воспроизведение аудиокниг", "Назад", "Вперёд", "с", "Возобновлять воспроизведение аудиокниг", "Продолжать с последней сохранённой позиции")
    AppLanguage.Ukrainian -> AudiobookSettingsCopy("Налаштування аудіокниг", "Налаштуйте відтворення аудіокниг", "Назад", "Вперед", "с", "Відновлювати відтворення аудіокниг", "Продовжувати з останньої збереженої позиції")
    AppLanguage.Czech -> AudiobookSettingsCopy("Nastavení audioknih", "Přizpůsobte přehrávání audioknih", "Zpět", "Vpřed", "s", "Obnovit přehrávání audioknih", "Pokračovat od poslední uložené pozice")
    AppLanguage.Slovak -> AudiobookSettingsCopy("Nastavenia audiokníh", "Prispôsobte prehrávanie audiokníh", "Späť", "Vpred", "s", "Obnoviť prehrávanie audiokníh", "Pokračovať od poslednej uloženej pozície")
    AppLanguage.Dutch -> AudiobookSettingsCopy("Instellingen voor luisterboeken", "Pas het afspelen van luisterboeken aan", "Terug", "Vooruit", "s", "Afspelen van luisterboeken hervatten", "Ga verder vanaf de laatst opgeslagen positie")
    AppLanguage.Swedish -> AudiobookSettingsCopy("Inställningar för ljudböcker", "Anpassa uppspelning av ljudböcker", "Bakåt", "Framåt", "s", "Återuppta uppspelning av ljudböcker", "Fortsätt från den senast sparade positionen")
    AppLanguage.Danish -> AudiobookSettingsCopy("Indstillinger for lydbøger", "Tilpas afspilning af lydbøger", "Tilbage", "Frem", "s", "Genoptag afspilning af lydbøger", "Fortsæt fra den senest gemte position")
    AppLanguage.Norwegian -> AudiobookSettingsCopy("Innstillinger for lydbøker", "Tilpass avspilling av lydbøker", "Tilbake", "Frem", "s", "Fortsett avspilling av lydbøker", "Fortsett fra sist lagrede posisjon")
    AppLanguage.English -> AudiobookSettingsCopy("Audiobooks settings", "Customize audiobook playback", "Rewind amount", "Forward amount", "s", "Resume audiobook playback", "Continue from the last saved position")
    else -> AudiobookSettingsCopy("Audiobooks settings", "Customize audiobook playback", "Rewind amount", "Forward amount", "s", "Resume audiobook playback", "Continue from the last saved position")
}

internal data class SmartPlaylistSettingsCopy(
    val title: String,
    val subtitle: String,
    val availableMixes: String,
    val maximumSongs: String,
    val maximumSongsSubtitle: String,
)

internal fun smartPlaylistSettingsCopy(language: AppLanguage): SmartPlaylistSettingsCopy = when (language) {
    AppLanguage.Polish -> SmartPlaylistSettingsCopy("Inteligentne playlisty", "Dostosuj działanie inteligentnych playlist", "Dostępne miksy", "Maksymalna liczba utworów", "Wybierz, ile utworów może wyświetlać każdy inteligentny miks")
    AppLanguage.German -> SmartPlaylistSettingsCopy("Intelligente Playlists", "Verhalten intelligenter Playlists anpassen", "Verfügbare Mixe", "Maximale Anzahl an Songs", "Lege fest, wie viele Songs jeder intelligente Mix anzeigen darf")
    AppLanguage.French -> SmartPlaylistSettingsCopy("Playlists intelligentes", "Personnaliser le comportement des playlists intelligentes", "Mix disponibles", "Nombre maximal de morceaux", "Choisissez le nombre de morceaux affichés par chaque mix intelligent")
    AppLanguage.Spanish -> SmartPlaylistSettingsCopy("Playlists inteligentes", "Ajusta el comportamiento de las playlists inteligentes", "Mixes disponibles", "Máximo de canciones", "Elige cuántas canciones puede mostrar cada mix inteligente")
    AppLanguage.Italian -> SmartPlaylistSettingsCopy("Playlist intelligenti", "Personalizza il comportamento delle playlist intelligenti", "Mix disponibili", "Numero massimo di brani", "Scegli quanti brani può mostrare ogni mix intelligente")
    AppLanguage.Portuguese -> SmartPlaylistSettingsCopy("Playlists inteligentes", "Ajuste o comportamento das playlists inteligentes", "Mixes disponíveis", "Número máximo de músicas", "Escolha quantas músicas cada mix inteligente pode mostrar")
    AppLanguage.Russian -> SmartPlaylistSettingsCopy("Умные плейлисты", "Настройте поведение умных плейлистов", "Доступные миксы", "Максимум треков", "Выберите, сколько треков может показывать каждый умный микс")
    AppLanguage.Ukrainian -> SmartPlaylistSettingsCopy("Розумні плейлисти", "Налаштуйте роботу розумних плейлистів", "Доступні мікси", "Максимум треків", "Виберіть, скільки треків може показувати кожен розумний мікс")
    AppLanguage.Czech -> SmartPlaylistSettingsCopy("Chytré playlisty", "Upravte chování chytrých playlistů", "Dostupné mixy", "Maximální počet skladeb", "Zvolte, kolik skladeb může každý chytrý mix zobrazit")
    AppLanguage.Slovak -> SmartPlaylistSettingsCopy("Inteligentné playlisty", "Upravte správanie inteligentných playlistov", "Dostupné mixy", "Maximálny počet skladieb", "Vyberte, koľko skladieb môže každý inteligentný mix zobraziť")
    AppLanguage.Dutch -> SmartPlaylistSettingsCopy("Slimme afspeellijsten", "Pas het gedrag van slimme afspeellijsten aan", "Beschikbare mixen", "Maximaal aantal nummers", "Kies hoeveel nummers elke slimme mix mag tonen")
    AppLanguage.Swedish -> SmartPlaylistSettingsCopy("Smarta spellistor", "Anpassa hur smarta spellistor fungerar", "Tillgängliga mixar", "Maximalt antal låtar", "Välj hur många låtar varje smart mix får visa")
    AppLanguage.Danish -> SmartPlaylistSettingsCopy("Smarte afspilningslister", "Tilpas, hvordan smarte afspilningslister fungerer", "Tilgængelige mix", "Maksimalt antal sange", "Vælg, hvor mange sange hvert smart mix må vise")
    AppLanguage.Norwegian -> SmartPlaylistSettingsCopy("Smarte spillelister", "Tilpass hvordan smarte spillelister fungerer", "Tilgjengelige mikser", "Maksimalt antall sanger", "Velg hvor mange sanger hver smart miks kan vise")
    AppLanguage.English -> SmartPlaylistSettingsCopy("Smart playlists", "Adjust how smart playlist behave", "Available mixes", "Maximum songs per mix", "Choose how many songs each smart mix can show")
    else -> SmartPlaylistSettingsCopy("Smart playlists", "Adjust how smart playlist behave", "Available mixes", "Maximum songs per mix", "Choose how many songs each smart mix can show")
}

@Suppress("CyclomaticComplexMethod")
internal fun smartPlaylistDescription(type: BuiltInSmartPlaylistType, language: AppLanguage): String = when (type) {
    BuiltInSmartPlaylistType.RecentlyAdded -> when (language) {
        AppLanguage.Albanian -> "Këngët dhe albumet e shtuara së fundmi në bibliotekën tënde"
        AppLanguage.Bengali -> "তোমার লাইব্রেরিতে সম্প্রতি যোগ করা গান ও অ্যালবাম"
        AppLanguage.ChineseSimplified -> "最近添加到媒体库的歌曲和专辑"
        AppLanguage.Croatian -> "Pjesme i albumi nedavno dodani u tvoju biblioteku"
        AppLanguage.Czech -> "Skladby a alba nedávno přidaná do tvé knihovny"
        AppLanguage.Danish -> "Sange og albummer, der for nylig er føjet til dit bibliotek"
        AppLanguage.Dutch -> "Nummers en albums die onlangs aan je bibliotheek zijn toegevoegd"
        AppLanguage.English -> "Songs and albums added to your library recently"
        AppLanguage.Estonian -> "Hiljuti sinu teeki lisatud laulud ja albumid"
        AppLanguage.French -> "Les morceaux et albums récemment ajoutés à votre bibliothèque"
        AppLanguage.German -> "Zuletzt zu deiner Bibliothek hinzugefügte Songs und Alben"
        AppLanguage.Greek -> "Τραγούδια και άλμπουμ που προστέθηκαν πρόσφατα στη βιβλιοθήκη σου"
        AppLanguage.Hindi -> "आपकी लाइब्रेरी में हाल ही में जोड़े गए गाने और एल्बम"
        AppLanguage.Hungarian -> "A nemrég hozzáadott dalok és albumok a könyvtáradban"
        AppLanguage.Italian -> "Brani e album aggiunti di recente alla tua libreria"
        AppLanguage.Japanese -> "ライブラリに最近追加された曲とアルバム"
        AppLanguage.Korean -> "라이브러리에 최근 추가된 노래와 앨범"
        AppLanguage.Latin -> "Carmina et albumina nuper bibliothecae tuae addita"
        AppLanguage.Latvian -> "Dziesmas un albumi, kas nesen pievienoti tavai bibliotēkai"
        AppLanguage.Lithuanian -> "Neseniai į tavo biblioteką įtrauktos dainos ir albumai"
        AppLanguage.Macedonian -> "Песни и албуми неодамна додадени во твојата библиотека"
        AppLanguage.Malay -> "Lagu dan album yang baru ditambahkan ke pustaka anda"
        AppLanguage.Norwegian -> "Sanger og album som nylig ble lagt til i biblioteket ditt"
        AppLanguage.Polish -> "Utwory i albumy niedawno dodane do Twojej biblioteki"
        AppLanguage.Portuguese -> "Músicas e álbuns adicionados recentemente à sua biblioteca"
        AppLanguage.Russian -> "Песни и альбомы, недавно добавленные в вашу медиатеку"
        AppLanguage.Serbian -> "Песме и албуми недавно додати у твоју библиотеку"
        AppLanguage.Slovak -> "Skladby a albumy nedávno pridané do tvojej knižnice"
        AppLanguage.Spanish -> "Canciones y álbumes añadidos recientemente a tu biblioteca"
        AppLanguage.Swedish -> "Låtar och album som nyligen lagts till i ditt bibliotek"
        AppLanguage.Thai -> "เพลงและอัลบั้มที่เพิ่มลงในคลังของคุณเมื่อไม่นานมานี้"
        AppLanguage.Ukrainian -> "Пісні й альбоми, нещодавно додані до вашої бібліотеки"
        AppLanguage.Urdu -> "آپ کی لائبریری میں حال ہی میں شامل کیے گئے گانے اور البمز"
    }
    BuiltInSmartPlaylistType.MostPlayed -> when (language) {
        AppLanguage.Albanian -> "Këngët që i dëgjon më shpesh"
        AppLanguage.Bengali -> "যে গানগুলো তুমি সবচেয়ে বেশি শোনো"
        AppLanguage.ChineseSimplified -> "你最常播放的歌曲"
        AppLanguage.Croatian -> "Pjesme koje najčešće slušaš"
        AppLanguage.Czech -> "Skladby, které posloucháš nejčastěji"
        AppLanguage.Danish -> "De sange, du lytter mest til"
        AppLanguage.Dutch -> "De nummers waar je het vaakst naar luistert"
        AppLanguage.English -> "Songs you return to most often"
        AppLanguage.Estonian -> "Laulud, mida sa kõige sagedamini kuulad"
        AppLanguage.French -> "Les morceaux que vous écoutez le plus souvent"
        AppLanguage.German -> "Songs, die du am häufigsten hörst"
        AppLanguage.Greek -> "Τραγούδια που ακούς πιο συχνά"
        AppLanguage.Hindi -> "वे गाने जिन्हें आप सबसे अधिक सुनते हैं"
        AppLanguage.Hungarian -> "A leggyakrabban hallgatott dalaid"
        AppLanguage.Italian -> "I brani che ascolti più spesso"
        AppLanguage.Japanese -> "最もよく再生する曲"
        AppLanguage.Korean -> "가장 자주 다시 듣는 노래"
        AppLanguage.Latin -> "Carmina quae saepissime audis"
        AppLanguage.Latvian -> "Dziesmas, kuras tu klausies visbiežāk"
        AppLanguage.Lithuanian -> "Dainos, kurių dažniausiai klausaisi"
        AppLanguage.Macedonian -> "Песните што најчесто ги слушаш"
        AppLanguage.Malay -> "Lagu yang paling kerap anda dengar"
        AppLanguage.Norwegian -> "Sanger du hører på oftest"
        AppLanguage.Polish -> "Utwory, których słuchasz najczęściej"
        AppLanguage.Portuguese -> "As músicas que você ouve com mais frequência"
        AppLanguage.Russian -> "Песни, которые вы слушаете чаще всего"
        AppLanguage.Serbian -> "Песме које најчешће слушаш"
        AppLanguage.Slovak -> "Skladby, ktoré počúvaš najčastejšie"
        AppLanguage.Spanish -> "Las canciones que escuchas con más frecuencia"
        AppLanguage.Swedish -> "Låtarna du lyssnar mest på"
        AppLanguage.Thai -> "เพลงที่คุณกลับมาฟังบ่อยที่สุด"
        AppLanguage.Ukrainian -> "Пісні, які ви слухаєте найчастіше"
        AppLanguage.Urdu -> "وہ گانے جنہیں آپ سب سے زیادہ سنتے ہیں"
    }
    BuiltInSmartPlaylistType.Favorites -> when (language) {
        AppLanguage.Albanian -> "Këngët që i ke shënuar si të preferuara"
        AppLanguage.Bengali -> "তোমার পছন্দের হিসেবে চিহ্নিত গানগুলো"
        AppLanguage.ChineseSimplified -> "你标记为收藏的歌曲"
        AppLanguage.Croatian -> "Pjesme koje si označio kao omiljene"
        AppLanguage.Czech -> "Skladby, které sis označil jako oblíbené"
        AppLanguage.Danish -> "Sange, du har markeret som favoritter"
        AppLanguage.Dutch -> "Nummers die je als favoriet hebt gemarkeerd"
        AppLanguage.English -> "Songs you have marked as favorites"
        AppLanguage.Estonian -> "Laulud, mille oled lemmikuteks märkinud"
        AppLanguage.French -> "Les morceaux que vous avez ajoutés à vos favoris"
        AppLanguage.German -> "Songs, die du als Favoriten markiert hast"
        AppLanguage.Greek -> "Τραγούδια που έχεις σημειώσει ως αγαπημένα"
        AppLanguage.Hindi -> "वे गाने जिन्हें आपने पसंदीदा के रूप में चिह्नित किया है"
        AppLanguage.Hungarian -> "A kedvencként megjelölt dalaid"
        AppLanguage.Italian -> "I brani che hai segnato come preferiti"
        AppLanguage.Japanese -> "お気に入りに登録した曲"
        AppLanguage.Korean -> "즐겨찾기로 표시한 노래"
        AppLanguage.Latin -> "Carmina quae dilecta signasti"
        AppLanguage.Latvian -> "Dziesmas, kuras atzīmēji kā iecienītās"
        AppLanguage.Lithuanian -> "Dainos, kurias pažymėjai kaip mėgstamas"
        AppLanguage.Macedonian -> "Песните што ги означи како омилени"
        AppLanguage.Malay -> "Lagu yang anda tandakan sebagai kegemaran"
        AppLanguage.Norwegian -> "Sanger du har merket som favoritter"
        AppLanguage.Polish -> "Utwory oznaczone przez Ciebie jako ulubione"
        AppLanguage.Portuguese -> "As músicas que você marcou como favoritas"
        AppLanguage.Russian -> "Песни, отмеченные вами как избранные"
        AppLanguage.Serbian -> "Песме које си означио као омиљене"
        AppLanguage.Slovak -> "Skladby, ktoré si označil ako obľúbené"
        AppLanguage.Spanish -> "Las canciones que has marcado como favoritas"
        AppLanguage.Swedish -> "Låtar du har markerat som favoriter"
        AppLanguage.Thai -> "เพลงที่คุณทำเครื่องหมายเป็นรายการโปรด"
        AppLanguage.Ukrainian -> "Пісні, які ви позначили як улюблені"
        AppLanguage.Urdu -> "وہ گانے جنہیں آپ نے پسندیدہ کے طور پر نشان زد کیا ہے"
    }
}
