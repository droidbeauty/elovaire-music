package elovaire.music.droidbeauty.app.platform

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

internal fun mediaStoreWriteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest? {
    val requestUris = uris.filter(Uri::isContentUri)
    if (requestUris.isEmpty()) return null
    return IntentSenderRequest.Builder(
        MediaStore.createWriteRequest(context.contentResolver, requestUris).intentSender,
    ).build()
}

internal fun mediaStoreDeleteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest? {
    val requestUris = uris.filter(Uri::isContentUri)
    if (requestUris.isEmpty()) return null
    return IntentSenderRequest.Builder(
        MediaStore.createDeleteRequest(context.contentResolver, requestUris).intentSender,
    ).build()
}

internal fun Uri.isContentUri(): Boolean {
    return scheme == "content"
}
