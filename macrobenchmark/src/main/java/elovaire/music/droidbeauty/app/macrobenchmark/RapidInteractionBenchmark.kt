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
class RapidInteractionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun bottomNavigationBurstsWithoutIdleSynchronization() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(
                FrameTimingMetric(),
                TraceSectionMetric(
                    sectionName = "route_change",
                    mode = TraceSectionMetric.Mode.Count,
                    label = "route_change_count",
                ),
            ),
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
            listOf(150L, 100L, 75L, 50L, 32L).forEach { intervalMs ->
                rapidBottomNavigationBurst(intervalMs)
            }
            uiDevice.waitForIdle()
        }
    }

    @Test
    fun backBurstsWithoutIdleSynchronization() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
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
            rapidBottomNavigationBurst(100L)
            burstPressBack(count = 3, interInputDelayMs = 50L)
            uiDevice.waitForIdle()
        }
    }
}
