package elovaire.music.droidbeauty.app.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import elovaire.music.droidbeauty.app.core.backend.BackendResourceKind
import elovaire.music.droidbeauty.app.core.backend.BackendResourceRegistry
import java.io.Closeable
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AppForegroundTracker(
    application: Application,
) : Application.ActivityLifecycleCallbacks, Closeable {
    private val application = application
    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()
    private val startedActivities = Collections.newSetFromMap(IdentityHashMap<Activity, Boolean>())
    private val closed = AtomicBoolean(false)
    private val callbackResource: Closeable

    init {
        application.registerActivityLifecycleCallbacks(this)
        callbackResource = BackendResourceRegistry.acquire(BackendResourceKind.ActiveRegisteredCallback)
    }

    override fun onActivityStarted(activity: Activity) {
        if (closed.get() || !startedActivities.add(activity)) return
        if (startedActivities.size == 1) {
            _isForeground.value = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (closed.get() || !startedActivities.remove(activity)) return
        if (startedActivities.isEmpty()) {
            _isForeground.value = false
        }
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (closed.get() || !startedActivities.remove(activity)) return
        if (startedActivities.isEmpty()) {
            _isForeground.value = false
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        application.unregisterActivityLifecycleCallbacks(this)
        startedActivities.clear()
        _isForeground.value = false
        callbackResource.close()
    }
}
