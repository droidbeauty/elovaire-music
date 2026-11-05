package elovaire.music.droidbeauty.app.ui.screens.tags

import elovaire.music.droidbeauty.app.ui.interaction.elovaireClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.tagAdditionalIssuesCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagValidationError
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagSaveFailure
import elovaire.music.droidbeauty.app.ui.i18n.localizedTagSaveStatus
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.screens.DetailListTopBar
import elovaire.music.droidbeauty.app.ui.screens.FastScrollbar
import elovaire.music.droidbeauty.app.ui.screens.ensureSingleItemRubberBand
import elovaire.music.droidbeauty.app.ui.screens.TopBarActionSpec
import elovaire.music.droidbeauty.app.ui.screens.common.EditorTextField
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.ElovaireSpacing

@Composable
internal fun AudiobookTagEditorScreen(
    state: AudiobookTagEditorUiState,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onPickCoverArt: () -> Unit,
    onBookTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReleaseYearChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onPartTitleChange: (Long, String) -> Unit,
    onPartArtistChange: (Long, String) -> Unit,
    onPartTrackNumberChange: (Long, String) -> Unit,
    onPartDiscNumberChange: (Long, String) -> Unit,
) {
    val copy = remember(appLanguage) { audiobookTagEditorCopy(appLanguage) }
    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.originalBook == null -> Text(copy.notFound, Modifier.align(Alignment.Center))
            else -> LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier.fillMaxSize().imePadding().ensureSingleItemRubberBand(listState),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    top = ElovaireSpacing.topBarContentHeight + 44.dp,
                    end = 18.dp,
                    bottom = 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    AudiobookBookMetadataSection(
                        state = state,
                        copy = copy,
                        onPickCoverArt = onPickCoverArt,
                        onBookTitleChange = onBookTitleChange,
                        onAuthorChange = onAuthorChange,
                        onDescriptionChange = onDescriptionChange,
                        onReleaseYearChange = onReleaseYearChange,
                        onGenreChange = onGenreChange,
                    )
                }
                item { Text(copy.parts, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 2.dp)) }
                itemsIndexed(state.parts, key = { _, part -> part.songId }) { index, part ->
                    AudiobookPhysicalPartCard(
                        index = index,
                        part = part,
                        copy = copy,
                        onPartTitleChange = onPartTitleChange,
                        onPartArtistChange = onPartArtistChange,
                        onPartTrackNumberChange = onPartTrackNumberChange,
                        onPartDiscNumberChange = onPartDiscNumberChange,
                    )
                }
                item {
                    AudiobookTagEditorFeedback(state, appLanguage)
                }
            }
        }
        if (state.originalBook != null) {
            FastScrollbar(state = listState, topInset = ElovaireSpacing.topBarContentHeight + 44.dp, bottomInset = 28.dp)
            DetailListTopBar(
                title = copy.editorTitle,
                subtitle = state.bookTitle,
                onBack = onBack,
                actions = listOf(
                    TopBarActionSpec(
                        iconResId = R.drawable.ic_lucide_check,
                        contentDescription = copy.save,
                        onClick = onSave,
                        enabled = state.canSave && !state.isSaving,
                    ),
                ),
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun AudiobookBookMetadataSection(
    state: AudiobookTagEditorUiState,
    copy: AudiobookTagEditorCopy,
    onPickCoverArt: () -> Unit,
    onBookTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReleaseYearChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
) {
    val book = state.originalBook ?: return
    Surface(
        shape = RoundedCornerShape(ElovaireRadii.module),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                ArtworkImage(
                    uri = state.selectedArtworkUri ?: book.artUri,
                    title = state.bookTitle,
                    placeholderIconResId = R.drawable.ic_lucide_book_headphones,
                    modifier = Modifier
                        .size(112.dp)
                        .elovaireClickable(onClick = onPickCoverArt),
                    requestedSizePx = 256,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(copy.bookSection, fontWeight = FontWeight.SemiBold)
                    Text(copy.coverHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            EditorTextField(state.bookTitle, onBookTitleChange, copy.bookTitle, Modifier.fillMaxWidth())
            EditorTextField(state.author, onAuthorChange, copy.author, Modifier.fillMaxWidth())
            EditorTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = copy.description,
                singleLine = false,
                minLines = 4,
                maxLines = 8,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorTextField(state.releaseYear, onReleaseYearChange, copy.year, Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                EditorTextField(state.genre, onGenreChange, copy.genre, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AudiobookPhysicalPartCard(
    index: Int,
    part: EditableAudiobookPartState,
    copy: AudiobookTagEditorCopy,
    onPartTitleChange: (Long, String) -> Unit,
    onPartArtistChange: (Long, String) -> Unit,
    onPartTrackNumberChange: (Long, String) -> Unit,
    onPartDiscNumberChange: (Long, String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(ElovaireRadii.card),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "${index + 1}  ${part.fileName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            EditorTextField(part.title, { onPartTitleChange(part.songId, it) }, copy.partTitle, Modifier.fillMaxWidth())
            EditorTextField(part.artist, { onPartArtistChange(part.songId, it) }, copy.partAuthor, Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorTextField(part.trackNumber, { onPartTrackNumberChange(part.songId, it) }, copy.partNumber, Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                EditorTextField(part.discNumber, { onPartDiscNumberChange(part.songId, it) }, copy.discNumber, Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}

@Composable
private fun AudiobookTagEditorFeedback(state: AudiobookTagEditorUiState, language: AppLanguage) {
    val issues = tagAdditionalIssuesCopy(language)
    Column(
        modifier = Modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        state.validationErrors.take(3).forEach { error ->
            Text(localizedTagValidationError(error, language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (state.validationErrors.size > 3) {
            Text(
                text = if (language == AppLanguage.English) additionalTagIssueCount(state.validationErrors.size - 3, "validation")
                    else "${issues.validation}: ${state.validationErrors.size - 3}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.saveFailures.take(3).forEach { failure ->
            Text(
                text = "${failure.fileName}: ${localizedTagSaveFailure(failure, language)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.saveFailures.size > 3) {
            Text(
                text = if (language == AppLanguage.English) additionalTagIssueCount(state.saveFailures.size - 3, "save")
                    else "${issues.save}: ${state.saveFailures.size - 3}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.statusMessage?.let {
            val message = state.saveFailures.firstOrNull { failure -> failure.reason == it }
                ?.let { failure -> localizedTagSaveFailure(failure, language) }
                ?: localizedTagSaveStatus(it, state.saveFailures.size, language)
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

internal fun additionalTagIssueCount(count: Int, kind: String): String =
    "+$count more $kind ${if (count == 1) "issue" else "issues"}"

internal data class AudiobookTagEditorCopy(
    val editorTitle: String,
    val notFound: String,
    val bookSection: String,
    val bookTitle: String,
    val author: String,
    val description: String,
    val year: String,
    val genre: String,
    val coverHint: String,
    val parts: String,
    val partTitle: String,
    val partAuthor: String,
    val partNumber: String,
    val discNumber: String,
    val save: String,
)

internal fun audiobookTagEditorCopy(language: AppLanguage): AudiobookTagEditorCopy = when (language) {
    AppLanguage.Albanian -> AudiobookTagEditorCopy("Redakto librin audio", "Libri audio nuk u gjet", "Informacion mbi librin", "Titulli i librit", "Autori", "Përshkrimi", "Viti i publikimit", "Zhanri", "Prek kopertinën për ta ndryshuar", "Pjesët", "Titulli i pjesës", "Artisti i pjesës", "Numri i pjesës", "Numri i diskut", "Ruaj")
    AppLanguage.Estonian -> AudiobookTagEditorCopy("Muuda audioraamatut", "Audioraamatut ei leitud", "Raamatu teave", "Raamatu pealkiri", "Autor", "Kirjeldus", "Ilmumisaasta", "Žanr", "Kaanepildi muutmiseks puuduta seda", "Osad", "Osa pealkiri", "Osa esitaja", "Osa number", "Plaadi number", "Salvesta")
    AppLanguage.Greek -> AudiobookTagEditorCopy("Επεξεργασία ηχητικού βιβλίου", "Το ηχητικό βιβλίο δεν βρέθηκε", "Πληροφορίες βιβλίου", "Τίτλος βιβλίου", "Συγγραφέας", "Περιγραφή", "Έτος κυκλοφορίας", "Είδος", "Πατήστε το εξώφυλλο για αλλαγή", "Μέρη", "Τίτλος μέρους", "Καλλιτέχνης μέρους", "Αριθμός μέρους", "Αριθμός δίσκου", "Αποθήκευση")
    AppLanguage.Hungarian -> AudiobookTagEditorCopy("Hangoskönyv szerkesztése", "A hangoskönyv nem található", "Könyv adatai", "Könyv címe", "Szerző", "Leírás", "Megjelenés éve", "Műfaj", "Koppints a borítóra a módosításhoz", "Részek", "Rész címe", "Rész előadója", "Rész száma", "Lemez száma", "Mentés")
    AppLanguage.Latvian -> AudiobookTagEditorCopy("Rediģēt audiogrāmatu", "Audiogrāmata nav atrasta", "Informācija par grāmatu", "Grāmatas nosaukums", "Autors", "Apraksts", "Izdošanas gads", "Žanrs", "Pieskaries vāciņam, lai to mainītu", "Daļas", "Daļas nosaukums", "Daļas izpildītājs", "Daļas numurs", "Diska numurs", "Saglabāt")
    AppLanguage.Lithuanian -> AudiobookTagEditorCopy("Redaguoti garsinę knygą", "Garsinė knyga nerasta", "Knygos informacija", "Knygos pavadinimas", "Autorius", "Aprašas", "Išleidimo metai", "Žanras", "Palieskite viršelį, kad jį pakeistumėte", "Dalys", "Dalies pavadinimas", "Dalies atlikėjas", "Dalies numeris", "Disko numeris", "Išsaugoti")
    AppLanguage.Macedonian -> AudiobookTagEditorCopy("Уреди аудиокнига", "Аудиокнигата не е пронајдена", "Информации за книгата", "Наслов на книгата", "Автор", "Опис", "Година на издавање", "Жанр", "Допрете ја насловната за да ја смените", "Делови", "Наслов на делот", "Изведувач на делот", "Број на делот", "Број на дискот", "Зачувај")
    AppLanguage.Serbian -> AudiobookTagEditorCopy("Уреди аудио-књигу", "Аудио-књига није пронађена", "Подаци о књизи", "Наслов књиге", "Аутор", "Опис", "Година издања", "Жанр", "Додирните омот да га промените", "Делови", "Наслов дела", "Извођач дела", "Број дела", "Број диска", "Сачувај")
    AppLanguage.Thai -> AudiobookTagEditorCopy("แก้ไขหนังสือเสียง", "ไม่พบหนังสือเสียง", "ข้อมูลหนังสือ", "ชื่อหนังสือ", "ผู้เขียน", "คำอธิบาย", "ปีที่เผยแพร่", "แนวเพลง", "แตะปกเพื่อเปลี่ยน", "ส่วน", "ชื่อส่วน", "ศิลปินของส่วน", "หมายเลขส่วน", "หมายเลขแผ่น", "บันทึก")
    AppLanguage.ChineseSimplified -> AudiobookTagEditorCopy("编辑有声书", "找不到有声书", "图书信息", "书名", "作者", "简介", "发行年份", "流派", "点击封面进行更换", "部分", "部分标题", "部分艺人", "部分编号", "唱片编号", "保存")
    AppLanguage.Czech -> AudiobookTagEditorCopy("Upravit audioknihu", "Audiokniha nebyla nalezena", "Informace o knize", "Název knihy", "Autor", "Popis", "Rok vydání", "Žánr", "Klepnutím na obal jej změníte", "Části", "Název části", "Interpret části", "Číslo části", "Číslo disku", "Uložit")
    AppLanguage.Danish -> AudiobookTagEditorCopy("Rediger lydbog", "Lydbogen blev ikke fundet", "Bogoplysninger", "Bogtitel", "Forfatter", "Beskrivelse", "Udgivelsesår", "Genre", "Tryk på coveret for at ændre det", "Dele", "Delens titel", "Delens kunstner", "Delnummer", "Disknummer", "Gem")
    AppLanguage.Dutch -> AudiobookTagEditorCopy("Luisterboek bewerken", "Luisterboek niet gevonden", "Boekinformatie", "Boektitel", "Auteur", "Beschrijving", "Jaar van uitgave", "Genre", "Tik op de omslag om deze te wijzigen", "Delen", "Titel van deel", "Artiest van deel", "Deelnummer", "Schijfnummer", "Opslaan")
    AppLanguage.Hindi -> AudiobookTagEditorCopy("ऑडियोबुक संपादित करें", "ऑडियोबुक नहीं मिली", "पुस्तक की जानकारी", "पुस्तक का शीर्षक", "लेखक", "विवरण", "रिलीज़ का वर्ष", "शैली", "बदलने के लिए कवर पर टैप करें", "भाग", "भाग का शीर्षक", "भाग का कलाकार", "भाग नंबर", "डिस्क नंबर", "सहेजें")
    AppLanguage.Japanese -> AudiobookTagEditorCopy("オーディオブックを編集", "オーディオブックが見つかりません", "本の情報", "本のタイトル", "著者", "説明", "リリース年", "ジャンル", "カバーをタップして変更", "パート", "パートのタイトル", "パートのアーティスト", "パート番号", "ディスク番号", "保存")
    AppLanguage.Norwegian -> AudiobookTagEditorCopy("Rediger lydbok", "Lydboken ble ikke funnet", "Bokinformasjon", "Boktittel", "Forfatter", "Beskrivelse", "Utgivelsesår", "Sjanger", "Trykk på omslaget for å endre det", "Deler", "Delens tittel", "Delens artist", "Delnummer", "Platenummer", "Lagre")
    AppLanguage.Swedish -> AudiobookTagEditorCopy("Redigera ljudbok", "Ljudboken hittades inte", "Bokinformation", "Boktitel", "Författare", "Beskrivning", "Utgivningsår", "Genre", "Tryck på omslaget för att ändra det", "Delar", "Delens titel", "Delens artist", "Delnummer", "Skivnummer", "Spara")
    AppLanguage.French -> AudiobookTagEditorCopy("Modifier le livre audio", "Livre audio introuvable", "Informations sur le livre", "Titre du livre", "Auteur", "Description", "Année de sortie", "Genre", "Appuyez sur la couverture pour la modifier", "Parties", "Titre de la partie", "Artiste de la partie", "Numéro de partie", "Numéro de disque", "Enregistrer")
    AppLanguage.German -> AudiobookTagEditorCopy("Hörbuch bearbeiten", "Hörbuch nicht gefunden", "Buchinformationen", "Buchtitel", "Autor", "Beschreibung", "Erscheinungsjahr", "Genre", "Zum Ändern auf das Cover tippen", "Teile", "Titel des Teils", "Interpret des Teils", "Teilnummer", "CD-Nummer", "Speichern")
    AppLanguage.Italian -> AudiobookTagEditorCopy("Modifica audiolibro", "Audiolibro non trovato", "Informazioni sul libro", "Titolo del libro", "Autore", "Descrizione", "Anno di pubblicazione", "Genere", "Tocca la copertina per cambiarla", "Parti", "Titolo della parte", "Artista della parte", "Numero della parte", "Numero disco", "Salva")
    AppLanguage.Portuguese -> AudiobookTagEditorCopy("Editar audiolivro", "Audiolivro não encontrado", "Informações do livro", "Título do livro", "Autor", "Descrição", "Ano de lançamento", "Género", "Toque na capa para a alterar", "Partes", "Título da parte", "Artista da parte", "Número da parte", "Número do disco", "Guardar")
    AppLanguage.Russian -> AudiobookTagEditorCopy("Редактировать аудиокнигу", "Аудиокнига не найдена", "Информация о книге", "Название книги", "Автор", "Описание", "Год выпуска", "Жанр", "Нажмите на обложку, чтобы изменить её", "Части", "Название части", "Исполнитель части", "Номер части", "Номер диска", "Сохранить")
    AppLanguage.Spanish -> AudiobookTagEditorCopy("Editar audiolibro", "Audiolibro no encontrado", "Información del libro", "Título del libro", "Autor", "Descripción", "Año de lanzamiento", "Género", "Toca la portada para cambiarla", "Partes", "Título de la parte", "Artista de la parte", "Número de parte", "Número de disco", "Guardar")
    AppLanguage.Ukrainian -> AudiobookTagEditorCopy("Редагувати аудіокнигу", "Аудіокнигу не знайдено", "Інформація про книгу", "Назва книги", "Автор", "Опис", "Рік випуску", "Жанр", "Натисніть обкладинку, щоб змінити її", "Частини", "Назва частини", "Виконавець частини", "Номер частини", "Номер диска", "Зберегти")
    AppLanguage.Bulgarian -> AudiobookTagEditorCopy("Редактирай аудиокнигата", "Аудиокнигата не е намерена", "Информация за книгата", "Заглавие на книгата", "Автор", "Описание", "Година на издаване", "Жанр", "Докоснете обложката, за да я смените", "Части", "Заглавие на частта", "Изпълнител на частта", "Номер на частта", "Номер на диска", "Запази")
    AppLanguage.Catalan -> AudiobookTagEditorCopy("Edita l’audiollibre", "No s’ha trobat l’audiollibre", "Informació del llibre", "Títol del llibre", "Autor", "Descripció", "Any de publicació", "Gènere", "Toca la portada per canviar-la", "Parts", "Títol de la part", "Artista de la part", "Número de part", "Número de disc", "Desa")
    AppLanguage.Finnish -> AudiobookTagEditorCopy("Muokkaa äänikirjaa", "Äänikirjaa ei löytynyt", "Kirjan tiedot", "Kirjan nimi", "Kirjailija", "Kuvaus", "Julkaisuvuosi", "Tyylilaji", "Vaihda kansikuvaa napauttamalla", "Osat", "Osan nimi", "Osan esittäjä", "Osan numero", "Levyn numero", "Tallenna")
    AppLanguage.Romanian -> AudiobookTagEditorCopy("Editează audiocartea", "Audiocartea nu a fost găsită", "Informații despre carte", "Titlul cărții", "Autor", "Descriere", "Anul lansării", "Gen", "Atinge coperta pentru a o schimba", "Părți", "Titlul părții", "Artistul părții", "Numărul părții", "Numărul discului", "Salvează")
    AppLanguage.Slovenian -> AudiobookTagEditorCopy("Uredi zvočno knjigo", "Zvočne knjige ni mogoče najti", "Podatki o knjigi", "Naslov knjige", "Avtor", "Opis", "Leto izdaje", "Žanr", "Tapnite naslovnico, da jo zamenjate", "Deli", "Naslov dela", "Izvajalec dela", "Številka dela", "Številka diska", "Shrani")
    AppLanguage.Bosnian -> AudiobookTagEditorCopy("Uredi oznake audioknjige", "Audioknjiga nije pronađena", "Informacije o knjizi", "Naslov knjige", "Autor", "Opis", "Godina izdanja", "Žanr", "Dodirnite omot da ga promijenite", "Dijelovi", "Naslov dijela", "Autor dijela", "Broj dijela", "Broj diska", "Sačuvaj")
    AppLanguage.Indonesian -> AudiobookTagEditorCopy("Edit tag buku audio", "Buku audio tidak ditemukan", "Informasi buku", "Judul buku", "Penulis", "Deskripsi", "Tahun rilis", "Genre", "Ketuk sampul untuk mengubahnya", "Bagian", "Judul bagian", "Artis bagian", "Nomor bagian", "Nomor cakram", "Simpan")
    AppLanguage.Punjabi -> AudiobookTagEditorCopy("ਆਡੀਓ ਕਿਤਾਬ ਦੇ ਟੈਗ ਸੋਧੋ", "ਆਡੀਓ ਕਿਤਾਬ ਨਹੀਂ ਮਿਲੀ", "ਕਿਤਾਬ ਦੀ ਜਾਣਕਾਰੀ", "ਕਿਤਾਬ ਦਾ ਸਿਰਲੇਖ", "ਲੇਖਕ", "ਵੇਰਵਾ", "ਰਿਲੀਜ਼ ਦਾ ਸਾਲ", "ਸ਼ੈਲੀ", "ਕਵਰ ਬਦਲਣ ਲਈ ਇਸ ਨੂੰ ਦਬਾਓ", "ਭਾਗ", "ਭਾਗ ਦਾ ਸਿਰਲੇਖ", "ਭਾਗ ਦਾ ਕਲਾਕਾਰ", "ਭਾਗ ਨੰਬਰ", "ਡਿਸਕ ਨੰਬਰ", "ਸੰਭਾਲੋ")
    AppLanguage.Polish -> AudiobookTagEditorCopy("Edytuj audiobook", "Nie znaleziono audiobooka", "Informacje o książce", "Tytuł książki", "Autor", "Opis", "Rok wydania", "Gatunek", "Dotknij okładki, aby ją zmienić", "Części", "Tytuł części", "Autor części", "Numer części", "Numer dysku", "Zapisz")
    AppLanguage.Slovak -> AudiobookTagEditorCopy("Upraviť audioknihu", "Audiokniha sa nenašla", "Informácie o knihe", "Názov knihy", "Autor", "Popis", "Rok vydania", "Žáner", "Ťuknutím zmeníte obal", "Časti", "Názov časti", "Interpret časti", "Číslo časti", "Číslo disku", "Uložiť")
    AppLanguage.Croatian -> AudiobookTagEditorCopy("Uredi audioknjigu", "Audioknjiga nije pronađena", "Podaci o knjizi", "Naslov knjige", "Autor", "Opis", "Godina izdanja", "Žanr", "Dodirnite omot za promjenu", "Dijelovi", "Naslov dijela", "Autor dijela", "Broj dijela", "Broj diska", "Spremi")
    AppLanguage.Korean -> AudiobookTagEditorCopy("오디오북 태그 편집", "오디오북을 찾을 수 없습니다", "도서 정보", "도서 제목", "저자", "설명", "발매 연도", "장르", "커버를 탭하여 변경", "부분", "부분 제목", "부분 저자", "부분 번호", "디스크 번호", "저장")
    AppLanguage.Malay -> AudiobookTagEditorCopy("Edit tag audiobook", "Buku audio tidak ditemui", "Maklumat buku", "Tajuk buku", "Pengarang", "Penerangan", "Tahun keluaran", "Genre", "Ketik kulit untuk menukar", "Bahagian", "Tajuk bahagian", "Pengarang bahagian", "Nombor bahagian", "Nombor cakera", "Simpan")
    AppLanguage.Bengali -> AudiobookTagEditorCopy("অডিওবুক ট্যাগ সম্পাদনা", "অডিওবুক পাওয়া যায়নি", "বইয়ের তথ্য", "বইয়ের শিরোনাম", "লেখক", "বিবরণ", "প্রকাশের বছর", "ধরন", "পরিবর্তন করতে কভারে ট্যাপ করুন", "অংশ", "অংশের শিরোনাম", "অংশের লেখক", "অংশের নম্বর", "ডিস্ক নম্বর", "সংরক্ষণ করুন")
    AppLanguage.Urdu -> AudiobookTagEditorCopy("آڈیو بک ٹیگز میں ترمیم کریں", "آڈیو بک نہیں ملی", "کتاب کی معلومات", "کتاب کا عنوان", "مصنف", "تفصیل", "اجرا کا سال", "صنف", "تبدیل کرنے کے لیے کور پر ٹیپ کریں", "حصے", "حصے کا عنوان", "حصے کا مصنف", "حصے کا نمبر", "ڈسک نمبر", "محفوظ کریں")
    AppLanguage.English -> AudiobookTagEditorCopy("Edit audiobook tags", "Audiobook not found", "Book information", "Book title", "Author", "Description", "Release year", "Genre", "Tap the cover to change it", "Parts", "Part title", "Part artist", "Part number", "Disc number", "Save")
}
