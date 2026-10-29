package elovaire.music.droidbeauty.app.data.library.network

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Test

class WebDavRequestTest {
    @Test
    fun failedDirectoryResponseCannotProduceAnEmptySuccessfulListing() {
        val client = OkHttpClient.Builder().followRedirects(false).addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(207).message("Multi-Status")
                .body("<multistatus><response><href>/Music/</href><status>HTTP/1.1 403 Forbidden</status></response></multistatus>".toResponseBody())
                .build()
        }.build()
        val filesystem = WebDavNetworkFileSystem(propFindClient = client)
        val source = NetworkLibrarySource("id", "NAS", NetworkLibraryProtocol.WebDav,
            "https://nas.example", "Music", "user", "key")
        try {
            org.junit.Assert.assertThrows(java.io.IOException::class.java) {
                filesystem.listBlocking(source, NetworkCredentials("user", "pass"), 20, 1)
            }
            org.junit.Assert.assertNotEquals(NetworkAvailability.Available,
                filesystem.probeBlocking(source, NetworkCredentials("user", "pass")).availability)
        } finally {
            filesystem.release()
        }
    }

    @Test
    fun discoverySendsPropfindAndParsesTheResponse() {
        val requests = mutableListOf<okhttp3.Request>()
        val client = OkHttpClient.Builder().followRedirects(false).addInterceptor { chain ->
            val request = chain.request().also(requests::add)
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(207).message("Multi-Status")
                .body("""
                    <multistatus><response><href>/Music/</href><propstat>
                    <prop><resourcetype><collection/></resourcetype></prop>
                    <status>HTTP/1.1 200 OK</status></propstat></response>
                    <response><href>/Music/song.mp3</href><propstat>
                    <prop><getcontentlength>10</getcontentlength><resourcetype/></prop>
                    <status>HTTP/1.1 200 OK</status></propstat></response></multistatus>
                """.trimIndent().toResponseBody()).build()
        }.build()
        val filesystem = WebDavNetworkFileSystem(propFindClient = client)
        val source = NetworkLibrarySource("id", "NAS", NetworkLibraryProtocol.WebDav,
            "https://nas.example", "Music", "user", "key")
        try {
            val listing = filesystem.listBlocking(source, NetworkCredentials("user", "pass"), 20, 1)
            assertEquals("PROPFIND", requests.single().method)
            assertEquals("1", requests.single().header("Depth"))
            assertEquals("Basic dXNlcjpwYXNz", requests.single().header("Authorization"))
            val body = Buffer().also { requests.single().body!!.writeTo(it) }.readUtf8()
            assertEquals(true, body.startsWith("<?xml"))
            assertEquals("Music/song.mp3", listing.entries.single().path)
        } finally {
            filesystem.release()
        }
    }

    @Test
    fun discoveryDoesNotSendCredentialsToRedirectedHost() {
        var count = 0
        val client = OkHttpClient.Builder().followRedirects(false).addInterceptor { chain ->
            count++
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(302).message("Found")
                .header("Location", "https://other.example/Music")
                .body("".toResponseBody()).build()
        }.build()
        val filesystem = WebDavNetworkFileSystem(propFindClient = client)
        val source = NetworkLibrarySource("id", "NAS", NetworkLibraryProtocol.WebDav,
            "https://nas.example", "Music", "user", "key")
        try {
            org.junit.Assert.assertThrows(NetworkRedirectException::class.java) {
                filesystem.listBlocking(source, NetworkCredentials("user", "pass"), 20, 1)
            }
            assertEquals(1, count)
        } finally {
            filesystem.release()
        }
    }
}
