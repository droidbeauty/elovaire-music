package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelectionResolver
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.libraryFoldersCopy
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.interaction.elovairePressScale
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.theme.DestructiveRed
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import elovaire.music.droidbeauty.app.ui.theme.elovaireScaledSp

@Composable
internal fun LibraryFoldersScreen(
    appLanguage: AppLanguage,
    folders: List<LibraryFolderSelection>,
    songs: List<Song>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAddFolder: (Uri) -> Unit,
    onRemoveFolder: (LibraryFolderSelection) -> Unit,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    val copy = remember(appLanguage) { libraryFoldersCopy(appLanguage) }
    val listState = rememberElovaireLazyListState("library_folders_screen")
    val songCountsByFolder = remember(folders, songs) {
        folders.associateWith { folder -> songs.countInFolder(folder) }
    }
    var editMode by rememberSaveable { mutableStateOf(false) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
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
            if (folders.isEmpty()) {
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
                        subtitle = if (unavailable) copy.unavailableSubtitle else folder.path,
                        songCountLabel = localizedCountLabel(songCountsByFolder[folder] ?: 0, "song", appLanguage),
                        trailingLabel = if (unavailable) copy.unavailable else null,
                        showRemove = editMode,
                        iconResId = R.drawable.ic_lucide_library,
                        onClick = {},
                        onLongClick = { editMode = true },
                        onRemove = {
                            folder.uri?.let { uri ->
                                runCatching {
                                    context.contentResolver.releasePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                    )
                                }
                            }
                            onRemoveFolder(folder)
                        },
                    )
                }
            }
        }
        AddFolderPill(
            text = copy.addFolder,
            onClick = { folderPicker.launch(null) },
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
                text = copy.removalSafety,
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
            .elovairePressScale(
                pressedScale = 0.9f,
                animationSpec = ElovaireMotion.bounceSpringSpec(),
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
    val folderPath = LibraryFolderSelectionResolver.normalizedPathKey(folder.path)
    if (folderPath.isBlank()) return 0
    return count { song ->
        val songPath = song.libraryPath?.let(LibraryFolderSelectionResolver::normalizedPathKey) ?: return@count false
        songPath == folderPath || songPath.startsWith("$folderPath/")
    }
}

private fun LibraryFolderSelection.isAvailable(context: Context): Boolean {
    val hasUriAccess = uri == null || context.contentResolver.persistedUriPermissions.any { permission ->
        permission.uri == uri && permission.isReadPermission
    }
    val hasPathAccess = path.isBlank() || java.io.File(path).let { it.exists() && it.isDirectory }
    return hasUriAccess && hasPathAccess
}

@Composable
internal fun SettingNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
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
