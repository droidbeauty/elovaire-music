package elovaire.music.droidbeauty.app.core

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnostics
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticContext
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import elovaire.music.droidbeauty.app.core.backend.BackendResourceTracker
import elovaire.music.droidbeauty.app.core.backend.BackendDiagnosticsRuntime
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

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
    val versionCode: Int = BuildConfig.VERSION_CODE,
)

internal data class PreviousProcessBreadcrumb(
    val processId: String,
    val versionCode: Int,
    val phase: String,
    val subsystem: String?,
    val operationPhase: String?,
    val outcome: String?,
    val resourceCounts: Map<String, Int>,
    val timestampMs: Long,
)

internal data class AppExitSnapshot(
    val records: List<AppExitRecord>,
    val suppressOptionalStartup: Boolean,
    val previousProcessBreadcrumb: PreviousProcessBreadcrumb? = null,
)

internal class AppExitDiagnostics(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
    private val diagnostics: BackendDiagnosticsRuntime? = null,
    private val resources: BackendResourceTracker = diagnostics?.resources ?: BackendResourceRegistry,
) {
    private val appContext = context.applicationContext
    private val preferences = allowStrictModeDiskReads {
        appContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
    }
    private val processId = UUID.randomUUID().toString()
    private val lastBreadcrumbWriteMs = AtomicLong(0L)
    @Volatile private var lastBreadcrumbPayload: String? = null

    init {
        (diagnostics ?: BackendDiagnostics).installBreadcrumbCheckpoint { context ->
            checkpointRuntime(
                phase = context.phase ?: context.eventName,
                backendContext = context,
            )
        }
    }

    fun inspect(): AppExitSnapshot {
        val previousBreadcrumb = readBreadcrumb()
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val platformRecords = runCatching {
            activityManager?.getHistoricalProcessExitReasons(appContext.packageName, 0, MAX_PLATFORM_RECORDS)
        }.getOrNull().orEmpty().map { info ->
            AppExitRecord(
                reason = info.reason,
                status = info.status,
                importance = info.importance,
                timestampMs = info.timestamp,
                category = classifyAppExitReason(info.reason, info.description),
                versionCode = BuildConfig.VERSION_CODE,
            )
        }
        val records = (platformRecords + readStored())
            .distinctBy { it.timestampMs to it.reason }
            .sortedByDescending(AppExitRecord::timestampMs)
            .take(MAX_STORED_RECORDS)
        writeStored(records)
        return AppExitSnapshot(
            records = records,
            suppressOptionalStartup = shouldSuppressOptionalStartup(records, clock.wallTimeMs()),
            previousProcessBreadcrumb = previousBreadcrumb,
        )
    }

    fun checkpointRuntime(
        phase: String,
        backendContext: BackendDiagnosticContext? = diagnostics?.lastContext() ?: BackendDiagnostics.lastContext(),
        outcome: String? = null,
        force: Boolean = false,
    ) {
        val now = clock.wallTimeMs()
        val previous = lastBreadcrumbWriteMs.get()
        if (!force && now - previous in 0 until BREADCRUMB_MIN_WRITE_INTERVAL_MS) return
        val payload = encodePreviousProcessBreadcrumb(
            breadcrumb = PreviousProcessBreadcrumb(
                processId = processId,
                versionCode = BuildConfig.VERSION_CODE,
                phase = phase.safeBreadcrumbValue(),
                subsystem = backendContext?.subsystem?.safeBreadcrumbValue(),
                operationPhase = backendContext?.phase?.safeBreadcrumbValue(),
                outcome = outcome?.safeBreadcrumbValue(),
                resourceCounts = resources.snapshot(),
                timestampMs = now,
            ),
        )
        if (!force && payload == lastBreadcrumbPayload) return
        if (!lastBreadcrumbWriteMs.compareAndSet(previous, now)) return
        lastBreadcrumbPayload = payload
        runCatching { preferences.edit().putString(KEY_BREADCRUMB, payload).apply() }
    }

    fun release() {
        (diagnostics ?: BackendDiagnostics).clearBreadcrumbCheckpoint()
    }

    private fun readStored(): List<AppExitRecord> {
        val array = runCatching { JSONArray(preferences.getString(KEY_RECORDS, "[]")) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until minOf(array.length(), MAX_STORED_RECORDS)) {
                val item = array.optJSONObject(index) ?: continue
                val category = AppExitCategory.entries.firstOrNull { it.name == item.optString("category") }
                    ?: AppExitCategory.Unknown
                val versionCode = item.optInt("versionCode", -1)
                if (versionCode != BuildConfig.VERSION_CODE) continue
                add(
                    AppExitRecord(
                        reason = item.optInt("reason", -1),
                        status = item.optInt("status", 0),
                        importance = item.optInt("importance", 0),
                        timestampMs = item.optLong("timestamp", 0L),
                        category = category,
                        versionCode = versionCode,
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
                    .put("category", record.category.name)
                    .put("versionCode", record.versionCode),
            )
        }
        val serialized = array.toString()
        if (preferences.getString(KEY_RECORDS, null) == serialized) return
        preferences.edit().putString(KEY_RECORDS, serialized).apply()
    }

    private fun readBreadcrumb(): PreviousProcessBreadcrumb? {
        val encoded = runCatching { preferences.getString(KEY_BREADCRUMB, null) }.getOrNull() ?: return null
        val breadcrumb = decodePreviousProcessBreadcrumb(encoded) ?: run {
            preferences.edit().remove(KEY_BREADCRUMB).apply()
            return null
        }
        if (!isUsablePreviousProcessBreadcrumb(breadcrumb, clock.wallTimeMs(), BuildConfig.VERSION_CODE)) {
            preferences.edit().remove(KEY_BREADCRUMB).apply()
            return null
        }
        return breadcrumb
    }

    private companion object {
        const val PREFERENCE_FILE = "exit_diagnostics"
        const val KEY_RECORDS = "records"
        const val KEY_BREADCRUMB = "previous_process_breadcrumb"
        const val MAX_PLATFORM_RECORDS = 12
        const val MAX_STORED_RECORDS = 12
        const val BREADCRUMB_MIN_WRITE_INTERVAL_MS = 2_000L
    }
}

internal fun isUsablePreviousProcessBreadcrumb(
    breadcrumb: PreviousProcessBreadcrumb,
    nowMs: Long,
    currentVersionCode: Int,
): Boolean {
    val age = nowMs - breadcrumb.timestampMs
    return breadcrumb.versionCode == currentVersionCode && age in 0..BREADCRUMB_MAX_AGE_MS
}

internal fun encodePreviousProcessBreadcrumb(breadcrumb: PreviousProcessBreadcrumb): String {
    val resources = breadcrumb.resourceCounts.entries
        .filter { (_, count) -> count > 0 }
        .sortedBy { it.key }
        .take(MAX_BREADCRUMB_RESOURCES)
        .joinToString(",") { (key, count) ->
            "${encodeBreadcrumbPart(key)}=${count.coerceAtMost(MAX_RESOURCE_COUNT)}"
        }
    val payload = listOf(
        "1",
        encodeBreadcrumbPart(breadcrumb.processId),
        breadcrumb.versionCode.toString(),
        encodeBreadcrumbPart(breadcrumb.phase),
        encodeBreadcrumbPart(breadcrumb.subsystem.orEmpty()),
        encodeBreadcrumbPart(breadcrumb.operationPhase.orEmpty()),
        encodeBreadcrumbPart(breadcrumb.outcome.orEmpty()),
        breadcrumb.timestampMs.toString(),
        resources,
        "end",
    ).joinToString("|")
    return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(Charsets.UTF_8))
}

