package elovaire.music.droidbeauty.app.widget

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetPlaybackActionDispatcherTest {
    @Test
    fun validActionIsDispatchedExactlyOnce() = runTest {
        val actions = mutableListOf<WidgetPlaybackAction>()
        val dispatcher = WidgetPlaybackActionDispatcher(WidgetPlaybackCommandGateway(actions::add))

        assertEquals(
            WidgetActionDispatchResult.Dispatched,
            dispatcher.dispatch(WidgetPlaybackAction.SkipNext.parameterValue),
        )
        assertEquals(listOf(WidgetPlaybackAction.SkipNext), actions)
    }

    @Test
    fun unknownActionDoesNotReachPlaybackGateway() = runTest {
        var calls = 0
        val dispatcher = WidgetPlaybackActionDispatcher(WidgetPlaybackCommandGateway { calls += 1 })

        assertEquals(WidgetActionDispatchResult.UnknownAction, dispatcher.dispatch("invalid"))
        assertEquals(0, calls)
    }

    @Test
    fun connectionTimeoutIsBounded() = runTest {
        val dispatcher = WidgetPlaybackActionDispatcher(
            gateway = WidgetPlaybackCommandGateway { awaitCancellation() },
            timeoutMs = 500L,
        )
        val result = async { dispatcher.dispatch(WidgetPlaybackAction.TogglePlayback.parameterValue) }

        runCurrent()
        advanceTimeBy(500L)
        runCurrent()

        assertEquals(WidgetActionDispatchResult.TimedOut, result.await())
    }

    @Test
    fun callerCancellationPropagatesToGateway() = runTest {
        var gatewayCancelled = false
        val dispatcher = WidgetPlaybackActionDispatcher(
            gateway = WidgetPlaybackCommandGateway {
                try {
                    awaitCancellation()
                } finally {
                    gatewayCancelled = true
                }
            },
        )
        val result = async { dispatcher.dispatch(WidgetPlaybackAction.SkipPrevious.parameterValue) }

        runCurrent()
        result.cancel()
        runCurrent()

        assertEquals(true, gatewayCancelled)
    }
}
