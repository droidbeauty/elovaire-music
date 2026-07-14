package elovaire.music.droidbeauty.app.ui.screens

import android.app.Activity
import android.net.TestUri
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteFailure
import elovaire.music.droidbeauty.app.data.lyrics.userMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LyricsSavePermissionStateTest {
    private val uri = TestUri("content://media/external/audio/media/42")
    private val awaiting = LyricsSavePermissionState.AwaitingResult("operation", uri)

    @Test
    fun resultOk_grantsExactPendingOperation() {
        assertEquals(
            LyricsPermissionResultDecision.Granted,
            resolveLyricsPermissionResult(awaiting, "operation", uri, Activity.RESULT_OK),
        )
    }

    @Test
    fun cancelledResult_isTheOnlyDenialDecision() {
        assertEquals(
            LyricsPermissionResultDecision.Denied,
            resolveLyricsPermissionResult(awaiting, "operation", uri, Activity.RESULT_CANCELED),
        )
    }

    @Test
    fun staleOperationOrUri_isRejected() {
        assertEquals(
            LyricsPermissionResultDecision.Stale,
            resolveLyricsPermissionResult(awaiting, "other", uri, Activity.RESULT_OK),
        )
        assertEquals(
            LyricsPermissionResultDecision.Stale,
            resolveLyricsPermissionResult(
                awaiting,
                "operation",
                TestUri("content://media/external/audio/media/43"),
                resultCode = Activity.RESULT_OK,
            ),
        )
    }

    @Test
    fun grantedState_cannotLaunchAnotherPermissionRetry() {
        assertEquals(
            LyricsPermissionResultDecision.Stale,
            resolveLyricsPermissionResult(
                LyricsSavePermissionState.Granted("operation", uri),
                "operation",
                uri,
                resultCode = Activity.RESULT_OK,
            ),
        )
    }

    @Test
    fun postGrantFailure_neverUsesDenialMessage() {
        assertNotEquals(
            LYRICS_PERMISSION_DENIED_MESSAGE,
            EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant.userMessage,
        )
        assertEquals(
            LYRICS_POST_GRANT_FAILURE_MESSAGE,
            EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant.userMessage,
        )
    }
}