internal fun decodePreviousProcessBreadcrumb(encoded: String): PreviousProcessBreadcrumb? {
    return runCatching {
        val payload = String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
        val fields = payload.split('|')
        if (fields.size != 10 || fields[0] != "1" || fields[9] != "end") return null
        val resources = buildMap {
            fields[8].takeIf(String::isNotBlank)?.split(',')
                .orEmpty()
                .take(MAX_BREADCRUMB_RESOURCES)
                .forEach { entry ->
                    val separator = entry.lastIndexOf('=')
                    if (separator <= 0) return@forEach
                    val key = decodeBreadcrumbPart(entry.substring(0, separator))
                    val value = entry.substring(separator + 1).toIntOrNull()
                    if (key != null && key.isNotBlank() && value != null && value in 1..MAX_RESOURCE_COUNT) {
                        put(key, value)
                    }
                }
        }
        PreviousProcessBreadcrumb(
            processId = decodeBreadcrumbPart(fields[1])?.takeIf(String::isNotBlank) ?: return null,
            versionCode = fields[2].toIntOrNull() ?: return null,
            phase = decodeBreadcrumbPart(fields[3])?.takeIf(String::isNotBlank) ?: return null,
            subsystem = decodeBreadcrumbPart(fields[4])?.takeIf(String::isNotBlank),
            operationPhase = decodeBreadcrumbPart(fields[5])?.takeIf(String::isNotBlank),
            outcome = decodeBreadcrumbPart(fields[6])?.takeIf(String::isNotBlank),
            resourceCounts = resources,
            timestampMs = fields[7].toLongOrNull() ?: return null,
        )
    }.getOrNull()
}

