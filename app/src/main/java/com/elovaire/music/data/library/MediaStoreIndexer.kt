package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.media.MediaScannerConnection
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import java.io.File
import java.nio.file.Files
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class MediaStoreIndexer(
    private val context: Context,
    private val scanRoots: () -> List<File>,
) {
    fun refreshAll() {
        val roots = scanRoots()
            .filter { it.exists() && it.isDirectory }
            .distinctBy { it.absolutePath }
        if (roots.isEmpty()) return

        val pendingChunk = ArrayList<String>(MEDIA_SCANNER_CHUNK_SIZE)

        fun flushChunk() {
            if (pendingChunk.isEmpty()) return
            scanAudioPaths(
                paths = pendingChunk,
                timeoutSeconds = MEDIA_SCAN_TIMEOUT_SECONDS,
            )
            pendingChunk.clear()
        }

        roots.asSequence()
            .flatMap { root ->
                root.walkTopDown().onEnter { directory -> !directory.isSymbolicLinkSafely() }
            }
            .filter { file -> file.isFile && file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions }
            .map(File::getAbsolutePath)
            .forEach { path ->
                pendingChunk += path
                if (pendingChunk.size >= MEDIA_SCANNER_CHUNK_SIZE) {
                    flushChunk()
                }
            }
        flushChunk()
    }

    fun refreshPaths(paths: List<String>) {
        scanAudioPaths(
            paths = audioFilesForPaths(paths).map(File::getAbsolutePath),
            timeoutSeconds = TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS,
        )
    }

    private fun scanAudioPaths(
        paths: Iterable<String>,
        timeoutSeconds: Long,
    ) {
        val audioPaths = paths
            .map(::File)
            .filter { file ->
                file.exists() &&
                    file.isFile &&
                    file.extension.lowercase(Locale.ROOT) in AudioFormatPolicy.scannerExtensions
            }
            .map(File::getAbsolutePath)
            .distinct()
        if (audioPaths.isEmpty()) return

        audioPaths.chunked(MEDIA_SCANNER_CHUNK_SIZE).forEach { chunk ->
            val latch = CountDownLatch(chunk.size)
            MediaScannerConnection.scanFile(
                context,
                chunk.toTypedArray(),
                null,
            ) { _, _ ->
                latch.countDown()
            }
            latch.await(timeoutSeconds, TimeUnit.SECONDS)
        }
    }

    private companion object {
        const val MEDIA_SCAN_TIMEOUT_SECONDS = 8L
        const val TARGETED_MEDIA_SCAN_TIMEOUT_SECONDS = 5L
        const val MEDIA_SCANNER_CHUNK_SIZE = 160
    }
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
