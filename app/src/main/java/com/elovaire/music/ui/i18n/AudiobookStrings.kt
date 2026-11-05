package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun audiobookPartsCount(count: Int, language: AppLanguage): String {
    val lastTwo = count % 100
    val lastDigit = count % 10
    val slavicOne = lastDigit == 1 && lastTwo != 11
    val slavicFew = lastDigit in 2..4 && lastTwo !in 12..14
    val part = when (language) {
        AppLanguage.Albanian -> "pjesë"
        AppLanguage.Bengali -> return "${count}টি অংশ"
        AppLanguage.Bosnian, AppLanguage.Croatian -> when { slavicOne -> "dio"; slavicFew -> "dijela"; else -> "dijelova" }
        AppLanguage.Bulgarian -> if (count == 1) "част" else "части"
        AppLanguage.Catalan -> if (count == 1) "part" else "parts"
        AppLanguage.ChineseSimplified -> return "${count}个部分"
        AppLanguage.Czech -> when (count) { 1 -> "část"; in 2..4 -> "části"; else -> "částí" }
        AppLanguage.Danish -> if (count == 1) "del" else "dele"
        AppLanguage.Dutch -> if (count == 1) "deel" else "delen"
        AppLanguage.English -> if (count == 1) "part" else "parts"
        AppLanguage.Estonian -> "osa"
        AppLanguage.Finnish -> if (count == 1) "osa" else "osaa"
        AppLanguage.French -> if (count == 1) "partie" else "parties"
        AppLanguage.German -> if (count == 1) "Teil" else "Teile"
        AppLanguage.Greek -> if (count == 1) "μέρος" else "μέρη"
        AppLanguage.Hindi -> "भाग"
        AppLanguage.Hungarian -> "rész"
        AppLanguage.Indonesian -> "bagian"
        AppLanguage.Italian -> if (count == 1) "parte" else "parti"
        AppLanguage.Japanese -> return "${count}パート"
        AppLanguage.Korean -> return "부분 ${count}개"
        AppLanguage.Latvian -> when { lastTwo in 11..19 || lastDigit == 0 -> "daļu"; lastDigit == 1 -> "daļa"; else -> "daļas" }
        AppLanguage.Lithuanian -> when { lastTwo in 11..19 || lastDigit == 0 -> "dalių"; lastDigit == 1 -> "dalis"; else -> "dalys" }
        AppLanguage.Macedonian -> if (count == 1) "дел" else "делови"
        AppLanguage.Malay -> "bahagian"
        AppLanguage.Norwegian -> if (count == 1) "del" else "deler"
        AppLanguage.Polish -> if (count == 1) "część" else "części"
        AppLanguage.Portuguese -> if (count == 1) "parte" else "partes"
        AppLanguage.Punjabi -> "ਭਾਗ"
        AppLanguage.Romanian -> when { count == 1 -> "parte"; lastTwo == 0 || lastTwo >= 20 -> "de părți"; else -> "părți" }
        AppLanguage.Russian -> when { slavicOne -> "часть"; slavicFew -> "части"; else -> "частей" }
        AppLanguage.Serbian -> when { slavicOne -> "део"; slavicFew -> "дела"; else -> "делова" }
        AppLanguage.Slovak -> when (count) { 1 -> "časť"; in 2..4 -> "časti"; else -> "častí" }
        AppLanguage.Slovenian -> when (lastTwo) { 1 -> "del"; 2 -> "dela"; 3, 4 -> "deli"; else -> "delov" }
        AppLanguage.Spanish -> if (count == 1) "parte" else "partes"
        AppLanguage.Swedish -> if (count == 1) "del" else "delar"
        AppLanguage.Thai -> return "$count ส่วน"
        AppLanguage.Ukrainian -> when { slavicOne -> "частина"; slavicFew -> "частини"; else -> "частин" }
        AppLanguage.Urdu -> if (count == 1) "حصہ" else "حصے"
    }
    return "$count $part"
}

internal fun localizedAudiobookAuthor(author: String, language: AppLanguage): String {
    if (author.isNotBlank() && author != "Unknown Author" && author != "Unknown Artist") return author
    return when (language) {
        AppLanguage.Albanian -> "Autor i panjohur"
        AppLanguage.Bengali -> "অজানা লেখক"
        AppLanguage.Bosnian -> "Nepoznat autor"
        AppLanguage.Bulgarian -> "Неизвестен автор"
        AppLanguage.Catalan -> "Autor desconegut"
        AppLanguage.ChineseSimplified -> "未知作者"
        AppLanguage.Croatian -> "Nepoznat autor"
        AppLanguage.Czech -> "Neznámý autor"
        AppLanguage.Danish -> "Ukendt forfatter"
        AppLanguage.Dutch -> "Onbekende auteur"
        AppLanguage.English -> "Unknown Author"
        AppLanguage.Estonian -> "Tundmatu autor"
        AppLanguage.Finnish -> "Tuntematon kirjailija"
        AppLanguage.French -> "Auteur inconnu"
        AppLanguage.German -> "Unbekannter Autor"
        AppLanguage.Greek -> "Άγνωστος συγγραφέας"
        AppLanguage.Hindi -> "अज्ञात लेखक"
        AppLanguage.Hungarian -> "Ismeretlen szerző"
        AppLanguage.Indonesian -> "Penulis tidak dikenal"
        AppLanguage.Italian -> "Autore sconosciuto"
        AppLanguage.Japanese -> "不明な著者"
        AppLanguage.Korean -> "알 수 없는 저자"
        AppLanguage.Latvian -> "Nezināms autors"
        AppLanguage.Lithuanian -> "Nežinomas autorius"
        AppLanguage.Macedonian -> "Непознат автор"
        AppLanguage.Malay -> "Pengarang tidak diketahui"
        AppLanguage.Norwegian -> "Ukjent forfatter"
        AppLanguage.Polish -> "Nieznany autor"
        AppLanguage.Portuguese -> "Autor desconhecido"
        AppLanguage.Punjabi -> "ਅਣਜਾਣ ਲੇਖਕ"
        AppLanguage.Romanian -> "Autor necunoscut"
        AppLanguage.Russian -> "Неизвестный автор"
        AppLanguage.Serbian -> "Непознат аутор"
        AppLanguage.Slovak -> "Neznámy autor"
        AppLanguage.Slovenian -> "Neznan avtor"
        AppLanguage.Spanish -> "Autor desconocido"
        AppLanguage.Swedish -> "Okänd författare"
        AppLanguage.Thai -> "ไม่ทราบผู้แต่ง"
        AppLanguage.Ukrainian -> "Невідомий автор"
        AppLanguage.Urdu -> "نامعلوم مصنف"
    }
}

