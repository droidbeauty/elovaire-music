import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenchmarkRegressionPolicyTest {
    @Test
    fun environmentSignatureIsStableAndIncludesHardwareIdentity() {
        val first = benchmarkEnvironmentSignature(
            mapOf(
                "model" to "Pixel",
                "fingerprint" to "build-a",
                "abi" to "arm64-v8a",
                "metrics" to listOf(mapOf("timeMs" to 10)),
            ),
        )
        val reordered = benchmarkEnvironmentSignature(
            mapOf("abi" to "arm64-v8a", "fingerprint" to "build-a", "model" to "Pixel"),
        )

        assertEquals("abi=arm64-v8a|fingerprint=build-a|model=Pixel", first.substringBefore("|metrics"))
        assertEquals(first.substringBefore("|metrics"), reordered)
        assertTrue(benchmarkEnvironmentsCompatible(first, reordered))
        assertFalse(benchmarkEnvironmentsCompatible(first, "model=Pixel|fingerprint=build-b"))
        assertFalse(benchmarkEnvironmentsCompatible("", reordered))
    }

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

    @Test
    fun invalidMetricValuesAreRejectedWithoutRejectingFrameHeadroom() {
        assertFalse(isUsableBenchmarkMetric("timeToInitialDisplayMs", -1.0))
        assertFalse(isUsableBenchmarkMetric("memoryHeapSizeMaxKb", Double.NaN))
        assertFalse(isUsableBenchmarkMetric("rssBytes", Double.POSITIVE_INFINITY))
        assertTrue(isUsableBenchmarkMetric("frameOverrunMs", -8.0))
        assertTrue(isUsableBenchmarkMetric("traceCount", 0.0))
    }
}
