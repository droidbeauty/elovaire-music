plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = AppBuildConfig.Application.macrobenchmarkNamespace
    compileSdk = AppBuildConfig.Android.compileSdk
    experimentalProperties["android.experimental.self-instrumenting"] = true

    val runBenchmarks = providers.gradleProperty("app.runMacrobenchmarks")
        .orElse(
            providers.provider {
                    val requestedTasks = gradle.startParameter.taskNames
                    requestedTasks
                        .any { taskName ->
                            taskName.endsWith("generateBaselineProfile") ||
                                taskName.endsWith("performanceQualityCheck") ||
                                taskName.endsWith(":macrobenchmark:connectedCheck") ||
                                taskName.endsWith(":macrobenchmark:connectedBenchmarkAndroidTest") ||
                                taskName.endsWith(":macrobenchmark:connectedDebugAndroidTest")
                        }
                        .toString()
            },
        )
    val benchmarkIterations = providers.gradleProperty("app.benchmarkIterations").orNull
    val baselineProfileStableIterations = providers.gradleProperty("app.baselineProfileStableIterations").orNull

    defaultConfig {
        minSdk = AppBuildConfig.Android.minSdk
        targetSdk = AppBuildConfig.Android.targetSdk
        testInstrumentationRunner = AppBuildConfig.Testing.instrumentationRunner
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,NOT-SELF-INSTRUMENTING"
        testInstrumentationRunnerArguments["elovaire.runBenchmarks"] = runBenchmarks.get()
        benchmarkIterations?.let { value ->
            testInstrumentationRunnerArguments["elovaire.benchmarkIterations"] = value
        }
        baselineProfileStableIterations?.let { value ->
            testInstrumentationRunnerArguments["elovaire.baselineProfileStableIterations"] = value
        }
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += "release"
        }
    }

    targetProjectPath = ":app"
}

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        variantBuilder.enable = variantBuilder.buildType == "benchmark"
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit)
}
