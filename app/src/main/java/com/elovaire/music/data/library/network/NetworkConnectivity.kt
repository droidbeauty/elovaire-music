package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

internal data class NetworkConnectivitySignal(
    val networkAvailable: Boolean,
    val networkBlocked: Boolean,
    val localNetworkAccessAllowed: Boolean,
)

internal data class NetworkConnectivityState(
    val networkAvailable: Boolean = false,
    val networkBlocked: Boolean = false,
    val localNetworkAccessAllowed: Boolean = true,
    val generation: Long = 0L,
)

internal object NetworkConnectivityReducer {
    fun reduce(
        current: NetworkConnectivityState,
        signal: NetworkConnectivitySignal,
    ): NetworkConnectivityState {
        if (
            current.networkAvailable == signal.networkAvailable &&
            current.networkBlocked == signal.networkBlocked &&
            current.localNetworkAccessAllowed == signal.localNetworkAccessAllowed
        ) {
            return current
        }
        return NetworkConnectivityState(
            networkAvailable = signal.networkAvailable,
            networkBlocked = signal.networkBlocked,
            localNetworkAccessAllowed = signal.localNetworkAccessAllowed,
            generation = current.generation + 1L,
        )
    }
}

internal class NetworkConnectivityObserver(
    context: Context,
    private val localNetworkAccessAllowed: () -> Boolean,
    private val onStateChanged: (NetworkConnectivityState) -> Unit,
) : Closeable {
    private val connectivityManager = context.applicationContext
        .getSystemService(ConnectivityManager::class.java)
    private val lifecycle = AtomicBoolean(false)
    private val lock = Any()
    private val activeNetworks = LinkedHashSet<Network>()
    private val blockedNetworks = HashSet<Network>()
    @Volatile
    private var state = NetworkConnectivityState()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            synchronized(lock) { activeNetworks.add(network) }
            publish()
        }

        override fun onLost(network: Network) {
            synchronized(lock) {
                activeNetworks.remove(network)
                blockedNetworks.remove(network)
            }
            publish()
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            synchronized(lock) {
                if (blocked) blockedNetworks.add(network) else blockedNetworks.remove(network)
            }
            publish()
        }
    }

    @Suppress("SwallowedException")
    fun start() {
        if (!lifecycle.compareAndSet(false, true)) return
        try {
            connectivityManager?.registerDefaultNetworkCallback(callback)
            publish()
        } catch (failure: SecurityException) {
            lifecycle.set(false)
        } catch (failure: IllegalArgumentException) {
            lifecycle.set(false)
        }
    }

    fun syncPermission() {
        publish()
    }

    fun currentState(): NetworkConnectivityState = state

    override fun close() {
        if (!lifecycle.compareAndSet(true, false)) return
        connectivityManager?.unregisterNetworkCallback(callback)
        synchronized(lock) {
            activeNetworks.clear()
            blockedNetworks.clear()
        }
    }

    private fun publish() {
        val signal = synchronized(lock) {
            NetworkConnectivitySignal(
                networkAvailable = activeNetworks.isNotEmpty(),
                networkBlocked = activeNetworks.isNotEmpty() &&
                    activeNetworks.all(blockedNetworks::contains),
                localNetworkAccessAllowed = localNetworkAccessAllowed(),
            )
        }
        val updated = synchronized(lock) {
            val next = NetworkConnectivityReducer.reduce(state, signal)
            if (next == state) return@synchronized null
            state = next
            next
        }
        updated?.let(onStateChanged)
    }
}
