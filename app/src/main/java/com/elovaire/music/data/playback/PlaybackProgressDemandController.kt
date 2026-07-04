package elovaire.music.droidbeauty.app.data.playback

internal enum class PlaybackProgressConsumer {
    NowPlaying,
    CompactDock,
    SyncedLyrics,
    Scrubbing,
}

internal class PlaybackProgressDemandController {
    private val activeConsumers = linkedSetOf<PlaybackProgressConsumer>()

    fun setActive(
        consumer: PlaybackProgressConsumer,
        active: Boolean,
    ): Boolean {
        return if (active) {
            activeConsumers.add(consumer)
        } else {
            activeConsumers.remove(consumer)
        }
    }

    fun hasAnyDemand(): Boolean = activeConsumers.isNotEmpty()

    fun pollingIntervalMs(): Long {
        return when {
            PlaybackProgressConsumer.Scrubbing in activeConsumers -> 120L
            PlaybackProgressConsumer.NowPlaying in activeConsumers -> 250L
            PlaybackProgressConsumer.SyncedLyrics in activeConsumers -> 250L
            PlaybackProgressConsumer.CompactDock in activeConsumers -> 500L
            else -> 1_000L
        }
    }

    fun clear() {
        activeConsumers.clear()
    }
}
