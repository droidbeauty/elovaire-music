package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.audio.AudioContainerFormat
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryAudioFileFilterTest {
    @Test
    fun evaluate_excludesPodcastUnderDefaultRoot() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = setOf("/storage/emulated/0/music"),
        )

        assertTrue(
            filter.evaluate(candidate("/storage/emulated/0/Music/Podcasts/song.mp3")) is AudioFileFilterDecision.Exclude,
        )
    }

    @Test
    fun evaluate_allowsExplicitCustomFolderWithExcludedFragment() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = setOf("/storage/emulated/0/music/podcasts"),
            explicitCustomRootPaths = setOf("/storage/emulated/0/music/podcasts"),
        )

        assertTrue(
            filter.evaluate(candidate("/storage/emulated/0/Music/Podcasts/song.mp3")) is AudioFileFilterDecision.Include,
        )
    }

    @Test
    fun evaluate_matchesRelativePathWhenAbsolutePathMissing() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = setOf("music/custom"),
            libraryRootPaths = emptySet(),
        )

        assertTrue(
            filter.evaluate(candidate(absolutePath = null, relativePath = "Music/Custom/")) is AudioFileFilterDecision.Include,
        )
    }

    private fun candidate(
        absolutePath: String?,
        relativePath: String? = "Music/",
    ): AudioScanCandidate {
        return AudioScanCandidate(
            id = 1L,
            uri = TestUri("content://media/song"),
            displayName = "song.mp3",
            title = "Song",
            artist = "Artist",
            album = "Album",
            durationMs = 180_000L,
            mimeType = "audio/mpeg",
            relativePath = relativePath,
            absolutePath = absolutePath,
            extension = "mp3",
            isMusic = true,
            detectedFormat = DetectedAudioFormat(
                container = AudioContainerFormat.Mp3,
                displayName = AudioFormatPolicy.displayName(AudioContainerFormat.Mp3, "mp3"),
                mimeType = "audio/mpeg",
                codecMimeType = "audio/mpeg",
                detectionSucceeded = true,
                hasAudioTrack = true,
                hasVideoTrack = false,
                decoderAvailable = true,
                sampleRate = null,
                channelCount = null,
                bitrate = null,
                bitDepth = null,
            ),
        )
    }
}
