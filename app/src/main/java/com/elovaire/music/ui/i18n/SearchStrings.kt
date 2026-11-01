package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistSortField
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.data.playback.PlaybackRepeatMode
import elovaire.music.droidbeauty.app.ui.screens.SearchSongSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode


internal data class SearchUiCopy(
    val placeholder: String,
    val clearSearch: String,
    val nothingSearchedTitle: String,
    val nothingSearchedMessage: String,
    val suggestedAlbumsTitle: String,
    val suggestedAlbumsSubtitle: String,
    val recentlySearched: String,
    val clearHistory: String,
    val noResultsTitle: String,
    val noResultsPrefix: String,
    val noResultsSuffix: String,
    val matchingArtistsSuffix: String,
    val matchingAlbumsSuffix: String,
    val matchingSongsSuffix: String,
) {
    fun noResultsMessage(query: String): String = "$noResultsPrefix \"$query\" $noResultsSuffix".trimEnd()
    fun matchingArtists(count: Int, language: AppLanguage): String = if (language == AppLanguage.Polish) {
        val adjective = if (count == 1) "pasujący" else "pasujących"
        "$count $adjective ${localizedCountLabel(count, "artist", language).substringAfter(' ')}"
    } else if (language == AppLanguage.English && count == 1) {
        "1 matching artist"
    } else "$count $matchingArtistsSuffix"

    fun matchingAlbums(count: Int, language: AppLanguage): String = matchingItems(count, "album", matchingAlbumsSuffix, language)
    fun matchingSongs(count: Int, language: AppLanguage): String = matchingItems(count, "song", matchingSongsSuffix, language)

    private fun matchingItems(count: Int, noun: String, suffix: String, language: AppLanguage): String {
        if (language == AppLanguage.English && count == 1) return "1 matching $noun result"
        if (language != AppLanguage.Polish) return "$count $suffix"
        val adjective = when {
            count == 1 -> "pasujący"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "pasujące"
            else -> "pasujących"
        }
        return "$count $adjective ${localizedCountLabel(count, noun, language).substringAfter(' ')}"
    }
}

