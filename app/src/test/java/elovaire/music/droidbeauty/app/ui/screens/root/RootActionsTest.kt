package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.core.PlaylistActionDependencies
import elovaire.music.droidbeauty.app.core.SettingsActionDependencies
import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsStore
import elovaire.music.droidbeauty.app.data.settings.AppearanceSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.FavoritesStore
import elovaire.music.droidbeauty.app.data.settings.LibrarySettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaybackSettingsWriter
import elovaire.music.droidbeauty.app.data.settings.PlaylistStore
import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.data.smartplaylists.BuiltInSmartPlaylistType
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.AudiobookSettings
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.NowPlayingBarStyle
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RootActionsTest {
    @Test
    fun playlistActions_delegateThroughNarrowDependencies() = runBlocking {
        val store = FakePlaylistStore()
        val favorites = FakeFavoritesStore()
        val actions = RootPlaylistActions(
            object : PlaylistActionDependencies {
                override val playlistStore = store
                override val favoritesStore = favorites
            },
        )

        val result = actions.createPlaylistAndAddSongs("Road", listOf(7L, 9L)).await()
        actions.toggleFavorite(7L)
        actions.setSongsFavorite(listOf(7L, 9L), true)

        assertEquals(PlaylistMutationResult.Success(42L), result)
        assertEquals("Road", store.createdName)
        assertEquals(42L to listOf(7L, 9L), store.createdSongs)
        assertEquals(7L, favorites.toggledSongId)
        assertEquals(listOf(7L, 9L) to true, favorites.favoriteBatch)
    }

    @Test
    fun settingsActions_delegateThroughFeatureScopedWriters() {
        val appearance = FakeAppearanceSettingsWriter()
        val playback = FakePlaybackSettingsWriter()
        val actions = SettingsRouteActions(
            object : SettingsActionDependencies {
                override val appearanceSettings = appearance
                override val appearanceSettingsReader = FakeAppearanceSettingsStore()
                override val librarySettings = FakeLibrarySettingsWriter()
                override val playbackSettings = playback
            },
        )

        actions.setAppLanguage(AppLanguage.Polish)
        actions.updateTreble(0.75f)

        assertEquals(AppLanguage.Polish, appearance.language)
        assertEquals(0.75f, playback.treble)
    }
}

private class FakePlaylistStore : PlaylistStore {
    var createdName: String? = null
    var createdSongs: Pair<Long, List<Long>>? = null

    override fun createPlaylist(name: String) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(42L))

    override fun createPlaylistWithSongs(name: String, songIds: List<Long>) = CompletableDeferred<PlaylistMutationResult>().also {
        createdName = name
        createdSongs = 42L to songIds
        it.complete(PlaylistMutationResult.Success(42L))
    }

    override fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(playlistId))

    override fun renamePlaylist(playlistId: Long, name: String) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(playlistId))
    override fun updatePlaylistSongIds(playlistId: Long, songIds: List<Long>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(playlistId))
    override fun importPlaylists(playlists: List<Playlist>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success())
    override fun deletePlaylists(playlistIds: Set<Long>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success())
    override fun removeSongReferences(songIds: Set<Long>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success())
    override fun createSmartPlaylist(name: String) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(42L))
    override fun createSmartPlaylist(playlist: SmartPlaylist) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(42L))
    override fun updateSmartPlaylist(playlist: SmartPlaylist) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success(playlist.id))
    override fun deleteSmartPlaylists(playlistIds: Set<Long>) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success())
}

private class FakeFavoritesStore : FavoritesStore {
    var toggledSongId: Long? = null
    var favoriteBatch: Pair<List<Long>, Boolean>? = null

    override fun toggleFavoriteSong(songId: Long) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success()) .also {
        toggledSongId = songId
    }

    override fun setFavoriteSongs(songIds: List<Long>, favorite: Boolean) = CompletableDeferred<PlaylistMutationResult>(PlaylistMutationResult.Success()).also {
        favoriteBatch = songIds to favorite
    }
}

private class FakeAppearanceSettingsWriter : AppearanceSettingsWriter {
    var language: AppLanguage? = null

    override fun setThemeMode(themeMode: ThemeMode) = Unit
    override fun setTextSizePreset(textSizePreset: TextSizePreset) = Unit
    override fun setNowPlayingBarStyle(style: NowPlayingBarStyle) = Unit

    override fun setAppLanguage(language: AppLanguage) {
        this.language = language
    }
}

private class FakeAppearanceSettingsStore : AppearanceSettingsStore {
    override val eqSettings = MutableStateFlow(EqSettings())
    override val themeMode = MutableStateFlow(ThemeMode.System)
    override val textSizePreset = MutableStateFlow(TextSizePreset.Default)
    override val appLanguage = MutableStateFlow(AppLanguage.English)
    override val albumCollectionLayoutMode = MutableStateFlow("List")
    override val songCollectionGridEnabled = MutableStateFlow(false)
    override val albumCollectionSortMode = MutableStateFlow("Title")
    override val songCollectionSortMode = MutableStateFlow("Title")
    override val volumeNormalizationEnabled = MutableStateFlow(false)
    override val onlineLyricsEnabled = MutableStateFlow(false)
    override val nowPlayingBarStyle = MutableStateFlow(NowPlayingBarStyle.Compact)
    override val crossfadeDurationMs = MutableStateFlow(2_500L)
    override val crossfadeSilenceThresholdDb = MutableStateFlow(-80f)
    override val audiobookSettings = MutableStateFlow(AudiobookSettings())
    override val smartPlaylistEnabledTypes = MutableStateFlow(BuiltInSmartPlaylistType.entries.toSet())
    override val smartPlaylistMaxSongs = MutableStateFlow(30)
}

private class FakeLibrarySettingsWriter : LibrarySettingsWriter {
    override val libraryFolders = MutableStateFlow(emptyList<LibraryFolderSelection>())

    override fun addLibraryFolder(selection: LibraryFolderSelection) = Unit
    override fun removeLibraryFolder(selection: LibraryFolderSelection) = Unit
    override fun setLibraryFolders(selections: List<LibraryFolderSelection>) = Unit
    override fun restoreDefaultLibraryFolderIfEmpty() = Unit
    override fun setAlbumCollectionLayoutMode(mode: String) = Unit
    override fun setSongCollectionGridEnabled(enabled: Boolean) = Unit
    override fun setAlbumCollectionSortMode(sortMode: String) = Unit
    override fun setSongCollectionSortMode(sortMode: String) = Unit
}

private class FakePlaybackSettingsWriter : PlaybackSettingsWriter {
    var treble: Float? = null

    override fun setPlaybackVolume(value: Float) = Unit
    override fun setCrossfadeEnabled(enabled: Boolean) = Unit
    override fun setCrossfadeDurationMs(value: Long) = Unit
    override fun setCrossfadeSilenceThresholdDb(value: Float) = Unit
    override fun setVolumeNormalizationEnabled(enabled: Boolean) = Unit
    override fun setOnlineLyricsEnabled(enabled: Boolean) = Unit
    override fun setAudiobookRewindSeconds(value: Int) = Unit
    override fun setAudiobookForwardSeconds(value: Int) = Unit
    override fun setAudiobookResumePlayback(enabled: Boolean) = Unit
    override fun setSmartPlaylistEnabled(type: BuiltInSmartPlaylistType, enabled: Boolean) = Unit
    override fun setSmartPlaylistMaxSongs(value: Int) = Unit
    override fun updateBass(value: Float) = Unit
    override fun updateMidrange(value: Float) = Unit

    override fun updateTreble(value: Float) {
        treble = value
    }

}
