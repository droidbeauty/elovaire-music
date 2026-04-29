package elovaire.music.app.data.playback

internal class UsbAudioClassVolumeParser {
    fun parse(
        rawDescriptors: ByteArray,
        identity: UsbDacDeviceIdentity,
    ): UsbDacHardwareVolumeCapability? {
        var offset = 0
        var currentInterfaceNumber = -1
        var currentInterfaceClass = -1
        var currentInterfaceSubclass = -1
        var currentAudioSpecVersion = UsbAudioClassVersion.Unknown
        val candidates = mutableListOf<UsbDacHardwareVolumeCapability>()

        while (offset + 1 < rawDescriptors.size) {
            val length = rawDescriptors[offset].toInt() and 0xFF
            if (length <= 0 || offset + length > rawDescriptors.size) {
                break
            }
            val descriptorType = rawDescriptors[offset + 1].toInt() and 0xFF
            if (descriptorType == USB_DESCRIPTOR_TYPE_INTERFACE && length >= 9) {
                currentInterfaceNumber = rawDescriptors[offset + 2].toInt() and 0xFF
                currentInterfaceClass = rawDescriptors[offset + 5].toInt() and 0xFF
                currentInterfaceSubclass = rawDescriptors[offset + 6].toInt() and 0xFF
            } else if (
                currentInterfaceClass == USB_CLASS_AUDIO &&
                currentInterfaceSubclass == USB_AUDIO_SUBCLASS_CONTROL &&
                descriptorType == USB_DESCRIPTOR_TYPE_CS_INTERFACE &&
                length >= 3
            ) {
                val subtype = rawDescriptors[offset + 2].toInt() and 0xFF
                when (subtype) {
                    USB_AUDIO_CS_SUBTYPE_HEADER -> {
                        currentAudioSpecVersion = parseAudioSpecVersion(rawDescriptors, offset, length)
                    }

                    USB_AUDIO_CS_SUBTYPE_FEATURE_UNIT -> {
                        parseFeatureUnitDescriptor(
                            rawDescriptors = rawDescriptors,
                            offset = offset,
                            length = length,
                            interfaceNumber = currentInterfaceNumber,
                            audioClassVersion = currentAudioSpecVersion,
                            identity = identity,
                        )?.let(candidates::add)
                    }
                }
            }
            offset += length
        }

        return candidates
            .sortedWith(
                compareByDescending<UsbDacHardwareVolumeCapability> { it.canWriteVolume }
                    .thenByDescending { it.usesMasterChannel }
                    .thenBy { it.controlChannels.size },
            )
            .firstOrNull()
    }

    private fun parseAudioSpecVersion(
        rawDescriptors: ByteArray,
        offset: Int,
        length: Int,
    ): UsbAudioClassVersion {
        if (length < 5) return UsbAudioClassVersion.Unknown
        val version = littleEndianUnsignedShort(rawDescriptors, offset + 3)
        return when {
            version >= 0x0200 -> UsbAudioClassVersion.Uac2
            version >= 0x0100 -> UsbAudioClassVersion.Uac1
            else -> UsbAudioClassVersion.Unknown
        }
    }

    private fun parseFeatureUnitDescriptor(
        rawDescriptors: ByteArray,
        offset: Int,
        length: Int,
        interfaceNumber: Int,
        audioClassVersion: UsbAudioClassVersion,
        identity: UsbDacDeviceIdentity,
    ): UsbDacHardwareVolumeCapability? {
        if (interfaceNumber < 0 || length < 7) return null
        return when (audioClassVersion) {
            UsbAudioClassVersion.Uac2 -> parseUac2FeatureUnit(
                rawDescriptors,
                offset,
                length,
                interfaceNumber,
                identity,
            )

            UsbAudioClassVersion.Uac1,
            UsbAudioClassVersion.Unknown,
            -> parseUac1FeatureUnit(
                rawDescriptors,
                offset,
                length,
                interfaceNumber,
                identity,
            )
        }
    }

    private fun parseUac1FeatureUnit(
        rawDescriptors: ByteArray,
        offset: Int,
        length: Int,
        interfaceNumber: Int,
        identity: UsbDacDeviceIdentity,
    ): UsbDacHardwareVolumeCapability? {
        if (length < 7) return null
        val unitId = rawDescriptors[offset + 3].toInt() and 0xFF
        val controlSize = rawDescriptors[offset + 5].toInt() and 0xFF
        if (controlSize <= 0) return null
        val controlCount = ((length - 7) / controlSize).coerceAtLeast(0)
        if (controlCount <= 0) return null

        var masterHasVolume = false
        var masterHasMute = false
        val channelControls = mutableListOf<Int>()
        for (index in 0 until controlCount) {
            val controlOffset = offset + 6 + (index * controlSize)
            val controlMask = littleEndianUnsignedInt(rawDescriptors, controlOffset, controlSize)
            val hasMute = (controlMask and UAC1_MUTE_MASK) != 0
            val hasVolume = (controlMask and UAC1_VOLUME_MASK) != 0
            if (index == 0) {
                masterHasMute = hasMute
                masterHasVolume = hasVolume
            } else if (hasVolume) {
                channelControls += index
            }
        }
        if (!masterHasVolume && channelControls.isEmpty()) return null

        return UsbDacHardwareVolumeCapability(
            identity = identity,
            audioClassVersion = UsbAudioClassVersion.Uac1,
            interfaceNumber = interfaceNumber,
            featureUnitId = unitId,
            range = UsbDacHardwareVolumeRange(minRaw = 0, maxRaw = 0, stepRaw = 1),
            controlChannels = if (masterHasVolume) listOf(0) else channelControls,
            usesMasterChannel = masterHasVolume,
            muteSupported = masterHasMute,
            canReadCurrent = true,
            canWriteVolume = true,
        )
    }

