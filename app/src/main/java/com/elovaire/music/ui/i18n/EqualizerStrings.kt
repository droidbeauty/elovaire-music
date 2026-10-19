package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class EqualizerCopy(
    val presets: String,
    val savePreset: String,
    val presetName: String,
    val customPresets: String,
    val replace: String,
    val replaceWarning: String,
)

internal fun equalizerCopy(language: AppLanguage): EqualizerCopy = when (language) {
    AppLanguage.English -> EqualizerCopy("Presets", "Save preset", "Preset name", "Your presets", "Replace", "This name is already in use; saving will replace that preset")
    AppLanguage.Polish -> EqualizerCopy("Ustawienia korektora", "Zapisz ustawienia", "Nazwa ustawień", "Twoje ustawienia", "Zastąp", "Ta nazwa jest już używana — zapisanie zastąpi poprzednie ustawienia")
    AppLanguage.Albanian -> EqualizerCopy("Paracaktime", "Ruaj paracaktimin", "Emri i paracaktimit", "Paracaktimet e tua", "Zëvendëso", "Ky emër është në përdorim; ruajtja do të zëvendësojë paracaktimin")
    AppLanguage.ChineseSimplified -> EqualizerCopy("预设", "保存预设", "预设名称", "你的预设", "替换", "此名称已被使用，保存将替换该预设")
    AppLanguage.Croatian -> EqualizerCopy("Postavke", "Spremi postavku", "Naziv postavke", "Vaše postavke", "Zamijeni", "Ovaj se naziv već koristi; spremanje će zamijeniti tu postavku")
    AppLanguage.Czech -> EqualizerCopy("Předvolby", "Uložit předvolbu", "Název předvolby", "Vaše předvolby", "Nahradit", "Tento název se již používá; uložením nahradíte původní předvolbu")
    AppLanguage.Danish -> EqualizerCopy("Forudindstillinger", "Gem indstilling", "Indstillingens navn", "Dine indstillinger", "Erstat", "Navnet er allerede i brug; den eksisterende indstilling erstattes")
    AppLanguage.Dutch -> EqualizerCopy("Voorinstellingen", "Voorinstelling opslaan", "Naam voorinstelling", "Je voorinstellingen", "Vervangen", "Deze naam is al in gebruik; opslaan vervangt die voorinstelling")
    AppLanguage.Bengali -> EqualizerCopy("প্রিসেট", "প্রিসেট সংরক্ষণ", "প্রিসেটের নাম", "আপনার প্রিসেট", "প্রতিস্থাপন", "এই নামটি ইতিমধ্যে ব্যবহৃত হচ্ছে; সংরক্ষণ করলে আগের প্রিসেটটি প্রতিস্থাপিত হবে")
    AppLanguage.Estonian -> EqualizerCopy("Eelseaded", "Salvesta eelseade", "Eelseade nimi", "Sinu eelseaded", "Asenda", "See nimi on juba kasutusel; salvestamine asendab olemasoleva eelseade")
    AppLanguage.French -> EqualizerCopy("Préréglages", "Enregistrer", "Nom du préréglage", "Vos préréglages", "Remplacer", "Ce nom est déjà utilisé ; l’enregistrement remplacera ce préréglage")
    AppLanguage.German -> EqualizerCopy("Voreinstellungen", "Speichern", "Name der Voreinstellung", "Deine Voreinstellungen", "Ersetzen", "Dieser Name wird bereits verwendet; beim Speichern wird die Voreinstellung ersetzt")
    AppLanguage.Greek -> EqualizerCopy("Προεπιλογές", "Αποθήκευση", "Όνομα προεπιλογής", "Οι προεπιλογές σου", "Αντικατάσταση", "Αυτό το όνομα χρησιμοποιείται ήδη· η αποθήκευση θα αντικαταστήσει την προεπιλογή")
    AppLanguage.Hindi -> EqualizerCopy("प्रीसेट", "प्रीसेट सहेजें", "प्रीसेट का नाम", "आपके प्रीसेट", "बदलें", "यह नाम पहले से उपयोग में है; सहेजने पर पुराना प्रीसेट बदल जाएगा")
    AppLanguage.Hungarian -> EqualizerCopy("Hangzásprofilok", "Profil mentése", "Profil neve", "Saját profilok", "Felülírás", "Ez a név már használatban van; a mentés felülírja a korábbi profilt")
    AppLanguage.Italian -> EqualizerCopy("Preimpostazioni", "Salva", "Nome preimpostazione", "Le tue preimpostazioni", "Sostituisci", "Questo nome è già in uso; il salvataggio sostituirà la preimpostazione")
    AppLanguage.Japanese -> EqualizerCopy("プリセット", "プリセットを保存", "プリセット名", "自分のプリセット", "置き換える", "この名前は使用中です。保存すると既存のプリセットが置き換わります")
    AppLanguage.Korean -> EqualizerCopy("프리셋", "프리셋 저장", "프리셋 이름", "내 프리셋", "교체", "이미 사용 중인 이름입니다. 저장하면 기존 프리셋이 교체됩니다")
    AppLanguage.Latin -> EqualizerCopy("Praefinitiones", "Serva", "Nomen praefinitionis", "Praefinitiones tuae", "Substitue", "Hoc nomen iam in usu est; servando praefinitionem priorem substitues")
    AppLanguage.Latvian -> EqualizerCopy("Priekšiestatījumi", "Saglabāt", "Priekšiestatījuma nosaukums", "Jūsu priekšiestatījumi", "Aizstāt", "Šis nosaukums jau tiek izmantots; saglabājot tiks aizstāts iepriekšējais priekšiestatījums")
    AppLanguage.Lithuanian -> EqualizerCopy("Išankstiniai nustatymai", "Išsaugoti", "Nustatymo pavadinimas", "Jūsų nustatymai", "Pakeisti", "Šis pavadinimas jau naudojamas; išsaugojus ankstesnis nustatymas bus pakeistas")
    AppLanguage.Malay -> EqualizerCopy("Pratetap", "Simpan pratetap", "Nama pratetap", "Pratetap anda", "Ganti", "Nama ini sudah digunakan; menyimpan akan menggantikan pratetap itu")
    AppLanguage.Macedonian -> EqualizerCopy("Поставки", "Зачувај поставка", "Име на поставката", "Ваши поставки", "Замени", "Ова име веќе се користи; зачувувањето ќе ја замени претходната поставка")
    AppLanguage.Norwegian -> EqualizerCopy("Forhåndsinnstillinger", "Lagre innstilling", "Navn på innstilling", "Dine innstillinger", "Erstatt", "Navnet er allerede i bruk; lagring erstatter den eksisterende innstillingen")
    AppLanguage.Portuguese -> EqualizerCopy("Predefinições", "Guardar", "Nome da predefinição", "As suas predefinições", "Substituir", "Este nome já está em uso; guardar irá substituir essa predefinição")
    AppLanguage.Russian -> EqualizerCopy("Пресеты", "Сохранить пресет", "Название пресета", "Ваши пресеты", "Заменить", "Это название уже используется; сохранение заменит существующий пресет")
    AppLanguage.Slovak -> EqualizerCopy("Predvoľby", "Uložiť predvoľbu", "Názov predvoľby", "Vaše predvoľby", "Nahradiť", "Tento názov sa už používa; uložením nahradíte pôvodnú predvoľbu")
    AppLanguage.Serbian -> EqualizerCopy("Подешавања", "Сачувај подешавање", "Назив подешавања", "Ваша подешавања", "Замени", "Овај назив се већ користи; чување ће заменити претходно подешавање")
    AppLanguage.Spanish -> EqualizerCopy("Preajustes", "Guardar preajuste", "Nombre del preajuste", "Tus preajustes", "Reemplazar", "Este nombre ya está en uso; al guardar se reemplazará ese preajuste")
    AppLanguage.Swedish -> EqualizerCopy("Förinställningar", "Spara inställning", "Inställningens namn", "Dina inställningar", "Ersätt", "Namnet används redan; den befintliga inställningen ersätts när du sparar")
    AppLanguage.Thai -> EqualizerCopy("พรีเซ็ต", "บันทึกพรีเซ็ต", "ชื่อพรีเซ็ต", "พรีเซ็ตของคุณ", "แทนที่", "ชื่อนี้ถูกใช้แล้ว การบันทึกจะแทนที่พรีเซ็ตเดิม")
    AppLanguage.Ukrainian -> EqualizerCopy("Пресети", "Зберегти пресет", "Назва пресета", "Ваші пресети", "Замінити", "Ця назва вже використовується; збереження замінить наявний пресет")
    AppLanguage.Urdu -> EqualizerCopy("پری سیٹس", "پری سیٹ محفوظ کریں", "پری سیٹ کا نام", "آپ کے پری سیٹس", "تبدیل کریں", "یہ نام پہلے سے استعمال میں ہے؛ محفوظ کرنے سے پرانا پری سیٹ تبدیل ہو جائے گا")
}
