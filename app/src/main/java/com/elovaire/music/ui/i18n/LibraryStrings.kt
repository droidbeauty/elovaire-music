package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


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

internal data class PlaylistMainCopy(
    val smartMixes: String,
    val autoUpdating: String,
)

internal fun playlistMainCopy(language: AppLanguage): PlaylistMainCopy = PlaylistMainCopy(
    smartMixes = smartMixesLabel(language),
    autoUpdating = autoUpdatingLabel(language),
)

private fun smartMixesLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Smart miksy"
    AppLanguage.Albanian -> "Përzierje smart"
    AppLanguage.ChineseSimplified -> "智能混音"
    AppLanguage.Croatian -> "Pametni miksevi"
    AppLanguage.Czech -> "Chytré mixy"
    AppLanguage.Danish -> "Smarte miks"
    AppLanguage.Dutch -> "Slimme mixes"
    AppLanguage.Estonian -> "Nutikad miksid"
    AppLanguage.French -> "Mix intelligents"
    AppLanguage.German -> "Smarte Mixe"
    AppLanguage.Greek -> "Έξυπνες μίξεις"
    AppLanguage.Hindi -> "स्मार्ट मिक्स"
    AppLanguage.Hungarian -> "Okos mixek"
    AppLanguage.Italian -> "Mix smart"
    AppLanguage.Japanese -> "スマートミックス"
    AppLanguage.Latin -> "Mixturae callidae"
    AppLanguage.Latvian -> "Viedie miksi"
    AppLanguage.Lithuanian -> "Išmanūs miksai"
    AppLanguage.Macedonian -> "Паметни миксови"
    AppLanguage.Norwegian -> "Smarte mikser"
    AppLanguage.Portuguese -> "Misturas inteligentes"
    AppLanguage.Russian -> "Умные миксы"
    AppLanguage.Serbian -> "Паметни миксеви"
    AppLanguage.Spanish -> "Mezclas inteligentes"
    AppLanguage.Swedish -> "Smarta mixar"
    AppLanguage.Thai -> "มิกซ์อัจฉริยะ"
    AppLanguage.Ukrainian -> "Розумні мікси"
    AppLanguage.Slovak -> "Smart mixy"
    AppLanguage.Korean -> "스마트 믹스"
    AppLanguage.Malay -> "Campuran pintar"
    AppLanguage.Bengali -> "স্মার্ট মিক্স"
    AppLanguage.Urdu -> "اسمارٹ مکسز"
    AppLanguage.English -> "Smart mixes"
}

private fun autoUpdatingLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Aktualizuje się automatycznie"
    AppLanguage.Albanian -> "Përditësohet automatikisht"
    AppLanguage.ChineseSimplified -> "自动更新"
    AppLanguage.Croatian -> "Automatski se ažurira"
    AppLanguage.Czech -> "Automaticky se aktualizuje"
    AppLanguage.Danish -> "Opdateres automatisk"
    AppLanguage.Dutch -> "Wordt automatisch bijgewerkt"
    AppLanguage.Estonian -> "Uueneb automaatselt"
    AppLanguage.French -> "Mise à jour automatique"
    AppLanguage.German -> "Aktualisiert sich automatisch"
    AppLanguage.Greek -> "Ενημερώνεται αυτόματα"
    AppLanguage.Hindi -> "अपने आप अपडेट होता है"
    AppLanguage.Hungarian -> "Automatikusan frissül"
    AppLanguage.Italian -> "Si aggiorna automaticamente"
    AppLanguage.Japanese -> "自動更新"
    AppLanguage.Latin -> "Sponte renovatur"
    AppLanguage.Latvian -> "Atjauninās automātiski"
    AppLanguage.Lithuanian -> "Atnaujinama automatiškai"
    AppLanguage.Macedonian -> "Се ажурира автоматски"
    AppLanguage.Norwegian -> "Oppdateres automatisk"
    AppLanguage.Portuguese -> "Atualiza automaticamente"
    AppLanguage.Russian -> "Обновляется автоматически"
    AppLanguage.Serbian -> "Аутоматски се ажурира"
    AppLanguage.Spanish -> "Se actualiza automáticamente"
    AppLanguage.Swedish -> "Uppdateras automatiskt"
    AppLanguage.Thai -> "อัปเดตอัตโนมัติ"
    AppLanguage.Ukrainian -> "Оновлюється автоматично"
    AppLanguage.Slovak -> "Aktualizuje sa automaticky"
    AppLanguage.Korean -> "자동 업데이트"
    AppLanguage.Malay -> "Dikemas kini automatik"
    AppLanguage.Bengali -> "স্বয়ংক্রিয়ভাবে আপডেট হয়"
    AppLanguage.Urdu -> "خودکار طور پر اپ ڈیٹ ہوتا ہے"
    AppLanguage.English -> "Auto-updating"
}

