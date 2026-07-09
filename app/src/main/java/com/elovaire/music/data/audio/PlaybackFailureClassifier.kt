package elovaire.music.droidbeauty.app.data.audio

import androidx.media3.common.PlaybackException

internal enum class PlaybackFailureCategory {
    UnsupportedFormat,
    CorruptOrMalformedMedia,
    DecoderUnavailable,
    SourceUnavailable,
    Unknown,
}

internal data class PlaybackFailureClassification(
    val category: PlaybackFailureCategory,
    val userSafeReason: String,
)

internal object PlaybackFailureClassifier {
    fun classify(error: PlaybackException): PlaybackFailureClassification {
        return classify(error.errorCode)
    }

    fun classify(errorCode: Int): PlaybackFailureClassification {
        return when (errorCode) {
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
            -> PlaybackFailureClassification(
                category = PlaybackFailureCategory.UnsupportedFormat,
                userSafeReason = "Unsupported audio format",
            )

            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
            PlaybackException.ERROR_CODE_DECODING_FAILED,
            -> PlaybackFailureClassification(
                category = PlaybackFailureCategory.CorruptOrMalformedMedia,
                userSafeReason = "Corrupt or malformed media",
            )

            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            -> PlaybackFailureClassification(
                category = PlaybackFailureCategory.DecoderUnavailable,
                userSafeReason = "No compatible decoder available",
            )

            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
            PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
            -> PlaybackFailureClassification(
                category = PlaybackFailureCategory.SourceUnavailable,
                userSafeReason = "Media source unavailable",
            )

            else -> PlaybackFailureClassification(
                category = PlaybackFailureCategory.Unknown,
                userSafeReason = "Playback failed",
            )
        }
    }
}
