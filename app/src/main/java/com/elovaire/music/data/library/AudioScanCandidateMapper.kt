package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.data.audio.DetectionEvidence

internal object AudioScanCandidateMapper {
    fun fastDetectedFormat(
        extension: String,
        mimeType: String?,
    ): DetectedAudioFormat {
        val container = AudioFormatPolicy.resolveContainer(extension, mimeType, null)
        return DetectedAudioFormat(
            container = container,
            displayName = AudioFormatPolicy.displayName(container, extension),
            mimeType = mimeType,
            codecMimeType = null,
            detectionSucceeded = false,
            hasAudioTrack = true,
            hasVideoTrack = false,
            decoderAvailable = null,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
            evidence = if (mimeType.isNullOrBlank()) {
                DetectionEvidence.ExtensionFallback
            } else {
                DetectionEvidence.ProviderMime
            },
        )
    }

    fun toCandidate(
        row: MediaStoreAudioRow,
        detectedFormat: DetectedAudioFormat?,
    ): AudioScanCandidate {
        return AudioScanCandidate(
            id = row.id,
            uri = row.uri,
            displayName = row.fileName,
            title = row.title,
            artist = row.artist,
            album = row.album,
            durationMs = row.durationMs,
            mimeType = row.mimeType,
            relativePath = row.relativePath,
            absolutePath = row.filePath,
            extension = row.extension,
            isMusic = row.isMusic,
            detectedFormat = detectedFormat,
        )
    }
}
