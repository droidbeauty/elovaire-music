package elovaire.music.droidbeauty.app.data.tags

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song

internal data class AudiobookTagEditRequest(
    val bookKey: String,
    override val songs: List<Song>,
    val bookTitle: TagFieldEdit<String>,
    val author: TagFieldEdit<String>,
    val description: TagFieldEdit<String> = TagFieldEdit.Unchanged,
    override val releaseYear: TagFieldEdit<Int>,
    override val genre: TagFieldEdit<String>,
    override val coverArtUri: Uri?,
    override val coverArtBytes: ByteArray? = null,
    override val tracks: List<EditableAlbumTrack>,
) : TagMutationRequest {
    override val collectionTitle: TagFieldEdit<String> get() = bookTitle
    override val collectionArtist: TagFieldEdit<String> get() = author
    override val collectionDescription: TagFieldEdit<String> get() = description
}

internal interface AudiobookTagEditor {
    suspend fun applyEdits(
        request: AudiobookTagEditRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult
}

internal class AudiobookTagEditorService(
    private val tagMutationEditor: TagMutationEditor,
) : AudiobookTagEditor {
    override suspend fun applyEdits(
        request: AudiobookTagEditRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult = tagMutationEditor.applyEdits(request, writeConsentGranted)
}

internal fun AudiobookTagEditRequest.mutatedUris(): List<Uri> =
    TagEditPlanner.plansFor(this).map { it.song.uri }

internal fun AudiobookTagEditRequest.retryForFailures(
    failedSongIds: Set<Long>,
): AudiobookTagEditRequest = copy(
    songs = songs.filter { it.id in failedSongIds },
    tracks = tracks.filter { it.songId in failedSongIds },
)
