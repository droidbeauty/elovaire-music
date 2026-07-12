package elovaire.music.droidbeauty.app.data.playback.library

import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class MediaTreeSnapshotCacheTest {
    @Test
    fun unchangedStateFlowValuesReuseDerivedSnapshot() {
        val songs = emptyList<Song>()
        val albums = emptyList<Album>()
        val playlists = emptyList<Playlist>()
        val favorites = emptyList<Long>()
        val recent = emptyList<Long>()
        val cache = MediaTreeSnapshotCache()
        val first = cache.snapshot(
            permissionGranted = true,
            songs = songs,
            albums = albums,
            playlists = playlists,
            favoriteSongIds = favorites,
            recentSongIds = recent,
            lastPlayedCollectionKind = null,
            lastPlayedCollectionId = null,
        )
        val second = cache.snapshot(
            true, songs, albums, playlists, favorites, recent, null, null,
        )
        val changed = cache.snapshot(
            true, songs, albums, playlists, listOf(1L), recent, null, null,
        )

        assertSame(first, second)
        assertNotSame(second, changed)
    }
}