    private fun parseUac2FeatureUnit(
        rawDescriptors: ByteArray,
        offset: Int,
        length: Int,
        interfaceNumber: Int,
        identity: UsbDacDeviceIdentity,
    ): UsbDacHardwareVolumeCapability? {
        if (length < 10) return null
        val unitId = rawDescriptors[offset + 3].toInt() and 0xFF
        val controlCount = ((length - 6) / UAC2_CONTROL_SIZE).coerceAtLeast(0)
        if (controlCount <= 0) return null

        var masterReadable = false
        var masterWritable = false
        var masterMute = false
        val readableChannels = mutableListOf<Int>()
        val writableChannels = mutableListOf<Int>()
        for (index in 0 until controlCount) {
            val controlOffset = offset + 5 + (index * UAC2_CONTROL_SIZE)
            val controls = littleEndianUnsignedInt(rawDescriptors, controlOffset, UAC2_CONTROL_SIZE)
            val hasMute = uac2ControlReadable(controls, UAC2_MUTE_CONTROL_SELECTOR)
            val volumeReadable = uac2ControlReadable(controls, UAC2_VOLUME_CONTROL_SELECTOR)
            val volumeWritable = uac2ControlWritable(controls, UAC2_VOLUME_CONTROL_SELECTOR)
            if (index == 0) {
                masterMute = hasMute
                masterReadable = volumeReadable
                masterWritable = volumeWritable
            } else {
                if (volumeReadable) {
                    readableChannels += index
                }
                if (volumeWritable) {
                    writableChannels += index
                }
            }
        }
        if (!masterReadable && readableChannels.isEmpty()) return null

        return UsbDacHardwareVolumeCapability(
            identity = identity,
            audioClassVersion = UsbAudioClassVersion.Uac2,
            interfaceNumber = interfaceNumber,
            featureUnitId = unitId,
            range = UsbDacHardwareVolumeRange(minRaw = 0, maxRaw = 0, stepRaw = 1),
            controlChannels = if (masterReadable || masterWritable) listOf(0) else readableChannels,
            usesMasterChannel = masterReadable || masterWritable,
            muteSupported = masterMute,
            canReadCurrent = masterReadable || readableChannels.isNotEmpty(),
            canWriteVolume = masterWritable || writableChannels.isNotEmpty(),
        )
    }

    private fun uac2ControlReadable(
        bmControls: Int,
        controlSelector: Int,
    ): Boolean {
        return ((bmControls shr (controlSelector * 2)) and 0x1) != 0
    }

    private fun uac2ControlWritable(
        bmControls: Int,
        controlSelector: Int,
    ): Boolean {
        return ((bmControls shr (controlSelector * 2)) and 0x2) != 0
    }

    private fun littleEndianUnsignedShort(
        bytes: ByteArray,
        offset: Int,
    ): Int {
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun littleEndianUnsignedInt(
        bytes: ByteArray,
        offset: Int,
        size: Int,
    ): Int {
        var value = 0
        repeat(size.coerceAtMost(4)) { index ->
            value = value or ((bytes[offset + index].toInt() and 0xFF) shl (index * 8))
        }
        return value
    }

    private companion object {
        const val USB_DESCRIPTOR_TYPE_INTERFACE = 0x04
        const val USB_DESCRIPTOR_TYPE_CS_INTERFACE = 0x24
        const val USB_CLASS_AUDIO = 0x01
        const val USB_AUDIO_SUBCLASS_CONTROL = 0x01
        const val USB_AUDIO_CS_SUBTYPE_HEADER = 0x01
        const val USB_AUDIO_CS_SUBTYPE_FEATURE_UNIT = 0x06
        const val UAC2_CONTROL_SIZE = 4
        const val UAC1_MUTE_MASK = 0x01
        const val UAC1_VOLUME_MASK = 0x02
        const val UAC2_MUTE_CONTROL_SELECTOR = 1
        const val UAC2_VOLUME_CONTROL_SELECTOR = 2
    }
}
