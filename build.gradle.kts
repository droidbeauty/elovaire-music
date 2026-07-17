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

tasks.register("dependencyIntegrityCheck") {
    group = "verification"
    val verificationMetadata = layout.projectDirectory.file("gradle/verification-metadata.xml")
    val versionCatalog = layout.projectDirectory.file("gradle/libs.versions.toml")
    val wrapperProperties = layout.projectDirectory.file("gradle/wrapper/gradle-wrapper.properties")
    inputs.files(verificationMetadata, versionCatalog, wrapperProperties)
    doLast {
        val verification = verificationMetadata.asFile.readText()
        check("<sha256 value=" in verification) { "Gradle dependency verification has no SHA-256 entries." }
        val trustedArtifacts = Regex("<trust file=\"([^\"]+)\"")
            .findAll(verification)
            .map { match -> match.groupValues[1] }
            .toSet()
        check(
            trustedArtifacts == setOf(
                ".*-javadoc[.]jar",
                ".*-sources[.]jar",
                "gradle-[0-9.]+-src[.]zip",
            ),
        ) { "Only IDE source and documentation artifacts may bypass dependency verification." }
        val catalog = versionCatalog.asFile.readText()
        val dynamicVersion = Regex("""(?m)^\s*\w+\s*=\s*\"(?:latest\.|[^\"]*[+*]|[^\"]*-SNAPSHOT)\"""")
            .find(catalog)
        check(dynamicVersion == null) { "Dynamic dependency version is forbidden: ${dynamicVersion?.value}" }
        val wrapper = wrapperProperties.asFile.readText()
        check(Regex("(?m)^distributionSha256Sum=[0-9a-f]{64}$").containsMatchIn(wrapper)) {
            "Gradle wrapper distribution must have a pinned SHA-256 checksum."
        }
    }
}
