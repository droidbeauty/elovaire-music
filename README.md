# Elovaire

<p align="center">
  <b>An elegant offline music player for Android — designed for local libraries, rich album browsing, and a calmer way to listen.</b>
</p>

<p align="center">
  Elovaire turns your on-device music collection into a polished, artwork-led listening space.
  It keeps playback local, fast, and personal while giving your albums, artists, playlists,
  lyrics, queue, and now-playing screen the visual attention they deserve.
</p>

<table>
  <tr>
    <td align="center" width="50%">
      <b>Latest release</b><br>
      <sub>Get the newest Elovaire APK from GitHub Releases.</sub><br><br>
      <a href="https://github.com/droidbeauty/elovaire-music/releases/latest">
        <img alt="Latest GitHub release" src="https://img.shields.io/github/v/release/droidbeauty/elovaire-music?style=for-the-badge&label=Download&logo=github">
      </a>
    </td>
    <td align="center" width="50%">
      <b>Support the project</b><br>
      <sub>Enjoying Elovaire? A small tip helps keep development going.</sub><br><br>
      <a href="https://ko-fi.com/droidbeauty">
        <img alt="Support on Ko-fi" src="https://img.shields.io/badge/Support%20on-Ko--fi-ff5f5f?style=for-the-badge&logo=kofi&logoColor=white">
      </a>
    </td>
  </tr>
</table>

---

## Why Elovaire?

Most offline music players are built to be useful. Elovaire tries to be useful **and** beautiful.

It is made for people who still care about their local music library: the albums they have collected, the artwork attached to them, the artists they return to, and the feeling of opening an app that does not treat music like a spreadsheet.

Elovaire focuses on a smooth album-first experience, adaptive artwork-driven surfaces, and playback controls that stay close without getting in the way. It is fully offline-first, built around Android’s local media system, and designed to feel refined whether you are quickly starting a song or settling into a full listening session.

## Highlights

- **Offline-first playback** for music stored directly on your Android device
- **Album-first browsing** with dedicated album, artist, playlist, and now-playing views
- **Artwork-led interface** that adapts key surfaces around your music
- **Compact now playing bar** for quick transport controls without leaving your library
- **Full now playing screen** with queue, lyrics overlay, volume control, and playback mode controls
- **Local library scanning** through Android `MediaStore`, including automatic refresh behavior
- **Light, dark, and system theme support**
- **Persisted sound shaping controls** with an equalizer-ready playback architecture
- **Notification playback controls** and media-session integration
- **Release update flow** connected to GitHub Releases

## Built with

Elovaire is built as a native Android app using:

- Kotlin
- Jetpack Compose
- Android Media3 / ExoPlayer
- Android `MediaStore`
- Android Storage Access Framework
- Haze for frosted and blur-driven UI layers

## Project notes

- The app currently targets `minSdk 27` and `targetSdk 37`
- The project uses a single Android app module
- Versioning is centralized in `buildSrc/src/main/kotlin/AppBuildConfig.kt`
- Changelog content is defined in `app/src/main/res/xml/changelog.xml`

## Build from source

The repository includes the Gradle wrapper, so cloning the project should be enough to open it in Android Studio and build a debug APK locally.

```bash
./gradlew assembleDebug
