package elovaire.music.droidbeauty.app.data.audio

import android.net.Uri

internal data class MediaCapabilityReport(
    val fileName: String,
    val uri: Uri?,
    val discovered: Boolean,
    val indexedByMediaStore: Boolean,
    val foundThroughSaf: Boolean,
    val includedInLibrary: Boolean,
    val extension: String,
    val mediaStoreMimeType: String?,
    val detectedContainer: AudioContainerFormat,
    val detectedCodecMimeType: String?,
    val decoderAvailable: Boolean?,
    val softwareDecoderFallbackAvailable: Boolean?,
    val hasAudioTrack: Boolean,
    val hasVideoTrack: Boolean,
    val scannerIncluded: Boolean,
    val scannerExclusionReason: MediaCompatibilityReason?,
    val playbackSupport: PlaybackSupport,
    val playbackSupportReason: MediaCompatibilityReason,
    val tagWriteSupport: TagWriteSupport,
    val tagWriteReason: MediaCompatibilityReason,
    val lyricsWriteSupport: TagWriteSupport,
    val lyricsWriteReason: MediaCompatibilityReason,
    val artworkWriteSupport: Boolean,
    val artworkWriteReason: MediaCompatibilityReason,
    val metadataReadSupport: MetadataReadSupport,
    val fingerprintEligible: Boolean,
    val userSafeExplanation: String,
)

internal enum class MediaCompatibilityReason {
    Supported,
    UnsupportedExtension,
    MissingAudioTrack,
    VideoTrackPresent,
    UnsupportedCodec,
    DecoderUnavailable,
    UnsafeTagWriter,
    UnsafeArtworkWriter,
    UnknownContainer,
    PlatformDependentDecoder,
}

internal object MediaCompatibilityDiagnostics {
    fun report(
        fileName: String,
        uri: Uri? = null,
        discovered: Boolean = true,
        indexedByMediaStore: Boolean = true,
        foundThroughSaf: Boolean = false,
        includedInLibrary: Boolean = true,
        mediaStoreMimeType: String? = null,
        detected: DetectedAudioFormat? = null,
        scannerIncluded: Boolean = true,
        scannerExclusionReason: MediaCompatibilityReason? = null,
        softwareDecoderFallbackAvailable: Boolean? = null,
    ): MediaCapabilityReport {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val capability = AudioFormatPolicy.capabilityForExtension(extension)
        val container = detected?.container ?: capability?.format ?: AudioContainerFormat.Unknown
        val playbackSupport = detected?.let(AudioFormatPolicy::playbackSupport)
            ?: capability?.playbackSupport
            ?: PlaybackSupport.Unsupported
        val playbackReason = playbackReason(detected, capability, playbackSupport)
        val tagWriteSupport = AudioFormatPolicy.tagWriteSupport(detected, fileName)
        val lyricsWriteSupport = AudioFormatPolicy.embeddedLyricsWriteSupport(detected, fileName)
        val artworkWriteSupport = playbackSupport != PlaybackSupport.Unsupported &&
            AudioFormatPolicy.canEmbedArtwork(detected, fileName)
        return MediaCapabilityReport(
            fileName = fileName,
            uri = uri,
            discovered = discovered,
            indexedByMediaStore = indexedByMediaStore,
            foundThroughSaf = foundThroughSaf,
            includedInLibrary = includedInLibrary,
            extension = extension,
            mediaStoreMimeType = mediaStoreMimeType,
            detectedContainer = container,
            detectedCodecMimeType = detected?.codecMimeType,
            decoderAvailable = detected?.decoderAvailable,
            softwareDecoderFallbackAvailable = softwareDecoderFallbackAvailable,
            hasAudioTrack = detected?.hasAudioTrack ?: true,
            hasVideoTrack = detected?.hasVideoTrack ?: false,
            scannerIncluded = scannerIncluded,
            scannerExclusionReason = scannerExclusionReason,
            playbackSupport = playbackSupport,
            playbackSupportReason = playbackReason,
            tagWriteSupport = tagWriteSupport,
            tagWriteReason = if (tagWriteSupport == TagWriteSupport.Safe) {
                MediaCompatibilityReason.Supported
            } else {
                MediaCompatibilityReason.UnsafeTagWriter
            },
            lyricsWriteSupport = lyricsWriteSupport,
            lyricsWriteReason = if (lyricsWriteSupport == TagWriteSupport.Safe) {
                MediaCompatibilityReason.Supported
            } else {
                MediaCompatibilityReason.UnsafeTagWriter
            },
            artworkWriteSupport = artworkWriteSupport,
            artworkWriteReason = if (artworkWriteSupport) {
                MediaCompatibilityReason.Supported
            } else {
                MediaCompatibilityReason.UnsafeArtworkWriter
            },
            metadataReadSupport = capability?.metadataReadSupport ?: MetadataReadSupport.Unsupported,
            fingerprintEligible = AudioFormatPolicy.canFingerprint(fileName),
            userSafeExplanation = explanation(
                scannerIncluded = scannerIncluded,
                scannerExclusionReason = scannerExclusionReason,
                playbackReason = playbackReason,
                playbackSupport = playbackSupport,
                tagWriteSupport = tagWriteSupport,
                artworkWriteSupport = artworkWriteSupport,
            ),
        )
    }

