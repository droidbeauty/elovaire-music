package elovaire.music.droidbeauty.app.quality

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.PowerManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.AndroidCapabilities
import elovaire.music.droidbeauty.app.core.AppExitCategory
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.core.StrictModeViolationRecorder
import elovaire.music.droidbeauty.app.core.requiredAudioPermission

internal enum class PlatformPermissionState {
    NotRequired,
    Granted,
    Denied,
}

internal enum class PlatformNotificationState {
    MediaSessionExempt,
    Enabled,
    Disabled,
    Unknown,
}

internal enum class PlatformAudioFocusState {
    Unknown,
    Granted,
    Denied,
    Active,
}

internal enum class PlatformPlaybackState {
    Unknown,
    Idle,
    Ready,
    Playing,
    Paused,
    Ended,
    Error,
}

internal data class PlatformCompatibilityRuntimeState(
    val activityVisible: Boolean? = null,
    val appForeground: Boolean? = null,
    val playbackFgsRunning: Boolean? = null,
    val mediaLibraryServiceRunning: Boolean? = null,
    val keepAliveServiceRunning: Boolean? = null,
    val mediaSessionActive: Boolean? = null,
    val playerApplicationLooperCorrect: Boolean? = null,
    val audioFocusState: PlatformAudioFocusState = PlatformAudioFocusState.Unknown,
    val playbackState: PlatformPlaybackState = PlatformPlaybackState.Unknown,
    val workStopReason: Int? = null,
    val mediaStoreReadable: Boolean? = null,
    val networkSourceCount: Int? = null,
    val memoryPressureMode: MemoryPressure = MemoryPressure.Normal,
    val lastExitCategory: AppExitCategory? = null,
    val compatibilityChanges: Set<String> = emptySet(),
    val resourceCounters: Map<String, Int> = emptyMap(),
)

internal data class PlatformCompatibilitySnapshot(
    val sdkInt: Int,
    val targetSdk: Int,
    val buildType: String,
    val debuggable: Boolean,
    val physicalDevice: Boolean,
    val requiredAudioPermission: String,
    val audioPermissionState: PlatformPermissionState,
    val localNetworkPermissionRequired: Boolean,
    val localNetworkPermissionState: PlatformPermissionState,
    val notificationPermissionRelevant: Boolean,
    val notificationState: PlatformNotificationState,
    val activityVisible: Boolean?,
    val appForeground: Boolean?,
    val playbackFgsRunning: Boolean?,
    val mediaLibraryServiceRunning: Boolean?,
    val keepAliveServiceRunning: Boolean?,
    val mediaSessionActive: Boolean?,
    val playerApplicationLooperCorrect: Boolean?,
    val audioFocusState: PlatformAudioFocusState,
    val playbackState: PlatformPlaybackState,
    val standbyBucket: Int?,
    val backgroundRestricted: Boolean?,
    val batterySaver: Boolean?,
    val workStopReason: Int?,
    val mediaStoreReadable: Boolean?,
    val networkSourceCount: Int?,
    val externalVolumeCount: Int?,
    val safGrantCount: Int,
    val compatibilityChanges: Set<String>,
    val memoryPressureMode: MemoryPressure,
    val lastExitCategory: AppExitCategory?,
    val strictModeViolationCount: Int,
    val resourceCounters: Map<String, Int>,
)

