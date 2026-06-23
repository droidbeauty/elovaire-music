package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.PlaybackSupport
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
    val detectedFormat: DetectedAudioFormat? = null,
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
) {
    fun evaluate(candidate: AudioScanCandidate): AudioFileFilterDecision {
        val normalizedExtension = candidate.extension
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
            ?: return AudioFileFilterDecision.Exclude("Missing extension")
        if (normalizedExtension !in AudioFormatPolicy.scannerExtensions) {
            return AudioFileFilterDecision.Exclude("Unsupported extension")
        }

        val normalizedAbsolutePath = candidate.absolutePath.normalizeAbsolutePath()
        val normalizedRelativePath = candidate.relativePath.normalizeRelativePath()
        val insidePreferredFolder = isInsidePreferredFolder(
            normalizedAbsolutePath = normalizedAbsolutePath,
            normalizedRelativePath = normalizedRelativePath,
        )
        val capability = AudioFormatPolicy.capabilityForExtension(normalizedExtension)
            ?: return AudioFileFilterDecision.Exclude("Unsupported format")
        if (!insidePreferredFolder && normalizedExtension in VOICE_CONTAINER_EXTENSIONS) {
            return AudioFileFilterDecision.Exclude("Voice-oriented format outside preferred music folder")
        }
        val detectedFormat = candidate.detectedFormat
        if (detectedFormat?.detectionSucceeded == true) {
            if (!detectedFormat.hasAudioTrack) {
                return AudioFileFilterDecision.Exclude("No detectable audio track")
            }
            if (detectedFormat.hasVideoTrack) {
                return AudioFileFilterDecision.Exclude("Contains a video track")
            }
            if (AudioFormatPolicy.playbackSupport(detectedFormat) == PlaybackSupport.Unsupported) {
                return AudioFileFilterDecision.Exclude("No compatible audio decoder")
            }
        } else if (normalizedExtension in AudioFormatPolicy.validationRequiredExtensions) {
            return AudioFileFilterDecision.Exclude("Container could not be validated")
        }

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

        if (
            capability.playbackSupport == PlaybackSupport.PlatformDependent &&
            !insidePreferredFolder &&
            candidate.isMusic != true
        ) {
            return AudioFileFilterDecision.Exclude("Platform-dependent non-music audio")
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
        private val VOICE_CONTAINER_EXTENSIONS = setOf("amr", "3gp")

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
