package elovaire.music.droidbeauty.app.data.artist

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elovaire.music.droidbeauty.app.data.network.BoundedHttpTransport
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistInformationInstrumentedTest {
    @Test
    fun liveArtistLookupReturnsBiography() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveArtistLookup") == "true")
        val result = MusicBrainzArtistDiscographyClient(BoundedHttpTransport()) { delay(1_100) }.lookup("Coldplay")
        assertTrue("Expected a live artist biography, got $result", result is ArtistDiscographyResult.Found && !result.description.isNullOrBlank())
    }

    @Test
    fun descriptionGenresAndAlbumsAreReadFromTheirSources() = runBlocking {
        val artistSearchAttempts = AtomicInteger()
        val transport = BoundedHttpTransport(openConnection = { url ->
            val busy = url.path == "/ws/2/artist/" && artistSearchAttempts.getAndIncrement() == 0
            val body = when {
                url.host == "en.wikipedia.org" -> """{"extract":"An artist biography","content_urls":{"desktop":{"page":"https://en.wikipedia.org/wiki/Example"}}}"""
                url.host == "www.wikidata.org" -> """{"entities":{"Q123":{"sitelinks":{"enwiki":{"title":"Example"}}}}}"""
                url.path.contains("release-group") -> """{"release-groups":[{"title":"First album","primary-type":"Album","first-release-date":"2020","artist-credit":[{"name":"Example"}]}]}"""
                url.path.contains("12345678-1234-1234-1234-123456789abc") -> """{"genres":[{"name":"rock"}],"relations":[{"type":"wikipedia","url":{"resource":"https://en.wikipedia.org/wiki/Missing"}},{"type":"wikidata","url":{"resource":"https://www.wikidata.org/wiki/Q123"}}]}"""
                else -> """{"artists":[{"name":"Example","id":"12345678-1234-1234-1234-123456789abc"}]}"""
            }
            object : HttpURLConnection(url) {
                override fun connect() = Unit
                override fun usingProxy() = false
                override fun disconnect() = Unit
                override fun getResponseCode() = when {
                    busy -> 503
                    url.path.endsWith("/Missing") -> 404
                    else -> 200
                }
                override fun getInputStream() = ByteArrayInputStream(body.toByteArray())
                override fun getHeaderField(name: String?): String? = null
            }
        })
        val result = MusicBrainzArtistDiscographyClient(transport) {}.lookup("Example")
        assertTrue(result is ArtistDiscographyResult.Found)
        result as ArtistDiscographyResult.Found
        assertEquals("An artist biography", result.description)
        assertEquals(listOf("rock"), result.genres)
        assertEquals(listOf(ArtistDiscographyAlbum("First album", "2020")), result.albums)
        assertEquals("https://en.wikipedia.org/wiki/Example", result.descriptionSourceUrl)
        assertEquals(2, artistSearchAttempts.get())
    }
}
