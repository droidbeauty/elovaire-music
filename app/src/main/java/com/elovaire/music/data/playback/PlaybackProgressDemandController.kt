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

    fun clear() {
        activeConsumers.clear()
    }
}
