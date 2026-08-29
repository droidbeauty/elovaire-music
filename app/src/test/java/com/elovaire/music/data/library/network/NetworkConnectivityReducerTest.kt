package elovaire.music.droidbeauty.app.data.library.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkConnectivityReducerTest {
    @Test
    fun generationChangesOnlyForMeaningfulSignals() {
        val available = NetworkConnectivitySignal(
            networkAvailable = true,
            networkBlocked = false,
            localNetworkAccessAllowed = true,
        )
        val same = NetworkConnectivityReducer.reduce(NetworkConnectivityState(), available)
        val unchanged = NetworkConnectivityReducer.reduce(same, available)
        val blocked = NetworkConnectivityReducer.reduce(
            unchanged,
            available.copy(networkBlocked = true),
        )

        assertEquals(1L, same.generation)
        assertEquals(same, unchanged)
        assertEquals(2L, blocked.generation)
    }

    @Test
    fun permissionRevocationIsDistinctFromTransportLoss() {
        val state = NetworkConnectivityState(
            networkAvailable = true,
            localNetworkAccessAllowed = true,
        )
        val revoked = NetworkConnectivityReducer.reduce(
            state,
            NetworkConnectivitySignal(
                networkAvailable = true,
                networkBlocked = false,
                localNetworkAccessAllowed = false,
            ),
        )

        assertEquals(1L, revoked.generation)
        assertEquals(false, revoked.localNetworkAccessAllowed)
        assertEquals(true, revoked.networkAvailable)
    }
}
