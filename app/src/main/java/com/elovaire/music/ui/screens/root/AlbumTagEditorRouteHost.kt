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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.platform.mediaStoreWriteRequest
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorEvent
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorScreen

@Composable
internal fun AlbumTagEditorRouteHost(
    albumId: Long,
    backStackEntry: NavBackStackEntry,
    viewModelFactory: ElovaireViewModelFactory,
    appLanguage: AppLanguage,
    onBack: () -> Unit,
) {
    val tagEditorViewModel: AlbumTagEditorViewModel = viewModel(
        viewModelStoreOwner = backStackEntry,
        key = "album_tag_editor_$albumId",
        factory = viewModelFactory,
    )
    val tagEditorState by tagEditorViewModel.uiState.collectAsStateWithLifecycle()
    var pendingWriteRequest by remember(albumId) {
        mutableStateOf<elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest?>(null)
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val albumTagWriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val pendingRequest = pendingWriteRequest ?: return@rememberLauncherForActivityResult
        pendingWriteRequest = null
        tagEditorViewModel.onWritePermissionResult(
            granted = result.resultCode == Activity.RESULT_OK,
            request = pendingRequest,
        )
    }

    val coverArtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        tagEditorViewModel.onPickedCoverArt(uri)
    }

    LaunchedEffect(albumId) {
        tagEditorViewModel.loadAlbum(albumId)
    }

    LaunchedEffect(tagEditorViewModel) {
        tagEditorViewModel.events.collect { event ->
            when (event) {
                is AlbumTagEditorEvent.RequestWritePermission -> {
                    pendingWriteRequest = event.request
                    val requestResult = runCatching {
                        mediaStoreWriteRequest(
                            context = context,
                            uris = event.uris,
                        )
                    }
                    if (requestResult.isFailure) {
                        pendingWriteRequest = null
                        tagEditorViewModel.onWritePermissionResult(
                            granted = false,
                            request = event.request,
                        )
                        return@collect
                    }
                    when (val request = requestResult.getOrNull()) {
                        null -> {
                            pendingWriteRequest = null
                            tagEditorViewModel.onWritePermissionResult(
                                granted = true,
                                request = event.request,
                            )
                        }
                        else -> runCatching {
                            albumTagWriteLauncher.launch(request)
                        }.onFailure {
                            pendingWriteRequest = null
                            tagEditorViewModel.onWritePermissionResult(
                                granted = false,
                                request = event.request,
                            )
                        }
                    }
                }

                is AlbumTagEditorEvent.RequestRecoverableWritePermission -> {
                    pendingWriteRequest = event.request
                    runCatching {
                        albumTagWriteLauncher.launch(
                            IntentSenderRequest.Builder(event.intentSender).build(),
                        )
                    }.onFailure {
                        pendingWriteRequest = null
                        tagEditorViewModel.onWritePermissionResult(
                            granted = false,
                            request = event.request,
                        )
                    }
                }

                AlbumTagEditorEvent.SaveSucceeded -> {
                    onBack()
                }

                is AlbumTagEditorEvent.SavePartiallySucceeded -> Unit
            }
        }
    }

    AlbumTagEditorScreen(
        state = tagEditorState,
        appLanguage = appLanguage,
        onBack = onBack,
        onSave = tagEditorViewModel::requestSave,
        onAutoMatch = tagEditorViewModel::matchOnline,
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
