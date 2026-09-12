import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ResourceStructureCheckTaskTest {
    @Test
    fun normalizedXmlSignatureIgnoresFormattingAndAttributeOrder() {
        val first = Files.createTempFile("resource-first", ".xml").toFile()
        val second = Files.createTempFile("resource-second", ".xml").toFile()
        try {
            first.writeText("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\" android:width=\"24dp\" android:height=\"24dp\"/>\n")
            second.writeText("<vector android:height=\"24dp\" android:width=\"24dp\" xmlns:android=\"http://schemas.android.com/apk/res/android\" />")

            assertEquals(normalizedResourceSignature(first), normalizedResourceSignature(second))
        } finally {
            first.delete()
            second.delete()
        }
    }

    @Test
    fun normalizedXmlSignatureKeepsResourceValuesDistinct() {
        val first = Files.createTempFile("resource-first", ".xml").toFile()
        val second = Files.createTempFile("resource-second", ".xml").toFile()
        try {
            first.writeText("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\" android:width=\"24dp\" />")
            second.writeText("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\" android:width=\"48dp\" />")

            assertNotEquals(normalizedResourceSignature(first), normalizedResourceSignature(second))
        } finally {
            first.delete()
            second.delete()
        }
    }
}
