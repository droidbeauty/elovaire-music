package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ElovaireDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ElovaireDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migrateOneToSevenCreatesUserDataSchemaAndReopens() {
        helper.createDatabase(DATABASE_NAME, 1).use { database ->
            database.execSQL(
                "INSERT INTO albums(" +
                    "albumId, title, artist, songCount, durationMs, releaseYear, genre, artUri, " +
                    "lastSeenGenerationId, removedAtMs" +
                    ") VALUES(7, 'Migration Album', 'Artist', 1, 123456, 2024, 'Rock', NULL, 9, NULL)",
            )
            database.execSQL(
                "INSERT INTO songs(" +
                    "songId, mediaStoreId, uri, filePath, fileName, title, artist, album, albumArtist, " +
                    "albumId, durationMs, trackNumber, discNumber, dateAddedSeconds, dateModifiedSeconds, " +
                    "releaseYear, genre, audioFormat, audioQuality, metadataResolved, artUri, " +
                    "volumeNormalization, lastSeenGenerationId, removedAtMs" +
                    ") VALUES(" +
                    "11, 22, 'content://media/external/audio/media/22', " +
                    "'/storage/emulated/0/Music/song.mp3', 'song.mp3', 'Migration Song', 'Artist', " +
                    "'Migration Album', 'Artist', 7, 123456, 1, 1, 100, 200, 2024, 'Rock', 'MP3', " +
                    "'320 kbps', 1, NULL, NULL, 9, NULL" +
                    ")",
            )
        }

        helper.runMigrationsAndValidate(
            DATABASE_NAME,
            7,
            true,
            ElovaireDatabase.MIGRATION_1_2,
            ElovaireDatabase.MIGRATION_2_3,
            ElovaireDatabase.MIGRATION_3_4,
            ElovaireDatabase.MIGRATION_4_5,
            ElovaireDatabase.MIGRATION_5_6,
            ElovaireDatabase.MIGRATION_6_7,
        ).use { database ->
            database.query("SELECT COUNT(*) FROM user_playlists").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
            database.query("SELECT title FROM songs WHERE songId = 11").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Migration Song", cursor.getString(0))
            }
            database.query("PRAGMA foreign_key_check").use { cursor ->
                assertEquals(0, cursor.count)
            }
            database.query("SELECT COUNT(*) FROM network_inventory").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
            database.query("SELECT locationFingerprint FROM network_inventory_sources WHERE sourceId = 'missing'").use { cursor ->
                assertEquals(0, cursor.count)
            }
        }

        val roomDatabase = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ElovaireDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(
            ElovaireDatabase.MIGRATION_1_2,
            ElovaireDatabase.MIGRATION_2_3,
            ElovaireDatabase.MIGRATION_3_4,
            ElovaireDatabase.MIGRATION_4_5,
            ElovaireDatabase.MIGRATION_5_6,
            ElovaireDatabase.MIGRATION_6_7,
        ).build()
        try {
            roomDatabase.openHelper.readableDatabase.query(
                "SELECT COUNT(*) FROM songs WHERE songId = 11 AND albumId = 7",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
        } finally {
            roomDatabase.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrateFiveToSixReplacesRedundantPlaylistIndexWithSongLookupIndex() {
        helper.createDatabase("migration-test-v5", 5).use { database ->
            database.execSQL("INSERT INTO user_playlists(playlistId, name, isSystem) VALUES(1, 'Migration', 0)")
            database.execSQL(
                "INSERT INTO user_playlist_entries(playlistId, songId, position) VALUES(1, 7, 0)",
            )
        }

        helper.runMigrationsAndValidate(
            "migration-test-v5",
            6,
            true,
            ElovaireDatabase.MIGRATION_5_6,
        ).use { database ->
            database.query(
                "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'user_playlist_entries'",
            ).use { cursor ->
                val indexes = buildList {
                    while (cursor.moveToNext()) add(cursor.getString(0))
                }
                assertTrue(indexes.contains("index_user_playlist_entries_songId"))
                assertTrue(!indexes.contains("index_user_playlist_entries_playlistId"))
            }
            database.query("PRAGMA foreign_key_check").use { cursor ->
                assertEquals(0, cursor.count)
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrateTwoToSevenPreservesEveryDurableUserRowAndAddsCurrentContracts() {
        val databaseName = "migration-test-v2-all-rows"
        helper.createDatabase(databaseName, 2).use { database ->
            database.execSQL("INSERT INTO user_playlists(playlistId, name, isSystem) VALUES(4, 'Saved', 0)")
            database.execSQL(
                "INSERT INTO user_playlist_entries(playlistId, songId, position) VALUES(4, 11, 2)",
            )
            database.execSQL(
                "INSERT INTO user_smart_playlists(playlistId, payload) VALUES(4, '{\"kind\":\"recent\"}')",
            )
            database.execSQL("INSERT INTO favorite_songs(songId, position) VALUES(11, 3)")
            database.execSQL("INSERT INTO song_play_counts(songId, playCount) VALUES(11, 5)")
            database.execSQL("INSERT INTO album_play_counts(albumId, playCount) VALUES(7, 6)")
            database.execSQL("INSERT INTO recent_playback(kind, itemId, position) VALUES('song', 11, 1)")
            database.execSQL(
                "INSERT INTO search_history(entryKey, kind, title, subtitle, artUri, albumId, query, position) " +
                    "VALUES('entry-1', 'song', 'Song', 'Artist', NULL, 7, 'song', 0)",
            )
            database.execSQL(
                "INSERT INTO playback_collection_state(singletonId, kind, collectionId) VALUES(1, 'playlist', 4)",
            )
            database.execSQL(
                "INSERT INTO user_data_migrations(migrationId, completedAtMs) VALUES('seed', 1234)",
            )
        }

        helper.runMigrationsAndValidate(
            databaseName,
            7,
            true,
            ElovaireDatabase.MIGRATION_2_3,
            ElovaireDatabase.MIGRATION_3_4,
            ElovaireDatabase.MIGRATION_4_5,
            ElovaireDatabase.MIGRATION_5_6,
            ElovaireDatabase.MIGRATION_6_7,
        ).use { database ->
            assertRowCount(database, "user_playlists", 1)
            assertRowCount(database, "user_playlist_entries", 1)
            assertRowCount(database, "user_smart_playlists", 1)
            assertRowCount(database, "favorite_songs", 1)
            assertRowCount(database, "song_play_counts", 1)
            assertRowCount(database, "album_play_counts", 1)
            assertRowCount(database, "recent_playback", 1)
            assertRowCount(database, "search_history", 1)
            assertRowCount(database, "playback_collection_state", 1)
            assertRowCount(database, "user_data_migrations", 1)
            assertColumnDefault(database, "songs", "mediaKind", "'Music'")
            assertColumnDefault(database, "network_inventory", "mediaKind", "'Music'")
            assertColumn(database, "network_inventory_sources", "locationFingerprint")
            assertTable(database, "user_data_revision")
            assertIndex(database, "index_user_playlist_entries_songId")
            assertNoIndex(database, "index_user_playlist_entries_playlistId")
            database.query("PRAGMA foreign_key_check").use { cursor ->
                assertEquals(0, cursor.count)
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrateEachAdjacentVersionKeepsThePreMigrationDataAndExpectedSchemaChange() {
        val migrations = listOf(
            ElovaireDatabase.MIGRATION_1_2,
            ElovaireDatabase.MIGRATION_2_3,
            ElovaireDatabase.MIGRATION_3_4,
            ElovaireDatabase.MIGRATION_4_5,
            ElovaireDatabase.MIGRATION_5_6,
            ElovaireDatabase.MIGRATION_6_7,
        )
        migrations.forEachIndexed { index, migration ->
            val startVersion = index + 1
            val databaseName = "migration-test-adjacent-$startVersion"
            helper.createDatabase(databaseName, startVersion).use { database ->
                when (startVersion) {
                    1 -> database.execSQL(
                        "INSERT INTO songs(" +
                            "songId, uri, fileName, title, artist, album, albumId, durationMs, trackNumber, " +
                            "discNumber, dateAddedSeconds, dateModifiedSeconds, genre, audioFormat, " +
                            "metadataResolved, lastSeenGenerationId" +
                            ") VALUES(11, 'content://song', 'song.mp3', 'Song', 'Artist', 'Album', 7, 1, 1, 1, 1, 1, '', 'MP3', 1, 1)",
                    )
                    2 -> database.execSQL(
                        "INSERT INTO user_playlists(playlistId, name, isSystem) VALUES(4, 'Saved', 0)",
                    )
                    3 -> database.execSQL(
                        "INSERT INTO network_inventory_sources(sourceId, generation, committedAtMs, availability) " +
                            "VALUES('source-a', 2, 3, 'Available')",
                    )
                    4 -> database.execSQL(
                        "INSERT INTO network_inventory_sources(sourceId, generation, committedAtMs, availability, locationFingerprint) " +
                            "VALUES('source-a', 2, 3, 'Available', 'fingerprint')",
                    )
                    5 -> database.execSQL(
                        "INSERT INTO songs(" +
                            "songId, uri, fileName, title, artist, album, albumId, durationMs, trackNumber, " +
                            "discNumber, dateAddedSeconds, dateModifiedSeconds, genre, audioFormat, metadataResolved, " +
                            "mediaKind, lastSeenGenerationId" +
                            ") VALUES(11, 'content://song', 'song.mp3', 'Song', 'Artist', 'Album', 7, 1, 1, 1, 1, 1, '', 'MP3', 1, 'Music', 1)",
                    )
                    6 -> database.execSQL(
                        "INSERT INTO user_data_migrations(migrationId, completedAtMs) VALUES('seed', 1234)",
                    )
                }
            }
            helper.runMigrationsAndValidate(
                databaseName,
                startVersion + 1,
                true,
                migration,
            ).use { database ->
                when (startVersion) {
                    1 -> assertRowCount(database, "songs", 1)
                    2 -> assertRowCount(database, "user_playlists", 1)
                    3 -> assertColumn(database, "network_inventory_sources", "locationFingerprint")
                    4 -> assertColumnDefault(database, "songs", "mediaKind", "'Music'")
                    5 -> {
                        assertRowCount(database, "songs", 1)
                        assertIndex(database, "index_user_playlist_entries_songId")
                    }
                    6 -> {
                        assertRowCount(database, "user_data_migrations", 1)
                        assertTable(database, "user_data_revision")
                    }
                }
                database.query("PRAGMA foreign_key_check").use { cursor ->
                    assertEquals(0, cursor.count)
                }
            }
        }
    }

    private fun assertRowCount(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        table: String,
        expected: Int,
    ) {
        database.query("SELECT COUNT(*) FROM `$table`").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(expected, cursor.getInt(0))
        }
    }

    private fun assertTable(database: androidx.sqlite.db.SupportSQLiteDatabase, table: String) {
        database.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '$table'").use { cursor ->
            assertTrue(cursor.moveToFirst())
        }
    }

    private fun assertColumn(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        table: String,
        column: String,
    ) {
        database.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            assertTrue(nameIndex >= 0)
            var found = false
            while (cursor.moveToNext()) {
                found = found || cursor.getString(nameIndex) == column
            }
            assertTrue(found)
        }
    }

    private fun assertColumnDefault(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        table: String,
        column: String,
        expectedDefault: String,
    ) {
        database.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            val defaultIndex = cursor.getColumnIndex("dflt_value")
            assertTrue(nameIndex >= 0)
            assertTrue(defaultIndex >= 0)
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) {
                    assertEquals(expectedDefault, cursor.getString(defaultIndex))
                    return@use
                }
            }
            throw AssertionError("Missing $table.$column")
        }
    }

    private fun assertIndex(database: androidx.sqlite.db.SupportSQLiteDatabase, name: String) {
        database.query("SELECT name FROM sqlite_master WHERE type = 'index' AND name = '$name'").use { cursor ->
            assertTrue(cursor.moveToFirst())
        }
    }

    private fun assertNoIndex(database: androidx.sqlite.db.SupportSQLiteDatabase, name: String) {
        database.query("SELECT name FROM sqlite_master WHERE type = 'index' AND name = '$name'").use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    private companion object {
        const val DATABASE_NAME = "migration-test"
    }
}
