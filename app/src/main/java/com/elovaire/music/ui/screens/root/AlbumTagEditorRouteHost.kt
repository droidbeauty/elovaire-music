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
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorPlatformAction
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorSaveOutcome
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun AlbumTagEditorRouteHost(
    albumId: Long?,
    backStackEntry: NavBackStackEntry,
    viewModelFactory: ElovaireViewModelFactory,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
) {
    val tagEditorViewModel: AlbumTagEditorViewModel = viewModel(
        viewModelStoreOwner = backStackEntry,
        key = "album_tag_editor_${albumId ?: "missing"}",
        factory = viewModelFactory,
    )
    val tagEditorState by tagEditorViewModel.uiState.collectAsStateWithLifecycle()

    AlbumTagWriteEffects(
        viewModel = tagEditorViewModel,
        platformAction = tagEditorState.platformAction,
        saveOutcome = tagEditorState.saveOutcome,
        onBack = onBack,
    )

    val coverArtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        tagEditorViewModel.onPickedCoverArt(uri)
    }

    LaunchedEffect(albumId) {
        if (albumId == null) {
            tagEditorViewModel.clearAlbum()
        } else {
            tagEditorViewModel.loadAlbum(albumId)
        }
    }

    AlbumTagEditorScreen(
        state = tagEditorState,
        appLanguage = appLanguage,
        onBack = onBack,
        onSave = tagEditorViewModel::requestSave,
        onPickCoverArt = {
            coverArtPickerLauncher.launch(arrayOf("image/*"))
        },
        onAlbumTitleChange = tagEditorViewModel::onAlbumTitleChange,
        onAlbumArtistChange = tagEditorViewModel::onAlbumArtistChange,
        onReleaseYearChange = tagEditorViewModel::onReleaseYearChange,
        onGenreChange = tagEditorViewModel::onGenreChange,
        onTrackTitleChange = tagEditorViewModel::onTrackTitleChange,
        onTrackArtistChange = tagEditorViewModel::onTrackArtistChange,
        onTrackNumberChange = tagEditorViewModel::onTrackNumberChange,
        onDiscNumberChange = tagEditorViewModel::onDiscNumberChange,
    )
}

@Composable
private fun AlbumTagWriteEffects(
    viewModel: AlbumTagEditorViewModel,
    platformAction: AlbumTagEditorPlatformAction?,
    saveOutcome: AlbumTagEditorSaveOutcome?,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingWriteOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSafWriteOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    var safWriteAttemptedOperationId by rememberSaveable { mutableStateOf<String?>(null) }
    val mediaStoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        pendingWriteOperationId?.let { operationId ->
            viewModel.onWritePermissionResult(operationId, result.resultCode == Activity.RESULT_OK)
        }
        pendingWriteOperationId = null
    }
    val safWriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        val operationId = pendingSafWriteOperationId ?: return@rememberLauncherForActivityResult
        pendingSafWriteOperationId = null
        val granted = uri != null && takePersistableTreeWritePermission(context, uri)
        viewModel.onSafWritePermissionResult(
            operationId = operationId,
            granted = granted,
            failureMessage = if (uri == null) {
                "Write access was not granted."
            } else {
                "The selected folder did not grant persistent write access."
            },
        )
        if (!granted) safWriteAttemptedOperationId = null
    }
    LaunchedEffect(platformAction) {
        val action = platformAction ?: return@LaunchedEffect
        viewModel.consumePlatformAction(action.operationId)
        when (action) {
            is AlbumTagEditorPlatformAction.RequestWritePermission -> {
                val unsupported = action.uris
                        .map { MediaWriteTargetClassifier.classify(context, it) }
                        .filterIsInstance<MediaWriteTarget.Unsupported>()
                        .firstOrNull()
                if (unsupported != null) {
                    viewModel.onWritePreflightFailed(action.operationId, unsupported.reason)
                    return@LaunchedEffect
                }
                val safProblem = withContext(Dispatchers.IO) {
                    firstSafWriteProblem(context, action.uris)
                }
                if (safProblem != null) {
                    if (safWriteAttemptedOperationId == action.operationId) {
                        safWriteAttemptedOperationId = null
                        viewModel.onSafWritePermissionResult(
                            action.operationId,
                            granted = false,
                            failureMessage = safProblem.message,
                        )
                        return@LaunchedEffect
                    }
                    safWriteAttemptedOperationId = action.operationId
                    pendingSafWriteOperationId = action.operationId
                    runCatching {
                        safWriteLauncher.launch(safTreeUriForDocument(safProblem.uri))
                    }.onFailure {
                        pendingSafWriteOperationId = null
                        safWriteAttemptedOperationId = null
                        viewModel.onWritePermissionLaunchFailed(action.operationId)
                    }
                    return@LaunchedEffect
                }
                safWriteAttemptedOperationId = null
                pendingWriteOperationId = action.operationId
                val requestResult = runCatching { mediaStoreWriteRequest(context, action.uris) }
                if (requestResult.isFailure) {
                    viewModel.onWritePermissionLaunchFailed(action.operationId)
                    pendingWriteOperationId = null
                    return@LaunchedEffect
                }
                when (val request = requestResult.getOrNull()) {
                    null -> {
                        viewModel.onWritePermissionNotRequired(action.operationId)
                        pendingWriteOperationId = null
                    }
                    else -> runCatching { mediaStoreLauncher.launch(request) }.onFailure {
                        viewModel.onWritePermissionLaunchFailed(action.operationId)
                        pendingWriteOperationId = null
                    }
                }
            }
            is AlbumTagEditorPlatformAction.RequestRecoverableWritePermission -> {
                pendingWriteOperationId = action.operationId
                runCatching {
                    mediaStoreLauncher.launch(IntentSenderRequest.Builder(action.intentSender).build())
                }.onFailure {
                    viewModel.onWritePermissionLaunchFailed(action.operationId)
                    pendingWriteOperationId = null
                }
            }
        }
    }

    LaunchedEffect(saveOutcome) {
        if (saveOutcome is AlbumTagEditorSaveOutcome.Succeeded) {
            onBack()
        }
    }
}

private data class SafWriteProblem(
    val uri: Uri,
    val message: String,
)

private fun firstSafWriteProblem(
    context: android.content.Context,
    uris: List<Uri>,
): SafWriteProblem? {
    val contentIo = ContentIo(context.contentResolver)
    return uris.asSequence()
        .map { MediaWriteTargetClassifier.classify(context, it) }
        .filterIsInstance<MediaWriteTarget.SafDocument>()
        .mapNotNull { target ->
            runCatching { contentIo.requireSafWriteAccess(target.uri) }
                .exceptionOrNull()
                ?.let { failure ->
                    SafWriteProblem(
                        uri = target.uri,
                        message = failure.message ?: "The selected document provider does not allow tag editing.",
                    )
                }
        }
        .firstOrNull()
}
