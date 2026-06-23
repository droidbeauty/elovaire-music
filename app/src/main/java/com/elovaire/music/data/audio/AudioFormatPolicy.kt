package elovaire.music.droidbeauty.app.data.audio

import java.util.Locale

internal enum class AudioContainerFormat {
    Mp3,
    Mp4Audio,
    AacAdts,
    Flac,
    Wav,
    OggVorbis,
    OggOpus,
    OggFlac,
    Opus,
    Amr,
    ThreeGpAudio,
    MatroskaAudio,
    Unknown,
}

internal enum class PlaybackSupport {
    Supported,
    PlatformDependent,
    Unsupported,
}

internal enum class MetadataReadSupport {
    Strong,
    Partial,
    Weak,
    Unsupported,
}

internal enum class TagWriteSupport {
    Safe,
    Partial,
    Unsupported,
}

internal data class AudioFormatCapability(
    val format: AudioContainerFormat,
    val displayName: String,
    val extensions: Set<String>,
    val mimeTypes: Set<String>,
    val playbackSupport: PlaybackSupport,
    val metadataReadSupport: MetadataReadSupport,
    val tagWriteSupport: TagWriteSupport,
    val canEmbedArtwork: Boolean,
    val notes: String,
)

internal object AudioFormatPolicy {
    val capabilities = listOf(
        AudioFormatCapability(AudioContainerFormat.Mp3, "MP3", setOf("mp3"), setOf("audio/mpeg"), PlaybackSupport.Supported, MetadataReadSupport.Strong, TagWriteSupport.Safe, true, "ID3 metadata and artwork are supported."),
        AudioFormatCapability(AudioContainerFormat.Mp4Audio, "M4A/MP4 Audio", setOf("m4a", "m4b", "mp4"), setOf("audio/mp4", "audio/mp4a-latm", "video/mp4"), PlaybackSupport.Supported, MetadataReadSupport.Strong, TagWriteSupport.Safe, true, "MP4 files must contain audio and no video track."),
        AudioFormatCapability(AudioContainerFormat.AacAdts, "AAC", setOf("aac"), setOf("audio/aac", "audio/aac-adts"), PlaybackSupport.Supported, MetadataReadSupport.Weak, TagWriteSupport.Unsupported, false, "ADTS metadata writes are unsafe."),
        AudioFormatCapability(AudioContainerFormat.Flac, "FLAC", setOf("flac"), setOf("audio/flac"), PlaybackSupport.Supported, MetadataReadSupport.Strong, TagWriteSupport.Safe, true, "Vorbis comments and artwork are supported."),
        AudioFormatCapability(AudioContainerFormat.Wav, "WAV", setOf("wav"), setOf("audio/wav", "audio/x-wav"), PlaybackSupport.Supported, MetadataReadSupport.Partial, TagWriteSupport.Partial, false, "WAV metadata layouts are inconsistent."),
        AudioFormatCapability(AudioContainerFormat.OggVorbis, "OGG/VORBIS", setOf("ogg", "oga"), setOf("audio/ogg", "application/ogg", "audio/vorbis"), PlaybackSupport.Supported, MetadataReadSupport.Partial, TagWriteSupport.Partial, false, "Codec is detected before playback; writes remain disabled."),
        AudioFormatCapability(AudioContainerFormat.OggOpus, "OGG/OPUS", setOf("ogg", "oga"), setOf("audio/ogg", "application/ogg", "audio/opus"), PlaybackSupport.Supported, MetadataReadSupport.Partial, TagWriteSupport.Partial, false, "Codec is detected before playback; writes remain disabled."),
        AudioFormatCapability(AudioContainerFormat.OggFlac, "OGG/FLAC", setOf("ogg", "oga"), setOf("audio/ogg", "application/ogg", "audio/flac"), PlaybackSupport.Supported, MetadataReadSupport.Partial, TagWriteSupport.Partial, false, "Codec is detected before playback; writes remain disabled."),
        AudioFormatCapability(AudioContainerFormat.Opus, "OPUS", setOf("opus"), setOf("audio/opus"), PlaybackSupport.Supported, MetadataReadSupport.Partial, TagWriteSupport.Partial, false, "Text and artwork writes are not guaranteed."),
        AudioFormatCapability(AudioContainerFormat.Amr, "AMR", setOf("amr"), setOf("audio/amr", "audio/amr-wb"), PlaybackSupport.PlatformDependent, MetadataReadSupport.Weak, TagWriteSupport.Unsupported, false, "Usually voice content and device-decoder dependent."),
        AudioFormatCapability(AudioContainerFormat.ThreeGpAudio, "3GP Audio", setOf("3gp"), setOf("audio/3gpp", "video/3gpp"), PlaybackSupport.PlatformDependent, MetadataReadSupport.Weak, TagWriteSupport.Unsupported, false, "Only audio-only files with a device decoder are eligible."),
        AudioFormatCapability(AudioContainerFormat.MatroskaAudio, "MKA", setOf("mka"), setOf("audio/x-matroska", "video/x-matroska"), PlaybackSupport.PlatformDependent, MetadataReadSupport.Weak, TagWriteSupport.Unsupported, false, "Codec playback depends on the device decoder."),
    )

