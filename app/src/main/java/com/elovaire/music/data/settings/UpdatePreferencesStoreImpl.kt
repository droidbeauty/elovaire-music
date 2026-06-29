package elovaire.music.droidbeauty.app.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class UpdatePreferencesStoreImpl(
    private val preferences: SharedPreferences,
) : UpdatePreferencesStore {
    private val dismissedUpdateVersionState = MutableStateFlow(loadDismissedUpdateVersion())
    override val dismissedUpdateVersion: StateFlow<String?> = dismissedUpdateVersionState.asStateFlow()

    override fun setDismissedUpdateVersion(versionName: String?) {
        val normalizedVersion = versionName?.trim()?.takeIf { it.isNotBlank() }
        if (dismissedUpdateVersionState.value == normalizedVersion) return
        preferences.edit {
            if (normalizedVersion == null) {
                remove(KEY_DISMISSED_UPDATE_VERSION)
            } else {
                putString(KEY_DISMISSED_UPDATE_VERSION, normalizedVersion)
            }
        }
        dismissedUpdateVersionState.value = normalizedVersion
    }

    override fun lastAutomaticUpdateCheckAtMs(): Long {
        return preferences.getLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, 0L).coerceAtLeast(0L)
    }

    override fun setLastAutomaticUpdateCheckAtMs(timestampMs: Long) {
        val normalizedTimestamp = timestampMs.coerceAtLeast(0L)
        if (lastAutomaticUpdateCheckAtMs() == normalizedTimestamp) return
        preferences.edit {
            putLong(KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS, normalizedTimestamp)
        }
    }

    private fun loadDismissedUpdateVersion(): String? {
        return preferences.getString(KEY_DISMISSED_UPDATE_VERSION, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        const val KEY_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
        const val KEY_LAST_AUTOMATIC_UPDATE_CHECK_AT_MS = "last_automatic_update_check_at_ms"
    }
}
