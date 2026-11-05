package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class SmartPlaylistActionCopy(val shuffle: String, val delete: String)

internal fun smartPlaylistActionCopy(language: AppLanguage): SmartPlaylistActionCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistActionCopy("Përziej listën inteligjente", "Fshi listën inteligjente")
    AppLanguage.Bengali -> SmartPlaylistActionCopy("স্মার্ট প্লেলিস্ট এলোমেলোভাবে চালান", "স্মার্ট প্লেলিস্ট মুছুন")
    AppLanguage.Bosnian -> SmartPlaylistActionCopy("Nasumično reproduciraj pametnu plejlistu", "Izbriši pametnu plejlistu")
    AppLanguage.Bulgarian -> SmartPlaylistActionCopy("Разбъркай интелигентния плейлист", "Изтрий интелигентния плейлист")
    AppLanguage.Catalan -> SmartPlaylistActionCopy("Reprodueix aleatòriament la llista intel·ligent", "Suprimeix la llista intel·ligent")
    AppLanguage.ChineseSimplified -> SmartPlaylistActionCopy("随机播放智能播放列表", "删除智能播放列表")
    AppLanguage.Croatian -> SmartPlaylistActionCopy("Nasumično reproduciraj pametnu playlistu", "Izbriši pametnu playlistu")
    AppLanguage.Czech -> SmartPlaylistActionCopy("Náhodně přehrát chytrý playlist", "Smazat chytrý playlist")
    AppLanguage.Danish -> SmartPlaylistActionCopy("Bland smart afspilningsliste", "Slet smart afspilningsliste")
    AppLanguage.Dutch -> SmartPlaylistActionCopy("Slimme afspeellijst in willekeurige volgorde afspelen", "Slimme afspeellijst verwijderen")
    AppLanguage.English -> SmartPlaylistActionCopy("Shuffle smart playlist", "Delete smart playlist")
    AppLanguage.Estonian -> SmartPlaylistActionCopy("Esita nutikat esitusloendit juhuslikult", "Kustuta nutikas esitusloend")
    AppLanguage.Finnish -> SmartPlaylistActionCopy("Sekoita älykäs soittolista", "Poista älykäs soittolista")
    AppLanguage.French -> SmartPlaylistActionCopy("Lire la playlist intelligente aléatoirement", "Supprimer la playlist intelligente")
    AppLanguage.German -> SmartPlaylistActionCopy("Smarte Playlist zufällig abspielen", "Smarte Playlist löschen")
    AppLanguage.Greek -> SmartPlaylistActionCopy("Τυχαία αναπαραγωγή έξυπνης λίστας", "Διαγραφή έξυπνης λίστας")
    AppLanguage.Hindi -> SmartPlaylistActionCopy("स्मार्ट प्लेलिस्ट शफ़ल करें", "स्मार्ट प्लेलिस्ट मिटाएँ")
    AppLanguage.Hungarian -> SmartPlaylistActionCopy("Intelligens lejátszási lista véletlen lejátszása", "Intelligens lejátszási lista törlése")
    AppLanguage.Indonesian -> SmartPlaylistActionCopy("Acak daftar putar pintar", "Hapus daftar putar pintar")
    AppLanguage.Italian -> SmartPlaylistActionCopy("Riproduci casualmente la playlist intelligente", "Elimina playlist intelligente")
    AppLanguage.Japanese -> SmartPlaylistActionCopy("スマートプレイリストをシャッフル", "スマートプレイリストを削除")
    AppLanguage.Korean -> SmartPlaylistActionCopy("스마트 재생목록 셔플", "스마트 재생목록 삭제")
    AppLanguage.Latvian -> SmartPlaylistActionCopy("Atskaņot viedo sarakstu jauktā secībā", "Dzēst viedo atskaņošanas sarakstu")
    AppLanguage.Lithuanian -> SmartPlaylistActionCopy("Maišyti išmanųjį grojaraštį", "Ištrinti išmanųjį grojaraštį")
    AppLanguage.Macedonian -> SmartPlaylistActionCopy("Измешај паметна плејлиста", "Избриши паметна плејлиста")
    AppLanguage.Malay -> SmartPlaylistActionCopy("Rombak senarai main pintar", "Padam senarai main pintar")
    AppLanguage.Norwegian -> SmartPlaylistActionCopy("Spill smart spilleliste i tilfeldig rekkefølge", "Slett smart spilleliste")
    AppLanguage.Polish -> SmartPlaylistActionCopy("Odtwórz inteligentną playlistę losowo", "Usuń inteligentną playlistę")
    AppLanguage.Portuguese -> SmartPlaylistActionCopy("Reproduzir playlist inteligente aleatoriamente", "Eliminar playlist inteligente")
    AppLanguage.Punjabi -> SmartPlaylistActionCopy("ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਬੇਤਰਤੀਬੇ ਚਲਾਓ", "ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਮਿਟਾਓ")
    AppLanguage.Romanian -> SmartPlaylistActionCopy("Redă aleatoriu lista inteligentă", "Șterge lista inteligentă")
    AppLanguage.Russian -> SmartPlaylistActionCopy("Перемешать смарт-плейлист", "Удалить смарт-плейлист")
    AppLanguage.Serbian -> SmartPlaylistActionCopy("Насумично репродукуј паметну плејлисту", "Избриши паметну плејлисту")
    AppLanguage.Slovak -> SmartPlaylistActionCopy("Náhodne prehrať inteligentný playlist", "Odstrániť inteligentný playlist")
    AppLanguage.Slovenian -> SmartPlaylistActionCopy("Predvajaj pametni seznam naključno", "Izbriši pametni seznam predvajanja")
    AppLanguage.Spanish -> SmartPlaylistActionCopy("Reproducir lista inteligente aleatoriamente", "Eliminar lista inteligente")
    AppLanguage.Swedish -> SmartPlaylistActionCopy("Blanda smart spellista", "Ta bort smart spellista")
    AppLanguage.Thai -> SmartPlaylistActionCopy("สุ่มเล่นเพลย์ลิสต์อัจฉริยะ", "ลบเพลย์ลิสต์อัจฉริยะ")
    AppLanguage.Ukrainian -> SmartPlaylistActionCopy("Перемішати розумний плейлист", "Видалити розумний плейлист")
    AppLanguage.Urdu -> SmartPlaylistActionCopy("اسمارٹ پلے لسٹ بے ترتیب چلائیں", "اسمارٹ پلے لسٹ حذف کریں")
}

