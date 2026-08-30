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
        TraceSectionMetric(
            sectionName = "route_change",
            mode = TraceSectionMetric.Mode.Sum,
            label = "route_change_duration_ms",
        ),
    ) + BACKEND_TRACE_SECTIONS.flatMap { section ->
        buildList {
            add(
                TraceSectionMetric(
                    sectionName = section,
                    mode = TraceSectionMetric.Mode.Count,
                    label = "trace_${section}_count",
                ),
            )
            if (section in DURATION_TRACE_SECTIONS) {
                add(
                    TraceSectionMetric(
                        sectionName = section,
                        mode = TraceSectionMetric.Mode.Sum,
                        label = "trace_${section}_duration_ms",
                    ),
                )
            }
        }
    }

    private companion object {
        val DURATION_TRACE_SECTIONS = setOf(
            "route_change",
            "library_refresh_scan",
            "library_prepare_content",
            "library_diff",
            "library_snapshot_persist",
            "library_room_index_commit",
            "mediastore_discovery",
            "mediastore_query_full",
            "mediastore_query_delta",
            "mediastore_metadata_enrichment",
            "network_source_list",
            "network_metadata_enrichment",
            "artwork_remote_fetch",
            "artwork_decode",
        )
        val BACKEND_TRACE_SECTIONS = listOf(
            "library_refresh_scan",
            "library_prepare_content",
            "library_diff",
            "library_snapshot_persist",
            "library_room_index_commit",
            "mediastore_discovery",
            "mediastore_query_full",
            "mediastore_query_delta",
            "mediastore_metadata_enrichment",
            "network_source_list",
            "network_metadata_enrichment",
            "lyrics_cache_read",
            "lyrics_cache_write",
            "settings_backup_checkpoint",
            "user_recovery_checkpoint",
            "artwork_remote_fetch",
            "artwork_decode",
            "artwork_disk_commit",
        )
    }
}