internal fun Context.platformCompatibilitySnapshot(
    runtimeState: PlatformCompatibilityRuntimeState = PlatformCompatibilityRuntimeState(),
): PlatformCompatibilitySnapshot {
    val appInfo = applicationInfo
    val sdkInt = Build.VERSION.SDK_INT
    val audioPermission = requiredAudioPermission(sdkInt)
    val localNetworkRequired = AndroidCapabilities.requiresLocalNetworkPermission(sdkInt)
    val audioPermissionState = if (
        ContextCompat.checkSelfPermission(
            this,
            audioPermission,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        PlatformPermissionState.Granted
    } else {
        PlatformPermissionState.Denied
    }
    val localNetworkState = when {
        !localNetworkRequired -> PlatformPermissionState.NotRequired
        ContextCompat.checkSelfPermission(
            this,
            AndroidCapabilities.LOCAL_NETWORK_PERMISSION,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> PlatformPermissionState.Granted
        else -> PlatformPermissionState.Denied
    }
    val notificationManager = getSystemService(NotificationManager::class.java)
    val notificationPermissionRelevant = false
    val notificationState = when {
        notificationManager == null -> PlatformNotificationState.Unknown
        !notificationPermissionRelevant -> PlatformNotificationState.MediaSessionExempt
        notificationManager.areNotificationsEnabled() -> PlatformNotificationState.Enabled
        else -> PlatformNotificationState.Disabled
    }
    val activityManager = getSystemService(ActivityManager::class.java)
    val usageStatsManager = getSystemService(UsageStatsManager::class.java)
    val powerManager = getSystemService(PowerManager::class.java)
    return PlatformCompatibilitySnapshot(
        sdkInt = sdkInt,
        targetSdk = appInfo.targetSdkVersion,
        buildType = if (BuildConfig.DEBUG) "debug" else "release",
        debuggable = appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
        physicalDevice = isPhysicalDevice(),
        requiredAudioPermission = audioPermission,
        audioPermissionState = audioPermissionState,
        localNetworkPermissionRequired = localNetworkRequired,
        localNetworkPermissionState = localNetworkState,
        notificationPermissionRelevant = notificationPermissionRelevant,
        notificationState = notificationState,
        activityVisible = runtimeState.activityVisible,
        appForeground = runtimeState.appForeground,
        playbackFgsRunning = runtimeState.playbackFgsRunning,
        mediaLibraryServiceRunning = runtimeState.mediaLibraryServiceRunning,
        keepAliveServiceRunning = runtimeState.keepAliveServiceRunning,
        mediaSessionActive = runtimeState.mediaSessionActive,
        playerApplicationLooperCorrect = runtimeState.playerApplicationLooperCorrect,
        audioFocusState = runtimeState.audioFocusState,
        playbackState = runtimeState.playbackState,
        standbyBucket = runCatching { usageStatsManager?.appStandbyBucket }.getOrNull(),
        backgroundRestricted = runCatching { activityManager?.isBackgroundRestricted }.getOrNull(),
        batterySaver = runCatching { powerManager?.isPowerSaveMode }.getOrNull(),
        workStopReason = runtimeState.workStopReason,
        mediaStoreReadable = runtimeState.mediaStoreReadable,
        networkSourceCount = runtimeState.networkSourceCount?.coerceAtLeast(0),
        externalVolumeCount = runCatching { MediaStore.getExternalVolumeNames(this).size }.getOrNull(),
        safGrantCount = contentResolver.persistedUriPermissions.size,
        compatibilityChanges = runtimeState.compatibilityChanges
            .asSequence()
            .sorted()
            .take(MAX_DIAGNOSTIC_ENTRIES)
            .toSet(),
        memoryPressureMode = runtimeState.memoryPressureMode,
        lastExitCategory = runtimeState.lastExitCategory,
        strictModeViolationCount = StrictModeViolationRecorder.snapshot().sumOf { it.count },
        resourceCounters = runtimeState.resourceCounters
            .asSequence()
            .sortedBy { (name, _) -> name }
            .take(MAX_DIAGNOSTIC_ENTRIES)
            .associate { (name, value) -> name to value.coerceAtLeast(0) },
    )
}

private fun isPhysicalDevice(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val hardware = Build.HARDWARE.lowercase()
    val model = Build.MODEL.lowercase()
    return !fingerprint.contains("generic") &&
        !fingerprint.contains("emulator") &&
        hardware !in setOf("goldfish", "ranchu", "cutf_cvm") &&
        !model.contains("sdk_gphone")
}

private const val MAX_DIAGNOSTIC_ENTRIES = 16
