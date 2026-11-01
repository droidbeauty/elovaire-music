package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class AboutUpdateCopy(
    val updateAvailable: String,
    val version: String,
    val later: String,
    val download: String,
    val closeChangelog: String,
    val noChangelog: String,
)

internal fun aboutUpdateCopy(language: AppLanguage): AboutUpdateCopy = when (language) {
    AppLanguage.Albanian -> AboutUpdateCopy("Përditësim i disponueshëm", "Versioni", "Më vonë", "Shkarko", "Mbyll ndryshimet", "Ende nuk ka ndryshime")
    AppLanguage.Bengali -> AboutUpdateCopy("আপডেট উপলভ্য", "সংস্করণ", "পরে", "ডাউনলোড", "পরিবর্তনপঞ্জি বন্ধ করুন", "এখনও কোনো পরিবর্তন নেই")
    AppLanguage.Bosnian -> AboutUpdateCopy("Dostupno je ažuriranje", "Verzija", "Kasnije", "Preuzmi", "Zatvori listu promjena", "Još nema promjena")
    AppLanguage.Bulgarian -> AboutUpdateCopy("Налична е актуализация", "Версия", "По-късно", "Изтегли", "Затвори списъка с промени", "Все още няма промени")
    AppLanguage.Catalan -> AboutUpdateCopy("Actualització disponible", "Versió", "Més tard", "Baixa", "Tanca les novetats", "Encara no hi ha novetats")
    AppLanguage.ChineseSimplified -> AboutUpdateCopy("有可用更新", "版本", "稍后", "下载", "关闭更新日志", "暂无更新记录")
    AppLanguage.Croatian -> AboutUpdateCopy("Dostupno je ažuriranje", "Verzija", "Kasnije", "Preuzmi", "Zatvori popis promjena", "Još nema promjena")
    AppLanguage.Czech -> AboutUpdateCopy("Je k dispozici aktualizace", "Verze", "Později", "Stáhnout", "Zavřít přehled změn", "Zatím žádné změny")
    AppLanguage.Danish -> AboutUpdateCopy("Opdatering tilgængelig", "Version", "Senere", "Download", "Luk ændringsloggen", "Ingen ændringer endnu")
    AppLanguage.Dutch -> AboutUpdateCopy("Update beschikbaar", "Versie", "Later", "Downloaden", "Wijzigingen sluiten", "Nog geen wijzigingen")
    AppLanguage.English -> AboutUpdateCopy("Update available", "Version", "Later", "Download", "Close changelog", "No changelog entries yet")
    AppLanguage.Estonian -> AboutUpdateCopy("Värskendus on saadaval", "Versioon", "Hiljem", "Laadi alla", "Sulge muudatuste loend", "Muudatusi veel pole")
    AppLanguage.Finnish -> AboutUpdateCopy("Päivitys saatavilla", "Versio", "Myöhemmin", "Lataa", "Sulje muutosloki", "Ei vielä muutoksia")
    AppLanguage.French -> AboutUpdateCopy("Mise à jour disponible", "Version", "Plus tard", "Télécharger", "Fermer les nouveautés", "Aucune nouveauté pour le moment")
    AppLanguage.German -> AboutUpdateCopy("Update verfügbar", "Version", "Später", "Herunterladen", "Änderungsprotokoll schließen", "Noch keine Änderungen")
    AppLanguage.Greek -> AboutUpdateCopy("Διαθέσιμη ενημέρωση", "Έκδοση", "Αργότερα", "Λήψη", "Κλείσιμο αλλαγών", "Δεν υπάρχουν ακόμα αλλαγές")
    AppLanguage.Hindi -> AboutUpdateCopy("अपडेट उपलब्ध है", "संस्करण", "बाद में", "डाउनलोड करें", "बदलाव सूची बंद करें", "अभी कोई बदलाव नहीं")
    AppLanguage.Hungarian -> AboutUpdateCopy("Frissítés érhető el", "Verzió", "Később", "Letöltés", "Változások bezárása", "Még nincsenek változások")
    AppLanguage.Indonesian -> AboutUpdateCopy("Pembaruan tersedia", "Versi", "Nanti", "Unduh", "Tutup riwayat perubahan", "Belum ada perubahan")
    AppLanguage.Italian -> AboutUpdateCopy("Aggiornamento disponibile", "Versione", "Più tardi", "Scarica", "Chiudi le novità", "Nessuna novità per ora")
    AppLanguage.Japanese -> AboutUpdateCopy("アップデートがあります", "バージョン", "後で", "ダウンロード", "更新履歴を閉じる", "更新履歴はまだありません")
    AppLanguage.Korean -> AboutUpdateCopy("업데이트가 있습니다", "버전", "나중에", "다운로드", "변경 사항 닫기", "아직 변경 사항이 없습니다")
    AppLanguage.Latin -> AboutUpdateCopy("Renovatio praesto", "Versio", "Postea", "Depone", "Mutationes claude", "Nullae adhuc mutationes")
    AppLanguage.Latvian -> AboutUpdateCopy("Pieejams atjauninājums", "Versija", "Vēlāk", "Lejupielādēt", "Aizvērt izmaiņu sarakstu", "Vēl nav izmaiņu")
    AppLanguage.Lithuanian -> AboutUpdateCopy("Yra naujinys", "Versija", "Vėliau", "Atsisiųsti", "Uždaryti pakeitimų sąrašą", "Pakeitimų dar nėra")
    AppLanguage.Macedonian -> AboutUpdateCopy("Достапно е ажурирање", "Верзија", "Подоцна", "Преземи", "Затвори ги промените", "Сè уште нема промени")
    AppLanguage.Malay -> AboutUpdateCopy("Kemas kini tersedia", "Versi", "Kemudian", "Muat turun", "Tutup log perubahan", "Belum ada perubahan")
    AppLanguage.Norwegian -> AboutUpdateCopy("Oppdatering tilgjengelig", "Versjon", "Senere", "Last ned", "Lukk endringsloggen", "Ingen endringer ennå")
    AppLanguage.Polish -> AboutUpdateCopy("Dostępna aktualizacja", "Wersja", "Później", "Pobierz", "Zamknij listę zmian", "Nie ma jeszcze żadnych zmian")
    AppLanguage.Portuguese -> AboutUpdateCopy("Atualização disponível", "Versão", "Mais tarde", "Transferir", "Fechar novidades", "Ainda não há alterações")
    AppLanguage.Punjabi -> AboutUpdateCopy("ਅੱਪਡੇਟ ਉਪਲਬਧ ਹੈ", "ਵਰਜਨ", "ਬਾਅਦ ਵਿੱਚ", "ਡਾਊਨਲੋਡ ਕਰੋ", "ਤਬਦੀਲੀਆਂ ਦੀ ਸੂਚੀ ਬੰਦ ਕਰੋ", "ਹਾਲੇ ਕੋਈ ਤਬਦੀਲੀ ਨਹੀਂ")
    AppLanguage.Romanian -> AboutUpdateCopy("Actualizare disponibilă", "Versiunea", "Mai târziu", "Descarcă", "Închide lista de modificări", "Nu există încă modificări")
    AppLanguage.Russian -> AboutUpdateCopy("Доступно обновление", "Версия", "Позже", "Скачать", "Закрыть список изменений", "Изменений пока нет")
    AppLanguage.Serbian -> AboutUpdateCopy("Доступно је ажурирање", "Верзија", "Касније", "Преузми", "Затвори списак промена", "Још нема промена")
    AppLanguage.Slovak -> AboutUpdateCopy("Aktualizácia je dostupná", "Verzia", "Neskôr", "Stiahnuť", "Zavrieť prehľad zmien", "Zatiaľ žiadne zmeny")
    AppLanguage.Slovenian -> AboutUpdateCopy("Posodobitev je na voljo", "Različica", "Pozneje", "Prenesi", "Zapri seznam sprememb", "Sprememb še ni")
    AppLanguage.Spanish -> AboutUpdateCopy("Actualización disponible", "Versión", "Más tarde", "Descargar", "Cerrar novedades", "Todavía no hay novedades")
    AppLanguage.Swedish -> AboutUpdateCopy("Uppdatering tillgänglig", "Version", "Senare", "Ladda ner", "Stäng ändringsloggen", "Inga ändringar än")
    AppLanguage.Thai -> AboutUpdateCopy("มีอัปเดตพร้อมใช้งาน", "เวอร์ชัน", "ภายหลัง", "ดาวน์โหลด", "ปิดบันทึกการเปลี่ยนแปลง", "ยังไม่มีรายการเปลี่ยนแปลง")
    AppLanguage.Ukrainian -> AboutUpdateCopy("Доступне оновлення", "Версія", "Пізніше", "Завантажити", "Закрити список змін", "Змін поки немає")
    AppLanguage.Urdu -> AboutUpdateCopy("اپ ڈیٹ دستیاب ہے", "ورژن", "بعد میں", "ڈاؤن لوڈ کریں", "تبدیلیوں کی فہرست بند کریں", "ابھی کوئی تبدیلی نہیں")
}

