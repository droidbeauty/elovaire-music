# Elovaire

Elovaire is a modern Android-only offline music player scaffold focused on elegant album-driven presentation, lightweight local playback, and a codebase that can grow cleanly toward deeper audio features.

## Current foundation

- Kotlin + Jetpack Compose single-app-module project targeting `minSdk 27` and `targetSdk 37`
- Local library scan from the device `Music` folder via `MediaStore`
- Album-centric home screen with horizontal cover flow treatment
- Album detail screen, now playing screen, and adaptive artwork-based gradients
- Offline playback queue powered by Media3 ExoPlayer
- Light, dark, and system-following appearance modes
- 16-band EQ UI plus bass, treble, and spaciousness controls with persisted state

## Deliberate extension seams

- `LyricsRepository` is the integration point for embedded-tag parsing and Genius-backed lyric retrieval
- `PlaybackManager` owns playback state and is the place to connect advanced DSP or a media session service
- `MediaStoreScanner` centralizes local storage discovery and can be expanded for richer metadata extraction

## Notes

- Media3 already covers common offline formats such as MP3, M4A, AAC, WAV, OGG/Vorbis, and Opus. FLAC support is stronger on newer Android versions, but fully universal ALAC and broader long-tail codec coverage still require decoder-extension work.
- The current EQ screen persists settings and is ready for DSP hookup, but it does not yet alter playback output.
- The current lyrics flow reports missing embedded lyrics and keeps the repository boundary ready for a Genius implementation.

## Build

The project includes a Gradle wrapper and is set to use the Android Studio bundled JDK on this machine so it can build cleanly alongside the system Java 26 install.

```bash
./gradlew assembleDebug
```
