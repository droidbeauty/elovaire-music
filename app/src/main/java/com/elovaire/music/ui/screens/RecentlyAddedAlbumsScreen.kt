package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.ui.components.ArtworkImage
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.searchCopy
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii

private enum class RecentlyAddedViewMode {
    List,
    Grid,
}

@Composable
@Suppress("LongMethod")
internal fun RecentlyAddedAlbumsScreen(
    albums: List<Album>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
) {
    val language = LocalAppLanguage.current
    val searchCopy = remember(language) { searchCopy(language) }
    var query by rememberSaveable { mutableStateOf("") }
    var viewMode by rememberSaveable { mutableStateOf(RecentlyAddedViewMode.Grid) }
    val matchingAlbums = remember(albums, query) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            albums
        } else {
            albums.filter { album ->
                album.title.contains(normalizedQuery, ignoreCase = true) ||
                    album.artist.contains(normalizedQuery, ignoreCase = true) ||
                    album.songs.any { song -> song.title.contains(normalizedQuery, ignoreCase = true) }
            }
        }
    }
    val listState = rememberElovaireLazyListState("recently_added_albums_list")
    val gridState = rememberElovaireLazyGridState("recently_added_albums_grid")
    val contentTopInset = detailTopBarOccupiedHeight() + 92.dp

    Box(modifier = Modifier.fillMaxSize()) {
        if (matchingAlbums.isEmpty()) {
            RecentlyAddedEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentTopInset,
                        bottom = bottomPadding,
                    ),
            )
        } else {
            when (viewMode) {
                RecentlyAddedViewMode.List -> {
                    LazyColumn(
                        state = listState,
                        overscrollEffect = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .ensureSingleItemRubberBand(listState),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = contentTopInset,
                            end = 20.dp,
                            bottom = bottomPadding + 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        itemsIndexed(
                            items = matchingAlbums,
                            key = { _, album -> album.id },
                            contentType = { _, _ -> "recently_added_album_row" },
                        ) { index, album ->
                            RecentlyAddedAlbumRow(
                                album = album,
                                onClick = { onAlbumSelected(album, ExpandOrigin()) },
                            )
                            if (index != matchingAlbums.lastIndex) DividerLine()
                        }
                    }
                    FastScrollbar(
                        state = listState,
                        topInset = contentTopInset,
                        bottomInset = bottomPadding + 16.dp,
                    )
                }

                RecentlyAddedViewMode.Grid -> {
                    LazyVerticalGrid(
                        state = gridState,
                        overscrollEffect = null,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .ensureSingleItemRubberBand(gridState),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = contentTopInset,
                            end = 20.dp,
                            bottom = bottomPadding + 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = matchingAlbums,
                            key = { it.id },
                            contentType = { "recently_added_album_grid" },
                        ) { album ->
                            RecentlyAddedAlbumGridCard(
                                album = album,
                                onClick = { onAlbumSelected(album, ExpandOrigin()) },
                            )
                        }
                    }
                    FastScrollbar(
                        state = gridState,
                        topInset = contentTopInset,
                        bottomInset = bottomPadding + 16.dp,
                    )
                }
            }
        }

        RecentlyAddedSearchControls(
            query = query,
            searchPlaceholder = searchCopy.placeholder,
            viewMode = viewMode,
            onQueryChange = { query = it },
            onClearQuery = { query = "" },
            onViewModeChanged = { viewMode = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = detailTopBarOccupiedHeight() + 8.dp,
                    end = 20.dp,
                ),
        )
        DetailListTopBar(
            title = miscPhrase(language, MiscPhrase.RecentlyAdded),
            subtitle = localizedCountLabel(matchingAlbums.size, "album", language),
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun RecentlyAddedSearchControls(
    query: String,
    searchPlaceholder: String,
    viewMode: RecentlyAddedViewMode,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onViewModeChanged: (RecentlyAddedViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(ElovaireRadii.input),
            singleLine = true,
            placeholder = { Text(searchPlaceholder) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_x),
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onClearQuery),
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RecentlyAddedViewButton(
                iconResId = R.drawable.ic_lucide_list,
                selected = viewMode == RecentlyAddedViewMode.List,
                contentDescription = "List view",
                onClick = { onViewModeChanged(RecentlyAddedViewMode.List) },
            )
            RecentlyAddedViewButton(
                iconResId = R.drawable.ic_lucide_grid_2x2,
                selected = viewMode == RecentlyAddedViewMode.Grid,
                contentDescription = "Grid view",
                onClick = { onViewModeChanged(RecentlyAddedViewMode.Grid) },
            )
        }
    }
}

@Composable
private fun RecentlyAddedViewButton(
    iconResId: Int,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = RoundedCornerShape(ElovaireRadii.button),
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun RecentlyAddedEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No matches found",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add music to your device to see something here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun RecentlyAddedAlbumGridCard(
    album: Album,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            uri = album.artUri,
            title = album.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = ElovaireRadii.artwork,
            showArtworkGlow = true,
        )
        Column(
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = album.artist,
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecentlyAddedAlbumRow(
    album: Album,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtworkImage(
            uri = album.artUri,
            title = album.title,
            modifier = Modifier.size(62.dp),
            cornerRadius = ElovaireRadii.artworkSmall,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = album.artist,
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${localizedCountLabel(album.songCount, "track", language)}  •  ${formatDuration(album.durationMs)}",
                style = MaterialTheme.typography.labelLarge,
                color = readableSecondaryTextColor().copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_lucide_chevron_left),
            contentDescription = null,
            tint = readableMutedIconColor().copy(alpha = 0.55f),
            modifier = Modifier.size(18.dp),
        )
    }
}
