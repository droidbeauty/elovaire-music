plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.dependency.analysis) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("debugQualityCheck") {
    group = "verification"
    dependsOn(":app:lintDebug", ":app:testDebugUnitTest", ":app:detekt")
}

tasks.register("performanceQualityCheck") {
    group = "verification"
    dependsOn(":app:assembleBenchmark", ":macrobenchmark:connectedCheck", "generateBaselineProfile")
}

tasks.register("releaseQualityCheck") {
    group = "verification"
    dependsOn(":app:verifyReleaseReadiness", "buildHealth")
}

tasks.register("generateBaselineProfile") {
    group = "verification"
    dependsOn(":macrobenchmark:connectedCheck")
}

tasks.register("buildHealth") {
    group = "verification"
    dependsOn(
        ":app:analyzeDebugDependencies",
        ":app:analyzeReleaseDependencies",
    )
}
