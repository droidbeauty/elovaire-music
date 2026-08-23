package elovaire.music.droidbeauty.app.ui.screens

import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import elovaire.music.droidbeauty.app.ui.i18n.libraryFoldersCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp
import elovaire.music.droidbeauty.app.platform.takePersistableTreePermission
import elovaire.music.droidbeauty.app.platform.releasePersistableTreePermission
import java.util.UUID

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
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val songCountsByFolder = remember(folders, songs) {
        folders.associateWith { folder -> songs.countInFolder(folder) }
    }
    var editMode by rememberSaveable { mutableStateOf(false) }
    var showSourceChooser by rememberSaveable { mutableStateOf(false) }
    var showNetworkEditor by rememberSaveable { mutableStateOf(false) }
    var editingNetworkSource by remember { mutableStateOf<NetworkLibrarySource?>(null) }
    var pendingNetworkRemoval by remember { mutableStateOf<NetworkLibrarySource?>(null) }
    BackHandler(enabled = editMode) {
        editMode = false
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        takePersistableTreePermission(context, uri)
        onAddFolder(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        text = "Network sources",
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
                        NetworkAvailability.Available -> "Available"
                        NetworkAvailability.Checking -> "Testing..."
                        NetworkAvailability.AuthenticationRequired -> "Sign in"
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
        AddFolderPill(
            text = "Add source",
            onClick = { showSourceChooser = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding + navigationBarInsetDp() + 20.dp),
        )
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
            title = "Library",
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
        if (showSourceChooser) {
            AlertDialog(
                onDismissRequest = { showSourceChooser = false },
                title = { Text("Add library source") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showSourceChooser = false
                                folderPicker.launch(null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(copy.addFolder) }
                        TextButton(
                            onClick = {
                                showSourceChooser = false
                                editingNetworkSource = null
                                showNetworkEditor = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("NAS or WebDAV") }
                    }
                },
                confirmButton = {},
            )
        }
        if (showNetworkEditor) {
            NetworkSourceEditorDialog(
                source = editingNetworkSource,
                probeResults = networkProbeResults,
                onDismiss = {
                    showNetworkEditor = false
                    editingNetworkSource = null
                },
                onSave = { source, credentials ->
                    onAddNetworkSource(source, credentials)
                },
            )
        }
        pendingNetworkRemoval?.let { source ->
            AlertDialog(
                onDismissRequest = { pendingNetworkRemoval = null },
                title = { Text("Remove network source?") },
                text = { Text("This removes the source from the library. Files on the network are not deleted.") },
                dismissButton = {
                    TextButton(onClick = { pendingNetworkRemoval = null }) { Text("Cancel") }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingNetworkRemoval = null
                            onRemoveNetworkSource(source)
                        },
                    ) { Text("Remove") }
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
private fun NetworkSourceEditorDialog(
    source: NetworkLibrarySource?,
    probeResults: Map<String, NetworkProbeResult>,
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
    LaunchedEffect(probeResult?.availability) {
        if (probeResult?.availability == NetworkAvailability.Available) onDismiss()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Network library") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { protocolName = NetworkLibraryProtocol.Smb.name }) { Text("SMB2/3") }
                    TextButton(onClick = { protocolName = NetworkLibraryProtocol.WebDav.name }) { Text("WebDAV HTTPS") }
                }
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it },
                    label = { Text(if (protocol == NetworkLibraryProtocol.Smb) "Server" else "HTTPS server") },
                    placeholder = { Text(if (protocol == NetworkLibraryProtocol.Smb) "192.168.1.20" else "https://nas.example.com/music") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text(if (protocol == NetworkLibraryProtocol.Smb) "Share / path" else "Path") },
                    placeholder = { Text(if (protocol == NetworkLibraryProtocol.Smb) "Music" else "") },
                    singleLine = true,
                )
                OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true)
                if (protocol == NetworkLibraryProtocol.Smb) {
                    OutlinedTextField(domain, { domain = it }, label = { Text("Domain / workgroup (optional)") }, singleLine = true)
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
                probeResult?.let { result ->
                    if (result.availability != NetworkAvailability.Checking) {
                        Text(
                            text = when (result.availability) {
                                NetworkAvailability.Available -> "Connection available"
                                NetworkAvailability.AuthenticationRequired -> "Authentication required"
                                NetworkAvailability.Offline -> "Host is unreachable"
                                NetworkAvailability.Misconfigured -> "Check the server and path"
                                NetworkAvailability.Unavailable -> "Network source is unavailable"
                                NetworkAvailability.Checking -> "Testing connection..."
                            },
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
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(
                onClick = {
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
                },
                enabled = canSave && !isChecking,
            ) { Text(if (isChecking) "Testing..." else "Test and save") }
        },
    )
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
                contentDescription = "Remove",
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
