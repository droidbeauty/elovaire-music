package elovaire.music.droidbeauty.app.data.library.network

import java.io.IOException

internal sealed interface NetworkListingResult {
    val entries: List<NetworkFileEntry>

    data class Complete(override val entries: List<NetworkFileEntry>) : NetworkListingResult

    data class Incomplete(
        override val entries: List<NetworkFileEntry>,
        val reason: String,
    ) : NetworkListingResult
}

internal interface NetworkFileSystem {
    fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult

    fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int = 10_000,
        maxDepth: Int = 12,
    ): NetworkListingResult

    fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle

    fun invalidate(sourceId: String) = Unit

    fun release() = Unit
}

internal class NetworkFileSystemRegistry(
    private val sourceStore: NetworkLibrarySourceStore,
    private val credentialStore: NetworkCredentialStore,
    private val fileSystems: Map<NetworkLibraryProtocol, NetworkFileSystem>,
) {
    fun source(sourceId: String): NetworkLibrarySource? = sourceStore.sources.value.firstOrNull { it.id == sourceId }

    fun sourceGeneration(sourceId: String): Long = sourceStore.generation(sourceId)

    fun isCurrent(source: NetworkLibrarySource, generation: Long): Boolean =
        sourceStore.isCurrent(source, generation)

    fun credentials(source: NetworkLibrarySource): NetworkCredentials? = credentialStore.get(source.credentialKey)

    fun probeBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkProbeResult {
        return fileSystems[source.protocol]?.probeBlocking(source, credentials)
            ?: NetworkProbeResult(NetworkAvailability.Misconfigured, "Protocol is unavailable")
    }

    fun listBlocking(source: NetworkLibrarySource, credentials: NetworkCredentials): NetworkListingResult {
        return fileSystems[source.protocol]?.listBlocking(source, credentials)
            ?: throw IOException("Network protocol is unavailable")
    }

    fun openBlocking(
        sourceId: String,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        val source = source(sourceId) ?: throw IOException("Network library source is unavailable")
        val credentials = credentials(source) ?: throw IOException("Network library credentials are unavailable")
        return fileSystems[source.protocol]?.openBlocking(source, credentials, path, position, length)
            ?: throw IOException("Network protocol is unavailable")
    }

    fun invalidate(sourceId: String) {
        fileSystems.values.forEach { it.invalidate(sourceId) }
    }

    fun release() {
        fileSystems.values.forEach(NetworkFileSystem::release)
    }
}
