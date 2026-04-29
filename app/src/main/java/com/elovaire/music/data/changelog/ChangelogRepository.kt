package elovaire.music.app.data.changelog

import android.content.Context
import elovaire.music.app.R
import org.xmlpull.v1.XmlPullParser

data class ChangelogRelease(
    val version: String,
    val changes: List<String>,
)

class ChangelogRepository(
    private val context: Context,
) {
    fun loadReleases(): List<ChangelogRelease> {
        val parser = context.resources.getXml(R.xml.changelog)
        val releases = mutableListOf<ChangelogRelease>()
        var currentVersion = ""
        val currentChanges = mutableListOf<String>()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "release" -> {
                            currentVersion = parser.getAttributeValue(null, "version").orEmpty()
                            currentChanges.clear()
                        }

                        "item", "change" -> {
                            currentChanges += parser.nextText().trim()
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "release") {
                        releases += ChangelogRelease(
                            version = currentVersion,
                            changes = currentChanges.toList(),
                        )
                        currentVersion = ""
                        currentChanges.clear()
                    }
                }
            }
            parser.next()
        }

        parser.close()
        return releases
    }
}
