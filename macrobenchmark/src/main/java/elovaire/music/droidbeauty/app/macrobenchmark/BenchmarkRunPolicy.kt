package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue

internal fun assumeMacrobenchmarksEnabled() {
    val enabled = InstrumentationRegistry.getArguments()
        .getString("elovaire.runBenchmarks")
        .toBoolean()
    assumeTrue(enabled)
}

internal fun benchmarkIterations(defaultValue: Int = 5): Int =
    InstrumentationRegistry.getArguments()
        .getString("elovaire.benchmarkIterations")
        ?.toIntOrNull()
        ?.coerceAtLeast(1)
        ?: defaultValue

internal fun baselineProfileMaxIterations(): Int = benchmarkIterations(defaultValue = 3)

internal fun baselineProfileStableIterations(): Int =
    InstrumentationRegistry.getArguments()
        .getString("elovaire.baselineProfileStableIterations")
        ?.toIntOrNull()
        ?.coerceAtLeast(1)
        ?: 2
