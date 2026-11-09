package elovaire.music.droidbeauty.app.data.library.network

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/** Replaces a WebDAV resource only while its original bytes are exclusively locked. */
internal class WebDavConditionalWriter(client: OkHttpClient) : Closeable {
    private val client = client.newBuilder().followRedirects(false).followSslRedirects(false).build()
    private val closed = AtomicBoolean(false)
    private val calls = ConcurrentHashMap.newKeySet<Call>()

    fun replace(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        path: String,
        expectedOriginal: File,
        replacement: File,
        operationId: String,
        recordTemporary: (String) -> Unit,
        resolveTemporary: () -> Unit,
        onCommitStarting: () -> Unit,
    ) {
        val relative = NetworkPathPolicy.validateRelativePath(path)?.takeIf(String::isNotBlank)
            ?: throw IOException("Invalid WebDAV file path")
        val root = NetworkPathPolicy.validateRelativePath(source.shareOrPath)
            ?: throw IOException("Invalid WebDAV root path")
        if (root.isNotEmpty() && !relative.startsWith("$root/")) throw IOException("WebDAV file is outside the source")
        if (!OPERATION_ID.matches(operationId)) throw IOException("Invalid WebDAV operation identifier")
        if (!expectedOriginal.isFile || expectedOriginal.length() <= 0L || !replacement.isFile || replacement.length() <= 0L) {
            throw IOException("WebDAV replacement files are unavailable")
        }
        val target = NetworkPathPolicy.webDavResourceUrl(source.server, relative)
            ?: throw IOException("Invalid WebDAV server")
        val directory = relative.substringBeforeLast('/', "")
        val temporaryPath = NetworkPathPolicy.join(directory, ".elovaire-$operationId.tmp")
        val temporary = NetworkPathPolicy.webDavResourceUrl(source.server, temporaryPath)
            ?: throw IOException("Invalid WebDAV temporary path")
        val auth = okhttp3.Credentials.basic(credentials.username, credentials.password, Charsets.UTF_8)
        fun request(url: java.net.URL): Request.Builder = Request.Builder().url(url).header("Authorization", auth)

        val lock = withResponse(
            request(target).header("Depth", "0").header("Timeout", "Second-600")
                .method("LOCK", LOCK_BODY.toRequestBody("application/xml; charset=utf-8".toMediaType())).build(),
        ) { response ->
            requireStatus(response, 200, 201)
            response.header("Lock-Token")?.trim()?.takeIf(LOCK_TOKEN::matches)
                ?: throw IOException("WebDAV server did not provide a valid exclusive lock")
        }
        var temporaryCreated = false
        var moved = false
        try {
            val expectedSize = expectedOriginal.length()
            val expectedDigest = expectedOriginal.inputStream().use { digest(it, expectedSize) }
            val remoteDigest = withResponse(request(target).get().header("Cache-Control", "no-cache").build()) { response ->
                requireStatus(response, 200)
                response.body?.byteStream()?.use { digest(it, expectedSize) }
                    ?: throw IOException("WebDAV file has no response body")
            }
            if (!MessageDigest.isEqual(expectedDigest, remoteDigest)) throw IOException("WebDAV file changed during editing")
            recordTemporary(temporaryPath)
            withResponse(
                request(temporary).header("If-None-Match", "*")
                    .put(replacement.asRequestBody("application/octet-stream".toMediaType())).build(),
            ) { response ->
                if (response.code == 412) resolveTemporary()
                requireStatus(response, 200, 201, 204)
            }
            temporaryCreated = true
            val replacementSize = replacement.length()
            val replacementDigest = replacement.inputStream().use { digest(it, replacementSize) }
            val stagedDigest = withResponse(request(temporary).get().header("Cache-Control", "no-cache").build()) { response ->
                requireStatus(response, 200)
                response.body?.byteStream()?.use { digest(it, replacementSize) }
                    ?: throw IOException("WebDAV temporary file has no response body")
            }
            if (!MessageDigest.isEqual(replacementDigest, stagedDigest)) {
                throw IOException("WebDAV temporary file does not match the replacement")
            }
            onCommitStarting()
            withResponse(
                request(temporary).header("Destination", target.toString()).header("Overwrite", "T")
                    .header("If", "<$target> ($lock)").method("MOVE", null).build(),
            ) { response -> requireStatus(response, 200, 201, 204) }
            moved = true
            resolveTemporary()
        } finally {
            try {
                if (temporaryCreated && !moved) {
                    try {
                        cleanupTemporary(source, credentials, relative, temporaryPath, operationId,
                            listOf(replacement), onCommitStarting)
                        resolveTemporary()
                    } catch (_: IOException) {
                        // The journal retains the temporary path for a later cleanup attempt.
                    } catch (_: IllegalStateException) {
                        // A changed source must not be touched during cleanup.
                    }
                }
            } finally {
                try {
                    withResponse(request(target).header("Lock-Token", lock).method("UNLOCK", null).build()) {
                        requireStatus(it, 200, 204)
                    }
                } catch (_: IOException) {
                    // The server's lock timeout bounds an interrupted unlock.
                }
            }
        }
    }

