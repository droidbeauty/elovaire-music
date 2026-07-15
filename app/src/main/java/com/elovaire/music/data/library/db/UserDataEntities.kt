package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "user_playlists")
internal data class UserPlaylistEntity(
    @androidx.room.PrimaryKey val playlistId: Long,
    val name: String,
    val isSystem: Boolean,
)

@Entity(
    tableName = "user_playlist_entries",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = UserPlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["playlistId", "position"], unique = true),
    ],
)
internal data class UserPlaylistEntryEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int,
)

@Entity(tableName = "user_smart_playlists")
internal data class UserSmartPlaylistEntity(
    @androidx.room.PrimaryKey val playlistId: Long,
    val payload: String,
)

@Entity(
    tableName = "favorite_songs",
    indices = [Index(value = ["position"], unique = true)],
)
internal data class FavoriteSongEntity(
    @androidx.room.PrimaryKey val songId: Long,
    val position: Int,
)

@Entity(tableName = "song_play_counts")
internal data class SongPlayCountEntity(
    @androidx.room.PrimaryKey val songId: Long,
    val playCount: Int,
)

@Entity(tableName = "album_play_counts")
internal data class AlbumPlayCountEntity(
    @androidx.room.PrimaryKey val albumId: Long,
    val playCount: Int,
)

@Entity(
    tableName = "recent_playback",
    primaryKeys = ["kind", "itemId"],
    indices = [Index(value = ["kind", "position"], unique = true)],
)
internal data class RecentPlaybackEntity(
    val kind: String,
    val itemId: Long,
    val position: Int,
)

@Entity(
    tableName = "search_history",
    indices = [Index(value = ["position"], unique = true)],
)
internal data class SearchHistoryEntity(
    @androidx.room.PrimaryKey val entryKey: String,
    val kind: String,
    val title: String,
    val subtitle: String,
    val artUri: String?,
    val albumId: Long?,
    val query: String?,
    val position: Int,
)

@Entity(tableName = "playback_collection_state")
internal data class PlaybackCollectionStateEntity(
    @androidx.room.PrimaryKey val singletonId: Int = 0,
    val kind: String?,
    val collectionId: Long?,
)

@Entity(tableName = "user_data_migrations")
internal data class UserDataMigrationEntity(
    @androidx.room.PrimaryKey val migrationId: String,
    val completedAtMs: Long,
)
