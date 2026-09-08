package elovaire.music.droidbeauty.app.data.tags

internal class AudiobookTagMutationCoordinator(
    private val editor: AudiobookTagEditor,
    private val artworkInvalidator: AlbumTagArtworkInvalidator,
) : AudiobookTagEditor {
    override suspend fun applyEdits(
        request: AudiobookTagEditRequest,
        writeConsentGranted: Boolean,
    ): TagEditApplyResult {
        val result = editor.applyEdits(request, writeConsentGranted)
        if (result.artworkChanged) {
            artworkInvalidator.invalidate(request.songs.map { it.artUri })
        }
        return result
    }
}
