package elovaire.music.droidbeauty.app.domain.model

/** Integer sample-rate conversion. The output remains subject to Android's audio mixer. */
data class UpscalerSettings(
    val enabled: Boolean = false,
    val multiplier: Int = 2,
    val highPrecision: Boolean = false,
    val headroomDb: Int = 3,
) {
    fun sanitized(): UpscalerSettings = copy(
        multiplier = multiplier.takeIf { it == 2 || it == 4 || it == 8 } ?: 2,
        headroomDb = headroomDb.coerceIn(0, 6),
    )
}
