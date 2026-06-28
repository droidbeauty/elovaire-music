import java.io.File
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    alias(libs.plugins.kotlin.compose)
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
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("github") {
            dimension = "distribution"
            buildConfigField("boolean", "ENABLE_GITHUB_UPDATE_FLOW", "true")
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("boolean", "ENABLE_GITHUB_UPDATE_FLOW", "false")
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

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
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
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.graphics.path)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.extractor)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.haze)
    implementation(libs.jaudiotagger)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}
