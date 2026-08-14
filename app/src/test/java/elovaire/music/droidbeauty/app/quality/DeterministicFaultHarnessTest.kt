package elovaire.music.droidbeauty.app.quality

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DeterministicFaultHarnessTest {
    @Test
    fun failOnCallLeavesLaterRetryAvailable() {
        val harness = DeterministicFaultHarness()
        var calls = 0
        harness.failOnCall("write", 2)

        assertEquals(1, harness.invoke("write") { ++calls })
        assertThrows(IllegalStateException::class.java) {
            harness.invoke("write") { ++calls }
        }
        assertEquals(2, harness.invoke("write") { ++calls })
    }

    @Test
    fun malformedRuleTerminatesDeterministically() {
        val harness = DeterministicFaultHarness()
        harness.malformed("parse")

        assertThrows(IllegalStateException::class.java) {
            harness.invoke("parse") { "valid" }
        }
    }
}

