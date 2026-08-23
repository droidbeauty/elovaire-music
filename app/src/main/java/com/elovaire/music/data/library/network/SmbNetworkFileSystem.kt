package elovaire.music.droidbeauty.app.data.library.network

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import java.io.IOException
import java.io.InputStream
import java.util.EnumSet
import java.util.concurrent.TimeUnit

internal class SmbNetworkFileSystem : NetworkFileSystem {
    override fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult {
        return runCatching {
            withShare(source, credentials) { share ->
                share.list(smbPath(source)).size
            }
            NetworkProbeResult(NetworkAvailability.Available)
        }.getOrElse { failure ->
            NetworkProbeResult(
                availability = if (failure.message?.contains("STATUS_LOGON_FAILURE") == true) {
                    NetworkAvailability.AuthenticationRequired
                } else {
                    NetworkAvailability.Unavailable
                },
                message = failure::class.simpleName,
            )
        }
    }

    override fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int,
        maxDepth: Int,
    ): List<NetworkFileEntry> = withShare(source, credentials) { share ->
        val entries = ArrayList<NetworkFileEntry>()
        val pending = ArrayDeque<Pair<String, Int>>()
        val rootPath = smbPath(source)
        pending.addLast(rootPath to 0)
        while (pending.isNotEmpty() && entries.size < maxEntries) {
            val (directory, depth) = pending.removeFirst()
            share.list(directory).forEach { item ->
                if (entries.size >= maxEntries) return@forEach
                val name = item.fileName ?: return@forEach
                if (name == "." || name == "..") return@forEach
                val fullPath = NetworkPathPolicy.join(directory, name)
                val directoryEntry = item.isDirectory()
                val entry = NetworkFileEntry(
                    path = fullPath.removeRootPath(rootPath),
                    isDirectory = directoryEntry,
                    sizeBytes = item.endOfFile.takeIf { it >= 0L },
                )
                entries += entry
                if (directoryEntry && depth < maxDepth) pending.addLast(fullPath to depth + 1)
            }
        }
        entries
    }

    override fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        val host = NetworkPathPolicy.smbServer(source.server) ?: throw IOException("SMB server is invalid")
        val shareName = NetworkPathPolicy.smbShareAndPath(source.shareOrPath)?.first
            ?: throw IOException("SMB share is missing")
        val client = SMBClient(SMB_CONFIG)
        val resources = mutableListOf<AutoCloseable>(client)
        var handleCreated = false
        return try {
            val connection = client.connect(host, NetworkPathPolicy.smbPort(source.server))
            resources += connection
            val session = connection.authenticate(
                AuthenticationContext(
                    credentials.username,
                    credentials.password.toCharArray(),
                    credentials.domain,
                ),
            )
            resources += session
            val share = session.connectShare(shareName) as? DiskShare
                ?: throw IOException("SMB share is not a disk share")
            resources += share
            val file = share.openFile(
                NetworkPathPolicy.join(smbPath(source), path),
                EnumSet.of(AccessMask.FILE_READ_DATA),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE),
            )
            resources += file
            val totalLength = file.getFileInformation(
                com.hierynomus.msfscc.fileinformation.FileStandardInformation::class.java,
            ).endOfFile
            val start = position.coerceIn(0L, totalLength)
            val available = (totalLength - start).coerceAtLeast(0L)
            val requested = length.takeIf { it > 0L }?.coerceAtMost(available) ?: available
            NetworkReadHandle(
                input = SmbRangeInputStream(file, start, requested, totalLength),
                length = requested,
                closeHandle = {
                    resources.asReversed().forEach { resource -> runCatching { resource.close() } }
                },
            ).also { handleCreated = true }
        } finally {
            if (!handleCreated) {
                resources.asReversed().forEach { resource -> runCatching { resource.close() } }
            }
        }
    }

    private fun smbPath(source: NetworkLibrarySource): String {
        return NetworkPathPolicy.smbShareAndPath(source.shareOrPath)?.second.orEmpty()
    }

    private fun String.removeRootPath(rootPath: String): String {
        if (rootPath.isBlank()) return this
        return removePrefix(rootPath).trimStart('/').ifBlank { this }
    }

    private fun <T> withShare(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        block: (DiskShare) -> T,
    ): T = withShareHandle(source, credentials) { share, _ -> block(share) }

    private fun <T> withShareHandle(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        block: (DiskShare, MutableList<AutoCloseable>) -> T,
    ): T {
        val host = NetworkPathPolicy.smbServer(source.server) ?: throw IOException("SMB server is invalid")
        val shareName = NetworkPathPolicy.smbShareAndPath(source.shareOrPath)?.first
            ?: throw IOException("SMB share is missing")
        val client = SMBClient(SMB_CONFIG)
        val resources = mutableListOf<AutoCloseable>(client)
        return try {
            val connection = client.connect(host, NetworkPathPolicy.smbPort(source.server))
            resources += connection
            val session = connection.authenticate(
                AuthenticationContext(
                    credentials.username,
                    credentials.password.toCharArray(),
                    credentials.domain,
                ),
            )
            resources += session
            val share = session.connectShare(shareName) as? DiskShare
                ?: throw IOException("SMB share is not a disk share")
            resources += share
            block(share, resources)
        } finally {
            resources.asReversed().forEach { resource -> runCatching { resource.close() } }
        }
    }

    private fun FileIdBothDirectoryInformation.isDirectory(): Boolean {
        return fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L
    }

    private class SmbRangeInputStream(
        private val file: com.hierynomus.smbj.share.File,
        private val start: Long,
        private val requestedLength: Long?,
        private val totalLength: Long,
    ) : InputStream() {
        private var position = 0L

        override fun read(): Int {
            val one = ByteArray(1)
            return if (read(one, 0, 1) < 0) -1 else one[0].toInt() and 0xff
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            val remaining = requestedLength?.minus(position)?.coerceAtLeast(0L)
                ?: (totalLength - start - position).coerceAtLeast(0L)
            if (remaining == 0L) return -1
            val count = file.read(buffer, start + position, offset, minOf(length.toLong(), remaining).toInt())
            if (count > 0) position += count
            return count
        }

        override fun close() = Unit
    }

    private companion object {
        val SMB_CONFIG = SmbConfig.builder()
            .withDialects(
                com.hierynomus.mssmb2.SMB2Dialect.SMB_2_0_2,
                com.hierynomus.mssmb2.SMB2Dialect.SMB_2_1,
                com.hierynomus.mssmb2.SMB2Dialect.SMB_3_0,
                com.hierynomus.mssmb2.SMB2Dialect.SMB_3_0_2,
                com.hierynomus.mssmb2.SMB2Dialect.SMB_3_1_1,
            )
            .withTimeout(12, TimeUnit.SECONDS)
            .withSoTimeout(12, TimeUnit.SECONDS)
            .build()
    }
}
