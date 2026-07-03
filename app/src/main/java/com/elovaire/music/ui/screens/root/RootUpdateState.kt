package elovaire.music.droidbeauty.app.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRelease
import elovaire.music.droidbeauty.app.data.changelog.ChangelogRepository
import kotlinx.coroutines.delay

@Composable
internal fun rememberChangelogReleases(context: Context): List<ChangelogRelease> {
    return remember(context) { ChangelogRepository(context).loadReleases() }
}

@Composable
internal fun RootUpdateTransientStatusEffect(
    enabled: Boolean,
    transientStatus: Any?,
    clearTransientStatus: () -> Unit,
) {
    LaunchedEffect(enabled, transientStatus) {
        if (enabled && transientStatus != null) {
            delay(2_500L)
            clearTransientStatus()
        }
    }
}
