package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class AudiobookSettingsCopy(
    val title: String,
    val subtitle: String,
    val rewindAmount: String,
    val forwardAmount: String,
    val seconds: String,
    val resumePlayback: String,
    val resumePlaybackSubtitle: String,
    val chapterMetadata: String,
    val chapterMetadataSubtitle: String,
)

internal fun audiobookSettingsCopy(language: AppLanguage): AudiobookSettingsCopy = when (language) {
    AppLanguage.Polish -> AudiobookSettingsCopy("Ustawienia audiobooków", "Dostosuj odtwarzanie audiobooków", "Cofanie", "Przewijanie do przodu", "s", "Wznawiaj odtwarzanie audiobooków", "Kontynuuj od ostatnio zapisanego miejsca", "Metadane rozdziałów", "Używaj znaczników rozdziałów z metadanych audiobooka, gdy są dostępne")
    AppLanguage.German -> AudiobookSettingsCopy("Hörbuch-Einstellungen", "Hörbuchwiedergabe anpassen", "Zurückspringen", "Vorspringen", "s", "Hörbuchwiedergabe fortsetzen", "Am zuletzt gespeicherten Wiedergabepunkt fortsetzen", "Kapitelmetadaten", "Verwende eingebettete Kapitelmarkierungen, wenn sie verfügbar sind")
    AppLanguage.French -> AudiobookSettingsCopy("Réglages des livres audio", "Personnaliser la lecture des livres audio", "Retour", "Avance", "s", "Reprendre la lecture des livres audio", "Reprendre depuis la dernière position enregistrée", "Métadonnées des chapitres", "Utiliser les marqueurs de chapitre intégrés lorsqu’ils sont disponibles")
    AppLanguage.Spanish -> AudiobookSettingsCopy("Ajustes de audiolibros", "Personaliza la reproducción de audiolibros", "Retroceder", "Avanzar", "s", "Reanudar la reproducción de audiolibros", "Continuar desde la última posición guardada", "Metadatos de capítulos", "Usar marcadores de capítulo integrados cuando estén disponibles")
    AppLanguage.Italian -> AudiobookSettingsCopy("Impostazioni audiolibri", "Personalizza la riproduzione degli audiolibri", "Indietro", "Avanti", "s", "Riprendi la riproduzione degli audiolibri", "Continua dall’ultima posizione salvata", "Metadati dei capitoli", "Usa i marcatori dei capitoli incorporati quando disponibili")
    AppLanguage.Portuguese -> AudiobookSettingsCopy("Definições de audiolivros", "Personalize a reprodução de audiolivros", "Retroceder", "Avançar", "s", "Retomar a reprodução de audiolivros", "Continuar a partir da última posição guardada", "Metadados dos capítulos", "Usar marcadores de capítulos incorporados quando disponíveis")
    AppLanguage.Russian -> AudiobookSettingsCopy("Настройки аудиокниг", "Настройте воспроизведение аудиокниг", "Назад", "Вперёд", "с", "Возобновлять воспроизведение аудиокниг", "Продолжать с последней сохранённой позиции", "Метаданные глав", "Использовать встроенные метки глав, если они доступны")
    AppLanguage.Ukrainian -> AudiobookSettingsCopy("Налаштування аудіокниг", "Налаштуйте відтворення аудіокниг", "Назад", "Вперед", "с", "Відновлювати відтворення аудіокниг", "Продовжувати з останньої збереженої позиції", "Метадані розділів", "Використовувати вбудовані позначки розділів, якщо вони доступні")
    AppLanguage.Czech -> AudiobookSettingsCopy("Nastavení audioknih", "Přizpůsobte přehrávání audioknih", "Zpět", "Vpřed", "s", "Obnovit přehrávání audioknih", "Pokračovat od poslední uložené pozice", "Metadata kapitol", "Používat vložené značky kapitol, pokud jsou k dispozici")
    AppLanguage.Slovak -> AudiobookSettingsCopy("Nastavenia audiokníh", "Prispôsobte prehrávanie audiokníh", "Späť", "Vpred", "s", "Obnoviť prehrávanie audiokníh", "Pokračovať od poslednej uloženej pozície", "Metadáta kapitol", "Používať vložené značky kapitol, ak sú dostupné")
    AppLanguage.Dutch -> AudiobookSettingsCopy("Instellingen voor luisterboeken", "Pas het afspelen van luisterboeken aan", "Terug", "Vooruit", "s", "Afspelen van luisterboeken hervatten", "Ga verder vanaf de laatst opgeslagen positie", "Hoofdstukmetagegevens", "Gebruik ingebouwde hoofdstukmarkeringen wanneer die beschikbaar zijn")
    AppLanguage.Swedish -> AudiobookSettingsCopy("Inställningar för ljudböcker", "Anpassa uppspelning av ljudböcker", "Bakåt", "Framåt", "s", "Återuppta uppspelning av ljudböcker", "Fortsätt från den senast sparade positionen", "Kapitelmetadata", "Använd inbäddade kapitelmarkeringar när de finns")
    AppLanguage.Danish -> AudiobookSettingsCopy("Indstillinger for lydbøger", "Tilpas afspilning af lydbøger", "Tilbage", "Frem", "s", "Genoptag afspilning af lydbøger", "Fortsæt fra den senest gemte position", "Kapitelmetadata", "Brug indlejrede kapitelmarkeringer, når de er tilgængelige")
    AppLanguage.Norwegian -> AudiobookSettingsCopy("Innstillinger for lydbøker", "Tilpass avspilling av lydbøker", "Tilbake", "Frem", "s", "Fortsett avspilling av lydbøker", "Fortsett fra sist lagrede posisjon", "Kapittelmetadata", "Bruk innebygde kapittelmarkører når de er tilgjengelige")
    AppLanguage.English -> AudiobookSettingsCopy("Audiobooks settings", "Customize audiobook playback", "Rewind amount", "Forward amount", "s", "Resume audiobook playback", "Continue from the last saved position", "Chapter metadata", "Use embedded chapter markers when available")
    else -> AudiobookSettingsCopy("Audiobooks settings", "Customize audiobook playback", "Rewind amount", "Forward amount", "s", "Resume audiobook playback", "Continue from the last saved position", "Chapter metadata", "Use embedded chapter markers when available")
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
