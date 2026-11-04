package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun crossfadeToggleAccessibilityLabel(enabled: Boolean, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> "Aktivizo ndërthurjen" to "Çaktivizo ndërthurjen"
        AppLanguage.Bengali -> "ক্রসফেড চালু করুন" to "ক্রসফেড বন্ধ করুন"
        AppLanguage.Bosnian -> "Uključi pretapanje" to "Isključi pretapanje"
        AppLanguage.Bulgarian -> "Включи преливането" to "Изключи преливането"
        AppLanguage.Catalan -> "Activa la fosa" to "Desactiva la fosa"
        AppLanguage.ChineseSimplified -> "启用交叉淡化" to "停用交叉淡化"
        AppLanguage.Croatian -> "Uključi pretapanje" to "Isključi pretapanje"
        AppLanguage.Czech -> "Zapnout prolínání" to "Vypnout prolínání"
        AppLanguage.Danish -> "Slå overgang til" to "Slå overgang fra"
        AppLanguage.Dutch -> "Crossfade inschakelen" to "Crossfade uitschakelen"
        AppLanguage.English -> "Enable crossfade" to "Disable crossfade"
        AppLanguage.Estonian -> "Lülita sujuv üleminek sisse" to "Lülita sujuv üleminek välja"
        AppLanguage.Finnish -> "Ota ristihäivytys käyttöön" to "Poista ristihäivytys käytöstä"
        AppLanguage.French -> "Activer le fondu enchaîné" to "Désactiver le fondu enchaîné"
        AppLanguage.German -> "Überblendung aktivieren" to "Überblendung deaktivieren"
        AppLanguage.Greek -> "Ενεργοποίηση ομαλής μετάβασης" to "Απενεργοποίηση ομαλής μετάβασης"
        AppLanguage.Hindi -> "क्रॉसफेड चालू करें" to "क्रॉसफेड बंद करें"
        AppLanguage.Hungarian -> "Átkeverés bekapcsolása" to "Átkeverés kikapcsolása"
        AppLanguage.Indonesian -> "Aktifkan crossfade" to "Nonaktifkan crossfade"
        AppLanguage.Italian -> "Attiva dissolvenza incrociata" to "Disattiva dissolvenza incrociata"
        AppLanguage.Japanese -> "クロスフェードを有効にする" to "クロスフェードを無効にする"
        AppLanguage.Korean -> "크로스페이드 켜기" to "크로스페이드 끄기"
        AppLanguage.Latin -> "Transitum levem activa" to "Transitum levem deactiva"
        AppLanguage.Latvian -> "Ieslēgt laidenu pāreju" to "Izslēgt laidenu pāreju"
        AppLanguage.Lithuanian -> "Įjungti sklandų perėjimą" to "Išjungti sklandų perėjimą"
        AppLanguage.Macedonian -> "Вклучи претопување" to "Исклучи претопување"
        AppLanguage.Malay -> "Hidupkan pudar silang" to "Matikan pudar silang"
        AppLanguage.Norwegian -> "Slå på overgang" to "Slå av overgang"
        AppLanguage.Polish -> "Włącz przenikanie utworów" to "Wyłącz przenikanie utworów"
        AppLanguage.Portuguese -> "Ativar transição gradual" to "Desativar transição gradual"
        AppLanguage.Punjabi -> "ਕਰਾਸਫੇਡ ਚਾਲੂ ਕਰੋ" to "ਕਰਾਸਫੇਡ ਬੰਦ ਕਰੋ"
        AppLanguage.Romanian -> "Activează tranziția graduală" to "Dezactivează tranziția graduală"
        AppLanguage.Russian -> "Включить плавный переход" to "Выключить плавный переход"
        AppLanguage.Serbian -> "Укључи претапање" to "Искључи претапање"
        AppLanguage.Slovak -> "Zapnúť prelínanie" to "Vypnúť prelínanie"
        AppLanguage.Slovenian -> "Vklopi prelivanje" to "Izklopi prelivanje"
        AppLanguage.Spanish -> "Activar fundido cruzado" to "Desactivar fundido cruzado"
        AppLanguage.Swedish -> "Aktivera övertoning" to "Inaktivera övertoning"
        AppLanguage.Thai -> "เปิดการเฟดข้ามเพลง" to "ปิดการเฟดข้ามเพลง"
        AppLanguage.Ukrainian -> "Увімкнути плавний перехід" to "Вимкнути плавний перехід"
        AppLanguage.Urdu -> "کراس فیڈ آن کریں" to "کراس فیڈ بند کریں"
    }
    return if (enabled) labels.second else labels.first
}

internal fun songFavoriteAccessibilityLabel(favorite: Boolean, language: AppLanguage): String {
    val labels = when (language) {
        AppLanguage.Albanian -> "Pëlqe këngën" to "Hiq pëlqimin e këngës"
        AppLanguage.Bengali -> "গানটি পছন্দ করুন" to "গানটির পছন্দ সরান"
        AppLanguage.Bosnian -> "Označi pjesmu kao omiljenu" to "Ukloni pjesmu iz omiljenih"
        AppLanguage.Bulgarian -> "Добави песента в любими" to "Премахни песента от любими"
        AppLanguage.Catalan -> "Afegeix la cançó a preferides" to "Treu la cançó de preferides"
        AppLanguage.ChineseSimplified -> "喜欢这首歌" to "取消喜欢这首歌"
        AppLanguage.Croatian -> "Označi pjesmu kao omiljenu" to "Ukloni pjesmu iz omiljenih"
        AppLanguage.Czech -> "Přidat skladbu mezi oblíbené" to "Odebrat skladbu z oblíbených"
        AppLanguage.Danish -> "Tilføj sangen til favoritter" to "Fjern sangen fra favoritter"
        AppLanguage.Dutch -> "Nummer toevoegen aan favorieten" to "Nummer verwijderen uit favorieten"
        AppLanguage.English -> "Like song" to "Unlike song"
        AppLanguage.Estonian -> "Lisa lugu lemmikutesse" to "Eemalda lugu lemmikutest"
        AppLanguage.Finnish -> "Lisää kappale suosikkeihin" to "Poista kappale suosikeista"
        AppLanguage.French -> "Ajouter le morceau aux favoris" to "Retirer le morceau des favoris"
        AppLanguage.German -> "Titel zu Favoriten hinzufügen" to "Titel aus Favoriten entfernen"
        AppLanguage.Greek -> "Προσθήκη τραγουδιού στα αγαπημένα" to "Αφαίρεση τραγουδιού από τα αγαπημένα"
        AppLanguage.Hindi -> "गाना पसंद करें" to "गाने की पसंद हटाएं"
        AppLanguage.Hungarian -> "Dal hozzáadása a kedvencekhez" to "Dal eltávolítása a kedvencekből"
        AppLanguage.Indonesian -> "Sukai lagu" to "Batalkan suka lagu"
        AppLanguage.Italian -> "Aggiungi brano ai preferiti" to "Rimuovi brano dai preferiti"
        AppLanguage.Japanese -> "曲をお気に入りに追加" to "曲をお気に入りから削除"
        AppLanguage.Korean -> "노래를 즐겨찾기에 추가" to "노래를 즐겨찾기에서 제거"
        AppLanguage.Latin -> "Cantum in favoritis adde" to "Cantum e favoritis remove"
        AppLanguage.Latvian -> "Pievienot dziesmu izlasei" to "Noņemt dziesmu no izlases"
        AppLanguage.Lithuanian -> "Pridėti dainą prie mėgstamiausių" to "Pašalinti dainą iš mėgstamiausių"
        AppLanguage.Macedonian -> "Додај песна во омилени" to "Отстрани песна од омилени"
        AppLanguage.Malay -> "Sukai lagu" to "Batalkan suka lagu"
        AppLanguage.Norwegian -> "Legg sangen til i favoritter" to "Fjern sangen fra favoritter"
        AppLanguage.Polish -> "Dodaj utwór do ulubionych" to "Usuń utwór z ulubionych"
        AppLanguage.Portuguese -> "Adicionar música aos favoritos" to "Remover música dos favoritos"
        AppLanguage.Punjabi -> "ਗੀਤ ਪਸੰਦ ਕਰੋ" to "ਗੀਤ ਦੀ ਪਸੰਦ ਹਟਾਓ"
        AppLanguage.Romanian -> "Adaugă melodia la favorite" to "Elimină melodia din favorite"
        AppLanguage.Russian -> "Добавить песню в избранное" to "Удалить песню из избранного"
        AppLanguage.Serbian -> "Додај песму у омиљене" to "Уклони песму из омиљених"
        AppLanguage.Slovak -> "Pridať skladbu medzi obľúbené" to "Odobrať skladbu z obľúbených"
        AppLanguage.Slovenian -> "Dodaj skladbo med priljubljene" to "Odstrani skladbo iz priljubljenih"
        AppLanguage.Spanish -> "Añadir canción a favoritos" to "Quitar canción de favoritos"
        AppLanguage.Swedish -> "Lägg till låten i favoriter" to "Ta bort låten från favoriter"
        AppLanguage.Thai -> "เพิ่มเพลงในรายการโปรด" to "นำเพลงออกจากรายการโปรด"
        AppLanguage.Ukrainian -> "Додати пісню до вподобаного" to "Видалити пісню з уподобаного"
        AppLanguage.Urdu -> "گانا پسند کریں" to "گانے کی پسند ہٹائیں"
    }
    return if (favorite) labels.second else labels.first
}

