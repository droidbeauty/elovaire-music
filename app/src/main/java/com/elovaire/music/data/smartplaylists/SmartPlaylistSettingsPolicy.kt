package elovaire.music.droidbeauty.app.data.smartplaylists

internal object SmartPlaylistSettingsPolicy {
    const val MIN_SONG_LIMIT = 20
    const val MAX_SONG_LIMIT = 50
    const val SONG_LIMIT_STEP = 5

    fun sanitizeSongLimit(value: Int): Int = value.coerceIn(MIN_SONG_LIMIT, MAX_SONG_LIMIT)
}
