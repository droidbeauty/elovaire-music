package elovaire.music.droidbeauty.app.data.library.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        MediaFileEntity::class,
        LibraryScanGenerationEntity::class,
        MetadataEnrichmentEntity::class,
        LibraryMutationEntity::class,
        UserPlaylistEntity::class,
        UserPlaylistEntryEntity::class,
        UserSmartPlaylistEntity::class,
        FavoriteSongEntity::class,
        SongPlayCountEntity::class,
        AlbumPlayCountEntity::class,
        RecentPlaybackEntity::class,
        SearchHistoryEntity::class,
        PlaybackCollectionStateEntity::class,
        UserDataMigrationEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
internal abstract class ElovaireDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun persistenceMaintenanceDao(): PersistenceMaintenanceDao
    abstract fun userDataDao(): UserDataDao

    companion object {
        fun create(context: Context): ElovaireDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ElovaireDatabase::class.java,
                "elovaire-library.db",
            ).addMigrations(MIGRATION_1_2).build()
        }

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_playlists` " +
                        "(`playlistId` INTEGER NOT NULL, `name` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`playlistId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_playlist_entries` " +
                        "(`playlistId` INTEGER NOT NULL, `songId` INTEGER NOT NULL, `position` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`playlistId`, `songId`), " +
                        "FOREIGN KEY(`playlistId`) REFERENCES `user_playlists`(`playlistId`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_playlist_entries_playlistId` ON `user_playlist_entries` (`playlistId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_playlist_entries_playlistId_position` ON `user_playlist_entries` (`playlistId`, `position`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_smart_playlists` " +
                        "(`playlistId` INTEGER NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`playlistId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorite_songs` " +
                        "(`songId` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`songId`))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_favorite_songs_position` ON `favorite_songs` (`position`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `song_play_counts` " +
                        "(`songId` INTEGER NOT NULL, `playCount` INTEGER NOT NULL, PRIMARY KEY(`songId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `album_play_counts` " +
                        "(`albumId` INTEGER NOT NULL, `playCount` INTEGER NOT NULL, PRIMARY KEY(`albumId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recent_playback` " +
                        "(`kind` TEXT NOT NULL, `itemId` INTEGER NOT NULL, `position` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`kind`, `itemId`))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_recent_playback_kind_position` ON `recent_playback` (`kind`, `position`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `search_history` " +
                        "(`entryKey` TEXT NOT NULL, `kind` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`subtitle` TEXT NOT NULL, `artUri` TEXT, `albumId` INTEGER, `query` TEXT, " +
                        "`position` INTEGER NOT NULL, PRIMARY KEY(`entryKey`))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_search_history_position` ON `search_history` (`position`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `playback_collection_state` " +
                        "(`singletonId` INTEGER NOT NULL, `kind` TEXT, `collectionId` INTEGER, " +
                        "PRIMARY KEY(`singletonId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_data_migrations` " +
                        "(`migrationId` TEXT NOT NULL, `completedAtMs` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`migrationId`))",
                )
            }
        }
    }
}
