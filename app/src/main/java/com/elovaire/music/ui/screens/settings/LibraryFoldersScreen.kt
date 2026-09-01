package elovaire.music.droidbeauty.app.ui.screens
import elovaire.music.droidbeauty.app.ui.screens.common.readableSecondaryTextColor

import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentials
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibraryProtocol
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySource
import elovaire.music.droidbeauty.app.data.library.network.NetworkAvailability
import elovaire.music.droidbeauty.app.data.library.network.NetworkProbeResult
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.commonUiCopy
import elovaire.music.droidbeauty.app.ui.i18n.libraryFoldersCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.NetworkSourcesCopy
import elovaire.music.droidbeauty.app.ui.i18n.networkSourcesCopy
import elovaire.music.droidbeauty.app.ui.i18n.uiPhrase
import elovaire.music.droidbeauty.app.ui.i18n.UiPhrase
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.PopupCardMotionHost
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import elovaire.music.droidbeauty.app.platform.takePersistableTreePermission
import elovaire.music.droidbeauty.app.platform.releasePersistableTreePermission
import java.util.UUID

@OptIn(ExperimentalHazeApi::class)
@Suppress("LongMethod")
@Composable
internal fun LibraryFoldersScreen(
    appLanguage: AppLanguage,
    folders: List<LibraryFolderSelection>,
    networkSources: List<NetworkLibrarySource>,
    networkProbeResults: Map<String, NetworkProbeResult>,
    songs: List<Song>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAddFolder: (Uri) -> Unit,
    onAddNetworkSource: (NetworkLibrarySource, NetworkCredentials) -> Unit,
    onRemoveFolder: (LibraryFolderSelection) -> Unit,
    onRemoveNetworkSource: (NetworkLibrarySource) -> Unit,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    val copy = remember(appLanguage) { libraryFoldersCopy(appLanguage) }
    val networkCopy = remember(appLanguage) { networkSourcesCopy(appLanguage) }
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val sourceChooserHazeState = rememberHazeState()
    val songCountsByFolder = remember(folders, songs) {
        folders.associateWith { folder -> songs.countInFolder(folder) }
    }
    var editMode by rememberSaveable { mutableStateOf(false) }
    var showSourceChooser by rememberSaveable { mutableStateOf(false) }
    var showNetworkEditor by rememberSaveable { mutableStateOf(false) }
    var editingNetworkSource by remember { mutableStateOf<NetworkLibrarySource?>(null) }
    var pendingNetworkRemoval by remember { mutableStateOf<NetworkLibrarySource?>(null) }
    BackHandler(enabled = showSourceChooser) {
        showSourceChooser = false
    }
    BackHandler(enabled = editMode) {
        editMode = false
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (takePersistableTreePermission(context, uri)) {
            onAddFolder(uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (!showNetworkEditor) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(sourceChooserHazeState, zIndex = -1f),
            ) {
            LazyColumn(
                state = listState,
                overscrollEffect = null,
                modifier = Modifier
                    .fillMaxSize()
                    .ensureSingleItemRubberBand(listState),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    top = topBarOccupiedHeight() + 60.dp,
                    end = 18.dp,
                    bottom = bottomPadding + buttonNavigationScrollBoost() + 104.dp,
                ),
            ) {
            if (networkSources.isNotEmpty()) {
                item {
                    Text(
                        text = networkCopy.sectionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                items(
                    items = networkSources,
                    key = NetworkLibrarySource::id,
                    contentType = { "network-library-source-row" },
                ) { source ->
                    val status = when (networkProbeResults[source.id]?.availability) {
                        NetworkAvailability.Available -> networkCopy.available
                        NetworkAvailability.Checking -> networkCopy.checking
                        NetworkAvailability.AuthenticationRequired -> networkCopy.signIn
                        NetworkAvailability.LocalNetworkPermissionRequired -> networkCopy.allowLocalNetwork
                        NetworkAvailability.Offline -> copy.unavailable
                        NetworkAvailability.Misconfigured,
                        NetworkAvailability.Unavailable,
                        -> copy.unavailable
                        null -> null
                    }
                    LibraryFolderListRow(
                        title = source.name,
                        subtitle = "${source.protocol.displayName()} • ${source.server}/${source.shareOrPath}".trimEnd('/'),
                        songCountLabel = localizedCountLabel(
                            songs.count { song -> song.uri.host == source.id },
                            "song",
                            appLanguage,
                        ),
                        trailingLabel = status,
                        showRemove = editMode,
                        removeLabel = networkCopy.remove,
                        iconResId = R.drawable.ic_lucide_library,
                        onClick = {
                            editingNetworkSource = source
                            showNetworkEditor = true
                        },
                        onLongClick = { editMode = true },
                        onRemove = { pendingNetworkRemoval = source },
                    )
                }
            }
            if (folders.isEmpty() && networkSources.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 42.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = copy.noFoldersTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = copy.noFoldersMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                items(
                    items = folders,
                    key = { folder -> folder.uri?.toString() ?: folder.path },
                    contentType = { "library-folder-row" },
                ) { folder ->
                    val unavailable = remember(folder, context) { !folder.isAvailable(context) }
                    LibraryFolderListRow(
                        title = folder.displayName,
                        subtitle = if (unavailable) {
                            copy.unavailableSubtitle
                        } else {
                            folder.path.takeUnless(LibraryFolderSelectionResolver::isUriBackedPath) ?: folder.displayName
                        },
                        songCountLabel = localizedCountLabel(songCountsByFolder[folder] ?: 0, "song", appLanguage),
                        trailingLabel = if (unavailable) copy.unavailable else null,
                        showRemove = editMode,
                        removeLabel = uiPhrase(appLanguage, UiPhrase.Delete),
                        iconResId = R.drawable.ic_lucide_library,
                        onClick = {},
                        onLongClick = { editMode = true },
                        onRemove = {
                            folder.uri?.let { uri -> releasePersistableTreePermission(context, uri) }
                            onRemoveFolder(folder)
                        },
                    )
                }
            }
            }
            AnimatedVisibility(
                visible = !editMode && !showSourceChooser,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPadding + navigationBarInsetDp() + 20.dp),
                enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
                exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
            ) {
                AddFolderPill(
                    text = networkCopy.addSource,
                    onClick = { showSourceChooser = true },
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 18.dp,
                        top = topBarOccupiedHeight(),
                        end = 18.dp,
                    )
                    .height(60.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = copy.removalSafety.trimEnd('.'),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            PinnedBackTopBar(
                title = commonUiCopy(appLanguage).library,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { editMode = !editMode }) {
                    Text(if (editMode) copy.done else copy.edit)
                }
                HeaderIconButton(
                    iconResId = R.drawable.ic_lucide_refresh_ccw,
                    contentDescription = copy.refresh,
                    showBackground = false,
                    onClick = onRefresh,
                )
            }
            }
            LibrarySourceChooserSheet(
                visible = showSourceChooser,
                hazeState = sourceChooserHazeState,
                addFolderLabel = copy.addFolder,
                copy = networkCopy,
                onDismiss = { showSourceChooser = false },
                onAddFolder = {
                    showSourceChooser = false
                    folderPicker.launch(defaultLibraryPickerUri())
                },
                onAddNetwork = {
                    showSourceChooser = false
                    editingNetworkSource = null
                    showNetworkEditor = true
                },
            )
            pendingNetworkRemoval?.let { source ->
                Dialog(onDismissRequest = { pendingNetworkRemoval = null }) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PopupCardMotionHost(
                            visible = true,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        ) {
                            DynamicBackdropSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ElovaireRadii.card),
                                overlayAlpha = 0.6f,
                                borderColor = blurSurfaceBorderColor(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    Text(
                                        text = networkCopy.removeTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    )
                                    Text(
                                        text = networkCopy.removeMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = readableSecondaryTextColor(),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(onClick = { pendingNetworkRemoval = null }) {
                                            Text(uiPhrase(appLanguage, UiPhrase.Cancel))
                                        }
                                        TextButton(
                                            onClick = {
                                                pendingNetworkRemoval = null
                                                onRemoveNetworkSource(source)
                                            },
                                        ) {
                                            Text(networkCopy.remove, color = DestructiveRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            NetworkSourceEditorScreen(
                source = editingNetworkSource,
                probeResults = networkProbeResults,
                bottomPadding = bottomPadding,
                copy = networkCopy,
                onDismiss = {
                    showNetworkEditor = false
                    editingNetworkSource = null
                },
                onSave = { source, credentials ->
                    onAddNetworkSource(source, credentials)
                },
            )
        }
    }
}

@Composable
private fun AddFolderPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .background(MaterialTheme.colorScheme.primary)
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "addFolderPillScale",
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_plus),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }

}

@Composable
@Suppress("LongMethod")
private fun LibrarySourceChooserSheet(
    visible: Boolean,
    hazeState: HazeState,
    addFolderLabel: String,
    copy: NetworkSourcesCopy,
    onDismiss: () -> Unit,
    onAddFolder: () -> Unit,
    onAddNetwork: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(animationSpec = ElovaireMotion.fadeMedium()),
            exit = fadeOut(animationSpec = ElovaireMotion.fadeFast()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        PopupCardMotionHost(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            DynamicBackdropSurface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(
                    topStart = ElovaireRadii.dialog,
                    topEnd = ElovaireRadii.dialog,
                ),
                overlayAlpha = 0.6f,
                borderColor = null,
                hazeState = hazeState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_library),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = copy.addSource,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDismiss,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_x),
                                contentDescription = copy.closeSourcePicker,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    LibrarySourceChoice(
                        iconResId = R.drawable.ic_lucide_library,
                        title = addFolderLabel,
                        subtitle = copy.chooseFolderSubtitle,
                        onClick = onAddFolder,
                    )
                    LibrarySourceChoice(
                        iconResId = R.drawable.ic_about_globe,
                        title = copy.nasTitle,
                        subtitle = copy.nasSubtitle,
                        onClick = onAddNetwork,
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun LibrarySourceChoice(
    @DrawableRes iconResId: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.tile))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "library_source_choice_bump",
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            modifier = Modifier
                .size(18.dp)
                .rotate(180f),
        )
    }
}

@Composable
private fun NetworkSourceEditorScreen(
    source: NetworkLibrarySource?,
    probeResults: Map<String, NetworkProbeResult>,
    bottomPadding: Dp,
    copy: NetworkSourcesCopy,
    onDismiss: () -> Unit,
    onSave: (NetworkLibrarySource, NetworkCredentials) -> Unit,
) {
    var protocolName by rememberSaveable(source?.id) { mutableStateOf(source?.protocol?.name ?: NetworkLibraryProtocol.Smb.name) }
    var name by rememberSaveable(source?.id) { mutableStateOf(source?.name.orEmpty()) }
    var server by rememberSaveable(source?.id) { mutableStateOf(source?.server.orEmpty()) }
    var path by rememberSaveable(source?.id) { mutableStateOf(source?.shareOrPath.orEmpty()) }
    var username by rememberSaveable(source?.id) { mutableStateOf(source?.username.orEmpty()) }
    var domain by rememberSaveable(source?.id) { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val sourceId = rememberSaveable(source?.id) { mutableStateOf(source?.id ?: UUID.randomUUID().toString()) }.value
    val credentialKey = source?.credentialKey ?: "network-credential-$sourceId"
    val protocol = NetworkLibraryProtocol.entries.first { it.name == protocolName }
    val probeResult = probeResults[sourceId]
    val canSave = server.isNotBlank() && path.isNotBlank()
    val isChecking = probeResult?.availability == NetworkAvailability.Checking
    val listState = rememberElovaireLazyListState("network_source_editor", sourceId)
    BackHandler(onBack = onDismiss)
    LaunchedEffect(probeResult?.availability) {
        if (probeResult?.availability == NetworkAvailability.Available) onDismiss()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .ensureSingleItemRubberBand(listState),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = topBarOccupiedHeight() + 20.dp,
                end = 18.dp,
                bottom = bottomPadding + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            networkSourceEditorItems(
                protocol = protocol,
                copy = copy,
                name = name,
                onNameChange = { name = it },
                server = server,
                onServerChange = { server = it },
                path = path,
                onPathChange = { path = it },
                username = username,
                onUsernameChange = { username = it },
                domain = domain,
                onDomainChange = { domain = it },
                password = password,
                onPasswordChange = { password = it },
                probeResult = probeResult,
                onProtocolSelected = { protocolName = it.name },
            )
        }
        PinnedBackTopBar(
            title = copy.editorTitle,
            onBack = onDismiss,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = listOf(
                TopBarActionSpec(
                    iconResId = R.drawable.ic_lucide_check,
                    contentDescription = copy.saveEditor,
                    onClick = {
                        if (canSave && !isChecking) {
                            onSave(
                                NetworkLibrarySource(
                                    id = sourceId,
                                    name = name.trim().ifBlank { server.trim() },
                                    protocol = protocol,
                                    server = server.trim(),
                                    shareOrPath = path.trim(),
                                    username = username.trim(),
                                    credentialKey = credentialKey,
                                ),
                                NetworkCredentials(username.trim(), password, domain.trim().ifBlank { null }),
                            )
                        }
                    },
                ),
            ),
        )
        FastScrollbar(
            state = listState,
            topInset = topBarOccupiedHeight() + 8.dp,
            bottomInset = bottomPadding,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.networkSourceEditorItems(
    protocol: NetworkLibraryProtocol,
    copy: NetworkSourcesCopy,
    name: String,
    onNameChange: (String) -> Unit,
    server: String,
    onServerChange: (String) -> Unit,
    path: String,
    onPathChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    domain: String,
    onDomainChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    probeResult: NetworkProbeResult?,
    onProtocolSelected: (NetworkLibraryProtocol) -> Unit,
) {
    item {
        NetworkProtocolSelector(
            selected = protocol,
            onSelected = onProtocolSelected,
        )
    }
    item {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(copy.name) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    item {
        OutlinedTextField(
            value = server,
            onValueChange = onServerChange,
            label = { Text(if (protocol == NetworkLibraryProtocol.Smb) copy.server else copy.httpsServer) },
            placeholder = { Text(if (protocol == NetworkLibraryProtocol.Smb) "192.168.1.20" else "https://nas.example.com/music") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    item {
        OutlinedTextField(
            value = path,
            onValueChange = onPathChange,
            label = { Text(if (protocol == NetworkLibraryProtocol.Smb) copy.sharePath else copy.path) },
            placeholder = { Text(if (protocol == NetworkLibraryProtocol.Smb) "Music" else "") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    item {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(copy.username) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    if (protocol == NetworkLibraryProtocol.Smb) {
        item {
            OutlinedTextField(
                value = domain,
                onValueChange = onDomainChange,
                label = { Text(copy.domainOptional) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
    item {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(copy.password) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
    }
    probeResult?.let { result ->
        item {
            Text(
                text = networkProbeMessage(result.availability, copy),
                color = if (result.availability == NetworkAvailability.Available) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun networkProbeMessage(
    availability: NetworkAvailability,
    copy: NetworkSourcesCopy,
): String = when (availability) {
    NetworkAvailability.Available -> copy.connectionAvailable
    NetworkAvailability.LocalNetworkPermissionRequired -> copy.allowLocalNetworkSettings
    NetworkAvailability.AuthenticationRequired -> copy.authenticationRequired
    NetworkAvailability.Offline -> copy.hostUnreachable
    NetworkAvailability.Misconfigured -> copy.checkServerPath
    NetworkAvailability.Unavailable -> copy.sourceUnavailable
    NetworkAvailability.Checking -> copy.testingConnection
}

@Composable
private fun NetworkProtocolSelector(
    selected: NetworkLibraryProtocol,
    onSelected: (NetworkLibraryProtocol) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkProtocolChoice(
            protocol = NetworkLibraryProtocol.Smb,
            label = "SMB2/3",
            selected = selected == NetworkLibraryProtocol.Smb,
            onClick = { onSelected(NetworkLibraryProtocol.Smb) },
        )
        Spacer(modifier = Modifier.size(10.dp))
        NetworkProtocolChoice(
            protocol = NetworkLibraryProtocol.WebDav,
            label = "WebDAV HTTPS",
            selected = selected == NetworkLibraryProtocol.WebDav,
            onClick = { onSelected(NetworkLibraryProtocol.WebDav) },
        )
    }
}

@Composable
private fun NetworkProtocolChoice(
    protocol: NetworkLibraryProtocol,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = rememberElovaireInteractionSource()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                },
            )
            .elovaireActionBump(
                interactionSource = interactionSource,
                label = "network_protocol_${protocol.name}_bump",
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun NetworkLibraryProtocol.displayName(): String = when (this) {
    NetworkLibraryProtocol.Smb -> "SMB"
    NetworkLibraryProtocol.WebDav -> "WebDAV"
}

@Composable
private fun LibraryFolderListRow(
    title: String,
    subtitle: String,
    songCountLabel: String?,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    trailingLabel: String? = null,
    showRemove: Boolean = false,
    removeLabel: String,
    onRemove: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ElovaireRadii.card))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = elovaireScaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            songCountLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        trailingLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.error,
            )
        }
        AnimatedVisibility(visible = showRemove) {
            HeaderIconButton(
                iconResId = R.drawable.ic_lucide_x,
                contentDescription = removeLabel,
                showBackground = true,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DestructiveRed.copy(alpha = 0.5f)),
                onClick = onRemove,
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

private fun List<Song>.countInFolder(folder: LibraryFolderSelection): Int {
    val folderPath = folder.uri
        ?.let(LibraryFolderSelectionResolver::safSyntheticRoot)
        ?: LibraryFolderSelectionResolver.normalizedPathKey(folder.path)
    if (folderPath.isBlank()) return 0
    return count { song ->
        val songPath = song.libraryPath?.let(LibraryFolderSelectionResolver::normalizedPathKey) ?: return@count false
        songPath == folderPath || songPath.startsWith("$folderPath/")
    }
}

@Composable
internal fun SettingNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .then(
                testTag?.let {
                    Modifier
                        .semantics { testTagsAsResourceId = true }
                        .testTag(it)
                } ?: Modifier,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            modifier = Modifier
                .size(18.dp)
                .rotate(180f),
        )
    }
}
