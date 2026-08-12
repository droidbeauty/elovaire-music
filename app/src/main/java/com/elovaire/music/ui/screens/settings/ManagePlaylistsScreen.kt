package elovaire.music.droidbeauty.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.playlists.deserializePlaylists
import elovaire.music.droidbeauty.app.data.playlists.serializePlaylists
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.playlistManagementCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun ManagePlaylistsScreen(
    appLanguage: AppLanguage,
    playlists: List<Playlist>,
    songsById: Map<Long, Song>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onImportPlaylists: (List<Playlist>) -> PlaylistMutationRequest,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val copy = remember(appLanguage) { playlistManagementCopy(appLanguage) }
    val userPlaylists = remember(playlists) { playlists.filterNot(Playlist::isSystem) }
    val listState = rememberElovaireLazyListState("manage_playlists_screen")
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val payload = pendingExport ?: return@rememberLauncherForActivityResult
        pendingExport = null
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                writePlaylistFile(context, uri, payload)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val imported = readPlaylistFile(context, uri)
                if (imported.isNotEmpty()) {
                    onImportPlaylists(imported).await()
                }
            }
        }
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
                top = topBarOccupiedHeight() + 16.dp,
                end = 18.dp,
                bottom = bottomPadding + navigationBarInsetDp() + 98.dp,
            ),
        ) {
            if (userPlaylists.isEmpty()) {
                item {
                    Text(
                        text = copy.empty,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 44.dp),
                    )
                }
            } else {
                itemsIndexed(
                    items = userPlaylists,
                    key = { _, playlist -> playlist.id },
                    contentType = { _, _ -> "manage-playlist-row" },
                ) { index, playlist ->
                    ManagePlaylistRow(
                        playlist = playlist,
                        previewSongs = playlist.songIds.mapNotNull(songsById::get),
                        appLanguage = appLanguage,
                        onExport = {
                            pendingExport = serializePlaylists(listOf(playlist))
                            exportLauncher.launch(playlistExportFileName(playlist.name))
                        },
                    )
                    if (index != userPlaylists.lastIndex) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .height(1.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = bottomPadding + 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlaylistActionPill(
                text = copy.importAction,
                accent = false,
                onClick = { importLauncher.launch(arrayOf("application/octet-stream", "text/plain")) },
            )
            PlaylistActionPill(
                text = copy.exportAll,
                accent = true,
                onClick = {
                    if (userPlaylists.isNotEmpty()) {
                        pendingExport = serializePlaylists(userPlaylists)
                        exportLauncher.launch("elovaire-playlists.elovaire")
                    }
                },
            )
        }

        PinnedBackTopBar(
            title = copy.title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ManagePlaylistRow(
    playlist: Playlist,
    previewSongs: List<Song>,
    appLanguage: AppLanguage,
    onExport: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PlaylistArtworkPreview(
            songs = previewSongs,
            title = playlist.name,
            modifier = Modifier.size(62.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = localizedCountLabel(playlist.songIds.size, "song", appLanguage),
                style = secondaryBodyTextStyle().copy(fontSize = MaterialTheme.typography.labelLarge.fontSize),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            )
        }
        Surface(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onExport),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_file_up),
                    contentDescription = "Export playlist",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaylistActionPill(
    text: String,
    accent: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(ElovaireRadii.pill))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ElovaireRadii.pill),
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        contentColor = if (accent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        )
    }
}

private fun playlistExportFileName(name: String): String {
    val safeName = name.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_')
    return "${safeName.ifBlank { "playlist" }}.elovaire"
}

private fun writePlaylistFile(
    context: android.content.Context,
    uri: Uri,
    payload: String,
) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        output.write(payload.toByteArray(Charsets.UTF_8))
    }
}

private fun readPlaylistFile(
    context: android.content.Context,
    uri: Uri,
): List<Playlist> {
    val content = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes().toString(Charsets.UTF_8)
    } ?: return emptyList()
    return deserializePlaylists(content)
}