internal data class SmartPlaylistMatchCopy(val mode: String, val all: String, val any: String, val add: String)

internal fun smartPlaylistMatchCopy(language: AppLanguage): SmartPlaylistMatchCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistMatchCopy("Mënyra e përputhjes", "Përputh të gjitha rregullat", "Përputh të paktën një rregull", "Shto rregull")
    AppLanguage.Bengali -> SmartPlaylistMatchCopy("মেলানোর ধরন", "সব নিয়ম মেলান", "যেকোনো একটি নিয়ম মেলান", "নিয়ম যোগ করুন")
    AppLanguage.Bosnian -> SmartPlaylistMatchCopy("Način podudaranja", "Sva pravila moraju odgovarati", "Bilo koje pravilo može odgovarati", "Dodaj pravilo")
    AppLanguage.Bulgarian -> SmartPlaylistMatchCopy("Начин на съвпадение", "Всички правила", "Кое да е правило", "Добави правило")
    AppLanguage.Catalan -> SmartPlaylistMatchCopy("Mode de coincidència", "Compleix totes les regles", "Compleix qualsevol regla", "Afegeix una regla")
    AppLanguage.ChineseSimplified -> SmartPlaylistMatchCopy("匹配方式", "匹配所有规则", "匹配任一规则", "添加规则")
    AppLanguage.Croatian -> SmartPlaylistMatchCopy("Način podudaranja", "Sva pravila moraju odgovarati", "Bilo koje pravilo može odgovarati", "Dodaj pravilo")
    AppLanguage.Czech -> SmartPlaylistMatchCopy("Režim shody", "Splnit všechna pravidla", "Splnit libovolné pravidlo", "Přidat pravidlo")
    AppLanguage.Danish -> SmartPlaylistMatchCopy("Matchtype", "Match alle regler", "Match mindst én regel", "Tilføj regel")
    AppLanguage.Dutch -> SmartPlaylistMatchCopy("Overeenkomst", "Voldoe aan alle regels", "Voldoe aan minstens één regel", "Regel toevoegen")
    AppLanguage.English -> SmartPlaylistMatchCopy("Match mode", "Match all rules", "Match any rule", "Add rule")
    AppLanguage.Estonian -> SmartPlaylistMatchCopy("Vastavus", "Vasta kõigile reeglitele", "Vasta vähemalt ühele reeglile", "Lisa reegel")
    AppLanguage.Finnish -> SmartPlaylistMatchCopy("Vastaavuus", "Vastaa kaikkia sääntöjä", "Vastaa mitä tahansa sääntöä", "Lisää sääntö")
    AppLanguage.French -> SmartPlaylistMatchCopy("Correspondance", "Respecter toutes les règles", "Respecter au moins une règle", "Ajouter une règle")
    AppLanguage.German -> SmartPlaylistMatchCopy("Übereinstimmung", "Alle Regeln erfüllen", "Mindestens eine Regel erfüllen", "Regel hinzufügen")
    AppLanguage.Greek -> SmartPlaylistMatchCopy("Τρόπος αντιστοίχισης", "Όλοι οι κανόνες", "Οποιοσδήποτε κανόνας", "Προσθήκη κανόνα")
    AppLanguage.Hindi -> SmartPlaylistMatchCopy("मिलान का तरीका", "सभी नियम मिलें", "कोई भी एक नियम मिले", "नियम जोड़ें")
    AppLanguage.Hungarian -> SmartPlaylistMatchCopy("Egyezés módja", "Minden szabálynak megfelel", "Legalább egy szabálynak megfelel", "Szabály hozzáadása")
    AppLanguage.Indonesian -> SmartPlaylistMatchCopy("Mode pencocokan", "Cocokkan semua aturan", "Cocokkan salah satu aturan", "Tambahkan aturan")
    AppLanguage.Italian -> SmartPlaylistMatchCopy("Corrispondenza", "Rispetta tutte le regole", "Rispetta almeno una regola", "Aggiungi regola")
    AppLanguage.Japanese -> SmartPlaylistMatchCopy("一致条件", "すべてのルールに一致", "いずれかのルールに一致", "ルールを追加")
    AppLanguage.Korean -> SmartPlaylistMatchCopy("일치 방식", "모든 규칙과 일치", "하나 이상의 규칙과 일치", "규칙 추가")
    AppLanguage.Latvian -> SmartPlaylistMatchCopy("Atbilstības veids", "Atbilst visiem noteikumiem", "Atbilst vismaz vienam noteikumam", "Pievienot noteikumu")
    AppLanguage.Lithuanian -> SmartPlaylistMatchCopy("Atitikties būdas", "Atitikti visas taisykles", "Atitikti bent vieną taisyklę", "Pridėti taisyklę")
    AppLanguage.Macedonian -> SmartPlaylistMatchCopy("Начин на совпаѓање", "Да одговараат сите правила", "Да одговара кое било правило", "Додај правило")
    AppLanguage.Malay -> SmartPlaylistMatchCopy("Mod padanan", "Padankan semua peraturan", "Padankan mana-mana peraturan", "Tambah peraturan")
    AppLanguage.Norwegian -> SmartPlaylistMatchCopy("Samsvar", "Samsvar med alle regler", "Samsvar med minst én regel", "Legg til regel")
    AppLanguage.Polish -> SmartPlaylistMatchCopy("Tryb dopasowania", "Spełnia wszystkie reguły", "Spełnia dowolną regułę", "Dodaj regułę")
    AppLanguage.Portuguese -> SmartPlaylistMatchCopy("Modo de correspondência", "Cumprir todas as regras", "Cumprir pelo menos uma regra", "Adicionar regra")
    AppLanguage.Punjabi -> SmartPlaylistMatchCopy("ਮੇਲ ਦਾ ਤਰੀਕਾ", "ਸਾਰੇ ਨਿਯਮ ਮਿਲਣ", "ਕੋਈ ਵੀ ਇੱਕ ਨਿਯਮ ਮਿਲੇ", "ਨਿਯਮ ਜੋੜੋ")
    AppLanguage.Romanian -> SmartPlaylistMatchCopy("Mod de potrivire", "Respectă toate regulile", "Respectă cel puțin o regulă", "Adaugă regulă")
    AppLanguage.Russian -> SmartPlaylistMatchCopy("Режим соответствия", "Все правила", "Любое правило", "Добавить правило")
    AppLanguage.Serbian -> SmartPlaylistMatchCopy("Начин подударања", "Морају да одговарају сва правила", "Може да одговара било које правило", "Додај правило")
    AppLanguage.Slovak -> SmartPlaylistMatchCopy("Režim zhody", "Splniť všetky pravidlá", "Splniť ľubovoľné pravidlo", "Pridať pravidlo")
    AppLanguage.Slovenian -> SmartPlaylistMatchCopy("Način ujemanja", "Ustrezaj vsem pravilom", "Ustrezaj vsaj enemu pravilu", "Dodaj pravilo")
    AppLanguage.Spanish -> SmartPlaylistMatchCopy("Modo de coincidencia", "Cumplir todas las reglas", "Cumplir al menos una regla", "Añadir regla")
    AppLanguage.Swedish -> SmartPlaylistMatchCopy("Matchning", "Matcha alla regler", "Matcha minst en regel", "Lägg till regel")
    AppLanguage.Thai -> SmartPlaylistMatchCopy("วิธีจับคู่", "ตรงกับทุกกฎ", "ตรงกับอย่างน้อยหนึ่งกฎ", "เพิ่มกฎ")
    AppLanguage.Ukrainian -> SmartPlaylistMatchCopy("Режим відповідності", "Усі правила", "Будь-яке правило", "Додати правило")
    AppLanguage.Urdu -> SmartPlaylistMatchCopy("مطابقت کا طریقہ", "تمام اصول ملیں", "کوئی بھی ایک اصول ملے", "اصول شامل کریں")
}

internal fun smartPlaylistRemoveRuleLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Hiq rregullin"
    AppLanguage.Bengali -> "নিয়ম সরান"
    AppLanguage.Bosnian -> "Ukloni pravilo"
    AppLanguage.Bulgarian -> "Премахни правилото"
    AppLanguage.Catalan -> "Treu la regla"
    AppLanguage.ChineseSimplified -> "移除规则"
    AppLanguage.Croatian -> "Ukloni pravilo"
    AppLanguage.Czech -> "Odebrat pravidlo"
    AppLanguage.Danish -> "Fjern regel"
    AppLanguage.Dutch -> "Regel verwijderen"
    AppLanguage.English -> "Remove rule"
    AppLanguage.Estonian -> "Eemalda reegel"
    AppLanguage.Finnish -> "Poista sääntö"
    AppLanguage.French -> "Retirer la règle"
    AppLanguage.German -> "Regel entfernen"
    AppLanguage.Greek -> "Αφαίρεση κανόνα"
    AppLanguage.Hindi -> "नियम हटाएँ"
    AppLanguage.Hungarian -> "Szabály eltávolítása"
    AppLanguage.Indonesian -> "Hapus aturan"
    AppLanguage.Italian -> "Rimuovi regola"
    AppLanguage.Japanese -> "ルールを削除"
    AppLanguage.Korean -> "규칙 삭제"
    AppLanguage.Latvian -> "Noņemt noteikumu"
    AppLanguage.Lithuanian -> "Pašalinti taisyklę"
    AppLanguage.Macedonian -> "Отстрани правило"
    AppLanguage.Malay -> "Buang peraturan"
    AppLanguage.Norwegian -> "Fjern regel"
    AppLanguage.Polish -> "Usuń regułę"
    AppLanguage.Portuguese -> "Remover regra"
    AppLanguage.Punjabi -> "ਨਿਯਮ ਹਟਾਓ"
    AppLanguage.Romanian -> "Elimină regula"
    AppLanguage.Russian -> "Удалить правило"
    AppLanguage.Serbian -> "Уклони правило"
    AppLanguage.Slovak -> "Odstrániť pravidlo"
    AppLanguage.Slovenian -> "Odstrani pravilo"
    AppLanguage.Spanish -> "Eliminar regla"
    AppLanguage.Swedish -> "Ta bort regel"
    AppLanguage.Thai -> "ลบกฎ"
    AppLanguage.Ukrainian -> "Видалити правило"
    AppLanguage.Urdu -> "اصول ہٹائیں"
}

internal data class SmartPlaylistEditorHeadingCopy(val create: String, val edit: String, val save: String)

