package elovaire.music.droidbeauty.app.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        assumeMacrobenchmarksEnabled()
        baselineProfileRule.collect(
            packageName = TARGET_PACKAGE,
            maxIterations = 1,
            stableIterations = 1,
        ) {
            pressHome()
            startActivityAndWait()
            waitForAppVisible()
            homeJourney()
            topLevelNavigationJourney()
            searchJourney()
            playerJourneyIfAvailable()
            routeOpenBackJourney()
        }
    }
}
