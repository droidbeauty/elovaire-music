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
    fun migrateOneToTwoCreatesUserDataSchemaAndReopens() {
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
            2,
            true,
            ElovaireDatabase.MIGRATION_1_2,
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
        }

        val roomDatabase = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ElovaireDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(ElovaireDatabase.MIGRATION_1_2).build()
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

    private companion object {
        const val DATABASE_NAME = "migration-test"
    }
}
