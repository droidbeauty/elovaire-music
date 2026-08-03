package elovaire.music.droidbeauty.app.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class UpdatePreferencesStoreImpl(
    private val preferences: SharedPreferences,
) : UpdatePreferencesStore {
    private val dismissedVersionState = MutableStateFlow(loadDismissedVersion())
    override val dismissedUpdateVersion: StateFlow<String?> = dismissedVersionState.asStateFlow()

    override fun setDismissedUpdateVersion(versionName: String?) {
        val normalized = versionName?.trim()?.takeIf { it.isNotBlank() }
        if (dismissedVersionState.value == normalized) return
        preferences.edit {
            if (normalized == null) remove(KEY_DISMISSED_UPDATE_VERSION) else putString(KEY_DISMISSED_UPDATE_VERSION, normalized)
        }
        dismissedVersionState.value = normalized
    }

    override fun lastAutomaticUpdateCheckAtMs(): Long {
        return preferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L).coerceAtLeast(0L)
    }

    override fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long) {
        val normalized = timestampMs.coerceAtLeast(0L)
        if (lastAutomaticUpdateCheckAtMs() == normalized) return
        preferences.edit { putLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, normalized) }
    }

    private fun loadDismissedVersion(): String? {
        return preferences.getString(KEY_DISMISSED_UPDATE_VERSION, null)?.trim()?.takeIf { it.isNotBlank() }
    }

    private companion object {
        const val KEY_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
        const val KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS = "last_automatic_update_check_at_ms"
    }
}
