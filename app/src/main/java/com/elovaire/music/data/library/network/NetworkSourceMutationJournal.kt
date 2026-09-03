package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import org.json.JSONArray
import org.json.JSONObject

internal enum class NetworkSourceMutationKind {
    Save,
    Remove,
}

internal data class NetworkSourceMutationMarker(
    val sourceId: String,
    val kind: NetworkSourceMutationKind,
    val previousCredentialKey: String?,
    val newCredentialKey: String?,
    val previousLocationFingerprint: String?,
    val newLocationFingerprint: String?,
    val phase: String,
)

/** Small commit marker for the two preference stores and the source-scoped inventory. */
internal class NetworkSourceMutationJournal(context: Context) {
    private val preferences = allowStrictModeDiskReads {
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }
    private val lock = Any()

    fun prepareSave(
        sourceId: String,
        previousCredentialKey: String?,
        newCredentialKey: String,
        previousLocationFingerprint: String?,
        newLocationFingerprint: String,
    ) = write(
        NetworkSourceMutationMarker(
            sourceId = sourceId,
            kind = NetworkSourceMutationKind.Save,
            previousCredentialKey = previousCredentialKey,
            newCredentialKey = newCredentialKey,
            previousLocationFingerprint = previousLocationFingerprint,
            newLocationFingerprint = newLocationFingerprint,
            phase = PHASE_PREPARED,
        ),
    )

    fun prepareRemove(source: NetworkLibrarySource) = write(
        NetworkSourceMutationMarker(
            sourceId = source.id,
            kind = NetworkSourceMutationKind.Remove,
            previousCredentialKey = source.credentialKey,
            newCredentialKey = null,
            previousLocationFingerprint = NetworkSourceIdentity.locationFingerprint(source),
            newLocationFingerprint = null,
            phase = PHASE_PREPARED,
        ),
    )

    fun markPhase(sourceId: String, phase: String) {
        synchronized(lock) {
            val current = readLocked().firstOrNull { it.sourceId == sourceId } ?: return
            writeLocked(readLocked().map { marker ->
                if (marker.sourceId == sourceId) current.copy(phase = phase) else marker
            })
        }
    }

    fun clear(sourceId: String) {
        synchronized(lock) {
            val next = readLocked().filterNot { it.sourceId == sourceId }
            writeLocked(next)
        }
    }

    fun pending(): List<NetworkSourceMutationMarker> = synchronized(lock) { readLocked() }

    private fun write(marker: NetworkSourceMutationMarker) {
        synchronized(lock) {
            val current = readLocked()
            val replacingExisting = current.any { it.sourceId == marker.sourceId }
            check(replacingExisting || current.size < MAX_MARKERS) {
                "Network source mutation journal is full"
            }
            writeLocked(current.filterNot { it.sourceId == marker.sourceId } + marker)
        }
    }

    private fun writeLocked(markers: List<NetworkSourceMutationMarker>) {
        check(markers.size <= MAX_MARKERS) { "Network source mutation journal is full" }
        val array = JSONArray()
        markers.forEach { marker ->
            array.put(
                JSONObject()
                    .put("sourceId", marker.sourceId)
                    .put("kind", marker.kind.name)
                    .put("previousCredentialKey", marker.previousCredentialKey)
                    .put("newCredentialKey", marker.newCredentialKey)
                    .put("previousLocationFingerprint", marker.previousLocationFingerprint)
                    .put("newLocationFingerprint", marker.newLocationFingerprint)
                    .put("phase", marker.phase),
            )
        }
        check(preferences.edit().putString(KEY_MARKERS, array.toString()).commit()) {
            "Unable to persist network source mutation marker"
        }
    }

    private fun readLocked(): List<NetworkSourceMutationMarker> {
        val array = runCatching { JSONArray(preferences.getString(KEY_MARKERS, "[]")) }.getOrNull()
            ?: return emptyList()
        return buildList {
            repeat(minOf(array.length(), MAX_MARKERS)) { index ->
                val item = array.optJSONObject(index) ?: return@repeat
                val kind = runCatching { NetworkSourceMutationKind.valueOf(item.optString("kind")) }.getOrNull()
                    ?: return@repeat
                val sourceId = item.optString("sourceId").trim().takeIf(String::isNotBlank)
                    ?: return@repeat
                add(
                    NetworkSourceMutationMarker(
                        sourceId = sourceId,
                        kind = kind,
                        previousCredentialKey = item.optString("previousCredentialKey").takeIf(String::isNotBlank),
                        newCredentialKey = item.optString("newCredentialKey").takeIf(String::isNotBlank),
                        previousLocationFingerprint = item.optString("previousLocationFingerprint")
                            .takeIf(String::isNotBlank),
                        newLocationFingerprint = item.optString("newLocationFingerprint")
                            .takeIf(String::isNotBlank),
                        phase = item.optString("phase", PHASE_PREPARED),
                    ),
                )
            }
        }
    }

    private companion object {
        const val PREFERENCES = "network_source_mutations_v1"
        const val KEY_MARKERS = "markers"
        const val MAX_MARKERS = 32
        const val PHASE_PREPARED = "prepared"
    }
}

internal suspend fun NetworkSourceMutationJournal.recover(
    sourceStore: NetworkLibrarySourceStore,
    credentialStore: NetworkCredentialStore,
    inventoryStore: NetworkInventoryStore,
    invalidateRuntime: ((sourceId: String) -> Unit)? = null,
) {
    pending().forEach { marker ->
        // Recovery may run after process death before the runtime registry has
        // observed the durable mutation. Drop any session that still reflects
        // the interrupted operation before the source is used again.
        invalidateRuntime?.invoke(marker.sourceId)
        when (marker.kind) {
            NetworkSourceMutationKind.Save -> {
                val current = sourceStore.sources.value.firstOrNull { it.id == marker.sourceId }
                if (current?.credentialKey == marker.newCredentialKey) {
                    marker.previousCredentialKey
                        ?.takeIf { it != marker.newCredentialKey }
                        ?.let(credentialStore::remove)
                    if (marker.previousLocationFingerprint != marker.newLocationFingerprint) {
                        inventoryStore.remove(marker.sourceId)
                    }
                } else {
                    marker.newCredentialKey?.let(credentialStore::remove)
                }
            }

            NetworkSourceMutationKind.Remove -> {
                if (sourceStore.sources.value.none { it.id == marker.sourceId }) {
                    marker.previousCredentialKey?.let(credentialStore::remove)
                    inventoryStore.remove(marker.sourceId)
                }
            }
        }
        clear(marker.sourceId)
    }
}
