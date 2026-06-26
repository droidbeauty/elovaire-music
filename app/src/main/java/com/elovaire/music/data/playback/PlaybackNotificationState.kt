package elovaire.music.droidbeauty.app.data.playback

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class PlaybackNotificationRenderState(
    val songId: Long?,
    val title: String,
    val artist: String,
    val album: String,
    val artUri: Uri?,
    val isPlaying: Boolean,
) {
    val hasSong: Boolean get() = songId != null
}

internal fun notificationRenderStateOf(
    song: Song?,
    isPlaying: Boolean,
): PlaybackNotificationRenderState {
    return PlaybackNotificationRenderState(
        songId = song?.id,
        title = song?.title.orEmpty(),
        artist = song?.artist.orEmpty(),
        album = song?.album.orEmpty(),
        artUri = song?.artUri,
        isPlaying = isPlaying,
    )
}