    private fun playbackReason(
        detected: DetectedAudioFormat?,
        capability: AudioFormatCapability?,
        playbackSupport: PlaybackSupport,
    ): MediaCompatibilityReason {
        if (capability == null) return MediaCompatibilityReason.UnsupportedExtension
        if (detected == null) {
            return when (playbackSupport) {
                PlaybackSupport.Supported -> MediaCompatibilityReason.Supported
                PlaybackSupport.PlatformDependent -> MediaCompatibilityReason.PlatformDependentDecoder
                PlaybackSupport.Unsupported -> MediaCompatibilityReason.UnsupportedCodec
            }
        }
        return when {
            !detected.hasAudioTrack -> MediaCompatibilityReason.MissingAudioTrack
            detected.hasVideoTrack -> MediaCompatibilityReason.VideoTrackPresent
            detected.container == AudioContainerFormat.Unknown -> MediaCompatibilityReason.UnknownContainer
            detected.decoderAvailable == false -> MediaCompatibilityReason.DecoderUnavailable
            playbackSupport == PlaybackSupport.Unsupported -> MediaCompatibilityReason.UnsupportedCodec
            playbackSupport == PlaybackSupport.PlatformDependent -> MediaCompatibilityReason.PlatformDependentDecoder
            else -> MediaCompatibilityReason.Supported
        }
    }

    private fun explanation(
        scannerIncluded: Boolean,
        scannerExclusionReason: MediaCompatibilityReason?,
        playbackReason: MediaCompatibilityReason,
        playbackSupport: PlaybackSupport,
        tagWriteSupport: TagWriteSupport,
        artworkWriteSupport: Boolean,
    ): String {
        if (!scannerIncluded) {
            return when (scannerExclusionReason) {
                MediaCompatibilityReason.VideoTrackPresent -> "This file contains video and is not treated as music."
                MediaCompatibilityReason.MissingAudioTrack -> "No audio track was detected."
                MediaCompatibilityReason.UnsupportedExtension -> "This file type is not supported by the library scanner."
                MediaCompatibilityReason.DecoderUnavailable -> "No compatible audio decoder is available on this device."
                else -> "This file is not eligible for the music library."
            }
        }
        return when {
            playbackReason == MediaCompatibilityReason.VideoTrackPresent ->
                "This file contains video and cannot be played as a music track."
            playbackReason == MediaCompatibilityReason.DecoderUnavailable ->
                "This device does not provide a compatible decoder for this audio stream."
            playbackSupport == PlaybackSupport.PlatformDependent ->
                "Playback depends on decoder support from this Android device."
            tagWriteSupport != TagWriteSupport.Safe ->
                "Playback may work, but tag or lyrics writing is not considered safe for this file type."
            !artworkWriteSupport ->
                "Playback may work, but artwork writing is not considered safe for this file type."
            else -> "This file is supported for library, playback, metadata, lyrics, and artwork operations."
        }
    }
}
