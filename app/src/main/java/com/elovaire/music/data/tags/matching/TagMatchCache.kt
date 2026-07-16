package elovaire.music.droidbeauty.app.data.tags.matching

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import java.security.MessageDigest
import java.util.LinkedHashMap

internal class TagMatchCache(context: Context) {
    private val preferences = allowStrictModeDiskReads {
        // The tag-match cache keeps small fingerprints in SharedPreferences for immediate reuse.
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    private val responseCache = object : LinkedHashMap<String, String>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_RESPONSE_ENTRIES
        }
    }

    fun getFingerprint(signature: String): String? {
        return preferences.getString(fingerprintKey(signature), null)
    }

    fun putFingerprint(signature: String, fingerprint: String) {
        if (fingerprint.isBlank() || fingerprint.length > MAX_FINGERPRINT_CHARACTERS) return
        val key = fingerprintKey(signature)
        val editor = preferences.edit().putString(key, fingerprint)
        fingerprintKeysToTrim(
            keys = preferences.all.keys.filterTo(mutableSetOf()) { it.startsWith(FINGERPRINT_PREFIX) },
            incomingKey = key,
            maxEntries = MAX_FINGERPRINT_ENTRIES,
        ).forEach(editor::remove)
        editor.apply()
    }

    @Synchronized
    fun getResponse(key: String): String? = responseCache[key.sha256()]

    @Synchronized
    fun putResponse(key: String, value: String) {
        if (value.isBlank() || value.length > MAX_RESPONSE_CHARACTERS) return
        responseCache[key.sha256()] = value
    }

    private fun fingerprintKey(signature: String): String = "$FINGERPRINT_PREFIX${signature.sha256()}"

    private fun String.sha256(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val PREFERENCES_NAME = "tag_match_cache"
        const val FINGERPRINT_PREFIX = "fp_"
        const val MAX_FINGERPRINT_ENTRIES = 256
        const val MAX_FINGERPRINT_CHARACTERS = 64 * 1024
        const val MAX_RESPONSE_ENTRIES = 48
        const val MAX_RESPONSE_CHARACTERS = 1 * 1024 * 1024
    }
}

internal fun fingerprintKeysToTrim(
    keys: Set<String>,
    incomingKey: String,
    maxEntries: Int,
): List<String> {
    val projectedSize = keys.size + if (incomingKey in keys) 0 else 1
    val overflow = (projectedSize - maxEntries.coerceAtLeast(0)).coerceAtLeast(0)
    if (overflow == 0) return emptyList()
    return keys.asSequence()
        .filterNot { it == incomingKey }
        .sorted()
        .take(overflow)
        .toList()
}
