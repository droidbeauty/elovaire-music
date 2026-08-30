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
    testImplementation("junit:junit:4.13.2")
}
