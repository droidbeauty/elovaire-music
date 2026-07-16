package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
@Suppress("TooManyFunctions")
internal interface UserDataDao {
    @Query("SELECT * FROM user_playlists ORDER BY playlistId")
    suspend fun playlists(): List<UserPlaylistEntity>

    @Query("SELECT * FROM user_playlist_entries ORDER BY playlistId, position")
    suspend fun playlistEntries(): List<UserPlaylistEntryEntity>

    @Query("SELECT * FROM user_smart_playlists ORDER BY playlistId")
    suspend fun smartPlaylists(): List<UserSmartPlaylistEntity>

    @Query("SELECT * FROM favorite_songs ORDER BY position")
    suspend fun favorites(): List<FavoriteSongEntity>

    @Query("SELECT * FROM song_play_counts")
    suspend fun songPlayCounts(): List<SongPlayCountEntity>

    @Query("SELECT * FROM album_play_counts")
    suspend fun albumPlayCounts(): List<AlbumPlayCountEntity>

    @Query("SELECT * FROM recent_playback ORDER BY kind, position")
    suspend fun recentPlayback(): List<RecentPlaybackEntity>

    @Query("SELECT * FROM search_history ORDER BY position")
    suspend fun searchHistory(): List<SearchHistoryEntity>

    @Query("SELECT * FROM playback_collection_state WHERE singletonId = 0")
    suspend fun playbackCollectionState(): PlaybackCollectionStateEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM user_data_migrations WHERE migrationId = :migrationId)")
    suspend fun migrationComplete(migrationId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlaylist(playlist: UserPlaylistEntity)

    @Query("UPDATE user_playlists SET name = :name WHERE playlistId = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)

    @Query("DELETE FROM user_playlists WHERE playlistId IN (:playlistIds)")
    suspend fun deletePlaylists(playlistIds: Set<Long>)

    @Query("DELETE FROM user_playlist_entries WHERE playlistId = :playlistId")
    suspend fun deletePlaylistEntries(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlaylistEntries(entries: List<UserPlaylistEntryEntity>)

    @Upsert
    suspend fun upsertSmartPlaylist(playlist: UserSmartPlaylistEntity)

    @Query("DELETE FROM user_smart_playlists WHERE playlistId IN (:playlistIds)")
    suspend fun deleteSmartPlaylists(playlistIds: Set<Long>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity): Long

    @Query("DELETE FROM favorite_songs WHERE songId IN (:songIds)")
    suspend fun deleteFavorites(songIds: Set<Long>)

    @Query("DELETE FROM favorite_songs")
    suspend fun clearFavorites()

    @Query("SELECT COALESCE(MAX(position), -1) FROM favorite_songs")
    suspend fun lastFavoritePosition(): Int

    @Query(
        "INSERT INTO song_play_counts(songId, playCount) VALUES(:songId, :increment) " +
            "ON CONFLICT(songId) DO UPDATE SET playCount = MIN(playCount + :increment, 2147483647)",
    )
    suspend fun incrementSongPlayCount(songId: Long, increment: Int = 1)

    @Query(
        "INSERT INTO album_play_counts(albumId, playCount) VALUES(:albumId, :increment) " +
            "ON CONFLICT(albumId) DO UPDATE SET playCount = MIN(playCount + :increment, 2147483647)",
    )
    suspend fun incrementAlbumPlayCount(albumId: Long, increment: Int = 1)

    @Query("DELETE FROM recent_playback")
    suspend fun clearRecentPlayback()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecentPlayback(entries: List<RecentPlaybackEntity>)

    @Upsert
    suspend fun upsertPlaybackCollectionState(state: PlaybackCollectionStateEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSearchHistory(entries: List<SearchHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylists(playlists: List<UserPlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSmartPlaylists(playlists: List<UserSmartPlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorites(favorites: List<FavoriteSongEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongPlayCounts(counts: List<SongPlayCountEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbumPlayCounts(counts: List<AlbumPlayCountEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMigration(migration: UserDataMigrationEntity)

    @Transaction
    suspend fun replacePlaylistEntries(playlistId: Long, songIds: List<Long>) {
        deletePlaylistEntries(playlistId)
        val entries = songIds.distinct().mapIndexed { position, songId ->
            UserPlaylistEntryEntity(playlistId, songId, position)
        }
        if (entries.isNotEmpty()) insertPlaylistEntries(entries)
    }

    @Transaction
    suspend fun replaceRecentPlayback(
        entries: List<RecentPlaybackEntity>,
        state: PlaybackCollectionStateEntity,
    ) {
        clearRecentPlayback()
        if (entries.isNotEmpty()) insertRecentPlayback(entries)
        upsertPlaybackCollectionState(state)
    }

    @Transaction
    suspend fun replaceSearchHistory(entries: List<SearchHistoryEntity>) {
        clearSearchHistory()
        if (entries.isNotEmpty()) insertSearchHistory(entries)
    }

    @Transaction
    suspend fun removeSongReferences(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        removeFavorites(songIds)
        val affectedPlaylists = playlistIdsContaining(songIds)
        deleteSongReferencesFromPlaylists(songIds)
        affectedPlaylists.forEach { playlistId ->
            replacePlaylistEntries(playlistId, songIdsForPlaylist(playlistId))
        }
    }

    @Transaction
    suspend fun removeFavorites(songIds: Set<Long>) {
        if (songIds.isEmpty()) return
        deleteFavorites(songIds)
        val remaining = favorites().map(FavoriteSongEntity::songId)
        clearFavorites()
        if (remaining.isNotEmpty()) {
            insertFavorites(remaining.mapIndexed { position, songId -> FavoriteSongEntity(songId, position) })
        }
    }

    @Query("SELECT DISTINCT playlistId FROM user_playlist_entries WHERE songId IN (:songIds)")
    suspend fun playlistIdsContaining(songIds: Set<Long>): List<Long>

    @Query("SELECT songId FROM user_playlist_entries WHERE playlistId = :playlistId ORDER BY position")
    suspend fun songIdsForPlaylist(playlistId: Long): List<Long>

    @Query("DELETE FROM user_playlist_entries WHERE songId IN (:songIds)")
    suspend fun deleteSongReferencesFromPlaylists(songIds: Set<Long>)

    @Transaction
    suspend fun migrateLegacy(
        playlists: List<UserPlaylistEntity>,
        entries: List<UserPlaylistEntryEntity>,
        smartPlaylists: List<UserSmartPlaylistEntity>,
        favorites: List<FavoriteSongEntity>,
        songCounts: List<SongPlayCountEntity>,
        albumCounts: List<AlbumPlayCountEntity>,
        recents: List<RecentPlaybackEntity>,
        searchHistory: List<SearchHistoryEntity>,
        collectionState: PlaybackCollectionStateEntity,
        migration: UserDataMigrationEntity,
    ) {
        if (playlists.isNotEmpty()) insertPlaylists(playlists)
        if (entries.isNotEmpty()) insertPlaylistEntries(entries)
        if (smartPlaylists.isNotEmpty()) insertSmartPlaylists(smartPlaylists)
        if (favorites.isNotEmpty()) insertFavorites(favorites)
        if (songCounts.isNotEmpty()) insertSongPlayCounts(songCounts)
        if (albumCounts.isNotEmpty()) insertAlbumPlayCounts(albumCounts)
        if (recents.isNotEmpty()) insertRecentPlayback(recents)
        if (searchHistory.isNotEmpty()) insertSearchHistory(searchHistory)
        upsertPlaybackCollectionState(collectionState)
        insertMigration(migration)
    }
}
