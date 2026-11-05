package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType

internal data class AudiobookExtraCopy(val speed: String, val speedSubtitle: String, val rewind: String, val rewindSubtitle: String)

internal fun audiobookPlaybackCategory(language: AppLanguage): String = when (language) {
    AppLanguage.Estonian -> "Esitamine"
    AppLanguage.Greek -> "Αναπαραγωγή"
    AppLanguage.Hungarian -> "Lejátszás"
    AppLanguage.Latvian -> "Atskaņošana"
    AppLanguage.Lithuanian -> "Atkūrimas"
    AppLanguage.Malay -> "Main semula"
    AppLanguage.Macedonian -> "Репродукција"
    AppLanguage.ChineseSimplified -> "播放"
    AppLanguage.Hindi -> "प्लेबैक"
    AppLanguage.Japanese -> "再生"
    AppLanguage.Korean -> "재생"
    AppLanguage.Thai -> "การเล่น"
    AppLanguage.Albanian -> "Riprodhimi"
    AppLanguage.Bengali -> "প্লেব্যাক"
    AppLanguage.Croatian -> "Reprodukcija"
    AppLanguage.Serbian -> "Репродукција"
    AppLanguage.Urdu -> "پلے بیک"
    AppLanguage.Bulgarian -> "Възпроизвеждане"
    AppLanguage.Catalan -> "Reproducció"
    AppLanguage.Finnish -> "Toisto"
    AppLanguage.Romanian -> "Redare"
    AppLanguage.Slovenian -> "Predvajanje"
    AppLanguage.Bosnian -> "Reprodukcija"
    AppLanguage.Indonesian -> "Pemutaran"
    AppLanguage.Punjabi -> "ਪਲੇਬੈਕ"
    AppLanguage.Polish -> "Odtwarzanie"
    AppLanguage.German -> "Wiedergabe"
    AppLanguage.French -> "Lecture"
    AppLanguage.Spanish -> "Reproducción"
    AppLanguage.Italian -> "Riproduzione"
    AppLanguage.Portuguese -> "Reprodução"
    AppLanguage.Russian -> "Воспроизведение"
    AppLanguage.Ukrainian -> "Відтворення"
    AppLanguage.Czech -> "Přehrávání"
    AppLanguage.Slovak -> "Prehrávanie"
    AppLanguage.Dutch -> "Afspelen"
    AppLanguage.Swedish -> "Uppspelning"
    AppLanguage.Danish -> "Afspilning"
    AppLanguage.Norwegian -> "Avspilling"
    AppLanguage.English -> "Playback"
}

