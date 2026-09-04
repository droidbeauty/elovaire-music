package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.C
import androidx.media3.common.DataReader
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.extractor.DefaultExtractorInput
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput
import androidx.media3.extractor.metadata.Chapter
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.domain.model.AudiobookChapter
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.core.backend.BackendEvent
import elovaire.music.droidbeauty.app.core.backend.BackendEventSink
import elovaire.music.droidbeauty.app.core.backend.LogcatBackendEventSink
import elovaire.music.droidbeauty.app.core.backend.emitLazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import java.io.EOFException
import java.io.IOException
import java.util.LinkedHashMap
import kotlin.math.min

internal interface AudiobookChapterReader {
    suspend fun chapters(song: Song): List<AudiobookChapter>

    fun onMemoryPressure(pressure: MemoryPressure) = Unit
}

@UnstableApi
internal class Media3AudiobookChapterReader(
    private val dataSourceFactory: DataSource.Factory,
    private val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory(),
    private val backendEventSink: BackendEventSink = LogcatBackendEventSink,
) : AudiobookChapterReader {
    private val cache = object : LinkedHashMap<ChapterCacheKey, List<AudiobookChapter>>(CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ChapterCacheKey, List<AudiobookChapter>>): Boolean =
            size > CACHE_SIZE
    }

    override fun onMemoryPressure(pressure: MemoryPressure) {
        if (pressure == MemoryPressure.Normal) return
        synchronized(cache) { cache.clear() }
    }

    override suspend fun chapters(song: Song): List<AudiobookChapter> = withContext(Dispatchers.IO) {
        val cacheKey = ChapterCacheKey(song.uri.toString(), song.durationMs, song.dateModifiedSeconds)
        synchronized(cache) { cache[cacheKey]?.let {
            recordDiagnostics(result = "cache_hit", chapterCount = it.size)
            return@withContext it
        } }
        var dataSource: DataSource? = null
        var extractor: Extractor? = null
        var cacheResult = false
        var sniffFailed = false
        var result = emptyList<AudiobookChapter>()
        try {
            dataSource = dataSourceFactory.createDataSource()
            val totalLength = dataSource.open(DataSpec(song.uri))
            var input = DefaultExtractorInput(dataSource, 0L, totalLength)
            val selectedExtractor = extractorsFactory
                .createExtractors(song.uri, dataSource.responseHeaders)
                .firstOrNull { candidate ->
                    input.resetPeekPosition()
                    try {
                        candidate.sniff(input)
                    } catch (_: IOException) {
                        sniffFailed = true
                        false
                    }
                }
                ?: run {
                    cacheResult = !sniffFailed
                    recordDiagnostics(result = "no_chapters", chapterCount = 0)
                    return@withContext emptyList()
                }
            extractor = selectedExtractor
            input.resetPeekPosition()
            val output = ChapterExtractorOutput()
            selectedExtractor.init(output)
            val positionHolder = PositionHolder()
            var probeIterations = 0
            while (!output.audioFormatCaptured && probeIterations++ < MAX_PROBE_ITERATIONS) {
                coroutineContext.ensureActive()
                when (selectedExtractor.read(input, positionHolder)) {
                    Extractor.RESULT_CONTINUE -> Unit
                    Extractor.RESULT_END_OF_INPUT -> break
                    Extractor.RESULT_SEEK -> {
                        val position = positionHolder.position.coerceAtLeast(0L)
                        closeDataSource(dataSource)
                        dataSource = dataSourceFactory.createDataSource()
                        dataSource.open(DataSpec(song.uri, position, C.LENGTH_UNSET.toLong()))
                        input = DefaultExtractorInput(dataSource, position, totalLength)
                        selectedExtractor.seek(position, 0L)
                    }
                    else -> break
                }
            }
            if (!output.audioFormatCaptured && probeIterations >= MAX_PROBE_ITERATIONS) {
                recordDiagnostics(result = "budget_exhausted", chapterCount = 0, errorType = "ProbeBudget")
                return@withContext emptyList()
            }
            result = output.chapters(song.durationMs)
            cacheResult = true
            recordDiagnostics(result = if (result.isEmpty()) "no_chapters" else "chapters_found", chapterCount = result.size)
            result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            recordDiagnostics(result = "io_failure", chapterCount = 0, errorType = "IOException")
            emptyList()
        } catch (_: RuntimeException) {
            recordDiagnostics(result = "runtime_failure", chapterCount = 0, errorType = "RuntimeException")
            emptyList()
        } finally {
            releaseExtractor(extractor)
            closeDataSource(dataSource)
            if (cacheResult) {
                synchronized(cache) { cache[cacheKey] = result }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun releaseExtractor(extractor: Extractor?) {
        try {
            extractor?.release()
        } catch (failure: RuntimeException) {
            recordDiagnostics(result = "cleanup_failure", chapterCount = 0, errorType = failure::class.simpleName)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun closeDataSource(dataSource: DataSource?) {
        try {
            dataSource?.close()
        } catch (failure: IOException) {
            recordDiagnostics(result = "cleanup_failure", chapterCount = 0, errorType = failure::class.simpleName)
        } catch (failure: RuntimeException) {
            recordDiagnostics(result = "cleanup_failure", chapterCount = 0, errorType = failure::class.simpleName)
        }
    }

    private fun recordDiagnostics(result: String, chapterCount: Int, errorType: String? = null) {
        backendEventSink.emitLazy {
            BackendEvent.AudiobookChapterRead(
                fields = mapOf(
                    "result" to result,
                    "chapter_count" to chapterCount.toString(),
                    "duration_known" to "true",
                ) + errorType?.let { mapOf("error_type" to it) }.orEmpty(),
            )
        }
    }

    private companion object {
        data class ChapterCacheKey(
            val uri: String,
            val durationMs: Long,
            val dateModifiedSeconds: Long?,
        )

        const val CACHE_SIZE = 64
        const val MAX_PROBE_ITERATIONS = 20_000
    }
}

@UnstableApi
internal class ChapterExtractorOutput : ExtractorOutput {
    private val trackOutputs = mutableListOf<ChapterTrackOutput>()

    val audioFormatCaptured: Boolean
        get() = trackOutputs.any { it.isAudio && it.format != null }

    override fun track(id: Int, type: Int): TrackOutput {
        return ChapterTrackOutput().also(trackOutputs::add)
    }

    override fun endTracks() = Unit

    override fun seekMap(seekMap: SeekMap) = Unit

    fun chapters(durationMs: Long): List<AudiobookChapter> {
        return trackOutputs
            .asSequence()
            .filter { it.isAudio }
            .flatMap { it.format?.metadata?.getEntriesOfType(Chapter::class.java).orEmpty().asSequence() }
            .filterNot(Chapter::isHidden)
            .mapIndexedNotNull { index, chapter ->
                val startMs = chapter.startTimeMs.takeUnless { it == C.TIME_UNSET }?.coerceAtLeast(0L)
                    ?: return@mapIndexedNotNull null
                val endMs = chapter.endTimeMs.takeUnless { it == C.TIME_UNSET }
                    ?.coerceAtLeast(startMs)
                    ?: durationMs.coerceAtLeast(startMs)
                if (endMs <= startMs) return@mapIndexedNotNull null
                AudiobookChapter(
                    title = chapter.title?.value?.trim().orEmpty().ifBlank { "Chapter ${index + 1}" },
                    startMs = startMs,
                    endMs = endMs,
                )
            }
            .distinctBy { it.startMs to it.endMs }
            .toList()
    }
}

@UnstableApi
internal class ChapterTrackOutput : TrackOutput {
    private val scratch = ByteArray(16 * 1024)
    var format: Format? = null
        private set

    val isAudio: Boolean
        get() = format?.sampleMimeType?.startsWith("audio/") == true

    override fun format(format: Format) {
        this.format = format
    }

    override fun sampleData(
        input: DataReader,
        length: Int,
        allowEndOfInput: Boolean,
        sampleDataPart: Int,
    ): Int {
        var remaining = length
        while (remaining > 0) {
            val read = input.read(scratch, 0, min(remaining, scratch.size))
            if (read == C.RESULT_END_OF_INPUT) {
                if (!allowEndOfInput) throw EOFException()
                break
            }
            if (read <= 0) break
            remaining -= read
        }
        return length - remaining
    }

    override fun sampleData(data: ParsableByteArray, length: Int, sampleDataPart: Int) {
        data.skipBytes(length)
    }

    override fun sampleMetadata(
        timeUs: Long,
        flags: Int,
        size: Int,
        offset: Int,
        cryptoData: TrackOutput.CryptoData?,
    ) = Unit
}
