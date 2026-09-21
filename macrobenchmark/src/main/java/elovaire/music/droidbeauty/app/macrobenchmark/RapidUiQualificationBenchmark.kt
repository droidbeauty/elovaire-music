package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
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
class RapidUiQualificationBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun topLevelRouteStormCadenceSweep() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = rapidMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            RAPID_INPUT_CADENCES_MS.forEach { cadenceMs ->
                rapidTopLevelRouteStorm(cadenceMs)
            }
        }
    }

    @Test
    fun openBackStormCadenceSweep() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = rapidMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            RAPID_INPUT_CADENCES_MS.forEach { cadenceMs ->
                rapidOpenBackStorm(cadenceMs)
            }
        }
    }

    @Test
    fun searchAndPlayerInterruptionJourney() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = rapidMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            searchRapidInputJourney()
            rapidTopLevelRouteStorm(64L)
            playerJourneyIfAvailable()
            assertAppProcessIsAlive()
        }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.prepareApp() {
        grantMediaPermission()
        pressHome()
        startActivityAndWait()
        waitForAppVisible()
    }

    private fun rapidMetrics() = listOf(
        FrameTimingMetric(),
        TraceSectionMetric(
            sectionName = "route_change",
            mode = TraceSectionMetric.Mode.Count,
            label = "route_change_count",
        ),
    )
}