internal fun editLyricsAccessibilityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Redakto tekstin e këngës"
    AppLanguage.Bengali -> "গানের কথা সম্পাদনা করুন"
    AppLanguage.Bosnian -> "Uredi tekst pjesme"
    AppLanguage.Bulgarian -> "Редактиране на текста на песента"
    AppLanguage.Catalan -> "Edita la lletra"
    AppLanguage.ChineseSimplified -> "编辑歌词"
    AppLanguage.Croatian -> "Uredi tekst pjesme"
    AppLanguage.Czech -> "Upravit text skladby"
    AppLanguage.Danish -> "Rediger sangtekst"
    AppLanguage.Dutch -> "Songtekst bewerken"
    AppLanguage.English -> "Edit lyrics"
    AppLanguage.Estonian -> "Muuda laulusõnu"
    AppLanguage.Finnish -> "Muokkaa sanoitusta"
    AppLanguage.French -> "Modifier les paroles"
    AppLanguage.German -> "Liedtext bearbeiten"
    AppLanguage.Greek -> "Επεξεργασία στίχων"
    AppLanguage.Hindi -> "गाने के बोल संपादित करें"
    AppLanguage.Hungarian -> "Dalszöveg szerkesztése"
    AppLanguage.Indonesian -> "Edit lirik"
    AppLanguage.Italian -> "Modifica testo"
    AppLanguage.Japanese -> "歌詞を編集"
    AppLanguage.Korean -> "가사 편집"
    AppLanguage.Latin -> "Verba cantus muta"
    AppLanguage.Latvian -> "Rediģēt dziesmas vārdus"
    AppLanguage.Lithuanian -> "Redaguoti dainos žodžius"
    AppLanguage.Macedonian -> "Уреди текст на песната"
    AppLanguage.Malay -> "Edit lirik"
    AppLanguage.Norwegian -> "Rediger sangtekst"
    AppLanguage.Polish -> "Edytuj tekst utworu"
    AppLanguage.Portuguese -> "Editar letra"
    AppLanguage.Punjabi -> "ਗੀਤ ਦੇ ਬੋਲ ਸੰਪਾਦਿਤ ਕਰੋ"
    AppLanguage.Romanian -> "Editează versurile"
    AppLanguage.Russian -> "Изменить текст песни"
    AppLanguage.Serbian -> "Уреди текст песме"
    AppLanguage.Slovak -> "Upraviť text skladby"
    AppLanguage.Slovenian -> "Uredi besedilo skladbe"
    AppLanguage.Spanish -> "Editar letra"
    AppLanguage.Swedish -> "Redigera sångtext"
    AppLanguage.Thai -> "แก้ไขเนื้อเพลง"
    AppLanguage.Ukrainian -> "Редагувати текст пісні"
    AppLanguage.Urdu -> "گانے کے بول میں ترمیم کریں"
}

internal fun artworkPlaceholderLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Imazh zëvendësues i kopertinës"
    AppLanguage.Bengali -> "প্রচ্ছদের বিকল্প চিত্র"
    AppLanguage.Bosnian -> "Zamjenska slika omota"
    AppLanguage.Bulgarian -> "Заместващо изображение на обложката"
    AppLanguage.Catalan -> "Imatge de portada provisional"
    AppLanguage.ChineseSimplified -> "封面占位图"
    AppLanguage.Croatian -> "Zamjenska slika omota"
    AppLanguage.Czech -> "Zástupný obrázek obalu"
    AppLanguage.Danish -> "Pladsholder for omslagsbillede"
    AppLanguage.Dutch -> "Tijdelijke afbeelding voor de hoes"
    AppLanguage.English -> "Artwork placeholder"
    AppLanguage.Estonian -> "Kaanepildi kohatäitja"
    AppLanguage.Finnish -> "Kansikuvan paikkamerkki"
    AppLanguage.French -> "Image de couverture de remplacement"
    AppLanguage.German -> "Platzhalter für das Cover"
    AppLanguage.Greek -> "Εναλλακτική εικόνα εξωφύλλου"
    AppLanguage.Hindi -> "कवर के लिए प्लेसहोल्डर"
    AppLanguage.Hungarian -> "Borító helyőrzője"
    AppLanguage.Indonesian -> "Gambar pengganti sampul"
    AppLanguage.Italian -> "Immagine segnaposto della copertina"
    AppLanguage.Japanese -> "ジャケットの代替画像"
    AppLanguage.Korean -> "앨범 표지 대체 이미지"
    AppLanguage.Latin -> "Imago vicaria operculi"
    AppLanguage.Latvian -> "Vāka attēla vietturis"
    AppLanguage.Lithuanian -> "Viršelio vietaženklis"
    AppLanguage.Macedonian -> "Заменска слика за насловна"
    AppLanguage.Malay -> "Imej pengganti kulit album"
    AppLanguage.Norwegian -> "Plassholder for omslagsbilde"
    AppLanguage.Polish -> "Obraz zastępczy okładki"
    AppLanguage.Portuguese -> "Imagem temporária da capa"
    AppLanguage.Punjabi -> "ਕਵਰ ਲਈ ਥਾਂ ਰੱਖਣ ਵਾਲੀ ਤਸਵੀਰ"
    AppLanguage.Romanian -> "Imagine substitut pentru copertă"
    AppLanguage.Russian -> "Заглушка обложки"
    AppLanguage.Serbian -> "Заменска слика омота"
    AppLanguage.Slovak -> "Zástupný obrázok obalu"
    AppLanguage.Slovenian -> "Nadomestna slika naslovnice"
    AppLanguage.Spanish -> "Imagen provisional de la portada"
    AppLanguage.Swedish -> "Platshållare för omslagsbild"
    AppLanguage.Thai -> "ภาพตัวแทนปก"
    AppLanguage.Ukrainian -> "Зображення-заповнювач обкладинки"
    AppLanguage.Urdu -> "سرورق کی متبادل تصویر"
}

internal data class PlaylistAccessibilityCopy(val confirmSongs: String, val reorderSong: String)

internal fun playlistAccessibilityCopy(language: AppLanguage): PlaylistAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> PlaylistAccessibilityCopy("Konfirmo këngët e shtuara", "Rendit këngën")
    AppLanguage.Bengali -> PlaylistAccessibilityCopy("যোগ করা গান নিশ্চিত করুন", "গানের ক্রম বদলান")
    AppLanguage.Bosnian -> PlaylistAccessibilityCopy("Potvrdi dodane pjesme", "Promijeni redoslijed pjesme")
    AppLanguage.Bulgarian -> PlaylistAccessibilityCopy("Потвърди добавените песни", "Промени реда на песента")
    AppLanguage.Catalan -> PlaylistAccessibilityCopy("Confirma les cançons afegides", "Canvia l’ordre de la cançó")
    AppLanguage.ChineseSimplified -> PlaylistAccessibilityCopy("确认添加的歌曲", "调整歌曲顺序")
    AppLanguage.Croatian -> PlaylistAccessibilityCopy("Potvrdi dodane pjesme", "Promijeni redoslijed pjesme")
    AppLanguage.Czech -> PlaylistAccessibilityCopy("Potvrdit přidané skladby", "Změnit pořadí skladby")
    AppLanguage.Danish -> PlaylistAccessibilityCopy("Bekræft tilføjede sange", "Skift sangens rækkefølge")
    AppLanguage.Dutch -> PlaylistAccessibilityCopy("Toegevoegde nummers bevestigen", "Volgorde van nummer wijzigen")
    AppLanguage.English -> PlaylistAccessibilityCopy("Confirm added songs", "Reorder song")
    AppLanguage.Estonian -> PlaylistAccessibilityCopy("Kinnita lisatud lood", "Muuda loo järjekorda")
    AppLanguage.Finnish -> PlaylistAccessibilityCopy("Vahvista lisätyt kappaleet", "Muuta kappaleen järjestystä")
    AppLanguage.French -> PlaylistAccessibilityCopy("Confirmer les morceaux ajoutés", "Modifier l’ordre du morceau")
    AppLanguage.German -> PlaylistAccessibilityCopy("Hinzugefügte Titel bestätigen", "Titelreihenfolge ändern")
    AppLanguage.Greek -> PlaylistAccessibilityCopy("Επιβεβαίωση προστιθέμενων τραγουδιών", "Αλλαγή σειράς τραγουδιού")
    AppLanguage.Hindi -> PlaylistAccessibilityCopy("जोड़े गए गानों की पुष्टि करें", "गाने का क्रम बदलें")
    AppLanguage.Hungarian -> PlaylistAccessibilityCopy("Hozzáadott dalok megerősítése", "Dal átrendezése")
    AppLanguage.Indonesian -> PlaylistAccessibilityCopy("Konfirmasi lagu yang ditambahkan", "Ubah urutan lagu")
    AppLanguage.Italian -> PlaylistAccessibilityCopy("Conferma i brani aggiunti", "Cambia l’ordine del brano")
    AppLanguage.Japanese -> PlaylistAccessibilityCopy("追加した曲を確定", "曲の順序を変更")
    AppLanguage.Korean -> PlaylistAccessibilityCopy("추가한 노래 확인", "노래 순서 변경")
    AppLanguage.Latin -> PlaylistAccessibilityCopy("Cantus additos confirma", "Ordinem cantus muta")
    AppLanguage.Latvian -> PlaylistAccessibilityCopy("Apstiprināt pievienotās dziesmas", "Mainīt dziesmas secību")
    AppLanguage.Lithuanian -> PlaylistAccessibilityCopy("Patvirtinti pridėtas dainas", "Pakeisti dainos vietą")
    AppLanguage.Macedonian -> PlaylistAccessibilityCopy("Потврди ги додадените песни", "Промени го редоследот на песната")
    AppLanguage.Malay -> PlaylistAccessibilityCopy("Sahkan lagu yang ditambah", "Susun semula lagu")
    AppLanguage.Norwegian -> PlaylistAccessibilityCopy("Bekreft tilføyde sanger", "Endre rekkefølgen på sangen")
    AppLanguage.Polish -> PlaylistAccessibilityCopy("Potwierdź dodanie utworów", "Zmień kolejność utworu")
    AppLanguage.Portuguese -> PlaylistAccessibilityCopy("Confirmar músicas adicionadas", "Alterar a ordem da música")
    AppLanguage.Punjabi -> PlaylistAccessibilityCopy("ਜੋੜੇ ਗੀਤਾਂ ਦੀ ਪੁਸ਼ਟੀ ਕਰੋ", "ਗੀਤ ਦਾ ਕ੍ਰਮ ਬਦਲੋ")
    AppLanguage.Romanian -> PlaylistAccessibilityCopy("Confirmă melodiile adăugate", "Schimbă ordinea melodiei")
    AppLanguage.Russian -> PlaylistAccessibilityCopy("Подтвердить добавление песен", "Изменить порядок песни")
    AppLanguage.Serbian -> PlaylistAccessibilityCopy("Потврди додате песме", "Промени редослед песме")
    AppLanguage.Slovak -> PlaylistAccessibilityCopy("Potvrdiť pridané skladby", "Zmeniť poradie skladby")
    AppLanguage.Slovenian -> PlaylistAccessibilityCopy("Potrdi dodane skladbe", "Spremeni vrstni red skladbe")
    AppLanguage.Spanish -> PlaylistAccessibilityCopy("Confirmar canciones añadidas", "Cambiar el orden de la canción")
    AppLanguage.Swedish -> PlaylistAccessibilityCopy("Bekräfta tillagda låtar", "Ändra låtens ordning")
    AppLanguage.Thai -> PlaylistAccessibilityCopy("ยืนยันเพลงที่เพิ่ม", "เปลี่ยนลำดับเพลง")
    AppLanguage.Ukrainian -> PlaylistAccessibilityCopy("Підтвердити додані пісні", "Змінити порядок пісні")
    AppLanguage.Urdu -> PlaylistAccessibilityCopy("شامل کردہ گانوں کی تصدیق کریں", "گانے کی ترتیب بدلیں")
}

