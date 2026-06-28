package elovaire.music.droidbeauty.app.platform

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

internal fun mediaStoreWriteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest {
    return IntentSenderRequest.Builder(
        MediaStore.createWriteRequest(context.contentResolver, uris.toList()).intentSender,
    ).build()
}

internal fun mediaStoreDeleteRequest(
    context: Context,
    uris: Collection<Uri>,
): IntentSenderRequest {
    return IntentSenderRequest.Builder(
        MediaStore.createDeleteRequest(context.contentResolver, uris.toList()).intentSender,
    ).build()
}