internal fun searchCopy(language: AppLanguage): SearchUiCopy = when (language) {
    AppLanguage.Hindi -> SearchUiCopy("कलाकार, एल्बम और बहुत कुछ", "खोज साफ़ करें", "अभी तक कुछ नहीं खोजा", "गाने और एल्बम खोजने पर नतीजे यहाँ दिखेंगे", "सुझाए गए एल्बम", "शायद आप इन्हें फिर सुनना चाहें", "हाल की खोजें", "इतिहास साफ़ करें", "कोई नतीजा नहीं", "लाइब्रेरी में इसके लिए कोई नतीजा नहीं मिला", "", "मिलते कलाकार", "मिलते एल्बम", "मिलते गाने")
    AppLanguage.Latin -> SearchUiCopy("Artifices, alba et alia", "Quaestionem dele", "Nihil adhuc quaesitum", "Eventus hic apparebunt cum cantus et alba quaeres", "Alba proposita", "Fortasse ea iterum audire velis", "Quaestiones recentes", "Historiam dele", "Nulli eventus", "Nihil in bibliotheca respondet", "", "artifices inventi", "alba inventa", "cantus inventi")
    AppLanguage.Latvian -> SearchUiCopy("Izpildītāji, albumi un cits", "Notīrīt meklēšanu", "Vēl nekas nav meklēts", "Meklējot dziesmas un albumus, rezultāti parādīsies šeit", "Ieteiktie albumi", "Varbūt vēlies tos noklausīties vēlreiz", "Nesen meklētais", "Notīrīt vēsturi", "Nav rezultātu", "Bibliotēkā nav rezultātu vaicājumam", "", "atbilstoši izpildītāji", "atbilstoši albumi", "atbilstošas dziesmas")
    AppLanguage.Lithuanian -> SearchUiCopy("Atlikėjai, albumai ir daugiau", "Išvalyti paiešką", "Dar nieko neieškota", "Ieškant dainų ir albumų rezultatai bus rodomi čia", "Siūlomi albumai", "Galbūt norėsite jų paklausyti dar kartą", "Naujausios paieškos", "Išvalyti istoriją", "Rezultatų nėra", "Bibliotekoje nerasta rezultatų pagal", "", "atitinkančių atlikėjų", "atitinkančių albumų", "atitinkančių dainų")
    AppLanguage.Thai -> SearchUiCopy("ศิลปิน อัลบั้ม และอื่น ๆ", "ล้างการค้นหา", "ยังไม่มีการค้นหา", "ผลลัพธ์จะแสดงที่นี่เมื่อค้นหาเพลงและอัลบั้ม", "อัลบั้มแนะนำ", "คุณอาจอยากกลับไปฟังอีกครั้ง", "การค้นหาล่าสุด", "ล้างประวัติ", "ไม่พบผลลัพธ์", "ไม่พบผลลัพธ์ในคลังสำหรับ", "", "ศิลปินที่ตรงกัน", "อัลบั้มที่ตรงกัน", "เพลงที่ตรงกัน")
    AppLanguage.Dutch -> SearchUiCopy("Artiesten, albums en meer", "Zoekopdracht wissen", "Nog niets gezocht", "Resultaten verschijnen hier wanneer je naar nummers en albums zoekt", "Voorgestelde albums", "Misschien wil je deze opnieuw beluisteren", "Recente zoekopdrachten", "Geschiedenis wissen", "Geen resultaten", "Geen resultaten in de bibliotheek voor", "", "overeenkomende artiesten", "overeenkomende albums", "overeenkomende nummers")
    AppLanguage.Danish -> SearchUiCopy("Kunstnere, album og mere", "Ryd søgning", "Ingen søgninger endnu", "Resultater vises her, når du søger efter sange og album", "Foreslåede album", "Måske vil du lytte til dem igen", "Seneste søgninger", "Ryd historik", "Ingen resultater", "Ingen resultater i biblioteket for", "", "matchende kunstnere", "matchende album", "matchende sange")
    AppLanguage.Swedish -> SearchUiCopy("Artister, album och mer", "Rensa sökning", "Inga sökningar än", "Resultat visas här när du söker efter låtar och album", "Föreslagna album", "Du kanske vill lyssna på dem igen", "Senaste sökningarna", "Rensa historik", "Inga resultat", "Inga resultat i biblioteket för", "", "matchande artister", "matchande album", "matchande låtar")
    AppLanguage.Norwegian -> SearchUiCopy("Artister, album og mer", "Tøm søk", "Ingen søk ennå", "Resultater vises her når du søker etter sanger og album", "Foreslåtte album", "Kanskje du vil høre på disse igjen", "Nylige søk", "Tøm historikk", "Ingen resultater", "Ingen resultater i biblioteket for", "", "treff på artister", "treff på album", "treff på sanger")
    AppLanguage.Croatian -> SearchUiCopy("Izvođači, albumi i više", "Očisti pretragu", "Još nema pretraga", "Rezultati će se pojaviti kada tražite pjesme i albume", "Predloženi albumi", "Možda ih poželite ponovno poslušati", "Nedavne pretrage", "Očisti povijest", "Nema rezultata", "Nema rezultata u biblioteci za", "", "pronađenih izvođača", "pronađenih albuma", "pronađenih pjesama")
    AppLanguage.Estonian -> SearchUiCopy("Esitajad, albumid ja muu", "Tühjenda otsing", "Otsinguid veel pole", "Lugude ja albumite otsimisel ilmuvad tulemused siia", "Soovitatud albumid", "Võib-olla soovid neid uuesti kuulata", "Viimased otsingud", "Tühjenda ajalugu", "Tulemusi pole", "Teegist ei leitud vasteid päringule", "", "sobivat esitajat", "sobivat albumit", "sobivat lugu")
    AppLanguage.Hungarian -> SearchUiCopy("Előadók, albumok és mások", "Keresés törlése", "Még nem kerestél semmit", "A dalok és albumok keresésekor itt jelennek meg a találatok", "Ajánlott albumok", "Talán szívesen meghallgatnád őket újra", "Legutóbbi keresések", "Előzmények törlése", "Nincs találat", "A könyvtárban nincs találat erre:", "", "egyező előadó", "egyező album", "egyező dal")
    AppLanguage.Greek -> SearchUiCopy("Καλλιτέχνες, άλμπουμ και άλλα", "Εκκαθάριση αναζήτησης", "Δεν έχετε κάνει αναζήτηση ακόμα", "Τα αποτελέσματα θα εμφανίζονται εδώ όταν αναζητάτε τραγούδια και άλμπουμ", "Προτεινόμενα άλμπουμ", "Ίσως θέλετε να τα ακούσετε ξανά", "Πρόσφατες αναζητήσεις", "Εκκαθάριση ιστορικού", "Κανένα αποτέλεσμα", "Δεν βρέθηκαν αποτελέσματα στη βιβλιοθήκη για", "", "καλλιτέχνες που βρέθηκαν", "άλμπουμ που βρέθηκαν", "τραγούδια που βρέθηκαν")
    AppLanguage.Macedonian -> SearchUiCopy("Изведувачи, албуми и повеќе", "Исчисти пребарување", "Сè уште нема пребарувања", "Резултатите ќе се појават тука кога ќе пребарувате песни и албуми", "Предложени албуми", "Можеби ќе сакате повторно да ги слушнете", "Неодамнешни пребарувања", "Исчисти историја", "Нема резултати", "Нема резултати во библиотеката за", "", "пронајдени изведувачи", "пронајдени албуми", "пронајдени песни")
    AppLanguage.Bulgarian -> SearchUiCopy("Изпълнители, албуми и още", "Изчисти търсенето", "Все още няма търсения", "Резултатите ще се появят при търсене на песни и албуми", "Предложени албуми", "Може да искате да ги чуете отново", "Последни търсения", "Изчисти историята", "Няма резултати", "Няма резултати в библиотеката за", "", "намерени изпълнители", "намерени албуми", "намерени песни")
    AppLanguage.Catalan -> SearchUiCopy("Artistes, àlbums i més", "Esborra la cerca", "Encara no has cercat res", "Els resultats apareixeran quan cerquis cançons i àlbums", "Àlbums suggerits", "Potser els vols tornar a escoltar", "Cerques recents", "Esborra l’historial", "Cap resultat", "No hi ha resultats a la biblioteca per a", "", "artistes trobats", "àlbums trobats", "cançons trobades")
    AppLanguage.Finnish -> SearchUiCopy("Esittäjät, albumit ja muut", "Tyhjennä haku", "Ei vielä hakuja", "Tulokset näkyvät tässä, kun haet kappaleita ja albumeita", "Suositellut albumit", "Voisit kuunnella nämä uudelleen", "Viimeisimmät haut", "Tyhjennä historia", "Ei tuloksia", "Kirjastosta ei löydy tuloksia haulle", "", "vastaavaa esittäjää", "vastaavaa albumia", "vastaavaa kappaletta")
    AppLanguage.Romanian -> SearchUiCopy("Artiști, albume și altele", "Șterge căutarea", "Nu ai căutat nimic încă", "Rezultatele vor apărea când cauți melodii și albume", "Albume sugerate", "Poate dorești să le reasculți", "Căutări recente", "Șterge istoricul", "Niciun rezultat", "Nu există rezultate în bibliotecă pentru", "", "artiști găsiți", "albume găsite", "melodii găsite")
    AppLanguage.Slovenian -> SearchUiCopy("Izvajalci, albumi in več", "Počisti iskanje", "Še ni iskanj", "Rezultati bodo prikazani med iskanjem skladb in albumov", "Predlagani albumi", "Morda jih želite znova poslušati", "Nedavna iskanja", "Počisti zgodovino", "Ni rezultatov", "V knjižnici ni rezultatov za", "", "najdenih izvajalcev", "najdenih albumov", "najdenih skladb")
    AppLanguage.Bosnian -> SearchUiCopy("Izvođači, albumi i više", "Obriši pretragu", "Još nema pretraga", "Rezultati će se pojaviti kada počnete tražiti pjesme i albume", "Predloženi albumi", "Možda ih poželite ponovo poslušati", "Nedavne pretrage", "Obriši historiju", "Nema rezultata", "U biblioteci nema rezultata za", "", "pronađenih izvođača", "pronađenih albuma", "pronađenih pjesama")
    AppLanguage.Indonesian -> SearchUiCopy("Artis, album, dan lainnya", "Hapus pencarian", "Belum ada pencarian", "Hasil akan muncul saat Anda mencari lagu dan album", "Album yang disarankan", "Mungkin Anda ingin mendengarkannya lagi", "Pencarian terbaru", "Hapus riwayat", "Tidak ada hasil", "Tidak ada hasil di pustaka untuk", "", "artis cocok", "album cocok", "lagu cocok")
    AppLanguage.Punjabi -> SearchUiCopy("ਕਲਾਕਾਰ, ਐਲਬਮਾਂ ਅਤੇ ਹੋਰ", "ਖੋਜ ਸਾਫ਼ ਕਰੋ", "ਹਾਲੇ ਕੋਈ ਖੋਜ ਨਹੀਂ", "ਗੀਤਾਂ ਅਤੇ ਐਲਬਮਾਂ ਦੀ ਖੋਜ ਕਰਨ ਉੱਤੇ ਨਤੀਜੇ ਇੱਥੇ ਦਿਖਣਗੇ", "ਸੁਝਾਈਆਂ ਐਲਬਮਾਂ", "ਤੁਸੀਂ ਇਨ੍ਹਾਂ ਨੂੰ ਦੁਬਾਰਾ ਸੁਣਨਾ ਚਾਹ ਸਕਦੇ ਹੋ", "ਹਾਲੀਆ ਖੋਜਾਂ", "ਇਤਿਹਾਸ ਸਾਫ਼ ਕਰੋ", "ਕੋਈ ਨਤੀਜਾ ਨਹੀਂ", "ਲਾਇਬ੍ਰੇਰੀ ਵਿੱਚ ਇਸ ਲਈ ਕੋਈ ਨਤੀਜਾ ਨਹੀਂ", "", "ਮਿਲਦੇ ਕਲਾਕਾਰ", "ਮਿਲਦੀਆਂ ਐਲਬਮਾਂ", "ਮਿਲਦੇ ਗੀਤ")
    AppLanguage.Albanian -> SearchUiCopy("Artistë, albume dhe më shumë", "Pastro kërkimin", "Ende asnjë kërkim", "Rezultatet do të shfaqen kur të kërkosh këngë dhe albume", "Albume të sugjeruara", "Mund të dëshirosh t’i dëgjosh përsëri", "Kërkimet e fundit", "Pastro historikun", "Nuk u gjet asnjë rezultat", "Nuk ka përputhje në bibliotekë për", "", "artistë të gjetur", "albume të gjetura", "këngë të gjetura")
    AppLanguage.Serbian -> SearchUiCopy("Извођачи, албуми и друго", "Обриши претрагу", "Још нема претрага", "Резултати ће се појавити када претражиш песме и албуме", "Предложени албуми", "Можда ћеш желети да их поново послушаш", "Недавне претраге", "Обриши историју", "Нема резултата", "Нема подударања у библиотеци за", "", "пронађених извођача", "пронађених албума", "пронађених песама")
    AppLanguage.Polish -> SearchUiCopy("Artyści, albumy i więcej", "Wyczyść wyszukiwanie", "Jeszcze nic nie wyszukano", "Więcej wyników pojawi się podczas wyszukiwania utworów i albumów", "Sugerowane albumy", "Warto do nich wrócić", "Ostatnio wyszukiwane", "Wyczyść historię", "Brak wyników", "Nic w obecnej bibliotece offline nie pasuje do", "jeszcze", "pasujących artystów", "pasujących albumów", "pasujących utworów")
    AppLanguage.ChineseSimplified -> SearchUiCopy("艺人、专辑等", "清除搜索", "还没有搜索", "搜索歌曲和专辑时会显示更多结果", "推荐专辑", "你可能会想再听听", "最近搜索", "清除历史", "没有结果", "当前离线媒体库中没有匹配", "", "个匹配艺人", "个匹配专辑", "个匹配歌曲")
    AppLanguage.Czech -> SearchUiCopy("Umělci, alba a další", "Vymazat hledání", "Zatím nic nehledáno", "Další výsledky se zobrazí při hledání skladeb a alb", "Navržená alba", "Možná se k nim chcete vrátit", "Nedávno hledané", "Vymazat historii", "Žádné výsledky", "V aktuální offline knihovně se nic neshoduje s", "zatím", "odpovídajících umělců", "odpovídajících alb", "odpovídajících skladeb")
    AppLanguage.French -> SearchUiCopy("Artistes, albums et plus", "Effacer la recherche", "Aucune recherche pour l’instant", "Plus de résultats apparaîtront pendant la recherche de morceaux et d’albums", "Albums suggérés", "Vous devriez peut-être les réécouter", "Recherches récentes", "Effacer l’historique", "Aucun résultat", "Rien dans la bibliothèque hors ligne actuelle ne correspond à", "pour l’instant", "artistes correspondants", "albums correspondants", "morceaux correspondants")
    AppLanguage.German -> SearchUiCopy("Künstler, Alben und mehr", "Suche löschen", "Noch nichts gesucht", "Weitere Ergebnisse erscheinen, wenn du nach Songs und Alben suchst", "Vorgeschlagene Alben", "Diese solltest du vielleicht wieder hören", "Zuletzt gesucht", "Verlauf löschen", "Keine Ergebnisse", "In der aktuellen Offline-Bibliothek passt nichts zu", "bisher", "passende Künstler", "passende Alben", "passende Songs")
    AppLanguage.Italian -> SearchUiCopy("Artisti, album e altro", "Cancella ricerca", "Nessuna ricerca ancora", "Altri risultati appariranno mentre cerchi brani e album", "Album suggeriti", "Potresti volerli riascoltare", "Ricerche recenti", "Cancella cronologia", "Nessun risultato", "Nella libreria offline attuale non corrisponde nulla a", "ancora", "artisti corrispondenti", "album corrispondenti", "brani corrispondenti")
    AppLanguage.Japanese -> SearchUiCopy("アーティスト、アルバムなど", "検索をクリア", "まだ検索していません", "曲やアルバムを検索すると、さらに結果が表示されます", "おすすめアルバム", "また聴きたくなるかもしれません", "最近の検索", "履歴を消去", "結果なし", "現在のオフラインライブラリに一致するものはありません:", "", "件の一致するアーティスト", "件の一致するアルバム", "件の一致する曲")
    AppLanguage.Spanish -> SearchUiCopy("Artistas, álbumes y más", "Borrar búsqueda", "Aún no has buscado nada", "Aparecerán más resultados al buscar canciones y álbumes", "Álbumes sugeridos", "Quizá quieras volver a escucharlos", "Búsquedas recientes", "Borrar historial", "Sin resultados", "Nada en la biblioteca sin conexión actual coincide con", "todavía", "artistas coincidentes", "álbumes coincidentes", "canciones coincidentes")
    AppLanguage.Portuguese -> SearchUiCopy("Artistas, álbuns e mais", "Limpar pesquisa", "Ainda nada pesquisado", "Mais resultados aparecerão ao pesquisar músicas e álbuns", "Álbuns sugeridos", "Talvez queira revisitá-los", "Pesquisas recentes", "Limpar histórico", "Sem resultados", "Nada na biblioteca offline atual corresponde a", "ainda", "artistas correspondentes", "álbuns correspondentes", "músicas correspondentes")
    AppLanguage.Russian -> SearchUiCopy("Исполнители, альбомы и другое", "Очистить поиск", "Пока ничего не искали", "Больше результатов появится при поиске песен и альбомов", "Предложенные альбомы", "Возможно, стоит вернуться к ним", "Недавние поиски", "Очистить историю", "Нет результатов", "В текущей офлайн-библиотеке ничего не найдено для", "пока", "подходящих исполнителей", "подходящих альбомов", "подходящих песен")
    AppLanguage.Ukrainian -> SearchUiCopy("Виконавці, альбоми тощо", "Очистити пошук", "Поки нічого не шукали", "Більше результатів з’явиться під час пошуку пісень і альбомів", "Запропоновані альбоми", "Можливо, варто повернутися до них", "Нещодавні пошуки", "Очистити історію", "Немає результатів", "У поточній офлайн-бібліотеці нічого не збігається з", "поки", "відповідних виконавців", "відповідних альбомів", "відповідних пісень")
    AppLanguage.Slovak -> SearchUiCopy("Interpreti, albumy a ďalšie", "Vymazať hľadanie", "Zatiaľ nič nehľadané", "Ďalšie výsledky sa zobrazia počas hľadania skladieb a albumov", "Navrhované albumy", "Možno sa k nim budete chcieť vrátiť", "Nedávno hľadané", "Vymazať históriu", "Žiadne výsledky", "V aktuálnej offline knižnici sa nič nezhoduje s", "zatiaľ", "zodpovedajúcich interpretov", "zodpovedajúcich albumov", "zodpovedajúcich skladieb")
    AppLanguage.Korean -> SearchUiCopy("아티스트, 앨범 등", "검색 지우기", "아직 검색한 내용이 없습니다", "곡과 앨범을 검색하면 더 많은 결과가 여기에 표시됩니다", "추천 앨범", "다시 들어보고 싶을지도 모릅니다", "최근 검색", "기록 지우기", "결과 없음", "현재 오프라인 라이브러리에서 다음과 일치하는 항목이 없습니다", "", "개의 일치하는 아티스트", "개의 일치하는 앨범", "개의 일치하는 곡")
    AppLanguage.Malay -> SearchUiCopy("Artis, album dan banyak lagi", "Kosongkan carian", "Belum ada carian", "Lebih banyak hasil akan muncul di sini apabila anda mencari lagu dan album", "Album disyorkan", "Anda mungkin mahu kembali mendengarnya", "Carian terkini", "Kosongkan sejarah", "Tiada hasil", "Tiada apa-apa dalam pustaka luar talian semasa yang sepadan dengan", "lagi", "artis sepadan", "album sepadan", "lagu sepadan")
    AppLanguage.Bengali -> SearchUiCopy("শিল্পী, অ্যালবাম এবং আরও", "সার্চ মুছুন", "এখনও কিছু খোঁজা হয়নি", "গান ও অ্যালবাম খুঁজলে আরও ফল এখানে দেখা যাবে", "প্রস্তাবিত অ্যালবাম", "সম্ভবত এগুলোতে আবার ফিরতে চাইবেন", "সাম্প্রতিক অনুসন্ধান", "ইতিহাস মুছুন", "কোনো ফল নেই", "বর্তমান অফলাইন লাইব্রেরিতে এর সাথে মেলে এমন কিছু নেই", "এখনও", "মিল থাকা শিল্পী", "মিল থাকা অ্যালবাম", "মিল থাকা গান")
    AppLanguage.Urdu -> SearchUiCopy("آرٹسٹس، البمز اور مزید", "تلاش صاف کریں", "ابھی تک کچھ تلاش نہیں کیا گیا", "گانے اور البمز تلاش کرتے وقت مزید نتائج یہاں دکھائی دیں گے", "تجویز کردہ البمز", "شاید آپ دوبارہ انہیں سننا چاہیں", "حالیہ تلاشیں", "تاریخ صاف کریں", "کوئی نتیجہ نہیں", "موجودہ آف لائن لائبریری میں اس سے ملتا کچھ نہیں", "ابھی", "مطابق آرٹسٹس", "مطابق البمز", "مطابق گانے")
    AppLanguage.English -> SearchUiCopy("Artists, albums & more", "Clear search", "Nothing searched yet", "More results will show here as you search for songs and albums", "Suggested albums", "You should probably revisit these", "Recently searched", "Clear history", "No results", "Nothing in the current offline library matches", "yet", "matching artists", "matching album results", "matching song results")
}

