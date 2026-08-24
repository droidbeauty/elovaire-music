package elovaire.music.droidbeauty.app.data.settings

import elovaire.music.droidbeauty.app.data.library.LibraryFolderSelection
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryEntry
import elovaire.music.droidbeauty.app.domain.model.SearchHistoryKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceCollectionCodecTest {
    @Test
    fun corruptPlayCountsAreDiscardedWithoutLosingValidEntries() {
        val decoded = PreferenceCollectionCodec.deserializePlayCounts("1:3,broken,2:-4,-1:9,0:7,3:x")

        assertEquals(mapOf(1L to 3, 2L to 0, -1L to 9), decoded)
    }

    @Test
    fun corruptSearchHistoryIsRejected() {
        assertEquals(null, PreferenceCollectionCodec.deserializeSearchHistory("broken"))
    }

    @Test
    fun corruptLibraryFolderIsRejected() {
        assertEquals(null, PreferenceCollectionCodec.deserializeLibraryFolder("broken"))
    }

    @Test
    fun emptyPlayCountsRemainEmpty() {
        assertTrue(PreferenceCollectionCodec.deserializePlayCounts("").isEmpty())
    }

    @Test
    fun separatorCharactersRoundTripInSearchAndFolderFields() {
        val search = SearchHistoryEntry(
            key = "key\u001E\u001F",
            kind = SearchHistoryKind.Album,
            title = "title\u001F",
            subtitle = "subtitle\u001E",
            artUri = null,
            albumId = -7L,
            query = "query\u001E\u001F",
        )
        val folder = LibraryFolderSelection(
            uri = null,
            path = "path\u001F\u001E",
            displayName = "display\u001E\u001F",
            isDefaultMusicFolder = true,
        )

        assertEquals(search, PreferenceCollectionCodec.deserializeSearchHistory(
            PreferenceCollectionCodec.serializeSearchHistory(search),
        ))
        assertEquals(folder, PreferenceCollectionCodec.deserializeLibraryFolder(
            PreferenceCollectionCodec.serializeLibraryFolder(folder),
        ))
    }
}
