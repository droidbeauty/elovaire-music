package elovaire.music.droidbeauty.app.data.audio

import java.io.File
import java.util.Locale
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp4.Mp4TagReader
import org.jaudiotagger.audio.mp4.Mp4TagWriter
import org.jaudiotagger.tag.Tag

internal object AudioTagIO {
    fun isMp4(file: File): Boolean = file.extension.lowercase(Locale.ROOT) in MP4_EXTENSIONS

    // MP4 metadata is independent of the codec. The full reader cannot parse some surround codecs.
    fun read(file: File): Tag? = if (isMp4(file)) {
        Mp4TagReader().read(file.toPath())
    } else {
        AudioFileIO.read(file).tag
    }

    fun edit(file: File, update: (Tag) -> Unit) {
        if (isMp4(file)) {
            val tag = Mp4TagReader().read(file.toPath())
            update(tag)
            Mp4TagWriter(file.path).write(tag, file.toPath())
        } else {
            val audioFile = AudioFileIO.read(file)
            update(audioFile.tagOrCreateAndSetDefault)
            audioFile.commit()
        }
    }

    private val MP4_EXTENSIONS = setOf("m4a", "m4b", "mp4")
}
