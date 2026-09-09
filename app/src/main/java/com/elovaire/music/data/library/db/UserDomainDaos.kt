package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
internal interface PlaybackHistoryDao {
    @Query("SELECT * FROM song_play_counts ORDER BY songId")
    suspend fun songPlayCounts(): List<SongPlayCountEntity>

    @Query("SELECT * FROM album_play_counts ORDER BY albumId")
    suspend fun albumPlayCounts(): List<AlbumPlayCountEntity>

    @Query("SELECT * FROM recent_playback ORDER BY kind, position")
    suspend fun recentPlayback(): List<RecentPlaybackEntity>

    @Query("SELECT * FROM playback_collection_state WHERE singletonId = 0")
    suspend fun playbackCollectionState(): PlaybackCollectionStateEntity?

    @Query("DELETE FROM song_play_counts")
    suspend fun clearSongPlayCounts()

    @Query("DELETE FROM album_play_counts")
    suspend fun clearAlbumPlayCounts()

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

    @Transaction
    suspend fun incrementPlaybackCounts(
        songCounts: Map<Long, Int>,
        albumCounts: Map<Long, Int>,
    ) {
        songCounts.forEach { (songId, increment) -> incrementSongPlayCount(songId, increment) }
        albumCounts.forEach { (albumId, increment) -> incrementAlbumPlayCount(albumId, increment) }
    }

    @Query("DELETE FROM recent_playback")
    suspend fun clearRecentPlayback()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecentPlayback(entries: List<RecentPlaybackEntity>)

    @Upsert
    suspend fun upsertPlaybackCollectionState(state: PlaybackCollectionStateEntity)

    @Transaction
    suspend fun replaceRecentPlayback(
        entries: List<RecentPlaybackEntity>,
        state: PlaybackCollectionStateEntity,
    ) {
        clearRecentPlayback()
        if (entries.isNotEmpty()) insertRecentPlayback(entries)
        upsertPlaybackCollectionState(state)
    }
}

@Dao
internal interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY position")
    suspend fun searchHistory(): List<SearchHistoryEntity>

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSearchHistory(entries: List<SearchHistoryEntity>)

    @Transaction
    suspend fun replaceSearchHistory(entries: List<SearchHistoryEntity>) {
        clearSearchHistory()
        if (entries.isNotEmpty()) insertSearchHistory(entries)
    }
}
