package elovaire.music.droidbeauty.app.data.audio

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackFailureClassifierTest {
    @Test
    fun classifyUnsupportedFormat() {
        val result = PlaybackFailureClassifier.classify(
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
        )

        assertEquals(PlaybackFailureCategory.UnsupportedFormat, result.category)
        assertEquals("Unsupported audio format", result.userSafeReason)
    }

    @Test
    fun classifyMalformedMedia() {
        val result = PlaybackFailureClassifier.classify(
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        )

        assertEquals(PlaybackFailureCategory.CorruptOrMalformedMedia, result.category)
    }

    @Test
    fun classifyMissingSource() {
        val result = PlaybackFailureClassifier.classify(
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        )

        assertEquals(PlaybackFailureCategory.SourceUnavailable, result.category)
    }

}
