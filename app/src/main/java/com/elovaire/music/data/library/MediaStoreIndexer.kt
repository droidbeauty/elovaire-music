package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.media.MediaScannerConnection
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import java.io.File
import java.nio.file.Files
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

internal interface MediaStoreIndexRefresher {
    fun refreshAll(shouldContinue: () -> Boolean = { true }): MediaStoreIndexRefreshResult

    fun refreshPaths(
        paths: List<String>,
        shouldContinue: () -> Boolean = { true },
    ): MediaStoreIndexRefreshResult
}

internal class MediaStoreIndexer(
    private val context: Context,
    private val scanRoots: () -> List<File>,
) : MediaStoreIndexRefresher {
    override fun refreshAll(shouldContinue: () -> Boolean): MediaStoreIndexRefreshResult {
        shouldContinue()
        val roots = scanRoots()
            .filter { it.exists() && it.isDirectory }
            .distinctBy { it.absolutePath }
        if (roots.isEmpty()) return MediaStoreIndexRefreshResult.Complete

        val pendingChunk = ArrayList<String>(MEDIA_SCANNER_CHUNK_SIZE)
        var result: MediaStoreIndexRefreshResult = MediaStoreIndexRefreshResult.Complete

        fun flushChunk() {
            shouldContinue()
            if (pendingChunk.isEmpty()) return
            result = result.combine(scanAudioPaths(
                paths = pendingChunk,
                timeoutSeconds = MEDIA_SCAN_TIMEOUT_SECONDS,
                shouldContinue = shouldContinue,
            ))
            pendingChunk.clear()
        }

        roots.asSequence()
            .flatMap { root ->
                shouldContinue()
                root.walkTopDown().onEnter { directory ->
                    shouldContinue()
                    !directory.isSymbolicLinkSafely()
                }
            }
            .filter { file -> file.isFile && file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions }
            .map(File::getAbsolutePath)
            .forEach { path ->
                shouldContinue()
                pendingChunk += path
                if (pendingChunk.size >= MEDIA_SCANNER_CHUNK_SIZE) {
                    flushChunk()
                }
            }
        flushChunk()
        return result
    }

    override fun refreshPaths(
        paths: List<String>,
        shouldContinue: () -> Boolean,
    ): MediaStoreIndexRefreshResult {
        shouldContinue()
        return scanAudioPaths(
            paths = audioFilesForPaths(paths).map(File::getAbsolutePath),
            timeoutSeconds = TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS,
            shouldContinue = shouldContinue,
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun scanAudioPaths(
        paths: Iterable<String>,
        timeoutSeconds: Long,
        shouldContinue: () -> Boolean,
    ): MediaStoreIndexRefreshResult {
        shouldContinue()
        val audioPaths = paths
            .map(::File)
            .filter { file ->
                file.exists() &&
                    file.isFile &&
                    file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions
            }
            .map(File::getAbsolutePath)
            .distinct()
        if (audioPaths.isEmpty()) return MediaStoreIndexRefreshResult.Complete

        var timedOutChunks = 0
        audioPaths.chunked(MEDIA_SCANNER_CHUNK_SIZE).forEach { chunk ->
            shouldContinue()
            val latch = CountDownLatch(chunk.size)
            try {
                MediaScannerConnection.scanFile(
                    context,
                    chunk.toTypedArray(),
                    null,
                ) { _, _ ->
                    latch.countDown()
                }
                if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) timedOutChunks += 1
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                return MediaStoreIndexRefreshResult.Unavailable(interrupted)
            } catch (failure: Exception) {
                return MediaStoreIndexRefreshResult.Unavailable(failure)
            }
        }
        return if (timedOutChunks == 0) {
            MediaStoreIndexRefreshResult.Complete
        } else {
            MediaStoreIndexRefreshResult.Partial(timedOutChunks)
        }
    }

    private fun MediaStoreIndexRefreshResult.combine(
        next: MediaStoreIndexRefreshResult,
    ): MediaStoreIndexRefreshResult {
        return when {
            this is MediaStoreIndexRefreshResult.Unavailable -> this
            next is MediaStoreIndexRefreshResult.Unavailable -> next
            this is MediaStoreIndexRefreshResult.Partial && next is MediaStoreIndexRefreshResult.Partial ->
                MediaStoreIndexRefreshResult.Partial(timedOutChunks + next.timedOutChunks)
            this is MediaStoreIndexRefreshResult.Partial -> this
            next is MediaStoreIndexRefreshResult.Partial -> next
            else -> MediaStoreIndexRefreshResult.Complete
        }
    }

    private companion object {
        const val MEDIA_SCAN_TIMEOUT_SECONDS = 8L
        const val TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS = 5L
        const val MEDIA_SCANNER_CHUNK_SIZE = 160
    }
}

internal sealed interface MediaStoreIndexRefreshResult {
    data object Complete : MediaStoreIndexRefreshResult

    data class Partial(val timedOutChunks: Int) : MediaStoreIndexRefreshResult

    data class Unavailable(val failure: Throwable) : MediaStoreIndexRefreshResult
}

internal fun audioFilesForPaths(paths: Iterable<String>): List<File> {
    return paths.asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .map(::File)
        .flatMap { path ->
            when {
                path.isFile -> sequenceOf(path)
                path.isDirectory -> path.walkTopDown()
                    .onEnter { directory -> !directory.isSymbolicLinkSafely() }
                    .filter(File::isFile)
                else -> emptySequence()
            }
        }
        .filter { file -> file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions }
        .distinctBy { it.absolutePath }
        .toList()
}

internal fun File.isSymbolicLinkSafely(): Boolean {
    return runCatching { Files.isSymbolicLink(toPath()) }.getOrDefault(false)
}
