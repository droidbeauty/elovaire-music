package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri

internal sealed interface PlaybackFailure {
    data class UnsupportedFormat(val fileName: String) : PlaybackFailure
    data class SourceUnavailable(val uri: Uri) : PlaybackFailure
    data class PlayerInitializationFailed(val cause: Throwable?) : PlaybackFailure
    data object AudioFocusDenied : PlaybackFailure
    data class RecoveryExhausted(val songId: Long?) : PlaybackFailure
}