internal data class AudiobookUiCopy(
    val title: String,
    val continueListening: String,
    val allAudiobooks: String,
    val author: String,
    val resume: String,
    val play: String,
    val playAgain: String,
    val startOver: String,
    val chapters: String,
    val completed: String,
    val notStarted: String,
    val listening: String,
    val speed: String,
    val parts: String,
    val rewind15: String,
    val forward15: String,
    val editTags: String,
)

internal data class AudiobookDescriptionCopy(
    val about: String,
    val loading: String,
    val unavailable: String,
    val more: String,
    val close: String,
)

internal data class AudiobookUnavailableCopy(val title: String, val message: String, val retry: String)

internal fun audiobookUnavailableCopy(language: AppLanguage): AudiobookUnavailableCopy = when (language) {
    AppLanguage.Albanian -> AudiobookUnavailableCopy("Libri audio nuk është i disponueshëm", "Ky libër audio nuk është më i disponueshëm", "Rifresko bibliotekën dhe provo përsëri")
    AppLanguage.Bengali -> AudiobookUnavailableCopy("অডিওবুক উপলভ্য নয়", "এই অডিওবুক আর উপলভ্য নয়", "লাইব্রেরি রিফ্রেশ করে আবার চেষ্টা করুন")
    AppLanguage.Bosnian -> AudiobookUnavailableCopy("Audioknjiga nije dostupna", "Ova audioknjiga više nije dostupna", "Osvježite biblioteku i pokušajte ponovo")
    AppLanguage.Bulgarian -> AudiobookUnavailableCopy("Аудиокнигата е недостъпна", "Тази аудиокнига вече не е достъпна", "Обновете библиотеката и опитайте отново")
    AppLanguage.Catalan -> AudiobookUnavailableCopy("Audiollibre no disponible", "Aquest audiollibre ja no està disponible", "Actualitza la biblioteca i torna-ho a provar")
    AppLanguage.ChineseSimplified -> AudiobookUnavailableCopy("有声书不可用", "这本有声书已不可用", "刷新音乐库后重试")
    AppLanguage.Croatian -> AudiobookUnavailableCopy("Audioknjiga nije dostupna", "Ova audioknjiga više nije dostupna", "Osvježite biblioteku i pokušajte ponovno")
    AppLanguage.Czech -> AudiobookUnavailableCopy("Audiokniha není dostupná", "Tato audiokniha již není dostupná", "Obnovte knihovnu a zkuste to znovu")
    AppLanguage.Danish -> AudiobookUnavailableCopy("Lydbog er ikke tilgængelig", "Denne lydbog er ikke længere tilgængelig", "Opdater biblioteket, og prøv igen")
    AppLanguage.Dutch -> AudiobookUnavailableCopy("Luisterboek niet beschikbaar", "Dit luisterboek is niet meer beschikbaar", "Vernieuw je bibliotheek en probeer het opnieuw")
    AppLanguage.English -> AudiobookUnavailableCopy("Audiobook unavailable", "This audiobook is no longer available", "Refresh your library and try again")
    AppLanguage.Estonian -> AudiobookUnavailableCopy("Audioraamat pole saadaval", "See audioraamat pole enam saadaval", "Värskenda teeki ja proovi uuesti")
    AppLanguage.Finnish -> AudiobookUnavailableCopy("Äänikirja ei ole saatavilla", "Tämä äänikirja ei ole enää saatavilla", "Päivitä kirjasto ja yritä uudelleen")
    AppLanguage.French -> AudiobookUnavailableCopy("Livre audio indisponible", "Ce livre audio n’est plus disponible", "Actualisez votre bibliothèque et réessayez")
    AppLanguage.German -> AudiobookUnavailableCopy("Hörbuch nicht verfügbar", "Dieses Hörbuch ist nicht mehr verfügbar", "Aktualisiere deine Bibliothek und versuche es erneut")
    AppLanguage.Greek -> AudiobookUnavailableCopy("Το ηχητικό βιβλίο δεν είναι διαθέσιμο", "Αυτό το ηχητικό βιβλίο δεν είναι πλέον διαθέσιμο", "Ανανεώστε τη βιβλιοθήκη και δοκιμάστε ξανά")
    AppLanguage.Hindi -> AudiobookUnavailableCopy("ऑडियोबुक उपलब्ध नहीं है", "यह ऑडियोबुक अब उपलब्ध नहीं है", "लाइब्रेरी रीफ़्रेश करें और फिर कोशिश करें")
    AppLanguage.Hungarian -> AudiobookUnavailableCopy("A hangoskönyv nem érhető el", "Ez a hangoskönyv már nem érhető el", "Frissítsd a könyvtárat, és próbáld újra")
    AppLanguage.Indonesian -> AudiobookUnavailableCopy("Buku audio tidak tersedia", "Buku audio ini tidak lagi tersedia", "Segarkan pustaka dan coba lagi")
    AppLanguage.Italian -> AudiobookUnavailableCopy("Audiolibro non disponibile", "Questo audiolibro non è più disponibile", "Aggiorna la libreria e riprova")
    AppLanguage.Japanese -> AudiobookUnavailableCopy("オーディオブックを利用できません", "このオーディオブックは利用できなくなりました", "ライブラリを更新してもう一度お試しください")
    AppLanguage.Korean -> AudiobookUnavailableCopy("오디오북을 사용할 수 없음", "이 오디오북은 더 이상 사용할 수 없습니다", "라이브러리를 새로 고친 후 다시 시도하세요")
    AppLanguage.Latvian -> AudiobookUnavailableCopy("Audiogrāmata nav pieejama", "Šī audiogrāmata vairs nav pieejama", "Atsvaidzini bibliotēku un mēģini vēlreiz")
    AppLanguage.Lithuanian -> AudiobookUnavailableCopy("Garsinė knyga neprieinama", "Ši garsinė knyga nebeprieinama", "Atnaujinkite biblioteką ir bandykite dar kartą")
    AppLanguage.Macedonian -> AudiobookUnavailableCopy("Аудиокнигата не е достапна", "Оваа аудиокнига повеќе не е достапна", "Освежете ја библиотеката и обидете се повторно")
    AppLanguage.Malay -> AudiobookUnavailableCopy("Buku audio tidak tersedia", "Buku audio ini tidak lagi tersedia", "Segarkan pustaka anda dan cuba lagi")
    AppLanguage.Norwegian -> AudiobookUnavailableCopy("Lydboken er ikke tilgjengelig", "Denne lydboken er ikke lenger tilgjengelig", "Oppdater biblioteket og prøv igjen")
    AppLanguage.Polish -> AudiobookUnavailableCopy("Audiobook jest niedostępny", "Ten audiobook nie jest już dostępny", "Odśwież bibliotekę i spróbuj ponownie")
    AppLanguage.Portuguese -> AudiobookUnavailableCopy("Audiolivro indisponível", "Este audiolivro já não está disponível", "Atualize a biblioteca e tente novamente")
    AppLanguage.Punjabi -> AudiobookUnavailableCopy("ਆਡੀਓ ਕਿਤਾਬ ਉਪਲਬਧ ਨਹੀਂ", "ਇਹ ਆਡੀਓ ਕਿਤਾਬ ਹੁਣ ਉਪਲਬਧ ਨਹੀਂ ਹੈ", "ਲਾਇਬ੍ਰੇਰੀ ਤਾਜ਼ਾ ਕਰੋ ਅਤੇ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ")
    AppLanguage.Romanian -> AudiobookUnavailableCopy("Audiocarte indisponibilă", "Această audiocarte nu mai este disponibilă", "Actualizează biblioteca și încearcă din nou")
    AppLanguage.Russian -> AudiobookUnavailableCopy("Аудиокнига недоступна", "Эта аудиокнига больше недоступна", "Обновите библиотеку и попробуйте снова")
    AppLanguage.Serbian -> AudiobookUnavailableCopy("Аудио-књига није доступна", "Ова аудио-књига више није доступна", "Освежите библиотеку и покушајте поново")
    AppLanguage.Slovak -> AudiobookUnavailableCopy("Audiokniha nie je dostupná", "Táto audiokniha už nie je dostupná", "Obnovte knižnicu a skúste to znova")
    AppLanguage.Slovenian -> AudiobookUnavailableCopy("Zvočna knjiga ni na voljo", "Ta zvočna knjiga ni več na voljo", "Osvežite knjižnico in poskusite znova")
    AppLanguage.Spanish -> AudiobookUnavailableCopy("Audiolibro no disponible", "Este audiolibro ya no está disponible", "Actualiza la biblioteca y vuelve a intentarlo")
    AppLanguage.Swedish -> AudiobookUnavailableCopy("Ljudboken är inte tillgänglig", "Den här ljudboken är inte längre tillgänglig", "Uppdatera biblioteket och försök igen")
    AppLanguage.Thai -> AudiobookUnavailableCopy("หนังสือเสียงไม่พร้อมใช้งาน", "หนังสือเสียงเล่มนี้ไม่พร้อมใช้งานแล้ว", "รีเฟรชคลังแล้วลองอีกครั้ง")
    AppLanguage.Ukrainian -> AudiobookUnavailableCopy("Аудіокнига недоступна", "Ця аудіокнига більше недоступна", "Оновіть бібліотеку та спробуйте ще раз")
    AppLanguage.Urdu -> AudiobookUnavailableCopy("آڈیو بک دستیاب نہیں", "یہ آڈیو بک اب دستیاب نہیں ہے", "لائبریری تازہ کر کے دوبارہ کوشش کریں")
}

