package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationMetadata
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationPolicy

internal data class EffectiveDspState(
    val processorsEnabled: Boolean,
    val normalizationEnabled: Boolean,
    val fineGain: Float,
    val altersSignal: Boolean,
    val configurationKey: String,
)

internal fun hasRequestedSignalChanges(
    effectsRequested: Boolean,
    normalizationRequested: Boolean,
    normalizationMetadata: VolumeNormalizationMetadata?,
    softwareGainActive: Boolean = false,
): Boolean = resolveEffectiveDspState(
    effectsRequested = effectsRequested,
    normalizationRequested = normalizationRequested,
    normalizationMetadata = normalizationMetadata,
    directPlaybackActive = false,
    softwareGainAllowed = true,
    baseGain = 1f,
).altersSignal || softwareGainActive

internal fun resolveEffectiveDspState(
    effectsRequested: Boolean,
    normalizationRequested: Boolean,
    normalizationMetadata: VolumeNormalizationMetadata?,
    directPlaybackActive: Boolean,
    softwareGainAllowed: Boolean,
    baseGain: Float,
): EffectiveDspState {
    val softwareProcessingAllowed = softwareGainAllowed && !directPlaybackActive
    val processorsEnabled = effectsRequested && softwareProcessingAllowed
    val normalizationEnabled = normalizationRequested && softwareProcessingAllowed
    val gain = VolumeNormalizationPolicy.effectivePlayerGain(
        baseGain = baseGain,
        metadata = normalizationMetadata,
        enabled = normalizationEnabled,
        softwareGainAllowed = softwareProcessingAllowed,
    )
    val normalizationAltersSignal = VolumeNormalizationPolicy.isSignalAltering(
        metadata = normalizationMetadata,
        enabled = normalizationEnabled,
    )
    return EffectiveDspState(
        processorsEnabled = processorsEnabled,
        normalizationEnabled = normalizationEnabled,
        fineGain = gain,
        altersSignal = processorsEnabled || normalizationAltersSignal,
        configurationKey = "processors=$processorsEnabled;normalization=$normalizationEnabled",
    )
}
