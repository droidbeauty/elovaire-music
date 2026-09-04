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
        return if (listOf(relativePath, absolutePath, sourcePath).any(::isAudiobooksPath)) {
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
        return normalized.split('/').any { it == "audiobooks" }
    }

    private fun String?.normalizeExtension(): String? = this
        ?.trim()
        ?.removePrefix(".")
        ?.lowercase(Locale.ROOT)
        ?.takeIf(String::isNotBlank)
}
