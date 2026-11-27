@file:Suppress("InlinedApi")

package elovaire.music.droidbeauty.app.data.playback

import android.media.AudioManager
import android.os.Build
import elovaire.music.droidbeauty.app.core.AndroidCapabilities

internal enum class DirectPlaybackMode {
    RegularPlaybackRequired,
    LegacyUnverifiedUsbRoute,
    DirectUsbPlaybackEligible,
}

internal enum class DirectPlaybackSupportKind {
    Offload,
    DirectPlayback,
}

internal enum class BitPerfectConfidence {
    Unsupported,
    NotEligible,
    DirectCapable,
    DirectConfigured,
    RouteConfirmed,
    ExternallyUnverified,
}

internal data class DirectPlaybackCapability(
    val attributeRouteKnown: Boolean,
    val routeEligible: Boolean,
    val formatSupported: Boolean,
    val directSupportFlags: Int,
    val supportKinds: Set<DirectPlaybackSupportKind>,
)

internal data class DirectPlaybackConfiguration(
    val directModeRequested: Boolean,
    val usbRouteReportedForAttributes: Boolean,
    val signalProcessingBypassRequested: Boolean,
    val currentTrackConfigKnown: Boolean,
)

internal data class SignalProcessingState(
    val equalizerActive: Boolean = false,
    val reverbActive: Boolean = false,
    val volumeNormalizationActive: Boolean = false,
    val softwareGainActive: Boolean = false,
    val channelMappingActive: Boolean = false,
    val customAudioProcessorActive: Boolean = false,
) {
    val active: Boolean
        get() = equalizerActive || reverbActive || volumeNormalizationActive || softwareGainActive ||
            channelMappingActive || customAudioProcessorActive

    companion object {
        fun fromAggregate(active: Boolean): SignalProcessingState =
            SignalProcessingState(customAudioProcessorActive = active)
    }
}

internal data class DirectPlaybackSupportClassification(
    val rawFlags: Int,
    val kinds: Set<DirectPlaybackSupportKind>,
) {
    val supportsOffload: Boolean
        get() = DirectPlaybackSupportKind.Offload in kinds

    val supportsDirectPlayback: Boolean
        get() = DirectPlaybackSupportKind.DirectPlayback in kinds

    val isSupported: Boolean
        get() = kinds.isNotEmpty()

    val summary: String
        get() = when {
            !isSupported -> "none"
            supportsDirectPlayback && supportsOffload -> "offload+direct"
            supportsDirectPlayback -> "direct"
            supportsOffload -> "offload"
            else -> "unknown"
        }

    companion object {
        val notSupported = DirectPlaybackSupportClassification(
            rawFlags = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
            kinds = emptySet(),
        )
    }
}

internal object DirectPlaybackSupportClassifier {
    fun classify(rawFlags: Int): DirectPlaybackSupportClassification {
        if (rawFlags == AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED) {
            return DirectPlaybackSupportClassification.notSupported
        }
        val kinds = linkedSetOf<DirectPlaybackSupportKind>()
        if ((rawFlags and AudioManager.DIRECT_PLAYBACK_OFFLOAD_SUPPORTED) != 0) {
            kinds += DirectPlaybackSupportKind.Offload
        }
        if ((rawFlags and AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED) != 0) {
            kinds += DirectPlaybackSupportKind.DirectPlayback
        }
        return DirectPlaybackSupportClassification(
            rawFlags = rawFlags,
            kinds = kinds,
        )
    }
}

internal data class DirectPlaybackRouteContext(
    val hasEligibleUsbRoute: Boolean,
    val hasUsbRouteForAttributes: Boolean,
    val hasBluetoothRouteForAttributes: Boolean,
    val attributesRouteDeviceId: Int? = null,
    val attributesRouteType: Int? = null,
    val attributesRouteSignature: Int? = null,
    val attributeRouteKnown: Boolean = hasUsbRouteForAttributes || hasBluetoothRouteForAttributes,
)