internal fun searchSortModeLabel(
    mode: SearchSongSortMode,
    language: AppLanguage,
): String = when (mode) {
    SearchSongSortMode.Title -> songSortLabel(language, SongSortMode.Title)
    SearchSongSortMode.Artist -> songSortLabel(language, SongSortMode.Artist)
}

internal fun recentAdditionsEmptyMessage(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> "Shto muzikë në pajisjen tënde për të parë diçka këtu"
    AppLanguage.Bengali -> "এখানে কিছু দেখতে ডিভাইসে গান যোগ করুন"
    AppLanguage.Bosnian -> "Dodajte muziku na uređaj da biste je vidjeli ovdje"
    AppLanguage.Bulgarian -> "Добавете музика на устройството, за да се покаже тук"
    AppLanguage.Catalan -> "Afegeix música al dispositiu perquè aparegui aquí"
    AppLanguage.ChineseSimplified -> "向设备添加音乐后即可在此查看"
    AppLanguage.Croatian -> "Dodajte glazbu na uređaj da biste je vidjeli ovdje"
    AppLanguage.Czech -> "Přidejte do zařízení hudbu, aby se zobrazila zde"
    AppLanguage.Danish -> "Føj musik til enheden for at se den her"
    AppLanguage.Dutch -> "Voeg muziek toe aan je apparaat om die hier te zien"
    AppLanguage.English -> "Add music to your device to see something here"
    AppLanguage.Estonian -> "Lisa seadmesse muusikat, et seda siin näha"
    AppLanguage.Finnish -> "Lisää musiikkia laitteellesi nähdäksesi sen täällä"
    AppLanguage.French -> "Ajoutez de la musique sur votre appareil pour la voir ici"
    AppLanguage.German -> "Füge deinem Gerät Musik hinzu, damit sie hier erscheint"
    AppLanguage.Greek -> "Προσθέστε μουσική στη συσκευή σας για να εμφανιστεί εδώ"
    AppLanguage.Hindi -> "यहाँ संगीत देखने के लिए अपने डिवाइस में गाने जोड़ें"
    AppLanguage.Hungarian -> "Adj zenét az eszközödhöz, hogy itt megjelenjen"
    AppLanguage.Indonesian -> "Tambahkan musik ke perangkat agar muncul di sini"
    AppLanguage.Italian -> "Aggiungi musica al dispositivo per visualizzarla qui"
    AppLanguage.Japanese -> "端末に音楽を追加するとここに表示されます"
    AppLanguage.Korean -> "기기에 음악을 추가하면 여기에 표시됩니다"
    AppLanguage.Latin -> "Musicam ad machinam adde ut hic appareat"
    AppLanguage.Latvian -> "Pievieno ierīcei mūziku, lai to šeit redzētu"
    AppLanguage.Lithuanian -> "Pridėkite muzikos prie įrenginio, kad ji būtų rodoma čia"
    AppLanguage.Macedonian -> "Додајте музика на уредот за да се прикаже тука"
    AppLanguage.Malay -> "Tambah muzik pada peranti anda untuk melihatnya di sini"
    AppLanguage.Norwegian -> "Legg til musikk på enheten for å se den her"
    AppLanguage.Polish -> "Dodaj muzykę do urządzenia, aby zobaczyć ją tutaj"
    AppLanguage.Portuguese -> "Adicione música ao dispositivo para a ver aqui"
    AppLanguage.Punjabi -> "ਇੱਥੇ ਸੰਗੀਤ ਦੇਖਣ ਲਈ ਆਪਣੇ ਡਿਵਾਈਸ ਉੱਤੇ ਗੀਤ ਜੋੜੋ"
    AppLanguage.Romanian -> "Adaugă muzică pe dispozitiv pentru a o vedea aici"
    AppLanguage.Russian -> "Добавьте музыку на устройство, чтобы она появилась здесь"
    AppLanguage.Serbian -> "Додајте музику на уређај да би се приказала овде"
    AppLanguage.Slovak -> "Pridajte do zariadenia hudbu, aby sa zobrazila tu"
    AppLanguage.Slovenian -> "Dodajte glasbo v napravo, da se prikaže tukaj"
    AppLanguage.Spanish -> "Añade música al dispositivo para verla aquí"
    AppLanguage.Swedish -> "Lägg till musik på enheten för att se den här"
    AppLanguage.Thai -> "เพิ่มเพลงลงในอุปกรณ์เพื่อให้แสดงที่นี่"
    AppLanguage.Ukrainian -> "Додайте музику на пристрій, щоб вона з’явилася тут"
    AppLanguage.Urdu -> "یہاں موسیقی دیکھنے کے لیے اپنے آلے میں گانے شامل کریں"
}


