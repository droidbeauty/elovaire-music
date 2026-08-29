package elovaire.music.droidbeauty.app.data.library.network

import android.os.SystemClock
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.EnumSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SmbNetworkFileSystem(
    private val scope: CoroutineScope,
) : NetworkFileSystem {
    private val sessionMapLock = Any()
    private val sessions = mutableMapOf<String, SourceSession>()

    override fun probeBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): NetworkProbeResult {
        return runCatching {
            withShare(source, credentials) { share -> share.folderExists(smbPath(source)) }
            NetworkProbeResult(NetworkAvailability.Available)
        }.getOrElse { failure ->
            val remoteFailure = failure.asRemoteIoFailure()
            NetworkProbeResult(
                availability = remoteFailure.kind.toNetworkAvailability(),
                message = remoteFailure::class.simpleName,
            )
        }
    }

    override fun listBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        maxEntries: Int,
        maxDepth: Int,
    ): NetworkListingResult = withShare(source, credentials) { share ->
        require(maxEntries > 0)
        val entries = ArrayList<NetworkFileEntry>()
        val pending = ArrayDeque<Pair<String, Int>>()
        val visitedDirectories = HashSet<String>()
        val rootPath = smbPath(source)
        pending.addLast(rootPath to 0)
        var directoryCount = 0
        var incompleteReason: String? = null
        while (pending.isNotEmpty() && entries.size < maxEntries) {
            val (directory, depth) = pending.removeFirst()
            if (!visitedDirectories.add(directory)) continue
            if (++directoryCount > MAX_DIRECTORY_COUNT) {
                incompleteReason = "directory-budget"
                break
            }
            share.list(directory).forEach { item ->
                if (entries.size >= maxEntries) {
                    incompleteReason = "entry-budget"
                    return@forEach
                }
                val name = item.fileName ?: return@forEach
                if (name == "." || name == "..") return@forEach
                val fullPath = NetworkPathPolicy.join(directory, name)
                val directoryEntry = item.isDirectory()
                val entry = NetworkFileEntry(
                    path = fullPath.removeRootPath(rootPath),
                    isDirectory = directoryEntry,
                    sizeBytes = item.endOfFile.takeIf { it >= 0L },
                    modifiedAtMs = item.lastWriteTime?.toEpochMillis(),
                    sourceEntryId = item.fileId.takeIf { it != 0L }?.toString(),
                )
                entries += entry
                val isReparsePoint = item.fileAttributes and FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT.value != 0L
                if (directoryEntry && !isReparsePoint) {
                    if (depth < maxDepth) {
                        pending.addLast(fullPath to depth + 1)
                    } else {
                        incompleteReason = incompleteReason ?: "depth-budget"
                    }
                }
            }
        }
        if (entries.size >= maxEntries) incompleteReason = incompleteReason ?: "entry-budget"
        incompleteReason?.let { NetworkListingResult.Incomplete(entries, it) }
            ?: NetworkListingResult.Complete(entries)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun openBlocking(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        position: Long,
        length: Long,
    ): NetworkReadHandle {
        val session = sessionFor(source, credentials)
        val lease = session.acquire()
        var file: com.hierynomus.smbj.share.File? = null
        return try {
            val openedFile = lease.share.openFile(
                NetworkPathPolicy.join(smbPath(source), path),
                EnumSet.of(AccessMask.FILE_READ_DATA),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE),
            )
            file = openedFile
            val totalLength = openedFile.getFileInformation(
                com.hierynomus.msfscc.fileinformation.FileStandardInformation::class.java,
            ).endOfFile
            val range = resolveNetworkRange(totalLength, position, length)
            val start = position
            NetworkReadHandle(
                input = SmbRangeInputStream(
                    file = openedFile,
                    start = start,
                    requestedLength = range.exposedLength,
                    totalLength = totalLength,
                    onReadFailure = { failure ->
                        if (failure.shouldInvalidateSession()) session.invalidate()
                        failure.asRemoteIoFailure()
                    },
                ),
                length = range.exposedLength,
                closeHandle = {
                    runCatching { openedFile.close() }
                    lease.close()
                },
            )
        } catch (failure: Throwable) {
            runCatching { file?.close() }
            lease.close()
            if (failure.shouldInvalidateSession()) session.invalidate()
            throw failure.asRemoteIoFailure()
        } finally {
            if (file == null) {
                lease.close()
            }
        }
    }

    override fun invalidate(sourceId: String) {
        val session = synchronized(sessionMapLock) { sessions.remove(sourceId) }
        session?.invalidate()
    }

    override fun invalidateAll() {
        val activeSessions = synchronized(sessionMapLock) {
            sessions.values.toList().also { sessions.clear() }
        }
        activeSessions.forEach(SourceSession::invalidate)
    }

    override fun release() {
        val activeSessions = synchronized(sessionMapLock) {
            sessions.values.toList().also { sessions.clear() }
        }
        activeSessions.forEach(SourceSession::release)
    }

    private fun smbPath(source: NetworkLibrarySource): String {
        return NetworkPathPolicy.smbShareAndPath(source.shareOrPath)?.second.orEmpty()
    }

    private fun String.removeRootPath(rootPath: String): String {
        if (rootPath.isBlank()) return this
        return removePrefix(rootPath).trimStart('/').ifBlank { this }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun <T> withShare(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        block: (DiskShare) -> T,
    ): T {
        val session = sessionFor(source, credentials)
        val lease = session.acquire()
        return try {
            block(lease.share)
        } catch (failure: Throwable) {
            if (failure.shouldInvalidateSession()) session.invalidate()
            throw failure.asRemoteIoFailure()
        } finally {
            lease.close()
        }
    }

    private fun sessionFor(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): SourceSession {
        val key = SessionKey(
            sourceKey = source.id,
            server = source.server,
            shareOrPath = source.shareOrPath,
            username = credentials.username,
            domain = credentials.domain,
            passwordFingerprint = credentials.password.fingerprint(),
        )
        val staleSessions = mutableListOf<SourceSession>()
        val selected = synchronized(sessionMapLock) {
            sessions.values.filterTo(staleSessions) { it.isIdle(SystemClock.elapsedRealtime()) }
            staleSessions.forEach { session -> sessions.remove(session.key.sourceKey, session) }
            val current = sessions[source.id]
            if (current != null && current.key == key && current.isUsable()) {
                current
            } else {
                current?.let(staleSessions::add)
                SourceSession(key, source, credentials).also { sessions[source.id] = it }
            }
        }
        staleSessions.distinct().forEach(SourceSession::invalidate)
        return selected
    }

    @Suppress("TooGenericExceptionCaught")
    private fun createSessionResources(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
    ): SessionResources {
        val host = NetworkPathPolicy.smbServer(source.server) ?: throw IOException("SMB server is invalid")
        val shareName = NetworkPathPolicy.smbShareAndPath(source.shareOrPath)?.first
            ?: throw IOException("SMB share is missing")
        val client = SMBClient(SMB_CONFIG)
        var connection: com.hierynomus.smbj.connection.Connection? = null
        var session: com.hierynomus.smbj.session.Session? = null
        var share: DiskShare? = null
        return try {
            connection = client.connect(host, NetworkPathPolicy.smbPort(source.server))
            session = connection.authenticate(
                AuthenticationContext(
                    credentials.username,
                    credentials.password.toCharArray(),
                    credentials.domain,
                ),
            )
            share = session.connectShare(shareName) as? DiskShare
                ?: throw IOException("SMB share is not a disk share")
            SessionResources(client, connection, session, share)
        } catch (failure: Exception) {
            listOf(share, session, connection, client)
                .forEach { resource -> resource?.let { runCatching { it.close() } } }
            throw failure
        }
    }

    private inner class SourceSession(
        val key: SessionKey,
        private val source: NetworkLibrarySource,
        private val credentials: NetworkCredentials,
    ) {
        private val lock = Any()
        private var resources: SessionResources? = null
        private var connecting: CompletableFuture<SessionResources>? = null
        private var activeLeases = 0
        private var lastUsedElapsedMs = SystemClock.elapsedRealtime()
        private var invalidated = false
        private var idleCloseJob: Job? = null

        fun isUsable(): Boolean = synchronized(lock) {
            !invalidated && resources?.share?.isConnected == true
        }

        fun isIdle(nowElapsedMs: Long): Boolean = synchronized(lock) {
            activeLeases == 0 && nowElapsedMs - lastUsedElapsedMs >= SESSION_IDLE_TIMEOUT_MS
        }

        @Suppress("TooGenericExceptionCaught")
        fun acquire(): SessionLease {
            val connectFuture: CompletableFuture<SessionResources>
            val ownsConnect: Boolean
            synchronized(lock) {
                check(!invalidated) { "SMB session is invalidated" }
                val currentResources = resources
                if (currentResources?.share?.isConnected == true) {
                    idleCloseJob?.cancel()
                    idleCloseJob = null
                    activeLeases += 1
                    lastUsedElapsedMs = SystemClock.elapsedRealtime()
                    return SessionLease(currentResources.share, ::releaseLease)
                }
                val existingConnect = connecting
                if (existingConnect != null) {
                    connectFuture = existingConnect
                    ownsConnect = false
                } else {
                    connectFuture = CompletableFuture()
                    connecting = connectFuture
                    ownsConnect = true
                }
            }

            if (ownsConnect) {
                try {
                    val created = createSessionResources(source, credentials)
                    val rejected = synchronized(lock) {
                        if (connecting === connectFuture) connecting = null
                        if (invalidated) {
                            connectFuture.completeExceptionally(IOException("SMB session was invalidated"))
                            true
                        } else {
                            resources = created
                            connectFuture.complete(created)
                            false
                        }
                    }
                    if (rejected) created.close()
                } catch (failure: Throwable) {
                    synchronized(lock) {
                        if (connecting === connectFuture) connecting = null
                        connectFuture.completeExceptionally(failure)
                    }
                    throw failure
                }
            }

            val resolved = try {
                connectFuture.get()
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("SMB session acquisition interrupted", interrupted)
            } catch (failure: ExecutionException) {
                throw (failure.cause ?: failure)
            }
            synchronized(lock) {
                check(!invalidated) { "SMB session is invalidated" }
                check(resources === resolved && resolved.share.isConnected) { "SMB session is unavailable" }
                idleCloseJob?.cancel()
                idleCloseJob = null
                activeLeases += 1
                lastUsedElapsedMs = SystemClock.elapsedRealtime()
                return SessionLease(resolved.share, ::releaseLease)
            }
        }

        fun invalidate() {
            val closing = synchronized(lock) {
                invalidated = true
                connecting?.completeExceptionally(IOException("SMB session was invalidated"))
                connecting = null
                if (activeLeases == 0) detachResourcesLocked() else null
            }
            closing?.close()
        }

        fun release() {
            val closing = synchronized(lock) {
                invalidated = true
                connecting?.completeExceptionally(IOException("SMB session was released"))
                connecting = null
                detachResourcesLocked()
            }
            closing?.close()
        }

        private fun releaseLease() {
            val closing = synchronized(lock) {
                activeLeases = (activeLeases - 1).coerceAtLeast(0)
                lastUsedElapsedMs = SystemClock.elapsedRealtime()
                if (activeLeases == 0) {
                    if (invalidated) {
                        detachResourcesLocked()
                    } else {
                        scheduleIdleCloseLocked()
                        null
                    }
                } else null
            }
            closing?.close()
        }

        private fun scheduleIdleCloseLocked() {
            idleCloseJob?.cancel()
            idleCloseJob = scope.launch(Dispatchers.IO) {
                delay(SESSION_IDLE_TIMEOUT_MS)
                val closing = synchronized(lock) {
                    if (
                        activeLeases == 0 &&
                        !invalidated &&
                        SystemClock.elapsedRealtime() - lastUsedElapsedMs >= SESSION_IDLE_TIMEOUT_MS
                    ) {
                        invalidated = true
                        detachResourcesLocked()
                    } else {
                        idleCloseJob = null
                        null
                    }
                }
                closing?.close()
            }
        }

        private fun detachResourcesLocked(): SessionResources? {
            idleCloseJob?.cancel()
            idleCloseJob = null
            return resources.also { resources = null }
        }
    }

    private class SessionLease(
        val share: DiskShare,
        private val onClose: () -> Unit,
    ) : AutoCloseable {
        private var closed = false

        override fun close() {
            if (closed) return
            closed = true
            onClose()
        }
    }

    private data class SessionResources(
        val client: SMBClient,
        val connection: com.hierynomus.smbj.connection.Connection,
        val session: com.hierynomus.smbj.session.Session,
        val share: DiskShare,
    ) : AutoCloseable {
        private val resource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveSmbSession)

        override fun close() {
            try {
                listOf(share, session, connection, client)
                    .forEach { resource -> runCatching { resource.close() } }
            } finally {
                resource.close()
            }
        }
    }

    private data class SessionKey(
        val sourceKey: String,
        val server: String,
        val shareOrPath: String,
        val username: String,
        val domain: String?,
        val passwordFingerprint: String,
    )

    private fun String.fingerprint(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

    private fun Throwable.asRemoteIoFailure(): NetworkRemoteIoException {
        if (this is NetworkRemoteIoException) return this
        val kind = when (this) {
            is SMBApiException -> when (getStatus()) {
                NtStatus.STATUS_LOGON_FAILURE,
                NtStatus.STATUS_PASSWORD_EXPIRED,
                NtStatus.STATUS_ACCOUNT_DISABLED,
                -> RemoteIoFailureKind.Authentication
                NtStatus.STATUS_ACCESS_DENIED,
                NtStatus.STATUS_PRIVILEGE_NOT_HELD,
                NtStatus.STATUS_LOGON_TYPE_NOT_GRANTED,
                -> RemoteIoFailureKind.Permission
                NtStatus.STATUS_BAD_NETWORK_NAME -> RemoteIoFailureKind.ShareMissing
                NtStatus.STATUS_OBJECT_NAME_NOT_FOUND,
                NtStatus.STATUS_OBJECT_PATH_NOT_FOUND,
                NtStatus.STATUS_NO_SUCH_FILE,
                NtStatus.STATUS_NOT_FOUND,
                NtStatus.STATUS_FILE_DELETED,
                -> RemoteIoFailureKind.PathMissing
                NtStatus.STATUS_TIMEOUT,
                NtStatus.STATUS_IO_TIMEOUT,
                -> RemoteIoFailureKind.Timeout
                NtStatus.STATUS_CONNECTION_DISCONNECTED,
                NtStatus.STATUS_CONNECTION_RESET,
                NtStatus.STATUS_NETWORK_NAME_DELETED,
                -> RemoteIoFailureKind.ConnectionReset
                NtStatus.STATUS_NETWORK_SESSION_EXPIRED,
                NtStatus.STATUS_USER_SESSION_DELETED,
                -> RemoteIoFailureKind.SessionInvalid
                NtStatus.STATUS_BAD_NETWORK_PATH -> RemoteIoFailureKind.HostUnreachable
                NtStatus.STATUS_NOT_SUPPORTED,
                NtStatus.STATUS_NOT_IMPLEMENTED,
                NtStatus.STATUS_INVALID_PARAMETER,
                -> RemoteIoFailureKind.Protocol
                NtStatus.STATUS_RETRY,
                NtStatus.STATUS_REQUEST_NOT_ACCEPTED,
                -> RemoteIoFailureKind.TransientServer
                else -> RemoteIoFailureKind.Unknown
            }
            else -> remoteIoFailureKind()
        }
        return NetworkRemoteIoException(kind, "SMB operation failed", this)
    }

    private fun Throwable.shouldInvalidateSession(): Boolean {
        val kind = when (this) {
            is SMBApiException -> when (getStatus()) {
                NtStatus.STATUS_TIMEOUT,
                NtStatus.STATUS_IO_TIMEOUT,
                NtStatus.STATUS_CONNECTION_DISCONNECTED,
                NtStatus.STATUS_CONNECTION_RESET,
                NtStatus.STATUS_NETWORK_NAME_DELETED,
                NtStatus.STATUS_NETWORK_SESSION_EXPIRED,
                NtStatus.STATUS_USER_SESSION_DELETED,
                -> RemoteIoFailureKind.ConnectionReset
                else -> RemoteIoFailureKind.Unknown
            }
            else -> remoteIoFailureKind()
        }
        return kind in setOf(
            RemoteIoFailureKind.HostUnreachable,
            RemoteIoFailureKind.Timeout,
            RemoteIoFailureKind.ConnectionReset,
            RemoteIoFailureKind.SessionInvalid,
        )
    }

    private fun FileIdBothDirectoryInformation.isDirectory(): Boolean {
        return fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L
    }

    private inner class SmbRangeInputStream(
        private val file: com.hierynomus.smbj.share.File,
        private val start: Long,
        private val requestedLength: Long?,
        private val totalLength: Long,
        private val onReadFailure: (Throwable) -> Throwable,
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
            val count = try {
                file.read(buffer, start + position, offset, minOf(length.toLong(), remaining).toInt())
            } catch (failure: Exception) {
                throw onReadFailure(failure)
            }
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
        const val SESSION_IDLE_TIMEOUT_MS = 60_000L
        const val MAX_DIRECTORY_COUNT = 25_000
    }
}
