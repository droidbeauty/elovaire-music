package elovaire.music.droidbeauty.app.core.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BackendOperationContextTest {
    @Test
    fun fieldsPreserveCorrelationAndMonotonicDuration() {
        val context = BackendOperationContext(
            id = "operation-7",
            subsystem = BackendSubsystem.MediaMutation,
            startedAtElapsedMs = 100L,
        )

        val fields = context.fields(
            phase = "verified",
            elapsedTimeMs = 175L,
            extra = mapOf("retry" to "1"),
        )

        assertEquals("operation-7", fields["operation_id"])
        assertEquals("MediaMutation", fields["subsystem"])
        assertEquals("verified", fields["phase"])
        assertEquals("75", fields["elapsed_ms"])
        assertEquals("1", fields["retry"])
        assertFalse(fields.keys.any { it.contains("lyrics", ignoreCase = true) })
    }
}
