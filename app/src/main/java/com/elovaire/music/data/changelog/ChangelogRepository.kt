package elovaire.music.app.data.changelog

import android.content.Context
import elovaire.music.app.R
import org.xmlpull.v1.XmlPullParser

data class ChangelogRelease(
    val version: String,
    val date: String,
    val changes: List<String>,
)

class ChangelogRepository(
    private val context: Context,
) {
    fun loadReleases(): List<ChangelogRelease> {
        val parser = context.resources.getXml(R.xml.changelog)
        val releases = mutableListOf<ChangelogRelease>()
        var currentVersion = ""
        var currentDate = ""
        val currentChanges = mutableListOf<String>()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "release" -> {
                            currentVersion = parser.getAttributeValue(null, "version").orEmpty()
                            currentDate = parser.getAttributeValue(null, "date").orEmpty()
                            currentChanges.clear()
                        }

                        "change" -> {
                            currentChanges += parser.nextText().trim()
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "release") {
                        releases += ChangelogRelease(
                            version = currentVersion,
                            date = currentDate,
                            changes = currentChanges.toList(),
                        )
                        currentVersion = ""
                        currentDate = ""
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
