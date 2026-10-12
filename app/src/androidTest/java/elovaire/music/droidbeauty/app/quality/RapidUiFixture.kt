package elovaire.music.droidbeauty.app.quality

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.MediaFilePathResolver
import elovaire.music.droidbeauty.app.data.settings.PortableSettingsBackup
import elovaire.music.droidbeauty.app.data.settings.PreferenceCollectionCodec
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import elovaire.music.droidbeauty.app.data.settings.elovaireSettingsDataStore
import elovaire.music.droidbeauty.app.data.settings.editSettings
import elovaire.music.droidbeauty.app.data.settings.putString
import elovaire.music.droidbeauty.app.data.settings.remove
import java.io.IOException
import java.io.File
import java.util.UUID
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
    private val bootSnapshotPreferences = context.getSharedPreferences(BOOT_SNAPSHOT_FILE_NAME, Context.MODE_PRIVATE)
    private val settingsDataStore = context.elovaireSettingsDataStore()
    private val insertedUris = ArrayList<Uri>(FIXTURES.size)
    private val runId = UUID.randomUUID().toString()
    private val relativePath = "$FIXTURE_RELATIVE_PATH_PREFIX$runId/"
    private var originalSharedPreferences: SharedPreferencesSnapshot? = null
    private var originalBootSnapshot: SharedPreferencesSnapshot? = null
    private var originalDataStoreLibraryFolders: String? = null
    private var hadOriginalDataStoreLibraryFolders = false
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
        val cleanupFailures = mutableListOf<Throwable>()
        try {
            insertedUris.forEach { uri ->
                captureCleanupFailure(cleanupFailures) {
                    if (resolver.delete(uri, null, null) != 1) {
                        error("A rapid UI fixture row was not removed.")
                    }
                }
            }
        } finally {
            insertedUris.clear()
            captureCleanupFailure(cleanupFailures) {
                restoreLibraryFolderSelection()
            }
        }
        if (cleanupFailures.isNotEmpty()) {
            val failure = AssertionError("Rapid UI fixture cleanup failed.")
            cleanupFailures.forEach(failure::addSuppressed)
            throw failure
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
                check(resolver.delete(
                    Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getString(idIndex)),
                    null,
                    null,
                ) == 1) { "A stale rapid UI fixture row was not removed." }
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
        val libraryFoldersKey = stringPreferencesKey(LIBRARY_FOLDERS_KEY)
        if (!libraryFoldersCaptured) {
            originalSharedPreferences = SharedPreferencesSnapshot.capture(libraryPreferences)
            originalBootSnapshot = SharedPreferencesSnapshot.capture(bootSnapshotPreferences)
            runBlocking {
                val settings = settingsDataStore.data.first()
                hadOriginalDataStoreLibraryFolders = settings.contains(libraryFoldersKey)
                originalDataStoreLibraryFolders = settings[libraryFoldersKey]
            }
            libraryFoldersCaptured = true
        }
        fixtureRootSelected = true
        check(libraryPreferences.edit().putString(LIBRARY_FOLDERS_KEY, encodedSelection).commit())
        runBlocking {
            settingsDataStore.editSettings { putString(LIBRARY_FOLDERS_KEY, encodedSelection) }
        }
        updateBootLibraryFolderSelection(encodedSelection)
    }

    private fun restoreLibraryFolderSelection() {
        if (!fixtureRootSelected) return
        val failures = mutableListOf<Throwable>()
        captureCleanupFailure(failures) {
            runBlocking {
                settingsDataStore.editSettings {
                    if (hadOriginalDataStoreLibraryFolders) {
                        putString(LIBRARY_FOLDERS_KEY, checkNotNull(originalDataStoreLibraryFolders))
                    } else {
                        remove(LIBRARY_FOLDERS_KEY)
                    }
                }
            }
        }
        listOf(
            { originalSharedPreferences?.restore(libraryPreferences) },
            { originalBootSnapshot?.restore(bootSnapshotPreferences) },
        ).forEach { restore ->
            captureCleanupFailure(failures) {
                restore()
            }
        }
        fixtureRootSelected = false
        if (failures.isNotEmpty()) {
            throw AssertionError("Unable to restore rapid UI fixture settings.").apply {
                failures.forEach(::addSuppressed)
            }
        }
    }

    private inline fun captureCleanupFailure(failures: MutableList<Throwable>, action: () -> Unit) {
        try {
            action()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            failures += failure
        } catch (failure: IllegalStateException) {
            failures += failure
        } catch (failure: SecurityException) {
            failures += failure
        } catch (failure: IllegalArgumentException) {
            failures += failure
        } catch (failure: UnsupportedOperationException) {
            failures += failure
        }
    }

    private fun updateBootLibraryFolderSelection(encodedSelection: String) {
        val ownerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val backup = PortableSettingsBackup(context, ownerScope = ownerScope)
            val values = backup.bootSettingsSnapshot().asMap() + (LIBRARY_FOLDERS_KEY to encodedSelection)
            backup.checkpointBootSettings(values)
            runBlocking { backup.flushBootSnapshot() }
        } finally {
            ownerScope.cancel()
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
        val mimeType: String = "audio/mpeg",
    )

    private companion object {
        const val NAME_PREFIX = "elovaire-rapid-ui-"
        const val IS_AUDIOBOOK_COLUMN = "is_audiobook"
        const val FIXTURE_RELATIVE_PATH_PREFIX = "Music/ElovaireRapidUi/"
        const val LIBRARY_FOLDERS_KEY = "library_folders"
        const val BOOT_SNAPSHOT_FILE_NAME = "settings_boot_snapshot"
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
