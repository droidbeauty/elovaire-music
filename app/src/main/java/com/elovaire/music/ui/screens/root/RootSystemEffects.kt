package elovaire.music.droidbeauty.app.ui.screens

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import elovaire.music.droidbeauty.app.core.LibraryActionDependencies
import elovaire.music.droidbeauty.app.core.AndroidCapabilities
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.core.hasLocalNetworkPermission
import elovaire.music.droidbeauty.app.core.requiredAudioPermission
import elovaire.music.droidbeauty.app.data.library.DeviceDeletePlan
import elovaire.music.droidbeauty.app.data.library.DeviceDeleteHandler
import elovaire.music.droidbeauty.app.data.library.LibraryActionController
import elovaire.music.droidbeauty.app.data.library.LibraryRefreshIntent
import elovaire.music.droidbeauty.app.data.library.LibraryRefreshReason
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.mediaStoreDeleteRequest
import kotlinx.coroutines.launch

internal data class RootPermissionState(
    val hasAudioPermission: Boolean,
    val hasLocalNetworkPermission: Boolean,
    val firstLaunchPermissionExperienceActive: Boolean,
    val playFirstLaunchHomeReveal: Boolean,
    val showFirstLaunchPermissionOverlay: Boolean,
)

internal class RootPermissionController internal constructor(
    val state: RootPermissionState,
    private val requestAudioPermissionAction: () -> Unit,
    private val requestLocalNetworkPermissionAction: () -> Unit,
    private val setPlayFirstLaunchHomeRevealAction: (Boolean) -> Unit,
    private val setFirstLaunchPermissionExperienceActiveAction: (Boolean) -> Unit,
) {
    fun requestAudioPermission() = requestAudioPermissionAction()

    fun requestLocalNetworkPermission() = requestLocalNetworkPermissionAction()

    fun onInitialRevealFinished() {
        setPlayFirstLaunchHomeRevealAction(false)
        setFirstLaunchPermissionExperienceActiveAction(false)
    }
}

