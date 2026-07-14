package elovaire.music.droidbeauty.app.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaWriteTargetClassifierTest {
    @Test
    fun classifyParts_recognizesMediaStoreItemUri() {
        assertEquals(
            MediaWriteTargetKind.MediaStoreItem,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "content",
                authority = "media",
                pathSegments = listOf("external", "audio", "media", "42"),
            ),
        )
    }

    @Test
    fun classifyParts_rejectsMediaStoreCollectionUri() {
        assertEquals(
            MediaWriteTargetKind.Unsupported,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "content",
                authority = "media",
                pathSegments = listOf("external", "audio", "media"),
            ),
        )
    }

    @Test
    fun classifyParts_rejectsNonAudioMediaStoreItemUri() {
        assertEquals(
            MediaWriteTargetKind.Unsupported,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "content",
                authority = "media",
                pathSegments = listOf("external", "images", "media", "42"),
            ),
        )
    }

    @Test
    fun classifyParts_safDocumentIsNotMediaStoreItem() {
        assertEquals(
            MediaWriteTargetKind.SafDocument,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "content",
                authority = "com.android.externalstorage.documents",
                pathSegments = listOf("tree", "primary:Music", "document", "primary:Music/song.mp3"),
            ),
        )
    }

    @Test
    fun classifyParts_recognizesFileUri() {
        assertEquals(
            MediaWriteTargetKind.FileUri,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "file",
                authority = null,
                pathSegments = listOf("storage", "emulated", "0", "Music", "song.mp3"),
            ),
        )
    }

    @Test
    fun classifyParts_rejectsUnsupportedContentProvider() {
        assertEquals(
            MediaWriteTargetKind.Unsupported,
            MediaWriteTargetClassifier.classifyParts(
                scheme = "content",
                authority = "example.provider",
                pathSegments = listOf("song", "42"),
            ),
        )
    }
}
