package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import elovaire.music.droidbeauty.app.R
import org.xmlpull.v1.XmlPullParser

internal fun Context.loadAboutScreenModel(): AboutScreenModel {
    val parser = resources.getXml(R.xml.info_screen)
    val sections = mutableListOf<AboutSection>()
    var currentSectionTitle = ""
    var currentSectionDescription: String? = null
    var sectionOpen = false
    var currentEntries = mutableListOf<AboutEntry>()
    var currentEntryTitle: String? = null
    var currentEntryDescription: String? = null
    var currentEntryLogoUri: String? = null
    var currentLinks = mutableListOf<AboutLink>()

    fun closeEntry() {
        val entryTitle = currentEntryTitle
        if (!entryTitle.isNullOrBlank()) {
            val entry = AboutEntry(
                title = entryTitle,
                description = currentEntryDescription?.takeIf { it.isNotBlank() },
                logoUri = currentEntryLogoUri?.takeIf { it.isNotBlank() },
                links = currentLinks.toList(),
            )
            if (sectionOpen) {
                currentEntries += entry
            } else {
                sections += AboutSection(
                    title = entry.title,
                    description = null,
                    entries = listOf(entry),
                )
            }
        }
        currentEntryTitle = null
        currentEntryDescription = null
        currentEntryLogoUri = null
        currentLinks = mutableListOf()
    }

    fun closeSection() {
        closeEntry()
        if (sectionOpen && (currentSectionTitle.isNotBlank() || currentEntries.isNotEmpty())) {
            sections += AboutSection(
                title = currentSectionTitle,
                description = currentSectionDescription?.takeIf { it.isNotBlank() },
                entries = currentEntries.toList(),
            )
        }
        sectionOpen = false
        currentSectionTitle = ""
        currentSectionDescription = null
        currentEntries = mutableListOf()
    }

    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
            XmlPullParser.START_TAG -> when (parser.name) {
                "section" -> {
                    closeSection()
                    sectionOpen = true
                    currentSectionTitle = parser.getAttributeValue(null, "title").orEmpty()
                    currentSectionDescription = parser.getAttributeValue(null, "description")
                }

                "entry" -> {
                    closeEntry()
                    currentEntryTitle = parser.getAttributeValue(null, "title")
                    currentEntryDescription = parser.getAttributeValue(null, "description")
                    currentEntryLogoUri = parser.getAttributeValue(null, "logoUrl")
                        ?: parser.getAttributeValue(null, "logoUri")
                }

                "link" -> {
                    val label = parser.getAttributeValue(null, "label").orEmpty()
                    val url = parser.getAttributeValue(null, "url").orEmpty()
                    if (label.isNotBlank() && url.isNotBlank()) {
                        currentLinks += AboutLink(label = label, url = url)
                    }
                }
            }

            XmlPullParser.END_TAG -> when (parser.name) {
                "entry" -> closeEntry()
                "section" -> closeSection()
            }
        }
        parser.next()
    }
    closeSection()
    return AboutScreenModel(sections = sections)
}

internal data class AboutScreenModel(
    val sections: List<AboutSection>,
)

internal data class AboutSection(
    val title: String,
    val description: String?,
    val entries: List<AboutEntry>,
)

internal data class AboutEntry(
    val title: String,
    val description: String?,
    val logoUri: String?,
    val links: List<AboutLink>,
)

internal data class AboutLink(
    val label: String,
    val url: String,
)
