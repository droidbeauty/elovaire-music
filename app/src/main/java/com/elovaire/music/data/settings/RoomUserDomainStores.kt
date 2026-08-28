package elovaire.music.droidbeauty.app.data.settings

import android.net.Uri
import elovaire.music.droidbeauty.app.data.library.db.PlaybackCollectionStateEntity
import elovaire.music.droidbeauty.app.data.library.db.RecentPlaybackEntity
import elovaire.music.droidbeauty.app.data.library.db.SearchHistoryEntity
import elovaire.music.droidbeauty.app.data.library.db.UserDataDao
import elovaire.music.droidbeauty.app.data.library.isValidMediaId
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RoomPlaybackHistoryStore(
    private val dao: UserDataDao,
    private val enqueue: (String, suspend () -> Unit) -> Unit,
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
        if (writeBuffer.addTransition(songId, albumId)) {
            enqueue("playback.counts", ::flushPlaybackCounts)
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
        val pending = RecentPlaybackWrite(songs, albums, lastPlayedCollectionKind, collectionId)
        if (writeBuffer.setRecent(pending)) {
            enqueue("playback.recent", ::flushRecentPlayback)
        }
    }

    fun schedulePendingWrites() {
        if (writeBuffer.scheduleCountsIfNeeded()) {
            enqueue("playback.counts", ::flushPlaybackCounts)
        }
        if (writeBuffer.scheduleRecentIfNeeded()) {
            enqueue("playback.recent", ::flushRecentPlayback)
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

    fun relocateSongIds(replacements: Map<Long, Long>) {
        if (replacements.isEmpty()) return
        writeBuffer.relocateSongIds(replacements)
        _songPlayCounts.value = _songPlayCounts.value
            .entries
            .groupBy { resolveRelocatedSongId(it.key, replacements) }
            .mapValues { (_, entries) -> entries.maxOf { it.value } }
        _recentSongIds.value = _recentSongIds.value
            .map { resolveRelocatedSongId(it, replacements) }
            .distinct()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun flushPlaybackCounts() {
        val batch = writeBuffer.takeTransitions()
        if (batch.songCounts.isEmpty() && batch.albumCounts.isEmpty()) return
        try {
            dao.incrementPlaybackCounts(batch.songCounts, batch.albumCounts)
        } catch (failure: CancellationException) {
            writeBuffer.restoreTransitions(batch)
            throw failure
        } catch (failure: RuntimeException) {
            writeBuffer.restoreTransitions(batch)
            throw failure
        }
        if (batch.songCounts.isNotEmpty()) {
            _songPlayCounts.value = _songPlayCounts.value.incrementedBy(batch.songCounts)
        }
        if (batch.albumCounts.isNotEmpty()) {
            _albumPlayCounts.value = _albumPlayCounts.value.incrementedBy(batch.albumCounts)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun flushRecentPlayback() {
        val pending = writeBuffer.takeRecent() ?: return
        if (
            pending.songIds == _recentSongIds.value &&
            pending.albumIds == _recentAlbumIds.value &&
            pending.collectionKind == _lastPlayedCollectionKind.value &&
            pending.collectionId == _lastPlayedCollectionId.value
        ) return
        try {
            dao.replaceRecentPlayback(
                entries = pending.songIds.toRecentEntities(RECENT_KIND_SONG) +
                    pending.albumIds.toRecentEntities(RECENT_KIND_ALBUM),
                state = PlaybackCollectionStateEntity(
                    kind = pending.collectionKind?.name,
                    collectionId = pending.collectionId,
                ),
            )
        } catch (failure: CancellationException) {
            writeBuffer.restoreRecent(pending)
            throw failure
        } catch (failure: RuntimeException) {
            writeBuffer.restoreRecent(pending)
            throw failure
        }
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
    fun restoreTransitions(batch: PlaybackCountBatch) {
        batch.songCounts.forEach { (id, increment) ->
            songCounts[id] = incrementPlayCount(songCounts[id], increment)
        }
        batch.albumCounts.forEach { (id, increment) ->
            albumCounts[id] = incrementPlayCount(albumCounts[id], increment)
        }
        countFlushScheduled = false
    }

    @Synchronized
    fun scheduleCountsIfNeeded(): Boolean {
        if (songCounts.isEmpty() && albumCounts.isEmpty()) return false
        return if (countFlushScheduled) false else true.also { countFlushScheduled = it }
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

    @Synchronized
    fun restoreRecent(value: RecentPlaybackWrite) {
        recent = value
        recentFlushScheduled = false
    }

    @Synchronized
    fun scheduleRecentIfNeeded(): Boolean {
        if (recent == null) return false
        return if (recentFlushScheduled) false else true.also { recentFlushScheduled = it }
    }

    @Synchronized
    fun relocateSongIds(replacements: Map<Long, Long>) {
        if (replacements.isEmpty()) return
        val relocatedCounts = HashMap<Long, Int>(songCounts.size)
        songCounts.forEach { (id, count) ->
            val resolvedId = resolveRelocatedSongId(id, replacements)
            val existing = relocatedCounts[resolvedId]
            if (existing == null || count > existing) relocatedCounts[resolvedId] = count
        }
        songCounts.clear()
        songCounts.putAll(relocatedCounts)
        val pendingRecent = recent
        if (pendingRecent != null) {
            recent = pendingRecent.copy(
                songIds = pendingRecent.songIds
                    .map { resolveRelocatedSongId(it, replacements) }
                    .distinct(),
            )
        }
    }
}

internal fun resolveRelocatedSongId(id: Long, replacements: Map<Long, Long>): Long {
    var current = id
    val visited = HashSet<Long>()
    while (true) {
        val next = replacements[current] ?: return current
        if (!visited.add(current) || next == current) return current
        current = next
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
    private val enqueue: (String, suspend () -> Unit) -> Unit,
) : SearchHistoryStore {
    private val _searchHistory = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())
    override val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()
    private val pendingLock = Any()
    private var pendingHistory: List<SearchHistoryEntry>? = null
    private var pendingWriteScheduled = false

    override fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        val normalized = entry.normalized() ?: return
        val shouldSchedule = synchronized(pendingLock) {
            val base = pendingHistory ?: _searchHistory.value
            pendingHistory = historyAfterAdding(base, normalized)
            if (pendingWriteScheduled) false else true.also { pendingWriteScheduled = it }
        }
        if (shouldSchedule) enqueue("search_history", ::persistPendingSearchHistory)
    }

    override fun clearSearchHistoryEntries() {
        val shouldSchedule = synchronized(pendingLock) {
            pendingHistory = emptyList()
            if (pendingWriteScheduled) false else true.also { pendingWriteScheduled = it }
        }
        if (shouldSchedule) enqueue("search_history", ::persistPendingSearchHistory)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun persistPendingSearchHistory() {
        try {
            while (true) {
                val desired = synchronized(pendingLock) {
                    pendingHistory ?: return
                }
                if (desired != _searchHistory.value) {
                    if (desired.isEmpty()) {
                        dao.clearSearchHistory()
                    } else {
                        dao.replaceSearchHistory(desired.mapIndexed { index, item -> item.toEntity(index) })
                    }
                    _searchHistory.value = desired
                }
                synchronized(pendingLock) {
                    if (pendingHistory == desired) {
                        pendingHistory = null
                        pendingWriteScheduled = false
                        return
                    }
                }
            }
        } catch (failure: CancellationException) {
            synchronized(pendingLock) { pendingWriteScheduled = false }
            throw failure
        } catch (failure: RuntimeException) {
            synchronized(pendingLock) { pendingWriteScheduled = false }
            throw failure
        }
    }

    fun schedulePendingWrite() {
        val shouldSchedule = synchronized(pendingLock) {
            if (pendingHistory == null || pendingWriteScheduled) {
                false
            } else {
                pendingWriteScheduled = true
                true
            }
        }
        if (shouldSchedule) enqueue("search_history", ::persistPendingSearchHistory)
    }

    fun publish(entries: List<SearchHistoryEntry>) {
        synchronized(pendingLock) {
            pendingHistory = null
            pendingWriteScheduled = false
        }
        _searchHistory.value = entries
    }

    private fun historyAfterAdding(
        current: List<SearchHistoryEntry>,
        entry: SearchHistoryEntry,
    ): List<SearchHistoryEntry> = buildList {
        add(entry)
        current.asSequence()
            .filter { it.key != entry.key }
            .take(MAX_SEARCH_HISTORY - 1)
            .forEach(::add)
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
        albumId = albumId?.takeIf(::isValidMediaId),
        query = query,
    ).normalized()
}
