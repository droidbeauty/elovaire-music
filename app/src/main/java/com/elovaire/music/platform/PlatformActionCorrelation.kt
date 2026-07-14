package elovaire.music.droidbeauty.app.platform

internal fun matchesPlatformActionResult(
    pendingOperationId: String?,
    resultOperationId: String,
): Boolean {
    return pendingOperationId != null && pendingOperationId == resultOperationId
}
