package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.PlaybackCollectionStateEntity
import elovaire.music.droidbeauty.app.data.library.db.RecentPlaybackEntity
import elovaire.music.droidbeauty.app.data.library.db.SearchHistoryEntity
import elovaire.music.droidbeauty.app.data.library.db.UserDataDao
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RoomPlaybackHistoryStore(
    private val dao: UserDataDao,
    private val enqueue: (suspend () -> Unit) -> Unit,
) : PlaybackHistoryStore {
    private val _albumPlayCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    override val albumPlayCounts: StateFlow<Map<Long, Int>> = _albumPlayCounts.asStateFlow()
    private val _songPlayCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    override val songPlayCounts: StateFlow<Map<Long, Int>> = _songPlayCounts.asStateFlow()
    private val _recentSongIds = MutableStateFlow<List<Long>>(emptyList())
    override val recentSongIds: StateFlow<List<Long>> = _recentSongIds.asStateFlow()
    private val _recentAlbumIds = MutableStateFlow<List<Long>>(emptyList())
    override val recentAlbumIds: StateFlow<List<Long>> = _recentAlbumIds.asStateFlow()
    private val _lastPlayedCollectionKind = MutableStateFlow<PlaybackCollectionKind?>(null)
    override val lastPlayedCollectionKind: StateFlow<PlaybackCollectionKind?> =
        _lastPlayedCollectionKind.asStateFlow()
    private val _lastPlayedCollectionId = MutableStateFlow<Long?>(null)
    override val lastPlayedCollectionId: StateFlow<Long?> = _lastPlayedCollectionId.asStateFlow()

    override fun recordPlaybackTransition(songId: Long?, albumId: Long?) {
        if (songId == null && albumId == null) return
        enqueue {
            songId?.takeIf { it != 0L }?.let { id ->
                dao.incrementSongPlayCount(id)
                _songPlayCounts.value = _songPlayCounts.value + (id to incrementPlayCount(_songPlayCounts.value[id]))
            }
            albumId?.takeIf { it != 0L }?.let { id ->
                dao.incrementAlbumPlayCount(id)
                _albumPlayCounts.value = _albumPlayCounts.value + (id to incrementPlayCount(_albumPlayCounts.value[id]))
            }
        }
    }

    override fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) {
        val songs = normalizeRecentIds(songIds)
        val albums = normalizeRecentIds(albumIds)
        val collectionId = lastPlayedCollectionId?.takeIf { it != 0L }
        enqueue {
            if (
                songs == _recentSongIds.value &&
                albums == _recentAlbumIds.value &&
                lastPlayedCollectionKind == _lastPlayedCollectionKind.value &&
                collectionId == _lastPlayedCollectionId.value
            ) return@enqueue
            dao.replaceRecentPlayback(
                entries = songs.toRecentEntities(RECENT_KIND_SONG) + albums.toRecentEntities(RECENT_KIND_ALBUM),
                state = PlaybackCollectionStateEntity(
                    kind = lastPlayedCollectionKind?.name,
                    collectionId = collectionId,
                ),
            )
            publish(songs, albums, lastPlayedCollectionKind, collectionId)
        }
    }

    fun publish(
        songCounts: Map<Long, Int>,
        albumCounts: Map<Long, Int>,
        songIds: List<Long>,
        albumIds: List<Long>,
        collectionKind: PlaybackCollectionKind?,
        collectionId: Long?,
    ) {
        _songPlayCounts.value = songCounts
        _albumPlayCounts.value = albumCounts
        publish(songIds, albumIds, collectionKind, collectionId)
    }

    private fun publish(
        songIds: List<Long>,
        albumIds: List<Long>,
        collectionKind: PlaybackCollectionKind?,
        collectionId: Long?,
    ) {
        _recentSongIds.value = songIds
        _recentAlbumIds.value = albumIds
        _lastPlayedCollectionKind.value = collectionKind
        _lastPlayedCollectionId.value = collectionId
    }

    private companion object {
        const val RECENT_KIND_SONG = "song"
        const val RECENT_KIND_ALBUM = "album"
        const val MAX_RECENT_PLAYBACK_IDS = 24

        fun normalizeRecentIds(ids: List<Long>): List<Long> = ids.asSequence()
            .filter { it != 0L }
            .distinct()
            .take(MAX_RECENT_PLAYBACK_IDS)
            .toList()

        fun List<Long>.toRecentEntities(kind: String): List<RecentPlaybackEntity> =
            mapIndexed { index, id -> RecentPlaybackEntity(kind, id, index) }
    }
}

internal class RoomSearchHistoryStore(
    private val dao: UserDataDao,
    private val enqueue: (suspend () -> Unit) -> Unit,
) : SearchHistoryStore {
    private val _searchHistory = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())
    override val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()

    override fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        val normalized = entry.normalized() ?: return
        enqueue {
            val updated = buildList {
                add(normalized)
                _searchHistory.value.asSequence()
                    .filter { it.key != normalized.key }
                    .take(MAX_SEARCH_HISTORY - 1)
                    .forEach(::add)
            }
            if (updated == _searchHistory.value) return@enqueue
            dao.replaceSearchHistory(updated.mapIndexed { index, item -> item.toEntity(index) })
            _searchHistory.value = updated
        }
    }

    override fun clearSearchHistoryEntries() {
        enqueue {
            if (_searchHistory.value.isEmpty()) return@enqueue
            dao.clearSearchHistory()
            _searchHistory.value = emptyList()
        }
    }

    fun publish(entries: List<SearchHistoryEntry>) {
        _searchHistory.value = entries
    }

    private companion object {
        const val MAX_SEARCH_HISTORY = 6
    }
}

internal fun SearchHistoryEntry.normalized(): SearchHistoryEntry? {
    val normalized = copy(
        key = key.trim(),
        title = title.trim(),
        subtitle = subtitle.trim(),
        query = query?.trim()?.takeIf(String::isNotBlank),
    )
    return normalized.takeIf { it.key.isNotBlank() && it.title.isNotBlank() }
}

internal fun SearchHistoryEntry.toEntity(position: Int): SearchHistoryEntity = SearchHistoryEntity(
    entryKey = key,
    kind = kind.name,
    title = title,
    subtitle = subtitle,
    artUri = artUri?.toString(),
    albumId = albumId,
    query = query,
    position = position,
)

internal fun SearchHistoryEntity.toDomain(): SearchHistoryEntry? {
    val parsedKind = SearchHistoryKind.entries.firstOrNull { it.name == kind } ?: return null
    return SearchHistoryEntry(
        key = entryKey,
        kind = parsedKind,
        title = title,
        subtitle = subtitle,
        artUri = artUri?.let(Uri::parse),
        albumId = albumId,
        query = query,
    ).normalized()
}