internal data class SettingsAccessibilityCopy(val closeLanguage: String, val smallerText: String, val largerText: String)

internal fun settingsAccessibilityCopy(language: AppLanguage): SettingsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> SettingsAccessibilityCopy("Mbyll zgjedhjen e gjuhës", "Tekst më i vogël", "Tekst më i madh")
    AppLanguage.Bengali -> SettingsAccessibilityCopy("ভাষা নির্বাচন বন্ধ করুন", "ছোট লেখা", "বড় লেখা")
    AppLanguage.Bosnian -> SettingsAccessibilityCopy("Zatvori odabir jezika", "Manji tekst", "Veći tekst")
    AppLanguage.Bulgarian -> SettingsAccessibilityCopy("Затвори избора на език", "По-малък текст", "По-голям текст")
    AppLanguage.Catalan -> SettingsAccessibilityCopy("Tanca la selecció d’idioma", "Text més petit", "Text més gran")
    AppLanguage.ChineseSimplified -> SettingsAccessibilityCopy("关闭语言选择", "较小文字", "较大文字")
    AppLanguage.Croatian -> SettingsAccessibilityCopy("Zatvori odabir jezika", "Manji tekst", "Veći tekst")
    AppLanguage.Czech -> SettingsAccessibilityCopy("Zavřít výběr jazyka", "Menší text", "Větší text")
    AppLanguage.Danish -> SettingsAccessibilityCopy("Luk sprogvalg", "Mindre tekst", "Større tekst")
    AppLanguage.Dutch -> SettingsAccessibilityCopy("Taalkeuze sluiten", "Kleinere tekst", "Grotere tekst")
    AppLanguage.English -> SettingsAccessibilityCopy("Close language selector", "Smaller text", "Larger text")
    AppLanguage.Estonian -> SettingsAccessibilityCopy("Sulge keelevalik", "Väiksem tekst", "Suurem tekst")
    AppLanguage.Finnish -> SettingsAccessibilityCopy("Sulje kielivalinta", "Pienempi teksti", "Suurempi teksti")
    AppLanguage.French -> SettingsAccessibilityCopy("Fermer le choix de langue", "Texte plus petit", "Texte plus grand")
    AppLanguage.German -> SettingsAccessibilityCopy("Sprachauswahl schließen", "Kleinere Schrift", "Größere Schrift")
    AppLanguage.Greek -> SettingsAccessibilityCopy("Κλείσιμο επιλογής γλώσσας", "Μικρότερο κείμενο", "Μεγαλύτερο κείμενο")
    AppLanguage.Hindi -> SettingsAccessibilityCopy("भाषा चयन बंद करें", "छोटा टेक्स्ट", "बड़ा टेक्स्ट")
    AppLanguage.Hungarian -> SettingsAccessibilityCopy("Nyelvválasztó bezárása", "Kisebb szöveg", "Nagyobb szöveg")
    AppLanguage.Indonesian -> SettingsAccessibilityCopy("Tutup pemilihan bahasa", "Teks lebih kecil", "Teks lebih besar")
    AppLanguage.Italian -> SettingsAccessibilityCopy("Chiudi selezione lingua", "Testo più piccolo", "Testo più grande")
    AppLanguage.Japanese -> SettingsAccessibilityCopy("言語選択を閉じる", "小さい文字", "大きい文字")
    AppLanguage.Korean -> SettingsAccessibilityCopy("언어 선택 닫기", "작은 글씨", "큰 글씨")
    AppLanguage.Latin -> SettingsAccessibilityCopy("Electionem linguae claude", "Textus minor", "Textus maior")
    AppLanguage.Latvian -> SettingsAccessibilityCopy("Aizvērt valodas izvēli", "Mazāks teksts", "Lielāks teksts")
    AppLanguage.Lithuanian -> SettingsAccessibilityCopy("Uždaryti kalbos pasirinkimą", "Mažesnis tekstas", "Didesnis tekstas")
    AppLanguage.Macedonian -> SettingsAccessibilityCopy("Затвори избор на јазик", "Помал текст", "Поголем текст")
    AppLanguage.Malay -> SettingsAccessibilityCopy("Tutup pilihan bahasa", "Teks lebih kecil", "Teks lebih besar")
    AppLanguage.Norwegian -> SettingsAccessibilityCopy("Lukk språkvalg", "Mindre tekst", "Større tekst")
    AppLanguage.Polish -> SettingsAccessibilityCopy("Zamknij wybór języka", "Mniejszy tekst", "Większy tekst")
    AppLanguage.Portuguese -> SettingsAccessibilityCopy("Fechar seleção de idioma", "Texto mais pequeno", "Texto maior")
    AppLanguage.Punjabi -> SettingsAccessibilityCopy("ਭਾਸ਼ਾ ਚੋਣ ਬੰਦ ਕਰੋ", "ਛੋਟਾ ਲਿਖਤ", "ਵੱਡਾ ਲਿਖਤ")
    AppLanguage.Romanian -> SettingsAccessibilityCopy("Închide selectarea limbii", "Text mai mic", "Text mai mare")
    AppLanguage.Russian -> SettingsAccessibilityCopy("Закрыть выбор языка", "Меньший текст", "Больший текст")
    AppLanguage.Serbian -> SettingsAccessibilityCopy("Затвори избор језика", "Мањи текст", "Већи текст")
    AppLanguage.Slovak -> SettingsAccessibilityCopy("Zavrieť výber jazyka", "Menší text", "Väčší text")
    AppLanguage.Slovenian -> SettingsAccessibilityCopy("Zapri izbiro jezika", "Manjše besedilo", "Večje besedilo")
    AppLanguage.Spanish -> SettingsAccessibilityCopy("Cerrar selección de idioma", "Texto más pequeño", "Texto más grande")
    AppLanguage.Swedish -> SettingsAccessibilityCopy("Stäng språkvalet", "Mindre text", "Större text")
    AppLanguage.Thai -> SettingsAccessibilityCopy("ปิดการเลือกภาษา", "ข้อความเล็กลง", "ข้อความใหญ่ขึ้น")
    AppLanguage.Ukrainian -> SettingsAccessibilityCopy("Закрити вибір мови", "Менший текст", "Більший текст")
    AppLanguage.Urdu -> SettingsAccessibilityCopy("زبان کا انتخاب بند کریں", "چھوٹا متن", "بڑا متن")
}

internal data class PlayerControlAccessibilityCopy(val minimize: String, val resetTimer: String, val mutedVolume: String, val maximumVolume: String)