internal fun audiobookDescriptionCopy(language: AppLanguage): AudiobookDescriptionCopy = when (language) {
    AppLanguage.Bulgarian -> AudiobookDescriptionCopy("За тази книга", "Зареждане на описанието…", "Няма описание", "ОЩЕ", "Затвори описанието")
    AppLanguage.Catalan -> AudiobookDescriptionCopy("Sobre aquest llibre", "S’està carregant la descripció…", "No hi ha cap descripció", "MÉS", "Tanca la descripció")
    AppLanguage.Finnish -> AudiobookDescriptionCopy("Tietoa kirjasta", "Ladataan kuvausta…", "Kuvausta ei ole saatavilla", "LISÄÄ", "Sulje kuvaus")
    AppLanguage.Romanian -> AudiobookDescriptionCopy("Despre această carte", "Se încarcă descrierea…", "Nu există descriere", "MAI MULT", "Închide descrierea")
    AppLanguage.Slovenian -> AudiobookDescriptionCopy("O tej knjigi", "Nalaganje opisa…", "Opis ni na voljo", "VEČ", "Zapri opis")
    AppLanguage.Bosnian -> AudiobookDescriptionCopy("O ovoj knjizi", "Učitavanje opisa…", "Opis nije dostupan", "VIŠE", "Zatvori opis")
    AppLanguage.Indonesian -> AudiobookDescriptionCopy("Tentang buku ini", "Memuat deskripsi…", "Deskripsi tidak tersedia", "LAINNYA", "Tutup deskripsi")
    AppLanguage.Punjabi -> AudiobookDescriptionCopy("ਇਸ ਕਿਤਾਬ ਬਾਰੇ", "ਵੇਰਵਾ ਲੋਡ ਹੋ ਰਿਹਾ ਹੈ…", "ਕੋਈ ਵੇਰਵਾ ਉਪਲਬਧ ਨਹੀਂ", "ਹੋਰ", "ਵੇਰਵਾ ਬੰਦ ਕਰੋ")
    AppLanguage.Albanian -> AudiobookDescriptionCopy("Rreth këtij libri", "Po ngarkohet përshkrimi…", "Nuk ka përshkrim", "MË SHUMË", "Mbyll përshkrimin")
    AppLanguage.ChineseSimplified -> AudiobookDescriptionCopy("关于本书", "正在加载简介…", "没有简介", "更多", "关闭简介")
    AppLanguage.Croatian -> AudiobookDescriptionCopy("O ovoj knjizi", "Učitavanje opisa…", "Opis nije dostupan", "VIŠE", "Zatvori opis")
    AppLanguage.Czech -> AudiobookDescriptionCopy("O této knize", "Načítání popisu…", "Popis není k dispozici", "VÍCE", "Zavřít popis")
    AppLanguage.Danish -> AudiobookDescriptionCopy("Om denne bog", "Indlæser beskrivelse…", "Ingen beskrivelse", "MERE", "Luk beskrivelse")
    AppLanguage.Dutch -> AudiobookDescriptionCopy("Over dit boek", "Beschrijving laden…", "Geen beschrijving", "MEER", "Beschrijving sluiten")
    AppLanguage.Bengali -> AudiobookDescriptionCopy("এই বই সম্পর্কে", "বিবরণ লোড হচ্ছে…", "কোনও বিবরণ নেই", "আরও", "বিবরণ বন্ধ করুন")
    AppLanguage.Estonian -> AudiobookDescriptionCopy("Sellest raamatust", "Kirjelduse laadimine…", "Kirjeldus puudub", "ROHKEM", "Sulge kirjeldus")
    AppLanguage.French -> AudiobookDescriptionCopy("À propos de ce livre", "Chargement de la description…", "Aucune description", "PLUS", "Fermer la description")
    AppLanguage.German -> AudiobookDescriptionCopy("Über dieses Buch", "Beschreibung wird geladen…", "Keine Beschreibung", "MEHR", "Beschreibung schließen")
    AppLanguage.Greek -> AudiobookDescriptionCopy("Σχετικά με αυτό το βιβλίο", "Φόρτωση περιγραφής…", "Δεν υπάρχει περιγραφή", "ΠΕΡΙΣΣΟΤΕΡΑ", "Κλείσιμο περιγραφής")
    AppLanguage.Hindi -> AudiobookDescriptionCopy("इस पुस्तक के बारे में", "विवरण लोड हो रहा है…", "कोई विवरण उपलब्ध नहीं", "अधिक", "विवरण बंद करें")
    AppLanguage.Hungarian -> AudiobookDescriptionCopy("A könyvről", "Leírás betöltése…", "Nincs leírás", "TÖBB", "Leírás bezárása")
    AppLanguage.Italian -> AudiobookDescriptionCopy("Informazioni sul libro", "Caricamento descrizione…", "Nessuna descrizione", "ALTRO", "Chiudi descrizione")
    AppLanguage.Japanese -> AudiobookDescriptionCopy("この本について", "説明を読み込み中…", "説明はありません", "もっと見る", "説明を閉じる")
    AppLanguage.Korean -> AudiobookDescriptionCopy("이 책 정보", "설명 로드 중…", "설명이 없습니다", "더 보기", "설명 닫기")
    AppLanguage.Latvian -> AudiobookDescriptionCopy("Par šo grāmatu", "Notiek apraksta ielāde…", "Apraksts nav pieejams", "VAIRĀK", "Aizvērt aprakstu")
    AppLanguage.Lithuanian -> AudiobookDescriptionCopy("Apie šią knygą", "Įkeliamas aprašas…", "Aprašo nėra", "DAUGIAU", "Uždaryti aprašą")
    AppLanguage.Malay -> AudiobookDescriptionCopy("Tentang buku ini", "Memuatkan penerangan…", "Tiada penerangan", "LAGI", "Tutup penerangan")
    AppLanguage.Macedonian -> AudiobookDescriptionCopy("За оваа книга", "Описот се вчитува…", "Нема опис", "ПОВЕЌЕ", "Затвори го описот")
    AppLanguage.Norwegian -> AudiobookDescriptionCopy("Om denne boken", "Laster inn beskrivelse…", "Ingen beskrivelse", "MER", "Lukk beskrivelse")
    AppLanguage.Polish -> AudiobookDescriptionCopy("O tej książce", "Ładowanie opisu…", "Brak opisu", "WIĘCEJ", "Zamknij opis")
    AppLanguage.Portuguese -> AudiobookDescriptionCopy("Sobre este livro", "A carregar a descrição…", "Sem descrição", "MAIS", "Fechar descrição")
    AppLanguage.Russian -> AudiobookDescriptionCopy("Об этой книге", "Загрузка описания…", "Описание отсутствует", "ЕЩЁ", "Закрыть описание")
    AppLanguage.Serbian -> AudiobookDescriptionCopy("О овој књизи", "Учитавање описа…", "Нема описа", "ЈОШ", "Затвори опис")
    AppLanguage.Slovak -> AudiobookDescriptionCopy("O tejto knihe", "Načítava sa popis…", "Popis nie je k dispozícii", "VIAC", "Zavrieť popis")
    AppLanguage.Spanish -> AudiobookDescriptionCopy("Sobre este libro", "Cargando descripción…", "No hay descripción", "MÁS", "Cerrar descripción")
    AppLanguage.Swedish -> AudiobookDescriptionCopy("Om den här boken", "Läser in beskrivning…", "Ingen beskrivning", "MER", "Stäng beskrivning")
    AppLanguage.Thai -> AudiobookDescriptionCopy("เกี่ยวกับหนังสือเล่มนี้", "กำลังโหลดคำอธิบาย…", "ไม่มีคำอธิบาย", "เพิ่มเติม", "ปิดคำอธิบาย")
    AppLanguage.Ukrainian -> AudiobookDescriptionCopy("Про цю книгу", "Завантаження опису…", "Опис відсутній", "БІЛЬШЕ", "Закрити опис")
    AppLanguage.Urdu -> AudiobookDescriptionCopy("اس کتاب کے بارے میں", "تفصیل لوڈ ہو رہی ہے…", "کوئی تفصیل نہیں", "مزید", "تفصیل بند کریں")
    AppLanguage.English -> AudiobookDescriptionCopy("About this book", "Loading description…", "No description available", "MORE", "Close description")
}