internal fun audiobookExtraCopy(language: AppLanguage): AudiobookExtraCopy = when (language) {
    AppLanguage.Estonian -> AudiobookExtraCopy("Esituskiirus", "Muuda audioraamatu ettelugemise kiirust", "Keri jätkamisel tagasi", "Raamatu jätkamisel mine valitud aja võrra tagasi")
    AppLanguage.Greek -> AudiobookExtraCopy("Ταχύτητα αναπαραγωγής", "Προσαρμόστε την ταχύτητα αφήγησης", "Πίσω κατά τη συνέχιση", "Κατά τη συνέχιση του βιβλίου επιστρέψτε κατά τον επιλεγμένο χρόνο")
    AppLanguage.Hungarian -> AudiobookExtraCopy("Lejátszási sebesség", "Állítsd be a narráció sebességét", "Visszatekerés folytatáskor", "A könyv folytatásakor lépj vissza a beállított idővel")
    AppLanguage.Latvian -> AudiobookExtraCopy("Atskaņošanas ātrums", "Pielāgo audiogrāmatas lasījuma ātrumu", "Attīt, atsākot klausīšanos", "Atsākot grāmatu, atgriezies par izvēlēto laiku")
    AppLanguage.Lithuanian -> AudiobookExtraCopy("Atkūrimo greitis", "Koreguok garsinės knygos skaitymo greitį", "Atsuk tęsiant", "Tęsdamas knygą grįžk pasirinktu laiku atgal")
    AppLanguage.Malay -> AudiobookExtraCopy("Kelajuan main balik", "Laraskan kelajuan narasi buku audio", "Undur apabila menyambung", "Undur mengikut tempoh yang ditetapkan apabila menyambung buku")
    AppLanguage.Macedonian -> AudiobookExtraCopy("Брзина на репродукција", "Приспособете ја брзината на читањето", "Врати назад при продолжување", "При продолжување на книгата вратете се за избраното време")
    AppLanguage.ChineseSimplified -> AudiobookExtraCopy("播放速度", "调整有声书的朗读速度", "继续播放时后退", "继续收听时按设定时长后退")
    AppLanguage.Hindi -> AudiobookExtraCopy("प्लेबैक गति", "ऑडियोबुक के कथन की गति बदलें", "दोबारा शुरू करते समय पीछे जाएँ", "पुस्तक जारी करते समय तय अवधि तक पीछे जाएँ")
    AppLanguage.Japanese -> AudiobookExtraCopy("再生速度", "オーディオブックの朗読速度を調整", "再開時に巻き戻す", "続きを再生するときに設定した時間だけ戻る")
    AppLanguage.Korean -> AudiobookExtraCopy("재생 속도", "오디오북 낭독 속도 조절", "이어 들을 때 되감기", "책을 이어 들을 때 설정한 시간만큼 되돌아갑니다")
    AppLanguage.Thai -> AudiobookExtraCopy("ความเร็วในการเล่น", "ปรับความเร็วการบรรยายหนังสือเสียง", "ย้อนกลับเมื่อเล่นต่อ", "ย้อนกลับตามเวลาที่ตั้งไว้เมื่อฟังหนังสือต่อ")
    AppLanguage.Albanian -> AudiobookExtraCopy("Shpejtësia e riprodhimit", "Rregullo shpejtësinë e rrëfimit", "Kthehu pas kur rifillon", "Kur rifillon librin, kthehu pas sipas kohës së zgjedhur")
    AppLanguage.Bengali -> AudiobookExtraCopy("প্লেব্যাকের গতি", "অডিওবুকের বর্ণনার গতি বদলান", "আবার শুরু করার সময় পিছিয়ে যান", "বই চালু করলে নির্ধারিত সময় পিছিয়ে যান")
    AppLanguage.Croatian -> AudiobookExtraCopy("Brzina reprodukcije", "Prilagodite brzinu pripovijedanja", "Premotaj pri nastavku", "Pri nastavku knjige vratite se za odabrano vrijeme")
    AppLanguage.Serbian -> AudiobookExtraCopy("Брзина репродукције", "Подесите брзину приповедања", "Премотај при наставку", "При наставку књиге вратите се за изабрано време")
    AppLanguage.Urdu -> AudiobookExtraCopy("پلے بیک کی رفتار", "آڈیو بک کی آواز کی رفتار بدلیں", "دوبارہ شروع کرتے وقت پیچھے جائیں", "کتاب جاری کرتے وقت مقررہ دورانیے تک پیچھے جائیں")
    AppLanguage.Bulgarian -> AudiobookExtraCopy("Скорост на възпроизвеждане", "Променете скоростта на аудиокнигата", "Връщане при продължаване", "Върнете се с избраното време при продължаване")
    AppLanguage.Catalan -> AudiobookExtraCopy("Velocitat de reproducció", "Ajusta la velocitat de narració", "Recula en reprendre", "En reprendre el llibre, recula el temps seleccionat")
    AppLanguage.Finnish -> AudiobookExtraCopy("Toistonopeus", "Säädä äänikirjan kerrontanopeutta", "Kelaa taaksepäin jatkettaessa", "Palaa valitun ajan verran taaksepäin kirjaa jatkettaessa")
    AppLanguage.Romanian -> AudiobookExtraCopy("Viteza de redare", "Ajustează viteza narațiunii", "Derulează înapoi la reluare", "Revino cu durata aleasă când reiei cartea")
    AppLanguage.Slovenian -> AudiobookExtraCopy("Hitrost predvajanja", "Prilagodite hitrost pripovedovanja", "Previj ob nadaljevanju", "Ob nadaljevanju knjige se vrnite za izbrani čas")
    AppLanguage.Bosnian -> AudiobookExtraCopy("Brzina reprodukcije", "Podesite brzinu čitanja audioknjige", "Vrati unazad pri nastavku", "Pri nastavku knjige vratite se za odabrano vrijeme")
    AppLanguage.Indonesian -> AudiobookExtraCopy("Kecepatan pemutaran", "Atur kecepatan narasi buku audio", "Mundur saat melanjutkan", "Mundur sesuai durasi yang ditentukan saat melanjutkan buku")
    AppLanguage.Punjabi -> AudiobookExtraCopy("ਚਲਾਉਣ ਦੀ ਰਫ਼ਤਾਰ", "ਆਡੀਓ ਕਿਤਾਬ ਦੇ ਬਿਰਤਾਂਤ ਦੀ ਰਫ਼ਤਾਰ ਬਦਲੋ", "ਦੁਬਾਰਾ ਚਲਾਉਣ ਵੇਲੇ ਪਿੱਛੇ ਜਾਓ", "ਕਿਤਾਬ ਜਾਰੀ ਕਰਨ ਵੇਲੇ ਤੈਅ ਸਮੇਂ ਲਈ ਪਿੱਛੇ ਜਾਓ")
    AppLanguage.Polish -> AudiobookExtraCopy("Prędkość odtwarzania", "Dostosuj tempo narracji audiobooka", "Cofaj przy wznawianiu", "Przy wznawianiu cofnij o ustawiony czas")
    AppLanguage.German -> AudiobookExtraCopy("Wiedergabegeschwindigkeit", "Das Erzähltempo anpassen", "Beim Fortsetzen zurückspringen", "Beim Fortsetzen um die eingestellte Zeit zurückspringen")
    AppLanguage.French -> AudiobookExtraCopy("Vitesse de lecture", "Ajuster le rythme de narration", "Reculer à la reprise", "Reculer de la durée réglée lors de la reprise")
    AppLanguage.Spanish -> AudiobookExtraCopy("Velocidad de reproducción", "Ajusta el ritmo de la narración", "Retroceder al reanudar", "Retrocede el tiempo configurado al continuar")
    AppLanguage.Italian -> AudiobookExtraCopy("Velocità di riproduzione", "Regola la velocità della narrazione", "Riavvolgi alla ripresa", "Torna indietro del tempo impostato alla ripresa")
    AppLanguage.Portuguese -> AudiobookExtraCopy("Velocidade de reprodução", "Ajuste a velocidade da narração", "Recuar ao retomar", "Recue o tempo definido ao continuar")
    AppLanguage.Russian -> AudiobookExtraCopy("Скорость воспроизведения", "Настройте скорость повествования", "Отматывать при продолжении", "При продолжении отматывать на заданное время")
    AppLanguage.Ukrainian -> AudiobookExtraCopy("Швидкість відтворення", "Налаштуйте швидкість оповіді", "Перемотувати під час продовження", "При продовженні перемотувати на заданий час")
    AppLanguage.Czech -> AudiobookExtraCopy("Rychlost přehrávání", "Upravte rychlost vyprávění", "Při obnovení převinout zpět", "Při pokračování se vrátit o nastavený čas")
    AppLanguage.Slovak -> AudiobookExtraCopy("Rýchlosť prehrávania", "Upravte rýchlosť rozprávania", "Pri obnovení pretočiť späť", "Pri pokračovaní sa vrátiť o nastavený čas")
    AppLanguage.Dutch -> AudiobookExtraCopy("Afspeelsnelheid", "Pas de snelheid van de vertelling aan", "Terugspoelen bij hervatten", "Ga bij hervatten de ingestelde tijd terug")
    AppLanguage.Swedish -> AudiobookExtraCopy("Uppspelningshastighet", "Justera berättelsens hastighet", "Spola tillbaka vid återupptagning", "Gå tillbaka den inställda tiden när du fortsätter")
    AppLanguage.Danish -> AudiobookExtraCopy("Afspilningshastighed", "Juster fortællingens hastighed", "Spol tilbage ved genoptagelse", "Gå den indstillede tid tilbage, når du fortsætter")
    AppLanguage.Norwegian -> AudiobookExtraCopy("Avspillingshastighet", "Juster fortellerhastigheten", "Spol tilbake ved fortsettelse", "Gå tilbake den innstilte tiden når du fortsetter")
    AppLanguage.English -> AudiobookExtraCopy("Playback speed", "Adjust audiobook narration speed", "Rewind when resuming", "Go back by the rewind amount when continuing a book")
}

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
    AppLanguage.Estonian -> AudiobookSettingsCopy("Audioraamatute seaded", "Kohanda audioraamatute esitamist", "Tagasikerimise aeg", "Edasikerimise aeg", "s", "Jätka audioraamatu esitamist", "Jätka viimati salvestatud kohast")
    AppLanguage.Greek -> AudiobookSettingsCopy("Ρυθμίσεις ηχητικών βιβλίων", "Προσαρμόστε την αναπαραγωγή ηχητικών βιβλίων", "Χρόνος προς τα πίσω", "Χρόνος προς τα εμπρός", "δ", "Συνέχιση ηχητικού βιβλίου", "Συνεχίστε από την τελευταία αποθηκευμένη θέση")
    AppLanguage.Hungarian -> AudiobookSettingsCopy("Hangoskönyv-beállítások", "Szabd testre a hangoskönyvek lejátszását", "Visszaugrás ideje", "Előreugrás ideje", "mp", "Hangoskönyv folytatása", "Folytatás az utoljára mentett helyről")
    AppLanguage.Latvian -> AudiobookSettingsCopy("Audiogrāmatu iestatījumi", "Pielāgo audiogrāmatu atskaņošanu", "Attīšanas laiks", "Pārtīšanas laiks", "s", "Turpināt audiogrāmatu", "Turpini no pēdējās saglabātās vietas")
    AppLanguage.Lithuanian -> AudiobookSettingsCopy("Garsinių knygų nustatymai", "Tinkink garsinių knygų atkūrimą", "Atsukimo trukmė", "Prasukimo trukmė", "s", "Tęsti garsinę knygą", "Tęsk nuo paskutinės išsaugotos vietos")
    AppLanguage.Malay -> AudiobookSettingsCopy("Tetapan buku audio", "Sesuaikan main balik buku audio", "Tempoh undur", "Tempoh maju", "s", "Sambung main buku audio", "Teruskan dari kedudukan terakhir yang disimpan")
    AppLanguage.Macedonian -> AudiobookSettingsCopy("Поставки за аудиокниги", "Приспособете ја репродукцијата на аудиокниги", "Премотување назад", "Премотување напред", "с", "Продолжи со аудиокнигата", "Продолжете од последното зачувано место")
    AppLanguage.ChineseSimplified -> AudiobookSettingsCopy("有声书设置", "自定义有声书播放", "后退时长", "前进时长", "秒", "继续播放有声书", "从上次保存的位置继续")
    AppLanguage.Hindi -> AudiobookSettingsCopy("ऑडियोबुक सेटिंग्स", "ऑडियोबुक प्लेबैक अनुकूलित करें", "पीछे जाने की अवधि", "आगे जाने की अवधि", "से", "ऑडियोबुक जारी रखें", "पिछली सहेजी गई जगह से जारी रखें")
    AppLanguage.Japanese -> AudiobookSettingsCopy("オーディオブックの設定", "オーディオブックの再生をカスタマイズ", "戻る時間", "進む時間", "秒", "オーディオブックを再開", "最後に保存した位置から続ける")
    AppLanguage.Korean -> AudiobookSettingsCopy("오디오북 설정", "오디오북 재생 맞춤 설정", "뒤로 이동", "앞으로 이동", "초", "오디오북 이어 듣기", "마지막으로 저장된 위치부터 계속 듣기")
    AppLanguage.Thai -> AudiobookSettingsCopy("ตั้งค่าหนังสือเสียง", "ปรับแต่งการเล่นหนังสือเสียง", "เวลาย้อนกลับ", "เวลาเดินหน้า", "วิ", "เล่นหนังสือเสียงต่อ", "ฟังต่อจากตำแหน่งที่บันทึกล่าสุด")
    AppLanguage.Albanian -> AudiobookSettingsCopy("Cilësimet e librave audio", "Personalizo riprodhimin e librave audio", "Koha e kthimit pas", "Koha e kalimit përpara", "s", "Rifillo librin audio", "Vazhdo nga pozicioni i fundit i ruajtur")
    AppLanguage.Bengali -> AudiobookSettingsCopy("অডিওবুকের সেটিংস", "অডিওবুকের প্লেব্যাক সাজান", "পিছিয়ে যাওয়ার সময়", "এগিয়ে যাওয়ার সময়", "সে", "অডিওবুক চালানো চালিয়ে যান", "সর্বশেষ সংরক্ষিত স্থান থেকে চালিয়ে যান")
    AppLanguage.Croatian -> AudiobookSettingsCopy("Postavke audioknjiga", "Prilagodite reprodukciju audioknjiga", "Premotavanje unatrag", "Premotavanje unaprijed", "s", "Nastavi reprodukciju audioknjige", "Nastavite od posljednjeg spremljenog mjesta")
    AppLanguage.Serbian -> AudiobookSettingsCopy("Подешавања аудио-књига", "Прилагодите репродукцију аудио-књига", "Премотавање уназад", "Премотавање унапред", "с", "Настави репродукцију аудио-књиге", "Наставите од последњег сачуваног места")
    AppLanguage.Urdu -> AudiobookSettingsCopy("آڈیو بکس کی ترتیبات", "آڈیو بکس کا پلے بیک حسبِ پسند بنائیں", "پیچھے جانے کا دورانیہ", "آگے جانے کا دورانیہ", "سیکنڈ", "آڈیو بک جاری رکھیں", "آخری محفوظ مقام سے جاری رکھیں")
    AppLanguage.Bulgarian -> AudiobookSettingsCopy("Настройки на аудиокниги", "Настройте възпроизвеждането на аудиокниги", "Връщане назад", "Превъртане напред", "сек", "Продължи аудиокнигата", "Продължете от последната запазена позиция")
    AppLanguage.Catalan -> AudiobookSettingsCopy("Configuració dels audiollibres", "Personalitza la reproducció dels audiollibres", "Temps de retrocés", "Temps d’avanç", "s", "Reprèn l’audiollibre", "Continua des de l’última posició desada")
    AppLanguage.Finnish -> AudiobookSettingsCopy("Äänikirja-asetukset", "Mukauta äänikirjojen toistoa", "Taaksepäin siirtyminen", "Eteenpäin siirtyminen", "s", "Jatka äänikirjan toistoa", "Jatka viimeksi tallennetusta kohdasta")
    AppLanguage.Romanian -> AudiobookSettingsCopy("Setări pentru audiocărți", "Personalizează redarea audiocărților", "Salt înapoi", "Salt înainte", "s", "Reia audiocartea", "Continuă de la ultima poziție salvată")
    AppLanguage.Slovenian -> AudiobookSettingsCopy("Nastavitve zvočnih knjig", "Prilagodite predvajanje zvočnih knjig", "Preskok nazaj", "Preskok naprej", "s", "Nadaljuj predvajanje knjige", "Nadaljujte z zadnjega shranjenega mesta")
    AppLanguage.Bosnian -> AudiobookSettingsCopy("Postavke audioknjiga", "Prilagodite reprodukciju audioknjiga", "Premotavanje unazad", "Premotavanje unaprijed", "s", "Nastavi reprodukciju audioknjige", "Nastavite od posljednjeg sačuvanog mjesta")
    AppLanguage.Indonesian -> AudiobookSettingsCopy("Pengaturan buku audio", "Sesuaikan pemutaran buku audio", "Durasi mundur", "Durasi maju", "dtk", "Lanjutkan pemutaran buku audio", "Lanjutkan dari posisi terakhir yang disimpan")
    AppLanguage.Punjabi -> AudiobookSettingsCopy("ਆਡੀਓ ਕਿਤਾਬਾਂ ਦੀਆਂ ਸੈਟਿੰਗਾਂ", "ਆਡੀਓ ਕਿਤਾਬ ਦਾ ਪਲੇਬੈਕ ਬਦਲੋ", "ਪਿੱਛੇ ਜਾਣ ਦੀ ਮਿਆਦ", "ਅੱਗੇ ਜਾਣ ਦੀ ਮਿਆਦ", "ਸ", "ਆਡੀਓ ਕਿਤਾਬ ਜਾਰੀ ਰੱਖੋ", "ਪਿਛਲੀ ਸੰਭਾਲੀ ਥਾਂ ਤੋਂ ਜਾਰੀ ਰੱਖੋ")
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
    AppLanguage.English -> AudiobookSettingsCopy("Audiobook settings", "Customize audiobook playback", "Rewind amount", "Forward amount", "s", "Resume audiobook playback", "Continue from the last saved position")
}

