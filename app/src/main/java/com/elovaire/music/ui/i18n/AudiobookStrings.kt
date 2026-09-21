package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

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
    val editTags: String = "Edit audiobook tags",
)

internal data class AudiobookDescriptionCopy(
    val about: String,
    val loading: String,
    val unavailable: String,
    val more: String,
    val close: String,
)

internal fun audiobookDescriptionCopy(language: AppLanguage): AudiobookDescriptionCopy = when (language) {
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
    AppLanguage.Latin -> AudiobookDescriptionCopy("De hoc libro", "Descriptio oneratur…", "Descriptio nulla", "PLURA", "Descriptionem claude")
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
        AppLanguage.German -> AudiobookUiCopy("Hörbücher", "Weiterhören", "Alle Hörbücher", "Autor", "Fortsetzen", "Wiedergeben", "Erneut wiedergeben", "Von vorn beginnen", "Kapitel", "Abgeschlossen", "Nicht begonnen", "Wird angehört", "Geschwindigkeit", "Teile", "15 Sekunden zurück", "15 Sekunden vor")
        AppLanguage.French -> AudiobookUiCopy("Livres audio", "Reprendre l’écoute", "Tous les livres audio", "Auteur", "Reprendre", "Lire", "Lire à nouveau", "Recommencer", "Chapitres", "Terminé", "Non commencé", "Écoute en cours", "Vitesse", "Parties", "Reculer de 15 secondes", "Avancer de 15 secondes")
        AppLanguage.Spanish -> AudiobookUiCopy("Audiolibros", "Continuar escuchando", "Todos los audiolibros", "Autor", "Continuar", "Reproducir", "Reproducir de nuevo", "Empezar de nuevo", "Capítulos", "Completado", "Sin empezar", "Escuchando", "Velocidad", "Partes", "Retroceder 15 segundos", "Avanzar 15 segundos")
        AppLanguage.Italian -> AudiobookUiCopy("Audiolibri", "Continua l’ascolto", "Tutti gli audiolibri", "Autore", "Riprendi", "Riproduci", "Riproduci di nuovo", "Ricomincia", "Capitoli", "Completato", "Non iniziato", "In ascolto", "Velocità", "Parti", "Indietro di 15 secondi", "Avanti di 15 secondi")
        AppLanguage.Portuguese -> AudiobookUiCopy("Audiolivros", "Continuar a ouvir", "Todos os audiolivros", "Autor", "Continuar", "Reproduzir", "Reproduzir novamente", "Recomeçar", "Capítulos", "Concluído", "Não iniciado", "A ouvir", "Velocidade", "Partes", "Recuar 15 segundos", "Avançar 15 segundos")
        AppLanguage.Russian -> AudiobookUiCopy("Аудиокниги", "Продолжить слушать", "Все аудиокниги", "Автор", "Продолжить", "Воспроизвести", "Воспроизвести снова", "С начала", "Главы", "Завершено", "Не начато", "Слушается", "Скорость", "Части", "Назад на 15 секунд", "Вперёд на 15 секунд")
        AppLanguage.Ukrainian -> AudiobookUiCopy("Аудіокниги", "Продовжити слухати", "Усі аудіокниги", "Автор", "Продовжити", "Відтворити", "Відтворити знову", "Почати спочатку", "Розділи", "Завершено", "Не розпочато", "Слухається", "Швидкість", "Частини", "Назад на 15 секунд", "Вперед на 15 секунд")
        AppLanguage.Czech -> AudiobookUiCopy("Audioknihy", "Pokračovat v poslechu", "Všechny audioknihy", "Autor", "Pokračovat", "Přehrát", "Přehrát znovu", "Začít znovu", "Kapitoly", "Dokončeno", "Nezahájeno", "Probíhá poslech", "Rychlost", "Části", "Zpět o 15 sekund", "Vpřed o 15 sekund")
        AppLanguage.Slovak -> AudiobookUiCopy("Audioknihy", "Pokračovať v počúvaní", "Všetky audioknihy", "Autor", "Pokračovať", "Prehrať", "Prehrať znova", "Začať od začiatku", "Kapitoly", "Dokončené", "Nezačaté", "Prebieha počúvanie", "Rýchlosť", "Časti", "Späť o 15 sekúnd", "Vpred o 15 sekúnd")
        AppLanguage.Dutch -> AudiobookUiCopy("Luisterboeken", "Verder luisteren", "Alle luisterboeken", "Auteur", "Hervatten", "Afspelen", "Opnieuw afspelen", "Opnieuw beginnen", "Hoofdstukken", "Voltooid", "Niet gestart", "Bezig met luisteren", "Snelheid", "Delen", "15 seconden terug", "15 seconden vooruit")
        AppLanguage.Swedish -> AudiobookUiCopy("Ljudböcker", "Fortsätt lyssna", "Alla ljudböcker", "Författare", "Fortsätt", "Spela", "Spela igen", "Börja om", "Kapitel", "Slutförd", "Inte påbörjad", "Lyssnar", "Hastighet", "Delar", "15 sekunder bakåt", "15 sekunder framåt")
        AppLanguage.Danish -> AudiobookUiCopy("Lydbøger", "Fortsæt med at lytte", "Alle lydbøger", "Forfatter", "Fortsæt", "Afspil", "Afspil igen", "Start forfra", "Kapitler", "Gennemført", "Ikke startet", "Lytter", "Hastighed", "Dele", "15 sekunder tilbage", "15 sekunder frem")
        AppLanguage.Norwegian -> AudiobookUiCopy("Lydbøker", "Fortsett å lytte", "Alle lydbøker", "Forfatter", "Fortsett", "Spill av", "Spill av på nytt", "Start på nytt", "Kapitler", "Fullført", "Ikke startet", "Lytter", "Hastighet", "Deler", "15 sekunder tilbake", "15 sekunder frem")
        else -> AudiobookUiCopy("Audiobooks", "Continue listening", "All audiobooks", "Author", "Resume", "Play", "Play again", "Start over", "Chapters", "Completed", "Not started", "Listening", "Speed", "Parts", "Rewind 15 seconds", "Forward 15 seconds")
    }
}

internal fun audiobookFinishedLabel(language: AppLanguage): String = when (language) {
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
    AppLanguage.Latin -> "Perfectum"
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
