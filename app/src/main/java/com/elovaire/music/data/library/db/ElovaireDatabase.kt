package elovaire.music.droidbeauty.app.data.library.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
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
        UserDataRevisionEntity::class,
        NetworkInventoryEntity::class,
        NetworkInventorySourceEntity::class,
        LibraryCommitRelocationEntity::class,
        AudiobookProgressEntity::class,
    ],
    version = 10,
    exportSchema = true,
)
internal abstract class ElovaireDatabase : RoomDatabase() {
    abstract fun mediaMutationDao(): MediaMutationDao
    abstract fun networkInventoryDao(): NetworkInventoryDao
    abstract fun persistenceMaintenanceDao(): PersistenceMaintenanceDao
    abstract fun userDataDao(): UserDataDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        fun create(context: Context): ElovaireDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ElovaireDatabase::class.java,
                "elovaire-library.db",
            )
                // The app and the WorkManager maintenance worker can hold separate handles to
                // this file. WAL keeps short user-data writes from blocking integrity checks.
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                )
                .build()
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

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `network_inventory` (" +
                        "`sourceId` TEXT NOT NULL, `relativePath` TEXT NOT NULL, " +
                        "`sizeBytes` INTEGER, `modifiedAtMs` INTEGER, `etag` TEXT, `contentType` TEXT, `sourceEntryId` TEXT, " +
                        "`songId` INTEGER NOT NULL, `albumId` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
                        "`artist` TEXT NOT NULL, `album` TEXT NOT NULL, `albumArtist` TEXT, `releaseYear` INTEGER, " +
                        "`genre` TEXT NOT NULL, `audioFormat` TEXT NOT NULL, `audioQuality` TEXT, " +
                        "`durationMs` INTEGER NOT NULL, `trackNumber` INTEGER NOT NULL, `discNumber` INTEGER NOT NULL, " +
                        "`dateAddedSeconds` INTEGER NOT NULL, `dateModifiedSeconds` INTEGER, " +
                        "`metadataResolved` INTEGER NOT NULL, `artUri` TEXT, `lastSeenGeneration` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`sourceId`, `relativePath`))",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_inventory_sourceId_lastSeenGeneration` ON `network_inventory` (`sourceId`, `lastSeenGeneration`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_inventory_sourceId_songId` ON `network_inventory` (`sourceId`, `songId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `network_inventory_sources` (" +
                        "`sourceId` TEXT NOT NULL, `generation` INTEGER NOT NULL, " +
                        "`committedAtMs` INTEGER NOT NULL, `availability` TEXT NOT NULL, " +
                        "PRIMARY KEY(`sourceId`))",
                )
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `network_inventory_sources` " +
                        "ADD COLUMN `locationFingerprint` TEXT",
                )
            }
        }

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `songs` ADD COLUMN `mediaKind` TEXT NOT NULL DEFAULT 'Music'")
                db.execSQL("ALTER TABLE `network_inventory` ADD COLUMN `mediaKind` TEXT NOT NULL DEFAULT 'Music'")
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_user_playlist_entries_playlistId`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_user_playlist_entries_songId` " +
                        "ON `user_playlist_entries` (`songId`)",
                )
            }
        }

        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_data_revision` " +
                        "(`singletonId` INTEGER NOT NULL, `revision` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`singletonId`))",
                )
            }
        }

        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // The JSON snapshot is the only runtime library authority. These tables were
                // write-only derived duplicates and are safe to remove without touching user
                // data, mutation journals, or network inventory.
                db.execSQL("DROP TABLE IF EXISTS `metadata_enrichment_state`")
                db.execSQL("DROP TABLE IF EXISTS `media_files`")
                db.execSQL("DROP TABLE IF EXISTS `songs`")
                db.execSQL("DROP TABLE IF EXISTS `albums`")
                db.execSQL("DROP TABLE IF EXISTS `scan_generations`")
            }
        }

        internal val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `library_commit_relocations` " +
                        "(`commitId` TEXT NOT NULL, `appliedAtMs` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`commitId`))",
                )
            }
        }

        internal val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `audiobook_progress` " +
                        "(`bookKey` TEXT NOT NULL, `songId` INTEGER NOT NULL, `positionMs` INTEGER NOT NULL, " +
                        "`completed` INTEGER NOT NULL, `updatedAtMs` INTEGER NOT NULL, " +
                        "`bookElapsedMs` INTEGER, `bookDurationMs` INTEGER, PRIMARY KEY(`bookKey`))",
                )
            }
        }
    }
}
