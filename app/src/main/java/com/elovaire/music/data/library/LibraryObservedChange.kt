package elovaire.music.droidbeauty.app.data.library

import android.net.Uri
import android.os.FileObserver

internal sealed interface LibraryObservedChange {
    data class MediaStore(val uri: Uri?) : LibraryObservedChange

    data class SafTree(val uri: Uri?) : LibraryObservedChange

    data class DirectFile(
        val path: String,
        val operation: DirectFileOperation,
    ) : LibraryObservedChange

    data object CoverageIncomplete : LibraryObservedChange
}

internal enum class DirectFileOperation {
    Create,
    CloseWrite,
    MoveTo,
    Delete,
    MoveFrom,
    DirectoryTopology,
    Other,
}

internal fun directFileOperation(
    event: Int,
    changedFileIsDirectory: Boolean = false,
): DirectFileOperation {
    if (changedFileIsDirectory && event and FileObserver.CREATE != 0) {
        return DirectFileOperation.DirectoryTopology
    }
    return when {
        event and FileObserver.CLOSE_WRITE != 0 -> DirectFileOperation.CloseWrite
        event and FileObserver.MOVED_TO != 0 -> DirectFileOperation.MoveTo
        event and FileObserver.MOVED_FROM != 0 -> DirectFileOperation.MoveFrom
        event and FileObserver.DELETE != 0 -> DirectFileOperation.Delete
        event and FileObserver.DELETE_SELF != 0 -> DirectFileOperation.Delete
        event and FileObserver.MOVE_SELF != 0 -> DirectFileOperation.MoveFrom
        event and FileObserver.CREATE != 0 -> DirectFileOperation.Create
        event and FileObserver.MODIFY != 0 -> DirectFileOperation.Other
        else -> DirectFileOperation.DirectoryTopology
    }
}

enum class LibraryRefreshPriority {
    Background,
    FreshnessCritical,
}