@Composable
internal fun rememberRootPermissionController(
    libraryActionDependencies: LibraryActionDependencies,
    libraryState: LibraryUiState,
): RootPermissionController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val networkSources by libraryActionDependencies.networkSources.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(context.hasAudioReadPermission()) }
    var hasLocalPermission by remember { mutableStateOf(context.hasLocalNetworkPermission()) }
    var localPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var firstLaunchPermissionExperienceActive by rememberSaveable { mutableStateOf(!hasPermission) }
    var playFirstLaunchHomeReveal by rememberSaveable { mutableStateOf(false) }
    var syncedAudioPermission by remember { mutableStateOf<Boolean?>(null) }

    fun syncAudioPermission(granted: Boolean) {
        hasPermission = granted
        if (syncedAudioPermission != granted) {
            syncedAudioPermission = granted
            libraryActionDependencies.libraryController.onPermissionChanged(granted)
        }
    }

    fun syncLocalNetworkPermission(granted: Boolean): Boolean {
        if (hasLocalPermission == granted) return false
        hasLocalPermission = granted
        return true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        syncAudioPermission(granted)
    }
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        localPermissionRequested = true
        if (syncLocalNetworkPermission(granted) && hasPermission) {
            requestPermissionReconciliation(libraryActionDependencies.libraryController)
        }
    }

    LaunchedEffect(hasPermission) {
        syncAudioPermission(hasPermission)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val refreshedAudioPermission = context.hasAudioReadPermission()
                val refreshedLocalPermission = context.hasLocalNetworkPermission()
                if (hasPermission != refreshedAudioPermission || syncedAudioPermission != refreshedAudioPermission) {
                    syncAudioPermission(refreshedAudioPermission)
                }
                val localPermissionChanged = syncLocalNetworkPermission(refreshedLocalPermission)
                if (refreshedAudioPermission && localPermissionChanged) {
                    requestPermissionReconciliation(libraryActionDependencies.libraryController)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(networkSources, hasPermission, hasLocalPermission, localPermissionRequested) {
        if (
            hasPermission &&
            !hasLocalPermission &&
            !localPermissionRequested &&
            networkSources.any { it.enabled } &&
            AndroidCapabilities.requiresLocalNetworkPermission(android.os.Build.VERSION.SDK_INT)
        ) {
            localPermissionRequested = true
            localNetworkPermissionLauncher.launch(AndroidCapabilities.LOCAL_NETWORK_PERMISSION)
        }
    }
    val showFirstLaunchPermissionOverlay = shouldShowFirstLaunchPermissionOverlay(
        firstLaunchPermissionExperienceActive = firstLaunchPermissionExperienceActive,
        hasAudioPermission = hasPermission,
        isLibraryLoading = libraryState.isLoading,
        songsCount = libraryState.songs.size,
        albumsCount = libraryState.albums.size,
        errorMessage = libraryState.errorMessage,
        playFirstLaunchHomeReveal = playFirstLaunchHomeReveal,
    )

    LaunchedEffect(
        firstLaunchPermissionExperienceActive,
        hasPermission,
        hasLocalPermission,
        libraryState.isLoading,
        libraryState.songs.size,
        libraryState.albums.size,
        libraryState.errorMessage,
    ) {
        if (shouldRevealFirstLaunchHome(
                firstLaunchPermissionExperienceActive = firstLaunchPermissionExperienceActive,
                hasAudioPermission = hasPermission,
                isLibraryLoading = libraryState.isLoading,
                songsCount = libraryState.songs.size,
                albumsCount = libraryState.albums.size,
                errorMessage = libraryState.errorMessage,
            )
        ) {
            playFirstLaunchHomeReveal = true
        }
    }

    val state = remember(
        hasPermission,
        hasLocalPermission,
        firstLaunchPermissionExperienceActive,
        playFirstLaunchHomeReveal,
        showFirstLaunchPermissionOverlay,
    ) {
        RootPermissionState(
            hasAudioPermission = hasPermission,
            hasLocalNetworkPermission = hasLocalPermission,
            firstLaunchPermissionExperienceActive = firstLaunchPermissionExperienceActive,
            playFirstLaunchHomeReveal = playFirstLaunchHomeReveal,
            showFirstLaunchPermissionOverlay = showFirstLaunchPermissionOverlay,
        )
    }
    return remember(state) {
        RootPermissionController(
            state = state,
            requestAudioPermissionAction = {
                permissionLauncher.launch(requiredAudioPermission())
            },
            requestLocalNetworkPermissionAction = {
                localPermissionRequested = false
                localNetworkPermissionLauncher.launch(AndroidCapabilities.LOCAL_NETWORK_PERMISSION)
            },
            setPlayFirstLaunchHomeRevealAction = { playFirstLaunchHomeReveal = it },
            setFirstLaunchPermissionExperienceActiveAction = { firstLaunchPermissionExperienceActive = it },
        )
    }
}

private fun requestPermissionReconciliation(controller: LibraryActionController) {
    controller.refresh(
        LibraryRefreshIntent(
            reason = LibraryRefreshReason.PermissionReconciliation,
            showLoadingIndicator = false,
        ),
    )
}

internal class RootDeleteController internal constructor(
    private val deleteSongsAction: (List<Song>) -> Unit,
    private val deleteAlbumAction: (Album) -> Unit,
) {
    fun deleteSongsFromDevice(songs: List<Song>) = deleteSongsAction(songs)

    fun deleteAlbumFromDevice(album: Album) = deleteAlbumAction(album)
}

@Composable
internal fun rememberRootDeleteController(
    deleteCoordinator: DeviceDeleteHandler,
): RootDeleteController {
    val context = LocalContext.current
    val rootScope = androidx.compose.runtime.rememberCoroutineScope()
    var pendingSongDeletion by remember { mutableStateOf<DeviceDeletePlan?>(null) }
    var pendingDeleteStateLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(deleteCoordinator) {
        pendingSongDeletion = deleteCoordinator.restorePendingDeletePlan()
        pendingDeleteStateLoaded = true
    }

    val deleteSongLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        rootScope.launch {
            val pendingDeletion = pendingSongDeletion ?: deleteCoordinator.restorePendingDeletePlan()
            pendingSongDeletion = null
            if (pendingDeletion == null) return@launch
            if (result.resultCode == Activity.RESULT_OK) {
                deleteCoordinator.completeDelete(pendingDeletion)
            } else {
                deleteCoordinator.clearPendingDelete(pendingDeletion.operationId)
            }
        }
    }

    val deleteSongsCallback: (List<Song>) -> Unit = deleteSongsCallback@{ songs ->
        if (!pendingDeleteStateLoaded || pendingSongDeletion != null) return@deleteSongsCallback
        rootScope.launch {
            val deletePlan = deleteCoordinator.prepareSongDeletePlan(songs) ?: return@launch
            val request = mediaStoreDeleteRequest(context, deletePlan.uris) ?: return@launch
            pendingSongDeletion = deletePlan
            deleteSongLauncher.launch(request)
        }
    }

    return remember(deleteCoordinator, context, deleteSongLauncher) {
        RootDeleteController(
            deleteSongsAction = deleteSongsCallback,
            deleteAlbumAction = { album ->
                deleteSongsCallback(album.songs)
            },
        )
    }
}
