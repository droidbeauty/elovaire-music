package elovaire.music.droidbeauty.app.data.audio

import org.junit.Assert.assertEquals
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
    fun playbackMimeType_includesOgaAndM4b() {
        assertEquals("audio/ogg", AudioFormatPolicy.playbackMimeType("chapter.oga"))
        assertEquals("audio/mp4", AudioFormatPolicy.playbackMimeType("book.m4b"))
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
        assertTrue("oga" in AudioFormatPolicy.validationRequiredExtensions)
        assertTrue("m4b" in AudioFormatPolicy.validationRequiredExtensions)
        assertTrue("m4a" in AudioFormatPolicy.validationRequiredExtensions)
    }
}
