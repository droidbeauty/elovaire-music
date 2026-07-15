package elovaire.music.droidbeauty.app.data.library.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Rule
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
        helper.createDatabase(DATABASE_NAME, 1).close()

        helper.runMigrationsAndValidate(
            DATABASE_NAME,
            2,
            true,
            ElovaireDatabase.MIGRATION_1_2,
        ).use { database ->
            database.query("SELECT COUNT(*) FROM user_playlists").use { cursor ->
                check(cursor.moveToFirst())
                check(cursor.getInt(0) == 0)
            }
            database.query("PRAGMA foreign_key_check").use { cursor ->
                check(cursor.count == 0)
            }
        }
    }

    private companion object {
        const val DATABASE_NAME = "migration-test"
    }
}
