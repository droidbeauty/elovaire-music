package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebDavMultiStatusTest {
    @Test
    fun failedPropstatDoesNotSupplyMetadata() {
        val source = NetworkLibrarySource(
            id = "dav",
            name = "Library",
            protocol = NetworkLibraryProtocol.WebDav,
            server = "https://nas.example",
            shareOrPath = "",
            username = "user",
            credentialKey = "key",
        )
        val body = """
            <multistatus>
              <response>
                <href>https://nas.example/song.mp3</href>
                <propstat>
                  <prop><getcontentlength>10</getcontentlength></prop>
                  <status>HTTP/1.1 200 OK</status>
                </propstat>
                <propstat>
                  <prop><getetag>\"private\"</getetag></prop>
                  <status>HTTP/1.1 404 Not Found</status>
                </propstat>
              </response>
            </multistatus>
        """.trimIndent().toByteArray()

        val filesystem = WebDavNetworkFileSystem()
        assertEquals("song.mp3", filesystem.hrefToPath("https://nas.example/song.mp3", source, ""))
        val entries = filesystem.parseMultiStatus(body, source, "")

        assertEquals(1, entries.size)
        assertEquals("song.mp3", entries.single().path)
        assertEquals(10L, entries.single().sizeBytes)
        assertNull(entries.single().etag)
    }
}
