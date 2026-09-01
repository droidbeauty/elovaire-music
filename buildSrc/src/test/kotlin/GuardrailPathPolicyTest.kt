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
}
