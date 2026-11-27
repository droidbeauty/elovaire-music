package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioFormat
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectPlaybackPolicyTest {
    @Test
    fun availableUsbDeviceWithoutAnAttributesRouteCannotAuthorizeDirectPlayback() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = DirectPlaybackRouteContext(
                hasEligibleUsbRoute = true,
                hasUsbRouteForAttributes = false,
                hasBluetoothRouteForAttributes = false,
            ),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
            supportClassification = DirectPlaybackSupportClassifier.classify(
                AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED,
            ),
        )
        assertFalse(status.shouldUseDirectPlayback)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
    }

    @Test
    fun incompleteGaplessFlagDoesNotClaimOffloadSupport() {
        val gaplessOnlyBit = AudioManager.DIRECT_PLAYBACK_OFFLOAD_GAPLESS_SUPPORTED xor
            AudioManager.DIRECT_PLAYBACK_OFFLOAD_SUPPORTED
        assertFalse(DirectPlaybackSupportClassifier.classify(gaplessOnlyBit).isSupported)
    }

    @Test
    fun classifierRecognizesCombinedDirectPlaybackFlags() {
        val classification = DirectPlaybackSupportClassifier.classify(
            AudioManager.DIRECT_PLAYBACK_OFFLOAD_GAPLESS_SUPPORTED or
                AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED,
        )

        assertTrue(classification.supportsOffload)
        assertTrue(classification.supportsDirectPlayback)
        assertEquals("offload+direct", classification.summary)
    }

    @Test
    fun android12UsbRouteRemainsLegacyUnverified() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.S,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
        )

        assertEquals(BitPerfectPlaybackState.LegacyUnverifiedUsbRoute, status.state)
        assertEquals(DirectPlaybackMode.LegacyUnverifiedUsbRoute, status.mode)
        assertFalse(status.shouldUseDirectPlayback)
    }

    @Test
    fun bluetoothRouteRequiresRegularPlayback() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = DirectPlaybackRouteContext(
                hasEligibleUsbRoute = false,
                hasUsbRouteForAttributes = false,
                hasBluetoothRouteForAttributes = true,
                attributesRouteDeviceId = 8,
                attributesRouteType = 8,
            ),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
        )

        assertEquals(BitPerfectPlaybackState.BluetoothRoute, status.state)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
    }

    @Test
    fun effectsBlockDirectPlayback() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = true,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
        )

        assertEquals(BitPerfectPlaybackState.EffectsActive, status.state)
        assertFalse(status.shouldUseDirectPlayback)
    }

    @Test
    fun unknownTrackFormatKeepsCurrentPath() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = null,
            evaluationKey = null,
        )

        assertEquals(BitPerfectPlaybackState.FormatUnknown, status.state)
        assertEquals(BitPerfectPlaybackDirective.KeepCurrent, status.directive)
    }

    @Test
    fun unsupportedTrackFormatRequiresRegularPlayback() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(encoding = C.ENCODING_AC3),
            evaluationKey = null,
        )

        assertEquals(BitPerfectPlaybackState.FormatUnsupported, status.state)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
    }

    @Test
    fun unsupportedDirectPlaybackFallsBackToRegularPath() {
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
            supportClassification = DirectPlaybackSupportClassification.notSupported,
            directPlaybackSupport = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
        )

        assertEquals(BitPerfectPlaybackState.UsbRouteDetectedButDirectSupportUnavailable, status.state)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
    }

    @Test
    fun offloadOnlySupportIsPowerSavingOnlyAndDoesNotForceDirectPlayback() {
        val classification = DirectPlaybackSupportClassification(
            rawFlags = AudioManager.DIRECT_PLAYBACK_OFFLOAD_SUPPORTED,
            kinds = setOf(DirectPlaybackSupportKind.Offload),
        )
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
            supportClassification = classification,
            directPlaybackSupport = classification.rawFlags,
        )

        assertEquals(BitPerfectPlaybackState.OffloadOnlyDirectPlaybackSupported, status.state)
        assertEquals(DirectPlaybackMode.RegularPlaybackRequired, status.mode)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
        assertFalse(status.shouldUseDirectPlayback)
        assertEquals(BitPerfectConfidence.DirectCapable, status.confidence)
    }

    @Test
    fun seamlessOffloadCapabilityDoesNotForceDirectPlayback() {
        val classification = DirectPlaybackSupportClassification(
            rawFlags = AudioManager.DIRECT_PLAYBACK_OFFLOAD_GAPLESS_SUPPORTED,
            kinds = setOf(DirectPlaybackSupportKind.Offload),
        )
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
            supportClassification = classification,
            directPlaybackSupport = classification.rawFlags,
        )

        assertEquals(BitPerfectPlaybackState.OffloadOnlyDirectPlaybackSupported, status.state)
        assertEquals(BitPerfectPlaybackDirective.PreferRegular, status.directive)
        assertFalse(status.shouldUseDirectPlayback)
    }

    @Test
    fun directSupportCanUseNoDspDirectPath() {
        val classification = DirectPlaybackSupportClassification(
            rawFlags = AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED,
            kinds = setOf(DirectPlaybackSupportKind.DirectPlayback),
        )
        val status = BitPerfectEligibilityPolicy.evaluate(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            routeContext = usbRouteContext(),
            effectsActive = false,
            trackConfig = trackConfig(),
            evaluationKey = evaluationKey(),
            supportClassification = classification,
            directPlaybackSupport = classification.rawFlags,
        )

        assertEquals(BitPerfectPlaybackState.DirectPlaybackSupported, status.state)
        assertEquals(DirectPlaybackMode.DirectUsbPlaybackEligible, status.mode)
        assertEquals(BitPerfectPlaybackDirective.PreferDirect, status.directive)
        assertTrue(status.shouldUseDirectPlayback)
        assertEquals(BitPerfectConfidence.ExternallyUnverified, status.confidence)
        assertTrue(status.capability.attributeRouteKnown)
        assertTrue(status.configuration.signalProcessingBypassRequested)
    }

    private fun usbRouteContext(): DirectPlaybackRouteContext {
        return DirectPlaybackRouteContext(
            hasEligibleUsbRoute = true,
            hasUsbRouteForAttributes = true,
            hasBluetoothRouteForAttributes = false,
            attributesRouteDeviceId = 17,
            attributesRouteType = 11,
            attributesRouteSignature = 1234,
        )
    }

    private fun trackConfig(encoding: Int = C.ENCODING_PCM_24BIT): DirectPlaybackTrackConfig {
        return DirectPlaybackTrackConfig(
            encoding = encoding,
            sampleRate = 96_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            channelCount = 2,
            tunneling = false,
            offload = false,
        )
    }

    private fun evaluationKey(): DirectPlaybackEvaluationKey {
        return DirectPlaybackTrackConfig(
            encoding = C.ENCODING_PCM_24BIT,
            sampleRate = 96_000,
            channelMask = AudioFormat.CHANNEL_OUT_STEREO,
            channelCount = 2,
            tunneling = false,
            offload = false,
        ).toEvaluationKey(
            routeFingerprintHash = 99,
            routeDeviceId = 17,
            routeType = 11,
            effectsActive = false,
        )!!
    }
}
