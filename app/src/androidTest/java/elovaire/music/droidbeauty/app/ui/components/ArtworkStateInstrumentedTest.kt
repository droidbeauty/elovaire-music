package elovaire.music.droidbeauty.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elovaire.music.droidbeauty.app.data.artwork.ArtworkBitmapCache
import elovaire.music.droidbeauty.app.data.artwork.ArtworkPurpose
import elovaire.music.droidbeauty.app.data.artwork.artworkRequestKey
import elovaire.music.droidbeauty.app.data.artwork.invalidateArtworkCaches
import elovaire.music.droidbeauty.app.ui.screens.playlists.PlaylistTestActivity
import java.util.concurrent.atomic.AtomicReference
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkStateInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<PlaylistTestActivity>()

    @Test
    fun invalidatingTheSameUriRefreshesVisibleArtworkAndColors() {
        val file = File.createTempFile("artwork-state-", ".png", composeRule.activity.cacheDir)
        val uri = Uri.fromFile(file)
        val image = AtomicReference<ImageBitmap?>()
        val gradient = AtomicReference<List<Color>>()
        val accent = AtomicReference<Color?>()
        val source = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        fun writeCover(color: Int) {
            source.eraseColor(color)
            file.outputStream().use { check(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        }
        try {
            writeCover(android.graphics.Color.BLUE)
            composeRule.setContent {
                MaterialTheme {
                    val currentImage = rememberArtworkBitmap(uri, 512).value
                    val currentGradient = rememberArtworkGradient(uri).value
                    val currentAccent = rememberArtworkPaletteAccent(uri).value
                    SideEffect {
                        image.set(currentImage)
                        gradient.set(currentGradient)
                        accent.set(currentAccent)
                    }
                }
            }
            composeRule.waitUntil(5_000L) {
                val colors = gradient.get()
                image.get() != null && accent.get() != null && colors != null &&
                    colors.first().blue > colors.first().red
            }
            val originalImage = image.get()
            val originalGradient = gradient.get()
            val originalAccent = accent.get()

            writeCover(android.graphics.Color.RED)
            invalidateArtworkCaches(listOf(uri))

            composeRule.waitUntil(5_000L) {
                image.get() != null && image.get() !== originalImage &&
                    gradient.get() != originalGradient && accent.get() != originalAccent
            }
            assertEquals(Color.Red, image.get()!!.toPixelMap()[0, 0])
        } finally {
            invalidateArtworkCaches(listOf(uri))
            source.recycle()
            check(file.delete())
        }
    }

    @Test
    fun changingToASongWithoutArtworkClearsThePreviousCover() {
        val uri = Uri.parse("content://artwork/state-${System.nanoTime()}")
        val selectedUri = mutableStateOf<Uri?>(uri)
        val observed = AtomicReference<ImageBitmap?>()
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val key = requireNotNull(artworkRequestKey(uri, 96, ArtworkPurpose.UiGrid))
        try {
            ArtworkBitmapCache.put(key.cacheKey, bitmap)
            composeRule.setContent {
                val image = rememberArtworkBitmap(selectedUri.value, 96).value
                SideEffect { observed.set(image) }
            }
            composeRule.waitUntil(5_000L) { observed.get() != null }

            composeRule.runOnIdle { selectedUri.value = null }
            composeRule.waitForIdle()

            assertNull("A song without artwork must not show the preceding song's cover", observed.get())
        } finally {
            invalidateArtworkCaches(listOf(uri))
        }
    }

    @Test
    fun artworkGradientTracksThemeChangesForTheSameCover() {
        val uri = Uri.parse("content://artwork/theme-${System.nanoTime()}")
        val light = lightColorScheme()
        val dark = darkColorScheme()
        val scheme = mutableStateOf(dark)
        val observed = AtomicReference<List<Color>>()
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.BLUE) }
        val key = requireNotNull(artworkRequestKey(uri, 512, ArtworkPurpose.UiLarge))
        try {
            ArtworkBitmapCache.put(key.cacheKey, bitmap)
            composeRule.setContent {
                MaterialTheme(colorScheme = scheme.value) {
                    val gradient = rememberArtworkGradient(uri).value
                    SideEffect { observed.set(gradient) }
                }
            }
            composeRule.waitUntil(5_000L) { observed.get()?.get(1) == dark.background }

            composeRule.runOnIdle { scheme.value = light }
            composeRule.waitUntil(5_000L) { observed.get()?.get(1) == light.background }

            // Returning to the previous theme must also reuse only its matching colors.
            composeRule.runOnIdle { scheme.value = dark }
            composeRule.waitUntil(5_000L) { observed.get()?.get(1) == dark.background }
        } finally {
            invalidateArtworkCaches(listOf(uri))
        }
    }
}
