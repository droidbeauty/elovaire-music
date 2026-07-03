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
            explicitCustomRelativeRoots = setOf("music/custom"),
        )

        assertTrue(
            filter.evaluate(candidate(absolutePath = null, relativePath = "Music/Custom/")) is AudioFileFilterDecision.Include,
        )
    }

    @Test
    fun evaluate_allowsNonMusicMediaStoreRowsInsideExplicitRelativeRoot() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = setOf("music/flac archive"),
            libraryRootPaths = emptySet(),
            explicitCustomRelativeRoots = setOf("music/flac archive"),
        )

        assertTrue(
            filter.evaluate(
                candidate(
                    absolutePath = null,
                    relativePath = "Music/FLAC Archive/",
                    extension = "flac",
                    mimeType = "audio/x-flac",
                    isMusic = false,
                    detectedFormat = detected(AudioContainerFormat.Flac, "flac", "audio/flac", "audio/x-flac"),
                ),
            ) is AudioFileFilterDecision.Include,
        )
    }

    @Test
    fun evaluate_rejectsValidationRequiredExtensionWhenDetectionFailed() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = setOf("/storage/emulated/0/music/custom"),
            explicitCustomRootPaths = setOf("/storage/emulated/0/music/custom"),
        )

        assertTrue(
            filter.evaluate(
                candidate(
                    absolutePath = "/storage/emulated/0/Music/Custom/song.m4a",
                    extension = "m4a",
                    mimeType = "audio/mp4",
                    detectedFormat = detected(
                        container = AudioContainerFormat.Mp4Audio,
                        extension = "m4a",
                        codecMimeType = null,
                        mimeType = "audio/mp4",
                        detectionSucceeded = false,
                        hasAudioTrack = false,
                    ),
                ),
            ) is AudioFileFilterDecision.Exclude,
        )
    }

    @Test
    fun evaluate_allowsValidatedM4aInsideExplicitCustomRoot() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = setOf("/storage/emulated/0/music/custom"),
            explicitCustomRootPaths = setOf("/storage/emulated/0/music/custom"),
        )

        assertTrue(
            filter.evaluate(
                candidate(
                    absolutePath = "/storage/emulated/0/Music/Custom/song.m4a",
                    extension = "m4a",
                    mimeType = "audio/mp4",
                    isMusic = false,
                    detectedFormat = detected(AudioContainerFormat.Mp4Audio, "m4a", "audio/mp4a-latm", "audio/mp4"),
                ),
            ) is AudioFileFilterDecision.Include,
        )
    }

    @Test
    fun evaluate_rejectsValidatedMp4Video() {
        val filter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = setOf("/storage/emulated/0/music/custom"),
            explicitCustomRootPaths = setOf("/storage/emulated/0/music/custom"),
        )

        assertTrue(
            filter.evaluate(
                candidate(
                    absolutePath = "/storage/emulated/0/Music/Custom/video.mp4",
                    extension = "mp4",
                    mimeType = "video/mp4",
                    detectedFormat = detected(
                        container = AudioContainerFormat.Mp4Audio,
                        extension = "mp4",
                        codecMimeType = "audio/mp4a-latm",
                        mimeType = "video/mp4",
                        hasVideoTrack = true,
                    ),
                ),
            ) is AudioFileFilterDecision.Exclude,
        )
    }

    private fun candidate(
        absolutePath: String?,
        relativePath: String? = "Music/",
        extension: String = "mp3",
        mimeType: String = "audio/mpeg",
        isMusic: Boolean? = true,
        detectedFormat: DetectedAudioFormat = detected(AudioContainerFormat.Mp3, extension, mimeType, mimeType),
    ): AudioScanCandidate {
        return AudioScanCandidate(
            id = 1L,
            uri = TestUri("content://media/song"),
            displayName = "song.$extension",
            title = "Song",
            artist = "Artist",
            album = "Album",
            durationMs = 180_000L,
            mimeType = mimeType,
            relativePath = relativePath,
            absolutePath = absolutePath,
            extension = extension,
            isMusic = isMusic,
            detectedFormat = detectedFormat,
        )
    }

    private fun detected(
        container: AudioContainerFormat,
        extension: String,
        codecMimeType: String?,
        mimeType: String?,
        detectionSucceeded: Boolean = true,
        hasAudioTrack: Boolean = true,
        hasVideoTrack: Boolean = false,
    ): DetectedAudioFormat {
        return DetectedAudioFormat(
            container = container,
            displayName = AudioFormatPolicy.displayName(container, extension, codecMimeType),
            mimeType = mimeType,
            codecMimeType = codecMimeType,
            detectionSucceeded = detectionSucceeded,
            hasAudioTrack = hasAudioTrack,
            hasVideoTrack = hasVideoTrack,
            decoderAvailable = true,
            sampleRate = null,
            channelCount = null,
            bitrate = null,
            bitDepth = null,
        )
    }
}
