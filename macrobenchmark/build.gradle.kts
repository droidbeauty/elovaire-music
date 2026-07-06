plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "elovaire.music.droidbeauty.app.macrobenchmark"
    compileSdk = 37
    experimentalProperties["android.experimental.self-instrumenting"] = true

    val runBenchmarks = providers.gradleProperty("app.runMacrobenchmarks")
        .orElse(
            providers.provider {
                val requestedTasks = gradle.startParameter.taskNames
                requestedTasks
                    .any { taskName ->
                        taskName.endsWith("generateBaselineProfile") ||
                            taskName.endsWith("performanceQualityCheck")
                    }
                    .toString()
            },
        )
    val benchmarkIterations = providers.gradleProperty("app.benchmarkIterations").orNull
    val baselineProfileStableIterations = providers.gradleProperty("app.baselineProfileStableIterations").orNull

    defaultConfig {
        minSdk = 30
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "DEBUGGABLE,EMULATOR,NOT-SELF-INSTRUMENTING"
        testInstrumentationRunnerArguments["elovaire.runBenchmarks"] = runBenchmarks.get()
        benchmarkIterations?.let { value ->
            testInstrumentationRunnerArguments["elovaire.benchmarkIterations"] = value
        }
        baselineProfileStableIterations?.let { value ->
            testInstrumentationRunnerArguments["elovaire.baselineProfileStableIterations"] = value
        }
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit)
}
