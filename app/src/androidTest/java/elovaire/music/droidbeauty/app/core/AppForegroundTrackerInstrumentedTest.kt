package elovaire.music.droidbeauty.app.core

import android.app.Activity
import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppForegroundTrackerInstrumentedTest {
    @Test
    fun duplicateLifecycleCallbacksDoNotCorruptForegroundState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val application = instrumentation.targetContext.applicationContext as Application
            val tracker = AppForegroundTracker(application)
            val first = Activity()
            val second = Activity()

            try {
                tracker.onActivityStarted(first)
                tracker.onActivityStarted(first)
                assertTrue(tracker.isForeground.value)

                tracker.onActivityStopped(first)
                assertFalse(tracker.isForeground.value)
                tracker.onActivityStopped(first)
                assertFalse(tracker.isForeground.value)

                tracker.onActivityStarted(first)
                tracker.onActivityStarted(second)
                tracker.onActivityStopped(first)
                assertTrue(tracker.isForeground.value)
                tracker.onActivityStopped(second)
                assertFalse(tracker.isForeground.value)
            } finally {
                tracker.close()
            }
        }
    }

    @Test
    fun callbacksAfterCloseAreIgnored() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val application = instrumentation.targetContext.applicationContext as Application
            val tracker = AppForegroundTracker(application)
            val activity = Activity()

            tracker.close()
            tracker.onActivityStarted(activity)

            assertFalse(tracker.isForeground.value)
        }
    }
}