internal data class SettingsLanguageCopy(
    val settings: String,
    val appearance: String,
    val theme: String,
    val textSize: String,
    val language: String,
    val currentlyUsed: String,
    val sound: String,
    val bassBoost: String,
    val spaciousness: String,
    val equalizer: String,
    val otherSettings: String,
    val scanLibrary: String,
    val scanLibrarySubtitle: String,
    val scan: String,
    val changelog: String,
    val footerSubtitle: String,
    val volumeNormalization: String = "Volume normalization",
    val volumeNormalizationSubtitle: String = "Reduce loudness differences between songs when supported by file metadata",
    val crossfadeTitle: String = "Crossfade",
    val crossfadeSubtitle: String = "Adjust the transition between songs",
    val managePlaylistsTitle: String = "Manage playlists",
    val managePlaylistsSubtitle: String = "Import and export your playlists",
    val onlineLyricsTitle: String = "Online lyrics",
    val onlineLyricsSubtitle: String = "Fetch lyrics from LRCLIB",
    val checkForUpdatesTitle: String = "Check for updates",
    val checkForUpdatesSubtitle: String = "Check whether a newer version is available",
)

internal data class NowPlayingBarStyleCopy(
    val title: String,
    val subtitle: String,
    val floating: String,
    val compact: String,
)

