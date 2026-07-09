package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.security.MessageDigest
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class SafTreeLibraryScanner(
    private val context: Context,
) {
    private val audioFormatDetector = AudioFormatDetector(context)

    suspend fun scan(selections: List<LibraryFolderSelection>): List<Song> {
        if (selections.isEmpty()) return emptyList()
        val songs = mutableListOf<Song>()
        selections.forEach { selection ->
            currentCoroutineContext().ensureActive()
            val treeUri = selection.uri ?: return@forEach
            if (!selection.hasPersistedReadPermission(context)) return@forEach
            songs += scanTree(selection, treeUri)
        }
        return songs
    }

    private suspend fun scanTree(
        selection: LibraryFolderSelection,
        treeUri: Uri,
    ): List<Song> {
        val rootDocumentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return emptyList()
        val rootKey = LibraryFolderSelectionResolver.safSyntheticRoot(treeUri)
        val libraryRootPaths = buildSet {
            add(rootKey)
            LibrarySongDuplicateResolver.normalizedRealPath(selection.path)?.let(::add)
        }
        val audioFileFilter = LibraryAudioFileFilter(
            selectedRelativeRoots = emptySet(),
            libraryRootPaths = libraryRootPaths,
            explicitCustomRootPaths = libraryRootPaths,
            explicitCustomRelativeRoots = emptySet(),
        )
        val pending = ArrayDeque<SafDirectory>()
        pending += SafDirectory(documentId = rootDocumentId, relativePath = "")
        val songs = mutableListOf<Song>()
        var visitedDocuments = 0
        while (pending.isNotEmpty() && visitedDocuments < MAX_DOCUMENTS) {
            currentCoroutineContext().ensureActive()
            val directory = pending.removeFirst()
            queryChildren(treeUri, directory.documentId).forEach { child ->
                currentCoroutineContext().ensureActive()
                visitedDocuments += 1
                if (visitedDocuments > MAX_DOCUMENTS) return@forEach
                if (child.name.startsWith('.')) return@forEach
                val childRelativePath = listOf(directory.relativePath, child.name)
                    .filter(String::isNotBlank)
                    .joinToString("/")
                if (child.isDirectory) {
                    if (childRelativePath.count { it == '/' } < MAX_DEPTH) {
                        pending += SafDirectory(
                            documentId = child.documentId,
                            relativePath = childRelativePath,
                        )
                    }
                    return@forEach
                }
                val extension = child.name.substringAfterLast('.', "").lowercase(Locale.ROOT)
                if (extension !in AudioFormatPolicy.scannerExtensions) return@forEach
                val detectedFormat = audioFormatDetector.detect(child.uri, child.name, child.mimeType)
                val metadata = readMetadata(child.uri)
                val durationMs = detectedFormat.durationMs ?: metadata.durationMs ?: return@forEach
                val libraryPath = resolvedLibraryPath(selection, rootKey, childRelativePath)
                val candidate = AudioScanCandidate(
                    id = stableNegativeId("saf-song:${child.uri}"),
                    uri = child.uri,
                    displayName = child.name,
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    durationMs = durationMs,
                    mimeType = child.mimeType,
                    relativePath = childRelativePath.substringBeforeLast('/', ""),
                    absolutePath = libraryPath,
                    extension = extension,
                    isMusic = true,
                    detectedFormat = detectedFormat,
                )
                if (audioFileFilter.evaluate(candidate) !is AudioFileFilterDecision.Include) return@forEach
                val title = metadata.title ?: child.name.substringBeforeLast('.').ifBlank { child.name }
                val artist = metadata.artist ?: "Unknown Artist"
                val album = metadata.album ?: selection.displayName.ifBlank { "Unknown Album" }
                val albumArtist = metadata.albumArtist ?: artist
                songs += Song(
                    id = candidate.id,
                    title = title,
                    isExplicit = false,
                    artist = artist,
                    album = album,
                    releaseYear = metadata.year,
                    genre = metadata.genre ?: "Unknown Genre",
                    audioFormat = detectedFormat.displayName,
                    audioQuality = null,
                    fileName = child.name,
                    albumId = stableNegativeId("saf-album:$albumArtist::$album"),
                    durationMs = durationMs,
                    trackNumber = metadata.trackNumber ?: 0,
                    discNumber = metadata.discNumber ?: 1,
                    dateAddedSeconds = 0L,
                    dateModifiedSeconds = child.lastModifiedMs?.div(1000L),
                    libraryPath = libraryPath,
                    uri = child.uri,
                    artUri = null,
                    metadataResolved = true,
                    albumArtist = albumArtist,
                    volumeNormalization = null,
                )
            }
        }
        return songs
    }

    private fun resolvedLibraryPath(
        selection: LibraryFolderSelection,
        rootKey: String,
        childRelativePath: String,
    ): String {
        val selectionPath = LibrarySongDuplicateResolver.normalizedRealPath(selection.path)
            ?: return "$rootKey/$childRelativePath"
        val root = File(selectionPath)
        val candidate = File(root, childRelativePath)
        return runCatching {
            val canonicalRoot = root.canonicalFile
            val canonicalCandidate = candidate.canonicalFile
            if (
                canonicalCandidate.path.startsWith("${canonicalRoot.path}${File.separator}") &&
                canonicalCandidate.isFile
            ) {
                canonicalCandidate.absolutePath
            } else {
                "$rootKey/$childRelativePath"
            }
        }.getOrDefault("$rootKey/$childRelativePath")
    }

    private fun queryChildren(
        treeUri: Uri,
        documentId: String,
    ): List<SafDocument> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        return runCatching {
            context.contentResolver.query(
                childrenUri,
                DOCUMENT_PROJECTION,
                null,
                null,
                null,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                buildList {
                    while (cursor.moveToNext()) {
                        val childId = cursor.getString(idIndex) ?: continue
                        val name = cursor.getString(nameIndex)?.trim().orEmpty()
                        if (name.isBlank()) continue
                        val mimeType = cursor.getString(mimeIndex)?.trim()?.ifBlank { null }
                        add(
                            SafDocument(
                                uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId),
                                documentId = childId,
                                name = name,
                                mimeType = mimeType,
                                lastModifiedMs = modifiedIndex
                                    .takeIf { it >= 0 && !cursor.isNull(it) }
                                    ?.let(cursor::getLong)
                                    ?.takeIf { it > 0L },
                            ),
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun readMetadata(uri: Uri): SafMetadata {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                SafMetadata(
                    durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                        ?.takeIf { it > 0L },
                    title = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    albumArtist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                    album = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                        ?.take(4)
                        ?.toIntOrNull(),
                    genre = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                    trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.substringBefore('/')
                        ?.trim()
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 },
                    discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                        ?.substringBefore('/')
                        ?.trim()
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 },
                )
            } finally {
                runCatching { retriever.release() }
            }
        }.getOrDefault(SafMetadata())
    }

    private fun MediaMetadataRetriever.metadata(keyCode: Int): String? {
        return extractMetadata(keyCode)?.trim()?.takeIf(String::isNotBlank)
    }

    private data class SafDirectory(
        val documentId: String,
        val relativePath: String,
    )

    private data class SafDocument(
        val uri: Uri,
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val lastModifiedMs: Long?,
    ) {
        val isDirectory: Boolean = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
    }

    private data class SafMetadata(
        val durationMs: Long? = null,
        val title: String? = null,
        val artist: String? = null,
        val albumArtist: String? = null,
        val album: String? = null,
        val year: Int? = null,
        val genre: String? = null,
        val trackNumber: Int? = null,
        val discNumber: Int? = null,
    )

    private companion object {
        const val MAX_DOCUMENTS = 5_000
        const val MAX_DEPTH = 16
        val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}

private fun stableNegativeId(input: String): Long {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    val positive = digest.take(8).fold(0L) { acc, byte ->
        (acc shl 8) or (byte.toLong() and 0xffL)
    } and Long.MAX_VALUE
    return -positive.coerceAtLeast(1L)
}
