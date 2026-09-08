import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InstrumentationTestSelectionTest {
    @Test
    fun blankSelectionDoesNotNarrowTheDefaultSuite() {
        assertNull(normalizedInstrumentationTestClass("  "))
        assertNull(normalizedInstrumentationTestClass(null))
    }

    @Test
    fun focusedSelectionIsTrimmedAndPreserved() {
        assertEquals(
            ROOM_QUERY_PLAN_TEST_CLASS,
            normalizedInstrumentationTestClass(" $ROOM_QUERY_PLAN_TEST_CLASS "),
        )
    }
}
