enum class BenchmarkRegressionClassification {
    PASS,
    SUSPICIOUS,
    HARD_REGRESSION,
}

internal fun classifyBenchmarkRegression(
    metric: String,
    baseline: Double,
    current: Double,
): BenchmarkRegressionClassification {
    if (metric.endsWith("frameOverrunMs", ignoreCase = true)) {
        return classifyFrameOverrunRegression(baseline, current)
    }
    if (baseline <= 0.0) return BenchmarkRegressionClassification.PASS

    val deltaRatio = (current - baseline) / baseline
    return when {
        deltaRatio >= HARD_REGRESSION_RATIO -> BenchmarkRegressionClassification.HARD_REGRESSION
        deltaRatio >= SUSPICIOUS_RATIO -> BenchmarkRegressionClassification.SUSPICIOUS
        else -> BenchmarkRegressionClassification.PASS
    }
}

internal fun isUsableBenchmarkMetric(metric: String, value: Double): Boolean {
    if (!value.isFinite()) return false
    // Frame timing reports encode headroom as a negative overrun. Other metrics recognized by
    // the evaluator represent counts, durations, sizes, or costs and cannot be negative.
    return metric.endsWith("frameOverrunMs", ignoreCase = true) || value >= 0.0
}

private fun classifyFrameOverrunRegression(
    baseline: Double,
    current: Double,
): BenchmarkRegressionClassification {
    val deltaMs = current - baseline
    return when {
        baseline <= 0.0 && current > 0.0 -> BenchmarkRegressionClassification.HARD_REGRESSION
        baseline < 0.0 && current < 0.0 && deltaMs >= FRAME_OVERRUN_HARD_DELTA_MS ->
            BenchmarkRegressionClassification.HARD_REGRESSION
        baseline < 0.0 && current < 0.0 && deltaMs >= FRAME_OVERRUN_SUSPICIOUS_DELTA_MS ->
            BenchmarkRegressionClassification.SUSPICIOUS
        baseline > 0.0 && deltaMs / baseline >= HARD_REGRESSION_RATIO ->
            BenchmarkRegressionClassification.HARD_REGRESSION
        baseline > 0.0 && deltaMs / baseline >= SUSPICIOUS_RATIO ->
            BenchmarkRegressionClassification.SUSPICIOUS
        else -> BenchmarkRegressionClassification.PASS
    }
}

private const val SUSPICIOUS_RATIO = 0.15
private const val HARD_REGRESSION_RATIO = 0.30
private const val FRAME_OVERRUN_SUSPICIOUS_DELTA_MS = 1.0
private const val FRAME_OVERRUN_HARD_DELTA_MS = 4.0