    fun cleanupTemporary(
        source: NetworkLibrarySource,
        credentials: NetworkCredentials,
        targetPath: String,
        temporaryPath: String,
        operationId: String,
        expectedFiles: List<File>,
        onDeleteStarting: () -> Unit,
    ) {
        val target = NetworkPathPolicy.validateRelativePath(targetPath)?.takeIf(String::isNotBlank)
            ?: throw IOException("Invalid WebDAV file path")
        val root = NetworkPathPolicy.validateRelativePath(source.shareOrPath)
            ?: throw IOException("Invalid WebDAV root path")
        if (root.isNotEmpty() && !target.startsWith("$root/")) throw IOException("WebDAV file is outside the source")
        if (!OPERATION_ID.matches(operationId)) throw IOException("Invalid WebDAV operation identifier")
        val directory = target.substringBeforeLast('/', "")
        val allowed = setOf(
            NetworkPathPolicy.join(directory, ".elovaire-$operationId.tmp"),
            NetworkPathPolicy.join(directory, ".elovaire-rollback-$operationId.tmp"),
        )
        if (temporaryPath !in allowed) throw IOException("Invalid WebDAV recovery temporary path")
        val expected = expectedFiles.map { file ->
            if (!file.isFile || file.length() <= 0L) throw IOException("WebDAV recovery file is unavailable")
            file.length() to file.inputStream().use { digest(it, file.length()) }
        }
        if (expected.isEmpty()) throw IOException("WebDAV recovery verification is unavailable")
        val url = NetworkPathPolicy.webDavResourceUrl(source.server, temporaryPath)
            ?: throw IOException("Invalid WebDAV temporary path")
        val auth = okhttp3.Credentials.basic(credentials.username, credentials.password, Charsets.UTF_8)
        fun request(): Request.Builder = Request.Builder().url(url).header("Authorization", auth)
        val tag = withResponse(request().get().header("Cache-Control", "no-cache").build()) { response ->
            if (response.code == 404) return@withResponse null
            requireStatus(response, 200)
            val etag = response.header("ETag")?.trim()?.takeIf(STRONG_ETAG::matches)
                ?: throw IOException("WebDAV temporary file has no strong ETag")
            val (size, hash) = response.body?.byteStream()?.use { digestUpTo(it, expected.maxOf { item -> item.first }) }
                ?: throw IOException("WebDAV temporary file has no response body")
            if (expected.none { it.first == size && MessageDigest.isEqual(it.second, hash) }) {
                throw IOException("WebDAV temporary file does not match recovery data")
            }
            etag
        } ?: return
        onDeleteStarting()
        withResponse(request().header("If-Match", tag).delete().build()) { response ->
            if (response.code != 404) requireStatus(response, 200, 204)
        }
    }

    private fun <T> withResponse(request: Request, block: (Response) -> T): T {
        if (closed.get()) throw IOException("WebDAV writer is closed")
        val call = client.newCall(request)
        calls.add(call)
        try {
            if (closed.get()) {
                call.cancel()
                throw IOException("WebDAV writer is closed")
            }
            return call.execute().use(block)
        } finally {
            calls.remove(call)
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) calls.toList().forEach(Call::cancel)
    }

    private fun requireStatus(response: Response, vararg accepted: Int) {
        if (response.code !in accepted) throw WebDavHttpException(response.code)
    }

    private fun digest(input: InputStream, expectedSize: Long): ByteArray {
        val (size, hash) = digestUpTo(input, expectedSize)
        if (size != expectedSize) throw IOException("WebDAV file changed during editing")
        return hash
    }

    private fun digestUpTo(input: InputStream, maximumSize: Long): Pair<Long, ByteArray> {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            if (Thread.currentThread().isInterrupted) throw IOException("WebDAV write was interrupted")
            val count = input.read(buffer)
            if (count < 0) {
                return total to digest.digest()
            }
            if (count == 0) throw IOException("WebDAV read made no progress")
            total += count
            if (total > maximumSize) throw IOException("WebDAV file exceeds its expected size")
            digest.update(buffer, 0, count)
        }
    }

    private companion object {
        val OPERATION_ID = Regex("[A-Za-z0-9_-]{1,80}")
        val LOCK_TOKEN = Regex("<[^<>\\s]+>")
        val STRONG_ETAG = Regex("\"[^\"\\x00-\\x20\\x7f]*\"")
        const val LOCK_BODY = """<?xml version="1.0" encoding="utf-8"?><D:lockinfo xmlns:D="DAV:"><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockinfo>"""
    }
}
