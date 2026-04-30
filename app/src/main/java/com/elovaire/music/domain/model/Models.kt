package elovaire.music.app.domain.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val isExplicit: Boolean,
    val artist: String,
    val album: String,
    val releaseYear: Int?,
    val genre: String,
    val audioFormat: String,
    val audioQuality: String?,
    val fileName: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val dateAddedSeconds: Long,
    val uri: Uri,
    val artUri: Uri?,
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artUri: Uri?,
    val songCount: Int,
    val durationMs: Long,
    val songs: List<Song>,
)

data class LibrarySnapshot(
    val songs: List<Song>,
    val albums: List<Album>,
)

data class Playlist(
    val id: Long,
    val name: String,
    val songIds: List<Long> = emptyList(),
    val isSystem: Boolean = false,
)

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class TextSizePreset(
    val scaleFactor: Float,
) {
    Small(0.88f),
    Compact(0.94f),
    Default(1f),
    Large(1.08f),
    ExtraLarge(1.16f),
}

enum class SearchHistoryKind {
    Album,
    Artist,
}

data class SearchHistoryEntry(
    val key: String,
    val kind: SearchHistoryKind,
    val title: String,
    val subtitle: String,
    val artUri: Uri?,
    val albumId: Long? = null,
    val query: String? = null,
)

data class EqSettings(
    val bands: List<Float> = List(24) { 0f },
    val bass: Float = 0f,
    val treble: Float = 0f,
    val spaciousness: Float = 0f,
    val spaciousnessMode: SpaciousnessMode = SpaciousnessMode.StereoWidth,
)

enum class SpaciousnessMode {
    Off,
    StereoWidth,
    CrossfeedDepth,
    EarlyReflectionRoom,
    HaasSpace,
    HarmonicAir,
}
