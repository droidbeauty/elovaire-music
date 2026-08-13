package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import java.io.File
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LibraryObserverController(
    appContext: Context,
    private val scanner: MediaStoreScanner,
    private val scope: CoroutineScope,
    private val onObservedRefresh: (forceMediaIndex: Boolean, changedFilePath: String?) -> Unit,
    private val clock: AppClock = AndroidAppClock,
) {
    private val contentResolver = appContext.contentResolver
    private var mediaObserverRegistered = false
    private var libraryFolderObservers: List<RecursiveMusicDirectoryObserver> = emptyList()
    private var observerRebuildJob: Job? = null
    @Volatile
    private var directoryObserversEnabled = false
    private val recentObservedPaths = linkedMapOf<String, Long>()
    private val recentObservedPathsLock = Any()
    @Volatile
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

    fun ensureRegistered(
        enableDirectoryObservers: Boolean,
        forceRebuildDirectoryObserver: Boolean = false,
    ) {
        ensureMediaObserverRegistered()
        if (enableDirectoryObservers) {
            ensureLibraryFolderObservers(forceRebuild = forceRebuildDirectoryObserver)
        } else {
            releaseLibraryFolderObservers()
        }
    }

    fun release() {
        observerRebuildJob?.cancel()
        observerRebuildJob = null
        synchronized(recentObservedPathsLock) {
            recentObservedPaths.clear()
        }
        suppressObserverRefreshUntilMs = 0L
        releaseLibraryFolderObservers()
        unregisterMediaObserver()
    }

    fun suppressRefreshFor(durationMs: Long) {
        suppressObserverRefreshUntilMs = clock.elapsedTimeMs() + durationMs.coerceAtLeast(0L)
    }

    private fun onObservedMediaChange() {
        if (clock.elapsedTimeMs() < suppressObserverRefreshUntilMs) return
        onObservedRefresh(false, null)
    }

    private fun ensureMediaObserverRegistered() {
        if (mediaObserverRegistered) return
        mediaObserverRegistered = runCatching {
            contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                mediaObserver,
            )
        }.isSuccess
        logDebug("media store observer active=$mediaObserverRegistered")
    }

    private fun unregisterMediaObserver() {
        if (!mediaObserverRegistered) return
        runCatching {
            contentResolver.unregisterContentObserver(mediaObserver)
        }
        mediaObserverRegistered = false
        logDebug("media store observer active=false")
    }

    fun ensureLibraryFolderObservers(forceRebuild: Boolean = false) {
        directoryObserversEnabled = true
        val currentRootPaths = libraryFolderObservers.map(RecursiveMusicDirectoryObserver::rootPath)
        observerRebuildJob?.cancel()
        val rebuildJob = scope.launch(start = CoroutineStart.LAZY) {
            val currentJob = currentCoroutineContext()[Job] ?: return@launch
            var installed = false
            val observers = withContext(Dispatchers.IO) {
                val roots = scanner.scanRoots()
                val rootPaths = roots.map(File::getAbsolutePath)
                if (!forceRebuild && currentRootPaths == rootPaths) return@withContext null
                roots.mapNotNull(::createMusicDirectoryObserver)
            } ?: return@launch
            try {
                if (observerRebuildJob !== currentJob || !directoryObserversEnabled) return@launch
                withContext(Dispatchers.IO) {
                    observers.forEach(RecursiveMusicDirectoryObserver::startWatching)
                }
                if (observerRebuildJob !== currentJob || !directoryObserversEnabled) return@launch
                observerRebuildJob = null
                stopLibraryFolderObservers()
                libraryFolderObservers = observers
                installed = true
            } finally {
                if (!installed) {
                    withContext(NonCancellable + Dispatchers.IO) {
                        observers.forEach(RecursiveMusicDirectoryObserver::stopWatching)
                    }
                }
            }
        }
        observerRebuildJob = rebuildJob
        rebuildJob.start()
    }

    fun releaseLibraryFolderObservers() {
        directoryObserversEnabled = false
        observerRebuildJob?.cancel()
        observerRebuildJob = null
        stopLibraryFolderObservers()
    }

    private fun stopLibraryFolderObservers() {
        libraryFolderObservers.forEach(RecursiveMusicDirectoryObserver::stopWatching)
        libraryFolderObservers = emptyList()
    }

    private fun requestMusicDirectoryObserverRebuild() {
        scope.launch {
            observerRebuildJob?.cancel()
            val currentJob = currentCoroutineContext()[Job] ?: return@launch
            observerRebuildJob = currentJob
            delay(AUTO_REFRESH_DEBOUNCE_MS)
            if (observerRebuildJob !== currentJob) return@launch
            observerRebuildJob = null
            if (libraryFolderObservers.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    libraryFolderObservers.forEach(RecursiveMusicDirectoryObserver::rebuildWatchingTree)
                }
            } else {
                ensureLibraryFolderObservers(forceRebuild = true)
            }
        }
    }

    private fun shouldCoalesceObservedPath(path: String): Boolean {
        val nowMs = clock.elapsedTimeMs()
        synchronized(recentObservedPathsLock) {
            recentObservedPaths.entries.removeIf { (_, observedAtMs) ->
                nowMs - observedAtMs > OBSERVED_PATH_COALESCE_WINDOW_MS
            }
            val lastObservedAtMs = recentObservedPaths[path]
            recentObservedPaths[path] = nowMs
            return lastObservedAtMs != null && nowMs - lastObservedAtMs < OBSERVED_PATH_COALESCE_WINDOW_MS
        }
    }

    private fun createMusicDirectoryObserver(rootDirectory: File): RecursiveMusicDirectoryObserver? {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory) return null

        return RecursiveMusicDirectoryObserver(
            rootDirectory = rootDirectory,
            onEventReceived = { event, changedFile ->
                scope.launch(Dispatchers.IO) {
                    handleObservedDirectoryEvent(event, changedFile)
                }
            },
            onCoverageIncomplete = { onObservedRefresh(true, null) },
        )
    }

    private fun handleObservedDirectoryEvent(
        event: Int,
        changedFile: File?,
    ) {
        if (!directoryObserversEnabled || clock.elapsedTimeMs() < suppressObserverRefreshUntilMs) return
        if (event and DIRECTORY_STRUCTURE_CHANGE_MASK != 0) {
            requestMusicDirectoryObserverRebuild()
        }
        val requiresFullMediaIndexRefresh = event and FULL_INDEX_REFRESH_EVENT_MASK != 0
        val normalizedChangedPath = changedFile?.absolutePath?.normalizedObservedPath()
        if (requiresFullMediaIndexRefresh) {
            onObservedRefresh(true, null)
            return
        }
        if (
            normalizedChangedPath != null &&
            shouldCoalesceObservedPath(normalizedChangedPath)
        ) {
            return
        }
        if (shouldNotifyForObservedDirectoryEvent(
                event = event,
                changedFileExists = changedFile != null,
                changedFileIsDirectory = changedFile?.isDirectory == true,
                changedFileHasSupportedAudioExtension = changedFile
                    ?.let { isSupportedAudioExtension(it.extension) }
                    == true,
            )
        ) {
            onObservedRefresh(
                false,
                normalizedChangedPath,
            )
        }
    }

    private inner class RecursiveMusicDirectoryObserver(
        private val rootDirectory: File,
        private val onEventReceived: (event: Int, changedFile: File?) -> Unit,
        private val onCoverageIncomplete: () -> Unit,
    ) {
        val rootPath: String = rootDirectory.absolutePath
        private val observers = linkedMapOf<String, FileObserver>()
        private var lastObservedDirectories: List<String>? = null
        private var budgetExceeded = false

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
                lastObservedDirectories = null
                budgetExceeded = false
                stopWatching()
                return
            }
            val tree = snapshotObserverTree(rootDirectory)
            if (tree.reason != null) {
                if (!budgetExceeded) {
                    logDebug(
                        "recursive observers disabled reason=${tree.reason} " +
                            "count=${tree.directories.size} limit=$MAX_RECURSIVE_DIRECTORY_OBSERVERS",
                    )
                    onCoverageIncomplete()
                }
                budgetExceeded = true
                lastObservedDirectories = null
                stopWatching()
                return
            }
            val normalizedDirectories = observerTreeIdentity(tree.directories)
            budgetExceeded = false
            if (!force && lastObservedDirectories == normalizedDirectories) return
            lastObservedDirectories = normalizedDirectories
            stopWatching()
            normalizedDirectories.forEach { path ->
                observeDirectory(File(path))
            }
            logDebug("recursive observers active count=${normalizedDirectories.size}")
        }

        private fun snapshotObserverTree(root: File): ObserverTreeSnapshot {
            val pending = ArrayDeque<Pair<File, Int>>()
            pending.add(root to 0)
            val directories = ArrayList<String>()
            var reason: String? = null
            while (pending.isNotEmpty() && reason == null) {
                val (directory, depth) = pending.removeFirst()
                if (!directory.isDirectory || directory.isSymbolicLinkSafely()) continue
                directories += directory.absolutePath
                if (directories.size > MAX_RECURSIVE_DIRECTORY_OBSERVERS) {
                    reason = "observer budget exceeded"
                    break
                }
                directory.listFiles().orEmpty().forEach { child ->
                    if (!child.isDirectory || child.isSymbolicLinkSafely()) return@forEach
                    if (depth >= MAX_RECURSIVE_DIRECTORY_DEPTH) {
                        reason = "directory depth budget exceeded"
                        return@forEach
                    }
                    pending.add(child to depth + 1)
                }
            }
            if (reason == null && pending.isNotEmpty()) reason = "directory traversal budget exceeded"
            return ObserverTreeSnapshot(directories, reason)
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

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "LibraryObserver"
        const val AUTO_REFRESH_DEBOUNCE_MS = 350L
        const val OBSERVED_PATH_COALESCE_WINDOW_MS = 900L
        const val MAX_RECURSIVE_DIRECTORY_OBSERVERS = 512
        const val MAX_RECURSIVE_DIRECTORY_DEPTH = 8
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

private data class ObserverTreeSnapshot(
    val directories: List<String>,
    val reason: String?,
)

internal fun shouldNotifyForObservedDirectoryEvent(
    event: Int,
    changedFileExists: Boolean,
    changedFileIsDirectory: Boolean,
    changedFileHasSupportedAudioExtension: Boolean,
): Boolean {
    if (event and (
            FileObserver.DELETE or
                FileObserver.MOVED_FROM or
                FileObserver.DELETE_SELF or
                FileObserver.MOVE_SELF
        ) != 0
    ) {
        return true
    }
    return !changedFileExists || changedFileIsDirectory || changedFileHasSupportedAudioExtension
}

internal fun observerTreeIdentity(paths: List<String>): List<String> = paths.sorted()
