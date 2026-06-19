package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import java.util.Locale

internal data class AudioScanCandidate(
    val id: Long,
    val uri: Uri,
    val displayName: String?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val mimeType: String?,
    val relativePath: String?,
    val absolutePath: String?,
    val extension: String?,
    val isMusic: Boolean?,
)

internal sealed interface AudioFileFilterDecision {
    data object Include : AudioFileFilterDecision

    data class Exclude(
        val reason: String,
    ) : AudioFileFilterDecision
}

internal class LibraryAudioFileFilter(
    private val preferredMusicFolderPath: String?,
    private val preferredRelativeRoots: Set<String>,
    private val libraryRootPaths: Set<String>,
    private val supportedExtensions: Set<String>,
) {
    fun evaluate(candidate: AudioScanCandidate): AudioFileFilterDecision {
        val normalizedExtension = candidate.extension
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
            ?: return AudioFileFilterDecision.Exclude("Missing extension")
        if (normalizedExtension !in supportedExtensions) {
            return AudioFileFilterDecision.Exclude("Unsupported extension")
        }

        val normalizedAbsolutePath = candidate.absolutePath.normalizeAbsolutePath()
        val normalizedRelativePath = candidate.relativePath.normalizeRelativePath()
        val insidePreferredFolder = isInsidePreferredFolder(
            normalizedAbsolutePath = normalizedAbsolutePath,
            normalizedRelativePath = normalizedRelativePath,
        )

        if (candidate.durationMs <= 0L) {
            return AudioFileFilterDecision.Exclude("Invalid duration")
        }

        if (!insidePreferredFolder && candidate.durationMs < MIN_MUSIC_DURATION_MS) {
            return AudioFileFilterDecision.Exclude("Too short")
        }

        val combinedPath = buildCombinedPath(
            normalizedRelativePath = normalizedRelativePath,
            normalizedAbsolutePath = normalizedAbsolutePath,
        )
        if (!insidePreferredFolder && ExcludedPathFragments.any(combinedPath::contains)) {
            return AudioFileFilterDecision.Exclude("Excluded path")
        }

        if (!insidePreferredFolder && matchesExcludedName(candidate)) {
            return AudioFileFilterDecision.Exclude("Excluded name")
        }

        if (!insidePreferredFolder && candidate.isMusic == false && !isInsideLibraryRoot(normalizedAbsolutePath)) {
            return AudioFileFilterDecision.Exclude("MediaStore says non-music")
        }

        return AudioFileFilterDecision.Include
    }

    private fun isInsidePreferredFolder(
        normalizedAbsolutePath: String?,
        normalizedRelativePath: String?,
    ): Boolean {
        val preferredPath = preferredMusicFolderPath.normalizeAbsolutePath()
        if (preferredPath != null && normalizedAbsolutePath != null) {
            if (normalizedAbsolutePath == preferredPath || normalizedAbsolutePath.startsWith("$preferredPath/")) {
                return true
            }
        }
        if (normalizedRelativePath != null) {
            return preferredRelativeRoots.any { preferredRoot ->
                normalizedRelativePath == preferredRoot || normalizedRelativePath.startsWith("$preferredRoot/")
            }
        }
        return false
    }

    private fun isInsideLibraryRoot(normalizedAbsolutePath: String?): Boolean {
        if (normalizedAbsolutePath == null) return false
        return libraryRootPaths.any { root ->
            normalizedAbsolutePath == root || normalizedAbsolutePath.startsWith("$root/")
        }
    }

    private fun matchesExcludedName(candidate: AudioScanCandidate): Boolean {
        val normalizedDisplayName = candidate.displayName.orEmpty().lowercase(Locale.ROOT)
        val normalizedTitle = candidate.title.orEmpty().lowercase(Locale.ROOT)
        return ExcludedNameRegexes.any { regex ->
            regex.containsMatchIn(normalizedDisplayName) || regex.containsMatchIn(normalizedTitle)
        }
    }

    private fun buildCombinedPath(
        normalizedRelativePath: String?,
        normalizedAbsolutePath: String?,
    ): String {
        return buildString {
            normalizedRelativePath?.let {
                append('/')
                append(it)
                append('/')
            }
            normalizedAbsolutePath?.let {
                append('/')
                append(it)
                append('/')
            }
        }
    }

    private fun String?.normalizeAbsolutePath(): String? {
        return this
            ?.trim()
            ?.replace('\\', '/')
            ?.trimEnd('/')
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
    }

    private fun String?.normalizeRelativePath(): String? {
        return this
            ?.trim()
            ?.replace('\\', '/')
            ?.trim('/')
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        private const val MIN_MUSIC_DURATION_MS = 45_000L

        private val ExcludedPathFragments = listOf(
            "/ringtones/",
            "/notifications/",
            "/alarms/",
            "/recordings/",
            "/recorder/",
            "/voice recorder/",
            "/call recordings/",
            "/call recorder/",
            "/whatsapp/",
            "/whatsapp audio/",
            "/whatsapp voice notes/",
            "/messenger/",
            "/telegram/",
            "/signal/",
            "/viber/",
            "/discord/",
            "/instagram/",
            "/facebook/",
            "/snapchat/",
            "/podcasts/",
            "/audiobooks/",
        )

        private val ExcludedNameRegexes = listOf(
            Regex("""\bvoice note\b"""),
            Regex("""\bcall recording\b"""),
            Regex("""\baudio record(?:ing)?\b"""),
            Regex("""\bwhatsapp (?:audio|ptt)\b"""),
            Regex("""^ptt-"""),
            Regex("""^opus_"""),
            Regex("""^recording[\s_-]"""),
            Regex("""^voice[\s_-]?record(?:ing)?"""),
        )
    }
}
