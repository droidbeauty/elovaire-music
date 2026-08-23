package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.TransferListener
import elovaire.music.droidbeauty.app.data.library.network.NetworkFileSystemRegistry
import elovaire.music.droidbeauty.app.data.library.network.NetworkReadHandle
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import java.io.IOException

/** Routes only Elovaire's network URI scheme to the protocol adapters. */
@UnstableApi
internal class NetworkDataSourceFactory(
    private val defaultFactory: DefaultDataSource.Factory,
    private val registry: NetworkFileSystemRegistry,
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return RoutingDataSource(defaultFactory, registry)
    }
}

@UnstableApi
private class RoutingDataSource(
    private val defaultFactory: DefaultDataSource.Factory,
    private val registry: NetworkFileSystemRegistry,
) : DataSource {
    private var delegate: DataSource? = null
    private var networkHandle: NetworkReadHandle? = null
    private var currentUri: android.net.Uri? = null
    private val listeners = mutableListOf<TransferListener>()

    override fun addTransferListener(transferListener: TransferListener) {
        listeners += transferListener
        delegate?.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        close()
        currentUri = dataSpec.uri
        if (!NetworkResourceUri.isNetworkUri(dataSpec.uri)) {
            val local = defaultFactory.createDataSource()
            listeners.forEach(local::addTransferListener)
            delegate = local
            return local.open(dataSpec)
        }
        val sourceId = NetworkResourceUri.sourceId(dataSpec.uri)
            ?: throw IOException("Network media source is missing its source identity")
        val path = NetworkResourceUri.path(dataSpec.uri)
            ?: throw IOException("Network media source is missing its path")
        val handle = registry.openBlocking(
            sourceId = sourceId,
            path = path,
            position = dataSpec.position,
            length = dataSpec.length,
        )
        networkHandle = handle
        return handle.length ?: C_LENGTH_UNKNOWN
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        networkHandle?.let { handle -> return handle.input.read(buffer, offset, length) }
        return delegate?.read(buffer, offset, length) ?: throw IOException("Data source is not open")
    }

    override fun getUri(): android.net.Uri? = delegate?.uri ?: currentUri

    override fun getResponseHeaders(): Map<String, List<String>> = delegate?.responseHeaders.orEmpty()

    override fun close() {
        networkHandle?.close()
        networkHandle = null
        delegate?.close()
        delegate = null
        currentUri = null
    }

    private companion object {
        const val C_LENGTH_UNKNOWN = -1L
    }
}