internal data class SmartPlaylistSettingsCopy(
    val title: String,
    val subtitle: String,
    val availableMixes: String,
    val maximumSongs: String,
    val maximumSongsSubtitle: String,
)

internal fun smartPlaylistSettingsCopy(language: AppLanguage): SmartPlaylistSettingsCopy = when (language) {
    AppLanguage.Estonian -> SmartPlaylistSettingsCopy("Nutikad esitusloendid", "Kohanda nutikate esitusloendite käitumist", "Saadaolevad miksid", "Lugude ülempiir miksis", "Vali, mitu lugu iga nutikas miks võib näidata")
    AppLanguage.Greek -> SmartPlaylistSettingsCopy("Έξυπνες λίστες", "Προσαρμόστε τη συμπεριφορά των έξυπνων λιστών", "Διαθέσιμες μίξεις", "Μέγιστος αριθμός τραγουδιών ανά μίξη", "Επιλέξτε πόσα τραγούδια μπορεί να εμφανίζει κάθε μίξη")
    AppLanguage.Hungarian -> SmartPlaylistSettingsCopy("Intelligens lejátszási listák", "Állítsd be az intelligens listák működését", "Elérhető mixek", "Dalok maximális száma mixenként", "Válaszd ki, hány dalt jeleníthet meg egy mix")
    AppLanguage.Latvian -> SmartPlaylistSettingsCopy("Viedie atskaņošanas saraksti", "Pielāgo viedo sarakstu darbību", "Pieejamie miksi", "Maksimālais dziesmu skaits miksā", "Izvēlies, cik dziesmu katrs viedais mikss var rādīt")
    AppLanguage.Lithuanian -> SmartPlaylistSettingsCopy("Išmanieji grojaraščiai", "Pritaikyk išmaniųjų grojaraščių veikimą", "Galimi rinkiniai", "Didžiausias dainų skaičius rinkinyje", "Pasirink, kiek dainų gali rodyti kiekvienas išmanusis rinkinys")
    AppLanguage.Malay -> SmartPlaylistSettingsCopy("Senarai main pintar", "Laraskan cara senarai main pintar berfungsi", "Campuran tersedia", "Lagu maksimum setiap campuran", "Pilih bilangan lagu yang boleh dipaparkan oleh setiap campuran")
    AppLanguage.Macedonian -> SmartPlaylistSettingsCopy("Паметни плејлисти", "Приспособете го однесувањето на паметните плејлисти", "Достапни миксови", "Најмногу песни по микс", "Изберете колку песни може да прикаже секој паметен микс")
    AppLanguage.ChineseSimplified -> SmartPlaylistSettingsCopy("智能播放列表", "调整智能播放列表的行为", "可用混音", "每个混音的歌曲上限", "选择每个智能混音可显示的歌曲数量")
    AppLanguage.Hindi -> SmartPlaylistSettingsCopy("स्मार्ट प्लेलिस्ट", "स्मार्ट प्लेलिस्ट का व्यवहार बदलें", "उपलब्ध मिक्स", "हर मिक्स में अधिकतम गाने", "चुनें कि हर स्मार्ट मिक्स में कितने गाने दिखें")
    AppLanguage.Japanese -> SmartPlaylistSettingsCopy("スマートプレイリスト", "スマートプレイリストの動作を調整", "利用可能なミックス", "ミックスごとの最大曲数", "各スマートミックスに表示する曲数を選択")
    AppLanguage.Korean -> SmartPlaylistSettingsCopy("스마트 플레이리스트", "스마트 플레이리스트 동작 조정", "사용 가능한 믹스", "믹스당 최대 곡 수", "각 스마트 믹스에 표시할 곡 수 선택")
    AppLanguage.Thai -> SmartPlaylistSettingsCopy("เพลย์ลิสต์อัจฉริยะ", "ปรับการทำงานของเพลย์ลิสต์อัจฉริยะ", "มิกซ์ที่พร้อมใช้งาน", "จำนวนเพลงสูงสุดต่อมิกซ์", "เลือกจำนวนเพลงที่แสดงในแต่ละมิกซ์อัจฉริยะ")
    AppLanguage.Albanian -> SmartPlaylistSettingsCopy("Lista inteligjente", "Përshtat sjelljen e listave inteligjente", "Përzierjet e disponueshme", "Numri maksimal i këngëve", "Zgjidh sa këngë mund të shfaqë çdo përzierje inteligjente")
    AppLanguage.Bengali -> SmartPlaylistSettingsCopy("স্মার্ট প্লেলিস্ট", "স্মার্ট প্লেলিস্টের আচরণ বদলান", "উপলব্ধ মিক্স", "প্রতি মিক্সে সর্বোচ্চ গান", "প্রতিটি স্মার্ট মিক্সে কত গান দেখাবে তা বেছে নিন")
    AppLanguage.Croatian -> SmartPlaylistSettingsCopy("Pametne playliste", "Prilagodite rad pametnih playlista", "Dostupni miksevi", "Najviše pjesama po miksu", "Odaberite koliko pjesama svaki pametni miks može prikazati")
    AppLanguage.Serbian -> SmartPlaylistSettingsCopy("Паметне плејлисте", "Прилагодите рад паметних плејлиста", "Доступни миксеви", "Највише песама по миксу", "Изаберите колико песама сваки паметни микс може да прикаже")
    AppLanguage.Urdu -> SmartPlaylistSettingsCopy("اسمارٹ پلے لسٹس", "اسمارٹ پلے لسٹس کا رویہ تبدیل کریں", "دستیاب مکس", "ہر مکس میں زیادہ سے زیادہ گانے", "منتخب کریں کہ ہر اسمارٹ مکس میں کتنے گانے دکھائے جائیں")
    AppLanguage.Bulgarian -> SmartPlaylistSettingsCopy("Интелигентни плейлисти", "Настройте поведението на интелигентните плейлисти", "Налични миксове", "Максимум песни в микс", "Изберете колко песни може да показва всеки микс")
    AppLanguage.Catalan -> SmartPlaylistSettingsCopy("Llistes intel·ligents", "Ajusta el comportament de les llistes intel·ligents", "Mescles disponibles", "Màxim de cançons per mescla", "Tria quantes cançons pot mostrar cada mescla")
    AppLanguage.Finnish -> SmartPlaylistSettingsCopy("Älykkäät soittolistat", "Mukauta älykkäiden soittolistojen toimintaa", "Saatavilla olevat miksaukset", "Kappaleiden enimmäismäärä", "Valitse kuinka monta kappaletta kukin miksaus voi näyttää")
    AppLanguage.Romanian -> SmartPlaylistSettingsCopy("Liste inteligente", "Configurează comportamentul listelor inteligente", "Mixuri disponibile", "Număr maxim de melodii", "Alege câte melodii poate afișa fiecare mix")
    AppLanguage.Slovenian -> SmartPlaylistSettingsCopy("Pametni seznami", "Prilagodite delovanje pametnih seznamov", "Razpoložljive mešanice", "Največ skladb v mešanici", "Izberite, koliko skladb lahko prikaže posamezna mešanica")
    AppLanguage.Bosnian -> SmartPlaylistSettingsCopy("Pametne plejliste", "Prilagodite rad pametnih plejlista", "Dostupni miksevi", "Najviše pjesama po miksu", "Odaberite koliko pjesama svaki pametni miks može prikazati")
    AppLanguage.Indonesian -> SmartPlaylistSettingsCopy("Daftar putar pintar", "Atur cara kerja daftar putar pintar", "Campuran tersedia", "Jumlah lagu maksimum per campuran", "Pilih jumlah lagu yang dapat ditampilkan setiap campuran pintar")
    AppLanguage.Punjabi -> SmartPlaylistSettingsCopy("ਸਮਾਰਟ ਪਲੇਲਿਸਟਾਂ", "ਸਮਾਰਟ ਪਲੇਲਿਸਟਾਂ ਦਾ ਵਿਹਾਰ ਬਦਲੋ", "ਉਪਲਬਧ ਮਿਕਸ", "ਹਰ ਮਿਕਸ ਵਿੱਚ ਵੱਧ ਤੋਂ ਵੱਧ ਗੀਤ", "ਚੁਣੋ ਕਿ ਹਰ ਸਮਾਰਟ ਮਿਕਸ ਵਿੱਚ ਕਿੰਨੇ ਗੀਤ ਦਿਖਾਏ ਜਾਣ")
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
    AppLanguage.English -> SmartPlaylistSettingsCopy("Smart playlists", "Adjust how smart playlists behave", "Available mixes", "Maximum songs per mix", "Choose how many songs each smart mix can show")
}

