package elovaire.music.droidbeauty.app.data.playback

import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.AudioFormatCapability
import java.util.Locale

internal object ExternalAudioMetadataPolicy {
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
        val mimeCapability = normalizedMime?.let { mime ->
            AudioFormatPolicy.capabilities.firstOrNull { capability -> mime in capability.mimeTypes }
        }
        return extensionCapability ?: mimeCapability
    }

    private fun extensionFromName(name: String): String? {
        return name.substringAfterLast('.', "")
            .lowercase(Locale.ROOT)
            .takeIf(String::isNotBlank)
    }

    private const val DEFAULT_DISPLAY_NAME = "External audio"
    private const val MAX_DISPLAY_NAME_LENGTH = 160
    private const val MAX_EXTERNAL_DURATION_MS = 7L * 24L * 60L * 60L * 1_000L
}
