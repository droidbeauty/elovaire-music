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
    AppLanguage.Latin -> RootUiCopy("Aditus ad memoriam requiritur", "Applicatio musicam in directorio Music quaerit", "Aditum permitte", "Elovaire", "Omnia alba", "Ordine alphabetico secundum artificem et titulum", "Indicem renomina", "Serva", "Index novus", "Imago indicis", "Indicem crea", "Tange ut indicem crees", "Nomen indicis", "Nomen dele", "Nulli adhuc cantus in hoc indice", "Bibliothecam scrutare", "Cantus adde", "Verba onerantur…", "Hic cantus verba habere non videtur", "Verba cela", "Bene", "Album non inventum", "Index non inventus", "Cantus saepissime auditi", "editiones praesto", "Album redde", "Album ordine fortuito redde", "Notas muta", "Mutationes serva", "Indicem muta")
    AppLanguage.Latvian -> RootUiCopy("Nepieciešama piekļuve krātuvei", "Lietotne meklē mūziku mapē Music", "Atļaut piekļuvi", "Elovaire", "Visi albumi", "Alfabētiski pēc albuma izpildītāja un nosaukuma", "Pārdēvēt sarakstu", "Saglabāt", "Jauns saraksts", "Saraksta vāciņš", "Izveidot sarakstu", "Pieskaries, lai izveidotu sarakstu", "Saraksta nosaukums", "Notīrīt nosaukumu", "Sarakstā vēl nav dziesmu", "Meklēt bibliotēkā", "Pievienot dziesmas", "Notiek dziesmas vārdu ielāde…", "Šai dziesmai, šķiet, nav vārdu", "Paslēpt dziesmas vārdus", "Labi", "Albums nav atrasts", "Saraksts nav atrasts", "Visvairāk atskaņotās dziesmas", "pieejamu izdevumu", "Atskaņot albumu", "Atskaņot albumu jauktā secībā", "Rediģēt tagus", "Saglabāt izmaiņas", "Rediģēt sarakstu")
    AppLanguage.Lithuanian -> RootUiCopy("Reikia prieigos prie saugyklos", "Programėlė ieško muzikos Music aplanke", "Leisti prieigą", "Elovaire", "Visi albumai", "Abėcėlės tvarka pagal atlikėją ir albumo pavadinimą", "Pervadinti grojaraštį", "Išsaugoti", "Naujas grojaraštis", "Grojaraščio viršelis", "Sukurti grojaraštį", "Palieskite, kad sukurtumėte grojaraštį", "Grojaraščio pavadinimas", "Išvalyti pavadinimą", "Grojaraštyje dar nėra dainų", "Ieškoti bibliotekoje", "Pridėti dainų", "Įkeliami dainos žodžiai…", "Panašu, kad ši daina neturi žodžių", "Slėpti žodžius", "Gerai", "Albumas nerastas", "Grojaraštis nerastas", "Dažniausiai klausomos dainos", "galimų leidinių", "Leisti albumą", "Leisti albumą atsitiktine tvarka", "Redaguoti žymas", "Išsaugoti pakeitimus", "Redaguoti grojaraštį")
    AppLanguage.Estonian -> RootUiCopy("Vajalik on juurdepääs salvestusruumile", "Rakendus otsib muusikat Music-kaustast", "Luba juurdepääs", "Elovaire", "Kõik albumid", "Tähestiku järgi albumi esitaja ja pealkirja põhjal", "Nimeta esitusloend ümber", "Salvesta", "Uus esitusloend", "Esitusloendi kaanepilt", "Loo esitusloend", "Esitusloendi loomiseks puuduta", "Esitusloendi nimi", "Tühjenda nimi", "Esitusloendis pole veel lugusid", "Otsi teegist", "Lisa lugusid", "Laulusõnade laadimine…", "Sellel lool ei näi olevat sõnu", "Peida laulusõnad", "OK", "Albumit ei leitud", "Esitusloendit ei leitud", "Enim esitatud lood", "väljaannet saadaval", "Esita album", "Esita album juhuslikult", "Muuda silte", "Salvesta muudatused", "Muuda esitusloendit")
    AppLanguage.Greek -> RootUiCopy("Απαιτείται πρόσβαση στον αποθηκευτικό χώρο", "Η εφαρμογή αναζητά μουσική στον φάκελο Music", "Να επιτραπεί η πρόσβαση", "Elovaire", "Όλα τα άλμπουμ", "Αλφαβητικά ανά καλλιτέχνη και τίτλο άλμπουμ", "Μετονομασία λίστας", "Αποθήκευση", "Νέα λίστα", "Εξώφυλλο λίστας", "Δημιουργία λίστας", "Πατήστε για δημιουργία λίστας", "Όνομα λίστας", "Εκκαθάριση ονόματος", "Δεν υπάρχουν ακόμα τραγούδια στη λίστα", "Αναζήτηση στη βιβλιοθήκη", "Προσθήκη τραγουδιών", "Φόρτωση στίχων…", "Αυτό το τραγούδι φαίνεται να μην έχει στίχους", "Απόκρυψη στίχων", "Εντάξει", "Το άλμπουμ δεν βρέθηκε", "Η λίστα δεν βρέθηκε", "Τραγούδια με τις περισσότερες αναπαραγωγές", "διαθέσιμες κυκλοφορίες", "Αναπαραγωγή άλμπουμ", "Τυχαία αναπαραγωγή άλμπουμ", "Επεξεργασία ετικετών", "Αποθήκευση αλλαγών", "Επεξεργασία λίστας")
    AppLanguage.Hungarian -> RootUiCopy("Tárhelyhozzáférés szükséges", "Az alkalmazás a Music mappában keresi a zenéket", "Hozzáférés engedélyezése", "Elovaire", "Összes album", "Ábécérendben az album előadója és címe szerint", "Lejátszási lista átnevezése", "Mentés", "Új lejátszási lista", "A lista borítója", "Lejátszási lista létrehozása", "Koppints egy lista létrehozásához", "A lista neve", "Név törlése", "Még nincsenek dalok a listán", "Keresés a könyvtárban", "Dalok hozzáadása", "Dalszöveg betöltése…", "Úgy tűnik, ehhez a dalhoz nincs dalszöveg", "Dalszöveg elrejtése", "OK", "Az album nem található", "A lista nem található", "Legtöbbet játszott dalok", "elérhető kiadvány", "Album lejátszása", "Album véletlenszerű lejátszása", "Címkék szerkesztése", "Módosítások mentése", "Lista szerkesztése")
    AppLanguage.Macedonian -> RootUiCopy("Потребен е пристап до складиштето", "Апликацијата бара музика во папката Music", "Дозволи пристап", "Elovaire", "Сите албуми", "По азбучен ред на изведувач и наслов", "Преименувај плејлиста", "Зачувај", "Нова плејлиста", "Насловна на плејлистата", "Создај плејлиста", "Допрете за да создадете плејлиста", "Име на плејлистата", "Исчисти име", "Сè уште нема песни во плејлистата", "Пребарај библиотека", "Додај песни", "Вчитување текст…", "Изгледа дека песната нема текст", "Скриј текст", "Во ред", "Албумот не е пронајден", "Плејлистата не е пронајдена", "Најслушани песни", "достапни изданија", "Пушти албум", "Мешај албум", "Уреди ознаки", "Зачувај промени", "Уреди плејлиста")
    AppLanguage.Italian -> RootUiCopy("Accesso alla memoria richiesto", "L’app cerca la musica nella cartella Music", "Consenti accesso", "Elovaire", "Tutti gli album", "In ordine alfabetico per artista e titolo", "Rinomina playlist", "Salva", "Nuova playlist", "Copertina della playlist", "Crea playlist", "Tocca per creare una playlist", "Nome della playlist", "Cancella nome", "Nessun brano nella playlist", "Cerca nella libreria", "Aggiungi brani", "Caricamento del testo…", "Questo brano sembra non avere un testo", "Nascondi testo", "OK", "Album non trovato", "Playlist non trovata", "Brani più ascoltati", "pubblicazioni disponibili", "Riproduci album", "Riproduci album in ordine casuale", "Modifica tag", "Salva modifiche", "Modifica playlist")
    AppLanguage.Portuguese -> RootUiCopy("É necessário acesso ao armazenamento", "A aplicação procura música na pasta Music", "Permitir acesso", "Elovaire", "Todos os álbuns", "Por ordem alfabética de artista e título do álbum", "Renomear playlist", "Guardar", "Nova playlist", "Capa da playlist", "Criar playlist", "Toque para criar uma playlist", "Nome da playlist", "Limpar nome", "Ainda não há músicas nesta playlist", "Pesquisar na biblioteca", "Adicionar músicas", "A carregar a letra…", "Esta música parece não ter letra", "Ocultar letra", "OK", "Álbum não encontrado", "Playlist não encontrada", "Músicas mais ouvidas", "edições disponíveis", "Reproduzir álbum", "Reproduzir álbum aleatoriamente", "Editar etiquetas", "Guardar alterações", "Editar playlist")
    AppLanguage.Russian -> RootUiCopy("Необходим доступ к хранилищу", "Приложение ищет музыку в папке Music", "Разрешить доступ", "Elovaire", "Все альбомы", "По исполнителю и названию альбома", "Переименовать плейлист", "Сохранить", "Новый плейлист", "Обложка плейлиста", "Создать плейлист", "Нажмите, чтобы создать плейлист", "Название плейлиста", "Очистить название", "В плейлисте пока нет песен", "Поиск в библиотеке", "Добавить песни", "Загрузка текста…", "Похоже, у этой песни нет текста", "Скрыть текст", "ОК", "Альбом не найден", "Плейлист не найден", "Самые прослушиваемые песни", "доступных изданий", "Воспроизвести альбом", "Перемешать альбом", "Редактировать теги", "Сохранить изменения", "Редактировать плейлист")
    AppLanguage.Ukrainian -> RootUiCopy("Потрібен доступ до сховища", "Застосунок шукає музику в папці Music", "Дозволити доступ", "Elovaire", "Усі альбоми", "За виконавцем і назвою альбому", "Перейменувати плейлист", "Зберегти", "Новий плейлист", "Обкладинка плейлиста", "Створити плейлист", "Натисніть, щоб створити плейлист", "Назва плейлиста", "Очистити назву", "У плейлисті ще немає пісень", "Пошук у бібліотеці", "Додати пісні", "Завантаження тексту…", "Здається, ця пісня не має тексту", "Приховати текст", "Гаразд", "Альбом не знайдено", "Плейлист не знайдено", "Найчастіше прослуховувані пісні", "доступних видань", "Відтворити альбом", "Перемішати альбом", "Редагувати теги", "Зберегти зміни", "Редагувати плейлист")
    AppLanguage.Czech -> RootUiCopy("Je nutný přístup k úložišti", "Aplikace hledá hudbu ve složce Music", "Povolit přístup", "Elovaire", "Všechna alba", "Abecedně podle interpreta a názvu alba", "Přejmenovat playlist", "Uložit", "Nový playlist", "Obal playlistu", "Vytvořit playlist", "Klepnutím vytvoříte playlist", "Název playlistu", "Vymazat název", "V playlistu zatím nejsou žádné skladby", "Hledat v knihovně", "Přidat skladby", "Načítání textu…", "Tato skladba zřejmě nemá text", "Skrýt text", "OK", "Album nebylo nalezeno", "Playlist nebyl nalezen", "Nejhranější skladby", "dostupných vydání", "Přehrát album", "Náhodně přehrát album", "Upravit značky", "Uložit změny", "Upravit playlist")
    AppLanguage.Danish -> RootUiCopy("Adgang til lager er nødvendig", "Appen søger efter musik i mappen Music", "Tillad adgang", "Elovaire", "Alle album", "Alfabetisk efter albumkunstner og albumtitel", "Omdøb afspilningsliste", "Gem", "Ny afspilningsliste", "Afspilningslistens cover", "Opret afspilningsliste", "Tryk for at oprette en afspilningsliste", "Afspilningslistens navn", "Ryd navn", "Der er endnu ingen sange på listen", "Søg i biblioteket", "Tilføj sange", "Indlæser sangtekst…", "Denne sang ser ikke ud til at have en sangtekst", "Skjul sangtekst", "OK", "Album blev ikke fundet", "Afspilningslisten blev ikke fundet", "Mest afspillede sange", "tilgængelige udgivelser", "Afspil album", "Bland album", "Rediger tags", "Gem ændringer", "Rediger afspilningsliste")
    AppLanguage.Dutch -> RootUiCopy("Opslagtoegang vereist", "De app zoekt muziek in de map Music", "Toegang toestaan", "Elovaire", "Alle albums", "Alfabetisch op albumartiest en albumtitel", "Afspeellijst hernoemen", "Opslaan", "Nieuwe afspeellijst", "Omslag van afspeellijst", "Afspeellijst maken", "Tik om een afspeellijst te maken", "Naam van afspeellijst", "Naam wissen", "Deze afspeellijst bevat nog geen nummers", "Zoeken in bibliotheek", "Nummers toevoegen", "Songtekst laden…", "Dit nummer lijkt geen songtekst te hebben", "Songtekst verbergen", "OK", "Album niet gevonden", "Afspeellijst niet gevonden", "Meest afgespeelde nummers", "uitgaven beschikbaar", "Album afspelen", "Album in willekeurige volgorde afspelen", "Tags bewerken", "Wijzigingen opslaan", "Afspeellijst bewerken")
    AppLanguage.Norwegian -> RootUiCopy("Tilgang til lagring kreves", "Appen leter etter musikk i Music-mappen", "Tillat tilgang", "Elovaire", "Alle album", "Alfabetisk etter albumartist og albumtittel", "Gi spillelisten nytt navn", "Lagre", "Ny spilleliste", "Spillelisteomslag", "Opprett spilleliste", "Trykk for å opprette en spilleliste", "Navn på spilleliste", "Tøm navn", "Det er ingen sanger i spillelisten ennå", "Søk i biblioteket", "Legg til sanger", "Laster sangtekst…", "Denne sangen ser ikke ut til å ha sangtekst", "Skjul sangtekst", "OK", "Albumet ble ikke funnet", "Spillelisten ble ikke funnet", "Mest spilte sanger", "utgivelser tilgjengelig", "Spill av album", "Spill av album i tilfeldig rekkefølge", "Rediger tagger", "Lagre endringer", "Rediger spilleliste")
    AppLanguage.Swedish -> RootUiCopy("Lagringsåtkomst krävs", "Appen söker efter musik i mappen Music", "Tillåt åtkomst", "Elovaire", "Alla album", "Alfabetiskt efter albumartist och albumtitel", "Byt namn på spellistan", "Spara", "Ny spellista", "Spellistans omslag", "Skapa spellista", "Tryck för att skapa en spellista", "Spellistans namn", "Rensa namn", "Det finns inga låtar i spellistan än", "Sök i biblioteket", "Lägg till låtar", "Läser in låttext…", "Den här låten verkar inte ha någon låttext", "Dölj låttext", "OK", "Albumet hittades inte", "Spellistan hittades inte", "Mest spelade låtar", "utgåvor tillgängliga", "Spela album", "Blanda album", "Redigera taggar", "Spara ändringar", "Redigera spellista")
    AppLanguage.ChineseSimplified -> RootUiCopy("需要存储访问权限", "应用会在 Music 文件夹中查找音乐", "允许访问", "Elovaire", "所有专辑", "按专辑艺人和专辑名称排序", "重命名播放列表", "保存", "新建播放列表", "播放列表封面", "创建播放列表", "点击创建播放列表", "播放列表名称", "清除名称", "播放列表中还没有歌曲", "搜索音乐库", "添加歌曲", "正在加载歌词…", "这首歌似乎没有歌词", "隐藏歌词", "确定", "找不到专辑", "找不到播放列表", "播放最多的歌曲", "张可用专辑", "播放专辑", "随机播放专辑", "编辑标签", "保存更改", "编辑播放列表")
    AppLanguage.Hindi -> RootUiCopy("स्टोरेज की अनुमति ज़रूरी है", "ऐप Music फ़ोल्डर में संगीत ढूँढ़ता है", "पहुँच की अनुमति दें", "Elovaire", "सभी एल्बम", "एल्बम कलाकार और फिर शीर्षक के अनुसार", "प्लेलिस्ट का नाम बदलें", "सहेजें", "नई प्लेलिस्ट", "प्लेलिस्ट का कवर", "प्लेलिस्ट बनाएँ", "प्लेलिस्ट बनाने के लिए टैप करें", "प्लेलिस्ट का नाम", "नाम साफ़ करें", "इस प्लेलिस्ट में अभी कोई गाना नहीं है", "लाइब्रेरी में खोजें", "गाने जोड़ें", "गीत लोड हो रहे हैं…", "लगता है इस गाने के बोल उपलब्ध नहीं हैं", "गीत छिपाएँ", "ठीक है", "एल्बम नहीं मिला", "प्लेलिस्ट नहीं मिली", "सबसे अधिक सुने गए गाने", "उपलब्ध रिलीज़", "एल्बम चलाएँ", "एल्बम शफ़ल करें", "टैग संपादित करें", "बदलाव सहेजें", "प्लेलिस्ट संपादित करें")
    AppLanguage.Japanese -> RootUiCopy("ストレージへのアクセスが必要です", "アプリはMusicフォルダから音楽を探します", "アクセスを許可", "Elovaire", "すべてのアルバム", "アルバムアーティスト、タイトル順", "プレイリスト名を変更", "保存", "新しいプレイリスト", "プレイリストの画像", "プレイリストを作成", "タップしてプレイリストを作成", "プレイリスト名", "名前をクリア", "このプレイリストに曲はまだありません", "ライブラリを検索", "曲を追加", "歌詞を読み込み中…", "この曲には歌詞がないようです", "歌詞を非表示", "OK", "アルバムが見つかりません", "プレイリストが見つかりません", "よく聴く曲", "件のリリース", "アルバムを再生", "アルバムをシャッフル", "タグを編集", "変更を保存", "プレイリストを編集")
    AppLanguage.Thai -> RootUiCopy("ต้องได้รับสิทธิ์เข้าถึงพื้นที่เก็บข้อมูล", "แอปค้นหาเพลงในโฟลเดอร์ Music", "อนุญาตการเข้าถึง", "Elovaire", "อัลบั้มทั้งหมด", "เรียงตามศิลปินและชื่ออัลบั้ม", "เปลี่ยนชื่อเพลย์ลิสต์", "บันทึก", "เพลย์ลิสต์ใหม่", "ภาพปกเพลย์ลิสต์", "สร้างเพลย์ลิสต์", "แตะเพื่อสร้างเพลย์ลิสต์", "ชื่อเพลย์ลิสต์", "ล้างชื่อ", "ยังไม่มีเพลงในเพลย์ลิสต์นี้", "ค้นหาในคลัง", "เพิ่มเพลง", "กำลังโหลดเนื้อเพลง…", "เพลงนี้ดูเหมือนจะไม่มีเนื้อเพลง", "ซ่อนเนื้อเพลง", "ตกลง", "ไม่พบอัลบั้ม", "ไม่พบเพลย์ลิสต์", "เพลงที่เล่นบ่อยที่สุด", "รายการเผยแพร่ที่มี", "เล่นอัลบั้ม", "สุ่มเล่นอัลบั้ม", "แก้ไขแท็ก", "บันทึกการเปลี่ยนแปลง", "แก้ไขเพลย์ลิสต์")
    AppLanguage.Croatian -> RootUiCopy("Potreban je pristup pohrani", "Aplikacija traži glazbu u mapi Music", "Dopusti pristup", "Elovaire", "Svi albumi", "Abecedno prema izvođaču i naslovu albuma", "Preimenuj playlistu", "Spremi", "Nova playlista", "Omot playliste", "Izradi playlistu", "Dodirnite za izradu playliste", "Naziv playliste", "Očisti naziv", "Na playlisti još nema pjesama", "Pretraži biblioteku", "Dodaj pjesme", "Učitavanje teksta…", "Čini se da ova pjesma nema tekst", "Sakrij tekst", "U redu", "Album nije pronađen", "Playlista nije pronađena", "Najslušanije pjesme", "dostupnih izdanja", "Reproduciraj album", "Nasumično reproduciraj album", "Uredi oznake", "Spremi promjene", "Uredi playlistu")
    AppLanguage.French -> RootUiCopy("Accès au stockage requis", "L’application recherche la musique dans le dossier Music", "Autoriser l’accès", "Elovaire", "Tous les albums", "Par artiste puis par titre d’album", "Renommer la playlist", "Enregistrer", "Nouvelle playlist", "Pochette de la playlist", "Créer une playlist", "Appuyez pour créer une playlist", "Nom de la playlist", "Effacer le nom", "Aucun morceau dans cette playlist", "Rechercher dans la bibliothèque", "Ajouter des morceaux", "Chargement des paroles…", "Ce morceau ne semble pas avoir de paroles", "Masquer les paroles", "OK", "Album introuvable", "Playlist introuvable", "Morceaux les plus écoutés", "publications disponibles", "Lire l’album", "Lire l’album aléatoirement", "Modifier les tags", "Enregistrer les modifications", "Modifier la playlist")
    AppLanguage.German -> RootUiCopy("Speicherzugriff erforderlich", "Die App sucht im Music-Ordner nach Musik", "Zugriff erlauben", "Elovaire", "Alle Alben", "Alphabetisch nach Albuminterpret und Albumtitel", "Playlist umbenennen", "Speichern", "Neue Playlist", "Playlist-Cover", "Playlist erstellen", "Zum Erstellen einer Playlist tippen", "Playlistname", "Namen löschen", "Noch keine Titel in dieser Playlist", "Bibliothek durchsuchen", "Titel hinzufügen", "Liedtexte werden geladen…", "Für diesen Titel scheint es keinen Liedtext zu geben", "Liedtext ausblenden", "OK", "Album nicht gefunden", "Playlist nicht gefunden", "Meistgespielte Titel", "Veröffentlichungen verfügbar", "Album abspielen", "Album zufällig abspielen", "Tags bearbeiten", "Änderungen speichern", "Playlist bearbeiten")
    AppLanguage.Spanish -> RootUiCopy("Se necesita acceso al almacenamiento", "La aplicación busca música en la carpeta Music", "Permitir acceso", "Elovaire", "Todos los álbumes", "Por artista y título del álbum", "Cambiar nombre de la playlist", "Guardar", "Nueva playlist", "Portada de la playlist", "Crear playlist", "Toca para crear una playlist", "Nombre de la playlist", "Borrar nombre", "Todavía no hay canciones en esta playlist", "Buscar en la biblioteca", "Añadir canciones", "Cargando letras…", "Parece que esta canción no tiene letra", "Ocultar letra", "Aceptar", "Álbum no encontrado", "Playlist no encontrada", "Canciones más reproducidas", "lanzamientos disponibles", "Reproducir álbum", "Reproducir álbum aleatoriamente", "Editar etiquetas", "Guardar cambios", "Editar playlist")
    AppLanguage.Bulgarian -> RootUiCopy("Необходим е достъп до хранилището", "Приложението търси музика в папката Music", "Разреши достъп", "Elovaire", "Всички албуми", "По изпълнител и заглавие на албума", "Преименувай плейлиста", "Запази", "Нов плейлист", "Обложка на плейлиста", "Създай плейлист", "Докосни, за да създадеш плейлист", "Име на плейлиста", "Изчисти името", "Все още няма песни в плейлиста", "Търси в библиотеката", "Добави песни", "Зареждане на текстове…", "Изглежда тази песен няма текст", "Скрий текста", "Добре", "Албумът не е намерен", "Плейлистът не е намерен", "Най-слушани песни", "налични издания", "Пусни албума", "Разбъркай албума", "Редактирай етикетите", "Запази промените", "Редактирай плейлиста")
    AppLanguage.Catalan -> RootUiCopy("Cal accedir a l’emmagatzematge", "L’aplicació cerca música a la carpeta Music", "Permet l’accés", "Elovaire", "Tots els àlbums", "Ordenats per artista i títol", "Canvia el nom de la llista", "Desa", "Llista nova", "Portada de la llista", "Crea una llista", "Toca per crear una llista", "Nom de la llista", "Esborra el nom", "Encara no hi ha cançons en aquesta llista", "Cerca a la biblioteca", "Afegeix cançons", "S’està carregant la lletra…", "Sembla que aquesta cançó no té lletra", "Amaga la lletra", "D’acord", "No s’ha trobat l’àlbum", "No s’ha trobat la llista", "Cançons més escoltades", "edicions disponibles", "Reprodueix l’àlbum", "Barreja l’àlbum", "Edita les etiquetes", "Desa els canvis", "Edita la llista")
    AppLanguage.Finnish -> RootUiCopy("Tallennustilan käyttöoikeus tarvitaan", "Sovellus etsii musiikkia Music-kansiosta", "Salli käyttöoikeus", "Elovaire", "Kaikki albumit", "Esittäjän ja albumin nimen mukaan", "Nimeä soittolista uudelleen", "Tallenna", "Uusi soittolista", "Soittolistan kansikuva", "Luo soittolista", "Luo soittolista napauttamalla", "Soittolistan nimi", "Tyhjennä nimi", "Soittolistalla ei ole vielä kappaleita", "Hae kirjastosta", "Lisää kappaleita", "Ladataan sanoituksia…", "Tällä kappaleella ei näytä olevan sanoituksia", "Piilota sanoitukset", "OK", "Albumia ei löytynyt", "Soittolistaa ei löytynyt", "Eniten soitetut kappaleet", "julkaisua saatavilla", "Toista albumi", "Sekoita albumi", "Muokkaa tunnisteita", "Tallenna muutokset", "Muokkaa soittolistaa")
    AppLanguage.Romanian -> RootUiCopy("Este necesar accesul la stocare", "Aplicația caută muzică în dosarul Music", "Permite accesul", "Elovaire", "Toate albumele", "După artist și titlul albumului", "Redenumește lista", "Salvează", "Listă nouă", "Coperta listei", "Creează o listă", "Atinge pentru a crea o listă", "Numele listei", "Șterge numele", "Lista nu conține încă melodii", "Caută în bibliotecă", "Adaugă melodii", "Se încarcă versurile…", "Această melodie pare să nu aibă versuri", "Ascunde versurile", "OK", "Albumul nu a fost găsit", "Lista nu a fost găsită", "Cele mai ascultate melodii", "lansări disponibile", "Redă albumul", "Amestecă albumul", "Editează etichetele", "Salvează modificările", "Editează lista")
    AppLanguage.Slovenian -> RootUiCopy("Potreben je dostop do shrambe", "Aplikacija išče glasbo v mapi Music", "Dovoli dostop", "Elovaire", "Vsi albumi", "Po izvajalcu in naslovu albuma", "Preimenuj seznam", "Shrani", "Nov seznam", "Naslovnica seznama", "Ustvari seznam", "Tapni za ustvarjanje seznama", "Ime seznama", "Počisti ime", "Na seznamu še ni skladb", "Išči po knjižnici", "Dodaj skladbe", "Nalaganje besedila…", "Videti je, da ta skladba nima besedila", "Skrij besedilo", "V redu", "Albuma ni mogoče najti", "Seznama ni mogoče najti", "Največkrat predvajane skladbe", "izdaj na voljo", "Predvajaj album", "Premešaj album", "Uredi oznake", "Shrani spremembe", "Uredi seznam")
    AppLanguage.Bosnian -> RootUiCopy("Potreban je pristup pohrani", "Aplikacija traži muziku u mapi Music", "Dozvoli pristup pohrani", "Elovaire", "Svi albumi", "Abecedno po izvođaču i nazivu albuma", "Preimenuj plejlistu", "Sačuvaj", "Nova plejlista", "Omot plejliste", "Kreiraj plejlistu", "Dodirni za kreiranje plejliste", "Naziv plejliste", "Obriši naziv plejliste", "Još nema pjesama u ovoj plejlisti", "Pretraži biblioteku", "Dodaj pjesme", "Učitavanje tekstova…", "Izgleda da ova pjesma nema tekst", "Sakrij tekst", "U redu", "Album nije pronađen", "Plejlista nije pronađena", "Najslušanije pjesme", "dostupnih izdanja", "Pusti album", "Nasumično pusti album", "Uredi oznake", "Sačuvaj izmjene plejliste", "Uredi plejlistu")
    AppLanguage.Indonesian -> RootUiCopy("Akses penyimpanan diperlukan", "Aplikasi mencari musik di folder Music", "Izinkan akses penyimpanan", "Elovaire", "Semua album", "Urut menurut artis album, lalu judul album", "Ganti nama daftar putar", "Simpan", "Daftar putar baru", "Sampul daftar putar", "Buat daftar putar", "Ketuk untuk membuat daftar putar", "Nama daftar putar", "Hapus nama daftar putar", "Belum ada lagu dalam daftar putar ini", "Cari di pustaka", "Tambahkan lagu", "Memuat lirik…", "Lagu ini tampaknya tidak memiliki lirik", "Sembunyikan lirik", "OK", "Album tidak ditemukan", "Daftar putar tidak ditemukan", "Lagu paling sering diputar", "rilisan tersedia", "Putar album", "Acak album", "Edit tag", "Simpan perubahan daftar putar", "Edit daftar putar")
    AppLanguage.Punjabi -> RootUiCopy("ਸਟੋਰੇਜ ਤੱਕ ਪਹੁੰਚ ਲੋੜੀਂਦੀ ਹੈ", "ਐਪ Music ਫੋਲਡਰ ਵਿੱਚ ਸੰਗੀਤ ਲੱਭਦੀ ਹੈ", "ਸਟੋਰੇਜ ਤੱਕ ਪਹੁੰਚ ਦਿਓ", "Elovaire", "ਸਾਰੀਆਂ ਐਲਬਮਾਂ", "ਐਲਬਮ ਕਲਾਕਾਰ ਅਤੇ ਫਿਰ ਸਿਰਲੇਖ ਅਨੁਸਾਰ", "ਪਲੇਲਿਸਟ ਦਾ ਨਾਮ ਬਦਲੋ", "ਸੰਭਾਲੋ", "ਨਵੀਂ ਪਲੇਲਿਸਟ", "ਪਲੇਲਿਸਟ ਦੀ ਤਸਵੀਰ", "ਪਲੇਲਿਸਟ ਬਣਾਓ", "ਪਲੇਲਿਸਟ ਬਣਾਉਣ ਲਈ ਟੈਪ ਕਰੋ", "ਪਲੇਲਿਸਟ ਦਾ ਨਾਮ", "ਪਲੇਲਿਸਟ ਦਾ ਨਾਮ ਸਾਫ਼ ਕਰੋ", "ਇਸ ਪਲੇਲਿਸਟ ਵਿੱਚ ਹਾਲੇ ਕੋਈ ਗੀਤ ਨਹੀਂ", "ਲਾਇਬ੍ਰੇਰੀ ਵਿੱਚ ਖੋਜੋ", "ਗੀਤ ਜੋੜੋ", "ਬੋਲ ਲੋਡ ਹੋ ਰਹੇ ਹਨ…", "ਲੱਗਦਾ ਹੈ ਇਸ ਗੀਤ ਦੇ ਬੋਲ ਨਹੀਂ ਹਨ", "ਬੋਲ ਲੁਕਾਓ", "ਠੀਕ ਹੈ", "ਐਲਬਮ ਨਹੀਂ ਮਿਲੀ", "ਪਲੇਲਿਸਟ ਨਹੀਂ ਮਿਲੀ", "ਸਭ ਤੋਂ ਵੱਧ ਚੱਲੇ ਗੀਤ", "ਉਪਲਬਧ ਰਿਲੀਜ਼ਾਂ", "ਐਲਬਮ ਚਲਾਓ", "ਐਲਬਮ ਸ਼ਫ਼ਲ ਕਰੋ", "ਟੈਗ ਸੋਧੋ", "ਪਲੇਲਿਸਟ ਦੀਆਂ ਤਬਦੀਲੀਆਂ ਸੰਭਾਲੋ", "ਪਲੇਲਿਸਟ ਸੋਧੋ")
    AppLanguage.Albanian -> RootUiCopy(
        firstLaunchPermissionTitle = "Kërkohet qasje në hapësirën ruajtëse",
        firstLaunchPermissionMessage = "Aplikacioni skanon dosjen Music për muzikë në pajisje",
        firstLaunchPermissionButton = "Lejo qasjen në hapësirën ruajtëse",
        appName = "Elovaire", allAlbumsTitle = "Të gjitha albumet",
        allAlbumsSubtitle = "Sipas emrit të artistit dhe më pas titullit të albumit",
        renamePlaylistTitle = "Riemërto listën e dëgjimit", save = "Ruaj", newPlaylist = "Listë e re dëgjimi",
        playlistArtworkPlaceholder = "Kopertina e listës së dëgjimit", createPlaylistButton = "Krijo listë dëgjimi",
        tapToCreateNewPlaylist = "Prek për të krijuar një listë dëgjimi", playlistNamePlaceholder = "Emri i listës së dëgjimit",
        clearPlaylistName = "Pastro emrin e listës", noSongsInPlaylistYet = "Nuk ka ende këngë në këtë listë",
        searchLibrary = "Kërko në bibliotekë", addSongsTitle = "Shto këngë", loadingLyrics = "Po ngarkohen tekstet…",
        noLyrics = "Duket se kjo këngë nuk ka tekst", hideLyrics = "Fshih tekstin", ok = "OK",
        albumNotFound = "Albumi nuk u gjet", playlistNotFound = "Lista e dëgjimit nuk u gjet",
        mostPlayedSongs = "Këngët më të dëgjuara", availableReleasesSuffix = "publikime të disponueshme",
        playAlbum = "Luaj albumin", shuffleAlbum = "Përziej albumin", editTags = "Redakto etiketat",
        savePlaylistChanges = "Ruaj ndryshimet e listës", editPlaylist = "Redakto listën e dëgjimit",
    )
    AppLanguage.Serbian -> RootUiCopy(
        firstLaunchPermissionTitle = "Потребан је приступ складишту",
        firstLaunchPermissionMessage = "Апликација подразумевано претражује фасциклу Music за музику на уређају",
        firstLaunchPermissionButton = "Дозволи приступ складишту",
        appName = "Elovaire", allAlbumsTitle = "Сви албуми",
        allAlbumsSubtitle = "По абецедном реду извођача, затим назива албума",
        renamePlaylistTitle = "Преименуј плејлисту", save = "Сачувај", newPlaylist = "Нова плејлиста",
        playlistArtworkPlaceholder = "Омот плејлисте", createPlaylistButton = "Направи плејлисту",
        tapToCreateNewPlaylist = "Додирни да направиш плејлисту", playlistNamePlaceholder = "Назив плејлисте",
        clearPlaylistName = "Обриши назив плејлисте", noSongsInPlaylistYet = "Још нема песама у овој плејлисти",
        searchLibrary = "Претражи библиотеку", addSongsTitle = "Додај песме", loadingLyrics = "Учитавање текстова…",
        noLyrics = "Изгледа да ова песма нема текст", hideLyrics = "Сакриј текст", ok = "У реду",
        albumNotFound = "Албум није пронађен", playlistNotFound = "Плејлиста није пронађена",
        mostPlayedSongs = "Најслушаније песме", availableReleasesSuffix = "доступних издања",
        playAlbum = "Пусти албум", shuffleAlbum = "Насумично пусти албум", editTags = "Уреди ознаке",
        savePlaylistChanges = "Сачувај измене плејлисте", editPlaylist = "Уреди плејлисту",
    )
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
    AppLanguage.English -> RootUiCopy(
        firstLaunchPermissionTitle = "Storage access required to start",
        firstLaunchPermissionMessage = "The app scans Music folder by default in search for music on the device",
        firstLaunchPermissionButton = "Allow storage access",
        appName = "Elovaire",
        allAlbumsTitle = "All albums",
        allAlbumsSubtitle = "Alphabetical by album artist, then album title",
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
        albumNotFound = "Album not found",
        playlistNotFound = "Playlist not found",
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
    AppLanguage.Bulgarian -> "Необходим е достъп до хранилището"
    AppLanguage.Catalan -> "Cal accedir a l’emmagatzematge"
    AppLanguage.Finnish -> "Tallennustilan käyttöoikeus tarvitaan"
    AppLanguage.Romanian -> "Este necesar accesul la stocare"
    AppLanguage.Slovenian -> "Potreben je dostop do shrambe"
    AppLanguage.Bosnian -> "Potreban je pristup pohrani"
    AppLanguage.Indonesian -> "Akses penyimpanan diperlukan"
    AppLanguage.Punjabi -> "ਸਟੋਰੇਜ ਤੱਕ ਪਹੁੰਚ ਲੋੜੀਂਦੀ ਹੈ"
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
    AppLanguage.Bulgarian -> "Приложението търси музика в папката Music на устройството"
    AppLanguage.Catalan -> "L’aplicació cerca música a la carpeta Music del dispositiu"
    AppLanguage.Finnish -> "Sovellus etsii musiikkia laitteen Music-kansiosta"
    AppLanguage.Romanian -> "Aplicația caută muzică în dosarul Music al dispozitivului"
    AppLanguage.Slovenian -> "Aplikacija išče glasbo v mapi Music v napravi"
    AppLanguage.Bosnian -> "Aplikacija traži muziku u mapi Music na uređaju"
    AppLanguage.Indonesian -> "Aplikasi memindai folder Music untuk mencari musik di perangkat"
    AppLanguage.Punjabi -> "ਐਪ ਡਿਵਾਈਸ ਦੇ Music ਫੋਲਡਰ ਵਿੱਚ ਸੰਗੀਤ ਲੱਭਦੀ ਹੈ"
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
    AppLanguage.Latin -> "Nisi aliter statuis, applicatio directorium Music scrutatur ut musicam in machina inveniat"
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
    AppLanguage.Bulgarian -> "Разреши достъп"
    AppLanguage.Catalan -> "Permet l’accés"
    AppLanguage.Finnish -> "Salli käyttöoikeus"
    AppLanguage.Romanian -> "Permite accesul"
    AppLanguage.Slovenian -> "Dovoli dostop"
    AppLanguage.Bosnian -> "Dozvoli pristup pohrani"
    AppLanguage.Indonesian -> "Izinkan akses penyimpanan"
    AppLanguage.Punjabi -> "ਸਟੋਰੇਜ ਤੱਕ ਪਹੁੰਚ ਦਿਓ"
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
    if (language == AppLanguage.Thai) return "มี $count รายการเผยแพร่"
    val lastTwo = count % 100
    val lastDigit = count % 10
    val slavicFew = lastDigit in 2..4 && lastTwo !in 12..14
    val suffix = when (language) {
        AppLanguage.English -> if (count == 1) "available release" else "available releases"
        AppLanguage.Albanian -> if (count == 1) "publikim i disponueshëm" else "publikime të disponueshme"
        AppLanguage.Bulgarian -> if (count == 1) "налично издание" else "налични издания"
        AppLanguage.Catalan -> if (count == 1) "publicació disponible" else "publicacions disponibles"
        AppLanguage.Danish -> if (count == 1) "tilgængelig udgivelse" else "tilgængelige udgivelser"
        AppLanguage.Dutch -> if (count == 1) "uitgave beschikbaar" else "uitgaven beschikbaar"
        AppLanguage.Estonian -> if (count == 1) "väljaanne saadaval" else "väljaannet saadaval"
        AppLanguage.Finnish -> if (count == 1) "julkaisu saatavilla" else "julkaisua saatavilla"
        AppLanguage.French -> if (count == 1) "publication disponible" else "publications disponibles"
        AppLanguage.German -> if (count == 1) "Veröffentlichung verfügbar" else "Veröffentlichungen verfügbar"
        AppLanguage.Greek -> if (count == 1) "διαθέσιμη κυκλοφορία" else "διαθέσιμες κυκλοφορίες"
        AppLanguage.Italian -> if (count == 1) "pubblicazione disponibile" else "pubblicazioni disponibili"
        AppLanguage.Latin -> if (count == 1) "editio praesto" else "editiones praesto"
        AppLanguage.Latvian -> when {
            lastTwo in 11..19 || lastDigit == 0 -> "pieejamu izdevumu"
            lastDigit == 1 -> "pieejams izdevums"
            else -> "pieejami izdevumi"
        }
        AppLanguage.Lithuanian -> when {
            lastTwo in 11..19 || lastDigit == 0 -> "galimų leidinių"
            lastDigit == 1 -> "galimas leidinys"
            else -> "galimi leidiniai"
        }
        AppLanguage.Macedonian -> if (count == 1) "достапно издание" else "достапни изданија"
        AppLanguage.Norwegian -> if (count == 1) "utgivelse tilgjengelig" else "utgivelser tilgjengelig"
        AppLanguage.Portuguese -> if (count == 1) "edição disponível" else "edições disponíveis"
        AppLanguage.Punjabi -> if (count == 1) "ਉਪਲਬਧ ਰਿਲੀਜ਼" else "ਉਪਲਬਧ ਰਿਲੀਜ਼ਾਂ"
        AppLanguage.Romanian -> when {
            count == 1 -> "lansare disponibilă"
            lastTwo == 0 || lastTwo >= 20 -> "de lansări disponibile"
            else -> "lansări disponibile"
        }
        AppLanguage.Spanish -> if (count == 1) "lanzamiento disponible" else "lanzamientos disponibles"
        AppLanguage.Swedish -> if (count == 1) "utgåva tillgänglig" else "utgåvor tillgängliga"
        AppLanguage.Polish -> when {
            count == 1 -> "dostępne wydanie"
            slavicFew -> "dostępne wydania"
            else -> "dostępnych wydań"
        }
        AppLanguage.Czech -> when (count) {
            1 -> "dostupné vydání"
            in 2..4 -> "dostupná vydání"
            else -> "dostupných vydání"
        }
        AppLanguage.Slovak -> when (count) {
            1 -> "dostupné vydanie"
            in 2..4 -> "dostupné vydania"
            else -> "dostupných vydaní"
        }
        AppLanguage.Slovenian -> when (lastTwo) {
            1 -> "izdaja na voljo"
            2 -> "izdaji na voljo"
            3, 4 -> "izdaje na voljo"
            else -> "izdaj na voljo"
        }
        AppLanguage.Russian -> when {
            lastDigit == 1 && lastTwo != 11 -> "доступный релиз"
            slavicFew -> "доступных релиза"
            else -> "доступных релизов"
        }
        AppLanguage.Ukrainian -> when {
            lastDigit == 1 && lastTwo != 11 -> "доступний реліз"
            slavicFew -> "доступні релізи"
            else -> "доступних релізів"
        }
        AppLanguage.Bosnian, AppLanguage.Croatian -> when {
            lastDigit == 1 && lastTwo != 11 -> "dostupno izdanje"
            slavicFew -> "dostupna izdanja"
            else -> "dostupnih izdanja"
        }
        AppLanguage.Serbian -> when {
            lastDigit == 1 && lastTwo != 11 -> "доступно издање"
            slavicFew -> "доступна издања"
            else -> "доступних издања"
        }
        else -> rootUiCopy(language).availableReleasesSuffix
    }
    return when (language) {
        AppLanguage.Bengali, AppLanguage.ChineseSimplified, AppLanguage.Japanese, AppLanguage.Korean -> "$count$suffix"
        else -> "$count $suffix"
    }
}

