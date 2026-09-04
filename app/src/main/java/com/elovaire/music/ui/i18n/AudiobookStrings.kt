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
)

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
