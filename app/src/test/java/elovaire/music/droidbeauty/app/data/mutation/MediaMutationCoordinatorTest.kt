package elovaire.music.droidbeauty.app.data.mutation

import java.util.Collections
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMutationCoordinatorTest {
    @Test
    fun sameTargetIsSerialized() {
        runBlocking {
        val target = android.net.TestUri("file:///music/song.mp3")
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()

        val first = async {
            MediaMutationCoordinator.withTarget(target) {
                entered.complete(Unit)
                release.await()
            }
        }
        entered.await()
        val second = async {
            MediaMutationCoordinator.withTarget(target) {
                secondEntered.complete(Unit)
            }
        }

        yield()
        assertFalse(secondEntered.isCompleted)
        release.complete(Unit)
        first.await()
        second.await()
        assertTrue(secondEntered.isCompleted)
        }
    }

    @Test
    fun independentTargetsCanProceedTogether() {
        runBlocking {
        val firstEntered = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val events = Collections.synchronizedList(mutableListOf<String>())

        val first = async {
            MediaMutationCoordinator.withTarget(android.net.TestUri("file:///music/a.mp3")) {
                events += "a"
                firstEntered.complete(Unit)
                release.await()
            }
        }
        firstEntered.await()
        val second = async {
            MediaMutationCoordinator.withTarget(android.net.TestUri("file:///music/b.mp3")) {
                events += "b"
                secondEntered.complete(Unit)
            }
        }

        secondEntered.await()
        assertTrue(events.containsAll(listOf("a", "b")))
        release.complete(Unit)
        first.await()
        second.await()
        }
    }

    @Test
    fun targetLockRegistryIsReclaimedAfterIndependentMutations() = runBlocking {
        repeat(256) { index ->
            MediaMutationCoordinator.withTarget(android.net.TestUri("file:///music/$index.mp3")) { }
        }

        assertEquals(0, MediaMutationCoordinator.activeTargetLockCount())
    }
}
