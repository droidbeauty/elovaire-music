package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test
import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.AudiobookPart
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.playback.AudiobookProgress
import elovaire.music.droidbeauty.app.data.playback.NowPlayingPlayback
import java.lang.reflect.Proxy

class HomeLayoutTest {
    @Test
    fun restoredLayoutRepairsMissingDuplicateAndUnknownModules() {
        val restored = parseHomeLayout("FavoriteAlbums,FavoriteAlbums,RecentlyPlayed,unknown,LastPlayed")
        assertEquals(HomeModule.RecentlyAdded, restored[0])
        assertEquals(HomeModule.entries.toSet(), restored.toSet())
        assertEquals(HomeModule.entries.size, restored.size)
        assertEquals(HomeModule.FavoriteAlbums, restored[1])
        assertEquals(defaultHomeLayout, parseHomeLayout(null))
    }

    @Test
    fun movableModulesCanBeReorderedWhileRecentlyAddedStaysFirst() {
        val first = moveHomeModule(defaultHomeLayout, HomeModule.LastPlayed, 1)
        assertEquals(HomeModule.RecentlyAdded, first[0])
        assertEquals(HomeModule.RecentlyPlayed, first[1])
        assertEquals(HomeModule.LastPlayed, first[2])
        val second = moveHomeModule(first, HomeModule.LastPlayed, -1)
        assertEquals(defaultHomeLayout, second)
        assertEquals(defaultHomeLayout, moveHomeModule(defaultHomeLayout, HomeModule.RecentlyAdded, 1))
        assertEquals(defaultHomeLayout, moveHomeModule(defaultHomeLayout, HomeModule.LastPlayed, -1))
    }

    @Test
    fun savedOrderRoundTrips() {
        val order = moveHomeModule(defaultHomeLayout, HomeModule.FavoriteAlbums, -1)
        assertEquals(order, parseHomeLayout(order.joinToString(",") { it.name }))
    }

    @Test
    fun audiobookResumeRewindStaysWithinTheCurrentFile() {
        val first = song(1, 90_000)
        val second = song(2, 90_000)
        val songs = listOf(first, second)
        assertEquals(second to 45_000L, audiobookResumeTarget(songs, 1, 60_000L, 15))
        assertEquals(first to 79_000L, audiobookResumeTarget(songs, 1, 4_000L, 15))
        assertEquals(first to 0L, audiobookResumeTarget(songs, 0, 4_000L, 15))
        assertEquals(second to 75_000L, audiobookResumeTarget(songs, 1, 120_000L, 15))
        assertEquals(second to 60_000L, audiobookResumeTarget(songs, 1, 60_000L, 0))
        assertEquals(second to 0L, audiobookResumeTarget(listOf(song(1, 0), second), 1, 4_000L, 15))
    }

    @Test
    fun playbackUsesRewindOnlyWhenResumingNotWhenSelectingAChapter() {
        val first = song(1, 90_000)
        val second = song(2, 90_000)
        val parts = listOf(AudiobookPart(first, 1), AudiobookPart(second, 2, startMs = 20_000))
        val book = Audiobook("book", "Book", "Author", null, 180_000, parts)
        var result: Pair<Long, Long>? = null
        val playback = Proxy.newProxyInstance(NowPlayingPlayback::class.java.classLoader, arrayOf(NowPlayingPlayback::class.java)) { _, method, args ->
            when (method.name) {
                "audiobookResumeSongId" -> 2L
                "audiobookProgress" -> AudiobookProgress(2, 4_000, false, 0)
                "playSongAtPosition" -> { result = (args[0] as Song).id to (args[2] as Long); Unit }
                else -> error("Unexpected call ${method.name}")
            }
        } as NowPlayingPlayback
        val actions = RootPlaybackActions(playback, { AppLanguage.English }, { emptyMap() }, { emptyMap() }, {})
        actions.playAudiobook(book, resume = true, rewindOnResumeSeconds = 15)
        assertEquals(1L to 79_000L, result)
        actions.playAudiobook(book, part = parts[1], resume = false, rewindOnResumeSeconds = 15)
        assertEquals(2L to 20_000L, result)
    }

    private fun song(id: Long, duration: Long) = Song(
        id, "Part", false, "Author", "Book", null, "", "m4b", null, "$id.m4b", id,
        duration, 1, 1, 0, uri = TestUri("content://test/$id"), artUri = null,
    )
}
