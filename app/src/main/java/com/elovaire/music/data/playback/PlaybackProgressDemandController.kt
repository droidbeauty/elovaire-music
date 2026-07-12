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
        return ProgressPollingPolicy.decide(
            ProgressPollingPolicyInput(
                isScrubbing = PlaybackProgressConsumer.Scrubbing in activeConsumers,
                nowPlayingVisible = PlaybackProgressConsumer.NowPlaying in activeConsumers,
                compactDockVisible = PlaybackProgressConsumer.CompactDock in activeConsumers,
                syncedLyricsVisible = PlaybackProgressConsumer.SyncedLyrics in activeConsumers,
            ),
        ).intervalMs
    }

    fun clear() {
        activeConsumers.clear()
    }
}

internal data class ProgressPollingPolicyInput(
    val isScrubbing: Boolean,
    val nowPlayingVisible: Boolean,
    val compactDockVisible: Boolean,
    val syncedLyricsVisible: Boolean,
)

internal data class ProgressPollingDecision(
    val intervalMs: Long,
    val reason: String,
)

internal object ProgressPollingPolicy {
    fun decide(input: ProgressPollingPolicyInput): ProgressPollingDecision {
        return when {
            input.isScrubbing -> ProgressPollingDecision(100L, "scrubbing")
            input.syncedLyricsVisible -> ProgressPollingDecision(200L, "synced_lyrics")
            input.nowPlayingVisible -> ProgressPollingDecision(250L, "now_playing")
            input.compactDockVisible -> ProgressPollingDecision(1_000L, "compact_dock")
            else -> ProgressPollingDecision(1_000L, "idle")
        }
    }
}
