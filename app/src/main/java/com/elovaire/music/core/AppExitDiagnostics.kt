package elovaire.music.droidbeauty.app.core

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal enum class AppExitCategory {
    Expected,
    Crash,
    Anr,
    ResourcePressure,
    System,
    Unknown,
}

internal data class AppExitRecord(
    val reason: Int,
    val status: Int,
    val importance: Int,
    val timestampMs: Long,
    val category: AppExitCategory,
)

internal data class AppExitSnapshot(
    val records: List<AppExitRecord>,
    val suppressOptionalStartup: Boolean,
)

internal class AppExitDiagnostics(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)

    fun inspect(): AppExitSnapshot {
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val platformRecords = runCatching {
            activityManager?.getHistoricalProcessExitReasons(appContext.packageName, 0, MAX_PLATFORM_RECORDS)
        }.getOrNull().orEmpty().map { info ->
            AppExitRecord(
                reason = info.reason,
                status = info.status,
                importance = info.importance,
                timestampMs = info.timestamp,
                category = classifyAppExitReason(info.reason),
            )
        }
        val records = (platformRecords + readStored())
            .distinctBy { it.timestampMs to it.reason }
            .sortedByDescending(AppExitRecord::timestampMs)
            .take(MAX_STORED_RECORDS)
        writeStored(records)
        return AppExitSnapshot(records, shouldSuppressOptionalStartup(records, clock.wallTimeMs()))
    }

    private fun readStored(): List<AppExitRecord> {
        val array = runCatching { JSONArray(preferences.getString(KEY_RECORDS, "[]")) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until minOf(array.length(), MAX_STORED_RECORDS)) {
                val item = array.optJSONObject(index) ?: continue
                val category = AppExitCategory.entries.firstOrNull { it.name == item.optString("category") }
                    ?: AppExitCategory.Unknown
                add(
                    AppExitRecord(
                        reason = item.optInt("reason", -1),
                        status = item.optInt("status", 0),
                        importance = item.optInt("importance", 0),
                        timestampMs = item.optLong("timestamp", 0L),
                        category = category,
                    ),
                )
            }
        }
    }

    private fun writeStored(records: List<AppExitRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("reason", record.reason)
                    .put("status", record.status)
                    .put("importance", record.importance)
                    .put("timestamp", record.timestampMs)
                    .put("category", record.category.name),
            )
        }
        preferences.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    private companion object {
        const val PREFERENCE_FILE = "exit_diagnostics"
        const val KEY_RECORDS = "records"
        const val MAX_PLATFORM_RECORDS = 12
        const val MAX_STORED_RECORDS = 12
    }
}

internal fun shouldSuppressOptionalStartup(records: List<AppExitRecord>, nowMs: Long): Boolean {
    val cutoff = nowMs - 10L * 60L * 1_000L
    return records.count { record ->
        record.timestampMs >= cutoff &&
            (record.category == AppExitCategory.Crash || record.category == AppExitCategory.Anr)
    } >= 3
}

internal fun classifyAppExitReason(reason: Int): AppExitCategory = when (reason) {
    ApplicationExitInfo.REASON_CRASH,
    ApplicationExitInfo.REASON_CRASH_NATIVE,
    -> AppExitCategory.Crash
    ApplicationExitInfo.REASON_ANR -> AppExitCategory.Anr
    ApplicationExitInfo.REASON_LOW_MEMORY -> AppExitCategory.ResourcePressure
    ApplicationExitInfo.REASON_USER_REQUESTED,
    ApplicationExitInfo.REASON_EXIT_SELF,
    -> AppExitCategory.Expected
    ApplicationExitInfo.REASON_DEPENDENCY_DIED,
    ApplicationExitInfo.REASON_PERMISSION_CHANGE,
    ApplicationExitInfo.REASON_SIGNALED,
    ApplicationExitInfo.REASON_OTHER,
    -> AppExitCategory.System
    else -> AppExitCategory.Unknown
}
