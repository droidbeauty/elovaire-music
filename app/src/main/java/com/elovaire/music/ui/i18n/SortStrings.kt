package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.screens.AlbumSortMode
import elovaire.music.droidbeauty.app.ui.screens.SongSortMode
import java.time.YearMonth
import java.time.format.DateTimeFormatter

internal data class SortNames(val song: String, val artist: String, val album: String, val albumName: String)

private fun sortNames(language: AppLanguage): SortNames = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.sort_sortNames_1
    AppLanguage.Bosnian -> BosnianTranslations.sort_sortNames_1
    AppLanguage.Bulgarian -> BulgarianTranslations.sort_sortNames_1
    AppLanguage.Catalan -> CatalanTranslations.sort_sortNames_1
    AppLanguage.Finnish -> FinnishTranslations.sort_sortNames_1
    AppLanguage.Romanian -> RomanianTranslations.sort_sortNames_1
    AppLanguage.Slovenian -> SlovenianTranslations.sort_sortNames_1
    AppLanguage.Indonesian -> IndonesianTranslations.sort_sortNames_1
    AppLanguage.Punjabi -> PunjabiTranslations.sort_sortNames_1
    AppLanguage.Bengali -> BengaliTranslations.sort_sortNames_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.sort_sortNames_1
    AppLanguage.Croatian -> CroatianTranslations.sort_sortNames_1
    AppLanguage.Czech -> CzechTranslations.sort_sortNames_1
    AppLanguage.Danish -> DanishTranslations.sort_sortNames_1
    AppLanguage.Dutch -> DutchTranslations.sort_sortNames_1
    AppLanguage.English -> EnglishTranslations.sort_sortNames_1
    AppLanguage.Estonian -> EstonianTranslations.sort_sortNames_1
    AppLanguage.French -> FrenchTranslations.sort_sortNames_1
    AppLanguage.German -> GermanTranslations.sort_sortNames_1
    AppLanguage.Greek -> GreekTranslations.sort_sortNames_1
    AppLanguage.Hindi -> HindiTranslations.sort_sortNames_1
    AppLanguage.Hungarian -> HungarianTranslations.sort_sortNames_1
    AppLanguage.Italian -> ItalianTranslations.sort_sortNames_1
    AppLanguage.Japanese -> JapaneseTranslations.sort_sortNames_1
    AppLanguage.Korean -> KoreanTranslations.sort_sortNames_1
    AppLanguage.Latvian -> LatvianTranslations.sort_sortNames_1
    AppLanguage.Lithuanian -> LithuanianTranslations.sort_sortNames_1
    AppLanguage.Macedonian -> MacedonianTranslations.sort_sortNames_1
    AppLanguage.Malay -> MalayTranslations.sort_sortNames_1
    AppLanguage.Norwegian -> NorwegianTranslations.sort_sortNames_1
    AppLanguage.Polish -> PolishTranslations.sort_sortNames_1
    AppLanguage.Portuguese -> PortugueseTranslations.sort_sortNames_1
    AppLanguage.Russian -> RussianTranslations.sort_sortNames_1
    AppLanguage.Serbian -> SerbianTranslations.sort_sortNames_1
    AppLanguage.Slovak -> SlovakTranslations.sort_sortNames_1
    AppLanguage.Spanish -> SpanishTranslations.sort_sortNames_1
    AppLanguage.Swedish -> SwedishTranslations.sort_sortNames_1
    AppLanguage.Thai -> ThaiTranslations.sort_sortNames_1
    AppLanguage.Ukrainian -> UkrainianTranslations.sort_sortNames_1
    AppLanguage.Urdu -> UrduTranslations.sort_sortNames_1
}

internal fun songSortLabel(language: AppLanguage, mode: SongSortMode): String = sortNames(language).let {
    when (mode) {
        SongSortMode.Title -> it.song
        SongSortMode.Artist -> it.artist
        SongSortMode.Album -> it.album
    }
}

