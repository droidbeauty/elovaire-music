package elovaire.music.droidbeauty.app.core

internal class FakeAppClock(
    var wallTime: Long = 0L,
    var elapsedTime: Long = 0L,
) : AppClock {
    override fun wallTimeMs(): Long = wallTime

    override fun elapsedTimeMs(): Long = elapsedTime
}
