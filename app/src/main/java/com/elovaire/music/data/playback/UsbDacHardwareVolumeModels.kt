package elovaire.music.app.data.playback

import kotlin.math.abs
import kotlin.math.roundToInt

internal sealed interface UsbDacHardwareVolumeState {
    data object NoExternalDac : UsbDacHardwareVolumeState
    data object ExternalDacDetected : UsbDacHardwareVolumeState
    data object HardwareVolumeSupported : UsbDacHardwareVolumeState
    data object HardwareVolumeUnsupported : UsbDacHardwareVolumeState
    data object HardwareVolumeActive : UsbDacHardwareVolumeState
    data object HardwareVolumeUnavailable : UsbDacHardwareVolumeState
    data class Error(val message: String) : UsbDacHardwareVolumeState
}

internal enum class UsbAudioClassVersion {
    Uac1,
    Uac2,
    Unknown,
}

internal data class UsbDacDeviceIdentity(
    val vendorId: Int,
    val productId: Int,
    val manufacturerName: String?,
    val productName: String?,
    val serialNumber: String?,
) {
    val isReliable: Boolean
        get() = vendorId > 0 &&
            productId > 0 &&
            (
                !serialNumber.isNullOrBlank() ||
                    (!manufacturerName.isNullOrBlank() && !productName.isNullOrBlank())
                )

    fun persistenceKey(): String {
        return buildString {
            append(vendorId)
            append(":")
            append(productId)
            append(":")
            append(manufacturerName.orEmpty().trim().lowercase())
            append(":")
            append(productName.orEmpty().trim().lowercase())
            append(":")
            append(serialNumber.orEmpty().trim().lowercase())
        }
    }
}

internal data class UsbDacHardwareVolumeRange(
    val minRaw: Int,
    val maxRaw: Int,
    val stepRaw: Int,
) {
    fun clamp(rawValue: Int): Int {
        val bounded = rawValue.coerceIn(minRaw, maxRaw)
        val step = abs(stepRaw).coerceAtLeast(1)
        val snapped = minRaw + (((bounded - minRaw).toDouble() / step.toDouble()).roundToInt() * step)
        return snapped.coerceIn(minRaw, maxRaw)
    }
}

internal data class UsbDacHardwareVolumeCapability(
    val identity: UsbDacDeviceIdentity,
    val audioClassVersion: UsbAudioClassVersion,
    val interfaceNumber: Int,
    val featureUnitId: Int,
    val range: UsbDacHardwareVolumeRange,
    val controlChannels: List<Int>,
    val usesMasterChannel: Boolean,
    val muteSupported: Boolean,
    val canReadCurrent: Boolean,
    val canWriteVolume: Boolean,
)

internal data class UsbDacHardwareVolumeStatus(
    val state: UsbDacHardwareVolumeState,
    val currentNormalizedVolume: Float? = null,
    val range: UsbDacHardwareVolumeRange? = null,
    val identity: UsbDacDeviceIdentity? = null,
    val canWriteVolume: Boolean = false,
    val reason: String? = null,
) {
    val shouldOwnVolumeControls: Boolean
        get() = canWriteVolume &&
            (
                state == UsbDacHardwareVolumeState.HardwareVolumeSupported ||
                    state == UsbDacHardwareVolumeState.HardwareVolumeActive
                )

    val shouldBypassSoftwareVolume: Boolean
        get() = shouldOwnVolumeControls
}

internal object UsbDacHardwareVolumeMath {
    fun normalizedToRaw(
        normalizedValue: Float,
        range: UsbDacHardwareVolumeRange,
    ): Int {
        val clamped = normalizedValue.coerceIn(0f, 1f)
        val raw = range.minRaw + ((range.maxRaw - range.minRaw) * clamped)
        return range.clamp(raw.roundToInt())
    }

    fun rawToNormalized(
        rawValue: Int,
        range: UsbDacHardwareVolumeRange,
    ): Float {
        val span = (range.maxRaw - range.minRaw).coerceAtLeast(1)
        val clamped = range.clamp(rawValue)
        return ((clamped - range.minRaw).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    fun shouldAutoRestoreStoredVolume(
        identity: UsbDacDeviceIdentity,
        currentVolumeReadable: Boolean,
        storedNormalizedVolume: Float?,
    ): Boolean {
        return identity.isReliable && currentVolumeReadable && storedNormalizedVolume != null
    }
}

internal class UsbDacHardwareVolumeController {
    private var capability: UsbDacHardwareVolumeCapability? = null
    private var currentNormalizedVolume: Float? = null
    private var identity: UsbDacDeviceIdentity? = null
    private var lastReason: String? = null
    private var state: UsbDacHardwareVolumeState = UsbDacHardwareVolumeState.NoExternalDac

    fun status(): UsbDacHardwareVolumeStatus {
        return UsbDacHardwareVolumeStatus(
            state = state,
            currentNormalizedVolume = currentNormalizedVolume,
            range = capability?.range,
            identity = identity,
            canWriteVolume = capability?.canWriteVolume == true,
            reason = lastReason,
        )
    }

    fun onNoExternalDac() {
        capability = null
        currentNormalizedVolume = null
        identity = null
        lastReason = null
        state = UsbDacHardwareVolumeState.NoExternalDac
    }

    fun onExternalDacDetected(identity: UsbDacDeviceIdentity?) {
        capability = null
        currentNormalizedVolume = null
        this.identity = identity
        lastReason = null
        state = UsbDacHardwareVolumeState.ExternalDacDetected
    }

    fun onHardwareVolumeUnavailable(reason: String) {
        capability = null
        currentNormalizedVolume = null
        lastReason = reason
        state = UsbDacHardwareVolumeState.HardwareVolumeUnavailable
    }

    fun onHardwareVolumeUnsupported(reason: String) {
        capability = null
        currentNormalizedVolume = null
        lastReason = reason
        state = UsbDacHardwareVolumeState.HardwareVolumeUnsupported
    }

    fun onHardwareVolumeSupported(
        capability: UsbDacHardwareVolumeCapability,
        currentRawValue: Int?,
    ) {
        this.capability = capability
        identity = capability.identity
        currentNormalizedVolume = currentRawValue?.let { UsbDacHardwareVolumeMath.rawToNormalized(it, capability.range) }
        lastReason = null
        state = if (currentNormalizedVolume != null && capability.canWriteVolume) {
            UsbDacHardwareVolumeState.HardwareVolumeActive
        } else {
            UsbDacHardwareVolumeState.HardwareVolumeSupported
        }
    }

    fun onHardwareVolumeApplied(appliedRawValue: Int) {
        val currentCapability = capability ?: return
        currentNormalizedVolume = UsbDacHardwareVolumeMath.rawToNormalized(appliedRawValue, currentCapability.range)
        state = UsbDacHardwareVolumeState.HardwareVolumeActive
        lastReason = null
    }

    fun onError(message: String) {
        lastReason = message
        state = UsbDacHardwareVolumeState.Error(message)
    }

    fun currentCapability(): UsbDacHardwareVolumeCapability? = capability
}
