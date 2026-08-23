package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkPathPolicyTest {
    @Test
    fun relativePathsCannotEscapeTheConfiguredRoot() {
        assertEquals("Music/Albums/track.mp3", NetworkPathPolicy.normalizeRelativePath("/Music/./Albums/../Albums/track.mp3"))
        assertEquals("Music", NetworkPathPolicy.normalizeRelativePath("../../Music"))
    }

    @Test
    fun webDavRequiresHttpsAndHost() {
        assertEquals("https://nas.example/music", NetworkPathPolicy.webDavBaseUrl("https://nas.example/music/"))
        assertEquals(null, NetworkPathPolicy.webDavBaseUrl("http://nas.example/music"))
        assertEquals(null, NetworkPathPolicy.webDavBaseUrl("nas.example/music"))
        assertEquals(null, NetworkPathPolicy.webDavBaseUrl("https://user:password@nas.example/music"))
    }

    @Test
    fun smbShareAndPathAreSeparatedWithoutLeadingSlashes() {
        assertEquals("Music" to "Albums", NetworkPathPolicy.smbShareAndPath("/Music/Albums"))
        assertEquals(null, NetworkPathPolicy.smbShareAndPath("/"))
    }

    @Test
    fun sourceIdentityIsStableForEquivalentPaths() {
        val source = NetworkLibrarySource(
            id = "id",
            name = "NAS",
            protocol = NetworkLibraryProtocol.Smb,
            server = "NAS.EXAMPLE",
            shareOrPath = "/Music/",
            username = "User",
            credentialKey = "secret",
        )
        val equivalent = source.copy(server = "nas.example/", shareOrPath = "Music")
        assertEquals(NetworkSourceIdentity.stableKey(source), NetworkSourceIdentity.stableKey(equivalent))
    }

    @Test
    fun networkSongIdsAreStableAndReservedOutsideMediaStoreIds() {
        val first = NetworkSourceIdentity.songId("source", "Music/track.mp3")
        val second = NetworkSourceIdentity.songId("source", "Music/other.mp3")
        assertTrue(first < 0L)
        assertNotEquals(first, second)
        assertEquals(first, NetworkSourceIdentity.songId("source", "./Music/track.mp3"))
    }
}
