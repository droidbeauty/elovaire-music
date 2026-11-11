import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class BenchmarkMetricQualityCheckTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resultsDir: DirectoryProperty

    @get:Input
    abstract val selectedBenchmarkTest: Property<String>

    @TaskAction
    fun verify() {
        val resultFiles = resultsDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .toList()
        check(resultFiles.isNotEmpty()) { "Macrobenchmark produced no structured result JSON." }

        val benchmarks = resultFiles.flatMap { file ->
            val root = runCatching { JsonSlurper().parse(file) as? Map<*, *> }.getOrElse { failure ->
                error("Unable to parse Macrobenchmark result ${file.absolutePath}: ${failure.message}")
            }
            (root?.get("benchmarks") as? List<*>).orEmpty().mapNotNull { it as? Map<*, *> }
        }
        check(benchmarks.isNotEmpty()) { "Macrobenchmark result JSON contains no benchmark scenarios." }

        val selected = selectedBenchmarkTest.get().substringAfterLast('#', "").trim()
        val expectsFrameMetrics = selected.isBlank() || FRAME_BENCHMARKS.any(selected::contains)
        val frameBenchmarks = benchmarks.filter { it["metrics"] is Map<*, *> &&
            (it["metrics"] as Map<*, *>).containsKey("gfxFrameTotalCount")
        }
        if (expectsFrameMetrics) {
            check(frameBenchmarks.isNotEmpty()) {
                "Interactive Macrobenchmark scenarios produced no frame-count metrics."
            }
        }
        frameBenchmarks.forEach { benchmark ->
            val name = benchmark["name"]?.toString() ?: "unknown"
            val metrics = benchmark["metrics"] as Map<*, *>
            val requiredMetrics = listOf(
                "gfxFrameJankPercent",
                "gfxFrameTime50thPercentileMs",
                "gfxFrameTime90thPercentileMs",
                "gfxFrameTime95thPercentileMs",
                "gfxFrameTime99thPercentileMs",
                "gfxFrameTotalCount",
            )
            requiredMetrics.forEach { key ->
                val median = ((metrics[key] as? Map<*, *>)?.get("median") as? Number)?.toDouble()
                check(median != null && median.isFinite()) {
                    "$name is missing a usable $key measurement from FrameTimingGfxInfoMetric."
                }
                check(median > 0.0 || key == "gfxFrameJankPercent") {
                    "$name reported a non-positive $key measurement."
                }
                if (key == "gfxFrameJankPercent") {
                    check(median in 0.0..100.0) { "$name reported an invalid jank percentage: $median." }
                }
            }
        }
        logger.lifecycle(
            "Validated ${benchmarks.size} Macrobenchmark scenarios and " +
                "${frameBenchmarks.size} frame-timing measurements.",
        )
    }

    private companion object {
        val FRAME_BENCHMARKS = listOf("FrameTiming", "Bursts", "CadenceSweep", "Journey", "NavigationMemory")
    }
}
