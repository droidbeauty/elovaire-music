import java.io.File
import java.util.Properties
import com.android.build.api.artifact.SingleArtifact
import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.testing.Test

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun releaseSecret(name: String): String? = providers.gradleProperty(name).orNull
    ?: System.getenv(name)
    ?: localProperties.getProperty(name)

val queryPlanOnly = gradle.startParameter.taskNames.any { taskName ->
    taskName.substringAfterLast(':') == "queryPlanCheck"
}

val releaseStoreFile = releaseSecret("RELEASE_STORE_FILE")
val releaseStorePassword = releaseSecret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = releaseSecret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = releaseSecret("RELEASE_KEY_PASSWORD")
val releaseSigningValues = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
if (releaseSigningValues.any { it != null } && releaseSigningValues.any { it.isNullOrBlank() }) {
    error("Release signing requires RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, and RELEASE_KEY_PASSWORD.")
}
val releaseSigningConfigured = releaseSigningValues.all { !it.isNullOrBlank() }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = AppBuildConfig.Application.packageName
    compileSdk = AppBuildConfig.Android.compileSdk

    defaultConfig {
        applicationId = AppBuildConfig.Application.packageName
        minSdk = AppBuildConfig.Android.minSdk
        targetSdk = AppBuildConfig.Android.targetSdk
        versionCode = AppBuildConfig.Application.versionCode
        versionName = AppBuildConfig.Application.versionName
        testInstrumentationRunner = AppBuildConfig.Testing.instrumentationRunner
        if (queryPlanOnly) {
            testInstrumentationRunnerArguments["class"] =
                "elovaire.music.droidbeauty.app.data.library.db.RoomQueryPlanQualificationTest"
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(AppBuildConfig.Java.version)
        targetCompatibility = JavaVersion.toVersion(AppBuildConfig.Java.version)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets.named("androidTest") {
        assets.directories.add("schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/DebugProbesKt.bin"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        error += setOf(
            "PrivateApi",
            "BlockedPrivateApi",
            "SoonBlockedPrivateApi",
            "DiscouragedPrivateApi",
        )
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

configurations.matching { configuration ->
    configuration.name.endsWith("AndroidTestRuntimeClasspath")
}.configureEach {
    // Room 2.8.4 migration serializers require the 1.8 serializer ABI; Navigation otherwise pins 1.7.3.
    resolutionStrategy.force(
        libs.kotlinx.serialization.core,
        libs.kotlinx.serialization.core.jvm,
    )
}

androidComponents {
    onVariants(selector().all()) { variant ->
        val buildLabel = variant.buildType ?: variant.name
        val apkFileName = "${AppBuildConfig.Application.packageName}-$buildLabel.apk"
        val variantName = variant.name
        val buildDirPath = layout.buildDirectory.asFile.get().absolutePath
        val apkArtifact = variant.artifacts.get(SingleArtifact.APK)
        val variantTaskSuffix = variantName.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }

        if (variantName == "debug" || variantName == "release") {
            val installerOutput = layout.buildDirectory.file("$variantName/$apkFileName")
            tasks.matching { task -> task.name == "assemble$variantTaskSuffix" }.configureEach {
                outputs.file(installerOutput)
                doLast {
                    val installerDir = File(buildDirPath, variantName)
                    installerDir.mkdirs()
                    installerDir.listFiles()
                        ?.filter { file -> file.extension == "apk" }
                        .orEmpty()
                        .forEach(File::deleteRecursively)

                    val apkArtifactFile = apkArtifact.get().asFile
                    val sourceApk = if (apkArtifactFile.isFile) {
                        apkArtifactFile
                    } else {
                        apkArtifactFile.listFiles()
                            ?.filter { file -> file.isFile && file.extension == "apk" && !file.name.contains("androidTest") }
                            ?.singleOrNull()
                    } ?: error("$variantName APK was not generated.")
                    sourceApk.copyTo(
                        target = installerDir.resolve(apkFileName),
                        overwrite = true,
                    )
                }
            }
        }
    }
}

if (providers.gradleProperty("app.r8Diagnostics").map(String::toBoolean).getOrElse(false)) {
    android.buildTypes.named("release").configure {
        proguardFile("r8-diagnostics.pro")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(AppBuildConfig.Java.kotlinJvmTarget)
    }
}

ksp {
    arg("room.schemaLocation", file("schemas").path)
}

if (providers.gradleProperty("app.composeCompilerReports").map(String::toBoolean).getOrElse(false)) {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
        metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
    }
}

detekt {
    buildUponDefaultConfig = false
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    source.setFrom(
        files(
            "src/main/java",
            "src/test/java",
            "src/androidTest/java",
        ),
    )
}

val detektJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(AppBuildConfig.Java.version))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget.set(AppBuildConfig.Java.kotlinJvmTarget)
    jdkHome.set(detektJavaLauncher.map { launcher ->
        launcher.metadata.installationPath
    })
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.tracing.ktx)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.extractor)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.metrics.performance)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.haze)
    implementation(libs.jaudiotagger)
    implementation(libs.smbj)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestImplementation(libs.androidx.room.testing)
}

