import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardrailSourceTextTest {
    @Test
    fun commentsAreRemovedWithoutChangingLinePositionsOrLiterals() {
        val source = """
            // GlobalScope in a comment
            val url = "https://example.test/path"
            /* nested /* comment */ still ignored */
            val value = 42
        """.trimIndent()

        val sanitized = stripCommentsPreservingLiterals(source)

        assertEquals(source.lines().size, sanitized.lines().size)
        assertTrue(sanitized.contains("https://example.test/path"))
        assertTrue("GlobalScope" !in sanitized)
        assertTrue("nested" !in sanitized)
        assertTrue(sanitized.contains("val value = 42"))
    }

    @Test
    fun codeScanIgnoresStringsAndComments() {
        val source = """
            val marker = "AppContainer("
            // MediaStore.createWriteRequest
            AppContainer.create()
        """.trimIndent()

        val sanitized = stripCommentsAndStringLiterals(source)

        assertTrue("AppContainer.create()" in sanitized)
        assertTrue("MediaStore.createWriteRequest" !in sanitized)
        assertTrue("AppContainer(" !in sanitized)
    }
}
