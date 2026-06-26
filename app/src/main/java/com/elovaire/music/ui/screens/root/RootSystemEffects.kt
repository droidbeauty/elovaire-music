package elovaire.music.droidbeauty.app.ui.screens

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.core.hasNotificationPostingPermission
import elovaire.music.droidbeauty.app.core.requiredAudioPermission
import elovaire.music.droidbeauty.app.data.library.DeviceDeleteCoordinator
import elovaire.music.droidbeauty.app.data.library.DeviceDeletePlan
import elovaire.music.droidbeauty.app.data.library.LibraryUiState
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.mediaStoreDeleteRequest
import elovaire.music.droidbeauty.app.ui.components.invalidateArtworkCaches
import kotlinx.coroutines.launch

internal data class RootPermissionState(
    val hasAudioPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val firstLaunchPermissionExperienceActive: Boolean,
    val playFirstLaunchHomeReveal: Boolean,
    val showFirstLaunchPermissionOverlay: Boolean,
)

internal class RootPermissionController internal constructor(
    val state: RootPermissionState,
    private val requestAudioPermissionAction: () -> Unit,
    private val requestNotificationPermissionAction: () -> Unit,
    private val setPlayFirstLaunchHomeRevealAction: (Boolean) -> Unit,
    private val setFirstLaunchPermissionExperienceActiveAction: (Boolean) -> Unit,
) {
    fun requestAudioPermission() = requestAudioPermissionAction()

    fun requestNotificationPermission() = requestNotificationPermissionAction()

    fun onInitialRevealFinished() {
        setPlayFirstLaunchHomeRevealAction(false)
        setFirstLaunchPermissionExperienceActiveAction(false)
    }
}

@Composable
internal fun rememberRootPermissionController(
    container: AppContainer,
    libraryState: LibraryUiState,
): RootPermissionController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(context.hasAudioReadPermission()) }
    var hasNotificationPermission by remember { mutableStateOf(context.hasNotificationPostingPermission()) }
    var hasRequestedAudioPermission by rememberSaveable { mutableStateOf(false) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var firstLaunchPermissionExperienceActive by rememberSaveable {
        mutableStateOf(!hasPermission)
    }
    var playFirstLaunchHomeReveal by rememberSaveable {
        mutableStateOf(false)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasNotificationPermission = granted
        container.setNotificationsEnabled(granted)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        container.libraryRepository.onPermissionChanged(granted)
        if (
            granted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission &&
            !hasRequestedNotificationPermission
        ) {
            hasRequestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasPermission, hasNotificationPermission) {
        container.libraryRepository.onPermissionChanged(hasPermission)
        if (!hasPermission && !hasRequestedAudioPermission) {
            hasRequestedAudioPermission = true
            permissionLauncher.launch(requiredAudioPermission())
        } else if (
            hasPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission &&
            !hasRequestedNotificationPermission
        ) {
            hasRequestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasNotificationPermission) {
        container.setNotificationsEnabled(hasNotificationPermission)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val refreshedAudioPermission = context.hasAudioReadPermission()
                val refreshedNotificationPermission = context.hasNotificationPostingPermission()
                if (hasPermission != refreshedAudioPermission) {
                    hasPermission = refreshedAudioPermission
                    container.libraryRepository.onPermissionChanged(refreshedAudioPermission)
                }
                if (hasNotificationPermission != refreshedNotificationPermission) {
                    hasNotificationPermission = refreshedNotificationPermission
                    container.setNotificationsEnabled(refreshedNotificationPermission)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val showFirstLaunchPermissionOverlay =
        firstLaunchPermissionExperienceActive &&
            (
                !hasPermission ||
                    libraryState.isLoading ||
                    (
                        libraryState.songs.isEmpty() &&
                            libraryState.albums.isEmpty() &&
                            libraryState.errorMessage == null &&
                            !playFirstLaunchHomeReveal
                        )
                )

    LaunchedEffect(
        firstLaunchPermissionExperienceActive,
        hasPermission,
        libraryState.isLoading,
        libraryState.songs.size,
        libraryState.albums.size,
        libraryState.errorMessage,
    ) {
        if (
            firstLaunchPermissionExperienceActive &&
            hasPermission &&
            !libraryState.isLoading &&
            (libraryState.songs.isNotEmpty() || libraryState.albums.isNotEmpty() || libraryState.errorMessage != null)
        ) {
            playFirstLaunchHomeReveal = true
        }
    }

    val state = remember(
        hasPermission,
        hasNotificationPermission,
        firstLaunchPermissionExperienceActive,
        playFirstLaunchHomeReveal,
        showFirstLaunchPermissionOverlay,
    ) {
        RootPermissionState(
            hasAudioPermission = hasPermission,
            hasNotificationPermission = hasNotificationPermission,
            firstLaunchPermissionExperienceActive = firstLaunchPermissionExperienceActive,
            playFirstLaunchHomeReveal = playFirstLaunchHomeReveal,
            showFirstLaunchPermissionOverlay = showFirstLaunchPermissionOverlay,
        )
    }
    return remember(state) {
        RootPermissionController(
            state = state,
            requestAudioPermissionAction = { permissionLauncher.launch(requiredAudioPermission()) },
            requestNotificationPermissionAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            setPlayFirstLaunchHomeRevealAction = { playFirstLaunchHomeReveal = it },
            setFirstLaunchPermissionExperienceActiveAction = { firstLaunchPermissionExperienceActive = it },
        )
    }
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
    container: AppContainer,
): RootDeleteController {
    val context = LocalContext.current
    val rootScope = androidx.compose.runtime.rememberCoroutineScope()
    val deleteCoordinator = remember(container, context) {
        DeviceDeleteCoordinator(
            context = context,
            libraryRepository = container.libraryRepository,
            playbackManager = container.playbackManager,
            preferenceStore = container.preferenceStore,
            invalidateArtwork = ::invalidateArtworkCaches,
        )
    }
    var pendingSongDeletion by remember { mutableStateOf<DeviceDeletePlan?>(null) }

    val deleteSongLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val pendingDeletion = pendingSongDeletion ?: return@rememberLauncherForActivityResult
        pendingSongDeletion = null
        if (result.resultCode == Activity.RESULT_OK) {
            rootScope.launch {
                deleteCoordinator.completeDelete(pendingDeletion)
            }
        }
    }

    val deleteSongsCallback: (List<Song>) -> Unit = deleteSongsCallback@{ songs ->
        rootScope.launch {
            val deletePlan = deleteCoordinator.prepareSongDeletePlan(songs) ?: return@launch
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pendingSongDeletion = deletePlan
                deleteSongLauncher.launch(
                    mediaStoreDeleteRequest(context, deletePlan.uris),
                )
            } else {
                deleteCoordinator.performLegacyDelete(deletePlan).onSuccess {
                    deleteCoordinator.completeDelete(deletePlan)
                }.onFailure { throwable ->
                    val intentSender = when (throwable) {
                        is android.app.RecoverableSecurityException -> throwable.userAction.actionIntent.intentSender
                        else -> null
                    }
                    if (intentSender != null) {
                        pendingSongDeletion = deletePlan
                        deleteSongLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build(),
                        )
                    }
                }
            }
        }
    }

    return remember(container, context, deleteSongLauncher) {
        RootDeleteController(
            deleteSongsAction = deleteSongsCallback,
            deleteAlbumAction = { album ->
                deleteSongsCallback(album.songs)
            },
        )
    }
}
