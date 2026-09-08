import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PhysicalDeviceQualificationTask : DefaultTask() {
    @get:Input
    abstract val adbExecutable: Property<String>

    @get:Input
    abstract val serial: Property<String>

    @get:Input
    abstract val benchmarkVariant: Property<String>

    @get:Input
    abstract val compilationMode: Property<String>

    @get:Input
    abstract val iterations: Property<String>

    @get:Input
    abstract val fixtureProfile: Property<String>

    @TaskAction
    fun qualify() {
        val selectedSerial = serial.get().trim()
        check(selectedSerial.isNotEmpty()) {
            "Physical performance qualification requires ANDROID_SERIAL."
        }
        check(!selectedSerial.contains(Regex("\\s"))) {
            "ANDROID_SERIAL must contain one device serial without whitespace."
        }
        val devices = runAdb("devices", "-l")
        val selectedDevices = parseAdbDevices(devices).filter { it.serial == selectedSerial }
        check(selectedDevices.size == 1 && selectedDevices.single().state == "device") {
            "ANDROID_SERIAL=$selectedSerial is not the only matching responsive device entry."
        }

        val properties = runAdb("-s", selectedSerial, "shell", "getprop")
            .lineSequence()
            .mapNotNull(::parseAdbProperty)
            .toMap()
        check(isPhysicalDeviceProperties(properties)) {
            "Performance qualification requires a physical device; $selectedSerial appears to be an emulator."
        }

        val battery = runOptionalAdb(selectedSerial, "shell", "dumpsys", "battery")
        val thermal = runOptionalAdb(selectedSerial, "shell", "dumpsys", "thermalservice")
        val storage = runOptionalAdb(selectedSerial, "shell", "df", "-k", "/data")
        val display = runOptionalAdb(selectedSerial, "shell", "dumpsys", "display")
        logger.lifecycle(
            "physical_benchmark_device serial=$selectedSerial " +
                "model=${properties["ro.product.model"].orUnknown()} " +
                "fingerprint=${properties["ro.build.fingerprint"].orUnknown()} " +
                "api=${properties["ro.build.version.sdk"].orUnknown()} " +
                "abi=${(properties["ro.product.cpu.abilist"] ?: properties["ro.product.cpu.abi"]).orUnknown()} " +
                "refresh_rate=${parseRefreshRate(display)} " +
                "battery=${parseBatteryLevel(battery)} " +
                "charging=${parseChargingState(battery)} " +
                "thermal=${parseThermalState(thermal)} " +
                "storage_available_kb=${parseAvailableStorageKb(storage)} " +
                "variant=${benchmarkVariant.get()} " +
                "compilation=${compilationMode.get()} " +
                "iterations=${iterations.get()} " +
                "fixture=${fixtureProfile.get()}",
        )
    }

    private fun runOptionalAdb(serial: String, vararg args: String): String {
        return runCatching { runAdb("-s", serial, *args) }.getOrDefault("")
    }

    private fun runAdb(vararg args: String): String {
        val process = ProcessBuilder(adbExecutable.get(), *args)
            .redirectErrorStream(true)
            .start()
        val outputExecutor = Executors.newSingleThreadExecutor()
        val outputFuture = outputExecutor.submit<String> {
            process.inputStream.bufferedReader().use { it.readText() }
        }
        try {
            check(process.waitFor(ADB_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                outputFuture.cancel(true)
                "adb ${args.joinToString(" ")} timed out."
            }
            val output = outputFuture.get(OUTPUT_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            check(process.exitValue() == 0) {
                "adb ${args.joinToString(" ")} failed: ${output.trim()}"
            }
            return output
        } finally {
            outputFuture.cancel(true)
            outputExecutor.shutdownNow()
        }
    }

    private companion object {
        const val ADB_TIMEOUT_SECONDS = 15L
        const val OUTPUT_DRAIN_TIMEOUT_SECONDS = 1L
    }
}

internal data class AdbDeviceEntry(
    val serial: String,
    val state: String,
)

internal fun parseAdbDevices(output: String): List<AdbDeviceEntry> {
    return output.lineSequence()
        .map(String::trim)
        .filter { it.isNotBlank() && !it.startsWith("List of devices attached") }
        .mapNotNull { line ->
            val fields = line.split(Regex("\\s+"))
            fields.getOrNull(1)?.let { state -> AdbDeviceEntry(fields[0], state) }
        }
        .toList()
}

internal fun parseAdbProperty(line: String): Pair<String, String>? {
    val match = Regex("^\\[([^]]+)]\\s*:\\s*\\[([^]]*)]$").find(line.trim()) ?: return null
    return match.groupValues[1] to match.groupValues[2]
}

internal fun isPhysicalDeviceProperties(properties: Map<String, String>): Boolean {
    val fingerprint = properties["ro.build.fingerprint"].orEmpty().lowercase(Locale.ROOT)
    val hardware = properties["ro.hardware"].orEmpty().lowercase(Locale.ROOT)
    val model = properties["ro.product.model"].orEmpty().lowercase(Locale.ROOT)
    return properties["ro.kernel.qemu"] != "1" &&
        !fingerprint.contains("generic") &&
        !fingerprint.contains("emulator") &&
        hardware !in setOf("goldfish", "ranchu", "cutf_cvm") &&
        !model.contains("sdk_gphone")
}

private fun String?.orUnknown(): String = this?.takeIf(String::isNotBlank) ?: "unknown"

private fun parseBatteryLevel(output: String): String {
    return output.lineSequence()
        .firstOrNull { it.trimStart().startsWith("level:") }
        ?.substringAfter(':')
        ?.trim()
        .orUnknown()
}

private fun parseChargingState(output: String): String {
    val powered = output.lineSequence()
        .filter { it.trimStart().matches(Regex("(?:AC|USB|Wireless) powered:.*")) }
        .map { it.substringAfter(':').trim().equals("true", ignoreCase = true) }
        .toList()
    return if (powered.isEmpty()) "unknown" else powered.any { it }.toString()
}

private fun parseThermalState(output: String): String {
    return output.lineSequence()
        .firstOrNull { it.contains("status", ignoreCase = true) }
        ?.substringAfter(':', "")
        ?.trim()
        .orUnknown()
}

private fun parseAvailableStorageKb(output: String): String {
    return output.lineSequence()
        .map { it.trim().split(Regex("\\s+")) }
        .lastOrNull { fields -> fields.size >= 5 && fields.last() == "/data" }
        ?.let { fields -> fields[fields.size - 3] }
        ?.toLongOrNull()
        ?.takeIf { it >= 0L }
        ?.toString()
        .orUnknown()
}

private fun parseRefreshRate(output: String): String {
    return Regex("refreshRate[=: ]+([0-9]+(?:\\.[0-9]+)?)", RegexOption.IGNORE_CASE)
        .find(output)
        ?.groupValues
        ?.getOrNull(1)
        .orUnknown()
}
