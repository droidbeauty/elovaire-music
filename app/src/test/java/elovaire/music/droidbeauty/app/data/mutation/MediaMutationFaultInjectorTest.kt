package elovaire.music.droidbeauty.app.data.mutation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MediaMutationFaultInjectorTest {
    @Test
    fun everyDurabilityBoundaryCanBeInjectedDeterministically() {
        val observed = mutableListOf<MediaMutationTransactionPhase>()
        val injector = MediaMutationFaultInjector { observed += it }

        MediaMutationTransactionPhase.entries.forEach(injector::checkpoint)

        assertEquals(MediaMutationTransactionPhase.entries.toList(), observed)
    }

    @Test
    fun aSelectedBoundaryFailsEveryTimeItIsReached() {
        MediaMutationTransactionPhase.entries.forEach { selectedPhase ->
            val injector = MediaMutationFaultInjector { phase ->
                if (phase == selectedPhase) throw IllegalStateException("injected:$phase")
            }

            assertThrows(IllegalStateException::class.java) {
                injector.checkpoint(selectedPhase)
            }
        }
    }
}