@Suppress("CyclomaticComplexMethod")
internal fun smartPlaylistDescription(type: BuiltInSmartPlaylistType, language: AppLanguage): String = when (type) {
    BuiltInSmartPlaylistType.RecentlyAdded -> when (language) {
        AppLanguage.Bulgarian -> "Песни, добавени наскоро в библиотеката"
        AppLanguage.Catalan -> "Cançons afegides recentment a la biblioteca"
        AppLanguage.Finnish -> "Äskettäin kirjastoon lisätyt kappaleet"
        AppLanguage.Romanian -> "Melodii adăugate recent în bibliotecă"
        AppLanguage.Slovenian -> "Skladbe, nedavno dodane v knjižnico"
        AppLanguage.Bosnian -> "Pjesme nedavno dodane u biblioteku"
        AppLanguage.Indonesian -> "Lagu yang baru ditambahkan ke pustaka"
        AppLanguage.Punjabi -> "ਲਾਇਬ੍ਰੇਰੀ ਵਿੱਚ ਹਾਲ ਹੀ ਵਿੱਚ ਜੋੜੇ ਗੀਤ"
        AppLanguage.Albanian -> "Këngët e shtuara së fundmi në bibliotekën tënde"
        AppLanguage.Bengali -> "তোমার লাইব্রেরিতে সম্প্রতি যোগ করা গান"
        AppLanguage.ChineseSimplified -> "最近添加到媒体库的歌曲"
        AppLanguage.Croatian -> "Pjesme nedavno dodane u tvoju biblioteku"
        AppLanguage.Czech -> "Skladby nedávno přidané do tvé knihovny"
        AppLanguage.Danish -> "Sange, der for nylig er føjet til dit bibliotek"
        AppLanguage.Dutch -> "Nummers die onlangs aan je bibliotheek zijn toegevoegd"
        AppLanguage.English -> "Songs added to your library recently"
        AppLanguage.Estonian -> "Hiljuti sinu teeki lisatud laulud"
        AppLanguage.French -> "Les morceaux récemment ajoutés à votre bibliothèque"
        AppLanguage.German -> "Zuletzt zu deiner Bibliothek hinzugefügte Songs"
        AppLanguage.Greek -> "Τραγούδια που προστέθηκαν πρόσφατα στη βιβλιοθήκη σου"
        AppLanguage.Hindi -> "आपकी लाइब्रेरी में हाल ही में जोड़े गए गाने"
        AppLanguage.Hungarian -> "A nemrég hozzáadott dalok a könyvtáradban"
        AppLanguage.Italian -> "Brani aggiunti di recente alla tua libreria"
        AppLanguage.Japanese -> "ライブラリに最近追加された曲"
        AppLanguage.Korean -> "라이브러리에 최근 추가된 노래"
        AppLanguage.Latvian -> "Dziesmas, kas nesen pievienotas tavai bibliotēkai"
        AppLanguage.Lithuanian -> "Neseniai į tavo biblioteką įtrauktos dainos"
        AppLanguage.Macedonian -> "Песни неодамна додадени во твојата библиотека"
        AppLanguage.Malay -> "Lagu yang baru ditambahkan ke pustaka anda"
        AppLanguage.Norwegian -> "Sanger som nylig ble lagt til i biblioteket ditt"
        AppLanguage.Polish -> "Utwory niedawno dodane do Twojej biblioteki"
        AppLanguage.Portuguese -> "Músicas adicionadas recentemente à sua biblioteca"
        AppLanguage.Russian -> "Песни, недавно добавленные в вашу медиатеку"
        AppLanguage.Serbian -> "Песме недавно додате у твоју библиотеку"
        AppLanguage.Slovak -> "Skladby nedávno pridané do tvojej knižnice"
        AppLanguage.Spanish -> "Canciones añadidas recientemente a tu biblioteca"
        AppLanguage.Swedish -> "Låtar som nyligen lagts till i ditt bibliotek"
        AppLanguage.Thai -> "เพลงที่เพิ่มลงในคลังของคุณเมื่อไม่นานมานี้"
        AppLanguage.Ukrainian -> "Пісні, нещодавно додані до вашої бібліотеки"
        AppLanguage.Urdu -> "آپ کی لائبریری میں حال ہی میں شامل کیے گئے گانے"
    }
    BuiltInSmartPlaylistType.MostPlayed -> when (language) {
        AppLanguage.Bulgarian -> "Песните, които слушате най-често"
        AppLanguage.Catalan -> "Les cançons que escoltes més sovint"
        AppLanguage.Finnish -> "Useimmin kuuntelemasi kappaleet"
        AppLanguage.Romanian -> "Melodiile pe care le asculți cel mai des"
        AppLanguage.Slovenian -> "Skladbe, ki jih najpogosteje poslušate"
        AppLanguage.Bosnian -> "Pjesme koje najčešće slušate"
        AppLanguage.Indonesian -> "Lagu yang paling sering Anda dengarkan"
        AppLanguage.Punjabi -> "ਗੀਤ ਜੋ ਤੁਸੀਂ ਸਭ ਤੋਂ ਵੱਧ ਸੁਣਦੇ ਹੋ"
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
        AppLanguage.Bulgarian -> "Песните, отбелязани като любими"
        AppLanguage.Catalan -> "Les cançons que has marcat com a preferides"
        AppLanguage.Finnish -> "Suosikeiksi merkitsemäsi kappaleet"
        AppLanguage.Romanian -> "Melodiile marcate ca favorite"
        AppLanguage.Slovenian -> "Skladbe, označene kot priljubljene"
        AppLanguage.Bosnian -> "Pjesme koje ste označili kao omiljene"
        AppLanguage.Indonesian -> "Lagu yang Anda tandai sebagai favorit"
        AppLanguage.Punjabi -> "ਗੀਤ ਜਿਨ੍ਹਾਂ ਨੂੰ ਤੁਸੀਂ ਮਨਪਸੰਦ ਬਣਾਇਆ ਹੈ"
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
