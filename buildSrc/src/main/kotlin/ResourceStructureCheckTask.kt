import java.io.File
import java.net.URI
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ResourceStructureCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val assetFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val profileFiles: ConfigurableFileCollection

    @TaskAction
    fun checkResources() {
        val violations = mutableListOf<String>()
        val packagedFiles = (resourceFiles.files + assetFiles.files).filter(File::isFile)
        packagedFiles.filter { it.name.startsWith(".") }.forEach { file ->
            violations += "Packaged resources must not contain hidden metadata files: ${file.invariantSeparatorsPath}"
        }
        resourceFiles.files.filter(File::isFile).forEach { file ->
            if (!RESOURCE_NAME.matches(file.nameWithoutExtension)) {
                violations += "Invalid Android resource name: ${file.invariantSeparatorsPath}"
            }
            if (file.extension == "xml") validateXml(file, violations)
        }
        validateDuplicateResources(packagedFiles, violations)
        val inspectedFiles = packagedFiles + sourceFiles.files.filter { it.isFile }
        inspectedFiles.forEach { file ->
            if (file.extension in TEXT_EXTENSIONS) {
                val text = file.readText()
                if (file.extension in SOURCE_EXTENSIONS && "getIdentifier(" in text) {
                    violations += "Resource lookup must use typed references instead of getIdentifier(): ${file.invariantSeparatorsPath}"
                }
                FORBIDDEN_RELEASE_CONTENT.firstOrNull(text::contains)
                    ?.takeUnless { marker -> file.name == "LrclibClient.kt" && marker == "lrclib.net" }
                    ?.let { marker ->
                    violations += "${file.invariantSeparatorsPath} contains removed remote-content integration: $marker"
                }
            }
        }
        val assetNames = assetFiles.files.filter(File::isFile).mapTo(mutableSetOf(), File::getName)
        REQUIRED_LICENSE_FILES.filterNot(assetNames::contains).forEach { missing ->
            violations += "Packaged third-party license is missing: $missing"
        }
        validateProfiles(violations)
        if (violations.isNotEmpty()) throw GradleException(violations.joinToString(separator = "\n"))
    }

    private fun validateDuplicateResources(files: Collection<File>, violations: MutableList<String>) {
        files.asSequence()
            .filter { it.isFile && it.extension in STATIC_RESOURCE_EXTENSIONS }
            .mapNotNull { file -> normalizedResourceSignature(file)?.let { signature -> signature to file } }
            .groupBy({ it.first }, { it.second })
            .values
            .filter { it.size > 1 }
            .filterNot { files -> files.mapTo(mutableSetOf(), File::getName) in ALLOWED_DUPLICATE_RESOURCE_GROUPS }
            .forEach { files ->
                violations += "Duplicate packaged resource content: ${files.joinToString { it.invariantSeparatorsPath }}"
            }
    }

    private fun validateXml(file: File, violations: MutableList<String>) {
        try {
            val document = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }.newDocumentBuilder().parse(file)
            if (file.name == "info_screen.xml") validateAboutLinks(document.documentElement, file, violations)
        } catch (failure: Exception) {
            violations += "Invalid XML resource ${file.invariantSeparatorsPath}: ${failure.message}"
        }
    }

    private fun validateAboutLinks(
        root: org.w3c.dom.Element,
        file: File,
        violations: MutableList<String>,
    ) {
        val links = root.getElementsByTagName("link")
        for (index in 0 until links.length) {
            val url = (links.item(index) as? org.w3c.dom.Element)?.getAttribute("url").orEmpty()
            if (!url.isPublicHttpsUrl()) {
                violations += "About link must use a public HTTPS URL: ${file.invariantSeparatorsPath}"
            }
        }
        val entries = root.getElementsByTagName("entry")
        for (index in 0 until entries.length) {
            val entry = entries.item(index) as? org.w3c.dom.Element ?: continue
            val logo = entry.getAttribute("logoUrl").ifBlank { entry.getAttribute("logoUri") }
            if (logo.isNotBlank() && !logo.startsWith("@drawable/") && !logo.isPublicHttpsUrl()) {
                violations += "About logo must use a bundled drawable or public HTTPS URL: ${file.invariantSeparatorsPath}"
            }
        }
    }

    private fun validateProfiles(violations: MutableList<String>) {
        val sourceTypes = buildSet {
            sourceFiles.files.filter { it.isFile && it.extension == "kt" }.forEach { file ->
                val text = file.readText()
                val packageName = PACKAGE.find(text)?.groupValues?.get(1).orEmpty()
                if (packageName.isBlank()) return@forEach
                add("$packageName.${file.nameWithoutExtension}Kt")
                TYPE.findAll(text).forEach { match ->
                    add("$packageName.${match.groupValues[1]}")
                }
            }
        }
        profileFiles.files.filter(File::isFile).forEach { profile ->
            profile.readLines().forEachIndexed { index, line ->
                val className = PROFILE_CLASS.find(line)?.groupValues?.get(1)?.replace('/', '.') ?: return@forEachIndexed
                if (className !in sourceTypes) {
                    violations += "${profile.invariantSeparatorsPath}:${index + 1} references missing source type $className"
                }
            }
        }
    }

    private companion object {
        val RESOURCE_NAME = Regex("""[a-z][a-z0-9_]*""")
        val PACKAGE = Regex("""(?m)^package\s+([\w.]+)""")
        val TYPE = Regex(
            """(?m)^(?:internal\s+|public\s+|private\s+|protected\s+)?""" +
                """(?:data\s+|sealed\s+|value\s+|enum\s+|annotation\s+|fun\s+)?""" +
                """(?:class|interface|object)\s+(\w+)""",
        )
        val PROFILE_CLASS = Regex("""^L([^;]+);""")
        val TEXT_EXTENSIONS = setOf("kt", "kts", "xml", "txt", "json", "properties")
        val SOURCE_EXTENSIONS = setOf("kt", "kts", "java")
        val STATIC_RESOURCE_EXTENSIONS = setOf("xml", "png", "webp", "jpg", "jpeg", "gif", "ttf", "otf")
        val ALLOWED_DUPLICATE_RESOURCE_GROUPS = setOf(
            setOf("ic_launcher.xml", "ic_launcher_round.xml"),
        )
        val REQUIRED_LICENSE_FILES = setOf(
            "APACHE-2.0.txt",
            "GEIST_LICENSE.txt",
            "LGPL-2.1.txt",
            "LUCIDE_LICENSE.txt",
            "THIRD_PARTY_NOTICES.txt",
        )
        val FORBIDDEN_RELEASE_CONTENT = setOf(
            "lrclib.net",
            "lyrics.ovh",
            "coverartarchive.org",
            "api.acoustid.org",
            "musicbrainz.org/ws",
            "fanart.tv",
            "theaudiodb.com/api",
            "youtube.googleapis.com",
            "api.spotify.com",
            "api.tidal.com",
            "listen.tidal.com",
            "resources.tidal.com",
            "api.deezer.com",
            "ws.audioscrobbler.com",
            "api.genius.com",
            "api.musixmatch.com",
            "api.discogs.com",
            "api.audd.io",
            "ACOUSTID_API_KEY",
            "THEAUDIODB_API_KEY",
            "FANART_API_KEY",
            "FANART_TV_API_KEY",
            "YOUTUBE_API_KEY",
            "YOUTUBE_DATA_API_KEY",
            "SPOTIFY_CLIENT_ID",
            "HttpAcoustIdClient",
            "HttpMusicBrainzClient",
            "LrcLibLyricsProvider",
            "LyricsOvhProvider",
            "CoverArtArchiveProvider",
            "FingerprintAlbumTagMatcher",
        )
    }
}

