plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.dependency.analysis) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val checkTrackedSourceSecrets = tasks.register("checkTrackedSourceSecrets") {
    group = "verification"
    description = "Rejects high-signal credentials in tracked source files."
    val projectRoot = layout.projectDirectory.asFile
    doLast {
        val sourceExtensions = setOf("c", "cc", "cpp", "gradle", "h", "hpp", "java", "json", "kt", "kts", "properties", "sh", "toml", "xml", "yaml", "yml")
        val trackedFiles = ProcessBuilder("git", "ls-files", "-z")
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()
            .let { process ->
                val output = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                check(process.waitFor() == 0) { "Unable to enumerate tracked source files." }
                output.split('\u0000').filter(String::isNotBlank)
            }
        val credentialPatterns = listOf(
            "AIza[0-9A-Za-z_-]{20,}".toRegex() to "Google API key",
            "ghp_[A-Za-z0-9]{20,}".toRegex() to "GitHub token",
            "github_pat_[A-Za-z0-9_]{20,}".toRegex() to "GitHub fine-grained token",
            "-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----".toRegex() to "private key",
            "AKIA[0-9A-Z]{16}".toRegex() to "AWS access key",
            "(?i)Authorization\\s*:\\s*Bearer\\s+[A-Za-z0-9._~+/=-]{16,}".toRegex() to "Bearer credential",
            "(?i)client[_-]?secret\\s*[:=]\\s*[\\\"'][^\\\"'\\n]{16,}[\\\"']".toRegex() to "client secret",
        )
        val violations = trackedFiles.asSequence()
            .filter { path -> path.substringAfterLast('.', "") in sourceExtensions }
            .flatMap { path ->
                val file = projectRoot.resolve(path)
                if (!file.isFile) return@flatMap emptySequence()
                val text = file.readText()
                credentialPatterns.asSequence().mapNotNull { (pattern, category) ->
                    pattern.find(text)?.let { match -> "$path: $category (${match.value.take(8)})" }
                }
            }
            .distinct()
            .toList()
        check(violations.isEmpty()) {
            "High-signal credentials found in tracked source:\n${violations.joinToString("\n")}"
        }
    }
}

tasks.register("debugQualityCheck") {
    group = "verification"
    dependsOn(":app:lintDebug", ":app:testDebugUnitTest", ":app:detekt", "buildStructureCheck", checkTrackedSourceSecrets)
}

tasks.register("performanceQualityCheck") {
    group = "verification"
    dependsOn(":app:assembleBenchmark", ":macrobenchmark:connectedCheck", "generateBaselineProfile")
}

tasks.register("releaseQualityCheck") {
    group = "verification"
    dependsOn(":app:verifyReleaseReadiness", "buildHealth", checkTrackedSourceSecrets)
}

tasks.register<BaselineProfileResultCheckTask>("generateBaselineProfile") {
    group = "verification"
    dependsOn(":macrobenchmark:connectedCheck")
    testResultFiles.from(
        fileTree("macrobenchmark/build/outputs/androidTest-results/connected/benchmark") {
            include("**/TEST-*.xml")
        },
    )
    generatedProfileFiles.from(
        fileTree("macrobenchmark/build/outputs/connected_android_test_additional_output/benchmark") {
            include("**/BaselineProfileGenerator_generate-startup-prof.txt")
        },
    )
}

tasks.register("buildHealth") {
    group = "verification"
    dependsOn(
        ":app:analyzeDebugDependencies",
        ":app:analyzeReleaseDependencies",
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
