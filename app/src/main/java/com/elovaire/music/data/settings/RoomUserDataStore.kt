package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.database.SQLException
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.library.db.AlbumPlayCountEntity
import elovaire.music.droidbeauty.app.data.library.db.FavoriteSongEntity
import elovaire.music.droidbeauty.app.data.library.db.PlaybackCollectionStateEntity
import elovaire.music.droidbeauty.app.data.library.db.RecentPlaybackEntity
import elovaire.music.droidbeauty.app.data.library.db.SearchHistoryEntity
import elovaire.music.droidbeauty.app.data.library.db.SongPlayCountEntity
import elovaire.music.droidbeauty.app.data.library.db.UserDataDao
import elovaire.music.droidbeauty.app.data.library.db.UserDataMigrationEntity
import elovaire.music.droidbeauty.app.data.library.db.UserPlaylistEntity
import elovaire.music.droidbeauty.app.data.library.db.UserPlaylistEntryEntity
import elovaire.music.droidbeauty.app.data.library.db.UserSmartPlaylistEntity
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playlists.addSongsToPlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.createPlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.deletePlaylistEntries
import elovaire.music.droidbeauty.app.data.playlists.deserializePlaylists
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistName
import elovaire.music.droidbeauty.app.data.playlists.removeSongReferencesFromPlaylists
import elovaire.music.droidbeauty.app.data.playlists.renamePlaylistEntry
import elovaire.music.droidbeauty.app.data.playlists.updatePlaylistSongIdsEntry
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylistDefaults
import elovaire.music.droidbeauty.app.data.smartplaylists.createSmartPlaylistEntry
import elovaire.music.droidbeauty.app.data.smartplaylists.deleteSmartPlaylistEntries
import elovaire.music.droidbeauty.app.data.smartplaylists.deserializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.serializeSmartPlaylists
import elovaire.music.droidbeauty.app.data.smartplaylists.updateSmartPlaylistEntry
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class RoomUserDataStore(
    context: Context,
    private val dao: UserDataDao,
    private val clock: AppClock = AndroidAppClock,
) : CollectionSettingsStore, PlaylistStore, FavoritesStore, PlaybackHistoryStore, SearchHistoryStore {
    private val preferences = PreferenceStorage(context.applicationContext).preferences
    private val released = AtomicBoolean(false)
    private val nextId = AtomicLong(clock.wallTimeMs().coerceAtLeast(1L))
    // Mandatory non-suspending mutations cannot be dropped or block the main thread. High-frequency
    // history writes are coalesced before entering this serialized persistence queue.
    private val operations = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val operationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queueDepth = AtomicInteger()
    private val maxQueueDepth = AtomicInteger()
    private val playbackHistoryStore = RoomPlaybackHistoryStore(dao, ::enqueue)
    private val searchHistoryStore = RoomSearchHistoryStore(dao, ::enqueue)
    private val ownerJob: Job = operationScope.launch {
        initialize()
        for (operation in operations) {
            queueDepth.decrementAndGet()
            runOperation(operation)
        }
    }

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    override val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _userSmartPlaylists = MutableStateFlow<List<SmartPlaylist>>(emptyList())
    private val _smartPlaylists = MutableStateFlow(SmartPlaylistDefaults.builtIns())
    override val smartPlaylists: StateFlow<List<SmartPlaylist>> = _smartPlaylists.asStateFlow()

    private val _favoriteSongIds = MutableStateFlow<List<Long>>(emptyList())
    override val favoriteSongIds: StateFlow<List<Long>> = _favoriteSongIds.asStateFlow()

    override val albumPlayCounts get() = playbackHistoryStore.albumPlayCounts
    override val songPlayCounts get() = playbackHistoryStore.songPlayCounts
    override val recentSongIds get() = playbackHistoryStore.recentSongIds
    override val recentAlbumIds get() = playbackHistoryStore.recentAlbumIds
    override val lastPlayedCollectionKind get() = playbackHistoryStore.lastPlayedCollectionKind
    override val lastPlayedCollectionId get() = playbackHistoryStore.lastPlayedCollectionId
    override val searchHistory get() = searchHistoryStore.searchHistory

    override fun createPlaylist(name: String): Long {
        if (normalizePlaylistName(name).isBlank()) return -1L
        val id = newId()
        return if (tryEnqueue {
            val result = createPlaylistEntries(_userPlaylists.value, name, id) ?: return@tryEnqueue
            dao.insertPlaylist(result.createdPlaylist.toEntity())
            _userPlaylists.value = result.playlists
            _playlists.value = result.playlists
        }) id else -1L
    }

    override fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        enqueue {
            val updated = addSongsToPlaylistEntries(_userPlaylists.value, playlistId, songIds) ?: return@enqueue
            val playlist = updated.first { it.id == playlistId }
            dao.replacePlaylistEntries(playlistId, playlist.songIds)
            publishPlaylists(updated)
        }
    }

    override fun renamePlaylist(playlistId: Long, name: String) {
        enqueue {
            val updated = renamePlaylistEntry(_userPlaylists.value, playlistId, name) ?: return@enqueue
            val playlist = updated.first { it.id == playlistId }
            dao.renamePlaylist(playlistId, playlist.name)
            publishPlaylists(updated)
        }
    }

    override fun updatePlaylistSongIds(playlistId: Long, songIds: List<Long>) {
        enqueue {
            val updated = updatePlaylistSongIdsEntry(_userPlaylists.value, playlistId, songIds) ?: return@enqueue
            val playlist = updated.first { it.id == playlistId }
            dao.replacePlaylistEntries(playlistId, playlist.songIds)
            publishPlaylists(updated)
        }
    }

    override fun deletePlaylists(playlistIds: Set<Long>) {
        enqueue {
            val updated = deletePlaylistEntries(_userPlaylists.value, playlistIds) ?: return@enqueue
            dao.deletePlaylists(playlistIds)
            publishPlaylists(updated)
        }
    }

    override fun createSmartPlaylist(name: String): Long {
        if (name.isBlank()) return -1L
        val id = newId()
        return if (tryEnqueue {
            val result = createSmartPlaylistEntry(
                playlists = _userSmartPlaylists.value,
                name = name,
                nextSmartPlaylistId = id,
                nowMs = clock.wallTimeMs(),
            ) ?: return@tryEnqueue
            dao.upsertSmartPlaylist(result.createdPlaylist.toEntity())
            publishSmartPlaylists(result.playlists)
        }) id else -1L
    }

    override fun updateSmartPlaylist(playlist: SmartPlaylist) {
        enqueue {
            val updated = updateSmartPlaylistEntry(
                playlists = _userSmartPlaylists.value,
                playlist = playlist,
                nowMs = clock.wallTimeMs(),
            ) ?: return@enqueue
            dao.upsertSmartPlaylist(updated.first { it.id == playlist.id }.toEntity())
            publishSmartPlaylists(updated)
        }
    }

    override fun deleteSmartPlaylists(playlistIds: Set<Long>) {
        enqueue {
            val updated = deleteSmartPlaylistEntries(_userSmartPlaylists.value, playlistIds) ?: return@enqueue
            dao.deleteSmartPlaylists(playlistIds)
            publishSmartPlaylists(updated)
        }
    }

    override fun toggleFavoriteSong(songId: Long) {
        if (songId == 0L) return
        enqueue {
            if (songId in _favoriteSongIds.value) {
                dao.removeFavorites(setOf(songId))
                publishFavorites(_favoriteSongIds.value.filterNot { it == songId })
            } else {
                val position = dao.lastFavoritePosition() + 1
                if (dao.insertFavorite(FavoriteSongEntity(songId, position)) != -1L) {
                    publishFavorites(_favoriteSongIds.value + songId)
                }
            }
        }
    }

    override fun setFavoriteSongs(songIds: List<Long>, favorite: Boolean) {
        val normalized = normalizeFavoriteSongIds(songIds)
        if (normalized.isEmpty()) return
        enqueue {
            if (favorite) {
                var position = dao.lastFavoritePosition() + 1
                val current = _favoriteSongIds.value.toMutableList()
                normalized.forEach { songId ->
                    if (dao.insertFavorite(FavoriteSongEntity(songId, position)) != -1L) {
                        current += songId
                        position += 1
                    }
                }
                publishFavorites(current)
            } else {
                val ids = normalized.toSet()
                dao.removeFavorites(ids)
                publishFavorites(_favoriteSongIds.value.filterNot(ids::contains))
            }
        }
    }

    override fun removeSongReferences(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        enqueue {
            dao.removeSongReferences(songIds)
            val playlists = removeSongReferencesFromPlaylists(_userPlaylists.value, songIds)
                ?: _userPlaylists.value
            publishPlaylists(playlists)
            publishFavorites(_favoriteSongIds.value.filterNot(songIds::contains))
        }
    }

    override fun recordPlaybackTransition(songId: Long?, albumId: Long?) {
        playbackHistoryStore.recordPlaybackTransition(songId, albumId)
    }

    override fun setRecentPlaybackIds(
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) {
        playbackHistoryStore.setRecentPlaybackIds(
            songIds,
            albumIds,
            lastPlayedCollectionKind,
            lastPlayedCollectionId,
        )
    }

    override fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        searchHistoryStore.addSearchHistoryEntry(entry)
    }

    override fun clearSearchHistoryEntries() {
        searchHistoryStore.clearSearchHistoryEntries()
    }

    fun release(onDrained: () -> Unit = {}) {
        if (!released.compareAndSet(false, true)) return
        operations.close()
        ownerJob.invokeOnCompletion {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "User-data queue drained maxDepth=${maxQueueDepth.get()}")
            }
            operationScope.cancel()
            onDrained()
        }
    }

    private suspend fun initialize() {
        val legacy = readLegacyUserData(preferences)
        try {
            if (!dao.migrationComplete(MIGRATION_ID)) {
                dao.migrateLegacy(
                    playlists = legacy.playlists.map(Playlist::toEntity),
                    entries = legacy.playlists.flatMap(Playlist::toEntryEntities),
                    smartPlaylists = legacy.smartPlaylists.map(SmartPlaylist::toEntity),
                    favorites = legacy.favoriteSongIds.mapIndexed { index, id -> FavoriteSongEntity(id, index) },
                    songCounts = legacy.songPlayCounts.map { (id, count) -> SongPlayCountEntity(id, count) },
                    albumCounts = legacy.albumPlayCounts.map { (id, count) -> AlbumPlayCountEntity(id, count) },
                    recents = legacy.recentSongIds.toRecentEntities(RECENT_KIND_SONG) +
                        legacy.recentAlbumIds.toRecentEntities(RECENT_KIND_ALBUM),
                    searchHistory = legacy.searchHistory.mapIndexed { index, entry -> entry.toEntity(index) },
                    collectionState = PlaybackCollectionStateEntity(
                        kind = legacy.lastPlayedCollectionKind?.name,
                        collectionId = legacy.lastPlayedCollectionId,
                    ),
                    migration = UserDataMigrationEntity(MIGRATION_ID, clock.wallTimeMs()),
                )
            }
            check(dao.migrationComplete(MIGRATION_ID))
            clearLegacyUserData(preferences)
            publishSnapshot(loadSnapshot())
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: SQLException) {
            onMigrationFailure(legacy, failure)
        } catch (failure: IllegalStateException) {
            onMigrationFailure(legacy, failure)
        }
    }

    private fun onMigrationFailure(legacy: UserDataSnapshot, failure: RuntimeException) {
        Log.e(TAG, "User-data migration failed; legacy data remains available for retry.", failure)
        publishSnapshot(legacy)
    }

    private suspend fun loadSnapshot(): UserDataSnapshot {
        val playlistEntries = dao.playlistEntries().groupBy(UserPlaylistEntryEntity::playlistId)
        val collectionState = dao.playbackCollectionState()
        val recents = dao.recentPlayback().groupBy(RecentPlaybackEntity::kind)
        return UserDataSnapshot(
            playlists = dao.playlists().map { entity ->
                Playlist(
                    id = entity.playlistId,
                    name = entity.name,
                    songIds = playlistEntries[entity.playlistId].orEmpty().map(UserPlaylistEntryEntity::songId),
                    isSystem = entity.isSystem,
                )
            },
            smartPlaylists = dao.smartPlaylists().mapNotNull { entity ->
                deserializeSmartPlaylists(entity.payload).singleOrNull()
            },
            favoriteSongIds = dao.favorites().map(FavoriteSongEntity::songId),
            songPlayCounts = dao.songPlayCounts().associate { it.songId to it.playCount },
            albumPlayCounts = dao.albumPlayCounts().associate { it.albumId to it.playCount },
            recentSongIds = recents[RECENT_KIND_SONG].orEmpty().map(RecentPlaybackEntity::itemId),
            recentAlbumIds = recents[RECENT_KIND_ALBUM].orEmpty().map(RecentPlaybackEntity::itemId),
            lastPlayedCollectionKind = collectionState?.kind?.let { stored ->
                PlaybackCollectionKind.entries.firstOrNull { it.name == stored }
            },
            lastPlayedCollectionId = collectionState?.collectionId,
            searchHistory = dao.searchHistory().mapNotNull(SearchHistoryEntity::toDomain),
        )
    }

    private fun publishSnapshot(snapshot: UserDataSnapshot) {
        publishPlaylists(snapshot.playlists)
        publishSmartPlaylists(snapshot.smartPlaylists)
        publishFavorites(snapshot.favoriteSongIds)
        playbackHistoryStore.publish(
            songCounts = snapshot.songPlayCounts,
            albumCounts = snapshot.albumPlayCounts,
            songIds = snapshot.recentSongIds,
            albumIds = snapshot.recentAlbumIds,
            collectionKind = snapshot.lastPlayedCollectionKind,
            collectionId = snapshot.lastPlayedCollectionId,
        )
        searchHistoryStore.publish(snapshot.searchHistory)
        val maxId = sequenceOf(snapshot.playlists.maxOfOrNull(Playlist::id), snapshot.smartPlaylists.maxOfOrNull(SmartPlaylist::id))
            .filterNotNull()
            .maxOrNull()
            ?: 0L
        val nextAfterPersisted = if (maxId == Long.MAX_VALUE) Long.MAX_VALUE else maxId + 1L
        nextId.updateAndGet { current -> maxOf(current, nextAfterPersisted) }
    }

    private fun publishPlaylists(playlists: List<Playlist>) {
        if (_userPlaylists.value == playlists) return
        _userPlaylists.value = playlists
        _playlists.value = playlists
    }

    private fun publishSmartPlaylists(playlists: List<SmartPlaylist>) {
        if (_userSmartPlaylists.value == playlists) return
        _userSmartPlaylists.value = playlists
        _smartPlaylists.value = SmartPlaylistDefaults.builtIns() + playlists
    }

    private fun publishFavorites(songIds: List<Long>) {
        val normalized = normalizeFavoriteSongIds(songIds)
        if (_favoriteSongIds.value != normalized) _favoriteSongIds.value = normalized
    }

    private fun enqueue(operation: suspend () -> Unit) {
        tryEnqueue(operation)
    }

    private fun tryEnqueue(operation: suspend () -> Unit): Boolean {
        if (released.get()) return false
        val depth = queueDepth.incrementAndGet()
        return if (operations.trySend(operation).isSuccess) {
            maxQueueDepth.updateAndGet { current -> maxOf(current, depth) }
            true
        } else {
            queueDepth.decrementAndGet()
            false
        }
    }

    private suspend fun runOperation(operation: suspend () -> Unit) {
        try {
            operation()
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: SQLException) {
            Log.e(TAG, "User-data operation failed.", failure)
        }
    }

    private fun newId(): Long {
        return nextId.getAndUpdate(::nextPersistentUserDataId)
    }

    private companion object {
        const val TAG = "RoomUserDataStore"
        const val MIGRATION_ID = "shared_preferences_domain_data_v1"
        const val RECENT_KIND_SONG = "song"
        const val RECENT_KIND_ALBUM = "album"
    }
}

