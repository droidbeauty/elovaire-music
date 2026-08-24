package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaButtonReceiver
import elovaire.music.droidbeauty.app.core.getParcelableExtraCompat
import elovaire.music.droidbeauty.app.core.hasAudioReadPermission
import elovaire.music.droidbeauty.app.data.library.LibrarySnapshotStore
import elovaire.music.droidbeauty.app.data.playback.library.ResolvedPlayableQueue
import elovaire.music.droidbeauty.app.domain.model.Song
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class MediaButtonPlaybackResumption(
    val queue: ResolvedPlayableQueue,
    val persisted: PersistedPlaybackSession,
)

private val pendingMediaButtonResumption = AtomicReference<MediaButtonPlaybackResumption?>()
private val mediaButtonResumptionInFlight = AtomicBoolean()

internal fun pendingMediaButtonResumption(consume: Boolean): MediaButtonPlaybackResumption? {
    return if (consume) pendingMediaButtonResumption.getAndSet(null) else pendingMediaButtonResumption.get()
}

internal fun storePendingMediaButtonResumption(resumption: MediaButtonPlaybackResumption?) {
    pendingMediaButtonResumption.set(resumption)
}

internal fun resolveMediaButtonResumption(
    persisted: PersistedPlaybackSession?,
    songs: List<Song>,
): MediaButtonPlaybackResumption? {
    persisted ?: return null
    if (songs.isEmpty()) return null
    val songsById = songs.associateBy(Song::id)
    val queue = persisted.queueSongIds.map { songsById[it] ?: return null }
    val index = persisted.currentIndex
        .takeIf { it in queue.indices && queue[it].id == persisted.currentSongId }
        ?: persisted.currentSongId
            ?.let { currentId -> queue.indexOfFirst { it.id == currentId } }
            ?.takeIf { it >= 0 }
        ?: return null
    val currentSong = queue[index]
    return MediaButtonPlaybackResumption(
        queue = ResolvedPlayableQueue(
            startSong = currentSong,
            queue = queue,
            sourceLabel = currentSong.album,
            sourcePlaylistId = persisted.sourcePlaylistId,
        ),
        persisted = persisted,
    )
}

@OptIn(UnstableApi::class)
class ElovaireMediaButtonReceiver : MediaButtonReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!intent.isPlaybackStartRequest()) {
            super.onReceive(context, intent)
            return
        }
        if (!mediaButtonResumptionInFlight.compareAndSet(false, true)) return
        val pendingResult = goAsync()
        val applicationContext = context.applicationContext
        val serviceIntent = Intent(intent)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resumption = if (applicationContext.hasAudioReadPermission()) {
                    resolveMediaButtonResumption(
                        persisted = PlaybackSessionStore(applicationContext).load(),
                        songs = LibrarySnapshotStore(applicationContext).load()?.snapshot?.songs.orEmpty(),
                    )
                } else {
                    null
                }
                storePendingMediaButtonResumption(resumption)
                if (resumption != null) {
                    withContext(Dispatchers.Main.immediate) {
                        handleIntentAndMaybeStartTheService(applicationContext, serviceIntent)
                    }
                }
            } finally {
                mediaButtonResumptionInFlight.set(false)
                pendingResult.finish()
            }
        }
    }
}

private fun Intent?.isPlaybackStartRequest(): Boolean {
    if (this?.action != Intent.ACTION_MEDIA_BUTTON) return false
    val keyEvent = getParcelableExtraCompat<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
    if (keyEvent.action != KeyEvent.ACTION_DOWN || keyEvent.repeatCount != 0) return false
    return keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
        keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
        keyEvent.keyCode == KeyEvent.KEYCODE_HEADSETHOOK
}