internal fun smartPlaylistEditorHeadingCopy(language: AppLanguage): SmartPlaylistEditorHeadingCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistEditorHeadingCopy("Listë e re inteligjente", "Ndrysho listën inteligjente", "Ruaj listën inteligjente")
    AppLanguage.Bengali -> SmartPlaylistEditorHeadingCopy("নতুন স্মার্ট প্লেলিস্ট", "স্মার্ট প্লেলিস্ট সম্পাদনা", "স্মার্ট প্লেলিস্ট সংরক্ষণ করুন")
    AppLanguage.Bosnian -> SmartPlaylistEditorHeadingCopy("Nova pametna plejlista", "Uredi pametnu plejlistu", "Sačuvaj pametnu plejlistu")
    AppLanguage.Bulgarian -> SmartPlaylistEditorHeadingCopy("Нов интелигентен плейлист", "Редактиране на интелигентния плейлист", "Запази интелигентния плейлист")
    AppLanguage.Catalan -> SmartPlaylistEditorHeadingCopy("Llista intel·ligent nova", "Edita la llista intel·ligent", "Desa la llista intel·ligent")
    AppLanguage.ChineseSimplified -> SmartPlaylistEditorHeadingCopy("新建智能播放列表", "编辑智能播放列表", "保存智能播放列表")
    AppLanguage.Croatian -> SmartPlaylistEditorHeadingCopy("Nova pametna playlista", "Uredi pametnu playlistu", "Spremi pametnu playlistu")
    AppLanguage.Czech -> SmartPlaylistEditorHeadingCopy("Nový chytrý playlist", "Upravit chytrý playlist", "Uložit chytrý playlist")
    AppLanguage.Danish -> SmartPlaylistEditorHeadingCopy("Ny smart afspilningsliste", "Rediger smart afspilningsliste", "Gem smart afspilningsliste")
    AppLanguage.Dutch -> SmartPlaylistEditorHeadingCopy("Nieuwe slimme afspeellijst", "Slimme afspeellijst bewerken", "Slimme afspeellijst opslaan")
    AppLanguage.English -> SmartPlaylistEditorHeadingCopy("New smart playlist", "Edit smart playlist", "Save smart playlist")
    AppLanguage.Estonian -> SmartPlaylistEditorHeadingCopy("Uus nutikas esitusloend", "Muuda nutikat esitusloendit", "Salvesta nutikas esitusloend")
    AppLanguage.Finnish -> SmartPlaylistEditorHeadingCopy("Uusi älykäs soittolista", "Muokkaa älykästä soittolistaa", "Tallenna älykäs soittolista")
    AppLanguage.French -> SmartPlaylistEditorHeadingCopy("Nouvelle playlist intelligente", "Modifier la playlist intelligente", "Enregistrer la playlist intelligente")
    AppLanguage.German -> SmartPlaylistEditorHeadingCopy("Neue smarte Playlist", "Smarte Playlist bearbeiten", "Smarte Playlist speichern")
    AppLanguage.Greek -> SmartPlaylistEditorHeadingCopy("Νέα έξυπνη λίστα αναπαραγωγής", "Επεξεργασία έξυπνης λίστας", "Αποθήκευση έξυπνης λίστας")
    AppLanguage.Hindi -> SmartPlaylistEditorHeadingCopy("नई स्मार्ट प्लेलिस्ट", "स्मार्ट प्लेलिस्ट संपादित करें", "स्मार्ट प्लेलिस्ट सहेजें")
    AppLanguage.Hungarian -> SmartPlaylistEditorHeadingCopy("Új intelligens lejátszási lista", "Intelligens lejátszási lista szerkesztése", "Intelligens lejátszási lista mentése")
    AppLanguage.Indonesian -> SmartPlaylistEditorHeadingCopy("Daftar putar pintar baru", "Edit daftar putar pintar", "Simpan daftar putar pintar")
    AppLanguage.Italian -> SmartPlaylistEditorHeadingCopy("Nuova playlist intelligente", "Modifica playlist intelligente", "Salva playlist intelligente")
    AppLanguage.Japanese -> SmartPlaylistEditorHeadingCopy("新しいスマートプレイリスト", "スマートプレイリストを編集", "スマートプレイリストを保存")
    AppLanguage.Korean -> SmartPlaylistEditorHeadingCopy("새 스마트 재생목록", "스마트 재생목록 수정", "스마트 재생목록 저장")
    AppLanguage.Latvian -> SmartPlaylistEditorHeadingCopy("Jauns viedais atskaņošanas saraksts", "Rediģēt viedo atskaņošanas sarakstu", "Saglabāt viedo atskaņošanas sarakstu")
    AppLanguage.Lithuanian -> SmartPlaylistEditorHeadingCopy("Naujas išmanusis grojaraštis", "Redaguoti išmanųjį grojaraštį", "Išsaugoti išmanųjį grojaraštį")
    AppLanguage.Macedonian -> SmartPlaylistEditorHeadingCopy("Нова паметна плејлиста", "Уреди паметна плејлиста", "Зачувај паметна плејлиста")
    AppLanguage.Malay -> SmartPlaylistEditorHeadingCopy("Senarai main pintar baharu", "Edit senarai main pintar", "Simpan senarai main pintar")
    AppLanguage.Norwegian -> SmartPlaylistEditorHeadingCopy("Ny smart spilleliste", "Rediger smart spilleliste", "Lagre smart spilleliste")
    AppLanguage.Polish -> SmartPlaylistEditorHeadingCopy("Nowa inteligentna playlista", "Edytuj inteligentną playlistę", "Zapisz inteligentną playlistę")
    AppLanguage.Portuguese -> SmartPlaylistEditorHeadingCopy("Nova playlist inteligente", "Editar playlist inteligente", "Guardar playlist inteligente")
    AppLanguage.Punjabi -> SmartPlaylistEditorHeadingCopy("ਨਵੀਂ ਸਮਾਰਟ ਪਲੇਲਿਸਟ", "ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਸੋਧੋ", "ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਸੁਰੱਖਿਅਤ ਕਰੋ")
    AppLanguage.Romanian -> SmartPlaylistEditorHeadingCopy("Listă inteligentă nouă", "Editează lista inteligentă", "Salvează lista inteligentă")
    AppLanguage.Russian -> SmartPlaylistEditorHeadingCopy("Новый смарт-плейлист", "Изменить смарт-плейлист", "Сохранить смарт-плейлист")
    AppLanguage.Serbian -> SmartPlaylistEditorHeadingCopy("Нова паметна плејлиста", "Уреди паметну плејлисту", "Сачувај паметну плејлисту")
    AppLanguage.Slovak -> SmartPlaylistEditorHeadingCopy("Nový inteligentný playlist", "Upraviť inteligentný playlist", "Uložiť inteligentný playlist")
    AppLanguage.Slovenian -> SmartPlaylistEditorHeadingCopy("Nov pametni seznam predvajanja", "Uredi pametni seznam predvajanja", "Shrani pametni seznam predvajanja")
    AppLanguage.Spanish -> SmartPlaylistEditorHeadingCopy("Nueva lista inteligente", "Editar lista inteligente", "Guardar lista inteligente")
    AppLanguage.Swedish -> SmartPlaylistEditorHeadingCopy("Ny smart spellista", "Redigera smart spellista", "Spara smart spellista")
    AppLanguage.Thai -> SmartPlaylistEditorHeadingCopy("เพลย์ลิสต์อัจฉริยะใหม่", "แก้ไขเพลย์ลิสต์อัจฉริยะ", "บันทึกเพลย์ลิสต์อัจฉริยะ")
    AppLanguage.Ukrainian -> SmartPlaylistEditorHeadingCopy("Новий розумний плейлист", "Редагувати розумний плейлист", "Зберегти розумний плейлист")
    AppLanguage.Urdu -> SmartPlaylistEditorHeadingCopy("نئی اسمارٹ پلے لسٹ", "اسمارٹ پلے لسٹ میں ترمیم کریں", "اسمارٹ پلے لسٹ محفوظ کریں")
}

internal fun smartPlaylistEditRulesLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Ndrysho rregullat"
    AppLanguage.Bengali -> "নিয়ম সম্পাদনা করুন"
    AppLanguage.Bosnian -> "Uredi pravila"
    AppLanguage.Bulgarian -> "Редактиране на правилата"
    AppLanguage.Catalan -> "Edita les regles"
    AppLanguage.ChineseSimplified -> "编辑规则"
    AppLanguage.Croatian -> "Uredi pravila"
    AppLanguage.Czech -> "Upravit pravidla"
    AppLanguage.Danish -> "Rediger regler"
    AppLanguage.Dutch -> "Regels bewerken"
    AppLanguage.English -> "Edit rules"
    AppLanguage.Estonian -> "Muuda reegleid"
    AppLanguage.Finnish -> "Muokkaa sääntöjä"
    AppLanguage.French -> "Modifier les règles"
    AppLanguage.German -> "Regeln bearbeiten"
    AppLanguage.Greek -> "Επεξεργασία κανόνων"
    AppLanguage.Hindi -> "नियम संपादित करें"
    AppLanguage.Hungarian -> "Szabályok szerkesztése"
    AppLanguage.Indonesian -> "Edit aturan"
    AppLanguage.Italian -> "Modifica regole"
    AppLanguage.Japanese -> "ルールを編集"
    AppLanguage.Korean -> "규칙 수정"
    AppLanguage.Latvian -> "Rediģēt noteikumus"
    AppLanguage.Lithuanian -> "Redaguoti taisykles"
    AppLanguage.Macedonian -> "Уреди правила"
    AppLanguage.Malay -> "Edit peraturan"
    AppLanguage.Norwegian -> "Rediger regler"
    AppLanguage.Polish -> "Edytuj reguły"
    AppLanguage.Portuguese -> "Editar regras"
    AppLanguage.Punjabi -> "ਨਿਯਮ ਸੋਧੋ"
    AppLanguage.Romanian -> "Editează regulile"
    AppLanguage.Russian -> "Изменить правила"
    AppLanguage.Serbian -> "Уреди правила"
    AppLanguage.Slovak -> "Upraviť pravidlá"
    AppLanguage.Slovenian -> "Uredi pravila"
    AppLanguage.Spanish -> "Editar reglas"
    AppLanguage.Swedish -> "Redigera regler"
    AppLanguage.Thai -> "แก้ไขกฎ"
    AppLanguage.Ukrainian -> "Редагувати правила"
    AppLanguage.Urdu -> "اصولوں میں ترمیم کریں"
}

internal fun smartPlaylistPreviewLabel(language: AppLanguage, count: Int): String {
    val preview = when (language) {
        AppLanguage.Albanian -> "Parapamje"
        AppLanguage.Bengali -> "পূর্বরূপ"
        AppLanguage.Bosnian -> "Pregled"
        AppLanguage.Bulgarian -> "Преглед"
        AppLanguage.Catalan -> "Previsualització"
        AppLanguage.ChineseSimplified -> "预览"
        AppLanguage.Croatian -> "Pregled"
        AppLanguage.Czech -> "Náhled"
        AppLanguage.Danish -> "Forhåndsvisning"
        AppLanguage.Dutch -> "Voorbeeld"
        AppLanguage.English -> "Preview"
        AppLanguage.Estonian -> "Eelvaade"
        AppLanguage.Finnish -> "Esikatselu"
        AppLanguage.French -> "Aperçu"
        AppLanguage.German -> "Vorschau"
        AppLanguage.Greek -> "Προεπισκόπηση"
        AppLanguage.Hindi -> "पूर्वावलोकन"
        AppLanguage.Hungarian -> "Előnézet"
        AppLanguage.Indonesian -> "Pratinjau"
        AppLanguage.Italian -> "Anteprima"
        AppLanguage.Japanese -> "プレビュー"
        AppLanguage.Korean -> "미리보기"
        AppLanguage.Latvian -> "Priekšskatījums"
        AppLanguage.Lithuanian -> "Peržiūra"
        AppLanguage.Macedonian -> "Преглед"
        AppLanguage.Malay -> "Pratonton"
        AppLanguage.Norwegian -> "Forhåndsvisning"
        AppLanguage.Polish -> "Podgląd"
        AppLanguage.Portuguese -> "Pré-visualização"
        AppLanguage.Punjabi -> "ਝਲਕ"
        AppLanguage.Romanian -> "Previzualizare"
        AppLanguage.Russian -> "Предпросмотр"
        AppLanguage.Serbian -> "Преглед"
        AppLanguage.Slovak -> "Náhľad"
        AppLanguage.Slovenian -> "Predogled"
        AppLanguage.Spanish -> "Vista previa"
        AppLanguage.Swedish -> "Förhandsvisning"
        AppLanguage.Thai -> "ตัวอย่าง"
        AppLanguage.Ukrainian -> "Попередній перегляд"
        AppLanguage.Urdu -> "پیش نظارہ"
    }
    return "$preview: ${localizedCountLabel(count, "song", language)}"
}

internal data class SmartPlaylistEmptyCopy(val title: String, val message: String)

internal data class SmartPlaylistMissingCopy(val title: String, val message: String)

