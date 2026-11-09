package elovaire.music.droidbeauty.app.data.library.network

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

/** The handles deny other writers and renames until they are closed. */
internal interface SmbEditHandle : Closeable {
    fun input(): InputStream
    fun output(): OutputStream
    fun flush()
    fun rename(path: String)
    fun deleteOnClose()
}

internal interface SmbEditStore {
    /** Returns null for an existing name. */
    fun create(path: String): SmbEditHandle?
    /** Returns null only when the path does not exist. */
    fun open(path: String): SmbEditHandle?
}

/** Keeps a server-side original until the staged replacement has been installed. */
internal class SmbConditionalWriter(private val store: SmbEditStore) {
    fun replace(
        target: String,
        expected: File,
        replacement: File,
        operationId: String,
        recordTemporary: (String) -> Unit,
        resolveTemporary: () -> Unit,
        onCommitStarting: () -> Unit,
    ) {
        val paths = paths(target, operationId)
        requireFile(expected)
        requireFile(replacement)
        recordTemporary(paths.temporary)
        val staged = store.create(paths.temporary)
        if (staged == null) {
            resolveTemporary()
            throw IOException("SMB temporary file already exists")
        }
        staged.use {
            replacement.inputStream().use { input ->
                staged.output().use { output -> input.copyTo(output, BUFFER_SIZE) }
            }
            staged.flush()
            if (!matches(staged, replacement)) throw IOException("SMB staged file does not match replacement")
            val original = store.open(paths.target) ?: throw IOException("SMB original file is unavailable")
            original.use {
                if (!matches(original, expected)) throw IOException("SMB file changed during editing")
                onCommitStarting()
                original.rename(paths.backup)
                staged.rename(paths.target)
                original.deleteOnClose()
            }
        }
        resolveTemporary()
    }

    fun cleanup(
        target: String,
        temporary: String,
        operationId: String,
        expectedFiles: List<File>,
        onDeleteStarting: () -> Unit,
    ) {
        val normal = paths(target, operationId)
        val rollback = paths(target, "rollback-$operationId")
        val paths = when (temporary) {
            normal.temporary -> normal
            rollback.temporary -> rollback
            else -> throw IOException("Invalid SMB recovery temporary path")
        }
        if (expectedFiles.isEmpty()) throw IOException("SMB recovery verification is unavailable")
        expectedFiles.forEach(::requireFile)
        store.open(paths.target).use { targetHandle ->
            store.open(paths.backup).use { backupHandle ->
                store.open(paths.temporary).use { temporaryHandle ->
                    var current = targetHandle
                    if (current == null) {
                        val backup = backupHandle ?: throw IOException("SMB original is missing")
                        val expectedOriginal = if (paths == rollback) expectedFiles.last() else expectedFiles.first()
                        if (!matches(backup, expectedOriginal)) throw IOException("SMB backup does not match recovery data")
                        onDeleteStarting()
                        backup.rename(paths.target)
                        current = backup
                    }
                    if (backupHandle != null && backupHandle !== current) {
                        val before = if (paths == rollback) expectedFiles.last() else expectedFiles.first()
                        val after = if (paths == rollback) expectedFiles.first() else expectedFiles.last()
                        if (temporaryHandle != null || !matches(current, after) || !matches(backupHandle, before)) {
                            throw IOException("SMB file changed after interrupted editing")
                        }
                    }
                    if (temporaryHandle != null && !matchesAny(temporaryHandle, expectedFiles)) {
                        throw IOException("SMB temporary file does not match recovery data")
                    }
                    if (backupHandle != null && backupHandle !== current) {
                        onDeleteStarting()
                        backupHandle.deleteOnClose()
                    }
                    if (temporaryHandle != null) {
                        onDeleteStarting()
                        temporaryHandle.deleteOnClose()
                    }
                }
            }
        }
    }

    private fun matchesAny(handle: SmbEditHandle, expected: List<File>): Boolean = expected.any { matches(handle, it) }

    private fun matches(handle: SmbEditHandle, expected: File): Boolean {
        val expectedHash = expected.inputStream().use { digest(it, expected.length()) }
            ?: throw IOException("SMB recovery file changed during verification")
        val actualHash = handle.input().use { digest(it, expected.length()) }
        return actualHash != null && MessageDigest.isEqual(expectedHash, actualHash)
    }

    private fun digest(input: InputStream, maximum: Long): ByteArray? {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            if (Thread.currentThread().isInterrupted) throw IOException("SMB write interrupted")
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) throw IOException("SMB read made no progress")
            total += count
            if (total > maximum) return null
            digest.update(buffer, 0, count)
        }
        return if (total == maximum) digest.digest() else null
    }

    private fun requireFile(file: File) {
        if (!file.isFile || file.length() <= 0L) throw IOException("SMB recovery file is unavailable")
    }

    private fun paths(target: String, id: String): Paths {
        val safeTarget = NetworkPathPolicy.validateRelativePath(target)?.takeIf(String::isNotBlank)
            ?: throw IOException("Invalid SMB media path")
        if (!OPERATION_ID.matches(id)) throw IOException("Invalid SMB operation identifier")
        val directory = safeTarget.substringBeforeLast('/', "")
        return Paths(safeTarget, NetworkPathPolicy.join(directory, ".elovaire-$id.tmp"),
            NetworkPathPolicy.join(directory, ".elovaire-$id.bak"))
    }

    private data class Paths(val target: String, val temporary: String, val backup: String)

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
        val OPERATION_ID = Regex("[A-Za-z0-9_-]{1,80}")
    }
}
