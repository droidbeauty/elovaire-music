plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(libs.junit)
}
