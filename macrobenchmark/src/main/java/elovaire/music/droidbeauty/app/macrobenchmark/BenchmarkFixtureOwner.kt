package elovaire.music.droidbeauty.app.macrobenchmark

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.util.UUID
import org.junit.After
import org.junit.Before

abstract class BenchmarkFixtureOwner {
    private var fixture: BenchmarkMediaFixture? = null

    @Before
    fun installBenchmarkFixture() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        check(device.executeShellCommand("pm clear $TARGET_PACKAGE").contains("Success")) {
            "Unable to reset the isolated benchmark app"
        }
        val mediaFixture = BenchmarkMediaFixture(InstrumentationRegistry.getInstrumentation().context)
        fixture = mediaFixture
        mediaFixture.install()
        val setupResult = device.executeShellCommand(
            "am start -W -n $TARGET_PACKAGE/elovaire.music.droidbeauty.app.BenchmarkFixtureSetupActivity " +
                "--es benchmark_run_id ${mediaFixture.runId}",
        )
        check("Status: ok" in setupResult) { "Unable to select isolated benchmark media" }
    }

    @After
    fun removeBenchmarkFixture() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            check(device.executeShellCommand("pm clear $TARGET_PACKAGE").contains("Success")) {
                "Unable to clear isolated benchmark app data"
            }
        } finally {
            fixture?.close()
            fixture = null
        }
    }
}

private class BenchmarkMediaFixture(
    private val context: Context,
) : AutoCloseable {
    private val resolver: ContentResolver = context.contentResolver
    private val insertedUris = ArrayList<Uri>(FIXTURES.size)
    val runId: String = UUID.randomUUID().toString()
    private val relativePath = "$FIXTURE_RELATIVE_PATH_PREFIX$runId/"

    fun install() {
        removeStaleRows()
        FIXTURES.forEachIndexed { index, fixture ->
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$NAME_PREFIX$runId-$index-${fixture.fileName}")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.TITLE, fixture.title)
                put(MediaStore.Audio.Media.ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.ALBUM, fixture.album)
                put(MediaStore.Audio.Media.ALBUM_ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.YEAR, fixture.year)
                put(MediaStore.Audio.Media.TRACK, fixture.track)
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create benchmark media fixture")
            insertedUris += uri
            resolver.openOutputStream(uri)?.use { output ->
                context.assets.open("media-metadata/${fixture.fileName}").use { input ->
                    input.copyTo(output)
                }
            } ?: error("Unable to write benchmark media fixture")
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
            "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? " +
                "AND ${MediaStore.MediaColumns.OWNER_PACKAGE_NAME} = ?",
            arrayOf("$NAME_PREFIX%", "$FIXTURE_RELATIVE_PATH_PREFIX%", context.packageName),
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
    )

    private companion object {
        const val NAME_PREFIX = "elovaire-benchmark-fixture-"
        const val FIXTURE_RELATIVE_PATH_PREFIX = "Music/ElovaireMacrobenchmark/"
        val FIXTURES = listOf(
            Fixture("write-fixture.mp3", "Benchmark Song One", "Benchmark Artist", "Benchmark Album", 2024, 1),
            Fixture("write-fixture.flac", "Benchmark Song Two", "Benchmark Artist", "Benchmark Album", 2024, 2),
            Fixture("write-fixture.m4a", "Benchmark Song Three", "Benchmark Artist", "Benchmark Album", 2025, 1),
        )
    }
}
