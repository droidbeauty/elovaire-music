package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.util.Base64
import java.security.GeneralSecurityException
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
        require(key.isNotBlank() && key.length <= MAX_KEY_LENGTH)
        val plaintext = JSONObject()
            .put("username", credentials.username)
            .put("password", credentials.password)
            .put("domain", credentials.domain.orEmpty())
            .toString()
            .toByteArray(Charsets.UTF_8)
        synchronized(lock) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(createIfMissing = true))
            cipher.updateAAD(key.toByteArray(Charsets.UTF_8))
            val ciphertext = cipher.doFinal(plaintext)
            val value = NetworkCredentialEnvelope.encode(cipher.iv, ciphertext)
            check(
                preferences.edit()
                    .putString(key, Base64.encodeToString(value, Base64.NO_WRAP))
                    .commit(),
            ) { "Unable to persist network credentials" }
        }
    }

    fun read(key: String): NetworkCredentialReadResult {
        require(key.isNotBlank() && key.length <= MAX_KEY_LENGTH)
        return synchronized(lock) {
            val encoded = preferences.getString(key, null)
                ?: return@synchronized NetworkCredentialReadResult.Missing
            val bytes = try {
                Base64.decode(encoded, Base64.NO_WRAP)
            } catch (_: IllegalArgumentException) {
                return@synchronized NetworkCredentialReadResult.Corrupt(
                    NetworkCredentialCorruption.InvalidBase64,
                )
            }
            val envelope = NetworkCredentialEnvelope.decode(bytes)
            val (iv, ciphertext, legacy) = when (envelope) {
                is NetworkCredentialEnvelopeResult.Current -> Triple(envelope.iv, envelope.ciphertext, false)
                is NetworkCredentialEnvelopeResult.Legacy -> Triple(envelope.iv, envelope.ciphertext, true)
                is NetworkCredentialEnvelopeResult.Invalid -> {
                    return@synchronized NetworkCredentialReadResult.Corrupt(
                        NetworkCredentialCorruption.InvalidEnvelope(envelope.reason),
                    )
                }
            }
            val keyMaterial = try {
                secretKey(createIfMissing = false)
            } catch (_: GeneralSecurityException) {
                return@synchronized NetworkCredentialReadResult.KeyUnavailable
            } catch (_: RuntimeException) {
                return@synchronized NetworkCredentialReadResult.KeyUnavailable
            }
            val credentials = try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, keyMaterial, GCMParameterSpec(128, iv))
                if (!legacy) cipher.updateAAD(key.toByteArray(Charsets.UTF_8))
                val json = JSONObject(cipher.doFinal(ciphertext).toString(Charsets.UTF_8))
                NetworkCredentials(
                    username = json.optString("username"),
                    password = json.optString("password"),
                    domain = json.optString("domain").takeIf(String::isNotBlank),
                )
            } catch (_: GeneralSecurityException) {
                return@synchronized NetworkCredentialReadResult.Corrupt(
                    NetworkCredentialCorruption.AuthenticationFailed,
                )
            } catch (_: RuntimeException) {
                return@synchronized NetworkCredentialReadResult.Corrupt(
                    NetworkCredentialCorruption.MalformedPayload,
                )
            }
            if (legacy) {
                val current = encrypt(key, credentials, keyMaterial)
                if (preferences.getString(key, null) == encoded) {
                    preferences.edit()
                        .putString(key, Base64.encodeToString(current, Base64.NO_WRAP))
                        .commit()
                }
            }
            NetworkCredentialReadResult.Available(credentials)
        }
    }

    fun get(key: String): NetworkCredentials? = (read(key) as? NetworkCredentialReadResult.Available)?.credentials

    fun remove(key: String) {
        require(key.isNotBlank() && key.length <= MAX_KEY_LENGTH)
        synchronized(lock) {
            check(preferences.edit().remove(key).commit()) { "Unable to remove network credentials" }
        }
    }

    private fun secretKey(createIfMissing: Boolean): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        check(createIfMissing) { "Network credential key is unavailable" }
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

    private fun encrypt(key: String, credentials: NetworkCredentials, keyMaterial: SecretKey): ByteArray {
        val plaintext = JSONObject()
            .put("username", credentials.username)
            .put("password", credentials.password)
            .put("domain", credentials.domain.orEmpty())
            .toString()
            .toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyMaterial)
        cipher.updateAAD(key.toByteArray(Charsets.UTF_8))
        return NetworkCredentialEnvelope.encode(cipher.iv, cipher.doFinal(plaintext))
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "elovaire-network-credentials-v1"
        const val PREFERENCES = "network_credentials_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val MAX_KEY_LENGTH = 256
    }
}

internal sealed interface NetworkCredentialReadResult {
    data object Missing : NetworkCredentialReadResult
    data class Available(val credentials: NetworkCredentials) : NetworkCredentialReadResult
    data object KeyUnavailable : NetworkCredentialReadResult
    data class Corrupt(val reason: NetworkCredentialCorruption) : NetworkCredentialReadResult
}

internal sealed interface NetworkCredentialCorruption {
    data object InvalidBase64 : NetworkCredentialCorruption
    data class InvalidEnvelope(val reason: NetworkCredentialEnvelopeError) : NetworkCredentialCorruption
    data object AuthenticationFailed : NetworkCredentialCorruption
    data object MalformedPayload : NetworkCredentialCorruption
}
