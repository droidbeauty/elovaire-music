package elovaire.music.droidbeauty.app.domain.model

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
    val metadataResolved: Boolean = false,
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

enum class AppLanguage(
    val englishName: String,
    val nativeName: String,
) {
    Albanian("Albanian", "Shqip"),
    English("English", "English"),
    ChineseSimplified("Simplified Chinese", "简体中文"),
    Croatian("Croatian", "Hrvatski"),
    Czech("Czech", "Čeština"),
    Danish("Danish", "Dansk"),
    Dutch("Dutch", "Nederlands"),
    Bengali("Bengali", "বাংলা"),
    Estonian("Estonian", "Eesti"),
    French("French", "Français"),
    German("German", "Deutsch"),
    Greek("Greek", "Ελληνικά"),
    Hindi("Hindi", "हिन्दी"),
    Hungarian("Hungarian", "Magyar"),
    Italian("Italian", "Italiano"),
    Japanese("Japanese", "日本語"),
    Korean("Korean", "한국어"),
    Latin("Latin", "Latina"),
    Latvian("Latvian", "Latviešu"),
    Lithuanian("Lithuanian", "Lietuvių"),
    Malay("Malay", "Bahasa Melayu"),
    Macedonian("Macedonian", "Македонски"),
    Norwegian("Norwegian", "Norsk"),
    Polish("Polish", "Polski"),
    Portuguese("Portuguese", "Português"),
    Russian("Russian", "Русский"),
    Slovak("Slovak", "Slovenčina"),
    Serbian("Serbian", "Српски"),
    Spanish("Spanish", "Español"),
    Swedish("Swedish", "Svenska"),
    Thai("Thai", "ไทย"),
    Ukrainian("Ukrainian", "Українська"),
    Urdu("Urdu", "اردو"),
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
    val bands: List<Float> = List(18) { 0f },
    val bass: Float = 0f,
    val midrange: Float = 0f,
    val treble: Float = 0f,
    val spaciousness: Float = 0f,
    val spaciousnessMode: SpaciousnessMode = SpaciousnessMode.StereoWidth,
    val monoEnabled: Boolean = false,
    val reverbDurationMs: Int = 0,
    val reverbProfile: ReverbProfile = ReverbProfile.Dry,
)

enum class SpaciousnessMode {
    Off,
    StereoWidth,
    CrossfeedDepth,
    EarlyReflectionRoom,
    Philharmony,
    HaasSpace,
    HarmonicAir,
}

enum class ReverbProfile {
    Dry,
    Wet,
}