    val scannerExtensions: Set<String> = capabilities
        .filter { it.playbackSupport != PlaybackSupport.Unsupported }
        .flatMapTo(linkedSetOf()) { it.extensions }

    val safelyTaggableExtensions: Set<String> = capabilities
        .filter { it.tagWriteSupport == TagWriteSupport.Safe }
        .flatMapTo(linkedSetOf()) { it.extensions }

    val validationRequiredExtensions: Set<String> = setOf(
        "m4a",
        "m4b",
        "mp4",
        "ogg",
        "oga",
        "mka",
        "3gp",
        "amr",
    )

    fun capabilityForExtension(extension: String?): AudioFormatCapability? {
        val normalized = extension.orEmpty().trim().lowercase(Locale.ROOT)
        return capabilities.firstOrNull { normalized in it.extensions }
    }

    fun capabilityForFileName(fileName: String): AudioFormatCapability? {
        return capabilityForExtension(fileName.substringAfterLast('.', ""))
    }

    fun capabilityFor(format: AudioContainerFormat): AudioFormatCapability? {
        return capabilities.firstOrNull { it.format == format }
    }

    fun resolveContainer(
        extension: String?,
        mediaStoreMimeType: String?,
        codecMimeType: String?,
    ): AudioContainerFormat {
        val ext = extension.orEmpty().lowercase(Locale.ROOT)
        val codec = codecMimeType.orEmpty().lowercase(Locale.ROOT)
        if (isOggExtension(ext)) {
            return when (codec) {
                "audio/opus" -> AudioContainerFormat.OggOpus
                "audio/flac" -> AudioContainerFormat.OggFlac
                "audio/vorbis", "audio/ogg" -> AudioContainerFormat.OggVorbis
                else -> AudioContainerFormat.OggVorbis
            }
        }
        return when (ext) {
            "mp3" -> AudioContainerFormat.Mp3
            "m4a", "m4b", "mp4" -> AudioContainerFormat.Mp4Audio
            "aac" -> AudioContainerFormat.AacAdts
            "flac" -> AudioContainerFormat.Flac
            "wav" -> AudioContainerFormat.Wav
            "opus" -> AudioContainerFormat.Opus
            "amr" -> AudioContainerFormat.Amr
            "3gp" -> AudioContainerFormat.ThreeGpAudio
            "mka" -> AudioContainerFormat.MatroskaAudio
            else -> capabilities.firstOrNull { capability ->
                mediaStoreMimeType.orEmpty().lowercase(Locale.ROOT) in capability.mimeTypes
            }?.format ?: AudioContainerFormat.Unknown
        }
    }