internal fun smartPlaylistMissingCopy(language: AppLanguage): SmartPlaylistMissingCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistMissingCopy("Listë inteligjente", "Lista inteligjente nuk u gjet")
    AppLanguage.Bengali -> SmartPlaylistMissingCopy("স্মার্ট প্লেলিস্ট", "স্মার্ট প্লেলিস্ট পাওয়া যায়নি")
    AppLanguage.Bosnian -> SmartPlaylistMissingCopy("Pametna plejlista", "Pametna plejlista nije pronađena")
    AppLanguage.Bulgarian -> SmartPlaylistMissingCopy("Интелигентен плейлист", "Интелигентният плейлист не е намерен")
    AppLanguage.Catalan -> SmartPlaylistMissingCopy("Llista intel·ligent", "No s’ha trobat la llista intel·ligent")
    AppLanguage.ChineseSimplified -> SmartPlaylistMissingCopy("智能播放列表", "未找到智能播放列表")
    AppLanguage.Croatian -> SmartPlaylistMissingCopy("Pametna playlista", "Pametna playlista nije pronađena")
    AppLanguage.Czech -> SmartPlaylistMissingCopy("Chytrý playlist", "Chytrý playlist nebyl nalezen")
    AppLanguage.Danish -> SmartPlaylistMissingCopy("Smart afspilningsliste", "Den smarte afspilningsliste blev ikke fundet")
    AppLanguage.Dutch -> SmartPlaylistMissingCopy("Slimme afspeellijst", "Slimme afspeellijst niet gevonden")
    AppLanguage.English -> SmartPlaylistMissingCopy("Smart playlist", "Smart playlist not found")
    AppLanguage.Estonian -> SmartPlaylistMissingCopy("Nutikas esitusloend", "Nutikat esitusloendit ei leitud")
    AppLanguage.Finnish -> SmartPlaylistMissingCopy("Älykäs soittolista", "Älykästä soittolistaa ei löytynyt")
    AppLanguage.French -> SmartPlaylistMissingCopy("Playlist intelligente", "Playlist intelligente introuvable")
    AppLanguage.German -> SmartPlaylistMissingCopy("Smarte Playlist", "Smarte Playlist nicht gefunden")
    AppLanguage.Greek -> SmartPlaylistMissingCopy("Έξυπνη λίστα αναπαραγωγής", "Η έξυπνη λίστα αναπαραγωγής δεν βρέθηκε")
    AppLanguage.Hindi -> SmartPlaylistMissingCopy("स्मार्ट प्लेलिस्ट", "स्मार्ट प्लेलिस्ट नहीं मिली")
    AppLanguage.Hungarian -> SmartPlaylistMissingCopy("Intelligens lejátszási lista", "Az intelligens lejátszási lista nem található")
    AppLanguage.Indonesian -> SmartPlaylistMissingCopy("Daftar putar pintar", "Daftar putar pintar tidak ditemukan")
    AppLanguage.Italian -> SmartPlaylistMissingCopy("Playlist intelligente", "Playlist intelligente non trovata")
    AppLanguage.Japanese -> SmartPlaylistMissingCopy("スマートプレイリスト", "スマートプレイリストが見つかりません")
    AppLanguage.Korean -> SmartPlaylistMissingCopy("스마트 재생목록", "스마트 재생목록을 찾을 수 없습니다")
    AppLanguage.Latvian -> SmartPlaylistMissingCopy("Viedais atskaņošanas saraksts", "Viedais atskaņošanas saraksts nav atrasts")
    AppLanguage.Lithuanian -> SmartPlaylistMissingCopy("Išmanusis grojaraštis", "Išmanusis grojaraštis nerastas")
    AppLanguage.Macedonian -> SmartPlaylistMissingCopy("Паметна плејлиста", "Паметната плејлиста не е пронајдена")
    AppLanguage.Malay -> SmartPlaylistMissingCopy("Senarai main pintar", "Senarai main pintar tidak ditemui")
    AppLanguage.Norwegian -> SmartPlaylistMissingCopy("Smart spilleliste", "Fant ikke den smarte spillelisten")
    AppLanguage.Polish -> SmartPlaylistMissingCopy("Inteligentna playlista", "Nie znaleziono inteligentnej playlisty")
    AppLanguage.Portuguese -> SmartPlaylistMissingCopy("Playlist inteligente", "Playlist inteligente não encontrada")
    AppLanguage.Punjabi -> SmartPlaylistMissingCopy("ਸਮਾਰਟ ਪਲੇਲਿਸਟ", "ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਨਹੀਂ ਮਿਲੀ")
    AppLanguage.Romanian -> SmartPlaylistMissingCopy("Listă inteligentă", "Lista inteligentă nu a fost găsită")
    AppLanguage.Russian -> SmartPlaylistMissingCopy("Смарт-плейлист", "Смарт-плейлист не найден")
    AppLanguage.Serbian -> SmartPlaylistMissingCopy("Паметна плејлиста", "Паметна плејлиста није пронађена")
    AppLanguage.Slovak -> SmartPlaylistMissingCopy("Inteligentný playlist", "Inteligentný playlist sa nenašiel")
    AppLanguage.Slovenian -> SmartPlaylistMissingCopy("Pametni seznam predvajanja", "Pametnega seznama predvajanja ni mogoče najti")
    AppLanguage.Spanish -> SmartPlaylistMissingCopy("Lista inteligente", "No se ha encontrado la lista inteligente")
    AppLanguage.Swedish -> SmartPlaylistMissingCopy("Smart spellista", "Den smarta spellistan hittades inte")
    AppLanguage.Thai -> SmartPlaylistMissingCopy("เพลย์ลิสต์อัจฉริยะ", "ไม่พบเพลย์ลิสต์อัจฉริยะ")
    AppLanguage.Ukrainian -> SmartPlaylistMissingCopy("Розумний плейлист", "Розумний плейлист не знайдено")
    AppLanguage.Urdu -> SmartPlaylistMissingCopy("اسمارٹ پلے لسٹ", "اسمارٹ پلے لسٹ نہیں ملی")
}

internal data class SmartPlaylistDeleteCopy(val title: String, val message: String)

