package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.TrackSelectionParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackOffloadPolicyTest {
    @Test
    fun offloadIsEnabledOnlyForSignalProcessingPlayerWithoutActiveEffects() {
        val policy = PlaybackOffloadPolicy.from(
            signalProcessingEnabled = true,
            signalAlteringEffectsActive = false,
            crossfadeEnabled = false,
        )

        assertTrue(policy.enabled)
        assertFalse(policy.toAudioOffloadPreferences().isGaplessSupportRequired)
        assertEquals(
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED,
            policy.toAudioOffloadPreferences().audioOffloadMode,
        )
    }

    @Test
    fun crossfadeDisablesOffloadBecauseItRequiresTwoSoftwarePlayers() {
        val policy = PlaybackOffloadPolicy.from(
            signalProcessingEnabled = true,
            signalAlteringEffectsActive = false,
            crossfadeEnabled = true,
        )

        assertFalse(policy.enabled)
        assertFalse(policy.toAudioOffloadPreferences().isGaplessSupportRequired)
    }

    @Test
    fun directNoDspPlayerDisablesMedia3Offload() {
        val policy = PlaybackOffloadPolicy.from(
            signalProcessingEnabled = false,
            signalAlteringEffectsActive = false,
            crossfadeEnabled = true,
        )

        assertFalse(policy.enabled)
        assertEquals(
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED,
            policy.toAudioOffloadPreferences().audioOffloadMode,
        )
    }

    @Test
    fun signalAlteringEffectsDisableMedia3Offload() {
        val policy = PlaybackOffloadPolicy.from(
            signalProcessingEnabled = true,
            signalAlteringEffectsActive = true,
            crossfadeEnabled = true,
        )

        assertFalse(policy.enabled)
    }
}
