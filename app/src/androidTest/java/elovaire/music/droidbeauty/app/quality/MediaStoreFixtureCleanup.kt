package elovaire.music.droidbeauty.app.quality

import android.content.ContentResolver
import android.net.Uri

internal fun ContentResolver.deleteTestOwnedRows(uris: Collection<Uri>, fixtureName: String) {
    val failures = mutableListOf<Exception>()
    uris.forEach { uri ->
        try {
            val deleted = delete(uri, null, null)
            if (deleted != 1) {
                failures += IllegalStateException("$fixtureName row cleanup deleted $deleted rows for $uri.")
            }
        } catch (failure: IllegalStateException) {
            failures += failure
        } catch (failure: SecurityException) {
            failures += failure
        } catch (failure: IllegalArgumentException) {
            failures += failure
        } catch (failure: UnsupportedOperationException) {
            failures += failure
        }
    }
    if (failures.isNotEmpty()) {
        throw AssertionError("Unable to clean up $fixtureName rows.").apply {
            failures.forEach(::addSuppressed)
        }
    }
}
