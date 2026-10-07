package elovaire.music.droidbeauty.app.quality

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.MediaFilePathResolver
import elovaire.music.droidbeauty.app.data.settings.PreferenceCollectionCodec
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import java.io.File
import java.util.UUID

/** Creates only test-owned MediaStore rows so content-dependent routes are reproducible. */
internal class RapidUiFixture(
    private val context: Context,
    private val assetContext: Context,
) : AutoCloseable {
    private val resolver: ContentResolver = context.contentResolver
    private val libraryPreferences = context.getSharedPreferences(
        PreferenceStorage.PREFERENCE_FILE_NAME,
        Context.MODE_PRIVATE,
    )
    private val insertedUris = ArrayList<Uri>(FIXTURES.size)
    private val runId = UUID.randomUUID().toString()
    private val relativePath = "$FIXTURE_RELATIVE_PATH_PREFIX$runId/"
    private var originalLibraryFolders: String? = null
    private var hadOriginalLibraryFolders = false
    private var libraryFoldersCaptured = false
    private var fixtureRootSelected = false

    fun install() {
        removeStaleRows()
        FIXTURES.forEachIndexed { index, fixture ->
            val fixturePath = if (fixture.audiobook) "${relativePath}Audiobooks/Rapid Book/" else relativePath
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "${NAME_PREFIX}${runId}-$index-${fixture.fileName}")
                put(MediaStore.Audio.Media.MIME_TYPE, fixture.mimeType)
                put(MediaStore.Audio.Media.TITLE, fixture.title)
                put(MediaStore.Audio.Media.ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.ALBUM, fixture.album)
                put(MediaStore.Audio.Media.ALBUM_ARTIST, fixture.artist)
                put(MediaStore.Audio.Media.YEAR, fixture.year)
                put(MediaStore.Audio.Media.TRACK, fixture.track)
                put(MediaStore.Audio.Media.IS_MUSIC, if (fixture.audiobook) 0 else 1)
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, fixturePath)
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
        selectFixtureRoot()
    }

    fun firstSongTitle(): String {
        val uri = insertedUris.first()
        return checkNotNull(
            resolver.query(uri, arrayOf(MediaStore.Audio.Media.TITLE), null, null, null)?.use { cursor ->
                check(cursor.moveToFirst())
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
            },
        )
    }

    override fun close() {
        try {
            insertedUris.forEach { uri -> resolver.delete(uri, null, null) }
        } finally {
            insertedUris.clear()
            restoreLibraryFolderSelection()
        }
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

    private fun selectFixtureRoot() {
        val selection = LibraryFolderSelection(
            uri = null,
            path = File(MediaFilePathResolver.defaultMusicDirectory(), "ElovaireRapidUi/$runId").absolutePath,
            displayName = "Elovaire UI qualification fixtures",
        )
        val encodedSelection = PreferenceCollectionCodec.serializeLibraryFolder(selection)
        if (!libraryFoldersCaptured) {
            hadOriginalLibraryFolders = libraryPreferences.contains(LIBRARY_FOLDERS_KEY)
            originalLibraryFolders = libraryPreferences.getString(LIBRARY_FOLDERS_KEY, null)
            libraryFoldersCaptured = true
        }
        fixtureRootSelected = true
        check(libraryPreferences.edit().putString(LIBRARY_FOLDERS_KEY, encodedSelection).commit())
    }

    private fun restoreLibraryFolderSelection() {
        if (!fixtureRootSelected) return
        val editor = libraryPreferences.edit()
        if (hadOriginalLibraryFolders) {
            editor.putString(LIBRARY_FOLDERS_KEY, originalLibraryFolders)
        } else {
            editor.remove(LIBRARY_FOLDERS_KEY)
        }
        check(editor.commit())
        fixtureRootSelected = false
    }

    private data class Fixture(
        val fileName: String,
        val title: String,
        val artist: String,
        val album: String,
        val year: Int,
        val track: Int,
        val audiobook: Boolean,
        val mimeType: String = "audio/mpeg",
    )

    private companion object {
        const val NAME_PREFIX = "elovaire-rapid-ui-"
        const val IS_AUDIOBOOK_COLUMN = "is_audiobook"
        const val FIXTURE_RELATIVE_PATH_PREFIX = "Music/ElovaireRapidUi/"
        const val LIBRARY_FOLDERS_KEY = "library_folders"
        val FIXTURES = listOf(
            Fixture(
                "playback-fixture.wav",
                "Original Playback Fixture",
                "Rapid Artist One",
                "Rapid Album One",
                2024,
                1,
                false,
                mimeType = "audio/wav",
            ),
            Fixture("write-fixture.flac", "Rapid Song Two", "Rapid Artist One", "Rapid Album One", 2024, 2, false),
            Fixture("write-fixture.m4a", "Rapid Song Three", "Rapid Artist Two", "Rapid Album Two", 2025, 1, false),
            Fixture("write-fixture.mp3", "Rapid Book Part One", "Rapid Author", "Rapid Book", 2023, 1, true),
            Fixture("write-fixture.flac", "Rapid Book Part Two", "Rapid Author", "Rapid Book", 2023, 2, true),
            Fixture("write-fixture.m4a", "Rapid Book Part Three", "Rapid Author", "Rapid Book", 2023, 3, true),
        )
    }
}
