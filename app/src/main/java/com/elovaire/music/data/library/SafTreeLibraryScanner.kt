package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.RemoteException
import android.provider.DocumentsContract
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.data.audio.AudioFormatDetector
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.DetectedAudioFormat
import elovaire.music.droidbeauty.app.domain.model.Song
import java.io.File
import java.security.MessageDigest
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal sealed interface SafTreeScanResult {
    val selection: LibraryFolderSelection

    data class Complete(
        override val selection: LibraryFolderSelection,
        val songs: List<Song>,
    ) : SafTreeScanResult

    data class Incomplete(
        override val selection: LibraryFolderSelection,
        val songs: List<Song>,
        val failure: SafScanIncompleteException,
    ) : SafTreeScanResult

    data class Unavailable(
        override val selection: LibraryFolderSelection,
        val failure: SafProviderUnavailableException,
    ) : SafTreeScanResult
}

@Suppress("TooGenericExceptionCaught")
internal class SafTreeLibraryScanner(
    private val context: Context,
) {
    private val audioFormatDetector = AudioFormatDetector(context)
    private val localMetadataReader = LocalAudioMetadataReader(context)
    private var fileMetadataCache = emptyMap<SafDocumentKey, CachedSafFile>()

    suspend fun scan(selections: List<LibraryFolderSelection>): List<Song> {
        return scanByTree(selections).flatMap { result ->
            when (result) {
                is SafTreeScanResult.Complete -> result.songs
                is SafTreeScanResult.Incomplete -> throw result.failure
                is SafTreeScanResult.Unavailable -> throw result.failure
            }
        }
    }

    suspend fun scanByTree(selections: List<LibraryFolderSelection>): List<SafTreeScanResult> {
        if (selections.isEmpty()) return emptyList()
        val refreshedCache = HashMap<SafDocumentKey, CachedSafFile>(fileMetadataCache.size)
        val visitedDirectories = hashSetOf<SafDocumentKey>()
        val albumIds = hashMapOf<String, Long>()
        val results = selections.mapNotNull { selection ->
            currentCoroutineContext().ensureActive()
            val treeUri = selection.uri ?: return@mapNotNull null
            try {
                if (!selection.hasPersistedReadPermission(context)) {
                    throw SafProviderUnavailableException(
                        authority = treeUri.authority,
                        operation = "validate-persisted-permission",
                        cause = SecurityException("Persisted SAF read permission is unavailable."),
                    )
                }
                val outcome = scanTree(selection, treeUri, refreshedCache, visitedDirectories, albumIds)
                outcome.incompleteReason?.let { reason ->
                    SafTreeScanResult.Incomplete(
                        selection = selection,
                        songs = outcome.songs,
                        failure = SafScanIncompleteException(treeUri.authority, reason),
                    )
                } ?: SafTreeScanResult.Complete(selection = selection, songs = outcome.songs)
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: SafScanIncompleteException) {
                SafTreeScanResult.Incomplete(selection, emptyList(), failure)
            } catch (failure: SafProviderUnavailableException) {
                SafTreeScanResult.Unavailable(selection, failure)
            } catch (failure: RuntimeException) {
                SafTreeScanResult.Unavailable(
                    selection,
                    SafProviderUnavailableException(
                        authority = treeUri.authority,
                        operation = "scan-tree",
                        cause = failure,
                    ),
                )
            }
        }
        fileMetadataCache = refreshedCache
        return results
    }

    @Suppress("LongMethod")
    private suspend fun scanTree(
        selection: LibraryFolderSelection,
        treeUri: Uri,
        refreshedCache: MutableMap<SafDocumentKey, CachedSafFile>,
        visitedDirectories: MutableSet<SafDocumentKey>,
        albumIds: MutableMap<String, Long>,
    ): SafTreeScanOutcome {
        val rootDocumentId = treeDocumentId(treeUri)
        val rootKey = LibraryFolderSelectionResolver.safSyntheticRoot(treeUri)
        val providerKey = treeUri.authority?.lowercase(Locale.ROOT).orEmpty()
        val canonicalRoot = LibrarySongDuplicateResolver.normalizedRealPath(selection.path)
            ?.let(::File)
            ?.let { runCatching { it.canonicalFile }.getOrNull() }
            ?.takeIf(File::isDirectory)
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
        var incompleteReason: String? = null
        while (pending.isNotEmpty() && incompleteReason == null) {
            currentCoroutineContext().ensureActive()
            val directory = pending.removeFirst()
            if (!visitedDirectories.add(SafDocumentKey(providerKey, directory.documentId))) continue
            ElovaireTrace.section("library_saf_child_query") {
                queryChildren(treeUri, directory.documentId)
            }.forEach { child ->
                currentCoroutineContext().ensureActive()
                if (visitedDocuments >= MAX_DOCUMENTS) {
                    incompleteReason = "document budget exceeded"
                    return@forEach
                }
                visitedDocuments += 1
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
                    } else {
                        incompleteReason = "directory depth budget exceeded"
                    }
                    return@forEach
                }
                val extension = child.name.substringAfterLast('.', "").lowercase(Locale.ROOT)
                if (extension !in AudioFormatPolicy.scannerExtensions) return@forEach
                val documentKey = SafDocumentKey(providerKey, child.documentId)
                val cachedFile = (refreshedCache[documentKey] ?: fileMetadataCache[documentKey])
                    ?.takeIf { it.matches(child) }
                val detectedFormat = cachedFile?.detectedFormat
                    ?: audioFormatDetector.detect(
                        uri = child.uri,
                        fileName = child.name,
                        mediaStoreMimeType = child.mimeType,
                        revisionKey = if (child.lastModifiedMs != null || child.sizeBytes != null) {
                            MediaIdentityResolver.sourceRevisionKey(
                                modifiedAtMs = child.lastModifiedMs,
                                sizeBytes = child.sizeBytes,
                            )
                        } else null,
                        identityKey = MediaIdentityResolver.safDocument(
                            providerKey,
                            child.documentId,
                        )?.stableKey,
                    )
                val metadata = cachedFile?.metadata ?: readMetadata(
                    uri = child.uri,
                    fileName = child.name,
                    identityKey = MediaIdentityResolver.safDocument(providerKey, child.documentId)?.stableKey,
                    revisionKey = if (child.lastModifiedMs != null || child.sizeBytes != null) {
                        MediaIdentityResolver.sourceRevisionKey(
                            modifiedAtMs = child.lastModifiedMs,
                            sizeBytes = child.sizeBytes,
                        )
                    } else null,
                )
                if (child.hasStableChangeSignal) {
                    refreshedCache[documentKey] = CachedSafFile.from(child, detectedFormat, metadata)
                }
                val durationMs = detectedFormat.durationMs ?: metadata.durationMs ?: 0L
                val libraryPath = resolveSafLibraryPath(canonicalRoot, rootKey, childRelativePath)
                val stableSongId = stableNegativeId(
                    MediaIdentityResolver.safDocument(providerKey, child.documentId)?.stableKey
                        ?: "saf-uri:${child.uri}",
                )
                val candidate = AudioScanCandidate(
                    id = stableSongId,
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
                val albumIdentity = "$providerKey|$rootKey|$albumArtist::$album"
                songs += Song(
                    id = stableSongId,
                    title = title,
                    isExplicit = false,
                    artist = artist,
                    album = album,
                    releaseYear = metadata.releaseYear,
                    genre = metadata.genre ?: "Unknown Genre",
                    audioFormat = detectedFormat.displayName,
                    audioQuality = null,
                    fileName = child.name,
                    albumId = albumIds.getOrPut(albumIdentity) {
                        stableNegativeId("saf-album:$albumIdentity")
                    },
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
                    volumeNormalization = metadata.volumeNormalization,
                )
            }
        }
        if (incompleteReason == null && pending.isNotEmpty()) {
            incompleteReason = "directory traversal budget exceeded"
        }
        return SafTreeScanOutcome(songs = songs, incompleteReason = incompleteReason)
    }

    private fun queryChildren(
        treeUri: Uri,
        documentId: String,
    ): List<SafDocument> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        return try {
            val cursor = context.contentResolver.query(
                childrenUri,
                DOCUMENT_PROJECTION,
                null,
                null,
                null,
            ) ?: throw SafProviderUnavailableException(
                authority = treeUri.authority,
                operation = "query children",
                cause = IllegalStateException("The document provider returned no cursor."),
            )
            cursor.use {
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
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
                                sizeBytes = sizeIndex
                                    .takeIf { it >= 0 && !cursor.isNull(it) }
                                    ?.let(cursor::getLong)
                                    ?.takeIf { it >= 0L },
                            ),
                        )
                    }
                }
            }
        } catch (throwable: CancellationException) {
            throw throwable
        } catch (failure: SecurityException) {
            throw treeUri.providerFailure(failure)
        } catch (failure: RemoteException) {
            throw treeUri.providerFailure(failure)
        } catch (failure: IllegalArgumentException) {
            throw treeUri.providerFailure(failure)
        } catch (failure: SQLiteException) {
            throw treeUri.providerFailure(failure)
        } catch (failure: IllegalStateException) {
            throw treeUri.providerFailure(failure)
        }
    }

    private fun treeDocumentId(treeUri: Uri): String {
        return try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (failure: IllegalArgumentException) {
            throw treeUri.providerFailure(failure, "resolve tree document")
        }
    }

    private fun Uri.providerFailure(
        failure: Throwable,
        operation: String = "query children",
    ): SafProviderUnavailableException {
        return SafProviderUnavailableException(
            authority = authority,
            operation = operation,
            cause = failure,
        )
    }

    private fun readMetadata(
        uri: Uri,
        fileName: String,
        identityKey: String?,
        revisionKey: String?,
    ): LocalAudioMetadata {
        return localMetadataReader.read(
            uri = uri,
            filePath = null,
            fileName = fileName,
            identityKey = identityKey,
            revisionKey = revisionKey,
        )
    }

    private data class SafDirectory(
        val documentId: String,
        val relativePath: String,
    )

    private data class SafTreeScanOutcome(
        val songs: List<Song>,
        val incompleteReason: String?,
    )

    private data class SafDocument(
        val uri: Uri,
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val lastModifiedMs: Long?,
        val sizeBytes: Long?,
    ) {
        val isDirectory: Boolean = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
        val hasStableChangeSignal: Boolean = lastModifiedMs != null || sizeBytes != null
    }

    private data class SafDocumentKey(
        val provider: String,
        val documentId: String,
    )

    private data class CachedSafFile(
        val name: String,
        val mimeType: String?,
        val lastModifiedMs: Long?,
        val sizeBytes: Long?,
        val detectedFormat: DetectedAudioFormat,
        val metadata: LocalAudioMetadata,
    ) {
        fun matches(document: SafDocument): Boolean {
            return document.hasStableChangeSignal &&
                name == document.name &&
                mimeType == document.mimeType &&
                lastModifiedMs == document.lastModifiedMs &&
                sizeBytes == document.sizeBytes
        }

        companion object {
            fun from(
                document: SafDocument,
                detectedFormat: DetectedAudioFormat,
                metadata: LocalAudioMetadata,
            ): CachedSafFile {
                return CachedSafFile(
                    name = document.name,
                    mimeType = document.mimeType,
                    lastModifiedMs = document.lastModifiedMs,
                    sizeBytes = document.sizeBytes,
                    detectedFormat = detectedFormat,
                    metadata = metadata,
                )
            }
        }
    }

    private companion object {
        const val MAX_DOCUMENTS = 5_000
        const val MAX_DEPTH = 16
        val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
        )
    }
}

internal fun resolveSafLibraryPath(
    canonicalRoot: File?,
    rootKey: String,
    childRelativePath: String,
): String {
    val syntheticPath = "$rootKey/$childRelativePath"
    val root = canonicalRoot ?: return syntheticPath
    return runCatching {
        val candidate = File(root, childRelativePath).canonicalFile
        if (
            candidate.path.startsWith("${root.path}${File.separator}") &&
            candidate.isFile
        ) {
            candidate.absolutePath
        } else {
            syntheticPath
        }
    }.getOrDefault(syntheticPath)
}

private fun stableNegativeId(input: String): Long {
    val digest = checkNotNull(STABLE_ID_DIGEST.get()).digest(input.toByteArray())
    val positive = digest.take(8).fold(0L) { acc, byte ->
        (acc shl 8) or (byte.toLong() and 0xffL)
    } and Long.MAX_VALUE
    return -positive.coerceAtLeast(1L)
}

private val STABLE_ID_DIGEST = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }
