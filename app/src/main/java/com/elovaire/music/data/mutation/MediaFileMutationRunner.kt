package elovaire.music.droidbeauty.app.data.mutation

import elovaire.music.droidbeauty.app.platform.readWithProgress

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.StatFs
import elovaire.music.droidbeauty.app.data.library.db.LibraryMutationEntity
import elovaire.music.droidbeauty.app.data.library.network.NetworkFileSystemRegistry
import elovaire.music.droidbeauty.app.data.library.network.NetworkReadPurpose
import elovaire.music.droidbeauty.app.data.library.network.NetworkResourceUri
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceIdentity
import elovaire.music.droidbeauty.app.domain.kernel.MediaMutationStatus
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.platform.MediaWriteTarget
import elovaire.music.droidbeauty.app.platform.MediaWriteTargetClassifier
import elovaire.music.droidbeauty.app.platform.ContentIo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

internal class MediaFileMutationRunner(
    context: Context,
    private val tempDirectoryName: String = RECOVERY_DIRECTORY,
    private val faultInjector: MediaMutationFaultInjector = NoOpMediaMutationFaultInjector,
    private val networkRegistryProvider: (() -> NetworkFileSystemRegistry)? = null,
) {
    private val appContext = context.applicationContext
    private val contentResolver: ContentResolver = appContext.contentResolver
    private val contentIo = ContentIo(contentResolver)

    fun requireWritable(uri: Uri) {
        if (NetworkResourceUri.isNetworkUri(uri)) {
            networkSourceFingerprint(uri)
            return
        }
        when (val target = MediaWriteTargetClassifier.classify(appContext, uri)) {
            is MediaWriteTarget.MediaStoreItem -> Unit
            is MediaWriteTarget.SafDocument -> contentIo.requireSafWriteAccess(target.uri)
            is MediaWriteTarget.FileUri -> {
                val path = target.uri.path ?: error("The file path is unavailable.")
                check(File(path).canWrite()) { "The song file is not writable." }
            }
            is MediaWriteTarget.Unsupported -> error(target.reason)
        }
    }

    fun preflight(song: Song) {
        faultInjector.checkpoint(MediaMutationTransactionPhase.SourcePreflight)
        if (NetworkResourceUri.isNetworkUri(song.uri)) {
            requireWritable(song.uri)
            networkRead(song.uri).use { handle ->
                check(handle.input.read() >= 0) { "The song file is empty." }
                handle.length?.takeIf { it > 0L }?.let { size ->
                    check(hasSufficientMutationStorage(StatFs(appContext.filesDir.path).availableBytes, size)) {
                        "There is not enough temporary storage to edit this song safely."
                    }
                }
            }
            return
        }
        val target = MediaWriteTargetClassifier.classify(appContext, song.uri)
        requireWritable(song.uri)
        val sourceSize = when (target) {
            is MediaWriteTarget.FileUri -> target.uri.path?.let(::File)?.length() ?: -1L
            else -> contentIo.openReadableDescriptor(song.uri).use { it.statSize }
        }
        check(sourceSize != 0L) { "The song file is empty." }
        contentResolver.openInputStream(song.uri)?.use { input ->
            check(input.read() >= 0) { "The song file is empty." }
        } ?: error("Unable to open the source file.")
        if (sourceSize > 0L) {
            val availableBytes = StatFs(appContext.filesDir.path).availableBytes
            check(hasSufficientMutationStorage(availableBytes, sourceSize)) {
                "There is not enough temporary storage to edit this song safely."
            }
            if (target is MediaWriteTarget.FileUri) {
                val destination = target.uri.path?.let(::File)
                val destinationAvailable = destination?.parentFile?.let { StatFs(it.path).availableBytes }
                check(destinationAvailable != null && destinationAvailable >= sourceSize) {
                    "There is not enough storage to replace this song safely."
                }
            }
        }
    }

    fun copySongToTemp(
        song: Song,
        purpose: String,
    ): File {
        val destination = createTempFile(song, purpose)
        var complete = false
        try {
            if (NetworkResourceUri.isNetworkUri(song.uri)) {
                networkRead(song.uri).use { handle ->
                    FileOutputStream(destination, false).use { output ->
                        val copiesRemaining = if (purpose == "backup") TEMP_COPY_COUNT else 1L
                        val maxBytes = StatFs(appContext.filesDir.path).availableBytes / copiesRemaining
                        val buffer = ByteArray(COPY_BUFFER_SIZE)
                        var copied = 0L
                        while (true) {
                            if (Thread.currentThread().isInterrupted) throw java.io.InterruptedIOException("Network copy interrupted")
                            val count = handle.input.readWithProgress(buffer)
                            if (count < 0) break
                            copied += count
                            check(copied <= maxBytes) { "There is not enough temporary storage to edit this song safely." }
                            output.write(buffer, 0, count)
                        }
                        output.flush()
                        output.fd.sync()
                        check(handle.length == null || copied == handle.length) { "The network song file was truncated." }
                    }
                }
            } else {
                contentIo.copyToFile(song.uri, destination)
            }
            check(destination.length() > 0L) { "The song file is empty." }
            faultInjector.checkpoint(
                if (purpose == "backup") {
                    MediaMutationTransactionPhase.BackupCopied
                } else {
                    MediaMutationTransactionPhase.PersistedVerification
                },
            )
            complete = true
            return destination
        } finally {
            if (!complete) destination.delete()
        }
    }

    fun createTempFile(
        song: Song,
        purpose: String,
    ): File {
        val directory = File(appContext.filesDir, if (purpose == "backup" || purpose == "network-working") RECOVERY_DIRECTORY else tempDirectoryName)
        check(directory.isDirectory || directory.mkdirs()) { "Unable to create the metadata edit directory." }
        val extension = mediaMutationTemporaryExtension(song.fileName)
        return File.createTempFile("${song.id}-$purpose-", ".$extension", directory)
    }

    fun recoverBackup(mutation: LibraryMutationEntity): File {
        val backup = File(checkNotNull(mutation.recoveryBackupPath)).canonicalFile
        check(backup.parentFile == File(appContext.filesDir, RECOVERY_DIRECTORY).canonicalFile) {
            "The media recovery backup is outside the recovery directory."
        }
        if (mutation.status !in setOf(
                MediaMutationStatus.PersistedVerified.name,
                MediaMutationStatus.Published.name,
                MediaMutationStatus.Completed.name,
            )
        ) {
            check(backup.isFile && backup.length() > 0L) { "The media recovery backup is unavailable." }
            val uri = Uri.parse(checkNotNull(mutation.uri))
            if (!originalMatchesBackup(uri, backup)) {
                overwriteOriginal(uri, backup)
                verifyOriginalBytes(uri, backup)
            }
        }
        return backup
    }

    fun networkRecoveryFiles(mutation: LibraryMutationEntity): Pair<File, File> {
        val directory = File(appContext.filesDir, RECOVERY_DIRECTORY).canonicalFile
        val backup = File(checkNotNull(mutation.recoveryBackupPath)).canonicalFile
        val replacement = File(checkNotNull(mutation.recoveryReplacementPath)).canonicalFile
        check(backup.parentFile == directory && replacement.parentFile == directory && backup != replacement &&
            backup.isFile && backup.length() > 0L && replacement.isFile && replacement.length() > 0L
        ) { "The network recovery files are unavailable or invalid." }
        return backup to replacement
    }

    fun cleanupNetworkTemporary(mutation: LibraryMutationEntity, expectedFiles: List<File>) {
        val uri = Uri.parse(checkNotNull(mutation.uri))
        check(NetworkResourceUri.isNetworkUri(uri)) { "The network recovery target is invalid." }
        val registry = checkNotNull(networkRegistryProvider) { "Network file access is unavailable." }.invoke()
        registry.cleanupTemporaryBlocking(
            sourceId = checkNotNull(NetworkResourceUri.sourceId(uri)),
            targetPath = checkNotNull(NetworkResourceUri.path(uri)),
            temporaryPath = checkNotNull(mutation.recoveryNetworkTemporaryPath),
            operationId = mutation.mutationId,
            expectedSourceFingerprint = checkNotNull(mutation.recoveryNetworkSourceFingerprint),
            expectedFiles = expectedFiles,
        )
    }

    fun copyFileDurably(source: File, destination: File): Long {
        check(source.isFile && source.length() > 0L) { "The source file is empty." }
        var copied = 0L
        var complete = false
        try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination, false).use { output ->
                    copied = input.copyTo(output, COPY_BUFFER_SIZE)
                    output.flush()
                    output.fd.sync()
                }
            }
            check(copied == source.length() && destination.length() == source.length()) {
                "Unable to create a complete working copy."
            }
            faultInjector.checkpoint(MediaMutationTransactionPhase.WorkingCopyCopied)
            complete = true
            return copied
        } finally {
            if (!complete) destination.delete()
        }
    }

    fun overwriteOriginal(
        uri: Uri,
        source: File,
        networkContext: NetworkMutationContext? = null,
        onWriteStarted: () -> Unit = {},
    ) {
        faultInjector.checkpoint(MediaMutationTransactionPhase.OriginalOverwrite)
        check(source.isFile && source.length() > 0L) { "The replacement song file is empty." }
        if (NetworkResourceUri.isNetworkUri(uri)) {
            val operation = checkNotNull(networkContext) { "Network replacement recovery is unavailable." }
            val registry = checkNotNull(networkRegistryProvider) { "Network file access is unavailable." }.invoke()
            registry.replaceBlocking(
                sourceId = checkNotNull(NetworkResourceUri.sourceId(uri)),
                path = checkNotNull(NetworkResourceUri.path(uri)),
                expectedOriginal = operation.expectedOriginal,
                replacement = source,
                operationId = operation.operationId,
                expectedSourceFingerprint = operation.sourceFingerprint,
                recordTemporary = operation.recordTemporary,
                resolveTemporary = operation.resolveTemporary,
                onCommitStarting = onWriteStarted,
            )
            faultInjector.checkpoint(MediaMutationTransactionPhase.OriginalCommitted)
            return
        }
        val target = MediaWriteTargetClassifier.classify(appContext, uri)
        if (target is MediaWriteTarget.Unsupported) error(target.reason)
        if (target is MediaWriteTarget.FileUri) {
            val destination = target.uri.path?.let(::File) ?: error("The file path is unavailable.")
            val parent = destination.parentFile ?: error("The song directory is unavailable.")
            val replacement = File.createTempFile(
                ".${destination.name}-",
                ".elovaire-replacement",
                parent,
            )
            var moved = false
            try {
                copyFileDurably(source, replacement)
                try {
                    onWriteStarted()
                    Files.move(
                        replacement.toPath(),
                        destination.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                } catch (unsupported: AtomicMoveNotSupportedException) {
                    throw IllegalStateException("The file system cannot atomically replace this song.", unsupported)
                }
                moved = true
            } finally {
                if (!moved) replacement.delete()
            }
            check(destination.length() == source.length()) { "Unable to replace the complete song file." }
            faultInjector.checkpoint(MediaMutationTransactionPhase.OriginalCommitted)
            return
        }
        contentIo.replaceFromFile(uri, source, onWriteStarted)
        faultInjector.checkpoint(MediaMutationTransactionPhase.OriginalCommitted)
    }

    fun verifyOriginalBytes(
        uri: Uri,
        expected: File,
    ) {
        faultInjector.checkpoint(MediaMutationTransactionPhase.PersistedVerification)
        check(originalMatchesBackup(uri, expected)) { "The restored song does not match the backup." }
    }

    fun originalMatchesBackup(uri: Uri, expected: File): Boolean {
        val actualDigest = if (NetworkResourceUri.isNetworkUri(uri)) {
            networkRead(uri).use { sha256(it.input) }
        } else {
            contentResolver.openInputStream(uri)?.use(::sha256)
                ?: error("Unable to verify the restored song.")
        }
        val expectedDigest = expected.inputStream().use(::sha256)
        return actualDigest.contentEquals(expectedDigest)
    }

    fun networkSourceFingerprint(uri: Uri): String {
        val registry = checkNotNull(networkRegistryProvider) { "Network file access is unavailable." }.invoke()
        val sourceId = checkNotNull(NetworkResourceUri.sourceId(uri)) { "The network source is unavailable." }
        checkNotNull(NetworkResourceUri.path(uri)) { "The network song path is invalid." }
        val source = checkNotNull(registry.source(sourceId)) { "The network source is unavailable." }
        check(source.enabled && registry.credentials(source) != null) { "The network source cannot be accessed." }
        return NetworkSourceIdentity.locationFingerprint(source)
    }

    private fun networkRead(uri: Uri): elovaire.music.droidbeauty.app.data.library.network.NetworkReadHandle {
        val registry = checkNotNull(networkRegistryProvider) { "Network file access is unavailable." }.invoke()
        val sourceId = checkNotNull(NetworkResourceUri.sourceId(uri)) { "The network source is unavailable." }
        val path = checkNotNull(NetworkResourceUri.path(uri)) { "The network song path is invalid." }
        return registry.openBlocking(sourceId, path, 0L, -1L, NetworkReadPurpose.Metadata)
    }

    private fun sha256(input: InputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(COMPARE_BUFFER_SIZE)
        while (true) {
            val count = input.readWithProgress(buffer)
            if (count < 0) break
            if (count > 0) digest.update(buffer, 0, count)
        }
        return digest.digest()
    }

    private companion object {
        const val COMPARE_BUFFER_SIZE = 64 * 1024
        const val COPY_BUFFER_SIZE = 64 * 1024
        const val RECOVERY_DIRECTORY = "media-mutation-backups"
    }
}

internal data class NetworkMutationContext(
    val expectedOriginal: File,
    val operationId: String,
    val sourceFingerprint: String,
    val recordTemporary: (String) -> Unit,
    val resolveTemporary: () -> Unit,
)

internal fun mediaMutationTemporaryExtension(fileName: String): String = fileName
    .substringAfterLast('.', "")
    .filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }
    .take(MAX_TEMP_EXTENSION_LENGTH)
    .ifBlank { "tmp" }

private const val MAX_TEMP_EXTENSION_LENGTH = 16

internal fun requiredMutationStagingBytes(sourceSizeBytes: Long): Long {
    if (sourceSizeBytes <= 0L) return 0L
    return sourceSizeBytes.coerceAtMost(Long.MAX_VALUE / TEMP_COPY_COUNT) * TEMP_COPY_COUNT
}

internal fun hasSufficientMutationStorage(
    availableBytes: Long,
    sourceSizeBytes: Long,
): Boolean {
    return availableBytes >= requiredMutationStagingBytes(sourceSizeBytes)
}

private const val TEMP_COPY_COUNT = 3L