    fun displayName(
        container: AudioContainerFormat,
        extension: String,
        codecMimeType: String? = null,
    ): String {
        return when (container) {
            AudioContainerFormat.Mp3 -> "MP3"
            AudioContainerFormat.Mp4Audio -> when {
                codecMimeType.equals("audio/alac", true) && extension.equals("m4a", true) -> "M4A/ALAC"
                codecMimeType.equals("audio/alac", true) && extension.equals("m4b", true) -> "M4B/ALAC"
                codecMimeType.equals("audio/alac", true) -> "ALAC"
                extension.equals("m4a", true) -> "M4A"
                extension.equals("m4b", true) -> "M4B"
                else -> "MP4 Audio"
            }
            AudioContainerFormat.AacAdts -> "AAC"
            AudioContainerFormat.Flac -> "FLAC"
            AudioContainerFormat.Wav -> "WAV"
            AudioContainerFormat.OggVorbis -> if (codecMimeType.isNullOrBlank()) "OGG" else "OGG/VORBIS"
            AudioContainerFormat.OggOpus -> "OGG/OPUS"
            AudioContainerFormat.OggFlac -> "OGG/FLAC"
            AudioContainerFormat.Opus -> "OPUS"
            AudioContainerFormat.Amr -> "AMR"
            AudioContainerFormat.ThreeGpAudio -> "3GP Audio"
            AudioContainerFormat.MatroskaAudio -> "MKA"
            AudioContainerFormat.Unknown -> extension.uppercase(Locale.ROOT).ifBlank {
                codecMimeType?.substringAfter('/')?.uppercase(Locale.ROOT).orEmpty().ifBlank { "AUDIO" }
            }
        }
    }

    fun playbackSupport(detected: DetectedAudioFormat): PlaybackSupport {
        if (!detected.hasAudioTrack || detected.hasVideoTrack) return PlaybackSupport.Unsupported
        if (!isCodecAllowed(detected.container, detected.codecMimeType)) return PlaybackSupport.Unsupported
        if (detected.decoderAvailable == false) return PlaybackSupport.Unsupported
        return capabilityFor(detected.container)?.playbackSupport ?: PlaybackSupport.Unsupported
    }

    private fun isCodecAllowed(container: AudioContainerFormat, codecMimeType: String?): Boolean {
        val codec = codecMimeType.orEmpty().lowercase(Locale.ROOT)
        if (codec.isBlank()) return false
        return when (container) {
            AudioContainerFormat.Mp3 -> codec == "audio/mpeg"
            AudioContainerFormat.Mp4Audio -> codec in setOf("audio/mp4a-latm", "audio/aac", "audio/mpeg", "audio/opus", "audio/alac")
            AudioContainerFormat.AacAdts -> codec in setOf("audio/aac", "audio/mp4a-latm")
            AudioContainerFormat.Flac -> codec == "audio/flac"
            AudioContainerFormat.Wav -> codec.startsWith("audio/raw") || codec in setOf("audio/g711-alaw", "audio/g711-mlaw")
            AudioContainerFormat.OggVorbis -> codec in setOf("audio/vorbis", "audio/ogg")
            AudioContainerFormat.OggOpus,
            AudioContainerFormat.Opus,
            -> codec == "audio/opus"
            AudioContainerFormat.OggFlac -> codec == "audio/flac"
            AudioContainerFormat.Amr -> codec in setOf("audio/3gpp", "audio/amr-wb", "audio/amr")
            AudioContainerFormat.ThreeGpAudio -> codec in setOf("audio/3gpp", "audio/amr-wb", "audio/mp4a-latm", "audio/aac")
            AudioContainerFormat.MatroskaAudio -> codec in setOf("audio/opus", "audio/vorbis", "audio/flac", "audio/mpeg", "audio/mp4a-latm")
            AudioContainerFormat.Unknown -> false
        }
    }

    fun canFingerprint(fileName: String): Boolean {
        return capabilityForFileName(fileName)?.playbackSupport != PlaybackSupport.Unsupported
    }

    fun playbackMimeType(fileName: String): String? {
        return when (fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
            "mp3" -> "audio/mpeg"
            "m4a", "m4b", "mp4" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "ogg", "oga" -> "audio/ogg"
            "opus" -> "audio/opus"
            "amr" -> "audio/amr"
            "3gp" -> "audio/3gpp"
            "mka" -> "audio/x-matroska"
            else -> null
        }
    }

    private fun isOggExtension(extension: String): Boolean {
        return extension == "ogg" || extension == "oga"
    }
}
