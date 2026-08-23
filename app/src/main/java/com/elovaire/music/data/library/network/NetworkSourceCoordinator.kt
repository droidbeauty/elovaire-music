package elovaire.music.droidbeauty.app.data.library.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes each source independently so late probes cannot publish obsolete state. */
internal class NetworkSourceCoordinator(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStoreProvider: () -> NetworkCredentialStore,
    private val registryProvider: () -> NetworkFileSystemRegistry,
    private val inventoryStore: NetworkInventoryStore,
) {
    private val mutationLockRegistry = Any()
    private val mutationLocks = mutableMapOf<String, MutationLock>()

    private suspend fun <T> withSourceLock(sourceId: String, block: suspend () -> T): T {
        val entry = synchronized(mutationLockRegistry) {
            mutationLocks.getOrPut(sourceId) { MutationLock() }.also { it.users += 1 }
        }
        return try {
            entry.mutex.withLock { block() }
        } finally {
            synchronized(mutationLockRegistry) {
                entry.users -= 1
                if (entry.users == 0 && mutationLocks[sourceId] === entry) {
                    mutationLocks.remove(sourceId)
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun save(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult = withSourceLock(source.id) {
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
        val normalized = sourceStore.normalized(source)
        credentialStore.put(normalized.credentialKey, effectiveCredentials)
        try {
            sourceStore.upsert(normalized)
        } catch (failure: RuntimeException) {
            runCatching {
                if (previous != null && previousSource?.credentialKey == normalized.credentialKey) {
                    credentialStore.put(normalized.credentialKey, previous)
                } else {
                    credentialStore.remove(normalized.credentialKey)
                }
            }
            throw failure
        }
        if (previousSource != null && previousSource.credentialKey != normalized.credentialKey) {
            credentialStore.remove(previousSource.credentialKey)
        }
        if (previousSource != null && previousSource != normalized) {
            inventoryStore.remove(source.id)
        }
        if (previousSource != normalized || previous != effectiveCredentials) {
            registryProvider().invalidate(source.id)
        }
        registryProvider().probeBlocking(normalized, effectiveCredentials)
    }

    suspend fun remove(source: NetworkLibrarySource) = withSourceLock(source.id) {
        val currentSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        registryProvider().invalidate(source.id)
        sourceStore.remove(source.id)
        inventoryStore.remove(source.id)
        credentialStoreProvider().apply {
            remove(source.credentialKey)
            if (currentSource != null && currentSource.credentialKey != source.credentialKey) {
                remove(currentSource.credentialKey)
            }
        }
    }

    private class MutationLock {
        val mutex = Mutex()
        var users: Int = 0
    }
}
