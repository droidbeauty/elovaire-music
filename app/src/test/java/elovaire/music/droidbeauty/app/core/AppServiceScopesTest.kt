package elovaire.music.droidbeauty.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertFalse
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
}