internal fun playerControlAccessibilityCopy(language: AppLanguage): PlayerControlAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> PlayerControlAccessibilityCopy("Minimizo", "Rivendos kohëmatësin", "Zëri i heshtur", "Volumi maksimal")
    AppLanguage.Bengali -> PlayerControlAccessibilityCopy("ছোট করুন", "টাইমার রিসেট করুন", "শব্দ বন্ধ", "সর্বোচ্চ শব্দ")
    AppLanguage.Bosnian -> PlayerControlAccessibilityCopy("Smanji prikaz", "Resetuj tajmer", "Zvuk isključen", "Najveća jačina zvuka")
    AppLanguage.Bulgarian -> PlayerControlAccessibilityCopy("Минимизирай", "Нулирай таймера", "Звукът е изключен", "Максимална сила на звука")
    AppLanguage.Catalan -> PlayerControlAccessibilityCopy("Minimitza", "Restableix el temporitzador", "Volum silenciat", "Volum màxim")
    AppLanguage.ChineseSimplified -> PlayerControlAccessibilityCopy("最小化", "重置计时器", "静音", "最大音量")
    AppLanguage.Croatian -> PlayerControlAccessibilityCopy("Smanji prikaz", "Poništi mjerač vremena", "Zvuk je isključen", "Najveća glasnoća")
    AppLanguage.Czech -> PlayerControlAccessibilityCopy("Minimalizovat", "Resetovat časovač", "Ztlumený zvuk", "Maximální hlasitost")
    AppLanguage.Danish -> PlayerControlAccessibilityCopy("Minimer", "Nulstil timer", "Lyd slået fra", "Maksimal lydstyrke")
    AppLanguage.Dutch -> PlayerControlAccessibilityCopy("Minimaliseren", "Timer resetten", "Geluid gedempt", "Maximaal volume")
    AppLanguage.English -> PlayerControlAccessibilityCopy("Minimize", "Reset timer", "Muted volume", "Maximum volume")
    AppLanguage.Estonian -> PlayerControlAccessibilityCopy("Minimeeri", "Lähtesta taimer", "Heli vaigistatud", "Suurim helitugevus")
    AppLanguage.Finnish -> PlayerControlAccessibilityCopy("Pienennä", "Nollaa ajastin", "Ääni mykistetty", "Suurin äänenvoimakkuus")
    AppLanguage.French -> PlayerControlAccessibilityCopy("Réduire", "Réinitialiser la minuterie", "Son coupé", "Volume maximal")
    AppLanguage.German -> PlayerControlAccessibilityCopy("Minimieren", "Timer zurücksetzen", "Ton stummgeschaltet", "Maximale Lautstärke")
    AppLanguage.Greek -> PlayerControlAccessibilityCopy("Ελαχιστοποίηση", "Επαναφορά χρονοδιακόπτη", "Σίγαση ήχου", "Μέγιστη ένταση")
    AppLanguage.Hindi -> PlayerControlAccessibilityCopy("छोटा करें", "टाइमर रीसेट करें", "आवाज़ बंद", "अधिकतम आवाज़")
    AppLanguage.Hungarian -> PlayerControlAccessibilityCopy("Kis méret", "Időzítő visszaállítása", "Némított hangerő", "Maximális hangerő")
    AppLanguage.Indonesian -> PlayerControlAccessibilityCopy("Perkecil", "Atur ulang pengatur waktu", "Suara dibisukan", "Volume maksimum")
    AppLanguage.Italian -> PlayerControlAccessibilityCopy("Riduci", "Reimposta timer", "Audio disattivato", "Volume massimo")
    AppLanguage.Japanese -> PlayerControlAccessibilityCopy("最小化", "タイマーをリセット", "消音", "最大音量")
    AppLanguage.Korean -> PlayerControlAccessibilityCopy("최소화", "타이머 초기화", "음소거", "최대 음량")
    AppLanguage.Latin -> PlayerControlAccessibilityCopy("Minime redde", "Temporizatorem restitue", "Sonus mutus", "Sonus maximus")
    AppLanguage.Latvian -> PlayerControlAccessibilityCopy("Minimizēt", "Atiestatīt taimeri", "Skaņa izslēgta", "Maksimālais skaļums")
    AppLanguage.Lithuanian -> PlayerControlAccessibilityCopy("Sumažinti", "Nustatyti laikmatį iš naujo", "Garsas nutildytas", "Didžiausias garsumas")
    AppLanguage.Macedonian -> PlayerControlAccessibilityCopy("Намали приказ", "Ресетирај тајмер", "Звукот е исклучен", "Максимална јачина")
    AppLanguage.Malay -> PlayerControlAccessibilityCopy("Minimumkan", "Tetapkan semula pemasa", "Bunyi diredamkan", "Kelantangan maksimum")
    AppLanguage.Norwegian -> PlayerControlAccessibilityCopy("Minimer", "Nullstill tidtaker", "Lyden er dempet", "Maksimalt volum")
    AppLanguage.Polish -> PlayerControlAccessibilityCopy("Zminimalizuj", "Zresetuj wyłącznik czasowy", "Dźwięk wyciszony", "Maksymalna głośność")
    AppLanguage.Portuguese -> PlayerControlAccessibilityCopy("Minimizar", "Repor temporizador", "Som silenciado", "Volume máximo")
    AppLanguage.Punjabi -> PlayerControlAccessibilityCopy("ਛੋਟਾ ਕਰੋ", "ਟਾਈਮਰ ਰੀਸੈਟ ਕਰੋ", "ਆਵਾਜ਼ ਬੰਦ", "ਵੱਧ ਤੋਂ ਵੱਧ ਆਵਾਜ਼")
    AppLanguage.Romanian -> PlayerControlAccessibilityCopy("Minimizează", "Resetează temporizatorul", "Sunet oprit", "Volum maxim")
    AppLanguage.Russian -> PlayerControlAccessibilityCopy("Свернуть", "Сбросить таймер", "Звук выключен", "Максимальная громкость")
    AppLanguage.Serbian -> PlayerControlAccessibilityCopy("Умањи приказ", "Ресетуј тајмер", "Звук је искључен", "Највећа јачина звука")
    AppLanguage.Slovak -> PlayerControlAccessibilityCopy("Minimalizovať", "Resetovať časovač", "Stlmený zvuk", "Maximálna hlasitosť")
    AppLanguage.Slovenian -> PlayerControlAccessibilityCopy("Pomanjšaj", "Ponastavi časovnik", "Zvok je utišan", "Največja glasnost")
    AppLanguage.Spanish -> PlayerControlAccessibilityCopy("Minimizar", "Restablecer temporizador", "Sonido silenciado", "Volumen máximo")
    AppLanguage.Swedish -> PlayerControlAccessibilityCopy("Minimera", "Återställ timern", "Ljud avstängt", "Maximal volym")
    AppLanguage.Thai -> PlayerControlAccessibilityCopy("ย่อ", "รีเซ็ตตัวตั้งเวลา", "ปิดเสียง", "ระดับเสียงสูงสุด")
    AppLanguage.Ukrainian -> PlayerControlAccessibilityCopy("Згорнути", "Скинути таймер", "Звук вимкнено", "Максимальна гучність")
    AppLanguage.Urdu -> PlayerControlAccessibilityCopy("چھوٹا کریں", "ٹائمر ری سیٹ کریں", "آواز بند", "زیادہ سے زیادہ آواز")
}

internal data class MediaOptionsAccessibilityCopy(val queueSong: String, val album: String, val song: String)

