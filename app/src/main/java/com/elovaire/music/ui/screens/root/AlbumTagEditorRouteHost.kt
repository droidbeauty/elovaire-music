package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val albumTagWriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        tagEditorViewModel.onWritePermissionResult(
            granted = result.resultCode == Activity.RESULT_OK,
        )
    }

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

    LaunchedEffect(tagEditorViewModel) {
        tagEditorViewModel.events.collect { event ->
            when (event) {
                is AlbumTagEditorEvent.RequestWritePermission -> {
                    val requestResult = runCatching {
                        mediaStoreWriteRequest(
                            context = context,
                            uris = event.uris,
                        )
                    }
                    if (requestResult.isFailure) {
                        tagEditorViewModel.onWritePermissionResult(
                            granted = false,
                        )
                        return@collect
                    }
                    when (val request = requestResult.getOrNull()) {
                        null -> {
                            tagEditorViewModel.onWritePermissionResult(
                                granted = true,
                            )
                        }
                        else -> runCatching {
                            albumTagWriteLauncher.launch(request)
                        }.onFailure {
                            tagEditorViewModel.onWritePermissionResult(
                                granted = false,
                            )
                        }
                    }
                }

                is AlbumTagEditorEvent.RequestRecoverableWritePermission -> {
                    runCatching {
                        albumTagWriteLauncher.launch(
                            IntentSenderRequest.Builder(event.intentSender).build(),
                        )
                    }.onFailure {
                        tagEditorViewModel.onWritePermissionResult(
                            granted = false,
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
