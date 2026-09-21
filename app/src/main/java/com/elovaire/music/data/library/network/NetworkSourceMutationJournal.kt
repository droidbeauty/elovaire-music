package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import elovaire.music.droidbeauty.app.core.allowStrictModeDiskReads
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlinx.coroutines.withTimeout

internal enum class NetworkSourceMutationKind {
    Save,
    Remove,
}

/** Durable phases use stable wire values so recovery can be audited and repeated safely. */
internal enum class NetworkSourceMutationPhase(val wireValue: String) {
    Prepared("prepared"),
    CredentialPersisted("credential_persisted"),
    SourcePersisted("source_persisted"),
    CredentialsCleaned("credentials_cleaned"),
    InventoryInvalidated("inventory_invalidated"),
    RuntimeInvalidated("runtime_invalidated"),
    SourceRemoved("source_removed"),
    InventoryRemoved("inventory_removed"),
}

internal data class NetworkSourceMutationMarker(
    val sourceId: String,
    val kind: NetworkSourceMutationKind,
    val previousCredentialKey: String?,
    val newCredentialKey: String?,
    val previousLocationFingerprint: String?,
    val newLocationFingerprint: String?,
    val phase: NetworkSourceMutationPhase,
)

internal class NetworkSourceMutationJournalCorruptionException(
    message: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

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
            phase = NetworkSourceMutationPhase.Prepared,
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
            phase = NetworkSourceMutationPhase.Prepared,
        ),
    )

    fun markPhase(sourceId: String, phase: NetworkSourceMutationPhase) {
        synchronized(lock) {
            val markers = readLocked()
            val current = markers.firstOrNull { it.sourceId == sourceId } ?: return
            if (current.phase == phase) return
            check(isValidNetworkSourceMutationPhaseTransition(current.kind, current.phase, phase)) {
                "Invalid ${current.kind.name} network source mutation transition: " +
                    "${current.phase.wireValue} -> ${phase.wireValue}"
            }
            writeLocked(markers.map { marker ->
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
                    .put("phase", marker.phase.wireValue),
            )
        }
        check(preferences.edit().putString(KEY_MARKERS, array.toString()).commit()) {
            "Unable to persist network source mutation marker"
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun readLocked(): List<NetworkSourceMutationMarker> {
        val raw = preferences.getString(KEY_MARKERS, "[]") ?: "[]"
        val array = try {
            JSONArray(raw)
        } catch (failure: JSONException) {
            throw NetworkSourceMutationJournalCorruptionException(
                "Network source mutation journal is unreadable.",
                failure,
            )
        }
        if (array.length() > MAX_MARKERS) {
            throw NetworkSourceMutationJournalCorruptionException(
                "Network source mutation journal exceeds its safety bound.",
            )
        }
        return buildList {
            repeat(array.length()) { index ->
                val item = array.optJSONObject(index)
                    ?: throw NetworkSourceMutationJournalCorruptionException(
                        "Network source mutation journal contains an invalid marker.",
                    )
                val kind = try {
                    NetworkSourceMutationKind.valueOf(item.optString("kind"))
                } catch (failure: RuntimeException) {
                    throw NetworkSourceMutationJournalCorruptionException(
                        "Network source mutation journal contains an unknown mutation kind.",
                        failure,
                    )
                }
                val sourceId = item.optString("sourceId").trim().takeIf(String::isNotBlank)
                    ?: throw NetworkSourceMutationJournalCorruptionException(
                        "Network source mutation journal contains a marker without a source.",
                    )
                val phaseValue = item.optString("phase", NetworkSourceMutationPhase.Prepared.wireValue)
                val phase = NetworkSourceMutationPhase.entries.firstOrNull { phase ->
                    phase.wireValue == phaseValue || phase.name == phaseValue
                } ?: throw NetworkSourceMutationJournalCorruptionException(
                    "Network source mutation journal contains an unknown phase.",
                )
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
                        phase = phase,
                    ),
                )
            }
        }
    }

    private companion object {
        const val PREFERENCES = "network_source_mutations_v1"
        const val KEY_MARKERS = "markers"
        const val MAX_MARKERS = 32
    }
}

internal fun isValidNetworkSourceMutationPhaseTransition(
    kind: NetworkSourceMutationKind,
    current: NetworkSourceMutationPhase,
    next: NetworkSourceMutationPhase,
): Boolean {
    val allowed = when (kind) {
        NetworkSourceMutationKind.Save -> when (current) {
            NetworkSourceMutationPhase.Prepared -> setOf(NetworkSourceMutationPhase.CredentialPersisted)
            NetworkSourceMutationPhase.CredentialPersisted -> setOf(NetworkSourceMutationPhase.SourcePersisted)
            NetworkSourceMutationPhase.SourcePersisted -> setOf(NetworkSourceMutationPhase.CredentialsCleaned)
            NetworkSourceMutationPhase.CredentialsCleaned -> setOf(NetworkSourceMutationPhase.InventoryInvalidated)
            NetworkSourceMutationPhase.InventoryInvalidated -> setOf(NetworkSourceMutationPhase.RuntimeInvalidated)
            NetworkSourceMutationPhase.RuntimeInvalidated,
            NetworkSourceMutationPhase.SourceRemoved,
            NetworkSourceMutationPhase.InventoryRemoved,
            -> emptySet()
        }
        NetworkSourceMutationKind.Remove -> when (current) {
            NetworkSourceMutationPhase.Prepared -> setOf(NetworkSourceMutationPhase.RuntimeInvalidated)
            NetworkSourceMutationPhase.RuntimeInvalidated -> setOf(NetworkSourceMutationPhase.SourceRemoved)
            NetworkSourceMutationPhase.SourceRemoved -> setOf(NetworkSourceMutationPhase.InventoryRemoved)
            NetworkSourceMutationPhase.InventoryRemoved,
            NetworkSourceMutationPhase.CredentialPersisted,
            NetworkSourceMutationPhase.SourcePersisted,
            NetworkSourceMutationPhase.CredentialsCleaned,
            NetworkSourceMutationPhase.InventoryInvalidated,
            -> emptySet()
        }
    }
    return next in allowed
}

internal suspend fun NetworkSourceMutationJournal.recover(
    sourceStore: NetworkLibrarySourceStore,
    credentialStore: NetworkCredentialStore,
    inventoryStore: NetworkInventoryStore,
    invalidateRuntime: ((sourceId: String) -> Unit)? = null,
    perMarkerTimeoutMs: Long = 15_000L,
) {
    pending().forEach { marker ->
        withTimeout(perMarkerTimeoutMs) {
            // Each operation below is idempotent. If cancellation interrupts this block,
            // the marker remains durable and the next recovery pass repeats the same
            // reconciliation from the durable source state.
            invalidateRuntime?.invoke(marker.sourceId)
            when (marker.kind) {
                NetworkSourceMutationKind.Save -> {
                    val current = sourceStore.sources.value.firstOrNull { it.id == marker.sourceId }
                    if (current?.credentialKey == marker.newCredentialKey) {
                        val newCredentialKey = marker.newCredentialKey
                            ?: error("A committed network source save has no credential key")
                        check(
                            credentialStore.read(marker.sourceId, newCredentialKey) is
                                NetworkCredentialReadResult.Available,
                        ) { "Committed network source credentials are unavailable" }
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
                    // Removal is the durable user intent. Completing it is safe even when
                    // process death occurred before the source preference was removed.
                    sourceStore.remove(marker.sourceId)
                    marker.previousCredentialKey?.let(credentialStore::remove)
                    inventoryStore.remove(marker.sourceId)
                }
            }
            clear(marker.sourceId)
        }
    }
}