internal fun mediaOptionsAccessibilityCopy(language: AppLanguage): MediaOptionsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> MediaOptionsAccessibilityCopy("Opsionet e këngës në radhë", "Opsionet e albumit", "Opsionet e këngës")
    AppLanguage.Bengali -> MediaOptionsAccessibilityCopy("সারির গানের বিকল্প", "অ্যালবামের বিকল্প", "গানের বিকল্প")
    AppLanguage.Bosnian -> MediaOptionsAccessibilityCopy("Opcije pjesme u redu", "Opcije albuma", "Opcije pjesme")
    AppLanguage.Bulgarian -> MediaOptionsAccessibilityCopy("Опции за песента в опашката", "Опции за албума", "Опции за песента")
    AppLanguage.Catalan -> MediaOptionsAccessibilityCopy("Opcions de la cançó de la cua", "Opcions de l’àlbum", "Opcions de la cançó")
    AppLanguage.ChineseSimplified -> MediaOptionsAccessibilityCopy("队列歌曲选项", "专辑选项", "歌曲选项")
    AppLanguage.Croatian -> MediaOptionsAccessibilityCopy("Opcije pjesme u redu", "Opcije albuma", "Opcije pjesme")
    AppLanguage.Czech -> MediaOptionsAccessibilityCopy("Možnosti skladby ve frontě", "Možnosti alba", "Možnosti skladby")
    AppLanguage.Danish -> MediaOptionsAccessibilityCopy("Valgmuligheder for sang i køen", "Albumindstillinger", "Sangindstillinger")
    AppLanguage.Dutch -> MediaOptionsAccessibilityCopy("Opties voor nummer in wachtrij", "Albumopties", "Nummeropties")
    AppLanguage.English -> MediaOptionsAccessibilityCopy("Queue song options", "Album options", "Song options")
    AppLanguage.Estonian -> MediaOptionsAccessibilityCopy("Järjekorras oleva loo valikud", "Albumi valikud", "Loo valikud")
    AppLanguage.Finnish -> MediaOptionsAccessibilityCopy("Jonossa olevan kappaleen valinnat", "Albumin valinnat", "Kappaleen valinnat")
    AppLanguage.French -> MediaOptionsAccessibilityCopy("Options du morceau dans la file", "Options de l’album", "Options du morceau")
    AppLanguage.German -> MediaOptionsAccessibilityCopy("Optionen für Titel in der Warteschlange", "Albumoptionen", "Titeloptionen")
    AppLanguage.Greek -> MediaOptionsAccessibilityCopy("Επιλογές τραγουδιού στην ουρά", "Επιλογές άλμπουμ", "Επιλογές τραγουδιού")
    AppLanguage.Hindi -> MediaOptionsAccessibilityCopy("कतार के गाने के विकल्प", "एल्बम के विकल्प", "गाने के विकल्प")
    AppLanguage.Hungarian -> MediaOptionsAccessibilityCopy("Lejátszási sorban lévő dal beállításai", "Album beállításai", "Dal beállításai")
    AppLanguage.Indonesian -> MediaOptionsAccessibilityCopy("Opsi lagu dalam antrean", "Opsi album", "Opsi lagu")
    AppLanguage.Italian -> MediaOptionsAccessibilityCopy("Opzioni del brano in coda", "Opzioni album", "Opzioni brano")
    AppLanguage.Japanese -> MediaOptionsAccessibilityCopy("キュー内の曲のオプション", "アルバムのオプション", "曲のオプション")
    AppLanguage.Korean -> MediaOptionsAccessibilityCopy("대기열 노래 옵션", "앨범 옵션", "노래 옵션")
    AppLanguage.Latin -> MediaOptionsAccessibilityCopy("Optiones cantus in ordine", "Optiones albi", "Optiones cantus")
    AppLanguage.Latvian -> MediaOptionsAccessibilityCopy("Rindā esošās dziesmas opcijas", "Albuma opcijas", "Dziesmas opcijas")
    AppLanguage.Lithuanian -> MediaOptionsAccessibilityCopy("Eilėje esančios dainos parinktys", "Albumo parinktys", "Dainos parinktys")
    AppLanguage.Macedonian -> MediaOptionsAccessibilityCopy("Опции за песната во редицата", "Опции за албумот", "Опции за песната")
    AppLanguage.Malay -> MediaOptionsAccessibilityCopy("Pilihan lagu dalam baris gilir", "Pilihan album", "Pilihan lagu")
    AppLanguage.Norwegian -> MediaOptionsAccessibilityCopy("Valg for sang i køen", "Albumvalg", "Sangvalg")
    AppLanguage.Polish -> MediaOptionsAccessibilityCopy("Opcje utworu w kolejce", "Opcje albumu", "Opcje utworu")
    AppLanguage.Portuguese -> MediaOptionsAccessibilityCopy("Opções da música na fila", "Opções do álbum", "Opções da música")
    AppLanguage.Punjabi -> MediaOptionsAccessibilityCopy("ਕਤਾਰ ਵਾਲੇ ਗੀਤ ਦੇ ਵਿਕਲਪ", "ਐਲਬਮ ਦੇ ਵਿਕਲਪ", "ਗੀਤ ਦੇ ਵਿਕਲਪ")
    AppLanguage.Romanian -> MediaOptionsAccessibilityCopy("Opțiunile melodiei din coadă", "Opțiunile albumului", "Opțiunile melodiei")
    AppLanguage.Russian -> MediaOptionsAccessibilityCopy("Действия с песней в очереди", "Действия с альбомом", "Действия с песней")
    AppLanguage.Serbian -> MediaOptionsAccessibilityCopy("Опције песме у реду", "Опције албума", "Опције песме")
    AppLanguage.Slovak -> MediaOptionsAccessibilityCopy("Možnosti skladby v rade", "Možnosti albumu", "Možnosti skladby")
    AppLanguage.Slovenian -> MediaOptionsAccessibilityCopy("Možnosti skladbe v čakalni vrsti", "Možnosti albuma", "Možnosti skladbe")
    AppLanguage.Spanish -> MediaOptionsAccessibilityCopy("Opciones de la canción en cola", "Opciones del álbum", "Opciones de la canción")
    AppLanguage.Swedish -> MediaOptionsAccessibilityCopy("Alternativ för låt i kön", "Albumalternativ", "Låtalternativ")
    AppLanguage.Thai -> MediaOptionsAccessibilityCopy("ตัวเลือกเพลงในคิว", "ตัวเลือกอัลบั้ม", "ตัวเลือกเพลง")
    AppLanguage.Ukrainian -> MediaOptionsAccessibilityCopy("Дії з піснею в черзі", "Дії з альбомом", "Дії з піснею")
    AppLanguage.Urdu -> MediaOptionsAccessibilityCopy("قطار میں گانے کے اختیارات", "البم کے اختیارات", "گانے کے اختیارات")
}

internal data class LyricsAccessibilityCopy(val lyrics: String, val addLyrics: String)

internal fun lyricsAccessibilityCopy(language: AppLanguage): LyricsAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> LyricsAccessibilityCopy("Teksti i këngës", "Shto tekstin e këngës")
    AppLanguage.Bengali -> LyricsAccessibilityCopy("গানের কথা", "গানের কথা যোগ করুন")
    AppLanguage.Bosnian -> LyricsAccessibilityCopy("Tekst pjesme", "Dodaj tekst pjesme")
    AppLanguage.Bulgarian -> LyricsAccessibilityCopy("Текст на песента", "Добави текст на песента")
    AppLanguage.Catalan -> LyricsAccessibilityCopy("Lletra", "Afegeix la lletra")
    AppLanguage.ChineseSimplified -> LyricsAccessibilityCopy("歌词", "添加歌词")
    AppLanguage.Croatian -> LyricsAccessibilityCopy("Tekst pjesme", "Dodaj tekst pjesme")
    AppLanguage.Czech -> LyricsAccessibilityCopy("Text skladby", "Přidat text skladby")
    AppLanguage.Danish -> LyricsAccessibilityCopy("Sangtekst", "Tilføj sangtekst")
    AppLanguage.Dutch -> LyricsAccessibilityCopy("Songtekst", "Songtekst toevoegen")
    AppLanguage.English -> LyricsAccessibilityCopy("Lyrics", "Add lyrics")
    AppLanguage.Estonian -> LyricsAccessibilityCopy("Laulusõnad", "Lisa laulusõnad")
    AppLanguage.Finnish -> LyricsAccessibilityCopy("Sanoitukset", "Lisää sanoitukset")
    AppLanguage.French -> LyricsAccessibilityCopy("Paroles", "Ajouter les paroles")
    AppLanguage.German -> LyricsAccessibilityCopy("Liedtext", "Liedtext hinzufügen")
    AppLanguage.Greek -> LyricsAccessibilityCopy("Στίχοι", "Προσθήκη στίχων")
    AppLanguage.Hindi -> LyricsAccessibilityCopy("गाने के बोल", "गाने के बोल जोड़ें")
    AppLanguage.Hungarian -> LyricsAccessibilityCopy("Dalszöveg", "Dalszöveg hozzáadása")
    AppLanguage.Indonesian -> LyricsAccessibilityCopy("Lirik", "Tambahkan lirik")
    AppLanguage.Italian -> LyricsAccessibilityCopy("Testo", "Aggiungi testo")
    AppLanguage.Japanese -> LyricsAccessibilityCopy("歌詞", "歌詞を追加")
    AppLanguage.Korean -> LyricsAccessibilityCopy("가사", "가사 추가")
    AppLanguage.Latin -> LyricsAccessibilityCopy("Verba cantus", "Verba cantus adde")
    AppLanguage.Latvian -> LyricsAccessibilityCopy("Dziesmas vārdi", "Pievienot dziesmas vārdus")
    AppLanguage.Lithuanian -> LyricsAccessibilityCopy("Dainos žodžiai", "Pridėti dainos žodžius")
    AppLanguage.Macedonian -> LyricsAccessibilityCopy("Текст на песна", "Додај текст на песна")
    AppLanguage.Malay -> LyricsAccessibilityCopy("Lirik", "Tambah lirik")
    AppLanguage.Norwegian -> LyricsAccessibilityCopy("Sangtekst", "Legg til sangtekst")
    AppLanguage.Polish -> LyricsAccessibilityCopy("Tekst utworu", "Dodaj tekst utworu")
    AppLanguage.Portuguese -> LyricsAccessibilityCopy("Letra", "Adicionar letra")
    AppLanguage.Punjabi -> LyricsAccessibilityCopy("ਗੀਤ ਦੇ ਬੋਲ", "ਗੀਤ ਦੇ ਬੋਲ ਜੋੜੋ")
    AppLanguage.Romanian -> LyricsAccessibilityCopy("Versuri", "Adaugă versuri")
    AppLanguage.Russian -> LyricsAccessibilityCopy("Текст песни", "Добавить текст песни")
    AppLanguage.Serbian -> LyricsAccessibilityCopy("Текст песме", "Додај текст песме")
    AppLanguage.Slovak -> LyricsAccessibilityCopy("Text skladby", "Pridať text skladby")
    AppLanguage.Slovenian -> LyricsAccessibilityCopy("Besedilo pesmi", "Dodaj besedilo pesmi")
    AppLanguage.Spanish -> LyricsAccessibilityCopy("Letra", "Añadir letra")
    AppLanguage.Swedish -> LyricsAccessibilityCopy("Låttext", "Lägg till låttext")
    AppLanguage.Thai -> LyricsAccessibilityCopy("เนื้อเพลง", "เพิ่มเนื้อเพลง")
    AppLanguage.Ukrainian -> LyricsAccessibilityCopy("Текст пісні", "Додати текст пісні")
    AppLanguage.Urdu -> LyricsAccessibilityCopy("گانے کے بول", "گانے کے بول شامل کریں")
}

