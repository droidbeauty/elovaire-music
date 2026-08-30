import org.junit.Assert.assertEquals
import org.junit.Test

class BenchmarkRegressionPolicyTest {
    @Test
    fun signedFrameOverrunHandlesNegativeHealthyMargins() {
        assertEquals(
            BenchmarkRegressionClassification.PASS,
            classifyBenchmarkRegression("frameOverrunMs", -8.0, -10.0),
        )
        assertEquals(
            BenchmarkRegressionClassification.SUSPICIOUS,
            classifyBenchmarkRegression("frameOverrunMs", -8.0, -5.0),
        )
        assertEquals(
            BenchmarkRegressionClassification.HARD_REGRESSION,
            classifyBenchmarkRegression("frameOverrunMs", -8.0, 1.0),
        )
    }

    @Test
    fun signedFrameOverrunTreatsZeroCrossingAsHardRegression() {
        assertEquals(
            BenchmarkRegressionClassification.PASS,
            classifyBenchmarkRegression("frameOverrunMs", 0.0, -1.0),
        )
        assertEquals(
            BenchmarkRegressionClassification.HARD_REGRESSION,
            classifyBenchmarkRegression("frameOverrunMs", 0.0, 3.0),
        )
    }

    @Test
    fun positiveFrameOverrunStillUsesRatio() {
        assertEquals(
            BenchmarkRegressionClassification.HARD_REGRESSION,
            classifyBenchmarkRegression("frameOverrunMs", 2.0, 5.0),
        )
    }

    @Test
    fun ordinaryMetricsKeepExistingPositiveBaselinePolicy() {
        assertEquals(
            BenchmarkRegressionClassification.HARD_REGRESSION,
            classifyBenchmarkRegression("timeToInitialDisplayMs", 200.0, 260.0),
        )
        assertEquals(
            BenchmarkRegressionClassification.PASS,
            classifyBenchmarkRegression("memoryHeapSizeMaxKb", 0.0, 10.0),
        )
    }
}
