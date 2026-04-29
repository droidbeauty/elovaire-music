package elovaire.music.app.data.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import elovaire.music.app.BuildConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class UsbDacHardwareVolumeManager(
    context: Context,
    private val audioManager: AudioManager?,
    private val usbManager: UsbManager?,
    private val parser: UsbAudioClassVolumeParser = UsbAudioClassVolumeParser(),
) {
    private val appContext = context.applicationContext
    private val controller = UsbDacHardwareVolumeController()
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _status = MutableStateFlow(controller.status())
    val status: StateFlow<UsbDacHardwareVolumeStatus> = _status.asStateFlow()

    private var currentAudioDeviceDescriptor: UsbAudioDeviceDescriptor? = null
    private var currentUsbDevice: UsbDevice? = null
    private var currentCapability: UsbDacHardwareVolumeCapability? = null
    private var permissionReceiverRegistered = false
    private var pendingRequestedVolume: Float? = null

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            when (intent.action) {
                ACTION_USB_DAC_PERMISSION -> {
                    val device = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (device == null || currentUsbDevice?.deviceId != device.deviceId) return
                    if (granted) {
                        refreshConnectedDevice()
                        pendingRequestedVolume?.let { requested ->
                            pendingRequestedVolume = null
                            setHardwareVolume(requested)
                        }
                    } else {
                        controller.onHardwareVolumeUnavailable("USB permission denied")
                        publishStatus("permission-denied")
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val detached = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (detached != null && detached.deviceId == currentUsbDevice?.deviceId) {
                        clearCurrentDevice()
                        publishStatus("usb-detached")
                    }
                }
            }
        }
    }

    init {
        registerReceiverIfNeeded()
        refreshConnectedDevice()
    }

    fun refreshConnectedDevice() {
        val selectedAudioDevice = currentAudioDeviceDescriptor
        val usbDevice = selectedAudioDevice?.let(::findUsbAudioDeviceForDescriptor)
        if (selectedAudioDevice == null || usbDevice == null) {
            clearCurrentDevice()
            publishStatus("no-external-dac")
            return
        }
        val identity = usbDevice.toIdentity()
        currentUsbDevice = usbDevice
        controller.onExternalDacDetected(identity)
        currentCapability = null
        publishStatus("dac-detected")

        if (usbManager?.hasPermission(usbDevice) != true) {
            return
        }
        inspectCurrentUsbDevice(usbDevice, identity)
    }

    fun updateAudioOutputDevice(
        audioDeviceDescriptor: UsbAudioDeviceDescriptor?,
    ) {
        currentAudioDeviceDescriptor = audioDeviceDescriptor
        refreshConnectedDevice()
    }

    fun release() {
        clearCurrentDevice()
        if (permissionReceiverRegistered) {
            runCatching { appContext.unregisterReceiver(permissionReceiver) }
            permissionReceiverRegistered = false
        }
    }

    fun isHardwareVolumeSupported(): Boolean {
        return status.value.state == UsbDacHardwareVolumeState.HardwareVolumeSupported ||
            status.value.state == UsbDacHardwareVolumeState.HardwareVolumeActive
    }

    fun shouldOwnVolumeControls(): Boolean = status.value.shouldOwnVolumeControls

    fun shouldBypassSoftwareVolume(): Boolean = status.value.shouldBypassSoftwareVolume

    fun currentHardwareVolume(): Float? = status.value.currentNormalizedVolume

    fun hardwareVolumeRange(): UsbDacHardwareVolumeRange? = status.value.range

    fun setHardwareVolume(normalizedValue: Float): Boolean {
        val usbDevice = currentUsbDevice ?: return false
        if (usbManager?.hasPermission(usbDevice) != true) {
            pendingRequestedVolume = normalizedValue.coerceIn(0f, 1f)
            requestPermissionIfNeeded(usbDevice)
            return true
        }
        val capability = currentCapability ?: run {
            inspectCurrentUsbDevice(usbDevice, usbDevice.toIdentity())
            currentCapability ?: return false
        }
        if (!capability.canWriteVolume) {
            controller.onHardwareVolumeUnsupported("DAC volume is read-only")
            publishStatus("read-only")
            return false
        }
        val beforeSystemVolume = currentSystemMediaVolume()
        val targetRaw = UsbDacHardwareVolumeMath.normalizedToRaw(
            normalizedValue = normalizedValue,
            range = capability.range,
        )
        val applied = performVolumeWrite(usbDevice, capability, targetRaw)
        val afterSystemVolume = currentSystemMediaVolume()
        logDebug(
            "event=set identity=${capability.identity.persistenceKey()} api=${Build.VERSION.SDK_INT} " +
                "supported=true min=${capability.range.minRaw} max=${capability.range.maxRaw} res=${capability.range.stepRaw} " +
                "master=${capability.usesMasterChannel} current=${status.value.currentNormalizedVolume ?: -1f} " +
                "requested=$normalizedValue appliedRaw=$targetRaw systemBefore=$beforeSystemVolume systemAfter=$afterSystemVolume",
        )
        if (applied) {
            controller.onHardwareVolumeApplied(targetRaw)
            storePerDeviceVolume(capability.identity, normalizedValue)
            publishStatus("volume-applied")
        } else {
            controller.onHardwareVolumeUnavailable("Hardware volume write failed")
            publishStatus("volume-write-failed")
        }
        return true
    }

    fun increaseHardwareVolume(): Boolean {
        val current = status.value.currentNormalizedVolume ?: return false
        val step = currentHardwareStepNormalized() ?: DEFAULT_NORMALIZED_STEP
        return setHardwareVolume((current + step).coerceIn(0f, 1f))
    }

    fun decreaseHardwareVolume(): Boolean {
        val current = status.value.currentNormalizedVolume ?: return false
        val step = currentHardwareStepNormalized() ?: DEFAULT_NORMALIZED_STEP
        return setHardwareVolume((current - step).coerceIn(0f, 1f))
    }

    private fun currentHardwareStepNormalized(): Float? {
        val range = currentCapability?.range ?: return null
        val span = (range.maxRaw - range.minRaw).coerceAtLeast(1)
        return (range.stepRaw.coerceAtLeast(1).toFloat() / span.toFloat()).coerceIn(0.001f, 1f)
    }

    private fun inspectCurrentUsbDevice(
        usbDevice: UsbDevice,
        identity: UsbDacDeviceIdentity,
    ) {
        val connection = usbManager?.openDevice(usbDevice)
        if (connection == null) {
            controller.onHardwareVolumeUnavailable("Unable to open USB device")
            publishStatus("open-failed")
            return
        }
        connection.useSafely { safeConnection ->
            val parsedCapability = parser.parse(
                rawDescriptors = safeConnection.getRawDescriptors() ?: ByteArray(0),
                identity = identity,
            )
            if (parsedCapability == null) {
                controller.onHardwareVolumeUnsupported("No USB Audio Class feature unit volume control found")
                publishStatus("unsupported-no-feature-unit")
                return@useSafely
            }
            val resolvedRange = readRange(safeConnection, parsedCapability)
            if (resolvedRange == null) {
                controller.onHardwareVolumeUnsupported("Unable to read DAC volume range")
                publishStatus("unsupported-no-range")
                return@useSafely
            }
            val resolvedCapability = parsedCapability.copy(
                range = resolvedRange,
                canWriteVolume = parsedCapability.canWriteVolume && resolvedRange.maxRaw > resolvedRange.minRaw,
            )
            currentCapability = resolvedCapability
            val currentRaw = if (resolvedCapability.canReadCurrent) {
                readCurrentVolumeRaw(safeConnection, resolvedCapability)
            } else {
                null
            }
            controller.onHardwareVolumeSupported(resolvedCapability, currentRaw)
            val stored = restoreStoredVolumeIfSafe(resolvedCapability, currentRaw != null)
            logDebug(
                "event=detect identity=${identity.persistenceKey()} api=${Build.VERSION.SDK_INT} supported=${resolvedCapability.canWriteVolume} " +
                    "min=${resolvedRange.minRaw} max=${resolvedRange.maxRaw} res=${resolvedRange.stepRaw} " +
                    "master=${resolvedCapability.usesMasterChannel} currentRaw=${currentRaw ?: Int.MIN_VALUE} stored=${stored ?: -1f}",
            )
            publishStatus("supported")
        }
    }

    private fun restoreStoredVolumeIfSafe(
        capability: UsbDacHardwareVolumeCapability,
        currentReadable: Boolean,
    ): Float? {
        val stored = lookupStoredVolume(capability.identity)
        return if (UsbDacHardwareVolumeMath.shouldAutoRestoreStoredVolume(capability.identity, currentReadable, stored)) {
            stored
        } else {
            null
        }
    }

    private fun performVolumeWrite(
        usbDevice: UsbDevice,
        capability: UsbDacHardwareVolumeCapability,
        targetRaw: Int,
    ): Boolean {
        val connection = usbManager?.openDevice(usbDevice) ?: return false
        return connection.useSafely { safeConnection ->
            capability.controlChannels.all { channel ->
                setVolumeRaw(
                    connection = safeConnection,
                    capability = capability,
                    channel = channel,
                    rawValue = targetRaw,
                )
            }
        }
    }

    private fun readRange(
        connection: UsbDeviceConnection,
        capability: UsbDacHardwareVolumeCapability,
    ): UsbDacHardwareVolumeRange? {
        val preferredChannel = capability.controlChannels.firstOrNull() ?: return null
        val min = getVolumeRequest(connection, capability, REQUEST_GET_MIN, preferredChannel) ?: return null
        val max = getVolumeRequest(connection, capability, REQUEST_GET_MAX, preferredChannel) ?: return null
        val res = getVolumeRequest(connection, capability, REQUEST_GET_RES, preferredChannel) ?: 1
        return UsbDacHardwareVolumeRange(
            minRaw = min,
            maxRaw = max,
            stepRaw = res.coerceAtLeast(1),
        )
    }

    private fun readCurrentVolumeRaw(
        connection: UsbDeviceConnection,
        capability: UsbDacHardwareVolumeCapability,
    ): Int? {
        val readings = capability.controlChannels.mapNotNull { channel ->
            getVolumeRequest(connection, capability, REQUEST_GET_CUR, channel)
        }
        if (readings.isEmpty()) return null
        return (readings.sum().toFloat() / readings.size.toFloat()).toInt()
    }

    private fun getVolumeRequest(
        connection: UsbDeviceConnection,
        capability: UsbDacHardwareVolumeCapability,
        request: Int,
        channel: Int,
    ): Int? {
        val buffer = ByteArray(2)
        val transferred = connection.controlTransfer(
            REQUEST_TYPE_CLASS_INTERFACE_IN,
            request,
            (CONTROL_SELECTOR_VOLUME shl 8) or channel,
            (capability.featureUnitId shl 8) or capability.interfaceNumber,
            buffer,
            buffer.size,
            USB_CONTROL_TIMEOUT_MS,
        )
        if (transferred < 2) return null
        return ByteBuffer.wrap(buffer)
            .order(ByteOrder.LITTLE_ENDIAN)
            .short
            .toInt()
    }

    private fun setVolumeRaw(
        connection: UsbDeviceConnection,
        capability: UsbDacHardwareVolumeCapability,
        channel: Int,
        rawValue: Int,
    ): Boolean {
        val clamped = capability.range.clamp(rawValue)
        val payload = ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(clamped.toShort())
            .array()
        val transferred = connection.controlTransfer(
            REQUEST_TYPE_CLASS_INTERFACE_OUT,
            REQUEST_SET_CUR,
            (CONTROL_SELECTOR_VOLUME shl 8) or channel,
            (capability.featureUnitId shl 8) or capability.interfaceNumber,
            payload,
            payload.size,
            USB_CONTROL_TIMEOUT_MS,
        )
        return transferred == payload.size
    }

    private fun requestPermissionIfNeeded(usbDevice: UsbDevice) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            usbDevice.deviceId,
            Intent(ACTION_USB_DAC_PERMISSION).setPackage(appContext.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        usbManager?.requestPermission(usbDevice, pendingIntent)
    }

    private fun registerReceiverIfNeeded() {
        if (permissionReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_DAC_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            appContext,
            permissionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        permissionReceiverRegistered = true
    }

    private fun findUsbAudioDeviceForDescriptor(
        descriptor: UsbAudioDeviceDescriptor,
    ): UsbDevice? {
        val usbAudioDevices = usbManager
            ?.getDeviceList()
            ?.values
            ?.filter(::isUsbAudioDevice)
            .orEmpty()
        if (usbAudioDevices.isEmpty()) return null
        if (usbAudioDevices.size == 1) return usbAudioDevices.first()

        val productName = descriptor.productName?.trim().orEmpty()
        return usbAudioDevices.firstOrNull { usbDevice ->
            val usbName = usbDevice.productName.orEmpty().trim()
            productName.isNotBlank() && usbName.equals(productName, ignoreCase = true)
        } ?: usbAudioDevices.first()
    }

    private fun isUsbAudioDevice(device: UsbDevice): Boolean {
        if (device.deviceClass == UsbConstants.USB_CLASS_AUDIO) return true
        return (0 until device.interfaceCount).any { index ->
            device.getInterface(index).interfaceClass == UsbConstants.USB_CLASS_AUDIO
        }
    }

    private fun currentSystemMediaVolume(): Int {
        return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    private fun storePerDeviceVolume(
        identity: UsbDacDeviceIdentity,
        normalizedValue: Float,
    ) {
        if (!identity.isReliable) return
        preferences.edit()
            .putFloat(identity.persistenceKey(), normalizedValue.coerceIn(0f, 1f))
            .apply()
    }

    private fun lookupStoredVolume(identity: UsbDacDeviceIdentity): Float? {
        if (!identity.isReliable) return null
        if (!preferences.contains(identity.persistenceKey())) return null
        return preferences.getFloat(identity.persistenceKey(), 0f).coerceIn(0f, 1f)
    }

    private fun clearCurrentDevice() {
        currentUsbDevice = null
        currentCapability = null
        pendingRequestedVolume = null
        controller.onNoExternalDac()
    }

    private fun publishStatus(reason: String) {
        _status.value = controller.status()
        logDebug(
            "event=status reason=$reason api=${Build.VERSION.SDK_INT} state=${status.value.state.javaClass.simpleName} " +
                "identity=${status.value.identity?.persistenceKey().orEmpty()} current=${status.value.currentNormalizedVolume ?: -1f} " +
                "range=${status.value.range?.minRaw ?: 0}:${status.value.range?.maxRaw ?: 0}:${status.value.range?.stepRaw ?: 0}",
        )
    }

    private fun UsbDevice.toIdentity(): UsbDacDeviceIdentity {
        return UsbDacDeviceIdentity(
            vendorId = vendorId,
            productId = productId,
            manufacturerName = manufacturerName,
            productName = productName,
            serialNumber = safeSerialNumber(),
        )
    }

    private fun UsbDevice.safeSerialNumber(): String? {
        return runCatching { serialNumber }.getOrNull()
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }

    private inline fun <T> UsbDeviceConnection.useSafely(block: (UsbDeviceConnection) -> T): T {
        return try {
            block(this)
        } finally {
            runCatching { close() }
        }
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T> Intent.getParcelableExtraCompat(name: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, T::class.java)
        } else {
            getParcelableExtra(name) as? T
        }
    }

    private companion object {
        const val TAG = "UsbDacVolume"
        const val PREFS_NAME = "usb_dac_hardware_volume"
        const val ACTION_USB_DAC_PERMISSION = "elovaire.music.app.action.USB_DAC_PERMISSION"
        const val CONTROL_SELECTOR_VOLUME = 0x02
        const val REQUEST_SET_CUR = 0x01
        const val REQUEST_GET_CUR = 0x81
        const val REQUEST_GET_MIN = 0x82
        const val REQUEST_GET_MAX = 0x83
        const val REQUEST_GET_RES = 0x84
        const val REQUEST_TYPE_CLASS_INTERFACE_OUT = 0x21
        const val REQUEST_TYPE_CLASS_INTERFACE_IN = 0xA1
        const val USB_CONTROL_TIMEOUT_MS = 1_000
        const val DEFAULT_NORMALIZED_STEP = 0.05f
    }
}
