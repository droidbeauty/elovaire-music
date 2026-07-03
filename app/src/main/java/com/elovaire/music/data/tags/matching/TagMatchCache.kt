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
        preferences.edit().putString(fingerprintKey(signature), fingerprint).apply()
    }

    @Synchronized
    fun getResponse(key: String): String? = responseCache[key.sha256()]

    @Synchronized
    fun putResponse(key: String, value: String) {
        responseCache[key.sha256()] = value
    }

    private fun fingerprintKey(signature: String): String = "fp_${signature.sha256()}"

    private fun String.sha256(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val PREFERENCES_NAME = "tag_match_cache"
        const val MAX_RESPONSE_ENTRIES = 48
    }
}