internal fun nextPersistentUserDataId(current: Long): Long {
    check(current in 1 until Long.MAX_VALUE) { "User-data ID space is exhausted." }
    return current + 1L
}

private data class UserDataSnapshot(
    val playlists: List<Playlist> = emptyList(),
    val smartPlaylists: List<SmartPlaylist> = emptyList(),
    val favoriteSongIds: List<Long> = emptyList(),
    val songPlayCounts: Map<Long, Int> = emptyMap(),
    val albumPlayCounts: Map<Long, Int> = emptyMap(),
    val recentSongIds: List<Long> = emptyList(),
    val recentAlbumIds: List<Long> = emptyList(),
    val lastPlayedCollectionKind: PlaybackCollectionKind? = null,
    val lastPlayedCollectionId: Long? = null,
    val searchHistory: List<SearchHistoryEntry> = emptyList(),
)

private fun readLegacyUserData(preferences: SharedPreferences): UserDataSnapshot {
    return UserDataSnapshot(
        playlists = deserializePlaylists(preferences.stringOrNull(LegacyUserDataKeys.PLAYLISTS))
            .distinctBy(Playlist::id),
        smartPlaylists = deserializeSmartPlaylists(preferences.stringOrNull(LegacyUserDataKeys.SMART_PLAYLISTS))
            .distinctBy(SmartPlaylist::id),
        favoriteSongIds = preferences.idList(LegacyUserDataKeys.FAVORITES),
        songPlayCounts = preferences.playCounts(LegacyUserDataKeys.SONG_PLAY_COUNTS),
        albumPlayCounts = preferences.playCounts(LegacyUserDataKeys.ALBUM_PLAY_COUNTS),
        recentSongIds = preferences.idList(LegacyUserDataKeys.RECENT_SONG_IDS).take(24),
        recentAlbumIds = preferences.idList(LegacyUserDataKeys.RECENT_ALBUM_IDS).take(24),
        lastPlayedCollectionKind = preferences.stringOrNull(LegacyUserDataKeys.LAST_COLLECTION_KIND)
            ?.let { value -> PlaybackCollectionKind.entries.firstOrNull { it.name == value } },
        lastPlayedCollectionId = preferences.longOrNull(LegacyUserDataKeys.LAST_COLLECTION_ID)
            ?.takeIf { it != 0L },
        searchHistory = preferences.stringOrNull(LegacyUserDataKeys.SEARCH_HISTORY)
            ?.split(PreferenceCollectionCodec.RECORD_SEPARATOR)
            ?.mapNotNull(PreferenceCollectionCodec::deserializeSearchHistory)
            .orEmpty()
            .distinctBy(SearchHistoryEntry::key)
            .take(6),
    )
}

