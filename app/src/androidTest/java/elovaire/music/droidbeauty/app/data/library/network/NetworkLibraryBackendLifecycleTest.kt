package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.library.LibraryNetworkController
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkLibraryBackendLifecycleTest {
    @Test
    fun queuedOperationsRecheckPermissionBeforeUsingTransport() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allowed = AtomicBoolean(true)
        val transportCalls = AtomicInteger()
        val source = NetworkLibrarySource("queued-permission", "Test", NetworkLibraryProtocol.Smb,
            "nas.example", "Music", "user", "queued-permission")
        val credentials = NetworkCredentials("user", "secret")
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
                transportCalls.incrementAndGet()
                return NetworkProbeResult(NetworkAvailability.Available)
            }
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int): NetworkListingResult {
                transportCalls.incrementAndGet()
                error("Transport should not be called")
            }
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long) = error("Not used")
        }
        val registry = NetworkFileSystemRegistry(NetworkLibrarySourceStore(context), NetworkCredentialStore(context),
            mapOf(NetworkLibraryProtocol.Smb to fileSystem), localNetworkAccessAllowed = allowed::get, applicationContext = context)
        val admission = NetworkFileSystemRegistry::class.java.getDeclaredField("operationAdmission")
            .apply { isAccessible = true }.get(registry) as NetworkOperationAdmission
        try {
            for (probe in listOf(true, false)) {
                allowed.set(true)
                val permits = List(3) { admission.acquire(NetworkReadPurpose.Listing) }
                val result = AtomicReference<Any>()
                val worker = Thread {
                    try {
                        result.set(if (probe) registry.probeBlocking(source, credentials) else registry.listBlocking(source, credentials))
                    } catch (failure: Exception) {
                        result.set(failure)
                    }
                }
                try {
                    worker.start()
                    val deadline = System.nanoTime() + 5_000_000_000L
                    while (admission.snapshot().waitingBackground == 0 && System.nanoTime() < deadline) Thread.yield()
                    assertEquals(1, admission.snapshot().waitingBackground)
                    allowed.set(false)
                    permits.forEach { it.close() }
                    worker.join(5_000)
                    assertFalse(worker.isAlive)
                    if (probe) {
                        assertEquals(NetworkAvailability.LocalNetworkPermissionRequired, (result.get() as NetworkProbeResult).availability)
                    } else {
                        assertTrue(result.get() is NetworkLocalNetworkPermissionException)
                    }
                    assertEquals(0, transportCalls.get())
                } finally {
                    permits.forEach { it.close() }
                    worker.join(5_000)
                }
            }
        } finally {
            registry.release()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun permissionChangesAreEnforcedBeforeConnectivityObserverStarts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var allowed = true
        var probes = 0
        val source = NetworkLibrarySource("permission-test", "Test", NetworkLibraryProtocol.Smb,
            "nas.example", "Music", "user", "permission-test")
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
                probes++
                return NetworkProbeResult(NetworkAvailability.Available)
            }
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) = error("Not used")
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long) = error("Not used")
        }
        val registry = NetworkFileSystemRegistry(NetworkLibrarySourceStore(context), NetworkCredentialStore(context),
            mapOf(NetworkLibraryProtocol.Smb to fileSystem), localNetworkAccessAllowed = { allowed }, applicationContext = context)
        try {
            val credentials = NetworkCredentials("user", "secret")
            allowed = false
            assertEquals(NetworkAvailability.LocalNetworkPermissionRequired, registry.probeBlocking(source, credentials).availability)
            assertEquals(0, probes)
            val revokedGeneration = registry.networkGeneration()
            allowed = true
            assertEquals(NetworkAvailability.Available, registry.probeBlocking(source, credentials).availability)
            assertEquals(1, probes)
            assertTrue(registry.networkGeneration() > revokedGeneration)
            val beforeCallback = registry.networkGeneration()
            val observer = checkNotNull(NetworkFileSystemRegistry::class.java.getDeclaredField("connectivityObserver")
                .apply { isAccessible = true }.get(registry))
            val callback = checkNotNull(NetworkConnectivityObserver::class.java.getDeclaredField("onStateChanged")
                .apply { isAccessible = true }.get(observer)) as (NetworkConnectivityState) -> Unit
            callback(NetworkConnectivityState(networkAvailable = true, generation = 1))
            assertTrue(registry.networkGeneration() > beforeCallback)
        } finally {
            registry.release()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun releaseWaitsForAnAcceptedConnectivityInvalidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val released = CountDownLatch(1)
        val fileSystem = object : NetworkFileSystem {
            override fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials) = error("Not used")
            override fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, maxEntries: Int, maxDepth: Int) = error("Not used")
            override fun openBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials, path: String, position: Long, length: Long) = error("Not used")
            override fun invalidateAll() {
                entered.countDown()
                check(finish.await(5, TimeUnit.SECONDS))
            }
            override fun release() { released.countDown() }
        }
        val registry = NetworkFileSystemRegistry(NetworkLibrarySourceStore(context), NetworkCredentialStore(context),
            mapOf(NetworkLibraryProtocol.Smb to fileSystem), applicationContext = context)
        val observer = checkNotNull(NetworkFileSystemRegistry::class.java.getDeclaredField("connectivityObserver")
            .apply { isAccessible = true }.get(registry))
        val callback = checkNotNull(NetworkConnectivityObserver::class.java.getDeclaredField("onStateChanged")
            .apply { isAccessible = true }.get(observer)) as (NetworkConnectivityState) -> Unit
        val invalidating = Thread { callback(NetworkConnectivityState(networkAvailable = true, generation = 1)) }
        val closing = Thread { registry.release() }
        try {
            invalidating.start()
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            closing.start()
            val deadline = System.nanoTime() + 5_000_000_000L
            while (closing.state != Thread.State.BLOCKED && released.count != 0L && System.nanoTime() < deadline) Thread.yield()
            assertEquals(Thread.State.BLOCKED, closing.state)
            assertEquals(1L, released.count)
            finish.countDown()
            invalidating.join(5_000)
            closing.join(5_000)
            assertTrue(released.await(5, TimeUnit.SECONDS))
            assertFalse(invalidating.isAlive)
            assertFalse(closing.isAlive)
        } finally {
            finish.countDown()
            invalidating.join(5_000)
            closing.join(5_000)
            registry.release()
        }
    }

    @Test
    fun releasedRegistryDoesNotRestartItsConnectivityObserver() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val registry = NetworkFileSystemRegistry(NetworkLibrarySourceStore(context), NetworkCredentialStore(context),
            emptyMap(), applicationContext = context)
        val observer = checkNotNull(NetworkFileSystemRegistry::class.java.getDeclaredField("connectivityObserver")
            .apply { isAccessible = true }.get(registry)) as NetworkConnectivityObserver
        val lifecycle = checkNotNull(NetworkConnectivityObserver::class.java.getDeclaredField("lifecycle")
            .apply { isAccessible = true }.get(observer)) as java.util.concurrent.atomic.AtomicBoolean
        try {
            registry.start()
            registry.release()
            registry.start()
            assertFalse(lifecycle.get())
        } finally {
            observer.close()
            registry.release()
        }
    }

    @Test
    fun closeWinsAgainstAStartAlreadyWaitingForTheLifecycleLock() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sources = NetworkLibrarySourceStore(context)
        val source = NetworkLibrarySource(UUID.randomUUID().toString(), "Test", NetworkLibraryProtocol.Smb,
            "nas.example", "Music", "user", UUID.randomUUID().toString())
        val credentials = NetworkCredentialStore(context)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val calls = AtomicInteger()
        val registries = mutableListOf<NetworkFileSystemRegistry>()
        val backend = NetworkLibraryBackend(scope, sources, {
            calls.incrementAndGet()
            NetworkFileSystemRegistry(sources, credentials, emptyMap()).also(registries::add)
        }, object : NetworkSourceMutationBackend {
            override suspend fun save(source: NetworkLibrarySource, credentials: NetworkCredentials) =
                error("Not used")
            override suspend fun remove(source: NetworkLibrarySource) = Unit
        }, object : LibraryNetworkController {
            override fun unblockNetworkSource(sourceId: String) = Unit
            override fun setNetworkSources(sources: List<NetworkLibrarySource>, enrichMetadata: Boolean,
                showLoadingIndicator: Boolean, forceRefreshSourceIds: Set<String>) = Unit
        }, Dispatchers.Unconfined)
        try {
            sources.upsert(source)
            val lock = checkNotNull(NetworkLibraryBackend::class.java.getDeclaredField("servicesLock")
                .apply { isAccessible = true }.get(backend))
            val starting = Thread { backend.start() }
            val closing = Thread { backend.close() }
            fun awaitBlocked(thread: Thread) {
                val deadline = System.nanoTime() + 5_000_000_000L
                while (thread.state != Thread.State.BLOCKED && System.nanoTime() < deadline) Thread.yield()
                assertEquals(Thread.State.BLOCKED, thread.state)
            }
            synchronized(lock) {
                starting.start()
                awaitBlocked(starting)
                closing.start()
                awaitBlocked(closing)
            }
            starting.join(5_000)
            closing.join(5_000)
            assertFalse(starting.isAlive)
            assertFalse(closing.isAlive)
            assertEquals(0, calls.get())
        } finally {
            backend.close()
            registries.forEach(NetworkFileSystemRegistry::release)
            scope.cancel()
            sources.remove(source.id)
        }
    }
}
