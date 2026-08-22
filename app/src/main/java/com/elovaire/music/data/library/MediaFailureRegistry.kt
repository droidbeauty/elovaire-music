package elovaire.music.droidbeauty.app.data.library

import java.util.LinkedHashMap

internal enum class MediaFailureDomain {
    Metadata,
    EmbeddedTags,
    Artwork,
    FormatProbe,
    LyricsRead,
    PlaybackProbe,
}

internal enum class MediaFailureCategory {
    Unsupported,
    Malformed,
    TransientIo,
    Permission,
    Missing,
    Resource,
    Unknown,
}

internal data class MediaFailureKey(
    val identity: String,
    val revision: String,
    val domain: MediaFailureDomain,
)

internal data class MediaFailureRecord(
    val category: MediaFailureCategory,
    val attempts: Int,
    val nextRetryAtMs: Long,
)

/** In-memory suppression for repeatedly failing unchanged media; it never blacklists a file. */
internal class MediaFailureRegistry(
    private val nowMs: () -> Long = { System.currentTimeMillis() },
    private val maxEntries: Int = MAX_ENTRIES,
) {
    private val entries = object : LinkedHashMap<MediaFailureKey, MediaFailureRecord>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<MediaFailureKey, MediaFailureRecord>?): Boolean {
            return size > maxEntries
        }
    }

    @Synchronized
    fun shouldSuppress(key: MediaFailureKey, force: Boolean = false): Boolean {
        if (force) return false
        val record = entries[key] ?: return false
        return record.nextRetryAtMs > nowMs()
    }

    @Synchronized
    fun recordFailure(key: MediaFailureKey, category: MediaFailureCategory) {
        val previous = entries[key]
        val attempts = (previous?.attempts ?: 0) + 1
        val delayMs = when (attempts) {
            1 -> 0L
            2 -> FIRST_BACKOFF_MS
            else -> (FIRST_BACKOFF_MS * (1L shl (attempts - 2).coerceAtMost(5))).coerceAtMost(MAX_BACKOFF_MS)
        }
        entries[key] = MediaFailureRecord(
            category = category,
            attempts = attempts,
            nextRetryAtMs = nowMs() + delayMs,
        )
    }

    @Synchronized
    fun recordSuccess(key: MediaFailureKey) {
        entries.remove(key)
    }

    @Synchronized
    fun clearIdentity(identity: String) {
        entries.keys.removeAll { it.identity == identity }
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    @Synchronized
    fun size(): Int = entries.size

    companion object {
        private const val MAX_ENTRIES = 256
        private const val FIRST_BACKOFF_MS = 30_000L
        private const val MAX_BACKOFF_MS = 30L * 60L * 1_000L
    }
}

internal fun mediaFailureCategory(failure: Throwable): MediaFailureCategory {
    return when (failure) {
        is SecurityException -> MediaFailureCategory.Permission
        is java.io.FileNotFoundException -> MediaFailureCategory.Missing
        is java.io.IOException -> MediaFailureCategory.TransientIo
        is IllegalArgumentException -> MediaFailureCategory.Malformed
        else -> MediaFailureCategory.Unknown
    }
}
