import java.io.File
import java.util.Properties
import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.jvm.toolchain.JavaLanguageVersion

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val acoustIdApiKey = providers.gradleProperty("ACOUSTID_API_KEY").orNull
    ?: System.getenv("ACOUSTID_API_KEY")
    ?: localProperties.getProperty("ACOUSTID_API_KEY")
val fanartTvApiKey = providers.gradleProperty("FANART_TV_API_KEY").orNull
    ?: System.getenv("FANART_TV_API_KEY")
    ?: localProperties.getProperty("FANART_TV_API_KEY")
val youtubeDataApiKey = providers.gradleProperty("YOUTUBE_DATA_API_KEY").orNull
    ?: System.getenv("YOUTUBE_DATA_API_KEY")
    ?: localProperties.getProperty("YOUTUBE_DATA_API_KEY")
val privacyPolicyUrl = providers.gradleProperty("PRIVACY_POLICY_URL").orNull
    ?: System.getenv("PRIVACY_POLICY_URL")
    ?: localProperties.getProperty("PRIVACY_POLICY_URL")
    ?: ""
val nativeSanitizersEnabled = providers.gradleProperty("app.nativeSanitizers")
    .map(String::toBoolean)
    .getOrElse(false)
fun releaseSecret(name: String): String? = providers.gradleProperty(name).orNull
    ?: System.getenv(name)
    ?: localProperties.getProperty(name)

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
    namespace = AppBuildConfig.packageName
    compileSdk = AppBuildConfig.compileSdk
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = AppBuildConfig.packageName
        minSdk = AppBuildConfig.minSdk
        targetSdk = AppBuildConfig.targetSdk
        versionCode = AppBuildConfig.versionCode
        versionName = AppBuildConfig.versionName
        buildConfigField(
            "String",
            "ACOUSTID_API_KEY",
            "\"${acoustIdApiKey.orEmpty().replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "FANART_TV_API_KEY",
            "\"${fanartTvApiKey.orEmpty().replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "YOUTUBE_DATA_API_KEY",
            "\"${youtubeDataApiKey.orEmpty().replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "PRIVACY_POLICY_URL",
            "\"${privacyPolicyUrl.replace("\"", "\\\"")}\"",
        )
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DELOVAIRE_NATIVE_SANITIZERS=${if (nativeSanitizersEnabled) "ON" else "OFF"}"
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            ndk.debugSymbolLevel = "SYMBOL_TABLE"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets.named("androidTest") {
        assets.directories.add("schemas")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
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
}

configurations.matching { configuration ->
    configuration.name.endsWith("AndroidTestRuntimeClasspath")
}.configureEach {
    // Room 2.8.4 migration serializers require the 1.8 serializer ABI; Navigation otherwise pins 1.7.3.
    resolutionStrategy.force(
        "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1",
        "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.8.1",
    )
}

androidComponents {
    onVariants(selector().all()) { variant ->
        val buildLabel = variant.buildType ?: variant.name
        val apkFileName = "${AppBuildConfig.packageName}-$buildLabel.apk"
        val aabFileName = "${AppBuildConfig.packageName}-$buildLabel.aab"
        val variantName = variant.name
        val buildDirPath = layout.buildDirectory.asFile.get().absolutePath
        val variantTaskSuffix = variantName.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }

        tasks.matching { task ->
            task.name == "assemble$variantTaskSuffix" ||
                task.name == "bundle$variantTaskSuffix" ||
                task.name == "sign${variantTaskSuffix}Bundle"
        }.configureEach {
            doLast {
                val apkDir = File(buildDirPath, "outputs/apk/$variantName")
                apkDir
                    .listFiles()
                    ?.asList()
                    .orEmpty()
                    .filter { file: File -> file.isFile && file.extension == "apk" && !file.name.contains("androidTest") }
                    .forEach { file: File ->
                        val target = file.parentFile.resolve(apkFileName)
                        if (file.name != apkFileName && file.absolutePath != target.absolutePath) {
                            file.copyTo(target, overwrite = true)
                        }
                    }

                val bundleDir = File(buildDirPath, "outputs/bundle/$variantName")
                bundleDir
                    .listFiles()
                    ?.asList()
                    .orEmpty()
                    .filter { file: File -> file.isFile && file.extension == "aab" }
                    .forEach { file: File ->
                        val target = file.parentFile.resolve(aabFileName)
                        if (file.name != aabFileName && file.absolutePath != target.absolutePath) {
                            file.copyTo(target, overwrite = true)
                        }
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
        jvmTarget = JvmTarget.JVM_17
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
    languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget.set("17")
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
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.room.testing)
}

tasks.register("propertyTest") {
    group = "verification"
    description = "Runs deterministic generated backend property tests."
    dependsOn("testDebugUnitTest")
}

tasks.register("queryPlanCheck") {
    group = "verification"
    description = "Runs device-backed Room query-plan regression checks."
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
    publicPrivacyPolicyUrl.set(privacyPolicyUrl)
}

val validateReleaseNativePageSize = tasks.register<NativePageSizeValidationTask>("validateReleaseNativePageSize") {
    dependsOn("bundleRelease")
    bundleFile.set(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
    minPageSize.set(16 * 1024L)
}

val releaseArtifactInspect = tasks.register<ReleaseArtifactIntegrityTask>("releaseArtifactInspect") {
    dependsOn(
        "bundleRelease",
        "collectReleaseDependencies",
        "mergeReleaseNativeDebugMetadata",
        "sdkReleaseDependencyData",
    )
    bundleFile.set(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
    mappingFile.set(layout.buildDirectory.file("outputs/mapping/release/mapping.txt"))
    nativeSymbolsFile.set(layout.buildDirectory.file("outputs/native-debug-symbols/release/native-debug-symbols.zip"))
    dependencyInventoryFile.set(layout.buildDirectory.file("outputs/sdk-dependencies/release/sdkDependencies.txt"))
    expectedAbis.set(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
    checksumFile.set(layout.buildDirectory.file("reports/release/app-release.aab.sha256"))
}

tasks.register<NativeSanitizerCheckTask>("nativeSanitizerCheck") {
    group = "verification"
    dependsOn("assembleDebug")
    sanitizersEnabled.set(nativeSanitizersEnabled)
}

tasks.register("verifyReleaseReadiness") {
    dependsOn(
        "check",
        "lintRelease",
        "assembleRelease",
        "bundleRelease",
        assertReleaseManifest,
        checkResourceStructure,
        validateReleaseNativePageSize,
        releaseArtifactInspect,
        ":dependencyIntegrityCheck",
        ":buildHealth",
    )
}