internal fun nowPlayingBarStyleCopy(language: AppLanguage): NowPlayingBarStyleCopy = when (language) {
    AppLanguage.Bulgarian -> NowPlayingBarStyleCopy("Стил на лентата за възпроизвеждане", "Персонализирайте изгледа на плейъра", "Плаващ", "Компактен")
    AppLanguage.Catalan -> NowPlayingBarStyleCopy("Estil de la barra de reproducció", "Personalitza el reproductor", "Flotant", "Compacte")
    AppLanguage.Finnish -> NowPlayingBarStyleCopy("Toistopalkin tyyli", "Mukauta toistopalkkia", "Kelluva", "Tiivis")
    AppLanguage.Romanian -> NowPlayingBarStyleCopy("Stilul barei de redare", "Personalizează playerul", "Plutitor", "Compact")
    AppLanguage.Slovenian -> NowPlayingBarStyleCopy("Slog vrstice predvajanja", "Prilagodite predvajalnik", "Plavajoča", "Kompaktna")
    AppLanguage.Bosnian -> NowPlayingBarStyleCopy("Stil trake za reprodukciju", "Prilagodite prikaz reprodukcije", "Plutajuća", "Kompaktna")
    AppLanguage.Indonesian -> NowPlayingBarStyleCopy("Gaya bilah pemutar", "Sesuaikan tampilan pemutar", "Mengambang", "Ringkas")
    AppLanguage.Punjabi -> NowPlayingBarStyleCopy("ਹੁਣ ਚੱਲ ਰਹੀ ਪੱਟੀ ਦੀ ਸ਼ੈਲੀ", "ਪਲੇਅਰ ਦਾ ਰੂਪ ਬਦਲੋ", "ਤੈਰਦੀ", "ਸੰਖੇਪ")
    AppLanguage.Albanian -> NowPlayingBarStyleCopy("Stili i shiritit të riprodhimit", "Personalizo modulin e riprodhimit", "Lundrues", "Kompakt")
    AppLanguage.Bengali -> NowPlayingBarStyleCopy("এখন বাজছে বারের স্টাইল", "এখন বাজছে মডিউলটি কাস্টমাইজ করুন", "ভাসমান", "কমপ্যাক্ট")
    AppLanguage.ChineseSimplified -> NowPlayingBarStyleCopy("正在播放栏样式", "自定义正在播放模块", "浮动", "紧凑")
    AppLanguage.Croatian -> NowPlayingBarStyleCopy("Stil trake reprodukcije", "Prilagodite modul reprodukcije", "Plutajući", "Kompaktni")
    AppLanguage.Czech -> NowPlayingBarStyleCopy("Styl lišty přehrávání", "Přizpůsobte modul přehrávání", "Plovoucí", "Kompaktní")
    AppLanguage.Danish -> NowPlayingBarStyleCopy("Stil for afspilningslinjen", "Tilpas afspilningsmodulet", "Flydende", "Kompakt")
    AppLanguage.Dutch -> NowPlayingBarStyleCopy("Stijl van de afspeelbalk", "Pas de afspeelmodule aan", "Zwevend", "Compact")
    AppLanguage.English -> NowPlayingBarStyleCopy("Now playing bar style", "Customize the now playing module", "Floating", "Compact")
    AppLanguage.Estonian -> NowPlayingBarStyleCopy("Esitusriba stiil", "Kohanda esituse moodulit", "Hõljuv", "Kompaktne")
    AppLanguage.French -> NowPlayingBarStyleCopy("Style de la barre de lecture", "Personnalisez le module de lecture", "Flottant", "Compact")
    AppLanguage.German -> NowPlayingBarStyleCopy("Stil der Wiedergabeleiste", "Passe das Wiedergabemodul an", "Schwebend", "Kompakt")
    AppLanguage.Greek -> NowPlayingBarStyleCopy("Στυλ γραμμής αναπαραγωγής", "Προσαρμόστε τη μονάδα αναπαραγωγής", "Αιωρούμενο", "Συμπαγές")
    AppLanguage.Hindi -> NowPlayingBarStyleCopy("नाउ प्लेइंग बार शैली", "नाउ प्लेइंग मॉड्यूल को कस्टमाइज़ करें", "फ्लोटिंग", "कॉम्पैक्ट")
    AppLanguage.Hungarian -> NowPlayingBarStyleCopy("Lejátszósáv stílusa", "A lejátszási modul testreszabása", "Lebegő", "Kompakt")
    AppLanguage.Italian -> NowPlayingBarStyleCopy("Stile della barra di riproduzione", "Personalizza il modulo di riproduzione", "Fluttuante", "Compatto")
    AppLanguage.Japanese -> NowPlayingBarStyleCopy("再生バーのスタイル", "再生モジュールをカスタマイズ", "フローティング", "コンパクト")
    AppLanguage.Korean -> NowPlayingBarStyleCopy("재생 바 스타일", "재생 모듈 맞춤설정", "플로팅", "컴팩트")
    AppLanguage.Latin -> NowPlayingBarStyleCopy("Stylus vectis lusionis", "Modulum lusionis customiza", "Fluitans", "Compactus")
    AppLanguage.Latvian -> NowPlayingBarStyleCopy("Atskaņošanas joslas stils", "Pielāgo atskaņošanas moduli", "Peldošs", "Kompakts")
    AppLanguage.Lithuanian -> NowPlayingBarStyleCopy("Atkūrimo juostos stilius", "Pritaikykite atkūrimo modulį", "Slankusis", "Kompaktiškas")
    AppLanguage.Macedonian -> NowPlayingBarStyleCopy("Стил на лентата за репродукција", "Приспособете го модулот за репродукција", "Пловечки", "Компактен")
    AppLanguage.Malay -> NowPlayingBarStyleCopy("Gaya bar sedang dimainkan", "Sesuaikan modul sedang dimainkan", "Terapung", "Kompak")
    AppLanguage.Norwegian -> NowPlayingBarStyleCopy("Stil for avspillingslinje", "Tilpass avspillingsmodulen", "Flytende", "Kompakt")
    AppLanguage.Polish -> NowPlayingBarStyleCopy("Styl paska odtwarzania", "Dostosuj moduł odtwarzania", "Pływający", "Kompaktowy")
    AppLanguage.Portuguese -> NowPlayingBarStyleCopy("Estilo da barra de reprodução", "Personalize o módulo de reprodução", "Flutuante", "Compacto")
    AppLanguage.Russian -> NowPlayingBarStyleCopy("Стиль панели воспроизведения", "Настройте модуль воспроизведения", "Плавающий", "Компактный")
    AppLanguage.Serbian -> NowPlayingBarStyleCopy("Стил траке за репродукцију", "Прилагодите модул за репродукцију", "Плутајући", "Компактан")
    AppLanguage.Slovak -> NowPlayingBarStyleCopy("Štýl lišty prehrávania", "Prispôsobte modul prehrávania", "Plávajúci", "Kompaktný")
    AppLanguage.Spanish -> NowPlayingBarStyleCopy("Estilo de la barra de reproducción", "Personaliza el módulo de reproducción", "Flotante", "Compacto")
    AppLanguage.Swedish -> NowPlayingBarStyleCopy("Stil på uppspelningsfältet", "Anpassa uppspelningsmodulen", "Flytande", "Kompakt")
    AppLanguage.Thai -> NowPlayingBarStyleCopy("รูปแบบแถบกำลังเล่น", "ปรับแต่งโมดูลกำลังเล่น", "ลอย", "กะทัดรัด")
    AppLanguage.Ukrainian -> NowPlayingBarStyleCopy("Стиль панелі відтворення", "Налаштуйте модуль відтворення", "Плаваючий", "Компактний")
    AppLanguage.Urdu -> NowPlayingBarStyleCopy("اب چلنے والی بار کا انداز", "اب چلنے والے ماڈیول کو حسبِ ضرورت بنائیں", "فلوٹنگ", "کمپیکٹ")
}