internal fun smartPlaylistDeleteCopy(language: AppLanguage): SmartPlaylistDeleteCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistDeleteCopy("Të fshihet lista inteligjente?", "Kjo heq vetëm rregullat e ruajtura")
    AppLanguage.Bengali -> SmartPlaylistDeleteCopy("স্মার্ট প্লেলিস্ট মুছবেন?", "এতে শুধু সংরক্ষিত নিয়মগুলি সরানো হবে")
    AppLanguage.Bosnian -> SmartPlaylistDeleteCopy("Izbrisati pametnu plejlistu?", "Ovo uklanja samo sačuvana pravila")
    AppLanguage.Bulgarian -> SmartPlaylistDeleteCopy("Изтриване на интелигентния плейлист?", "Това премахва само запазените правила")
    AppLanguage.Catalan -> SmartPlaylistDeleteCopy("Vols suprimir la llista intel·ligent?", "Això només elimina les regles desades")
    AppLanguage.ChineseSimplified -> SmartPlaylistDeleteCopy("删除智能播放列表？", "这只会移除已保存的规则")
    AppLanguage.Croatian -> SmartPlaylistDeleteCopy("Izbrisati pametnu playlistu?", "Time se uklanjaju samo spremljena pravila")
    AppLanguage.Czech -> SmartPlaylistDeleteCopy("Smazat chytrý playlist?", "Tím odstraníte pouze uložená pravidla")
    AppLanguage.Danish -> SmartPlaylistDeleteCopy("Slet smart afspilningsliste?", "Dette fjerner kun de gemte regler")
    AppLanguage.Dutch -> SmartPlaylistDeleteCopy("Slimme afspeellijst verwijderen?", "Hiermee verwijder je alleen de opgeslagen regels")
    AppLanguage.English -> SmartPlaylistDeleteCopy("Delete smart playlist?", "This removes only the saved rules")
    AppLanguage.Estonian -> SmartPlaylistDeleteCopy("Kas kustutada nutikas esitusloend?", "See eemaldab ainult salvestatud reeglid")
    AppLanguage.Finnish -> SmartPlaylistDeleteCopy("Poistetaanko älykäs soittolista?", "Tämä poistaa vain tallennetut säännöt")
    AppLanguage.French -> SmartPlaylistDeleteCopy("Supprimer la playlist intelligente ?", "Seules les règles enregistrées seront supprimées")
    AppLanguage.German -> SmartPlaylistDeleteCopy("Smarte Playlist löschen?", "Dadurch werden nur die gespeicherten Regeln entfernt")
    AppLanguage.Greek -> SmartPlaylistDeleteCopy("Διαγραφή έξυπνης λίστας αναπαραγωγής;", "Αυτό αφαιρεί μόνο τους αποθηκευμένους κανόνες")
    AppLanguage.Hindi -> SmartPlaylistDeleteCopy("स्मार्ट प्लेलिस्ट मिटाएँ?", "इससे केवल सहेजे गए नियम हटेंगे")
    AppLanguage.Hungarian -> SmartPlaylistDeleteCopy("Törlöd az intelligens lejátszási listát?", "Ez csak a mentett szabályokat távolítja el")
    AppLanguage.Indonesian -> SmartPlaylistDeleteCopy("Hapus daftar putar pintar?", "Hanya aturan yang disimpan yang akan dihapus")
    AppLanguage.Italian -> SmartPlaylistDeleteCopy("Eliminare la playlist intelligente?", "Verranno rimosse solo le regole salvate")
    AppLanguage.Japanese -> SmartPlaylistDeleteCopy("スマートプレイリストを削除しますか？", "保存したルールのみが削除されます")
    AppLanguage.Korean -> SmartPlaylistDeleteCopy("스마트 재생목록을 삭제할까요?", "저장된 규칙만 삭제됩니다")
    AppLanguage.Latvian -> SmartPlaylistDeleteCopy("Dzēst viedo atskaņošanas sarakstu?", "Tiks noņemti tikai saglabātie noteikumi")
    AppLanguage.Lithuanian -> SmartPlaylistDeleteCopy("Ištrinti išmanųjį grojaraštį?", "Bus pašalintos tik išsaugotos taisyklės")
    AppLanguage.Macedonian -> SmartPlaylistDeleteCopy("Да се избрише паметната плејлиста?", "Со ова се отстрануваат само зачуваните правила")
    AppLanguage.Malay -> SmartPlaylistDeleteCopy("Padam senarai main pintar?", "Ini hanya membuang peraturan yang disimpan")
    AppLanguage.Norwegian -> SmartPlaylistDeleteCopy("Slette smart spilleliste?", "Dette fjerner bare de lagrede reglene")
    AppLanguage.Polish -> SmartPlaylistDeleteCopy("Usunąć inteligentną playlistę?", "Usunięte zostaną tylko zapisane reguły")
    AppLanguage.Portuguese -> SmartPlaylistDeleteCopy("Eliminar playlist inteligente?", "Isto remove apenas as regras guardadas")
    AppLanguage.Punjabi -> SmartPlaylistDeleteCopy("ਸਮਾਰਟ ਪਲੇਲਿਸਟ ਮਿਟਾਉਣੀ ਹੈ?", "ਇਸ ਨਾਲ ਸਿਰਫ਼ ਸੁਰੱਖਿਅਤ ਕੀਤੇ ਨਿਯਮ ਹਟਣਗੇ")
    AppLanguage.Romanian -> SmartPlaylistDeleteCopy("Ștergi lista inteligentă?", "Aceasta elimină doar regulile salvate")
    AppLanguage.Russian -> SmartPlaylistDeleteCopy("Удалить смарт-плейлист?", "Будут удалены только сохранённые правила")
    AppLanguage.Serbian -> SmartPlaylistDeleteCopy("Избрисати паметну плејлисту?", "Овим се уклањају само сачувана правила")
    AppLanguage.Slovak -> SmartPlaylistDeleteCopy("Odstrániť inteligentný playlist?", "Týmto odstránite iba uložené pravidlá")
    AppLanguage.Slovenian -> SmartPlaylistDeleteCopy("Izbrišem pametni seznam predvajanja?", "S tem odstranite samo shranjena pravila")
    AppLanguage.Spanish -> SmartPlaylistDeleteCopy("¿Eliminar lista inteligente?", "Solo se eliminarán las reglas guardadas")
    AppLanguage.Swedish -> SmartPlaylistDeleteCopy("Ta bort smart spellista?", "Detta tar bara bort de sparade reglerna")
    AppLanguage.Thai -> SmartPlaylistDeleteCopy("ลบเพลย์ลิสต์อัจฉริยะหรือไม่", "การดำเนินการนี้จะลบเฉพาะกฎที่บันทึกไว้")
    AppLanguage.Ukrainian -> SmartPlaylistDeleteCopy("Видалити розумний плейлист?", "Буде видалено лише збережені правила")
    AppLanguage.Urdu -> SmartPlaylistDeleteCopy("اسمارٹ پلے لسٹ حذف کریں؟", "اس سے صرف محفوظ کردہ اصول ہٹیں گے")
}

