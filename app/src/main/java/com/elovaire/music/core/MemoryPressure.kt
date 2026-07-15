package elovaire.music.droidbeauty.app.core

internal enum class MemoryPressure {
    Normal,
    Moderate,
    Critical,
}

internal fun memoryPressureForTrimLevel(level: Int): MemoryPressure = when {
    level == RUNNING_CRITICAL_TRIM_LEVEL || level >= COMPLETE_TRIM_LEVEL -> MemoryPressure.Critical
    level == RUNNING_LOW_TRIM_LEVEL || level >= BACKGROUND_TRIM_LEVEL -> MemoryPressure.Moderate
    else -> MemoryPressure.Normal
}

private const val RUNNING_LOW_TRIM_LEVEL = 10
private const val RUNNING_CRITICAL_TRIM_LEVEL = 15
private const val BACKGROUND_TRIM_LEVEL = 40
private const val COMPLETE_TRIM_LEVEL = 80