internal object BitPerfectEligibilityPolicy {
    /*
     * Elovaire's "direct" path is a framework-mediated path, not a custom USB transport.
     *
     * Policy:
     * - On Android 11/12, a USB route can be detected and preferred for routing, but direct
     *   playback cannot be verified through the public framework API. Those routes stay on the
     *   regular player path and are marked as legacy/unverified.
     * - On Android 13+, Elovaire classifies the direct-playback bitfield for diagnostics. Only
     *   non-offload direct support moves to the no-DSP direct path. Offload-only support stays on
     *   Media3's normal player path, where offload can be enabled as a separate power-saving path
     *   without being presented as bit-perfect/direct USB playback.
     */
    fun evaluate(
        sdkInt: Int = Build.VERSION.SDK_INT,
        routeContext: DirectPlaybackRouteContext,
        effectsActive: Boolean,
        trackConfig: DirectPlaybackTrackConfig?,
        evaluationKey: DirectPlaybackEvaluationKey?,
        supportClassification: DirectPlaybackSupportClassification = DirectPlaybackSupportClassification.notSupported,
        directPlaybackSupport: Int = AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED,
    ): BitPerfectPlaybackStatus {
        if (routeContext.hasBluetoothRouteForAttributes && !routeContext.hasEligibleUsbRoute) {
            return buildStatus(
                state = BitPerfectPlaybackState.BluetoothRoute,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (!routeContext.hasEligibleUsbRoute) {
            return buildStatus(
                state = BitPerfectPlaybackState.NoEligibleRoute,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (!AndroidCapabilities.supportsDirectPlaybackQuery(sdkInt)) {
            return buildStatus(
                state = BitPerfectPlaybackState.LegacyUnverifiedUsbRoute,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.LegacyUnverifiedUsbRoute,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (!routeContext.hasUsbRouteForAttributes) {
            return buildStatus(
                state = BitPerfectPlaybackState.UsbRouteDetectedButDirectSupportUnavailable,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (effectsActive) {
            return buildStatus(
                state = BitPerfectPlaybackState.EffectsActive,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (trackConfig == null) {
            return buildStatus(
                state = BitPerfectPlaybackState.FormatUnknown,
                directive = BitPerfectPlaybackDirective.KeepCurrent,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (trackConfig.offload || trackConfig.tunneling) {
            return buildStatus(
                state = BitPerfectPlaybackState.OffloadOrTunneling,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (evaluationKey == null) {
            return buildStatus(
                state = BitPerfectPlaybackState.FormatUnsupported,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
            )
        }

        if (!supportClassification.isSupported) {
            return buildStatus(
                state = BitPerfectPlaybackState.UsbRouteDetectedButDirectSupportUnavailable,
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
                evaluationKey = evaluationKey,
            )
        }

        if (supportClassification.supportsOffload && !supportClassification.supportsDirectPlayback) {
            return buildStatus(
                state = supportClassification.toPlaybackState(),
                directive = BitPerfectPlaybackDirective.PreferRegular,
                mode = DirectPlaybackMode.RegularPlaybackRequired,
                routeContext = routeContext,
                supportClassification = supportClassification,
                directPlaybackSupport = directPlaybackSupport,
                evaluationKey = evaluationKey,
            )
        }

        return buildStatus(
            state = supportClassification.toPlaybackState(),
            directive = BitPerfectPlaybackDirective.PreferDirect,
            mode = DirectPlaybackMode.DirectUsbPlaybackEligible,
            routeContext = routeContext,
            supportClassification = supportClassification,
            directPlaybackSupport = directPlaybackSupport,
            evaluationKey = evaluationKey,
            shouldUseDirectPlayback = true,
            confidence = BitPerfectConfidence.ExternallyUnverified,
        )
    }

    private fun DirectPlaybackSupportClassification.toPlaybackState(): BitPerfectPlaybackState {
        return when {
            supportsDirectPlayback && !supportsOffload ->
                BitPerfectPlaybackState.DirectPlaybackSupported

            supportsOffload && !supportsDirectPlayback ->
                BitPerfectPlaybackState.OffloadOnlyDirectPlaybackSupported

            else -> BitPerfectPlaybackState.Eligible
        }
    }

    private fun buildStatus(
        state: BitPerfectPlaybackState,
        directive: BitPerfectPlaybackDirective,
        mode: DirectPlaybackMode,
        routeContext: DirectPlaybackRouteContext,
        supportClassification: DirectPlaybackSupportClassification,
        directPlaybackSupport: Int,
        evaluationKey: DirectPlaybackEvaluationKey? = null,
        shouldUseDirectPlayback: Boolean = false,
        confidence: BitPerfectConfidence = defaultConfidence(
            routeContext = routeContext,
            supportClassification = supportClassification,
            shouldUseDirectPlayback = shouldUseDirectPlayback,
        ),
    ): BitPerfectPlaybackStatus {
        return BitPerfectPlaybackStatus(
            state = state,
            directive = directive,
            mode = mode,
            shouldUseDirectPlayback = shouldUseDirectPlayback,
            attributesRouteDeviceId = routeContext.attributesRouteDeviceId,
            attributesRouteType = routeContext.attributesRouteType,
            directPlaybackSupport = directPlaybackSupport,
            supportClassification = supportClassification,
            evaluationKey = evaluationKey,
            confidence = confidence,
            capability = DirectPlaybackCapability(
                attributeRouteKnown = routeContext.attributeRouteKnown,
                routeEligible = routeContext.hasUsbRouteForAttributes,
                formatSupported = evaluationKey != null,
                directSupportFlags = directPlaybackSupport,
                supportKinds = supportClassification.kinds,
            ),
            configuration = DirectPlaybackConfiguration(
                directModeRequested = shouldUseDirectPlayback,
                usbRouteReportedForAttributes = routeContext.hasUsbRouteForAttributes,
                signalProcessingBypassRequested = shouldUseDirectPlayback,
                currentTrackConfigKnown = evaluationKey != null,
            ),
        )
    }

    private fun defaultConfidence(
        routeContext: DirectPlaybackRouteContext,
        supportClassification: DirectPlaybackSupportClassification,
        shouldUseDirectPlayback: Boolean,
    ): BitPerfectConfidence = when {
        !routeContext.hasEligibleUsbRoute -> BitPerfectConfidence.NotEligible
        !supportClassification.isSupported -> BitPerfectConfidence.Unsupported
        shouldUseDirectPlayback -> BitPerfectConfidence.ExternallyUnverified
        else -> BitPerfectConfidence.DirectCapable
    }
}
