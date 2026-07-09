package elovaire.music.droidbeauty.app.data.library.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        MediaFileEntity::class,
        LibraryScanGenerationEntity::class,
        MetadataEnrichmentEntity::class,
        LibraryMutationEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
internal abstract class ElovaireDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        fun create(context: Context): ElovaireDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ElovaireDatabase::class.java,
                "elovaire-library.db",
            ).build()
        }
    }
}