internal fun repeatModeLabel(repeatMode: PlaybackRepeatMode, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> Triple("Renditja", "Përsërit një", "Përsërit të gjitha")
        AppLanguage.Bengali -> Triple("ক্রম", "একটি পুনরাবৃত্তি", "সব পুনরাবৃত্তি")
        AppLanguage.Bosnian -> Triple("Redoslijed", "Ponovi pjesmu", "Ponovi sve")
        AppLanguage.Bulgarian -> Triple("Ред", "Повтори песента", "Повтори всички")
        AppLanguage.Catalan -> Triple("Ordre", "Repeteix-ne una", "Repeteix-les totes")
        AppLanguage.ChineseSimplified -> Triple("顺序播放", "单曲循环", "全部循环")
        AppLanguage.Croatian -> Triple("Redoslijed", "Ponovi pjesmu", "Ponovi sve")
        AppLanguage.Czech -> Triple("Pořadí", "Opakovat skladbu", "Opakovat vše")
        AppLanguage.Danish -> Triple("Rækkefølge", "Gentag én", "Gentag alle")
        AppLanguage.Dutch -> Triple("Volgorde", "Herhaal één", "Herhaal alles")
        AppLanguage.English -> Triple("Order", "Repeat one", "Repeat all")
        AppLanguage.Estonian -> Triple("Järjekord", "Korda üht", "Korda kõiki")
        AppLanguage.Finnish -> Triple("Järjestys", "Toista yksi", "Toista kaikki")
        AppLanguage.French -> Triple("Ordre", "Répéter un titre", "Tout répéter")
        AppLanguage.German -> Triple("Reihenfolge", "Einen Titel wiederholen", "Alle wiederholen")
        AppLanguage.Greek -> Triple("Σειρά", "Επανάληψη ενός", "Επανάληψη όλων")
        AppLanguage.Hindi -> Triple("क्रम", "एक दोहराएँ", "सभी दोहराएँ")
        AppLanguage.Hungarian -> Triple("Sorrend", "Egy ismétlése", "Összes ismétlése")
        AppLanguage.Indonesian -> Triple("Urutan", "Ulangi satu", "Ulangi semua")
        AppLanguage.Italian -> Triple("Ordine", "Ripeti un brano", "Ripeti tutto")
        AppLanguage.Japanese -> Triple("順番", "1曲リピート", "全曲リピート")
        AppLanguage.Korean -> Triple("순서", "한 곡 반복", "전체 반복")
        AppLanguage.Latin -> Triple("Ordo", "Unum repete", "Omnia repete")
        AppLanguage.Latvian -> Triple("Secība", "Atkārtot vienu", "Atkārtot visu")
        AppLanguage.Lithuanian -> Triple("Tvarka", "Kartoti vieną", "Kartoti viską")
        AppLanguage.Macedonian -> Triple("Редослед", "Повтори ја песната", "Повтори сè")
        AppLanguage.Malay -> Triple("Turutan", "Ulang satu", "Ulang semua")
        AppLanguage.Norwegian -> Triple("Rekkefølge", "Gjenta én", "Gjenta alle")
        AppLanguage.Polish -> Triple("Kolejność", "Powtórz utwór", "Powtórz wszystko")
        AppLanguage.Portuguese -> Triple("Ordem", "Repetir faixa", "Repetir tudo")
        AppLanguage.Punjabi -> Triple("ਕ੍ਰਮ", "ਇੱਕ ਦੁਹਰਾਓ", "ਸਭ ਦੁਹਰਾਓ")
        AppLanguage.Romanian -> Triple("Ordine", "Repetă una", "Repetă toate")
        AppLanguage.Russian -> Triple("Порядок", "Повторить трек", "Повторить всё")
        AppLanguage.Serbian -> Triple("Редослед", "Понови песму", "Понови све")
        AppLanguage.Slovak -> Triple("Poradie", "Opakovať skladbu", "Opakovať všetko")
        AppLanguage.Slovenian -> Triple("Vrstni red", "Ponovi skladbo", "Ponovi vse")
        AppLanguage.Spanish -> Triple("Orden", "Repetir canción", "Repetir todo")
        AppLanguage.Swedish -> Triple("Ordning", "Upprepa en", "Upprepa alla")
        AppLanguage.Thai -> Triple("ลำดับ", "เล่นซ้ำหนึ่งเพลง", "เล่นซ้ำทั้งหมด")
        AppLanguage.Ukrainian -> Triple("Порядок", "Повторити трек", "Повторити все")
        AppLanguage.Urdu -> Triple("ترتیب", "ایک کو دہرائیں", "سب دہرائیں")
    }
    return when (repeatMode) {
        PlaybackRepeatMode.Off -> labels.first
        PlaybackRepeatMode.One -> labels.second
        PlaybackRepeatMode.All -> labels.third
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

internal fun albumOfDayTitle(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> "Албум на деня"
    AppLanguage.Catalan -> "Àlbum del dia"
    AppLanguage.Finnish -> "Päivän albumi"
    AppLanguage.Romanian -> "Albumul zilei"
    AppLanguage.Slovenian -> "Album dneva"
    AppLanguage.Bosnian -> "Album dana"
    AppLanguage.Indonesian -> "Album hari ini"
    AppLanguage.Punjabi -> "ਅੱਜ ਦੀ ਐਲਬਮ"
    AppLanguage.Albanian -> "Albumi i ditës"
    AppLanguage.English -> "Album of the day"
    AppLanguage.ChineseSimplified -> "每日专辑"
    AppLanguage.Croatian -> "Album dana"
    AppLanguage.Czech -> "Album dne"
    AppLanguage.Danish -> "Dagens album"
    AppLanguage.Dutch -> "Album van de dag"
    AppLanguage.Bengali -> "আজকের অ্যালবাম"
    AppLanguage.Estonian -> "Päeva album"
    AppLanguage.French -> "Album du jour"
    AppLanguage.German -> "Album des Tages"
    AppLanguage.Greek -> "Άλμπουμ της ημέρας"
    AppLanguage.Hindi -> "आज का एल्बम"
    AppLanguage.Hungarian -> "A nap albuma"
    AppLanguage.Italian -> "Album del giorno"
    AppLanguage.Japanese -> "今日のアルバム"
    AppLanguage.Korean -> "오늘의 앨범"
    AppLanguage.Latin -> "Album diei"
    AppLanguage.Latvian -> "Dienas albums"
    AppLanguage.Lithuanian -> "Dienos albumas"
    AppLanguage.Malay -> "Album hari ini"
    AppLanguage.Macedonian -> "Албум на денот"
    AppLanguage.Norwegian -> "Dagens album"
    AppLanguage.Polish -> "Album dnia"
    AppLanguage.Portuguese -> "Álbum do dia"
    AppLanguage.Russian -> "Альбом дня"
    AppLanguage.Slovak -> "Album dňa"
    AppLanguage.Serbian -> "Албум дана"
    AppLanguage.Spanish -> "Álbum del día"
    AppLanguage.Swedish -> "Dagens album"
    AppLanguage.Thai -> "อัลบั้มประจำวัน"
    AppLanguage.Ukrainian -> "Альбом дня"
    AppLanguage.Urdu -> "آج کا البم"
}

internal fun homeCopy(language: AppLanguage): HomeUiCopy = when (language) {
    AppLanguage.Bulgarian -> HomeUiCopy("Индексиране на библиотеката", "Търсене на музика в устройството", "Библиотеката е празна", "Добавете музика в устройството", "Няма скоро добавени албуми", "Скоро добавените албуми ще се появят тук", "Последно слушани", "Песните ще се появят тук", "Любими албуми", "Музиката, към която се връщате често", "Все още няма любими албуми", "Слушайте музика и любимите ви албуми ще се появят тук")
    AppLanguage.Catalan -> HomeUiCopy("S’està indexant la biblioteca", "S’està cercant música al dispositiu", "La biblioteca és buida", "Afegeix música al dispositiu", "No hi ha àlbums afegits recentment", "Els àlbums nous apareixeran aquí", "Escoltades recentment", "Les cançons apareixeran aquí aviat", "Àlbums preferits", "La música que escoltes sovint", "Encara no hi ha àlbums preferits", "Escolta música i els teus àlbums preferits apareixeran aquí")
    AppLanguage.Finnish -> HomeUiCopy("Indeksoidaan kirjastoa", "Etsitään musiikkia laitteelta", "Kirjasto on tyhjä", "Lisää musiikkia laitteellesi", "Ei äskettäin lisättyjä albumeita", "Uudet albumit näkyvät täällä", "Äskettäin soitetut", "Kappaleet näkyvät täällä pian", "Suosikkialbumisi", "Musiikkia, johon palaat usein", "Ei vielä suosikkialbumeita", "Kuuntele musiikkia, niin suosikkialbumisi näkyvät täällä")
    AppLanguage.Romanian -> HomeUiCopy("Se indexează biblioteca", "Se caută muzică pe dispozitiv", "Biblioteca este goală", "Adaugă muzică pe dispozitiv", "Nu există albume adăugate recent", "Albumele adăugate recent vor apărea aici", "Redate recent", "Melodiile vor apărea aici în curând", "Albumele tale preferate", "Muzica pe care o asculți des", "Nu ai încă albume preferate", "Ascultă muzică și albumele preferate vor apărea aici")
    AppLanguage.Slovenian -> HomeUiCopy("Indeksiranje knjižnice", "Iskanje glasbe v napravi", "Knjižnica je prazna", "Dodajte glasbo v napravo", "Ni nedavno dodanih albumov", "Nedavno dodani albumi bodo prikazani tukaj", "Nedavno predvajano", "Skladbe bodo kmalu prikazane tukaj", "Vaši priljubljeni albumi", "Glasba, h kateri se pogosto vračate", "Še ni priljubljenih albumov", "Poslušajte glasbo in priljubljeni albumi bodo prikazani tukaj")
    AppLanguage.Bosnian -> HomeUiCopy("Indeksiranje biblioteke", "Tražimo muziku na uređaju", "Biblioteka je prazna", "Dodajte muziku na uređaj", "Nema nedavno dodanih albuma", "Nedavno dodani albumi će se pojaviti ovdje", "Nedavno slušano", "Pjesme će se uskoro pojaviti ovdje", "Vaši omiljeni albumi", "Muzika kojoj se često vraćate", "Još nema omiljenih albuma", "Slušajte muziku i vaši omiljeni albumi će se pojaviti ovdje")
    AppLanguage.Indonesian -> HomeUiCopy("Mengindeks pustaka", "Mencari musik di perangkat", "Pustaka kosong", "Tambahkan musik ke perangkat", "Belum ada album yang baru ditambahkan", "Album yang baru ditambahkan akan muncul di sini", "Baru diputar", "Lagu akan segera muncul di sini", "Album favorit Anda", "Musik yang sering Anda dengarkan", "Belum ada album favorit", "Dengarkan musik dan album favorit Anda akan muncul di sini")
    AppLanguage.Punjabi -> HomeUiCopy("ਲਾਇਬ੍ਰੇਰੀ ਦੀ ਸੂਚੀ ਬਣ ਰਹੀ ਹੈ", "ਡਿਵਾਈਸ ਉੱਤੇ ਸੰਗੀਤ ਲੱਭਿਆ ਜਾ ਰਿਹਾ ਹੈ", "ਲਾਇਬ੍ਰੇਰੀ ਖਾਲੀ ਹੈ", "ਆਪਣੇ ਡਿਵਾਈਸ ਉੱਤੇ ਸੰਗੀਤ ਜੋੜੋ", "ਹਾਲ ਹੀ ਵਿੱਚ ਕੋਈ ਐਲਬਮ ਨਹੀਂ ਜੋੜੀ ਗਈ", "ਹਾਲ ਹੀ ਵਿੱਚ ਜੋੜੀਆਂ ਐਲਬਮਾਂ ਇੱਥੇ ਦਿਖਣਗੀਆਂ", "ਹਾਲ ਹੀ ਵਿੱਚ ਚੱਲੇ", "ਗੀਤ ਜਲਦੀ ਇੱਥੇ ਦਿਖਣਗੇ", "ਤੁਹਾਡੀਆਂ ਮਨਪਸੰਦ ਐਲਬਮਾਂ", "ਸੰਗੀਤ ਜੋ ਤੁਸੀਂ ਅਕਸਰ ਸੁਣਦੇ ਹੋ", "ਹਾਲੇ ਕੋਈ ਮਨਪਸੰਦ ਐਲਬਮ ਨਹੀਂ", "ਸੰਗੀਤ ਸੁਣੋ ਅਤੇ ਮਨਪਸੰਦ ਐਲਬਮਾਂ ਇੱਥੇ ਦਿਖਣਗੀਆਂ")
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
        indexingMessage = "Cantus et alba hic apparebunt post indicem confectum",
        emptyLibraryTitle = "Nulla musica inventa est",
        emptyLibraryMessage = "Cantus et alba hic apparebunt cum musicam in folder Music praeordinatum addideris",
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
    if (language == AppLanguage.Bengali) {
        val label = when (noun) {
            "song" -> "গান"
            "track" -> "ট্র্যাক"
            "audiobook" -> "অডিওবুক"
            "album" -> "অ্যালবাম"
            "artist" -> "শিল্পী"
            "genre" -> "ধরন"
            "playlist" -> "প্লেলিস্ট"
            else -> null
        }
        if (label != null) return "$count${if (noun == "artist") "জন" else "টি"} $label"
    }
    if (noun == "playlist") {
        return localizedPlaylistCountLabel(count, language)
    }
    localizedComplexCountLabel(count, noun, language)?.let { return it }
    val (singular, plural) = when (language) {
        AppLanguage.Bulgarian -> when (noun) {
            "song", "track" -> "песен" to "песни"
            "audiobook" -> "аудиокнига" to "аудиокниги"
            "album" -> "албум" to "албума"
            "artist" -> "изпълнител" to "изпълнители"
            "genre" -> "жанр" to "жанра"
            else -> noun to noun
        }
        AppLanguage.Catalan -> when (noun) {
            "song", "track" -> "cançó" to "cançons"
            "audiobook" -> "audiollibre" to "audiollibres"
            "album" -> "àlbum" to "àlbums"
            "artist" -> "artista" to "artistes"
            "genre" -> "gènere" to "gèneres"
            else -> noun to noun
        }
        AppLanguage.Finnish -> when (noun) {
            "song", "track" -> "kappale" to "kappaletta"
            "audiobook" -> "äänikirja" to "äänikirjaa"
            "album" -> "albumi" to "albumia"
            "artist" -> "esittäjä" to "esittäjää"
            "genre" -> "tyylilaji" to "tyylilajia"
            else -> noun to noun
        }
        AppLanguage.Romanian -> when (noun) {
            "song", "track" -> "melodie" to "melodii"
            "audiobook" -> "audiocarte" to "audiocărți"
            "album" -> "album" to "albume"
            "artist" -> "artist" to "artiști"
            "genre" -> "gen" to "genuri"
            else -> noun to noun
        }
        AppLanguage.Slovenian -> when (noun) {
            "song", "track" -> "skladba" to "skladb"
            "audiobook" -> "zvočna knjiga" to "zvočnih knjig"
            "album" -> "album" to "albumov"
            "artist" -> "izvajalec" to "izvajalcev"
            "genre" -> "žanr" to "žanrov"
            else -> noun to noun
        }
        AppLanguage.Bosnian -> when (noun) {
            "song", "track" -> "pjesma" to "pjesme"
            "audiobook" -> "audioknjiga" to "audioknjige"
            "album" -> "album" to "albuma"
            "artist" -> "izvođač" to "izvođača"
            "genre" -> "žanr" to "žanra"
            else -> noun to noun
        }
        AppLanguage.Indonesian -> when (noun) {
            "song", "track" -> "lagu" to "lagu"
            "audiobook" -> "buku audio" to "buku audio"
            "album" -> "album" to "album"
            "artist" -> "artis" to "artis"
            "genre" -> "genre" to "genre"
            else -> noun to noun
        }
        AppLanguage.Punjabi -> when (noun) {
            "song", "track" -> "ਗੀਤ" to "ਗੀਤ"
            "audiobook" -> "ਆਡੀਓ ਕਿਤਾਬ" to "ਆਡੀਓ ਕਿਤਾਬਾਂ"
            "album" -> "ਐਲਬਮ" to "ਐਲਬਮਾਂ"
            "artist" -> "ਕਲਾਕਾਰ" to "ਕਲਾਕਾਰ"
            "genre" -> "ਸ਼ੈਲੀ" to "ਸ਼ੈਲੀਆਂ"
            else -> noun to noun
        }
        AppLanguage.Albanian -> when (noun) {
            "song" -> "këngë" to "këngë"
            "track" -> "këngë" to "këngë"
            "audiobook" -> "libër audio" to "libra audio"
            "album" -> "album" to "albume"
            "artist" -> "artist" to "artistë"
            "genre" -> "zhanër" to "zhanre"
            else -> noun to "${noun}e"
        }
        AppLanguage.ChineseSimplified -> when (noun) {
            "song", "track" -> "首歌曲" to "首歌曲"
            "audiobook" -> "本有声书" to "本有声书"
            "album" -> "张专辑" to "张专辑"
            "artist" -> "位艺术家" to "位艺术家"
            "genre" -> "种曲风" to "种曲风"
            else -> noun to noun
        }
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
            "audiobook" -> "lydbog" to "lydbøger"
            "album" -> "album" to "albummer"
            "artist" -> "kunstner" to "kunstnere"
            "genre" -> "genre" to "genrer"
            else -> noun to "${noun}er"
        }
        AppLanguage.Dutch -> when (noun) {
            "song" -> "nummer" to "nummers"
            "track" -> "track" to "tracks"
            "audiobook" -> "luisterboek" to "luisterboeken"
            "album" -> "album" to "albums"
            "artist" -> "artiest" to "artiesten"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.Estonian -> when (noun) {
            "song" -> "lugu" to "lugu"
            "track" -> "lugu" to "lugu"
            "audiobook" -> "audioraamat" to "audioraamatut"
            "album" -> "album" to "albumit"
            "artist" -> "artist" to "artisti"
            "genre" -> "žanr" to "žanri"
            else -> noun to noun
        }
        AppLanguage.French -> when (noun) {
            "song" -> "morceau" to "morceaux"
            "track" -> "piste" to "pistes"
            "audiobook" -> "livre audio" to "livres audio"
            "album" -> "album" to "albums"
            "artist" -> "artiste" to "artistes"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
        AppLanguage.German -> when (noun) {
            "song" -> "Titel" to "Titel"
            "track" -> "Track" to "Tracks"
            "audiobook" -> "Hörbuch" to "Hörbücher"
            "album" -> "Album" to "Alben"
            "artist" -> "Künstler" to "Künstler"
            "genre" -> "Genre" to "Genres"
            else -> noun to "${noun}e"
        }
        AppLanguage.Greek -> when (noun) {
            "song" -> "τραγούδι" to "τραγούδια"
            "track" -> "κομμάτι" to "κομμάτια"
            "audiobook" -> "ηχητικό βιβλίο" to "ηχητικά βιβλία"
            "album" -> "άλμπουμ" to "άλμπουμ"
            "artist" -> "καλλιτέχνης" to "καλλιτέχνες"
            "genre" -> "είδος" to "είδη"
            else -> noun to noun
        }
        AppLanguage.Hindi -> when (noun) {
            "song" -> "गाना" to "गाने"
            "track" -> "ट्रैक" to "ट्रैक"
            "audiobook" -> "ऑडियोबुक" to "ऑडियोबुक"
            "album" -> "एल्बम" to "एल्बम"
            "artist" -> "कलाकार" to "कलाकार"
            "genre" -> "शैली" to "शैलियाँ"
            else -> noun to noun
        }
        AppLanguage.Hungarian -> when (noun) {
            "song" -> "dal" to "dal"
            "track" -> "szám" to "szám"
            "audiobook" -> "hangoskönyv" to "hangoskönyv"
            "album" -> "album" to "album"
            "artist" -> "előadó" to "előadó"
            "genre" -> "műfaj" to "műfaj"
            else -> noun to noun
        }
        AppLanguage.Italian -> when (noun) {
            "song" -> "brano" to "brani"
            "track" -> "traccia" to "tracce"
            "audiobook" -> "audiolibro" to "audiolibri"
            "album" -> "album" to "album"
            "artist" -> "artista" to "artisti"
            "genre" -> "genere" to "generi"
            else -> noun to "${noun}i"
        }
        AppLanguage.Japanese -> when (noun) {
            "song", "track" -> "曲" to "曲"
            "audiobook" -> "冊のオーディオブック" to "冊のオーディオブック"
            "album" -> "枚のアルバム" to "枚のアルバム"
            "artist" -> "組のアーティスト" to "組のアーティスト"
            "genre" -> "種類のジャンル" to "種類のジャンル"
            else -> noun to noun
        }
        AppLanguage.Latin -> when (noun) {
            "song" -> "cantus" to "cantus"
            "track" -> "cantus" to "cantus"
            "audiobook" -> "liber audio" to "libri audio"
            "album" -> "album" to "alba"
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
            "audiobook" -> "аудиокнига" to "аудиокниги"
            "album" -> "албум" to "албуми"
            "artist" -> "артист" to "артисти"
            "genre" -> "жанр" to "жанрови"
            else -> noun to noun
        }
        AppLanguage.Norwegian -> when (noun) {
            "song" -> "sang" to "sanger"
            "track" -> "spor" to "spor"
            "audiobook" -> "lydbok" to "lydbøker"
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
            "audiobook" -> "audiolivro" to "audiolivros"
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
            "audiobook" -> "audiolibro" to "audiolibros"
            "album" -> "álbum" to "álbumes"
            "artist" -> "artista" to "artistas"
            "genre" -> "género" to "géneros"
            else -> noun to "${noun}s"
        }
        AppLanguage.Swedish -> when (noun) {
            "song" -> "låt" to "låtar"
            "track" -> "spår" to "spår"
            "audiobook" -> "ljudbok" to "ljudböcker"
            "album" -> "album" to "album"
            "artist" -> "artist" to "artister"
            "genre" -> "genre" to "genrer"
            else -> noun to noun
        }
        AppLanguage.Korean -> when (noun) {
            "song", "track" -> "곡" to "곡"
            "audiobook" -> "권의 오디오북" to "권의 오디오북"
            "album" -> "개의 앨범" to "개의 앨범"
            "artist" -> "명의 아티스트" to "명의 아티스트"
            "genre" -> "개의 장르" to "개의 장르"
            else -> noun to noun
        }
        AppLanguage.Malay -> when (noun) {
            "song" -> "lagu" to "lagu"
            "track" -> "runut" to "runut"
            "audiobook" -> "buku audio" to "buku audio"
            "album" -> "album" to "album"
            "artist" -> "artis" to "artis"
            "genre" -> "genre" to "genre"
            else -> noun to noun
        }
        AppLanguage.Thai -> when (noun) {
            "song" -> "เพลง" to "เพลง"
            "track" -> "แทร็ก" to "แทร็ก"
            "audiobook" -> "หนังสือเสียง" to "หนังสือเสียง"
            "album" -> "อัลบั้ม" to "อัลบั้ม"
            "artist" -> "ศิลปิน" to "ศิลปิน"
            "genre" -> "แนวเพลง" to "แนวเพลง"
            else -> noun to noun
        }
        AppLanguage.Bengali -> when (noun) {
            "song" -> "গান" to "গান"
            "track" -> "ট্র্যাক" to "ট্র্যাক"
            "audiobook" -> "অডিওবুক" to "অডিওবুক"
            "album" -> "অ্যালবাম" to "অ্যালবাম"
            "artist" -> "শিল্পী" to "শিল্পী"
            "genre" -> "ধরন" to "ধরন"
            else -> noun to noun
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> "пісня" to "пісні"
            "track" -> "трек" to "треки"
            "audiobook" -> "аудіокнига" to "аудіокниги"
            "album" -> "альбом" to "альбоми"
            "artist" -> "виконавець" to "виконавці"
            "genre" -> "жанр" to "жанри"
            else -> noun to noun
        }
        AppLanguage.Urdu -> when (noun) {
            "song" -> "گانا" to "گانے"
            "track" -> "ٹریک" to "ٹریک"
            "audiobook" -> "آڈیو بک" to "آڈیو بکس"
            "album" -> "البم" to "البمز"
            "artist" -> "آرٹسٹ" to "آرٹسٹس"
            "genre" -> "صنف" to "اصناف"
            else -> noun to noun
        }
        AppLanguage.English -> when (noun) {
            "song" -> "song" to "songs"
            "track" -> "track" to "tracks"
            "audiobook" -> "audiobook" to "audiobooks"
            "album" -> "album" to "albums"
            "artist" -> "artist" to "artists"
            "genre" -> "genre" to "genres"
            else -> noun to "${noun}s"
        }
    }
    val label = if (count == 1) singular else plural
    val separator = if (language == AppLanguage.Romanian && count >= 20 && count % 100 !in 1..19) " de " else " "
    return "$count$separator$label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedPlaylistCountLabel(count: Int, language: AppLanguage): String {
    val forms = when (language) {
        AppLanguage.Bulgarian -> Triple("плейлист", "плейлиста", "плейлиста")
        AppLanguage.Catalan -> Triple("llista", "llistes", "llistes")
        AppLanguage.Finnish -> Triple("soittolista", "soittolistaa", "soittolistaa")
        AppLanguage.Romanian -> Triple("listă", "liste", "liste")
        AppLanguage.Slovenian -> Triple("seznam predvajanja", "seznama predvajanja", "seznamov predvajanja")
        AppLanguage.Bosnian -> Triple("plejlista", "plejliste", "plejlista")
        AppLanguage.Indonesian -> Triple("daftar putar", "daftar putar", "daftar putar")
        AppLanguage.Punjabi -> Triple("ਪਲੇਲਿਸਟ", "ਪਲੇਲਿਸਟਾਂ", "ਪਲੇਲਿਸਟਾਂ")
        AppLanguage.Albanian -> Triple("listë dëgjimi", "lista dëgjimi", "lista dëgjimi")
        AppLanguage.ChineseSimplified -> Triple("个播放列表", "个播放列表", "个播放列表")
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
        AppLanguage.Hungarian -> Triple("lejátszási lista", "lejátszási lista", "lejátszási lista")
        AppLanguage.Italian -> Triple("playlist", "playlist", "playlist")
        AppLanguage.Japanese -> Triple("件のプレイリスト", "件のプレイリスト", "件のプレイリスト")
        AppLanguage.Korean -> Triple("개의 재생목록", "개의 재생목록", "개의 재생목록")
        AppLanguage.Latin -> Triple("index cantuum", "indices cantuum", "indices cantuum")
        AppLanguage.Latvian -> Triple("atskaņošanas saraksts", "atskaņošanas saraksti", "atskaņošanas sarakstu")
        AppLanguage.Lithuanian -> Triple("grojaraštis", "grojaraščiai", "grojaraščių")
        AppLanguage.Malay -> Triple("senarai main", "senarai main", "senarai main")
        AppLanguage.Macedonian -> Triple("плејлиста", "плејлисти", "плејлисти")
        AppLanguage.Norwegian -> Triple("spilleliste", "spillelister", "spillelister")
        AppLanguage.Polish -> Triple("playlista", "playlisty", "playlist")
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
        language == AppLanguage.Slovenian && lastTwo == 1 -> forms.first
        language == AppLanguage.Slovenian && lastTwo == 2 -> forms.second
        language == AppLanguage.Slovenian && lastTwo in 3..4 -> "seznami predvajanja"
        language == AppLanguage.Slovenian -> forms.third
        language in setOf(AppLanguage.Bulgarian, AppLanguage.Catalan, AppLanguage.Finnish, AppLanguage.Romanian) -> if (number == 1) forms.first else forms.second
        language == AppLanguage.Punjabi -> if (number == 1) forms.first else forms.second
        language == AppLanguage.Polish && number == 1 -> forms.first
        language == AppLanguage.Polish && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Polish -> forms.third
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number == 1 -> forms.first
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number in 2..4 -> forms.second
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) -> forms.third
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Latvian && last in 2..9 -> forms.second
        language == AppLanguage.Latvian && last == 1 -> forms.first
        language == AppLanguage.Lithuanian && (last == 0 || lastTwo in 11..19) -> forms.third
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) && last == 1 && lastTwo != 11 -> forms.first
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language in setOf(
            AppLanguage.Bosnian,
            AppLanguage.Croatian,
            AppLanguage.Russian,
            AppLanguage.Serbian,
            AppLanguage.Ukrainian,
        ) -> forms.third
        language == AppLanguage.Lithuanian && last == 1 && lastTwo != 11 -> forms.first
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        number == 1 -> forms.first
        else -> forms.second
    }
    val separator = if (language == AppLanguage.Romanian && count >= 20 && count % 100 !in 1..19) " de " else " "
    return "$count$separator$label"
}

