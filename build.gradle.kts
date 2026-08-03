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
    dependsOn(":app:lintGithubDebug", ":app:testGithubDebugUnitTest", ":app:detekt", "buildStructureCheck")
}

tasks.register("performanceQualityCheck") {
    group = "verification"
    dependsOn(":app:assembleBenchmark", ":macrobenchmark:connectedCheck", "generateBaselineProfile")
}

tasks.register("releaseQualityCheck") {
    group = "verification"
    dependsOn(":app:verifyReleaseReadiness", "buildHealth")
}

tasks.register<BaselineProfileResultCheckTask>("generateBaselineProfile") {
    group = "verification"
    dependsOn(":macrobenchmark:connectedCheck")
    testResultFiles.from(
        fileTree("macrobenchmark/build/outputs/androidTest-results/connected/debug") {
            include("**/TEST-*.xml")
        },
    )
    generatedProfileFiles.from(
        fileTree("macrobenchmark/build/outputs/connected_android_test_additional_output") {
            include("**/BaselineProfileGenerator_generate-startup-prof.txt")
        },
    )
}

tasks.register("buildHealth") {
    group = "verification"
    dependsOn(
        ":app:analyzeGithubDebugDependencies",
        ":app:analyzePlayReleaseDependencies",
    )
}

val buildStructureCheck = tasks.register("buildStructureCheck") {
    group = "verification"
    description = "Checks that module build scripts use the central build structure."
    val projectRoot = rootProject.projectDir
    val moduleBuildScripts = listOf(
        projectRoot.resolve("app/build.gradle.kts"),
        projectRoot.resolve("macrobenchmark/build.gradle.kts"),
    )
    val settingsFile = projectRoot.resolve("settings.gradle.kts")
    val catalogFile = projectRoot.resolve("gradle/libs.versions.toml")
    val appBuildConfigFile = projectRoot.resolve("buildSrc/src/main/kotlin/AppBuildConfig.kt")
    inputs.files(
        moduleBuildScripts,
        catalogFile,
        appBuildConfigFile,
    )
    doLast {
        val rawDependencies = Regex(
            """(?m)^\s*(?:implementation|api|compileOnly|runtimeOnly|testImplementation|androidTestImplementation|ksp)\s*\(\s*[\"']([^\"']+:[^\"']+:[^\"']+)[\"']""",
        )
        val rawPluginVersions = Regex(
            """(?m)^\s*id\([\"'][^\"']+[\"']\)\s*version\s*[\"'][^\"']+[\"']""",
        )
        val moduleViolations = moduleBuildScripts.flatMap { script ->
            val text = script.readText()
            buildList {
                rawDependencies.findAll(text).forEach { match ->
                    add("${script.path}: raw dependency ${match.groupValues[1]}")
                }
                rawPluginVersions.findAll(text).forEach { match ->
                    add("${script.path}: raw plugin version ${match.value.trim()}")
                }
                if (Regex("(?m)^\\s*repositories\\s*\\{").containsMatchIn(text)) {
                    add("${script.path}: module repositories must be configured in settings.gradle.kts")
                }
            }
        }
        check(moduleViolations.isEmpty()) {
            "Build structure violations found:\n${moduleViolations.joinToString("\n")}"
        }

        val settings = settingsFile.readText()
        check("RepositoriesMode.FAIL_ON_PROJECT_REPOS" in settings) {
            "Dependency repositories must be centrally governed in settings.gradle.kts."
        }
        check("jcenter()" !in settings) { "jcenter() is not allowed in dependency resolution." }

        val catalog = catalogFile.readText()
        check(!Regex("""(?im)^\s*[A-Za-z][A-Za-z0-9_-]*\s*=\s*[\"'][^\"']*(?:latest(?:[.]release)?|SNAPSHOT|[+*])[^\"']*[\"']\s*$""").containsMatchIn(catalog)) {
            "Dynamic dependency or plugin versions are not allowed in gradle/libs.versions.toml."
        }
    }
}

tasks.register("dependencyIntegrityCheck") {
    group = "verification"
    dependsOn(buildStructureCheck)
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
        val dynamicVersion = Regex("""(?i)latest(?:\.release)?|SNAPSHOT|[+*]""")
            .find(catalog)
        check(dynamicVersion == null) { "Dynamic dependency version is forbidden: ${dynamicVersion?.value}" }
        val wrapper = wrapperProperties.asFile.readText()
        check(Regex("(?m)^distributionSha256Sum=[0-9a-f]{64}$").containsMatchIn(wrapper)) {
            "Gradle wrapper distribution must have a pinned SHA-256 checksum."
        }
    }
}
