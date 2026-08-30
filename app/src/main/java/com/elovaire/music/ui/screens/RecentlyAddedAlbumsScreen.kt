package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.R
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.ui.i18n.LocalAppLanguage
import elovaire.music.droidbeauty.app.ui.i18n.MiscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.localizedCountLabel
import elovaire.music.droidbeauty.app.ui.i18n.miscPhrase
import elovaire.music.droidbeauty.app.ui.i18n.searchCopy
import elovaire.music.droidbeauty.app.ui.interaction.elovaireActionBump
import elovaire.music.droidbeauty.app.ui.interaction.rememberElovaireInteractionSource
import elovaire.music.droidbeauty.app.ui.motion.ElovaireMotion
import elovaire.music.droidbeauty.app.ui.motion.elovaireListReveal
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionRevealRegistry
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
@Suppress("LongMethod")
internal fun RecentlyAddedAlbumsScreen(
    albums: List<Album>,
    playlists: List<Playlist>,
    playlistSongsById: Map<Long, Song>,
    favoriteSongIds: Set<Long>,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onAlbumSelected: (Album, ExpandOrigin) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onAddAlbumToPlaylist: (Long, Album) -> PlaylistMutationRequest,
    onCreatePlaylist: PlaylistCreateAction,
    onSetAlbumFavorite: (List<Long>, Boolean) -> Unit,
    onDeleteAlbumFromDevice: (Album) -> Unit,
) {
    val language = LocalAppLanguage.current
    val searchCopy = remember(language) { searchCopy(language) }
    var query by rememberSaveable { mutableStateOf("") }
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
    val revealRegistry = rememberMotionRevealRegistry()
    val searchBarAreaHeight = 96.dp
    val contentTopInset = detailTopBarOccupiedHeight() + searchBarAreaHeight
    val searchBarHazeState = rememberHazeState()

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(searchBarHazeState, zIndex = -1f),
            ) {
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
                        contentType = { _, _ -> "album_compact_row" },
                    ) { index, album ->
                        Box(
                            modifier = Modifier
                                .animateItem(placementSpec = ElovaireMotion.listPlacementSpec())
                                .elovaireListReveal(
                                    itemKey = album.id,
                                    index = index,
                                    registry = revealRegistry,
                                ),
                        ) {
                            CompactAlbumRow(
                                album = album,
                                isFavorite = album.songs.isNotEmpty() && album.songs.all { it.id in favoriteSongIds },
                                showFavoriteButton = true,
                                playlists = playlists,
                                playlistSongsById = playlistSongsById,
                                onOpen = { origin -> onAlbumSelected(album, origin) },
                                onToggleFavorite = {
                                    onSetAlbumFavorite(
                                        album.songs.map(Song::id),
                                        album.songs.any { it.id !in favoriteSongIds },
                                    )
                                },
                                onAddToQueue = { onAddAlbumToQueue(album) },
                                onAddToPlaylist = { playlistId -> onAddAlbumToPlaylist(playlistId, album) },
                                onCreatePlaylist = onCreatePlaylist,
                                onDeleteAlbum = { onDeleteAlbumFromDevice(album) },
                            )
                        }
                        if (index != matchingAlbums.lastIndex) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                DividerLine(modifier = Modifier.fillMaxWidth(0.9f))
                            }
                        }
                    }
                }
                FastScrollbar(
                    state = listState,
                    topInset = contentTopInset,
                    bottomInset = bottomPadding + 16.dp,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    top = detailTopBarOccupiedHeight(),
                )
                .height(searchBarAreaHeight),
            contentAlignment = Alignment.Center,
        ) {
            RecentlyAddedSearchControls(
                query = query,
                searchPlaceholder = searchCopy.placeholder,
                onQueryChange = { query = it },
                onClearQuery = { query = "" },
                hazeState = searchBarHazeState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
        }
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
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val searchBarContentColor = MaterialTheme.colorScheme.onSurface
    DynamicBackdropSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ElovaireRadii.input),
        overlayAlpha = 0.6f,
        hazeState = hazeState,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
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
                    val interactionSource = rememberElovaireInteractionSource()
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(searchBarContentColor.copy(alpha = 0.1f))
                            .elovaireActionBump(
                                interactionSource = interactionSource,
                                label = "recently_added_clear_search_bump",
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onClearQuery,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_x),
                            contentDescription = "Clear search",
                            tint = searchBarContentColor.copy(alpha = 0.86f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = searchBarContentColor,
                focusedPlaceholderColor = searchBarContentColor.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = searchBarContentColor.copy(alpha = 0.5f),
            ),
        )
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
