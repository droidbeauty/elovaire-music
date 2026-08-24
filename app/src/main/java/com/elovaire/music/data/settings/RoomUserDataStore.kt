package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.database.SQLException
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
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
import elovaire.music.droidbeauty.app.data.playlists.distinctImportedPlaylists
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistName
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistSongIds
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
import java.util.concurrent.atomic.AtomicReference
import java.util.LinkedHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "TooGenericExceptionCaught")
internal class RoomUserDataStore(
    context: Context,
    private val dao: UserDataDao,
    private val clock: AppClock = AndroidAppClock,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val recoverySnapshot: UserDataRecoverySnapshot? = null,
) : CollectionSettingsStore, PlaylistStore, FavoritesStore, PlaybackHistoryStore, SearchHistoryStore {
    private val preferences = allowStrictModeDiskReads {
        PreferenceStorage(context.applicationContext).preferences
    }
    private val released = AtomicBoolean(false)
    private val lifecycle = AtomicReference(StoreLifecycle.Initializing)
    private val actorFailure = AtomicReference<Throwable?>(null)
    private val recoveryWriteFailureReported = AtomicBoolean(false)
    private val nextId = AtomicLong(clock.wallTimeMs().coerceAtLeast(1L))
    // Durable mutations are accepted only when they fit in the bounded channel. Coalescible
    // state uses one replaceable slot per semantic operation instead of suspended senders.
    private val operations = Channel<RoomOperation>(MAX_OPERATION_QUEUE_DEPTH)
    private val operationScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val queueDepth = AtomicInteger()
    private val maxQueueDepth = AtomicInteger()
    private val submissionLock = Any()
    private val coalescedOperations = LinkedHashMap<String, RoomOperation>()
    private val playbackHistoryStore = RoomPlaybackHistoryStore(dao, ::enqueueCoalesced)
    private val searchHistoryStore = RoomSearchHistoryStore(dao, ::enqueueCoalesced)

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    override val playlists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

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

    // Start only after every state flow is initialized. The actor may run on another
    // thread immediately, so starting it during an earlier property initializer can
    // publish the startup snapshot into fields that do not exist yet.
    private val ownerJob: Job = operationScope.launch(start = CoroutineStart.LAZY) {
        try {
            initialize()
            if (!lifecycle.compareAndSet(StoreLifecycle.Initializing, StoreLifecycle.Ready)) {
                synchronized(submissionLock) {
                    rejectPendingOperationsLocked()
                }
                return@launch
            }
            for (operation in operations) {
                queueDepth.decrementAndGet()
                runOperation(operation)
                promoteCoalescedOperation()
            }
        } catch (cancelled: CancellationException) {
            if (!released.get()) failActor(cancelled)
            throw cancelled
        } catch (failure: RuntimeException) {
            failActor(failure)
        } catch (failure: Error) {
            failActor(failure)
        } finally {
            if (released.get()) lifecycle.set(StoreLifecycle.Released)
        }
    }

    init {
        ownerJob.start()
    }

    override fun createPlaylist(name: String): Deferred<PlaylistMutationResult> {
        return enqueueMutation("playlist.create") {
            val id = newId()
            val result = createPlaylistEntries(_userPlaylists.value, name, id)
                ?: return@enqueueMutation PlaylistMutationResult.InvalidInput
            dao.insertPlaylistAndVerify(result.createdPlaylist.toEntity())
            publishPlaylists(result.playlists)
            PlaylistMutationResult.Success(result.createdPlaylist.id)
        }
    }

    override fun createPlaylistWithSongs(
        name: String,
        songIds: List<Long>,
    ): Deferred<PlaylistMutationResult> {
        return enqueueMutation("playlist.create_with_songs") {
            val id = newId()
            val result = createPlaylistEntries(_userPlaylists.value, name, id)
                ?: return@enqueueMutation PlaylistMutationResult.InvalidInput
            val normalizedSongIds = normalizePlaylistSongIds(songIds)
            dao.insertPlaylistWithEntries(result.createdPlaylist.toEntity(), normalizedSongIds)
            publishPlaylists(result.playlists.map { playlist ->
                if (playlist.id == result.createdPlaylist.id) {
                    playlist.copy(songIds = normalizedSongIds)
                } else {
                    playlist
                }
            })
            PlaylistMutationResult.Success(result.createdPlaylist.id)
        }
    }

    override fun addSongsToPlaylist(
        playlistId: Long,
        songIds: List<Long>,
    ): Deferred<PlaylistMutationResult> = enqueueMutation("playlist.add_songs") {
        if (normalizePlaylistSongIds(songIds).isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput
        val current = _userPlaylists.value.firstOrNull { it.id == playlistId }
            ?: return@enqueueMutation PlaylistMutationResult.NotFound
        if (current.isSystem) return@enqueueMutation PlaylistMutationResult.NotAllowed
        val updated = addSongsToPlaylistEntries(_userPlaylists.value, playlistId, songIds)
            ?: return@enqueueMutation PlaylistMutationResult.Success(playlistId, changed = false)
        if (updated == _userPlaylists.value) {
            return@enqueueMutation PlaylistMutationResult.Success(playlistId, changed = false)
        }
        val playlist = updated.first { it.id == playlistId }
        dao.replacePlaylistEntriesAndVerify(playlistId, playlist.songIds)
        publishPlaylists(updated)
        PlaylistMutationResult.Success(playlistId)
    }

    override fun renamePlaylist(
        playlistId: Long,
        name: String,
    ): Deferred<PlaylistMutationResult> = enqueueMutation("playlist.rename") {
        val current = _userPlaylists.value.firstOrNull { it.id == playlistId }
            ?: return@enqueueMutation PlaylistMutationResult.NotFound
        if (current.isSystem) return@enqueueMutation PlaylistMutationResult.NotAllowed
        val updated = renamePlaylistEntry(_userPlaylists.value, playlistId, name)
        if (updated == null) {
            return@enqueueMutation if (normalizePlaylistName(name).isBlank()) {
                PlaylistMutationResult.InvalidInput
            } else {
                PlaylistMutationResult.Success(playlistId, changed = false)
            }
        }
        if (updated == _userPlaylists.value) {
            return@enqueueMutation PlaylistMutationResult.Success(playlistId, changed = false)
        }
        val playlist = updated.first { it.id == playlistId }
        dao.renamePlaylistAndVerify(playlistId, playlist.name)
        publishPlaylists(updated)
        PlaylistMutationResult.Success(playlistId)
    }

    override fun updatePlaylistSongIds(
        playlistId: Long,
        songIds: List<Long>,
    ): Deferred<PlaylistMutationResult> = enqueueMutation("playlist.update_songs") {
        val current = _userPlaylists.value.firstOrNull { it.id == playlistId }
            ?: return@enqueueMutation PlaylistMutationResult.NotFound
        if (current.isSystem) return@enqueueMutation PlaylistMutationResult.NotAllowed
        val normalizedSongIds = normalizePlaylistSongIds(songIds)
        val updated = updatePlaylistSongIdsEntry(_userPlaylists.value, playlistId, normalizedSongIds)
            ?: return@enqueueMutation PlaylistMutationResult.Success(playlistId, changed = false)
        if (updated == _userPlaylists.value) {
            return@enqueueMutation PlaylistMutationResult.Success(playlistId, changed = false)
        }
        val playlist = updated.first { it.id == playlistId }
        dao.replacePlaylistEntriesAndVerify(playlistId, playlist.songIds)
        publishPlaylists(updated)
        PlaylistMutationResult.Success(playlistId)
    }

    override fun importPlaylists(playlists: List<Playlist>): Deferred<PlaylistMutationResult> =
        enqueueMutation("playlist.import") {
            if (playlists.isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput
            val imported = distinctImportedPlaylists(_userPlaylists.value, playlists)
                .map { it.copy(id = newId()) }
            if (imported.isEmpty()) return@enqueueMutation PlaylistMutationResult.Success(changed = false)
            dao.insertPlaylistsWithEntries(
                playlists = imported.map(Playlist::toEntity),
                entries = imported.flatMap(Playlist::toEntryEntities),
            )
            publishPlaylists(_userPlaylists.value + imported)
            PlaylistMutationResult.Success(changed = true)
        }

    override fun deletePlaylists(playlistIds: Set<Long>): Deferred<PlaylistMutationResult> = enqueueMutation("playlist.delete") {
        if (playlistIds.isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput
        val selected = _userPlaylists.value.filter { it.id in playlistIds }
        if (selected.isEmpty()) return@enqueueMutation PlaylistMutationResult.NotFound
        if (selected.any(Playlist::isSystem)) return@enqueueMutation PlaylistMutationResult.NotAllowed
        val updated = deletePlaylistEntries(_userPlaylists.value, playlistIds)
            ?: return@enqueueMutation PlaylistMutationResult.Success(changed = false)
        dao.deletePlaylistsAndVerify(playlistIds)
        publishPlaylists(updated)
        PlaylistMutationResult.Success(changed = true)
    }

    override fun createSmartPlaylist(name: String): Deferred<PlaylistMutationResult> {
        return enqueueMutation("smart_playlist.create") {
            val id = newId()
            val result = createSmartPlaylistEntry(
                playlists = _userSmartPlaylists.value,
                name = name,
                nextSmartPlaylistId = id,
                nowMs = clock.wallTimeMs(),
            ) ?: return@enqueueMutation PlaylistMutationResult.InvalidInput
            dao.upsertSmartPlaylistAndVerify(result.createdPlaylist.toEntity())
            publishSmartPlaylists(result.playlists)
            PlaylistMutationResult.Success(result.createdPlaylist.id)
        }
    }

    override fun createSmartPlaylist(playlist: SmartPlaylist): Deferred<PlaylistMutationResult> {
        return enqueueMutation("smart_playlist.create_definition") {
            val id = newId()
            val nowMs = clock.wallTimeMs()
            val result = createSmartPlaylistEntry(
                playlists = _userSmartPlaylists.value,
                name = playlist.name,
                nextSmartPlaylistId = id,
                nowMs = nowMs,
            ) ?: return@enqueueMutation PlaylistMutationResult.InvalidInput
            val created = result.createdPlaylist.copy(
                matchMode = playlist.matchMode,
                rules = playlist.rules,
                sort = playlist.sort,
                limit = playlist.limit,
                createdAtMs = playlist.createdAtMs.takeIf { it > 0L } ?: nowMs,
                updatedAtMs = nowMs,
            )
            val updatedPlaylists = result.playlists.map { current ->
                if (current.id == created.id) created else current
            }
            dao.upsertSmartPlaylistAndVerify(created.toEntity())
            publishSmartPlaylists(updatedPlaylists)
            PlaylistMutationResult.Success(created.id)
        }
    }

    override fun updateSmartPlaylist(playlist: SmartPlaylist): Deferred<PlaylistMutationResult> = enqueueMutation("smart_playlist.update") {
        val current = _userSmartPlaylists.value.firstOrNull { it.id == playlist.id }
            ?: return@enqueueMutation PlaylistMutationResult.NotFound
        if (current.isBuiltIn) return@enqueueMutation PlaylistMutationResult.NotAllowed
        val updated = updateSmartPlaylistEntry(
            playlists = _userSmartPlaylists.value,
            playlist = playlist,
            nowMs = clock.wallTimeMs(),
        ) ?: return@enqueueMutation if (playlist.name.trim().isBlank()) {
            PlaylistMutationResult.InvalidInput
        } else {
            PlaylistMutationResult.Success(playlist.id, changed = false)
        }
        val persisted = updated.first { it.id == playlist.id }
        dao.upsertSmartPlaylistAndVerify(persisted.toEntity())
        publishSmartPlaylists(updated)
        PlaylistMutationResult.Success(playlist.id)
    }

    override fun deleteSmartPlaylists(playlistIds: Set<Long>): Deferred<PlaylistMutationResult> = enqueueMutation("smart_playlist.delete") {
        if (playlistIds.isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput
        if (_userSmartPlaylists.value.any { it.id in playlistIds && it.isBuiltIn }) {
            return@enqueueMutation PlaylistMutationResult.NotAllowed
        }
        val updated = deleteSmartPlaylistEntries(_userSmartPlaylists.value, playlistIds)
            ?: return@enqueueMutation PlaylistMutationResult.NotFound
        dao.deleteSmartPlaylistsAndVerify(playlistIds)
        publishSmartPlaylists(updated)
        PlaylistMutationResult.Success(changed = true)
    }

    override fun toggleFavoriteSong(songId: Long) {
        if (songId == 0L) return
        enqueue("favorite.toggle") {
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
        enqueue("favorite.set") {
            if (favorite) {
                val current = _favoriteSongIds.value.toMutableList()
                val currentIds = current.toHashSet()
                val additions = normalized.filterNot(currentIds::contains)
                if (additions.isEmpty()) return@enqueue
                val firstPosition = dao.lastFavoritePosition() + 1
                dao.insertFavorites(additions.mapIndexed { index, songId ->
                    FavoriteSongEntity(songId, firstPosition + index)
                })
                current += additions
                publishFavorites(current)
            } else {
                val ids = normalized.toSet()
                dao.removeFavorites(ids)
                publishFavorites(_favoriteSongIds.value.filterNot(ids::contains))
            }
        }
    }

    override fun removeSongReferences(songIds: Set<Long>): Deferred<PlaylistMutationResult> = enqueueMutation("playlist.remove_song_references") {
        if (songIds.isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput
        run {
            dao.removeSongReferences(songIds)
            val playlists = removeSongReferencesFromPlaylists(_userPlaylists.value, songIds)
                ?: _userPlaylists.value
            publishPlaylists(playlists)
            publishFavorites(_favoriteSongIds.value.filterNot(songIds::contains))
        }
        PlaylistMutationResult.Success(changed = true)
    }

    /** Re-keys persisted media references after library reconciliation proves a relocation. */
    fun relocateSongReferences(replacements: Map<Long, Long>): Deferred<PlaylistMutationResult> =
        enqueueMutation("library.relocate_song_references") {
            val normalized = replacements.filter { (before, after) ->
                before > 0L && after > 0L && before != after
            }
            if (normalized.isEmpty()) return@enqueueMutation PlaylistMutationResult.InvalidInput

            dao.relocateSongReferences(normalized)
            val playlists = _userPlaylists.value.map { playlist ->
                playlist.copy(
                    songIds = playlist.songIds
                        .map { resolveRelocatedSongId(it, normalized) }
                        .distinct(),
                )
            }
            publishPlaylists(playlists)
            publishFavorites(
                _favoriteSongIds.value
                    .map { resolveRelocatedSongId(it, normalized) }
                    .distinct(),
            )
            playbackHistoryStore.relocateSongIds(normalized)
            PlaylistMutationResult.Success(changed = true)
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
        lifecycle.set(StoreLifecycle.Releasing)
        synchronized(submissionLock) {
            promoteCoalescedOperationLocked()
            closeOperationsIfDrainedLocked()
        }
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
        var migrationRequired = false
        try {
            migrationRequired = !dao.migrationComplete(MIGRATION_ID)
            if (migrationRequired) {
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
            val snapshot = loadSnapshot()
            publishSnapshot(snapshot)
            persistRecoverySnapshot(snapshot)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: SQLException) {
            if (!migrationRequired && restoreFromRecoverySnapshot()) return
            onMigrationFailure(legacy, failure)
        } catch (failure: IllegalStateException) {
            if (!migrationRequired && restoreFromRecoverySnapshot()) return
            onMigrationFailure(legacy, failure)
        }
    }

    private suspend fun restoreFromRecoverySnapshot(): Boolean {
        val recovery = recoverySnapshot?.read() ?: return false
        return try {
            dao.restoreUserData(
                playlists = recovery.playlists.map(Playlist::toEntity),
                playlistEntries = recovery.playlists.flatMap(Playlist::toEntryEntities),
                smartPlaylists = recovery.smartPlaylists.map(SmartPlaylist::toEntity),
                favorites = recovery.favoriteSongIds.mapIndexed { index, id -> FavoriteSongEntity(id, index) },
                songCounts = recovery.songPlayCounts.map { (id, count) -> SongPlayCountEntity(id, count) },
                albumCounts = recovery.albumPlayCounts.map { (id, count) -> AlbumPlayCountEntity(id, count) },
                recentPlayback = recovery.recentSongIds.toRecentEntities(RECENT_KIND_SONG) +
                    recovery.recentAlbumIds.toRecentEntities(RECENT_KIND_ALBUM),
                searchHistory = recovery.searchHistory.mapIndexed { index, entry -> entry.toEntity(index) },
                collectionState = PlaybackCollectionStateEntity(
                    kind = recovery.lastPlayedCollectionKind?.name,
                    collectionId = recovery.lastPlayedCollectionId,
                ),
            )
            val restored = loadSnapshot()
            publishSnapshot(restored)
            persistRecoverySnapshot(restored)
            true
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: SQLException) {
            Log.e(TAG, "User-data recovery snapshot could not be restored.", failure)
            false
        } catch (failure: IllegalStateException) {
            Log.e(TAG, "User-data recovery snapshot could not be restored.", failure)
            false
        }
    }

    private fun onMigrationFailure(legacy: UserDataSnapshot, failure: RuntimeException): Nothing {
        Log.e(TAG, "User-data migration failed; legacy data remains available for retry.", failure)
        publishSnapshot(legacy)
        throw failure
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

    private fun persistRecoverySnapshot(snapshot: UserDataSnapshot) {
        val recovery = recoverySnapshot ?: return
        try {
            recovery.write(snapshot)
            recoveryWriteFailureReported.set(false)
        } catch (failure: Exception) {
            if (recoveryWriteFailureReported.compareAndSet(false, true)) {
                Log.w(TAG, "User-data recovery snapshot write failed.", failure)
            }
        }
    }

    private fun publishPlaylists(playlists: List<Playlist>) {
        if (_userPlaylists.value == playlists) return
        _userPlaylists.value = playlists
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

    private fun enqueue(name: String, operation: suspend () -> Unit) {
        tryEnqueue(RoomOperation(name, operation))
    }

    private fun enqueueCoalesced(name: String, operation: suspend () -> Unit) {
        tryEnqueue(RoomOperation(name, operation), coalescible = true)
    }

    private fun enqueueMutation(
        name: String,
        operation: suspend () -> PlaylistMutationResult,
    ): Deferred<PlaylistMutationResult> {
        val completion = CompletableDeferred<PlaylistMutationResult>()
        val accepted = tryEnqueue(
            RoomOperation(
                name = name,
                block = { completion.complete(operation()) },
                completion = completion,
            ),
        )
        if (!accepted) {
            completion.complete(submissionFailure("User-data queue is full."))
        }
        return completion
    }

    private fun submissionFailure(message: String = "User-data store is released."): PlaylistMutationResult {
        val failure = actorFailure.get()
        return if (failure != null) {
            PlaylistMutationResult.Failure("User-data store failed.", failure)
        } else {
            PlaylistMutationResult.Failure(message)
        }
    }

    private fun tryEnqueue(operation: RoomOperation, coalescible: Boolean = false): Boolean {
        synchronized(submissionLock) {
            if (released.get() || lifecycle.get() == StoreLifecycle.Failed) return false
            if (operations.trySend(operation).isSuccess) {
                val depth = queueDepth.incrementAndGet()
                maxQueueDepth.updateAndGet { current -> maxOf(current, depth) }
                return true
            }
            if (coalescible) {
                coalescedOperations[operation.name] = operation
                return true
            }
            return false
        }
    }

    private fun promoteCoalescedOperation() {
        synchronized(submissionLock) {
            promoteCoalescedOperationLocked()
            closeOperationsIfDrainedLocked()
        }
    }

    private fun promoteCoalescedOperationLocked() {
        if (coalescedOperations.isEmpty()) return
        val entry = coalescedOperations.entries.first()
        if (!operations.trySend(entry.value).isSuccess) return
        coalescedOperations.remove(entry.key)
        val depth = queueDepth.incrementAndGet()
        maxQueueDepth.updateAndGet { current -> maxOf(current, depth) }
    }

    private fun closeOperationsIfDrainedLocked() {
        if (released.get() && queueDepth.get() == 0 && coalescedOperations.isEmpty()) {
            operations.close()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runOperation(operation: RoomOperation) {
        try {
            operation.block()
            persistRecoverySnapshot(currentSnapshot())
        } catch (failure: CancellationException) {
            if (currentCoroutineContext().isActive) {
                operation.completion?.cancel(failure)
            } else {
                throw failure
            }
        } catch (failure: SQLException) {
            Log.e(TAG, "User-data operation failed: ${operation.name}.", failure)
            operation.completion?.complete(PlaylistMutationResult.Failure("Database operation failed.", failure))
        } catch (failure: RuntimeException) {
            Log.e(TAG, "User-data operation failed: ${operation.name}.", failure)
            operation.completion?.complete(PlaylistMutationResult.Failure("User-data operation failed.", failure))
        } catch (failure: Error) {
            Log.e(TAG, "User-data actor encountered an unrecoverable operation failure: ${operation.name}.", failure)
            operation.completion?.complete(PlaylistMutationResult.Failure("User-data actor failed.", failure))
            throw failure
        }
    }

    private fun currentSnapshot(): UserDataSnapshot = UserDataSnapshot(
        playlists = _userPlaylists.value,
        smartPlaylists = _userSmartPlaylists.value,
        favoriteSongIds = _favoriteSongIds.value,
        songPlayCounts = playbackHistoryStore.songPlayCounts.value,
        albumPlayCounts = playbackHistoryStore.albumPlayCounts.value,
        recentSongIds = playbackHistoryStore.recentSongIds.value,
        recentAlbumIds = playbackHistoryStore.recentAlbumIds.value,
        lastPlayedCollectionKind = playbackHistoryStore.lastPlayedCollectionKind.value,
        lastPlayedCollectionId = playbackHistoryStore.lastPlayedCollectionId.value,
        searchHistory = searchHistoryStore.searchHistory.value,
    )

    private fun failActor(failure: Throwable) {
        if (released.get()) return
        actorFailure.compareAndSet(null, failure)
        lifecycle.set(StoreLifecycle.Failed)
        Log.e(TAG, "User-data actor stopped; rejecting future operations.", failure)
        synchronized(submissionLock) {
            operations.close()
            rejectPendingOperationsLocked()
        }
    }

    private fun rejectPendingOperationsLocked() {
        rejectQueuedOperations()
        coalescedOperations.values.forEach { operation ->
            operation.completion?.complete(submissionFailure())
        }
        coalescedOperations.clear()
    }

    private fun rejectQueuedOperations() {
        while (true) {
            val operation = operations.tryReceive().getOrNull() ?: break
            queueDepth.decrementAndGet()
            operation.completion?.complete(submissionFailure())
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
        const val MAX_OPERATION_QUEUE_DEPTH = 128
    }
}

private enum class StoreLifecycle {
    Initializing,
    Ready,
    Failed,
    Releasing,
    Released,
}

private data class RoomOperation(
    val name: String,
    val block: suspend () -> Unit,
    val completion: CompletableDeferred<PlaylistMutationResult>? = null,
)

internal fun nextPersistentUserDataId(current: Long): Long {
    check(current in 1 until Long.MAX_VALUE) { "User-data ID space is exhausted." }
    return current + 1L
}

internal data class UserDataSnapshot(
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
