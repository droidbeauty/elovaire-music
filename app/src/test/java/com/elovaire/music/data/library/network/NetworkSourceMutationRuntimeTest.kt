package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkSourceMutationRuntimeTest {
    @Test
    fun saveEmitsOneTypedCurrentGenerationOutcome() = runTest {
        val source = source()
        val results = mutableListOf<NetworkSourceMutationResult>()
        val runtime = NetworkSourceMutationRuntime(
            scope = this,
            coordinator = object : NetworkSourceMutationBackend {
                override suspend fun save(
                    source: NetworkLibrarySource,
                    credentials: NetworkCredentials,
                ) = NetworkSourceMutationOutcome(
                    probeResult = NetworkProbeResult(NetworkAvailability.Available),
                    refreshRequired = true,
                )

                override suspend fun remove(source: NetworkLibrarySource) = Unit
            },
            onResult = results::add,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runtime.save(source, NetworkCredentials("user", "password"))
        advanceUntilIdle()

        assertEquals(
            listOf(
                NetworkSourceMutationResult.Checking(source.id),
                NetworkSourceMutationResult.Saved(
                    sourceId = source.id,
                    probeResult = NetworkProbeResult(NetworkAvailability.Available),
                    refreshRequired = true,
                ),
            ),
            results,
        )
        runtime.release()
    }

    @Test
    fun failedMutationPreservesFailureOutcomeWithoutPublishingAChange() = runTest {
        val source = source()
        val results = mutableListOf<NetworkSourceMutationResult>()
        val runtime = NetworkSourceMutationRuntime(
            scope = this,
            coordinator = object : NetworkSourceMutationBackend {
                override suspend fun save(
                    source: NetworkLibrarySource,
                    credentials: NetworkCredentials,
                ): NetworkSourceMutationOutcome = error("not available")

                override suspend fun remove(source: NetworkLibrarySource) = Unit
            },
            onResult = results::add,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runtime.save(source, NetworkCredentials("user", "password"))
        advanceUntilIdle()

        assertEquals(source.id, results.first().sourceId)
        assertEquals(NetworkSourceMutationResult.Checking(source.id), results.first())
        assertEquals(
            NetworkSourceMutationResult.Failed(source.id, "IllegalStateException"),
            results.last(),
        )
        runtime.release()
    }

    @Test
    fun newerMutationSuppressesOlderTypedOutcome() = runTest {
        val source = source()
        val first = CompletableDeferred<NetworkSourceMutationOutcome>()
        val second = CompletableDeferred<NetworkSourceMutationOutcome>()
        val results = mutableListOf<NetworkSourceMutationResult>()
        var saveCount = 0
        val runtime = NetworkSourceMutationRuntime(
            scope = this,
            coordinator = object : NetworkSourceMutationBackend {
                override suspend fun save(
                    source: NetworkLibrarySource,
                    credentials: NetworkCredentials,
                ): NetworkSourceMutationOutcome {
                    return if (saveCount++ == 0) first.await() else second.await()
                }

                override suspend fun remove(source: NetworkLibrarySource) = Unit
            },
            onResult = results::add,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runtime.save(source, NetworkCredentials("user-a", "password"))
        advanceUntilIdle()
        runtime.save(source, NetworkCredentials("user-b", "password"))
        second.complete(
            NetworkSourceMutationOutcome(
                probeResult = NetworkProbeResult(NetworkAvailability.Available, "new"),
                refreshRequired = true,
            ),
        )
        advanceUntilIdle()
        first.complete(
            NetworkSourceMutationOutcome(
                probeResult = NetworkProbeResult(NetworkAvailability.Available, "old"),
                refreshRequired = false,
            ),
        )
        advanceUntilIdle()

        assertEquals(2, saveCount)
        assertEquals(2, results.count { it is NetworkSourceMutationResult.Checking })
        assertEquals(
            NetworkSourceMutationResult.Saved(
                sourceId = source.id,
                probeResult = NetworkProbeResult(NetworkAvailability.Available, "new"),
                refreshRequired = true,
            ),
            results.last(),
        )
        assert(results.none { result ->
            result is NetworkSourceMutationResult.Saved && result.probeResult.message == "old"
        })
        runtime.release()
    }

    @Test
    fun consumerFailureIsNotReclassifiedAsMutationFailure() = runTest {
        val source = source()
        val results = mutableListOf<NetworkSourceMutationResult>()
        val failures = mutableListOf<Throwable>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, failure ->
                failures += failure
            },
        )
        val runtime = NetworkSourceMutationRuntime(
            scope = scope,
            coordinator = object : NetworkSourceMutationBackend {
                override suspend fun save(
                    source: NetworkLibrarySource,
                    credentials: NetworkCredentials,
                ) = NetworkSourceMutationOutcome(
                    probeResult = NetworkProbeResult(NetworkAvailability.Available),
                    refreshRequired = false,
                )

                override suspend fun remove(source: NetworkLibrarySource) = Unit
            },
            onResult = { result ->
                results += result
                if (result is NetworkSourceMutationResult.Saved) error("consumer failure")
            },
            ioDispatcher = dispatcher,
        )

        runtime.save(source, NetworkCredentials("user", "password"))
        advanceUntilIdle()

        assertEquals(2, results.size)
        assertEquals(1, failures.size)
        assertEquals("consumer failure", failures.single().message)
        runtime.release()
        scope.cancel()
    }

    private fun source() = NetworkLibrarySource(
        id = "source-a",
        name = "Source A",
        protocol = NetworkLibraryProtocol.Smb,
        server = "server",
        shareOrPath = "share",
        username = "user",
        credentialKey = "key",
    )
}
