package elovaire.music.droidbeauty.app.quality

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.util.concurrent.atomic.AtomicLong

/** Creates only test-owned MediaStore rows so content-dependent routes are reproducible. */
internal class RapidUiFixture(
    private val context: Context,
    private val assetContext: Context,
) : AutoCloseable {
    private val resolver: ContentResolver = context.contentResolver
    private val insertedUris = ArrayList<Uri>(FIXTURES.size)
    private val runId = NEXT_RUN_ID.incrementAndGet()

    fun install() {
        removeStaleRows()
        FIXTURES.forEachIndexed { index, fixture ->
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "${NAME_PREFIX}${runId}-$index-${fixture.fileName}")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.TITLE, fixture.title)
                put(MediaStore.Audio.Media.ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.ALBUM, fixture.album)
                put(MediaStore.Audio.Media.ALBUM_ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.YEAR, fixture.year)
                put(MediaStore.Audio.Media.TRACK, fixture.track)
                put(MediaStore.Audio.Media.IS_MUSIC, if (fixture.audiobook) 0 else 1)
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, fixture.relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                    put(IS_AUDIOBOOK_COLUMN, if (fixture.audiobook) 1 else 0)
                }
            }
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create rapid UI fixture ${fixture.fileName}")
            insertedUris += uri
            resolver.openOutputStream(uri)?.use { output ->
                assetContext.assets.open("media-metadata/${fixture.fileName}").use { input ->
                    input.copyTo(output)
                }
            } ?: error("Unable to write rapid UI fixture ${fixture.fileName}")
            if (Build.VERSION.SDK_INT >= 29) {
                resolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null,
                )
            }
        }
    }

    override fun close() {
        insertedUris.forEach { uri -> resolver.delete(uri, null, null) }
        insertedUris.clear()
    }

    private fun removeStaleRows() {
        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?",
            arrayOf("$NAME_PREFIX%"),
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (cursor.moveToNext()) {
                resolver.delete(
                    Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getString(idIndex)),
                    null,
                    null,
                )
            }
        }
    }

    private data class Fixture(
        val fileName: String,
        val title: String,
        val artist: String,
        val album: String,
        val year: Int,
        val track: Int,
        val audiobook: Boolean,
        val relativePath: String,
    )

    private companion object {
        const val NAME_PREFIX = "elovaire-rapid-ui-"
        const val IS_AUDIOBOOK_COLUMN = "is_audiobook"
        val NEXT_RUN_ID = AtomicLong()
        val FIXTURES = listOf(
            Fixture("write-fixture.mp3", "Rapid Song One", "Rapid Artist One", "Rapid Album One", 2024, 1, false, "Music/ElovaireRapidUi/"),
            Fixture("write-fixture.flac", "Rapid Song Two", "Rapid Artist One", "Rapid Album One", 2024, 2, false, "Music/ElovaireRapidUi/"),
            Fixture("write-fixture.m4a", "Rapid Song Three", "Rapid Artist Two", "Rapid Album Two", 2025, 1, false, "Music/ElovaireRapidUi/"),
            Fixture("write-fixture.mp3", "Rapid Book Part One", "Rapid Author", "Rapid Book", 2023, 1, true, "Music/ElovaireRapidUi/Audiobooks/Rapid Book/"),
            Fixture("write-fixture.flac", "Rapid Book Part Two", "Rapid Author", "Rapid Book", 2023, 2, true, "Music/ElovaireRapidUi/Audiobooks/Rapid Book/"),
            Fixture("write-fixture.m4a", "Rapid Book Part Three", "Rapid Author", "Rapid Book", 2023, 3, true, "Music/ElovaireRapidUi/Audiobooks/Rapid Book/"),
        )
    }
}
