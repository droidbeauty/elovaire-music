# Elovaire

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/1.png"
    alt="Elovaire - elegant Android music player for local libraries"
    width="100%"
  />
</p>

---

<p align="center">
  <a href="https://github.com/droidbeauty/elovaire-music/releases/latest">
    <img
      alt="Download the latest Elovaire release"
      src="https://img.shields.io/github/v/release/droidbeauty/elovaire-music?style=for-the-badge&label=Download%20latest&logo=github&logoColor=white&color=3CB371"
    />
  </a>
  &nbsp;
  <a href="https://ko-fi.com/droidbeauty">
    <img
      alt="Support Elovaire on Ko-fi"
      src="https://img.shields.io/badge/Support%20on%20Ko--fi-ff5f5f?style=for-the-badge&logo=kofi&logoColor=white"
    />
  </a>
</p>

<p align="center">
  <b>Your music, refined into an elegant local listening experience</b>
</p>

<p align="center">
  Elovaire is a native Android music player for offline music libraries, built around clean browsing, expressive artwork, smooth playback, and thoughtful audio controls.
</p>

---

## About

Elovaire is designed for people who still care about music stored directly on their device.

The app gives albums, artists, playlists, lyrics, queue controls, and the now-playing screen a polished visual space without making the player feel noisy or overcomplicated. It is made to feel calm, responsive and personal whether you are quickly starting a track or settling into a longer listening session.

Unlike streaming platforms, Elovaire focuses on local files stored on device. It's offline-first, tag-aware and puts highlight on your favorite albums or songs. Streamlined interface keeps library navigation and search organized around your own collection.

## Highlights

- Offline-first playback for music stored on your Android device
- Artwork-led home, library, playlist, search, and now-playing screens
- Elegant, intuitive UI with frosted blur accents
- Smooth navigation and UI transitions
- Built-in update engine based on looking for the latest GitHub release

---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/2.png"
    width="49%"
  />
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/3.png"
    width="49%"
  />
</p>

## Features

- Full now-playing screen with queue, lyrics overlay, volume control, repeat, shuffle, and playback actions
- Compact now-playing bar for quick access while browsing
- Library views for songs, albums, artists, genres, and playlists
- Search with recent history and expandable song results
- Timed and plain lyrics support with local embedded lyrics, sidecar lyric files, and online lookup fallbacks
- 18-band equalizer with presets, bass and treble controls, and visual EQ editing
- Spaciousness presets for wider stereo presentation
- True mono playback toggle that downmixes stereo into centered dual-mono output
- Light, dark, and system theme modes
- Common local audio formats supported through Android Media3 and platform decoders, including MP3, AAC/M4A, ALAC, FLAC, WAV, AIFF, Ogg, and Opus where supported by the device

---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/4.png"
    width="100%"
  />
</p>

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/5.png"
    width="100%"
  />
</p>

---

## Built With

Elovaire is a native Android project built with:

- Kotlin
- Jetpack Compose
- Compose Navigation
- Android Media3 / ExoPlayer
- Android `MediaStore`
- Android Storage Access Framework
- Haze for frosted glass and backdrop blur surfaces
- Gradle Kotlin DSL

---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/6.png"
    width="100%"
  />
</p>

---

## Building

Clone the repository and open it in Android Studio:

```bash
git clone https://github.com/droidbeauty/elovaire-music.git
cd elovaire-music
```

Build a debug APK from the command line:

```bash
./gradlew assembleDebug
```

The generated APK will be available under:

```text
app/build/outputs/apk/debug/
```

## Support

Elovaire is a personal project made in pursuit of a beautiful, focused alternative to streaming-first music apps. Support is optional, but always appreciated.

<p align="center">
  <a href="https://ko-fi.com/droidbeauty">
    <img
      alt="Support Elovaire on Ko-fi"
      src="https://img.shields.io/badge/Leave%20a%20tip-Ko--fi-ff5f5f?style=for-the-badge&logo=kofi&logoColor=white"
    />
  </a>
</p>
