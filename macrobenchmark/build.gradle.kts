plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = AppBuildConfig.Application.macrobenchmarkNamespace
    compileSdk = AppBuildConfig.Android.compileSdk
    experimentalProperties["android.experimental.self-instrumenting"] = true

    sourceSets.getByName("main").assets.srcDir("../app/src/androidTest/assets")

    val runBenchmarks = providers.gradleProperty("app.runMacrobenchmarks")
        .orElse(
            providers.provider {
                    val requestedTasks = gradle.startParameter.taskNames
                    requestedTasks
                        .any { taskName ->
                            taskName.endsWith("generateBaselineProfile") ||
                                taskName.endsWith("performanceQuality") ||
                                taskName.endsWith("performanceQualityCheck") ||
                                taskName.endsWith("technicalQualification") ||
                                taskName.endsWith("backendQualification") ||
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
            "NOT-SELF-INSTRUMENTING"
        normalizedInstrumentationTestClass(providers.gradleProperty(ANDROID_TEST_CLASS_PROPERTY).orNull)
            ?.let { testInstrumentationRunnerArguments["class"] = it }
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

val compilationToolchain = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(AppBuildConfig.Java.toolchainVersion))
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain>().configureEach {
    kotlinJavaToolchain.toolchain.use(compilationToolchain)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
}
tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(AppBuildConfig.Java.toolchainVersion))
    })
}

tasks.configureEach {
    if (name == "connectedCheck" || name == "connectedBenchmarkAndroidTest") {
        dependsOn(":app:installBenchmark")
        mustRunAfter(":app:installBenchmark")
        mustRunAfter(":requirePhysicalPerformanceDevice")
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit)
}
