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

val geniusAccessToken = providers.gradleProperty("GENIUS_ACCESS_TOKEN").orNull
    ?: System.getenv("GENIUS_ACCESS_TOKEN")
    ?: localProperties.getProperty("GENIUS_ACCESS_TOKEN")
val acoustIdApiKey = providers.gradleProperty("ACOUSTID_API_KEY").orNull
    ?: System.getenv("ACOUSTID_API_KEY")
    ?: localProperties.getProperty("ACOUSTID_API_KEY")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = AppBuildConfig.packageName
    compileSdk = 37
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = AppBuildConfig.packageName
        minSdk = 30
        targetSdk = 37
        versionCode = AppBuildConfig.versionCode
        versionName = AppBuildConfig.versionName
        buildConfigField(
            "String",
            "GENIUS_ACCESS_TOKEN",
            "\"${geniusAccessToken.orEmpty().replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "ACOUSTID_API_KEY",
            "\"${acoustIdApiKey.orEmpty().replace("\"", "\\\"")}\"",
        )
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    implementation(libs.haze)
    implementation(libs.jaudiotagger)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
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

val assertReleaseManifest = tasks.register<ReleaseManifestCheckTask>("assertReleaseManifest") {
    dependsOn("processReleaseManifest")
    manifestFile.set(
        layout.buildDirectory.file(
            "intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml",
        ),
    )
}

val validateReleaseNativePageSize = tasks.register<NativePageSizeValidationTask>("validateReleaseNativePageSize") {
    dependsOn("bundleRelease")
    bundleFile.set(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
    minPageSize.set(16 * 1024L)
}

tasks.register("verifyReleaseReadiness") {
    dependsOn(
        "check",
        "lintRelease",
        "assembleRelease",
        "bundleRelease",
        assertReleaseManifest,
        validateReleaseNativePageSize,
    )
}
