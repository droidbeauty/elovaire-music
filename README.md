# Elovaire

Elovaire is an Android music player built for local libraries, refined playback, and a more atmospheric visual experience than the usual utilitarian offline player. It focuses on album-first browsing, adaptive artwork-driven surfaces, and a UI that still feels calm and intentional while staying fully offline-first.

## Core features

- Offline playback for local music stored on the device
- Album-first navigation with dedicated album, artist, playlist, and now playing views
- Adaptive artwork-led visuals across playback surfaces and key cards
- Compact now playing bar with quick access to transport controls
- Full now playing screen with queue, lyrics overlay, volume control, and playback mode controls
- Local library scanning through Android `MediaStore`, including automatic refresh behavior
- Light, dark, and system theme support with custom in-app appearance controls
- Persisted sound shaping controls and equalizer-ready playback architecture
- Notification playback controls and media-session integration
- Release-update flow wired to GitHub releases

## Built with

- Kotlin
- Jetpack Compose
- Android Media3 / ExoPlayer
- Android `MediaStore`
- Android Storage Access Framework
- Haze for frosted / blur-driven UI layers

## Project notes

- The app currently targets `minSdk 27` and `targetSdk 37`
- The project uses a single Android app module
- Versioning is centralized in `buildSrc/src/main/kotlin/AppBuildConfig.kt`
- Changelog content is defined in `app/src/main/res/xml/changelog.xml`

## Build

The repository includes the Gradle wrapper, so cloning it should be enough for another developer to open the project in Android Studio and build a debug APK locally.

```bash
./gradlew assembleDebug
```