tasks.register<Test>("propertyTest") {
    group = "verification"
    description = "Runs deterministic property, invariant, and generated-sequence tests only."
    val debugUnitTest = tasks.named<Test>("testDebugUnitTest")
    testClassesDirs = debugUnitTest.get().testClassesDirs
    classpath = debugUnitTest.get().classpath
    useJUnit()
    filter {
        includeTestsMatching("*PropertyTest")
        includeTestsMatching("*InvariantTest")
        includeTestsMatching("*InvariantsTest")
        includeTestsMatching("*GeneratedSequenceTest")
    }
}

tasks.register("queryPlanCheck") {
    group = "verification"
    description = "Runs the focused device-backed Room query-plan qualification test."
    dependsOn("connectedDebugAndroidTest")
}

val checkHiddenApiUsage = tasks.register<HiddenApiUsageCheckTask>("checkHiddenApiUsage") {
    projectDirectory.set(layout.projectDirectory)
    sourceFiles.from(
        fileTree("src/main/java") {
            include("**/*.kt", "**/*.java")
        },
        fileTree("src/main/cpp") {
            include("**/*.cpp", "**/*.h", "**/*.c", "**/*.cc", "**/*.hpp")
        },
    )
    riskyPatterns.set(
        listOf(
            "com.android.internal",
            "android.os.SystemProperties",
            "VMRuntime",
            "sun.misc.Unsafe",
            "setAccessible",
            "getDeclaredMethod",
            "getDeclaredField",
            "android.os.MessageQueue",
            "MessageQueue::class",
            "MessageQueue.class",
            "java.lang.reflect.Modifier",
            "GetMethodID",
            "GetFieldID",
            "FindClass",
            "RegisterNatives",
            "dlopen",
            "dlsym",
            "__system_property_get",
        ),
    )
}

val checkDeprecatedAndroidApiUsage = tasks.register<DeprecatedAndroidApiUsageCheckTask>("checkDeprecatedAndroidApiUsage") {
    projectDirectory.set(layout.projectDirectory)
    sourceFiles.from(
        fileTree("src/main/java") {
            include("**/*.kt", "**/*.java")
        },
    )
}

tasks.named("check") {
    dependsOn(checkHiddenApiUsage)
    dependsOn(checkDeprecatedAndroidApiUsage)
}

val checkArchitectureBoundaries = tasks.register<ArchitectureBoundaryCheckTask>("checkArchitectureBoundaries") {
    sourceFiles.from(
        fileTree("src/main") {
            include("**/*.kt", "**/*.java", "**/*.xml")
        },
        rootProject.file("README.md"),
    )
}

val checkResourceStructure = tasks.register<ResourceStructureCheckTask>("checkResourceStructure") {
    resourceFiles.from(
        fileTree("src/main/res"),
        fileTree("src/debug/res"),
        fileTree("src/release/res"),
    )
    assetFiles.from(
        fileTree("src/main/assets"),
        fileTree("src/debug/assets"),
        fileTree("src/release/assets"),
    )
    sourceFiles.from(
        fileTree("src/main/java") {
            include("**/*.kt")
        },
    )
    profileFiles.from(
        fileTree("src/main/baselineProfiles") {
            include("*.txt")
        },
    )
}

tasks.named("check") {
    dependsOn(checkArchitectureBoundaries)
    dependsOn(checkResourceStructure)
}

val assertReleaseManifest = tasks.register<ReleaseManifestCheckTask>("assertReleaseManifest") {
    dependsOn("processReleaseManifest")
    manifestFile.set(
        layout.buildDirectory.file(
            "intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml",
        ),
    )
    backupRulesFile.set(layout.projectDirectory.file("src/main/res/xml/backup_rules.xml"))
    dataExtractionRulesFile.set(layout.projectDirectory.file("src/main/res/xml/data_extraction_rules.xml"))
    baselineProfileFile.set(layout.projectDirectory.file("src/main/baselineProfiles/baseline-prof.txt"))
    startupProfileFile.set(layout.projectDirectory.file("src/main/baselineProfiles/startup-prof.txt"))
}

val validateReleaseNativePageSize = tasks.register<NativePageSizeValidationTask>("validateReleaseNativePageSize") {
    dependsOn("assembleRelease")
    apkFile.set(layout.buildDirectory.file("release/${AppBuildConfig.Application.packageName}-release.apk"))
    minPageSize.set(16 * 1024L)
}

val releaseArtifactInspect = tasks.register<ReleaseArtifactIntegrityTask>("releaseArtifactInspect") {
    dependsOn(
        "assembleRelease",
        "collectReleaseDependencies",
        "sdkReleaseDependencyData",
    )
    apkFile.set(layout.buildDirectory.file("release/${AppBuildConfig.Application.packageName}-release.apk"))
    mappingFile.set(layout.buildDirectory.file("outputs/mapping/release/mapping.txt"))
    dependencyInventoryFile.set(layout.buildDirectory.file("outputs/sdk-dependencies/release/sdkDependencies.txt"))
    checksumFile.set(layout.buildDirectory.file("release/${AppBuildConfig.Application.packageName}-release.apk.sha256"))
}

tasks.register("verifyReleaseReadiness") {
    dependsOn(
        "check",
        "lintRelease",
        "assembleRelease",
        assertReleaseManifest,
        checkResourceStructure,
        validateReleaseNativePageSize,
        releaseArtifactInspect,
        ":dependencyIntegrityCheck",
        ":buildHealth",
    )
}
