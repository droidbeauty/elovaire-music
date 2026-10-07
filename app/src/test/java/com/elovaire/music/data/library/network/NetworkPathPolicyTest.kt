package elovaire.music.droidbeauty.app.data.library.network

import elovaire.music.droidbeauty.app.data.library.networkFilterFingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkPathPolicyTest {
    @Test
    fun filterFingerprintUsesLocationNotCredentialsOrCaseFoldedPaths() {
        val source = NetworkLibrarySource(
            id = "nas",
            name = "NAS",
            protocol = NetworkLibraryProtocol.WebDav,
            server = "https://nas.example:443/base",
            shareOrPath = "Music/Live",
            username = "first",
            credentialKey = "credential-a",
        )

        val changedCredentials = source.copy(username = "second", credentialKey = "credential-b")
        val changedPathCase = source.copy(shareOrPath = "Music/live")

        assertEquals(
            networkFilterFingerprint(listOf(source)),
            networkFilterFingerprint(listOf(changedCredentials)),
        )
        org.junit.Assert.assertNotEquals(
            networkFilterFingerprint(listOf(source)),
            networkFilterFingerprint(listOf(changedPathCase)),
        )
    }

    @Test
    fun relativePathsCannotEscapeTheConfiguredRoot() {
        assertEquals("Music/Albums/track.mp3", NetworkPathPolicy.normalizeRelativePath("/Music/./Albums/../Albums/track.mp3"))
        assertEquals("Music", NetworkPathPolicy.normalizeRelativePath("../../Music"))
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

    @Test
    fun stableServerEntryIdentitySurvivesRename() {
        val before = NetworkSourceIdentity.songId("source", "Music/old.mp3", "42")
        val after = NetworkSourceIdentity.songId("source", "Music/new.mp3", "42")

        assertEquals(before, after)
    }

    @Test
    fun webDavPathsEncodeSpecialCharactersWithoutChangingSegments() {
        assertEquals("Music/A%20%23%3F%25/%E6%AD%8C.mp3", NetworkPathPolicy.encodePath("Music/A #?%/歌.mp3"))
        assertNull(NetworkPathPolicy.validateRelativePath("Music/../outside.mp3"))
        assertNull(NetworkPathPolicy.validateRelativePath("Music\\outside.mp3"))
        assertNull(NetworkPathPolicy.validateRelativePath("Music/track\u0000.mp3"))
    }

    @Test
    fun relativePathValidationHandlesManySegments() {
        val path = (0 until 2_048).joinToString("/") { "folder$it" } + "/track.mp3"

        assertEquals(path, NetworkPathPolicy.validateRelativePath(path))
    }

    @Test
    fun webDavResourceUrlValidatesEndpointAndUsesConfiguredBasePath() {
        assertEquals(
            "https://nas.example/music/track.mp3",
            NetworkPathPolicy.webDavResourceUrl("https://nas.example/music/", "track.mp3")?.toString(),
        )
        assertNull(NetworkPathPolicy.webDavResourceUrl("http://nas.example/music", "track.mp3"))
        assertNull(NetworkPathPolicy.webDavResourceUrl("nas.example/music", "track.mp3"))
        assertNull(NetworkPathPolicy.webDavResourceUrl("https://user:password@nas.example/music", "track.mp3"))
        assertNull(NetworkPathPolicy.webDavResourceUrl("https://nas.example/Music/%2e%2e/private", "track.mp3"))
        assertNull(NetworkPathPolicy.webDavResourceUrl("https://nas.example/Music", "../private/track.mp3"))
        assertNull(
            NetworkPathPolicy.webDavConfiguredRoot(
                NetworkLibrarySource(
                    id = "id",
                    name = "NAS",
                    protocol = NetworkLibraryProtocol.WebDav,
                    server = "https://nas.example/Music/%2e%2e/private",
                    shareOrPath = "Music",
                    username = "",
                    credentialKey = "credential",
                ),
            ),
        )
        assertNull(
            NetworkPathPolicy.webDavConfiguredRoot(
                NetworkLibrarySource(
                    id = "id",
                    name = "NAS",
                    protocol = NetworkLibraryProtocol.WebDav,
                    server = "https://nas.example/Music",
                    shareOrPath = "../private",
                    username = "",
                    credentialKey = "credential",
                ),
            ),
        )
    }

    @Test
    fun smbIpv6HostAndPortRemainIntact() {
        assertEquals("2001:db8::20", NetworkPathPolicy.smbServer("[2001:db8::20]:1445"))
        assertEquals(1445, NetworkPathPolicy.smbPort("[2001:db8::20]:1445"))
    }

    @Test
    fun malformedSmbPortsAreRejectedInsteadOfSilentlyUsingTheDefault() {
        listOf("nas.example:abc", "nas.example:70000", "[2001:db8::20]:abc", "2001:db8::20:1445").forEach { endpoint ->
            assertNull(NetworkPathPolicy.smbServer(endpoint))
        }
    }

    @Test
    fun smbSchemeIsCaseInsensitive() {
        assertEquals("nas.example", NetworkPathPolicy.smbServer("SMB://nas.example"))
        assertEquals(445, NetworkPathPolicy.smbPort("SMB://nas.example"))
    }

    @Test
    fun webDavDecodedPathRejectsTraversalAndEncodedSeparators() {
        assertEquals("Music/A #?%/歌.mp3", NetworkPathPolicy.decodeUriPath("/Music/A%20%23%3F%25/%E6%AD%8C.mp3"))
        assertNull(NetworkPathPolicy.decodeUriPath("/Music/../outside.mp3"))
        assertNull(NetworkPathPolicy.decodeUriPath("/Music/%2Foutside.mp3"))
        assertNull(NetworkPathPolicy.decodeUriPath("/Music/%5Coutside.mp3"))
    }
}
