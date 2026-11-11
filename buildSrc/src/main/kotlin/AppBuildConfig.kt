object AppBuildConfig {
    object Android {
        const val compileSdk = 37
        const val minSdk = 29
        const val targetSdk = 37
    }

    object Java {
        const val toolchainVersion = 27
        const val version = 17
        const val kotlinJvmTarget = "17"
    }

    object Application {
        const val packageName = "elovaire.music.droidbeauty.app"
        const val versionCode = 261001185
        const val versionName = "3.2"
        const val macrobenchmarkNamespace = "elovaire.music.droidbeauty.app.macrobenchmark"
    }

    object Testing {
        const val instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