@Suppress("CyclomaticComplexMethod")
internal fun nowPlayingStyleDescription(language: AppLanguage, compact: Boolean): String = when (language) {
    AppLanguage.Bulgarian -> if (compact) "Плейър по цялата ширина над навигационната лента" else "Плаващ плейър над навигационната лента"
    AppLanguage.Catalan -> if (compact) "Reproductor d’amplada completa sobre la barra de navegació" else "Reproductor flotant sobre la barra de navegació"
    AppLanguage.Finnish -> if (compact) "Koko näytön levyinen soitin navigointipalkin yläpuolella" else "Kelluva soitin navigointipalkin yläpuolella"
    AppLanguage.Romanian -> if (compact) "Player pe toată lățimea deasupra barei de navigare" else "Player plutitor deasupra barei de navigare"
    AppLanguage.Slovenian -> if (compact) "Predvajalnik po celotni širini nad navigacijsko vrstico" else "Plavajoči predvajalnik nad navigacijsko vrstico"
    AppLanguage.Bosnian -> if (compact) "Player preko cijele širine, odmah iznad navigacije" else "Plutajući player iznad navigacijske trake"
    AppLanguage.Indonesian -> if (compact) "Pemutar selebar layar tepat di atas bilah navigasi" else "Pemutar mengambang di atas bilah navigasi"
    AppLanguage.Punjabi -> if (compact) "ਨੈਵੀਗੇਸ਼ਨ ਪੱਟੀ ਦੇ ਉੱਪਰ ਪੂਰੀ ਚੌੜਾਈ ਵਾਲਾ ਪਲੇਅਰ" else "ਨੈਵੀਗੇਸ਼ਨ ਪੱਟੀ ਦੇ ਉੱਪਰ ਤੈਰਦਾ ਪਲੇਅਰ"
    AppLanguage.Albanian -> if (compact) "Luajtës nga skaji në skaj direkt mbi shiritin e navigimit" else "Luajtës lundrues mbi shiritin e navigimit"
    AppLanguage.Bengali -> if (compact) "নেভিগেশন বারের ঠিক উপরে প্রান্ত-থেকে-প্রান্ত প্লেয়ার" else "নেভিগেশন বারের উপরে ভাসমান প্লেয়ার"
    AppLanguage.ChineseSimplified -> if (compact) "紧贴导航栏上方的全宽播放器" else "位于导航栏上方的浮动播放器"
    AppLanguage.Croatian -> if (compact) "Player od ruba do ruba izravno iznad navigacijske trake" else "Plutajući player iznad navigacijske trake"
    AppLanguage.Czech -> if (compact) "Přehrávač přes celou šířku přímo nad navigační lištou" else "Plovoucí přehrávač nad navigační lištou"
    AppLanguage.Danish -> if (compact) "Kant-til-kant-afspiller lige over navigationslinjen" else "Flydende afspiller over navigationslinjen"
    AppLanguage.Dutch -> if (compact) "Rand-tot-randspeler direct boven de navigatiebalk" else "Zwevende speler boven de navigatiebalk"
    AppLanguage.English -> if (compact) "An edge-to-edge player directly above the navigation bar" else "A floating player above the navigation bar"
    AppLanguage.Estonian -> if (compact) "Servast servani mängija otse navigeerimisriba kohal" else "Hõljuv mängija navigeerimisriba kohal"
    AppLanguage.French -> if (compact) "Un lecteur bord à bord juste au-dessus de la barre de navigation" else "Un lecteur flottant au-dessus de la barre de navigation"
    AppLanguage.German -> if (compact) "Ein randloser Player direkt über der Navigationsleiste" else "Ein schwebender Player über der Navigationsleiste"
    AppLanguage.Greek -> if (compact) "Πρόγραμμα αναπαραγωγής από άκρη σε άκρη ακριβώς πάνω από τη γραμμή πλοήγησης" else "Αιωρούμενο πρόγραμμα αναπαραγωγής πάνω από τη γραμμή πλοήγησης"
    AppLanguage.Hindi -> if (compact) "नेविगेशन बार के ठीक ऊपर किनारे से किनारे तक प्लेयर" else "नेविगेशन बार के ऊपर फ्लोटिंग प्लेयर"
    AppLanguage.Hungarian -> if (compact) "Teljes szélességű lejátszó közvetlenül a navigációs sáv felett" else "Lebegő lejátszó a navigációs sáv felett"
    AppLanguage.Italian -> if (compact) "Un lettore da bordo a bordo direttamente sopra la barra di navigazione" else "Un lettore fluttuante sopra la barra di navigazione"
    AppLanguage.Japanese -> if (compact) "ナビゲーションバーのすぐ上に表示する全幅プレーヤー" else "ナビゲーションバーの上に浮かぶプレーヤー"
    AppLanguage.Korean -> if (compact) "내비게이션 바 바로 위에 표시되는 전체 너비 플레이어" else "내비게이션 바 위에 떠 있는 플레이어"
    AppLanguage.Latin -> if (compact) "Lusor per totam latitudinem directe supra vectem navigationis" else "Lusor fluitans supra vectem navigationis"
    AppLanguage.Latvian -> if (compact) "No malas līdz malai tieši virs navigācijas joslas" else "Peldošs atskaņotājs virs navigācijas joslas"
    AppLanguage.Lithuanian -> if (compact) "Per visą plotį iškart virš naršymo juostos" else "Slankusis grotuvas virš naršymo juostos"
    AppLanguage.Macedonian -> if (compact) "Плеер од раб до раб директно над лентата за навигација" else "Пловечки плеер над лентата за навигација"
    AppLanguage.Malay -> if (compact) "Pemain dari hujung ke hujung terus di atas bar navigasi" else "Pemain terapung di atas bar navigasi"
    AppLanguage.Norwegian -> if (compact) "Kant-til-kant-spiller rett over navigasjonslinjen" else "Flytende spiller over navigasjonslinjen"
    AppLanguage.Polish -> if (compact) "Odtwarzacz od krawędzi do krawędzi bezpośrednio nad paskiem nawigacji" else "Pływający odtwarzacz nad paskiem nawigacji"
    AppLanguage.Portuguese -> if (compact) "Um leitor de largura total diretamente acima da barra de navegação" else "Um leitor flutuante acima da barra de navegação"
    AppLanguage.Russian -> if (compact) "Проигрыватель на всю ширину прямо над панелью навигации" else "Плавающий проигрыватель над панелью навигации"
    AppLanguage.Serbian -> if (compact) "Плејер од ивице до ивице директно изнад траке за навигацију" else "Плутајући плејер изнад траке за навигацију"
    AppLanguage.Slovak -> if (compact) "Prehrávač cez celú šírku priamo nad navigačnou lištou" else "Plávajúci prehrávač nad navigačnou lištou"
    AppLanguage.Spanish -> if (compact) "Un reproductor de borde a borde justo encima de la barra de navegación" else "Un reproductor flotante sobre la barra de navegación"
    AppLanguage.Swedish -> if (compact) "En kant-till-kant-spelare direkt ovanför navigeringsfältet" else "En flytande spelare ovanför navigeringsfältet"
    AppLanguage.Thai -> if (compact) "เครื่องเล่นเต็มความกว้างที่อยู่เหนือแถบนำทางโดยตรง" else "เครื่องเล่นแบบลอยเหนือแถบนำทาง"
    AppLanguage.Ukrainian -> if (compact) "Програвач на всю ширину безпосередньо над панеллю навігації" else "Плаваючий програвач над панеллю навігації"
    AppLanguage.Urdu -> if (compact) "نیویگیشن بار کے بالکل اوپر کنارے سے کنارے تک پلیئر" else "نیویگیشن بار کے اوپر تیرتا ہوا پلیئر"
}