internal fun closeQueueAccessibilityLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Mbyll radhën"
    AppLanguage.Bengali -> "সারি বন্ধ করুন"
    AppLanguage.Bosnian -> "Zatvori red reprodukcije"
    AppLanguage.Bulgarian -> "Затвори опашката"
    AppLanguage.Catalan -> "Tanca la cua"
    AppLanguage.ChineseSimplified -> "关闭播放队列"
    AppLanguage.Croatian -> "Zatvori red reprodukcije"
    AppLanguage.Czech -> "Zavřít frontu"
    AppLanguage.Danish -> "Luk afspilningskøen"
    AppLanguage.Dutch -> "Wachtrij sluiten"
    AppLanguage.English -> "Close queue"
    AppLanguage.Estonian -> "Sulge esitusjärjekord"
    AppLanguage.Finnish -> "Sulje toistojono"
    AppLanguage.French -> "Fermer la file d’attente"
    AppLanguage.German -> "Warteschlange schließen"
    AppLanguage.Greek -> "Κλείσιμο ουράς αναπαραγωγής"
    AppLanguage.Hindi -> "कतार बंद करें"
    AppLanguage.Hungarian -> "Lejátszási sor bezárása"
    AppLanguage.Indonesian -> "Tutup antrean"
    AppLanguage.Italian -> "Chiudi la coda"
    AppLanguage.Japanese -> "再生キューを閉じる"
    AppLanguage.Korean -> "재생 대기열 닫기"
    AppLanguage.Latin -> "Ordinem claude"
    AppLanguage.Latvian -> "Aizvērt atskaņošanas rindu"
    AppLanguage.Lithuanian -> "Uždaryti atkūrimo eilę"
    AppLanguage.Macedonian -> "Затвори редица за репродукција"
    AppLanguage.Malay -> "Tutup baris gilir"
    AppLanguage.Norwegian -> "Lukk avspillingskøen"
    AppLanguage.Polish -> "Zamknij kolejkę"
    AppLanguage.Portuguese -> "Fechar fila de reprodução"
    AppLanguage.Punjabi -> "ਕਤਾਰ ਬੰਦ ਕਰੋ"
    AppLanguage.Romanian -> "Închide coada de redare"
    AppLanguage.Russian -> "Закрыть очередь воспроизведения"
    AppLanguage.Serbian -> "Затвори ред за репродукцију"
    AppLanguage.Slovak -> "Zavrieť rad prehrávania"
    AppLanguage.Slovenian -> "Zapri čakalno vrsto"
    AppLanguage.Spanish -> "Cerrar cola de reproducción"
    AppLanguage.Swedish -> "Stäng uppspelningskön"
    AppLanguage.Thai -> "ปิดคิวการเล่น"
    AppLanguage.Ukrainian -> "Закрити чергу відтворення"
    AppLanguage.Urdu -> "قطار بند کریں"
}

internal data class NavigationAccessibilityCopy(val back: String, val menu: String)

internal fun navigationAccessibilityCopy(language: AppLanguage): NavigationAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> NavigationAccessibilityCopy("Kthehu", "Menuja")
    AppLanguage.Bengali -> NavigationAccessibilityCopy("ফিরে যান", "মেনু")
    AppLanguage.Bosnian -> NavigationAccessibilityCopy("Nazad", "Meni")
    AppLanguage.Bulgarian -> NavigationAccessibilityCopy("Назад", "Меню")
    AppLanguage.Catalan -> NavigationAccessibilityCopy("Enrere", "Menú")
    AppLanguage.ChineseSimplified -> NavigationAccessibilityCopy("返回", "菜单")
    AppLanguage.Croatian -> NavigationAccessibilityCopy("Natrag", "Izbornik")
    AppLanguage.Czech -> NavigationAccessibilityCopy("Zpět", "Nabídka")
    AppLanguage.Danish -> NavigationAccessibilityCopy("Tilbage", "Menu")
    AppLanguage.Dutch -> NavigationAccessibilityCopy("Terug", "Menu")
    AppLanguage.English -> NavigationAccessibilityCopy("Back", "Menu")
    AppLanguage.Estonian -> NavigationAccessibilityCopy("Tagasi", "Menüü")
    AppLanguage.Finnish -> NavigationAccessibilityCopy("Takaisin", "Valikko")
    AppLanguage.French -> NavigationAccessibilityCopy("Retour", "Menu")
    AppLanguage.German -> NavigationAccessibilityCopy("Zurück", "Menü")
    AppLanguage.Greek -> NavigationAccessibilityCopy("Πίσω", "Μενού")
    AppLanguage.Hindi -> NavigationAccessibilityCopy("वापस", "मेनू")
    AppLanguage.Hungarian -> NavigationAccessibilityCopy("Vissza", "Menü")
    AppLanguage.Indonesian -> NavigationAccessibilityCopy("Kembali", "Menu")
    AppLanguage.Italian -> NavigationAccessibilityCopy("Indietro", "Menu")
    AppLanguage.Japanese -> NavigationAccessibilityCopy("戻る", "メニュー")
    AppLanguage.Korean -> NavigationAccessibilityCopy("뒤로", "메뉴")
    AppLanguage.Latin -> NavigationAccessibilityCopy("Redi", "Index")
    AppLanguage.Latvian -> NavigationAccessibilityCopy("Atpakaļ", "Izvēlne")
    AppLanguage.Lithuanian -> NavigationAccessibilityCopy("Atgal", "Meniu")
    AppLanguage.Macedonian -> NavigationAccessibilityCopy("Назад", "Мени")
    AppLanguage.Malay -> NavigationAccessibilityCopy("Kembali", "Menu")
    AppLanguage.Norwegian -> NavigationAccessibilityCopy("Tilbake", "Meny")
    AppLanguage.Polish -> NavigationAccessibilityCopy("Wróć", "Menu")
    AppLanguage.Portuguese -> NavigationAccessibilityCopy("Voltar", "Menu")
    AppLanguage.Punjabi -> NavigationAccessibilityCopy("ਪਿੱਛੇ ਜਾਓ", "ਮੀਨੂ")
    AppLanguage.Romanian -> NavigationAccessibilityCopy("Înapoi", "Meniu")
    AppLanguage.Russian -> NavigationAccessibilityCopy("Назад", "Меню")
    AppLanguage.Serbian -> NavigationAccessibilityCopy("Назад", "Мени")
    AppLanguage.Slovak -> NavigationAccessibilityCopy("Späť", "Ponuka")
    AppLanguage.Slovenian -> NavigationAccessibilityCopy("Nazaj", "Meni")
    AppLanguage.Spanish -> NavigationAccessibilityCopy("Volver", "Menú")
    AppLanguage.Swedish -> NavigationAccessibilityCopy("Tillbaka", "Meny")
    AppLanguage.Thai -> NavigationAccessibilityCopy("ย้อนกลับ", "เมนู")
    AppLanguage.Ukrainian -> NavigationAccessibilityCopy("Назад", "Меню")
    AppLanguage.Urdu -> NavigationAccessibilityCopy("واپس", "مینو")
}

internal data class PlaybackNavigationAccessibilityCopy(
    val previous: String,
    val next: String,
    val rewind: String,
    val forward: String,
)

internal fun playbackNavigationAccessibilityCopy(language: AppLanguage): PlaybackNavigationAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> PlaybackNavigationAccessibilityCopy("E mëparshmja", "Tjetra", "Kthehu pas", "Kalo përpara")
    AppLanguage.Bengali -> PlaybackNavigationAccessibilityCopy("আগেরটি", "পরেরটি", "পিছিয়ে যান", "এগিয়ে যান")
    AppLanguage.Bosnian -> PlaybackNavigationAccessibilityCopy("Prethodno", "Sljedeće", "Premotaj nazad", "Premotaj naprijed")
    AppLanguage.Bulgarian -> PlaybackNavigationAccessibilityCopy("Предишна", "Следваща", "Назад", "Напред")
    AppLanguage.Catalan -> PlaybackNavigationAccessibilityCopy("Anterior", "Següent", "Recula", "Avança")
    AppLanguage.ChineseSimplified -> PlaybackNavigationAccessibilityCopy("上一首", "下一首", "后退", "前进")
    AppLanguage.Croatian -> PlaybackNavigationAccessibilityCopy("Prethodno", "Sljedeće", "Premotaj natrag", "Premotaj naprijed")
    AppLanguage.Czech -> PlaybackNavigationAccessibilityCopy("Předchozí", "Další", "Přetočit zpět", "Přetočit vpřed")
    AppLanguage.Danish -> PlaybackNavigationAccessibilityCopy("Forrige", "Næste", "Spol tilbage", "Spol frem")
    AppLanguage.Dutch -> PlaybackNavigationAccessibilityCopy("Vorige", "Volgende", "Terugspoelen", "Vooruitspoelen")
    AppLanguage.English -> PlaybackNavigationAccessibilityCopy("Previous", "Next", "Rewind", "Forward")
    AppLanguage.Estonian -> PlaybackNavigationAccessibilityCopy("Eelmine", "Järgmine", "Keri tagasi", "Keri edasi")
    AppLanguage.Finnish -> PlaybackNavigationAccessibilityCopy("Edellinen", "Seuraava", "Kelaa taaksepäin", "Kelaa eteenpäin")
    AppLanguage.French -> PlaybackNavigationAccessibilityCopy("Précédent", "Suivant", "Reculer", "Avancer")
    AppLanguage.German -> PlaybackNavigationAccessibilityCopy("Zurück", "Weiter", "Zurückspulen", "Vorspulen")
    AppLanguage.Greek -> PlaybackNavigationAccessibilityCopy("Προηγούμενο", "Επόμενο", "Πίσω", "Μπροστά")
    AppLanguage.Hindi -> PlaybackNavigationAccessibilityCopy("पिछला", "अगला", "पीछे जाएँ", "आगे जाएँ")
    AppLanguage.Hungarian -> PlaybackNavigationAccessibilityCopy("Előző", "Következő", "Visszatekerés", "Előretekerés")
    AppLanguage.Indonesian -> PlaybackNavigationAccessibilityCopy("Sebelumnya", "Berikutnya", "Mundur", "Maju")
    AppLanguage.Italian -> PlaybackNavigationAccessibilityCopy("Precedente", "Successivo", "Indietro", "Avanti")
    AppLanguage.Japanese -> PlaybackNavigationAccessibilityCopy("前へ", "次へ", "巻き戻す", "早送り")
    AppLanguage.Korean -> PlaybackNavigationAccessibilityCopy("이전", "다음", "되감기", "빨리 감기")
    AppLanguage.Latin -> PlaybackNavigationAccessibilityCopy("Praecedens", "Sequens", "Retro cede", "Progredere")
    AppLanguage.Latvian -> PlaybackNavigationAccessibilityCopy("Iepriekšējais", "Nākamais", "Attīt", "Pārtīt uz priekšu")
    AppLanguage.Lithuanian -> PlaybackNavigationAccessibilityCopy("Ankstesnis", "Kitas", "Atsukti", "Prasukti pirmyn")
    AppLanguage.Macedonian -> PlaybackNavigationAccessibilityCopy("Претходно", "Следно", "Премотај назад", "Премотај напред")
    AppLanguage.Malay -> PlaybackNavigationAccessibilityCopy("Sebelumnya", "Seterusnya", "Undur", "Maju")
    AppLanguage.Norwegian -> PlaybackNavigationAccessibilityCopy("Forrige", "Neste", "Spol tilbake", "Spol frem")
    AppLanguage.Polish -> PlaybackNavigationAccessibilityCopy("Poprzedni", "Następny", "Przewiń wstecz", "Przewiń do przodu")
    AppLanguage.Portuguese -> PlaybackNavigationAccessibilityCopy("Anterior", "Seguinte", "Recuar", "Avançar")
    AppLanguage.Punjabi -> PlaybackNavigationAccessibilityCopy("ਪਿਛਲਾ", "ਅਗਲਾ", "ਪਿੱਛੇ ਜਾਓ", "ਅੱਗੇ ਜਾਓ")
    AppLanguage.Romanian -> PlaybackNavigationAccessibilityCopy("Anterior", "Următor", "Derulează înapoi", "Derulează înainte")
    AppLanguage.Russian -> PlaybackNavigationAccessibilityCopy("Предыдущий", "Следующий", "Перемотать назад", "Перемотать вперёд")
    AppLanguage.Serbian -> PlaybackNavigationAccessibilityCopy("Претходно", "Следеће", "Премотај назад", "Премотај напред")
    AppLanguage.Slovak -> PlaybackNavigationAccessibilityCopy("Predchádzajúce", "Nasledujúce", "Pretočiť späť", "Pretočiť dopredu")
    AppLanguage.Slovenian -> PlaybackNavigationAccessibilityCopy("Prejšnja", "Naslednja", "Previj nazaj", "Previj naprej")
    AppLanguage.Spanish -> PlaybackNavigationAccessibilityCopy("Anterior", "Siguiente", "Retroceder", "Avanzar")
    AppLanguage.Swedish -> PlaybackNavigationAccessibilityCopy("Föregående", "Nästa", "Spola tillbaka", "Spola framåt")
    AppLanguage.Thai -> PlaybackNavigationAccessibilityCopy("ก่อนหน้า", "ถัดไป", "ย้อนกลับ", "เดินหน้า")
    AppLanguage.Ukrainian -> PlaybackNavigationAccessibilityCopy("Попередній", "Наступний", "Перемотати назад", "Перемотати вперед")
    AppLanguage.Urdu -> PlaybackNavigationAccessibilityCopy("پچھلا", "اگلا", "پیچھے جائیں", "آگے جائیں")
}

