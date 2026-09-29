package elovaire.music.droidbeauty.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.library.MediaFilePathResolver
import elovaire.music.droidbeauty.app.data.settings.PreferenceCollectionCodec
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage
import java.io.File
import java.util.UUID

class BenchmarkFixtureSetupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runId = intent.getStringExtra(EXTRA_RUN_ID)
        if (!runId.isCanonicalUuid()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        val selection = LibraryFolderSelection(
            uri = null,
            path = File(MediaFilePathResolver.defaultMusicDirectory(), "$FIXTURE_DIRECTORY/$runId").absolutePath,
            displayName = "Benchmark fixtures",
        )
        val encodedSelection = PreferenceCollectionCodec.serializeLibraryFolder(selection)
        val saved = getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LIBRARY_FOLDERS_KEY, encodedSelection)
            .commit()
        setResult(if (saved) RESULT_OK else RESULT_CANCELED)
        finish()
    }

    private fun String?.isCanonicalUuid(): Boolean {
        val value = this ?: return false
        return try {
            UUID.fromString(value).toString() == value
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private companion object {
        const val EXTRA_RUN_ID = "benchmark_run_id"
        const val FIXTURE_DIRECTORY = "ElovaireMacrobenchmark"
        const val LIBRARY_FOLDERS_KEY = "library_folders"
    }
}
