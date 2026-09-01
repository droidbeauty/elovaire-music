package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode


internal enum class UiPhrase {
    About,
    AddToPlaylist,
    AddToQueue,
    GoToAlbum,
    DeleteFromLibrary,
    DeleteAlbum,
    Delete,
    Rename,
    RemoveFromList,
    NewPlaylist,
    Cancel,
    Create,
    Reset,
    Dry,
    Wet,
    Off,
    Reverb,
    ToneShaping,
    Bass,
    Midrange,
    Treble,
    EffectStrength,
}

internal fun uiPhrase(language: AppLanguage, phrase: UiPhrase): String {
    return uiPhraseTranslations[language]?.get(phrase) ?: uiPhraseTranslations.getValue(AppLanguage.English).getValue(phrase)
}

private val uiPhraseTranslations = mapOf(
    AppLanguage.English to mapOf(
        UiPhrase.About to "About",
        UiPhrase.AddToPlaylist to "Add to playlist",
        UiPhrase.AddToQueue to "Add to queue",
        UiPhrase.GoToAlbum to "Go to album",
        UiPhrase.DeleteFromLibrary to "Delete from library",
        UiPhrase.DeleteAlbum to "Delete album",
        UiPhrase.Delete to "Delete",
        UiPhrase.Rename to "Rename",
        UiPhrase.RemoveFromList to "Remove from list",
        UiPhrase.NewPlaylist to "New playlist",
        UiPhrase.Cancel to "Cancel",
        UiPhrase.Create to "Create",
        UiPhrase.Reset to "Reset",
        UiPhrase.Dry to "Dry",
        UiPhrase.Wet to "Wet",
        UiPhrase.Off to "Off",
        UiPhrase.Reverb to "Reverb",
        UiPhrase.ToneShaping to "Tonal balance",
        UiPhrase.Bass to "Bass",
        UiPhrase.Midrange to "Midrange",
        UiPhrase.Treble to "Treble",
        UiPhrase.EffectStrength to "Effect strength",
    ),
    AppLanguage.Polish to mapOf(UiPhrase.About to "O aplikacji", UiPhrase.AddToPlaylist to "Dodaj do playlisty", UiPhrase.AddToQueue to "Dodaj do kolejki", UiPhrase.GoToAlbum to "Przejdź do albumu", UiPhrase.DeleteFromLibrary to "Usuń z biblioteki", UiPhrase.DeleteAlbum to "Usuń album", UiPhrase.Delete to "Usuń", UiPhrase.Rename to "Zmień nazwę", UiPhrase.RemoveFromList to "Usuń z listy", UiPhrase.NewPlaylist to "Nowa playlista", UiPhrase.Cancel to "Anuluj", UiPhrase.Create to "Utwórz", UiPhrase.Reset to "Resetuj", UiPhrase.Dry to "Suchy", UiPhrase.Wet to "Mokry", UiPhrase.Off to "Wyłączone", UiPhrase.Reverb to "Pogłos", UiPhrase.ToneShaping to "Balans tonalny", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Środek", UiPhrase.Treble to "Góra", UiPhrase.EffectStrength to "Siła efektu"),
    AppLanguage.Albanian to mapOf(UiPhrase.About to "Rreth", UiPhrase.AddToPlaylist to "Shto në listë", UiPhrase.AddToQueue to "Shto në radhë", UiPhrase.GoToAlbum to "Shko te albumi", UiPhrase.DeleteFromLibrary to "Fshi nga biblioteka", UiPhrase.DeleteAlbum to "Fshi albumin", UiPhrase.Delete to "Fshi", UiPhrase.Rename to "Riemërto", UiPhrase.RemoveFromList to "Hiq nga lista", UiPhrase.NewPlaylist to "Listë e re", UiPhrase.Cancel to "Anulo", UiPhrase.Create to "Krijo", UiPhrase.Reset to "Rivendos", UiPhrase.Dry to "I thatë", UiPhrase.Wet to "I lagësht", UiPhrase.Off to "Fikur", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Formësim toni", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mesatare", UiPhrase.Treble to "Të larta", UiPhrase.EffectStrength to "Fuqia e efektit"),
    AppLanguage.ChineseSimplified to mapOf(UiPhrase.About to "关于", UiPhrase.AddToPlaylist to "添加到播放列表", UiPhrase.AddToQueue to "添加到队列", UiPhrase.GoToAlbum to "前往专辑", UiPhrase.DeleteFromLibrary to "从媒体库删除", UiPhrase.DeleteAlbum to "删除专辑", UiPhrase.Delete to "删除", UiPhrase.Rename to "重命名", UiPhrase.RemoveFromList to "从列表移除", UiPhrase.NewPlaylist to "新建播放列表", UiPhrase.Cancel to "取消", UiPhrase.Create to "创建", UiPhrase.Reset to "重置", UiPhrase.Dry to "干声", UiPhrase.Wet to "湿声", UiPhrase.Off to "关闭", UiPhrase.Reverb to "混响", UiPhrase.ToneShaping to "音色塑形", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中频", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "效果强度"),
    AppLanguage.Croatian to mapOf(UiPhrase.About to "O aplikaciji", UiPhrase.AddToPlaylist to "Dodaj na popis", UiPhrase.AddToQueue to "Dodaj u red", UiPhrase.GoToAlbum to "Idi na album", UiPhrase.DeleteFromLibrary to "Izbriši iz biblioteke", UiPhrase.DeleteAlbum to "Izbriši album", UiPhrase.Delete to "Izbriši", UiPhrase.Rename to "Preimenuj", UiPhrase.RemoveFromList to "Ukloni s popisa", UiPhrase.NewPlaylist to "Novi popis", UiPhrase.Cancel to "Odustani", UiPhrase.Create to "Stvori", UiPhrase.Reset to "Resetiraj", UiPhrase.Dry to "Suho", UiPhrase.Wet to "Mokro", UiPhrase.Off to "Isključeno", UiPhrase.Reverb to "Odjek", UiPhrase.ToneShaping to "Oblikovanje tona", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Srednji", UiPhrase.Treble to "Visoki", UiPhrase.EffectStrength to "Jačina efekta"),
    AppLanguage.Slovak to mapOf(UiPhrase.About to "O aplikácii", UiPhrase.AddToPlaylist to "Pridať do playlistu", UiPhrase.AddToQueue to "Pridať do frontu", UiPhrase.GoToAlbum to "Prejsť na album", UiPhrase.DeleteFromLibrary to "Odstrániť z knižnice", UiPhrase.DeleteAlbum to "Odstrániť album", UiPhrase.Delete to "Odstrániť", UiPhrase.Rename to "Premenovať", UiPhrase.RemoveFromList to "Odstrániť zo zoznamu", UiPhrase.NewPlaylist to "Nový playlist", UiPhrase.Cancel to "Zrušiť", UiPhrase.Create to "Vytvoriť", UiPhrase.Reset to "Resetovať", UiPhrase.Dry to "Suchý", UiPhrase.Wet to "Mokrý", UiPhrase.Off to "Vypnuté", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Tónové vyváženie", UiPhrase.Bass to "Basy", UiPhrase.Midrange to "Stredy", UiPhrase.Treble to "Výšky", UiPhrase.EffectStrength to "Sila efektu"),
    AppLanguage.Korean to mapOf(UiPhrase.About to "정보", UiPhrase.AddToPlaylist to "플레이리스트에 추가", UiPhrase.AddToQueue to "대기열에 추가", UiPhrase.GoToAlbum to "앨범으로 이동", UiPhrase.DeleteFromLibrary to "라이브러리에서 삭제", UiPhrase.DeleteAlbum to "앨범 삭제", UiPhrase.Delete to "삭제", UiPhrase.Rename to "이름 변경", UiPhrase.RemoveFromList to "목록에서 제거", UiPhrase.NewPlaylist to "새 플레이리스트", UiPhrase.Cancel to "취소", UiPhrase.Create to "만들기", UiPhrase.Reset to "재설정", UiPhrase.Dry to "드라이", UiPhrase.Wet to "웻", UiPhrase.Off to "끔", UiPhrase.Reverb to "리버브", UiPhrase.ToneShaping to "톤 밸런스", UiPhrase.Bass to "저음", UiPhrase.Midrange to "중역", UiPhrase.Treble to "고역", UiPhrase.EffectStrength to "효과 강도"),
    AppLanguage.Malay to mapOf(UiPhrase.About to "Perihal", UiPhrase.AddToPlaylist to "Tambah ke senarai main", UiPhrase.AddToQueue to "Tambah ke barisan", UiPhrase.GoToAlbum to "Pergi ke album", UiPhrase.DeleteFromLibrary to "Padam daripada pustaka", UiPhrase.DeleteAlbum to "Padam album", UiPhrase.Delete to "Padam", UiPhrase.Rename to "Namakan semula", UiPhrase.RemoveFromList to "Buang daripada senarai", UiPhrase.NewPlaylist to "Senarai main baharu", UiPhrase.Cancel to "Batal", UiPhrase.Create to "Cipta", UiPhrase.Reset to "Tetapkan semula", UiPhrase.Dry to "Kering", UiPhrase.Wet to "Basah", UiPhrase.Off to "Mati", UiPhrase.Reverb to "Reverb", UiPhrase.ToneShaping to "Imbangan tonal", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Pertengahan", UiPhrase.Treble to "Trebel", UiPhrase.EffectStrength to "Kekuatan kesan"),
    AppLanguage.Bengali to mapOf(UiPhrase.About to "অ্যাপ সম্পর্কে", UiPhrase.AddToPlaylist to "প্লেলিস্টে যোগ করুন", UiPhrase.AddToQueue to "কিউতে যোগ করুন", UiPhrase.GoToAlbum to "অ্যালবামে যান", UiPhrase.DeleteFromLibrary to "লাইব্রেরি থেকে মুছুন", UiPhrase.DeleteAlbum to "অ্যালবাম মুছুন", UiPhrase.Delete to "মুছুন", UiPhrase.Rename to "নাম বদলান", UiPhrase.RemoveFromList to "তালিকা থেকে সরান", UiPhrase.NewPlaylist to "নতুন প্লেলিস্ট", UiPhrase.Cancel to "বাতিল", UiPhrase.Create to "তৈরি করুন", UiPhrase.Reset to "রিসেট", UiPhrase.Dry to "ড্রাই", UiPhrase.Wet to "ওয়েট", UiPhrase.Off to "বন্ধ", UiPhrase.Reverb to "রিভার্ব", UiPhrase.ToneShaping to "টোনাল ব্যালান্স", UiPhrase.Bass to "বেস", UiPhrase.Midrange to "মিডরেঞ্জ", UiPhrase.Treble to "ট্রেবল", UiPhrase.EffectStrength to "ইফেক্টের শক্তি"),
    AppLanguage.Urdu to mapOf(UiPhrase.About to "ایپ کے بارے میں", UiPhrase.AddToPlaylist to "پلے لسٹ میں شامل کریں", UiPhrase.AddToQueue to "قطار میں شامل کریں", UiPhrase.GoToAlbum to "البم پر جائیں", UiPhrase.DeleteFromLibrary to "لائبریری سے حذف کریں", UiPhrase.DeleteAlbum to "البم حذف کریں", UiPhrase.Delete to "حذف کریں", UiPhrase.Rename to "نام تبدیل کریں", UiPhrase.RemoveFromList to "فہرست سے ہٹائیں", UiPhrase.NewPlaylist to "نئی پلے لسٹ", UiPhrase.Cancel to "منسوخ", UiPhrase.Create to "بنائیں", UiPhrase.Reset to "ری سیٹ", UiPhrase.Dry to "خشک", UiPhrase.Wet to "گیلا", UiPhrase.Off to "بند", UiPhrase.Reverb to "ریورب", UiPhrase.ToneShaping to "ٹونل بیلنس", UiPhrase.Bass to "باس", UiPhrase.Midrange to "درمیانی", UiPhrase.Treble to "ٹرےبل", UiPhrase.EffectStrength to "اثر کی طاقت"),
    AppLanguage.Czech to mapOf(UiPhrase.About to "O aplikaci", UiPhrase.AddToPlaylist to "Přidat do playlistu", UiPhrase.AddToQueue to "Přidat do fronty", UiPhrase.GoToAlbum to "Přejít na album", UiPhrase.DeleteFromLibrary to "Smazat z knihovny", UiPhrase.DeleteAlbum to "Smazat album", UiPhrase.Delete to "Smazat", UiPhrase.Rename to "Přejmenovat", UiPhrase.RemoveFromList to "Odebrat ze seznamu", UiPhrase.NewPlaylist to "Nový playlist", UiPhrase.Cancel to "Zrušit", UiPhrase.Create to "Vytvořit", UiPhrase.Reset to "Resetovat", UiPhrase.Dry to "Suchý", UiPhrase.Wet to "Mokrý", UiPhrase.Off to "Vypnuto", UiPhrase.Reverb to "Dozvuk", UiPhrase.ToneShaping to "Tvarování tónu", UiPhrase.Bass to "Basy", UiPhrase.Midrange to "Středy", UiPhrase.Treble to "Výšky", UiPhrase.EffectStrength to "Síla efektu"),
    AppLanguage.Danish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Føj til playliste", UiPhrase.AddToQueue to "Føj til kø", UiPhrase.GoToAlbum to "Gå til album", UiPhrase.DeleteFromLibrary to "Slet fra bibliotek", UiPhrase.DeleteAlbum to "Slet album", UiPhrase.Delete to "Slet", UiPhrase.Rename to "Omdøb", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny playliste", UiPhrase.Cancel to "Annuller", UiPhrase.Create to "Opret", UiPhrase.Reset to "Nulstil", UiPhrase.Dry to "Tør", UiPhrase.Wet to "Våd", UiPhrase.Off to "Fra", UiPhrase.Reverb to "Rumklang", UiPhrase.ToneShaping to "Toneformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellemtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Dutch to mapOf(UiPhrase.About to "Over", UiPhrase.AddToPlaylist to "Toevoegen aan afspeellijst", UiPhrase.AddToQueue to "Toevoegen aan wachtrij", UiPhrase.GoToAlbum to "Ga naar album", UiPhrase.DeleteFromLibrary to "Verwijderen uit bibliotheek", UiPhrase.DeleteAlbum to "Album verwijderen", UiPhrase.Delete to "Verwijderen", UiPhrase.Rename to "Naam wijzigen", UiPhrase.RemoveFromList to "Uit lijst verwijderen", UiPhrase.NewPlaylist to "Nieuwe afspeellijst", UiPhrase.Cancel to "Annuleren", UiPhrase.Create to "Maken", UiPhrase.Reset to "Resetten", UiPhrase.Dry to "Droog", UiPhrase.Wet to "Nat", UiPhrase.Off to "Uit", UiPhrase.Reverb to "Galm", UiPhrase.ToneShaping to "Toonvorming", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Midden", UiPhrase.Treble to "Hoge tonen", UiPhrase.EffectStrength to "Effectsterkte"),
    AppLanguage.Estonian to mapOf(UiPhrase.About to "Teave", UiPhrase.AddToPlaylist to "Lisa esitusloendisse", UiPhrase.AddToQueue to "Lisa järjekorda", UiPhrase.GoToAlbum to "Ava album", UiPhrase.DeleteFromLibrary to "Kustuta teegist", UiPhrase.DeleteAlbum to "Kustuta album", UiPhrase.Delete to "Kustuta", UiPhrase.Rename to "Nimeta ümber", UiPhrase.RemoveFromList to "Eemalda loendist", UiPhrase.NewPlaylist to "Uus esitusloend", UiPhrase.Cancel to "Tühista", UiPhrase.Create to "Loo", UiPhrase.Reset to "Lähtesta", UiPhrase.Dry to "Kuiv", UiPhrase.Wet to "Märg", UiPhrase.Off to "Väljas", UiPhrase.Reverb to "Kaja", UiPhrase.ToneShaping to "Tooni kujundus", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Keskvahemik", UiPhrase.Treble to "Kõrged", UiPhrase.EffectStrength to "Efekti tugevus"),
    AppLanguage.French to mapOf(UiPhrase.About to "À propos", UiPhrase.AddToPlaylist to "Ajouter à une playlist", UiPhrase.AddToQueue to "Ajouter à la file", UiPhrase.GoToAlbum to "Aller à l’album", UiPhrase.DeleteFromLibrary to "Supprimer de la bibliothèque", UiPhrase.DeleteAlbum to "Supprimer l’album", UiPhrase.Delete to "Supprimer", UiPhrase.Rename to "Renommer", UiPhrase.RemoveFromList to "Retirer de la liste", UiPhrase.NewPlaylist to "Nouvelle playlist", UiPhrase.Cancel to "Annuler", UiPhrase.Create to "Créer", UiPhrase.Reset to "Réinitialiser", UiPhrase.Dry to "Sec", UiPhrase.Wet to "Humide", UiPhrase.Off to "Désactivé", UiPhrase.Reverb to "Réverbération", UiPhrase.ToneShaping to "Modelage du son", UiPhrase.Bass to "Basses", UiPhrase.Midrange to "Médiums", UiPhrase.Treble to "Aigus", UiPhrase.EffectStrength to "Intensité de l’effet"),
    AppLanguage.German to mapOf(UiPhrase.About to "Über", UiPhrase.AddToPlaylist to "Zur Playlist hinzufügen", UiPhrase.AddToQueue to "Zur Warteschlange hinzufügen", UiPhrase.GoToAlbum to "Zum Album", UiPhrase.DeleteFromLibrary to "Aus Bibliothek löschen", UiPhrase.DeleteAlbum to "Album löschen", UiPhrase.Delete to "Löschen", UiPhrase.Rename to "Umbenennen", UiPhrase.RemoveFromList to "Aus Liste entfernen", UiPhrase.NewPlaylist to "Neue Playlist", UiPhrase.Cancel to "Abbrechen", UiPhrase.Create to "Erstellen", UiPhrase.Reset to "Zurücksetzen", UiPhrase.Dry to "Trocken", UiPhrase.Wet to "Nass", UiPhrase.Off to "Aus", UiPhrase.Reverb to "Hall", UiPhrase.ToneShaping to "Klangformung", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mitten", UiPhrase.Treble to "Höhen", UiPhrase.EffectStrength to "Effektstärke"),
    AppLanguage.Greek to mapOf(UiPhrase.About to "Σχετικά", UiPhrase.AddToPlaylist to "Προσθήκη σε playlist", UiPhrase.AddToQueue to "Προσθήκη στην ουρά", UiPhrase.GoToAlbum to "Μετάβαση στο άλμπουμ", UiPhrase.DeleteFromLibrary to "Διαγραφή από βιβλιοθήκη", UiPhrase.DeleteAlbum to "Διαγραφή άλμπουμ", UiPhrase.Delete to "Διαγραφή", UiPhrase.Rename to "Μετονομασία", UiPhrase.RemoveFromList to "Αφαίρεση από λίστα", UiPhrase.NewPlaylist to "Νέο playlist", UiPhrase.Cancel to "Άκυρο", UiPhrase.Create to "Δημιουργία", UiPhrase.Reset to "Επαναφορά", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Ανενεργό", UiPhrase.Reverb to "Αντήχηση", UiPhrase.ToneShaping to "Διαμόρφωση τόνου", UiPhrase.Bass to "Μπάσα", UiPhrase.Midrange to "Μεσαία", UiPhrase.Treble to "Πρίμα", UiPhrase.EffectStrength to "Ένταση εφέ"),
    AppLanguage.Hindi to mapOf(UiPhrase.About to "परिचय", UiPhrase.AddToPlaylist to "प्लेलिस्ट में जोड़ें", UiPhrase.AddToQueue to "कतार में जोड़ें", UiPhrase.GoToAlbum to "एल्बम पर जाएं", UiPhrase.DeleteFromLibrary to "लाइब्रेरी से हटाएं", UiPhrase.DeleteAlbum to "एल्बम हटाएं", UiPhrase.Delete to "हटाएं", UiPhrase.Rename to "नाम बदलें", UiPhrase.RemoveFromList to "सूची से हटाएं", UiPhrase.NewPlaylist to "नई प्लेलिस्ट", UiPhrase.Cancel to "रद्द करें", UiPhrase.Create to "बनाएं", UiPhrase.Reset to "रीसेट", UiPhrase.Dry to "ड्राई", UiPhrase.Wet to "वेट", UiPhrase.Off to "बंद", UiPhrase.Reverb to "रीवर्ब", UiPhrase.ToneShaping to "टोन शेपिंग", UiPhrase.Bass to "बास", UiPhrase.Midrange to "मिडरेंज", UiPhrase.Treble to "ट्रेबल", UiPhrase.EffectStrength to "प्रभाव शक्ति"),
    AppLanguage.Hungarian to mapOf(UiPhrase.About to "Névjegy", UiPhrase.AddToPlaylist to "Hozzáadás lejátszási listához", UiPhrase.AddToQueue to "Hozzáadás a sorhoz", UiPhrase.GoToAlbum to "Ugrás az albumhoz", UiPhrase.DeleteFromLibrary to "Törlés a könyvtárból", UiPhrase.DeleteAlbum to "Album törlése", UiPhrase.Delete to "Törlés", UiPhrase.Rename to "Átnevezés", UiPhrase.RemoveFromList to "Eltávolítás a listából", UiPhrase.NewPlaylist to "Új lejátszási lista", UiPhrase.Cancel to "Mégse", UiPhrase.Create to "Létrehozás", UiPhrase.Reset to "Visszaállítás", UiPhrase.Dry to "Száraz", UiPhrase.Wet to "Nedves", UiPhrase.Off to "Ki", UiPhrase.Reverb to "Visszhang", UiPhrase.ToneShaping to "Hangformálás", UiPhrase.Bass to "Basszus", UiPhrase.Midrange to "Közép", UiPhrase.Treble to "Magas", UiPhrase.EffectStrength to "Effekt erőssége"),
    AppLanguage.Italian to mapOf(UiPhrase.About to "Informazioni", UiPhrase.AddToPlaylist to "Aggiungi alla playlist", UiPhrase.AddToQueue to "Aggiungi alla coda", UiPhrase.GoToAlbum to "Vai all'album", UiPhrase.DeleteFromLibrary to "Elimina dalla libreria", UiPhrase.DeleteAlbum to "Elimina album", UiPhrase.Delete to "Elimina", UiPhrase.Rename to "Rinomina", UiPhrase.RemoveFromList to "Rimuovi dalla lista", UiPhrase.NewPlaylist to "Nuova playlist", UiPhrase.Cancel to "Annulla", UiPhrase.Create to "Crea", UiPhrase.Reset to "Ripristina", UiPhrase.Dry to "Dry", UiPhrase.Wet to "Wet", UiPhrase.Off to "Disattivato", UiPhrase.Reverb to "Riverbero", UiPhrase.ToneShaping to "Modellazione tono", UiPhrase.Bass to "Bassi", UiPhrase.Midrange to "Medi", UiPhrase.Treble to "Alti", UiPhrase.EffectStrength to "Intensità effetto"),
    AppLanguage.Japanese to mapOf(UiPhrase.About to "情報", UiPhrase.AddToPlaylist to "プレイリストに追加", UiPhrase.AddToQueue to "キューに追加", UiPhrase.GoToAlbum to "アルバムへ移動", UiPhrase.DeleteFromLibrary to "ライブラリから削除", UiPhrase.DeleteAlbum to "アルバムを削除", UiPhrase.Delete to "削除", UiPhrase.Rename to "名前を変更", UiPhrase.RemoveFromList to "リストから削除", UiPhrase.NewPlaylist to "新しいプレイリスト", UiPhrase.Cancel to "キャンセル", UiPhrase.Create to "作成", UiPhrase.Reset to "リセット", UiPhrase.Dry to "ドライ", UiPhrase.Wet to "ウェット", UiPhrase.Off to "オフ", UiPhrase.Reverb to "リバーブ", UiPhrase.ToneShaping to "音色調整", UiPhrase.Bass to "低音", UiPhrase.Midrange to "中域", UiPhrase.Treble to "高音", UiPhrase.EffectStrength to "エフェクト強度"),
    AppLanguage.Latin to mapOf(UiPhrase.About to "De app", UiPhrase.AddToPlaylist to "Ad indicem adde", UiPhrase.AddToQueue to "Ad ordinem adde", UiPhrase.GoToAlbum to "I ad album", UiPhrase.DeleteFromLibrary to "E bibliotheca dele", UiPhrase.DeleteAlbum to "Album dele", UiPhrase.Delete to "Dele", UiPhrase.Rename to "Renomina", UiPhrase.RemoveFromList to "E indice remove", UiPhrase.NewPlaylist to "Novus index", UiPhrase.Cancel to "Rescinde", UiPhrase.Create to "Crea", UiPhrase.Reset to "Restitue", UiPhrase.Dry to "Siccus", UiPhrase.Wet to "Humidus", UiPhrase.Off to "Exstinctum", UiPhrase.Reverb to "Reverberatio", UiPhrase.ToneShaping to "Formatio toni", UiPhrase.Bass to "Bassus", UiPhrase.Midrange to "Media", UiPhrase.Treble to "Acuti", UiPhrase.EffectStrength to "Vis effectus"),
    AppLanguage.Latvian to mapOf(UiPhrase.About to "Par", UiPhrase.AddToPlaylist to "Pievienot atskaņošanas sarakstam", UiPhrase.AddToQueue to "Pievienot rindai", UiPhrase.GoToAlbum to "Atvērt albumu", UiPhrase.DeleteFromLibrary to "Dzēst no bibliotēkas", UiPhrase.DeleteAlbum to "Dzēst albumu", UiPhrase.Delete to "Dzēst", UiPhrase.Rename to "Pārdēvēt", UiPhrase.RemoveFromList to "Noņemt no saraksta", UiPhrase.NewPlaylist to "Jauns saraksts", UiPhrase.Cancel to "Atcelt", UiPhrase.Create to "Izveidot", UiPhrase.Reset to "Atiestatīt", UiPhrase.Dry to "Sauss", UiPhrase.Wet to "Mitrs", UiPhrase.Off to "Izslēgts", UiPhrase.Reverb to "Atbalss", UiPhrase.ToneShaping to "Toņa veidošana", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Vidējās", UiPhrase.Treble to "Augšas", UiPhrase.EffectStrength to "Efekta stiprums"),
    AppLanguage.Lithuanian to mapOf(UiPhrase.About to "Apie", UiPhrase.AddToPlaylist to "Pridėti į grojaraštį", UiPhrase.AddToQueue to "Pridėti į eilę", UiPhrase.GoToAlbum to "Eiti į albumą", UiPhrase.DeleteFromLibrary to "Ištrinti iš bibliotekos", UiPhrase.DeleteAlbum to "Ištrinti albumą", UiPhrase.Delete to "Ištrinti", UiPhrase.Rename to "Pervadinti", UiPhrase.RemoveFromList to "Pašalinti iš sąrašo", UiPhrase.NewPlaylist to "Naujas grojaraštis", UiPhrase.Cancel to "Atšaukti", UiPhrase.Create to "Sukurti", UiPhrase.Reset to "Atstatyti", UiPhrase.Dry to "Sausas", UiPhrase.Wet to "Šlapias", UiPhrase.Off to "Išjungta", UiPhrase.Reverb to "Aidas", UiPhrase.ToneShaping to "Tono formavimas", UiPhrase.Bass to "Bosai", UiPhrase.Midrange to "Viduriai", UiPhrase.Treble to "Aukšti", UiPhrase.EffectStrength to "Efekto stiprumas"),
    AppLanguage.Macedonian to mapOf(UiPhrase.About to "За апликацијата", UiPhrase.AddToPlaylist to "Додај во плејлиста", UiPhrase.AddToQueue to "Додај во редица", UiPhrase.GoToAlbum to "Оди на албум", UiPhrase.DeleteFromLibrary to "Избриши од библиотека", UiPhrase.DeleteAlbum to "Избриши албум", UiPhrase.Delete to "Избриши", UiPhrase.Rename to "Преименувај", UiPhrase.RemoveFromList to "Отстрани од листа", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетирај", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Влажно", UiPhrase.Off to "Исклучено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликување тон", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средни", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Сила на ефект"),
    AppLanguage.Norwegian to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Legg til i spilleliste", UiPhrase.AddToQueue to "Legg til i kø", UiPhrase.GoToAlbum to "Gå til album", UiPhrase.DeleteFromLibrary to "Slett fra bibliotek", UiPhrase.DeleteAlbum to "Slett album", UiPhrase.Delete to "Slett", UiPhrase.Rename to "Gi nytt navn", UiPhrase.RemoveFromList to "Fjern fra liste", UiPhrase.NewPlaylist to "Ny spilleliste", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Opprett", UiPhrase.Reset to "Tilbakestill", UiPhrase.Dry to "Tørr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Romklang", UiPhrase.ToneShaping to "Toneforming", UiPhrase.Bass to "Bass", UiPhrase.Midrange to "Mellomtone", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrke"),
    AppLanguage.Portuguese to mapOf(UiPhrase.About to "Sobre", UiPhrase.AddToPlaylist to "Adicionar à playlist", UiPhrase.AddToQueue to "Adicionar à fila", UiPhrase.GoToAlbum to "Ir para o álbum", UiPhrase.DeleteFromLibrary to "Eliminar da biblioteca", UiPhrase.DeleteAlbum to "Eliminar álbum", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renomear", UiPhrase.RemoveFromList to "Remover da lista", UiPhrase.NewPlaylist to "Nova playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Criar", UiPhrase.Reset to "Repor", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Molhado", UiPhrase.Off to "Desligado", UiPhrase.Reverb to "Reverberação", UiPhrase.ToneShaping to "Modelação de tom", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Médios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Força do efeito"),
    AppLanguage.Russian to mapOf(UiPhrase.About to "О приложении", UiPhrase.AddToPlaylist to "Добавить в плейлист", UiPhrase.AddToQueue to "Добавить в очередь", UiPhrase.GoToAlbum to "Перейти к альбому", UiPhrase.DeleteFromLibrary to "Удалить из библиотеки", UiPhrase.DeleteAlbum to "Удалить альбом", UiPhrase.Delete to "Удалить", UiPhrase.Rename to "Переименовать", UiPhrase.RemoveFromList to "Убрать из списка", UiPhrase.NewPlaylist to "Новый плейлист", UiPhrase.Cancel to "Отмена", UiPhrase.Create to "Создать", UiPhrase.Reset to "Сбросить", UiPhrase.Dry to "Сухой", UiPhrase.Wet to "Мокрый", UiPhrase.Off to "Выкл.", UiPhrase.Reverb to "Реверберация", UiPhrase.ToneShaping to "Формирование тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верх", UiPhrase.EffectStrength to "Сила эффекта"),
    AppLanguage.Serbian to mapOf(UiPhrase.About to "О апликацији", UiPhrase.AddToPlaylist to "Додај у плејлисту", UiPhrase.AddToQueue to "Додај у ред", UiPhrase.GoToAlbum to "Иди на албум", UiPhrase.DeleteFromLibrary to "Обриши из библиотеке", UiPhrase.DeleteAlbum to "Обриши албум", UiPhrase.Delete to "Обриши", UiPhrase.Rename to "Преименуј", UiPhrase.RemoveFromList to "Уклони са листе", UiPhrase.NewPlaylist to "Нова плејлиста", UiPhrase.Cancel to "Откажи", UiPhrase.Create to "Креирај", UiPhrase.Reset to "Ресетуј", UiPhrase.Dry to "Суво", UiPhrase.Wet to "Мокро", UiPhrase.Off to "Искључено", UiPhrase.Reverb to "Реверб", UiPhrase.ToneShaping to "Обликовање тона", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Средњи", UiPhrase.Treble to "Високи", UiPhrase.EffectStrength to "Јачина ефекта"),
    AppLanguage.Spanish to mapOf(UiPhrase.About to "Acerca de", UiPhrase.AddToPlaylist to "Añadir a playlist", UiPhrase.AddToQueue to "Añadir a la cola", UiPhrase.GoToAlbum to "Ir al álbum", UiPhrase.DeleteFromLibrary to "Eliminar de la biblioteca", UiPhrase.DeleteAlbum to "Eliminar álbum", UiPhrase.Delete to "Eliminar", UiPhrase.Rename to "Renombrar", UiPhrase.RemoveFromList to "Quitar de la lista", UiPhrase.NewPlaylist to "Nueva playlist", UiPhrase.Cancel to "Cancelar", UiPhrase.Create to "Crear", UiPhrase.Reset to "Restablecer", UiPhrase.Dry to "Seco", UiPhrase.Wet to "Húmedo", UiPhrase.Off to "Desactivado", UiPhrase.Reverb to "Reverberación", UiPhrase.ToneShaping to "Modelado de tono", UiPhrase.Bass to "Graves", UiPhrase.Midrange to "Medios", UiPhrase.Treble to "Agudos", UiPhrase.EffectStrength to "Intensidad del efecto"),
    AppLanguage.Swedish to mapOf(UiPhrase.About to "Om", UiPhrase.AddToPlaylist to "Lägg till i spellista", UiPhrase.AddToQueue to "Lägg till i kö", UiPhrase.GoToAlbum to "Gå till album", UiPhrase.DeleteFromLibrary to "Ta bort från bibliotek", UiPhrase.DeleteAlbum to "Ta bort album", UiPhrase.Delete to "Ta bort", UiPhrase.Rename to "Byt namn", UiPhrase.RemoveFromList to "Ta bort från lista", UiPhrase.NewPlaylist to "Ny spellista", UiPhrase.Cancel to "Avbryt", UiPhrase.Create to "Skapa", UiPhrase.Reset to "Återställ", UiPhrase.Dry to "Torr", UiPhrase.Wet to "Våt", UiPhrase.Off to "Av", UiPhrase.Reverb to "Efterklang", UiPhrase.ToneShaping to "Tonformning", UiPhrase.Bass to "Bas", UiPhrase.Midrange to "Mellanregister", UiPhrase.Treble to "Diskant", UiPhrase.EffectStrength to "Effektstyrka"),
    AppLanguage.Thai to mapOf(UiPhrase.About to "เกี่ยวกับ", UiPhrase.AddToPlaylist to "เพิ่มไปยังเพลย์ลิสต์", UiPhrase.AddToQueue to "เพิ่มไปยังคิว", UiPhrase.GoToAlbum to "ไปที่อัลบั้ม", UiPhrase.DeleteFromLibrary to "ลบจากคลัง", UiPhrase.DeleteAlbum to "ลบอัลบั้ม", UiPhrase.Delete to "ลบ", UiPhrase.Rename to "เปลี่ยนชื่อ", UiPhrase.RemoveFromList to "ลบออกจากรายการ", UiPhrase.NewPlaylist to "เพลย์ลิสต์ใหม่", UiPhrase.Cancel to "ยกเลิก", UiPhrase.Create to "สร้าง", UiPhrase.Reset to "รีเซ็ต", UiPhrase.Dry to "แห้ง", UiPhrase.Wet to "เปียก", UiPhrase.Off to "ปิด", UiPhrase.Reverb to "รีเวิร์บ", UiPhrase.ToneShaping to "ปรับโทนเสียง", UiPhrase.Bass to "เบส", UiPhrase.Midrange to "เสียงกลาง", UiPhrase.Treble to "เสียงแหลม", UiPhrase.EffectStrength to "ความแรงของเอฟเฟกต์"),
    AppLanguage.Ukrainian to mapOf(UiPhrase.About to "Про застосунок", UiPhrase.AddToPlaylist to "Додати до плейлиста", UiPhrase.AddToQueue to "Додати до черги", UiPhrase.GoToAlbum to "Перейти до альбому", UiPhrase.DeleteFromLibrary to "Видалити з бібліотеки", UiPhrase.DeleteAlbum to "Видалити альбом", UiPhrase.Delete to "Видалити", UiPhrase.Rename to "Перейменувати", UiPhrase.RemoveFromList to "Прибрати зі списку", UiPhrase.NewPlaylist to "Новий плейлист", UiPhrase.Cancel to "Скасувати", UiPhrase.Create to "Створити", UiPhrase.Reset to "Скинути", UiPhrase.Dry to "Сухий", UiPhrase.Wet to "Мокрий", UiPhrase.Off to "Вимкнено", UiPhrase.Reverb to "Реверберація", UiPhrase.ToneShaping to "Формування тону", UiPhrase.Bass to "Бас", UiPhrase.Midrange to "Середина", UiPhrase.Treble to "Верхи", UiPhrase.EffectStrength to "Сила ефекту"),
)

internal fun SpaciousnessMode.displayLabel(language: AppLanguage = AppLanguage.English): String {
    return when (this) {
        SpaciousnessMode.Off -> uiPhrase(language, UiPhrase.Off)
        SpaciousnessMode.StereoWidth -> when (language) {
            AppLanguage.Albanian -> "Gjerësi stereo"
            AppLanguage.Polish -> "Szerokość stereo"
            AppLanguage.Hindi -> "स्टीरियो चौड़ाई"
            AppLanguage.Hungarian -> "Sztereó szélesség"
            AppLanguage.German -> "Stereo-Breite"
            AppLanguage.French -> "Largeur stéréo"
            AppLanguage.Spanish -> "Amplitud estéreo"
            AppLanguage.Italian -> "Ampiezza stereo"
            AppLanguage.Latin -> "Latitudo stereo"
            AppLanguage.Portuguese -> "Largura estéreo"
            AppLanguage.Dutch -> "Stereo-breedte"
            AppLanguage.Swedish -> "Stereobredd"
            AppLanguage.Norwegian -> "Stereobredde"
            AppLanguage.Danish -> "Stereobredde"
            AppLanguage.Czech -> "Šířka sterea"
            AppLanguage.Croatian -> "Stereo širina"
            AppLanguage.Lithuanian -> "Stereo plotis"
            AppLanguage.Latvian -> "Stereo platums"
            AppLanguage.Estonian -> "Stereo laius"
            AppLanguage.Greek -> "Πλάτος stereo"
            AppLanguage.Macedonian -> "Стерео ширина"
            AppLanguage.Russian -> "Ширина стерео"
            AppLanguage.Serbian -> "Ширина стереа"
            AppLanguage.Thai -> "ความกว้างสเตอริโอ"
            AppLanguage.Ukrainian -> "Ширина стерео"
            AppLanguage.Slovak -> "Stereo šírka"
            AppLanguage.Korean -> "스테레오 폭"
            AppLanguage.Malay -> "Lebar stereo"
            AppLanguage.Bengali -> "স্টেরিও প্রস্থ"
            AppLanguage.Urdu -> "اسٹیریو چوڑائی"
            AppLanguage.ChineseSimplified -> "立体声宽度"
            AppLanguage.Japanese -> "ステレオ幅"
            else -> "Stereo Width"
        }
        SpaciousnessMode.CrossfeedDepth -> when (language) {
            AppLanguage.Albanian -> "Përzierje kanalesh"
            AppLanguage.Polish -> "Przenikanie kanałów"
            AppLanguage.Hindi -> "क्रॉसफीड"
            AppLanguage.Hungarian -> "Crossfeed"
            AppLanguage.German -> "Crossfeed"
            AppLanguage.French -> "Crossfeed"
            AppLanguage.Spanish -> "Crossfeed"
            AppLanguage.Italian -> "Crossfeed"
            AppLanguage.Latin -> "Canales mixti"
            AppLanguage.Portuguese -> "Crossfeed"
            AppLanguage.Dutch -> "Crossfeed"
            AppLanguage.Swedish -> "Crossfeed"
            AppLanguage.Norwegian -> "Crossfeed"
            AppLanguage.Danish -> "Crossfeed"
            AppLanguage.Czech -> "Crossfeed"
            AppLanguage.Croatian -> "Crossfeed"
            AppLanguage.Lithuanian -> "Kanalų susiliejimas"
            AppLanguage.Latvian -> "Kanālu sajaukums"
            AppLanguage.Estonian -> "Kanalite segamine"
            AppLanguage.Greek -> "Crossfeed"
            AppLanguage.Macedonian -> "Вкрстено мешање"
            AppLanguage.Russian -> "Кроссфид"
            AppLanguage.Serbian -> "Кросфид"
            AppLanguage.Thai -> "ครอสฟีด"
            AppLanguage.Ukrainian -> "Кросфід"
            AppLanguage.Slovak -> "Crossfeed"
            AppLanguage.Korean -> "크로스피드"
            AppLanguage.Malay -> "Crossfeed"
            AppLanguage.Bengali -> "ক্রসফিড"
            AppLanguage.Urdu -> "کراس فیڈ"
            AppLanguage.ChineseSimplified -> "交叉馈送"
            AppLanguage.Japanese -> "クロスフィード"
            else -> "Crossfeed"
        }
        SpaciousnessMode.EarlyReflectionRoom -> when (language) {
            AppLanguage.Albanian -> "Dhomë"
            AppLanguage.Polish -> "Pokój"
            AppLanguage.Hindi -> "कमरा"
            AppLanguage.Hungarian -> "Szoba"
            AppLanguage.German -> "Raum"
            AppLanguage.French -> "Pièce"
            AppLanguage.Spanish -> "Sala"
            AppLanguage.Italian -> "Stanza"
            AppLanguage.Latin -> "Camera"
            AppLanguage.Portuguese -> "Sala"
            AppLanguage.Dutch -> "Ruimte"
            AppLanguage.Swedish -> "Rum"
            AppLanguage.Norwegian -> "Rom"
            AppLanguage.Danish -> "Rum"
            AppLanguage.Czech -> "Místnost"
            AppLanguage.Croatian -> "Soba"
            AppLanguage.Lithuanian -> "Kambarys"
            AppLanguage.Latvian -> "Istaba"
            AppLanguage.Estonian -> "Tuba"
            AppLanguage.Greek -> "Δωμάτιο"
            AppLanguage.Macedonian -> "Соба"
            AppLanguage.Russian -> "Комната"
            AppLanguage.Serbian -> "Соба"
            AppLanguage.Thai -> "ห้อง"
            AppLanguage.Ukrainian -> "Кімната"
            AppLanguage.Slovak -> "Miestnosť"
            AppLanguage.Korean -> "룸"
            AppLanguage.Malay -> "Bilik"
            AppLanguage.Bengali -> "রুম"
            AppLanguage.Urdu -> "کمرہ"
            AppLanguage.ChineseSimplified -> "房间"
            AppLanguage.Japanese -> "ルーム"
            else -> "Room"
        }
        SpaciousnessMode.Philharmony -> when (language) {
            AppLanguage.Albanian -> "Filarmonia"
            AppLanguage.Polish -> "Filharmonia"
            AppLanguage.Hindi -> "फिलहार्मनी"
            AppLanguage.Hungarian -> "Filharmónia"
            AppLanguage.German -> "Philharmonie"
            AppLanguage.French -> "Philharmonie"
            AppLanguage.Spanish -> "Filarmónica"
            AppLanguage.Italian -> "Filarmonica"
            AppLanguage.Latin -> "Philharmonia"
            AppLanguage.Portuguese -> "Filarmônica"
            AppLanguage.Dutch -> "Filharmonie"
            AppLanguage.Swedish -> "Filharmoni"
            AppLanguage.Norwegian -> "Filharmoni"
            AppLanguage.Danish -> "Filharmoni"
            AppLanguage.Czech -> "Filharmonie"
            AppLanguage.Croatian -> "Filharmonija"
            AppLanguage.Lithuanian -> "Filharmonija"
            AppLanguage.Latvian -> "Filharmonija"
            AppLanguage.Estonian -> "Filharmoonia"
            AppLanguage.Greek -> "Φιλαρμονική"
            AppLanguage.Macedonian -> "Филхармонија"
            AppLanguage.Russian -> "Филармония"
            AppLanguage.Serbian -> "Филхармонија"
            AppLanguage.Thai -> "ฟิลฮาร์โมนี"
            AppLanguage.Ukrainian -> "Філармонія"
            AppLanguage.Slovak -> "Filharmónia"
            AppLanguage.Korean -> "필하모니"
            AppLanguage.Malay -> "Filharmoni"
            AppLanguage.Bengali -> "ফিলহারমনি"
            AppLanguage.Urdu -> "فلہارمونی"
            AppLanguage.ChineseSimplified -> "爱乐厅"
            AppLanguage.Japanese -> "フィルハーモニー"
            else -> "Philharmony"
        }
        SpaciousnessMode.HaasSpace -> when (language) {
            AppLanguage.Albanian -> "Hapësira Haas"
            AppLanguage.Polish -> "Przestrzeń Haasa"
            AppLanguage.Hindi -> "हास स्पेस"
            AppLanguage.Hungarian -> "Haas tér"
            AppLanguage.German -> "Haas-Raum"
            AppLanguage.French -> "Espace Haas"
            AppLanguage.Spanish -> "Espacio Haas"
            AppLanguage.Italian -> "Spazio Haas"
            AppLanguage.Latin -> "Spatium Haas"
            AppLanguage.Portuguese -> "Espaço Haas"
            AppLanguage.Dutch -> "Haas-ruimte"
            AppLanguage.Swedish -> "Haas-rymd"
            AppLanguage.Norwegian -> "Haas-rom"
            AppLanguage.Danish -> "Haas-rum"
            AppLanguage.Czech -> "Haasův prostor"
            AppLanguage.Croatian -> "Haas prostor"
            AppLanguage.Lithuanian -> "Haas erdvė"
            AppLanguage.Latvian -> "Haas telpa"
            AppLanguage.Estonian -> "Haas ruum"
            AppLanguage.Greek -> "Χώρος Haas"
            AppLanguage.Macedonian -> "Haas простор"
            AppLanguage.Russian -> "Пространство Хааса"
            AppLanguage.Serbian -> "Haas простор"
            AppLanguage.Thai -> "พื้นที่ Haas"
            AppLanguage.Ukrainian -> "Простір Хааса"
            AppLanguage.Slovak -> "Haas priestor"
            AppLanguage.Korean -> "하스 공간"
            AppLanguage.Malay -> "Ruang Haas"
            AppLanguage.Bengali -> "হাস স্পেস"
            AppLanguage.Urdu -> "ہاس اسپیس"
            AppLanguage.ChineseSimplified -> "Haas 空间"
            AppLanguage.Japanese -> "ハース空間"
            else -> "Haas Space"
        }
        SpaciousnessMode.HarmonicAir -> when (language) {
            AppLanguage.Albanian -> "Ajër harmonik"
            AppLanguage.Polish -> "Harmoniczne powietrze"
            AppLanguage.Hindi -> "हार्मोनिक एयर"
            AppLanguage.Hungarian -> "Harmonikus levegő"
            AppLanguage.German -> "Harmonische Luft"
            AppLanguage.French -> "Air harmonique"
            AppLanguage.Spanish -> "Aire armónico"
            AppLanguage.Italian -> "Aria armonica"
            AppLanguage.Latin -> "Aer harmonicus"
            AppLanguage.Portuguese -> "Ar harmônico"
            AppLanguage.Dutch -> "Harmonische lucht"
            AppLanguage.Swedish -> "Harmonisk luft"
            AppLanguage.Norwegian -> "Harmonisk luft"
            AppLanguage.Danish -> "Harmonisk luft"
            AppLanguage.Czech -> "Harmonický vzduch"
            AppLanguage.Croatian -> "Harmonični zrak"
            AppLanguage.Lithuanian -> "Harmoningas oras"
            AppLanguage.Latvian -> "Harmonisks gaiss"
            AppLanguage.Estonian -> "Harmooniline õhk"
            AppLanguage.Greek -> "Αρμονικός αέρας"
            AppLanguage.Macedonian -> "Хармоничен воздух"
            AppLanguage.Russian -> "Гармонический воздух"
            AppLanguage.Serbian -> "Хармонични ваздух"
            AppLanguage.Thai -> "อากาศฮาร์มอนิก"
            AppLanguage.Ukrainian -> "Гармонійне повітря"
            AppLanguage.Slovak -> "Harmonický vzduch"
            AppLanguage.Korean -> "하모닉 에어"
            AppLanguage.Malay -> "Udara harmonik"
            AppLanguage.Bengali -> "হারমনিক এয়ার"
            AppLanguage.Urdu -> "ہارمونک ایئر"
            AppLanguage.ChineseSimplified -> "和声音场"
            AppLanguage.Japanese -> "ハーモニックエア"
            else -> "Harmonic Air"
        }
    }
}
