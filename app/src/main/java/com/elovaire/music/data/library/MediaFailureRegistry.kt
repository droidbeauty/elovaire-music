package elovaire.music.droidbeauty.app.data.library

import android.os.SystemClock
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
    // Backoff is process-local elapsed time; wall-clock changes must not extend or skip it.
    private val nowMs: () -> Long = { SystemClock.elapsedRealtime() },
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
        val attempts = if (previous?.category == category) {
            (previous.attempts + 1).coerceAtMost(MAX_ATTEMPTS)
        } else {
            1
        }
        val delayMs = mediaFailureRetryDelayMs(category, attempts)
        entries[key] = MediaFailureRecord(
            category = category,
            attempts = attempts,
            nextRetryAtMs = saturatedAdd(nowMs(), delayMs),
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
    }
}

private fun saturatedAdd(value: Long, amount: Long): Long {
    return if (amount > 0L && value > Long.MAX_VALUE - amount) {
        Long.MAX_VALUE
    } else if (amount < 0L && value < Long.MIN_VALUE - amount) {
        Long.MIN_VALUE
    } else {
        value + amount
    }
}

internal fun mediaFailureRetryDelayMs(
    category: MediaFailureCategory,
    attempts: Int,
): Long {
    val safeAttempts = attempts.coerceIn(1, MAX_ATTEMPTS)
    if (safeAttempts == 1) return 0L
    val (baseDelayMs, maximumDelayMs) = when (category) {
        MediaFailureCategory.TransientIo -> 30_000L to 30L * 60L * 1_000L
        MediaFailureCategory.Resource -> 5_000L to 5L * 60L * 1_000L
        MediaFailureCategory.Permission -> 5_000L to 60L * 60L * 1_000L
        MediaFailureCategory.Missing -> 5L * 60L * 1_000L to 24L * 60L * 60L * 1_000L
        MediaFailureCategory.Unsupported -> 30L * 60L * 1_000L to 7L * 24L * 60L * 60L * 1_000L
        MediaFailureCategory.Malformed -> 10L * 60L * 1_000L to 6L * 60L * 60L * 1_000L
        MediaFailureCategory.Unknown -> 30_000L to 30L * 60L * 1_000L
    }
    val multiplier = 1L shl (safeAttempts - 2).coerceAtMost(20)
    return if (baseDelayMs > maximumDelayMs / multiplier) maximumDelayMs else {
        (baseDelayMs * multiplier).coerceAtMost(maximumDelayMs)
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

private const val MAX_ATTEMPTS = 1_000_000
