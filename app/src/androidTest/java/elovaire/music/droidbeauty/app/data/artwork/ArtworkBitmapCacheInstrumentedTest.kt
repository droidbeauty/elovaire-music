package elovaire.music.droidbeauty.app.data.artwork

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkBitmapCacheInstrumentedTest {
    @Test
    fun sameSizeEquivalentPurpose_reusesArgbBitmapOnly() {
        val uri = Uri.parse("content://artwork/instrumented-equivalent-purpose")
        val key = requireNotNull(artworkRequestKey(uri, 512, ArtworkPurpose.UiLarge))
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            ArtworkBitmapCache.put(key.cacheKey, bitmap)

            assertSame(
                bitmap,
                ArtworkBitmapCache.sameSizeForEquivalentPurpose(
                    uri,
                    key.targetPx,
                    ArtworkPurpose.Notification,
                ),
            )
            assertNull(
                ArtworkBitmapCache.sameSizeForEquivalentPurpose(
                    uri,
                    key.targetPx,
                    ArtworkPurpose.UiGrid,
                ),
            )
        } finally {
            ArtworkBitmapCache.removeAllMatchingUris(listOf(uri.toString()))
            bitmap.recycle()
        }
    }
}
