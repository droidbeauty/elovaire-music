package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppServiceScopesTest {
    @Test
    fun childScopesCanBeReleasedIndependently() {
        val parent = SupervisorJob()
        val scopes = AppServiceScopes(
            kotlinx.coroutines.CoroutineScope(parent + Dispatchers.Unconfined),
        )
        val playbackJob = scopes.playback.coroutineContext[Job]
        val libraryJob = scopes.library.coroutineContext[Job]

        scopes.cancelPlayback()

        assertFalse(playbackJob?.isActive == true)
        assertTrue(libraryJob?.isActive == true)

        scopes.cancelLibrary()
        scopes.cancelOptional()
        parent.cancel()
    }

    @Test
    fun unexpected_worker_failure_is_owned_and_does_not_cancel_siblings() {
        BackendDiagnostics.clear()
        val parent = SupervisorJob()
        val scopes = AppServiceScopes(
            kotlinx.coroutines.CoroutineScope(parent + Dispatchers.Unconfined),
        )

        scopes.optional.launch { error("worker failure") }

        assertTrue(parent.isActive)
        assertTrue(scopes.playback.coroutineContext[Job]?.isActive == true)
        assertEquals("WorkerFailed", BackendDiagnostics.snapshot().single().name)
        assertEquals("optional", BackendDiagnostics.snapshot().single().fields["owner"])

        parent.cancel()
    }
}
