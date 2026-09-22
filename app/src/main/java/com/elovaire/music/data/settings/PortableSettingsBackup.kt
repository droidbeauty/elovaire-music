package elovaire.music.droidbeauty.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import elovaire.music.droidbeauty.app.core.AndroidAppClock
import elovaire.music.droidbeauty.app.core.AppClock
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(FlowPreview::class)
internal class PortableSettingsBackup(
    context: Context,
    private val clock: AppClock = AndroidAppClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ownerScope: CoroutineScope? = null,
) {
    private val appContext = context.applicationContext
    private val settingsDataStore: DataStore<Preferences> = appContext.elovaireSettingsDataStore()
    private val backup by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        allowStrictModeDiskReads {
            appContext.getSharedPreferences(BACKUP_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    private val bootSnapshot by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        allowStrictModeDiskReads {
            appContext.getSharedPreferences(BOOT_SNAPSHOT_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    private val restored = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val released = AtomicBoolean(false)
    private val pendingBootSnapshot = AtomicReference<Map<String, Any?>?>(null)
    private val bootSnapshotJobLock = Any()
    private var bootSnapshotJob: Job? = null
    private val mirrorScope = CoroutineScope(
            (ownerScope?.coroutineContext ?: EmptyCoroutineContext) +
            SupervisorJob(ownerScope?.coroutineContext?.get(Job)) +
            ioDispatcher + CoroutineName("portable-settings-backup"),
    )
    private var settingsObservationJob: Job? = null

    /** Returns the last validated synchronous boot snapshot without touching DataStore. */
    fun bootSettingsSnapshot(): SettingsSnapshot {
        val stored = readBootSnapshot()
        if (stored != null) return SettingsSnapshot(stored)

        val legacy = allowStrictModeDiskReads {
            appContext.getSharedPreferences(PreferenceStorage.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                .all
                .filterKeys { it in settingsPreferenceKeys }
        }
        return SettingsSnapshot(legacy.mapValues { (_, value) -> value })
    }

    /** Checkpoints only the small immutable settings snapshot needed for the next process start. */
    fun checkpointBootSettings(values: Map<String, Any?>) {
        if (released.get()) return
        val filtered = values.filterKeys { it in settingsPreferenceKeys }
            pendingBootSnapshot.set(filtered)
            synchronized(bootSnapshotJobLock) {
                if (bootSnapshotJob?.isActive != true && !released.get()) {
                bootSnapshotJob = mirrorScope.launch {
                    kotlinx.coroutines.delay(BOOT_SNAPSHOT_COALESCE_DELAY_MS)
                    flushPendingBootSnapshot()
                }
            }
        }
    }

    fun restore() {
        start()
    }

    @Suppress("TooGenericExceptionCaught")
    fun start() {
        if (released.get()) return
        if (!started.compareAndSet(false, true)) return
        settingsObservationJob = mirrorScope.launch {
            try {
                ElovaireTrace.suspendSection("portable_settings_restore") {
                    restoreDataStoreIfEmpty()
                }
                settingsDataStore.data
                    .debounce(MIRROR_COALESCE_DELAY_MS)
                    .collect { settings ->
                        try {
                            syncAll(settings)
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (failure: RuntimeException) {
                            android.util.Log.w(TAG, "Unable to update portable settings backup.", failure)
                        }
                    }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: RuntimeException) {
                android.util.Log.w(TAG, "Portable settings backup observation stopped.", failure)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun release() {
        if (!released.compareAndSet(false, true)) return
        if (started.compareAndSet(true, false)) {
            settingsObservationJob?.cancel()
        }
        settingsObservationJob = null
        mirrorScope.launch {
            try {
                flushBootSnapshot()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: RuntimeException) {
                android.util.Log.w(TAG, "Unable to flush settings boot snapshot during release.", failure)
            } finally {
                mirrorScope.cancel()
            }
        }
    }

    suspend fun flushBootSnapshot() {
        val job = synchronized(bootSnapshotJobLock) { bootSnapshotJob }
        job?.join()
        flushPendingBootSnapshot()
    }

    private suspend fun flushPendingBootSnapshot() {
        while (true) {
            val values = pendingBootSnapshot.getAndSet(null) ?: break
            writeBootSnapshot(values)
        }
        synchronized(bootSnapshotJobLock) {
            bootSnapshotJob = null
            if (pendingBootSnapshot.get() != null && !released.get()) {
                bootSnapshotJob = mirrorScope.launch { flushPendingBootSnapshot() }
            }
        }
    }

    private fun writeBootSnapshot(values: Map<String, Any?>) {
        val mergedValues = readBootSnapshot().orEmpty() + values
        val normalizedValues = mergedValues
            .filterKeys { it in settingsPreferenceKeys }
            .filterValues { it != null }
        val editor = bootSnapshot.edit()
        bootSnapshot.all.keys
            .filterNot { it == BOOT_FORMAT_VERSION_KEY || it == BOOT_CHECKSUM_KEY }
            .forEach(editor::remove)
        normalizedValues.forEach { (key, value) -> editor.putPreferenceValue(key, value) }
        editor.putInt(BOOT_FORMAT_VERSION_KEY, BOOT_FORMAT_VERSION)
        editor.putString(BOOT_CHECKSUM_KEY, settingsBootSnapshotChecksum(normalizedValues))
        check(editor.commit()) { "Unable to persist settings boot snapshot" }
    }

    private fun syncAll(settings: Preferences) {
        ElovaireTrace.section("settings_backup_checkpoint") {
            val desired = settings.portableValues()
            val current = backup.all.filterKeys(::isPortableSettingKey)
            val checksum = portableSettingsBackupChecksum(desired)
            val currentVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
            val currentChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
            if (
                desired == current &&
                currentVersion == BACKUP_FORMAT_VERSION &&
                currentChecksum == checksum
            ) return@section
            val editor = backup.edit()
            (current.keys - desired.keys).forEach(editor::remove)
            desired.forEach { (key, value) -> editor.putPreferenceValue(key, value) }
            editor.putInt(BACKUP_FORMAT_VERSION_KEY, BACKUP_FORMAT_VERSION)
            editor.putString(BACKUP_CHECKSUM_KEY, checksum)
            editor.putLong(BACKUP_CREATED_AT_KEY, clock.wallTimeMs())
            check(editor.commit()) { "Unable to persist portable settings backup" }
        }
    }

    private suspend fun restoreDataStoreIfEmpty() {
        if (restored.getAndSet(true)) return
        val bootValues = readBootSnapshot()
        val backupValues = backup.all.filterKeys(::isPortableSettingKey)
        val current = settingsDataStore.data.first().asMap()
        if (current.isNotEmpty()) return
        val values = when {
            bootValues != null -> bootValues
            backupValues.isNotEmpty() && isValidBackup(backupValues) -> backupValues
            else -> emptyMap()
        }
        if (values.isEmpty()) return
        settingsDataStore.edit { target ->
            values.forEach { (key, value) -> target.putDataStoreValue(key, value) }
        }
    }

    private fun readBootSnapshot(): Map<String, Any?>? {
        return allowStrictModeDiskReads {
            val values = bootSnapshot.all
                .filterKeys { it in settingsPreferenceKeys }
            if (values.isEmpty()) return@allowStrictModeDiskReads null
            val version = runCatching { bootSnapshot.getInt(BOOT_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
            val checksum = runCatching { bootSnapshot.getString(BOOT_CHECKSUM_KEY, null) }.getOrNull()
            if (version != BOOT_FORMAT_VERSION || checksum != settingsBootSnapshotChecksum(values)) return@allowStrictModeDiskReads null
            values
        }
    }

    private fun isValidBackup(values: Map<String, *>): Boolean {
        val storedVersion = runCatching { backup.getInt(BACKUP_FORMAT_VERSION_KEY, 0) }.getOrDefault(0)
        val storedChecksum = runCatching { backup.getString(BACKUP_CHECKSUM_KEY, null) }.getOrNull()
        if (storedVersion == 0 && storedChecksum == null) return true
        return storedVersion == BACKUP_FORMAT_VERSION &&
            storedChecksum == portableSettingsBackupChecksum(values)
    }

    private fun android.content.SharedPreferences.Editor.putPreferenceValue(key: String, value: Any?): android.content.SharedPreferences.Editor {
        return when (value) {
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value.filterIsInstance<String>().toSet())
            else -> remove(key)
        }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.putDataStoreValue(
        key: String,
        value: Any?,
    ) {
        when (value) {
            is Boolean -> set(androidx.datastore.preferences.core.booleanPreferencesKey(key), value)
            is Float -> set(androidx.datastore.preferences.core.floatPreferencesKey(key), value)
            is Int -> set(androidx.datastore.preferences.core.intPreferencesKey(key), value)
            is Long -> set(androidx.datastore.preferences.core.longPreferencesKey(key), value)
            is String -> set(androidx.datastore.preferences.core.stringPreferencesKey(key), value)
            is Set<*> -> set(
                androidx.datastore.preferences.core.stringSetPreferencesKey(key),
                value.filterIsInstance<String>().toSet(),
            )
        }
    }

    private companion object {
        const val BACKUP_FILE_NAME = "portable_settings"
        const val BACKUP_FORMAT_VERSION_KEY = "_format_version"
        const val BACKUP_CHECKSUM_KEY = "_checksum"
        const val BACKUP_CREATED_AT_KEY = "_created_at_ms"
        const val BACKUP_FORMAT_VERSION = 1
        const val BOOT_SNAPSHOT_FILE_NAME = "settings_boot_snapshot"
        const val BOOT_FORMAT_VERSION_KEY = "_format_version"
        const val BOOT_CHECKSUM_KEY = "_checksum"
        const val BOOT_FORMAT_VERSION = 1
        const val BOOT_SNAPSHOT_COALESCE_DELAY_MS = 100L
        const val MIRROR_COALESCE_DELAY_MS = 400L
        const val TAG = "PortableSettingsBackup"
    }
}

private fun Preferences.portableValues(): Map<String, Any?> = asMap()
    .asSequence()
    .filter { (key, _) -> isPortableSettingKey(key.name) }
    .associate { (key, value) -> key.name to value }

internal fun portableSettingsBackupChecksum(values: Map<String, *>): String {
    val canonical = values
        .filterKeys(::isPortableSettingKey)
        .toSortedMap()
        .entries
        .joinToString("\n") { (key, value) ->
            val (type, encoded) = when (value) {
                is Boolean -> "boolean" to value.toString()
                is Float -> "float" to value.toString()
                is Int -> "int" to value.toString()
                is Long -> "long" to value.toString()
                is Set<*> -> "string_set" to value.filterIsInstance<String>().sorted().joinToString(",")
                else -> "string" to value?.toString().orEmpty()
            }
            "$key:$type:${encoded.length}:$encoded"
        }
    return MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}

internal fun settingsBootSnapshotChecksum(values: Map<String, *>): String {
    val canonical = values
        .filterKeys { it in settingsPreferenceKeys }
        .toSortedMap()
        .entries
        .joinToString("\n") { (key, value) ->
            val (type, encoded) = when (value) {
                is Boolean -> "boolean" to value.toString()
                is Float -> "float" to value.toString()
                is Int -> "int" to value.toString()
                is Long -> "long" to value.toString()
                is Set<*> -> "string_set" to value.filterIsInstance<String>().sorted().joinToString(",")
                else -> "string" to value?.toString().orEmpty()
            }
            "$key:$type:${encoded.length}:$encoded"
        }
    return MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}

internal fun isPortableSettingKey(key: String): Boolean = key in portableSettingKeys

private val portableSettingKeys = setOf(
            "theme_mode",
            "text_size_preset",
            "app_language",
            "playback_volume",
            "crossfade_enabled",
            "crossfade_duration_ms",
            "crossfade_silence_threshold_db",
            "audiobook_rewind_seconds",
            "audiobook_forward_seconds",
            "audiobook_resume_playback",
            "smart_playlist_enabled_types",
            "smart_playlist_max_songs",
            "volume_normalization_enabled",
            "online_lyrics_enabled",
            "album_collection_grid_enabled",
            "album_collection_layout_mode",
            "song_collection_grid_enabled",
            "album_collection_sort_mode",
            "song_collection_sort_mode",
            "eq_bands",
            "eq_preamp_db",
            "eq_bass",
            "eq_midrange",
            "eq_treble",
            "eq_spaciousness",
            "eq_spaciousness_mode",
            "eq_reverb_duration_ms",
            "eq_reverb_profile",
            "eq_custom_presets",
)
