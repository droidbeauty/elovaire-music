package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes source configuration mutations so late probes cannot publish obsolete state. */
internal class NetworkSourceCoordinator(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStoreProvider: () -> NetworkCredentialStore,
    private val registryProvider: () -> NetworkFileSystemRegistry,
    private val inventoryStore: NetworkInventoryStore,
) {
    private val mutationLock = Mutex()

    suspend fun save(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult = mutationLock.withLock {
        val credentialStore = credentialStoreProvider()
        val previous = credentialStore.get(source.credentialKey)
        val effectiveCredentials = if (credentials.password.isBlank() && previous != null) {
            previous.copy(
                username = credentials.username.ifBlank { previous.username },
                domain = credentials.domain ?: previous.domain,
            )
        } else {
            credentials
        }
        val previousSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        val normalized = sourceStore.upsert(source)
        if (previousSource != null && previousSource != normalized) {
            inventoryStore.remove(source.id)
        }
        credentialStore.put(normalized.credentialKey, effectiveCredentials)
        registryProvider().probeBlocking(normalized, effectiveCredentials)
    }

    suspend fun remove(source: NetworkLibrarySource) = mutationLock.withLock {
        sourceStore.remove(source.id)
        inventoryStore.remove(source.id)
        credentialStoreProvider().remove(source.credentialKey)
    }
}