internal data class ShuffleAccessibilityCopy(val playlist: String, val artist: String)

internal fun shuffleAccessibilityCopy(language: AppLanguage): ShuffleAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> ShuffleAccessibilityCopy("Përziej listën", "Përziej këngët e artistit")
    AppLanguage.Bengali -> ShuffleAccessibilityCopy("প্লেলিস্ট শাফল করুন", "শিল্পীর গান শাফল করুন")
    AppLanguage.Bosnian -> ShuffleAccessibilityCopy("Nasumično pusti plejlistu", "Nasumično pusti izvođača")
    AppLanguage.Bulgarian -> ShuffleAccessibilityCopy("Разбъркай плейлиста", "Разбъркай песните на изпълнителя")
    AppLanguage.Catalan -> ShuffleAccessibilityCopy("Barreja la llista", "Barreja les cançons de l’artista")
    AppLanguage.ChineseSimplified -> ShuffleAccessibilityCopy("随机播放列表", "随机播放艺人的歌曲")
    AppLanguage.Croatian -> ShuffleAccessibilityCopy("Nasumično reproduciraj playlistu", "Nasumično reproduciraj izvođača")
    AppLanguage.Czech -> ShuffleAccessibilityCopy("Náhodně přehrát playlist", "Náhodně přehrát interpreta")
    AppLanguage.Danish -> ShuffleAccessibilityCopy("Bland afspilningslisten", "Bland kunstnerens sange")
    AppLanguage.Dutch -> ShuffleAccessibilityCopy("Afspeellijst in willekeurige volgorde afspelen", "Nummers van artiest in willekeurige volgorde afspelen")
    AppLanguage.English -> ShuffleAccessibilityCopy("Shuffle playlist", "Shuffle artist")
    AppLanguage.Estonian -> ShuffleAccessibilityCopy("Esita esitusloend juhuslikult", "Esita esitaja lood juhuslikult")
    AppLanguage.Finnish -> ShuffleAccessibilityCopy("Sekoita soittolista", "Sekoita esittäjän kappaleet")
    AppLanguage.French -> ShuffleAccessibilityCopy("Mélanger la playlist", "Mélanger les morceaux de l’artiste")
    AppLanguage.German -> ShuffleAccessibilityCopy("Playlist zufällig abspielen", "Titel des Interpreten zufällig abspielen")
    AppLanguage.Greek -> ShuffleAccessibilityCopy("Τυχαία αναπαραγωγή λίστας", "Τυχαία αναπαραγωγή καλλιτέχνη")
    AppLanguage.Hindi -> ShuffleAccessibilityCopy("प्लेलिस्ट शफ़ल करें", "कलाकार के गाने शफ़ल करें")
    AppLanguage.Hungarian -> ShuffleAccessibilityCopy("Lejátszási lista keverése", "Előadó dalainak keverése")
    AppLanguage.Indonesian -> ShuffleAccessibilityCopy("Acak daftar putar", "Acak lagu artis")
    AppLanguage.Italian -> ShuffleAccessibilityCopy("Riproduci playlist in ordine casuale", "Riproduci artista in ordine casuale")
    AppLanguage.Japanese -> ShuffleAccessibilityCopy("プレイリストをシャッフル", "アーティストの曲をシャッフル")
    AppLanguage.Korean -> ShuffleAccessibilityCopy("플레이리스트 무작위 재생", "아티스트 곡 무작위 재생")
    AppLanguage.Latin -> ShuffleAccessibilityCopy("Indicem ordine fortuito redde", "Cantus artificis ordine fortuito redde")
    AppLanguage.Latvian -> ShuffleAccessibilityCopy("Atskaņot sarakstu jauktā secībā", "Atskaņot izpildītāja dziesmas jauktā secībā")
    AppLanguage.Lithuanian -> ShuffleAccessibilityCopy("Maišyti grojaraštį", "Maišyti atlikėjo dainas")
    AppLanguage.Macedonian -> ShuffleAccessibilityCopy("Мешај плејлиста", "Мешај песни од изведувачот")
    AppLanguage.Malay -> ShuffleAccessibilityCopy("Rombak senarai main", "Rombak lagu artis")
    AppLanguage.Norwegian -> ShuffleAccessibilityCopy("Bland spillelisten", "Bland artistens sanger")
    AppLanguage.Polish -> ShuffleAccessibilityCopy("Odtwórz playlistę losowo", "Odtwórz utwory artysty losowo")
    AppLanguage.Portuguese -> ShuffleAccessibilityCopy("Reproduzir playlist aleatoriamente", "Reproduzir artista aleatoriamente")
    AppLanguage.Punjabi -> ShuffleAccessibilityCopy("ਪਲੇਲਿਸਟ ਸ਼ਫ਼ਲ ਕਰੋ", "ਕਲਾਕਾਰ ਦੇ ਗੀਤ ਸ਼ਫ਼ਲ ਕਰੋ")
    AppLanguage.Romanian -> ShuffleAccessibilityCopy("Amestecă lista", "Amestecă melodiile artistului")
    AppLanguage.Russian -> ShuffleAccessibilityCopy("Перемешать плейлист", "Перемешать песни исполнителя")
    AppLanguage.Serbian -> ShuffleAccessibilityCopy("Пусти плејлисту насумично", "Пусти песме извођача насумично")
    AppLanguage.Slovak -> ShuffleAccessibilityCopy("Náhodne prehrať playlist", "Náhodne prehrať interpreta")
    AppLanguage.Slovenian -> ShuffleAccessibilityCopy("Premešaj seznam", "Premešaj skladbe izvajalca")
    AppLanguage.Spanish -> ShuffleAccessibilityCopy("Reproducir playlist aleatoriamente", "Reproducir artista aleatoriamente")
    AppLanguage.Swedish -> ShuffleAccessibilityCopy("Blanda spellistan", "Blanda artistens låtar")
    AppLanguage.Thai -> ShuffleAccessibilityCopy("สุ่มเล่นเพลย์ลิสต์", "สุ่มเล่นเพลงของศิลปิน")
    AppLanguage.Ukrainian -> ShuffleAccessibilityCopy("Перемішати плейлист", "Перемішати пісні виконавця")
    AppLanguage.Urdu -> ShuffleAccessibilityCopy("پلے لسٹ شفل کریں", "فنکار کے گانے شفل کریں")
}

