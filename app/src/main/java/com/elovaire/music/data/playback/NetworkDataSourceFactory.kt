package elovaire.music.droidbeauty.app.data.playback

import androidx.media3.common.util.UnstableApi
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.BaseDataSource
import elovaire.music.droidbeauty.app.data.library.network.NetworkFileSystemRegistry
import elovaire.music.droidbeauty.app.data.library.network.NetworkReadHandle
import elovaire.music.droidbeauty.app.data.library.network.NetworkReadPurpose
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import elovaire.music.droidbeauty.app.data.library.network.RemoteIoFailureKind
import elovaire.music.droidbeauty.app.data.library.network.NetworkRemoteIoException
import elovaire.music.droidbeauty.app.data.library.network.remoteIoFailureKind
import java.io.IOException

/** Routes only Elovaire's network URI scheme to the protocol adapters. */
@UnstableApi
internal class NetworkDataSourceFactory(
    private val defaultFactory: DefaultDataSource.Factory,
    private val registryProvider: () -> NetworkFileSystemRegistry,
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return RoutingDataSource(defaultFactory, registryProvider)
    }
}

@UnstableApi
private class RoutingDataSource(
    private val defaultFactory: DefaultDataSource.Factory,
    private val registryProvider: () -> NetworkFileSystemRegistry,
) : BaseDataSource(true) {
    private var delegate: DataSource? = null
    private var networkHandle: NetworkReadHandle? = null
    private var currentUri: android.net.Uri? = null
    private var networkTransferStarted = false

    override fun open(dataSpec: DataSpec): Long {
        close()
        currentUri = dataSpec.uri
        if (!NetworkResourceUri.isNetworkUri(dataSpec.uri)) {
            val local = defaultFactory.createDataSource()
            delegate = local
            return local.open(dataSpec)
        }
        transferInitializing(dataSpec)
        val sourceId = NetworkResourceUri.sourceId(dataSpec.uri)
            ?: throw IOException("Network media source is missing its source identity")
        val path = NetworkResourceUri.path(dataSpec.uri)
            ?: throw IOException("Network media source is missing its path")
        val handle = try {
            registryProvider().openBlocking(
                sourceId = sourceId,
                path = path,
                position = dataSpec.position,
                length = dataSpec.length,
                purpose = NetworkReadPurpose.Playback,
            )
        } catch (failure: NetworkRemoteIoException) {
            throw DataSourceException(
                failure,
                failure.media3ErrorCode(),
            )
        }
        networkHandle = handle
        transferStarted(dataSpec)
        networkTransferStarted = true
        return handle.length ?: C_LENGTH_UNKNOWN
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        networkHandle?.let { handle ->
            val read = try {
                handle.input.read(buffer, offset, length)
            } catch (failure: NetworkRemoteIoException) {
                throw DataSourceException(failure, failure.media3ErrorCode())
            } catch (failure: IOException) {
                val remoteFailure = NetworkRemoteIoException(
                    kind = failure.remoteIoFailureKind(),
                    message = "Network media read failed",
                    cause = failure,
                )
                throw DataSourceException(remoteFailure, remoteFailure.media3ErrorCode())
            }
            if (read > 0) bytesTransferred(read)
            return read
        }
        return delegate?.read(buffer, offset, length) ?: throw IOException("Data source is not open")
    }

    override fun getUri(): android.net.Uri? = delegate?.uri ?: currentUri

    override fun getResponseHeaders(): Map<String, List<String>> = delegate?.responseHeaders.orEmpty()

    override fun close() {
        val shouldEndTransfer = networkTransferStarted
        try {
            networkHandle?.close()
            delegate?.close()
        } finally {
            networkHandle = null
            delegate = null
            currentUri = null
            if (shouldEndTransfer) {
                networkTransferStarted = false
                transferEnded()
            }
        }
    }

    private companion object {
        const val C_LENGTH_UNKNOWN = -1L
    }
}

private fun NetworkRemoteIoException.media3ErrorCode(): Int = when (kind) {
    RemoteIoFailureKind.RangeOutOfBounds -> PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE
    RemoteIoFailureKind.Authentication,
    RemoteIoFailureKind.Permission,
    -> PlaybackException.ERROR_CODE_IO_NO_PERMISSION
    RemoteIoFailureKind.PathMissing,
    RemoteIoFailureKind.ShareMissing,
    RemoteIoFailureKind.SourceRemoved,
    -> PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
    RemoteIoFailureKind.Timeout -> PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
    RemoteIoFailureKind.HostUnreachable,
    RemoteIoFailureKind.ConnectionReset,
    RemoteIoFailureKind.TransientServer,
    -> PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
    RemoteIoFailureKind.RangeUnsupported,
    RemoteIoFailureKind.SessionInvalid,
    RemoteIoFailureKind.Protocol,
    RemoteIoFailureKind.Interrupted,
    RemoteIoFailureKind.Unknown,
    -> PlaybackException.ERROR_CODE_IO_UNSPECIFIED
}
