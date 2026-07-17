package elovaire.music.droidbeauty.app.quality

import elovaire.music.droidbeauty.app.data.lyrics.MAX_LYRICS_CHARACTERS
import elovaire.music.droidbeauty.app.data.lyrics.parseLrcOrPlain
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaId
import elovaire.music.droidbeauty.app.data.playback.library.ElovaireMediaIds
import elovaire.music.droidbeauty.app.domain.kernel.normalizePlaybackQueue
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistName
import elovaire.music.droidbeauty.app.data.playlists.normalizePlaylistSongIds
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendPropertyTest {
    @Test
    fun mediaIdsRoundTripAndMalformedIdsFailClosed() {
        val random = Random(SEED)
        repeat(CASES) { case ->
            val id = random.nextLong().let { if (it == 0L) 1L else it }
            val typedIds = listOf(
                ElovaireMediaId.Song(id),
                ElovaireMediaId.Album(id),
                ElovaireMediaId.Playlist(id),
            )
            typedIds.forEach { typed ->
                assertEquals("seed=$SEED case=$case", typed, ElovaireMediaIds.parse(typed.value))
            }

            val malformed = "invalid:" + randomText(random, random.nextInt(0, 160))
            runCatching { ElovaireMediaIds.parse(malformed) }
                .onFailure { throw AssertionError("seed=$SEED case=$case input=$malformed", it) }
        }
        assertNull(ElovaireMediaIds.parse("x".repeat(1_025)))
    }

    @Test
    fun normalizationIsIdempotentAndQueueIndicesStayValid() {
        val random = Random(SEED)
        repeat(CASES) { case ->
            val ids = List(random.nextInt(0, 200)) { random.nextLong(-30L, 31L) }
            val normalizedIds = normalizePlaylistSongIds(ids)
            assertEquals("seed=$SEED case=$case", normalizedIds, normalizePlaylistSongIds(normalizedIds))
            assertEquals("seed=$SEED case=$case", normalizedIds.size, normalizedIds.toSet().size)
            assertTrue("seed=$SEED case=$case", 0L !in normalizedIds)

            val name = randomText(random, random.nextInt(0, 120))
            val normalizedName = normalizePlaylistName(name)
            assertEquals("seed=$SEED case=$case", normalizedName, normalizePlaylistName(normalizedName))

            val queueSize = random.nextInt(-10, 500)
            val queue = normalizePlaybackQueue(queueSize, random.nextInt(), random.nextLong())
            if (queueSize <= 0) {
                assertEquals("seed=$SEED case=$case", -1, queue.currentIndex)
                assertNull("seed=$SEED case=$case", queue.sourcePlaylistId)
            } else {
                assertTrue("seed=$SEED case=$case", queue.currentIndex in 0 until queueSize)
            }
        }
    }

    @Test
    fun boundedMalformedLyricsAlwaysTerminateWithoutEscapingLimits() {
        val random = Random(SEED)
        repeat(CASES) { case ->
            val input = randomText(random, random.nextInt(0, 1_024))
            val result = runCatching { parseLrcOrPlain(input, "property", 50) }
                .getOrElse { throw AssertionError("seed=$SEED case=$case", it) }
            result?.let { payload ->
                assertTrue("seed=$SEED case=$case", payload.sourceTextForEmbedding.orEmpty().length <= MAX_LYRICS_CHARACTERS)
                assertEquals("seed=$SEED case=$case", payload.lines.indices.toList(), payload.lines.map { it.index })
            }
        }
    }

    private fun randomText(random: Random, length: Int): String = buildString(length) {
        repeat(length) {
            append(ALPHABET[random.nextInt(ALPHABET.size)])
        }
    }

    private companion object {
        const val SEED = 0x5EED_C0DEL
        const val CASES = 1_000
        val ALPHABET = charArrayOf(
            ' ', '\t', '\n', ':', '%', '[', ']', '0', '9', 'A', 'z',
            '\u0000', '\u0301', '\u0130', '\u03A9', '\u4E2D', '\uD83D', '\uDE00',
        )
    }
}
