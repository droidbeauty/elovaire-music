package elovaire.music.droidbeauty.app.data.audio

import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.mp4.Mp4FieldKey
import org.jaudiotagger.tag.mp4.Mp4Tag
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmbeddedDescriptionMetadataTest {
    @Test
    fun mp3UsesCommentFieldAndRoundTripsClearing() {
        val tag = ID3v24Tag()

        EmbeddedDescriptionMetadata.write(tag, "A short synopsis")

        assertEquals("A short synopsis", EmbeddedDescriptionMetadata.read(tag))
        assertEquals("A short synopsis", tag.getFirst(FieldKey.COMMENT))
        EmbeddedDescriptionMetadata.write(tag, null, legacyValue = "A short synopsis")
        assertNull(EmbeddedDescriptionMetadata.read(tag))
    }

    @Test
    fun flacUsesDedicatedDescriptionWithoutReplacingComment() {
        val tag = FlacTag()
        tag.setField(FieldKey.COMMENT, "Unrelated comment")

        EmbeddedDescriptionMetadata.write(tag, "Book synopsis")

        assertEquals("Book synopsis", EmbeddedDescriptionMetadata.read(tag))
        assertEquals("Unrelated comment", tag.getFirst(FieldKey.COMMENT))
        assertEquals("Book synopsis", tag.getFirst("DESCRIPTION"))

        EmbeddedDescriptionMetadata.write(tag, null, legacyValue = "Book synopsis")

        assertNull(EmbeddedDescriptionMetadata.read(tag))
        assertEquals("Unrelated comment", tag.getFirst(FieldKey.COMMENT))
    }

    @Test
    fun mp4UsesNativeDescriptionAndPreservesComment() {
        val tag = Mp4Tag()
        tag.setField(Mp4FieldKey.COMMENT, "Unrelated comment")

        EmbeddedDescriptionMetadata.write(tag, "Book synopsis")

        assertEquals("Book synopsis", EmbeddedDescriptionMetadata.read(tag))
        assertEquals("Book synopsis", tag.getFirst(Mp4FieldKey.DESCRIPTION))
        assertEquals("Unrelated comment", tag.getFirst(Mp4FieldKey.COMMENT))

        EmbeddedDescriptionMetadata.write(tag, null, legacyValue = "Book synopsis")

        assertNull(EmbeddedDescriptionMetadata.read(tag))
        assertEquals("Unrelated comment", tag.getFirst(Mp4FieldKey.COMMENT))
    }

    @Test
    fun supportedFixturesRoundTripDescriptionThroughTheContainer() {
        listOf("lyrics-id3v24.mp3", "lyrics.flac", "lyrics.m4a").forEach { fixture ->
            val file = copyFixture(fixture)
            val audioFile = AudioFileIO.read(file)
            EmbeddedDescriptionMetadata.write(audioFile.tag, "Persisted $fixture synopsis")
            audioFile.commit()

            assertEquals(
                "Persisted $fixture synopsis",
                EmbeddedDescriptionMetadata.read(AudioFileIO.read(file).tag),
            )
        }
    }

    private fun copyFixture(name: String): File {
        val output = kotlin.io.path.createTempFile(suffix = ".${name.substringAfterLast('.')}").toFile()
        javaClass.getResourceAsStream("/media/$name")!!.use { input ->
            output.outputStream().use(input::copyTo)
        }
        output.deleteOnExit()
        return output
    }
}
