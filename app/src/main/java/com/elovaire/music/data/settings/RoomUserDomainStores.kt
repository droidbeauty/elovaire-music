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
    private val writeBuffer = PlaybackHistoryWriteBuffer()
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
        if (writeBuffer.addTransition(songId, albumId)) enqueue(::flushPlaybackCounts)
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
        val pending = RecentPlaybackWrite(songs, albums, lastPlayedCollectionKind, collectionId)
        if (writeBuffer.setRecent(pending)) enqueue(::flushRecentPlayback)
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

    private suspend fun flushPlaybackCounts() {
        val batch = writeBuffer.takeTransitions()
        batch.songCounts.forEach { (id, increment) -> dao.incrementSongPlayCount(id, increment) }
        batch.albumCounts.forEach { (id, increment) -> dao.incrementAlbumPlayCount(id, increment) }
        if (batch.songCounts.isNotEmpty()) {
            _songPlayCounts.value = _songPlayCounts.value.incrementedBy(batch.songCounts)
        }
        if (batch.albumCounts.isNotEmpty()) {
            _albumPlayCounts.value = _albumPlayCounts.value.incrementedBy(batch.albumCounts)
        }
    }

    private suspend fun flushRecentPlayback() {
        val pending = writeBuffer.takeRecent() ?: return
        if (
            pending.songIds == _recentSongIds.value &&
            pending.albumIds == _recentAlbumIds.value &&
            pending.collectionKind == _lastPlayedCollectionKind.value &&
            pending.collectionId == _lastPlayedCollectionId.value
        ) return
        dao.replaceRecentPlayback(
            entries = pending.songIds.toRecentEntities(RECENT_KIND_SONG) +
                pending.albumIds.toRecentEntities(RECENT_KIND_ALBUM),
            state = PlaybackCollectionStateEntity(
                kind = pending.collectionKind?.name,
                collectionId = pending.collectionId,
            ),
        )
        publish(pending.songIds, pending.albumIds, pending.collectionKind, pending.collectionId)
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

internal data class PlaybackCountBatch(
    val songCounts: Map<Long, Int>,
    val albumCounts: Map<Long, Int>,
)

internal data class RecentPlaybackWrite(
    val songIds: List<Long>,
    val albumIds: List<Long>,
    val collectionKind: PlaybackCollectionKind?,
    val collectionId: Long?,
)

internal class PlaybackHistoryWriteBuffer {
    private val songCounts = mutableMapOf<Long, Int>()
    private val albumCounts = mutableMapOf<Long, Int>()
    private var countFlushScheduled = false
    private var recentFlushScheduled = false
    private var recent: RecentPlaybackWrite? = null

    @Synchronized
    fun addTransition(songId: Long?, albumId: Long?): Boolean {
        songId?.takeIf { it != 0L }?.let { id ->
            songCounts[id] = incrementPlayCount(songCounts[id])
        }
        albumId?.takeIf { it != 0L }?.let { id ->
            albumCounts[id] = incrementPlayCount(albumCounts[id])
        }
        if (songCounts.isEmpty() && albumCounts.isEmpty()) return false
        return if (countFlushScheduled) false else true.also { countFlushScheduled = it }
    }

    @Synchronized
    fun takeTransitions(): PlaybackCountBatch {
        val batch = PlaybackCountBatch(songCounts.toMap(), albumCounts.toMap())
        songCounts.clear()
        albumCounts.clear()
        countFlushScheduled = false
        return batch
    }

    @Synchronized
    fun setRecent(value: RecentPlaybackWrite): Boolean {
        recent = value
        return if (recentFlushScheduled) false else true.also { recentFlushScheduled = it }
    }

    @Synchronized
    fun takeRecent(): RecentPlaybackWrite? {
        val value = recent
        recent = null
        recentFlushScheduled = false
        return value
    }
}

private fun Map<Long, Int>.incrementedBy(increments: Map<Long, Int>): Map<Long, Int> {
    if (increments.isEmpty()) return this
    return toMutableMap().apply {
        increments.forEach { (id, increment) ->
            this[id] = incrementPlayCount(this[id], increment)
        }
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
