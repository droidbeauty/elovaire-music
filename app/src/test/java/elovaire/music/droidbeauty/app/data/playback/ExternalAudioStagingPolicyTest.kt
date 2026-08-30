package elovaire.music.droidbeauty.app.data.playback

import android.net.TestUri
import java.io.File
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalAudioStagingPolicyTest {
    @Test
    fun stageIdentityChangesWhenProviderRevisionChanges() {
        val uri = TestUri("content://provider/document/1")

        val original = externalAudioStageKey(uri, ExternalAudioStageMetadata(100L, 10L))
        val changedSize = externalAudioStageKey(uri, ExternalAudioStageMetadata(101L, 10L))
        val changedRevision = externalAudioStageKey(uri, ExternalAudioStageMetadata(100L, 11L))

        assertNotEquals(original, changedSize)
        assertNotEquals(original, changedRevision)
        assertEquals(original, externalAudioStageKey(uri, ExternalAudioStageMetadata(100L, 10L)))
    }

    @Test
    fun incompleteProviderMetadataCannotAuthorizeCacheReuse() {
        assertTrue(ExternalAudioStageMetadata(100L, 10L).hasReliableRevision)
        assertFalse(ExternalAudioStageMetadata(100L, null).hasReliableRevision)
        assertFalse(ExternalAudioStageMetadata(null, 10L).hasReliableRevision)
        assertFalse(ExternalAudioStageMetadata().hasReliableRevision)
    }

    @Test
    fun activePlaybackStageIsProtectedFromPruning() {
        val directory = File("/tmp/elovaire-external-audio-stage")
        val active = File(directory, "active.mp3")
        val inactive = File(directory, "inactive.mp3")
        ExternalAudioStageUsage.registerDirectory(directory)
        ExternalAudioStageUsage.setActivePlaybackUris(listOf(TestUri("file://${active.absolutePath}")))

        assertTrue(ExternalAudioStageUsage.isActive(active))
        assertFalse(ExternalAudioStageUsage.isActive(inactive))

        ExternalAudioStageUsage.setActivePlaybackUris(emptyList())
        assertFalse(ExternalAudioStageUsage.isActive(active))
    }
}
