package elovaire.music.droidbeauty.app.data.artist

import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

internal data class ArtistImageCacheEntry(
    val artistKey: String,
    val providerArtistId: String?,
    val providerName: String?,
    val musicBrainzArtistId: String?,
    val imageFileName: String?,
    val imageUrl: String?,
    val fetchedAtMs: Long,
    val negativeUntilMs: Long,
    val etag: String?,
    val lastModified: String?,
)

internal class ArtistImageCache(
    context: Context,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private val directory = File(context.cacheDir, CACHE_DIRECTORY_NAME)
    private val indexFile = File(directory, INDEX_FILE_NAME)
    private val mutex = Mutex()
    private var loaded = false
    private val entries = linkedMapOf<String, ArtistImageCacheEntry>()

    suspend fun get(artistKey: String): ArtistImageCacheEntry? = mutex.withLock {
        loadLocked()
        val entry = entries[artistKey] ?: return@withLock null
        if (entry.imageFileName == null) return@withLock entry
        val imageFile = File(directory, entry.imageFileName)
        if (!isValidImageFile(imageFile)) {
            imageFile.delete()
            entries.remove(artistKey)
            persistLocked()
            return@withLock null
        }
        imageFile.setLastModified(nowMs())
        entry
    }

    suspend fun putImage(
        artistKey: String,
        match: ArtistImageRemoteMatch,
        bytes: ByteArray,
        etag: String?,
        lastModified: String?,
    ): ArtistImageCacheEntry? = mutex.withLock {
        loadLocked()
        if (bytes.isEmpty()) return@withLock null
        directory.mkdirs()
        val fileName = "${stableHash(artistKey)}.img"
        val destination = File(directory, fileName)
        val temporary = File(directory, "$fileName.tmp")
        if (!writeAtomically(temporary, destination, bytes)) return@withLock null
        val entry = ArtistImageCacheEntry(
            artistKey = artistKey,
            providerArtistId = match.providerArtistId,
            providerName = match.providerName,
            musicBrainzArtistId = match.musicBrainzArtistId,
            imageFileName = fileName,
            imageUrl = match.imageUrl,
            fetchedAtMs = nowMs(),
            negativeUntilMs = 0L,
            etag = etag,
            lastModified = lastModified,
        )
        entries[artistKey] = entry
        evictLocked()
        persistLocked()
        entry.takeIf { destination.isFile }
    }

    suspend fun putNegative(artistKey: String, expiresAtMs: Long) = mutex.withLock {
        loadLocked()
        val previous = entries[artistKey]
        entries[artistKey] = ArtistImageCacheEntry(
            artistKey = artistKey,
            providerArtistId = previous?.providerArtistId,
            providerName = previous?.providerName,
            musicBrainzArtistId = previous?.musicBrainzArtistId,
            imageFileName = previous?.imageFileName,
            imageUrl = previous?.imageUrl,
            fetchedAtMs = previous?.fetchedAtMs ?: 0L,
            negativeUntilMs = expiresAtMs,
            etag = previous?.etag,
            lastModified = previous?.lastModified,
        )
        persistLocked()
    }

    suspend fun markValidated(artistKey: String): ArtistImageCacheEntry? = mutex.withLock {
        loadLocked()
        val previous = entries[artistKey] ?: return@withLock null
        val updated = previous.copy(
            fetchedAtMs = nowMs(),
            negativeUntilMs = 0L,
        )
        entries[artistKey] = updated
        persistLocked()
        updated
    }

    suspend fun imageUri(entry: ArtistImageCacheEntry): Uri? = mutex.withLock {
        loadLocked()
        entry.imageFileName
            ?.let { File(directory, it) }
            ?.takeIf(::isValidImageFile)
            ?.let(Uri::fromFile)
    }

    private fun loadLocked() {
        if (loaded) return
        loaded = true
        if (!indexFile.isFile) return
        val parsed = try {
            JSONObject(indexFile.readText())
        } catch (_: IOException) {
            return
        } catch (_: SecurityException) {
            return
        } catch (_: org.json.JSONException) {
            return
        }
        val array = parsed.optJSONArray("entries") ?: JSONArray()
        (0 until array.length()).forEach { index ->
            val json = array.optJSONObject(index) ?: return@forEach
            val key = json.optString("artistKey").takeIf(String::isNotBlank) ?: return@forEach
            entries[key] = ArtistImageCacheEntry(
                artistKey = key,
                providerArtistId = json.optString("providerArtistId").takeIf(String::isNotBlank),
                providerName = json.optString("providerName").takeIf(String::isNotBlank),
                musicBrainzArtistId = json.optString("musicBrainzArtistId").takeIf(String::isNotBlank),
                imageFileName = json.optString("imageFileName").takeIf(String::isNotBlank),
                imageUrl = json.optString("imageUrl").takeIf(String::isNotBlank),
                fetchedAtMs = json.optLong("fetchedAtMs", 0L),
                negativeUntilMs = json.optLong("negativeUntilMs", 0L),
                etag = json.optString("etag").takeIf(String::isNotBlank),
                lastModified = json.optString("lastModified").takeIf(String::isNotBlank),
            )
        }
    }

    private fun persistLocked() {
        directory.mkdirs()
        val root = JSONObject().apply {
            put("entries", JSONArray().apply { entries.values.forEach { put(it.toJson()) } })
        }
        val temporary = File(directory, "$INDEX_FILE_NAME.tmp")
        try {
            FileOutputStream(temporary).use { output ->
                output.write(root.toString().toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            if (!temporary.renameTo(indexFile)) {
                temporary.delete()
            }
        } catch (_: IOException) {
            temporary.delete()
        } catch (_: SecurityException) {
            temporary.delete()
        }
    }

    private fun evictLocked() {
        val files = directory.listFiles { file -> file.extension == IMAGE_EXTENSION }.orEmpty()
        var totalBytes = files.sumOf(File::length)
        if (totalBytes <= MAX_CACHE_BYTES) return
        files.sortedBy(File::lastModified).forEach { file ->
            if (totalBytes <= MAX_CACHE_BYTES) return@forEach
            val removed = file.length()
            if (file.delete()) totalBytes -= removed
            entries.entries.removeIf { it.value.imageFileName == file.name }
        }
    }

    private fun ArtistImageCacheEntry.toJson(): JSONObject = JSONObject().apply {
        put("artistKey", artistKey)
        providerArtistId?.let { put("providerArtistId", it) }
        providerName?.let { put("providerName", it) }
        musicBrainzArtistId?.let { put("musicBrainzArtistId", it) }
        imageFileName?.let { put("imageFileName", it) }
        imageUrl?.let { put("imageUrl", it) }
        put("fetchedAtMs", fetchedAtMs)
        put("negativeUntilMs", negativeUntilMs)
        etag?.let { put("etag", it) }
        lastModified?.let { put("lastModified", it) }
    }

    private fun writeAtomically(temporary: File, destination: File, bytes: ByteArray): Boolean {
        return try {
            FileOutputStream(temporary).use { output ->
                output.write(bytes)
                output.fd.sync()
            }
            if (!temporary.renameTo(destination)) {
                temporary.delete()
                false
            } else {
                true
            }
        } catch (_: IOException) {
            temporary.delete()
            false
        } catch (_: SecurityException) {
            temporary.delete()
            false
        }
    }

    private fun isValidImageFile(file: File): Boolean {
        if (!file.isFile || file.length() <= 0L || file.length() > MAX_IMAGE_BYTES) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        return isArtworkBoundsSafe(bounds.outWidth, bounds.outHeight)
    }

    private companion object {
        const val CACHE_DIRECTORY_NAME = "artist-images"
        const val INDEX_FILE_NAME = "index.json"
        const val IMAGE_EXTENSION = "img"
        const val MAX_CACHE_BYTES = 16L * 1024L * 1024L
        const val MAX_IMAGE_BYTES = 4L * 1024L * 1024L
    }
}

internal fun stableArtistCacheKey(artistName: String): String? {
    val normalized = normalizeArtistIdentity(artistName)
    if (normalized.isBlank() || isPseudoArtistName(normalized)) return null
    return normalized
}

internal fun stableArtistCacheFileHash(artistName: String): String? {
    return stableArtistCacheKey(artistName)?.let(::stableHash)
}

private fun stableHash(value: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
}

private fun isArtworkBoundsSafe(width: Int, height: Int): Boolean {
    return width in 1..8_192 && height in 1..8_192 && width.toLong() * height <= 40_000_000L
}
