package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.platform.ContentIo
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import elovaire.music.droidbeauty.app.platform.mediaStoreWriteRequest
import elovaire.music.droidbeauty.app.platform.safTreeUriForDocument
import elovaire.music.droidbeauty.app.platform.takePersistableTreeWritePermission
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorPlatformAction
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorSaveOutcome
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorScreen
import elovaire.music.droidbeauty.app.ui.screens.tags.AudiobookTagEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun AudiobookTagEditorRouteHost(
    bookKey: String,
    backStackEntry: NavBackStackEntry,
    viewModelFactory: ElovaireViewModelFactory,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
    onSaveSucceeded: (String) -> Unit,
) {
    val viewModel: AudiobookTagEditorViewModel = viewModel(
        viewModelStoreOwner = backStackEntry,
        key = "audiobook_tag_editor_$bookKey",
        factory = viewModelFactory,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AudiobookTagWriteEffects(viewModel, state.platformAction, state.saveOutcome, onSaveSucceeded)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        viewModel.onPickedCoverArt(uri)
    }
    LaunchedEffect(bookKey) { viewModel.loadBook(bookKey) }
    AudiobookTagEditorScreen(
        state = state,
        appLanguage = appLanguage,
        onBack = onBack,
        onSave = viewModel::requestSave,
        onPickCoverArt = { picker.launch(arrayOf("image/*")) },
        onBookTitleChange = viewModel::onBookTitleChange,
        onAuthorChange = viewModel::onAuthorChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onReleaseYearChange = viewModel::onReleaseYearChange,
        onGenreChange = viewModel::onGenreChange,
        onPartTitleChange = viewModel::onPartTitleChange,
        onPartArtistChange = viewModel::onPartArtistChange,
        onPartTrackNumberChange = viewModel::onPartTrackNumberChange,
        onPartDiscNumberChange = viewModel::onPartDiscNumberChange,
    )
}

@Composable
private fun AudiobookTagWriteEffects(
    viewModel: AudiobookTagEditorViewModel,
    platformAction: AudiobookTagEditorPlatformAction?,
    saveOutcome: AudiobookTagEditorSaveOutcome?,
    onSaveSucceeded: (String) -> Unit,
) {
    val context = LocalContext.current
    var pendingWriteOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSafWriteOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    var safWriteAttemptedOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    val mediaStoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        pendingWriteOperationId?.let { viewModel.onWritePermissionResult(it, result.resultCode == Activity.RESULT_OK) }
        pendingWriteOperationId = null
    }
    val safWriteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val operationId = pendingSafWriteOperationId ?: return@rememberLauncherForActivityResult
        pendingSafWriteOperationId = null
        val granted = uri != null && takePersistableTreeWritePermission(context, uri)
        viewModel.onSafWritePermissionResult(
            operationId,
            granted,
            if (uri == null) "Write access was not granted." else "The selected folder did not grant persistent write access.",
        )
        if (!granted) safWriteAttemptedOperationId = null
    }
    LaunchedEffect(platformAction) {
        val action = platformAction ?: return@LaunchedEffect
        viewModel.consumePlatformAction(action.operationId)
        when (action) {
            is AudiobookTagEditorPlatformAction.RequestWritePermission -> {
                val unsupported = action.uris.map { MediaWriteTargetClassifier.classify(context, it) }
                    .filterIsInstance<MediaWriteTarget.Unsupported>().firstOrNull()
                if (unsupported != null) {
                    viewModel.onWritePreflightFailed(action.operationId, unsupported.reason)
                    return@LaunchedEffect
                }
                val safProblem = withContext(Dispatchers.IO) { firstAudiobookSafWriteProblem(context, action.uris) }
                if (safProblem != null) {
                    if (safWriteAttemptedOperationId == action.operationId) {
                        safWriteAttemptedOperationId = null
                        viewModel.onSafWritePermissionResult(action.operationId, false, safProblem.message)
                        return@LaunchedEffect
                    }
                    safWriteAttemptedOperationId = action.operationId
                    pendingSafWriteOperationId = action.operationId
                    runCatching { safWriteLauncher.launch(safTreeUriForDocument(safProblem.uri)) }.onFailure {
                        pendingSafWriteOperationId = null
                        safWriteAttemptedOperationId = null
                        viewModel.onWritePermissionLaunchFailed(action.operationId)
                    }
                    return@LaunchedEffect
                }
                safWriteAttemptedOperationId = null
                pendingWriteOperationId = action.operationId
                runCatching { mediaStoreWriteRequest(context, action.uris) }
                    .onSuccess { request ->
                        if (request == null) {
                            pendingWriteOperationId = null
                            viewModel.onWritePermissionNotRequired(action.operationId)
                        } else {
                            runCatching { mediaStoreLauncher.launch(request) }.onFailure {
                                pendingWriteOperationId = null
                                viewModel.onWritePermissionLaunchFailed(action.operationId)
                            }
                        }
                    }
                    .onFailure {
                        pendingWriteOperationId = null
                        viewModel.onWritePermissionLaunchFailed(action.operationId)
                    }
            }
            is AudiobookTagEditorPlatformAction.RequestRecoverableWritePermission -> {
                pendingWriteOperationId = action.operationId
                runCatching { mediaStoreLauncher.launch(IntentSenderRequest.Builder(action.intentSender).build()) }.onFailure {
                    pendingWriteOperationId = null
                    viewModel.onWritePermissionLaunchFailed(action.operationId)
                }
            }
        }
    }
    LaunchedEffect(saveOutcome) {
        val success = saveOutcome as? AudiobookTagEditorSaveOutcome.Succeeded ?: return@LaunchedEffect
        onSaveSucceeded(success.stableKey)
    }
}

private data class AudiobookSafWriteProblem(val uri: Uri, val message: String)

private fun firstAudiobookSafWriteProblem(context: android.content.Context, uris: List<Uri>): AudiobookSafWriteProblem? {
    val contentIo = ContentIo(context.contentResolver)
    return uris.asSequence()
        .map { MediaWriteTargetClassifier.classify(context, it) }
        .filterIsInstance<MediaWriteTarget.SafDocument>()
        .mapNotNull { target ->
            runCatching { contentIo.requireSafWriteAccess(target.uri) }.exceptionOrNull()?.let { failure ->
                AudiobookSafWriteProblem(target.uri, failure.message ?: "The selected document provider does not allow tag editing.")
            }
        }
        .firstOrNull()
}