internal fun albumSortLabel(language: AppLanguage, mode: AlbumSortMode): String = sortNames(language).let {
    when (mode) {
        AlbumSortMode.Artist -> it.artist
        AlbumSortMode.Album -> it.albumName
        AlbumSortMode.ReleaseYear -> albumSortAdditionalLabels(language).first
        AlbumSortMode.DateAdded -> albumSortAdditionalLabels(language).second
    }
}

private fun albumSortAdditionalLabels(language: AppLanguage): Pair<String, String> = when (language) {
    AppLanguage.Albanian -> "Viti i publikimit" to "Data e shtimit"
    AppLanguage.Bengali -> "প্রকাশের বছর" to "যোগের তারিখ"
    AppLanguage.Bosnian -> "Godina izdanja" to "Datum dodavanja"
    AppLanguage.Bulgarian -> "Година на издаване" to "Дата на добавяне"
    AppLanguage.Catalan -> "Any de publicació" to "Data d’afegit"
    AppLanguage.ChineseSimplified -> "发行年份" to "添加日期"
    AppLanguage.Croatian -> "Godina izdanja" to "Datum dodavanja"
    AppLanguage.Czech -> "Rok vydání" to "Datum přidání"
    AppLanguage.Danish -> "Udgivelsesår" to "Tilføjelsesdato"
    AppLanguage.Dutch -> "Uitgavejaar" to "Toegevoegd op"
    AppLanguage.English -> "Release year" to "Date added"
    AppLanguage.Estonian -> "Väljalaskeaasta" to "Lisamise kuupäev"
    AppLanguage.Finnish -> "Julkaisuvuosi" to "Lisäyspäivä"
    AppLanguage.French -> "Année de sortie" to "Date d’ajout"
    AppLanguage.German -> "Erscheinungsjahr" to "Hinzugefügt am"
    AppLanguage.Greek -> "Έτος κυκλοφορίας" to "Ημερομηνία προσθήκης"
    AppLanguage.Hindi -> "रिलीज़ वर्ष" to "जोड़ने की तारीख"
    AppLanguage.Hungarian -> "Megjelenés éve" to "Hozzáadás dátuma"
    AppLanguage.Indonesian -> "Tahun rilis" to "Tanggal ditambahkan"
    AppLanguage.Italian -> "Anno di uscita" to "Data di aggiunta"
    AppLanguage.Japanese -> "発売年" to "追加日"
    AppLanguage.Korean -> "발매 연도" to "추가 날짜"
    AppLanguage.Latvian -> "Izdošanas gads" to "Pievienošanas datums"
    AppLanguage.Lithuanian -> "Išleidimo metai" to "Pridėjimo data"
    AppLanguage.Macedonian -> "Година на издавање" to "Датум на додавање"
    AppLanguage.Malay -> "Tahun keluaran" to "Tarikh ditambah"
    AppLanguage.Norwegian -> "Utgivelsesår" to "Dato lagt til"
    AppLanguage.Polish -> "Rok wydania" to "Data dodania"
    AppLanguage.Portuguese -> "Ano de lançamento" to "Data de adição"
    AppLanguage.Punjabi -> "ਰਿਲੀਜ਼ ਸਾਲ" to "ਸ਼ਾਮਲ ਕਰਨ ਦੀ ਮਿਤੀ"
    AppLanguage.Romanian -> "Anul lansării" to "Data adăugării"
    AppLanguage.Russian -> "Год выпуска" to "Дата добавления"
    AppLanguage.Serbian -> "Година издања" to "Датум додавања"
    AppLanguage.Slovak -> "Rok vydania" to "Dátum pridania"
    AppLanguage.Slovenian -> "Leto izdaje" to "Datum dodajanja"
    AppLanguage.Spanish -> "Año de lanzamiento" to "Fecha de incorporación"
    AppLanguage.Swedish -> "Utgivningsår" to "Datum tillagd"
    AppLanguage.Thai -> "ปีที่วางจำหน่าย" to "วันที่เพิ่ม"
    AppLanguage.Ukrainian -> "Рік випуску" to "Дата додавання"
    AppLanguage.Urdu -> "ریلیز کا سال" to "شامل کرنے کی تاریخ"
}

internal fun albumAddedMonthLabel(month: YearMonth, language: AppLanguage): String =
    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", language.appLocale()))