private fun clearLegacyUserData(preferences: SharedPreferences) {
    preferences.edit().apply {
        LegacyUserDataKeys.ALL.forEach(::remove)
    }.apply()
}

private object LegacyUserDataKeys {
    const val SEARCH_HISTORY = "search_history"
    const val PLAYLISTS = "playlists"
    const val NEXT_PLAYLIST_ID = "next_playlist_id"
    const val SMART_PLAYLISTS = "smart_playlists"
    const val NEXT_SMART_PLAYLIST_ID = "next_smart_playlist_id"
    const val FAVORITES = "favorite_song_ids"
    const val ALBUM_PLAY_COUNTS = "album_play_counts"
    const val SONG_PLAY_COUNTS = "song_play_counts"
    const val RECENT_SONG_IDS = "recent_song_ids"
    const val RECENT_ALBUM_IDS = "recent_album_ids"
    const val LAST_COLLECTION_KIND = "last_played_collection_kind"
    const val LAST_COLLECTION_ID = "last_played_collection_id"
    val ALL = setOf(
        SEARCH_HISTORY,
        PLAYLISTS,
        NEXT_PLAYLIST_ID,
        SMART_PLAYLISTS,
        NEXT_SMART_PLAYLIST_ID,
        FAVORITES,
        ALBUM_PLAY_COUNTS,
        SONG_PLAY_COUNTS,
        RECENT_SONG_IDS,
        RECENT_ALBUM_IDS,
        LAST_COLLECTION_KIND,
        LAST_COLLECTION_ID,
    )
}

