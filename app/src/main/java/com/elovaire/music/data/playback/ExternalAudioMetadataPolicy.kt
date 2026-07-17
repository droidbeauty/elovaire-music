package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioFormatCapability
import java.util.Locale

internal object ExternalAudioMetadataPolicy {
    fun acceptsUri(scheme: String?, uriLength: Int): Boolean {
        return scheme in SUPPORTED_URI_SCHEMES && uriLength in 1..MAX_URI_LENGTH
    }

    fun acceptsDeclaredMimeType(mimeType: String?): Boolean {
        val normalized = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf(String::isNotBlank)
            ?: return true
        if (normalized.startsWith("audio/") || normalized == GENERIC_BINARY_MIME_TYPE) return true
        return AudioFormatPolicy.capabilityForMimeType(normalized) != null
    }

    fun sanitizeDisplayName(rawValue: String?): String {
        return rawValue
            ?.asSequence()
            ?.filterNot { it.isISOControl() }
            ?.joinToString(separator = "")
            ?.replace('/', ' ')
            ?.replace('\\', ' ')
            ?.trim()
            ?.take(MAX_DISPLAY_NAME_LENGTH)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DISPLAY_NAME
    }

    fun titleFromDisplayName(displayName: String): String {
        return displayName.substringBeforeLast('.', displayName)
            .trim()
            .ifBlank { displayName }
    }

    fun boundedDurationMs(rawValue: String?): Long {
        val durationMs = rawValue
            ?.trim()
            ?.toLongOrNull()
            ?: return 0L
        return durationMs.takeIf { it in 1L..MAX_EXTERNAL_DURATION_MS } ?: 0L
    }

    fun resolveCapability(
        displayName: String,
        pathSegment: String?,
        mimeType: String?,
    ): AudioFormatCapability? {
        val extension = sequenceOf(displayName, pathSegment.orEmpty())
            .mapNotNull(::extensionFromName)
            .firstOrNull()
        val extensionCapability = extension?.let(AudioFormatPolicy::capabilityForExtension)
        val normalizedMime = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase(Locale.ROOT)
        val mimeCapability = AudioFormatPolicy.capabilityForMimeType(normalizedMime)
        return extensionCapability ?: mimeCapability
    }

    private fun extensionFromName(name: String): String? {
        return name.substringAfterLast('.', "")
            .lowercase(Locale.ROOT)
            .takeIf(String::isNotBlank)
    }

    private const val DEFAULT_DISPLAY_NAME = "External audio"
    private const val MAX_URI_LENGTH = 4_096
    private const val MAX_DISPLAY_NAME_LENGTH = 160
    private const val MAX_EXTERNAL_DURATION_MS = 7L * 24L * 60L * 60L * 1_000L
    private const val GENERIC_BINARY_MIME_TYPE = "application/octet-stream"
    private val SUPPORTED_URI_SCHEMES = setOf("content", "file")
}
