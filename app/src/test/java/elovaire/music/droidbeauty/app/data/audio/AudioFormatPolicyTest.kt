package elovaire.music.droidbeauty.app.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFormatPolicyTest {
    @Test
    fun resolveContainer_distinguishesOggCodecsForOga() {
        assertEquals(
            AudioContainerFormat.OggOpus,
            AudioFormatPolicy.resolveContainer("oga", null, "audio/opus"),
        )
        assertEquals(
            AudioContainerFormat.OggFlac,
            AudioFormatPolicy.resolveContainer("oga", null, "audio/flac"),
        )
        assertEquals(
            AudioContainerFormat.OggVorbis,
            AudioFormatPolicy.resolveContainer("oga", null, "audio/vorbis"),
        )
    }

    @Test
    fun resolveContainer_treatsM4bAsMp4Audio() {
        assertEquals(
            AudioContainerFormat.Mp4Audio,
            AudioFormatPolicy.resolveContainer("m4b", "audio/mp4", null),
        )
    }

    @Test
    fun capabilityForExtension_acceptsCommonFlacMimeAlias() {
        val flac = AudioFormatPolicy.capabilityFor(AudioContainerFormat.Flac)

        requireNotNull(flac)
        assertTrue("audio/x-flac" in flac.mimeTypes)
    }

    @Test
    fun playbackMimeType_includesOgaAndM4b() {
        assertEquals("audio/ogg", AudioFormatPolicy.playbackMimeType("chapter.oga"))
        assertEquals("audio/mp4", AudioFormatPolicy.playbackMimeType("book.m4b"))
    }

    @Test
    fun scannerExtensions_allMapToCapabilitiesAndPlaybackMime() {
        AudioFormatPolicy.scannerExtensions.forEach { extension ->
            requireNotNull(AudioFormatPolicy.capabilityForExtension(extension))
            requireNotNull(AudioFormatPolicy.playbackMimeType("song.$extension"))
        }
    }

    @Test
    fun playbackSupport_rejectsVideoContainersEvenWithAudioTrack() {
        val detected = DetectedAudioFormat(
            container = AudioContainerFormat.Mp4Audio,
            displayName = "MP4 Audio",
            mimeType = "video/mp4",
            codecMimeType = "audio/mp4a-latm",
            detectionSucceeded = true,
            hasAudioTrack = true,
            hasVideoTrack = true,
            decoderAvailable = true,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
        )

        assertEquals(PlaybackSupport.Unsupported, AudioFormatPolicy.playbackSupport(detected))
    }

    @Test
    fun resolveContainer_rejectsUnknownOggCodec() {
        assertEquals(
            AudioContainerFormat.Unknown,
            AudioFormatPolicy.resolveContainer("ogg", null, "audio/speex"),
        )
    }

    @Test
    fun playbackSupport_allowsDeviceDecodedMp4AlacOnly() {
        val supported = DetectedAudioFormat(
            container = AudioContainerFormat.Mp4Audio,
            displayName = "M4A/ALAC",
            mimeType = "audio/mp4",
            codecMimeType = "audio/alac",
            detectionSucceeded = true,
            hasAudioTrack = true,
            hasVideoTrack = false,
            decoderAvailable = true,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
        )
        val unsupported = supported.copy(decoderAvailable = false)

        assertEquals(PlaybackSupport.Supported, AudioFormatPolicy.playbackSupport(supported))
        assertEquals(PlaybackSupport.Unsupported, AudioFormatPolicy.playbackSupport(unsupported))
    }

    @Test
    fun validationExtensions_includeRequestedAliasesOnly() {
        assertEquals(
            setOf("m4a", "m4b", "mp4", "ogg", "oga", "mka", "3gp", "amr"),
            AudioFormatPolicy.validationRequiredExtensions,
        )
        assertFalse("webm" in AudioFormatPolicy.validationRequiredExtensions)
    }

    @Test
    fun writeSupport_isCodecAwareForMp4Family() {
        val supported = detected(
            container = AudioContainerFormat.Mp4Audio,
            codecMimeType = "audio/alac",
        )
        val unsupported = supported.copy(codecMimeType = "audio/opus")

        assertEquals(TagWriteSupport.Safe, AudioFormatPolicy.tagWriteSupport(supported, "song.m4a"))
        assertEquals(TagWriteSupport.Partial, AudioFormatPolicy.tagWriteSupport(unsupported, "song.m4a"))
        assertTrue(AudioFormatPolicy.canEmbedArtwork(supported, "song.m4a"))
        assertFalse(AudioFormatPolicy.canEmbedArtwork(unsupported, "song.m4a"))
    }

    @Test
    fun writeSupport_rejectsUnsafeContainers() {
        assertEquals(
            TagWriteSupport.Unsupported,
            AudioFormatPolicy.embeddedLyricsWriteSupport(detected(AudioContainerFormat.OggOpus, "audio/opus"), "song.oga"),
        )
        assertEquals(
            TagWriteSupport.Unsupported,
            AudioFormatPolicy.embeddedLyricsWriteSupport(detected(AudioContainerFormat.Amr, "audio/amr"), "voice.amr"),
        )
        assertEquals(
            TagWriteSupport.Unsupported,
            AudioFormatPolicy.embeddedLyricsWriteSupport(detected(AudioContainerFormat.MatroskaAudio, "audio/flac"), "song.mka"),
        )
    }

    private fun detected(
        container: AudioContainerFormat,
        codecMimeType: String,
    ): DetectedAudioFormat {
        return DetectedAudioFormat(
            container = container,
            displayName = AudioFormatPolicy.displayName(container, "tmp", codecMimeType),
            mimeType = null,
            codecMimeType = codecMimeType,
            detectionSucceeded = true,
            hasAudioTrack = true,
            hasVideoTrack = false,
            decoderAvailable = true,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
        )
    }
}