private fun encodeBreadcrumbPart(value: String): String = Base64.getUrlEncoder()
    .withoutPadding()
    .encodeToString(value.safeBreadcrumbValue().toByteArray(Charsets.UTF_8))

private fun decodeBreadcrumbPart(value: String): String? = runCatching {
    String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
}.getOrNull()

private fun String.safeBreadcrumbValue(): String = filter { it in '\u0020'..'\u007e' }
    .take(MAX_BREADCRUMB_VALUE_LENGTH)

private const val MAX_BREADCRUMB_RESOURCES = 16
private const val MAX_RESOURCE_COUNT = 1_000_000
private const val MAX_BREADCRUMB_VALUE_LENGTH = 64
private const val BREADCRUMB_MAX_AGE_MS = 15L * 60L * 1_000L

internal fun shouldSuppressOptionalStartup(records: List<AppExitRecord>, nowMs: Long): Boolean {
    val cutoff = nowMs - 10L * 60L * 1_000L
    val recentRecords = records.filter { it.timestampMs in cutoff..nowMs }
    return recentRecords.any { it.category == AppExitCategory.ResourcePressure } ||
        recentRecords.count { record ->
            record.category == AppExitCategory.Crash || record.category == AppExitCategory.Anr
        } >= 3
}

internal fun classifyAppExitReason(reason: Int, description: String? = null): AppExitCategory {
    if (description?.contains("MemoryLimiter", ignoreCase = true) == true) {
        return AppExitCategory.ResourcePressure
    }
    return when (reason) {
    ApplicationExitInfo.REASON_CRASH,
    ApplicationExitInfo.REASON_CRASH_NATIVE,
    ApplicationExitInfo.REASON_INITIALIZATION_FAILURE,
    -> AppExitCategory.Crash
    ApplicationExitInfo.REASON_ANR -> AppExitCategory.Anr
    ApplicationExitInfo.REASON_LOW_MEMORY,
    ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE,
    -> AppExitCategory.ResourcePressure
    ApplicationExitInfo.REASON_USER_REQUESTED,
    ApplicationExitInfo.REASON_USER_STOPPED,
    ApplicationExitInfo.REASON_EXIT_SELF,
    ApplicationExitInfo.REASON_PACKAGE_UPDATED,
    -> AppExitCategory.Expected
    ApplicationExitInfo.REASON_DEPENDENCY_DIED,
    ApplicationExitInfo.REASON_FREEZER,
    ApplicationExitInfo.REASON_PACKAGE_STATE_CHANGE,
    ApplicationExitInfo.REASON_PERMISSION_CHANGE,
    ApplicationExitInfo.REASON_SIGNALED,
    ApplicationExitInfo.REASON_OTHER,
    -> AppExitCategory.System
    ApplicationExitInfo.REASON_UNKNOWN -> AppExitCategory.Unknown
    else -> AppExitCategory.Unknown
    }
}
