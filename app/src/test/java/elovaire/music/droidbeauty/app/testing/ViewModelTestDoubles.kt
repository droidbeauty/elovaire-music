package elovaire.music.droidbeauty.app.testing

import android.net.TestUri
import elovaire.music.droidbeauty.app.data.library.LibraryContentState
import elovaire.music.droidbeauty.app.data.library.LibraryReader
import elovaire.music.droidbeauty.app.data.library.LibraryScanState
import elovaire.music.droidbeauty.app.data.library.LibraryTagUpdateWriter
import elovaire.music.droidbeauty.app.data.playback.PlaybackNowPlayingState
import elovaire.music.droidbeauty.app.data.playback.PlaybackQueueState
import elovaire.music.droidbeauty.app.data.playback.PlaybackReader
import elovaire.music.droidbeauty.app.data.playback.PlaybackTransportState
import elovaire.music.droidbeauty.app.data.playback.PlaybackVolumeState
import elovaire.music.droidbeauty.app.data.playback.PlaybackCollectionKind
import elovaire.music.droidbeauty.app.data.playback.RecentPlaybackState
import elovaire.music.droidbeauty.app.data.settings.EqualizerSettingsStore
import elovaire.music.droidbeauty.app.data.settings.UserDataReadiness
import elovaire.music.droidbeauty.app.data.settings.UserDataSnapshot
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.data.settings.SearchSettingsStore
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditor
import elovaire.music.droidbeauty.app.data.tags.TagEditApplyResult
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeLibraryReader(
    initialContent: LibraryContentState = LibraryContentState(),
    initialScan: LibraryScanState = LibraryScanState(),
) : LibraryReader {
    val mutableContentState = MutableStateFlow(initialContent)
    val mutableScanState = MutableStateFlow(initialScan)
    override val contentState: StateFlow<LibraryContentState> = mutableContentState.asStateFlow()
    override val scanState: StateFlow<LibraryScanState> = mutableScanState.asStateFlow()
}

internal class FakePlaybackReader : PlaybackReader {
    override val nowPlayingState = MutableStateFlow(PlaybackNowPlayingState())
    override val transportState = MutableStateFlow(PlaybackTransportState())
    override val queueState = MutableStateFlow(PlaybackQueueState())
    override val volumeState = MutableStateFlow(PlaybackVolumeState())
    override val recentPlaybackState = MutableStateFlow(RecentPlaybackState())
}

internal class FakeSearchSettingsStore : SearchSettingsStore {
    override val albumPlayCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    override val searchHistory = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())

    override fun addSearchHistoryEntry(entry: SearchHistoryEntry) {
        searchHistory.value = listOf(entry) + searchHistory.value
    }

    override fun clearSearchHistory() {
        searchHistory.value = emptyList()
    }
}

internal class FakeEqualizerSettingsStore(
    initialSettings: EqSettings = EqSettings(),
) : EqualizerSettingsStore {
    override val eqSettings = MutableStateFlow(initialSettings)
    val writes = mutableListOf<EqSettings>()

    override fun setEqSettings(settings: EqSettings) {
        writes += settings
        eqSettings.value = settings
    }
}

internal class FakeRootSettingsReader : RootSettingsReader {
    override val eqSettings = MutableStateFlow(EqSettings())
    override val themeMode = MutableStateFlow(ThemeMode.System)
    override val textSizePreset = MutableStateFlow(TextSizePreset.Default)
    override val appLanguage = MutableStateFlow(AppLanguage.English)
    override val albumCollectionLayoutMode = MutableStateFlow("Grid")
    override val songCollectionGridEnabled = MutableStateFlow(true)
    override val albumCollectionSortMode = MutableStateFlow("Artist")
    override val songCollectionSortMode = MutableStateFlow("Title")
    override val volumeNormalizationEnabled = MutableStateFlow(false)
    override val onlineLyricsEnabled = MutableStateFlow(true)
    override val nowPlayingBarStyle = MutableStateFlow(NowPlayingBarStyle.Floating)
    override val crossfadeDurationMs = MutableStateFlow(0L)
    override val crossfadeSilenceThresholdDb = MutableStateFlow(-60f)
    override val userDataReadiness = MutableStateFlow(UserDataReadiness.Ready)
    override val userDataSnapshot = MutableStateFlow(UserDataSnapshot())
    override val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    override val smartPlaylists = MutableStateFlow<List<SmartPlaylist>>(emptyList())
    override val favoriteSongIds = MutableStateFlow<List<Long>>(emptyList())
    override val albumPlayCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    override val songPlayCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    override val recentSongIds = MutableStateFlow<List<Long>>(emptyList())
    override val recentAlbumIds = MutableStateFlow<List<Long>>(emptyList())
    override val lastPlayedCollectionKind = MutableStateFlow<PlaybackCollectionKind?>(null)
    override val lastPlayedCollectionId = MutableStateFlow<Long?>(null)
}

internal class FakeLibraryTagUpdateWriter : LibraryTagUpdateWriter {
    val editedSongs = mutableListOf<Song>()

    override suspend fun applyVerifiedTagEdits(editedSongs: List<Song>) {
        this.editedSongs += editedSongs
    }
}

internal class FakeAlbumTagEditor(
    var result: TagEditApplyResult = TagEditApplyResult(
        editedSongIds = emptyList(),
        editedUris = emptyList(),
        editedFilePaths = emptyList(),
        editedSongs = emptyList(),
        artworkChanged = false,
    ),
) : AlbumTagEditor {
    val requests = mutableListOf<AlbumTagEditRequest>()

    override suspend fun applyEdits(
        request: AlbumTagEditRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult {
        requests += request
        return result
    }
}

internal fun testSong(
    id: Long = 1L,
    title: String = "Song $id",
    artist: String = "Artist",
    album: String = "Album",
    albumId: Long = 1L,
) = Song(
    id = id,
    title = title,
    isExplicit = false,
    artist = artist,
    album = album,
    releaseYear = 2024,
    genre = "Genre",
    audioFormat = "mp3",
    audioQuality = "320 kbps",
    fileName = "$title.mp3",
    albumId = albumId,
    durationMs = 180_000L,
    trackNumber = id.toInt(),
    discNumber = 1,
    dateAddedSeconds = 1L,
    uri = TestUri("content://songs/$id"),
    artUri = TestUri("content://art/$albumId"),
)

internal fun testAlbum(
    id: Long = 1L,
    title: String = "Album",
    songs: List<Song> = listOf(testSong(albumId = id)),
) = Album(
    id = id,
    title = title,
    artist = songs.firstOrNull()?.artist ?: "Artist",
    artUri = null,
    songCount = songs.size,
    durationMs = songs.sumOf(Song::durationMs),
    songs = songs,
)