internal fun audiobookCopy(language: AppLanguage): AudiobookUiCopy {
    return when (language) {
        AppLanguage.Estonian -> AudiobookUiCopy("Audioraamatud", "Jätka kuulamist", "Kõik audioraamatud", "Autor", "Jätka", "Esita", "Esita uuesti", "Alusta algusest", "Peatükid", "Lõpetatud", "Alustamata", "Kuulamisel", "Kiirus", "Osad", "15 sekundit tagasi", "15 sekundit edasi", "Muuda audioraamatu silte")
        AppLanguage.Greek -> AudiobookUiCopy("Ηχητικά βιβλία", "Συνέχεια ακρόασης", "Όλα τα ηχητικά βιβλία", "Συγγραφέας", "Συνέχεια", "Αναπαραγωγή", "Αναπαραγωγή ξανά", "Από την αρχή", "Κεφάλαια", "Ολοκληρώθηκε", "Δεν έχει ξεκινήσει", "Ακρόαση", "Ταχύτητα", "Μέρη", "15 δευτερόλεπτα πίσω", "15 δευτερόλεπτα μπροστά", "Επεξεργασία ετικετών ηχητικού βιβλίου")
        AppLanguage.Hungarian -> AudiobookUiCopy("Hangoskönyvek", "Hallgatás folytatása", "Összes hangoskönyv", "Szerző", "Folytatás", "Lejátszás", "Újra lejátszás", "Kezdés elölről", "Fejezetek", "Befejezve", "Nem kezdődött el", "Hallgatás alatt", "Sebesség", "Részek", "15 másodperc vissza", "15 másodperc előre", "Hangoskönyv címkéinek szerkesztése")
        AppLanguage.Latvian -> AudiobookUiCopy("Audiogrāmatas", "Turpināt klausīties", "Visas audiogrāmatas", "Autors", "Turpināt", "Atskaņot", "Atskaņot vēlreiz", "Sākt no jauna", "Nodaļas", "Pabeigts", "Nav sākts", "Klausās", "Ātrums", "Daļas", "Atpakaļ 15 sekundes", "Uz priekšu 15 sekundes", "Rediģēt audiogrāmatas tagus")
        AppLanguage.Lithuanian -> AudiobookUiCopy("Garsinės knygos", "Tęsti klausymą", "Visos garsinės knygos", "Autorius", "Tęsti", "Leisti", "Leisti dar kartą", "Pradėti iš naujo", "Skyriai", "Baigta", "Nepradėta", "Klausoma", "Greitis", "Dalys", "Atgal 15 sekundžių", "Pirmyn 15 sekundžių", "Redaguoti garsinės knygos žymas")
        AppLanguage.Malay -> AudiobookUiCopy("Buku audio", "Teruskan mendengar", "Semua buku audio", "Pengarang", "Sambung", "Mainkan", "Mainkan semula", "Mula semula", "Bab", "Selesai", "Belum bermula", "Sedang mendengar", "Kelajuan", "Bahagian", "Undur 15 saat", "Maju 15 saat", "Edit tag buku audio")
        AppLanguage.Macedonian -> AudiobookUiCopy("Аудиокниги", "Продолжи со слушање", "Сите аудиокниги", "Автор", "Продолжи", "Пушти", "Пушти повторно", "Почни одново", "Поглавја", "Завршено", "Не е започнато", "Се слуша", "Брзина", "Делови", "Назад 15 секунди", "Напред 15 секунди", "Уреди ги ознаките на аудиокнигата")
        AppLanguage.Thai -> AudiobookUiCopy("หนังสือเสียง", "ฟังต่อ", "หนังสือเสียงทั้งหมด", "ผู้เขียน", "ฟังต่อ", "เล่น", "เล่นอีกครั้ง", "เริ่มใหม่", "บท", "ฟังจบแล้ว", "ยังไม่ได้เริ่ม", "กำลังฟัง", "ความเร็ว", "ส่วน", "ย้อนกลับ 15 วินาที", "เดินหน้า 15 วินาที", "แก้ไขแท็กหนังสือเสียง")
        AppLanguage.Croatian -> AudiobookUiCopy("Audioknjige", "Nastavi slušati", "Sve audioknjige", "Autor", "Nastavi", "Reproduciraj", "Reproduciraj ponovno", "Počni ispočetka", "Poglavlja", "Dovršeno", "Nije započeto", "Slušanje", "Brzina", "Dijelovi", "Natrag 15 sekundi", "Naprijed 15 sekundi", "Uredi oznake audioknjige")
        AppLanguage.Bengali -> AudiobookUiCopy("অডিওবুক", "শোনা চালিয়ে যান", "সব অডিওবুক", "লেখক", "চালিয়ে যান", "চালান", "আবার চালান", "শুরু থেকে চালান", "অধ্যায়", "সম্পন্ন", "শুরু হয়নি", "শোনা হচ্ছে", "গতি", "অংশ", "১৫ সেকেন্ড পিছিয়ে যান", "১৫ সেকেন্ড এগিয়ে যান", "অডিওবুকের ট্যাগ সম্পাদনা করুন")
        AppLanguage.ChineseSimplified -> AudiobookUiCopy("有声书", "继续收听", "所有有声书", "作者", "继续", "播放", "再次播放", "从头开始", "章节", "已完成", "尚未开始", "正在收听", "速度", "部分", "后退15秒", "前进15秒", "编辑有声书标签")
        AppLanguage.Hindi -> AudiobookUiCopy("ऑडियोबुक", "सुनना जारी रखें", "सभी ऑडियोबुक", "लेखक", "जारी रखें", "चलाएँ", "फिर चलाएँ", "शुरू से चलाएँ", "अध्याय", "पूरा हुआ", "शुरू नहीं हुआ", "सुन रहे हैं", "गति", "भाग", "15 सेकंड पीछे जाएँ", "15 सेकंड आगे जाएँ", "ऑडियोबुक टैग संपादित करें")
        AppLanguage.Japanese -> AudiobookUiCopy("オーディオブック", "続きを聴く", "すべてのオーディオブック", "著者", "続ける", "再生", "もう一度再生", "最初から再生", "チャプター", "完了", "未開始", "再生中", "速度", "パート", "15秒戻る", "15秒進む", "オーディオブックのタグを編集")
        AppLanguage.Korean -> AudiobookUiCopy("오디오북", "이어 듣기", "모든 오디오북", "저자", "계속", "재생", "다시 재생", "처음부터 재생", "챕터", "완료", "시작하지 않음", "듣는 중", "속도", "부분", "15초 뒤로", "15초 앞으로", "오디오북 태그 편집")
        AppLanguage.Bulgarian -> AudiobookUiCopy("Аудиокниги", "Продължи слушането", "Всички аудиокниги", "Автор", "Продължи", "Пусни", "Пусни отново", "Започни отначало", "Глави", "Завършено", "Не е започната", "Слушане", "Скорост", "Части", "Назад с 15 секунди", "Напред с 15 секунди", "Редактирай етикетите на аудиокнигата")
        AppLanguage.Catalan -> AudiobookUiCopy("Audiollibres", "Continua escoltant", "Tots els audiollibres", "Autor", "Continua", "Reprodueix", "Torna a reproduir", "Comença de nou", "Capítols", "Completat", "Sense començar", "Escoltant", "Velocitat", "Parts", "Recula 15 segons", "Avança 15 segons", "Edita les etiquetes de l’audiollibre")
        AppLanguage.Finnish -> AudiobookUiCopy("Äänikirjat", "Jatka kuuntelua", "Kaikki äänikirjat", "Kirjailija", "Jatka", "Toista", "Toista uudelleen", "Aloita alusta", "Luvut", "Valmis", "Ei aloitettu", "Kuunnellaan", "Nopeus", "Osat", "Kelaa 15 sekuntia taaksepäin", "Kelaa 15 sekuntia eteenpäin", "Muokkaa äänikirjan tunnisteita")
        AppLanguage.Romanian -> AudiobookUiCopy("Audiocărți", "Continuă ascultarea", "Toate audiocărțile", "Autor", "Continuă", "Redă", "Redă din nou", "Începe de la capăt", "Capitole", "Finalizat", "Neînceput", "În ascultare", "Viteză", "Părți", "Înapoi 15 secunde", "Înainte 15 secunde", "Editează etichetele audiocărții")
        AppLanguage.Slovenian -> AudiobookUiCopy("Zvočne knjige", "Nadaljuj poslušanje", "Vse zvočne knjige", "Avtor", "Nadaljuj", "Predvajaj", "Predvajaj znova", "Začni znova", "Poglavja", "Končano", "Ni začeto", "Poslušanje", "Hitrost", "Deli", "Nazaj 15 sekund", "Naprej 15 sekund", "Uredi oznake zvočne knjige")
        AppLanguage.Bosnian -> AudiobookUiCopy("Audioknjige", "Nastavi slušati", "Sve audioknjige", "Autor", "Nastavi", "Pusti", "Pusti ponovo", "Počni ispočetka", "Poglavlja", "Završeno", "Nije započeto", "Slušanje", "Brzina", "Dijelovi", "Nazad 15 sekundi", "Naprijed 15 sekundi", "Uredi oznake audioknjige")
        AppLanguage.Indonesian -> AudiobookUiCopy("Buku audio", "Lanjutkan mendengarkan", "Semua buku audio", "Penulis", "Lanjutkan", "Putar", "Putar lagi", "Mulai ulang", "Bab", "Selesai", "Belum dimulai", "Mendengarkan", "Kecepatan", "Bagian", "Mundur 15 detik", "Maju 15 detik", "Edit tag buku audio")
        AppLanguage.Punjabi -> AudiobookUiCopy("ਆਡੀਓ ਕਿਤਾਬਾਂ", "ਸੁਣਨਾ ਜਾਰੀ ਰੱਖੋ", "ਸਾਰੀਆਂ ਆਡੀਓ ਕਿਤਾਬਾਂ", "ਲੇਖਕ", "ਜਾਰੀ ਰੱਖੋ", "ਚਲਾਓ", "ਦੁਬਾਰਾ ਚਲਾਓ", "ਸ਼ੁਰੂ ਤੋਂ ਚਲਾਓ", "ਅਧਿਆਇ", "ਮੁਕੰਮਲ", "ਸ਼ੁਰੂ ਨਹੀਂ ਕੀਤੀ", "ਸੁਣ ਰਹੇ ਹੋ", "ਰਫ਼ਤਾਰ", "ਭਾਗ", "15 ਸਕਿੰਟ ਪਿੱਛੇ", "15 ਸਕਿੰਟ ਅੱਗੇ", "ਆਡੀਓ ਕਿਤਾਬ ਦੇ ਟੈਗ ਸੋਧੋ")
        AppLanguage.Albanian -> AudiobookUiCopy("Libra audio", "Vazhdo dëgjimin", "Të gjithë librat audio", "Autori", "Vazhdo", "Luaj", "Luaj përsëri", "Fillo nga e para", "Kapitujt", "Përfunduar", "Nuk ka filluar", "Duke dëgjuar", "Shpejtësia", "Pjesë", "Kthehu 15 sekonda", "Kalo 15 sekonda përpara", "Redakto etiketat e librit audio")
        AppLanguage.Serbian -> AudiobookUiCopy("Аудио-књиге", "Настави слушање", "Све аудио-књиге", "Аутор", "Настави", "Пусти", "Пусти поново", "Почни испочетка", "Поглавља", "Завршено", "Није започето", "Слушање", "Брзина", "Делови", "Назад 15 секунди", "Напред 15 секунди", "Уреди ознаке аудио-књиге")
        AppLanguage.Urdu -> AudiobookUiCopy("آڈیو بکس", "سننا جاری رکھیں", "تمام آڈیو بکس", "مصنف", "جاری رکھیں", "چلائیں", "دوبارہ چلائیں", "شروع سے چلائیں", "ابواب", "مکمل", "شروع نہیں کی", "سن رہے ہیں", "رفتار", "حصے", "15 سیکنڈ پیچھے", "15 سیکنڈ آگے", "آڈیو بک ٹیگز میں ترمیم کریں")
        AppLanguage.Polish -> AudiobookUiCopy(
            title = "Audiobooki",
            continueListening = "Kontynuuj słuchanie",
            allAudiobooks = "Wszystkie audiobooki",
            author = "Autor",
            resume = "Kontynuuj",
            play = "Odtwórz",
            playAgain = "Odtwórz ponownie",
            startOver = "Zacznij od początku",
            chapters = "Rozdziały",
            completed = "Ukończono",
            notStarted = "Nie rozpoczęto",
            listening = "Słuchanie",
            speed = "Prędkość",
            parts = "części",
            rewind15 = "Cofnij o 15 sekund",
            forward15 = "Przewiń o 15 sekund",
            editTags = "Edytuj tagi audiobooka",
        )
        AppLanguage.German -> AudiobookUiCopy("Hörbücher", "Weiterhören", "Alle Hörbücher", "Autor", "Fortsetzen", "Wiedergeben", "Erneut wiedergeben", "Von vorn beginnen", "Kapitel", "Abgeschlossen", "Nicht begonnen", "Wird angehört", "Geschwindigkeit", "Teile", "15 Sekunden zurück", "15 Sekunden vor", "Hörbuch-Tags bearbeiten")
        AppLanguage.French -> AudiobookUiCopy("Livres audio", "Reprendre l’écoute", "Tous les livres audio", "Auteur", "Reprendre", "Lire", "Lire à nouveau", "Recommencer", "Chapitres", "Terminé", "Non commencé", "Écoute en cours", "Vitesse", "Parties", "Reculer de 15 secondes", "Avancer de 15 secondes", "Modifier les tags du livre audio")
        AppLanguage.Spanish -> AudiobookUiCopy("Audiolibros", "Continuar escuchando", "Todos los audiolibros", "Autor", "Continuar", "Reproducir", "Reproducir de nuevo", "Empezar de nuevo", "Capítulos", "Completado", "Sin empezar", "Escuchando", "Velocidad", "Partes", "Retroceder 15 segundos", "Avanzar 15 segundos", "Editar etiquetas del audiolibro")
        AppLanguage.Italian -> AudiobookUiCopy("Audiolibri", "Continua l’ascolto", "Tutti gli audiolibri", "Autore", "Riprendi", "Riproduci", "Riproduci di nuovo", "Ricomincia", "Capitoli", "Completato", "Non iniziato", "In ascolto", "Velocità", "Parti", "Indietro di 15 secondi", "Avanti di 15 secondi", "Modifica i tag dell’audiolibro")
        AppLanguage.Portuguese -> AudiobookUiCopy("Audiolivros", "Continuar a ouvir", "Todos os audiolivros", "Autor", "Continuar", "Reproduzir", "Reproduzir novamente", "Recomeçar", "Capítulos", "Concluído", "Não iniciado", "A ouvir", "Velocidade", "Partes", "Recuar 15 segundos", "Avançar 15 segundos", "Editar etiquetas do audiolivro")
        AppLanguage.Russian -> AudiobookUiCopy("Аудиокниги", "Продолжить слушать", "Все аудиокниги", "Автор", "Продолжить", "Воспроизвести", "Воспроизвести снова", "С начала", "Главы", "Завершено", "Не начато", "Слушается", "Скорость", "Части", "Назад на 15 секунд", "Вперёд на 15 секунд", "Редактировать теги аудиокниги")
        AppLanguage.Ukrainian -> AudiobookUiCopy("Аудіокниги", "Продовжити слухати", "Усі аудіокниги", "Автор", "Продовжити", "Відтворити", "Відтворити знову", "Почати спочатку", "Розділи", "Завершено", "Не розпочато", "Слухається", "Швидкість", "Частини", "Назад на 15 секунд", "Вперед на 15 секунд", "Редагувати теги аудіокниги")
        AppLanguage.Czech -> AudiobookUiCopy("Audioknihy", "Pokračovat v poslechu", "Všechny audioknihy", "Autor", "Pokračovat", "Přehrát", "Přehrát znovu", "Začít znovu", "Kapitoly", "Dokončeno", "Nezahájeno", "Probíhá poslech", "Rychlost", "Části", "Zpět o 15 sekund", "Vpřed o 15 sekund", "Upravit značky audioknihy")
        AppLanguage.Slovak -> AudiobookUiCopy("Audioknihy", "Pokračovať v počúvaní", "Všetky audioknihy", "Autor", "Pokračovať", "Prehrať", "Prehrať znova", "Začať od začiatku", "Kapitoly", "Dokončené", "Nezačaté", "Prebieha počúvanie", "Rýchlosť", "Časti", "Späť o 15 sekúnd", "Vpred o 15 sekúnd", "Upraviť značky audioknihy")
        AppLanguage.Dutch -> AudiobookUiCopy("Luisterboeken", "Verder luisteren", "Alle luisterboeken", "Auteur", "Hervatten", "Afspelen", "Opnieuw afspelen", "Opnieuw beginnen", "Hoofdstukken", "Voltooid", "Niet gestart", "Bezig met luisteren", "Snelheid", "Delen", "15 seconden terug", "15 seconden vooruit", "Tags van luisterboek bewerken")
        AppLanguage.Swedish -> AudiobookUiCopy("Ljudböcker", "Fortsätt lyssna", "Alla ljudböcker", "Författare", "Fortsätt", "Spela", "Spela igen", "Börja om", "Kapitel", "Slutförd", "Inte påbörjad", "Lyssnar", "Hastighet", "Delar", "15 sekunder bakåt", "15 sekunder framåt", "Redigera ljudbokens taggar")
        AppLanguage.Danish -> AudiobookUiCopy("Lydbøger", "Fortsæt med at lytte", "Alle lydbøger", "Forfatter", "Fortsæt", "Afspil", "Afspil igen", "Start forfra", "Kapitler", "Gennemført", "Ikke startet", "Lytter", "Hastighed", "Dele", "15 sekunder tilbage", "15 sekunder frem", "Rediger lydbogens tags")
        AppLanguage.Norwegian -> AudiobookUiCopy("Lydbøker", "Fortsett å lytte", "Alle lydbøker", "Forfatter", "Fortsett", "Spill av", "Spill av på nytt", "Start på nytt", "Kapitler", "Fullført", "Ikke startet", "Lytter", "Hastighet", "Deler", "15 sekunder tilbake", "15 sekunder frem", "Rediger lydbokens tagger")
        AppLanguage.English -> AudiobookUiCopy("Audiobooks", "Continue listening", "All audiobooks", "Author", "Resume", "Play", "Play again", "Start over", "Chapters", "Completed", "Not started", "Listening", "Speed", "Parts", "Rewind 15 seconds", "Forward 15 seconds", "Edit audiobook tags")
    }
}

