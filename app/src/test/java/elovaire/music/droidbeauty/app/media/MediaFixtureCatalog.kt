package elovaire.music.droidbeauty.app.media

internal data class MediaFixtureDescriptor(
    val name: String,
    val fileName: String,
    val expectedScannerEligible: Boolean,
    val expectedTagWritable: Boolean,
)

internal object MediaFixtureCatalog {
    val descriptors = listOf(
        MediaFixtureDescriptor("MP3 ID3v2.3", "fixture-id3v23.mp3", true, true),
        MediaFixtureDescriptor("MP3 ID3v2.4", "fixture-id3v24.mp3", true, true),
        MediaFixtureDescriptor("FLAC Vorbis comments", "fixture.flac", true, true),
        MediaFixtureDescriptor("M4A AAC", "fixture-aac.m4a", true, true),
        MediaFixtureDescriptor("M4A ALAC", "fixture-alac.m4a", true, true),
        MediaFixtureDescriptor("Ogg Vorbis", "fixture-vorbis.ogg", true, false),
        MediaFixtureDescriptor("Ogg Opus", "fixture-opus.oga", true, false),
        MediaFixtureDescriptor("Standalone Opus", "fixture.opus", true, false),
        MediaFixtureDescriptor("WAV PCM", "fixture.wav", true, false),
        MediaFixtureDescriptor("MP4 with video", "fixture-video.mp4", true, false),
        MediaFixtureDescriptor("Corrupt file", "fixture-corrupt.mp3", true, false),
    )
}
