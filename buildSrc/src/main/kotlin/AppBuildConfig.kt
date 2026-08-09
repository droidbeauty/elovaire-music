object AppBuildConfig {
    object Android {
        const val compileSdk = 37
        const val minSdk = 30
        const val targetSdk = 36
    }

    object Java {
        const val version = 17
        const val kotlinJvmTarget = "17"
    }

    object Application {
        const val packageName = "elovaire.music.droidbeauty.app"
        const val versionCode = 261001050
        const val versionName = "2.6.5"
        const val macrobenchmarkNamespace = "elovaire.music.droidbeauty.app.macrobenchmark"
    }

    object Testing {
        const val instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
