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
    val playbackMimeType: String?,
    val allowedCodecMimeTypes: Set<String>,
    val requiresContainerValidation: Boolean,
    val playbackSupport: PlaybackSupport,
    val metadataReadSupport: MetadataReadSupport,
    val tagWriteSupport: TagWriteSupport,
    val canEmbedArtwork: Boolean,
    val notes: String,
)

internal object AudioFormatPolicy {
    val capabilities = listOf(
        AudioFormatCapability(
            format = AudioContainerFormat.Mp3,
            displayName = "MP3",
            extensions = setOf("mp3"),
            mimeTypes = setOf("audio/mpeg"),
            playbackMimeType = "audio/mpeg",
            allowedCodecMimeTypes = setOf("audio/mpeg"),
            requiresContainerValidation = false,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Strong,
            tagWriteSupport = TagWriteSupport.Safe,
            canEmbedArtwork = true,
            notes = "ID3 metadata and artwork are supported.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.Mp4Audio,
            displayName = "M4A/MP4 Audio",
            extensions = setOf("m4a", "m4b", "mp4"),
            mimeTypes = setOf("audio/mp4", "audio/mp4a-latm", "video/mp4"),
            playbackMimeType = "audio/mp4",
            allowedCodecMimeTypes = setOf("audio/mp4a-latm", "audio/aac", "audio/mpeg", "audio/opus", "audio/alac"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Strong,
            tagWriteSupport = TagWriteSupport.Safe,
            canEmbedArtwork = true,
            notes = "MP4 files must contain audio and no video track.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.AacAdts,
            displayName = "AAC",
            extensions = setOf("aac"),
            mimeTypes = setOf("audio/aac", "audio/aac-adts"),
            playbackMimeType = "audio/aac",
            allowedCodecMimeTypes = setOf("audio/aac", "audio/mp4a-latm"),
            requiresContainerValidation = false,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Weak,
            tagWriteSupport = TagWriteSupport.Unsupported,
            canEmbedArtwork = false,
            notes = "ADTS metadata writes are unsafe.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.Flac,
            displayName = "FLAC",
            extensions = setOf("flac"),
            mimeTypes = setOf("audio/flac", "audio/x-flac"),
            playbackMimeType = "audio/flac",
            allowedCodecMimeTypes = setOf("audio/flac"),
            requiresContainerValidation = false,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Strong,
            tagWriteSupport = TagWriteSupport.Safe,
            canEmbedArtwork = true,
            notes = "Vorbis comments and artwork are supported.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.Wav,
            displayName = "WAV",
            extensions = setOf("wav"),
            mimeTypes = setOf("audio/wav", "audio/x-wav"),
            playbackMimeType = "audio/wav",
            allowedCodecMimeTypes = setOf("audio/g711-alaw", "audio/g711-mlaw"),
            requiresContainerValidation = false,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Partial,
            tagWriteSupport = TagWriteSupport.Partial,
            canEmbedArtwork = false,
            notes = "WAV metadata layouts are inconsistent.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.OggVorbis,
            displayName = "OGG/VORBIS",
            extensions = setOf("ogg", "oga"),
            mimeTypes = setOf("audio/ogg", "application/ogg", "audio/vorbis"),
            playbackMimeType = "audio/ogg",
            allowedCodecMimeTypes = setOf("audio/vorbis", "audio/ogg"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Partial,
            tagWriteSupport = TagWriteSupport.Partial,
            canEmbedArtwork = false,
            notes = "Codec is detected before playback; writes remain disabled.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.OggOpus,
            displayName = "OGG/OPUS",
            extensions = setOf("ogg", "oga"),
            mimeTypes = setOf("audio/ogg", "application/ogg", "audio/opus"),
            playbackMimeType = "audio/ogg",
            allowedCodecMimeTypes = setOf("audio/opus"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Partial,
            tagWriteSupport = TagWriteSupport.Partial,
            canEmbedArtwork = false,
            notes = "Codec is detected before playback; writes remain disabled.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.OggFlac,
            displayName = "OGG/FLAC",
            extensions = setOf("ogg", "oga"),
            mimeTypes = setOf("audio/ogg", "application/ogg", "audio/flac"),
            playbackMimeType = "audio/ogg",
            allowedCodecMimeTypes = setOf("audio/flac"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Partial,
            tagWriteSupport = TagWriteSupport.Partial,
            canEmbedArtwork = false,
            notes = "Codec is detected before playback; writes remain disabled.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.Opus,
            displayName = "OPUS",
            extensions = setOf("opus"),
            mimeTypes = setOf("audio/opus"),
            playbackMimeType = "audio/opus",
            allowedCodecMimeTypes = setOf("audio/opus"),
            requiresContainerValidation = false,
            playbackSupport = PlaybackSupport.Supported,
            metadataReadSupport = MetadataReadSupport.Partial,
            tagWriteSupport = TagWriteSupport.Partial,
            canEmbedArtwork = false,
            notes = "Text and artwork writes are not guaranteed.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.Amr,
            displayName = "AMR",
            extensions = setOf("amr"),
            mimeTypes = setOf("audio/amr", "audio/amr-wb"),
            playbackMimeType = "audio/amr",
            allowedCodecMimeTypes = setOf("audio/3gpp", "audio/amr-wb", "audio/amr"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.PlatformDependent,
            metadataReadSupport = MetadataReadSupport.Weak,
            tagWriteSupport = TagWriteSupport.Unsupported,
            canEmbedArtwork = false,
            notes = "Usually voice content and device-decoder dependent.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.ThreeGpAudio,
            displayName = "3GP Audio",
            extensions = setOf("3gp"),
            mimeTypes = setOf("audio/3gpp", "video/3gpp"),
            playbackMimeType = "audio/3gpp",
            allowedCodecMimeTypes = setOf("audio/3gpp", "audio/amr-wb", "audio/mp4a-latm", "audio/aac"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.PlatformDependent,
            metadataReadSupport = MetadataReadSupport.Weak,
            tagWriteSupport = TagWriteSupport.Unsupported,
            canEmbedArtwork = false,
            notes = "Only audio-only files with a device decoder are eligible.",
        ),
        AudioFormatCapability(
            format = AudioContainerFormat.MatroskaAudio,
            displayName = "MKA",
            extensions = setOf("mka"),
            mimeTypes = setOf("audio/x-matroska", "video/x-matroska"),
            playbackMimeType = "audio/x-matroska",
            allowedCodecMimeTypes = setOf("audio/opus", "audio/vorbis", "audio/flac", "audio/mpeg", "audio/mp4a-latm"),
            requiresContainerValidation = true,
            playbackSupport = PlaybackSupport.PlatformDependent,
            metadataReadSupport = MetadataReadSupport.Weak,
            tagWriteSupport = TagWriteSupport.Unsupported,
            canEmbedArtwork = false,
            notes = "Codec playback depends on the device decoder.",
        ),
    )

    val scannerExtensions: Set<String> = capabilities
        .filter { it.playbackSupport != PlaybackSupport.Unsupported }
        .flatMapTo(linkedSetOf()) { it.extensions }

    val safelyTaggableExtensions: Set<String> = capabilities
        .filter { it.tagWriteSupport == TagWriteSupport.Safe }
        .flatMapTo(linkedSetOf()) { it.extensions }

    val validationRequiredExtensions: Set<String> = capabilities
        .filter { it.requiresContainerValidation }
        .flatMapTo(linkedSetOf()) { it.extensions }

    fun requiresContainerValidation(extension: String?): Boolean {
        return extension.orEmpty().trim().lowercase(Locale.ROOT) in validationRequiredExtensions
    }

    fun shouldDetectContainer(
        extension: String?,
        enrichMetadata: Boolean,
    ): Boolean {
        return enrichMetadata || requiresContainerValidation(extension)
    }

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
                "" -> AudioContainerFormat.OggVorbis
                else -> AudioContainerFormat.Unknown
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
            else -> capabilityForMimeType(mediaStoreMimeType)?.format ?: AudioContainerFormat.Unknown
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

    fun tagWriteSupport(
        detected: DetectedAudioFormat?,
        fileName: String,
    ): TagWriteSupport {
        val base = capabilityForFileName(fileName) ?: return TagWriteSupport.Unsupported
        if (base.tagWriteSupport != TagWriteSupport.Safe) return base.tagWriteSupport
        if (detected == null || !detected.detectionSucceeded) return base.tagWriteSupport
        return when (detected.container) {
            AudioContainerFormat.Mp3,
            AudioContainerFormat.Flac,
            -> TagWriteSupport.Safe

            AudioContainerFormat.Mp4Audio -> when (detected.codecMimeType.orEmpty().lowercase(Locale.ROOT)) {
                "audio/mp4a-latm",
                "audio/aac",
                "audio/alac",
                "audio/mpeg",
                -> TagWriteSupport.Safe

                else -> TagWriteSupport.Partial
            }

            else -> TagWriteSupport.Unsupported
        }
    }

    fun embeddedLyricsWriteSupport(
        detected: DetectedAudioFormat?,
        fileName: String,
    ): TagWriteSupport {
        return when (detected?.container ?: resolveContainer(
            extension = fileName.substringAfterLast('.', ""),
            mediaStoreMimeType = null,
            codecMimeType = null,
        )) {
            AudioContainerFormat.Mp3,
            AudioContainerFormat.Flac,
            -> TagWriteSupport.Safe

            else -> TagWriteSupport.Unsupported
        }
    }

    fun canEmbedArtwork(
        detected: DetectedAudioFormat?,
        fileName: String,
    ): Boolean {
        return capabilityForFileName(fileName)?.canEmbedArtwork == true &&
            tagWriteSupport(detected, fileName) == TagWriteSupport.Safe
    }

    private fun isCodecAllowed(container: AudioContainerFormat, codecMimeType: String?): Boolean {
        if (container == AudioContainerFormat.Wav && codecMimeType.orEmpty().lowercase(Locale.ROOT).startsWith("audio/raw")) {
            return true
        }
        val codec = codecMimeType.orEmpty().lowercase(Locale.ROOT)
        if (codec.isBlank()) return false
        return codec in capabilityFor(container).orEmptyAllowedCodecs()
    }

    fun canFingerprint(fileName: String): Boolean {
        return capabilityForFileName(fileName)?.playbackSupport != PlaybackSupport.Unsupported
    }

    fun playbackMimeType(fileName: String): String? {
        return capabilityForFileName(fileName)?.playbackMimeType
    }

    private fun isOggExtension(extension: String): Boolean {
        return extension == "ogg" || extension == "oga"
    }

    private fun capabilityForMimeType(mimeType: String?): AudioFormatCapability? {
        val normalized = mimeType.orEmpty().lowercase(Locale.ROOT)
        return capabilities.firstOrNull { normalized in it.mimeTypes }
    }

    private fun AudioFormatCapability?.orEmptyAllowedCodecs(): Set<String> {
        return this?.allowedCodecMimeTypes.orEmpty()
    }
}
