package elovaire.music.droidbeauty.app.ui.screens.tags

import elovaire.music.droidbeauty.app.ui.i18n.*

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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import elovaire.music.droidbeauty.app.ui.screens.sharedTopBarOccupiedHeight
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
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val contentTop = sharedTopBarOccupiedHeight() + ElovaireSpacing.sectionVerticalGap
    LaunchedEffect(state.saveFailures, state.statusMessage, state.isSaving) {
        if (!state.isSaving && (state.saveFailures.isNotEmpty() || state.statusMessage != null)) {
            listState.animateScrollToItem(0)
        }
    }
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
                    top = contentTop,
                    end = 18.dp,
                    bottom = 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.validationErrors.isNotEmpty() || state.saveFailures.isNotEmpty() || state.statusMessage != null) {
                    item(key = "feedback") {
                        AudiobookTagEditorFeedback(state, appLanguage)
                    }
                }
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
                        enabled = !state.isSaving,
                        onPartTitleChange = onPartTitleChange,
                        onPartArtistChange = onPartArtistChange,
                        onPartTrackNumberChange = onPartTrackNumberChange,
                        onPartDiscNumberChange = onPartDiscNumberChange,
                    )
                }
            }
        }
        if (state.originalBook != null && !state.isLoading) {
            FastScrollbar(state = listState, topInset = contentTop, bottomInset = 28.dp)
        }
        DetailListTopBar(
            title = copy.editorTitle,
            subtitle = state.bookTitle.takeIf { it.isNotBlank() },
            onBack = onBack,
            actions = if (state.originalBook != null) {
                listOf(
                    TopBarActionSpec(
                        iconResId = R.drawable.ic_lucide_check,
                        contentDescription = copy.save,
                        onClick = {
                            focusManager.clearFocus()
                            keyboard?.hide()
                            onSave()
                        },
                        enabled = state.canSave && !state.isSaving,
                    ),
                )
            } else {
                emptyList()
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
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
                        .elovaireClickable(enabled = !state.isSaving, onClick = onPickCoverArt),
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
            AudiobookEditorTextField(state.bookTitle, onBookTitleChange, copy.bookTitle, Modifier.fillMaxWidth(), enabled = !state.isSaving)
            AudiobookEditorTextField(state.author, onAuthorChange, copy.author, Modifier.fillMaxWidth(), enabled = !state.isSaving)
            AudiobookEditorTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = copy.description,
                enabled = !state.isSaving,
                singleLine = false,
                minLines = 4,
                maxLines = 8,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AudiobookEditorTextField(state.releaseYear, onReleaseYearChange, copy.year, Modifier.weight(1f), enabled = !state.isSaving, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                AudiobookEditorTextField(state.genre, onGenreChange, copy.genre, Modifier.weight(1f), enabled = !state.isSaving)
            }
        }
    }
}

@Composable
private fun AudiobookPhysicalPartCard(
    index: Int,
    part: EditableAudiobookPartState,
    copy: AudiobookTagEditorCopy,
    enabled: Boolean,
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
            AudiobookEditorTextField(part.title, { onPartTitleChange(part.songId, it) }, copy.partTitle, Modifier.fillMaxWidth(), enabled = enabled)
            AudiobookEditorTextField(part.artist, { onPartArtistChange(part.songId, it) }, copy.partAuthor, Modifier.fillMaxWidth(), enabled = enabled)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AudiobookEditorTextField(part.trackNumber, { onPartTrackNumberChange(part.songId, it) }, copy.partNumber, Modifier.weight(1f), enabled = enabled, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                AudiobookEditorTextField(part.discNumber, { onPartDiscNumberChange(part.songId, it) }, copy.discNumber, Modifier.weight(1f), enabled = enabled, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}

@Composable
private fun AudiobookEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        shape = RoundedCornerShape(ElovaireRadii.input),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            disabledContainerColor = colors.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurface.copy(alpha = 0.7f),
            cursorColor = colors.primary,
        ),
    )
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
    AppLanguage.Albanian -> AlbanianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Estonian -> EstonianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Greek -> GreekTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Latvian -> LatvianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Serbian -> SerbianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Thai -> ThaiTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Czech -> CzechTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Danish -> DanishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Dutch -> DutchTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Hindi -> HindiTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Swedish -> SwedishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.French -> FrenchTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.German -> GermanTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Italian -> ItalianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Russian -> RussianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Spanish -> SpanishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Catalan -> CatalanTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Finnish -> FinnishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Romanian -> RomanianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Polish -> PolishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Slovak -> SlovakTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Croatian -> CroatianTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Korean -> KoreanTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Malay -> MalayTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Bengali -> BengaliTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.Urdu -> UrduTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
    AppLanguage.English -> EnglishTranslations.audiobookTagEditorScreen_audiobookTagEditorCopy_1
}
