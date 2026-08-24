package elovaire.music.droidbeauty.app.core

import android.os.SystemClock
import java.util.UUID

internal interface AppClock {
    fun wallTimeMs(): Long
    fun elapsedTimeMs(): Long
}

internal object AndroidAppClock : AppClock {
    override fun wallTimeMs(): Long = System.currentTimeMillis()

    override fun elapsedTimeMs(): Long = SystemClock.elapsedRealtime()
}

internal fun isWallTimeDeadlineFresh(
    nowWallTimeMs: Long,
    deadlineWallTimeMs: Long,
    maxRemainingMs: Long,
): Boolean {
    if (maxRemainingMs <= 0L || deadlineWallTimeMs <= nowWallTimeMs) return false
    return deadlineWallTimeMs - nowWallTimeMs in 1L..maxRemainingMs
}

internal fun interface OperationIdGenerator {
    fun nextId(): String
}

internal object UuidOperationIdGenerator : OperationIdGenerator {
    override fun nextId(): String = UUID.randomUUID().toString()
}
