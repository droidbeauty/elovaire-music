package elovaire.music.droidbeauty.app.data.tags

import android.net.Uri

internal fun interface AlbumTagArtworkInvalidator {
    fun invalidate(uris: Collection<Uri?>)
}

internal class AlbumTagMutationCoordinator(
    private val editor: AlbumTagEditor,
    private val artworkInvalidator: AlbumTagArtworkInvalidator,
) : AlbumTagEditor {
    override suspend fun applyEdits(
        request: AlbumTagEditRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult {
        val result = editor.applyEdits(
            request = request,
            writeConsentGranted = writeConsentGranted,
        )
        if (result.artworkChanged) {
            artworkInvalidator.invalidate(
                buildList {
                    add(request.album.artUri)
                    addAll(request.album.songs.map { it.artUri })
                },
            )
        }
        return result
    }
}