internal fun smartPlaylistSortLabel(language: AppLanguage, field: SmartPlaylistSortField): String {
    return when (field) {
        SmartPlaylistSortField.Title -> sortedByLabel(language, titleName(language))
        SmartPlaylistSortField.Artist -> sortedByLabel(language, artistName(language))
        SmartPlaylistSortField.Album -> sortedByLabel(language, albumName(language))
        SmartPlaylistSortField.Genre -> sortedByLabel(language, genreName(language))
        SmartPlaylistSortField.Duration -> sortedByLabel(language, durationName(language))
        SmartPlaylistSortField.DateAdded -> sortedByLabel(language, dateAddedName(language))
        SmartPlaylistSortField.PlayCount -> sortedByLabel(language, playCountName(language))
        SmartPlaylistSortField.Random -> randomOrderLabel(language)
    }
}

private fun sortedByLabel(language: AppLanguage, field: String): String = when (language) {
    AppLanguage.Polish -> "Sortowane według: $field"
    AppLanguage.Albanian -> "Renditur sipas: $field"
    AppLanguage.ChineseSimplified -> "排序依据：$field"
    AppLanguage.Croatian -> "Sortirano po: $field"
    AppLanguage.Czech -> "Seřazeno podle: $field"
    AppLanguage.Danish -> "Sorteret efter: $field"
    AppLanguage.Dutch -> "Gesorteerd op: $field"
    AppLanguage.Estonian -> "Sorditud: $field"
    AppLanguage.French -> "Trié par : $field"
    AppLanguage.German -> "Sortiert nach: $field"
    AppLanguage.Greek -> "Ταξινόμηση κατά: $field"
    AppLanguage.Hindi -> "इसके अनुसार क्रमबद्ध: $field"
    AppLanguage.Hungarian -> "Rendezés: $field"
    AppLanguage.Italian -> "Ordinato per: $field"
    AppLanguage.Japanese -> "並び替え: $field"
    AppLanguage.Latin -> "Ordinem secundum: $field"
    AppLanguage.Latvian -> "Kārtots pēc: $field"
    AppLanguage.Lithuanian -> "Rikiuojama pagal: $field"
    AppLanguage.Macedonian -> "Сортирано според: $field"
    AppLanguage.Norwegian -> "Sortert etter: $field"
    AppLanguage.Portuguese -> "Ordenado por: $field"
    AppLanguage.Russian -> "Сортировка: $field"
    AppLanguage.Serbian -> "Сортирано по: $field"
    AppLanguage.Spanish -> "Ordenado por: $field"
    AppLanguage.Swedish -> "Sorterat efter: $field"
    AppLanguage.Thai -> "จัดเรียงตาม: $field"
    AppLanguage.Ukrainian -> "Сортування: $field"
    AppLanguage.Slovak -> "Zoradené podľa: $field"
    AppLanguage.Korean -> "정렬 기준: $field"
    AppLanguage.Malay -> "Diisih mengikut: $field"
    AppLanguage.Bengali -> "যেভাবে সাজানো: $field"
    AppLanguage.Urdu -> "ترتیب: $field"
    AppLanguage.English -> "Sorted by $field"
}

private fun titleName(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "tytuł"
    AppLanguage.ChineseSimplified -> "标题"
    AppLanguage.Korean -> "제목"
    AppLanguage.Malay -> "tajuk"
    AppLanguage.Bengali -> "শিরোনাম"
    AppLanguage.Urdu -> "عنوان"
    else -> "title"
}

private fun artistName(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "wykonawca"
    AppLanguage.ChineseSimplified -> "艺人"
    AppLanguage.Korean -> "아티스트"
    AppLanguage.Malay -> "artis"
    AppLanguage.Bengali -> "শিল্পী"
    AppLanguage.Urdu -> "آرٹسٹ"
    else -> "artist"
}

private fun albumName(language: AppLanguage): String = commonUiCopy(language).albums.lowercase()

private fun genreName(language: AppLanguage): String = commonUiCopy(language).genres.lowercase()

private fun durationName(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "czas trwania"
    AppLanguage.ChineseSimplified -> "时长"
    AppLanguage.Korean -> "길이"
    AppLanguage.Malay -> "tempoh"
    AppLanguage.Bengali -> "সময়কাল"
    AppLanguage.Urdu -> "دورانیہ"
    else -> "duration"
}

private fun dateAddedName(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "data dodania"
    AppLanguage.ChineseSimplified -> "添加日期"
    AppLanguage.Korean -> "추가 날짜"
    AppLanguage.Malay -> "tarikh ditambah"
    AppLanguage.Bengali -> "যোগ করার তারিখ"
    AppLanguage.Urdu -> "شامل کرنے کی تاریخ"
    else -> "date added"
}

private fun playCountName(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "liczba odtworzeń"
    AppLanguage.ChineseSimplified -> "播放次数"
    AppLanguage.Korean -> "재생 횟수"
    AppLanguage.Malay -> "bilangan main"
    AppLanguage.Bengali -> "চালানোর সংখ্যা"
    AppLanguage.Urdu -> "چلانے کی تعداد"
    else -> "play count"
}

private fun randomOrderLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Polish -> "Losowa kolejność"
    AppLanguage.Albanian -> "Rend i rastësishëm"
    AppLanguage.ChineseSimplified -> "随机顺序"
    AppLanguage.Croatian -> "Nasumičan redoslijed"
    AppLanguage.Czech -> "Náhodné pořadí"
    AppLanguage.Danish -> "Tilfældig rækkefølge"
    AppLanguage.Dutch -> "Willekeurige volgorde"
    AppLanguage.Estonian -> "Juhuslik järjestus"
    AppLanguage.French -> "Ordre aléatoire"
    AppLanguage.German -> "Zufällige Reihenfolge"
    AppLanguage.Greek -> "Τυχαία σειρά"
    AppLanguage.Hindi -> "यादृच्छिक क्रम"
    AppLanguage.Hungarian -> "Véletlen sorrend"
    AppLanguage.Italian -> "Ordine casuale"
    AppLanguage.Japanese -> "ランダム順"
    AppLanguage.Latin -> "Ordo fortuitus"
    AppLanguage.Latvian -> "Nejauša secība"
    AppLanguage.Lithuanian -> "Atsitiktinė tvarka"
    AppLanguage.Macedonian -> "Случаен редослед"
    AppLanguage.Norwegian -> "Tilfeldig rekkefølge"
    AppLanguage.Portuguese -> "Ordem aleatória"
    AppLanguage.Russian -> "Случайный порядок"
    AppLanguage.Serbian -> "Насумичан редослед"
    AppLanguage.Spanish -> "Orden aleatorio"
    AppLanguage.Swedish -> "Slumpmässig ordning"
    AppLanguage.Thai -> "ลำดับแบบสุ่ม"
    AppLanguage.Ukrainian -> "Випадковий порядок"
    AppLanguage.Slovak -> "Náhodné poradie"
    AppLanguage.Korean -> "무작위 순서"
    AppLanguage.Malay -> "Susunan rawak"
    AppLanguage.Bengali -> "এলোমেলো ক্রম"
    AppLanguage.Urdu -> "بے ترتیب ترتیب"
    AppLanguage.English -> "Random order"
}

