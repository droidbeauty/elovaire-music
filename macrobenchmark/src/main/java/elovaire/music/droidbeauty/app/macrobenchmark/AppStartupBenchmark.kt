package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class AppStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = benchmarkIterations(),
            setupBlock = {
                grantMediaPermission()
                pressHome()
            },
        ) {
            startActivityAndWait()
            waitForAppVisible()
        }
    }

    @Test
    fun warmStartup() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(),
            setupBlock = {
                grantMediaPermission()
                pressHome()
            },
        ) {
            startActivityAndWait()
            waitForAppVisible()
        }
    }

    @Test
    fun coldStartupWithBaselineProfile() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require,
            ),
            startupMode = StartupMode.COLD,
            iterations = benchmarkIterations(),
            setupBlock = {
                grantMediaPermission()
                pressHome()
            },
        ) {
            startActivityAndWait()
            waitForAppVisible()
        }
    }

    @Test
    fun commonInteractionFrameTiming() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = diagnosticFrameMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(),
            setupBlock = {
                grantMediaPermission()
                pressHome()
                startActivityAndWait()
                waitForAppVisible()
            },
        ) {
            homeJourney()
            topLevelNavigationJourney()
            searchJourney()
            playerJourneyIfAvailable()
            routeOpenBackJourney()
        }
    }

    @Test
    fun commonInteractionFrameTimingWithBaselineProfile() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = diagnosticFrameMetrics(),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require,
            ),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(),
            setupBlock = {
                grantMediaPermission()
                pressHome()
                startActivityAndWait()
                waitForAppVisible()
            },
        ) {
            homeJourney()
            topLevelNavigationJourney()
            searchJourney()
            playerJourneyIfAvailable()
            routeOpenBackJourney()
        }
    }

    @Test
    fun repeatedNavigationMemory() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(MemoryUsageMetric(MemoryUsageMetric.Mode.Max)),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = {
                grantMediaPermission()
                pressHome()
                startActivityAndWait()
                waitForAppVisible()
            },
        ) {
            repeat(3) {
                topLevelNavigationJourney()
                routeOpenBackJourney()
            }
        }
    }

    private fun diagnosticFrameMetrics() = listOf(
        FrameTimingMetric(),
        TraceSectionMetric(
            sectionName = "route_change",
            mode = TraceSectionMetric.Mode.Count,
            label = "route_change_count",
        ),
    )
}
