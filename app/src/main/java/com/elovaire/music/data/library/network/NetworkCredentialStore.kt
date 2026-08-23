package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONObject

/** Stores network credentials as ciphertext; the clear-text password never enters preferences. */
internal class NetworkCredentialStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val lock = Any()

    fun put(key: String, credentials: NetworkCredentials) {
        require(key.isNotBlank())
        val plaintext = JSONObject()
            .put("username", credentials.username)
            .put("password", credentials.password)
            .put("domain", credentials.domain.orEmpty())
            .toString()
            .toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(plaintext)
        val value = ByteBuffer.allocate(4 + cipher.iv.size + ciphertext.size)
        value.putInt(cipher.iv.size).put(cipher.iv).put(ciphertext)
        synchronized(lock) {
            check(
                preferences.edit()
                    .putString(key, Base64.encodeToString(value.array(), Base64.NO_WRAP))
                    .commit(),
            ) { "Unable to persist network credentials" }
        }
    }

    fun get(key: String): NetworkCredentials? {
        val encoded = synchronized(lock) { preferences.getString(key, null) } ?: return null
        return runCatching {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            val buffer = ByteBuffer.wrap(bytes)
            val ivSize = buffer.int
            require(ivSize in 12..32)
            val iv = ByteArray(ivSize).also(buffer::get)
            val ciphertext = ByteArray(buffer.remaining()).also(buffer::get)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            val json = JSONObject(cipher.doFinal(ciphertext).toString(Charsets.UTF_8))
            NetworkCredentials(
                username = json.optString("username"),
                password = json.optString("password"),
                domain = json.optString("domain").takeIf(String::isNotBlank),
            )
        }.getOrNull()
    }

    fun remove(key: String) {
        synchronized(lock) {
            check(preferences.edit().remove(key).commit()) { "Unable to remove network credentials" }
        }
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "elovaire-network-credentials-v1"
        const val PREFERENCES = "network_credentials_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
