package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Label
import androidx.media3.common.Metadata
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.metadata.Chapter
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.backend.RecordingBackendEventSink
import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class Media3AudiobookChapterReaderTest {
    @Test
    fun chapterMetadataIsFilteredAndNormalized() {
        val output = ChapterExtractorOutput()
        val track = output.track(0, C.TRACK_TYPE_AUDIO) as ChapterTrackOutput
        track.format(
            Format.Builder()
                .setSampleMimeType("audio/mp4")
                .setMetadata(
                    Metadata(
                        Chapter.Builder()
                            .setStartTimeMs(0L)
                            .setEndTimeMs(100L)
                            .setTitle(Label(null, "Intro"))
                            .build(),
                        Chapter.Builder()
                            .setStartTimeMs(100L)
                            .setEndTimeMs(C.TIME_UNSET)
                            .setHidden(true)
                            .build(),
                    ),
                )
                .build(),
        )

        val chapters = output.chapters(500L)

        assertEquals(1, chapters.size)
        assertEquals("Intro", chapters.single().title)
        assertEquals(100L, chapters.single().endMs)
    }

    @Test
    fun openFailureFallsBackAndRecordsDiagnostics() = runBlocking {
        val sink = RecordingBackendEventSink()
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { FailingDataSource(openFailure = IOException()) },
            extractorsFactory = ExtractorsFactory.EMPTY,
            backendEventSink = sink,
        )

        assertTrue(reader.chapters(song()).isEmpty())
        assertEquals("io_failure", sink.snapshot().last().fields["result"])
    }

    @Test
    fun cleanupFailuresDoNotReplaceSuccessfulFallback() = runBlocking {
        val source = FailingDataSource(closeFailure = IOException())
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { source },
            extractorsFactory = ExtractorsFactory.EMPTY,
            backendEventSink = RecordingBackendEventSink(),
        )

        assertTrue(reader.chapters(song()).isEmpty())
        assertTrue(source.closeCount > 0)
    }

    @Test
    fun runtimeParserAndReleaseFailuresAreContained() = runBlocking {
        val source = FailingDataSource()
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { source },
            extractorsFactory = ExtractorsFactory { arrayOf(FailingExtractor(readFailure = IllegalStateException(), releaseFailure = IllegalStateException())) },
            backendEventSink = RecordingBackendEventSink(),
        )

        assertTrue(reader.chapters(song()).isEmpty())
        assertTrue(source.closeCount > 0)
    }

    @Test
    fun seekRequestsReopenTheSourceAndStillReturnSafeFallback() = runBlocking {
        val source = FailingDataSource()
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { source },
            extractorsFactory = ExtractorsFactory { arrayOf(FailingExtractor(seekOnce = true)) },
            backendEventSink = RecordingBackendEventSink(),
        )

        assertTrue(reader.chapters(song()).isEmpty())
        assertTrue(source.openCount >= 2)
        assertTrue(source.closeCount >= 2)
    }

    @Test
    fun cancellationIsNotConvertedToFallback() = runBlocking {
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { FailingDataSource() },
            extractorsFactory = ExtractorsFactory { arrayOf(FailingExtractor(sniffFailure = CancellationException())) },
            backendEventSink = RecordingBackendEventSink(),
        )

        var cancelled = false
        try {
            reader.chapters(song())
        } catch (_: CancellationException) {
            cancelled = true
        }
        assertTrue(cancelled)
    }

    @Test
    fun semanticNoChaptersAreCachedButReadFailuresAreNotRequiredToBeCached() = runBlocking {
        var factoryCalls = 0
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { factoryCalls++; FailingDataSource() },
            extractorsFactory = ExtractorsFactory.EMPTY,
            backendEventSink = RecordingBackendEventSink(),
        )

        reader.chapters(song())
        reader.chapters(song())

        assertEquals(1, factoryCalls)
    }

    @Test
    fun memoryPressureClearsChapterCache() = runBlocking {
        var factoryCalls = 0
        val reader = Media3AudiobookChapterReader(
            dataSourceFactory = factory { factoryCalls++; FailingDataSource() },
            extractorsFactory = ExtractorsFactory.EMPTY,
            backendEventSink = RecordingBackendEventSink(),
        )

        reader.chapters(song())
        reader.onMemoryPressure(MemoryPressure.Critical)
        reader.chapters(song())

        assertEquals(2, factoryCalls)
    }

    private fun song() = Song(
        id = 1L,
        title = "Part",
        isExplicit = false,
        artist = "Author",
        album = "Book",
        releaseYear = null,
        genre = "",
        audioFormat = "M4B",
        audioQuality = null,
        fileName = "book.m4b",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        uri = TestUri("file:///book.m4b"),
        artUri = null,
        mediaKind = AudioMediaKind.Audiobook,
    )

    private fun factory(create: () -> DataSource): DataSource.Factory = DataSource.Factory { create() }

    private class FailingDataSource(
        private val openFailure: IOException? = null,
        private val closeFailure: IOException? = null,
    ) : DataSource {
        var openCount = 0
        var closeCount = 0

        override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) = Unit

        override fun open(dataSpec: DataSpec): Long {
            openCount++
            openFailure?.let { throw it }
            return 1L
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = C.RESULT_END_OF_INPUT

        override fun getUri() = TestUri("file:///book.m4b")

        override fun close() {
            closeCount++
            closeFailure?.let { throw it }
        }
    }

    private class FailingExtractor(
        private val sniffFailure: RuntimeException? = null,
        private val readFailure: RuntimeException? = null,
        private val releaseFailure: RuntimeException? = null,
        private val seekOnce: Boolean = false,
    ) : Extractor {
        private var hasRequestedSeek = false

        override fun sniff(input: ExtractorInput): Boolean {
            sniffFailure?.let { throw it }
            return true
        }

        override fun init(output: ExtractorOutput) = Unit

        override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int {
            readFailure?.let { throw it }
            if (seekOnce && !hasRequestedSeek) {
                hasRequestedSeek = true
                seekPosition.position = 0L
                return Extractor.RESULT_SEEK
            }
            return Extractor.RESULT_END_OF_INPUT
        }

        override fun seek(position: Long, timeUs: Long) = Unit

        override fun release() {
            releaseFailure?.let { throw it }
        }
    }
}
