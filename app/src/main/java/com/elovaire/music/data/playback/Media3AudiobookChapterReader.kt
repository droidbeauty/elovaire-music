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
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput
import androidx.media3.extractor.metadata.Chapter
import elovaire.music.droidbeauty.app.domain.model.AudiobookChapter
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import java.io.EOFException
import java.io.IOException
import kotlin.math.min

internal interface AudiobookChapterReader {
    suspend fun chapters(song: Song): List<AudiobookChapter>
}

@UnstableApi
internal class Media3AudiobookChapterReader(
    private val dataSourceFactory: DataSource.Factory,
    private val extractorsFactory: DefaultExtractorsFactory = DefaultExtractorsFactory(),
) : AudiobookChapterReader {
    override suspend fun chapters(song: Song): List<AudiobookChapter> = withContext(Dispatchers.IO) {
        var dataSource = dataSourceFactory.createDataSource()
        var extractor: Extractor? = null
        try {
            val totalLength = dataSource.open(DataSpec(song.uri))
            var input = DefaultExtractorInput(dataSource, 0L, totalLength)
            val selectedExtractor = extractorsFactory
                .createExtractors(song.uri, dataSource.responseHeaders)
                .firstOrNull { candidate ->
                    input.resetPeekPosition()
                    try {
                        candidate.sniff(input)
                    } catch (_: IOException) {
                        false
                    }
                }
                ?: return@withContext emptyList()
            extractor = selectedExtractor
            input.resetPeekPosition()
            val output = ChapterExtractorOutput()
            selectedExtractor.init(output)
            val positionHolder = PositionHolder()
            while (!output.audioFormatCaptured) {
                coroutineContext.ensureActive()
                when (selectedExtractor.read(input, positionHolder)) {
                    Extractor.RESULT_CONTINUE -> Unit
                    Extractor.RESULT_END_OF_INPUT -> break
                    Extractor.RESULT_SEEK -> {
                        val position = positionHolder.position.coerceAtLeast(0L)
                        dataSource.close()
                        dataSource = dataSourceFactory.createDataSource()
                        dataSource.open(DataSpec(song.uri, position, C.LENGTH_UNSET.toLong()))
                        input = DefaultExtractorInput(dataSource, position, totalLength)
                        selectedExtractor.seek(position, 0L)
                    }
                    else -> break
                }
            }
            output.chapters(song.durationMs)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            emptyList()
        } finally {
            extractor?.release()
            dataSource.close()
        }
    }
}

@UnstableApi
private class ChapterExtractorOutput : ExtractorOutput {
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
private class ChapterTrackOutput : TrackOutput {
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
        val buffer = ByteArray(min(length, 16 * 1024).coerceAtLeast(1))
        while (remaining > 0) {
            val read = input.read(buffer, 0, min(remaining, buffer.size))
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
