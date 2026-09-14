package elovaire.music.droidbeauty.app.ui.screens.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.screens.DetailListTopBar
import elovaire.music.droidbeauty.app.ui.screens.FastScrollbar
import elovaire.music.droidbeauty.app.ui.screens.TopBarActionSpec
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
                modifier = Modifier.fillMaxSize(),
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
                    AudiobookTagEditorFeedback(state)
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
                        .clickable(onClick = onPickCoverArt),
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
            OutlinedTextField(state.bookTitle, onBookTitleChange, Modifier.fillMaxWidth(), label = { Text(copy.bookTitle) }, singleLine = true)
            OutlinedTextField(state.author, onAuthorChange, Modifier.fillMaxWidth(), label = { Text(copy.author) }, singleLine = true)
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(copy.description) },
                minLines = 4,
                maxLines = 8,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.releaseYear, onReleaseYearChange, Modifier.weight(1f), label = { Text(copy.year) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(state.genre, onGenreChange, Modifier.weight(1f), label = { Text(copy.genre) }, singleLine = true)
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
            OutlinedTextField(part.title, { onPartTitleChange(part.songId, it) }, Modifier.fillMaxWidth(), label = { Text(copy.partTitle) }, singleLine = true)
            OutlinedTextField(part.artist, { onPartArtistChange(part.songId, it) }, Modifier.fillMaxWidth(), label = { Text(copy.partAuthor) }, singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(part.trackNumber, { onPartTrackNumberChange(part.songId, it) }, Modifier.weight(1f), label = { Text(copy.partNumber) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(part.discNumber, { onPartDiscNumberChange(part.songId, it) }, Modifier.weight(1f), label = { Text(copy.discNumber) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}

@Composable
private fun AudiobookTagEditorFeedback(state: AudiobookTagEditorUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        state.validationErrors.take(3).forEach { error ->
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (state.validationErrors.size > 3) {
            Text(
                text = "+${state.validationErrors.size - 3} more validation issue(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.saveFailures.take(3).forEach { failure ->
            Text(
                text = "${failure.fileName}: ${failure.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.saveFailures.size > 3) {
            Text(
                text = "+${state.saveFailures.size - 3} more save issue(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.statusMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class AudiobookTagEditorCopy(
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

private fun audiobookTagEditorCopy(language: AppLanguage): AudiobookTagEditorCopy = when (language) {
    AppLanguage.Polish -> AudiobookTagEditorCopy("Edytuj audiobook", "Nie znaleziono audiobooka", "Informacje o książce", "Tytuł książki", "Autor", "Opis", "Rok wydania", "Gatunek", "Dotknij okładki, aby ją zmienić", "Części", "Tytuł części", "Autor części", "Numer części", "Numer dysku", "Zapisz")
    AppLanguage.Slovak -> AudiobookTagEditorCopy("Upraviť audioknihu", "Audiokniha sa nenašla", "Informácie o knihe", "Názov knihy", "Autor", "Popis", "Rok vydania", "Žáner", "Ťuknutím zmeníte obal", "Časti", "Názov časti", "Interpret časti", "Číslo časti", "Číslo disku", "Uložiť")
    AppLanguage.Croatian -> AudiobookTagEditorCopy("Uredi audioknjigu", "Audioknjiga nije pronađena", "Podaci o knjizi", "Naslov knjige", "Autor", "Opis", "Godina izdanja", "Žanr", "Dodirnite omot za promjenu", "Dijelovi", "Naslov dijela", "Autor dijela", "Broj dijela", "Broj diska", "Spremi")
    AppLanguage.Korean -> AudiobookTagEditorCopy("오디오북 태그 편집", "오디오북을 찾을 수 없습니다", "도서 정보", "도서 제목", "저자", "설명", "발매 연도", "장르", "커버를 탭하여 변경", "부분", "부분 제목", "부분 저자", "부분 번호", "디스크 번호", "저장")
    AppLanguage.Malay -> AudiobookTagEditorCopy("Edit tag audiobook", "Buku audio tidak ditemui", "Maklumat buku", "Tajuk buku", "Pengarang", "Penerangan", "Tahun keluaran", "Genre", "Ketik kulit untuk menukar", "Bahagian", "Tajuk bahagian", "Pengarang bahagian", "Nombor bahagian", "Nombor cakera", "Simpan")
    AppLanguage.Bengali -> AudiobookTagEditorCopy("অডিওবুক ট্যাগ সম্পাদনা", "অডিওবুক পাওয়া যায়নি", "বইয়ের তথ্য", "বইয়ের শিরোনাম", "লেখক", "বিবরণ", "প্রকাশের বছর", "ধরন", "পরিবর্তন করতে কভারে ট্যাপ করুন", "অংশ", "অংশের শিরোনাম", "অংশের লেখক", "অংশের নম্বর", "ডিস্ক নম্বর", "সংরক্ষণ করুন")
    AppLanguage.Urdu -> AudiobookTagEditorCopy("آڈیو بک ٹیگز میں ترمیم کریں", "آڈیو بک نہیں ملی", "کتاب کی معلومات", "کتاب کا عنوان", "مصنف", "تفصیل", "اجرا کا سال", "صنف", "تبدیل کرنے کے لیے کور پر ٹیپ کریں", "حصے", "حصے کا عنوان", "حصے کا مصنف", "حصے کا نمبر", "ڈسک نمبر", "محفوظ کریں")
    else -> AudiobookTagEditorCopy("Edit audiobook tags", "Audiobook not found", "Book information", "Book title", "Author", "Description", "Release year", "Genre", "Tap the cover to change it", "Parts", "Part title", "Part artist", "Part number", "Disc number", "Save")
}
