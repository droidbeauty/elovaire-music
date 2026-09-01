package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal data class PrivacySafetyCopy(
    val title: String,
    val sections: List<PrivacySafetySectionCopy>,
)

internal data class PrivacySafetySectionCopy(
    val title: String,
    val body: String,
)

internal fun privacyPolicyCopy(language: AppLanguage): PrivacySafetyCopy {
    val title = when (language) {
        AppLanguage.Polish -> "Polityka prywatności"
        AppLanguage.Slovak -> "Zásady ochrany súkromia"
        AppLanguage.Czech -> "Zásady ochrany osobních údajů"
        AppLanguage.German -> "Datenschutzerklärung"
        AppLanguage.French -> "Politique de confidentialité"
        AppLanguage.Spanish -> "Política de privacidad"
        AppLanguage.Portuguese -> "Política de privacidade"
        AppLanguage.Italian -> "Informativa sulla privacy"
        AppLanguage.Dutch -> "Privacybeleid"
        AppLanguage.Danish -> "Privatlivspolitik"
        AppLanguage.Swedish -> "Integritetspolicy"
        AppLanguage.Norwegian -> "Personvern"
        AppLanguage.Estonian -> "Privaatsuspoliitika"
        AppLanguage.Latvian -> "Privātuma politika"
        AppLanguage.Lithuanian -> "Privatumo politika"
        AppLanguage.Hungarian -> "Adatvédelmi szabályzat"
        AppLanguage.Greek -> "Πολιτική απορρήτου"
        AppLanguage.Russian -> "Политика конфиденциальности"
        AppLanguage.Ukrainian -> "Політика конфіденційності"
        AppLanguage.Serbian -> "Политика приватности"
        AppLanguage.Croatian -> "Pravila privatnosti"
        AppLanguage.Macedonian -> "Политика за приватност"
        AppLanguage.Albanian -> "Politika e privatësisë"
        AppLanguage.Hindi -> "गोपनीयता नीति"
        AppLanguage.Bengali -> "গোপনীয়তা নীতি"
        AppLanguage.Urdu -> "رازداری کی پالیسی"
        AppLanguage.ChineseSimplified -> "隐私政策"
        AppLanguage.Japanese -> "プライバシーポリシー"
        AppLanguage.Korean -> "개인정보 처리방침"
        AppLanguage.Thai -> "นโยบายความเป็นส่วนตัว"
        AppLanguage.Malay -> "Dasar privasi"
        AppLanguage.Latin -> "Consilium de secreto"
        AppLanguage.English -> "Privacy policy"
    }
    return PrivacySafetyCopy(
        title = title,
        sections = listOf(
            PrivacySafetySectionCopy(
                "Overview",
                "Elovaire is a local music player distributed through GitHub. It does not require an account, advertising profile, analytics, or cloud music library",
            ),
            PrivacySafetySectionCopy(
                "Music and media files",
                "Elovaire reads the audio files, tags, artwork, and lyrics in folders and documents that you grant it access to. Tag and lyrics edits happen only after you request them and Android allows the write. Complete audio files are not uploaded",
            ),
            PrivacySafetySectionCopy(
                "Online lyrics",
                "When Online lyrics is enabled, Elovaire may send the track title, artist, album, and duration to LRCLIB to retrieve missing lyrics. Lyrics may be cached locally. Turning Online lyrics off prevents LRCLIB lookup and use",
            ),
            PrivacySafetySectionCopy(
                "Local app data",
                "Playlists, smart mixes, favorites, play history, play counts, search history, settings, the library index, artwork cache, and update preferences are stored locally. Android backup may include portable settings; music files, permissions, caches, and operation journals are excluded",
            ),
            PrivacySafetySectionCopy(
                "Permissions",
                "The app may request audio-media access, notifications for playback, document access for folders you choose, USB access for compatible audio devices, and permission to install GitHub updates. Each permission is used only for the related feature",
            ),
            PrivacySafetySectionCopy(
                "Network",
                "The GitHub-only updater checks public releases and downloads an APK and its checksum when you check for an update or an automatic foreground check is due. Remote lyrics and paid metadata services are not used. No music content or search history is sent to GitHub",
            ),
            PrivacySafetySectionCopy(
                "GitHub updates",
                "GitHub receives normal network request information when release metadata or update files are requested. Downloaded files remain in private app storage until Android's installer is opened. Elovaire verifies the checksum, package name, version, and signing certificate before handing an update to Android",
            ),
            PrivacySafetySectionCopy(
                "Sharing, retention, and deletion",
                "Elovaire does not sell or share your music data. Local data remains until you clear app data, remove it through an in-app feature, or uninstall the app. Temporary update files are cleaned when they are no longer needed. The source code and release APKs are published at github.com/droidbeauty/elovaire-music",
            ),
        ),
    )
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

internal fun libraryFoldersCopy(language: AppLanguage): LibraryFoldersCopy {
    fun copy(
        title: String,
        subtitle: String,
        addFolder: String,
        edit: String,
        done: String,
        refresh: String,
        noFoldersTitle: String,
        noFoldersMessage: String,
        unavailable: String,
        unavailableSubtitle: String,
        removalSafety: String,
    ) = LibraryFoldersCopy(title, subtitle, addFolder, edit, done, refresh, noFoldersTitle, noFoldersMessage, unavailable, unavailableSubtitle, removalSafety)
    return when (language) {
        AppLanguage.Polish -> copy("Foldery biblioteki", "Wybierz foldery skanowane w poszukiwaniu muzyki", "Dodaj folder", "Edytuj", "Gotowe", "Odśwież", "Nie wybrano folderów biblioteki", "Dodaj folder, aby muzyka pojawiła się w bibliotece.", "Niedostępne", "Dostęp do folderu jest niedostępny", "Usunięcie folderu usuwa go tylko ze skanowania biblioteki Elovaire. Pliki audio zostają na urządzeniu.")
        AppLanguage.Slovak -> copy("Priečinky knižnice", "Vyberte priečinky skenované pre hudbu", "Pridať priečinok", "Upraviť", "Hotovo", "Obnoviť", "Nie sú vybraté žiadne priečinky knižnice", "Pridajte priečinok, aby sa hudba zobrazila v knižnici.", "Nedostupné", "Prístup k priečinku nie je dostupný", "Odstránenie priečinka ho odstráni iba zo skenovania knižnice Elovaire. Vaše zvukové súbory zostanú v zariadení.")
        AppLanguage.ChineseSimplified -> copy("音乐库文件夹", "选择要扫描音乐的文件夹", "添加文件夹", "编辑", "完成", "刷新", "未选择音乐库文件夹", "添加文件夹后音乐会显示在音乐库中。", "不可用", "文件夹访问不可用", "移除文件夹只会将其从 Elovaire 的音乐库扫描中移除。你的音频文件仍保留在设备上。")
        AppLanguage.Korean -> copy("라이브러리 폴더", "음악을 스캔할 폴더 선택", "폴더 추가", "편집", "완료", "새로 고침", "선택한 라이브러리 폴더가 없습니다", "음악이 라이브러리에 나타나도록 폴더를 추가하세요.", "사용할 수 없음", "폴더 접근을 사용할 수 없습니다", "폴더를 제거해도 Elovaire의 라이브러리 스캔에서만 제외됩니다. 오디오 파일은 기기에 그대로 남습니다.")
        AppLanguage.Czech -> copy("Složky knihovny", "Vyberte složky skenované pro hudbu", "Přidat složku", "Upravit", "Hotovo", "Obnovit", "Nejsou vybrány žádné složky knihovny", "Přidejte složku, aby se hudba zobrazila v knihovně.", "Nedostupné", "Přístup ke složce není dostupný", "Odebrání složky ji odstraní pouze ze skenování knihovny Elovaire. Vaše zvukové soubory zůstanou v zařízení.")
        AppLanguage.Lithuanian -> copy("Bibliotekos aplankai", "Pasirinkite muzikai skenuojamus aplankus", "Pridėti aplanką", "Redaguoti", "Atlikta", "Atnaujinti", "Nepasirinkta bibliotekos aplankų", "Pridėkite aplanką, kad muzika pasirodytų bibliotekoje.", "Nepasiekiama", "Aplanko prieiga nepasiekiama", "Aplanko pašalinimas jį pašalina tik iš Elovaire bibliotekos skenavimo. Jūsų garso failai lieka įrenginyje.")
        AppLanguage.Danish -> copy("Biblioteksmapper", "Vælg mapper der scannes for musik", "Tilføj mappe", "Rediger", "Færdig", "Opdater", "Ingen biblioteksmapper valgt", "Tilføj en mappe for at få musik vist i biblioteket.", "Utilgængelig", "Mappeadgang er utilgængelig", "Når du fjerner en mappe, fjernes den kun fra Elovaires biblioteksscanning. Dine lydfiler bliver på enheden.")
        AppLanguage.French -> copy("Dossiers de bibliothèque", "Choisir les dossiers analysés pour la musique", "Ajouter un dossier", "Modifier", "Terminé", "Actualiser", "Aucun dossier de bibliothèque sélectionné", "Ajoutez un dossier pour faire apparaître la musique dans votre bibliothèque.", "Indisponible", "L’accès au dossier est indisponible", "Retirer un dossier le retire seulement de l’analyse de la bibliothèque d’Elovaire. Vos fichiers audio restent sur votre appareil.")
        AppLanguage.German -> copy("Bibliotheksordner", "Ordner wählen, die nach Musik gescannt werden", "Ordner hinzufügen", "Bearbeiten", "Fertig", "Aktualisieren", "Keine Bibliotheksordner ausgewählt", "Füge einen Ordner hinzu, damit Musik in deiner Bibliothek erscheint.", "Nicht verfügbar", "Ordnerzugriff ist nicht verfügbar", "Das Entfernen eines Ordners entfernt ihn nur aus Elovaires Bibliotheksscan. Deine Audiodateien bleiben auf dem Gerät.")
        AppLanguage.Dutch -> copy("Bibliotheekmappen", "Kies mappen die op muziek worden gescand", "Map toevoegen", "Bewerken", "Gereed", "Vernieuwen", "Geen bibliotheekmappen geselecteerd", "Voeg een map toe om muziek in je bibliotheek te tonen.", "Niet beschikbaar", "Maptoegang is niet beschikbaar", "Een map verwijderen haalt die alleen uit Elovaires bibliotheekscan. Je audiobestanden blijven op je apparaat.")
        AppLanguage.Malay -> copy("Folder pustaka", "Pilih folder yang diimbas untuk muzik", "Tambah folder", "Edit", "Selesai", "Segar semula", "Tiada folder pustaka dipilih", "Tambah folder supaya muzik muncul dalam pustaka anda.", "Tidak tersedia", "Akses folder tidak tersedia", "Mengalih keluar folder hanya membuangnya daripada imbasan pustaka Elovaire. Fail audio anda kekal pada peranti.")
        AppLanguage.Norwegian -> copy("Biblioteksmapper", "Velg mapper som skannes etter musikk", "Legg til mappe", "Rediger", "Ferdig", "Oppdater", "Ingen biblioteksmapper valgt", "Legg til en mappe for at musikk skal vises i biblioteket.", "Utilgjengelig", "Mappetilgang er utilgjengelig", "Å fjerne en mappe fjerner den bare fra Elovaires biblioteksskann. Lydfilene dine blir på enheten.")
        AppLanguage.Swedish -> copy("Biblioteksmappar", "Välj mappar som skannas efter musik", "Lägg till mapp", "Redigera", "Klar", "Uppdatera", "Inga biblioteksmappar valda", "Lägg till en mapp så visas musik i biblioteket.", "Otillgänglig", "Mappåtkomst är otillgänglig", "Att ta bort en mapp tar bara bort den från Elovaires biblioteksskanning. Dina ljudfiler finns kvar på enheten.")
        AppLanguage.Spanish -> copy("Carpetas de biblioteca", "Elige carpetas escaneadas para música", "Añadir carpeta", "Editar", "Listo", "Actualizar", "No hay carpetas de biblioteca seleccionadas", "Añade una carpeta para que la música aparezca en tu biblioteca.", "No disponible", "El acceso a la carpeta no está disponible", "Quitar una carpeta solo la elimina del escaneo de biblioteca de Elovaire. Tus archivos de audio permanecen en el dispositivo.")
        AppLanguage.Portuguese -> copy("Pastas da biblioteca", "Escolha pastas analisadas para música", "Adicionar pasta", "Editar", "Concluído", "Atualizar", "Nenhuma pasta de biblioteca selecionada", "Adicione uma pasta para a música aparecer na biblioteca.", "Indisponível", "O acesso à pasta está indisponível", "Remover uma pasta apenas a retira da análise da biblioteca do Elovaire. Os seus ficheiros de áudio ficam no dispositivo.")
        AppLanguage.Estonian -> copy("Teegi kaustad", "Vali muusika skannimiseks kaustad", "Lisa kaust", "Muuda", "Valmis", "Värskenda", "Ühtegi teegi kausta pole valitud", "Lisa kaust, et muusika ilmuks sinu teeki.", "Pole saadaval", "Kaustale juurdepääs pole saadaval", "Kausta eemaldamine eemaldab selle ainult Elovaire'i teegi skannimisest. Sinu helifailid jäävad seadmesse.")
        AppLanguage.Bengali -> copy("লাইব্রেরি ফোল্ডার", "সঙ্গীত স্ক্যানের জন্য ফোল্ডার বেছে নিন", "ফোল্ডার যোগ করুন", "সম্পাদনা", "সম্পন্ন", "রিফ্রেশ", "কোনো লাইব্রেরি ফোল্ডার নির্বাচিত নয়", "লাইব্রেরিতে সঙ্গীত দেখাতে একটি ফোল্ডার যোগ করুন।", "অনুপলব্ধ", "ফোল্ডার অ্যাক্সেস অনুপলব্ধ", "ফোল্ডার সরালে তা শুধু Elovaire-এর লাইব্রেরি স্ক্যান থেকে সরে যায়। আপনার অডিও ফাইল ডিভাইসে থাকে।")
        AppLanguage.Greek -> copy("Φάκελοι βιβλιοθήκης", "Επιλέξτε φακέλους για σάρωση μουσικής", "Προσθήκη φακέλου", "Επεξεργασία", "Τέλος", "Ανανέωση", "Δεν έχουν επιλεγεί φάκελοι βιβλιοθήκης", "Προσθέστε έναν φάκελο για να εμφανιστεί μουσική στη βιβλιοθήκη.", "Μη διαθέσιμο", "Η πρόσβαση στον φάκελο δεν είναι διαθέσιμη", "Η αφαίρεση φακέλου τον αφαιρεί μόνο από τη σάρωση βιβλιοθήκης του Elovaire. Τα αρχεία ήχου μένουν στη συσκευή.")
        AppLanguage.Croatian -> copy("Mape biblioteke", "Odaberite mape koje se skeniraju za glazbu", "Dodaj mapu", "Uredi", "Gotovo", "Osvježi", "Nema odabranih mapa biblioteke", "Dodajte mapu kako bi se glazba pojavila u biblioteci.", "Nedostupno", "Pristup mapi nije dostupan", "Uklanjanje mape uklanja je samo iz skeniranja biblioteke Elovaire. Vaše audio datoteke ostaju na uređaju.")
        AppLanguage.Russian -> copy("Папки библиотеки", "Выберите папки для сканирования музыки", "Добавить папку", "Изменить", "Готово", "Обновить", "Папки библиотеки не выбраны", "Добавьте папку, чтобы музыка появилась в библиотеке.", "Недоступно", "Доступ к папке недоступен", "Удаление папки убирает её только из сканирования библиотеки Elovaire. Ваши аудиофайлы остаются на устройстве.")
        AppLanguage.Ukrainian -> copy("Папки бібліотеки", "Виберіть папки для сканування музики", "Додати папку", "Редагувати", "Готово", "Оновити", "Папки бібліотеки не вибрано", "Додайте папку, щоб музика з’явилася в бібліотеці.", "Недоступно", "Доступ до папки недоступний", "Видалення папки прибирає її лише зі сканування бібліотеки Elovaire. Ваші аудіофайли залишаються на пристрої.")
        AppLanguage.Urdu -> copy("لائبریری فولڈرز", "موسیقی کے لیے اسکین ہونے والے فولڈرز منتخب کریں", "فولڈر شامل کریں", "ترمیم", "مکمل", "تازہ کریں", "کوئی لائبریری فولڈر منتخب نہیں", "موسیقی لائبریری میں دکھانے کے لیے فولڈر شامل کریں۔", "دستیاب نہیں", "فولڈر تک رسائی دستیاب نہیں", "فولڈر ہٹانے سے وہ صرف Elovaire کی لائبریری اسکین سے نکلتا ہے۔ آپ کی آڈیو فائلیں ڈیوائس پر رہتی ہیں۔")
        AppLanguage.Latvian -> copy("Bibliotēkas mapes", "Izvēlieties mapes, ko skenēt mūzikai", "Pievienot mapi", "Rediģēt", "Gatavs", "Atjaunot", "Nav izvēlētas bibliotēkas mapes", "Pievienojiet mapi, lai mūzika parādītos bibliotēkā.", "Nav pieejams", "Mapes piekļuve nav pieejama", "Mapes noņemšana to izņem tikai no Elovaire bibliotēkas skenēšanas. Audio faili paliek ierīcē.")
        AppLanguage.Italian -> copy("Cartelle libreria", "Scegli le cartelle scansionate per la musica", "Aggiungi cartella", "Modifica", "Fatto", "Aggiorna", "Nessuna cartella libreria selezionata", "Aggiungi una cartella per far apparire musica nella libreria.", "Non disponibile", "L’accesso alla cartella non è disponibile", "Rimuovere una cartella la elimina solo dalla scansione della libreria di Elovaire. I file audio restano sul dispositivo.")
        AppLanguage.Albanian -> copy("Dosjet e bibliotekës", "Zgjidh dosjet që skanohen për muzikë", "Shto dosje", "Redakto", "U krye", "Rifresko", "Nuk është zgjedhur asnjë dosje biblioteke", "Shto një dosje që muzika të shfaqet në bibliotekë.", "E padisponueshme", "Qasja në dosje nuk është e disponueshme", "Heqja e një dosjeje e largon vetëm nga skanimi i bibliotekës së Elovaire. Skedarët audio mbeten në pajisje.")
        AppLanguage.Hindi -> copy("लाइब्रेरी फ़ोल्डर", "संगीत स्कैन के लिए फ़ोल्डर चुनें", "फ़ोल्डर जोड़ें", "संपादित करें", "हो गया", "रीफ़्रेश", "कोई लाइब्रेरी फ़ोल्डर चयनित नहीं", "संगीत को लाइब्रेरी में दिखाने के लिए फ़ोल्डर जोड़ें।", "अनुपलब्ध", "फ़ोल्डर एक्सेस उपलब्ध नहीं है", "फ़ोल्डर हटाने से वह केवल Elovaire की लाइब्रेरी स्कैन से हटता है। आपकी ऑडियो फ़ाइलें डिवाइस पर रहती हैं।")
        AppLanguage.Hungarian -> copy("Könyvtármappák", "Válaszd ki a zenéhez beolvasott mappákat", "Mappa hozzáadása", "Szerkesztés", "Kész", "Frissítés", "Nincs kiválasztott könyvtármappa", "Adj hozzá egy mappát, hogy a zene megjelenjen a könyvtárban.", "Nem elérhető", "A mappa-hozzáférés nem elérhető", "Egy mappa eltávolítása csak az Elovaire könyvtárbeolvasásából veszi ki. A hangfájlok az eszközön maradnak.")
        AppLanguage.Japanese -> copy("ライブラリフォルダ", "音楽をスキャンするフォルダを選択", "フォルダを追加", "編集", "完了", "更新", "ライブラリフォルダが選択されていません", "音楽をライブラリに表示するにはフォルダを追加してください。", "利用不可", "フォルダへのアクセスが利用できません", "フォルダを削除しても Elovaire のライブラリスキャンから外れるだけです。音声ファイルは端末に残ります。")
        AppLanguage.Latin -> copy("Folder bibliothecae", "Elige folders pro musica scrutanda", "Adde folder", "Recense", "Factum", "Renova", "Nulli folders bibliothecae electi", "Adde folder ut musica in bibliotheca appareat.", "Non praesto", "Accessus folder non praesto est", "Folder removere tantum eum e scrutatione bibliothecae Elovaire removet. Fasciculi audio in machina manent.")
        AppLanguage.Macedonian -> copy("Папки на библиотеката", "Изберете папки за скенирање музика", "Додај папка", "Уреди", "Готово", "Освежи", "Нема избрани папки на библиотеката", "Додајте папка за музиката да се појави во библиотеката.", "Недостапно", "Пристапот до папката е недостапен", "Отстранувањето папка ја трга само од скенирањето на библиотеката на Elovaire. Аудио датотеките остануваат на уредот.")
        AppLanguage.Serbian -> copy("Фасцикле библиотеке", "Изаберите фасцикле које се скенирају за музику", "Додај фасциклу", "Уреди", "Готово", "Освежи", "Нема изабраних фасцикли библиотеке", "Додајте фасциклу да би се музика појавила у библиотеци.", "Недоступно", "Приступ фасцикли није доступан", "Уклањање фасцикле је уклања само из скенирања библиотеке Elovaire. Аудио фајлови остају на уређају.")
        AppLanguage.Thai -> copy("โฟลเดอร์คลังเพลง", "เลือกโฟลเดอร์ที่จะสแกนหาเพลง", "เพิ่มโฟลเดอร์", "แก้ไข", "เสร็จ", "รีเฟรช", "ยังไม่ได้เลือกโฟลเดอร์คลังเพลง", "เพิ่มโฟลเดอร์เพื่อให้เพลงปรากฏในคลังเพลง", "ไม่พร้อมใช้งาน", "ไม่สามารถเข้าถึงโฟลเดอร์ได้", "การลบโฟลเดอร์จะลบออกจากการสแกนคลังของ Elovaire เท่านั้น ไฟล์เสียงของคุณยังอยู่บนอุปกรณ์")
        AppLanguage.English -> copy("Library folders", "Choose folders scanned for music", "Add folder", "Edit", "Done", "Refresh", "No library folders selected", "Add a folder to make music appear in your library.", "Unavailable", "Folder access is unavailable", "Removing a folder only removes it from Elovaire's library scan. Your audio files stay on your device.")
    }
}

internal data class NetworkSourcesCopy(
    val sectionTitle: String,
    val available: String,
    val checking: String,
    val signIn: String,
    val allowLocalNetwork: String,
    val addSource: String,
    val closeSourcePicker: String,
    val chooseFolderSubtitle: String,
    val nasTitle: String,
    val nasSubtitle: String,
    val removeTitle: String,
    val removeMessage: String,
    val remove: String,
    val editorTitle: String,
    val saveEditor: String,
    val name: String,
    val server: String,
    val httpsServer: String,
    val sharePath: String,
    val path: String,
    val username: String,
    val domainOptional: String,
    val password: String,
    val connectionAvailable: String,
    val allowLocalNetworkSettings: String,
    val authenticationRequired: String,
    val hostUnreachable: String,
    val checkServerPath: String,
    val sourceUnavailable: String,
    val testingConnection: String,
)

internal fun networkSourcesCopy(language: AppLanguage): NetworkSourcesCopy = when (language) {
    AppLanguage.Albanian -> NetworkSourcesCopy("Burimet e rrjetit", "E disponueshme", "Po testohet…", "Hyr", "Lejo rrjetin lokal", "Shto burim", "Mbyll zgjedhësin e burimit", "Zgjidh një dosje në këtë pajisje", "NAS ose WebDAV", "Lidhu me një burim muzikor në rrjet", "Të hiqet burimi i rrjetit?", "Kjo e heq burimin nga biblioteka. Skedarët në rrjet nuk fshihen.", "Hiq", "Burim rrjeti", "Ruaj burimin e rrjetit", "Emri", "Serveri", "Serveri HTTPS", "Shtegu i ndarjes", "Shtegu", "Emri i përdoruesit", "Domeni / grupi i punës (opsional)", "Fjalëkalimi", "Lidhja është e disponueshme", "Lejo qasjen në rrjetin lokal te cilësimet e Android", "Kërkohet vërtetimi", "Hosti nuk arrihet", "Kontrollo serverin dhe shtegun", "Burimi i rrjetit nuk është i disponueshëm", "Po testohet lidhja…")
    AppLanguage.Bengali -> NetworkSourcesCopy("নেটওয়ার্ক সোর্স", "উপলভ্য", "পরীক্ষা চলছে…", "সাইন ইন", "স্থানীয় নেটওয়ার্কের অনুমতি দিন", "সোর্স যোগ করুন", "সোর্স বাছাই বন্ধ করুন", "এই ডিভাইসে একটি ফোল্ডার বেছে নিন", "NAS বা WebDAV", "একটি নেটওয়ার্ক মিউজিক সোর্সে সংযোগ করুন", "নেটওয়ার্ক সোর্স সরাবেন?", "এটি শুধু লাইব্রেরি থেকে সোর্সটি সরাবে। নেটওয়ার্কের ফাইল মুছে যাবে না।", "সরান", "নেটওয়ার্ক সোর্স", "নেটওয়ার্ক সোর্স সংরক্ষণ করুন", "নাম", "সার্ভার", "HTTPS সার্ভার", "শেয়ার / পাথ", "পাথ", "ব্যবহারকারীর নাম", "ডোমেইন / ওয়ার্কগ্রুপ (ঐচ্ছিক)", "পাসওয়ার্ড", "সংযোগ উপলভ্য", "Android সেটিংসে স্থানীয় নেটওয়ার্ক অ্যাক্সেসের অনুমতি দিন", "প্রমাণীকরণ প্রয়োজন", "হোস্টে পৌঁছানো যাচ্ছে না", "সার্ভার ও পাথ পরীক্ষা করুন", "নেটওয়ার্ক সোর্স উপলভ্য নয়", "সংযোগ পরীক্ষা চলছে…")
    AppLanguage.ChineseSimplified -> NetworkSourcesCopy("网络来源", "可用", "正在测试…", "登录", "允许访问本地网络", "添加来源", "关闭来源选择器", "选择此设备上的文件夹", "NAS 或 WebDAV", "连接到网络音乐来源", "要移除网络来源吗？", "这只会将来源从媒体库中移除，不会删除网络上的文件。", "移除", "网络来源", "保存网络来源", "名称", "服务器", "HTTPS 服务器", "共享 / 路径", "路径", "用户名", "域 / 工作组（可选）", "密码", "连接可用", "在 Android 设置中允许访问本地网络", "需要身份验证", "无法访问主机", "检查服务器和路径", "网络来源不可用", "正在测试连接…")
    AppLanguage.Croatian -> NetworkSourcesCopy("Mrežni izvori", "Dostupno", "Testiranje…", "Prijava", "Dopusti lokalnu mrežu", "Dodaj izvor", "Zatvori birač izvora", "Odaberite mapu na ovom uređaju", "NAS ili WebDAV", "Povežite se s mrežnim izvorom glazbe", "Ukloniti mrežni izvor?", "Ovo uklanja izvor samo iz biblioteke. Datoteke na mreži neće biti izbrisane.", "Ukloni", "Mrežni izvor", "Spremi mrežni izvor", "Naziv", "Poslužitelj", "HTTPS poslužitelj", "Dijeljenje / putanja", "Putanja", "Korisničko ime", "Domena / radna grupa (neobavezno)", "Lozinka", "Veza je dostupna", "Dopustite lokalni mrežni pristup u postavkama Androida", "Potrebna je provjera autentičnosti", "Domaćin nije dostupan", "Provjerite poslužitelj i putanju", "Mrežni izvor nije dostupan", "Testiranje veze…")
    AppLanguage.Czech -> NetworkSourcesCopy("Síťové zdroje", "Dostupné", "Probíhá test…", "Přihlásit se", "Povolit místní síť", "Přidat zdroj", "Zavřít výběr zdroje", "Vyberte složku v tomto zařízení", "NAS nebo WebDAV", "Připojte se k síťovému zdroji hudby", "Odebrat síťový zdroj?", "Tím se zdroj odebere pouze z knihovny. Soubory v síti nebudou smazány.", "Odebrat", "Síťový zdroj", "Uložit síťový zdroj", "Název", "Server", "Server HTTPS", "Sdílení / cesta", "Cesta", "Uživatelské jméno", "Doména / pracovní skupina (volitelné)", "Heslo", "Připojení je dostupné", "Povolte přístup k místní síti v nastavení Androidu", "Je vyžadováno ověření", "Hostitel není dostupný", "Zkontrolujte server a cestu", "Síťový zdroj není dostupný", "Testování připojení…")
    AppLanguage.Danish -> NetworkSourcesCopy("Netværkskilder", "Tilgængelig", "Tester…", "Log ind", "Tillad lokalt netværk", "Tilføj kilde", "Luk kildevælger", "Vælg en mappe på denne enhed", "NAS eller WebDAV", "Opret forbindelse til en netværksmusikkilde", "Fjern netværkskilde?", "Dette fjerner kun kilden fra biblioteket. Filer på netværket slettes ikke.", "Fjern", "Netværkskilde", "Gem netværkskilde", "Navn", "Server", "HTTPS-server", "Deling / sti", "Sti", "Brugernavn", "Domæne / arbejdsgruppe (valgfrit)", "Adgangskode", "Forbindelsen er tilgængelig", "Tillad adgang til lokalt netværk i Android-indstillinger", "Godkendelse påkrævet", "Værten kan ikke nås", "Kontrollér server og sti", "Netværkskilden er ikke tilgængelig", "Tester forbindelsen…")
    AppLanguage.Dutch -> NetworkSourcesCopy("Netwerkbronnen", "Beschikbaar", "Testen…", "Inloggen", "Lokaal netwerk toestaan", "Bron toevoegen", "Bronkiezer sluiten", "Kies een map op dit apparaat", "NAS of WebDAV", "Verbind met een netwerkbron voor muziek", "Netwerkbron verwijderen?", "Hiermee wordt de bron alleen uit de bibliotheek verwijderd. Bestanden op het netwerk worden niet verwijderd.", "Verwijderen", "Netwerkbron", "Netwerkbron opslaan", "Naam", "Server", "HTTPS-server", "Share / pad", "Pad", "Gebruikersnaam", "Domein / werkgroep (optioneel)", "Wachtwoord", "Verbinding beschikbaar", "Sta lokale netwerktoegang toe in de Android-instellingen", "Authenticatie vereist", "Host is onbereikbaar", "Controleer de server en het pad", "Netwerkbron is niet beschikbaar", "Verbinding testen…")
    AppLanguage.Estonian -> NetworkSourcesCopy("Võrguallikad", "Saadaval", "Testimine…", "Logi sisse", "Luba kohalik võrk", "Lisa allikas", "Sulge allikavalija", "Vali selles seadmes kaust", "NAS või WebDAV", "Ühenda võrgu muusikaallikaga", "Kas eemaldada võrguallikas?", "See eemaldab allika ainult teegist. Võrgus olevaid faile ei kustutata.", "Eemalda", "Võrguallikas", "Salvesta võrguallikas", "Nimi", "Server", "HTTPS-server", "Jaga / tee", "Tee", "Kasutajanimi", "Domeen / töörühm (valikuline)", "Parool", "Ühendus on saadaval", "Luba Androidi seadetes juurdepääs kohalikule võrgule", "Nõutav on autentimine", "Hosti ei saa kätte", "Kontrolli serverit ja teed", "Võrguallikas pole saadaval", "Ühenduse testimine…")
    AppLanguage.French -> NetworkSourcesCopy("Sources réseau", "Disponible", "Test en cours…", "Se connecter", "Autoriser le réseau local", "Ajouter une source", "Fermer le sélecteur de source", "Choisissez un dossier sur cet appareil", "NAS ou WebDAV", "Connectez-vous à une source de musique réseau", "Retirer la source réseau ?", "Cette action retire uniquement la source de la bibliothèque. Les fichiers du réseau ne sont pas supprimés.", "Retirer", "Source réseau", "Enregistrer la source réseau", "Nom", "Serveur", "Serveur HTTPS", "Partage / chemin", "Chemin", "Nom d’utilisateur", "Domaine / groupe de travail (facultatif)", "Mot de passe", "Connexion disponible", "Autorisez l’accès au réseau local dans les réglages Android", "Authentification requise", "Hôte inaccessible", "Vérifiez le serveur et le chemin", "Source réseau indisponible", "Test de la connexion…")
    AppLanguage.German -> NetworkSourcesCopy("Netzwerkquellen", "Verfügbar", "Wird getestet…", "Anmelden", "Lokales Netzwerk erlauben", "Quelle hinzufügen", "Quellenauswahl schließen", "Wähle einen Ordner auf diesem Gerät", "NAS oder WebDAV", "Mit einer Netzwerk-Musikquelle verbinden", "Netzwerkquelle entfernen?", "Dadurch wird die Quelle nur aus der Bibliothek entfernt. Dateien im Netzwerk werden nicht gelöscht.", "Entfernen", "Netzwerkquelle", "Netzwerkquelle speichern", "Name", "Server", "HTTPS-Server", "Freigabe / Pfad", "Pfad", "Benutzername", "Domäne / Arbeitsgruppe (optional)", "Passwort", "Verbindung verfügbar", "Erlaube den Zugriff auf das lokale Netzwerk in den Android-Einstellungen", "Authentifizierung erforderlich", "Host nicht erreichbar", "Server und Pfad prüfen", "Netzwerkquelle nicht verfügbar", "Verbindung wird getestet…")
    AppLanguage.Greek -> NetworkSourcesCopy("Πηγές δικτύου", "Διαθέσιμη", "Γίνεται δοκιμή…", "Σύνδεση", "Να επιτρέπεται τοπικό δίκτυο", "Προσθήκη πηγής", "Κλείσιμο επιλογέα πηγής", "Επιλέξτε έναν φάκελο σε αυτήν τη συσκευή", "NAS ή WebDAV", "Συνδεθείτε σε πηγή μουσικής δικτύου", "Αφαίρεση πηγής δικτύου;", "Η πηγή θα αφαιρεθεί μόνο από τη βιβλιοθήκη. Τα αρχεία στο δίκτυο δεν θα διαγραφούν.", "Αφαίρεση", "Πηγή δικτύου", "Αποθήκευση πηγής δικτύου", "Όνομα", "Διακομιστής", "Διακομιστής HTTPS", "Κοινόχρηστη διαδρομή", "Διαδρομή", "Όνομα χρήστη", "Τομέας / ομάδα εργασίας (προαιρετικό)", "Κωδικός πρόσβασης", "Η σύνδεση είναι διαθέσιμη", "Επιτρέψτε την πρόσβαση στο τοπικό δίκτυο από τις ρυθμίσεις Android", "Απαιτείται έλεγχος ταυτότητας", "Ο κεντρικός υπολογιστής δεν είναι προσβάσιμος", "Ελέγξτε τον διακομιστή και τη διαδρομή", "Η πηγή δικτύου δεν είναι διαθέσιμη", "Γίνεται δοκιμή σύνδεσης…")
    AppLanguage.Hindi -> NetworkSourcesCopy("नेटवर्क स्रोत", "उपलब्ध", "जाँच हो रही है…", "साइन इन करें", "स्थानीय नेटवर्क की अनुमति दें", "स्रोत जोड़ें", "स्रोत चयनकर्ता बंद करें", "इस डिवाइस पर कोई फ़ोल्डर चुनें", "NAS या WebDAV", "नेटवर्क संगीत स्रोत से कनेक्ट करें", "नेटवर्क स्रोत हटाएँ?", "यह स्रोत को केवल लाइब्रेरी से हटाता है। नेटवर्क की फ़ाइलें हटाई नहीं जाएँगी।", "हटाएँ", "नेटवर्क स्रोत", "नेटवर्क स्रोत सहेजें", "नाम", "सर्वर", "HTTPS सर्वर", "शेयर / पथ", "पथ", "उपयोगकर्ता नाम", "डोमेन / वर्कग्रुप (वैकल्पिक)", "पासवर्ड", "कनेक्शन उपलब्ध है", "Android सेटिंग्स में स्थानीय नेटवर्क एक्सेस की अनुमति दें", "प्रमाणीकरण आवश्यक है", "होस्ट तक पहुँचा नहीं जा सकता", "सर्वर और पथ जाँचें", "नेटवर्क स्रोत उपलब्ध नहीं है", "कनेक्शन की जाँच हो रही है…")
    AppLanguage.Hungarian -> NetworkSourcesCopy("Hálózati források", "Elérhető", "Tesztelés…", "Bejelentkezés", "Helyi hálózat engedélyezése", "Forrás hozzáadása", "Forrásválasztó bezárása", "Válassz mappát ezen az eszközön", "NAS vagy WebDAV", "Kapcsolódj hálózati zenei forráshoz", "Eltávolítod a hálózati forrást?", "A forrás csak a könyvtárból lesz eltávolítva. A hálózaton lévő fájlok nem törlődnek.", "Eltávolítás", "Hálózati forrás", "Hálózati forrás mentése", "Név", "Szerver", "HTTPS-szerver", "Megosztás / elérési út", "Elérési út", "Felhasználónév", "Tartomány / munkacsoport (nem kötelező)", "Jelszó", "A kapcsolat elérhető", "Engedélyezd a helyi hálózati hozzáférést az Android beállításaiban", "Hitelesítés szükséges", "A gazdagép nem érhető el", "Ellenőrizd a szervert és az elérési utat", "A hálózati forrás nem érhető el", "Kapcsolat tesztelése…")
    AppLanguage.Italian -> NetworkSourcesCopy("Sorgenti di rete", "Disponibile", "Test in corso…", "Accedi", "Consenti rete locale", "Aggiungi sorgente", "Chiudi selettore sorgente", "Scegli una cartella su questo dispositivo", "NAS o WebDAV", "Connettiti a una sorgente musicale di rete", "Rimuovere la sorgente di rete?", "La sorgente verrà rimossa solo dalla libreria. I file sulla rete non verranno eliminati.", "Rimuovi", "Sorgente di rete", "Salva sorgente di rete", "Nome", "Server", "Server HTTPS", "Condivisione / percorso", "Percorso", "Nome utente", "Dominio / gruppo di lavoro (facoltativo)", "Password", "Connessione disponibile", "Consenti l’accesso alla rete locale nelle impostazioni Android", "Autenticazione richiesta", "Host irraggiungibile", "Controlla server e percorso", "Sorgente di rete non disponibile", "Test della connessione…")
    AppLanguage.Japanese -> NetworkSourcesCopy("ネットワークソース", "利用可能", "テスト中…", "サインイン", "ローカルネットワークを許可", "ソースを追加", "ソース選択を閉じる", "この端末のフォルダを選択", "NAS または WebDAV", "ネットワーク上の音楽ソースに接続", "ネットワークソースを削除しますか？", "ソースをライブラリからのみ削除します。ネットワーク上のファイルは削除されません。", "削除", "ネットワークソース", "ネットワークソースを保存", "名前", "サーバー", "HTTPS サーバー", "共有 / パス", "パス", "ユーザー名", "ドメイン / ワークグループ（任意）", "パスワード", "接続可能", "Android の設定でローカルネットワークへのアクセスを許可", "認証が必要です", "ホストに接続できません", "サーバーとパスを確認", "ネットワークソースを利用できません", "接続をテスト中…")
    AppLanguage.Korean -> NetworkSourcesCopy("네트워크 소스", "사용 가능", "테스트 중…", "로그인", "로컬 네트워크 허용", "소스 추가", "소스 선택기 닫기", "이 기기에서 폴더를 선택하세요", "NAS 또는 WebDAV", "네트워크 음악 소스에 연결", "네트워크 소스를 삭제할까요?", "소스를 라이브러리에서만 삭제합니다. 네트워크의 파일은 삭제되지 않습니다.", "삭제", "네트워크 소스", "네트워크 소스 저장", "이름", "서버", "HTTPS 서버", "공유 / 경로", "경로", "사용자 이름", "도메인 / 작업 그룹(선택사항)", "비밀번호", "연결 가능", "Android 설정에서 로컬 네트워크 액세스를 허용하세요", "인증이 필요합니다", "호스트에 연결할 수 없습니다", "서버와 경로를 확인하세요", "네트워크 소스를 사용할 수 없습니다", "연결 테스트 중…")
    AppLanguage.Latin -> NetworkSourcesCopy("Fontes retis", "Praesto", "Probatur…", "Intra", "Retem localem permitte", "Adde fontem", "Claudere selectorem fontis", "Elige folder in hac machina", "NAS vel WebDAV", "Connecte ad fontem musicum retis", "Fontem retis removere?", "Hic fontem tantum e bibliotheca removet. Fasciculi in rete non delebuntur.", "Amove", "Fons retis", "Fontem retis serva", "Nomen", "Servitor", "Servitor HTTPS", "Communicatio / semita", "Semita", "Nomen usoris", "Dominium / coetus laboris (optional)", "Tessera", "Connexio praesto est", "Permitte accessum ad rete locale in optionibus Android", "Agnitio necessaria est", "Hospes attingi non potest", "Servitorem et semitam verifica", "Fons retis non praesto est", "Connexio probatur…")
    AppLanguage.Latvian -> NetworkSourcesCopy("Tīkla avoti", "Pieejams", "Notiek pārbaude…", "Pierakstīties", "Atļaut lokālo tīklu", "Pievienot avotu", "Aizvērt avota izvēli", "Izvēlieties mapi šajā ierīcē", "NAS vai WebDAV", "Izveidojiet savienojumu ar tīkla mūzikas avotu", "Noņemt tīkla avotu?", "Avots tiks noņemts tikai no bibliotēkas. Tīklā esošie faili netiks dzēsti.", "Noņemt", "Tīkla avots", "Saglabāt tīkla avotu", "Nosaukums", "Serveris", "HTTPS serveris", "Koplietojums / ceļš", "Ceļš", "Lietotājvārds", "Domēns / darba grupa (neobligāti)", "Parole", "Savienojums ir pieejams", "Atļaujiet piekļuvi lokālajam tīklam Android iestatījumos", "Nepieciešama autentifikācija", "Resursdators nav sasniedzams", "Pārbaudiet serveri un ceļu", "Tīkla avots nav pieejams", "Notiek savienojuma pārbaude…")
    AppLanguage.Lithuanian -> NetworkSourcesCopy("Tinklo šaltiniai", "Pasiekiama", "Tikrinama…", "Prisijungti", "Leisti vietinį tinklą", "Pridėti šaltinį", "Uždaryti šaltinių pasirinkimą", "Pasirinkite aplanką šiame įrenginyje", "NAS arba WebDAV", "Prisijunkite prie tinklo muzikos šaltinio", "Pašalinti tinklo šaltinį?", "Šaltinis bus pašalintas tik iš bibliotekos. Tinkle esantys failai nebus ištrinti.", "Pašalinti", "Tinklo šaltinis", "Išsaugoti tinklo šaltinį", "Pavadinimas", "Serveris", "HTTPS serveris", "Bendrinimas / kelias", "Kelias", "Naudotojo vardas", "Domenas / darbo grupė (nebūtina)", "Slaptažodis", "Ryšys pasiekiamas", "Leiskite prieigą prie vietinio tinklo „Android“ nustatymuose", "Reikia autentifikavimo", "Pagrindinis kompiuteris nepasiekiamas", "Patikrinkite serverį ir kelią", "Tinklo šaltinis nepasiekiamas", "Tikrinamas ryšys…")
    AppLanguage.Malay -> NetworkSourcesCopy("Sumber rangkaian", "Tersedia", "Sedang diuji…", "Log masuk", "Benarkan rangkaian tempatan", "Tambah sumber", "Tutup pemilih sumber", "Pilih folder pada peranti ini", "NAS atau WebDAV", "Sambung kepada sumber muzik rangkaian", "Alih keluar sumber rangkaian?", "Ini hanya mengalih keluar sumber daripada pustaka. Fail pada rangkaian tidak dipadamkan.", "Alih keluar", "Sumber rangkaian", "Simpan sumber rangkaian", "Nama", "Pelayan", "Pelayan HTTPS", "Kongsi / laluan", "Laluan", "Nama pengguna", "Domain / kumpulan kerja (pilihan)", "Kata laluan", "Sambungan tersedia", "Benarkan akses rangkaian tempatan dalam tetapan Android", "Pengesahan diperlukan", "Hos tidak dapat dicapai", "Semak pelayan dan laluan", "Sumber rangkaian tidak tersedia", "Sedang menguji sambungan…")
    AppLanguage.Macedonian -> NetworkSourcesCopy("Мрежни извори", "Достапно", "Се тестира…", "Најави се", "Дозволи локална мрежа", "Додај извор", "Затвори го избирачот на извори", "Изберете папка на овој уред", "NAS или WebDAV", "Поврзете се со мрежен извор на музика", "Да се отстрани мрежниот извор?", "Изворот ќе се отстрани само од библиотеката. Датотеките на мрежата нема да се избришат.", "Отстрани", "Мрежен извор", "Зачувај мрежен извор", "Име", "Сервер", "HTTPS-сервер", "Споделување / патека", "Патека", "Корисничко име", "Домен / работна група (изборно)", "Лозинка", "Врската е достапна", "Дозволете пристап до локалната мрежа во поставките на Android", "Потребна е автентикација", "Хостот не е достапен", "Проверете ги серверот и патеката", "Мрежниот извор не е достапен", "Се тестира врската…")
    AppLanguage.Norwegian -> NetworkSourcesCopy("Nettverkskilder", "Tilgjengelig", "Tester…", "Logg inn", "Tillat lokalt nettverk", "Legg til kilde", "Lukk kildevelger", "Velg en mappe på denne enheten", "NAS eller WebDAV", "Koble til en nettverkskilde for musikk", "Fjerne nettverkskilden?", "Dette fjerner bare kilden fra biblioteket. Filer på nettverket slettes ikke.", "Fjern", "Nettverkskilde", "Lagre nettverkskilde", "Navn", "Server", "HTTPS-server", "Deling / bane", "Bane", "Brukernavn", "Domene / arbeidsgruppe (valgfritt)", "Passord", "Tilkoblingen er tilgjengelig", "Tillat tilgang til lokalt nettverk i Android-innstillingene", "Autentisering kreves", "Verten kan ikke nås", "Kontroller server og bane", "Nettverkskilden er ikke tilgjengelig", "Tester tilkoblingen…")
    AppLanguage.Polish -> NetworkSourcesCopy("Źródła sieciowe", "Dostępne", "Testowanie…", "Zaloguj się", "Zezwól na dostęp do sieci lokalnej", "Dodaj źródło", "Zamknij wybór źródła", "Wybierz folder na tym urządzeniu", "NAS lub WebDAV", "Połącz z sieciowym źródłem muzyki", "Usunąć źródło sieciowe?", "Źródło zostanie usunięte tylko z biblioteki. Pliki w sieci nie zostaną usunięte.", "Usuń", "Źródło sieciowe", "Zapisz źródło sieciowe", "Nazwa", "Serwer", "Serwer HTTPS", "Udział / ścieżka", "Ścieżka", "Nazwa użytkownika", "Domena / grupa robocza (opcjonalnie)", "Hasło", "Połączenie jest dostępne", "Zezwól na dostęp do sieci lokalnej w ustawieniach Androida", "Wymagane uwierzytelnianie", "Nie można połączyć się z hostem", "Sprawdź serwer i ścieżkę", "Źródło sieciowe jest niedostępne", "Testowanie połączenia…")
    AppLanguage.Portuguese -> NetworkSourcesCopy("Fontes de rede", "Disponível", "A testar…", "Iniciar sessão", "Permitir rede local", "Adicionar fonte", "Fechar seletor de fontes", "Escolha uma pasta neste dispositivo", "NAS ou WebDAV", "Ligue-se a uma fonte de música de rede", "Remover a fonte de rede?", "A fonte será removida apenas da biblioteca. Os ficheiros na rede não serão eliminados.", "Remover", "Fonte de rede", "Guardar fonte de rede", "Nome", "Servidor", "Servidor HTTPS", "Partilha / caminho", "Caminho", "Nome de utilizador", "Domínio / grupo de trabalho (opcional)", "Palavra-passe", "Ligação disponível", "Permita o acesso à rede local nas definições do Android", "Autenticação necessária", "Não é possível aceder ao anfitrião", "Verifique o servidor e o caminho", "A fonte de rede não está disponível", "A testar a ligação…")
    AppLanguage.Russian -> NetworkSourcesCopy("Сетевые источники", "Доступно", "Проверка…", "Войти", "Разрешить локальную сеть", "Добавить источник", "Закрыть выбор источника", "Выберите папку на этом устройстве", "NAS или WebDAV", "Подключитесь к сетевому источнику музыки", "Удалить сетевой источник?", "Источник будет удалён только из библиотеки. Файлы в сети не будут удалены.", "Удалить", "Сетевой источник", "Сохранить сетевой источник", "Название", "Сервер", "HTTPS-сервер", "Общий ресурс / путь", "Путь", "Имя пользователя", "Домен / рабочая группа (необязательно)", "Пароль", "Соединение доступно", "Разрешите доступ к локальной сети в настройках Android", "Требуется аутентификация", "Хост недоступен", "Проверьте сервер и путь", "Сетевой источник недоступен", "Проверка соединения…")
    AppLanguage.Serbian -> NetworkSourcesCopy("Мрежни извори", "Доступно", "Тестирање…", "Пријави се", "Дозволи локалну мрежу", "Додај извор", "Затвори бирач извора", "Изаберите фасциклу на овом уређају", "NAS или WebDAV", "Повежите се са мрежним извором музике", "Уклонити мрежни извор?", "Ово уклања извор само из библиотеке. Датотеке на мрежи неће бити обрисане.", "Уклони", "Мрежни извор", "Сачувај мрежни извор", "Назив", "Сервер", "HTTPS сервер", "Дељење / путања", "Путања", "Корисничко име", "Домен / радна група (опционо)", "Лозинка", "Веза је доступна", "Дозволите приступ локалној мрежи у Android подешавањима", "Потребна је аутентификација", "Хост није доступан", "Проверите сервер и путању", "Мрежни извор није доступан", "Тестирање везе…")
    AppLanguage.Slovak -> NetworkSourcesCopy("Sieťové zdroje", "Dostupné", "Testuje sa…", "Prihlásiť sa", "Povoliť lokálnu sieť", "Pridať zdroj", "Zavrieť výber zdroja", "Vyberte priečinok v tomto zariadení", "NAS alebo WebDAV", "Pripojte sa k sieťovému zdroju hudby", "Odstrániť sieťový zdroj?", "Zdroj sa odstráni iba z knižnice. Súbory v sieti sa neodstránia.", "Odstrániť", "Sieťový zdroj", "Uložiť sieťový zdroj", "Názov", "Server", "HTTPS server", "Zdieľanie / cesta", "Cesta", "Používateľské meno", "Doména / pracovná skupina (voliteľné)", "Heslo", "Pripojenie je dostupné", "Povoľte prístup k lokálnej sieti v nastaveniach Androidu", "Vyžaduje sa overenie", "Hostiteľ je nedostupný", "Skontrolujte server a cestu", "Sieťový zdroj nie je dostupný", "Testuje sa pripojenie…")
    AppLanguage.Spanish -> NetworkSourcesCopy("Fuentes de red", "Disponible", "Comprobando…", "Iniciar sesión", "Permitir red local", "Añadir fuente", "Cerrar selector de fuentes", "Elige una carpeta en este dispositivo", "NAS o WebDAV", "Conéctate a una fuente de música de red", "¿Quitar la fuente de red?", "La fuente solo se quitará de la biblioteca. Los archivos de la red no se eliminarán.", "Quitar", "Fuente de red", "Guardar fuente de red", "Nombre", "Servidor", "Servidor HTTPS", "Recurso compartido / ruta", "Ruta", "Nombre de usuario", "Dominio / grupo de trabajo (opcional)", "Contraseña", "Conexión disponible", "Permite el acceso a la red local en los ajustes de Android", "Autenticación necesaria", "No se puede acceder al host", "Comprueba el servidor y la ruta", "La fuente de red no está disponible", "Comprobando la conexión…")
    AppLanguage.Swedish -> NetworkSourcesCopy("Nätverkskällor", "Tillgänglig", "Testar…", "Logga in", "Tillåt lokalt nätverk", "Lägg till källa", "Stäng källväljaren", "Välj en mapp på den här enheten", "NAS eller WebDAV", "Anslut till en nätverkskälla för musik", "Ta bort nätverkskälla?", "Källan tas bara bort från biblioteket. Filer på nätverket raderas inte.", "Ta bort", "Nätverkskälla", "Spara nätverkskälla", "Namn", "Server", "HTTPS-server", "Delning / sökväg", "Sökväg", "Användarnamn", "Domän / arbetsgrupp (valfritt)", "Lösenord", "Anslutningen är tillgänglig", "Tillåt åtkomst till lokalt nätverk i Android-inställningarna", "Autentisering krävs", "Värden kan inte nås", "Kontrollera server och sökväg", "Nätverkskällan är inte tillgänglig", "Testar anslutningen…")
    AppLanguage.Thai -> NetworkSourcesCopy("แหล่งที่มาบนเครือข่าย", "พร้อมใช้งาน", "กำลังทดสอบ…", "ลงชื่อเข้าใช้", "อนุญาตเครือข่ายในพื้นที่", "เพิ่มแหล่งที่มา", "ปิดตัวเลือกแหล่งที่มา", "เลือกโฟลเดอร์ในอุปกรณ์นี้", "NAS หรือ WebDAV", "เชื่อมต่อกับแหล่งเพลงบนเครือข่าย", "ลบแหล่งที่มาบนเครือข่ายไหม", "การดำเนินการนี้จะลบแหล่งที่มาออกจากคลังเท่านั้น ไฟล์ในเครือข่ายจะไม่ถูกลบ", "ลบ", "แหล่งที่มาบนเครือข่าย", "บันทึกแหล่งที่มาบนเครือข่าย", "ชื่อ", "เซิร์ฟเวอร์", "เซิร์ฟเวอร์ HTTPS", "การแชร์ / เส้นทาง", "เส้นทาง", "ชื่อผู้ใช้", "โดเมน / เวิร์กกรุ๊ป (ไม่บังคับ)", "รหัสผ่าน", "การเชื่อมต่อพร้อมใช้งาน", "อนุญาตการเข้าถึงเครือข่ายในพื้นที่ในการตั้งค่า Android", "ต้องมีการยืนยันตัวตน", "ไม่สามารถเข้าถึงโฮสต์ได้", "ตรวจสอบเซิร์ฟเวอร์และเส้นทาง", "แหล่งที่มาบนเครือข่ายไม่พร้อมใช้งาน", "กำลังทดสอบการเชื่อมต่อ…")
    AppLanguage.Ukrainian -> NetworkSourcesCopy("Мережеві джерела", "Доступно", "Перевірка…", "Увійти", "Дозволити локальну мережу", "Додати джерело", "Закрити вибір джерела", "Виберіть папку на цьому пристрої", "NAS або WebDAV", "Підключіться до мережевого музичного джерела", "Видалити мережеве джерело?", "Джерело буде видалено лише з бібліотеки. Файли в мережі не буде видалено.", "Видалити", "Мережеве джерело", "Зберегти мережеве джерело", "Назва", "Сервер", "HTTPS-сервер", "Спільний ресурс / шлях", "Шлях", "Ім’я користувача", "Домен / робоча група (необов’язково)", "Пароль", "З’єднання доступне", "Дозвольте доступ до локальної мережі в налаштуваннях Android", "Потрібна автентифікація", "Вузол недоступний", "Перевірте сервер і шлях", "Мережеве джерело недоступне", "Перевірка з’єднання…")
    AppLanguage.Urdu -> NetworkSourcesCopy("نیٹ ورک ذرائع", "دستیاب", "جانچ جاری ہے…", "سائن اِن", "مقامی نیٹ ورک کی اجازت دیں", "ذریعہ شامل کریں", "ذریعہ منتخب کرنے والا بند کریں", "اس ڈیوائس پر فولڈر منتخب کریں", "NAS یا WebDAV", "نیٹ ورک موسیقی کے ذریعے سے جڑیں", "نیٹ ورک ذریعہ ہٹائیں؟", "یہ ذریعہ صرف لائبریری سے ہٹائے گا۔ نیٹ ورک پر موجود فائلیں حذف نہیں ہوں گی۔", "ہٹائیں", "نیٹ ورک ذریعہ", "نیٹ ورک ذریعہ محفوظ کریں", "نام", "سرور", "HTTPS سرور", "شیئر / راستہ", "راستہ", "صارف نام", "ڈومین / ورک گروپ (اختیاری)", "پاس ورڈ", "کنکشن دستیاب ہے", "Android ترتیبات میں مقامی نیٹ ورک تک رسائی کی اجازت دیں", "تصدیق درکار ہے", "ہوسٹ تک رسائی ممکن نہیں", "سرور اور راستہ چیک کریں", "نیٹ ورک ذریعہ دستیاب نہیں", "کنکشن کی جانچ جاری ہے…")
    AppLanguage.English -> NetworkSourcesCopy("Network sources", "Available", "Testing…", "Sign in", "Allow local network", "Add source", "Close source picker", "Choose a folder on this device", "NAS or WebDAV", "Connect to a network music source", "Remove network source?", "This removes the source from the library. Files on the network are not deleted.", "Remove", "Network source", "Save network source", "Name", "Server", "HTTPS server", "Share / path", "Path", "Username", "Domain / workgroup (optional)", "Password", "Connection available", "Allow local network access in Android settings", "Authentication required", "Host is unreachable", "Check the server and path", "Network source is unavailable", "Testing connection…")
}

