package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import java.io.File
import java.io.Closeable
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineDispatcher
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val contentResolver = appContext.contentResolver
    private var mediaObserverRegistered = false
    private var mediaObserverLease: Closeable? = null
    private var libraryFolderObservers: List<RecursiveMusicDirectoryObserver> = emptyList()
    private var libraryFolderObserverLeases: List<Closeable> = emptyList()
    private var observerRebuildJob: Job? = null
    @Volatile
    private var directoryObserversEnabled = false
    private val recentObservedPaths = linkedMapOf<String, Long>()
    private val recentObservedPathsLock = Any()
    private val expectedMutations = ArrayDeque<ExpectedMutation>()
    private val expectedMutationsLock = Any()

    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            onObservedMediaChange(null)
        }

        override fun onChange(
            selfChange: Boolean,
            uri: Uri?,
        ) {
            onObservedMediaChange(uri)
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
        synchronized(expectedMutationsLock) { expectedMutations.clear() }
        releaseLibraryFolderObservers()
        unregisterMediaObserver()
    }

    fun expectSelfMutation(
        paths: Collection<String>,
        uris: Collection<Uri>,
        durationMs: Long,
    ) {
        val normalizedPaths = paths.mapNotNull { it.normalizedObservedPath() }.toSet()
        val normalizedUris = uris.map(Uri::toString).toSet()
        if (normalizedPaths.isEmpty() && normalizedUris.isEmpty()) return
        synchronized(expectedMutationsLock) {
            removeExpiredExpectedMutations()
            val expiresAtMs = clock.elapsedTimeMs() + durationMs.coerceAtLeast(0L)
            normalizedPaths.forEach { path ->
                expectedMutations += ExpectedMutation(path = path, uri = null, expiresAtMs = expiresAtMs)
            }
            normalizedUris.forEach { uri ->
                expectedMutations += ExpectedMutation(path = null, uri = uri, expiresAtMs = expiresAtMs)
            }
        }
    }

    private fun onObservedMediaChange(uri: Uri?) {
        if (consumeExpectedMutation(changedUri = uri, changedPath = null)) return
        onObservedRefresh(false, null)
    }

    private fun consumeExpectedMutation(changedUri: Uri?, changedPath: String?): Boolean {
        synchronized(expectedMutationsLock) {
            removeExpiredExpectedMutations()
            val iterator = expectedMutations.iterator()
            while (iterator.hasNext()) {
                val expected = iterator.next()
                if (expectedMutationTargetMatches(expected.path, expected.uri, changedPath, changedUri?.toString())) {
                    iterator.remove()
                    return true
                }
            }
            return false
        }
    }

    private fun removeExpiredExpectedMutations() {
        val nowMs = clock.elapsedTimeMs()
        expectedMutations.removeIf { it.expiresAtMs <= nowMs }
    }

    private fun ensureMediaObserverRegistered() {
        if (mediaObserverRegistered) return
        val registered = runCatching {
            contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                mediaObserver,
            )
        }.isSuccess
        if (registered) {
            mediaObserverLease = BackendResourceRegistry.acquire(BackendResourceKind.ActiveObserver)
            mediaObserverRegistered = true
        }
        logDebug("media store observer active=$mediaObserverRegistered")
    }

    private fun unregisterMediaObserver() {
        if (!mediaObserverRegistered) return
        val failure = runCatching {
            contentResolver.unregisterContentObserver(mediaObserver)
        }.exceptionOrNull()
        if (failure != null) {
            logDebug("media store observer unregister failed=${failure::class.simpleName}")
            return
        }
        mediaObserverRegistered = false
        mediaObserverLease?.close()
        mediaObserverLease = null
        logDebug("media store observer active=false")
    }

    fun ensureLibraryFolderObservers(forceRebuild: Boolean = false) {
        directoryObserversEnabled = true
        val currentRootPaths = libraryFolderObservers.map(RecursiveMusicDirectoryObserver::rootPath)
        observerRebuildJob?.cancel()
        val rebuildJob = scope.launch(start = CoroutineStart.LAZY) {
            val currentJob = currentCoroutineContext()[Job] ?: return@launch
            var installed = false
            val observers = withContext(ioDispatcher) {
                val roots = scanner.scanRoots()
                val rootPaths = roots.map(File::getAbsolutePath)
                if (!forceRebuild && currentRootPaths == rootPaths) return@withContext null
                roots.mapNotNull(::createMusicDirectoryObserver)
            } ?: return@launch
            try {
                if (observerRebuildJob !== currentJob || !directoryObserversEnabled) return@launch
                withContext(ioDispatcher) {
                    observers.forEach(RecursiveMusicDirectoryObserver::startWatching)
                }
                if (observerRebuildJob !== currentJob || !directoryObserversEnabled) return@launch
                observerRebuildJob = null
                stopLibraryFolderObservers()
                libraryFolderObservers = observers
                libraryFolderObserverLeases = observers.map {
                    BackendResourceRegistry.acquire(BackendResourceKind.ActiveObserver)
                }
                installed = true
            } finally {
                if (!installed) {
                    withContext(NonCancellable + ioDispatcher) {
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
        libraryFolderObserverLeases.forEach(Closeable::close)
        libraryFolderObserverLeases = emptyList()
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
                withContext(ioDispatcher) {
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
            trimRecentObservedPaths(
                recentObservedPaths,
                MAX_RECENT_OBSERVED_PATHS,
            )
            return lastObservedAtMs != null && nowMs - lastObservedAtMs < OBSERVED_PATH_COALESCE_WINDOW_MS
        }
    }

    private fun createMusicDirectoryObserver(rootDirectory: File): RecursiveMusicDirectoryObserver? {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory) return null

        return RecursiveMusicDirectoryObserver(
            rootDirectory = rootDirectory,
            onEventReceived = { event, changedFile ->
                scope.launch(ioDispatcher) {
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
        if (!directoryObserversEnabled) return
        if (event and DIRECTORY_STRUCTURE_CHANGE_MASK != 0) {
            requestMusicDirectoryObserverRebuild()
        }
        val normalizedChangedPath = changedFile?.absolutePath?.normalizedObservedPath()
        if (consumeExpectedMutation(changedUri = null, changedPath = normalizedChangedPath)) return
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
        const val MAX_RECENT_OBSERVED_PATHS = 512
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
    }
}

private data class ExpectedMutation(
    val path: String?,
    val uri: String?,
    val expiresAtMs: Long,
)

internal fun expectedMutationTargetMatches(
    expectedPath: String?,
    expectedUri: String?,
    changedPath: String?,
    changedUri: String?,
): Boolean {
    return expectedPath != null && expectedPath == changedPath ||
        expectedUri != null && expectedUri == changedUri
}

internal fun trimRecentObservedPaths(
    paths: MutableMap<String, Long>,
    maxEntries: Int,
) {
    require(maxEntries > 0)
    while (paths.size > maxEntries) {
        val iterator = paths.entries.iterator()
        if (!iterator.hasNext()) return
        iterator.next()
        iterator.remove()
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
