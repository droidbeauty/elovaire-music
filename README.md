# Elovaire

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/1.png"
    alt="Elovaire - elegant music listening experience"
    width="100%"
  />
</p>

---

<p align="center">
  <a href="https://github.com/droidbeauty/elovaire-music/releases/latest">
    <img
      alt="Download the latest release"
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
  <b>Your local music library, present like art that it is</b>
</p>

<p align="center">
  Elovaire is Android native music player for people who still love owning their music — minimalistic UI focused around album artwork, customizable playlists and refined audio controls. All designed so you can take full advantage of listening offline
</p>

---

## About

Elovaire turns listening to music into elegant experience - as it should be

It gives your albums, artists and favorite songs an organized place. Intuitive UI feels calm, delivering classy visuals. Artwork is treated as essential part of the experience, the now-playing screen feels spacious so you can listen without interruptions. No ads, no random notifications. Your music under your control

Efficient architecture supports all features across devices running Android 10 through 17. Browse by songs, albums, artists, genres and playlists. Instantly search even through large collection of music, open lyrics when you feel like a superstar, and shape playback with built-in audio controls

## Highlights

- Offline-first playback for music stored directly on your Android device
- Beautiful artwork-led library, playlist, search and now-playing screens
- Full player and compact mini-player for quick control while browsing
- Lyrics support with local and online lookup paths
- 18-band equalizer with built-in presets and additional features
- Smooth Compose UI with frosted blur, animated transitions and adaptive visual details
- Fast library scanning through Android MediaStore and local folder observation
- Built-in update flow based on looking for the latest GitHub release

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

### Library and browsing

- Browse your local collection by songs, albums, artists, genres and playlists
- Artwork-rich album and playlist views
- Fast search with recent history and expandable song results
- Favorites, play counts and recent playback awareness
- Folder-aware library scanning with refresh handling for changed music files
- Support for common local audio containers and codecs through Android Media3 and platform decoders, including MP3, AAC/M4A, FLAC, WAV, Ogg, Opus, AMR, 3GP, MP4 and MKA, with some additional formats depending on device decoder support

### Playback

- Full now-playing screen with queue, lyrics overlay, volume control, repeat, shuffle and playback actions
- Compact now-playing bar for quick access from the rest of the app
- Background playback with media notification controls
- Playback recovery handling for unexpected idle/player states
- USB DAC and direct-output awareness for cleaner output paths where supported by the device

### Lyrics

- Support for manually added lyrics - stored in song metadata
- Local embedded lyrics and sidecar lyric-file lookup
- Online lookup fallbacks when local lyrics are unavailable
- Lyrics overlay designed to stay close to the listening experience without taking over the app

### Audio controls

- 18-band equalizer with visual editing
- Preset support and manual shaping
- Bass and treble controls
- Spaciousness modes for a wider stereo presentation
- Reverb control for added room character
- True mono playback toggle that downmixes stereo into centered dual-mono output

### Interface

- Light, dark and system theme modes
- Adjustable text-size
- Frosted glass effects and backdrop blur surfaces
- Smooth route transitions, animated player surfaces and artwork-driven visual accents
- Built with Jetpack Compose for a modern Android native feel

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

Elovaire is built for Android based on optimized architecture

- Kotlin
- Jetpack Compose
- Compose Navigation
- Android Media3 / ExoPlayer
- Android MediaStore
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
