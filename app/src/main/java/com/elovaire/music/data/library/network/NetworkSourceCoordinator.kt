package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/** Serializes each source independently so late probes cannot publish obsolete state. */
internal class NetworkSourceCoordinator(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStoreProvider: () -> NetworkCredentialStore,
    private val registryProvider: () -> NetworkFileSystemRegistry,
    private val inventoryStore: NetworkInventoryStore,
) {
    private val mutationLocks = ConcurrentHashMap<String, Mutex>()

    private fun mutationLock(sourceId: String): Mutex =
        mutationLocks.computeIfAbsent(sourceId) { Mutex() }

    suspend fun save(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult = mutationLock(source.id).withLock {
        val credentialStore = credentialStoreProvider()
        val previousSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        val previous = credentialStore.get(previousSource?.credentialKey ?: source.credentialKey)
        val effectiveCredentials = if (credentials.password.isBlank() && previous != null) {
            previous.copy(
                username = credentials.username.ifBlank { previous.username },
                domain = credentials.domain ?: previous.domain,
            )
        } else {
            credentials
        }
        val normalized = sourceStore.upsert(source)
        if (previousSource != null && previousSource != normalized) {
            inventoryStore.remove(source.id)
        }
        credentialStore.put(normalized.credentialKey, effectiveCredentials)
        if (previousSource != null && previousSource.credentialKey != normalized.credentialKey) {
            credentialStore.remove(previousSource.credentialKey)
        }
        registryProvider().probeBlocking(normalized, effectiveCredentials)
    }

    suspend fun remove(source: NetworkLibrarySource) = mutationLock(source.id).withLock {
        val currentSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        sourceStore.remove(source.id)
        inventoryStore.remove(source.id)
        credentialStoreProvider().apply {
            remove(source.credentialKey)
            if (currentSource != null && currentSource.credentialKey != source.credentialKey) {
                remove(currentSource.credentialKey)
            }
        }
    }
}
