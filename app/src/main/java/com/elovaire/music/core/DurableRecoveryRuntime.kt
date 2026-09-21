package elovaire.music.droidbeauty.app.core

import android.database.sqlite.SQLiteException
import android.util.Log
import elovaire.music.droidbeauty.app.data.library.network.NetworkCredentialStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkInventoryStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkLibrarySourceStore
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationJournal
import elovaire.music.droidbeauty.app.data.library.network.NetworkSourceMutationMarker
import elovaire.music.droidbeauty.app.data.library.network.recover
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationRecoveryResult
import java.security.GeneralSecurityException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

internal data class DurableRecoveryResult(
    val mediaMutationRecoverySucceeded: Boolean,
    val networkRecoverySucceeded: Boolean,
    val blockedSourceIds: Set<String>,
)

/** Owns the bounded, journal-backed recovery work required before durable startup is ready. */
internal class DurableRecoveryRuntime(
    private val mediaMutationJournal: MediaMutationJournal,
    private val networkSourceMutationJournal: NetworkSourceMutationJournal,
    private val networkSourceStore: NetworkLibrarySourceStore,
    private val networkCredentialStoreProvider: () -> NetworkCredentialStore,
    private val networkInventoryStore: NetworkInventoryStore,
    private val invalidateNetworkSourceRuntime: ((sourceId: String) -> Unit)?,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun recover(): DurableRecoveryResult {
        val mediaMutationRecoverySucceeded = recoverCriticalMediaMutations()
        var networkRecoverySucceeded = true
        val blockedSourceIds = try {
            networkSourceMutationJournal.recover(
                sourceStore = networkSourceStore,
                credentialStore = networkCredentialStoreProvider(),
                inventoryStore = networkInventoryStore,
                invalidateRuntime = invalidateNetworkSourceRuntime,
                perMarkerTimeoutMs = DURABLE_RECOVERY_TIMEOUT_MS,
            )
            emptySet()
        } catch (_: TimeoutCancellationException) {
            networkRecoverySucceeded = false
            pendingNetworkSourceIds()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            networkRecoverySucceeded = false
            Log.w(TAG, "Network source mutation recovery deferred", failure)
            pendingNetworkSourceIds()
        } catch (failure: IllegalStateException) {
            networkRecoverySucceeded = false
            Log.w(TAG, "Network source mutation recovery deferred", failure)
            pendingNetworkSourceIds()
        } catch (failure: SecurityException) {
            networkRecoverySucceeded = false
            Log.w(TAG, "Network source mutation recovery deferred", failure)
            pendingNetworkSourceIds()
        } catch (failure: GeneralSecurityException) {
            networkRecoverySucceeded = false
            Log.w(TAG, "Network source credential recovery deferred", failure)
            pendingNetworkSourceIds()
        } catch (failure: RuntimeException) {
            networkRecoverySucceeded = false
            Log.e(TAG, "Network source mutation recovery failed", failure)
            pendingNetworkSourceIds()
        }
        return DurableRecoveryResult(
            mediaMutationRecoverySucceeded = mediaMutationRecoverySucceeded,
            networkRecoverySucceeded = networkRecoverySucceeded,
            blockedSourceIds = blockedSourceIds,
        )
    }

    private fun pendingNetworkSourceIds(): Set<String> = networkSourceMutationJournal.pending()
        .mapTo(linkedSetOf(), NetworkSourceMutationMarker::sourceId)

    @Suppress("TooGenericExceptionCaught")
    private suspend fun recoverCriticalMediaMutations(): Boolean {
        return try {
            withTimeout(DURABLE_RECOVERY_TIMEOUT_MS) {
                mediaMutationJournal.recoverIncomplete() is MediaMutationRecoveryResult.Success
            }
        } catch (failure: TimeoutCancellationException) {
            Log.w(TAG, "Media mutation recovery timed out", failure)
            false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            Log.w(TAG, "Media mutation recovery deferred", failure)
            false
        } catch (failure: IllegalStateException) {
            Log.w(TAG, "Media mutation recovery deferred", failure)
            false
        } catch (failure: RuntimeException) {
            Log.e(TAG, "Media mutation recovery failed", failure)
            false
        }
    }

    private companion object {
        const val DURABLE_RECOVERY_TIMEOUT_MS = 15_000L
        const val TAG = "DurableRecovery"
    }
}
