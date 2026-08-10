package elovaire.music.droidbeauty.app.data.playback

internal object CrossfadeSilencePolicy {
    const val BASE_LEVEL_DB = -80f
    const val MIN_SILENCE_DURATION_MS = 100L

    // -80 dBFS expressed as a linear PCM sample amplitude.
    const val BASE_AMPLITUDE_THRESHOLD = 0.0001f
}
