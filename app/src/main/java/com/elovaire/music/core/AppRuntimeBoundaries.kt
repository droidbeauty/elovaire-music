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

internal fun interface OperationIdGenerator {
    fun nextId(): String
}

internal object UuidOperationIdGenerator : OperationIdGenerator {
    override fun nextId(): String = UUID.randomUUID().toString()
}
