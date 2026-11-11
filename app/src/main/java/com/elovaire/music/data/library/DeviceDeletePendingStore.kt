package elovaire.music.droidbeauty.app.data.library

import android.content.Context
import android.content.SharedPreferences
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.data.settings.PreferenceStorage

internal interface DeviceDeletePendingStore {
    fun persist(operationId: String, serializedPlan: String): Boolean
    fun read(): String?
    fun clear(operationId: String): Boolean
}

internal class PreferenceBackedDeviceDeleteStore(context: Context) : DeviceDeletePendingStore {
    private val preferences = allowStrictModeDiskReads {
        PreferenceStorage(context.applicationContext).preferences
    }

    override fun persist(operationId: String, serializedPlan: String): Boolean =
        persistPendingDeleteState(preferences, operationId, serializedPlan)

    override fun read(): String? = synchronized(preferences) {
        preferences.getString(KEY_PENDING_DEVICE_DELETE_PLAN, null)
    }

    override fun clear(operationId: String): Boolean = synchronized(preferences) {
        if (preferences.getString(KEY_PENDING_DEVICE_DELETE_OPERATION_ID, null) != operationId) {
            true
        } else {
            preferences.edit()
                .remove(KEY_PENDING_DEVICE_DELETE_OPERATION_ID)
                .remove(KEY_PENDING_DEVICE_DELETE_PLAN)
                .commit()
        }
    }
}

internal fun persistPendingDeleteState(
    preferences: SharedPreferences,
    operationId: String,
    serializedPlan: String,
): Boolean = synchronized(preferences) {
    if (preferences.contains(KEY_PENDING_DEVICE_DELETE_PLAN)) return@synchronized false
    preferences.edit()
        .putString(KEY_PENDING_DEVICE_DELETE_OPERATION_ID, operationId)
        .putString(KEY_PENDING_DEVICE_DELETE_PLAN, serializedPlan)
        .commit()
}

private const val KEY_PENDING_DEVICE_DELETE_OPERATION_ID = "pending_device_delete_operation_id"
private const val KEY_PENDING_DEVICE_DELETE_PLAN = "pending_device_delete_plan"
