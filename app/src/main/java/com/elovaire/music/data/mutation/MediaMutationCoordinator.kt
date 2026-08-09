package elovaire.music.droidbeauty.app.data.mutation

import kotlinx.coroutines.sync.Mutex

/** Serializes destructive media replacement across tag, artwork, and lyrics mutations. */
internal object MediaMutationCoordinator {
    val mutex = Mutex()
}
