package elovaire.music.droidbeauty.app.widget

import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetUpdateRuntimeTest {
    @Test
    fun emptyRegistryDoesNoPersistenceOrPlaybackObservationWork() = runTest {
        val store = FakeStore()
        val reader = FakePlaybackReader()
        val runtime = WidgetUpdateRuntime(
            playbackReader = reader,
            snapshotStore = store,
            scope = backgroundScope,
            providers = WidgetProviderRegistry.productionProviders,
            clock = FixedClock,
        )

        runtime.onProviderLifecycleChanged()
        reader.transportState.value = PlaybackTransportState(isPlaying = true)
        advanceTimeBy(100L)

        assertEquals(0, store.readCount)
        assertEquals(0, store.writeCount)
        runtime.release()
    }

    @Test
    fun registeredProviderGetsOneCoalescedUpdateForPlaybackStateBurst() = runTest {
        val store = FakeStore()
        val reader = FakePlaybackReader()
        val provider = FakeProvider(instances = 1)
        val runtime = WidgetUpdateRuntime(
            playbackReader = reader,
            snapshotStore = store,
            scope = backgroundScope,
            providers = listOf(provider),
            clock = FixedClock,
        )

        runtime.onProviderLifecycleChanged()
        runCurrent()
        advanceTimeBy(50L)
        runCurrent()
        val initialUpdates = provider.updateCount
        val initialWrites = store.writeCount

        reader.transportState.value = PlaybackTransportState(isPlaying = true, transportShowsPause = true)
        reader.nowPlayingState.value = PlaybackNowPlayingState(sourceLabel = "invisible source change")
        reader.queueState.value = PlaybackQueueState(sourcePlaylistId = 12L)
        advanceTimeBy(50L)
        runCurrent()

        assertEquals(initialWrites + 1, store.writeCount)
        assertEquals(initialUpdates + 1, provider.updateCount)
        assertEquals(true, store.snapshot?.isPlaying)
        runtime.release()
    }

    @Test
    fun lastProviderInstanceStopsObservation() = runTest {
        val store = FakeStore()
        val reader = FakePlaybackReader()
        val provider = FakeProvider(instances = 1)
        val runtime = WidgetUpdateRuntime(
            playbackReader = reader,
            snapshotStore = store,
            scope = backgroundScope,
            providers = listOf(provider),
            clock = FixedClock,
        )
        runtime.onProviderLifecycleChanged()
        runCurrent()
        advanceTimeBy(50L)
        runCurrent()
        val writesWithInstance = store.writeCount

        provider.instances = 0
        runtime.onProviderLifecycleChanged()
        runCurrent()
        reader.transportState.value = PlaybackTransportState(isPlaying = true)
        advanceTimeBy(100L)
        runCurrent()

        assertEquals(writesWithInstance, store.writeCount)
        runtime.release()
    }

    private class FakePlaybackReader : PlaybackReader {
        override val nowPlayingState = MutableStateFlow(PlaybackNowPlayingState())
        override val transportState = MutableStateFlow(PlaybackTransportState())
        override val queueState = MutableStateFlow(PlaybackQueueState())
        override val volumeState = MutableStateFlow(PlaybackVolumeState())
        override val recentPlaybackState = MutableStateFlow(RecentPlaybackState())
    }

    private class FakeProvider(var instances: Int) : WidgetProviderUpdater {
        var updateCount = 0
        override suspend fun activeInstanceCount(): Int = instances
        override suspend fun updateAll() { updateCount += 1 }
    }

    private class FakeStore : WidgetSnapshotStore {
        var snapshot: WidgetPlaybackSnapshot? = null
        var readCount = 0
        var writeCount = 0

        override suspend fun readLatest(): WidgetPlaybackSnapshot? {
            readCount += 1
            return snapshot
        }

        override suspend fun writeIfChanged(snapshot: WidgetPlaybackSnapshot): Boolean {
            if (this.snapshot?.hasSameWidgetContent(snapshot) == true) return false
            this.snapshot = snapshot
            writeCount += 1
            return true
        }

        override suspend fun clear() {
            snapshot = null
        }
    }

    private object FixedClock : AppClock {
        override fun wallTimeMs(): Long = 1_000L
        override fun elapsedTimeMs(): Long = 2_000L
    }
}
