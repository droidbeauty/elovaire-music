package elovaire.music.droidbeauty.app.ui.screens.tags

import elovaire.music.droidbeauty.app.core.OperationIdGenerator
import elovaire.music.droidbeauty.app.data.tags.AlbumTagEditRequest

internal data class PendingTagWrite(
    val operationId: String,
    val request: AlbumTagEditRequest,
)

internal class AlbumTagWritePermissionState(
    private val operationIdGenerator: OperationIdGenerator,
) {
    private var pending: PendingTagWrite? = null

    fun begin(request: AlbumTagEditRequest): PendingTagWrite? {
        if (pending != null) return null
        return PendingTagWrite(operationIdGenerator.nextId(), request).also { pending = it }
    }

    fun pending(operationId: String): PendingTagWrite? {
        return pending?.takeIf { it.operationId == operationId }
    }

    fun consume(operationId: String): PendingTagWrite? {
        val matching = pending(operationId) ?: return null
        pending = null
        return matching
    }
}
