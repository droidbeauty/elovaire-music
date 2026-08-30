import groovy.json.JsonSlurper
import java.io.File
import java.util.Locale
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

abstract class BenchmarkRegressionEvaluatorTask : DefaultTask() {
    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val currentResultsDir: DirectoryProperty

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineResultsDir: DirectoryProperty

    @TaskAction
    fun evaluate() {
        if (!baselineResultsDir.isPresent) {
            logger.lifecycle("Benchmark regression evaluation skipped: set -Papp.benchmarkBaselineDir for a same-device baseline.")
            return
        }

        val currentFiles = jsonFiles(currentResultsDir.asFile.get())
        val baselineFiles = jsonFiles(baselineResultsDir.asFile.get())
        if (currentFiles.isEmpty() || baselineFiles.isEmpty()) {
            logger.lifecycle("Benchmark regression evaluation skipped: structured JSON results are not available yet.")
            return
        }

        val baselineByKey = baselineFiles
            .flatMap(::readSamples)
            .associateBy { it.key }
        val observations = currentFiles
            .flatMap(::readSamples)
            .mapNotNull { current ->
                val baseline = baselineByKey[current.key] ?: return@mapNotNull null
                if (current.environment.isBlank() || baseline.environment.isBlank()) return@mapNotNull null
                if (current.environment != baseline.environment) return@mapNotNull null
                current to baseline
            }
        if (observations.isEmpty()) {
            logger.lifecycle("Benchmark regression evaluation skipped: no compatible same-device samples matched.")
            return
        }

        val hardRegressions = mutableListOf<String>()
        observations.forEach { (current, baseline) ->
            val classification = classifyBenchmarkRegression(
                metric = current.metric,
                baseline = baseline.value,
                current = current.value,
            ).name
            val delta = regressionDelta(
                metric = current.metric,
                baseline = baseline.value,
                current = current.value,
            )
            if (classification != "PASS") {
                logger.lifecycle(
                    "scenario=${current.scenario} metric=${current.metric} " +
                        "baseline=${format(baseline.value)} current=${format(current.value)} " +
                        "delta=${formatDelta(current.metric, delta)} classification=$classification",
                )
            }
            if (classification == "HARD_REGRESSION") {
                hardRegressions +=
                    "${current.scenario}/${current.metric} ${formatDelta(current.metric, delta)} " +
                        "(${format(baseline.value)} -> ${format(current.value)})"
            }
        }
        check(hardRegressions.isEmpty()) {
            "Benchmark hard regression detected (P2):\n${hardRegressions.joinToString("\n")}"
        }
    }

    private fun jsonFiles(root: File): List<File> {
        return if (root.isDirectory) root.walkTopDown().filter { it.isFile && it.extension == "json" }.toList() else emptyList()
    }

    private fun readSamples(file: File): List<Sample> {
        val parsed = runCatching { JsonSlurper().parse(file) }.getOrNull() ?: return emptyList()
        val environment = environmentSignature(parsed)
        val samples = mutableListOf<Sample>()
        collectSamples(parsed, file.nameWithoutExtension, "", environment, samples)
        return samples
    }

    private fun collectSamples(
        value: Any?,
        scenario: String,
        path: String,
        environment: String,
        samples: MutableList<Sample>,
    ) {
        when (value) {
            is Map<*, *> -> value.forEach { (rawKey, child) ->
                val key = rawKey?.toString().orEmpty()
                val childScenario = if (key.equals("name", ignoreCase = true) && child is String) child else scenario
                val childPath = if (path.isBlank()) key else "$path.$key"
                if (child is Number && isMetricKey(key)) {
                    samples += Sample(
                        key = "$childScenario|$childPath",
                        scenario = childScenario,
                        metric = childPath,
                        value = child.toDouble(),
                        environment = environment,
                    )
                } else {
                    collectSamples(child, childScenario, childPath, environment, samples)
                }
            }
            is List<*> -> value.forEachIndexed { index, child ->
                collectSamples(child, scenario, "$path[$index]", environment, samples)
            }
        }
    }

    private fun environmentSignature(value: Any?): String {
        val entries = mutableListOf<String>()
        collectEnvironment(value, entries)
        return entries.sorted().joinToString("|")
    }

    private fun collectEnvironment(value: Any?, entries: MutableList<String>) {
        when (value) {
            is Map<*, *> -> value.forEach { (rawKey, child) ->
                val key = rawKey?.toString().orEmpty()
                if (key.lowercase(Locale.US) in ENVIRONMENT_KEYS && child !is Map<*, *> && child !is List<*>) {
                    entries += "$key=$child"
                } else {
                    collectEnvironment(child, entries)
                }
            }
            is List<*> -> value.forEach { collectEnvironment(it, entries) }
        }
    }

    private fun isMetricKey(key: String): Boolean {
        val normalized = key.lowercase(Locale.US)
        return normalized.contains("time") ||
            normalized.contains("frame") ||
            normalized.contains("jank") ||
            normalized.contains("memory") ||
            normalized.contains("heap") ||
            normalized.contains("rss") ||
            normalized.contains("trace") ||
            normalized.contains("power") ||
            normalized.contains("battery")
    }

    private fun format(value: Double): String = "%.2f".format(Locale.US, value)

    private fun formatPercent(value: Double): String = "%.1f%%".format(Locale.US, value * 100.0)

    private fun regressionDelta(metric: String, baseline: Double, current: Double): Double {
        return if (metric.endsWith("frameOverrunMs", ignoreCase = true)) {
            current - baseline
        } else if (baseline != 0.0) {
            (current - baseline) / baseline
        } else {
            current - baseline
        }
    }

    private fun formatDelta(metric: String, delta: Double): String {
        return if (metric.endsWith("frameOverrunMs", ignoreCase = true)) {
            "${if (delta >= 0.0) "+" else ""}${format(delta)}ms"
        } else {
            formatPercent(delta)
        }
    }

    private data class Sample(
        val key: String,
        val scenario: String,
        val metric: String,
        val value: Double,
        val environment: String,
    )

    private companion object {
        val ENVIRONMENT_KEYS = setOf(
            "device",
            "deviceid",
            "model",
            "sdk",
            "sdkversion",
            "androidversion",
            "refreshrate",
            "compilationmode",
            "buildtype",
        )
    }
}
