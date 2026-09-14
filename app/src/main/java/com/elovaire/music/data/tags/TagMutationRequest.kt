package elovaire.music.droidbeauty.app.data.tags

import android.net.Uri
import elovaire.music.droidbeauty.app.domain.model.Song

internal interface TagMutationRequest {
    val songs: List<Song>
    val collectionTitle: TagFieldEdit<String>
    val collectionArtist: TagFieldEdit<String>
    val collectionDescription: TagFieldEdit<String>
    val releaseYear: TagFieldEdit<Int>
    val genre: TagFieldEdit<String>
    val coverArtUri: Uri?
    val coverArtBytes: ByteArray?
    val tracks: List<EditableAlbumTrack>
}

internal interface TagMutationEditor {
    suspend fun applyEdits(
        request: TagMutationRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult
}
