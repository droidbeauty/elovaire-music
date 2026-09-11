package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import androidx.media3.common.Player
import elovaire.music.droidbeauty.app.domain.model.Song
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("UNCHECKED_CAST")
class PlaybackStateReducerTest {
    @Test
    fun reduceUsesCallerSnapshotForQueueIndexAndVolume() {
        val playerHandler = CountingPlayerHandler()
        val reducer = PlaybackStateReducer(
            playerProvider = { playerHandler.player },
            onRecentPlaybackChanged = { _, _, _, _ -> },
        )
        val existing = PlaybackUiState(queue = listOf(song), currentIndex = 0)
        val resolvedIndex = reducer.resolveCurrentQueueIndex(existing)

        val updated = reducer.reduce(
            existingState = existing,
            isPauseTransitioningToStopped = false,
            resolvedQueueIndex = resolvedIndex,
            displayedVolume = 0.37f,
        )

        assertEquals(1, playerHandler.currentMediaItemIndexReads)
        assertEquals(0.37f, updated.volume)
        assertEquals(0, updated.currentIndex)
    }

    private companion object {
        val song = Song(
            id = 1L,
            title = "Song",
            isExplicit = false,
            artist = "Artist",
            album = "Album",
            releaseYear = null,
            genre = "Genre",
            audioFormat = "MP3",
            audioQuality = null,
            fileName = "song.mp3",
            albumId = 1L,
            durationMs = 1_000L,
            trackNumber = 1,
            discNumber = 1,
            dateAddedSeconds = 0L,
            uri = TestUri("content://media/1"),
            artUri = null,
        )
    }
}

private class CountingPlayerHandler : InvocationHandler {
    var currentMediaItemIndexReads = 0
    val player: Player = Proxy.newProxyInstance(
        Player::class.java.classLoader,
        arrayOf(Player::class.java),
        this,
    ) as Player

    override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
        return when (method.name) {
            "getCurrentMediaItemIndex" -> {
                currentMediaItemIndexReads += 1
                -1
            }
            "getCurrentMediaItem" -> null
            "getCurrentPosition" -> 100L
            "isPlaying", "getPlayWhenReady", "getShuffleModeEnabled" -> false
            "getRepeatMode", "getAudioSessionId" -> 0
            else -> method.returnType.defaultValue()
        }
    }

    private fun Class<*>.defaultValue(): Any? = when {
        this == Boolean::class.javaPrimitiveType -> false
        this == Byte::class.javaPrimitiveType -> 0.toByte()
        this == Short::class.javaPrimitiveType -> 0.toShort()
        this == Int::class.javaPrimitiveType -> 0
        this == Long::class.javaPrimitiveType -> 0L
        this == Float::class.javaPrimitiveType -> 0f
        this == Double::class.javaPrimitiveType -> 0.0
        this == Char::class.javaPrimitiveType -> '\u0000'
        else -> null
    }
}