internal fun normalizedResourceSignature(file: File): String? {
    val canonicalBytes = if (file.extension == "xml") {
        runCatching {
            val document = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }.newDocumentBuilder().parse(file)
            buildString { appendCanonicalXml(document.documentElement, this) }
        }.getOrNull()?.toByteArray(Charsets.UTF_8) ?: return null
    } else {
        file.readBytes()
    }
    return MessageDigest.getInstance("SHA-256")
        .digest(canonicalBytes)
        .joinToString("") { byte -> "%02x".format(byte) }
}

private fun appendCanonicalXml(node: org.w3c.dom.Node, output: StringBuilder) {
    when (node.nodeType) {
        org.w3c.dom.Node.ELEMENT_NODE -> {
            output.append('<').append(node.nodeName)
            val attributes = node.attributes
            (0 until attributes.length)
                .map { attributes.item(it) }
                .sortedBy { it.nodeName }
                .forEach { attribute ->
                    output.append(' ').append(attribute.nodeName).append('=').append(attribute.nodeValue)
                }
            output.append('>')
            val children = node.childNodes
            for (index in 0 until children.length) {
                appendCanonicalXml(children.item(index), output)
            }
            output.append("</").append(node.nodeName).append('>')
        }

        org.w3c.dom.Node.TEXT_NODE,
        org.w3c.dom.Node.CDATA_SECTION_NODE,
        -> if (!node.nodeValue.isNullOrBlank()) output.append(node.nodeValue)
    }
}

private fun String.isPublicHttpsUrl(): Boolean {
    if (any(Char::isWhitespace)) return false
    val uri = runCatching { URI(this) }.getOrNull() ?: return false
    return uri.scheme == "https" && uri.host?.contains('.') == true && uri.userInfo == null
}
