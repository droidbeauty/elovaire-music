package elovaire.music.droidbeauty.app.data.artwork

import android.graphics.Bitmap
import android.net.TestUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtworkLoaderTest {
    @Test
    fun artworkRequestKey_normalizesSizeAndSeparatesPurpose() {
        val uri = TestUri("content://artwork/1")
        val uiKey = artworkRequestKey(uri, 240, ArtworkPurpose.UiLarge)
        val notificationKey = artworkRequestKey(uri, 240, ArtworkPurpose.Notification)

        requireNotNull(uiKey)
        requireNotNull(notificationKey)
        assertEquals(256, uiKey.targetPx)
        assertNotEquals(uiKey.cacheKey, notificationKey.cacheKey)
    }

    @Test
    fun normalizeArtworkRequestSize_usesBoundedBuckets() {
        assertEquals(96, normalizeArtworkRequestSize(1))
        assertEquals(256, normalizeArtworkRequestSize(240))
        assertEquals(1024, normalizeArtworkRequestSize(2048))
    }

    @Test
    fun audioSources_skipImageStreamProbe_thatCouldReadTheWholeTrack() {
        assertTrue(isLikelyAudioMediaUri(TestUri("content://media/external/audio/media/42")))
        assertTrue(isLikelyAudioMediaUri(TestUri("content://documents/song.m4b")))
        assertTrue(isLikelyAudioMediaUri(TestUri("content://documents/song.mp3?raw=1")))
        assertFalse(isLikelyAudioMediaUri(TestUri("content://images/cover.jpg")))
        assertFalse(shouldUseContentResolverThumbnail(TestUri("content://documents/song.m4b"), ArtworkPurpose.UiLarge))
        assertTrue(shouldUseContentResolverThumbnail(TestUri("content://images/cover.jpg"), ArtworkPurpose.UiLarge))
        assertFalse(shouldUseContentResolverThumbnail(TestUri("content://images/cover.jpg"), ArtworkPurpose.Notification))
    }

    @Test
    fun bitmapConfig_preservesQualityForLargeAndEditorArtwork() {
        assertEquals(Bitmap.Config.RGB_565, bitmapConfigForPurpose(ArtworkPurpose.UiGrid))
        assertEquals(Bitmap.Config.ARGB_8888, bitmapConfigForPurpose(ArtworkPurpose.UiLarge))
        assertEquals(Bitmap.Config.ARGB_8888, bitmapConfigForPurpose(ArtworkPurpose.Notification))
        assertEquals(Bitmap.Config.ARGB_8888, bitmapConfigForPurpose(ArtworkPurpose.TagEditorPreview))
    }

    @Test
    fun artworkBounds_rejectInvalidAndDecompressionBombDimensions() {
        assertTrue(isArtworkBoundsSafe(4_000, 4_000))
        assertFalse(isArtworkBoundsSafe(0, 512))
        assertFalse(isArtworkBoundsSafe(8_193, 1))
        assertFalse(isArtworkBoundsSafe(8_192, 8_192))
    }
}
