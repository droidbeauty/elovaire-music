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
class FocusedInteractionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun topLevelNavigationFrameTiming() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = metrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            topLevelNavigationJourney()
        }
    }

    @Test
    fun searchFrameTiming() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = searchMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            searchJourney()
        }
    }

    @Test
    fun searchRapidInputFrameTiming() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = searchMetrics(),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.WARM,
            iterations = benchmarkIterations(defaultValue = 3),
            setupBlock = { prepareApp() },
        ) {
            searchRapidInputJourney()
        }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.prepareApp() {
        grantMediaPermission()
        pressHome()
        startActivityAndWait()
        waitForAppVisible()
    }

    private fun metrics() = listOf(
        FrameTimingMetric(),
        TraceSectionMetric(
            sectionName = "route_change",
            mode = TraceSectionMetric.Mode.Count,
            label = "route_change_count",
        ),
    )

    private fun searchMetrics() = metrics() + listOf(
        TraceSectionMetric(
            sectionName = "search_index_build",
            mode = TraceSectionMetric.Mode.Count,
            label = "search_index_build_count",
        ),
        TraceSectionMetric(
            sectionName = "search_index_build",
            mode = TraceSectionMetric.Mode.Sum,
            label = "search_index_build_sum_ms",
        ),
        TraceSectionMetric(
            sectionName = "search_query_preview",
            mode = TraceSectionMetric.Mode.Count,
            label = "search_query_preview_count",
        ),
        TraceSectionMetric(
            sectionName = "search_query_preview",
            mode = TraceSectionMetric.Mode.Sum,
            label = "search_query_preview_sum_ms",
        ),
        TraceSectionMetric(
            sectionName = "search_query_full",
            mode = TraceSectionMetric.Mode.Count,
            label = "search_query_full_count",
        ),
        TraceSectionMetric(
            sectionName = "search_query_full",
            mode = TraceSectionMetric.Mode.Sum,
            label = "search_query_full_sum_ms",
        ),
    )
}
