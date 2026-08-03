package elovaire.music.droidbeauty.app.data.update

import android.content.Context
import elovaire.music.droidbeauty.app.core.AppBackgroundWorkPolicy
import elovaire.music.droidbeauty.app.data.settings.UpdatePreferencesStore
import kotlinx.coroutines.CoroutineScope

internal fun createUpdateController(
    context: Context,
    scope: CoroutineScope,
    preferences: UpdatePreferencesStore,
    backgroundWorkPolicy: AppBackgroundWorkPolicy,
): UpdateController = GitHubUpdateController(
    context = context,
    scope = scope,
    preferences = preferences,
    backgroundWorkPolicy = backgroundWorkPolicy,
)