internal fun audiobookFinishedLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Bulgarian -> "Завършено"
    AppLanguage.Catalan -> "Acabat"
    AppLanguage.Finnish -> "Valmis"
    AppLanguage.Romanian -> "Finalizat"
    AppLanguage.Slovenian -> "Končano"
    AppLanguage.Bosnian -> "Završeno"
    AppLanguage.Indonesian -> "Selesai"
    AppLanguage.Punjabi -> "ਮੁਕੰਮਲ"
    AppLanguage.Albanian -> "Përfunduar"
    AppLanguage.Bengali -> "সম্পন্ন"
    AppLanguage.ChineseSimplified -> "已完成"
    AppLanguage.Croatian -> "Dovršeno"
    AppLanguage.Czech -> "Dokončeno"
    AppLanguage.Danish -> "Færdig"
    AppLanguage.Dutch -> "Voltooid"
    AppLanguage.English -> "Finished"
    AppLanguage.Estonian -> "Lõpetatud"
    AppLanguage.French -> "Terminé"
    AppLanguage.German -> "Fertig"
    AppLanguage.Greek -> "Ολοκληρώθηκε"
    AppLanguage.Hindi -> "पूरा हुआ"
    AppLanguage.Hungarian -> "Befejezve"
    AppLanguage.Italian -> "Terminato"
    AppLanguage.Japanese -> "完了"
    AppLanguage.Korean -> "완료됨"
    AppLanguage.Latvian -> "Pabeigts"
    AppLanguage.Lithuanian -> "Baigta"
    AppLanguage.Macedonian -> "Завршено"
    AppLanguage.Malay -> "Selesai"
    AppLanguage.Norwegian -> "Fullført"
    AppLanguage.Polish -> "Ukończono"
    AppLanguage.Portuguese -> "Concluído"
    AppLanguage.Russian -> "Завершено"
    AppLanguage.Serbian -> "Завршено"
    AppLanguage.Slovak -> "Dokončené"
    AppLanguage.Spanish -> "Terminado"
    AppLanguage.Swedish -> "Slutfört"
    AppLanguage.Thai -> "เสร็จสิ้น"
    AppLanguage.Ukrainian -> "Завершено"
    AppLanguage.Urdu -> "مکمل"
}