internal fun showAllSongResultsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Shfaq të gjitha rezultatet e këngëve"
    AppLanguage.Bengali -> "গানের সব ফল দেখুন"
    AppLanguage.Bosnian -> "Prikaži sve rezultate pjesama"
    AppLanguage.Bulgarian -> "Покажи всички намерени песни"
    AppLanguage.Catalan -> "Mostra tots els resultats de cançons"
    AppLanguage.ChineseSimplified -> "显示所有歌曲搜索结果"
    AppLanguage.Croatian -> "Prikaži sve rezultate pjesama"
    AppLanguage.Czech -> "Zobrazit všechny nalezené skladby"
    AppLanguage.Danish -> "Vis alle fundne sange"
    AppLanguage.Dutch -> "Alle gevonden nummers tonen"
    AppLanguage.English -> "Show all song results"
    AppLanguage.Estonian -> "Kuva kõik leitud lood"
    AppLanguage.Finnish -> "Näytä kaikki löydetyt kappaleet"
    AppLanguage.French -> "Afficher tous les morceaux trouvés"
    AppLanguage.German -> "Alle gefundenen Titel anzeigen"
    AppLanguage.Greek -> "Εμφάνιση όλων των αποτελεσμάτων τραγουδιών"
    AppLanguage.Hindi -> "गानों के सभी नतीजे दिखाएँ"
    AppLanguage.Hungarian -> "Összes találat megjelenítése a dalok között"
    AppLanguage.Indonesian -> "Tampilkan semua hasil lagu"
    AppLanguage.Italian -> "Mostra tutti i brani trovati"
    AppLanguage.Japanese -> "曲の検索結果をすべて表示"
    AppLanguage.Korean -> "모든 곡 검색 결과 표시"
    AppLanguage.Latin -> "Omnes cantus inventos monstra"
    AppLanguage.Latvian -> "Rādīt visus dziesmu rezultātus"
    AppLanguage.Lithuanian -> "Rodyti visus dainų rezultatus"
    AppLanguage.Macedonian -> "Прикажи ги сите резултати за песни"
    AppLanguage.Malay -> "Tunjukkan semua hasil lagu"
    AppLanguage.Norwegian -> "Vis alle sangene som ble funnet"
    AppLanguage.Polish -> "Pokaż wszystkie znalezione utwory"
    AppLanguage.Portuguese -> "Mostrar todas as músicas encontradas"
    AppLanguage.Punjabi -> "ਗੀਤਾਂ ਦੇ ਸਾਰੇ ਨਤੀਜੇ ਦਿਖਾਓ"
    AppLanguage.Romanian -> "Afișează toate rezultatele pentru melodii"
    AppLanguage.Russian -> "Показать все найденные песни"
    AppLanguage.Serbian -> "Прикажи све резултате за песме"
    AppLanguage.Slovak -> "Zobraziť všetky nájdené skladby"
    AppLanguage.Slovenian -> "Prikaži vse najdene skladbe"
    AppLanguage.Spanish -> "Mostrar todos los resultados de canciones"
    AppLanguage.Swedish -> "Visa alla låtar som hittats"
    AppLanguage.Thai -> "แสดงผลการค้นหาเพลงทั้งหมด"
    AppLanguage.Ukrainian -> "Показати всі знайдені пісні"
    AppLanguage.Urdu -> "گانوں کے تمام نتائج دکھائیں"
}

internal data class ViewModeAccessibilityCopy(val list: String, val compactList: String, val grid: String, val denseGrid: String)

internal fun viewModeAccessibilityCopy(language: AppLanguage): ViewModeAccessibilityCopy = when (language) {
    AppLanguage.Albanian -> ViewModeAccessibilityCopy("Listë", "Listë kompakte", "Rrjetë", "Rrjetë e dendur")
    AppLanguage.Bengali -> ViewModeAccessibilityCopy("তালিকা", "সংক্ষিপ্ত তালিকা", "গ্রিড", "ঘন গ্রিড")
    AppLanguage.Bosnian -> ViewModeAccessibilityCopy("Lista", "Kompaktna lista", "Mreža", "Gusta mreža")
    AppLanguage.Bulgarian -> ViewModeAccessibilityCopy("Списък", "Компактен списък", "Мрежа", "Гъста мрежа")
    AppLanguage.Catalan -> ViewModeAccessibilityCopy("Llista", "Llista compacta", "Graella", "Graella densa")
    AppLanguage.ChineseSimplified -> ViewModeAccessibilityCopy("列表", "紧凑列表", "网格", "密集网格")
    AppLanguage.Croatian -> ViewModeAccessibilityCopy("Popis", "Kompaktni popis", "Mreža", "Gusta mreža")
    AppLanguage.Czech -> ViewModeAccessibilityCopy("Seznam", "Kompaktní seznam", "Mřížka", "Hustá mřížka")
    AppLanguage.Danish -> ViewModeAccessibilityCopy("Liste", "Kompakt liste", "Gitter", "Tæt gitter")
    AppLanguage.Dutch -> ViewModeAccessibilityCopy("Lijst", "Compacte lijst", "Raster", "Dicht raster")
    AppLanguage.English -> ViewModeAccessibilityCopy("List", "Compact list", "Grid", "Dense grid")
    AppLanguage.Estonian -> ViewModeAccessibilityCopy("Loend", "Kompaktne loend", "Ruudustik", "Tihe ruudustik")
    AppLanguage.Finnish -> ViewModeAccessibilityCopy("Luettelo", "Tiivis luettelo", "Ruudukko", "Tiheä ruudukko")
    AppLanguage.French -> ViewModeAccessibilityCopy("Liste", "Liste compacte", "Grille", "Grille dense")
    AppLanguage.German -> ViewModeAccessibilityCopy("Liste", "Kompakte Liste", "Raster", "Dichtes Raster")
    AppLanguage.Greek -> ViewModeAccessibilityCopy("Λίστα", "Συμπαγής λίστα", "Πλέγμα", "Πυκνό πλέγμα")
    AppLanguage.Hindi -> ViewModeAccessibilityCopy("सूची", "संक्षिप्त सूची", "ग्रिड", "सघन ग्रिड")
    AppLanguage.Hungarian -> ViewModeAccessibilityCopy("Lista", "Tömör lista", "Rács", "Sűrű rács")
    AppLanguage.Indonesian -> ViewModeAccessibilityCopy("Daftar", "Daftar ringkas", "Kisi", "Kisi rapat")
    AppLanguage.Italian -> ViewModeAccessibilityCopy("Elenco", "Elenco compatto", "Griglia", "Griglia fitta")
    AppLanguage.Japanese -> ViewModeAccessibilityCopy("リスト", "コンパクトリスト", "グリッド", "高密度グリッド")
    AppLanguage.Korean -> ViewModeAccessibilityCopy("목록", "간단한 목록", "격자", "촘촘한 격자")
    AppLanguage.Latin -> ViewModeAccessibilityCopy("Index", "Index compactus", "Cancellus", "Cancellus densus")
    AppLanguage.Latvian -> ViewModeAccessibilityCopy("Saraksts", "Kompakts saraksts", "Režģis", "Blīvs režģis")
    AppLanguage.Lithuanian -> ViewModeAccessibilityCopy("Sąrašas", "Glaustas sąrašas", "Tinklelis", "Tankus tinklelis")
    AppLanguage.Macedonian -> ViewModeAccessibilityCopy("Листа", "Компактна листа", "Мрежа", "Густа мрежа")
    AppLanguage.Malay -> ViewModeAccessibilityCopy("Senarai", "Senarai padat", "Grid", "Grid rapat")
    AppLanguage.Norwegian -> ViewModeAccessibilityCopy("Liste", "Kompakt liste", "Rutenett", "Tett rutenett")
    AppLanguage.Polish -> ViewModeAccessibilityCopy("Lista", "Lista kompaktowa", "Siatka", "Gęsta siatka")
    AppLanguage.Portuguese -> ViewModeAccessibilityCopy("Lista", "Lista compacta", "Grelha", "Grelha densa")
    AppLanguage.Punjabi -> ViewModeAccessibilityCopy("ਸੂਚੀ", "ਸੰਖੇਪ ਸੂਚੀ", "ਗਰਿੱਡ", "ਸੰਘਣੀ ਗਰਿੱਡ")
    AppLanguage.Romanian -> ViewModeAccessibilityCopy("Listă", "Listă compactă", "Grilă", "Grilă densă")
    AppLanguage.Russian -> ViewModeAccessibilityCopy("Список", "Компактный список", "Сетка", "Плотная сетка")
    AppLanguage.Serbian -> ViewModeAccessibilityCopy("Листа", "Компактна листа", "Мрежа", "Густа мрежа")
    AppLanguage.Slovak -> ViewModeAccessibilityCopy("Zoznam", "Kompaktný zoznam", "Mriežka", "Hustá mriežka")
    AppLanguage.Slovenian -> ViewModeAccessibilityCopy("Seznam", "Strnjen seznam", "Mreža", "Gosta mreža")
    AppLanguage.Spanish -> ViewModeAccessibilityCopy("Lista", "Lista compacta", "Cuadrícula", "Cuadrícula densa")
    AppLanguage.Swedish -> ViewModeAccessibilityCopy("Lista", "Kompakt lista", "Rutnät", "Tätt rutnät")
    AppLanguage.Thai -> ViewModeAccessibilityCopy("รายการ", "รายการแบบกระชับ", "ตาราง", "ตารางแบบหนาแน่น")
    AppLanguage.Ukrainian -> ViewModeAccessibilityCopy("Список", "Компактний список", "Сітка", "Щільна сітка")
    AppLanguage.Urdu -> ViewModeAccessibilityCopy("فہرست", "مختصر فہرست", "گرڈ", "گھنا گرڈ")
}
