package elovaire.music.droidbeauty.app.data.library.network

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebDavConditionalWriterTest {
    @get:Rule val files = TemporaryFolder()
    private val source = NetworkLibrarySource("source", "NAS", NetworkLibraryProtocol.WebDav,
        "https://nas.example", "Music", "user", "key")
    private val credentials = NetworkCredentials("user", "pass")

    @Test
    fun locksComparesStagesAndMovesBeforeClearingTheJournal() {
        val events = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            val response = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
            when (request.method) {
                "LOCK" -> response.header("Lock-Token", "<opaquelocktoken:123>").body("".toResponseBody())
                "GET" -> response.body((if (request.url.encodedPath.endsWith(".tmp")) "replacement" else "original").toResponseBody())
                "PUT" -> {
                    assertEquals("*", request.header("If-None-Match"))
                    assertEquals("replacement", Buffer().also { request.body!!.writeTo(it) }.readUtf8())
                    response.body("".toResponseBody())
                }
                "MOVE" -> {
                    assertEquals("https://nas.example/Music/song.mp3", request.header("Destination"))
                    assertEquals("<https://nas.example/Music/song.mp3> (<opaquelocktoken:123>)", request.header("If"))
                    assertEquals("T", request.header("Overwrite"))
                    response.code(204).body("".toResponseBody())
                }
                "UNLOCK" -> response.code(204).body("".toResponseBody())
                else -> error("Unexpected request ${request.method}")
            }.build()
        }.build()
        val expected = files.newFile("original.mp3").apply { writeText("original") }
        val replacement = files.newFile("replacement.mp3").apply { writeText("replacement") }
        WebDavConditionalWriter(client).use { writer ->
            writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-1",
                recordTemporary = { events += "record:$it" },
                resolveTemporary = { events += "resolved" },
                onCommitStarting = { events += "commit" },
            )
        }
        assertEquals(listOf("LOCK", "GET", "record:Music/.elovaire-edit-1.tmp", "PUT", "GET", "commit", "MOVE", "resolved", "UNLOCK"), events)
    }

    @Test
    fun changedOriginalIsNeverReplaced() {
        val methods = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            methods += request.method
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .apply { if (request.method == "LOCK") header("Lock-Token", "<opaquelocktoken:123>") }
                .body((if (request.method == "GET") "changed" else "").toResponseBody()).build()
        }.build()
        val expected = files.newFile("expected").apply { writeText("original") }
        val replacement = files.newFile("replacement").apply { writeText("edited") }
        WebDavConditionalWriter(client).use { writer ->
            assertThrows(IOException::class.java) {
                writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-2",
                    recordTemporary = { error("Must not stage a conflicting edit") },
                    resolveTemporary = { error("No staged file exists") },
                    onCommitStarting = { error("Must not overwrite a conflicting edit") },
                )
            }
        }
        assertEquals(listOf("LOCK", "GET", "UNLOCK"), methods)
        assertFalse("PUT" in methods)
        assertTrue("UNLOCK" in methods)
    }

    @Test
    fun uncertainUploadRetainsItsJournalEntryWithoutMovingTheOriginal() {
        val events = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            if (request.method == "PUT") throw IOException("Connection lost during upload")
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .apply { if (request.method == "LOCK") header("Lock-Token", "<opaquelocktoken:123>") }
                .body((if (request.method == "GET") "original" else "").toResponseBody()).build()
        }.build()
        val expected = files.newFile("expected").apply { writeText("original") }
        val replacement = files.newFile("replacement").apply { writeText("edited") }
        WebDavConditionalWriter(client).use { writer ->
            assertThrows(IOException::class.java) {
                writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-3",
                    recordTemporary = { events += "record" },
                    resolveTemporary = { events += "resolved" },
                    onCommitStarting = { events += "commit" },
                )
            }
        }
        assertEquals(listOf("LOCK", "GET", "record", "PUT", "UNLOCK"), events)
    }

    @Test
    fun temporaryNameCollisionIsNotDeleted() {
        val events = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(if (request.method == "PUT") 412 else 200).message("Response")
                .apply { if (request.method == "LOCK") header("Lock-Token", "<opaquelocktoken:123>") }
                .body((if (request.method == "GET") "original" else "").toResponseBody()).build()
        }.build()
        val expected = files.newFile("expected").apply { writeText("original") }
        val replacement = files.newFile("replacement").apply { writeText("edited") }
        WebDavConditionalWriter(client).use { writer ->
            assertThrows(IOException::class.java) {
                writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-4",
                    recordTemporary = { events += "record" },
                    resolveTemporary = { events += "resolved" },
                    onCommitStarting = { events += "commit" },
                )
            }
        }
        assertEquals(listOf("LOCK", "GET", "record", "PUT", "resolved", "UNLOCK"), events)
    }

    @Test
    fun failedCommitCheckRetainsTemporaryAndUnlocksWithoutMoving() {
        val events = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .apply {
                    if (request.method == "LOCK") header("Lock-Token", "<opaquelocktoken:123>")
                    if (request.method == "GET") header("ETag", "\"revision-1\"")
                }
                .body((if (request.method == "GET") {
                    if (request.url.encodedPath.endsWith(".tmp")) "edited" else "original"
                } else "").toResponseBody()).build()
        }.build()
        val expected = files.newFile("expected").apply { writeText("original") }
        val replacement = files.newFile("replacement").apply { writeText("edited") }
        WebDavConditionalWriter(client).use { writer ->
            assertThrows(IllegalStateException::class.java) {
                writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-5",
                    recordTemporary = { events += "record" },
                    resolveTemporary = { events += "resolved" },
                    onCommitStarting = { throw IllegalStateException("Source was changed") },
                )
            }
        }
        assertEquals(listOf("LOCK", "GET", "record", "PUT", "GET", "GET", "UNLOCK"), events)
    }

    @Test
    fun corruptedStagingFileIsNeverMovedOverOriginal() {
        val events = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            val body = when {
                request.method != "GET" -> ""
                request.url.encodedPath.endsWith(".tmp") -> "wrong!"
                else -> "original"
            }
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .apply {
                    if (request.method == "LOCK") header("Lock-Token", "<opaquelocktoken:123>")
                    if (request.method == "GET") header("ETag", "\"revision-1\"")
                }
                .body(body.toResponseBody()).build()
        }.build()
        val expected = files.newFile("expected").apply { writeText("original") }
        val replacement = files.newFile("replacement").apply { writeText("edited") }
        WebDavConditionalWriter(client).use { writer ->
            assertThrows(IOException::class.java) {
                writer.replace(source, credentials, "Music/song.mp3", expected, replacement, "edit-6",
                    recordTemporary = { events += "record" },
                    resolveTemporary = { events += "resolved" },
                    onCommitStarting = { events += "commit" },
                )
            }
        }
        assertEquals(listOf("LOCK", "GET", "record", "PUT", "GET", "GET", "UNLOCK"), events)
    }

    @Test
    fun recoveryDeletesOnlyVerifiedTemporaryWithMatchingStrongEtag() {
        val events = mutableListOf<String>()
        var body = "replacement"
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            events += request.method
            if (request.method == "DELETE") assertEquals("\"revision-1\"", request.header("If-Match"))
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("ETag", "\"revision-1\"").body(body.toResponseBody()).build()
        }.build()
        val replacement = files.newFile("replacement").apply { writeText("replacement") }
        WebDavConditionalWriter(client).use { writer ->
            writer.cleanupTemporary(source, credentials, "Music/song.mp3", "Music/.elovaire-edit-1.tmp",
                "edit-1", listOf(replacement)) { events += "checked" }
            assertEquals(listOf("GET", "checked", "DELETE"), events)
            events.clear()
            body = "other data!"
            assertThrows(IOException::class.java) {
                writer.cleanupTemporary(source, credentials, "Music/song.mp3", "Music/.elovaire-edit-1.tmp",
                    "edit-1", listOf(replacement)) { events += "checked" }
            }
            assertEquals(listOf("GET"), events)
        }
    }
}
