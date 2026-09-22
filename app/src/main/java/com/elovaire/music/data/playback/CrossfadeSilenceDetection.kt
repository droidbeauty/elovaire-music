package elovaire.music.droidbeauty.app.data.playback

import kotlin.math.pow
import kotlin.math.roundToInt

internal object CrossfadeSilencePolicy {
    const val BASE_LEVEL_DB = -90f
    const val MIN_LEVEL_DB = -100f
    const val MAX_LEVEL_DB = -80f
    const val LEVEL_STEP_DB = 5f
    const val MIN_SILENCE_DURATION_MS = 100L

    // -90 dBFS expressed as a linear PCM sample amplitude.
    const val BASE_AMPLITUDE_THRESHOLD = 0.0000316228f

    fun sanitizeLevelDb(value: Float): Float {
        val bounded = value.coerceIn(MIN_LEVEL_DB, MAX_LEVEL_DB)
        return ((bounded - MIN_LEVEL_DB) / LEVEL_STEP_DB).roundToInt() * LEVEL_STEP_DB + MIN_LEVEL_DB
    }

    fun amplitudeThresholdForDb(value: Float): Float {
        return 10f.pow(sanitizeLevelDb(value) / 20f)
    }
}
