package elovaire.music.droidbeauty.app.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaCompatibilityDiagnosticsTest {
    @Test
    fun reportExplainsVideoTrackRejection() {
        val report = MediaCompatibilityDiagnostics.report(
            fileName = "clip.mp4",
            mediaStoreMimeType = "video/mp4",
            detected = detected(
                container = AudioContainerFormat.Mp4Audio,
                codec = "audio/mp4a-latm",
                hasVideoTrack = true,
            ),
        )

        assertEquals(PlaybackSupport.Unsupported, report.playbackSupport)
        assertEquals(MediaCompatibilityReason.VideoTrackPresent, report.playbackSupportReason)
        assertFalse(report.artworkWriteSupport)
    }

    @Test
    fun reportAllowsSafeMp3TagAndLyricsWrites() {
        val report = MediaCompatibilityDiagnostics.report(
            fileName = "song.mp3",
            detected = detected(AudioContainerFormat.Mp3, "audio/mpeg"),
        )

        assertEquals(PlaybackSupport.Supported, report.playbackSupport)
        assertEquals(TagWriteSupport.Safe, report.tagWriteSupport)
        assertEquals(TagWriteSupport.Safe, report.lyricsWriteSupport)
        assertTrue(report.artworkWriteSupport)
        assertEquals("This file is supported for library, playback, metadata, lyrics, and artwork operations.", report.userSafeExplanation)
    }

    @Test
    fun reportExplainsPlatformDependentPlayback() {
        val report = MediaCompatibilityDiagnostics.report(
            fileName = "song.mka",
            detected = detected(AudioContainerFormat.MatroskaAudio, "audio/opus"),
            softwareDecoderFallbackAvailable = false,
        )

        assertEquals(PlaybackSupport.PlatformDependent, report.playbackSupport)
        assertEquals(MediaCompatibilityReason.PlatformDependentDecoder, report.playbackSupportReason)
        assertEquals(false, report.softwareDecoderFallbackAvailable)
    }

    @Test
    fun reportPreservesDiscoverySourceFields() {
        val report = MediaCompatibilityDiagnostics.report(
            fileName = "song.flac",
            indexedByMediaStore = false,
            foundThroughSaf = true,
            detected = detected(AudioContainerFormat.Flac, "audio/flac"),
        )

        assertTrue(report.discovered)
        assertFalse(report.indexedByMediaStore)
        assertTrue(report.foundThroughSaf)
        assertTrue(report.includedInLibrary)
    }

    private fun detected(
        container: AudioContainerFormat,
        codec: String,
        hasVideoTrack: Boolean = false,
    ) = DetectedAudioFormat(
        container = container,
        displayName = AudioFormatPolicy.displayName(container, "tmp", codec),
        mimeType = null,
        codecMimeType = codec,
        detectionSucceeded = true,
        hasAudioTrack = true,
        hasVideoTrack = hasVideoTrack,
        decoderAvailable = true,
        sampleRate = null,
        channelCount = null,
        bitrate = null,
        bitDepth = null,
    )
}
