package elovaire.music.droidbeauty.app.data.library.network

internal data class NetworkSourceMutationOutcome(
    val probeResult: NetworkProbeResult,
    val refreshRequired: Boolean,
)

internal interface NetworkSourceMutationBackend {
    suspend fun save(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkSourceMutationOutcome

    suspend fun remove(source: NetworkLibrarySource)
}

/** Applies one already-serialized source mutation while preserving journal ordering. */
internal class NetworkSourceCoordinator(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStoreProvider: () -> NetworkCredentialStore,
    private val registryProvider: () -> NetworkFileSystemRegistry,
    private val inventoryStore: NetworkInventoryStore,
    private val mutationJournal: NetworkSourceMutationJournal,
) : NetworkSourceMutationBackend {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun save(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkSourceMutationOutcome {
        val credentialStore = credentialStoreProvider()
        val previousSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        val previousResult = credentialStore.read(
            sourceId = source.id,
            key = previousSource?.credentialKey ?: source.credentialKey,
        )
        val previous = when (previousResult) {
            NetworkCredentialReadResult.Missing -> null
            is NetworkCredentialReadResult.Available -> previousResult.credentials
            NetworkCredentialReadResult.KeyUnavailable,
            is NetworkCredentialReadResult.Corrupt,
            -> if (credentials.password.isBlank()) {
                throw IllegalStateException("Stored network credentials are unavailable; enter the password again")
            } else {
                null
            }
        }
        val effectiveCredentials = if (credentials.password.isBlank() && previous != null) {
            previous.copy(
                username = credentials.username.ifBlank { previous.username },
                domain = credentials.domain ?: previous.domain,
            )
        } else {
            credentials
        }
        val normalizedInput = sourceStore.normalized(source)
        val normalized = normalizedInput.copy(username = effectiveCredentials.username.trim())
        mutationJournal.prepareSave(
            sourceId = normalized.id,
            previousCredentialKey = previousSource?.credentialKey,
            newCredentialKey = normalized.credentialKey,
            previousLocationFingerprint = previousSource?.let(NetworkSourceIdentity::locationFingerprint),
            newLocationFingerprint = NetworkSourceIdentity.locationFingerprint(normalized),
        )
        credentialStore.put(normalized.id, normalized.credentialKey, effectiveCredentials)
        mutationJournal.markPhase(normalized.id, NetworkSourceMutationPhase.CredentialPersisted)
        try {
            sourceStore.upsert(normalized)
        } catch (failure: RuntimeException) {
            val rollbackComplete = runCatching {
                if (previous != null && previousSource?.credentialKey == normalized.credentialKey) {
                    credentialStore.put(normalized.id, normalized.credentialKey, previous)
                } else if (sourceStore.sources.value.none {
                    it.id != normalized.id && it.credentialKey == normalized.credentialKey
                }) {
                    credentialStore.remove(normalized.credentialKey)
                }
            }.isSuccess
            if (rollbackComplete) {
                runCatching { mutationJournal.clear(normalized.id) }
            }
            throw failure
        }
        mutationJournal.markPhase(normalized.id, NetworkSourceMutationPhase.SourcePersisted)
        if (previousSource != null && previousSource.credentialKey != normalized.credentialKey) {
            credentialStore.remove(previousSource.credentialKey)
        }
        mutationJournal.markPhase(normalized.id, NetworkSourceMutationPhase.CredentialsCleaned)
        if (previousSource != null && previousSource != normalized) {
            inventoryStore.remove(source.id)
        }
        mutationJournal.markPhase(normalized.id, NetworkSourceMutationPhase.InventoryInvalidated)
        if (previousSource != normalized || previous != effectiveCredentials) {
            registryProvider().invalidate(source.id)
        }
        mutationJournal.markPhase(normalized.id, NetworkSourceMutationPhase.RuntimeInvalidated)
        val outcome = NetworkSourceMutationOutcome(
            probeResult = registryProvider().probeBlocking(normalized, effectiveCredentials),
            refreshRequired = previousSource != normalized || previous != effectiveCredentials,
        )
        mutationJournal.clear(normalized.id)
        return outcome
    }

    override suspend fun remove(source: NetworkLibrarySource) {
        val currentSource = sourceStore.sources.value.firstOrNull { it.id == source.id }
        mutationJournal.prepareRemove(currentSource ?: source)
        registryProvider().invalidate(source.id)
        mutationJournal.markPhase(source.id, NetworkSourceMutationPhase.RuntimeInvalidated)
        sourceStore.remove(source.id)
        mutationJournal.markPhase(source.id, NetworkSourceMutationPhase.SourceRemoved)
        inventoryStore.remove(source.id)
        mutationJournal.markPhase(source.id, NetworkSourceMutationPhase.InventoryRemoved)
        credentialStoreProvider().apply {
            remove(source.credentialKey)
            if (currentSource != null && currentSource.credentialKey != source.credentialKey) {
                remove(currentSource.credentialKey)
            }
        }
        mutationJournal.clear(source.id)
    }
}
