package elovaire.music.droidbeauty.app.data.playback

import java.util.LinkedHashMap

internal data class UsbDacConnectionToken(
    val generation: Long,
    val deviceId: Int,
    val identityKey: String,
)

internal fun isCurrentUsbDacCallback(
    current: UsbDacConnectionToken?,
    callbackGeneration: Long,
    callbackDeviceId: Int,
    callbackIdentityKey: String,
): Boolean {
    return current?.generation == callbackGeneration &&
        current.deviceId == callbackDeviceId &&
        current.identityKey == callbackIdentityKey
}

/** Bounded LRU cache for descriptor-derived DAC capabilities. */
internal class UsbDacCapabilityCache(
    maxEntries: Int = DEFAULT_CAPABILITY_CACHE_SIZE,
) {
    private val values = object : LinkedHashMap<String, UsbDacHardwareVolumeCapability>(
        maxEntries.coerceAtLeast(1),
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, UsbDacHardwareVolumeCapability>,
        ): Boolean = size > maxEntries
    }

    operator fun get(key: String?): UsbDacHardwareVolumeCapability? {
        return key?.let(values::get)
    }

    operator fun set(key: String, value: UsbDacHardwareVolumeCapability) {
        values[key] = value
    }

    internal fun size(): Int = values.size

    private companion object {
        const val DEFAULT_CAPABILITY_CACHE_SIZE = 16
    }
}
