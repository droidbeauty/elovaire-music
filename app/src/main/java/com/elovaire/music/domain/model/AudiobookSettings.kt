package elovaire.music.droidbeauty.app.domain.model

data class AudiobookSettings(
    val rewindSeconds: Int = 15,
    val forwardSeconds: Int = 15,
    val resumePlayback: Boolean = true,
)
