package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
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
    fun commonInteractionFrameTiming() {
        assumeMacrobenchmarksEnabled()
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
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
}
