package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi

@UnstableApi
internal data class PlaybackOffloadPolicy(
    val enabled: Boolean,
    val gaplessRequired: Boolean,
) {
    fun toAudioOffloadPreferences(): TrackSelectionParameters.AudioOffloadPreferences {
        return TrackSelectionParameters.AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(
                if (enabled) {
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
                } else {
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
                },
            )
            .setIsGaplessSupportRequired(gaplessRequired)
            .setIsSpeedChangeSupportRequired(false)
            .build()
    }

    companion object {
        val Disabled = PlaybackOffloadPolicy(enabled = false, gaplessRequired = false)

        fun from(
            signalProcessingEnabled: Boolean,
            signalAlteringEffectsActive: Boolean,
            gaplessPlaybackEnabled: Boolean,
        ): PlaybackOffloadPolicy {
            val enabled = signalProcessingEnabled && !signalAlteringEffectsActive
            return PlaybackOffloadPolicy(
                enabled = enabled,
                gaplessRequired = enabled && gaplessPlaybackEnabled,
            )
        }
    }
}