@Suppress("CyclomaticComplexMethod")
private fun localizedComplexCountLabel(
    count: Int,
    noun: String,
    language: AppLanguage,
): String? {
    val forms = when (language) {
        AppLanguage.Slovenian -> when (noun) {
            "song", "track" -> Triple("skladba", "skladbi", "skladb")
            "audiobook" -> Triple("zvočna knjiga", "zvočni knjigi", "zvočnih knjig")
            "album" -> Triple("album", "albuma", "albumov")
            "artist" -> Triple("izvajalec", "izvajalca", "izvajalcev")
            "genre" -> Triple("žanr", "žanra", "žanrov")
            else -> return null
        }
        AppLanguage.Bosnian -> when (noun) {
            "song", "track" -> Triple("pjesma", "pjesme", "pjesama")
            "audiobook" -> Triple("audioknjiga", "audioknjige", "audioknjiga")
            "album" -> Triple("album", "albuma", "albuma")
            "artist" -> Triple("izvođač", "izvođača", "izvođača")
            "genre" -> Triple("žanr", "žanra", "žanrova")
            else -> return null
        }
        AppLanguage.Croatian -> when (noun) {
            "song", "track" -> Triple("pjesma", "pjesme", "pjesama")
            "audiobook" -> Triple("audioknjiga", "audioknjige", "audioknjiga")
            "album" -> Triple("album", "albuma", "albuma")
            "artist" -> Triple("izvođač", "izvođača", "izvođača")
            "genre" -> Triple("žanr", "žanra", "žanrova")
            else -> return null
        }
        AppLanguage.Czech -> when (noun) {
            "song", "track" -> Triple("skladba", "skladby", "skladeb")
            "audiobook" -> Triple("audiokniha", "audioknihy", "audioknih")
            "album" -> Triple("album", "alba", "alb")
            "artist" -> Triple("umělec", "umělci", "umělců")
            "genre" -> Triple("žánr", "žánry", "žánrů")
            else -> return null
        }
        AppLanguage.Latvian -> when (noun) {
            "song" -> Triple("dziesma", "dziesmas", "dziesmu")
            "track" -> Triple("ieraksts", "ieraksti", "ierakstu")
            "audiobook" -> Triple("audiogrāmata", "audiogrāmatas", "audiogrāmatu")
            "album" -> Triple("albums", "albumi", "albumu")
            "artist" -> Triple("mākslinieks", "mākslinieki", "mākslinieku")
            "genre" -> Triple("žanrs", "žanri", "žanru")
            else -> return null
        }
        AppLanguage.Lithuanian -> when (noun) {
            "song" -> Triple("daina", "dainos", "dainų")
            "track" -> Triple("takelis", "takeliai", "takelių")
            "audiobook" -> Triple("audioknyga", "audioknygos", "audioknygų")
            "album" -> Triple("albumas", "albumai", "albumų")
            "artist" -> Triple("atlikėjas", "atlikėjai", "atlikėjų")
            "genre" -> Triple("žanras", "žanrai", "žanrų")
            else -> return null
        }
        AppLanguage.Polish -> when (noun) {
            "song", "track" -> Triple("utwór", "utwory", "utworów")
            "audiobook" -> Triple("audiobook", "audiobooki", "audiobooków")
            "album" -> Triple("album", "albumy", "albumów")
            "artist" -> Triple("artysta", "artystów", "artystów")
            "genre" -> Triple("gatunek", "gatunki", "gatunków")
            else -> return null
        }
        AppLanguage.Russian -> when (noun) {
            "song" -> Triple("песня", "песни", "песен")
            "track" -> Triple("трек", "трека", "треков")
            "audiobook" -> Triple("аудиокнига", "аудиокниги", "аудиокниг")
            "album" -> Triple("альбом", "альбома", "альбомов")
            "artist" -> Triple("исполнитель", "исполнителя", "исполнителей")
            "genre" -> Triple("жанр", "жанра", "жанров")
            else -> return null
        }
        AppLanguage.Serbian -> when (noun) {
            "song" -> Triple("песма", "песме", "песама")
            "track" -> Triple("нумера", "нумере", "нумера")
            "audiobook" -> Triple("аудиокњига", "аудиокњиге", "аудиокњига")
            "album" -> Triple("албум", "албума", "албума")
            "artist" -> Triple("извођач", "извођача", "извођача")
            "genre" -> Triple("жанр", "жанра", "жанрова")
            else -> return null
        }
        AppLanguage.Slovak -> when (noun) {
            "song", "track" -> Triple("skladba", "skladby", "skladieb")
            "audiobook" -> Triple("audiokniha", "audioknihy", "audiokníh")
            "album" -> Triple("album", "albumy", "albumov")
            "artist" -> Triple("interpret", "interpreti", "interpretov")
            "genre" -> Triple("žáner", "žánre", "žánrov")
            else -> return null
        }
        AppLanguage.Ukrainian -> when (noun) {
            "song" -> Triple("пісня", "пісні", "пісень")
            "track" -> Triple("трек", "треки", "треків")
            "audiobook" -> Triple("аудіокнига", "аудіокниги", "аудіокниг")
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
        language == AppLanguage.Slovenian && lastTwo == 1 -> forms.first
        language == AppLanguage.Slovenian && lastTwo == 2 -> forms.second
        language == AppLanguage.Slovenian && lastTwo in 3..4 -> when (noun) {
            "song", "track" -> "skladbe"
            "audiobook" -> "zvočne knjige"
            "album" -> "albumi"
            "artist" -> "izvajalci"
            "genre" -> "žanri"
            else -> forms.third
        }
        language == AppLanguage.Slovenian -> forms.third
        language == AppLanguage.Polish && number == 1 -> forms.first
        language == AppLanguage.Polish && last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Polish -> forms.third
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number == 1 -> forms.first
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) && number in 2..4 -> forms.second
        language in setOf(AppLanguage.Czech, AppLanguage.Slovak) -> forms.third
        language == AppLanguage.Latvian && (last == 0 || lastTwo in 11..19) -> forms.third
        language == AppLanguage.Lithuanian && lastTwo in 11..19 -> forms.third
        last == 1 && lastTwo != 11 -> forms.first
        language == AppLanguage.Latvian && last in 2..9 -> forms.second
        last in 2..4 && lastTwo !in 12..14 -> forms.second
        language == AppLanguage.Lithuanian && last in 2..9 -> forms.second
        else -> forms.third
    }
    return "$count $label"
}
