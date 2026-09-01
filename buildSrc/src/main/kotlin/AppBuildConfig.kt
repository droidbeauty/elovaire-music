object AppBuildConfig {
    object Android {
        const val compileSdk = 37
        const val minSdk = 30
        const val targetSdk = 37
    }

    object Java {
        const val version = 17
        const val kotlinJvmTarget = "17"
    }

    object Application {
        const val packageName = "elovaire.music.droidbeauty.app"
        const val versionCode = 261001151
        const val versionName = "2.8.3"
        const val macrobenchmarkNamespace = "elovaire.music.droidbeauty.app.macrobenchmark"
    }

    object Testing {
        const val instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