internal fun smartPlaylistEmptyCopy(language: AppLanguage): SmartPlaylistEmptyCopy = when (language) {
    AppLanguage.Albanian -> SmartPlaylistEmptyCopy("Asnjë këngë nuk përputhet me këto rregulla", "Provo të ndryshosh filtrat ose të rifreskosh bibliotekën")
    AppLanguage.Bengali -> SmartPlaylistEmptyCopy("এই নিয়মগুলির সঙ্গে কোনো গান মেলে না", "ফিল্টার বদলে দেখুন বা লাইব্রেরি রিফ্রেশ করুন")
    AppLanguage.Bosnian -> SmartPlaylistEmptyCopy("Nijedna pjesma ne odgovara ovim pravilima", "Promijenite filtere ili osvježite biblioteku")
    AppLanguage.Bulgarian -> SmartPlaylistEmptyCopy("Няма песни, които отговарят на тези правила", "Променете филтрите или обновете библиотеката")
    AppLanguage.Catalan -> SmartPlaylistEmptyCopy("Cap cançó no compleix aquestes regles", "Prova de canviar els filtres o d’actualitzar la biblioteca")
    AppLanguage.ChineseSimplified -> SmartPlaylistEmptyCopy("没有歌曲符合这些规则", "请尝试更改筛选条件或刷新音乐库")
    AppLanguage.Croatian -> SmartPlaylistEmptyCopy("Nijedna pjesma ne odgovara ovim pravilima", "Promijenite filtre ili osvježite biblioteku")
    AppLanguage.Czech -> SmartPlaylistEmptyCopy("Těmto pravidlům neodpovídají žádné skladby", "Zkuste změnit filtry nebo obnovit knihovnu")
    AppLanguage.Danish -> SmartPlaylistEmptyCopy("Ingen sange matcher disse regler", "Prøv at ændre filtrene eller opdatere biblioteket")
    AppLanguage.Dutch -> SmartPlaylistEmptyCopy("Geen nummers voldoen aan deze regels", "Wijzig de filters of vernieuw je bibliotheek")
    AppLanguage.English -> SmartPlaylistEmptyCopy("No songs match these rules", "Try changing the filters or refreshing your library")
    AppLanguage.Estonian -> SmartPlaylistEmptyCopy("Ükski lugu ei vasta neile reeglitele", "Proovi filtreid muuta või teeki värskendada")
    AppLanguage.Finnish -> SmartPlaylistEmptyCopy("Yksikään kappale ei vastaa näitä sääntöjä", "Kokeile muuttaa suodattimia tai päivittää kirjasto")
    AppLanguage.French -> SmartPlaylistEmptyCopy("Aucun morceau ne correspond à ces règles", "Modifiez les filtres ou actualisez votre bibliothèque")
    AppLanguage.German -> SmartPlaylistEmptyCopy("Keine Titel entsprechen diesen Regeln", "Ändere die Filter oder aktualisiere deine Bibliothek")
    AppLanguage.Greek -> SmartPlaylistEmptyCopy("Κανένα τραγούδι δεν ταιριάζει με αυτούς τους κανόνες", "Αλλάξτε τα φίλτρα ή ανανεώστε τη βιβλιοθήκη")
    AppLanguage.Hindi -> SmartPlaylistEmptyCopy("इन नियमों से कोई गाना मेल नहीं खाता", "फ़िल्टर बदलें या लाइब्रेरी रीफ़्रेश करें")
    AppLanguage.Hungarian -> SmartPlaylistEmptyCopy("Egyetlen dal sem felel meg ezeknek a szabályoknak", "Módosítsd a szűrőket, vagy frissítsd a könyvtárat")
    AppLanguage.Indonesian -> SmartPlaylistEmptyCopy("Tidak ada lagu yang cocok dengan aturan ini", "Coba ubah filter atau segarkan pustaka Anda")
    AppLanguage.Italian -> SmartPlaylistEmptyCopy("Nessun brano corrisponde a queste regole", "Prova a modificare i filtri o ad aggiornare la libreria")
    AppLanguage.Japanese -> SmartPlaylistEmptyCopy("このルールに一致する曲はありません", "フィルタを変更するか、ライブラリを更新してください")
    AppLanguage.Korean -> SmartPlaylistEmptyCopy("이 규칙에 맞는 노래가 없습니다", "필터를 변경하거나 라이브러리를 새로고침해 보세요")
    AppLanguage.Latvian -> SmartPlaylistEmptyCopy("Šiem noteikumiem neatbilst neviena dziesma", "Maini filtrus vai atsvaidzini bibliotēku")
    AppLanguage.Lithuanian -> SmartPlaylistEmptyCopy("Šių taisyklių neatitinka nė viena daina", "Pabandykite pakeisti filtrus arba atnaujinti biblioteką")
    AppLanguage.Macedonian -> SmartPlaylistEmptyCopy("Ниту една песна не одговара на овие правила", "Променете ги филтрите или освежете ја библиотеката")
    AppLanguage.Malay -> SmartPlaylistEmptyCopy("Tiada lagu yang sepadan dengan peraturan ini", "Cuba ubah penapis atau muat semula pustaka anda")
    AppLanguage.Norwegian -> SmartPlaylistEmptyCopy("Ingen sanger samsvarer med disse reglene", "Prøv å endre filtrene eller oppdatere biblioteket")
    AppLanguage.Polish -> SmartPlaylistEmptyCopy("Żaden utwór nie spełnia tych reguł", "Zmień filtry lub odśwież bibliotekę")
    AppLanguage.Portuguese -> SmartPlaylistEmptyCopy("Nenhuma música corresponde a estas regras", "Experimente alterar os filtros ou atualizar a biblioteca")
    AppLanguage.Punjabi -> SmartPlaylistEmptyCopy("ਕੋਈ ਵੀ ਗੀਤ ਇਨ੍ਹਾਂ ਨਿਯਮਾਂ ਨਾਲ ਮੇਲ ਨਹੀਂ ਖਾਂਦਾ", "ਫਿਲਟਰ ਬਦਲੋ ਜਾਂ ਲਾਇਬ੍ਰੇਰੀ ਰਿਫ੍ਰੈਸ਼ ਕਰੋ")
    AppLanguage.Romanian -> SmartPlaylistEmptyCopy("Nicio melodie nu corespunde acestor reguli", "Încearcă să schimbi filtrele sau să reîmprospătezi biblioteca")
    AppLanguage.Russian -> SmartPlaylistEmptyCopy("Нет песен, соответствующих этим правилам", "Измените фильтры или обновите медиатеку")
    AppLanguage.Serbian -> SmartPlaylistEmptyCopy("Ниједна песма не одговара овим правилима", "Промените филтере или освежите библиотеку")
    AppLanguage.Slovak -> SmartPlaylistEmptyCopy("Týmto pravidlám nezodpovedajú žiadne skladby", "Skúste zmeniť filtre alebo obnoviť knižnicu")
    AppLanguage.Slovenian -> SmartPlaylistEmptyCopy("Nobena skladba ne ustreza tem pravilom", "Spremenite filtre ali osvežite knjižnico")
    AppLanguage.Spanish -> SmartPlaylistEmptyCopy("Ninguna canción coincide con estas reglas", "Prueba a cambiar los filtros o actualizar la biblioteca")
    AppLanguage.Swedish -> SmartPlaylistEmptyCopy("Inga låtar matchar de här reglerna", "Prova att ändra filtren eller uppdatera biblioteket")
    AppLanguage.Thai -> SmartPlaylistEmptyCopy("ไม่มีเพลงที่ตรงกับกฎเหล่านี้", "ลองเปลี่ยนตัวกรองหรือรีเฟรชคลังเพลง")
    AppLanguage.Ukrainian -> SmartPlaylistEmptyCopy("Немає пісень, що відповідають цим правилам", "Змініть фільтри або оновіть медіатеку")
    AppLanguage.Urdu -> SmartPlaylistEmptyCopy("کوئی گانا ان اصولوں سے مطابقت نہیں رکھتا", "فلٹر تبدیل کریں یا لائبریری ریفریش کریں")
}