internal data class UpdateActionCopy(
    val checking: String,
    val downloading: String,
    val installing: String,
    val allowInstall: String,
    val update: String,
    val retry: String,
)

internal fun updateActionCopy(language: AppLanguage): UpdateActionCopy = when (language) {
    AppLanguage.Albanian -> UpdateActionCopy("Po kontrollohet…", "Po shkarkohet…", "Po instalohet…", "Lejo instalimin", "Përditëso", "Provo përsëri")
    AppLanguage.Bengali -> UpdateActionCopy("পরীক্ষা করা হচ্ছে…", "ডাউনলোড হচ্ছে…", "ইনস্টল হচ্ছে…", "ইনস্টলের অনুমতি দিন", "আপডেট করুন", "আবার চেষ্টা করুন")
    AppLanguage.Bosnian -> UpdateActionCopy("Provjera…", "Preuzimanje…", "Instaliranje…", "Dozvoli instalaciju", "Ažuriraj", "Pokušaj ponovo")
    AppLanguage.Bulgarian -> UpdateActionCopy("Проверка…", "Изтегляне…", "Инсталиране…", "Разреши инсталацията", "Актуализирай", "Опитай отново")
    AppLanguage.Catalan -> UpdateActionCopy("S’està comprovant…", "S’està baixant…", "S’està instal·lant…", "Permet la instal·lació", "Actualitza", "Torna-ho a provar")
    AppLanguage.ChineseSimplified -> UpdateActionCopy("正在检查…", "正在下载…", "正在安装…", "允许安装", "更新", "重试")
    AppLanguage.Croatian -> UpdateActionCopy("Provjera…", "Preuzimanje…", "Instaliranje…", "Dopusti instalaciju", "Ažuriraj", "Pokušaj ponovno")
    AppLanguage.Czech -> UpdateActionCopy("Kontrola…", "Stahování…", "Instalace…", "Povolit instalaci", "Aktualizovat", "Zkusit znovu")
    AppLanguage.Danish -> UpdateActionCopy("Kontrollerer…", "Downloader…", "Installerer…", "Tillad installation", "Opdater", "Prøv igen")
    AppLanguage.Dutch -> UpdateActionCopy("Controleren…", "Downloaden…", "Installeren…", "Installatie toestaan", "Bijwerken", "Opnieuw proberen")
    AppLanguage.English -> UpdateActionCopy("Checking...", "Downloading...", "Installing...", "Allow install", "Update", "Retry")
    AppLanguage.Estonian -> UpdateActionCopy("Kontrollimine…", "Allalaadimine…", "Installimine…", "Luba installimine", "Värskenda", "Proovi uuesti")
    AppLanguage.Finnish -> UpdateActionCopy("Tarkistetaan…", "Ladataan…", "Asennetaan…", "Salli asennus", "Päivitä", "Yritä uudelleen")
    AppLanguage.French -> UpdateActionCopy("Vérification…", "Téléchargement…", "Installation…", "Autoriser l’installation", "Mettre à jour", "Réessayer")
    AppLanguage.German -> UpdateActionCopy("Prüfung läuft…", "Wird heruntergeladen…", "Wird installiert…", "Installation erlauben", "Aktualisieren", "Erneut versuchen")
    AppLanguage.Greek -> UpdateActionCopy("Έλεγχος…", "Λήψη…", "Εγκατάσταση…", "Να επιτραπεί η εγκατάσταση", "Ενημέρωση", "Δοκιμάστε ξανά")
    AppLanguage.Hindi -> UpdateActionCopy("जाँच हो रही है…", "डाउनलोड हो रहा है…", "इंस्टॉल हो रहा है…", "इंस्टॉल की अनुमति दें", "अपडेट करें", "फिर कोशिश करें")
    AppLanguage.Hungarian -> UpdateActionCopy("Ellenőrzés…", "Letöltés…", "Telepítés…", "Telepítés engedélyezése", "Frissítés", "Újra")
    AppLanguage.Indonesian -> UpdateActionCopy("Memeriksa…", "Mengunduh…", "Memasang…", "Izinkan pemasangan", "Perbarui", "Coba lagi")
    AppLanguage.Italian -> UpdateActionCopy("Verifica…", "Download…", "Installazione…", "Consenti installazione", "Aggiorna", "Riprova")
    AppLanguage.Japanese -> UpdateActionCopy("確認中…", "ダウンロード中…", "インストール中…", "インストールを許可", "更新", "再試行")
    AppLanguage.Korean -> UpdateActionCopy("확인 중…", "다운로드 중…", "설치 중…", "설치 허용", "업데이트", "다시 시도")
    AppLanguage.Latin -> UpdateActionCopy("Verificatur…", "Deponitur…", "Installatur…", "Installationem permitte", "Renova", "Iterum conare")
    AppLanguage.Latvian -> UpdateActionCopy("Pārbauda…", "Lejupielādē…", "Instalē…", "Atļaut instalēšanu", "Atjaunināt", "Mēģināt vēlreiz")
    AppLanguage.Lithuanian -> UpdateActionCopy("Tikrinama…", "Atsisiunčiama…", "Diegiama…", "Leisti diegti", "Atnaujinti", "Bandyti dar kartą")
    AppLanguage.Macedonian -> UpdateActionCopy("Се проверува…", "Се презема…", "Се инсталира…", "Дозволи инсталација", "Ажурирај", "Обиди се повторно")
    AppLanguage.Malay -> UpdateActionCopy("Menyemak…", "Memuat turun…", "Memasang…", "Benarkan pemasangan", "Kemas kini", "Cuba lagi")
    AppLanguage.Norwegian -> UpdateActionCopy("Sjekker…", "Laster ned…", "Installerer…", "Tillat installasjon", "Oppdater", "Prøv igjen")
    AppLanguage.Polish -> UpdateActionCopy("Sprawdzanie…", "Pobieranie…", "Instalowanie…", "Zezwól na instalację", "Aktualizuj", "Spróbuj ponownie")
    AppLanguage.Portuguese -> UpdateActionCopy("A verificar…", "A transferir…", "A instalar…", "Permitir instalação", "Atualizar", "Tentar novamente")
    AppLanguage.Punjabi -> UpdateActionCopy("ਜਾਂਚ ਕੀਤੀ ਜਾ ਰਹੀ ਹੈ…", "ਡਾਊਨਲੋਡ ਹੋ ਰਿਹਾ ਹੈ…", "ਇੰਸਟਾਲ ਹੋ ਰਿਹਾ ਹੈ…", "ਇੰਸਟਾਲ ਦੀ ਇਜਾਜ਼ਤ ਦਿਓ", "ਅੱਪਡੇਟ ਕਰੋ", "ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ")
    AppLanguage.Romanian -> UpdateActionCopy("Se verifică…", "Se descarcă…", "Se instalează…", "Permite instalarea", "Actualizează", "Încearcă din nou")
    AppLanguage.Russian -> UpdateActionCopy("Проверка…", "Загрузка…", "Установка…", "Разрешить установку", "Обновить", "Повторить")
    AppLanguage.Serbian -> UpdateActionCopy("Провера…", "Преузимање…", "Инсталација…", "Дозволи инсталацију", "Ажурирај", "Покушај поново")
    AppLanguage.Slovak -> UpdateActionCopy("Kontrola…", "Sťahovanie…", "Inštalácia…", "Povoliť inštaláciu", "Aktualizovať", "Skúsiť znova")
    AppLanguage.Slovenian -> UpdateActionCopy("Preverjanje…", "Prenašanje…", "Nameščanje…", "Dovoli namestitev", "Posodobi", "Poskusi znova")
    AppLanguage.Spanish -> UpdateActionCopy("Comprobando…", "Descargando…", "Instalando…", "Permitir instalación", "Actualizar", "Reintentar")
    AppLanguage.Swedish -> UpdateActionCopy("Kontrollerar…", "Laddar ner…", "Installerar…", "Tillåt installation", "Uppdatera", "Försök igen")
    AppLanguage.Thai -> UpdateActionCopy("กำลังตรวจสอบ…", "กำลังดาวน์โหลด…", "กำลังติดตั้ง…", "อนุญาตการติดตั้ง", "อัปเดต", "ลองอีกครั้ง")
    AppLanguage.Ukrainian -> UpdateActionCopy("Перевірка…", "Завантаження…", "Установлення…", "Дозволити встановлення", "Оновити", "Спробувати ще раз")
    AppLanguage.Urdu -> UpdateActionCopy("جانچ جاری ہے…", "ڈاؤن لوڈ ہو رہا ہے…", "انسٹال ہو رہا ہے…", "انسٹال کی اجازت دیں", "اپ ڈیٹ کریں", "دوبارہ کوشش کریں")
}
