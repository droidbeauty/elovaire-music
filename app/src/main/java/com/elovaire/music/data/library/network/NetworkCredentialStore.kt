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

    fun put(sourceId: String, key: String, credentials: NetworkCredentials) {
        validateIdentity(sourceId, key)
        val plaintext = JSONObject()
            .put("sourceId", sourceId)
            .put("username", credentials.username)
            .put("password", credentials.password)
            .put("domain", credentials.domain.orEmpty())
            .toString()
            .toByteArray(Charsets.UTF_8)
        synchronized(lock) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(createIfMissing = true))
            cipher.updateAAD(binding(sourceId, key))
            val ciphertext = cipher.doFinal(plaintext)
            val value = NetworkCredentialEnvelope.encode(cipher.iv, ciphertext)
            check(
                preferences.edit()
                    .putString(key, Base64.encodeToString(value, Base64.NO_WRAP))
                    .commit(),
            ) { "Unable to persist network credentials" }
        }
    }

    fun read(sourceId: String, key: String): NetworkCredentialReadResult {
        validateIdentity(sourceId, key)
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
            val firstAttempt = decrypt(
                sourceId = sourceId,
                key = key,
                iv = iv,
                ciphertext = ciphertext,
                keyMaterial = keyMaterial,
                useLegacyBinding = legacy,
            )
            val decrypted = when {
                firstAttempt is DecryptionResult.Success -> firstAttempt
                firstAttempt is DecryptionResult.MalformedPayload -> {
                    return@synchronized NetworkCredentialReadResult.Corrupt(
                        NetworkCredentialCorruption.MalformedPayload,
                    )
                }
                !legacy -> {
                    // Version-2 entries created before credentials were bound to the owning
                    // source used only the credential key as AAD. Read them once, then upgrade
                    // them in place with source-bound AAD and an embedded owner id.
                    decrypt(
                        sourceId = sourceId,
                        key = key,
                        iv = iv,
                        ciphertext = ciphertext,
                        keyMaterial = keyMaterial,
                        useLegacyBinding = true,
                    )
                }
                else -> firstAttempt
            }
            val available = decrypted as? DecryptionResult.Success
                ?: return@synchronized NetworkCredentialReadResult.Corrupt(
                    if (decrypted is DecryptionResult.MalformedPayload) {
                        NetworkCredentialCorruption.MalformedPayload
                    } else {
                        NetworkCredentialCorruption.AuthenticationFailed
                    },
                )
            val credentials = available.value.credentials
            if (available.value.needsMigration) {
                val current = encrypt(sourceId, key, credentials, keyMaterial)
                if (preferences.getString(key, null) == encoded) {
                    preferences.edit()
                        .putString(key, Base64.encodeToString(current, Base64.NO_WRAP))
                        .commit()
                }
            }
            NetworkCredentialReadResult.Available(credentials)
        }
    }

    fun get(sourceId: String, key: String): NetworkCredentials? =
        (read(sourceId, key) as? NetworkCredentialReadResult.Available)?.credentials

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

    private fun encrypt(
        sourceId: String,
        key: String,
        credentials: NetworkCredentials,
        keyMaterial: SecretKey,
    ): ByteArray {
        val plaintext = JSONObject()
            .put("sourceId", sourceId)
            .put("username", credentials.username)
            .put("password", credentials.password)
            .put("domain", credentials.domain.orEmpty())
            .toString()
            .toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyMaterial)
        cipher.updateAAD(binding(sourceId, key))
        return NetworkCredentialEnvelope.encode(cipher.iv, cipher.doFinal(plaintext))
    }

    private fun decrypt(
        sourceId: String,
        key: String,
        iv: ByteArray,
        ciphertext: ByteArray,
        keyMaterial: SecretKey,
        useLegacyBinding: Boolean,
    ): DecryptionResult {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keyMaterial, GCMParameterSpec(128, iv))
            cipher.updateAAD(if (useLegacyBinding) key.toByteArray(Charsets.UTF_8) else binding(sourceId, key))
            val json = try {
                JSONObject(cipher.doFinal(ciphertext).toString(Charsets.UTF_8))
            } catch (_: RuntimeException) {
                return DecryptionResult.MalformedPayload
            }
            val storedSourceId = json.optString("sourceId").takeIf(String::isNotBlank)
            if (!useLegacyBinding && storedSourceId != sourceId) return DecryptionResult.SourceMismatch
            if (useLegacyBinding && storedSourceId != null && storedSourceId != sourceId) {
                return DecryptionResult.SourceMismatch
            }
            DecryptionResult.Success(
                DecryptedCredentials(
                    credentials = NetworkCredentials(
                        username = json.optString("username"),
                        password = json.optString("password"),
                        domain = json.optString("domain").takeIf(String::isNotBlank),
                    ),
                    needsMigration = useLegacyBinding || storedSourceId == null,
                ),
            )
        } catch (_: GeneralSecurityException) {
            DecryptionResult.AuthenticationFailed
        } catch (_: RuntimeException) {
            DecryptionResult.AuthenticationFailed
        }
    }

    private fun validateIdentity(sourceId: String, key: String) {
        require(sourceId.isNotBlank() && sourceId.length <= MAX_SOURCE_ID_LENGTH)
        require(key.isNotBlank() && key.length <= MAX_KEY_LENGTH)
    }

    private fun binding(sourceId: String, key: String): ByteArray {
        return "$sourceId\u0000$key".toByteArray(Charsets.UTF_8)
    }

    private data class DecryptedCredentials(
        val credentials: NetworkCredentials,
        val needsMigration: Boolean,
    )

    private sealed interface DecryptionResult {
        data class Success(val value: DecryptedCredentials) : DecryptionResult
        data object AuthenticationFailed : DecryptionResult
        data object MalformedPayload : DecryptionResult
        data object SourceMismatch : DecryptionResult
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "elovaire-network-credentials-v1"
        const val PREFERENCES = "network_credentials_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val MAX_SOURCE_ID_LENGTH = 256
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
