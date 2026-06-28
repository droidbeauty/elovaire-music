package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LibraryObserverController(
    appContext: Context,
    private val scanner: MediaStoreScanner,
    private val scope: CoroutineScope,
    private val onObservedRefresh: (forceMediaIndex: Boolean, changedFilePath: String?) -> Unit,
) {
    private val contentResolver = appContext.contentResolver
    private var mediaObserverRegistered = false
    private var libraryFolderObservers: List<RecursiveMusicDirectoryObserver> = emptyList()
    private var observerRebuildJob: Job? = null
    private val recentObservedPaths = linkedMapOf<String, Long>()
    private var suppressObserverRefreshUntilMs = 0L

    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            onObservedMediaChange()
        }

        override fun onChange(
            selfChange: Boolean,
            uri: android.net.Uri?,
        ) {
            onObservedMediaChange()
        }
    }

    fun ensureRegistered(forceRebuildDirectoryObserver: Boolean = false) {
        ensureMediaObserverRegistered()
        ensureLibraryFolderObservers(forceRebuild = forceRebuildDirectoryObserver)
    }

    fun release() {
        observerRebuildJob?.cancel()
        observerRebuildJob = null
        recentObservedPaths.clear()
        suppressObserverRefreshUntilMs = 0L
        libraryFolderObservers.forEach(RecursiveMusicDirectoryObserver::stopWatching)
        libraryFolderObservers = emptyList()
        unregisterMediaObserver()
    }

    fun setSuppressRefreshUntil(timestampMs: Long) {
        suppressObserverRefreshUntilMs = timestampMs
    }

    private fun onObservedMediaChange() {
        if (System.currentTimeMillis() < suppressObserverRefreshUntilMs) return
        onObservedRefresh(false, null)
    }

    private fun ensureMediaObserverRegistered() {
        if (mediaObserverRegistered) return
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver,
        )
        mediaObserverRegistered = true
    }

    private fun unregisterMediaObserver() {
        if (!mediaObserverRegistered) return
        runCatching {
            contentResolver.unregisterContentObserver(mediaObserver)
        }
        mediaObserverRegistered = false
    }

    fun ensureLibraryFolderObservers(forceRebuild: Boolean = false) {
        val roots = scanner.scanRoots()
        val rootPaths = roots.map(File::getAbsolutePath)
        if (!forceRebuild && libraryFolderObservers.map(RecursiveMusicDirectoryObserver::rootPath) == rootPaths) return
        libraryFolderObservers.forEach(RecursiveMusicDirectoryObserver::stopWatching)
        libraryFolderObservers = roots.mapNotNull(::createMusicDirectoryObserver)
            .also { observers -> observers.forEach(RecursiveMusicDirectoryObserver::startWatching) }
    }

    private fun requestMusicDirectoryObserverRebuild() {
        observerRebuildJob?.cancel()
        observerRebuildJob = scope.launch {
            delay(AUTO_REFRESH_DEBOUNCE_MS)
            observerRebuildJob = null
            if (libraryFolderObservers.isNotEmpty()) {
                libraryFolderObservers.forEach(RecursiveMusicDirectoryObserver::rebuildWatchingTree)
            } else {
                ensureLibraryFolderObservers(forceRebuild = true)
            }
        }
    }

    private fun shouldCoalesceObservedPath(path: String): Boolean {
        val nowMs = SystemClock.elapsedRealtime()
        recentObservedPaths.entries.removeIf { (_, observedAtMs) ->
            nowMs - observedAtMs > OBSERVED_PATH_COALESCE_WINDOW_MS
        }
        val lastObservedAtMs = recentObservedPaths[path]
        recentObservedPaths[path] = nowMs
        return lastObservedAtMs != null && nowMs - lastObservedAtMs < OBSERVED_PATH_COALESCE_WINDOW_MS
    }

    private fun createMusicDirectoryObserver(rootDirectory: File): RecursiveMusicDirectoryObserver? {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory) return null

        return RecursiveMusicDirectoryObserver(rootDirectory) { event, changedFile ->
            if (System.currentTimeMillis() < suppressObserverRefreshUntilMs) return@RecursiveMusicDirectoryObserver
            if (event and DIRECTORY_STRUCTURE_CHANGE_MASK != 0) {
                requestMusicDirectoryObserverRebuild()
            }
            val requiresFullMediaIndexRefresh = event and FULL_INDEX_REFRESH_EVENT_MASK != 0
            val normalizedChangedPath = changedFile?.absolutePath?.normalizedObservedPath()
            if (
                !requiresFullMediaIndexRefresh &&
                normalizedChangedPath != null &&
                shouldCoalesceObservedPath(normalizedChangedPath)
            ) {
                return@RecursiveMusicDirectoryObserver
            }
            if (changedFile == null || changedFile.isDirectory || isSupportedAudioExtension(changedFile.extension)) {
                onObservedRefresh(
                    requiresFullMediaIndexRefresh,
                    if (requiresFullMediaIndexRefresh) null else normalizedChangedPath,
                )
            }
        }
    }

    private inner class RecursiveMusicDirectoryObserver(
        private val rootDirectory: File,
        private val onEventReceived: (event: Int, changedFile: File?) -> Unit,
    ) {
        val rootPath: String = rootDirectory.absolutePath
        private val observers = linkedMapOf<String, FileObserver>()
        private var lastTreeSignature: Int? = null

        fun startWatching() {
            rebuildObservers(force = true)
        }

        fun rebuildWatchingTree() {
            rebuildObservers(force = false)
        }

        fun stopWatching() {
            observers.values.forEach(FileObserver::stopWatching)
            observers.clear()
        }

        private fun rebuildObservers(force: Boolean) {
            if (!rootDirectory.exists() || !rootDirectory.isDirectory) {
                lastTreeSignature = null
                stopWatching()
                return
            }
            val nextDirectories = rootDirectory.walkTopDown()
                .maxDepth(8)
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .toList()
            val nextSignature = nextDirectories
                .sorted()
                .fold(17) { acc, path -> 31 * acc + path.hashCode() }
            if (!force && lastTreeSignature == nextSignature) return
            lastTreeSignature = nextSignature
            stopWatching()
            nextDirectories.forEach { path ->
                observeDirectory(File(path))
            }
        }

        private fun observeDirectory(directory: File) {
            val observer = object : FileObserver(directory, OBSERVER_MASK) {
                override fun onEvent(
                    event: Int,
                    path: String?,
                ) {
                    if (event == 0) return
                    onEventReceived(event, path?.let { File(directory, it) })
                }
            }
            observer.startWatching()
            observers[directory.absolutePath] = observer
        }
    }

    private fun String.normalizedObservedPath(): String? {
        return trim()
            .takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.absolutePath
    }

    private companion object {
        const val AUTO_REFRESH_DEBOUNCE_MS = 350L
        const val OBSERVED_PATH_COALESCE_WINDOW_MS = 900L
        const val OBSERVER_MASK =
            FileObserver.CREATE or
                FileObserver.CLOSE_WRITE or
                FileObserver.MOVED_TO or
                FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MODIFY or
                FileObserver.MOVE_SELF
        const val DIRECTORY_STRUCTURE_CHANGE_MASK =
            FileObserver.CREATE or
                FileObserver.MOVED_TO or
                FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF
        const val FULL_INDEX_REFRESH_EVENT_MASK =
            FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF
    }
}
