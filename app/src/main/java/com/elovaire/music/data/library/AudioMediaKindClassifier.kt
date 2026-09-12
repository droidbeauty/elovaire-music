package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.domain.model.AudioMediaKind
import java.util.Locale

/** A conservative, deterministic classifier shared by every library source. */
internal object AudioMediaKindClassifier {
    fun classify(
        isAudiobook: Boolean?,
        extension: String?,
        relativePath: String?,
        absolutePath: String?,
        sourcePath: String? = null,
    ): AudioMediaKind {
        if (isAudiobook == true) return AudioMediaKind.Audiobook
        if (extension.normalizeExtension() == "m4b") return AudioMediaKind.Audiobook
        return if (
            isAudiobooksPath(relativePath) ||
            isAudiobooksPath(absolutePath) ||
            isAudiobooksPath(sourcePath)
        ) {
            AudioMediaKind.Audiobook
        } else {
            AudioMediaKind.Music
        }
    }

    private fun isAudiobooksPath(path: String?): Boolean {
        val normalized = path
            ?.replace('\\', '/')
            ?.trim('/')
            ?.lowercase(Locale.ROOT)
            ?: return false
        var segmentStart = 0
        while (segmentStart < normalized.length) {
            val segmentEnd = normalized.indexOf('/', segmentStart).let { separator ->
                if (separator == -1) normalized.length else separator
            }
            if (
                segmentEnd - segmentStart == AUDIOBOOKS_SEGMENT_LENGTH &&
                normalized.regionMatches(segmentStart, AUDIOBOOKS_SEGMENT, 0, AUDIOBOOKS_SEGMENT_LENGTH)
            ) {
                return true
            }
            if (segmentEnd == normalized.length) return false
            segmentStart = segmentEnd + 1
        }
        return false
    }

    private const val AUDIOBOOKS_SEGMENT = "audiobooks"
    private const val AUDIOBOOKS_SEGMENT_LENGTH = 10

    private fun String?.normalizeExtension(): String? = this
        ?.trim()
        ?.removePrefix(".")
        ?.lowercase(Locale.ROOT)
        ?.takeIf(String::isNotBlank)
}
