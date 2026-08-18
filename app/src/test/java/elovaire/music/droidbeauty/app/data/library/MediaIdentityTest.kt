package elovaire.music.droidbeauty.app.data.library

import android.net.TestUri
import elovaire.music.droidbeauty.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaIdentityTest {
    @Test
    fun mediaStoreIdentityIncludesVolume() {
        val primary = MediaIdentityResolver.mediaStore("external_primary", 42L)
        val removable = MediaIdentityResolver.mediaStore("1234-5678", 42L)

        assertNotEquals(primary, removable)
        assertNotEquals(primary?.stableKey, removable?.stableKey)
    }

    @Test
    fun safIdentityUsesAuthorityAndDocumentNotDisplayNameOrTree() {
        val first = MediaIdentityResolver.safDocument("provider.one", "folder/song.mp3", "root-a")
        val overlappingTree = MediaIdentityResolver.safDocument("provider.one", "folder/song.mp3", "root-b")
        val otherProvider = MediaIdentityResolver.safDocument("provider.two", "folder/song.mp3", "root-a")

        assertEquals(first?.stableKey, overlappingTree?.stableKey)
        assertNotEquals(first?.stableKey, otherProvider?.stableKey)
    }

    @Test
    fun invalidIdentityPartsAreRejected() {
        assertNull(MediaIdentityResolver.mediaStore("", 1L))
        assertNull(MediaIdentityResolver.mediaStore("external", -1L))
        assertNull(MediaIdentityResolver.safDocument("provider", ""))
    }

    @Test
    fun mediaStoreAlbumArtworkUsesTheProviderArtworkEndpoint() {
        assertEquals(
            "content://media/external/audio/albumart/42",
            mediaStoreAlbumArtworkUriString("external", 42L),
        )
        assertNull(mediaStoreAlbumArtworkUriString("external", -1L))
    }

    @Test
    fun revisionChangesWithoutChangingIdentity() {
        val original = song(modifiedSeconds = 10L)
        val edited = song(modifiedSeconds = 11L)

        assertEquals(MediaIdentityResolver.stableKey(original), MediaIdentityResolver.stableKey(edited))
        assertNotEquals(MediaIdentityResolver.revision(original), MediaIdentityResolver.revision(edited))
    }

    @Test
    fun sourceRevisionIsIndependentOfLocatorAndChangesWithContentSignals() {
        val first = MediaIdentityResolver.sourceRevisionKey(1_000L, 10L)
        val same = MediaIdentityResolver.sourceRevisionKey(1_000L, 10L)
        val changed = MediaIdentityResolver.sourceRevisionKey(1_000L, 11L)

        assertEquals(first, same)
        assertNotEquals(first, changed)
    }

    @Test
    fun unknownContentProviderDoesNotFallBackToMutablePath() {
        val song = song(10L).copy(uri = TestUri("content://example.provider/item/1"))

        assertEquals(
            "uri:content://example.provider/item/1",
            MediaIdentityResolver.stableKey(song),
        )
    }

    private fun song(modifiedSeconds: Long): Song = Song(
        id = 1L,
        title = "Title",
        isExplicit = false,
        artist = "Artist",
        album = "Album",
        releaseYear = null,
        genre = "",
        audioFormat = "MP3",
        audioQuality = null,
        fileName = "song.mp3",
        albumId = 1L,
        durationMs = 1_000L,
        trackNumber = 1,
        discNumber = 1,
        dateAddedSeconds = 1L,
        dateModifiedSeconds = modifiedSeconds,
        libraryPath = "/storage/emulated/0/Music/song.mp3",
        uri = TestUri("content://media/external/audio/media/1"),
        artUri = null,
        metadataResolved = true,
    )
}