private fun SharedPreferences.idList(key: String): List<Long> = stringOrNull(key)
    ?.split(',')
    ?.mapNotNull(String::toLongOrNull)
    ?.let(::normalizeFavoriteSongIds)
    .orEmpty()

private fun SharedPreferences.playCounts(key: String): Map<Long, Int> = stringOrNull(key)
    ?.let(PreferenceCollectionCodec::deserializePlayCounts)
    .orEmpty()

private fun SharedPreferences.stringOrNull(key: String): String? = try {
    getString(key, null)
} catch (_: ClassCastException) {
    null
}

private fun SharedPreferences.longOrNull(key: String): Long? {
    if (!contains(key)) return null
    return try {
        getLong(key, 0L)
    } catch (_: ClassCastException) {
        null
    }
}

private fun Playlist.toEntity(): UserPlaylistEntity = UserPlaylistEntity(id, name, isSystem)

private fun Playlist.toEntryEntities(): List<UserPlaylistEntryEntity> = songIds.distinct().mapIndexed { index, songId ->
    UserPlaylistEntryEntity(id, songId, index)
}

private fun SmartPlaylist.toEntity(): UserSmartPlaylistEntity = UserSmartPlaylistEntity(
    playlistId = id,
    payload = serializeSmartPlaylists(listOf(this)),
)

private fun List<Long>.toRecentEntities(kind: String): List<RecentPlaybackEntity> =
    mapIndexed { index, id -> RecentPlaybackEntity(kind, id, index) }
