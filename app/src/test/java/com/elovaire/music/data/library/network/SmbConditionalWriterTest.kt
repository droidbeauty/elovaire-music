package elovaire.music.droidbeauty.app.data.library.network

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SmbConditionalWriterTest {
    @get:Rule val files = TemporaryFolder()

    @Test
    fun replacesOnlyTheVerifiedOriginalAndRemovesTheBackup() {
        val store = Store("original")
        val (original, replacement) = files()
        val events = mutableListOf<String>()
        SmbConditionalWriter(store).replace("music/song.mp3", original, replacement, "id-1",
            { events += "record:$it" }, { events += "resolved" }, { events += "commit" })
        assertEquals(mapOf("music/song.mp3" to "edited"), store.contents())
        assertEquals(listOf("record:music/.elovaire-id-1.tmp", "commit", "resolved"), events)
    }

    @Test
    fun interruptedRenameRestoresTheOriginalDuringRecovery() {
        val store = Store("original").apply { failStagedRename = true }
        val (original, replacement) = files()
        val writer = SmbConditionalWriter(store)
        assertThrows(IOException::class.java) {
            writer.replace("music/song.mp3", original, replacement, "id-2", {}, {}, {})
        }
        assertEquals(null, store.contents()["music/song.mp3"])
        writer.cleanup("music/song.mp3", "music/.elovaire-id-2.tmp", "id-2",
            listOf(original, replacement), {})
        assertEquals(mapOf("music/song.mp3" to "original"), store.contents())
    }

    @Test
    fun recoveryNeverOverwritesAChangedTarget() {
        val store = Store("original").apply { failStagedRename = true }
        val (original, replacement) = files()
        val writer = SmbConditionalWriter(store)
        assertThrows(IOException::class.java) {
            writer.replace("music/song.mp3", original, replacement, "id-3", {}, {}, {})
        }
        store.put("music/song.mp3", "newer external edit")
        assertThrows(IOException::class.java) {
            writer.cleanup("music/song.mp3", "music/.elovaire-id-3.tmp", "id-3",
                listOf(original, replacement), {})
        }
        assertEquals("newer external edit", store.contents()["music/song.mp3"])
        assertEquals("original", store.contents()["music/.elovaire-id-3.bak"])
    }

    @Test
    fun recoveryHandlesAReplacementWhoseRenameResponseWasLost() {
        val store = Store("original").apply { loseStagedRenameResponse = true }
        val (original, replacement) = files()
        val writer = SmbConditionalWriter(store)
        assertThrows(IOException::class.java) {
            writer.replace("music/song.mp3", original, replacement, "id-5", {}, {}, {})
        }
        writer.cleanup("music/song.mp3", "music/.elovaire-id-5.tmp", "id-5",
            listOf(original, replacement), {})
        assertEquals(mapOf("music/song.mp3" to "edited"), store.contents())
    }

    @Test
    fun anExistingBackupNameIsNeverDeletedWhenTheOriginalWasNotMoved() {
        val store = Store("original").apply { put("music/.elovaire-id-6.bak", "original") }
        val (original, replacement) = files()
        val writer = SmbConditionalWriter(store)
        assertThrows(IOException::class.java) {
            writer.replace("music/song.mp3", original, replacement, "id-6", {}, {}, {})
        }
        assertThrows(IOException::class.java) {
            writer.cleanup("music/song.mp3", "music/.elovaire-id-6.tmp", "id-6",
                listOf(original, replacement), {})
        }
        assertEquals("original", store.contents()["music/.elovaire-id-6.bak"])
        assertEquals("original", store.contents()["music/song.mp3"])
    }

    @Test
    fun collisionDoesNotDeleteAnotherFile() {
        val store = Store("original").apply { put("music/.elovaire-id-4.tmp", "other") }
        val (original, replacement) = files()
        var resolved = false
        assertThrows(IOException::class.java) {
            SmbConditionalWriter(store).replace("music/song.mp3", original, replacement, "id-4",
                {}, { resolved = true }, {})
        }
        assertEquals(true, resolved)
        assertEquals("other", store.contents()["music/.elovaire-id-4.tmp"])
        assertEquals("original", store.contents()["music/song.mp3"])
    }

    @Test
    fun replacementStopsWhenTheOriginalChangedAfterItWasCopied() {
        val store = Store("newer")
        val (original, replacement) = files()
        val writer = SmbConditionalWriter(store)
        assertThrows(IOException::class.java) {
            writer.replace("music/song.mp3", original, replacement, "id-7", {}, {}, {})
        }
        assertEquals("newer", store.contents()["music/song.mp3"])
        writer.cleanup("music/song.mp3", "music/.elovaire-id-7.tmp", "id-7",
            listOf(original, replacement), {})
        assertEquals(mapOf("music/song.mp3" to "newer"), store.contents())
    }

    @Test
    fun recoveryPreservesAnUnverifiedTemporaryFile() {
        val store = Store("original").apply { put("music/.elovaire-id-8.tmp", "partial") }
        val (original, replacement) = files()
        assertThrows(IOException::class.java) {
            SmbConditionalWriter(store).cleanup("music/song.mp3", "music/.elovaire-id-8.tmp", "id-8",
                listOf(original, replacement), {})
        }
        assertEquals("partial", store.contents()["music/.elovaire-id-8.tmp"])
        assertEquals("original", store.contents()["music/song.mp3"])
    }

    private fun files(): Pair<File, File> =
        files.newFile("original").apply { writeText("original") } to
            files.newFile("replacement").apply { writeText("edited") }

    private class Store(original: String) : SmbEditStore {
        private class Node(var bytes: ByteArray)
        private val entries = mutableMapOf("music/song.mp3" to Node(original.toByteArray()))
        var failStagedRename = false
        var loseStagedRenameResponse = false
        fun put(path: String, value: String) { entries[path] = Node(value.toByteArray()) }
        fun contents(): Map<String, String> = entries.mapValues { String(it.value.bytes) }

        override fun create(path: String): SmbEditHandle? {
            if (path in entries) return null
            return handle(path, Node(byteArrayOf()).also { entries[path] = it })
        }

        override fun open(path: String): SmbEditHandle? = entries[path]?.let { handle(path, it) }

        private fun handle(start: String, node: Node): SmbEditHandle = object : SmbEditHandle {
            var path = start
            var delete = false
            override fun input() = ByteArrayInputStream(node.bytes)
            override fun output() = object : ByteArrayOutputStream() {
                override fun close() { node.bytes = toByteArray() }
            }
            override fun flush() = Unit
            override fun rename(path: String) {
                if (failStagedRename && this.path.endsWith(".tmp")) {
                    failStagedRename = false
                    throw IOException("Lost staged rename")
                }
                if (path in entries) throw IOException("Destination exists")
                entries.remove(this.path)
                entries[path] = node
                val lostResponse = loseStagedRenameResponse && this.path.endsWith(".tmp")
                this.path = path
                if (lostResponse) {
                    loseStagedRenameResponse = false
                    throw IOException("Lost rename response")
                }
            }
            override fun deleteOnClose() { delete = true }
            override fun close() { if (delete && entries[path] === node) entries.remove(path) }
        }
    }
}
