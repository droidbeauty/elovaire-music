package elovaire.music.droidbeauty.app.data.artist

import org.junit.Assert.assertEquals
import org.junit.Test

class ArtistDiscographyRepositoryTest {
    @Test
    fun selectionKeepsOnlyTheRequestedArtistsAlbumsAndOrdersNewestFirst() {
        assertEquals(
            listOf(
                ArtistDiscographyAlbum("Newest Album", "2024-03-12"),
                ArtistDiscographyAlbum("Older Album", "1998"),
            ),
            selectMusicBrainzArtistDiscography(
                artistName = "  example   artist ",
                groups = listOf(
                    MusicBrainzReleaseGroup(
                        title = "Older Album",
                        primaryType = "Album",
                        firstReleaseDate = "1998",
                        artistNames = listOf("Example Artist"),
                    ),
                    MusicBrainzReleaseGroup(
                        title = "Compilation",
                        primaryType = "Album",
                        firstReleaseDate = "2024-01-01",
                        artistNames = listOf("Various Artists"),
                    ),
                    MusicBrainzReleaseGroup(
                        title = "Not an Album",
                        primaryType = "Single",
                        firstReleaseDate = "2025",
                        artistNames = listOf("Example Artist"),
                    ),
                    MusicBrainzReleaseGroup(
                        title = "Newest Album",
                        primaryType = "Album",
                        firstReleaseDate = "2024-03-12",
                        artistNames = listOf("Example Artist"),
                    ),
                ),
            ),
        )
    }
}
