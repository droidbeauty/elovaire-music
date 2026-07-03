package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue

internal fun assumeMacrobenchmarksEnabled() {
    val enabled = InstrumentationRegistry.getArguments()
        .getString("elovaire.runBenchmarks")
        .toBoolean()
    assumeTrue(enabled)
}
