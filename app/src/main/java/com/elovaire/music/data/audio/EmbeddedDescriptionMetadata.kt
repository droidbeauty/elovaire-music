package elovaire.music.droidbeauty.app.data.audio

import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.mp4.Mp4FieldKey
import org.jaudiotagger.tag.mp4.Mp4Tag

/** Keeps description field selection consistent for reading, mutation, and verification. */
internal object EmbeddedDescriptionMetadata {
    const val MAX_CHARACTERS = 20_000
    private const val VORBIS_DESCRIPTION_FIELD = "DESCRIPTION"

    fun read(tag: Tag): String? {
        return when (tag) {
            is Mp4Tag -> if (runCatching { tag.hasField(Mp4FieldKey.DESCRIPTION) }.getOrDefault(false)) {
                runCatching { tag.getFirst(Mp4FieldKey.DESCRIPTION) }.getOrNull()
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            } else {
                legacyComment(tag)
            }
            is FlacTag -> if (runCatching { tag.hasField(VORBIS_DESCRIPTION_FIELD) }.getOrDefault(false)) {
                runCatching { tag.getFirst(VORBIS_DESCRIPTION_FIELD) }.getOrNull()
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            } else {
                legacyComment(tag)
            }
            else -> legacyComment(tag)
        }
    }

    /**
     * Writes only the container's canonical description field. [legacyValue] allows a
     * description that was stored in the old COMMENT field to be cleared without deleting
     * an unrelated comment that happens to be present.
     */
    fun write(tag: Tag, description: String?, legacyValue: String? = null) {
        val normalized = description?.trim().orEmpty()
        require(normalized.length <= MAX_CHARACTERS) { "Description is too long to save safely." }
        when (tag) {
            is Mp4Tag -> {
                if (normalized.isNotBlank()) {
                    runCatching { tag.deleteField(Mp4FieldKey.DESCRIPTION) }
                    tag.setField(Mp4FieldKey.DESCRIPTION, normalized)
                } else {
                    deleteLegacyCommentIfMatching(tag, legacyValue)
                    tag.setField(Mp4FieldKey.DESCRIPTION, "")
                }
            }
            is FlacTag -> {
                if (normalized.isNotBlank()) {
                    runCatching { tag.deleteField(VORBIS_DESCRIPTION_FIELD) }
                    tag.setField(VORBIS_DESCRIPTION_FIELD, normalized)
                } else {
                    deleteLegacyCommentIfMatching(tag, legacyValue)
                    tag.setField(VORBIS_DESCRIPTION_FIELD, "")
                }
            }
            else -> {
                runCatching { tag.deleteField(FieldKey.COMMENT) }
                if (normalized.isNotBlank()) {
                    tag.setField(tag.createField(FieldKey.COMMENT, normalized))
                }
            }
        }
    }

    fun matches(tag: Tag, expected: String?): Boolean =
        normalize(read(tag)) == normalize(expected)

    private fun deleteLegacyCommentIfMatching(tag: Tag, legacyValue: String?) {
        if (legacyValue != null && normalize(runCatching { tag.getFirst(FieldKey.COMMENT) }.getOrNull()) == normalize(legacyValue)) {
            runCatching { tag.deleteField(FieldKey.COMMENT) }
        }
    }

    private fun legacyComment(tag: Tag): String? = runCatching { tag.getFirst(FieldKey.COMMENT) }
        .getOrNull()
        ?.trim()
        ?.takeIf(String::isNotBlank)

    private fun normalize(value: String?): String = value.orEmpty().trim()
}
