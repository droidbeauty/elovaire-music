import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardrailPathPolicyTest {
    @Test
    fun allowlistMatchesBothPathSeparatorStyles() {
        val suffix = "/src/main/java/com/example/AndroidApiCompat.kt"

        assertTrue(isGuardrailPathAllowed("C:\\repo\\src\\main\\java\\com\\example\\AndroidApiCompat.kt", listOf(suffix)))
        assertTrue(isGuardrailPathAllowed("/repo/src/main/java/com/example/AndroidApiCompat.kt", listOf(suffix)))
        assertFalse(isGuardrailPathAllowed("/repo/src/main/java/com/example/Other.kt", listOf(suffix)))
    }

    @Test
    fun coreUiImportsAreRejectedWithoutFlaggingOtherLayers() {
        val uiImport = "import elovaire.music.droidbeauty.app.ui.components.ArtworkImage"

        assertTrue(coreImportsUi("/repo/src/main/java/com/example/core/AppContainer.kt", uiImport))
        assertFalse(coreImportsUi("/repo/src/main/java/com/example/data/ArtworkLoader.kt", uiImport))
        assertFalse(coreImportsUi("/repo/src/main/java/com/example/core/AppContainer.kt", "import com.example.data.Artwork"))
    }
}
