package elovaire.music.droidbeauty.app.data.playlists

import elovaire.music.droidbeauty.app.domain.model.Playlist
import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistOperationStateMachineTest {
    @Test
    fun generatedPlaylistOperationsConvergeWithReferenceModel() {
        val seeds = listOf(0x1741L, 0x5EEDL, 0xC0FFEE)
        seeds.forEach { seed ->
            runSequence(seed)
        }
    }

    @Test
    fun playlistNormalizationAndSerializationAreIdempotent() {
        val source = listOf(
            Playlist(id = 1L, name = "  Cafe\u0301   🎵  ", songIds = listOf(4L, 4L, 0L, -2L)),
            Playlist(id = 2L, name = "Built in", isSystem = true),
        )

        val once = deserializePlaylists(serializePlaylists(source))
        val twice = deserializePlaylists(serializePlaylists(once))

        assertEquals(once, twice)
        assertEquals(listOf(4L, -2L), once.single().songIds)
        assertEquals("Café 🎵", once.single().name)
    }

    private fun runSequence(seed: Long) {
        val random = Random(seed)
        var production = emptyList<Playlist>()
        var reference = emptyList<Playlist>()
        var nextPlaylistId = 1L

        repeat(250) { step ->
            val playlistId = randomPlaylistId(random, production)
            when (random.nextInt(7)) {
                0 -> {
                    val result = createPlaylistEntries(
                        playlists = production,
                        name = randomName(random),
                        nextPlaylistId = nextPlaylistId,
                    )
                    if (result != null) {
                        production = result.playlists
                        reference = listOf(result.createdPlaylist) + reference
                        nextPlaylistId = result.nextPlaylistId
                    }
                }

                1 -> {
                    val name = randomName(random)
                    val result = renamePlaylistEntry(production, playlistId, name)
                    if (result != null) production = result
                    val normalized = normalizePlaylistName(name)
                    if (normalized.isNotBlank()) {
                        reference = reference.map { playlist ->
                            if (playlist.id == playlistId && !playlist.isSystem) {
                                playlist.copy(name = normalized)
                            } else {
                                playlist
                            }
                        }
                    }
                }

                2 -> {
                    val songIds = randomSongIds(random)
                    val result = addSongsToPlaylistEntries(production, playlistId, songIds)
                    if (result != null) production = result
                    val normalized = normalizePlaylistSongIds(songIds)
                    if (normalized.isNotEmpty()) {
                        reference = reference.map { playlist ->
                            if (playlist.id == playlistId && !playlist.isSystem) {
                                playlist.copy(songIds = normalizePlaylistSongIds(playlist.songIds + normalized))
                            } else {
                                playlist
                            }
                        }
                    }
                }

                3 -> {
                    val songIds = randomSongIds(random)
                    val result = updatePlaylistSongIdsEntry(production, playlistId, songIds)
                    if (result != null) production = result
                    val normalized = normalizePlaylistSongIds(songIds)
                    reference = reference.map { playlist ->
                        if (playlist.id == playlistId && !playlist.isSystem) {
                            playlist.copy(songIds = normalized)
                        } else {
                            playlist
                        }
                    }
                }

                4 -> {
                    val ids = randomSongIds(random).toSet()
                    val result = removeSongReferencesFromPlaylists(production, ids)
                    if (result != null) production = result
                    val validIds = ids.filterTo(hashSetOf()) { it != 0L }
                    if (validIds.isNotEmpty()) {
                        reference = reference.map { playlist ->
                            playlist.copy(songIds = playlist.songIds.filterNot(validIds::contains))
                        }
                    }
                }

                5 -> {
                    val result = deletePlaylistEntries(production, setOf(playlistId))
                    if (result != null) production = result
                    reference = reference.filterNot { it.id == playlistId && !it.isSystem }
                }

                else -> {
                    production = deserializePlaylists(serializePlaylists(production))
                    reference = deserializePlaylists(serializePlaylists(reference))
                }
            }

            assertEquals("seed=$seed step=$step", reference, production)
        }
    }

    private fun randomPlaylistId(random: Random, playlists: List<Playlist>): Long {
        return when {
            playlists.isEmpty() || random.nextBoolean() -> random.nextInt(8).toLong() + 1L
            else -> playlists[random.nextInt(playlists.size)].id
        }
    }

    private fun randomName(random: Random): String = when (random.nextInt(5)) {
        0 -> "  Road   Trip ${random.nextInt(6)}  "
        1 -> "東京 🎵 ${random.nextInt(6)}"
        2 -> "Cafe\u0301 ${random.nextInt(6)}"
        3 -> "   "
        else -> "Mix ${random.nextInt(6)}"
    }

    private fun randomSongIds(random: Random): List<Long> {
        return List(random.nextInt(8)) {
            when (random.nextInt(8)) {
                0 -> 0L
                1 -> -random.nextInt(4).toLong()
                else -> random.nextInt(12).toLong() + 1L
            }
        }
    }
}
